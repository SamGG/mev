package org.tigr.microarray.mev.cluster.algorithm.impl.ease;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.junit.Test;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.util.FloatMatrix;

public class TestEASE extends EASE {


	@Test
	public void realUnitTest() {
		String[] sampleList = new String[]{"ann1", "ann2"};
		String[] popList = new String[]{"ann1", "ann2", "ann3"};
		try {
			File convertfile = writeClassPathFileToTempFile("convertfile.txt");
			String convertfileName = convertfile.getAbsolutePath();
			File classFile = writeClassPathFileToTempFile("classfile.txt");
			String[] classFiles = new String[]{classFile.getAbsolutePath()};
			FloatMatrix fm = new FloatMatrix(new float[][]{new float[]{1,2}, new float[]{1,2}});
			AlgorithmData resultData = genericTestEase(sampleList, popList, convertfileName, classFiles, fm);
			if(resultData == null)
				fail("Null Result Data");
			Object[][] results = resultData.getObjectMatrix("result-matrix");
			if(!results[0][2].equals("class1")) {
//				System.out.println(results[0][2]);
				fail();	
			}
			if(!results[1][2].equals("class2"))
				fail();
			if(!results[0][7].equals("6.667E-01"))
				fail();			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			fail("Couldn't write test file.");
		}
		
	}
	
	
	public AlgorithmData genericTestEase(String[] sampleList, String[] popList, String convertfile, String[] classFiles, FloatMatrix expression) {
    	EaseAlgorithmData data = new EaseAlgorithmData();
    	data.setExpression(expression);
    	data.setPerformClusterAnalysis(true);
    	data.setRunNease(false);
    	data.setReportEaseScore(false);
    	
    	if(convertfile != null && !convertfile.equals(""))
    		data.setConverterFileName(convertfile);
    	data.setAnnotationFileList(classFiles);
    	data.setPvalueCorrectionsOption("none");
    	data.setTrimOption("NO_TRIM");
    	data.setPopulationList(popList);
    	
        data.setSampleList(sampleList);
        //The indices of the genes that go with these annotations
        int[] sampleIndices = new int[sampleList.length];
        for(int i=0; i<sampleIndices.length; i++) 
        	sampleIndices[i] = i;
        data.setSampleIndices(sampleIndices);
		try {
			AlgorithmData outData = execute(data);
		    Object [][] datamatrix = outData.getObjectMatrix("result-matrix");
		    String [] headerNames = outData.getStringArray("header-names");

		    if(datamatrix == null || datamatrix.length <= 0) {
		    	System.out.println("No results found.");
		    	return null;
		    }
		    System.out.print(headerNames[0]);
		    for(int i=1; i<headerNames.length; i++){
		    	System.out.print("\t" + headerNames[i]);
		    }
		    System.out.print("\n");
		    for(int i=0; i<datamatrix.length; i++){
		    	System.out.print(datamatrix[i][0]);
		    	for(int j=1; j<datamatrix[i].length; j++) {
		    		System.out.print("\t" + datamatrix[i][j]);
		    	}
		    	System.out.print("\n");
		    }		    
		    return outData;
		} catch (AlgorithmException ae) {
			ae.printStackTrace();
			fail();
		}
		return null;
	}
	
	
	/**
	 * loads a text file into a String[]. Each line in the file is loaded into one element in the String[].
	 * @param listfile
	 * @return An array where each element contains one line of listfile
	 */
	private String[] loadListFile(String listfile) {

		Vector<String> poplist = new Vector<String>();
		InputStream in = null;
		in = TestEASE.class.getResourceAsStream(listfile);
		if(in == null) {
			System.out.println("Could not read file " + listfile + 
					"\nDid you refresh Eclipse's Navigator view?");
			fail("Could not read infile " + listfile);
		} 
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			while((line = br.readLine()) != null) {
				poplist.add(line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return poplist.toArray(new String[poplist.size()]);
	}
	
	/**
	 * EASE requires file names as part of it's input. Since the data files used by these tests are kept
	 * on the classpath, file names are not available. This method will take a file from the classpath
	 * in the current directory (tThe same one this class is in) and write it to a temp file, then return
	 * that temp file. That temp file name can be safely passed to EASE.
	 * @param fileOnClasspath The name of a file located in org.tigr.microarraym.mev.cluster.algorithm.impl.ease
	 * @return A temp file containing the contents of fileOnClasspath
	 * @throws IOException If there is a problem writing the file to the temp directory.
	 */
	private static File writeClassPathFileToTempFile(String fileOnClasspath) throws IOException {
		InputStream in = null;
		in = TestEASE.class.getResourceAsStream(fileOnClasspath);
		if(in == null) {
			System.out.println("Could not read file " + fileOnClasspath + 
					"\nDid you refresh Eclipse's Navigator view?");
			fail("Could not read infile " + fileOnClasspath);
		} 
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		File tempFile = File.createTempFile("mev_unit_test", ".txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
		String line;
		try {
			while((line = br.readLine()) != null) {
				bw.write(line + "\n");
//				System.out.print(line);
			}
			bw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return tempFile;
	}
}


