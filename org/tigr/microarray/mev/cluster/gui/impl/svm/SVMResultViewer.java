/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMResultViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

abstract class SVMResultViewer extends JPanel implements IViewer {
    
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
    
    public void onSelected(IFramework frm) {
        onMenuChanged(frm.getDisplayMenu());
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

