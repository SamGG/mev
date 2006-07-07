    /*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: AffyGCOSFileLoader.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-07-07 13:15:55 $
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import javax.swing.JRadioButton;
import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
//import org.tigr.microarray.mev.GCOSSlideDataElement;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;

public class AffyGCOSFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private AffyGCOSFileLoaderPanel sflp;
    private int affyDataType = IData.DATA_TYPE_AFFY_ABS;

    public AffyGCOSFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        sflp = new AffyGCOSFileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
        return loadAffyGCOSExpressionFile(new File(this.sflp.pathTextField.getText()));
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }
    /*by wwang 
     * set datatype =DATA_TYPE_AFFY
     */ 
     
     public void setTMEVDataType(){
         TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
     }
     
    /*
     *  Handling of Mas5 data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
    public Vector loadAffyGCOSExpressionFile(File f) throws IOException {
    	this.setTMEVDataType();
        final int preSpotRows = this.sflp.getXRow()+1;
        final int preExperimentColumns = this.sflp.getXColumn();
        int numLines = this.getCountOfLines(f);
        int spotCount = numLines - preSpotRows;

        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
        
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        //String value,pvalue;
         String detection;

        float cy3, cy5;

        String[] moreFields = new String[1];
        String[] extraFields=null;
        final int rColumns = 1;
        final int rRows = spotCount;
        
        ISlideData slideDataArray[]=null;
        AffySlideDataElement sde=null;
        FloatSlideData slideData=null;
        
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringSplitter ss = new StringSplitter((char)0x09);
        String currentLine;
        int counter, row, column,experimentCount=0;
        counter = 0;
        row = column = 1;
        this.setFilesCount(1);
        this.setRemain(1);
        this.setFilesProgress(0);
        this.setLinesCount(numLines);
        this.setFileProgress(0);
        float[] intensities = new float[2];
        
        while ((currentLine = reader.readLine()) != null) {
            if (stop) {
                return null;
            }
//          fix empty tabbs appending to the end of line by wwang
            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);
            }
            ss.init(currentLine);
            if (counter == 0) { // parse header
            	if(sflp.absoluteRadioButton.isSelected())
            		experimentCount = ss.countTokens()- preExperimentColumns;
            	if(sflp.absMeanRadioButton.isSelected())
            		experimentCount = (ss.countTokens()+1- preExperimentColumns)/2;
            	if(sflp.referenceRadioButton.isSelected())
            		experimentCount = (ss.countTokens()+1- preExperimentColumns)/3;
            	slideDataArray = new ISlideData[experimentCount];
                slideDataArray[0] = new SlideData(rRows, rColumns);
                slideDataArray[0].setSlideFileName(f.getPath());
                for (int i=1; i<experimentCount; i++) {
                	slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(),spotCount);
                	slideDataArray[i].setSlideFileName(f.getPath());
                }
                if(sflp.absoluteRadioButton.isSelected()){
                	String [] fieldNames = new String[1];
                	//extraFields = new String[1];
                	fieldNames[0]="AffyID";
                	slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                }else if(sflp.absMeanRadioButton.isSelected()){
                	String [] fieldNames = new String[2];
                	extraFields = new String[1];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                }else{
                	String [] fieldNames = new String[3];
                	extraFields = new String[2];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    fieldNames[2]="P-value";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                }
                ss.nextToken();//pares the blank on header
                for (int i=0; i<experimentCount; i++) {
                    slideDataArray[i].setSlideDataName(ss.nextToken());
                    if(sflp.referenceRadioButton.isSelected()){
                    	ss.nextToken();//parse the detection
                        ss.nextToken();//parse the pvalue
                     }else if(sflp.absMeanRadioButton.isSelected()){
                        	ss.nextToken();//parse the detection  
                     }            
                }
                
            } else if (counter >= preSpotRows) { // data rows
                rows[0] = rows[2] = row;
                columns[0] = columns[2] = column;
                if (column == rColumns) {
                    column = 1;
                    row++;
                } else {
                    column++;
                }
                
                //affy ID
                moreFields[0] = ss.nextToken();
                sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields);
                slideDataArray[0].addSlideDataElement(sde);
                int i=0;
                for ( i=0; i<slideDataArray.length; i++) {                   
                    try {	
                    	
                        // Intensity
                        intensities[0] = 1.0f;
                        intensities[1] = ss.nextFloatToken(0.0f);
                        if(sflp.referenceRadioButton.isSelected()){
                        	 extraFields[0]=ss.nextToken();//detection
                             extraFields[1]=ss.nextToken();//p-value
                        }else if(sflp.absMeanRadioButton.isSelected()){
                        	extraFields[0]=ss.nextToken();//detection
                        }
                        
                    } catch (Exception e) {
                        cy3 = 0;
                        cy5 = Float.NaN;
                    }
                    if(i==0){
                    	slideDataArray[i].setIntensities(counter - preSpotRows, intensities[0], intensities[1]);
                    	//sde.setExtraFields(extraFields);
                    	 if(sflp.referenceRadioButton.isSelected()){
                    		 sde.setDetection(extraFields[0]);
                             sde.setPvalue(new Float(extraFields[1]).floatValue());
                    	 }else if(sflp.absMeanRadioButton.isSelected()){
                    		 sde.setDetection(extraFields[0]);
                    	 }
                    }else{
                    	if(i==1){
                    		meta = slideDataArray[0].getSlideMetaData();                    	
                    	}
                    	slideDataArray[i].setIntensities(counter-preSpotRows,intensities[0],intensities[1]);
                    	if(sflp.referenceRadioButton.isSelected()){
                    		((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
                    		((FloatSlideData)slideDataArray[i]).setPvalue(counter-preSpotRows,new Float(extraFields[1]).floatValue());
                    	}
                    	if(sflp.absMeanRadioButton.isSelected()){
                    		((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
                    	}
                    }
                }
               
            } else {
                //we have additional sample annoation
                
                //advance to sample key
            		for(int i = 0; i < preExperimentColumns-1; i++) {
            			ss.nextToken();
            		}
            		String key = ss.nextToken();
                
            		for(int j = 0; j < slideDataArray.length; j++) {
            			slideDataArray[j].addNewSampleLabel(key, ss.nextToken());
            		}
            	}
            	
            this.setFileProgress(counter);
           	counter++;
           	
        	}
        reader.close();
        
        Vector data = new Vector(slideDataArray.length);
        
        for(int j = 0; j < slideDataArray.length; j++)
        	data.add(slideDataArray[j]);
        
        this.setFilesProgress(1);
        return data;
    }
    
    
    public FileFilter getFileFilter() {
        
        FileFilter mevFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "AffyGCOS Files(*.txt)";
            }
        };
        
        return mevFileFilter;
    }
    
    public boolean checkLoadEnable() {
        
        // Currently, the only requirement is that a cell has been highlighted
        
        int tableRow = sflp.getXRow() + 1; // Adjusted by 1 to account for the table header
        int tableColumn = sflp.getXColumn();
        if (tableColumn < 0) return false;
        
        TableModel model = sflp.getTable().getModel();
        String fieldSummary = "";
        for (int i = 0; i < tableColumn; i++) {
            // System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
            fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
        }
        
        sflp.setFieldsText(fieldSummary);
        
        if (tableRow >= 1 && tableColumn >= 0) {
            setLoadEnabled(true);
            return true;
        } else {
            setLoadEnabled(false);
            return false;
        }
    }
    
    public boolean validateFile(File targetFile) {
        return true; // For now, no validation on Mas5 Files
    }
    
    public JPanel getFileLoaderPanel() {
        return sflp;
    }
    public int getAffyDataType(){
        return this.affyDataType;
    }
   
    public void processAffyGCOSFile(File targetFile) {
        
        Vector columnHeaders = new Vector();
        Vector dataVector = new Vector();
        Vector rowVector = null;
        BufferedReader reader = null;
        String currentLine = null;
        
        if (! validateFile(targetFile)) return;
        sflp.setDataFileName(targetFile.getAbsolutePath());
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
//          fix empty tabbs appending to the end of line by wwang
            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);
            }
            ss.init(currentLine);
        
            for (int i = 0; i < ss.countTokens()+1; i++) {
                columnHeaders.add(ss.nextToken());
            }
            //for (int i = 0; i < columnHeaders.size(); i++) {
            //    System.out.print(columnHeaders.get(i));
           // }
            model.setColumnIdentifiers(columnHeaders);
            int cnt = 0;
            while ((currentLine = reader.readLine()) != null && cnt < 100) {
                cnt++;
                ss.init(currentLine);
                rowVector = new Vector();
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
        
        sflp.setTableModel(model);
    }
    
    
    public String getFilePath() {
        return this.sflp.pathTextField.getText();
    }
    
    public void openDataPath() {
        this.sflp.openDataPath();
    }
    
/*
//
//	AffyGCOSFileLoader - Internal Classes
//
 */
    
    private class AffyGCOSFileLoaderPanel extends JPanel {
    	FileTreePane fileTreePane;
        
    	JTextField pathTextField;
        JPanel pathPanel;
        
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        
        JPanel AffyGCOSListPanel,refSelectionPanel;
        JList AffyGCOSAvailableList;
        JScrollPane AffyGCOSAvailableScrollPane;

        ButtonGroup optionsButtonGroup;
        JRadioButton absoluteRadioButton;
        JRadioButton absMeanRadioButton;
        JRadioButton referenceRadioButton;
        
        JTextField annoTextField;
        JPanel annoPanel;
        
        JPanel fileLoaderPanel,rightLoaderPanel;
        JSplitPane splitPane;
 
        private int xRow = -1;
        private int xColumn = -1;
        
        
        public AffyGCOSFileLoaderPanel() {                
                setLayout(new GridBagLayout());
                fileTreePane = new FileTreePane(SuperExpressionFileLoader.DATA_PATH);
                fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());

                pathTextField = new JTextField();
                pathTextField.setEditable(false);
                pathTextField.setBorder(new TitledBorder(new EtchedBorder(), "Selected Path"));
                pathTextField.setForeground(Color.black);
                pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
                
                pathPanel = new JPanel();
                pathPanel.setLayout(new GridBagLayout());
                pathPanel.setBorder(new TitledBorder(new EtchedBorder(), getFileFilter().getDescription()));
                gba.add(pathPanel, pathTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                
                refSelectionPanel = new JPanel();
                refSelectionPanel.setLayout(new GridBagLayout());
                refSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Affymetrix Data Options"));
                optionsButtonGroup = new ButtonGroup();
                absoluteRadioButton = new JRadioButton("Only Intensity", true);
                //absoluteRadioButton.addActionListener(new EventHandler());
                optionsButtonGroup.add(absoluteRadioButton);

                absMeanRadioButton = new JRadioButton("Intensity With Detection");
                //absMeanRadioButton.addActionListener(new EventHandler());
                optionsButtonGroup.add(absMeanRadioButton);

                referenceRadioButton = new JRadioButton("Intensity with Detection and P-value");
                //referenceRadioButton.addActionListener(new EventHandler());
                optionsButtonGroup.add(referenceRadioButton);
                gba.add(refSelectionPanel, absoluteRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
                gba.add(refSelectionPanel, referenceRadioButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
                gba.add(refSelectionPanel, absMeanRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);

        
                AffyGCOSAvailableList = new JList(new DefaultListModel());
                AffyGCOSAvailableList.setCellRenderer(new ListRenderer());
                AffyGCOSAvailableList.addListSelectionListener(new ListListener());
                AffyGCOSAvailableScrollPane = new JScrollPane(AffyGCOSAvailableList);
                AffyGCOSListPanel = new JPanel();
                AffyGCOSListPanel.setLayout(new GridBagLayout());
                AffyGCOSListPanel.setBorder(new TitledBorder(new EtchedBorder(), "Data File Available"));
                gba.add(AffyGCOSListPanel, AffyGCOSAvailableScrollPane, 0, 0, 2, 8, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                
                expressionTable = new JTable();
                expressionTable.setCellSelectionEnabled(true);
                expressionTable.setColumnSelectionAllowed(false);
                expressionTable.setRowSelectionAllowed(false);
                expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                expressionTable.getTableHeader().setReorderingAllowed(false);
                expressionTable.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent event) {
                        xRow = expressionTable.rowAtPoint(event.getPoint());
                        xColumn = expressionTable.columnAtPoint(event.getPoint());
                        checkLoadEnable();
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
                gba.add(tablePanel, tableScrollPane, 0, 0, 1, 5, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(tablePanel, instructionsLabel, 0, 6, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
 
                
                annoTextField = new JTextField();
                annoTextField.setEditable(false);
                annoTextField.setForeground(Color.black);
                annoTextField.setFont(new Font("serif", Font.BOLD, 12));
                
                annoPanel = new JPanel();
                annoPanel.setLayout(new GridBagLayout());
                annoPanel.setBorder(new TitledBorder(new EtchedBorder(), "Annotation Fields"));
                gba.add(annoPanel, annoTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
               
                fileLoaderPanel = new JPanel();
                fileLoaderPanel.setLayout(new GridBagLayout());
                
                //gba.add(fileLoaderPanel,AffyGCOSListPanel, 0, 0, 2, 9, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(fileLoaderPanel, pathPanel, 0, 0, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(fileLoaderPanel, refSelectionPanel, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(fileLoaderPanel, tablePanel, 0, 2, 1, 5, 3, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(fileLoaderPanel, annoPanel, 0, 7, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);               
                
                rightLoaderPanel=new JPanel();
                rightLoaderPanel.setLayout(new GridBagLayout());
                gba.add(rightLoaderPanel,AffyGCOSListPanel, 0, 0, 1, 9, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                gba.add(rightLoaderPanel,fileLoaderPanel, 1, 0, 1, 9, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreePane, rightLoaderPanel);
                splitPane.setPreferredSize(new java.awt.Dimension(600, 600));
                splitPane.setDividerLocation(200);
                gba.add(this,splitPane,0,0,1,1,1,1,GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                
        }
        
        public void openDataPath() {
            fileTreePane.openDataPath();
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
        
        public void selectAffyGCOSFile() {
            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
            jfc.setFileFilter(getFileFilter());
            int activityCode = jfc.showDialog(this, "Select");
            
            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
                processAffyGCOSFile(target);
            }
        }
        
        public void setDataFileName(String fileName) {
            pathTextField.setText(fileName);
           // System.out.println(pathTextField);
        }
    
        
        public void setTableModel(TableModel model) {
            expressionTable.setModel(model);
            int numCols = expressionTable.getColumnCount();
            //System.out.print(numCols);
            for(int i = 0; i < numCols; i++){
                expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }
        
        public void setFieldsText(String fieldsText) {
            annoTextField.setText(fieldsText);
        }
        
        
        private class ListRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = (File) value;
                setText(file.getName());
                return this;
            }
        }
        
        
        
        private class ListListener implements javax.swing.event.ListSelectionListener {
            
            public void valueChanged(ListSelectionEvent lse) {
            	File file;
            	//Object source=lse.getSource();
            	file = (File)(AffyGCOSAvailableList.getSelectedValue());
            	if(file == null || !(file.exists()))
            		return;
            	processAffyGCOSFile(file);
            	return;
            	
            }
        }
        
     
        
        private class FileTreePaneEventHandler implements FileTreePaneListener {
            
            public void nodeSelected(FileTreePaneEvent event) {
                
                String filePath = (String) event.getValue("Path");
                Vector fileNames = (Vector) event.getValue("Filenames");
     
                if(fileNames.size() < 1)
                    return;
                
                FileFilter AffyGCOSFileFilter = getFileFilter();
//                FileFilter AffyGCOSCallFileFilter = getFileFilter();
                ((DefaultListModel)(AffyGCOSAvailableList.getModel())).clear();
                
                for (int i = 0; i < fileNames.size(); i++) {
                    
                    File targetFile = new File((String) fileNames.elementAt(i));
                      
                    if (AffyGCOSFileFilter.accept(targetFile)) {
                        ((DefaultListModel)(AffyGCOSAvailableList.getModel())).addElement(new File((String) fileNames.elementAt(i)));
                    }
                }
            }
            
            public void nodeCollapsed(FileTreePaneEvent event) {}
            public void nodeExpanded(FileTreePaneEvent event) {}
        }
    }  
    }

