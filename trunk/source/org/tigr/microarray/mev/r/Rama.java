/*
 * Created on Jul 22, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
	public static String R_VECTOR_NAME = "ramaData";
	
	private MultipleArrayViewer mav;
	private MultipleArrayMenubar menuBar;
	private IData data;
	private RamaInitDialog initDialog;
	
	private int B;
	private int minIter;
	private int iGene;
	
	private RconnectionManager rcMan;
	private Rconnection rc;
	
	
	/**
	 * 
	 * @param mavP
	 * @param menuBarP
	 */
	public Rama( MultipleArrayViewer mavP, MultipleArrayMenubar menuBarP ) {
		this.mav = mavP;
		this.menuBar = menuBarP;
			
		//gather up necessary data
		this.data = this.mav.getData();
		
		//might as well verify that there is even enough data loaded to continue
		if( this.data.getFeaturesCount() < 2 ) {
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
			this.ramify( this.data, true );
		} else {
			this.ramify( this.data, false );
		}//end else - data looks like it may work
	}//constructor
			
	
	/**
	 * 
	 * @param data
	 */
	private void ramify( IData data, boolean isAffy ) {
		//we need to gather up the data and format it for RAMA
		String[] hybNames = this.gatherHybNames( data );
		
		//deal with two-color and affy data differently
		if( isAffy ) {
			this.initDialog = new RamaInitDialog( this.mav.getFrame(), hybNames, IData.DATA_TYPE_AFFY_ABS );
		} else {
			this.initDialog = new RamaInitDialog( this.mav.getFrame(), hybNames, IData.DATA_TYPE_TWO_INTENSITY );
		}
		
		//make sure that the user clicked OK
		if( this.initDialog.showModal() == JOptionPane.OK_OPTION ) {
			//get the advanced parameters
			this.B = this.initDialog.getNumIter();
			this.minIter = this.initDialog.getBurnIn();
			boolean allOut = this.initDialog.getAllOut();
			String sConnPort = this.initDialog.getSelectedConnString();
			String sConn = this.parseSPort( sConnPort );
			int iPort = this.parseIPort( sConnPort );
			
			//Object to format MeV-IData structure into R data String
			RDataFormatter rDataFormatter = new RDataFormatter( data );
			
			//declare all the necessary variables for the mcmc
			String sData;
			int nbCol1;
			int iHybKount;
			int iColorKount;
			int iTwo;
			
			RHybSet rhs = this.initDialog.getRamaHybSet();
			
			//deal with affy and 2 color differently
			if( isAffy ) {	//this is an affy array
				sData =  rDataFormatter.rNonSwapString( Rama.R_VECTOR_NAME, rhs.getVRHyb() );
				this.iGene = data.getExperiment().getNumberOfGenes();
				nbCol1 = 0;
				iHybKount = rhs.getVRHyb().size();
				iColorKount = iHybKount * 2;
				iTwo = iHybKount + 1;
			} else {	//this is a 2 color array
				if( rhs.isFlip() ) {	//dealing with a dye swap experiment
					//Split into color state
					Vector vTreatCy3 = this.getVRamaHybTreatCy3( rhs.getVRHyb() );
					Vector vTreatCy5 = this.getVRamaHybTreatCy5( rhs.getVRHyb() );
					
					//sData =  this.swapDataString( data, vTreatCy3, vTreatCy5 );
					sData = rDataFormatter.rSwapString( Rama.R_VECTOR_NAME, vTreatCy3, vTreatCy5 );
					this.iGene = data.getExperiment().getNumberOfGenes();
					nbCol1 = vTreatCy3.size();
					iHybKount = vTreatCy3.size() + vTreatCy5.size();
					iColorKount = iHybKount * 2;
					iTwo = iHybKount + 1;
				} else {	//not dye swap
					//sData =  this.nonSwapDataString( data, rhs.getVRamaHyb() );
					sData = rDataFormatter.rNonSwapString( Rama.R_VECTOR_NAME, rhs.getVRHyb() );
					this.iGene = data.getExperiment().getNumberOfGenes();
					nbCol1 = 0;
					iHybKount = rhs.getVRHyb().size();
					iColorKount = iHybKount * 2;
					iTwo = iHybKount + 1;
				}//end rhs.isFlip() else
			}//end if( isAffy )
			
			//display an inderterminate progress bar so user knows it's working
			String message = "As a reference, 4 arrays (640 genes) takes about half an hour";
			RProgress progress = new RProgress( this.mav.getFrame(), message );
			
			//try to get a connection
			this.rcMan = new RconnectionManager( this.mav.getFrame(), sConn, iPort );
			this.rc = this.rcMan.getConnection();
			
			//don't continue if we can't get a connection
			if( rc != null ) {
				//prepare all the R command strings
				String sClear = "rm( " + Rama.R_VECTOR_NAME + " )";
				String sLibrary = "library(rama)";
				String sReform = "dim(" + Rama.R_VECTOR_NAME + ") <- c(" + iGene + "," + iColorKount + ")";
				String sMcmc;
				if( rhs.isFlip() ) {
					sMcmc = this.createMcMc( allOut, iGene, iHybKount, iTwo, iColorKount, B, minIter, nbCol1, true );
				} else {
					sMcmc = this.createMcMc( allOut, iGene, iHybKount, iTwo, iColorKount, B, minIter, nbCol1, false );
				}
				String sAvgGamma1 = "gamma1<-mat.mean(mcmc." + Rama.R_VECTOR_NAME + "$gamma1)[,1]";
				String sAvgGamma2 = "gamma2<-mat.mean(mcmc." + Rama.R_VECTOR_NAME + "$gamma2)[,1]";
				String sQLo = "mcmc." + Rama.R_VECTOR_NAME + "$q.low";
				String sQUp = "mcmc." + Rama.R_VECTOR_NAME + "$q.up";
				String sShift = "mcmc." + Rama.R_VECTOR_NAME + "$shift";
				
				//run the R process in its own thread through a SwingWorker object
		        final RSwingWorker rThread = new RSwingWorker( rc, sClear, 
		        		sLibrary, sData, sReform, sMcmc, allOut, sAvgGamma1, 
						sAvgGamma2, sQLo, sQUp, sShift, progress, this );
		        rThread.start();
			} else {	//end if( rc != null )
				//couldn't get a connection, kill everything
				progress.kill();
				this.error( "MeV could not establish a connection with Rserve" );
				System.out.println( "MeV could not establish a connection with Rserve" );
			}
		}//end if( RamaInitDialog.showModal == JOptionPane.OK_OPTION )
	}//ramaTwoIntensityData()
	
	
	/**
	 * 
	 * @param worker
	 * @param result
	 */
	public void fireThreadFinished( RSwingWorker worker, RamaResult result ) {
		//check to see if it went ok
		if( worker.isOk() ) {
			//need to know the gene annotation data
			String[] geneNames = new String[ this.iGene ];
			for( int g = 0; g < iGene; g ++ ) {
				geneNames[ g ] = data.getGeneName( g );
			}//g
			
			//see if there were any non null genenames
			for( int i = 0; i < geneNames.length; i ++ ) {
				if( !geneNames[ i ].equalsIgnoreCase( "" ) ) {
					result.setGenes( geneNames );
				}
			}//i
			result.setB( this.B );
			result.setMinIter( this.minIter );
			
			//save
			result.saveRamaResult( this.mav.getFrame() );
	        
	        //seemed to have worked so save the connection strings
	        if( initDialog.connAdded() ) {
	        	TMEV.updateRPath( this.initDialog.getRPathToWrite() );
	        }
			
	        //close old mav, open new one with results
			MultipleArrayViewer newMav = this.spawnNewMav( data, result.getGamma1(), 
					result.getGamma2(), result.getGenes(), result.getShift() );
			//RamaSummaryViewer sumViewer = new RamaSummaryViewer( result.getShift(), 
					//this.B, this.minIter, rc, newMav.getFrame() );
			RamaSummaryViewer sumViewer = new RamaSummaryViewer( result );
			LeafInfo li = new LeafInfo( "Rama Summary", sumViewer );
			DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Rama" );
			node.add( new DefaultMutableTreeNode( li ) );
			newMav.addAnalysisResult( node );
		}//worker.isOK()
	}//fireThreadFinished()
	
	
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
		sb.append( "mcmc." + Rama.R_VECTOR_NAME + " <- fit.model( " + Rama.R_VECTOR_NAME + "[ 1:" );
		sb.append( iGene );
		sb.append( " , c( 1:" );
		sb.append( iHybKount );
		sb.append( " )], " + Rama.R_VECTOR_NAME + "[ 1:" );
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
		//sb.append( ", batch = 1, shift = 30, mcmc.obj = NULL, dye.swap = " );
		//System.out.println("Dev Shift = 30");
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
			sb.append( ", all.out = TRUE" );
		}
		sb.append( ")" );
		//System.out.println(sb.toString());
		return sb.toString();
	}//createMcMc()
	
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private String[] gatherHybNames( IData data ) {
		String[] hybNames = new String[ data.getFeaturesCount() ];
		for( int h = 0; h < hybNames.length; h ++ ) {
			hybNames[ h ] = data.getFullSampleName( h );
		}
		return hybNames;
	}//gatherHybNames()
	
	
	/**
	 * 
	 * @return
	 */
	private JProgressBar createProgress(  ) {
		//create a progressbar
		JProgressBar bar = new JProgressBar();
		bar.setString( "" );
		bar.setIndeterminate( true );
		bar.repaint();
		
		JPanel barPanel = new JPanel();
		barPanel.add( bar );
		barPanel.setSize( 200, 100 );
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame( "Talking to R" );
		frame.setSize( 300, 200 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setLocation( ( screenSize.width - frame.getSize().width)/2, 
		( screenSize.height - frame.getSize().height)/2 );
		frame.setContentPane( barPanel );
		frame.show();
		
		return bar;
	}//createProgress()
	
	
	/**
	 * 
	 * @param gamma1
	 * @param gamma2
	 * @param genes
	 */
	private MultipleArrayViewer spawnNewMav( IData data, double[] gamma1, double[] gamma2, 
			String[] genes, double shift ) {
		//get the field names before they're cleared from creating a new mav
		//String[] currentFieldNames = TMEV.getFieldNames();
		String[] currentFieldNames = data.getFieldNames();
		
		//create the new mav
		Manager.createNewMultipleArrayViewer( 20, 20 );
		
		//reset the fieldnames
		//EH field names are set in SlideData now.
		//TMEV.setFieldNames( currentFieldNames );
		
		//un log2 transform the ramafied intensities
		double[] norm1 = this.unLogify( gamma1, 2 );
		double[] norm2 = this.unLogify( gamma2, 2 );
		//double[] norm1 = gamma1;
		//double[] norm2 = gamma2;
		
		//array to hold SlideData objects for mav
		ISlideData[] features = new ISlideData[ 1 ];
		
		//SlideData will be iNumGenes (rows) by 1 column
		SlideData slideData = new SlideData( gamma1.length,1 );
		//EH
		slideData.setFieldNames(currentFieldNames);
		
		//loop through the genes
		for( int i = 0; i < norm1.length; i ++ ) {
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
		newMav.fireDataLoaded( features, null, IData.DATA_TYPE_TWO_INTENSITY );
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
			RHyb hyb = ( RHyb ) ramaHybs.elementAt( h );
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
			RHyb hyb = ( RHyb ) ramaHybs.elementAt( h );
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
                //System.out.println( "path" + path );
                if( path.toLowerCase().endsWith("txt") ) {
                    //great, already ok
                    saveFile = new File(path);
                } else {
                    //add it
                    String subPath;
                    int period = path.lastIndexOf(".txt");
                    //System.out.println( "period:" + period );
                    if( period != -1 ) {
                        //System.out.println("period found");
                        subPath = path.substring(0, period);
                    } else {
                        //System.out.println("period not found");
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    //System.out.println( "newPath:" + newPath );
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
/*
try {
	//should clear R
	rc.voidEval( "rm( " + Rama.R_VECTOR_NAME + " )" );
	
	//load rama
	rc.voidEval( "library(rama)" );
	
	//load data as vector
	rc.voidEval( sData );
	
	//reform vector data into matrix
	rc.voidEval( sReform );
	
	//call fit.model()
	rc.voidEval( sMcmc );
	
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
	*/
/*
} catch( RSrvException e ) {
	e.printStackTrace();
	this.error( e.getMessage() );
}*/



/*
//check to see if it went ok
if( rThread.isOk() ) {
	//get the credible intervals (if allOut == true)
	double[] qLo;
	double[] qUp;
	if( allOut ) {
		qLo = rThread.getQLo();
		qUp = rThread.getQUp();
	} else {
		qLo = new double[ 0 ];
		qUp = new double[ 0 ];
	}
	
	//get gammas
	double[] gamma1 = rThread.getGamma1();
	double[] gamma2 = rThread.getGamma2();
	
	//get shift
	double shift = rThread.getShift();

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
} else {
	//throw error
	System.out.println( "Rama Failed" );
}
*/