/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * PasswordDialog.java
 *
 * Created on July 16, 2004, 2:49 PM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/** Collects user and password information.
 * @author braisted
 */
public class PasswordDialog extends AlgorithmDialog {
    
    private JTextField userField;
    private JPasswordField passField;
    protected int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of PasswordDialog
     * @param title Dialog title
     * @param userFieldLabel label for user field
     * @param user optional user name, can be null
     * @param password optional password, can be null
     */
    public PasswordDialog(String title, String userFieldLabel, String user, String password) {
        super(new JFrame(), title, true);
        JLabel userLabel = new JLabel(userFieldLabel);
        userLabel.setHorizontalAlignment(JLabel.RIGHT);
        userField = new JTextField(20);        
        if(user != null)
            userField.setText(user);
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setHorizontalAlignment(JLabel.RIGHT);
        passField = new JPasswordField(20);
        if(password != null) {
            passField.setText(password);
            
        }
        ParameterPanel panel = new ParameterPanel("User Login Information");
        panel.setLayout(new GridBagLayout());
        
        panel.add(userLabel, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(10,20,0,0), 0,0));
        panel.add(userField, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,0), 0,0));
        panel.add(passLabel, new GridBagConstraints(0,1,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(15,20,10,0), 0,0));
        panel.add(passField, new GridBagConstraints(1,1,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15,10,10,0), 0,0));
        
        addContent(panel);        
        if(password != null) {
         passField.selectAll();
            passField.grabFocus();   
        }
        pack();
    }
    
    /** Returns the password
     * @return  */
    public String getPassword() {
        return new String(passField.getPassword());
    }
    
    /** Returns the user name
     * @return
     */    
    public String getUserName() {
        return userField.getText().trim();
    }
    
    protected void resetControls() {
        this.userField.setText("");
        this.passField.setText("");
        this.userField.grabFocus();
    }
        /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    public static void main(String [] args) {
        PasswordDialog d = new PasswordDialog("LOLA User Login", "Email:", "braisted@tigr.org", "password");
        d.showModal();
        System.out.println("password ="+new String(d.getPassword()));
        
    }
}
