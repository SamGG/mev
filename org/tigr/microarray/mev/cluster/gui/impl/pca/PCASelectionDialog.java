/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * PCASelectionDialog.java
 *
 * Created on February 27, 2003, 11:05 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.pca;

//import javax.swing.JDialog;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class PCASelectionDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private SampleSelectionPanel sampleSelectionPanel;
    
    /** Creates new PCASelectionDialog */
    public PCASelectionDialog(Frame frame) {
        super(new JFrame(), "PCA: Principal Components Analysis", true);
        setSize(300, 110);
        super.setResizable(false);
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        setActionListeners(new EventListener());
        addContent(sampleSelectionPanel);
        pack();
    }
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    public boolean isClusterGenesSelected(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    public void resetControls(){
        sampleSelectionPanel.setClusterGenesSelected(true);
    }
    
    protected class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            //  Object source = event.getSource();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(PCASelectionDialog.this, "PCA Initialization Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 350);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
    }
    
    public static void main(String [] args){
        PCASelectionDialog dialog = new PCASelectionDialog(new Frame());
        int result = dialog.showModal();
        System.out.println("result = "+result);
        System.exit(0);
    }
    
}
