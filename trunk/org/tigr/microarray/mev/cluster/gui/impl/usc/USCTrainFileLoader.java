/*
 * Created on Oct 28, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * General loader.  
 * 
 * Handles the specific cases:
 * 1	Read all the data that was already loaded into MeV
 * 2	Read only the test hybs that were loaded into MeV
 * 3	Read a training file
 * 
 * In every case, the data is formatted into USCHybSets
 * 
 * @author vu
 */
public class USCTrainFileLoader {
	//member variables
	private USCHybSet trainHybSet;
	private USCHybSet testHybSet;
	private double delta;
	private double rho;
	
	
	/**
	 * Creates a USCHybSet from an IData implementation
	 * @param data
	 * @param hybLabels	[ String ] names of hybs that are "Unknown (Test)"
	 */
	public USCTrainFileLoader( IData data, String[] hybLabels ) {
		//gene indices
		int[] sortedIndices = data.getSortedIndices( 0 );
		
		//get the hybIndices of the labeled (training) hybs
		Vector vInclude = new Vector();
		Vector vTest = new Vector();
		
		//don't include the hybs labeled "Unknown (Test)"
		for( int i = 0; i < hybLabels.length; i ++ ) {
			if( ! hybLabels[ i ].equals( USCAssignLabel.TEST_LABEL ) ) {
				vInclude.add( new Integer( i ) );
			} else {
				vTest.add( new Integer( i ) );
			}
		}
		
		int hybKount = vInclude.size();
		int testKount = vTest.size();
		
		//create a USCHyb[ numHybs ]
		USCHyb[] hybArray = new USCHyb[ hybKount ];
		USCHyb[] testArray = new USCHyb[ testKount ];
		
		//ratios [ numHybs ][ numGenes ]
		double[][] ratios = USCGUI.castFloatToDoubleArray( this.transpose( data.getExperiment().getValues() ) );
		
		for( int i = 0; i < ratios.length; i ++ ) {
			for( int j = 0; j < ratios[ i ].length; j ++ ) {
				if( ratios[ i ][ j ] == Float.NaN ) {
					System.out.println( "Nan" );
				} else if( ratios[ i ][ j ] == Float.NEGATIVE_INFINITY || ratios[ i ][ j ] == 
				Float.POSITIVE_INFINITY ) {
					System.out.println( "Infinity" );
				}
			}
		}
		
		//loop through the hybs, creating USCHyb objects and store in hybArray
		for( int h = 0; h < hybKount; h ++ ) {
			Integer I = ( Integer ) vInclude.elementAt( h );
			int iIndex = I.intValue();
			
			String sHybName = data.getFullSampleName( iIndex );
			
			USCHyb hyb = new USCHyb( h, hybLabels[ iIndex ], sHybName, ratios[ iIndex ] );
			hybArray[ h ] = hyb;
		}//end h (hybs)
		
		//loop through the test
		for( int h = 0; h < testKount; h ++ ) {
			Integer I = ( Integer ) vTest.elementAt( h );
			int iIndex = I.intValue();
			
			String sHybName = data.getFullSampleName( iIndex );
			
			USCHyb hyb = new USCHyb( h, USCAssignLabel.TEST_LABEL, sHybName, ratios[ iIndex ] );
			testArray[ h ] = hyb;
		}
		
		//create the USCHybSet
		this.trainHybSet = new USCHybSet( hybArray, this.createGeneList( data ) );
		this.testHybSet = new USCHybSet( testArray, this.createGeneList( data ) );
	}//end USCTrainFileLoader();
	
	
	/**
	 * Creates a USCHybSet from an IData implementation.  Used when getting test
	 * data to test against Saved Train Data
	 * @param data
	 */
	public USCTrainFileLoader( IData data ) {
		//gene indices
		int[] sortedIndices = data.getSortedIndices( 0 );
		
		//all hybs will be tested
		int testKount = data.getFeaturesCount();
		USCHyb[] testArray = new USCHyb[ testKount ];
	
		//now, get all the ratios [ numGenes ][ numHybs ]
		float[][] tempRatios = data.getExperiment().getValues();
		//pull out only those used for training and transpose [ numHybs ][ numGenes ]
		//double[][] ratios = USCGUI.castFloatToDoubleArray( this.transpose( this.condenseRatios( tempRatios, geneIndex ) ) );
		double[][] ratios = USCGUI.castFloatToDoubleArray( this.transpose( tempRatios ) );
		
		//loop through the hybs
		for( int h = 0; h < ratios.length; h ++ ) {
			testArray[ h ] = new USCHyb( h, USCAssignLabel.TEST_LABEL,
			data.getFullSampleName( h ), ratios[ h ] );
		}//end h
		
		this.testHybSet = new USCHybSet( testArray, this.createGeneList( data ) );
	}//end USCTrainFileLoader();
	
	
	/**
	 * Parse the Training File into USCHybSet
	 * <p>
	 * 'GeneID'	Hyb1Name	Hyb2Name	etc<br>
	 * 'blank'	Hyb1Label	Hyb2Label	etc<br>
	 * GeneID1	UID1		ratio1		ratio2		etc<br>
	 * GeneID2	UID2		ratio1		ratio2		etc<br>
	 * etc		etc			etc			etc<br>
	 * <p>
	 * Important: When the labels are unknown, 'blank' should be used in lieu of labels
	 */
	public USCTrainFileLoader( File f ) throws IOException {
		int hybKount = 0;
		int geneKount = 0;
		String[] geneNames = null;
		String[] geneIndex = null;
		USCHyb[] hybs = null;
		
		//read the file
		Reader r  = new Reader();
		r.readFile( f );
		Vector v = r.getVNullLine( USCGUI.NULL_REPLACER );

		//loop through the lines of the File
		for( int i = 0; i < v.size(); i ++ ) {
			String line = ( String ) v.elementAt( i );
			StringTokenizer st = new StringTokenizer( line, USCGUI.TAB );
			int tokenKount = st.countTokens();
			
			if( i == 0 ) {
				//initialize stuff
				//there is 1 column of gene names, rest are hybs
				hybKount = tokenKount - 2;
				hybs = new USCHyb[ hybKount ];
				//there are 2 rows of headers, rest are genes
				geneKount = v.size() - 2;
				geneNames = new String[ geneKount ];
				geneIndex = new String[ geneKount ];
				
				//this is the 1st header line
				for( int j = 0; j < tokenKount; j ++ ) {
					//these tokens are the hyb names
					String hybName = st.nextToken();
					
					if( j == 0 ) {
						//delta
						this.delta = this.parseDR( hybName );
					} else if( j == 1 ) {
						//nothing here
					} else {
						//Instantiate the USCHyb objects
						USCHyb hyb = new USCHyb( ( j - 1 ), hybName, geneKount );
						hybs[ ( j - 2 ) ] = hyb;
					}
				}//end j
			} else if( i == 1 ) {
				//this is the 2nd header line
				for( int j = 0; j < tokenKount; j ++ ) {
					//these tokens are the labels
					String s = st.nextToken();
					
					if( j == 0 ) {
						//rho
						this.rho = this.parseDR( s );
					}  else if( j == 1 ) {
						//nothing here
					} else {
						//set the labels for these hybs
						hybs[ ( j - 2 ) ].setHybLabel( s );
					}
				}//end j
			} else {
				//these are rows of data
				for( int j = 0; j < tokenKount; j ++ ) {
					//these tokens are ratios
					String s = st.nextToken();
					/*
					if( s.equals("M55998_s_at")) {
					    System.out.println( "M55998_s_at = " + ( i - 2 ) );
					}
					*/
					
					if( j == 0 ) {
						//(2,0) gene names
						geneNames[ ( i - 2 ) ] = s;
					}  else if( j == 1 ) {
						//this is a uid
						geneIndex[ ( i - 2 ) ] = s;
					} else {
						Float FRatio = new Float( s );
						if( FRatio.isNaN() ) {
							//if there is no ratio, enter a 0
							hybs[ ( j - 2 ) ].setRatio( ( i - 2 ), 0.0f );
						} else {
							//set the ratio for this gene for this hyb
							hybs[ ( j - 2 ) ].setRatio( ( i - 2 ), FRatio.doubleValue() );
						}
					}
				}//end j
			}
		}//end i
		
		USCGene[] genes = new USCGene[ geneNames.length ];
		
		for( int i = 0; i < geneNames.length; i ++ ) {
			genes[ i ] = new USCGene( geneNames[ i ], null );
		}
		
		int[] geneIndices = this.intifyStringArray( geneIndex );
		this.trainHybSet = new USCHybSet( hybs,	genes );
	}//end USCTrainFileLoader()
	
	
	/**
	 * 
	 * @param data
	 * @param geneIndices
	 * @return
	 */
	private USCGene[] createGeneList( IData data ) {
		int numGenes = data.getFeaturesSize();
		
		USCGene[] genes = new USCGene[ numGenes ];
		
		//which genes weren't specified, so do them all
		for( int i = 0; i < numGenes; i ++ ) {
			String geneName = data.getGeneName( i );
			String[] extraFields = data.getSlideDataElement( 0, i ).getExtraFields();
			USCGene gene = new USCGene( geneName, extraFields );
			genes[ i ] = gene;
		}
		
		return genes;
	}//createGeneList()
	
	
	/**
	 * Removes ratios that were not used during training and thus are not present in 
	 * the Training Result File and will not be used for classification
	 * @param ratios
	 * @param geneIndex
	 * @return
	 */
	private float[][] condenseRatios( float[][] ratios, int[] geneIndex ) {
		float[][] toReturn = new float[ geneIndex.length ][ ratios.length ];
		
		for( int i = 0; i < geneIndex.length; i ++ ) {
			toReturn[ i ] = ratios[ geneIndex[ i ] ];
		}
		
		return toReturn;
	}//condenseRatios()
	
	
	/**
	 * Converst the String[] to an int[]
	 * @param sInts
	 * @return
	 */
	private int[] intifyStringArray( String[] sInts ) {
		int[] toReturn = new int[ sInts.length ];
		
		for( int i = 0; i < sInts.length; i ++ ) {
			toReturn[ i ] = new Integer( sInts[ i ] ).intValue();
		}
		
		return toReturn;
	}//intifyStringArray()
	
	
	/**
	 * 
	 * @param sDR
	 * @return
	 */
	private double parseDR( String sDR ) {
		int iEqual = sDR.indexOf( "=" );
		Float F = new Float( sDR.substring( iEqual + 1 ) );
		return F.doubleValue();
	}
	
	
	/**
	 * Constructor for testing purposes
	 * @param m
	 */
	public USCTrainFileLoader( double[][] m ) {
		//this.transpose( m );
	}//end USCTrainFileLoader();
	
	
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
	
	
	public static void main( String[] args ) {
		double[][] m = new double[ 3 ][ 2 ];
		
		m[ 0 ][ 0 ] = 0;
		m[ 1 ][ 0 ] = 1;
		m[ 2 ][ 0 ] = 2;
		m[ 0 ][ 1 ] = 3;
		m[ 1 ][ 1 ] = 4;
		m[ 2 ][ 1 ] = 5;

		for( int i = 0; i < m.length; i ++ ) {
			for( int j = 0; j < m[ 0 ].length; j ++ ) {
				System.out.println( i + "," + j + " = " + m[ i ][ j ] );
			}
		}
		
		USCTrainFileLoader loader = new USCTrainFileLoader( m );
	}
	
	
	public USCHybSet getTrainHybSet() {
		return this.trainHybSet;
	}
	public USCHybSet getTestHybSet() {
		return this.testHybSet;
	}
	public double getDelta() {
		return this.delta;
	}
	public double getRho() {
		return this.rho;
	}
}//end class 