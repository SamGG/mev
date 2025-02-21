/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * SAMInitDialog.java
 *
 * Created on November 7, 2002, 2:06 PM
 */
package org.tigr.microarray.mev.cluster.gui.impl.sam;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.StringSplitter;
/**
 *
 * @author  nbhagaba
 * @version
 */
public class SAMInitDialog extends AlgorithmDialog {
 
    ImageIcon forwardImage, backImage; 
    JTabbedPane tabPane;
    JTabbedPane unpairedTab;
    JTabbedPane oneClassTab;
    JTextField oneClassClusterMean;
    GroupExperimentsPanel gPanel;
    TwoClassPairedMainPanel tcpmPanel;
    MultiClassPanel mPanel;
    CensoredSurvivalPanel csPanel;
    OneClassPanel oneCPanel;
    S0AndQValueCalcPanel sqPanel;
    VersionPanel vPanel;
    PermutationsPanel pPanel;
    ImputationPanel iPanel;
    OKCancelPanel oPanel;
    final int fileLoadMin=20;
    boolean okPressed = false, allUniquePermsUsed = false;
    Vector<String> exptNames;
    int numGenes, numUniquePerms;
    HCLSigOnlyPanel hclOpsPanel;
    boolean lotsOfSamples = false;
    String lotsOfSamplesWarningText = "                                                Note: You can assign large numbers of samples quickly by using a saved text file.";
    
    
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 0;
    
    public static final int TWO_CLASS_UNPAIRED = 4;
    public static final int TWO_CLASS_PAIRED = 5;
    public static final int MULTI_CLASS = 6;
    public static final int CENSORED_SURVIVAL = 7;
    public static final int ONE_CLASS = 8;    
    public static final int BUTTON_SELECTION = 9;
    public static final int CLUSTER_SELECTION = 10;
    protected ClusterSelector unpairedSelector;
    protected ClusterSelector oneClassSelector;
    protected ClusterSelector multiClassSelector;
    protected ClusterRepository repository;
    

    /** Creates new SAMInitDialog */
    public SAMInitDialog(JFrame parentFrame, boolean modality, Vector<String> exptNames, int numGenes, ClusterRepository repository) {
    	this(parentFrame, modality, exptNames,numGenes,repository,true);
    }
    /** Creates new SAMInitDialog */
    public SAMInitDialog(JFrame parentFrame, boolean modality, Vector<String> exptNames, int numGenes, ClusterRepository repository, boolean isDataAppropriate) {
        
        super(parentFrame, "SAM Initialization", modality, isDataAppropriate);
        //this.parentFrame = parentFrame;
        this.exptNames = exptNames;
        this.numGenes = numGenes;
        this.numUniquePerms = 0;
        this.repository = repository;
        setBounds(0, 0, 700, 800);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        if(exptNames.size()>fileLoadMin){
        	lotsOfSamples = true;
        }
        
        forwardImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("org/tigr/images/Forward24.gif")));        
        backImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("org/tigr/images/Back24.gif")));        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        javax.swing.UIManager.put("TabbedPane.selected", Color.white);
        tabPane = new JTabbedPane();
        gPanel = new GroupExperimentsPanel(exptNames);
        unpairedSelector = new ClusterSelector(repository, 2);
        unpairedTab = new JTabbedPane();
        unpairedTab.add("Button Selection", gPanel);
        unpairedTab.add("Cluster Selection",unpairedSelector);

        unpairedTab.setSelectedIndex(1);
        if (repository==null||repository.isEmpty())
        	unpairedTab.setSelectedIndex(0);
        
        tabPane.add("Two-class unpaired", unpairedTab);
        tcpmPanel = new TwoClassPairedMainPanel();

        
        
        tabPane.add("Two-class paired", tcpmPanel);
        mPanel = new MultiClassPanel(/*exptNames*/);
        tabPane.add("Multi-class", mPanel);
        
        csPanel = new CensoredSurvivalPanel(exptNames);
        tabPane.add("Censored survival", csPanel);
        
        
        JLabel meanLabel = new JLabel("Enter the mean value to be tested against: ");
        JPanel oneClassPanel = new JPanel();
        oneClassPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        oneClassClusterMean = new JTextField("0", 7);
        oneClassClusterMean.setSize(15, oneClassClusterMean.getHeight());
        oneClassSelector = new ClusterSelector(repository, 1);
        oneClassPanel.add(meanLabel,c);
        c.gridx = 1;
        oneClassPanel.add(oneClassClusterMean,c);
        c.weighty =1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_END;
        oneClassPanel.add(oneClassSelector,c);
        oneCPanel = new OneClassPanel();
        oneClassTab = new JTabbedPane();
        oneClassTab.add("Button Selection", oneCPanel);
        oneClassTab.add("Cluster Selection", oneClassPanel);
        oneClassTab.setSelectedIndex(1);
        if (repository==null||repository.isEmpty())
        	oneClassTab.setSelectedIndex(0);
        tabPane.add("One-Class", oneClassTab);
   
        buildConstraints(constraints, 0, 0, 1, 1, 100, 75);
        
        gridbag.setConstraints(tabPane, constraints);
        
        pane.add(tabPane);

        vPanel = new VersionPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        gridbag.setConstraints(vPanel, constraints);
        pane.add(vPanel);
        
        pPanel = new PermutationsPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 5);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        sqPanel = new S0AndQValueCalcPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 10);
        gridbag.setConstraints(sqPanel, constraints);
        pane.add(sqPanel);        
  
        iPanel = new ImputationPanel();
        buildConstraints(constraints, 0, 4, 1, 1, 0, 5);
        gridbag.setConstraints(iPanel, constraints);
        pane.add(iPanel);
        
        hclOpsPanel = new HCLSigOnlyPanel();
        buildConstraints(constraints, 0, 5, 1, 1, 0, 5);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);
        
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
    
    public boolean isSaveMatrix() {
        return iPanel.saveMatrixChkBox.isSelected();
    }
    
    class GroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        int numPanels = 0;
        JTextField[] timeField;
        JCheckBox[] startCB, endCB;
        JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
        JLabel lotsOfSamplesWarningLabel;
     
       GroupExperimentsPanel(Vector<String> exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            JPanel panel1 = new JPanel();
            expLabels = new JLabel[exptNames.size()];
            groupARadioButtons = new JRadioButton[exptNames.size()];
            groupBRadioButtons = new JRadioButton[exptNames.size()];
            neitherGroupRadioButtons = new JRadioButton[exptNames.size()];
            timeField = new JTextField[exptNames.size()];
            startCB = new JCheckBox[exptNames.size()];
            endCB = new JCheckBox[exptNames.size()];
            ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            panel1.setLayout(gridbag);
            
            numPanels = exptNames.size()/512 + 1;
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            
            for (int i = 0; i < exptNames.size(); i++) {
                String s1 = (String)(exptNames.get(i));
                expLabels[i] = new JLabel(s1);
                chooseGroup[i] = new ButtonGroup();
                groupARadioButtons[i] = new JRadioButton("Group A", true);
                chooseGroup[i].add(groupARadioButtons[i]);
                groupBRadioButtons[i] = new JRadioButton("Group B", false);
                chooseGroup[i].add(groupBRadioButtons[i]);
                neitherGroupRadioButtons[i] = new JRadioButton("Neither group", false);
                chooseGroup[i].add(neitherGroupRadioButtons[i]);
                timeField[i] = new JTextField("0.0");
                timeField[i].setVisible(false);
                startCB[i] = new JCheckBox("Start");
                startCB[i].setVisible(false);
                endCB[i] = new JCheckBox("End");
                endCB[i].setVisible(false);
                startCB[i].setActionCommand(String.valueOf(i));
                startCB[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                    	if (startCB[Integer.parseInt(evt.getActionCommand())].isSelected())
                    		endCB[Integer.parseInt(evt.getActionCommand())].setSelected(false);
                    }
                });

                endCB[i].setActionCommand(String.valueOf(i));
                endCB[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                    	if (endCB[Integer.parseInt(evt.getActionCommand())].isSelected())
                    		startCB[Integer.parseInt(evt.getActionCommand())].setSelected(false);
                    }
                });
