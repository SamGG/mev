package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.clvalid.CLVALIDInitBox;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;


public class ClusterValidationPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private JCheckBox useValidationBox;

	public boolean isValidate() {
		return getUseValidationBox().isSelected();
	}
	public boolean isInternalV() {
		return getInternalValidationBox().isSelected();
	}
	public boolean isStabilityV() {
		return getStabilityValidationBox().isSelected();
	}
	public boolean isBiologicalV() {
		return getBiologicalValidationBox().isSelected();
	}
	public String getValidationLinkageMethod() {
		return getLinkageMethod();
	}
	public boolean isClusterGenes(){
		return clusterGenesCheckBox.isSelected();
	}
	public boolean isClusterSamples(){
		return clusterSamplesCheckBox.isSelected();
	}
    	
	public JCheckBox getUseValidationBox() {
		return useValidationBox;
	}
	public void setUseValidationBox(JCheckBox useValidationBox) {
		this.useValidationBox = useValidationBox;
	}
	public JCheckBox getInternalValidationBox() {
		return internalValidationBox;
	}
	public void setInternalValidationBox(JCheckBox internalValidationBox) {
		this.internalValidationBox = internalValidationBox;
	}
	public JCheckBox getStabilityValidationBox() {
		return stabilityValidationBox;
	}
	public void setStabilityValidationBox(JCheckBox stabilityValidationBox) {
		this.stabilityValidationBox = stabilityValidationBox;
	}
	public JCheckBox getBiologicalValidationBox() {
		return biologicalValidationBox;
	}
	public void setBiologicalValidationBox(JCheckBox biologicalValidationBox) {
		this.biologicalValidationBox = biologicalValidationBox;
	}
	public int getLowClusterRange() {
		return Integer.parseInt(lowClusterRange.getText());
	}
	public JPanel getLinkageMethodPanel() {
		return this.linkageMethodPanel;
	}
	public JPanel getAnnotationTypePanel() {
		return this.annotationTypePanel;
	}
	public void setLowClusterRange(JTextField lowClusterRange) {
		this.lowClusterRange = lowClusterRange;
	}
	public int getHighClusterRange() {
		return Integer.parseInt(highClusterRange.getText());
	}
	public void setHighClusterRange(JTextField highClusterRange) {
		this.highClusterRange = highClusterRange;
	}
	public String[] getMethodsArray(){
		String[] ret;
		ArrayList<String> al = new ArrayList<String>();
		if (hierarchicalMethodBox.isSelected())
			al.add("hierarchical");
		if (kmeansMethodBox.isSelected())
			al.add("kmeans");
		if (dianaMethodBox.isSelected())
			al.add("diana");
		if (fannyMethodBox.isSelected())
			al.add("fanny");
		if (somMethodBox.isSelected())
			al.add("som");
		if (modelMethodBox.isSelected())
			al.add("model");
		if (sotaMethodBox.isSelected())
			al.add("sota");
		if (pamMethodBox.isSelected())
			al.add("pam");
		if (claraMethodBox.isSelected())
			al.add("clara");
		if (agnesMethodBox.isSelected())
			al.add("agnes");
		ret = new String[al.size()];
		for (int i=0; i<ret.length; i++){
			ret[i] = al.get(i);
		}
		return ret;
	}

	private String[] getMeasuresArray() {
		String[] ret;
		ArrayList<String> al = new ArrayList<String>();
		if (internalValidationBox.isSelected())
			al.add("internal");
		if (stabilityValidationBox.isSelected())
			al.add("stability");
		if (biologicalValidationBox.isSelected())
			al.add("biological");
		ret = new String[al.size()];
		for (int i=0; i<ret.length; i++){
			ret[i] = al.get(i);
		}
		return ret;
	}
	private JCheckBox internalValidationBox;
	private JCheckBox stabilityValidationBox;
	private JCheckBox biologicalValidationBox;
	private JTextField lowClusterRange;
	private JTextField highClusterRange;
	private JLabel clusterRange;
	private JLabel dashLabel;
	private JComboBox clusterMethod;
	private JTextField maxItems;
	private JCheckBox hierarchicalMethodBox;
	private JCheckBox kmeansMethodBox;
	private JCheckBox dianaMethodBox;
	private JCheckBox fannyMethodBox;
	private JCheckBox somMethodBox;
	private JCheckBox modelMethodBox;
	private JCheckBox sotaMethodBox;
	private JCheckBox pamMethodBox;
	private JCheckBox claraMethodBox;
	private JCheckBox agnesMethodBox;
	private JPanel methodPanel;
	private JPanel linkageMethodPanel;
	private JComboBox distanceMetricCB;
	private JPanel distanceMetricPanel;
	private JComboBox linkageMetricCB;
	private JRadioButton bioCAnnotationRB;
	private JRadioButton localAnnotationRB;
	private JPanel annotationTypePanel;
	private Listener listener;
	private JPanel annotationPanel;
	private JComboBox chipNameBox;
	private AlgorithmDialog parentDialog;
	private boolean standaloneModule;
	private JRadioButton clusterGenesCheckBox;
	private JRadioButton clusterSamplesCheckBox;
	
	/** 
	 * Constructor for use in other clustering modules
	 */
	public ClusterValidationPanel(AlgorithmDialog parent, String title){
		this(parent, title, false);
	}
	/**
	 * Constructor for use in standalone module
	 * @param parent
	 * @param title
	 * @param standaloneModule
	 */
	public ClusterValidationPanel(AlgorithmDialog parent, String title, boolean standaloneModule) {
		super();
		this.standaloneModule = standaloneModule;
		this.parentDialog = parent;
		listener = new Listener();   
		this.addAncestorListener(new AncestorListener(){
			public void ancestorAdded(AncestorEvent event) {
				parentDialog.pack();	
			}
			public void ancestorMoved(AncestorEvent event) {
				parentDialog.pack();	
			}
			public void ancestorRemoved(AncestorEvent event) {
				parentDialog.pack();	
			}        	
        });
		this.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
        this.setLayout(new GridBagLayout());
        
        useValidationBox = new JCheckBox("Use Validation (Requires MeV+R)");
        useValidationBox.setSelected(standaloneModule);
        clusterGenesCheckBox = new JRadioButton("Cluster Genes");
        clusterSamplesCheckBox = new JRadioButton("Cluster Samples");
        ButtonGroup bg = new ButtonGroup();
        bg.add(clusterGenesCheckBox);
        bg.add(clusterSamplesCheckBox);
        clusterGenesCheckBox.setSelected(true);
        internalValidationBox = new JCheckBox("Internal Validation");
        stabilityValidationBox = new JCheckBox("Stability Validation");
        biologicalValidationBox = new JCheckBox("Biological Validation");
        clusterRange = new JLabel("Cluster Range: ");
        lowClusterRange = new JTextField("2",7);
        dashLabel = new JLabel(" - ");
        highClusterRange = new JTextField("6",7);
        hierarchicalMethodBox = new JCheckBox("HCL");
        kmeansMethodBox = new JCheckBox("K-Means");
        dianaMethodBox = new JCheckBox("DIANA");
        fannyMethodBox = new JCheckBox("FANNY");
        somMethodBox = new JCheckBox("SOM");
        modelMethodBox = new JCheckBox("MODEL");
        sotaMethodBox = new JCheckBox("SOTA");
        pamMethodBox = new JCheckBox("PAM");
        claraMethodBox = new JCheckBox("CLARA");
        agnesMethodBox = new JCheckBox("AGNES");
        maxItems = new JTextField("",7);
        methodPanel = new JPanel();
        methodPanel.add(hierarchicalMethodBox);
        methodPanel.add(kmeansMethodBox);
        methodPanel.add(dianaMethodBox);
        methodPanel.add(fannyMethodBox);
        methodPanel.add(somMethodBox);
        methodPanel.add(modelMethodBox);
        methodPanel.add(sotaMethodBox);
        methodPanel.add(pamMethodBox);
        methodPanel.add(claraMethodBox);
        methodPanel.add(agnesMethodBox);
        distanceMetricCB = new JComboBox(new String[]{"euclidean", "correlation","manhattan"});
        distanceMetricPanel = new JPanel();
        distanceMetricPanel.add(new JLabel("Distance Metric: "));
        distanceMetricPanel.add(distanceMetricCB);
        linkageMetricCB = new JComboBox(new String[]{"ward", "single", "complete","average"});
        linkageMetricCB.setSelectedItem("average");
        linkageMethodPanel = new JPanel();
        linkageMethodPanel.add(new JLabel("Linkage Metric: "));
        linkageMethodPanel.add(linkageMetricCB);
        annotationTypePanel = new JPanel();
        bioCAnnotationRB = new JRadioButton("Bioconductor Annotation");
        bioCAnnotationRB.setSelected(true);
        localAnnotationRB = new JRadioButton("Local Annotation");
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(bioCAnnotationRB);
        bg1.add(localAnnotationRB);
        annotationTypePanel.add(bioCAnnotationRB);
        annotationTypePanel.add(localAnnotationRB);

        chipNameBox = new JComboBox(getBioCAnnotationsArray());
        chipNameBox.setSelectedItem("hgu133plus2.db");
        annotationPanel = new JPanel();
        annotationPanel.add(new JLabel("Chip Name: "));
        annotationPanel.add(chipNameBox);
        
        boolean startVis = false;
        clusterGenesCheckBox.setVisible(startVis);
        clusterSamplesCheckBox.setVisible(startVis);
        internalValidationBox.setVisible(startVis);
        stabilityValidationBox.setVisible(startVis);
        biologicalValidationBox.setVisible(startVis);
        clusterRange.setVisible(startVis);
        lowClusterRange.setVisible(startVis);
        dashLabel.setVisible(startVis);
        highClusterRange.setVisible(startVis);
        methodPanel.setVisible(startVis);
        distanceMetricPanel.setVisible(startVis);
        linkageMethodPanel.setVisible(startVis);
        annotationTypePanel.setVisible(startVis);
        annotationPanel.setVisible(startVis);
        
        useValidationBox.setBackground(Color.white);
        clusterGenesCheckBox.setBackground(Color.white);
        clusterSamplesCheckBox.setBackground(Color.white);
        internalValidationBox.setBackground(Color.white);
        stabilityValidationBox.setBackground(Color.white);
        biologicalValidationBox.setBackground(Color.white);
        clusterRange.setBackground(Color.white);
        lowClusterRange.setBackground(Color.white);
        dashLabel.setBackground(Color.white);
        highClusterRange.setBackground(Color.white);
        methodPanel.setBackground(Color.white);
        distanceMetricPanel.setBackground(Color.white);
        linkageMethodPanel.setBackground(Color.white);
        annotationTypePanel.setBackground(Color.white);
        bioCAnnotationRB.setBackground(Color.white);
        localAnnotationRB.setBackground(Color.white);
        annotationPanel.setBackground(Color.white);
        
        useValidationBox.addActionListener(listener);
        agnesMethodBox.addActionListener(listener);
        hierarchicalMethodBox.addActionListener(listener);
        bioCAnnotationRB.addActionListener(listener);
        localAnnotationRB.addActionListener(listener);
        biologicalValidationBox.addActionListener(listener);
        
        if (!standaloneModule)
        	this.add(useValidationBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(clusterGenesCheckBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(clusterSamplesCheckBox, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(internalValidationBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(stabilityValidationBox, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(biologicalValidationBox, new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(annotationTypePanel, new GridBagConstraints(0,5,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        this.add(annotationPanel, new GridBagConstraints(0,6,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        this.add(methodPanel, new GridBagConstraints(0,7,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(distanceMetricPanel, new GridBagConstraints(0,8,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(linkageMethodPanel, new GridBagConstraints(0,9,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(clusterRange, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(lowClusterRange, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(dashLabel, new GridBagConstraints(3,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(highClusterRange, new GridBagConstraints(4,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        resetVisibleComponents();
		
	}
	private String[] getBioCAnnotationsArray() {
		ArrayList<String> bioCAnnotations = new ArrayList<String>();
	    try {
			String urlString = "ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support_devel/R2.11/win/attract/annotationSupported.txt";
			String fullURL = urlString;
			URL url = new URL(fullURL); // Interpret, connect to URL
	
			URLConnection url_conn = url.openConnection();
			url_conn.setDoInput(true);
			url_conn.setUseCaches(true);
	
			BufferedReader inp = new BufferedReader( // Setup buffered input stream
					new InputStreamReader(url_conn.getInputStream()));
			String s = inp.readLine();
			while (s != null) {						
				bioCAnnotations.add(s);
				s = inp.readLine();
			}
		} catch (Exception e){
			System.out.println("Error reading supported Bioconductor annotations");
		}
		String[] bioCAnnotationsArray = new String[bioCAnnotations.size()];
		for (int i=0; i<bioCAnnotationsArray.length; i++){
			bioCAnnotationsArray[i]=bioCAnnotations.get(i);
		}
		return bioCAnnotationsArray;
	}
	public static void main(String[] args){
		JFrame jf = new JFrame();
		jf.add(new ClusterValidationPanel(null,"asdasda"));
		jf.setSize(800, 600);
		jf.setVisible(true);
	}
	private class Listener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			resetVisibleComponents();
		}		
	}
	private void resetVisibleComponents(){
		boolean vis = useValidationBox.isSelected();
		clusterGenesCheckBox.setVisible(vis);
		clusterSamplesCheckBox.setVisible(vis);
		internalValidationBox.setVisible(vis);
        stabilityValidationBox.setVisible(vis);
        biologicalValidationBox.setVisible(vis);
        clusterRange.setVisible(vis);
        lowClusterRange.setVisible(vis);
        dashLabel.setVisible(vis);
        highClusterRange.setVisible(vis);
        methodPanel.setVisible(vis);
        distanceMetricPanel.setVisible(vis);
        annotationTypePanel.setVisible(vis&&biologicalValidationBox.isSelected());
        annotationPanel.setVisible(vis&&biologicalValidationBox.isSelected()&&bioCAnnotationRB.isSelected());
		linkageMethodPanel.setVisible(vis&&hierarchicalMethodBox.isSelected()||agnesMethodBox.isSelected());
        validate();
        updateUI();
		
	}
	public String getLinkageMethod() {
		return this.linkageMetricCB.getSelectedItem().toString();
	}
	public String getValidationDistanceMetric() {
		return this.distanceMetricCB.getSelectedItem().toString();
	}
	public String getBioCAnnotationString() {
		return this.chipNameBox.getSelectedItem().toString();
	}
	public boolean isValidationPanelValid(){
		if (!this.useValidationBox.isSelected())
			return true;
		if(getMethodsArray().length==0||getMeasuresArray().length==0)
			return false;
		return true;
	}
	public void addValidationParameters(AlgorithmData validationData) {
		validationData.addStringArray("methodsArray", getMethodsArray());
        validationData.addParam("validate", String.valueOf(isValidate()));
        validationData.addParam("cluster-genes", String.valueOf(isClusterGenes()));
        validationData.addParam("cluster-samples", String.valueOf(isClusterSamples()));
        validationData.addParam("internal-validation", String.valueOf(isInternalV()));
        validationData.addParam("stability-validation", String.valueOf(isStabilityV()));
        validationData.addParam("biological-validation", String.valueOf(isBiologicalV()));
        validationData.addParam("cluster-range-low", String.valueOf(getLowClusterRange()));
        validationData.addParam("cluster-range-high", String.valueOf(getHighClusterRange()));
        validationData.addParam("validation-linkage", String.valueOf(getValidationLinkageMethod()));
        validationData.addParam("validation-distance", String.valueOf(getValidationDistanceMetric()));
        validationData.addParam("bioC-annotation", String.valueOf(getBioCAnnotationString()));		
	}
}
