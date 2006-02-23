/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CastFOMContentComponentA.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

public class CastFOMContentComponentA extends JPanel implements java.io.Serializable {
    public static final long serialVersionUID = 202003020001L;
    
    private FOMGraph fomGraphA;
    private FOMGraph fomGraphB;
    
    private float[] fom_values;
    float interval;
    
    public CastFOMContentComponentA(float[] fom_values, float interval, int[] numOfCastClusters) { // take average by default
	setLayout(new GridBagLayout());
	this.fom_values = fom_values;
	this.interval = interval;
	this.fomGraphA = new FOMGraph(fom_values, null, "FOM value vs. Threshold", "Threshold", "Adjusted FOM", false);
	this.fomGraphA.setItems(createXThresholdItems(fom_values.length), createYItems(fom_values));
	this.fomGraphA.setMaxYValue((float)Math.ceil(getMaxValue(fom_values)));
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 0.9;
	gbc.weighty = 1.0;
	add(this.fomGraphA, gbc);
	gbc.gridx = 1;
	gbc.weightx = 0.1;
	add(createValuesList(fom_values), gbc);
    }
    
    public BufferedImage getImageA() {
	return this.fomGraphA.getImage();
    }
    
    public BufferedImage getImageB() {
	return this.fomGraphB.getImage();
    }
    
    
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
    
    public void onSelected(IFramework framework) {
	this.fomGraphA.setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
    }
    
    public void onMenuChanged(IDisplayMenu menu) {
	this.fomGraphA.setAntiAliasing(menu.isAntiAliasing());
    }
    
    private String[] createXItems(int iterations) {
	String[] items = new String[iterations];
	for (int i=0; i<items.length; i++) {
	    if ((i+1)%5 == 0 || i == 0 || i == items.length-1) {
		items[i] = String.valueOf(i+1);
	    }
	}
	return items;
    }
    
    
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
    
    /////////////////////////////////////////////////////////////////
 /*   public static void main(String[] args) throws Exception {
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
  */
}
