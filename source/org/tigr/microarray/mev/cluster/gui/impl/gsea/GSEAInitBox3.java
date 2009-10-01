/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFAInitBox2.java
 *
 * Created on February 12, 2004, 4:15 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.gsea;

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
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.util.StringSplitter;


/**
 *
 * @author  Sarita Nair
 */
public class GSEAInitBox3 extends AlgorithmDialog {
    
    
    protected boolean okPressed = false;
    protected boolean oneSamplePerCell = false;
    Vector exptNames;
    int num_factors;
    String[] factorNames;
    int[] numFactorLevels;
    GroupExptsPanel gPanel;
    JTabbedPane tabbedSelectors;
    ClusterSelector[] clusterSelector;
    
    
    public GSEAInitBox3(JFrame parentFrame, boolean modality, int num_factors, Vector exptNames, String[] factorNames, int[] numFactorLevels, ClusterRepository repository) {
        super(parentFrame, "GSEA - Initialization", modality);
        this.exptNames = exptNames;
        this.factorNames = factorNames;
        this.numFactorLevels = numFactorLevels;
        clusterSelector = new ClusterSelector[num_factors];
//        System.out.println("repository null? "+repository.isEmpty());
        for (int i=0; i<num_factors; i++){
        	clusterSelector[i]=new ClusterSelector(repository, numFactorLevels[i]);
        }
        setNumberofFactors(num_factors);
        
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
         
        if (repository!=null){
        	for (int i=0; i<num_factors;i++){
        		clusterSelector[i].setClusterType(factorNames[i]);
        	}
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
        for (int i=0; i<num_factors; i++){
        	clusterSelectorPanel.add(clusterSelector[i], c);
        	c.gridy++;
        }
        tabbedSelectors = new JTabbedPane();
        tabbedSelectors.add("Button Selection",gPanel);      
        tabbedSelectors.add("Cluster Selection",clusterSelectorPanel);  
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(tabbedSelectors, constraints);
        pane.add(tabbedSelectors);
        
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
    }
    
    public void setNumberofFactors(int num){
    	this.num_factors=num;
    }
    
    public int getNumberofFactors(){
    	return this.num_factors;
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
        MultiGroupExperimentsPanel factorAPanel, factorBPanel, factorCPanel;
        JPanel panel2;
        GroupExptsPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            
            GridBagLayout grid1 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(grid1);
            
            
            if(getNumberofFactors()==3){
            	factorAPanel = new MultiGroupExperimentsPanel(factorNames[0], numFactorLevels[0]);
            	factorBPanel = new MultiGroupExperimentsPanel(factorNames[1], numFactorLevels[1]);
            	factorCPanel = new MultiGroupExperimentsPanel(factorNames[2], numFactorLevels[2]);
            	
            	
                buildConstraints(constraints, 0, 0, 1, 1, 50, 90);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorAPanel, constraints);
                this.add(factorAPanel);
                
                buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorBPanel, constraints);
                this.add(factorBPanel);
                
                buildConstraints(constraints, 2, 0, 1, 1, 50, 0);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorCPanel, constraints);
                this.add(factorCPanel);
                
            	
            	
            }else if(getNumberofFactors()==2){
            	factorAPanel = new MultiGroupExperimentsPanel(factorNames[0], numFactorLevels[0]);
            	factorBPanel = new MultiGroupExperimentsPanel(factorNames[1], numFactorLevels[1]);
            	
            	
                buildConstraints(constraints, 0, 0, 1, 1, 50, 90);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorAPanel, constraints);
                this.add(factorAPanel);
                
                buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorBPanel, constraints);
                this.add(factorBPanel);
                
            	
            }else{
            	factorAPanel = new MultiGroupExperimentsPanel(factorNames[0], numFactorLevels[0]);
            	
                buildConstraints(constraints, 0, 0, 1, 1, 50, 90);
                constraints.fill = GridBagConstraints.BOTH;
                grid1.setConstraints(factorAPanel, constraints);
                this.add(factorAPanel);
                
            }

            
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
                	if(getNumberofFactors()==2){
                		factorAPanel.reset();
                		factorBPanel.reset();
                	}else if(getNumberofFactors()==3){
                		factorAPanel.reset();
                		factorBPanel.reset();
                		factorCPanel.reset();
                	}else{
                		factorAPanel.reset();
                	}
                	
                }
            });
            
            final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	saveAssignments();
                	
