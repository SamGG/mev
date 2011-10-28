/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: OneWayANOVAInitBox.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.attract;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.ChipAnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class ATTRACTInitBox extends AlgorithmDialog {

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
    JTabbedPane selectionPanel;
    HCLoptionPanel hclOpsPanel;
    ClusterRepository repository;
    JButton step2Button = new JButton("Continue...");
	private String initialChipType;
    
    /** Creates new ATTRACTInitBox */
    public ATTRACTInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, ClusterRepository repository, String initialChipType) {
        super(parentFrame, "ATTRACT Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = repository;
        this.initialChipType = initialChipType;
        
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
        showGroupNameTextFields();
        
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
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }   
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    

    class HCLoptionPanel extends JPanel {
    	 
		private static final long serialVersionUID = 1L;
		private JCheckBox hclCluster;  
        private JRadioButton sigOnly, allClusters;
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        /** Creates a new instance of HCLSigOnlyPanel */
        public HCLoptionPanel() {
            super();
            this.setBackground(Color.white);
            Font font = new Font("Dialog", Font.BOLD, 12);
            this.setLayout(gridbag);
            this.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Hierarchical Clustering", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            hclCluster = new JCheckBox("Construct Hierarchical Trees for :            ");
            hclCluster.setFocusPainted(false);
            hclCluster.setBackground(Color.white);
            hclCluster.setForeground(UIManager.getColor("Label.foreground"));
            
            sigOnly = new JRadioButton("Significant genes only", true);
            sigOnly.setBackground(Color.white);
            sigOnly.setForeground(UIManager.getColor("Label.foreground"));     
            
            allClusters = new JRadioButton("All clusters", false);
            allClusters.setBackground(Color.white);
            allClusters.setForeground(UIManager.getColor("Label.foreground"));        

            sigOnly.setEnabled(false);
            allClusters.setEnabled(false);
            
            ButtonGroup allOrSig = new ButtonGroup();
            allOrSig.add(sigOnly);
            allOrSig.add(allClusters);
            
            hclCluster.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        sigOnly.setEnabled(false);
                        allClusters.setEnabled(false);
                    } else {
                        sigOnly.setEnabled(true);
                        allClusters.setEnabled(true);                    
                    }
                }
            });        

            buildConstraints(constraints, 0, 0, 1, 1, 25, 10, GridBagConstraints.WEST);
            gridbag.setConstraints(hclCluster, constraints);
            add(hclCluster);
            buildConstraints(constraints, 0, 1, 1, 1, 25, 10);
            gridbag.setConstraints(sigOnly, constraints);
            add(sigOnly);
            buildConstraints(constraints, 0, 2, 1, 1, 25, 10);
            gridbag.setConstraints(allClusters, constraints);
            add(allClusters);
            
            JPanel dummyPanel = new JPanel();

            buildConstraints(constraints, 0, 3, 1, 1, 25, 100);
            gridbag.setConstraints(dummyPanel, constraints);
            dummyPanel.setBackground(Color.white);
            add(dummyPanel);
        }
        
        public HCLoptionPanel(Color background){
            this();
            setBackground(background);
        }
        
        public boolean isHCLSelected(){
            return hclCluster.isSelected();
        }  
        
        public boolean drawSigTreesOnly() {
            return sigOnly.isSelected();
        }
        
        public void setHCLSelected(boolean value){
                hclCluster.setSelected(value);
        }    
        
    }
    class MultiClassPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DesignPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        ExperimentsSelectionPanel sampleSelectionPanel;
        JTabbedPane tabbedmulg;
        ClusterSelector groupsCS;
        JLabel infoLabel;
        JLabel infoLabel2;
        int numGroups=-1;
        float alpha;
		public String chipName;
        //Vector exptNames;
        
        public MultiClassPanel(/*Vector exptNames*/) {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            //this.exptNames = exptNames;
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
            
            
            JPanel topPanel =  new JPanel();
            topPanel.setBackground(Color.white);
            topPanel.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 2, 75, 100);
            gridbag.setConstraints(ngPanel, constraints);
            topPanel.add(ngPanel);

            hclOpsPanel = new HCLoptionPanel();
            hclOpsPanel.setBorder(null);
            buildConstraints(constraints, 1, 0, 1, 1, 25, 100);
            gridbag.setConstraints(hclOpsPanel, constraints);
            topPanel.add(hclOpsPanel);

            
            buildConstraints(constraints, 1, 2, 1, 1, 0, 10);
            gridbag.setConstraints(step2Button, constraints);
            topPanel.add(step2Button);

            topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "ATTRACT Parameters",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 0, 0,1,1,100,10);
            gridbag.setConstraints(topPanel, constraints);
            this.add(topPanel);
            
            infoLabel = new JLabel("Sample Group Assignment");
            infoLabel.setMaximumSize(new Dimension(50,50));
            Font font = infoLabel.getFont();
            infoLabel.setFont(font.deriveFont(20.0f));
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5,GridBagConstraints.CENTER,GridBagConstraints.NONE);
            gridbag.setConstraints(infoLabel, constraints);
            
            this.add(infoLabel, constraints);
            infoLabel2 = new JLabel("Please select the type of ATTRACT analysis to be run, then click 'Continue'.");
            buildConstraints(constraints, 0, 2, 1, 1, 100, 5,GridBagConstraints.CENTER);
            gridbag.setConstraints(infoLabel2, constraints);
            
            this.add(infoLabel2, constraints);
            
            
            buildConstraints(constraints, 0, 3, 1, 1, 100, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
            
        }
        private void goBack(){
    		infoLabel.setVisible(true);
            infoLabel2.setVisible(true);
            ngPanel.numGroupsField.setEnabled(true);
            ngPanel.alphaField.setEnabled(true);
            ngPanel.chipNameBox.setEnabled(true);
//            ngPanel.oneClass.setEnabled(true);
            ngPanel.twoClass.setEnabled(true);
            ngPanel.multiClass.setEnabled(true);
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
            	alpha = Float.parseFloat(ngPanel.alphaField.getText());
            	chipName = ngPanel.chipNameBox.getSelectedItem().toString();
            	numGroups = 0;
            	if (getExperimentalDesign()==2)
            		numGroups = 2;
            	if (getExperimentalDesign()==3)
            		numGroups = Integer.parseInt(ngPanel.numGroupsField.getText());
            
            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Error reading parameter input.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (alpha>1||alpha<0){//checks alpha value
            	JOptionPane.showMessageDialog(null, "Please enter an alpha value between 0 and 1.", "Error", JOptionPane.ERROR_MESSAGE);
            	return;
            }
            if (numGroups<2||numGroups>8){ 
            	JOptionPane.showMessageDialog(null, "The number of groups must be greater than 1 and less than 9.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JPanel selectionPanel = new JPanel();
            GridBagLayout gbg = new GridBagLayout();
            selectionPanel.setLayout(gbg);
            GridBagConstraints cnstr = new GridBagConstraints();

            buildConstraints(cnstr, 0, 0, 1, 1, 1, 1);
            cnstr.fill = GridBagConstraints.BOTH;
            JPanel clusterSelectorPanel = new JPanel();
            clusterSelectorPanel.setLayout(new GridBagLayout());
            JLabel clusterInstructions = new JLabel("Use the drop-down menus to assign clusters of samples to their corresponding groups.");
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth=2;
            clusterSelectorPanel.add(clusterInstructions, c);
    		sampleSelectionPanel = new ExperimentsSelectionPanel(exptNames, numGroups, ngPanel.getExperimentDesign(), "Group", true);
    		selectionPanel.add(sampleSelectionPanel, cnstr);
    		cnstr.gridy++;
    		cnstr.weighty = 0;
    		selectionPanel.add(createSaveLoadPanel(), cnstr);
    		
    		if(getExperimentalDesign()==5){//ever occur?
    			groupsCS= new ClusterSelector(repository, numGroups, "Timepoint");
            	groupsCS.setClusterType("Timepoint");
    		}else{
    			groupsCS= new ClusterSelector(repository, numGroups, "Class");
    			groupsCS.setClusterType("Class");
    		}
            	
            buildConstraints(c, 0, 1, 1, 1, 1, 1);
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 1;
            clusterSelectorPanel.add(groupsCS, c);
        	

            MultiClassPanel.this.remove(dummyPanel);
            tabbedmulg = new JTabbedPane();
            
            tabbedmulg.add("Button Selection", selectionPanel);
            tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
            tabbedmulg.setSelectedIndex(1);
            if (repository==null||repository.isEmpty())
            	tabbedmulg.setSelectedIndex(0);
            buildConstraints(constraints, 0, 1, 2, 1, 0, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tabbedmulg, constraints);
            MultiClassPanel.this.add(tabbedmulg);
            MultiClassPanel.this.validate();
            enableOK();
            ngPanel.numGroupsField.setEnabled(false);
            ngPanel.alphaField.setEnabled(false);
            ngPanel.chipNameBox.setEnabled(false);
            ngPanel.twoClass.setEnabled(false);
            ngPanel.multiClass.setEnabled(false);
            step2Button.setText("<<< Go Back");
            infoLabel.setVisible(false);
            infoLabel2.setVisible(false);
            step2 = true;
        }
        class DesignPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			JTextField numGroupsField, alphaField;
			JComboBox chipNameBox;
			JLabel numGroupsLabel;
            boolean okPressed = false;
            JRadioButton twoClass, multiClass;
			private JLabel[] groupNameLabel;
			private JTextField[] groupNameField;
            public DesignPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                this.setLayout(gridbag);
                this.setMinimumSize(new Dimension(300,100));
                
                JLabel dataTypeLabel = new JLabel("Experimental Design:   ");
                buildConstraints(constraints, 0, 0, 1, 1, 30, 100,GridBagConstraints.EAST);
                gridbag.setConstraints(dataTypeLabel, constraints);
                this.add(dataTypeLabel);
                
                twoClass=new JRadioButton("Two Class", true);
                multiClass=new JRadioButton("Multi-Class", false);
                twoClass.setBackground(Color.white);
                twoClass.setBorder(null);
                multiClass.setBackground(Color.white);
                multiClass.setBorder(null);
                ButtonGroup dataType = new ButtonGroup();
                dataType.add(twoClass);
                dataType.add(multiClass);
                twoClass.addActionListener(new RadioButtonListener());
                multiClass.addActionListener(new RadioButtonListener());
                buildConstraints(constraints, 1, 0, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(twoClass, constraints);
                this.add(twoClass);
                buildConstraints(constraints, 1, 1, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(multiClass, constraints);
                this.add(multiClass);
                
                numGroupsLabel = new JLabel("Number of groups: ");
                numGroupsLabel.setVisible(false);
                buildConstraints(constraints, 0, 5, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numGroupsLabel, constraints);
                this.add(numGroupsLabel);
                
                numGroupsField = new JTextField("4", 7);
                numGroupsField.setVisible(false);
                numGroupsField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 5, 1, 1, 30, 0);
                gridbag.setConstraints(numGroupsField, constraints);
                this.add(numGroupsField);
                numGroupsField.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
//						showGroupNameTextFields();
					}

					public void keyReleased(KeyEvent e) {
						showGroupNameTextFields();
						
					}

					public void keyTyped(KeyEvent e) {
//						showGroupNameTextFields();
						
					}
                });

                constraints.ipady = 0;
                
                JLabel alphaLabel = new JLabel("Limma significance Level: Alpha = ");
                buildConstraints(constraints, 0, 6, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(alphaLabel, constraints);
                this.add(alphaLabel);
                
                alphaField = new JTextField(".2", 7);
                alphaField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 6, 1, 1, 30, 0);
                gridbag.setConstraints(alphaField, constraints);
                this.add(alphaField);

                JLabel chipNameLabel = new JLabel("Chip Name: ");
                buildConstraints(constraints, 0, 7, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(chipNameLabel, constraints);
                this.add(chipNameLabel);
                

				ArrayList<String> bioCAnnotations = new ArrayList<String>();
                try {
    				String urlString = "ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support_devel/R2.11/win/attract/annotationSupported.txt";
    				String fullURL = urlString;
    				URL url = new URL(fullURL); // Interpret, connect to URL

    				URLConnection url_conn = url.openConnection();
    				url_conn.setDoInput(true);
    				url_conn.setUseCaches(true);

    				BufferedReader inp = new BufferedReader( // Setup buffered input stream
    						new InputStreamReader(url_conn.getInputStream()));
    				String s = inp.readLine();
    				while (s != null) {						
    					bioCAnnotations.add(s);
    					s = inp.readLine();
    				}
    			} catch (Exception e){
    				System.out.println("Error reading supported Bioconductor annotations");
    			}
    			String[] bioCAnnotationsArray = new String[bioCAnnotations.size()];
    			int defaultIndex = -1;
    			for (int i=0; i<bioCAnnotationsArray.length; i++){
    				bioCAnnotationsArray[i]=bioCAnnotations.get(i);
    				if(bioCAnnotationsArray[i].equals(initialChipType)||bioCAnnotationsArray[i].equals(initialChipType+".db")){
    					defaultIndex = i;
    				}
    			}
                chipNameBox = new JComboBox(bioCAnnotationsArray);
                if (defaultIndex==-1||initialChipType.equals(ChipAnnotationFieldConstants.NOT_AVAILABLE))
                	chipNameBox.setSelectedItem("hgu133plus2.db");
                else
                	chipNameBox.setSelectedIndex(defaultIndex);
                chipNameBox.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 7, 1, 1, 30, 0);
                gridbag.setConstraints(chipNameBox, constraints);
                this.add(chipNameBox);

                groupNameLabel = new JLabel[8];
                groupNameField = new JTextField[8];
                for (int i=0; i<8; i++){
                    groupNameLabel[i] = new JLabel("Group "+(i+1)+" Name: ");
                    buildConstraints(constraints, 0, 8+i, 1, 1, 30, 100);
                    constraints.anchor = GridBagConstraints.EAST;
                    gridbag.setConstraints(groupNameLabel[i], constraints);
                    this.add(groupNameLabel[i]);
                    groupNameLabel[i].setVisible(false);
                    
                    groupNameField[i] = new JTextField("Group "+(i+1), 7);
                    groupNameField[i].setMinimumSize(new Dimension(50,20));
                    constraints.anchor = GridBagConstraints.WEST;
                    buildConstraints(constraints, 1, 8+i, 1, 1, 30, 0);
                    gridbag.setConstraints(groupNameField[i], constraints);
                    this.add(groupNameField[i]);
                    groupNameField[i].setVisible(false);
                }

            }
            
            public int getExperimentDesign(){
            	if (twoClass.isSelected())
            		return 2;
            	if (multiClass.isSelected())
            		return 3;
            	return 0;
            }
            
            public void setVisible(boolean visible) {
                //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setLocation((MultiClassPanel.this.getWidth() - getSize().width)/2, (MultiClassPanel.this.getHeight() - getSize().height)/2);
                
                super.setVisible(visible);
                
                if (visible) {
                    //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
                }
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
    				pw.println("ATTRACT");
    				pw.print("Design:\t");
    				pw.println(ngPanel.getExperimentDesign());
    				int groupMax;
					groupMax=this.numGroups;
    				for (int i=0; i<groupMax; i++){
        				pw.print("Group "+(i+1)+" Label:\t");
    					pw.println("Group "+(i+1));
    				}
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");

    				
					int[] groupAssgn=getGroupAssignments();
    				for(int sample = 0; sample < exptNames.size(); sample++) {
    					pw.print(String.valueOf(sample+1)+"\t"); //sample index
    					pw.print(exptNames.get(sample)+"\t");
    					if (groupAssgn[sample]!=0)
    						pw.println("Group "+(groupAssgn[sample]));
    					else
    						pw.println("Exclude");
    					
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
			int design = 0;
            int numPanels = 0;
            JLabel[] expLabels;
            JRadioButton[][] assignmentRBs;
            JRadioButton[] notInTimeGroupRadioButtons;
            ExperimentsSelectionPanel(Vector<String> exptNames, int numGroups, int design, String title, boolean firstPanel) {
            	this.design = design;
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
                    String[] groupNames = getGroupNames();
	                for (int j = 0; j < numGroups; j++) {
	                    assignmentRBs[j][i] = new JRadioButton(groupNames[j] + "     ", true);
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
        			for(int row = 0; row < data.size(); row++) {
        				line = (String)(data.get(row));

        				//if not a comment line, and not the header line
        				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
        					
        					lineArray = line.split("\t");
        					
        					//check what module saved the file
        					if(lineArray[0].startsWith("Module:")) {
        						if (!lineArray[1].equals("ATTRACT")){
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
        				
        				//set state
        				try{
                    		mPanel.sampleSelectionPanel.assignmentRBs[groupIndex][sample].setSelected(true);
                        	
        				}catch (Exception e){
                    		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[sample].setSelected(true);                        	
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

    		for(int sample = 0; sample < exptNames.size(); sample++) {
    			try{
            		mPanel.sampleSelectionPanel.assignmentRBs[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
                	
    			}catch(Exception e){
            		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[sample].setSelected(true);
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
                		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[i].setSelected(true);
                    	
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
//            gridbag2.setConstraints(panel2, constraints);
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
        	if (getExperimentalDesign()==3){
        		mPanel.ngPanel.numGroupsLabel.setText("Number of Groups: ");
        		mPanel.ngPanel.numGroupsField.setVisible(true);
        		mPanel.ngPanel.numGroupsLabel.setVisible(true);
        	} else{
        		mPanel.ngPanel.numGroupsField.setVisible(false);
        		mPanel.ngPanel.numGroupsLabel.setVisible(false);
        	}
        	showGroupNameTextFields();
        }
    	
    }
    private void showGroupNameTextFields(){
    	int numGroups = this.getNumGroups();
    	for (int i=0; i<8; i++){
    		mPanel.ngPanel.groupNameLabel[i].setVisible(false);
    		mPanel.ngPanel.groupNameField[i].setVisible(false);
    	}
    	for (int i=0; i<numGroups; i++){
    		mPanel.ngPanel.groupNameLabel[i].setVisible(true);
    		mPanel.ngPanel.groupNameField[i].setVisible(true);
    	}
    }
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getSelectionDesign()==ATTRACTInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
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
            	HelpWindow.launchBrowser(ATTRACTInitBox.this, "Linear Models for Microarray Data- Initialization Dialog");
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
    	switch (getExperimentalDesign()){
	    	case 2:{
	    		int[] inc = new int[2];
	    		int[] grpAssign = getGroupAssignments();
	    		for (int i=0; i<grpAssign.length; i++){
	    			if (grpAssign[i]!=0)
	    				inc[grpAssign[i]-1]++;
	    		}
	    		if (inc[0] < 2 || inc[1] < 2){
	    			JOptionPane.showMessageDialog(null, "Please select at least 2 samples for each group.", "Error", JOptionPane.WARNING_MESSAGE);
	        		return false;
	    		}
	    		return true;
	    	}	
	    	case 3:{
	    		int[] inc = new int[getNumGroups()];
	    		int[] grpAssign = getGroupAssignments();
	    		for (int i=0; i<grpAssign.length; i++){
	    			if (grpAssign[i]!=0)
	    				inc[grpAssign[i]-1]++;
	    		}
	    		for (int i=0; i<inc.length; i++){
	        		if (inc[i] < 2){
	        			JOptionPane.showMessageDialog(null, "Please select at least 2 samples for each group.", "Error", JOptionPane.WARNING_MESSAGE);
	            		return false;
	        		}
	    		}
	    		return true;
	    	}	
    	}
    	return false;
    }
    
    public int[] getGroupAssignments() {
    	if (getExperimentalDesign()<4)
    		return getSimpleGroupAssignments();
        return null;
    }  

    private int[] getClusterSelectorGroupAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
    	for (int i=0; i<mPanel.numGroups; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.groupsCS.getGroupSamples("Class "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
    		for (int j = 0;j<mPanel.numGroups;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	return groupAssignments;
    }
    
    private int[] getSimpleGroupAssignments() {
    	if (getSelectionDesign()==ATTRACTInitBox.CLUSTER_SELECTION)
    		return getClusterSelectorGroupAssignments();
        int[] groupAssignments = new int[exptNames.size()];
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                groupAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.sampleSelectionPanel.assignmentRBs.length; j++) {
                    if (mPanel.sampleSelectionPanel.assignmentRBs[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        return groupAssignments;
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
    	int design = -1;
    	if (mPanel.ngPanel.twoClass.isSelected())
    		design = 2;
    	if (mPanel.ngPanel.multiClass.isSelected())
    		design = 3;
    	return design;
    }
    
    public int getSelectionDesign() {
        int design = -1;
        if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = ATTRACTInitBox.BUTTON_SELECTION;
        } else {
        	design = ATTRACTInitBox.CLUSTER_SELECTION;
        }
        return design;
    }

    public String[] getGroupNames() {
    	String[] ret = new String[getNumGroups()];
    	for (int i=0; i<ret.length; i++){
    		ret[i] = mPanel.ngPanel.groupNameField[i].getText();
    	}
    	return ret;
    }
    public int getNumGroups() {
    	int numGroups = 0;
    	if (getExperimentalDesign()==2){
    		numGroups = 2;
    	}else{
    		try{
    			numGroups =Integer.parseInt(mPanel.ngPanel.numGroupsField.getText());
    			if (numGroups>8||numGroups<2){
                	JOptionPane.showMessageDialog(null, "The number of groups must be greater than 1 and less than 9.", "Error", JOptionPane.ERROR_MESSAGE);
                	numGroups =-1;
    			}
    		} catch (Exception e){
    			JOptionPane.showMessageDialog(null, "Error reading parameter input.", "Error", JOptionPane.ERROR_MESSAGE);
    		}
    	}
        return numGroups;
    }
    public String getChipName() {
        return mPanel.chipName;
    }
    public float getAlpha() {
    	return Float.parseFloat(mPanel.ngPanel.alphaField.getText());
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 24; i++) {
            dummyVect.add("Expt " + i);
        }
        String annot = "hgu133a";
        ATTRACTInitBox oBox = new ATTRACTInitBox(dummyFrame, true, dummyVect, null, annot);
        oBox.setVisible(true);
        for (int i=0; i<oBox.getGroupNames().length; i++){
        	System.out.println(oBox.getGroupNames()[i]);
        }
        System.exit(0);
    }
}
