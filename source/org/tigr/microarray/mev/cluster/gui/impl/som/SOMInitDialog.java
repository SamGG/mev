/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOMInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:04 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class SOMInitDialog extends AlgorithmDialog {
    
    private int result;
    private SampleSelectionPanel sampleSelectionPanel;
    private HCLSelectionPanel hclOpsPanel;
    private DistanceMetricPanel metricPanel;
    
    public JTextField dimXField;
    public JTextField dimYField;
    public JTextField iterField;
    public JTextField alphaField;
    public JTextField radiusField;
    public JComboBox initList;
    public JComboBox neighbList;
    public JComboBox topoList;
    
    /**
     * Constructs the dialog with predefined parameters.
     */
    public SOMInitDialog(Frame frame, int dimX, int dimY, long iterations,
    float alpha, float radius,
    int initType, int neighborhood, int topology, String globalFunctionName, boolean globalAbsoluteValue) {
        super(frame, "SOM: Self Organizing Maps", true);
        this.setSize(520, 343);
        this.setResizable(false);
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        sampleSelectionPanel.setExperimentButtonActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ){
                if( sampleSelectionPanel.isClusterGenesSelected() )
                    return;
                int sel = initList.getSelectedIndex();
                initList.removeItemAt(1);
                initList.insertItemAt("Random Samples", 1);
                initList.setSelectedIndex(sel);
            }
        });
        sampleSelectionPanel.setGeneButtonActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ){
                if( !sampleSelectionPanel.isClusterGenesSelected() )
                    return;
                int sel = initList.getSelectedIndex();
                initList.removeItemAt(1);
                initList.insertItemAt("Random Genes", 1);
                initList.setSelectedIndex(sel);
            }
        });
        
    
        metricPanel = new DistanceMetricPanel(globalFunctionName, globalAbsoluteValue, "Pearson Correlation", "SOM", true, true);
        
        JPanel parameters1 = new JPanel();
        parameters1.setBorder(new EmptyBorder(5, 10, 20, 0));
        parameters1.setBackground(Color.white);
        parameters1.setForeground(Color.black);
        parameters1.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.right = 10;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        
        parameters1.add(new JLabel("Dimension X"), gbc);
        gbc.gridy = 1;
        parameters1.add(new JLabel("Dimension Y"), gbc);
        gbc.gridy = 2;
        parameters1.add(new JLabel("Iterations") , gbc);
        gbc.gridy = 3;
        parameters1.add(new JLabel("Alpha"),       gbc);
        gbc.gridy = 4;
        parameters1.add(new JLabel("Radius"),      gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dimXField = new JTextField(String.valueOf(dimX), 5);
        parameters1.add(dimXField  , gbc);
        gbc.gridy = 1;
        dimYField = new JTextField(String.valueOf(dimY), 5);
        parameters1.add(dimYField  , gbc);
        gbc.gridy = 2;
        iterField = new JTextField(String.valueOf(iterations), 5);
        parameters1.add(iterField  , gbc);
        gbc.gridy = 3;
        alphaField = new JTextField(String.valueOf(alpha), 5);
        parameters1.add(alphaField , gbc);
        gbc.gridy = 4;
        radiusField = new JTextField(String.valueOf(radius), 5);
        parameters1.add(radiusField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        
        JPanel parameters2 = new JPanel();
        parameters2.setLayout(new GridLayout(0, 1, 0, 10));
        parameters2.setBorder(new EmptyBorder(5, 25, 15, 10));
        parameters2.setBackground(Color.white);
        
        parameters2.add(new JLabel("Initialization"), BorderLayout.WEST);
        parameters2.add(new JLabel("Neighborhood"), BorderLayout.WEST);
        parameters2.add(new JLabel("Topology"), BorderLayout.WEST);
        
        JPanel parameters3 = new JPanel();
        parameters3.setLayout(new GridLayout(0, 1, 10, 10));
        parameters3.setBorder(new EmptyBorder(10, 0, 15, 10));
        parameters3.setBackground(Color.white);
        
        initList = new JComboBox(new String[] {"Random Vector", "Random Genes"});
        initList.setSelectedIndex(initType);
        
        FontMetrics fm = initList.getFontMetrics(initList.getFont());
        int width = fm.stringWidth("Random Samples");
        initList.setPreferredSize( new Dimension(width + 40, initList.getHeight()));
        
        neighbList = new JComboBox(new String[] {"Bubble", "Gaussian"});
        neighbList.setSelectedIndex(neighborhood);
        neighbList.setPreferredSize( new Dimension(width, initList.getHeight()));
        
        topoList = new JComboBox(new String[] {"Hexagonal", "Rectangular"});
        topoList.setSelectedIndex(topology);
        
        initList.setBackground(Color.white);
        neighbList.setBackground(Color.white);
        topoList.setBackground(Color.white);
        
        parameters3.add(initList);
        parameters3.add(neighbList);
        parameters3.add(topoList);
        
        hclOpsPanel = new HCLSelectionPanel();
        
        ParameterPanel paramPanel = new ParameterPanel();
        paramPanel.setLayout(new BorderLayout());
        
        paramPanel.add(parameters1, BorderLayout.WEST);
        paramPanel.add(parameters2, BorderLayout.CENTER);
        paramPanel.add(parameters3, BorderLayout.EAST);
                
        JPanel panel3 = new JPanel(new GridBagLayout());
        panel3.add(sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));       
        panel3.add(metricPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        panel3.add(paramPanel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        panel3.add(hclOpsPanel, new GridBagConstraints(0,3,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        setActionListeners(listener);
        addContent(panel3);
        pack();
        // panel1.add(topPanel, BorderLayout.CENTER);
        
        //    this.getContentPane().add(panel1, BorderLayout.CENTER);
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
     * Returns if clustering Genes is true
     */
    public boolean isClusterGenes(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    
    /**
     * Returns the x dimension.
     */
    public int getDimensionX() {
        return Integer.parseInt(dimXField.getText());
    }
    
    /**
     * Returns the y dimension.
     */
    public int getDimensionY() {
        return Integer.parseInt(dimYField.getText());
    }
    
    /**
     * Returns count of iterations.
     */
    public long getIterations() {
        return Long.parseLong(iterField.getText());
    }
    
    /**
     * Returns the alpha value.
     */
    public float getAlpha() {
        return Float.parseFloat(alphaField.getText());
    }
    
    /**
     * Returns the radius value.
     */
    public float getRadius() {
        return Float.parseFloat(radiusField.getText());
    }
    
    /**
     * Returns the initialization type.
     */
    public int getInitType() {
        return initList.getSelectedIndex();
    }
    
    /**
     * Returns the neighborhood index.
     */
    public int getNeighborhood() {
        return neighbList.getSelectedIndex();
    }
    
    /**
     * Returns the topology index.
     */
    public int getTopology() {
        return topoList.getSelectedIndex();
    }
    
    /**
     * Returns true if the hierarchical checkbox is selected.
     */
    public boolean isHierarchicalTree() {
        return hclOpsPanel.isHCLSelected();
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
    public boolean isAbsoluteDistance() {
        return metricPanel.getAbsoluteSelection();
    }
    
    
    /**
     *  Resets the controls to default
     */
    private void resetControls(){
        this.dimXField.setText("3");
        this.dimYField.setText("3");
        this.iterField.setText("2000");
        this.alphaField.setText("0.05");
        this.radiusField.setText("3.0");
        initList.removeItemAt(1);
        initList.insertItemAt("Random Genes", 1);
        this.initList.setSelectedIndex(1);
        this.neighbList.setSelectedIndex(1);
        this.topoList.setSelectedIndex(0);
        this.sampleSelectionPanel.setClusterGenesSelected(true);
        this.hclOpsPanel.setHCLSelected(false);
        this.initList.setSelectedIndex(1);
        this.neighbList.setSelectedIndex(1);
        this.topoList.setSelectedIndex(0);
        this.metricPanel.reset();
    }
    
    /**
     * Validates input values
     */
    private boolean validInput(int x, int y, long i, float a, float r){
        boolean valid = true;
        if(x < 1 ){
            JOptionPane.showMessageDialog(SOMInitDialog.this, "x Dimension must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.dimXField.requestFocus();
            this.dimXField.selectAll();
            valid = false;
        }
        else if( y < 1){
            JOptionPane.showMessageDialog(SOMInitDialog.this, "y Dimension must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.dimYField.requestFocus();
            this.dimYField.selectAll();
            valid = false;
        }
        else if( i < 1 ){
            JOptionPane.showMessageDialog(SOMInitDialog.this, "Number of Iterations must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.iterField.requestFocus();
            this.iterField.selectAll();
            valid = false;
        }
        else if( a <= 0 ){
            JOptionPane.showMessageDialog(SOMInitDialog.this, "Alpha value must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.alphaField.requestFocus();
            this.alphaField.selectAll();
            valid = false;
        }
        else if(r <= 0){
            JOptionPane.showMessageDialog(SOMInitDialog.this, "Radius value must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.radiusField.requestFocus();
            this.radiusField.selectAll();
            valid = false;
        }
        return valid;
    }
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                int x,y;
                long i;
                float a,r;
                try {
                    x = Integer.parseInt(dimXField.getText());
                    y = Integer.parseInt(dimYField.getText());
                    i = Long.parseLong(iterField.getText());
                    a = Float.parseFloat(alphaField.getText());
                    r = Float.parseFloat(radiusField.getText());
                    if(validInput(x,y,i,a,r)){
                        result = JOptionPane.OK_OPTION;
                        dispose();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SOMInitDialog.this, "Input value " + nfe.getMessage() +" is not valid input. \nPlease enter a new value or view the parameter information help page for this algorithm.", "Input Error!", JOptionPane.ERROR_MESSAGE);
                    result = JOptionPane.CANCEL_OPTION;
                }
                
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }else if (command.equals("reset-command")){
                resetControls();
            }else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(SOMInitDialog.this, "SOM Initialization Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 600);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String[] args) {
        SOMInitDialog dlg = new SOMInitDialog(new Frame(), 0 ,0, 0, 0, 0, 1, 0, 0, "Euclidean Distance", false);
        dlg.showModal();
        System.exit(0);
    }
}
