/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RPInitBox.java,v $
 * $Revision: 1.10 $
 * $Date: 2008-11-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.rp;

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
import javax.swing.JTabbedPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class RPInitBox extends AlgorithmDialog {

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
    Vector<String> exptNames;    
    MultiClassPanel mPanel;
    JTabbedPane selectionPanel;
    //PermOrFDistPanel permPanel;
    PValuePanel pPanel;
    UpDownPanel upDownPanel;
    //HCLSelectionPanel hclOpsPanel;    
    HCLSigOnlyPanel hclOpsPanel;
    ClusterRepository repository;
    JButton step2Button = new JButton("Continue...");
    
    /** Creates new RPInitBox */
    public RPInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, ClusterRepository repository) {
        super(parentFrame, "RP Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = repository;
        setBounds(0, 0, 600, 850);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        this.okButton.setEnabled(true);
          
        mPanel = new MultiClassPanel();


        
        JTabbedPane consolidatedPane = new JTabbedPane();
        //permPanel = new PermOrFDistPanel();
        //buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        //gridbag.setConstraints(permPanel, constraints);
        //consolidatedPane.add("Permutations or F-Distribution", permPanel);        

        pPanel = new PValuePanel();
        //buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
        //gridbag.setConstraints(pPanel, constraints);
        consolidatedPane.add("P-Value/False Discovery Parameters", pPanel);
        
        upDownPanel = new UpDownPanel();
        consolidatedPane.add("Targeted Genes", upDownPanel);
        
        
        hclOpsPanel = new HCLSigOnlyPanel();
        //buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
        //gridbag.setConstraints(hclOpsPanel, constraints);
        consolidatedPane.add("Hierarchical Clusters", hclOpsPanel);  
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(consolidatedPane, constraints);
        pane.add(consolidatedPane);   
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(hclOpsPanel, constraints);
        //mPanel.add(hclOpsPanel, constraints);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  
        //pack();
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
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }   
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    
    
    class MultiClassPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        MultiGroupExperimentsPanel mulgPanel;
        JTabbedPane tabbedmulg;
        ClusterSelector clusterSelectorCondition;
        //ClusterSelector clusterSelectorTime;
        //int numTimePoints;
        float alpha;
        //Vector exptNames;
        
        public MultiClassPanel(/*Vector exptNames*/) {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            //this.exptNames = exptNames;
            this.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
                okReady = true;
                try {
                    mulgPanel = new MultiGroupExperimentsPanel(exptNames, 1);
                    
                    
                    tabbedmulg = new JTabbedPane();
                    
                    clusterSelectorCondition= new ClusterSelector(repository,1, "Samples");
                    if (repository!=null){
                    	clusterSelectorCondition.setClusterType("Experiment");
            		}
                    JPanel clusterSelectorPanel = new JPanel();
                    clusterSelectorPanel.setLayout(new GridBagLayout());

                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weighty =1;
                    c.weightx = 1;
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.anchor = GridBagConstraints.PAGE_END;
                    clusterSelectorPanel.add(clusterSelectorCondition, c);
                    c.gridx = 1;
                    //clusterSelectorPanel.add(clusterSelectorTime, c);
                    
                    
                    
                    
                    tabbedmulg.add("Button Selection", mulgPanel);
                    tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
                    tabbedmulg.setSelectedIndex(0);//set to always be button selection
                    if (repository==null||repository.isEmpty())
                    	tabbedmulg.setSelectedIndex(0);
                    buildConstraints(constraints, 1, 0, 1, 3, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(tabbedmulg, constraints);
                    MultiClassPanel.this.add(tabbedmulg);
                    MultiClassPanel.this.validate();
                
            
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please enter a value greater than 0 and less than 1!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
        

        
        class MultiGroupExperimentsPanel extends JPanel {
            int numPanels = 0;
            JLabel[] expLabels;
            JCheckBox[] exptCheckBoxes;
            MultiGroupExperimentsPanel(Vector<String> exptNames, int numTimePoints) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Experiment Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

               // JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                exptCheckBoxes = new JCheckBox[exptNames.size()];
                	
                numPanels = exptNames.size()/512 + 1;
                
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);
//                panel1.setLayout(gridbag);
  

                JPanel [] panels = new JPanel[numPanels];
                
                int currPanel = 0;
                for(int i = 0; i < panels.length; i++) {
                    panels[i] = new JPanel(gridbag);
                }
                
                for (int i = 0; i < exptNames.size(); i++) {
                    String s1 = (String)(exptNames.get(i));
                    expLabels[i] = new JLabel(s1);
                    for (int j = 0; j < numTimePoints; j++) {
                        exptCheckBoxes[i] = new JCheckBox("",true);
                    }
                    
                    
                    
                    
	                    	
                    
                    
                    
                    
                    buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptCheckBoxes[i], constraints);
                        panels[currPanel].add(exptCheckBoxes[i]);
                        // panel1.add(exptGroupRadioButtons[j][i]);
                    
                              
                    
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
                //exptNameHeaderPanel.HEIGHT = panel1.getHeight();
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    exptNameHeaderPanels[i] = new JPanel();
                    exptNameHeaderPanels[i].setSize(50, panels[i].getPreferredSize().height);
                    exptNameHeaderPanels[i].setPreferredSize(new Dimension(maxLabelWidth + 10, panels[i].getPreferredSize().height));
                    exptNameHeaderPanels[i].setLayout(exptHeaderGridbag);
                }
                //scroll.getRowHeader().setLayout(exptHeaderGridbag);
                
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
                //
                
                
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
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(panel2, constraints);
                this.add(panel2);
                
            /*
            JButton gButton = new JButton("groupExpts");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(gButton, constraints);
            this.add(gButton);
             */
                
            }
            
            /**
             *  resets all group assignments
             */
            protected void reset(){
            	for (int i=0; i<mulgPanel.exptCheckBoxes.length; i++){
            		mulgPanel.exptCheckBoxes[i].setSelected(true);
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
        		JFileChooser fileChooser = new JFileChooser("./data");	
        		
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
        				pw.println("RP");
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
	        						if (!lineArray[1].equals("RP")){
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
	        					setStateBasedOnIndex(groupAssignments,groupNames);
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
        	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
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
        	
            
            
        }
        protected void reset(){
        	for (int i=0; i<mulgPanel.exptCheckBoxes.length; i++){
        		mulgPanel.exptCheckBoxes[i].setSelected(true);
        	}
        }
    }
    
    
    class UpDownPanel extends JPanel {
    	JRadioButton upButton, downButton, bothButton;
    	public UpDownPanel(){
    		this.setBorder(new TitledBorder(new EtchedBorder(), "Find regulated genes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
    		GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setBackground(Color.white);
            this.setLayout(gridbag);
            upButton = new JRadioButton("Up-Regulated", true);
            downButton = new JRadioButton("Down-Regulated", false);
            bothButton = new JRadioButton("Both", false);
            upButton.setBackground(Color.white);
            downButton.setBackground(Color.white);
            bothButton.setBackground(Color.white);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(upButton);
            buttonGroup.add(downButton);
            buttonGroup.add(bothButton);
            

            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(upButton, constraints);
            this.add(upButton);  

            buildConstraints(constraints, 1, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(downButton, constraints);
            this.add(downButton);  

            buildConstraints(constraints, 2, 0, 1, 1, 50, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(bothButton, constraints);
            this.add(bothButton);  
            
    	}
    	public int getUpDown(){
    		if (upButton.isSelected())
    			return 1;
    		if (downButton.isSelected())
    			return 2;
    		if (bothButton.isSelected())
    			return 3;
    		return 0;
    	}
    }
    class PValuePanel extends JPanel {
    	JLabel numPermsLabel;
    	JTextField timesField;
        JTextField pValueInputField, falseNumField, falsePropField;
        JRadioButton falseNumButton, falsePropButton;
        
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
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 0, 1, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(pValueLabel, constraints);
            this.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            pValueInputField.setMinimumSize(new Dimension(50, 20));
            buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(pValueInputField, constraints);
            this.add(pValueInputField);
            
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
            //stdBonfButton.setEnabled(false);
            //adjBonfButton.setEnabled(false);
            
            JPanel FDRPanel = new JPanel();
            FDRPanel.setBackground(Color.white);
            FDRPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout grid3 = new GridBagLayout(); 
            FDRPanel.setLayout(grid3);            
            
            JLabel FDRLabel = new JLabel("With confidence of [1 - alpha] : ");            
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 0, 2, 1, 100, 34);
            grid3.setConstraints(FDRLabel, constraints);
            FDRPanel.add(FDRLabel);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falseNumButton, constraints);
            FDRPanel.add(falseNumButton);       
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falseNumField, constraints);
            FDRPanel.add(falseNumField);    
            
            buildConstraints(constraints, 0, 2, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falsePropButton, constraints);
            FDRPanel.add(falsePropButton);    
            
            buildConstraints(constraints, 1, 2, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falsePropField, constraints);
            FDRPanel.add(falsePropField);    
            
            constraints.anchor = GridBagConstraints.CENTER;
           
            
            buildConstraints(constraints, 0, 3, 4, 1, 100, 50);
            gridbag.setConstraints(FDRPanel, constraints);
            this.add(FDRPanel);            
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
            	if ((getTestDesign()==RPInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                try {               
                    if (pPanel.falseNumButton.isSelected()) {
                        if (!validateFalseNum()) {
                            okPressed = false;
                            return;
                        }
                    }
                    if (pPanel.falsePropButton.isSelected()) {
                        if (!validateFalseProp()) {
                            okPressed = false;
                            return;
                        }
                    }                     
                    double d = Double.parseDouble(pPanel.pValueInputField.getText());
                    /*
                    if (usePerms()) {
                        int p = getNumPerms();
                    }
                     */
                    int[] inGroupAssignments;
                    if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
                    	inGroupAssignments=getClusterInGroupAssignments();
                    }else{
                    	inGroupAssignments=getInGroupAssignments();
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
                    if ((d <= 0d)||(d > 1d) || (usePerms() && (getNumPerms() <= 1))) {
                        JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);                            
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
                HelpWindow hw = new HelpWindow(RPInitBox.this, "Rank Products- Initialization Dialog");
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
    
    

    
    public int[] getInGroupAssignments() {
        int[] inGroupAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.exptCheckBoxes[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                inGroupAssignments[i] = 1;
            } else {
                inGroupAssignments[i] = 0;
            }
        }
        return inGroupAssignments;
    }  
   
    
    public int getTestDesign() {
        int design = -1;
        if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = RPInitBox.BUTTON_SELECTION;
        	} else {
        	design = RPInitBox.CLUSTER_SELECTION;
        }
        return design;
    }
    
    public int[] getClusterInGroupAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.clusterSelectorCondition.getGroupSamples("Samples "+j);
    		
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
    
    
//    public int getNumTimePoints() {
//        return mPanel.numTimePoints;
//    }
    
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
            JOptionPane.showMessageDialog(RPInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(RPInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(RPInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(RPInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
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
        dummyVect.add("Same");
        dummyVect.add("Same");
        dummyVect.add("Same");
        dummyVect.add("Same");
        dummyVect.add("Same");
        for (int i = 0; i < 95; i++) {
            dummyVect.add("Expt " + i);
        }
        
        RPInitBox oBox = new RPInitBox(dummyFrame, true, dummyVect, null);
        oBox.setVisible(true);
        
    }
}
