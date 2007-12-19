
/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AccessibleColorSchemeSelectionDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2007-12-19 21:47:04 $
 * $Author: saritanair $
 * $State: Exp $
 */

package org.tigr.microarray.util.awt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeListener;


/** Supplies option dialog for selection of
 * expression color scheme
 */
public class AccessibleColorSchemeSelectionDialog extends JDialog {
        
    private ButtonGroup chanelSelectionGroup;
    private JPanel channelSelectionPanel;
    private JRadioButton lowEndColorButton;
    private JRadioButton highEndColorButton;
    private JPanel actionButtonPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JColorChooser colorChooser;
    private JPanel gradientPreviewPanel;
    private AccessibleColorPalette cbp=new AccessibleColorPalette();
    private Color posColor=cbp.yellow; 
    private Color negColor=cbp.orange; 
    private Color neutralColor=cbp.black; 
    private PreviewPanel previewer;
    private JCheckBox neutralColorCheckBox;

    private JRadioButton doubleGradientButton, singleGradientButton;
    
    private boolean useDoubleGradient;
    
    private int result = 0;
  
    	
    /**
     * Creates new form ColorSchemeSelectionDialog
     * @param parent parent Frame
     * @param modal modal selection for dialog
     * @param negImage initial negative gradient image
     * @param posImage initial positive gradient image
     */
    public AccessibleColorSchemeSelectionDialog(java.awt.Frame parent, boolean modal, BufferedImage negImage, BufferedImage posImage, boolean useDouble) {
        super(parent, modal);
        this.useDoubleGradient = useDouble;
        this.setTitle("Color Scheme Selection");
        this.previewer = new PreviewPanel(negImage, posImage);
        initComponents();
        this.lowEndColorButton.setFocusPainted(false);
        this.highEndColorButton.setFocusPainted(false);
        
        neutralColorCheckBox = new javax.swing.JCheckBox("Use Black as Neutral Color", true);
        neutralColorCheckBox.setFocusPainted(false);
        neutralColorCheckBox.setOpaque(false);
        neutralColorCheckBox.setEnabled(useDouble);
        
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
                previewer.alterNeutralColor();
            }
        });
        
        this.channelSelectionPanel.add(neutralColorCheckBox, new GridBagConstraints(0,1,2,0,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(0,50,0,0), 0,0));
        this.channelSelectionPanel.validate();
        
        //this.colorChooser.removeAll();
        
       AbstractColorChooserPanel[]panel=this.colorChooser.getChooserPanels();
       for(int i=0;i<panel.length;i++) {
    	   if(!panel[i].equals(cbp)) {
    		   this.colorChooser.removeChooserPanel(panel[i]);
    	   }
       }
        this.colorChooser.addChooserPanel(cbp);
        this.colorChooser.setVisible(true);
        this.colorChooser.setPreviewPanel(cbp);
        this.gradientPreviewPanel.add(this.previewer, BorderLayout.CENTER);
        this.colorChooser.getSelectionModel().addChangeListener(this.previewer);
        //setSize(450, 465);
       
        pack();
        
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

    	Listener listener = new Listener();
    	
        ButtonGroup bg = new ButtonGroup();
		doubleGradientButton = new JRadioButton("Double Gradient", useDoubleGradient);
		doubleGradientButton.setActionCommand("change-gradient-command");
		doubleGradientButton.addActionListener(listener);
		doubleGradientButton.setFocusPainted(false);
		doubleGradientButton.setOpaque(false);
		bg.add(doubleGradientButton);
		
		singleGradientButton = new JRadioButton("Single Gradient", !useDoubleGradient);
		singleGradientButton.setActionCommand("change-gradient-command");
		singleGradientButton.addActionListener(listener);
		singleGradientButton.setFocusPainted(false);
		singleGradientButton.setOpaque(false);
		bg.add(singleGradientButton);
		
    	JPanel gradientStylePanel = new JPanel(new GridBagLayout());
    	gradientStylePanel.setBackground(Color.white);
        gradientStylePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Gradient Style"));

		gradientStylePanel.add(doubleGradientButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,20),0,0));
		gradientStylePanel.add(singleGradientButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,20,5,0),0,0));		
				
    	chanelSelectionGroup = new javax.swing.ButtonGroup();
        channelSelectionPanel = new javax.swing.JPanel();
        channelSelectionPanel.setBackground(Color.white);
        channelSelectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Gradient Selection"));
        
        lowEndColorButton = new javax.swing.JRadioButton();
        highEndColorButton = new javax.swing.JRadioButton();
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
        
        lowEndColorButton.setSelected(true);
        lowEndColorButton.setText("Select Low End Color");
        lowEndColorButton.setOpaque(false);
        chanelSelectionGroup.add(lowEndColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(lowEndColorButton, gridBagConstraints2);
        
        highEndColorButton.setText("Select High End Color");
        highEndColorButton.setOpaque(false);
        chanelSelectionGroup.add(highEndColorButton);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        channelSelectionPanel.add(highEndColorButton, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.gridheight = 1;        
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;        
        getContentPane().add(gradientStylePanel, gridBagConstraints1);
                
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
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(actionButtonPanel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(channelSelectionPanel, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;      
        getContentPane().add(colorChooser, gridBagConstraints1);
        
        gradientPreviewPanel.setLayout(new java.awt.BorderLayout());
        
        gradientPreviewPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Gradient Preview"));
        gradientPreviewPanel.setPreferredSize(new java.awt.Dimension(200, 70));
        gradientPreviewPanel.setMinimumSize(new java.awt.Dimension(200, 70));
        gradientPreviewPanel.setBackground(Color.white);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
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
    
    public boolean getUseDoubleGradient() {
    	return doubleGradientButton.isSelected();
    }
    
    public BufferedImage getPosImage() {
    	return previewer.getPositiveGradient();    
    }
    
    
    
    
    /**
     * Panel which displays the current color scheme gradient
     */
    public class PreviewPanel extends JPanel implements ChangeListener {
        
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
            System.out.println("new Color selected:"+newColor);
            if(newColor == null) return;
            
            if(highEndColorButton.isSelected()){
	                posColor = newColor;
	                if(useDoubleGradient)
	                	currentPosGradient = createGradientImage(neutralColor, posColor);
	                else
	                	currentPosGradient = createGradientImage(negColor, posColor);
            }
	            else if(lowEndColorButton.isSelected()){
	                negColor = newColor;
	                if(useDoubleGradient)
	                	currentNegGradient = createGradientImage(negColor, neutralColor);
	                else  //single gradient, modify positive gradient only
	                	currentPosGradient = createGradientImage(negColor, posColor);	                	
	            }           
            repaint();
        }
        
        public void alterNeutralColor() {
            if(useDoubleGradient) {
            	currentPosGradient = createGradientImage(neutralColor, posColor);
            	currentNegGradient = createGradientImage(negColor, neutralColor);
                repaint();       
            }
        }
        
    	public void onSwitchGradientStyle() {
    		if(useDoubleGradient)
    			refreshPreview();
    		else { //modify for single gradient
            	currentPosGradient = createGradientImage(negColor, posColor);    			
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
            if(useDoubleGradient) {
            	g.drawImage(currentNegGradient, 0, 0, this.getWidth()/2, this.getHeight(), null);
            	g.drawImage(currentPosGradient, this.getWidth()/2, 0, this.getWidth()/2, this.getHeight(), null);
            } else {
            	g.drawImage(currentPosGradient, 0, 0, this.getWidth(), this.getHeight(), null);            	            
            }
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
        	 return this.currentPosGradient;
        }
        
        /**
         * Returns the current positive gradient image
         * @return negative gradient image
         */
        public BufferedImage getNegativeGradient(){
            return this.currentNegGradient;
        }
        
    }
    
    public class Listener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("change-gradient-command")) {
				useDoubleGradient = doubleGradientButton.isSelected();
				neutralColorCheckBox.setEnabled(useDoubleGradient);
				previewer.onSwitchGradientStyle();				
			}
			
		}
    	
    
    }
    
}
