/*
 * ListOrderDialog.java
 *
 * Created on May 14, 2004, 12:15 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


/**
 *
 * @author  braisted
 */
public class ListOrderDialog extends AlgorithmDialog {
    
    int result = JOptionPane.CANCEL_OPTION;
    JList list;
    Vector data;
    
    /** Creates a new instance of ListOrderDialog */
    public ListOrderDialog(JFrame parent, JList list, Vector data, String listTitle, String message) {
        super(parent, "List Sorter", true);
        this.list = list;
        this.data = data;
        EventListener listener = new EventListener();
        
        ParameterPanel parameters = new ParameterPanel(listTitle);
        list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        parameters.setLayout(new GridBagLayout());
        JScrollPane pane = new JScrollPane(list);
     //   pane.set
        parameters.add(pane, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,20,20,0), 0,0));

        ParameterPanel buttonPanel = new ParameterPanel("Move");
        buttonPanel.setLayout(new GridBagLayout());
        JButton upButton = new JButton(GUIFactory.getIcon("arrow_up.gif"));
        upButton.setFocusPainted(false);
        upButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        upButton.setPreferredSize(new Dimension(30, 50));
        upButton.setActionCommand("shift-up-command");
        upButton.addActionListener(listener);       
        
        JButton downButton = new JButton(GUIFactory.getIcon("arrow_down.gif"));
        downButton.setFocusPainted(false);
        downButton.setPreferredSize(new Dimension(30, 50));
        downButton.setActionCommand("shift-down-command");
        downButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        downButton.addActionListener(listener);
        
        buttonPanel.add(upButton, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,2,20), 0,0));
        buttonPanel.add(downButton, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,0,0,20), 0,0));

        parameters.add(buttonPanel, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20,5,20,20), 0,0));

        addContent(parameters);
        setActionListeners(listener);
        pack();
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
    
    public Vector getSortedVector() {
        return data;
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
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(ListOrderDialog.this, "List Sorter Dialog");
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
            } else if (command.equals("shift-up-command")) {
                int index = list.getSelectedIndex();
                if(index == 0)
                    return;                
                Object obj = list.getSelectedValue();
                data.remove(obj);
                data.insertElementAt(obj, index-1);
                list.setSelectedIndex(index-1);
            } else if (command.equals("shift-down-command")) {
                int index = list.getSelectedIndex();
                if(index == data.size()-1)
                    return;
                Object obj = list.getSelectedValue();
                data.remove(obj);
                data.insertElementAt(obj, index+1);
                list.setSelectedIndex(index+1);
            }
        }
        
        public void itemStateChanged(ItemEvent e) {
            //okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    
    
    
    public static void main(String [] args) {
        Vector data = new Vector();
        data.add("Label1");
        data.add("Label2");
        data.add("Label3");
        JList list = new JList(data);
        ListOrderDialog dialog = new ListOrderDialog(new JFrame(), list, data, "Sample Label Keys", "Please Arrange Keys in Desired order.");
        dialog.showModal();
    }
    
}
