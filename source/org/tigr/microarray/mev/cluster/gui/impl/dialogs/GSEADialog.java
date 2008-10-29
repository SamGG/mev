package org.tigr.microarray.mev.cluster.gui.impl.dialogs;


/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: AlgorithmDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-02-24 16:07:53 $
 * $Author: wwang67 $
 * $State: Exp $
 */



import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.GSEA.GSEADataPanel;

public class GSEADialog extends JDialog {
    
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton resetButton;
    protected JButton infoButton;
    protected JPanel mainPanel;
    protected JPanel contentPanel;
    protected JPanel buttonPanel;
   
    
    GradientPaint gp;
    Color backgroundColor = new Color(25,25,169);
    Color fadeColor = new Color(140,220,240);
   
    
    
   /** Creates new AlgorithmDialog */
    public GSEADialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
        
        
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        
        buttonPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.setActionCommand("ok-command");
        okButton.setSize(60,30);
        okButton.setPreferredSize(new Dimension(60,30));
        okButton.setFocusPainted(false);
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel-command");
        cancelButton.setSize(60,30);
        cancelButton.setPreferredSize(new Dimension(60,30));
        cancelButton.setFocusPainted(false);
        resetButton = new JButton("Reset");
        resetButton.setActionCommand("reset-command");
        resetButton.setSize(60,30);
        resetButton.setPreferredSize(new Dimension(60,30));
        resetButton.setFocusPainted(false);
        infoButton = new JButton(null, GUIFactory.getIcon("Information24.gif"));
        infoButton.setActionCommand("info-command");
        infoButton.setSize(30,30);
        infoButton.setPreferredSize(new Dimension(30,30));
        infoButton.setFocusPainted(false);
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        infoButton.setBorder(border);
        okButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cancelButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        
        //layout button panel
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(infoButton, new GridBagConstraints(0,0,1,1,0.0,1.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,5), 0,0));
        JLabel label = new JLabel(GUIFactory.getIcon("dialog_button_bar.gif"));
        buttonPanel.add(label, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
        buttonPanel.add(resetButton, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,5,0,5), 0,0));
        buttonPanel.add(cancelButton, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,5), 0,0));
        buttonPanel.add(okButton, new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,30,0,0), 0,0));
       
        
        
       
        mainPanel.add(new HeaderImagePanel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        mainPanel.add(contentPanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        mainPanel.add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(5,0,0,0), 0,0));
        
        
        
        
        this.getContentPane().add(mainPanel);
        pack();
    }
    
    protected void setActionListeners(ActionListener listener){
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        resetButton.addActionListener(listener);
        infoButton.addActionListener(listener);
       }
    
    protected void addContent(Component content){
        contentPanel.add(content, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        validate();
    }
    
    protected void disposeDialog(){
        dispose();
    };
    
    protected void supplantButtonPanel(Component buttonsSet){
        mainPanel.remove(buttonPanel);
        this.mainPanel.add(buttonsSet, new GridBagConstraints(0,3,1,1,1.0,0.0,GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(5,0,0,0), 0,0)); 
        validate();
    }
    
      
    public class HeaderImagePanel extends JPanel{
        
        public HeaderImagePanel(){
        	this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
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
    
    public void setOKButtonText(String label){
        this.okButton.setText(label);
    }
    
    public void setResetButtonText(String label){
    	this.resetButton.setText(label);
    }
    public class FillPanel extends JPanel{
        
        public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            Dimension dim = this.getSize();
            gp = new GradientPaint(0,dim.height/2,backgroundColor,dim.width,dim.height/2,fadeColor);
            g2.setPaint(gp);
            g2.fillRect(0,0,dim.width, dim.height);
            g2.setColor(Color.black);
        }
        
        
        
        
    }
    
    public static void main(String [] args){
    	
        GSEADialog dialog = new GSEADialog(new JFrame(), "", true);
       
      //  GSEADataPanel p = new GSEADataPanel();
        //dialog.addContent(p);
       // dialog.remove(((JFrame)dialog.getParent()).getJMenuBar());
    	
       // dialog.supplantMenuBar();
        dialog.pack();
        dialog.setVisible(true);
      
        System.exit(0);
    }
    
}
