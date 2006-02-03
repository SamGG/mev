/*
 * MenubarUtility.java
 *
 * Created on July 5, 2003, 3:56 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class MenubarUtility {

    /** Creates a new instance of MenubarUtility */
    public MenubarUtility() {
    }

    /**
     * Creates a menu item from specified action.
     */
    public static JMenuItem createJMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setActionCommand((String)action.getValue(Action.ACTION_COMMAND_KEY));
        return item;
    }

    /**
     * Creates a menu item with specified name and acton command.
     */
    public static JMenuItem createJMenuItem(String name, String command, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        return item;
    }

    /**
     * Creates a check box menu item with specified name, acton command and state.
     */
    public static JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener, boolean isSelected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        return item;
    }

    /**
     * Creates a check box menu item with specified name and acton command.
     */
    public static JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener) {
        return createJCheckBoxMenuItem(name, command, listener, false);
    }

    /**
     * Creates a radio button menu item with specified name, acton command and state.
     */
    public static JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup, boolean isSelected) {
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
     * Creates a radio button menu item with specified name, acton command, state and enabled/disabled.
     */
    public static JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        item.setEnabled(isEnabled);
        if (buttonGroup != null) {
            buttonGroup.add(item);
        }
        return item;
    }

    /**
     * Creates a radio button menu item with specified name, acton command and button group.
     */
    public static JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup) {
        return createJRadioButtonMenuItem(name, command, listener, buttonGroup, false);
    }

}
