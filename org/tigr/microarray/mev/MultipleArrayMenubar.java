/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: MultipleArrayMenubar.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-02-27 22:19:13 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.action.DefaultAction;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;

public class MultipleArrayMenubar extends JMenuBar {
    
    private DisplayMenu displayMenu = new DisplayMenu();
    private DistanceMenu distanceMenu = new DistanceMenu();
    private JMenu normalizationMenu;
    private JMenu labelMenu;
    private JMenu adjustMenu;
    private ButtonGroup labelGroup;
    private JMenu sortMenu;
    private ButtonGroup sortGroup;
    private ActionListener listener;
    private boolean affyNormAdded = false;

    
    private ActionManager actionManager;
    
    /**
     * Constructs a <code>MultipleArrayMenubar</code> using specified
     * action maneger.
     * @see ActionManager
     */
    public MultipleArrayMenubar(ActionManager manager) {
        listener = manager.getListener();
        actionManager = manager;
        JMenu fileMenu = new JMenu("File");
        // fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_DIRECTORY_ACTION)));
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_ANALYSIS_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_ANALYSIS_ACTION)));
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_ANALYSIS_AS_ACTION)));
        
        // fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_DB_ACTION)));
        //  fileMenu.getMenuComponent(2).setEnabled(false);
        //  fileMenu.addSeparator();
        //  fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_STANFORD_ACTION)));
        //    fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_CLUSTER_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_MATRIX_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_IMAGE_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.PRINT_IMAGE_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.CLOSE_ACTION)));
        add(fileMenu);
        
        adjustMenu = new JMenu("Adjust Data");
        adjustMenu.add(createJMenuItem("Log2 Transform", ActionManager.LOG2_TRANSFORM_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Normalize Spots", ActionManager.NORMALIZE_SPOTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Divide Spots by RMS", ActionManager.DIVIDE_SPOTS_RMS_CMD, listener));
        adjustMenu.add(createJMenuItem("Divide Spots by SD", ActionManager.DIVIDE_SPOTS_SD_CMD, listener));
        adjustMenu.add(createJMenuItem("Mean Center Spots", ActionManager.MEAN_CENTER_SPOTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Median Center Spots", ActionManager.MEDIAN_CENTER_SPOTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Digital Spots", ActionManager.DIGITAL_SPOTS_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Normalize Experiments", ActionManager.NORMALIZE_EXPERIMENTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Divide Experiments by RMS", ActionManager.DIVIDE_EXPERIMENTS_RMS_CMD, listener));
        adjustMenu.add(createJMenuItem("Divide Experiments by SD", ActionManager.DIVIDE_EXPERIMENTS_SD_CMD, listener));
        adjustMenu.add(createJMenuItem("Mean Center Experiments", ActionManager.MEAN_CENTER_EXPERIMENTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Median Center Experiments", ActionManager.MEDIAN_CENTER_EXPERIMENTS_CMD, listener));
        adjustMenu.add(createJMenuItem("Digital Experiments", ActionManager.DIGITAL_EXPERIMENTS_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Log10 to Log2", ActionManager.LOG10_TO_LOG2_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Set Lower Cutoffs", ActionManager.SET_LOWER_CUTOFFS_CMD, listener));
        adjustMenu.add(createJCheckBoxMenuItem("Use Lower Cutoffs", ActionManager.USE_LOWER_CUTOFFS_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Set Percentage Cutoffs", ActionManager.SET_PERCENTAGE_CUTOFFS_CMD, listener));
        adjustMenu.add(createJCheckBoxMenuItem("Use Percentage Cutoffs", ActionManager.USE_PERCENTAGE_CUTOFFS_CMD, listener));
        adjustMenu.addSeparator();
        adjustMenu.add(createJCheckBoxMenuItem("Adjust Intensities of '0'", ActionManager.ADJUST_INTENSITIES_0_CMD, listener, true));
        
        // pcahan
        /*
        if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
            adjustMenu.addSeparator();
            adjustMenu.add(createJMenuItem("Set Detection Filter", ActionManager.SET_DETECTION_FILTER_CMD, listener));
            adjustMenu.add(createJCheckBoxMenuItem("Use Detection Filter", ActionManager.USE_DETECTION_FILTER_CMD, listener));
            
            adjustMenu.add(createJMenuItem("Set Fold Filter", ActionManager.SET_FOLD_FILTER_CMD, listener));
            adjustMenu.add(createJCheckBoxMenuItem("Use Fold Filter", ActionManager.USE_FOLD_FILTER_CMD, listener));
        }
        */
        
        add(adjustMenu);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        normalizationMenu = new JMenu("Normalization");
        normalizationMenu.add(createJRadioButtonMenuItem("Total Intensity", ActionManager.TOTAL_INTENSITY_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Linear Regression", ActionManager.LINEAR_REGRESSION_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Ratio Statistics", ActionManager.RATIO_STATISTICS_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Iterative Log", ActionManager.ITERATIVE_LOG_CMD, listener, buttonGroup));
        normalizationMenu.addSeparator();
        normalizationMenu.add(createJRadioButtonMenuItem("No Normalization", ActionManager.NO_NORMALIZATION_CMD, listener, buttonGroup, true));
        add(normalizationMenu);
        
        buttonGroup = new ButtonGroup();
        JMenu distanceMenu = new JMenu("Distance");
        distanceMenu.add(createJRadioButtonMenuItem("Default Distance", ActionManager.DEFAULT_DISTANCE_CMD, listener, buttonGroup, true));
        distanceMenu.addSeparator();
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Correlation", ActionManager.PEARSON_CORRELATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Uncentered", ActionManager.PEARSON_UNCENTERED_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Squared", ActionManager.PEARSON_SQUARED_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Cosine Correlation", ActionManager.COSINE_CORRELATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Covariance Value", ActionManager.COVARIANCE_VALUE_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Euclidean Distance", ActionManager.EUCLIDEAN_DISTANCE_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Average Dot Product", ActionManager.AVERAGE_DOT_PRODUCT_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Manhattan Distance", ActionManager.MANHATTAN_DISTANCE_CMD, listener, buttonGroup));
        distanceMenu.addSeparator();
        distanceMenu.add(createJRadioButtonMenuItem("Mutual Information", ActionManager.MUTUAL_INFORMATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Spearman Rank Correlation", ActionManager.SPEARMAN_RANK_CORRELATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Kendall's Tau", ActionManager.KENDALLS_TAU_CMD, listener, buttonGroup));
        distanceMenu.addSeparator();
        distanceMenu.add(createJCheckBoxMenuItem("Absolute distance", ActionManager.ABSOLUTE_DISTANCE_CMD, listener, false));
        add(distanceMenu);
        
        JMenu analysisMenu = new JMenu("Analysis");
        // add analysis menu here
        addAnalysisMenu(analysisMenu, manager);
        analysisMenu.addSeparator();
        analysisMenu.add(manager.getAction(ActionManager.DELETE_ALL_ACTION));
        analysisMenu.addSeparator();
        analysisMenu.add(manager.getAction(ActionManager.DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION));
        analysisMenu.addSeparator();
        analysisMenu.add(manager.getAction(ActionManager.SHOW_THUMBNAIL_ACTION));
        add(analysisMenu);
        
        
        JMenu displayMenu = new JMenu("Display");
        
        buttonGroup = new ButtonGroup();
        JMenu colorSchemeMenu = new JMenu("Color Scheme");
        colorSchemeMenu.add(createJRadioButtonMenuItem("Green/Red Scheme", ActionManager.GREEN_RED_COLOR_SCHEME_CMD, listener, buttonGroup, true));
        colorSchemeMenu.add(createJRadioButtonMenuItem("Blue/Yellow Scheme", ActionManager.BLUE_YELLOW_COLOR_SCHEME_CMD, listener, buttonGroup));
        colorSchemeMenu.add(createJRadioButtonMenuItem("Custom Color Scheme", ActionManager.CUSTOM_COLOR_SCHEME_CMD, listener, buttonGroup));
        colorSchemeMenu.add(createJCheckBoxMenuItem("Use Color Gradient on Graphs", ActionManager.COLOR_GRADIENT_CMD, listener));
        displayMenu.add(colorSchemeMenu);
        displayMenu.addSeparator();
        
        buttonGroup = new ButtonGroup();
        displayMenu.add(createJRadioButtonMenuItem("Expression Bar View", ActionManager.DISPLAY_GREEN_RED_CMD, listener, buttonGroup));
        displayMenu.add(createJRadioButtonMenuItem("Ratio Split View", ActionManager.DISPLAY_GR_RATIO_SPLIT_CMD, listener, buttonGroup, true));
        displayMenu.add(createJRadioButtonMenuItem("Color Overlay View", ActionManager.DISPLAY_GR_OVERLAY_CMD, listener, buttonGroup));
        
        displayMenu.addSeparator();
        
        displayMenu.add(createJMenuItem("Abbr. Experiment Names", ActionManager.TOGGLE_ABBR_EXPT_NAMES_CMD, listener));
        
        displayMenu.addSeparator();
        
        labelMenu = new JMenu("Label");
        labelGroup = new ButtonGroup();
        //addLabelMenuItems(labelMenu, manager, labelGroup);
        displayMenu.add(labelMenu);
        displayMenu.addSeparator();
        displayMenu.add(createJCheckBoxMenuItem("G/R Scale", ActionManager.DISPLAY_GR_SCALE_CMD, listener, true));
        displayMenu.add(createJCheckBoxMenuItem("Draw Borders", ActionManager.DISPLAY_DRAW_BORDERS_CMD, listener, false));
        displayMenu.add(createJCheckBoxMenuItem("Tracing", ActionManager.DISPLAY_TRACING_CMD, listener));
        displayMenu.add(createJCheckBoxMenuItem("Use Anti-Aliasing", ActionManager.DISPLAY_USE_ANTIALIASING_CMD, listener, true));
        displayMenu.addSeparator();
        displayMenu.add(createJMenuItem("Set Upper Limits", ActionManager.DISPLAY_SET_UPPER_LIMITS_CMD, listener));
        displayMenu.add(createJMenuItem("Set Ratio Scale", ActionManager.DISPLAY_SET_RATIO_SCALE_CMD, listener));
        JMenu sizeMenu = new JMenu("Element Size");
        buttonGroup = new ButtonGroup();
        sizeMenu.add(createJRadioButtonMenuItem("5 x 2", ActionManager.DISPLAY_5X2_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("10 x 10", ActionManager.DISPLAY_10X10_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("20 x 5", ActionManager.DISPLAY_20X5_CMD, listener, buttonGroup, true));
        sizeMenu.add(createJRadioButtonMenuItem("50 x 10", ActionManager.DISPLAY_50X10_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("Other", ActionManager.DISPLAY_OTHER_CMD, listener, buttonGroup));
        displayMenu.add(sizeMenu);
        add(displayMenu);
        
        sortGroup = new ButtonGroup();
        sortMenu = new JMenu("Sort");
        sortMenu.add(createJRadioButtonMenuItem("Sort by Location", ActionManager.SORT_BY_LOCATION_CMD, listener, sortGroup, true));
        sortMenu.add(createJRadioButtonMenuItem("Sort by Ratio", ActionManager.SORT_BY_RATIO_CMD, listener, sortGroup));
        ((JRadioButtonMenuItem)sortMenu.getMenuComponent(0)).setSelected(true);
        // addSortMenuItems(sortMenu, manager, sortGroup);
        add(sortMenu);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createJMenuItem("Default Distances", ActionManager.DEFAULT_DISTANCES_CMD, listener));
        helpMenu.add(createJMenuItem("Support Tree Legend", ActionManager.SHOW_SUPPORTTREE_LEGEND_COMMAND, listener));
        add(helpMenu);
    }
    
    /**
     * Enables some menu items according to specified state.
     */
    public void systemEnable(int state) {
        switch (state) {
            case TMEV.SYSTEM:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.LOAD_STANFORD_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, false);
                break;
            case TMEV.DATA_AVAILABLE:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_MATRIX_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_IMAGE_COMMAND, true);
                setEnableMenuItem("File", ActionManager.PRINT_IMAGE_COMMAND, true);
                setEnableMenu("Adjust Data", true);
                setEnableMenu("Normalization", true);
                setEnableMenu("Distance", true);
                setEnableMenu("Analysis", true);
                setEnableMenu("Display", true);
                setEnableMenu("Sort", true);
                break;
            case TMEV.ANALYSIS_LOADED:
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, true);
            case TMEV.DB_AVAILABLE:
                break;
            case TMEV.DB_LOGIN:
                setEnableMenuItem("File", ActionManager.LOAD_DB_COMMAND, true);
                break;
        }
    }
    
    /**
     * Disables some menu items according to specified state.
     */
    public void systemDisable(int state) {
        switch (state) {
            case TMEV.SYSTEM:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.LOAD_DB_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_FILE_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, false);
                
                setEnableMenuItem("File", ActionManager.LOAD_DIRECTORY_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_STANFORD_COMMAND, false);
                break;
            case TMEV.DATA_AVAILABLE:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, false);
                
                setEnableMenuItem("File", ActionManager.SAVE_MATRIX_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_IMAGE_COMMAND, false);
                setEnableMenuItem("File", ActionManager.PRINT_IMAGE_COMMAND, false);
                setEnableMenu("Adjust Data", false);
                setEnableMenu("Normalization", false);
                setEnableMenu("Distance", false);
                break;
            case TMEV.DB_AVAILABLE:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.LOAD_DB_COMMAND, false);
                break;
            case TMEV.DB_LOGIN:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.LOAD_DB_COMMAND, false);
                break;
        }
        setEnableMenu("Analysis", false);
        setEnableMenu("Display", false);
        setEnableMenu("Sort", false);
    }
    
    /**
     * Adds label menu items.
     */
    private void addLabelMenuItems(JMenu menu, ActionManager manager, ButtonGroup buttonGroup) {
        int index = -1;
        JRadioButtonMenuItem item;
        Action action;
        while ((action = manager.getAction(ActionManager.DISPLAY_LABEL_ACTION+String.valueOf(index)))!=null) {
            item = new JRadioButtonMenuItem(action);
            buttonGroup.add(item);
            if (index < 1) { // try to select next item if exists
                item.setSelected(true);
                String indexStr = (String)action.getValue(ActionManager.PARAMETER);
                displayMenu.labelIndex = Integer.parseInt(indexStr);
            }
            menu.add(item);
            index++;
        }
    }
    
    public void addLabelMenuItems(String [] fieldNames){
        JRadioButtonMenuItem item;
        ButtonGroup bg = new ButtonGroup();
        DefaultAction action;
        for(int i = 0; i < fieldNames.length; i++){
            action = new DefaultAction(actionManager, "Label by "+fieldNames[i], ActionManager.DISPLAY_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, String.valueOf(i));
            item = new JRadioButtonMenuItem(action);
            labelGroup.add(item);
            if(i == 0)
                item.setSelected(true);
            this.labelMenu.add(item);
        }
    }
    
    
    public void replaceLabelMenuItems(String [] fieldNames){
        //remove all menu items
        this.labelMenu.removeAll();
        
        JRadioButtonMenuItem item;
        ButtonGroup bg = new ButtonGroup();
        DefaultAction action;
        for(int i = 0; i < fieldNames.length; i++){
            action = new DefaultAction(actionManager, "Label by "+fieldNames[i], ActionManager.DISPLAY_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, String.valueOf(i));
            item = new JRadioButtonMenuItem(action);
            labelGroup.add(item);
            if(i == 0)
                item.setSelected(true);
            this.labelMenu.add(item);
        }
    }
    
    
    public void addAffyFilterMenuItems(){
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Set Detection Filter", ActionManager.SET_DETECTION_FILTER_CMD, listener));
        adjustMenu.add(createJCheckBoxMenuItem("Use Detection Filter", ActionManager.USE_DETECTION_FILTER_CMD, listener));
        adjustMenu.add(createJMenuItem("Set Fold Filter", ActionManager.SET_FOLD_FILTER_CMD, listener));
        adjustMenu.add(createJCheckBoxMenuItem("Use Fold Filter", ActionManager.USE_FOLD_FILTER_CMD, listener));
    }
    
    
    public void addAffyNormMenuItems() {
        adjustMenu.addSeparator();
        adjustMenu.add(createJMenuItem("Divide Genes by Median", ActionManager.DIVIDE_GENES_MEDIAN_CMD, listener));
        adjustMenu.add(createJMenuItem("Divide Genes by Mean", ActionManager.DIVIDE_GENES_MEAN_CMD, listener));
        this.set_affyNormAddded(true);
    }
    
    /**
     * Adds sort menu items.
     */
    private void addSortMenuItems(JMenu menu, ActionManager manager, ButtonGroup buttonGroup) {
        int index = 0;
        JRadioButtonMenuItem item;
        Action action;
        while ((action = manager.getAction(ActionManager.SORT_LABEL_ACTION+String.valueOf(index)))!=null) {
            item = new JRadioButtonMenuItem(action);
            buttonGroup.add(item);
            menu.add(item);
            index++;
        }
    }
    
    public void addSortMenuItems(String [] fieldNames){
        JRadioButtonMenuItem item;
        DefaultAction action;
        for(int i = 0; i < fieldNames.length; i++){
            action = new DefaultAction(actionManager, "Sort by "+fieldNames[i], ActionManager.SORT_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, String.valueOf(i));
            item = new JRadioButtonMenuItem(action);
            sortGroup.add(item);
            this.sortMenu.add(item);
        }
    }
    
    public void replaceSortMenuItems(String [] fieldNames){
        //Remove all items
        this.sortMenu.removeAll();
        
        //Restore defaults
        sortMenu.add(createJRadioButtonMenuItem("Sort by Location", ActionManager.SORT_BY_LOCATION_CMD, listener, sortGroup, true));
        sortMenu.add(createJRadioButtonMenuItem("Sort by Ratio", ActionManager.SORT_BY_RATIO_CMD, listener, sortGroup));
        
        JRadioButtonMenuItem item;
        DefaultAction action;
        for(int i = 0; i < fieldNames.length; i++){
            action = new DefaultAction(actionManager, "Sort by "+fieldNames[i], ActionManager.SORT_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, String.valueOf(i));
            item = new JRadioButtonMenuItem(action);
            sortGroup.add(item);
            this.sortMenu.add(item);
        }
    }
    
    /**
     * Adds analysis menu items.
     */
    private void addAnalysisMenu(JMenu menu, ActionManager manager) {
        int index = 0;
        Action action;
        while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)))!=null) {
            menu.add(createJMenuItem(action));
            index++;
        }
    }
    
    /**
     * Returns an implementation of <code>IDisplayMenu</code> interface.
     */
    public IDisplayMenu getDisplayMenu() {
        return displayMenu;
    }
    
    /**
     * Returns an implementation of <code>IDistanceMenu</code> interface.
     */
    public IDistanceMenu getDistanceMenu() {
        return distanceMenu;
    }
    
    /**
     * Creates a menu item from specified action.
     */
    private JMenuItem createJMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setActionCommand((String)action.getValue(Action.ACTION_COMMAND_KEY));
        return item;
    }
    
    /**
     * Creates a menu item with specified name and acton command.
     */
    private JMenuItem createJMenuItem(String name, String command, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name, acton command and state.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener, boolean isSelected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name and acton command.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener) {
        return createJCheckBoxMenuItem(name, command, listener, false);
    }
    
    /**
     * Creates a radio button menu item with specified name, acton command and state.
     */
    private JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup, boolean isSelected) {
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
    private JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, ActionListener listener, ButtonGroup buttonGroup) {
        return createJRadioButtonMenuItem(name, command, listener, buttonGroup, false);
    }
    
    /**
     * Returns a menu by it name.
     */
    private JMenu getJMenu(String name) {
        JMenu jmenu;
        for (int i = 0; i < getMenuCount(); i++) {
            jmenu = getMenu(i);
            if (jmenu != null && jmenu.getText().equals(name))
                return jmenu;
        }
        return null;
    }
    
    /**
     * Returns a menu item from specified menu and item action command.
     */
    private JMenuItem getJMenuItem(String menu_name, String item_command) {
        JMenu menu = getJMenu(menu_name);
        if (menu == null) {
            return null;
        }
        JMenuItem item;
        for (int i=0; i<menu.getItemCount(); i++) {
            item = menu.getItem(i);
            if (item != null && item.getActionCommand().equals(item_command)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Sets a menu state with specified name.
     */
    private void setEnableMenu(String name, boolean enable) {
        JMenu menu = getJMenu(name);
        if (menu == null) {
            return;
        }
        menu.setEnabled(enable);
    }
    
    /**
     * Sets a menu item state with specified name.
     */
    private void setEnableMenuItem(String menu_name, String item_command, boolean enable) {
        JMenuItem item = getJMenuItem(menu_name, item_command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }
    
    // pcahan
    boolean get_affyNormAdded(){
        return this.affyNormAdded;
    }

    void set_affyNormAddded(boolean set){
        this.affyNormAdded = set;
    }
    
    /**
     * Sets current index of label menu.
     */
    void setLabelIndex(int index) {
        displayMenu.labelIndex = index;
    }
    
    /**
     * Sets element size attribute.
     */
    void setElementSize(int width, int height) {
        displayMenu.elementSize.setSize(width, height);
    }
    
    /**
     * Sets a palette style.
     */
    void setPaletteStyle(int style) {
        displayMenu.paletteStyle = style;
    }
    
    /**
     * Sets tracing attribute.
     */
    void setTracing(boolean tracing) {
        displayMenu.tracing = tracing;
    }
    
    /**
     * Sets draw borders attribute.
     */
    void setDrawBorders(boolean drawBorders) {
        displayMenu.drawBorders = drawBorders;
    }
    
    /**
     * Sets anti-aliasing attribute.
     */
    void setAntiAliasing(boolean antialiasing) {
        displayMenu.antialiasing = antialiasing;
    }
    
    /**
     * Sets GR scale attribute.
     */
    void setGRScale(boolean grscale) {
        displayMenu.grscale = grscale;
    }
    
    /**
     * Sets the absolute attribute value.
     */
    void setDistanceAbsolute(boolean absolute) {
        distanceMenu.absolute = absolute;
    }
    
    /**
     * Sets the distance function attribute.
     */
    void setDistanceFunction(int function) {
        distanceMenu.function = function;
    }
    
    /**
     * Sets min ratio scale value.
     */
    void setMinRatioScale(float scale) {
        displayMenu.minRatioScale = scale;
    }
    
    /**
     * Sets max ratio scale value.
     */
    void setMaxRatioScale(float scale) {
        displayMenu.maxRatioScale = scale;
    }
    
    /**
     * Sets max CY3 scale value.
     */
    void setMaxCY3Scale(float scale) {
        displayMenu.maxCY3Scale = scale;
    }
    
    /**
     * Sets max CY5 scale value.
     */
    void setMaxCY5Scale(float scale) {
        displayMenu.maxCY5Scale = scale;
    }
    
    /**
     * Sets positive color image
     */
    void setPositiveCustomGradient(BufferedImage image){
        displayMenu.posCustomColorImage = image;
    }
    
    /**
     * Sets negative color image
     */
    void setNegativeCustomGradient(BufferedImage image){
        displayMenu.negCustomColorImage = image;
    }
    
    /**
     * Sets color scheme index
     */
    void setColorSchemeIndex(int index){
        displayMenu.colorScheme = index;
    }
    
    int getColorScheme(){
        return displayMenu.colorScheme;
    }
    
    
    /**
     * Return current positive gradient image
     */
    public BufferedImage getPositiveGradientImage() {
        return displayMenu.getPositiveGradientImage();
    }
    
    
    /**
     * Return current negative gradient image
     */
    public BufferedImage getNegativeGradientImage() {
        return displayMenu.getNegativeGradientImage();
    }
    
    public boolean getColorGradientState(){
        return this.displayMenu.getColorGradientState();
    }
    
    public void setColorGradientState(boolean state){
        this.displayMenu.setColorGradientState(state);
    }
    
    public void setNormalizedButtonState(int index){
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)(this.normalizationMenu.getMenuComponent(index));
        button.setSelected(true);
    }
    
    /**
     * The class to present a state of the display menu.
     */
    private class DisplayMenu implements IDisplayMenu {
        
        private final Dimension elementSize = new Dimension(20, 5);
        private int labelIndex;
        private int paletteStyle = IDisplayMenu.RATIOSPLIT;
        private boolean tracing = false;
        private boolean drawBorders = false;
        private boolean antialiasing = true;
        private boolean grscale = true;
        private boolean useColorGradient = false;
        private float minRatioScale = -3f;
        private float maxRatioScale = 3f;
        private float maxCY3Scale = 0f;
        private float maxCY5Scale = 0f;
        
        private int colorScheme = IDisplayMenu.GREEN_RED_SCHEME;
        private BufferedImage negGreenColorImage = createGradientImage(Color.green, Color.black);
        private BufferedImage posRedColorImage = createGradientImage(Color.black, Color.red);
        private BufferedImage negBlueColorImage = createGradientImage(Color.blue, Color.black);
        private BufferedImage posYellowColorImage = createGradientImage(Color.black, Color.yellow);
        private BufferedImage negCustomColorImage;
        private BufferedImage posCustomColorImage;
        
        public int getPaletteStyle() {
            return paletteStyle;
        }
        
        public boolean isGRScale() {
            return grscale;
        }
        
        public Dimension getElementSize() {
            return elementSize;
        }
        
        public boolean isDrawingBorder() {
            return drawBorders;
        }
        
        public boolean isTracing() {
            return tracing;
        }
        
        public boolean isAntiAliasing() {
            return antialiasing;
        }
        
        public int getLabelIndex() {
            return labelIndex;
        }
        
        public float getMaxRatioScale() {
            return maxRatioScale;
        }
        
        public float getMinRatioScale() {
            return minRatioScale;
        }
        
        public float getMaxCY3Scale() {
            return maxCY3Scale;
        }
        
        public float getMaxCY5Scale() {
            return maxCY5Scale;
        }
        
        /**
         * Return current positive gradient image
         */
        public BufferedImage getPositiveGradientImage() {
            BufferedImage image = this.posRedColorImage;
            switch (this.colorScheme){
                case IDisplayMenu.GREEN_RED_SCHEME:
                    break;
                case IDisplayMenu.BLUE_YELLOW_SCHEME:
                    image = this.posYellowColorImage;
                    break;
                case IDisplayMenu.CUSTOM_COLOR_SCHEME:
                    if(this.posCustomColorImage != null)
                        image = this.posCustomColorImage;
            }
            return image;
        }
        
        
        /**
         * Return current negative gradient image
         */
        public BufferedImage getNegativeGradientImage() {
            BufferedImage image = this.negGreenColorImage;
            switch (this.colorScheme){
                case IDisplayMenu.GREEN_RED_SCHEME:
                    break;
                case IDisplayMenu.BLUE_YELLOW_SCHEME:
                    image = this.negBlueColorImage;
                    break;
                case IDisplayMenu.CUSTOM_COLOR_SCHEME:
                    if(this.negCustomColorImage != null)
                        image = this.negCustomColorImage;
            }
            return image;
        }
        
        /**
         * Creates a gradient image given specifiedColors
         */
        public BufferedImage createGradientImage(Color color1, Color color2) {
            // BufferedImage image = (BufferedImage)createCompatibleImage(256,1);
            
            BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
            Graphics2D graphics = image.createGraphics();
            GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
            graphics.setPaint(gp);
            graphics.drawRect(0, 0, 255, 1);
            return image;
        }
        
        /**
         * Returns true if the use gradient color box is selected
         */
        public boolean getColorGradientState(){
            return useColorGradient;
        }
        
        public void setColorGradientState(boolean state){
            useColorGradient = state;
        }
        
        
    }
    
    /**
     * The class to present a state of the distance menu.
     */
    private class DistanceMenu implements IDistanceMenu {
        private boolean absolute = false;
        private int function = Algorithm.DEFAULT;
        
        public boolean isAbsoluteDistance() {
            return absolute;
        }
        
        public int getDistanceFunction() {
            return function;
        }
        
        public String getFunctionName(int function) {
            String name;
            switch (function) {
                case Algorithm.PEARSON: name="Pearson correlation"; break;
                case Algorithm.COSINE: name="Cosine correlation"; break;
                case Algorithm.COVARIANCE: name="Covariance"; break;
                case Algorithm.EUCLIDEAN: name="Euclidean distance"; break;
                case Algorithm.DOTPRODUCT: name="Average dot product"; break;
                case Algorithm.PEARSONUNCENTERED: name="Pearson uncentered"; break;
                case Algorithm.PEARSONSQARED: name="Pearson squared"; break;
                case Algorithm.MANHATTAN: name="Manhattan distance"; break;
                case Algorithm.SPEARMANRANK: name="Spearman rank correlation"; break;
                case Algorithm.KENDALLSTAU: name="Kendall's Tau"; break;
                case Algorithm.MUTUALINFORMATION: name="Mutual information"; break;
                default: { name="not defined";}
            }
            return name;
        }
    }
    
    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
}
