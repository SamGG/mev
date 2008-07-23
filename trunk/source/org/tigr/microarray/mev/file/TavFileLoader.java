/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TavFileLoader.java,v $
 * $Revision: 1.7 $
 * $Date: 2007-12-19 21:39:37 $
 * $Author: saritanair $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;

import org.tigr.microarray.util.FileLoaderUtility;

public class TavFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private TavFileLoaderPanel tflp;
    
    private boolean loadEnabled = false;
    private boolean stop = false;
    private ISlideMetaData meta;
    private boolean fillMissingSpots = false;
    private static final int BUFFER_SIZE = 1024*128;
    
    public TavFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        tflp = new TavFileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
        
        Object [] tavFiles = tflp.getTavSelectedListModel().toArray();
        Vector data = new Vector(tavFiles.length); 
        ISlideData slideData;
        
        if(tavFiles.length < 1)
            return null;
        
        setFilesCount(tavFiles.length);
        
        File ffile=new File(this.tflp.pathTextField.getText(),((File)tavFiles[0]).getName());
    //   int countOfLines = getCountOfLines((File)tavFiles[0]);--original code commented by Sarita
        int countOfLines = getCountOfLines(ffile);
      
        for (int i = 0; i < tavFiles.length; i++) {
        	File file=new File(this.tflp.pathTextField.getText(),((File)tavFiles[i]).getName());
        	
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(tavFiles.length-i);
            //setFileName(((File)tavFiles[i]).getPath());
            setFileName(file.getPath());
            if (i == 0) {                
                setLinesCount(countOfLines);
                if (meta == null) {
                    if(fillMissingSpots) {
                       // slideData = loadSlideDataFillAllSpots((File)tavFiles[i]);
                    	 slideData = loadSlideDataFillAllSpots(file);
                    }
                    else {
                       // slideData = loadSlideData((File)tavFiles[i]);
                    	slideData = loadSlideData(file);
                    }
                    meta = slideData.getSlideMetaData();
                } else {
                  //  slideData = loadFloatSlideData((File)tavFiles[i], countOfLines, meta);
                	  slideData = loadFloatSlideData(file, countOfLines, meta);
                }
            } else {
              //  slideData = loadFloatSlideData((File)tavFiles[i], countOfLines, meta);
            	slideData = loadFloatSlideData(file, countOfLines, meta);
            }
            data.add(slideData);            
        }
        return data;
    }
    
    
    
    public ISlideData loadExpressionFile(File currentFile) throws IOException {   
        return null;
    }
    
    
    /**
     * Loads full a microarray data from a specified file.
     * Skips missing spots.
     */
    
    private ISlideData loadSlideData(final File file) throws IOException {
        
        ISlideDataElement slideDataElement;
        String currentLine;
        
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        int maxRows = 0, maxColumns = 0;
        String avoidNullString;
        int p, q;
        int coordinatePairCount = 3; //TMEV.getCoordinatePairCount();
        int intensityCount = 2; //TMEV.getIntensityCount();
        final int preSpotRows = 0; //TMEV.getHeaderRowCount();
        
        int[] rows = new int[coordinatePairCount];
        int[] columns = new int[coordinatePairCount];
        float[] intensities = new float[intensityCount];
        Vector moreFields = new Vector();
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        StringSplitter ss = new StringSplitter((char)0x09);
        int currentRow, currentColumn;
        int header_row = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            ss.init(currentLine);
            currentRow = ss.nextIntToken();
            currentColumn = ss.nextIntToken();
            if (currentRow > maxRows) maxRows = currentRow;
            if (currentColumn > maxColumns) maxColumns = currentColumn;
        }
        SlideData slideData = new SlideData(maxRows, maxColumns);
        reader.close();
        reader = new BufferedReader(new FileReader(file));
        header_row = 0;
        int curpos = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            setFileProgress(curpos++);
            ss.init(currentLine);
            for (int j = 0; j < coordinatePairCount; j++) {
                rows[j] = ss.nextIntToken();
                columns[j] = ss.nextIntToken();
            }
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            
            //EH fieldnames are saved to SlideData rather than TMEV
            while(ss.hasMoreTokens()) {
                avoidNullString = ss.nextToken();
                if (avoidNullString.equals("null")) moreFields.add("");
                else moreFields.add(avoidNullString);
            }
            /*
            for (int j = 0; j < TMEV.getFieldNames().length; j++) {
                if (ss.hasMoreTokens()) {
                    avoidNullString = ss.nextToken();
                    if (avoidNullString.equals("null")) moreFields[j] = "";
                    else moreFields[j] = avoidNullString;
                } else {
                    moreFields[j] = "";
                }
            }
            */
            String[] allFields = new String[moreFields.size()];
            for(int i=0; i<moreFields.size(); i++) {
            	allFields[i] = (String)moreFields.get(i);
            }
            slideDataElement = new SlideDataElement(String.valueOf(curpos),rows, columns, intensities, allFields);
            //EH end fieldnames loading change
            
            slideData.addSlideDataElement(slideDataElement);
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    
    /**
     * Loads a microarray float values from the specified file.
     */
    private ISlideData loadFloatSlideData(final File file, final int countOfLines, ISlideMetaData slideMetaData) throws IOException {
        final int coordinatePairCount = 6;//TMEV.getCoordinatePairCount()*2;
        final int intensityCount = 2;//TMEV.getIntensityCount();
        final int preSpotRows = 0; //TMEV.getHeaderRowCount();

        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        FloatSlideData slideData = new FloatSlideData(slideMetaData);
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        
        String currentLine;
        StringSplitter ss = new StringSplitter((char)0x09);
        float[] intensities = new float[intensityCount];
        int header_row = 0;
        int index  = 0;
        while ((currentLine = reader.readLine()) != null) {
        	
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            setFileProgress(index);
            ss.init(currentLine);
            ss.passTokens(coordinatePairCount);
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            slideData.setIntensities(index, intensities[0], intensities[1]);
            index++;
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    
    /**
     * Loads full a microarray data from a specified file.
     * Fills all missing spots with default missing color.
     */
    private ISlideData loadSlideDataFillAllSpots(final File file) throws IOException {
        
        ISlideDataElement slideDataElement;
        String currentLine;
        
        //FL
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        int maxRows = 0, maxColumns = 0;
        String avoidNullString;
        int p, q;
        int coordinatePairCount = TMEV.getCoordinatePairCount();
        int intensityCount = TMEV.getIntensityCount();
        final int preSpotRows = TMEV.getHeaderRowCount();
        
        int[] rows = new int[coordinatePairCount];
        int[] columns = new int[coordinatePairCount];
        
        float[] intensities = new float[intensityCount];
        
        //EH fieldnames saved to SlideData rather than TMEV
        //String[] moreFields = new String[TMEV.getFieldNames().length];
        Vector moreFields = new Vector();
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        StringSplitter ss = new StringSplitter((char)0x09);
        int currentRow, currentColumn;
        int header_row = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            ss.init(currentLine);
            currentRow = ss.nextIntToken();
            currentColumn = ss.nextIntToken();
            if (currentRow > maxRows) maxRows = currentRow;
            if (currentColumn > maxColumns) maxColumns = currentColumn;
        }
        SlideData slideData = new SlideData(maxRows, maxColumns);
        reader.close();
        reader = new BufferedReader(new FileReader(file));
        header_row = 0;
        int curpos = 0;
        
        boolean [][] realData = new boolean[maxRows][maxColumns];
        
        while ((currentLine = reader.readLine()) != null) {
            
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            setFileProgress(curpos++);
            ss.init(currentLine);
            for (int j = 0; j < coordinatePairCount; j++) {
                rows[j] = ss.nextIntToken();
                columns[j] = ss.nextIntToken();
            }
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            
            //EH loading fieldnames for additional annotation
            while(ss.hasMoreTokens()) {
                avoidNullString = ss.nextToken();
                if (avoidNullString.equals("null")) moreFields.add("");
                else moreFields.add(avoidNullString);
            }
            String[] allFields = new String[moreFields.size()];
            for(int i=0; i<moreFields.size(); i++) {
            	allFields[i] = (String)moreFields.get(i);
            }
            /*
            for (int j = 0; j < TMEV.getFieldNames().length; j++) {
                if (ss.hasMoreTokens()) {
                    avoidNullString = ss.nextToken();
                    if (avoidNullString.equals("null")) moreFields[j] = "";
                    else moreFields[j] = avoidNullString;
                } else {
                    moreFields[j] = "";
                }
            }
            */
            realData[rows[0]-1][columns[0]-1] = true;
            slideDataElement = new SlideDataElement(String.valueOf(curpos), rows, columns, intensities, allFields);
            slideData.addSlideDataElement(slideDataElement);
        }
        reader.close();
        intensities[0] = 0.0f;
        intensities[1] = 0.0f;
 /*       //EH
        String [] dummyString = new String[TMEV.getFieldNames().length];
        for(int i = 0; i < dummyString.length; i++)
            dummyString[i] = "";
*/        
        for(int i = 0; i < maxRows ; i++){
            for(int j = 0; j < maxColumns; j++){
                if(!realData[i][j]){
                    slideDataElement = new SlideDataElement(new int[]{i+1, 1, 1}, new int[]{j+1, 1,1}, intensities, new String[0]);
                    slideData.insertElementAt(slideDataElement, i*maxColumns+j);
                }
            }
        }
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter tavFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".tav")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "TIGR ArrayViewer Expression Files (*.tav)";
            }
        };
        
        return tavFileFilter;
    }
    
    public boolean checkLoadEnable() {
        setLoadEnabled(loadEnabled);
        return this.loadEnabled;
    }
    
    public void markLoadEnabled(boolean state) {
        loadEnabled = state;
        checkLoadEnable();
    }
    
    public JPanel getFileLoaderPanel() {
        return tflp;
    }
    
    public void processFileList(String filePath, Vector fileNames) {
        
        tflp.setPath(filePath);
        
        if (fileNames == null) return; // Don't process files if there aren't any
        
        FileFilter tavFileFilter = getFileFilter();
        
        tflp.getTavAvailableListModel().clear();
        
        for (int i = 0; i < fileNames.size(); i++) {
            
            File targetFile = new File((String) fileNames.elementAt(i));
            
            if (tavFileFilter.accept(targetFile)) {
                tflp.getTavAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }
    
    public String getFilePath() {
        if(this.tflp.getTavSelectedListModel().getSize() <1)
            return null;
        return ((File)(tflp.getTavSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
     //   this.tflp.openDataPath();
    }
    
/*
//
//	TavFileLoader - Internal Classes
//
 */
    
    private class TavFileLoaderPanel extends JPanel {
        
       
        JTextField pathTextField;
        
        JPanel tavSelectionPanel;
        JPanel tavListPanel;
        JLabel tavAvailableLabel;
        JLabel tavSelectedLabel;
        JList tavAvailableList;
        JList tavSelectedList;
        JScrollPane tavAvailableScrollPane;
        JScrollPane tavSelectedScrollPane;
        JButton tavAddButton;
        JButton tavAddAllButton;
        JButton tavRemoveButton;
        JButton tavRemoveAllButton;
        JPanel tavButtonPanel;
        
        JTextField preferencesTextField;
        JButton browseButton2;
        JPanel preferencesSelectionPanel;
        JPanel preferencesPanel;
        JPanel manualPanel;
        JPanel genericPanel;
        JTabbedPane fieldsTabbedPane;
        JPanel fieldsPanel;
        
        JPanel selectionPanel;
        JSplitPane splitPane;
        JPanel fileLoaderPanel;
        
        //Added by Sarita
        JPanel selectFilePanel;
		JLabel selectFile, selectAnnotation;
		JButton browseButton1;
		
        
        public TavFileLoaderPanel() {
            
            setLayout(new GridBagLayout());
            selectFilePanel = new JPanel();
			// selectFilePanel.setSize(new Dimension(100,20));
			selectFilePanel.setLayout(new GridBagLayout());

			selectFile = new JLabel("Select expression data directory");

			browseButton1 = new JButton("Browse");
			browseButton1.setSize(new Dimension(100, 30));
			browseButton1.setPreferredSize(new Dimension(100, 30));
			browseButton1.addActionListener(new EventHandler());
		
			
			pathTextField = new JTextField();
			pathTextField.setEditable(false);
			pathTextField.setForeground(Color.black);
			pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));

		
			gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    	      
			
			
			
			/*gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 0, 1,
					1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			*/
			
			//

			tavSelectionPanel = new JPanel();
			tavSelectionPanel.setLayout(new GridBagLayout());

			tavSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(),
					"File    (TaV Format Files)"));

			tavAvailableLabel = new JLabel("Available");
			tavSelectedLabel = new JLabel("Selected");
			tavAvailableList = new JList(new DefaultListModel());
			tavSelectedList = new JList(new DefaultListModel());

			tavAvailableScrollPane = new JScrollPane(tavAvailableList);
			tavSelectedScrollPane = new JScrollPane(tavSelectedList);
			tavAddButton = new JButton("Add");
			tavAddButton.setPreferredSize(new Dimension(100, 20));

			tavAddButton.addActionListener(new EventHandler());
			tavAddAllButton = new JButton("Add All");

			tavAddAllButton.setPreferredSize(new Dimension(100, 20));

			tavAddAllButton.addActionListener(new EventHandler());
			tavRemoveButton = new JButton("Remove");

			tavRemoveButton.setPreferredSize(new Dimension(100, 20));

			tavRemoveButton.addActionListener(new EventHandler());
			tavRemoveAllButton = new JButton("Remove All");

			tavRemoveAllButton.setPreferredSize(new Dimension(100, 20));

			tavRemoveAllButton.addActionListener(new EventHandler());

			tavButtonPanel = new JPanel();
			tavButtonPanel.setLayout(new GridBagLayout());

			gba.add(tavButtonPanel, tavAddButton, 0, 0, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavButtonPanel, tavAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavButtonPanel, tavRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavButtonPanel, tavRemoveAllButton, 0, 3, 1, 1, 0, 0,
					GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			// Medians vs. Integrate intensities
			
			tavListPanel = new JPanel();
			tavListPanel.setLayout(new GridBagLayout());

			gba.add(tavListPanel, tavAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavListPanel, tavAvailableScrollPane, 0, 1, 1, 4, 5, 1,
					GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
					new Insets(0, 0, 0, 0), 0, 0);
			gba.add(tavListPanel, tavButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavListPanel, tavSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavListPanel, tavSelectedScrollPane, 2, 1, 1, 4, 5, 1,
					GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			gba.add(tavSelectionPanel, selectFilePanel, 0, 0, 1, 1, 1, 0,
					GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			//gba.add(tavSelectionPanel, buttonPanel, 0, 1, 1, 1, 1, 1, GBA.B,
			//		GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(tavSelectionPanel, tavListPanel, 0, 1, 1, 1, 1, 1, GBA.B,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			
			 preferencesTextField = new JTextField();
	            preferencesTextField.setEditable(false);
	            preferencesTextField.setForeground(Color.black);
	            preferencesTextField.setFont(new Font("monospaced", Font.BOLD, 12));
	            
	            browseButton2 = new JButton("Browse Preferences");
	            browseButton2.addActionListener(new EventHandler());
	            
	            preferencesSelectionPanel = new JPanel();
	            preferencesSelectionPanel.setLayout(new GridBagLayout());
	            preferencesSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Selected Preferences File"));
	            gba.add(preferencesSelectionPanel, preferencesTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            gba.add(preferencesSelectionPanel, browseButton2, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            
	            preferencesPanel = new JPanel();
	            preferencesPanel.setLayout(new GridBagLayout());
	            gba.add(preferencesPanel, preferencesSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            
	            manualPanel = new JPanel();
	            manualPanel.setLayout(new GridBagLayout());
	            gba.add(manualPanel, new JPanel(), 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            
	            genericPanel = new JPanel();
	            genericPanel.setLayout(new GridBagLayout());
	            gba.add(genericPanel, new JPanel(), 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            
            fieldsTabbedPane = new JTabbedPane();
            fieldsTabbedPane.addTab("Preferences", preferencesPanel);
            fieldsTabbedPane.addTab("Manual", manualPanel);
            fieldsTabbedPane.addTab("Generic", genericPanel);
            fieldsTabbedPane.setEnabledAt(1, false);
            fieldsTabbedPane.setEnabledAt(2, false);
            fieldsTabbedPane.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    validateLists();
                }
            });
            
            fieldsPanel = new JPanel();
            fieldsPanel.setLayout(new GridBagLayout());
            fieldsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Additional Requirements"));
            gba.add(fieldsPanel, fieldsTabbedPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            selectionPanel = new JPanel();
            selectionPanel.setLayout(new GridBagLayout());
            //gba.add(selectionPanel, pathTextField, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, tavSelectionPanel, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, fieldsPanel, 0, 3, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
          
            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            gba.add(fileLoaderPanel, selectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        }
        
    public void onTavFileBrowse() {
		FileLoaderUtility fileLoad=new FileLoaderUtility();
		Vector retrievedFileNames=new Vector();
		JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.DATA_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retVal = fileChooser.showOpenDialog(TavFileLoaderPanel.this);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			((DefaultListModel) tavAvailableList.getModel()).clear();
			((DefaultListModel) tavSelectedList.getModel()).clear();

			File selectedFile = fileChooser.getSelectedFile();
			pathTextField.setText(selectedFile.getAbsolutePath());
			String path=selectedFile.getAbsolutePath();
			retrievedFileNames=fileLoad.getFileNameList(selectedFile.getAbsolutePath());
			
			for (int i = 0; i < retrievedFileNames.size(); i++) {
				Object fileName=retrievedFileNames.get(i);
				boolean acceptFile=getFileFilter().accept((File)fileName);

				if(acceptFile) {
					String Name=fileChooser.getName((File) fileName);
				/*	Object addItem = fileName;
					((DefaultListModel) tavAvailableList.getModel())
					.addElement(addItem);*/
					((DefaultListModel) tavAvailableList.getModel())
					.addElement(new File(Name));
					
				}
			}

		}
		    
    }
    
    
    
    
    
    
    
    
    
    
    
    public void setPath(String path) {
            pathTextField.setText(path);
        }

        public void openDataPath(){
           // this.fileTreePane.openDataPath();
        }       
        
        
        
        
        public void validateLists() {
            
            // Check if at least one tav file has been selected
            if (! (((DefaultListModel) tavSelectedList.getModel()).size() > 0)) {
                markLoadEnabled(false);
                return;
            }
            
            // Check the Additional Fields Selection area
            
            Object tabbedPaneTarget = fieldsTabbedPane.getSelectedComponent();
            if (tabbedPaneTarget == preferencesPanel) {
                if (! (preferencesTextField.getText().length() > 0)) {
                    markLoadEnabled(false);
                    return;
                } else {
                    markLoadEnabled(true);
                    return;
                }
            } else if (tabbedPaneTarget == manualPanel) {
                markLoadEnabled(true);
                return;
            } else { // tabbedPaneTarget == genericPanel
                markLoadEnabled(true);
                return;
            }
        }
        
        public void onTavAdd() {
            int[] chosenIndices = tavAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];
            
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) tavAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) tavAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }
            
            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) tavSelectedList.getModel()).addElement(chosenObjects[i]);
            }
            
            validateLists();
        }
        
        public void onTavAddAll() {
            int elementCount = ((DefaultListModel) tavAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) tavAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) tavSelectedList.getModel()).addElement(addItem);
            }
            
            validateLists();
        }
        
        public void onTavRemove() {
            int[] chosenIndices = tavSelectedList.getSelectedIndices();
            
            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) tavSelectedList.getModel()).remove(chosenIndices[i]);
            }
            
            validateLists();
        }
        
        public void onTavRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) tavSelectedList.getModel()).removeAllElements();
            
            validateLists();
        }
        
        public DefaultListModel getTavAvailableListModel() {
            return (DefaultListModel) tavAvailableList.getModel();
        }
        
        public DefaultListModel getTavSelectedListModel() {
            return (DefaultListModel) tavSelectedList.getModel();
        }
        
        public void processPreferencesFile(File target) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(target));
                
                String currentLine;
                StringSplitter ss;
                while ((currentLine = br.readLine()) != null) {
                    currentLine.trim();
                    if (!(currentLine.startsWith("//") || (currentLine.length() == 0))) {
                        if (currentLine.startsWith("Additional Fields")) {
                            ss = new StringSplitter('\t');
                            ss.init(currentLine);
                            ss.nextToken();
                            String fieldsString = ss.nextToken();
                            
                            ss = new StringSplitter(':');
                            ss.init(fieldsString);
                            String [] fieldNames = new String[ss.countTokens()+1];

                            int cnt = 0;
                            while (ss.hasMoreTokens()) {
                                fieldNames[cnt] = ss.nextToken();
                                cnt++;
                            }
                            //TODO this is a problem - can't set field names 
                            //for tav files because SlideData isn't accessible
//                            TMEV.setFieldNames(fieldNames);
                            preferencesTextField.setText(target.getPath());
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
            
            validateLists();
        }
        
        public void selectPreferencesFile() {
            
            JFileChooser jfc = new JFileChooser(TMEV.getFile("preferences/"));
            FileFilter ff = new FileFilter() {
                public boolean accept(File file) {
                    if (file.isDirectory()) return true;
                    String filename = file.getName();
                    if (filename.endsWith("Preferences")) return true;
                    else if (filename.endsWith("preferences")) return true;
                    else if (filename.endsWith(".pref")) return true;
                    else return false;
                }
                
                public String getDescription() {
                    return "Preference Files";
                }
            };
            jfc.setFileFilter(ff);
            int activityCode = jfc.showDialog(this, "Select");
            
            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
                processPreferencesFile(target);
            }
        }
        
        private class ListRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = (File) value;
                setText(file.getName());
                return this;
            }
        }
        
        private class EventHandler implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                
                Object source = event.getSource();
                
                if (source == tavAddButton) {
                    onTavAdd();
                } else if (source == tavAddAllButton) {
                    onTavAddAll();
                } else if (source == tavRemoveButton) {
                    onTavRemove();
                } else if (source == tavRemoveAllButton) {
                    onTavRemoveAll();
                } else if (source == browseButton2) {
                    selectPreferencesFile();
                }
                else if (source == browseButton1) {
                    onTavFileBrowse();
                }
            }
        }
        
        private class FileTreePaneEventHandler implements FileTreePaneListener {
            
            public void nodeSelected(FileTreePaneEvent event) {
                
                String filePath = (String) event.getValue("Path");
                Vector fileNames = (Vector) event.getValue("Filenames");
                
                processFileList(filePath, fileNames);
            }
            
            public void nodeCollapsed(FileTreePaneEvent event) {}
            public void nodeExpanded(FileTreePaneEvent event) {}
        }
    }

	@Override
	public String getAnnotationFilePath() {
		// TODO Auto-generated method stub
		return null;
	}
}