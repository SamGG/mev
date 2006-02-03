/*
 * AbstractMenubar.java
 *
 * Created on March 27, 2003, 2:22 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author  Adam Margolin
 */

public class MenuUtil {
    
    /** Creates a new instance of AbstractMenubar */
    public MenuUtil() {
    }
    
    /**
     * Creates a menu item with specified name and acton command.
     */
    protected JMenuItem createJMenuItem(String name, String command, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name, acton command and state.
     */
    protected JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener, boolean isSelected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name and acton command.
     */
    protected JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener) {
        return createJCheckBoxMenuItem(name, command, listener, false);
    }
    
    /**
     * Creates a radio button menu item with specified name, acton command and state.
     */
    protected JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup, boolean isSelected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        if (buttonGroup != null) {
            buttonGroup.add(item);
        }
        return item;
    }
    
    /**
     * Creates a radio button menu item with specified name, acton command and button group.
     */
    protected JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup) {
        return createJRadioButtonMenuItem(name, command, listener, buttonGroup, false);
    }
}
