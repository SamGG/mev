/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ActionManager.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.action;

import java.net.URL;
import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.tigr.microarray.mev.cluster.gui.IGUIFactory;
import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;

public class ActionManager {
    
    public static final String PARAMETER = "command-parameter";
    public static final String LARGE_ICON = "LargeIcon";
    
    private HashMap actions = new HashMap();
    private ActionListener listener;
    
    /**
     * Constructs an <code>ActionManager</code> with specified
     * action listener, array of labels and gui factory.
     */
    public ActionManager(ActionListener listener, String[] labels, IGUIFactory factory) {
	this.listener = listener;
	initActions();
	initLabelActions(labels);
	initSortActions(labels);
	initAnalysisActions(factory);
    }
    
    /**
     * Returns a wrapped action listaner.
     */
    public ActionListener getListener() {
	return listener;
    }
    
    /**
     * Rturns an action by its name.
     */
    public Action getAction(String name) {
	return(Action)actions.get(name);
    }
    
    /**
     * Delegates this invokation to a wrapped action listener.
     */
    public void forwardAction(ActionEvent event) {
	listener.actionPerformed(event);
    }
    
    /**
     * Initializes main menu and toolbar actions.
     */
    private void initActions() {
        actions.put(LOAD_ACTION, new DefaultAction(this, LOAD_NAME, LOAD_COMMAND, getIcon(LOAD_FILE_SMALLICON), getIcon(LOAD_FILE_LARGEICON)));

	actions.put(LOAD_DIRECTORY_ACTION, new DefaultAction(this, LOAD_DIRECTORY_NAME, LOAD_DIRECTORY_COMMAND, getIcon(LOAD_DIRECTORY_SMALLICON), getIcon(LOAD_DIRECTORY_LARGEICON)));
	actions.put(LOAD_FILE_ACTION, new DefaultAction(this, LOAD_FILE_NAME, LOAD_FILE_COMMAND, getIcon(LOAD_FILE_SMALLICON), getIcon(LOAD_FILE_LARGEICON)));
	actions.put(LOAD_EXPRESSION_ACTION, new DefaultAction(this, LOAD_EXPRESSION_NAME, LOAD_EXPRESSION_COMMAND, getIcon(LOAD_EXPRESSION_SMALLICON), getIcon(LOAD_EXPRESSION_LARGEICON)));
	actions.put(LOAD_DB_ACTION, new DefaultAction(this, LOAD_DB_NAME, LOAD_DB_COMMAND, getIcon(LOAD_DB_SMALLICON), getIcon(LOAD_DB_LARGEICON)));
	actions.put(LOAD_STANFORD_ACTION, new DefaultAction(this, LOAD_STANFORD_NAME, LOAD_STANFORD_COMMAND, getIcon(LOAD_STANFORD_ICON)));
	actions.put(LOAD_CLUSTER_ACTION, new DefaultAction(this, LOAD_CLUSTER_NAME, LOAD_CLUSTER_COMMAND, getIcon(LOAD_CLUSTER_ICON)));
	actions.put(SAVE_MATRIX_ACTION, new DefaultAction(this, SAVE_MATRIX_NAME, SAVE_MATRIX_COMMAND, getIcon(SAVE_MATRIX_ICON)));
	actions.put(SAVE_IMAGE_ACTION, new DefaultAction(this, SAVE_IMAGE_NAME, SAVE_IMAGE_COMMAND, getIcon(SAVE_IMAGE_SMALLICON), getIcon(SAVE_IMAGE_LARGEICON)));
	actions.put(PRINT_IMAGE_ACTION, new DefaultAction(this, PRINT_IMAGE_NAME, PRINT_IMAGE_COMMAND, getIcon(PRINT_IMAGE_SMALLICON), getIcon(PRINT_IMAGE_LARGEICON)));
	actions.put(CLOSE_ACTION, new DefaultAction(this, CLOSE_NAME, CLOSE_COMMAND, getIcon(CLOSE_ICON)));
	actions.put(SHOW_THUMBNAIL_ACTION, new DefaultAction(this, SHOW_THUMBNAIL_NAME, SHOW_THUMBNAIL_COMMAND, getIcon(SHOW_THUMBNAIL_SMALLICON), getIcon(SHOW_THUMBNAIL_LARGEICON)));
	actions.put(DELETE_ALL_ACTION, new DefaultAction(this, DELETE_ALL_NAME, DELETE_ALL_COMMAND, getIcon(DELETE_ALL_ICON)));
	actions.put(DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION, new DefaultAction(this, DELETE_ALL_EXPERIMENT_CLUSTERS_NAME, DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND, getIcon(DELETE_ALL_ICON)));
    }
    
