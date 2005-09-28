/*
 * Created on Jul 22, 2005
 */
package org.tigr.microarray.mev.r;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.Manager;
import org.tigr.microarray.mev.MultipleArrayMenubar;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;

/**
 * @author iVu
 */
public class Rama {
	public static String COMMA = ",";
	public static String END_LINE = "\r\n";
	public static String TAB = "\t";
	public static String R_VECTOR_NAME = "hiv";
	
	private MultipleArrayViewer mav;
	private MultipleArrayMenubar menuBar;
	
	
	/**
	 * 
	 * @param mavP
	 * @param menuBarP
	 */
	public Rama( MultipleArrayViewer mavP, MultipleArrayMenubar menuBarP ) {
		this.mav = mavP;
		this.menuBar = menuBarP;
			
		//gather up necessary data
		IData data = this.mav.getData();
		
		//might as well verify that there is even enough data loaded to continue
		if( data.getFeaturesCount() < 2 ) {
			//not enough to normalize
			this.error( "The loaded dataset doesn't appear to be \"Ramalizable\"" 
					+ "\r\nYou must have at least 2 replicates of each sample" );
		} else if( data.getDataType() == IData.DATA_TYPE_RATIO_ONLY ) {
			this.error( "Rama does not work on Ratio data.\nIt only works with Intensity data." );
		} else if( data.getDataType() == IData.DATA_TYPE_AFFY_MEAN ) {
			this.error( "Rama does not work on Ratio data.\nIt only works with Intensity data." );
		} else if( data.getDataType() == IData.DATA_TYPE_AFFY_MEDIAN ) {
			this.error( "Rama does not work on Ratio data.\nIt only works with Intensity data." );
		} else if( data.getDataType() == IData.DATA_TYPE_AFFY_REF ) {
			this.error( "Rama does not work on Ratio data.\nIt only works with Intensity data." );
		} else if( data.getDataType() == IData.DATA_TYPE_AFFY_ABS ) {
			//deal with Affy Data
			//System.out.println( "Affy Abs" );
			this.ramify( data, true );
		} else {
			this.ramify( data, false );
		}//end else - data looks like it may work
	}//constructor
		
	
	/**
	 * 
	 * @param data
	 */
	private void ramify( IData data, boolean isAffy ) {
		//we need to gather up the data and format it for RAMA
		String[] hybNames = new String[ data.getFeaturesCount() ];
		for( int h = 0; h < hybNames.length; h ++ ) {
			hybNames[ h ] = data.getFullSampleName( h );
		}
		
		//Display a dialog for initialization
		RamaInitDialog initDialog;
		
		//deal with two-color and affy data differently
		if( isAffy ) {
			initDialog = new RamaInitDialog( this.mav.getFrame(), 
					hybNames, IData.DATA_TYPE_AFFY_ABS );
		} else {
			initDialog = new RamaInitDialog( this.mav.getFrame(), 
					hybNames, IData.DATA_TYPE_TWO_INTENSITY );
		}
		
		//make sure that the user clicked OK
		if( initDialog.showModal() == JOptionPane.OK_OPTION ) {
			//get the advanced parameters
			int B = initDialog.getNumIter();
			int minIter = initDialog.getBurnIn();
			boolean allOut = initDialog.getAllOut();
			String sConnPort = initDialog.getSelectedConnString();
			int iPort = this.parseIPort( sConnPort );
			String sConn = this.parseSPort( sConnPort );
			
			//Object to format MeV-IData structure into R data String
			RDataFormatter rDataFormatter = new RDataFormatter( data );
			
			//declare all the necessary variables for the mcmc
			String sData;
			int iGene;
			int nbCol1;
			int iHybKount;
			int iColorKount;
			int iTwo;
			
			RamaHybSet rhs = initDialog.getRamaHybSet();
			
			//deal with affy and 2 color differently
			if( isAffy ) {	//this is an affy array
				sData =  rDataFormatter.ramaNonSwapString( rhs.getVRamaHyb() );
				iGene = data.getExperiment().getNumberOfGenes();
				nbCol1 = 0;
				iHybKount = rhs.getVRamaHyb().size();
				iColorKount = iHybKount * 2;
				iTwo = iHybKount + 1;
			} else {	//this is a 2 color array
				if( rhs.isFlip() ) {	//dealing with a dye swap experiment
					//Split into color state
					Vector vTreatCy3 = this.getVRamaHybTreatCy3( rhs.getVRamaHyb() );
					Vector vTreatCy5 = this.getVRamaHybTreatCy5( rhs.getVRamaHyb() );
					
					//sData =  this.swapDataString( data, vTreatCy3, vTreatCy5 );
					sData = rDataFormatter.ramaSwapString( vTreatCy3, vTreatCy5 );
					iGene = data.getExperiment().getNumberOfGenes();
					nbCol1 = vTreatCy3.size();
					iHybKount = vTreatCy3.size() + vTreatCy5.size();
					iColorKount = iHybKount * 2;
					iTwo = iHybKount + 1;
				} else {	//not dye swap
					//sData =  this.nonSwapDataString( data, rhs.getVRamaHyb() );
					sData = rDataFormatter.ramaNonSwapString( rhs.getVRamaHyb() );
					iGene = data.getExperiment().getNumberOfGenes();
					nbCol1 = 0;
					iHybKount = rhs.getVRamaHyb().size();
					iColorKount = iHybKount * 2;
					iTwo = iHybKount + 1;
				}//end rhs.isFlip() else
			}
			
			//try to get a connection
			RconnectionManager rcMan = new RconnectionManager( this.mav.getFrame(), sConn, iPort );
			Rconnection rc = rcMan.getConnection();
			
			//don't continue if we can't get a connection
			if( rc != null ) {
				try {
					//should clear R
					rc.voidEval( "rm( " + Rama.R_VECTOR_NAME + " )" );
					
					//load rama
					rc.voidEval( "library(rama)" );
					
					//load data as vector
					rc.voidEval( sData );
					
					//reform vector data into matrix
					rc.voidEval( "dim(" + Rama.R_VECTOR_NAME + ") <- c(" + iGene + "," 
							+ iColorKount + ")" );
					
					if( rhs.isFlip() ) {
						//call fit.model()
						rc.voidEval( this.createMcMc( allOut, iGene, iHybKount, 
								iTwo, iColorKount, B, minIter, nbCol1, true ) );
					} else {
						//call fit.model()
						rc.voidEval( this.createMcMc( allOut, iGene, iHybKount, 
								iTwo, iColorKount, B, minIter, 0, false ) );
					}
					
					//if the user wishes, output credible intervals
					double[] qLo;
					double[] qUp;
					if( allOut ) {
						//average the rows when doing all.out = TRUE
						rc.voidEval( "gamma1<-mat.mean(mcmc.hiv$gamma1)[,1]" );
						rc.voidEval( "gamma2<-mat.mean(mcmc.hiv$gamma2)[,1]" );
														  
						qLo = rc.eval( "mcmc.hiv$q.low" ).asDoubleArray();
						qUp = rc.eval( "mcmc.hiv$q.up" ).asDoubleArray();
					} else {
						qLo = new double[ 0 ];
						qUp = new double[ 0 ];
					}
					
					//get normalized data vectors
					double[] gamma1 = rc.eval( "gamma1" ).asDoubleArray();
					double[] gamma2 = rc.eval( "gamma2" ).asDoubleArray();
					
					//get the shift
					double shift = rc.eval( "mcmc.hiv$shift" ).asDouble();
					
					//need to know the gene annotation data
					String[] geneNames = new String[ iGene ];
					for( int g = 0; g < iGene; g ++ ) {
						geneNames[ g ] = data.getGeneName( g );
					}
					
					//save
			        this.onSave( gamma1, gamma2, qLo, qUp, allOut, geneNames );
			        
			        //seemed to have worked so save the connection strings
			        if( initDialog.connAdded() ) {
			        	TMEV.updateRPath( initDialog.getRPathToWrite() );
			        }
					
					MultipleArrayViewer newMav = this.spawnNewMav( data, gamma1, 
							gamma2, geneNames, shift );
					RamaSummaryViewer sumViewer = new RamaSummaryViewer( shift, 
							B, minIter, rc, newMav.getFrame() );
					LeafInfo li = new LeafInfo( "Rama Summary", sumViewer );
					DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Rama" );
					node.add( new DefaultMutableTreeNode( li ) );
					newMav.addAnalysisResult( node );
				} catch( RSrvException e ) {
					e.printStackTrace();
					this.error( e.getMessage() );
				}
			} else {	//end if( rc != null )
				//couldn't get a connection
				System.out.println( "MeV could not establish a connection with Rserve" );
			}
			
			//System.out.println("Done Rama.142" );
		}//end if( RamaInitDialog.showModal == JOptionPane.OK_OPTION )
	}//ramaTwoIntensityData()
	
	
	/**
	 * 
	 * @param allOut
	 * @param iGene
	 * @param iHybKount
	 * @param iTwo
	 * @param iColorKount
	 * @param B
	 * @param minIter
	 * @param nbCol1
	 * @param isDyeSwap
	 * @return
	 */
	private String createMcMc( boolean allOut, int iGene, int iHybKount, int iTwo, 
			int iColorKount, int B, int minIter, int nbCol1, boolean isDyeSwap ) {
		StringBuffer sb = new StringBuffer();
		sb.append( "mcmc.hiv <- fit.model( hiv[ 1:" );
		sb.append( iGene );
		sb.append( " , c( 1:" );
		sb.append( iHybKount );
		sb.append( " )], hiv[ 1:" );
		sb.append( iGene );
		sb.append( ", c( " );
		sb.append( iTwo );
		sb.append( ":" );
		sb.append( iColorKount );
		sb.append( " )], B = " );
		sb.append( B );
		sb.append( ", min.iter = " );
		sb.append( minIter );
		sb.append( ", batch = 1, shift = NULL, mcmc.obj = NULL, dye.swap = " );
		if( isDyeSwap ) {
			sb.append( "TRUE" );
			sb.append( ", nb.col1 = " );
			sb.append( nbCol1 );
		} else {
			sb.append( "FALSE" );
		}
		if( allOut ) {
			sb.append( ", all.out = TRUE" );
		} else {
			sb.append( ", all.out = FALSE )" );
		}
		sb.append( ")" );
		//System.out.println(sb.toString());
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param gamma1
	 * @param gamma2
	 * @param genes
	 */
	private MultipleArrayViewer spawnNewMav( IData data, double[] gamma1, double[] gamma2, 
			String[] genes, double shift ) {
		//get the field names before they're cleared from creating a new mav
		String[] currentFieldNames = TMEV.getFieldNames();
		
		//create the new mav
		Manager.createNewMultipleArrayViewer( 20, 20 );
		
		//reset the fieldnames
		TMEV.setFieldNames( currentFieldNames );
		
		//un log2 transform the ramafied intensities
		double[] norm1 = this.unLogify( gamma1, 2 );
		double[] norm2 = this.unLogify( gamma2, 2 );
		//double[] norm1 = gamma1;
		//double[] norm2 = gamma2;
		
		//array to hold SlideData objects for mav
		ISlideData[] features = new ISlideData[ 1 ];
		
		//SlideData will be iNumGenes (rows) by 1 column
		SlideData slideData = new SlideData( gamma1.length,1 );
		//loop through the genes
		for( int i = 0; i < genes.length; i ++ ) {
			int[] rows = new int[ 3 ];
			int[] cols = new int[ 3 ];
			float[] intensities = new float[ 2 ];
			//[ 0 ] is R, [ 1 ] is MR, [ 2 ] is SR
			rows[ 0 ] = ( i + 1 );
			rows[ 1 ] = 1;
			rows[ 2 ] = 0;
			cols[ 0 ] = 1;
			cols[ 1 ] = 1;
			cols[ 2 ] = 0;
			//intensities[ 0 ] is Cy3, [ 1 ] is Cy5
			intensities[ 0 ] = ( float ) norm1[ i ];
			intensities[ 1 ] = ( float ) norm2[ i ];
			
			//take care of annotations
			String[] extraFields = new String[ currentFieldNames.length ];
			SlideDataElement loadedSDE = ( SlideDataElement ) data.getSlideDataElement( 0, i );
			for( int e = 0; e < extraFields.length; e ++ ) {
				extraFields[ e ] = loadedSDE.getFieldAt( e );
			}
			
			SlideDataElement sde = new SlideDataElement( data.getUniqueId( i ), 
					rows, cols, intensities, extraFields );
			slideData.add( sde );
		}
		
		features[ 0 ] = slideData;
		features[ 0 ].setSlideFileName( "Rama Intensities" );
		features[ 0 ].setSlideDataName( "Rama Intensities" );
		
		MultipleArrayViewer newMav = ( MultipleArrayViewer ) Manager.getLastComponent();
		newMav.fireDataLoaded( features, IData.DATA_TYPE_TWO_INTENSITY );
		this.mav.getFrame().dispose();
		return newMav;
	}//spawnNewMav()
	
	
	/**
	 * Takes a connection string in the form ipaddress:port# and returns just the
	 * port number part
	 * @param connPort
	 * @return
	 */
	private int parseIPort( String connPort ) {
		if( connPort == null ) {
			return 6311;
		} else {
			int iColon = connPort.indexOf( ":" );
			if( iColon == -1 ) {
				return 6311;
			} else {
				int toReturn = Integer.parseInt( connPort.substring( iColon + 1 ) );
				return toReturn;
			}
		}
	}
	/**
	 * Takes a connection string in the form ipaddress:port# and returns just the
	 * ipaddress part
	 * @param connPort
	 * @return
	 */
	private String parseSPort( String connPort ) {
		if( connPort == null ) {
			return "127.0.0.1";
		} else {
			int iColon = connPort.indexOf( ":" );
			if( iColon == -1 ) {
				return "127.0.0.1";
			} else {
				return connPort.substring( 0, iColon );
			}
		}
	}
	
	
	
	/**
	 * Creates a Vector of RamaHybs where: treated-Cy3 | control-Cy5
	 * @return
	 */
	private Vector getVRamaHybTreatCy3( Vector ramaHybs ) {
		Vector vReturn = new Vector();
		
		for( int h = 0; h < ramaHybs.size(); h ++ ) {
			RamaHyb hyb = ( RamaHyb ) ramaHybs.elementAt( h );
			if( ! hyb.controlCy3() ) {
				vReturn.add( hyb );
			}
		}
		
		return vReturn;
	}
	/**
	 * Creates a Vector of RamaHybs where: control-Cy3 | treated-Cy5
	 * @return
	 */
	private Vector getVRamaHybTreatCy5( Vector ramaHybs ) {
		Vector vReturn = new Vector();

		for( int h = 0; h < ramaHybs.size(); h ++ ) {
			RamaHyb hyb = ( RamaHyb ) ramaHybs.elementAt( h );
			if( hyb.controlCy3() ) {
				vReturn.add( hyb );
			}
		}
		
		return vReturn;
	}//
	
	
	/**
	 * Just un Log transforms a double array by base
	 * @param log
	 * @param base
	 * @return
	 */
	private double[] unLogify( double[] log, int base ) {
		double[] toReturn = new double[ log.length ];
		
		for( int i = 0; i < log.length; i ++ ) {
			double d = log[ i ];
			double y = Math.pow( 2, d );
			toReturn[ i ] = y;
		}
		
		return toReturn;
	}//unLogify()


    /**
     * 
     * @param gamma1
     * @param gamma2
     * @param genes
     */
    private void onSave( double[] gamma1, double[] gamma2, double[] qLo, 
    		double[] qUp, boolean allOut, String[] genes) {
        String currentPath = TMEV.getDataPath();
        RamaTextFileFilter textFilter = new RamaTextFileFilter();
        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.addChoosableFileFilter(textFilter);
        if( chooser.showSaveDialog(this.mav.getFrame()) == JFileChooser.APPROVE_OPTION ) {
            File saveFile;

            if( chooser.getFileFilter() == textFilter ) {
                //make sure to add .txt
                String path = chooser.getSelectedFile().getPath();
                if( path.toLowerCase().endsWith("txt") ) {
                    //great, already ok
                    saveFile = new File(path);
                } else {
                    //add it
                    String subPath;
                    int period = path.lastIndexOf(".");
                    if( period != -1 ) {
                        System.out.println("period  = -1");
                        subPath = path.substring(0, period);
                    } else {
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    saveFile = new File(newPath);
                }
            } else {
                saveFile = chooser.getSelectedFile();
            }
            StringBuffer sb = new StringBuffer();
            sb.append( "GeneName" );
            sb.append( Rama.TAB );
            sb.append( "IntensityA" );
            sb.append( Rama.TAB );
            sb.append( "IntensityB" );
            if( allOut ) {
            	sb.append( Rama.TAB );
            	sb.append( "qLow" );
            	sb.append( Rama.TAB );
            	sb.append( "qUp" );
            }
            sb.append( Rama.END_LINE );
            
            for( int i = 0; i < genes.length; i++ ) {
            	sb.append( genes[ i ] );
                sb.append( Rama.TAB );
            	sb.append( gamma1[ i ] );
                sb.append( Rama.TAB );
            	sb.append( gamma2[ i ] );
            	if( allOut ) {
            		sb.append( Rama.TAB );
            		sb.append( qLo[ i ] );
            		sb.append( Rama.TAB );
            		sb.append( qUp[ i ] );
            	}
				sb.append( Rama.END_LINE );
            }
            
            this.writeFile(saveFile, sb.toString());
        } else {
            //System.out.println("User cancelled Gene List Save");
        }
    }//onSaveGeneList()


    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }//writeFile()
	
	
	/**
	 * Transposes the ith and jth elements of a 2D double[ i ][ j ] matrix
	 * @param m
	 * @return
	 */
	private float[][] transpose( float[][] m ) {
		float[][] toReturn = new float[ m[ 0 ].length ][ m.length ];
		
		for( int i = 0; i < m.length; i ++ ) {
			for( int j = 0; j < m[ 0 ].length; j ++ ) {
				toReturn[ j ][ i ] = m[ i ][ j ];
			}
		}
		
		return toReturn;
	}//end transpose()
	
	
	/**
	 * Casts a float[][] to a double[][]
	 * @param floatMatrix
	 * @return
	 */
	static double[][] castFloatToDoubleArray( float[][] floatMatrix ) {
		double[][] toReturn = new double[ floatMatrix.length ][ floatMatrix[ 0 ].length ];
		for( int i = 0; i < floatMatrix.length; i ++ ) {
			for( int j = 0; j < floatMatrix[ i ].length; j ++ ) {
				toReturn[ i ][ j ] = ( double ) floatMatrix[ i ][ j ];
			}
		}
		return toReturn;
	}//castFloatToDoubleArray()
	
	
	/**
	 * 
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( ( JFrame ) this.mav.getFrame(), 
				message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
}//end class

/*
 * mcmc.hiv <- fit.model(hiv[1:10, c(1:4)], hiv[1:10, c(5:8)], B = 5000, min.iter = 4000, batch = 1, shift = 30, mcmc.obj = NULL, dye.swap = TRUE, nb.col1 = 2, all.out = TRUE) 
 */
/*
private void ramaAffyData( IData data ) {
	//we need to gather up the data and format it for RAMA
	String[] hybNames = new String[ data.getFeaturesCount() ];
	for( int h = 0; h < hybNames.length; h ++ ) {
		hybNames[ h ] = data.getFullSampleName( h );
	}
	
	//have user label loaded data
	RamaInitDialog initDialog = new RamaInitDialog( this.mav.getFrame(), 
			hybNames, IData.DATA_TYPE_AFFY_ABS );
	if( initDialog.showModal() == JOptionPane.OK_OPTION ) {
		//get the advanced parameters
		int B = initDialog.getNumIter();
		int minIter = initDialog.getBurnIn();
		boolean allOut = initDialog.getAllOut();
		String sConnPort = initDialog.getSelectedConnString();
		int iPort = this.parseIPort( sConnPort );
		String sConn = this.parseSPort( sConnPort );

		RamaHybSet rhs = initDialog.getRamaHybSet();
		
		String sData =  this.nonSwapDataString( data, rhs.getVRamaHyb() );
		int iGene = data.getExperiment().getNumberOfGenes();
		int nbCol1 = 0;
		int iHybKount = rhs.getVRamaHyb().size();
		int iColorKount = iHybKount * 2;
		int iTwo = iHybKount + 1;
		
		//get a connection
		RconnectionManager rcMan = new RconnectionManager( this.mav.getFrame(), sConn, iPort );
		Rconnection rc = rcMan.getConnection();
			
		//don't continue if we can't get a connection
		if( rc != null ) {
			try {
				//should clear R
				rc.voidEval( "rm( hiv )" );
				
				//load rama
				rc.voidEval( "library(rama)" );
				
				//load data as vector
				rc.voidEval( sData );
				
				//reform vector data into matrix
				rc.voidEval( "dim(" + Rama.R_VECTOR_NAME + ") <- c(" + iGene + "," 
						+ iColorKount + ")" );
				
				if( rhs.isFlip() ) {
					//call fit.model()
					rc.voidEval( this.createMcMc( allOut, iGene, iHybKount, 
							iTwo, iColorKount, B, minIter, nbCol1, true ) );
				} else {
					//call fit.model()
					rc.voidEval( this.createMcMc( allOut, iGene, iHybKount, 
							iTwo, iColorKount, B, minIter, 0, false ) );
				}
				
				//get normalized data vectors
				double[] gamma1 = rc.eval( "mcmc.hiv$gamma1" ).asDoubleArray();
				double[] gamma2 = rc.eval( "mcmc.hiv$gamma2" ).asDoubleArray();
				
				
				//need to know the gene annotation data
				String[] geneNames = new String[ iGene ];
				for( int g = 0; g < iGene; g ++ ) {
					geneNames[ g ] = data.getGeneName( g );
				}
				
				//save
		        //this.onSave( gamma1, gamma2, geneNames );
		        
		        //seemed to have worked so save the connection strings
		        if( initDialog.connAdded() ) {
		        	TMEV.updateRPath( initDialog.getRPathToWrite() );
		        }
				
				this.spawnNewMav( data, gamma1, gamma2, geneNames );
			} catch( RSrvException e ) {
				e.printStackTrace();
				this.error( e.getMessage() );
			}
		}//end if( rc != null )
	}//end if( RamaInitDialog.showModal == JOptionPane.OK_OPTION )
}//
	
	
	/**
	 * Creates formatted String for loading data to R from a non dye swap experiment
	 * @param data
	 * @param vRamaHyb
	 * @return
	 
	private String nonSwapDataString( IData data, Vector vRamaHyb ) {
		StringBuffer sbTreat = new StringBuffer( Rama.R_VECTOR_NAME + " <- c(" );
		StringBuffer sbControl = new StringBuffer();
		
		//figure out which color is which
		RamaHyb firstHyb = ( RamaHyb ) vRamaHyb.elementAt( 0 );
		boolean controlCy3 = firstHyb.controlCy3();
		
		//loop through all the hybs, they will all be the same color state
		for( int i = 0; i < vRamaHyb.size(); i ++ ) {
			if( i > 0 ) { 
				sbTreat.append( "," );
				sbControl.append( "," );
			}
			
			RamaHyb hyb = ( RamaHyb ) vRamaHyb.elementAt( i );
			int iHyb = hyb.getHybIndex();
			
			//loop through the genes
			int iGene = data.getExperiment().getNumberOfGenes();
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( "," );
					sbControl.append( "," );
				}
				
				if( controlCy3 ) {
					//treatment is Cy5 for all hybs since they're all the same
					sbTreat.append( data.getCY5( iHyb, g ) );
					sbControl.append( data.getCY3( iHyb, g ) );
					//System.out.println( i + "," + g + ":" + data.getCY5( iHyb, g ) );
				} else {
					//treatment is Cy3 for all hybs since they're all the same
					sbTreat.append( data.getCY3( iHyb, g ) );
					sbControl.append( data.getCY5( iHyb, g ) );
					//System.out.println( i + "," + g + ":" + data.getCY3( iHyb, g ) );
				}
			}//g
		}//i
		
		sbTreat.append( "," );
		sbTreat.append( sbControl );
		sbTreat.append( ")" );
		
		return sbTreat.toString();
	}
	/**
	 * Pretty convoluted to allow the user to load dye swap exp
	 * @param data
	 * @param vTreatCy3
	 * @param vTreatCy5
	 * @return
	 
	private String swapDataString( IData data, Vector vTreatCy3, Vector vTreatCy5 ) {
		StringBuffer sbTreat = new StringBuffer( Rama.R_VECTOR_NAME + " <- c(" );
		StringBuffer sbControl = new StringBuffer();
		
		int iTreat3 = vTreatCy3.size();
		int iTreat5 = vTreatCy5.size();
		
		int iGene = data.getExperiment().getNumberOfGenes();
		
		//first gather all treat3
		for( int i = 0; i < iTreat3; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RamaHyb hyb = ( RamaHyb ) vTreatCy3.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				sbTreat.append( data.getCY3( hybIndex, g ) );
				//System.out.println( "TreatCy3("+i+","+g+"):" + data.getCY3( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		//next do all treat5
		for( int i = 0; i < iTreat5; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RamaHyb hyb = ( RamaHyb ) vTreatCy5.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				sbTreat.append( data.getCY5( hybIndex, g ) );
				//System.out.println( "TreatCy5("+i+","+g+"):" + data.getCY5( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		//next control5
		for( int i = 0; i < iTreat3; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RamaHyb hyb = ( RamaHyb ) vTreatCy3.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				sbTreat.append( data.getCY5( hybIndex, g ) );
				//System.out.println( "ControlCy3("+i+","+g+"):" + data.getCY5( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		//finally control3
		for( int i = 0; i < iTreat5; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RamaHyb hyb = ( RamaHyb ) vTreatCy5.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				sbTreat.append( data.getCY3( hybIndex, g ) );
				//System.out.println( "ControlCy5("+i+","+g+"):" + data.getCY3( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( ")" );
		//System.out.println(sbTreat.toString());
		return sbTreat.toString();
	}//createDataString()
*/