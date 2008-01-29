/*
 * Created on Feb 9, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/**
 * @author braisted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NonparFDRDialog extends AlgorithmDialog {

	private int result;
	private JFrame par;
	private float [] orderedFDR;
	private int [] orderedIndices;
	
	private GraphPanel graphPanel;
	private ControlPanel controlPanel;
	
	private int numCaptured;
	private float currFDR;

	public NonparFDRDialog(JFrame parent, float [] fdr, int [] indices)  {
		super(parent, "FDR Selection", true);
		result = JOptionPane.CANCEL_OPTION;
		orderedFDR = fdr;
		orderedIndices = indices;
		
		currFDR = 0;

		graphPanel = new GraphPanel(orderedFDR);
		controlPanel = new ControlPanel();
		
		mainPanel = new JPanel(new GridBagLayout());
		
		mainPanel.add(graphPanel, new GridBagConstraints(0,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));;
		mainPanel.add(controlPanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));;
				
		super.addContent(mainPanel);
		super.setActionListeners(new Listener());
				
		pack();
	}
	
	public float getFDRLimit() {
		return (float)currFDR;
	}
	
	public int [] getSelectedIndices() {
		int lastIndex = graphPanel.getNumberOfGenesLessThan(currFDR)-1;
		int [] selGenes = new int[lastIndex+1];
		
		for(int i = 0; i < selGenes.length; i++) {
			//subtract one to get zero base
			selGenes[i] = orderedIndices[i];
		}
				
		return selGenes;
	}
	
	public int [] getNonSelectedIndices() {
		int firstIndex = graphPanel.getNumberOfGenesLessThan(currFDR);
		int [] nonSelGenes = new int[orderedIndices.length-firstIndex];
		
		int index = 0;
		for(int i = firstIndex; i < orderedIndices.length; i++) {
			//subtract 1 to get zero based
			nonSelGenes[index] = orderedIndices[i];
			index++;
		}
		
		return nonSelGenes;
	}
		
	public void setCurrentFDR(int fdr) {
		currFDR = fdr;
	}


	public int showModal() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		this.setLocation(dim.width/2-getWidth()/2, dim.height/2-getHeight()/2);	
		show();
		return result;
	}
	
	public class GraphPanel extends JPanel {

		private NonparGraphViewer viewer;
		private int zoomLevel = 0;
		private float [] yZoomRange = {1f, 0.5f, 0.25f, 0.10f, 0.05f};
		private int [] xZoomRange = new int[5];		
		private GraphLine fdrLine;

		public GraphPanel(float [] fdr) {
			
			super(new GridBagLayout());
			
			int yLabelSpacing = 40;
			if(fdr.length > 10000)
				yLabelSpacing = 80;
			else if(fdr.length > 1000)
				yLabelSpacing = 60;
			
			viewer = new NonparGraphViewer(par, 0, 100, 0, 100, 0, fdr.length, 0, 1.0f, 50, 30, 30, yLabelSpacing,
				"FDR", "Count", "FDR");

			int zoomRangeIndex = 4; //for setting xZoomLimit
			xZoomRange[0] = fdr.length;
			
			for(int i = 0; i < fdr.length-1; i++) {
				if(!Float.isNaN(fdr[i])) {
					viewer.addGraphElement(new GraphPoint(i+1, fdr[i], Color.RED, 3));
					if(!Float.isNaN(fdr[i+1]))
						if(!Float.isNaN(fdr[i+1]))
							viewer.addGraphElement(new GraphLine(i+1, fdr[i],
									i+2, fdr[i+1]));					

					if(zoomRangeIndex > -1 && fdr[i] >= yZoomRange[zoomRangeIndex]) {
						xZoomRange[zoomRangeIndex] = i+1;
						zoomRangeIndex--;
					}
					
				}
				
				if(!Float.isNaN(fdr[fdr.length-1]))
					viewer.addGraphElement(new GraphPoint(fdr.length, fdr[fdr.length-1], Color.RED, 3));
			}
			
			//set ticks
			for(int i = 0; i < xZoomRange.length; i++) {
				viewer.addGraphElement(new GraphTick(xZoomRange[i], 3, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(xZoomRange[i]), Color.black));
				viewer.addGraphElement(new GraphTick(yZoomRange[i], 3, Color.black, GC.VERTICAL, GC.C, String.valueOf(yZoomRange[i]), Color.black));
			}
			
			fdrLine = new GraphLine(0, 0, fdr.length, 0);
			
			viewer.addGraphElement(fdrLine);
			
			viewer.setPointSize(100);
			
			setPreferredSize(new Dimension(300,300));
			setSize(new Dimension(300,300));
			add(viewer, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));;
		}
		
		public void zoomIn() {
			zoomLevel++;			
			viewer.setGraphStopX(xZoomRange[zoomLevel]);
			viewer.setGraphStopY(yZoomRange[zoomLevel]);
			
			//remove fdr line
			viewer.removeLastGraphElement();
			
			//update fdrLine
			if(currFDR != 0)				
				fdrLine = new GraphLine(0, currFDR, xZoomRange[zoomLevel], currFDR, Color.orange);
			else
				fdrLine = new GraphLine(0, currFDR, xZoomRange[zoomLevel], currFDR, Color.black);				
			viewer.addGraphElement(fdrLine);
			viewer.repaint();			
		}

		public void zoomOut() {
			zoomLevel--;
			viewer.setGraphStopX(xZoomRange[zoomLevel]);
			viewer.setGraphStopY(yZoomRange[zoomLevel]);
			
			//remove fdr line
			viewer.removeLastGraphElement();
			
			//update fdrLine
			if(currFDR != 0)				
				fdrLine = new GraphLine(0, currFDR, xZoomRange[zoomLevel], currFDR, Color.orange);
			else
				fdrLine = new GraphLine(0, currFDR, xZoomRange[zoomLevel], currFDR, Color.black);				
			viewer.addGraphElement(fdrLine);
			viewer.repaint();			
		}
		
		public int getZoomLevel() {
			return zoomLevel;
		}
		
		public int getNumberOfGenesLessThan(float fdr) {
			int numGenes = 0;
			
			if(orderedFDR.length == 0)
				return 0;

			while(numGenes <= orderedFDR.length && orderedFDR[numGenes] <= fdr) {
				numGenes++;
				if(numGenes == orderedFDR.length)
					break;
			}

			//remove fdr line
			viewer.removeLastGraphElement();
			
			//update fdrLine
			if(fdr != 0)
				fdrLine = new GraphLine(0, fdr, xZoomRange[zoomLevel], fdr, Color.orange);
			else
				fdrLine = new GraphLine(0, fdr, xZoomRange[zoomLevel], fdr, Color.black);
				
			viewer.addGraphElement(fdrLine);
			viewer.repaint();

			return numGenes;
		}
		
		public void resetGraph() {
			zoomLevel = 1;
			zoomOut();
		}
		
	}
	
	public class ControlPanel extends JPanel {
		
		private JSpinner fdrSpinner;
		private JButton inButton, outButton;
		private JTextField selGenesField;
		private JTextField falseGenesField;
		private JTextField currFDRField;
		
		public ControlPanel() {
			super(new GridBagLayout());
			
			Listener listener = new Listener();

			JPanel zoomPanel = new JPanel(new GridBagLayout());
			zoomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Graph Zoom"));

			inButton = new JButton("+");
			inButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			inButton.setFocusPainted(false);
			Dimension buttonDim = new Dimension(25,25);
			inButton.setPreferredSize(buttonDim);
			inButton.setSize(buttonDim);
			inButton.setActionCommand("zoom-in-command");
			inButton.addActionListener(listener);
			
			outButton = new JButton("-");
			outButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			outButton.setFocusPainted(false);
			outButton.setPreferredSize(buttonDim);
			outButton.setSize(buttonDim);
			outButton.setActionCommand("zoom-out-command");
			outButton.setEnabled(false);
			outButton.addActionListener(listener);
			
			zoomPanel.add(inButton, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));;
			zoomPanel.add(outButton, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));;
			
			JPanel fdrPanel = new JPanel(new GridBagLayout());
			fdrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "FDR Selection"));

			Dimension fieldDim = new Dimension(70,20);
			
			JLabel spinnerLabel = new JLabel("FDR Limit: ");			
			spinnerLabel.setOpaque(false);
			
			fdrSpinner = new JSpinner(new SpinnerNumberModel(0,0,1,0.01));			
			fdrSpinner.addChangeListener(listener);
			fdrSpinner.setPreferredSize(fieldDim);
			
			JLabel calcFDRLabel = new JLabel("Current Est. FDR:");
			currFDRField = new JTextField("0");
			currFDRField.setPreferredSize(fieldDim);
			currFDRField.setEditable(false);
			
			JLabel numSigLabel = new JLabel("Number of selected genes: ");
			selGenesField = new JTextField("0");
			selGenesField.setPreferredSize(fieldDim);
			selGenesField.setEditable(false);
			
			JLabel numFalseSigLabel = new JLabel("Est. # of false positive genes: ");
			falseGenesField = new JTextField("0");
			falseGenesField.setPreferredSize(fieldDim);
			falseGenesField.setEditable(false);

			fdrPanel.add(spinnerLabel, new GridBagConstraints(0,0,1,1,0.5,1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;
			fdrPanel.add(fdrSpinner, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;

			fdrPanel.add(calcFDRLabel, new GridBagConstraints(0,1,1,1,0.5,1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;
			fdrPanel.add(currFDRField, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;

			fdrPanel.add(numSigLabel, new GridBagConstraints(0,2,1,1,0.5,1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;
			fdrPanel.add(selGenesField, new GridBagConstraints(1,2,1,1,1,1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;

			fdrPanel.add(numFalseSigLabel, new GridBagConstraints(0,3,1,1,0.5,1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;
			fdrPanel.add(falseGenesField, new GridBagConstraints(1,3,1,1,1,1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0));;
			
			add(zoomPanel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));;
			add(fdrPanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));;			
		}
		
		public void validateZoomButtons(int zoomLevel) {
			inButton.setEnabled(zoomLevel<4);
			outButton.setEnabled(zoomLevel>0);
		}
		
		public void updateFields() {

			float fdr = (float)((Double)(fdrSpinner.getValue())).doubleValue();
			currFDR = fdr;
			
			int numberCaptured = graphPanel.getNumberOfGenesLessThan(fdr);
			
			selGenesField.setText(String.valueOf(numberCaptured));
						
			if(numberCaptured > 0) {
				currFDRField.setText(String.valueOf(orderedFDR[numberCaptured-1]));
				falseGenesField.setText(String.valueOf((int)(numberCaptured*(orderedFDR[numberCaptured-1]))));
			} else {
				currFDRField.setText("0");
				falseGenesField.setText(String.valueOf(0));
			}
		}
		
		public void reset() {
			validateZoomButtons(0);
			fdrSpinner.setValue(new Double(0));
			updateFields();
		}
	}
	
	public class Listener implements ActionListener, ChangeListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("zoom-in-command")) {
				graphPanel.zoomIn();
				controlPanel.validateZoomButtons(graphPanel.getZoomLevel());
			} else if(command.equals("zoom-out-command")){
				graphPanel.zoomOut();
				controlPanel.validateZoomButtons(graphPanel.getZoomLevel());
			} else if(command.equals("ok-command")) {
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if(command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if(command.equals("reset-command")) {
				graphPanel.resetGraph();
				controlPanel.reset();
			} else if(command.equals("info-command")) {

			}
			
		}

		public void stateChanged(ChangeEvent e) {
			//fdr change			
			controlPanel.updateFields();
		}
		
	}
	
	public class NonparGraphViewer extends GraphViewer {
		public NonparGraphViewer(JFrame frame, int startx, int stopx, int starty, int stopy,
			    double graphstartx, double graphstopx, double graphstarty, double graphstopy,
			    int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing,
			    String title, String xLabel, String yLabel) {
			super(frame, startx, stopx, starty, stopy,
				    graphstartx, graphstopx, graphstarty, graphstopy,
				    preXSpacing, postXSpacing, preYSpacing, postYSpacing,
				    title, xLabel, yLabel);			
		}
		
		public void removeLastGraphElement() {
			graphElements.remove(graphElements.size()-1);
			repaint();
		}
		
	}
	
	public static void main(String[] args) {
		float [] fdr = {0.01f, 0.02f, 0.04f, 0.05f, 0.1f, 0.3f, 0.5f, 0.8f, 0.9f, 0.98f};
		int [] index = {1,2,3,4,5,6,7,8,9,10};
		NonparFDRDialog d = new NonparFDRDialog(new JFrame(), fdr, index);
		d.showModal();
		d.dispose();
	}
}