    /**
     * Initializes 'display/label' menu actions.
     */
    public void initLabelActions(String[] labels) {
	DefaultAction action = new DefaultAction(this, "No Label", DISPLAY_LABEL_CMD);
	action.putValue(PARAMETER, String.valueOf(-1));
	actions.put(DISPLAY_LABEL_ACTION+String.valueOf(-1), action);

        if(labels == null)
            return;
        
	for (int i=0; i<labels.length; i++) {
	    action = new DefaultAction(this, "Label by "+labels[i], DISPLAY_LABEL_CMD);
	    action.putValue(PARAMETER, String.valueOf(i));
	    actions.put(DISPLAY_LABEL_ACTION+String.valueOf(i), action);
	}
    }
    
    /**
     * Initializes analysis actions, using specified <code>IGUIFactory</code>.
     * @see IGUIFactory
     */
    private void initAnalysisActions(IGUIFactory factory) {
	if (factory == null) {
	    return;
	}
	AnalysisDescription[] descs = factory.getAnalysisDescriptions();
	if (descs == null) {
	    return;
	}
	int counter = 0;
	for (int i=0; i<descs.length; i++) {
	    if (isValidDescription(descs[i])) {
		actions.put(ANALYSIS_ACTION+String.valueOf(i), new AnalysisAction(this, descs[i]));
		counter++;
	    }
	}
    }
    
    /**
     * Checkes if specified <code>AnalysisDescription</code> is valid.
     */
    private boolean isValidDescription(AnalysisDescription desc) {
	return desc != null && desc.getName() != null && desc.getClassName() != null;
    }
    
    /**
     * Initializes sorting actions.
     */
    private void initSortActions(String[] labels) {
	DefaultAction action;
        if(labels == null)
            return;
	for (int i=0; i<labels.length; i++) {
	    action = new DefaultAction(this, "Sort by "+labels[i], SORT_LABEL_CMD);
	    action.putValue(PARAMETER, String.valueOf(i));
	    actions.put(SORT_LABEL_ACTION+String.valueOf(i), action);
	}
    }
    
    /**
     * Creates an image by specified name.
     */
    private ImageIcon getIcon(String name) {
	URL url = getClass().getResource("/org/tigr/images/"+name);
	if (url == null)
	    return null;
	return new ImageIcon(url);
    }
    
    // analysis action(s)
    public static final String ANALYSIS_ACTION = "analysis-action";
    public static final String ANALYSIS_COMMAND = "analysis-command";
    
    //load data action
    public static final String  LOAD_ACTION  = "action-load";
    public static final String  LOAD_COMMAND = "command-load";
    public static final String  LOAD_NAME    = "Load Data";
    private static final String LOAD_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_LARGEICON = "addmultiple.gif";
    
    // load directory action
    public static final String  LOAD_DIRECTORY_ACTION  = "action-load-directory";
    public static final String  LOAD_DIRECTORY_COMMAND = "command-load-directory";
    public static final String  LOAD_DIRECTORY_NAME    = "Add Experiments from Directory";
    private static final String LOAD_DIRECTORY_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_DIRECTORY_LARGEICON = "addmultiple.gif";
    // load file action
    public static final String  LOAD_FILE_ACTION  = "action-load-file";
    public static final String  LOAD_FILE_COMMAND = "command-load-file";
    public static final String  LOAD_FILE_NAME    = "Add Experiment from File";
    private static final String LOAD_FILE_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_FILE_LARGEICON    = "addfromfile.gif";
    
    // pcahan
    
