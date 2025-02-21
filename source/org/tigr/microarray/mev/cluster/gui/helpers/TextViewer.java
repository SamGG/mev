/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TextViewer.java
 *
 * Created on October 8, 2004, 12:56 PM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
/**
 *
 * @author  braisted
 */
public class TextViewer extends ViewerAdapter implements Serializable {
    
    private JPanel content;
    private JTextPane pane;
    private String text;
    
    
    public String getText() {return text;}

    public static String[] getPersistenceDelegateArgs() {
    	return new String[]{"text"};
    }
    public TextViewer(JPanel content, Object header){
    	this.content = content;
    }
    public void setExperiment(Experiment e){}
    
    /** Creates a new instance of TextViewer */
    public TextViewer(String message) {
        text = message;
        content = new JPanel(new GridBagLayout());
        content.setBackground(Color.white);
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setMargin(new Insets(5,5,5,5));        
        pane.setText(message);
        content.add(pane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
    }
    
    public JComponent getContentComponent() {
        return content;
    }
    
}
