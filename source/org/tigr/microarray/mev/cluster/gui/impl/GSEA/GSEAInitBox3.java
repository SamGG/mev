/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFAInitBox2.java
 *
 * Created on February 12, 2004, 4:15 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    
    
/*    public GSEAInitBox3(JFrame parentFrame, boolean modality, int num_factors, Vector exptNames, String[] factorNames, int[] numFactorLevels) {
    	this(parentFrame, modality, num_factors, exptNames, factorNames, numFactorLevels, null);
    	    	
    }*/
    public GSEAInitBox3(JFrame parentFrame, boolean modality, int num_factors, Vector exptNames, String[] factorNames, int[] numFactorLevels, ClusterRepository repository) {
        super(parentFrame, "GSEA - Initialization", modality);
        this.exptNames = exptNames;
        this.factorNames = factorNames;
        this.numFactorLevels = numFactorLevels;
        clusterSelector = new ClusterSelector[num_factors];
        System.out.println("repository null? "+repository.isEmpty());
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
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(GroupExptsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                          
                            if(getNumberofFactors()==2){
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
                            		out.print(factorAGroupAssgn[i]);
                            		if (i < factorAGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();

                            	for (int i = 0; i < factorBGroupAssgn.length; i++) {
                            		out.print(factorBGroupAssgn[i]);
                            		if (i < factorBGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();
                            }else if(getNumberofFactors()==3){
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            	int[] factorCGroupAssgn = factorCPanel.getGroupAssignments();
                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
                            		out.print(factorAGroupAssgn[i]);
                            		if (i < factorAGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();

                            	for (int i = 0; i < factorBGroupAssgn.length; i++) {
                            		out.print(factorBGroupAssgn[i]);
                            		if (i < factorBGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();
                                                       	
                            	for (int i = 0; i < factorCGroupAssgn.length; i++) {
                            		out.print(factorCGroupAssgn[i]);
                            		if (i < factorCGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();
                            	
                            }else{
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	for (int i = 0; i < factorAGroupAssgn.length; i++) {
                            		out.print(factorAGroupAssgn[i]);
                            		if (i < factorAGroupAssgn.length - 1) {
                            			out.print("\t");
                            		}
                            	}
                            	out.println();

                            }
                            
                            
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
                }
            });
            
            
            
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(GroupExptsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                        	
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String line = buff.readLine();
                            
                            Vector factorAGroupsVector = new Vector();
                        	Vector factorBGroupsVector = new Vector();
                        	Vector factorCGroupsVector = new Vector();
                            //System.out.println(line);
                            StringSplitter st = new StringSplitter('\t');
                            st.init(line);
                            
                            if(getNumberofFactors()==2){
                            	 while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorAGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}

                            	line = buff.readLine();
                            	st.init(line);
                            	while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorBGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}

                            	buff.close();
                            }else if(getNumberofFactors()== 3){
                            	 
                            	while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorAGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}

                            	line = buff.readLine();
                            	st.init(line);
                            	while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorBGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}
                            	
                            	line=buff.readLine();
                            	st.init(line);
                            	
                            	while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorCGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}
                              	buff.close();

                            }else{
                            	while (st.hasMoreTokens()) {
                            		String current = st.nextToken();
                            		factorAGroupsVector.add(new Integer(current));
                            		//System.out.print(current);
                            	}
                            	buff.close();
                            }


                            
                            if(getNumberofFactors()==2){
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) || (factorBGroupsVector.size() != factorBGroupAssgn.length)){
                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            	} else {
                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
                            		int[] factorBAssignments = new int[factorBGroupsVector.size()];
                            		//Unlike ANOVA, NO assumption of factor levels being same across factors
                            		for (int i = 0; i < factorAAssignments.length; i++) {
                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
                            			
                            		}
                            		
                            		for (int i = 0; i < factorBAssignments.length; i++) {
                            			factorBAssignments[i] = ((Integer)(factorBGroupsVector.get(i))).intValue();
                            		}
                            		
                            		factorAPanel.setGroupAssignments(factorAAssignments);
                            		factorBPanel.setGroupAssignments(factorBAssignments);
                            	}
                            }else if(getNumberofFactors()==3){
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            	int[] factorCGroupAssgn = factorCPanel.getGroupAssignments();
                            	
                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) || (factorBGroupsVector.size() != factorBGroupAssgn.length)
                            			||(factorCGroupsVector.size() != factorCGroupAssgn.length)){
                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            	} else {
                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
                            		int[] factorBAssignments = new int[factorBGroupsVector.size()];
                            		int[] factorCAssignments = new int[factorCGroupsVector.size()];
                            		
                            		for (int i = 0; i < factorAAssignments.length; i++) {
                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
                            			
                            		}
                            		
                            		for (int i = 0; i < factorBAssignments.length; i++) {
                            			factorBAssignments[i] = ((Integer)(factorBGroupsVector.get(i))).intValue();
                            			
                            		}
                            		
                            		for (int i = 0; i < factorCAssignments.length; i++) {
                            			factorCAssignments[i] = ((Integer)(factorCGroupsVector.get(i))).intValue();
                            			
                            		}

                            		factorAPanel.setGroupAssignments(factorAAssignments);
                            		factorBPanel.setGroupAssignments(factorBAssignments);
                            		factorCPanel.setGroupAssignments(factorCAssignments);
                            	}

                            }else{
                            	int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            	if ((factorAGroupsVector.size() != factorAGroupAssgn.length) ){
                            		JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            	} else {
                            		int[] factorAAssignments = new int[factorAGroupsVector.size()];
                            		//Unlike ANOVA, NO assumption of factor levels being same across factors
                            		for (int i = 0; i < factorAAssignments.length; i++) {
                            			factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
                            			
                            		}
                            		
                            		factorAPanel.setGroupAssignments(factorAAssignments);
                            	
                            	}
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                           
                            if(getNumberofFactors() == 2){
                            	factorAPanel.reset();
                            	factorBPanel.reset();
                            }else if(getNumberofFactors() == 3){
                            	factorAPanel.reset();
                            	factorBPanel.reset();
                            	factorCPanel.reset();
                            }else{
                            	factorAPanel.reset();
                            }
                            	
                            //e.printStackTrace();
                        }
                        
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
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
