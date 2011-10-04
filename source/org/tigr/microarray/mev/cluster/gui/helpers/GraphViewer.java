/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CentroidViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.ui.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

public class GraphViewer extends JPanel implements IViewer {
	ChartPanel chartPanel;
	private CategoryDataset dataset;

	private static final long serialVersionUID = 1L;

	public GraphViewer(String chartTitle, double[][] doubleDataset, String[] seriesNames, String xAxisTitle, String xAxisLabel, String yAxisTitle, String yAxisLabel, int clusterMin) {
	    XYSeries[] series = new XYSeries[seriesNames.length];
        XYSeriesCollection dataset = new XYSeriesCollection();
	    for (int i=0; i<series.length; i++){
	        series[i] = new XYSeries(seriesNames[i]);
	        for (int j=0; j<doubleDataset[i].length; j++){
		        series[i].add(j+clusterMin, doubleDataset[i][j]);
	        }
	        dataset.addSeries(series[i]);
	    }
			
	    JFreeChart chart1 = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL,
	    		true, false, false);
		chartPanel = new ChartPanel(chart1);
	
	}

	public int[][] getClusters() {

		return null;
	}

	public JComponent getContentComponent() {

		return chartPanel;
	}

	public JComponent getCornerComponent(int cornerIndex) {

		return null;
	}

	public Experiment getExperiment() {

		return null;
	}

	public int getExperimentID() {

		return 0;
	}

	public Expression getExpression() {

		return null;
	}

	public JComponent getHeaderComponent() {

		return null;
	}

	public BufferedImage getImage() {

		return null;
	}

	public JComponent getRowHeaderComponent() {

		return null;
	}

	public int getViewerType() {

		return 0;
	}

	public void onClosed() {

	}

	public void onDataChanged(IData data) {

	}

	public void onDeselected() {

	}

	public void onMenuChanged(IDisplayMenu menu) {

	}

	public void onSelected(IFramework framework) {

	}

	public void setExperiment(Experiment e) {

	}

	public void setExperimentID(int id) {

	}

	public static void main(String[] args) {

		GraphViewer gv = new GraphViewer("title", new double[][]{{3.3,6.6},{1.1,8.8}},new String[]{"SA!","sdfsdf"},"xAxisTitle",  "xAxisLabel", "yAxisTitle",  "yAxisLabel", 4);
		JDialog jd = new JDialog();
		jd.add(gv.chartPanel);
		jd.setVisible(true);
	}

	private static CategoryDataset createDataset() {
		double[][] data = new double[][] { { 210, 300, 320, 265, 299, 200 },
				{ 200, 304, 201, 201, 340, 300 }, };
		return DatasetUtilities.createCategoryDataset("Team ", "Cluster", data);
	}

}
