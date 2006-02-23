/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: MultipleArrayViewer.java,v $
 * $Revision: 1.32 $
 * $Date: 2006-02-23 21:19:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.media.jai.JAI;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.tigr.microarray.file.AnnFileParser;
import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations.GeneAmplifications;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations.GeneDeletions;
import org.tigr.microarray.mev.cgh.CGHDataGenerator.FlankingRegionCalculator;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHAnnotationsModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHChartDataModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHChartDataModelDyeSwap;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHChartDataModelNoDyeSwap;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHCircleViewerModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHPositionGraphDataModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHTableDataModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHTableDataModelDyeSwap;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHTableDataModelNoDyeSwap;
import org.tigr.microarray.mev.cgh.CGHDataModel.CytoBandsModel;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHDataRegionInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.CytoBands;
import org.tigr.microarray.mev.cgh.CGHDataObj.DataRegionGeneData;
import org.tigr.microarray.mev.cgh.CGHDataObj.GeneDataSet;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHDataValuesDisplay;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHDisplayOrderChanger;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHThresholdSetter;
import org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers.NumberOfAlterationsViewer;
import org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers.NumberOfDeletionsAmpilficationsDataModel;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHBrowser.CGHBrowser;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHCircleViewer.CGHCircleViewerPanel;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph.CGHPositionGraphCombinedHeader;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph.CGHPositionGraphViewer;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.GuiUtil;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.SingleValueSelectorDialog;
import org.tigr.microarray.mev.cgh.CGHListenerObj.ICGHListener;
import org.tigr.microarray.mev.cgh.CGHListenerObj.IDataRegionSelectionListener;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterAttributesDialog;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterTable;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHViewer;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.TextViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HTMLMessageFileChooser;
import org.tigr.microarray.mev.file.AnnFileFilter;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.microarray.mev.r.Rama;
import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.util.awt.ColorSchemeSelectionDialog;
import org.tigr.microarray.util.awt.SetElementSizeDialog;
import org.tigr.microarray.util.awt.SetSlideFilenameDialog;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;
import org.tigr.util.swing.BMPFileFilter;
import org.tigr.util.swing.ImageFileFilter;
import org.tigr.util.swing.JPGFileFilter;
import org.tigr.util.swing.PNGFileFilter;
import org.tigr.util.swing.TIFFFileFilter;

import com.sun.media.jai.codec.ImageEncodeParam;
public class MultipleArrayViewer extends ArrayViewer implements Printable {
    public static final long serialVersionUID = 100010201010001L;
    
    private MultipleArrayMenubar menubar;
    private MultipleArrayToolbar toolbar;
    private JSplitPane splitPane;
    private JScrollPane viewScrollPane;
    private JLabel statusLabel;
    // the tree and special nodes and scroll pane
    private JScrollPane treeScrollPane;
    private ResultTree tree;
    private DefaultMutableTreeNode mainViewerNode;
    private DefaultMutableTreeNode clusterNode;
    private DefaultMutableTreeNode analysisNode;
    private DefaultMutableTreeNode scriptNode;
    private DefaultMutableTreeNode historyNode;
    // current viewer
    private IViewer viewer;
    // callback reference
    private IFramework framework = new FrameworkImpl();
    // features data
    private MultipleArrayData data = new MultipleArrayData();
    private float sortedValues[];
    //Action Manager
    private ActionManager manager;
    
    private int resultCount = 1;
    
    private ClusterRepository geneClusterRepository;
    private ClusterRepository experimentClusterRepository;
    private ClusterTable geneClusterManager;
    private ClusterTable experimentClusterManager;
    private ScriptManager scriptManager;
    private HistoryViewer historyLog;
    
    private File currentAnalysisFile;
    private boolean modifiedResult = false;    /* Raktim, CGH Model for Cytoband */
    private CytoBandsModel cytoBandsModel;
    
    /**
     * Construct a <code>MultipleArrayViewer</code> with default title,
     * creates menu and tool bars from new instance of action manager,
     * creates the navigation tree and the scroll pane to be used to display
     * a calculation result, creates a status bar.
     */
    public MultipleArrayViewer() {
        super(new JFrame("TIGR Multiple Array Viewer"));
        
        // listener
        EventListener eventListener = new EventListener();
        mainframe.addWindowListener(eventListener);
        manager = new ActionManager(eventListener, TMEV.getFieldNames(), TMEV.getGUIFactory());
        
        menubar = new MultipleArrayMenubar(manager);
              
        mainframe.setJMenuBar(menubar);
        
        toolbar = new MultipleArrayToolbar(manager);
        mainframe.getContentPane().add(toolbar, BorderLayout.NORTH);
        
        viewScrollPane = createViewScrollPane(eventListener);
        viewScrollPane.setBackground(Color.white);
        
        treeScrollPane = createTreeScrollPane(eventListener);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        splitPane.setOneTouchExpandable(true);
        mainframe.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel("TIGR MultiExperiment Viewer");
        mainframe.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        mainframe.pack();
        splitPane.setDividerLocation(.2);
        
        systemDisable(TMEV.DB_AVAILABLE);
        systemDisable(TMEV.DATA_AVAILABLE);
    }
    
    
    /**
     * Construct a <code>MultipleArrayViewer</code> with default title,
     * creates menu and tool bars from new instance of action manager,
     * creates the navigation tree and the scroll pane to be used to display
     * a calculation result, creates a status bar.
     */
    public MultipleArrayViewer(MultipleArrayData arrayData) {
        super(new JFrame("TIGR Multiple Array Viewer"));
        
        // listener
        EventListener eventListener = new EventListener();
        mainframe.addWindowListener(eventListener);
        manager = new ActionManager(eventListener, arrayData.getFieldNames(), TMEV.getGUIFactory());
        
        data = arrayData;
        
        menubar = new MultipleArrayMenubar(manager);
        
        //have new session but need to build field names into menus
        menubar.addLabelMenuItems(arrayData.getFieldNames());
       
        //need to populate the experiment label menu items
        menubar.addExperimentLabelMenuItems(arrayData.getSlideNameKeyVectorUnion());
              
        mainframe.setJMenuBar(menubar);
        
        toolbar = new MultipleArrayToolbar(manager);
        mainframe.getContentPane().add(toolbar, BorderLayout.NORTH);
        
        viewScrollPane = createViewScrollPane(eventListener);
        viewScrollPane.setBackground(Color.white);
        
        treeScrollPane = createTreeScrollPane(eventListener);
        
        //have the main scroll pane
        ((MultipleArrayCanvas)this.viewer).addSortMenuItems(arrayData.getFieldNames());
         
        //Add the time stamp node
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
        
        setNormalizedState(arrayData.getNormalizationState());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        splitPane.setOneTouchExpandable(true);
        mainframe.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel("TIGR MultiExperiment Viewer");
        mainframe.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        mainframe.pack();
        splitPane.setDividerLocation(.2);

        if (data.getDataType() == IData.DATA_TYPE_RATIO_ONLY || data.getDataType() == IData.DATA_TYPE_AFFY_ABS){
            this.menubar.enableNormalizationMenu(false);
        }        
        
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
        
        //systemDisable(TMEV.DB_AVAILABLE);
        //systemDisable(TMEV.DATA_AVAILABLE);
    }
    
    /**
     * Construct a <code>MultipleArrayViewer</code> with default title,
     * creates menu and tool bars from new instance of action manager,
     * creates the navigation tree and the scroll pane to be used to display
     * a calculation result, creates a status bar.  Uses passed MultipleArrayMenubar
     * to dictate initial settings.
     */
    public MultipleArrayViewer(MultipleArrayData arrayData, MultipleArrayMenubar origMenubar) {
        super(new JFrame("TIGR Multiple Array Viewer"));
        
        // listener
        EventListener eventListener = new EventListener();
        mainframe.addWindowListener(eventListener);
        manager = new ActionManager(eventListener, arrayData.getFieldNames(), TMEV.getGUIFactory());
        
        data = arrayData;
        
        menubar = new MultipleArrayMenubar(origMenubar, manager);
        
        //have new session but need to build field names into menus
        menubar.addLabelMenuItems(arrayData.getFieldNames());       
        
        menubar.synchronizeSettings(origMenubar);
        
        //need to populate the experiment label menu items
        menubar.addExperimentLabelMenuItems(arrayData.getSlideNameKeyVectorUnion());
       
        mainframe.setJMenuBar(menubar);
        
        toolbar = new MultipleArrayToolbar(manager);
        mainframe.getContentPane().add(toolbar, BorderLayout.NORTH);
        
        viewScrollPane = createViewScrollPane(eventListener);
        viewScrollPane.setBackground(Color.white);
        
        treeScrollPane = createTreeScrollPane(eventListener);

        //have the main scroll pane
        ((MultipleArrayCanvas)this.viewer).addSortMenuItems(arrayData.getFieldNames());
          
        //Add the time stamp node
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
        
        //set IData as primary and selected
        data.setUseMainData(true);
        ((LeafInfo)(mainViewerNode.getUserObject())).setSelectedDataSource(true);
        //record main data as source      
        createDataSelectionNode((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0)), data.getExperiment(), data.getExperiment().getNumberOfGenes(), Cluster.GENE_CLUSTER);              
        tree.repaint();
        
        
        setNormalizedState(arrayData.getNormalizationState());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        splitPane.setOneTouchExpandable(true);
        mainframe.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel("TIGR MultiExperiment Viewer");
        mainframe.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        mainframe.pack();
        splitPane.setDividerLocation(.2);

        if (data.getDataType() == IData.DATA_TYPE_RATIO_ONLY || data.getDataType() == IData.DATA_TYPE_AFFY_ABS){
            this.menubar.enableNormalizationMenu(false);
        }
        	
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
        
