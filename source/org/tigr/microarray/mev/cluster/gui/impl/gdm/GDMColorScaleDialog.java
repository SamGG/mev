/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMColorScaleDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:00 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.FloatMatrix;

public class GDMColorScaleDialog extends AlgorithmDialog {
    
    public static final String OK_CMD = "ok-cmd";
    public static final String CANCEL_CMD = "cancel-cmd";
    public static final String REFRESH_CMD = "refresh-cmd";
    public static final String RESET_CMD = "reset-cmd";
    public static final int REFRESH_RES = 100;
    public static final int RESET_RES = 101;
    
    private int result;
    
    private JLabel lowerJL = new JLabel("Lower Limit [0,1):  ", JLabel.RIGHT);
    private JLabel upperJL = new JLabel("Upper Limit (0,1]: ", JLabel.RIGHT);
    private JLabel saturationJL = new JLabel("% Saturation: ", JLabel.RIGHT);
    
    private JTextField lowerJTF;
    private JTextField upperJTF;
    private JTextField saturationJTF;
    
    private JPanel buttonsPanel;
    private JButton refreshBut;
    private JButton resetBut;
    private JButton cancelBut;
    private JButton okBut;
    private JButton infoButton;
    
    private RangePreviewPanel previewPanel;
    
    private FloatMatrix geneMatrix;
    private int num_genes;
    
    private float origLower;
    private float origUpper;
    
    private GDMScaleListener listener;
    
