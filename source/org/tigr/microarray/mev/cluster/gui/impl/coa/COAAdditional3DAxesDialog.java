/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * COAAdditional3DAxesDialog.java
 *
 * Created on December 10, 2004, 11:00 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/**
 *
 * @author  nbhagaba
 */
public class COAAdditional3DAxesDialog extends AlgorithmDialog {
    boolean okPressed = false;    
    private int numAxes;
    private JComboBox xAxisBox, yAxisBox, zAxisBox;
    
    /** Creates a new instance of PCAAdditional3DAxesDialog */
    public COAAdditional3DAxesDialog(JFrame parentFrame, boolean modal, int numAxes) {        
        super(parentFrame, "Select new projection axes", modal);
        this.numAxes = numAxes;
        setBounds(0, 0, 500, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);        
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);      
        
        String[] axes = new String[numAxes];
        for (int i = 0; i < axes.length; i++) {
            axes[i] = String.valueOf(i + 1);
        }
        xAxisBox = new JComboBox(axes);
        yAxisBox = new JComboBox(axes);
        zAxisBox = new JComboBox(axes);
        
        xAxisBox.setSelectedIndex(0);
        yAxisBox.setSelectedIndex(1);
        zAxisBox.setSelectedIndex(2);

        JLabel selectAxesLabel = new JLabel("Select components to plot: ");
        buildConstraints(constraints, 0, 0, 1, 1, 40, 100);
        gridbag.setConstraints(selectAxesLabel, constraints);
        pane.add(selectAxesLabel);
        
        buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
        gridbag.setConstraints(xAxisBox, constraints);
        pane.add(xAxisBox);     
        
        buildConstraints(constraints, 2, 0, 1, 1, 20, 0);
        gridbag.setConstraints(yAxisBox, constraints);
        pane.add(yAxisBox);        
        
        buildConstraints(constraints, 3, 0, 1, 1, 20, 0);
        gridbag.setConstraints(zAxisBox, constraints);
        pane.add(zAxisBox);     
                
        addContent(pane);
        //pack();
        EventListener listener = new EventListener();        
        setActionListeners(listener);
        this.addWindowListener(listener);        
    }

    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
    }    
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    } 
    
    public int getXAxis() {
        return xAxisBox.getSelectedIndex();
    }
    
    public int getYAxis() {
        return yAxisBox.getSelectedIndex();
    }
    
    public int getZAxis() {
        return zAxisBox.getSelectedIndex();
    }    
    
    public void setZBoxInvisible(boolean invis) {
        zAxisBox.setEnabled(!invis);
        zAxisBox.setVisible(!invis);
    }
    
    public boolean isOkPressed() {
        return okPressed;
    }      
    
    public static void main(String[] args) {
        COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog(new JFrame(), true, 10);
        pd.setVisible(true);
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                int x = getXAxis();
                int y = getYAxis();
                int z = getZAxis();
                if (!zAxisBox.isVisible()) {
                    if (x==y) {
                       JOptionPane.showMessageDialog(null, "Both axes cannot be the same", "Error", JOptionPane.ERROR_MESSAGE); 
                       return;
                    }
                } else {
                    if ((x==y)||(y==z)||(x==z)) {
                        JOptionPane.showMessageDialog(null, "No two axes can be the same", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                okPressed = true;
                dispose();  
                
            } else if (command.equals("reset-command")) {
                xAxisBox.setSelectedIndex(0);
                yAxisBox.setSelectedIndex(1);
                zAxisBox.setSelectedIndex(2);
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){		
            }
        }
    }    
}
