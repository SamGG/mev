package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import com.borland.jbcl.layout.BoxLayout2;
import com.borland.jbcl.layout.VerticalFlowLayout;

/**
* This dialog allows users to configure data analysis runs.
*
 * <p>Title: CharmAnalyzeDialog </p>
 * <p>Description: This dialog allows users to configure data analysis runs.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */
public class CharmAnalyzeDialog extends JDialog {
  JPanel contentPane = new JPanel();
  JScrollPane jScrollPane1 = new JScrollPane();
  JPanel jPanel1 = new JPanel();
  JButton jButton1 = new JButton();
  JButton jButton2 = new JButton();
  //JTextField jTextField1 = new JTextField();
  
  private CheckboxListener checkBoxListener = new CheckboxListener();

  //private String analysisID;
  private ArrayList selectedExps;
  private ArrayList allExps;
  private int exitStatus;

  //private Component parent;
  //private DisplayStateManager displayStateManager;

  /**
   * Indicator flag to indicate user clicked "OK".
   */
  public final static int OK_VERIFIED=1;
  /**
   * Indicator flag to indicate user clicked "Cancel".
   */
  public final static int CANCEL=2;
  /**
   * Exit status indicator- user pressed "Cancel".
   */
  static final int CANCELLED=-1;
  
  JButton jButton3 = new JButton();
  BoxLayout2 boxLayout21 = new BoxLayout2();
  JButton jButton4 = new JButton();
  //JRadioButton jRadioButton1 = new JRadioButton();
  //JRadioButton jRadioButton2 = new JRadioButton();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  //JComboBox jComboBox1 = new JComboBox();
  ButtonGroup buttons = new ButtonGroup();
  //JPanel jPanel4 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JPanel jPanel5 = new JPanel();
  FlowLayout flowLayout2 = new FlowLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  //TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  JPanel jPanel6 = new JPanel();
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  JPanel jPanel7 = new JPanel();
  VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();


  /**
   * Class constructor.
   * @param displayStateManager DisplayStateManager- display state information container class reference
   * @param parent Component
   * @throws HeadlessException
   */
  //public CharmAnalyzeDialog(DisplayStateManager displayStateManager,Component parent) throws HeadlessException {
  public CharmAnalyzeDialog(ArrayList exprs) throws HeadlessException {
    super();
    //this.parent = parent;

    //this.displayStateManager = displayStateManager;
    //this.allExps = displayStateManager.getExperimentList();
    this.allExps = exprs;
    this.selectedExps = new ArrayList();


    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Dialog initialization.
   * @throws Exception
   */
  private void jbInit() throws Exception {
    //titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Analysis ID");
    titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Select experiments to be analyzed");
    this.setSize(new Dimension(321, 335));
    this.setResizable(false);
    this.setTitle("ChARM Analysis");
    this.setModal(true);
    contentPane.setLayout(verticalFlowLayout2);
    jPanel1.setBackground(Color.white);
    jPanel1.setLayout(boxLayout21);

    jButton1.setText("Select All");
    jButton1.setMaximumSize(new Dimension(150, 20));
    jButton1.setMinimumSize(new Dimension(79, 20));
    jButton1.setPreferredSize(new Dimension(105, 20));
    jButton1.setActionCommand("Select all");
    jButton1.addActionListener(checkBoxListener);

    jButton4.addActionListener(checkBoxListener);
    jButton4.setMaximumSize(new Dimension(150, 20));
    jButton4.setMinimumSize(new Dimension(79, 20));
    jButton4.setPreferredSize(new Dimension(105, 20));
    jButton4.setActionCommand("Remove all");
    jButton4.setText("Remove All");

    jButton2.setText("Start Analysis");
    jButton2.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        
        /*
        String IDselection = buttons.getSelection().getActionCommand();
        if(IDselection.equals("create")) {
          analysisID = jTextField1.getText();
        }
        else if(IDselection.equals("add")) {
          analysisID = (String)jComboBox1.getSelectedItem();
        }

        analysisID.trim();
		*/
    	//String IDselection = jButton2.getActionCommand();
        String errorMsg = verifyValidReturnState();
        if(! errorMsg.equals("true")) {
          JOptionPane.showMessageDialog(null,errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
          //exitStatus = LoadDatasetDialog.OK_VERIFIED; Raktim 8/31
        	exitStatus = OK_VERIFIED;
          hide();
        }
      }
    }
    );

