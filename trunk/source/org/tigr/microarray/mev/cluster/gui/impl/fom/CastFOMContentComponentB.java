/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CastFOMContentComponentB.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:50:09 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

public class CastFOMContentComponentB extends JPanel implements java.io.Serializable {
    public static final long serialVersionUID = 202003030001L;    
    
    //private FOMGraph fomGraphA;
    private FOMGraph fomGraphB;
    private int[] numOfCastClusters;
    
    private float[] fom_values;
    float interval;
    float[] sortedFomValues;
    int[] sortedNumCastClusters;
    JPanel listPanel;

    private GridBagConstraints gbc;
    
    public CastFOMContentComponentB(float[] fom_values, float interval, int[] numOfCastClusters) { // take average by default
	setLayout(new GridBagLayout());
	//System.out.println("Entered CastFOMComponentB");
	this.fom_values = fom_values;
	this.interval = interval;
	this.numOfCastClusters = numOfCastClusters;
	this.sortedFomValues = createSortedFomValues(fom_values, numOfCastClusters);
	this.sortedNumCastClusters = createSortedClusterArray(numOfCastClusters);
	this.fomGraphB = new FOMGraph(sortedFomValues, null, "FOM value vs. # of Clusters", "# of Clusters", "Adjusted FOM", false);
	this.fomGraphB.setItems(createXItems(sortedFomValues.length), createYItems(sortedFomValues));
	this.fomGraphB.setMaxYValue((float)Math.ceil(getMaxValue(sortedFomValues)));
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 0.9;
	gbc.weighty = 1.0;
	add(this.fomGraphB, gbc);
	gbc.gridx = 1;
	gbc.weightx = 0.1;
	add(createValuesList(sortedFomValues, numOfCastClusters), gbc);
    }
    
    public static String[] getPersistenceDelegateArgs(){
    	return new String[] {"fom_values", "interval", "numOfCastClusters"};
    }
    public CastFOMContentComponentB(){
    	setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
    }

	/**
	 * @return Returns the fomGraphA.
	 */
	public FOMGraph getFomGraphB() {
		return fomGraphB;
	}
	/**
	 * @param fomGraphA The fomGraphA to set.
	 */
	public void setFomGraphB(FOMGraph fomGraphB) {
		gbc.gridx = 0;
		gbc.weightx = 0.9;
		this.fomGraphB = fomGraphB;
	}
    /*
    public BufferedImage getImageA() {
	return this.fomGraphA.getImage();
    }
     */
    public BufferedImage getImageB() {
	return this.fomGraphB.getImage();
    }
    
