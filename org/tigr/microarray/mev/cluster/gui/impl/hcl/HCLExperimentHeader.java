/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HCLExperimentHeader.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-05 20:25:10 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.MouseListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.helpers.IExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;

public class HCLExperimentHeader extends JPanel implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    // wrapped experiment header.
    private JComponent expHeader;
    
    public HCLExperimentHeader(){ }
    
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
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.expHeader);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.expHeader = (ExperimentHeader)ois.readObject();
        
        if(this.expHeader == null)
            System.out.println("NULL HEADER");
    }
}
