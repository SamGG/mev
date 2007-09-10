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
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.ChARM;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.PValue;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.CharmGUI;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.GraphViewPanel;

import com.borland.jbcl.layout.VerticalFlowLayout;
/**
* This class implements the dialog that allows users to export image or text results.
*
 * <p>Title: ExportResultsDialog </p>
 * <p>Description: This class implements the dialog that allows users to export image or text results.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University </p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class ExportResultsDialog extends JDialog {
  /**
   * Export type indicator: user selected image output.
   */
  public static final int EXPORT_IMAGE=0;
  /**
   * Export type indicator: user selected text output.
   */
  public static final int EXPORT_FLAT_FILE=1;
  /**
   * Exit status indicator: user clicked "Cancel" button.
   */
  public static final int CANCELLED=-1;
  /**
   * Exit status indicator: user clicked "OK" button.
   */
  public static final int OK_VERIFIED=1;


  JPanel contentPane = new JPanel();
  JRadioButton jRadioButton1 = new JRadioButton();
  JRadioButton jRadioButton2 = new JRadioButton();
  JButton jButton1 = new JButton();

  ButtonGroup buttonGroup1 = new ButtonGroup();

  private String filename;
  private int exportType;
  private int exitStatus;
  private Component parent;
  //private DisplayStateManager displayStateManager;
  private ChARM displayStateManager;
  
  JButton jButton3 = new JButton();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JTextField jTextField1 = new JTextField();
  JTextField jTextField2 = new JTextField();
  JButton jButton4 = new JButton();
  JLabel jLabel1 = new JLabel();
  JComboBox jComboBox1 = new JComboBox();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  TitledBorder titledBorder1;
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  TitledBorder titledBorder2;
  JPanel jPanel3 = new JPanel();
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();
  VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  FlowLayout flowLayout2 = new FlowLayout();
  FlowLayout flowLayout3 = new FlowLayout();
  JPanel jPanel6 = new JPanel();
  JPanel jPanel7 = new JPanel();
  FlowLayout flowLayout5 = new FlowLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();


  /**
   * Class constructor.
   * @param parent Component
   * @param dispState DisplayStateManager- reference to main state container class
   * @throws HeadlessException
   */
  public ExportResultsDialog(Component parent, ChARM dispState) throws HeadlessException {
    super();
    this.parent = parent;
    this.displayStateManager = dispState;

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
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Output options");
    titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"P-value cutoffs");
    this.setSize(new Dimension(343, 404));
    this.setTitle("Export Results");
    this.setModal(false);
    this.setResizable(false);
    contentPane.setPreferredSize(new Dimension(100, 200));

    jRadioButton1.setText("Export current display to image");
    jRadioButton1.setActionCommand("IMAGE");

    jRadioButton2.setText("Export results to text file");
    jRadioButton2.setActionCommand("FLATFILE");
    jTextField1.setMinimumSize(new Dimension(80, 21));
    jTextField1.setPreferredSize(new Dimension(50, 21));
    jTextField2.setPreferredSize(new Dimension(50, 21));
    verticalFlowLayout2.setAlignment(VerticalFlowLayout.MIDDLE);
    jPanel3.setLayout(flowLayout1);
    jPanel4.setLayout(flowLayout2);
    jPanel5.setLayout(flowLayout3);
    flowLayout2.setAlignment(FlowLayout.LEFT);
    flowLayout3.setAlignment(FlowLayout.LEFT);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    jButton4.setMaximumSize(new Dimension(150, 25));
    jButton4.setMinimumSize(new Dimension(115, 25));
    jButton4.setPreferredSize(new Dimension(130, 20));
    jPanel6.setLayout(flowLayout5);
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton2);
    buttonGroup1.setSelected(jRadioButton1.getModel(),true);

    jPanel1.setLayout(verticalFlowLayout1);
    jPanel1.setBorder(titledBorder1);

    jPanel2.setBorder(titledBorder2);
    jPanel2.setLayout(verticalFlowLayout2);

    jLabel1.setText("Filter type:");
    jLabel2.setText("Sign p-value cutoff:");
    jLabel3.setText("Mean  p-value cutoff:");
    PValue currPvalCutoff = displayStateManager.getPvalueCutoff();
    jTextField1.setText(currPvalCutoff.getSignPvalue()+"");
    jTextField2.setText(currPvalCutoff.getMeanPvalue()+"");

    String[] cutoff_types = new String[] {
        "Sign AND Mean Tests",
        "Sign OR Mean Tests",
        "Sign Test",
        "Mean Test"};


    jComboBox1 = new JComboBox(cutoff_types);
    jComboBox1.setSelectedIndex(displayStateManager.getPValueTestType());
    jComboBox1.setToolTipText("Select the p-value cutoffs that exported predictions must meet");
    jComboBox1.addActionListener(new CutoffSelectionListener());
    jButton4.setText("Update display");
    jButton4.addActionListener(new CutoffListener());


    jButton1.setText("Next >");
    jButton1.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String choice = buttonGroup1.getSelection().getActionCommand();
        if(choice.equals("IMAGE")) {
          exportType = ExportResultsDialog.EXPORT_IMAGE;
        }
        else if(choice.equals("FLATFILE")) {
          exportType = ExportResultsDialog.EXPORT_FLAT_FILE;
        }

        if(getExportType() == ExportResultsDialog.EXPORT_IMAGE) {
          JFileChooser fileDialog = new JFileChooser();
          //add all supported image types
          String[] imageWriters = javax.imageio.ImageIO.getWriterFormatNames();
          HashMap typeHash = new HashMap();
            for(int i=0; i<imageWriters.length; i++) {
              if(!typeHash.containsKey(imageWriters[i].toUpperCase())) {
                ExampleFileFilter filter = new ExampleFileFilter();
                filter.addExtension(imageWriters[i].toUpperCase());
                filter.setDescription(imageWriters[i].toUpperCase());
                fileDialog.addChoosableFileFilter(filter);
                typeHash.put(imageWriters[i].toUpperCase(),new String("1"));
              }
          }
          int returnVal = fileDialog.showSaveDialog(ExportResultsDialog.this);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            String file = fileDialog.getSelectedFile().getAbsolutePath();
            filename = file;
            String currExt = fileDialog.getFileFilter().getDescription();
            currExt = currExt.substring(0,currExt.indexOf(' '));
            int printStatus = displayStateManager.getGraphPanel().printToFile(
                filename + "." + currExt,
                currExt);
            if (printStatus < 0) JOptionPane.showMessageDialog(null,
                "Error writing to file!  Please try again.",
                "File Output Error", JOptionPane.ERROR_MESSAGE);

           exitStatus = ExportResultsDialog.OK_VERIFIED;
          }
          closeDialog();
        }

        else {
          JFileChooser fileDialog = new JFileChooser();

          int returnVal = fileDialog.showSaveDialog(ExportResultsDialog.this);


          if (returnVal == JFileChooser.APPROVE_OPTION) {
            String file = fileDialog.getSelectedFile().getAbsolutePath();
            filename = file;

            int printStatus = displayStateManager.printVisiblePredictionWindows(filename+CharmGUI.EXTENSION);
            if(printStatus < 0) JOptionPane.showMessageDialog(null,"Error writing to file!  Please try again.","File Output Error", JOptionPane.ERROR_MESSAGE);
            exitStatus = ExportResultsDialog.OK_VERIFIED;
          }
          closeDialog();
        }
      }
      });

  jButton3.setText("Cancel");
  jButton3.addActionListener(new ActionListener(){
  public void actionPerformed(ActionEvent e) {
    exitStatus = ExportResultsDialog.CANCELLED;
    closeDialog();
  }
});


  contentPane.setLayout(gridBagLayout1);
    jPanel1.add(jRadioButton1, null);
    jPanel1.add(jRadioButton2, null);
    contentPane.add(jPanel1,        new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 32, 0, 34), 0, 11));
    contentPane.add(jPanel2,       new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(23, 32, 11, 33), 0, 12));
    contentPane.add(jPanel7,    new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 66, 8, 67), 0, 0));
    jPanel3.add(jLabel1, null);
    jPanel3.add(jComboBox1, null);
    jPanel2.add(jPanel3, null);
    jPanel2.add(jPanel4, null);
    jPanel2.add(jPanel5, null);
    jPanel4.add(jLabel2, null);
    jPanel4.add(jTextField1, null);
    jPanel2.add(jPanel6, null);
    jPanel6.add(jButton4, null);
    jPanel5.add(jLabel3, null);
    jPanel5.add(jTextField2, null);
    jPanel7.add(jButton1, null);
    jPanel7.add(jButton3, null);
    this.getContentPane().add(contentPane, BorderLayout.CENTER);

  }

  /**
   * Returns the user-specified filename to which results will be written.
   * @return String
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Verifies that all required fields have been completed.
   * @return boolean
   */
  private boolean verifyValidReturnState() {
    boolean valid = true;

    if(filename == null || filename.length() == 0) {
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
   * Returns the user exit status (CANCELLED or OK_VERIFIED).
   * @return int
   */
  public int getExitStatus() {
    return exitStatus;
  }

  /**
   * Returns export type (EXPORT_FLAT_FILE or EXPORT_IMAGE).
   * @return int
   */
  public int getExportType() {
    return exportType;
  }

  /**
   *
   * <p>Title: CutoffSelectionListener</p>
   * <p>Description: Handles all UI updates to the cutoff type selection component.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class CutoffSelectionListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      int index = jComboBox1.getSelectedIndex();
      if(index == 2) { jTextField1.setEnabled(true); jTextField2.setEnabled(false);}
      else if(index == 3)  { jTextField2.setEnabled(true); jTextField1.setEnabled(false);}
      else {
        jTextField1.setEnabled(true);
        jTextField2.setEnabled(true);
      }
    }
  }

  /**
   *
   * <p>Title: CutoffListener</p>
   * <p>Description: Handles all UI updates to the cutoff selection component.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */

  private class CutoffListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {

      try{
        Double signCutoff = Double.valueOf(jTextField1.getText());
        Double meanCutoff = Double.valueOf(jTextField2.getText());

        PValue filterPval = new PValue(meanCutoff.doubleValue(),
                                       signCutoff.doubleValue());
        jComboBox1.validate();
        int index = jComboBox1.getSelectedIndex();
        int testType = 0;
        switch (index) {
          case 0:
            testType = PValue.MEAN_AND_SIGN_TEST;
            break;
          case 1:
            testType = PValue.MEAN_OR_SIGN_TEST;
            break;
          case 2:
            testType = PValue.SIGN_TEST;
            break;
          case 3:
            testType = PValue.MEAN_TEST;
            break;
        }

        displayStateManager.setPValueCutoff(filterPval);
        displayStateManager.setPValueTestType(testType);
        GraphViewPanel graphPanel = displayStateManager.getGraphPanel();
        graphPanel.initializePredictionNodes(displayStateManager.getSelectedExperiments());
        graphPanel.updateGraph();
      }
      catch(java.lang.NumberFormatException exc) {
        JOptionPane.showMessageDialog(ExportResultsDialog.this,"Please enter P-values between 0 and 1.","Error",JOptionPane.ERROR_MESSAGE);

      }
    }
}
}
