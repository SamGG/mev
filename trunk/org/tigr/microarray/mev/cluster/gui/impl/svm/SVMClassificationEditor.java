/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMClassificationEditor.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-05-24 18:22:26 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.swing.table.*;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;


public class SVMClassificationEditor extends javax.swing.JDialog {//javax.swing.JFrame {
    
    IFramework framework;
    IData data;
    Experiment experiment;
    String [] fieldNames;
    File currentFile;
    SVMSearchDialog searchDialog;
    SortListener sorter;
    SVMTableModel svmTableModel;
    boolean classifyGenes;
    boolean cancelForm = false;
    /** Creates new form SVMClassificationEditor
     * @param Framework <CODE>Framework</CODE> object to supply initial data to editor
     */
    public SVMClassificationEditor(IFramework Framework, boolean classifyGenes){
        super(new JFrame(), "SVM Classification Editor", true);
        this.classifyGenes = classifyGenes;
        initComponents();
        
       // if(!classifyGenes){
            //remove these options for later inclusion
            this.editMenu.remove(2); //gene cluster
            this.editMenu.remove(1); //gene list
            this.editMenu.validate();
        //}
        
        inClassButton.setIcon(GUIFactory.getIcon("in_class.gif"));
        outClassButton.setIcon(GUIFactory.getIcon("out_class.gif"));
        neutralButton.setIcon(GUIFactory.getIcon("neutral_class.gif"));
        searchButton.setIcon(GUIFactory.getIcon("search.gif"));
        runButton.setIcon(GUIFactory.getIcon("go.gif"));
        svcApplyMenuItem.setIcon(GUIFactory.getIcon("svcfileicon.gif"));
        saveAsMenuItem.setIcon(GUIFactory.getIcon("svcfileicon.gif"));
        this.tcListMenuItem.setIcon(null);
        
        currentFile = null;
        this.framework = Framework;
        this.data = framework.getData();
        this.experiment = this.data.getExperiment();
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = table.getColumnModel();
        
        this.jScrollPane1.setBackground(Color.black);
        loadTable();  //create and build table
        
        searchDialog = new SVMSearchDialog(new JFrame(), table, false); //persistent search dialog
        
        sorter = new SortListener(svmTableModel);	//listener to fire sorting events
        indexSortMenuItem.addActionListener(sorter);
        classSortMenuItem.addActionListener(sorter);
        
        for(int i = 0; i < this.fieldNames.length ; i++){  //add listener to menu items
            JMenuItem item = new JMenuItem(fieldNames[i]);
            item.addActionListener(sorter);
            this.sortByMenu.add(item);
        }
        //table.setDefaultRenderer(Boolean.class, new ClassificationCellRenderer()); //render as radiobuttons
    }
    
    
    /**
     * Loads data into the table array object in the default model
     */
    private void loadTable(){
        
        if(data == null) return;
        
        int numGenes = data.getFeaturesSize();
        int numSamples = data.getFeaturesCount();
        
        if(classifyGenes){
            fieldNames =  data.getFieldNames();     //get all field names
        }
        else{
            fieldNames = new String[1];
            fieldNames[0] = "Sample/Experiment Name";
        }
        String [] headerNames = new String[fieldNames.length + 4];  //create header names
        headerNames[0] = "Index";
        headerNames[1] = "In Class";
        headerNames[2] = "Out of Class";
        headerNames[3] = "Neutral";
        
        for(int i = 4; i < fieldNames.length + 4 ; i++){
            headerNames[i] = fieldNames[i-4];
        }
        
        //create a Default TM and an SVM TM.
        DefaultTableModel model;
        
        if(classifyGenes)
            model = new DefaultTableModel(new Object [numGenes][fieldNames.length + 4], headerNames);
        else
            model = new DefaultTableModel(new Object [numSamples][fieldNames.length + 4], headerNames);
        
        svmTableModel = new SVMTableModel(headerNames, model);
        this.table.setModel(svmTableModel);
        
        String annot;
        
        if(classifyGenes){
            for(int row = 0; row < numGenes; row++){
                this.table.setValueAt( new Integer(row), row, 0);
                this.table.setValueAt( new Boolean(false), row, 1);
                this.table.setValueAt( new Boolean(true), row, 2);
                this.table.setValueAt( new Boolean(false), row, 3);
                
                for(int j = 4; j < headerNames.length ; j++){
                    annot = data.getElementAttribute(row, j - 4);
                    this.table.setValueAt( annot, row, j );
                }
            }
        }
        else{
            for(int row = 0; row < numSamples; row++){
                this.table.setValueAt( new Integer(row), row, 0);
                this.table.setValueAt( new Boolean(false), row, 1);
                this.table.setValueAt( new Boolean(true), row, 2);
                this.table.setValueAt( new Boolean(false), row, 3);
                
                this.table.setValueAt( data.getFullSampleName(row), row, 4);
            }
        }
        
        
        int W = 75;
        
        TableColumn col = this.table.getColumn("In Class");
        setWidth(col, W, true);
        col = this.table.getColumn("Out of Class");
        setWidth(col, W, true);
        col = this.table.getColumn("Neutral");
        setWidth(col, W, true);
        
        col = this.table.getColumn("Index");
        W = getIndexColumnWidth();
        setWidth(col, W, true);
        
        for(int i = 0; i < fieldNames.length; i++){
            col = this.table.getColumn(fieldNames[i]);
            W = getColumnTextWidth(i + 4);
            W = Math.min( W, 300);
            setWidth(col, W, false);
        }
        
        table.getModel().addTableModelListener(new ClassSelectionListener());
        table.setColumnModel(new SVMTableColumnModel(table.getColumnModel()));
    }
    
