/*
 * Copyright @ 1999-2005, The Institute for Genomic Research (TIGR). All rights
 * reserved.
 */

/*
 * $RCSfile: SetRatioScaleDialog.java,v $ $Revision: 1.3 $ $Date: 2005/02/10
 * 16:06:10 $ $Author: braistedj $ $State: Exp $
 */
package org.tigr.microarray.mev;


import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FontMetrics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame; 
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


/**
 * Permits the alteration of color scale value range for rendering data 
 * as color extracted from a gradient.  Upper and Lower values represent the
 * values beyond which the color representation is staturated. (beyond color range)
 * Values within the range will be represented as a color from the gradient while
 * values outside the bounds of the range will be represented by the end colors of 
 * the gradient or gradients. 
 */

public class SetRatioScaleDialog extends AlgorithmDialog {
	
	private int result;
	private boolean doubleGradient;
	private boolean origDoubleGradient;
	private BufferedImage origPosImage, origNegImage;
	private JTextField upperTextField, lowerTextField;
	private JRadioButton singleGradientButton, doubleGradientButton;
	

	private float currMinValue, currMaxValue;
	private float origUpper, origLower;
	private String formattedUpper, formattedLower;

	private StatsPanel statsPanel;
	private GradientPreviewPanel gradientPreviewPanel;
	private IFramework framework;
	private MultipleArrayMenubar menubar;
	
