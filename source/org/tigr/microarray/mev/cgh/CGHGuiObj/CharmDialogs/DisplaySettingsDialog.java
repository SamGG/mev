package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs;

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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.ChARM;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.GraphViewPanel;

import com.borland.jbcl.layout.BoxLayout2;
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class DisplaySettingsDialog extends JDialog {
  /**
   * Exit status indicator: user pressed "Ok"
   */
  public static int EXIT_STATUS_OK=0;
  /**
   * Exit status indicator: user pressed "Cancel"
   */
  public static int EXIT_STATUS_CANCEL=0;

  //private DisplayStateManager displayStateManager;
  private ChARM displayStateManager;
  int exitStatus;
  double currAxisScale=1;

  private Color backgroundColor;

  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  BoxLayout2 boxLayout21 = new BoxLayout2();
  JPanel jPanel3 = new JPanel();
  JButton jButton2 = new JButton();
  JButton jButton3 = new JButton();
  JPanel jPanel4 = new JPanel();
  JCheckBox jCheckBox1 = new JCheckBox();
  JCheckBox jCheckBox2 = new JCheckBox();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel4 = new JLabel();
  FlowLayout flowLayout1 = new FlowLayout();
  JTextField jTextField1 = new JTextField();

  /*
   * Test Constructor
   * Raktim 9/15/06
   */
  public DisplaySettingsDialog () throws HeadlessException {
	super();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public DisplaySettingsDialog(/*DisplayStateManager*/ ChARM dispState) throws HeadlessException {
    super();
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
    jPanel1.setLayout(boxLayout21);

    boxLayout21.setAxis(BoxLayout.Y_AXIS);
    jButton2.setText("Close");
    jButton2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitStatus = DisplaySettingsDialog.EXIT_STATUS_CANCEL;
        closeDialog();
      }
    }
    );

    jButton3.setText("Apply");
    jButton3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GraphViewPanel graphPanel = displayStateManager.getGraphPanel();
        //graphPanel.updateAxisScale(Math.abs(1.0/Double.parseDouble(jTextField1.getText())));
        graphPanel.updateAxisScale(Math.abs(1.0f/(float)Double.parseDouble(jTextField1.getText())));
        currAxisScale = Math.abs(1.0/Double.parseDouble(jTextField1.getText()));
        //graphPanel.updateAxisScale(Math.pow(10.0,(currAxisScale-50.0)/50.0));
        graphPanel.setBackgroundColor(backgroundColor);
      }
    });

    jPanel2.setLayout(flowLayout1);
    this.setResizable(false);
    jCheckBox1.setText("Black");
    jCheckBox1.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
          if (jCheckBox2.isSelected()) {
            jCheckBox2.setSelected(false);
            backgroundColor = Color.BLACK;
          }
     }
    });

    jCheckBox2.setText("White");
    jCheckBox2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (jCheckBox1.isSelected()) {
          jCheckBox1.setSelected(false);
          backgroundColor = Color.WHITE;
        }
      }
    });



    jPanel4.setLayout(gridBagLayout1);
    jLabel2.setText("Background Color");
    jLabel3.setText("Max gene value (absolute):  ");
    jLabel4.setText("Y-axis scaling:");
    jTextField1.setMinimumSize(new Dimension(50, 20));
    jTextField1.setPreferredSize(new Dimension(50, 20));
    jTextField1.setText("");
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel2.add(jLabel3, null);
    jPanel2.add(jTextField1, null);
    jPanel4.add(jLabel4,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(jPanel4, null);
    jPanel4.add(jCheckBox1,          new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 81, 0, 14), 99, 0));
    jPanel4.add(jCheckBox2,                  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 98, 0));
    jPanel4.add(jLabel2,          new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 100), 84, 0));
    jPanel4.add(jPanel2,    new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 14));
    jPanel1.add(jPanel3, null);
    jPanel3.add(jButton3, null);
    jPanel3.add(jButton2, null);


    GraphViewPanel graphPanel = displayStateManager.getGraphPanel();
    if(graphPanel.getBackgroundColor().equals(Color.BLACK)) {
      jCheckBox1.setSelected(true);
      backgroundColor = Color.BLACK;
    }
    else {
      jCheckBox2.setSelected(true);
      backgroundColor = Color.WHITE;
    }

    double currScale = graphPanel.getAxisScale();
    this.currAxisScale = 1.0/graphPanel.getAxisScale();
    jTextField1.setText(1.0/graphPanel.getAxisScale()+"");
  }

  /**
   * Returns user-selected y-axis scale.
   * @return double
   */
  public double getAxisScale() {
   return this.currAxisScale;
 }

 /**
  * Returns user-selected background color option.
  * @return Color
  */
 public Color getBackgroundColor() {
   return this.backgroundColor;
 }

 /**
  * Closes this dialog.
  */
 private void closeDialog() {
    this.hide();
  }

  /**
   * Returns exit status
   * @return int- options: EXIT_STATUS_CANCEL, EXIT_STATUS_OK
   */
  public int getExitStatus() {
    return exitStatus;
  }

  public static void main(String args[]) {
	  /*
	  DisplaySettingsDialog testDisp = new DisplaySettingsDialog();
	  testDisp.setModal(true);
	  testDisp.pack();
	  testDisp.setVisible(true);
	  */
  }
}
