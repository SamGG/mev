/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Hashtable;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

/**
 * @author braisted
 * 
 * Class to handle sorting data by chromosomal location
 * Returns:
 * 
 * 1.) a condensed matrix of sorted and averaged values
 * where rows are in 'start location' order and averages are taken for
 * in-slide replicates.
 * 
 * 2.) A 2D array of replicate indices, rows are sorted by locus location
 * for each locus there will be one or more indices to IData that represnt
 * replicate spots.  This is for mapping locus to relevant spots.
 * 
 * 3.) 'Strata' if loci overlap, a strata is determined to display an offset
 * for each of the overlapping loci.  This is used by the viewer under
 * the 'scaled' viewer mode.
 * 
 * 4.) Direction array indicates if 'start' < 'end' location, if reversed transcription
 * is on the 'antisense' strand, relevant to prokaryotic organisms.
 * 
 * 5.) Sorted locus names (id's sorted by location)
 * 
 * 6.) Sorted start locations
 * 
 * 7.) Sorted end locations
 * 
 */
public class LEM extends AbstractAlgorithm {

	
	private int [] sortedStart;
	private int [] sortedEnd;
		
	/**
	 * Uses parameters and raw data in AlgorithmData to construct
	 * output described in the LEM class description above.
	 */
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
		
		//experiment indices for the loci passed in
		int [] origIndices = data.getIntArray("original-indices");

		//idata indices for the current float matrix
		int [] idataIndices = data.getIntArray("idata-indices");
		
		FloatMatrix fm = data.getMatrix("expression-matrix");
		String [] locusArray = data.getStringArray("locus-array");
		int [] start = data.getIntArray("start-array");
		int [] end = data.getIntArray("end-array");
		
		//unique locus array and vector of vector indices where each index
		//in a vector corresponds to the same locus
		Vector lociV = new Vector();				
		
		Vector v;
		int index;
		int missingDataCount = 0;
		//also construct a hash from locus to an origial IData index
		//Hashtable locus2IDataIndex = new Hashtable();
		
		AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0, "Building and Validating Locus Vector\n");
		this.fireValueChanged(event);

		//holds full experiment IData indices for replicates array and 
		//for value extraction
		Hashtable table = new Hashtable(locusArray.length);
		
		//holds locus index (relative to current run) to map locus to start and end arrays
		Hashtable locusToStartArrayTable = new Hashtable(locusArray.length);
		
		Vector indicesV;
		
		for(int i = 0; i < locusArray.length; i++) {

			//filter null locus ids and flagged missing coordinates
			if(locusArray[i].equals("") || start[i] == -1 || end[i] == -1) {
				missingDataCount++;
				continue;
			}
		
			//handle refs to full experiment
			v = (Vector)table.get(locusArray[i]);			
			if(v == null) {
				v = new Vector();
				table.put(locusArray[i], v);
				lociV.add(locusArray[i]);
			}
						
						
			//map to IData
			v.add(new Integer(idataIndices[origIndices[i]]));				
			//try to just store locus index
			//v.add(new Integer(i));
				
			//handle refs with this anlysis relative to locus array
			indicesV = (Vector)locusToStartArrayTable.get(locusArray[i]);			

			if(indicesV == null) {
				indicesV = new Vector();
				locusToStartArrayTable.put(locusArray[i], indicesV);
			}
			
			//just store the locus index (not mapped to experiment)
			indicesV.add(new Integer(i));			

		}

		//get min coord. and direction info.
		float [] minCoord = new float[lociV.size()];
		int [] direction = new int[lociV.size()];
		
		event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0, "Determining Loci Read Polarity\n");
		this.fireValueChanged(event);
		
		for(int i = 0; i < minCoord.length; i++) {
		//	index = ((Integer)(((Vector)(table.get(lociV.get(i)))).get(0))).intValue();
			
			index = ((Integer)(((Vector)(locusToStartArrayTable.get(lociV.get(i)))).get(0))).intValue();
			
			
			if(start[index] < end[index]) {
				minCoord[i] = start[index];
				direction[i] = 1;
			} else {
				minCoord[i] = end[index];
				direction[i] = -1;
			}
		}

		event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0, "Sorting Loci on Location\n");
		this.fireValueChanged(event);

		//sort on min coord
		QSort qsort;
		//constructs and sorts
		qsort = new QSort(minCoord);
		int [] sortedIndices = qsort.getOrigIndx();
		float [] sortedCoords = qsort.getSorted();
		
		//need to sort the direction of loci
		int [] sortedDirection = new int[direction.length];

		//order the direction array based on new sorted order
		for(int i = 0; i < direction.length; i++) {
			sortedDirection[i] = direction[sortedIndices[i]];
		}
				
		//construct an ordered list of replicate sets and condensed FloatMatrix
		int [][] replicates = new int[minCoord.length][];
		
		//vector of IData indices
		Vector repIDataVector;
		
		//vector of current FloatMatrix row indices
		Vector repVector;
		
		int [] array;
		int numCol = fm.getColumnDimension();
		FloatMatrix condensedMatrix = new FloatMatrix(minCoord.length, numCol);
		int [] validN;
		float [] sumExp;
		
		sortedStart = new int[replicates.length]; 			
		sortedEnd = new int[replicates.length];
		
		int [] sortedIDataIndices = new int[replicates.length];
		String [] sortedLociNames = new String[sortedIndices.length];
				
		event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0, "Logging Spot to Locus Mapping and... \n ...Locus Mean Expression Calculation\n");
		this.fireValueChanged(event);
		
		//System.out.println("num replicates = "+replicates.length);
		
		for(int i = 0; i < replicates.length; i++) {

			//grab the sorted locus name using sorted indices
			sortedLociNames[i] = (String)(lociV.get(sortedIndices[i]));

			//current replicate vector
			repIDataVector = (Vector)(table.get(sortedLociNames[i]));			

			//prepare to convert to array
			array = new int[repIDataVector.size()];
			
			//populate replicate array
			for(int m = 0; m < array.length; m++)
				array[m] = ((Integer)repIDataVector.get(m)).intValue();

			//append current array
			replicates[i] = array;
		
			//replicates array has IData indices
			sortedIDataIndices[i] = replicates[i][0];
			
			//current FloatMatrix row replicates
			repVector = (Vector)(locusToStartArrayTable.get(sortedLociNames[i]));
			
			array = new int[repVector.size()];
			
			//populate indices
			for(int m = 0; m < array.length; m++)
				array[m] = origIndices[((Integer)repVector.get(m)).intValue()];
			
			//average expression of replicates expression
			validN = new int[numCol];
			sumExp = new float[numCol];
			
			//build new FloatMatrix entry and grab valid number count
			for(int j = 0; j < array.length; j++) { //over replicates
				for(int k = 0; k < numCol; k++) {
					if(!Float.isNaN(fm.A[array[j]][k])) {
						validN[k]++;
						sumExp[k] += fm.A[array[j]][k];
					}					
				}
			}
			
			//take mean
			for(int j = 0; j < numCol; j++) {
				if(validN[j] > 0)
					sumExp[j] /= validN[j];
				else
					sumExp[j] = Float.NaN;
			}
			
			//drop into new FloatMatrix in sorted order
			condensedMatrix.A[i] = sumExp;
			
			index = ((Integer)(((Vector)(locusToStartArrayTable.get(sortedLociNames[i]))).get(0))).intValue();
			sortedStart[i] = Math.min(start[index], end[index]);
			sortedEnd[i] = Math.max(start[index], end[index]);
		}
		
		//build strata
		//for each sorted locus have a sorted start and end and determine
		//overlap and strata with one pass		
		int [] strata = getStrataArray();
		
		
		//accumulate results
		
		//sorted locus names
		data.addStringArray("sorted-loci-names", sortedLociNames);

		//this holds IData replicates in Locus coord, order
		//required for selecting spots for a cluster or for finding
		//individual expression values for each loci (replicate measures)
		data.addIntMatrix("replication-indices-matrix", replicates);

		//Matrix and indices can be used to build a new Experiment for the viewer
		data.addMatrix("condensed-matrix", condensedMatrix);
		data.addIntArray("sorted-idata-indices", sortedIDataIndices);
		
		//sorted start and end points (min and max coord regardless of direction)
		data.addIntArray("sorted-start", sortedStart);
		data.addIntArray("sorted-end", sortedEnd);

		//direction indicator, 1 == forward, -1 == back
		data.addIntArray("direction-array", sortedDirection);
		
		//offset for overlaps
		data.addIntArray("strata-array", strata);
		data.addParam("missing-data-count", String.valueOf(missingDataCount));
		
		return data;
	}




	//strata strategy
	//look for all overlaps with a smaller start value
	//sort overlaps by strata
	
	//traverse strata from low to high
	//if strata 0 is not taken, take it and move on
	//else move up the strata and take the lowest
	//or take maxStrata + 1
	
	//move on to next strata;
	
	/**
	 * Returns true if two loci overlap in any possible way
	 * |--------|              |------------|
	 *        |---------|          |------|
	 * One encapsulates the other or one starts within the other
	 */
	private boolean haveOverLap(int currX, int otherX) {
		if((sortedStart[currX] >= sortedStart[otherX] && sortedStart[currX] <= sortedEnd[otherX])
				|| (sortedEnd[currX] <= sortedEnd[otherX] && sortedEnd[currX] >= sortedEnd[otherX])
				||(sortedStart[otherX] >= sortedStart[currX] && sortedStart[otherX] <= sortedEnd[currX]))
				return true;
		return false;
	}
	
	/**
	 * Builds a vector by looking back to all loci for overlap
	 * @param currIndex
	 * @return
	 */
	private Vector getOverLapIndicesFromPrevious(int currIndex) {
		if(currIndex == 0)
			return null;
		
		Vector v = new Vector();
		for(int i = currIndex-1; i >= 0 ; i--) {
			if(haveOverLap(currIndex, i)) {
				v.add(new Integer(i));
			}
		}		
		return v;
	}
	
	/**
	 * Returns the strata for the given locus index based on locus overlap
	 * @param currIndex current locus index
	 * @param strata strata array 
	 * @return strata
	 */
	private int getStrata(int currIndex, int [] strata) {
		Vector v = getOverLapIndicesFromPrevious(currIndex);
		
		//no overlaps
		if(v == null || v.size() == 0)
			return 0;
		
		int index;
		
		//simple base case		
		if(v.size() == 1) {
			index = ((Integer)(v.get(0))).intValue();
			if(strata[index] >0)
				return 0;
			else
				return strata[index]+1;
		}
		
		//get strata in overlaps
		Vector setStrata = new Vector();	
		for(int i = 0; i < v.size(); i++) {
			index = ((Integer)(v.get(i))).intValue();
			setStrata.add(new Integer(strata[index]));
		}

		//find the lowest unclaimed strata
		
		int strataValue = 0;
		while(setStrata.contains(new Integer(strataValue)))
			strataValue++;
		
		return strataValue;		
	}
	
	/**
	 * Wrapper method to kick off the recursive search for strata
	 * @return returns an array indicating strata for each sorted loci
	 */
	private int [] getStrataArray() {
		int [] strata = new int[sortedStart.length];

		for(int i = 0; i< strata.length; i++) {
			strata [i] = getStrata(i, strata);
		}			
		return strata;
	}
	

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.algorithm.Algorithm#abort()
	 */
	public void abort() {
		
	}

}
