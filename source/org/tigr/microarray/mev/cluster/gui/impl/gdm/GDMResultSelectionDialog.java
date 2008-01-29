/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
* $RCSfile: GDMResultSelectionDialog.java,v $
* $Revision: 1.4 $
* $Date: 2005-12-06 16:29:15 $
* $Author: wwang67 $
* $State: Exp $
*/
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.QSort;

public class GDMResultSelectionDialog extends AlgorithmDialog {

    private int result;
    private JList resultList;

  
    /**
     * Constructs a <code>GDMInitDialog</code> with default
     * initial parameters.
     */
    public GDMResultSelectionDialog(JFrame frame, Enumeration nameEnum) {
        super(frame, "GDM Result Selection Dialog", true);
      
        Listener listener = new Listener();
        addWindowListener(listener);

        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());

        //enumeration is not sorted, sort based on alg index and load 
        //into the vector for the JList
        Vector v = sortAndLoadResultVector(nameEnum);
        
        this.resultList = new JList(v);
        resultList.setVisibleRowCount(3);
        resultList.setSelectedIndex(0);       
        
        JScrollPane pane = new JScrollPane(resultList);
        JLabel label = new JLabel("Select Cluster Result: ");
        label.setVerticalAlignment(JLabel.TOP);
    
        parameters.add(label, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,30,10),0,0));
        parameters.add(pane, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,30,50),0,0));
        
        this.addContent(parameters);
        this.setActionListeners(listener);
        pack();
    }

    public void resetControls() {
        this.resultList.setSelectedIndex(0);
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
    
    /**
     *  Returns the selected result name
     */
    public String getSelectedResult() {
        return (String)this.resultList.getSelectedValue();
    }

    
    public static void main (String [] args){
        Vector nameVector = new Vector();

        nameVector.add("KMC - genes (6)");
        nameVector.add("TTEST");
        nameVector.add("SOTA Test");
        nameVector.add("SOTA - genes (1)");
        nameVector.add("QTC - genes (2)");
        
        GDMResultSelectionDialog dialog = new GDMResultSelectionDialog(new JFrame(), nameVector.elements());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.showModal();
        System.out.println("Selected result: "+ dialog.getSelectedResult());
    }
    
 
    private Vector sortAndLoadResultVector( Enumeration _enum ) {
        Vector results = new Vector();
        
        while(_enum.hasMoreElements())
            results.add(_enum.nextElement());
        
        //Need to restore order
        String [] names = new String[results.size()];
        float [] order = new float[results.size()];
        int index;
        int algIndex;
        
        for(int i = 0; i < names.length; i++) {
            names[i] = (String)results.elementAt(i);
            index = names[i].indexOf("(");
            if(index != -1){
                try {
                    order[i] = Integer.parseInt(names[i].substring(index+1, names[i].indexOf(")")));
                } catch (NumberFormatException nfe) {
                    order[i] = 1000+i;
                }
            } else {
                order[i] = 1000+i;
            }         
        }

        QSort sorter = new QSort(order);
        int [] origOrder = sorter.getOrigIndx();
        
        results = new Vector();
        for(int i = 0; i < origOrder.length; i++){
            results.add(names[origOrder[i]]);
        }

        return results;
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
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")) {
                resetControls();
            } else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(GDMResultSelectionDialog.this, "GDM Result Selection Dialog");
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

        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
}