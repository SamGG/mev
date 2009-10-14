package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.GeneAnnotationImportDialog;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;

public class DataPanel extends JPanel implements IWizardParameterPanel{
	
		
	//Panel containing the text box for entering number of factors
	private javax.swing.JPanel numberOfFactorPanel;
	private javax.swing.JLabel factorLabel;
	private javax.swing.JTextField factorTextField;
	private  int num_factors=0;
	
	//Panel for entering name and levels of these factors
	private javax.swing.JLabel factorNameLabel;
	private javax.swing.JTextField factorNameTextField;
	private javax.swing.JPanel factorLevelPanel;
	private javax.swing.JLabel factorLevelLabel;
	private javax.swing.JTextField factorLevelTextField;
	
	private ArrayList<String> factorNameList=new ArrayList<String>();
	private ArrayList factorLevelList=new ArrayList();
	private AlgorithmData algData;
	private IData idata;
	//private JFrame parentFrame;
	private ClusterRepository clusterRepository;
	private IFramework framework;
	private JPanel fileLoaderPanel;
	
	private JPanel  gPanel,pane;
	private JTabbedPane tabbedSelectors;
	private ClusterSelector[] clusterSelector;
	
	
	private static int xcoord=0;
	private static int ycoord=0;
	
	//Panel for group assignments/cluster selection
	
	//Constructor
	public DataPanel(IData idata, AlgorithmData algData, JFrame parent,
			ClusterRepository clusterRepository, IFramework framework) {

		//this.parentFrame = parent;
		this.idata = idata;
		this.algData = algData;
		this.clusterRepository = clusterRepository;
		this.framework = framework;
		initComponents();
	}
	
	
	private void initComponents(){
		this.setPreferredSize(new Dimension(1000, 850));
		GridBagLayout gridbag=new GridBagLayout();
		setLayout(gridbag);
		GridBagConstraints constraints = new GridBagConstraints();
	 
	    
		//Panel for entering number of factors
		numberOfFactorPanel=new javax.swing.JPanel();
		numberOfFactorPanel.setBackground(Color.white);
	    numberOfFactorPanel.setBorder((new EtchedBorder()));
	
		factorLabel=new javax.swing.JLabel("Enter the number of groups in your samples:");
		
		factorTextField=new javax.swing.JTextField(50);
		factorTextField.setText("2");
		factorTextField.setMinimumSize(new Dimension(100, 30));
		factorTextField.addKeyListener(new Listener());
		
		
		//panel for factor level text boxes
		factorLevelPanel=new javax.swing.JPanel();
		factorLevelPanel.setBackground(Color.WHITE);
		factorLevelPanel.setBorder((new EtchedBorder()));
		factorLevelPanel.setVisible(true);
			
		pane = new javax.swing.JPanel();
		pane.setBackground(Color.white);
	    pane.setBorder(new EtchedBorder());
	   
	    pane.setVisible(true);
		//Fitting everything together
		//Add label to numberOfFactorPanel
		constraints=buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
		constraints.fill=GridBagConstraints.BOTH;
		constraints.insets=new Insets(0,0,0,5);
		gridbag.setConstraints(factorLabel, constraints);
	    numberOfFactorPanel.add(factorLabel);
	   

	    //Add factortextfield to numberOfFactorPanel
	    constraints=buildConstraints(constraints, 4, 0, 1, 1, 30, 100);
		constraints.fill=GridBagConstraints.BOTH;
	    gridbag.setConstraints(factorTextField, constraints);
	    numberOfFactorPanel.add(factorTextField);
	    

	    //Add numberOfFactorPanel to the mainpanel
	    constraints=buildConstraints(constraints, 0, 0, 1, 1, 0, 0);
		constraints.fill=GridBagConstraints.BOTH;
	    gridbag.setConstraints(numberOfFactorPanel, constraints);
	    this.add(numberOfFactorPanel);
	   

	    //Add FactorLevelPane to the mainpanel
	    constraints= buildConstraints(constraints, 0, 2, 1, 1, 0, 30);
		constraints.fill=GridBagConstraints.BOTH;
	    gridbag.setConstraints(factorLevelPanel, constraints);
	    this.add(factorLevelPanel);
	  

	    //Add "Cluster selector" panel to the main panel
	  //  constraints=buildConstraints(constraints, 0,3, 1, 4, 100, 100);
	    constraints=buildConstraints(constraints, 0,3, 1, 1, 100, 100);
		constraints.fill=GridBagConstraints.BOTH;
		pane.setLayout(gridbag);
	    gridbag.setConstraints(pane, constraints);
	    this.add(pane);
	    
	    addRemoveFactor(2);
	    initialize();
	    if(drawSampleGroupingsPanel()){
	       	makeClusterSelector();
	    }
	    	
	    revalidate();
	}
	
	
	public void initialize(){
		factorNameList=new ArrayList(getNumberOfFactors());
		factorLevelList=new ArrayList(getNumberOfFactors());
		
		for(int i=0; i<getNumberOfFactors(); i++){
			factorNameList.add(i,"Factor"+i);
			factorLevelList.add(i, 2);
		}
	}
	
