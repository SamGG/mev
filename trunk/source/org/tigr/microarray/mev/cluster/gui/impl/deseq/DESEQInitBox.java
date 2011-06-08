/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: DESEQInitBox.java,v $
 * $Revision: 1.10 $
 * $Date: 2008-11-07 17:27:40 $
 * $Author: raktim $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.deseq;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.edger.EDGERInitBox;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.rp.RPInitBox;

/**
 *
 * @author  raktim
 * @version 
 */
public class DESEQInitBox extends AlgorithmDialog {

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
    //MethodsPanel methodsPanel; 
    //EstimatorPanel estimatorPanel;
    //DiscretizationPanel discretizationPanel;
    //HCLSigOnlyPanel hclOpsPanel;
    ClusterRepository repository;
    //ClusterRepository geneClusterRepository;
    //ClusterBrowser browser;
    
    /** Creates new DESEQInitBox */
    public DESEQInitBox(
    		JFrame parentFrame, 
    		boolean modality, 
    		Vector<String> exptNames,
    		ClusterRepository experimentClusterRepository
    		//ClusterRepository geneClusterRepository
    		) {
        super(parentFrame, "DESeq Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = experimentClusterRepository;
        //this.geneClusterRepository = geneClusterRepository;
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
        pPanel = new PValuePanel();
        paramsPane.add(pPanel);
        
		JTabbedPane consolidatedPane = new JTabbedPane(); 
		consolidatedPane.add("Parameters", paramsPane);
		 
      
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(consolidatedPane, constraints);
        pane.add(consolidatedPane);   
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
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
    
    
    class MainPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        //ExperimentsPanel oneClassPanel;
        ExperimentsPanel twoClassPanel;
        //TwoClassPairedMainPanel pairedPanel;
        //JTabbedPane chooseDesignPane;
        //JTabbedPane oneClassmulg;
        JTabbedPane twoClassmulg;
        //ClusterSelector oneClassClusterSelector;
        ClusterSelector twoClassClusterSelector;
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
                   
                    twoClassPanel = new ExperimentsPanel(exptNames, 2);
                    
                    twoClassClusterSelector= new ClusterSelector(repository, 2, "Samples");
                    if (repository!=null){
                    	twoClassClusterSelector.setClusterType("Experiment");
            		}

                    JPanel twoClassClusterSelectorPanel = new JPanel();
                    twoClassClusterSelectorPanel.setLayout(new GridBagLayout());
                    
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weighty =1;
                    c.weightx = 1;
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.anchor = GridBagConstraints.PAGE_END;
                    twoClassClusterSelectorPanel.add(twoClassClusterSelector, c);
                    
                    twoClassmulg = new JTabbedPane();
                    twoClassmulg.add("Button Selection", twoClassPanel);
                    twoClassmulg.add("Cluster Selection", twoClassClusterSelectorPanel);
                    twoClassmulg.setSelectedIndex(1);//set to be cluster selection
                    if (repository==null||repository.isEmpty())
                    	twoClassmulg.setSelectedIndex(0);
                                        
                    buildConstraints(constraints, 1, 0, 1, 3, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    
                    gridbag.setConstraints(twoClassmulg, constraints);
                    MainPanel.this.add(twoClassmulg);
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
                    	//if (numberGroups==1)
                    		//saveOneClassAssignments();
                    	if (numberGroups==2)
                    		saveTwoClassAssignments();
                    }
                });
                
                
                loadButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                    	//if (numberGroups==1)
                    		//loadOneClassAssignments();
                    	if (numberGroups==2)
                    		loadTwoClassAssignments();
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
            	//for (int i=0; i<oneClassPanel.exptCheckBoxes.length; i++){
            		//oneClassPanel.exptCheckBoxes[i].setSelected(true);
            	//}
            	for (int i=0; i<twoClassPanel.rbArray.length; i++){
            		//twoClassPanel.rbArray[i][2].setSelected(true);
            	}
            }
            
            /**
     	   	 * Saves the assignments to file.
     	   	 * 
     	   	 */
     	   	private void saveTwoClassAssignments() {
     	   		
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
     	   				pw.println("edgeR: Two-Class Unpaired");
     	       			pw.print("Group 1"+" Label:\t");
     	   				pw.println("1");
     	       			pw.print("Group 2"+" Label:\t");
     	   				pw.println("2");
     	   				
     	   								
     	   				pw.println("#");
     	   				
     	   				pw.println("Sample Index\tSample Name\tGroup Assignment");
     	   				
     	   				int[] groupAssgn = getTwoClassAssignments();
     	   				
     	   				for(int sample = 0; sample < exptNames.size(); sample++) {
     	   					pw.print(String.valueOf(sample+1)+"\t"); //sample index
     	   					pw.print(exptNames.get(sample)+"\t");
     	   					if (groupAssgn[sample]!=3)
     	   						pw.println((groupAssgn[sample]));
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
            						if (!lineArray[1].equals("edgeR: Two-Class Unpaired")){
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
        	//for (int i=0; i<oneClassPanel.exptCheckBoxes.length; i++){
        		//oneClassPanel.exptCheckBoxes[i].setSelected(true);
        	//}
        	
        	for (int i=0; i<twoClassPanel.rbArray.length; i++){
        		twoClassPanel.rbArray[i][2].setSelected(true);
        	}
        }
    } 
    
    class MethodsPanel extends JPanel {
    	//"modDisp", "commDisp" 
    	JRadioButton commDisp, modDisp;
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
            commDisp = new JRadioButton("Common dispersion", false);
            modDisp = new JRadioButton("Moderated tagwise dispersions", false);
            //commDisp.setBackground(Color.white);
            //modDisp.setBackground(Color.white);
            //mrnet.setBackground(Color.white);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(commDisp);
            buttonGroup.add(modDisp);
            commDisp.setSelected(true);

            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(commDisp, constraints);
            this.add(commDisp);  

            buildConstraints(constraints, 1, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(modDisp, constraints);
            this.add(modDisp);
            
    	}
    	public int getMethod(){
    		if (commDisp.isSelected())
    			return 1;
    		if (modDisp.isSelected())
    			return 2;
    		return 0;
    	}
    	
    	public String getMethodName(){
    		if (commDisp.isSelected())
    			return commDisp.getText();
    		if (modDisp.isSelected())
    			return modDisp.getText();
    		return null;
    	}
    }
        
    class PValuePanel extends JPanel {
    	JLabel cutOffLabel, chooseCutoffLabel;
        JTextField pValueInputField;
        JRadioButton pValueButton, fdrButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value / FDR parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
                       
            JPanel pValSelectionPanel = new JPanel();
            pValSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Cutoff Value", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout pValSelectionPanelgrid = new GridBagLayout();
            pValSelectionPanel.setLayout(pValSelectionPanelgrid);
            

            pValueButton = new JRadioButton("P-Value", false);
            pValueButton.setFocusPainted(false);
            pValSelectionPanel.add(pValueButton);
            
            fdrButton = new JRadioButton("FDR", true);
            fdrButton.setFocusPainted(false);
            pValSelectionPanel.add(fdrButton);
                        
            pValueInputField = new JTextField("0.05", 7);
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
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(pValueButton);
            chooseCorrection.add(fdrButton);
        }
        
        protected void reset() {
            pValueInputField.setText("0.05");
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	
                try {               
                   
                    int[] inGroupAssignments;
                    if (getTestDesign()==DESEQInitBox.ONE_CLASS){
	                   
                    } else if(getTestDesign()==DESEQInitBox.TWO_CLASS){
                    	if (getSelectionDesign()==DESEQInitBox.CLUSTER_SELECTION){
	                    	inGroupAssignments=getClusterTwoClassAssignments();
	                    }else{
	                    	inGroupAssignments=getTwoClassAssignments();
	                    }
	                    int grpA=0;
	                    int grpB=0;
	                    for (int i=0; i<inGroupAssignments.length; i++){
	                    	if (inGroupAssignments[i]==1)
	                    		grpA++;
	                    	if (inGroupAssignments[i]==2)
	                    		grpB++;
	                    }    
                    	if (grpA<2||grpB<2){
                    		JOptionPane.showMessageDialog(null, "At least 2 samples must be assigned to each group.", "Error!", JOptionPane.ERROR_MESSAGE);
                    		okPressed = false;
                    		return;
                    	}else {
                            okPressed = true;
                            dispose();
                        }
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
            	HelpWindow.launchBrowser(DESEQInitBox.this, "DESeq Initialization Dialog");
            }
        }
    }
    
    public int[] getClusterTwoClassAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.twoClassClusterSelector.getGroupSamples("Samples "+j);
    		
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
    
    public int[] getTwoClassAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
        	for (int j = 0; j < mPanel.twoClassPanel.numberGroups; j++) {
	            if (mPanel.twoClassPanel.rbArray[i][j].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
	                groupAssignments[i] = j+1;
	                break;
            	}
	            groupAssignments[i] = 0;
            }
        }
        return groupAssignments;
    }
    
    public int getTestDesign() {
        int design = -1;
        //if (mPanel.chooseDesignPane.getSelectedIndex() == 0) {
        	//design = DESEQInitBox.ONE_CLASS;
        //} else if (mPanel.chooseDesignPane.getSelectedIndex() == 1) {
        	design = DESEQInitBox.TWO_CLASS;
        //} else if (mPanel.chooseDesignPane.getSelectedIndex() == 2) {
        	//design = DESEQInitBox.PAIRED;
        //}
        return design;
    }
    
    public int getSelectionDesign() {
        int design = -1;
        if (getTestDesign()==RPInitBox.ONE_CLASS){
	        
        }else if(getTestDesign()==RPInitBox.TWO_CLASS){
        	if (mPanel.twoClassmulg.getSelectedIndex() == 0) {
	        	design = RPInitBox.BUTTON_SELECTION;
	        } else {
	        	design = RPInitBox.CLUSTER_SELECTION;
	        }
        }
        return design;
    }
    
    public float getPValue() {
        return Float.parseFloat(pPanel.pValueInputField.getText());
    }
    
    public String getCutOffField(){
    	if (pPanel.fdrButton.isSelected())
    		return "fdr";
    	else
    		return "pvalue";
    }
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 23; i++) {
            dummyVect.add("Expt " + (i+1));
        }
        dummyVect.add("Exptsdfsdfsgwegsgsgsd");
        
        DESEQInitBox oBox = new DESEQInitBox(dummyFrame, true, dummyVect, null);
        oBox.setVisible(true);
        System.out.println("end");
        System.exit(0);
        
    }
}
