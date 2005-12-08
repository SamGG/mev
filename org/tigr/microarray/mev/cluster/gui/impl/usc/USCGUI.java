/*
 * Created on Jun 4, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.util.FloatMatrix;

/**
 * USCGUI is the main handler for the USC algorithm.  It creates 2 - 4 dialog boxes
 * and returns a some IViewer implementations in a DefaultMutableTreeNode.
 * 
 * 1.	USCClassDialog - allows user to choose between training (default) or 
 * classifying from a file.  If training, the user is required to provide USC
 * with the complete list of classes.  The user may also tweak some advanced
 * parameters if desired.
 * 
 * 2.	USCAssignLabel - asks user to assign the class labels to the training
 * hybs that are known.
 * 
 * 3.	USCDeltaDialog - displays a synopsis of the results from Cross Validation.
 * The user must choose 1 Delta/Rho combination to use for classification.  The
 * user is allowed here to save the training results to a file to use later.
 * 
 * 4.	USCFileDialog - just a File Browser so the user can point to a training
 * file.
 * 
 * 5.	USCConfirmDelta - allow user to try different Delta/Rho values
 * 
 * @author vu
 */
public class USCGUI implements IClusterGUI {
	//0 based row and column numbers
	public static final int START_ROW = 1;
	public static final int START_COLUMN = 1;
	public static final int NUMBER_REPLICATES = 1;
	public static final String ERROR_OPTION = "0";
	
	public static final String NULL_REPLACER = "MOTHRA";
	public static final String END_LINE = "\r\n";
	public static final String TAB = "\t";
	
	private Hashtable htNameIndex;	//Hashtable of HybName/index in IData

	private int numClasses;
	private int fold;
	private int numBins;
	private int delta;
	private int xValKount;
	private double corrLo;
	private double corrHi;
	private double corrStep;
	private boolean doLoocv;

	private String[] uniqueClassArray;
	private String[] userLabelArray;
	private String[] hybNames;
	private String[] params;	//[ numBins, corrLo, corrHi, corrStep ]
	
