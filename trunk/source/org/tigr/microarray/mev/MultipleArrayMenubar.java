/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MultipleArrayMenubar.java,v $
 * $Revision: 1.17 $
 * $Date: 2007-12-19 21:39:35 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.action.DefaultAction;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.util.awt.AccessibleColorPalette;

public class MultipleArrayMenubar extends JMenuBar {
    
    private DisplayMenu displayMenu = new DisplayMenu();
    private DistanceMenu distanceMenu = new DistanceMenu();
    private JMenu normalizationMenu;
    private JMenu labelMenu;
    private JMenu expLabelSelectionMenu;
    private JMenu adjustMenu;
    private JMenu filterMenu;
    private JMenu clusterMenu;
    private ButtonGroup labelGroup;
    private ActionListener listener;
    private boolean affyNormAdded = false;
    private ButtonGroup experimentLabelGroup;

    JMenu fileMenu;
    JMenuItem loadDataMenuItem, loadAnalysisItem;

    /**
     * Raktim
     * CGH Specific Menus
     */
    private JMenu cloneValuesMenu;
    private JMenu cghDispMenu;
    private JMenu cghAnalysisMenu;
    private JMenu helpMenu;
    CGHDisplayMenu cghDisplayMenu = new CGHDisplayMenu();
    CGHCloneValueMenu cghCloneValueMenu = new CGHCloneValueMenu();
   
    JMenu gaggleMenu, targetMenu, showMenu;
    ButtonGroup targetGooseNameGroup, showtargetGooseNameGroup;
   
    private ActionManager actionManager;


    /**
     * Constructs a <code>MultipleArrayMenubar</code> using specified
     * action maneger.
     * @see ActionManager
     */
    public MultipleArrayMenubar(ActionManager manager) {
        listener = manager.getListener();
        actionManager = manager;
        fileMenu = new JMenu("File");
        loadDataMenuItem = createJMenuItem(manager.getAction(ActionManager.LOAD_ACTION));
        fileMenu.add(loadDataMenuItem);
        fileMenu.addSeparator();
        loadAnalysisItem = createJMenuItem(manager.getAction(ActionManager.LOAD_ANALYSIS_ACTION));
        fileMenu.add(loadAnalysisItem);
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_ANALYSIS_ACTION)));
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.SAVE_ANALYSIS_AS_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.NEW_SCRIPT_ACTION)));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem(manager.getAction(ActionManager.LOAD_SCRIPT_ACTION)));
        
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
        
        JMenu spotMenu = new JMenu("Gene/Row Adjustments");
        spotMenu.add(createJMenuItem("Normalize Genes/Rows", ActionManager.NORMALIZE_SPOTS_CMD, listener));
        spotMenu.add(createJMenuItem("Divide Genes/Rows by RMS", ActionManager.DIVIDE_SPOTS_RMS_CMD, listener));
        spotMenu.add(createJMenuItem("Divide Genes/Rows by SD", ActionManager.DIVIDE_SPOTS_SD_CMD, listener));
        spotMenu.add(createJMenuItem("Mean Center Genes/Rows", ActionManager.MEAN_CENTER_SPOTS_CMD, listener));
        spotMenu.add(createJMenuItem("Median Center Genes/Rows", ActionManager.MEDIAN_CENTER_SPOTS_CMD, listener));
        spotMenu.add(createJMenuItem("Digitalize Genes/Rows", ActionManager.DIGITAL_SPOTS_CMD, listener));
        adjustMenu.add(spotMenu);
        adjustMenu.addSeparator();
        
        
        JMenu sampMenu = new JMenu("Sample/Column Adjustments");
        sampMenu.add(createJMenuItem("Normalize Samples/Columns", ActionManager.NORMALIZE_EXPERIMENTS_CMD, listener));
        sampMenu.add(createJMenuItem("Divide Samples/Columns by RMS", ActionManager.DIVIDE_EXPERIMENTS_RMS_CMD, listener));
        sampMenu.add(createJMenuItem("Divide Samples/Columns by SD", ActionManager.DIVIDE_EXPERIMENTS_SD_CMD, listener));
        sampMenu.add(createJMenuItem("Mean Center Samples/Columns", ActionManager.MEAN_CENTER_EXPERIMENTS_CMD, listener));
        sampMenu.add(createJMenuItem("Median Center Samples/Columns", ActionManager.MEDIAN_CENTER_EXPERIMENTS_CMD, listener));
        sampMenu.add(createJMenuItem("Digitalize Samples/Columns", ActionManager.DIGITAL_EXPERIMENTS_CMD, listener));
        adjustMenu.add(sampMenu);
        adjustMenu.addSeparator();
