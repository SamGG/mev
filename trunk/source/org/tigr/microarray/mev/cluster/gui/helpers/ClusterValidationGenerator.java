package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;


public class ClusterValidationGenerator{

	private static final long serialVersionUID = 1L;

	public boolean isValidate() {
		return useValidationBox.isSelected();
	}
	public boolean isInternalV() {
		return internalValidationBox.isSelected();
	}
	public boolean isStabilityV() {
		return stabilityValidationBox.isSelected();
	}
	public boolean isBiologicalV() {
		return biologicalValidationBox.isSelected();
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
	public int getLowClusterRange() {
		return Integer.parseInt(lowClusterRange.getText());
	}
	public int getHighClusterRange() {
		return Integer.parseInt(highClusterRange.getText());
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
	private JCheckBox useValidationBox;
	private JCheckBox internalValidationBox;
	private JCheckBox stabilityValidationBox;
	private JCheckBox biologicalValidationBox;
	private JTextField lowClusterRange;
	private JTextField highClusterRange;
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
	private JRadioButton clusterGenesCheckBox;
	private JRadioButton clusterSamplesCheckBox;
	private JCheckBox clusterClusterBox;
	private JComboBox clusterComboBox;
	private ClusterRepository repository;
	private JPanel validationTypePanel;
	private JPanel clusterRangePanel;
	private JPanel mainValidationPanel;
	
	/** 
	 * Constructor for use in other clustering modules
	 */
	public ClusterValidationGenerator(AlgorithmDialog parent, ClusterRepository repository, String title){
		this(parent, title, repository, false);
	}
	/**
	 * Constructor for use in standalone module
	 * @param parent
	 * @param title
	 * @param standaloneModule
	 */
	public ClusterValidationGenerator(AlgorithmDialog parent, String title, ClusterRepository repository, boolean standaloneModule) {
		super();
		mainValidationPanel = new JPanel();
		this.parentDialog = parent;
		this.repository = repository;
		listener = new Listener();   
		mainValidationPanel.addAncestorListener(new AncestorListener(){
			public void ancestorAdded(AncestorEvent event) {
				try{
					parentDialog.pack();	
				}catch (Exception e){
					
				}
			}      
			public void ancestorMoved(AncestorEvent event) {
				try{
					parentDialog.pack();	
				}catch (Exception e){
					
				}
			}     
			public void ancestorRemoved(AncestorEvent event) {
				try{
					parentDialog.pack();	
				}catch (Exception e){
					
				}
			}        	
        });
		mainValidationPanel.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        mainValidationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
        mainValidationPanel.setLayout(new GridBagLayout());
        
        useValidationBox = new JCheckBox("Use Validation (Requires MeV+R)");
        useValidationBox.setSelected(standaloneModule);
        clusterGenesCheckBox = new JRadioButton("Cluster Genes");
        clusterSamplesCheckBox = new JRadioButton("Cluster Samples");
        ButtonGroup bg = new ButtonGroup();
        bg.add(clusterGenesCheckBox);
        bg.add(clusterSamplesCheckBox);
        clusterGenesCheckBox.setSelected(true);
        clusterClusterBox = new JCheckBox("Perform Analysis on MeV Cluster");
        clusterComboBox = new JComboBox();
        internalValidationBox = new JCheckBox("Internal Validation");
        stabilityValidationBox = new JCheckBox("Stability Validation");
        biologicalValidationBox = new JCheckBox("Biological Validation");
        validationTypePanel = new JPanel();
        validationTypePanel.add(internalValidationBox);
        validationTypePanel.add(stabilityValidationBox);
        validationTypePanel.add(biologicalValidationBox);
        validationTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Validation Type(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
        JLabel clusterRange = new JLabel("Number of Clusters: ");
        lowClusterRange = new JTextField("2",7);
        JLabel dashLabel = new JLabel(" - ");
        highClusterRange = new JTextField("6",7);
        clusterRangePanel = new JPanel();
        clusterRangePanel.add(clusterRange);
        clusterRangePanel.add(lowClusterRange);
        clusterRangePanel.add(dashLabel);
        clusterRangePanel.add(highClusterRange);
        clusterRangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Cluster Range", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
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
        methodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Clustering Method(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
        
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
        if (repository==null||repository.isEmpty()){
        	clusterComboBox.addItem("No stored clusters");
        	clusterClusterBox.setEnabled(false);
        } else {
	    	for (int i=0; i<repository.size(); i++){
				if (repository.getCluster(i+1)==null)
					break;
				Cluster cluster = repository.getCluster(i+1);
				clusterComboBox.addItem("Cluster #: "+cluster.getSerialNumber()+", "+cluster.getClusterLabel());
	    	}
        }
        
        boolean startVis = false;
        clusterGenesCheckBox.setVisible(startVis);
        clusterSamplesCheckBox.setVisible(startVis);
        clusterClusterBox.setVisible(startVis);
        clusterComboBox.setVisible(startVis);
        internalValidationBox.setVisible(startVis);
        stabilityValidationBox.setVisible(startVis);
        biologicalValidationBox.setVisible(startVis);
        validationTypePanel.setVisible(startVis);
        clusterRangePanel.setVisible(startVis);
        methodPanel.setVisible(startVis);
        distanceMetricPanel.setVisible(startVis);
        linkageMethodPanel.setVisible(startVis);
        annotationTypePanel.setVisible(startVis);
        annotationPanel.setVisible(startVis);
        
        useValidationBox.setBackground(Color.white);
        clusterGenesCheckBox.setBackground(Color.white);
        clusterSamplesCheckBox.setBackground(Color.white);
        clusterClusterBox.setBackground(Color.white);
        clusterComboBox.setBackground(Color.white);
        internalValidationBox.setBackground(Color.white);
        stabilityValidationBox.setBackground(Color.white);
        biologicalValidationBox.setBackground(Color.white);
        validationTypePanel.setBackground(Color.white);
        clusterRangePanel.setBackground(Color.white);
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
        clusterClusterBox.addActionListener(listener);
        clusterGenesCheckBox.addActionListener(listener);
        clusterSamplesCheckBox.addActionListener(listener);
        
        int y=0;
        if (!standaloneModule)
        	mainValidationPanel.add(useValidationBox, new GridBagConstraints(0,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterGenesCheckBox, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterSamplesCheckBox, new GridBagConstraints(1,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterClusterBox, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterComboBox, new GridBagConstraints(1,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterRangePanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(validationTypePanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(annotationTypePanel, new GridBagConstraints(0,++y,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(annotationPanel, new GridBagConstraints(0,++y,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(methodPanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(distanceMetricPanel, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(linkageMethodPanel, new GridBagConstraints(1,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
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
		ClusterValidationGenerator cvg = new ClusterValidationGenerator(null,null,"asdasda");
		jf.add(cvg.getClusterValidationPanel());
		jf.setSize(800, 600);
		jf.setVisible(true);
	}
	public JPanel getClusterValidationPanel() {
		return mainValidationPanel;
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
		clusterClusterBox.setVisible(vis&&clusterGenesCheckBox.isSelected());
		internalValidationBox.setVisible(vis);
        stabilityValidationBox.setVisible(vis);
        biologicalValidationBox.setVisible(vis);
        clusterRangePanel.setVisible(vis);
        methodPanel.setVisible(vis);
        validationTypePanel.setVisible(vis);
        distanceMetricPanel.setVisible(vis);
		clusterComboBox.setVisible(vis&&clusterClusterBox.isSelected()&&clusterGenesCheckBox.isSelected()&&repository!=null);            	
        annotationTypePanel.setVisible(vis&&biologicalValidationBox.isSelected());
        annotationPanel.setVisible(vis&&biologicalValidationBox.isSelected()&&bioCAnnotationRB.isSelected());
		linkageMethodPanel.setVisible(vis&&hierarchicalMethodBox.isSelected()||agnesMethodBox.isSelected());
		mainValidationPanel.validate();
		mainValidationPanel.updateUI();		
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
	/**
	 * 
	 * @return true if parameters are sufficient
	 */
	public boolean validateParameters(){
		boolean pass = true;
		if (!this.useValidationBox.isSelected())
			return true;
		if(getMethodsArray().length==0){
			methodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Clustering Method(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
			pass = false;
		}
		if(getMeasuresArray().length==0){
			validationTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Validation Type(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
        	pass = false;
		}
		if (!pass)
            JOptionPane.showMessageDialog(null, "Validation parameters are insufficient.", "Error", JOptionPane.ERROR_MESSAGE);
		return pass;
	}
	public void addValidationParameters(AlgorithmData validationData) {
		validationData.addStringArray("methodsArray", getMethodsArray());
		validationData.addIntArray("subCluster", getSubCluster());
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
	private int[] getSubCluster() {
		if (!this.clusterClusterBox.isSelected())
			return null;
		try{
			return repository.getCluster(clusterComboBox.getSelectedIndex()+1).getIndices();
		} catch (Exception e){		
			return null;
		}
	}
	public String getClusterID() {
		if (!this.clusterClusterBox.isSelected())
			return "All genes";
		else
			return (String) clusterComboBox.getSelectedItem();
	}
}
