/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GenePixFileLoader.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
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
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;

public class GenePixFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private GenePixFileLoaderPanel gpflp;    
    private boolean loadEnabled = false;
    
    public GenePixFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        gpflp = new GenePixFileLoaderPanel();
    }
    
    
    public Vector loadExpressionFiles() throws IOException {
        Object[] genePixFiles = gpflp.getGenePixSelectedListModel().toArray();
        if(genePixFiles == null || genePixFiles.length < 1)
            return null;
        Vector data = new Vector();
        ISlideMetaData meta = null;
        setFilesCount(genePixFiles.length);
        setRemain(genePixFiles.length);
        for (int i = 0; i < genePixFiles.length; i++) {
            setFileName(((File)genePixFiles[i]).getName());
            if(i == 0){
                data.add( loadSlideData((File)genePixFiles[i]) );
                meta = (ISlideMetaData)(data.elementAt(0));
            } else {
                data.add( loadFloatSlideData((File)genePixFiles[i], meta));
            }
            setFilesProgress(i+1);    
            if(i > 0 && i%10 == 0)
                java.lang.Runtime.getRuntime().gc();
        }
        if(data != null && data.size() > 0){
            String [] fieldNames = new String[2];
            fieldNames[0] = "Name";
            fieldNames[1] = "ID";
            TMEV.setFieldNames(fieldNames);
        }
        return data;
    }
    
    
    public ISlideData loadSlideData(File currentFile){
        SlideData slideData = null;
        GenepixFileParser parser = new GenepixFileParser(currentFile, false);
      //  parser.run();
        if(parser.isCompleted()){
            Vector data = parser.getTavFile();
            Vector spotData;
            ISlideDataElement sde;
            int [] rows = new int[3];
            int [] cols = new int[3];
            float [] intensity = new float[2];
            String [] moreFields = new String[2];
            int numElements = data.size();
            
            int maxRows = 0;
            int maxCols = 0;
            int currRow, currCol;
            
            for(int i = 0; i < numElements; i++){
                spotData = (Vector)(data.elementAt(i));
                maxRows = Math.max(maxRows, ((Integer)spotData.elementAt(2)).intValue());
                maxCols = Math.max(maxCols, ((Integer)spotData.elementAt(3)).intValue());
            }
                 
            slideData = new SlideData(maxRows, maxCols);
            this.setLinesCount(numElements);
            for(int i = 0; i < numElements; i++){  //start at 1 to pass header
                rows = new int[3];
                cols = new int[3];
                intensity = new float[2];
                moreFields = new String[2];
                spotData = (Vector)(data.elementAt(i));
                intensity[0] = (float)((Integer)spotData.elementAt(0)).intValue();
                intensity[1] = (float)((Integer)spotData.elementAt(1)).intValue();
                rows[0] = ((Integer)spotData.elementAt(2)).intValue();
                cols[0] = ((Integer)spotData.elementAt(3)).intValue();
                rows[1] = 0;  //no slide row or slide column provided
                cols[1] = 0;
                rows[2] = ((Integer)spotData.elementAt(4)).intValue();
                cols[2] = ((Integer)spotData.elementAt(5)).intValue();
                moreFields[0] = (String)spotData.elementAt(6);
                moreFields[1] = (String)spotData.elementAt(7);
                sde = new SlideDataElement(String.valueOf(i+1), rows, cols, intensity, moreFields);
                slideData.add(sde);
                setFileProgress(i);
            }
            slideData.setSlideDataName(currentFile.getName());
            slideData.setSlideFileName(currentFile.getPath());
        }
        return slideData;
    }
    
    
    public ISlideData loadFloatSlideData(File currentFile, ISlideMetaData meta){
        FloatSlideData slideData = null;
        float cy3, cy5;
        Vector spotData;
        
        GenepixFileParser parser = new GenepixFileParser(currentFile, false);
     //   parser.run();
        if(parser.isCompleted()){
            slideData = new FloatSlideData(meta);
            Vector data = parser.getTavFile();
            int numElements = data.size();
            setLinesCount(numElements);
            for(int i = 0; i < numElements; i++){
                spotData = (Vector)(data.elementAt(i));                
                cy3 = (float)((Integer)spotData.elementAt(0)).intValue();
                cy5 = (float)((Integer)spotData.elementAt(1)).intValue();
                slideData.setIntensities( i, cy3, cy5);
                setFileProgress(i);
            }
            slideData.setSlideDataName(currentFile.getName());
            slideData.setSlideFileName(currentFile.getPath());
        }
        return slideData;
    }
    
    
    
    public ISlideData loadExpressionFile(File f) throws IOException {
        return null;
    }
    
    
    public Vector loadAnnotationFile(File f) throws IOException {
        return new Vector();
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter genePixFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".gpr")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "GenePix Files (*.gpr)";
            }
        };
        
        return genePixFileFilter;
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
        return gpflp;
    }
    
    public void processFileList(String filePath, Vector fileNames) {
        
        gpflp.setPath(filePath);
        
        if (fileNames == null) return; // Don't process files if there aren't any
        
        FileFilter genePixFileFilter = getFileFilter();
        
        gpflp.getGenePixAvailableListModel().clear();
        
        for (int i = 0; i < fileNames.size(); i++) {
            
            File targetFile = new File((String) fileNames.elementAt(i));
            
            if (genePixFileFilter.accept(targetFile)) {
                gpflp.getGenePixAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }
    
    public String getFilePath() {
        if(this.gpflp.getGenePixSelectedListModel().getSize() < 1)
            return null;
        return ((File)(gpflp.getGenePixSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
        this.gpflp.openDataPath();
    }

    
/*
//
//	GenePixFileLoader - Internal Classes
//
 */
    
    private class GenePixFileLoaderPanel extends JPanel {
        
        FileTreePane fileTreePane;
        JTextField pathTextField;
        
        JPanel genePixSelectionPanel;
        JPanel genePixListPanel;
        JLabel genePixAvailableLabel;
        JLabel genePixSelectedLabel;
        JList genePixAvailableList;
        JList genePixSelectedList;
        JScrollPane genePixAvailableScrollPane;
        JScrollPane genePixSelectedScrollPane;
        JButton genePixAddButton;
        JButton genePixAddAllButton;
        JButton genePixRemoveButton;
        JButton genePixRemoveAllButton;
        JPanel genePixButtonPanel;
        
        JPanel selectionPanel;
        JSplitPane splitPane;
        JPanel fileLoaderPanel;
        
        public GenePixFileLoaderPanel() {
            
            setLayout(new GridBagLayout());
            
            fileTreePane = new FileTreePane(SuperExpressionFileLoader.DATA_PATH);
            fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());
            fileTreePane.setPreferredSize(new java.awt.Dimension(200, 50));
            
            pathTextField = new JTextField();
            pathTextField.setEditable(false);
            pathTextField.setBorder(new TitledBorder(new EtchedBorder(), "Selected Path"));
            pathTextField.setForeground(Color.black);
            pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
            
            genePixSelectionPanel = new JPanel();
            genePixSelectionPanel.setLayout(new GridBagLayout());
            genePixSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), getFileFilter().getDescription()));
            
            genePixAvailableLabel = new JLabel("Available");
            genePixSelectedLabel = new JLabel("Selected");
            genePixAvailableList = new JList(new DefaultListModel());
            genePixAvailableList.setCellRenderer(new ListRenderer());
            genePixSelectedList = new JList(new DefaultListModel());
            genePixSelectedList.setCellRenderer(new ListRenderer());
            genePixAvailableScrollPane = new JScrollPane(genePixAvailableList);
            genePixSelectedScrollPane = new JScrollPane(genePixSelectedList);
            genePixAddButton = new JButton("Add");
            genePixAddButton.addActionListener(new EventHandler());
            genePixAddAllButton = new JButton("Add All");
            genePixAddAllButton.addActionListener(new EventHandler());
            genePixRemoveButton = new JButton("Remove");
            genePixRemoveButton.addActionListener(new EventHandler());
            genePixRemoveAllButton = new JButton("Remove All");
            genePixRemoveAllButton.addActionListener(new EventHandler());
            
            Dimension largestGenePixButtonSize = genePixRemoveAllButton.getPreferredSize();
            genePixAddButton.setPreferredSize(largestGenePixButtonSize);
            genePixAddAllButton.setPreferredSize(largestGenePixButtonSize);
            genePixRemoveButton.setPreferredSize(largestGenePixButtonSize);
            genePixRemoveAllButton.setPreferredSize(largestGenePixButtonSize);
            
            genePixButtonPanel = new JPanel();
            genePixButtonPanel.setLayout(new GridBagLayout());
            
            gba.add(genePixButtonPanel, genePixAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixButtonPanel, genePixAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixButtonPanel, genePixRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixButtonPanel, genePixRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            genePixListPanel = new JPanel();
            genePixListPanel.setLayout(new GridBagLayout());
            
            gba.add(genePixListPanel, genePixAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixListPanel, genePixSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixListPanel, genePixAvailableScrollPane, 0, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixListPanel, genePixButtonPanel, 1, 1, 1, 4, 0, 1, GBA.V, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(genePixListPanel, genePixSelectedScrollPane, 2, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(genePixSelectionPanel, genePixListPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            selectionPanel = new JPanel();
            selectionPanel.setLayout(new GridBagLayout());
            gba.add(selectionPanel, pathTextField, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(selectionPanel, genePixSelectionPanel, 0, 1, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
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
            
            // Currently, a minimum of one GenePix file must be selected to enable loading
            
            if (((DefaultListModel) genePixSelectedList.getModel()).size() > 0) {
                markLoadEnabled(true);
            } else {
                markLoadEnabled(false);
            }
        }
        
        public void onGenePixAdd() {
            int[] chosenIndices = genePixAvailableList.getSelectedIndices();
            Object[] chosenObjects = new Object[chosenIndices.length];
            
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                // For remove-then-add functionality
                //Object addItem = ((DefaultListModel) genePixAvailableList.getModel()).remove(chosenIndices[i]);
                // For copy-then-add functionality
                Object addItem = ((DefaultListModel) genePixAvailableList.getModel()).getElementAt(chosenIndices[i]);
                chosenObjects[i] = addItem;
            }
            
            for (int i = 0; i < chosenIndices.length; i++) {
                ((DefaultListModel) genePixSelectedList.getModel()).addElement(chosenObjects[i]);
            }
            
            validateLists();
        }
        
        public void onGenePixAddAll() {
            int elementCount = ((DefaultListModel) genePixAvailableList.getModel()).size();
            for (int i = 0; i < elementCount; i++) {
                Object addItem = ((DefaultListModel) genePixAvailableList.getModel()).getElementAt(i);
                ((DefaultListModel) genePixSelectedList.getModel()).addElement(addItem);
            }
            
            validateLists();
        }
        
        public void onGenePixRemove() {
            int[] chosenIndices = genePixSelectedList.getSelectedIndices();
            
            // Designed with copy-then-add functionality in mind
            for (int i = chosenIndices.length - 1; i >= 0; i--) {
                ((DefaultListModel) genePixSelectedList.getModel()).remove(chosenIndices[i]);
            }
            
            validateLists();
        }
        
        public void onGenePixRemoveAll() {
            // Designed with copy-then-add functionality in mind
            ((DefaultListModel) genePixSelectedList.getModel()).removeAllElements();
            
            validateLists();
        }
        
        public DefaultListModel getGenePixAvailableListModel() {
            return (DefaultListModel) genePixAvailableList.getModel();
        }
        
        public DefaultListModel getGenePixSelectedListModel() {
            return (DefaultListModel) genePixSelectedList.getModel();
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
                
                if (source == genePixAddButton) {
                    onGenePixAdd();
                } else if (source == genePixAddAllButton) {
                    onGenePixAddAll();
                } else if (source == genePixRemoveButton) {
                    onGenePixRemove();
                } else if (source == genePixRemoveAllButton) {
                    onGenePixRemoveAll();
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