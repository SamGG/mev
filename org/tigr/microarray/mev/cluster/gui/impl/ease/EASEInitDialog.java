/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: EASEInitDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004-02-06 22:52:36 $
 * $Author: braisted $
 * $State: Exp $
 */
/*
 * EaseInitDialog.java
 *
 * Created on August 25, 2003, 11:39 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;

import java.io.File;

import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


/** Accumulates parameters for execution of
 * EASE analysis.
 * @author braisted
 */
public class EASEInitDialog extends AlgorithmDialog {
    
    /** Result when dialog is dismissed.
     */    
    private int result = JOptionPane.CANCEL_OPTION;
    
    ModePanel modePanel;
    ClusterBrowser browser;
    EventListener listener;
    EaseParameterPanel easeParamPanel;
    AlphaPanel alphaPanel;
    Font font;    
    String sep;
    Frame parent;
    
    /** Creates a new instance of EaseInitDialog
     * @param parent Parent Frame
     * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
     * @param annotationLabels Annotation types
     */
    public EASEInitDialog(Frame parent, ClusterRepository repository, String [] annotationLabels) {
        super(new JFrame(), "EASE: Ease Annotation Analysis", true);
        this.parent = parent;
        font = new Font("Dialog", Font.BOLD, 12);                
        listener = new EventListener();
        addWindowListener(listener);
        
        //Tabbed pane creation
        JTabbedPane tabbedPane = new JTabbedPane();
        
        browser = new ClusterBrowser(repository);
        tabbedPane.add("Cluster Browser", browser);
        
        easeParamPanel = new EaseParameterPanel(annotationLabels);
        tabbedPane.add("Annotation Parameters", easeParamPanel);
        
        alphaPanel = new AlphaPanel();    
        tabbedPane.add("Statistical Parameters", alphaPanel);
        
        JPanel parameters = new JPanel(new GridBagLayout());
        parameters.setBackground(Color.white);
        
        //mode panel
        modePanel = new ModePanel(!(repository == null || repository.isEmpty()));
        
        parameters.add(modePanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        parameters.add(tabbedPane, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
       
        addContent(parameters);
        setActionListeners(listener);
        tabbedPane.setSelectedIndex(2);  //pack with largest pane visible
        this.pack();
        tabbedPane.setSelectedIndex(0);
    }
    
    
    /** Shows the dialog.
     * @return  */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /** Resets dialog controls.
     */    
    private void resetControls(){
        
    }
    
    /** Indicates if mode is cluster analysis, if not mode is annotation survey.
     * @return  */    
    public boolean isClusterModeSelected(){
        return this.modePanel.clusterAnalysisButton.isSelected();
    }
    
    /** Returns the cluster selected for analysis.
     * @return  */    
    public Cluster getSelectedCluster(){
        return this.browser.getSelectedCluster();
    }
    
    /** Returns the name of the converter file selected.
     * If none selected null is returned.
     */    
    public String getConverterFileName(){
        return easeParamPanel.getConverterFileName();
    }
    
    /** Returns the minimum clusters size if trimming result.
     */    
    public int getMinClusterSize() {
        String value = easeParamPanel.minClusterSizeField.getText();
        try {
            int size = Integer.parseInt(value);
            return size;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /** Returns the annotation key type.
     */    
    public String getAnnotationKeyType(){
        return easeParamPanel.getAnnotationKeyType();
    }
    
    
    /** Returns a list of file names corresponding to files mapping
     * indices to annotation terms (themes).
     */    
    public String [] getAnnToGOFileList(){
        return this.easeParamPanel.getAnnToGOFileList();
    }
    
    /** Returns the stat to report.  If true then EaseScore, else Fisher's Exact.
     */    
    public boolean isEaseScoreSelected(){
        return (this.alphaPanel.easeBox.isSelected());
    }
    
    /** Returns true if multiplicity corrections are
     * selected.
     */    
    public boolean isCorrectPvaluesSelected(){     
        return (this.isBonferroniSelected() || this.isStepDownBonferroniSelected() || this.isSidakSelected());
    }
    
    /** Returns true if Bonferroni correction is selected.
     */    
    public boolean isBonferroniSelected(){
        return alphaPanel.bonferroniBox.isSelected();
    }
    
    /** Returns true if step down Bonferroni is selected.
     */    
    public boolean isStepDownBonferroniSelected(){
        return alphaPanel.bonferroniStepBox.isSelected();
    }
    
    /** Returns true is Sidak method correction is selected
     */    
    public boolean isSidakSelected(){
        return alphaPanel.sidakBox.isSelected();        
    }
    
    /** Returns true if bootstrapping permutations
     * are selected.
     */    
    public boolean isPermutationAnalysisSelected(){
        return alphaPanel.permBox.isSelected();
    }
    
    /** Returns the number of permutations to perform.
     */    
    public int getPermutationCount(){
        return Integer.parseInt(alphaPanel.permField.getText());
    }
    
    /** Returns the trim options as two strings.
     * The first string indicates the type of trim
     * NO_TRIM, N_TRIM, PERCENT_TRIM.  The second string indicates the value to be applied.
     */    
    public String [] getTrimOptions(){
        String [] options = new String[2];
        if ( alphaPanel.trimBox.isSelected() ){
            if ( alphaPanel.trimPercentBox.isSelected() ){
                options[0] = "PERCENT_TRIM";
                options[1] = alphaPanel.trimPercentField.getText();
            } else {
                options[0] = "N_TRIM";
                options[1] = alphaPanel.trimNField.getText();
            }
        } else {
            options[0] = "NO_TRIM";
            options[1] = "0";
        }
        return options;
    }
    
    /** Contains mode controls. (anal. or survey)
     */    
    private class ModePanel extends JPanel {
        private JRadioButton clusterAnalysisButton;
        private JRadioButton slideSurveyButton;
        
        /** Constructs a mode panel.
         * @param haveClusters
         */        
        public ModePanel(boolean haveClusters){
            super(new GridBagLayout());
            setLayout(new GridBagLayout());
            setBackground(Color.white);
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mode Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
                        
            ButtonGroup bg = new ButtonGroup();
            clusterAnalysisButton = new JRadioButton("Cluster Analysis", haveClusters);
            clusterAnalysisButton.setFocusPainted(false);
            clusterAnalysisButton.setBackground(Color.white);    
            clusterAnalysisButton.setHorizontalAlignment(JRadioButton.CENTER);
            bg.add(clusterAnalysisButton);

            slideSurveyButton = new JRadioButton("Slide Annotation Survey");
            slideSurveyButton.setFocusPainted(false);
            slideSurveyButton.setBackground(Color.white);
            slideSurveyButton.setHorizontalAlignment(JRadioButton.CENTER);

            bg.add(slideSurveyButton);
            
            if(!haveClusters){
                slideSurveyButton.setSelected(true);
                clusterAnalysisButton.setEnabled(false);
            }
            
            add(clusterAnalysisButton, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
            add(slideSurveyButton, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        }
    }
        
    /** Contains annotation parameter controls.
     */    
    private class EaseParameterPanel extends JPanel {

        JTextField converterFileField;
        JList fileList;
        JButton browserButton;
        JTextField minClusterSizeField;
        JComboBox fieldNamesBox;
        
        JList annFileList;
        Vector annVector;
        JButton removeButton;
        JCheckBox useAnnBox;
        
        /** Constructs a new EaseParameterPanel
         * @param fieldNames annotation types
         */        
        public EaseParameterPanel(String [] fieldNames) {            
            //Conversion File Panel
            JPanel convPanel = new JPanel(new GridBagLayout());
            convPanel.setBackground(Color.white);
            convPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Annotation Conversion File", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            useAnnBox = new JCheckBox("use annotation converter", false);
            useAnnBox.setActionCommand("use-converter-command");
            useAnnBox.addActionListener(listener);
            useAnnBox.setBackground(Color.white);
            useAnnBox.setFocusPainted(false);
            
            converterFileField = new JTextField(30);
            converterFileField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray));
            converterFileField.setEnabled(false);
            
            browserButton = new JButton("File Browser");
            browserButton.setActionCommand("converter-file-browser-command");
            browserButton.setFocusPainted(false);
            browserButton.setPreferredSize(new Dimension(150, 25));
            browserButton.setSize(150, 25);
            browserButton.addActionListener(listener);
            browserButton.setEnabled(false);
            
            
            convPanel.add(useAnnBox, new GridBagConstraints(0,0,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            convPanel.add(new JLabel("File :"), new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            convPanel.add(this.browserButton, new GridBagConstraints(0,2,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(0,15,0,0),0,0));
            convPanel.add(this.converterFileField, new GridBagConstraints(1,1,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
                   
            //Annotation file panel
            JPanel annPanel = new JPanel(new GridBagLayout());
            annPanel.setBackground(Color.white);
            annPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gene Annotation / Gene Ontology Linking Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            annVector = new Vector();
            annFileList = new JList(new DefaultListModel());
            annFileList.setCellRenderer(new ListRenderer());
            annFileList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            JScrollPane annPane = new JScrollPane(annFileList);
            
            JButton  annButton = new JButton("Add Files");
            annButton.setActionCommand("ann-file-browser-command");
            annButton.addActionListener(listener);
            annButton.setFocusPainted(false);
            annButton.setPreferredSize(new Dimension(150, 25));
            annButton.setSize(150, 25);
            
            removeButton = new JButton("Remove Selected");
            removeButton.setActionCommand("remove-ann-file-command");
            removeButton.addActionListener(listener);
            removeButton.setFocusPainted(false);
            removeButton.setPreferredSize(new Dimension(150, 25));
            removeButton.setSize(150, 25);
            removeButton.setEnabled(false);
            
            JPanel fillPanel = new JPanel();
            fillPanel.setBackground(Color.white);
            annPanel.add(fillPanel, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            annPanel.add(annButton, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,5), 0,0));
            annPanel.add(removeButton, new GridBagConstraints(2,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,5,10,0), 0,0));
            annPanel.add(new JLabel("Files: "), new GridBagConstraints(0,1,1,1,0.0,0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
            annPanel.add(annPane, new GridBagConstraints(1,1,2,1,0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            
            sep = System.getProperty("file.separator");
            String tempPath = System.getProperty("user.dir")+sep+"Data"+sep+"EASE";
            File file = new File(tempPath);
            Vector fileVector = new Vector();
            fileList = new JList(fileVector);
            if(file.exists()){
                String [] listFileNames = file.list();
                for(int i = 0; i < listFileNames.length; i++){
                    File tempFile = new File(tempPath+sep+listFileNames[i]);
                    if(tempFile.isFile())
                        fileVector.add(listFileNames[i]);
                }
                if(fileVector.size() > 0){
                    converterFileField.setText(tempPath+sep+((String)fileVector.elementAt(0)));
                }
            }
            
            this.fieldNamesBox = new JComboBox(fieldNames);
            this.fieldNamesBox.setEditable(false);
                        
            minClusterSizeField = new JTextField(5);
            minClusterSizeField.setText("5");
            
            JPanel contentPanel = new JPanel(new GridBagLayout());

            //Content Panels
            //Ease File Panel
            JPanel easeFilePanel = new JPanel(new GridBagLayout());
            
            this.setLayout(new GridBagLayout());
            
            JPanel annotKeyPanel = new JPanel(new GridBagLayout());
            annotKeyPanel.setBackground(Color.white);
            annotKeyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "MeV Annotation Key  (\"Unique ID\")", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            annotKeyPanel.add(new JLabel("Annotation Key:  "), new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            annotKeyPanel.add(this.fieldNamesBox, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));   
            
            this.add(annotKeyPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(convPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(annPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));          
        }
        
        private void updateFileDirectoryField(){
            
            File file = new File((String)this.fileList.getSelectedValue());
            if(file == null)
                return;
            
            String tempPath = file.getParent();
            int fileIndex = this.fileList.getSelectedIndex();
            String fileName = (String)(this.fileList.getModel().getElementAt(this.fileList.getSelectedIndex()));
            this.converterFileField.setText(tempPath+sep+fileName);
        }
        
        private void updateAnnFileList(File [] files){
            File file;
            for(int i = 0; i < files.length; i++){
                file = files[i];
                if(!((DefaultListModel) annFileList.getModel()).contains(file)){
                    ((DefaultListModel) annFileList.getModel()).addElement(file);
                }
            }
            annFileList.validate();            
        }
        
        /** Returns the converter file name (or null if none)
         */        
        public String getConverterFileName(){
            if(this.useAnnBox.isSelected())
                return converterFileField.getText();
            return null;
        }
        
        /** Returns the annotation type string.
         */        
        public String getAnnotationKeyType(){
            return (String)this.fieldNamesBox.getSelectedItem();
        }
        
        private class EaseListListener implements ListSelectionListener {            
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                updateFileDirectoryField();
            }            
        }
        
        private void updateConverterFileField(String field){
            this.converterFileField.setText(field);
        }
        
        /** Returns the list of annotation-theme mapping files.
         */        
        public String [] getAnnToGOFileList(){
            String [] fileNames = new String[((DefaultListModel) annFileList.getModel()).size()];
            for(int i = 0; i < fileNames.length; i++){
                fileNames[i] = ((File)(((DefaultListModel)annFileList.getModel()).elementAt(i))).getPath();
            }
            return fileNames;
        }
        
        public void removeSelectedFiles(){            
            int [] indices = annFileList.getSelectedIndices();

            for(int i = 0; i < indices.length; i++){
              // annFileList.remove(indices[i]);
               ((DefaultListModel)annFileList.getModel()).removeElementAt(indices[i]);
            }
            if(annFileList.getModel().getSize() < 1){
                this.removeButton.setEnabled(false);
                okButton.setEnabled(false);
            }
            annFileList.validate();
        }
        
        private class ListRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = (File) value;
                setText(file.getName());
                return this;
            }
        }
    }
    
    /** Contains statistical parameter controls.
     */    
    private class AlphaPanel extends JPanel{

        //Stats
        private JCheckBox fisherBox;
        private JCheckBox easeBox;
        //mult. corrections
        private JCheckBox bonferroniBox;
        private JCheckBox sidakBox;
        private JCheckBox bonferroniStepBox;        
        private JCheckBox permBox;
        private JTextField permField;
        private JLabel permLabel;
        //Trim params
        private JCheckBox trimBox;
        private JCheckBox trimNBox;
        private JLabel trimNLabel;
        private JTextField trimNField;        
        private JCheckBox trimPercentBox;
        private JLabel trimPercentLabel;
        private JTextField trimPercentField;

        
        /** Constucts a new AlphaPanel.
         */        
        public AlphaPanel(){
            super(new GridBagLayout());
            setBackground(Color.white);
        
            //STAT PANEL
            JPanel statPanel = new JPanel(new GridBagLayout());
            statPanel.setBackground(Color.white);
            statPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Reported Statistic", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));            
            
            ButtonGroup bg = new ButtonGroup();
            fisherBox = new JCheckBox("Fisher Exact Probability", true);
            fisherBox.setBackground(Color.white);
            fisherBox.setFocusPainted(false);
            bg.add(fisherBox);
            
            easeBox = new JCheckBox("Ease Score", false);
            easeBox.setBackground(Color.white);
            easeBox.setFocusPainted(false);
            bg.add(easeBox);
            
            statPanel.add(fisherBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            statPanel.add(easeBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
                      
            
            //P-value Correction Panel
            JPanel correctionPanel = new JPanel(new GridBagLayout());
            correctionPanel.setBackground(Color.white);
            correctionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Multiplicity Corrections", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

            bonferroniBox = new JCheckBox("Bonferroni Correction", false);            
            bonferroniBox.setBackground(Color.white);
            bonferroniBox.setFocusPainted(false);
            
            bonferroniStepBox = new JCheckBox("Bonferroni Step Down Correction", false);            
            bonferroniStepBox.setBackground(Color.white);
            bonferroniStepBox.setFocusPainted(false);
            
            sidakBox = new JCheckBox("Sidak Method", false);            
            sidakBox.setBackground(Color.white);
            sidakBox.setFocusPainted(false);            
                    
            permBox = new JCheckBox("Resampling Probability Analysis", false);
            permBox.setActionCommand("permutation-analysis-command");
            permBox.setBackground(Color.white);
            permBox.setFocusPainted(false);
            permBox.addActionListener(listener);
            //permBox.setEnabled(false);
            
            permField = new JTextField("1000", 10);
            permField.setBackground(Color.white);
            //permField.setEnabled(false);
            
            permLabel = new JLabel("Number of Permutations");
            permLabel.setBackground(Color.white);
            //permLabel.setEnabled(false);
            
            correctionPanel.add(bonferroniBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(sidakBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(bonferroniStepBox, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
            correctionPanel.add(permBox, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            correctionPanel.add(permLabel, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
            correctionPanel.add(permField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
                       
            //Trim Panel            
            JPanel trimPanel = new JPanel(new GridBagLayout());
            trimPanel.setBackground(Color.white);
            trimPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Trim Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

            trimBox = new JCheckBox("Trim Resulting Groups", false);
            trimBox.setActionCommand("trim-result-command");
            trimBox.addActionListener(listener);
            trimBox.setBackground(Color.white);
            trimBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            trimBox.setFocusPainted(false);
            
            bg = new ButtonGroup();
            
            trimNBox = new JCheckBox("Select Minimum Hit Number", true);
            trimNBox.setActionCommand("trim-result-command");
            trimNBox.addActionListener(listener);
            trimNBox.setEnabled(false);            
            trimNBox.setBackground(Color.white);
            trimNBox.setFocusPainted(false);
            bg.add(trimNBox);
            
            trimNLabel = new JLabel("Min. Hits");
            trimNLabel.setBackground(Color.white);
            trimNLabel.setEnabled(false);
            
            trimNField = new JTextField("5", 10);
            trimNField.setEnabled(false);      
                       
            trimPercentBox = new JCheckBox("Select Minimum Hit Percentage", false);
            trimPercentBox.setActionCommand("trim-result-command");
            trimPercentBox.addActionListener(listener);
            trimPercentBox.setEnabled(false);
            trimPercentBox.setBackground(Color.white);
            trimPercentBox.setFocusPainted(false);
            bg.add(trimPercentBox);
                        
            trimPercentLabel = new JLabel("Percent Hits");
            trimPercentLabel.setBackground(Color.white);
            trimPercentLabel.setEnabled(false);
            
            trimPercentField = new JTextField("5", 10);
            trimPercentField.setEnabled(false);            
            
            trimPanel.add(trimBox, new GridBagConstraints(0,0,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));

            trimPanel.add(trimNBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            trimPanel.add(trimNLabel, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(0,20,0,15),0,0));
            trimPanel.add(trimNField, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));

            trimPanel.add(trimPercentBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            trimPanel.add(trimPercentLabel, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(10,20,0,15),0,0));
            trimPanel.add(trimPercentField, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE, new Insets(10,0,0,0),0,0));            

            //Add panels to main panel
            add(statPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            add(correctionPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            add(trimPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));            
        }
        
        /** Indicates if permutations are selected.
         */        
        public boolean performPermutations(){
            return permBox.isSelected();
        }
        
        public void setEnablePermutations(){
            permLabel.setEnabled(permBox.isSelected());
            permField.setEnabled(permBox.isSelected());
        }
        
        public void validateTrimOptions(){
            if(this.trimBox.isSelected()){                
                trimNBox.setEnabled(true);
                trimPercentBox.setEnabled(true);
                
                trimNLabel.setEnabled(trimNBox.isSelected());
                trimNField.setEnabled(trimNBox.isSelected());
                trimPercentLabel.setEnabled(!trimNBox.isSelected());
                trimPercentField.setEnabled(!trimNBox.isSelected());
            } else {
                trimNBox.setEnabled(false);
                trimPercentBox.setEnabled(false);
                
                trimNLabel.setEnabled(false);
                trimNField.setEnabled(false);
                trimPercentLabel.setEnabled(false);
                trimPercentField.setEnabled(false);
            }
        }
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class EventListener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("use-converter-command")) {
                if(easeParamPanel.useAnnBox.isSelected()){
                    easeParamPanel.browserButton.setEnabled(true);
                    easeParamPanel.converterFileField.setEnabled(true);
                } else {                 
                    easeParamPanel.browserButton.setEnabled(false);
                    easeParamPanel.converterFileField.setEnabled(false);   
                }
            } else if (command.equals("converter-file-browser-command")){
                String file = System.getProperty("user.dir")+sep+"Data"+sep+"EASE"+sep+"Data"+sep+"Convert";
                JFileChooser chooser = new JFileChooser(file);
                chooser.setDialogTitle("Annotation Converter Selection");
                chooser.setMultiSelectionEnabled(false);
                if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
                    easeParamPanel.updateConverterFileField(chooser.getSelectedFile().getPath());
                }
                return;
            } else if (command.equals("ann-file-browser-command")){
                String file = System.getProperty("user.dir")+sep+"Data"+sep+"EASE"+sep+"Data"+sep+"Class";
                JFileChooser chooser = new JFileChooser(file);
                chooser.setDialogTitle("Annotation --> GO Term, File(s) Selection");
                chooser.setMultiSelectionEnabled(true);
                if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
                    easeParamPanel.updateAnnFileList(chooser.getSelectedFiles());
                    easeParamPanel.removeButton.setEnabled(true);
                    okButton.setEnabled(true);
                }
            } else if (command.equals("remove-ann-file-command")){
                easeParamPanel.removeSelectedFiles();               
            }else if (command.equals("permutation-analysis-command")){
                alphaPanel.setEnablePermutations();
            } else if (command.equals("trim-result-command")){
                alphaPanel.validateTrimOptions();
            } else if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(EASEInitDialog.this, "EASE Initialization Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
        public void itemStateChanged(ItemEvent e) {
            //okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }        
    }
}