//              set current panel
                currPanel = i / 512;
                
                buildConstraints(constraints, 0, i%512, 1, 1, 25, 100);
                gridbag.setConstraints(expLabels[i], constraints);
                panels[currPanel].add(expLabels[i]);
                
                buildConstraints(constraints, 1, i%512, 1, 1, 25, 100);
                gridbag.setConstraints(groupARadioButtons[i], constraints);
                panels[currPanel].add(groupARadioButtons[i]);
                
                buildConstraints(constraints, 2, i%512, 1, 1, 25, 100);
                gridbag.setConstraints(groupBRadioButtons[i], constraints);
                panels[currPanel].add(groupBRadioButtons[i]);
                
                buildConstraints(constraints, 3, i%512, 1, 1, 25, 100);
                gridbag.setConstraints(neitherGroupRadioButtons[i], constraints);
                panels[currPanel].add(neitherGroupRadioButtons[i]);    

                buildConstraints(constraints, 4, i%512, 1, 1, 25, 100);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(timeField[i], constraints);
                panels[currPanel].add(timeField[i]);  
                constraints.fill = GridBagConstraints.NONE;
                buildConstraints(constraints, 5, i%512, 1, 1, 25, 100);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(startCB[i], constraints);
                panels[currPanel].add(startCB[i]);  
                constraints.fill = GridBagConstraints.NONE;
                buildConstraints(constraints, 6, i%512, 1, 1, 25, 100);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(endCB[i], constraints);
                panels[currPanel].add(endCB[i]);  
                constraints.fill = GridBagConstraints.NONE;
            }
            
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.getHorizontalScrollBar().setUnitIncrement(20);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
            JLabel label1 = new JLabel("Note: Group A and Group B  MUST each contain more than one sample.");
            label1.setHorizontalAlignment(JLabel.CENTER);
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(label1, constraints);
            this.add(label1);
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	lotsOfSamplesWarningLabel.setBackground(Color.gray);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            
            JButton saveButton = new JButton("  Save grouping  ");
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            saveButton.setSize(180,30);
            saveButton.setPreferredSize(new Dimension(180,30));
            JButton loadButton = new JButton("  Load grouping  ");
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            loadButton.setPreferredSize(new Dimension(180,30));
            loadButton.setSize(180,30);
            JButton resetButton = new JButton("  Reset  ");
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            resetButton.setSize(180,30);
            resetButton.setPreferredSize(new Dimension(180,30));
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                        groupARadioButtons[i].setSelected(true);
                    }
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
            
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(5,5,5,5);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 10);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 10);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 33, 10);
            gridbag3.setConstraints(resetButton, constraints);
            panel2.add(resetButton);
            
            constraints.insets = new Insets(0,0,0,0);
            buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(panel2, constraints);
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
	   				pw.println("SAM: Two-Class Unpaired");
	       			pw.print("Group 1"+" Label:\t");
	   				pw.println("1");
	       			pw.print("Group 2"+" Label:\t");
	   				pw.println("2");
	   				
	   								
	   				pw.println("#");
	   				
	   				pw.println("Sample Index\tSample Name\tGroup Assignment");
	   				
	   				int[] groupAssgn = getGroupAssignments();
	   				
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
    	private void loadAssignments() {
    		
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
        						if (!lineArray[1].equals("SAM: Two-Class Unpaired")){
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
//        				System.out.println(exptNames.size()+"  "+sampleNames.size());
        				//status = "number-of-samples-mismatch";
//        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
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
        					setStateBasedOnIndex(groupAssignments,groupNames);
        					break;
        				}
        				
        				groupName = (String)(groupAssignments.get(fileSampleIndex));
        				groupIndex = groupNames.indexOf(groupName);
        				
        				
        				//set state
        				try{
	        				if (groupIndex==0)
	        					groupARadioButtons[sample].setSelected(true);
	        				if (groupIndex==1)
	        					groupBRadioButtons[sample].setSelected(true);
	        				if (groupIndex==2||groupIndex==-1)
	        					neitherGroupRadioButtons[sample].setSelected(true);
        				}catch (Exception e){
        					neitherGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
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
					"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
					"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
					optionst, optionst[0])==1)
				return;
			
    		for(int sample = 0; sample < exptNames.size(); sample++) {
    			//set state
				try{
    				if (groupNames.indexOf(groupAssignments.get(sample))==0)
    					groupARadioButtons[sample].setSelected(true);
    				if (groupNames.indexOf(groupAssignments.get(sample))==1)
    					groupBRadioButtons[sample].setSelected(true);
    				if (groupNames.indexOf(groupAssignments.get(sample))==2||groupNames.indexOf(groupAssignments.get(sample))==-1)
    					neitherGroupRadioButtons[sample].setSelected(true);
				}catch (Exception e){
					neitherGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
				}
    		}
    	}
       
        /*
         *  Resets group selections
         */
        private void reset(){
            for (int i = 0; i < exptNames.size(); i++) {
                groupARadioButtons[i].setSelected(true);
            }
        }
        
        
    }
    
    class CensoredSurvivalPanel extends JPanel {
        ExptTimeField[] fields;
        int numPanels = 0;
        JLabel lotsOfSamplesWarningLabel;
        
        CensoredSurvivalPanel(Vector<String> exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Time / State Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
           // JPanel panel1 = new JPanel();
            fields = new ExptTimeField[exptNames.size()];
            numPanels = exptNames.size()/512 + 1;
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
           // panel1.setLayout(gridbag);
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            
            for (int i = 0; i < exptNames.size(); i++) {
                String s1 = (String)(exptNames.get(i));
                fields[i] = new ExptTimeField(s1);
//              set current panel
                currPanel = i / 512;
            
                buildConstraints(constraints, 0, i%512, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].inAnalysisCheckBox, constraints);
               
                panels[currPanel].add(fields[i].inAnalysisCheckBox);
                
                buildConstraints(constraints, 1, i%512, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].expLabel, constraints);
                
                panels[currPanel].add(fields[i].expLabel);
                
                
                buildConstraints(constraints, 2, i%512, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(fields[i].timeLabel, constraints);
               
                panels[currPanel].add(fields[i].timeLabel);
                
                buildConstraints(constraints, 3, i%512, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(fields[i].timeInputField, constraints);
                
                panels[currPanel].add(fields[i].timeInputField);
                
                constraints.anchor = GridBagConstraints.CENTER;
                buildConstraints(constraints, 4, i%512, 1, 1, 20, 100);
                gridbag.setConstraints(fields[i].censoredRadioButton, constraints);
                panels[currPanel].add(fields[i].censoredRadioButton);
                
                buildConstraints(constraints, 5, i%512, 1, 1, 20, 100);
                gridbag.setConstraints(fields[i].deadRadioButton, constraints);
 
                panels[currPanel].add(fields[i].deadRadioButton);
                
            }
            
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.getHorizontalScrollBar().setUnitIncrement(20);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	lotsOfSamplesWarningLabel.setBackground(Color.gray);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
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
            
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                        fields[i].inAnalysisCheckBox.setSelected(true);
                        fields[i].timeInputField.setText("0.0");
                        fields[i].censoredRadioButton.setSelected(true);
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(CensoredSurvivalPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < finNum; i++) {
                                if (fields[i].inAnalysisCheckBox.isSelected()) {
                                    out.print(1);
                                    out.print("\t");
                                    if (fields[i].timeInputField.getText() == "") {
                                        out.print("0.0");
                                    } else {
                                        out.print(fields[i].timeInputField.getText());
                                    }
                                    out.print("\t");
                                    if (fields[i].censoredRadioButton.isSelected()) {
                                        out.print(1);
                                    } else {
                                        out.print(0);
                                    }
                                    //DONE UP TO HERE 3/20/03
                                } else {
                                    out.print(0);
                                }
                                out.println();
                            }
                            //out.println();
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
                    int returnVal = fc.showOpenDialog(CensoredSurvivalPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String currentLine;
                            Vector<String> inputVector = new Vector<String>();
                            //System.out.println(line);
                            //StringSplitter st = new StringSplitter('\t');
                            while((currentLine = buff.readLine()) != null) {
                                inputVector.add(currentLine);
                            }
                            buff.close();
                            for (int i = 0; i < inputVector.size(); i++) {
                                double[] currentSettings = getCurrentSettings((String)inputVector.get(i));
                                if (currentSettings[0] == 0) {
                                    fields[i].inAnalysisCheckBox.setSelected(false);
                                } else if (currentSettings[0] == 1) {
                                    fields[i].inAnalysisCheckBox.setSelected(true);
                                    fields[i].timeInputField.setText("" + currentSettings[1]);
                                    if (currentSettings[2] == 0) {
                                        fields[i].deadRadioButton.setSelected(true);
                                    } else if (currentSettings[2] == 1) {
                                        fields[i].censoredRadioButton.setSelected(true);
                                    } else {
                                        for (int k = 0; k < finNum; k++) {
                                            fields[k].inAnalysisCheckBox.setSelected(false);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                } else { // if currentSettings[0] != 0 or 1
                                    for (int k = 0; k < finNum; k++) {
                                        fields[k].inAnalysisCheckBox.setSelected(false);
                                    }
                                    JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                    break;
                                }
                            }
                            
                        } catch (Exception e) {
                            for (int k = 0; k < finNum; k++) {
                                fields[k].inAnalysisCheckBox.setSelected(false);
                            }
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            //e.printStackTrace();
                        }
                        
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
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
            buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
            constraints.anchor = GridBagConstraints.CENTER;
            gridbag2.setConstraints(panel2, constraints);
            this.add(panel2);
        }
        
        /*
         *  Resets valuse to default values
         */
        private void reset(){
            for (int i = 0; i < exptNames.size(); i++) {
                fields[i].inAnalysisCheckBox.setSelected(true);
                fields[i].timeInputField.setText("0.0");
                fields[i].censoredRadioButton.setSelected(true);
            }
        }
        
        /*
        private void addAnalysisCheckBoxListener(JCheckBox inAnalysisCheckBox, JLabel expLabel, JTextField timeInputField, JRadioButton censoredRadioButton, JRadioButton deadRadioButton, JLabel timeLabel) {
            inAnalysisCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        expLabel.setEnabled(false);
                        timeLabel.setEnabled(false);
                        timeInputField.setBackground(Color.darkGray);
                        timeInputField.setEnabled(false);
                        censoredRadioButton.setEnabled(false);
                        deadRadioButton.setEnabled(false);
         
                    }
         
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        expLabel.setEnabled(true);
                        timeLabel.setEnabled(false);
                        timeInputField.setBackground(Color.white);
                        timeInputField.setEnabled(true);
                        censoredRadioButton.setEnabled(true);
                        deadRadioButton.setEnabled(true);
                    }
         
                }
            });
        }
         */
        
        private class ExptTimeField {
            JCheckBox inAnalysisCheckBox;
            JLabel expLabel, timeLabel;
            JTextField timeInputField;
            JRadioButton censoredRadioButton, deadRadioButton;
            public ExptTimeField(String exptName) {
                inAnalysisCheckBox = new JCheckBox("", true);
                expLabel = new JLabel(exptName);
                timeLabel = new JLabel("Time: ");
                timeInputField = new JTextField("0.0", 7);
                censoredRadioButton = new JRadioButton("Censored", true);
                deadRadioButton = new JRadioButton("Dead", false);
                ButtonGroup chooseGroup = new ButtonGroup();
                chooseGroup.add(censoredRadioButton);
                chooseGroup.add(deadRadioButton);
                inAnalysisCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.DESELECTED) {
                            expLabel.setEnabled(false);
                            timeLabel.setEnabled(false);
                            timeInputField.setText("");
                            timeInputField.setBackground(Color.darkGray);
                            timeInputField.setEnabled(false);
                            censoredRadioButton.setEnabled(false);
                            deadRadioButton.setEnabled(false);
                            
                        }
                        
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            expLabel.setEnabled(true);
                            timeLabel.setEnabled(true);
                            timeInputField.setText("0.0");
                            timeInputField.setBackground(Color.white);
                            timeInputField.setEnabled(true);
                            censoredRadioButton.setEnabled(true);
                            deadRadioButton.setEnabled(true);
                        }
                        
                    }
                });
            }
            
        }
        
        private double[] getCurrentSettings(String currentLine) {
            double[] currentSettings;
            Vector<Double> currentVector = new Vector<Double>();
            StringSplitter st = new StringSplitter('\t');
            st.init(currentLine);
            
            while (st.hasMoreTokens()) {
                String current = st.nextToken();
                currentVector.add(new Double(current));
            }
            
            currentSettings = new double[currentVector.size()];
            for (int i = 0; i < currentSettings.length; i++) {
                currentSettings[i] = ((Double)currentVector.get(i)).doubleValue();
            }
            
            return currentSettings;
            
        }
        
        
    }
    
    class OneClassPanel extends JPanel {
        JTextField meanField;
        JCheckBox[] includeExpts, startCB, endCB;
        JTextField[] timefields;
        int numPanels = 0;
        JButton saveButton, loadButton, resetButton;
        JLabel lotsOfSamplesWarningLabel;
        
        OneClassPanel() {
            this.setBackground(Color.white);
            JLabel meanLabel = new JLabel("Enter the mean value to be tested against: ");
            meanField = new JTextField("0", 7);
            includeExpts = new JCheckBox[exptNames.size()];
            timefields = new JTextField[exptNames.size()];
            startCB = new JCheckBox[exptNames.size()];
            endCB = new JCheckBox[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            numPanels = exptNames.size()/512 + 1;
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            for (int i = 0; i < exptNames.size(); i++) {
                //set current panel
                currPanel = i / 512;
                
                includeExpts[i] = new JCheckBox((String)(exptNames.get(i)), true);
                buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(includeExpts[i], constraints);
                panels[currPanel].add(includeExpts[i]);
                
                timefields[i] = new JTextField("0.0");
                buildConstraints(constraints, 1, i%512, 1, 1, 100, 100);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(timefields[i], constraints);
                timefields[i].setVisible(false);
                panels[currPanel].add(timefields[i]);
                constraints.fill = GridBagConstraints.NONE;
                
                startCB[i] = new JCheckBox("Start");
                buildConstraints(constraints, 2, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(startCB[i], constraints);
                startCB[i].setVisible(false);
                panels[currPanel].add(startCB[i]);

                endCB[i] = new JCheckBox("End");
                buildConstraints(constraints, 3, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(endCB[i], constraints);
                endCB[i].setVisible(false);
                panels[currPanel].add(endCB[i]);
                
                startCB[i].setActionCommand(String.valueOf(i));
                startCB[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                    	if (startCB[Integer.parseInt(evt.getActionCommand())].isSelected())
                    		endCB[Integer.parseInt(evt.getActionCommand())].setSelected(false);
                    }
                });

                endCB[i].setActionCommand(String.valueOf(i));
                endCB[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                    	if (endCB[Integer.parseInt(evt.getActionCommand())].isSelected())
                    		startCB[Integer.parseInt(evt.getActionCommand())].setSelected(false);
                    }
                });
            }
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            
            JPanel enterMeanPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            enterMeanPanel.setLayout(grid2);        
            
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(meanLabel, constraints);
            enterMeanPanel.add(meanLabel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(meanField, constraints);
            enterMeanPanel.add(meanField);    
            
            JScrollPane scroll2 = new JScrollPane(enterMeanPanel);
            
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, scroll2);
            split.setOneTouchExpandable(true);
            split.setDividerLocation(300);
           
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
            gridbag.setConstraints(split, constraints);
            this.add(split);  
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	lotsOfSamplesWarningLabel.setBackground(Color.gray);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            
            JPanel lsrPanel = new JPanel();
            loadButton = new JButton("Load settings");
            saveButton = new JButton("Save settings");
            resetButton = new JButton("Reset");
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < includeExpts.length; i++) {
                        includeExpts[i].setSelected(true);
                    }
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
            
            
            GridBagLayout grid3 = new GridBagLayout();
            lsrPanel.setLayout(grid3);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            grid3.setConstraints(saveButton, constraints);
            lsrPanel.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            grid3.setConstraints(loadButton, constraints);
            lsrPanel.add(loadButton);            
            
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            grid3.setConstraints(resetButton, constraints);
            lsrPanel.add(resetButton);            
            
            //constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
            gridbag.setConstraints(lsrPanel, constraints);
            this.add(lsrPanel);            
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
    				pw.println("SAM: One Class");
        			pw.print("Group 1"+" Label:\t");
    				pw.println("Include");
    				
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");
    				
    				
    				for(int sample = 0; sample < exptNames.size(); sample++) {
    					pw.print(String.valueOf(sample+1)+"\t"); //sample index
    					pw.print(exptNames.get(sample)+"\t");
    					if (includeExpts[sample].isSelected())
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
        						if (!lineArray[1].equals("SAM: One Class")){
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
//        				System.out.println(exptNames.size()+"  "+sampleNames.size());
        				//status = "number-of-samples-mismatch";
//        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
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
        					setStateBasedOnIndex(groupAssignments,groupNames);
        					break;
        				}
        				
        				groupName = (String)(groupAssignments.get(fileSampleIndex));
        				groupIndex = groupNames.indexOf(groupName);
        				
        				
        				//set state
        				try{
	        				if (groupIndex==0)
	        					includeExpts[sample].setSelected(true);
	        				if (groupIndex==1||groupIndex==-1)
	        					includeExpts[sample].setSelected(false);
        				}catch (Exception e){
        					includeExpts[sample].setSelected(false);;  //set to last state... excluded
        				}
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
    			//set state
    			
    			try{
    				if (groupNames.indexOf(groupAssignments.get(sample))==0)
    					includeExpts[sample].setSelected(true);
    				if (groupNames.indexOf(groupAssignments.get(sample))==1||groupNames.indexOf(groupAssignments.get(sample))==-1)
    					includeExpts[sample].setSelected(false);
				}catch (Exception e){
					includeExpts[sample].setSelected(false);;  //set to last state... excluded
				}
    		}
    	}
        public void reset() {
            for (int i = 0; i < includeExpts.length; i++) {
                includeExpts[i].setSelected(true);
            }
            meanField.setText("0");
        }
    }    
    
    class TwoClassPairedMainPanel extends JPanel {
        TwoClassPairedPanel tcpPanel;
        JButton saveButton, resetButton, loadButton;
        GridBagConstraints constraints;
        GridBagLayout gridbag;  
        int dummy=0;
        JLabel lotsOfSamplesWarningLabel;
        
        public TwoClassPairedMainPanel() {
        	//if(exptNames.size()<11)
        		tcpPanel = new TwoClassPairedPanel();
        	//else
        		//tcpPanel = new TwoClassPairedPanel(dummy);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(Color.white);
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();   
            this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tcpPanel, constraints);
            this.add(tcpPanel);
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	lotsOfSamplesWarningLabel.setBackground(Color.gray);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            GridBagLayout grid1 = new GridBagLayout();
            bottomPanel.setLayout(grid1);
            
            saveButton = new JButton("Save pairings");
            
            final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
            
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(TwoClassPairedMainPanel.this); 
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile(); 
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currentA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currentB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                out.print(currentA);
                                out.print("\t");
                                out.print(currentB);
                                out.print("\t");
                                out.println();
                            }
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            });
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            grid1.setConstraints(saveButton, constraints);
            bottomPanel.add(saveButton);       
            
            loadButton = new JButton("Load pairings");
            
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    tcpPanel.reset();
                    int returnVal = fc.showOpenDialog(TwoClassPairedMainPanel.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String currentLine;  
                            while((currentLine = buff.readLine()) != null) {
                                StringSplitter st = new StringSplitter('\t');
                                st.init(currentLine);
                                
                                for (int i = 0; i < 2; i++) {
                                    String s = st.nextToken();
                                    if (i == 0) {
                                        tcpPanel.pairedAExpts.add(new Integer(s));
                                    } else if (i == 1) {
                                        tcpPanel.pairedBExpts.add(new Integer(s));
                                    }
                                }
                                
                            }
                            buff.close();
                            
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                String currPair = "A: " + (String)(exptNames.get(currA)) + " - B: " + (String)(exptNames.get(currB));
                              //  tcpPanel.exptButtons[currA].setEnabled(false);
                               // tcpPanel.exptButtons[currB].setEnabled(false); 
                                tcpPanel.pairedListModel.addElement(currPair);
                                
                            }
                            
                            if (tcpPanel.pairedAExpts.size() > 0) {
                                tcpPanel.removeABPairButton.setEnabled(true);
                                tcpPanel.pairedExptsList.setSelectedIndex(tcpPanel.pairedListModel.size() - 1);
                            }
                            
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(tcpPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);                            
                        }
                    } else {
                    }
                }
            });
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 100);
            grid1.setConstraints(loadButton, constraints);
            bottomPanel.add(loadButton);     
            
            resetButton = new JButton("Reset");
            
            resetButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    tcpPanel.reset();
                }
            });
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 100);
            grid1.setConstraints(resetButton, constraints);
            bottomPanel.add(resetButton);    
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(bottomPanel, constraints);
            this.add(bottomPanel);             
        }
    }
    
    class TwoClassPairedPanel extends JPanel {
        ExperimentButton[] exptButtons;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JTextField currentATextField, currentBTextField;
        JButton removeCurrentAButton, removeCurrentBButton, loadABPairButton, removeABPairButton;
        PairedExperimentsPanel pairPanel;
        JList pairedExptsList;
        DefaultListModel pairedListModel;
        boolean currentAFilled, currentBFilled;
        int currentAExpt, currentBExpt;
        Vector<Integer> pairedAExpts, pairedBExpts;
      
        public TwoClassPairedPanel() {
            currentAExpt = -1;
            currentBExpt = -1;
            int numPanels = 0;
            currentAFilled = false;
            currentBFilled = false;
            pairedAExpts = new Vector<Integer>();
            pairedBExpts = new Vector<Integer>();
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            //this.setBackground(Color.white);
            this.setLayout(gridbag);  
            /*
            currentATextField = new JTextField("", 10);
            currentBTextField = new JTextField("", 10);
            currentATextField.setBackground(Color.white);
            currentBTextField.setBackground(Color.white);
            currentATextField.setEditable(false);
            currentBTextField.setEditable(false);   
             */      
            
            pairedListModel = new DefaultListModel();
            pairedExptsList = new JList(pairedListModel);
            
            /*JPanel exptNamesPanel = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            exptNamesPanel.setLayout(grid1);
           */
            exptButtons = new ExperimentButton[exptNames.size()];
            
            //wwang add for fixing 512 problem
            numPanels = exptNames.size()/512 + 1;
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            
            int maxWidth = 0,i=0;
            int maxNameLength = 0;
           
            for ( i = 0; i <exptNames.size() ; i++) {
                //String s = (String)(exptNames.get(i));
                exptButtons[i] = new ExperimentButton(i);
                
                if (exptButtons[i].getPreferredSize().getWidth() > maxWidth) {
                    maxWidth = (int)Math.ceil(exptButtons[i].getPreferredSize().getWidth());
                }
                
                String s = (String)(exptNames.get(i));
                int currentNameLength = s.length();
                
                if (currentNameLength > maxNameLength) {
                    maxNameLength = currentNameLength;
                }
                
//              set current panel
                currPanel = i / 512;
 
                buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(exptButtons[i], constraints);
                panels[currPanel].add(exptButtons[i]);
               // if(i<maxButton)
               // exptNamesPanel.add(exptButtons[i]);
            }
            
            currentATextField = new JTextField("", maxNameLength + 2);
            currentBTextField = new JTextField("", maxNameLength + 2);
            //currentATextField.setSize(maxWidth + 5, 80);
            //currentBTextField.setSize(maxWidth + 5, 80);
            //currentATextField.setPreferredSize(new Dimension(maxWidth + 5, 80));
            //currentBTextField.setPreferredSize(new Dimension(maxWidth + 5, 80));
            
            currentATextField.setBackground(Color.white);
            currentBTextField.setBackground(Color.white);
            currentATextField.setEditable(false);
            currentBTextField.setEditable(false);   
            
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int m = 0; m < numPanels; m++) {
                bigPanel.add(panels[m] ,new GridBagConstraints(0,m,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);            
            scroll.getHorizontalScrollBar().setUnitIncrement(20);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            
            buildConstraints(constraints, 0, 0, 1, 1, 40, 100);
            constraints.fill =GridBagConstraints.BOTH;
            gridbag.setConstraints(scroll, constraints);
            this.add(scroll);
            
            constraints.fill = GridBagConstraints.NONE;
            
            JPanel currentSelectionPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            currentSelectionPanel.setLayout(grid2);
            removeCurrentAButton = new JButton("< Remove A");
            removeCurrentBButton = new JButton("< Remove B");
            loadABPairButton = new JButton("   Load Pair >>   ");
            removeABPairButton = new JButton("<< Remove Pair");
            removeCurrentAButton.setEnabled(false);
            removeCurrentBButton.setEnabled(false);
            loadABPairButton.setEnabled(false); 
            removeABPairButton.setEnabled(false);
            removeCurrentAButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentAExpt].setEnabled(true);
                    currentAExpt = -1;
                    currentATextField.setText("");
                    currentAFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);
                }
            });
            
            removeCurrentBButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentBExpt].setEnabled(true);
                    currentBExpt = -1;
                    currentBTextField.setText("");
                    currentBFilled = false;
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);                    
                }
            });     
            
            loadABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String currentPair = "A: " + (String)(exptNames.get(currentAExpt)) + " - B: " + (String)(exptNames.get(currentBExpt));
                    pairedListModel.addElement(currentPair);
                    pairedAExpts.add(new Integer(currentAExpt));
                    pairedBExpts.add(new Integer(currentBExpt));
                    currentAExpt = -1;
                    currentBExpt = -1;
                    currentATextField.setText("");
                    currentBTextField.setText("");
                    currentAFilled = false;
                    currentBFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false); 
                    removeABPairButton.setEnabled(true);
                    pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                }
            });
            
            removeABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    int index = pairedExptsList.getSelectedIndex();
                    pairedListModel.removeElementAt(index);
                    int removedAIndex = ((Integer)(pairedAExpts.remove(index))).intValue();
                    int removedBIndex = ((Integer)(pairedBExpts.remove(index))).intValue();
                    exptButtons[removedAIndex].setEnabled(true);
                    exptButtons[removedBIndex].setEnabled(true);
                    if (pairedListModel.isEmpty()) {
                        removeABPairButton.setEnabled(false);
                    } else {
                        pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                    }
                }
            });

            JScrollPane currentAScroll = new JScrollPane(currentATextField);
            currentAScroll.setMinimumSize(new Dimension(90, 50));
            JScrollPane currentBScroll = new JScrollPane(currentBTextField);
            currentBScroll.setMinimumSize(new Dimension(90, 50));
            
            currentAScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentAScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);   
            
            currentAScroll.getHorizontalScrollBar().setUnitIncrement(20);
            currentAScroll.getVerticalScrollBar().setUnitIncrement(20);
            
            currentBScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentBScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);            
            
            currentBScroll.getHorizontalScrollBar().setUnitIncrement(20);
            currentBScroll.getVerticalScrollBar().setUnitIncrement(20);
            
            buildConstraints(constraints, 0, 0, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentAButton, constraints);
            currentSelectionPanel.add(removeCurrentAButton);
            
            JLabel aLabel = new JLabel(" Current A: ");
            buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
            grid2.setConstraints(aLabel, constraints);
            currentSelectionPanel.add(aLabel);    
            
            buildConstraints(constraints, 2, 0, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentAScroll, constraints);
            currentSelectionPanel.add(currentAScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 0, 1, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentBButton, constraints);
            currentSelectionPanel.add(removeCurrentBButton);   
            
            JLabel bLabel = new JLabel("Current B: ");
            buildConstraints(constraints, 1, 1, 1, 1, 20, 0);
            grid2.setConstraints(bLabel, constraints);
            currentSelectionPanel.add(bLabel);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentBScroll, constraints);
            currentSelectionPanel.add(currentBScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 1, 0, 1, 1, 10, 0);
            //constraints.fill = GridBagConstraints.HORIZONTAL;
            //constraints.ipadx = 200;
            gridbag.setConstraints(currentSelectionPanel, constraints);
            this.add(currentSelectionPanel);   
            
            constraints.fill = GridBagConstraints.NONE;
            //constraints.ipadx = 0;
            
            JPanel pairButtonsPanel = new JPanel();
            GridBagLayout grid3 = new GridBagLayout();
            pairButtonsPanel.setLayout(grid3);

            buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
            grid3.setConstraints(loadABPairButton, constraints);
            pairButtonsPanel.add(loadABPairButton);

            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            grid3.setConstraints(removeABPairButton, constraints);
            pairButtonsPanel.add(removeABPairButton);            
            
            buildConstraints(constraints, 2, 0, 1, 1, 5, 0);
            gridbag.setConstraints(pairButtonsPanel, constraints);
            this.add(pairButtonsPanel);  
            
            //pairPanel = new PairedExperimentsPanel();
            buildConstraints(constraints, 3, 0, 1, 1, 45, 0);
            constraints.fill = GridBagConstraints.BOTH;
            JScrollPane pairScroll = new JScrollPane(pairedExptsList);
            pairScroll.setBorder(new TitledBorder("Paired Samples"));
            gridbag.setConstraints(pairScroll, constraints);
            this.add(pairScroll);              
            
        }
        
     
        public void reset() {
        	
            for (int i = 0; i < exptButtons.length; i++) {
                exptButtons[i].setEnabled(true);
                currentATextField.setText("");
                currentBTextField.setText("");
                removeCurrentAButton.setEnabled(false);
                removeCurrentBButton.setEnabled(false);
                loadABPairButton.setEnabled(false);
                removeABPairButton.setEnabled(false);
                pairedListModel.clear();
                currentAFilled = false;
                currentBFilled = false;
                currentAExpt = -1;
                currentBExpt = -1;
                pairedAExpts.clear();
                pairedBExpts.clear();
            }
        }
        
        class ExperimentButton extends JButton {
            String s;
            int index;
            public ExperimentButton(int i) {
                this.index = i;
                s = (String)(exptNames.get(i));
                this.setText(s);
                this.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if ((currentAFilled)&&(currentBFilled)) {
                            JOptionPane.showMessageDialog(null, "Clear at least one current field first!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (!currentAFilled) {
                            currentAExpt = index;
                            currentATextField.setText(s);
                            currentAFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentAButton.setEnabled(true);
                        } else if (!currentBFilled) {
                            currentBExpt = index;
                            currentBTextField.setText(s);
                            currentBFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentBButton.setEnabled(true);
                        }
                        
                        if ((currentAFilled) && (currentBFilled)) {
                            loadABPairButton.setEnabled(true);
                        } else {
                            loadABPairButton.setEnabled(false);
                        }
                    }
                });
            }
        }
        
        class PairedExperimentsPanel extends JPanel {
            public PairedExperimentsPanel() {
                //this.setBorder(new TitledBorder("Paired Experiments"));
            }
        }
    }
    
    
    class MultiClassPanel extends JPanel {
        NumGroupsPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        JTabbedPane multiClassTab;
        MultiGroupExperimentsPanel mulgPanel;
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
                        	multiClassTab = new JTabbedPane();
                            mulgPanel = new MultiGroupExperimentsPanel(exptNames, numGroups);
                            
                            //dummyButton.setVisible(true);
                            MultiClassPanel.this.remove(dummyPanel);
                            multiClassTab.add("Button Selection", mulgPanel);
                            multiClassSelector = new ClusterSelector(repository, numGroups);
                            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
                            constraints.fill = GridBagConstraints.BOTH;
                            gridbag.setConstraints(multiClassTab, constraints);
                            multiClassTab.add("Cluster Selection", multiClassSelector);

                            multiClassTab.setSelectedIndex(1);
                            if (repository==null||repository.isEmpty())
                            	multiClassTab.setSelectedIndex(0);
                            MultiClassPanel.this.add(multiClassTab);
                            MultiClassPanel.this.validate();
                            ngPanel.okButton.setEnabled(false);
                            ngPanel.numGroupsField.setEnabled(false);
                        }
                       
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please enter a positive integer >= 2!", "Error", JOptionPane.ERROR_MESSAGE);
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
                //super(parentFrame, "Multiclass design: number of groups", modality);
                //setBounds(0, 0, 350, 150);
                //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                /*
                okButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        okPressed = true;
                        //dispose();
                    }
                });
                 */
                this.add(okButton);
                /*
                cancelButton = new JButton("Cancel");
                buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
                gridbag.setConstraints(cancelButton, constraints);
                cancelButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        okPressed = false;
                        dispose();
                    }
                });
                pane.add(cancelButton);
                 */
                
                //setContentPane(pane);
                
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
            JLabel[] expLabels;
            JRadioButton[][] exptGroupRadioButtons;
            JRadioButton[] notInGroupRadioButtons;
            int numPanels = 0;
            JLabel lotsOfSamplesWarningLabel;
            MultiGroupExperimentsPanel(Vector<String> exptNames, int numGroups) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments"));
                setBackground(Color.white);
               // JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                
                exptGroupRadioButtons = new JRadioButton[numGroups][exptNames.size()];
                notInGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);
                //wwang fix 512 problem
                numPanels = exptNames.size()/512 + 1;
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
                    }
                    
                    buildConstraints(constraints, (numGroups + 1), i%512, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(notInGroupRadioButtons[i], constraints);
                    panels[currPanel].add(notInGroupRadioButtons[i]);
 
                }
                
                int maxLabelWidth = 0;
                
                for (int j= 0; j < expLabels.length; j++) {
                    if (expLabels[j].getPreferredSize().getWidth() > maxLabelWidth) {
                        maxLabelWidth = (int)Math.ceil(expLabels[j].getPreferredSize().getWidth());
                    }
                }
                
                JPanel bigPanel = new JPanel(new GridBagLayout());
                
                for(int m = 0; m< numPanels; m++) {
                    bigPanel.add(panels[m] ,new GridBagConstraints(0,m,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
                }
                JScrollPane scroll = new JScrollPane(bigPanel);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                scroll.getHorizontalScrollBar().setUnitIncrement(20);
                scroll.getVerticalScrollBar().setUnitIncrement(20);
                //add by wwang
                JPanel [] exptNameHeaderPanels = new JPanel[this.numPanels];
                GridBagLayout exptHeaderGridbag = new GridBagLayout();
                
                for(int i = 0; i< exptNameHeaderPanels.length; i++) {
                    exptNameHeaderPanels[i] = new JPanel();
                    exptNameHeaderPanels[i].setSize(50, panels[i].getPreferredSize().height);
                    exptNameHeaderPanels[i].setPreferredSize(new Dimension(maxLabelWidth + 10, panels[i].getPreferredSize().height));
                    exptNameHeaderPanels[i].setLayout(exptHeaderGridbag);
                }
 
                //need to possibly add to additional panels if number of exp. excedes 512
                for (int m = 0; m < expLabels.length; m++) {
                    currPanel = m/512;
                    buildConstraints(constraints, 0, m%512, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    exptHeaderGridbag.setConstraints(expLabels[m], constraints);
                    exptNameHeaderPanels[currPanel].add(expLabels[m]);
                }

               JPanel headerPanel = new JPanel(new GridBagLayout());
                for(int k = 0; k < exptNameHeaderPanels.length; k++) {
                    headerPanel.add(exptNameHeaderPanels[k], new GridBagConstraints(0,k,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0)); 
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
                
                if(lotsOfSamples){
                	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
                	lotsOfSamplesWarningLabel.setBackground(Color.gray);
                	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
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
                buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
                constraints.anchor = GridBagConstraints.CENTER;
                gridbag2.setConstraints(panel2, constraints);
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
        				pw.println("SAM: Multi-Class");
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
	        						if (!lineArray[1].equals("SAM: Multi-Class")){
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
//	        				System.out.println(exptNames.size()+"  "+sampleNames.size());
	        				//status = "number-of-samples-mismatch";
//	        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
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
            private void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                    exptGroupRadioButtons[0][i].setSelected(true);
                }
            }
        }
        private void reset(){
            mulgPanel.reset();
        }
    }
    class VersionPanel extends JPanel {
        //JLabel permsInfoLabel;
        //JRadioButton useAllPermsButton, numPermsButton;
        JLabel numPermsLabel;
        JCheckBox useRSAM,timecourseCB;
        JRadioButton versionThree;
        ButtonGroup bg = new ButtonGroup();
        VersionPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "R settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            numPermsLabel = new JLabel("Enter number of permutations:   ");
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            useRSAM = new JCheckBox("Use R");
            useRSAM.setBackground(Color.white);
            useRSAM.setSelected(false);
            useRSAM.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                	if (!useRSAM.isSelected()){
                		timecourseCB.setSelected(false);
                		setTimeCourse(false);
                	}
                	timecourseCB.setEnabled(useRSAM.isSelected());
                }
            });
            buildConstraints(constraints, 0, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(useRSAM, constraints);
            this.add(useRSAM);
            timecourseCB = new JCheckBox("Time-Course Data");
            timecourseCB.setBackground(Color.white);
            timecourseCB.setSelected(false);
            timecourseCB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                	setTimeCourse(timecourseCB.isSelected());
                }
            });
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(timecourseCB, constraints);
            this.add(timecourseCB);
            
        }
        
        public void reset(){

        }
        
    }


	private void setTimeCourse(boolean set) {
		for (int i=0; i<gPanel.timeField.length; i++){
			gPanel.timeField[i].setVisible(set);
			gPanel.startCB[i].setVisible(set);
			gPanel.endCB[i].setVisible(set);
			oneCPanel.timefields[i].setVisible(set);
			oneCPanel.startCB[i].setVisible(set);
			oneCPanel.endCB[i].setVisible(set);
		}
		this.unpairedTab.setSelectedIndex(0);
		this.unpairedTab.setEnabledAt(1, !set);
		this.oneClassTab.setSelectedIndex(0);
		this.oneClassTab.setEnabledAt(1, !set);
		
		this.gPanel.setEnabled(!set);
		if (tabPane.getSelectedIndex()==1||tabPane.getSelectedIndex()==2||tabPane.getSelectedIndex()==3)
			tabPane.setSelectedIndex(0);
		this.tabPane.setEnabledAt(1, !set);
		this.tabPane.setEnabledAt(2, !set);
		this.tabPane.setEnabledAt(3, !set);
		
		gPanel.updateUI();
		gPanel.repaint();
		
	}
    class PermutationsPanel extends JPanel {
        //JLabel permsInfoLabel;
        //JRadioButton useAllPermsButton, numPermsButton;
        JLabel numPermsLabel;
        JTextField numPermsInputField;
        PermutationsPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Number of permutations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            numPermsLabel = new JLabel("Enter number of permutations:   ");
            numPermsInputField = new JTextField("100", 7);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(numPermsInputField, constraints);
            this.add(numPermsInputField);
           
        }
        
        public void reset(){
            this.numPermsInputField.setText("100");
        }
        
    }
    
    class S0AndQValueCalcPanel extends JPanel {
        JComboBox s0SelectBox;
        JTextField s0EntryField;
        JRadioButton s0SelectButton, s0EntryButton, qYesButton, qNoButton;
        
        S0AndQValueCalcPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "S0 and Q Value parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));            
            String[] s0SelectOptions = {"Tusher et al. method", "5th percentile", "50th percentile", "90th percentile", "Minimum S Value"};
            setBackground(Color.white);
            s0SelectBox = new JComboBox(s0SelectOptions);
            //s0SelectBox.setBackground(Color.white);
            s0SelectButton = new JRadioButton("Select S0 using", true);
            s0SelectButton.setBackground(Color.white);
            s0EntryButton = new JRadioButton(" OR Enter s0 percentile (0-100)", false);
            s0EntryButton.setBackground(Color.white);
            s0EntryField = new JTextField("", 7);
            s0EntryField.setEnabled(false);
            s0EntryField.setBackground(Color.darkGray);         
            ButtonGroup s0Group = new ButtonGroup();
            s0SelectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    s0SelectBox.setEnabled(true);
                    s0EntryField.setText("");
                    s0EntryField.setEnabled(false);
                    s0EntryField.setBackground(Color.darkGray);
                }
            });
            s0EntryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    s0EntryField.setText("10");
                    s0EntryField.setEnabled(true);
                    s0EntryField.setBackground(Color.white);
                    s0SelectBox.setEnabled(false);
                }
            });            
            s0Group.add(s0SelectButton);
            s0Group.add(s0EntryButton);
            
            qYesButton = new JRadioButton("Yes (slow!)", false);
            qYesButton.setBackground(Color.white);
            qNoButton = new JRadioButton("No (quick)", true);
            qNoButton.setBackground(Color.white);
            ButtonGroup qGroup = new ButtonGroup();
            qGroup.add(qYesButton);
            qGroup.add(qNoButton);
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);

            buildConstraints(constraints, 0, 0, 1, 1, 25, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(s0SelectButton, constraints);
            this.add(s0SelectButton);            
            
            buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(s0SelectBox, constraints);
            this.add(s0SelectBox);  
            
            buildConstraints(constraints, 2, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(s0EntryButton, constraints);
            this.add(s0EntryButton);   
            
            
            buildConstraints(constraints, 3, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(s0EntryField, constraints);
            this.add(s0EntryField);   
            
            JLabel qLabel = new JLabel("Calculate q-values?  ");
            buildConstraints(constraints, 0, 1, 1, 1, 10, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(qLabel, constraints);
            this.add(qLabel);   
            
            buildConstraints(constraints, 1, 1, 1, 1, 10, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(qNoButton, constraints);
            this.add(qNoButton);
            
            buildConstraints(constraints, 2, 1, 2, 1, 80, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(qYesButton, constraints);
            this.add(qYesButton);             
            
        }
        
        public void reset() {
            s0SelectButton.setSelected(true);
            s0SelectBox.setEnabled(true);
            s0SelectBox.setSelectedIndex(0);
            s0EntryField.setText("");
            s0EntryField.setBackground(Color.darkGray);
            s0EntryField.setEnabled(false);
            qNoButton.setSelected(true);
        }
    }
    private void getTCPRVector(){
//    	for (int i=0; i<tcpmPanel.tcpPanel.pairedBExpts.size(); i++){
//    		System.out.print(tcpmPanel.tcpPanel.pairedAExpts.get(i)+"-");
//    		System.out.println(tcpmPanel.tcpPanel.pairedBExpts.get(i));
//    	}
    	Vector<Integer> rpair = new Vector<Integer>();
//    	int index = 0;
    	for (int i=0; i<exptNames.size(); i++){
    		int found = tcpmPanel.tcpPanel.pairedAExpts.indexOf(i);
    		if (found!=-1){
    			rpair.add(found+1);
    		} else {
    			found = tcpmPanel.tcpPanel.pairedBExpts.indexOf(i);
        		if (found!=-1)
        			rpair.add(-found-1);
    		}
    	}
//    	for (int i=0; i<rpair.size(); i++){
//    		System.out.print(rpair.get(i)+", ");
//    	}
    }
    class ImputationPanel extends JPanel {
        JRadioButton kNearestButton, rowAverageButton;
        JTextField numNeighborsField;
        JLabel numNeighborsLabel;
        String numNeighborsText;
        JCheckBox saveMatrixChkBox;
        ImputationPanel() {
            //           this.setBorder(new TitledBorder(new EtchedBorder(), "Imputation Engine"));
            this.setBorder(new TitledBorder(new EtchedBorder(), "Imputation Engine", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            numNeighborsText = "10";
            if (numGenes < 10) {
                numNeighborsText = Integer.toString(numGenes);
            }
            numNeighborsField = new JTextField(numNeighborsText, 7);
            numNeighborsLabel = new JLabel("Number of neighbors:       ");
            kNearestButton = new JRadioButton("K-nearest neighbors imputer", true);
            kNearestButton.setBackground(Color.white);
            kNearestButton.setFocusPainted(false);
            kNearestButton.setForeground(UIManager.getColor("Label.foreground"));
            rowAverageButton = new JRadioButton("Row average imputer              ", false);
            rowAverageButton.setBackground(Color.white);
            rowAverageButton.setFocusPainted(false);
            rowAverageButton.setForeground(UIManager.getColor("Label.foreground"));
            kNearestButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numNeighborsLabel.setEnabled(true);
                    numNeighborsField.setEnabled(true);
                    numNeighborsField.setBackground(Color.white);
                    numNeighborsField.setText(numNeighborsText);
                }
            });
            
            rowAverageButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numNeighborsLabel.setEnabled(false);
                    numNeighborsField.setEnabled(false);
                    numNeighborsField.setBackground(Color.gray);
                    numNeighborsField.setText("");
                }
            });
            ButtonGroup chooseKOrRowAvg = new ButtonGroup();
            chooseKOrRowAvg.add(kNearestButton);
            chooseKOrRowAvg.add(rowAverageButton);
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            //buildConstraints(constraints, 0, 0, 1, 1, 100, 25);
            buildConstraints(constraints, 0, 0, 1, 1, 40, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(kNearestButton, constraints);
            this.add(kNearestButton);
            
            
            
            buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numNeighborsLabel, constraints);
            this.add(numNeighborsLabel);
            
            
            buildConstraints(constraints, 2, 0, 1, 1, 30, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(numNeighborsField, constraints);
            this.add(numNeighborsField);
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(rowAverageButton, constraints);
            this.add(rowAverageButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 0, 50);
            //  constraints.anchor = GridBagConstraints.EAST;
            JPanel dummy = new JPanel();
            dummy.setBackground(Color.white);
            gridbag.setConstraints(dummy, constraints);
            this.add(dummy);
            
            buildConstraints(constraints, 2, 1, 1, 1, 0, 50);
            //  constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(dummy, constraints);
            this.add(dummy);
            
            
            constraints.anchor = GridBagConstraints.CENTER;
            buildConstraints(constraints, 0, 2, 3, 1, 0, 50);
            saveMatrixChkBox = new JCheckBox("Save Imputed Matrix", false);
            saveMatrixChkBox.setBackground(Color.white);
            saveMatrixChkBox.setFocusPainted(false);
            saveMatrixChkBox.setForeground(UIManager.getColor("Label.foreground"));
            gridbag.setConstraints(saveMatrixChkBox, constraints);
            this.add(saveMatrixChkBox);
        }
        
        private void reset(){
            this.numNeighborsField.setText(this.numNeighborsText);
            this.numNeighborsField.setBackground(Color.white);
            this.numNeighborsField.setEnabled(true);
            this.kNearestButton.setSelected(true);
            this.saveMatrixChkBox.setSelected(false);
        }
    }
    
    
    class OKCancelPanel extends JPanel {
        JButton okButton, cancelButton;
        JCheckBox drawTreesBox;
        
        OKCancelPanel() {
            okButton = new JButton("OK");
            cancelButton = new JButton("Cancel");
            drawTreesBox = new JCheckBox("Draw Hierarchical Trees");
            
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                        // if (evt.getSource() == okButton) {
                        int[] grpAssignments = getGroupAssignments();
                        int grpACounter = 0;
                        int grpBCounter = 0;
                        for (int i = 0; i < grpAssignments.length; i++) {
                            if (grpAssignments[i] == GROUP_A) {
                                grpACounter++;
                            } else if (grpAssignments[i] == GROUP_B) {
                                grpBCounter++;
                            }
                        }
                        if ((grpACounter < 2) || (grpBCounter < 2)) {
                            JOptionPane.showMessageDialog(null, "Group A and Group B must contain more than one sample", "Error", JOptionPane.WARNING_MESSAGE);
                        } /*else if ((Integer.parseInt(iPanel.numNeighborsField.getText()) > numGenes) || (Integer.parseInt(iPanel.numNeighborsField.getText()) <= 0)) {
                            JOptionPane.showMessageDialog(null, "Number of neighbors must be  > 0, and <= the total number of genes", "Error", JOptionPane.WARNING_MESSAGE);
                        } else if (Integer.parseInt(pPanel.numPermsInputField.getText()) < 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations must be > 0", "Error", JOptionPane.WARNING_MESSAGE);
                        } */else {
                            try {
                                getUserNumCombs();
                                //}
                                
                                if (useKNearest()) {
                                    getNumNeighbors();
                                }
                                okPressed = true;
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            
                            //okPressed = true;
                            //hide();
                            //dispose();
                        }
                        // }
                    } else if (getStudyDesign() == SAMInitDialog.MULTI_CLASS) {
                        boolean tooFew = false;
                        int[] grpAssignments = getGroupAssignments();
                        int numGroups = getMultiClassNumGroups();
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
                        
                        /*
                        for (int i = 0; i < grpAssignments.length; i++) {
                            System.out.println("grpAssignments[" + i + "] = "  + grpAssignments[i]);
                        }
                         
                        for (int i = 0; i < groupSize.length; i++) {
                            System.out.println("groupSize[" + i + "] = " + groupSize[i] + " (group " + (i +1) + ")");
                        }
                         */
                        
                        for (int i = 0; i < groupSize.length; i++) {
                            if (groupSize[i] <= 1) {
                                JOptionPane.showMessageDialog(null, "Each group must contain more than one sample.", "Error", JOptionPane.WARNING_MESSAGE);
                                tooFew = true;
                                break;
                            }
                        }
                        
                        if (!tooFew) {
                            try {
                                getUserNumCombs();
                                //}
                                
                                if (useKNearest()) {
                                    getNumNeighbors();
                                }
                                okPressed = true;
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else if (getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL) {
                        try {
                            boolean allSame = true;
                            int selectedCounter = 0;
                            double previousTime = 0;
                            for (int i = 0; i < exptNames.size(); i++) {
                                if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                                    selectedCounter++;
                                    double d = (new Double(csPanel.fields[i].timeInputField.getText())).doubleValue();
                                    if (i > 0) {
                                        if (previousTime != d) {
                                            allSame = false;
                                        }
                                    }
                                    previousTime = d;
                                }
                            }
                            getUserNumCombs();
                            //}
                            
                            if (useKNearest()) {
                                getNumNeighbors();
                            }
                            if (selectedCounter < 2) {
                                JOptionPane.showMessageDialog(null, "At least 2 samples must be selected!", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (allSame) {
                                JOptionPane.showMessageDialog(null, "At least one of the survival time values must be different from the rest!", "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                okPressed = true;
                                dispose();
                            }
                        } catch (NumberFormatException nfe){
                            JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //  if (evt.getSource() == cancelButton) {
                    okPressed = false;
                    //hide();
                    dispose();
                    // }
                }
            });
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            //constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 2, 1, 0, 50);
            gridbag.setConstraints(drawTreesBox, constraints);
            this.add(drawTreesBox);
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
            //constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(okButton, constraints);
            this.add(okButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            //constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(cancelButton, constraints);
            this.add(cancelButton);
        }
    }
    public int getTestDesign() {
    	int testDesign = SAMInitDialog.BUTTON_SELECTION;
    	if (tabPane.getSelectedIndex() == 0) {
            if (unpairedTab.getSelectedIndex()==1)
            	testDesign = SAMInitDialog.CLUSTER_SELECTION;
    	}else if (tabPane.getSelectedIndex() == 2) {
            if (mPanel.multiClassTab.getSelectedIndex()==1)
            	testDesign = SAMInitDialog.CLUSTER_SELECTION;
    	}else if (tabPane.getSelectedIndex() == 4) {
            if (oneClassTab.getSelectedIndex()==1)
            	testDesign = SAMInitDialog.CLUSTER_SELECTION;
    	}
    	return testDesign;
    }
    
    public int getStudyDesign() {
        int studyDesign = TWO_CLASS_UNPAIRED;
        if (tabPane.getSelectedIndex() == 0) {
            studyDesign = TWO_CLASS_UNPAIRED;
        } else if (tabPane.getSelectedIndex() == 1) {
            studyDesign = SAMInitDialog.TWO_CLASS_PAIRED;
        } else if (tabPane.getSelectedIndex() == 2) {
            studyDesign = SAMInitDialog.MULTI_CLASS;
        } else if (tabPane.getSelectedIndex() == 3) {
            studyDesign = SAMInitDialog.CENSORED_SURVIVAL;
        } else if (tabPane.getSelectedIndex() == 4) {
            studyDesign = SAMInitDialog.ONE_CLASS;
        }
        return studyDesign;
    }

    public int[] getStartEnd(){
    	int[] tc = new int[exptNames.size()];
    	if (getStudyDesign() == TWO_CLASS_UNPAIRED){
    		for (int i=0; i<tc.length; i++){
    			if (gPanel.startCB[i].isSelected())
    				tc[i] = 1;
    			else if (gPanel.endCB[i].isSelected())
    				tc[i] = 2;
    			else
    				tc[i] = 0;
    		}
    	} else if (getStudyDesign() == ONE_CLASS){
    		for (int i=0; i<tc.length; i++){
    			if (oneCPanel.startCB[i].isSelected())
    				tc[i] = 1;
    			else if (oneCPanel.endCB[i].isSelected())
    				tc[i] = 2;
    			else
    				tc[i] = 0;
    		}
    	}
    	return tc;
    }
    public float[] getTimeCourse(){
    	float[] tc = new float[exptNames.size()];
    	if (getStudyDesign() == TWO_CLASS_UNPAIRED){
    		for (int i=0; i<tc.length; i++){
    			tc[i] = Float.parseFloat(this.gPanel.timeField[i].getText());
    		}
    	} else if (getStudyDesign() == ONE_CLASS){
    		for (int i=0; i<tc.length; i++){
    			tc[i] = Float.parseFloat(this.oneCPanel.timefields[i].getText());
    		}
    	}
    	return tc;
    }
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        if (getStudyDesign() == TWO_CLASS_UNPAIRED) {
        	if (getTestDesign()==CLUSTER_SELECTION){
        		groupAssignments = getUnpairedClusterGroupAssignments();
        	}else{
	            for (int i = 0; i < exptNames.size(); i++) {
	                if (gPanel.groupARadioButtons[i].isSelected()) {
	                    groupAssignments[i] = GROUP_A;
	                } else if (gPanel.groupBRadioButtons[i].isSelected()) {
	                    groupAssignments[i] = GROUP_B;
	                } else {
	                    groupAssignments[i] = NEITHER_GROUP;
	                }
	            }
        	}
        } else if (getStudyDesign() == MULTI_CLASS) { //  THAT "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
        	if (getTestDesign()==CLUSTER_SELECTION){
        		groupAssignments = getMultiGroupClusterAssignments();
            }else{
	        	for (int i = 0; i < exptNames.size(); i++) {
	                if (mPanel.mulgPanel.notInGroupRadioButtons[i].isSelected()) {
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
            }
        } else if (getStudyDesign() == ONE_CLASS) {
        	if (getTestDesign()==CLUSTER_SELECTION){
        		return getOneClassClusterAssignments();
        	}else{
        		return getOneClassAssignments();
        	}
        }
        
        return groupAssignments;
    }
    
    
    
    
    
    
    
    public int[] getUnpairedClusterGroupAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList<Integer> groupAsamps = unpairedSelector.getGroupSamples("Group "+1);
    	ArrayList<Integer> groupBsamps = unpairedSelector.getGroupSamples("Group "+2);
    	int toWhich = 0;
    	boolean chosen = false;
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = NEITHER_GROUP;
    		if (groupAsamps.contains(i)){
    			groupAssignments[i] = GROUP_A;
    			doubleAssigned = true;
    		} 
    		if (groupBsamps.contains(i)){
    			groupAssignments[i] = GROUP_B;
    			if (doubleAssigned){
    				if (!chosen){
	    		        Object[] optionst = { "GROUP 1", "GROUP 2", "NEITHER", "CANCEL" };
	    				int option = JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Which group should these samples be added to?", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				
	    		        if (option==0) groupAssignments[i] = GROUP_A;
	    		        if (option==1) groupAssignments[i] = GROUP_B;
	    		        if (option==2) groupAssignments[i] = NEITHER_GROUP;
	    		        if (option==3) return null;
	    		        toWhich=groupAssignments[i];
	    		        chosen = true;
    				} else {
    					groupAssignments[i]=toWhich;
    				}
    			}
    		}
        }
    	return groupAssignments;
    }
    
    public int[] getMultiGroupClusterAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
    	for (int i=0; i<mPanel.numGroups; i++){
    		int j = i+1;
    		arraylistArray[i] = multiClassSelector.getGroupSamples("Group "+j);
    		
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
    
    
    
    public int[] getOneClassAssignments() {
        int[] oneClassAssignments = new int[oneCPanel.includeExpts.length];
        
        for (int i = 0; i < oneClassAssignments.length; i++) {
            if (oneCPanel.includeExpts[i].isSelected()) {
                oneClassAssignments[i] = 1;
            } else {
                oneClassAssignments[i] = 0;
            }
        }
        
        return oneClassAssignments;
    }
    public int[] getOneClassClusterAssignments(){
    	ArrayList<Integer> groupAsamps = oneClassSelector.getGroupSamples("Group "+1);
    	int[] groupAssignments = new int[exptNames.size()];
    	for (int i = 0; i < exptNames.size(); i++) {
    		groupAssignments[i] = 0;
    		if (groupAsamps.contains(i)){
    			groupAssignments[i] = 1;
    		} 
    	}
    	
    	return groupAssignments;
    }
    
    
    
    
    public double getOneClassMean() {
    	if (getTestDesign()==CLUSTER_SELECTION){
    		return Double.parseDouble(oneClassClusterMean.getText());
    	}
        return Double.parseDouble(oneCPanel.meanField.getText());
    }    
    
    public int getMultiClassNumGroups() {
        return Integer.parseInt(mPanel.ngPanel.numGroupsField.getText());
    }
    
    public int getNumValidOneClassExpts() {
        int validNum = 0;
        int[] oca;
        if (getTestDesign()==BUTTON_SELECTION){
        	oca = getOneClassAssignments();
        }else{
        	oca = getOneClassClusterAssignments();
        }
        
        for (int i =0; i < oca.length; i++) {
            if (oca[i] == 1) {
                validNum++;
            }
        }
        
        return validNum;
    }    
    
    
    public int[] getGroupCount() {
        int[] groupAssignments = getGroupAssignments();
        int groupACount = 0;
        int groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == GROUP_A) {
                groupACount++;
            } else if (groupAssignments[i] == GROUP_B) {
                groupBCount++;
            }
        }
        
        int[] groupCount = new int[2];
        groupCount[0] = groupACount;
        groupCount[1] = groupBCount;
        
        return groupCount;
    }
    
    public boolean[] isInSurvivalAnalysis() {
        boolean[] inAnalysis = new boolean[csPanel.fields.length];
        for (int i = 0; i < inAnalysis.length; i++) {
            if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                inAnalysis[i] = true;
            } else {
                inAnalysis[i] = false;
            }
        }
        return inAnalysis;
    }
    
    public double[] getSurvivalTimes() {
        double[] survivalTimes = new double[exptNames.size()];
        for (int i = 0; i < survivalTimes.length; i++) {
            try {
                String s = csPanel.fields[i].timeInputField.getText();
                double d = (new Double(s)).doubleValue();
                survivalTimes[i] = d;
                //System.out.println("SAMInitDialog.getSurvivalTimes(): i =  " + i + ", s = " + s + ", d = " + d);
            } catch (NumberFormatException nfe){
                //nfe.printStackTrace();
                survivalTimes[i] = 0;
            }
        }
        return survivalTimes;
    }
    
    public boolean[] isCensored() {
        boolean[] censored = new boolean[exptNames.size()];
        for (int i = 0; i  < censored.length; i++) {
            if (csPanel.fields[i].censoredRadioButton.isSelected()) {
                censored[i] = true;
            } else {
                censored[i] = false;
            }
        }
        return censored;
    }
    
    public Vector<Integer> getPairedAExpts() {
        return tcpmPanel.tcpPanel.pairedAExpts;
    }
    
    public Vector<Integer> getPairedBExpts() {
        return tcpmPanel.tcpPanel.pairedBExpts;
    }
    
    private long factorial(int n) {
        if ((n==1) || (n == 0)) {
            return 1;
        }
        else {
            return factorial(n-1) * n;
        }
    }
    
//    private int getNumCombs(int n, int k) { // nCk
//        
//        /*
//        System.out.println("n = " + n);
//        System.out.println("k = " + k);
//        System.out.println("Numerator: factorial(n) = " + factorial(n));
//        System.out.println("factorial(k) = " + factorial(k));
//        System.out.println("factorial(n-k) = " + factorial(n-k));
//         */
//        return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
//    }
    
    public int getNumNeighbors() {
        String s = iPanel.numNeighborsField.getText();
        return Integer.parseInt(s);
    }

    public boolean isUseRSAM() {
        return vPanel.useRSAM.isSelected();
    }
    
    public int getUserNumCombs() {
        String s = pPanel.numPermsInputField.getText();
        return Integer.parseInt(s);
    }
    
    
    public boolean useKNearest() {
        return iPanel.kNearestButton.isSelected();
    }
    
    /*
    public boolean useAllCombs() {
        return pPanel.useAllPermsButton.isSelected();
    }
     */
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    
    
    public boolean useTusherEtAlS0() {
        if (sqPanel.s0SelectBox.getSelectedIndex() == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public double getPercentile() {
        if (sqPanel.s0SelectButton.isSelected()) {
            int index = sqPanel.s0SelectBox.getSelectedIndex();
            if (index == 0) {
                return -1d;
            } else if (index == 1) {
                return 5d;
            } else if (index == 2) {
                return 50d;
            } else if (index == 3) {
                return 90d;
            } else if (index == 4) {
                return 0d;
            }
        } else {
            return Double.parseDouble(sqPanel.s0EntryField.getText());
        }
        return 0d;
    }
    
    public boolean calculateQLowestFDR() {
        if (sqPanel.qYesButton.isSelected()){
            return true;
        } else {
            return false;
        }
    }
    
    public int getNumUniquePerms() {
        return this.numUniquePerms;
    }
    
    public boolean useAllUniquePerms() {
        return this.allUniquePermsUsed;
    }
    
    private int getNumUnique2ClassUnpairedPerms(int n, int k) { // nCk
        return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
    }    
    
    
    public class EventListener extends WindowAdapter implements ActionListener{
        public void actionPerformed(ActionEvent ae){
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if ((getTestDesign()==CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                try {
                    if (sqPanel.s0EntryButton.isSelected()) {
                        double d = Double.parseDouble(sqPanel.s0EntryField.getText());
                        if ((d < 0)||(d > 100)) {
                            JOptionPane.showMessageDialog(null, "Enter a valid percentile between 0 and 100!", "Error!", JOptionPane.ERROR_MESSAGE);
                            return;                          
                        }
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Enter a valid percentile between 0 and 100!", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    getUserNumCombs();
                    //}
                    
                    if (useKNearest()) {
                        getNumNeighbors();
                    }                  
                    
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE); 
                    return;
                }
                if (getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                    // if (evt.getSource() == okButton) {
                    int[] grpAssignments;
                	if (getTestDesign()==SAMInitDialog.BUTTON_SELECTION){
                		grpAssignments = getGroupAssignments();
                	}else{
                		grpAssignments = getUnpairedClusterGroupAssignments();
                	}
                	
                    int grpACounter = 0;
                    int grpBCounter = 0;
                    for (int i = 0; i < grpAssignments.length; i++) {
                        if (grpAssignments[i] == GROUP_A) {
                            grpACounter++;
                        } else if (grpAssignments[i] == GROUP_B) {
                            grpBCounter++;
                        }
                    }
                    if ((grpACounter < 2) || (grpBCounter < 2)) {
                        JOptionPane.showMessageDialog(null, "Group A and Group B must contain more than one sample.", "Error", JOptionPane.WARNING_MESSAGE);
                    } /*else if ((Integer.parseInt(iPanel.numNeighborsField.getText()) > numGenes) || (Integer.parseInt(iPanel.numNeighborsField.getText()) <= 0)) {
                            JOptionPane.showMessageDialog(null, "Number of neighbors must be  > 0, and <= the total number of genes", "Error", JOptionPane.WARNING_MESSAGE);
                        } else if (Integer.parseInt(pPanel.numPermsInputField.getText()) < 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations must be > 0", "Error", JOptionPane.WARNING_MESSAGE);
                        } */else {
                            try {
                                
                                int numCombs = 0;
                                //if (!useAllCombs()) {
                                numCombs = getUserNumCombs();
                                //}
                                
                                if (useKNearest()) {
                                    getNumNeighbors();
                                }
                                 
                                if ((grpACounter + grpBCounter) <= 20) {
                                    numUniquePerms = getNumUnique2ClassUnpairedPerms((grpACounter + grpBCounter), grpACounter);
                                    SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                    sapDialog.setVisible(true);
                                    allUniquePermsUsed = sapDialog.useAllPerms();
                                }
                                okPressed = true;
                                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                                
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            
                            //okPressed = true;
                            //hide();
                            //dispose();
                        }
                    // }
                } else if (getStudyDesign() == SAMInitDialog.TWO_CLASS_PAIRED) {
                    if (tcpmPanel.tcpPanel.pairedListModel.size() < 2) {
                        JOptionPane.showMessageDialog(null, "Need at least two pairs of samples!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (tcpmPanel.tcpPanel.pairedListModel.size() <= 29) {
                            int numCombs = getUserNumCombs();
                            numUniquePerms = (int)(Math.pow(2, tcpmPanel.tcpPanel.pairedListModel.size()));
                            SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                            sapDialog.setVisible(true);
                            allUniquePermsUsed = sapDialog.useAllPerms();                            
                        }
                        okPressed = true;
                        javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                        dispose();
                    }
                    
                } else if (getStudyDesign() == SAMInitDialog.MULTI_CLASS) {
                    boolean tooFew = false;
                    int[] grpAssignments = getGroupAssignments();
                    int numGroups = getMultiClassNumGroups();
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
                    
                        /*
                        for (int i = 0; i < grpAssignments.length; i++) {
                            System.out.println("grpAssignments[" + i + "] = "  + grpAssignments[i]);
                        }
                         
                        for (int i = 0; i < groupSize.length; i++) {
                            System.out.println("groupSize[" + i + "] = " + groupSize[i] + " (group " + (i +1) + ")");
                        }
                         */
                    
                    for (int i = 0; i < groupSize.length; i++) {
                        if (groupSize[i] <= 1) {
                            JOptionPane.showMessageDialog(null, "Each group must contain more than one sample.", "Error", JOptionPane.WARNING_MESSAGE);
                            tooFew = true;
                            break;
                        }
                    }
                    
                    if (!tooFew) {
                        try {
                            getUserNumCombs();
                            //}
                            
                            if (useKNearest()) {
                                getNumNeighbors();
                            }
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            
                            dispose();
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                     
                } else if (getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL) {
                    try {
                        boolean allSame = true;
                        int selectedCounter = 0;
                        double previousTime = 0;
                        for (int i = 0; i < exptNames.size(); i++) {
                            if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                                selectedCounter++;
                                double d = (new Double(csPanel.fields[i].timeInputField.getText())).doubleValue();
                                if (i > 0) {
                                    if (previousTime != d) {
                                        allSame = false;
                                    }
                                }
                                previousTime = d;
                            }
                        }
                        int numCombs = 0;
                        //if (!useAllCombs()) {
                        numCombs = getUserNumCombs();
                        //}
                        
                        if (useKNearest()) {
                            getNumNeighbors();
                        }
                        if (selectedCounter < 2) {
                            JOptionPane.showMessageDialog(null, "At least 2 samples must be selected!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (allSame) {
                            JOptionPane.showMessageDialog(null, "At least one of the survival time values must be different from the rest!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            if (selectedCounter <= 10) {
                                numUniquePerms = (int)(factorial(selectedCounter));
                                SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                sapDialog.setVisible(true);
                                allUniquePermsUsed = sapDialog.useAllPerms();
                            }                            
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            dispose();
                        }
                    } catch (NumberFormatException nfe){
                        JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (getStudyDesign() == SAMInitDialog.ONE_CLASS) {
                    try {
                        getOneClassMean();
                        if (getNumValidOneClassExpts() < 2) {
                            JOptionPane.showMessageDialog(null, "At least 2 samples must be selected for one-class test!", "Error", JOptionPane.ERROR_MESSAGE);                            
                        } else {
                            if (getNumValidOneClassExpts() <= 29) {
                                int numCombs = getUserNumCombs();
                                numUniquePerms = (int)(Math.pow(2, getNumValidOneClassExpts()));
                                SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                sapDialog.setVisible(true);
                                allUniquePermsUsed = sapDialog.useAllPerms();
                            }                            
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            dispose();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Invalid value for one-class mean!", "Error", JOptionPane.ERROR_MESSAGE);                        
                    }
                }
            }  // ends handling ok-command
            else if(command.equals("reset-command")){
                if(getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED)
                    gPanel.reset();
                else if(getStudyDesign() == SAMInitDialog.MULTI_CLASS && mPanel.mulgPanel != null)
                    mPanel.reset();
                else if(getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL)
                    csPanel.reset();
                sqPanel.reset();
                pPanel.reset();
                iPanel.reset();
                tcpmPanel.tcpPanel.reset();
                hclOpsPanel.setHCLSelected(false);
            }
            else if(command.equals("cancel-command")){
                okPressed = false;
                //hide();
                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                dispose();
            }
            else if(command.equals("info-command")){
            	HelpWindow.launchBrowser(SAMInitDialog.this, "SAM Initialization Dialog");
            }
        }
        
        public void windowClosing(WindowEvent we){
            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
            //System.out.println("Sam is closed");
        }
        
    }
    
    public static void main(String[] args) {
        
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 100; i++) {
            dummyVect.add("Expt " + i);
        }
        SAMInitDialog sDialog = new SAMInitDialog(dummyFrame, true, dummyVect, 5, null);
        sDialog.setVisible(true);
        sDialog.getTCPRVector();
        float[] asd = sDialog.getTimeCourse();
        for(int i=0; i<asd.length; i++){
        	System.out.print(asd[i]+"   ");
        }
        System.exit(0);
        
    }

	public boolean isTimeCourse() {
		return vPanel.timecourseCB.isSelected();
	}
    
}
