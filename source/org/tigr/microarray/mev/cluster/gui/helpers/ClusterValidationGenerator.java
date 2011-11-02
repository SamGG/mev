package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;
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

	private JCheckBox useValidationBox,internalValidationBox,stabilityValidationBox,biologicalValidationBox,hierarchicalMethodBox,
		kmeansMethodBox,dianaMethodBox,fannyMethodBox,somMethodBox,modelMethodBox,sotaMethodBox,pamMethodBox,claraMethodBox,
		agnesMethodBox,clusterClusterBox;
	private JTextField lowClusterRange,highClusterRange;
	private JPanel methodPanel,linkageMethodPanel,distanceMetricPanel,annotationTypePanel,validationTypePanel,clusterRangePanel,mainValidationPanel,annotationPanel;
	private JComboBox distanceMetricCB,linkageMetricCB,chipNameBox;
	private JRadioButton bioCAnnotationRB,localAnnotationRB,clusterGenesCheckBox,clusterSamplesCheckBox;
	private Listener listener;
	private AlgorithmDialog parentDialog;
	private ClusterRepository repository;
	private boolean standaloneModule;
	private String bioCAnnotation, title;
	private ClusterBrowser clusterBrowser;
	private JScrollPane clusterBrowerScrollPane;
	
	/** 
	 * Constructor for use in other clustering modules
	 */
	public ClusterValidationGenerator(AlgorithmDialog parent, ClusterRepository repository, String bioCAnnotation, String title){
		this(parent, title, repository, bioCAnnotation, false);
	}	
	
	/**
	 * Constructor for use in standalone module
	 * @param parent
	 * @param title
	 * @param standaloneModule
	 */
	public ClusterValidationGenerator(AlgorithmDialog parent, String title, ClusterRepository repository, String bioCAnnotation, boolean standaloneModule) {
		super();
		mainValidationPanel = new JPanel();
		this.parentDialog = parent;
		this.repository = repository;
		this.standaloneModule = standaloneModule;
		this.bioCAnnotation = bioCAnnotation;
		this.title = title;
		createGUIComponents();        
        resetVisibleComponents();
        addListeners();
        addGUIComponents();
        setChildrenBackground(this.mainValidationPanel);
	}

	/**
	 * Recursive- sets all elements to be white
	 * @param comp
	 */
	private void setChildrenBackground(Component comp){
		Component[] components = ((Container)comp).getComponents();
	    for (int i = 0; i < components.length; i++){
	        setChildrenBackground(components[i]);
	    	components[i].setBackground(Color.white);
	    } 
		
	}
	private void addGUIComponents() {
        int y=0;
        if (!standaloneModule)
        	mainValidationPanel.add(useValidationBox, new GridBagConstraints(0,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterGenesCheckBox, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterSamplesCheckBox, new GridBagConstraints(1,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterClusterBox, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterBrowerScrollPane, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(clusterRangePanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(validationTypePanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(annotationTypePanel, new GridBagConstraints(0,++y,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(annotationPanel, new GridBagConstraints(0,++y,5,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(methodPanel, new GridBagConstraints(0,++y,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(distanceMetricPanel, new GridBagConstraints(0,++y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        mainValidationPanel.add(linkageMethodPanel, new GridBagConstraints(1,y,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        resetVisibleComponents();		
	}
	private void createGUIComponents() {		
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
	    clusterBrowser = new ClusterBrowser(repository);
	    clusterBrowerScrollPane = new JScrollPane(clusterBrowser);
	    clusterBrowerScrollPane.setMaximumSize(new Dimension(clusterBrowerScrollPane.getWidth(),240));
	    clusterBrowerScrollPane.setPreferredSize(new Dimension(clusterBrowerScrollPane.getWidth(),240));
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
	    chipNameBox = generateChipNameBox(bioCAnnotation);
	    annotationPanel = new JPanel();
	    annotationPanel.add(new JLabel("Chip Name: "));
	    annotationPanel.add(chipNameBox);
	    if (repository==null||repository.isEmpty()){
	    	clusterClusterBox.setEnabled(false);
	    } else {
	    }		
	}
	private void addListeners() {
        useValidationBox.addActionListener(listener);
        agnesMethodBox.addActionListener(listener);
        hierarchicalMethodBox.addActionListener(listener);
        bioCAnnotationRB.addActionListener(listener);
        localAnnotationRB.addActionListener(listener);
        biologicalValidationBox.addActionListener(listener);
        clusterClusterBox.addActionListener(listener);
        clusterGenesCheckBox.addActionListener(listener);
        clusterSamplesCheckBox.addActionListener(listener);		
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
	private JComboBox generateChipNameBox(String bioCAnnotation) {
		JComboBox chipNameBox = new JComboBox(getBioCAnnotationsArray());
        if (bioCAnnotation!=null){
	        for (int i=0; i<chipNameBox.getItemCount(); i++){
				if(chipNameBox.getItemAt(i).toString().equals(bioCAnnotation)||chipNameBox.getItemAt(i).toString().equals(bioCAnnotation+".db")){
					chipNameBox.setSelectedIndex(i);
				}
			}
        }
		return chipNameBox;
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
		ClusterValidationGenerator cvg = new ClusterValidationGenerator(null,null,"hgu133a","asdasda");
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
        clusterBrowerScrollPane.setVisible(vis&&clusterClusterBox.isSelected()&&clusterGenesCheckBox.isSelected()&&repository!=null);          	
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
		String clusteringMethodMessage = "";
		String validTypeMessage = "";
		String numberFormatMessage = "";
		if (!this.useValidationBox.isSelected())
			return true;
		if(getMethodsArray().length==0){
			methodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Clustering Method(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
			clusteringMethodMessage = "*Please select at least one clustering method.";
			pass = false;
		}
		if(getMeasuresArray().length==0){
			validationTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Validation Type(s)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
			validTypeMessage = "*Please select at least one validation type.";
			pass = false;
		}
		try{
			if(		Integer.parseInt(this.lowClusterRange.getText())<2||
					Integer.parseInt(this.highClusterRange.getText())>6||
					Integer.parseInt(this.highClusterRange.getText()) - Integer.parseInt(this.lowClusterRange.getText())<0){
				numberFormatMessage = "*Please enter a cluster range between 2 and 6, inclusive.";	
				pass = false;		
				clusterRangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Cluster Range", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
	
			}
		}catch(Exception e){
			clusterRangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Cluster Range", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.red));        
			pass = false;
			numberFormatMessage = "*Please enter a cluster range using integers only.";
		}
		if (!pass)
            JOptionPane.showMessageDialog(null, "Validation parameters are insufficient.\n"+validTypeMessage+"\n"+clusteringMethodMessage+"\n"+numberFormatMessage, "Error", JOptionPane.ERROR_MESSAGE);
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
			return clusterBrowser.getSelectedCluster().getIndices();
		} catch (Exception e){		
			return null;
		}
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
	public String getClusterID() {
		if (!this.clusterClusterBox.isSelected())
			return "All genes";
		else
			return clusterBrowser.getSelectedCluster().getClusterLabel();
	}
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
}
