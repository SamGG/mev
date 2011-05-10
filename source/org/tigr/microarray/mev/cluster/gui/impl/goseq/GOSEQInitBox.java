/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: goseq.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.goseq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSet;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSetList;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GeneSigDbGeneSets;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCInitDialog;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.SelectMultiFilesDialog;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class GOSEQInitBox extends AlgorithmDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    boolean okPressed = false;
    boolean okReady = false;
    Vector<String> exptNames;    
    ClusterBrowserPanel mPanel;
    GeneSetFilePanel clusterAnalysisPanel;
    JTabbedPane selectionPanel;
    ClusterRepository repository;
	private String[] annotFields;

	private JTabbedPane analysisTypeTab;

	private GOAnalysisPanel goPanel;

	private GeneSetFilePanel runDEAnalysisPanel;
    
    /** Creates new GOSEQInitBox */
    public GOSEQInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, String[] annotFields, ClusterRepository repository) {
        super(parentFrame, "GOSEQ Initialization", modality);
        this.annotFields = annotFields;
        this.exptNames = exptNames;  
        this.repository = repository;
        
        
        setBounds(0, 0, 1000, 850);
        setBackground(Color.white);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);

        analysisTypeTab = new JTabbedPane();
        clusterAnalysisPanel = new GeneSetFilePanel(false);
        goPanel = new GOAnalysisPanel();
        runDEAnalysisPanel = new GeneSetFilePanel(true);
        
        analysisTypeTab.add(goPanel, "GO Analysis");
        analysisTypeTab.add(clusterAnalysisPanel, "Cluster Analysis");
        analysisTypeTab.add(runDEAnalysisPanel, "Differential Expression Analysis");
        analysisTypeTab.setSelectedIndex(1);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(analysisTypeTab, constraints);
        pane.add(analysisTypeTab);   
        
        mPanel = new ClusterBrowserPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
         
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  
    }

    
    @Override
	public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
        }
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy, int anc, int fill) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.anchor = anc;
        gbc.fill = fill;
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    
    public boolean isOkPressed() {
        return okPressed;
    }    
    
    public void enableOK(){
    	this.okButton.setEnabled(true);
    } 

    class ClusterBrowserPanel extends JPanel {
		private static final long serialVersionUID = 1L;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        JTabbedPane tabbedmulg;
        JLabel infoLabel;
        JLabel infoLabel2;
        int numGroups=-1;
        private JTextField alphaField;
		private JTextField permField;
		private JTextField genesPerBinField;
		private JComboBox biasBox;
        
        public ClusterBrowserPanel() {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            this.setLayout(gridbag);
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "GOSEQ Parameters",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            
            JPanel topPanel =  new JPanel();
            topPanel.setBackground(Color.white);
            topPanel.setLayout(gridbag);

            JLabel alphaLabel = new JLabel("Significance Level: Alpha = ");
            buildConstraints(constraints, 0, 0, 1, 1, 30, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(alphaLabel, constraints);
            this.add(alphaLabel);
            
            alphaField = new JTextField(".05", 7);
            alphaField.setMinimumSize(new Dimension(50,20));
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
            gridbag.setConstraints(alphaField, constraints);
            this.add(alphaField);

            JLabel permLabel = new JLabel("Number of Permutations: ");
            buildConstraints(constraints, 0, 1, 1, 1, 30, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(permLabel, constraints);
            this.add(permLabel);
            
            permField = new JTextField("1000", 7);
            permField.setMinimumSize(new Dimension(50,20));
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
            gridbag.setConstraints(permField, constraints);
            this.add(permField);

            JLabel binCountLabel = new JLabel("Number of Genes per Transcript Length Bin: ");
            buildConstraints(constraints, 0, 2, 1, 1, 30, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(binCountLabel, constraints);
            this.add(binCountLabel);
            
            genesPerBinField = new JTextField("50", 7);
            genesPerBinField.setMinimumSize(new Dimension(50,20));
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 1, 2, 1, 1, 30, 0);
            gridbag.setConstraints(genesPerBinField, constraints);
            this.add(genesPerBinField);
            
            JLabel biasLabel = new JLabel("Account for differential expression bias in: ");
            buildConstraints(constraints, 0, 3, 1, 1, 30, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(biasLabel, constraints);
            this.add(biasLabel);
            
            String[] selectedBiases = new String[2+annotFields.length];
            selectedBiases[0] = "RNASeq Transcript Length";
            selectedBiases[1] = "Total Expression";
            for (int i=0; i<annotFields.length; i++){
            	selectedBiases[i+2] = annotFields[i];
            }
            biasBox = new JComboBox(selectedBiases);
            biasBox.setMinimumSize(new Dimension(50,20));
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 1, 3, 1, 1, 30, 0);
            gridbag.setConstraints(biasBox, constraints);
            this.add(biasBox);
        }       
        
        protected void reset(){
        }
    }
	private FileResourceManager frm;
	private boolean geneSigValid = false;
	private String errorString = "ERROR:";
	boolean msigOK = false;
	
	class GOAnalysisPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        ClusterBrowser clusterBrowser;
		public GOAnalysisPanel(){
			this.setLayout(gridbag);
            clusterBrowser =  new ClusterBrowser(repository);
            buildConstraints(constraints, 0, 0, 1, 1, 1, 5,GridBagConstraints.CENTER,GridBagConstraints.BOTH);
            gridbag.setConstraints(clusterBrowser, constraints);
            this.add(clusterBrowser, constraints);
		}
		
	}

	class RunAnalysisPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RunAnalysisPanel(){
			
		}
		
	}
    class GeneSetFilePanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JLabel chooseFileLabel = new JLabel("Choose File: ");
		JLabel chooseAnnoLabel = new JLabel("Choose Annotation Type: ");

		private String[] genesetFilePath={""};
		JDialog jd;
		private JButton browseDownloadButton;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        public JTextField filePath = new JTextField();
        JTextField jtf;
		private JComboBox geneIdentifierBox;
		String broademail = "";
		private JComboBox analysisBox;
		private JComboBox geneSetSelectionBox;
	    ClusterBrowser clusterBrowser;
		public GeneSetFilePanel(boolean isRun){
			this.setBackground(Color.white);
			this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gene Set File",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            setLayout(gridbag);
			String buttonName = "Browse";
			String actionCommand = "genesigdb_download";
    		String[] selectionMethods=new String[3];
            selectionMethods[0]="Load local geneset file/files";
            selectionMethods[1]="Download from MSigDB";
            selectionMethods[2]="Download from GeneSigDB";
            
            geneSetSelectionBox=new JComboBox(selectionMethods);
            geneSetSelectionBox.addActionListener(new Listener());
            
            jd = new JDialog();
            jd.setLayout(gridbag);
			jd.setTitle("Please enter a valid MSigDB email address");
			jtf = new JTextField(25);
			jd.add(jtf);
			JButton jb = new JButton("OK");
			jb.addActionListener(new Listener(){
    			@Override
				public void actionPerformed(ActionEvent e){
    				broademail = jtf.getText();
    				jd.dispose();
    				
    			}
			});
			
            buildConstraints(constraints, 0, 0, 2, 2, 0, 0);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(jtf, constraints);
            jd.add(jtf);
            buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
            gridbag.setConstraints(jb, constraints);
            jd.add(jb);
			jd.setPreferredSize(new Dimension(400, 130));
			jd.setMinimumSize(new Dimension(400, 130));
			jd.setSize(new Dimension(400, 130));
			jd.setLocationRelativeTo(this);
			jd.pack();
			
			jd.setModal(true);

            constraints.fill = GridBagConstraints.NONE;
            filePath.setEditable(false);
            
            browseDownloadButton = new JButton(buttonName);
    		browseDownloadButton.setName(buttonName);
    		browseDownloadButton.setActionCommand(actionCommand);
    		browseDownloadButton.setSize(new Dimension(100, 30));
    		browseDownloadButton.setPreferredSize(new Dimension(100, 30));
    		browseDownloadButton.addActionListener(new Listener(){
    			@Override
				public void actionPerformed(ActionEvent e){
    				if(geneSetSelectionBox.getSelectedIndex()==0) {//browse local file
    					if (!onBrowse())
    						return;
    					filePath.setText(genesetFilePath[0]); 
    					browseDownloadButton.setEnabled(false);
    					geneSetSelectionBox.setEnabled(false);
    					chooseFileLabel.setForeground(Color.red);
    					chooseFileLabel.setText("File Loaded");
    				}else if(geneSetSelectionBox.getSelectedIndex()==1) {
						jd.setVisible(true);
						if (broademail.length()==0)
							return;
						BROADDownloads(broademail);
						if (msigOK){
							geneIdentifierBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);	
							geneIdentifierBox.setEnabled(false);					
        					browseDownloadButton.setEnabled(false);
        					geneSetSelectionBox.setEnabled(false);
        					chooseFileLabel.setForeground(Color.red);
        					chooseFileLabel.setText("File Loaded");
        					String allfiles = "";
        					for (int i=0; i<genesetFilePath.length; i++){
        						allfiles = allfiles +genesetFilePath[i];
        						if (i<genesetFilePath.length-1)
        							allfiles = allfiles +"; ";
        					}
        					filePath.setText(allfiles);    		
						} else {
							JOptionPane.showMessageDialog(null, "Invalid MSigDB email address.", "Error", JOptionPane.ERROR_MESSAGE);
		                }
    				}else if(e.getActionCommand().equalsIgnoreCase("genesigdb_download")) {
    					GeneSigDBDownloads();
						geneIdentifierBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);
						geneIdentifierBox.setEnabled(false);
    					browseDownloadButton.setEnabled(!geneSigValid);
    					geneSetSelectionBox.setEnabled(!geneSigValid);
    					chooseFileLabel.setForeground((geneSigValid ? Color.red: Color.black));
    					chooseFileLabel.setText(geneSigValid ? "File Loaded": "Choose File:");
    					filePath.setText(genesetFilePath[0]);  	
    				}
    			}
    		});

            Field[]fields=AnnotationFieldConstants.class.getFields();
            
            String[]annotation=new String[fields.length+1];
            annotation[0]="";
            try{
            for(int index=0; index<fields.length; index++){
            	annotation[index+1]=(String)fields[index].get(new AnnotationFieldConstants());
            }
            }catch(Exception e){
            	
            }
            
            geneIdentifierBox=new JComboBox(annotFields);
            geneIdentifierBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);
            
            String[] analysisTypes = {"EDGER","DEGSEQ","DESEQ"};
            analysisBox=new JComboBox(analysisTypes);
            analysisBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);
            
            constraints.anchor = GridBagConstraints.EAST;
            int xind = 0;
            int yind = 0;
            if (isRun){ //run analysis first

                buildConstraints(constraints, xind++, yind, 1, 1, 5, 10);
                this.add(new JLabel("Select Differential Expression Analysis"), constraints);
                buildConstraints(constraints, xind++, yind, 1, 1, 5, 10);
                gridbag.setConstraints(analysisBox, constraints);
                this.add(analysisBox);
            }
            xind=0;
            buildConstraints(constraints, xind++, ++yind, 1, 1, 5, 10);
            this.add(chooseFileLabel, constraints);
            buildConstraints(constraints, xind++, yind, 1, 1, 5, 10);
            gridbag.setConstraints(geneSetSelectionBox, constraints);
            this.add(geneSetSelectionBox);
            buildConstraints(constraints, xind++, yind, 1, 1, 5, 10);
            gridbag.setConstraints(browseDownloadButton, constraints);
            this.add(browseDownloadButton);
            
            buildConstraints(constraints, xind=0, ++yind, 1, 1, 5, 10);
            gridbag.setConstraints(chooseAnnoLabel, constraints);
            this.add(chooseAnnoLabel);
            buildConstraints(constraints, ++xind, yind, 1, 1, 5, 10);
            gridbag.setConstraints(geneIdentifierBox, constraints);
            this.add(geneIdentifierBox);
            
            buildConstraints(constraints, xind=0, ++yind, 1, 1, 5, 10);
            constraints.anchor = GridBagConstraints.EAST;
            this.add(new JLabel("File Location: "), constraints);
            buildConstraints(constraints, ++xind, yind, 2, 1, 5, 10);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(filePath, constraints);
            this.add(filePath);
            
            if (!isRun){
                JLabel infoLabel = new JLabel("Please select a cluster of differentially expressed genes");
    			infoLabel.setMaximumSize(new Dimension(50,50));
    			Font font = infoLabel.getFont();
    			infoLabel.setFont(font.deriveFont(10.0f));
	            buildConstraints(constraints, xind=0, ++yind, 3, 1, 0, 5,GridBagConstraints.CENTER,GridBagConstraints.NONE);
    			gridbag.setConstraints(infoLabel, constraints);
                this.add(infoLabel, constraints);
                
	            clusterBrowser =  new ClusterBrowser(repository);
	            buildConstraints(constraints, xind=0, ++yind, 3, 1, 0, 5,GridBagConstraints.CENTER,GridBagConstraints.NONE);
	            constraints.fill = GridBagConstraints.BOTH;
	            gridbag.setConstraints(clusterBrowser, constraints);
	            this.add(clusterBrowser, constraints);
            }
    	} 
	    private void BROADDownloads(String broademail) {
			try {
				
				frm = new FileResourceManager(new File(new File(System.getProperty("user.home"), ".mev"), "repository"));
				
				//Get the file containing the list of available geneset files.
				File geneSetList = frm.getSupportFile(new BroadGeneSetList(), true);
				try {
					//Parse the list of geneset files into filename strings
					ArrayList<String> genesetFilenames = BroadGeneSetList.getFileNames(geneSetList);

					//get email address from user
					String email = broademail;
					String[] genesetFileNameArray=new String[genesetFilenames.size()];				
					int index=0;
					ArrayList<ISupportFileDefinition> defs = new ArrayList<ISupportFileDefinition>();
					Iterator<String> it = genesetFilenames.iterator();
					//Add each geneset file name to a String array
					while(it.hasNext()) {
						genesetFileNameArray[index] = it.next();
						index=index+1;
						
					}
					//Ask the resource manager to download a file for each definition
					SelectMultiFilesDialog dialog = new SelectMultiFilesDialog(new JFrame(), "Select files to download", ((new BroadGeneSetList()).getURL().getHost()), genesetFileNameArray);
					dialog.setVisible(true);
					
					int[] indices = dialog.getSelectedFilesIndices();
					String[] selectedFiles = new String[indices.length];
					for(int i=0; i<indices.length; i++) {
						selectedFiles[i] = genesetFilenames.get(indices[i]);
						//Create a definition for each geneset file
					//	System.out.println("Selected file names:"+selectedFiles[i]);
						defs.add(new BroadGeneSet(selectedFiles[i], email));
					}
					
					
					Hashtable<ISupportFileDefinition, File> results = frm.getSupportFiles(defs, true);
					
					//Check each file for validity, print a list of the valid downloaded files
					Enumeration<ISupportFileDefinition> e = results.keys();
					ArrayList<String> arl = new ArrayList<String>();
					while(e.hasMoreElements()) {
						ISupportFileDefinition thisDef = e.nextElement();
						File temp = results.get(thisDef);
						if (!isValidAddress(temp)){
							msigOK = false;
							temp.deleteOnExit();
							return;
						}					
						if(thisDef.isValid(temp)) {
							arl.add(temp.getAbsolutePath());
							msigOK = true;
						} else {
							System.out.println("support file not downloaded " + temp.getAbsolutePath());
						}
					}
					this.genesetFilePath = new String[arl.size()];
					for (int i=0; i<genesetFilePath.length; i++){
						genesetFilePath[i] = arl.get(i);
					}
					
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				
			} catch (SupportFileAccessError sfae) {
				sfae.printStackTrace();
			}catch (RepositoryInitializationError rie) {
				rie.printStackTrace();
			}
		
		} 
	    
		private void GeneSigDBDownloads() {
			
			try {
				File file = new File(new File(System.getProperty("user.home"), ".mev"), "repository/org.tigr.microarray.mev.cluster.gui.impl.gsea.GeneSigDbGeneSets/genesigdb_genesets.txt");
				if (!file.exists()){
					frm = new FileResourceManager(new File(new File(System.getProperty("user.home"), ".mev"), "repository"));	
					GeneSigDbGeneSets temp = new GeneSigDbGeneSets();
					File geneSigs = frm.getSupportFile(temp, true);
					if(temp.isValid(geneSigs)) {
						genesetFilePath = new String[1];
						genesetFilePath[0]=geneSigs.getAbsolutePath();
						geneSigValid = true;
					}
				} else {
					genesetFilePath = new String[1];
					genesetFilePath[0]=file.getAbsolutePath();
					geneSigValid = true;				
					System.out.println(file.getAbsolutePath());
				}
							
			} catch (SupportFileAccessError sfae) {
				System.out.println("Could not download GeneSigDbGeneSets file.");
			} catch (RepositoryInitializationError e) {
					e.printStackTrace();
			}
		}
		public boolean onBrowse(){
			JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);		
			int retVal = fileChooser.showOpenDialog(this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				genesetFilePath = new String[1];
				this.genesetFilePath[0] = selectedFile.getAbsolutePath();
				return true;
			}
			return false;
		}
		private boolean isValidAddress(File f){
			try {
				FileReader fr = null;
				BufferedReader buff = null;
				fr = new FileReader(f);
				buff = new BufferedReader(fr);
	            String line = buff.readLine();
	            if (line.startsWith(errorString)){
	            	return false;
	            }            
				return true;
			} catch (IOException ioe) {
				return false;
			}
		}
		private class Listener implements ActionListener{

			public void actionPerformed(ActionEvent e) {
				if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Download from MSigDB")){
					browseDownloadButton.setText("Download");
				}else if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Load local geneset file/files")){
					browseDownloadButton.setText("Browse");
				}else if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Download from GeneSigDB")) {
					browseDownloadButton.setText("Download");
				}			
			}
		}
    }
	
	


  
    public class RadioButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ae) {
        }
    	
    }
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	//check parameters
            	if (!isParamSufficient())
            		return;
                okPressed = true;
            	dispose();
            } else if (command.equals("reset-command")) {
                mPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
            	HelpWindow.launchBrowser(GOSEQInitBox.this, "GO Analysis for RNA-seq- Initialization Dialog");
                okPressed = false;
            }
        }
    }
    /**
     * Checks to make sure samples have been properly assigned to groups for each experimental design.
     * 
     * @return true, if the group assignment is sufficient.
     * false, if the group assignment is lacking.
     */
    private boolean isParamSufficient(){
    	if (this.isRunDEAnalysis()){
    		if (this.runDEAnalysisPanel.genesetFilePath[0].equals("")){
    			JOptionPane.showMessageDialog(null, "Please choose a gene set file.", "Error", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    	}
    	if (this.isClusterAnalysis()){
    		if (clusterAnalysisPanel.genesetFilePath[0].equals("")){
    			JOptionPane.showMessageDialog(null, "Please choose a gene set file.", "Error", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    		if (this.repository==null||this.repository.isEmpty()){
    			JOptionPane.showMessageDialog(null, "Cluster Repository is empty.\n" +
    					"Please create a cluster or choose an alternative analysis.", "Error", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    	}
    	if (this.isGOAnalysis()){
    		if (this.repository==null||this.repository.isEmpty()){
				JOptionPane.showMessageDialog(null, "Cluster Repository is empty.\n" +
						"Please create a cluster or choose an alternative analysis.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
    	}
    	return true;
    }

	public String[] getClusterGeneSetFilePath() {
		return this.clusterAnalysisPanel.genesetFilePath;
	}

	public String[] getDEGeneSetFilePath() {
		return this.runDEAnalysisPanel.genesetFilePath;
	}

	public boolean isGOAnalysis() {
		return analysisTypeTab.getSelectedIndex()==0;
	}

	private boolean isClusterAnalysis() {
		return analysisTypeTab.getSelectedIndex()==1;	
	}
	
	public boolean isRunDEAnalysis() {
		return analysisTypeTab.getSelectedIndex()==2;
	}

	public int getClusterGeneSetOrigin() {
		return this.clusterAnalysisPanel.geneSetSelectionBox.getSelectedIndex();
	}

	public int getDEGeneSetOrigin() {
		return this.runDEAnalysisPanel.geneSetSelectionBox.getSelectedIndex();
	}
	
    public float getAlpha() {
    	return Float.parseFloat(mPanel.alphaField.getText());
    }

    public int getNumPerms() {
    	return Integer.parseInt(mPanel.permField.getText());
    }

    public int getNumBins() {
    	return Integer.parseInt(mPanel.genesPerBinField.getText());
    }
    
    public int[] getDiffGeneSet(){
		if (this.isGOAnalysis())
			return this.goPanel.clusterBrowser.getSelectedCluster().getIndices();
		if (this.isClusterAnalysis())
			return this.clusterAnalysisPanel.clusterBrowser.getSelectedCluster().getIndices();
		return null;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 24; i++) {
            dummyVect.add("Expt " + i);
        }
        String[] anfi = {"asdasd","qweqweqwe","zxczczxc"};
        GOSEQInitBox oBox = new GOSEQInitBox(dummyFrame, true, dummyVect, anfi, null);
        oBox.setVisible(true);
        System.exit(0);
    }


	public String getSelectedAnnotation() {
		if (this.isGOAnalysis())
			return "Probe_ID";
		if (this.isClusterAnalysis())
			return (String)this.clusterAnalysisPanel.geneIdentifierBox.getSelectedItem();
		if (this.isRunDEAnalysis())
			return (String)this.runDEAnalysisPanel.geneIdentifierBox.getSelectedItem();
		return null;
	}

	public String getBias() {
		return (String)this.mPanel.biasBox.getSelectedItem();
	}


	public int[] getClusterExpIndices() {
		if (this.isGOAnalysis())
			return this.goPanel.clusterBrowser.getSelectedCluster().getExperimentIndices();
		if (this.isClusterAnalysis())
			return this.clusterAnalysisPanel.clusterBrowser.getSelectedCluster().getExperimentIndices();
		return null;
	}


	public String getDEAnalysis() {
		return (String)this.runDEAnalysisPanel.analysisBox.getSelectedItem();
		
	}
}

