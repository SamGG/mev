/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMInitDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-07-27 19:59:17 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.io.*;
import javax.swing.border.*;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class SVMInitDialog extends AlgorithmDialog {
    public JPanel panel1 = new JPanel();
    public JPanel panel3 = new JPanel();
    public JPanel panel4 = new JPanel();
    public JPanel Parameters1 = new JPanel();
    public JPanel Parameters2 = new JPanel();
    public JPanel Parameters3 = new JPanel();
    public JPanel ClassFilePanel = new JPanel();
    public JPanel insetsPanel = new JPanel();
    public JLabel label1 = new JLabel();
    public JLabel label2 = new JLabel();
    public JLabel chooseFileLabel;
    public JTextField textField1;
    public JTextField textField2;
    public JTextField textField3;
    public JTextField textField4;
    public JTextField textField5;
    public JTextField textField6;
    public JTextField textField7;
    public JTextField textField8;
    public JTextField textField9;
    public JComboBox List1;
    public JComboBox List2;
    public JComboBox List3;
    public BorderLayout borderLayout1 = new BorderLayout();
    public BorderLayout borderLayout2 = new BorderLayout();
    public BorderLayout borderLayout3 = new BorderLayout();
    public BorderLayout borderLayout4 = new BorderLayout();
    public BorderLayout borderLayout5 = new BorderLayout();
    public BorderLayout borderLayout6 = new BorderLayout();
    public GridLayout gridLayout = new GridLayout();
    private JButton ChooseFileButton;
    public JCheckBox editorCheckBox;
    public JCheckBox CheckBox1;
    public JCheckBox CheckBox2;
    public JCheckBox CheckBox3;
    private JLabel Label1;
    private JLabel Label2;
    private JLabel Label3;

    private int result;
    private SVMData origData;
    private SVMData resultData;

    /**
     * Crates a new <CODE>SVMInitDialog</CODE>
     * @param MyParent parent Frame
     * @param data <CODE>SVMData</CODE> to initialize and mainitain
     * selected parameters.
     */    
    public SVMInitDialog(Frame MyParent, SVMData data) {
        super(new JFrame(), "SVM Initialization", true);

        origData = new SVMData(data);
        resultData = data;

        this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.setResizable(false);

        Listener listener = new Listener();
        addWindowListener(listener);
        
        panel1.setLayout(borderLayout1);
        panel3.setLayout(borderLayout3);
        panel4.setLayout(borderLayout4);
        insetsPanel.setLayout(gridLayout);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
	
	Parameters1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), 
		    "Classification Input", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
		    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", Font.BOLD, 12), Color.black));
	
	Parameters1.setName("Classification Input");
	Parameters1.setBackground(Color.white);
        Parameters1.setForeground(Color.black);
        Parameters1.setLayout(new GridBagLayout());

        borderLayout5.setHgap(10);
        borderLayout6.setHgap(10);

	editorCheckBox = new JCheckBox("Use SVM Classification Editor", true);
	editorCheckBox.setBackground(Color.white);
        editorCheckBox.setForeground(UIManager.getColor("Label.foreground"));
	editorCheckBox.setFocusPainted(false);
	editorCheckBox.setActionCommand("use-editor-command");

	Parameters1.add(editorCheckBox, new GridBagConstraints(0, 0, 3, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0));
	
	chooseFileLabel = new JLabel("Use Classification File:");
        Parameters1.add( chooseFileLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0));
        ChooseFileButton = new JButton("Choose");
        ChooseFileButton.setActionCommand("choose-file-command");
        ChooseFileButton.addActionListener( listener );
        ChooseFileButton.setFocusPainted(false);

        Parameters1.add(ChooseFileButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0));
        textField1 = new JTextField();
        textField1.setActionCommand("choose-file-command");
        textField1.addActionListener( listener );
        Parameters1.add(textField1,new GridBagConstraints(1, 1, 1, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0));
	
	disableFileSelection();
		editorCheckBox.addActionListener( listener );
	
	Parameters2.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
	Parameters2.setName("Training Parameters");
        Parameters2.setBackground(Color.white);
        Parameters2.setForeground(Color.black);
        Parameters2.setLayout(new GridLayout(0,2,10,10));

        Parameters2.add(new JLabel("Constant"));
        textField2 = new JTextField(Float.toString( data.constant),7);
        Parameters2.add(textField2);

        Parameters2.add(new JLabel("Coefficient  "));
        textField3 = new JTextField(Float.toString(data.coefficient),7);
        Parameters2.add(textField3);

        Parameters2.add(new JLabel("Power  "));

        textField4 = new JTextField(Float.toString(data.power),7);
        Parameters2.add(textField4);

        Parameters2.add(new JLabel("Diag. factor"));
        textField8 = new JTextField(Float.toString(data.diagonalFactor),7);
        Parameters2.add(textField8);

        Parameters2.add(new JLabel("Threshold"));
        textField9 = new JTextField(Float.toString(data.convergenceThreshold),7);
        Parameters2.add(textField9);

        Parameters3.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        Parameters3.setBackground(Color.white);
        Parameters3.setForeground(Color.black);
        Parameters3.setLayout(new GridLayout(0,2,10,10));

        CheckBoxListener MyListener = new CheckBoxListener();

        CheckBox1 = new JCheckBox("Radial");
        if (data.radial) {
            CheckBox1.setSelected(true);
        } else {
            CheckBox1.setSelected(false);
        }
        CheckBox1.setFocusPainted(false);
        CheckBox1.setBackground(Color.white);
        CheckBox1.setForeground(UIManager.getColor("Label.foreground"));
        CheckBox1.addItemListener(MyListener);

        Parameters3.add(CheckBox1);

        CheckBox2 = new JCheckBox("Normalize");
        if (data.normalize) {
            CheckBox2.setSelected(true);
        } else {
           CheckBox2.setSelected(false);
        }
        CheckBox2.setFocusPainted(false);
        CheckBox2.setBackground(Color.white);
        CheckBox2.setForeground(UIManager.getColor("Label.foreground"));
        CheckBox2.addItemListener(MyListener);
        CheckBox2.setVisible(false);
        CheckBox2.setEnabled(false);
        Parameters3.add(CheckBox2);

        Label1=new JLabel("Width factor");
        textField5 = new JTextField(Float.toString(data.widthFactor),7);
        if (data.radial) {
            Label1.setEnabled(true);
            textField5.setEnabled(true);
        } else {
            Label1.setEnabled(false);
            textField5.setEnabled(false);
        }
        Parameters3.add(Label1);
        Parameters3.add(textField5);

        CheckBox3 = new JCheckBox("Constraints");
        if (data.constrainWeights) {
            CheckBox3.setSelected(true);
        } else {
            CheckBox3.setSelected(false);
        }
        CheckBox3.setFocusPainted(false);
        CheckBox3.setBackground(Color.white);
        CheckBox3.setForeground(Color.black);
        CheckBox3.addItemListener(MyListener);
        Parameters3.add(CheckBox3);

        Parameters3.add(new JLabel(""));

        Label2=new JLabel("Pos. constraint");
        Label2.setEnabled(false);

        textField6 = new JTextField(Float.toString(1),7);
        textField6.setEnabled(false);

        Label3=new JLabel("Neg. constraint");
        Label3.setEnabled(false);
        Parameters3.add(Label3);

        textField7 = new JTextField(Float.toString(1),7);
        textField7.setEnabled(false);

        if (data.constrainWeights) {
            Label2.setEnabled(true);
            Label3.setEnabled(true);
            textField6.setEnabled(true);
            textField7.setEnabled(true);
        } else {
            Label2.setEnabled(false);
            Label3.setEnabled(false);
            textField6.setEnabled(false);
            textField7.setEnabled(false);
        }

        Parameters3.add(Label2);
        Parameters3.add(textField6);
        Parameters3.add(Label3);
        Parameters3.add(textField7);

        gridLayout.setRows(2);
        gridLayout.setColumns(1);

        panel1.add(panel3, BorderLayout.NORTH);
        panel3.add(panel4, BorderLayout.CENTER);
        panel4.add(Parameters1, BorderLayout.NORTH);

	JPanel trainingPanel = new JPanel();
	trainingPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), 
		    "Training Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
		    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", Font.BOLD, 12), Color.black));
	trainingPanel.setBackground(Color.white);
	trainingPanel.setLayout(new BorderLayout());
	trainingPanel.add(Parameters2, BorderLayout.WEST);
	trainingPanel.add(Parameters3, BorderLayout.CENTER);
	
	panel4.add(trainingPanel, BorderLayout.SOUTH);
	
        addContent(panel1);
        setActionListeners(listener);
        this.pack();
    }

    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            dispose();
        }
        super.processWindowEvent(e);
    }

    /**
     * Launches file chooser
     */    
    protected void onChooseFile() {
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.setCurrentDirectory(new File("Data"));
        fc.addChoosableFileFilter(new ClassificationFileFilter());
        fc.setFileView(new ClassificationFileView());
        int returnVal = fc.showOpenDialog( this );
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            resultData.classificationFile = fc.getSelectedFile();
            textField1.setText(resultData.classificationFile.getName());
        }
    }

    /**
     * Displays dialog for user interaction
     */    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }

    /**
     * Returns SVMData containing the current parameter
     * selection
     * @return contains current selection
     */    
    public SVMData getData() {
        return resultData;
    }

    /**
     * Disables SVC file chooser option
     */    
    private void disableFileSelection(){
	chooseFileLabel.setEnabled(false);
	textField1.setEnabled(false);
	ChooseFileButton.setEnabled(false);
    }
    
    /**
     * Enables SVC file chooser selection
     */    
    private void enableFileSelection(){
		chooseFileLabel.setEnabled(true);
	textField1.setEnabled(true);
	ChooseFileButton.setEnabled(true);
    }
    
    
    /**
     * Collects data from the dialog and stores in resident
     * <CODE>SVMData</CODE>
     * @throws Exception thows exception if format is bad
     */    
    protected boolean parseData() {
        try{
        resultData.constant=Float.parseFloat( textField2.getText() );
        resultData.coefficient=Float.parseFloat( textField3.getText() );
        resultData.power=Float.parseFloat( textField4.getText() );
        resultData.diagonalFactor=Float.parseFloat(textField8.getText());
        resultData.convergenceThreshold=Float.parseFloat(textField9.getText());
        resultData.widthFactor=Float.parseFloat(textField5.getText());
        resultData.positiveConstraint=Float.parseFloat(textField6.getText());
        resultData.negativeConstraint=Float.parseFloat(textField7.getText());
        resultData.radial=CheckBox1.isSelected();
        //resultData.normalize=CheckBox2.isSelected();
        resultData.normalize = false;  //JB now using normalized dot prod. kernel by defaultd^2(x,y) = K(x,x) - 2 K(x,y) + K(y,y)
        resultData.constrainWeights=CheckBox3.isSelected();
	resultData.useEditor = editorCheckBox.isSelected();
        } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(SVMInitDialog.this, "Entry format error. "+e.getMessage()+" is not valid input.  Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        if(!resultData.validate(this))
            return false;
        return true;
        }

    
    public void resetControls(){
        
        editorCheckBox.setSelected(origData.useEditor);
        if(!origData.useEditor && origData.classificationFile != null)
            textField1.setText(origData.classificationFile.getPath());
        else
            textField1.setText("");
        textField2.setText(Float.toString(origData.constant)) ;
        textField3.setText(Float.toString(origData.coefficient));
        textField4.setText(Float.toString(origData.power));
        textField8.setText(Float.toString(origData.diagonalFactor));
        
        textField9.setText(Float.toString(origData.convergenceThreshold));
        textField5.setText(Float.toString(origData.widthFactor));
        textField6.setText(Float.toString(origData.positiveConstraint));
        
        textField7.setText(Float.toString(origData.negativeConstraint));
        CheckBox1.setSelected(origData.radial);
        CheckBox2.setSelected(false);
        CheckBox3.setSelected(origData.constrainWeights);
	editorCheckBox.setSelected(origData.useEditor);
        
    }
    
    public static void main(String [] args){
        SVMInitDialog dialog = new SVMInitDialog(new java.awt.Frame(), new SVMData());
        dialog.show();
        System.exit(0);
    }
    
    /**
     * Listens for state of SVC file/editor selection
     */    
    class CheckBoxListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {

            Object source = e.getItemSelectable();

            if (source == CheckBox1) {
                if (CheckBox1.isSelected()) {
                    Label1.setEnabled(true);
                    textField5.setEnabled(true);
                } else {
                    Label1.setEnabled(false);
                    textField5.setEnabled(false);
                }
            } else if (source == CheckBox3) {
                if (CheckBox3.isSelected()) {
                    Label2.setEnabled(true);
                    Label3.setEnabled(true);
                    textField6.setEnabled(true);
                    textField7.setEnabled(true);
                } else {
                    Label2.setEnabled(false);
                    Label3.setEnabled(false);
                    textField6.setEnabled(false);
                    textField7.setEnabled(false);
                }
            }
        }
    }

    private class Listener extends DialogListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {                    
                    if(parseData())
                        result = JOptionPane.OK_OPTION;
                    else
                        return;
            } catch (Exception exception) {
                    exception.printStackTrace();
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("choose-file-command")){
                onChooseFile();
	    } else if (command.equals("use-editor-command")){
		  if(editorCheckBox.isSelected())
		      disableFileSelection();
		  else
		      enableFileSelection();
	    } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                              HelpWindow hw = new HelpWindow(SVMInitDialog.this, "SVM Training Initialization Dialog");
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
            
        }

        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }

}