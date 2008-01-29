/*
 * GDMClusterBrowserDialog.java
 *
 * Created on February 3, 2004, 1:22 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class GDMClusterBrowserDialog extends AlgorithmDialog {
   
    private int result = JOptionPane.CANCEL_OPTION;
    
    ClusterBrowser browser;
    Font font;
    EventListener listener;
    
    /** Creates a new instance of GDMClusterBrowserDialog */
    public GDMClusterBrowserDialog( ClusterRepository crep) {        
        super(new JFrame(), "GDM Cluster Selection", true);
        font = new Font("Dialog", Font.BOLD, 12);                
        listener = new EventListener();
        addWindowListener(listener);
        
        browser = new ClusterBrowser(crep);
        
        this.addContent(browser);
        this.setActionListeners(listener);
    }
    
        /** Shows the dialog.
     * @return  */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }

    /** Returns the cluster selected for analysis.
     * @return  */    
    public Cluster getSelectedCluster(){
        return this.browser.getSelectedCluster();
    }
    
    /** Resets dialog controls.
     */    
    private void resetControls(){
        
    }
    
     /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class EventListener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(GDMClusterBrowserDialog.this, "GDM Cluster Browser Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
        public void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        } 
        
    }
    
}
