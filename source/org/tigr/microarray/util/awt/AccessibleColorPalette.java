package org.tigr.microarray.util.awt;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.colorchooser.*;

import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * @author Sarita Nair
 * 
 * 
 * This class can be used to  add a ColorBlind chooser panel to the default
 * JColorChooser palette, which includes three chooser panels namely
 * Swatches, HSB and RGB.
 * 
 *  Orange, skyBlue, bluishGreen, Yellow, Vermillion and Reddish purple are 
 *  the colorblind colors currently defined in the class.
 *  
 *  This set of colors which is unambiguos to both colorblind and non-colorblind
 *  people has been adapted from 
 *  "How to make figures and presentations that are friendly to color blind people"
 *  by Masataka Okabe
 *
 */
public class AccessibleColorPalette extends AbstractColorChooserPanel implements ActionListener/*, ChangeListener*/{

	
/*Set of colors distinguishable by both colorblind and non-colorblind people. The
values for R,G and B have been adapted from the paper mentioned above*/
	public Color black=new Color(0,0,0);
	public Color blue=new Color(0,114,178);
	public Color orange=new Color(230,159,0);
	public Color skyBlue=new Color(86, 180, 233);
	public Color bluishGreen=new Color(0, 158, 115);
	public Color yellow=new Color(0, 228, 66);
	public Color vermillion=new Color(213, 94, 0);
	public Color reddishPurple=new Color(204, 121, 167);


//Colors are represented as respective ToggleButtons
	private JToggleButton blackCrayon;
	private JToggleButton blueCrayon;
	private JToggleButton orangeCrayon;
	private JToggleButton yellowCrayon;
	private JToggleButton skyBlueCrayon;
	private JToggleButton bluishGreenCrayon;
	private JToggleButton vermillionCrayon;
	private JToggleButton reddishPurpleCrayon;




	public void updateChooser() {
		Color color = getColorFromModel();
		if (orange.equals(color)) {
			orangeCrayon.setSelected(true);
		} else if (yellow.equals(color)) {
			yellowCrayon.setSelected(true);
		} else if (skyBlue.equals(color)) {
			skyBlueCrayon.setSelected(true);
		} else if (bluishGreen.equals(color)) {
			bluishGreenCrayon.setSelected(true);
		}else if (vermillion.equals(color)) {
			vermillionCrayon.setSelected(true);
		}else if (reddishPurple.equals(color)) {
			reddishPurpleCrayon.setSelected(true);
		}else if (blue.equals(color)) {
			blueCrayon.setSelected(true);
		}else if (black.equals(color)) {
			blackCrayon.setSelected(true);
		}
	}

	/**
	 * Creates buttons representing each of the colorblind colors
	 * @param name
	 * @param normalBorder
	 * @return
	 */
	
	protected JToggleButton createCrayon(String name,Border normalBorder) {
		JToggleButton crayon = new JToggleButton();
		crayon.setActionCommand(name);
		crayon.addActionListener(this);


		crayon.setText(name);
		crayon.setSize(4, 4);

		crayon.setFont(crayon.getFont().deriveFont(Font.ITALIC));
		//crayon.setHorizontalAlignment(JButton.HORIZONTAL);
		crayon.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		return crayon;
	}

	/**
	 * Adds each button, created by calling "createCrayon" to a ButtonGroup
	 * 
	 * 
	 */
	
	protected void buildChooser() {
		
		
		ButtonGroup boxOfCrayons = new ButtonGroup();
		Border border = BorderFactory.createEmptyBorder(4,4,4,4);
		
		blackCrayon = createCrayon("black", border);
		blackCrayon.setBackground(black);
		blackCrayon.setForeground(black);
		boxOfCrayons.add(blackCrayon);
		add(blackCrayon);

		
		blueCrayon = createCrayon("blue", border);
		blueCrayon.setBackground(blue);
		blueCrayon.setForeground(blue);
		boxOfCrayons.add(blueCrayon);
		add(blueCrayon);

		
		
		orangeCrayon = createCrayon("orange", border);
		orangeCrayon.setBackground(orange);
		orangeCrayon.setForeground(orange);
		boxOfCrayons.add(orangeCrayon);
		add(orangeCrayon);

		skyBlueCrayon = createCrayon("skyBlue", border);
		skyBlueCrayon.setBackground(skyBlue);
		skyBlueCrayon.setForeground(skyBlue);
		boxOfCrayons.add(skyBlueCrayon);
		add(skyBlueCrayon);

		bluishGreenCrayon = createCrayon("bluishGreen", border);
		bluishGreenCrayon.setBackground(bluishGreen);
		bluishGreenCrayon.setForeground(bluishGreen);
		boxOfCrayons.add(bluishGreenCrayon);
		add(bluishGreenCrayon);

		yellowCrayon = createCrayon("yellow", border);
		yellowCrayon.setBackground(yellow);
		yellowCrayon.setForeground(yellow);
		boxOfCrayons.add(yellowCrayon);
		add(yellowCrayon);

		vermillionCrayon = createCrayon("vermillion", border);
		vermillionCrayon.setBackground(vermillion);
		vermillionCrayon.setForeground(vermillion);
		boxOfCrayons.add(vermillionCrayon);
		add(vermillionCrayon);

		reddishPurpleCrayon = createCrayon("reddishPurple", border);
		reddishPurpleCrayon.setBackground(reddishPurple);
		reddishPurpleCrayon.setForeground(reddishPurple);
		boxOfCrayons.add(reddishPurpleCrayon);
		add(reddishPurpleCrayon);


	}

/**
 * Checks which color (button) has been selected 
 */

	public void actionPerformed(ActionEvent e) {
		Color newColor = null;
		String command = ((JToggleButton)e.getSource()).getActionCommand();
		if ("orange".equals(command)) 
			newColor=orange;
		else if ("skyBlue".equals(command)) 
			newColor = skyBlue;
		else if ("yellow".equals(command))
			newColor = yellow;
		else if ("bluishGreen".equals(command))
			newColor = bluishGreen;
		else if ("reddishPurple".equals(command))
			newColor = reddishPurple;
		else if ("vermillion".equals(command))
			newColor = vermillion;
		else if ("blue".equals(command))
			newColor = blue;
		else if ("black".equals(command))
			newColor = black;
			
		getColorSelectionModel().setSelectedColor(newColor);
	}

	public String getDisplayName() {
		return "ColorBlindPalette";
	}

	public Icon getSmallDisplayIcon() {
		return null;
	}

	public Icon getLargeDisplayIcon() {
		return null;
	}
	
	public void uninstallChooserPanel(JColorChooser panel) {
		super.uninstallChooserPanel(panel);
	}

	
	
	
	

}
