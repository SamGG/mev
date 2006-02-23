/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * COADummyViewer.java
 *
 * Created on December 15, 2004, 2:07 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
public class COADummyViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202022010001L;
    
    private static final String ADD_NEW_3D_CMD = "add-new-3d-cmd";
    private static final String ADD_NEW_2D_CMD = "add-new-2d-cmd";    
    private JPopupMenu popup;
    private IFramework framework;    
    private FloatMatrix geneUMatrix, exptUMatrix;
    private Experiment experiment;    
    
    /** Creates a new instance of COADummyViewer */
    public COADummyViewer(FloatMatrix geneUMatrix, FloatMatrix exptUMatrix) {
        //no effect this.experiment = experiment;        
        this.geneUMatrix = geneUMatrix;
        this.exptUMatrix = exptUMatrix;
        popup = createJPopupMenu();        
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(this.experiment);        
        //oos.defaultWriteObject();
        oos.writeObject(this.geneUMatrix);
        oos.writeObject(this.exptUMatrix);
        //oos.writeObject();;
        
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.experiment = (Experiment)ois.readObject();        
        this.geneUMatrix = (FloatMatrix)ois.readObject();
        this.exptUMatrix = (FloatMatrix)ois.readObject();
        //ois.readObject();
        //ois.defaultReadObject();
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
       COA2DViewer geneViewer, exptViewer, bothViewer;
       DefaultMutableTreeNode genes = new DefaultMutableTreeNode("2D views - genes");
       DefaultMutableTreeNode expts = new DefaultMutableTreeNode("2D views - expts");
       DefaultMutableTreeNode both = new DefaultMutableTreeNode("2D views - both");
       
       COA2DViewer coaxy = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, xAxis, yAxis);
       COA2DViewer coayz = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, yAxis, zAxis);
       COA2DViewer coaxz = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, xAxis, zAxis);
       
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), coaxy, coaxy.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), coayz, coayz.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), coaxz, coaxz.getJPopupMenu())));
       
       COA2DViewer coaExptsxy = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, xAxis, yAxis);
       COA2DViewer coaExptsyz = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, yAxis, zAxis);
       COA2DViewer coaExptsxz = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, xAxis, zAxis);
       
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), coaExptsxy, coaExptsxy.getJPopupMenu())));    
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), coaExptsyz, coaExptsyz.getJPopupMenu()))); 
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), coaExptsxz, coaExptsxz.getJPopupMenu()))); 
       
       COA2DViewer bothxy = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, xAxis, yAxis);
       COA2DViewer bothyz = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, yAxis, zAxis);
       COA2DViewer bothxz = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, xAxis, zAxis);
       
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), bothxy, bothxy.getJPopupMenu())));
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), bothyz, bothyz.getJPopupMenu())));       
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), bothxz, bothxz.getJPopupMenu())));    
       
       node.add(genes);
       node.add(expts);
       node.add(both);
   }   
   
   private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
       COA3DViewer coa3DGeneViewer, coa3DExptViewer, coa3DBothViewer;
       coa3DGeneViewer = new COA3DViewer(frame, geneUMatrix, experiment, COAGUI.GENES, xAxis, yAxis, zAxis);
       coa3DExptViewer = new COA3DViewer(frame, exptUMatrix, experiment, COAGUI.EXPTS, xAxis, yAxis, zAxis);
       coa3DBothViewer = new COA3DViewer(frame, geneUMatrix, exptUMatrix, experiment, COAGUI.BOTH, xAxis, yAxis, zAxis);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - genes", coa3DGeneViewer, coa3DGeneViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - expts", coa3DExptViewer, coa3DExptViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - both", coa3DBothViewer, coa3DBothViewer.getJPopupMenu())));
   }      
    
   public void addNew3DNode() {
        
       COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog((JFrame)framework.getFrame(), true, geneUMatrix.getColumnDimension());
       //COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog(new JFrame(), true, geneUMatrix.getColumnDimension());
        pd.setVisible(true);      
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();
            int selectedZ = pd.getZAxis();
            
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1) + ", " + (selectedZ + 1));
            DefaultMutableTreeNode threeDNode = new DefaultMutableTreeNode("3D Views");
            add3DViewNode(framework.getFrame(), threeDNode, framework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            //add3DViewNode(new JFrame(), newNode, framework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            DefaultMutableTreeNode twoDNode = new DefaultMutableTreeNode("2D Views");
            add2DViewNode(twoDNode, framework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            newNode.add(threeDNode);
            newNode.add(twoDNode);   
            framework.addNode(framework.getCurrentNode(), newNode);
        }        
   }
   
   public void addNew2DNode() {
        COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog((JFrame)framework.getFrame(), true, geneUMatrix.getColumnDimension());
       //COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog(new JFrame(), true, geneUMatrix.getColumnDimension());
        pd.setZBoxInvisible(true);
        pd.setVisible(true);  
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();  
            
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1));   
            COA2DViewer coaGenexy = new COA2DViewer(framework.getData().getExperiment(), geneUMatrix, COAGUI.GENES, selectedX, selectedY);
            COA2DViewer coaExptxy = new COA2DViewer(framework.getData().getExperiment(), exptUMatrix, COAGUI.EXPTS, selectedX, selectedY);
            COA2DViewer coaBothxy = new COA2DViewer(framework.getData().getExperiment(), geneUMatrix, exptUMatrix, COAGUI.BOTH, selectedX, selectedY);
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Genes", coaGenexy, coaGenexy.getJPopupMenu())));
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Expts", coaExptxy, coaExptxy.getJPopupMenu())));
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Both", coaBothxy, coaBothxy.getJPopupMenu())));
            framework.addNode(framework.getCurrentNode(), newNode);
            //add2DViewNode(newNode, );
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
