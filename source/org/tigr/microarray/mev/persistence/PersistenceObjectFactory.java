/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.CGHSlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.SpotInformationData;
import org.tigr.microarray.mev.annotation.AnnotationStateSavingParser;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.util.FloatMatrix;

/**
 * Writes FloatMatrix objects out to binary files and reads from those files to re-created
 * the saved FloatMatrix.  
 * 
 * @author eleanora
 *
 */
public class PersistenceObjectFactory {

	/**
	 * Reads a binary file written by writeMatrix(File outputFile, FloatMatrix fm) and
	 * returns a FloatMatrix object created from the data in outputFile.  
	 * 
	 * @param inputFile The name of the file to be read from. Assumed to be found in the System
	 *   temp directory in a folder named by MultipleArrayViewer.CURRENT_TEMP_DIR
	 * @return a new FloatMatrix containing the data from inputFile
	 * @throws IOException
	 */
	public static FloatMatrix readFloatMatrix(String inputFile) throws IOException{
		File binFile = new File(MultipleArrayViewer.CURRENT_TEMP_DIR , inputFile);
		FloatMatrix fm = new FloatMatrix(readMatrix(binFile));
		return fm;
	}
	
	/**
	 * Writes a FloatMatrix out to a binary file specified by the name outputFile.  
	 * @param outputFile This file is assumed to be located in the System temp directory
	 *   in the folder MultipleArrayViewer.CURRENT_TEMP_DIR
	 * @param fm the FloatMatrix to be written to file
	 * @throws IOException
	 */
    public static void writeMatrix(File outputFile, FloatMatrix fm) throws IOException {
    	
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        try {
    	
			int numRows = fm.getRowDimension();
			int numCols = fm.getColumnDimension();
			dos.writeInt(numRows);
			dos.writeInt(numCols);
			for(int i=0; i<numRows; i++){
				for(int j=0; j<numCols; j++){
					dos.writeFloat(new Float(fm.A[i][j]).byteValue());
				}
			}
    	} catch (Exception e) {
    		System.out.println("Error in writing floatmatrix "+ outputFile.getName());
    		e.printStackTrace();

    	} finally {
    		dos.flush();
    		dos.close();
    	}
    }