//                    int returnVal = fc.showSaveDialog(GroupExptsPanel.this);
//                    if (returnVal == JFileChooser.APPROVE_OPTION) {
//                        File file = fc.getSelectedFile();
//                        try {
//                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
//                          
//                            if(getNumberofFactors()==2){
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
//                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
//                            		out.print(factorAGroupAssgn[i]);
//                            		if (i < factorAGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//
//                            	for (int i = 0; i < factorBGroupAssgn.length; i++) {
//                            		out.print(factorBGroupAssgn[i]);
//                            		if (i < factorBGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//                            }else if(getNumberofFactors()==3){
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
//                            	int[] factorCGroupAssgn = factorCPanel.getGroupAssignments();
//                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
//                            		out.print(factorAGroupAssgn[i]);
//                            		if (i < factorAGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//
//                            	for (int i = 0; i < factorBGroupAssgn.length; i++) {
//                            		out.print(factorBGroupAssgn[i]);
//                            		if (i < factorBGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//                                                       	
//                            	for (int i = 0; i < factorCGroupAssgn.length; i++) {
//                            		out.print(factorCGroupAssgn[i]);
//                            		if (i < factorCGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//                            	
//                            }else{
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
//                            		out.print(factorAGroupAssgn[i]);
//                            		if (i < factorAGroupAssgn.length - 1) {
//                            			out.print("\t");
//                            		}
//                            	}
//                            	out.println();
//
//                            }
//                            
//                            
//                            out.flush();
//                            out.close();
//                        } catch (Exception e) {
//                            //e.printStackTrace();
//                        }
//                        //this is where a real application would save the file.
//                        //log.append("Saving: " + file.getName() + "." + newline);
//                    } else {
//                        //log.append("Save command cancelled by user." + newline);
//                    }
                }
            });
            
            
            
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	loadAssignments();
//                    int returnVal = fc.showOpenDialog(GroupExptsPanel.this);
//                    if (returnVal == JFileChooser.APPROVE_OPTION) {
//                        try {
//                        	
//                            FileReader file = new FileReader(fc.getSelectedFile());
//                            BufferedReader buff = new BufferedReader(file);
//                            String line = buff.readLine();
//                            
//                            Vector factorAGroupsVector = new Vector();
//                        	Vector factorBGroupsVector = new Vector();
//                        	Vector factorCGroupsVector = new Vector();
//                            //System.out.println(line);
//                            StringSplitter st = new StringSplitter('\t');
//                            st.init(line);
//                            
//                            if(getNumberofFactors()==2){
//                            	 while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorAGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//
//                            	line = buff.readLine();
//                            	st.init(line);
//                            	while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorBGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//
//                            	buff.close();
//                            }else if(getNumberofFactors()== 3){
//                            	 
//                            	while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorAGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//
//                            	line = buff.readLine();
//                            	st.init(line);
//                            	while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorBGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//                            	
//                            	line=buff.readLine();
//                            	st.init(line);
//                            	
//                            	while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorCGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//                              	buff.close();
//
//                            }else{
//                            	while (st.hasMoreTokens()) {
//                            		String current = st.nextToken();
//                            		factorAGroupsVector.add(new Integer(current));
//                            		//System.out.print(current);
//                            	}
//                            	buff.close();
//                            }
//
//
//                            
//                            if(getNumberofFactors()==2){
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
//                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) || (factorBGroupsVector.size() != factorBGroupAssgn.length)){
//                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
//                            	} else {
//                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
//                            		int[] factorBAssignments = new int[factorBGroupsVector.size()];
//                            		//Unlike ANOVA, NO assumption of factor levels being same across factors
//                            		for (int i = 0; i < factorAAssignments.length; i++) {
//                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
//                            			
//                            		}
//                            		
//                            		for (int i = 0; i < factorBAssignments.length; i++) {
//                            			factorBAssignments[i] = ((Integer)(factorBGroupsVector.get(i))).intValue();
//                            		}
//                            		
//                            		factorAPanel.setGroupAssignments(factorAAssignments);
//                            		factorBPanel.setGroupAssignments(factorBAssignments);
//                            	}
//                            }else if(getNumberofFactors()==3){
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
//                            	int[] factorCGroupAssgn = factorCPanel.getGroupAssignments();
//                            	
//                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) || (factorBGroupsVector.size() != factorBGroupAssgn.length)
//                            			||(factorCGroupsVector.size() != factorCGroupAssgn.length)){
//                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
//                            	} else {
//                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
//                            		int[] factorBAssignments = new int[factorBGroupsVector.size()];
//                            		int[] factorCAssignments = new int[factorCGroupsVector.size()];
//                            		
//                            		for (int i = 0; i < factorAAssignments.length; i++) {
//                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
//                            			
//                            		}
//                            		
//                            		for (int i = 0; i < factorBAssignments.length; i++) {
//                            			factorBAssignments[i] = ((Integer)(factorBGroupsVector.get(i))).intValue();
//                            			
//                            		}
//                            		
//                            		for (int i = 0; i < factorCAssignments.length; i++) {
//                            			factorCAssignments[i] = ((Integer)(factorCGroupsVector.get(i))).intValue();
//                            			
//                            		}
//
//                            		factorAPanel.setGroupAssignments(factorAAssignments);
//                            		factorBPanel.setGroupAssignments(factorBAssignments);
//                            		factorCPanel.setGroupAssignments(factorCAssignments);
//                            	}
//
//                            }else{
//                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
//                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) ){
//                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
//                            	} else {
//                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
//                            		//Unlike ANOVA, NO assumption of factor levels being same across factors
//                            		for (int i = 0; i < factorAAssignments.length; i++) {
//                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
//                            			
//                            		}
//                            		
//                            		factorAPanel.setGroupAssignments(factorAAssignments);
//                            	
//                            	}
//                            }
//                        } catch (Exception e) {
//                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
//                           
//                            if(getNumberofFactors() == 2){
//                            	factorAPanel.reset();
//                            	factorBPanel.reset();
//                            }else if(getNumberofFactors() == 3){
//                            	factorAPanel.reset();
//                            	factorBPanel.reset();
//                            	factorCPanel.reset();
//                            }else{
//                            	factorAPanel.reset();
//                            }
//                            	
//                            //e.printStackTrace();
//                        }
//                        
//                        //this is where a real application would save the file.
//                        //log.append("Saving: " + file.getName() + "." + newline);
//                    } else {
//                        //log.append("Save command canceled by user." + newline);
//                    }
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
        
        public void reset() {
        	if(getNumberofFactors()==2){
        		factorAPanel.reset();
        		factorBPanel.reset();
        	}else if(getNumberofFactors()==3){
        		factorAPanel.reset();
        		factorBPanel.reset();
        		factorCPanel.reset();
        	}else{
        		factorAPanel.reset();
        	}
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
    				pw.println("GSEA");
    				int maxClasses=0;
    				for (int i=0; i<numFactorLevels.length; i++){
    					if (numFactorLevels[i]>maxClasses)
    						maxClasses = numFactorLevels[i];
    				}
    				for (int i=0; i<maxClasses; i++){
        				pw.print("Group "+(i+1)+" Label:\t");
    					pw.println("Group "+(i+1));
    				}
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");

    				int[]groupAAssgn=getFactorAAssignments();
    				int[]groupBAssgn=null;
    				int[]groupCAssgn=null;
    				if(getNumberofFactors()==3||getNumberofFactors()==2)
    					groupBAssgn=getFactorBAssignments();
    				if(getNumberofFactors()==3){
    					groupCAssgn=getFactorCAssignments();
    				}
    				
    				for(int sample = 0; sample < exptNames.size(); sample++) {
    					pw.print(String.valueOf(sample+1)+"\t"); //sample index
    					pw.print(exptNames.get(sample)+"\t");
    					
    					if (groupAAssgn[sample]!=0)
    						pw.print("Group "+(groupAAssgn[sample]));
    					else
    						pw.print("Exclude");

    					if(getNumberofFactors()==3||getNumberofFactors()==2){
	    					if (groupBAssgn[sample]!=0)
	    						pw.print("\tGroup "+(groupBAssgn[sample]));
	    					else
	    						pw.print("\tExclude");
    					}
    					
    					if(getNumberofFactors()==3){
	    					if (groupCAssgn[sample]!=0)
	    						pw.print("\tGroup "+(groupCAssgn[sample]));
	    					else
	    						pw.print("\tExclude");
    					}
    					pw.println();
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
	       				
	       			//build structures to capture the data for assignment information and for *validation
	       			
	       			//factor names
	       			Vector<String> groupNames = new Vector<String>();
	       			
	       			
	       			Vector<Integer> sampleIndices = new Vector<Integer>();
	       			Vector<String> sampleNames = new Vector<String>();
	       			Vector<String> groupAAssignments = new Vector<String>();	
	       			Vector<String> groupBAssignments = new Vector<String>();		
	       			Vector<String> groupCAssignments = new Vector<String>();	
	       			
	       			//parse the data in to these structures
	       			String [] lineArray;
	       			//String status = "OK";
	       			boolean twoloaded = true;
	       			boolean threeloaded = true;
	       			for(int row = 0; row < data.size(); row++) {
	       				line = (String)(data.get(row));
	
	       				//if not a comment line, and not the header line
	       				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
	       					
	       					lineArray = line.split("\t");
	       					
	       					//check what module saved the file
	       					if(lineArray[0].startsWith("Module:")) {
	       						if (!lineArray[1].equals("GSEA")){
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
	       						twoloaded=false;
	       					}
	       					try{
		       					groupCAssignments.add(lineArray[4]);
	       					}catch (Exception e){
	       						threeloaded=false;
	       					}
	       				}				
	       			}
	       			if (!twoloaded)
	       				JOptionPane.showMessageDialog(null, "The loaded file contained only 1 Factor.  Only "+factorNames[0]+" will be loaded.", "Error", JOptionPane.ERROR_MESSAGE);                            	       						
	       			else if ((!threeloaded)&&factorNames.length==3)
	       				JOptionPane.showMessageDialog(null, "The loaded file contained only 2 Factors.  Only "+factorNames[0]+" and "+factorNames[1]+" will be loaded.", "Error", JOptionPane.ERROR_MESSAGE);                            	       						
	       			
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
	       			int fileSampleIndexC = 0;
	       			int groupIndexC = 0;
	       			String groupAName;
	       			String groupBName;
	       			String groupCName;
	       			
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
        				if (twoloaded){
		       				fileSampleIndexB = sampleNames.indexOf(exptNames.get(sample));
		       				if (fileSampleIndexB==-1){
		       					doIndex=true;
		       					break;
		       				}
        				}
        				if (threeloaded&&factorNames.length==3){
		       				fileSampleIndexC = sampleNames.indexOf(exptNames.get(sample));
		       				if (fileSampleIndexC==-1){
		       					doIndex=true;
		       					break;
		       				}
        				}
        				if (doIndex){
        					setStateBasedOnIndex(groupAAssignments,groupBAssignments,groupCAssignments,groupNames,twoloaded,threeloaded);
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
	       				
	       				if (twoloaded){
		       				
		       				groupBName = (String)(groupBAssignments.get(fileSampleIndexB));
		       				groupIndexB = groupNames.indexOf(groupBName);
		       				
		       				//set state
		       				try{
		       					gPanel.factorBPanel.exptGroupRadioButtons[groupIndexB][sample].setSelected(true);
		       				}catch (Exception e){
		       					gPanel.factorBPanel.notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
		       				}
	       				}
	       				if (threeloaded&&factorNames.length==3){
		       				
		       				groupCName = (String)(groupCAssignments.get(fileSampleIndexC));
		       				groupIndexC = groupNames.indexOf(groupCName);
		       				
		       				//set state
		       				try{
		       					gPanel.factorCPanel.exptGroupRadioButtons[groupIndexC][sample].setSelected(true);
		       				}catch (Exception e){
		       					gPanel.factorCPanel.notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
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
	   	
	   	private void setStateBasedOnIndex(Vector<String>groupAAssignments,Vector<String>groupBAssignments,Vector<String>groupCAssignments,Vector<String>groupNames, boolean twoloaded, boolean threeloaded){
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
	   		if (twoloaded){
		   		for(int sample = 0; sample < exptNames.size(); sample++) {
		   			try{
		   				gPanel.factorBPanel.exptGroupRadioButtons[groupNames.indexOf(groupBAssignments.get(sample))][sample].setSelected(true);
		   			}catch(Exception e){
		   				gPanel.factorBPanel.notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
		   			}
		   		}
	   		}
	   		if (threeloaded&&factorNames.length==3){
		   		for(int sample = 0; sample < exptNames.size(); sample++) {
		   			try{
		   				gPanel.factorCPanel.exptGroupRadioButtons[groupNames.indexOf(groupCAssignments.get(sample))][sample].setSelected(true);
		   			}catch(Exception e){
		   				gPanel.factorCPanel.notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
		   			}
		   		}
	   		}
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
    
    
    public boolean allCellsHaveOneSample() {
        return this.oneSamplePerCell;
    }
    
    
  /*  public boolean isBalancedDesign() {
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
    }*/
    
    
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
            	
       /*         Vector[][] bothFactorAssignments = getBothFactorAssignments();
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
                }*/
                
                
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
    //Think about converting to this in the future. The overhead would be to check the panel names i guess, 
    //in order to figure which factor does it go to?
    //public int[]getfactorAssignment(String factorName)
    
    
    public int[] getFactorAAssignments() {
    	if (!isButtonSelectionMethod()){
    		return getFactorClusterAssignments(0);
    	}
        return gPanel.factorAPanel.getGroupAssignments();
    }

    public int[] getFactorBAssignments() {
    	if (!isButtonSelectionMethod()){
    		return getFactorClusterAssignments(1);
    	}
        return gPanel.factorBPanel.getGroupAssignments();
    }   
    
    public int[] getFactorCAssignments() {
    	if (!isButtonSelectionMethod()){
    		return getFactorClusterAssignments(2);
    	}
        return gPanel.factorCPanel.getGroupAssignments();
    }   
    
    public int[] getFactorClusterAssignments(int factor){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[numFactorLevels[0]];
    	for (int i=0; i<numFactorLevels[0]; i++){
    		int j = i+1;
    		arraylistArray[i] = clusterSelector[factor].getGroupSamples("Group "+j);
    		
    	}
    	for (int i=0; i<arraylistArray[0].size();i++){
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
       
    public boolean isButtonSelectionMethod(){
    	return (tabbedSelectors.getSelectedIndex()==0);
    }
    
    public int[][]getAllFactorAssignments(){
    	int[][]factorAssignments=new int[getNumberofFactors()][];
    	if(getNumberofFactors()==3){
    		factorAssignments[0]=getFactorAAssignments();
    		factorAssignments[1]=getFactorBAssignments();
    		factorAssignments[2]=getFactorCAssignments();
    	}else if(getNumberofFactors()==2){
    		factorAssignments[0]=getFactorAAssignments();
    		factorAssignments[1]=getFactorBAssignments();
    	}else{
    		factorAssignments[0]=getFactorAAssignments();
    	}
    	
    	
    	return factorAssignments;
    	
    }
    
    
    
    
    
  /*  public Vector[][] getBothFactorAssignments() {
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
    	for (int i = 0; i < factorAAssgn.length; i++) {
            if ((factorAAssgn[i] != 0)&&(factorBAssgn[i] != 0)) {
                bothFactorAssignments[factorAAssgn[i] - 1][factorBAssgn[i] - 1].add(new Integer(i));
            }
        }
        
        return bothFactorAssignments;
    }*/
    
    public static void main(String[] args){
    	
  /*  	String[]factorNames=null;
    	int[]factorLevels=null;
    	GSEAInitBox1 tBox = new GSEAInitBox1(new JFrame(), null,true);
	    tBox.setVisible(true);
	    
	    if(tBox.okPressed){
	    GSEAAInitBox2 tBox2 = new GSEAAInitBox2(new JFrame(), true,Integer.parseInt(tBox.getNumberofFactors()));
	    tBox2.setVisible(true);
	    
	    if(Integer.parseInt(tBox.getNumberofFactors())== 3){
	    	factorNames=new String[3];
	    		    	
	    	factorNames[0]=tBox2.getFactorAName();
	    	factorNames[1]=tBox2.getFactorBName();
	    	factorNames[2]=tBox2.getFactorCName();
	    	
	    	
	    }else if(Integer.parseInt(tBox.getNumberofFactors())== 2){
	    	factorNames=new String[2];
	    		    		    	
	    	factorNames[0]=tBox2.getFactorAName();
	    	factorNames[1]=tBox2.getFactorBName();
	    	
	    	
	    }else{
	    	factorNames=new String[1];
	       	factorNames[0]=tBox2.getFactorAName();
	    	
	    }
	    
	    if(tBox2.okPressed){
	    	if(Integer.parseInt(tBox.getNumberofFactors())== 3){
	    		factorLevels=new int[3];
	    		factorLevels[0]=tBox2.getNumFactorALevels();
		    	factorLevels[1]=tBox2.getNumFactorBLevels();
		    	factorLevels[2]=tBox2.getNumFactorCLevels();
	    	}else if(Integer.parseInt(tBox.getNumberofFactors())== 2){
	    		factorLevels=new int[2];
	    	   	factorLevels[0]=tBox2.getNumFactorALevels();
		    	factorLevels[1]=tBox2.getNumFactorBLevels();
	    	}else{
	    		factorLevels=new int[1];
	    		factorLevels[0]=tBox2.getNumFactorALevels();
	    	}
	    }
	    }
	    
	    Vector exptNames=new Vector();
	    for(int i=0; i<100; i++){
	    	exptNames.add(i, "Expt:"+i);
	    }
	    
	    GSEAInitBox3 gBox3=new GSEAInitBox3(new JFrame(), true,Integer.parseInt(tBox.getNumberofFactors()), exptNames, factorNames, factorLevels);
	    gBox3.setVisible(true);
	    int[][]assignments=new int[3][];
	    assignments[0]=gBox3.getFactorAAssignments();*/
    }
    
    
    

  }
