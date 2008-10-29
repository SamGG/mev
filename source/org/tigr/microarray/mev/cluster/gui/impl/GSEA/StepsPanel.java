package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.file.GBA;

public class StepsPanel extends JPanel implements IWizardParameterPanel{

	/**
	 * NOTE: Want to keep the steps panel here, because this panel will only be shown
	 * at the beginning. This would be replaced by the GSEADataPanel on clicking
	 * next button. 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param args
	 */
	
	  
	    private javax.swing.JPanel StepsPanel;
	  
	    private javax.swing.JScrollPane jScrollPane2;
	    private javax.swing.JTextArea stepsTextArea;
	    private javax.swing.JTextField titleTextField;
	
	    
	//Change the constructor as needed. 
	public StepsPanel() {
		initializePanel();
	}
	    
	    
	    
	    
	public void initializePanel() {
			
			GBA gba = new GBA();
			setLayout(new GridBagLayout());
	       
	        
	        jScrollPane2 = new javax.swing.JScrollPane();
	        stepsTextArea = new javax.swing.JTextArea();
	        titleTextField = new javax.swing.JTextField();
	    
	        titleTextField.setBackground(new Color(233,230,212)); 
	        titleTextField.setBorder(null);
	        titleTextField.setLayout(new GridBagLayout());
	        titleTextField.setEditable(false);
	        titleTextField.setText("STEPS"); 
	        titleTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
	       	titleTextField.setForeground(Color.black);
            titleTextField.setFont(new Font("Times New Roman", Font.BOLD, 18));
            
            
	       //JScrollPane layout 
	        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	        jScrollPane2.setBackground(new Color(233,230,212)); 
	        jScrollPane2.setBorder(null);
	        jScrollPane2.setPreferredSize(new java.awt.Dimension(350,400));

	        stepsTextArea.setBackground(new Color(233,230,212)); 
	        stepsTextArea.setLayout(new GridBagLayout());
	        stepsTextArea.setColumns(20);
	        stepsTextArea.setEditable(false);
	        stepsTextArea.setBorder(null);
	       
	        stepsTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 18));
	        stepsTextArea.setText(" 1. GSEA Requirements "+  "\n"+
	        						"- Annotation file (if not already loaded)"+"\n"+
	        						"- Gene set file (gst, gmx and txt formats)"+"\n"+
	        						"- Factor file"+"\n"+"\n"+"\n"+

	        					   "2. Run GSEA"+"\n"+
	        						"- Probes will be collapsed to genes"+"\n"+
	        						"- Default parameters provided"+"\n"+"\n"+"\n"+

	        						"3. View Results" ); 
	        stepsTextArea.setAutoscrolls(false);
	        jScrollPane2.setViewportView(stepsTextArea);
	    
	        
	        StepsPanel = new javax.swing.JPanel();
	        StepsPanel.setLayout(new GridBagLayout());
	        StepsPanel.setBackground(new Color(233,230,212));
	       
	        gba.add(StepsPanel,titleTextField, 0, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            gba.add(StepsPanel, jScrollPane2, 0, 2, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            gba.add(this, StepsPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            
	}
	
	
	
	
	
	
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		StepsPanel p = new StepsPanel();	
		//p.setVisible(true);
		frame.getContentPane().add(p);
		frame.setSize(600,600);
		frame.setVisible(true);

	}*/




	
	public void clearValuesFromAlgorithmData() {
		// TODO Auto-generated method stub
		
	}




	
	public void onDisplayed() {
		// TODO Auto-generated method stub
		
	}




	
	public void populateAlgorithmData() {
		// TODO Auto-generated method stub
		
	}

}