    // load affy directory action
    public static final String  LOAD_AFFY_DIRECTORY_ACTION  = "action-affy-load-directory";
    public static final String  LOAD_AFFY_DIRECTORY_COMMAND = "command-affy-load-directory";
    public static final String  LOAD_AFFY_DIRECTORY_NAME    = "Add Affymetrix Chips from Directory";
    private static final String LOAD_AFFY_DIRECTORY_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_AFFY_DIRECTORY_LARGEICON = "addmultiple.gif";
    
        // load Affy action
    public static final String  LOAD_AFFY_ACTION  = "action-load-affy";
    public static final String  LOAD_AFFY_COMMAND = "command-load-affy";
    public static final String  LOAD_AFFY_NAME    = "Add Experiment from Affymetrix datafile";
    private static final String LOAD_AFFY_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_AFFY_LARGEICON    = "addfromfile.gif";
    
    
    // load expression action
    public static final String  LOAD_EXPRESSION_ACTION  = "action-load-expression";
    public static final String  LOAD_EXPRESSION_COMMAND = "command-load-expression";
    public static final String  LOAD_EXPRESSION_NAME    = "Add Experiment from Expression File";
    private static final String LOAD_EXPRESSION_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_EXPRESSION_LARGEICON    = "addfromfile.gif";
    // load db action
    public static final String  LOAD_DB_ACTION  = "action-load-db";
    public static final String  LOAD_DB_COMMAND = "command-load-db";
    public static final String  LOAD_DB_NAME    = "Add Experiment from DB";
    private static final String LOAD_DB_SMALLICON    = "addfromdb16.gif";
    private static final String LOAD_DB_LARGEICON    = "addfromdb.gif";
    // load stanford action
    public static final String  LOAD_STANFORD_ACTION  = "action-load-stanford";
    public static final String  LOAD_STANFORD_COMMAND = "command-load-stanford";
    private static final String LOAD_STANFORD_NAME    = "Add Experiments from Stanford File";
    private static final String LOAD_STANFORD_ICON    = "addmultiple16.gif";
    // load cluster action
    public static final String  LOAD_CLUSTER_ACTION  = "action-load-cluster";
    public static final String  LOAD_CLUSTER_COMMAND = "command-load-cluster";
    private static final String LOAD_CLUSTER_NAME    = "Load Cluster";
    private static final String LOAD_CLUSTER_ICON    = "addfromfile16.gif";
    // save matrix action
    public static final String  SAVE_MATRIX_ACTION  = "action-save-matrix";
    public static final String  SAVE_MATRIX_COMMAND = "command-save-matrix";
    private static final String SAVE_MATRIX_NAME    = "Save Matrix";
    private static final String SAVE_MATRIX_ICON    = "expression.gif";
    // save image action
    public static final String  SAVE_IMAGE_ACTION  = "action-save-image";
    public static final String  SAVE_IMAGE_COMMAND = "command-save-image";
    public static final String  SAVE_IMAGE_NAME    = "Save Image";
    private static final String SAVE_IMAGE_SMALLICON    = "saveimage16.gif";
    private static final String SAVE_IMAGE_LARGEICON    = "saveimage.gif";
    // print image action
    public static final String  PRINT_IMAGE_ACTION  = "action-print-image";
    public static final String  PRINT_IMAGE_COMMAND = "command-print-image";
    private static final String PRINT_IMAGE_NAME    = "Print Image";
    private static final String PRINT_IMAGE_SMALLICON = "printimage16.gif";
    private static final String PRINT_IMAGE_LARGEICON = "printimage.gif";
    // close action
    public static final String  CLOSE_ACTION  = "action-close";
    public static final String  CLOSE_COMMAND = "command-close";
    private static final String CLOSE_NAME    = "Close";
    private static final String CLOSE_ICON    = "close16.gif";
    // show thumbnail action
    public static final String SHOW_THUMBNAIL_ACTION  = "show-thumbnail-action";
    public static final String SHOW_THUMBNAIL_COMMAND = "show-thumbnail-cmd";
    private static final String SHOW_THUMBNAIL_NAME   = "Show Thumbnail";
    private static final String SHOW_THUMBNAIL_SMALLICON = "thumbnail16.gif";
    private static final String SHOW_THUMBNAIL_LARGEICON = "thumbnail.gif";
    // delete all gene clusters action
    public static final String  DELETE_ALL_ACTION  = "delete-all-action";
    public static final String  DELETE_ALL_COMMAND = "delete-all-cmd";
    private static final String DELETE_ALL_NAME    = "Delete All Gene Clusters";
    private static final String DELETE_ALL_ICON    = "Delete16.gif";    
    // delete all experiment clusters action
    public static final String  DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION  = "delete-all-experiments-action";
    public static final String  DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND = "delete-all-experiments-cmd";
    private static final String DELETE_ALL_EXPERIMENT_CLUSTERS_NAME    = "Delete All Experiment Clusters";
    // abbr. expt. names toggle
    public static final String TOGGLE_ABBR_EXPT_NAMES_ACTION = "toggle-abbr-expt-names-action";
    public static final String TOGGLE_ABBR_EXPT_NAMES_CMD = "toggle-abbr-expt-names-cmd";
    public static final String TOGGLE_ABBR_EXPT_NAMES_NAME = "Abbr. Experiment Names";    
    // display label actions
    public static final String DISPLAY_LABEL_ACTION = "display--label-action";
    public static final String DISPLAY_LABEL_CMD    = "display-label-cmd";
    // sort label actions
    public static final String SORT_LABEL_ACTION = "sort-label-action";
    public static final String SORT_LABEL_CMD    = "sort-label-cmd";
    