	private DecimalFormat twoDecimalFormat;	

	
	/**
	 * Constructs a new SetRatioScaleDialog
	 * 
	 * @param parent parent frame
	 * @param framework permits interaction with MultipleArrayViewer and data
	 * @param menubar permits setting of new limits on update
	 * @param upper current upper limit
	 * @param lower current lower limit
	 * @param doubleGradient true if the gradient is a double gradient
	 */
	public SetRatioScaleDialog(JFrame parent, IFramework framework, MultipleArrayMenubar menubar, float upper, float lower, boolean doubleGradient) {
		super(parent, "Set Ratio Scale", true);
		
		EventListener listener = new EventListener();
		
		Experiment experiment = framework.getData().getFullExperiment();
		FloatMatrix matrix = experiment.getMatrix();
		float [] extreems = experiment.getMinAndMax();
		float minValue = extreems[0];
		float maxValue = extreems[1];
		this.menubar = menubar;
		this.doubleGradient = doubleGradient;
		this.origDoubleGradient = doubleGradient;
		
		this.framework = framework;
		origLower = lower;
		origUpper = upper;
		origNegImage = framework.getDisplayMenu().getNegativeGradientImage();
		origPosImage = framework.getDisplayMenu().getPositiveGradientImage();

		twoDecimalFormat = new DecimalFormat();
		twoDecimalFormat.setMaximumFractionDigits(2);
		twoDecimalFormat.setMinimumFractionDigits(2);
		
		formattedUpper = twoDecimalFormat.format(origUpper);
		formattedLower = twoDecimalFormat.format(origLower);
		
		ButtonGroup bg = new ButtonGroup();
		
		doubleGradientButton = new JRadioButton("Double Gradient  (Suitable for log2Ratio Data)", doubleGradient);
		doubleGradientButton.setActionCommand("change-gradient-command");
		doubleGradientButton.addActionListener(listener);
		doubleGradientButton.setFocusPainted(false);
		doubleGradientButton.setOpaque(false);
		bg.add(doubleGradientButton);
		
		singleGradientButton = new JRadioButton("Single Gradient  (Suitable for Absolute Intensity Values)", !doubleGradient);
		singleGradientButton.setActionCommand("change-gradient-command");
		singleGradientButton.addActionListener(listener);
		singleGradientButton.setFocusPainted(false);
		singleGradientButton.setOpaque(false);
		bg.add(singleGradientButton);
		
		ParameterPanel gradientPanel = new ParameterPanel("Gradient Style");
		gradientPanel.setLayout(new GridBagLayout());		
		gradientPanel.add(doubleGradientButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0),0,0));
		gradientPanel.add(singleGradientButton, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));		
				
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		JLabel upperLabel = new JLabel("Upper Color Limit ( max. data value = " + format.format(maxValue) + " ): ");
		upperLabel.addKeyListener(listener);
		
		upperTextField = new JTextField(5);
		upperTextField.addKeyListener(listener);
		upperTextField.setText("" + upper);
		
		JLabel lowerLabel = new JLabel("Lower Color Limit ( min. data value = " + format.format(minValue) + " ): ");
		lowerLabel.addKeyListener(listener);
		
		lowerTextField = new JTextField(5);
		lowerTextField.addKeyListener(listener);
		lowerTextField.setText("" + lower);

		JButton updateLimitsButton = new JButton("Update Limits");
		updateLimitsButton.setPreferredSize(new Dimension(130,30));
		updateLimitsButton.setFocusPainted(false);
		updateLimitsButton.setActionCommand("update-command");
		updateLimitsButton.addActionListener(listener);
		
		ParameterPanel rangePanel = new ParameterPanel("Color Range Selection");
		rangePanel.setLayout(new GridBagLayout());
		rangePanel.add(lowerLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,10,0),0,0));
		rangePanel.add(lowerTextField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,10,0),0,0));
		rangePanel.add(upperLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,25,0),0,0));
		rangePanel.add(upperTextField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,25,0),0,0));
		rangePanel.add(updateLimitsButton, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,20,0),0,0));

		statsPanel = new StatsPanel(framework);
			
		ParameterPanel preview = new ParameterPanel("Gradient and Limits Preview");
		preview.setLayout(new GridBagLayout());		
		gradientPreviewPanel = new GradientPreviewPanel(framework);
	    preview.add(gradientPreviewPanel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0,0));		
   
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBackground(Color.white);
		
		mainPanel.add(gradientPanel,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		mainPanel.add(rangePanel,new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		mainPanel.add(statsPanel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		mainPanel.add(preview, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		
		addContent(mainPanel);
		addWindowListener(listener);
		setActionListeners(listener);
		
		pack();
		setResizable(false);
		upperTextField.grabFocus();
	}
	
	/**
	 * Displays the dialog, centered in scree
	 * 
	 * @return returns the exit state, OK or CANCEL
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}
	
	/**
	 * Returns the upper ratio limit
	 * @return upper limit
	 */
	public float getUpperRatio() {
		return Float.parseFloat(upperTextField.getText());
	}
		
	/**
	 * Returns the lower limit
	 * @return lower limit
	 */
	public float getLowerRatio() {
		return Float.parseFloat(lowerTextField.getText());
	}
	
	/**
	 * Returns true if the gradient style is double
	 * @return true if using double gradient style, false if single gradient
	 */
	public boolean getUseDoubleGradient() {
		return doubleGradientButton.isSelected();
	}
	
	public boolean isGradientStyleAltered() {
		return (origDoubleGradient == doubleGradient);	
	}
	
	public BufferedImage getPosImage() {
			return gradientPreviewPanel.getPosImage();
	}
	
	/**
	 *  Validates the values then updates the stats panel and the preview panel
	 */
	private void updateLimits() {
		if(validateValues()) {
			currMinValue = Float.parseFloat(lowerTextField.getText());
			currMaxValue = Float.parseFloat(upperTextField.getText());
			formattedLower = twoDecimalFormat.format(currMinValue);
			formattedUpper = twoDecimalFormat.format(currMaxValue);
			statsPanel.updateStats(currMinValue, currMaxValue);
			menubar.setUseDoubleGradient(getUseDoubleGradient());
			menubar.setMaxRatioScale(currMaxValue);
			menubar.setMinRatioScale(currMinValue);
			framework.refreshCurrentViewer();
			repaint();
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
	 * Validates the entered values.
	 * Double gradient => lower<0 && upper>0 
	 * single gradient => lower<upper
	 * @return returns true if the values are valid, else false
	 */
	private boolean validateValues() {
		int progress = 0;
		boolean valid = true;
		float newLower, newUpper;
		try {
			newLower = Float.parseFloat(this.lowerTextField.getText());
			progress++;
			newUpper = Float.parseFloat(this.upperTextField.getText());
			progress++;
			if(this.doubleGradient) {
				if(newLower >= 0 || newUpper <=0)
					valid = false;
			} else {  //single gradient
				if(newLower >= newUpper)
					valid = false;
			}
		} catch (NumberFormatException nfe) {
			valid = false;
			JOptionPane.showMessageDialog(this, "Input Error, Values cannot be parsed as floating point numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);			
		}
		
		if(!valid) {
			if(doubleGradient) {
				JOptionPane.showMessageDialog(this, "Input Error (Double Gradient), Value Limits:  floating point number, lower limit < 0, upper limit > 0", "Input Error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Input Error (Single Gradient), Value Limits:  floating point number, lower limit < upper limit", "Input Error", JOptionPane.ERROR_MESSAGE);			
			}
		}			
		return valid;
	}

	
	/**
	 * Resets the text fields to original values, updates stats, repaints preview
	 */
	private void resetControls() {
		upperTextField.setText(String.valueOf(origUpper));
		lowerTextField.setText(String.valueOf(origLower));
		formattedUpper = twoDecimalFormat.format(origUpper);		
		formattedLower = twoDecimalFormat.format(origLower);
		statsPanel.updateStats(origLower, origUpper);
		repaint();
	}
	
	
	
	
	/**
	 * @author braisted
	 *
	 * Preview class to display the current gradient type and limits.
	 */
	private class GradientPreviewPanel extends JPanel {
		
		private BufferedImage negImage, posImage;
		private String centerPointStr;
		private int TIC_HEIGHT = 35;
		private int GRAD_HEIGHT = 30;
		private int TEXT_BUFFER = 20;
		private Insets insets;
		
		/**
		 * Constructs a new GradientPreviewPanel
		 * @param framework IFramework implementatin to provide display menu
		 */
		public GradientPreviewPanel(IFramework framework) {
			super();					
			insets = new Insets(5,5,5,5);
			centerPointStr = "0.0";
			negImage = framework.getDisplayMenu().getNegativeGradientImage();
			posImage = framework.getDisplayMenu().getPositiveGradientImage();	
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));
			super.setPreferredSize(new Dimension(negImage.getWidth()+posImage.getWidth()+insets.left+insets.right, GRAD_HEIGHT+insets.top+insets.bottom+TEXT_BUFFER));						
		}

		
		/* (non-Javadoc)
		 * @see java.awt.Component#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			super.paint(g);
			
			FontMetrics fm = g.getFontMetrics();
			int totalWidth = negImage.getWidth()+posImage.getWidth();

			if(doubleGradient) {

				g.drawImage(negImage, insets.left, insets.top, negImage.getWidth(), GRAD_HEIGHT, null);
				g.drawImage(posImage, insets.left+negImage.getWidth()+1, insets.top, posImage.getWidth(), GRAD_HEIGHT, null);

				//draw center tic
				g.drawLine(insets.left+negImage.getWidth(), insets.top, insets.left+negImage.getWidth(), insets.top+TIC_HEIGHT);
				g.drawLine(insets.left+negImage.getWidth()-1, insets.top, insets.left+negImage.getWidth()-1, insets.top+TIC_HEIGHT);
				g.drawLine(insets.left+negImage.getWidth()+1, insets.top, insets.left+negImage.getWidth()+1, insets.top+TIC_HEIGHT);												
			
				//draw center point string
				g.drawString(centerPointStr, insets.left+negImage.getWidth()-(int)(fm.stringWidth(centerPointStr)/2f), insets.top+TIC_HEIGHT+fm.getHeight());
			} else {				
				g.drawImage(posImage, insets.left, insets.top, totalWidth, GRAD_HEIGHT, null);				
			}

			//draw left tic
			g.drawLine(insets.left, insets.top, insets.left, insets.top+TIC_HEIGHT);
			g.drawLine(insets.left+1, insets.top, insets.left+1, insets.top+TIC_HEIGHT);
			g.drawLine(insets.left+2, insets.top, insets.left+2, insets.top+TIC_HEIGHT);				

			//draw right tic
			g.drawLine(insets.left+totalWidth, insets.top, insets.left+totalWidth, insets.top+TIC_HEIGHT);
			g.drawLine(insets.left+totalWidth-1, insets.top, insets.left+totalWidth-1, insets.top+TIC_HEIGHT);
			g.drawLine(insets.left+totalWidth-2, insets.top, insets.left+totalWidth-2, insets.top+TIC_HEIGHT);				
							
			//put limits on axis			
			g.drawString(formattedLower, insets.left, insets.top+TIC_HEIGHT+fm.getHeight());
			g.drawString(formattedUpper, insets.left+totalWidth-fm.stringWidth(formattedUpper), insets.top+TIC_HEIGHT+fm.getHeight());							

		}
		
		public void setPosImage(BufferedImage posBI) {
			posImage = posBI;
			repaint();
		}
		
		public BufferedImage getPosImage() {
			return posImage;
		}
		
		public BufferedImage getNegImage() {
			return negImage;		
		}		
	}
	

/**
 * @author braisted
 *
 * Presents staturation statistics for the current limits
 */
public class StatsPanel extends ParameterPanel {

	private JTextField offScaleField, lowEndField, highEndField;
	private JTextField offScalePercentField, lowEndPercentField, highEndPercentField;
	private float [] sortedValues;
	private DecimalFormat format;
		
	/** 
	 * Constructs a new StatsPanel
	 * @param framework
	 */
	public StatsPanel(IFramework framework) {
		super("Range and Saturation Statistics");
		setLayout(new GridBagLayout());
		format = new DecimalFormat();
		format.setMaximumIntegerDigits(3);
		format.setMaximumFractionDigits(1);
		format.setMinimumFractionDigits(1);
		
		JLabel numberLabel = new JLabel("Number");
		numberLabel.setOpaque(false);
		
		JLabel percentLabel = new JLabel("Percent");
		percentLabel.setOpaque(false);
		
		JLabel offScaleLabel = new JLabel("Elements Off Color Scale");
		JLabel lowEndLabel = new JLabel("Elements Below Low End");
		JLabel highEndLabel = new JLabel("Elements Above High End");

		offScaleField = new JTextField(5);
		offScaleField.setBackground(Color.lightGray);
		offScaleField.setEditable(false);
		offScaleField.setHorizontalAlignment(JTextField.RIGHT);
		
		lowEndField = new JTextField(5);
		lowEndField.setBackground(Color.lightGray);
		lowEndField.setEditable(false);
		lowEndField.setHorizontalAlignment(JTextField.RIGHT);
		
		highEndField = new JTextField(5);
		highEndField.setBackground(Color.lightGray);
		highEndField.setEditable(false);
		highEndField.setHorizontalAlignment(JTextField.RIGHT);
		
		offScalePercentField = new JTextField(5);
		offScalePercentField.setBackground(Color.lightGray);
		offScalePercentField.setEditable(false);
		offScalePercentField.setHorizontalAlignment(JTextField.RIGHT);
		
		lowEndPercentField = new JTextField(5);
		lowEndPercentField.setBackground(Color.lightGray);
		lowEndPercentField.setEditable(false);
		lowEndPercentField.setHorizontalAlignment(JTextField.RIGHT);

		highEndPercentField = new JTextField(5);
		highEndPercentField.setBackground(Color.lightGray);
		highEndPercentField.setEditable(false);
		highEndPercentField.setHorizontalAlignment(JTextField.RIGHT);
		
		sortedValues = initSortedValues(framework.getData().getFullExperiment().getMatrix());
		updateStats(origLower, origUpper);
		
		JPanel dummyPanel = new JPanel();
		dummyPanel.setOpaque(false);
		
		add(numberLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,20), 0,0));	    
	    add(numberLabel, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,4,10), 0,0));
	    add(percentLabel, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,4,0), 0,0));
	    
	    add(offScaleLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,20), 0,0));
	    add(offScaleField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,10), 0,0));
	    add(offScalePercentField, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));

	    add(lowEndLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,20), 0,0));
	    add(lowEndField, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,10), 0,0));
	    add(lowEndPercentField, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));

	    add(highEndLabel, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,20), 0,0));
	    add(highEndField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,10), 0,0));
	    add(highEndPercentField, new GridBagConstraints(2,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
	    
	}
	
	/**
	 * Initializes the sorted values array of all matrix values, sorted array will
	 * consist of only !NaN values and will be sorted from low to high
	 * @param m <code>org.tigr.util.FloatMatrix</code>
	 * @return returns the sorted float array of !NaN values
	 */
	private float [] initSortedValues(FloatMatrix m) {
		float [] vals = m.getColumnPackedCopy();
		QSort qsort = new QSort(vals, QSort.ASCENDING);
		vals = qsort.getSorted();
	
		int numberNaN = 0;
		
		for(int i = 0; i < vals.length; i++) {		
			if(Float.isNaN(vals[i]))
				numberNaN++;
			else
				break;			
		}
		
		int validN = vals.length-numberNaN;
		
		float [] values = new float[validN];
		
		for(int i = 0; i < values.length; i++) {
			values[i] = vals[i+numberNaN];			
		}
		
		return values;
		
	}

	/**
	 * Updates the stats panel with the current limits and resulting
	 * saturation stats.
	 * @param lower current lower limit
	 * @param upper current upper limit
	 */
	public void updateStats(float lower, float upper) {
		
		int lowCount = getLowCount(lower);
		int highCount = getHighCount(upper);
		
		float lowPerc = ((float)lowCount/sortedValues.length)*100f;
		float highPerc = ((float)highCount/sortedValues.length)*100f;
		float offPerc = ((float)(lowCount+highCount)/sortedValues.length)*100f;
		
		this.offScaleField.setText(String.valueOf(lowCount+highCount));
		this.offScalePercentField.setText(format.format(offPerc));

		this.lowEndField.setText(String.valueOf(lowCount));
		this.lowEndPercentField.setText(format.format(lowPerc));

		this.highEndField.setText(String.valueOf(highCount));
		this.highEndPercentField.setText(format.format(highPerc));
		
	}
	
	/**
	 * Returns the number of !NaN values in the data below the lower limit
	 * @param lower current lower limit
	 * @return number of points off low end
	 */
	private int getLowCount(float lower) {
		int index = 0;
		while(sortedValues[index]<lower) {
			index++;
		}
		return index;
	}
	
	/**
	 * Returns the number of !NaN values in the data above the upper limit
	 * @param upper current upper limit
	 * @return number of points off high end
	 */	
	private int getHighCount(float upper) {
		int index = sortedValues.length-1;
		int highCount  = 0;
		while(sortedValues[index]>upper) {
			index--;
			highCount++;
		}
		return highCount;
	}	
}
	
	
/**
 * @author braisted
 *
 * Handles dialog events
 */
