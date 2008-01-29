/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * LOLAPasswordDialog.java
 *
 * Created on July 16, 2004, 3:57 PM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit.lola;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.clusterUtil.submit.PasswordDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class LOLAPasswordDialog extends PasswordDialog {
    
    /** Creates a new instance of LOLAPasswordDialog */
    public LOLAPasswordDialog(String email, String password) {
        super("LOLA Login", "Email:", email, password);
        setActionListeners(new Listener());
    }
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
                return;
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
                return;
            }
            else if (command.equals("reset-command")) {
                resetControls();
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(LOLAPasswordDialog.this, "LOLA Login Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
    }
           
}
