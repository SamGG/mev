/*
 * Created on Aug 30, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.r.RDataFormatter;
import org.tigr.microarray.mev.r.RHyb;
import org.tigr.microarray.mev.r.RHybSet;
import org.tigr.microarray.mev.r.RProgress;
import org.tigr.microarray.mev.r.RamaTextFileFilter;
import org.tigr.microarray.mev.r.Rconnection;
import org.tigr.microarray.mev.r.RconnectionManager;

/**
 * @author iVu
 */
public class BridgeGUI implements IClusterGUI {
	public static String TAB = "\t";
	public static String END_LINE = "\r\n";
	public static String R_VECTOR_NAME = "bData";
	
	private double threshold;
	
	private RProgress progress;
	
	//Labels for the Y Axis of Expression Graphs
	private String yNum = "IntB";
	private String yDenom = "IntA";
	
	
	
	public DefaultMutableTreeNode execute(IFramework framework)
			throws AlgorithmException {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode( "BRIDGE" );
		IData data = framework.getData();
		Experiment exp = data.getExperiment();
		int dataType = data.getDataType();
		
		if( dataType == IData.DATA_TYPE_RATIO_ONLY ) {
			this.error( "bridge does not work on Ratio data.\nIt only works with Intensity data." );
			return null;
		} else if( dataType == IData.DATA_TYPE_AFFY_MEAN ) {
			this.error( "bridge does not work on Affy Mean data.\nIt only works with Affy Absolute data." );
			return null;
		} else if( dataType == IData.DATA_TYPE_AFFY_MEDIAN ) {
			this.error( "bridge does not work on Affy Median data.\nIt only works with Affy Absolute data." );
			return null;
		} else if( dataType == IData.DATA_TYPE_AFFY_REF ) {
			this.error( "bridge does not work on Affy Reference data.\nIt only works with Affy Absolute data." );
			return null;
		} else {
			//data is either Affy or 2 color (both colors present as IA & IB rather than just a ratio)
			
			//first check to see if there is enough data to run (4 affy or 2 2Color)
			int minFeatures = 4;
			if( dataType == IData.DATA_TYPE_TWO_INTENSITY ) {
				minFeatures = 2;
			}
			if( data.getFeaturesCount() < minFeatures ) {
				//not enough
				this.error( "bridge requires a minimum of 2 replicates per treatment type" );
				return null;
			} else {
				BridgeResult br = this.bridgify( framework, data, data.getDataType() );
				if( br != null ) {
					this.createExpressionImages( root, data, br, this.yNum, this.yDenom );
					
					//kill the progress bar if it is lingering (if user cancels)
					if( this.progress != null ) {
						this.progress.kill();
					}
					
					return root;
				} else {
					//deal with null result

					//kill the progress bar if it is lingering (if user cancels)
					if( this.progress != null ) {
						this.progress.kill();
					}
					
					return null;
				}
			}
		}//else
	}//constructor
	
	
	/**
	 * 
	 * @param framework
	 * @param data
	 * @param dataType
	 * @return
	 */
	private BridgeResult bridgify( IFramework framework, IData data, int dataType ) {
		BridgeResult toReturn = null;
		
		//we need to gather up the data and format it for RAMA
		String[] hybNames = new String[ data.getFeaturesCount() ];
		for( int h = 0; h < hybNames.length; h ++ ) {
			hybNames[ h ] = data.getFullSampleName( h );
		}
		
		//have user label loaded data
		BridgeInitDialog initDialog = new BridgeInitDialog( framework.getFrame(),
				hybNames, dataType );
		if( initDialog.showModal() == JOptionPane.OK_OPTION ) {
			//get the advanced parameters
			int B = initDialog.getNumIter();
			int minIter = initDialog.getBurnIn();
			this.threshold = initDialog.getThreshold();
			String sConnPort = initDialog.getSelectedConnString();
			int iPort = this.parseIPort( sConnPort );
			String sConn = this.parseSPort( sConnPort );
			this.setYNum( initDialog.getYNum() );
			this.setYDenom( initDialog.getYDen() );

			//Object to format MeV-IData structure into R data String
			RDataFormatter rDataFormatter = new RDataFormatter( data );
			
			RHybSet bhs = initDialog.getBridgeHybSet();
			
			//data that characterizes loaded data
			String sData;		//
			int iGene;			//# genes
			int iHybKount;		//# hybs
			int iColorKount;	//# data points for both colors/all hybs (#hybs * 2)
			int iTwo;			//start index of 2nd color
			int nbCol1;			//# of hybs in 1st color state
			
			if( bhs.isFlip() ) {	//dealing with a dye swap experiment
				//Split into color state
				Vector vTreatCy3 = this.getVRamaHybTreatCy3( bhs.getVRamaHyb() );
				Vector vTreatCy5 = this.getVRamaHybTreatCy5( bhs.getVRamaHyb() );
				
				sData = rDataFormatter.rSwapString( BridgeGUI.R_VECTOR_NAME, vTreatCy3, vTreatCy5 );
				iGene = data.getExperiment().getNumberOfGenes();
				nbCol1 = vTreatCy3.size();
				iHybKount = vTreatCy3.size() + vTreatCy5.size();
				iColorKount = iHybKount * 2;
				iTwo = iHybKount + 1;
			} else {	//not dye swap
				sData = rDataFormatter.rNonSwapString( BridgeGUI.R_VECTOR_NAME, bhs.getVRamaHyb() );
				iGene = data.getExperiment().getNumberOfGenes();
				nbCol1 = 0;
				iHybKount = bhs.getVRamaHyb().size();
				iColorKount = iHybKount * 2;
				iTwo = iHybKount + 1;
			}//end rhs.isFlip() else
			
			//display an inderterminate progress bar so user knows it's working
			double dTimePerCalc = 0.000015d;
			double dIntensityKount = ( double ) iGene * ( double ) iHybKount;
			double dTotalTime = dIntensityKount * dTimePerCalc * B;
			double dMinutes = dTotalTime / 60d;
			DecimalFormat df = new DecimalFormat( "###.#" );
			String estTime = df.format( dMinutes );
			String message = "It may take as long as " + estTime + " minutes with your data set";
			this.progress = new RProgress( ( JFrame ) framework.getFrame(), message );
			
			//get a connection
			RconnectionManager rcMan = new RconnectionManager( 
					framework.getFrame(), sConn, iPort );
			Rconnection rc = rcMan.getConnection();
			
			//don't continue if we can't get a connection
			if( rc != null ) {
				//create R command strings
				String sClear = "rm(" + BridgeGUI.R_VECTOR_NAME + ")";
				String sLibrary = "library(bridge)";
				String sReform = "dim(" + BridgeGUI.R_VECTOR_NAME + ") <- c(" + iGene + "," + iColorKount + ")";
				String sMcMc = this.createMcMc( iGene, iHybKount, iHybKount, iTwo, iColorKount, B, minIter, dataType );
				String sAvg1 = "gamma1<-mat.mean(bridge." + BridgeGUI.R_VECTOR_NAME + "$gamma1)[,1]";
				String sAvg2 = "gamma2<-mat.mean(bridge." + BridgeGUI.R_VECTOR_NAME + "$gamma2)[,1]";
				String sPostP = "bridge." + BridgeGUI.R_VECTOR_NAME + "$post.p";
				
				final BridgeWorker bThread = new BridgeWorker( rc, sClear, 
						sLibrary, sData, sReform, sMcMc, sAvg1, sAvg2, 
						sPostP, this.threshold, progress );
				bThread.start();
				
				//wait to allow the worker to finish before proceeding
				while( !bThread.isDone() ) {
					try {
						Thread.sleep( 10000 );
					} catch( InterruptedException e ) {
						e.printStackTrace();
					}
				}//while()
				
				//get the result
				toReturn = bThread.getResult();
				
				//need to know the gene annotation data
				String[] geneNames = new String[ iGene ];
				for( int g = 0; g < iGene; g ++ ) {
					geneNames[ g ] = data.getGeneName( g );
				}//g
				
				//store these arrays in BridgeResult object
				//toReturn = new BridgeResult( gamma1, gamma2, pprob, this.threshold );
				toReturn.setGeneNames( geneNames );
		        
		        //seemed to have worked so save the connection strings
		        if( initDialog.connAdded() ) {
		        	TMEV.updateRPath( initDialog.getRPathToWrite() );
		        }
			} else { //end if( rc != null )
				//deal with null connection, kill everything
			}
		}//end OK_OPTION
		
		return toReturn;
	}//twoIntensity()
	
	
	