public class EventListener extends WindowAdapter implements ActionListener, KeyListener {
	
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("ok-command")) {
        	if(validateValues()) {
        		System.out.println("set ratio dialog ok hit, it's valid");
	        	result = JOptionPane.OK_OPTION;
	        	dispose();
        	}
        } else if (command.equals("cancel-command")) {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        } else if (command.equals("reset-command")) {
            resetControls();
        } else if (command.equals("info-command")) {
            HelpWindow hw = new HelpWindow(SetRatioScaleDialog.this, "Ratio Scale Dialog");
            if(hw.getWindowContent()){
                hw.setSize(450,500);
                hw.setLocation();
                hw.show();
            } else {
                hw.setVisible(false);
                hw.dispose();
            }
        } else if (command.equals("update-command")) {
        	updateLimits();        
        } else if (command.equals("change-gradient-command")) {
        	doubleGradient = doubleGradientButton.isSelected();          

        	if(doubleGradient) {  //might have to alter pos image to have proper neutal
        		BufferedImage posImage = createGradientImage(new Color(gradientPreviewPanel.getNegImage().getRGB(255,0)), new Color(gradientPreviewPanel.getPosImage().getRGB(255,0)));
				gradientPreviewPanel.setPosImage(posImage);			
        	}
        	
        	repaint();
        }
    }	
	
	public void windowClosing(WindowEvent e) {
		result = JOptionPane.CLOSED_OPTION;
		dispose();
	}
	
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				Float.parseFloat(lowerTextField.getText());
				Float.parseFloat(upperTextField.getText());
				result = JOptionPane.OK_OPTION;
			} catch (Exception exception) {
				result = JOptionPane.CANCEL_OPTION;
			}
			dispose();
		}
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
}
}