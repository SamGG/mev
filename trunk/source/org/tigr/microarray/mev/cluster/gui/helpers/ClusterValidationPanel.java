package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ClusterValidationPanel extends JPanel{

	private JCheckBox useValidationBox;
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
	public JTextField getLowClusterRange() {
		return lowClusterRange;
	}
	public void setLowClusterRange(JTextField lowClusterRange) {
		this.lowClusterRange = lowClusterRange;
	}
	public JTextField getHighClusterRange() {
		return highClusterRange;
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
	public ClusterValidationPanel(String title){
		super();
		this.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
        this.setLayout(new GridBagLayout());
        
        useValidationBox = new JCheckBox("Use Validation (Requires MeV+R)");
        internalValidationBox = new JCheckBox("Internal Validation");
        stabilityValidationBox = new JCheckBox("Stability Validation");
        biologicalValidationBox = new JCheckBox("Biological Validation");
        clusterRange = new JLabel("Cluster Range: ");
        lowClusterRange = new JTextField("2",7);
        dashLabel = new JLabel(" - ");
        highClusterRange = new JTextField("6",7);
        hierarchicalMethodBox = new JCheckBox("HCL");
        hierarchicalMethodBox.addActionListener(new LinkageListener());
        kmeansMethodBox = new JCheckBox("K-Means");
        dianaMethodBox = new JCheckBox("DIANA");
        fannyMethodBox = new JCheckBox("FANNY");
        somMethodBox = new JCheckBox("SOM");
        modelMethodBox = new JCheckBox("MODEL");
        sotaMethodBox = new JCheckBox("SOTA");
        pamMethodBox = new JCheckBox("PAM");
        claraMethodBox = new JCheckBox("CLARA");
        agnesMethodBox = new JCheckBox("AGNES");
        agnesMethodBox.addActionListener(new LinkageListener());
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
        linkageMethodPanel = new JPanel();
        linkageMethodPanel.add(new JLabel("Linkage Metric: "));
        linkageMethodPanel.add(linkageMetricCB);
        
        boolean startVis = false;
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
        
        useValidationBox.setBackground(Color.white);
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
        
        useValidationBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				boolean vis = useValidationBox.isSelected();
				internalValidationBox.setVisible(vis);
		        stabilityValidationBox.setVisible(vis);
		        biologicalValidationBox.setVisible(vis);
		        clusterRange.setVisible(vis);
		        lowClusterRange.setVisible(vis);
		        dashLabel.setVisible(vis);
		        highClusterRange.setVisible(vis);
		        methodPanel.setVisible(vis);
		        distanceMetricPanel.setVisible(vis);
		        validate();
		        updateUI();
			}        	
        });

        this.add(useValidationBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(internalValidationBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(stabilityValidationBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(biologicalValidationBox, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(clusterRange, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(lowClusterRange, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(dashLabel, new GridBagConstraints(3,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(highClusterRange, new GridBagConstraints(4,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(methodPanel, new GridBagConstraints(0,4,6,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(distanceMetricPanel, new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        this.add(linkageMethodPanel, new GridBagConstraints(0,6,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
		
	}
	public static void main(String[] args){
		JDialog jd = new JDialog();
		jd.add(new ClusterValidationPanel("asdasda"));
		jd.setVisible(true);
	}
	private class LinkageListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			linkageMethodPanel.setVisible(hierarchicalMethodBox.isSelected()||agnesMethodBox.isSelected());
		}
		
	}
}