        // pcahan 
    public static final String SET_DETECTION_FILTER_CMD = "set-detection-filter-cmd";
    public static final String USE_DETECTION_FILTER_CMD = "use-detection-filter-cmd";

    public static final String SET_FOLD_FILTER_CMD = "set-fold-filter-cmd";
    public static final String USE_FOLD_FILTER_CMD = "use-fold-filter-cmd";
    
    // adjust data commands
    public static final String LOG2_TRANSFORM_CMD  = "log2-transform-cmd";
    public static final String NORMALIZE_SPOTS_CMD = "normalize-spots-cmd";
    public static final String DIVIDE_SPOTS_RMS_CMD = "divide-spots-rms-cmd";
    public static final String DIVIDE_SPOTS_SD_CMD = "divide-spots-sd-cmd";
    public static final String MEAN_CENTER_SPOTS_CMD = "mean-center-spots-cmd";
    public static final String MEDIAN_CENTER_SPOTS_CMD = "median-center-spots-cmd";
    public static final String DIGITAL_SPOTS_CMD = "digital-spots-cmd";
    public static final String NORMALIZE_EXPERIMENTS_CMD = "normalize-experiments-cmd";
    public static final String DIVIDE_EXPERIMENTS_RMS_CMD = "divide-experiments-rms-cmd";
    public static final String DIVIDE_EXPERIMENTS_SD_CMD = "divide-experiments-sd-cmd";
    public static final String MEAN_CENTER_EXPERIMENTS_CMD = "mean-center-experiments-cmd";
    public static final String MEDIAN_CENTER_EXPERIMENTS_CMD = "median-center-experiments-cmd";
    public static final String DIGITAL_EXPERIMENTS_CMD = "digital-experiments-cmd";
    public static final String LOG10_TO_LOG2_CMD = "log10-to-log2-cmd";
    public static final String SET_LOWER_CUTOFFS_CMD = "set-lower-cutoffs-cmd";
    public static final String USE_LOWER_CUTOFFS_CMD = "use-lower-cutoffs-cmd";
    public static final String SET_PERCENTAGE_CUTOFFS_CMD = "set-percentage-cutoffs-cmd";
    public static final String USE_PERCENTAGE_CUTOFFS_CMD = "use-percentage-cutoffs-cmd";
    public static final String ADJUST_INTENSITIES_0_CMD = "adjust-intensities-0-cmd";
    // normalization commands
    public static final String TOTAL_INTENSITY_CMD        = "total-intensity-cmd";
    public static final String LINEAR_REGRESSION_CMD      = "linear-regression-cmd";
    public static final String RATIO_STATISTICS_CMD       = "ratio-statistics-cmd";
    public static final String ITERATIVE_LOG_CMD          = "iterative-log-cmd";
    public static final String TOTAL_INTENSITY_LIST_CMD   = "total-intensity-list-cmd";
    public static final String LINEAR_REGRESSION_LIST_CMD = "linear-regression-list-cmd";
    public static final String RATIO_STATISTICS_LIST_CMD  = "ratio-statistics-list-cmd";
    public static final String ITERATIVE_LOG_LIST_CMD     = "iterative-log-list-cmd";
    public static final String NO_NORMALIZATION_CMD       = "no-normalization-cmd";
    // distance commands
    public static final String DEFAULT_DISTANCE_CMD = "default-distance-cmd";
    public static final String PEARSON_CORRELATION_CMD = "pearson-correlation-cmd";
    public static final String PEARSON_UNCENTERED_CMD = "pearson-uncentered-cmd";
    public static final String PEARSON_SQUARED_CMD = "pearson-squared-cmd";
    public static final String COSINE_CORRELATION_CMD = "cosine-correlation-cmd";
    public static final String COVARIANCE_VALUE_CMD = "covariance-value-cmd";
    public static final String EUCLIDEAN_DISTANCE_CMD = "euclidean-distance-cmd";
    public static final String AVERAGE_DOT_PRODUCT_CMD = "average-dot-product-cmd";
    public static final String MANHATTAN_DISTANCE_CMD = "manhattan-distance-cmd";
    public static final String MUTUAL_INFORMATION_CMD = "mutual-information-cmd";
    public static final String SPEARMAN_RANK_CORRELATION_CMD = "spearman-rank-correlation-cmd";
    public static final String KENDALLS_TAU_CMD = "kendalls-tau-cmd";
    public static final String ABSOLUTE_DISTANCE_CMD = "absolute-distance-cmd";
    // display commands    
    public static final String GREEN_RED_COLOR_SCHEME_CMD = "display-green-red-scheme-cmd";
    public static final String BLUE_YELLOW_COLOR_SCHEME_CMD = "display-blue-yellow-scheme-cmd";    
    public static final String CUSTOM_COLOR_SCHEME_CMD = "display-custom-color-scheme-cmd";   
    public static final String COLOR_GRADIENT_CMD = "display-color-gradient-cmd";
    public static final String DISPLAY_GREEN_RED_CMD = "display-green-red-cmd";
    public static final String DISPLAY_GR_RATIO_SPLIT_CMD = "display-gr-ratio-split-cmd";
    public static final String DISPLAY_GR_OVERLAY_CMD = "display-gr-overlay-cmd";
    public static final String DISPLAY_GR_SCALE_CMD = "display-gr-dcale-cmd";
    public static final String DISPLAY_DRAW_BORDERS_CMD = "display-draw-borders-cmd";
    public static final String DISPLAY_TRACING_CMD = "display-tracing-cmd";
    public static final String DISPLAY_USE_ANTIALIASING_CMD = "display-use-antialiasing-cmd";
    public static final String DISPLAY_SET_UPPER_LIMITS_CMD = "display-set-upper-limits-cmd";
    public static final String DISPLAY_SET_RATIO_SCALE_CMD = "display-set-ratio-scale-cmd";
    public static final String DISPLAY_5X2_CMD = "display-5x2-cmd";
    public static final String DISPLAY_10X10_CMD = "display-10x10-cmd";
    public static final String DISPLAY_20X5_CMD = "display-20x5-cmd";
    public static final String DISPLAY_50X10_CMD = "display-50x10-cmd";
    public static final String DISPLAY_OTHER_CMD = "display-other-cmd";
    public static final String SORT_BY_LOCATION_CMD = "sort-by-location-cmd";
    public static final String SORT_BY_RATIO_CMD = "sort-by-ratio-cmd";
    public static final String SYSTEM_INFO_CMD = "system-info-cmd";
    public static final String DEFAULT_DISTANCES_CMD = "default-distances-cmd";
    // popup commands
    public static final String DELETE_NODE_CMD = "delete-node-cmd";
    // help commands
    public static final String SHOW_SUPPORTTREE_LEGEND_COMMAND = "show-supporttree-legend-command";
}
