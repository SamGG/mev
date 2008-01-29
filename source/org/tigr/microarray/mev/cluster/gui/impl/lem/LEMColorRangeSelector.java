/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/*
 * @author braisted
 * 
 * Dialog to set the colors and bin ranges for LEM
 * 
 */
public class LEMColorRangeSelector extends AlgorithmDialog {

	private LinearExpressionMapViewer lem;
	
	private int result = JOptionPane.CANCEL_OPTION;
		
	private JButton lowest, lower, higher, highest;
	private float limit1, limit2, limit3, limit4, midPoint;

	private Color origColor1, origColor2, origColor3, origColor4;
	private float origLimit1, origLimit2, origLimit3, origLimit4, origMidPoint;

	private int buttonW = 100;
	private int buttonH = 40;
	
	private JTextField c1Field;
	private JTextField c2Field;
	private JTextField midField;
	private JTextField c3Field;
	private JTextField c4Field;
	
	private int fieldW = 80;
	private int fieldH = 30;
		
	public LEMColorRangeSelector(JFrame parent, LinearExpressionMapViewer lem, Color c1, Color c2, Color c3, Color c4,
			float lim1, float lim2, float mid, float lim3, float lim4) {
		
		super(parent, "LEM Bin Range and Color Selection", false);
		this.lem = lem;
		//capture original settings
		origColor1 = c1;
		origColor2 = c2;
		origColor3 = c3;
		origColor4 = c4;		
		limit1 = origLimit1 = lim1;
		limit2 = origLimit2 = lim2;
		limit3 = origLimit3 = lim3; 
		limit4 = origLimit4 = lim4;
		midPoint = origMidPoint = mid;

		ButtonListener listener = new ButtonListener();
		
		ParameterPanel panel = new ParameterPanel("Color and Range Settings");
		panel.setLayout(new GridBagLayout());

		Dimension buttonDim = new Dimension(buttonW, buttonH);

		lowest = createColorButton(c1, listener, buttonDim, "lowest");
		lower = createColorButton(c2, listener, buttonDim, "lower");
		higher = createColorButton(c3, listener, buttonDim, "higher");
		highest = createColorButton(c4, listener, buttonDim, "highest");
		
		JPanel midPanel = new JPanel();
		midPanel.setBackground(Color.white);
		midPanel.setPreferredSize(new Dimension(buttonW, buttonH));
		midPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		JPanel midPanel2 = new JPanel();
		midPanel2.setBackground(Color.white);
		midPanel2.setPreferredSize(new Dimension(buttonW, buttonH));
		midPanel2.setBorder(BorderFactory.createLineBorder(Color.black, 1));		

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		
		buttonPanel.add(lowest, new GridBagConstraints(0,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		buttonPanel.add(lower, new GridBagConstraints(2,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		buttonPanel.add(midPanel, new GridBagConstraints(4,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		buttonPanel.add(midPanel2, new GridBagConstraints(6,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		buttonPanel.add(higher, new GridBagConstraints(8,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		buttonPanel.add(highest, new GridBagConstraints(10,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

		
		//text field panel
		JPanel textPanel = new JPanel(new GridBagLayout());
		textPanel.setBackground(Color.WHITE);

		c1Field = createField(limit1, listener);
		c2Field = createField(limit2,listener);
		midField = createField(midPoint,listener);
		c3Field = createField(limit3,listener);
		c4Field = createField(limit4,listener);

		textPanel.add(createFillPanel(buttonW/2, fieldH), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		textPanel.add(c1Field, new GridBagConstraints(1,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10), 0,0));
		textPanel.add(c2Field, new GridBagConstraints(3,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10), 0,0));
		textPanel.add(midField, new GridBagConstraints(5,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10), 0,0));		
		textPanel.add(c3Field, new GridBagConstraints(7,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10), 0,0));
		textPanel.add(c4Field, new GridBagConstraints(9,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,10), 0,0));
		textPanel.add(createFillPanel(buttonW/2, fieldH), new GridBagConstraints(11,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));		
		
		panel.add(buttonPanel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,0,5), 0,0));
		panel.add(new ArrowPanel(), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1,5,1,5), 0,0));		
		panel.add(textPanel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5), 0,0));

		addContent(panel);
	
		reconfigureButtons(listener);
		setActionListeners(listener);
		pack();
		
	}
	
	/**
	 * Sets up dialog buttons
	 * @param listener
	 */
	private void reconfigureButtons(ButtonListener listener) {
		JButton previewButton = new JButton("Preview");		
		previewButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		previewButton.setFocusPainted(false);
		previewButton.setActionCommand("preview-command");
		previewButton.addActionListener(listener);
		Dimension dim = new Dimension(65, 30);
		previewButton.setPreferredSize(dim);
		previewButton.setSize(dim);
		
		okButton.setText("Apply");
		
		Component [] comp = buttonPanel.getComponents();
		
		//remove buttons
		for(int i = 0; i < comp.length; i++) {
			buttonPanel.remove(comp[i]);
		}

		buttonPanel.add(comp[0], new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[1], new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,10,2,0), 0,0));
		buttonPanel.add(previewButton, new GridBagConstraints(2,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[2], new GridBagConstraints(3,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[3], new GridBagConstraints(4,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[4], new GridBagConstraints(5,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,5), 0,0));
	}
	
	/**
	 * displays the dialog, returns OK or Cancel (JOptionPane)
	 * @return close status
 	*/
	public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
	
	/**
	 * Returns the color for the specified bin
	 * @param bin bin index 
	 * @return bin color
	 */
	public Color getColor(int bin) {
		Color color = null;
		if(bin == 0)
			color = lowest.getBackground();
		else if(bin == 1)
			color = lower.getBackground();
		else if(bin == 2)
			color = Color.white;		
		else if(bin == 3)
			color = higher.getBackground();
		else if(bin == 4)
			color = highest.getBackground();		
		return color;
	}

	/**
	 * Returns cutoff for a given bin
	 * @param index bin index
	 * @return cutoff value
	 */
	public float getCutoff(int index) {
		float limit = 0;
		if(index == 0)
			limit = Float.parseFloat(c1Field.getText());
		else if(index == 1)
			limit = Float.parseFloat(c2Field.getText());
		else if(index == 2)
			limit = Float.parseFloat(midField.getText());
		else if(index == 3)
			limit = Float.parseFloat(c3Field.getText());
		else if(index == 4)
			limit = Float.parseFloat(c4Field.getText());
		return limit;
	}
	
	/**
	 * applies current settings to lem, preview
	 * @return
	 */
	private boolean applySettings() {
		boolean valid = validateValues();
		if(valid) {
			lem.setBinLimitsAndColors(getCutoff(0), getCutoff(1),getCutoff(2),getCutoff(3),getCutoff(4),
					getColor(0), getColor(1), getColor(3), getColor(4));
		}
		return valid;
	}
	
	/**
	 * Returns true if cutoffs are valid
	 * @return validity state
	 */
	public boolean validateValues() {
		float v0, v1, v2, v3, v4;
		try {
			v0 = Float.parseFloat(c1Field.getText());
			v1 = Float.parseFloat(c2Field.getText());
			v2 = Float.parseFloat(midField.getText());
			v3 = Float.parseFloat(c3Field.getText());
			v4 = Float.parseFloat(c4Field.getText());			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "An entered value seems to be in an invalid format. Please try again.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		 if (v0 <= v1 && v1 <= v2 && v2 <= v3 && v3 <= v4)
		 	return true;
		 else {
			JOptionPane.showMessageDialog(this, "The cutoff values should be in asscending value from left to right. Please try again.", "Cutoff Value Error", JOptionPane.ERROR_MESSAGE);
			return false;		 
		 }
	}
	

	/**
	 * Constructs a JButton to render a bin.
	 * @param color
	 * @param listener
	 * @param buttonDim
	 * @param buttonName
	 * @return
	 */

	private JButton createColorButton(Color color, ButtonListener listener, Dimension buttonDim, String buttonName) {
		JButton b;
		b = new JButton("<html><center>Select<br>Color</center></html>");
		b.setBackground(color);
		b.setFocusPainted(false);
		b.setBorder(BorderFactory.createLineBorder(Color.black,1));
		b.setPreferredSize(buttonDim);
		b.setSize(buttonDim);
		//b.setMaximumSize(buttonDim);
		//b.setMinimumSize(buttonDim);
		b.setActionCommand("color-button-hit-command");
		b.addActionListener(listener);
		return b;	
	}
	
	/**
	 * Constructs text field
	 * @param val current limit
	 * @param listener event listener
	 * @return
	 */
	private JTextField createField(float val, ButtonListener listener) {
		JTextField field = new JTextField();
		field.setText(String.valueOf(val));
		Dimension dim = new Dimension(fieldW, fieldH);
		field.setPreferredSize(dim);
		field.setSize(dim);
		field.setActionCommand("field-focus-command");
		field.addFocusListener(listener);
		return field;
	}
	
	/**
	 * Filler panel
	 * @param w
	 * @param h
	 * @return
	 */
	private JPanel createFillPanel(int w, int h) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		Dimension dim = new Dimension(w,h);
		panel.setPreferredSize(dim);
		panel.setSize(dim);		
		return panel;
	}
	
	/**
	 * gets a new color and assigns it to button
	 * @param button button to recieve new color
	 */
	private void assignColor(JButton button) {
		Color color = button.getBackground();

		/*
		 * I want the HSB panel to be default so reorder the panels
		 */
		JColorChooser chooser = new JColorChooser(color);
		AbstractColorChooserPanel [] panels = chooser.getChooserPanels();
		AbstractColorChooserPanel [] newPanels = new  AbstractColorChooserPanel[panels.length];
		int hsbIndex = 0;		
		for(int i = 0; i < panels.length; i++) {
			if(panels[i].getClass().getName().equals("javax.swing.colorchooser.DefaultHSBChooserPanel"))
				hsbIndex = i;
		}		
		newPanels[0] = panels[hsbIndex];		
		int cnt = 1;
		for(int i = 0; i < panels.length; i++) {
			if( i != hsbIndex) {
				newPanels[cnt] = panels[i];
				cnt++;
			}				
		}
		chooser.setChooserPanels(newPanels);

		/*
		 *  Special listener to pair a button with a particular chooser
		 */
		ButtonListener listener = new ButtonListener(button, chooser);
		JDialog dialog = JColorChooser.createDialog(this, "Bin Color Selection", true, chooser, listener, new ButtonListener());
		dialog.show();

		button.setBorder(BorderFactory.createLineBorder(Color.black, 1));
	}
	
	/**
	 * resets controls to original state.
	 */
	private void resetControls() {
		lowest.setBackground(this.origColor1);
		lower.setBackground(this.origColor2);
		higher.setBackground(this.origColor3);
		highest.setBackground(this.origColor4);

		c1Field.setText(String.valueOf(this.origLimit1));
		c2Field.setText(String.valueOf(this.origLimit2));
		midField.setText(String.valueOf(this.origMidPoint));
		c3Field.setText(String.valueOf(this.origLimit3));
		c4Field.setText(String.valueOf(this.origLimit4));
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * Widget to render an arrow selectable/focusable to indicate
	 * current bin limit being adjusted
	 */
	public class ArrowPanel extends JPanel {
		int [] x;
		int [] y;
		
		Point p;
		
		public ArrowPanel () {
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			Dimension dim = new Dimension(6*buttonW, 60);
			setPreferredSize(dim);
			setSize(dim);
			x = new int[3];
			y = new int[3];
		}
		
		public void paint(Graphics g) {
			g.setColor(Color.blue);
			Graphics2D g2 = (Graphics2D)g;
				
			y[0] = 0;
			y[1] = getHeight();
			y[2] = getHeight();
			
			p =  lowest.getLocation();			
			x[0] = p.x + buttonW;
			
			p = c1Field.getLocation();
			x[1] = p.x;
			x[2] = p.x + fieldW;

			if(c1Field.hasFocus())
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));							
			else
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
							
			g.fillPolygon(x,y,3);

			
			p =  lower.getLocation();			
			x[0] = p.x + buttonW;
			
			p = c2Field.getLocation();
			x[1] = p.x;
			x[2] = p.x + fieldW;
			
			if(c2Field.hasFocus())
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));							
			else
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
				
			g.fillPolygon(x,y,3);
			 
			
			p =  lower.getLocation();			
			x[0] = p.x+2*buttonW;
			
			p = midField.getLocation();
			x[1] = p.x;
			x[2] = p.x + fieldW;
			
			if(midField.hasFocus())
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));							
			else
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
				
			g.fillPolygon(x,y,3);
			
			
			p =  higher.getLocation();			
			x[0] = p.x;
			
			p = c3Field.getLocation();
			x[1] = p.x;
			x[2] = p.x + fieldW;

			if(c3Field.hasFocus())
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));							
			else
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
	
			g.fillPolygon(x,y,3);
			
			
			p =  highest.getLocation();			
			x[0] = p.x;
			
			p = c4Field.getLocation();
			x[1] = p.x;
			x[2] = p.x + fieldW;

			if(c4Field.hasFocus())
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));							
			else
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
	
			g.fillPolygon(x,y,3);
		}
	}
	
	public class ButtonListener implements ActionListener, FocusListener {

		JButton button;
		JColorChooser chooser;
		
		public ButtonListener() { }

		public ButtonListener(JButton button, JColorChooser chooser) {
			this.button = button;
			this.chooser = chooser;
		}
		
		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();

			if(ae.getSource() instanceof JTextField)
				repaint();
			
			//ok from color chooser
			if(command.equals("OK")) {				
				button.setBackground(chooser.getColor());

				//cancel from color chooser
			} else if (command.equals("cancel")){
				
			} else if (command.equals("color-button-hit-command")){
				JButton sourceButton = (JButton)ae.getSource();
				sourceButton.setBorder(BorderFactory.createLineBorder(Color.black, 3));
				assignColor(sourceButton);
			} else if (command.equals("ok-command")) {
				if(applySettings()) {
					result = JOptionPane.OK_OPTION;
					dispose();
				}				
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				resetControls();
			} else if (command.equals("preview-command")) {
				applySettings();
			} else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(LEMColorRangeSelector.this, "LEM Bin Color and Limits Selection Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,500);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }				
			}
		}


		public void focusGained(FocusEvent e) {
			repaint();
		}

		public void focusLost(FocusEvent e) {
			repaint();
		}		
	}
	
	
	
	
}
