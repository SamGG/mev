/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PTMInitDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.tigr.util.*;
import org.tigr.util.awt.*;
import org.tigr.graph.*;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;


public class PTMInitDialog extends AlgorithmDialog {
    
    String[] SampleNames;
    Vector sampleNamesVector;
    Vector uniqueIDs;
    Vector clusters; //THIS IS A VECTOR OF VECTORS (EACH SUB-VECTOR CONTAINS THE INDICES OF UNIQUEIDS IN THAT CLUSTER)
    Vector expClusters;
    Color[] clusterColors;
    Color[] expClusterColors;
    GeneSelector geneSelectPanel;
    GeneClusterSelector geneClusterSelectPanel;
    ExperimentSelector expSelectPanel;
    ExperimentClusterSelector clusterSelectPanel;
    SavedTemplateSelector templateSelectPanel;
    TemplatePanel tempPanel;
    ThresholdPanel thresh;
    FloatMatrix expMatrix;
    JCheckBox drawTreesBox;
    JPanel drawTreesPane;
    JTabbedPane tabbedPane;
    JButton saveTemplateButton;
    Vector template = new Vector();;
    FloatMatrix templateMatrix;
    boolean setTemplate = false;
    boolean clusterGenes = true;
    int numberOfExperiments;
    private boolean okPressed = false;
    
    
    public PTMInitDialog(JFrame parentFrame, boolean modality, FloatMatrix expMatrix, Vector uniqueIDs, Vector sampleNamesVector, Vector clusters, Vector expClusters, Color[] clusterColors, Color[] expClusterColors) {
        
        super (parentFrame, "PTM: Pavlidis Template Matching", modality);
        
        this.expMatrix = expMatrix;
        this.numberOfExperiments = expMatrix.getColumnDimension();
        this.uniqueIDs = uniqueIDs;
        this.sampleNamesVector = sampleNamesVector;
        this.clusters = clusters;
        this.clusterColors = clusterColors;
        this.expClusters = expClusters;
        this.expClusterColors = expClusterColors;
        SampleNames = getSampleNames();
      //  setBounds(0, 0, 1000, 800); // may need to tinker with this, to set the right size and position of the frame
        setBounds(0,0,890,700);
        setSize(890, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        buildConstraints(constraints, 0, 0, 2, 1, 0, 10);
        tabbedPane = new JTabbedPane();
        geneSelectPanel = new GeneSelector();
        geneClusterSelectPanel = new GeneClusterSelector();
        expSelectPanel = new ExperimentSelector();
        clusterSelectPanel = new ExperimentClusterSelector();
        templateSelectPanel = new SavedTemplateSelector();
        
        tabbedPane.addTab("Gene Templates", geneSelectPanel);
        tabbedPane.addTab("Gene Cluster Templates", geneClusterSelectPanel);
        tabbedPane.addTab("Experiment Templates", expSelectPanel);
        tabbedPane.addTab("Experiment Cluster Templates", clusterSelectPanel);
        tabbedPane.addTab("Select a Saved Template", templateSelectPanel);
        tabbedPane.setSelectedIndex(0);
        
        gridbag.setConstraints(tabbedPane, constraints);
        pane.add(tabbedPane);
        
        //placeholder button for top panel
        buildConstraints(constraints, 0, 1, 2, 1, 0, 10);
        tempPanel = new TemplatePanel();
        tempPanel.setPreferredSize(new Dimension(80, 400));
        //JButton button1 = new JButton("Enter template");
        gridbag.setConstraints(tempPanel, constraints);
        pane.add(tempPanel);
        
        tabbedPane.addChangeListener(new ChangeListener(){
            
            public void stateChanged(ChangeEvent evt){
                int index = tabbedPane.getSelectedIndex();
                if(index > 1){
                    tempPanel.setEnabled(false);
                    tempPanel.setVisible(false);
                }
                else{
                    tempPanel.setEnabled(true);
                    tempPanel.setVisible(true);
                }
            }
            
        });
        
        // threshold prob panel
        buildConstraints(constraints, 0, 2, 1, 1, 20, 0);
        thresh = new ThresholdPanel();
        gridbag.setConstraints(thresh, constraints);
        pane.add(thresh);
        
        buildConstraints(constraints, 1,2, 1, 1, 5, 0);
        
        drawTreesPane = new JPanel();
        drawTreesPane.setLayout(new GridBagLayout());
        drawTreesBox = new JCheckBox("Construct Hierarchical Trees");
        drawTreesBox.setFocusPainted(false);
        saveTemplateButton = new JButton("Save Current Template", GUIFactory.getIcon("save16.gif"));
        saveTemplateButton.setToolTipText("Saves template to file");
        saveTemplateButton.setFocusPainted(false);
        saveTemplateButton.setActionCommand("save-template-to-file-command");
        saveTemplateButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        saveTemplateButton.setMargin(new Insets(2,50,2,5));
        saveTemplateButton.setPreferredSize(new Dimension(175,30));
        saveTemplateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == saveTemplateButton) {
                    JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                    fc.setCurrentDirectory(new File("Data"));
                    
                    int returnVal = fc.showSaveDialog(PTMInitDialog.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            //   DataOutputStream templateData = new DataOutputStream(new FileOutputStream(file.getPath(), true));
                            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getPath()));
                            if(tabbedPane.getSelectedIndex() < 2){
                                for(int i = 0; i < tempPanel.tempScr.tempGrid.length; i++) {
                                    String s = tempPanel.tempScr.tempGrid[i].templateField.getText();
                                    float f = Float.parseFloat(s);
                                    //                                    templateData.writeFloat(f);
                                    bw.write(Float.toString(f)+"\n");
                                }
                            }
                            else{
                                for(int i = 0; i < template.size() ; i++){
                                    bw.write(((Float)template.elementAt(i)).toString()+"\n");
                                    //templateData.writeFloat(((Float)template.elementAt(i)).floatValue());
                                }
                            }
                            bw.flush();
                            bw.close();
                            // templateData.close();
                        } catch (IOException e) {
                            System.out.println("Error: " + e.toString());
                        }
                    }
                }
            }
        });
        
        drawTreesPane.add(saveTemplateButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,30,0,20),0,0));
        drawTreesPane.add(drawTreesBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,30,0,20),0,0));
        constraints.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(drawTreesPane, constraints);
        buildConstraints(constraints, 1, 2, 1, 1, 5, 0);
        gridbag.setConstraints(drawTreesPane, constraints);
        pane.add(drawTreesPane);
        addContent(pane);
        setActionListeners(new EventListener());
    }
    
    
    public void setVisible(boolean visible) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
    }
    
    public boolean isGeneTemplate(){
        return this.clusterGenes;
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
    
    
    
    
    class ExperimentSelector extends JPanel{
        JList expList;
        GraphCanvas profileDisplayPanel;
        JScrollPane listScrollPane;
        JButton selectButton;
        int maxIndex = -1;
        int minIndex = -1;
        
        JSplitPane expSplitPane;
        
        ExperimentSelector() {
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            expList = new JList(sampleNamesVector);
            expList.setSelectedIndex(0);
            
            //AIS -- Use the graph classes
            profileDisplayPanel = new GraphCanvas();
            profileDisplayPanel.setGraphBounds(0, expMatrix.getRowDimension(), -3, 3);
            profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
            profileDisplayPanel.setXAxisValue(0);
            profileDisplayPanel.setYAxisValue(0);
            
            //AIS -- Use the graph classes
        /*    expProfileDisplayPanel = new GraphCanvas();
            expProfileDisplayPanel.setGraphBounds(0, expMatrix.getRowDimension(), -3, 3);
            expProfileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
            expProfileDisplayPanel.setXAxisValue(0);
            expProfileDisplayPanel.setYAxisValue(0);
         */
            expList.addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    refreshGraph();
                }
            });
            //-- AIS
            
            listScrollPane = new JScrollPane(expList);
            
            expSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, profileDisplayPanel);
            expSplitPane.setOneTouchExpandable(true);
            expSplitPane.setDividerLocation(200);
            
            Dimension minimumSize = new Dimension(100,50);
            expList.setMinimumSize(minimumSize);
            profileDisplayPanel.setMinimumSize(minimumSize);
            
            //uIDSplitPane.setPreferredSize(new Dimension(700,150));
            
            buildConstraints(constraints, 0, 0, 1, 1, 1, 1);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(expSplitPane, constraints);
            this.add(expSplitPane);
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.insets = new Insets(10,0,10,0);
            selectButton = new JButton("Select highlighted experiments from above list to use as template", GUIFactory.getIcon("select_check24.gif"));
            selectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            selectButton.setMargin(new Insets(10,10,10,10));
            selectButton.setPreferredSize(new Dimension(400,30));
            selectButton.setFocusPainted(false);
            selectButton.addActionListener(new ActionListener(){
                
                public void actionPerformed(ActionEvent e) {
                    int [] index = expList.getSelectedIndices();
                    Vector expProfile = new Vector();
                    Vector expProfiles = new Vector();
                    
                    for(int j = 0; j < index.length; j++){
                        
                        expProfile = getExperiment(index[j]);
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
                        expProfiles.add(expProfile);
                    }
                    
                    template = getMeanProfile(expProfiles);
                    setTemplate = true;
                    clusterGenes = false;
                    refreshGraph();
                }
                
            });
            
            gridbag.setConstraints(selectButton, constraints);
            this.add(selectButton);
            refreshGraph();
        }
        
        private void refreshGraph() {
            profileDisplayPanel.removeAllGraphElements();
            //  int index = expList.getSelectedIndex();
            int [] indices = expList.getSelectedIndices();
            Vector expProfile;
            Vector expProfiles = new Vector();
            float max = Float.NEGATIVE_INFINITY;
            float min = Float.POSITIVE_INFINITY;
            float maxRange;
            for(int exp = 0; exp < indices.length; exp++){
                expProfile = getExperiment(indices[exp]);
                expProfiles.add(expProfile);
                max = Math.max(max, getMax(expProfile));
                min = Math.min(min, getMin(expProfile));
                maxRange = Math.max(max, Math.abs(min));
                profileDisplayPanel.setGraphBounds(0,expProfile.size(),-maxRange,maxRange);
                
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
            }
            
            expProfile = getMeanProfile(expProfiles);
            
            if(setTemplate){
                
                for (int i = 0; i < expProfile.size() - 1; i++) {
                    if ((Float.isNaN(((Float) expProfile.elementAt(i)).floatValue())) || (Float.isNaN(((Float) expProfile.elementAt(i+1)).floatValue()))) {
                        continue;
                    }
                    GraphLine gl = new GraphLine(i /*+ 1*/, ((Float) expProfile.elementAt(i)).floatValue(),
                    i + 1, ((Float) expProfile.elementAt(i + 1)).floatValue(), Color.green);
                    profileDisplayPanel.addGraphElement(gl);
                }
            }
            profileDisplayPanel.repaint();
        }

        public void reset(){
            expList.setSelectedIndex(0);
            setTemplate = false;
            refreshGraph();
        }
    }
    
    Vector getMeanProfile(Vector expInCluster) {
        Vector meanProfile = new Vector();
        float[][] expMatrix = this.convertToFloatMatrix(expInCluster);
        float[] meanArray;
        int n = 0;
        float mean;
        
        for (int i = 0; i < expMatrix[0].length; i++){
            float sum = 0;
            n = 0;
            for (int j = 0; j < expMatrix.length; j++){
                if (!Float.isNaN(expMatrix[j][i])){
                    sum += expMatrix[j][i];
                    n++;
                }
            }
            mean = (n > 0) ? (sum / (float)n) : 0;
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
    
    
    private Vector getExperiment(int index) {
        Vector exp = new Vector();
        int rows = expMatrix.getRowDimension();
        for (int i = 0; i < rows; i++) {
            exp.add(new Float(expMatrix.get(i, index)));
        }
        return exp;
    }
    
    
    
    private float getMax(Vector element) {
        float max = Float.NEGATIVE_INFINITY;
        
        for(int i = 0; i < element.size(); i++) {
            if (! Float.isNaN(((Float)element.get(i)).floatValue())) {
                float current = ((Float)element.get(i)).floatValue();
                if (current > max) max = current;
            }
        }
        
        return max;
    }
    
    
    private float getMin(Vector element) {
        float min = Float.MAX_VALUE;
        
        for(int i = 0; i < element.size(); i++) {
            if (! Float.isNaN(((Float)element.get(i)).floatValue())) {
                float current = ((Float)element.get(i)).floatValue();
                if (current < min) min = current;
            }
        }
        return min;
    }
    
    
    
    class ExperimentClusterSelector extends JPanel {
        JList clusterList;
        GraphCanvas profileDisplayPanel;
        JScrollPane listScrollPane;
        JButton selectButton;
        
        Vector clusterNames; // JUST THE NAMES OF THE CLUSTERS, i.e., "CLUSTER 1", "CLUSTER 2", ETC.
        //Vector clusterContents; // THIS IS A VECTOR OF VECTORS (= THE INDICES OF THE UNIQUE IDS IN A CLUSTER)
        Vector expDataInClusters; // THIS IS A VECTOR OF VECTORS ( = CLUSTERS) OF VECTORS ( = EXPRESSION PROFILES OF GENES IN THAT CLUSTER).
        Vector averageClusterProfiles;
        
        JSplitPane clusterSplitPane;
        
        ExperimentClusterSelector() {
            
            clusterNames = new Vector();
            
            if (expClusters.size() == 0) {
                clusterNames.add("No experiment clusters to show");
            } else {
               
                for (int i = 0; i < expClusters.size(); i++) {
                    clusterNames.add("Exp. Cluster " + (i+1));
                }
                
                expDataInClusters = new Vector();
                averageClusterProfiles = new Vector();
                int index;
                for(int j = 0; j < expClusters.size(); j++) {//IN THIS "FOR" LOOP, CURRENT CLUSTER INITIALLY CONTAINS THE INDICES OF THE GENES IN THE CLUSTER, BUT THESE ARE REPLACED BY THE ACUALLY EXPRESSION VECTORS
                    Vector currentCluster = (Vector)expClusters.get(j);
                    Vector cluster = new Vector();
                    System.out.println("Current cluster size = "+currentCluster.size());
                    for (int k = 0; k < currentCluster.size(); k++) {
                        index = ((Integer)currentCluster.get(k)).intValue();
                        Vector currentGene = getExperiment(index);
                        //currentCluster.set(k, currentGene);
                        cluster.add(currentGene);
                    }
                    
                    expDataInClusters.add(cluster);
                    
                    Vector meanOfCurrentCluster = getMeanProfile(cluster);
                    
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
            
            buildConstraints(constraints, 0, 0, 1, 1, 1, 1);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(clusterSplitPane, constraints);
            this.add(clusterSplitPane);
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.insets = new Insets(10,0,10,0);
            selectButton = new JButton("Select highlighted cluster from above list to use its mean as template", GUIFactory.getIcon("select_check24.gif"));
            selectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            selectButton.setMargin(new Insets(10,10,10,10));
            selectButton.setPreferredSize(new Dimension(430,30));
            selectButton.setFocusPainted(false);
            selectButton.addActionListener(new ActionListener(){
                
                public void actionPerformed(ActionEvent e) {
                    if (expClusters.size() == 0){
                        JOptionPane.showMessageDialog(null, "No experiment clusters to select");
                    } else {
                        int c = clusterList.getSelectedIndex();
                        Vector v = (Vector)(expClusters.elementAt(c));
                        
                        

                        int [] index = new int[v.size()];
                        for(int i = 0; i < index.length; i++)
                            index[i] = ((Integer)v.elementAt(i)).intValue();
                        
                        Vector expProfile = new Vector();
                    Vector expProfiles = new Vector();
                    
                    for(int j = 0; j < index.length; j++){
                        
                        expProfile = getExperiment(index[j]);
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
                        expProfiles.add(expProfile);
                    }
                    
                    template = getMeanProfile(expProfiles);
                    setTemplate = true;
                    clusterGenes = false;  
                        okButton.setEnabled(true);
                    }
                }
                
            });
            
            gridbag.setConstraints(selectButton, constraints);
            this.add(selectButton);
            refreshGraph();
        }
        
        private void refreshGraph() {
            
            int index = clusterList.getSelectedIndex();
            if (expClusters.size() <= 0) return;
            Vector meanProfile = (Vector) averageClusterProfiles.elementAt(index);
            Vector currExp;
            GraphLine gL;
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            float maxRange;
            profileDisplayPanel.removeAllGraphElements();
            Vector selectedCluster = (Vector)expDataInClusters.get(index);
            
            for (int j = 0; j < selectedCluster.size(); j++) {
                
                currExp = (Vector)selectedCluster.get(j);
                
                max = Math.max(max, getMax(currExp));
                min = Math.min(min, getMin(currExp));
                maxRange = Math.max(max, Math.abs(min));
                profileDisplayPanel.setGraphBounds(0, currExp.size(), -maxRange, maxRange);
                
                for (int i = 0; i < currExp.size() - 1; i++) {
                    if ((Float.isNaN(((Float) currExp.elementAt(i)).floatValue()))||(Float.isNaN(((Float) currExp.elementAt(i+1)).floatValue()))) {
                        continue;
                    }
                    gL = new GraphLine(i /*+ 1*/, ((Float) currExp.elementAt(i)).floatValue(),
                    i + 1, ((Float) currExp.elementAt(i + 1)).floatValue(), expClusterColors[index]);
                    profileDisplayPanel.addGraphElement(gL);
                }
            }
            
            for (int i = 0; i < meanProfile.size(); i++) {
                if (!Float.isNaN(((Float) meanProfile.elementAt(i)).floatValue())) {
                    GraphPoint gp = new GraphPoint(i/* + 1*/, ((Float) meanProfile.elementAt(i)).floatValue(), Color.red, 5);
                    profileDisplayPanel.addGraphElement(gp);
                }
            }
            
            for (int i = 0; i < meanProfile.size() - 1; i++) {
                gL = new GraphLine(i /*+ 1*/, ((Float) meanProfile.elementAt(i)).floatValue(),
                i + 1, ((Float) meanProfile.elementAt(i + 1)).floatValue(), Color.blue);
                profileDisplayPanel.addGraphElement(gL);
            }
            
            if(setTemplate){
                
                for (int i = 0; i < meanProfile.size() - 1; i++) {
                    if ((Float.isNaN(((Float) meanProfile.elementAt(i)).floatValue())) || (Float.isNaN(((Float) meanProfile.elementAt(i+1)).floatValue()))) {
                        continue;
                    }
                    gL = new GraphLine(i /*+ 1*/, ((Float) meanProfile.elementAt(i)).floatValue(),
                    i + 1, ((Float) meanProfile.elementAt(i + 1)).floatValue(), Color.green);
                    profileDisplayPanel.addGraphElement(gL);
                }
            }
            profileDisplayPanel.repaint();
        }
        
        public void reset(){
            clusterList.setSelectedIndex(0);
            setTemplate = false;
            refreshGraph();
        }
        
    }
    
    /*
    class AbsolutePanel extends JPanel {
     
        JCheckBox absBox;
        JLabel absLabel1;
        JLabel absLabel2;
     
        AbsolutePanel() {
     
            GridBagLayout gridbag = new GridBagLayout();
                        GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            //constraints.anchor = GridBagConstraints.WEST;
            this.setLayout(gridbag);
     
            buildConstraints(constraints, 0, 0, 1, 2, 30, 0);
            absBox = new JCheckBox(" Match to Absolute R");
            gridbag.setConstraints(absBox, constraints);
            this.add(absBox);
     
            JPanel tipPanel = new JPanel();
            tipPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Tip on Using Absolute R"));
           // tipPanel.setLayout(gridbag2);
     
            JTextArea text = new JTextArea("If checked, both positively and negatively correlated profiles\n"+
                                            "will be matched.\n"+
                                            "If not checked, only negatively correlated profiles will be matched");
            text.setBackground(new Color(208,208,208));
     
            text.setFont(new Font("Dialog", Font.PLAIN, 12));
         //   buildConstraints(constraints, 0, 0, 1, 1, 70, 50);
         //   absLabel1 = new JLabel("Yes: both +vely and -vely correlated expression profiles will be matched");
        //    constraints.anchor = GridBagConstraints.WEST;
          //  gridbag2.setConstraints(absLabel1, constraints);
          //  tipPanel.add(absLabel1);
     
          //  buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
         //   absLabel2 = new JLabel("No: only +vely correlated expression profiles will be matched");
          //  constraints.anchor = GridBagConstraints.NORTHWEST;
         //   gridbag2.setConstraints(absLabel2, constraints);
         //   tipPanel.add(absLabel2);
            tipPanel.add(text);
     
     
            buildConstraints(constraints, 1, 0, 1, 2, 30, 0);
            gridbag.setConstraints(tipPanel, constraints);
            this.add(tipPanel);
        }
     
    }*/
    
    
    
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
        boolean savedTemplateSelected;
        
        SavedTemplateSelector() {
            
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
            templateList.setSelectedIndex(-1);
            
            //AIS -- Use the graph classes
            profileDisplayPanel = new GraphCanvas();
            profileDisplayPanel.setGraphBounds(0, sampleNamesVector.size(), -3, 3);
            profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
            profileDisplayPanel.setXAxisValue(0);
            profileDisplayPanel.setYAxisValue(0);
            
            templateList.addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    if(templatesVector.size() > 0){
                        Vector temp = (Vector)templatesVector.elementAt(templateList.getSelectedIndex());
                        if(temp.size() == numberOfExperiments){
                            tempPanel.setVisible(true);
                            tempPanel.setEnabled(true);
                        } else {
                            tempPanel.setVisible(false);
                            tempPanel.setEnabled(false);
                        }
                    }
                    savedTemplateSelected = false;
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
            
            templateSplitPane.setPreferredSize(new Dimension(700,150));
            
            buildConstraints(constraints, 0, 0, 1, 1, 1, 1);
            
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.insets = new Insets(5,5,5,5);
            gridbag.setConstraints(templateSplitPane, constraints);
            this.add(templateSplitPane);

            loadButton = new JButton("Load saved template", GUIFactory.getIcon("Open24.gif"));
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            loadButton.setMargin(new Insets(10,10,10,10));
            loadButton.setFocusPainted(false);
            loadButton.setPreferredSize(new Dimension(200, 30));
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == loadButton) {
                        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                        fc.setCurrentDirectory(new File(fc.getCurrentDirectory(), "Data"));
                        
                        int returnVal = fc.showOpenDialog(SavedTemplateSelector.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            //     listModel.removeAllElements();
                            File file = fc.getSelectedFile();
                            try {
                                //  DataInputStream in = new DataInputStream(new FileInputStream(file.getPath()));
                                BufferedReader br = new BufferedReader(new java.io.FileReader(file.getPath()));
                                try {
                                    //*****READING OF INPUT DATA TAKES PLACE IN THIS BLOCK
                                    //  templatesVector = new Vector();
                                    int index = templatesVector.size();
                                    String value;
                                    Vector currentTemplate = new Vector();
                                    while ((value = br.readLine())!= null) {       
                                        currentTemplate.add(new Float((value)));
                                    }
                                    listModel.addElement(file.getName());
                                    templatesVector.add(currentTemplate);
                                    templateList.setSelectedIndex(templatesVector.size()-1);
                                    br.close();
                                    refreshGraph();
                                    profileDisplayPanel.setGraphBounds(0, currentTemplate.size(), -3, 3);
                                } catch (EOFException exc) {
                                    br.close();
                                }
                            } catch (IOException e){
                                System.out.println("Error: " + e.toString());
                                JOptionPane.showMessageDialog(null, e.toString());
                            }
                        }
                    }
                }
            });

            selectButton = new JButton("Select Displayed Template", GUIFactory.getIcon("select_check24.gif"));
            selectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            selectButton.setMargin(new Insets(10,10,10,10));
            selectButton.setFocusPainted(false);
            selectButton.setPreferredSize(new Dimension(200, 30));
            selectButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if (listModel.size() == 0){
                        JOptionPane.showMessageDialog(null, "No templates to select");
                    } else {
                        savedTemplateSelected = true;
                        int index = templateList.getSelectedIndex();
                        Vector expProfile = (Vector) ((Vector) templatesVector.get(index)).clone();
                        
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
                        if(expProfile.size() == numberOfExperiments){
                            clusterGenes = true;
                            for(int i  = 0; i < tempPanel.tempScr.tempGrid.length; i++){
                                tempPanel.tempScr.tempGrid[i].templateSlider.setValue((int) Math.floor(((Float)expProfile.get(i)).floatValue()*100));
                                tempPanel.tempScr.tempGrid[i].templateField.setText("" + ((Float)expProfile.get(i)).floatValue());
                            }
                        }
                        else{
                            clusterGenes = false;
                            template = expProfile;
                        }
                    }
                    refreshGraph();
                }
                
            });
            
            gridbag.setConstraints(selectButton, constraints);
            this.add(selectButton);
            buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
            templateButtonPanel = new JPanel();
            templateButtonPanel.add(loadButton);
            templateButtonPanel.add(selectButton);
            gridbag.setConstraints(templateButtonPanel, constraints);
            this.add(templateButtonPanel);
            refreshGraph();
            
        }
        
        private void refreshGraph() {
            float maxRange;
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            int index = templateList.getSelectedIndex();
            Color lineColor = Color.blue;
            if(savedTemplateSelected == true)
                lineColor = Color.green;
            
            if (index < 0) return;
            Vector expProfile = (Vector) ((Vector) templatesVector.get(index)).clone();
            
            max = Math.max(max, getMax(expProfile));
            min = Math.min(min, getMin(expProfile));
            maxRange = Math.max(max, Math.abs(min));
            profileDisplayPanel.setGraphBounds(0,expProfile.size(),-maxRange,maxRange);
            
            profileDisplayPanel.removeAllGraphElements();
            for (int i = 0; i < expProfile.size(); i++) {
                GraphPoint gp = new GraphPoint(i + 1, ((Float) expProfile.elementAt(i)).floatValue(), Color.red, 2);
                profileDisplayPanel.addGraphElement(gp);
            }
            
            for (int i = 0; i < expProfile.size() - 1; i++) {
                GraphLine gl = new GraphLine(i + 1, ((Float) expProfile.elementAt(i)).floatValue(),
                i + 2, ((Float) expProfile.elementAt(i + 1)).floatValue(), lineColor);
                profileDisplayPanel.addGraphElement(gl);
            }
            
            profileDisplayPanel.repaint();
        }
        
        public void reset(){
            savedTemplateSelected = false;
            refreshGraph();
        }
        
    }
    
    
    
    class ThresholdPanel extends JPanel {
        
        JLabel threshLabel1;
        JLabel threshLabel2;
        JTextField threshInputField;
        JRadioButton chooseR;
        JRadioButton chooseP;
        JCheckBox useAbsolute;
        JLabel inputLabel;
        
        ThresholdPanel() {
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(10, 30, 10, 0);
            this.setLayout(gridbag);
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Threshold Parameters"));
            
            
            useAbsolute = new JCheckBox("Use Absolute R");
            useAbsolute.setFocusPainted(false);
            buildConstraints(constraints, 0, 0, 3, 1, 0, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(useAbsolute, constraints);
            this.add(useAbsolute);
            
            constraints.insets = new Insets(0, 30, 0, 0);
            
            ButtonGroup chooseRorP = new ButtonGroup();
            chooseR = new JRadioButton("Use Threshold R",false);
            chooseR.setFocusPainted(false);
            chooseRorP.add(chooseR);
            chooseP = new JRadioButton("Use Threshold p-Value",true);
            chooseP.setFocusPainted(false);
            chooseRorP.add(chooseP);
            
            buildConstraints(constraints, 0, 1, 1, 1, 10, 0);
            //constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(chooseR, constraints);
            this.add(chooseR);
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 0);
            // constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(chooseP, constraints);
            this.add(chooseP);
            
            buildConstraints(constraints, 1, 0, 1, 1, 80, 0);
            threshLabel1 = new JLabel("Enter magnitude of threshold R or p-Value (between 0 and 1 inclusive)");
            buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
            threshLabel2 = new JLabel("at which selected profiles should be correlated to template");   
            constraints.insets = new Insets(15, 5, 0, 0);
            
            JPanel thresholdInputPanel = new JPanel(new GridBagLayout());
            
            //            buildConstraints(constraints, 1, 0, 1, 3, 10, 0);
            inputLabel = new JLabel("Enter p-value [0,1] :");
            inputLabel.setForeground(Color.black);
            inputLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
            inputLabel.setSize(150, 40);
            inputLabel.setMinimumSize( new Dimension(150,40));
            gridbag.setConstraints(inputLabel, constraints);
            thresholdInputPanel.add(inputLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            
            chooseR.addChangeListener(new ChangeListener(){
                
                public void stateChanged(ChangeEvent evt){
                    if(chooseR.isSelected())
                        inputLabel.setText("Enter R [0,1] :");
                    else
                        inputLabel.setText("Enter p-value [0,1] :");
                }
                
            });

            threshInputField = new JTextField("", 7);
            threshInputField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    
                    if(evt.getSource() == threshInputField){
                        try {
                            String s = threshInputField.getText();
                            double r = Double.parseDouble(s);
                            if ((r > 1)||(r < 0)) {
                                JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
                                threshInputField.selectAll();
                                threshInputField.requestFocus();
                            }
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Input Format Error: Threshold R value must be between 0 and 1 (inclusive)");
                            threshInputField.selectAll();
                            threshInputField.requestFocus();
                        }
                        
                    }
                    
                }
            });
            
            thresholdInputPanel.add(threshInputField, new GridBagConstraints(1,0,1,1,5,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,10,0,0),0,0));
            
            buildConstraints(constraints, 1, 0, 1, 3, 10, 0);
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.BOTH;
            
            gridbag.setConstraints(thresholdInputPanel, constraints);
            
            this.add(thresholdInputPanel);
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
            setMaximumSize(new Dimension(600,100));
            setSize(600,100);
        }
        
        public void reset(){
            tempScr.reset();
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
                scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                return scroller;
            }
            
            public void reset(){
                for(int i = 0; i < tempGrid.length; i++)
                    tempGrid[i].reset();
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
                                    JOptionPane.showMessageDialog(null, "Input Format Error: Template values must be between 0 and 1 (inclusive)");
                                    
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
                    
                    buildConstraints(constraints, 0, 2, 2, 1, 0, 85);
                    templateSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);  templateSlider.setPreferredSize(new Dimension(30,50));
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
        int paneIndex = tabbedPane.getSelectedIndex();
        
        //if a gene, gene cluster, or stored gene template
        if(paneIndex == 0 || paneIndex == 1 || ( paneIndex == 4 && tempPanel.isVisible() )){
            
            int numExp = this.expMatrix.getColumnDimension();
            
            for(int i = 0; i < numExp; i++) {
                String s = tempPanel.tempScr.tempGrid[i].templateField.getText();
                if (s.equals("NaN")||(s.equals("NULL"))||(!(tempPanel.tempScr.tempGrid[i].activeBox.isSelected()))) {
                    geneTemplate.add(new Float(Float.NaN));
                } else {
                    geneTemplate.add(new Float(s));
                }
            }
            return geneTemplate;
        }
        return template;
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
        boolean absoluteSelected = thresh.useAbsolute.isSelected();
        boolean drawTreesSelected = drawTreesBox.isSelected();
        boolean rSelected = thresh.chooseR.isSelected();
        String s = thresh.threshInputField.getText();
        double threshR = Double.parseDouble(s);
        
        Hashtable hash = new Hashtable();
        hash.put(new String("drawTrees"), new Boolean(drawTreesSelected)); //Should be a checkbox in the init dialog
        hash.put(new String("useAbsolute"), new Boolean(absoluteSelected));
        hash.put(new String("useR"), new Boolean(rSelected));
        hash.put(new String("template"), getTemplate());
        hash.put(new String("thresholdR"), new Double(threshR));
        
        // fireEvent(new ActionInfoEvent(this, hash));
        
    }
    
    public boolean isDrawTrees() {
        return drawTreesBox.isSelected();
    }
    
    public boolean isUseAbsolute() {
        return thresh.useAbsolute.isSelected();
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
            
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    String s = thresh.threshInputField.getText();
                    double r = Double.parseDouble(s);
                    if ((r > 1)||(r < 0)) {
                        JOptionPane.showMessageDialog(null, "Threshold R value must be between 0 and 1 (inclusive)");
                        thresh.threshInputField.selectAll();
                        thresh.threshInputField.requestFocus();
                    }
                    else {
                        okPressed = true;
                        fireOkButtonEvent();
                        setVisible(false);
                    }
                } catch (Exception exc) {
                    //  exc.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Input Format Error: Requires value between 0 and 1 (inclusive)");
                    thresh.threshInputField.selectAll();
                    thresh.threshInputField.requestFocus();
                }
            }
            else if (command.equals("reset-command")){
                template = new Vector();
                expSelectPanel.reset();
                clusterSelectPanel.reset();
                geneSelectPanel.reset();
                geneClusterSelectPanel.reset();
                tempPanel.reset();
                templateSelectPanel.reset();
                thresh.useAbsolute.setSelected(false);
                thresh.chooseP.setSelected(true);
                thresh.threshInputField.setText("0.01");
                okPressed = false;
            }
            else if (command.equals("cancel-command")){
                hide();
                dispose();
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(PTMInitDialog.this, "PTM Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
            else if (command.equals("save-template-command")){
                
            }
        }
    }
    
    
    /*********************************************************************
     *
     *   Gene related code
     *
     *
     */
    
    class GeneSelector extends JPanel{
        JList uniqueIDList;
        GraphCanvas profileDisplayPanel;
        JScrollPane listScrollPane;
        JButton selectButton;
        int maxIndex = -1;
        int minIndex = -1;
        
        
        JSplitPane uIDSplitPane;
        
        GeneSelector() {
            
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
            
            buildConstraints(constraints, 0, 0, 1, 1, 1, 1);
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTH;
            gridbag.setConstraints(uIDSplitPane, constraints);
            this.add(uIDSplitPane);
            
            buildConstraints(constraints, 0, 1, 1, 1, 1, 0);
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.insets = new Insets(10,10,10,10);
            selectButton = new JButton("Select highlighted gene from above list to use as template", GUIFactory.getIcon("select_check24.gif"));
            selectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            selectButton.setMargin(new Insets(10,10,10,10));
            selectButton.setPreferredSize(new Dimension(400,30));
            selectButton.setFocusPainted(false);
            
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
                    template = expProfile;
                    setTemplate = true;
                    clusterGenes = true;
                    okButton.setEnabled(true);
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
            float maxRange = Math.max(max, Math.abs(min));
            profileDisplayPanel.removeAllGraphElements();
            profileDisplayPanel.setGraphBounds(0, expProfile.size(), -maxRange, maxRange);
            
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
        
        public void reset(){
            uniqueIDList.setSelectedIndex(0);
            setTemplate = false;
            refreshGraph();
        }
    }
    
    private Vector getGene(int index) {
        Vector gene = new Vector();
        
        for (int i = 0; i < sampleNamesVector.size(); i++) {
            gene.add(new Float(expMatrix.get(index, i)));
        }
        
        return gene;
    }
    
    
    class GeneClusterSelector extends JPanel {
        JList clusterList;
        GraphCanvas profileDisplayPanel;
        JScrollPane listScrollPane;
        JButton selectButton;
        
        Vector clusterNames; // JUST THE NAMES OF THE CLUSTERS, i.e., "CLUSTER 1", "CLUSTER 2", ETC.
        //Vector clusterContents; // THIS IS A VECTOR OF VECTORS (= THE INDICES OF THE UNIQUE IDS IN A CLUSTER)
        Vector geneDataInClusters; // THIS IS A VECTOR OF VECTORS ( = CLUSTERS) OF VECTORS ( = EXPRESSION PROFILES OF GENES IN THAT CLUSTER).
        Vector averageClusterProfiles;
        
        JSplitPane clusterSplitPane;
        
        GeneClusterSelector() {
            
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
                    
                                        for(int i = 0; i < meanOfCurrentCluster.size(); i++){
                        System.out.println("mean = "+ ((Float)(meanOfCurrentCluster.elementAt(i))).floatValue());
                    }
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
            selectButton = new JButton("Select highlighted cluster from above list to use its mean as template", GUIFactory.getIcon("select_check24.gif"));
            selectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            selectButton.setMargin(new Insets(10,10,10,10));
            selectButton.setPreferredSize(new Dimension(430,30));
            selectButton.setFocusPainted(false);
            
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
                        if(setTemplate){
                            for (int i = 0; i < expProfile.size() - 1; i++) {
                                if ((Float.isNaN(((Float) expProfile.elementAt(i)).floatValue())) || (Float.isNaN(((Float) expProfile.elementAt(i+1)).floatValue()))) {
                                    continue;
                                }
                                GraphLine gl = new GraphLine(i /*+ 1*/, ((Float) expProfile.elementAt(i)).floatValue(),
                                i + 1, ((Float) expProfile.elementAt(i + 1)).floatValue(), Color.green);
                                profileDisplayPanel.addGraphElement(gl);
                            }
                        }
                        template = expProfile;
                        setTemplate = true;
                        clusterGenes = true;
                        okButton.setEnabled(true);
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
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            float maxRange;
            profileDisplayPanel.removeAllGraphElements();
            Vector selectedCluster = (Vector)geneDataInClusters.get(index);
            
            for (int j = 0; j < selectedCluster.size(); j++) {
                
                Vector currGene = (Vector)selectedCluster.get(j);
                
                max = Math.max(max, getMax(currGene));
                min = Math.min(min, getMin(currGene));
                maxRange = Math.max(max, Math.abs(min));
                profileDisplayPanel.setGraphBounds(0, currGene.size(), -maxRange, maxRange);
                
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
        
        public void reset(){
            clusterList.setSelectedIndex(0);
            setTemplate = false;
            refreshGraph();
        }
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
                            JOptionPane.showMessageDialog(null, "Input Format Error: Template values must be between 0 and 1 (inclusive)");
                            
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
    
    public static void main(String [] args){
        FloatMatrix expMatrix = new FloatMatrix(100,10);
        Vector uniqueIDs = new Vector();
        Vector sampleNamesVector = new Vector();
        Color [] colors = null;
        for(int i = 0; i < 100; i++)
            uniqueIDs.add(String.valueOf(i));
        for(int i = 0; i < 10; i++)
            sampleNamesVector.add(("Exp "+String.valueOf(i)));
        //             public (JFrame parentFrame, boolean modality, FloatMatrix expMatrix, Vector uniqueIDs, Vector sampleNamesVector, Vector clusters, Vector expClusters, Color[] clusterColors, Color [] expClusterColors) {
        
        PTMInitDialog box = new PTMInitDialog(new JFrame(), true, expMatrix, uniqueIDs, sampleNamesVector, new Vector(), new Vector(), colors, colors);
        box.setVisible(true);
        System.exit(0);
    }
    
}