	public void setNumberOfFactors(int num_factors){
		this.num_factors=num_factors;
	}
	
	public int getNumberOfFactors(){
		return this.num_factors;
	}
	
	
	/**
	 * makeFactorLevel dynamically generates a new Factor Level
	 * label and text box and assigns listener and adds them to the factorLevelPanel
	 * 
	 * @param num_levels
	 */
	public void makeFactorLevel(String name){
		
		GridBagConstraints c=new GridBagConstraints();
		GridBagLayout grid=new GridBagLayout();
		
		javax.swing.JLabel factorNameLabel=new javax.swing.JLabel("Factor "+name+" name:");
		factorNameTextField=new javax.swing.JTextField(name,30);
		factorNameTextField.setPreferredSize(new Dimension(100, 30));
		factorNameTextField.setEditable(true);
		factorNameTextField.setName(name);
		factorNameTextField.addKeyListener(new Listener(){
			public void keyReleased(KeyEvent e) {
				if(((javax.swing.JTextField)e.getSource()).getText()!= "")
					setFactorName((Integer.parseInt(e.getComponent().getName())-1), ((javax.swing.JTextField)e.getSource()).getText());
							
			}
			
		});
		
		
		factorLevelLabel=new JLabel("Number of levels of factor "+name+" :");
		factorLevelTextField=new javax.swing.JTextField(30);
		factorLevelTextField.setPreferredSize(new Dimension(100, 30));
		factorLevelTextField.setEditable(true);
		factorLevelTextField.setName(name);
		factorLevelTextField.setText("2");
			
		factorLevelTextField.addKeyListener(new Listener(){
		
			public void keyReleased(KeyEvent e) {
				//if(!((javax.swing.JTextField)e.getSource()).getText().isEmpty())
				if(((javax.swing.JTextField)e.getSource()).getText() != "")
					setFactorLevel((Integer.parseInt(e.getComponent().getName())-1), Integer.parseInt(((javax.swing.JTextField)e.getSource()).getText()));
				if(drawSampleGroupingsPanel()){
					makeClusterSelector();
				}

			}
		});
		
		
		c=buildConstraints(c, xcoord, ycoord, 1, 1, 100, 100);
	    grid.setConstraints(factorNameLabel, c);
	    factorLevelPanel.add(factorNameLabel);
	    
	    c=buildConstraints(c, xcoord+5, ycoord, 1, 1, 100, 100);
	    grid.setConstraints(factorNameTextField, c);
	    factorLevelPanel.add(factorNameTextField);
		
	    c=buildConstraints(c, xcoord+10, ycoord, 1, 1, 100, 100);
		grid.setConstraints(factorLevelLabel, c);
	    factorLevelPanel.add(factorLevelLabel);
	    
	    c=buildConstraints(c, xcoord+15, ycoord, 1, 1, 100, 100);
	    grid.setConstraints(factorLevelTextField, c);
	    factorLevelPanel.add(factorLevelTextField);
		
		
		
		revalidate();
		//Increment coordinates to add more if needed..
		
		ycoord=ycoord+1;


	}
	
	
	