        //systemDisable(TMEV.DB_AVAILABLE);
        //systemDisable(TMEV.DATA_AVAILABLE);
    }
   
    /**
     * Sets toolbar and menubar states.
     */
    public void systemDisable(int state) {
        menubar.systemDisable(state);
        toolbar.systemDisable(state);
    }
    
    /**
     * Sets toolbar and menubar states.
     */
    public void systemEnable(int state) {
        menubar.systemEnable(state);
        toolbar.systemEnable(state);
    }
    
    /**
     * Returns a reference to an instance of algorithm factory.
     */
    public AlgorithmFactory getAlgorithmFactory() {
        return TMEV.getAlgorithmFactory();
    }
    
    /**
     * Returns a reference to an instance of microarrays data.
     */
    public IData getData() {
        return data;
    }
    
    /**
     * Runs a single array viewer for specified column.
     */
    private void displaySingleArrayViewer(int column) {
        Manager.createNewSingleArrayViewer(data.getFeature(column));
    }
    
    /**
     * Runs a slide element info dialog for a specified spot.
     */
    private void displaySlideElementInfo(int column, int row) {
        Manager.displaySlideElementInfo(mainframe, data, column, row);
    }
    
    /*********************************************
     *  This section of code defines methods to save the state of MeV
     *  to file.
     *
     *  Process:
     *
     *  -Save a time stamp
     *  -Save MultipleArrayData
     *  -Save Analysis Counter
     *  -Save the Analysis Node via ResultTree
     *  -Save the ClusterRepositories
     *  -Save the History Node via ResultTree
     *
     */
    
    public void saveAnalysisAs() {
        try {
            
            String dataPath = TMEV.getDataPath();
            File fileLoc = TMEV.getFile("data/");
            
            // if the data path is null go to default, if not null and not exist then to to default
            // else use the dataPath
            
            if(dataPath != null) {
                fileLoc = new File(dataPath);
                
                if(!fileLoc.exists()) {
                    fileLoc = TMEV.getFile("data/");
                }
            }
            
            final JFileChooser chooser = new JFileChooser(fileLoc);
            chooser.setFileView(new AnalysisFileView());
            chooser.setFileFilter(new AnalysisFileFilter());
            chooser.setApproveButtonText("Save");
            JPanel panel = new JPanel(new GridBagLayout());
            
            final javax.swing.JDialog dialog = new javax.swing.JDialog(getFrame(), "Save Dialog", true);
            
            chooser.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String cmd = ae.getActionCommand();
                    if(cmd.equals(JFileChooser.APPROVE_SELECTION)) {
                        File file = chooser.getSelectedFile();
                        dialog.dispose();
                        try {
                            AnalysisFileFilter filter = new AnalysisFileFilter();
                            String ext = filter.getExtension(file);
                            
                            if(ext == null)
                                file = new File(file.getPath()+".anl");
                            
                            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                            saveState(oos, file);
                            
                            //set tmev.cfg path to match formatted path
                            TMEV.updateDataPath(formatDataPath(file.getPath()));
                            //set variable to OS specific path format
                            TMEV.setDataPath(file.getParentFile().getPath());
                        } catch (IOException ioe) {
                            JOptionPane.showMessageDialog(MultipleArrayViewer.this, "I/O Exception, Error saving analysis. File ("+(file != null ? file.getName() : "name unknown")+")", "Save Analysis", JOptionPane.ERROR_MESSAGE);
                            ioe.printStackTrace();
                        }
                    } else {
                        dialog.dispose();
                    }
                }
            });
            
            
            javax.swing.JTextPane pane = new javax.swing.JTextPane();
            pane.setContentType("text/html");
            pane.setEditable(false);
            
            String text = "<html><body><font face=arial size=4><b><center>Analysis Save and Restoration Warning</center><b><hr size=3><br>";//<hr size=3>";
            text += "<font face=arial size=4>Proper restoration of analysis files is dependent on the Java and Java Virtual Machine versions used to open the file. ";
            text += "Analysis files should be opened using Java and Java Virtual Machine versions that match the versions used to save the file.<br><br>";
            
            text += "If version inconsistencies are found when loading an analysis file the saved and current versions " ;
            text +=  "will be reported at that time.  This problem only arises when moving analysis files between computers ";
            text += "running different versions of Java.<br><br></body></html>";
            
            pane.setMargin(new Insets(10,10,10,10));
            pane.setFont(new java.awt.Font("arial", java.awt.Font.PLAIN, 4));
            pane.setText(text);
            
            JPanel panePanel = new JPanel(new GridBagLayout());
            panePanel.setBorder(BorderFactory.createLineBorder(Color.black));
            panePanel.add(pane,  new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0) );
            panePanel.setPreferredSize(new Dimension(chooser.getPreferredSize().width,((int)(chooser.getPreferredSize().height/1.4))));
            
            panel.add(panePanel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));
            panel.add(chooser, new GridBagConstraints(0,1,1,1,0,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
            
            
            dialog.getContentPane().add(panel);
            dialog.pack();
            
            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((screenSize.width - dialog.getSize().width)/2, (screenSize.height - dialog.getSize().height)/2);
            dialog.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String formatDataPath(String dataPath) {
        if(dataPath == null)
            return " ";
        
        String renderedSep = "/";
        String renderedPath = new String();
        
        String sep = System.getProperty("file.separator");
        
        StringTokenizer stok = new StringTokenizer(dataPath, sep);
        
        String newDataPath = new String();
        
        String str;
        while(stok.hasMoreTokens() && stok.countTokens() > 1){
            str = stok.nextToken();
            renderedPath += str + renderedSep;
            newDataPath += str + sep;
        }
        return renderedPath;
    }
    
    public void saveAnalysis() {
        if(this.currentAnalysisFile != null) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.currentAnalysisFile));
                saveState(oos, currentAnalysisFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "State was not saved.  Error finding file to save. \n"+
            "Please use the \"Save As...\" menu item.", "Save Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    
    private void saveState(ObjectOutputStream os, File file) throws IOException {
        final ObjectOutputStream oos = os;
        final String filePath = file.getAbsolutePath();
        this.currentAnalysisFile = file;
        
        TMEV.activeSave = true;
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try{
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
                    // Save MeV tag
                    oos.writeObject(TMEV.VERSION);
                    //Save JRE Version
                    String jre = System.getProperty("java.version");
                    if(jre != null)
                        oos.writeObject(jre);
                    else
                        oos.writeObject("unknown");
                    //Save JVM Version
                    String jvm = System.getProperty("java.vm.version");
                    if(jvm != null)
                        oos.writeObject(jvm);
                    else
                        oos.writeObject("unknown");
                    // Save Date
                    oos.writeLong(System.currentTimeMillis());
                    // Save IData
                    oos.writeObject(data);
                    // Save Analysis Counter
                    oos.writeInt(resultCount);
                    // Save Analysis Tree
                    tree.writeResults(oos);
                    // Save Cluster Repositories
                    saveClusterRepositories(oos);
                    // Record the save to history
                    addHistory("Save Analysis: "+filePath);
                    // Save History Tree
                    tree.writeHistory(oos, historyNode);
                    //reset result changed boolean
                    modifiedResult = false;
                    //enable save menu item, current file is already set
                    menubar.systemEnable(TMEV.ANALYSIS_LOADED);
                    oos.flush();
                    oos.close();
                    setCursor(Cursor.DEFAULT_CURSOR);
                    
                    TMEV.activeSave = false;
                    
                } catch (IOException ioe){
                    setCursor(Cursor.DEFAULT_CURSOR);
                    JOptionPane.showMessageDialog(MultipleArrayViewer.this, "Analysis was not saved.  Error writing output file.",
                    "Save Error", JOptionPane.WARNING_MESSAGE);
                    ioe.printStackTrace();
                    TMEV.activeSave = false;
                }
            }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }
    
    
    private void loadState(ObjectInputStream is) throws IOException, ClassNotFoundException {
        final ObjectInputStream ois = is;
        
        Thread thread = new Thread( new Runnable(){
            public void run() {
                try {
                    
                    String savedMEVVersion = (String)ois.readObject();
                    String savedJREVersion = (String)ois.readObject();
                    String savedJVMVersion = (String)ois.readObject();
                    String jreVersion = System.getProperty("java.version");
                    String jvmVersion = System.getProperty("java.vm.version");
                    
                    if(!savedMEVVersion.equals(TMEV.VERSION) || !savedJREVersion.equals(jreVersion) || !savedJVMVersion.equals(jvmVersion)) {
                        String msg = "";
                        String error = "<b>";
                        int errorCount = 0;
                        
                        if(!savedMEVVersion.equals(TMEV.VERSION)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current MeV Version: "+TMEV.VERSION+"<br>";
                            error += "Analysis File MeV Version: "+savedMEVVersion+"<br>";
                        }
                        if(!savedJREVersion.equals(jreVersion)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current Java Runtime Version: "+jreVersion+"<br>";
                            error += "Java Runtime Version During Save: "+savedJREVersion+"<br>";
                        }
                        if(!savedJVMVersion.equals(jvmVersion)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current Java Virtual Machine Version: "+jvmVersion+"<br>";
                            error += "Java Virtual Machine Version During Save: "+savedJVMVersion+"<br>";
                        }
                        
                        msg += "<html><body><font face=arial size=4>The following inconsistenc"+(errorCount == 1 ? "y was" : "ies were")+
                        " detected during analysis file loading:<br>" + error + "</b><br>";
                        if(errorCount > 1) {
                            msg += "These differences ";
                        } else {
                            msg += "This difference ";
                        }
                        msg += "could affect analysis file loading. <br><br>";
                        msg += "Hit <b>\"OK\"</b> to continue loading. (Loading errors will be reported if they occur)<br>";
                        msg += "Hit <b>\"Cancel\"</b> to abort the loading process.</body></html>";
                        
                        JPanel panel = new JPanel();
                        panel.setBackground(Color.white);
                        panel.setBorder(BorderFactory.createLineBorder(Color.black));
                        javax.swing.JTextPane pane = new javax.swing.JTextPane();
                        pane.setEditable(false);
                        pane.setMargin(new Insets(10,10,10,10));
                        pane.setBackground(Color.white);
                        pane.setContentType("text/html");
                        pane.setText(msg);
                        panel.add(pane);
                        
                        int choice = JOptionPane.showConfirmDialog(getFrame(), panel, "Analysis Load Version Confirmations", JOptionPane.WARNING_MESSAGE);
                        //  JOptionPane.showMessageDialog(getFrame(), msg, "Analysis Load Version Confirmations", JOptionPane.WARNING_MESSAGE);
                        if(choice != JOptionPane.OK_OPTION)
                            return;
                    }
                    long dateLong = ois.readLong();
                    //Load IData object and set annotation field names
                    loadIData(ois);
                    
                    //set the current result count
                    resultCount = ois.readInt();
                    
                    //load analysis viewers
                    loadAnalysisNode(ois);
                    
                    //load cluster repositories
                    loadClusterRepositories(ois);
                    
                    //load history
                    loadHistoryNode(ois);
                    
                    //Add time node to the analysis node
                    Date date = new Date(System.currentTimeMillis());
                    DateFormat format = DateFormat.getDateTimeInstance();
                    
                    format.setTimeZone(TimeZone.getDefault());
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
                    DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
                    treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
                    
                    TreePath path = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(analysisNode));
                    tree.expandPath(path);
                    path = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(historyNode));
                    tree.expandPath(path);
                    
                    //signal mev analysis loaded
                    menubar.systemEnable(TMEV.ANALYSIS_LOADED);
                    
                    //pcahan
                    if(TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                        menubar.addAffyFilterMenuItems();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultipleArrayViewer.this, "Analysis was not loaded.  Error reading input file.",
                    "Load Analysis Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    
    private void loadClusterRepositories(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        if(ois.readBoolean()){
            this.geneClusterRepository = (ClusterRepository)ois.readObject();
            this.data.setGeneClusterRepository(this.geneClusterRepository);
            this.geneClusterRepository.setFramework(this.framework);
            
            this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
            DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
            addNode(this.clusterNode, genesNode);
            
        }
        if(ois.readBoolean()){
            this.experimentClusterRepository = (ClusterRepository)ois.readObject();
            this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            this.experimentClusterRepository.setFramework(this.framework);
            
            this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
            DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Sample Clusters", this.experimentClusterManager), false);
            addNode(this.clusterNode, experimentNode);
        }
    }
    
    
    private void loadAnalysisNode(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        
        DefaultMutableTreeNode node = tree.loadResults(ois);
        
        if(node != null){
            
            int location = tree.getModel().getIndexOfChild(tree.getRoot(), analysisNode);
            tree.removeNode(analysisNode);
            analysisNode = node;
            tree.insertNode(analysisNode, tree.getRoot(), location);
            tree.setAnalysisNode(analysisNode);
        }
    }
    
    private void loadHistoryNode(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        
        DefaultMutableTreeNode node = tree.loadResults(ois);
        
        if(node != null){
            tree.removeNode(historyNode);
            historyNode = node;
            tree.insertNode(historyNode, tree.getRoot(), tree.getRoot().getChildCount());
            historyLog = (HistoryViewer)(((LeafInfo)(((DefaultMutableTreeNode)historyNode.getChildAt(0)).getUserObject())).getViewer());
        }
    }
    
    
    private void loadIData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //loads IData and sets TMEV field names fields
        this.data = (MultipleArrayData)(ois.readObject());
        
        //pcahan
        int data_type = this.data.getDataType();
        if (data_type!=0 || data_type!=1){
            TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
        }
        
        //resets the log state depending on data type
        this.data.setDataType(this.data.getDataType());
        
        //get the experiment label keys
        this.menubar.replaceExperimentLabelMenuItems(data.getSlideNameKeyArray());
        
        //populate the display menu
        this.menubar.replaceLabelMenuItems(this.data.getFieldNames());
        this.menubar.replaceSortMenuItems(this.data.getFieldNames());
        
        setMaxCY3AndCY5();
        systemEnable(TMEV.DATA_AVAILABLE);
        fireMenuChanged();
        fireDataChanged();
        fireHeaderChanged();
    }
    
    
    private void saveClusterRepositories(ObjectOutputStream oos) throws IOException {
        if(this.geneClusterRepository == null)
            oos.writeBoolean(false);
        else{
            oos.writeBoolean(true);
            oos.writeObject(this.geneClusterRepository);
        }
        if(this.experimentClusterRepository == null)
            oos.writeBoolean(false);
        else{
            oos.writeBoolean(true);
            oos.writeObject(this.experimentClusterRepository);
        }
    }
    
    
    private void loadAnalysis() {
        
        String dataPath = TMEV.getDataPath();
        File pathFile = TMEV.getFile("data/");
        
        if(dataPath != null) {
            pathFile = new File(dataPath);
            if(!pathFile.exists())
                pathFile = TMEV.getFile("data/");
        }
        
        File file;
        try {
            JFileChooser chooser = new JFileChooser(pathFile);
            chooser.setFileView(new AnalysisFileView());
            chooser.setFileFilter(new AnalysisFileFilter());
            if(chooser.showOpenDialog(this) == JOptionPane.OK_OPTION) {
                file = chooser.getSelectedFile();
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                loadState(ois);
                this.currentAnalysisFile = file;
                //set tmev.cfg to formatted path
                TMEV.updateDataPath(formatDataPath(file.getPath()));
                //set variable to OS format path
                TMEV.setDataPath(file.getParentFile().getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*****************************
     *
     * Script code
     */
    private void onNewScript() {
        if(this.scriptManager == null) {
            scriptManager = new ScriptManager(framework, scriptNode, manager);
        }
        scriptManager.addNewScript();
    }
    
    private void onLoadScript() {
        if(this.scriptManager == null) {
            scriptManager = new ScriptManager(framework, scriptNode, manager);
        }
        scriptManager.loadScript();
    }
    
    
    /**
     * Returns the status bar text.
     */
    private String getStatusText() {
        return statusLabel.getText();
    }
    
    /**
     * Sets the status bar text.
     */
    private void setStatusText(String text) {
        statusLabel.setText(text);
    }
    
    /**
     * Returns an user object of a selected LeafInfo.
     */
    private Object getUserObject() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return null;
        }
        Object leaf = node.getUserObject();
        if (!(leaf instanceof LeafInfo)) {
            return null;
        }
        return((LeafInfo)leaf).getUserObject();
    }
    
    /**
     * Returns the framework main frame.
     */
    public JFrame getFrame() {
        return mainframe;
    }
    
    /**
     * Moves the scroll pane content into specified coordinaties.
     */
    public void setContentLocation(int x, int y) {
        Dimension viewSize = viewScrollPane.getViewport().getViewSize();
        Dimension extSize  = viewScrollPane.getViewport().getExtentSize();
        if (extSize.height+y > viewSize.height) {
            y = viewSize.height - extSize.height;
        }
        viewScrollPane.getViewport().setViewPosition(new Point(x, y));
    }
    
    /**
     * Creates the navigation tree.
     */
    private JScrollPane createTreeScrollPane(EventListener listener) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MultipleExperimentViewer");
        
        this.viewer = new MultipleArrayCanvas(this.framework, new Insets(0, 10, 0, 20));
        
        LeafInfo mainViewLeafInfo = new LeafInfo("Main View", viewer);
        //mainViewerNode = new DefaultMutableTreeNode(mainViewLeafInfo, false);        mainViewerNode = new DefaultMutableTreeNode(mainViewLeafInfo, true); /* To add new nodes at this level */
        
        root.add(mainViewerNode);
        
        clusterNode = new DefaultMutableTreeNode(new LeafInfo("Cluster Manager"));
        root.add(clusterNode);
        
        analysisNode = new DefaultMutableTreeNode(new LeafInfo("Analysis Results"));
        root.add(analysisNode);
        
        scriptNode = new DefaultMutableTreeNode(new LeafInfo("Script Manager"));
        root.add(scriptNode);
        
        historyNode = new DefaultMutableTreeNode(new LeafInfo("History"));
        root.add(historyNode);
        historyLog = new HistoryViewer();
        historyNode.add(new DefaultMutableTreeNode(new LeafInfo("History Log", historyLog)));
        
        tree = new ResultTree(root);
        tree.setAnalysisNode(analysisNode);
        
        tree.addTreeSelectionListener(listener);
        tree.addMouseListener(listener);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setSelectionPath(new TreePath(mainViewerNode.getPath()));
        tree.setEditable(false);
        
        ToolTipManager.sharedInstance().registerComponent(tree);
        
        
        
        return new JScrollPane(tree);
    }
    
    /**
     * Creates the scroll pane to display calculation results.
     */
    private JScrollPane createViewScrollPane(EventListener listener) {
        JScrollPane scrollPane = new JScrollPane();
        //scrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        //scrollPane.getVerticalScrollBar().setToolTipText("Use up/down/pgup/pgdown to scroll image");
        KeyStroke up = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0);
        KeyStroke pgup = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0);
        KeyStroke pgdown = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0);
        scrollPane.registerKeyboardAction(listener, "lineup", up, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "linedown", down, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "pageup", pgup, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "pagedown", pgdown, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return scrollPane;
    }
    
    /**
     * Sets a current viewer. The viewer content will be inserted
     * into the scroll pane view port and the viewer header will
     * be used as the scroll pane header view.
     */
    /*private void setCurrentViewer(IViewer viewer) {
        if (viewer == null || viewer.getContentComponent() == null) {
            return;
        }
        if (this.viewer != null) {
            this.viewer.onDeselected();
        }
        this.viewer = viewer;
        this.viewScrollPane.setViewportView(this.viewer.getContentComponent());
     
        JPanel emptycorner = new JPanel();
        emptycorner.setBackground(Color.white);
        emptycorner.setOpaque(true);
     
        if (viewer instanceof GDMGeneViewer == true) {
     
                GDMGeneViewer gdmV = (GDMGeneViewer)viewer;
     
                gdmV.setMultipleArrayData(data);
     
                gdmV.setMainFrame(mainframe);
     
                JComponent colHeader = gdmV.getColumnHeaderComponent();
                if (colHeader != null) {
                    this.viewScrollPane.setColumnHeaderView(colHeader);
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
     
                JComponent rowHeader = gdmV.getRowHeaderComponent();
                if (rowHeader != null) {
                    this.viewScrollPane.setRowHeaderView(rowHeader);
                } else {
                    this.viewScrollPane.setRowHeader(null);
                }
     
                JComponent upperRightCornerSB = gdmV.getUpperRightCornerSB();
                if (upperRightCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, upperRightCornerSB);
                }
     
                JComponent lowerLeftCornerSB = gdmV.getLowerLeftCornerSB();
                if (lowerLeftCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, lowerLeftCornerSB);
                }
     
                this.viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                this.viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     
        } else if (viewer instanceof GDMExpViewer == true) {
     
                GDMExpViewer gdmV = (GDMExpViewer)viewer;
     
                gdmV.setMultipleArrayData(data);
     
                gdmV.setMainFrame(mainframe);
     
                JComponent colHeader = gdmV.getColumnHeaderComponent();
                if (colHeader != null) {
                    this.viewScrollPane.setColumnHeaderView(colHeader);
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
     
                JComponent rowHeader = gdmV.getRowHeaderComponent();
                if (rowHeader != null) {
                    this.viewScrollPane.setRowHeaderView(rowHeader);
                } else {
                    this.viewScrollPane.setRowHeader(null);
                }
     
                JComponent upperRightCornerSB = gdmV.getUpperRightCornerSB();
                if (upperRightCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, upperRightCornerSB);
                }
     
                JComponent lowerLeftCornerSB = gdmV.getLowerLeftCornerSB();
                if (lowerLeftCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, lowerLeftCornerSB);
                }
     
                this.viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                this.viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     
        } else {
     
                JComponent header = viewer.getHeaderComponent();
                if (header != null) {
                    this.viewScrollPane.setColumnHeaderView(header);
     
                    if (this.viewScrollPane.getCorner(JScrollPane.UPPER_RIGHT_CORNER) != null) {
                        this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, emptycorner);
                    }
     
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
                this.viewScrollPane.setRowHeader(null);
     
        }
     
        this.viewer.onSelected(framework);
        doViewLayout();
        handleThumbnailButton(this.viewer);
    }
     */
    
    /**
     * Sets a current viewer. The viewer content will be inserted
     * into the scroll pane view port and the viewer header will
     * be used as the scroll pane header view.
     */
    
    private void setCurrentViewer(IViewer viewer) {
        
        /*
        if (viewer == null || viewer.getContentComponent() == null) {
            return;
        }
         *
         *        
         * Above wascode changed to support nodes that have viewers with null content
         * These 'viewers' still implement onSelected for the sake of initialization
         * of node-based popups created after deserialization (loading an analysis)
         * 
         * The new corresponding code is in two blocks below marked by '&&'.
         *
         * 12.16.2004 implemented for this purpose to handle new menus in PCA
         * for selection of displayed components.
         */
        
        // && handles the cases where selected node does not have an IViewer
        if(viewer == null) {
            return;
        }
        
        // && handles viewers that contain a null content component, *See Above*
        if(viewer.getContentComponent() == null) {
            viewer.onSelected(framework);
            return;
        }
        
        if (this.viewer != null) {
            this.viewer.onDeselected();
        }
        this.viewer = viewer;        /* Raktim Nov 15, 2005 - CGH Specific         * Special case to handle CGHPositionGraphViewer.		 * CGHPositionGraphViewer needs onSelected called before setting the viewport		 */		if (this.viewer instanceof CGHPositionGraphViewer) {			this.viewer.onSelected(framework);        }
     
        this.viewScrollPane.setViewportView(this.viewer.getContentComponent());
        
        //Top Header (column header)
        JComponent header = viewer.getHeaderComponent();
        if (header != null) {
            this.viewScrollPane.setColumnHeaderView(header);
        } else {
            this.viewScrollPane.setColumnHeader(null);
        }
        
        //Left header (row header)
        JComponent rowHeader = viewer.getRowHeaderComponent();
        if (rowHeader != null) {
            this.viewScrollPane.setRowHeaderView(rowHeader);
        } else {
            this.viewScrollPane.setRowHeader(null);
        }
        
        //Corner components
        JComponent cornerComponent = viewer.getCornerComponent(IViewer.UPPER_LEFT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerComponent);
        
        cornerComponent = viewer.getCornerComponent(IViewer.UPPER_RIGHT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerComponent);
        
        cornerComponent = viewer.getCornerComponent(IViewer.LOWER_LEFT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerComponent);
        
        //this.viewer.onSelected(framework);        /* Raktim Nov 15, 2005 - CGH Specific		 * New if condition.		 * Do not call onSelected if viewer id of type CGHPositionGraphViewer		 */		if (!(this.viewer instanceof CGHPositionGraphViewer)) {			this.viewer.onSelected(framework);        }        doViewLayout();
    }
    

    /**
     * Returns a current viewer.
     */
    private IViewer getCurrentViewer() {
        return viewer;
    }
    
    /**
     * Invokes onClose method for all the viewers when the framework is
     * going to be closed.
     */
    private void fireOnCloseEvent(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof LeafInfo) {
            LeafInfo leafInfo = (LeafInfo)userObject;
            IViewer viewer = leafInfo.getViewer();
            if (viewer != null) {
                viewer.onClosed();
            }
        }
        for (int i=0; i<node.getChildCount(); i++) {
            fireOnCloseEvent((DefaultMutableTreeNode)node.getChildAt(i));
        }
    }
    
    /**
     * Invoked by a window listener when frame close button was pressed.
     */
    private void onClose() {
        onSaveCheck();
        
        addHistory("Close Viewer");
        
        TMEV.setDataType(TMEV.DATA_TYPE_TWO_DYE);  //default type
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        fireOnCloseEvent((DefaultMutableTreeNode)model.getRoot());
        mainframe.dispose();
        Manager.removeComponent(this);
    }
    
    /**
     * Checks to see if the session should be saved
     */
    private void onSaveCheck() {
        //meets three criteria, has data loaded, result is modified, allowed to prompt
        if(this.modifiedResult && this.data != null && TMEV.permitSavePrompt){
            AnalysisSaveDialog dialog = new AnalysisSaveDialog(this.getFrame());
            int result = dialog.showModal();
            boolean permitSave = dialog.askAgain();
            if(result == JOptionPane.YES_OPTION){
                saveAnalysisAs();
            }
            if(TMEV.permitSavePrompt != permitSave) {
                TMEV.setPermitPrompt(permitSave);
            }
        }
    }
    
    /**
     * Creates an image for specified viewer.
     */
    private BufferedImage createDefaultImage(IViewer viewer) {
        JComponent content = viewer.getContentComponent();
        JComponent header  = viewer.getHeaderComponent();
        int width  = content.getWidth();
        int height = content.getHeight();
        if (header != null) {
            width = Math.max(width, header.getWidth());
            height += header.getHeight();
        }
        // BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);  //need to use this type for image creation
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        if (header != null) {
            int headerHeight = header.getHeight();
            g.setClip(0, 0, width, headerHeight);
            header.paint(g);
            g.translate(0, headerHeight);
            g.setClip(0, 0, width, height-headerHeight);
        } else {
            g.setClip(0, 0, width, height);
        }
        content.paint(g);
        return image;
    }
    
    /**
     * Saves a current viewer image into the user specified file.
     */
    private void onSaveImage() {
        final JFileChooser chooser = new JFileChooser(TMEV.getFile("data?"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new BMPFileFilter());
        chooser.addChoosableFileFilter(new JPGFileFilter());
        chooser.addChoosableFileFilter(new PNGFileFilter());
        chooser.addChoosableFileFilter(new TIFFFileFilter());
        int chooserState = chooser.showSaveDialog(getFrame());
        if (chooserState == JFileChooser.APPROVE_OPTION) {
            IViewer viewer = getCurrentViewer();
            BufferedImage image = viewer.getImage();
            if (image == null) {
                image = createDefaultImage(viewer);
            }
            final File fFile = chooser.getSelectedFile();
            final BufferedImage fImage = image;
            final String fFormat = ((ImageFileFilter)chooser.getFileFilter()).getFileFormat();
            final ImageEncodeParam fParam = ((ImageFileFilter)chooser.getFileFilter()).getImageEncodeParam();
            try {
                Thread thread = new Thread() {
                    public void run() {
                        JAI.create("filestore", fImage, fFile.getPath(), fFormat, fParam);
                        Manager.message(getFrame(), "Image saved: "+fFile.getPath());
                    }
                };
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            } catch (Exception e) {
                Manager.message(getFrame(), e);
            }
        }
    }
    
    
    /**
     * Loads file with a microarray data.
     */
    private void onLoadFile() {
        try {
            ISlideData slideData = loadSlideData(data.getSlideMetaData());
            if (slideData != null){
                addFeature(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load File Error", e);
        }
    }
    
    /**
     * Loads microarrays data from cluster formatted file.
     */
    private void onLoadCluster() {
        loadFromStanfordFile("Select a Cluster File to Open");
    }
    
    /**
     * Loads microarrays data from stanford formatted file.
     */
    private void onLoadStanford() {
        loadFromStanfordFile("Select a Stanford File to Open");
    }
    
    /**
     * Loads microarrays data from a database.
     *
     * Note: does'nt implemented at the moment.
     */
    private void onLoadDatabase() {
        SetDatabaseDialog sdd = new SetDatabaseDialog(getFrame());
        if (sdd.showModal() != JOptionPane.OK_OPTION) {
            return;
        }
        String database = sdd.getDatabase();
        
        // STUB: file names shold be loaded from the 'database'
        String[] files = new String[] {"L4A1", "L4A2", "L4A3"};
        
        SetSlideFilenameDialog ssfd = new SetSlideFilenameDialog(getFrame(), files);
        if (ssfd.showModal() != JOptionPane.OK_OPTION) {
            return;
        }
        String filename = ssfd.getFileName();
        System.out.println("db  : "+database);
        System.out.println("file: "+filename);
    }
    
    /**
     * Loads stanford file.
     * @param title the title for standard file chooser dialog.
     */
    private void loadFromStanfordFile(String title) {
        try {
            ISlideData[] slideData = super.loadStanfordFile(title);
            if (slideData != null) {
                addFeatures(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load Data Error", e);
        }
    }
    
    /**
     * Loads data from the user specified directory.
     */
    private void onLoadDirectory() {
        try {
            ISlideData[] slideData = loadDirectory(data.getSlideMetaData());
            if (slideData != null) {
                addFeatures(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load Directory Error", e);
        }
    }
    
    /**
     * Sets Initial Max CY3 and CY5 in menu
     */
    private void setMaxCY3AndCY5(){
      this.menubar.setMaxCY3Scale(data.getMaxCY3());
      this.menubar.setMaxCY5Scale(data.getMaxCY5());
    }
    
    /**
     * Invoked when a label menu item is selected.
     */
    private void onLabelChanged(Action action) {
        String index = (String)action.getValue(ActionManager.PARAMETER);
        menubar.setLabelIndex(Integer.parseInt(index));
        fireMenuChanged();
    }
    
    /**
     * Invoked when a label menu item is selected.
     */
    private void onExperimentLabelChanged(Action action) {
        String key = (String)action.getValue(ActionManager.PARAMETER);
        //menubar.setExperimentLabelIndex(Integer.parseInt(index));
        this.data.setSampleLabelKey(key);
        fireMenuChanged();
    }
    
    
    private void onExperimentLabelAdded() {
        
        boolean safeToReorderExperiments = false;
        
        //make sure no results exist and cluster repositories are null, then safe to reorder.
        //note that result counter starts at 1 and holds the index for the next result
        safeToReorderExperiments = (this.resultCount < 2 && this.geneClusterRepository == null && this.experimentClusterRepository == null);
        
        //get the longest key set from loaded samples
        Vector featureAttributes = this.data.getSlideNameKeyVectorUnion();
        
        
        ExperimentLabelEditor editor = new ExperimentLabelEditor(this.getFrame(), featureAttributes, this.data, safeToReorderExperiments);
        
        //return if not OK
        if(editor.showModal() != JOptionPane.OK_OPTION)
            return;
        
        //get data and keys
        String [][] data = editor.getLabelDataWithoutKeys();
        String [] keys = editor.getLabelKeys();
        
        //add/update features
        for(int i=0; i < keys.length; i++)
            this.data.addNewExperimentLabel(keys[i], data[i]);
        
        //add the new label to the experiment label menu
        this.menubar.replaceExperimentLabelMenuItems(keys);
        
        //now the data has been updated, check for reordering request
        if(safeToReorderExperiments && editor.isReorderedSelected()) {
            int [] order = editor.getNewOrderScheme();
            ArrayList featuresList = new ArrayList(order.length);
            for(int i = 0; i < order.length; i++) {
                featuresList.add(this.data.getFeature(order[i]));
            }
            //set new features list
            this.data.setFeaturesList(featuresList);
        }
        this.fireDataChanged();
    }
    
    /**
     * Adds a microarray data into the framework.
     */
    private void addFeature(ISlideData slideData) {
        data.addFeature(slideData);
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
    }
    
    /**
     * Adds an array of microarrays data into the framework.
     */
    private void addFeatures(ISlideData[] slideData) {
        data.addFeatures(slideData);
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
    }
    
    /**
     * Notifies a current viewer what the framework data is changed.
     */
    public void fireDataChanged() {
        IViewer viewer = getCurrentViewer();
        if (viewer == null) {
            return;
        }
        viewer.onDataChanged(data);
        doViewLayout();
    }
    
    /**
     * Notifies a current viewer what the framework menu is changed.
     */
    private void fireMenuChanged() {
        IViewer viewer = getCurrentViewer();
        if (viewer == null) {
            return;
        }        /* Raktim, Handle differently for CGH Menu */        if(viewer instanceof ICGHViewer){        	//((ICGHViewer)viewer).onMenuChanged(menubar.getCghDisplayMenu());        	((ICGHViewer)viewer).onMenuChanged(menubar.getDisplayMenu());        } else {        	viewer.onMenuChanged(menubar.getDisplayMenu());        }        doViewLayout();
    }
    
    
    /**
     * Invoked when the header name is truncated or expanded.
     */
    private void fireHeaderChanged() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object userObject = node.getUserObject();
        if (!(userObject instanceof LeafInfo)) {
            return;
        }
        setCurrentViewer(((LeafInfo)userObject).getViewer());
    }
    
    /**
     * Updates the scroll pane size according to a current
     * viewer one.
     */
    private void doViewLayout() {
        JViewport header = viewScrollPane.getColumnHeader();
        if (header != null) {
            header.doLayout();
        }
        viewScrollPane.getViewport().doLayout();
        viewScrollPane.doLayout();
        viewScrollPane.repaint();
    }
    
    /**
     * Normalize the framework data with specified mode.
     */
    private void onNormalizeData(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalize(mode, this);
        addHistory("Normalization State: "+SlideData.normalizationString(mode));
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Normalize the framework data with specified mode.
     */
    private void onNormalizeDataList(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalizeList(mode);
        addHistory(SlideData.normalizationString(mode));
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Invoked when the navigation tree node is changed.
     */
    private void onNodeChanged(TreeSelectionEvent event) {
        JTree tree = (JTree)event.getSource();
        TreePath path = event.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object userObject = node.getUserObject();
        if (!(userObject instanceof LeafInfo)) {
            return;
        }
        setCurrentViewer(((LeafInfo)userObject).getViewer());
    }
    
    /**
     * Sets a spot size.
     */
    private void onElementSizeChanged(int width, int height) {
        menubar.setElementSize(width, height);
        fireMenuChanged();
    }
    
    /**
     * Sets the user specified spot size.
     */
    private void onElementSizeChanged() {
        SetElementSizeDialog sesd = new SetElementSizeDialog(getFrame(), menubar.getDisplayMenu().getElementSize());
        if (sesd.showModal() == JOptionPane.OK_OPTION) {
            Dimension size = sesd.getElementSize();
            onElementSizeChanged(size.width, size.height);
        }
    }
    
    /**
     * Sets the color pallete colors
     */
    private void onColorSchemeChange(int colorScheme){
        int initColorScheme = menubar.getColorScheme();
        if(colorScheme == IDisplayMenu.GREEN_RED_SCHEME || colorScheme == IDisplayMenu.BLUE_YELLOW_SCHEME || colorScheme == IDisplayMenu.RAINBOW_COLOR_SCHEME) {
            this.menubar.setColorSchemeIndex(colorScheme);
            if(colorScheme == IDisplayMenu.RAINBOW_COLOR_SCHEME) {
            	this.menubar.setUseDoubleGradient(false); //rainbow uses single gradient (pos)
            } else {
                this.menubar.setUseDoubleGradient(true);  //use double gradient
            }
        } else { 
        	//select a custom color scheme
        	
        	// using rainbow scheme set current to standard green/black/red first
        	boolean rainbowScheme = (initColorScheme == IDisplayMenu.RAINBOW_COLOR_SCHEME);
        	if(rainbowScheme) {
        		menubar.setColorSchemeIndex(IDisplayMenu.GREEN_RED_SCHEME);
        		menubar.setUseDoubleGradient(true);
        	}
            ColorSchemeSelectionDialog dialog = new ColorSchemeSelectionDialog((Frame)getFrame(), true, menubar.getNegativeGradientImage(), menubar.getPositiveGradientImage(), this.menubar.getDisplayMenu().getUseDoubleGradient());            
            if(dialog.showModal() != JOptionPane.OK_OPTION) {
            	//if not ok and using rainbow, set back
            	if(rainbowScheme) { 
            		menubar.setColorSchemeIndex(IDisplayMenu.RAINBOW_COLOR_SCHEME);            		
            		menubar.setUseDoubleGradient(false);
            	}
            	return;
            }
            this.menubar.setPositiveCustomGradient(dialog.getPositiveGradient());
            this.menubar.setNegativeCustomGradient(dialog.getNegativeGradient());
            this.menubar.setColorSchemeIndex(colorScheme);
            this.menubar.setUseDoubleGradient(dialog.getUseDoubleGradient());                    	
        }
        
        fireMenuChanged();
    }
    

    /**
     *  Sets the current (selected) state of gradient use
     */
    private void onColorGradientChange(boolean gradientState){
        menubar.setColorGradientState(gradientState);
        fireMenuChanged();
    }
    
    
    /**
     * Invoked when draw borders menu item is changed.
     */
    private void onDrawBorders() {
        menubar.setDrawBorders(!menubar.getDisplayMenu().isDrawingBorder());
        fireMenuChanged();
    }
    
    
    /**
     * Shows the system info dialog.
     */
    private void onSystemInfo() {
        int width = 640, height = 550;
        InformationPanel infoPanel = new InformationPanel();
        JFrame frame = new JFrame("System Information");
        frame.getContentPane().add(infoPanel);
        frame.setSize(width, height);
        Dimension screenSize = getToolkit().getScreenSize();
        frame.setLocation(screenSize.width/2 - width/2, screenSize.height/2 - height/2);
        frame.setResizable(false);
        frame.setVisible(true);
        infoPanel.Start();
    }
    
    /**
     * Shows algorithms default distance functions.
     */
    private void onDefaultDistance() {
        String defaultText = "<html>"+
        "<font color=\"#000000\"><b><u>Default Distances</u></b></font>"+
        "<p>"+
        "<table border=20 cellspacing=10 cellpadding = 10 width= 380 height= 400>"+
        "<tr><th><u><center><width=200>Algorithm</center></u></th><th width = 150><u><center>Default Metric</center></u></th></tr>"+
        "<tr><td><center>HCL, ST, SOTA, KMC, KMS, SOM, CAST, GSH, FOM</center></td><td><center>Euclidean</center></td></tr>"+
        // "<tr><td></td></td><td></td></tr>"+
        //"<tr>"+
        "<tr><td><center>PCA</center></td><td><center>Covariance</center></td></tr>"+
        "<tr><td><center>SVM</center></td><td><center>Dot Product</center></td></tr>"+
        "<tr><td><center>RN, QTC, PTM</center></td><td><center>Pearson Correlation</center></td></tr>"+
        "</center></table>"+
        "</html>";
        JOptionPane.showMessageDialog(getFrame(), new JLabel(defaultText), "Default Distances", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void setNormalizedState(int originalMode){
        if(originalMode == ISlideData.NO_NORMALIZATION){
            menubar.setNormalizedButtonState(5);
        } else {
            addHistory(SlideData.normalizationString(originalMode));
            if(originalMode == ISlideData.TOTAL_INTENSITY){
                menubar.setNormalizedButtonState(0);
            } else if(originalMode == ISlideData.LINEAR_REGRESSION){
                menubar.setNormalizedButtonState(1);
            } else if(originalMode == ISlideData.RATIO_STATISTICS_95 ||
            originalMode == ISlideData.RATIO_STATISTICS_99){
                menubar.setNormalizedButtonState(2);
            } else if(originalMode == ISlideData.ITERATIVE_LOG){
                menubar.setNormalizedButtonState(3);
            }
        }
    }
    
    /**
     * Normalize the framework data.
     */
    private void onNormalize(int mode) {
        final int originalMode = data.getFeature(0).getNormalizedState();
        final int Mode = mode;
        setCursor(Cursor.WAIT_CURSOR);
        try{
            Thread thread = new Thread(new Runnable(){
                public void run(){
                    String result = data.normalize(Mode, MultipleArrayViewer.this);
                    if(!result.equals("no_change")){                // if not aborted in dialog before start
                        if(result.equals("normalized"))                // if normalized
                            addHistory(SlideData.normalizationString(Mode));
                        else if(result.equals("process_abort_reset")){  // if process started then aborted, reset to no norm.
                            addHistory("Norm. aborted, reset to raw state");
                            menubar.setNormalizedButtonState(5); //move radio button
                        }
                        fireDataChanged();
                        setCursor(Cursor.DEFAULT_CURSOR);
                    } else {
                        // process aborted before it starts no change in data, return button state
                        if(originalMode == ISlideData.NO_NORMALIZATION)
                            menubar.setNormalizedButtonState(5);
                        else if(originalMode == ISlideData.TOTAL_INTENSITY)
                            menubar.setNormalizedButtonState(0);
                        else if(originalMode == ISlideData.LINEAR_REGRESSION)
                            menubar.setNormalizedButtonState(1);
                        else if(originalMode == ISlideData.RATIO_STATISTICS_95 ||
                        originalMode == ISlideData.RATIO_STATISTICS_99)
                            menubar.setNormalizedButtonState(2);
                        else if(originalMode == ISlideData.ITERATIVE_LOG)
                            menubar.setNormalizedButtonState(3);
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace(); }
        setCursor(Cursor.DEFAULT_CURSOR);
        fireDataChanged();
    }
    
    
    /**
     * Normalize the framework data.
     */
    private void onNormalizeList(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalizeList(mode);
        addHistory(SlideData.normalizationString(mode));
        fireDataChanged();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Runs a printer job.
     */
    private void onPrintImage() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this, pj.defaultPage());
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException pe) {
                pe.printStackTrace();
            }
        }
    }
    
    /**
     * Prints a current viewer image.
     */
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        IViewer viewer = getCurrentViewer();
        BufferedImage bImage = viewer.getImage();
        if (bImage == null) {
            bImage = createDefaultImage(viewer);
        }
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform t2d = new AffineTransform();
        t2d.translate(pf.getImageableX(), pf.getImageableY());
        double xscale  = pf.getImageableWidth() / (double)bImage.getWidth();
        double yscale  = pf.getImageableHeight() / (double)bImage.getHeight();
        double scale = Math.min(xscale, yscale);
        t2d.scale(scale, scale);
        try {
            g2.drawImage(bImage, t2d, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Printable.NO_SUCH_PAGE;
        }
        return Printable.PAGE_EXISTS;
    }
    
    /**
     * Adds a specified node into the analysis node.
     */
    public synchronized void addAnalysisResult(DefaultMutableTreeNode node) {
        if (node == null) {
            return;
        }
        String nodeTitle = (String) node.getUserObject();
        nodeTitle += " ("+resultCount+")";
        resultCount++;
        modifiedResult = true;
        node.setUserObject(nodeTitle);
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
        TreeSelectionModel selModel = tree.getSelectionModel();
        TreePath treePath = new TreePath(node.getPath());
        selModel.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        JScrollBar bar = this.treeScrollPane.getHorizontalScrollBar();
        if(bar != null)
            bar.setValue(0);
        
        addHistory("Analysis Result: "+nodeTitle);
        /// this.saveAnalysis();
        // this.loadAnalysis();
   /*     try{
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("c:/Temp/out.out"));
             tree.writeResults(oos);
             oos.flush();
             oos.close();
    
             ObjectInputStream ois = new ObjectInputStream(new FileInputStream("c:/Temp/out.out"));
    
             treeModel.insertNodeInto(tree.loadResults(ois), analysisNode, analysisNode.getChildCount());
    
        } catch (Exception e) { e.printStackTrace();}
    **/
    }
    
    /**
     * Adds info into the history node.
     */
    private void addHistory(String info) {
        historyLog.addHistory(info);
        /*
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, historyNode, historyNode.getChildCount());
        TreeSelectionModel selModel = tree.getSelectionModel();
        TreePath treePath = new TreePath(node.getPath());
        selModel.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        this.treeScrollPane.getHorizontalScrollBar().setValue(0);
         */
    }
    
    /**
     * Runs an analysis task and inserts its result into the analysis node.
     */
    private void onAnalysis(Action action) {
        String className = (String)action.getValue(ActionManager.PARAMETER);
        try {
            Class clazz = Class.forName(className);
            final IClusterGUI gui = (IClusterGUI)clazz.newInstance();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        DefaultMutableTreeNode result = gui.execute(framework);
                        addAnalysisResult(result);
                    } catch (AbortException e) {
                        // analysis was canceled by the user
                    } catch (Exception e) {
                        ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } catch (ClassCastException e) {
            System.out.println("Error: org.tigr.microarray.mev.cluster.gui.IClusterGUI interface is expected.");
            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
        }
    }
    
    /**
     * Deletes a selected navigation tree node.
     */
    private void onDeleteNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null || node.getParent() == null) {
            return;
        }
        fireOnCloseEvent(node);
        TreePath parentPath = new TreePath(((DefaultMutableTreeNode)node.getParent()).getPath());
        ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);
        ((TreeSelectionModel)tree.getSelectionModel()).setSelectionPath(parentPath);
        tree.scrollPathToVisible(parentPath);
        
        String nodeName = " ";
        Object object = node.getUserObject();
        if(object instanceof LeafInfo)
            nodeName = ((LeafInfo)object).toString();
        else if(object instanceof String)
            nodeName = (String)object;
        addHistory("Deleted Node: "+nodeName);
        
    }
    
    /**
     *
     */
    private void onSetData(boolean isSelected) {
        boolean selected = isSelected;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        Object object = node.getUserObject();
        String nodeName;
        Experiment experiment;
        int [][] clusters;
        LeafInfo leafInfo;
        int index = 0;
        int viewerType;
        
        if (node == null || node.getParent() == null) {
            return;
        }
        
        if(object instanceof LeafInfo) {
            leafInfo = (LeafInfo)object;
            
            IViewer viewer = leafInfo.getViewer();
            if(viewer != null) {
                experiment = viewer.getExperiment();
                clusters = viewer.getClusters();
                
                //special case, reset to main view data
                if(leafInfo.toString().equals("Main View")) {
                    tree.clearDataSelection();
                    leafInfo.setSelectedDataSource(isSelected);
                    data.setUseMainData(true);
                    //createDataSelectionNode((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0)), data.getExperiment());
                    createDataSelectionNode((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0)), data.getExperiment(), data.getExperiment().getNumberOfGenes(), Cluster.GENE_CLUSTER);
                    tree.repaint();
                    return;
                }
                
                if(experiment == null || clusters == null)
                    return;
                
                Object leafUserObject = leafInfo.getUserObject();
                if(leafUserObject instanceof Integer) {
                    index = ((Integer)leafUserObject).intValue();
                } else if (leafUserObject instanceof CentroidUserObject) {
                    index = ((CentroidUserObject)leafUserObject).getClusterIndex();
                } else {
                    //need to consider status of check box, log report
                    return;
                }
                
                viewerType = viewer.getViewerType();
                
                if(viewerType == -1)
                    return;
                
                //have clusters, experiment, cluster index, and viewer type
                
                //Need to traverse result tree and set all LeafInfo selections to false.
                tree.clearDataSelection();
                
                //set as selected data source
                leafInfo.setSelectedDataSource(isSelected);
                
                if(isSelected) {
                    data.constructAndSetAlternateExperiment(experiment, clusters[index], viewerType);
                    createDataSelectionNode(node, experiment, clusters[index].length, viewerType);
                } else {
                    //reset to main data set due to de-selection of node
                    data.setUseMainData(true);
                    ((LeafInfo)((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0))).getUserObject()).setSelectedDataSource(true);
                    createDataSelectionNode((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0)), data.getExperiment(), data.getExperiment().getNumberOfGenes(), Cluster.GENE_CLUSTER);
                }
            }
        }
        tree.repaint();
    }
    
    private void createDataSelectionNode(DefaultMutableTreeNode node, Experiment experiment, int clusterSize, int clusterType) {
        int numGenes;
        int numSamples;
        String dataSourcePath = "";
        String msg = "<html><body>";
        
        msg += "<h1>Data Source Selection Information</h1>";
        msg += "<table align=left>";
        
        TreeNode [] path = node.getPath();
        String indent = "   ";
        
        msg += "<tr><td><b>Data Source Path:</b></td><td>";
        dataSourcePath += "Data Source Path: ";
        
        for(int i = 1; i < path.length-1; i++) {
            msg+= path[i].toString();
            msg+=" : ";
            
            dataSourcePath += path[i].toString();
            dataSourcePath += " : ";
        }
        
        msg += path[path.length-1]+"</td></tr>";
        dataSourcePath += path[path.length-1];
        
        if(clusterType == Cluster.GENE_CLUSTER) {
            numGenes = clusterSize;
            numSamples = experiment.getNumberOfSamples();
            msg += "<tr><td><b>Number of Genes:</b></td><td>"+clusterSize+"</td></tr>";
            msg += "<tr><td><b>Number of Samples:</b></td><td>"+experiment.getNumberOfSamples()+"</td></tr>";
        } else {
            numGenes = experiment.getNumberOfGenes();
            numSamples = clusterSize;
            msg += "<tr><td><b>Number of Genes:</b></td><td>"+experiment.getNumberOfGenes()+"</td></tr>";
            msg += "<tr><td><b>Number of Samples:</b></td><td>"+clusterSize+"</td></tr>";
        }
        
        msg += "</table></body></html>";
        
        DefaultMutableTreeNode dataInfoNode = new DefaultMutableTreeNode(new LeafInfo("Data Source Selection", new TextViewer(msg)));
        addNode(this.analysisNode, dataInfoNode);
        //  tree.scrollToVisible(dataInfoNode);
        String historyString = "Data Source Selection\n";
        historyString +=        "=====================\n";
        historyString += dataSourcePath + "\n";
        historyString += "Number of Genes: "+String.valueOf(numGenes)+"\n";
        historyString += "Number of Samples: "+String.valueOf(numSamples);
        addHistory(historyString);
    }
    //vu 7.22.05
	     private void onRama() {
	         //to modularize code as much as possible, will create object here that
	         //handles rama stuff
	         Rama rama = new Rama(this, this.menubar);
	     }
	     private void onRamaDoc() {
  	         try {
  	                BrowserLauncher.openURL( "http://www.expression.washington.edu/ramaDoc/MeV-R_Documentation.html" );
  	                         //BrowserLauncher.openURL( "http://192.168.200.50:8080/ramaDoc/MeV-R_Documentation.html" );
  	                 } catch( IOException e ) {
  	                         e.printStackTrace();
  	                         //BrowserLauncher doesn't work on this system, display dialog
  	                         JOptionPane.showMessageDialog( framework.getFrame(),
  	                                         "Please see MeV-R_Documentation.html in the documentation folder",
  	                                         "Input Error", JOptionPane.ERROR_MESSAGE );
  	                 }
  	     }
    /** pcahan
     * Sets the user specified Detection Filter.
     */
    private void onSetDetectionFilter() {
        //SetDetectionFilterDialog sdfd = new SetDetectionFilterDialog(getFrame(), data.getDetectionFilter() );
        
        int num_samples = data.getFeaturesCount();
        String[] sample_names = new String[num_samples];
        for (int i = 0; i < num_samples; i++){
            sample_names[i] = data.getFullSampleName(i);
        }
        SetDetectionFilterDialog sdfd;
        if ( data.getdfSet() ) {
            sdfd = new SetDetectionFilterDialog(getFrame(), sample_names, data.getDetectionFilter() );
        }
        else {
            sdfd = new SetDetectionFilterDialog(getFrame(),
            sample_names);
            data.setdfSet(true);
        }
        //SetDetectionFilterDialog sdfd = new SetDetectionFilterDialog(getFrame(), data.getDetectionFilter() );
        if (sdfd.showModal() == JOptionPane.OK_OPTION) {
            data.setDetectionFilter(sdfd.getDetectionFilter());
            if (data.isDetectionFilter()) {
                addHistory("Detection Filter (" + data.getDetectionFilter() + ")");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will used in subsequent analyses");
            }
        }
    }
    
    
    private void onSetFoldFilter() {
        SetFoldFilterDialog ffd;
        int num_samples = data.getFeaturesCount();
        String[] sample_names = new String[num_samples];
        for (int i = 0; i < num_samples; i++){
            sample_names[i] = data.getFullSampleName(i);
        }
        
        if ( data.getffSet() ) {
            ffd = new SetFoldFilterDialog(getFrame(),sample_names);
        }
        else{
            ffd = new SetFoldFilterDialog(getFrame(),sample_names);
            data.setffSet(true);
        }
        
        if (ffd.showModal() == JOptionPane.OK_OPTION) {
            data.setFoldFilter(ffd.getFoldFilter());
            if (data.isFoldFilter()) {
                addHistory("Fold Filter (" + data.getFoldFilter().toString() + ")");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will used in subsequent analyses");
                
            }
        }
    }
        
    
    /**
     * Applys the percentage cutoff filter
     */
    private void applyLowerCutoffs() {                        
        SetLowerCutoffsDialog slcd = new SetLowerCutoffsDialog(getFrame(), data.getLowerCY3Cutoff(), data.getLowerCY5Cutoff());
        if (slcd.showModal() == JOptionPane.OK_OPTION) {

            boolean useCutoff = slcd.isLowerCutoffEnabled();

            data.setUseLowerCutoffs(useCutoff);            
            data.setLowerCutoffs(slcd.getLowerCY3Cutoff(), slcd.getLowerCY5Cutoff());
            Properties props = new Properties();
            props.setProperty("CY3 Cutoff", Float.toString(slcd.getLowerCY3Cutoff()));
            props.setProperty("CY5 Cutoff", Float.toString(slcd.getLowerCY5Cutoff()));
            
            if (data.isLowerCutoffs()) {
                addAdjustmentResultNodes("Data Filter - Low Intensity Cutoff Filter", data.getExperiment(), props);                
                addHistory("Low Intensity Cutoff Filter is ON ( percent = cy3= " +Float.toString(slcd.getLowerCY3Cutoff())+"  cy5 ="+Float.toString(slcd.getLowerCY5Cutoff())+" )");
            } else {
                addHistory("Low Intensity Filter is OFF");
            }
            addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");            
        }
    }    

    /**
     * Applys the percentage cutoff filter
     */
    private void applyPercentageCutoffs() {                        
        SetPercentageCutoffsDialog spcd = new SetPercentageCutoffsDialog(getFrame(), data.getPercentageCutoff());
        if (spcd.showModal() == JOptionPane.OK_OPTION) {
            boolean useCutoff = spcd.isCutoffFilterEnabled();
            float percent = spcd.getPercentageCutoff();

            data.setUsePercentageCutoff(useCutoff);            
            data.setPercentageCutoff(percent);
            
            Properties props = new Properties();
            props.setProperty("Percentage", Float.toString(percent));
            
            if (data.isPercentageCutoff()) {
                 addAdjustmentResultNodes("Data Filter - Percentage Cutoff Filter", data.getExperiment(), props);
                 addHistory("Percentage Cutoff Filter is ON ( percent = " +Float.toString(percent)+" )");
            } else {
                addHistory("Percentage Cutoff Filter is OFF");
            }
            addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");            
        }
    }
    //add present call noise filter by wwang    
    private void applyPresentCallFilter(){
    	SetPresentCallDialog spcd = new SetPresentCallDialog(getFrame(), data.getPercentageCutoff());
        if (spcd.showModal() == JOptionPane.OK_OPTION) {
            boolean useCutoff = spcd.isCutoffFilterEnabled();
            float percent = spcd.getPercentageCutoff();

            data.setUsePresentCutoff(useCutoff);            
            data.setPercentageCutoff(percent);
            
            Properties props = new Properties();
            props.setProperty("Percentage", Float.toString(percent));
            
            if (data.isPresentCallCutoff()) {
                 addAdjustmentResultNodes("Data Filter - Percentage Cutoff Filter", data.getExperiment(), props);
                 addHistory("Percentage Cutoff Filter is ON ( percent = " +Float.toString(percent)+" )");
            } else {
                addHistory("Percentage Cutoff Filter is OFF");
            }
            addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");            
        }
    	
    }

    //add present call noise filter by wwang    
    private void applyGCOSPercentageFilter(){
    	SetPresentCallDialog spcd = new SetPresentCallDialog(getFrame(), data.getPercentageCutoff());
        if (spcd.showModal() == JOptionPane.OK_OPTION) {
            boolean useCutoff = spcd.isCutoffFilterEnabled();
            float percent = spcd.getPercentageCutoff();

            data.setUseGCOSPercentageCutoff(useCutoff);            
            data.setPercentageCutoff(percent);
            
            Properties props = new Properties();
            props.setProperty("Percentage", Float.toString(percent));
            
            if (data.isGCOSPercentCutoff()) {
                 addAdjustmentResultNodes("Data Filter - Percentage Cutoff Filter", data.getExperiment(), props);
                 addHistory("Percentage Cutoff Filter is ON ( percent = " +Float.toString(percent)+" )");
            } else {
                addHistory("Percentage Cutoff Filter is OFF");
            }
            addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");            
        }
    	
    }
    /* Applies a variance filter
     */
    private void applyVarianceFilter() {
        VarianceFilterDialog dialog = new VarianceFilterDialog(getFrame());
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            Properties props = dialog.getProperties();
            data.setVarianceFilter(props);
                        
            if (data.isVarianceFilter()) {
                addAdjustmentResultNodes("Data Filter - Variance Filter", data.getExperiment(), props); 
                addHistory("Variance Filter is ON ( mode= " + props.getProperty("Filter Mode")+ " value= "+ props.getProperty("Value")+" )");
            } else {
                addHistory("Variance Filter is OFF");
            }
            
            addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");            
        }
    }
    
    private void addAdjustmentResultNodes(String mainTitle, Experiment experiment, Properties props) {
        DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode(mainTitle);
        int [][] cluster = new int[1][experiment.getNumberOfGenes()];
        
        //default indices for viewer, experiment will handle mapping
        for(int i = 0; i < cluster[0].length; i++)
            cluster[0][i] = i;
        
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode(new LeafInfo("Expression Image", new ExperimentViewer(experiment, cluster), new Integer(0)));
        
        filterNode.add(expressionNode);
        filterNode.add(new DefaultMutableTreeNode("Gene/Row Count: "+cluster[0].length));
        
        if(props.size() > 0) {
            String label = "Parameter";
            if(props.size() > 1)
                label += "s";
            DefaultMutableTreeNode parameterNode = new DefaultMutableTreeNode(label);
            Enumeration _enum = props.keys();
            String key;
            while(_enum.hasMoreElements()) {
                key = (String)_enum.nextElement();
                parameterNode.add(new DefaultMutableTreeNode(key+ ": " + (String)(props.get(key))));
            }
            filterNode.add(parameterNode);
        }
        
        this.addNode(analysisNode, filterNode);

        Object [] path = new Object[3];
        path[0] = tree.getRoot();
        path[1] = analysisNode;
        path[2] = filterNode;
        tree.scrollPathToVisible(new TreePath(path));
        //this.addAnalysisResult(filterNode);
    }
    
    
    /**
     * Sets the user specified use detection filter flag.
     * pcahan
     */
    private void onUseDetectionFilter(AbstractButton item) {
        data.setUseDetectionFilter(item.isSelected());
        if (data.isDetectionFilter()) {
            addHistory("Detection Filter (" + data.getDetectionFilter() + ")");
            addAdjustmentResultNodes("Data Filter - Affy Detection Filter", data.getExperiment(), new Properties());             
        } else {
            addHistory("Detection Filter not used.");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    private void onUseFoldFilter(AbstractButton item) {
        data.setUseFoldFilter(item.isSelected());
        if (data.isFoldFilter()) {
            addHistory("Fold Filter (" + data.getDetectionFilter() + ")");
            addAdjustmentResultNodes("Data Filter - Affy Fold Filter", data.getExperiment(), new Properties());             
        } else {
            addHistory("Fold Filter not used.");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    ////////////////////////////////////////////////
    //                                            //
    // Some methods to adjust the framework data. //
    //                                            //
    ////////////////////////////////////////////////
    
    private void onLog2Transform() {
        //data.log10toLog2();
        data.log2Transform();
        fireDataChanged();
        addHistory("Log2 Transform");
    }
    
    private void onNormalizeSpots() {
        data.normalizeSpots();
        fireDataChanged();
        addHistory("Normalize Gene/Row Vectors");
    }
    
    private void onDivideSpotsRMS() {
        data.divideSpotsRMS();
        fireDataChanged();
        addHistory("Divide by Gene/Row RMS");
    }
    
    private void onDivideSpotsSD() {
        data.divideSpotsSD();
        fireDataChanged();
        addHistory("Divide by Gene/Row SD");
    }
    
    // pcahan
    private void onDivideGenesMedian() {
        data.divideGenesMedian();
        fireDataChanged();
        addHistory("Divide by Gene/Row Median");
    }
    
    private void onDivideGenesMean() {
        data.divideGenesMean();
        fireDataChanged();
        addHistory("Divide by Gene/Row Mean");
    }
    
    private void onMeanCenterSpots() {
        data.meanCenterSpots();
        fireDataChanged();
        addHistory("Mean Center by Gene/Row Mean");
    }
    
    private void onMedianCenterSpots() {
        data.medianCenterSpots();
        fireDataChanged();
        addHistory("Median Center by Gene/Row Median");
    }
    
    private void onDigitalSpots() {
        data.digitalSpots();
        fireDataChanged();
        addHistory("Digital Rows");
    }
    
    private void onNormalizeExperiments() {
        data.normalizeExperiments();
        fireDataChanged();
        addHistory("Normalize Sample/Column Vectors");
    }
    
    private void onDivideExperimentsRMS() {
        data.divideExperimentsRMS();
        fireDataChanged();
        addHistory("Divide by Sample/Column RMS");
    }
    
    private void onDivideExperimentsSD() {
        data.divideExperimentsSD();
        fireDataChanged();
        addHistory("Divide by Sample/Column SD");
    }
    
    private void onMeanCenterExperiments() {
        data.meanCenterExperiments();
        fireDataChanged();
        addHistory("Mean Center by Column/Sample Mean");
    }
    
    private void onMedianCenterExperiments() {
        data.medianCenterExperiments();
        fireDataChanged();
        addHistory("Median Center by Sample/Column Median");
    }
    
    private void onDigitalExperiments() {
        data.digitalExperiments();
        fireDataChanged();
        addHistory("Digital Samples");
    }
    
    private void onLog10toLog2() {
        data.log10toLog2();
        fireDataChanged();
        addHistory("Log10 to Log2");
    }
    private void onLog2toLog10() {
        data.log2toLog10();
        fireDataChanged();
        addHistory("Log2 to Log10");
    }
    private void onAdjustIntensities(AbstractButton item) {
        data.setNonZero(item.isSelected());
        fireDataChanged();
    }
    
    /**
     * Saves the framework data ratio values.
     */
    private void onSaveMatrix() {
        try {
            ExperimentUtil.saveExperiment(mainframe, data.getExperiment(), data);
            addHistory("Save Data Matrix to File");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainframe, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    
    /**
     * Sets the user specified ratio scale.
     */
    private void onSetRatioScale() {
        IDisplayMenu menu = menubar.getDisplayMenu();
        SetRatioScaleDialog srsd = new SetRatioScaleDialog(mainframe, framework, menubar, menu.getMaxRatioScale(), menu.getMinRatioScale(), menu.getMidRatioValue(), menu.getUseDoubleGradient());
        if (srsd.showModal() == JOptionPane.OK_OPTION) {
            menubar.setMaxRatioScale(srsd.getUpperLimit());
            menubar.setMinRatioScale(srsd.getLowerLimit());
            menubar.setMidRatioValue(srsd.getMidValue());
            menubar.setUseDoubleGradient(srsd.getUseDoubleGradient());
    
            if(srsd.isGradientStyleAltered() && srsd.getUseDoubleGradient()) 
            	this.menubar.setPositiveCustomGradient(srsd.getPosImage());
               
            fireMenuChanged();
        }
        addHistory("Color Sat. Limits Set: Lower = "+ srsd.getLowerLimit() +" Upper = "+ srsd.getUpperLimit());
    }
    
    /**
     * Removes all published clusters.
     */
    private void onDeleteAll() {
        data.deleteColors();
        if(this.geneClusterManager != null)
            this.geneClusterManager.deleteAllClusters();
        fireDataChanged();
        fireMenuChanged();
        addHistory("Deleted All Gene Clusters");
    }
    
    /**
     * Removes all published Experiment clusters
     */
    private void onDeleteAllExperimentClusters() {
        data.deleteExperimentColors();
        if(this.experimentClusterManager != null)
            this.experimentClusterManager.deleteAllClusters();
        fireDataChanged();
        fireMenuChanged();
        addHistory("Deleted All Sample Clusters");
    }
    

    private void selectNode(DefaultMutableTreeNode node) {
        this.tree.setSelectionPath(new TreePath(node.getPath()));
    }
    
    /**
     *  Allows node additions to tree from objects with a framework reference
     */
    private void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child){
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        model.insertNodeInto(child,parent, parent.getChildCount());
        this.treeScrollPane.getHorizontalScrollBar().setValue(0);
        fireDataChanged();
    }
    
    /***********
     *
     * Cluster saving and repository code
     *
     */
    
    /**
     * Stores a cluster with specified indices.
     */
    private Color storeCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        if(!(leafInfo instanceof LeafInfo))
            return null;
        if(path.getPathCount() < 3)
            return null;
        Cluster cluster;
        Color clusterColor = null;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null){
                this.geneClusterRepository = new ClusterRepository(data.getFeaturesSize(), framework, true);
                this.data.setGeneClusterRepository(this.geneClusterRepository);
            }
            cluster = geneClusterRepository.storeCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null) {
                clusterColor = cluster.getClusterColor();
                if(geneClusterManager == null){
                    this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
                    DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
                    addNode(this.clusterNode, genesNode);
                } else {
                    geneClusterManager.onRepositoryChanged(geneClusterRepository);
                }
            }
            geneClusterRepository.printRepository();
        } else {
            if(this.experimentClusterRepository == null){
                this.experimentClusterRepository = new ClusterRepository(data.getFeaturesCount(), framework);
                this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            }
            cluster = experimentClusterRepository.storeCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null) {
                clusterColor = cluster.getClusterColor();
                if(experimentClusterManager == null){
                    this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
                    DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Sample Clusters", this.experimentClusterManager), false);
                    addNode(this.clusterNode, experimentNode);
                } else {
                    experimentClusterManager.onRepositoryChanged(experimentClusterRepository);
                }
            }
            experimentClusterRepository.printRepository();
        }
        
        if(cluster != null) {
            int serNum = cluster.getSerialNumber();
            String algName = cluster.getAlgorithmName();
            
            if(clusterType == Cluster.GENE_CLUSTER)
                addHistory("Save Gene Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
            else
                addHistory("Save Experiment Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
        }
        
        fireDataChanged();
        tree.repaint();
        return clusterColor;
    }
    
    /**
     * Stores cluster with provieded indices, allows storage if indices are a subset of
     * the displayed clusters (as in <code>HCLViewer</code>
     */
    private Color storeSubCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        if(!(leafInfo instanceof LeafInfo))
            return null;
        if(path.getPathCount() < 3)
            return null;
        Cluster cluster;
        Color clusterColor = null;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null){
                this.geneClusterRepository = new ClusterRepository(data.getFeaturesSize(), framework, true);
                this.data.setGeneClusterRepository(this.geneClusterRepository);
            }
            cluster = geneClusterRepository.storeSubCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(geneClusterManager == null){
                    this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
                    DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
                    addNode(this.clusterNode, genesNode);
                } else{
                    geneClusterManager.onRepositoryChanged(geneClusterRepository);
                }
            }
            geneClusterRepository.printRepository();
        } else {
            if(this.experimentClusterRepository == null){
                this.experimentClusterRepository = new ClusterRepository(data.getFeaturesCount(), framework);
                this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            }
            cluster = experimentClusterRepository.storeSubCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(experimentClusterManager == null){
                    this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
                    DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Sample Clusters", this.experimentClusterManager), false);
                    addNode(this.clusterNode, experimentNode);
                } else{
                    experimentClusterManager.onRepositoryChanged(experimentClusterRepository);
                }
            }
            experimentClusterRepository.printRepository();
        }
        //Record history
        if(cluster != null) {
            int serNum = cluster.getSerialNumber();
            String algName = cluster.getAlgorithmName();
            
            if(clusterType == Cluster.GENE_CLUSTER)
                addHistory("Save Gene Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
            else
                addHistory("Save Experiment Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
        }
        
        fireDataChanged();
        tree.repaint();
        return clusterColor;
    }
    
    /**
     *  Stores cluster to manager but doesn't link to a particular viewer node.
     */
    public void storeOperationCluster(String source, String clusterID, int [] indices, boolean geneCluster){
        
        ClusterRepository repository;
        Cluster cluster;
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", source, clusterID);
        
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            Experiment experiment;
            if (geneCluster) {
                FloatMatrix matrix = data.getFullExperiment().getMatrix();
                experiment = new Experiment(matrix, indices);
                repository = getClusterRepository(Cluster.GENE_CLUSTER);
                cluster = new Cluster(indices, source, dialog.getLabel(), clusterID, "", dialog.getDescription(), -1, repository.takeNextClusterSerialNumber(), dialog.getColor(), experiment);
                repository.addCluster(repository.getClusterOperationsList(), cluster);
                //geneClusterManager.onRepositoryChanged(geneClusterRepository);
                geneClusterManager.addCluster(cluster);
                
            } else {
                experiment = data.getFullExperiment();
                repository = getClusterRepository(Cluster.EXPERIMENT_CLUSTER);
                cluster = new Cluster(indices, "Search Result", dialog.getLabel(), "Selected Samples", "", dialog.getDescription(), -1, repository.takeNextClusterSerialNumber(), dialog.getColor(), experiment);
                repository.addCluster(repository.getClusterOperationsList(), cluster);
                //experimentClusterManager.onRepositoryChanged(experimentClusterRepository);
                experimentClusterManager.addCluster(cluster);
            }
            
            if(cluster != null) {
                int serNum = cluster.getSerialNumber();
                String algName = cluster.getAlgorithmName();
                
                if(geneCluster)
                    addHistory("Save Gene Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                    algName+", Cluster: "+clusterID);
                else
                    addHistory("Save Experiment Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                    algName+", Cluster: "+clusterID);
            }
        }
        refreshCurrentViewer();
    }
    
    /** Removes a cluster based on indices, Experiment, and cluster type.
     * Returns true if found and removed.
     */
    public boolean removeCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        boolean removed = false;
        if(!(leafInfo instanceof LeafInfo))
            return removed;
        if(path.getPathCount() < 3)
            return removed;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null)
                return removed;
            removed = this.geneClusterRepository.removeCluster(indices, algorithmName, clusterID);
            this.geneClusterManager.onRepositoryChanged(this.geneClusterRepository);
        } else {
            if(this.experimentClusterRepository == null)
                return removed;
            removed = this.experimentClusterRepository.removeCluster(indices, algorithmName, clusterID);
            this.experimentClusterManager.onRepositoryChanged(this.experimentClusterRepository);
        }
        if(removed)
            fireDataChanged();
        
        return removed;
    }
    
    
    public boolean removeSubCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        boolean removed = false;
        if(!(leafInfo instanceof LeafInfo))
            return removed;
        if(path.getPathCount() < 3)
            return removed;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null)
                return removed;
            removed = this.geneClusterRepository.removeSubCluster(indices, algorithmName, clusterID);
            this.geneClusterManager.onRepositoryChanged(this.geneClusterRepository);
        } else {
            if(this.experimentClusterRepository == null)
                return removed;
            removed = this.experimentClusterRepository.removeSubCluster(indices, algorithmName, clusterID);
            this.experimentClusterManager.onRepositoryChanged(this.experimentClusterRepository);
        }
        if(removed)
            fireDataChanged();
        
        return removed;
    }

    /** Launches a new MultipleArrayViewer containing the indexed items, Experiment, and cluster type.
     * clusterType specifies whether the indices relate to genes or samples
     */
    private void launchNewMAV(int [] indices, Experiment experiment, String label, int clusterType){
        MultipleArrayData newData;
        if(indices.length < 1){
            JOptionPane.showMessageDialog(this.getFrame(), "The selected cluster does not contain any members. The new viewer session has been aborted.", "New Session Abort", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if(clusterType == Cluster.GENE_CLUSTER){
            newData = this.data.getDataSubset(indices);
        } else {
            newData = this.data.getDataSubset(indices, experiment.getRowMappingArrayCopy());
        }
        Manager.createNewMultipleArrayViewer(this.menubar, newData, label);
        
        addHistory("Launch New MAV: "+label);
    }
    
    /** Opens the specified cluster node based on parent algorithm name and the cluster's node name
     */
    private void openClusterNode(String algorithmNode, String clusterNode){
        DefaultMutableTreeNode node = findNode(algorithmNode, clusterNode);
        if(node == null){
            return;
        }
        selectNode(node);
    }
    
    
    /** Finds the specified child node given node name (child) and the parent's name.
     */
    private DefaultMutableTreeNode findNode(String parent, String child){
        int childCount = this.analysisNode.getChildCount();
        DefaultMutableTreeNode curr = this.analysisNode;
        DefaultMutableTreeNode target = null;
        for(int i = 0; i < childCount; i++){
            curr = (DefaultMutableTreeNode)(analysisNode.getChildAt(i));
            Object userObject = curr.getUserObject();
            if(userObject instanceof String && ((String)userObject).equals(parent)){
                target = curr;
                break;
            }
            else if(userObject instanceof LeafInfo && (((LeafInfo)userObject).toString()).equals(parent)){
                target = curr;
                break;
            }
        }
        
        if(target == null)
            return null;
        
        childCount = target.getChildCount();
        
        for(int i = 0; i < childCount; i++){
            curr = (DefaultMutableTreeNode)(target.getChildAt(i));
            Object userObject = curr.getUserObject();
            if(userObject instanceof String && ((String)userObject).equals(child)){
                target = curr;
                break;
            }
            else if(userObject instanceof LeafInfo && (((LeafInfo)userObject).toString()).equals(parent)){
                target = curr;
                break;
            }
        }
        
        if(target != curr)
            return null;
        
        return (DefaultMutableTreeNode)curr;
    }
    
    /** Returns the currently selected node
     */
    public DefaultMutableTreeNode getCurrentNode(){
        TreePath path = this.tree.getSelectionPath();
        if(path == null)
            return null;
        return (DefaultMutableTreeNode)path.getLastPathComponent();
    }
    
    public DefaultMutableTreeNode getNode(Object object) {
        return this.tree.getNode(object);
    }
    /** add for sort MAD for auto-color scale
     * by wwang
     * @param m
     * @return
     */
    public float [] initSortedValues(FloatMatrix m) {
		float [] vals = m.getColumnPackedCopy();
		QSort qsort = new QSort(vals, QSort.ASCENDING);
		vals = qsort.getSorted();
	
		int numberNaN = 0;
		
		for(int i = 0; i < vals.length; i++) {		
			if(Float.isNaN(vals[i]))
				numberNaN++;
			else
				break;			
		}
		
		int validN = vals.length-numberNaN;
		
		float [] values = new float[validN];
		
		for(int i = 0; i < values.length; i++) {
			values[i] = vals[i+numberNaN];			
		}
		
		return values;
		
	}
    /** by wwang
	 *  returns the median from the sorted array
	 * @return
	 */
	public float getMedian() {
		float median;
		if(sortedValues.length % 2 == 0)
			median = (sortedValues[sortedValues.length/2] + sortedValues[sortedValues.length/2 + 1])/2.0f;
		else
			median = sortedValues[sortedValues.length/2 + 1];	
		return median;
	}
	
	/** by wwang
	 *  returns the maxScale(80% of total data)from the sorted array
	 * @return
	 */		
	public float getMaxScale() {
		int index=(int)Math.floor(sortedValues.length*0.8);
		float max=sortedValues[index];
		return max;
	}

	
    /**
     *  Handles new data load.  Vector contains ISlideData objects.
     */
    public void fireDataLoaded(ISlideData [] features, int dataType){
    	//add for auto-color scaling format(onedecimalformat)
    	DecimalFormat oneDecimalFormat = new DecimalFormat();
		oneDecimalFormat.setMaximumFractionDigits(1);
		//oneDecimalFormat.setMaximumIntegerDigits(5);
        if(features == null || features.length < 1)
            return;
        if(this.data.getFieldNames() != null && this.data.getFeaturesCount() < 1){
            this.menubar.addLabelMenuItems(this.data.getFieldNames());
            //add the experiment key vector that is longest
            this.menubar.addExperimentLabelMenuItems(getSlideNameKeyVectorUnion(features));
            //this.menubar.addSortMenuItems(this.data.getFieldNames());
            
            //have the main scroll pane and canvas
            ((MultipleArrayCanvas)this.viewer).addSortMenuItems(data.getFieldNames());
  
            this.menubar.setLabelIndex(0);
            
            //pcahan
            if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                this.menubar.addAffyFilterMenuItems();
            }            /**			 * Raktim			 * Place to load CytoBand Information			 * Place to add CGH Menus if datatype is of such type			 * Load CGH Analysis factory here			 */			if(data.isCGHData()) {				loadCytoBandFile();				manager.initCghAnalysiActions(new org.tigr.microarray.mev.cgh.CGHAlgorithms.CGHAlgorithmFactory());				this.menubar.addCGHMenus();				mainframe.validate();            }
        }
        data.addFeatures(features);
        data.setDataType(dataType);
        
        // by wwang add for auto color scaling(for affy data) 
        sortedValues=initSortedValues(data.getExperiment().getMatrix());
        if (TMEV.getDataType() != TMEV.DATA_TYPE_TWO_DYE){
         	this.menubar.setMinRatioScale(0f);
         	//this.menubar.setMidRatioValue(Float.parseFloat(oneDecimalFormat.format(getMedian())));
         	this.menubar.setMidRatioValue(getMedian());
         	//this.menubar.setMaxRatioScale(Float.parseFloat(oneDecimalFormat.format(getMaxScale())));
         	this.menubar.setMaxRatioScale(getMaxScale());
         }
        // if we have field names and data is not loaded
        //if(TMEV.getDataType() == TMEV.DATA_TYPE_AFFY)
        //    this.menubar.addAffyFilterMenuItems();
        
        // pcahan - convoluted but it works
        if ( (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY) &&
        (data.getDataType() == IData.DATA_TYPE_AFFY_ABS) &&
        (!this.menubar.get_affyNormAdded())) {
            this.menubar.addAffyNormMenuItems();
        }
                
        if (data.getDataType() == IData.DATA_TYPE_RATIO_ONLY || data.getDataType() == IData.DATA_TYPE_AFFY_ABS){
            this.menubar.enableNormalizationMenu(false);
        }

        //set IData as primary and selected
        data.setUseMainData(true);
        ((LeafInfo)(mainViewerNode.getUserObject())).setSelectedDataSource(true);
        //record main data as source      
        createDataSelectionNode((DefaultMutableTreeNode)(tree.getRoot().getChildAt(0)), data.getExperiment(), data.getExperiment().getNumberOfGenes(), Cluster.GENE_CLUSTER);              
        tree.repaint();		/**         * Add CGH Specific Views & Menus         */        if(data.isCGHData()) {        	ExperimentsLoaded();        	onFlankingRegionDeterminationChanged();        }        
        setMaxCY3AndCY5();
        systemEnable(TMEV.DATA_AVAILABLE);
        this.viewer.onSelected(framework);
        fireMenuChanged();
        fireDataChanged();
        fireHeaderChanged();
        
        // record it for history's sake
        String [] featureNames = new String[features.length];
        for(int i = 0; i < features.length; i++){
            featureNames[i] = features[i].getSlideFileName();
            if(i == 0)
                addHistory("Load Data File: "+featureNames[i]);
            else {
                if(featureNames[i].equals(featureNames[i-1]))
                    break;
                addHistory("Load Data File: "+featureNames[i]);
            }
        }
        if(features.length > 1)
            addHistory(features.length+" samples loaded.");
        else
            addHistory("1 sample loaded.");
        
        if(features.length > 0)
            addHistory(features[0].getSize()+" genes loaded.");
    }
    
    
    
    /**
     * Returns the key vector for the sample with the longest sample name key list
     */
    private Vector getSlideNameKeyVectorUnion(ISlideData [] features) {
        Vector keyVector;
        Vector fullKeyVector = new Vector();
        String key;
        for( int i = 0; i < features.length; i++) {
            keyVector = features[i].getSlideDataKeys();
            for(int j = 0; j < keyVector.size(); j++) {
                key = (String)(keyVector.elementAt(j));
                if(!fullKeyVector.contains(key))
                    fullKeyVector.addElement(key);
            }
        }
        return fullKeyVector;
    }
    
    /**
     *  Loads data using <code>SuperExpressionFileLoader</code>.
     */
    private void loadData(){
        SuperExpressionFileLoader loader = new SuperExpressionFileLoader(this);
        
        //Add time node to the analysis node
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());        
    }
    
    /**
     * Returns specfied the cluster repository, possibly null
     */
    protected ClusterRepository getClusterRepository(int clusterType){
        if(clusterType == Cluster.GENE_CLUSTER) {
            if(this.geneClusterRepository == null) {
                this.geneClusterRepository = new ClusterRepository(data.getFeaturesSize(), framework, true);
                this.data.setGeneClusterRepository(this.geneClusterRepository);
            }
            
            
            if(geneClusterManager == null){
                this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
                DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
                addNode(this.clusterNode, genesNode);
                
            }
            return this.geneClusterRepository;
            
        } else {
            if(this.experimentClusterRepository == null) {
                this.experimentClusterRepository = new ClusterRepository(data.getFeaturesCount(), framework);
                this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            }
            if(experimentClusterManager == null) {
                this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
                DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Sample Clusters", this.experimentClusterManager), false);
                addNode(this.clusterNode, experimentNode);
            }
            return this.experimentClusterRepository;
        }
    }
    
    /**
     *  Returns the <CODE>ResultTree</CODE> object
     */
    protected ResultTree getResultTree() {
        return this.tree;
    }
    
    
    /**
     * Perform a search for elements based on keys
     */
    private void search() {
        SearchDialog dialog = new SearchDialog(this.getFrame(), this.data.getFieldNames(), this.data.getSlideNameKeyArray());
        if( dialog.showModal() == JOptionPane.OK_OPTION) {
            AlgorithmData searchParameters = dialog.getSearchCriteria();
            
            AlgorithmParameters params = searchParameters.getParams();
            boolean geneSearch = params.getBoolean("gene-search");
            boolean caseSens = params.getBoolean("case-sensitive");
            boolean fullTerm = params.getBoolean("full-term");
            String searchTerm = params.getString("search-term");
            String [] fields = searchParameters.getStringArray("field-names");
            
            ResultTree resultTree = framework.getResultTree();
            
            //get IData indices
            int [] indices = data.search(searchParameters);
            
            if(indices.length > 0) {
                
                //returns Vector of result objects (Vector, Hashtable, Hashtable), #0 ExperimentViewers, #2 TableViewers
                Vector result =  resultTree.findViewerCollection(indices, searchParameters.getParams().getBoolean("gene-search"));
                
                if(result != null) {
                    
                    Vector primaryNodes = (Vector)(result.elementAt(0));
                    Hashtable expViewHash = (Hashtable)(result.elementAt(1));
                    Hashtable tableViewHash = (Hashtable)(result.elementAt(2));
                    
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Search Result Shortcuts");
                    for(int i = 0; i < primaryNodes.size(); i++) {
                        root.add((DefaultMutableTreeNode)(primaryNodes.elementAt(i)));
                    }
                    JTree tree = new JTree(root);
                    tree.setCellRenderer(resultTree.getCellRenderer());
                    
                    SearchResultDialog resultDialog = new SearchResultDialog(this.framework, searchParameters, tree, expViewHash, tableViewHash, indices);
                    resultDialog.showModal();
                } else {
                    SearchResultDialog resultDialog = new SearchResultDialog(this.framework, searchParameters, indices);
                    resultDialog.showModal();
                }
                
                
            } else {
                if(geneSearch)
                    JOptionPane.showMessageDialog(framework.getFrame(), "No genes matching the search criteria were found.", "Empty Search Result", JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(framework.getFrame(), "No samples matching the search criteria were found.", "Empty Search Result", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }
    
    /** Refreshes the current viewer by calling <code>IViewer</code> onSelected(IFramework)
     */
    private void refreshCurrentViewer() {
        TreePath path = tree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        
        Object leafInfo = node.getUserObject();
        
        if(leafInfo instanceof LeafInfo) {
            IViewer viewer = ((LeafInfo)leafInfo).getViewer();
            if(viewer != null) {
                viewer.onSelected(framework);
            }
        }
    }
    
    /** Imports a list of gene or sample identifiers based on matching an annotation key
     * Imported cluster will be added to the proper repository.
     */
    private void onImportList(int clusterType) {
        ClusterRepository cr = getClusterRepository(clusterType);
        
        Cluster cluster = cr.createClusterFromList();
        
        if(cluster != null) {
            if(clusterType == Cluster.GENE_CLUSTER) {
                this.geneClusterManager.onRepositoryChanged(cr);
                addHistory("Save Gene Cluster: Serial #: "+cluster.getSerialNumber()+", Source: List Import");
            } else {
                this.experimentClusterManager.onRepositoryChanged(cr);
                addHistory("Save Sample Cluster: Serial #: "+cluster.getSerialNumber()+", Source: List Import");
            }
            refreshCurrentViewer();
        }
    }
    
    /** Appends Sample annoation.  Loads annoation using the loaded order.
     */
    private void appendSampleAnnotation() {
           
        String msg = "<html><center><h1>Import Sample Annotation</h1></center>";
        msg += "The sample annotation file should be a tab-delimited text file containing one header row for annotation labels (field names).";
        msg += "The file may contain multiple columns of annotation with each column containing a header entry that indicates the nature of the annotation.";
        msg += "The annotation for each sample is organized in rows corresponding to the order of the loaded samples.";
        msg += "If annotation is missing for a sample the entry in that sample row may be left blank.  Please see the manual appendix on file formats for more information. </html>";
        
        HTMLMessageFileChooser dialog = new HTMLMessageFileChooser(getFrame(), "Sample Annotation File Selection", msg, TMEV.getFile("data"), true);
        dialog.setApproveButtonText("Load");
        dialog.setSize(500, 600);
        if(dialog.showModal() == JFileChooser.APPROVE_OPTION) {
            File file = dialog.getSelectedFile();           
            try {
                if(data.addNewSampleLabels(getFrame(), file)) {                            
                    //add the new label to the experiment label menu
                    this.menubar.replaceExperimentLabelMenuItems(data.getSlideNameKeyArray());
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(getFrame(), "Error processing sample annotation file. Check file format.", "Sample Annotation Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }                    
    }
    
    
    /** Appends gene annoation from an annotation file.  Mapping depends on a key selected from the loaded data     
     *
     *  and a second key selected from the annotation file.  These keys map annoation to genes.
     */
    private void appendGeneAnnotation() {
        
        String msg = "<html><center><h1>Import Gene Annotation</h1></center>";
        msg += "Please select an annotation file to import.  The file should contain a column that can be used ";
        msg += "to map annotation in the file to the proper genes.  After file selection you will be asked to identify ";
        msg += "a key from the data and from the input file to be used to insure proper mapping of annotation. ";
        msg += "Note that this file format should conform the MeV annotation file format conventions (.ann) file ";
        msg += "described in the appendix of the manual</html>";
        
        HTMLMessageFileChooser dialog = new HTMLMessageFileChooser(getFrame(), "Gene Annotation File Selection", msg, TMEV.getFile("data"), true);
        dialog.setFileFilter(new AnnFileFilter());
        dialog.setApproveButtonText("Load");
        dialog.setSize(500, 600);

        if(dialog.showModal() == JFileChooser.APPROVE_OPTION) {
            File file = dialog.getSelectedFile();
            try {
                
                //parse the file
                AnnFileParser parser = new AnnFileParser();
                parser.loadFile(file);
                Vector headerVector;
                String [] headers;
                String [][] annMatrix = null;
                if(parser.isAnnFileLoaded()) {
                    headerVector = parser.getColumnHeaders();
                    annMatrix = parser.getDataMatrix(true);                    
                } else {
                    JOptionPane.showMessageDialog(getFrame(), "Error processing gene annotation file. Please check file format.", "Sample Annotation Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                //get field names from current data and add UID as an option
                String [] dataFieldNames = data.getFieldNames();
                String [] fieldNamesWithUID = new String[dataFieldNames.length+1];
                fieldNamesWithUID[0] = "UID";
                for(int i = 0; i < dataFieldNames.length; i++) {
                    fieldNamesWithUID[i+1] = dataFieldNames[i];
                }
                                
                //get annotation keys for mapping
                GeneAnnotationImportDialog importDialog = new GeneAnnotationImportDialog(getFrame(), fieldNamesWithUID, annMatrix[0]);

                //select columns of annoation to append
                
                
                if(importDialog.showModal() == JOptionPane.OK_OPTION) {

                    String [] newFields = importDialog.getSelectedAnnotationFields();
                    
                    int updateCount = data.addNewGeneAnnotation(annMatrix, importDialog.getDataAnnotationKey(), importDialog.getFileAnnotationKey(), newFields);
                    
                    if(updateCount > 0) {
                        
                        //update menubar and TMEV field names
                        TMEV.appendFieldNames(newFields);
                        menubar.replaceLabelMenuItems(data.getFieldNames());
                        
                        //add event to history log
                        String historyMsg = "New Gene Annotation\n";
                        historyMsg += "Annotation File = " + file.getAbsolutePath() + "\n";
                        historyMsg += "New Annotation Fields: ";
                        for(int i = 0; i < newFields.length; i++) {
                            historyMsg += newFields[i];
                            if(i < newFields.length-1)
                                historyMsg += ", ";
                        }
                        addHistory(historyMsg);
                        
                        JOptionPane.showMessageDialog(getFrame(), "<html>Gene annotation has been successfully added.<br>Check the history node for field information.</html>", "Append Gene Annotation", JOptionPane.INFORMATION_MESSAGE);
                        
                    } else {
                        String eMsg = "<html>Gene annotation addition has failed.  The identifying keys in the loaded data ("+importDialog.getDataAnnotationKey()+")<br>";
                        eMsg += "and the keys in the file ("+importDialog.getFileAnnotationKey()+") did not have any matches.<br<br>The new annotation could not be mapped to the data.</html>";
                        JOptionPane.showMessageDialog(getFrame(), eMsg, "Append Gene Annotation", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(getFrame(), "Error processing gene annotation file. Please check file format.", "Sample Annotation Input Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
	/* Start CGH Functions */    /**     * Raktim Nov 11, 05     * Adding CGH Listener Functions     */    public void onSetShowFlankingRegions(boolean show){    	//System.out.println("CGH onSetShowFlankingRegions " + show);    	menubar.setShowFlankingRegions(show);        //fireCghMenuChanged();    	fireMenuChanged();    }	/**     * Raktim     * CGH display element change     */    private void onChangeCghElementLength(double length){    	//System.out.println("CGH onChangeCghElementLength " + length);        menubar.setCghElementLength(length);        //fireCghMenuChanged();        fireMenuChanged();    }	/**     * Raktim     * CGH display element change     */    private void onChangeCghElementLengthOther(){        String s = (String)JOptionPane.showInputDialog(        		new JFrame(), "Enter Element Length", "Element Length", JOptionPane.PLAIN_MESSAGE);        if(s == null || s.length() == 0){            return;        }        try{            int val = Integer.parseInt(s);            onChangeCghElementLength(val);        }catch (NumberFormatException e){            JOptionPane.showMessageDialog(null, "Input Must Be An Integer", "Number Format Error", JOptionPane.PLAIN_MESSAGE);        }    }	/**     * Raktim     * CGH display element change     */    private void onChangeCghElementWidth(int width){    	//System.out.println("CGH onChangeCghElementWidth " + width);        menubar.setCghElementWidth(width);        //fireCghMenuChanged();        fireMenuChanged();    }	/**     * Raktim     * CGH display element change     */    private void onChangeCghElementWidthOther(){    	//System.out.println("onChangeCghElementWidthOther ");    	String s = (String)JOptionPane.showInputDialog(        new JFrame(), "Enter Element Width", "Element Width", JOptionPane.PLAIN_MESSAGE);        if(s == null || s.length() == 0){ return; }        try{            int val = Integer.parseInt(s);            onChangeCghElementWidth(val);        }catch (NumberFormatException e){            JOptionPane.showMessageDialog(null, "Input Must Be An Integer", "Number Format Error", JOptionPane.PLAIN_MESSAGE);        }    }    /**	 * Raktim	 * CGH display type change     */    private void onChangeCghDisplayType(int displayType){    	//System.out.println("CGH onChangeCghDisplayType " + displayType);        menubar.setCghDisplayType(displayType);        //fireCghMenuChanged();    	fireMenuChanged();    	/**    	 * Remember to change setCurrentViewer function    	 */        setCurrentViewer(getCurrentViewer());    }    /**     * Raktim     * CGH display label change     */    private void onChangeDisplayLabelType(int displayLabelType){    	//System.out.println("CGH onChangeDisplayLabelType " + displayLabelType);        menubar.setCghDisplayLabelType(displayLabelType);        //fireCghMenuChanged();        fireMenuChanged();    }    /**     * Raktim     * Sets the p-value threshold for deeming probes as aberrant if using the     * probe value distribution method of copy number determination     */    public void onSetClonePThresh(){    	//System.out.println("CGH onSetClonePThresh ");        SingleValueSelectorDialog dlg = new SingleValueSelectorDialog(framework.getFrame(), menubar.getCloneValueMenu().getClonePThresh() + "");        if(dlg.showModal() == JOptionPane.OK_OPTION){            String value = dlg.getValue();            try{                float val = Float.parseFloat(value);                menubar.setClonePThresh(val);            }catch (NumberFormatException e){                JOptionPane.showMessageDialog(null, "Input Must Be An Number", "Number Format Error", JOptionPane.PLAIN_MESSAGE);            }            fireThresholdsChanged();        }    }    /**     * Raktim     * On CGH threshold change     */    private void fireThresholdsChanged(){        onThresholdsChanged();        IViewer viewer = getCurrentViewer();        if (viewer == null) {            return;        }        doViewLayout();    }    /**     * Raktim     * Event processed when the probe copy determination thresholds     * have changed     */    public void onThresholdsChanged(){        ICGHCloneValueMenu menu = framework.getCghCloneValueMenu();        data.onCopyDeterminationChanged(menu);        onFlankingRegionDeterminationChanged();    }    /**     * Raktim     * event that is processed when the method of calculating     * flanking regions has changed     */    public void onFlankingRegionDeterminationChanged(){        int flankingRegionType = framework.getCghCloneValueMenu().getFlankingRegionType();        calculateFlankingRegions(flankingRegionType);    }    /**     * Raktim     * Calculates flanking regions for the data     * @param flankingRegionType the copy number determination method used to     * calculate the flanking regions     */    public void calculateFlankingRegions(int flankingRegionType){        FlankingRegionCalculator flCalc = new FlankingRegionCalculator();        flCalc.setExperiments(data.getFeaturesList());        flCalc.setData(data);        flCalc.setCopyDeterminationType(flankingRegionType);        flCalc.calculateFlankingRegions();    }    /**     * Raktim     * If CGH circle viewer background changes     */    private void onSetCircleViewerBackground(){    	//System.out.println("CGH onSetCircleViewerBackground ");        Color newColor = javax.swing.JColorChooser.showDialog(        new JFrame(), "Choose Background Color", Color.black);        menubar.setCircleViewerBackgroundColor(newColor);        //fireCghMenuChanged();        fireMenuChanged();    }    /**     * Raktim     * Changes the order in which experiments are displayed in the     * position graph     */    private void onChangeCghDisplayOrder(){    	//System.out.println("CGH onChangeCghDisplayOrder ");    	CGHDisplayOrderChanger changer = new CGHDisplayOrderChanger(data, framework.getFrame(), true);    	changer.show(true);    	if (changer.isCancelled())    		return;        fireDataChanged();    }    /**     * Raktim     * UN-Used     * Need to carefully take care of removing samples.     * Remember to revisit.     */    private void onDeleteSample(){    	System.out.println("CGH onDeleteSample. Code ready not validated");        //ctl.onDeleteSample();    	/*    	CGHSampleDeleter deleter = new CGHSampleDeleter(data, framework.getFrame(), true);        if(deleter.showModal() == JOptionPane.OK_OPTION){            int deletedSampleIndex = deleter.getDeletedIndex();            if(deletedSampleIndex != -1){                deleteSample(deletedSampleIndex);            }        }        fireDataChanged();        */    }    /**     * Raktim     * UN-Used     * Need to revisit for syncing     * @param sampleIndex     */    private void deleteSample(int sampleIndex){        int[] indicesOrder = data.getSamplesOrder();        int deletedSampleIndex = indicesOrder[sampleIndex];        data.getFeaturesList().remove(deletedSampleIndex);        int[] newIndicesOrder = new int[indicesOrder.length - 1];        int curIndex = 0;        for(int i = 0; i < indicesOrder.length; i++){            if(i != sampleIndex){                if(indicesOrder[i] > deletedSampleIndex){                    newIndicesOrder[curIndex] = indicesOrder[i] - 1;                }else{                    newIndicesOrder[curIndex] = indicesOrder[i];                }                curIndex++;            }        }        data.setSamplesOrder(newIndicesOrder);    }    /**     * Raktim     * Clear annotations if any from CGH views     */    private void onClearAnnotations(){    	//System.out.println("CGH onClearAnnotations ");        //ctl.onClearAnnotations();    	data.setAnnotations(new ICGHDataRegion[0][0]);        fireDataChanged();    }    /**     * Raktim     * Changes the method of calculating probe values     * @param cloneValueType     */    public void onChangeCloneValueType(int cloneValueType){    	//System.out.println("CGH onChangeCloneValueType " + cloneValueType);        menubar.setCloneValueType(cloneValueType);        //onChangeCloneValueType(cloneValueType);        fireCloneValuesChanged();    }    /**     * Raktim     * Changes the method of calculating flanking regions     * @param flankingRegionType     */    public void onChangeFlankingRegionType(int flankingRegionType){    	//System.out.println("CGH onChangeFlankingRegionType " + flankingRegionType);        menubar.setFlankingRegionType(flankingRegionType);        onFlankingRegionDeterminationChanged();        fireCloneValuesChanged();    }    /**     * Raktim     * Sets the threshold parameters for copy number determination based     * on probe value thresholds     */    private void onCghSetThresholds(){    	//System.out.println("CGH onCghSetThresholds ");        ICGHCloneValueMenu menu = menubar.getCloneValueMenu();        CGHThresholdSetter setter = new CGHThresholdSetter(getFrame(), menu.getAmpThresh(),        menu.getDelThresh(), menu.getAmpThresh2Copy(), menu.getDelThresh2Copy());        if(setter.showModal() == JOptionPane.OK_OPTION){            menubar.setAmpThresh(setter.getAmpThresh());            menubar.setDelThresh(setter.getDelThresh());            menubar.setAmpThresh2Copy(setter.getAmpThresh2Copy());            menubar.setDelThresh2Copy(setter.getDelThresh2Copy());            fireThresholdsChanged();        }    }    /**     * Raktim     * Renames a selected navigation tree node.     */    private void onRenameNode() {        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();        if (node == null || node.getParent() == null) {            return;        }        if(node.getUserObject() instanceof org.tigr.microarray.mev.cluster.gui.LeafInfo){            String name = JOptionPane.showInputDialog(getFrame(), "Enter New Name");            if(name != null && name.length() > 0){                ((org.tigr.microarray.mev.cluster.gui.LeafInfo)node.getUserObject()).setName(name);                ((DefaultTreeModel)tree.getModel()).nodeChanged(node);            }        }else if(node.getUserObject() instanceof String){            String name = JOptionPane.showInputDialog(getFrame(), "Enter New Name");            if(name != null && name.length() > 0){                node.setUserObject(name);                ((DefaultTreeModel)tree.getModel()).nodeChanged(node);            }        }    }    /**     * Raktim     * Adds a CGH Data View     */    private void addDataView(DefaultMutableTreeNode node) {    	System.out.println("addDataView ");        if (node == null) {            return;        }        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();        //treeModel.insertNodeInto(node, mainViewNode, mainViewNode.getChildCount());        if (!mainViewerNode.getAllowsChildren()){        	mainViewerNode.setAllowsChildren(true);        	treeModel.insertNodeInto(node, mainViewerNode, mainViewerNode.getChildCount());        	mainViewerNode.setAllowsChildren(false);        } else {        	treeModel.insertNodeInto(node, mainViewerNode, mainViewerNode.getChildCount());        }        TreeSelectionModel selModel = tree.getSelectionModel();        TreePath treePath = new TreePath(node.getPath());        selModel.setSelectionPath(treePath);        tree.scrollPathToVisible(treePath);    }    /**     * Raktim     * Remember to revisit CGH algorithims after views are ready     * @param action     */    private void onCghAnalysis(Action action){    	//System.out.println("onCghAnalysis ");    	String className = (String)action.getValue(ActionManager.PARAMETER);        try {            Class clazz = Class.forName(className);            NumberOfAlterationsCalculator gui = (NumberOfAlterationsCalculator)clazz.newInstance();            DefaultMutableTreeNode result = gui.execute(framework);            addAnalysisResult(result);        } catch (ClassCastException e) {            System.out.println("Error: org.tigr.microarray.mev.cluster.gui.IClusterGUI interface is expected.");            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);        } catch (Exception e) {            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);        }    }    /**     * Raktim     * ICGHListener impl     * Event that is processed what the underlying data     * has changed     */    public void onDataChanged() {        fireDataChanged();    }    /**     * Raktim     * ICGHListener impl     * Event that is processed when the method of     * probe value calculation has changed     */    public void onCloneValuesChanged(){        fireCloneValuesChanged();    }    /**     * Raktim     * Once CGH clone values have been updated     */    private void fireCloneValuesChanged(){    	//System.out.println("fireCloneValuesChanged ");        IViewer viewer = getCurrentViewer();        if(viewer instanceof ICGHViewer){            ((ICGHViewer)viewer).onCloneValuesChanged(framework.getCghCloneValueMenu());            doViewLayout();        }    }    /**     * Raktim     * @param boolean show     * Removes/Adds the viewer header     */    private void onShowHeader(boolean show){    	//System.out.println("onShowHeader ");    	if(show){            getCurrentViewer().getHeaderComponent().setVisible(show);            viewScrollPane.getColumnHeader().setVisible(show);        }else{            getCurrentViewer().getHeaderComponent().setVisible(show);            viewScrollPane.getColumnHeader().setVisible(show);        }    }    /**     * Raktim     * ICGHListener Impl     * Event that is processed when a chromosome is selected to be displayed     * @param eventObj     */    public void onChromosomeSelected(java.util.EventObject eventObj) {    	//System.out.println("onChromosomeSelected ");        int chromosomeIndex = ((Integer)eventObj.getSource()).intValue();        TreeNode selectedNode = mainViewerNode.getChildAt(1).getChildAt(chromosomeIndex);        tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)selectedNode).getPath()));    }    /**     * Raktim     * ICGHListener Impl     * Event that is processed when clone distributions have been loaded     * Remember to re-visit     */    public void CloneDistributionsLoaded() {    	System.out.println("onCloneDistributionsLoaded ");        //menubar.onCloneDistributionsLoaded();    }    /**     * Raktim     * Remember     * Event that is processed when the window has been closed     * Clash with onClose() of MAV So renamed to onCGHClose() for time being     */    protected void onCGHClose(){    	/*        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();        fireOnCloseEvent((DefaultMutableTreeNode)model.getRoot());        mainframe.dispose();        System.exit(0);        */    }    /**     * Raktim     * Getter for property menubar.     * @return Value of property menubar.     */    public MultipleArrayMenubar getMenubar() {        return menubar;    }    /**     * Raktim     * Setter for property menubar.     * @param menubar New value of property menubar.     */    //public void setMenubar(org.abramson.microarray.cgh.CGHMenubar menubar) {    public void setMenubar(MultipleArrayMenubar menubar) {        //this.menubar = menubar;        //super.menubar = menubar;    }    /**     * Raktim Nov 01, 2005     * CGH Event Handler     * Not Used Merged with MeV fireMenuChanged()    private void fireCghMenuChanged(){        IViewer viewer = getCurrentViewer();        if(viewer instanceof ICGHViewer){            //((ICGHViewer)viewer).onMenuChanged(((CGHMenubar)menubar).getCghDisplayMenu());        	((ICGHViewer)viewer).onMenuChanged(menubar.getCghDisplayMenu());            doViewLayout();        }    }    */    /**     * Raktim Nov 02, 2005     * Function to create Chr & Expr views     * CGH Specific     *     */    private Vector initializeViews(){        DefaultMutableTreeNode experimentViews = new DefaultMutableTreeNode(new LeafInfo("Experiment Views"), true);        DefaultMutableTreeNode chromosomeViews = new DefaultMutableTreeNode(new LeafInfo("Chromosome Views"), true);        /**         * Circle Viewer         */        for(int i = 0; i < data.getFeaturesCount(); i++){            CGHCircleViewerPanel circlePanel = new CGHCircleViewerPanel(new CGHCircleViewerModel(framework));            circlePanel.setExperimentIndex(i);            circlePanel.setBackground(Color.darkGray);            circlePanel.setDrsListener((EventListener)manager.getListener());            String name = data.getSampleName(i);            DefaultMutableTreeNode circleViewerNode = new DefaultMutableTreeNode(new LeafInfo(name, circlePanel), false);            experimentViews.add(circleViewerNode);        }        /**         * Raktim         * Chromomosome Viewer         */        CGHPositionGraphCombinedHeader combinedHeader = new CGHPositionGraphCombinedHeader(new java.awt.Insets(0,0,0,0));        for(int i = 0; i < data.getNumChromosomes(); i++){            CGHPositionGraphDataModel posModel = new CGHPositionGraphDataModel(/*fcd,*/ framework, i);            CGHAnnotationsModel annotationsModel = new CGHAnnotationsModel(/*fcd,*/ framework, i);            CGHPositionGraphViewer posGraphViewer = new CGHPositionGraphViewer(framework, combinedHeader);            posGraphViewer.setPositionGraphModel(posModel);            posGraphViewer.setAnnotationsModel(annotationsModel);            posGraphViewer.setCytoBandsModel(framework.getCytoBandsModel());            posGraphViewer.setDrsListener((EventListener)manager.getListener());            String name = CGHUtility.convertChromToLongString(i+1, data.getCGHSpecies());            DefaultMutableTreeNode chromosomeViewerNode = new DefaultMutableTreeNode(new LeafInfo(name, posGraphViewer), false);            chromosomeViews.add(chromosomeViewerNode);        }        Vector viewerNodes = new Vector(2);        viewerNodes.add(experimentViews);        viewerNodes.add(chromosomeViews);        //fireExperimentsLoaded(new EventObject(viewerNodes));        //onExperimentsLoaded(new EventObject(viewerNodes));        return viewerNodes;    }    /**     * Raktim     * IDataRegionSelection function impl for CGH view     * Imported from MevCtl class     * Shows a dialog displaying all of the data values in a data region     * @param eventObj contains the CGHDataRegionInfo object specifying the region for which     * to display the data values     */    public void DisplayDataValues(EventObject eventObj) {        CGHDataRegionInfo dataRegionInfo = (CGHDataRegionInfo)eventObj.getSource();        CGHDataValuesDisplay display = new CGHDataValuesDisplay(framework.getFrame(), true);        display.setData(data);        display.setDataRegionInfo(dataRegionInfo);        display.createTextDocument();        display.show();    }    /**     * Raktim     * Finds a Gene in RefGene DB and displays in how many samy samples its     * copy number has altered.     */    public void searchForGene(){        String geneName = (String)JOptionPane.showInputDialog(framework.getFrame(), "", "Enter Gene Name", JOptionPane.PLAIN_MESSAGE);        if(geneName == null || geneName.length() == 0){            return;        }        GeneDataSet geneDataSet = new GeneDataSet();        geneDataSet.loadGeneDataByGeneName(geneName, data.getCGHSpecies());        Vector vecGeneData = geneDataSet.getGeneData();        showGeneDataDlg(vecGeneData);    }    /**     * Raktim     * IDataRegionSelection function impl for CGH view     * Imported from MevCtl class     * Displays the genes in a data region     * @param eventObj contains the CGHDataRegionInfo object specifying the region for which     * to display the genes     */    public void ShowGenes(EventObject eventObj) {    	//Raktim    	System.out.println("Show Genes in Region");        CGHDataRegionInfo dataRegionInfo = (CGHDataRegionInfo)eventObj.getSource();        ICGHDataRegion dataRegion = dataRegionInfo.getDataRegion();        DataRegionGeneData geneDataSet = new DataRegionGeneData(dataRegion);        geneDataSet.loadGeneData(data.getCGHSpecies());        Vector vecGeneData = geneDataSet.getGeneData();        //Raktim        //System.out.println("Vector vecGeneData size: " + vecGeneData.size());        showGeneDataDlg(vecGeneData);    }    /**     * Raktim     * Helper for above     * @param vecGeneData     */    private void showGeneDataDlg(Vector vecGeneData){        GeneAmplifications amps = new GeneAmplifications(framework);        amps.setData(data);        GeneDeletions dels = new GeneDeletions(framework);        dels.setData(data);        Iterator it = vecGeneData.iterator();        Vector alterationRegions = new Vector();        while(it.hasNext()){            AlterationRegion alterationRegion = new AlterationRegion();            ICGHDataRegion dataRegion = (ICGHDataRegion)it.next();            alterationRegion.setDataRegion(dataRegion);            alterationRegion.setNumDeletions(dels.getNumAlterations(dataRegion));            alterationRegion.setNumAmplifications(amps.getNumAlterations(dataRegion));            alterationRegion.setNumSamples(data.getFeaturesCount());            alterationRegions.add(alterationRegion);        }        NumberOfDeletionsAmpilficationsDataModel dataModel = new NumberOfDeletionsAmpilficationsDataModel();        dataModel.setAlterationRegions((AlterationRegion[])alterationRegions.toArray(new AlterationRegion[(alterationRegions.size())]));        NumberOfAlterationsViewer viewer = new NumberOfAlterationsViewer();        viewer.setData(data);        viewer.addDrsListener((EventListener)manager.getListener());        viewer.setDataModel(dataModel);        JDialog dlg = new JDialog(framework.getFrame(), "Gene Alterations");        dlg.getContentPane().add(viewer);        dlg.setJMenuBar((JMenuBar)viewer.getHeaderComponent());        dlg.setSize(1000, 500);        GuiUtil.center(dlg);        dlg.show();    }    /**     * Raktim     * IDataRegionSelection function impl for CGH view     * Imported from MevCtl class     * Launches the CGH Browser     * @param eventObj contains the CGHDataRegionInfo object specifying     * the region to be displayed and highlighted on the browser     */    public void ShowBrowser(EventObject eventObj) {        CGHDataRegionInfo dataRegionInfo = (CGHDataRegionInfo)eventObj.getSource();        int experimentIndex = dataRegionInfo.getExperimentIndex();        ICGHDataRegion dataRegion = dataRegionInfo.getDataRegion();        int chromosomeIndex = dataRegion.getChromosomeIndex();        CGHChartDataModel chartModel;        CGHTableDataModel tableModel;        int browserCloneValues;        if(data.isHasDyeSwap()){            chartModel = new CGHChartDataModelDyeSwap(/*fcd,*/ framework, experimentIndex, chromosomeIndex);            tableModel = new CGHTableDataModelDyeSwap(/*fcd,*/ framework, experimentIndex, chromosomeIndex);            browserCloneValues = TMEV.browserDefaultDyeSwapValue;        }else{            chartModel = new CGHChartDataModelNoDyeSwap(/*fcd,*/ framework, experimentIndex, chromosomeIndex);            tableModel = new CGHTableDataModelNoDyeSwap(/*fcd,*/ framework, experimentIndex, chromosomeIndex);            browserCloneValues = TMEV.browserDefaultNoDyeSwapValue;        }        CGHBrowser browser = new CGHBrowser(data, experimentIndex, chromosomeIndex, chartModel, tableModel, browserCloneValues, data.isHasDyeSwap());        browser.show();        browser.setSelectedRegion(dataRegion);    }    /**     * Raktim     * IDataRegionSelection function impl for CGH view     * Imported from MevCtl class     * @param eventObj     */    public void AnnotationsSelected(EventObject eventObj) {        ICGHDataRegion[][] annotationRegions = (ICGHDataRegion[][])eventObj.getSource();        int chromIndex = getMinChromosomeIndex(annotationRegions);        if(chromIndex != -1){            fireChromosomeSelected(new EventObject(new Integer(chromIndex)));        }    }    /**     * Raktim     * @param annotationRegions     * @return     */    public int getMinChromosomeIndex(ICGHDataRegion[][] annotationRegions){        for(int i = 0; i < annotationRegions.length; i++){            if(annotationRegions[i].length > 0){                return i;            }        }        return -1;    }    /**     * Raktim     * @param eventObject     */    private void fireChromosomeSelected(EventObject eventObject){    	/*        Iterator it = cghArrayViewerListeners.iterator();        while(it.hasNext()){            ((ICGHListener)it.next()).onChromosomeSelected(eventObject);        }        */    	this.onChromosomeSelected(eventObject);    }    /**     * ICGHListenr Impl     * Raktim CGH     */    /**     * Not needed moved to onExperimentsLoaded(...)    public void onExperimentsInitialized(java.util.EventObject eventObj){        //initialize the clone values menu after the experiments have been loaded in order to        //create different menus based on the type of experiment that has been loaded.  For now,        //just dye swap or non dye swap experiments    	menubar.initCloneValuesMenu(data.isHasDyeSwap());    }    */    /**     * Raktim     * ICGHListner Impl     * Notifies viewer that experiments have been loaded     * Called from MAV.fireDataLoaded     * @param eventObj     */    public void ExperimentsLoaded(){        //initialize the clone values menu after the experiments have been loaded in order to        //create different menus based on the type of experiment that has been loaded. Moved    	//from onExperimentsInitialized(java.util.EventObject eventObj) to here    	menubar.initCloneValuesMenu(data.isHasDyeSwap());    	menubar.enableCloneDistributions(data.hasCloneDistribution(), data.isLog2Data());    	ICGHCloneValueMenu menu = framework.getCghCloneValueMenu();    	//Remember to replace with data.isHasDyeSwap()    	//data.setHasDyeSwap(TMEV.hasDyeSwap);    	//data.setCGHCopyNumberCalculator();        data.onCopyDeterminationChanged(menu);        //initializeViews() creates Chr & Circle viewers        //Vector viewerNodes = (Vector)eventObj.getSource();        Vector viewerNodes = initializeViews();        //Delete existing nodes from the main view        //removeChildren(mainViewNode);        removeChildren(mainViewerNode);        removeChildren(analysisNode);        Iterator it = viewerNodes.iterator();        while(it.hasNext()){            addDataView((DefaultMutableTreeNode)it.next());        }        tree.repaint();        //systemEnable(TMEV.DATA_AVAILABLE);    }    /**     * Raktim     * CGH Cytobands     */    private void loadCytoBandFile(){    	CytoBands cytoBands = new CytoBands();    	File cytoBandsFile = null;    	if (data.getCGHSpecies() == TMEV.CGH_SPECIES_HS)    		cytoBandsFile = new File("Data/Hs_CytoBands.txt");    	else if (data.getCGHSpecies() == TMEV.CGH_SPECIES_MM)    		cytoBandsFile = new File("Data/Mm_CytoBands.txt");        cytoBands.loadAllCytoBands(cytoBandsFile, data.getCGHSpecies());        cytoBandsModel = new CytoBandsModel(cytoBands);    }    /**     * Raktim     * Remove a node from View     */    protected void removeChildren(DefaultMutableTreeNode removeNode){        while(removeNode.getChildCount() > 0){            DefaultMutableTreeNode node = (DefaultMutableTreeNode)removeNode.getFirstChild();            if (node == null || node.getParent() == null) {                return;            }            fireOnCloseEvent(node);            TreePath parentPath = new TreePath(((DefaultMutableTreeNode)node.getParent()).getPath());            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);            ((TreeSelectionModel)tree.getSelectionModel()).setSelectionPath(parentPath);            tree.scrollPathToVisible(parentPath);        }    }	/* End CGH Functions*/ 


    /**
     * The listener to listen to mouse, action, tree, keyboard and window events.
     */
    private class EventListener extends MouseAdapter implements ActionListener, TreeSelectionListener, KeyListener, WindowListener, java.io.Serializable, IDataRegionSelectionListener, ICGHListener  {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(ActionManager.CLOSE_COMMAND)) {
                onClose();
            } else if (command.equals(ActionManager.LOAD_FILE_COMMAND)) {
                onLoadFile();
            } else if (command.equals(ActionManager.LOAD_EXPRESSION_COMMAND)) {
                //onLoadExpressionFile();
            } else if (command.equals(ActionManager.LOAD_DIRECTORY_COMMAND)) {
                onLoadDirectory();
            } else if (command.equals(ActionManager.LOAD_COMMAND)) {
                loadData();
            }else if (command.equals(ActionManager.LOAD_CLUSTER_COMMAND)) {
                onLoadCluster();
            } else if (command.equals(ActionManager.LOAD_STANFORD_COMMAND)) {
                onLoadStanford();
            } else if (command.equals(ActionManager.LOAD_DB_COMMAND)) {
                onLoadDatabase();
            } else if(command.equals(ActionManager.TOGGLE_ABBR_EXPT_NAMES_CMD)) {
                data.toggleExptNameLength();
                fireDataChanged();
                fireMenuChanged();
                fireHeaderChanged();
                doViewLayout();
            } else if (command.equals(ActionManager.DISPLAY_LABEL_CMD)) {
                onLabelChanged((Action)event.getSource());
            } else if (command.equals(ActionManager.DISPLAY_EXPERIMENT_LABEL_CMD)) {
                onExperimentLabelChanged((Action)event.getSource());
            } else if (command.equals(ActionManager.ADD_NEW_EXPERIMENT_LABEL_CMD)) {
                onExperimentLabelAdded();
            } else if (command.equals(ActionManager.DISPLAY_10X10_CMD)) {
                onElementSizeChanged(10, 10);
            } else if (command.equals(ActionManager.DISPLAY_20X5_CMD)) {
                onElementSizeChanged(20,  5);
            } else if (command.equals(ActionManager.DISPLAY_50X10_CMD)) {
                onElementSizeChanged(50, 10);
            } else if (command.equals(ActionManager.DISPLAY_5X2_CMD)) {
                onElementSizeChanged( 5,  2);
            } else if (command.equals(ActionManager.DISPLAY_OTHER_CMD)) {
                onElementSizeChanged();
            } else if (command.equals(ActionManager.GREEN_RED_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.GREEN_RED_SCHEME);
            } else if (command.equals(ActionManager.BLUE_YELLOW_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.BLUE_YELLOW_SCHEME);
            } else if (command.equals(ActionManager.RAINBOW_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.RAINBOW_COLOR_SCHEME);
            } else if (command.equals(ActionManager.CUSTOM_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.CUSTOM_COLOR_SCHEME);
            } else if (command.equals(ActionManager.COLOR_GRADIENT_CMD)){
                onColorGradientChange(((javax.swing.JCheckBoxMenuItem)(event.getSource())).isSelected());
            } else if (command.equals(ActionManager.DISPLAY_DRAW_BORDERS_CMD)) {
                onDrawBorders();
            } else if (command.equals(ActionManager.SYSTEM_INFO_CMD)) {
                onSystemInfo();
            } else if (command.equals(ActionManager.DEFAULT_DISTANCES_CMD)) {
                onDefaultDistance();
            } else if (command.equals(ActionManager.TOTAL_INTENSITY_CMD)) {
                onNormalize(ISlideData.TOTAL_INTENSITY);
            } else if (command.equals(ActionManager.LINEAR_REGRESSION_CMD)) {
                onNormalize(ISlideData.LINEAR_REGRESSION);
            } else if (command.equals(ActionManager.RATIO_STATISTICS_CMD)) {
                onNormalize(ISlideData.RATIO_STATISTICS_99);
            } else if (command.equals(ActionManager.ITERATIVE_LOG_CMD)) {
                onNormalize(ISlideData.ITERATIVE_LOG);
            } else if (command.equals(ActionManager.TOTAL_INTENSITY_LIST_CMD)) {
                onNormalizeList(ISlideData.TOTAL_INTENSITY_LIST);
            } else if (command.equals(ActionManager.LINEAR_REGRESSION_LIST_CMD)) {
                onNormalizeList(ISlideData.LINEAR_REGRESSION_LIST);
            } else if (command.equals(ActionManager.RATIO_STATISTICS_LIST_CMD )) {
                onNormalizeList(ISlideData.RATIO_STATISTICS_99_LIST);
            } else if (command.equals(ActionManager.ITERATIVE_LOG_LIST_CMD)) {
                onNormalizeList(ISlideData.ITERATIVE_LOG_LIST);
            } else if (command.equals(ActionManager.NO_NORMALIZATION_CMD)) {
                onNormalize(ISlideData.NO_NORMALIZATION);
            } else if (command.equals(ActionManager.SAVE_IMAGE_COMMAND)) {
                onSaveImage();
            } else if (command.equals(ActionManager.PRINT_IMAGE_COMMAND)) {
                onPrintImage();
            } else if (command.equals(ActionManager.ANALYSIS_COMMAND)) {
                onAnalysis((Action)event.getSource());
            } else if (command.equals(ActionManager.DEFAULT_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.DEFAULT);
            } else if (command.equals(ActionManager.PEARSON_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSON);
            } else if (command.equals(ActionManager.PEARSON_UNCENTERED_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSONUNCENTERED);
            } else if (command.equals(ActionManager.PEARSON_SQUARED_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSONSQARED);
            } else if (command.equals(ActionManager.COSINE_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.COSINE);
            } else if (command.equals(ActionManager.COVARIANCE_VALUE_CMD)) {
                menubar.setDistanceFunction(Algorithm.COVARIANCE);
            } else if (command.equals(ActionManager.EUCLIDEAN_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.EUCLIDEAN);
            } else if (command.equals(ActionManager.AVERAGE_DOT_PRODUCT_CMD)) {
                menubar.setDistanceFunction(Algorithm.DOTPRODUCT);
            } else if (command.equals(ActionManager.MANHATTAN_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.MANHATTAN);
            } else if (command.equals(ActionManager.MUTUAL_INFORMATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.MUTUALINFORMATION);
            } else if (command.equals(ActionManager.SPEARMAN_RANK_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.SPEARMANRANK);
            } else if (command.equals(ActionManager.KENDALLS_TAU_CMD)) {
                menubar.setDistanceFunction(Algorithm.KENDALLSTAU);
            } else if (command.equals(ActionManager.ABSOLUTE_DISTANCE_CMD)) {
                menubar.setDistanceAbsolute(((AbstractButton)event.getSource()).isSelected());
            } else if (command.equals(ActionManager.DELETE_NODE_CMD)) {
                onDeleteNode();
            } else if (command.equals(ActionManager.SET_DATA_SOURCE_COMMAND)) {
                Object source = event.getSource();
                if(source instanceof JCheckBoxMenuItem)
                    onSetData(((JCheckBoxMenuItem)event.getSource()).isSelected());
                else
                    onSetData(true);  //reset main view
            } else if (command.equals(ActionManager.USE_PERCENTAGE_CUTOFFS_CMD)) {
                applyPercentageCutoffs();
                //add mas5 present call noise filter
            }else if(command.equals(ActionManager.USE_PRESENT_CALL_CMD)){   
            	applyPresentCallFilter();
            }else if(command.equals(ActionManager.USE_GCOS_PERCENTAGE_CUTOFF_CMD)){   
            	applyGCOSPercentageFilter();	
            } else if (command.equals(ActionManager.USE_LOWER_CUTOFFS_CMD)) {
                applyLowerCutoffs();
            } else if (command.equals(ActionManager.USE_VARIANCE_FILTER_CMD)) {
                applyVarianceFilter();
            } else if (command.equals(ActionManager.IMPORT_GENE_LIST_COMMAND)) {
                onImportList(Cluster.GENE_CLUSTER);
            } else if (command.equals(ActionManager.IMPORT_SAMPLE_LIST_COMMAND)) {
                onImportList(Cluster.EXPERIMENT_CLUSTER);
            }
            
            // pcahan
            /*else if (command.equals(ActionManager.SET_DETECTION_FILTER_CMD)) {
                onSetDetectionFilter();
            } else if (command.equals(ActionManager.SET_FOLD_FILTER_CMD)) {
                onSetFoldFilter();
            } else if (command.equals(ActionManager.USE_DETECTION_FILTER_CMD)) {
                onUseDetectionFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.USE_FOLD_FILTER_CMD)) {
                onUseFoldFilter( (AbstractButton) event.getSource());
            }
             */
//          vu 7.22.05
	        else if ( command.equals( ActionManager.RAMA_CMD ) ) {
	                 onRama();
	       } else if( command.equals( ActionManager.RAMA_DOC_CMD ) ) {
	                 onRamaDoc();
	             }
            
            // pcahan
            else if (command.equals(ActionManager.SET_DETECTION_FILTER_CMD)) {
                onSetDetectionFilter();
            } else if (command.equals(ActionManager.SET_FOLD_FILTER_CMD)) {
                onSetFoldFilter();
            } else if (command.equals(ActionManager.USE_DETECTION_FILTER_CMD)) {
                onUseDetectionFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.USE_FOLD_FILTER_CMD)) {
                onUseFoldFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.DIVIDE_GENES_MEDIAN_CMD)) {
                onDivideGenesMedian();
            } else if (command.equals(ActionManager.DIVIDE_GENES_MEAN_CMD)) {
                onDivideGenesMean();                
            } else if (command.equals(ActionManager.LOG2_TRANSFORM_CMD)) {
                onLog2Transform();
            } else if (command.equals(ActionManager.NORMALIZE_SPOTS_CMD)) {
                onNormalizeSpots();
            } else if (command.equals(ActionManager.DIVIDE_SPOTS_RMS_CMD)) {
                onDivideSpotsRMS();
            } else if (command.equals(ActionManager.DIVIDE_SPOTS_SD_CMD)) {
                onDivideSpotsSD();
            } else if (command.equals(ActionManager.MEAN_CENTER_SPOTS_CMD)) {
                onMeanCenterSpots();
            } else if (command.equals(ActionManager.MEDIAN_CENTER_SPOTS_CMD)) {
                onMedianCenterSpots();
            } else if (command.equals(ActionManager.DIGITAL_SPOTS_CMD)) {
                onDigitalSpots();
            } else if (command.equals(ActionManager.NORMALIZE_EXPERIMENTS_CMD)) {
                onNormalizeExperiments();
            } else if (command.equals(ActionManager.DIVIDE_EXPERIMENTS_RMS_CMD)) {
                onDivideExperimentsRMS();
            } else if (command.equals(ActionManager.DIVIDE_EXPERIMENTS_SD_CMD)) {
                onDivideExperimentsSD();
            } else if (command.equals(ActionManager.MEAN_CENTER_EXPERIMENTS_CMD)) {
                onMeanCenterExperiments();
            } else if (command.equals(ActionManager.MEDIAN_CENTER_EXPERIMENTS_CMD)) {
                onMedianCenterExperiments();
            } else if (command.equals(ActionManager.DIGITAL_EXPERIMENTS_CMD)) {
                onDigitalExperiments();
            } else if (command.equals(ActionManager.LOG10_TO_LOG2_CMD)) {
                onLog10toLog2();
            } else if (command.equals(ActionManager.LOG2_TO_LOG10_CMD)) {
                onLog2toLog10();    
            } else if (command.equals(ActionManager.ADJUST_INTENSITIES_0_CMD)) {
                onAdjustIntensities((AbstractButton)event.getSource());
            } else if (command.equals(ActionManager.SAVE_MATRIX_COMMAND)) {
                onSaveMatrix();
            } else if (command.equals(ActionManager.DISPLAY_SET_RATIO_SCALE_CMD)) {
                onSetRatioScale();
            } else if (command.equals(ActionManager.DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND)) {
                onDeleteAllExperimentClusters();
            } else if (command.equals(ActionManager.DELETE_ALL_COMMAND)) {
                onDeleteAll();
            } else if (command.equals(ActionManager.LOAD_ANALYSIS_COMMAND)) {
                loadAnalysis();
            } else if (command.equals(ActionManager.SAVE_ANALYSIS_COMMAND)) {
                saveAnalysis();
            } else if (command.equals(ActionManager.SAVE_ANALYSIS_AS_COMMAND)) {
                saveAnalysisAs();
            } else if (command.equals(ActionManager.NEW_SCRIPT_COMMAND)) {
                onNewScript();
            } else if (command.equals(ActionManager.LOAD_SCRIPT_COMMAND)) {
                onLoadScript();
            } else if (command.equals(ActionManager.SEARCH_COMMAND)) {
                search();
            } else if (command.equals(ActionManager.APPEND_SAMPLE_ANNOTATION_COMMAND)) {
                appendSampleAnnotation();
            } else if (command.equals(ActionManager.APPEND_GENE_ANNOTATION_COMMAND)) {
                appendGeneAnnotation();
            }             /**             * Raktim Sept 29, 05             * CGH Command Handlers             */            else if(command.equals(ActionManager.LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION)){                //ctl.loadCloneDistributionsFromFile();            	//System.out.println("Called " + ActionManager.LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION);            	JOptionPane.showMessageDialog(null, "Called " + ActionManager.LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION, "alert", JOptionPane.INFORMATION_MESSAGE);            }else if(command.equals(ActionManager.SHOW_FLANKING_REGIONS)) {                onSetShowFlankingRegions( ((AbstractButton)event.getSource()).isSelected());            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_5)){                onChangeCghElementLength(5);            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_10)){                onChangeCghElementLength(10);            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_20)){                onChangeCghElementLength(20);            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_50)){                onChangeCghElementLength(50);            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_100)){                onChangeCghElementLength(100);            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_OTHER)){                onChangeCghElementLengthOther();            }else if(command.equals(ActionManager.CGH_ELEMENT_LENGTH_FIT)){                onChangeCghElementLength(ICGHDisplayMenu.FIT_SIZE);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_5)){                onChangeCghElementWidth(5);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_10)){                onChangeCghElementWidth(10);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_20)){                onChangeCghElementWidth(20);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_50)){                onChangeCghElementWidth(50);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_100)){                onChangeCghElementWidth(100);            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_OTHER)){                onChangeCghElementWidthOther();            }else if(command.equals(ActionManager.CGH_ELEMENT_WIDTH_FIT)){                onChangeCghElementWidth((int)ICGHDisplayMenu.FIT_SIZE);            }else if(command.equals(ActionManager.CGH_DISPLAY_TYPE_COMBINED)){                onChangeCghDisplayType(ICGHDisplayMenu.DISPLAY_TYPE_COMBINED);            }else if(command.equals(ActionManager.CGH_DISPLAY_TYPE_SEPARATED)){                onChangeCghDisplayType(ICGHDisplayMenu.DISPLAY_TYPE_SEPARATED);            }else if(command.equals(ActionManager.CGH_DISPLAY_ORDER)){                onChangeCghDisplayOrder();            }else if(command.equals(ActionManager.CGH_DELETE_SAMPLE)){                onDeleteSample();            }else if(command.equals(ActionManager.CGH_DISPLAY_LABEL_WSL_ID)){                onChangeDisplayLabelType(ICGHDisplayMenu.DISPLAY_WSL_ID);            }else if(command.equals(ActionManager.CGH_DISPLAY_LABEL_ALIAS)){                onChangeDisplayLabelType(ICGHDisplayMenu.DISPLAY_ALIAS);            }else if(command.equals(ActionManager.CGH_DISPLAY_LABEL_ID1)){                onChangeDisplayLabelType(ICGHDisplayMenu.DISPLAY_ID1);            }else if(command.equals(ActionManager.CGH_SET_THRESHOLDS)){                onCghSetThresholds();            }else if(command.equals(ActionManager.CGH_CLEAR_ANNOTATIONS)){                onClearAnnotations();            }else if(command.equals(ActionManager.CGH_ANALYSIS_COMMAND)){                onCghAnalysis((Action)event.getSource());            }else if(command.equals(ActionManager.FIND_GENE)){            	searchForGene();            }else if(command.equals(ActionManager.CIRCLE_VIEWER_BACKGROUND)){                onSetCircleViewerBackground();            }else if(command.equals(ActionManager.COMPARE_EXPERIMENTS)){            }else if(command.equals(ActionManager.CLONE_VALUE_DISCRETE_DETERMINATION)){                onChangeCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION);            }else if(command.equals(ActionManager.CLONE_VALUE_LOG_AVERAGE_INVERTED)){                onChangeCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_LOG_AVERAGE_INVERTED);            }else if(command.equals(ActionManager.CLONE_VALUE_LOG_CLONE_DISTRIBUTION)){                onChangeCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_LOG_CLONE_DISTRIBUTION);            }else if(command.equals(ActionManager.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION)){                onChangeCloneValueType(ICGHCloneValueMenu.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION);            }else if(command.equals(ActionManager.FLANKING_REGIONS_BY_THRESHOLD)){                onChangeFlankingRegionType(ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD);            }else if(command.equals(ActionManager.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION)){                onChangeFlankingRegionType(ICGHCloneValueMenu.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION);            }else if(command.equals(ActionManager.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION)){                onChangeFlankingRegionType(ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION);            }else if(command.equals(ActionManager.SHOW_HEADER)){                onShowHeader( ((AbstractButton)event.getSource()).isSelected());            }else if(command.equals(ActionManager.CLONE_P_THRESH)){                onSetClonePThresh();            }else if (command.equals(ActionManager.RENAME_NODE_CMD)) {                onRenameNode();            }            /* End CGH Command Handlers  */                  else {
                System.out.println("unhandled command = " + command);
            }
        }
        
        public void valueChanged(TreeSelectionEvent event) {
            onNodeChanged(event);
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        /**
         * Shows a popup menu for a selected navigation tree node.
         */
        private void maybeShowPopup(MouseEvent e) {
            
            if (!e.isPopupTrigger()) {
                return;
            }
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selPath == null) {
                return;
            }
            tree.setSelectionPaths(new TreePath[] {selPath});
            JPopupMenu popup = null;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof LeafInfo) {
                popup = ((LeafInfo)userObject).getJPopupMenu();
            }
            // adds the delete menu item for a custom node
            if (selPath.getPathCount() > 2) {
                if(node.getParent() == clusterNode)
                    return;
                if (popup == null) {
                    popup = new JPopupMenu();
                    if(userObject instanceof LeafInfo) {
                        Object viewerObj = ((LeafInfo)userObject).getViewer();
                        if(viewerObj == null) {
                            popup.add(createDeleteMenuItem());
                        } else if(viewerObj instanceof IViewer && ((IViewer)viewerObj).getClusters() != null && ((IViewer)viewerObj).getExperiment() != null && ((IViewer)viewerObj).getViewerType() != -1) {
                            popup.add(createDeleteMenuItem());
                            popup.addSeparator();
                            popup.add(createSetDataMenuItem(((LeafInfo)userObject).isSelectedDataSource()));
                        }
                    } else {
                        popup.add(createDeleteMenuItem());
                    }
                } else {
                    if (!isContainsDeleteItem(popup)) {
                        popup.addSeparator();
                        popup.add(createDeleteMenuItem());
                    }
                }
            } else if( ((LeafInfo)userObject).toString().equals("Main View") && data.getFeaturesCount() != 0 ) {
                popup = new JPopupMenu();
                JMenuItem item = new JMenuItem("Set as Data Source");
                item.setActionCommand(ActionManager.SET_DATA_SOURCE_COMMAND);
                item.addActionListener(this);
                popup.add(item);
            }
            if (popup != null) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        /**
         * Creates a delete menu item.
         */
        private JMenuItem createDeleteMenuItem() {
            JMenuItem menuItem = new JMenuItem("Delete");
            menuItem.setActionCommand(ActionManager.DELETE_NODE_CMD);
            menuItem.addActionListener(this);
            return menuItem;
        }
        
        
        /**
         * Creates a data source CheckBox menu item.
         */
        private JCheckBoxMenuItem createSetDataMenuItem(boolean selected) {
            JCheckBoxMenuItem box = new JCheckBoxMenuItem("Set as Data Source", selected);
            box.setActionCommand(ActionManager.SET_DATA_SOURCE_COMMAND);
            box.addActionListener(this);
            return box;
        }
        
        
        /**
         * Checkes if node already contains the delete item.
         */
        private boolean isContainsDeleteItem(JPopupMenu popup) {
            Component[] components = popup.getComponents();
            for (int i=components.length; --i >= 0;) {
                if (components[i] instanceof JMenuItem) {
                    if (((JMenuItem)components[i]).getActionCommand().equals(ActionManager.DELETE_NODE_CMD)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public void keyReleased(KeyEvent event) {}
        public void keyPressed(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
        
        public void windowOpened(WindowEvent e) {}
        public void windowClosing(WindowEvent e) {
            onClose();
        }
        public void windowClosed(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}        /**         * Raktim Sept 29, 05         * Adding CGH Listener Functions         * IDataRegionSelectionListener functions         */        public void onShowBrowser(EventObject eventObj){        	  ShowBrowser(eventObj);        }        public void onDisplayDataValues(EventObject eventObj){        	DisplayDataValues(eventObj);        }        public void onShowGenes(EventObject eventObj){        	ShowGenes(eventObj);        }        public void onAnnotationsSelected(EventObject eventObj){        	AnnotationsSelected(eventObj);        }         /**          * Raktim Sept 29, 05          * Adding CGH Listener Functions          * ICGHListener functions          */        public void onDataChanged(){        	fireDataChanged();        }        public void onCloneValuesChanged(){        	fireCloneValuesChanged();        }        public void onChromosomeSelected(java.util.EventObject eventObj){        	fireChromosomeSelected(eventObj);        }        public void onCloneDistributionsLoaded(){        	CloneDistributionsLoaded();        }        //public void onExperimentsLoaded(java.util.EventObject eventObj);        //public void onExperimentsInitialized(java.util.EventObject eventObj);        public void onExperimentsLoaded(){        	ExperimentsLoaded();        }        /**         * Raktim         * Loads data from the user specified directory.         */        protected void onLoadDirectory() {        	onLoadDirectory();        	//ctl.onLoadDirectory();        }        
    }
    
    /**
     * This <code>IFramework</code> implementation delegates
     * all its invokations to the outer class.
     */
    private class FrameworkImpl implements IFramework, java.io.Serializable {
        public static final long serialVersionUID = 10201020001L;
        
        public IData getData() {
            return MultipleArrayViewer.this.getData();
        }
        public JFrame getJFrame() {
        	return mainframe;
        }
        public AlgorithmFactory getAlgorithmFactory() {
            return MultipleArrayViewer.this.getAlgorithmFactory();
        }
        public IDisplayMenu getDisplayMenu() {
            return menubar.getDisplayMenu();
        }
        public IDistanceMenu getDistanceMenu() {
            return menubar.getDistanceMenu();
        }
        public Frame getFrame() {
            return MultipleArrayViewer.this.getFrame();
        }
        public void setContentLocation(int x, int y) {
            MultipleArrayViewer.this.setContentLocation(x, y);
        }
        public void displaySingleArrayViewer(int feature) {
            MultipleArrayViewer.this.displaySingleArrayViewer(feature);
        }
        public void displaySlideElementInfo(int feature, int probe) {
            MultipleArrayViewer.this.displaySlideElementInfo(feature, probe);
        }
        public String getStatusText() {
            return MultipleArrayViewer.this.getStatusText();
        }
        public void setStatusText(String text) {
            MultipleArrayViewer.this.setStatusText(text);
        }
        public Object getUserObject() {
            return MultipleArrayViewer.this.getUserObject();
        }
        public void setTreeNode(DefaultMutableTreeNode node){
            MultipleArrayViewer.this.selectNode(node);
        }
        
        public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
            MultipleArrayViewer.this.addNode(parent, child);
        }
        
        public Color storeCluster(int[] indices, Experiment experiment, int clusterType){
            return MultipleArrayViewer.this.storeCluster(indices, experiment, clusterType);
        }
        
        public Color storeSubCluster(int[] indices, Experiment experiment, int clusterType){
            return MultipleArrayViewer.this.storeSubCluster(indices, experiment, clusterType);
        }
        
        public boolean removeSubCluster(int[] indices, Experiment experiment, int clusterType) {
            return MultipleArrayViewer.this.removeSubCluster(indices, experiment, clusterType);
        }
        
        public boolean removeCluster(int[] indices, Experiment experiment, int clusterType) {
            return MultipleArrayViewer.this.removeCluster(indices, experiment, clusterType);
        }
        
        public void launchNewMAV(int[] indices, Experiment experiment, String label, int clusterType){
            MultipleArrayViewer.this.launchNewMAV(indices, experiment, label, clusterType);
        }
        
        public void openClusterNode(String algorithmNode, String clusterID) {
            MultipleArrayViewer.this.openClusterNode(algorithmNode, clusterID);
        }
        
        public ClusterRepository getClusterRepository(int clusterType){
            return MultipleArrayViewer.this.getClusterRepository(clusterType);
        }
        
        /** Returns the currently selected node.
         */
        public DefaultMutableTreeNode getCurrentNode() {
            return MultipleArrayViewer.this.getCurrentNode();
        }
        
        /** Returns the result node containing the supplied object
         */
        public DefaultMutableTreeNode getNode(Object object) {
            return MultipleArrayViewer.this.getNode(object);
        }
        
        /** Adds string to history node
         */
        public void addHistory(String historyEvent) {
            MultipleArrayViewer.this.addHistory(historyEvent);
        }
        
        /** Returns the ResultTree object
         */
        public ResultTree getResultTree() {
            return MultipleArrayViewer.this.getResultTree();
        }
        
        /** Adds result to the ResultTree
         */
        public void addAnalysisResult(DefaultMutableTreeNode resultNode) {
            MultipleArrayViewer.this.addAnalysisResult(resultNode);
        }
        
        /** Refreshes current viewer if it's an IViewer  */
        public void refreshCurrentViewer() {
            MultipleArrayViewer.this.refreshCurrentViewer();
        }
        
        /**  Stores indices to a cluster in the manager but doesn't link to a particular viewer node.
         */
        public void storeOperationCluster(String source, String clusterID, int[] indices, boolean geneCluster) {
            MultipleArrayViewer.this.storeOperationCluster(source, clusterID, indices, geneCluster);
        }                /**         * Raktim Nov 02, 2005         * CGH Specific methods         */        /**         * Raktim         * CGH Display Menu accessor         */        public ICGHDisplayMenu getCghDisplayMenu(){        	return menubar.getCghDisplayMenu();        }        /**         * Raktim         * CGH Clone Menu accessor         */        public ICGHCloneValueMenu getCghCloneValueMenu(){        	return menubar.getCloneValueMenu();        }        /**         * Raktim         * @return         */        public Rectangle getViewerBounds(){        	return viewScrollPane.getViewportBorderBounds();        }        /**         * Raktim         * Returns the cytoband model of the a species         */        public CytoBandsModel getCytoBandsModel(){        	return cytoBandsModel;        }
    }
}