	private USCResult finalResult;
	private USCFoldResult[] foldResults;
	//private USCXValResult xValResults;
	private USCDeltaRhoResult[][][] xResult;
	
	
	/**
	 * Reads hyb ratio data from the framework, classifies it and returns a 
	 * DefaultMutableTreeNode containing LeafInfo objects with the results
	 * @param framework
	 * @return 
	 */
	public DefaultMutableTreeNode execute( IFramework framework ) throws AlgorithmException {
		DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode( "USC Result" );
		
		IData data = framework.getData();
		
		//figure out what type of analysis the user wants to do and get parameters
		USCClassDialog classDialog = new USCClassDialog( framework.getFrame() );
		
		if( classDialog.showModal() != JOptionPane.OK_OPTION ) {
			System.out.println( "Cancelled out of USCClassDialog" );
			return null;
		}
		
		//gather up all the parameters
		this.numClasses = classDialog.getNumClasses();
		this.numBins = classDialog.getNumBins();
		this.delta = classDialog.getDeltaMax();
		this.fold = classDialog.getFolds();
		this.corrLo = classDialog.getCorrLo();
		this.corrHi = classDialog.getCorrHi();
		this.corrStep = classDialog.getCorrStep();
		this.userLabelArray = classDialog.getClassLabels();
		this.xValKount = classDialog.getXValRuns();
		
		this.params = new String[ 4 ];
		this.params[ 0 ] = Integer.toString( this.numBins );
		this.params[ 1 ] = Double.toString( this.corrLo );
		this.params[ 2 ] = Double.toString( this.corrHi );
		this.params[ 3 ] = Double.toString( this.corrStep );
		
		//get hyb names as String[], so user can assign labels
		this.hybNames = new String[ data.getFeaturesCount() ];
		for( int i = 0; i < this.hybNames.length; i ++ ) {
			this.hybNames[ i ] = data.getFullSampleName( i );
		}//end i
		
		//act according to user's choice
		if( classDialog.getAnalysisOption() == USCClassDialog.TRAIN_THEN_CLASSIFY ) {
			return this.trainThenClassify( data, framework );
		} else {
			return this.classifyFromFile( data, framework );
		}
	}//end execute()
	
	
	/**
	 * Train the training data, test the test data, return results
	 * @param data
	 * @return 
	 */
	private DefaultMutableTreeNode trainThenClassify( IData data, IFramework framework ) {
		//show dialog so user can assign labels
		USCAssignLabel assLabel = new USCAssignLabel( this.hybNames, this.userLabelArray );
		if( assLabel.showModal() == JOptionPane.OK_OPTION ) {
			//the labels entered by the user
			String[] userEnteredLabels = assLabel.getHybLabels();
			
			//gather up loaded data in the form of USCHybSet objects
			USCTrainFileLoader loader = new USCTrainFileLoader( data, userEnteredLabels );
			USCHybSet trainSet = loader.getTrainHybSet();
			USCHybSet testSet = loader.getTestHybSet();
			
			//should keep track of what hyb comes in what position
			this.htNameIndex = this.hashNameIndex( data );
			
			//similar to userEntered, but in order as determined by USCHybSet & USCTrainFileLoader
			this.uniqueClassArray = trainSet.getUniqueClasses();
			
			//make sure the #folds doesn't exceed minimum hybs/class
			this.fold = this.validateFold( this.fold, trainSet );
			trainSet.permute( this.fold );
			
			//ready to cross validate
			USCCrossValidation xVal = new USCCrossValidation( this.numBins, 
			this.delta, this.corrLo, this.corrHi, this.corrStep, this.fold, this.xValKount );
			this.xResult = xVal.crossValidate(trainSet,framework.getFrame());
			
			//let user determine which rho/delta from cross validation to use
			USCDeltaDialog deltaDialog;
			
			if( this.fold == 1 ) {
				deltaDialog = new USCDeltaDialog( this.xResult, true );
			} else {
				deltaDialog = new USCDeltaDialog( this.xResult, false );
			}
			if( deltaDialog.showModal() == JOptionPane.OK_OPTION ) {
				USCRow row = deltaDialog.getSelectedRow();
				double fDelta = row.getFDelta();
				double fRho = row.getFRho();
				int iRho = ( int ) ( fRho * 10 );
				
				//need a progres bar and dialog
				JFrame jf = new JFrame();
				JPanel panel = new JPanel();
				JProgressBar bar = new JProgressBar( 0, trainSet.getNumGenes() );
				JProgressBar rhoBar = new JProgressBar( 0, 6 );
				panel.add( bar );
				jf.getContentPane().add( panel );
				jf.setSize( 150, 100 );
				jf.show();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				jf.setLocation((screenSize.width - 200)/2, (screenSize.height - 100)/2);
				
				//test the test set against the training set using user selected delta and rho
				USCHyb[] trainArray = trainSet.getHybs();
				USCHyb[] testArray = testSet.getHybs();
				this.finalResult = xVal.testTest( trainArray, testArray, fDelta, 
						fRho, trainSet.getNumGenes(), trainSet.getNumClasses(), 
						uniqueClassArray, bar, iRho );
				
				jf.dispose();
			
				//USCOrder[] has relevant gene info
				USCOrder[] order = this.finalResult.getOrder();
			
				//have all requisite info to save the training to a file if desired
				if( deltaDialog.saveTraining() ) {
					this.saveTraining( trainSet.getHybs(), row, order, data, 
							framework.getFrame(), trainSet.getGenes() );
				}
				
				//create a new Experiment to represent this analysis (subset of genes used)
				Experiment newExp = this.createNewExperiment( data.getExperiment(), 
						this.finalResult.getNumGenesUsed(), order );
				
				DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode( "USC" );
				
				String[] testHybNames = new String[ testArray.length ];
				for( int h = 0; h < testArray.length; h ++ ) {
					testHybNames[ h ] = testArray[ h ].getHybName();
				}
				
				//add the summary
				USCSummaryViewer summaryViewer = new USCSummaryViewer( 
						testHybNames, this.finalResult, this.uniqueClassArray, this.params,
						this.getGenes( testSet.getGenes(), finalResult ), framework );
				LeafInfo summaryLeaf = new LeafInfo( "Summary", summaryViewer );
				returnNode.add( new DefaultMutableTreeNode( summaryLeaf ) );
				
				//add a node to display all the hybs and the genes used for analysis
				returnNode.add( new DefaultMutableTreeNode( 
						this.createFullCluster( data, newExp, finalResult ) ) );
				
				//add the class results
				for( int c = 0; c < this.numClasses; c ++ ) {
					LeafInfo li = this.createClassCluster( c, newExp, finalResult, trainSet, testSet );
					returnNode.add( new DefaultMutableTreeNode( li ) );
				}
				
				return returnNode;
			} else {
				//System.out.println( "Cancelled out of Delta dialog" );
				return null;
			}
		} else {
			//System.out.println( "Cancelled out of AssignLabel dialog" );
			return null;
		}
	}//trainThenClassify()
	
	
	/**
	 * Load Cross Validation data from a file, test against it and return results.  Need 
	 * to be careful here to keep track of genes.  For starters, only the genes that 
	 * were relevant and uncorrelated were written to the training file.  As such, we 
	 * need to make sure that the USCHybs in both Training and Test sets match.  
	 * Then, when we need to display the result hybs, we need to make sure we display
	 * the right genes.
	 * @param data
	 * @param framework
	 * @return
	 */
	private DefaultMutableTreeNode classifyFromFile( IData data, IFramework framework ) {
		framework.getFrame().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
		
		//let user load the training file
		USCFileDialog trainFileDialog = new USCFileDialog( framework.getFrame() );
		int iTfd = trainFileDialog.showModal();
			
		if( iTfd != JOptionPane.OK_OPTION ) {
			return null;
		}
			
		//create USCHybSet objects to represent training file
		USCTrainFileLoader loader = null;
		try {
			loader = new USCTrainFileLoader( trainFileDialog.getSelectedFile() );
		} catch (IOException e) {
			this.error("The File doesn't appear to be a Results File");
			e.printStackTrace();
			return null;
		}
			
		//file seems to have loaded properly
		USCHybSet trainSet = loader.getTrainHybSet();
		this.htNameIndex = this.hashNameIndex( data );
		this.uniqueClassArray = trainSet.getUniqueClasses();
			
		//also get the delta & rho to use
		double fDelta = loader.getDelta();
		double fRho = loader.getRho();
		
		//display dialog to see if user would like to try different delta or rho
		USCConfirmDelta confirmDelta = new USCConfirmDelta(framework.getFrame(), fDelta, fRho);
		int iCd = confirmDelta.showModal();
		
		if(iCd != JOptionPane.OK_OPTION) {
		    return null;
		}
		
		fDelta = confirmDelta.getDelta();
		fRho = confirmDelta.getRho();
		int iRho = ( int ) ( fRho * 10 );
		
		//create a USCHybSet of the test set
		USCTrainFileLoader testLoader = new USCTrainFileLoader( data );
		USCHybSet testSet = testLoader.getTestHybSet();
			
		USCHyb[] trainArray = trainSet.getHybs();
		USCHyb[] testArray = testSet.getHybs();
		
		//need numclasses
		this.numClasses = trainSet.getNumClasses();
				
		//need a progres bar and dialog
		JFrame jf = new JFrame();
		JPanel panel = new JPanel();
		JProgressBar bar = new JProgressBar( 0, trainSet.getNumGenes() );
		panel.add( bar );
		jf.getContentPane().add( panel );
		jf.setSize( 250, 100 );
		jf.show();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		jf.setLocation((screenSize.width - 200)/2, (screenSize.height - 100)/2);
			
		//test
		USCCrossValidation xVal = new USCCrossValidation( this.numBins, this.delta, 
				this.corrLo, this.corrHi, this.corrStep, this.fold, this.xValKount );
		this.finalResult = xVal.testTest( trainArray, testArray, fDelta, 
				fRho, trainSet.getNumGenes(), trainSet.getNumClasses(), 
				trainSet.getUniqueClasses(), bar, iRho);
		
		jf.dispose();

		//USCOrder[] has relevant gene info
		USCOrder[] order = finalResult.getOrder();

		//create a new Experiment to represent this analysis (subset of genes used)
		Experiment newExp = this.createNewExperiment( data.getExperiment(), 
		finalResult.getNumGenesUsed(), order );
		
		DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode( "USC" );
				
		//add the summary
		USCSummaryViewer summaryViewer = new USCSummaryViewer( 
				this.hybNames, this.finalResult, this.uniqueClassArray, this.params,
				this.getGenes( testSet.getGenes(), finalResult ), framework );
		
		LeafInfo summaryLeaf = new LeafInfo( "Summary", summaryViewer );
		returnNode.add( new DefaultMutableTreeNode( summaryLeaf ) );
				
		//add a node to display all the hybs and the genes used for analysis
		returnNode.add( new DefaultMutableTreeNode( 
		this.createFullCluster( data, newExp, finalResult ) ) );
		
		for( int c = 0; c < this.numClasses; c ++ ) {
			LeafInfo li = this.createClassClusterForTrainFile( c, newExp, finalResult, trainSet, testSet );
			returnNode.add( new DefaultMutableTreeNode( li ) );
		}
		
		framework.getFrame().setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
		
		return returnNode;
	}//classifyFromFile()
	
	
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
	 * Creates a new Experiment as a subset of the existing Experiment containing
	 * only the genes used for this analysis ( those found to be both relevant and 
	 * uncorrelated through Shrunken Centroid analysis ).
	 * @param exp
	 * @param numGenesUsed
	 * @param order
	 * @return
	 */
	private Experiment createNewExperiment( Experiment exp, int numGenesUsed,
	USCOrder[] order ) {
		//need to first get the full double[][] of ratios, the full rowMap and full columns
		float[][] fullMatrix =  exp.getValues();
		int[] fullRowMap = exp.getRowMappingArrayCopy();
		int[] fullColumns = exp.getColumnIndicesCopy();
		
		//our new float[ genes ][ hybs ]
		float[][] newMatrix = new float[ numGenesUsed ][];
		
		//our new rowMap
		int[] newRowMap = new int[ numGenesUsed ];
		
		//loop through the USCOrder[] and look for relevant/uncorrelated genes, get the
		//rowMap index of that gene from original experiment and store for new exp
		int iKount = 0;
		for( int i = 0; i < order.length; i ++ ) {
			if( order[ i ].use() ) {
				int iGene = order[ iKount ].getIOriginal();
				newMatrix[ iKount ] = fullMatrix[ iGene ];
				newRowMap[ iKount ] = fullRowMap[ iGene ];
				iKount ++;
			}
		}//end i
		
		FloatMatrix fm = new FloatMatrix( newMatrix );
		Experiment toReturn = new Experiment( fm, fullColumns, newRowMap );
		
		return toReturn;
	}//createNewExperiment
	
	
	/**
	 * Display all the hybs, but only the genes that were used in this analysis
	 * @param data
	 * @param exp
	 * @param result
	 * @return
	 */
	private LeafInfo createFullCluster( IData data, Experiment exp, USCResult result ) {
		LeafInfo toReturn;
		
		int[][] clusters = new int[ 1 ][ data.getFeaturesCount() ];
		for( int i = 0; i < data.getFeaturesCount(); i ++ ) {
			clusters[ 0 ][ i ] = i;
		}
		
		USCExperimentClusterViewer viewer = new USCExperimentClusterViewer( exp, clusters );
		toReturn = new LeafInfo( "All Loaded Hybs - Genes Used", viewer, new Integer( 0 ) );
		
		return toReturn;
	}//createFullCluster()
	
	
	/**
	 * Creates the Leaf Info for the case where the user loaded previous training file.
	 * @param c
	 * @param exp
	 * @param result
	 * @param trainSet
	 * @param testSet
	 * @return
	 */
	private LeafInfo createClassClusterForTrainFile( int c, Experiment exp, USCResult result, 
	USCHybSet trainSet, USCHybSet testSet ) {
		LeafInfo toReturn;
		double[][] scores = result.getDiscScores();
		
		//we'll create a separate viewer for each class
		//get this class's hybs
		USCHyb[] classHybs = trainSet.getHybsInClass( c );
		
		//find the hybs that were assigned to this class
		Vector vHybIndex = new Vector();
		for( int h = 0; h < scores.length; h ++ ) {
			//get a handle to the hyb we're testing
			USCHyb hyb = testSet.getHyb( h );
			
			//if this hyb belongs to this class, store its index
			int iMin = result.getAssignedClassIndex( h );
			if( iMin == c ) {
				//System.out.println( hyb.getHybName() );
				vHybIndex.add( new Integer( this.lookupIndex( hyb.getHybName() ) ) );
			}
		}//end h
		
		int[][] clusters = new int[ 1 ][ vHybIndex.size() ];
		for( int h = 0; h < vHybIndex.size(); h ++ ) {
			Integer I = ( Integer ) vHybIndex.elementAt( h );
			clusters[ 0 ][ h ] = I.intValue();
		}
		
		USCExperimentClusterViewer viewer = new USCExperimentClusterViewer( exp, clusters );
		String sNode = new String( "Classified As: " + trainSet.getUniqueClass( c ) );
		toReturn = new LeafInfo( sNode, viewer, new Integer( 0 ) );
		
		return toReturn;
	}//end createClassCluster()
	
	
	/**
	 * 
	 * @param c
	 * @param exp
	 * @param result
	 * @param trainSet
	 * @param testSet
	 * @return
	 */
	private LeafInfo createClassCluster( int c, Experiment exp, USCResult result, 
	USCHybSet trainSet, USCHybSet testSet ) {
		LeafInfo toReturn;
		double[][] scores = result.getDiscScores();
		
		//we'll create a separate viewer for each class
		//get this class's hybs
		USCHyb[] classHybs = trainSet.getHybsInClass( c );
		
		//find the hybs that were assigned to this class
		Vector vHybIndex = new Vector();
		for( int h = 0; h < scores.length; h ++ ) {
			//get a handle to the hyb we're testing
			USCHyb hyb = testSet.getHyb( h );
			
			//if this hyb belongs to this class, store its index
			int iMin = result.getAssignedClassIndex( h );
			if( iMin == c ) {
				//System.out.println( hyb.getHybName() );
				vHybIndex.add( new Integer( this.lookupIndex( hyb.getHybName() ) ) );
			}
		}//end h
		
		int[][] clusters = new int[ 1 ][ ( classHybs.length + vHybIndex.size() ) ];
		for( int h = 0; h < classHybs.length; h ++ ) {
			clusters[ 0 ][ h ] = this.lookupIndex( classHybs[ h ].getHybName() );
		}
		for( int h = 0; h < vHybIndex.size(); h ++ ) {
			Integer I = ( Integer ) vHybIndex.elementAt( h );
			clusters[ 0 ][ ( classHybs.length + h ) ] = I.intValue();
		}
		
		USCExperimentClusterViewer viewer = new USCExperimentClusterViewer( exp, clusters );
		String sNode = new String( "Classified As: " + trainSet.getUniqueClass( c ) );
		toReturn = new LeafInfo( sNode, viewer, new Integer( 0 ) );
		
		return toReturn;
	}//end createClassCluster()
	
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private Hashtable hashNameIndex( IData data ) {
		Hashtable ht = new Hashtable();
		
		int iHyb = data.getFeaturesCount();
		for( int i = 0; i < iHyb; i ++ ) {
			ht.put( data.getFullSampleName( i ), new Integer( i ) );
		}
		
		return ht;
	}//hashNameIndex()
	
	
	/**
	 * Save the Trained Hybs
	 * @param trainArray
	 * @param row
	 * @param order
	 * @param data
	 */
	private void saveTraining( USCHyb[] trainArray, USCRow row, 
	USCOrder[] order, IData data, Frame frame, USCGene[] genes ) {
		//System.out.println( "SaveTraining()" );
		
		//load the current path
		String dataPath = "/" + TMEV.getDataPath();
		if( dataPath == null ) {
			dataPath = "";
		}
		
		//resort the genes to be in original order
		Arrays.sort( order, new USCOrderSorter() );
		
		//pop up dialog for save
		JFileChooser chooser = new JFileChooser( dataPath );
		USCTextFileFilter textFilter = new USCTextFileFilter();
		chooser.addChoosableFileFilter( textFilter );
		int returnVal = chooser.showSaveDialog( frame );
		if( returnVal == JFileChooser.APPROVE_OPTION ) {
			File saveFile;
			
			if( chooser.getFileFilter() == textFilter ) {
				//make sure to add .txt
				String path = chooser.getSelectedFile().getPath();
				if( path.toLowerCase().endsWith( "txt" ) ) {
					//great, already ok
					saveFile = new File( path );
				} else {
					//add it
					String subPath;
					int period = path.lastIndexOf( "." );
					if( period != -1 ) {
						System.out.println( "period  = -1" );
						subPath = path.substring( 0, period );
					} else {
						subPath = path;
					}
					String newPath = subPath + ".txt";
					saveFile = new File( newPath );
				}
			} else {
				saveFile = chooser.getSelectedFile();
			}
		
			StringBuffer sb = new StringBuffer();
		
			//first line
			sb.append( "Delta=" );
			sb.append( row.getDelta() );
			//append space
			sb.append( USCGUI.TAB );
			sb.append( "BLANK" );
			//loop through training hybs
			for( int h = 0; h < trainArray.length; h ++ ) {
				USCHyb hyb = trainArray[ h ];
				sb.append( USCGUI.TAB );
				sb.append( hyb.getHybName() );
			}
			sb.append( USCGUI.END_LINE );
		
			//2nd line
			sb.append( "Rho=" );
			sb.append( row.getRho() );
			//append space
			sb.append( USCGUI.TAB );
			sb.append( "BLANK" );
			//loop through training hybs
			for( int h = 0; h < trainArray.length; h ++ ) {
				USCHyb hyb = trainArray[ h ];
				sb.append( USCGUI.TAB );
				sb.append( hyb.getHybLabel() );
			}
			sb.append( USCGUI.END_LINE );
		
			//loop through USCOrder
			for( int o = 0; o < order.length; o ++ ) {
				//if( order[ o ].use() ) {
					//get the original index
					int iOrig = order[ o ].getIOriginal();
				
					//get the original gene
					sb.append( data.getGeneName( iOrig ) );
					
					//write the uid
					sb.append( USCGUI.TAB );
					sb.append( data.getUniqueId( iOrig ) );
				
					//loop through hybs
					for( int h = 0; h < trainArray.length; h ++ ) {
						USCHyb hyb = trainArray[ h ];
						sb.append( USCGUI.TAB );
						sb.append( hyb.getRatio( iOrig ) );
					}//end h
				
					sb.append( USCGUI.END_LINE );
				//}
			}//end o
		
			this.writeFile( saveFile, sb.toString() );
		} else {
			System.out.println( "User Cancelled Saving Training File" );
			return;
		}
	}//saveTraining()
	
	
	/**
	 * Uses the indices in result to create a String[] of gene names
	 * @param data
	 * @param result
	 * @return
	 */
	/*
	private String[] getGeneNames( IData data, USCResult result ) {
		//first gather the genes that were actually used
		USCOrder[] order = result.getOrder();
		int usedKount = 0;
		for( int i = 0; i < order.length; i ++ ) {
			if( order[ i ].use() ) {
				usedKount ++;
			}
		}
		
		//now create the String[]
		String[] geneNames = new String[ usedKount ];
		int iGeneNames = 0;
		for( int i = 0; i < order.length; i ++ ) {
			if( order[ i ].use() ) {
				geneNames[ iGeneNames ] = data.getGeneName( order[ i ].getIOriginal() );
				iGeneNames ++;
			}
		}
		
		return geneNames;
	}//getGeneNames()*/
	private USCGene[] getGenes( USCGene[] fullGenes, USCResult result ) {
		USCOrder[] order = result.getOrder();
		int usedKount = 0;
		for( int i = 0; i < order.length; i ++ ) {
			if( order[ i ].use() ) {
				usedKount ++;
			}
		}//i
		
		USCGene[] returnGenes = new USCGene[ usedKount ];
		int iGene = 0;
		for( int i = 0; i < order.length; i ++ ) {
			if( order[ i ].use() ) {
				returnGenes[ iGene ] = fullGenes[ order[ i ].getIOriginal() ];
				iGene ++;
			}
		}
		
		return returnGenes;
	}//getGenes();
	
	
	/**
	 * 
	 * @param hybName
	 * @return
	 */
	private int lookupIndex( String hybName ) {
		Integer I = ( Integer ) this.htNameIndex.get( hybName );
		return I.intValue();
	}//end lookupIndex()
	
	
	/**
	 * 
	 * @param iFold
	 * @param hybSet
	 * @return
	 */
	private int validateFold( int iFold, USCHybSet hybSet ) {
		int toReturn = iFold;
		
		for( int i = 0; i < this.numClasses; i ++ ) {
			int hybKount = hybSet.getNumHybsInClass( i );
			if( hybKount < toReturn ) {
				toReturn = hybKount;
			}
		}
		
		return toReturn;
	}//end
	
	
	/**
	 * Write the String s to File f
	 * @param f
	 * @param s
	 */
	private void writeFile( File f, String s ) {
		try {
			FileWriter fw = new FileWriter( f );
			fw.write( s );
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.error( e.getMessage() );
		}
	}//writeFile()
	
	
	private int findParamValue( Hashtable ht, String sKey ) {
		int toReturn = 0;
		
		JTextField field = ( JTextField ) ht.get( sKey );
		Integer I = new Integer( field.getText() );
		toReturn = I.intValue();
		
		return toReturn;
	}
	
	
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}
}//end class

