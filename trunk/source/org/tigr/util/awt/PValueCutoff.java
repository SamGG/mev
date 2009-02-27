package org.tigr.util.awt;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.tigr.util.awt.BoundariesDialog.EventListener;

public class PValueCutoff extends ActionInfoDialog{
	 private JFrame parent;
	 
	    JLabel selectOptionLabel, heading;
	    JTextField pValueTextField;
	    JButton okButton, cancelButton;
	    ButtonGroup optionsButtonGroup;
	    JRadioButton option1Radio;
	    JRadioButton option2Radio;
	    JRadioButton option3Radio;
	    
	    Font boundariesDialogFont;
	    GBA gba;
	   

	public PValueCutoff(JFrame parent, double pValue) {
		super(parent, true);
		// TODO Auto-generated constructor stub
		try{
			  gba = new GBA();
			    
			  	heading=new JLabel("Enter a p value cutoff");
			    selectOptionLabel = new JLabel("Show me Genesets with pValues");
			   
			    pValueTextField = new JTextField(12);
			    pValueTextField.setText("" + 0.05);
			    
			    optionsButtonGroup = new ButtonGroup();
			    
			    option1Radio=new JRadioButton("Less than cutoff", true);
			    option1Radio.setName("option1");
			    optionsButtonGroup.add(option1Radio);
			    option2Radio=new JRadioButton("Greater than cutoff");
			    option2Radio.setName("option2");
			    optionsButtonGroup.add(option2Radio);
			    option3Radio=new JRadioButton("Equal to cutoff");
			    option3Radio.setName("option3");
			    optionsButtonGroup.add(option3Radio);
			    
			    
			    okButton = new JButton("OK");
			    okButton.addActionListener(new EventListener());
			    cancelButton = new JButton("Cancel");
			    cancelButton.addActionListener(new EventListener());
			    
			    contentPane.setLayout(new GridBagLayout());
			    
			    gba.add(contentPane, heading, 0, 0, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, pValueTextField, 0, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, selectOptionLabel, 1, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, option1Radio, 2, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, option2Radio, 3, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, option3Radio, 4, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    gba.add(contentPane, cancelButton, 0, 3, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			     gba.add(contentPane, okButton, 2, 3, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			    
			    pack();
			    setResizable(false);
			    setTitle("Set Graph Boundaries");
			    okButton.grabFocus();
			    setLocation(300, 300);

			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}
	
	  class EventListener implements ActionListener, KeyListener {
			public void actionPerformed(ActionEvent event) {
				
				String key="";
				double pvalue=0.05;
				if(event.getSource() == option1Radio){
					key=option1Radio.getName();
				}else if(event.getSource() == option2Radio){
					key=option2Radio.getName();
				}else if(event.getSource()== option3Radio){
					key=option3Radio.getName();
				}else if(event.getSource()== pValueTextField){
					pvalue=Double.parseDouble(pValueTextField.getText());
				}else if (event.getSource() == okButton) {
				
				Hashtable hash = new Hashtable();
				hash.put(key, pvalue);
				
				fireEvent(new ActionInfoEvent(this, hash));
				
				dispose();
			    } else if (event.getSource() == cancelButton) dispose();
			}
			
			
			//Enter key press
			public void keyPressed(KeyEvent event) {
			   
			}
			
			public void keyReleased(KeyEvent event) {}
			public void keyTyped(KeyEvent event) {}
		    }

	
	
	
	
	

}
