/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SingleArrayViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-27 22:19:13 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.media.jai.*;
import com.sun.media.jai.codec.*;
import org.tigr.util.Query;
import org.tigr.util.awt.GBA;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.microarray.util.awt.SetElementSizeDialog;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.graph.GC;
import org.tigr.graph.GraphBar;
import org.tigr.graph.GraphViewer;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphTick;
import org.tigr.microarray.util.awt.SetSlideFilenameDialog;
import org.tigr.util.Xcon;
import org.tigr.util.awt.MessageDisplay;
import org.tigr.microarray.util.SlideDataSorter;
import org.tigr.microarray.util.swing.SlideDataLoader;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.LinRegNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.RatioStatsNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.IterativeLogMCNormInitDialog;


//Graph package


public class SingleArrayViewer extends ArrayViewer implements Printable {
    private ISlideData slideData;
    private int[] indices;
    public SingleArrayViewerPanel panel;
    private SingleArrayViewerCanvas canvas;
    private SingleArrayViewerInfoBox infoBox;
    private EventListener eventListener = new EventListener();
    private GBA gba = new GBA();
    JScrollPane scrollPane;
    private int oldX = -1;
    private int oldY = -1;
    
    JMenuBar menuBar = null;
    JMenu fileMenu = null;
    JMenuItem newDatabaseItem = null;
    JMenuItem newFileItem = null;
    JMenuItem newSpotfireItem = null;
    JMenuItem newReportItem = null;
    JMenuItem saveImageItem = null;
    JMenuItem printImageItem = null;
    JMenuItem closeItem = null;
    
    JMenu viewsMenu = null;
    JMenu viewGraphMenu = null;
    JMenuItem newPlotItem = null;
    JMenuItem newPlotLogItem = null;
    JMenuItem newLogRatioProductItem = null;
    JMenuItem newLogRatioProductByBlockItem = null;
    JMenuItem newHistogramItem = null;
    JMenuItem newHistogramLogItem = null;
    
    JMenuItem subArrayItem = null;
    JMenuItem regionArrayItem = null;
    
    JMenu normalizationMenu = null;
    JRadioButtonMenuItem totalIntensityItem = null;
    JRadioButtonMenuItem leastSquaresItem = null;
    JRadioButtonMenuItem linearRegressionItem = null;
    JRadioButtonMenuItem ratioStatisticsItem = null;
    JRadioButtonMenuItem iterativeLogItem = null;
    // JRadioButtonMenuItem lowessItem = null;
    JRadioButtonMenuItem totalIntensityListItem = null;
    JRadioButtonMenuItem leastSquaresListItem = null;
    JRadioButtonMenuItem linearRegressionListItem = null;
    JRadioButtonMenuItem ratioStatisticsListItem = null;
    JRadioButtonMenuItem iterativeLogListItem = null;
    JRadioButtonMenuItem noNormalizationItem = null;
    //   JRadioButtonMenuItem lowessListItem = null;
    
    JMenu sortMenu = null;
    JRadioButtonMenuItem sortByLocationItem = null;
    JRadioButtonMenuItem sortByRatioItem = null;
    JRadioButtonMenuItem[] sortMenuItems = null;
    
    JMenu displayMenu = null;
    JCheckBoxMenuItem logItem = null;
    JMenu sizeMenu = null;
    JRadioButtonMenuItem defaultSize1Item = null;
    JRadioButtonMenuItem defaultSize2Item = null;
    JRadioButtonMenuItem defaultSize3Item = null;
    JRadioButtonMenuItem defaultSize4Item = null;
    JRadioButtonMenuItem setElementSizeItem = null;
    JCheckBoxMenuItem changeColorsItem = null;
    JRadioButtonMenuItem blueToRedItem = null;
    JRadioButtonMenuItem greenRedItem = null;
    JRadioButtonMenuItem overlayItem = null;
    
    JMenu controlMenu = null;
    JMenuItem setUpperLimitsItem = null;
    JMenuItem setGreenRedThresholdsItem = null;
    JMenuItem setConfidenceItem = null;
    JCheckBoxMenuItem setThresholdsItem = null;
    JCheckBoxMenuItem setScaleItem = null;
    ButtonGroup buttonGroup = null;
    
    private final static int startingXSize = 400;
    private final static int startingYSize = 400;
    
    private final static int LINEAR = 0;
    private final static int LOG = 1;
    
    public SingleArrayViewer(JFrame frame, ISlideData inputData) {
        super(frame);
        initializeViewer();
        initializePanel();
        initializeCanvas();
        initializeFrame();
        
        setSlideData(inputData);
        //this.slideData = new SlideData(inputData);
        //System.out.println("*****SAV 1:");
        //inputData.output();
        //System.out.println("*****SAV 2:");
        //slideData.output();
        //Draw the data
        
        panel.setXYScrollbars((long)slideData.getMaxIntensity(ISlideDataElement.CY3), (long)slideData.getMaxIntensity(ISlideDataElement.CY5));
        
        systemEnable(TMEV.DATA_AVAILABLE);
    }
    
    public SingleArrayViewer(JFrame frame) {
        super(frame);
        initializeFrame();
        initializeViewer();
        initializePanel();
        initializeCanvas();
        
        //Redundant?
        systemDisable(TMEV.DATA_AVAILABLE);
        systemDisable(TMEV.DB_LOGIN);
    }
    
    public SingleArrayViewer(ISlideData slideData) {
        super(new JFrame("Single Array Viewer"));
        setSlideData(slideData);
        initializeViewer();
        initializePanel();
        initializeCanvas();
        //Draw the data
        
        systemEnable(TMEV.DATA_AVAILABLE);
    }
    
    public SingleArrayViewer() {
        super(new JFrame("Single Array Viewer"));
        initializeViewer();
        initializePanel();
        initializeCanvas();
    }
    
    public JFrame getFrame() {
        return mainframe;
    }
    
    
    private int[] createIndices() {
        int[] indices = new int[slideData.getSize()];
        for (int i=0; i<indices.length; i++) {
            indices[i] = i;
        }
        return indices;
    }
    
    private void initializeViewer() {
        setLayout(new GridBagLayout());
        //setSize(startingXSize, startingYSize);
    }
    
