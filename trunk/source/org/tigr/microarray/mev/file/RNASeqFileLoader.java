/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RNASeqFileLoader.java,v $
 * $Revision: 1.17 $
 * $Date: 2008-01-16 22:45:31 $
 * $Author: Raktim $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.IRNASeqSlide;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.RNASeqChipAnnotation;
import org.tigr.microarray.mev.RNASeqElement;
import org.tigr.microarray.mev.RNASeqFloatSlideData;
import org.tigr.microarray.mev.RNASeqSlideData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AffymetrixAnnotationParser;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.ChipAnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.annotation.RnaseqAnnotationParser;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;
import org.tigr.microarray.util.FileLoaderUtility;

public class RNASeqFileLoader extends ExpressionFileLoader {

	/**
	 * @author raktim
	 */
	private static final long serialVersionUID = 1L;
	private GBA gba;
	// TODO
	// In IData ??
	private int dataType = IData.DATA_TYPE_RNASEQ;
	//private int dataType = IData.DATA_TYPE_AFFY_ABS;

	private boolean loadEnabled = false;
	private boolean stop = false;
	private RnaSeqFileLoaderPanel sflp;
	ExpressionFileTableCellRenderer myCellRenderer;
	protected String[] moreFields = new String[] {};

	private IRNASeqSlide[] slideDataArray = null;
	private ISlideDataElement sde;

	private Hashtable<String, ArrayList<MevAnnotation>> _tempAnno = new Hashtable<String, ArrayList<MevAnnotation>>();
	private MultipleArrayViewer mav;

	// DATA Columns expected in File
	private final int UID = 0;
	private final int LOCUS = 1;
	private final int REF_ID = 2;
	private final int STATUS = 3; // Class code or transcript status (known novel etc)
	private final int TRANSCRIPT_LEN = 4;
	private final int DATA_START = 5;

	// Data Format loading options
	private final int UNDEF = 0;
	private final int RPKM = 1;
	private final int COUNT = 2;
	private final int RPKM_AND_COUNT = 3;
	private final int FPKM_AND_COUNT = 4;

	// dataType Struct for ComboBox
	private String[] dataTypes = new String[5];


	public void setFilePath(String path) {
		sflp.setFileName(path);
		processStanfordFile(new File(path));
	}

	public RNASeqFileLoader(SuperExpressionFileLoader superLoader) {
		super(superLoader);
		this.mav = superLoader.getArrayViewer();

		// Set Up dataType loading options
		this.dataTypes[this.UNDEF] = "Select";
		this.dataTypes[this.RPKM] = "RPKM";
		this.dataTypes[this.COUNT] = "Count";
		this.dataTypes[this.RPKM_AND_COUNT] = "RPKM & Count";
		this.dataTypes[this.FPKM_AND_COUNT] = "FPKM & Count";

		gba = new GBA();
		sflp = new RnaSeqFileLoaderPanel();
	}

	public Vector<ISlideData> loadStanfordExpressionFile(File f) throws IOException {
		final int selectedPreSpotRows = this.sflp.getXRow()+1;
		final int selectedPreExperimentColumns = this.sflp.getXColumn();         
		return loadStanfordExpressionFile(f, selectedPreSpotRows, selectedPreExperimentColumns);
	}

	public Vector<ISlideData> loadExpressionFiles() throws IOException {
		if (this.sflp.getXColumn()==0){
			JOptionPane.showMessageDialog(null, 
					"The selected file has no gene annotation and cannot be loaded. " +
					"\n" +
					"\nPlease make sure you have selected an EXPRESSION value in the file loader table." +
					"\nThe 1st column cannot contain expression values.", 
					"Missing Annotation Error", JOptionPane.ERROR_MESSAGE, null);
			return null;
		}

		return loadStanfordExpressionFile(new File(this.sflp.fileNameTextField.getText()), 
				this.sflp.getXRow()+1,
				this.sflp.getXColumn());
	}

	public ISlideData loadExpressionFile(File f) {
		return null;
	}
	public boolean canAutoLoad(File f) {return true;}

	/*
	 *  Handling of Stanford data has been altered in version 3.0 to permit loading of
	 *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
	 *  are used to hold the input value.
	 *
	 *  getRatio methods are altered to return the value (held in cy5) rather than
	 *  taking log2(cy5/cy3).
	 */


