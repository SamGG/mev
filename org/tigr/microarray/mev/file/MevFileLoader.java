/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: MevFileLoader.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 20:52:48 $
 * $Author: eleanorahowe $
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
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.file.AnnFileParser;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.SpotInformationData;

public class MevFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private MevFileLoaderPanel mflp;
    
    private boolean loadEnabled = false;
    private String [] uidArray;
    
    boolean haveSRandSC;
    boolean loadMedianIntensities = false;
    
    private boolean haveAnnMatch = false;
    
    public MevFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        mflp = new MevFileLoaderPanel();
        mflp.splitPane.setDividerLocation(0.6);
        mflp.validate();
    }
    
    public Vector loadExpressionFiles() throws IOException {

        Object[] mevFiles = mflp.getMevSelectedListModel().toArray();
        Object[] annFiles = mflp.getAnnSelectedListModel().toArray();
        Vector data = new Vector();
        ISlideMetaData metaData = null;
        ISlideData slideData;
        
        this.loadMedianIntensities = mflp.loadMedButton.isSelected();
        
        setFilesCount(mevFiles.length);
        setRemain(mevFiles.length);

        for (int i = 0; i < mevFiles.length; i++) {
            setFileName(((File) mevFiles[i]).getName());
            if(i == 0){
                slideData = loadSlideData((File) mevFiles[i]);
                if(slideData == null)
                    return null;
                data.add(slideData);
                metaData = slideData.getSlideMetaData();
            }
            else
                data.add(loadFloatSlideData((File) mevFiles[i], metaData));
            
            setRemain(mevFiles.length-i-1);
            setFilesProgress(i);
        }
        if(!mflp.noAnnFileBox.isSelected()) {
            for (int i = 0; i < annFiles.length; i++){
                loadAnnotationFile((SlideData)data.elementAt(0), (File)annFiles[i]);
            }
        } else {
            loadAnnotationFromMevFile((File)mevFiles[0], (SlideData)data.elementAt(0));
        }
        
        //check for existance of annotation matches, just set to true if ann. is from mev file
        //or if no ann. files are selected.  haveAnnMatch is set when loading files to indicate a match
        if(mflp.noAnnFileBox.isSelected() || annFiles == null || annFiles.length == 0)
            haveAnnMatch = true;
        
        if(!haveAnnMatch) {
            String msg = "The selected annoation file";
            if(annFiles != null && annFiles.length > 1)
                msg += "s";
            
            msg += " did not have have UID's that matched any UID's \n";
            msg += "in the selected mev files.";
            JOptionPane.showMessageDialog(mflp, msg, "Annotation Mismatch Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        return data;
    }
    
    //not called
    public ISlideData loadExpressionFile(File file){
        return null;
    }
    
    
    public void loadAnnotationFromMevFile(File file, SlideData data) {
        MevParser mfp = new MevParser();
        mfp.loadFile(file);
        
        SpotInformationData annot = mfp.getSpotInformation();
        int length = annot.getSize();
        
        String [] header = annot.getSpotInformationHeader();
        //EH fieldnames are loaded into SlideData instead of TMEV
        data.getSlideMetaData().appendFieldNames(header);
//      Vector v = new Vector();
//      for(int i = 0; i < header.length; i++) {
//          v.add(header[i]);
//      }
        //setTMEVFieldNames(v);
        
        for(int i = 0; i < length; i++){
            ((SlideDataElement)data.getSlideDataElement(i)).setExtraFields(annot.getSpotInformationArray(i));
        }
    }
    
    public ISlideData loadSlideData(File currentFile) throws IOException {
        SlideData slideData = null;
        MevParser mfp = new MevParser();
        mfp.loadFile(currentFile);
        if (mfp.isMevFileLoaded()) {
            Vector headers = mfp.getColumnHeaders();
            
            //locate intensity columns
            int i1, i2;
            i1 = getIntensityColumn(headers, 1);
            i2 = getIntensityColumn(headers, 2);
            
            //Intensities exist??
            if(i1 == -1 || i2 == -1) {
                if(loadMedianIntensities)
                    JOptionPane.showMessageDialog(mflp, "Error loading "+currentFile.getName()+"\n"
                    + "The file was missing median intensity columns indicated by\n"
                    + "the header names MedA and MedB", "Load Error", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(mflp, "Error loading "+currentFile.getName()+"\n"
                    + "The file was missing intensity columns indicated by\n"
                    + "the header names IA and IB", "Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            //test for optional SR and SC
            haveSRandSC = false;
            if(headers.size() > 7) {
                String possibleSR = (String)(headers.elementAt(7));
                String possibleSC = (String)(headers.elementAt(8));
                if(possibleSR.equals("SR") && possibleSC.equals("SC"))
                    haveSRandSC = true;
            }
            
            String [][] data = mfp.getDataMatrix();
            SlideDataElement sde;
            int [] rows;
            int [] cols;
            float [] intensities;
            String uid;
            String [][] spotData;
            uidArray = new String[data.length];
            
            int maxRow = 0;
            int maxCol = 0;
            for(int i = 0; i < data.length; i++){
                maxRow = Math.max(maxRow, Integer.parseInt(data[i][3]));
                maxCol = Math.max(maxCol, Integer.parseInt(data[i][4]));
            }
            slideData = new SlideData(maxRow, maxCol);
            setLinesCount(data.length);
            for(int i = 0; i < data.length; i++){
                rows = new int[3];
                cols = new int[3];
                intensities = new float[2];
                
                uidArray[i] = data[i][0];
                
                try {
                    intensities[0] = Float.parseFloat(data[i][i1]);
                    intensities[1] = Float.parseFloat(data[i][i2]);
                    rows[0] = Integer.parseInt(data[i][3]);
                    cols[0] = Integer.parseInt(data[i][4]);
                    rows[1] = Integer.parseInt(data[i][5]);
                    cols[1] = Integer.parseInt(data[i][6]);
                    if(haveSRandSC) {
                        rows[2] = Integer.parseInt(data[i][7]);
                        cols[2] = Integer.parseInt(data[i][8]);
                    } else {
                        rows[2] = 0;
                        cols[2] = 0;
                    }
                } catch (NumberFormatException e) {
                    final String fileName = currentFile.getName();
                    final int loc = i;
                    Thread thread = new Thread( new Runnable() {
                        public void run() {
                            JOptionPane.showConfirmDialog(mflp, "The input file \""+fileName+"\" was missing critical information on line # "+ String.valueOf(loc+1) + "\n" +
                            "MeV files require entries for UID, Intensities, and slide location information.", "Loading Aborted/Loading Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    thread.start();
                    return null;
                }
                sde = new SlideDataElement(data[i][0], rows, cols, intensities, null);
                slideData.add(sde);
                setFileProgress(i);
            }
            
            if(mflp.saveSpotInfoBox.isSelected() && !mflp.noAnnFileBox.isSelected())
                slideData.setSpotInformationData(mfp.getSpotInformation());
            
            slideData.setSlideDataName(currentFile.getName());
            slideData.setSlideFileName(currentFile.getPath());
        }
        return slideData;
    }
    
    
    private int getIntensityColumn(Vector headers, int index) {
        String headerKey;
        int col = -1;
        
        if(loadMedianIntensities) {
            if(index == 1)
                headerKey = "MedA";
            else
                headerKey = "MedB";
            
        } else {
            if(index == 1)
                headerKey = "IA";
            else
                headerKey = "IB";
        }
        
        for(int i = 0; i < headers.size(); i++) {
            if(((String)(headers.elementAt(i))).equals(headerKey)) {
                col = i;
                break;
            }
        }
        return col;
    }
    
    
    public ISlideData loadFloatSlideData(File currentFile, ISlideMetaData metaData) throws IOException {
        
        MevParser mfp = new MevParser();
        mfp.loadFile(currentFile);
        FloatSlideData slideData = new FloatSlideData(metaData);
        if (mfp.isMevFileLoaded()) {
            
            Vector headers = mfp.getColumnHeaders();
            
            //locate intensity columns
            int i1, i2;
            i1 = getIntensityColumn(headers, 1);
            i2 = getIntensityColumn(headers, 2);
            
            //Intensities exist??
            if(i1 == -1 || i2 == -1) {
                if(loadMedianIntensities)
                    JOptionPane.showMessageDialog(mflp, "Error loading "+currentFile.getName()+"\n"
                    + "The file was missing median intensity columns indicated by\n"
                    + "the header names MedA and MedB", "Load Error", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(mflp, "Error loading "+currentFile.getName()+"\n"
                    + "The file was missing intensity columns indicated by\n"
                    + "the header names IA and IB", "Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            String [][] data = mfp.getDataMatrix();
            setLinesCount(data.length);
            for(int i = 0; i < data.length; i++){
                slideData.setIntensities(i, Float.parseFloat(data[i][i1]), Float.parseFloat(data[i][i2]));
                setFileProgress(i);
            }
            if(mflp.saveSpotInfoBox.isSelected() && !mflp.noAnnFileBox.isSelected())
                slideData.setSpotInformationData(mfp.getSpotInformation());
        }
        slideData.setSlideDataName(currentFile.getName());
        slideData.setSlideFileName(currentFile.getPath());
        return slideData;
    }
    
    public Vector loadAnnotationFile(SlideData targetData, File sourceFile) throws IOException {
        
        AnnFileParser parser = new AnnFileParser();
        parser.loadFile(sourceFile);
        if(parser.isAnnFileLoaded()){
            Vector headers = parser.getColumnHeaders();
            
            int firstAnnField = 1;
            
            // If columns 1 and 2 (after UID) are R and C skip over and use the ann. columns that follow.
            if(headers.size() >= 3) {
                if( ((String)(headers.get(1))).equalsIgnoreCase("R") &&
                    ((String)(headers.get(2))).equalsIgnoreCase("C") )
                    firstAnnField = 3;
            }
//EH fieldnames are added into SlideData instead of TMEV.java            
            String[] annotHeaders = new String[headers.size()-firstAnnField];
            for(int i = firstAnnField; i < headers.size(); i++){
                annotHeaders[i-firstAnnField] = (String)headers.elementAt(i);
            }
            targetData.getSlideMetaData().appendFieldNames(annotHeaders);
            
            String [][] annMatrix;
            if(mflp.cutQuotesBox.isSelected())
                annMatrix = parser.getDataMatrixMinusQuotes();
            else
                annMatrix = parser.getDataMatrix();
            
            Hashtable hash = new Hashtable();
            String [] value;
            int dataLength = targetData.size();
            for(int i = 0; i < annMatrix.length; i++){
                value = new String[annMatrix[i].length-firstAnnField];
                System.arraycopy(annMatrix[i], firstAnnField, value, 0, annMatrix[i].length-firstAnnField);
                hash.put(annMatrix[i][0], value);
            }
            
            SlideDataElement sde;
            String [] extraFields;
            for(int i = 0; i < dataLength; i++){
                extraFields = (String [])(hash.get(uidArray[i]));
                //if there is a match in the annotation set flag to true
                if(!haveAnnMatch && extraFields != null)
                    haveAnnMatch = true;
                ((SlideDataElement)targetData.getSlideDataElement(i)).setExtraFields(extraFields);
            }
        }
        return new Vector();
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter mevFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".mev")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "MeV Expression Files (*.mev)";
            }
        };
        
        return mevFileFilter;
    }
    
    public FileFilter getAnnotationFileFilter() {
        
        FileFilter annFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".ann")) return true;
                if (f.getName().endsWith(".dat")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "TIGR MeV Annotation Files (*.ann, *.dat)";
            }
        };
        
        return annFileFilter;
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
        return mflp;
    }
    
    public void processFileList(String filePath, Vector fileNames) {
        
        mflp.setPath(filePath);
        
        if (fileNames == null) return; // Don't process files if there aren't any
        
        FileFilter mevFileFilter = getFileFilter();
        FileFilter annFileFilter = getAnnotationFileFilter();
        
        mflp.getMevAvailableListModel().clear();
        mflp.getAnnAvailableListModel().clear();
        
        for (int i = 0; i < fileNames.size(); i++) {
            
            File targetFile = new File((String) fileNames.elementAt(i));
            
            if (mevFileFilter.accept(targetFile)) {
                mflp.getMevAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            } else if (annFileFilter.accept(targetFile)) {
                mflp.getAnnAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }
    
    public String getFilePath() {
        if(this.mflp.getMevSelectedListModel().getSize() <1)
            return null;
        return ((File)(mflp.getMevSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
        this.mflp.splitPane.setDividerLocation(0.3);
        this.mflp.openDataPath();
    }
    
    
/*
//
//	MevFileLoader - Internal Classes
//
 */
    
    private class MevFileLoaderPanel extends JPanel {
        
        FileTreePane fileTreePane;
        JTextField pathTextField;
        
        JPanel mevSelectionPanel;
        JPanel mevListPanel;
        JLabel mevAvailableLabel;
        JLabel mevSelectedLabel;
        JList mevAvailableList;
        JList mevSelectedList;
        JScrollPane mevAvailableScrollPane;
        JScrollPane mevSelectedScrollPane;
        JButton mevAddButton;
        JButton mevAddAllButton;
        JButton mevRemoveButton;
        JButton mevRemoveAllButton;
        JPanel mevButtonPanel;
        
        JPanel annSelectionPanel;
        JPanel annListPanel;
        JLabel annAvailableLabel;
        JLabel annSelectedLabel;
        JList annAvailableList;
        JList annSelectedList;
        JScrollPane annAvailableScrollPane;
        JScrollPane annSelectedScrollPane;
        JButton annAddButton;
        JButton annAddAllButton;
        JButton annRemoveButton;
        JButton annRemoveAllButton;
        JPanel annButtonPanel;
        JTextField annFieldsTextField;
        
        JPanel selectionPanel;
        JSplitPane splitPane;
        JPanel fileLoaderPanel;
        
        JCheckBox noAnnFileBox;
        JCheckBox saveSpotInfoBox;
        JCheckBox cutQuotesBox;
        
        JRadioButton loadIButton;
        JRadioButton loadMedButton;
        
        public MevFileLoaderPanel() {
            
            setLayout(new GridBagLayout());
            
            fileTreePane = new FileTreePane(SuperExpressionFileLoader.DATA_PATH);
            fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());
            fileTreePane.setPreferredSize(new java.awt.Dimension(200, 50));
            
            pathTextField = new JTextField();
            pathTextField.setEditable(false);
            pathTextField.setBorder(new TitledBorder(new EtchedBorder(), "Selected Path"));
            pathTextField.setForeground(Color.black);
            pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
            
            mevSelectionPanel = new JPanel();
            mevSelectionPanel.setLayout(new GridBagLayout());
            mevSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), getFileFilter().getDescription()));
            
            mevAvailableLabel = new JLabel("Available");
            mevSelectedLabel = new JLabel("Selected");
            mevAvailableList = new JList(new DefaultListModel());
            mevAvailableList.setCellRenderer(new ListRenderer());
            mevSelectedList = new JList(new DefaultListModel());
            mevSelectedList.setCellRenderer(new ListRenderer());
            mevAvailableScrollPane = new JScrollPane(mevAvailableList);
            mevSelectedScrollPane = new JScrollPane(mevSelectedList);
            mevAddButton = new JButton("Add");
            mevAddButton.setPreferredSize(new Dimension(100,20));
            //mevAddButton.setSize(100,20);
            mevAddButton.addActionListener(new EventHandler());
            mevAddAllButton = new JButton("Add All");
            //mevAddAllButton.setSize(100,20);
            mevAddAllButton.setPreferredSize(new Dimension(100,20));
            
            mevAddAllButton.addActionListener(new EventHandler());
            mevRemoveButton = new JButton("Remove");
            //mevRemoveButton.setSize(100,20);
            mevRemoveButton.setPreferredSize(new Dimension(100,20));
            
            mevRemoveButton.addActionListener(new EventHandler());
            mevRemoveAllButton = new JButton("Remove All");
            //mevRemoveAllButton.setSize(100,20);
            mevRemoveAllButton.setPreferredSize(new Dimension(100,20));
            
            mevRemoveAllButton.addActionListener(new EventHandler());
            
            //  Dimension largestMevButtonSize = mevRemoveAllButton.getPreferredSize();
            // mevAddButton.setPreferredSize(largestMevButtonSize);
            //   mevAddAllButton.setPreferredSize(largestMevButtonSize);
            ////     mevRemoveButton.setPreferredSize(largestMevButtonSize);
            //     mevRemoveAllButton.setPreferredSize(largestMevButtonSize);
            
            mevButtonPanel = new JPanel();
            mevButtonPanel.setLayout(new GridBagLayout());
            //mevButtonPanel.setSize(110, 150);
            //mevButtonPanel.setPreferredSize(new Dimension(110, 150));
            
            gba.add(mevButtonPanel, mevAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevButtonPanel, mevAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevButtonPanel, mevRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevButtonPanel, mevRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            
            //Medians vs. Integrate intensities
            loadIButton = new JRadioButton("Load Integrated Spot Intensities", true);
            loadIButton.setFocusPainted(false);
            loadMedButton = new JRadioButton("Load Median Spot Intensities");
            loadMedButton.setFocusPainted(false);
            ButtonGroup bg = new ButtonGroup();
            bg.add(loadIButton);
            bg.add(loadMedButton);
            
            noAnnFileBox = new JCheckBox("Use Annotation Contained in MeV File (no annotation file)", false);
            noAnnFileBox.setFocusPainted(false);
            noAnnFileBox.setActionCommand("use-annotation-in-mev-file");
            noAnnFileBox.addActionListener(new EventHandler());
            
            saveSpotInfoBox = new JCheckBox("Load Auxiliary Spot Information", false);
            saveSpotInfoBox.setFocusPainted(false);
            
            cutQuotesBox = new JCheckBox("Remove Annotation Quotes(\"...\")", false);
            cutQuotesBox.setHorizontalAlignment(JCheckBox.CENTER);
            cutQuotesBox.setFocusPainted(false);
            
            selectionPanel = new JPanel();
            selectionPanel.setLayout(new GridBagLayout());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            
            gba.add(buttonPanel, loadIButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            gba.add(buttonPanel, saveSpotInfoBox, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            
            gba.add(buttonPanel, loadMedButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);
            gba.add(buttonPanel, noAnnFileBox, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 20, 0, 5), 0, 0);            

            gba.add(buttonPanel, cutQuotesBox, 0, 2, 2, 1, 1, 0, GBA.H, GBA.E, new Insets(0, 118, 0, 5), 0, 0);
            
            mevListPanel = new JPanel();
            mevListPanel.setLayout(new GridBagLayout());
            
            //            gba.add(mevSelectionPanel, selectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(mevListPanel, mevAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevAvailableScrollPane, 0, 1, 1, 4, 5, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(0,0,0,0), 0, 0);
            gba.add(mevListPanel, mevButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevSelectedScrollPane, 2, 1, 1, 4, 5, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
                        
            gba.add(mevSelectionPanel, buttonPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevSelectionPanel, mevListPanel, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
 
            gba.add(selectionPanel, pathTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, mevSelectionPanel, 0, 1, 2, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            
          /*
           
                gba.add(mevListPanel, mevAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevAvailableScrollPane, 0, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevButtonPanel, 1, 1, 1, 4, 0, 1, GBA.V, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(mevListPanel, mevSelectedScrollPane, 2, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
           
            gba.add(mevSelectionPanel, mevListPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
           */
            
            
            annSelectionPanel = new JPanel();
            annSelectionPanel.setLayout(new GridBagLayout());
            annSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), getAnnotationFileFilter().getDescription()));
            
            annAvailableLabel = new JLabel("Available");
            annSelectedLabel = new JLabel("Selected");
            annAvailableList = new JList(new DefaultListModel());
            annAvailableList.setCellRenderer(new ListRenderer());
            annSelectedList = new JList(new DefaultListModel());
            annSelectedList.setCellRenderer(new ListRenderer());
            annAvailableScrollPane = new JScrollPane(annAvailableList);
            annSelectedScrollPane = new JScrollPane(annSelectedList);
            annAddButton = new JButton("Add");
            annAddButton.addActionListener(new EventHandler());
            annAddAllButton = new JButton("Add All");
            annAddAllButton.addActionListener(new EventHandler());
            annRemoveButton = new JButton("Remove");
            annRemoveButton.addActionListener(new EventHandler());
            annRemoveAllButton = new JButton("Remove All");
            annRemoveAllButton.addActionListener(new EventHandler());
            
            Dimension buttonSize = new Dimension(100, 20);
            
            Dimension largestAnnButtonSize = annRemoveAllButton.getPreferredSize();
            annAddButton.setPreferredSize(buttonSize);
            annAddAllButton.setPreferredSize(buttonSize);
            annRemoveButton.setPreferredSize(buttonSize);
            annRemoveAllButton.setPreferredSize(buttonSize);
            
            this.mevAddAllButton.setFocusPainted(false);
            this.mevAddButton.setFocusPainted(false);
            this.mevRemoveAllButton.setFocusPainted(false);
            this.mevRemoveButton.setFocusPainted(false);
            
            this.annAddAllButton.setFocusPainted(false);
            this.annAddButton.setFocusPainted(false);
            this.annRemoveAllButton.setFocusPainted(false);
            this.annRemoveButton.setFocusPainted(false);
            
            annButtonPanel = new JPanel();
            annButtonPanel.setLayout(new GridBagLayout());
            
            gba.add(annButtonPanel, annAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annButtonPanel, annAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annButtonPanel, annRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annButtonPanel, annRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            annListPanel = new JPanel();
            annListPanel.setLayout(new GridBagLayout());
            
            gba.add(annListPanel, annAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annListPanel, annSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annListPanel, annAvailableScrollPane, 0, 1, 1, 4, 5, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(0, 0, 0, 0), 0, 0); 
            gba.add(annListPanel, annButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(annListPanel, annSelectedScrollPane, 2, 1, 1, 4, 5, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            annFieldsTextField = new JTextField();
            annFieldsTextField.setEditable(false);
            annFieldsTextField.setBorder(new TitledBorder(new EtchedBorder(), "Annotation Fields"));
            annFieldsTextField.setForeground(Color.black);
            annFieldsTextField.setFont(new Font("serif", Font.BOLD, 12));
            
            gba.add(annSelectionPanel, annListPanel, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 0, 5), 0, 0);
            gba.add(annSelectionPanel, annFieldsTextField, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 5, 0, 5), 0, 0);
            
            gba.add(selectionPanel, annSelectionPanel, 0, 3, 2, 2, 1, 1, GBA.B, GBA.C, new Insets(0, 5, 0, 5), 0, 0);
            
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreePane, selectionPanel);

            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            gba.add(fileLoaderPanel, splitPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            splitPane.setDividerLocation(0.6);     
            
        }
        
        public void setPath(String path) {
            pathTextField.setText(path);
        }
        
        public void openDataPath(){
            this.fileTreePane.openDataPath();
        }
        
        
        public void validateLists() {
            
            // Currently, a minimum of one mev file must be selected to enable loading
            
            if (((DefaultListModel) mevSelectedList.getModel()).size() > 0) {
                markLoadEnabled(true);
            } else {
                markLoadEnabled(false);
            }
        }
        
        public void onMevAdd() {
            int[] chosenIndices = mevAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];
            
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) mevAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) mevAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }
            
            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) mevSelectedList.getModel()).addElement(chosenObjects[i]);
            }
            
            validateLists();
        }
        
        public void onMevAddAll() {
            int elementCount = ((DefaultListModel) mevAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) mevAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) mevSelectedList.getModel()).addElement(addItem);
            }
            
            validateLists();
        }
        
        public void onMevRemove() {
            int[] chosenIndices = mevSelectedList.getSelectedIndices();
            
            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) mevSelectedList.getModel()).remove(chosenIndices[i]);
            }
            
            validateLists();
        }
        
        public void onMevRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) mevSelectedList.getModel()).removeAllElements();
            
            validateLists();
        }
        
        public void onAnnAdd() {
            int[] chosenIndices = annAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];
            
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) annAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) annAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }
            
            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) annSelectedList.getModel()).addElement(chosenObjects[i]);
            }
            
            validateLists();
            updateAnnFieldTextField();
        }
        
        public void onAnnAddAll() {
            int elementCount = ((DefaultListModel) annAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) annAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) annSelectedList.getModel()).addElement(addItem);
            }
            
            validateLists();
            updateAnnFieldTextField();
        }
        
        public void onAnnRemove() {
            int[] chosenIndices = annSelectedList.getSelectedIndices();
            
            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) annSelectedList.getModel()).remove(chosenIndices[i]);
            }
            
            validateLists();
            updateAnnFieldTextField();
        }
        
        public void onAnnRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) annSelectedList.getModel()).removeAllElements();
            
            validateLists();
            updateAnnFieldTextField();
        }
        
        public void updateAnnFieldTextField() {
            DefaultListModel model = (DefaultListModel) annSelectedList.getModel();
            AnnFileParser parser = new AnnFileParser();
            Vector annVector;
            String text = "";
            for(int i = 0; i < model.getSize(); i++) {
                File annFile = (File)(model.get(i));
                parser.loadFile(annFile);
                if(parser.isAnnFileLoaded()) {
                    annVector = parser.getColumnHeaders();
                    
                    if(i > 0 && annVector.size() > 3)
                        text+= ", ";
                    
                    for(int j = 3; j < annVector.size()-1; j++){
                        text += (((String)annVector.elementAt(j)))+", ";
                    }
                    text += ((String)annVector.elementAt(annVector.size()-1));
                }
            }
            this.annFieldsTextField.setText(text);
        }
        
        public void onUseMevAnn() {
            if(this.noAnnFileBox.isSelected())
                enableAnnotationPanel(false);
            else {
                enableAnnotationPanel(true);
            }
        }
        
        public void enableAnnotationPanel(boolean enable) {
            this.annAddAllButton.setEnabled(enable);
            this.annAddButton.setEnabled(enable);
            this.annRemoveAllButton.setEnabled(enable);
            this.annRemoveButton.setEnabled(enable);
            this.annAvailableLabel.setEnabled(enable);
            this.annSelectedLabel.setEnabled(enable);
            this.annAvailableList.setEnabled(enable);
            this.annSelectedList.setEnabled(enable);
            this.annFieldsTextField.setEnabled(enable);
            
            if(!enable){
                this.annAvailableList.setBackground(Color.lightGray);
                this.annSelectedList.setBackground(Color.lightGray);
            } else {
                this.annAvailableList.setBackground(Color.white);
                this.annSelectedList.setBackground(Color.white);
            }
        }
        
        public DefaultListModel getMevAvailableListModel() {
            return (DefaultListModel) mevAvailableList.getModel();
        }
        
        public DefaultListModel getAnnAvailableListModel() {
            return (DefaultListModel) annAvailableList.getModel();
        }
        
        public DefaultListModel getMevSelectedListModel() {
            return (DefaultListModel) mevSelectedList.getModel();
        }
        
        public DefaultListModel getAnnSelectedListModel() {
            return (DefaultListModel) annSelectedList.getModel();
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
                
                if (source == mevAddButton) {
                    onMevAdd();
                } else if (source == mevAddAllButton) {
                    onMevAddAll();
                } else if (source == mevRemoveButton) {
                    onMevRemove();
                } else if (source == mevRemoveAllButton) {
                    onMevRemoveAll();
                } else if (source == annAddButton) {
                    onAnnAdd();
                } else if (source == annAddAllButton) {
                    onAnnAddAll();
                } else if (source == annRemoveButton) {
                    onAnnRemove();
                } else if (source == annRemoveAllButton) {
                    onAnnRemoveAll();
                } else if (source == noAnnFileBox) {
                    onUseMevAnn();
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
    
        public class ProgressRunner implements Runnable {
        
        
        public void run() {
            progress = new SlideLoaderProgressBar(superLoader.getFrame());
        }
        
    }
}