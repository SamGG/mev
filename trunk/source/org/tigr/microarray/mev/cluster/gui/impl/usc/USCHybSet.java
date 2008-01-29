/*
 * Created on Oct 28, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * A set of USCHyb objects.  Basically used to separate a training set from a
 * test set.
 * 
 * @author vu
 */
public class USCHybSet {
	private int hybKount;
	private int numClasses;
	private int foldKount;
	private int hybPerFold;
	private int hybRemain;
	
	private int[] iPermuted;
	//private int[] geneIndices;			//uid is common for all hybs
	
	private float s0;
	
	private float[] sis;					//s value for each gene
	private float[] geneCentroids;			//mean ratio for each gene across all hybs
	private float[][] classCentroids;		//mean ratio within each class for each gene
	private float[] classMks;				//mk for each class
	private String[] uniqueClasses;			//class names
	
	private USCHyb[] hybs;
	private USCHyb[][] testArray;			//[ foldIndex ][ hybPerFold ]
	private USCHyb[][] trainArray;			//same
	
	private USCGene[] genes;
	
	
	/**
	 * Constructor for Testing Data from MeV
	 * @param hybArray
	 * @param hybSetTypeP
	 * @param hybKountP
	 * @param geneKountP
	 * @param geneNamesP
	 */
	public USCHybSet( USCHyb[] hybArray, USCGene[] geneListP ) {
		this.hybs = hybArray;
		this.hybKount = hybArray.length;
		this.genes = geneListP;
		
		this.uniqueClasses = this.findUniqueClasses( this );
		this.numClasses = this.uniqueClasses.length;
		this.assignClassIndices();
		
		this.iPermuted = this.randomize( this.hybKount );
	}//end constructor
	/*
	public USCHybSet( USCHyb[] hybArray ) {
		this.hybs = hybArray;
		this.hybKount = hybArray.length;
		
		this.uniqueClasses = this.findUniqueClasses( this );
		this.numClasses = this.uniqueClasses.length;
		this.assignClassIndices();
		
		this.iPermuted = this.randomize( this.hybKount );
	}//end constructor
	public USCHybSet( USCHyb[] hybArray, int[] uidP ) {
		this.uid = uidP;
		this.hybs = hybArray;
		this.hybKount = hybArray.length;
		
		this.uniqueClasses = this.findUniqueClasses( this );
		this.numClasses = this.uniqueClasses.length;
		this.assignClassIndices();
		
		this.iPermuted = this.randomize( this.hybKount );
	}//end constructor
	public USCHybSet( USCHyb[] hybArray, int[] uidP, USCGeneList geneListP ) {
		this.geneIndices = uidP;
		this.hybs = hybArray;
		this.hybKount = hybArray.length;
		this.geneList = geneListP;
		
		this.uniqueClasses = this.findUniqueClasses( this );
		this.numClasses = this.uniqueClasses.length;
		this.assignClassIndices();
		
		this.iPermuted = this.randomize( this.hybKount );
	}//end constructor
	*/
	
	
	/**
	 * 
	 */
	private void assignClassIndices() {
		//loop through the hybs in this hybset
		for( int h = 0; h < this.hybs.length; h ++ ) {
			String sLabel = this.hybs[ h ].getHybLabel();
			
			//loop through the uniqueClasse
			for( int c = 0; c < this.uniqueClasses.length; c ++ ) {
				if( sLabel.equals( this.uniqueClasses[ c ] ) ) {
					this.hybs[ h ].setUniqueLabelIndex( c );
					break;
				}
			}
		}
	}//end assignClassIndices()
	
	
	/**
	 * Permute calls randomize() and createTestArrays()
	 * @param foldKountP
	 */
	public void permute( int foldKountP ) {
		this.foldKount = foldKountP;
		this.hybPerFold = this.hybKount / this.foldKount;
		this.hybRemain = this.hybKount % this.foldKount;
		this.createTestArrays();
	}//end permute()
	
	
	/**
	 * Creates 2 2D USCHyb[ foldKount ][ hybPerFold ]
	 */
	private void createTestArrays() {
		this.testArray = new USCHyb[ this.foldKount ][];
		this.trainArray = new USCHyb[ this.foldKount ][];
		
		//when foldKount=1, the entire set should be used to find relevant/uncorr genes
		if( this.foldKount == 1 ) {
			this.testArray[ 0 ] = new USCHyb[ 0 ];
			this.trainArray[ 0 ] = this.hybs;
		} else {
			//
			for( int i = 0; i < this.foldKount; i ++ ) {
				//System.out.println( "Fold:" + i );
				
				int iStart = i * this.hybPerFold;
				
				if( i == ( this.foldKount - 1 ) ) {
					this.testArray[ i ] = new USCHyb[ ( this.hybPerFold + this.hybRemain ) ];
					this.trainArray[ i ] = new USCHyb[ this.hybKount - ( this.hybPerFold + this.hybRemain ) ];
				} else {
					this.testArray[ i ] = new USCHyb[ this.hybPerFold ];
					this.trainArray[ i ] = new USCHyb[ this.hybKount - this.hybPerFold ];
				}
				
				//
				for( int j = 0; j < this.testArray[ i ].length; j ++ ) {
					this.testArray[ i ][ j ] = this.getHyb( this.iPermuted[ iStart + j ] );
					//System.out.println( this.testArray[ i ][ j ].getHybName() + " added to testArray" );
				}
				
				int index = 0;
				//
				for( int j = 0; j < this.hybKount; j ++ ) {
					boolean add = true;
					
					for( int k = 0; k < this.testArray[ i ].length; k ++ ) {
						if( this.getHyb( j ).getIndexInFullSet() == this.testArray[ i ][ k ].getIndexInFullSet() ) {
							add = false;
							break; 
						}
					}
					
					if( add ) {
						this.trainArray[ i ][ index ] = this.getHyb( j );
						//System.out.println( this.trainArray[ i ][ index ].getHybName() + " added to trainArray" );
						index ++;
					}
				}//end j(testHybs)
			}//end i(folds)
		}//end else
	}//end createTestArrays()
	
	
	/**
	 * Randomize kount integers (0-based) and store in hybIndices array
	 * @param kount	The # of integers to randomize
	 */
	public int[] randomize( int hybKount ) {
		Vector vIndices = new Vector();
		int[] toReturn = new int[ hybKount ];
		
		Random r = new Random();
		
		for( int i = 0; i < toReturn.length; i ++ ) {
			int iTry = -1;
			
			while( ! isNew( iTry, vIndices ) ) {
				iTry = r.nextInt( hybKount );
			}
			
			vIndices.add( new Integer( iTry ) );
			toReturn[ i ] = iTry;
			//System.out.println( "iTry:" + ( iTry + 1 ) );
		}//end i
		
		return toReturn;
	}//end constructor
	/**
	 * Tests to see if this integer has already been recorded
	 * @param iTry	int to test
	 * @param v	Vector of recorded Integer objects
	 * @return	true if this is a new int, false if this int has already been recorded
	 */
	private boolean isNew( int iTry, Vector v ) {
		if( iTry == -1 ) {
			return false;
		} else {
			boolean toReturn = true;
			
			for( int i = 0; i < v.size(); i ++ ) {
				Integer I = ( Integer ) v.elementAt( i );
				if( iTry == I.intValue() ) {
					toReturn = false;
					break; 
				}
			}
			
			return toReturn;
		}
	}//end isNew()
	
	
	/**
	 * Look through the classes and store the unique class labels in uniqueClassArray
	 * @param hybSet
	 * @return
	 */
	private String[] findUniqueClasses( USCHybSet hybSet ) {
		String[] toReturn;
		
		/*
		Hashtable ht = new Hashtable();
		
		for( int i = 0; i < hybSet.getHybs().length; i ++ ) {
			ht.put( hybSet.getHyb( i ).getHybLabel(), hybSet.getHyb( i ).getHybLabel() );
		}
		
		toReturn = new String[ ht.size() ];
		
		Enumeration en = ht.elements();
		int index = 0;
		while( en.hasMoreElements() ) {
			String s = ( String ) en.nextElement();
			toReturn[ index ] = s;
			index ++; 
		}//end while
		
		Arrays.sort( toReturn );
		*/
		
		Vector vUniqueLabel = new Vector();
		
		for( int i = 0; i < hybSet.getHybs().length; i ++ ) {
		    String sLabel = hybSet.getHyb( i ).getHybLabel();
		    
		    boolean labelFound = false;
		    
		    //loop through the
		    for( int j = 0; j < vUniqueLabel.size(); j ++ ) {
		        String jLabel = ( String ) vUniqueLabel.elementAt( j );
		        if( sLabel.equals( jLabel ) ) {
		            labelFound = true;
		            break;
		        }
		    }
		    
		    if( ! labelFound ) {
		        vUniqueLabel.add( sLabel );
		    }
		}//i
		
		toReturn = new String[ vUniqueLabel.size() ];
		
		for(int i = 0; i < vUniqueLabel.size(); i ++ ) {
		    toReturn[ i ] = ( String ) vUniqueLabel.elementAt( i );
		}
		
		return toReturn;
	}//end parseClass()
	
	
	/*------------------------------Getters & Setters--------------------------------*/
	/*
	public void setGeneIndex( int geneIndex, int uid ) {
		this.geneIndices[ geneIndex ] = uid;
	}
	public void setGeneIndices( int[] uids ) {
		this.geneIndices = uids;
	}
	public int getGeneIndex( int geneIndex ) {
		return this.geneIndices[ geneIndex ];
	}
	public int[] getGeneIndices() {
		return this.geneIndices;
	}
	*/
	public int getHybKount() {
		return this.hybKount;
	}
	public USCHyb[] getHybs() {
		return this.hybs;
	}
	public USCHyb getHyb( int index ) {
		return this.hybs[ index ];
	}
	public USCGene[] getGenes() {
		return this.genes;
	}
	public USCGene getGene( int iGene ) {
		return this.genes[ iGene ];
	}
	public int getNumClasses() {
		return this.numClasses;
	}
	public int getNumGenes() {
		return this.getHyb( 0 ).getNumGenes();
	}
	public void setS0( float s0P ) {
		this.s0 = s0P;
	}
	public float getS0() {
		 return this.s0;
	}
	public void setSis( float[] sisP ) {
		this.sis = sisP;
	}
	public void setSi( float siP, int geneIndex ) {
		this.sis[ geneIndex ] = siP;
	}
	public float getSi( int geneIndex ) {
		return this.sis[ geneIndex ];
	}
	public float[] getSis() {
		return this.sis;
	}
	public void setGeneCentroids( float[] geneCentroidsP ) {
		this.geneCentroids = geneCentroidsP;
	}
	public void setGeneCentroid( float geneCentroid, int geneIndex ) {
		this.geneCentroids[ geneIndex ] = geneCentroid;
	}
	public float getGeneCentroid( int geneIndex ) {
		return this.geneCentroids[ geneIndex ];
	}
	public float[] getGeneCentroids() {
		return this.geneCentroids;
	}
	public void setClassCentroids( float[] classCentroidsP, int geneIndex ) {
		this.classCentroids[ geneIndex ] = classCentroidsP;
	}
	public float[][] getClassCentroids() {
		return this.classCentroids;
	}
	public float getClassCentroid( int classIndex, int geneIndex ) {
		return this.classCentroids[ geneIndex ][ classIndex ];
	}
	public void setClassMks( float[] classMksP ) {
		this.classMks = classMksP;
	}
	public float[] getClassMks() {
		return this.classMks;
	}
	public float getClassMk( int classIndex ) {
		return this.classMks[ classIndex ];
	}
	public String[] getUniqueClasses() {
		return this.uniqueClasses;
	}
	public String getUniqueClass( int classIndex ) {
		return this.uniqueClasses[ classIndex ];
	}
	public USCHyb[] getHybsInClass( int classIndex ) {
		Vector v = new Vector();
		String classLabel = this.uniqueClasses[ classIndex ];
		
		for( int i = 0; i < this.hybKount; i ++ ) {
			USCHyb hyb = this.hybs[ i ];
			if( hyb.getHybLabel().equalsIgnoreCase( classLabel ) ) {
				v.add( hyb );
			}
		}//end i
		
		USCHyb[] toReturn = new USCHyb[ v.size() ];
		for( int i = 0; i < v.size(); i ++ ) {
			USCHyb hyb = ( USCHyb ) v.elementAt( i );
			toReturn[ i ] = hyb;
		}
		
		return toReturn;
	}
	public int getNumHybsInClass( int classIndex ) {
		Vector v = new Vector();
		String classLabel = this.uniqueClasses[ classIndex ];
		
		for( int i = 0; i < this.hybKount; i ++ ) {
			USCHyb hyb = this.hybs[ i ];
			if( hyb.getHybLabel().equalsIgnoreCase( classLabel ) ) {
				v.add( hyb );
			}
		}//end i
		
		return v.size();
	}
	/**
	 * Create and return a 2 D matrix of the ratios for the hybs in the hyb set.
	 * @return	float[ Hybs ][ Genes ]
	 */
	public double[][] getRatioMatrix() {
		double[][] toReturn = new double[ this.hybs.length ][];
		
		for( int i = 0; i < this.hybs.length; i ++ ) {
			USCHyb hyb = ( USCHyb ) this.hybs[ i ];
			toReturn[ i ] = hyb.getRatios();
		}
		
		return toReturn;
	}//end getRatioMatrix()
	public USCHyb[] getTestArray( int foldIndex ) {
		return this.testArray[ foldIndex ];
	}
	public USCHyb[] getTrainArray( int foldIndex ) {
		return this.trainArray[ foldIndex ];
	}
	
	
	public static void main( String [] args ) {
		System.out.println( "Invoked by main" );
		
		File f = new File( "C:" + File.separator + "Dev" + File.separator + "MeV" +
			File.separator + "files" + File.separator + "human" + File.separator + 
			"NewTrainFormat.txt" );
		
		try {
			USCTrainFileLoader loader = new USCTrainFileLoader( f );
			USCHybSet hs = loader.getTrainHybSet();
			hs.permute( 5 );
			//USCCrossValidation cv = new USCCrossValidation( hs, 50, 20, 0.5, 1.0, 0.1, 5 );
			//cv.crossValidate();

			/*
			System.out.println( "hybKount:" + hs.getHybKount() );
			for( int j = 0; j < hs.getHybs().length; j ++ ) {
				USCHyb hyb = ( USCHyb ) hs.getHybs()[ j ];
				System.out.println( "\r\nlabel:" + hyb.getHybLabel() + "\tname:" + hyb.getHybName() );
				float[] ratios = hyb.getRatios();
				for( int i = 0; i < ratios.length; i ++ ) {
					System.out.println( ratios[ i ] );
				}
			}
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//end main
}//end class