//      vu 7.22.05
	    JMenu repMenu = new JMenu( "Replicate Analysis" );
        repMenu.add( createJMenuItem( "Rama Documentation", ActionManager.RAMA_DOC_CMD, listener ) );
	    repMenu.add( createJMenuItem( "Rama", ActionManager.RAMA_CMD, listener ) );
	    adjustMenu.add( repMenu );
	    adjustMenu.addSeparator();
        
        JMenu logMenu = new JMenu("Log Transformations");
        logMenu.add(createJMenuItem("Log2 Transform", ActionManager.LOG2_TRANSFORM_CMD, listener));
        logMenu.add(createJMenuItem("UnLog2 Transform", ActionManager.UNLOG2_TRANSFORM_CMD, listener));
        logMenu.add(createJMenuItem("Log10 to Log2", ActionManager.LOG10_TO_LOG2_CMD, listener));
        logMenu.add(createJMenuItem("Log2 to Log10", ActionManager.LOG2_TO_LOG10_CMD, listener));
        adjustMenu.add(logMenu);
        adjustMenu.addSeparator();
        
        //wwang add low intensity filter of affy data and cdna
        filterMenu = new JMenu("Data Filters"); 
        JMenu lowIntensityMenu=new JMenu("Low Intensity Cutoff Filter");
        lowIntensityMenu.add(manager.getAction(ActionManager.CDNA_LOW_INTENSITY_ACTION));
       	lowIntensityMenu.add(manager.getAction(ActionManager.OLIGEN_LOW_INTENSITY_ACTION));
        filterMenu.add(lowIntensityMenu);
        filterMenu.addSeparator();
        
        //wwang
        filterMenu.add(createJMenuItem("Affymetrix detection call Noise Filter", ActionManager.USE_GCOS_PERCENTAGE_CUTOFF_CMD, listener));
        //filterMenu.addSeparator();
        filterMenu.add(createJMenuItem("Affymetrix P-value Filter", ActionManager.USE_PVALUE_CUTOFF_CMD, listener));
        //filterMenu.addSeparator();
        
        filterMenu.add(createJMenuItem("Bioconductor detection call Noise Filter", ActionManager.USE_PRESENT_CALL_CMD, listener));
        filterMenu.addSeparator();
        filterMenu.add(createJMenuItem("GenePix Flags Filter", ActionManager.USE_GENEPIXFLAGS_CMD, listener));
        filterMenu.addSeparator();
        filterMenu.add(createJMenuItem("Percentage Cutoff Filter", ActionManager.USE_PERCENTAGE_CUTOFFS_CMD, listener));
        filterMenu.addSeparator();
        filterMenu.add(createJMenuItem("Variance Filter", ActionManager.USE_VARIANCE_FILTER_CMD, listener));
        adjustMenu.add(filterMenu);        
        adjustMenu.addSeparator();
                
        ButtonGroup buttonGroup = new ButtonGroup();
        normalizationMenu = new JMenu("Normalization");
        normalizationMenu.add(createJRadioButtonMenuItem("Total Intensity", ActionManager.TOTAL_INTENSITY_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Linear Regression", ActionManager.LINEAR_REGRESSION_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Ratio Statistics", ActionManager.RATIO_STATISTICS_CMD, listener, buttonGroup));
        normalizationMenu.add(createJRadioButtonMenuItem("Iterative Log", ActionManager.ITERATIVE_LOG_CMD, listener, buttonGroup));
        normalizationMenu.addSeparator();
        normalizationMenu.add(createJRadioButtonMenuItem("No Normalization", ActionManager.NO_NORMALIZATION_CMD, listener, buttonGroup, true));
        
        adjustMenu.add(normalizationMenu);
        adjustMenu.addSeparator();
        adjustMenu.add(createJCheckBoxMenuItem("Adjust Intensities of '0'", ActionManager.ADJUST_INTENSITIES_0_CMD, listener, true));
        
        add(adjustMenu);
        
        buttonGroup = new ButtonGroup();
        JMenu distanceMenu = new JMenu("Metrics");
        distanceMenu.add(createJRadioButtonMenuItem("Default Distance", ActionManager.DEFAULT_DISTANCE_CMD, listener, buttonGroup, true));
        
        distanceMenu.addSeparator();
        
        distanceMenu.add(createJRadioButtonMenuItem("Euclidean Distance", ActionManager.EUCLIDEAN_DISTANCE_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Manhattan Distance", ActionManager.MANHATTAN_DISTANCE_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Average Dot Product", ActionManager.AVERAGE_DOT_PRODUCT_CMD, listener, buttonGroup));
        
        distanceMenu.addSeparator();
        
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Correlation", ActionManager.PEARSON_CORRELATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Uncentered", ActionManager.PEARSON_UNCENTERED_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Pearson Squared", ActionManager.PEARSON_SQUARED_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Cosine Correlation", ActionManager.COSINE_CORRELATION_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Covariance Value", ActionManager.COVARIANCE_VALUE_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Spearman Rank Correlation", ActionManager.SPEARMAN_RANK_CORRELATION_CMD, listener, buttonGroup));
        
        distanceMenu.addSeparator();
        
        distanceMenu.add(createJRadioButtonMenuItem("Kendall's Tau", ActionManager.KENDALLS_TAU_CMD, listener, buttonGroup));
        distanceMenu.add(createJRadioButtonMenuItem("Mutual Information", ActionManager.MUTUAL_INFORMATION_CMD, listener, buttonGroup));
        distanceMenu.addSeparator();
        distanceMenu.add(createJCheckBoxMenuItem("Absolute distance", ActionManager.ABSOLUTE_DISTANCE_CMD, listener, false));
        add(distanceMenu);
        
        JMenu analysisMenu = new JMenu("Analysis");
        // add analysis menu here
        addAnalysisMenu(analysisMenu, manager);
        add(analysisMenu);
        
        JMenu displayMenu = new JMenu("Display");
        
        
        JMenu expLabelMenu = new JMenu("Sample/Column Labels");   

        expLabelSelectionMenu = new JMenu("Select Sample Label");
        expLabelMenu.add(expLabelSelectionMenu);
        expLabelMenu.addSeparator();
        expLabelMenu.add(this.createJMenuItem("Edit Labels/Reorder Samples", ActionManager.ADD_NEW_EXPERIMENT_LABEL_CMD, listener));        
        expLabelMenu.add(createJMenuItem("Abbr. Sample Names", ActionManager.TOGGLE_ABBR_EXPT_NAMES_CMD, listener));    
        //IDF editing/display
        expLabelMenu.add(this.createJMenuItem("Edit / Display IDF Fields", ActionManager.ADD_NEW_IDF_LABEL_CMD, listener));
        experimentLabelGroup = new ButtonGroup();        
        displayMenu.add(expLabelMenu);
        
        labelMenu = new JMenu("Gene/Row Labels");        
        labelGroup = new ButtonGroup();      
        displayMenu.add(labelMenu);
        displayMenu.addSeparator();
        
      
        JMenu colorSchemeMenu = new JMenu("Color Scheme");
        colorSchemeMenu.add(createJMenuItem("Green-Black-Red Scheme", ActionManager.GREEN_RED_COLOR_SCHEME_CMD, listener));
        colorSchemeMenu.add(createJMenuItem("Blue-Black-Yellow Scheme", ActionManager.BLUE_YELLOW_COLOR_SCHEME_CMD, listener));
        colorSchemeMenu.add(createJMenuItem("Rainbow Scheme", ActionManager.RAINBOW_COLOR_SCHEME_CMD, listener));
        colorSchemeMenu.addSeparator();
        colorSchemeMenu.add(createJMenuItem("Custom Color Scheme", ActionManager.CUSTOM_COLOR_SCHEME_CMD, listener));
        colorSchemeMenu.addSeparator();
        colorSchemeMenu.add(createJMenuItem("Accessible Color Scheme", ActionManager.ACCESSIBLE_COLOR_SCHEME_CMD, listener));
        colorSchemeMenu.addSeparator();
        colorSchemeMenu.add(createJCheckBoxMenuItem("Use Color Gradient on Graphs", ActionManager.COLOR_GRADIENT_CMD, listener));
        displayMenu.add(colorSchemeMenu);
                
        displayMenu.add(createJMenuItem("Set Color Scale Limits", ActionManager.DISPLAY_SET_RATIO_SCALE_CMD, listener));
        displayMenu.addSeparator();
        
        JMenu sizeMenu = new JMenu("Set Element Size");
        buttonGroup = new ButtonGroup();
        sizeMenu.add(createJRadioButtonMenuItem("5 x 2", ActionManager.DISPLAY_5X2_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("10 x 10", ActionManager.DISPLAY_10X10_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("20 x 5", ActionManager.DISPLAY_20X5_CMD, listener, buttonGroup, true));
        sizeMenu.add(createJRadioButtonMenuItem("50 x 10", ActionManager.DISPLAY_50X10_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("Custom", ActionManager.DISPLAY_OTHER_CMD, listener, buttonGroup));
        displayMenu.add(sizeMenu);

        displayMenu.add(createJCheckBoxMenuItem("Draw Borders", ActionManager.DISPLAY_DRAW_BORDERS_CMD, listener, false));        
        displayMenu.addSeparator();
        
        JMenu clusteringMenu = new JMenu("Cluster Viewing Options");
        clusteringMenu.add(createJCheckBoxMenuItem("Compact Cluster Color Groups", ActionManager.COMPACT_CLUSTERS_CMD, listener, false));
        clusteringMenu.add(createJCheckBoxMenuItem("Show Mouse Rectangles", ActionManager.SHOW_RECTS_CMD, listener, true));
        clusteringMenu.add(createJCheckBoxMenuItem("Auto-Arrange Cluster Colors", ActionManager.AUTO_ARRANGE_COLORS, listener, false));
        
        displayMenu.add(clusteringMenu);
        
        add(displayMenu);
        
        JMenu utilMenu = new JMenu("Utilities");
        utilMenu.add(createJMenuItem(manager.getAction(ActionManager.SEARCH_ACTION)));
        utilMenu.addSeparator();
        
        clusterMenu = new JMenu("Cluster Utilities");
        
        JMenu importMenu = new JMenu("Manual Cluster Import");                
        importMenu.add(manager.getAction(ActionManager.IMPORT_GENE_LIST_ACTION));
        importMenu.add(manager.getAction(ActionManager.IMPORT_SAMPLE_LIST_ACTION));        
        clusterMenu.add(importMenu);
        
        JMenu binImportMenu = new JMenu("Binned Cluster Import");                
        binImportMenu.add(manager.getAction(ActionManager.BIN_IMPORT_GENE_LIST_ACTION));
        binImportMenu.add(manager.getAction(ActionManager.BIN_IMPORT_SAMPLE_LIST_ACTION));        
        clusterMenu.add(binImportMenu);
        
        JMenu autoImportMenu = new JMenu("Automatic Cluster Import");                
        autoImportMenu.add(manager.getAction(ActionManager.AUTO_IMPORT_GENE_LIST_ACTION));
        autoImportMenu.add(manager.getAction(ActionManager.AUTO_IMPORT_SAMPLE_LIST_ACTION));        
        clusterMenu.add(autoImportMenu);
        
        clusterMenu.addSeparator();
        
        clusterMenu.add(manager.getAction(ActionManager.DELETE_ALL_ACTION));
        clusterMenu.add(manager.getAction(ActionManager.DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION));
        utilMenu.add(clusterMenu);
        
        utilMenu.addSeparator();
                
        utilMenu.add(manager.getAction(ActionManager.APPEND_GENE_ANNOTATION_ACTION));        
        utilMenu.add(manager.getAction(ActionManager.IMPORT_RESOURCERER_ANNOTATION_ACTION));
        utilMenu.add(manager.getAction(ActionManager.APPEND_SAMPLE_ANNOTATION_ACTION));  
        utilMenu.add(manager.getAction(ActionManager.CHANGE_SPECIES_NAME_ACTION));
                        
        targetGooseNameGroup = new ButtonGroup();
        showtargetGooseNameGroup = new ButtonGroup();
        gaggleMenu = new JMenu("Gaggle");
        gaggleMenu.setIcon(actionManager.getIcon(ActionManager.GAGGLE_ICON));
        gaggleMenu.add(manager.getAction(ActionManager.GAGGLE_CONNECT));        
        gaggleMenu.add(manager.getAction(ActionManager.GAGGLE_DISCONNECT));
        targetMenu = new JMenu("Broadcast Target");
        showMenu = new JMenu("Show Goose");
        JMenuItem notargets = new JMenuItem("No Broadcast Targets");
        JMenuItem noGeese = new JMenuItem("No Geese Available");
        notargets.setEnabled(false);
        noGeese.setEnabled(false);
        targetMenu.add(notargets);
        showMenu.add(noGeese);
        gaggleMenu.add(targetMenu);
        gaggleMenu.add(showMenu);
        gaggleMenu.getMenuComponent(1).setEnabled(false);
        
        utilMenu.add(gaggleMenu);
        add(utilMenu);    
       
    }
    
    
    
    /**
     * Constructs a <code>MultipleArrayMenubar</code> using specified
     * action maneger.
     * @see ActionManager
     */
    public MultipleArrayMenubar(MultipleArrayMenubar origMenubar, ActionManager manager) {
        //run main constructor
        this(manager);
        
        IDisplayMenu origDisplayMenu = origMenubar.getDisplayMenu();
       
        this.setColorSchemeIndex(origDisplayMenu.getColorScheme());
        Dimension dim = origDisplayMenu.getElementSize();
        this.setElementSize(dim.width, dim.height);
        this.setAntiAliasing(origDisplayMenu.isAntiAliasing());
        this.setTracing(origDisplayMenu.isTracing());
        this.setLabelIndex(origDisplayMenu.getLabelIndex());
        this.setPaletteStyle(origDisplayMenu.getPaletteStyle());
        this.setGRScale(origDisplayMenu.isGRScale());
        this.setDrawBorders(origDisplayMenu.isDrawingBorder());
        this.setCompactClusters(origDisplayMenu.isCompactClusters());
        this.setShowRects(origDisplayMenu.isShowRects());
        this.setAutoArrangeColors(origDisplayMenu.isAutoArrangeColors());
        this.setMaxRatioScale(origDisplayMenu.getMaxRatioScale());
        this.setMinRatioScale(origDisplayMenu.getMinRatioScale());
        this.setMidRatioValue(origDisplayMenu.getMidRatioValue());
        this.setColorGradientState(origDisplayMenu.getColorGradientState());
        this.setNegativeGradientImage(origDisplayMenu.getNegativeGradientImage());
        this.setPositiveGradientImage(origDisplayMenu.getPositiveGradientImage());
        
        this.setUseDoubleGradient(origDisplayMenu.getUseDoubleGradient());
        this.addLabelMenuItems((origMenubar).getLabelMenuItems(), null);
        
        IDistanceMenu origDistanceMenu = origMenubar.getDistanceMenu();
        
        this.setDistanceAbsolute(origDistanceMenu.isAbsoluteDistance());
        this.setDistanceFunction(origDistanceMenu.getDistanceFunction());
        //now have the full menu, minus probably the full sort and label menu
    }
        
    public void setDataLoadersEnabled(boolean enabled) {
	    loadDataMenuItem.setEnabled(enabled);
	    loadAnalysisItem.setEnabled(enabled);
    }

    public void synchronizeSettings(MultipleArrayMenubar origMenuBar) {
        
        //first synchronize the distance menu
        
        JMenu origMenu = origMenuBar.getMenu(2);
        JMenu menu = getMenu(2);
        Object menuObject, origMenuObject;
        
        for(int i = 0; i < menu.getMenuComponentCount(); i++) {
            menuObject = menu.getMenuComponent(i);
            origMenuObject = origMenu.getMenuComponent(i);
            
            if(origMenuObject instanceof JRadioButtonMenuItem) {
                ((JRadioButtonMenuItem)menuObject).setSelected( ((JRadioButtonMenuItem)origMenuObject).isSelected());
            } else if (origMenuObject instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)menuObject).setSelected( ((JCheckBoxMenuItem)origMenuObject).isSelected());
            }
        }
        
        // now synchronize the display menu
        origMenu = origMenuBar.getMenu(4);
        menu = getMenu(4);
        
        //strategy: if it's a JMenu send it to the sync method else deal with it directly
        Component comp, origComp;
        int numComponents = origMenu.getMenuComponentCount();
        for(int i = 0; i < numComponents; i++) {
            origComp = origMenu.getMenuComponent(i);
            comp = menu.getMenuComponent(i);
            
            if(origComp instanceof JMenu) {
                syncMenus((JMenu) origComp, (JMenu)comp);
            } else {
                if(origComp instanceof JRadioButtonMenuItem) {
                    ((JRadioButtonMenuItem)comp).setSelected( ((JRadioButtonMenuItem)origComp).isSelected());
                } else if (origComp instanceof JCheckBoxMenuItem) {
                    ((JCheckBoxMenuItem)comp).setSelected( ((JCheckBoxMenuItem)origComp).isSelected());
                }
            }
        }
    }
    
    private void syncMenus(JMenu origMenu, JMenu menu) {
        Object menuObject, origMenuObject;
        
        for(int i = 0; i < menu.getMenuComponentCount(); i++) {
            menuObject = menu.getMenuComponent(i);
            origMenuObject = origMenu.getMenuComponent(i);
            
            if(origMenuObject instanceof JRadioButtonMenuItem) {
                ((JRadioButtonMenuItem)menuObject).setSelected( ((JRadioButtonMenuItem)origMenuObject).isSelected());
            } else if (origMenuObject instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)menuObject).setSelected( ((JCheckBoxMenuItem)origMenuObject).isSelected());
            } else if(origMenuObject instanceof JMenu) {
                syncMenus((JMenu)origMenuObject, (JMenu)menuObject);
            }
        }
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
                setEnableMenuItem("File", ActionManager.NEW_SCRIPT_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_SCRIPT_COMMAND, false);
                break;
            case TMEV.DATA_AVAILABLE:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_MATRIX_COMMAND, true);
                setEnableMenuItem("File", ActionManager.SAVE_IMAGE_COMMAND, true);
                setEnableMenuItem("File", ActionManager.NEW_SCRIPT_COMMAND, true);
                setEnableMenuItem("File", ActionManager.LOAD_SCRIPT_COMMAND, true);
                setEnableMenuItem("File", ActionManager.PRINT_IMAGE_COMMAND, true);
                setEnableMenu("Adjust Data", true);
                setEnableMenu("Normalization", true);
                setEnableMenu("Metrics", true);
                setEnableMenu("Utilities", true);
                setEnableMenu("Analysis", true);
                setEnableMenu("Display", true);
                setEnableMenu("Sort", true);
                clusterMenu.setEnabled(true);
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
                //  setEnableMenuItem("File", ActionManager.NEW_SCRIPT_COMMAND, false);
                //  setEnableMenuItem("File", ActionManager.LOAD_SCRIPT_COMMAND, false);
                setEnableMenuItem("File", ActionManager.NEW_SCRIPT_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_SCRIPT_COMMAND, false);
                
                
                setEnableMenuItem("File", ActionManager.LOAD_DIRECTORY_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_STANFORD_COMMAND, false);
                break;
            case TMEV.DATA_AVAILABLE:
                setEnableMenu("File", true);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_ANALYSIS_AS_COMMAND, false);
                setEnableMenuItem("File", ActionManager.NEW_SCRIPT_COMMAND, false);
                setEnableMenuItem("File", ActionManager.LOAD_SCRIPT_COMMAND, false);
                
                
                setEnableMenuItem("File", ActionManager.SAVE_MATRIX_COMMAND, false);
                setEnableMenuItem("File", ActionManager.SAVE_IMAGE_COMMAND, false);
                setEnableMenuItem("File", ActionManager.PRINT_IMAGE_COMMAND, false);
                setEnableMenu("Adjust Data", false);
                setEnableMenu("Normalization", false);
                setEnableMenu("Metrics", false);
                clusterMenu.setEnabled(false);
                //setEnableMenu("Utilities", false);
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
    
    
    public void enableNormalizationMenu(boolean enable) {
        normalizationMenu.setEnabled(enable);
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
    
    
    public String[] getLabelMenuItems(){
    	//String[] temp = new String[labelGroup.getElements().];
    	Vector<String> temp = new Vector<String>();
    	Enumeration labels = labelGroup.getElements();
    	
    	while(labels.hasMoreElements()){
    		JRadioButtonMenuItem aButton = (JRadioButtonMenuItem)labels.nextElement();
    		temp.add((String)aButton.getText());
    		
    	}
    	String[] temp2 = new String[temp.size()];
    	for(int i=0; i<temp.size();i++)
    		temp2[i] = (String)temp.get(i);
    	
    	
    	
    	
   		return temp2;
    }
  
    
   
    /**
     * Raktim - Added for Pilot Demo only
     * @param fieldNames
     * @param annoFields
     */
    public void addLabelMenuItems(String [] fieldNames, String[] annoFields){
        JRadioButtonMenuItem item;
       //ButtonGroup bg = new ButtonGroup();
        DefaultAction action;
        String thisFieldName;
    	String prefix = "Label by ";
        for(int i = 0; i < fieldNames.length; i++){
        	thisFieldName = fieldNames[i];
        	if(thisFieldName.startsWith(prefix)) {
        		thisFieldName = thisFieldName.substring(prefix.length()).trim();
        	}        		
        	action = new DefaultAction(actionManager, "Label by " + thisFieldName, ActionManager.DISPLAY_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, String.valueOf(i));
            item = new JRadioButtonMenuItem(action);
            labelGroup.add(item);
            if(i == 0)
                item.setSelected(true);
            this.labelMenu.add(item);
        }
        String thisAnnoField;
        if(annoFields != null) {
	        for(int i = 0; i < annoFields.length; i++){
	        	thisAnnoField = annoFields[i];
	        	if(thisAnnoField.startsWith(prefix)) {
	        		thisAnnoField = thisAnnoField.substring(prefix.length()).trim();
	        	}
	        	action = new DefaultAction(actionManager, "Label by "+thisAnnoField, ActionManager.DISPLAY_LABEL_CMD);
	            action.putValue(ActionManager.PARAMETER, String.valueOf(i + fieldNames.length));
	            item = new JRadioButtonMenuItem(action);
	            labelGroup.add(item);
	            this.labelMenu.add(item);
	        }
        }
    }
    

    
    
    
    
    public void addExperimentLabelMenuItems(Vector fieldNames){
        JRadioButtonMenuItem item;
        DefaultAction action;
        for(int i = 0; i < fieldNames.size(); i++){
            action = new DefaultAction(actionManager, "Label by "+(String)fieldNames.elementAt(i), ActionManager.DISPLAY_EXPERIMENT_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, (String)(fieldNames.elementAt(i)));
            item = new JRadioButtonMenuItem(action);
            experimentLabelGroup.add(item);
            if(i == 0)
                item.setSelected(true);
            this.expLabelSelectionMenu.add(item);
        }
    }
    
    public void addNewExperimentLabelMenuItem(String label) {
        DefaultAction action = new DefaultAction(actionManager, "Label by "+label, ActionManager.DISPLAY_EXPERIMENT_LABEL_CMD);
        action.putValue(ActionManager.PARAMETER, label);
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
        experimentLabelGroup.add(item);
        expLabelSelectionMenu.add(item);
    }
    

    public void replaceGaggleTargetMenuItems(String[] gooseNames) {
    	this.targetMenu.removeAll();
    	this.showMenu.removeAll();

    	JRadioButtonMenuItem item, item1;
    	DefaultAction action, action1; 
    	String cmd;
        for(int i = 0; i < gooseNames.length; i++){
            cmd = gooseNames[i];

            action = new DefaultAction(actionManager, cmd, ActionManager.SELECT_TARGET_GOOSE_CMD);
            action.putValue(ActionManager.PARAMETER, gooseNames[i]);
            item = new JRadioButtonMenuItem(action);
            targetGooseNameGroup.add(item);
            this.targetMenu.add(item);
            item.setSelected(false);

            action1 = new DefaultAction(actionManager, cmd, ActionManager.SHOW_GOOSE_CMD);
            action1.putValue(ActionManager.PARAMETER, gooseNames[i]);
            item1 = new JRadioButtonMenuItem(action1);
            showtargetGooseNameGroup.add(item1);
            this.showMenu.add(item1);
            item1.setSelected(false);
            
            if(i == 0) {
                item.setSelected(true);
                item1.setSelected(true);
            }
        }
    }


    
    public void replaceExperimentLabelMenuItems(String [] fieldNames){

        this.expLabelSelectionMenu.removeAll();
        JRadioButtonMenuItem item;
        DefaultAction action;
        String cmd;
        for(int i = 0; i < fieldNames.length; i++){
            cmd = "Label by "+fieldNames[i];
            action = new DefaultAction(actionManager, cmd, ActionManager.DISPLAY_EXPERIMENT_LABEL_CMD);
            action.putValue(ActionManager.PARAMETER, fieldNames[i]);
            item = new JRadioButtonMenuItem(action);
            experimentLabelGroup.add(item);
            this.expLabelSelectionMenu.add(item);
            item.setSelected(false);
            if(i == 0)
                item.setSelected(true);
        }
    }
    
    public void replaceLabelMenuItems(String [] fieldNames){
    
        //remove all menu items
    	 this.labelMenu.removeAll();
    	labelGroup=new ButtonGroup();
    
        JRadioButtonMenuItem item;
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
    
    
    //Added by Sarita
//    public void replaceLabelMenuItems(String [] fieldNames, String[]annoFields){
//        //remove all menu items
//        this.labelMenu.removeAll();
//        labelGroup=new ButtonGroup();
//       this.addLabelMenuItems(fieldNames, annoFields);
//    }
    
    
    
    public void addAffyFilterMenuItems(){
        filterMenu.addSeparator();
        filterMenu.add(createJMenuItem("Set Detection Filter", ActionManager.SET_DETECTION_FILTER_CMD, listener));
        filterMenu.add(createJCheckBoxMenuItem("Use Detection Filter", ActionManager.USE_DETECTION_FILTER_CMD, listener));
        filterMenu.add(createJMenuItem("Set Fold Filter", ActionManager.SET_FOLD_FILTER_CMD, listener));
        filterMenu.add(createJCheckBoxMenuItem("Use Fold Filter", ActionManager.USE_FOLD_FILTER_CMD, listener));
    }
    
    
    public void addAffyNormMenuItems() {
        adjustMenu.addSeparator();
        JMenu menu = new JMenu("Affymetrix Adjustments");
        menu.add(createJMenuItem("Divide Genes by Median", ActionManager.DIVIDE_GENES_MEDIAN_CMD, listener));
        menu.add(createJMenuItem("Divide Genes by Mean", ActionManager.DIVIDE_GENES_MEAN_CMD, listener));
        adjustMenu.add(menu);
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

    }
    
    public void replaceSortMenuItems(String [] fieldNames){

      
    }
    
    /**
     * Adds analysis menu items.
     */
    private void addAnalysisMenu(JMenu menu, ActionManager manager) {
        String []category={"Clustering","Statistics","Classification","Data Reduction","Meta Analysis","Visualization","Miscellaneous"};
        for(int i=0;i<category.length;i++){
        		menu.add(createJMenuItem(category[i],manager));
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
     * Creates a menu item from specified action and category.
     * add by wwang
     */
    private JMenu createJMenuItem(String category,ActionManager manager) {
    	int index=0;
    	Action action;
    	JMenu item = new JMenu(category);
    	while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)))!=null) {
    		if((action.getValue(ActionManager.CATEGORY)).equals(category))
    			item.add(createJMenuItem(action));
        index++;
    	}
        return item;
    }
    
    /**
     * Creates a menu item with specified name and action command.
     */
    private JMenuItem createJMenuItem(String name, String command, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
       
        item.setActionCommand(command);
        item.addActionListener(listener);
        return item;
    }
    
    
    /**
     * Creates a check box menu item with specified name, action command and state.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener, boolean isSelected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name and action command.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, ActionListener listener) {
        return createJCheckBoxMenuItem(name, command, listener, false);
    }
    
    /**
     * Creates a radio button menu item with specified name, action command and state.
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
     * Sets compact clusters attribute.
     */
    void setCompactClusters(boolean compactClusters) {
        displayMenu.compactClusters = compactClusters;
    }
    /**
     * Sets compact clusters attribute.
     */
    void setShowRects(boolean showRects) {
        displayMenu.showRects = showRects;
    }
    /**
     * Sets cluster colors auto-arrange attribute.
     */
    void setAutoArrangeColors(boolean arrangeColors) {
        displayMenu.arrangeColors = arrangeColors;
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
     *  Sets mid ratio scale value.
     */
    void setMidRatioValue(float value) {
    	displayMenu.midRatioValue = value;
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

   
    void setPositiveGradientImage(BufferedImage image){
        displayMenu.currentPositiveColorImage = image;
    }
    void setNegativeGradientImage(BufferedImage image){
        displayMenu.currentNegativeColorImage = image;
    }
    
    /**
     * Sets color scheme index
     */
    void setColorSchemeIndex(int index){
    	displayMenu.colorScheme = index;

      switch (displayMenu.colorScheme){
      	case IDisplayMenu.ACCESSIBLE_COLOR_SCHEME:
      		break;
          case IDisplayMenu.GREEN_RED_SCHEME:
        	  displayMenu.currentNegativeColorImage = displayMenu.negGreenColorImage;
        	  displayMenu.currentPositiveColorImage = displayMenu.posRedColorImage;
              break;
          case IDisplayMenu.BLUE_YELLOW_SCHEME:

        	  displayMenu.currentNegativeColorImage = displayMenu.negBlueColorImage;
        	  displayMenu.currentPositiveColorImage = displayMenu.posYellowColorImage;
              break;
          case IDisplayMenu.CUSTOM_COLOR_SCHEME:
              break;
          case IDisplayMenu.RAINBOW_COLOR_SCHEME:  
        	  displayMenu.currentPositiveColorImage = displayMenu.rainbowImage;
      }
    }
    
    int getColorScheme(){
        return displayMenu.getColorScheme();
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
    
    public void setUseDoubleGradient(boolean useDouble) {
    	this.displayMenu.setUseDoubleGradient(useDouble);
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
        private boolean compactClusters = false;
        private boolean showRects = false;
        private boolean arrangeColors = false;
        private boolean antialiasing = true;
        private boolean grscale = true;
        private boolean useColorGradient = false;
        private float minRatioScale = -3f;
        private float maxRatioScale = 3f;
        private float midRatioValue = 0f;
        private float maxCY3Scale = 0f;
        private float maxCY5Scale = 0f;
        
        private int colorScheme = IDisplayMenu.GREEN_RED_SCHEME;
        private BufferedImage negGreenColorImage = createGradientImage(Color.green, Color.black);
        private BufferedImage posRedColorImage = createGradientImage(Color.black, Color.red);
        private BufferedImage negBlueColorImage = createGradientImage(Color.blue, Color.black);
        private BufferedImage posYellowColorImage = createGradientImage(Color.black, Color.yellow);
        private BufferedImage rainbowImage = createRainbowImage();
        private boolean useDoubleGradient = true;
        
        BufferedImage currentNegativeColorImage = negGreenColorImage;
        BufferedImage currentPositiveColorImage = posRedColorImage;
        
        //Added by Sarita
        AccessibleColorPalette color=new AccessibleColorPalette();
       
        
        public int getPaletteStyle() {
            return paletteStyle;
        }
        
        public boolean isGRScale() {
            return grscale;
        }
        
        public Dimension getElementSize() {
            return elementSize;
        }
        
        public boolean getUseDoubleGradient() {
        	return useDoubleGradient;        
        }
        
        public void setUseDoubleGradient(boolean useDouble) {
        	useDoubleGradient = useDouble;         
        }
        
        public boolean isDrawingBorder() {
            return drawBorders;
        }
        
        public boolean isCompactClusters(){
        	return compactClusters;
        }
        public boolean isShowRects(){
        	return showRects;
        }
        public boolean isAutoArrangeColors(){
        	return arrangeColors;
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
        
        public float getMidRatioValue() {
        	return midRatioValue;        
        }
        
        public float getMaxCY3Scale() {
            return maxCY3Scale;
        }
        
        public float getMaxCY5Scale() {
            return maxCY5Scale;
        }
        
        public int getColorScheme() {
            return colorScheme;
        }
        
        /**
         * Return current positive gradient image
         */
        public BufferedImage getPositiveGradientImage() {
        	return currentPositiveColorImage;
        }
        
        
        /**
         * Return current negative gradient image
         */
        public BufferedImage getNegativeGradientImage() {
        	return currentNegativeColorImage;
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
        
        private BufferedImage createRainbowImage() {
        	Vector palette = buildPalette();
        	
            BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
            Graphics2D graphics = image.createGraphics();

            drawScale((Graphics2D)graphics, palette);
            
            return image;
        }
        
        private void drawScale(Graphics2D g, Vector palette) {	
            for (int i = 0; i < palette.size(); i++) {
            	g.setColor((Color) palette.elementAt(i));
                g.fillRect(i, 0, 1, 1);
            }
        }        
        
        private Vector buildPalette() {
            Vector<Color> palette = new Vector<Color>(256);
            Color newColor;
            double r, g, b;
            
            newColor = new Color(0, 0, 0);
            palette.addElement(newColor);
            
            for (int i = 1; i < 256; i++) {
                i = 255 - i;
                
                r = 0; g = 0; b = 0;
                
                if (i < 33) r = 255;
                else if (i > 32 && i < 108) r = Math.abs( 255 * Math.cos((i - 32) * Math.PI / 151));
                else if (i > 107) r = 0;
                
                if (i < 5) g = 0;
                else if (i > 4 && i < 101) g = Math.abs((255 * Math.cos((i - 100) * Math.PI / 189)));
                else if (i > 100 && i < 229) g = Math.abs((255 * Math.cos((i - 100) * Math.PI / 294)));
                else if (i > 230) g = 0;
                
                if (i < 72) b = 0;
                else if (i > 71 && i < 200) b = Math.abs((255 * Math.cos((i - 199) * Math.PI / 256)));
                else if (i > 199) b = Math.abs((255 * Math.cos((i - 199) * Math.PI / 175)));
                
                newColor = new Color((float) r / 255, (float) g / 255, (float) b / 255);
                palette.addElement(newColor);
                
                i = 255 - i;
            }
            
            return palette;
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
                case Algorithm.PEARSON: name="Pearson Correlation"; break;
                case Algorithm.COSINE: name="Cosine Correlation"; break;
                case Algorithm.COVARIANCE: name="Covariance"; break;
                case Algorithm.EUCLIDEAN: name="Euclidean Distance"; break;
                case Algorithm.DOTPRODUCT: name="Average Dot Product"; break;
                case Algorithm.PEARSONUNCENTERED: name="Pearson Uncentered"; break;
                case Algorithm.PEARSONSQARED: name="Pearson Squared"; break;
                case Algorithm.MANHATTAN: name="Manhattan Distance"; break;
                case Algorithm.SPEARMANRANK: name="Spearman Rank Correlation"; break;
                case Algorithm.KENDALLSTAU: name="Kendall's Tau"; break;
                case Algorithm.MUTUALINFORMATION: name="Mutual Information"; break;
                default: { name="not defined";}
            }
            return name;
        }
    }
    
    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    /**
     * Raktim CGH Menus
     * NOT USED
     */
    public void addCGHMenus2() {
    	System.out.println("In addCGHMenus2()");
        cloneValuesMenu = new JMenu("CloneValues");
        add(cloneValuesMenu);
        //Add conditional ClonesMenu
        //initCloneValuesMenu(cloneValuesMenu, manager, boolean hasDyeSwap, int cloneValueType, int flankingRegionType);
        setCloneValueType(TMEV.defaultCloneValue);
        setFlankingRegionType(TMEV.defaultFlankingRegionValue);
        initCloneValuesMenu(cloneValuesMenu, listener, false, cghCloneValueMenu.getCloneValueType(), cghCloneValueMenu.getFlankingRegionType());
        //cghCloneValueMenu.setCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION);
        //cghCloneValueMenu.setFlankingRegionType(ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD);
        cghDispMenu = new JMenu("CGH Display");
        cghDispMenu.add(createJCheckBoxMenuItem("Flanking Regions", ActionManager.SHOW_FLANKING_REGIONS, listener, false));
        cghDispMenu.addSeparator();
        JMenu cghSizeMenu = new JMenu("Element Length");
        ButtonGroup buttonGroup = new ButtonGroup();
        cghSizeMenu.add(createJRadioButtonMenuItem("5", ActionManager.CGH_ELEMENT_LENGTH_5, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("10", ActionManager.CGH_ELEMENT_LENGTH_10, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("20", ActionManager.CGH_ELEMENT_LENGTH_20, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("50", ActionManager.CGH_ELEMENT_LENGTH_50, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("100", ActionManager.CGH_ELEMENT_LENGTH_100, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("Other", ActionManager.CGH_ELEMENT_LENGTH_OTHER, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("Fit", ActionManager.CGH_ELEMENT_LENGTH_FIT, listener, buttonGroup, true));
        cghDispMenu.add(cghSizeMenu);
        JMenu widthMenu = new JMenu("Element Width");
        buttonGroup = new ButtonGroup();
        widthMenu.add(createJRadioButtonMenuItem("5", ActionManager.CGH_ELEMENT_WIDTH_5, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("10", ActionManager.CGH_ELEMENT_WIDTH_10, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("20", ActionManager.CGH_ELEMENT_WIDTH_20, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("50", ActionManager.CGH_ELEMENT_WIDTH_50, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("100", ActionManager.CGH_ELEMENT_WIDTH_100, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("Other", ActionManager.CGH_ELEMENT_WIDTH_OTHER, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("Fit", ActionManager.CGH_ELEMENT_WIDTH_FIT, listener, buttonGroup, true));
        cghDispMenu.add(widthMenu);
        cghDispMenu.addSeparator();
        JMenu displayTypeMenu = new JMenu("Display Type");
        buttonGroup = new ButtonGroup();
        displayTypeMenu.add(createJRadioButtonMenuItem("Mixed", ActionManager.CGH_DISPLAY_TYPE_COMBINED, listener, buttonGroup, true));
        displayTypeMenu.add(createJRadioButtonMenuItem("Separated", ActionManager.CGH_DISPLAY_TYPE_SEPARATED, listener, buttonGroup));
        cghDispMenu.add(displayTypeMenu);
        cghDispMenu.addSeparator();
        cghDispMenu.add(createJMenuItem("Display Order", ActionManager.CGH_DISPLAY_ORDER, listener));
        //cghDisplayMenu.add(createJMenuItem("Circle Viewer Background", CGHActionManager.CIRCLE_VIEWER_BACKGROUND, listener));
        cghDispMenu.add(createJCheckBoxMenuItem("Show Header", ActionManager.SHOW_HEADER, listener, true));
        cghDispMenu.addSeparator();
        cghDispMenu.add(createJMenuItem("Clear Annotations", ActionManager.CGH_CLEAR_ANNOTATIONS, listener));
        add(cghDispMenu);
        cghAnalysisMenu = new JMenu("CGH Analysis");
        addCghAnalysisMenu(cghAnalysisMenu, /*manager*/ actionManager);
        cghAnalysisMenu.addSeparator();
        cghAnalysisMenu.add(createJMenuItem("Find Gene", ActionManager.FIND_GENE, listener));
        /*
        cghAnalysisMenu.addSeparator();
        cghAnalysisMenu.add(createJMenuItem("Compare Experiments", ActionManager.COMPARE_EXPERIMENTS, listener));
        */
        add(cghAnalysisMenu);
        helpMenu = new JMenu("Help");
        helpMenu.add(createJMenuItem("Default Distances", ActionManager.DEFAULT_DISTANCES_CMD, listener));
        helpMenu.add(createJMenuItem("Support Tree Legend", ActionManager.SHOW_SUPPORTTREE_LEGEND_COMMAND, listener));
        add(helpMenu);
        setDefaultValues();
        //enableCloneDistributions(false);
    }
    public void addCGHMenus() {
    	System.out.println("In addCGHMenus()");
        cloneValuesMenu = new JMenu("CloneValues");
        add(cloneValuesMenu);
        //Add conditional ClonesMenu
        //initCloneValuesMenu(cloneValuesMenu, manager, boolean hasDyeSwap, int cloneValueType, int flankingRegionType);
        setCloneValueType(TMEV.defaultCloneValue);
        setFlankingRegionType(TMEV.defaultFlankingRegionValue);
        initCloneValuesMenu(cloneValuesMenu, listener, false, cghCloneValueMenu.getCloneValueType(), cghCloneValueMenu.getFlankingRegionType());
        //cghCloneValueMenu.setCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION);
        //cghCloneValueMenu.setFlankingRegionType(ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD);
        JMenu dispMenu = getJMenu("Display");
        dispMenu.addSeparator();
        dispMenu.add(createJCheckBoxMenuItem("Flanking Regions", ActionManager.SHOW_FLANKING_REGIONS, listener, false));
        dispMenu.addSeparator();
        JMenu cghSizeMenu = new JMenu("Element Length");
        ButtonGroup buttonGroup = new ButtonGroup();
        cghSizeMenu.add(createJRadioButtonMenuItem("5", ActionManager.CGH_ELEMENT_LENGTH_5, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("10", ActionManager.CGH_ELEMENT_LENGTH_10, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("20", ActionManager.CGH_ELEMENT_LENGTH_20, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("50", ActionManager.CGH_ELEMENT_LENGTH_50, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("100", ActionManager.CGH_ELEMENT_LENGTH_100, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("Other", ActionManager.CGH_ELEMENT_LENGTH_OTHER, listener, buttonGroup));
        cghSizeMenu.add(createJRadioButtonMenuItem("Fit", ActionManager.CGH_ELEMENT_LENGTH_FIT, listener, buttonGroup, true));
        dispMenu.add(cghSizeMenu);
        JMenu widthMenu = new JMenu("Element Width");
        buttonGroup = new ButtonGroup();
        widthMenu.add(createJRadioButtonMenuItem("5", ActionManager.CGH_ELEMENT_WIDTH_5, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("10", ActionManager.CGH_ELEMENT_WIDTH_10, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("20", ActionManager.CGH_ELEMENT_WIDTH_20, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("50", ActionManager.CGH_ELEMENT_WIDTH_50, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("100", ActionManager.CGH_ELEMENT_WIDTH_100, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("Other", ActionManager.CGH_ELEMENT_WIDTH_OTHER, listener, buttonGroup));
        widthMenu.add(createJRadioButtonMenuItem("Fit", ActionManager.CGH_ELEMENT_WIDTH_FIT, listener, buttonGroup, true));
        dispMenu.add(widthMenu);
        dispMenu.addSeparator();
        JMenu displayTypeMenu = new JMenu("Display Type");
        buttonGroup = new ButtonGroup();
        displayTypeMenu.add(createJRadioButtonMenuItem("Mixed", ActionManager.CGH_DISPLAY_TYPE_COMBINED, listener, buttonGroup, true));
        displayTypeMenu.add(createJRadioButtonMenuItem("Separated", ActionManager.CGH_DISPLAY_TYPE_SEPARATED, listener, buttonGroup));
        dispMenu.add(displayTypeMenu);
        dispMenu.addSeparator();
        dispMenu.add(createJMenuItem("Display Order", ActionManager.CGH_DISPLAY_ORDER, listener));
        //cghDisplayMenu.add(createJMenuItem("Circle Viewer Background", CGHActionManager.CIRCLE_VIEWER_BACKGROUND, listener));
        dispMenu.add(createJCheckBoxMenuItem("Show Header", ActionManager.SHOW_HEADER, listener, true));
        dispMenu.addSeparator();
        dispMenu.add(createJMenuItem("Clear Annotations", ActionManager.CGH_CLEAR_ANNOTATIONS, listener));
        cghAnalysisMenu = new JMenu("CGH Analysis");
        addCghAnalysisMenu(cghAnalysisMenu, /*manager*/ actionManager);
        cghAnalysisMenu.addSeparator();
        cghAnalysisMenu.add(createJMenuItem("Find Gene", ActionManager.FIND_GENE, listener));
        /*
        cghAnalysisMenu.addSeparator();
        cghAnalysisMenu.add(createJMenuItem("Compare Experiments", ActionManager.COMPARE_EXPERIMENTS, listener));
        */
        add(cghAnalysisMenu);
        helpMenu = new JMenu("Help");
        helpMenu.add(createJMenuItem("Default Distances", ActionManager.DEFAULT_DISTANCES_CMD, listener));
        helpMenu.add(createJMenuItem("Support Tree Legend", ActionManager.SHOW_SUPPORTTREE_LEGEND_COMMAND, listener));
        add(helpMenu);
        setDefaultValues();
        //enableCloneDistributions(false);
        // End Raktim CGH Menus
    }
	/**
     * Adds CGH analysis menu items.
     */
    protected void addCghAnalysisMenu(JMenu menu, ActionManager manager) {
        int index = 0;
        Action action;
        while ((action = manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index)))!=null) {
            menu.add(createJMenuItem(action));
            index++;
        }
    }
    /**
     * Raktim
     * CGH SetDefaultValues()
     *
     */
    protected void setDefaultValues(){
        setMinRatioScale(-1f);
        setMaxRatioScale(1f);
    }
    /**
     * Raktim CGH
     * If CGH data is Ratio only, and/or Log2 transformed, Clone distribution
     * is not avialable. Enable/Diasble ClonesMenu based on those.
     */
    public void enableCloneDistributions(boolean CloneDistState, boolean log2state){
    	setEnableMenuItem("CloneValues", ActionManager.CLONE_VALUE_DISCRETE_DETERMINATION, !log2state);
        setEnableMenuItem("CloneValues", ActionManager.CLONE_VALUE_LOG_CLONE_DISTRIBUTION, CloneDistState);
        setEnableMenuItem("CloneValues", ActionManager.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION, CloneDistState);
        setEnableMenuItem("CloneValues", ActionManager.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION, CloneDistState);
        setEnableMenuItem("CloneValues", ActionManager.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION, CloneDistState);
        setEnableMenuItem("CloneValues", ActionManager.CLONE_P_THRESH, CloneDistState);
    }
    /**
     * Raktim CGH Dsiplay Menu
     * A class to maintain state for CGH Display Menu
     */
    private class CGHDisplayMenu implements ICGHDisplayMenu {
    	private boolean showFlankingRegions = false;
        private double unitLength = ICGHDisplayMenu.FIT_SIZE;
        private int elementWidth = (int)ICGHDisplayMenu.FIT_SIZE;
        private int displayType;
        private int displayLabelType = ICGHDisplayMenu.DISPLAY_WSL_ID;
        Color circleViewerBackgroundColor = Color.black;
        /** Getter for property showFlankingRegions.
         * @return Value of property showFlankingRegions.
         */
        public boolean isShowFlankingRegions() {
            return showFlankingRegions;
        }
        /** Setter for property showFlankingRegions.
         * @param showFlankingRegions New value of property showFlankingRegions.
         */
        public void setShowFlankingRegions(boolean showFlankingRegions) {
            this.showFlankingRegions = showFlankingRegions;
        }
        /** Getter for property unitLength.
         * @return Value of property unitLength.
         */
        public double getUnitLength() {
            return unitLength;
        }
        /** Setter for property unitLength.
         * @param unitLength New value of property unitLength.
         */
        public void setUnitLength(double unitLength) {
            if(unitLength == ICGHDisplayMenu.FIT_SIZE){
                this.unitLength = unitLength;
            }else{
                this.unitLength = unitLength / 1000000;
            }
        }
        /** Getter for property elementWidth.
         * @return Value of property elementWidth.
         */
        public int getElementWidth() {
            return elementWidth;
        }
        /** Setter for property elementWidth.
         * @param elementWidth New value of property elementWidth.
         */
        public void setElementWidth(int elementWidth) {
            this.elementWidth = elementWidth;
        }
        /** Getter for property displayType.
         * @return Value of property displayType.
         */
        public int getDisplayType() {
            return displayType;
        }
        /** Setter for property displayType.
         * @param displayType New value of property displayType.
         */
        public void setDisplayType(int displayType) {
            this.displayType = displayType;
        }
        /** Getter for property circleViewerBackgroundColor.
         * @return Value of property circleViewerBackgroundColor.
         */
        public Color getCircleViewerBackgroundColor() {
            return circleViewerBackgroundColor;
        }
        /** Setter for property circleViewerBackgroundColor.
         * @param circleViewerBackgroundColor New value of property circleViewerBackgroundColor.
         */
        public void setCircleViewerBackgroundColor(java.awt.Color circleViewerBackgroundColor) {
            this.circleViewerBackgroundColor = circleViewerBackgroundColor;
        }
        /** Getter for property displayLabel.
         * @return Value of property displayLabel.
         */
        public int getDisplayLabelType() {
            return displayLabelType;
        }
        /** Setter for property displayLabel.
         * @param displayLabel New value of property displayLabel.
         */
        public void setDisplayLabelType(int displayLabelType) {
            this.displayLabelType = displayLabelType;
        }
    }
    /**
     * Toggles the view of flanking regions
     * @param show
     */
    public void setShowFlankingRegions(boolean show){
        cghDisplayMenu.setShowFlankingRegions(show);
    }
    /**
     * @param elementLength
     */
    public void setCghElementLength(double elementLength){
        cghDisplayMenu.setUnitLength(elementLength);
    }
    /**
     * @param elementWidth
     */
    public void setCghElementWidth(int elementWidth){
        cghDisplayMenu.setElementWidth(elementWidth);
    }
    /**
     * @param displayType
     */
    public void setCghDisplayType(int displayType){
        cghDisplayMenu.setDisplayType(displayType);
    }
    /**
     * Getter for property cghDisplayMenu.
     * @return Value of property cghDisplayMenu.
     */
    public ICGHDisplayMenu getCghDisplayMenu() {
        return cghDisplayMenu;
    }
    /**
     * @param thresh
     */
    public void setAmpThresh(float thresh){
        cghCloneValueMenu.setAmpThresh(thresh);
    }
    /**
     * @param thresh
     */
    public void setDelThresh(float thresh){
        cghCloneValueMenu.setDelThresh(thresh);
    }
    /**
     * @param thresh
     */
    public void setAmpThresh2Copy(float thresh){
        cghCloneValueMenu.setAmpThresh2Copy(thresh);
    }
    /**
     * @param thresh
     */
    public void setDelThresh2Copy(float thresh){
        cghCloneValueMenu.setDelThresh2Copy(thresh);
    }
    public void setCircleViewerBackgroundColor(java.awt.Color circleViewerBackgroundColor) {
        cghDisplayMenu.circleViewerBackgroundColor = circleViewerBackgroundColor;
    }
    /**
     * @return
     */
    public ICGHCloneValueMenu getCloneValueMenu(){
        return cghCloneValueMenu;
    }
    /** sets the threshold to consider a probe as aberrant is probe
     * distributions are used as the copy determination type
     * @param clonePThresh
     */
    public void setClonePThresh(float clonePThresh){
        this.cghCloneValueMenu.setClonePThresh(clonePThresh);
    }
    /** Sets the method by which to calculate flanking regions
     * @param flankingRegionsType
     */
    public void setFlankingRegionType(int flankingRegionsType){
        this.cghCloneValueMenu.setFlankingRegionType(flankingRegionsType);
    }
    /**
     * @param cloneValueType  */
    public void setCloneValueType(int cloneValueType){
        this.cghCloneValueMenu.setCloneValueType(cloneValueType);
    }
    public void setCghDisplayLabelType(int displayLabelType){
        this.cghDisplayMenu.setDisplayLabelType(displayLabelType);
    }
    /**
     * Raktim CGH CGH Clones Value Menu Init
     * Oct 3, 2005
     * Make separate methods in case we want to give different
     * options for determining clone values under different circumstances
     */
    public void initCloneValuesMenu(/*JMenu cloneValuesMenu, ActionListener listener,*/ boolean hasDyeSwap /*, int cloneValueType, int flankingRegionType*/){
        //cloneValuesMenu.removeAll();
    	JMenu cloneMenu = getJMenu("CloneValues");
    	cloneMenu.removeAll();
    	ICGHCloneValueMenu cloneValuesMenu =  getCloneValueMenu();
    	setCloneValueType(TMEV.defaultCloneValue);
    	setFlankingRegionType(TMEV.defaultFlankingRegionValue);
        if(hasDyeSwap){
            initCloneValuesMenuDyeSwap(cloneMenu, listener, cloneValuesMenu.getCloneValueType(), cloneValuesMenu.getFlankingRegionType());
        }else{
            initCloneValuesMenuNoDyeSwap(cloneMenu, listener, cloneValuesMenu.getCloneValueType(), cloneValuesMenu.getFlankingRegionType());
        }
    }
    public void initCloneValuesMenu(JMenu cloneValuesMenu, ActionListener listener, boolean hasDyeSwap, int cloneValueType, int flankingRegionType){
        cloneValuesMenu.removeAll();
    	if(hasDyeSwap){
            initCloneValuesMenuDyeSwap(cloneValuesMenu, listener, cloneValueType, flankingRegionType);
        }else{
            initCloneValuesMenuNoDyeSwap(cloneValuesMenu, listener, cloneValueType, flankingRegionType);
        }
    }
    /**
     * boolean isRatioOnly, boolean log2RatioOnly, boolean cloneDist
     */
    public void initCloneValuesMenuDyeSwap(JMenu cloneValuesMenu, ActionListener listener, int cloneValueType, int flankingRegionType){
        ButtonGroup buttonGroup = new ButtonGroup();
        cloneValuesMenu.add(createJRadioButtonMenuItem("Clone Ratio Threshold", ActionManager.CLONE_VALUE_DISCRETE_DETERMINATION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Log Invert Average", ActionManager.CLONE_VALUE_LOG_AVERAGE_INVERTED, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_AVERAGE_INVERTED));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Log Clone Distribution", ActionManager.CLONE_VALUE_LOG_CLONE_DISTRIBUTION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_CLONE_DISTRIBUTION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Ratio Threshold or Clone Distribution", ActionManager.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION));
        cloneValuesMenu.addSeparator();
        buttonGroup = new ButtonGroup();
        cloneValuesMenu.add(createJRadioButtonMenuItem("Flanking Regions By Threshold", ActionManager.FLANKING_REGIONS_BY_THRESHOLD, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Flanking Regions By Clone Distribution", ActionManager.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Threshold or Clone Distribution", ActionManager.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION));
        cloneValuesMenu.addSeparator();
        cloneValuesMenu.add(createJMenuItem("Clone P Threshold", ActionManager.CLONE_P_THRESH, listener));
        cloneValuesMenu.add(createJMenuItem("Set Thresholds", ActionManager.CGH_SET_THRESHOLDS, listener));
    }
    public void initCloneValuesMenuNoDyeSwap(JMenu cloneValuesMenu, ActionListener listener, int cloneValueType, int flankingRegionType){
        ButtonGroup buttonGroup = new ButtonGroup();
        cloneValuesMenu.add(createJRadioButtonMenuItem("Clone Ratio Threshold", ActionManager.CLONE_VALUE_DISCRETE_DETERMINATION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Log Value", ActionManager.CLONE_VALUE_LOG_AVERAGE_INVERTED, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_AVERAGE_INVERTED));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Log Clone Distribution", ActionManager.CLONE_VALUE_LOG_CLONE_DISTRIBUTION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_CLONE_DISTRIBUTION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Ratio Threshold or Clone Distribution", ActionManager.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION, listener, buttonGroup, cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION));
        cloneValuesMenu.addSeparator();
        buttonGroup = new ButtonGroup();
        cloneValuesMenu.add(createJRadioButtonMenuItem("Flanking Regions By Threshold", ActionManager.FLANKING_REGIONS_BY_THRESHOLD, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Flanking Regions By Clone Distribution", ActionManager.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION));
        cloneValuesMenu.add(createJRadioButtonMenuItem("Threshold or Clone Distribution", ActionManager.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION, listener, buttonGroup, flankingRegionType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION));
        cloneValuesMenu.addSeparator();
        cloneValuesMenu.add(createJMenuItem("Clone P Threshold", ActionManager.CLONE_P_THRESH, listener));
        cloneValuesMenu.add(createJMenuItem("Set Thresholds", ActionManager.CGH_SET_THRESHOLDS, listener));
    }
    /**
     * Raktim CGH Dsiplay Menu
     * A class to maintain state for CGH Clones Menu
     */
    protected class CGHCloneValueMenu implements ICGHCloneValueMenu {
        //private int cloneValueType = ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION;
        //private int flankingRegionType = ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD;
        /**
         * The following 2 statements needs to be revisited
         */
        private int cloneValueType = TMEV.defaultCloneValue;
        private int flankingRegionType = TMEV.defaultFlankingRegionValue;
        /**
         * Old thresold defaults based on unlogged ratio
        private float ampThresh = 1.2f;
        private float delThresh = .8f;
        private float ampThresh2Copy = 2.0f;
        private float delThresh2Copy = .3f;
        */
        /**
         * New CGH threshold based on log2 ratio cuttoffs
         * Raktim Nov 21, 2005
         */
        private float ampThresh = 0.8f;
        private float delThresh = -0.8f;
        private float ampThresh2Copy = 1.0f;
        private float delThresh2Copy = -1.0f;
        private float clonePThresh = .85f;
        public CGHCloneValueMenu(){  }
        /**
         * @return  */
        public int getCloneValueType() {
            return this.cloneValueType;
        }
        /**
         * @param cloneValueType  */
        public void setCloneValueType(int cloneValueType) {
            this.cloneValueType = cloneValueType;
        }
        /**
         * @return  */
        public float getClonePThresh() {
            return this.clonePThresh;
        }
        /**
         * @param clonePThresh  */
        public void setClonePThresh(float clonePThresh){
            this.clonePThresh = clonePThresh;
        }
        /** Getter for property ampThresh.
         * @return Value of property ampThresh.
         */
        public float getAmpThresh() {
            return ampThresh;
        }
        /** Setter for property ampThresh.
         * @param ampThresh New value of property ampThresh.
         */
        public void setAmpThresh(float ampThresh) {
            this.ampThresh = ampThresh;
        }
        /** Getter for property delThresh.
         * @return Value of property delThresh.
         */
        public float getDelThresh() {
            return delThresh;
        }
        /** Setter for property delThresh.
         * @param delThresh New value of property delThresh.
         */
        public void setDelThresh(float delThresh) {
            this.delThresh = delThresh;
        }
        /** Getter for property ampThresh2Copy.
         * @return Value of property ampThresh2Copy.
         */
        public float getAmpThresh2Copy() {
            return ampThresh2Copy;
        }
        /** Setter for property ampThresh2Copy.
         * @param ampThresh2Copy New value of property ampThresh2Copy.
         */
        public void setAmpThresh2Copy(float ampThresh2Copy) {
            this.ampThresh2Copy = ampThresh2Copy;
        }
        /** Getter for property delThresh2Copy.
         * @return Value of property delThresh2Copy.
         */
        public float getDelThresh2Copy() {
            return delThresh2Copy;
        }
        /** Setter for property delThresh2Copy.
         * @param delThresh2Copy New value of property delThresh2Copy.
         */
        public void setDelThresh2Copy(float delThresh2Copy) {
            this.delThresh2Copy = delThresh2Copy;
        }
        /** Getter for property flankingRegionType.
         * @return Value of property flankingRegionType.
         */
        public int getFlankingRegionType() {
            return flankingRegionType;
        }
        /** Setter for property flankingRegionType.
         * @param flankingRegionType New value of property flankingRegionType.
         */
        public void setFlankingRegionType(int flankingRegionType) {
            this.flankingRegionType = flankingRegionType;
        }
    }
}
