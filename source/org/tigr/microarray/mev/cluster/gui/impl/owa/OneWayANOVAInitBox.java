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
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.owa;

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
import javax.swing.JButton;
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
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class OneWayANOVAInitBox extends AlgorithmDialog {

    public static final int JUST_ALPHA = 1;
    public static final int STD_BONFERRONI = 2;
    public static final int ADJ_BONFERRONI = 3;
    public static final int MAX_T = 9; 
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;    
    public static final int BUTTON_SELECTION = 14;
    public static final int CLUSTER_SELECTION = 15;
    
    boolean okPressed = false;
    Vector exptNames;    
    MultiClassPanel mPanel;
    JTabbedPane selectionPanel;
    PermOrFDistPanel permPanel;
    PValuePanel pPanel;
    //HCLSelectionPanel hclOpsPanel;    
    HCLSigOnlyPanel hclOpsPanel;
    ClusterRepository repository;
    
    /** Creates new OneWayANOVAInitBox */
    public OneWayANOVAInitBox(JFrame parentFrame, boolean modality, Vector exptNames, ClusterRepository repository) {
        super(parentFrame, "One-way ANOVA Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = repository;
        setBounds(0, 0, 800, 850);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
          
        mPanel = new MultiClassPanel();

        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        JTabbedPane consolidatedPane = new JTabbedPane();
        permPanel = new PermOrFDistPanel();
        //buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        //gridbag.setConstraints(permPanel, constraints);
        consolidatedPane.add("Permutations of F-Distribution", permPanel);        

        pPanel = new PValuePanel();
        //buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
        //gridbag.setConstraints(pPanel, constraints);
        consolidatedPane.add("P-Value/False Discovery Parameters", pPanel);
        
        hclOpsPanel = new HCLSigOnlyPanel();
        //buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
        //gridbag.setConstraints(hclOpsPanel, constraints);
        consolidatedPane.add("Hierarchical Clusters", hclOpsPanel);  
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(consolidatedPane, constraints);
        pane.add(consolidatedPane);
        
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
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }   
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    
    
    class MultiClassPanel extends JPanel {
        NumGroupsPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        MultiGroupExperimentsPanel mulgPanel;
        JTabbedPane tabbedmulg;
        ClusterSelector clusterSelector;
        int numGroups;
        //Vector exptNames;
        
        public MultiClassPanel(/*Vector exptNames*/) {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            //this.exptNames = exptNames;
            this.setLayout(gridbag);
            ngPanel = new NumGroupsPanel();
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ngPanel, constraints);
            ngPanel.okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    ngPanel.okPressed = true;
                    try {
                        numGroups = Integer.parseInt(ngPanel.numGroupsField.getText());
                        if (numGroups <= 2) {
                            JOptionPane.showMessageDialog(null, "Please enter a positive integer > 2!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            mulgPanel = new MultiGroupExperimentsPanel(exptNames, numGroups);
                            
                            
                            //JButton dummyButton  = new JButton("dummyButton");
                            //dummyButton.setVisible(true);
                            MultiClassPanel.this.remove(dummyPanel);
                            tabbedmulg = new JTabbedPane();
                            clusterSelector = new ClusterSelector(repository, numGroups);
                            tabbedmulg.add("Button Selection", mulgPanel);
                            tabbedmulg.add("Cluster Selection", clusterSelector);
                            tabbedmulg.setSelectedIndex(1);
                            if (repository==null||repository.isEmpty())
                            	tabbedmulg.setSelectedIndex(0);
                            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
                            constraints.fill = GridBagConstraints.BOTH;
                            gridbag.setConstraints(tabbedmulg, constraints);
                            MultiClassPanel.this.add(tabbedmulg);
                            //MultiClassPanel.this.add(dummyButton);
                            MultiClassPanel.this.validate();
                            ngPanel.okButton.setEnabled(false);
                            ngPanel.numGroupsField.setEnabled(false);
                            
                        }
                        //MultiClassPanel.this.repaint();
                        //dispose();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please enter a positive integer > 2!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            this.add(ngPanel);
            
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
        }
        
        
        class NumGroupsPanel extends JPanel {
            JTextField numGroupsField;
            JButton okButton;
            boolean okPressed = false;
            
            public NumGroupsPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                //JPanel pane = new JPanel();
                this.setLayout(gridbag);
                
                JLabel numGroupsLabel = new JLabel("Number of groups ");
                buildConstraints(constraints, 0, 0, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numGroupsLabel, constraints);
                this.add(numGroupsLabel);
                
                numGroupsField = new JTextField("", 7);
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
                gridbag.setConstraints(numGroupsField, constraints);
                this.add(numGroupsField);
                
                okButton = new JButton("OK");
                buildConstraints(constraints, 2, 0, 1, 1, 40, 0);
                gridbag.setConstraints(okButton, constraints);
 
                this.add(okButton);
                
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
        
        class MultiGroupExperimentsPanel extends JPanel {
            int numPanels = 0;
            JLabel[] expLabels;
            JRadioButton[][] exptGroupRadioButtons;
            JRadioButton[] notInGroupRadioButtons;
            MultiGroupExperimentsPanel(Vector exptNames, int numGroups) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

               // JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                exptGroupRadioButtons = new JRadioButton[numGroups][exptNames.size()];
                numPanels = exptNames.size()/512 + 1;
                
                //groupARadioButtons = new JRadioButton[exptNames.size()];
                //groupBRadioButtons = new JRadioButton[exptNames.size()];
                notInGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
                
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
                    chooseGroup[i] = new ButtonGroup();
                    for (int j = 0; j < numGroups; j++) {
                        exptGroupRadioButtons[j][i] = new JRadioButton("Group " + (j + 1) + "     ", j == 0? true: false);
                        chooseGroup[i].add(exptGroupRadioButtons[j][i]);
                    }
                    
                    //set current panel
                    currPanel = i / 512;
                    
                    notInGroupRadioButtons[i] = new JRadioButton("Not in groups", false);
                    chooseGroup[i].add(notInGroupRadioButtons[i]);
                    
                
                    for (int j = 0; j < numGroups; j++) {
                        buildConstraints(constraints, j, i%512, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptGroupRadioButtons[j][i], constraints);
                        panels[currPanel].add(exptGroupRadioButtons[j][i]);
                        // panel1.add(exptGroupRadioButtons[j][i]);
                    }
                    
                    buildConstraints(constraints, (numGroups + 1), i%512, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(notInGroupRadioButtons[i], constraints);
                    
                    
                    //panel1.add(notInGroupRadioButtons[i]);
                    panels[currPanel].add(notInGroupRadioButtons[i]);                    
                    
                    
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
                
                JLabel label1 = new JLabel("Note: Each group MUST each contain more than one sample.");
                label1.setHorizontalAlignment(JLabel.CENTER);
                buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
                constraints.anchor = GridBagConstraints.EAST;
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(label1, constraints);
                this.add(label1);
                
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
                            exptGroupRadioButtons[0][i].setSelected(true);
                        }
                    }
                });
                
                final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
                
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
        	 * Saves the assignments to file.
        	 * 
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
        				pw.println("ANOVA");
        				for (int i=0; i<numGroups; i++){
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
	        						if (!lineArray[1].equals("ANOVA")){
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
	        			Vector<String> currSampleVector = new Vector<String>();
	        			for(int i = 0; i < exptNames.size(); i++)
	        				currSampleVector.add((String)exptNames.get(i));
	        			
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
	        				try{
	        					exptGroupRadioButtons[groupIndex][sample].setSelected(true);
	        				}catch (Exception e){
	        					notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
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
        	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
        		Object[] optionst = { "Continue", "Cancel" };
        		if (JOptionPane.showOptionDialog(null, 
						"The saved file was saved using a different sample annotation. \n Would you like MeV to try to load it by index order?", 
						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
						optionst, optionst[0])==1)
					return;
				
        		for(int sample = 0; sample < exptNames.size(); sample++) {
        			try{
        				exptGroupRadioButtons[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
        			}catch(Exception e){
    					notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
        			}
        		}
        	}
            /**
             *  resets group assignments
             */
            protected void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                    exptGroupRadioButtons[0][i].setSelected(true);
                }
            }
        }
        protected void reset(){
            mulgPanel.reset();
        }
    }
    
    class PermOrFDistPanel extends JPanel {
        JRadioButton tDistButton, permutButton; // randomGroupsButton, allCombsButton;
        JLabel numPermsLabel;
        JTextField timesField;//, alphaInputField;
        //JButton permParamButton;
        
        PermOrFDistPanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Permutations or F-distribution", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            this.setLayout(gridbag);
            
            //permParamButton = new JButton("Permutation parameters");
            //permParamButton.setEnabled(false);
            
            ButtonGroup chooseP = new ButtonGroup();
            
            tDistButton = new JRadioButton("p-values based on F-distribution", true);
            tDistButton.setFocusPainted(false);
            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
            tDistButton.setBackground(Color.white);
            
            numPermsLabel = new JLabel("Enter number of permutations");
            numPermsLabel.setEnabled(false);
            
            timesField = new JTextField("1000", 7);
            timesField.setEnabled(false);
            timesField.setBackground(Color.darkGray);
            
            tDistButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(false);
                    timesField.setEnabled(false);
                    timesField.setBackground(Color.darkGray); 
                    if (pPanel.maxTButton.isSelected() || pPanel.falseNumButton.isSelected() || pPanel.falsePropButton.isSelected()) {
                        pPanel.justAlphaButton.setSelected(true);
                    }
                    pPanel.maxTButton.setEnabled(false);                    
                    pPanel.falseNumButton.setEnabled(false);
                    pPanel.falsePropButton.setEnabled(false);
                    pPanel.falseNumField.setEnabled(false);
                    pPanel.falsePropField.setEnabled(false);                    
                    //pAdjPanel.minPButton.setEnabled(false);
                }
            });
            
            chooseP.add(tDistButton);
            
            permutButton = new JRadioButton("p-values based on permutation:  ", false);
            permutButton.setFocusPainted(false);
            permutButton.setForeground(UIManager.getColor("Label.foreground"));
            permutButton.setBackground(Color.white);
            
            permutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(true);
                    timesField.setEnabled(true);
                    timesField.setBackground(Color.white);                  
                    pPanel.maxTButton.setEnabled(true);  //UNCOMMENT THIS WHEN MAXT METHOD HAS BEEN IMPLEMEMTED
                    //pAdjPanel.minPButton.setEnabled(true);  //UNCOMMENT THIS WHEN MINP METHOD HAS BEEN DEBUGGED    
                    pPanel.falseNumButton.setEnabled(true);
                    pPanel.falsePropButton.setEnabled(true);
                    pPanel.falseNumField.setEnabled(true);
                    pPanel.falsePropField.setEnabled(true);
                }                
            });
            
            chooseP.add(permutButton);
            
            
            //constraints.anchor = GridBagConstraints.CENTER;
            
            //numCombsLabel = new JLabel("                                       ");
            //numCombsLabel.setOpaque(false);
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
            gridbag.setConstraints(numCombsLabel, constraints);
            this.add(numCombsLabel);
             */
            
            
            buildConstraints(constraints, 0, 0, 3, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(permutButton, constraints);
            this.add(permutButton);
            
            //JLabel numPermsLabel = new JLabel("Enter number of permutations");
            //numPermsLabel.setEnabled(false);
            buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            buildConstraints(constraints, 2, 1, 1, 1, 40, 0);
            gridbag.setConstraints(timesField, constraints);
            this.add(timesField);
            /*
            JLabel alphaLabel = new JLabel("Enter critical p-value");
            buildConstraints(constraints, 0, 2, 2, 1, 60, 40);
            //constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(alphaLabel, constraints);
            this.add(alphaLabel);
            
            alphaInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 2, 1, 1, 40, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(alphaInputField, constraints);
            this.add(alphaInputField);
             */
        }
    }
    
    
    class PValuePanel extends JPanel {
        JTextField pValueInputField, falseNumField, falsePropField;
        JRadioButton justAlphaButton, stdBonfButton, adjBonfButton, maxTButton, falseNumButton, falsePropButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value / false discovery parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            this.setBackground(Color.white);
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 0, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(pValueLabel, constraints);
            this.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(pValueInputField, constraints);
            this.add(pValueInputField);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            justAlphaButton = new JRadioButton("Just alpha (no correction)", true);
            justAlphaButton.setBackground(Color.white);
            //justAlphaButton.setVisible(false);
            stdBonfButton = new JRadioButton("Standard Bonferroni", false);
            stdBonfButton.setBackground(Color.white);
            //stdBonfButton.setVisible(false); // WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            adjBonfButton = new JRadioButton("Adjusted Bonferroni", false);
            adjBonfButton.setBackground(Color.white);
            //adjBonfButton.setVisible(false);// WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            maxTButton = new JRadioButton("Westfall-Young step-down maxT", false);
            maxTButton.setBackground(Color.white);
            maxTButton.setEnabled(false);
            
            falseNumButton = new JRadioButton("EITHER, The number of false significant genes should not exceed", false);
            falseNumButton.setEnabled(false);
            //falseNumButton.setEnabled(false);
            falseNumButton.setFocusPainted(false);
            falseNumButton.setForeground(UIManager.getColor("Label.foreground"));
            falseNumButton.setBackground(Color.white);
            //sigGroup.add(falseNumButton);            
            
            falsePropButton = new JRadioButton("OR, The proportion of false significant genes should not exceed", false);
            falsePropButton.setEnabled(false);
            //falsePropButton.setEnabled(false);
            falsePropButton.setFocusPainted(false);
            falsePropButton.setForeground(UIManager.getColor("Label.foreground"));
            falsePropButton.setBackground(Color.white);
            //sigGroup.add(falsePropButton);            
            
            falseNumField = new JTextField(10);
            falseNumField.setText("10");
            falseNumField.setEnabled(false);
            falsePropField = new JTextField(10);
            falsePropField.setText("0.05");
            falsePropField.setEnabled(false);            
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(justAlphaButton);
            chooseCorrection.add(stdBonfButton);
            chooseCorrection.add(adjBonfButton);
            chooseCorrection.add(maxTButton);
            chooseCorrection.add(falseNumButton);
            chooseCorrection.add(falsePropButton);
            //stdBonfButton.setEnabled(false);
            //adjBonfButton.setEnabled(false);
            
            JPanel FDRPanel = new JPanel();
            FDRPanel.setBackground(Color.white);
            FDRPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control (permutations only)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
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
            
           
            
            buildConstraints(constraints, 0, 1, 1, 1, 25, 25);
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
 
            buildConstraints(constraints, 1, 1, 1, 1, 25, 0);
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 25, 0);
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);     
            
            buildConstraints(constraints, 3, 1, 1, 1, 25, 0);
            gridbag.setConstraints(maxTButton, constraints);
            this.add(maxTButton); 
            
            buildConstraints(constraints, 0, 2, 4, 1, 100, 50);
            gridbag.setConstraints(FDRPanel, constraints);
            this.add(FDRPanel);            
        }
        
        protected void reset() {
            pValueInputField.setText("0.01");
            justAlphaButton.setSelected(true);
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if ((getTestDesign()==OneWayANOVAInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                boolean tooFew = false;
                int[] grpAssignments=getGroupAssignments();
                if (getTestDesign()==OneWayANOVAInitBox.CLUSTER_SELECTION){
                	grpAssignments=getClusterGroupAssignments();
                }
                if (grpAssignments==null)
                	return;
                int numGroups = getNumGroups();
                int[] groupSize = new int[numGroups];
                
                for (int i = 0; i < groupSize.length; i++) {
                    groupSize[i] = 0;
                }
                
                for (int i = 0; i < grpAssignments.length; i++) {
                    int currentGroup = grpAssignments[i];
                    if (currentGroup != 0) {
                        groupSize[currentGroup - 1]++;
                    }
                }
                
                for (int i = 0; i < groupSize.length; i++) {
                    if (groupSize[i] <= 1) {
                    	JOptionPane.showMessageDialog(null, "Each group must contain more than one sample.", "Error", JOptionPane.WARNING_MESSAGE);
                        tooFew = true;
                        break;
                    }
                }
                
                if (!tooFew) {
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
                        //HERE, CHECK OTHER INPUTS: P-VALUE VALIDITY - 4/25/03
                        double d = Double.parseDouble(pPanel.pValueInputField.getText());
                        /*
                        if (usePerms()) {
                            int p = getNumPerms();
                        }
                         */
                        if ((d <= 0d)||(d > 1d) || (usePerms() && (getNumPerms() <= 1))) {
                            JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);                            
                        } else {
                            okPressed = true;
                            dispose();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);
                    }

                }
            } else if (command.equals("reset-command")) {
                mPanel.reset();
                pPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(OneWayANOVAInitBox.this, "One Way ANOVA Initialization Dialog");
                okPressed = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
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
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.notInGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                groupAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.mulgPanel.exptGroupRadioButtons.length; j++) {
                    if (mPanel.mulgPanel.exptGroupRadioButtons[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        
        
        return groupAssignments;
    }   
    public int getTestDesign() {
        int design = -1;
        if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = OneWayANOVAInitBox.BUTTON_SELECTION;
        	} else {
        	design = OneWayANOVAInitBox.CLUSTER_SELECTION;
        }
        return design;
    }

    public int[] getClusterGroupAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
    	for (int i=0; i<mPanel.numGroups; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.clusterSelector.getGroupSamples("Group "+j);
    		
    	}
    	for (int i=0; i<arraylistArray[0].size();i++){
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
    
    public int getNumGroups() {
        return mPanel.numGroups;
    }
    
    public boolean usePerms() {
        return this.permPanel.permutButton.isSelected();
    }
    
    public int getNumPerms() {
        return Integer.parseInt(this.permPanel.timesField.getText());
    }
    
    public double getPValue() {
        return Double.parseDouble(pPanel.pValueInputField.getText());
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
            JOptionPane.showMessageDialog(OneWayANOVAInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(OneWayANOVAInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(OneWayANOVAInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(OneWayANOVAInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;          
        }
        return true;
    }       

    public int getCorrectionMethod() {
        int method = JUST_ALPHA;
        if (pPanel.justAlphaButton.isSelected()) {
            method = JUST_ALPHA;
        } else if (pPanel.stdBonfButton.isSelected()) {
            method = STD_BONFERRONI;
        } else if (pPanel.adjBonfButton.isSelected()) {
            method = ADJ_BONFERRONI;
        } else if (pPanel.maxTButton.isSelected()) {
            method = MAX_T;
        } else if (pPanel.falseNumButton.isSelected()) {
            method = FALSE_NUM;
        } else if (pPanel.falsePropButton.isSelected()) {
            method = FALSE_PROP;
        }
        
        return method;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector dummyVect = new Vector();
        for (int i = 0; i < 95; i++) {
            dummyVect.add("Expt " + i);
        }
        
        OneWayANOVAInitBox oBox = new OneWayANOVAInitBox(dummyFrame, true, dummyVect, null);
        oBox.setVisible(true);
        
    }
}
