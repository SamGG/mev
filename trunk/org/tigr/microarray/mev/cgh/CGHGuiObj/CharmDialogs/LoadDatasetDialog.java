package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.borland.jbcl.layout.BoxLayout2;
import com.borland.jbcl.layout.VerticalFlowLayout;

/**
* This class implements the dialog that allows users to load new pcl files.
*
 * <p>Title: LoadDatasetDialog</p>
 * <p>Description: This class implements the dialog that allows users to load new pcl files.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class LoadDatasetDialog extends JDialog {
  JPanel contentPane = new JPanel();
  JRadioButton jRadioButton1 = new JRadioButton();
  JRadioButton jRadioButton2 = new JRadioButton();
  JRadioButton jRadioButton3 = new JRadioButton();
  JButton jButton1 = new JButton();

  private String organism;
  private String datasetName;
  private String filename;

  private int exitStatus;

  /**
   * Exit status indicator- user pressed "Cancel".
   */
  static final int CANCELLED=-1;
  /**
   * Exit status indicator- user pressed "OK".
   */
  public static final int OK_VERIFIED=1;


  Component parent;

  ButtonGroup buttonGroup1 = new ButtonGroup();
  JTextField jTextField1 = new JTextField();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JTextField jTextField2 = new JTextField();
  JButton jButton2 = new JButton();
  JButton jButton3 = new JButton();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  TitledBorder titledBorder3;
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();
  VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  FlowLayout flowLayout2 = new FlowLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  BoxLayout2 boxLayout21 = new BoxLayout2();

  /**
   * Class constructor.
   * @param parent Component
   * @throws HeadlessException
   */
  public LoadDatasetDialog(Component parent) throws HeadlessException {
    super();
    this.parent = parent;

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
    titledBorder1 = new TitledBorder("");
    titledBorder2 = new TitledBorder("");
    titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Organism");
    this.setSize(new Dimension(345, 309));
    this.setTitle("Load dataset");
    this.setModal(true);
    this.setResizable(false);
    contentPane.setPreferredSize(new Dimension(100, 200));

    jRadioButton1.setText("Saccharomyces cerevisiae");
    jRadioButton1.setBorder(titledBorder1);
    jRadioButton1.setActionCommand("YEAST");
    jRadioButton2.setText("Human");
    jRadioButton2.setBorder(titledBorder1);
    jRadioButton2.setActionCommand("HUMAN");
    jRadioButton3.setText("Other");
    jRadioButton3.setBorder(titledBorder1);
    jRadioButton3.setActionCommand("OTHER");
    jRadioButton3.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       JOptionPane.showMessageDialog(null,"Please remember to include chromosomal coordinates in input file.  See help file for details.","Warning", JOptionPane.WARNING_MESSAGE);
     }
    });

    jLabel1.setText("Dataset name:");
    jLabel2.setText("File: ");

    jButton2.setText("Browse...");
    jButton2.addActionListener(new ActionListener () {
                               public void actionPerformed(ActionEvent e) {
      JFileChooser fileDialog = new JFileChooser();
      int returnVal = fileDialog.showOpenDialog(parent);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String file = fileDialog.getSelectedFile().getAbsolutePath();
        jTextField2.setText(file);
        filename = file;
      }
     }
    });

    jTextField2.setPreferredSize(new Dimension(100, 21));
    jTextField2.setText("");
    jTextField1.setPreferredSize(new Dimension(60, 21));
    jTextField1.setText("dataset1");

    jPanel3.setLayout(boxLayout21);
    jPanel3.setBorder(titledBorder3);
    jPanel5.setLayout(verticalFlowLayout2);
    verticalFlowLayout2.setAlignment(VerticalFlowLayout.TOP);
    jPanel2.setLayout(flowLayout1);
    jPanel1.setLayout(flowLayout2);
    flowLayout2.setAlignment(FlowLayout.LEFT);
    boxLayout21.setAxis(BoxLayout.Y_AXIS);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton2);
    buttonGroup1.add(jRadioButton3);
    buttonGroup1.setSelected(jRadioButton1.getModel(),true);

    contentPane.setLayout(gridBagLayout1);
    jPanel1.add(jLabel2, null);
    jPanel1.add(jTextField2, null);
    jPanel1.add(jButton2, null);
    jPanel5.add(jPanel1, null);
    jPanel5.add(jPanel2, null);
    jPanel2.add(jLabel1, null);
    jPanel2.add(jTextField1, null);
    contentPane.add(jPanel5,                     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 39, 0, 14), 46, 0));
    contentPane.add(jPanel3,                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 34, 0, 46), 102, 0));
    jPanel3.add(jRadioButton1, null);
    jPanel3.add(jRadioButton2, null);
    jPanel3.add(jRadioButton3, null);
    contentPane.add(jPanel4,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 5, 9, 5), 140, 0));
    jPanel4.add(jButton1, null);
    jPanel4.add(jButton3, null);
    this.getContentPane().add(contentPane, BorderLayout.CENTER);


    jButton1.setText("Load");
    jButton1.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        jTextField1.validate();
        jTextField2.validate();

        filename = jTextField2.getText();
        datasetName = jTextField1.getText();
        datasetName.trim();
        organism = buttonGroup1.getSelection().getActionCommand();

        if(!verifyValidReturnState()) {
          JOptionPane.showMessageDialog(null,"Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
          exitStatus = LoadDatasetDialog.OK_VERIFIED;
          closeDialog();
        }
      }
    }
    );

  jButton3.setText("Cancel");
  jButton3.addActionListener(new ActionListener(){
  public void actionPerformed(ActionEvent e) {
    exitStatus = LoadDatasetDialog.CANCELLED;
    closeDialog();
  }
});
  }

  /**
   * Returns the user-specified organism type.
   * @return String
   */
  public String getOrganism() {
    return organism;
  }

  /**
   * Returns the user-specified dataset name.
   * @return String
   */
  public String getDatasetName() {
    return datasetName;
  }

  /**
   * Returns the user-selected input filename.
   * @return String
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Verifies all required fields have been completed.
   * @return boolean
   */
  private boolean verifyValidReturnState() {
    boolean valid = true;

    if(filename == null || filename.length() == 0) {
      valid = false;
    }

    if(datasetName == null || datasetName.length() == 0) {
      valid = false;
    }

    if(organism == null || organism.length() == 0) {
      valid = false;
    }

    return valid;
  }

  /**
   * Closes this dialog.
   */
  private void closeDialog() {
    this.hide();
  }
  /**
   * Returns exit status.
   * @return int- options: CANCELLED, OK_VERIFIED
   */
  public int getExitStatus() {
    return exitStatus;
  }
}