    private static float[][] readMatrix(File binFile) throws IOException{

    	DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(binFile)));
		float[][] matrix = new float[1][1];
		try {
			int numRows, numCols;
			numRows = dis.readInt();
			numCols = dis.readInt();
			matrix = new float[numRows][numCols];
			for(int i=0; i<numRows; i++){
				for(int j=0; j<numCols; j++){
					matrix[i][j] = dis.readFloat();
				}
			}    	
		} catch (IOException e) {
			System.out.println("Couldn't read floatmatrix from file " + binFile.getName());
			e.printStackTrace();
		} finally {
			dis.close();
		}
    	return matrix;
    	
    }
    /**
     * 
     * Constructor to maintain backwards compatibility with version 4.3 and lower
     * @param sampleLabelKeys
     * @param sampleLabels
     * @param filename
     * @param name
     * @param isNonZero
     * @param normalizedState
     * @param sortState
     * @param spotInfoData
     * @param dataType
     * @param ismd
     * @param intensityFileName
     * @return
     * @throws IOException
     */
    public static FloatSlideData makeFloatSlideData(Vector sampleLabelKeys, Hashtable sampleLabels, 
    		String filename, String name, Boolean isNonZero,
			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
			Integer dataType, ISlideMetaData ismd, String intensityFileName) throws IOException{
    	
    	return(makeFloatSlideData(sampleLabelKeys, sampleLabels, filename, name, isNonZero, normalizedState, sortState, spotInfoData, dataType, ismd, intensityFileName, null));
    	
    	
    }
    
    
    
    
    

    public static FloatSlideData makeFloatSlideData(Vector sampleLabelKeys, Hashtable sampleLabels, 
    		String filename, String name, Boolean isNonZero,
			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
			Integer dataType, ISlideMetaData ismd, String intensityFileName, SampleAnnotation sampAnn) throws IOException {
    	FloatSlideData fsd;
    	fsd = new FloatSlideData(sampleLabelKeys, sampleLabels, 
    			filename, name, isNonZero.booleanValue(), 
				normalizedState.intValue(), sortState.intValue(), spotInfoData, 
				dataType, ismd);

    	File filePath = new File(MultipleArrayViewer.CURRENT_TEMP_DIR, intensityFileName);
    	DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
    	
    	float[] currentCY3 = new float[dis.readInt()];
    	for(int i=0; i<currentCY3.length; i++){
    		currentCY3[i] = dis.readFloat();
    	}
    	fsd.setCurrentCY3(currentCY3);
    	float[] currentCY5 = new float[dis.readInt()];
    	for(int i=0; i<currentCY5.length; i++){
    		currentCY5[i] = dis.readFloat();
    	}
    	fsd.setCurrentCY5(currentCY5);
    	float[] trueCY3 = new float[dis.readInt()];
    	for(int i=0; i<trueCY3.length; i++){
    		trueCY3[i] = dis.readFloat();
    	}
    	fsd.setTrueCY3(trueCY3);
    	float[] trueCY5 = new float[dis.readInt()];
    	for(int i=0; i<trueCY5.length; i++){
    		trueCY5[i] = dis.readFloat();
    	}
    	fsd.setTrueCY5(trueCY5);
    	char[] detection = new char[dis.readInt()];
    	for(int i=0; i<detection.length; i++){
    		detection[i] = dis.readChar();
    	}
    	fsd.setDetection(detection);
    	
    	float[] pvalues = new float[dis.readInt()];
    	for(int i=0; i<pvalues.length; i++){
    		pvalues[i] = dis.readFloat();
    	}
    	fsd.setPvalues(pvalues);
    	
    	int[] flags = new int[dis.readInt()];
    	for(int i=0; i<flags.length; i++){
    		flags[i] = dis.readInt();
    	}
    	fsd.setGenePixFlags(flags);
    	
    	dis.close();
    	if(sampAnn!=null){
    		fsd.setSampleAnnotation(sampAnn);
    	  	
    	}
    	
    	return fsd;
    }

    
    
    /**
     * This method was retained to allow backwards compatibility of saved MeV
     * analysis results
     * 
     * @param slideDataName
     * @param sampleLabelKeys
     * @param sampleLabelKey
     * @param sampleLabels
     * @param slideFileName
     * @param isNonZero
     * @param rows
     * @param columns
     * @param normalizedState
     * @param sortState
     * @param spotInfoData
     * @param fieldNames
     * @param dataType
     * @param annotationFileName
     * @param dataFile
     * @return
     * @throws IOException
     */
    
    public static SlideData makeSlideData(String slideDataName, Vector sampleLabelKeys, String sampleLabelKey,
    		Hashtable sampleLabels, String slideFileName, Boolean isNonZero, Integer rows, Integer columns,
			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
			String[] fieldNames, Integer dataType,
			String annotationFileName, String dataFile) throws IOException {
    	return PersistenceObjectFactory.makeSlideData(slideDataName, sampleLabelKeys, sampleLabelKey,
    			sampleLabels, slideFileName, isNonZero, rows, columns, 
    			normalizedState, sortState, spotInfoData, 
    			fieldNames, dataType, 
    			annotationFileName, dataFile, null, null);
    }

    /***
     * 
     * @param slideDataName
     * @param sampleLabelKeys
     * @param sampleLabelKey
     * @param sampleLabels
     * @param slideFileName
     * @param isNonZero
     * @param rows
     * @param columns
     * @param normalizedState
     * @param sortState
     * @param spotInfoData
     * @param fieldNames
     * @param dataType
     * @param annotationFileName
     * @param dataFile
     * @param iAnnotationFileName
     * @return
     * @throws IOException
     * 
     * Constructor to maintain backwards compatibility with version 4.1 thru 4.3 
     * 
     * 
     */
   public static SlideData makeSlideData(String slideDataName, Vector sampleLabelKeys, String sampleLabelKey,
     		Hashtable sampleLabels, String slideFileName, Boolean isNonZero, Integer rows, Integer columns,
 			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
 			String[] fieldNames, Integer dataType,	String annotationFileName, String dataFile, String iAnnotationFileName) throws IOException {
     	      return(  makeSlideData( slideDataName, sampleLabelKeys,  sampleLabelKey,
     	        		 sampleLabels, slideFileName,  isNonZero, rows,  columns,
     	    			 normalizedState,  sortState,  spotInfoData, 
     	    			 fieldNames,  dataType,
     	    			 annotationFileName,  dataFile,  iAnnotationFileName, null));
     	
     }
     
    
    
    /***
     * 
     * @param slideDataName
     * @param sampleLabelKeys
     * @param sampleLabelKey
     * @param sampleLabels
     * @param slideFileName
     * @param isNonZero
     * @param rows
     * @param columns
     * @param normalizedState
     * @param sortState
     * @param spotInfoData
     * @param fieldNames
     * @param dataType
     * @param annotationFileName
     * @param dataFile
     * @param iAnnotationFileName
     * @return
     * @throws IOException
     * 
     * This method was created to enable state-saving, when annotations (using Resourcerer) are loaded.
     * This was added in the MeV 4.1.01 release.
     * 
     */
    
    
  


    
    public static SlideData makeSlideData(String slideDataName, Vector sampleLabelKeys, String sampleLabelKey,
    		Hashtable sampleLabels, String slideFileName, Boolean isNonZero, Integer rows, Integer columns,
			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
			String[] fieldNames, Integer dataType,
			String annotationFileName, String dataFile, String iAnnotationFileName, SampleAnnotation sampAnn) throws IOException {

    	SlideData aSlideData;
    	aSlideData = new SlideData(slideDataName, sampleLabelKeys, sampleLabelKey,
        		sampleLabels, slideFileName, isNonZero, rows, columns,
				normalizedState, sortState, spotInfoData, 
				fieldNames, dataType);
    	
    	//load annotation
    	DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(MultipleArrayViewer.CURRENT_TEMP_DIR, annotationFileName))));
    	Vector allSlideDataElements = loadSlideDataAnnotation(dis, dataType.intValue());
    	dis.close();

    	Vector<IAnnotation> allIAnnotations = new Vector<IAnnotation>();
    	if(iAnnotationFileName != null) {
	    	File iAnnotFile=new File(MultipleArrayViewer.CURRENT_TEMP_DIR, iAnnotationFileName);
	    	FileReader fr=new FileReader(iAnnotFile);
	    	try {
	    		if(iAnnotFile.length()>1)
	    			allIAnnotations=loadSlideDataIAnnotation(iAnnotFile);
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    }

    	//load intensities
    	dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(MultipleArrayViewer.CURRENT_TEMP_DIR, dataFile))));
    	ISlideDataElement sde;
    	int numSlideDataElements = dis.readInt();
    	for(int i=0; i<numSlideDataElements; i++){
    		sde = (ISlideDataElement)allSlideDataElements.get(i);
    		sde.setIntensity(0, dis.readFloat());
    		sde.setIntensity(1, dis.readFloat());
    		sde.setTrueIntensity(0, dis.readFloat());
    		sde.setTrueIntensity(1, dis.readFloat());
    		if(dataType.intValue() != IData.DATA_TYPE_TWO_INTENSITY && dataType.intValue() != IData.DATA_TYPE_RATIO_ONLY){
        		sde.setDetection(new Character(dis.readChar()).toString());
    		} 	
    	
    		if(allIAnnotations != null && allIAnnotations.size()>0)
    		sde.setElementAnnotation((IAnnotation)allIAnnotations.get(i));
    	}
    	dis.close();
    	
		aSlideData.setAllElements(allSlideDataElements);
		aSlideData.updateFilledAnnFields();
		if(sampAnn!=null){
			aSlideData.setSampleAnnotation(sampAnn);
			
		}

    	return aSlideData;
    }
    
    private static Vector loadSlideDataAnnotation(DataInputStream dis, int dataType) throws IOException {
    	int numSlideDataElements = dis.readInt();
    	Vector allSlideDataElements = new Vector(numSlideDataElements);
    	boolean isCGHData = dis.readBoolean();
    	
    	int[] rows, cols;
    	String[] extraFields;
    	String uid;
    	int temp;
    	boolean isNull, isNonZero;
    	for(int i=0; i<numSlideDataElements; i++){

    		temp = dis.readInt();
    		char[] buff = new char[temp];
    		for(int j=0; j<temp; j++) {
    			buff[j] = dis.readChar();
    		}
    		uid = new String(buff);

    		rows = new int[dis.readInt()];
    		for(int j=0; j<rows.length; j++){
    			rows[j] = dis.readInt();
    		}
		
    		cols = new int[dis.readInt()];
    		for(int j=0; j<cols.length; j++){
    			cols[j] = dis.readInt();
    		}
		
    		extraFields = new String[dis.readInt()];
    		for(int j=0; j<extraFields.length; j++){
    			buff = new char[dis.readInt()];
        		for(int k=0; k<buff.length; k++){
        			buff[k] = dis.readChar();
        		}
        		extraFields[j] = new String(buff);
    		}
			isNull = dis.readBoolean();
			isNonZero = dis.readBoolean();
			//Make sure extraFields contains unique values
			
			HashMap<String, String> temphash = new HashMap<String,String>();
			for(String fieldName: extraFields) {
				temphash.put(fieldName, "");
			}
			extraFields = temphash.keySet().toArray(new String[temphash.keySet().size()]);
			
			if(dataType == IData.DATA_TYPE_TWO_INTENSITY || dataType == IData.DATA_TYPE_RATIO_ONLY){
				if(isCGHData){
					allSlideDataElements.add(i, new CGHSlideDataElement(rows, cols, extraFields, uid, isNull, isNonZero));
				} else {
					allSlideDataElements.add(i, new SlideDataElement(rows, cols, extraFields, uid, isNull, isNonZero));
				}
			} else {		//IData has affy data
				char detection = dis.readChar();
				float pValue = dis.readFloat();
				int flags = dis.readInt();
				allSlideDataElements.add(i, new AffySlideDataElement(rows, cols, extraFields, uid, isNull, isNonZero, detection, pValue, flags));

			}
    	}

    	return allSlideDataElements;
    	
    }

	/**
	 * Writes the annotation contained in SlideData sd to binary file outputFile.
	 * @param outputFile
	 * @param sd
	 */
	public static void writeSlideDataAnnotation(DataOutputStream dos, SlideData sd) throws IOException {
		Vector allSlideDataElements = sd.getAllElements();
    	int numSlideDataElements = allSlideDataElements.size();
    	ISlideDataElement sde;
    	dos.writeInt(numSlideDataElements);
   		dos.writeBoolean(sd.isCGHData());
    	   	 
    	for(int i=0; i<numSlideDataElements; i++){
   			sde = (ISlideDataElement)allSlideDataElements.get(i);
    		
    		String uid = sde.getUID();
    		char[] temp = uid.toCharArray();
    		dos.writeInt(temp.length);
    		for(int j=0; j<temp.length; j++){
    			dos.writeChar(temp[j]);
    		}
		
    		int rowsize = sde.getRows().length;
    		dos.writeInt(rowsize);
    		for(int j=0; j<rowsize; j++){
    			dos.writeInt(sde.getRows()[j]);
    		}
    		int colsize = sde.getColumns().length;
    		dos.writeInt(colsize);
    		for(int j=0; j<colsize; j++){
    			dos.writeInt(sde.getColumns()[j]);
    		}
		
    		int numFields = sde.getExtraFields().length;
    		dos.writeInt(numFields);
    		for(int j=0; j<numFields; j++){
    			try {
	    			temp = sde.getExtraFields()[j].toCharArray();
	        		dos.writeInt(temp.length);
	        		for(int k=0; k<temp.length; k++){
	        			dos.writeChar(temp[k]);
	        		}
    			} catch (NullPointerException npe){
    				dos.writeInt(0);
    			}
    		}

    		dos.writeBoolean(sde.getIsNull());
    		dos.writeBoolean(sde.isNonZero());
    		int dataType = sd.getDataType();
			if(dataType == IData.DATA_TYPE_TWO_INTENSITY || dataType == IData.DATA_TYPE_RATIO_ONLY){
				
			} else {		//IData has affy data
				dos.writeChar(((AffySlideDataElement)sde).getDetection().charAt(0));
				dos.writeFloat(((AffySlideDataElement)sde).getPvalue());
				dos.writeInt(((AffySlideDataElement)sde).getGenePixFlags());
				
			}
    	}
	}



	/**
	 * @param outputFile
	 */
    public static void writeSlideDataIntensities(DataOutputStream dos, SlideData sd) throws IOException {
	    	ISlideDataElement sde;
	    	Vector allSlideDataElements = sd.getAllElements();
	    	int numSlideDataElements = sd.size();
	    	dos.writeInt(numSlideDataElements);
	    	for(int i=0; i<numSlideDataElements; i++){
	    		sde = (ISlideDataElement)allSlideDataElements.get(i);
	    		dos.writeFloat(sde.getIntensity(0));
	    		dos.writeFloat(sde.getIntensity(1));
	    		dos.writeFloat(sde.getTrueIntensity(0));
	    		dos.writeFloat(sde.getTrueIntensity(1));	
	    		if(sd.getDataType() != IData.DATA_TYPE_TWO_INTENSITY && sd.getDataType() != IData.DATA_TYPE_RATIO_ONLY){
	    			dos.writeChar(sde.getDetection().toCharArray()[0]);
	    		} 
	    	}
    }

	/**
	 * @param dos
	 * @param fsd
	 */
	public static void writeFloatSlideDataIntensities(DataOutputStream dos, FloatSlideData fsd) throws IOException {
		
		float[] currentCY3 = fsd.getCurrentCY3();
    	if(currentCY3 != null){
    		dos.writeInt(currentCY3.length);
    		for(int i=0; i<currentCY3.length; i++){
    			dos.writeFloat(currentCY3[i]);
    		}
    	} else 
    		dos.writeInt(0);

    	float[] currentCY5 = fsd.getCurrentCY5();
    	if(currentCY5 != null){
	    	dos.writeInt(currentCY5.length);
	    	for(int i=0; i<currentCY5.length; i++){
	    		dos.writeFloat(currentCY5[i]);
	    	}
    	} else
    		dos.writeInt(0);
    	
    	float[] trueCY3 = fsd.getTrueCY3();
    	dos.writeInt(trueCY3.length);
    	for(int i=0; i<trueCY3.length; i++){
    		dos.writeFloat(trueCY3[i]);
    	}
    	
    	float[] trueCY5 = fsd.getTrueCY5();
    	dos.writeInt(trueCY5.length);
    	for(int i=0; i<trueCY5.length; i++){
    		dos.writeFloat(trueCY5[i]);
    	}
    	
    	char[] detection = fsd.getDetection();
    	dos.writeInt(detection.length);
    	for(int i=0; i<detection.length; i++){
    		dos.writeChar(detection[i]);
    	}
    	
    	float[] pvalues = fsd.getPvalue();
    	dos.writeInt(pvalues.length);
    	for(int i=0; i<pvalues.length; i++){
    		dos.writeFloat(pvalues[i]);
    	}
    	
		int[] flags = fsd.getGenePixFlags();
    	dos.writeInt(flags.length);
    	for(int i=0; i<flags.length; i++){
    		dos.writeInt(flags[i]);
    	}
    	
	}

	public static void writeBufferedImage(DataOutputStream dos, BufferedImage bi) throws IOException {
		try{
			ImageIO.write(bi, "jpg", dos);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public static BufferedImageWrapper readBufferedImage(String inputFile) throws IOException {
		File binFile = new File(MultipleArrayViewer.CURRENT_TEMP_DIR, inputFile);
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(binFile)));
		BufferedImage bi = ImageIO.read(binFile);
		dis.close();
		return new BufferedImageWrapper(bi);
	}


	public static void writeSlideDataIAnnotation(PrintWriter pw,
		SlideData sd) throws Exception {
		String iAnnotationFileName=null;
		if(sd.getAllElements().get(0) instanceof AffySlideDataElement) {
			AnnotationStateSavingParser asp=new AnnotationStateSavingParser();
			Vector <IAnnotation>annotationVector=new Vector<IAnnotation>();
			Vector allslideData=sd.getAllElements();

			for(int i=0; i<allslideData.size(); i++) {
				//We need to create a function getIAnnotation in ISlideDataElement/AffySlideDataElement
				ISlideDataElement sde=(ISlideDataElement)allslideData.get(i);
				IAnnotation annot;
				if( ( annot=sde.getElementAnnotation())!=null) {
					annotationVector.add(annot);
				}
			}
			AnnotationStateSavingParser fileParser = new AnnotationStateSavingParser();
			if(annotationVector.size()>1)
			fileParser.writeAnnotationFile(annotationVector, pw);
		}
	}
	
	//public static Vector<IAnnotation> loadSlideDataIAnnotation(FileReader fr) throws Exception{
	public static Vector<IAnnotation> loadSlideDataIAnnotation(File file) throws Exception{
		Vector<IAnnotation> allAnnotations = new Vector<IAnnotation>();
		AnnotationStateSavingParser fileParser = new AnnotationStateSavingParser();
		
		
		allAnnotations=fileParser.readSavedAnnotation(file);
		
		return allAnnotations;
	}
}
