package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.tigr.microarray.mev.cgh.CGHDataGenerator.CharmDataGenerator.Chromosome;

/**
* Encapsulates all data associated with a single dataset (a set of related experiments and associated array values.
*
 * <p>Title: DatasetContainer</p>
 * <p>Description: Encapsulates all data associated with a single dataset (a set of related experiments and associated array values.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class DatasetContainer {
  private String datasetId;
  private String organism;
  private String filename;

  private ArrayList chromosomes;
  private ArrayList experiments; //Array of Strings with Expr Names
  private HashMap extraData;
  private HashMap chromosomeHash;

  /**
   * Class constructor.
   * @param filename String- input file
   * @param organism String- organism type (YEAST, HUMAN, OTHER)
   * @param datasetID String- dataset identifier
   * @throws IOException
   */
  public DatasetContainer(String filename, String organism, String datasetID) throws IOException {
    this.organism = organism;
    this.filename = filename;
    this.datasetId = datasetID;

    experiments = new ArrayList();
    extraData = new HashMap();
    chromosomeHash = new HashMap();

    //GeneFileReader.loadDataFromFile(filename,organism,chromosomeHash,experiments,extraData);
    chromosomes = new ArrayList(chromosomeHash.values());
    Collections.sort(chromosomes);
  }


  /**
   *  Returns an array list of all the experiments of this data set.
   *  @return ArrayList
   */
  public ArrayList getExperiments(){
  return (ArrayList)experiments.clone();
}

/**
 * Returns an arraylist of all the chromosomes of this data set
 * @return ArrayList
 */
public ArrayList getChromosomes(){
return chromosomes;
}

/**
 * Returns the chromosome corresponding to the number given
 * @param chromNum int
 * @return Chromosome
 */
public Chromosome getChromosome(int chromNum){
  return ((Chromosome)chromosomeHash.get(new Integer(chromNum)));
}

/**
 * Returns the number of chromosomes associated with this dataset.
 * @return int
 */
public int numChromosomes(){
  return chromosomeHash.size();
}

}
