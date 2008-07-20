/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;

/**
 * @author braisted
 *
 * Dialog to support Linear Expression Graph rendering options
 */
public class GraphScaleCustomizationDialog extends AlgorithmDialog {

	private JTabbedPane pane;
	private YScalePanel yPanel;
	private GraphRenderingPanel graphRenderingPanel;
	private Hashtable props;
	private int result;

	//use display menu limits
	public static final int YRANGE_OPTION_DISPLAY_MENU = 0;
	//define custom limites
	public static final int YRANGE_OPTION_CUSTOM_RANGE = 1;
	//use data to automatically set range
	public static final int YRANGE_OPTION_AUTO_RANGE = 2;
	
	/**
	 * Constructor
	 * @param parent parent fram
	 * @param modal modal
	 */		
	public GraphScaleCustomizationDialog(JFrame parent, boolean modal, Hashtable props) {
		super(parent, "Customize Graph", modal);
		this.props = props;
		
		result = JOptionPane.CANCEL_OPTION;
		pane = new JTabbedPane();
		yPanel = new YScalePanel();
		graphRenderingPanel = new GraphRenderingPanel();
		
		pane.add("Y Range and X Axis Parameters", yPanel);
		pane.add("Graph Rendering Options", graphRenderingPanel);
		pane.setBackgroundAt(0,Color.white);
						
		this.addContent(pane);
		this.setActionListeners(new Listener());
		pack();
	}
	
