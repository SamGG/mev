/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: FOMInitDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-02-23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

/**
 *
 * @author  nbhagaba
 * @version
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class FOMInitDialog extends AlgorithmDialog {
    
    public final static int CAST   = 1;
    public final static int KMEANS = 2;
    
    IterationPanel iPanel;
    KMCPanel kPanel;
    CASTPanel cPanel;
    JTabbedPane methodTabPane;
    JCheckBox takeAverageBox;
    SampleSelectionPanel sPanel;
    
    boolean okPressed = false;
    /** Creates new FOMInitDialog */
    public FOMInitDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "FOM: Figure of Merit", modality);
        setBounds(0, 0, 450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(Color.white);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        pane.setBackground(Color.white);
        
        sPanel = new SampleSelectionPanel();
        buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
        
        gridbag.setConstraints(sPanel, constraints);
        pane.add(sPanel);
        
        iPanel = new IterationPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 100, 20);
        gridbag.setConstraints(iPanel, constraints);
        pane.add(iPanel);
        
        javax.swing.UIManager.put("TabbedPane.selected", Color.white);
        methodTabPane = new JTabbedPane();
        
        kPanel = new KMCPanel();
        cPanel = new CASTPanel();
        
        methodTabPane.add("K-Means / K-Medians", kPanel);
        methodTabPane.add("CAST", cPanel);
        
        buildConstraints(constraints, 0, 2, 1, 1, 100, 90);
        gridbag.setConstraints(methodTabPane, constraints);
        pane.add(methodTabPane);
        
        takeAverageBox = new JCheckBox("Take Average", true);
        takeAverageBox.setEnabled(false);
        
        constraints.fill = GridBagConstraints.BOTH;
        buildConstraints(constraints, 0, 2, 1, 1, 0, 30);
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
        methodTabPane.addChangeListener(listener);
        //      methodTabPane.addChangeListener(listener);
        pack();
        setSize(600,450);
        // setResizable(false);
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            okButton.requestFocus();
        }
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
    
    class KMCPanel extends JPanel{
        JRadioButton meansButton, mediansButton;
        JTextField intervalInputField;
        JTextField iterationInputField;
        
        KMCPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            
            this.setLayout(gridbag);
            this.setBackground(Color.white);
            this.setBorder(BorderFactory.createLineBorder(Color.lightGray,2));
            
            JPanel panel1 = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            panel1.setLayout(grid1);
            panel1.setBackground(Color.white);
            panel1.setBorder(BorderFactory.createEmptyBorder(10,10,15,10));
            
            meansButton = new JRadioButton("Calculate means", true);
            meansButton.setBackground(Color.white);
            meansButton.setForeground(UIManager.getColor("Label.foreground"));
            meansButton.setFocusPainted(false);
            mediansButton = new JRadioButton("Calculate medians", false);
            mediansButton.setBackground(Color.white);
            mediansButton.setForeground(UIManager.getColor("Label.foreground"));
            mediansButton.setFocusPainted(false);
            
            ButtonGroup chooseMeansOrMedians = new ButtonGroup();
            chooseMeansOrMedians.add(meansButton);
            chooseMeansOrMedians.add(mediansButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            grid1.setConstraints(meansButton, constraints);
            panel1.add(meansButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            grid1.setConstraints(mediansButton, constraints);
            panel1.add(mediansButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 40);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(panel1, constraints);
            this.add(panel1);
            
            JPanel panel2 = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            panel2.setLayout(grid2);
            panel2.setBackground(Color.white);
            
            constraints.fill = GridBagConstraints.NONE;
            
            JLabel intervalLabel = new JLabel("Maximum number of clusters (enter an integer > 0):   ");
            intervalLabel.setBackground(Color.white);
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(intervalLabel, constraints);
            panel2.add(intervalLabel);
            
            intervalInputField = new JTextField("20", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(intervalInputField, constraints);
            panel2.add(intervalInputField);
            
            JLabel iterationLabel = new JLabel("Maximum number of iterations (enter an integer > 0):   ");
            iterationLabel.setBackground(Color.white);
            buildConstraints(constraints, 0, 1, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets.top = 10;
            grid2.setConstraints(iterationLabel, constraints);
            panel2.add(iterationLabel);
            
            iterationInputField = new JTextField("50", 7);
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(iterationInputField, constraints);
            panel2.add(iterationInputField);
            constraints.insets.top = 0;
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
            gridbag.setConstraints(panel2, constraints);
            this.add(panel2);
            
            JPanel textPanel = new JPanel();
            GridBagLayout textGrid = new GridBagLayout();
            textPanel.setLayout(textGrid);
            textPanel.setBackground(Color.white);
            textPanel.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));
            
            JLabel label2 = new JLabel("K-Means / K-Medians will be run using a starting K (number of clusters) = 1,");
            label2.setBackground(Color.white);
            constraints.anchor = GridBagConstraints.SOUTHWEST;
            buildConstraints(constraints, 0, 1, 1, 1, 100, 33);
            textGrid.setConstraints(label2, constraints);
            textPanel.add(label2);
            
            JLabel label3 = new JLabel("with K being incremented by 1 in each subsequent iteration, up to the");
            label3.setBackground(Color.white);
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 2, 1, 1, 0, 33);
            textGrid.setConstraints(label3, constraints);
            textPanel.add(label3);
            
            JLabel label4 = new JLabel("maximum number of clusters specified above");
            label4.setBackground(Color.white);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            buildConstraints(constraints, 0, 3, 1, 1, 0, 34);
            textGrid.setConstraints(label4, constraints);
            textPanel.add(label4);
            
            constraints.anchor = GridBagConstraints.CENTER;
            buildConstraints(constraints, 0, 2, 1, 1, 0, 40);
            gridbag.setConstraints(textPanel, constraints);
            this.add(textPanel);
        }
    }
    
    class CASTPanel extends JPanel {
        JTextField thresholdInputField;
        
        CASTPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            
            JPanel pane = new JPanel();
            this.setLayout(gridbag);
            this.setBackground(Color.white);
            this.setBorder(BorderFactory.createLineBorder(Color.lightGray,2));
            
            JPanel panel1 = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            panel1.setLayout(grid1);
            panel1.setBackground(Color.white);
            
            JLabel thresholdLabel = new JLabel("Interval ( enter a value between 0 and 1):    ");
            thresholdLabel.setBackground(Color.white);
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid1.setConstraints(thresholdLabel, constraints);
            panel1.add(thresholdLabel);
            
            thresholdInputField = new JTextField("0.1", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid1.setConstraints(thresholdInputField, constraints);
            panel1.add(thresholdInputField);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 60);
            constraints.anchor = GridBagConstraints.CENTER;
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(panel1, constraints);
            this.add(panel1);
            
            /*
            JLabel label1 = new JLabel("(Interval should be between 0 and 1)");
            buildConstraints(constraints, 0, 1, 2, 1, 0, 10);
            //constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(label1, constraints);
            this.add(label1);
             */
            
            JPanel textPanel = new JPanel();
            GridBagLayout textGrid = new GridBagLayout();
            textPanel.setLayout(textGrid);
            textPanel.setBackground(Color.white);
            textPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            
            JLabel label2 = new JLabel("CAST will be run with threshold affinities starting with above interval,");
            label2.setBackground(Color.white);
            buildConstraints(constraints, 0, 1, 1, 1, 100, 33);
            constraints.anchor = GridBagConstraints.SOUTHWEST;
            textGrid.setConstraints(label2, constraints);
            textPanel.add(label2);
            
            JLabel label3 = new JLabel("with affinity being incremented by the interval value in subsequent");
            label3.setBackground(Color.white);
            buildConstraints(constraints, 0, 2, 1, 1, 0, 33);
            constraints.anchor = GridBagConstraints.WEST;
            textGrid.setConstraints(label3, constraints);
            textPanel.add(label3);
            
            JLabel label4 = new JLabel("iterations, up to a maximum threshold affinity of 1.0");
            label4.setBackground(Color.white);
            buildConstraints(constraints, 0, 3, 1, 1, 0, 34);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            textGrid.setConstraints(label4, constraints);
            textPanel.add(label4);
            
            constraints.anchor = GridBagConstraints.CENTER;
            buildConstraints(constraints, 0, 1, 1, 1, 0, 40);
            gridbag.setConstraints(textPanel, constraints);
            this.add(textPanel);
            
        }
    }
    
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    
    public int getMethod() {
        int method = 0;
        if (methodTabPane.getSelectedIndex() == 0) {
            method = KMEANS;
        } else if(methodTabPane.getSelectedIndex() == 1) {
            method = CAST;
        }
        
        return method;
    }
    
    public boolean useMeans() {
        return kPanel.meansButton.isSelected();
    }
    
    public float getInterval() {
        String s2 = cPanel.thresholdInputField.getText();
        float interval = Float.parseFloat(s2);
        return interval;
    }
    
    public int getIterations() {
        String s1 = kPanel.intervalInputField.getText();
        int k = Integer.parseInt(s1);
        return k;
    }
    
    public int getKMCIterations(){
        String s1 = kPanel.iterationInputField.getText();
        int iterations = Integer.parseInt(s1);
        return iterations;
    }
    
    public int getFOMIterations() {
        String s = iPanel.iterationField.getText();
        return Integer.parseInt(s);
    }
    
    public boolean isAverage() {
        return takeAverageBox.isSelected();
    }
    
    public boolean isClusterGenes(){
        return sPanel.calcGenes.isSelected();
    }
    
    private void resetControls(){
        //  this.methodTabPane.setSelectedIndex(0);  set kmc as reset value???
        this.sPanel.calcGenes.setSelected(true);
        kPanel.meansButton.setSelected(true);
        kPanel.intervalInputField.setText("20");
        kPanel.iterationInputField.setText("50");
        cPanel.thresholdInputField.setText("0.1");
        iPanel.iterationField.setText("1");
    }
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        FOMInitDialog fDialog = new FOMInitDialog(dummyFrame, true);
        fDialog.show();
        System.out.println(fDialog.getIterations());
        System.exit(0);
    }
    
    class SampleSelectionPanel extends JPanel{
        JRadioButton calcGenes;
        JRadioButton calcExperiments;
        
        SampleSelectionPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Sample Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12) ,Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            calcGenes = new JRadioButton("Gene Cluster FOM", true);
            calcGenes.setFocusPainted(false);
            calcGenes.setBackground(Color.white);
            calcGenes.setForeground(UIManager.getColor("Label.foreground"));
            buildConstraints(constraints, 0, 0, 1, 1, 50, 0);
            gridbag.setConstraints(calcGenes, constraints);
            this.add(calcGenes);
            
            calcExperiments = new JRadioButton("Sample Cluster FOM");
            calcExperiments.setFocusPainted(false);
            calcExperiments.setBackground(Color.white);
            calcExperiments.setForeground(UIManager.getColor("Label.foreground"));
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            gridbag.setConstraints(calcExperiments, constraints);
            this.add(calcExperiments);
            
            ButtonGroup group = new ButtonGroup();
            group.add(calcGenes);
            group.add(calcExperiments);
        }
    }
    
    private class IterationPanel extends ParameterPanel {
        
        JLabel iterationLabel;
        JTextField iterationField;
        
        public IterationPanel() {
            super("FOM Iteration Selection");
            setLayout(new GridBagLayout());
            iterationLabel = new JLabel("Number of FOM Iterations");
            iterationLabel.setOpaque(false);
            iterationField = new JTextField("1", 5);
            add(iterationLabel, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10,10,10,10), 0, 0));
            add(iterationField, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10,10,10,10), 0, 0));
        }
        
        public void setEnabled(boolean enable) {
            iterationLabel.setEnabled(enable);
            iterationField.setEnabled(enable);
            if(enable)
                iterationField.setBackground(Color.white);
            else
                iterationField.setBackground(Color.lightGray);
        }
    }
    
    protected class EventListener extends WindowAdapter implements ActionListener, ChangeListener{
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                okPressed = true;
                int method = FOMInitDialog.this.methodTabPane.getSelectedIndex();
                int fomI = 1;
                //KMC
                int k = -1;
                float inter = -1.0f, kmcIter = -1, progress = -1;
                try{
                    if(method == 0){
                        fomI = Integer.parseInt(iPanel.iterationField.getText());
                        progress++;
                        k = Integer.parseInt(FOMInitDialog.this.kPanel.intervalInputField.getText());
                        progress++;
                        kmcIter = Integer.parseInt(kPanel.iterationInputField.getText());
                        progress++;
                    }
                    else{
                        inter = Float.parseFloat(FOMInitDialog.this.cPanel.thresholdInputField.getText());
                    }
                } catch (NumberFormatException e){
                    if(progress == -1) {
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Number format error. Expecting an integer for FOM iteration parameter.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        iPanel.iterationField.requestFocus();
                        iPanel.iterationField.selectAll();
                    } else if(method == 0){
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        if(progress == 0){
                            kPanel.intervalInputField.requestFocus();
                            kPanel.intervalInputField.selectAll();
                        }
                        else {
                            kPanel.iterationInputField.requestFocus();
                            kPanel.iterationInputField.selectAll();
                        }
                    } else {
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        cPanel.thresholdInputField.requestFocus();
                        cPanel.thresholdInputField.selectAll();
                    }
                    return;
                }
                if(method == 0){
                    if(fomI < 1) {
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "FOM iterations must be > 0", "Invalid Input Error", JOptionPane.ERROR_MESSAGE);
                        iPanel.iterationField.requestFocus();
                        iPanel.iterationField.selectAll();
                        return;
                    }
                    if(k < 1){
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Maximum Number of Clusters must be > 0", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        kPanel.intervalInputField.requestFocus();
                        kPanel.intervalInputField.selectAll();
                        return;
                    }
                    if(kmcIter < 1){
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Maximum Number of intervals per KMC run must be > 0", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        kPanel.iterationInputField.requestFocus();
                        kPanel.iterationInputField.selectAll();
                        return;
                    }
                    
                } else {
                    if(inter <= 0 || inter >= 1.0f){
                        JOptionPane.showMessageDialog(FOMInitDialog.this, "Interval must be > 0 and < 1.0", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                        cPanel.thresholdInputField.requestFocus();
                        cPanel.thresholdInputField.selectAll();
                        return;
                    }
                }
                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                dispose();
            } else if (command.equals("cancel-command")){
                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(FOMInitDialog.this, "FOM Initialization Dialog");
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
        public void windowClosing(java.awt.event.WindowEvent we){
            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
        }
        
        public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
            //only methodTabbedPane has change events listener
            JTabbedPane tPane = (JTabbedPane)(changeEvent.getSource());
            if(tPane.getSelectedIndex() == 0)
                iPanel.setEnabled(true);
            else
                iPanel.setEnabled(false);
        }
        
    }
    
    
    
    
}
