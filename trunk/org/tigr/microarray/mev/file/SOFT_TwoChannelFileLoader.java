/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SOFT_TwoChannelFileLoader.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
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

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;

public class SOFT_TwoChannelFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private SOFT_TwoChannelFileLoaderPanel sflp;
    private Vector datainfo=new Vector();//store sample info
    private Vector platforminfo=new Vector();//store platform info
    private boolean unload=false;
    
    public SOFT_TwoChannelFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        sflp = new SOFT_TwoChannelFileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
        return loadSOFT_TwoChannelExpressionFile(new File(this.sflp.fileNameTextField.getText()));
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }

    /*
     *  Handling of SOFT_TwoChannel data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
    public Vector loadSOFT_TwoChannelExpressionFile(File f) throws IOException {
    	
        float cy3, cy5;
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        float[] intensities = new float[2];
        String[] extraFields=new String[1];
        SlideDataElement sde=null; 
        String[]moreFields=null;
        String[] fieldNames=null;
        final int rColumns = 1;
        final int totalRows =sflp.expressionTable.getRowCount();
        final int totalColumns =sflp.expressionTable.getColumnCount();
        String[] key=new String[datainfo.size()/2];
        for(int k=0;k<datainfo.size()/2;k++)
       	 key[k]="sample information "+(k+1);
        if(unload){
       	 moreFields = new String[totalColumns+platforminfo.size()/2];
       	 fieldNames = new String[totalColumns+platforminfo.size()/2];
        }else{
       	 moreFields = new String[totalColumns];
       	 fieldNames = new String[totalColumns];
        }
        this.setFilesCount(1);
        this.setRemain(1);
        this.setFilesProgress(0);
        this.setFileProgress(0);

        ISlideData slideData=null;
        slideData = new SlideData(totalRows, rColumns);
        slideData.setSlideFileName(f.getPath());
             
        for(int m=0;m<totalColumns;m++)
       	 fieldNames[m]=sflp.expressionTable.getColumnName(m);
        if(unload){
       	 	fieldNames[totalColumns]="Platform Information";        
       	 	for(int k=1;k<platforminfo.size()/2;k++)
       	 		fieldNames[totalColumns+k]="";
        }        	 
        TMEV.setFieldNames(fieldNames);
        
        for(int i=0;i<totalRows;i++){
        	intensities[0] = 1.0f;
            intensities[1] =Float.parseFloat((String)sflp.expressionTable.getValueAt(i,1));
            for(int j=0;j<totalColumns;j++)
            	moreFields[j]=(String)sflp.expressionTable.getValueAt(i,j);
            if(unload){
           	 	for(int k=0;k<platforminfo.size()/2;k++)
           	 		moreFields[totalColumns+k]=(String)platforminfo.elementAt(k);
            }
            sde = new SlideDataElement(String.valueOf(i), rows, columns, new float[2], moreFields);
            slideData.addSlideDataElement(sde);
            slideData.setIntensities(i, intensities[0], intensities[1]);
        }
//      add sample annotion
        for(int m=0;m<datainfo.size()/2;m++)
       	slideData.addNewSampleLabel(key[m],(String)datainfo.elementAt(m));
        Vector data=new Vector();
        data.add(slideData);
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
                return "Tab Delimited, Multiple Sample Files (TDMS) (*.txt)";
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
        /*for (int i = 0; i < tableColumn; i++) {
            //  System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
            fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
        }
        
        sflp.setFieldsText(fieldSummary);
        */
        if (tableRow >= 1 && tableColumn >= 0) {
            setLoadEnabled(true);
            return true;
        } else {
            setLoadEnabled(false);
            return false;
        }
    }
    
    public boolean validateFile(File targetFile) {
        return true; // For now, no validation on SOFT_TwoChannel Files
    }
    
    public JPanel getFileLoaderPanel() {
        return sflp;
    }
    public void loadPlatFormFile(File targetFile) {
    	BufferedReader reader = null;
    	String currentLine = null;    	
   	 	sflp.setCallFileName(targetFile.getAbsolutePath());
   	 	try {
   	 		reader = new BufferedReader(new FileReader(targetFile), 1024 * 128);
   	 	} catch (FileNotFoundException fnfe) {
   	 		fnfe.printStackTrace();
   	 	}
        try {
            StringSplitter ss = new StringSplitter('\t');
            currentLine = reader.readLine();
            
            if(currentLine.charAt(0)=='^'){            	
            	platforminfo.add(currentLine.substring(1));
            	currentLine = reader.readLine();
            }
            while(currentLine.charAt(0)=='!'){
            	platforminfo.add(currentLine.substring(1));
            	currentLine = reader.readLine();
            }
            reader.close();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
        
    
    public void processSOFT_TwoChannelFile(File targetFile) {
        
        Vector columnHeaders = new Vector();
        Vector dataVector = new Vector();
        Vector rowVector = null;
        BufferedReader reader = null;
        String currentLine = null;
        
        if (! validateFile(targetFile)) return;
        
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
            ss.init(currentLine);
            
            if(currentLine.charAt(0)=='^'){            	
            	datainfo.add(currentLine.substring(1));
            	currentLine = reader.readLine();
            }
            while(currentLine.charAt(0)=='!'){
            	datainfo.add(currentLine.substring(1));
            	currentLine = reader.readLine();
            }	
            
            while(currentLine.charAt(0)=='#'){
            	currentLine = reader.readLine();
            }
            //filter!sample_table_begin
            currentLine = reader.readLine(); 
           // System.out.print(currentLine);
            ss.init(currentLine);
         
            for (int i = 0; i < ss.countTokens()+1; i++) {
                columnHeaders.add(ss.nextToken());
            }
            
            model.setColumnIdentifiers(columnHeaders);
            int cnt = 0;
            while ((currentLine = reader.readLine()) != null && currentLine.charAt(0)!='!') {
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
        return this.sflp.fileNameTextField.getText();
    }
    
    public void openDataPath() {
        this.sflp.openDataPath();
    }
    
/*
//
//	SOFT_TwoChannelFileLoader - Internal Classes
//
 */
    
    private class SOFT_TwoChannelFileLoaderPanel extends JPanel {
        
        FileTreePane fileTreePane;
        
        JTextField fileNameTextField;
        JPanel fileSelectionPanel;
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        JPanel fileLoaderPanel;
        JTextField fieldsTextField;
        JPanel fieldsPanel;
        JSplitPane splitPane;
        ButtonGroup optionsButtonGroup;
        JRadioButton loadRadioButton;
        JRadioButton notLoadRadioButton;
        JList availableList;
        JScrollPane availableScrollPane;
        JPanel refListPanel;
        JList refAvailableList;
        JScrollPane refAvailableScrollPane;
        
        JTextField refTextField;
        JPanel refPanel,filePanel,checkPanel;
        
        
        private int xRow = -1;
        private int xColumn = -1;
        
        public SOFT_TwoChannelFileLoaderPanel() {
            
            setLayout(new GridBagLayout());
            
            fileTreePane = new FileTreePane(SuperExpressionFileLoader.DATA_PATH);
            fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());
            fileTreePane.setPreferredSize(new java.awt.Dimension(200, 50));
            
            fileNameTextField = new JTextField();
            fileNameTextField.setEditable(false);
            fileNameTextField.setForeground(Color.black);
            fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));
         
            fileSelectionPanel = new JPanel();
            fileSelectionPanel.setLayout(new GridBagLayout());
            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Selected TDMS File"));
            gba.add(fileSelectionPanel, fileNameTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    
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
            String instructions = "<html>The first column should be ChipID.<br>";
            instructions =instructions+	"The second column should be normalized ratio of means defined as CH1 divided by CH@.<br>";
            instructions =instructions+"If your file is in different order, you can reorder them as required.<br>";
            instructions =instructions+"Then click the upper-leftmost expression value. Click the <b>Load</b> button to finish.</html>";
            instructionsLabel.setText(instructions);
            
            tablePanel = new JPanel();
            tablePanel.setLayout(new GridBagLayout());
            tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));
            gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            refAvailableList = new JList(new DefaultListModel());
            refAvailableList.setCellRenderer(new ListRenderer());
            refAvailableList.addListSelectionListener(new ListListener());
            refAvailableScrollPane = new JScrollPane(refAvailableList);
            refListPanel = new JPanel();
            refListPanel.setLayout(new GridBagLayout());
            refListPanel.setBorder(new TitledBorder(new EtchedBorder(), "Platform File Available"));
            gba.add(refListPanel, refAvailableScrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            loadRadioButton=new JRadioButton("Load");
            loadRadioButton.addActionListener(new EventHandler());
            notLoadRadioButton=new JRadioButton("Unload", true);
            notLoadRadioButton.addActionListener(new EventHandler());
            
            optionsButtonGroup = new ButtonGroup();
            optionsButtonGroup.add(loadRadioButton);
            optionsButtonGroup.add(notLoadRadioButton);
            checkPanel=new JPanel();
            checkPanel.setLayout(new GridBagLayout());
            checkPanel.setBorder(new TitledBorder(new EtchedBorder(), "Platform Load Options"));
            gba.add(checkPanel, loadRadioButton, 0, 0, 1, 1, 1, 0, GBA.B, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
            gba.add(checkPanel, notLoadRadioButton, 1, 0, 1, 1, 1, 0, GBA.B, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
            
            refTextField = new JTextField();
            refTextField.setEditable(false);
            refTextField.setBorder(new TitledBorder(new EtchedBorder(), "Selected Platform File"));
            refTextField.setForeground(Color.black);
            refTextField.setFont(new Font("monospaced", Font.BOLD, 12));
            refTextField.setEnabled(unload);
            
            refPanel = new JPanel();
            refPanel.setLayout(new GridBagLayout());
            refPanel.setBorder(new TitledBorder(new EtchedBorder(), "Platform File"));
            gba.add(refPanel, refTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5,5),0,0);
            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            
            //jcb add list panel
            availableList = new JList(new DefaultListModel());
            availableList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
            availableList.setCellRenderer(new ListRenderer());
            availableList.addListSelectionListener(new ListListener());
            availableScrollPane = new JScrollPane(availableList);
            
            JPanel filePanel = new JPanel(new GridBagLayout());
            filePanel.setPreferredSize(new Dimension(10, 100));
            filePanel.setBorder(new TitledBorder(new EtchedBorder(), "Available Files (*.txt)"));
            gba.add(filePanel, availableScrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(fileLoaderPanel,filePanel, 0, 0, 1, 5, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, refListPanel, 0, 6, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, fileSelectionPanel, 2, 0, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, tablePanel, 2, 1, 1, 6, 3, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, checkPanel, 2, 7, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, refPanel, 2, 8, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            //jcb
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreePane, fileLoaderPanel);
            
            gba.add(this, splitPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            splitPane.setDividerLocation(200);
            
        }
        
        public void openDataPath() {
            fileTreePane.openDataPath();
        }
        
        public JTable getTable() {
            return expressionTable;
        }
        public void setCallFileName(String fileName) {
            refTextField.setText(fileName);
        }
        public int getXColumn() {
            return xColumn;
        }
        
        public int getXRow() {
            return xRow;
        }
        
        public void selectSOFT_TwoChannelFile() {
            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
            jfc.setFileFilter(getFileFilter());
            int activityCode = jfc.showDialog(this, "Select");
            
            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
                processSOFT_TwoChannelFile(target);
            }
        }
        
        public void setFileName(String fileName) {
            fileNameTextField.setText(fileName);
        }
        
        public void setTableModel(TableModel model) {
            expressionTable.setModel(model);
            int numCols = expressionTable.getColumnCount();
            for(int i = 0; i < numCols; i++){
                expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }
        
        public void setFieldsText(String fieldsText) {
            fieldsTextField.setText(fieldsText);
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
            	Object source=lse.getSource();
            	if(source==availableList){
            		file = (File)(availableList.getSelectedValue());
            		if(file == null || !(file.exists()))
            			return;
            		processSOFT_TwoChannelFile(file);
            		return;
            	}else
                file = (File)(refAvailableList.getSelectedValue());
            	loadPlatFormFile(file);
               
            }
            
        }
        
        private class EventHandler implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                Object source = event.getSource();
                if (source == loadRadioButton) {
                    unload=true;
                    sflp.refTextField.setEnabled(unload);
                }
            }
        }     
        
        private class FileTreePaneEventHandler implements FileTreePaneListener {
            
            public void nodeSelected(FileTreePaneEvent event) {
                
                String filePath = (String) event.getValue("Path");
                Vector fileNames = (Vector) event.getValue("Filenames");
                
                if(fileNames.size() < 1)
                    return;
                
                String fileName = (String)(fileNames.elementAt(0));
                
                ((DefaultListModel)(availableList.getModel())).clear();
                
                ((DefaultListModel)(refAvailableList.getModel())).clear();
                for (int i = 0; i < fileNames.size(); i++) {
                    
                    File targetFile = new File((String) fileNames.elementAt(i));
                    
                    FileFilter SOFT_TwoChannelFileFilter = getFileFilter();
                    
                    if (SOFT_TwoChannelFileFilter.accept(targetFile)) {
                        ((DefaultListModel)(availableList.getModel())).addElement(new File((String) fileNames.elementAt(i)));
                        ((DefaultListModel)(refAvailableList.getModel())).addElement(new File((String) fileNames.elementAt(i)));
                    }
                }
            }
            
            public void nodeCollapsed(FileTreePaneEvent event) {}
            public void nodeExpanded(FileTreePaneEvent event) {}
        }
    }    
}