/*

private DefaultMutableTreeNode trainThenClassify( IData data, IFramework framework ) {
	//show dialog so user can assign labels
	USCAssignLabel assLabel = new USCAssignLabel( this.hybNames, this.userLabelArray );
	if( assLabel.showModal() == JOptionPane.OK_OPTION ) {
		//the labels entered by the user
		String[] userEnteredLabels = assLabel.getHybLabels();
		
		//gather up loaded data in the form of USCHybSet objects
		USCTrainFileLoader loader = new USCTrainFileLoader( data, userEnteredLabels );
		USCHybSet trainSet = loader.getTrainHybSet();
		USCHybSet testSet = loader.getTestHybSet();
		
		//should keep track of what hyb comes in what position
		this.htNameIndex = this.hashNameIndex( data );
		
		//similar to userEntered, but in order as determined by USCHybSet & USCTrainFileLoader
		this.uniqueClassArray = trainSet.getUniqueClasses();
		
		//make sure the #folds doesn't exceed minimum hybs/class
		this.fold = this.validateFold( this.fold, trainSet );
		trainSet.permute( this.fold );
		
		//ready to cross validate
		USCCrossValidation xVal = new USCCrossValidation( this.numBins, 
		this.delta, this.corrLo, this.corrHi, this.corrStep, this.fold, this.xValKount );
		this.xValResults = xVal.crossValidate( trainSet, framework.getFrame() );
		//this.xResult = xVal.crossValidate(trainSet,framework.getFrame());
		
		//let user determine which rho/delta from cross validation to use
		USCDeltaDialog deltaDialog;
		
		if( this.fold == 1 ) {
			deltaDialog = new USCDeltaDialog( this.xValResults, true );
		} else {
			deltaDialog = new USCDeltaDialog( this.xValResults, false );
		}
		if( deltaDialog.showModal() == JOptionPane.OK_OPTION ) {
			USCRow row = deltaDialog.getSelectedRow();
			double fDelta = row.getFDelta();
			double fRho = row.getFRho();
			int iRho = ( int ) ( fRho * 10 );
			//System.out.println("iRho = " + iRho );
			
			//need a progres bar and dialog
			JFrame jf = new JFrame();
			JPanel panel = new JPanel();
			JProgressBar bar = new JProgressBar( 0, trainSet.getNumGenes() );
			panel.add( bar );
			jf.getContentPane().add( panel );
			jf.setSize( 150, 200 );
			jf.show();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			jf.setLocation((screenSize.width - 200)/2, (screenSize.height - 100)/2);
			
			//test the test set against the training set using user selected delta and rho
			USCHyb[] trainArray = trainSet.getHybs();
			USCHyb[] testArray = testSet.getHybs();
			this.finalResult = xVal.testTest( trainArray, testArray, fDelta, 
			fRho, trainSet.getNumGenes(), trainSet.getNumClasses(), 
			uniqueClassArray, bar, iRho );
			
			jf.dispose();
		
			//USCOrder[] has relevant gene info
			USCOrder[] order = this.finalResult.getOrder();
		
			//have all requisite info to save the training to a file if desired
			if( deltaDialog.saveTraining() ) {
				this.saveTraining( trainSet.getHybs(), row, order, data, 
				framework.getFrame(), trainSet.getGeneList() );
			}
			
			//create a new Experiment to represent this analysis (subset of genes used)
			Experiment newExp = this.createNewExperiment( data.getExperiment(), 
			this.finalResult.getNumGenesUsed(), order );
			
			DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode( "USC" );
			
			String[] testHybNames = new String[ testArray.length ];
			for( int h = 0; h < testArray.length; h ++ ) {
				testHybNames[ h ] = testArray[ h ].getHybName();
			}
			
			//add the summary
			USCSummaryViewer summaryViewer = new USCSummaryViewer( 
			testHybNames, this.finalResult, this.uniqueClassArray, this.params,
			this.getGeneNames( data, finalResult ), framework );
			LeafInfo summaryLeaf = new LeafInfo( "Summary", summaryViewer );
			returnNode.add( new DefaultMutableTreeNode( summaryLeaf ) );
			
			//add a node to display all the hybs and the genes used for analysis
			returnNode.add( new DefaultMutableTreeNode( 
			this.createFullCluster( data, newExp, finalResult ) ) );
			
			//add the class results
			for( int c = 0; c < this.numClasses; c ++ ) {
				LeafInfo li = this.createClassCluster( c, newExp, finalResult, trainSet, testSet );
				returnNode.add( new DefaultMutableTreeNode( li ) );
			}
			
			return returnNode;
		} else {
			//System.out.println( "Cancelled out of Delta dialog" );
			return null;
		}
	} else {
		//System.out.println( "Cancelled out of AssignLabel dialog" );
		return null;
	}
}//trainThenClassify()
*/