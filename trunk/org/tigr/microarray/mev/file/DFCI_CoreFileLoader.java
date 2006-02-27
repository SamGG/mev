/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: DFCI_CoreFileLoader.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-27 16:47:54 $
 * $Author: wwang67 $
 * $Revision: 1.4 $
 * $Date: 2006-02-27 16:47:54 $
 * $Author: wwang67 $
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
//import org.tigr.microarray.mev.GCOSSlideDataElement;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;

public class DFCI_CoreFileLoader extends ExpressionFileLoader {

    private GBA gba;
    private DFCI_CoreFileLoaderPanel aflp;

    private boolean loadEnabled = false;
    private File refChipFile;// = new File(".", "Data/");
    private String mode = "";
    private File [] files;
    private int affyDataType = IData.DATA_TYPE_AFFY_ABS;

    public DFCI_CoreFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        aflp = new DFCI_CoreFileLoaderPanel();
    }

    public Vector loadExpressionFiles() throws IOException {

        Object[] affymetrixFiles = aflp.getAffymetrixSelectedListModel().toArray();
        Object[] refFiles = aflp.getRefSelectedListModel().toArray();
        String [] fieldNames = new String[3];
        fieldNames[0] = "Affy_ID";
        fieldNames[1] = "Detection";
        fieldNames[2] = "P-value";
        TMEV.setFieldNames(fieldNames);
        ISlideData [] data = null;
        files = new File[affymetrixFiles.length];
        for(int j = 0; j < affymetrixFiles.length ; j++)
            files[j] = (File)affymetrixFiles[j];

        if(aflp.absoluteRadioButton.isSelected()){
            data = loadAffyAbsolute(files);
            this.affyDataType = IData.DATA_TYPE_AFFY_ABS;
        } else if (aflp.absMeanRadioButton.isSelected()){
              data = loadAffyAbsMean(files);
              this.affyDataType = IData.DATA_TYPE_AFFY_MEAN;
        } else if ( aflp.referenceRadioButton.isSelected()){
            DefaultListModel list = aflp.getRefSelectedListModel();
            DefaultListModel dataList = aflp.getAffymetrixSelectedListModel();
            if(list.getSize() < 1)
                return null;
            if(list.getSize() >= 1){
                if(dataList.size() < 1)
                    return null;
                File refFile = (File)(list.get(0));
                File [] dataFiles = toFileArray(dataList.toArray());
                data = loadAffyReference(refFile, dataFiles);
                this.affyDataType = IData.DATA_TYPE_AFFY_REF;
            }
        }
        Vector carrier = new Vector();
        if(data != null){
            TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
            for(int i = 0; i < data.length; i++)
                carrier.add(data[i]);
        }
        return carrier;
    }

    // converter
    private File [] toFileArray(Object [] files){
        File [] dataFiles = new File[files.length];
        for(int i = 0; i < files.length; i++)
            dataFiles[i] = (File)files[i];
        return dataFiles;
    }

    // Affy_ID:Detection:Description

    public ISlideData loadExpressionFile(File currentFile) throws IOException {

        return null;
    }

    public Vector loadReferenceFile(File currentFile) throws IOException {

        return new Vector();
    }

    private void setAffyDataType(int type){
        this.affyDataType = type;
    }

    public int getAffyDataType(){
        return this.affyDataType;
    }


    /**
     * Loads microarrays data compared to a specified ref chip
     */
    private ISlideData[] loadAffyReference(File refFile, File [] files) throws IOException {
        if (files.length < 1) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new FileReader(refFile));
        int numTokens = 0;
        String[] headfields=new String[4];
        headfields[0]="Probe Set Name";
        headfields[1]="Signal";
        headfields[2]="Detection";
        headfields[3]="Detection p-value";
        int[] tag=new int[4];
        
        StringSplitter ss = new StringSplitter((char)'\t');
        int countOfLines = getCountOfLines(refFile)-3;//two headers and one empty line

        // ref values
        float[] refSignals = new float[countOfLines];
        String currentLine;
        int index  = 0;
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        
        ss.init(currentLine);
        
        //countTokens returns the number of delimiters, not neccessarily num of fields
        numTokens = ss.countTokens();
        int m=0,k=0;
       
        for(k=0;k<numTokens;k++){  
        	if(m<4){
        		if(ss.nextToken().equals(headfields[m])){
        			tag[m]=k;
        			m++; 			
        		}
        	}
        }
        
        while ((currentLine = reader.readLine()) != null) {
        	setFileProgress(index);
            ss.init(currentLine);
            m=0;
            
            String tmp="";
            for(int j=0;j<tag[3]+1;j++){
            	tmp=ss.nextToken();
            	if(j==tag[m]){
            		if(m==1){
                        refSignals[index] = (new Float(tmp)).floatValue();
                        index++;
            		}
            		m++;
            	}
            }
        	
        }
        reader.close();

        ISlideData[] slideData = new ISlideData[files.length];
        countOfLines = 0;
        int numOfProbesets = 0;
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-3;
            }

            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();
            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);
            }
        }

        // set ref values
        for (int i = 0; i < files.length; i++){
            for (int j = 0; j < numOfProbesets; j++){
                slideData[i].setIntensities(j, refSignals[j],  slideData[i].getCY5(j));
            }
        }
        return slideData;
    }

    // loads abs/(mean of all probesets)
    private ISlideData[] loadAffyAbsMean(File [] files) throws IOException {

       if (files.length < 1) {
           return null;
       }

       // each element is an ongoing total of the signal for that probeset
       //float totalOfSignals = 0;
       //float[] mean_signal = new float[files.length];

       //float big_mean = 0;

       ISlideData[] slideData = new ISlideData[files.length];
       int countOfLines = 0;
       int numOfProbesets = 0;
       countOfLines = getCountOfLines(files[0]);
       setLinesCount(countOfLines);
       numOfProbesets = countOfLines-3; // two headers and one empty line
       float[] totalOfSignals=new float[numOfProbesets];
       
       setFilesCount(files.length);
       for (int i = 0; i < files.length; i++) {
           if (stop) {
               return null;
           }

           setFilesProgress(i);
           setRemain(files.length-i);
           setFileName(files[i].getPath());

           if (meta == null) {
               slideData[i] = loadAffySlideData(files[i]);
               meta = slideData[i].getSlideMetaData();

               for (int j = 0; j < numOfProbesets; j++){
                   totalOfSignals[j] += slideData[i].getCY5(j);
               }
           }
           else {
               slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);//countOfLines, meta);
               for (int j = 0; j < numOfProbesets; j++){
                   totalOfSignals[j] += slideData[i].getCY5(j);
               }
           }          
       }

       for (int i = 0; i <numOfProbesets ; i++){
	   totalOfSignals[i]= totalOfSignals[i]/files.length;
       }
       
       // for each slidedataelement setCY5(totalofSignals[j]/numOfFiles)
       // this will screw up data displayed if files are loaded one-by-one
       // assumptions include probesets present (not MAS (P)resent, just that it exists)  in all datafiles among many others
       for (int i = 0; i < files.length; i++){
           for (int j = 0; j < numOfProbesets; j++){
               slideData[i].setIntensities(j, totalOfSignals[j],  slideData[i].getCY5(j));
	   }
       }
       return slideData;
   }


    /**
     * Loads microarrays data.
     */
    private ISlideData[] loadAffyAbsolute(File [] files) throws IOException {

        if (files.length < 1) {
            return null;
        }

        ISlideData[] slideData = new ISlideData[files.length];
        int countOfLines = 0;
        int numOfProbesets = 0;
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-3;//two headers and one empty line
            }


            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();
            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);
            }
        }
        return slideData;
    }


    private ISlideData loadAffySlideData(final File file) throws IOException {

        AffySlideDataElement slideDataElement;
        String currentLine;
        
        String[] headfields=new String[4];
        headfields[0]="Probe Set Name";
        headfields[1]="Signal";
        headfields[2]="Detection";
        headfields[3]="Detection p-value";
        int[] tag=new int[4];
        
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(0);
            TMEV.setNameIndex(3);
            TMEV.setIndicesAdjusted(true);
        }

        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        float cy3, cy5;
        
        String[] moreFields  = new String[3];
        String detection="";
   
        float[] intensities = new float[2];

        int maxRows = getCountOfLines(this.files[0]);
        int maxColumns = 1;

        SlideData slideData = new SlideData(maxRows, maxColumns);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int curpos = 0;
        int row = 1;
        int column = 1;

        int numTokens = 0;

        StringSplitter ss = new StringSplitter((char)'\t');
        
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        
        ss.init(currentLine);
        
        //countTokens returns the number of delimiters, not neccessarily num of fields
        numTokens = ss.countTokens();
        int m=0,k=0;
        for(k=0;k<numTokens;k++){  
        	if(m<4){
        		if(ss.nextToken().equals(headfields[m])){
        			tag[m]=k;
        			m++; 			
        		}
        	}
        }
        while ((currentLine = reader.readLine()) != null) {
           
            setFileProgress(curpos++);
            ss = new StringSplitter((char)'\t');
            ss.init(currentLine);
            rows[0] = rows[2] = row++;
            columns[0] = columns[2] = column;
            m=0;
            String tmp="";
            for(int i=0;i<tag[3]+1;i++){
            	tmp=ss.nextToken();
            	if(i==tag[m]){
            		if(m==0)
            				moreFields[0]=tmp;
            		else if(m==1){
            				intensities[0] = 1.0f;
            				intensities[1] = (new Float(tmp)).floatValue();
            		}else if(m==2){
            				detection = tmp;
            				moreFields[1]=tmp;
            		}else if(m==3){
            				moreFields[2]=tmp;
            		}
            		m++;
            		}
            	
            }
          
            slideDataElement = new AffySlideDataElement(String.valueOf(curpos), rows, columns, intensities, moreFields);

            slideDataElement.setDetection(detection);
            slideDataElement.setPvalue(new Float(moreFields[2]).floatValue());
            slideData.addSlideDataElement(slideDataElement);

        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }



    private ISlideData loadAffyFloatSlideData(final File file, final int countOfLines, ISlideMetaData slideMetaData) throws IOException {

        //final int coordinatePairCount = TMEV.getCoordinatePairCount()*2;
        final int intensityCount = 2; //TMEV.getIntensityCount();
        int numTokens = 0;
        String[] headfields=new String[4];
        headfields[0]="Probe Set Name";
        headfields[1]="Signal";
        headfields[2]="Detection";
        headfields[3]="Detection p-value";
        float pvalue=0.0f;
        int[] tag=new int[4];
        
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(0);
            TMEV.setNameIndex(3);
            TMEV.setIndicesAdjusted(true);
        }
        FloatSlideData slideData;               

        slideData = new FloatSlideData(slideMetaData);
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);

        String currentLine;
        StringSplitter ss = new StringSplitter((char)0x09);
        float[] intensities = new float[2];
        String detection="";
        int index  = 0;
        
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        currentLine = reader.readLine();
        
        ss.init(currentLine);
        
        //countTokens returns the number of delimiters, not neccessarily num of fields
        numTokens = ss.countTokens();
        int m=0,k=0;
        for(k=0;k<numTokens;k++){
        	if(m<4){
        		if(ss.nextToken().equals(headfields[m])){
        			tag[m]=k;
        			m++;
        		}
        	}
        }
        
        while ((currentLine = reader.readLine()) != null) {
            
            setFileProgress(index);
            ss.init(currentLine);
            m=0;
            
            String tmp="";
            for(int j=0;j<tag[3]+1;j++){
            	tmp=ss.nextToken();
            	if(j==tag[m]){
            		if(m==1){
            			intensities[0] = 1.0f;
                        intensities[1] = (new Float(tmp)).floatValue();
            		}else if(m==2){
            			detection = tmp;
            		}else if(m==3){
            			pvalue=(new Float(tmp)).floatValue();
            		}
            		m++;
            	}
            }
            
            slideData.setIntensities(index, intensities[0], intensities[1]);
            slideData.setDetection(index, detection);
            slideData.setPvalue(index,pvalue);
            index++;
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }

    public float get_Median( ArrayList float_array ) {

        Collections.sort(float_array);

        Float median;

        if (float_array.size() == 1){
            return ( (Float) float_array.get(0)).floatValue();
        }

        int center = float_array.size() / 2;

        if (float_array.size() % 2 == 0) {
            Float a, b;
            a = (Float) float_array.get(center);
            b = (Float) float_array.get(center - 1);
            median = new Float(( a.floatValue() + b.floatValue() )/2);
        }
        else {
            median = (Float)float_array.get( center );
        }
        return median.floatValue();
    }



    public FileFilter getFileFilter() {

        FileFilter affymetrixFileFilter = new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt") || f.getName().endsWith(".TXT") ) return true;
                else return false;
            }

            public String getDescription() {
                return "Affymetrix Data Files (*.txt)";
            }
        };

        return affymetrixFileFilter;
    }

    public boolean checkLoadEnable() {
        setLoadEnabled(loadEnabled);
        return this.loadEnabled;
    }

    public void markLoadEnabled(boolean state) {
        loadEnabled = state;
        setLoadEnabled(loadEnabled);
        // checkLoadEnable();
    }

    public JPanel getFileLoaderPanel() {
        return aflp;
    }

    public void processFileList(String filePath, Vector fileNames) {

        aflp.setPath(filePath);

        if (fileNames == null) return; // Don't process files if there aren't any

        FileFilter affymetrixFileFilter = getFileFilter();

        aflp.getAffymetrixAvailableListModel().clear();
        aflp.getRefAvailableListModel().clear();

        for (int i = 0; i < fileNames.size(); i++) {

            File targetFile = new File((String) fileNames.elementAt(i));

            if (affymetrixFileFilter.accept(targetFile)) {
                aflp.getAffymetrixAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
                aflp.getRefAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }

    public String getFilePath() {
        if(this.aflp.getAffymetrixSelectedListModel().getSize() <1)
            return null;
        return ((File)(aflp.getAffymetrixSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
        this.aflp.openDataPath();
    }
    
/*
//
//	DFCI_CoreFileLoader - Internal Classes
//
 */

    private class DFCI_CoreFileLoaderPanel extends JPanel {

        FileTreePane fileTreePane;
        JTextField pathTextField;

        JPanel affymetrixSelectionPanel;
        JPanel affymetrixListPanel;
        JLabel affymetrixAvailableLabel;
        JLabel affymetrixSelectedLabel;
        JList affymetrixAvailableList;
        JList affymetrixSelectedList;
        JScrollPane affymetrixAvailableScrollPane;
        JScrollPane affymetrixSelectedScrollPane;
        JButton affymetrixAddButton;
        JButton affymetrixAddAllButton;
        JButton affymetrixRemoveButton;
        JButton affymetrixRemoveAllButton;
        JPanel affymetrixButtonPanel;

        JPanel refSelectionPanel;
        ButtonGroup optionsButtonGroup;
        JRadioButton absoluteRadioButton;

        /*
        JRadioButton meanRadioButton;
        JRadioButton medianRadioButton;
        */

        JRadioButton absMeanRadioButton;

        JRadioButton referenceRadioButton;
        JPanel refListPanel;
        JLabel refAvailableLabel;
        JLabel refSelectedLabel;
        JList refAvailableList;
        JList refSelectedList;
        JScrollPane refAvailableScrollPane;
        JScrollPane refSelectedScrollPane;
        JButton refAddButton;
        JButton refAddAllButton;
        JButton refRemoveButton;
        JButton refRemoveAllButton;
        JPanel refButtonPanel;
        JTextField refFieldsTextField;

        JPanel selectionPanel;
        JSplitPane splitPane;
        JPanel fileLoaderPanel;

        public DFCI_CoreFileLoaderPanel() {

            setLayout(new GridBagLayout());

            fileTreePane = new FileTreePane(SuperExpressionFileLoader.DATA_PATH);
            fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());
            fileTreePane.setPreferredSize(new java.awt.Dimension(200, 50));

            pathTextField = new JTextField();
            pathTextField.setEditable(false);
            pathTextField.setBorder(new TitledBorder(new EtchedBorder(), "Selected Path"));
            pathTextField.setForeground(Color.black);
            pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));

            affymetrixSelectionPanel = new JPanel();
            affymetrixSelectionPanel.setLayout(new GridBagLayout());
            affymetrixSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), getFileFilter().getDescription()));

            affymetrixAvailableLabel = new JLabel("Available");
            affymetrixSelectedLabel = new JLabel("Selected");
            affymetrixAvailableList = new JList(new DefaultListModel());
            affymetrixAvailableList.setCellRenderer(new ListRenderer());
            affymetrixSelectedList = new JList(new DefaultListModel());
            affymetrixSelectedList.setCellRenderer(new ListRenderer());
            affymetrixAvailableScrollPane = new JScrollPane(affymetrixAvailableList);
            affymetrixSelectedScrollPane = new JScrollPane(affymetrixSelectedList);
            affymetrixAddButton = new JButton("Add");
            affymetrixAddButton.addActionListener(new EventHandler());
            affymetrixAddAllButton = new JButton("Add All");
            affymetrixAddAllButton.addActionListener(new EventHandler());
            affymetrixRemoveButton = new JButton("Remove");
            affymetrixRemoveButton.addActionListener(new EventHandler());
            affymetrixRemoveAllButton = new JButton("Remove All");
            affymetrixRemoveAllButton.addActionListener(new EventHandler());

            Dimension largestAffymetrixButtonSize = affymetrixRemoveAllButton.getPreferredSize();
            affymetrixAddButton.setPreferredSize(largestAffymetrixButtonSize);
            affymetrixAddAllButton.setPreferredSize(largestAffymetrixButtonSize);
            affymetrixRemoveButton.setPreferredSize(largestAffymetrixButtonSize);
            affymetrixRemoveAllButton.setPreferredSize(largestAffymetrixButtonSize);

            affymetrixButtonPanel = new JPanel();
            affymetrixButtonPanel.setLayout(new GridBagLayout());

            gba.add(affymetrixButtonPanel, affymetrixAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixButtonPanel, affymetrixAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixButtonPanel, affymetrixRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixButtonPanel, affymetrixRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            affymetrixListPanel = new JPanel();
            affymetrixListPanel.setLayout(new GridBagLayout());

            gba.add(affymetrixListPanel, affymetrixAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixListPanel, affymetrixSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixListPanel, affymetrixAvailableScrollPane, 0, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixListPanel, affymetrixButtonPanel, 1, 1, 1, 4, 0, 1, GBA.V, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(affymetrixListPanel, affymetrixSelectedScrollPane, 2, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            gba.add(affymetrixSelectionPanel, affymetrixListPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            refSelectionPanel = new JPanel();
            refSelectionPanel.setLayout(new GridBagLayout());
            refSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Affymetrix Data Options"));

            optionsButtonGroup = new ButtonGroup();

            absoluteRadioButton = new JRadioButton("Absolute", true);
            absoluteRadioButton.addActionListener(new EventHandler());
            optionsButtonGroup.add(absoluteRadioButton);

            /* pcahan -- move functions to adjust data menu
            meanRadioButton = new JRadioButton("Mean Intensity");
            meanRadioButton.addActionListener(new EventHandler());
            optionsButtonGroup.add(meanRadioButton);
            medianRadioButton = new JRadioButton("Median Intensity");
            medianRadioButton.addActionListener(new EventHandler());
            optionsButtonGroup.add(medianRadioButton);
            */

            absMeanRadioButton = new JRadioButton("Absolute/Mean Intensity");
            absMeanRadioButton.addActionListener(new EventHandler());
            optionsButtonGroup.add(absMeanRadioButton);

            referenceRadioButton = new JRadioButton("Reference (Select reference files below)");
            referenceRadioButton.addActionListener(new EventHandler());
            optionsButtonGroup.add(referenceRadioButton);

            refAvailableLabel = new JLabel("Available");
            refSelectedLabel = new JLabel("Selected");
            refAvailableList = new JList(new DefaultListModel());
            refAvailableList.setCellRenderer(new ListRenderer());
            refSelectedList = new JList(new DefaultListModel());
            refSelectedList.setCellRenderer(new ListRenderer());
            refAvailableScrollPane = new JScrollPane(refAvailableList);
            refSelectedScrollPane = new JScrollPane(refSelectedList);
            refAddButton = new JButton("Add");
            refAddButton.addActionListener(new EventHandler());
            refAddAllButton = new JButton("Add All");
            refAddAllButton.addActionListener(new EventHandler());
            refRemoveButton = new JButton("Remove");
            refRemoveButton.addActionListener(new EventHandler());
            refRemoveAllButton = new JButton("Remove All");
            refRemoveAllButton.addActionListener(new EventHandler());

            Dimension largestRefButtonSize = refRemoveAllButton.getPreferredSize();
            refAddButton.setPreferredSize(largestRefButtonSize);
            refAddAllButton.setPreferredSize(largestRefButtonSize);
            refRemoveButton.setPreferredSize(largestRefButtonSize);
            refRemoveAllButton.setPreferredSize(largestRefButtonSize);

            refButtonPanel = new JPanel();
            refButtonPanel.setLayout(new GridBagLayout());

            this.affymetrixAddAllButton.setFocusPainted(false);
            this.affymetrixAddButton.setFocusPainted(false);
            this.affymetrixRemoveAllButton.setFocusPainted(false);
            this.affymetrixRemoveButton.setFocusPainted(false);

            this.refAddAllButton.setFocusPainted(false);
            this.refAddButton.setFocusPainted(false);
            this.refRemoveAllButton.setFocusPainted(false);
            this.refRemoveButton.setFocusPainted(false);

            this.referenceRadioButton.setFocusPainted(false);
            this.absoluteRadioButton.setFocusPainted(false);
            this.absMeanRadioButton.setFocusPainted(false);

            /* pc
            this.meanRadioButton.setFocusPainted(false);
            this.medianRadioButton.setFocusPainted(false);
           */

            gba.add(refButtonPanel, refAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refButtonPanel, refAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refButtonPanel, refRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refButtonPanel, refRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            refListPanel = new JPanel();
            refListPanel.setLayout(new GridBagLayout());
            refListPanel.setBorder(new TitledBorder(new EtchedBorder(), "Select Reference Files"));

            gba.add(refListPanel, refAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refListPanel, refSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refListPanel, refAvailableScrollPane, 0, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refListPanel, refButtonPanel, 1, 1, 1, 4, 0, 1, GBA.V, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(refListPanel, refSelectedScrollPane, 2, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            gba.add(refSelectionPanel, absoluteRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
            gba.add(refSelectionPanel, referenceRadioButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
            gba.add(refSelectionPanel, absMeanRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);

            /* pc
            gba.add(refSelectionPanel, meanRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);
            gba.add(refSelectionPanel, medianRadioButton, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);
            */

          gba.add(refSelectionPanel, refListPanel, 0, 4, 2, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 0, 5), 0, 0);

            selectionPanel = new JPanel();
            selectionPanel.setLayout(new GridBagLayout());
            gba.add(selectionPanel, pathTextField, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, affymetrixSelectionPanel, 0, 1, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, refSelectionPanel, 0, 3, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreePane, selectionPanel);

            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            gba.add(fileLoaderPanel, splitPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        }

        public void setPath(String path) {
            pathTextField.setText(path);
        }
        
        public void openDataPath(){
           this.fileTreePane.openDataPath();
        }
        
        public void validateLists() {

            // Currently, a minimum of one Affymetrix file must be selected to enable loading.
            // If the reference option is selected, a minimum of one Affymetrix file must also
            // be chosen as a reference.

            if (((DefaultListModel) affymetrixSelectedList.getModel()).size() > 0) {
                if (referenceRadioButton.isSelected()) {
                    if (((DefaultListModel) refSelectedList.getModel()).size() > 0) {
                        markLoadEnabled(true);
                    } else {
                        markLoadEnabled(false);
                    }
                } else {
                    markLoadEnabled(true);
                }
            } else {
                markLoadEnabled(false);
            }
        }

        public void onAffymetrixAdd() {
            int[] chosenIndices = affymetrixAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];

            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }

            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) affymetrixSelectedList.getModel()).addElement(chosenObjects[i]);
            }

            validateLists();
        }

        public void onAffymetrixAddAll() {
            int elementCount = ((DefaultListModel) affymetrixAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) affymetrixSelectedList.getModel()).addElement(addItem);
            }

            validateLists();
        }

        public void onAffymetrixRemove() {
            int[] chosenIndices = affymetrixSelectedList.getSelectedIndices();

            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) affymetrixSelectedList.getModel()).remove(chosenIndices[i]);
            }

            validateLists();
        }

        public void onAffymetrixRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) affymetrixSelectedList.getModel()).removeAllElements();

            validateLists();
        }

        public void onRefAdd() {
            int[] chosenIndices = refAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];

            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) refAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) refAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }

            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) refSelectedList.getModel()).addElement(chosenObjects[i]);
            }

            validateLists();
        }

        public void onRefAddAll() {
            int elementCount = ((DefaultListModel) refAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) refAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) refSelectedList.getModel()).addElement(addItem);
            }

            validateLists();
        }

        public void onRefRemove() {
            int[] chosenIndices = refSelectedList.getSelectedIndices();

            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) refSelectedList.getModel()).remove(chosenIndices[i]);
            }

            validateLists();
        }

        public void onRefRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) refSelectedList.getModel()).removeAllElements();

            validateLists();
        }

        public DefaultListModel getAffymetrixAvailableListModel() {
            return (DefaultListModel) affymetrixAvailableList.getModel();
        }

        public DefaultListModel getRefAvailableListModel() {
            return (DefaultListModel) refAvailableList.getModel();
        }

        public DefaultListModel getAffymetrixSelectedListModel() {
            return (DefaultListModel) affymetrixSelectedList.getModel();
        }

        public DefaultListModel getRefSelectedListModel() {
            return (DefaultListModel) refSelectedList.getModel();
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

                if (source == affymetrixAddButton) {
                    onAffymetrixAdd();
                } else if (source == affymetrixAddAllButton) {
                    onAffymetrixAddAll();
                } else if (source == affymetrixRemoveButton) {
                    onAffymetrixRemove();
                } else if (source == affymetrixRemoveAllButton) {
                    onAffymetrixRemoveAll();
                } else if (source == refAddButton) {
                    onRefAdd();
                } else if (source == refAddAllButton) {
                    onRefAddAll();
                } else if (source == refRemoveButton) {
                    onRefRemove();
                } else if (source == refRemoveAllButton) {
                    onRefRemoveAll();
                } else if (source instanceof JRadioButton){
                    aflp.validateLists();  //check state
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
}