    private JComponent createValuesList(float[] fom_values, int[] numOfCastClusters) {
	String[] items = new String[fom_values.length];
	int[] sortedNumCastClusters = createSortedClusterArray(numOfCastClusters);
	for (int i=0; i<fom_values.length; i++) {
	    items[i] = String.valueOf(sortedNumCastClusters[i]) + "---->" + String.valueOf(Math.round(fom_values[i]*1000)/1000f);
	}
	JPanel listPanel = new JPanel(new GridBagLayout());
	JScrollPane scroll = new JScrollPane(new JList(items));
	
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	listPanel.add(scroll, gbc);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridy = 1;
	gbc.weighty = 0.0;
	JButton button = new JButton("Details");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		onDetails();
	    }
	});
	listPanel.add(button, gbc);
		this.listPanel = listPanel;
	return listPanel;
    }
    
    private void onDetails() {
	JFrame f = new JFrame("Details");
	JTextArea t = new JTextArea();
	t.setEditable(false);
	JScrollPane sp = new JScrollPane(t);
	StringBuffer sb = new StringBuffer();
	for (int i=0; i<sortedFomValues.length; i++) {
	    sb.append(String.valueOf(sortedNumCastClusters[i]) + "\t" + String.valueOf(Math.round(sortedFomValues[i]*1000)/1000f)+"\n");
	}
	t.setText(sb.toString());
	t.setCaretPosition(0);
	f.getContentPane().add(sp);
	f.setSize(200, 200);
	java.awt.Dimension screenSize = f.getToolkit().getScreenSize();
	f.setLocation(screenSize.width/2 - f.getSize().width/2, screenSize.height/2 - f.getSize().height/2);
	f.setVisible(true);
    }
    
    /*
    private JComponent createValuesList(float[] fom_values) {
	String[] items = new String[fom_values.length];
	float threshold = interval;
	for (int i=0; i<fom_values.length; i++) {
		String s = "0.";
		int d = (int)Math.floor(threshold*100);
		//System.out.println ("Inside CastFOMContentComponent.createXThresholdItems(): d = " + d);
		if (d < 10) s = s + "0";
		s = s + d;
		if (d >= 100) s = "1.00";
		threshold = threshold + interval;
	    items[i] = s + "---->" + String.valueOf(Math.round(fom_values[i]*1000)/1000f);
	}
	JPanel listPanel = new JPanel(new GridBagLayout());
	JScrollPane scroll = new JScrollPane(new JList(items));
     
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	listPanel.add(scroll, gbc);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridy = 1;
	gbc.weighty = 0.0;
	JButton button = new JButton("Details");
	button.addActionListener(new ActionListener() {
				     public void actionPerformed(ActionEvent e) {
					 onDetails();
				     }
				 });
	listPanel.add(button, gbc);
	return listPanel;
    }
     
    private void onDetails() {
	JFrame f = new JFrame("Details");
	JTextArea t = new JTextArea();
	t.setEditable(false);
	JScrollPane sp = new JScrollPane(t);
	StringBuffer sb = new StringBuffer();
	float threshold = interval;
	for (int i=0; i<fom_values.length; i++) {
		String s = "0.";
		int d = (int)Math.floor(threshold*100);
		//System.out.println ("Inside CastFOMContentComponent.createXThresholdItems(): d = " + d);
		if (d < 10) s = s + "0";
		s = s + d;
		if (d >= 100) s = "1.00";
		threshold = threshold + interval;
	    sb.append(s + "\t" + String.valueOf(Math.round(fom_values[i]*1000)/1000f)+"\n");
	}
	t.setText(sb.toString());
	t.setCaretPosition(0);
	f.getContentPane().add(sp);
	f.setSize(200, 200);
	java.awt.Dimension screenSize = f.getToolkit().getScreenSize();
	f.setLocation(screenSize.width/2 - f.getSize().width/2, screenSize.height/2 - f.getSize().height/2);
	f.setVisible(true);
    }
     */
    
    public void onSelected(IFramework framework) {
    	boolean test = framework.getDisplayMenu().isAntiAliasing();
	this.fomGraphB.setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
    }
    
    public void onMenuChanged(IDisplayMenu menu) {
	this.fomGraphB.setAntiAliasing(menu.isAntiAliasing());
    }
    
    private String[] createXItems(int iterations) {
	String[] items = new String[iterations];
	for (int i=0; i<items.length; i++) {
	    /*if ((i+1)%5 == 0 || i == 0 || i == items.length-1)*/ //{
	    items[i] = String.valueOf(sortedNumCastClusters[i]);
	    //}
	}
	return items;
    }
    
