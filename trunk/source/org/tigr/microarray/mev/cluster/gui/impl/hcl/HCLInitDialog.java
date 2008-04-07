/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLInitDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/02/23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class HCLInitDialog extends AlgorithmDialog {//JDialog {
    
    private int result;
    
    private JCheckBox genes_box;
    private JCheckBox cluster_box;
    private JCheckBox gene_ordering_box;
    private JCheckBox sample_ordering_box;
    private JRadioButton ALC;
    private JRadioButton CLC;
    private JRadioButton SLC;
    private DistanceMetricPanel metricPanel;
    private String globalMetricName;
    private boolean globalAbsoluteDistance;
    
    public HCLInitDialog(Frame parent) {
        this(parent, " ", false, false);
    }
    
    /**
     * Constructs the dialog.
     */
    public HCLInitDialog(Frame parent, String globalMetricName, boolean globalAbsoluteDistance, boolean showDistancePanel) {
        super(parent, "HCL: Hierarchical Clustering", true);
        setResizable(false);
        
        this.globalMetricName = globalMetricName;
        this.globalAbsoluteDistance = globalAbsoluteDistance;
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        ParameterPanel sampleSelectionPanel = new ParameterPanel("Tree Selection");
        sampleSelectionPanel.setLayout(new GridBagLayout());
        
        ParameterPanel orderingSelectionPanel = new ParameterPanel("Ordering Optimization");
        orderingSelectionPanel.setLayout(new GridBagLayout());
        
        genes_box = new JCheckBox("Gene Tree");
        genes_box.setSelected(true);
        genes_box.setFocusPainted(false);
        genes_box.setBackground(Color.white);
        genes_box.setForeground(UIManager.getColor("Label.foreground"));
        genes_box.addItemListener(listener);
        
        cluster_box = new JCheckBox("Sample Tree");
        cluster_box.setSelected(true);
        cluster_box.setFocusPainted(false);
        cluster_box.setBackground(Color.white);
        cluster_box.setForeground(UIManager.getColor("Label.foreground"));
        cluster_box.addItemListener(listener);
        
        gene_ordering_box = new JCheckBox("Optimize Gene Leaf Order");
        gene_ordering_box.setSelected(false);
        gene_ordering_box.setFocusPainted(false);
        gene_ordering_box.setBackground(Color.white);
        gene_ordering_box.setForeground(UIManager.getColor("Label.foreground"));
        gene_ordering_box.addItemListener(listener);
        
        sample_ordering_box = new JCheckBox("Optimize Sample Leaf Order");
        sample_ordering_box.setSelected(false);
        sample_ordering_box.setFocusPainted(false);
        sample_ordering_box.setBackground(Color.white);
        sample_ordering_box.setForeground(UIManager.getColor("Label.foreground"));
        sample_ordering_box.addItemListener(listener);
        
        JLabel optimizationWarning = new JLabel("(Leaf ordering optimization will increase the calculation time)");
        
        sampleSelectionPanel.add(genes_box, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,5,111), 0,0));
        sampleSelectionPanel.add(cluster_box, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,5,86), 0,0));        
        orderingSelectionPanel.add(gene_ordering_box, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,25),0,0));
        orderingSelectionPanel.add(sample_ordering_box, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0),0,0));
        orderingSelectionPanel.add(optimizationWarning, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(10,10,0,0),0,0));
        
        metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteDistance, "Euclidean Distance", "HCL", true, true);
        
        ParameterPanel linkageMethodPanel = new ParameterPanel("Linkage Method Selection");
        linkageMethodPanel.setLayout(new GridBagLayout());
        
        ALC = new JRadioButton("Average linkage clustering");
        ALC.setBackground(Color.white);
        ALC.setFocusPainted(false);
        ALC.setForeground(UIManager.getColor("Label.foreground"));
        ALC.setMnemonic(KeyEvent.VK_A);
        ALC.setSelected(true);
        
        CLC = new JRadioButton("Complete linkage clustering");
        CLC.setBackground(Color.white);
        CLC.setFocusPainted(false);
        CLC.setForeground(UIManager.getColor("Label.foreground"));
        CLC.setMnemonic(KeyEvent.VK_C);
        
        SLC = new JRadioButton("Single linkage clustering");
        SLC.setBackground(Color.white);
        SLC.setFocusPainted(false);
        SLC.setForeground(UIManager.getColor("Label.foreground"));
        SLC.setMnemonic(KeyEvent.VK_S);
        
        linkageMethodPanel.add(ALC, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
        linkageMethodPanel.add(CLC, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        linkageMethodPanel.add(SLC, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(ALC);
        group.add(CLC);
        group.add(SLC);
        
        
//        JPanel parameters = new JPanel(new GridLayout(0, 2, 10, 10));
  //          ParameterPanel parameters = new ParameterPanel();
//        parameters.setLayout(new GridLayout(0, 2, 10, 10));
  //      parameters.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    //    parameters.setBackground(Color.white);
 //       parameters.setForeground(Color.black);
   //     parameters.add(ALC);
     //   parameters.add(genes_box);
   //     parameters.add(CLC);
    //    parameters.add(cluster_box);
      //  parameters.add(SLC);
        
        
    //    ParameterPanel parameterPanel = new ParameterPanel();
      //  parameterPanel.add(parameters);
        
        JPanel parameterPanel = new JPanel(new GridBagLayout());
        parameterPanel.setBackground(Color.white);
        
        parameterPanel.add(sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        parameterPanel.add(orderingSelectionPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        if(showDistancePanel) {
            parameterPanel.add(metricPanel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            parameterPanel.add(linkageMethodPanel, new GridBagConstraints(0,3,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        } else {
            parameterPanel.add(linkageMethodPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        }
        addContent(parameterPanel);
        setActionListeners(listener);
        
        //this.getContentPane().add(panel1, BorderLayout.CENTER);
        this.pack();
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
     * Resets controls to default initial settings
     */
    private void resetControls(){
        ALC.setSelected(true);
        genes_box.setSelected(true);
        cluster_box.setSelected(true);
        gene_ordering_box.setSelected(false);
        sample_ordering_box.setSelected(false);
        metricPanel.reset();
    }
    
    /**
     * Returns true, if genes check box is selected.
     */
    public boolean isClusterGenes() {
        return genes_box.isSelected();
    }
    
    /**
     * Returns true, if cluster check box is selected.
     */
    public boolean isClusterExperiments() {
        return cluster_box.isSelected();
    }
    
    /**
     * Returns true, if ordering check box is selected.
     */
    public boolean isGeneOrdering() {
        return gene_ordering_box.isSelected();
    }
    /**
     * Returns true, if ordering check box is selected.
     */
    public boolean isSampleOrdering() {
        return sample_ordering_box.isSelected();
    }
    
    /**
     * Returns a method code.
     * @return 0 for ALC method, 1 for CLC or -1 otherwise.
     */
    public int getMethod() {
        if (ALC.isSelected()) {
            return 0;
        }
        if (CLC.isSelected()) {
            return 1;
        }
        return -1;
    }
    
    /**
     * Returns the currently selected metric
     */
    public int getDistanceMetric() {
        return metricPanel.getMetricIndex();
    }
    
    /**
     *  Returns true if the absolute checkbox is selected, else false
     */
    public boolean getAbsoluteSelection() {
        return metricPanel.getAbsoluteSelection();
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(HCLInitDialog.this, "HCL Initialization Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
            dispose();
        }
        
        public void itemStateChanged(ItemEvent e) {
            okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
            if (!genes_box.isSelected()) {
            	gene_ordering_box.setEnabled(false);
            	gene_ordering_box.setSelected(false);
            }
            if (!cluster_box.isSelected()){
            	sample_ordering_box.setEnabled(false);
            	sample_ordering_box.setSelected(false);
            }
            if (genes_box.isSelected()) gene_ordering_box.setEnabled(true);
            if (cluster_box.isSelected())sample_ordering_box.setEnabled(true);
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
   public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame("Test");
        while (true) {
            HCLInitDialog dialog = new HCLInitDialog(frame, "Euclidean Distance", false, true);
            if (dialog.showModal() != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            System.out.println("===============================");
            System.out.println(dialog.isClusterGenes());
            System.out.println(dialog.isClusterExperiments());
            System.out.println(dialog.getMethod());
        }
    }
    
    protected void disposeDialog() {
    }
    
}