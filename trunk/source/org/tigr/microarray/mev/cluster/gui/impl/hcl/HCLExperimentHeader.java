/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLExperimentHeader.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-04-10 18:41:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.helpers.IExperimentHeader;


public class HCLExperimentHeader extends JPanel {

    // wrapped experiment header.
    private JComponent expHeader;
    
    public HCLExperimentHeader(){ }
    public JComponent getHeader(){return expHeader;}
    public void setHeader(JComponent header){this.expHeader = header;}
    
    /**
     * Constructs a <code>HCLExperimentHeader</code> with wrapped header component.
     */
    public HCLExperimentHeader(JComponent expHeader) {
        setLayout(null);
        setBackground(Color.white);
        add(this.expHeader = expHeader);
    }
    
    /**
     * Sets the header position.
     */
    public void setHeaderPosition(int position) {
        this.expHeader.setLocation(position, 0);
    }
    
    /**
     * Updates the header sizes.
     */
    public void updateSize(int newWidth, int elementWidth) {
        ((IExperimentHeader)this.expHeader).updateSizes(newWidth, elementWidth);
        setSizes(newWidth, this.expHeader.getHeight());
    }
    
    private void setSizes(int width, int height) {
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
        
    /**
     * Adds mouse listener to itself and to wrapped component.
     */
    public void addMouseListener(MouseListener listener) {
        super.addMouseListener(listener);
        if(this.expHeader != null)
            this.expHeader.addMouseListener(listener);
    }
    
    /**
     * Removes mouse listener from itself and from wrapped component.
     */
    public void removeMouseListener(MouseListener listener) {
        super.removeMouseListener(listener);
        this.expHeader.removeMouseListener(listener);
    }
    
    public void setUseDoubleGradient(boolean useDouble) {
    	((IExperimentHeader)this.expHeader).setUseDoubleGradient(useDouble);
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.expHeader);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.expHeader = (JComponent)ois.readObject();
        
        if(this.expHeader == null)
            System.out.println("NULL HEADER");
    }
}
