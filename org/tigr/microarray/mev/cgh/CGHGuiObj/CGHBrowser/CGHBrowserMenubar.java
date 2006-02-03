/*
 * CGHBrowserMenubar.java
 *
 * Created on July 5, 2003, 10:12 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHBrowser;

/*
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
*/

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHBrowserModelAdaptor;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.MenubarUtility;

/**
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHBrowserMenubar {
    ActionListener listener;

    public CGHBrowserMenubar(ActionListener listener) {
        this.listener = listener;
    }

    public JMenu createCloneValuesMenu(int cloneValueType, boolean hasDyeSwap, boolean isRatioOnly, boolean log2RatioOnly, boolean cloneDist){
        if(hasDyeSwap){
            return createCloneValuesMenuDyeSwap(cloneValueType, cloneDist);
        }else{
            return createCloneValuesMenuNoDyeSwap(cloneValueType, isRatioOnly, log2RatioOnly, cloneDist);
        }
    }

    public JMenu createCloneValuesMenuDyeSwap(int cloneValueType, boolean cloneDist){
        ButtonGroup buttonGroup = new ButtonGroup();
        JMenu cloneValuesMenu = new JMenu("CloneValues");

        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("Dye Swap", CGHBrowserActionManager.CLONE_VALUES_DYE_SWAP, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP));
        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("Log Average Inverted", CGHBrowserActionManager.CLONE_VALUES_LOG_AVERAGE_INVERTED, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED));
        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("Log Dye Swap", CGHBrowserActionManager.CLONE_VALUES_LOG_DYE_SWAP, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP));
        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("P Values", CGHBrowserActionManager.CLONE_VALUES_P_VALUES, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE, cloneDist));

        return cloneValuesMenu;
    }

    public JMenu createCloneValuesMenuNoDyeSwap(int cloneValueType, boolean isRatioOnly, boolean log2RatioOnly, boolean cloneDist){
        ButtonGroup buttonGroup = new ButtonGroup();
        JMenu cloneValuesMenu = new JMenu("CloneValues");

        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("Ratios", CGHBrowserActionManager.CLONE_VALUES_RATIOS, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_RATIOS, isRatioOnly &! log2RatioOnly));
        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("Log Ratios", CGHBrowserActionManager.CLONE_VALUES_LOG_RATIOS, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_LOG_RATIOS, log2RatioOnly));
        cloneValuesMenu.add(MenubarUtility.createJRadioButtonMenuItem("P Value", CGHBrowserActionManager.CLONE_VALUES_P_VALUES, listener, buttonGroup, cloneValueType == CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE, cloneDist));

        return cloneValuesMenu;
    }

}
