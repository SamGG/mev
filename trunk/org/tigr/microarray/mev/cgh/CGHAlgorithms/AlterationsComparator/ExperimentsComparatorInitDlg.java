/*
 * ExperimentsComparatorInitDlg.java
 *
 * Created on November 22, 2003, 4:19 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.AlterationsComparator;


import java.awt.Container;
import java.awt.BorderLayout;
/*
import java.io.File;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
*/
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.GroupExperimentsPanel;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
public class ExperimentsComparatorInitDlg extends org.tigr.microarray.util.swing.ModalDialog {
    GroupExperimentsPanel gPanel;

    /** Creates a new instance of ExperimentsComparatorInitDlg */
    public ExperimentsComparatorInitDlg(java.awt.Frame parent, ArrayList featuresList) {
        super(parent);
        setSize(700, 600);
        Container content = getContentPane();

        Vector exptNames = new Vector(featuresList.size());
        Iterator it = featuresList.iterator();
        while(it.hasNext()){
            //exptNames.add(it.next().toString());
        	exptNames.add(((ISlideData)it.next()).getSlideDataName());
        }

        this.gPanel = new GroupExperimentsPanel(exptNames);

        content.add(gPanel, BorderLayout.CENTER);
    }

    public GroupExperimentsPanel getGroupExperimentsPanel(){
        return gPanel;
    }

}
