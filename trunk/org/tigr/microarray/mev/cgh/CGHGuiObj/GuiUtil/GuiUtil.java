/*
 * UtilGui.java
 *
 * Created on January 19, 2003, 12:15 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.*;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GuiUtil {

    /** Creates a new instance of UtilGui */
    public static void center(Window w){
        Dimension us = w.getSize();
        Dimension them = Toolkit.getDefaultToolkit().getScreenSize();

        int newX = (them.width - us.width) / 2;
        int newY = (them.height - us.height) / 2;
        w.setLocation(newX, newY);
    }

}
