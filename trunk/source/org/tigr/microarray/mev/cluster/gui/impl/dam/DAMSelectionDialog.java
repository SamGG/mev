/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMSelectionDialog.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class DAMSelectionDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private SampleSelectionPanel sampleSelectionPanel;
    
    /** Creates new DAMSelectionDialog */
    public DAMSelectionDialog(Frame frame) {
        super(frame, "DAM: Principal Components Analysis", true);
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
                HelpWindow helpWindow = new HelpWindow(DAMSelectionDialog.this, "DAM Initialization Dialog");
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
        DAMSelectionDialog dialog = new DAMSelectionDialog(new Frame());
        int result = dialog.showModal();
        System.out.println("result = "+result);
        System.exit(0);
    }
    
}
