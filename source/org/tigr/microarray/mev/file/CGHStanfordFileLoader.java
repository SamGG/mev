/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CGHStanfordFileLoader.java,v $
 * $Revision: 1.12 $
 * $Date: 2008-01-17 19:51:15 $
 * $Author: raktim $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

import org.tigr.microarray.mev.CGHSlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHDataGenerator.CGHCloneComparator;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;

public class CGHStanfordFileLoader extends ExpressionFileLoader {

    private GBA gba;
    private boolean stop = false;
    private CGHStanfordFileLoaderPanel CGHsflp;
    private ExpressionFileTableCellRenderer myCellRenderer;
    private MultipleArrayViewer mav; 
    /**
     * 
     * @param superLoader
     */
    public CGHStanfordFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        this.mav= this.superLoader.getArrayViewer();//Moved here by Sarita
        gba = new GBA();
        CGHsflp = new CGHStanfordFileLoaderPanel();
    }

    public Vector loadExpressionFiles() throws IOException {
        return loadStanfordExpressionFile(new File(this.CGHsflp.fileNameTextField.getText()));
    }
    
    public void setFilePath(String path) {
    	CGHsflp.fileNameTextField.setText(path);
    	processStanfordFile(new File(path));
    }
    
    public int getDataType() {
    	return IData.DATA_TYPE_RATIO_ONLY;
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }

    /*
     *  Handling of Stanford data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */

    public Vector loadStanfordExpressionFile(File f) throws IOException {

        final int preSpotRows = this.CGHsflp.getXRow()+1;
        final int preExperimentColumns = this.CGHsflp.getXColumn();
        final int species = this.CGHsflp.getXSpecies();
        final boolean isLog2 = this.CGHsflp.getXLog2Status();
       // System.out.println("Selected Species: " + species);
        ArrayList clones = new ArrayList();
                
        if (preExperimentColumns < 5) { //hanged from 4 to 5 to accomodate Probe Desc
        	//Throw Message Here for errors
        	JOptionPane.showMessageDialog(superLoader.getFrame(),  "Insufficient annotation.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
        int cloneFileIndex = 0; // Position of Clone in File
        
        int numLines = this.getCountOfLines(f);

        int spotCount = numLines - preSpotRows;

        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
        
        CGHClone[] cloneArray = new CGHClone[numLines-1];
        Hashtable unSortedCloneNames = new Hashtable();
        
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        String value;
        float cy3, cy5;
        String[] moreFields = new String[preExperimentColumns];

        final int rColumns = 1;
        final int rRows = spotCount;

        ISlideData[] slideDataArray = null;
        CGHSlideDataElement sde;

        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringSplitter ss = new StringSplitter((char)0x09);
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
            if (stop) {
                return null;
            }
            ss.init(currentLine);
            if (counter == 0) { // parse header
                int experimentCount = ss.countTokens()+1 - preExperimentColumns;
                slideDataArray = new ISlideData[experimentCount];
                //Raktim
                //System.out.println("ISlideData length: " + experimentCount);
                SampleAnnotation sampAnn=new SampleAnnotation();
				slideDataArray = new ISlideData[experimentCount];
				slideDataArray[0] = new SlideData(rRows, rColumns, sampAnn);
              
                //Raktim
                //System.out.println("SlideData rRows & rColumns: " + rRows + ", " + rColumns);
                slideDataArray[0].setSlideFileName(f.getPath());
                
                for (int i=1; i<slideDataArray.length; i++) {
                	sampAnn=new SampleAnnotation();
					slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount, sampAnn);
                    slideDataArray[i].setSlideFileName(f.getPath());
                }
                //get Field Names
                String [] fieldNames = new String[preExperimentColumns];
                for(int i = 0; i < preExperimentColumns; i++){
                    fieldNames[i] = ss.nextToken();
                    //Raktim
                	//System.out.println("preExpr Token " + fieldNames[i]);
                }
                //TMEV.setFieldNames(fieldNames);
                slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);

                for (int i=0; i<experimentCount; i++) {
                	//This is where the "Default Slide Name" gets set in the SampleAnnotation Model
					
					String val=ss.nextToken();
					slideDataArray[i].setSampleAnnotationLoaded(true);
					slideDataArray[i].getSampleAnnotation().setAnnotation("Default Slide Name", val);
					slideDataArray[i].setSlideDataName(val);
					
					this.mav.getData().setSampleAnnotationLoaded(true);
                  
                    //Raktim
                	//System.out.println("Expr Token " + slideDataArray[i].getSlideDataName());
                }
            } else if (counter >= preSpotRows) { // data rows
            	//Raktim
            	//System.out.println("Else If counter value: " + counter);
                rows[0] = rows[2] = row;
                columns[0] = columns[2] = column;
                if (column == rColumns) {
                    column = 1;
                    row++;
                } else {
                    column++;
                }
                for (int i=0; i<preExperimentColumns; i++) {
                    moreFields[i] = ss.nextToken();
                }
                
                CGHClone clone_T2 = new CGHClone(moreFields[0].trim(), moreFields[1].trim(), moreFields[2].trim(), moreFields[3].trim(), species);
                cloneArray[cloneFileIndex] = clone_T2;
                unSortedCloneNames.put(moreFields[0].trim(), new Integer(cloneFileIndex));
                
                sde = new CGHSlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields);
                slideDataArray[0].addSlideDataElement(sde);

                for (int i=0; i<slideDataArray.length; i++) {

                    cy3 = 1f;  //set cy3 to a default value of 1.

                    try {
                        value = ss.nextToken();
                        cy5 = Float.parseFloat(value);  //set cy5 to hold the value
                        //getRatio methods will return cy5
                        //for Stanford data type
                    } catch (Exception e) {
                        cy3 = 0;
                        cy5 = Float.NaN;
                    }
                    slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
                }
                cloneFileIndex++;
            } else {
//            	Raktim
            	//System.out.println("Final Else counter value: " + counter);
                //we have additional sample annotation

                //advance to sample key
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
        }
        reader.close();
      //  MultipleArrayViewer mav = this.superLoader.getArrayViewer();--commented by Sarita
        
        //Set Data characteristics
        ((MultipleArrayData)mav.getData()).setHasDyeSwap(false);
        ((MultipleArrayData)mav.getData()).setLog2Data(isLog2);
        ((MultipleArrayData)mav.getData()).setHasCloneDistribution(false);
        ((MultipleArrayData)mav.getData()).setCGHData();
        ((MultipleArrayData)mav.getData()).setCGHSpecies(species);
        
        /**
         * Sort the Data based on CGHClone Chr & Start Position
         */
        
        List sortedList = Arrays.asList(cloneArray);
        Collections.sort(sortedList, new CGHCloneComparator());
        
        //sortSlideDataArray(slideDataArray, sortedList, unSortedCloneNames, clones, species);
        sortSlideDataArrays(slideDataArray, sortedList, unSortedCloneNames, clones, species);
        
        /**
         * Create Chromosome Indices for MAD variable chromosomeIndices
         */
        int chrIndices[][] = calculateChromosomeIndices(sortedList);
        
        /**
         * Remove temporary variables
         */
        unSortedCloneNames.clear();
        unSortedCloneNames = null;
        cloneArray = null;
        sortedList = null;
        
        ((MultipleArrayData)mav.getData()).setChromosomeIndices(chrIndices);
        
        /**
         * Create default samples order as apprered in the file
         */
        int samplesOrder[] = generateDefaultSamplesOrder(slideDataArray);
        ((MultipleArrayData)mav.getData()).setSamplesOrder(samplesOrder);
        
        /**
         * Set ArrayList clones in MAD
         */
        ((MultipleArrayData)mav.getData()).setClones(clones);
        
        Vector data = new Vector(slideDataArray.length);
        for(int i = 0; i < slideDataArray.length; i++)
            data.add(slideDataArray[i]);

        this.setFilesProgress(1);
        return data;
    }

    /**
     * Raktim Oct 27, 05
     * @param clones
     * @return
     */
    public static int[][] calculateChromosomeIndices(List sortedList){
    	int numClones  = sortedList.size();
        int numChromosomes = ((CGHClone)sortedList.get(sortedList.size()-1)).getChromosomeIndex() + 1;
        int[][] chromosomeIndices = new int[numChromosomes][3];
        
        chromosomeIndices[0][0] = 0;
        int curChromosomeIndex = 0;
        Iterator clonesIt = sortedList.iterator();
        int i = 0;
        while(clonesIt.hasNext()){
            CGHClone curClone = (CGHClone)clonesIt.next();
            /* Set Clones sorted index */
            curClone.setSortedIndex(i);
            while(curClone.getChromosomeIndex() > curChromosomeIndex){
                chromosomeIndices[curChromosomeIndex][1] = i - 1;
                curChromosomeIndex++;
                chromosomeIndices[curChromosomeIndex][0] = i;
            }
            i++;
        }
        chromosomeIndices[numChromosomes - 1][1] = numClones - 1;
        /**
         * Store number of genes/probes in each Chromosome 
         */
        for(int ii = 0; ii < numChromosomes; ii++){
        	chromosomeIndices[ii][2] = chromosomeIndices[ii][1] - chromosomeIndices[ii][0] + 1;
        }
//      Debug 9/8/06
        for(int ii = 0; ii < numChromosomes; ii++){
        	int st = chromosomeIndices[ii][0];
        	int end = chromosomeIndices[ii][1];
        	int len = chromosomeIndices[ii][2];
        	//System.out.println("Start " + st + " End " +  end + " Len " + len);
        }
        return chromosomeIndices;
    }
    
    /**
     * Raktim Oct 28, 05
     * @param slideDataArray
     * @return
     */
    private int[] generateDefaultSamplesOrder(ISlideData[] slideDataArray){
        int[] samplesOrder = new int[slideDataArray.length];
        for(int i = 0 ; i < samplesOrder.length; i++){
            samplesOrder[i] = i;
        }
        return samplesOrder;
    }
    
    /**
     * Raktim Oct 25, 05
     * CGH Function - NOT USED
     * Function to arrange SlideDataElements & Float values according to sort order
     */
    private void sortSlideDataArray(ISlideData[] slideDataArray, List sorted, ArrayList unSorted, ArrayList clones, int species) {
      //  System.out.println("unSorted size: " + unSorted.size());
  
        Iterator clonesIt = sorted.iterator();
        int sortInd_T = 0;
        while(clonesIt.hasNext()){
        	CGHClone curClone = (CGHClone)clonesIt.next();
        	int ind_T = unSorted.indexOf(curClone.getName());    
        	//System.out.println("Clone Ind & Name: " + ind_T + ": " + curClone.getName());
            clones.add(curClone);
                       
            if (curClone.getName() != unSorted.get(ind_T)) {
            	System.out.println("Swap Sort Ind, File Ind: " + sortInd_T + ", " + ind_T);
            	System.out.println("Sorted clone entry: " + curClone.getName());
            	System.out.println("UnSorted clone entry: " + unSorted.get(ind_T));
            	System.exit(1);
            }
            
        	CGHSlideDataElement sde_T1 = (CGHSlideDataElement)slideDataArray[0].getSlideDataElement(sortInd_T);
        	
			CGHClone clone_T1 = sde_T1.getClone(species);
        	if (clone_T1 == null) {
				System.out.println("Null CGHClone");
				System.exit(1);
			}
        	
			for(int j = 0; j < slideDataArray.length; j++) {		
        		//Swap SlideData Element for ordering
        		if (j == 0) {
        			CGHSlideDataElement rem_T = (CGHSlideDataElement)((SlideData)slideDataArray[j]).set(ind_T, sde_T1);
        			((SlideData)slideDataArray[j]).setElementAt(rem_T, sortInd_T);
        		}
        		else {
	        		//Swap FloatMatrix Element for ordering, index 1 to n samples
	        		FloatSlideData fse_T = (FloatSlideData)slideDataArray[j];
	        		float cy3_1 = fse_T.getCY3(sortInd_T);
	        		float cy5_1 = fse_T.getCY5(sortInd_T);
	        		float cy3_2 = fse_T.getCY3(ind_T);
	        		float cy5_2 = fse_T.getCY5(ind_T);
	        		fse_T.setIntensities(ind_T, cy3_1, cy5_1);
	        		fse_T.setIntensities(sortInd_T, cy3_2, cy5_2);
        		}
        	}
			String name_T = (String) unSorted.set(ind_T, clone_T1.getName());
			unSorted.set(sortInd_T, name_T);
			//if(sortInd_T % 500 == 0) System.out.println("Records Done: " + sortInd_T);
        	sortInd_T++;
        }
        
        System.out.println("Loop times: " + sortInd_T);
        System.out.println("# of SlideDataElements: " + slideDataArray[0].getSize() + ", " + sorted.size());

    }
    
    /**
     * Function to arrange SlideDataElements & Float values according to sort order
     * Improvement Over sortSlideDataArray() in terms of cost efficiency
     * @param slideDataArray
     * @param sorted
     * @param unSorted
     * @param clones
     */
    private void sortSlideDataArrays(ISlideData[] slideDataArray, List sorted, Hashtable unSorted, ArrayList clones, int species) {
        System.out.println("List size: " + sorted.size());
        System.out.println("unSorted size: " + unSorted.size());
  
        Iterator clonesIt = sorted.iterator();
        int sortInd_T = 0;
        while(clonesIt.hasNext()){
        	CGHClone curClone = (CGHClone)clonesIt.next();
        	Integer int_T = (Integer)unSorted.get(curClone.getName());
        	if (int_T == null) {
        		System.out.println("Sorted clone entry not found in Hashtable: " + curClone.getName());
        		System.exit(1);
        	}
        	int ind_T = int_T.intValue();
            clones.add(curClone);
                        
        	CGHSlideDataElement sde_T1 = (CGHSlideDataElement)slideDataArray[0].getSlideDataElement(sortInd_T);
			CGHClone clone_T1 = sde_T1.getClone(species);
        	if (clone_T1 == null) {
				System.out.println("Null CGHClone");
				System.exit(1);
			}
        	
			for(int j = 0; j < slideDataArray.length; j++) {		
        		//Swap SlideData Element for ordering
        		if (j == 0) {
        			CGHSlideDataElement rem_T = (CGHSlideDataElement)((SlideData)slideDataArray[j]).set(ind_T, sde_T1);
        			((SlideData)slideDataArray[j]).setElementAt(rem_T, sortInd_T);
        		}
        		else {
	        		//Swap FloatMatrix Element for ordering, index 1 to n samples
	        		FloatSlideData fse_T = (FloatSlideData)slideDataArray[j];
	        		float cy3_1 = fse_T.getCY3(sortInd_T);
	        		float cy5_1 = fse_T.getCY5(sortInd_T);
	        		float cy3_2 = fse_T.getCY3(ind_T);
	        		float cy5_2 = fse_T.getCY5(ind_T);
	        		fse_T.setIntensities(ind_T, cy3_1, cy5_1);
	        		fse_T.setIntensities(sortInd_T, cy3_2, cy5_2);
        		}
        	}
			Integer loc_T = (Integer) unSorted.put(clone_T1.getName(), new Integer(ind_T));
			unSorted.put(curClone.getName(), loc_T);
        	sortInd_T++;
        }       
         System.out.println("# of SlideDataElements: " + slideDataArray[0].getSize() + ", " + sorted.size());
    }
    
    public FileFilter getFileFilter() {

        FileFilter mevFileFilter = new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt")) return true;
                else return false;
            }

            public String getDescription() {
                return "CGH Tab Delimited, Multiple Sample Files (*.txt)";
            }
        };

        return mevFileFilter;
    }

    public boolean checkLoadEnable() {

        // Currently, the only requirement is that a cell has been highlighted

        int tableRow = CGHsflp.getXRow() + 1; // Adjusted by 1 to account for the table header
        int tableColumn = CGHsflp.getXColumn();

        if (tableColumn < 0) return false;

        TableModel model = CGHsflp.getTable().getModel();
        String fieldSummary = "";
        for (int i = 0; i < tableColumn; i++) {
            //  System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
            fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
        }

       // CGHsflp.setFieldsText(fieldSummary);

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
        return CGHsflp;
    }

    public void processStanfordFile(File targetFile) {

        Vector<String> columnHeaders = new Vector<String>();
        Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
        Vector<String> rowVector = null;
        BufferedReader reader = null;
        String currentLine = null;

        if (! validateFile(targetFile)) return;

        CGHsflp.setFileName(targetFile.getAbsolutePath());

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
            ss.init(currentLine);

            for (int i = 0; i < ss.countTokens()+1; i++) {
                columnHeaders.add(ss.nextToken());
            }

            model.setColumnIdentifiers(columnHeaders);
            int cnt = 0;
            while ((currentLine = reader.readLine()) != null && cnt < 100) {
                cnt++;
                ss.init(currentLine);
                rowVector = new Vector<String>();
                for (int i = 0; i < ss.countTokens()+1; i++) {
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

        CGHsflp.setTableModel(model);
        Point p = getFirstExpressionCell(dataVector);
        CGHsflp.setSelectedCell(p.x, p.y);
    }

    public String getFilePath() {
        return this.CGHsflp.fileNameTextField.getText();
    }

    public void openDataPath() {
        this.CGHsflp.openDataPath();
    }

	/*
	 * CGHStanfordFileLoader - Internal Classes
	 */

    private class CGHStanfordFileLoaderPanel extends JPanel {

        

        JTextField fileNameTextField, annFileNameTextField;
        JTextField selectedFiles;
        JPanel fileSelectionPanel;
        
        //New Fields for Species
        JRadioButton speciesHsButton;
        JRadioButton speciesMmButton;
        ButtonGroup speciesGroup;
        
        JRadioButton log2RatioButton;
        JRadioButton justRatioButton;
        ButtonGroup ratioGroup;
        JPanel additionalRequirements;
        //
        JTable expressionTable;
        JLabel instructionsLabel, customAnnotation;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        JPanel fileLoaderPanel;
        JPanel annotationPanel;
        JButton browseButton1, browseButton2;
        protected EventListener eventListener;

        JLabel fileSelectionLabel, dataSelection;

        private int xRow = -1;
        private int xColumn = -1;
        private int xSpecies = -1;
        private boolean isLog2 = false;

        public CGHStanfordFileLoaderPanel() {

            setLayout(new GridBagLayout());

           
            eventListener=new EventListener();
            fileNameTextField = new JTextField();
            fileNameTextField.setEditable(false);
            fileNameTextField.setForeground(Color.black);
            fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));

            selectedFiles = new JTextField();
            selectedFiles.setEditable(false);
            selectedFiles.setForeground(Color.black);
            selectedFiles.setFont(new Font("monospaced", Font.BOLD, 12));
         
            
            fileSelectionLabel=new JLabel();
            fileSelectionLabel.setForeground(java.awt.Color.BLACK);
            String fileTypeChoices = "<html> Selected files </html>";
            fileSelectionLabel.setText(fileTypeChoices);

            
            dataSelection=new JLabel();
            dataSelection.setForeground(java.awt.Color.BLACK);
            String chooseFile="<html>Select data file</html>";
            dataSelection.setText(chooseFile);
           
                           
            browseButton1=new JButton("Browse");
            browseButton1.addActionListener(eventListener);
           	browseButton1.setSize(100, 30);
    		browseButton1.setPreferredSize(new Dimension(100, 30));
    		

            
            fileSelectionPanel = new JPanel();
            fileSelectionPanel.setLayout(new GridBagLayout());
            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (CGH Stanford Format Files)"));
            
            gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
              
            speciesHsButton = new JRadioButton("Human", true);
            speciesHsButton.setFocusPainted(false);
            speciesMmButton = new JRadioButton("Mouse");
            speciesMmButton.setFocusPainted(false);
            speciesGroup = new ButtonGroup();
            speciesGroup.add(speciesHsButton);
            speciesGroup.add(speciesMmButton);
            
            additionalRequirements = new JPanel();
            additionalRequirements.setLayout(new GridBagLayout());
            additionalRequirements.setBorder(new TitledBorder(new EtchedBorder(), "Additional Requirements"));
           
            log2RatioButton = new JRadioButton("Log2 Ratio", true);
            log2RatioButton.setFocusPainted(false);
            justRatioButton = new JRadioButton("Ratio");
            justRatioButton.setFocusPainted(false);
            ratioGroup = new ButtonGroup();
            ratioGroup.add(log2RatioButton);
            ratioGroup.add(justRatioButton);
            
            
            gba.add(additionalRequirements, speciesHsButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            gba.add(additionalRequirements, speciesMmButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            gba.add(additionalRequirements, log2RatioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            gba.add(additionalRequirements, justRatioButton, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            //
            expressionTable = new JTable();
            myCellRenderer = new ExpressionFileTableCellRenderer();
            expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
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
            String instructions = "<html>First 5 columns MUST be ProbeID, Chromosome, Start, End and Description <BR>" +
            					  "Columns MUST be in the order mentioned above<BR>"+
            					  "Description field can contain anything e.g., Gene Symbol, RefSeq etc.<BR>" +
            					  "Click the upper-leftmost expression value. Click the <b>Load</b> button to finish.</html>";
            
            
         
            
            instructionsLabel.setText(instructions);

            tablePanel = new JPanel();
            tablePanel.setLayout(new GridBagLayout());
            tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));
            gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            
            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());

            gba.add(fileLoaderPanel,fileSelectionPanel, 		0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
         //   gba.add(fileLoaderPanel, annotationPanel, 		0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, additionalRequirements, 	0, 4, 1, 2, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, tablePanel, 				0, 7, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
           
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
           
        }
        private void setSelectedCell( int xR, int xC) {
            xRow = xR;
            xColumn = xC;
        myCellRenderer.setSelected(xRow, xColumn);
        expressionTable.repaint();
        checkLoadEnable();
    }
        public void openDataPath() {
            //fileTreePane.openDataPath();
        }
        
        public void onBrowse() {
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	int retVal=fileChooser.showOpenDialog(CGHStanfordFileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        	File selectedFile=fileChooser.getSelectedFile();
        	processStanfordFile(selectedFile);
        	}
           		
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
        
        public int getXSpecies() {
        	if(this.speciesHsButton.isSelected()) xSpecies = TMEV.CGH_SPECIES_HS;
        	else if (this.speciesMmButton.isSelected()) xSpecies = TMEV.CGH_SPECIES_MM;
        	else xSpecies = TMEV.CGH_SPECIES_Undef;
            return xSpecies;
        }
        
        public boolean getXLog2Status() {
        	if(this.log2RatioButton.isSelected()) 
        		isLog2 = true;
        	if(this.justRatioButton.isSelected()) 
        		isLog2 = false;
        	return isLog2;
        }
        public void selectStanfordFile() {
            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
            jfc.setFileFilter(getFileFilter());
            int activityCode = jfc.showDialog(this, "Select");

            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
               
                processStanfordFile(target);
            }
        }

        public void setFileName(String fileName) {
            fileNameTextField.setText(fileName);
            selectedFiles.setText(fileName);
        }

        public void setTableModel(TableModel model) {
            expressionTable.setModel(model);
            int numCols = expressionTable.getColumnCount();
            for(int i = 0; i < numCols; i++){
                expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }

     


        private class EventListener implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
    			Object source = event.getSource();
    			if (source == browseButton1) {
    				onBrowse();
    			} 
    		//	if (source == browseButton3) {
    				//onBrowse();
    		//	} 
    			
    			//if (source == browseButton2) {
    				//onCallFileBrowse();
    			//} 
    	
    			
    			}
    		}


       
    }

	/**
	 * Not useful in this loader because it doesn't use the annotation model yet.
	 */
    public String getAnnotationFilePath() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not useful in this loader because it doesn't use the annotation model yet.
	 */
	public void setAnnotationFilePath(String filePath) {
//		sflp.adh.setAnnFilePath(filePath);
	}
}