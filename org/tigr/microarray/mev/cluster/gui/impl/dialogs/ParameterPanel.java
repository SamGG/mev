/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ParameterPanel.java
 *
 * Created on March 5, 2003, 8:29 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ParameterPanel extends JPanel {

    /** Creates new ParameterPanel */
    public ParameterPanel() {
        super();
        this.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));        
    }

}
