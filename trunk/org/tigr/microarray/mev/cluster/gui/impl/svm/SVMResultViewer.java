/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMResultViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2005-03-10 20:21:56 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

abstract class SVMResultViewer extends JPanel implements IViewer, java.io.Serializable {
    public static final long serialVersionUID = 202018070001L;
    
    // gui stuff
    protected JTextArea Log;
    protected JPanel resultPanel;
    private JPopupMenu MyPopup;
    private JMenuItem  menuItem1;
    protected int labelIndex;
    protected IFramework framework;
    private Experiment analysisExperiment;
    protected String [] fieldNames;
    JPanel panel1;
    
    public SVMResultViewer(IFramework framework) {
        panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        this.framework = framework;
        labelIndex = framework.getDisplayMenu().getLabelIndex();
        this.analysisExperiment = framework.getData().getExperiment();
        fieldNames = framework.getData().getFieldNames();
        BorderLayout borderLayout1 = new BorderLayout();
        BorderLayout borderLayout2 = new BorderLayout();
        JLabel imageControl1 = new JLabel();
        Log = new JTextArea();
        Log.setFont(new Font("monospaced", Font.PLAIN, 14));
        Log.setTabSize(3);
        Log.setEditable(false);
        Log.setAutoscrolls(true);
        Log.setBackground(new Color(208,208,208));
        Log.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Parameters"));

        this.setLayout(new GridBagLayout());
        this.add(Log,new GridBagConstraints(0,0,1,1,1.0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        MyPopup = new JPopupMenu();
        menuItem1 = new JMenuItem("Save classification...", GUIFactory.getIcon("save_as16.gif"));
        menuItem1.setActionCommand("save-result-command");
        menuItem1.addActionListener( new Listener()  );
        MyPopup.add(menuItem1);
        MyListener myListener = new MyListener();
        this.addMouseListener(myListener);
        this.addMouseMotionListener(myListener);
        Log.addMouseListener(myListener);
        Log.addMouseMotionListener(myListener);
    }

    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.analysisExperiment = (Experiment)ois.readObject();
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
        oos.writeObject(this.analysisExperiment);
        oos.writeObject(this.Log);
        oos.writeObject(this.resultPanel);
        oos.writeObject(this.fieldNames);
        oos.writeInt(this.labelIndex);
    }
        
    public abstract void onSelected(IFramework frm);
    
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
        labelIndex = framework.getDisplayMenu().getLabelIndex();
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
        return analysisExperiment.getGeneIndexMappedToData(row);
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
        return null;
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

