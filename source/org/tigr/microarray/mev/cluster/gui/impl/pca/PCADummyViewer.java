/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * PCADummyViewer.java
 *
 * Created on December 16, 2004, 10:47 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 */
public class PCADummyViewer extends ViewerAdapter {
   
    private static final String ADD_NEW_3D_CMD = "add-new-3d-cmd";
    private static final String ADD_NEW_2D_CMD = "add-new-2d-cmd";    
    private JPopupMenu popup;
    private IFramework framework;    
    private FloatMatrix U, S;
    private int mode;
    //private Experiment experiment;    
    
    /** Creates a new instance of PCADummyViewer */
    public PCADummyViewer(FloatMatrix U, FloatMatrix S, int mode) {
        //this.experiment = experiment;
        this.U = U;
        this.S = S;
        this.mode = mode;
        popup = createJPopupMenu();
    }
    
    public PCADummyViewer(FloatMatrix U, FloatMatrix S, Integer mode){
    	this(U, S, mode.intValue());

    }    
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{U, S, new Integer(mode)});
    }  
    
    public void onSelected(IFramework framework) {
        this.framework = framework;        
        //In case it is viewed after serialization
        if(popup == null){
            popup = createJPopupMenu(); 
            DefaultMutableTreeNode node = framework.getCurrentNode();
            if(node != null){
                if(node.getUserObject() instanceof LeafInfo){
                    LeafInfo leafInfo = (LeafInfo) node.getUserObject();
                    leafInfo.setPopupMenu(this.popup);
                }
            }
        }          
    } 
    
   /**
     * Returns the viewer popup menu.
     */
    public JPopupMenu getJPopupMenu() {
        return popup;
    }   
    
    /**
     * Creates the viewer popup menu.
     */
    private JPopupMenu createJPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup);
        return popup;
    }    
    
    private void addMenuItems(JPopupMenu menu) {  
        Listener listener = new Listener();
        
        JMenuItem menuItem;
        menuItem = new JMenuItem("Add new 3-axis projections");
        menuItem.setActionCommand("add-new-3d-cmd");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
                
        menuItem = new JMenuItem("Add new 2-axis projections");
        menuItem.setActionCommand("add-new-2d-cmd");
        menuItem.addActionListener(listener);        
        menu.add(menuItem);    
    }  
    
    /**
     * Returns a menu item by specified action command.
     */
    private JMenuItem getJMenuItem(String command) {
        JMenuItem item;
        Component[] components = popup.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }    
    
   private void add2DViewNode(DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
       boolean geneViewer = false;
       
       if (mode == 1) 
           geneViewer = true;
       else if (mode == 3)
           geneViewer = false;
       
       PCA2DViewer pcaxy = new PCA2DViewer(experiment, U, geneViewer, xAxis, yAxis);
       PCA2DViewer pcayz = new PCA2DViewer(experiment, U, geneViewer, yAxis, zAxis);
       PCA2DViewer pcaxz = new PCA2DViewer(experiment, U, geneViewer, xAxis, zAxis);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), pcaxy, pcaxy.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), pcayz, pcayz.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), pcaxz, pcaxz.getJPopupMenu()))); 
   }    
   
    private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
        if (U == null || U.getColumnDimension() < 3) {
            return;
        }
        PCA3DViewer pca3DViewer;
        if(mode == 1)
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, true, xAxis, yAxis, zAxis);
        else
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, false, xAxis, yAxis, zAxis);
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("3D view", pca3DViewer, pca3DViewer.getJPopupMenu())));
    }   
    
    private void addNew3DNode() {
        if (S == null) {
            return;
        }
        PCAAdditional3DAxesDialog pd = new PCAAdditional3DAxesDialog((JFrame)framework.getFrame(), true, S.getRowDimension());
        pd.setVisible(true);
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();
            int selectedZ = pd.getZAxis();
            
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1) + ", " + (selectedZ + 1));
            add3DViewNode(framework.getFrame(), newNode, framework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            DefaultMutableTreeNode twoDNode = new DefaultMutableTreeNode("2D Views");
            add2DViewNode(twoDNode, framework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            newNode.add(twoDNode);   
            framework.addNode(framework.getCurrentNode(), newNode);
        }
    }
    
    private void addNew2DNode() {
        if (S == null) {
            return;
        }
        PCAAdditional3DAxesDialog pd = new PCAAdditional3DAxesDialog((JFrame)framework.getFrame(), true, S.getRowDimension());
        pd.setZBoxInvisible(true);
        pd.setVisible(true);  
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();
            //DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1));
            boolean geneViewer = false;            
            if (mode == 1)
                geneViewer = true;
            else if (mode == 3)
                geneViewer = false;
            
            PCA2DViewer pcaxy = new PCA2DViewer(framework.getData().getExperiment(), U, geneViewer, selectedX, selectedY); 
            framework.addNode(framework.getCurrentNode(), new DefaultMutableTreeNode(new LeafInfo("Components " + (selectedX + 1) + ", " + (selectedY + 1), pcaxy, pcaxy.getJPopupMenu())));
        }
    }    
    
    /**
     * The class to listen to dialog and algorithm events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("add-new-3d-cmd")) {
                addNew3DNode();
            } else if (command.equals("add-new-2d-cmd")) {
                addNew2DNode();
            }
        }
        
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
            //System.out.println("maybeShowPopup");
	    //if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
           if (!e.isPopupTrigger()) { 
		return;
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}          
        
    }     
    
}
