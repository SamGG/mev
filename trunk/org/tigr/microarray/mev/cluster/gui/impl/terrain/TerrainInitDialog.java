/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TerrainInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:33:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class TerrainInitDialog extends AlgorithmDialog {
    private int result;

    private JRadioButton genRadio;
    private JRadioButton expRadio;
    private JTextField   neighboursField;

  //  private static final String text1 = "Cluster Analysis Software Package ";
 //   private static final String text2 = "The Institute for Genomic Research ";

    /**
     * Constructs a <code>TerrainInitDialog</code> with default
     * initial parameters.
     */
    public TerrainInitDialog(Frame parent) {
        super((JFrame)parent, "Terrain Initialization", true);

        Listener listener = new Listener();
        addWindowListener(listener);

        JPanel parameters = new JPanel(new GridBagLayout());
        parameters.setBorder(new EmptyBorder(20, 10, 20, 10));
        parameters.setBackground(Color.white);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        genRadio = new JRadioButton("Genes");
        genRadio.setFocusPainted(false);
        genRadio.setBackground(Color.white);
        genRadio.setForeground(UIManager.getColor("Label.foreground"));
        gbc.gridx = 0; gbc.gridy = 0;
        parameters.add(genRadio, gbc);

        expRadio = new JRadioButton("Samples");
        expRadio.setForeground(UIManager.getColor("Label.foreground"));
        expRadio.setFocusPainted(false);
        expRadio.setBackground(Color.white);
        gbc.gridx = 1; gbc.gridy = 0; gbc.insets.left = 25;
        parameters.add(expRadio, gbc);
        gbc.insets.left = 0;
        gbc.gridx = 0; gbc.gridy = 1; gbc.insets.top = 20;
        parameters.add(new JLabel("Neighbors:"), gbc);
        gbc.gridx = 1; gbc.insets.left = 25; 
        neighboursField = new JTextField(String.valueOf(20), 5);
        parameters.add(neighboursField, gbc);

        ButtonGroup bg = new ButtonGroup();
        bg.add(genRadio);
        bg.add(expRadio);
        genRadio.setSelected(true);

    //    JButton button1 = new JButton("OK");
    //    button1.setActionCommand("ok-command");
     //   button1.addActionListener(listener);
     //   button1.setFocusPainted(false);

     //   JButton button2 = new JButton("Cancel");
    //    button2.setActionCommand("cancel-command");
    //    button2.addActionListener(listener);
    //    button2.setFocusPainted(false);

      //  JPanel insetsPanel = new JPanel(new GridLayout(2, 1));
      //  insetsPanel.add(new JLabel(text1));
      //  insetsPanel.add(new JLabel(text2));

    //    JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
   //     buttons.add(button1);
    //    buttons.add(button2);

      //  JPanel panel2 = new JPanel(new BorderLayout());
     //   panel2.setBorder(new EmptyBorder(10, 0, 0, 0));
     //   panel2.add(insetsPanel, BorderLayout.WEST);
    //    panel2.add(buttons, BorderLayout.EAST);

        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.setForeground(Color.white);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
       // panel3.add(new JLabel(GUIFactory.getIcon("genebar.gif")), BorderLayout.NORTH);
        panel3.setBackground(Color.white);
        panel3.add(parameters, BorderLayout.WEST);
        panel3.add(new JLabel(GUIFactory.getIcon("tigr_logo.gif")), BorderLayout.EAST);

      //  JPanel panel1 = new JPanel(new BorderLayout());
      //  panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
   //     panel1.add(panel2, BorderLayout.SOUTH);
     //   panel1.add(panel3, BorderLayout.NORTH);
        
        setActionListeners(listener);
        addContent(panel3);
     //   getContentPane().add(panel1, BorderLayout.CENTER);
        pack();
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
     * Returns true if genes radio button was selected.
     */
    public boolean isGenes() {
        return genRadio.isSelected();
    }

    /**
     * Returns neighbours value.
     */
    public int getNeighbours() {
        return Integer.parseInt(neighboursField.getText());
    }

    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    int value = Integer.parseInt(neighboursField.getText());
                    if (value < 1)
                        throw new NumberFormatException("value must be more than 0.");
                    result = JOptionPane.OK_OPTION;  
                    dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(TerrainInitDialog.this, "Error number: " + nfe.getMessage(), "Input Error!", JOptionPane.ERROR_MESSAGE);
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                genRadio.setSelected(true);
                neighboursField.setText("20");
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(TerrainInitDialog.this, "Terrain Map Initialization Dialog");
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

    public static void main(String[] args) {
        TerrainInitDialog dlg = new TerrainInitDialog(new javax.swing.JFrame());
        if (dlg.showModal() == JOptionPane.OK_OPTION) {
            System.out.println("ok");
        }
        System.exit(0);
    }
}