	/**
	 * Creates a Vector of RHybs where: treated-Cy3 | control-Cy5
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
	 * Creates a Vector of RHybs where: control-Cy3 | treated-Cy5
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
	 * Arrange such that 
	 * @param data
	 * @param vBridgeHyb
	 * @return
	 */
	private String BridgeNonSwapString( IData data, Vector vBridgeHyb ) {
		StringBuffer sbTreat = new StringBuffer( BridgeGUI.R_VECTOR_NAME + " <- c(" );
		StringBuffer sbControl = new StringBuffer();
		
		//figure out which color is treated, which is control
		BridgeHyb firstHyb = ( BridgeHyb ) vBridgeHyb.elementAt( 0 );
		boolean controlCy3 = firstHyb.controlCy3();
		
		//loop through all the hybs
		for( int i = 0; i < vBridgeHyb.size(); i ++ ) {
			if( i > 0 ) { 
				sbTreat.append( "," );
				sbControl.append( "," );
			}
			
			BridgeHyb hyb = ( BridgeHyb ) vBridgeHyb.elementAt( i );
			int iHyb = hyb.getHybIndex();
			
			//loop through the genes
			int iGene = data.getExperiment().getNumberOfGenes();
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( "," );
					sbControl.append( "," );
				}
				
				if( controlCy3 ) {
					//treated is Cy5 for all hybs since they're all the same
					sbTreat.append( data.getCY5( iHyb, g ) );
					sbControl.append( data.getCY3( iHyb, g ) );
					//System.out.println( i + "," + g + ":" + data.getCY5( iHyb, g ) );
				} else {
					//treated is Cy3 for all hybs since they're all the same
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
	}//BridgeNonSwapString()
	
	
	/**
	 * 
	 * @param iGene
	 * @param iHybKount
	 * @param iOne
	 * @param iTwo
	 * @param iColorKount
	 * @param B
	 * @param minIter
	 * @param dataType
	 * @return
	 */
	private String createMcMc( int iGene, int iHybKount, int iOne, int iTwo, 
			int iColorKount, int B, int minIter, int dataType ) {
		StringBuffer sb = new StringBuffer();
		sb.append( "bridge." );
		sb.append( BridgeGUI.R_VECTOR_NAME );
		sb.append( "<- bridge.2samples( " );
		sb.append( BridgeGUI.R_VECTOR_NAME );
		sb.append( "[ 1:" );
		sb.append( iGene );
		sb.append( " , c( 1:" );
		sb.append( iHybKount );
		sb.append( " )], " + BridgeGUI.R_VECTOR_NAME + "[ 1:" );
		sb.append( iGene );
		sb.append( ", c( " );
		sb.append( iTwo );
		sb.append( ":" );
		sb.append( iColorKount );
		sb.append( " )], B = " );
		sb.append( B );
		sb.append( ", min.iter = " );
		sb.append( minIter );
		sb.append( ", batch = 1, mcmc.obj = NULL, affy = " );
		if( dataType == IData.DATA_TYPE_TWO_INTENSITY ) {
			sb.append( "FALSE" );
		} else {
			sb.append( "TRUE" );
		}
		sb.append( ", verbose = TRUE )" );
		//System.out.println(sb.toString());
		return sb.toString();
		//return "mcmc.hiv <- bridge.2samples( hiv[ 1:10 , c( 1:2 )], hiv[ 1:10, c( 2:2 )], B = 21000, min.iter = 1000, batch = 1, mcmc.obj = NULL, affy = FALSE, verbose = TRUE )";
	}//createMcMc()
	
	
	/**
	 * Creates Heat Maps of Significant and Non Significant genes
	 * @param root
	 * @param data
	 * @param br
	 * @return
	 */
	private void createExpressionImages( DefaultMutableTreeNode root,
			IData data, BridgeResult br, String yNum, String yDenom ) {
		
		DefaultMutableTreeNode expViewerNode = new DefaultMutableTreeNode( 
				"Expression Images" );
		DefaultMutableTreeNode cenViewerNode = new DefaultMutableTreeNode(
				"Expression Graphs" );
        DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode("Table Views");
		
		Experiment exp = data.getExperiment();
		
		//create new clusters for sig/nonsig gene sets
		int[][] newClusters = this.createClusters( br );
		
		//create heat map viewers for sig/nonsig gene sets
		ExperimentViewer expViewer = new ExperimentViewer( exp, newClusters );
		expViewerNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(0))));
		expViewerNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(1))));
		
		//create centroid viewers for sig/nonsig gene sets
		BridgeCentroidViewer centViewer = new BridgeCentroidViewer( exp, newClusters, yNum, yDenom );
		centViewer.setMeans( this.calculateMeans( exp, br ) );
		cenViewerNode.add(new DefaultMutableTreeNode(new LeafInfo(
        		"Significant Genes ", centViewer, new CentroidUserObject( 0, 
        				CentroidUserObject.VALUES_MODE))));
		cenViewerNode.add(new DefaultMutableTreeNode(new LeafInfo(
        		"Non-significant Genes ", centViewer, new CentroidUserObject( 1, 
        				CentroidUserObject.VALUES_MODE))));
		//
		String[] headers = br.getHeaders();
		String[][] auxData = br.getAuxData();
        IViewer tabViewer = new ClusterTableViewer( exp, newClusters, data, headers, auxData );
        tableNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(0))));
		tableNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(1))));
		
		root.add( expViewerNode );
		root.add( cenViewerNode );
		root.add( tableNode );
	}//createViews()
	
	
	/**
	 * clusters = int[ dataset index ][ row index ]
	 * @param br
	 * @return
	 */
	private int[][] createClusters( BridgeResult br ) {
		int[][] toReturn = new int[ 2 ][];
		toReturn[ 0 ] = br.getSigIndices();
		toReturn[ 1 ] = br.getNonIndices();
		
		return toReturn;
	}//createClusters()
	
	
	/**
	 * 
	 * @param exp
	 * @return
	 */
	private float[][] calculateMeans( Experiment exp, BridgeResult br ) {
		int[] sigIndices = br.getSigIndices();
		int[] nonIndices = br.getNonIndices();
		float[][] ratios = exp.getValues();
		float[][] toReturn = new float[ 2 ][ ratios[ 0 ].length ];
		
		//loop through the hybs
		for( int h = 0; h < ratios[ 0 ].length; h ++ ) {
			//avg sig genes for each hyb
			float sigTotal = 0;
			float nonTotal = 0;
			
			//loop through the sig genes
			for( int i = 0; i < sigIndices.length; i ++ ) {
				sigTotal = sigTotal + ratios[ sigIndices[ i ] ][ h ];
			}//i
			
			float sigAvg = sigTotal / ( float ) sigIndices.length;
			toReturn[ 0 ][ h ] = sigAvg;
			
			//loop through the nonsig genes
			for( int i = 0; i < nonIndices.length; i ++ ) {
				nonTotal = nonTotal + ratios[ nonIndices[ i ] ][ h ];
			}
			
			float nonAvg = nonTotal / ( float ) nonIndices.length;
			toReturn [ 1 ][ h ] = nonAvg;
		}//h
		
		return toReturn;
	}//calculateMeans()


    /**
     * 
     * @param gamma1
     * @param gamma2
     * @param genes
     */
    private void onSave( double[] gamma1, double[] gamma2, String[] genes) {
        String currentPath = TMEV.getDataPath();
        RamaTextFileFilter textFilter = new RamaTextFileFilter();
        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.addChoosableFileFilter(textFilter);
        if( chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION ) {
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
            sb.append( BridgeGUI.TAB );
            sb.append( "RatioA" );
            sb.append( BridgeGUI.TAB );
            sb.append( "RatioB" );
            sb.append( BridgeGUI.END_LINE );
            
            for( int i = 0; i < gamma1.length; i++ ) {
            	sb.append( genes[ i ] );
                sb.append( BridgeGUI.TAB );
            	sb.append( gamma1[ i ] );
                sb.append( BridgeGUI.TAB );
            	sb.append( gamma2[ i ] );
				sb.append( BridgeGUI.END_LINE );
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
	 * Displays an error dialog
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), 
				message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
	
	public void setYNum( String s ) {
		this.yNum = s;
	}
	public void setYDenom( String s ) {
		this.yDenom = s;
	}
}//end class
/*
//flip doesn't matter for bridge
String sData =  this.BridgeNonSwapString( data, bhs.getVHyb() );
int iGene = data.getExperiment().getNumberOfGenes();
int nbCol1 = 0;
int iHybKount = bhs.getVHyb().size();
int iColorKount = iHybKount * 2;
int iOne = iHybKount;
int iTwo = iHybKount + 1;
*/
/*
//should clear R
rc.voidEval( "rm(" + BridgeGUI.R_VECTOR_NAME + " )" );

//load rama
rc.voidEval( "library(bridge)" );

//load data as vector
rc.voidEval( sData );

//reform vector data into matrix
rc.voidEval( "dim(" + BridgeGUI.R_VECTOR_NAME + ") <- c(" + iGene + "," 
		+ iColorKount + ")" );


//call fit.model()
rc.voidEval( this.createMcMc( iGene, iHybKount, iOne, iTwo, 
		iColorKount, B, minIter, dataType ) );

rc.voidEval( "gamma1<-mat.mean(mcmc.hiv$gamma1)[,1]" );
rc.voidEval( "gamma2<-mat.mean(mcmc.hiv$gamma2)[,1]" );

//get normalized data vectors
double[] gamma1 = rc.eval( "gamma1" ).asDoubleArray();
double[] gamma2 = rc.eval( "gamma2" ).asDoubleArray();
double[] pprob = rc.eval( "mcmc.hiv$post.p" ).asDoubleArray();
*/