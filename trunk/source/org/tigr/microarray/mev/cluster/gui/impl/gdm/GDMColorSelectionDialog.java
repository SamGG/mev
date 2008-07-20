/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMColorSelectionDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:00 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;


/** Supplies option dialog for selection of
 * expression color scheme
 */
public class GDMColorSelectionDialog extends javax.swing.JDialog {
    
    /**
     * Creates new form GDMColorSelectionDialog
     * @param parent parent Frame
     * @param modal modal selection for dialog
     * @param posImage initial positive gradient image
     */
    public GDMColorSelectionDialog(java.awt.Frame parent, boolean modal, BufferedImage posImage) {

        super(parent, modal);
        this.setTitle("Gene Distance Matrix Color Scheme Selection");
        previewer = new PreviewPanel(posImage);

        initComponents();
        this.maxColorButton.setFocusPainted(false);
	this.minColorButton.setFocusPainted(false);
        this.colorChooser.setPreviewPanel(new JPanel());
        this.gradientPreviewPanel.add(this.previewer, BorderLayout.CENTER);
        this.colorChooser.getSelectionModel().addChangeListener(previewer);
        setSize(450, 465);
        
        this.okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                result = JOptionPane.OK_OPTION;
                setVisible(false);
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                result = JOptionPane.CANCEL_OPTION;
                setVisible(false);
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        chanelSelectionGroup = new javax.swing.ButtonGroup();
        channelSelectionPanel = new javax.swing.JPanel();
        minColorButton = new javax.swing.JRadioButton();
        maxColorButton = new javax.swing.JRadioButton();
        actionButtonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        colorChooser = new javax.swing.JColorChooser();
        this.colorChooser.setPreviewPanel(previewer);
        gradientPreviewPanel = new javax.swing.JPanel();
        
        
        getContentPane().setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        channelSelectionPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        minColorButton.setSelected(true);
        minColorButton.setText("Select Minimum Distance Color");
        chanelSelectionGroup.add(minColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(minColorButton, gridBagConstraints2);
        
        maxColorButton.setText("Select Maximum Distance Color");
        chanelSelectionGroup.add(maxColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(maxColorButton, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(channelSelectionPanel, gridBagConstraints1);
        
        actionButtonPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        okButton.setText(" Apply Color Scheme");
        okButton.setFocusPainted(false);
        okButton.setSelected(true);
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.insets = new java.awt.Insets(10, 0, 10, 10);
        actionButtonPanel.add(okButton, gridBagConstraints3);
        
        cancelButton.setText("Cancel");
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.insets = new java.awt.Insets(10, 10, 10, 0);
        actionButtonPanel.add(cancelButton, gridBagConstraints3);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(actionButtonPanel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(colorChooser, gridBagConstraints1);
        
        gradientPreviewPanel.setLayout(new java.awt.BorderLayout());
        
        gradientPreviewPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Gradient Preview"));
        gradientPreviewPanel.setPreferredSize(new java.awt.Dimension(200, 70));
        gradientPreviewPanel.setMinimumSize(new java.awt.Dimension(200, 70));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(gradientPreviewPanel, gridBagConstraints1);
        
        pack();
    }//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /**
     * Returns the current positive gradient image
     * @return Returns positive color gradient
     */
    public BufferedImage getPositiveGradient(){
        return previewer.getPositiveGradient();
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup chanelSelectionGroup;
    private javax.swing.JPanel channelSelectionPanel;
    private javax.swing.JRadioButton minColorButton;
    private javax.swing.JRadioButton maxColorButton;
    private javax.swing.JPanel actionButtonPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.JPanel gradientPreviewPanel;
    // End of variables declaration//GEN-END:variables
    private Color maxColor = Color.red;
    private Color minColor = Color.black;
    private PreviewPanel previewer;
    private int result = 0;
    
    /**
     * Panel which displays the current color scheme gradient
     */
    public class PreviewPanel extends JPanel implements ChangeListener{
        
        BufferedImage currentPosGradient;
        
        /**
         * Creates a new PreviewPanel
         * @param posImage initial positive gradient image
         */
        public PreviewPanel(BufferedImage posImage){

            currentPosGradient = posImage;
            maxColor = new Color(posImage.getRGB(posImage.getWidth()-1, 0));
            minColor = new Color(posImage.getRGB(0,0));
            
            super.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Gradient Preview"));
            setSize(200, 70);
            setPreferredSize(new Dimension(200, 70));
            setVisible(true);
        }
        
        /**
         * Handles color change events
         */
        public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
            Color newColor = colorChooser.getColor();
            if(newColor == null) return;
            
            if(maxColorButton.isSelected()){
                maxColor = newColor;
            }
            else if(minColorButton.isSelected()){
                minColor = newColor;
            }
			setPositiveGradient(createGradientImage(minColor, maxColor));
            
            repaint();
        }
        
        /**
         * Refreshes gradients with current color
         */
        public void refreshPreview() {
			setPositiveGradient(createGradientImage(minColor, maxColor));
            repaint();
        }
        /**
         * Paints dialog
         */
        public void paint(Graphics g){
            super.paintComponent(g);
            g.drawImage(currentPosGradient, 0, 0, this.getWidth(), this.getHeight(), null);
        }
        
        /**
         * Creates a gradient image given specified <CODE>Color</CODE>(s)
         * @param color1 <CODE>Color</CODE> to display at left side of gradient
         * @param color2 <CODE>Color</CODE> to display at right side of gradient
         * @return returns a gradient image
         */
        private BufferedImage createGradientImage(Color color1, Color color2) {
            BufferedImage image = new BufferedImage(256, 1, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = image.createGraphics();
            GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
            graphics.setPaint(gp);
            graphics.drawRect(0, 0, 255, 1);
            return image;
        }
        
        /**
         * Returns the current positive gradient image
         * @return Returned positive gradient
         */
        public BufferedImage getPositiveGradient(){
            return currentPosGradient;
        }

        public void setPositiveGradient(BufferedImage gradient){
            currentPosGradient = gradient;
        }
                
    }
    
}
