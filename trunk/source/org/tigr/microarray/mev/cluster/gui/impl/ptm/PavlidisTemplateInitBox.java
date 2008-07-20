/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PavlidisTemplateInitBox.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 20:22:03 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.graph.GraphCanvas;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.microarray.mev.TMEV;
import org.tigr.util.FloatMatrix;


public class PavlidisTemplateInitBox extends JPanel {
    
    String[] SampleNames;
    Vector sampleNamesVector;
    Vector uniqueIDs;
    Vector clusters; //THIS IS A VECTOR OF VECTORS (EACH SUB-VECTOR CONTAINS THE INDICES OF UNIQUEIDS IN THAT CLUSTER)
    Color[] clusterColors;
    UniqueIDSelector uidSelectPanel;
    ClusterSelector clusterSelectPanel;
    SavedTemplateSelector templateSelectPanel;
    BottomPanel bott;
    TemplatePanel tempPanel;
    AbsolutePanel abs;
    ThresholdPanel thresh;
    FloatMatrix expMatrix;
    JCheckBox drawTreesBox;
    JPanel drawTreesPane;
    JTabbedPane tabbedPane;
    Vector template;
    FloatMatrix templateMatrix;
    
    private boolean okPressed = false;
    
    
    public PavlidisTemplateInitBox(JFrame parentFrame, boolean modality, FloatMatrix expMatrix, Vector sampleNamesVector, Vector uniqueIDs, Vector clusters, Color[] clusterColors) {
	
	//super (parentFrame, "Gene Template Matching using Pearson's R", modality);
	
	this.expMatrix = expMatrix;
	this.sampleNamesVector = sampleNamesVector;
	this.uniqueIDs = uniqueIDs;
	this.clusters = clusters;
	this.clusterColors = clusterColors;
	SampleNames = getSampleNames();
	
	setBounds(0, 0, 1000, 800); // may need to tinker with this, to set the right size and position of the frame
	//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;
	JPanel pane = new JPanel();
	pane.setLayout(gridbag);
	
	buildConstraints(constraints, 0, 0, 2, 1, 0, 25);
	tabbedPane = new JTabbedPane();
	uidSelectPanel = new UniqueIDSelector();
	clusterSelectPanel = new ClusterSelector();
	templateSelectPanel = new SavedTemplateSelector();
	tabbedPane.addTab("Select a gene as template", uidSelectPanel);
	tabbedPane.addTab("Select a cluster mean as template", clusterSelectPanel);
	tabbedPane.addTab("Select a saved template", templateSelectPanel);
	gridbag.setConstraints(tabbedPane, constraints);
	pane.add(tabbedPane);
	
			/*
			uidSelectPanel = new UniqueIDSelector();
			gridbag.setConstraints(uidSelectPanel, constraints);
			pane.add(uidSelectPanel);
			 */
	
	//placeholder button for top panel
	buildConstraints(constraints, 0, 1, 2, 1, 0, 55);
	tempPanel = new TemplatePanel();
	//JButton button1 = new JButton("Enter template");
	gridbag.setConstraints(tempPanel, constraints);
	pane.add(tempPanel);
	
	// Absolute Value panel
	buildConstraints(constraints, 0, 2, 1, 1, 70, 5);
	abs = new AbsolutePanel();
	gridbag.setConstraints(abs, constraints);
	pane.add(abs);
	
	// threshold prob panel
	buildConstraints(constraints, 0, 3, 1, 1, 0, 10);
	thresh = new ThresholdPanel();
	gridbag.setConstraints(thresh, constraints);
	pane.add(thresh);
	
	buildConstraints(constraints, 0, 4, 1, 1, 0, 5);
	drawTreesPane = new JPanel();
	drawTreesBox = new JCheckBox("Draw hierarchical trees?");
	drawTreesPane.add(drawTreesBox);
	//constraints.anchor = GridBagConstraints.EAST;
	gridbag.setConstraints(drawTreesPane, constraints);
	pane.add(drawTreesPane);
	
	// cancel / ok panel
	buildConstraints(constraints, 1, 2, 1, 3, 30, 0);
	bott = new BottomPanel();
	bott.cancelButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == bott.cancelButton) {
		//    hide();
		//    dispose();
		}
	    }
	});
	
	gridbag.setConstraints(bott, constraints);
	pane.add(bott);
	//JButton button4 = new JButton("Cancel / ok");
	//gridbag.setConstraints(button4, constraints);
	//pane.add(button4);
	this.add(pane);
	//setContentPane(pane);
    }
    
    
    public void setVisible(boolean visible) {
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	
	super.setVisible(visible);
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
    
    
    
    String[] getSampleNames() {
	String[] SampleNames = new String[sampleNamesVector.size()];
	for(int i = 0; i < sampleNamesVector.size(); i++){
	    SampleNames[i] = (String)sampleNamesVector.get(i);
	    
	}
	
	
	return SampleNames;
    }
    
    
    
    
    class UniqueIDSelector extends JPanel{
	JList uniqueIDList;
	GraphCanvas profileDisplayPanel;
	JScrollPane listScrollPane;
	JButton selectButton;
	int maxIndex = -1;
	int minIndex = -1;
	
	
	JSplitPane uIDSplitPane;
	
	UniqueIDSelector() {
	    
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    //constraints.fill = GridBagConstraints.NONE;
	    this.setLayout(gridbag);
	    
	    uniqueIDList = new JList(uniqueIDs);
	    uniqueIDList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    uniqueIDList.setSelectedIndex(0);
	    
	    //AIS -- Use the graph classes
	    profileDisplayPanel = new GraphCanvas();
	    profileDisplayPanel.setGraphBounds(0, sampleNamesVector.size(), -3, 3);
	    profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
	    profileDisplayPanel.setXAxisValue(0);
	    profileDisplayPanel.setYAxisValue(0);
	    
	    uniqueIDList.addListSelectionListener(new ListSelectionListener(){
		public void valueChanged(ListSelectionEvent e) {
		    refreshGraph();
		}
	    });
	    //-- AIS
	    
	    listScrollPane = new JScrollPane(uniqueIDList);
	    
	    uIDSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, profileDisplayPanel);
	    uIDSplitPane.setOneTouchExpandable(true);
	    uIDSplitPane.setDividerLocation(200);
	    
	    Dimension minimumSize = new Dimension(100,50);
	    uniqueIDList.setMinimumSize(minimumSize);
	    profileDisplayPanel.setMinimumSize(minimumSize);
	    
	    //uIDSplitPane.setPreferredSize(new Dimension(700,150));
	    
	    buildConstraints(constraints, 0, 0, 1, 1, 1, 80);
	    constraints.fill = GridBagConstraints.BOTH;
	    gridbag.setConstraints(uIDSplitPane, constraints);
	    this.add(uIDSplitPane);
	    
	    buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
	    constraints.fill = GridBagConstraints.NONE;
	    constraints.anchor = GridBagConstraints.SOUTH;
	    selectButton = new JButton("Select highlighted gene from above list to use as template");
	    selectButton.addActionListener(new ActionListener(){
		
		public void actionPerformed(ActionEvent e) {
		    int index = uniqueIDList.getSelectedIndex();
		    Vector expProfile = getGene(index);
		    float max = getMax(expProfile);
		    float min = getMin(expProfile);
		    
		    if ((max - min) == 0) {
			
			for(int i = 0; i < expProfile.size(); i++) {
			    expProfile.set(i, new Float(0.5));
			}
			
		    } else {
			
			for(int i = 0; i < expProfile.size(); i++) {
			    float f = (((Float)expProfile.get(i)).floatValue()- min)/(max - min);
			    expProfile.set(i, new Float(f));
			}
		    }
		    
		    for(int i  = 0; i < tempPanel.tempScr.tempGrid.length; i++){
			if (!Float.isNaN(((Float)expProfile.get(i)).floatValue())) {
			    tempPanel.tempScr.tempGrid[i].activeBox.setSelected(true);
			    tempPanel.tempScr.tempGrid[i].templateSlider.setEnabled(true);
			    tempPanel.tempScr.tempGrid[i].templateSlider.setValue((int) Math.floor(((Float)expProfile.get(i)).floatValue()*100));
			    tempPanel.tempScr.tempGrid[i].templateField.setEnabled(true);
			    tempPanel.tempScr.tempGrid[i].templateField.setBackground(Color.white);
			    tempPanel.tempScr.tempGrid[i].templateField.setText("" + ((Float)expProfile.get(i)).floatValue());
			} else {
			    tempPanel.tempScr.tempGrid[i].activeBox.setSelected(false);
			    tempPanel.tempScr.tempGrid[i].templateSlider.setEnabled(false);
			    tempPanel.tempScr.tempGrid[i].templateField.setText("NULL");
			    tempPanel.tempScr.tempGrid[i].templateField.setBackground(Color.gray);
			    tempPanel.tempScr.tempGrid[i].templateField.setEnabled(false);
			}
		    }
		}
		
	    });
	    
	    gridbag.setConstraints(selectButton, constraints);
	    this.add(selectButton);
	    refreshGraph();
	}
	
	private void refreshGraph() {
	    
	    int index = uniqueIDList.getSelectedIndex();
	    Vector expProfile = getGene(index);
	    float max = getMax(expProfile);
	    float min = getMin(expProfile);
	    
	    profileDisplayPanel.removeAllGraphElements();
	    for (int i = 0; i < expProfile.size(); i++) {
		if (!Float.isNaN(((Float) expProfile.elementAt(i)).floatValue())) {
		    GraphPoint gp = new GraphPoint(i /*+ 1*/, ((Float) expProfile.elementAt(i)).floatValue(), Color.red, 5);
		    profileDisplayPanel.addGraphElement(gp);
		}
	    }
	    
	    for (int i = 0; i < expProfile.size() - 1; i++) {
		if ((Float.isNaN(((Float) expProfile.elementAt(i)).floatValue())) || (Float.isNaN(((Float) expProfile.elementAt(i+1)).floatValue()))) {
		    continue;
		}
		GraphLine gl = new GraphLine(i /*+ 1*/, ((Float) expProfile.elementAt(i)).floatValue(),
		i + 1, ((Float) expProfile.elementAt(i + 1)).floatValue(), Color.blue);
		profileDisplayPanel.addGraphElement(gl);
		
	    }
	    
	    profileDisplayPanel.repaint();
	}
	
	
    }
    
    
    
    private Vector getGene(int index) {
	Vector gene = new Vector();
	
	for (int i = 0; i < sampleNamesVector.size(); i++) {
	    gene.add(new Float(expMatrix.get(index, i)));
	}
	
	return gene;
    }
    
    
    
    private float getMax(Vector gene) {
	float max = Float.NEGATIVE_INFINITY;
	
	for(int i = 0; i < gene.size(); i++) {
	    if (! Float.isNaN(((Float)gene.get(i)).floatValue())) {
		float current = ((Float)gene.get(i)).floatValue();
		if (current > max) max = current;
	    }
	}
	
	return max;
    }
    
    
    private float getMin(Vector gene) {
	float min = Float.MAX_VALUE;
	
	for(int i = 0; i < gene.size(); i++) {
	    if (! Float.isNaN(((Float)gene.get(i)).floatValue())) {
		float current = ((Float)gene.get(i)).floatValue();
		if (current < min) min = current;
	    }
	}
	
	return min;
    }
    
    
    
    class ClusterSelector extends JPanel {
	JList clusterList;
	GraphCanvas profileDisplayPanel;
	JScrollPane listScrollPane;
	JButton selectButton;
	
	Vector clusterNames; // JUST THE NAMES OF THE CLUSTERS, i.e., "CLUSTER 1", "CLUSTER 2", ETC.
	//Vector clusterContents; // THIS IS A VECTOR OF VECTORS (= THE INDICES OF THE UNIQUE IDS IN A CLUSTER)
	Vector geneDataInClusters; // THIS IS A VECTOR OF VECTORS ( = CLUSTERS) OF VECTORS ( = EXPRESSION PROFILES OF GENES IN THAT CLUSTER).
	Vector averageClusterProfiles;
	
	JSplitPane clusterSplitPane;
	
	ClusterSelector() {
	    
	    clusterNames = new Vector();
	    
	    if (clusters.size() == 0) {
		clusterNames.add("No clusters to show");
	    } else {
		
		for (int i = 0; i < clusters.size(); i++) {
		    clusterNames.add("Cluster " + (i+1));
		}
		
		geneDataInClusters = new Vector();
		averageClusterProfiles = new Vector();
		
		for(int j = 0; j < clusters.size(); j++) {//IN THIS "FOR" LOOP, CURRENT CLUSTER INITIALLY CONTAINS THE INDICES OF THE GENES IN THE CLUSTER, BUT THESE ARE REPLACED BY THE ACUALLY EXPRESSION VECTORS
		    Vector currentCluster = (Vector)clusters.get(j);
		    
		    for (int k = 0; k < currentCluster.size(); k++) {
			int index = ((Integer)currentCluster.get(k)).intValue();
			Vector currentGene = getGene(index);
			currentCluster.set(k, currentGene);
		    }
		    
		    geneDataInClusters.add(currentCluster);
		    
		    Vector meanOfCurrentCluster = getMeanProfile(currentCluster);
		    averageClusterProfiles.add(meanOfCurrentCluster);
		}
	    }
	    
	    
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    //constraints.fill = GridBagConstraints.NONE;
	    this.setLayout(gridbag);
	    
	    clusterList = new JList(clusterNames);
	    clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    clusterList.setSelectedIndex(0);
	    
	    //AIS -- Use the graph classes
	    profileDisplayPanel = new GraphCanvas();
	    profileDisplayPanel.setGraphBounds(0, sampleNamesVector.size(), -3, 3);
	    profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
	    profileDisplayPanel.setXAxisValue(0);
	    profileDisplayPanel.setYAxisValue(0);
	    
	    clusterList.addListSelectionListener(new ListSelectionListener(){
		public void valueChanged(ListSelectionEvent e) {
		    refreshGraph();
		}
	    });
	    //-- AIS
	    
	    listScrollPane = new JScrollPane(clusterList);
	    
	    clusterSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, profileDisplayPanel);
	    clusterSplitPane.setOneTouchExpandable(true);
	    clusterSplitPane.setDividerLocation(200);
	    
	    Dimension minimumSize = new Dimension(100,50);
	    clusterList.setMinimumSize(minimumSize);
	    profileDisplayPanel.setMinimumSize(minimumSize);
	    
	    //clusterSplitPane.setMinimumSize(new Dimension(700,150));
	    
	    buildConstraints(constraints, 0, 0, 1, 1, 1, 80);
	    constraints.fill = GridBagConstraints.BOTH;
	    gridbag.setConstraints(clusterSplitPane, constraints);
	    this.add(clusterSplitPane);
	    
	    buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
	    constraints.fill = GridBagConstraints.NONE;
	    //constraints.anchor = GridBagConstraints.WEST;
	    selectButton = new JButton("Select highlighted cluster from above list to use its mean as template");
	    selectButton.addActionListener(new ActionListener(){
		
		public void actionPerformed(ActionEvent e) {
		    if (clusters.size() == 0){
			JOptionPane.showMessageDialog(null, "No clusters to select");
		    } else {
			int index = clusterList.getSelectedIndex();
			Vector expProfile = (Vector) ((Vector) averageClusterProfiles.get(index)).clone();
			float max = getMax(expProfile);
			float min = getMin(expProfile);
			
			if ((max - min) == 0) {
			    
			    for(int i = 0; i < expProfile.size(); i++) {
				expProfile.set(i, new Float(0.5));
			    }
			    
			} else {
			    
			    for(int i = 0; i < expProfile.size(); i++) {
				float f = (((Float)expProfile.get(i)).floatValue()- min)/(max - min);
				expProfile.set(i, new Float(f));
			    }
			}
			
			for(int i  = 0; i < tempPanel.tempScr.tempGrid.length; i++){
			    tempPanel.tempScr.tempGrid[i].templateSlider.setValue((int) Math.floor(((Float)expProfile.get(i)).floatValue()*100));
			    tempPanel.tempScr.tempGrid[i].templateField.setText("" + ((Float)expProfile.get(i)).floatValue());
			}
		    }
		}
		
	    });
	    
	    gridbag.setConstraints(selectButton, constraints);
	    this.add(selectButton);
	    refreshGraph();
	}
	
	private void refreshGraph() {
	    
	    int index = clusterList.getSelectedIndex();
	    if (clusters.size() <= 0) return;
	    Vector meanProfile = (Vector) averageClusterProfiles.elementAt(index);
	    
	    profileDisplayPanel.removeAllGraphElements();
	    Vector selectedCluster = (Vector)geneDataInClusters.get(index);
	    
	    for (int j = 0; j < selectedCluster.size(); j++) {
		
		Vector currGene = (Vector)selectedCluster.get(j);
		for (int i = 0; i < currGene.size(); i++) {
		}
		
		for (int i = 0; i < currGene.size() - 1; i++) {
		    if ((Float.isNaN(((Float) currGene.elementAt(i)).floatValue()))||(Float.isNaN(((Float) currGene.elementAt(i+1)).floatValue()))) {
			continue;
		    }
		    GraphLine gl = new GraphLine(i /*+ 1*/, ((Float) currGene.elementAt(i)).floatValue(),
		    i + 1, ((Float) currGene.elementAt(i + 1)).floatValue(), clusterColors[index]);
		    profileDisplayPanel.addGraphElement(gl);
		    
		}
	    }
	    
	    for (int i = 0; i < meanProfile.size(); i++) {
		if (!Float.isNaN(((Float) meanProfile.elementAt(i)).floatValue())) {
		    GraphPoint gp = new GraphPoint(i/* + 1*/, ((Float) meanProfile.elementAt(i)).floatValue(), Color.red, 5);
		    profileDisplayPanel.addGraphElement(gp);
		}
	    }
	    
	    for (int i = 0; i < meanProfile.size() - 1; i++) {
		GraphLine gl = new GraphLine(i /*+ 1*/, ((Float) meanProfile.elementAt(i)).floatValue(),
		i + 1, ((Float) meanProfile.elementAt(i + 1)).floatValue(), Color.blue);
		profileDisplayPanel.addGraphElement(gl);
	    }
	    
	    profileDisplayPanel.repaint();
	}
	
	
	Vector getMeanProfile(Vector genesInCluster) {
	    Vector meanProfile = new Vector();
	    float[][] geneMatrix = convertToFloatMatrix(genesInCluster);
	    
	    float[] meanArray;
	    
	    for (int i = 0; i < geneMatrix[0].length; i++){
		float sum = 0;
		
		for (int j = 0; j < geneMatrix.length; j++){
		    if (!Float.isNaN(geneMatrix[j][i]))
			sum += geneMatrix[j][i];
		}
		
		float mean = sum / geneMatrix.length;
		meanProfile.add(new Float(mean));
	    }
	    
	    return meanProfile;
	}
	
	
	
	float[][] convertToFloatMatrix(Vector geneCluster) {
	    float[][] matrix = new float[geneCluster.size()][];
	    
	    for (int i = 0; i < geneCluster.size(); i++) {
		Vector currentGene = (Vector) ((Vector) geneCluster.get(i)).clone();
		matrix[i] = new float[currentGene.size()];
		for (int j = 0; j < currentGene.size(); j++) {
		    matrix[i][j] = ((Float)currentGene.get(j)).floatValue();
		}
	    }
	    
	    return matrix;
	}
	
    }
    
    
    class AbsolutePanel extends JPanel {
	
	JCheckBox absBox;
	JLabel absLabel1;
	JLabel absLabel2;
	
	AbsolutePanel() {
	    
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.NONE;
	    //constraints.anchor = GridBagConstraints.WEST;
	    this.setLayout(gridbag);
	    
	    buildConstraints(constraints, 0, 0, 1, 2, 30, 0);
	    absBox = new JCheckBox("Match to Absolute R?");
	    gridbag.setConstraints(absBox, constraints);
	    this.add(absBox);
	    
	    buildConstraints(constraints, 1, 0, 1, 1, 70, 50);
	    absLabel1 = new JLabel("Yes: both +vely and -vely correlated expression profiles will be matched");
	    constraints.anchor = GridBagConstraints.WEST;
	    gridbag.setConstraints(absLabel1, constraints);
	    this.add(absLabel1);
	    
	    buildConstraints(constraints, 1, 1, 1, 1, 0, 50);
	    absLabel2 = new JLabel("No: only +vely correlated expression profiles will be matched");
	    constraints.anchor = GridBagConstraints.NORTHWEST;
	    gridbag.setConstraints(absLabel2, constraints);
	    this.add(absLabel2);
	    
	}
	
    }
    
    
    class SavedTemplateSelector extends JPanel{
	JList templateList;
	GraphCanvas profileDisplayPanel;
	JScrollPane listScrollPane;
	JButton selectButton;
	JButton loadButton;
	Vector templatesVector = new Vector();
	DefaultListModel listModel;
	
	Vector templates;
	
	JSplitPane templateSplitPane;
	JPanel templateButtonPanel;
	
	SavedTemplateSelector() {
	    
			/*
			 
			listModel = new DefaultListModel();
			 
			//
			//templates = new Vector();
			//for(int i = 0; i < templateArray.length; i++) {
			//	templates.add(templateArray[i]);
			//}
			//
			 
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			//constraints.fill = GridBagConstraints.NONE;
			this.setLayout(gridbag);
			 
			templateList = new JList(listModel);
			templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			templateList.setSelectedIndex(0);
			 
					//AIS -- Use the graph classes
			profileDisplayPanel = new GraphCanvas();
			profileDisplayPanel.setGraphBounds(0, sampleNamesVector.size(), -3, 3);
			profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
			profileDisplayPanel.setXAxisValue(0);
			profileDisplayPanel.setYAxisValue(0);
			 
			templateList.addListSelectionListener(new ListSelectionListener(){
					public void valueChanged(ListSelectionEvent e) {
					refreshGraph();
				}
			});
				    //-- AIS
			 
		//uniqueIDList.addListSelectionListener(new listSelectionListener(){}); //IMPLEMENT THIS LATER
			 
			listScrollPane = new JScrollPane(templateList);
			 
			//profileDisplayPanel = new JPanel();
			templateSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, profileDisplayPanel);
			templateSplitPane.setOneTouchExpandable(true);
			templateSplitPane.setDividerLocation(200);
			 
			Dimension minimumSize = new Dimension(100,50);
			templateList.setMinimumSize(minimumSize);
			profileDisplayPanel.setMinimumSize(minimumSize);
			 
			//templateSplitPane.setPreferredSize(new Dimension(700,150));
			 
			buildConstraints(constraints, 0, 0, 1, 1, 1, 80);
			gridbag.setConstraints(templateSplitPane, constraints);
			this.add(templateSplitPane);
			 
			 
			//buildConstraints(constraints, 0, 1, 1, 1, 50, 20);
			//constraints.fill = GridBagConstraints.NONE;
			//constraints.anchor = GridBagConstraints.SOUTH;
			loadButton = new JButton("Load saved template(s) from file");
			loadButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt) {
					if (evt.getSource() == loadButton) {
						JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
						fc.setCurrentDirectory(new File(fc.getCurrentDirectory().getParentFile(), "Data"));
			 
						int returnVal = fc.showOpenDialog(SavedTemplateSelector.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							listModel.removeAllElements();
							File file = fc.getSelectedFile();
							try {
								DataInputStream in = new DataInputStream(new FileInputStream(file.getName()));
								try {
									//*****READING OF INPUT DATA TAKES PLACE IN THIS BLOCK
									templatesVector = new Vector();
									int index = 0;
									while (true) {
										Vector currentTemplate = new Vector();
										System.out.println("Template " + index);
										for(int i = 0; i < tempPanel.tempScr.tempGrid.length; i++) {
											float f = in.readFloat();
											System.out.println("" + f);
											currentTemplate.add(new Float(f));
										}
										templatesVector.add(currentTemplate);
										listModel.addElement("Template" + (index+1));
										index++;
									}
			 
								} catch (EOFException exc) {
									in.close();
								}
							} catch (IOException e){
								System.out.println("Error: " + e.toString());
								JOptionPane.showMessageDialog(null, e.toString());
							}
						}
					}
				}
			});
			//gridbag.setConstraints(loadButton, constraints);
			//this.add(loadButton);
			 
			//buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
			//constraints.fill = GridBagConstraints.NONE;
			//constraints.anchor = GridBagConstraints.SOUTH;
			selectButton = new JButton("Select saved template from above list");
			selectButton.addActionListener(new ActionListener(){
			 
				public void actionPerformed(ActionEvent e) {
							if (listModel.size() == 0){
								JOptionPane.showMessageDialog(null, "No templates to select");
							} else {
								int index = templateList.getSelectedIndex();
								Vector expProfile = (Vector) ((Vector) templatesVector.get(index)).clone();
			 
								/*
								float max = getMax(expProfile);
								float min = getMin(expProfile);
			 
								if ((max - min) == 0) {
			 
									for(int i = 0; i < expProfile.size(); i++) {
										expProfile.set(i, new Float(0.5));
									}
			 
								} else {
			 
									for(int i = 0; i < expProfile.size(); i++) {
										float f = (((Float)expProfile.get(i)).floatValue()- min)/(max - min);
										expProfile.set(i, new Float(f));
									}
								}
								//
			 
								for(int i  = 0; i < tempPanel.tempScr.tempGrid.length; i++){
									tempPanel.tempScr.tempGrid[i].templateSlider.setValue((int) Math.floor(((Float)expProfile.get(i)).floatValue()*100));
									tempPanel.tempScr.tempGrid[i].templateField.setText("" + ((Float)expProfile.get(i)).floatValue());
								}
							}
						}
			 
					});
			//gridbag.setConstraints(selectButton, constraints);
			//this.add(selectButton);
			buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
			 
			templateButtonPanel.add(loadButton);
			templateButtonPanel.add(selectButton);
			gridbag.setConstraints(templateButtonPanel, constraints);
			this.add(templateButtonPanel);
			refreshGraph();
			 */
	}
	
	private void refreshGraph() {
	    
	    int index = templateList.getSelectedIndex();
	    if (index < 0) return;
	    Vector expProfile = (Vector) ((Vector) templatesVector.get(index)).clone();
	    
	    profileDisplayPanel.removeAllGraphElements();
	    for (int i = 0; i < expProfile.size(); i++) {
		GraphPoint gp = new GraphPoint(i + 1, ((Float) expProfile.elementAt(i)).floatValue(), Color.red, 2);
		profileDisplayPanel.addGraphElement(gp);
	    }
	    
	    for (int i = 0; i < expProfile.size() - 1; i++) {
		GraphLine gl = new GraphLine(i + 1, ((Float) expProfile.elementAt(i)).floatValue(),
		i + 2, ((Float) expProfile.elementAt(i + 1)).floatValue(), Color.blue);
		profileDisplayPanel.addGraphElement(gl);
	    }
	    
	    profileDisplayPanel.repaint();
	}
	
	
    }
    
    
    
    class ThresholdPanel extends JPanel {
	
	JLabel threshLabel1;
	JLabel threshLabel2;
	JTextField threshInputField;
	JRadioButton chooseR;
	JRadioButton chooseP;
	
	ThresholdPanel() {
	    
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.NONE;
	    
	    this.setLayout(gridbag);
	    
	    ButtonGroup chooseRorP = new ButtonGroup();
	    chooseR = new JRadioButton("Use Threshold R",false);
	    chooseRorP.add(chooseR);
	    chooseP = new JRadioButton("Use Threshold p-Value",true);
	    chooseRorP.add(chooseP);
	    
	    buildConstraints(constraints, 0, 0, 1, 1, 10, 50);
	    constraints.anchor = GridBagConstraints.SOUTHWEST;
	    //constraints.fill = GridBagConstraints.NONE;
	    gridbag.setConstraints(chooseR, constraints);
	    this.add(chooseR);
	    
	    buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
	    constraints.anchor = GridBagConstraints.NORTHWEST;
	    gridbag.setConstraints(chooseP, constraints);
	    this.add(chooseP);
	    
	    buildConstraints(constraints, 1, 0, 1, 1, 80, 0);
	    constraints.anchor = GridBagConstraints.SOUTHWEST;
	    threshLabel1 = new JLabel("Enter magnitude of threshold R or p-Value (between 0 and 1 inclusive)");
	    gridbag.setConstraints(threshLabel1, constraints);
	    this.add(threshLabel1);
	    
	    buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
	    constraints.anchor = GridBagConstraints.NORTHWEST;
	    threshLabel2 = new JLabel("at which selected profiles should be correlated to template");
	    gridbag.setConstraints(threshLabel2, constraints);
	    this.add(threshLabel2);
	    
	    buildConstraints(constraints, 2, 0, 1, 2, 10, 0);
	    constraints.anchor = GridBagConstraints.WEST;
	    //constraints.fill = GridBagConstraints.HORIZONTAL;
	    //threshInputField = new JTextField("0.80",7);
	    threshInputField = new JTextField("", 7);
	    threshInputField.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
		    
		    if(evt.getSource() == threshInputField){
			try {
                            
                            System.out.println("in gene dialog, ptm");
			    String s = threshInputField.getText();
			    double r = Double.parseDouble(s);
			    
			    if ((r > 1)||(r < 0)) {
				JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
				threshInputField.setText("");
			    }
			} catch (Exception exc) {
			    JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
			    threshInputField.setText("");
			}
			
		    }
		    
		}
	    });
	    
	    gridbag.setConstraints(threshInputField, constraints);
	    this.add(threshInputField);
	    
	}
	
    }
    
    
    class BottomPanel extends JPanel {
	
	JButton resetButton;
	JButton okButton;
	JButton cancelButton;
	JButton saveButton;
	
	BottomPanel() {
	    
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.NONE;
	    this.setLayout(gridbag);
	    
	    buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
	    saveButton = new JButton("Save template to file");
	    gridbag.setConstraints(saveButton, constraints);
	    saveButton.setEnabled(false);
	    this.add(saveButton);
	    saveButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
		    if (evt.getSource() == saveButton) {
			JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
			
			int returnVal = fc.showSaveDialog(BottomPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    try {
				DataOutputStream templateData = new DataOutputStream(new FileOutputStream(file.getName(), true));
				for(int i = 0; i < tempPanel.tempScr.tempGrid.length; i++) {
				    String s = tempPanel.tempScr.tempGrid[i].templateField.getText();
				    float f = Float.parseFloat(s);
				    templateData.writeFloat(f);
				    
				}
				
				templateData.close();
			    } catch (IOException e) {
				System.out.println("Error: " + e.toString());
			    }
			}
		    }
		}
	    });
	    
	    
	    buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
	    resetButton = new JButton("Reset");
	    resetButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
		    if (evt.getSource() == resetButton) {
			for(int i  = 0; i < tempPanel.tempScr.tempGrid.length; i++){
			    tempPanel.tempScr.tempGrid[i].reset();
			}
			
			thresh.threshInputField.setText("");
			abs.absBox.setSelected(false);
		    }
		}
		
	    });
	    
	    gridbag.setConstraints(resetButton, constraints);
	    this.add(resetButton);
	    
	    
	    buildConstraints(constraints, 0, 2, 1, 1, 100, 100);
	    okButton = new JButton("OK");
	    okButton.addActionListener(new EventListener());
	    
	    
	    gridbag.setConstraints(okButton, constraints);
	    this.add(okButton);
	    
	    buildConstraints(constraints, 0, 3, 1, 1, 100, 100);
	    cancelButton = new JButton("Cancel");
	    gridbag.setConstraints(cancelButton, constraints);
	    this.add(cancelButton);
	    
	}
    }
    
    
    class TemplatePanel extends JPanel {
	
	TemplateScroller tempScr;
	
	TemplatePanel() {
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.BOTH;
	    this.setLayout(gridbag);
	    
	    tempScr = new TemplateScroller();
	    JScrollPane scroller = tempScr.createTemplateScroller();
	    buildConstraints(constraints, 0, 0, 1, 1, 100, 95);
	    gridbag.setConstraints(scroller, constraints);
	    this.add(scroller);
	    
	    buildConstraints(constraints, 0, 1, 1, 1, 100, 5);
	    constraints.fill = GridBagConstraints.NONE;
	    
	}
	
	
	
	class TemplateScroller extends JPanel {
	    
	    TemplateElementGrid[] tempGrid = new TemplateElementGrid[SampleNames.length];
	    
	    JScrollPane createTemplateScroller() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		this.setLayout(gridbag);
		
		for (int i = 0; i < SampleNames.length; i++) {
		    tempGrid[i] = new TemplateElementGrid(SampleNames[i]);
		    buildConstraints(constraints, i, 0, 1, 1, 100, 100);
		    gridbag.setConstraints(tempGrid[i], constraints);
		    this.add(tempGrid[i]);
		}
		
		JScrollPane scroller = new JScrollPane(this);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scroller;
	    }
	    
	    
	    
	    class TemplateElementGrid extends JPanel {
		
		JLabel exptName;
		JTextField templateField;
		JSlider templateSlider;
		JCheckBox activeBox;
		
		TemplateElementGrid(String SampleName) {
		    GridBagLayout templateElement = new GridBagLayout();
		    GridBagConstraints constraints = new GridBagConstraints();
		    constraints.fill = GridBagConstraints.BOTH;
		    this.setLayout(templateElement);
		    
		    buildConstraints(constraints, 0, 0, 1, 1, 50, 10);
		    exptName = new JLabel(SampleName, SwingConstants.CENTER);
		    templateElement.setConstraints(exptName, constraints);
		    this.add(exptName);
		    
		    buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
		    activeBox = new JCheckBox();
		    activeBox.setSelected(true);
		    activeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() == ItemEvent.DESELECTED) {
				//templateField.setText("NULL");
				templateField.setBackground(Color.gray);
				templateField.setEnabled(false);
				templateSlider.setEnabled(false);
			    }
			    
			    if (e.getStateChange() == ItemEvent.SELECTED) {
				//templateField.setText("NULL");
				templateField.setBackground(Color.white);
				templateField.setEnabled(true);
				templateSlider.setEnabled(true);
			    }
			    
			}
		    });
		    templateElement.setConstraints(activeBox, constraints);
		    this.add(activeBox);
		    
		    buildConstraints(constraints, 0, 1, 2, 1, 0, 5);
		    templateField = new JTextField("0.5", 4);
		    templateField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    int sliderValue;
			    if(e.getSource() == templateField){
				try {
				    String s = templateField.getText();
				    double d = Double.parseDouble(s);
				    sliderValue = (int)Math.floor(d*100);
				    if ((d > 1)||(d < 0)) {
					
					sliderValue = 50;
					templateField.setText("0.5");
					JOptionPane.showMessageDialog(null, "Template values must be between 0 and 1 (inclusive)");
					
				    }
				} catch (Exception exc) {
				    
				    templateField.setText("0.5");
				    sliderValue = 50;
				    JOptionPane.showMessageDialog(null, "Template values must be between 0 and 1 (inclusive)");
				    
				}
				
				templateSlider.setValue(sliderValue);
			    }
			    
			}
		    });
		    
		    templateField.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e) {
			    int sliderValue;
			    if(e.getSource() == templateField){
				try {
				    String s = templateField.getText();
				    double d = Double.parseDouble(s);
				    sliderValue = (int)Math.floor(d*100);
				    if ((d > 1)||(d < 0)) {
					
					sliderValue = 50;
					templateField.setText("0.5");
					JOptionPane.showMessageDialog(null, "Template values must be between 0 and 1 (inclusive)");
					
				    }
				} catch (Exception exc) {
				    
				    templateField.setText("0.5");
				    sliderValue = 50;
				    JOptionPane.showMessageDialog(null, "Template values must be between 0 and 1 (inclusive)");
				    
				}
				
				templateSlider.setValue(sliderValue);
			    }
			}
			
			
			public void focusGained(FocusEvent e) {
			}
		    });
		    
		    templateElement.setConstraints(templateField, constraints);
		    this.add(templateField);
		    
		    buildConstraints(constraints, 0, 2, 1, 2, 0, 85);
		    templateSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
		    templateSlider.setMajorTickSpacing(20);
		    templateSlider.setMinorTickSpacing(10);
		    //templateSlider.setPaintTicks(true);
		    Hashtable labelTable = new Hashtable();
		    labelTable.put( new Integer( 0 ), new JLabel("Min") );
		    labelTable.put( new Integer( 100 ), new JLabel("Max") );
		    templateSlider.setLabelTable( labelTable );
		    templateSlider.setPaintLabels(true);
		    templateSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
			    JSlider source = (JSlider)e.getSource();
			    if (!source.getValueIsAdjusting()) {
				int value = (int)source.getValue();
				
				if (value == 100) {
				    templateField.setText("1.0"); //CLUMSY WAY OF DISPLAYING INTEGER AS DOUBLE? MAYBE CHANGE?
				} else if (value > 0 && value < 10) {
				    templateField.setText("0.0" + value);
				} else {
				    templateField.setText("0." + value);
				}
			    }
			}
			
		    });
		    templateElement.setConstraints(templateSlider, constraints);
		    this.add(templateSlider);
		    
		    
		}
		
		
		
		public void reset() {
		    templateSlider.setValue(50);
		    templateField.setText("0.5");
		}
		
		
	    }
	    
	    
	    
	}
	
	
    }
    
    
    
    public Vector getTemplate() {
	Vector geneTemplate = new Vector();
	
	for(int i = 0; i < sampleNamesVector.size(); i++) {
	    String s = tempPanel.tempScr.tempGrid[i].templateField.getText();
	    if (s.equals("NaN")||(s.equals("NULL"))||(!(tempPanel.tempScr.tempGrid[i].activeBox.isSelected()))) {
		geneTemplate.add(new Float(Float.NaN));
	    } else {
		geneTemplate.add(new Float(s));
	    }
	}
	
	return geneTemplate;
    }
    
    
    public FloatMatrix convertTemplateVectorToFloatMatrix() {
	template = getTemplate();
	FloatMatrix templateMatrix = new FloatMatrix(1, template.size());
	for (int i = 0; i < template.size(); i++) {
	    templateMatrix.A[0][i] = ((Float) (template.get(i))).floatValue();
	}
	
	return templateMatrix;
    }
    
    
    protected void fireOkButtonEvent() {
	templateMatrix = convertTemplateVectorToFloatMatrix();
	boolean absoluteSelected = abs.absBox.isSelected();
	boolean drawTreesSelected = drawTreesBox.isSelected();
	boolean rSelected = thresh.chooseR.isSelected();
	String s = thresh.threshInputField.getText();
	double threshR = Double.parseDouble(s);
	
	Hashtable hash = new Hashtable();
	hash.put(new String("drawTrees"), new Boolean(drawTreesSelected)); //Should be a checkbox in the init dialog
	hash.put(new String("useAbsolute"), new Boolean(absoluteSelected));
	hash.put(new String("useR"), new Boolean(rSelected));
	hash.put(new String("template"), template);
	hash.put(new String("thresholdR"), new Double(threshR));
	
	//fireEvent(new ActionInfoEvent(this, hash));
	
    }
    
    public boolean isDrawTrees() {
	return drawTreesBox.isSelected();
    }
    
    public boolean isUseAbsolute() {
	return abs.absBox.isSelected();
    }
    
    public boolean isUseR() {
	return thresh.chooseR.isSelected();
    }
    
    public FloatMatrix getTemplateMatrix() {
	return convertTemplateVectorToFloatMatrix();
    }
    
    public double getThresholdR() {
	return Double.parseDouble(thresh.threshInputField.getText());
    }
    
    public boolean isOkPressed() {return this.okPressed;}
    
    protected class EventListener implements ActionListener {
	
	public void actionPerformed(ActionEvent event) {
	    
	    Object source = event.getSource();
	    
	    if (source == bott.okButton) {
		try {
		    String s = thresh.threshInputField.getText();
		    double r = Double.parseDouble(s);
		    
		    if ((r > 1)||(r < 0)) {
			JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
			thresh.threshInputField.setText("0.80");
		    }
		    else {
			setVisible(false);
			fireOkButtonEvent();
			okPressed = true;
			//dispose();
		    }
		    
		} catch (Exception exc) {
		    //exc.printStackTrace();
		    JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
		    thresh.threshInputField.setText("0.80");
		}
		
		
		
	    }
	}
    }
    
    
}
