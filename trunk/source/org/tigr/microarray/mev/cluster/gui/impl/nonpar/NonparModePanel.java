/*
 * Created on Jan 9, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
public class NonparModePanel extends JPanel implements IWizardParameterPanel {

	private AlgorithmData algData;
	
	private JRadioButton wilcoxonButton;
	private JRadioButton kruskalButton;
	private JRadioButton mackSkillingsButton;
	private JRadioButton fisherExactButton;
	JDialog parent;
	
	public NonparModePanel(AlgorithmData paramData, JDialog parent) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Test/Mode"));
		algData = paramData;
		this.parent = parent;
		constructPanel();
	}
	
	private void constructPanel() {
		//setBackground(Color.white);
		 		
		ButtonGroup bg = new ButtonGroup();
		
		wilcoxonButton = new JRadioButton("<html><u>Wilcoxon, Mann-Whitney Test</u><br>(one factor, two experimental groups)</html>", true);
		//wilcoxonButton.setFocusPainted(true);
		wilcoxonButton.setOpaque(false);
		wilcoxonButton.setIconTextGap(10);
		//wilcoxonButton.setVerticalTextPosition(JRadioButton.TOP);
		bg.add(wilcoxonButton);
		
		kruskalButton = new JRadioButton ("<html><u>Kruskal-Wallis Test</u><br>(one factor, n experimental groups)</html>");
		//kruskalButton.setFocusPainted(false);
		kruskalButton.setOpaque(false);
		kruskalButton.setIconTextGap(10);
		bg.add(kruskalButton);
		
		mackSkillingsButton = new JRadioButton ("<html><u>Mack-Skillings Test</u><br>(two-factor designs, n x k)</html>");
		//friedmanButton.setFocusPainted(false);
		mackSkillingsButton.setOpaque(false);
		mackSkillingsButton.setIconTextGap(10);
		bg.add(mackSkillingsButton);
		
		fisherExactButton = new JRadioButton ("<html><u>Fisher Exact Test</u><br>(two experimental groups,<br>data represents two categories or bins)</html>");
		//friedmanButton.setFocusPainted(false);
		fisherExactButton.setOpaque(false);
		fisherExactButton.setIconTextGap(10);
		bg.add(fisherExactButton);
		
		add(wilcoxonButton, new GridBagConstraints(0,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,20),0,0));
		add(kruskalButton, new GridBagConstraints(0,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,20),0,0));
		add(mackSkillingsButton, new GridBagConstraints(0,2,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,20),0,0));				
		add(fisherExactButton, new GridBagConstraints(0,3,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,20),0,0));				
	
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
        		   HelpWindow hw = new HelpWindow(parent, "NonpaR Mode Selection");
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
        
		add(infoButton, new GridBagConstraints(0,4,1,1,1,0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,5,0),0,0));				        
	}
	
	
	
	/**
	 * IWizardParameterPanel method to set parameters
	 */
	public void populateAlgorithmData() {
		if(wilcoxonButton.isSelected()) {
			algData.addParam("nonpar-mode", NonparConstants.MODE_WILCOXON_MANN_WHITNEY);
		} else if(kruskalButton.isSelected()) {
			algData.addParam("nonpar-mode", NonparConstants.MODE_KRUSKAL_WALLIS);
		} else if(mackSkillingsButton.isSelected()) {
			algData.addParam("nonpar-mode", NonparConstants.MODE_MACK_SKILLINGS);			
		} else if(fisherExactButton.isSelected()) {
			algData.addParam("nonpar-mode", NonparConstants.MODE_FISHER_EXACT);				
		}
	}

	/**
	 * IWizardParameterPanel method to clear parameters
	 */
	public void clearValuesFromAlgorithmData() {
		algData.getParams().getMap().remove("nonpar-mode");
	}

	/**
	 * IWizardParameterPanel method to adjust for display (if needed)
	 */
	public void onDisplayed() {
		
	}
	
	/*
	public static void main(String [] args) {
		JFrame frame = new JFrame();
		NonparModePanel p = new NonparModePanel(new AlgorithmData(), frame);		
		frame.getContentPane().add(p);
		frame.setSize(300,300);
		frame.setVisible(true);		
	}
	*/
}