    /*
     * Returns the max width of the contents of a column
     */
    
    private int getColumnTextWidth(int col){
        Graphics g = table.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int columnWidth = fm.stringWidth( table.getColumnName(col) );
        int numRows = table.getRowCount();
        
        for(int i = 0; i < numRows; i++){
            if( fm.stringWidth((String)table.getValueAt(i, col)) > columnWidth)
                columnWidth = fm.stringWidth((String)table.getValueAt(i, col));
        }
        return columnWidth + 10;
    }
    
    
    private int getIndexColumnWidth(){
        Graphics g = table.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int columnWidth = fm.stringWidth( "Index" );
        int numRows = table.getRowCount();
        
        for(int i = 0; i < numRows; i++){
            if( fm.stringWidth( ((Integer)table.getValueAt(i, 0)).toString() ) > columnWidth)
                columnWidth = fm.stringWidth(((Integer)table.getValueAt(i, 0)).toString());
        }
        return columnWidth + 10;
    }
    
    
    private void setWidth(TableColumn col, int width, boolean setAll){
        col.setWidth(width);
        col.setPreferredWidth(width);
        if(setAll){
            col.setMaxWidth(width);
            col.setMinWidth(width);
        }
    }
    
    private void applySVCFile(){
        File inputFile = null;
        
        final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setCurrentDirectory(new File("Data/SVM"));
        fc.setFileFilter(new SVCFileFilter());
        int returnVal = fc.showOpenDialog( this );
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            inputFile = fc.getSelectedFile();
        }
        readAndApplyFile(inputFile);
    }
    
    
    private void readAndApplyFile(File inputFile){
        if(inputFile == null)
            return;
        
        int [] fileClassification;
        
        try{
            
            BufferedReader br = new BufferedReader(new java.io.FileReader(inputFile));
            
            String line = new String();
            int currClass = 0;
            
            StringTokenizer stok;
            Vector values = new Vector();
            
            br.readLine(); //skip header
            while( (line = br.readLine()) != null){
                stok = new StringTokenizer(line, "\t");
                if(stok.hasMoreTokens())
                    stok.nextToken();
                if(stok.hasMoreElements()){
                    values.add(stok.nextToken());
                }
            }
            if(values.size() != table.getRowCount()){
                //send message out
                if(this.classifyGenes)
                    JOptionPane.showMessageDialog(this.framework.getFrame(), "Number of classification indices provided does not match the number of genes in the data set!","Classification Input Error", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(this.framework.getFrame(), "Number of classification indices provided does not match the number of experiments in the data set!","Classification Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
                        
            applyClassArrayToTable(values);
            
        } catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(this.framework.getFrame(), "Classification file not found.","Classification Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException e1){
            JOptionPane.showMessageDialog(this.framework.getFrame(), "File Input Error, please check format.","Classification Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (NumberFormatException e2){
                    JOptionPane.showMessageDialog(this.framework.getFrame(), "File Input Error, please check number format format.","Classification Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
    }
    
    
    public void applyClassArrayToTable(Vector values) throws NumberFormatException{
        
        if(values == null)
            return;
        
        svmTableModel.sort(0);
        int n = values.size();
        int classification = 0;
        
        for(int i = 0; i < n ; i++){
            if(! ((Boolean)(table.getValueAt(i, 1))).booleanValue()){  //only alter if not in class
                
                classification = Integer.parseInt((String)(values.elementAt(i)));
                
                if(classification == 1){
                    table.setValueAt( new Boolean(true),i, 1);
                }
                else if(classification == -1){
                    table.setValueAt( new Boolean(true),i, 2);
                }
                else {
                    table.setValueAt( new Boolean(true),i, 3);
                }
            }
        }
    }
    
    
    
    private void applyStoredCluster(){
        
    }
    
    private void sortBy(int col){
        
    }
    
    private void setClassificationForRange(int startRow, int endRow, int col){
        for( int row = startRow ; row <= endRow; row++){
            table.setValueAt(new Boolean(true), row, col);
        }
        //svmTableModel.fireTableDataChanged();
        table.repaint();
    }
    
    private void setClassificationForSet(int [] indices, int col){
        
        for(int row = 0; row < indices.length ; row++){
            table.setValueAt(new Boolean(true), indices[row] , col);
        }
        table.repaint();
        //	svmTableModel.fireTableDataChanged();
    }
    
    private void saveTableAsSVC() throws IOException{
        
        final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.addChoosableFileFilter(new SVCFileFilter());
        fc.setFileView(new SVCFileView());             
        fc.setCurrentDirectory(new File("Data/SVM"));
        if(currentFile != null)
            fc.setSelectedFile(currentFile);           
        int result = fc.showSaveDialog(this);        
        if(result == JFileChooser.CANCEL_OPTION)
            return;        
        else{
            currentFile = fc.getSelectedFile();
            writeToFile( currentFile );
            this.saveMenuItem.setEnabled(true);
        }        
        return;
    }
    
    
    private void writeToFile( File file ) throws IOException{        
        BufferedWriter bw = new java.io.BufferedWriter( new java.io.FileWriter(file) );
        PrintWriter pw = new java.io.PrintWriter( bw );
        String TAB = "\t";
        String annot;        
        int numRows = table.getRowCount();        
        pw.print("Index"+TAB+"Classification"+TAB);
        for(int col = 0; col < fieldNames.length; col++){
            pw.print(fieldNames[col] + TAB);
        }
        pw.print("\n");        
        
        for(int row = 0; row < numRows ; row++){            
            pw.print(table.getValueAt(row, 0)+TAB+getClassificationString(row)+TAB);
            annot = getRowAnnotationString(row);            
            if(!annot.equals(""))
                pw.print(annot);
            pw.print("\n");
        }        
        pw.close();
        bw.close();
    }
    
    
    private void saveTableToCurrentSVC() throws IOException{
        if( currentFile != null){
            writeToFile(currentFile);
        }
        
    }
    
    private String getClassificationString(int row){
        
        if( ((Boolean)table.getValueAt(row, 2)).booleanValue() == true )
            return "-1";
        else if( ((Boolean)table.getValueAt(row, 1)).booleanValue() == true )
            return "1";
        else
            return "0";
    }
    
    private String getRowAnnotationString(int row){
        
        String annot = new String("");
        int numCol = table.getColumnCount();
        String TAB = "\t";
        
        for(int col = 4; col < numCol ; col++){
            annot += (String)(table.getValueAt(row, col));
            annot += TAB;
        }
        return annot;
    }
    
    
    private void searchTable(){
        
        searchDialog.setVisible(true);
        searchDialog.toFront();
        searchDialog.setLocation(this.getLocation().x + 100, this.getLocation().y +100);
        
    }
    
    /**
     *	Returns the classification provided in the editor.  The classification array
     *  will only have values that are in the current matrix of the analysis
     */
    
    public int [] getClassification(){
        if(classifyGenes)
            return getGeneClassification();
        else
            return getSampleClassification();
    }
    
    
    private int [] getGeneClassification(){
        this.svmTableModel.sort(0);
        
        int numberOfExperimentRows = experiment.getNumberOfGenes();
        int numberOfClassificationRows = table.getRowCount();
        int currentIndex;
        int experimentRowCnt = 0;
        int [] classification = new int[numberOfExperimentRows];
        
        currentIndex = experiment.getGeneIndexMappedToData(experimentRowCnt);
        
        for(int row = 0; row < numberOfClassificationRows; row++){
            
            //if the currentIndex (in data) is the current row in editor
            if(currentIndex == row){
                if( ((Boolean)(table.getValueAt(row, 1))).booleanValue() )
                    classification[experimentRowCnt] = 1;
                else if( ((Boolean)(table.getValueAt(row, 2))).booleanValue() )
                    classification[experimentRowCnt] = -1;
                else
                    classification[experimentRowCnt] = 0;  //eventually it will be neutral
                experimentRowCnt++;
                currentIndex = experiment.getGeneIndexMappedToData(experimentRowCnt);
            }
        }
        return classification;
    }
    
    private int [] getSampleClassification(){
        this.svmTableModel.sort(0);
        int numRows = table.getRowCount();
        int [] classification = new int[numRows];
        
        for(int row = 0; row < numRows ; row++){
            if( ((Boolean)(table.getValueAt(row, 1))).booleanValue() )
                classification[row] = 1;
            else if( ((Boolean)(table.getValueAt(row, 2))).booleanValue() )
                classification[row] = -1;
            else
                classification[row] = -1;  //eventually it will be neutral
        }
        return classification;
    }
    
    /**
     * Return the editor close status, returns true if window closed by "X" excape button
     */
    public boolean formCanceled(){
        return this.cancelForm;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jToolBar1 = new javax.swing.JToolBar();
        inClassButton = new javax.swing.JButton();
        outClassButton = new javax.swing.JButton();
        neutralButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        svcApplyMenuItem = new javax.swing.JMenuItem();
        storedClusterMenuItem = new javax.swing.JMenuItem();
        tcListMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        searchMenuItem = new javax.swing.JMenuItem();
        sortByMenu = new javax.swing.JMenu();
        indexSortMenuItem = new javax.swing.JMenuItem();
        classSortMenuItem = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("SVM Classification Editor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jToolBar1.setAlignmentX(0.0F);
        jToolBar1.setMaximumSize(new java.awt.Dimension(18, 50));
        jToolBar1.setMinimumSize(new java.awt.Dimension(18, 35));
        jToolBar1.setPreferredSize(new java.awt.Dimension(18, 35));
        inClassButton.setToolTipText(" move into class");
        inClassButton.setMaximumSize(new java.awt.Dimension(32, 32));
        inClassButton.setMinimumSize(new java.awt.Dimension(32, 32));
        inClassButton.setPreferredSize(new java.awt.Dimension(32, 32));
        inClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inClassButtonActionPerformed(evt);
            }
        });

        jToolBar1.add(inClassButton);

        outClassButton.setToolTipText("move out of class");
        outClassButton.setMaximumSize(new java.awt.Dimension(32, 32));
        outClassButton.setMinimumSize(new java.awt.Dimension(32, 32));
        outClassButton.setPreferredSize(new java.awt.Dimension(32, 32));
        outClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outClassButtonActionPerformed(evt);
            }
        });

        jToolBar1.add(outClassButton);

        neutralButton.setToolTipText("move to neutal status");
        neutralButton.setMaximumSize(new java.awt.Dimension(32, 32));
        neutralButton.setMinimumSize(new java.awt.Dimension(32, 32));
        neutralButton.setPreferredSize(new java.awt.Dimension(32, 32));
        neutralButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neutralButtonActionPerformed(evt);
            }
        });

        jToolBar1.add(neutralButton);

        searchButton.setToolTipText("search (Ctrl - s)");
        searchButton.setMaximumSize(new java.awt.Dimension(32, 32));
        searchButton.setMinimumSize(new java.awt.Dimension(32, 32));
        searchButton.setPreferredSize(new java.awt.Dimension(32, 32));
        jToolBar1.addSeparator(new Dimension(20,32));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        jToolBar1.add(searchButton);

        runButton.setToolTipText("Run SVM on current classification...");
        runButton.setMaximumSize(new java.awt.Dimension(32, 32));
        runButton.setMinimumSize(new java.awt.Dimension(32, 32));
        runButton.setPreferredSize(new java.awt.Dimension(32, 32));
        jToolBar1.addSeparator(new Dimension(20,32));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        jToolBar1.add(runButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jToolBar1, gridBagConstraints);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);
        saveAsMenuItem.setText("Save as...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator2);
        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setText("Run");
        closeMenuItem.setToolTipText("Applies current classification");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(closeMenuItem);
        jMenuBar2.add(fileMenu);
        editMenu.setMnemonic('E');
        editMenu.setText("Edit");
        svcApplyMenuItem.setText("Apply SVC File");
        svcApplyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svcApplyMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(svcApplyMenuItem);
        storedClusterMenuItem.setText("Apply Stored Cluster");
        storedClusterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storedClusterMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(storedClusterMenuItem);
        tcListMenuItem.setText("Apply Gene Index List");
        tcListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tcListMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(tcListMenuItem);
        editMenu.add(jSeparator3);
        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(selectAllMenuItem);
        jMenuBar2.add(editMenu);
        toolsMenu.setMnemonic('T');
        toolsMenu.setText("Tools");
        searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        searchMenuItem.setText("Search ");
        searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(searchMenuItem);
        sortByMenu.setText("Sort by...");
        indexSortMenuItem.setText("index");
        indexSortMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexSortMenuItemActionPerformed(evt);
            }
        });

        sortByMenu.add(indexSortMenuItem);
        classSortMenuItem.setText("classification");
        classSortMenuItem.setToolTipText("in->out->neutral");
        classSortMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classSortMenuItemActionPerformed(evt);
            }
        });

        sortByMenu.add(classSortMenuItem);
        toolsMenu.add(sortByMenu);
        jMenuBar2.add(toolsMenu);
        setJMenuBar(jMenuBar2);

        pack();
    }//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_runButtonActionPerformed
    
    private void classSortMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classSortMenuItemActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_classSortMenuItemActionPerformed
    
    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        table.selectAll();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed
    
    private void indexSortMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexSortMenuItemActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_indexSortMenuItemActionPerformed
    
    private void svcApplyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcApplyMenuItemActionPerformed
        this.applySVCFile();
    }//GEN-LAST:event_svcApplyMenuItemActionPerformed
    
    private void tcListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tcListMenuItemActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_tcListMenuItemActionPerformed
    
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchTable();
    }//GEN-LAST:event_searchButtonActionPerformed
    
    private void hideOutClassCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideOutClassCheckBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_hideOutClassCheckBoxActionPerformed
    
    private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
        searchTable();
    }//GEN-LAST:event_searchMenuItemActionPerformed
    
    private void neutralButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neutralButtonActionPerformed
        int [] selectedRows = table.getSelectedRows();
        setClassificationForSet(selectedRows, 3);
    }//GEN-LAST:event_neutralButtonActionPerformed
    
    private void outClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outClassButtonActionPerformed
        int [] selectedRows = table.getSelectedRows();
        setClassificationForSet(selectedRows, 2);
    }//GEN-LAST:event_outClassButtonActionPerformed
    
    private void inClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inClassButtonActionPerformed
        int [] selectedRows = table.getSelectedRows();
        setClassificationForSet(selectedRows, 1);
    }//GEN-LAST:event_inClassButtonActionPerformed
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        try{
            this.saveTableToCurrentSVC();
        } catch (IOException e){
            
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        try{
            this.saveTableAsSVC();
        } catch (IOException e){
            
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_fileMenuActionPerformed
    
    private void storedClusterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storedClusterMenuItemActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_storedClusterMenuItemActionPerformed
    
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeMenuItemActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        cancelForm = true;
        setVisible(false);
    }//GEN-LAST:event_exitForm
    
    //   /**
    //    * @param args the command line arguments
    //     */
    //  public static void main(String args[]) {
    //	new SVMClassificationEditor(null).show();
    //    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable table;
    private javax.swing.JButton searchButton;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton inClassButton;
    private javax.swing.JMenuItem indexSortMenuItem;
    private javax.swing.JButton outClassButton;
    private javax.swing.JMenuItem svcApplyMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu sortByMenu;
    private javax.swing.JButton runButton;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem classSortMenuItem;
    private javax.swing.JButton neutralButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem storedClusterMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem tcListMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JMenuItem searchMenuItem;
    // End of variables declaration//GEN-END:variables
    
    public class SVMTableModel extends AbstractTableModel{
        
        private Class [] types;
        private boolean [] canEdit;
        private TableModel tableModel;
        private Row [] rows;
        private int colToSort;
        private String [] headerNames;
        
        /** Constructs a new instance of SVMTableModel
         * @param HeaderNames <CODE>String</CODE> array of column names
         * @param Model <CODE>TableModel</CODE> to set as core TableModel for SVMTableModel
         */
        public SVMTableModel(String [] HeaderNames, TableModel Model){
            super();
            
            tableModel = Model;
            headerNames = HeaderNames;
            
            //create and init row objects
            rows = new Row[tableModel.getRowCount()];
            
            for(int i = 0; i < rows.length; i++){
                rows[i] = new Row();
                rows[i].index = i;
            }
            
            types = new Class[headerNames.length];
            
            canEdit = new boolean[types.length];
            
            for(int i = 0; i < types.length ; i++){
                if(i == 1 || i == 2 || i == 3){
                    types[i] = java.lang.Boolean.class;
                    canEdit[i] = true;
                }
                else{
                    types[i] = java.lang.Object.class;
                    canEdit[i] = false;
                }
            }
        }
        
        
        /** Sorts table rows based on column index.
         * @param c Column to use as sort key when sorting columns
         */
        public void sort(int c){
            colToSort = c;
            Arrays.sort(rows);
            table.repaint();
        }
        
        /** Sorts table rows by column header key.
         * @param key <CODE>String</CODE> key to identify sort column.
         */
        public void sortBy(String key){
            int col = getColumnIndex(key);
            
            if(col >= 0){
                sort(col);
                colToSort = col;
            }
        }
        
        
        private int getColumnIndex(String key){
            int i;
            
            for(i = 0; i < headerNames.length; i++){
                if(headerNames[i].equals(key))
                    break;
            }
            if(i < headerNames.length)
                return i;
            else
                return -1;
        }
        
        
        /** Accesses column class information.
         * @param columnIndex int column index
         * @return Returns the class associated with the column index.
         */
        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
        
        
        public java.lang.Object getValueAt(int row, int col) {
            return tableModel.getValueAt(rows[row].index, col);
        }
        
        public void setValueAt(Object obj, int row, int col){
            tableModel.setValueAt(obj, rows[row].index, col);
            this.fireTableChanged(new TableModelEvent(this, row, row, col));
        }
        
        public int getRowCount() {
            return tableModel.getRowCount();
        }
        
        public int getColumnCount() {
            return tableModel.getColumnCount();
        }
        
        public String getColumnName(int col){
            return tableModel.getColumnName(col);
        }
        
        public int convertToViewerRow(int row){
            if(row > -1 && row < rows.length)
                return rows[row].index;
            else
                return -1;
        }
        
        
        private class Row implements Comparable{
            public int index;
            
            public int compareTo(Object other){
                Row otherRow = (Row)other;
                Object myObject = tableModel.getValueAt(index, colToSort);
                Object otherObject = tableModel.getValueAt(otherRow.index, colToSort);
                if( myObject instanceof Comparable )
                    return ((Comparable)myObject).compareTo(otherObject);
                
                else if(myObject instanceof Boolean){
                    boolean myValue = ((Boolean)myObject).booleanValue();
                    boolean otherValue = ((Boolean)otherObject).booleanValue();
                    if(otherValue && !myValue)
                        return 1;
                    else if(!otherValue && myValue)
                        return -1;
                    else
                        return 0;
                }
                else return index - otherRow.index;
            }
        }
    }
    
    
    public class SortListener implements ActionListener{
        
        private SVMTableModel model;
        
        public SortListener(TableModel Model){
            model = (SVMTableModel)Model;
        }
        
        public void actionPerformed(ActionEvent ae){
            Object source = ae.getSource();
            
            if(source instanceof JMenuItem){
                String key = ((JMenuItem)source).getText();
                
                if(key.equals("index"))
                    model.sort(0);
                else if(key.equals("classification"))
                    model.sort(1);
                else
                    model.sortBy(key);
            }
        }
    }
    
    
    public class ClassificationCellRenderer extends DefaultTableCellRenderer implements javax.swing.table.TableCellRenderer{
        
        private JRadioButton button;
        private Color selectionBackgroundColor;
        private Color selectionForegroundColor;
        
        
        public ClassificationCellRenderer(JTable table){
            button = new JRadioButton();
            button.setBackground(Color.white);
            
            selectionBackgroundColor = table.getSelectionBackground();
            selectionForegroundColor = table.getForeground();
            
        }
        
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable jTable, java.lang.Object obj, boolean param, boolean param3, int row, int col) {
            button.setSelected( ((Boolean)obj).booleanValue() );
            
            return  button;
        }
    }
    
    
    
    public class ClassSelectionListener implements TableModelListener{
        
        
        public void tableChanged( TableModelEvent tme){
            
            tme.getSource();
            
            int selectedCol = tme.getColumn(); //
            int selectedRow = tme.getFirstRow(); //
            
            if(selectedCol < 1 || selectedCol > 3)
                return;
            
            if( verifySelected(selectedRow, selectedCol)){
                changeNeighbors(selectedRow, selectedCol);
            }
        }
        
        
        private void changeNeighbors(int first, int col){
            
            if(col == 1){
                table.setValueAt(new Boolean(false), first, 2);
                table.setValueAt(new Boolean(false), first, 3);
            }
            else if(col == 2){
                table.setValueAt(new Boolean(false), first, 1);
                table.setValueAt(new Boolean(false), first, 3);
                
            }
            else if(col == 3){
                table.setValueAt(new Boolean(false), first, 1);
                table.setValueAt(new Boolean(false), first, 2);
            }
        }
        
        
        private boolean verifySelected(int row, int col){
            
            boolean selVal = ((Boolean)table.getValueAt(row,col)).booleanValue();
            boolean value1, value2;
            
            if(selVal == true){
                return true;
            }
            else if(col == 1){
                value1 = ((Boolean)table.getValueAt(row,2)).booleanValue();
                value2 = ((Boolean)table.getValueAt(row,3)).booleanValue();
                if(!value1 && !value2)
                    table.setValueAt(new Boolean(true), row, col);
                
            }
            else if(col == 2){
                value1 = ((Boolean)table.getValueAt(row,1)).booleanValue();
                value2 = ((Boolean)table.getValueAt(row,3)).booleanValue();
                if(!value1 && !value2)
                    table.setValueAt(new Boolean(true), row, col);
            }
            else if(col == 3){
                value1 = ((Boolean)table.getValueAt(row,1)).booleanValue();
                value2 = ((Boolean)table.getValueAt(row,2)).booleanValue();
                if(!value1 && !value2)
                    table.setValueAt(new Boolean(true), row, col);
            }
            return false;
        }
    }
    
    
    
    
    public class SVMTableColumnModel implements TableColumnModel{
        
        TableColumnModel tcm;
        
        public SVMTableColumnModel(TableColumnModel TCM){
            tcm = TCM;
        }
        
        public int getColumnMargin() {
            return tcm.getColumnMargin();
        }
        
        public int[] getSelectedColumns() {
            return tcm.getSelectedColumns();
        }
        
        public int getColumnIndex(java.lang.Object obj) {
            return tcm.getColumnIndex(obj);
        }
        
        public void setColumnSelectionAllowed(boolean param) {
            tcm.setColumnSelectionAllowed(param);
        }
        
        public javax.swing.ListSelectionModel getSelectionModel() {
            return tcm.getSelectionModel();
        }
        
        public void moveColumn(int from, int to) {
            if(from < 4 || to < 4)
                return;
            else
                tcm.moveColumn(from, to);
        }
        
        public void setColumnMargin(int param) {
            tcm.setColumnMargin(param);
        }
        
        public boolean getColumnSelectionAllowed() {
            return tcm.getColumnSelectionAllowed();
        }
        
        public java.util.Enumeration getColumns() {
            return tcm.getColumns();
        }
        
        public void removeColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
            tcm.removeColumnModelListener(tableColumnModelListener);
        }
        
        public void removeColumn(javax.swing.table.TableColumn tableColumn) {
            return;
        }
        
        public int getColumnIndexAtX(int param) {
            return tcm.getColumnIndexAtX(param);
        }
        
        public int getSelectedColumnCount() {
            return tcm.getSelectedColumnCount();
        }
        
        public int getTotalColumnWidth() {
            return tcm.getTotalColumnWidth();
        }
        
        public void addColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
            tcm.addColumnModelListener(tableColumnModelListener);
        }
        
        public void addColumn(javax.swing.table.TableColumn tableColumn) {
            tcm.addColumn(tableColumn);
        }
        
        public void setSelectionModel(javax.swing.ListSelectionModel listSelectionModel) {
            tcm.setSelectionModel(listSelectionModel);
        }
        
        public javax.swing.table.TableColumn getColumn(int param) {
            return tcm.getColumn(param);
        }
        
        public int getColumnCount() {
            return tcm.getColumnCount();
        }
    }
    
    
    
    public class SVCFileFilter extends javax.swing.filechooser.FileFilter{
        
        public boolean accept(File f){
            return (f.getName().toLowerCase().endsWith(".svc") ||
            f.isDirectory() );
        }
        
        public String getDescription(){
            return "SVC File";
        }
    }
    
    public class SVCFileView extends javax.swing.filechooser.FileView{
        private Icon SVMIcon = GUIFactory.getIcon("svcfileicon.gif");
        
        public String getName(File f) {
            return null; // let the L&F FileView figure this out
        }
        
        public String getDescription(File f) {
            return null; // let the L&F FileView figure this out
        }
        
        public Boolean isTraversable(File f) {
            return null; // let the L&F FileView figure this out
        }
        
        public String getTypeDescription(File f) {
            String extension = getExtension(f);
            String type = null;
            
            if (extension != null) {
                if (extension.equals("svc")) {
                    type = "SVC File";
                }
            }
            return type;
        }
        
        public Icon getIcon(File f) {
            String extension = getExtension(f);
            Icon icon = null;
            if (extension != null) {
                if (extension.equals("svc")) {
                    icon = SVMIcon;
                }
            }
            return icon;
        }
        
        /**
         * Get the extension of a file.
         */
        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }
}