    jButton3.setText("Cancel");
    jButton3.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        //exitStatus = LoadDatasetDialog.CANCELLED; Raktim 8/31
    	  exitStatus = CANCELLED;
        hide();
      }
    }
    );




    jButton3.setMaximumSize(new Dimension(103, 25));
    jButton3.setMinimumSize(new Dimension(103, 25));
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane1.setPreferredSize(new Dimension(265, 150));
    boxLayout21.setAxis(BoxLayout.Y_AXIS);

    /*
    jRadioButton1.setText("Create new ID:");
    jRadioButton1.setActionCommand("create");
    jRadioButton1.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      jComboBox1.setEnabled(false);
      jTextField1.setEnabled(true);
    }
    });
    jRadioButton2.setText("Associate with existing ID:");
    jRadioButton2.setActionCommand("add");
    jRadioButton2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jComboBox1.setEnabled(true);
        jTextField1.setEnabled(false);
      }
    });
	*/


    //jPanel2.setBorder(titledBorder1);
    jPanel2.setLayout(borderLayout1);
    jPanel3.setBorder(titledBorder2);
    jPanel3.setLayout(verticalFlowLayout1);
    //jPanel4.setLayout(flowLayout1);
    jPanel5.setLayout(flowLayout2);
    flowLayout2.setAlignment(FlowLayout.LEFT);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    verticalFlowLayout1.setAlignment(VerticalFlowLayout.MIDDLE);
    //jTextField1.setMinimumSize(new Dimension(50, 21));
    //jTextField1.setPreferredSize(new Dimension(80, 21));
    //jComboBox1.setPreferredSize(new Dimension(80, 21));
    //buttons.add(jRadioButton1);
    //buttons.add(jRadioButton2);
    //jRadioButton1.setSelected(true);
    //jComboBox1.setEnabled(false);

    /*
    ArrayList resultSets = displayStateManager.getResultSets();
    for (int i =0; i< resultSets.size(); i++) {
      jComboBox1.addItem(((ResultContainer)resultSets.get(i)).getResultID());
    }
    if(resultSets.size() == 0) {
      jComboBox1.setEnabled(false);
      jRadioButton2.setEnabled(false);
    }
    else {
      jComboBox1.setEnabled(true);
      jRadioButton2.setEnabled(true);
      jRadioButton2.setSelected(true);
      jTextField1.setEnabled(false);
    }

    int totSets = resultSets.size()+1;
   jTextField1.setText("results"+totSets);
	*/
    this.getContentPane().add(contentPane, BorderLayout.CENTER);
    contentPane.add(jPanel2, null);
    //jPanel2.add(jPanel4,  BorderLayout.NORTH);
    //jPanel4.add(jRadioButton1, null);
    //jPanel4.add(jTextField1, null);
    jPanel2.add(jPanel5, BorderLayout.SOUTH);
    //jPanel5.add(jRadioButton2, null);
    //jPanel5.add(jComboBox1, null);
    contentPane.add(jPanel3, null);
    jPanel3.add(jScrollPane1, null);
    jPanel3.add(jPanel6, null);
    jPanel6.add(jButton1, null);
    jPanel6.add(jButton4, null);
    contentPane.add(jPanel7, null);
    jPanel7.add(jButton2, null);
    jPanel7.add(jButton3, null);
    jScrollPane1.getViewport().add(jPanel1, null);


    for (int i = 0; i < allExps.size(); i++) {
      JCheckBox checkbox = new JCheckBox((String)allExps.get(i));
      checkbox.setBackground(Color.white);
      checkbox.addActionListener(checkBoxListener);
      jPanel1.add(checkbox);
    }

    jScrollPane1.setViewportView(jPanel1);

  }

  /**
   * Returns the user-specified analysis ID.
   * @return String
   */
  
  public int[] getSelectedExperimentsIndices() {
	  int[] indices = new int[selectedExps.size()];
	  
	  for(int i = 0; i < selectedExps.size(); i++){
		  if (allExps.contains(selectedExps.get(i))) {
			  indices[i] = allExps.indexOf(selectedExps.get(i));
	       }
		  else return null;
	  }
	  return indices;
  }
  /**
   * Verifies that all required fields have been completed.
   * @return String- indicates true or the error message to be displayed
   */
  private String verifyValidReturnState() {
	  String valid = new String("true");
	  
	  if(selectedExps == null || selectedExps.size() == 0) {
	    valid = new String("Please select at least one experiment.");
	  }
	
	  return valid;
}

/**
 * Returns exit status (OK, or CANCEL).
 * @return int
 */
public int getExitStatus() {
  return exitStatus;
}


/**
 * <p>Title:CheckboxListener</p>
 * <p>Description: Inner class for check box selection functionality</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.4
 */
private class CheckboxListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals("Select all")) {
        for (int i = 0; i < allExps.size(); i++) {
          JCheckBox curr = (JCheckBox) jPanel1.getComponent(i);
          curr.setSelected(true);
            if (!selectedExps.contains(allExps.get(i))) {
              selectedExps.add(allExps.get(i));
            }
          }
      }
      else if(e.getActionCommand().equals("Remove all")) {
        for (int i = 0; i < allExps.size(); i++) {
          JCheckBox curr = (JCheckBox) jPanel1.getComponent(i);
          curr.setSelected(false);
            if (selectedExps.contains(allExps.get(i))) {
              selectedExps.remove(allExps.get(i));
            }
          }
      }

      else {
        for (int i = 0; i < allExps.size(); i++) {
          JCheckBox curr = (JCheckBox) jPanel1.getComponent(i);
          if (e.getSource() == curr) {
            if (curr.isSelected() && !selectedExps.contains(allExps.get(i))) {
              selectedExps.add(allExps.get(i));
            }
            else {
              selectedExps.remove(allExps.get(i));
            }
          }
        }
      }
    }

  }
	public static void main(String args[]){
		ArrayList exprs = new ArrayList();
		exprs.add("Expr_A");
		exprs.add("Expr_B");
		exprs.add("Expr_C");
		exprs.add("Expr_D");
		exprs.add("Expr_E");
		exprs.add("Expr_A");
		exprs.add("Expr_B");
		exprs.add("Expr_C");
		exprs.add("Expr_D");
		exprs.add("Expr_E");
		CharmAnalyzeDialog analyzeData = new CharmAnalyzeDialog(exprs);
        analyzeData.setVisible(true);
	}
}
