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
public class NonparWilcoxonPanel extends JPanel implements
	IWizardParameterPanel {

	private AlgorithmData algData;

	private JRadioButton pValueButton;
	private JRadioButton fdrButton;
	private JCheckBox fdrGraphBox;
	
	private JLabel alphaLabel;
	private JTextField alphaField;
	private JLabel fdrLimitLabel;
	private JTextField fdrField;
	
	private JCheckBox hclBox;

	private boolean supportFDR;
	
	private String WILCOXON_TITLE = "Wilcoxon, Mann-Whitney Test Parameters";
	private String KRUSKAL_WALLIS_TITLE = "Kruskal-Wallis Test Parameters";
	private String MACK_SKILLINGS_TITLE = "Mack-Skillings Test Parameters";
	
	private JDialog parent;
	
	public NonparWilcoxonPanel(AlgorithmData parameters, JDialog parent) {
		super(new GridBagLayout());
		this.parent = parent;
		supportFDR = true;
		algData = parameters;		
	}
	
	public NonparWilcoxonPanel(String [] groupNames, int numX, int numY, String mode, JDialog parent) {
		super(new GridBagLayout());
		this.parent = parent;
		supportFDR = true;
		initializePanel(mode);
	}
	
	public void initializePanel(String mode) {
		removeAll();
		
		if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS))
			supportFDR = false;
		
		if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY))
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),WILCOXON_TITLE));		
		else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS))
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),KRUSKAL_WALLIS_TITLE));					
		else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS))
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),MACK_SKILLINGS_TITLE));					
		
		Listener listener = new Listener();
		
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
		
		int infoY = 7;
		
		if(supportFDR) {
			
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
			
			add(pValueButton, new GridBagConstraints(0,0,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,10,10,10),0,0));		
			add(alphaLabel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,10,10),0,0));
			add(alphaField, new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,10,10),0,0));	
			
			add(sep, new GridBagConstraints(0,2,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,10,20),0,0));	
			add(fdrButton, new GridBagConstraints(0,3,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,10,5,0),0,0));			
			add(fdrGraphBox, new GridBagConstraints(0,4,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));	
			add(fdrLimitLabel, new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,10),0,0));	
			add(fdrField, new GridBagConstraints(1,5,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));	
			add(hclBox, new GridBagConstraints(0,6,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20,10,10,10),0,0));					
		} else {
			infoY = 3;
			add(alphaLabel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,10,10),0,0));
			add(alphaField, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,10,10),0,0));				
			add(hclBox, new GridBagConstraints(0,1,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20,10,10,10),0,0));		
		}
		
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
        		   HelpWindow hw = new HelpWindow(parent, "NonpaR Significance Parameters");
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
        
		add(infoButton, new GridBagConstraints(0,infoY,1,1,1,0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,5,0),0,0));				        
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
		NonparWilcoxonPanel p = new NonparWilcoxonPanel(new AlgorithmData(), frame);
		frame.getContentPane().add(p);
		p.initializePanel(NonparConstants.MODE_WILCOXON_MANN_WHITNEY);
		
		frame.setSize(400,400);
		frame.setVisible(true);
	}
	*/
}
