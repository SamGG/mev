/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: StanfordFileLoader.java,v $
 * $Revision: 1.5 $
 * $Date: 2004-02-27 22:16:51 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.SlideDataElement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;

public class StanfordFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private StanfordFileLoaderPanel sflp;
    
    public StanfordFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        sflp = new StanfordFileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
        return loadStanfordExpressionFile(new File(this.sflp.fileNameTextField.getText()));
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

        final int preSpotRows = this.sflp.getXRow()+1;
        final int preExperimentColumns = this.sflp.getXColumn();
        
        int spotCount = getCountOfLines(f) - preSpotRows;
        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "Stanford Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
                
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        String value;
        float cy3, cy5;
        String[] moreFields = new String[preExperimentColumns];

        final int rColumns = 1;
        final int rRows = spotCount;
        
        ISlideData[] slideDataArray = null;
        SlideDataElement sde;
        
        int numLines = this.getCountOfLines(f);
        
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringSplitter ss = new StringSplitter((char)0x09);
        String currentLine;
        int counter, row, column;
        counter = 0;
        row = column = 1;
        this.setFilesCount(1);
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
                slideDataArray[0] = new SlideData(rRows, rColumns);
                    slideDataArray[0].setSlideFileName(f.getPath());
                for (int i=1; i<slideDataArray.length; i++) {
                    slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount);                    
                    slideDataArray[i].setSlideFileName(f.getPath());                   
                }
                //get Field Names
                String [] fieldNames = new String[preExperimentColumns];
                for(int i = 0; i < preExperimentColumns; i++){
                   fieldNames[i] = ss.nextToken();
                }         
                TMEV.setFieldNames(fieldNames);

                for (int i=0; i<experimentCount; i++) {
                    slideDataArray[i].setSlideDataName(ss.nextToken());
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
                for (int i=0; i<preExperimentColumns; i++) {
                    moreFields[i] = ss.nextToken();
                }
                sde = new SlideDataElement(rows, columns, new float[2], moreFields);
                slideDataArray[0].addSlideDataElement(sde);
                
                for (int i=0; i<slideDataArray.length; i++) {

                    cy3 = 1f;  //set cy3 to a default value of 1.
                    
                    try {                       
                        value = ss.nextToken();
                        cy5 = Float.parseFloat(value);  //set cy5 to hold the value
                                                        //getRatio methods will return cy5
                                                        //for Stanford data type
                    } catch (Exception e) {
                        cy3 = cy5 = Float.NaN;
                    }
                    slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
                }
            }
            this.setFileProgress(counter);
            counter++;            
        }
        reader.close();
   
        Vector data = new Vector(slideDataArray.length);
           
        for(int i = 0; i < slideDataArray.length; i++)
            data.add(slideDataArray[i]);
        
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
                return "Stanford Files (*.txt)";
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
          //  System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
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
        return true; // For now, no validation on Stanford Files
    }
    
    public JPanel getFileLoaderPanel() {
        return sflp;
    }
    
    public void processStanfordFile(File targetFile) {
        
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
            
            for (int i = 0; i < ss.countTokens()+1; i++) {
                columnHeaders.add(ss.nextToken());
            }
            
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
        return this.sflp.fileNameTextField.getText();
    }    

    public void openDataPath() {
    } 
    
/*
//
//	StanfordFileLoader - Internal Classes
//
 */
    
    private class StanfordFileLoaderPanel extends JPanel {
        
        JTextField fileNameTextField;
        JButton browseButton;
        JPanel fileSelectionPanel;
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        JPanel fileLoaderPanel;
        JTextField fieldsTextField;
        JPanel fieldsPanel;
        
        private int xRow = -1;
        private int xColumn = -1;
        
        public StanfordFileLoaderPanel() {
            
            setLayout(new GridBagLayout());
            
            fileNameTextField = new JTextField();
            fileNameTextField.setEditable(false);
            fileNameTextField.setForeground(Color.black);
            fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));
            
            browseButton = new JButton("File Browser");
            browseButton.addActionListener(new EventHandler());
            
            fileSelectionPanel = new JPanel();
            fileSelectionPanel.setLayout(new GridBagLayout());
            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Selected Stanford File"));
            gba.add(fileSelectionPanel, fileNameTextField, 0, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileSelectionPanel, browseButton, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
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
            gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            fieldsTextField = new JTextField();
            fieldsTextField.setEditable(false);
            fieldsTextField.setForeground(Color.black);
            fieldsTextField.setFont(new Font("serif", Font.BOLD, 12));
            
            fieldsPanel = new JPanel();
            fieldsPanel.setLayout(new GridBagLayout());
            fieldsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Annotation Fields"));
            gba.add(fieldsPanel, fieldsTextField, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            gba.add(fileLoaderPanel, fileSelectionPanel, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, tablePanel, 0, 1, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, fieldsPanel, 0, 3, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
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
        
        private class EventHandler implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == browseButton) {
                    selectStanfordFile();
                }
            }
        }                
    }
    
 
}