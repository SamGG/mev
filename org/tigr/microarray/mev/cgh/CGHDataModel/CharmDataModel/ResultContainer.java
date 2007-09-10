package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.PValue;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.SigTestThread;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.util.FloatMatrix;

/**
* Encapsulates results of ChARM run for a single dataset.
*
 * <p>Title: ResultContainer</p>
 * <p>Description: Encapsulates results of ChARM run for a single dataset.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class ResultContainer {

  private static double MERGE_SIGTHRESH = .03;
  private static double MERGE_SIZETHRESH = 5;
  private static int MERGE_PERMUTATIONS = 50;

  //private HashMap windowHash = null;
  private HashMap segmentHash = null;
  //private ArrayList chromosomes = null;
  private ArrayList experiments = null; //Array of Strings with Expr Names
  //private DatasetContainer sourceData = null; //Not needed in Mev

  private String resultID =  null;

  IData data;
  
  /**
   * Class constructor.
   * @param data DatasetContainer - the dataset with which these results are associated
   */
  public ResultContainer(/*DatasetContainer data*/IData data, ArrayList exprNames) {
    //this.chromosomes = data.getChromosomes();
    //this.experiments = data.getExperiments();
    //this.sourceData = data;
	  
	  this.data = data;
	  this.experiments = exprNames;
	  segmentHash = new HashMap();
  }

  /**
   * Adds a segment (a set of gene identifiers between which an "interesting" region has been identified) to this result set
   * @param newWindow Segment- segment struct with all associated data
   * @param exp String- experiment associated with this segment
   * @param chromosome int- chromosome number associated with this segment
   */
  public void addSegment(SegmentInfo newSegment, String exp, int chromosome) {
    if(segmentHash.containsKey(exp+","+chromosome)) {
      ((ArrayList)segmentHash.get(exp+","+chromosome)).add(newSegment);
    }
    else {
     ArrayList newArray = new ArrayList();
     newArray.add(newSegment);
     segmentHash.put(exp+","+chromosome,newArray);
    }
  }

  /**
   * Returns array list of all segments in this results set for a given experiment, chromosome number.
   * @param exp String
   * @param chromosome int
   * @return ArrayList
   */
  //public ArrayList getWindows(String exp, int chromosome) {
  public ArrayList getSegments(String exp, int chromosome) {
    ArrayList retArray = null;
    if(segmentHash.containsKey(exp + "," + chromosome)) {
      retArray = (ArrayList)segmentHash.get(exp + "," + chromosome);
    }
    else {
      retArray = new ArrayList();
    }
    return retArray;
  }

  /**
   * Sets all segments associated with a particular experiment and chromosome.  NOTE: any existing segment will be lost.
   * @param exp String
   * @param chromosome int
   * @param newSegments ArrayList
   */
  public void setSegments(String exp, int chromosome, ArrayList newSegments) {
    segmentHash.put(exp + "," + chromosome, newSegments);
  }

  /**
   * Returns HashMap of all segments (predictions) associated with this dataset. Keys are in experiment,chromosome-num format.
   * Values are ArrayLists of segments associated with each experiment, chromosome pair.
   * @return HashMap
   */
  public HashMap getAllSegments() {
    return segmentHash;
  }

  /**
   * Merges the given result set with the current one.
   * @param moreResults ResultContainer
   */
  public void addResults(ResultContainer moreResults) {
    HashMap newSegmentHash = moreResults.getAllSegments();
    ArrayList keyList = new ArrayList(newSegmentHash.keySet());
    for(int i=0; i<keyList.size(); i++) {
      if(((ArrayList)newSegmentHash.get(keyList.get(i))).size() > 0) segmentHash.put((String)keyList.get(i),(ArrayList)newSegmentHash.get(keyList.get(i)));
    }
  }

  /**
   *  Returns segments from the given experiment, chromosome meeting the specified pvalue cutoff
   *  with the specified test.  Returns an arraylist of the segments that meet this cutoff.
   *
   * @param exp String
   * @param chromosome int
   * @param pvalueCutoff PValue
   * @param pvalTestType int- tests defined in PValue.MEAN_AND_SIGN_TEST, PValue.MEAN_OR_SIGN_TEST, PValue.MEAN_TEST, PValue.SIGN_TEST
   * @return ArrayList
   */
  public ArrayList getSegmentsMeetingCriteria(String exp, int chromosome, PValue pvalueCutoff, int pvalTestType) {

    ArrayList allSegments = (ArrayList)this.getSegments(exp,chromosome);
    if (allSegments == null){
      return null;
    }
    else {
      ArrayList matches = new ArrayList();

      for (int i = 0; i < allSegments.size(); i++) {
    	  SegmentInfo currSeg = ( (SegmentInfo) allSegments.get(i));
        PValue pval = new PValue(currSeg.getStatistic(new String("Mean")),Math.min(currSeg.getStatistic(new String("Sign neg")),currSeg.getStatistic(new String("Sign pos"))));

        if (pvalueCutoff.compareTo(pval,pvalTestType) >= 0) {
          matches.add(currSeg);
        }
      }

      return matches;
    }
  }

  /**
   * Prints all segments currently contained in this ResultContainer to the specified file.
   * @param outFile String- output file
   * @param append boolean- append to file flag (true => append, false => overwrite)
   * @return int- status ( >=0 => succes, <0- failure
   */
  public int printToFile(String outFile, boolean append) {
    int status = 0;
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(outFile,append));
      int numOfChrs = this.data.getNumChromosomes();
      //for(int i=0; i < chromosomes.size(); i++) {
      for(int currChrom = 1; currChrom < numOfChrs; currChrom++) {
        //Chromosome currChrom = (Chromosome)chromosomes.get(i);
    	  int currChromInd = currChrom - 1;
        for (int j = 0; j < experiments.size(); j++) {
          String currExp = (String)experiments.get(j);
          //ArrayList segments = (ArrayList) this.getSegments(currExp,currChrom.getNumber());
          ArrayList segments = (ArrayList) this.getSegments(currExp, currChrom);
          if (segments != null) {
        	  ArrayList indOrig = getOrigIndArray(currExp, currChromInd);
            for (int k = 0; k < segments.size(); k++) {
            	SegmentInfo currSeg = (SegmentInfo) segments.get(k);
            	int startIndex = lookupGeneIndex(indOrig,currSeg.getStart());
                int endIndex = lookupGeneIndex(indOrig,currSeg.getEnd());
                //out.write(currExp + "\t" + currChrom.getNumber() + "\t" +
            	out.write(currExp + "\t" + currChrom + "\t" +
            	data.getCloneAt(startIndex, currChromInd) + "\t" +
            	data.getCloneAt(endIndex, currChromInd) + "\t" +
                //((Gene)currChrom.geneAtRatioIndex(currSeg.getStart(),currExp,false)).getID() + "\t" +
                //((Gene)currChrom.geneAtRatioIndex(currSeg.getEnd(),currExp,false)).getID() + "\t" +
                    currSeg.getDataMean() + "\t" +
                    currSeg.getStatistic("Sign pos") + "\t" +
                    currSeg.getStatistic("Sign neg") + "\t" +
                    currSeg.getStatistic("Mean") + "\t");
              out.newLine();
            }
          }
        }
      }
    out.flush();
    out.close();
    }
    catch(Exception e) {e.printStackTrace(); status = -1;}
    return status;
  }

  /**
   * Prints header for output data to specified file.
   * @param outFile String- output file
   * @param append boolean- append to file flag (true => append, false => overwrite)
   * @return int- status ( >=0 => succes, <0- failure
   */
  public int printHeaderToFile(String outFile,boolean append) {
	  int status = 0;
	  try {
		  BufferedWriter out = new BufferedWriter(new FileWriter(outFile,append));
		  out.write("Experiment\tChromosome\tStart ID\tEnd ID\tMean value over region\tSign test (positive) p-value\tSign test (negative) p-value\tMean test p-value");
		  out.newLine();
		  out.flush();
		  out.close();
	  }
	  catch(Exception e) {e.printStackTrace(); status = -1;}
	  return status;
}


     /**
      * Prints all segments meeting specified pvalueCutoff and significance test in this ResultContainer to the specified file.
      * @param outFile String- output file
      * @param append boolean- true => append, false => overwrite
      * @param pvalCriteria PValue- p-value cutoff
      * @param pvalTestType int- tests defined in PValue.MEAN_AND_SIGN_TEST, PValue.MEAN_OR_SIGN_TEST, PValue.MEAN_TEST, PValue.SIGN_TEST
      * @return int- status ( < 0 => error)
      */
    public int printToFile(String outFile, boolean append, PValue pvalCriteria,int pvalTestType) {
	    int status = 0;
	    try {
	      BufferedWriter out = new BufferedWriter(new FileWriter(outFile,append));
	      int numOfChrs = this.data.getNumChromosomes();
	      //for(int i=0; i < chromosomes.size(); i++) {
	      for(int currChrom = 1; currChrom <= numOfChrs; currChrom++) {
	        //Chromosome currChrom = (Chromosome)chromosomes.get(i);
	    	int currChromInd = currChrom - 1;
	        for (int j = 0; j < experiments.size(); j++) {
	          String currExp = (String)experiments.get(j);
	          //ArrayList windows = (ArrayList) this.getWindowsMeetingCriteria(currExp,currChrom.getNumber(),pvalCriteria,pvalTestType);
	          ArrayList segments = (ArrayList) this.getSegmentsMeetingCriteria(currExp,currChrom,pvalCriteria,pvalTestType);
	          if (segments != null) {
	        	  ArrayList indOrig = getOrigIndArray(currExp, currChromInd);
	            for (int k = 0; k < segments.size(); k++) {
	            	SegmentInfo currSeg = (SegmentInfo) segments.get(k);
	            	int startIndex = lookupGeneIndex(indOrig,currSeg.getStart());
	                int endIndex = lookupGeneIndex(indOrig,currSeg.getEnd());
	            	out.write(currExp + "\t" + currChrom + "\t" +
	            	data.getCloneAt(startIndex, currChromInd) + "\t" +
	                data.getCloneAt(endIndex, currChromInd) + "\t" +
	                //((Gene)currChrom.geneAtRatioIndex(currSeg.getStart(),currExp,false)).getID() + "\t" +
	                //((Gene)currChrom.geneAtRatioIndex(currSeg.getEnd(),currExp,false)).getID() + "\t" +
	                currSeg.getDataMean() + "\t" +
	                currSeg.getStatistic("Sign pos") + "\t" +
	                currSeg.getStatistic("Sign neg") + "\t" +
	                currSeg.getStatistic("Mean") + "\t");
	            	out.newLine();
	            }
	          }
	        }
	      }
	      out.flush();
	      out.close();
	    }
	    catch(Exception e) {e.printStackTrace(); status = -1;}
	    return status;
    }


  /**
   * Merges all segments (predictions) associated with any experiment/chromosome.
   * Not Used
   */
  public void mergeAllSegments() {
  //for (int i=0; i < chromosomes.size(); i++) {
	  for (int numOfChrs = 0; numOfChrs < this.data.getNumChromosomes(); numOfChrs++) {
		  for(int j=0; j < experiments.size(); j++) {
			  //mergeWindows((String)experiments.get(j),((Chromosome)chromosomes.get(i)).getNumber());
			  mergeSegments((String)experiments.get(j),numOfChrs+1);
   }
  }
}

