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
/* BootstrapModule.java
 * Copyright (C) 2005 Amira Djebbari
 */
//package edu.harvard.dfci.compbio.bnUsingLit.prepareArrayData.bootstrap;
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.util.Properties;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import weka.core.Instances;

/**
 * The class <code>BootstrapModule</code> contains methods to perform bootstrap from a given dataset with given options
 * as specified in a properties file.
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class BootstrapModule {
    /**
     * The <code>bootstrap</code> method performs bootstrap from a given dataset and options specified in a properties file.
     *
     * @param propsFileName a <code>String</code> denoting the name of the properties file that should 
     * contain one required property:    
     * <br> 
     * inArffExpressionFileName which denotes the name of the input expression file in WEKA ARFF format. 
     * The data needs to be nominal (in other words, discretized first).
     * and some optional properties:
     * <ul>
     * <li> numBins corresponding to the number of bins in which the data is to be discretized. The default is 3.
     * <li> binLabeli corresponding to the label of each bin. For example, if numBins=3,
     * binLabel0=state0, binLabel1=state1, binLabel2=state2, the labels of the 3 bins 
     * will be state0, state1 and state2 respectively
     * <li> numBootstrapIterations: if isBootstrap=true, it denotes the number of bootstrap iterations. The default is 100.
     * <li> rootOutputFileName: if isBootstrap=true, it denotes the name of the root output file where each bootstrapped 
     * dataset is to be written. The default is boot_.
     * </ul>
     */    
    public static void bootstrap(String propsFileName){
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    //read params from props
	    String inArffExpressionFileName = props.getProperty("inArffExpressionFileName", null);
	    Useful.checkFile(inArffExpressionFileName);
	    Instances instances = WekaUtil.readInstancesArff(inArffExpressionFileName);	    
	    //given instances in file or WEKA Instances data structure
	    bootstrap(props, instances);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	    nae.printStackTrace();
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	    ioe.printStackTrace();
	}
    }
    
    /**
     * The <code>bootstrap</code> method here perform bootstrap from a given dataset in WEKA Instances object 
     * with given options as specified in a properties file
     *
     * @param props a <code>Properties</code> denoting the name of the properties file 
     * that should contain 5 optional properties:
     * <ul>
     * <li> outArffExpressionFileName which denotes the name of the output expression file where the data will be written
     * after being transposed, discretized and missing values replaced in WEKA ARFF format.
     * The default is "outExpression.arff"
     * <li> numBins corresponding to the number of bins in which the data is to be discretized. The default is 3.
     * <li> binLabeli corresponding to the label of each bin. For example, if numBins=3,
     * binLabel0=state0, binLabel1=state1, binLabel2=state2, the labels of the 3 bins 
     * will be state0, state1 and state2 respectively
     * <li> numBootstrapIterations: if isBootstrap=true, it denotes the number of bootstrap iterations. The default is 100.
     * <li> rootOutputFileName: if isBootstrap=true, it denotes the name of the root output file where each bootstrapped 
     * dataset is to be written. The default is boot_.
     * </ul>
     * @param instances an <code>Instances</code> corresponding to the given dataset in WEKA Instances object
     * @exception NullArgumentException if an error occurs because at least one of properties or instances is null
     */
    public static void bootstrap(Properties props, Instances instances) throws NullArgumentException{	
		if(props == null || instances == null){
		    throw new NullArgumentException("At least one of properties or instances is null\nproperties="+props+"\ninstances="+instances);
		}
		//Raktim
		String sep = System.getProperty("file.separator");
		String path = System.getProperty("user.dir"); // Raktim - Use tmp Dir
		//path=path+sep+"data"+sep+"bn"+sep+"tmp"+sep;
		path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.TMP_DIR + BNConstants.SEP;
		
		int numBootstrapIterations = Integer.parseInt(props.getProperty("numBootstrapIterations","100"));
		int numBins = Integer.parseInt(props.getProperty("numBins", "3"));
		ArrayList binLabels = new ArrayList();
		for(int i = 0; i < numBins; i++){
		    binLabels.add(props.getProperty("binLabel"+i,"state"+(i+1)));
		}
		String rootOutFileName = props.getProperty("rootOutputFileName","boot_");
		long seed = (long) Integer.parseInt(props.getProperty("seed","1"));
		//save to CSV
	    WekaUtil.writeDataToCSVFile(instances, path+"instances.csv");
		//call Bootstrap on csv
		Bootstrap.createDataSets(path+"instances.csv", rootOutFileName, numBootstrapIterations, seed);
		System.out.println("Bootstrap.createDataSets() -- rootOutFileName " + rootOutFileName);
		//convert all boot files from csv to arff
		Instances[] bootstraps = new Instances[numBootstrapIterations];
		File f = null;
		for(int i = 0; i < numBootstrapIterations; i++){
			System.out.println("WekaUtil.readInstancesCSV() -- rootOutFileName+i " + rootOutFileName+i);
		    bootstraps[i] = WekaUtil.readInstancesCSV(rootOutFileName+i); 
		    f = new File(rootOutFileName+i);
		    f.delete();
		    System.out.println("WekaUtil.writeDataToArffFile() -- rootOutFileName+i " + rootOutFileName+i);
		    WekaUtil.writeDataToArffFile(bootstraps[i], rootOutFileName+i);
		    //fix states
		    System.out.println("FixStates.fixStates() -- rootOutFileName+i " + rootOutFileName+i);
		    FixStates.fixStates(path+rootOutFileName+i, binLabels, ".arff");
		    
		}	
    }

    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
	    System.out.println("Usage: java BootstrapModule propertiesFileName\nExample: java BootstrapModule bootstrap.props");
	    System.exit(0);	
    }
    public static void main(String[] argv){
  	if(argv.length != 1){
	    usage();
  	}
	String propsFileName = argv[0];
	bootstrap(propsFileName);
    }

}
