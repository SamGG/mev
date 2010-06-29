/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.globanc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSet;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSetList;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GeneSigDbGeneSets;
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
public class GLOBANCInitBox extends AlgorithmDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int JUST_ALPHA = 1;
    public static final int STD_BONFERRONI = 2;
    public static final int ADJ_BONFERRONI = 3;
    public static final int MAX_T = 9; 
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;    
    public static final int BUTTON_SELECTION = 14;
    public static final int CLUSTER_SELECTION = 15;
    
    boolean okPressed = false;
    boolean okReady = false;
    boolean step2 = false;
    Vector<String> exptNames;    
    MultiClassPanel mPanel;
    GeneSetFilePanel gsfPanel;
    JTabbedPane selectionPanel;
    ClusterRepository repository;
    JButton step2Button = new JButton("Continue...");
    String[] annotFields;
    
    /** Creates new Global AncovaInitBox */
    public GLOBANCInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, String[] annotFields, ClusterRepository repository) {
        super(parentFrame, "Global Ancova Initialization", modality);
        this.annotFields = annotFields;
        this.exptNames = exptNames;  
        this.repository = repository;
        
        setBounds(0, 0, 1000, 850);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        this.okButton.setEnabled(false);
          
        mPanel = new MultiClassPanel();

        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  
    }

    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy, int anc) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.anchor = anc;
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
    
	private JComboBox geneSetSelectionBox;
	private FileResourceManager frm;
	private String[] genesetFilePath={""};
	private boolean geneSigValid = false;
	private String errorString = "ERROR:";
	boolean msigOK = false;
    class GeneSetFilePanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JLabel chooseFileLabel = new JLabel("Choose File: ");
		JLabel chooseAnnoLabel = new JLabel("Choose Annotation Type: ");
		JDialog jd;
		private JButton browseDownloadButton;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        public JTextField filePath = new JTextField();
        JTextField jtf;
		private JComboBox geneIdentifierBox;
		String broademail = "";
		public GeneSetFilePanel(){
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
    			public void actionPerformed(ActionEvent e){
    				if(geneSetSelectionBox.getSelectedIndex()==0) {//browse local file
    					if (!onBrowse())
    						return;
    					filePath.setText(genesetFilePath[0]); 
    					browseDownloadButton.setEnabled(false);
    					geneSetSelectionBox.setEnabled(false);
    					chooseFileLabel.setForeground(Color.red);
    					chooseFileLabel.setText("File Loaded");
    					step2Button.setEnabled(true);
    				}else if(geneSetSelectionBox.getSelectedIndex()==1) {
						jd.setVisible(true);
						if (broademail.length()==0)
							return;
						BROADDownloads(broademail);
						if (msigOK){
							geneIdentifierBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);						
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
        					step2Button.setEnabled(true);		
						} else {
							JOptionPane.showMessageDialog(null, "Invalid MSigDB email address.", "Error", JOptionPane.ERROR_MESSAGE);
		                }
    				}else if(e.getActionCommand().equalsIgnoreCase("genesigdb_download")) {
    					GeneSigDBDownloads();
    					browseDownloadButton.setEnabled(!geneSigValid);
    					geneSetSelectionBox.setEnabled(!geneSigValid);
    					chooseFileLabel.setForeground((geneSigValid ? Color.red: Color.black));
    					chooseFileLabel.setText(geneSigValid ? "File Loaded": "Choose File:");
    					filePath.setText(genesetFilePath[0]);    		
    					step2Button.setEnabled(true);			
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
            
            
            constraints.anchor = GridBagConstraints.EAST;
            int xind = 0;
            buildConstraints(constraints, xind++, 0, 1, 1, 5, 10);
            this.add(chooseFileLabel, constraints);
            buildConstraints(constraints, xind++, 0, 1, 1, 5, 10);
            gridbag.setConstraints(geneSetSelectionBox, constraints);
            this.add(geneSetSelectionBox);
            buildConstraints(constraints, xind++, 0, 1, 1, 5, 10);
            gridbag.setConstraints(browseDownloadButton, constraints);
            this.add(browseDownloadButton);
            
            buildConstraints(constraints, xind=0, 1, 1, 1, 5, 10);
            gridbag.setConstraints(chooseAnnoLabel, constraints);
            this.add(chooseAnnoLabel);
            buildConstraints(constraints, ++xind, 1, 1, 1, 5, 10);
            gridbag.setConstraints(geneIdentifierBox, constraints);
            this.add(geneIdentifierBox);
            
            buildConstraints(constraints, xind=0, 2, 1, 1, 5, 10);
            constraints.anchor = GridBagConstraints.EAST;
            this.add(new JLabel("File Location: "), constraints);
            buildConstraints(constraints, ++xind, 2, 2, 1, 5, 10);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(filePath, constraints);
            this.add(filePath);
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
    
	private class Listener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Download from MSigDB")){
				gsfPanel.browseDownloadButton.setText("Download");
			}else if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Load local geneset file/files")){
				gsfPanel.browseDownloadButton.setText("Browse");
			}else if(((String)geneSetSelectionBox.getSelectedItem()).equalsIgnoreCase("Download from GeneSigDB")) {
				gsfPanel.browseDownloadButton.setText("Download");
			}			
		}
	}
    
    class MultiClassPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		DesignPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        ExperimentsSelectionPanel sampleSelectionPanel;
        ExperimentsSelectionPanel fullModelESP;
        ExperimentsSelectionPanel reducedModelESP;
        ExperimentsSelectionPanel ConditionESP;
        ExperimentsSelectionPanel TimePointESP;
        JTabbedPane tabbedmulg;
        ClusterSelector groupsCS,factorACS,factorBCS,conditionCS,timepointCS;
        JLabel infoLabel;
        JLabel infoLabel2;
        int numFullGroups=-1;
        int numRedGroups=-1;
        int numPerms = 100;
        String factorAName = "Full Model";
        String factorBName = "Reduced Model";
        
        public MultiClassPanel() {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            this.setLayout(gridbag);
            ngPanel = new DesignPanel();

            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ngPanel, constraints);
            
            step2Button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	if (step2){
                		goBack();
                		return;
                	}
                	initiatePanels();
                }
            });
            step2Button.setEnabled(false);
            
            gsfPanel = new GeneSetFilePanel();            
            buildConstraints(constraints, 0, 0,1,1,100,10);
            gridbag.setConstraints(gsfPanel, constraints);
            this.add(gsfPanel); 
            
            JPanel topPanel =  new JPanel();
            topPanel.setBackground(Color.white);
            topPanel.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 2, 75, 100);
            gridbag.setConstraints(ngPanel, constraints);
            topPanel.add(ngPanel);
            
            buildConstraints(constraints, 1, 1, 1, 1, 0, 10);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(step2Button, constraints);
            topPanel.add(step2Button);
            constraints.fill = GridBagConstraints.BOTH;

            topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Global Ancova Parameters",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 0, 1,1,1,100,10);
            gridbag.setConstraints(topPanel, constraints);
            this.add(topPanel);            
            
            infoLabel = new JLabel("Sample Group Assignment");
            infoLabel.setMaximumSize(new Dimension(50,50));
            Font font = infoLabel.getFont();
            infoLabel.setFont(font.deriveFont(20.0f));
            buildConstraints(constraints, 0, 2, 1, 1, 0, 5,GridBagConstraints.CENTER,GridBagConstraints.NONE);
            gridbag.setConstraints(infoLabel, constraints);
            
            this.add(infoLabel, constraints);
            infoLabel2 = new JLabel("Please select the Global Ancova parameters on which to run the analysis, then click 'Continue'.");
            buildConstraints(constraints, 0, 3, 1, 1, 100, 5,GridBagConstraints.CENTER);
            gridbag.setConstraints(infoLabel2, constraints);
            
            this.add(infoLabel2, constraints);
            
            buildConstraints(constraints, 0, 4, 1, 1, 100, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
        }
        private void goBack(){
    		infoLabel.setVisible(true);
            infoLabel2.setVisible(true);
            ngPanel.numFullGroupsField.setEnabled(true);
            ngPanel.numReducedGroupsField.setEnabled(true);
            ngPanel.numPermsField.setEnabled(true);
            step2Button.setText("Continue...");
            step2 = false;
            tabbedmulg.setVisible(false);
            buildConstraints(constraints, 0, 1, 2, 1, 0, 90);
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 3, 1, 1, 100, 90);
            gridbag.setConstraints(dummyPanel, constraints);
            MultiClassPanel.this.add(dummyPanel);
            step2Button.setSelected(false);
        }
        
        private void initiatePanels(){
            ngPanel.okPressed = true;
            okReady = true;
            try {
        		numFullGroups = Integer.parseInt(ngPanel.numFullGroupsField.getText());
            	numRedGroups = Integer.parseInt(ngPanel.numReducedGroupsField.getText());
            	numPerms = Integer.parseInt(ngPanel.numPermsField.getText());
            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Error reading parameter input.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ((numFullGroups<2||numRedGroups<2)){ //checks factorial design group amounts
            	JOptionPane.showMessageDialog(null, "The number of groups in each factor must be greater than 1.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JPanel selectionPanel = new JPanel();
            GridBagLayout gbg = new GridBagLayout();
            selectionPanel.setLayout(gbg);
            GridBagConstraints cnstr = new GridBagConstraints();

            buildConstraints(cnstr, 0, 0, 1, 1, 1, 1);
            cnstr.fill = GridBagConstraints.BOTH;
            JPanel clusterSelectorPanel = new JPanel();
            MultiClassPanel.this.remove(dummyPanel);
            tabbedmulg = new JTabbedPane();

    		fullModelESP = new ExperimentsSelectionPanel(exptNames, this.numFullGroups, "Full Model", false);
    		reducedModelESP = new ExperimentsSelectionPanel(exptNames, this.numRedGroups, "Reduced Model", false);
            selectionPanel.add(fullModelESP, cnstr);
    		cnstr.gridx = 1;
    		selectionPanel.add(reducedModelESP, cnstr);
    		cnstr.gridy++;
    		cnstr.gridx--;
    		cnstr.gridwidth=2;
    		cnstr.weighty = 0;
    		selectionPanel.add(createSaveLoadPanel(), cnstr);
            
            factorACS= new ClusterSelector(repository, numFullGroups, "Full Model");
            factorBCS= new ClusterSelector(repository, numRedGroups, "Reduced Model");
            if (repository!=null){
            	factorACS.setClusterType(factorAName);
            	factorBCS.setClusterType(factorBName);
    		}

            buildConstraints(cnstr, 0, 1, 1, 1, 1, 1);
            cnstr.fill = GridBagConstraints.BOTH;
        	clusterSelectorPanel.add(factorACS, cnstr);
        	cnstr.gridx = 1;
            clusterSelectorPanel.add(factorBCS, cnstr);            
            
            tabbedmulg.add("Button Selection", selectionPanel);
            tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
            tabbedmulg.setSelectedIndex(1);
            if (repository==null||repository.isEmpty())
            	tabbedmulg.setSelectedIndex(0);
            buildConstraints(constraints, 0, 2, 2, 1, 0, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tabbedmulg, constraints);
            MultiClassPanel.this.add(tabbedmulg);
            MultiClassPanel.this.validate();
            enableOK();
            ngPanel.numFullGroupsField.setEnabled(false);
            ngPanel.numReducedGroupsField.setEnabled(false);
            ngPanel.numPermsField.setEnabled(false);
            step2Button.setText("<<< Go Back");
            infoLabel.setVisible(false);
            infoLabel2.setVisible(false);
            step2 = true;
        }
        class DesignPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			JTextField factorAName, factorBName, factorALevel, factorBLevel, numFullGroupsField, numReducedGroupsField, numPermsField;//, alphaField;
            JLabel numGroupsLabel,numPermsLabel;
            JPanel factorPanel;
            boolean okPressed = false;
            public DesignPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                this.setLayout(gridbag);
                this.setMinimumSize(new Dimension(300,100));
                
                numGroupsLabel = new JLabel("Number of full model groups: ");
                numGroupsLabel.setVisible(true);
                buildConstraints(constraints, 0, 1, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numGroupsLabel, constraints);
                this.add(numGroupsLabel);
                
                numFullGroupsField = new JTextField("2", 7);
                numFullGroupsField.setVisible(true);
                numFullGroupsField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
                gridbag.setConstraints(numFullGroupsField, constraints);
                this.add(numFullGroupsField);

                numGroupsLabel = new JLabel("Number of reduced model groups: ");
                numGroupsLabel.setVisible(true);
                buildConstraints(constraints, 0, 2, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numGroupsLabel, constraints);
                this.add(numGroupsLabel);

                numReducedGroupsField = new JTextField("2", 7);
                numReducedGroupsField.setVisible(true);
                numReducedGroupsField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 2, 1, 1, 30, 0);
                gridbag.setConstraints(numReducedGroupsField, constraints);
                this.add(numReducedGroupsField);         

                numPermsLabel = new JLabel("Number of permutations: ");
                numPermsLabel.setVisible(true);
                buildConstraints(constraints, 0, 3, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numPermsLabel, constraints);
                this.add(numPermsLabel);

                numPermsField = new JTextField("100", 7);
                numPermsField.setVisible(true);
                numPermsField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 3, 1, 1, 30, 0);
                gridbag.setConstraints(numPermsField, constraints);
                this.add(numPermsField);  
            }           
            
            public void setVisible(boolean visible) {
                setLocation((MultiClassPanel.this.getWidth() - getSize().width)/2, (MultiClassPanel.this.getHeight() - getSize().height)/2);
                super.setVisible(visible);               
            }
            
            public boolean isOkPressed() {
                return okPressed;
            }            
        }
        

        /**
    	 * Saves the assignments to file.
    	 * 
    	 * Comments include title, user, save date
    	 * Design information includes factor a and b labels and the level names for each factor
    	 * A header row is followed by sample index, sample name (primary, field index = 0),
    	 * them factor A assignment (text label) then factor B assignment (text label)
    	 */
    	private void saveAssignments() {
    		
    		File file;		
    		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());	
    		
    		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    			file = fileChooser.getSelectedFile();			
    			try {
    				PrintWriter pw = new PrintWriter(new FileWriter(file));
    				
    				//comment row
    				Date currDate = new Date(System.currentTimeMillis());			
    				String dateString = currDate.toString();;
    				String userName = System.getProperty("user.name");
    				
    				pw.println("# Assignment File");
    				pw.println("# User: "+userName+" Save Date: "+dateString);
    				pw.println("#");
    				
    				//save group names..?
    				
    				pw.print("Module:\t");
    				pw.println("Global Ancova");
    				pw.print("Design:\t");
    				pw.println(getExperimentalDesign());
    				int groupMax;
    				if (getExperimentalDesign()!=4)
    					groupMax=this.numFullGroups;
    				else
    					groupMax = Math.max(this.numFullGroups,this.numRedGroups);
    				for (int i=0; i<groupMax; i++){
        				pw.print("Group "+(i+1)+" Label:\t");
    					pw.println("Group "+(i+1));
    				}
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");

    				
    				if (getExperimentalDesign()<4){
    					int[] groupAssgn=getGroupAssignments();
        				for(int sample = 0; sample < exptNames.size(); sample++) {
        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
        					pw.print(exptNames.get(sample)+"\t");
        					if (groupAssgn[sample]!=0)
        						pw.println("Group "+(groupAssgn[sample]));
        					else
        						pw.println("Exclude");
        					
        				}
        			}else{
        					
        				for(int sample = 0; sample < exptNames.size(); sample++) {
        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
        					pw.print(exptNames.get(sample)+"\t");
        					int a = 0;
        		        	int b = 0;
            				if (getExperimentalDesign()==4){
	        		            for (int j = 0; j < mPanel.fullModelESP.assignmentRBs.length; j++) {
	        		                if (mPanel.fullModelESP.assignmentRBs[j][sample].isSelected()) {
	        		                    a = j+1;
	        		                    break;
	        		                }
	        		            }
	        		            for (int j = 0; j < mPanel.reducedModelESP.assignmentRBs.length; j++) {
	        		                if (mPanel.reducedModelESP.assignmentRBs[j][sample].isSelected()) {
	        		                    b = j+1;
	        		                    break;
	        		                }
	        		            }
        					}else{
	        		            for (int j = 0; j < mPanel.ConditionESP.assignmentRBs.length; j++) {
	        		                if (mPanel.ConditionESP.assignmentRBs[j][sample].isSelected()) {
	        		                    b = j+1;
	        		                    break;
	        		                }
	        		            }
	        		            for (int j = 0; j < mPanel.TimePointESP.assignmentRBs.length; j++) {
	        		                if (mPanel.TimePointESP.assignmentRBs[j][sample].isSelected()) {
	        		                    a = j+1;
	        		                    break;
	        		                }
	        		            }        						
        					}
    						pw.println((a==0?"Exclude":("Group "+(a)))+"\t"+(b==0?"Exclude":"Group "+(b)));
        				}
    				}
    				pw.flush();
    				pw.close();			
    			} catch (FileNotFoundException fnfe) {
    				fnfe.printStackTrace();
    			} catch (IOException ioe) {
    				ioe.printStackTrace();
    			}
    		}
    	}
    	
        class ExperimentsSelectionPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			int numPanels = 0;
            JLabel[] expLabels;
            JRadioButton[][] assignmentRBs;
            JRadioButton[] notInTimeGroupRadioButtons;
            ExperimentsSelectionPanel(Vector<String> exptNames, int numGroups, String title, boolean firstPanel) {
                this.setBorder(new TitledBorder(new EtchedBorder(), title+" Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

                expLabels = new JLabel[exptNames.size()];
                assignmentRBs = new JRadioButton[numGroups][exptNames.size()];
                numPanels = exptNames.size()/512 + 1;
                
                notInTimeGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseTime[] = new ButtonGroup[exptNames.size()];
                ButtonGroup chooseCondition[] = new ButtonGroup[exptNames.size()];
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);

                JPanel [] panels = new JPanel[numPanels];
                
                int currPanel = 0;
                for(int i = 0; i < panels.length; i++) {
                    panels[i] = new JPanel(gridbag);
                }
                
                for (int i = 0; i < exptNames.size(); i++) {
                    String s1 = (String)(exptNames.get(i));
                    expLabels[i] = new JLabel(s1);
                    chooseTime[i] = new ButtonGroup();
                    chooseCondition[i] = new ButtonGroup();
	                for (int j = 0; j < numGroups; j++) {
	                    assignmentRBs[j][i] = new JRadioButton("Group " + (j+1) + "     ", true);
	                    chooseTime[i].add(assignmentRBs[j][i]);
	                }                    
                    
                    //set current panel
                    currPanel = i / 512;
                    
                    notInTimeGroupRadioButtons[i] = new JRadioButton("Unassigned", false);
                    chooseTime[i].add(notInTimeGroupRadioButtons[i]);
                    int twoCondRoom = 0;
                    
                    for (int j = 0; j < numGroups; j++) {
                        buildConstraints(constraints, j+twoCondRoom, i%512, 1, 1, 100, 100);
                        gridbag.setConstraints(assignmentRBs[j][i], constraints);
                        panels[currPanel].add(assignmentRBs[j][i]);
                    }
                    
                    buildConstraints(constraints, (numGroups + 1+twoCondRoom), i%512, 1, 1, 100, 100);
                    gridbag.setConstraints(notInTimeGroupRadioButtons[i], constraints);
                    
                    
                    panels[currPanel].add(notInTimeGroupRadioButtons[i]);
                }
                
                int maxLabelWidth = 0;
                
                for (int i = 0; i < expLabels.length; i++) {
                    if (expLabels[i].getPreferredSize().getWidth() > maxLabelWidth) {
                        maxLabelWidth = (int)Math.ceil(expLabels[i].getPreferredSize().getWidth());
                    }
                }
                
                JPanel bigPanel = new JPanel(new GridBagLayout());
                
                for(int i = 0; i < numPanels; i++) {
                    bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
                }
                
                JScrollPane scroll = new JScrollPane(bigPanel);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);                
                             
                JPanel [] exptNameHeaderPanels = new JPanel[this.numPanels];
                GridBagLayout exptHeaderGridbag = new GridBagLayout();
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    exptNameHeaderPanels[i] = new JPanel();
                    exptNameHeaderPanels[i].setSize(50, panels[i].getPreferredSize().height);
                    exptNameHeaderPanels[i].setPreferredSize(new Dimension(maxLabelWidth + 10, panels[i].getPreferredSize().height));
                    exptNameHeaderPanels[i].setLayout(exptHeaderGridbag);
                }
                
                //need to add to additional panels if number of samples exceeds 512
                for (int i = 0; i < expLabels.length; i++) {
                    currPanel = i / 512;
                    buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                    exptNameHeaderPanels[currPanel].add(expLabels[i]);
                }

                JPanel headerPanel = new JPanel(new GridBagLayout());
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    headerPanel.add(exptNameHeaderPanels[i], new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0)); 
                }
                
                scroll.setRowHeaderView(headerPanel);
                
                buildConstraints(constraints, 0, 0, 1, 1, 100, 90,GridBagConstraints.CENTER,GridBagConstraints.BOTH);
                gridbag2.setConstraints(scroll, constraints);
                this.add(scroll);              
            }
            /**
             *  resets all group assignments
             */
            protected void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                	notInTimeGroupRadioButtons[i].setSelected(true);
                }
            }        	
        }

    	/**
    	 * Loads file based assignments
    	 */
    	private void loadAssignments() {
    		/**
    		 * consider the following verifcations and policies
    		 *-number of loaded samples and rows in the assigment file should match, if not warning and quit
    		 *-each loaded file name should match a corresponding name in the assignment file, 1:1
    		 *		-if names don't match, throw warning and inform that assignments are based on loaded order
    		 *		 rather than a sample name
    		 *-the number of levels of factor A and factor B specified previously when defining the design
    		 *should match the number of levels in the assignment file, if not warning and quit
    		 *-if the level names match the level names entered then the level names will be used to make assignments
    		 *if not, then there will be a warning and the level index will be used.
    		 *-make sure that each level label pairs to a particular level index, this is a format 
    		 *-Note that all design labels in the assignment file will override existing labels
    		 *this means updating the data structures in this class, and updating AlgorithmData to set appropriate fields
    		 ***AlgorithmData modification requires a fixed vocab. for parameter names to be changed
    		 *these fields are (factorAName, factorBName, factorANames (level names) and factorANames (level names)
    		 *Wow, that was easy :)
    		 */
    		
    		File file;		
    		JFileChooser fileChooser = new JFileChooser("./data");
    		
    		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
    		
    			file = fileChooser.getSelectedFile();
    			
        		try {						
        			//first grab the data and close the file
        			BufferedReader br = new BufferedReader(new FileReader(file));
        			Vector<String> data = new Vector<String>();
        			String line;
        			while( (line = br.readLine()) != null)
        				data.add(line.trim());
        			
        			br.close();
        				
        			//build structures to capture the data for assingment information and for *validation
        			
        			//factor names
        			Vector<String> groupNames = new Vector<String>();
        			Vector<Integer> sampleIndices = new Vector<Integer>();
        			Vector<String> sampleNames = new Vector<String>();
        			Vector<String> groupAssignments = new Vector<String>();		
        			Vector<String> group2Assignments = new Vector<String>();		
        			
        			//parse the data in to these structures
        			String [] lineArray;
        			int design=0;
        			//String status = "OK";
        			for(int row = 0; row < data.size(); row++) {
        				line = (String)(data.get(row));

        				//if not a comment line, and not the header line
        				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
        					
        					lineArray = line.split("\t");
        					
        					//check what module saved the file
        					if(lineArray[0].startsWith("Module:")) {
        						if (!lineArray[1].equals("Global Ancova")){
        							Object[] optionst = { "Continue", "Cancel" };
        							if (JOptionPane.showOptionDialog(null, 
        		    						"The saved file was saved using a different module, "+lineArray[1]+". \n Would you like MeV to try to load it anyway?", 
        		    						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
        		    						optionst, optionst[0])==0)
        								continue;
        							return;
        						}
        						continue;
        					}
        					
        					//pick up group names
        					if(lineArray[0].startsWith("Group ") && lineArray[0].endsWith("Label:")) {
        						groupNames.add(lineArray[1]);
        						continue;
        					}
        					if(lineArray[0].startsWith("Design")) {
        						try {
        							design=Integer.parseInt(lineArray[1]);
	        					} catch ( NumberFormatException nfe) {
	        						//if not parsable continue
	        						continue;
	        					}
        						continue;
        					}        						

        					//non-comment line, non-header line and not a group label line
        					
        					try {
        						Integer.parseInt(lineArray[0]);
        					} catch ( NumberFormatException nfe) {
        						//if not parsable continue
        						continue;
        					}
        					
        					sampleIndices.add(new Integer(lineArray[0]));
        					sampleNames.add(lineArray[1]);
        					groupAssignments.add(lineArray[2]);	
        					if (design==4||design==5)
        						group2Assignments.add(lineArray[3]);
        				}				
        			}
        			
        			//we have the data parsed, now validate, assign current data


        			if( exptNames.size() != sampleNames.size()) {
        				System.out.println(exptNames.size()+"  "+sampleNames.size());
        				//status = "number-of-samples-mismatch";
        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
        				//warn and prompt to continue but omit assignments for those not represented				

        				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
        						                                   "does not match the number of samples loaded in MeV ("+exptNames.size()+").<br>" +
        						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
        				
        				return;
        			}
        			Vector<String> currSampleVector = new Vector<String>();
        			for(int i = 0; i < exptNames.size(); i++)
        				currSampleVector.add(exptNames.get(i));
        			
        			int fileSampleIndex = 0;
        			int groupIndex = 0;
        			String groupName;
        			String condName;
        			int condIndex = 0;
        			
        			for(int sample = 0; sample < exptNames.size(); sample++) {
        				boolean doIndex = false;
        				for (int i=0;i<exptNames.size(); i++){
        					if (i==sample)
        						continue;
        					if (exptNames.get(i).equals(exptNames.get(sample))){
        						doIndex=true;
        					}
        				}
        				fileSampleIndex = sampleNames.indexOf(exptNames.get(sample));
        				if (fileSampleIndex==-1){
        					doIndex=true;
        				}
        				if (doIndex){
        					setStateBasedOnIndex(groupAssignments, groupNames, design, group2Assignments);
        					break;
        				}
        				
        				groupName = (String)(groupAssignments.get(fileSampleIndex));
        				groupIndex = groupNames.indexOf(groupName);
    					if (design==4||design==5){
	        				condName = (String)(group2Assignments.get(fileSampleIndex));
	        				condIndex = groupNames.indexOf(condName);
        				}
        				
        				//set state
        				try{
                    		mPanel.reducedModelESP.assignmentRBs[condIndex][sample].setSelected(true);
                    		mPanel.fullModelESP.assignmentRBs[groupIndex][sample].setSelected(true);
        				}catch (Exception e){
                    		mPanel.fullModelESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                    		mPanel.reducedModelESP.notInTimeGroupRadioButtons[sample].setSelected(true);
        				}
        			}
        			
        			repaint();			
        			//need to clear assignments, clear assignment booleans in sample list and re-init
        			//maybe a specialized inti for the sample list panel.
        		} catch (Exception e) {
        			e.printStackTrace();
        			JOptionPane.showMessageDialog(this, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
        		}
        	}
    	}
    	
    	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames, int cond,Vector<String>condAssignments){
    		Object[] optionst = { "Continue", "Cancel" };
    		if (JOptionPane.showOptionDialog(null, 
					"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
					"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
					optionst, optionst[0])==1)
				return;

			String condName;
			int condIndex = 0;
    		for(int sample = 0; sample < exptNames.size(); sample++) {
    			if (cond==2){
    				condName = (String)(condAssignments.get(sample));
    				condIndex = groupNames.indexOf(condName);
				}
    			try{
            		mPanel.fullModelESP.assignmentRBs[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
            		mPanel.reducedModelESP.assignmentRBs[condIndex][sample].setSelected(true);
    			}catch(Exception e){
            		mPanel.fullModelESP.notInTimeGroupRadioButtons[sample].setSelected(true);
            		mPanel.reducedModelESP.notInTimeGroupRadioButtons[sample].setSelected(true);
    			}
    		}
    	}
        private JPanel createSaveLoadPanel(){

            JPanel panel2 = new JPanel();
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            panel2.setBackground(Color.white);
            JButton saveButton = new JButton("  Save settings  ");
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            JButton loadButton = new JButton("  Load settings  ");
            loadButton.setFocusPainted(false);
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            JButton resetButton = new JButton("  Reset  ");
            resetButton.setFocusPainted(false);
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            
            
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                		mPanel.fullModelESP.notInTimeGroupRadioButtons[i].setSelected(true);
                		mPanel.reducedModelESP.notInTimeGroupRadioButtons[i].setSelected(true);
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	saveAssignments();
                }
            });
            
            
            //NEED TO REWORK THIS FOR MULTICLASS
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	loadAssignments();
                	
                }
            });           
            
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(5,5,5,5);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            gridbag3.setConstraints(resetButton, constraints);
            panel2.add(resetButton);
            constraints.insets = new Insets(0,0,0,0);
            buildConstraints(constraints, 0, 2, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
        	return panel2;
        }
        protected void reset(){
        	if (ngPanel.okPressed){
        		sampleSelectionPanel.reset();
        	}
        }
    }
    
  
    public class RadioButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ae) {
        		mPanel.ngPanel.numFullGroupsField.setVisible(false);
        		mPanel.ngPanel.numGroupsLabel.setVisible(false);
        		mPanel.ngPanel.factorPanel.setVisible(true);
        }    	
    }
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getSelectionDesign()==GLOBANCInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
            	//check parameters
            	if (!isParamSufficient()){
            		JOptionPane.showMessageDialog(null, "Please select at least 1 sample for each group.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                okPressed = true;
            	dispose();
            } else if (command.equals("reset-command")) {
                mPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(GLOBANCInitBox.this, "Global Ancova- Initialization Dialog");
                okPressed = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.setVisible(true);
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
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
    	boolean[] full = new boolean[this.mPanel.numFullGroups];
    	boolean[] reduced = new boolean[this.mPanel.numRedGroups];
    	for (int i = 0; i < exptNames.size(); i++) {
        	if (mPanel.fullModelESP.notInTimeGroupRadioButtons[i].isSelected()||mPanel.reducedModelESP.notInTimeGroupRadioButtons[i].isSelected()){
        		continue;
        	}
            for (int j = 0; j < mPanel.fullModelESP.assignmentRBs.length; j++) {
                if (mPanel.fullModelESP.assignmentRBs[j][i].isSelected()) {
                	full[j]=true;
                }
            }
            for (int j = 0; j < mPanel.reducedModelESP.assignmentRBs.length; j++) {
                if (mPanel.reducedModelESP.assignmentRBs[j][i].isSelected()) {
                    reduced[j]=true;
                }
            }
        }
    	for (int i=0; i<full.length; i++){
    		if (!full[i])
    			return false;
    	}
    	for (int i=0; i<reduced.length; i++){
    		if (!reduced[i])
    			return false;
    	}
		return true;
//		for (int i=0; i<inc.length; i++){
//			if (inc[i] < 1){
//				JOptionPane.showMessageDialog(null, "Please select at least 1 sample for each group combination.", "Error", JOptionPane.WARNING_MESSAGE);
//	    		return false;
//			}
//		}
    }


	public int[] getGroupAssignments() {
    	if (getSelectionDesign()==GLOBANCInitBox.CLUSTER_SELECTION)
    		return getClusterSelectorFactorAssignments();
    	int[]factorGroupAssignments = new int[exptNames.size()];

        for (int i = 0; i < exptNames.size(); i++) {
        	if (mPanel.fullModelESP.notInTimeGroupRadioButtons[i].isSelected()||mPanel.reducedModelESP.notInTimeGroupRadioButtons[i].isSelected()){
        		factorGroupAssignments[i]=0;
        		continue;
        	}
        	int a = 0;
        	int b = 0;
            for (int j = 0; j < mPanel.fullModelESP.assignmentRBs.length; j++) {
                if (mPanel.fullModelESP.assignmentRBs[j][i].isSelected()) {
                    a = j;
                    break;
                }
            }
            for (int j = 0; j < mPanel.reducedModelESP.assignmentRBs.length; j++) {
                if (mPanel.reducedModelESP.assignmentRBs[j][i].isSelected()) {
                    b = j;
                    break;
                }
            }
            factorGroupAssignments[i]=a*this.getNumRedGroups()+b+1;
        }
    	return factorGroupAssignments;
    }
    private int[] getClusterSelectorFactorAssignments() {
    	boolean doubleAssigned;
    	int[][]groupAssignments = new int[2][exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[getNumRedGroups()];
    	for (int i=0; i<getNumRedGroups(); i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.factorACS.getGroupSamples("Full Model "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[0][i] = 0;
    		for (int j = 0;j<getNumRedGroups();j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[0][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.factorBCS.getGroupSamples("Reduced Model "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[1][i] = 0;
    		for (int j = 0;j<2;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[1][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	int[] groupAssignments2 = new int[exptNames.size()];
    	for (int i=0; i<groupAssignments2.length; i++){
    		if (groupAssignments[0][i]==0||groupAssignments[1][i]==0)
    			groupAssignments2[i]=0;
    		else
    			groupAssignments2[i] = (groupAssignments[0][i]-1)*this.getNumRedGroups()+(groupAssignments[1][i]-1)+1;
    	}
    	return groupAssignments2;
	}


	public int[][] getGroupMatrix(){
    	int[] timeAssignments;
		timeAssignments = getGroupAssignments();
    	int[] numEachTime = new int[getNumGroups()];
    	for (int i=0; i< timeAssignments.length; i++){
    		if (timeAssignments[i]!=0)
    			numEachTime[timeAssignments[i]-1]++;
    	}
    	int[][]timeMatrix=new int[getNumGroups()][];
    	for (int i=0; i<getNumGroups(); i++){
    		timeMatrix[i]=new int[numEachTime[i]];
    	}
    	int[]nextEntry=new int[getNumGroups()];
    	for (int i=0; i< timeAssignments.length; i++){
    		if (timeAssignments[i]!=0){
	    		timeMatrix[timeAssignments[i]-1][nextEntry[timeAssignments[i]-1]] = i;
	    		nextEntry[timeAssignments[i]-1]++;
    		}
    	}
    	return timeMatrix;
    }
    /**
     * 
     * @return
     * 
     */
    public int getExperimentalDesign() {
    	int design = 4;
    	return design;
    }
    
    public int getSelectionDesign() {
        int design = -1;
        if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = GLOBANCInitBox.BUTTON_SELECTION;
        } else {
        	design = GLOBANCInitBox.CLUSTER_SELECTION;
        }
        return design;
    }

    public int getNumGroups() {
    	if (getExperimentalDesign()==4)
    		return getNumFullGroups()*getNumRedGroups();
        return mPanel.numFullGroups;
    }
    public int getNumFullGroups() {
        return mPanel.numFullGroups;
    }
    public int getNumRedGroups() {
        return mPanel.numRedGroups;
    }
    public String getFactorAName() {
        return mPanel.factorAName;
    }
    public String getFactorBName() {
        return mPanel.factorBName;
    }
    public float getAlpha() {
    	return 0f;//Float.parseFloat(mPanel.ngPanel.alphaField.getText());
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 24; i++) {
            dummyVect.add("Expt " + i);
        }
        String[] anf = {"asd","dfgdfg","GENE_SYMBOL", "PROBE_ID"};
        GLOBANCInitBox oBox = new GLOBANCInitBox(dummyFrame, true, dummyVect, anf, null);
        oBox.setVisible(true);
//        int[] k = oBox.getGroupAssignments();
//        for (int i=0; i<k.length; i++){
//        	System.out.print(k[i]+"\t");
//        }
        System.exit(0);
    }


	public String[] getGeneSetFilePath() {
		return this.genesetFilePath;
	}


	public int getGeneSetOrigin() {
		return this.geneSetSelectionBox.getSelectedIndex();
	}


	public String getSelectedAnnotation() {
		return (String)this.gsfPanel.geneIdentifierBox.getSelectedItem();
	}


	public int getNumPerms() {
		return mPanel.numPerms;
	}
}
