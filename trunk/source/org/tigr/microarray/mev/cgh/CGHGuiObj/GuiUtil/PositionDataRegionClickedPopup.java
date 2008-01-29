/*
 * PositionDataRegionClickedPopup.java
 *
 * Created on December 25, 2002, 11:43 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class PositionDataRegionClickedPopup extends JPopupMenu {

    /** Creates a new instance of PositionDataRegionClickedPopup */
    public PositionDataRegionClickedPopup(ActionListener listener) {

        MenuUtil util = new MenuUtil();

        add(util.createJMenuItem("Show Genes in Region", "Show Genes in Region", listener));
        add(util.createJMenuItem("Show Browser", "Show Browser", listener));

        addSeparator();

        add(util.createJMenuItem("Display Data Values", "Display Data Values", listener));

        addSeparator();

        add(util.createJMenuItem("Launch Ensembl", "Launch Ensembl", listener));
        add(util.createJMenuItem("Launch Golden Path", "Launch Golden Path", listener));
        add(util.createJMenuItem("Launch NCBI Viewer", "Launch NCBI Viewer", listener));
    }

}