	public Vector<ISlideData> loadStanfordExpressionFile(File f, int rowcoord, int colcoord) throws IOException {
		int preSpotRows, preExperimentColumns;
		preSpotRows = rowcoord;
		preExperimentColumns = colcoord; 
		//System.out.println("preExperimentColumns: " + preExperimentColumns);
		int numLines = this.getCountOfLines(f);

		int spotCount = numLines - preSpotRows;

		if (spotCount <= 0) {
			JOptionPane.showMessageDialog(
					this,  
					"There is no spot data available.",  
					"TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		int[] rows = new int[] { 0, 1, 0 };
		int[] columns = new int[] { 0, 1, 0 };
		String value;
		float cy3, cy5;
		int count;
		moreFields = new String[preExperimentColumns];

		final int rColumns = 1;
		final int rRows = spotCount;

		// get the dataFormat in the file
		int dataFormat = sflp.getSelectedDataFormat();
		try {
			validateDataFormat(f, dataFormat, preExperimentColumns, preSpotRows);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					this,  
					e1.getMessage() +
					"\nInvalid File format for data type " + dataFormat,  
					"Bad Format Error", JOptionPane.INFORMATION_MESSAGE);
			//e1.printStackTrace();
			return null;
		}

		//System.out.println("Selected Data Format: " + dataTypes[dataFormat]);

		int notFoundCtr = 0;
		// Load library size file, if present
		Hashtable<String, Integer> libSizeTable = null;
		if (sflp.libSizeIsNeeded()) {
			libSizeTable = loadLibSize(sflp.getLibrarySizeFile());
			if (libSizeTable == null) {
				JOptionPane.showMessageDialog(
						null,  
						"Library File not selected or does not exist.",  
						"Library File Error", JOptionPane.INFORMATION_MESSAGE);
				return null;
			}
		}
		else {
			libSizeTable = calculateLibSize(dataFormat, f, preExperimentColumns, preSpotRows);
			//System.out.println("libSizeTable not NULL: ");
			//System.out.println("libSizeTable: " + libSizeTable.entrySet());
		}

		//TMEV.pause();
		// This is the place to load Flat File Based RefSeq Or ENSMBEL 
		// annotation into _tempAnno and chipAnno
		RnaseqAnnotationParser rnaseqAnnoParser ;
		// User annotation
		if(sflp.annoCheck.isSelected()) {
			rnaseqAnnoParser = RnaseqAnnotationParser.createAnnotationFileParser(
					new File(getAnnotationFile()));
		} 
		// MeV Annotation
		else {
			rnaseqAnnoParser = RnaseqAnnotationParser.createAnnotationFileParser(
					new File(getPathFromSpeciesAndGenome()));
		}
		_tempAnno = rnaseqAnnoParser.getAnnotation();
		//System.out.println("_tempAnno Size: "+ _tempAnno.size());
		//System.out.println(_tempAnno.entrySet());
		//TMEV.pause();

		if(sflp.annoCheck.isSelected()) {
			chipAnno.setChipName("-" );
			chipAnno.setChipType("-");
			// TODO correctly
			chipAnno.setDataType("RNASeq");
			chipAnno.setGenomeBuild("-");
			chipAnno.setSpeciesName("-");
		} else {
			chipAnno.setChipName(sflp.getGenome() + "_" + sflp.getSpecies());
			chipAnno.setChipType(sflp.getGenome() + "_" + sflp.getSpecies());
			// TODO correctly
			chipAnno.setDataType("RNASeq");
			chipAnno.setGenomeBuild(sflp.getBuild());
			chipAnno.setSpeciesName(sflp.getSpecies());
		}

		// Unused at this time
		((RNASeqChipAnnotation)chipAnno).setReadLength(100);

		this.mav.getData().setAnnotationLoaded(true);


		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringSplitter ss = new StringSplitter((char) 0x09);
		String currentLine;
		int counter, row, column;
		counter = 0;
		row = column = 1;
		this.setFilesCount(1);
		this.setRemain(1);
		this.setFilesProgress(0);
		this.setLinesCount(numLines);
		this.setFileProgress(0);

		while ((currentLine = reader.readLine()) != null) {
			try {
				if (stop) {
					return null;
				}
				while (currentLine.endsWith("\t")) {
					currentLine = currentLine.substring(0, currentLine.length() - 1);
				}

				ss.init(currentLine);
				if (counter == 0) { // parse header
					// figure out if its both exp and count or just one.
					// data column cnt would depend on that
					int experimentCount;
					if (dataFormat == RPKM_AND_COUNT || 
							dataFormat == FPKM_AND_COUNT)
						experimentCount = (ss.countTokens() + 1 - preExperimentColumns)/2;
					else
						experimentCount = ss.countTokens() + 1 - preExperimentColumns;

					SampleAnnotation sampAnn=new SampleAnnotation();
					// AS many samples
					slideDataArray = new IRNASeqSlide[experimentCount];
					slideDataArray[0] = new RNASeqSlideData(rRows, rColumns, sampAnn);
					slideDataArray[0].setSlideFileName(f.getPath());
					for (int i = 1; i < slideDataArray.length; i++) {
						sampAnn=new SampleAnnotation();
						//TODO
						// Check for the cng to RNAseq specific file
						//slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount, sampAnn);
						slideDataArray[i] = new RNASeqFloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount, sampAnn);
						slideDataArray[i].setSlideFileName(f.getPath());
					}
					//get Field Names
					String[] fieldNames = new String[preExperimentColumns];
					for (int i = 0; i < preExperimentColumns; i++) {
						fieldNames[i] = ss.nextToken();
					}
					slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);

					for (int i = 0; i < experimentCount; i++) {
						//This is where the "Default Slide Name" gets set in the SampleAnnotation Model

						String val=ss.nextToken();
						slideDataArray[i].setSampleAnnotationLoaded(true);
						slideDataArray[i].getSampleAnnotation().setAnnotation("Default Slide Name", val);
						slideDataArray[i].setSlideDataName(val);

						// skip over next token, if req
						if (dataFormat == RPKM_AND_COUNT || dataFormat == FPKM_AND_COUNT)
							ss.nextToken();

						this.mav.getData().setSampleAnnotationLoaded(true);
					}
				} else if (counter >= preSpotRows) { // data rows
					rows[0] = rows[2] = row;
					columns[0] = columns[2] = column;
					if (column == rColumns) {
						column = 1;
						row = row + 1;
					} else {
						column++;
					}
					for (int i = 0; i < preExperimentColumns; i++) {
						moreFields[i] = ss.nextToken();
					}

					// nearest_ref_id is the key not the clone name 
					String cloneName = moreFields[this.REF_ID]; //moreFields[0];

					//System.out.println("Looking annotation for Ref: " + cloneName);

					Vector<String> locusInfo = parserLocus(moreFields[this.LOCUS]);

					MevAnnotation mevAnno = null;
					if(_tempAnno.size() != 0) {
						ArrayList<MevAnnotation> annoObjList = (ArrayList<MevAnnotation>)_tempAnno.get(cloneName);
						if (annoObjList == null) {
							//System.out.println("Anno not found for: " + cloneName);
							notFoundCtr++;
							//continue; // No matching anno found skipping record
							mevAnno = new MevAnnotation();

							//Field[] temp = AnnotationFieldConstants.class.getFields();
							//System.out.println("# of Fields " + temp.length);
							//System.out.println("# of allSlideDataElements.size() " + allSlideDataElements.size());
							//for(int i=0; i<temp.length; i++) {
							//String thisFieldName = temp[i].getName();
							//mevAnno.setCloneID("");
							//}						
						}
						else {
							// Get the closest match of the record
							//for (int i=0; i < annoObjList.size(); i++) {
							//System.out.println("Anno search list size: " + annoObjList.size());
							MevAnnotation _tmpA = (MevAnnotation) annoObjList.get(0).clone();
							try {
								//System.out.println(_tmpA.getCloneID() + ":" + _tmpA.getProbeChromosome() + ":" + _tmpA.getProbeTxStartBP() + ":" + _tmpA.getGeneSymbol());
								//System.out.println(_tmpA.getCloneID());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Change to find a exact match 
							mevAnno = _tmpA;
							mevAnno.setCloneID(moreFields[this.UID]);
							//}
						}

						mevAnno.setCloneID(moreFields[this.UID]);
						mevAnno.setProbeChromosome(locusInfo.get(0));
						mevAnno.setProbeTxStartBP(locusInfo.get(1));
						mevAnno.setProbeTxEndBP(locusInfo.get(2));
						int givenLen = Integer.parseInt(locusInfo.get(2)) - Integer.parseInt(locusInfo.get(1));
						/*
						System.out.println(
								"Chr " + locusInfo.get(0) + 
								"\n St: " + locusInfo.get(1) + 
								"\nEnd: " + locusInfo.get(2) +
								"\n Len " + givenLen);
						 */
					}

					//TODO
					// Check for the cng to RNAseq specific file
					//sde = new SlideDataElement(String.valueOf(row + 1), rows, columns, new float[2], moreFields, mevAnno);
					sde = new RNASeqElement(String.valueOf(row + 1), rows, columns, new float[2], moreFields, mevAnno);

					// set class code/ status
					((RNASeqElement)sde).setClasscode(moreFields[this.STATUS]);

					//transcript length
					int len = 0;
					try {
						len = mevAnno.getProbeTxLengthInBP();
					} catch (Exception c) {
						if (mevAnno == null)
							System.out.println("mevAnno is NULL");
						return null;
						//TMEV.pause();
					}
					//System.out.println("Trns Len: " + moreFields[this.TRANSCRIPT_LEN] + "," + len);
					if(moreFields[this.TRANSCRIPT_LEN].trim().length() == 0){
						//System.out.println("Len not avaialble, using: " + len);
						((RNASeqElement)sde).setTranscriptLength(len);
					} else {
						try {
							//System.out.println("Len avaialble: " + moreFields[this.TRANSCRIPT_LEN]);
							((RNASeqElement)sde).setTranscriptLength(Integer.parseInt(moreFields[this.TRANSCRIPT_LEN]));
						} catch (NumberFormatException e) {
							//System.out.println("Len in bad format, using: " + len);
							((RNASeqElement)sde).setTranscriptLength(len);
						}
					}

					//}

					slideDataArray[0].addSlideDataElement(sde);
					//System.out.println("Clone Added " + mevAnno.getAttribute("PROBE_ID")[0]);

					for (int i = 0; i < slideDataArray.length; i++) {
						cy3 = 1f; //set cy3 to a default value of 1.
						cy5 = 1f; // will be overwritten in the switch{}
						//cy5 to hold the exp value
						//getRatio methods will return cy5 for Stanford data type
						count = 0; // will be overwritten in the switch{} or later
						// get the sample name
						String sample = slideDataArray[i].getSlideDataName();
						//System.out.println("SlideDataArray Smaple Name:" + sample);
						// set the lib here as well
						int libSize = libSizeTable.get(sample).intValue();
						slideDataArray[i].setLibrarySize(libSize);
						try {
							value = ss.nextToken();

							switch(dataFormat) {
							case UNDEF:
								//TODO
								// code for bailing out
								return null;
							case RPKM:
								// req lib size file
								// calculate cnt data 
								cy5 = Float.parseFloat(value); 
								if(cy5 == 0.0f || cy5 == 0) 
									count = 0;
								else
									/**
									 ** Mortazavi Transformation **
									RPKM = (10^9 * C)/N*L Where,
									C = # 0f reads mapped to exon(s)
									N = Total # of reads in the experiment (lib size)
									L = Sum of the exons in BP (Transcript length)
									 **/
								{
									len = ((RNASeqElement)sde).getTranscriptLength();
									count = (int)((cy5*libSize*len)/(Math.pow(10, 9)));
								}
								break;
							case COUNT:
								// if present load lib file
								// calculate RPKM data
								// Scale by 1 to avoid Inf or Div/0
								count = Integer.parseInt(value);//+1;
								if(count == 0) cy5 = 0.0f;
								else
									/**
									 ** Mortazavi Transformation **
									RPKM = (10^9 * C)/N*L Where,
									C = # 0f reads mapped to exon(s)
									N = Total # of reads in the experiment (lib size)
									L = Sum of the exons in BP (Transcript length)
									 **/
								{
									len = ((RNASeqElement)sde).getTranscriptLength();
									cy5 = (float)(Math.pow(10,9)*(float)count)/((float)libSize*(float)len);
								}
								break;
							case RPKM_AND_COUNT:
								cy5 = Float.parseFloat(value); 
								count = Integer.parseInt(ss.nextToken());
								break;
							case FPKM_AND_COUNT:
								cy5 = Float.parseFloat(value); 
								count = Integer.parseInt(ss.nextToken());
								break;
							}
						} catch (Exception e) {
							cy3 = 0;
							cy5 = 0.0f;
							count = 0;
						}
						slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
						//EH count value for this gene and sample
						slideDataArray[i].setCount(counter - preSpotRows, count);
						//slideDataArray[i].setLibrarySize(9);
					}
				} else {
					//we have additional sample annotation. 
					//Add the additional sample annotation to the SampleAnnotation object

					//advance to sample key
					for (int i = 0; i < preExperimentColumns - 1; i++) {
						ss.nextToken();
					}
					String key = ss.nextToken();

					for (int j = 0; j < slideDataArray.length; j++) {

						if(slideDataArray[j].getSampleAnnotation()!=null){

							String val=ss.nextToken();
							slideDataArray[j].getSampleAnnotation().setAnnotation(key, val);

						}else{
							SampleAnnotation sampAnn=new SampleAnnotation();
							sampAnn.setAnnotation(key, ss.nextToken());
							slideDataArray[j].setSampleAnnotation(sampAnn);
							slideDataArray[j].setSampleAnnotationLoaded(true);
						}
					}
				}

				this.setFileProgress(counter);
				counter++;
			} catch (NoSuchElementException nsee) {
				//Blank or corrupted line. Ignore.
				//System.out.println("caught a blank line");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		reader.close();

		Vector<ISlideData> data = new Vector<ISlideData>(slideDataArray.length);

		for (int i = 0; i < slideDataArray.length; i++)
			data.add(slideDataArray[i]);

		this.setFilesProgress(1);
		//System.out.println("Data Loaded, #Recs: " + counter);
		//System.out.println("Data Loaded, #Recs without anno: " + notFoundCtr);

		// clean up temp hastable
		_tempAnno.clear();
		_tempAnno = null;

		//TMEV.pause();
		return data;
	}

	/**
	 * Function to validate if selected format type matches wit h data file used.
	 * 
	 * @param f
	 * @param dataFormat
	 * @param preExperimentColumns
	 * @param preSpotRows
	 * @throws Exception
	 */
	private void validateDataFormat(File f, int dataFormat, int preExperimentColumns, int preSpotRows) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringSplitter ss = new StringSplitter((char) 0x09);
		String currentLine, lastSampleName, curSampleName;
		String[] sampleNames = null;
		int counter;
		int experimentCount = 0;
		counter = 0;

		while ((currentLine = reader.readLine()) != null) {
			while (currentLine.endsWith("\t")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}
			//System.out.println("currentlIne: " + currentLine);
			ss.init(currentLine);
			if (counter == 0) { // parse header
				// figure out if its both exp and count or just one.
				// data column cnt would depend on that
				//System.out.println("Parsing Header");
				if (dataFormat == RPKM_AND_COUNT || dataFormat == FPKM_AND_COUNT)
					experimentCount = (ss.countTokens() + 1 - preExperimentColumns)/2;
				else
					experimentCount = ss.countTokens() + 1 - preExperimentColumns;

				//skip over Field Names
				for (int i = 0; i < preExperimentColumns; i++) {
					ss.nextToken();
				}

				//System.out.println("Sample Cnt : " + experimentCount);
				sampleNames = new String[experimentCount];
				for (int i = 0; i < experimentCount; i++) {
					//This is where the "Default Slide Name" gets set in the SampleAnnotation Model

					// skip over next token (exp col), if req
					if (dataFormat == RPKM_AND_COUNT || dataFormat == FPKM_AND_COUNT){
						lastSampleName = ss.nextToken();
						curSampleName = ss.nextToken();
						if (!lastSampleName.equals(curSampleName))
							throw new Exception("Sample Names should match for adjacent Samples for this format." +
									lastSampleName + ", " + curSampleName);
					} else
						curSampleName=ss.nextToken();

					sampleNames[i] = curSampleName;
					//System.out.println("Added sample to tab and List: " + val);
				}
				//System.out.println(sampleNames);
			} else if (counter >= preSpotRows) { // data rows
				//System.out.println("Parsing Data");
				for (int i = 0; i < preExperimentColumns; i++) {
					ss.nextToken();
				}

				Integer count;
				for (int i = 0; i < experimentCount; i++) {
					try {
						switch(dataFormat) {
						case UNDEF:
							throw new Exception("Select valid format");
						case RPKM:
							break;
						case COUNT:
							// if present load lib file
							// calculate RPKM data
							String tmp = ss.nextToken();
							try {
								Integer.parseInt(tmp);
							} catch (NumberFormatException nfe){
								throw new Exception("Invalid data for COUNT format - " + tmp);
							}
							break;
						case RPKM_AND_COUNT:
							String tmp2;
							tmp = ss.nextToken();
							tmp2 = ss.nextToken();
							try {
								Float.parseFloat(tmp);
								Integer.parseInt(tmp2);
							} catch (NumberFormatException nfe) {
								throw new Exception("Invalid data for RPKM AND COUNT format - " + tmp + " " + tmp2);
							}

							break;
						case FPKM_AND_COUNT:
							tmp = ss.nextToken();
							tmp2 = ss.nextToken();
							try {
								Float.parseFloat(tmp);
								Integer.parseInt(tmp2);
							} catch (NumberFormatException nfe) {
								throw new Exception("Invalid data for RPKM AND COUNT format - " + tmp + " " + tmp2);
							}

							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
				}
			} else {
				//we have additional sample annotation. 
				//Add the additional sample annotation to the SampleAnnotation object
				//System.out.println("Parsing Void");
				//just step thru
			}
			counter++;
			// Just check first 100 lines
			if(counter > 1000)
				break;
		}
		reader.close();
	}

	/**
	 * Given a file of read counts it calculates the libsize as the sum of counts for a sample.
	 * 
	 * @param dataFormat
	 * @param f
	 * @param preExperimentColumns
	 * @param preSpotRows
	 * @return
	 */
	private Hashtable<String, Integer> calculateLibSize(int dataFormat, File f, int preExperimentColumns, int preSpotRows) {

		int counter = 0;
		int experimentCount = 0;
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		String[] sampleNames = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringSplitter ss = new StringSplitter((char) 0x09);
			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				while (currentLine.endsWith("\t")) {
					currentLine = currentLine.substring(0, currentLine.length() - 1);
				}
				//System.out.println("currentlIne: " + currentLine);
				ss.init(currentLine);
				if (counter == 0) { // parse header
					// figure out if its both exp and count or just one.
					// data column cnt would depend on that
					//System.out.println("Parsing Header");
					if (dataFormat == RPKM_AND_COUNT || dataFormat == FPKM_AND_COUNT)
						experimentCount = (ss.countTokens() + 1 - preExperimentColumns)/2;
					else
						experimentCount = ss.countTokens() + 1 - preExperimentColumns;

					//skip over Field Names
					for (int i = 0; i < preExperimentColumns; i++) {
						ss.nextToken();
					}

					//System.out.println("Sample Cnt : " + experimentCount);
					sampleNames = new String[experimentCount];
					for (int i = 0; i < experimentCount; i++) {
						//This is where the "Default Slide Name" gets set in the SampleAnnotation Model

						// skip over next token (exp col), if req
						if (dataFormat == RPKM_AND_COUNT || dataFormat == FPKM_AND_COUNT)
							ss.nextToken();

						String val=ss.nextToken();
						table.put(val, new Integer(0));
						sampleNames[i] = val;
						//System.out.println("Added sample to tab and List: " + val);
					}
					//System.out.println(sampleNames);
				} else if (counter >= preSpotRows) { // data rows
					//System.out.println("Parsing Data");
					for (int i = 0; i < preExperimentColumns; i++) {
						ss.nextToken();
					}

					Integer count;
					for (int i = 0; i < experimentCount; i++) {
						try {
							switch(dataFormat) {
							case UNDEF:
							case RPKM:
								break;
							case COUNT:
								// if present load lib file
								// calculate RPKM data
								count = new Integer(ss.nextToken());
								count = count + table.get(sampleNames[i]);
								table.put(sampleNames[i], count);
								break;
							case RPKM_AND_COUNT:
								ss.nextToken();
								count = new Integer(ss.nextToken());
								count = count + table.get(sampleNames[i]);
								table.put(sampleNames[i], count);
								break;
							case FPKM_AND_COUNT:
								ss.nextToken();
								count = new Integer(ss.nextToken());
								count = count + table.get(sampleNames[i]);
								table.put(sampleNames[i], count);
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
							count = new Integer(0);
						}
					}
				} else {
					//we have additional sample annotation. 
					//Add the additional sample annotation to the SampleAnnotation object
					//System.out.println("Parsing Void");
					//just step thru
				}
				counter++;
			} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Lines parsed" + counter);
		return table;
	}

	/**
	 * Expected file format:
	 * Sample_Name \t Count
	 * @param librarySizeFile
	 * @return
	 */
	private Hashtable<String, Integer> loadLibSize(String librarySizeFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(librarySizeFile));
			String currentLine;

			Hashtable<String, Integer> table = new Hashtable<String, Integer>();
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("#"))
					continue;
				String tmp[] = currentLine.split("\t");
				table.put(tmp[0], new Integer(tmp[1]));
			}
			reader.close();
			return table;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(librarySizeFile + " Size Lib File does not exist.");
			JOptionPane.showMessageDialog(this,  
					librarySizeFile + " File Not Found", 
					"Library File Error.",  JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this,  
					librarySizeFile + " File Error Reading", 
					"Library File Error.", JOptionPane.ERROR);
			return null;
		}
	}

	private Vector<String> parserLocus(String str) {
		Vector<String> temp = new Vector<String>(3); //chr/st/end
		String t [] = str.split(":");
		String chr = t[0].substring(3);
		t = t[1].split("-");
		temp.add(chr);
		temp.add(t[0]);
		temp.add(t[1]);
		return temp;
	}

	private String getPathFromSpeciesAndGenome() {
		String path = "data/rnaseq/";
		if(sflp.getSpecies().equals("Human"))
			path += "hg/";
		else if(sflp.getSpecies().equals("Mouse"))
			path += "mm/";
		else
			path += "null/";

		path += sflp.getBuild().toLowerCase() + "/" + sflp.getGenome().toLowerCase() + "/";

		String filename = sflp.getGenome().toLowerCase().substring(0, 3) + 
		"_gene_" +
		sflp.getBuild().toLowerCase() +
		".txt";
		//System.out.println("Anni File: " + path + filename);
		//TMEV.pause();
		return path + filename;
	}

	private String getAnnotationFile () {
		return sflp.annoTextField.getText();
	}

	public FileFilter getFileFilter() {

		FileFilter mevFileFilter = new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				if (f.getName().endsWith(".txt")) return true;
				else return false;
			}

			public String getDescription() {
				return "Tab Delimited, Multiple Sample Files (TDMS) (*.txt)";
			}
		};

		return mevFileFilter;
	}

	public void markLoadEnabled(boolean state) {
		loadEnabled = state;
		checkLoadEnable();
	}

	public boolean checkLoadEnable() {

		// Currently, the only requirement is that a cell has been highlighted
		//System.out.println("RNASeq Loader checkLoadEnable()");
		int tableRow = sflp.getXRow() + 1; // Adjusted by 1 to account for the table header
		int tableColumn = sflp.getXColumn();

		if (tableColumn < 0) return false;

		TableModel model = sflp.getTable().getModel();
		String fieldSummary = "";
		for (int i = 0; i < tableColumn; i++) {
			//  System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
			fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
		}

		if (tableRow >= 1 && tableColumn >= 0) {
			setLoadEnabled(true);
			return true;
		} else {
			setLoadEnabled(false);
			return false;
		}
	}

	public boolean validateFile(File targetFile) {
		return true; // For now, no validation on Stanford Files
	}

	public JPanel getFileLoaderPanel() {
		return sflp;
	}

	public void processLibSizeFile (String fileName) {
		sflp.setLibSizeFileName(fileName);
	}
	public void processStanfordFile(File targetFile) {
		if (! validateFile(targetFile)) return;
		//TODO

		Vector<String> columnHeaders = new Vector<String>();
		Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
		Vector<String> rowVector = null;
		BufferedReader reader = null;
		String currentLine = null;



		sflp.setFileName(targetFile.getAbsolutePath());

		DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		try {
			reader = new BufferedReader(new FileReader(targetFile), 1024 * 128);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}

		try {
			StringSplitter ss = new StringSplitter('\t');

			currentLine = reader.readLine();
			//fix empty tabbs appending to the end of line by wwang
			while (currentLine.endsWith("\t")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}
			ss.init(currentLine);

			for (int i = 0; i < ss.countTokens() + 1; i++) {
				columnHeaders.add(ss.nextToken());
			}

			model.setColumnIdentifiers(columnHeaders);

			int cnt = 0;
			while ((currentLine = reader.readLine()) != null && cnt < 100) {
				cnt++;
				ss.init(currentLine);
				rowVector = new Vector<String>();
				for (int i = 0; i < ss.countTokens() + 1; i++) {
					try {
						rowVector.add(ss.nextToken());
					} catch (java.util.NoSuchElementException nsee) {
						rowVector.add(" ");
					}
				}

				dataVector.add(rowVector);
				model.addRow(rowVector);
			}

			reader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		sflp.setTableModel(model);
		Point p = getFirstExpressionCell(dataVector);
		sflp.setSelectedCell(p.x, p.y);
	}

	public String getFilePath() {
		return this.sflp.fileNameTextField.getText();
	}

	public String getAnnotationFilePath() {
		return this.sflp.getAnnFilePath();
	}

	public void openDataPath() {
	}


	public void setDataType(int data_Type) {
		if (data_Type != -1)
			dataType = data_Type;
		else
			dataType = IData.DATA_TYPE_RATIO_ONLY;
	}

	public int getDataType() {
		return dataType;
	}
	public boolean isAnnotationSelected() {
		//return sflp.adh.annotationSelected;
		return false;
	}


	@SuppressWarnings("serial")
	private class RnaSeqFileLoaderPanel extends JPanel {
		String species [] = {"Select", "Human", "Mouse"};
		String refGenome [] = {"Select", "RefSeq", "ENSEMBL"};
		String hgGenomeBlds [] = {"Select", "hg19", "hg18"};
		String mmGenomeBlds [] = {"Select", "mm9", "mm8"};

		JLabel dataTypeLabel, speciesLabel, genomeLabel, bldLabel, readLen;
		JTextField readLenBox;

		JComboBox dataTypeCombo, speciesCombo, genomeCombo, bldCombo;

		// If count data is loaded libsize is optional
		// if R/FPKM data is loaded libsize is needed
		boolean needSampleLibSize = false;
		// this should be true only for FPKM data where both FPKM and Count data is required
		boolean needCountAndExp = false;
		JPanel dataAttributesPanel;

		JTextField fileNameTextField;

		JTextField libSizeFileTextField, fieldsTextField, annoTextField;
		JTable expressionTable;
		JLabel instructionsLabel;
		JScrollPane tableScrollPane;
		JPanel tablePanel;
		JPanel fileLoaderPanel;
		//JPanel fieldsPanel;
		JSplitPane splitPane;

		JList availableList;
		JScrollPane availableScrollPane;

		private int xRow = -1;
		private int xColumn = -1;

		JPanel fileSelectionPanel;
		JLabel fileSelectionLabel, dataSelectionLabel, annoSelectionLabel;
		JButton browseButton1, browseButton2, browseButton3;
		//JPanel buttonPanel;
		//JRadioButton twoColorArray;
		//JRadioButton singleColorArray;

		JCheckBox annoCheck;

		protected EventListener eventListener;

		public RnaSeqFileLoaderPanel() {
			super();

			//adh = new AnnotationDownloadHandler(superLoader.viewer.getResourceManager(), superLoader.annotationLists, superLoader.defaultSpeciesName, superLoader.defaultArrayName);

			eventListener = new EventListener();
			setLayout(new GridBagLayout());

			dataTypeCombo = new JComboBox(dataTypes);
			dataTypeCombo.addActionListener(eventListener);

			speciesCombo = new JComboBox(species);
			speciesCombo.addActionListener(eventListener);

			genomeCombo = new JComboBox(refGenome);
			bldCombo = new JComboBox();

			readLenBox = new JTextField();
			readLen = new JLabel("Read Length ");
			readLen.setForeground(java.awt.Color.BLACK);
			dataTypeLabel = new JLabel("Data Type");
			dataTypeLabel.setForeground(java.awt.Color.RED);
			speciesLabel = new JLabel("Species");
			speciesLabel.setForeground(java.awt.Color.RED);
			genomeLabel = new JLabel("Reference Genome");
			genomeLabel.setForeground(java.awt.Color.RED);
			bldLabel = new JLabel("UCSC Build");
			bldLabel.setForeground(java.awt.Color.BLACK);

			annoCheck = new JCheckBox("Upload Annotation", false);
			annoCheck.addActionListener(eventListener);

			dataAttributesPanel = new JPanel();
			dataAttributesPanel.setLayout(new GridBagLayout());
			dataAttributesPanel.setBorder(new TitledBorder(new EtchedBorder(), "RNASeq Data Info"));

			gba.add(dataAttributesPanel, annoCheck, 		0, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);

			gba.add(dataAttributesPanel, dataTypeLabel, 	0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, speciesLabel,  	1, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, genomeLabel, 		2, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, bldLabel, 			3, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, readLen, 			4, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 0, 1, 0), 0, 0);

			gba.add(dataAttributesPanel, dataTypeCombo, 	0, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(1, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, speciesCombo,  	1, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(1, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, genomeCombo, 		2, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(1, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, bldCombo, 			3, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(1, 0, 1, 10), 0, 0);
			gba.add(dataAttributesPanel, readLenBox, 		4, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(1, 0, 1, 0), 0, 0);
			dataAttributesPanel.validate();
			
			fileNameTextField = new JTextField();
			fileNameTextField.setEditable(false);
			fileNameTextField.setForeground(Color.black);
			fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			libSizeFileTextField = new JTextField();
			libSizeFileTextField.setEditable(false);
			libSizeFileTextField.setForeground(Color.black);
			libSizeFileTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			annoTextField = new JTextField();
			annoTextField.setEditable(false);
			annoTextField.setForeground(Color.black);
			annoTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			fileSelectionLabel = new JLabel();
			fileSelectionLabel.setForeground(java.awt.Color.BLACK);
			fileSelectionLabel.setText("Library size file");

			dataSelectionLabel = new JLabel();
			dataSelectionLabel.setForeground(java.awt.Color.RED);
			dataSelectionLabel.setText("RNASeq data file");

			annoSelectionLabel = new JLabel();
			annoSelectionLabel.setForeground(java.awt.Color.BLACK);
			annoSelectionLabel.setText("Annotation data file");

			fileSelectionPanel = new JPanel();
			fileSelectionPanel.setLayout(new GridBagLayout());
			fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File(Tab Delimited Multiple Sample (*.*))"));

			browseButton1 = new JButton("Browse");
			browseButton1.addActionListener(eventListener);
			browseButton1.setSize(100, 30);
			browseButton1.setPreferredSize(new Dimension(100, 30));

			browseButton2 = new JButton("Browse");
			browseButton2.addActionListener(eventListener);
			browseButton2.setSize(100, 30);
			browseButton2.setPreferredSize(new Dimension(100, 30));

			browseButton3 = new JButton("Browse");
			browseButton3.addActionListener(eventListener);
			browseButton3.setSize(100, 30);
			browseButton3.setPreferredSize(new Dimension(100, 30));
			browseButton3.setEnabled(false);
			/*
			gba.add(fileSelectionPanel, annoSelectionLabel, 0, 4, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, annoTextField, 		1, 4, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton3, 		2, 4, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(fileSelectionPanel, dataSelectionLabel, 0, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, fileNameTextField, 	1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton1, 		2, 0, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(fileSelectionPanel, fileSelectionLabel, 	0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, libSizeFileTextField, 	1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton2, 			2, 2, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			*/
			gba.add(fileSelectionPanel, dataSelectionLabel, 0, 4, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, fileNameTextField, 		1, 4, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton1, 		2, 4, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			
			gba.add(fileSelectionPanel, annoSelectionLabel, 0, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, annoTextField, 	1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton3, 		2, 0, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(fileSelectionPanel, fileSelectionLabel, 	0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, libSizeFileTextField, 	1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton2, 			2, 2, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			expressionTable = new JTable();
			myCellRenderer = new ExpressionFileTableCellRenderer();
			expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
			expressionTable.setIntercellSpacing(new Dimension(1, 1));
			expressionTable.setShowHorizontalLines(false);
			expressionTable.setShowVerticalLines(true);
			expressionTable.setGridColor(Color.LIGHT_GRAY);
			expressionTable.setCellSelectionEnabled(true);
			expressionTable.setColumnSelectionAllowed(false);
			expressionTable.setRowSelectionAllowed(false);
			expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			expressionTable.getTableHeader().setReorderingAllowed(false);

			expressionTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
					setSelectedCell(expressionTable.rowAtPoint(event.getPoint()), expressionTable.columnAtPoint(event.getPoint()));
				}
			});

			tableScrollPane = new JScrollPane(expressionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

			instructionsLabel = new JLabel();
			instructionsLabel.setForeground(java.awt.Color.red);
			String instructions = "<html>Click the upper-leftmost expression value. Click the <b>Load</b> button to finish.</html>";
			instructionsLabel.setText(instructions);

			tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));

			gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			fileLoaderPanel = new JPanel();
			fileLoaderPanel.setLayout(new GridBagLayout());


			gba.add(fileLoaderPanel, dataAttributesPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			//gba.add(fileLoaderPanel, adh.getAnnotationLoaderPanel(gba), 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);//Uncomment when you add annotation for non affy
			gba.add(fileLoaderPanel, fileSelectionPanel, 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);//Uncomment when you add annotation for non affy
			gba.add(fileLoaderPanel, tablePanel, 0, 3, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			fileLoaderPanel.validate();

			setDataType(IData.DATA_TYPE_RNASEQ);
			//setDataType(IData.DATA_TYPE_AFFY_ABS);
		}
		public boolean isAnnotationSelected() {
			return true; //adh.annotationSelected;
		}
		public String getAnnFilePath() {
			return "no/way"; //adh.getAnnFilePath();
		}
		private void setSelectedCell(int xR, int xC) {
			xRow = xR;
			xColumn = xC;
			myCellRenderer.setSelected(xRow, xColumn);
			expressionTable.repaint();
			checkLoadEnable();
		}

		public boolean onBrowse() {
			JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			int retVal = fileChooser.showOpenDialog(RnaSeqFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				processStanfordFile(selectedFile);
				return true;
			}
			return false;
		}

		// For loading library size file into data structure
		public boolean onBrowse2() {
			JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			int retVal = fileChooser.showOpenDialog(RnaSeqFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				//File selectedFile = fileChooser.getSelectedFile();
				processLibSizeFile(fileChooser.getSelectedFile().getAbsolutePath());
				return true;
			}
			return false;
		}

		//For loading user annotation
		private boolean onBrowse3() {
			JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			int retVal = fileChooser.showOpenDialog(RnaSeqFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				try {
					if(validateAnnoFile(fileChooser.getSelectedFile().getAbsolutePath())) {
						sflp.annoTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
						return true;
					}
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(
							this,  
							"Error " + e.getMessage() + " \n occured while reading " +
							fileChooser.getSelectedFile().getAbsolutePath(), 
							"Annotation File Error.",  JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}		
			}
			return false;
		}

		public JTable getTable() {
			return expressionTable;
		}

		public int getXColumn() {
			return xColumn;
		}

		public int getXRow() {
			return xRow;
		}

		public void setFileName(String fileName) {
			fileNameTextField.setText(fileName);
			// TODO 
			// TBR Need to change the libFile Name
			// Not needed here
			//libSizeFileTextField.setText(fileName);
		}

		public void setLibSizeFileName(String fileName) {
			libSizeFileTextField.setText(fileName);
		}

		public void setTableModel(TableModel model) {
			expressionTable.setModel(model);
			int numCols = expressionTable.getColumnCount();
			for (int i = 0; i < numCols; i++) {
				expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
			}
		}

		public void setFieldsText(String fieldsText) {
			//  fieldsTextField.setText(fieldsText);
		}

		protected String getLibrarySizeFile() {
			return libSizeFileTextField.getText();
		}

		public boolean libSizeIsNeeded() {
			return needSampleLibSize;
		}

		public boolean needBothCntAndExp() {
			return needCountAndExp;
		}

		public String getSpecies() {
			return (String)speciesCombo.getSelectedItem();
		}

		public String getBuild() {
			if (((String)bldCombo.getSelectedItem()).equals("Select")){
				if (getSpecies().equals(species[1]))
					return hgGenomeBlds[1];
				else
					return mmGenomeBlds[1]; 
			}
			return (String)bldCombo.getSelectedItem();
		}

		public String getGenome() {
			return (String)genomeCombo.getSelectedItem();
		}

		private int getSelectedDataFormat() {
			ItemSelectable is = (ItemSelectable)dataTypeCombo;
			Object selected[] = is.getSelectedObjects();
			//"RPKM"
			if (((String)selected[0]).equals(dataTypes[RPKM])) {
				return RPKM;
				// "FPKM & DGE"
			} else if (((String)selected[0]).equals(dataTypes[FPKM_AND_COUNT])) {
				return FPKM_AND_COUNT;
				// "DGE Count", "RPKM & DGE"
			} else if (((String)selected[0]).equals(dataTypes[RPKM_AND_COUNT])) {
				return RPKM_AND_COUNT;
			} else if (((String)selected[0]).equals(dataTypes[COUNT])) {
				return COUNT;
			} else
				return UNDEF;
		}

		private class EventListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source == browseButton1) {
					if((sflp.getSelectedDataFormat()== UNDEF) || 
							(!sflp.annoCheck.isSelected() && 
									(sflp.getSpecies().equalsIgnoreCase("Select") || 
											sflp.getGenome().equalsIgnoreCase("Select"))))
					{
						String eMsg = "Required Fields(RED) may be not have been selected";
						JOptionPane.showMessageDialog(null,
								eMsg, "Error", JOptionPane.ERROR_MESSAGE);
						return;
					} 
					
					if(sflp.annoCheck.isSelected() && sflp.annoTextField.getText().length() < 2) {
						String eMsg = "Annotation File may not have been selected";
						JOptionPane.showMessageDialog(null,
								eMsg, "File not selected Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if (sflp.libSizeIsNeeded() && sflp.libSizeFileTextField.getText().length() < 2) {
						String eMsg = "The Data type you selected requires a Library Size File \n which may not have been selected";
						JOptionPane.showMessageDialog(null,
								eMsg, "File not selected Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					onBrowse();
				} else if (source == browseButton2) {
					onBrowse2();
				} else if (source == browseButton3) {
					onBrowse3();
				} else if (source == annoCheck) {
					if(annoCheck.isSelected()) {
						annoSelectionLabel.setForeground(java.awt.Color.RED);
						speciesLabel.setForeground(java.awt.Color.BLACK);
						speciesCombo.setEnabled(false);
						bldLabel.setForeground(java.awt.Color.BLACK);
						bldCombo.setEnabled(false);
						genomeLabel.setForeground(java.awt.Color.BLACK);
						genomeCombo.setEnabled(false);
						browseButton3.setEnabled(true);
					} else {
						annoSelectionLabel.setForeground(java.awt.Color.BLACK);
						speciesLabel.setForeground(java.awt.Color.RED);
						speciesCombo.setEnabled(true);
						bldLabel.setForeground(java.awt.Color.RED);
						bldCombo.setEnabled(true);
						genomeLabel.setForeground(java.awt.Color.RED);
						genomeCombo.setEnabled(true);
						browseButton3.setEnabled(false);
					}
				} else if (source == dataTypeCombo) {
					ItemSelectable is = (ItemSelectable)source;
					Object selected[] = is.getSelectedObjects();
					
					fileSelectionLabel.setForeground(java.awt.Color.BLACK);
					fileSelectionLabel.setText("Library size file");
					//"RPKM"
					if (((String)selected[0]).equals(dataTypes[RPKM])) {
						needSampleLibSize = true;
						needCountAndExp = false;
						fileSelectionLabel.setForeground(java.awt.Color.RED);
						//System.out.println("Data Type: RPKM");
						// "FPKM & DGE"
					} else if (((String)selected[0]).equals(dataTypes[FPKM_AND_COUNT])) {
						needSampleLibSize = false;
						needCountAndExp = true;
						//System.out.println("Data Type: FPKM & DGE");
						// "DGE Count", "RPKM & DGE"
					} else { 
						needSampleLibSize = false;
						needCountAndExp = false;
						//System.out.println("Data Type: " + "Count, RPKM & DGE");
					}
				} else if (source == speciesCombo) {
					ItemSelectable is = (ItemSelectable)source;
					Object selected[] = is.getSelectedObjects();
					//"Human"
					if (((String)selected[0]).equals(species[1])) {
						bldCombo.removeAllItems();
						for(int i = 0; i < hgGenomeBlds.length; i++) {
							bldCombo.addItem(hgGenomeBlds[i]);
						}
					} 
					// Mouse
					else if (((String)selected[0]).equals(species[2])) {
						bldCombo.removeAllItems();
						for(int i = 0; i < mmGenomeBlds.length; i++) {
							bldCombo.addItem(mmGenomeBlds[i]);
						}
					}
				} else if (source == bldCombo) {

				} else if (source == genomeCombo) {

				}
			}
		}
	}

	@Override
	public void setAnnotationFilePath(String filePath) {
		//sflp.adh.setAnnFilePath(filePath);
		//sflp.adh.annotationSelected = true;
	}

	/**
	 * Annotation File format validation function
	 * Hard coded Field names & order for the short term. 
	 * Needs to validate against a template file not against hard coded values.
	 *  
	 * @param file
	 * @return
	 * @throws HeadlessException
	 * @throws IOException
	 */
	public boolean validateAnnoFile(String file) throws HeadlessException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringSplitter ss = new StringSplitter((char) 0x09);
		String currentLine;
		int counter = 0, i = 0;
		String _tmp = "", expected_col_name = "OK";
		// This is hard coded..should come from a template file instead.
		String COL_NAMES[] = {	"PROBE_ID", "CHR", "STRAND", 
								"TX_START", "TX_END", "CDS_START", 
								"CDS_END",	"exonCount", "exonStarts", 
								"exonEnds", "GENE_SYMBOL", "GENE_TITLE", 
								"REFSEQ_ACC", "PROTEIN_ACC", "ENTREZ_ID"
							 };

		while ((currentLine = reader.readLine()) != null) {
			while (currentLine.endsWith("\t")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}
			//System.out.println("currentlIne: " + currentLine);
			ss.init(currentLine);
			// check the 1st line, the column header line
			if (counter == 0) { 
				//check if file has expected number of cols
				if (ss.countTokens()+1 != COL_NAMES.length){
					while(ss.hasMoreTokens()) {
						_tmp += ss.nextToken() + ", ";
					}
					String exp_cols = "";
					for (i = 0; i < COL_NAMES.length; i++) {
						exp_cols += COL_NAMES[i] + ", ";
					}
					JOptionPane.showMessageDialog(
							null,  
							"Provided columns (" + String.valueOf(ss.countTokens())+ "): " + _tmp +
							"\n Expected columns ("+ String.valueOf(COL_NAMES.length) +") : " + exp_cols, 
							"Annotation File Format Error.",  
							JOptionPane.ERROR_MESSAGE);
					reader.close();
					return false;
				}
				
				//check if col names & order match
				while(ss.hasMoreTokens()) {
					_tmp = ss.nextToken();

					if (!_tmp.equals(COL_NAMES[i])) { 
						expected_col_name = COL_NAMES[i]; 
						break; 
					}
					i++;
				}
			}
			if(!expected_col_name.equals("OK")) {
				JOptionPane.showMessageDialog(
						null,  
						"Invalid Column at position " + String.valueOf(i+1) + " : " + _tmp + "," +
						"\n Expected column : " + expected_col_name, 
						"Annotation File Format Error.",  
						JOptionPane.ERROR_MESSAGE);
				break;
			}
			// Silly... Just to make sure there are somthings other than the col headers
			if(counter > 20)
				break;

			counter++;
		}
		reader.close();
		if(expected_col_name.equals("OK")) return true;
		return false;
	}

	public void setTCGADataFile(String string) {
		sflp.fileNameTextField.setText(string);
		File selectedFile = new File(string);
		sflp.dataTypeCombo.setSelectedItem("RPKM & Count");
		sflp.speciesCombo.setSelectedItem("Human");
		sflp.genomeCombo.setSelectedItem("RefSeq");
		sflp.bldCombo.setSelectedItem("hg19");
		processStanfordFile(selectedFile);
		
		
	}

}
