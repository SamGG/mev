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
/* WekaUtil.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;import java.io.IOException;import java.io.File;
import weka.core.Instances;import weka.core.converters.CSVLoader;
import weka.core.converters.ArffLoader;import weka.core.converters.ArffSaver;import weka.core.converters.CSVSaver;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
public class WekaUtil {
    /**
     * The <code>readInstancesCSV</code> method is given the name of data file in CSV format 
     * and returns a WEKA Instances object containing the data read from the given file name
     * @param inFileName a <code>String</code> corresponding to the name of the data file in CSV format:
     * <br>
     * In this application, the transposed expression matrix from MeV with GenBank accessions (GB)
     * in the X dimension and samples in the Y dimension.     
     * <br>
     * The format of the given gene expression matrix data file should be:
     * <br>
     * CLASS\tGB_1\tGB_2\t...\tGB_n
     * <br>
     * sample_1\texpr_1_1\texpr_2_1\t...\texpr_n_1
     * <br>
     * ...
     * <br>
     * sample_n\texpr_1_n\texpr_2_n\t...\texpr_n_n
     * <br>
     * where expr_i_j means expression of gene i in sample j
     * <br>
     * @return an <code>Instances</code> corresponding to a WEKA Instances object containing the data read 
     * from the given file name in CSV format
     */    public static Instances readInstancesCSV(/*String path,*/ String inFileName) {
		try {
			String dataPath = TMEV.getDataPath();
	    	File pathFile = TMEV.getFile("data/");
	    	if(dataPath != null) {
	            pathFile = new File(dataPath);
	            if(!pathFile.exists())
	                pathFile = TMEV.getFile("data/");
	        }
		    //Useful.checkFile(path);
		    CSVLoader loader = new CSVLoader();
		    //System.exit(1);
		    loader.setSource(new File(inFileName));
		    Instances data = loader.getDataSet();
		    return data;
		}
		catch(IOException ioe){
		    System.out.println(ioe);
		    ioe.printStackTrace();
		}	
		return null;
    }
    
    /**
     * The <code>readInstancesArff</code> method is given the name of the input file in ARFF format
     * and loads the data contained in the given file in WEKA Instances object
     * (See WEKA documentation for details on ARFF format)
     * @param inFileName a <code>String</code> corresponding to the name of the input file in ARFF format
     * @return an <code>Instances</code> corresponding to a new WEKA Instances object 
     * containing the data read from the given file name in ARFF format
     */
    public static Instances readInstancesArff(String inFileName) {
	try {		//System.out.println("readInstancesArff()" + inFileName);
	    Useful.checkFile(inFileName);
	    ArffLoader loader = new ArffLoader();
	    loader.setSource(new File(inFileName));
	    Instances data = loader.getDataSet();
	    return data;
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	    ioe.printStackTrace();
	}	
	return null;
    }

    /**
     * Describe <code>writeDataToArffFile</code> method is given a WEKA Instances object and the name of the output file name
     * and writes the given WEKA data to the output file in the WEKA ARFF format
     * (See WEKA documentation for details on ARFF format)
     *
     * @param data an <code>Instances</code> corresponding to a WEKA Instances object containing data
     * @param arffFileName a <code>String</code> denoting the name of the output file where the given data
     * will be written in WEKA ARFF format
     * @exception NullArgumentException if an error occurs because the given data is null
     */
    public static void writeDataToArffFile(Instances data, String arffFileName) throws NullArgumentException{	String sep = System.getProperty("file.separator");
	String path = System.getProperty("user.dir"); // Raktim - Use tmp Dir
	path=path+sep+"data"+sep+"bn"+sep+"tmp"+sep;
	if(data == null){
	    throw new NullArgumentException("Parameter data passed to writeDataToArffFile method was null!");
	}
	try {
	    	
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(path,arffFileName));
	    saver.setDestination(new File(path,arffFileName));
	    saver.writeBatch();
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }
     /**
     * The <code>writeDataToCSVFile</code> method is given a WEKA Instances object and the name of the output file name
     * and writes the given WEKA data to the output file in CSV format.
     * @param data an <code>Instances</code> corresponding to a WEKA Instances object containing data
     * @param csvFileName a <code>String</code> denoting the name of the file where the given data
     * will be written in CSV format.
     * In this application, with GenBank accessions (GB) in the X dimension and samples in the Y dimension
     * <br>
     * The format of the given gene expression matrix data file should be:
     * <br>
     * CLASS\tGB_1\tGB_2\t...\tGB_n
     * <br>
     * sample_1\texpr_1_1\texpr_2_1\t...\texpr_n_1
     * <br>
     * ...
     * <br>
     * sample_n\texpr_1_n\texpr_2_n\t...\texpr_n_n
     * <br>
     * where expr_i_j means expression of gene i in sample j 
     * <br>
     *
     * @exception NullArgumentException if an error occurs because the given data is null.
     */
    public static void writeDataToCSVFile(Instances data, String csvFileName) throws NullArgumentException{
	if(data == null){
	    throw new NullArgumentException("Parameter data passed to writeDataToCSVFile method was null!");
	}
	try {
	    CSVSaver saver = new CSVSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(csvFileName));
	    saver.setDestination(new File(csvFileName));
	    saver.writeBatch();
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }

}






