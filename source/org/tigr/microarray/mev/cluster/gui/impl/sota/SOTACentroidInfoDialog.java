/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTACentroidInfoDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:55 $
 * $Author: caliente $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JViewport;

/**
 *
 * @author  braisted
 */
public class SOTACentroidInfoDialog extends javax.swing.JDialog {
    
    private int INFO_PANEL_WIDTH = 300;
    private int clusterNumber;
    private SOTAExperimentViewer viewer;
    /** Creates new form SOTACentroidInfoDialog */
    public SOTACentroidInfoDialog(java.awt.Frame parent, boolean modal, int c1, int clusterPop1, float div1, int c2, int clusterPop2, float div2, float dist,
    SOTAExperimentViewer viewer) {
	
	super(parent, modal);
	initComponents();
	clusterNumber = c1;
	this.viewer = viewer;
	
	// this.jSplitPane1.setDividerLocation(viewer.getContentComponent().getWidth());
	
	this.viewerPane.setViewportView(viewer.getContentComponent());
	
	JComponent header = viewer.getHeaderComponent();
	
	if(header != null)
	    this.viewerPane.setColumnHeaderView(header);
	else
	    this.viewerPane.setColumnHeader(null);
	
	viewerPane.doLayout();
	viewerPane.validate();
	
	this.c1Label.setText(String.valueOf(c1+1));
	this.c1PopLabel.setText(String.valueOf(clusterPop1));
	this.c1DivLabel.setText(String.valueOf(div1));
	this.distLabel.setText(String.valueOf(dist));
	
	this.c2Label.setText(String.valueOf(c2+1));
	this.c2PopLabel.setText(String.valueOf(clusterPop2));
	this.c2DivLabel.setText(String.valueOf(div2));
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	infoPanel.setSize( INFO_PANEL_WIDTH , (int)(screenSize.height/2));
	setSize((int)(viewer.getContentComponent().getWidth()+ INFO_PANEL_WIDTH + jSplitPane1.getDividerSize() ), (int)(screenSize.height/2));
	this.jSplitPane1.setDividerLocation(viewer.getContentComponent().getWidth() + 50);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jSplitPane1 = new javax.swing.JSplitPane();
        viewerPane = new javax.swing.JScrollPane();
        infoPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        c1Label = new javax.swing.JLabel();
        c1PopLabel = new javax.swing.JLabel();
        c1DivLabel = new javax.swing.JLabel();
        distLabel = new javax.swing.JLabel();
        c2Label = new javax.swing.JLabel();
        c2DivLabel = new javax.swing.JLabel();
        c2PopLabel = new javax.swing.JLabel();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        jSplitPane1.setDividerLocation(450);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(100, 100));
        jSplitPane1.setLastDividerLocation(300);
        jSplitPane1.setAlignmentY(1.0F);
        jSplitPane1.setAlignmentX(1.0F);
        jSplitPane1.setLeftComponent(viewerPane);
        
        infoPanel.setLayout(null);
        
        infoPanel.setBackground(java.awt.Color.lightGray);
        infoPanel.setAlignmentY(1.0F);
        infoPanel.setAlignmentX(1.0F);
        infoPanel.setOpaque(false);
        jLabel10.setText("Cluster ID#:");
        jLabel10.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel10);
        jLabel10.setBounds(20, 30, 65, 17);
        
        jLabel11.setText("Cluster Population:");
        jLabel11.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel11);
        jLabel11.setBounds(20, 60, 107, 17);
        
        jLabel12.setText("Cluster Diversity:");
        jLabel12.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel12);
        jLabel12.setBounds(20, 90, 96, 17);
        
        jLabel13.setText("Distance to");
        jLabel13.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel13);
        jLabel13.setBounds(20, 130, 64, 17);
        
        jLabel14.setText("Closest Neighbor:");
        jLabel14.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel14);
        jLabel14.setBounds(20, 150, 100, 17);
        
        jLabel15.setText("Neighbor ID#:");
        jLabel15.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel15);
        jLabel15.setBounds(20, 190, 75, 17);
        
        jLabel16.setText("Neighbor Population:");
        jLabel16.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel16);
        jLabel16.setBounds(20, 220, 117, 17);
        
        jLabel17.setText("Neighbor Diversity:");
        jLabel17.setForeground(java.awt.Color.black);
        infoPanel.add(jLabel17);
        jLabel17.setBounds(20, 250, 106, 17);
        
        c1Label.setForeground(java.awt.Color.black);
        infoPanel.add(c1Label);
        c1Label.setBounds(150, 30, 70, 20);
        
        c1PopLabel.setForeground(java.awt.Color.black);
        infoPanel.add(c1PopLabel);
        c1PopLabel.setBounds(150, 60, 70, 20);
        
        c1DivLabel.setForeground(java.awt.Color.black);
        infoPanel.add(c1DivLabel);
        c1DivLabel.setBounds(150, 90, 70, 20);
        
        distLabel.setForeground(java.awt.Color.black);
        infoPanel.add(distLabel);
        distLabel.setBounds(150, 150, 70, 20);
        
        c2Label.setForeground(java.awt.Color.black);
        infoPanel.add(c2Label);
        c2Label.setBounds(150, 190, 70, 20);
        
        c2DivLabel.setForeground(java.awt.Color.black);
        infoPanel.add(c2DivLabel);
        c2DivLabel.setBounds(150, 250, 70, 20);
        
        c2PopLabel.setForeground(java.awt.Color.black);
        infoPanel.add(c2PopLabel);
        c2PopLabel.setBounds(150, 220, 70, 20);
        
        jSplitPane1.setRightComponent(infoPanel);
        
        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);
        
        pack();
    }//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
	setVisible(false);
	dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	//  new SOTACentroidInfoDialog(new javax.swing.JFrame(), true).show();
    }
    
    public void showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return;
    }
    private void closeDialog(){
	setVisible(false);
	dispose();
    }
    
    public void closeCurrentDialog(){
	closeDialog();
    }
    
    private void doViewLayout() {
	JViewport header = viewerPane.getColumnHeader();
	if (header != null) {
	    header.doLayout();
	}
	viewerPane.getViewport().doLayout();
	viewerPane.doLayout();
	viewerPane.repaint();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane viewerPane;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel c1Label;
    private javax.swing.JLabel c1PopLabel;
    private javax.swing.JLabel c1DivLabel;
    private javax.swing.JLabel distLabel;
    private javax.swing.JLabel c2Label;
    private javax.swing.JLabel c2DivLabel;
    private javax.swing.JLabel c2PopLabel;
    // End of variables declaration//GEN-END:variables
    
}
