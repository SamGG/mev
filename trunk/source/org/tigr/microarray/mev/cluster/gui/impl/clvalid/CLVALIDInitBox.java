/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: .java,v $
 * $Revision: 1.10 $
 * $Date: 2011-9-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.clvalid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterValidationGenerator;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class CLVALIDInitBox extends AlgorithmDialog {
	private static final long serialVersionUID = 1L;
	private boolean okPressed = false;
	private ClusterValidationGenerator validationPanel;
    public CLVALIDInitBox(Frame parent, ClusterRepository repository, String bioCAnnotation) {
    	super(parent, "CLVALID Initialization", true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/4);
        Listener listener = new Listener();
        addWindowListener(listener);  
        validationPanel = new ClusterValidationGenerator(this, "Validation", repository, bioCAnnotation, true);
        JPanel jp = new JPanel(new GridBagLayout());
        JPanel dummyPanel = new JPanel();//this is needed for resizing for some reason.  Panel won't properly resize without.
        dummyPanel.setBackground(Color.white);
        jp.add(dummyPanel, new GridBagConstraints(0,0,1,1,.1,.1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        jp.add(validationPanel.getClusterValidationPanel(), new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    	addContent(jp);
        setActionListeners(listener);
        this.pack();
	}
    public class Listener extends DialogListener implements ItemListener{
        

		public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!validationPanel.validateParameters())
                    return;
                okPressed = true;
            	dispose();
            } else if (command.equals("reset-command")) {
               
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
            	HelpWindow.launchBrowser(CLVALIDInitBox.this, "Cluster Validation- Initialization Dialog");
            }
        }

		public void itemStateChanged(ItemEvent e) {
			
		}
    }
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        dummyFrame.setSize(300,600);
        CLVALIDInitBox oBox = new CLVALIDInitBox(dummyFrame, null, "hgu133a");
        oBox.setVisible(true);
        System.exit(0);
    }

	public boolean isOkPressed() {
		return okPressed;
	}
	public ClusterValidationGenerator getValidationGenerator(){
		return this.validationPanel;
	}
}
