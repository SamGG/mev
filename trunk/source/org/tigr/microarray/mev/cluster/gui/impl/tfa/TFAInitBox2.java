/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TFAInitBox2.java
 *
 * Created on February 12, 2004, 4:15 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.util.StringSplitter;


/**
 *
 * @author  nbhagaba
 */
public class TFAInitBox2 extends AlgorithmDialog {
    
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;    
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    
    boolean okPressed = false;
    protected boolean oneSamplePerCell = false;
    Vector exptNames;
    String[] factorNames;
    int[] numFactorLevels;
    GroupExptsPanel gPanel;
    PValuePanel pPanel;
    PValueAdjustmentPanel pAdjPanel;
    HCLSigOnlyPanel hclOpsPanel;    
    JTabbedPane tabbedSelectors;
    ClusterSelector clusterSelectorA;
    ClusterSelector clusterSelectorB;
    ClusterRepository repository;
    //HCLSelectionPanel hclOpsPanel;
    /** Creates a new instance of TFAInitBox2 */
    public TFAInitBox2(JFrame parentFrame, boolean modality, Vector exptNames, String[] factorNames, int[] numFactorLevels, ClusterRepository repository) {
        super(parentFrame, "Two-factor ANOVA Initialization", modality);
        this.exptNames = exptNames;
        this.factorNames = factorNames;
        this.numFactorLevels = numFactorLevels;
        this.repository = repository;
        
        setBounds(0, 0, 1000, 720);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagConstraints c = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        gPanel = new GroupExptsPanel();
        clusterSelectorA= new ClusterSelector(repository,numFactorLevels[0]);
        clusterSelectorB= new ClusterSelector(repository,numFactorLevels[1]);
        if (repository!=null){
        	clusterSelectorA.setClusterType(factorNames[0]);
        	clusterSelectorB.setClusterType(factorNames[1]);
		}
        JPanel clusterSelectorPanel = new JPanel();
        clusterSelectorPanel.setLayout(new GridBagLayout());
        
        c.fill = GridBagConstraints.BOTH;
        c.weighty =1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        clusterSelectorPanel.add(clusterSelectorA, c);
        c.gridx = 1;
        clusterSelectorPanel.add(clusterSelectorB, c);
        
        tabbedSelectors = new JTabbedPane();
        tabbedSelectors.add("Button Selection",gPanel);        
        //buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        //gridbag.setConstraints(clusterSelectorPanel, constraints);
        //constraints.anchor = GridBagConstraints.PAGE_END;
        tabbedSelectors.add("Cluster Selection",clusterSelectorPanel);
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(tabbedSelectors, constraints);
        pane.add(tabbedSelectors);
        
        pPanel = new PValuePanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 8);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        
        pAdjPanel = new PValueAdjustmentPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 8);
        gridbag.setConstraints(pAdjPanel, constraints);
        pane.add(pAdjPanel);
        
        hclOpsPanel = new HCLSigOnlyPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 4);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    class GroupExptsPanel extends JPanel {
        MultiGroupExperimentsPanel factorAPanel, factorBPanel;
        JPanel panel2;
        GroupExptsPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            factorAPanel = new MultiGroupExperimentsPanel(factorNames[0], numFactorLevels[0]);
            factorBPanel = new MultiGroupExperimentsPanel(factorNames[1], numFactorLevels[1]);
            
            GridBagLayout grid1 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(grid1);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 90);
            constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(factorAPanel, constraints);
            this.add(factorAPanel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(factorBPanel, constraints);
            this.add(factorBPanel);
            
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
            
            
            //final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    factorAPanel.reset();
                    factorBPanel.reset();
                }
            });
            
            final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	saveAssignments();
                }
            });
            
            
            
            
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
            
            buildConstraints(constraints, 0, 1, 2, 1, 100, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            //constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(panel2, constraints);
            this.add(panel2);
            
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
    				pw.println("2-Factor ANOVA");
    				for (int i=0; i<Math.max(numFactorLevels[0],numFactorLevels[1]); i++){
        				pw.print("Group "+(i+1)+" Label:\t");
    					pw.println("Group "+(i+1));
    				}
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");

    				int[]groupAAssgn=getFactorAAssignments();
    				int[]groupBAssgn=getFactorBAssignments();
    				
    				for(int sample = 0; sample < exptNames.size(); sample++) {
    					pw.print(String.valueOf(sample+1)+"\t"); //sample index
    					pw.print(exptNames.get(sample)+"\t");
    					if (groupAAssgn[sample]!=0)
    						pw.print("Group "+(groupAAssgn[sample]));
    					else
    						pw.print("Exclude");
    					if (groupBAssgn[sample]!=0)
    						pw.println("\tGroup "+(groupBAssgn[sample]));
    					else
    						pw.println("\tExclude");
    					
    					
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
	       			Vector<String> groupAAssignments = new Vector<String>();	
	       			Vector<String> groupBAssignments = new Vector<String>();		
	       			
	       			//parse the data in to these structures
	       			String [] lineArray;
	       			//String status = "OK";
	       			boolean bothloaded = true;
	       			for(int row = 0; row < data.size(); row++) {
	       				line = (String)(data.get(row));
	
	       				//if not a comment line, and not the header line
	       				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
	       					
	       					lineArray = line.split("\t");
	       					
	       					//check what module saved the file
	       					if(lineArray[0].startsWith("Module:")) {
	       						if (!lineArray[1].equals("2-Factor ANOVA")){
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
	       					groupAAssignments.add(lineArray[2]);	
	       					try{
		       					groupBAssignments.add(lineArray[3]);
	       					}catch (Exception e){
	       						bothloaded=false;
	       					}
	       				}				
	       			}
	       			if (!bothloaded)
	       				JOptionPane.showMessageDialog(null, "The loaded file contained only 1 Factor.  Only "+factorNames[0]+" will be loaded.", "Error", JOptionPane.ERROR_MESSAGE);                            	       						
   					
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
	       			
	       			int fileSampleIndexA = 0;
	       			int groupIndexA = 0;
	       			int fileSampleIndexB = 0;
	       			int groupIndexB = 0;
	       			String groupAName;
	       			String groupBName;
	       			
	       			for(int sample = 0; sample < exptNames.size(); sample++) {

        				boolean doIndex = false;
        				for (int i=0;i<exptNames.size(); i++){
        					if (i==sample)
        						continue;
        					if (exptNames.get(i).equals(exptNames.get(sample))){
        						doIndex=true;
        					}
        				}
        				fileSampleIndexA = sampleNames.indexOf(exptNames.get(sample));
        				if (fileSampleIndexA==-1){
        					doIndex=true;
        				}
        				if (bothloaded){
		       				fileSampleIndexB = sampleNames.indexOf(exptNames.get(sample));
		       				if (fileSampleIndexB==-1){
		       					doIndex=true;
		       					break;
		       				}
        				}
        				if (doIndex){
        					setStateBasedOnIndex(groupAAssignments,groupBAssignments,groupNames,bothloaded);
        					break;
        				}
        				
	       				groupAName = (String)(groupAAssignments.get(fileSampleIndexA));
	       				groupIndexA = groupNames.indexOf(groupAName);
	       				
	       				//set state
	       				try{
	       					gPanel.factorAPanel.exptGroupRadioButtons[groupIndexA][sample].setSelected(true);
	       				}catch (Exception e){
	       					gPanel.factorAPanel.notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
	       				}
	       				
	       				if (bothloaded){
		       				
		       				groupBName = (String)(groupBAssignments.get(fileSampleIndexB));
		       				groupIndexB = groupNames.indexOf(groupBName);
		       				
		       				//set state
		       				try{
		       					gPanel.factorBPanel.exptGroupRadioButtons[groupIndexB][sample].setSelected(true);
		       				}catch (Exception e){
		       					gPanel.factorBPanel.notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
		       				}
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
	   	private void setStateBasedOnIndex(Vector<String>groupAAssignments,Vector<String>groupBAssignments,Vector<String>groupNames, boolean bothloaded){
	   		Object[] optionst = { "Continue", "Cancel" };
	   		if (JOptionPane.showOptionDialog(null, 
						"The saved file was saved using a different sample annotation. \n Would you like MeV to try to load it by index order?", 
						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
						optionst, optionst[0])==1)
					return;
				
	   		for(int sample = 0; sample < exptNames.size(); sample++) {
	   			try{
	   				gPanel.factorAPanel.exptGroupRadioButtons[groupNames.indexOf(groupAAssignments.get(sample))][sample].setSelected(true);
	   			}catch(Exception e){
	   				gPanel.factorAPanel.notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
	   			}
	   		}
	   		if (bothloaded){
		   		for(int sample = 0; sample < exptNames.size(); sample++) {
		   			try{
		   				gPanel.factorBPanel.exptGroupRadioButtons[groupNames.indexOf(groupBAssignments.get(sample))][sample].setSelected(true);
		   			}catch(Exception e){
		   				gPanel.factorBPanel.notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
		   			}
		   		}
	   		}
	   	}
        public void reset() {
            factorAPanel.reset();
            factorBPanel.reset();
        }
    }

    
    class MultiGroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JRadioButton[][] exptGroupRadioButtons;
        JRadioButton[] notInGroupRadioButtons;
        int numGroups;
        
        MultiGroupExperimentsPanel(String factorName, int numGroups) {
            this.setBorder(new TitledBorder(new EtchedBorder(), factorName + " assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.numGroups = numGroups;
            setBackground(Color.white);
            JPanel panel1 = new JPanel();
            expLabels = new JLabel[exptNames.size()];
            exptGroupRadioButtons = new JRadioButton[numGroups][exptNames.size()];
            //groupARadioButtons = new JRadioButton[exptNames.size()];
            //groupBRadioButtons = new JRadioButton[exptNames.size()];
            notInGroupRadioButtons = new JRadioButton[exptNames.size()];
            ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            panel1.setLayout(gridbag);
            
            for (int i = 0; i < exptNames.size(); i++) {
                String s1 = (String)(exptNames.get(i));
                expLabels[i] = new JLabel(s1);
                chooseGroup[i] = new ButtonGroup();
                for (int j = 0; j < numGroups; j++) {
                    exptGroupRadioButtons[j][i] = new JRadioButton("Group " + (j + 1) + "     ", j == 0? true: false);
                    chooseGroup[i].add(exptGroupRadioButtons[j][i]);
                }
                
                notInGroupRadioButtons[i] = new JRadioButton("Not in groups", false);
                chooseGroup[i].add(notInGroupRadioButtons[i]);
                
                for (int j = 0; j < numGroups; j++) {
                    buildConstraints(constraints, j, i, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(exptGroupRadioButtons[j][i], constraints);
                    panel1.add(exptGroupRadioButtons[j][i]);
                }
                
                buildConstraints(constraints, (numGroups + 1), i, 1, 1, 100, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(notInGroupRadioButtons[i], constraints);
                panel1.add(notInGroupRadioButtons[i]);
            }
            
            int maxLabelWidth = 0;
            
            for (int i = 0; i < expLabels.length; i++) {
                if (expLabels[i].getPreferredSize().getWidth() > maxLabelWidth) {
                    maxLabelWidth = (int)Math.ceil(expLabels[i].getPreferredSize().getWidth());
                }
            }
            
            JScrollPane scroll = new JScrollPane(panel1);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            JPanel exptNameHeaderPanel = new JPanel();
            GridBagLayout exptHeaderGridbag = new GridBagLayout();
            //exptNameHeaderPanel.HEIGHT = panel1.getHeight();
            //System.out.println("panel1.preferredSise().height = " + panel1.getPreferredSize().height);
            exptNameHeaderPanel.setSize(50, panel1.getPreferredSize().height);
            exptNameHeaderPanel.setPreferredSize(new Dimension(maxLabelWidth + 10, panel1.getPreferredSize().height));
            exptNameHeaderPanel.setLayout(exptHeaderGridbag);
            //scroll.getRowHeader().setLayout(exptHeaderGridbag);
            
            
            for (int i = 0; i < expLabels.length; i++) {
                buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                constraints.fill = GridBagConstraints.BOTH;
                exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                exptNameHeaderPanel.add(expLabels[i]);
            }
            
            scroll.setRowHeaderView(exptNameHeaderPanel);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
        }
        
        public void reset() {
            for (int i = 0; i < exptNames.size(); i++) {
                exptGroupRadioButtons[0][i].setSelected(true);
            }
        }
        
        
        public int[] getGroupAssignments() {
            int[] groupAssignments = new int[exptNames.size()];
            
            for (int i = 0; i < groupAssignments.length; i++) {
                groupAssignments[i] = 0;
            }
            
            for (int i = 0; i < groupAssignments.length; i++) {
                for (int j = 0; j < numGroups; j++) {
                    if (exptGroupRadioButtons[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
            
            return groupAssignments;
        }
        
        public void setGroupAssignments(int[] assignments) {
            for (int i = 0; i < assignments.length; i++) {
                if (assignments[i] == 0) {
                    notInGroupRadioButtons[i].setSelected(true);
                } else {
                    exptGroupRadioButtons[assignments[i] - 1][i].setSelected(true);
                }
            }
        }
        
    }
    
    class PValuePanel extends JPanel {
        JRadioButton tDistButton, permutButton; // randomGroupsButton, allCombsButton;
        JLabel numPermsLabel;
        JTextField timesField, alphaInputField;
        //JButton permParamButton;
        
        PValuePanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "P-Value Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
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
                    pAdjPanel.maxTButton.setEnabled(false);
                    pAdjPanel.minPButton.setEnabled(false);
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
                    //pAdjPanel.maxTButton.setEnabled(true);  //UNCOMMENT THIS WHEN MAXT METHOD HAS BEEN IMPLEMEMTED
                    //pAdjPanel.minPButton.setEnabled(true);  //UNCOMMENT THIS WHEN MINP METHOD HAS BEEN DEBUGGED                  
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
            
            
            buildConstraints(constraints, 0, 0, 3, 1, 100, 30);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 30);
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
        }
    }
    
    class PValueAdjustmentPanel extends JPanel {
        JRadioButton minPButton, maxTButton, justAlphaButton, stdBonfButton, adjBonfButton;
        PValueAdjustmentPanel() {
            //      this.setBorder(new TitledBorder(new EtchedBorder(), "Significance based on: "));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Alpha Corrections", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            /*
            JLabel sigLabel = new JLabel("Significance based on: ");
            buildConstraints(constraints, 0, 0, 3, 1, 0, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sigLabel, constraints);
            this.add(sigLabel);
             */
            
            ButtonGroup sigGroup = new ButtonGroup();
            
            justAlphaButton = new JRadioButton("just alpha (no correction)", true);
            justAlphaButton.setFocusPainted(false);
            justAlphaButton.setForeground(UIManager.getColor("Label.foreground"));
            justAlphaButton.setBackground(Color.white);
            sigGroup.add(justAlphaButton);
            
            stdBonfButton = new JRadioButton("standard Bonferroni correction", false);
            stdBonfButton.setEnabled(false);//enable this button when this option is implemented
            stdBonfButton.setFocusPainted(false);
            stdBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            stdBonfButton.setBackground(Color.white);
            sigGroup.add(stdBonfButton);
            
            adjBonfButton = new JRadioButton("adjusted Bonferroni correction", false);
            adjBonfButton.setEnabled(false);//enable this button when this option is implemented
            adjBonfButton.setFocusPainted(false);
            adjBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            adjBonfButton.setBackground(Color.white);
            sigGroup.add(adjBonfButton);
            
            minPButton = new JRadioButton("minP", false);
            minPButton.setEnabled(false);
            minPButton.setFocusPainted(false);
            minPButton.setForeground(UIManager.getColor("Label.foreground"));
            minPButton.setBackground(Color.white);
            sigGroup.add(minPButton);
            
            maxTButton = new JRadioButton("maxT", false);
            maxTButton.setEnabled(false);
            maxTButton.setFocusPainted(false);
            maxTButton.setForeground(UIManager.getColor("Label.foreground"));
            maxTButton.setBackground(Color.white);
            sigGroup.add(maxTButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);
            
            JPanel westfallYoungPanel = new JPanel();
            westfallYoungPanel.setBackground(Color.white);
            westfallYoungPanel.setBorder(new EtchedBorder());
            GridBagLayout grid2 = new GridBagLayout();
            westfallYoungPanel.setLayout(grid2);
            
            JLabel stepDownLabel = new JLabel("Step-down Westfall and Young methods (for permutations only): ");
            buildConstraints(constraints, 0, 0, 1, 1, 34, 100);
            //buildConstraints(constraints, 0, 1, 1, 1, 34, 50);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.EAST;
            //gridbag.setConstraints(stepDownLabel, constraints);
            grid2.setConstraints(stepDownLabel, constraints);
            //this.add(stepDownLabel);
            westfallYoungPanel.add(stepDownLabel);
            
            //buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(minPButton, constraints);
            grid2.setConstraints(minPButton, constraints);
            //this.add(minPButton);
            westfallYoungPanel.add(minPButton);
            
            
            //buildConstraints(constraints, 2, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(maxTButton, constraints);
            grid2.setConstraints(maxTButton, constraints);
            //this.add(maxTButton);
            westfallYoungPanel.add(maxTButton);
            
            buildConstraints(constraints, 0, 1, 3, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(westfallYoungPanel, constraints);
            this.add(westfallYoungPanel);
            /*
            JButton sButton = new JButton("significancePanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sButton, constraints);
            this.add(sButton);
             */
            
        }
    }
    
    public boolean allCellsHaveOneSample() {
        return this.oneSamplePerCell;
    }
    
    public boolean usePerms() {
        return pPanel.permutButton.isSelected();
    }
    
    public float getAlpha() {
        return Float.parseFloat(pPanel.alphaInputField.getText());
    }
    
    public int getNumPerms() {
        return Integer.parseInt(pPanel.timesField.getText());
    }
    public int getSelectionType() {
    	return tabbedSelectors.getSelectedIndex();
    }
    public boolean isBalancedDesign() {
        boolean balanced = true;
        Vector[][] bothFactorAssignments = getBothFactorAssignments();     
        int[] cellSizes =new int[bothFactorAssignments.length*bothFactorAssignments[0].length];
        //System.out.println("cellSizes.length = " + cellSizes.length);
        int cellCounter = 0;
        for (int i = 0; i < bothFactorAssignments.length; i++) {
            for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                cellSizes[cellCounter] = bothFactorAssignments[i][j].size();
                cellCounter++;
            }
        } 
        
        int numPerCell = cellSizes[0];
        
        for (int i = 1; i < cellSizes.length; i++) {
            if (cellSizes[i] != numPerCell) {
                balanced = false;
                break;
            }
        }
        
        return balanced;
    }
    
    public int getAdjustmentMethod() {
        if (pAdjPanel.justAlphaButton.isSelected()) {
            return TFAInitBox2.JUST_ALPHA;
        } else if (pAdjPanel.stdBonfButton.isSelected()) {
            return TFAInitBox2.STD_BONFERRONI;
        } else if (pAdjPanel.adjBonfButton.isSelected()){
            return TFAInitBox2.ADJ_BONFERRONI;
        } else if (pAdjPanel.maxTButton.isSelected()) {
            return TFAInitBox2.MAX_T;
        } else if (pAdjPanel.minPButton.isSelected()) {
            return TFAInitBox2.MIN_P;
        } else {
            return -1;
        }
    }    
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        super.setVisible(visible);
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }

    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (getSelectionType() == 1){
	            	if (repository==null||repository.isEmpty()){
	                    JOptionPane.showMessageDialog(new JPanel(), "Sample cluster repository is empty", "Error", JOptionPane.WARNING_MESSAGE);
	                    return;
	        		}
            	}
                Vector[][] bothFactorAssignments = getBothFactorAssignments();
                if (bothFactorAssignments==null)
                	return;
                int[] cellSizes =new int[bothFactorAssignments.length*bothFactorAssignments[0].length];
                //System.out.println("cellSizes.length = " + cellSizes.length);
                int cellCounter = 0;
                for (int i = 0; i < bothFactorAssignments.length; i++) {
                    for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                        cellSizes[cellCounter] = bothFactorAssignments[i][j].size();
                        cellCounter++;
                    }
                }
                if (cellSizes[0] == 1) {
                    boolean allOne = true;
                    for (int i = 1; i < cellSizes.length; i++) {
                        if (cellSizes[i] != 1) {
                            allOne = false;
                            //oneSamplePerCell = true;
                            break;
                        }
                    }
                    if (!allOne) {
                        JOptionPane.showMessageDialog(null, "All factor combinations must contain more than one sample, or else they must all contain exactly one sample each", "Error", JOptionPane.ERROR_MESSAGE);
                        okPressed = false;
                        return;
                    } else {
                        oneSamplePerCell = true;
                    }
                } else {
                    for (int i = 0; i < cellSizes.length; i++) {
                        if (cellSizes[i] < 2) {
                            JOptionPane.showMessageDialog(null, "All factor combinations must contain more than one sample, or else they must all contain exactly one sample each", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;                            
                        }
                    }
                }
                
                if (usePerms()) {
                    try {
                        int numPerms = getNumPerms();
                        if (numPerms <= 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations should be an integer > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;                         
                        }
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(null, "Number of permutations should be an integer > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;
                        }
                }
                
                try {
                    float alpha = getAlpha();
                    if ((alpha <= 0) || (alpha >= 1)) {
                        JOptionPane.showMessageDialog(null, "Critical p-value should be between 0 and 1", "Error", JOptionPane.ERROR_MESSAGE);
                        okPressed = false;
                        return;                        
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Critical p-value should be between 0 and 1", "Error", JOptionPane.ERROR_MESSAGE);
                    okPressed = false;
                    return;                    
                }
                
                okPressed = true;
                hide();
                dispose();                
            }
            else if(command.equals("reset-command")){
                okPressed = false;
                gPanel.reset();
                return;
            }
            else if(command.equals("cancel-command")){
                okPressed = false;
                setVisible(false);
                dispose();
            }
            else if(command.equals("info-command")){
            }
        }
        
    }
    
    public int[] getFactorAAssignments() {
        return gPanel.factorAPanel.getGroupAssignments();
    }

    public int[] getFactorBAssignments() {
        return gPanel.factorBPanel.getGroupAssignments();
    }   
    
    
    
    public int[] getFactorAClusterAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[numFactorLevels[0]];
    	for (int i=0; i<numFactorLevels[0]; i++){
    		int j = i+1;
    		arraylistArray[i] = clusterSelectorA.getGroupSamples("Group "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
    		for (int j = 0;j<numFactorLevels[0];j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen "+factorNames[0]+" have overlapping samples. \n Each group must contain unique samples.", 
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
    public int[] getFactorBClusterAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[numFactorLevels[1]];
    	for (int i=0; i<numFactorLevels[1]; i++){
    		int j = i+1;
    		arraylistArray[i] = clusterSelectorB.getGroupSamples("Group "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
    		for (int j = 0;j<numFactorLevels[1];j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen for "+factorNames[1]+" have overlapping samples. \n Each group must contain unique samples.", 
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
    
    
    public boolean isButtonSelectionMethod(){
    	return (tabbedSelectors.getSelectedIndex()==0);
    }
    

    public Vector[][] getBothFactorAssignments() {
        Vector[][] bothFactorAssignments = new Vector[numFactorLevels[0]][numFactorLevels[1]];
        
        for (int i = 0; i < bothFactorAssignments.length; i++) {
            for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                bothFactorAssignments[i][j] = new Vector();
            }
        }
        int[] factorAAssgn;
    	int[] factorBAssgn;
        if (isButtonSelectionMethod()){
        	factorAAssgn = getFactorAAssignments();
        	factorBAssgn = getFactorBAssignments();
        } else{
        	factorAAssgn = getFactorAClusterAssignments();
        	factorBAssgn = getFactorBClusterAssignments();
        }
        if (factorAAssgn==null||factorBAssgn==null)
        	return null;
    	for (int i = 0; i < factorAAssgn.length; i++) {
            if ((factorAAssgn[i] != 0)&&(factorBAssgn[i] != 0)) {
                bothFactorAssignments[factorAAssgn[i] - 1][factorBAssgn[i] - 1].add(new Integer(i));
            }
        }
        
        return bothFactorAssignments;
    }
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector dummyVect = new Vector();
        dummyVect.add("same");
        dummyVect.add("same");
        dummyVect.add("same");
        dummyVect.add("same");
        dummyVect.add("same");
        for (int i = 0; i < 95; i++) {
            dummyVect.add("Expt " + i);
        }
        String[] factorNames = {"Factor A", "Factor BEE"};
        int[] numFactorLevels = {4,3};
        TFAInitBox2 tfaBox = new TFAInitBox2(dummyFrame, true, dummyVect, factorNames,numFactorLevels, null);
        tfaBox.setVisible(true);
        
    }
}
