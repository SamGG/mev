 package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * @author braisted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NonparFisherPanel extends JPanel implements
IWizardParameterPanel {
	
	private AlgorithmData algData;
	private String [] binNames;
	private String [] groupNames;
	
	private JTextField cutoffField;
	private JRadioButton binOneButton;
	private JRadioButton binTwoButton;
	
	private JRadioButton pValueButton;
	private JRadioButton fdrButton;
	private JCheckBox fdrGraphBox;
	
	private JLabel alphaLabel;
	private JTextField alphaField;
	private JLabel fdrLimitLabel;
	private JTextField fdrField;	
	private JCheckBox hclBox;

	private int c1Index;
	private int r1Index;
	
	private JDialog parent;
	
	private String FISHER_TITLE = "Fisher Exact Test Parameters";
	
	public NonparFisherPanel(AlgorithmData parameters, JDialog parent) {
		super(new GridBagLayout());
		this.parent = parent;
		//supportFDR = true;                
		algData = parameters;
	}
	
	public void initializePanel() {
		removeAll();
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),FISHER_TITLE));		
		Listener listener = new Listener();
		
		binNames = algData.getStringArray("fisher-exact-bin-names");
		groupNames = algData.getStringArray("group-names");
		
		JLabel cutoffLabel = new JLabel("<html>Data Bin Partition Cutoff Value:<br>( segregates data values into two bins )</html>");		
		cutoffField = new JTextField("0.0");		
		JLabel polarityInstructionLabel = new JLabel("<html>Select the data bin label that represents data values <u>greater</u> than<br> the cutoff value selected above:");
		
		binOneButton = new JRadioButton("\""+binNames[0]+"\" Data Bin", true);
		binOneButton.setFocusPainted(false);

		binTwoButton = new JRadioButton("\""+binNames[1]+"\" Data Bin");
		binTwoButton.setFocusPainted(false);
		
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(binOneButton);
		bg1.add(binTwoButton);
		
		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
		sep1.setPreferredSize(new Dimension(200, 2));
		
		ContingencyMatrixPanel matrixPanel = new ContingencyMatrixPanel(groupNames, binNames);
		
		JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
		sep2.setPreferredSize(new Dimension(200, 2));
		
		//Hypothesis specification
		//Fill this in if we want to provide a one tailed test
		//JRadioButton twoTailedButton = new JRadioButton("<html>Two Tailed (non-random association<br>between the two variables)</html>");		
		//JRadioButton rightButton = new JRadioButton("<html>Right Tail (directional hypothesis,<br>)</html>");
				
		ButtonGroup bg = new ButtonGroup();
		pValueButton = new JRadioButton("Use p-value Significance Criterion", true);
		pValueButton.setIconTextGap(8);
		pValueButton.setFocusPainted(false);
		pValueButton.addActionListener(listener);
		bg.add(pValueButton);
		
		alphaLabel = new JLabel("Alpha, critcal p-value:");
		alphaField = new JTextField("0.05");
		alphaField.setPreferredSize(new Dimension(60,20));
		
		hclBox = new JCheckBox("<html>Create Hierarchical Trees (on significant genes)</html>", false);	
		hclBox.setFocusPainted(false);
		hclBox.setOpaque(false);
		hclBox.setIconTextGap(8);
		hclBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),"Hierarcical Clustering"));		
		
		fdrButton = new JRadioButton("<html>Use FDR Significance Criterion<br>(based on Benjamini-Hochberg Correction)</html>");
		fdrButton.setIconTextGap(8);
		fdrButton.setFocusPainted(false);
		fdrButton.addActionListener(listener);
		bg.add(fdrButton);
		
		fdrGraphBox = new JCheckBox("<html>Select FDR After Analysis<br><c>(interactive mode)</c></html>", true);
		fdrGraphBox.setIconTextGap(8);
		fdrGraphBox.setFocusPainted(false);
		fdrGraphBox.setEnabled(false);
		fdrGraphBox.addActionListener(listener);
		
		fdrLimitLabel = new JLabel("Selected FDR Limit: ");
		//fdrLimitLabel.setEnabled(false);
		
		fdrField = new JTextField("0.05");
		fdrField.setPreferredSize(new Dimension(60,20));
		fdrField.setEnabled(false);		
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(200, 2));
		
		add(cutoffLabel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,15,10,10),0,0));		
		add(cutoffField, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,10,10),0,0));		
		
		add(polarityInstructionLabel, new GridBagConstraints(0,1,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(15,15,10,10),0,0));		
		JPanel binPanel = new JPanel(new GridBagLayout());
		binPanel.add(binOneButton, new GridBagConstraints(0,0,1,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,15,0),0,0));		
		binPanel.add(binTwoButton, new GridBagConstraints(1,0,1,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,15,0),0,0));		
		add(binPanel, new GridBagConstraints(0,2,2,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		
		add(sep1, new GridBagConstraints(0,3,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,15,20),0,0));		

		add(matrixPanel, new GridBagConstraints(0,4,2,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10),0,0));		

		add(sep2, new GridBagConstraints(0,5,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15,20,15,20),0,0));		

		add(pValueButton, new GridBagConstraints(0,6,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,15,10,10),0,0));		
		add(alphaLabel, new GridBagConstraints(0,7,1,1,1,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,15,10),0,0));
		add(alphaField, new GridBagConstraints(1,7,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,15,10),0,0));	
		
		add(sep, new GridBagConstraints(0,8,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,10,20),0,0));	
		add(fdrButton, new GridBagConstraints(0,9,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,15,5,0),0,0));			
		add(fdrGraphBox, new GridBagConstraints(0,10,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));	
		add(fdrLimitLabel, new GridBagConstraints(0,11,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,10),0,0));	
		add(fdrField, new GridBagConstraints(1,11,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,10,10),0,0));	
		add(hclBox, new GridBagConstraints(0,12,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,10),0,0));
	
		//info button
		JButton infoButton = new JButton(null, GUIFactory.getIcon("Information24.gif"));
        infoButton.setActionCommand("info-command"); 
        infoButton.setSize(30,30);
        infoButton.setPreferredSize(new Dimension(30,30));
        infoButton.setFocusPainted(false);
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        infoButton.setBorder(border);

        infoButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        		   HelpWindow hw = new HelpWindow(parent, "NonpaR Fisher Exact Parameters");
                   if(hw.getWindowContent()){
                       hw.setSize(600,600);
                       hw.setLocation();
                       hw.setVisible(true);
                   }
                   else {
                       hw.setVisible(false);
                       hw.dispose();
                   }
        	}
        });
        
		add(infoButton, new GridBagConstraints(0,13,1,1,1,0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,5,5,0),0,0));				        
	}
	
	
	public class ContingencyMatrixPanel extends JPanel {
		private JLabel c1,c2,r1,r2;
		
		public ContingencyMatrixPanel(String [] groupNames, String [] binNames) {
			super(new GridBagLayout());
			
			c1Index = 0;
			r1Index = 0;
			
			c1 = new JLabel(groupNames[0]);
			c2 = new JLabel(groupNames[1]);
			
			r1 = new JLabel(binNames[0]);
			r2 = new JLabel(binNames[1]);
			
			JButton colSwapButton = new JButton("Swap Columns");
			colSwapButton.setFocusPainted(false);
			colSwapButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					swapCols();
				}});
			
			JButton rowSwapButton = new JButton("Swap Rows");
			rowSwapButton.setFocusPainted(false);
			rowSwapButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					swapRows();
				}});
			
			JButton d11 = new JButton(" ");
			d11.setFocusPainted(false);
			d11.setEnabled(false);
			d11.setBackground(Color.gray);
			d11.setOpaque(true);
			d11.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			JButton d12 = new JButton(" ");
			d12.setFocusPainted(false);
			d12.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			d12.setEnabled(false);
			JButton d21 = new JButton(" ");
			d21.setFocusPainted(false);			
			d21.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			d21.setEnabled(false);
			JButton d22 = new JButton(" ");
			d22.setFocusPainted(false);
			d22.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			d22.setEnabled(false);


			add(new JLabel("<html>The orientation of the 2x2 contingency matrix can be modified.<br>" +
					"The test reports tail probabilities relative to the upper left cell.<br>" +
					"(alteration is optional and will not effect the two tailed p-value)</html>"), new GridBagConstraints(0,0,4,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,10,5),0,0));
			add(colSwapButton, new GridBagConstraints(2,1,2,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,5,0),0,0));

			add(c1, new GridBagConstraints(2,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,5,0),0,0));
			add(c2, new GridBagConstraints(3,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,5,10),0,0));

			add(rowSwapButton, new GridBagConstraints(0,3,1,2,1,1,GridBagConstraints.CENTER, GridBagConstraints.EAST, new Insets(0,0,0,10),0,0));
			add(r1, new GridBagConstraints(1,3,1,1,0,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,5),0,0));
			add(d11, new GridBagConstraints(2,3,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			add(d12, new GridBagConstraints(3,3,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,10),0,0));

			add(r2, new GridBagConstraints(1,4,1,1,0,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,5),0,0));
			add(d21, new GridBagConstraints(2,4,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			add(d22, new GridBagConstraints(3,4,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,10),0,0));
			
		}
		
		
		private void swapCols() {
			//flip the index
			c1Index = (c1Index +1)%2;
			String temp = c1.getText();
			c1.setText(c2.getText());
			c2.setText(temp);
			validate();
		}
		
		private void swapRows() {
			//flip the index
			r1Index = (r1Index +1)%2;
			String temp = r1.getText();
			r1.setText(r2.getText());
			r2.setText(temp);
			validate();
		}				
	}
	
	/**
	 * IWizardParameterPanel method to set parameters
	 */
	public void populateAlgorithmData() {
		algData.addParam("use-alpha-criterion", String.valueOf(this.pValueButton.isSelected()));
		if(pValueButton.isSelected())		
			algData.addParam("alpha", alphaField.getText());		
		else {
			algData.addParam("use-fdr-graph", String.valueOf(fdrGraphBox.isSelected()));
			if(!fdrGraphBox.isSelected())
				algData.addParam("fdr", fdrField.getText());
		}						
		algData.addParam("hcl-execution", String.valueOf(runHCL()));
		
		algData.addParam("fisher-exact-bin-cutoff", cutoffField.getText());
		algData.addParam("upper-bin-index", binOneButton.isSelected() ? "0" : "1");
		
		algData.addParam("swap-groups", String.valueOf(c1Index == 1));
		algData.addParam("swap-bins", String.valueOf(r1Index == 1));
	}
	
	/**
	 * IWizardParameterPanel method to clear parameters
	 */	
	public void clearValuesFromAlgorithmData() {
		algData.getParams().getMap().remove("use-alpha-criterion");		
		algData.getParams().getMap().remove("alpha");		
		algData.getParams().getMap().remove("use-fdr-graph");		
		algData.getParams().getMap().remove("fdr");		
		algData.getParams().getMap().remove("hcl-execution");	
		
		algData.getParams().getMap().remove("fisher-exact-bin-cutoff");	
		algData.getParams().getMap().remove("upper-bin-index");	

		algData.getParams().getMap().remove("swap-groups");	
		algData.getParams().getMap().remove("swap-bins");	
	}
	
	/**
	 * IWizardParameterPanel method to adjust for display (if needed)
	 */
	public void onDisplayed() {
		
	}
	
	public boolean runHCL() {
		return hclBox.isSelected();
	}
	
	private long factorial(int n) {
		if ((n==1) || (n == 0)) {
			return 1;
		}
		else {
			return factorial(n-1) * n;
		}
	}
	
	private int getNumCombs(int n, int k) { // nCk
		return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
	}
	
	public class Listener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			//alphaLabel.setEnabled(pValueButton.isSelected());
			alphaField.setEnabled(pValueButton.isSelected());
			fdrGraphBox.setEnabled(fdrButton.isSelected());
			//fdrGraphBox.getComponentAt(fdrGraphBox.getWidth()-5, 4).setEnabled(fdrButton.isSelected());
			//fdrLimitLabel.setEnabled(!fdrGraphBox.isSelected());
			fdrField.setEnabled(!fdrGraphBox.isSelected());
		}
		
	}
	
	/*
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		String [] names = {"Control", "Experimental"};
		
		AlgorithmData data = new AlgorithmData();
		String [] bins = {"Present", "Absent"};
		String [] groupNames = {"Pathogenic", "Non-pathogenic"};
		data.addStringArray("fisher-exact-bin-names", bins);
		data.addStringArray("group-names", groupNames);
		
		NonparFisherPanel p = new NonparFisherPanel(data, frame);
		frame.getContentPane().add(p);
		p.initializePanel();
		
		frame.setSize(400,400);
		frame.pack();
		frame.setVisible(true);
	}
	*/
}