	public boolean validateFactorNumber(int num_factor){
		if(num_factor==0){
			JOptionPane.showMessageDialog(this, "You cannot have zero factors!", "Factor Number Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(num_factor>=4){
			JOptionPane.showMessageDialog(this, "You cannot have more than three factors!", "Factor Number Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
	    return true;
	}
	
	
	
	public void incrementXcoord(){
		xcoord=xcoord+1;
	}
	
	public void incrementYCoord(){
		ycoord=ycoord+1;
	}
	
	public void resetPanel(JPanel panel){
		panel.removeAll();
		validate();
	}
	
	public void resetCoordinates(){
		xcoord=0;
		ycoord=0;
	}
	
	
	/**
	 * 
	 * @param num_factor
	 */
	public void addRemoveFactor(int num_factor){
		if(!factorLevelPanel.isVisible()){
			factorLevelPanel.setVisible(true);
		}
		resetPanel(factorLevelPanel);
		resetCoordinates();
			
		for(int index=0; index<(num_factor); index++){
			makeFactorLevel(String.valueOf(index+1));
		}

		setNumberOfFactors(num_factor);
		
	}
	
	
	public void makeClusterSelector(){

		resetPanel(pane);
		pane.setVisible(true);
		setBackground(Color.white);
		clusterSelector = new ClusterSelector[getNumberOfFactors()];
		
		gPanel=new javax.swing.JPanel();
		GridBagLayout grid = new GridBagLayout();
		
		GridBagConstraints constraints = new GridBagConstraints();
		gPanel.setLayout(grid);

		for (int i=0; i<getNumberOfFactors(); i++){
			MultiGroupExperimentsPanel mulg=new MultiGroupExperimentsPanel((String)getAllFactorNames().get(i), ((Integer)getAllFactorLevels().get(i)).intValue());
			
			
			if(i==0)
				constraints=buildConstraints(constraints, i, 0, 1, 1, 50, 90);
			else
				constraints=buildConstraints(constraints, i, 0, 1, 1, 50, 0);
			constraints.fill = GridBagConstraints.BOTH;
			grid.setConstraints(mulg, constraints);
			gPanel.add(mulg);

		}
		
		//Add Save, Load and Reset buttons to gPanel now
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
         
         
         final int finNum = getSampleNames().size();
         
         resetButton.addActionListener(new ActionListener() {
        	 
             public void actionPerformed(ActionEvent evt) {
            	 for(int index=0; index<getAllFactorNames().size(); index++)
            		 ((MultiGroupExperimentsPanel)gPanel.getComponent(index)).reset();
             }
            
         });
         
         final JFileChooser fc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
      
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
         constraints=buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
         gridbag3.setConstraints(saveButton, constraints);
         panel2.add(saveButton);
         
         constraints=buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
         gridbag3.setConstraints(loadButton, constraints);
         panel2.add(loadButton);
         
         constraints=buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
         gridbag3.setConstraints(resetButton, constraints);
         panel2.add(resetButton);
         constraints.insets = new Insets(0,0,0,0);
   
         constraints=buildConstraints(constraints, 0, 1, 2, 1, 100, 5);
         constraints.anchor = GridBagConstraints.CENTER;
             
         grid.setConstraints(panel2, constraints);
         gPanel.add(panel2);
		
	
         

		for (int i=0; i<getNumberOfFactors(); i++){
			clusterSelector[i]=new ClusterSelector(this.clusterRepository, ((Integer)getAllFactorLevels().get(i)).intValue());
		}

		if (clusterRepository!=null){
			for (int i=0; i<num_factors;i++){
				clusterSelector[i].setClusterType((String)getAllFactorNames().get(i));
			}
		}


		GridBagConstraints c = new GridBagConstraints();

		JPanel clusterSelectorPanel = new JPanel();
		clusterSelectorPanel.setLayout(new GridBagLayout());

		c.fill = GridBagConstraints.BOTH;
		c.weighty =1;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.PAGE_END;
		for (int i=0; i<getNumberOfFactors(); i++){
			clusterSelectorPanel.add(clusterSelector[i], c);
			c.gridy++;
		}


        
        tabbedSelectors=new JTabbedPane();
		tabbedSelectors.insertTab("Cluster Selection",null,clusterSelectorPanel, null, 0);
		tabbedSelectors.insertTab("Button Selection", null, gPanel, null, 1);  
     
		constraints=buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
		constraints.fill=GridBagConstraints.BOTH;
		((GridBagLayout)pane.getLayout()).setConstraints(tabbedSelectors, constraints);
		pane.add(tabbedSelectors);
		
        
	  	revalidate();

	}

	
	
	public ArrayList<String> getAllFactorNames(){
		return factorNameList;
	}
	
	
	
	
	public ArrayList getAllFactorLevels(){
			return factorLevelList;
	}
	
	
	public int getMaximumFactorLevel(){
		int max=-1;
		for(int index=0; index<factorLevelList.size(); index++){
			if(((Integer)factorLevelList.get(index)).intValue() > max)
				max=((Integer)factorLevelList.get(index)).intValue();
		}
		return max;
	}
	
	
	
	public void setFactorLevel(int pos, int value){
		factorLevelList.set(pos, value);
	}
	
	public void setFactorName(int index, String name){
		factorNameList.set(index, name);
	}

	public ArrayList getSampleNames(){
		
		ArrayList<String> sampleNames=new ArrayList<String>();
		Experiment experiment = this.idata.getExperiment();
		int number_of_samples = experiment.getNumberOfSamples();
		
		for (int i = 0; i < number_of_samples; i++) {
			sampleNames.add(idata.getSampleName(i));
		}
		
		return sampleNames;
		
	}
	
	
	public boolean drawSampleGroupingsPanel(){
		boolean draw=false;
		
		
		for(int index=0; index<getNumberOfFactors(); index++){
			
			if(((Integer)getAllFactorLevels().get(index)).intValue()!=0)
				draw=true;
			else
				draw=false;
			
		}
	
		return draw;
		
	}
	
		
	private class Listener implements  KeyListener{

		
		public void keyReleased(KeyEvent e) {
			Object src=e.getSource();
			
			//if(src==factorTextField && !factorTextField.getText().isEmpty() && validateFactorNumber(Integer.parseInt(factorTextField.getText()))){
			if(src==factorTextField && factorTextField.getText() != "" && validateFactorNumber(Integer.parseInt(factorTextField.getText()))){
				addRemoveFactor(Integer.parseInt(factorTextField.getText()));
				initialize();
				if(getAllFactorLevels().size()!=0){
					if(drawSampleGroupingsPanel()){
						makeClusterSelector();
					}
				}

			}
			
			

		}
		
		public void keyTyped(KeyEvent e) {
			
		}
		
		
		
		public void keyPressed(KeyEvent e) {
			
		}

			
		
	}
	
	
	public void clearValuesFromAlgorithmData() {
		//Not necessary to clear values, because the parameters populated 
		//are stored in a hash and so if user enters different set of values
		//by clicking back button, the entry in the hash will get replaced

	}

	public void onDisplayed() {
		// TODO Auto-generated method stub
		
	}

	public void populateAlgorithmData() {
	
		// Add code here to capture factor names, factor levels and group assignments. 
		if(getAllFactorNames().size()!=0)
			this.algData.addStringArray("factor-names", (getAllFactorNames()
				.toArray(new String[getNumberOfFactors()])));
		int[] temp = new int[getNumberOfFactors()];
		int[][] grp_assignment = new int[getNumberOfFactors()][];

		for (int index = 0; index < getNumberOfFactors(); index++) {
			temp[index] = ((Integer) getAllFactorLevels().get(index)).intValue();
		}
		
		if(temp.length!=0)
			this.algData.addIntArray("factor-levels", temp);

		for (int grpIndex = 0; grpIndex < getNumberOfFactors(); grpIndex++) {
					grp_assignment[grpIndex]= ((MultiGroupExperimentsPanel) gPanel
					.getComponent(grpIndex)).getFactorAssignments(grpIndex);
					
			
		}
		
		
		
		
		if(checkGroupAssignment(grp_assignment))
			this.algData.addIntMatrix("factor-assignments", grp_assignment);
		else
			this.algData.addIntMatrix("factor-assignments", null);

	}
	
	
	public boolean checkGroupAssignment(int[][]grp_assignment){
		for(int i=0; i<1; i++){
			for(int j=0; j<grp_assignment[i].length; j++){
				if(grp_assignment[i][j]!=1)
					return true;
			}
			
		}
		
		
	return false;
	}
	
	
	
	

	private class MultiGroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JRadioButton[][] exptGroupRadioButtons;
        JRadioButton[] notInGroupRadioButtons;
        int numGroups;
        
        MultiGroupExperimentsPanel(String factorName, int numGroups) {
            this.setBorder(new TitledBorder(new EtchedBorder(), factorName + " assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.numGroups = numGroups;
            setBackground(Color.white);
            JPanel panel1 = new JPanel();
            expLabels = new JLabel[getSampleNames().size()];
            exptGroupRadioButtons = new JRadioButton[numGroups][getSampleNames().size()];
            notInGroupRadioButtons = new JRadioButton[getSampleNames().size()];
            ButtonGroup chooseGroup[] = new ButtonGroup[getSampleNames().size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            panel1.setLayout(gridbag);
            
            for (int i = 0; i < getSampleNames().size(); i++) {
                String s1 = (String)(getSampleNames().get(i));
                expLabels[i] = new JLabel(s1);
                chooseGroup[i] = new ButtonGroup();
                for (int j = 0; j < numGroups; j++) {
                    exptGroupRadioButtons[j][i] = new JRadioButton("Group " + (j + 1) + "     ", j == 0? true: false);
                    chooseGroup[i].add(exptGroupRadioButtons[j][i]);
                }
                
                notInGroupRadioButtons[i] = new JRadioButton("Not in groups", false);
                chooseGroup[i].add(notInGroupRadioButtons[i]);
                
                for (int j = 0; j < numGroups; j++) {
                    constraints=buildConstraints(constraints, j, i, 1, 1, 100, 100);
                    gridbag.setConstraints(exptGroupRadioButtons[j][i], constraints);
                    panel1.add(exptGroupRadioButtons[j][i]);
                }
                
                constraints=buildConstraints(constraints, (numGroups + 1), i, 1, 1, 100, 100);
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
            exptNameHeaderPanel.setSize(50, panel1.getPreferredSize().height);
            exptNameHeaderPanel.setPreferredSize(new Dimension(maxLabelWidth + 10, panel1.getPreferredSize().height));
            exptNameHeaderPanel.setLayout(exptHeaderGridbag);
            
            
            for (int i = 0; i < expLabels.length; i++) {
                constraints=buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                constraints.fill = GridBagConstraints.BOTH;
                exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                exptNameHeaderPanel.add(expLabels[i]);
            }
            
            scroll.setRowHeaderView(exptNameHeaderPanel);
            
            constraints=buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
           
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
          
           revalidate();
            
        }
        
        public void reset() {
            for (int i = 0; i < getSampleNames().size(); i++) {
                exptGroupRadioButtons[0][i].setSelected(true);
            }
        }
        
        
        public boolean isButtonSelectionMethod(){
        	if(tabbedSelectors.getSelectedIndex()==1)
        		return true;
        	return false;
        		
        }
        
        
        
        public int[] getGroupAssignments() {
            int[] groupAssignments = new int[getSampleNames().size()];
            
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
        
       
        public int[] getFactorAssignments(int factorNumber ) {
        	if (!isButtonSelectionMethod()){
        		
        		int[]temp=getFactorClusterAssignments(factorNumber);
        		return getFactorClusterAssignments(factorNumber);
        	}
            return ((MultiGroupExperimentsPanel)gPanel.getComponent(factorNumber)).getGroupAssignments();
        }

      
    
 }

	
	  public int[] getFactorClusterAssignments(int factor){
      	boolean doubleAssigned;
      	int[]groupAssignments = new int[getSampleNames().size()];
      	
      	
      	ArrayList[] arraylistArray = new ArrayList[getMaximumFactorLevel()];
      	
      	
      	
      	for (int i=0; i<getMaximumFactorLevel(); i++){
      		int j = i+1;
      		arraylistArray[i] = clusterSelector[factor].getGroupSamples("Group "+j);
      		
      	}
      
      	for (int i = 0; i < getSampleNames().size(); i++) {
      		doubleAssigned = false;
      		groupAssignments[i] = 0;
      		for (int j = 0;j<getMaximumFactorLevel();j++){
  	    		if (arraylistArray[j].contains(i)){
  	    			if (doubleAssigned){
  	    		        Object[] optionst = { "OK" };
  	    				JOptionPane.showOptionDialog(null, 
  	    						"The clusters you have chosen "+(String)getAllFactorNames().get(0)+" have overlapping samples. \n Each group must contain unique samples.", 
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
	  
	  
    
    /**
	 * Saves the assignments to file.
	 * 
	 */
	private void saveAssignments() {
		
		File file;		
		JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);	
		
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
				for (int i=0; i<getAllFactorLevels().size(); i++){
					if (((Integer)getAllFactorLevels().get(i)).intValue()>maxClasses)
						maxClasses = ((Integer)getAllFactorLevels().get(i)).intValue();
				}
				for (int i=0; i<maxClasses; i++){
    				pw.print("Group "+(i+1)+" Label:\t");
					pw.println("Group "+(i+1));
				}
								
				pw.println("#");
				
				pw.println("Sample Index\tSample Name\tGroup Assignment");

				int[]groupAAssgn=((MultiGroupExperimentsPanel)gPanel.getComponent(0)).getFactorAssignments(0);
				int[]groupBAssgn=null;
				int[]groupCAssgn=null;
				if(getAllFactorNames().size()==3||getAllFactorNames().size()==2)
					groupBAssgn=((MultiGroupExperimentsPanel)gPanel.getComponent(1)).getFactorAssignments(1);
				if(getAllFactorNames().size()==3){
					groupCAssgn=((MultiGroupExperimentsPanel)gPanel.getComponent(2)).getFactorAssignments(2);
				}
				
				for(int sample = 0; sample < getSampleNames().size(); sample++) {
					pw.print(String.valueOf(sample+1)+"\t"); //sample index
					pw.print(getSampleNames().get(sample)+"\t");
					
					if (groupAAssgn[sample]!=0)
						pw.print("Group "+(groupAAssgn[sample]));
					else
						pw.print("Exclude");

					if(getAllFactorNames().size()==3||getAllFactorNames().size()==2){
    					if (groupBAssgn[sample]!=0)
    						pw.print("\tGroup "+(groupBAssgn[sample]));
    					else
    						pw.print("\tExclude");
					}
					
					if(getAllFactorNames().size()==3){
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
   		JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
   		
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
       			Vector<String> loadedSampleNames = new Vector<String>();
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
       					loadedSampleNames.add(lineArray[1]);
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
       				JOptionPane.showMessageDialog(null, "The loaded file contained only 1 Factor.  Only "+(String)getAllFactorNames().get(0)+" will be loaded.", "Error", JOptionPane.ERROR_MESSAGE);                            	       						
       			else if ((!threeloaded)&& getAllFactorNames().size()==3)
       				JOptionPane.showMessageDialog(null, "The loaded file contained only 2 Factors.  Only "+(String)getAllFactorNames().get(0)+" and "+(String)getAllFactorNames().get(1)+" will be loaded.", "Error", JOptionPane.ERROR_MESSAGE);                            	       						
       			
       			//we have the data parsed, now validate, assign current data


       			if( getSampleNames().size() != loadedSampleNames.size()) {
       				System.out.println(getSampleNames().size()+"  "+loadedSampleNames.size());
       				//status = "number-of-samples-mismatch";
       				System.out.println(getSampleNames().size()+ " s length " + loadedSampleNames.size());
       				//warn and prompt to continue but omit assignments for those not represented				

       				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(loadedSampleNames.size())+")<br>" +
       						                                   "does not match the number of samples loaded in MeV ("+getSampleNames().size()+").<br>" +
       						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
       				
       				return;
       			}
       			Vector<String> currSampleVector = new Vector<String>();
       			for(int i = 0; i < getSampleNames().size(); i++)
       				currSampleVector.add((String)getSampleNames().get(i));
       			
       			int fileSampleIndexA = 0;
       			int groupIndexA = 0;
       			int fileSampleIndexB = 0;
       			int groupIndexB = 0;
       			int fileSampleIndexC = 0;
       			int groupIndexC = 0;
       			String groupAName;
       			String groupBName;
       			String groupCName;
       			
       			for(int sample = 0; sample < getSampleNames().size(); sample++) {

    				boolean doIndex = false;
    				for (int i=0;i<getSampleNames().size(); i++){
    					if (i==sample)
    						continue;
    					if (getSampleNames().get(i).equals(getSampleNames().get(sample))){
    						doIndex=true;
    					}
    				}
    				fileSampleIndexA = loadedSampleNames.indexOf(getSampleNames().get(sample));
    				if (fileSampleIndexA==-1){
    					doIndex=true;
    				}
    				if (twoloaded){
	       				fileSampleIndexB = loadedSampleNames.indexOf(getSampleNames().get(sample));
	       				if (fileSampleIndexB==-1){
	       					doIndex=true;
	       					break;
	       				}
    				}
    				if (threeloaded&& getAllFactorNames().size()==3){
	       				fileSampleIndexC = loadedSampleNames.indexOf(getSampleNames().get(sample));
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
       					((MultiGroupExperimentsPanel)gPanel.getComponent(0)).exptGroupRadioButtons[groupIndexA][sample].setSelected(true);
       				}catch (Exception e){
       					((MultiGroupExperimentsPanel)gPanel.getComponent(0)).notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
       				}
       				
       				if (twoloaded){
	       				
	       				groupBName = (String)(groupBAssignments.get(fileSampleIndexB));
	       				groupIndexB = groupNames.indexOf(groupBName);
	       				
	       				//set state
	       				try{
	       					((MultiGroupExperimentsPanel)gPanel.getComponent(1)).exptGroupRadioButtons[groupIndexB][sample].setSelected(true);
	       				}catch (Exception e){
	       					((MultiGroupExperimentsPanel)gPanel.getComponent(1)).notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
	       				}
       				}
       				if (threeloaded&&getAllFactorNames().size()==3){
	       				
	       				groupCName = (String)(groupCAssignments.get(fileSampleIndexC));
	       				groupIndexC = groupNames.indexOf(groupCName);
	       				
	       				//set state
	       				try{
	       					((MultiGroupExperimentsPanel)gPanel.getComponent(2)).exptGroupRadioButtons[groupIndexC][sample].setSelected(true);
	       				}catch (Exception e){
	       					((MultiGroupExperimentsPanel)gPanel.getComponent(2)).notInGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
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
			
   		for(int sample = 0; sample < getSampleNames().size(); sample++) {
   			try{
   				((MultiGroupExperimentsPanel)gPanel.getComponent(0)).exptGroupRadioButtons[groupNames.indexOf(groupAAssignments.get(sample))][sample].setSelected(true);
   			}catch(Exception e){
   				((MultiGroupExperimentsPanel)gPanel.getComponent(0)).notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
   			}
   		}
   		if (twoloaded){
	   		for(int sample = 0; sample < getSampleNames().size(); sample++) {
	   			try{
	   				((MultiGroupExperimentsPanel)gPanel.getComponent(1)).exptGroupRadioButtons[groupNames.indexOf(groupBAssignments.get(sample))][sample].setSelected(true);
	   			}catch(Exception e){
	   				((MultiGroupExperimentsPanel)gPanel.getComponent(1)).notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
	   			}
	   		}
   		}
   		if (threeloaded&& getAllFactorNames().size()==3){
	   		for(int sample = 0; sample < getSampleNames().size(); sample++) {
	   			try{
	   				((MultiGroupExperimentsPanel)gPanel.getComponent(2)).exptGroupRadioButtons[groupNames.indexOf(groupCAssignments.get(sample))][sample].setSelected(true);
	   			}catch(Exception e){
	   				((MultiGroupExperimentsPanel)gPanel.getComponent(2)).notInGroupRadioButtons[sample].setSelected(true);//set to last state... excluded
	   			}
	   		}
   		}
   	}   	
   
  
	
	
	public GridBagConstraints buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
		gbc=new GridBagConstraints();
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        return gbc;
    }
	
	public static void main(String[] args) {
       
    }
	
	

}
