/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * SAMDeltaInfoViewer.java
 *
 * Created on January 28, 2003, 1:17 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAMDeltaInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content; 
    private double[] deltaGrid, medNumFalse,false90th, FDRMedian, FDR90th;
    private int[] numSig;
    private JPopupMenu popup;
    
    //private SAMState localSAMState;

    /** Creates new SAMDeltaInfoViewer */
    public SAMDeltaInfoViewer(double[] deltaGrid, double[] medNumFalse, double[] false90th, int[] numSig, double[] FDRMedian, double[] FDR90th) {
        this.deltaGrid = deltaGrid;
        this.medNumFalse = medNumFalse;
        this.false90th = false90th;
        this.numSig = numSig;
        this.FDRMedian = FDRMedian;
        this.FDR90th = FDR90th;
        /*
        this.localSAMState = new SAMState();
        
        System.out.println("Before new SAMState(): ");
        System.out.println("SAMState.firstRun = " + SAMState.firstRun);
        System.out.println("SAMState.groupAssignments.length = " + SAMState.groupAssignments.length);
        SAMState localSAMState = new SAMState(); 
        System.out.println("After new SAMState(): ");        
        System.out.println("localSAMState.firstRun = " + localSAMState.firstRun);
        System.out.println("localSAMState.groupAssignments.length = " + localSAMState.groupAssignments.length); 
             
         */  
	header  = createHeader();
	content = createContent();
	setMaxWidth(content, header);   

    }
    
    public SAMDeltaInfoViewer(JTextArea content, JComponent header){
    	this.header = header;
    	this.content = content;
        setMaxWidth(content, header);
    }
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
	return content;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
	return header;
    }
    
    /**
     * Creates the viewer header.
     */
    /*
    private JComponent createHeader() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBackground(Color.white);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = new Insets(10, 0, 10, 0);
	panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Delta Table</b></font></body></html>"), gbc);
        //panel.add(new JLabel("Delta\tMedian false\t90th %ile false\t  # sig. genes\tFDR(%) Median\t   FDR(%) 90th %ile\n\n"), gbc);
	return panel;
    } 
     */
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
	
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }    
    
    private JComponent createHeader() {
	JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
	panel.setBackground(Color.white);
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.insets = new Insets(10, 200, 10, 200);
	JLabel label1 = new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Delta Table</b></font></body></html>");
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        //constraints.fill = GridBagConstraints.BOTH;
        //constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(label1, constraints);
        panel.add(label1);
        
        JTextArea area = new JTextArea();
        area.setEditable(false);
        StringBuffer sb = new StringBuffer();
        sb.append("  Delta\tMedian false\t90th %ile false\t  # sig. genes\tFDR(%) Median\t   FDR(%) 90th %ile");
        area.setForeground(Color.blue);
        area.setText(sb.toString());
	area.setCaretPosition(0);
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.insets = new Insets(10, 0, 0, 0);
        //constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(area, constraints);
        panel.add(area);        
        
	return panel;
    }    
    
    private JTextArea createContent() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        StringBuffer sb = new StringBuffer();
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);        
        //sb.append("Delta\tMedian false\t90th %ile false\t  # sig. genes\tFDR(%) Median\t   FDR(%) 90th %ile\n\n");
        //counter = 0;
        for (int counter = 0; counter < deltaGrid.length; counter++) {            
            sb.append("  " + printFormat(deltaGrid[counter]) + "\t" + printFormat(medNumFalse[counter]) + "\t" + printFormat(false90th[counter]) + "\t  " + numSig[counter] + "\t" + printFormat(FDRMedian[counter]) + "\t   " + printFormat(FDR90th[counter]) + "\n");
            //counter += 100;
        }
        area.setText(sb.toString());
	area.setCaretPosition(0);

        final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
        fc.setDialogTitle("Save delta table");
        
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Save delta table", GUIFactory.getIcon("save16.gif"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(SAMDeltaInfoViewer.this.getHeaderComponent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            out.print("Delta\tMedian false\t90th %ile false\t# sig. genes\tFDR(%) Median\tFDR(%) 90th %ile\n\n");
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < deltaGrid.length; i++) {
                                //out.print(groupAssgn[i]);
                                out.print(deltaGrid[i] + "\t" + medNumFalse[i] + "\t" + false90th[i] + "\t" + numSig[i] + "\t" + FDRMedian[i] + "\t" + FDR90th[i] + "\n");
                            }
                            out.println();
                            out.flush();
                            out.close();
                       } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }                
            }
        });
        
        popup.add(menuItem);
        
        area.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(),
                    e.getX(), e.getY());
                }
            }            
            /*
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    //System.out.println("Right clicked");
                    fc.setDialogTitle("Save delta table");
                    int returnVal = fc.showSaveDialog(SAMDeltaInfoViewer.this.getHeaderComponent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            out.print("Delta\tMedian false\t90th %ile false\t# sig. genes\tFDR(%) Median\tFDR(%) 90th %ile");
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < deltaGrid.length; i++) {
                                //out.print(groupAssgn[i]);
                                out.print(deltaGrid[i] + "\t" + medNumFalse[i] + "\t" + false90th[i] + "\t" + numSig[i] + "\t" + FDRMedian[i] + "\t" + FDR90th[i] + "\n");
                            }
                            out.println();
                            out.flush();
                            out.close();
                       } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }                     
                    
                }
            }
             */
        });
        
	return area;        
    }

    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
	int c_width = content.getPreferredSize().width;
	int h_width = header.getPreferredSize().width;
	if (c_width > h_width) {
	    header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
	} else {
	    content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
	}
    } 
    
    private String printFormat(double d) {
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3); 
        if (Double.isNaN(d)) {
            return "N/A";
        } else {
            return nf.format(d);
        }
    }
    
}