    private void initializePanel() {
        panel = new SingleArrayViewerPanel();
        gba.add(this, panel, 0, 0, 1, 2, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
        
        infoBox = new SingleArrayViewerInfoBox();
        gba.add(this, infoBox, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.SW, new Insets(5, 5, 5, 5), 0, 0);
    }
    
    private void initializeCanvas() {
        canvas = new SingleArrayViewerCanvas(20, 20, 50, 20);
        canvas.addMouseListener(eventListener);
        canvas.addMouseMotionListener(eventListener);
        canvas.addKeyListener(eventListener);
        canvas.setBackground(Color.white);
        scrollPane = new JScrollPane(canvas, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.getVerticalScrollBar().setToolTipText("Use up/down/pgup/pgdown to scroll image");
        gba.add(this, scrollPane, 1, 0, 1, 1, 1, 1, GBA.B, GBA.NW);
        //Setting the lineup, linedown, pageup, pagedown keystrokes
        KeyStroke up = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0);
        KeyStroke pgup = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0);
        KeyStroke pgdown = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0);
        scrollPane.registerKeyboardAction(eventListener, "lineup", up, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(eventListener, "linedown", down, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(eventListener, "pageup", pgup, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(eventListener, "pagedown", pgdown, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void initializeFrame() {
        mainframe.setTitle("Single Array Viewer");
        mainframe.getContentPane().setLayout(new GridBagLayout());
        mainframe.setResizable(true);
        mainframe.setBackground(Color.white);
        //mainframe.setIconImage((new ImageIcon(org.tigr.microarray.SingleArrayViewer.class.getResource("/org/tigr/images/expression.gif"))).getImage());
        mainframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {close();}
        });
        initializeMenuBar(mainframe);
        gba.add(mainframe.getContentPane(), this, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
        mainframe.pack();
        
        //Any initial MenuItem enables/disables go here
        newDatabaseItem.setEnabled(false);
        sortByLocationItem.setSelected(true);
        logItem.setSelected(false);
        changeColorsItem.setSelected(false);
        overlayItem.setSelected(true);
        setThresholdsItem.setSelected(false);
        setScaleItem.setSelected(true);
        noNormalizationItem.setSelected(true);
        setConfidenceItem.setEnabled(false);
        defaultSize3Item.setSelected(true);
    }
    
    private void initializeMenuBar(JFrame frame) {
        menuBar = new JMenuBar();
        
        fileMenu = new JMenu("File");
        
        newDatabaseItem = new JMenuItem("Open Experiment from DB");
        newDatabaseItem.addActionListener(eventListener);
        fileMenu.add(newDatabaseItem);
        fileMenu.getMenuComponent(0).setEnabled(false); //disable until supported, 4.30.03
        
        newFileItem = new JMenuItem("Open Experiment From File");
        newFileItem.addActionListener(eventListener);
        fileMenu.add(newFileItem);
        
        newReportItem = new JMenuItem("Generate Report");
        newReportItem.addActionListener(eventListener);
        fileMenu.add(newReportItem);
        
        fileMenu.addSeparator();
        
        saveImageItem = new JMenuItem("Save Image");
        saveImageItem.addActionListener(eventListener);
        fileMenu.add(saveImageItem);
        
        printImageItem = new JMenuItem("Print Image");
        printImageItem.addActionListener(eventListener);
        fileMenu.add(printImageItem);
        
        fileMenu.addSeparator();
        
        newSpotfireItem = new JMenuItem("Export to Spotfire");
        newSpotfireItem.addActionListener(eventListener);
        //fileMenu.add(newSpotfireItem);
        
        closeItem = new JMenuItem("Close");
        closeItem.addActionListener(eventListener);
        fileMenu.add(closeItem);
        
        menuBar.add(fileMenu);
        
        viewsMenu = new JMenu("Views");
        
        viewGraphMenu = new JMenu("View Graph");
        viewsMenu.add(viewGraphMenu);
        
        newPlotItem = new JMenuItem("Intensity Scatterplot");
        newPlotItem.addActionListener(eventListener);
        viewGraphMenu.add(newPlotItem);
        
        newPlotLogItem = new JMenuItem("Intensity Scatterplot (log)");
        newPlotLogItem.addActionListener(eventListener);
        viewGraphMenu.add(newPlotLogItem);
        
        newLogRatioProductItem = new JMenuItem("Log Ratio x Log Product");
        newLogRatioProductItem.addActionListener(eventListener);
        viewGraphMenu.add(newLogRatioProductItem);
        
        newLogRatioProductByBlockItem = new JMenuItem("Log Ratio x Log Product, by Metablock");
        newLogRatioProductByBlockItem.addActionListener(eventListener);
        viewGraphMenu.add(newLogRatioProductByBlockItem);
        
        newHistogramItem = new JMenuItem("Ratio Histogram");
        newHistogramItem.addActionListener(eventListener);
        viewGraphMenu.add(newHistogramItem);
        
        newHistogramLogItem = new JMenuItem("Ratio Histogram (log)");
        newHistogramLogItem.addActionListener(eventListener);
        viewGraphMenu.add(newHistogramLogItem);
        
        subArrayItem = new JMenuItem("View SubArray");
        subArrayItem.addActionListener(eventListener);
        viewsMenu.add(subArrayItem);
        
        regionArrayItem = new JMenuItem("View Region");
        regionArrayItem.addActionListener(eventListener);
        viewsMenu.add(regionArrayItem);
        
        menuBar.add(viewsMenu);
        
        buttonGroup = new ButtonGroup();
        normalizationMenu = new JMenu("Normalization");
        
        totalIntensityItem = new JRadioButtonMenuItem("Total Intensity");
        totalIntensityItem.addActionListener(eventListener);
        normalizationMenu.add(totalIntensityItem);
        buttonGroup.add(totalIntensityItem);
        
        linearRegressionItem = new JRadioButtonMenuItem("Linear Regression");
        linearRegressionItem.addActionListener(eventListener);
        normalizationMenu.add(linearRegressionItem);
        buttonGroup.add(linearRegressionItem);
        
        ratioStatisticsItem = new JRadioButtonMenuItem("Ratio Statistics");
        ratioStatisticsItem.addActionListener(eventListener);
        normalizationMenu.add(ratioStatisticsItem);
        buttonGroup.add(ratioStatisticsItem);
        
        iterativeLogItem = new JRadioButtonMenuItem("Iterative Log");
        iterativeLogItem.addActionListener(eventListener);
        normalizationMenu.add(iterativeLogItem);
        buttonGroup.add(iterativeLogItem);
        
        
        normalizationMenu.addSeparator();
        
        noNormalizationItem = new JRadioButtonMenuItem("No Normalization");
        noNormalizationItem.addActionListener(eventListener);
        normalizationMenu.add(noNormalizationItem);
        buttonGroup.add(noNormalizationItem);
        
        menuBar.add(normalizationMenu);
        
        buttonGroup = new ButtonGroup();
        sortMenu = new JMenu("Sort");
        
        sortByLocationItem = new JRadioButtonMenuItem("By Location");
        sortByLocationItem.addActionListener(eventListener);
        sortMenu.add(sortByLocationItem);
        buttonGroup.add(sortByLocationItem);
        
        sortByRatioItem = new JRadioButtonMenuItem("By Cy5/Cy3 Ratio");
        sortByRatioItem.addActionListener(eventListener);
        sortMenu.add(sortByRatioItem);
        buttonGroup.add(sortByRatioItem);
        
        if (TMEV.getFieldNames() != null) addSortMenuItems(TMEV.getFieldNames(), buttonGroup);
        
        menuBar.add(sortMenu);
        
        buttonGroup = new ButtonGroup();
        displayMenu = new JMenu("Display");
        
        blueToRedItem = new JRadioButtonMenuItem("Blue -> Red");
        blueToRedItem.addActionListener(eventListener);
        displayMenu.add(blueToRedItem);
        buttonGroup.add(blueToRedItem);
        
        greenRedItem = new JRadioButtonMenuItem("Green / Red");
        greenRedItem.addActionListener(eventListener);
        displayMenu.add(greenRedItem);
        buttonGroup.add(greenRedItem);
        
        overlayItem = new JRadioButtonMenuItem("G/R Overlay");
        overlayItem.addActionListener(eventListener);
        displayMenu.add(overlayItem);
        buttonGroup.add(overlayItem);
        
        displayMenu.addSeparator();
        
        logItem = new JCheckBoxMenuItem("Log Scale");
        logItem.addActionListener(eventListener);
        displayMenu.add(logItem);
        
        displayMenu.addSeparator();
        
        changeColorsItem = new JCheckBoxMenuItem("Change Colors");
        changeColorsItem.addActionListener(eventListener);
        displayMenu.add(changeColorsItem);
        
        displayMenu.addSeparator();
        
        sizeMenu = new JMenu("Element Size");
        sizeMenu.addActionListener(eventListener);
        displayMenu.add(sizeMenu);
        buttonGroup = new ButtonGroup();
        
        defaultSize1Item = new JRadioButtonMenuItem("5 x 2");
        defaultSize1Item.addActionListener(eventListener);
        sizeMenu.add(defaultSize1Item);
        buttonGroup.add(defaultSize1Item);
        
        defaultSize2Item = new JRadioButtonMenuItem("10 x 10");
        defaultSize2Item.addActionListener(eventListener);
        sizeMenu.add(defaultSize2Item);
        buttonGroup.add(defaultSize2Item);
        
        defaultSize3Item = new JRadioButtonMenuItem("20 x 5");
        defaultSize3Item.addActionListener(eventListener);
        sizeMenu.add(defaultSize3Item);
        buttonGroup.add(defaultSize3Item);
        
        defaultSize4Item = new JRadioButtonMenuItem("50 x 10");
        defaultSize4Item.addActionListener(eventListener);
        sizeMenu.add(defaultSize4Item);
        buttonGroup.add(defaultSize4Item);
        
        setElementSizeItem = new JRadioButtonMenuItem("Other");
        setElementSizeItem.addActionListener(eventListener);
        sizeMenu.add(setElementSizeItem);
        buttonGroup.add(setElementSizeItem);
        
        
        menuBar.add(displayMenu);
        
        controlMenu = new JMenu("Control");
        
        setUpperLimitsItem = new JMenuItem("Set Thresholds");
        setUpperLimitsItem.addActionListener(eventListener);
        controlMenu.add(setUpperLimitsItem);
        
        setGreenRedThresholdsItem = new JMenuItem("Set Expression Ratio");
        setGreenRedThresholdsItem.addActionListener(eventListener);
        controlMenu.add(setGreenRedThresholdsItem);
        
        setConfidenceItem = new JMenuItem("Set Confidence Level");
        setConfidenceItem.addActionListener(eventListener);
        controlMenu.add(setConfidenceItem);
        
        controlMenu.addSeparator();
        
        setThresholdsItem = new JCheckBoxMenuItem("Expression Ratio");
        setThresholdsItem.addActionListener(eventListener);
        controlMenu.add(setThresholdsItem);
        
        controlMenu.addSeparator();
        
        setScaleItem = new JCheckBoxMenuItem("G/R Scale");
        setScaleItem.addActionListener(eventListener);
        controlMenu.add(setScaleItem);
        
        menuBar.add(controlMenu);
        
        frame.setJMenuBar(menuBar);
    }
    
    public void addSortMenuItems(String[] sortTypes, ButtonGroup group) {
        sortMenuItems = new JRadioButtonMenuItem[sortTypes.length];
        for (int i = 0; i < sortMenuItems.length; i++) {
            sortMenuItems[i] = new JRadioButtonMenuItem("Sort by " + sortTypes[i]);
            sortMenuItems[i].addActionListener(eventListener);
            sortMenu.add(sortMenuItems[i]);
            group.add(sortMenuItems[i]);
        }
    }
    
    /*
    public void loadFirstData()
        {
        try
            {
            String inputPreference = TMEV.getSettingForOption("Input Preference");
     
            if (inputPreference.equals("Database"))
                {
                databaseLogin();
                //loadDataFromDatabase();
                }
            else if (inputPreference.equals("File"))
                {
                databaseLogin();
                //loadDataFromDatabase();
                }
            else if (inputPreference.equals("Only File"))
                {
                //selectFile();
                }
            else
                {
                Manager.message(frame, "Error: Invalid Preferences File");
                }
            }
        catch (Exception e) {System.out.println("Exception (MultipleArrayViewer.loadData()): " + e);}
        }
     */
    
    public ISlideData loadDataFromDatabase() {
        ISlideData slideData = null;
        
        try {
            //Query query = new Query("exec get_experiment_name");
            //Vector resultVector = query.executeQuery(TMEV.getConnection());
            // STUB:
            SetSlideFilenameDialog ssfd = new SetSlideFilenameDialog(mainframe, new String[] {});
            if (ssfd.showModal() == JOptionPane.OK_OPTION) {
                String filename = ssfd.getFileName();
                loadDataFromDatabase(filename);
            }
        } catch (Exception e) {
            System.out.println("Exception (SingleArrayViewer.loadDataFromDatabase()): " + e);
            e.printStackTrace();
        } finally {
            return slideData;
        }
    }
    
    public void loadDataFromDatabase(String filename) {
        /*SlideData slideData = super.loadDataFromDatabase(filename);
        panel.setXYScrollbars(slideData.getMaxIntensity(ISlideDataElement.CY3), slideData.getMaxIntensity(ISlideDataElement.CY5));
        setSlideData(slideData);
        refreshSlide();*/
    }
    
    public void loadFile() {
        JFileChooser jfc = new JFileChooser(System.getProperty("user.dir")+"\\Preferences");
        FileFilter ff = new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                String filename = file.getName();
                if (filename.endsWith("Preferences")) return true;
                else if (filename.endsWith("preferences")) return true;
                else if (filename.endsWith(".pref")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "Preference Files";
            }
        };
        jfc.setFileFilter(ff);
        int activityCode = jfc.showDialog(this, "Select");
        
        if (activityCode == JFileChooser.APPROVE_OPTION) {
            File target = jfc.getSelectedFile();
            TMEV.readPreferencesFile(target);
        }
        
        
        
        try {
            ISlideData slideData = loadSlideData(null);
            if (slideData != null) {
                setSlideData(slideData);
                panel.setXYScrollbars((long)slideData.getMaxIntensity(ISlideDataElement.CY3), (long)slideData.getMaxIntensity(ISlideDataElement.CY5));
                refreshSlide();
            }
        } catch (Exception e) {
            Manager.message(getFrame(), e);
        }
    }
    
    /*public SlideData loadDataFromFile(String filename) {
        SlideData slideData = super.loadSlideData(filename);
        setSlideData(slideData);
        panel.setXYScrollbars(slideData.getMaxIntensity(ISlideDataElement.CY3), slideData.getMaxIntensity(ISlideDataElement.CY5));
        refreshSlide();
        return slideData;
    }*/
    
    public ISlideData getSlideData() {
        return this.slideData;
    }
    
    public void setSlideData(ISlideData newData) {
        if(newData instanceof SlideData){
            SlideData dataClone = (SlideData)((SlideData)newData).clone();
            this.slideData = SlideDataLoader.fillBlankSpots(dataClone);
        }
        else if(newData instanceof FloatSlideData){
            this.slideData = new SlideData(newData);
            this.slideData = SlideDataLoader.fillBlankSpots(this.slideData);
        }
        else
            this.slideData = newData;
        indices = createIndices();
        canvas.updateSizes();
    }
    
    public void createSubArray() {
        String source;
        SingleArrayViewer subViewer;
        Vector holding;
        ISlideData newSlideData;
        ISlideDataElement sde, sdo;
        double low = canvas.getLowDifference();
        double high = canvas.getHighDifference();
        
        if (canvas.getThresholds()) source = "SubArray: Lower " + (Math.rint(low * 100) / 100) + ", Higher " + (Math.rint(high * 100) / 100);
        else source = "SubArray: No Thresholds";
        
        JFrame subFrame = new JFrame(source);
        subViewer = new SingleArrayViewer(subFrame);
        subViewer.changePaletteStyle(canvas.getPaletteStyle());
        subViewer.setThresholds(canvas.getThresholds());
        //Should this be nice?
        subViewer.canvas.setScale(canvas.getScale());
        
        if (canvas.getThresholds()) {
            holding = new Vector();
            for (int i = 0; i < indices.length; i++) {
                try {
                    sdo = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    sde = new SlideDataElement(new int[]{sdo.getRow(ISlideDataElement.BASE),
                    sdo.getRow(ISlideDataElement.META),
                    sdo.getRow(ISlideDataElement.SUB)},
                    new int[]{sdo.getColumn(ISlideDataElement.BASE),
                    sdo.getColumn(ISlideDataElement.META),
                    sdo.getColumn(ISlideDataElement.SUB)},
                    new float[]{sdo.getIntensity(ISlideDataElement.CY3),
                    sdo.getIntensity(ISlideDataElement.CY5)},
                    sdo.getExtraFields());
                    int cy3 = (int) sde.getIntensity(ISlideDataElement.CY3);
                    int cy5 = (int) sde.getIntensity(ISlideDataElement.CY5);
                    if ((cy3 + cy5 > 0) && (((double) cy3 / (cy3 + cy5) > high)
                    || ((double) cy3 / (cy3 + cy5) < low))) holding.addElement(sde);
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
            
            if (holding.size() > 0) {
                int columns = (int) Math.ceil(Math.sqrt(holding.size() / 6));
                int rows = columns * 6;
                
                for (; (columns * rows) - holding.size() >= columns; rows--) {
                    ;
                }
                
                newSlideData = new SlideData(rows, columns);
                newSlideData.setSlideDataName(slideData.getSlideDataName());
                newSlideData.setNormalizedState(slideData.getNormalizedState());
                int maxColumns = newSlideData.getSlideMetaData().getColumns();
                
                for (int i = 0; i < holding.size(); i++) {
                    try {
                        sde = (ISlideDataElement)holding.elementAt(i);
                        sde.setColumn(ISlideDataElement.BASE, (i % maxColumns) + 1);
                        sde.setRow(ISlideDataElement.BASE, (i / maxColumns) + 1);
                        
                        newSlideData.addSlideDataElement(sde);
                    } catch (Exception e) {
                        System.out.println("Exception (SingleArrayViewer.createSubArray(): " + e);
                        e.printStackTrace();
                    }
                }
                subViewer.setSlideData(newSlideData);
                subViewer.setUpperLimits(canvas.getTopCy3(), canvas.getTopCy5());
                subViewer.panel.setXYScrollbars((long)slideData.getMaxIntensity(ISlideDataElement.CY3), (long)slideData.getMaxIntensity(ISlideDataElement.CY5));
                subViewer.panel.updateXUpperLimitScrollbar((long)canvas.getTopCy3());
                subViewer.panel.updateYUpperLimitScrollbar((long)canvas.getTopCy5());
                subFrame.setSize(650, 650);
                subFrame.setLocation(150, 150);
                subFrame.setVisible(true);
            }
        } else {
            subViewer.setSlideData(slideData);
            subViewer.setUpperLimits(canvas.getTopCy3(), canvas.getTopCy5());
            subViewer.panel.setXYScrollbars((long)slideData.getMaxIntensity(ISlideDataElement.CY3), (long)slideData.getMaxIntensity(ISlideDataElement.CY5));
            subViewer.panel.updateXUpperLimitScrollbar((long)canvas.getTopCy3());
            subViewer.panel.updateYUpperLimitScrollbar((long)canvas.getTopCy5());
            subFrame.setSize(650, 650);
            subFrame.setLocation(150, 150);
            subFrame.setVisible(true);
        }
    }
    
    public void createRegion(int metaRow, int metaColumn) {
        SingleArrayViewer subViewer;
        Vector holding;
        ISlideData newSlideData;
        ISlideDataElement sde, sdo;
        int maxSubRow = 0, maxSubColumn = 0;
        
        holding = new Vector();
        for (int i = 0; i < indices.length; i++) {
            try {
                sdo = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                sde = new SlideDataElement(new int[]{sdo.getRow(ISlideDataElement.BASE),
                sdo.getRow(ISlideDataElement.META),
                sdo.getRow(ISlideDataElement.SUB)},
                new int[]{sdo.getColumn(ISlideDataElement.BASE),
                sdo.getColumn(ISlideDataElement.META),
                sdo.getColumn(ISlideDataElement.SUB)},
                new float[]{sdo.getIntensity(ISlideDataElement.CY3),
                sdo.getIntensity(ISlideDataElement.CY5)},
                sdo.getExtraFields());
                if ((sde.getRow(ISlideDataElement.META) == metaRow) && (sde.getColumn(ISlideDataElement.META) == metaColumn)) {
                    holding.addElement(sde);
                    if (sde.getRow(ISlideDataElement.SUB) > maxSubRow) maxSubRow = sde.getRow(ISlideDataElement.SUB);
                    if (sde.getColumn(ISlideDataElement.SUB) > maxSubColumn) maxSubColumn = sde.getColumn(ISlideDataElement.SUB);
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
        
        if (holding.size() > 0) {
            JFrame subFrame = new JFrame("Region: MetaColumn " + metaColumn + ", MetaRow " + metaRow);
            subViewer = new SingleArrayViewer(subFrame);
            subViewer.changePaletteStyle(canvas.getPaletteStyle());
            subViewer.setThresholds(canvas.getThresholds());
            //Should this be nice?
            subViewer.canvas.setScale(canvas.getScale());
            
            newSlideData = new SlideData(maxSubColumn, maxSubRow);
            newSlideData.setSlideDataName(slideData.getSlideDataName());
            newSlideData.setNormalizedState(slideData.getNormalizedState());
            int maxColumns = newSlideData.getSlideMetaData().getColumns();
            
            for (int i = 0; i < holding.size(); i++) {
                try {
                    sde = (ISlideDataElement) holding.elementAt(i);
                    sde.setColumn(ISlideDataElement.BASE, (i % maxColumns) + 1);
                    sde.setRow(ISlideDataElement.BASE, (i / maxColumns) + 1);
                    
                    newSlideData.addSlideDataElement(sde);
                } catch (Exception e) {
                    System.out.println("Exception (SingleArrayViewer.createRegion(): " + e);
                }
            }
            
            subViewer.setSlideData(newSlideData);
            subViewer.setUpperLimits(canvas.getTopCy3(), canvas.getTopCy5());
            subViewer.panel.setXYScrollbars((long)slideData.getMaxIntensity(ISlideDataElement.CY3), (long)slideData.getMaxIntensity(ISlideDataElement.CY5));
            subViewer.panel.updateXUpperLimitScrollbar((long)canvas.getTopCy3());
            subViewer.panel.updateYUpperLimitScrollbar((long)canvas.getTopCy5());
            subFrame.setSize(650, 650);
            subFrame.setLocation(150, 150);
            subFrame.setVisible(true);
        }
        
        else {
            MessageDisplay messageDisplay = new MessageDisplay(((JFrame) getParent()), "No region: Row " + metaRow + ", Column " + metaColumn, "");
        }
    }
    
    public void setReportFilename() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Choose a Report Name");
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(new File("Data"));
        if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            generateReport(chooser.getSelectedFile());
        }
    }
    
    public void generateReport(final File reportFile) {
        Thread thread = new Thread() {
            public void run() {
                String filename = reportFile.getName();
                FileOutputStream out = null;
                PrintStream ps = null;
                
                String outputString = "";
                float cy3, cy5;
                
                try {
                    ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(reportFile)));
                } catch (IOException e) {
                    MessageDisplay messageDisplay = new MessageDisplay(getFrame(), "Can't write output file: " + filename, "");
                }
                try {
                    int size = slideData.getSize();
                    
                    org.tigr.util.awt.ProgressBar pb = new org.tigr.util.awt.ProgressBar(getFrame(), "Writing report: " + filename, Color.green, Color.green, Color.black, size);
                    pb.drawProgressBar();
                    
                    ps.println("Report for SlideFile: " + slideData.getSlideDataName());
                    ps.println("Normalization: " + SlideData.normalizationString(slideData.getNormalizedState()) + "\tThresholds: " + canvas.getLowDifference() + "/" + canvas.getHighDifference());
                    outputString = "Row\tColumn\tMetaRow\tMetaColumn\tSubRow\tSubColumn\tCy3\tCy5\tCy5/Cy3";
                    for (int i = 0; i < TMEV.getFieldNames().length; i++) {
                        outputString += "\t" + TMEV.getFieldNames()[i];
                    }
                    ps.println(outputString);
                    ISlideDataElement sde;
                    for (int i = 0; i < indices.length; i++) {
                        outputString = "";
                        
                        sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                        //!
                        if (sde != null) {
                            for (int j = 0; j < TMEV.getFieldNames().length; j++) {
                                outputString += "\t" + sde.getFieldAt(j);
                            }
                            cy3 = sde.getIntensity(ISlideDataElement.CY3);
                            cy5 = sde.getIntensity(ISlideDataElement.CY5);
                            
                            if (canvas.getThresholds() == true) {
                                if ((cy3+cy5 > 0) &&
                                (cy3/(cy3+cy5) > canvas.getHighDifference() ||
                                (cy3/(cy3+cy5) < canvas.getLowDifference()))) {
                                    ps.println(sde.getRow(ISlideDataElement.BASE) + "\t" +
                                    sde.getColumn(ISlideDataElement.BASE) + "\t" +
                                    sde.getRow(ISlideDataElement.META) + "\t" +
                                    sde.getColumn(ISlideDataElement.META) + "\t" +
                                    sde.getRow(ISlideDataElement.SUB) + "\t" +
                                    sde.getColumn(ISlideDataElement.SUB) + "\t" +
                                    cy3 + "\t" +
                                    cy5 + "\t" +
                                    sde.getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, ISlideDataElement.BASE) +
                                    outputString);
                                }
                            } else {
                                ps.println(sde.getRow(ISlideDataElement.BASE) + "\t" +
                                sde.getColumn(ISlideDataElement.BASE) + "\t" +
                                sde.getRow(ISlideDataElement.META) + "\t" +
                                sde.getColumn(ISlideDataElement.META) + "\t" +
                                sde.getRow(ISlideDataElement.SUB) + "\t" +
                                sde.getColumn(ISlideDataElement.SUB) + "\t" +
                                cy3 + "\t" +
                                cy5 + "\t" +
                                sde.getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, ISlideDataElement.BASE) +
                                outputString);
                            }
                        }
                        
                        pb.increment(1);
                    }
                    ps.close();
                } catch (Exception e) {
                    MessageDisplay messageDisplay = new MessageDisplay(getFrame(), "Can't write: " + filename, "");
                }
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.run();
    }
    
    public void normalizeData(int mode) {
        //SlideData slideData = getData(); IS THIS IMPORTANT?
        //DisplayPanel displayPanel = ((DisplayFrame) parent).getDisplayPanel();
        
        Properties properties = new Properties();
        if(mode == ISlideData.LINEAR_REGRESSION){
            LinRegNormInitDialog dialog = new LinRegNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Float.toString(dialog.getSD()));
                properties.setProperty("mode", dialog.getMode());
                dialog.dispose();
            }
            else{
                return;
            }
        }
        
        else if(mode == ISlideData.RATIO_STATISTICS_95 || mode == ISlideData.RATIO_STATISTICS_99){
            RatioStatsNormInitDialog dialog = new RatioStatsNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("confidence-interval", Integer.toString(dialog.getCI()));
                dialog.dispose();
            }
            else{
                return;
            }
        }
        
        else if(mode == ISlideData.ITERATIVE_LOG){
            IterativeLogMCNormInitDialog dialog = new IterativeLogMCNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Float.toString(dialog.getSD()));
                dialog.dispose();
            }
            else{
                return;
            }
        }
        
        
        setCursor(Cursor.WAIT_CURSOR);
        
        slideData.applyNormalization(mode, properties);
        
        //Handle all drawing as necessary
        //long xVal, yVal;
        //xVal = displayPanel.xLastValue;
        //yVal = displayPanel.yLastValue;
        
        //canvas.setSlideData(slideData); IS THIS NECESSARY?
        //canvas.setUpperLimits(xVal, yVal); //Should be done, but think about the panel
        refreshSlide();
        //canvas.setXYScrollbars(slideData.getMaxIntensity(ISlideDataElement.CY3), slideData.getMaxIntensity(ISlideDataElement.CY5));
        //if (xVal <= displayPanel.xUpperLimitScrollbar.getMaximum()) displayPanel.updateXUpperLimitScrollbar(xVal);
        //else displayPanel.updateXUpperLimitScrollbar(displayPanel.xUpperLimitScrollbar.getMaximum());
        //if (yVal <= displayPanel.yUpperLimitScrollbar.getMaximum()) displayPanel.updateYUpperLimitScrollbar(yVal);
        //else displayPanel.updateYUpperLimitScrollbar(displayPanel.yUpperLimitScrollbar.getMaximum());
        //Should this second one be necessary?
        //canvas.drawSlide();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void normalizeDataList(int mode) {
        //SlideData slideData = getData(); IS THIS IMPORTANT?
        
        setCursor(Cursor.WAIT_CURSOR);
        Properties properties = new Properties();
        if(mode == ISlideData.LINEAR_REGRESSION){
            LinRegNormInitDialog dialog = new LinRegNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Double.toString(dialog.getSD()));
                properties.setProperty("mode", dialog.getMode());
                dialog.dispose();
            }
            else{
                return;
            }
        }
        
        else if(mode == ISlideData.RATIO_STATISTICS_95 || mode == ISlideData.RATIO_STATISTICS_99){
            RatioStatsNormInitDialog dialog = new RatioStatsNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("confidence-interval", Integer.toString(dialog.getCI()));
                dialog.dispose();
            }
            else{
                return;
            }
        }
        
        else if(mode == ISlideData.ITERATIVE_LOG){
            IterativeLogMCNormInitDialog dialog = new IterativeLogMCNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Double.toString(dialog.getSD()));
                dialog.dispose();
            }
            else{
                return;
            }
        }
        //Should change to -> slideData.applyNormalizationList(normalizationMode);
        slideData.applyNormalization(mode, properties);
        
        //Handle all drawing as necessary
        //canvas.setSlideData(slideData); IS THIS NECESSARY?
        //canvas.setUpperLimits(((DisplayFrame) parent).getDisplayPanel().xLastValue, ((DisplayFrame) parent).getDisplayPanel().yLastValue);
        refreshSlide();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void refreshSlide() {
        systemEnable(TMEV.DATA_AVAILABLE);
        repaint();
    }
    
    public void createScatterPlot(int scale) {
        JFrame graphFrame;
        GraphViewer graph;
        GraphPoint gp;
        GraphLine gl;
        GraphTick gt;
        ISlideDataElement sde;
        
        try {
            if (scale == AC.LOG) {
                int maxCy3 = (int) Xcon.log10(slideData.getMaxIntensity(ISlideDataElement.CY3)) + 1;
                int maxCy5 = (int) Xcon.log10(slideData.getMaxIntensity(ISlideDataElement.CY5)) + 1;
                int minCy3 = (int) Xcon.log10(slideData.getMinIntensity(ISlideDataElement.CY3, false));
                int minCy5 = (int) Xcon.log10(slideData.getMinIntensity(ISlideDataElement.CY5, false));
                
                int xGap = ((int) (maxCy3 - minCy3));
                int yGap = ((int) (maxCy5 - minCy5));
                
                //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY3, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY3));
                //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY5, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY5));
                
                //System.out.println(minCy3 + "\t" + maxCy3);
                //System.out.println(minCy5 + "\t" + maxCy5);
                //System.out.println(xGap + "\t" + yGap);
                
                graphFrame = new JFrame("Log Cy3 vs. Log Cy5");
                graph = new GraphViewer(graphFrame, 0, 500, 0, 500, minCy3, maxCy3, minCy5, maxCy5, 100, 100, 100, 100, "Log Cy3 vs. Log Cy5 - " + SlideData.normalizationString(slideData.getNormalizedState()), "Log Cy3", "Log Cy5");
                
                graph.setXAxisValue(minCy5);
                graph.setYAxisValue(minCy3);
                
                for (int i = minCy3 + 1; i <= maxCy3; i++) {
                    gl = new GraphLine(i, minCy5, i, maxCy5, Color.yellow);
                    graph.addGraphElement(gl);
                }
                
                for (int i = minCy5 + 1; i <= maxCy5; i++) {
                    gl = new GraphLine(minCy3, i, maxCy3, i, Color.yellow);
                    graph.addGraphElement(gl);
                }
                
                float cy3, cy5;
                double logCy3, logCy5;
                for (int i = 0; i < indices.length; i++) {
                    sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    
                    cy3 = sde.getIntensity(ISlideDataElement.CY3);
                    cy5 = sde.getIntensity(ISlideDataElement.CY5);
                    logCy3 = Xcon.log10(cy3);
                    logCy5 = Xcon.log10(cy5);
                    
                    if (canvas.thresholdsOn == true) {
                        if ((cy3 + cy5 > 0) && ((cy3/(cy3+cy5) > canvas.highDifference) || ((double) cy3 / (cy3 + cy5) < canvas.lowDifference))) {
                            gp = new GraphPoint(logCy3, logCy5, Color.red, 3);
                            graph.addGraphElement(gp);
                        } else {
                            gp = new GraphPoint(logCy3, logCy5, Color.blue, 3);
                            graph.addGraphElement(gp);
                        }
                    } else {
                        gp = new GraphPoint(logCy3, logCy5, Color.blue, 3);
                        graph.addGraphElement(gp);
                    }
                }
                for (int i = minCy5; i <= maxCy5; i++) {
                    gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "1.0E" + i, Color.black);
                    graph.addGraphElement(gt);
                }
                for (int i = minCy3; i <= maxCy3; i++) {
                    gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "1.0E" + i, Color.black);
                    graph.addGraphElement(gt);
                }
                
                graphFrame.setSize(500, 500);
                graph.setVisible(true);
            } else if (scale == AC.LINEAR) {
                float maxCy3 = slideData.getMaxIntensity(ISlideDataElement.CY3);
                float maxCy5 = slideData.getMaxIntensity(ISlideDataElement.CY5);
                
                float cy3Partition = maxCy3;
                float cy5Partition = maxCy5;
                long cy3Block = 0, cy5Block = 0;
                int power;
                
                for (power = 0; (! ((cy3Partition < 10) && (cy3Partition > 0))); power++) cy3Partition /= 10;
                cy3Block = (long) ((int) (cy3Partition * 10) * Math.pow(10, (power - 2)));
                for (power = 0; (! ((cy5Partition < 10) && (cy5Partition > 0))); power++) cy5Partition /= 10;
                cy5Block = (long) ((int) (cy5Partition * 10) * Math.pow(10, (power - 2)));
                
                maxCy3 = cy3Block * 11;
                maxCy5 = cy5Block * 11;
                
                graphFrame = new JFrame("Cy3 vs. Cy5");
                graph = new GraphViewer(graphFrame, 0, 500, 0, 500, 0, maxCy3, 0, maxCy5, 75, 75, 75, 75, "Cy3 vs. Cy5 - " + SlideData.normalizationString(slideData.getNormalizedState()), "Cy3", "Cy5");
                
                for (int i = 1; i < 12; i++) {
                    gl = new GraphLine(0, i * cy5Block, maxCy3, i * cy5Block, Color.yellow);
                    graph.addGraphElement(gl);
                    gl = new GraphLine(i * cy3Block, 0, i * cy3Block, maxCy5, Color.yellow);
                    graph.addGraphElement(gl);
                }
                
                float cy3, cy5;
                for (int i = 0; i < indices.length; i++) {
                    sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    cy3 = sde.getIntensity(ISlideDataElement.CY3);
                    cy5 = sde.getIntensity(ISlideDataElement.CY5);
                    if (canvas.thresholdsOn == true) {
                        if ((cy3 + cy5 > 0) && ((cy3 / (cy3 + cy5) > canvas.highDifference) || ((double) cy3 / (cy3 + cy5) < canvas.lowDifference))) {
                            gp = new GraphPoint(cy3, cy5, Color.red, 3);
                            graph.addGraphElement(gp);
                        } else {
                            gp = new GraphPoint(cy3, cy5, Color.blue, 3);
                            graph.addGraphElement(gp);
                        }
                    } else {
                        gp = new GraphPoint(cy3, cy5, Color.blue, 3);
                        graph.addGraphElement(gp);
                    }
                }
                for (int i = 0; i < 12; i++) {
                    gt = new GraphTick(i * cy3Block, 8, Color.black, GC.HORIZONTAL, GC.C, "" + i * cy3Block, Color.black);
                    graph.addGraphElement(gt);
                    gt = new GraphTick(i * cy5Block, 8, Color.black, GC.VERTICAL, GC.C, "" + i * cy5Block, Color.black);
                    graph.addGraphElement(gt);
                }
                graphFrame.setSize(500, 500);
                graph.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        Graph2D graph;
        double[] rawData = new double[slideData.size() * 2];
        DataSet dataSet;
        Axis xAxis, yAxis;
        Markers markers;
        ISlideDataElement sde;
         
        graph = new Graph2D();
        graph.drawzero = false;
        graph.drawgrid = false;
        graph.borderTop = 50;
        graph.borderRight = 50;
        graph.setDataBackground(Color.white);
         
        markers = new Markers();
        markers.AddMarker(2, new boolean[]{true, true}, new int[]{0, 0}, new int[]{0, 0});
        boolean[] moveDraw = new boolean[]{false, true, true, true, true};
        int[] xValues = new int[]{-1, 1, 1, -1, -1};
        int[] yValues = new int[]{1, 1, -1, -1, 1};
        markers.AddMarker(5, moveDraw, xValues, yValues);
         
        graph.setMarkers(markers);
        plotFrame.getContentPane().setLayout(new BorderLayout());
        plotFrame.getContentPane().add("Center", graph);
         
        for (int i = 0; i < slideData.size(); i++)
            {
            sde = slideData.getElementAt(i);
            rawData[i*2] = sde.getIntensity(ISlideDataElement.CY3);
            rawData[i*2+1] = sde.getIntensity(ISlideDataElement.CY5);
            }
         
        dataSet = graph.loadDataSet(rawData, slideData.size());
        dataSet.linestyle = DataSet.NOLINE;
        dataSet.marker = 1;
        dataSet.markerscale = 1;
        dataSet.markercolor = new Color(0, 0, 255);
        //dataSet.legend(200, 100, "Array Data");
        dataSet.legendColor(Color.black);
         
        xAxis = graph.createAxis(Axis.BOTTOM);
        xAxis.attachDataSet(dataSet);
        xAxis.setTitleText("Cy3");
        xAxis.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
        xAxis.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
         
        yAxis = graph.createAxis(Axis.LEFT);
        yAxis.attachDataSet(dataSet);
        yAxis.setTitleText("Cy5");
        yAxis.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
        yAxis.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
         
        plotFrame.move(200, 200);
        plotFrame.setSize(500, 500);
        plotFrame.setVisible(true);
         */
    }
    
    public void createRatioProductPlotByBlock() {
        JFrame graphFrame;
        BlockGraphViewer graph;
        GraphPoint gp;
        GraphLine gl;
        GraphTick gt;
        ISlideDataElement sde;
        
        try {
            int maxCy3 = (int) Xcon.log10(slideData.getMaxProduct(ISlideDataElement.CY3, ISlideDataElement.CY5)) + 1;
            int maxCy5 = (int) Xcon.log10(slideData.getMaxRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR)) + 1;
            int minCy3 = (int) Xcon.log10(slideData.getMinProduct(ISlideDataElement.CY3, ISlideDataElement.CY5, false, 2));
            int minCy5 = (int) Xcon.log10(slideData.getMinRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR));
            
            int xGap = ((int) (maxCy3 - minCy3));
            int yGap = ((int) (maxCy5 - minCy5));
            
            //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY3, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY3));
            //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY5, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY5));
            
            //System.out.println(minCy3 + "\t" + maxCy3);
            //System.out.println(minCy5 + "\t" + maxCy5);
            //System.out.println(xGap + "\t" + yGap);
            int metaRows, metaColumns;
            metaRows =  ((ISlideDataElement)slideData.getSlideDataElement(slideData.getSize()-1)).getRow(ISlideDataElement.META);
            metaColumns =  ((ISlideDataElement)slideData.getSlideDataElement(slideData.getSize()-1)).getColumn(ISlideDataElement.META);
            
            //    System.out.println("metarows = "+metaRows+"metacols"+metaColumns);
            
            graphFrame = new JFrame("Log Product vs. Log Ratio, by Metablock");
            graph = new BlockGraphViewer(graphFrame, metaRows, metaColumns, 0, 500, 0, 500, minCy3, maxCy3, minCy5, maxCy5, 100, 100, 100, 100, "Log Product vs. Log Ratio - " + SlideData.normalizationString(slideData.getNormalizedState()), "Log (Cy3 x Cy5)", "Log (Cy5 / Cy3)");
            
            graph.setXAxisValue(0);
            graph.setYAxisValue(minCy3);
            
            for (int i = minCy3 + 1; i <= maxCy3; i++) {
                gl = new GraphLine(i, minCy5, i, maxCy5, Color.yellow);
                graph.addGraphElement(gl);
            }
            
            for (int i = minCy5; i <= maxCy5; i++) {
                if (i != 0) {
                    gl = new GraphLine(minCy3, i, maxCy3, i, Color.yellow);
                    graph.addGraphElement(gl);
                }
            }
            
            float cy3, cy5;
            double logCy3, logCy5;
            int metaBlock, metaRow, metaCol;
            Color[] metaColors = graph.getColors();
            for (int i = 0; i < indices.length; i++) {
                sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                
                cy3 = sde.getIntensity(ISlideDataElement.CY3);
                cy5 = sde.getIntensity(ISlideDataElement.CY5);
                metaRow = sde.getRow(ISlideDataElement.META);
                metaCol = sde.getColumn(ISlideDataElement.META);
                metaBlock = (metaRow * metaCol) + ((metaRow - 1) * (metaColumns - metaCol))-1;
                //System.out.println("metablock = " + metaBlock);
                //metaBlock = (2 * (sde.getRow(ISlideDataElement.META) - 1) + sde.getColumn(ISlideDataElement.META) - 1); //Hardcoded for 2x6 block configuration
                logCy3 = Xcon.log10(cy3 * cy5);
                logCy5 = Xcon.log10((double) cy5 / (double) cy3);
                
                if(metaBlock < metaColors.length)
                    gp = new GraphPoint(logCy3, logCy5, metaColors[metaBlock], 3);
                else
                    gp = new GraphPoint(logCy3, logCy5, Color.black, 3);
                graph.addGraphElement(gp);
            }
            
            for (int i = minCy5; i <= maxCy5; i++) {
                if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "0" + i, Color.black);
                else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "1.0E" + i, Color.black);
                graph.addGraphElement(gt);
            }
            
            for (int i = minCy3; i <= maxCy3; i++) {
                if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "0" + i, Color.black);
                else gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "1.0E" + i, Color.black);
                graph.addGraphElement(gt);
            }
            
            graphFrame.setSize(500, 500);
            graph.setVisible(true);
        } catch (Exception e) {
            System.out.println("Exception (createRatioProduct()): " + e);
            e.printStackTrace();
        }
    }
    
    public void createRatioProductPlot() {
        JFrame graphFrame;
        GraphViewer graph;
        GraphPoint gp;
        GraphLine gl;
        GraphTick gt;
        ISlideDataElement sde;
        
        try {
            int maxCy3 = (int) Xcon.log10(slideData.getMaxProduct(ISlideDataElement.CY3, ISlideDataElement.CY5)) + 1;
            int maxCy5 = (int) Xcon.log10(slideData.getMaxRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR)) + 1;
            int minCy3 = (int) Xcon.log10(slideData.getMinProduct(ISlideDataElement.CY3, ISlideDataElement.CY5, false));
            int minCy5 = (int) Xcon.log10(slideData.getMinRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR));
            
            int xGap = ((int) (maxCy3 - minCy3));
            int yGap = ((int) (maxCy5 - minCy5));
            
            //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY3, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY3));
            //System.out.println(slideData.getMinIntensity(ISlideDataElement.CY5, false) + "\t" + slideData.getMaxIntensity(ISlideDataElement.CY5));
            
            //System.out.println(minCy3 + "\t" + maxCy3);
            //System.out.println(minCy5 + "\t" + maxCy5);
            //System.out.println(xGap + "\t" + yGap);
            
            graphFrame = new JFrame("Log Product vs. Log Ratio");
            graph = new GraphViewer(graphFrame, 0, 500, 0, 500, minCy3, maxCy3, minCy5, maxCy5, 100, 100, 100, 100, "Log Product vs. Log Ratio - " + SlideData.normalizationString(slideData.getNormalizedState()), "Log (Cy3 x Cy5)", "Log (Cy5 / Cy3)");
            
            graph.setXAxisValue(0);
            graph.setYAxisValue(minCy3);
            
            for (int i = minCy3 + 1; i <= maxCy3; i++) {
                gl = new GraphLine(i, minCy5, i, maxCy5, Color.yellow);
                graph.addGraphElement(gl);
            }
            
            for (int i = minCy5; i <= maxCy5; i++) {
                if (i != 0) {
                    gl = new GraphLine(minCy3, i, maxCy3, i, Color.yellow);
                    graph.addGraphElement(gl);
                }
            }
            
            float cy3, cy5;
            double logCy3, logCy5;
            for (int i = 0; i < indices.length; i++) {
                sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                
                cy3 = sde.getIntensity(ISlideDataElement.CY3);
                cy5 = sde.getIntensity(ISlideDataElement.CY5);
                logCy3 = Xcon.log10(cy3 * cy5);
                logCy5 = Xcon.log10(cy5/cy3);
                
                if (canvas.thresholdsOn == true) {
                    if ((cy3 + cy5 > 0) && ((cy3/(cy3+cy5) > canvas.highDifference) || ((double) cy3 / (cy3 + cy5) < canvas.lowDifference))) {
                        gp = new GraphPoint(logCy3, logCy5, Color.red, 3);
                        graph.addGraphElement(gp);
                    } else {
                        gp = new GraphPoint(logCy3, logCy5, Color.blue, 3);
                        graph.addGraphElement(gp);
                    }
                } else {
                    gp = new GraphPoint(logCy3, logCy5, Color.blue, 3);
                    graph.addGraphElement(gp);
                }
            }
            
            for (int i = minCy5; i <= maxCy5; i++) {
                if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "0" + i, Color.black);
                else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "1.0E" + i, Color.black);
                graph.addGraphElement(gt);
            }
            
            for (int i = minCy3; i <= maxCy3; i++) {
                if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "0" + i, Color.black);
                else gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "1.0E" + i, Color.black);
                graph.addGraphElement(gt);
            }
            
            graphFrame.setSize(500, 500);
            graph.setVisible(true);
        } catch (Exception e) {
            System.out.println("Exception (createRatioProduct()): " + e);
        }
    }
    
    public void createHistogram(int scale) {
        JFrame graphFrame;
        GraphViewer graph;
        GraphLine gl;
        GraphBar gb;
        GraphTick gt;
        ISlideDataElement sde;
        
        try {
            if (scale == AC.LOG) {
                int partitions = 103;
                double spread = 0.0;
                
                double bottomValue = -5.0, topValue = 5.0;
                double ratio = 0.0;
                int maxCount = 0;
                int[] bin = new int[partitions];
                
                double tempDouble = 0.0;
                
                if (partitions % 2 == 1) { //Odd number
                    spread = (double) (topValue - bottomValue) / (partitions - 3);
                }
                
                double tx, ty;
                for (int i = 0; i < indices.length; i++) {
                    sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    ratio = sde.getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LOG);
                    
                    for (int j = 0; j < partitions; j++) {
                        if (j == 0) {
                            tx = Double.MIN_VALUE;
                            ty = bottomValue - (spread / 2);
                        } else if (j == partitions - 1) {
                            tx = topValue + (spread / 2);
                            ty = Double.MAX_VALUE;
                        } else {
                            tx = bottomValue - (spread / 2) + ((j - 1) * spread);
                            ty = tx + spread;
                        }
                        
                        if (((ratio >= tx) && (ratio <= ty)) && (sde.hasNoZeros())) bin[j]++;
                    }
                }
                
                for (int i = 0; i < bin.length; i++) if (maxCount < bin[i]) maxCount = bin[i];
                int power, maxBlock = 0;
                float maxPartition = (float) maxCount;
                for (power = 0; (! ((maxPartition < 10) && (maxPartition > 0))); power++) maxPartition /= 10;
                maxBlock = (int) ((int) (maxPartition * 10) * Math.pow(10, (power - 2)));
                maxCount = maxBlock * 11;
                
                graphFrame = new JFrame("Log (Cy5 / Cy3)");
                graph = new GraphViewer(graphFrame, 0, 500, 0, 500, bottomValue - (3 * spread / 2), topValue + (3 * spread / 2), 0, maxCount, 75, 75, 75, 75, "Log (Cy5 / Cy3) - " + SlideData.normalizationString(slideData.getNormalizedState()), "Ratio", "Count");
                
                for (int i = 1; i < 12; i++) { //Guide lines
                    //gl = new GraphLine(bottomValue, i * cy5Block, topValue, i * cy5Block, Color.yellow);
                    //graph.addGraphElement(gl);
                    //gl = new GraphLine(i * cy3Block, 0, i * cy3Block, maxCy5, Color.yellow);
                    //graph.addGraphElement(gl);
                }
                double lowLog = Math.log(1 / canvas.getFactor());
                double highLog = Math.log(canvas.getFactor());
                
                for (int i = 0; i < bin.length; i++) { //Histogram Bars
                    if (canvas.thresholdsOn == true) {
                        if (((bottomValue + ((i - 1) * spread)) <= lowLog) || ((bottomValue + ((i - 1) * spread)) >= highLog)) {
                            gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                            bin[i], Color.red, GraphBar.VERTICAL, GraphBar.SOLID);
                        } else {
                            gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                            bin[i], Color.blue, GraphBar.VERTICAL, GraphBar.SOLID);
                        }
                    } else {
                        gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                        bin[i], Color.blue, GraphBar.VERTICAL, GraphBar.SOLID);
                    }
                    graph.addGraphElement(gb);
                }
                
                /*if (false) // For a tick on every bin
                    {
                    for (int i = 0; i < bin.length + 1; i++) //X-Axis ticks
                        {
                        tempDouble = bottomValue - (3 * spread / 2) + (i * spread);
                        if (i == 0) gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Minimum", Color.black);
                        else if (i == bin.length) gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Maximum", Color.black);
                        else
                            {
                            tempDouble *= 100;
                            tempDouble /= 100;
                            gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "" + tempDouble, Color.black);
                            }
                        graph.addGraphElement(gt);
                        }
                    }
                else
                    {*/
                tempDouble = bottomValue - (3 * spread / 2) + (0 * spread);
                gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Minimum", Color.black);
                graph.addGraphElement(gt);
                
                tempDouble = bottomValue - (3 * spread / 2) + (bin.length * spread);
                gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Maximum", Color.black);
                graph.addGraphElement(gt);
                
                for (int i = (int) bottomValue; i <= (int) topValue; i++) {
                    gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "" + i, Color.black);
                    graph.addGraphElement(gt);
                }
                //}
                
                for (int i = 1; i < 12; i++) { //Y-Axis ticks
                    gt = new GraphTick(i * maxBlock, 8, Color.black, GC.VERTICAL, GC.C, "" + i * maxBlock, Color.black);
                    graph.addGraphElement(gt);
                }
                
                graphFrame.setSize(500, 500);
                graph.setVisible(true);
            } else if (scale == AC.LINEAR) {
                int partitions = 53;
                double spread = 0.0;
                
                double bottomValue = 0.0, topValue = 5.0;
                double ratio = 0.0;
                int maxCount = 0;
                int[] bin = new int[partitions];
                
                double tempDouble = 0.0;
                
                if (partitions % 2 == 1) { //Odd number
                    spread = (double) (topValue - bottomValue) / (partitions - 3);
                }
                
                double tx, ty;
                for (int i = 0; i < indices.length; i++) {
                    sde = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    ratio = sde.getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR);
                    
                    for (int j = 0; j < partitions; j++) {
                        if (j == 0) {
                            tx = Double.MIN_VALUE;
                            ty = bottomValue - (spread / 2);
                        } else if (j == partitions - 1) {
                            tx = topValue + (spread / 2);
                            ty = Double.MAX_VALUE;
                        } else {
                            tx = bottomValue - (spread / 2) + ((j - 1) * spread);
                            ty = tx + spread;
                        }
                        
                        if (((ratio >= tx) && (ratio <= ty)) && (sde.hasNoZeros())) bin[j]++;
                    }
                }
                
                for (int i = 0; i < bin.length; i++) if (maxCount < bin[i]) maxCount = bin[i];
                int power, maxBlock = 0;
                float maxPartition = (float) maxCount;
                for (power = 0; (! ((maxPartition < 10) && (maxPartition > 0))); power++) maxPartition /= 10;
                maxBlock = (int) ((int) (maxPartition * 10) * Math.pow(10, (power - 2)));
                maxCount = maxBlock * 11;
                
                graphFrame = new JFrame("Cy5 / Cy3");
                graph = new GraphViewer(graphFrame, 0, 500, 0, 500, bottomValue - (3 * spread / 2), topValue + (3 * spread / 2), 0, maxCount, 75, 75, 75, 75, "Cy5 / Cy3 - " + SlideData.normalizationString(slideData.getNormalizedState()), "Ratio", "Count");
                
                for (int i = 1; i < 12; i++) { //Guide lines
                    //gl = new GraphLine(bottomValue, i * cy5Block, topValue, i * cy5Block, Color.yellow);
                    //graph.addGraphElement(gl);
                    //gl = new GraphLine(i * cy3Block, 0, i * cy3Block, maxCy5, Color.yellow);
                    //graph.addGraphElement(gl);
                }
                
                double lowLog = 1 / canvas.getFactor();
                double highLog = canvas.getFactor();
                
                for (int i = 0; i < bin.length; i++) { //Histogram Bars
                    if (canvas.thresholdsOn == true) {
                        if (((bottomValue + ((i - 1) * spread)) <= lowLog) || ((bottomValue + ((i - 1) * spread)) >= highLog)) {
                            gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                            bin[i], Color.red, GraphBar.VERTICAL, GraphBar.SOLID);
                        } else {
                            gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                            bin[i], Color.blue, GraphBar.VERTICAL, GraphBar.SOLID);
                        }
                    } else {
                        gb = new GraphBar(bottomValue - (3 * spread / 2) + (i * spread), bottomValue - (spread / 2) + (i * spread),
                        bin[i], Color.blue, GraphBar.VERTICAL, GraphBar.SOLID);
                    }
                    graph.addGraphElement(gb);
                }
                
                /*if (false) // For a tick on every bin
                    {
                    for (int i = 0; i < bin.length + 1; i++) //X-Axis ticks
                        {
                        tempDouble = bottomValue - (3 * spread / 2) + (i * spread);
                        if (i == 0) gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Minimum", Color.black);
                        else if (i == bin.length) gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Maximum", Color.black);
                        else
                            {
                            tempDouble *= 100;
                            tempDouble /= 100;
                            gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "" + tempDouble, Color.black);
                            }
                        graph.addGraphElement(gt);
                        }
                    }
                else
                    {*/
                tempDouble = bottomValue - (3 * spread / 2) + (0 * spread);
                gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Minimum", Color.black);
                graph.addGraphElement(gt);
                
                tempDouble = bottomValue - (3 * spread / 2) + (bin.length * spread);
                gt = new GraphTick(tempDouble, 8, Color.black, GC.HORIZONTAL, GC.C, "Maximum", Color.black);
                graph.addGraphElement(gt);
                
                for (int i = (int) bottomValue; i <= (int) topValue; i++) {
                    gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, "" + i, Color.black);
                    graph.addGraphElement(gt);
                }
                //}
                
                for (int i = 1; i < 12; i++) { //Y-Axis ticks
                    gt = new GraphTick(i * maxBlock, 8, Color.black, GC.VERTICAL, GC.C, "" + i * maxBlock, Color.black);
                    graph.addGraphElement(gt);
                }
                
                graphFrame.setSize(500, 500);
                graph.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        //HistogramViewer histogramViewer = null;
        //JFrame histogramFrame = null;
        
        /* If this compiles, this is not necessary
        int[] bin;
        int highY = 0;
        Color barColor = Color.blue;
        Color highlightColor = Color.red;
         */
        
        /*
        if (scale == SingleArrayViewer.LOG)
            {
            histogramFrame = new JFrame(slideData.getSlideDataName() + " - Ratio Histogram (Log)");
            histogramViewer = new HistogramViewer(histogramFrame, 0, 400, 0, 400, -1, 1, 0, 200, 50, 50, 50, 50);
            histogramViewer.setSlideData(slideData);
            histogramViewer.setScale(AC.LOG);
            histogramFrame.setIconImage(new ImageIcon(org.tigr.microarray.SingleArrayViewer.class.getResource("/org/tigr/images/expression.gif")).getImage());
            histogramFrame.setSize(600, 600);
            histogramFrame.move(300, 100);
            histogramFrame.setVisible(true);
            }
        else if (scale == SingleArrayViewer.LINEAR)
            {
            histogramFrame = new JFrame(slideData.getSlideDataName() + " - Ratio Histogram (Linear)");
            histogramViewer = new HistogramViewer(histogramFrame, 0, 400, 0, 400, 0, 5, 0, 200, 50, 50, 50, 50);
            histogramViewer.setSlideData(slideData);
            histogramViewer.setScale(AC.LINEAR);
            histogramFrame.setIconImage(new ImageIcon(org.tigr.microarray.SingleArrayViewer.class.getResource("/org/tigr/images/expression.gif")).getImage());
            histogramFrame.setSize(600, 600);
            histogramFrame.move(300, 100);
            histogramFrame.setVisible(true);
            }
         */
    }
    
    /*
    public void newSubArray()
        {
        String source;
        double low = displayCanvas.getLowDifference();
        double high = displayCanvas.getHighDifference();
     
        if (displayCanvas.getThresholds()) source = "SubArray: Lower " + (Math.rint(low * 100) / 100) + ", Higher " + (Math.rint(high * 100) / 100);
        else source = "SubArray: No Thresholds";
     
        SubApplet subApplet = new SubApplet(source);
        SubFrame subFrame = new SubFrame(subApplet);
        SubCanvas subCanvas = subApplet.getSubCanvas();
        SubPanel subPanel = subFrame.getSubPanel();
        Vector holding;
        SlideData newSlideData;
        ISlideDataElement sde, sdo;
     
        subApplet.changePaletteStyle(displayCanvas.getPaletteStyle());
        subApplet.setThresholds(displayCanvas.getThresholds());
        //Should this be nice?
        subCanvas.setScale(displayCanvas.getScale());
        switch (displayCanvas.getPaletteStyle())
            {
            case DisplayCanvas.BLUETORED: subFrame.handleItems(subFrame.blueToRedItem); break;
            case DisplayCanvas.GREENRED: subFrame.handleItems(subFrame.greenRedItem); break;
            case DisplayCanvas.OVERLAY: subFrame.handleItems(subFrame.overlayItem); break;
            }
        if (displayCanvas.getThresholds()) subFrame.handleItems(subFrame.setGreenRedThresholdsOnItem);
        else subFrame.handleItems(subFrame.setGreenRedThresholdsOffItem);
        if (displayCanvas.getScale()) subFrame.handleItems(subFrame.setScaleOnItem);
        else subFrame.handleItems(subFrame.setScaleOffItem);
     
        if (displayCanvas.getThresholds())
            {
            holding = new Vector();
            for (int i = 0; i < slideData.getSlideDataSize(); i++)
                {
                try
                    {
                    sdo = (ISlideDataElement) slideData.getSlideDataElementAt(i);
                    sde = new SlideDataElement(sdo.getRow(), sdo.getColumn(), sdo.getMetaRow(), sdo.getMetaColumn(),
                                            sdo.getSubRow(), sdo.getSubColumn(), sdo.getCy3(), sdo.getCy5(),
                                            sdo.getValues(), sdo.getIsNull());
                    int cy3 = (int) sde.getCy3();
                    int cy5 = (int) sde.getCy5();
                    if ((cy3 + cy5 > 0) && (((double) cy3 / (cy3 + cy5) > high)
                            || ((double) cy3 / (cy3 + cy5) < low))) holding.addElement(sde);
                    }
                catch (NullPointerException npe) {;}
                }
     
            if (holding.size() > 0)
                {
                int columns = (int) Math.ceil(Math.sqrt(holding.size() / 6));
                int rows = columns * 6;
     
                for (; (columns * rows) - holding.size() >= columns; rows--) {;}
     
                newSlideData = new SlideData(columns, rows, slideData.getTrueColumns(), slideData.getTrueRows());
                newSlideData.setSlideDataName(slideData.getSlideDataName());
                newSlideData.setNormalizedState(slideData.getNormalizedState());
                int maxColumns = newSlideData.getColumns();
                for (int i = 0; i < holding.size(); i++)
                    {
                    try
                        {
                        sde = (ISlideDataElement) holding.elementAt(i);
                        sde.setColumn((i % maxColumns) + 1);
                        sde.setRow((i / maxColumns) + 1);
     
                        //Should this be one step?
                        sde.setSlideData(newSlideData);
                        newSlideData.addSlideDataElement(sde);
                        }
                    catch (Exception e) {System.out.println("Exception (DisplayApplet.createSubarray(): " + e);}
                    }
                subApplet.setData(newSlideData);
                subCanvas.setUpperLimits(displayCanvas.getTopCy3(), displayCanvas.getTopCy5());
                subPanel.setXYScrollbars(slideData.getMaxCy3(), slideData.getMaxCy5());
                subPanel.updateXUpperLimitScrollbar(displayCanvas.getTopCy3());
                subPanel.updateYUpperLimitScrollbar(displayCanvas.getTopCy5());
                subApplet.drawNewPicture(newSlideData);
                subCanvas.drawSlide();
                }
            }
        else
            {
            subApplet.setData(slideData);
            subApplet.drawNewPicture(slideData);
            subCanvas.drawSlide();
            }
        }
     
    public void createRegion(int metaRow, int metaColumn)
        {
        SubApplet subApplet;
        SubFrame subFrame;
        SubCanvas subCanvas;
        SubPanel subPanel;
        Vector holding;
        SlideData newSlideData;
        ISlideDataElement sde, sdo;
        int maxSubRow = 0, maxSubColumn = 0;
     
        holding = new Vector();
        for (int i = 0; i < slideData.getSlideDataSize(); i++)
            {
            try
                {
                sdo = (ISlideDataElement) slideData.getSlideDataElementAt(i);
                sde = new SlideDataElement(sdo.getRow(), sdo.getColumn(), sdo.getMetaRow(), sdo.getMetaColumn(),
                                            sdo.getSubRow(), sdo.getSubColumn(), sdo.getCy3(), sdo.getCy5(),
                                            sdo.getValues(), sdo.getIsNull());
                if ((sde.getMetaRow() == metaRow) && (sde.getMetaColumn() == metaColumn))
                    {
                    holding.addElement(sde);
                    if (sde.getSubRow() > maxSubRow) maxSubRow = sde.getSubRow();
                    if (sde.getSubColumn() > maxSubColumn) maxSubColumn = sde.getSubColumn();
                    }
                }
            catch (NullPointerException npe) {;}
            }
     
        if (holding.size() > 0)
            {
            subApplet = new SubApplet("Region: MetaColumn " + metaColumn + ", MetaRow " + metaRow);
            subFrame = new SubFrame(subApplet);
            subCanvas = subApplet.getSubCanvas();
            subPanel = subFrame.getSubPanel();
     
            subApplet.changePaletteStyle(displayCanvas.getPaletteStyle());
            subApplet.setThresholds(displayCanvas.getThresholds());
            //Should this be nice?
            subCanvas.setScale(displayCanvas.getScale());
            switch (displayCanvas.getPaletteStyle())
                {
                case DisplayCanvas.BLUETORED: subFrame.handleItems(subFrame.blueToRedItem); break;
                case DisplayCanvas.GREENRED: subFrame.handleItems(subFrame.greenRedItem); break;
                case DisplayCanvas.OVERLAY: subFrame.handleItems(subFrame.overlayItem); break;
                }
            if (displayCanvas.getThresholds()) subFrame.handleItems(subFrame.setGreenRedThresholdsOnItem);
            else subFrame.handleItems(subFrame.setGreenRedThresholdsOffItem);
            if (displayCanvas.getScale()) subFrame.handleItems(subFrame.setScaleOnItem);
            else subFrame.handleItems(subFrame.setScaleOffItem);
     
            newSlideData = new SlideData(maxSubColumn, maxSubRow, slideData.getTrueColumns(), slideData.getTrueRows());
            newSlideData.setSlideDataName(slideData.getSlideDataName());
            newSlideData.setNormalizedState(slideData.getNormalizedState());
            int maxColumns = newSlideData.getColumns();
            for (int i = 0; i < holding.size(); i++)
                {
                try
                    {
                    sde = (ISlideDataElement) holding.elementAt(i);
                    sde.setColumn((i % maxColumns) + 1);
                    sde.setRow((i / maxColumns) + 1);
     
                    //Should this be one step?
                    sde.setSlideData(newSlideData);
                    newSlideData.addSlideDataElement(sde);
                    }
                catch (Exception e) {System.out.println("Exception (SetMetaLocation.actionPerformed(): " + e);}
                }
            subApplet.setData(newSlideData);
            subCanvas.setUpperLimits(displayCanvas.getTopCy3(), displayCanvas.getTopCy5());
            subPanel.setXYScrollbars(slideData.getMaxCy3(), slideData.getMaxCy5());
            subPanel.updateXUpperLimitScrollbar(displayCanvas.getTopCy3());
            subPanel.updateYUpperLimitScrollbar(displayCanvas.getTopCy5());
            subApplet.drawNewPicture(newSlideData);
            subCanvas.drawSlide();
            }
     
        else
            {
            MessageDisplay messageDisplay = new MessageDisplay((DisplayFrame) getParent(), "No region: Row " + metaRow + ", Column " + metaColumn, "");
            }
        }
     */
    
    public void setThresholds(boolean state) {
        setCursor(Cursor.WAIT_CURSOR);
        canvas.setThresholds(state);
        //for (int i = 0; i < getPlotAppletVector().size(); i++)
        //	{
        //	plotApplet = (PlotApplet) getPlotAppletVector().elementAt(i);
        //	plotApplet.getPlotCanvas().setThresholds(state);
        //	}
        //for (int i = 0; i < getHistogramAppletVector().size(); i++)
        //	{
        //	histogramApplet = (HistogramApplet) histogramAppletVector.elementAt(i);
        //	histogramApplet.setThresholds(state);
        //	}
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void setFactor(float factor) {
        double lower;
        double higher;
        
        setCursor(Cursor.WAIT_CURSOR);
        canvas.setFactor(factor);
        
        try {
            lower = 1 / (factor + 1);
        } catch (NumberFormatException nfe) {
            lower = 1 / (canvas.getFactor() + 1);
        }
        try {
            higher = factor / (factor + 1);
        } catch (NumberFormatException nfe) {
            higher = canvas.getFactor() / (canvas.getFactor() + 1);
        }
        
        canvas.setLowDifference(lower);
        canvas.setHighDifference(higher);
        
        /*
        for (int i = 0; i < plotAppletVector.size(); i++)
            {
            plotApplet = (PlotApplet) plotAppletVector.elementAt(i);
            plotApplet.getPlotCanvas().setLowDifference(lower);
            plotApplet.getPlotCanvas().setHighDifference(higher);
            plotApplet.getPlotCanvas().setThresholds(true);
            }
        for (int i = 0; i < histogramAppletVector.size(); i++)
            {
            histogramApplet = (HistogramApplet) histogramAppletVector.elementAt(i);
            histogramApplet.setRegion(lower, higher);
            histogramApplet.setThresholds(true);
            }
        if (displayCanvas.getThresholds() == true) displayCanvas.drawSlide();
        ((DisplayFrame) getParent()).setGreenRedThresholdsOnItem.disable();
        ((DisplayFrame) getParent()).setGreenRedThresholdsOffItem.enable();
         */
        
        panel.updateThresholdScrollbar((double) (Math.rint(factor * 100) / 100));
        
        if (setThresholdsItem.isSelected()) refreshSlide();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void setUpperLimits(float cy3, float cy5) {
        setCursor(Cursor.WAIT_CURSOR);
        canvas.setUpperLimits(cy3, cy5);
        panel.xUpperLimitScrollbar.setValue((int) cy3);
        panel.yUpperLimitScrollbar.setValue((int) cy5);
        refreshSlide();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void changePaletteStyle(int paletteStyle) {
        switch (paletteStyle) {
            case SingleArrayViewerCanvas.BLUETORED:
                canvas.setPaletteStyle(SingleArrayViewerCanvas.BLUETORED);
                break;
            case SingleArrayViewerCanvas.GREENRED:
                canvas.setPaletteStyle(SingleArrayViewerCanvas.GREENRED);
                break;
            case SingleArrayViewerCanvas.OVERLAY:
                canvas.setPaletteStyle(SingleArrayViewerCanvas.OVERLAY);
                break;
        }
    }
    
    public void sort(int sortType) {
        /*
        If the sortType > 9000, then it is a standard type. Otherwise, it represents the
        index of the additional field on which to sort.
         */
        setCursor(Cursor.WAIT_CURSOR);
        
        //slideData.sort(sortType);
        SlideDataSorter sorter = new SlideDataSorter(slideData);
        indices = sorter.sort(indices, sortType);
        //for (int i=0; i<indices.size(); i++) {
        //    System.out.println("indices["+i+"]="+((Integer)indices.get(i)).intValue());
        //}
        
        //long hTopCy3 = canvas.getTopCy3();
        //long hTopCy5 = canvas.getTopCy5();
        
        //canvas.setSlideData(slideData); IS THIS NECESSARY?
        //canvas.setUpperLimits(hTopCy3, hTopCy5);
        refreshSlide();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public void saveImage() {
        final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(new File("Data"));
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String extension = "";
                if (f.isDirectory()) return true;
                int i = f.getName().lastIndexOf('.');
                if (i > 0) extension = f.getName().substring(i + 1).toLowerCase();
                if (extension.equals("bmp")) return true;
                else return false;
            }
            public String getDescription() {
                return "Bitmap Files (*.bmp)";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String extension = "";
                if (f.isDirectory()) return true;
                int i = f.getName().lastIndexOf('.');
                if (i > 0) extension = f.getName().substring(i + 1).toLowerCase();
                if (extension.equals("jpg") || extension.equals("jpeg")) return true;
                else return false;
            }
            public String getDescription() {
                return "JPEG Files (*.jpg; *.jpeg)";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String extension = "";
                if (f.isDirectory()) return true;
                int i = f.getName().lastIndexOf('.');
                if (i > 0) extension = f.getName().substring(i + 1).toLowerCase();
                if (extension.equals("png")) return true;
                else return false;
            }
            public String getDescription() {
                return "PNG Files (*.png)";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String extension = "";
                if (f.isDirectory()) return true;
                int i = f.getName().lastIndexOf('.');
                if (i > 0) extension = f.getName().substring(i + 1).toLowerCase();
                if (extension.equals("tif") || extension.equals("tiff")) return true;
                else return false;
            }
            public String getDescription() {
                return "TIFF Files (*.tif; *.tiff)";
            }
        });
        
        int chooserState = chooser.showSaveDialog(getFrame());
        if (chooserState == JFileChooser.APPROVE_OPTION) {
            final File imageFile = chooser.getSelectedFile();
            final BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(canvas.getWidth(), canvas.getHeight());
            
            //final BufferedImage image = (BufferedImage)this.createImage(canvas.getWidth(), canvas.getHeight());
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            canvas.controlPaint(g);
            
            if (chooser.getFileFilter().getDescription() == "Bitmap Files (*.bmp)") {
                try {
                    Thread thread = new Thread() {
                        public void run() {
                            RenderedOp op = JAI.create("filestore", image, imageFile.getPath(), "BMP");
                            Manager.message(getFrame(), "BMP image saved");
                        }
                    };
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                } catch (Exception e) {
                    Manager.message(getFrame(), e);
                }
            } else if (chooser.getFileFilter().getDescription() == "JPEG Files (*.jpg; *.jpeg)") {
                try {
                    Thread thread = new Thread() {
                        public void run() {
                            JPEGEncodeParam encoder = new JPEGEncodeParam();
                            encoder.setQuality(1.0f);
                            RenderedOp op = JAI.create("filestore", image, imageFile.getPath(), "JPEG", encoder);
                            Manager.message(getFrame(), "JPEG image saved");
                        }
                    };
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                } catch (Exception e) {
                    Manager.message(getFrame(), e);
                }
            } else if (chooser.getFileFilter().getDescription() == "PNG Files (*.png)") {
                try {
                    Thread thread = new Thread() {
                        public void run() {
                            RenderedOp op = JAI.create("filestore", image, imageFile.getPath(), "PNG");
                            Manager.message(getFrame(), "PNG image saved");
                        }
                    };
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                } catch (Exception e) {
                    Manager.message(getFrame(), e);
                }
            } else if (chooser.getFileFilter().getDescription() == "TIFF Files (*.tif; *.tiff)") {
                try {
                    Thread thread = new Thread() {
                        public void run() {
                            TIFFEncodeParam encoder = new TIFFEncodeParam();
                            encoder.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
                            RenderedOp op = JAI.create("filestore", image, imageFile.getPath(), "TIFF", encoder);
                            Manager.message(getFrame(), "TIFF image saved");
                        }
                    };
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                } catch (Exception e) {
                    Manager.message(getFrame(), e);
                }
            }
        }
    }
    
    public void printImage() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this, pj.defaultPage());
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException pe) {
                System.out.println(pe);
            }
        }
    }
    
    public int print(Graphics graphics, PageFormat format, int pageNumber) {
        if (pageNumber > 0) return Printable.NO_SUCH_PAGE;
        Graphics2D g = (Graphics2D) graphics;
        Component target = canvas;
        
        g.translate(format.getImageableX(), format.getImageableY());
        Dimension size = target.getSize();
        double pageWidth = format.getImageableWidth();
        double pageHeight = format.getImageableHeight();
        
        /*
        //These two 'if' blocks control scale-to-fit WITHOUT aspect ratio
        if (size.width > pageWidth)
            {
            double factor = pageWidth / size.width;
            g.scale(factor, 1);
            pageWidth /= factor;
            }
        if (size.height > pageHeight)
            {
            double factor = pageHeight / size.height;
            g.scale(1, factor);
            pageHeight /= factor;
            }
         */
        
        //These two 'if' blocks control scale-to-fit WITH aspect ratio
        if (size.width > pageWidth) {
            double factor = pageWidth / size.width;
            g.scale(factor, factor);
            pageWidth /= factor;
            pageHeight /= factor;
        }
        if (size.height > pageHeight) {
            double factor = pageHeight / size.height;
            g.scale(factor, factor);
            pageWidth /= factor;
            pageHeight /= factor;
        }
        
        g.translate((pageWidth - size.width) / 2, (pageHeight - size.height) / 2);
        g.drawRect(-1, -1, size.width + 2, size.height + 2);
        g.setClip(0, 0, size.width, size.height);
        
        if (target instanceof JComponent) ((JComponent) target).setDoubleBuffered(false);
        target.paintAll(g);
        if (target instanceof JComponent) ((JComponent) target).setDoubleBuffered(true);
        
        return Printable.PAGE_EXISTS;
    }
    
    public void systemDisable(int state) {
        slideData = null;
        
        switch (state) {
            case TMEV.SYSTEM:
                fileMenu.setEnabled(true);
                newDatabaseItem.setEnabled(false);
                newFileItem.setEnabled(false);
                newReportItem.setEnabled(false);
                newSpotfireItem.setEnabled(false);
                saveImageItem.setEnabled(false);
                printImageItem.setEnabled(false);
                break;
            case TMEV.DATA_AVAILABLE:
                fileMenu.setEnabled(true);
                newReportItem.setEnabled(false);
                newSpotfireItem.setEnabled(false);
                newDatabaseItem.setEnabled(false); //disable until supported, 4.30.03
                break;
            case TMEV.DB_AVAILABLE:
                fileMenu.setEnabled(true);
                newDatabaseItem.setEnabled(false); //disable until supported, 4.30.03
                break;
            case TMEV.DB_LOGIN:
                fileMenu.setEnabled(true);
                newDatabaseItem.setEnabled(false); //disable until supported, 4.30.03
                break;
        }
        viewsMenu.setEnabled(false);
        normalizationMenu.setEnabled(false);
        sortMenu.setEnabled(false);
        displayMenu.setEnabled(false);
        controlMenu.setEnabled(false);
        
        panel.systemDisable(state);
    }
    
    public void systemEnable(int state) {
        switch (state) {
            case TMEV.SYSTEM:
                fileMenu.setEnabled(true);
                break;
            case TMEV.DATA_AVAILABLE:
                fileMenu.setEnabled(true);
                newFileItem.setEnabled(true);
                newReportItem.setEnabled(true);
                newSpotfireItem.setEnabled(true);
                saveImageItem.setEnabled(true);
                printImageItem.setEnabled(true);
                viewsMenu.setEnabled(true);
                normalizationMenu.setEnabled(true);
                sortMenu.setEnabled(true);
                displayMenu.setEnabled(true);
                controlMenu.setEnabled(true);
                break;
            case TMEV.DB_AVAILABLE:
                fileMenu.setEnabled(true);
                break;
            case TMEV.DB_LOGIN:
                //System.out.println("Enable DB_LOGIN"); //disable until supported, 4.30.03
                fileMenu.setEnabled(true);
                //newDatabaseItem.setEnabled(true);
                newDatabaseItem.setEnabled(false);  //disable until supported, 4.30.03
                break;
        }
        
        panel.systemEnable(state);
    }
    
    public void handleItems(Object target) {
        if (target == newDatabaseItem) {
            noNormalizationItem.setSelected(true);
        } else if (target == newFileItem) {
            noNormalizationItem.setSelected(true);
        } else if (target == totalIntensityItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == linearRegressionItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == ratioStatisticsItem) {
            setConfidenceItem.setEnabled(true);
        } else if (target == iterativeLogItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == totalIntensityListItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == linearRegressionListItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == ratioStatisticsListItem) {
            setConfidenceItem.setEnabled(true);
        } else if (target == noNormalizationItem) {
            setConfidenceItem.setEnabled(false);
        } else if (target == logItem) {
            panel.linearItem.setSelected(logItem.isSelected());
        } else if (target == setThresholdsItem) {
            panel.thresholdCheckbox.setSelected(setThresholdsItem.isSelected());
        } else if (target == setScaleItem) {
            panel.greenRedScaleCheckbox.setSelected(setScaleItem.isSelected());
        } else if (target == changeColorsItem) {
            if (greenRedItem.getText().equals("Green / Red")) {
                greenRedItem.setText("Blue / Red");
                overlayItem.setText("B/R Overlay");
                setScaleItem.setText("Use B/R Scale");
                panel.greenRedCheckbox.setText("B/R Bar Display");
                panel.overlayCheckbox.setText("B/R Overlay");
                panel.greenRedScaleCheckbox.setText("B/R Scale");
            } else if (greenRedItem.getText().equals("Blue / Red")) {
                greenRedItem.setText("Green / Red");
                overlayItem.setText("G/R Overlay");
                setScaleItem.setText("Use G/R Scale");
                panel.greenRedCheckbox.setText("G/R Bar Display");
                panel.overlayCheckbox.setText("G/R Overlay");
                panel.greenRedScaleCheckbox.setText("G/R Scale");
            }
        } else if (target == blueToRedItem) {
            if (!panel.blueRedCheckbox.isSelected()) {
                panel.blueRedCheckbox.setSelected(true);
            }
        } else if (target == panel.blueRedCheckbox) {
            if (panel.blueRedCheckbox.isSelected() && !blueToRedItem.isSelected()) {
                blueToRedItem.setSelected(true);
            }
        } else if (target == greenRedItem) {
            if (!panel.greenRedCheckbox.isSelected()) {
                panel.greenRedCheckbox.setSelected(true);
            }
        } else if (target == panel.greenRedCheckbox) {
            if (panel.greenRedCheckbox.isSelected() && !greenRedItem.isSelected()) {
                greenRedItem.setSelected(true);
            }
            panel.greenRedScaleCheckbox.setSelected(false);
            setScaleItem.setSelected(false);
        } else if (target == overlayItem) {
            if (!panel.overlayCheckbox.isSelected()) {
                panel.overlayCheckbox.setSelected(true);
            }
        } else if (target == panel.overlayCheckbox) {
            if (panel.overlayCheckbox.isSelected() && !overlayItem.isSelected()) {
                overlayItem.setSelected(true);
            }
        }
    }
    
    public void close() {
        mainframe.dispose();
        Manager.removeComponent(this);
    }
    
    private class EventListener implements ActionListener, AdjustmentListener, ChangeListener, ItemListener, KeyListener, MouseListener, MouseMotionListener {
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            handleItems(source);
            
            if (source == newDatabaseItem) {
                SetDatabaseDialog sdd = new SetDatabaseDialog(getFrame());
                if (sdd.showModal() == JOptionPane.OK_OPTION) {
                    loadDataFromDatabase();
                }
            } else if (source == newFileItem) {
                loadFile();
                //setSlideData(slideData);
                refreshSlide();
            } else if (source == newReportItem) {
                setReportFilename();
            } else if (source == saveImageItem) {
                saveImage();
            } else if (source == printImageItem) {
                printImage();
            } else if (source == newSpotfireItem) {
                // PC ONLY
                //displayApplet.exportSpotfire(displayApplet.getData());
                //
            } else if (source == closeItem) {
                close();
            } else if (source == newPlotItem) {
                createScatterPlot(SingleArrayViewer.LINEAR);
            } else if (source == newPlotLogItem) {
                createScatterPlot(SingleArrayViewer.LOG);
            } else if (source == newLogRatioProductItem) {
                createRatioProductPlot();
            } else if (source == newLogRatioProductByBlockItem) {
                createRatioProductPlotByBlock();
            } else if (source == newHistogramItem) {
                createHistogram(SingleArrayViewer.LINEAR);
            } else if (source == newHistogramLogItem) {
                createHistogram(SingleArrayViewer.LOG);
            } else if (source == subArrayItem) {
                createSubArray();
            } else if (source == regionArrayItem) {
                SetMetaLocationDialog smld = new SetMetaLocationDialog(getFrame());
                smld.addActionInfoListener(new ActionInfoListener() {
                    public void actionInfoPerformed(ActionInfoEvent event) {
                        Hashtable hash = event.getHashtable();
                        //Set row, column ((String) hash.get("row")), ((String) hash.get("column"));
                        createRegion(Integer.parseInt((String) hash.get("row")), Integer.parseInt((String) hash.get("column")));
                    }
                });
                smld.show();
            } else if (source == totalIntensityItem) {
                normalizeData(SlideData.TOTAL_INTENSITY);
            } else if (source == leastSquaresItem) {
                normalizeData(SlideData.LEAST_SQUARES);
            } else if (source == linearRegressionItem) {
                normalizeData(SlideData.LINEAR_REGRESSION);
            } else if (source == ratioStatisticsItem) {
                normalizeData(SlideData.RATIO_STATISTICS_95);
            } else if (source == iterativeLogItem) {
                normalizeData(SlideData.ITERATIVE_LOG);
            } else if (source == totalIntensityListItem) {
                normalizeDataList(SlideData.TOTAL_INTENSITY_LIST);
            } else if (source == leastSquaresListItem) {
                normalizeDataList(SlideData.LEAST_SQUARES_LIST);
            } else if (source == linearRegressionListItem) {
                normalizeDataList(SlideData.LINEAR_REGRESSION_LIST);
            } else if (source == ratioStatisticsListItem) {
                normalizeDataList(SlideData.RATIO_STATISTICS_95_LIST);
            } else if (source == iterativeLogListItem) {
                normalizeDataList(SlideData.ITERATIVE_LOG_LIST);
            } else if (source == noNormalizationItem) {
                normalizeData(SlideData.NO_NORMALIZATION);
            } else if (source == sortByLocationItem) {
                sort(SlideDataSorter.SORT_BY_LOCATION);
            } else if (source == sortByRatioItem) {
                sort(SlideDataSorter.SORT_BY_RATIO);
            } else if (source == logItem) {
                if (logItem.isSelected()) canvas.setStyle(SingleArrayViewerCanvas.LOG);
                else canvas.setStyle(SingleArrayViewerCanvas.LINEAR);
            } else if (source == changeColorsItem) {
                canvas.changeColorScheme();
            } else if (source == blueToRedItem) {
            } else if (source == greenRedItem) {
            } else if (source == overlayItem) {
            } else if (source == defaultSize1Item) {
                canvas.setElementSize(5, 2);
                refreshSlide();
            } else if (source == defaultSize2Item) {
                canvas.setElementSize(10, 10);
                refreshSlide();
            } else if (source == defaultSize3Item) {
                canvas.setElementSize(20, 5);
                refreshSlide();
            } else if (source == defaultSize4Item) {
                canvas.setElementSize(50, 10);
                refreshSlide();
            } else if (source == setElementSizeItem) {
                /*
                SetElementSizeDialog sesd = new SetElementSizeDialog(getFrame(), new Dimension(canvas.getXElementSize(), canvas.getYElementSize()));
                sesd.addActionInfoListener(new ActionInfoListener() {
                    public void actionInfoPerformed(ActionInfoEvent event) {
                        Hashtable hash = event.getHashtable();
                        int width  = canvas.getXElementSize();
                        int height = canvas.getYElementSize();
                        try {
                            width = Integer.parseInt((String)hash.get("width"));
                        }
                        catch (Exception e) {;}
                        try {
                            height = Integer.parseInt((String) hash.get("height"));
                        }
                        catch (Exception e) {;}
                        canvas.setElementSize(width, height);
                        refreshSlide();
                    }
                });
                sesd.show();*/
            } else if (source == setUpperLimitsItem) {
                SetUpperLimitsDialog suld = new SetUpperLimitsDialog(getFrame(), panel.xLastValue, panel.yLastValue);
                if (suld.showModal() == JOptionPane.OK_OPTION) {
                    setUpperLimits((long)suld.getUpperCY3(), (long)suld.getUpperCY5());
                }
            } else if (source == setGreenRedThresholdsItem) {
                SetGreenRedThresholdDialog sgrtd = new SetGreenRedThresholdDialog(getFrame(), new Double(canvas.getFactor()));
                sgrtd.addActionInfoListener(new ActionInfoListener() {
                    public void actionInfoPerformed(ActionInfoEvent event) {
                        Hashtable hash = event.getHashtable();
                        setFactor(Float.parseFloat((String) hash.get("factor")));
                        //Set factor ((String) hash.get("factor"))
                    }
                });
                sgrtd.show();
            } else if (source == setConfidenceItem) {
                SetConfidenceDialog scd = new SetConfidenceDialog(getFrame());
                scd.addActionInfoListener(new ActionInfoListener() {
                    public void actionInfoPerformed(ActionInfoEvent event) {
                        Hashtable hash = event.getHashtable();
                        //Set confidence ((String) hash.get("confidence"))
                    }
                });
                scd.show();
            } else if (source == setThresholdsItem) {
                if (setThresholdsItem.isSelected()) setThresholds(true);
                else setThresholds(false);
            } else if (source == setScaleItem) {
                if (setScaleItem.isSelected()) canvas.setScale(true);
                else canvas.setScale(false);
            }
            //From the components contained within the panel
            else if (source == panel.linearItem) {
                if (panel.linearItem.isSelected()) {
                    logItem.setSelected(true);
                    canvas.setStyle(SingleArrayViewerCanvas.LOG);
                } else {
                    logItem.setSelected(false);
                    canvas.setStyle(SingleArrayViewerCanvas.LINEAR);
                }
            } else if (source == panel.thresholdCheckbox) {
                if (panel.thresholdCheckbox.isSelected()) {
                    setGreenRedThresholdsItem.setSelected(true);
                    //canvas.setStyle(SingleArrayViewerCanvas.LOG);
                } else {
                    setGreenRedThresholdsItem.setSelected(false);
                    //canvas.setStyle(SingleArrayViewerCanvas.LINEAR);
                }
            } else if (source == panel.greenRedScaleCheckbox) {
                if (panel.greenRedScaleCheckbox.isSelected()) {
                    setScaleItem.setSelected(true);
                    canvas.setScale(true);
                } else {
                    setScaleItem.setSelected(false);
                    canvas.setScale(false);
                }
            } else if (source == panel.blueRedCheckbox) {
            } else if (source == panel.greenRedCheckbox) {
            } else if (source == panel.overlayCheckbox) {
            }
            
            //From the keys defined with the JScrollPane
            else if (event.getActionCommand() == "lineup") {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                vBar.setValue(vBar.getValue() - canvas.getYElementSize());
            } else if (event.getActionCommand() == "linedown") {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                vBar.setValue(vBar.getValue() + canvas.getYElementSize());
            } else if (event.getActionCommand() == "pageup") {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                vBar.setValue(vBar.getValue() - scrollPane.getViewport().getHeight());
            } else if (event.getActionCommand() == "pagedown") {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                vBar.setValue(vBar.getValue() + scrollPane.getViewport().getHeight());
            } else {
                for (int i = 0; i < sortMenuItems.length; i++) {
                    if (event.getSource() == sortMenuItems[i]) sort(i);
                }
            }
        }
        
        public void stateChanged(ChangeEvent event) {
            Object source = event.getSource();
            
            if (source == panel.xUpperLimitScrollbar) {
                setCursor(Cursor.WAIT_CURSOR);
                panel.xLastValue = ((JSlider) source).getValue();//event.getValue();
                panel.xUpperLimitValueLabel.setText(panel.xLastValue + " / " + panel.xUpperLimitScrollbar.getMaximum());
                canvas.setTopCy3(panel.xLastValue);
                //panel.updateXUpperLimitScrollbar(panel.xLastValue);
                refreshSlide();
                setCursor(Cursor.DEFAULT_CURSOR);
            } else if (source == panel.yUpperLimitScrollbar) {
                setCursor(Cursor.WAIT_CURSOR);
                panel.yLastValue = ((JSlider) source).getValue();//event.getValue();
                panel.yUpperLimitValueLabel.setText(panel.yLastValue + " / " + panel.yUpperLimitScrollbar.getMaximum());
                canvas.setTopCy5(panel.yLastValue);
                //panel.updateYUpperLimitScrollbar(panel.yLastValue);
                refreshSlide();
                setCursor(Cursor.DEFAULT_CURSOR);
            } else if (source == panel.thresholdScrollbar) {
                setCursor(Cursor.WAIT_CURSOR);
                panel.tLastValue = ((JSlider) source).getValue();//event.getValue();
                setFactor((float) panel.tLastValue / 1000);
                panel.updateThresholdScrollbar((double) panel.tLastValue / 1000);
                if (panel.tLastValue > 0) panel.thresholdValueLabel.setText((double) panel.tLastValue / 1000 + "");
                if (panel.thresholdCheckbox.isSelected()) refreshSlide();
                setCursor(Cursor.DEFAULT_CURSOR);
            }
        }
        
        public void adjustmentValueChanged(AdjustmentEvent event) {
        }
        
        public void itemStateChanged(ItemEvent event) {
            Object source = event.getSource();
            if (source == panel.blueRedCheckbox && event.getStateChange() == ItemEvent.SELECTED) {
                changePaletteStyle(SingleArrayViewerCanvas.BLUETORED);
                handleItems(source);
            } else if (source == panel.greenRedCheckbox && event.getStateChange() == ItemEvent.SELECTED) {
                changePaletteStyle(SingleArrayViewerCanvas.GREENRED);
                handleItems(source);
            } else if (source == panel.overlayCheckbox && event.getStateChange() == ItemEvent.SELECTED) {
                changePaletteStyle(SingleArrayViewerCanvas.OVERLAY);
                handleItems(source);
            } else if (source == panel.thresholdCheckbox) {
                if (canvas.getThresholds() == false) {
                    setThresholdsItem.setSelected(true);
                    setThresholds(true);
                } else if (canvas.getThresholds() == true) {
                    setThresholdsItem.setSelected(false);
                    setThresholds(false);
                }
            } else if (source == panel.greenRedScaleCheckbox) {
                if (canvas.getScale() == false) {
                    setScaleItem.setSelected(true);
                    canvas.setScale(true);
                } else if (canvas.getScale() == true) {
                    setScaleItem.setSelected(false);
                    canvas.setScale(false);
                }
            }
        }
        
        public void mouseClicked(MouseEvent event) {
            if (canvas.inPosition(event.getX(), event.getY())) {
                if (! event.isShiftDown()) {
                    try {
                        ISlideDataElement element = canvas.getSlideDataElementAt(event.getX(), event.getY());
                        if (element == null) {
                            return;
                        }
                        int row = canvas.findRow(event.getX(), event.getY());
                        int col = canvas.findColumn(event.getX(), event.getY());
                        final int columns = slideData.getSlideMetaData().getColumns();
                        int index = (row-1)*columns +col -1;
                     //   System.out.println("row, col, columns index "+ row +" "+col+" "+columns+  "   "+((columns*(row-1))+(col-1)));
                        Manager.displaySlideElementInfo(getFrame(), slideData, element, ((columns*(row-1))+(col-1))); 
                        //canvas.findRow(event.getX(), event.getY())-1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ;
                    }
                }
            }
        }
        
        public void mouseMoved(MouseEvent event) {
            if (slideData == null || event.isShiftDown())
                return;
            if ((slideData.getSize() > 0)) {
                int x = event.getX();
                int y = event.getY();
                if (canvas.inPosition(x, y)) {
                    if (canvas.getHighlight() == true) {
                        canvas.drawColoredBoxAt((Graphics2D)canvas.getGraphics(), x, y, Color.white);
                    }
                    setXOldEvent(x);
                    setYOldEvent(y);
                }
            }
        }
        
        public void mouseEntered(MouseEvent event) {;}
        public void mouseExited(MouseEvent event) {;}
        public void mousePressed(MouseEvent event) {;}
        public void mouseReleased(MouseEvent event) {;}
        public void mouseDragged(MouseEvent event) {;}
        public void keyPressed(KeyEvent event) {;}
        public void keyReleased(KeyEvent event) {;}
        public void keyTyped(KeyEvent event) {;}
    }
    
    public class SingleArrayViewerPanel extends JPanel {
        GridBagLayout displayPanelLayout;
        ButtonGroup displayCheckboxGroup;
        JRadioButton blueRedCheckbox;
        JRadioButton greenRedCheckbox;
        JRadioButton overlayCheckbox;
        JCheckBox thresholdCheckbox;
        JCheckBox greenRedScaleCheckbox;
        JCheckBox linearItem;
        JLabel xUpperLimitLabel;
        JLabel yUpperLimitLabel;
        JLabel xUpperLimitValueLabel;
        JLabel yUpperLimitValueLabel;
        JLabel thresholdValueLabel;
        JSlider xUpperLimitScrollbar;
        JSlider yUpperLimitScrollbar;
        JSlider thresholdScrollbar;
        
        long xLastValue = 0, yLastValue = 0, tLastValue = 0;
        
        public SingleArrayViewerPanel() {
            GridBagConstraints gbc;
            
            displayPanelLayout = new GridBagLayout();
            buttonGroup = new ButtonGroup();
            
            Font valueLabelFont = new Font("monospaced", Font.PLAIN, 9);
            
            blueRedCheckbox = new JRadioButton("Blue -> Red",false);
            blueRedCheckbox.addItemListener(eventListener);
            gbc = createConstraints(1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(blueRedCheckbox, gbc);
            buttonGroup.add(blueRedCheckbox);
            
            greenRedCheckbox = new JRadioButton("G/R Bar Display", false);
            greenRedCheckbox.addItemListener(eventListener);
            gbc = createConstraints(1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(greenRedCheckbox, gbc);
            buttonGroup.add(greenRedCheckbox);
            
            overlayCheckbox = new JRadioButton("G/R Overlay", true);
            overlayCheckbox.addItemListener(eventListener);
            gbc = createConstraints(1, 3, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(overlayCheckbox, gbc);
            buttonGroup.add(overlayCheckbox);
            
            greenRedScaleCheckbox = new JCheckBox("G/R Scale");
            greenRedScaleCheckbox.setSelected(true);
            greenRedScaleCheckbox.addItemListener(eventListener);
            gbc = createConstraints(1, 4, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(greenRedScaleCheckbox, gbc);
            
            linearItem = new JCheckBox("Log Scale");
            linearItem.addActionListener(eventListener);
            gbc = createConstraints(1, 5, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(linearItem, gbc);
            
            xUpperLimitLabel = new JLabel("Cy3 Upper Limit");
            gbc = createConstraints(1, 6, new Insets(10, 5, 0, 0), GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(xUpperLimitLabel, gbc);
            
            xUpperLimitScrollbar = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 0);
            xUpperLimitScrollbar.addChangeListener(eventListener);
            gbc = createConstraints(1, 7, new Insets(10, 5, 0, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(xUpperLimitScrollbar, gbc);
            
            xUpperLimitValueLabel = new JLabel("0/0", Label.RIGHT);
            xUpperLimitValueLabel.setFont(valueLabelFont);
            gbc = createConstraints(1, 8, new Insets(10, 5, 10, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(xUpperLimitValueLabel, gbc);
            
            yUpperLimitLabel = new JLabel("Cy5 Upper Limit");
            gbc = createConstraints(1, 9, new Insets(10, 5, 0, 0), GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(yUpperLimitLabel, gbc);
            
            yUpperLimitScrollbar = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 0);
            yUpperLimitScrollbar.addChangeListener(eventListener);
            gbc = createConstraints(1, 10, new Insets(10, 5, 0, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(yUpperLimitScrollbar, gbc);
            
            yUpperLimitValueLabel = new JLabel("0/0", Label.RIGHT);
            yUpperLimitValueLabel.setFont(valueLabelFont);
            gbc = createConstraints(1, 11, new Insets(10, 5, 10, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(yUpperLimitValueLabel, gbc);
            
            thresholdCheckbox = new JCheckBox("Expression Ratio");
            thresholdCheckbox.addItemListener(eventListener);
            gbc = createConstraints(1, 12, GridBagConstraints.WEST, GridBagConstraints.NONE);
            displayPanelLayout.setConstraints(thresholdCheckbox, gbc);
            
            thresholdScrollbar = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 2000);
            thresholdScrollbar.addChangeListener(eventListener);
            gbc = createConstraints(1, 13, new Insets(10, 5, 0, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(thresholdScrollbar, gbc);
            
            thresholdValueLabel = new JLabel("2.0", Label.RIGHT);
            thresholdValueLabel.setFont(valueLabelFont);
            gbc = createConstraints(1, 14, new Insets(10, 5, 10, 0), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
            displayPanelLayout.setConstraints(thresholdValueLabel, gbc);
            
            setLayout(displayPanelLayout);
            add(blueRedCheckbox);
            add(greenRedCheckbox);
            add(overlayCheckbox);
            add(thresholdCheckbox);
            add(greenRedScaleCheckbox);
            add(linearItem);
            add(xUpperLimitLabel);
            add(yUpperLimitLabel);
            add(xUpperLimitValueLabel);
            add(yUpperLimitValueLabel);
            add(thresholdValueLabel);
            add(xUpperLimitScrollbar);
            add(yUpperLimitScrollbar);
            add(thresholdScrollbar);
        }
        
        public void setXYScrollbars(long xMax, long yMax) {
            if (xMax > yMax) {
                //xUpperLimitScrollbar.setValues((int) xMax, 1000, 0, (int) xMax);
                //yUpperLimitScrollbar.setValues((int) yMax, 1000, 0, (int) xMax);
                //xUpperLimitValueLabel.setText(xMax + " / " + xMax);
                //yUpperLimitValueLabel.setText(yMax + " / " + xMax);
                xUpperLimitScrollbar.setMinimum(0);
                xUpperLimitScrollbar.setMaximum((int) xMax);
                xUpperLimitScrollbar.setValue((int) xMax);
                yUpperLimitScrollbar.setMinimum(0);
                yUpperLimitScrollbar.setMaximum((int) xMax);
                yUpperLimitScrollbar.setValue((int) yMax);
                xUpperLimitValueLabel.setText(xMax + " / " + xMax);
                yUpperLimitValueLabel.setText(yMax + " / " + xMax);
            } else {
                xUpperLimitScrollbar.setMinimum(0);
                xUpperLimitScrollbar.setMaximum((int) yMax);
                xUpperLimitScrollbar.setValue((int) xMax);
                yUpperLimitScrollbar.setMinimum(0);
                yUpperLimitScrollbar.setMaximum((int) yMax);
                yUpperLimitScrollbar.setValue((int) yMax);
                xUpperLimitValueLabel.setText(xMax + " / " + yMax);
                yUpperLimitValueLabel.setText(yMax + " / " + yMax);
            }
            xLastValue = xMax;
            yLastValue = yMax;
        }
        
        public void setThresholdScrollbar(double ratio) {
            thresholdScrollbar.setMinimum(0);
            thresholdScrollbar.setMaximum(10000);
            thresholdScrollbar.setValue((int) (1000 * ratio));
            if (ratio >= 0) thresholdValueLabel.setText(ratio + "");
            else thresholdValueLabel.setText("Off");
            tLastValue = (long) (1000 * ratio);
        }
        
        public void updateXUpperLimitScrollbar(long xMax) {
            xUpperLimitScrollbar.setValue((int) xMax);
            xUpperLimitValueLabel.setText(xMax + " / " + xUpperLimitScrollbar.getMaximum());
            xLastValue = xMax;
        }
        
        public void updateYUpperLimitScrollbar(long yMax) {
            yUpperLimitScrollbar.setValue((int) yMax);
            yUpperLimitValueLabel.setText(yMax + " / " + yUpperLimitScrollbar.getMaximum());
            yLastValue = yMax;
        }
        
        public void updateThresholdScrollbar(double ratio) {
            thresholdScrollbar.setValue((int) (1000 * ratio));
            if (ratio >= 0) thresholdValueLabel.setText(ratio + "");
            else thresholdValueLabel.setText("Off");
            tLastValue = (long) (1000 * ratio);
        }
        
        public GridBagConstraints createConstraints(int gridx, int gridy, int anchor, int fill) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = gridx;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.anchor = anchor;
            gridBagConstraints.fill = fill;
            return gridBagConstraints;
        }
        
        public GridBagConstraints createConstraints(int gridx, int gridy, Insets insets, int anchor, int fill) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = gridx;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.insets = insets;
            gridBagConstraints.anchor = anchor;
            gridBagConstraints.fill = fill;
            return gridBagConstraints;
        }
        
        public GridBagConstraints createConstraints(int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = gridx;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.anchor = anchor;
            gridBagConstraints.fill = fill;
            gridBagConstraints.gridwidth = gridwidth;
            gridBagConstraints.gridheight = gridheight;
            return gridBagConstraints;
        }
        
        public void systemDisable(int state) {
            switch (state) {
                case TMEV.DATA_AVAILABLE:
                    blueRedCheckbox.setEnabled(false);
                    greenRedCheckbox.setEnabled(false);
                    overlayCheckbox.setEnabled(false);
                    thresholdCheckbox.setEnabled(false);
                    greenRedScaleCheckbox.setEnabled(false);
                    linearItem.setEnabled(false);
                    xUpperLimitScrollbar.setEnabled(false);
                    yUpperLimitScrollbar.setEnabled(false);
                    thresholdScrollbar.setEnabled(false);
                    break;
            }
        }
        
        public void systemEnable(int state) {
            switch (state) {
                case TMEV.DATA_AVAILABLE:
                    blueRedCheckbox.setEnabled(true);
                    greenRedCheckbox.setEnabled(true);
                    overlayCheckbox.setEnabled(true);
                    thresholdCheckbox.setEnabled(true);
                    greenRedScaleCheckbox.setEnabled(true);
                    linearItem.setEnabled(true);
                    xUpperLimitScrollbar.setEnabled(true);
                    yUpperLimitScrollbar.setEnabled(true);
                    thresholdScrollbar.setEnabled(true);
                    break;
            }
        }
    }
    
    private class SingleArrayViewerInfoBox extends JPanel {
        private GBA gba;
        private JLabel slideLabel;
        private JLabel normalizationLabel;
        
        public SingleArrayViewerInfoBox() {
            gba = new GBA();
            
            setBackground(Color.white);
            
            slideLabel = new JLabel("Sample Text");
            // gba.add(this, slideLabel, 0, 0, 1, 1, 1, 1, GBA.N, GBA.B);
            
            normalizationLabel = new JLabel("Normalization Text");
            // gba.add(this, normalizationLabel, 0, 1, 1, 1, 1, 1, GBA.S, GBA.B);
        }
        
        public JLabel getSlideLabel() {return this.slideLabel;}
        public JLabel getNormalizationLabel() {return this.normalizationLabel;}
    }
    
    private class SingleArrayViewerCanvas extends ArrayViewerCanvas {
        //For use with style
        public final static int LINEAR = 0;
        public final static int LOG = 1;
        //For use with paletteStyle
        public final static int BLUETORED = 1;
        public final static int GREENRED = 2;
        public final static int OVERLAY = 3;
        //For use with colorScheme
        public final static int RED_GREEN_COLOR_SCHEME = 0;
        public final static int RED_BLUE_COLOR_SCHEME = 1;
        
        //private int xsize; //The horizontal size of each element
        //private int ysize; //The vertical size of each element
        private int maxRow; //?
        private int maxColumn; //?
        private Vector palette; //Stores the colors for the B->R scale
        
        private int style; //Sets log or linear -- maybe remove this?
        private int paletteStyle; //Sets display type (B->R, Bar, Over)
        private int colorScheme; //Sets GR and GB
        
        private double lowDifference; //Low bound for expression ratio -- maybe remove this?
        private double highDifference; //High bound for expression ratio -- maybe remove this?
        private float factor; //X factor for expression ratio
        
        private boolean scaleOn; //Toggles G/R scaling
        private boolean thresholdsOn; //Toggles Expression Ratio
        private boolean highlightOn; //Toggles white-box outlining
        private int startx, stopx, starty, stopy, xspacing, yspacing, xgap, ygap;
        private Font tinyFont, smallFont, largeFont;
        private float topCy3 = 0, topCy5 = 0;
        private int preXSpacing, postXSpacing, preYSpacing, postYSpacing;
        private int xElementSize, yElementSize;
        private int paletteSpacing = 0;
        
        public SingleArrayViewerCanvas(int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing) {
            super(0, preXSpacing + postXSpacing, 0, preYSpacing + postYSpacing);
            style = SingleArrayViewerCanvas.LINEAR;
            paletteStyle = SingleArrayViewerCanvas.OVERLAY;
            tinyFont = new Font("monospaced", Font.PLAIN, 8);
            smallFont = new Font("serif", Font.PLAIN, 12);
            largeFont = new Font("serif", Font.PLAIN, 16);
            colorScheme = SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME;
            
            this.preXSpacing = preXSpacing;
            this.postXSpacing = postXSpacing;
            this.preYSpacing = preYSpacing;
            this.postYSpacing = postYSpacing;
            
            scaleOn = true;
            thresholdsOn = false;
            highlightOn = true;
            this.lowDifference = .33;
            this.highDifference = .66;
            factor = (float) 2.0;
            paletteSpacing = 50;
            
            setElementSize(20, 5);
            
            palette = buildPalette();
            
            setXOldEvent(-1);
            setYOldEvent(-1);
        }
        
        public void controlPaint(Graphics g1D) {
            Graphics2D g = (Graphics2D) g1D;
            if (slideData != null)
                drawSlide(g);
        }
        
        public void setSpacing(int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing) {
            this.preXSpacing = preXSpacing;
            this.postXSpacing = postXSpacing;
            this.preYSpacing = preYSpacing;
            this.postYSpacing = postYSpacing;
            updateSizes();
        }
        
        public void setSize() {
            if (slideData != null) {
                int width  = preXSpacing + getXSize() + postXSpacing;
                int height = preYSpacing + getYSize() + postYSpacing;
                setPreferredSize(new Dimension(width, height));
                scrollPane.setViewportView(canvas);
            }
        }
        
        public void setX2Size() {
            if (slideData != null) {
                int width  = getX2Start() + getXSize() + postXSpacing;
                int height = preYSpacing + getYSize() + postYSpacing;
                setPreferredSize(new Dimension(width, height));
                scrollPane.setViewportView(canvas);
            }
        }
        
        public void setElementSize(int xElementSize, int yElementSize) {
            this.xElementSize = xElementSize;
            this.yElementSize = yElementSize;
            updateSizes();
        }
        
        public void updateSizes() {
            if (paletteStyle == SingleArrayViewerCanvas.BLUETORED) {
                setX2Size();
            } else {
                setSize();
            }
            //setPreferredSize(new Dimension(preXSpacing + getXSize() + postXSpacing, preYSpacing + getYSize() + postYSpacing));
        }
        
        //public void setXElementSize(int xElementSize) {this.xElementSize = xElementSize;}
        //public void setYElementSize(int yElementSize) {this.yElementSize = yElementSize;}
        public int getXElementSize() {return this.xElementSize;}
        public int getYElementSize() {return this.yElementSize;}
        
        public int getXSize() {
            int columnCount = 0;
            if (slideData != null) columnCount = slideData.getSlideMetaData().getColumns();
            if (columnCount > 0) return columnCount * xElementSize;
            else return 0;
        }
        
        public int getYSize() {
            int rowCount = 0;
            if (slideData != null) rowCount = slideData.getSlideMetaData().getRows();
            if (rowCount > 0) return rowCount * yElementSize;
            else return 0;
        }
        
        public void setScale(boolean setting) {
            scaleOn = setting;
            refreshSlide();
        }
        public boolean getScale() {return this.scaleOn;}
        public void setColorScheme(int colorScheme) {this.colorScheme = colorScheme;}
        public int getColorScheme() {return this.colorScheme;}
        public void changeColorScheme() {
            if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) {
                colorScheme = SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME;
            } else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) {
                colorScheme = SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME;
            }
            refreshSlide();
        }
        
        public void setThresholds(boolean setting) {
            thresholdsOn = setting;
            refreshSlide();
        }
        public boolean getThresholds() {return this.thresholdsOn;}
        public boolean getHighlight() {return this.highlightOn;}
        public void setHighlight(boolean setting) {this.highlightOn = setting;}
        public float getFactor() {return this.factor;}
        public void setFactor(float factor) {
            this.factor = factor;
        }
        public double getLowDifference() {return this.lowDifference;}
        public void setLowDifference(double difference) {this.lowDifference = difference;}
        public double getHighDifference() {return this.highDifference;}
        public void setHighDifference(double difference) {this.highDifference = difference;}
        public void setTopCy3(float cy3) {this.topCy3 = cy3;}
        public float getTopCy3() {return this.topCy3;}
        public void setTopCy5(float cy5) {this.topCy5 = cy5;}
        public float getTopCy5() {return this.topCy5;}
        public void setUpperLimits(float cy3, float cy5) {
            setTopCy3(cy3);
            setTopCy5(cy5);
        }
        
        /*
        public void setMaxColumn(int maxColumn)
            {
            this.maxColumn = maxColumn;
            this.xsize = this.xgap / this.maxColumn;
            }
         
        public void setMaxRow(int maxRow)
            {
            this.maxRow = maxRow;
            this.ysize = this.ygap / this.maxRow;
            }
         */
        
        public int getPaletteSpacing() {return this.paletteSpacing;}
        public void setPaletteSpacing(int paletteSpacing) {this.paletteSpacing = paletteSpacing;}
        
        public void setStyle(int style) {
            this.style = style;
            refreshSlide();
        }
        
        public int getStyle() {return this.style;}
        
        public void setPaletteStyle(int style) {
            setCursor(Cursor.WAIT_CURSOR);
            this.paletteStyle = style;
            if (this.paletteStyle == SingleArrayViewerCanvas.BLUETORED) {
                //eraseSlideDataElements2();
                //drawSlide2();
                //getFrame().pack();
                //refreshSlide();
            } else if (this.paletteStyle == SingleArrayViewerCanvas.GREENRED) {
                //eraseSlideDataElements2();
                //setSize();
                //getFrame().pack();
                setScale(false);
                //refreshSlide();
            } else if (this.paletteStyle == SingleArrayViewerCanvas.OVERLAY) {
                //eraseSlideDataElements2();
                //setSize();
                //getFrame().pack();
                //refreshSlide();
            }
            updateSizes();
            refreshSlide();
            setCursor(Cursor.DEFAULT_CURSOR);
        }
        
        public int getPaletteStyle() {return this.paletteStyle;}
        
        public void slideDataElementAt(int targetx, int targety) {
            ISlideDataElement slideDataElement = null;
            int column, row;
            
            column = findColumn(targetx, targety);
            row = findRow(targetx, targety);
            final int columns = slideData.getSlideMetaData().getColumns();
            if (column > 0 && row > 0) {
                slideDataElement = (ISlideDataElement)slideData.getSlideDataElement(indices[(row-1)*columns +column -1]);
            }
            //System.out.println(column + ", " + row);
            //if (slideDataElement != null) displaySlideElementInfo(slideDataElement);
            if (slideDataElement != null) {
                //System.out.println(slideDataElement.getRatio(SlideDataElement.CY5, ISlideDataElement.CY3, ISlideDataElement.LINEAR));
                
                int trueRow, trueColumn;
                float cy3, cy5;
                String[] fieldNames;
                String displayString;
                int stringLength = 0;
                
                trueRow = slideDataElement.getRow(ISlideDataElement.BASE);
                trueColumn = slideDataElement.getColumn(ISlideDataElement.BASE);
                cy3 = slideDataElement.getIntensity(ISlideDataElement.CY3);
                cy5 = slideDataElement.getIntensity(ISlideDataElement.CY5);
                fieldNames = TMEV.getFieldNames();
                
                String rowColLabel = "Row : Column";
                String cy3cy5Label = "Cy3 : Cy5";
                stringLength = 12;
                
                for (int i = 0; i < fieldNames.length; i++) {
                    stringLength = Math.max(stringLength, fieldNames[i].length());
                }
                
                displayString = rowColLabel + space(12, stringLength) + trueRow + " : " + trueColumn + "\n";
                displayString += cy3cy5Label + space(9, stringLength) + cy3 + " : " + cy5;
                
                for (int i = 0; i < fieldNames.length; i++) {
                    displayString += "\n" + fieldNames[i] + space(fieldNames[i].length(), stringLength) + slideDataElement.getFieldAt(i);
                }
                
                //System.out.println("In use?");
                //InfoDisplay infoDisplay = new InfoDisplay(getFrame(), displayString, 20, 80);
            }
        }
        
        public ISlideDataElement getSlideDataElementAt(int targetx, int targety) {
            ISlideDataElement slideDataElement = null;
            int row, column;
            
            row    = findRow(targetx, targety);
            column = findColumn(targetx, targety);
            final int columns = slideData.getSlideMetaData().getColumns();
            if (column > 0 && row > 0) {
                int index = (row-1)*columns +column -1;
                if (index < indices.length) {
                    return(ISlideDataElement)slideData.getSlideDataElement(indices[index]);
                }
            }
            return null;
        }
        
        public int findColumn(int targetx, int targety) {
            int column = 0;
            if (inHorizontalPositionOne(targetx, targety)) {
                column = (targetx-preXSpacing)/xElementSize +1;
            } else if (inHorizontalPositionTwo(targetx, targety)) {
                column = (targetx-getX2Start())/xElementSize +1;
            }
            return column;
        }
        
        public int findRow(int targetx, int targety) {
            int row = 0;
            if (inVerticalPosition(targetx, targety)) {
                row = (targety - preYSpacing)/yElementSize +1;
            }
            return row;
        }
        
        public boolean inHorizontalPositionOne(int targetx, int targety) {
            if ((targetx - preXSpacing) > 0 && (targetx - preXSpacing - getXSize()) < 0) return true;
            else return false;
        }
        
        public boolean inHorizontalPositionTwo(int targetx, int targety) {
            if (paletteStyle != SingleArrayViewerCanvas.BLUETORED)
                return false;
            if ((targetx - getX2Start()) > 0 && (targetx - getX2Start() - getXSize()) < 0)
                return true;
            else
                return false;
        }
        
        public boolean inHorizontalPosition(int targetx, int targety) {
            if ((inHorizontalPositionOne(targetx, targety)) || (inHorizontalPositionTwo(targetx, targety)))
                return true;
            else
                return false;
        }
        
        public boolean inVerticalPosition(int targetx, int targety) {
            if ((targety - preYSpacing) > 0 && (targety - preYSpacing - getYSize()) < 0) return true;
            else return false;
        }
        
        public boolean inPositionOne(int targetx, int targety) {
            if ((inHorizontalPositionOne(targetx, targety)) && (inVerticalPosition(targetx, targety))) return true;
            else return false;
        }
        
        public boolean inPositionTwo(int targetx, int targety) {
            if (paletteStyle != SingleArrayViewerCanvas.BLUETORED)
                return false;
            if ((inHorizontalPositionTwo(targetx, targety)) && (inVerticalPosition(targetx, targety)))
                return true;
            else
                return false;
        }
        
        public boolean inPosition(int targetx, int targety) {
            if ((inHorizontalPosition(targetx, targety)) && (inVerticalPosition(targetx, targety)))
                return true;
            else
                return false;
        }
        
        public void drawColoredBoxAt(Graphics2D g, int targetx, int targety, Color targetColor) {
            boolean legal = false, draw = false;
            int column = 0, row = 0, oldColumn = 0, oldRow = 0, location = 0, oldLocation = 0, xpos, ypos;
            int xOldEvent = getXOldEvent();
            int yOldEvent = getYOldEvent();
            
            column = findColumn(targetx, targety);
            row = findRow(targetx, targety);
            
            oldColumn = findColumn(xOldEvent, yOldEvent);
            oldRow = findRow(xOldEvent, yOldEvent);
            if (oldColumn == column && oldRow == row) {
                if (paletteStyle != SingleArrayViewerCanvas.BLUETORED)
                    return;
            }
            
            // clear old selection
            if (inPositionOne(xOldEvent, yOldEvent)) {
                xpos = getXpos(oldColumn);
                ypos = getYpos(oldRow);
                drawRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                draw = true;
            } else if (inPositionTwo(xOldEvent, yOldEvent)) {
                xpos = getXpos(oldColumn);
                ypos = getYpos(oldRow);
                drawRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                draw = true;
            }
            //draw target
            if (inPositionOne(targetx, targety)) {
                xpos = getXpos(column);
                ypos = getYpos(row);
                drawRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, targetColor);
                draw = true;
                legal = true;
            } else if (inPositionTwo(targetx, targety)) {
                xpos = getXpos(column);
                ypos = getYpos(row);
                drawRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, targetColor);
                draw = true;
                legal = true;
            }
            //if (draw == true) paint(getGraphics());
        }
        
        public int getXpos(int column) {
            int xpos;
            xpos = (column - 1) * xElementSize;
            return xpos;
        }
        
        public int getYpos(int row) {
            int ypos;
            ypos = (row - 1) * yElementSize;
            return ypos;
        }
        
        public void drawSlide(Graphics2D g) {
            ISlideDataElement slideDataElement;
            
            if (slideData == null){
                System.out.println("IN SAV canvas null data");                
                return;                
            }
            int index;
            final int rows    = slideData.getSlideMetaData().getRows();
            final int columns = slideData.getSlideMetaData().getColumns();
            if (columns==0) {
                System.out.println("IN SAV canvas columns = 0"); 
                return;
            }
            int row, column;
            final int SIZE = indices.length;
            if (paletteStyle == SingleArrayViewerCanvas.BLUETORED) {
                //setX2Size();
                eraseAll(g);
                eraseFoot(g);
                drawSlideFileName(g, slideData.getSlideDataName());
                
                for (int i = 0; i < SIZE; i++) {
                    slideDataElement = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    row = i/columns +1;
                    column = i % columns +1;
                    if (slideDataElement != null) {
                        drawSlideDataElement(g, indices[i], row, column);
                        drawSlideDataElement2(g, indices[i], row, column);
                    } else {
                        drawNullElement(g, i);
                        drawNullElement2(g, i);
                    }
                }
                
                if (SIZE < rows*columns) {
                    for (int i = SIZE; i < rows*columns; i++) {
                        drawNullElement(g, i);
                        drawNullElement2(g, i);
                    }
                }
                drawScale(g);
                setFoot(g);
            } else {
                //setSize();
                eraseAll(g);
                drawSlideFileName(g, slideData.getSlideDataName());
                for (int i = 0; i < SIZE; i++) {
                    slideDataElement = (ISlideDataElement)slideData.getSlideDataElement(indices[i]);
                    row = i/columns +1;
                    column = i % columns +1;
                    if (slideDataElement != null)
                        drawSlideDataElement(g, indices[i], row, column);
                    else
                        drawNullElement(g,i);
                }
                if (SIZE < rows*columns) {
                    for (int i = SIZE; i < rows*columns; i++) {
                        drawNullElement(g, i);
                    }
                }
            }
        }
        
        /*
        public void drawSlide2(Graphics2D g)
            {
            ISlideDataElement slideDataElement;
         
            if(slideData != null)
                {
                setSize(getX2Start() + getXSize() + postXSpacing, preYSpacing + getYSize() + postYSpacing);
         
                eraseSlideDataElements2(g);
                eraseFoot(g);
                for(int i = 0; i < slideData.size(); i++)
                    {
                    try
                        {
                        slideDataElement = slideData.getElementAt(i);
                        if (slideDataElement != null) drawSlideDataElement2(g, slideDataElement);
                        else drawNullElement2(g, i);
                        }
                    catch (NullPointerException npe) {;}
                    }
                setFoot(g);
                }
            }
         */
        
        public void eraseAll(Graphics2D g) {
            eraseSlideDataElements(g);
            eraseSlideDataElements2(g);
            eraseSlideFileName(g);
            eraseFoot(g);
        }
        
        public void eraseSlideDataElements(Graphics2D g) {
            fillRect(g, 0, 0, getXSize(), getYSize(), Color.white);
        }
        
        public void eraseSlideDataElements2(Graphics2D g) {
            fillRect(g, getX2Start(), 0, getXSize(), getYSize(), Color.white);
        }
        
        public void eraseSlideFileName(Graphics2D g) {
            fillRect(g, preXSpacing, 0, getXSize(), preYSpacing - 20, Color.white);
        }
        
        public void eraseFoot(Graphics2D g) {
            fillRect(g, 0, 0, 2 * getXSize(), preYSpacing, Color.white);
            fillRect(g, preXSpacing + 1, preYSpacing, preXSpacing, getYSize() + preYSpacing, Color.white);
        }
        
        public Color getFalseColor(long value) {
            Color falseColor = null;
            Double scale = new Double(0.0);
            
            try {
                if (style == SingleArrayViewerCanvas.LOG) {
                    if (value == 0) scale = new Double(255 * ((double) Math.log((double) 1.0) / Math.log((double) getTopCy3())));
                    else scale = new Double(255 * ((double) Math.log((double) value) / Math.log((double) getTopCy3())));
                } else if (style == SingleArrayViewerCanvas.LINEAR) {
                    scale = new Double(255 * ((double) value) / getTopCy3());
                }
                
                if (scale.doubleValue() > 255.0) scale = new Double(255);
                else if (scale.doubleValue() < 0) scale = new Double(0); //To avoid those negatives...
                try {
                    falseColor = (Color) palette.elementAt(scale.intValue());
                } catch (Exception e) {
                    System.out.println("!!!!!");
                }
            } catch (Exception e) {
                System.out.println("Exception (SingleArrayViewerCanvas.getFalseColor()): " + e);
            } finally {
                return falseColor;
            }
        }
        
        public Color getFalseColor2(long value) {
            Color falseColor = new Color(0, 0, 0);
            Double scale = new Double(0.0);
            
            try {
                if (this.style == SingleArrayViewerCanvas.LOG) {
                    if (value == 0) scale = new Double(255 * ((double) Math.log((double) 1.0) / Math.log((double) getTopCy5())));
                    else scale = new Double(255 * ((double) Math.log((double) value) / Math.log((double) getTopCy5())));
                } else if (this.style == SingleArrayViewerCanvas.LINEAR) {
                    scale = new Double(255 * ((double) value) / getTopCy5());
                }
                
                if (scale.doubleValue() > 255.0) scale = new Double(255);
                else if (scale.doubleValue() < 0) scale = new Double(0); //To avoid those negatives...
                falseColor = (Color) palette.elementAt(scale.intValue());
            } catch (Exception e) {
                System.out.println("Exception (SingleArrayViewerCanvas.getFalseColor()): " + e);
            } finally {
                return falseColor;
            }
        }
        
        public Color getGreenScaleColor(long value) {
            Color greenScaleColor = null;
            float temp;
            try {
                if (scaleOn == true) {
                    if (style == SingleArrayViewerCanvas.LOG) {
                        if (value == 0) {
                            temp = new Double((float) Math.log((float) 1.0) / Math.log((float) getTopCy3())).floatValue();
                            if (temp > 255) temp = 255;
                            if (temp < 0) temp = 0;
                            //COLOR
                            if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) greenScaleColor = new Color(0, temp, 0);
                            else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) greenScaleColor = new Color(0, 0, temp);
                        } else {
                            temp = new Double((float) Math.log((float) value) / Math.log((float) getTopCy3())).floatValue();
                            if (temp > 255) temp = 255;
                            if (temp < 0) temp = 0;
                            //COLOR
                            if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) greenScaleColor = new Color(0, temp, 0);
                            else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) greenScaleColor = new Color(0, 0, temp);
                        }
                    } else if (style == SingleArrayViewerCanvas.LINEAR) {
                        temp = (float) value / getTopCy3();
                        if (temp > 1) temp = 1;
                        if (temp < 0) temp = 0;
                        //COLOR
                        if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) greenScaleColor = new Color(0, temp, 0);
                        else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) greenScaleColor = new Color(0, 0, temp);
                    }
                } else if (scaleOn == false) {
                    if (value == 0) greenScaleColor = new Color(0, 0, 0);
                    //COLOR
                    else {
                        if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) greenScaleColor = new Color(0, 255, 0);
                        else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) greenScaleColor = new Color(0, 0, 255);
                    }
                }
            } catch (IllegalArgumentException iae) {
                ;
            } catch (Exception e) {
                System.out.println("Exception (SingleArrayViewerCanvas.getGreenScaleColor()): " + e);
            } finally {
                return greenScaleColor;
            }
        }
        
        public Color getRedScaleColor(long value) {
            Color redScaleColor = null;
            float temp;
            
            try {
                if (scaleOn == true) {
                    if (style == SingleArrayViewerCanvas.LOG) {
                        if (value == 0) {
                            temp = new Double((float) Math.log((float) 1.0) / Math.log((float) getTopCy5())).floatValue();
                            if (temp > 255) temp = 255;
                            if (temp < 0) temp = 0;
                            redScaleColor = new Color(temp, 0, 0);
                        } else {
                            temp = new Double((float) Math.log((float) value) / Math.log((float) getTopCy5())).floatValue();
                            if (temp > 255) temp = 255;
                            if (temp < 0) temp = 0;
                            redScaleColor = new Color(temp, 0, 0);
                        }
                    } else if (style == SingleArrayViewerCanvas.LINEAR) {
                        temp = (float) value / getTopCy5();
                        if (temp > 1) temp = 1;
                        if (temp < 0) temp = 0;
                        redScaleColor = new Color(temp, 0, 0);
                    }
                } else if (scaleOn == false) {
                    if (value == 0) redScaleColor = new Color(0, 0, 0);
                    else redScaleColor = new Color(255, 0, 0);
                }
            } catch (IllegalArgumentException iae) {
                ;
            } catch (Exception e) {
                System.out.println("Exception (SingleArrayViewerCanvas.getRedScaleColor()): " + e);
            } finally {
                return redScaleColor;
            }
        }
        
        public void drawSlideDataElement(Graphics2D g, int dataRow, int row, int column) {
            Color color;
            //int row, column,
            int  adjustedInt;
            long adjustedLong;
            
            //row = slideDataElement.getRow(SlideDataElement.BASE);
            //column = slideDataElement.getColumn(SlideDataElement.BASE);
            
            float cy3 = slideData.getCY3(dataRow);
            float cy5 = slideData.getCY5(dataRow);
            // System.out.println("cy3 = "+cy3+" cy5 = "+cy5);
            int xpos = getXpos(column);
            int ypos = getYpos(row);
            
            switch (paletteStyle) {
                case SingleArrayViewerCanvas.BLUETORED:
                    if (thresholdsOn == true) {
                        if ((cy3+cy5 > 0) && ((cy3/(cy3+cy5) > highDifference) || (cy3/(cy3+cy5) < lowDifference))) {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, getFalseColor((long)cy3));
                        } else if (cy3 == 0 && cy5 == 0) {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
                        } else {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                        }
                    } else {
                        if (cy3 == 0 && cy5 == 0) {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
                        } else {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, getFalseColor((long)cy3));
                        }
                    }
                    break;
                case SingleArrayViewerCanvas.GREENRED:
                    if (cy3 + cy5 == 0) adjustedInt = 0;
                    else adjustedInt = (int) (xElementSize * cy3 / (cy3 + cy5));
                    if (adjustedInt > xElementSize) adjustedInt = xElementSize;
                    
                    if (cy3 <= 0 && cy5 <= 0) fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
                    else {
                        if (thresholdsOn == true) {
                            if ((cy3 + cy5 > 0) && ((cy3/(cy3+cy5) > highDifference) || (cy3/(cy3+cy5) < lowDifference))) {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, getRedScaleColor((long)cy5));
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, adjustedInt, yElementSize, getGreenScaleColor((long)cy3));
                            } else {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                            }
                        } else {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, getRedScaleColor((long)cy5));
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, adjustedInt, yElementSize, getGreenScaleColor((long)cy3));
                        }
                    }
                    break;
                case SingleArrayViewerCanvas.OVERLAY:
                    if (thresholdsOn == true) {
                        if ((cy3+cy5 > 0) && ((cy3/(cy3+cy5) > highDifference) || (cy3/(cy3+cy5) < lowDifference))) {
                            //COLOR
                            if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize,
                                new Color(getRedScaleColor((long)cy5).getRed(), getGreenScaleColor((long)cy3).getGreen(), 0));
                            } else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize,
                                new Color(getRedScaleColor((long)cy5).getRed(), 0, getGreenScaleColor((long)cy3).getBlue()));
                            }
                        } else if (cy3 == 0 && cy5 == 0) {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
                        } else {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                        }
                    } else {
                        //COLOR
                        if (cy3 == 0 && cy5 == 0) {
                            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
                        } else {
                            if (colorScheme == SingleArrayViewerCanvas.RED_GREEN_COLOR_SCHEME) {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize,
                                new Color(getRedScaleColor((long)cy5).getRed(), getGreenScaleColor((long)cy3).getGreen(), 0));
                            } else if (colorScheme == SingleArrayViewerCanvas.RED_BLUE_COLOR_SCHEME) {
                                fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize,
                                new Color(getRedScaleColor((long)cy5).getRed(), 0, getGreenScaleColor((long)cy3).getBlue()));
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            drawRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
        }
        
        public void drawSlideDataElement2(Graphics2D g, int dataRow, int row, int column) {
            Color color;
            //int row, column,
            int adjustedInt;
            long adjustedLong;
            
            //row = slideDataElement.getRow(ISlideDataElement.BASE);
            //column = slideDataElement.getColumn(ISlideDataElement.BASE);
            //float cy3 = slideDataElement.getIntensity(ISlideDataElement.CY3);
            //float cy5 = slideDataElement.getIntensity(ISlideDataElement.CY5);
            float cy3 = slideData.getCY3(dataRow);
            float cy5 = slideData.getCY5(dataRow);
            int xpos = getXpos(column);
            int ypos = getYpos(row);
            
            if (thresholdsOn == true) {
                if ((cy3 + cy5 > 0) && ((cy3 / (cy3 + cy5) > highDifference) || (cy3 / (cy3 + cy5) < lowDifference))) {
                    fillRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, getFalseColor2((long)cy5));
                    drawRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                } else {
                    fillRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.black);
                }
            } else {
                fillRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, getFalseColor2((long)cy5));
                drawRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.black);
            }
        }
        
        public int getX2Start() {
            return(preXSpacing + getXSize() + getPaletteSpacing());
        }
        
        public void drawNullElement(Graphics2D g, int location) {
            int row, column, xpos, ypos;
            
            row = (location / slideData.getSlideMetaData().getColumns()) + 1;
            column = (location % slideData.getSlideMetaData().getColumns()) + 1;
            xpos = getXpos(column);
            ypos = getYpos(row);
            
            fillRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
            drawRect(g, xpos + preXSpacing, ypos + preYSpacing, xElementSize, yElementSize, Color.black);
        }
        
        public void drawNullElement2(Graphics2D g, int location) {
            int row, column, xpos, ypos;
            
            row = (location / slideData.getSlideMetaData().getColumns()) + 1;
            column = (location % slideData.getSlideMetaData().getColumns()) + 1;
            xpos = getXpos(column);
            ypos = getYpos(row);
            
            fillRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.gray);
            drawRect(g, xpos + getX2Start(), ypos + preYSpacing, xElementSize, yElementSize, Color.black);
        }
        
        public void drawSlideFileName(Graphics2D g, String filename) {
            drawString(g, filename, (16 * getXSize() / 40) + preXSpacing, preYSpacing / 2, Color.black, largeFont);
        }
        
        public void setFoot(Graphics2D g) //And head, really...
        {
            int xGap = getXSize();
            int yGap = getYSize();
            
            drawString(g, "Cy3", (19 * xGap / 40) + preXSpacing, preYSpacing + yGap + postYSpacing - 10, Color.black, largeFont);
            drawString(g, "Cy5", (19 * xGap / 40) + 2 * preXSpacing + xGap + 15, preYSpacing + yGap + postYSpacing - 10, Color.black, largeFont);
            drawString(g, Long.toString((long)getTopCy3()), (16 * xGap / 20) + preXSpacing, preYSpacing - 5, Color.black, smallFont);
            drawString(g, Long.toString((long)getTopCy5()), preXSpacing + xGap + getPaletteSpacing(), preYSpacing - 5, Color.black, smallFont);
        }
        
        public void drawScale(Graphics2D g) {
            int width = 0, height = 0;
            int leftPosition = 0, topPosition;
            
            if ((xElementSize / 2) < 1) width = 1;
            else width = (int) (((double) xElementSize) / 2.0);
            if ((getYSize() / palette.size()) < 1) height = 1;
            else height = (int) ((double) getYSize() / palette.size());
            leftPosition = preXSpacing + getXSize() + ((getPaletteSpacing() - width) / 2);
            topPosition = preYSpacing + (getYSize() - (height * palette.size())) / 2;
            
            for (int i = 0; i < palette.size(); i++) {
                fillRect(g, leftPosition, topPosition + i * height, width, height, (Color) palette.elementAt(255 - i));
            }
        }
    }
    
    private void setXOldEvent(int x) {
        oldX = x;
    }
    
    private void setYOldEvent(int y) {
        oldY = y;
    }
    
    private int getXOldEvent() {
        return oldX;
    }
    
    private int getYOldEvent() {
        return oldY;
    }
    
    
    public Vector buildPalette() {
        Vector palette = new Vector(256);
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
    
    private class SingleArrayViewerFrame extends Frame {
        protected void processWindowEvent(WindowEvent e) {
            if (e.getID() == WindowEvent.WINDOW_CLOSING) dispose();
            super.processWindowEvent(e);
        }
    }
}