/**
 * Not Used
 * Merges all segments (predictions) associated with specified experiment/chromosome index.  Two separate predictions are
 * deemed single prediction (merged) if the number of genes between them divided by their average length is less than
 * MERGE_SIZETHRESH and if both biases are in the same direction and both have at least a sign or mean test p-value of
 * less than MERGE_SIGTHRESH.
 * @param exp String
 * @param chromosome int
 */
private void mergeSegments(String exp, int chromosome) {
    ArrayList oldSegmentArray = getSegments(exp,chromosome);
    ArrayList segmentArray = (ArrayList)oldSegmentArray.clone();
    Collections.sort(segmentArray);

    for(int i=1; i < segmentArray.size(); i++) {
      SegmentInfo currLeft = ((SegmentInfo)segmentArray.get(i-1));
      SegmentInfo currRight = ((SegmentInfo)segmentArray.get(i));

      if((currRight.getStart() - currLeft.getEnd()) < this.MERGE_SIZETHRESH) {
          //if((currLeft.getStatistic("Sign pos") < this.MERGE_SIGTHRESH && currRight.getStatistic("Sign pos") < this.MERGE_SIGTHRESH) || (currLeft.getStatistic("Sign neg") < this.MERGE_SIGTHRESH && currRight.getStatistic("Sign neg") < this.MERGE_SIGTHRESH)||
          if((currLeft.getStatistic("Mean") < this.MERGE_SIGTHRESH && currRight.getStatistic("Mean") < this.MERGE_SIGTHRESH && ((currRight.getStatistic("Sign pos") < currRight.getStatistic("Sign neg") && currLeft.getStatistic("Sign pos") < currLeft.getStatistic("Sign neg")) || (currRight.getStatistic("Sign pos") > currRight.getStatistic("Sign neg") && currLeft.getStatistic("Sign pos") > currLeft.getStatistic("Sign neg"))))) {
          //merge segments and rerun significance analysis
          currLeft.setEnd(currRight.getEnd());
          currLeft.resetSegment();
          //SigTestThread tester = new SigTestThread((Chromosome)sourceData.getChromosome(chromosome),exp,currLeft);
          float[] geneRatiosOfExprChr = getGeneRatiosOfExprChr (chromosome, exp);
          SigTestThread tester = new SigTestThread(geneRatiosOfExprChr, chromosome, exp, currLeft);
          tester.setTests(true, true,false,false);
          tester.runPermuteTest();
          tester.runSignTest();
          
          //tester.start();
          //try {tester.join();} catch(Exception e){e.printStackTrace();}
          segmentArray.remove(i);
          i--;
        }
      }
    }

    this.setSegments(exp,chromosome,segmentArray);
  }
  /**
   * Method to get all ratios for a Chr of an Expr
   */
  public float[] getGeneRatiosOfExprChr (int curChr, String expr){
	  int expr_ind = 0;
	  for(; expr_ind < data.getFeaturesCount(); expr_ind++){
		  String exprName = data.getFullSampleName(expr_ind);
		  if(exprName.equals(expr.trim())) break;
	  }
	  
	  Experiment experiment = data.getExperiment(); 
      FloatMatrix fm = experiment.getMatrix(); 
      int[][] chrIndices = data.getChromosomeIndices();
      
      int st = chrIndices[curChr][0];
  	  int end = chrIndices[curChr][1];
  	/*
  	 * TODO
  	 * Check fm.getMatrix validity
  	 */
      float[] geneRatiosOfExprChr = fm.getMatrix(st, end, expr_ind, expr_ind).getColumnPackedCopy();
      return geneRatiosOfExprChr;
  }
  
  /**
   * CGH ChARM function, Raktim
   * Array to Map NA Remomed Indices of Genes to Real Indices
   */
  	public ArrayList getOrigIndArray(String currExp, int chrInd) {
 	  
  		ArrayList featuresList = data.getFeaturesList();
 		int exprInd = -1;
 		
 		for (int column = 0; column < featuresList.size(); column++){
 			String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
 			if(name.trim().equals(currExp.trim())) {
 				exprInd = column;
 				break;
 			}
 			//System.out.println("exprNames " + name);
 		}
 		
 	  	int[][] chrIndices = data.getChromosomeIndices();
 		int st = chrIndices[chrInd][0];
 	 	int end = chrIndices[chrInd][1];
 	 	
 	 	ArrayList ratioIndices = new ArrayList();
 	   	//int ind = 0;
 	   	for(int ii = st; ii < end; ii++) {
 	   		float tmp = data.getLogAverageInvertedValue(exprInd, ii);
 	   		if (!Float.isNaN(tmp)) {
 	   			ratioIndices.add(new Integer(ii-st));
 	   			//ind++;
 	   		}
 	   	}
 	   	return ratioIndices;
  	}

 	/**
 	 * CGH ChARM Function Raktim
 	 * TO match NA removed incdices to Original Indices of an Experiment & Chr
 	 */
 	private int lookupGeneIndex(ArrayList indices, int NAIndex) {
 		return ((Integer)indices.get(NAIndex)).intValue();
 	}
 	
  /**
   * Sets the identifier for this ResultContainer.
   * @param id String
   */
  public void setResultID(String id) {
    resultID = id;
  }

  /**
   * Returns the identifier for this ResultContainer.
   * @return String
   */
  public String getResultID() {
    return resultID;
  }
}
