package org.tigr.microarray.mev.cluster.gui.impl.bn;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RunWekaProgressPanel extends JFrame implements ActionListener {

	    private JProgressBar progressBar;
//	    private JButton cancelButton;
	    //MultipleArrayViewer mav;
	    JPanel progressPanel;
	    
	    public RunWekaProgressPanel() {
		setTitle("Building Network ...");    
		progressPanel = new JPanel(new BorderLayout());
		progressPanel.setPreferredSize(new Dimension(350, 80));
            
	        progressBar = new JProgressBar(0, 100);
	        //progressBar.setValue(0);
		    progressBar.setString("");
	        progressBar.setStringPainted(true);
		
	        progressBar.setPreferredSize(new Dimension(310, 30));
                 

	        progressPanel.add(progressBar, BorderLayout.PAGE_START);
	        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(progressPanel);

	        progressPanel.setOpaque(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack(); 
		setVisible(true);
	    }
	    
	     public void setIndeterminate(boolean b){
	    	progressBar.setIndeterminate(b);
	    }
	    
	    public void actionPerformed(ActionEvent evt) {
	    	String command = evt.getActionCommand();
            if (command.equals("cancel")) {
    			//mav.cancelLoadState();
    			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			progressBar.setIndeterminate(true);
            }
	    }
	}    
