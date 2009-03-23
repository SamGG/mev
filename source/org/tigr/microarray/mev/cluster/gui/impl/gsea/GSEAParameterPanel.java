package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.file.GBA;

public class GSEAParameterPanel extends JPanel implements IWizardParameterPanel{

	/**
	 * @param args
	 */
	private JPanel parameterPanel;
	private JLabel probeInformationLabel;
	private JLabel probe2GeneLabel;
	private JLabel probe2GeneInfo;
	private JComboBox choiceBox;
	private JLabel minGeneLabel;
	private JTextField geneNumber;
	private JLabel SDCutoffLabel;
	private JTextField sdTextField;
	private JLabel permutationLabel;
	private JTextField permutationTextField;
	
	private AlgorithmData algData;
	
	
	public GSEAParameterPanel(AlgorithmData algData, JDialog parent) {
		this.algData=algData;
		initializePanel();
		initialize(GSEAConstants.MAX_PROBE, Integer.toString(5), "0.6", Integer.toString(250));
	}
	
	
	/**
	 * Initializes data panel based on params. 
	 * 	 
	 * @param collapseMode
	 * @param minGenes
	 */
	    
	public void initialize(String collapseMode, String minGenes, String SDcutoff, String num_Perms ) {
		this.choiceBox.setSelectedItem(collapseMode);
		this.geneNumber.setText(minGenes);
		this.sdTextField.setText(SDcutoff);
		
		this.permutationTextField.setText(num_Perms);
		
		
	}
	
	
	public void initializePanel() {
		GBA gba=new GBA();
		setLayout(new GridBagLayout());
		
		String[] Key= {"Max_Probe", "Median_Probe", "Standard_Deviation"};

		probeInformationLabel=new JLabel();
	    probeInformationLabel.setText("<html>( If multiple probes in the expression data map to one gene, MeV can use either the<br>" +
	    							   "1. Probe with the maximum expression value.<br>" +
	    							   "2. Median of the expression values.<br>" +
	    							   "3. SD of the expression values...whichever you choose)<br></html>");
	    probeInformationLabel.setHorizontalTextPosition(SwingConstants.CENTER);
	    probeInformationLabel.setFont(new Font("Dialog",Font.ITALIC, 12));
        
        probe2GeneLabel=new JLabel();
		probe2GeneLabel.setText("Select a method to collapse probes"); 
		probe2GeneLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		//probe2GeneLabel.setFont(new Font("Dialog",Font.PLAIN, 12));
		
		
		SDCutoffLabel=new JLabel();
		SDCutoffLabel.setText("Select a cut off, if you chose Standard Deviation to collapse probes");
		SDCutoffLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		SDCutoffLabel.setEnabled(false);
		
		sdTextField=new JTextField();
		sdTextField.setEditable(true);
		sdTextField.setEnabled(false);
		sdTextField.setActionCommand("standard-deviation");
		
		
	
		minGeneLabel=new JLabel();
		minGeneLabel.setText("Minimum number of genes per gene set"); 
		minGeneLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		//minGeneLabel.setFont(new Font("Dialog",Font.PLAIN, 12));
	
		
		geneNumber=new JTextField();
		geneNumber.setEditable(true);
		geneNumber.setActionCommand("gene-number");
		
		permutationLabel=new JLabel();
		permutationLabel.setText("Enter number of permutations");
		permutationLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		
		permutationTextField=new JTextField();
		permutationTextField.setEditable(true);
		permutationTextField.setActionCommand("permutations");
		
			
		
		choiceBox=new JComboBox(Key);
		choiceBox.addActionListener(new Listener());
		
		parameterPanel = new JPanel();
        parameterPanel.setLayout(new GridBagLayout());
        parameterPanel.setBorder(new TitledBorder(new EtchedBorder(), "GSEA-Parameters"));
        
        
		gba.add(parameterPanel, probe2GeneLabel, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, choiceBox, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, probeInformationLabel, 0, 3, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);

		gba.add(parameterPanel, SDCutoffLabel, 0, 4, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, sdTextField, 2, 4, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 
		

		gba.add(parameterPanel, minGeneLabel, 0, 7, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, geneNumber, 2, 7, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 
		
		gba.add(parameterPanel, permutationLabel, 0, 8, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, permutationTextField, 2, 8, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 
		
		
	    gba.add(this, parameterPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

      	
				
	}
	
	
	protected void updateLabel(String name) {
		choiceBox.setSelectedItem(name);
		
	}
	
	
private class Listener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
						
			if(e.getSource().equals(choiceBox)) {
				updateLabel((String)choiceBox.getSelectedItem());
				if(((String)choiceBox.getSelectedItem()).equalsIgnoreCase("Standard_Deviation")){
					SDCutoffLabel.setEnabled(true);
					sdTextField.setEnabled(true);
					
				}else{
					SDCutoffLabel.setEnabled(false);
					sdTextField.setEnabled(false);
				}
			}else if(command.equalsIgnoreCase("gene-number")) {
				geneNumber.setText(geneNumber.getText());
			}else if(command.equalsIgnoreCase("standard-deviation")){
				sdTextField.setText(sdTextField.getText());
			}else if(command.equalsIgnoreCase("permutations")){
				permutationTextField.setText(permutationTextField.getText());
			}
			
	}    
	
}
	


	
	public void clearValuesFromAlgorithmData() {
		
		algData.getParams().getMap().remove("probe_value");
		algData.getParams().getMap().remove("gene-number");
		algData.getParams().getMap().remove("standard-deviation-cutoff");
		algData.getParams().getMap().remove("permutations");
		
		
	}

	
	public void onDisplayed() {
		// TODO Auto-generated method stub
		
	}

	
	public void populateAlgorithmData() {
		if(choiceBox.getSelectedItem()!=null){
			if(choiceBox.getSelectedItem().equals("Max_Probe"))
				algData.addParam("probe_value", GSEAConstants.MAX_PROBE);
			if(choiceBox.getSelectedItem().equals("Median_Probe"))
				algData.addParam("probe_value", GSEAConstants.MEDIAN_PROBE);
			if(choiceBox.getSelectedItem().equals("Standard_Deviation")){
				algData.addParam("probe_value", GSEAConstants.SD);
				algData.addParam("standard-deviation-cutoff", sdTextField.getText());
			}
		}
			else{
			algData.addParam("probe_value", GSEAConstants.MAX_PROBE);
			algData.addParam("standard-deviation-cutoff", "NA");
			}
		
		//if(geneNumber.getText()!=null)
			algData.addParam("gene-number", geneNumber.getText());
			algData.addParam("permutations", permutationTextField.getText());
		
	}
	
	
	
/*	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		GSEAParameterPanel p = new GSEAParameterPanel();	
		//p.setVisible(true);
		frame.getContentPane().add(p);
		frame.setSize(600,600);
		frame.setVisible(true);

	}*/

}
