/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Progress.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:50 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class Progress extends JDialog {
    
    private JProgressBar progress;
    private JLabel description;
    private static final String DESCRIPTION = "Description: ";
    
    /**
     * Creates a <code>Progress</code> with specified parent, a title and
     * dialog listener.
     */
    public Progress(Frame parent, String title, DialogListener listener) {
        super(parent, title);
        this.description = new JLabel();
        setDescription("");
        this.progress = new JProgressBar();
        this.progress.setStringPainted(true);
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
        super.setVisible(true);
    }
    
    /**
     * Sets progress max value.
     */
    public void setUnits(int units) {
        progress.setMaximum(units);
    }
    
    /**
     * Sets progress value.
     */
    public void setValue(int value) {
        progress.setValue(value);
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
    
    public static void main(String [] args){
        Progress p = new Progress(new Frame(), "Test Progress", null);
        p.show();
     //   System.exit(0);
    }
    
    /**
     * Creates a progress bar panel.
     */
    private JPanel createProgressPanel(JLabel description, JProgressBar progress) {
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
