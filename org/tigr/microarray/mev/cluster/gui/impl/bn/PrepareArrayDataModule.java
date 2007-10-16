/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* PrepareArrayDataModule.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;import weka.core.Instances;import weka.filters.Filter;import weka.filters.unsupervised.attribute.Discretize;import weka.filters.unsupervised.attribute.ReplaceMissingValues;import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.ArrayList;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
/**
 * The class <code>PrepareArrayDataModule</code> contains methods to prepare a subset of gene expression matrix 
 * in tab-delimited format (such as from MeV) to be analyzed in WEKA arff format 
 * by transposing, discretizing into 3 bins of equal-width (namely state1, state2, state3) 
 * and replacing missing values if any.
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class PrepareArrayDataModule {
    /**
     * The <code>transpose</code> method reads the expression matrix from the given input file
     * and writes the transpose of it to the output file.
     * @param inFileName a <code>String</code> denoting the name of the input expression matrix data file
     * with samples in the X dimension and GenBank accessions (GB) in the Y dimension
     * The format of the given gene expression matrix data file should be:
     * <br>
     * CLASS\tsample_1\tsample_2\t...\tsample_n 
     * <br>
     * GB_1\texpr_1_1\texpr_1_2\t...\texpr_1_n
     * <br>
     * ...
     * <br>
     * GB_n\texpr_n_1\texpr_n_2\t...\texpr_n_n
     * <br>
     * where expr_i_j means expression of gene i in sample j
     * @param outFileName a <code>String</code> denoting the name of the output expression matrix data file
     */
    public static void transpose(String inFileName, String outFileName) {
	try {		System.out.println("transpose()" + outFileName);
	    Useful.checkFile(inFileName);
	    Transpose.readAndWriteTranspose(inFileName, outFileName);	
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	    fnfe.printStackTrace();
	}
    }

    /**
     * The <code>discretize</code> method is given a WEKA Instances object corresponding to the gene expression data
     * and returns a new WEKA Instances object with the given data discretized into a given number of equal-width bins
     *
     * @param data an <code>Instances</code> which is a WEKA Instances object corresponding to the gene expression data
     * @param numBins a <code>String</code> corresponding to the number of bins in which the data is to be discretized
     * @return an <code>Instances</code> a new WEKA Instances object with the given data discretized 
     * into a given number of equal-width bins
     * @exception NullArgumentException if an error occurs if the data is null
     * @exception OutOfRangeException if an error occurs if the numBins is out of bounds (namely, negative or equal to zero)
     */
    public static Instances discretize(Instances data, String numBins) throws NullArgumentException, OutOfRangeException {
	if(data == null){
	    throw new NullArgumentException("Parameter data passed to discretize method was null!");
	}
	if(Integer.parseInt(numBins) <= 0){
	    throw new OutOfRangeException("numBins is out of range (should be strictly positive!\nnumBins="+numBins);
	}
	try {
	    String[] options = new String[2];
	    options[0] = "-B";
	    options[1] = numBins;
	    Discretize discretize = new Discretize();
	    discretize.setOptions(options);
	    discretize.setInputFormat(data);
	    Instances newData = Filter.useFilter(data, discretize);
	    return newData;
	}
	catch(Exception e){
	    System.out.println(e);
	    e.printStackTrace();
	}
	return null;
    }
    /**
     * Describe <code>replaceMissingValues</code> method is given a WEKA Instances object corresponding to
     * the gene expression data
     * and returns a new WEKA Instances object with missing values replaced, if any
     *
     * @param data an <code>Instances</code> which is a WEKA Instances object corresponding to the gene expression data
     * @return an <code>Instances</code> corresponding to a new WEKA Instances object with missing values replaced, if any
     * @exception NullArgumentException if an error occurs if the given data is null
     */
    public static Instances replaceMissingValues(Instances data) throws NullArgumentException{
	if(data == null){
	    throw new NullArgumentException("replace values passed to discretize method is null!");
	}
	try {
	    ReplaceMissingValues replaceMissingValues = new ReplaceMissingValues();
	    replaceMissingValues.setInputFormat(data);
	    Instances newData = Filter.useFilter(data, replaceMissingValues);
	    return newData;
	}
	catch(Exception e){
	    System.out.println(e);
	    e.printStackTrace();
	}
	return null;
    }
    
    /**
     * The <code>prepareArrayData</code> method prepares array data according to the options found 
     * in the given properties file
     * @param propsFileName a <code>String</code> denoting the name of the properties file that should contain
     * one required property:
     * <br> 
     * inTabDelimitedExpressionFileName which denotes the name of the input expression file in tab-delimited format
     * with samples in the X dimension and GenBank accessions (GB) in the Y dimension
     * The format of the given gene expression matrix data file should be:
     * <br>
     * CLASS\tsample_1\tsample_2\t...\tsample_n 
     * <br>
     * GB_1\texpr_1_1\texpr_1_2\t...\texpr_1_n
     * <br>
     * ...
     * <br>
     * GB_n\texpr_n_1\texpr_n_2\t...\texpr_n_n
     * <br>
     * where expr_i_j means expression of gene i in sample j
     * <br>
     * and 4 or 6 optional properties, depending on whether isBootstrap is false or true, respectively:
     * <ul>
     * <li> outArffExpressionFileName which denotes the name of the output expression file where the data will be written
     * after being transposed, discretized and missing values replaced in WEKA ARFF format.
     * The default is "outExpression.arff"
     * <li> numBins corresponding to the number of bins in which the data is to be discretized. The default is 3.
     * <li> binLabeli corresponding to the label of each bin. For example, if numBins=3,
     * binLabel0=state0, binLabel1=state1, binLabel2=state2, the labels of the 3 bins will be state0, state1 and state2
     * respectively
     * <li> isBootstrap denoting whether data should be bootstrapped. The default is false.
     * <li> numBootstrapIterations: if isBootstrap=true, it denotes the number of bootstrap iterations. The default is 100.
     * <li> rootOutputFileName: if isBootstrap=true, it denotes the name of the root output file where each bootstrapped 
     * dataset is to be written. The default is boot_.
     * </ul>
     */
      //public static void prepareArrayData(String fileName,String num){
    public static Properties prepareArrayData(String fileName,String num, boolean bootStrap, int numIter){
    	try {
    	        		System.out.println("prepareArrayData()" + fileName);
    	    Useful.checkFile(fileName);    	    boolean isBootstrapStr = bootStrap; //Raktim - Temp. Need to handle differently later.
    	    //String fullPathfileName=Useful.getFilePath();
    	    String outFileName = "outExpression.arff";
    	    String numBins = num;
    	    ArrayList binLabels = new ArrayList();
    	    for(int i = 0; i < Integer.parseInt(numBins); i++){    	    	binLabels.add("state"+i);
    	    }
    	    // transpose the given expression data
    	    transpose(fileName,fileName.substring(0, fileName.length()-4)+"_transposed.csv");
    	    // read the transposed data into WEKA Instances object    	    //System.exit(1);
    	    Instances data = WekaUtil.readInstancesCSV(/*fileName,*/fileName.substring(0, fileName.length()-4)+"_transposed.csv");
    	    // discretize the data
    	    Instances discreteData = discretize(data, numBins);
    	    // set the CLASS attribute to be the first attribute
    	    discreteData.setClassIndex(0);
    	    // replace missing values in the data, if any
    	    Instances discreteAndCompleteData = replaceMissingValues(discreteData);
    	    // rename states to be the name of the bins provided in the properties file (e.g. "state1", "state2", "state3")
    	    // for each attribute except the CLASS attribute
    	    Instances renamedStatesData = RenameStates.renameStates(discreteAndCompleteData, binLabels);    	    // Raktim - Bootstrap 
    	    Properties props = new Properties();
    	    
    	    if(isBootstrapStr){
    	    	// props.setProperty("outArffExpressionFileName",outFileName);
    	    	props.setProperty("bootStrap", "true");
        	    props.setProperty("numBootstrapIterations",Integer.toString(numIter));
        	    props.setProperty("numBins", numBins);
        	    props.setProperty("rootOutputFileName","boot_");
        	    props.setProperty("seed","1");
        	    for(int i = 0; i < Integer.parseInt(numBins); i++){
            		props.setProperty("binLabel"+i,"state"+(i));
            	}
    	    	// do bootstrap
    	    	BootstrapModule.bootstrap(props, renamedStatesData);
    	    	
    	    	// Also create ARFF file for observed data
    	    	WekaUtil.writeDataToArffFile(discreteAndCompleteData, outFileName);
    	    	return props;
    	    }
    	    else {
    	    	// write data to a file in ARFF format
    	    	WekaUtil.writeDataToArffFile(discreteAndCompleteData, outFileName);
    	    	props.setProperty("bootStrap", "false");
    	    	return props;
    	    }
    	}
    	
    	catch(OutOfRangeException oore){
    	    //System.out.println(oore);
    	    oore.printStackTrace();    	    return null;
    	}
    	catch(NullArgumentException nae){
    	    System.out.println(nae);
    	    nae.printStackTrace();    	    return null;
    	}
    	catch(IOException ioe){
    	    System.out.println(ioe);
    	    ioe.printStackTrace();    	    return null;
    	}
        }
    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
	    System.out.println("Usage: java PrepareArrayDataModule propertiesFileName\nExample: java PrepareArrayDataModule prepareArrayData.props");
	    System.exit(0);	
    }
}