    public GDMColorScaleDialog(Frame parent, float lower, float upper, FloatMatrix matrix, int genes, Color lowerColor, Color upperColor) {
        
        super(parent, "Set Color Scale", true);
        this.geneMatrix = matrix;
        this.num_genes = genes;
        origLower = lower;
        origUpper = upper;
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel paramsPanel = new JPanel(new GridBagLayout());
        
        paramsPanel.setBackground(Color.white);
        paramsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets.left = 40;
        gbc.insets.right = 10;
        gbc.insets.top = 25;
        lowerJL.setBackground(Color.white);
        lowerJL.setOpaque(true);
        
        lowerJL.setHorizontalAlignment(SwingConstants.RIGHT);
        lowerJL.setHorizontalTextPosition(SwingConstants.RIGHT);
        paramsPanel.add(lowerJL, gbc);
        gbc.insets.top = 10;
        gbc.gridy = 1;
        paramsPanel.add(upperJL, gbc);
        gbc.gridy = 2;
        gbc.insets.bottom = 25;
        paramsPanel.add(saturationJL, gbc);
        gbc.insets.bottom = 0;
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets.left = 10;
        gbc.insets.top = 25;
        Float lowerF = new Float(lower);
        lowerJTF = new JTextField(lowerF.toString(), 10);
        paramsPanel.add(lowerJTF, gbc);
        gbc.gridy = 1;
        gbc.insets.top = 10;
        Float upperF = new Float(upper);
        upperJTF = new JTextField(upperF.toString(), 10);
        paramsPanel.add(upperJTF, gbc);
        
        gbc.gridy = 2;
        gbc.insets.top = 10;
        gbc.insets.bottom = 25;
        Float saturationF = new Float(calcPercentSaturation(lower, upper));
        saturationJTF = new JTextField(saturationF.toString(), 10);
        saturationJTF.setEditable(false);
        paramsPanel.add(saturationJTF, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0,0,0,0);
        
        JPanel mainButtonPanel = new JPanel(new GridBagLayout());
        buttonsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        
        refreshBut = new JButton("Preview");
        refreshBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        refreshBut.setSize(60,30);
        refreshBut.setPreferredSize(new Dimension(60,30));
        refreshBut.setFocusPainted(false);
        refreshBut.setActionCommand(REFRESH_CMD);
        refreshBut.addActionListener(listener);
        buttonsPanel.add(refreshBut);
        
        resetBut = new JButton("Reset");
        resetBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        resetBut.setFocusPainted(false);
        resetBut.setActionCommand(RESET_CMD);
        resetBut.addActionListener(listener);
        
        buttonsPanel.add(resetBut);
        
        cancelBut = new JButton("Cancel");
        cancelBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cancelBut.setSize(60,30);
        cancelBut.setPreferredSize(new Dimension(60,30));
        cancelBut.setFocusPainted(false);
        cancelBut.setActionCommand(CANCEL_CMD);
        cancelBut.addActionListener(listener);
        buttonsPanel.add(cancelBut);
        
        okBut = new JButton("OK");
        okBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(240,240,240), new Color(180,180,180), new Color(10,0,0), new Color(10,10,10) ));
        okBut.setSize(60,30);
        okBut.setPreferredSize(new Dimension(60,30));
        okBut.setFocusPainted(false);
        okBut.setActionCommand(OK_CMD);
        okBut.addActionListener(listener);
        buttonsPanel.add(okBut);
        
        infoButton = new JButton(null, GUIFactory.getIcon("Information24.gif"));
        infoButton.setActionCommand("info-command");
        infoButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        infoButton.setSize(30,30);
        infoButton.setPreferredSize(new Dimension(30,30));
        infoButton.setFocusPainted(false);
        infoButton.addActionListener(listener);
        
        mainButtonPanel.add(infoButton, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        mainButtonPanel.add(buttonsPanel, new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        previewPanel = new RangePreviewPanel(lowerColor, upperColor, lower, upper);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        paramsPanel.add(previewPanel, gbc);
        
        this.addContent(paramsPanel);
        supplantButtonPanel(mainButtonPanel);
        pack();
    }
    
    /**
     * Sets the color scale listener
     */
    public void setGDMScaleListener( GDMScaleListener scaleListener){
        listener = scaleListener;
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        Float lowerF = new Float(lowerJTF.getText());
        Float upperF = new Float(upperJTF.getText());
        Float satF = new Float(calcPercentSaturation(lowerF.floatValue(), upperF.floatValue()));
        saturationJTF.setText(satF.toString());
        GDMColorScaleDialog.this.previewPanel.changeLimits(lowerF.floatValue(), upperF.floatValue());
        show();
        return result;
    }
    
    
    /**
     * Returns Lower Limit value.
     */
    public float getLowerLimit() {
        return Float.parseFloat(lowerJTF.getText());
    }
    
    /**
     * Returns Upper Limit value.
     */
    public float getUpperLimit() {
        return Float.parseFloat(upperJTF.getText());
    }
    
    public float calcPercentSaturation(float lowerf, float upperf) {
        int i, j, out_minmax;
        float dist, sat;
        
        out_minmax = 0;
        for (i=0; i<this.num_genes; i++) {
            for (j=0; j<this.num_genes; j++) {
                dist = geneMatrix.get(i, j);
                if ((dist < lowerf) || (dist > upperf)) {
                    out_minmax++;
                }
            }
        }
        
        sat = ((float)out_minmax / (float)(num_genes*num_genes)) * 100;
        return sat;
    }
    
    /**
     *  Verifies that that values are appropriate.
     */
    private boolean isValidInput(float low, float high){
        if(high <= low){
            JOptionPane.showMessageDialog(GDMColorScaleDialog.this, "The lower limit must be less than or equal to the upper limit.  Please enter new limits.", "Invalid Range Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if(high > 1.0){
            JOptionPane.showMessageDialog(GDMColorScaleDialog.this, "The upper limit must be equal to or less than 1.0. Please enter a new value.", "Invalid Range Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if(low < 0.0){
            JOptionPane.showMessageDialog(GDMColorScaleDialog.this, "The lower limit must be equal to or greater than 0.0. Please enter a new value.", "Invalid Range Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    public static void main(String [] args){
        FloatMatrix matrix = new FloatMatrix(3,3);
        matrix.set(0,0,1.0f );
        matrix.set(1,0,0.10f);
        matrix.set(2,0,0.80f);
        
        matrix.set(0,1,1.0f );
        matrix.set(1,1,0.134f);
        matrix.set(2,1,0.25f);
        
        matrix.set(0,2,0.430f );
        matrix.set(1,2,0.370f);
        matrix.set(2,2,0.04f);
        
        GDMColorScaleDialog dialog = new GDMColorScaleDialog(new Frame(), 0.0f, 0.1f, matrix, matrix.getRowDimension(), Color.black, Color.red);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.showModal();
    }
    
    
    /**
     *  The Range Preview Panel displays the current color range gradient.
     */
    private class RangePreviewPanel extends JPanel{
        int GRADIENT_HEIGHT = 40;
        
        Color lowerColor;
        Color upperColor;
        float lower;
        float upper;
        JPanel lowerPanel;
        JPanel upperPanel;
        GradientPanel midPanel;
        
        JPanel previewPanel;
        ScalePanel gradientPanel;
        
        /** Constructs a new RangePreviewPanel */
        public RangePreviewPanel(Color lowerColor, Color upperColor, float lower, float upper){
            this.lower = lower;
            this.upper = upper;
            this.lowerColor = lowerColor;
            this.upperColor = upperColor;
            this.setBackground(Color.white);
            lowerPanel = new JPanel();
            lowerPanel.setBackground(lowerColor);
            Dimension prefSize = new Dimension(10, 40);
            lowerPanel.setPreferredSize(prefSize);
            upperPanel = new JPanel();
            upperPanel.setBackground(upperColor);
            upperPanel.setPreferredSize(prefSize);
            
            midPanel = new GradientPanel();
            midPanel.setPreferredSize(prefSize);
            previewPanel = new JPanel(new GridBagLayout());
            previewPanel.setBorder(BorderFactory.createTitledBorder("Effective Range Preview"));
            previewPanel.setBackground(Color.white);
            
            gradientPanel = new ScalePanel();
            gradientPanel.setLayout(new GridBagLayout());
            gradientPanel.setBackground(Color.white);
            gradientPanel.add(lowerPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,20,0),0,0));
            gradientPanel.add(midPanel, new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,20,0),0,0));
            gradientPanel.add(upperPanel, new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,20,10),0,0));
            
            previewPanel.add(gradientPanel, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            this.setLayout(new GridBagLayout());
            add(previewPanel, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        }
        
        
        public void changeLimits(float low, float high){
            this.lower = low;
            this.upper = high;
            int w = gradientPanel.getWidth()-20;
            int h = lowerPanel.getHeight();
            lowerPanel.setSize((int)(w*(low)), h);
            lowerPanel.setPreferredSize(new Dimension((int)(w*(low)), h));
            upperPanel.setSize((int)(w*(1.0 - high)), h);
            upperPanel.setPreferredSize(new Dimension((int)(w*(1.0-high)), h));
            midPanel.setSize((int)(w*(high-low)), h);
            midPanel.setPreferredSize(new Dimension((int)(w*(high-low)), h));
            validate();
            gradientPanel.repaint();
        }
        
        /** The GradientPanel class displays the color gradient */
        private class GradientPanel extends JPanel{
            GradientPaint gp;
            
            public void paint(Graphics g){
                super.paint(g);
                Graphics2D g2 = (Graphics2D)g;
                Dimension dim = this.getSize();
                gp = new GradientPaint(0,dim.height/2, lowerColor,dim.width,dim.height/2,upperColor);
                g2.setPaint(gp);
                g2.fillRect(0,0,dim.width, dim.height);
                g2.setColor(Color.black);
            }
            
        }
        
        /** The ScalePanel class encapsulates the gradient panel and augments by drawing the
         * limit boundries and the scale below the gradient image
         */
        private class ScalePanel extends JPanel{
            int [] x;
            int [] y;
            
            public ScalePanel(){
                super();
                Font font = getFont();
                setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
                x = new int[3];
                y = new int[3];
            }
            
            /** Repaints based on current limits */
            public void paint(Graphics g){
                super.paint(g);
                int h = this.getHeight();
                int w = this.getWidth()- 20;
                int gradientH = this.getHeight() - 20;
                int xLower = (int)((lower)*w) + 10;
                int xUpper = (int)((upper)*w) + 10;
                x[0] = xLower;
                x[1] = xLower - 5;
                x[2] = xLower + 5;
                
                y[0] = 10;
                y[1] = 1;
                y[2] = 1;
                
                g.setColor(Color.blue);
                g.fillPolygon(x, y, 3);
                
                x[0] = xUpper;
                x[1] = xUpper - 5;
                x[2] = xUpper + 5;
                
                g.fillPolygon(x, y, 3);
                
                g.setColor(upperColor);
                g.fillRect(xLower-1, 10, 3, midPanel.getHeight());
                g.setColor(lowerColor);
                g.fillRect(xUpper-1, 10, 3, midPanel.getHeight());
                g.setColor(Color.black);
                FontMetrics fm = g.getFontMetrics();
                g.drawString("0.0", 10, midPanel.getHeight() + 10 + fm.getHeight());
                g.drawString("0.5", ((getWidth())/2 - fm.stringWidth("0.5")/2), midPanel.getHeight() + 10 + fm.getHeight());
                g.drawString("1.0", getWidth()- 10 - fm.stringWidth("1.0"), midPanel.getHeight() + 10 + fm.getHeight());
                
            }
            
        }
        
        
    }
    
    /**
     * Handles dialog events
     */
    private class Listener extends DialogListener {
        
        Float lowerF;
        Float upperF;
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(OK_CMD)) {
                try {
                    lowerF = new Float(lowerJTF.getText());
                    upperF = new Float(upperJTF.getText());
                } catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(GDMColorScaleDialog.this, "The entered values are not valid numberical entries. Please enter new values", "Invalid Range Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if(!isValidInput(lowerF.floatValue(), upperF.floatValue()))
                    return;
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals(CANCEL_CMD)) {
                result = JOptionPane.CANCEL_OPTION;
                GDMColorScaleDialog.this.lowerJTF.setText(String.valueOf(origLower));
                GDMColorScaleDialog.this.upperJTF.setText(String.valueOf(origUpper));
                lowerF = new Float(lowerJTF.getText());
                upperF = new Float(upperJTF.getText());
                Float satF = new Float(calcPercentSaturation(lowerF.floatValue(), upperF.floatValue()));
                saturationJTF.setText(satF.toString());
                dispose();
            } else if (command.equals(RESET_CMD)) {
                result = RESET_RES;
                GDMColorScaleDialog.this.lowerJTF.setText(String.valueOf(origLower));
                GDMColorScaleDialog.this.upperJTF.setText(String.valueOf(origUpper));
                lowerF = new Float(lowerJTF.getText());
                upperF = new Float(upperJTF.getText());
                Float satF = new Float(calcPercentSaturation(lowerF.floatValue(), upperF.floatValue()));
                saturationJTF.setText(satF.toString());
                GDMColorScaleDialog.this.previewPanel.changeLimits(lowerF.floatValue(), upperF.floatValue());
            } else if (command.equals(REFRESH_CMD)) {
                try{
                    lowerF = new Float(lowerJTF.getText());
                    upperF = new Float(upperJTF.getText());
                } catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(GDMColorScaleDialog.this, "The entered values are not valid numberical entries. Please enter new values", "Invalid Range Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if(!isValidInput(lowerF.floatValue(), upperF.floatValue()))
                    return;
                Float satF = new Float(calcPercentSaturation(lowerF.floatValue(), upperF.floatValue()));
                saturationJTF.setText(satF.toString());
                GDMColorScaleDialog.this.previewPanel.changeLimits(lowerF.floatValue(), upperF.floatValue());
                if(listener != null){
                    listener.scaleChanged(lowerF.floatValue(), upperF.floatValue());
                }
            } else if(command.equals("info-command")){
                HelpWindow hw = new HelpWindow(GDMColorScaleDialog.this, "GDM Color Range Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }                 
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
}
