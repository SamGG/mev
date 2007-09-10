package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
import org.tigr.microarray.mev.cluster.gui.IData;

import com.borland.jbcl.layout.VerticalFlowLayout;
/**
* This class implements the dialog that allows users to test
 * manually-selected regions for significance.
*
 * <p>Title: TestSignificanceDialog</p>
 * <p>Description: This class implements the dialog that allows users to test
 * manually-selected regions for significance.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class TestSignificanceDialog extends JDialog {
  JPanel jPanel1 = new JPanel();
  JRadioButton jRadioButton1 = new JRadioButton();
  JComboBox jComboBox1 = new JComboBox();
  JTextField jTextField1 = new JTextField();
  JPanel jPanel2 = new JPanel();
  JRadioButton jRadioButton2 = new JRadioButton();
  Border border1;
  TitledBorder titledBorder1;
  JButton jButton1 = new JButton();
  JButton jButton2 = new JButton();
  JPanel jPanel3 = new JPanel();
  Border border2;
  TitledBorder titledBorder2;
  ButtonGroup buttons = new ButtonGroup();

  //private DisplayStateManager displayStateManager;
  private ChARM displayStateManager;
  private String analysisID;
  private ArrayList selectedExps;
  private ArrayList allExps;
  private int exitStatus;
  private SegmentInfo segInfo;
  //private Chromosome chromosome;
  private int chromosome;
  private String experiment;

  /**
   * Status indicator- user canceled significance test.
   */
  public static int EXIT_CANCELLED=0;
  /**
   * Status indicator- significance test finished.
   */
  public static int EXIT_FINISHED=1;
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JLabel jLabel6 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel jLabel8 = new JLabel();
  JLabel jLabel9 = new JLabel();
  JLabel jLabel10 = new JLabel();
  JLabel jLabel11 = new JLabel();
  JLabel jLabel12 = new JLabel();
  JLabel jLabel13 = new JLabel();
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  FlowLayout flowLayout2 = new FlowLayout();
  JPanel jPanel6 = new JPanel();
  JPanel jPanel7 = new JPanel();
  FlowLayout flowLayout3 = new FlowLayout();
  FlowLayout flowLayout4 = new FlowLayout();
  JPanel jPanel8 = new JPanel();
  FlowLayout flowLayout5 = new FlowLayout();
  JPanel jPanel9 = new JPanel();
  FlowLayout flowLayout6 = new FlowLayout();
  JPanel jPanel10 = new JPanel();
  JPanel jPanel11 = new JPanel();
  VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
  FlowLayout flowLayout7 = new FlowLayout();
  FlowLayout flowLayout8 = new FlowLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();


  /**
   * Class constructor.
   * @param displayStateManager DisplayStateManager- state information container reference
   * @param testWindow Window- the manually-selected prediction to be tested
   * @param chrom Chromosome- chromosome object associated with <code>testWindow</code>
   * @param exp String- experiment associated with <code>testWindow</code>
   * @throws HeadlessException
   */
  public TestSignificanceDialog(/*DisplayStateManager*/ ChARM displayStateManager, SegmentInfo testSeg, int chrom, String exp) throws HeadlessException {

    this.displayStateManager = displayStateManager;
    this.segInfo = testSeg;
    this.chromosome = chrom;
    this.experiment = exp;

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
    this.setTitle("Test Significance");
    this.setSize(new Dimension(353, 394));
    this.setResizable(false);
    this.setTitle("Analyze Data");
    this.setModal(true);

    border2 = BorderFactory.createEtchedBorder(Color.white,new Color(165, 163, 151));
    titledBorder2 = new TitledBorder(border2,"Status");
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(165, 163, 151));
    titledBorder1 = new TitledBorder(border1,"Analysis ID");
    jRadioButton1.setText("Create new ID:");
    jRadioButton1.setActionCommand("create");
    jRadioButton1.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      jComboBox1.setEnabled(false);
      jTextField1.setEnabled(true);
    }
    });

    //ArrayList resultSets = displayStateManager.getResultSets();
    ResultContainer resultSet = displayStateManager.getSelectedResultSets();
    //for (int i =0; i< resultSets.size(); i++) {
      //jComboBox1.addItem(((ResultContainer)resultSets.get(i)).getResultID());
      jComboBox1.addItem(resultSet.getResultID());
    //}
      /*
    if(resultSets.size() == 0) {
      jComboBox1.setEnabled(false);
      jRadioButton2.setEnabled(false);
      jRadioButton1.setSelected(true);
      jTextField1.setEnabled(true);
    }
    else {
    */
      jComboBox1.setEnabled(true);
      jRadioButton2.setEnabled(true);
      jRadioButton2.setSelected(true);
      jTextField1.setEnabled(false);
    //}

    int totSets = 1; /* resultSets.size()+1;*/
    jTextField1.setPreferredSize(new Dimension(70, 21));
    jTextField1.setText("results"+totSets);

    jPanel2.setBorder(titledBorder1);
    jPanel2.setLayout(verticalFlowLayout1);
    jRadioButton2.setText("Associate with existing ID:");
    jRadioButton2.setActionCommand("add");
    jRadioButton2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jComboBox1.setEnabled(true);
        jTextField1.setEnabled(false);
      }
    });

    jLabel5.setDebugGraphicsOptions(0);
    jLabel5.setText("Mean test:");
    jLabel6.setText("Sign test (pos):");
    jLabel7.setText("Sign test (neg):");
    jLabel8.setText("");
    jLabel9.setText("");
    jLabel10.setText("");
    jLabel11.setText("");
    jLabel12.setText("");
    jLabel13.setText("");
    jComboBox1.setPreferredSize(new Dimension(70, 21));
    jPanel5.setLayout(flowLayout1);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    jPanel4.setLayout(flowLayout2);
    flowLayout2.setAlignment(FlowLayout.LEFT);

    flowLayout3.setAlignment(FlowLayout.LEFT);
    jPanel7.setLayout(flowLayout4);
    flowLayout4.setAlignment(FlowLayout.LEFT);
    jPanel8.setLayout(flowLayout5);
    flowLayout5.setAlignment(FlowLayout.LEFT);
    jPanel9.setLayout(flowLayout6);
    flowLayout6.setAlignment(FlowLayout.LEFT);
    flowLayout6.setHgap(30);
    jPanel11.setLayout(flowLayout7);
    flowLayout7.setAlignment(FlowLayout.LEFT);
    flowLayout7.setHgap(30);
    flowLayout7.setVgap(5);
    jPanel10.setLayout(flowLayout8);
    flowLayout8.setAlignment(FlowLayout.LEFT);
    flowLayout8.setHgap(30);
    buttons.add(jRadioButton1);
    buttons.add(jRadioButton2);

    jPanel1.setLayout(gridBagLayout1);
    jButton1.setText("Start");
    jButton1.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       jLabel4.setText("Running significance tests...");
       /* Get Ratio of the Exprs & Chr */
       IData data = displayStateManager.getData();
       ArrayList Exprs = data.getFeaturesList();
       int exprIndx = -1;
       for(int i = 0; i < Exprs.size(); i++){
    	   if(experiment.equals((String)Exprs.get(i))){
    		   exprIndx = i;
    		   break;
    	   }
       }
       
       float [] exprRatios = new float[data.getNumDataPointsInChrom(chromosome)];
       for(int clone = 0; clone < exprRatios.length; clone++) {
    	   exprRatios[0] = data.getValue(exprIndx, clone, chromosome);
  	   }
  	 
       SigTestThread tester = new SigTestThread(exprRatios, chromosome, experiment, segInfo); //new SigTestThread(chromosome, experiment, segInfo);
       tester.setTests(true, true,false,false);
       tester.setNumberPermutations(10);
       tester.runPermuteTest();
       tester.runSignTest();
       //tester.setPriority(Thread.MIN_PRIORITY);
       //tester.start();
       //try{tester.join();} catch(Exception exc) { exc.printStackTrace();}


       String IDselection = buttons.getSelection().getActionCommand();
       ResultContainer results;
       jTextField1.validate();
       if(IDselection.equals("create")) {
         analysisID = jTextField1.getText();
         analysisID.trim();
         jLabel4.setText("Results saved to "+analysisID+".");

         //results = new ResultContainer(displayStateManager.getCurrentDataset());
         //results.setResultID(analysisID);
         //results.addSegment(segInfo,experiment,chromosome.getNumber());
         //displayStateManager.addResultSet(results);
         //displayStateManager.addSelectedResultSet(results.getResultID());
       }
       else if(IDselection.equals("add")) {
         analysisID = (String)jComboBox1.getSelectedItem();
         analysisID.trim();
         jLabel4.setText("Results saved to "+analysisID+".");
         //results = displayStateManager.getResultSet(analysisID);
         //results.addSegment(segInfo,experiment,chromosome.getNumber());
         //displayStateManager.addSelectedResultSet(results.getResultID());
       }
       jLabel1.setText(segInfo.getStatistic("Mean")+"");
       jLabel12.setText(segInfo.getStatistic("Sign pos")+"");
       jLabel13.setText(segInfo.getStatistic("Sign neg")+"");

       jButton1.setEnabled(false);
       exitStatus = TestSignificanceDialog.EXIT_FINISHED;

     }
    });


    jButton2.setText("Close");
    jButton2.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       if(exitStatus != TestSignificanceDialog.EXIT_FINISHED) exitStatus = TestSignificanceDialog.EXIT_CANCELLED;
       hide();
     }
    });


    jPanel3.setBorder(titledBorder2);
    jPanel3.setDebugGraphicsOptions(0);
    jPanel3.setLayout(verticalFlowLayout2);
    jLabel1.setText("");
    jLabel2.setText("Current Activity:");
    jLabel3.setText("Results:");
    jLabel4.setText("");
    jPanel4.add(jRadioButton1, null);
    jPanel4.add(jTextField1, null);
    jPanel1.add(jPanel2, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 14, 0, 12), 75, 3));
    jPanel1.add(jPanel3, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 15, 0, 12), 149, 15));
    jPanel5.add(jRadioButton2, null);
    jPanel5.add(jComboBox1, null);
    jPanel2.add(jPanel4, null);
    jPanel2.add(jPanel5, null);
    jPanel7.add(jLabel2, null);
    jPanel7.add(jLabel4, null);
    jPanel3.add(jPanel7, null);
    jPanel3.add(jPanel8, null);
    jPanel8.add(jLabel3, null);
    jPanel3.add(jPanel11, null);
    jPanel9.add(jLabel5, null);
    jPanel9.add(jLabel1, null);
    jPanel3.add(jPanel10, null);
    jPanel10.add(jLabel6, null);
    jPanel10.add(jLabel12, null);
    jPanel11.add(jLabel7, null);
    jPanel11.add(jLabel13, null);
    jPanel3.add(jPanel9, null);
    jPanel6.add(jButton1, null);
    jPanel6.add(jButton2, null);
    jPanel1.add(jPanel6,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 5, 8, 5), 202, 0));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
  }

  /**
   * Returns exit status.
   * @return int
   */
  public int getExitStatus() {
    return exitStatus;
  }

  /**
   * Returns the user-specified analysis ID.
   * @return String
   */
  public String getAnalysisID() {
   return this.analysisID;
 }
  public static void main(String args[]){
	  
  }
}
