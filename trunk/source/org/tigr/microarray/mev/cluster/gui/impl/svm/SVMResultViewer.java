/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SVMResultViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.tigr.microarray.mev.cluster.gui.*;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

abstract class SVMResultViewer extends JPanel implements IViewer {
    
    private JPopupMenu MyPopup;
    private JMenuItem  menuItem1;
    protected int labelIndex;
    protected IFramework framework;
    protected IData iData;
    private Experiment experiment;
    protected String [] fieldNames;
    protected String annotationLabel = "";
    private int exptID = 0;
    
    JTable resultTable;
    JScrollPane jsp;

    public SVMResultViewer(Experiment e) {
        this.experiment = e;
        this.exptID = experiment.getId();
        this.setAutoscrolls(true);
        this.setLayout(new GridBagLayout());
        resultTable = new JTable();
        resultTable.setPreferredScrollableViewportSize(new Dimension(700,500));
        resultTable.setAutoResizeMode(3);
        resultTable.setFont(new Font("monospaced", Font.PLAIN, 12));
        MyPopup = new JPopupMenu();
        menuItem1 = new JMenuItem("Save classification...", GUIFactory.getIcon("save_as16.gif"));
        menuItem1.setActionCommand("save-result-command");
        menuItem1.addActionListener( new Listener()  );
        MyListener listener = new MyListener();
        resultTable.addMouseListener(listener);
        resultTable.addMouseMotionListener(listener);
        MyPopup.add(menuItem1);
    }
    /**
     * @inheritDoc
     * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment});
    }

    /*
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.experiment = (Experiment)ois.readObject();
        this.Log = (JTextArea)ois.readObject();
        this.resultPanel = (JPanel)ois.readObject();
        this.fieldNames = (String [])ois.readObject();
        this.labelIndex = ois.readInt();
        
        MyListener listener = new MyListener();
        MyPopup = new JPopupMenu();
        menuItem1 = new JMenuItem("Save classification...", GUIFactory.getIcon("save_as16.gif"));
        menuItem1.setActionCommand("save-result-command");
        menuItem1.addActionListener( new Listener()  );
        MyPopup.add(menuItem1);
        getContentComponent().addMouseListener(listener);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { 
        oos.writeObject(this.experiment);
        oos.writeObject(this.Log);
        oos.writeObject(this.resultPanel);
        oos.writeObject(this.fieldNames);
        oos.writeInt(this.labelIndex);
    }
    */
    public void setExperimentID(int i){this.exptID = i;}
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    	this.exptID = e.getId();
    }
    public int getExperimentID(){return exptID;}
    public void onSelected(IFramework frm){
    	this.framework = frm;
        this.iData = frm.getData();
        annotationLabel = framework.getData().getFieldNames()[framework.getDisplayMenu().getLabelIndex()];
    }
    
    protected abstract void displayData();
    
    protected abstract void onSaveResult();
    
    // IViewer interface
    public JComponent getContentComponent() {
        return this;
    }
    
    public JComponent getHeaderComponent() {
        return null;
    }
    
    public BufferedImage getImage() {
        return null;
    }
    

    
    public void onDataChanged(IData data) {}
    
    public void onMenuChanged(IDisplayMenu menu) {
        try {
            annotationLabel = framework.getData().getFieldNames()[framework.getDisplayMenu().getLabelIndex()];
        labelIndex = framework.getDisplayMenu().getLabelIndex();
        } catch (NullPointerException npe){
        	labelIndex = 0;
        	annotationLabel = "";
        }
        displayData();
        updateSize();
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    protected abstract Dimension updateSize();
    
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index
     */
    protected int getMultipleArrayDataRow(int row) {
        return experiment.getGeneIndexMappedToData(row);
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }

    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return this.experiment;
    }    
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    // GUI Listener class helpers
    class MyListener extends MouseInputAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                MyPopup.show(e.getComponent(),e.getX(),e.getY());
            }
        }
    }
    
    class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            AbstractButton source = (AbstractButton)e.getSource();
            if (source.getActionCommand().equals("save-result-command")) {
                onSaveResult();
            }
        }
    }
}

