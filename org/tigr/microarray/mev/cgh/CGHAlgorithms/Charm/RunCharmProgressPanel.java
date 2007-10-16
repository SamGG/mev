package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

/**
 * @author  Raktim Sinha
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

public class RunCharmProgressPanel extends JDialog implements ActionListener {

	private JProgressBar progress;
    private JLabel description;
    private static final String DESCRIPTION = "Description: ";
    
    public RunCharmProgressPanel(Frame parent, String title, DialogListener listener) {
        super(parent, title);
        this.description = new JLabel();
        setDescription("");
        //this.progress = new JProgressBar();
        progress = new JProgressBar(0, 100);
        this.progress.setString("");
		this.progress.setStringPainted(true);
		this.progress.setPreferredSize(new Dimension(310, 30));
        //this.progress.setStringPainted(true);
        JPanel progressPanel = createProgressPanel(this.description, this.progress);
        progressPanel.setBackground(Color.white);
        JPanel btnsPanel = createBtnsPanel(listener);
        
        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        content.add(new HeaderImagePanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        content.add(progressPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 0, 3), 0, 0));
        content.add(btnsPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
        addWindowListener(listener);
        setResizable(false);
        pack();
    }
    
    /**
     * Shows the dialog.
     */
    public void show() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        setIndeterminate(true);
		//setLocation((screenSize.width-getSize().width)/2,(screenSize.height-getSize().height)/2);
        //setVisible(true);
        super.show();
    }
    
    /**
     * Sets description.
     */
    public void setDescription(String text) {
        if (text == null) {
            text = "";
        }
        description.setText(DESCRIPTION+text);
    }
    
    /**
     * Creates a progress bar panel.
     */
    private JPanel createProgressPanel(JLabel description, JProgressBar progress) {
    	
    	
    	JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setPreferredSize(new Dimension(350, 80));
		//progress = new JProgressBar(0, 100);
	    //progressBar.setValue(0);
            
		progressPanel.add(this.progress, BorderLayout.PAGE_START);
	    progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	    progressPanel.setPreferredSize(new Dimension(400, 65));
		//setContentPane(progressPanel);

	    progressPanel.setOpaque(true);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//pack(); 
		//setVisible(true);
		return progressPanel;
    	/*
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(progress, gbc);
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        panel.add(description, gbc);
        panel.setPreferredSize(new Dimension(400, 65));
        return panel;
        */
    }
    
    /**
     * Creates a panel with cancel button.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBorder(javax.swing.BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(240,240,240), new Color(180,180,180), new Color(10,0,0), new Color(10,10,10) ));
        cancelButton.setPreferredSize(new Dimension(80,25));
        cancelButton.setActionCommand("cancel-command");
        cancelButton.addActionListener(listener);
        cancelButton.setFocusPainted(false);
        panel.add(cancelButton, BorderLayout.CENTER);
        getRootPane().setDefaultButton(cancelButton);
        return panel;
    }
    
	    //private JProgressBar progressBar;
//	    private JButton cancelButton;
	    //MultipleArrayViewer mav;
	    //JPanel progressPanel;
	    
	    public RunCharmProgressPanel() {
	    	/*
		setTitle("Running ChARM...");    
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
		*/
	    }
	    
	     public void setIndeterminate(boolean b){
	    	//progressBar.setIndeterminate(b);
	    	 progress.setIndeterminate(b);
	    }
	    
	    public void actionPerformed(ActionEvent evt) {
	    	String command = evt.getActionCommand();
            if (command.equals("cancel")) {
    			//mav.cancelLoadState();
    			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			//progressBar.setIndeterminate(true);
    			progress.setIndeterminate(true);
            }
	    }
	    
	    public class HeaderImagePanel extends JPanel{
	        
	        public HeaderImagePanel(){
	            setLayout(new GridBagLayout());
	            JLabel iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
	            iconLabel.setOpaque(false);
	            iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	            FillPanel fillPanel = new FillPanel();
	            fillPanel.setBackground(Color.blue);
	            add(iconLabel, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,5,0,0),0,0));
	            add(fillPanel, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
	       }
	    }
	    
	    public class FillPanel extends JPanel{
	        GradientPaint gp;
	        Color backgroundColor = new Color(25,25,169);
	        Color fadeColor = new Color(140,220,240);
	        
	        public void paint(Graphics g){
	            super.paint(g);
	            Graphics2D g2 = (Graphics2D)g;
	            Dimension dim = this.getSize();
	            //                gp = new GradientPaint(dim.width/2,0,backgroundColor,dim.width/2,dim.height/2,fadeColor);
	            gp = new GradientPaint(0,dim.height/2,backgroundColor,dim.width,dim.height/2,fadeColor);
	            g2.setPaint(gp);
	            g2.fillRect(0,0,dim.width, dim.height);
	            g2.setColor(Color.black);
	        }
	    }
	}    