/*
    private String[] createXThresholdItems(int iterations) {
	String[] items = new String[iterations];
	float threshold = interval;
	for (int i=0; i<items.length; i++) {
	    //if ((i+1)%5 == 0 || i == 0 || i == items.length-1) {
		String s = "0.";
		int d = (int)Math.floor(threshold*100);
		//System.out.println ("Inside CastFOMContentComponent.createXThresholdItems(): d = " + d);
		if (d < 10) s = s + "0";
		s = s + d;
		if (d >= 100) s = "1.00";
		items[i] = s;//String.valueOf(threshold);
		threshold = threshold + interval;
		//System.out.println("Inside CastFOMContentComponent.createXThresholdItems(): threshold[" + i + "] =" + threshold);
	    //}
	}
	return items;
    }
 */
    
    private int[] createSortedClusterArray(int[] numOfCastClusters) {
	int[] localNumClusters = new int[numOfCastClusters.length];
	for (int i = 0; i < localNumClusters.length; i++) {
	    localNumClusters[i] = numOfCastClusters[i];
	}
		/*
		for (int i = 0; i < localNumClusters.length; i++) {
		    System.out.println("localNumClusters[" + i + "] =" + localNumClusters[i]);
		}
		 */
	//Arrays.sort(sortedNumClusters);
	Vector localVector = new Vector();
	for (int i = 0; i < localNumClusters.length; i++) {
	    localVector.add(new Integer(localNumClusters[i]));
	}
	
	HashSet s = new HashSet(localVector);
	
	Vector setVector = new Vector();
	
	for (Iterator it = s.iterator(); it.hasNext();) {
	    setVector.add(it.next());
	}
		/*
		for (int i = 0; i < setVector.size(); i++) {
		    System.out.println("setVector[" + i + "] = " + ((Integer)(setVector.get(i))).intValue());
		}
		 */
	int[] setArray = new int[setVector.size()];
	
	for (int i = 0; i < setArray.length; i++) {
	    
	    setArray[i] = ((Integer)(setVector.get(i))).intValue();
	}
	
	Arrays.sort(setArray);
	
	int[] lastElementDeductedArray = new int[setArray.length - 1];
	for (int i = 0; i < lastElementDeductedArray.length; i++) {
	    lastElementDeductedArray[i] = setArray[i];
	}
		/*
		for (int i = 0; i < lastElementDeductedArray.length; i++){
		    System.out.println("lastElementDeductedArray[" + i + "] = " + lastElementDeductedArray[i]);
		}
		 */
	return lastElementDeductedArray;
    }
    
    
    private float[] createSortedFomValues(float[] fom_values, int[] numOfCastClusters) {
	
	int[] sortedCastClusters = createSortedClusterArray(numOfCastClusters);
	//System.out.println("Finished createSortedClusterArray()");
	Vector sortedFomVector = new Vector();
	
	for (int i = 0; i < sortedCastClusters.length; i++) {
	    float currentFomValue = 0;
	    int count = 0;
	    for (int j = 0; j < numOfCastClusters.length; j++) {
		if (numOfCastClusters[j] == sortedCastClusters[i]) {
		    currentFomValue = currentFomValue + fom_values[j];
		    count++;
		}
	    }
	    currentFomValue = currentFomValue/count;
	    sortedFomVector.add(new Float(currentFomValue));
	}
	
	float[] sortedFomValues = new float[sortedFomVector.size()];
	for (int i = 0; i < sortedFomValues.length; i++) {
	    sortedFomValues[i] = ((Float)(sortedFomVector.get(i))).floatValue();
	}
	
	return sortedFomValues;
	
    }
    
    
    private float getMaxValue(float[] fom_values) {
	float max_value = -Float.MAX_VALUE;
	for (int i=0; i<fom_values.length; i++) {
	    max_value = Math.max(max_value, fom_values[i]);
	}
	return max_value;
    }
    
    private String[] createYItems(float[] fom_values) {
	int number_of_items = (int)Math.ceil(getMaxValue(fom_values))+1;
	String[] items = new String[number_of_items];
	for (int i=0; i<items.length; i++) {
	    if (i%2 == 0 || i == items.length-1) {
		items[i] = String.valueOf(i);
	    }
	}
	return items;
    }
	/**
	 * @return Returns the fom_values.
	 */
	public float[] getFom_values() {
		return fom_values;
	}
	/**
	 * @param fom_values The fom_values to set.
	 */
	public void setFom_values(float[] fom_values) {
		this.fom_values = fom_values;
	}
	/**
	 * @return Returns the interval.
	 */
	public float getInterval() {
		return interval;
	}
	/**
	 * @param interval The interval to set.
	 */
	public void setInterval(float interval) {
		this.interval = interval;
	}
    /////////////////////////////////////////////////////////////////
  /*  public static void main(String[] args) throws Exception {
	javax.swing.JFrame frame = new javax.swing.JFrame();
	frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
	float[] fom_values = new float[] {1.5f, 0.88888888888f, 0.6666666666f, 2.5f, 0.33333333333f, 0.0f};
	FOMContentComponent content = new FOMContentComponent(fom_values);
	frame.getContentPane().add(content);
	frame.setSize(700, 400);
	java.awt.Dimension screenSize = frame.getToolkit().getScreenSize();
	frame.setLocation(screenSize.width/2 - frame.getSize().width/2, screenSize.height/2 - frame.getSize().height/2);
	frame.setVisible(true);
    }
   **/
	/**
	 * @return Returns the numOfCastClusters.
	 */
	public int[] getNumOfCastClusters() {
		return numOfCastClusters;
	}
	/**
	 * @return Returns the sortedFomValues.
	 */
	public float[] getSortedFomValues() {
		return sortedFomValues;
	}
	/**
	 * @return Returns the sortedNumCastClusters.
	 */
	public int[] getSortedNumCastClusters() {
		return sortedNumCastClusters;
	}
}
