/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MINETInitBox.java,v $
 * $Revision: 1.10 $
 * $Date: 2008-11-07 17:27:40 $
 * $Author: raktim $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.minet;

import java.awt.Color;
import java.awt.Component;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

/**
 *
 * @author  raktim
 * @version 
 */
public class MINETInitBox extends AlgorithmDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int JUST_ALPHA = 1;
    public static final int STD_BONFERRONI = 2;
    public static final int ADJ_BONFERRONI = 3;
    public static final int MAX_T = 4; 
    public static final int FALSE_NUM = 5;
    public static final int FALSE_PROP = 6;    
    public static final int ONE_CLASS = 7;
    public static final int TWO_CLASS = 8;
    public static final int PAIRED = 9;
    public static final int BUTTON_SELECTION = 10;
    public static final int CLUSTER_SELECTION = 11;
    
    boolean okPressed = false;
    boolean okReady = false;
    Vector<String> exptNames;    
    MainPanel mPanel;
    PValuePanel pPanel;
    MethodsPanel methodsPanel; 
    EstimatorPanel estimatorPanel;
    DiscretizationPanel discretizationPanel;
    //HCLSigOnlyPanel hclOpsPanel;
    ClusterRepository experimentClusterRepository;
    ClusterRepository geneClusterRepository;
    ClusterBrowser browser;
    
    /** Creates new MINETInitBox */
    public MINETInitBox(
    		JFrame parentFrame, 
    		boolean modality, 
    		Vector<String> exptNames, 
    		ClusterRepository experimentClusterRepository, 
    		ClusterRepository geneClusterRepository) {
        super(parentFrame, "MINET Initialization", modality);
        this.exptNames = exptNames;  
        this.experimentClusterRepository = experimentClusterRepository;
        this.geneClusterRepository = geneClusterRepository;
        setBounds(0, 0, 600, 700);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        this.okButton.setEnabled(true);
          
        mPanel = new MainPanel();

            
        JPanel paramsPane = new JPanel();
        methodsPanel = new MethodsPanel();
        paramsPane.add( methodsPanel);
        
        estimatorPanel = new EstimatorPanel();
        paramsPane.add(estimatorPanel);
        
        discretizationPanel = new DiscretizationPanel();
        paramsPane.add(discretizationPanel);
        
        JPanel popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);
		//popPanel = new PopSelectionPanel();
		browser = new ClusterBrowser(this.geneClusterRepository);
		popNClusterPanel.add(
				browser, 
				new GridBagConstraints(
						0,1,1,1,1.0,1.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, 
						new Insets(0,0,0,0),0,0)
				);
		
		JTabbedPane consolidatedPane = new JTabbedPane();  
		consolidatedPane.add("Cluster Selection", browser);
		consolidatedPane.add("Parameters", paramsPane);
		 
        //pPanel = new PValuePanel();
        //consolidatedPane.add("P-Value/FDR Parameters", pPanel);
        
        //hclOpsPanel = new HCLSigOnlyPanel();
        //consolidatedPane.add("Hierarchical Clusters", hclOpsPanel);  
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(consolidatedPane, constraints);
        pane.add(consolidatedPane);   
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        //gridbag.setConstraints(hclOpsPanel, constraints);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  
        
        if(geneClusterRepository == null || geneClusterRepository.isEmpty()) {
			Component comp = consolidatedPane.getComponentAt(0);
			JPanel panel = (JPanel)comp;
			panel.removeAll();
			panel.validate();
			panel.setOpaque(false);
			panel.add(new JLabel("Empty Cluster Repository"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(15,0,10,0),0,0));
			panel.add(new JLabel("Please create a gene cluster and launch MINET again."), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
			consolidatedPane.setSelectedIndex(0);
			this.okButton.setEnabled(false);
		}
    }

    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
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
    
   // public boolean drawTrees() {
        //return this.hclOpsPanel.isHCLSelected();
    //}   
    
    //public boolean drawSigTreesOnly() {
        //return hclOpsPanel.drawSigTreesOnly();
    //}    
    
    class MainPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        ExperimentsPanel oneClassPanel;
        //ExperimentsPanel twoClassPanel;
        //TwoClassPairedMainPanel pairedPanel;
        //JTabbedPane chooseDesignPane;
        JTabbedPane oneClassmulg;
        //JTabbedPane twoClassmulg;
        ClusterSelector oneClassClusterSelector;
        //ClusterSelector twoClassClusterSelector;
        float alpha;
        
        public MainPanel() {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            this.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
                okReady = true;
                try {
                    oneClassPanel = new ExperimentsPanel(exptNames, 1);
                    //twoClassPanel = new ExperimentsPanel(exptNames, 2);
                    
                    
                    
                    oneClassClusterSelector= new ClusterSelector(experimentClusterRepository,1, "Samples");
                    if (experimentClusterRepository!=null){
                    	oneClassClusterSelector.setClusterType("Experiment");
            		}
                    //twoClassClusterSelector= new ClusterSelector(repository,2, "Samples");
                    //if (repository!=null){
                    	//twoClassClusterSelector.setClusterType("Experiment");
            		//}
                    
                    JPanel oneClassClusterSelectorPanel = new JPanel();
                    oneClassClusterSelectorPanel.setLayout(new GridBagLayout());
                    //JPanel twoClassClusterSelectorPanel = new JPanel();
                    //twoClassClusterSelectorPanel.setLayout(new GridBagLayout());

                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weighty =1;
                    c.weightx = 1;
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.anchor = GridBagConstraints.PAGE_END;
                    oneClassClusterSelectorPanel.add(oneClassClusterSelector, c);
                    //twoClassClusterSelectorPanel.add(twoClassClusterSelector, c);
                    
                    oneClassmulg = new JTabbedPane();
                    oneClassmulg.add("Button Selection", oneClassPanel);
                    oneClassmulg.add("Cluster Selection", oneClassClusterSelectorPanel);
                    oneClassmulg.setSelectedIndex(1);//set to be cluster selection
                    if (experimentClusterRepository==null||experimentClusterRepository.isEmpty())
                    	oneClassmulg.setSelectedIndex(0);
                    
                    //twoClassmulg = new JTabbedPane();
                    //twoClassmulg.add("Button Selection", twoClassPanel);
                    //twoClassmulg.add("Cluster Selection", twoClassClusterSelectorPanel);
                    //twoClassmulg.setSelectedIndex(1);//set to be cluster selection
                    //if (repository==null||repository.isEmpty())
                    	//twoClassmulg.setSelectedIndex(0);

                    //pairedPanel = new TwoClassPairedMainPanel();

                    //chooseDesignPane = new JTabbedPane();
                    //chooseDesignPane.add("One-Class", oneClassmulg);
                    //chooseDesignPane.add("Two-Class Unpaired", twoClassmulg);
                    //chooseDesignPane.add("Two-Class Paired", pairedPanel);
                    
                    buildConstraints(constraints, 1, 0, 1, 3, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    //gridbag.setConstraints(chooseDesignPane, constraints);
                    //MainPanel.this.add(chooseDesignPane);
                    gridbag.setConstraints(oneClassPanel, constraints);
                    MainPanel.this.add(oneClassPanel);
                    MainPanel.this.validate();
                
            
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please enter a value greater than 0 and less than 1!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
        

        
        class ExperimentsPanel extends JPanel {
            int numPanels = 0;
            int numberGroups;
            JLabel[] expLabels;
            JCheckBox[] exptCheckBoxes;
            JRadioButton[][] rbArray;
            ExperimentsPanel(Vector<String> exptNames, int numGroups) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Experiment Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);
                numberGroups = numGroups;
                ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
                expLabels = new JLabel[exptNames.size()];
                exptCheckBoxes = new JCheckBox[exptNames.size()];
                rbArray = new JRadioButton[exptNames.size()][numberGroups+1];
                numPanels = exptNames.size()/512 + 1;
                
                
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
                    buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                    if (numGroups==1){
                    	constraints.anchor = GridBagConstraints.WEST;
                    	exptCheckBoxes[i] = new JCheckBox("",true);
                        gridbag.setConstraints(exptCheckBoxes[i], constraints);
                        panels[currPanel].add(exptCheckBoxes[i]);
                    }
                    else{
                        chooseGroup[i] = new ButtonGroup();
	                    for (int j = 0; j < numberGroups+1; j++) {
	                    	
	                        rbArray[i][j] = new JRadioButton("Group "+ (j+1));
	                        if (j==numberGroups)
		                        rbArray[i][j] = new JRadioButton("Excluded");
	                        
	                        chooseGroup[i].add(rbArray[i][j]);
	                        buildConstraints(constraints, j, i%512, 1, 1, 100, 100);
	                        gridbag.setConstraints(rbArray[i][j], constraints);
	                        panels[currPanel].add(rbArray[i][j]);
	                    }
	                    rbArray[i][numberGroups].setSelected(true);
                    }
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
                
                //need to possibly add to additional panels if number of exp. excedes 512
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
                
                buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(scroll, constraints);
                this.add(scroll);
                
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
                
                
                
                resetButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                    	reset();
                    }
                });
                
                final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
                
                saveButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                    	if (numberGroups==1)
                    		saveOneClassAssignments();
                    }
                });
                
                
                loadButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                    	if (numberGroups==1)
                    		loadOneClassAssignments();
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
                gridbag2.setConstraints(panel2, constraints);
                this.add(panel2);
                
            }
            
            /**
             *  resets all group assignments
             */
            protected void reset(){
            	for (int i=0; i<oneClassPanel.exptCheckBoxes.length; i++){
            		oneClassPanel.exptCheckBoxes[i].setSelected(true);
            	}
            	//for (int i=0; i<twoClassPanel.rbArray.length; i++){
            		//twoClassPanel.rbArray[i][2].setSelected(true);
            	//}
            }
            /**
        	 * Saves the assignments to file.
        	 * 
        	 * Comments include title, user, save date
        	 * Design information includes factor a and b labels and the level names for each factor
        	 * A header row is followed by sample index, sample name (primary, field index = 0),
        	 * them factor A assignment (text label) then factor B assignment (text label)
        	 */
        	private void saveOneClassAssignments() {
        		
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
        				pw.println("MINET");
        				pw.print("Group 1 Label:\t");
    					pw.println("Include");
        								
        				pw.println("#");
        				
        				pw.println("Sample Index\tSample Name\tGroup Assignment");

        				for(int sample = 0; sample < exptNames.size(); sample++) {
        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
        					pw.print(exptNames.get(sample)+"\t");
        					if(((JCheckBox)(exptCheckBoxes[sample])).isSelected())
        						pw.println("Include");
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

        	
        	/**
        	 * Loads file based assignments
        	 */
        	private void loadOneClassAssignments() {
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
        		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());
        		
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
	        			
	        			//parse the data in to these structures
	        			String [] lineArray;
	        			//String status = "OK";
	        			for(int row = 0; row < data.size(); row++) {
	        				line = (String)(data.get(row));
	
	        				//if not a comment line, and not the header line
	        				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
	        					
	        					lineArray = line.split("\t");
	        					
	        					//check what module saved the file
	        					if(lineArray[0].startsWith("Module:")) {
	        						if (!lineArray[1].equals("MINET")){
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
	        				}				
	        			}
	        			
	        			//we have the data parsed, now validate, assign current data
	
	
	        			if( exptNames.size() != sampleNames.size()) {
	        				//status = "number-of-samples-mismatch";
	        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
	        				//warn and prompt to continue but omit assignments for those not represented				
	
	        				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
	        						                                   "does not match the number of samples loaded in MeV ("+exptNames.size()+").<br>" +
	        						                                   	"Assingments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
	        				
	        				return;
	        			}
	        	
	        			
	        			Vector<String> currSampleVector = new Vector<String>();
	        			for(int i = 0; i < exptNames.size(); i++)
	        				currSampleVector.add(exptNames.get(i));
	        			
	        			//set all to excluded
	        			
	        			
	        			
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
	        					setOneClassStateBasedOnIndex(groupAssignments,groupNames);
	        					break;
	        				}
	        				
	        				groupName = (String)(groupAssignments.get(fileSampleIndex));
	        				
	        				groupIndex = groupNames.indexOf(groupName);
	        				
	        				//set state
	        				if(groupIndex ==0)
	        					exptCheckBoxes[sample].setSelected(true);
	        				else
	        					exptCheckBoxes[sample].setSelected(false);  //set to last state... excluded
	        			}
	        			
	        			repaint();			
	        			//need to clear assignments, clear assignment booleans in sample list and re-init
	        			//maybe a specialized inti for the sample list panel.
	        		} catch (Exception e) {
	        			JOptionPane.showMessageDialog(this, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
	        		}
	        	}
        	}
        	private void setOneClassStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
        		Object[] optionst = { "Continue", "Cancel" };
        		if (JOptionPane.showOptionDialog(null, 
						"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
						optionst, optionst[0])==1)
					return;
				
        		for(int sample = 0; sample < exptNames.size(); sample++) {
        			if(groupNames.indexOf(groupAssignments.get(sample))==0)
    					exptCheckBoxes[sample].setSelected(true);
    				else
    					exptCheckBoxes[sample].setSelected(false);
        		}
        	}

        	/**
        	 * Loads file based assignments
        	 */
        	private void loadTwoClassAssignments() {
        		
        		File file;		
        		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());
        		
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
            			
            			//parse the data in to these structures
            			String [] lineArray;
            			//String status = "OK";
            			for(int row = 0; row < data.size(); row++) {
            				line = (String)(data.get(row));

            				//if not a comment line, and not the header line
            				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
            					
            					lineArray = line.split("\t");
            					
            					//check what module saved the file
            					if(lineArray[0].startsWith("Module:")) {
            						if (!lineArray[1].equals("MINET: Two-Class Unpaired")){
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
            					setTwoClassStateBasedOnIndex(groupAssignments,groupNames);
            					break;
            				}
            				
            				groupName = (String)(groupAssignments.get(fileSampleIndex));
            				groupIndex = groupNames.indexOf(groupName);
            				
            				//set state
            				try{
    	        				if (groupIndex==0)
    	        					rbArray[sample][0].setSelected(true);
    	        				if (groupIndex==1)
    	        					rbArray[sample][1].setSelected(true);
    	        				if (groupIndex==2||groupIndex==-1)
    	        					rbArray[sample][2].setSelected(true);
            				}catch (Exception e){
            					rbArray[sample][2].setSelected(true);  //set to last state... excluded
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

        	private void setTwoClassStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
        		Object[] optionst = { "Continue", "Cancel" };
        		if (JOptionPane.showOptionDialog(null, 
    					"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
    					"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
    					optionst, optionst[0])==1)
    				return;
    			
        		for(int sample = 0; sample < exptNames.size(); sample++) {
        			//set state
    				try{
        				if (groupNames.indexOf(groupAssignments.get(sample))==0)
        					rbArray[0][sample].setSelected(true);
        				if (groupNames.indexOf(groupAssignments.get(sample))==1)
        					rbArray[1][sample].setSelected(true);
        				if (groupNames.indexOf(groupAssignments.get(sample))==2||groupNames.indexOf(groupAssignments.get(sample))==-1)
        					rbArray[2][sample].setSelected(true);
    				}catch (Exception e){
    					rbArray[2][sample].setSelected(true);  //set to last state... excluded
    				}
        		}
        	}
           
            
            
        }
        protected void reset(){
        	for (int i=0; i<oneClassPanel.exptCheckBoxes.length; i++){
        		oneClassPanel.exptCheckBoxes[i].setSelected(true);
        	}
        	
        	//for (int i=0; i<twoClassPanel.rbArray.length; i++){
        		//twoClassPanel.rbArray[i][2].setSelected(true);
        	//}
        }
    } 
    
    class MethodsPanel extends JPanel {
    	//"clr", "aracne" or "mrnet"
    	JRadioButton aracne, clr, mrnet;
    	public MethodsPanel(){
    		this.setBorder(new TitledBorder(
    				new EtchedBorder(), 
    				"Inference algorithm", 
    				TitledBorder.DEFAULT_JUSTIFICATION, 
    				TitledBorder.DEFAULT_POSITION, 
    				new Font("Dialog", Font.BOLD, 12), Color.black)
    		);
    		GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //this.setBackground(Color.white);
            this.setLayout(gridbag);
            aracne = new JRadioButton("aracne", false);
            clr = new JRadioButton("clr", false);
            mrnet = new JRadioButton("mrnet", true);
            //aracne.setBackground(Color.white);
            //clr.setBackground(Color.white);
            //mrnet.setBackground(Color.white);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(aracne);
            buttonGroup.add(clr);
            buttonGroup.add(mrnet);
            aracne.setSelected(true);

            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(aracne, constraints);
            this.add(aracne);  

            buildConstraints(constraints, 1, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(clr, constraints);
            this.add(clr);  

            buildConstraints(constraints, 2, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(mrnet, constraints);
            this.add(mrnet);  
            
    	}
    	public int getMethod(){
    		if (aracne.isSelected())
    			return 1;
    		if (clr.isSelected())
    			return 2;
    		if (mrnet.isSelected())
    			return 3;
    		return 0;
    	}
    	
    	public String getMethodName(){
    		if (aracne.isSelected())
    			return aracne.getText();
    		if (clr.isSelected())
    			return clr.getText();
    		if (mrnet.isSelected())
    			return mrnet.getText();
    		return null;
    	}
    }
    
    
    class EstimatorPanel extends JPanel {
    	//"mi.empirical", "mi.mm", "mi.shrink", "mi.sg"(default: "mi.empirical")
    	JRadioButton miEmpirical, miMm, miShrink, miSg;
    	public EstimatorPanel(){
    		this.setBorder(new TitledBorder(
    				new EtchedBorder(), 
    				"Mutual information estimator", 
    				TitledBorder.DEFAULT_JUSTIFICATION, 
    				TitledBorder.DEFAULT_POSITION, 
    				new Font("Dialog", Font.BOLD, 12), Color.black)
    		);
    		GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //this.setBackground(Color.white);
            this.setLayout(gridbag);
            miEmpirical = new JRadioButton("mi.empirical", false);
            miMm = new JRadioButton("mi.mm", false);
            miShrink = new JRadioButton("mi.shrink", true);
            miSg = new JRadioButton("mi.sg", true);
            //miEmpirical.setBackground(Color.white);
            //miMm.setBackground(Color.white);
            //miShrink.setBackground(Color.white);
            //miSg.setBackground(Color.white);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(miEmpirical);
            buttonGroup.add(miMm);
            buttonGroup.add(miShrink);
            buttonGroup.add(miSg);

            miShrink.setSelected(true);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(miEmpirical, constraints);
            this.add(miEmpirical);  

            buildConstraints(constraints, 1, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(miMm, constraints);
            this.add(miMm);  

            buildConstraints(constraints, 2, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(miShrink, constraints);
            this.add(miShrink);
            
            buildConstraints(constraints, 3, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(miSg, constraints);
            this.add(miSg); 
            
    	}
    	public int getEstimator(){
    		if (miEmpirical.isSelected())
    			return 1;
    		if (miMm.isSelected())
    			return 2;
    		if (miShrink.isSelected())
    			return 3;
    		if (miSg.isSelected())
    			return 4;
    		return 0;
    	}
    	
    	public String getEstimatorName(){
    		if (miEmpirical.isSelected())
    			return miEmpirical.getText();
    		if (miMm.isSelected())
    			return miMm.getText();;
    		if (miShrink.isSelected())
    			return miShrink.getText();;
    		if (miSg.isSelected())
    			return miSg.getText();;
    		return null;
    	}
    }
    
    class DiscretizationPanel extends JPanel {

    	//discretization method to be used, if required by the estimator
    	//"none" ,"equalfreq", "equalwidth" or "globalequalwidth" (default : "equalfreq")
    	JRadioButton none, equalfreq, equalwidth, globalequalwidth;
    	public DiscretizationPanel(){
    		this.setBorder(new TitledBorder(
    				new EtchedBorder(), 
    				"Discretization Method", 
    				TitledBorder.DEFAULT_JUSTIFICATION, 
    				TitledBorder.DEFAULT_POSITION, 
    				new Font("Dialog", Font.BOLD, 12), Color.black)
    		);
    		GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //this.setBackground(Color.white);
            this.setLayout(gridbag);
            none = new JRadioButton("none", false);
            equalfreq = new JRadioButton("Equal Frequency", false);
            equalwidth = new JRadioButton("Equal width", false);
            globalequalwidth = new JRadioButton("Global Equal Width", true);
            //equalfreq.setBackground(Color.white);
            //equalwidth.setBackground(Color.white);
            //globalequalwidth.setBackground(Color.white);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(none);
            buttonGroup.add(equalfreq);
            buttonGroup.add(equalwidth);
            buttonGroup.add(globalequalwidth);
            
            equalwidth.setSelected(true);

            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(equalfreq, constraints);
            this.add(none); 
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(equalfreq, constraints);
            this.add(equalfreq); 

            buildConstraints(constraints, 2, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(equalwidth, constraints);
            this.add(equalwidth);  

            buildConstraints(constraints, 3, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(globalequalwidth, constraints);
            this.add(globalequalwidth);
            
    	}
    	public int getDiscretizationMethod(){
    		if (equalfreq.isSelected())
    			return 1;
    		if (equalwidth.isSelected())
    			return 2;
    		if (globalequalwidth.isSelected())
    			return 3;
    		return 0;
    	}
    	
    	public String getDiscretizationMethodName(){
    		if (equalfreq.isSelected())
    			return "equalfreq";
    		if (none.isSelected())
    			return "none";
    		if (equalwidth.isSelected())
    			return "equalwidth";
    		if (globalequalwidth.isSelected())
    			return "globalequalwidth";
    		return null;
    	}
    }
    
    class PValuePanel extends JPanel {
    	JLabel numPermsLabel;
    	JTextField timesField;
        JTextField pValueInputField, falseNumField, falsePropField;
        JRadioButton pValueButton, falseNumButton, falsePropButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value / false discovery parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            this.setBackground(Color.white);
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);
            numPermsLabel = new JLabel("Enter number of permutations");
            //numPermsLabel.setEnabled(false);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            timesField = new JTextField("100", 7);
            timesField.setMinimumSize(new Dimension(50, 20));
            //timesField.setEnabled(false);
            //timesField.setBackground(Color.darkGray);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(timesField, constraints);
            this.add(timesField);
            
            JPanel pValSelectionPanel = new JPanel();
            pValSelectionPanel.setBackground(Color.white);
            pValSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "P-Value Cutoff ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout pValSelectionPanelgrid = new GridBagLayout();
            pValSelectionPanel.setLayout(pValSelectionPanelgrid);
            

            pValueButton = new JRadioButton("", true);
            pValueButton.setFocusPainted(false);
            pValueButton.setForeground(UIManager.getColor("Label.foreground"));
            pValueButton.setBackground(Color.white);
            pValSelectionPanel.add(pValueButton);
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 1, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.WEST;
            pValSelectionPanelgrid.setConstraints(pValueLabel, constraints);
            pValSelectionPanel.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            pValueInputField.setMinimumSize(new Dimension(50, 20));
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.EAST;
            pValSelectionPanelgrid.setConstraints(pValueInputField, constraints);
            pValSelectionPanel.add(pValueInputField);

	        buildConstraints(constraints, 0, 1, 2, 1, 33, 25);
	        constraints.anchor = GridBagConstraints.EAST;
	        constraints.fill=GridBagConstraints.HORIZONTAL;
	        gridbag.setConstraints(pValSelectionPanel, constraints);
            this.add(pValSelectionPanel);
	        
            constraints.anchor = GridBagConstraints.CENTER;
            
            
            falseNumButton = new JRadioButton("EITHER, The number of false significant genes should not exceed", false);
            //falseNumButton.setEnabled(false);
            falseNumButton.setFocusPainted(false);
            falseNumButton.setForeground(UIManager.getColor("Label.foreground"));
            falseNumButton.setBackground(Color.white);
            //sigGroup.add(falseNumButton);            
            
            falsePropButton = new JRadioButton("OR, The proportion of false significant genes should not exceed", false);
            //falsePropButton.setEnabled(false);
            falsePropButton.setFocusPainted(false);
            falsePropButton.setForeground(UIManager.getColor("Label.foreground"));
            falsePropButton.setBackground(Color.white);
            //sigGroup.add(falsePropButton);            
            
            falseNumField = new JTextField(10);
            falseNumField.setText("10");
            falseNumField.setMinimumSize(new Dimension(50, 20));
            falsePropField = new JTextField(10);
            falsePropField.setText("0.05");
            falsePropField.setMinimumSize(new Dimension(50, 20));          
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(falseNumButton);
            chooseCorrection.add(falsePropButton);
            chooseCorrection.add(pValueButton);
            //stdBonfButton.setEnabled(false);
            //adjBonfButton.setEnabled(false);
            
            JPanel FDMINETanel = new JPanel();
            FDMINETanel.setBackground(Color.white);
            FDMINETanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout grid3 = new GridBagLayout(); 
            FDMINETanel.setLayout(grid3);            
            
            JLabel FDRLabel = new JLabel("With confidence of [1 - alpha] : ");            
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 0, 2, 1, 100, 34);
            grid3.setConstraints(FDRLabel, constraints);
            FDMINETanel.add(FDRLabel);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falseNumButton, constraints);
            FDMINETanel.add(falseNumButton);       
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falseNumField, constraints);
            FDMINETanel.add(falseNumField);    
            
            buildConstraints(constraints, 0, 2, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falsePropButton, constraints);
            FDMINETanel.add(falsePropButton);    
            
            buildConstraints(constraints, 1, 2, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falsePropField, constraints);
            FDMINETanel.add(falsePropField);    
            
            constraints.anchor = GridBagConstraints.CENTER;
           
            
            buildConstraints(constraints, 0, 3, 4, 1, 100, 50);
            gridbag.setConstraints(FDMINETanel, constraints);
            this.add(FDMINETanel);            
        }
        
        protected void reset() {
            pValueInputField.setText("0.01");
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getSelectionDesign()==MINETInitBox.CLUSTER_SELECTION)&&(experimentClusterRepository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                try {               
                   
                    int[] inGroupAssignments;
                    if (getTestDesign()==MINETInitBox.ONE_CLASS){
	                    if (getSelectionDesign()==MINETInitBox.CLUSTER_SELECTION){
	                    	inGroupAssignments=getClusterOneClassAssignments();
	                    }else{
	                    	inGroupAssignments=getOneClassAssignments();
	                    }
	                    int inNum = 0;
	                    while(true){
	                    	
	                    	if (inGroupAssignments[inNum]==1)
	                    		break;
	                    	inNum++;
	                    	if (inNum==inGroupAssignments.length){
	                    		JOptionPane.showMessageDialog(null, "No samples have been assigned to the analysis.", "Error!", JOptionPane.ERROR_MESSAGE);
	                    		okPressed = false;
	                    		return;
	                    	}
	                    }
	                    okPressed = true;
                        dispose();
                    } else {
                        okPressed = true;
                        dispose();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);
                }

                
            } else if (command.equals("reset-command")) {
                mPanel.reset();
                pPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(MINETInitBox.this, "MINET Initialization Dialog");
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
    
    public int getSelectionDesign() {
        int design = -1;
        if (getTestDesign()==MINETInitBox.ONE_CLASS){
	        if (mPanel.oneClassmulg.getSelectedIndex() == 0) {
	        	design = MINETInitBox.BUTTON_SELECTION;
	        } else {
	        	design = MINETInitBox.CLUSTER_SELECTION;
	        }
        }
        return MINETInitBox.BUTTON_SELECTION;
    }
    public int getTestDesign() {
        int design = -1;
        //if (mPanel.chooseDesignPane.getSelectedIndex() == 0) {
        	design = MINETInitBox.ONE_CLASS;
        //} else if (mPanel.chooseDesignPane.getSelectedIndex() == 1) {
        	//design = MINETInitBox.TWO_CLASS;
        //} else if (mPanel.chooseDesignPane.getSelectedIndex() == 2) {
        	//design = MINETInitBox.PAIRED;
        //}
        return design;
    }
    
    public int[] getOneClassAssignments() {
        int[] inGroupAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.oneClassPanel.exptCheckBoxes[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                inGroupAssignments[i] = 1;
            } else {
                inGroupAssignments[i] = 0;
            }
        }
        return inGroupAssignments;
    }  
   
    public int[] getClusterOneClassAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.oneClassClusterSelector.getGroupSamples("Samples "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
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
	    			groupAssignments[i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	return groupAssignments;
    }
    

    /** Returns the cluster selected for analysis.
	 * @return  */
	public Cluster getSelectedCluster(){
		return this.browser.getSelectedCluster();
	}
	
    public boolean usePerms() {
        return true;//this.pPanel.permutButton.isSelected();
    }
    
    public int getNumPerms() {
        return Integer.parseInt(this.pPanel.timesField.getText());
    }
    
    public float getPValue() {
        return Float.parseFloat(pPanel.pValueInputField.getText());
    }
    
    
    public int getFalseNum() {
        return Integer.parseInt(pPanel.falseNumField.getText());
    }
    
    public double getFalseProp() {
        return Double.parseDouble(pPanel.falsePropField.getText());
    }    
    
    public boolean validateFalseNum() {
        int a;
        try {
            String falseNum = pPanel.falseNumField.getText();
            a = Integer.parseInt(falseNum);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(MINETInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(MINETInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;          
        }
        return true;
    }
    
    public boolean validateFalseProp() {
        float a;
        try {
            String falseProp = pPanel.falsePropField.getText();
            a = Float.parseFloat(falseProp);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(MINETInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(MINETInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;          
        }
        return true;
    }       

    public int getCorrectionMethod() {
        int method = JUST_ALPHA;
        if (pPanel.falseNumButton.isSelected()) {
            method = FALSE_NUM;
        } else if (pPanel.falsePropButton.isSelected()) {
            method = FALSE_PROP;
        }
        
        return method;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 23; i++) {
            dummyVect.add("Expt " + (i+1));
        }
        dummyVect.add("Exptsdfsdfsgwegsgsgsd");
        
        MINETInitBox oBox = new MINETInitBox(dummyFrame, true, dummyVect, null, null);
        oBox.setVisible(true);
        System.out.println("end");
        System.exit(0);
        
    }
}
