/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ColorSchemeSelectionDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.util.awt;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;


/** Supplies option dialog for selection of
 * expression color scheme
 */
public class ColorSchemeSelectionDialog extends javax.swing.JDialog {
    
    /**
     * Creates new form ColorSchemeSelectionDialog
     * @param parent parent Frame
     * @param modal modal selection for dialog
     * @param negImage initial negative gradient image
     * @param posImage initial positive gradient image
     */
    public ColorSchemeSelectionDialog(java.awt.Frame parent, boolean modal, BufferedImage negImage, BufferedImage posImage) {
        super(parent, modal);
        this.setTitle("Color Scheme Selection");
        this.previewer = new PreviewPanel(negImage, posImage);
        initComponents();
        this.negativeColorButton.setFocusPainted(false);
        this.positiveColorButton.setFocusPainted(false);
        
        neutralColorCheckBox = new javax.swing.JCheckBox("Use Black as Neutral Color", true);
        neutralColorCheckBox.setFocusPainted(false);

        if((posImage.getRGB(0,0)) == ((Color.white).getRGB())) { //if neutral is white, then set to neutral = white
            neutralColorCheckBox.setSelected(false);
            neutralColor = Color.white;
        }
        else
            neutralColor = Color.black;
            
        neutralColorCheckBox.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(neutralColorCheckBox.isSelected())
                    neutralColor = Color.black;
                else
                    neutralColor = Color.white;
                previewer.refreshPreview();
            }
        });
        
        this.channelSelectionPanel.add(neutralColorCheckBox, new GridBagConstraints(0,1,2,0,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(0,50,0,0), 0,0));
        this.channelSelectionPanel.validate();
        	  
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
        negativeColorButton = new javax.swing.JRadioButton();
        positiveColorButton = new javax.swing.JRadioButton();
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
        
        negativeColorButton.setSelected(true);
        negativeColorButton.setText("Select Negative Color");
        chanelSelectionGroup.add(negativeColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(negativeColorButton, gridBagConstraints2);
        
        positiveColorButton.setText("Select Positive Color");
        chanelSelectionGroup.add(positiveColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(positiveColorButton, gridBagConstraints2);
        
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
    
    /**
     * Returns the current positive gradient image
     * @return Returns negative color gradient
     */
    public BufferedImage getNegativeGradient(){
        return previewer.getNegativeGradient();
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup chanelSelectionGroup;
    private javax.swing.JPanel channelSelectionPanel;
    private javax.swing.JRadioButton negativeColorButton;
    private javax.swing.JRadioButton positiveColorButton;
    private javax.swing.JPanel actionButtonPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.JPanel gradientPreviewPanel;
    // End of variables declaration//GEN-END:variables
    private Color posColor = Color.red;
    private Color negColor = Color.green;
    private Color neutralColor = Color.black;
    private PreviewPanel previewer;
    private int result = 0;
    private javax.swing.JCheckBox neutralColorCheckBox;
    
    /**
     * Panel which displays the current color scheme gradient
     */
    public class PreviewPanel extends JPanel implements ChangeListener{
        
        BufferedImage currentPosGradient;
        BufferedImage currentNegGradient;
        
        /**
         * Creates a new PreviewPanel
         * @param negImage initial negative gradient image
         * @param posImage initial positive gradient image
         */
        public PreviewPanel(BufferedImage negImage, BufferedImage posImage){
            currentNegGradient = negImage;
            currentPosGradient = posImage;
            posColor = new Color(posImage.getRGB(posImage.getWidth()-1, 0));
            negColor = new Color(negImage.getRGB(0,0));
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
            
            if(positiveColorButton.isSelected()){
                posColor = newColor;
                currentPosGradient = createGradientImage(neutralColor, posColor);
            }
            else if(negativeColorButton.isSelected()){
                negColor = newColor;
                currentNegGradient = createGradientImage(negColor, neutralColor);
            }
            repaint();
        }
        
        /**
         * Refreshes gradients with current color
         */
        public void refreshPreview() {
            currentPosGradient = createGradientImage(neutralColor, posColor);
            currentNegGradient = createGradientImage(negColor, neutralColor);
            repaint();
        }
        /**
         * Paints dialog
         */
        public void paint(Graphics g){
            super.paintComponent(g);
            g.drawImage(currentNegGradient, 0, 0, this.getWidth()/2, this.getHeight(), null);
            g.drawImage(currentPosGradient, this.getWidth()/2, 0, this.getWidth()/2, this.getHeight(), null);
        }
        
        /**
         * Creates a gradient image given specified <CODE>Color</CODE>(s)
         * @param color1 <CODE>Color</CODE> to display at left side of gradient
         * @param color2 <CODE>Color</CODE> to display at right side of gradient
         * @return returns a gradient image
         */
        private BufferedImage createGradientImage(Color color1, Color color2) {
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);       
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
        
        /**
         * Returns the current positive gradient image
         * @return negative gradient image
         */
        public BufferedImage getNegativeGradient(){
            return currentNegGradient;
        }
        
        
    }
    
}
