/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ExperimentsComparatorInitDlg.java
 *
 * Created on November 22, 2003, 4:19 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.AlterationsComparator;


import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