	/**
	 * Displays dialog, returns closing state
	 * @return closeing state from JOptionPane.OK_OPTION, JOptionPane.CANCEL_OPTION
	 */
	public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();		
		return result;
	}
	
	/**
	 * Returns the range mode option selected
	 * @return mode
	 */
	public int getYRangeMode() {
		int mode;
		if(yPanel.displaySettingRangeButton.isSelected())
			mode = YRANGE_OPTION_DISPLAY_MENU;
		else if(yPanel.customRangeButton.isSelected()) 
			mode = YRANGE_OPTION_CUSTOM_RANGE;
		else
			mode = YRANGE_OPTION_AUTO_RANGE;
		return mode;
	}
	
	/**
	 * Minium custom y limit
	 * @return min. limit
	 */
	public float getYMin() {
		return Float.parseFloat(yPanel.minField.getText());
	}
	
	/**
	 * Max custom y limit
	 * @return max. limit
	 */
	public float getYMax() {
		return Float.parseFloat(yPanel.maxField.getText());
	}
	
	/**
	 * Returns the cutom tick interval
	 */
	public float getYTicInterval() {
		return Float.parseFloat(yPanel.ticIntervalField.getText());
	}	

	/**
	 * Returns true if X axis should be displayed
	 * @return state
	 */
	public boolean showXAxisLine() {
		return yPanel.showXAxisBox.isSelected();
	}
	
	/**
	 * returns the y value for x axis, often zero
	 * @return value for crossing
	 */
	public float getXAxisCrossPoint() {
		return Float.parseFloat(yPanel.xAxisField.getText());
	}
	
	/**
	 * Current color selected for x axis
	 * @return current color
	 */
	public Color getXAxisColor() {
		if(yPanel.xAxisColorBox.getSelectedItem() instanceof Color) 
			return (Color)yPanel.xAxisColorBox.getSelectedItem();
		return yPanel.getCustomXAxisColor();			
	}
	
	/**
	 * Current x axis style
	 * @return stroke pattern
	 */
	public BasicStroke getXAxisStyle() {
		return ((StrokePreview)yPanel.xAxisLineBox.getSelectedItem()).getBasicStroke();
	}
	
	/**
	 * True for offset lines mode rather than connected mode
	 * @return
	 */
	public boolean isOffsetLinesModeSelected() {
		return this.graphRenderingPanel.isOffsetLinesModeSelected();
	}
	
	/**
	 * Offset mode midpoint value
	 * @return midpoint
	 */
	public float getOffsetMidpoint() {
		return this.graphRenderingPanel.getOffsetMidpoint();
	}	

	/**
	 * Retruns min. value for offset used for discrete rendering
	 * @return min value
	 */
	public float getOffsetMin() {
		return this.graphRenderingPanel.getOffsetMin();
	}

	/**
	 * Retruns max. value for offset used for discrete rendering
	 * @return max value
	 */
	public float getOffsetMax() {
		return this.graphRenderingPanel.getOffsetMax();
	}

	/**
	 * True if overlay is selected
	 * @return
	 */
	public boolean getShowOverlay() {
		return this.graphRenderingPanel.getShowOverlay();
	}
	
	/*
	public static void main(String [] args) {
		Hashtable props = new Hashtable();
		props.put("x-axis-color", Color.magenta);
		GraphScaleCustomizationDialog d = new GraphScaleCustomizationDialog(null, true, props);
		d.show();
	}	
	*/
	
	/**
	 * Validates range, true if valid
	 */
	public boolean validateRange() {				
		if(this.yPanel.customRangeButton.isSelected()) {
			float min, max, interval;
			int level = 0;
			String msg;
			
			//try to parse
			try {
				min = Float.parseFloat(yPanel.minField.getText());
				level++;
				max = Float.parseFloat(yPanel.maxField.getText());
				level++;
				interval = Float.parseFloat(yPanel.ticIntervalField.getText());
				level++;
			} catch (NumberFormatException nfe) {
				msg = "Minimum Value entry is not a number.";
				if(level == 1)
					msg = "Maximum Value entry is not a number.";
				else if(level == 2)
					msg = "Tick Interval Value entry is not a number.";
					
				JOptionPane.showMessageDialog(this, msg, "Number Format Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(min >= max) {
				msg = "Min Value should be < Max Value.";
				JOptionPane.showMessageDialog(this, msg, "Value Error", JOptionPane.ERROR_MESSAGE);				
				return false;
			}
			
			if(max-min < interval) {
				msg = "The tick interval is larger than the y range.  Please make an appropriate change\n to either the range limits or tick interval.";
				JOptionPane.showMessageDialog(this, msg, "Value Error", JOptionPane.ERROR_MESSAGE);				
				return false;
			}
		}		
		return true;		
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * Contains first pane of controls
	 */
	public class YScalePanel extends JPanel {
		//private JRadioButton globalAutoFitButton;
		//private JCheckBox symetricRangeBox;
		private JRadioButton displaySettingRangeButton;
		private JRadioButton customRangeButton;
		
		private JTextField minField;
		private JTextField maxField;
		private JTextField ticIntervalField;
		private JLabel minLabel, maxLabel, ticLabel;
		
		private JCheckBox showXAxisBox;
		private JLabel xAxisCrossesLabel, lineLabel, colorLabel;
		private JTextField xAxisField;
		private StrokePreviewBox xAxisLineBox;
		private JComboBox xAxisColorBox;

		private MyCellRenderer renderer;
		
		public YScalePanel() {
			
			super(new GridBagLayout());
			
			Listener listener = new Listener();
			ParameterPanel rangeOptionPanel = new ParameterPanel("Y Range Options");
			rangeOptionPanel.setLayout(new GridBagLayout());
			ButtonGroup bg = new ButtonGroup();		
			
			//intial option, external display setting
			displaySettingRangeButton = new JRadioButton("External Display Menu Range Setting", true);
			displaySettingRangeButton.setOpaque(false);
			displaySettingRangeButton.setFocusPainted(false);
			displaySettingRangeButton.setActionCommand("y-range-selection-command");
			displaySettingRangeButton.addActionListener(listener);			
			bg.add(displaySettingRangeButton);
			
			//second option, custom display option
			JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
			sep1.setPreferredSize(new Dimension(250,10));
			sep1.setSize(250,10);
			customRangeButton = new JRadioButton("Custom Y Range");
			customRangeButton.setOpaque(false);
			customRangeButton.setFocusPainted(false);
			customRangeButton.setActionCommand("y-range-selection-command");
			customRangeButton.addActionListener(listener);
			bg.add(customRangeButton);

			Dimension fieldDim = new Dimension(60, 20);
			minLabel = new JLabel("Minimum Value");
			minField = new JTextField(((Float)(props.get("y-axis-min"))).toString());
			minField.setPreferredSize(fieldDim);
			minField.setSize(fieldDim);
			maxLabel = new JLabel("Maximum Value");
			maxField = new JTextField(((Float)(props.get("y-axis-max"))).toString());
			maxField.setPreferredSize(fieldDim);
			maxField.setSize(fieldDim);
			ticLabel = new JLabel("Tick Interval");
			ticIntervalField = new JTextField(((Float)(props.get("y-axis-tic-interval"))).toString());
			ticIntervalField.setPreferredSize(fieldDim);
			ticIntervalField.setSize(fieldDim);
				
			//last option
			//JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);			
			//sep2.setPreferredSize(new Dimension(250,10));
			//sep1.setSize(250,10);
			
			//globalAutoFitButton = new JRadioButton("Autofit to Data Range");
			//globalAutoFitButton.setOpaque(false);
			//globalAutoFitButton.setFocusPainted(false);
			//globalAutoFitButton.setActionCommand("y-range-selection-command");
			//globalAutoFitButton.addActionListener(listener);			
			//bg.add(globalAutoFitButton);
			
			//symetricRangeBox = new JCheckBox("Symetric Range about y=0");
			//symetricRangeBox.setOpaque(false);
			//symetricRangeBox.setFocusPainted(false);
			//symetricRangeBox.setSelected(((Boolean)(props.get("y-axis-symetry"))).booleanValue());
			
			//load the range panel
			rangeOptionPanel.add(displaySettingRangeButton, new GridBagConstraints(0,0,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,0),0,0));
			rangeOptionPanel.add(sep1, new GridBagConstraints(0,1,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			rangeOptionPanel.add(customRangeButton, new GridBagConstraints(0,2,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,0),0,0));
			rangeOptionPanel.add(minLabel, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,5,15),0,0));
			rangeOptionPanel.add(minField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,5,0),0,0));
			rangeOptionPanel.add(maxLabel, new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,5,15),0,0));
			rangeOptionPanel.add(maxField, new GridBagConstraints(1,4,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,5,0),0,0));
			rangeOptionPanel.add(ticLabel, new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,10,10,15),0,0));
			rangeOptionPanel.add(ticIntervalField, new GridBagConstraints(1,5,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));
			//rangeOptionPanel.add(sep2, new GridBagConstraints(0,6,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

			//rangeOptionPanel.add(globalAutoFitButton, new GridBagConstraints(0,7,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,0),0,0));
			//rangeOptionPanel.add(symetricRangeBox, new GridBagConstraints(1,7,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
			
			ParameterPanel xAxisOptionPanel = new ParameterPanel("X Axis Display Options");
			xAxisOptionPanel.setLayout(new GridBagLayout());

			showXAxisBox = new JCheckBox("Show X Axis Line");
			showXAxisBox.setOpaque(false);
			showXAxisBox.setFocusPainted(false);
			showXAxisBox.setActionCommand("y-range-selection-command");
			showXAxisBox.addActionListener(listener);
			showXAxisBox.setSelected(((Boolean)(props.get("show-x-axis"))).booleanValue());
			
			xAxisCrossesLabel = new JLabel("X axis crosses at");
			xAxisCrossesLabel.setOpaque(false);
			
			xAxisField = new JTextField(((Float)(props.get("x-axis-cross-point"))).toString());
			xAxisField.setPreferredSize(fieldDim);
			xAxisField.setSize(fieldDim);
			
			xAxisLineBox = new StrokePreviewBox();
		    Dimension dim = new Dimension(80,18);
			xAxisLineBox.setPreferredSize(dim);
		    xAxisLineBox.setSize(dim);

		    StrokePreview currStrokePreview = new StrokePreview((BasicStroke)(props.get("x-axis-stroke")));
		    
		    for(int i = 0; i < xAxisLineBox.getItemCount(); i++) {
		    	if(((StrokePreview)(xAxisLineBox.getItemAt(i))).compareTo(currStrokePreview) == 0) {
		    		xAxisLineBox.setSelectedIndex(i);
		    		break;
		    	}
		    }
		    
		    
		    xAxisLineBox.setSelectedItem(new StrokePreview((BasicStroke)(props.get("x-axis-stroke"))));
		    ((StrokePreview)(xAxisLineBox.getSelectedItem())).setSelected(true);
		    
			Vector v = new Vector();
			v.add(Color.lightGray);
			v.add(new Color(150, 150, 250));
			v.add(Color.gray);
			v.add(Color.black);
			v.add("Customize");
			
			xAxisColorBox = new JComboBox(v);
			xAxisColorBox.setBackground((Color)xAxisColorBox.getSelectedItem());
			xAxisColorBox.setEditable(false);
			xAxisColorBox.setOpaque(true);
	
		    Color currXAxisColor = (Color)(props.get("x-axis-color"));
		    xAxisColorBox.setBackground(currXAxisColor);
		    
		    if(currXAxisColor.equals(Color.gray)
		    		|| currXAxisColor.equals(Color.black)
					|| currXAxisColor.equals(new Color(150,150,250))) {
		    	xAxisColorBox.setSelectedItem(currXAxisColor);		    	
		    	renderer = new MyCellRenderer();
		    } else {
		    	xAxisColorBox.setSelectedIndex(3);				
		    	renderer = new MyCellRenderer(currXAxisColor);
		    }
		    
		    xAxisColorBox.setRenderer(renderer);
			    
		    xAxisColorBox.setPreferredSize(dim);
		    xAxisColorBox.setSize(dim);
			xAxisColorBox.addActionListener(new ListListener());
			
			xAxisOptionPanel.add(showXAxisBox, new GridBagConstraints(0,0,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,0),0,0));
			//xAxisOptionPanel.add(xAxisCrossesLabel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0),0,0));
			//xAxisOptionPanel.add(xAxisField, new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
	
			JPanel xCrossesPanel = new JPanel(new GridBagLayout());
			xCrossesPanel.setBackground(Color.white);
			Dimension panDim = new Dimension(200, 20);
			xCrossesPanel.setPreferredSize(panDim);
			xCrossesPanel.setSize(panDim);

			xCrossesPanel.add(xAxisCrossesLabel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0),0,0));
			xCrossesPanel.add(xAxisField, new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0),0,0));
			xAxisOptionPanel.add(xCrossesPanel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,10,10,0),0,0));

			JPanel fillPanel = new JPanel();
			fillPanel.setPreferredSize(panDim);
			fillPanel.setSize(panDim);
			fillPanel.setBackground(Color.white);
			xAxisOptionPanel.add(fillPanel, new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));			
			
			lineLabel = new JLabel("Line Style");
			colorLabel = new JLabel("Line Color");
			xAxisOptionPanel.add(lineLabel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,4,10),0,0));
			xAxisOptionPanel.add(colorLabel, new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,4,10),0,0));
			
			xAxisOptionPanel.add(xAxisLineBox, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,10),0,0));
			xAxisOptionPanel.add(xAxisColorBox, new GridBagConstraints(1,3,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,10),0,0));

			add(rangeOptionPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			add(xAxisOptionPanel, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		
			//Set range option selection
			int rangeMode = ((Integer)(props.get("y-range-mode"))).intValue();
			if(rangeMode == GraphScaleCustomizationDialog.YRANGE_OPTION_DISPLAY_MENU)
				this.displaySettingRangeButton.setSelected(true);
			else if(rangeMode == GraphScaleCustomizationDialog.YRANGE_OPTION_CUSTOM_RANGE)
				this.customRangeButton.setSelected(true);
			//else
				//this.globalAutoFitButton.setSelected(true);
 
			
			updateForRangeModeSelection();
		}
		
	
		
		/**
		 * refreshes state of control based on range mode		 *
		 */
		private void updateForRangeModeSelection() {
			minField.setEnabled(this.customRangeButton.isSelected());
			minLabel.setEnabled(this.customRangeButton.isSelected());
			maxField.setEnabled(this.customRangeButton.isSelected());
			maxLabel.setEnabled(this.customRangeButton.isSelected());
			ticIntervalField.setEnabled(this.customRangeButton.isSelected());
			ticLabel.setEnabled(this.customRangeButton.isSelected());			
			//this.symetricRangeBox.setEnabled(this.globalAutoFitButton.isSelected());
			this.xAxisCrossesLabel.setEnabled(this.showXAxisBox.isSelected());
			this.xAxisField.setEnabled(this.showXAxisBox.isSelected());
			this.colorLabel.setEnabled(this.showXAxisBox.isSelected());
			this.lineLabel.setEnabled(this.showXAxisBox.isSelected());						
			this.xAxisLineBox.setEnabled(this.showXAxisBox.isSelected());
			this.xAxisColorBox.setEnabled(this.showXAxisBox.isSelected());			
		}
		
		/**
		 * Opens a color chooser for axis color selection.
		 * @param startColor current color
		 * @return 
		 */
		public Color setCustomXAxisColor(Color startColor) {
			Color color = JColorChooser.showDialog(this, "Custom X-Axis Color", startColor);
			if(color != null)
				renderer.setCustomColor(color);
			repaint();
			return color;
		}
		
		/**
		 * returns the current axis color
		 * @return
		 */
		public Color getCustomXAxisColor() {
			return renderer.getCustomColor();
		}
		
	
	}
	

	/**
	 * 
	 * @author braisted
	 *
	 * JPanel extension to render a preview
	 */
	public class GraphRenderingPanel extends JPanel {
		
		private JRadioButton offsetLineButton;
		private JRadioButton connectPointsButton;
		
		private JTextField midPointField;
		private JTextField maxField;		
		private JTextField minField;
			
		private JCheckBox discreteOverlayBox;		
		private GraphPreview preview;
		
		
		public GraphRenderingPanel() {
			super(new GridBagLayout());
			setBackground(Color.white);
			PreviewListener listener = new PreviewListener();
			
			ButtonGroup bg = new ButtonGroup();
			
			offsetLineButton = new JRadioButton("Offset Lines from Midpoint");
			offsetLineButton.setFocusPainted(false);
			offsetLineButton.setOpaque(false);
			offsetLineButton.addActionListener(listener);
			bg.add(offsetLineButton);
			
			minField = new JTextField(((Float)(props.get("offset-graph-min"))).toString());				
			midPointField = new JTextField(((Float)(props.get("offset-graph-midpoint"))).toString());			
			maxField = new JTextField(((Float)(props.get("offset-graph-max"))).toString());			
					
			connectPointsButton = new JRadioButton("Connect Points");
			connectPointsButton.setFocusPainted(false);
			connectPointsButton.setOpaque(false);
			connectPointsButton.addActionListener(listener);
			bg.add(connectPointsButton);
	
			discreteOverlayBox = new JCheckBox("Discrete Value Overlay");
			discreteOverlayBox.setOpaque(false);
			discreteOverlayBox.setFocusPainted(false);
			discreteOverlayBox.addActionListener(listener);
			
			offsetLineButton.setSelected(((Boolean)(props.get("offset-lines-mode"))).booleanValue());
			connectPointsButton.setSelected(!((Boolean)(props.get("offset-lines-mode"))).booleanValue());
			discreteOverlayBox.setSelected(((Boolean)(props.get("show-discrete-overlay"))).booleanValue());
			
			
			preview = new GraphPreview(offsetLineButton.isSelected(), discreteOverlayBox.isSelected());
			
			add(offsetLineButton, new GridBagConstraints(0,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0), 0,0));
			
			add(new JLabel("Lower Cutoff"), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,15,10,10), 0,0));			
			add(minField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,10,0), 0,0));

			add(new JLabel("Neutral Point"), new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,10,10), 0,0));
			add(midPointField, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));

			add(new JLabel("Upper Cutoff"), new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,10,10), 0,0));			
			add(maxField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));

			add(discreteOverlayBox, new GridBagConstraints(0,4,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,20,0), 0,0));
			
			add(connectPointsButton, new GridBagConstraints(0,5,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));
			add(preview, new GridBagConstraints(2,0,1,6,0,0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,20,0,0), 0,0));
		}

		public boolean isOffsetLinesModeSelected() {
			return this.offsetLineButton.isSelected();
		}
		
		public float getOffsetMidpoint() {
			return Float.parseFloat(this.midPointField.getText());
		}
		
		public float getOffsetMax() {
			return Float.parseFloat(this.maxField.getText());
		}

		public float getOffsetMin() {
			return Float.parseFloat(this.minField.getText());
		}
	
		public boolean getShowOverlay() {
			return discreteOverlayBox.isSelected();
		}

		
		public class GraphPreview extends ParameterPanel {
			
			float [] previewValues;
			boolean offsetMode;
			float min = 0.3f;
			float max = 0.8f;
			float mid = 0.5f;
			
			boolean discreteOverlay;			
			
			public GraphPreview(boolean offsetMode, boolean discreteOverlay) {
				super("Preview");
				this.offsetMode = offsetMode;
				this.discreteOverlay = discreteOverlay;
				previewValues = new float[10];
				previewValues[0] = 0.85f;
				previewValues[1] = 0.90f;
				previewValues[2] = 0.83f;
				previewValues[3] = 0.91f;				
				previewValues[4] = 0.43f;
				previewValues[5] = 0.55f;
				previewValues[6] = 0.45f;
				previewValues[7] = 0.2f;
				previewValues[8] = 0.25f;
				previewValues[9] = 0.22f;

				setPreferredSize(new Dimension(200,200));
			}

			public void enableOffsetMode(boolean isOffset) {
				offsetMode = isOffset;
			}
			
			public void enableOverlay(boolean enable) {
				this.discreteOverlay = enable;				
			}

			public void paint(Graphics g) {
				super.paint(g);
				int w = getWidth();
				int h = getHeight();
				int xoffset = 25;
				int yoffset = 25;
				int x = xoffset;
				int y = yoffset;
				
				int xInt, yInt;

				Graphics2D g2 = (Graphics2D)g;				
				Composite defaultComp = g2.getComposite();

				xInt = (w-xoffset)/(previewValues.length-1);
				
				g.drawLine(xoffset/2, h/2+yoffset, w-xoffset/2, h/2+yoffset);
				
				int yLoc, yLoc1=0;
		
				if(discreteOverlay) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				} else {
					g2.setComposite(defaultComp);
				}
				
				if(offsetMode) {
				for(int i = 0; i < previewValues.length;i++) {
					
					yLoc = (int)(h*(1f-previewValues[i]))+yoffset;
					
					if(previewValues[i]<min)
						g.setColor(Color.green);
					else if(previewValues[i]>max)
						g.setColor(Color.red);
						
					g.drawLine(xInt*i+xoffset/2,h/2+yoffset, xInt*i+xoffset/2, yLoc);
					
					g.setColor(Color.black);
					g.fillOval(xInt*i+xoffset/2-2, yLoc, 4,4);
					
					}
				} else {										
					for(int i = 0; i < previewValues.length-1;i++) {						
						yLoc = (int)(h*(1f-previewValues[i]))+yoffset;
						yLoc1 = (int)(h*(1f-previewValues[i+1]))+yoffset;
									
						g.setColor(Color.black);
						g.drawLine(xInt*i+xoffset/2,yLoc+2, xInt*(i+1)+xoffset/2, yLoc1+2);

						g.setColor(Color.blue);
						g.fillOval(xInt*i+xoffset/2-2, yLoc, 4,4);
						//g.fillOval(xInt*i+xoffset/2, yLoc1, 4,4);					
						}
					g.setColor(Color.blue);
					g.fillOval(xInt*(previewValues.length-1)+xoffset/2-2, yLoc1, 4,4);
				}
				
				if(discreteOverlay) {
					//set default Composite
					g2.setComposite(defaultComp);
					g2.setStroke(new BasicStroke(2f));
					g2.setColor(Color.black);
					
					int x1 = 0, x2 = 0;
					float prevY = mid;
					float currY = mid;
					
					//intialize previous y
					if(previewValues[0] >= max)
						prevY = max;
					else if(previewValues[0] <= min)
						prevY = min;
					else
						prevY = mid;					
					
					
					for(int i = 1; i < previewValues.length;i++) {
						
						//set current y position
						if(previewValues[i] >= max)
							currY = max;
						else if(previewValues[i] <= min)
							currY = min;
						else
							currY = mid;
												
						//set current x position
							x2 = xInt*i+xoffset/2;							
							x1 = x2-xInt;							
						
						if(currY == prevY) {
							//draw a horizontal line from currY to prevY							
							g.drawLine(x1, (int)(h*(1f-prevY))+yoffset, x2, (int)(h*(1f-currY))+yoffset);
						} else if(prevY == mid) {
							//draw horizontal on mid
							g.drawLine(x1, (int)(h*(1f-prevY))+yoffset, x2, (int)(h*(1f-prevY))+yoffset);							
							//draw vertical line from mid to currY
							g.drawLine(x2, (int)(h*(1f-prevY))+yoffset, x2, (int)(h*(1f-currY))+yoffset);
						} else if(currY == mid) {
							//draw vertical line from prevY to mid							
							g.drawLine(x1, (int)(h*(1f-prevY))+yoffset, x1, (int)(h*(1f-mid))+yoffset);
							//draw horizontal line from mid to mid
							g.drawLine(x1, (int)(h*(1f-mid))+yoffset, x2, (int)(h*(1f-mid))+yoffset);
						} else {
							//draw vertical line from prevY to mid
							g.drawLine(x1, (int)(h*(1f-prevY))+yoffset, x1, (int)(h*(1f-mid))+yoffset);
							//draw horizontal line from mid to mid
							g.drawLine(x1, (int)(h*(1f-mid))+yoffset, x2, (int)(h*(1f-mid))+yoffset);
							//draw vertical line from mid to currY
							g.drawLine(x2, (int)(h*(1f-mid))+yoffset, x2, (int)(h*(1f-currY))+yoffset);
						}
						
						prevY = currY;

						
						
						}
					
				}
				g2.setComposite(defaultComp);
				g2.setStroke(new BasicStroke(1f));
			}
			

		}
		
		public class PreviewListener implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				preview.enableOffsetMode(offsetLineButton.isSelected());
				preview.enableOverlay(discreteOverlayBox.isSelected());
				preview.repaint();
			}
		}
	}

	
	/**
	 * 
	 * @author braisted
	 *
	 * JComboBox extension to present line stroke options
	 */
	public class StrokePreviewBox extends JComboBox {

		public StrokePreviewBox() {
			super();
			this.setBackground(Color.white);
			this.setRenderer(new MyCellRenderer());
			addItem(new StrokePreview(new BasicStroke(2f)));
			addItem(new StrokePreview(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {5f, 5f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {2f, 2f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {20f, 10f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {20f, 5f, 5f, 5f}, 0f)));	
			addItem(new StrokePreview(new BasicStroke(1f)));
			addItem(new StrokePreview(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {5f, 5f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {2f, 2f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {20f, 10f}, 0f)));
			addItem(new StrokePreview(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {20f, 5f, 5f, 5f}, 0f)));	
		}		
	}
	
	public class ListListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox source = (JComboBox)e.getSource();
			if(source.getSelectedItem() instanceof Color)
				source.setBackground((Color)source.getSelectedItem());
			else {
				Color color = yPanel.setCustomXAxisColor(yPanel.getCustomXAxisColor());
				if(color != null) {
					source.setBackground(color);
				}
			}
				
		}		
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * JLabel extension to handle color selections
	 */
	public class MyCellRenderer extends JLabel implements ListCellRenderer {

		boolean isSelected;
		Color customColor = Color.lightGray;

		public MyCellRenderer() {
			setPreferredSize(new Dimension(20,20));
			setSize(20,20);
			setBackground(Color.white);
			setOpaque(true);
		}
		
		public MyCellRenderer(Color color) {
			setPreferredSize(new Dimension(20,20));
			setSize(20,20);
			setBackground(Color.white);
			setOpaque(true);
			customColor = color;
		}

		public void setSelected(boolean selected) {
			this.isSelected = selected;
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {						
			if(value instanceof Color) {
				setText("");
				setBackground((Color)value);								
				return this;
			} else if(value instanceof String){
				setText("Custom Color");
				setBackground(customColor);
				return this;
			}
			//return the object (StrokePreview) if it's not a Color object
			return (Component)value;		
		}
		
		public void paint(Graphics g) {
			super.paint(g);
		}		
		
		public Color getCustomColor() {
			return customColor;
		}
		
		public void setCustomColor(Color c) {
			customColor = c;
		}
		
	}
	/**
	 * 
	 * @author braisted
	 *
	 * Stroke preview object
	 */
	public class StrokePreview extends JLabel implements Comparable {
		private BasicStroke stroke;
		private boolean isSelected;
		
		public StrokePreview(BasicStroke stroke) {
			setOpaque(false);			
			this.stroke = stroke;
			setPreferredSize(new Dimension(100, 20));
			setSize(100,15);
			isSelected = false;
		}
		
		public BasicStroke getBasicStroke() {
			return stroke;
		}
		
		public void setSelected(boolean sel) {
			isSelected = sel;
		}
		
		public void paint(Graphics g) {
			super.paint(g);			
			Graphics2D g2 = (Graphics2D)g;
			Stroke origStroke = g2.getStroke();
			g2.setStroke(stroke);
			g2.drawLine(3, getHeight()/2, getWidth()-3, getHeight()/2);
			g2.setStroke(origStroke);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			StrokePreview other = (StrokePreview)o;

			if(other.getBasicStroke().equals(stroke))
				return 0;
			else
				return 1;
		}
		
	}
	
	public class Listener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("y-range-selection-command")) {
				yPanel.updateForRangeModeSelection();
			} else if(command.equals("ok-command")) {
				//check range parameters
				if(!validateRange()) {
					return;
				}								
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if(command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if(command.equals("")) {
				
			} else if(command.equals("")) {
				
			}
		}
		
	}

}
