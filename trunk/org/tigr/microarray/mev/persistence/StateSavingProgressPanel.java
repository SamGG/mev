/*
 * Progress bar used for monitoring the state-saving and loading process.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.persistence;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.action.ActionManager;

/**
 * @author eleanora
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//public class StateSavingProgressPanel extends JPanel implements ActionListener {
	public class StateSavingProgressPanel extends JFrame implements ActionListener {

	    private JProgressBar progressBar;
//	    private JButton cancelButton;
	    MultipleArrayViewer mav;
	    JPanel progressPanel;
	    
	    public StateSavingProgressPanel(String initialMessage, MultipleArrayViewer mav) {
			setTitle(initialMessage);
            this.mav = mav;
            
			progressPanel = new JPanel(new BorderLayout());
			progressPanel.setPreferredSize(new Dimension(350, 120));
            
	        progressBar = new JProgressBar(0, 100);
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);
	        progressBar.setPreferredSize(new Dimension(310, 30));

//	        cancelButton = new JButton("Cancel");
//	        cancelButton.setActionCommand("cancel");	      
//	        cancelButton.addActionListener(this);
//	        cancelButton.setPreferredSize(new Dimension(70, 30));

	        progressPanel.add(progressBar, BorderLayout.PAGE_START);
//	        progressPanel.add(cancelButton, BorderLayout.CENTER);
	        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	        
			setContentPane(progressPanel);

	        progressPanel.setOpaque(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			pack(); 
			setVisible(true);
	    }
	    
	    public void update(String message){
	    	progressBar.setString(message);
	    	progressBar.setValue(progressBar.getMinimum());
	    }
	    
	    public void increment(){
	    	progressBar.setValue(progressBar.getValue() + 1);
	    }
	    
	    public void setMaximum(int i){
	    	progressBar.setMaximum(i);
	    }
	    
	    public int getMaximum(){
	    	return progressBar.getMaximum();
	    }

	    public void setValue(int i){
	    	progressBar.setValue(i);
	    }

	    public void setIndeterminate(boolean b){
	    	progressBar.setIndeterminate(b);
	    }
	    
	    public void actionPerformed(ActionEvent evt) {
	    	String command = evt.getActionCommand();
            if (command.equals("cancel")) {
    			mav.cancelLoadState();
    			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			progressBar.setIndeterminate(true);
    			progressBar.setString("Cleaning Up...");
            }
	    }
	    public void onClose(){
	    	mav.cancelLoadState();
	    }
}

