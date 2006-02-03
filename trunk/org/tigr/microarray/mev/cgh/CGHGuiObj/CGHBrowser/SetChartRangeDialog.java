/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetChartRangeDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-03 14:35:43 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHBrowser;

import org.tigr.util.awt.GBA;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author Adam Margolin
 * @author Raktim Sinha
 *
 */
public class SetChartRangeDialog extends JDialog {
    private int result;
    private Dimension size;
    private JLabel minLabel, maxLabel;
    float min, max;
    private JTextField minTextField, maxTextField;
    private GBA gba;

    public SetChartRangeDialog(JFrame parent, float origMin, float origMax) {
        super(parent, true);

	setTitle("Set Display Range");
	gba = new GBA();
	minLabel = new JLabel("Min: ");
	minTextField = new JTextField(10);
	minTextField.setText("" + origMin);
	maxLabel = new JLabel("Max: ");
	maxTextField = new JTextField(10);
	maxTextField.setText("" + origMax);
	JButton okButton = new JButton("Okay");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(new Listener());
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(new Listener());

	Container content = getContentPane();
	content.setLayout(new GridBagLayout());
	gba.add(content, minLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, minTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, maxLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, maxTextField, 1, 1, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);

	setResizable(false);
	minTextField.grabFocus();
	getRootPane().setDefaultButton(okButton);
	pack();
    }

    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }

    /** Getter for property min.
     * @return Value of property min.
     */
    public float getMin() {
        return min;
    }

    /** Setter for property min.
     * @param min New value of property min.
     */
    public void setMin(float min) {
        this.min = min;
    }

    /** Getter for property max.
     * @return Value of property max.
     */
    public float getMax() {
        return max;
    }

    /** Setter for property max.
     * @param max New value of property max.
     */
    public void setMax(float max) {
        this.max = max;
    }

    private class Listener extends WindowAdapter implements ActionListener {

	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		String minText = minTextField.getText();
		String maxText = maxTextField.getText();
		try {
		    min = Float.parseFloat(minText);
                    max = Float.parseFloat(maxText);
		    result = JOptionPane.OK_OPTION;
		} catch (Exception e) {
		    result = JOptionPane.CANCEL_OPTION;
		}
		dispose();
	    } else if (command.equals("cancel-command")) {
		result = JOptionPane.CANCEL_OPTION;
		dispose();
	    }
	}

	public void windowClosing(WindowEvent e) {
	    result = JOptionPane.CLOSED_OPTION;
	    dispose();
	}
    }

}