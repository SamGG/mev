package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.CharmGUI;

import com.borland.jbcl.layout.BoxLayout2;
import com.borland.jbcl.layout.VerticalFlowLayout;
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class MainFrame_AboutBox extends JDialog implements ActionListener {

  JPanel panel1 = new JPanel();
  JButton button1 = new JButton();
  JLabel label3 = new JLabel();
  ImageIcon image1 = new ImageIcon();
  String product = "";
  String version = "1.1";
  String copyright = "Copyright (c) 2004";
  String comments = "";
  JLabel label4 = new JLabel();
  JLabel label5 = new JLabel();
  JLabel label6 = new JLabel();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
  BoxLayout2 boxLayout21 = new BoxLayout2();
  JPanel jPanel3 = new JPanel();
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();

  /**
   * Class constructor.
   * @param parent Frame
   */
  public MainFrame_AboutBox(Frame parent) {
    super(parent);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  MainFrame_AboutBox() {
    this(null);
  }

  //Component initialization
  /**
   * Dialog initialization.
   * @throws Exception
   */
  private void jbInit() throws Exception  {
    image1 = new ImageIcon(CharmGUI.class.getResource("about.png"));
    this.setTitle("About");
    this.getContentPane().setLayout(flowLayout1);
    panel1.setLayout(verticalFlowLayout1);
    label3.setText(copyright);
    button1.setText("Ok");
    button1.addActionListener(this);
    label4.setText("CHromosomal Aberration Region Miner");
    label5.setText("version "+version);
    label6.setFont(new java.awt.Font("Arial Black", 0, 14));
    label6.setForeground(Color.red);
    label6.setAlignmentX((float) 0.0);
    label6.setToolTipText("");
    label6.setText("ChARM");
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Email bug reports, comments, and questions to:");
    jLabel2.setAlignmentX((float) 0.0);
    jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel2.setText("          charm@genomics.princeton.edu");
    jPanel1.setLayout(verticalFlowLayout2);
    jPanel2.setMaximumSize(new Dimension(32767, 32767));
    jPanel2.setLayout(boxLayout21);
    boxLayout21.setAxis(BoxLayout.Y_AXIS);
    jPanel1.add(label4, null);
    jPanel1.add(label5, null);
    jPanel1.add(label3, null);
    jPanel2.add(jLabel1, null);
    jPanel2.add(jLabel2, null);
    this.getContentPane().add(panel1, null);
    panel1.add(jPanel4, null);
    jPanel4.add(label6, null);
    panel1.add(jPanel3, null);
    jPanel3.add(jPanel1, null);
    panel1.add(jPanel2, null);
    panel1.add(jPanel5, null);
    jPanel5.add(button1, null);
    setResizable(false);
  }

  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }

  //Close the dialog
  void cancel() {
    dispose();
  }

  //Close the dialog on a button event
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == button1) {
      cancel();
    }
  }
}
