/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMBorderColorDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:00 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;


/** Supplies option dialog for selection of
 * expression color scheme
 */
public class GDMBorderColorDialog extends JDialog {
   
    /**
     * Creates new form ColorSchemeSelectionDialog
     * @param parent parent Frame
     * @param modal modal selection for dialog
     */
    public GDMBorderColorDialog(Frame parent, boolean modal, Color color) {
        super(parent, modal);
        this.setTitle("Border Color Scheme Selection");
        this.borderColor = color;
        previewer = new PreviewPanel(color);
        initComponents();

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
		
		boolean initState = (borderColor == Color.black) ? true : false;
		
        defaultBlackCheckBox = new javax.swing.JCheckBox("Black - default", initState);
        defaultBlackCheckBox.setFocusPainted(false);
            
        defaultBlackCheckBox.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(defaultBlackCheckBox.isSelected()) {
                    borderColor = Color.black;
					previewer.setColorGradient(previewer.createGradientImage(borderColor, borderColor));
            		repaint();

                } else {
                   	defaultBlackSet = false;
                }
            }
        });
        
        this.defaultBlackPanel.add(defaultBlackCheckBox, new GridBagConstraints(0,1,2,0,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(0,50,0,0), 0,0));
        this.defaultBlackPanel.validate();
       
    }
    
    public Color getBorderColor() {
    	return this.borderColor;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        defaultBlackPanel = new javax.swing.JPanel();    
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
        
        defaultBlackPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(defaultBlackPanel, gridBagConstraints1);
        
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
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel defaultBlackPanel;    
    private javax.swing.JPanel actionButtonPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.JPanel gradientPreviewPanel;
    private javax.swing.JCheckBox defaultBlackCheckBox;
    
    // End of variables declaration//GEN-END:variables
	private Color borderColor;    
    private PreviewPanel previewer;
    private boolean defaultBlackSet;
    private int result = 0;
    
    /**
     * Panel which displays the current color scheme gradient
     */
    public class PreviewPanel extends JPanel implements ChangeListener{
        
        BufferedImage currentColorGradient;
        
        public PreviewPanel(Color color) {
        	borderColor = color;
            currentColorGradient = createGradientImage(borderColor, borderColor);;
            super.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Gradient Preview"));
            setSize(200, 70);
            setPreferredSize(new Dimension(200, 70));
            setVisible(true);
        }
        
        void setColorGradient(BufferedImage image) {
        	currentColorGradient = image;
        }
        
        /**
         * Handles color change events
         */
        public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {

			Color newColor = colorChooser.getColor();

            if(newColor == null || (borderColor == Color.black && defaultBlackSet == true)) return;
            
            if (defaultBlackSet == true) 
            	borderColor = Color.black;
            else             	 
				borderColor = newColor;
				
			setColorGradient(createGradientImage(borderColor, borderColor));
			// currentColorGradient = createGradientImage(borderColor, borderColor);
            repaint();
        }
        
        /**
         * Paints dialog
         */
        public void paint(Graphics g){
            super.paintComponent(g);
            g.drawImage(currentColorGradient, 0, 0, this.getWidth()/2, this.getHeight(), null);
            g.drawImage(currentColorGradient, this.getWidth()/2, 0, this.getWidth()/2, this.getHeight(), null);
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
        
    }
    
}
