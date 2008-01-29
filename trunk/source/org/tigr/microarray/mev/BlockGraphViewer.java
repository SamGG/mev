package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.graph.GraphBar;
import org.tigr.graph.GraphElement;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.util.awt.GBA;

public class BlockGraphViewer extends GraphViewer {
	
	public int maxRows;
	public int maxColumns;
	
	public JPanel buttonPanel;
	public JButton button;
	 
	public JButton allButton;
	public JButton noneButton;
	
	private boolean[] visibles;
	private Color[] colors = {Color.black, Color.blue, Color.red, Color.yellow, Color.orange, Color.magenta, 
								Color.cyan, Color.pink, Color.green, Color.gray, Color.lightGray, Color.darkGray};

	public BlockGraphViewer(JFrame frame, int maxRows, int maxColumns, int startx, int stopx, int starty, int stopy, double graphstartx, double graphstopx, double graphstarty, double graphstopy, int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing, String title, String xLabel, String yLabel) {
		
		super(frame, startx, stopx, starty, stopy, graphstartx, graphstopx, graphstarty, graphstopy,
				preXSpacing, postXSpacing, preYSpacing, postYSpacing, title, xLabel, yLabel);
		
		this.maxRows = maxRows;
		this.maxColumns = maxColumns;
		
		generatePalette();
		
		visibles = new boolean[maxRows * maxColumns];
		for (int i = 0; i < visibles.length; i++) {
			visibles[i] = true;
		}
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		
		allButton = new JButton("All");
		allButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i = 0; i < visibles.length; i++) {
					visibles[i] = true;
				}
				repaint();
			}
		});
		gba.add(buttonPanel, allButton, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
		
		noneButton = new JButton("None");
		noneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i = 0; i < visibles.length; i++) {
					visibles[i] = false;
				}
				repaint();
			}
		});
		gba.add(buttonPanel, noneButton, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C);
		
		for (int i = 0; i < maxRows; i++) {
			for (int j = 0; j < maxColumns; j++) {
				button = new JButton((j + 1) + ", " + (i + 1));
				final int location = (maxColumns * i + j);
                                if(i == 0 && j == 0)
                                    button.setForeground(Color.white);
				button.setBackground(colors[location]);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						visibles[location] = (! visibles[location]);
						repaint();
					}
				});
				
				gba.add(buttonPanel, button, j, i + 1, 1, 1, 1, 1, GBA.B, GBA.C);
			}
		}
		
		frame.getContentPane().add(buttonPanel);
	}
	
	public void generatePalette() {
		
		int elements = maxColumns * maxRows;
		int strata;
		
		if (elements != 12) {
			for (strata = 0; (strata * strata * strata) < elements; strata++) {;}
			colors = new Color[strata * strata * strata];
			for (int r = strata - 1; r >= 0; r--) {
				for (int g = strata - 1; g >= 0; g--) {
					for (int b = strata - 1; b >= 0; b--) {
//			for (int r = 0; r < strata; r++) {
//				for (int g = 0; g < strata; g++) {
//					for (int b = 0; b < strata; b++) {
						colors[(r * strata * strata) + (g * strata) + b] = new Color((int) ((double) r * 255 / (strata - 1)), 
																						(int) ((double) g * 255 / (strata - 1)), 
																						(int) ((double) b * 255 / (strata - 1)));
					}
				}
			}
		}
	}
	
	public Color[] getColors() {return this.colors;}
	public boolean[] getVisibles() {return this.visibles;}
	
	public void drawGraph(Graphics2D g) {
	
		GraphElement e;
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		
		drawSystem(g, SYSTEM_BOUNDS);
		for (int i = 0; i < graphElements.size(); i++) {
			e = (GraphElement) graphElements.elementAt(i);
			if (e instanceof GraphPoint) {
				for (int j = 0; j < visibles.length; j++) {
					if ((visibles[j] == true) && (((GraphPoint) e).getColor() == colors[j])) {
						drawPoint(g, (GraphPoint) e);
					}
				}
			} else if (e instanceof GraphBar) {
				drawBar(g, (GraphBar) e);
			} else if (e instanceof GraphTick) {
				drawTick(g, (GraphTick) e);
			} else if (e instanceof GraphLine) {
				drawLine(g, (GraphLine) e);
			}
		}
		
		if (referenceLinesOn) { //Grid tracing is active
			int x = getXOldEvent();
			int y = getYOldEvent();
			
			if ((x <= convertX(graphstopx)) && (x >= convertX(graphstartx))) {
				g.setColor(Color.magenta);
				g.drawLine(x, convertY(graphstarty), x, convertY(graphstopy));
			}
			if ((y >= convertY(graphstopy)) && (y <= convertY(graphstarty))) {
				g.setColor(Color.magenta);
				g.drawLine(convertX(graphstartx), y, convertX(graphstopx), y);
			}
		}
		
		drawXLabel(g, xLabel, Color.black);
		drawYLabel(g, yLabel, Color.black);
		drawTitle(g, title, Color.black);
	}
}