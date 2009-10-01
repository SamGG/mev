package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.ChARM;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.PValue;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.TestSignificanceDialog;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs.ExportResultsDialog;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * Mainframe class renamed CharmGUI.java
 */
/**
* This class coordinates all of the ChARM GUI version operation.
*
 * <p>Title: MainFrame</p>
 * <p>Description: This class coordinates all of the ChARM GUI version operation.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

//public class MainFrame extends JPanel implements IViewer {
public class CharmGUI extends JPanel implements IViewer {

  public static final int LEFT_WIDTH = 285;
  //public static final int LEFT_HEIGHT = 100;
  //public static final int RIGHT_WIDTH = 1200;
  //public static final int RIGHT_HEIGHT = 700;
  //public static final int GRAPH_WIDTH = 800;
  //public static final int GRAPH_HEIGHT = 3200;
  //public static final int LEFT_COMP_HEIGHT = 50;
  //public static final int BUTTON_HEIGHT = 50;

  public static final String EXTENSION = ".txt";

  public static final Color GRAPH_BACKGROUND = Color.BLACK; //.LIGHT_GRAY;
  public static final Color STAT_BACKGROUND = Color.WHITE;

  public static final int TIMER_DELAY = 1000;
  
  /* TODO Replaced with ChARM reference */
  //DisplayStateManager displayState;
  ChARM displayState;
  
  JPanel contentPane = this;
  //JMenuBar jMenuBar1 = new JMenuBar();
  //JMenu jMenuFile = new JMenu();
  //JMenuItem jMenuFileExit = new JMenuItem();
  //JMenu jMenuHelp = new JMenu();
  //JMenuItem jMenuHelpAbout = new JMenuItem();
  //JToolBar jToolBar = new JToolBar();
  ImageIcon image1;
  ImageIcon image2;
  ImageIcon zoomInImage;
  ImageIcon zoomOutImage;
  ImageIcon image5;
  ImageIcon mousePointerImage;
  ImageIcon resetZoom;
  ImageIcon saveImage;
  ImageIcon image9;
  ImageIcon image10;
  ImageIcon predImage;


  //MenuListener menuListener = new MenuListener();
  GraphButtonListener graphButtonListener = new GraphButtonListener();
  ExpOptionsCheckboxListener expCheckboxListener = new ExpOptionsCheckboxListener();
  //PredictionOptionsCheckboxListener predCheckboxListener = new PredictionOptionsCheckboxListener();


  private Cursor zoomInCursor;
  private Cursor zoomOutCursor;

  JPanel dummyPanel = new JPanel();

  //JButton fileOpenButton = new JButton();
  //JButton fileCloseButton = new JButton();

  JToggleButton zoomInButton = new JToggleButton();
  JToggleButton zoomOutButton = new JToggleButton();
  JToggleButton selectButton = new JToggleButton();
  JButton resetZoomButton = new JButton();

  BorderLayout borderLayout1 = new BorderLayout();
  JTabbedPane jTabbedPane1 = new JTabbedPane();

  //JPanel statusPanel = new JPanel();
  //JLabel statusText;

  Box viewResultsBox = Box.createVerticalBox();
  Box expOptionsBox = Box.createVerticalBox();
  Box predictionOptionsBox = Box.createVerticalBox();
  //Box displayOptionsBox = Box.createVerticalBox();

  //StatComponent statdisp;

  JPanel genelistPanel;
  //JPanel leftButtonsPanel;

  JPanel pvaluePanel;

  JButton exportGenesButton;
  //JButton runCharmButton;
  JButton runSigTestButton;

  FileDialog fileDialog1;

  PScrollPane scrollPaneRight;

  JPanel jPanelToolBar = new JPanel();
  JToolBar jToolBarZoom = new JToolBar();
  GraphViewPanel graphPanel;
  JSplitPane jSplitPane1 = new JSplitPane();
  //JMenu jMenuTools = new JMenu();

  GeneList genelist;
  GeneTable geneTable;
  int exptID;
  
  //private JToggleButton zoom_inbutton;
  //private JToggleButton zoom_outbutton;
  //private JButton zoom_reset;
  //private JToggleButton select_button;
  //private JToggleButton kgraph_button;
  //private JToggleButton predictgraph_button;

  private JPanel expcheckboxes;
  private JPanel predcheckboxes;

  //private JDialog rundialog;
  //private JSpinner num_permute;
  //private JButton cancel_button;

  private JTextField[] pvalue_fields;
  private JComboBox cutoff_box;
  private JButton update_cutoffs_button;


  private JScrollPane expscrollpane;

  //private int delay;
  //private int elapsedTime;

  XYLayout xYLayout1 = new XYLayout();
  //JMenuItem jMenuLoadDataset = new JMenuItem();
  //JMenuItem jMenuCloseDataset = new JMenuItem();
  //JToolBar jToolBar1 = new JToolBar(); //UnUsed
  //JToolBar jToolBar3 = new JToolBar(); //UnUsed
  //JToolBar jToolBarDummy = new JToolBar();
  JToolBar jToolBarRatioPred = new JToolBar();
  //JToggleButton toggleRatiosButton = new JToggleButton();
  JToggleButton togglePredictionsButton = new JToggleButton();
  TitledBorder titledBorder1;
  //JMenuItem jMenuAnalyzeData = new JMenuItem();
  //JMenuItem jMenuExportResults = new JMenuItem();
  //JMenuItem jMenuHelpView = new JMenuItem();
  JButton exportButton = new JButton();
  //JButton analyzeDataButton = new JButton();
  //JButton helpButton = new JButton();
  //JMenu jMenuSettings = new JMenu();
  //JMenuItem jMenuDisplaySettings = new JMenuItem();
  
  IFramework framework;
  IData data;
  int analyzedExprIndices[];
  //ArrayList selectedExps;
  
  //Construct the frame
  /**
   * Class constructor.
   */
  public CharmGUI(IFramework framework, ChARM charm, int exprInidces[]) {
	  //System.out.println("In CharmGUI Constructor");
	this.framework = framework;
	data = this.framework.getData();
	analyzedExprIndices = exprInidces;
	
    //displayState = new DisplayStateManager(this);
	displayState = charm;
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Const created for State Saving
   * @param dat
   * @param charm
   * @param exprInidces
   */
  public CharmGUI(IData dat, ChARM charm, int exprInidces[]) {
	  //System.out.println("In SS CharmGUI Constructor");
	this.data = dat;
	if(this.data == null){
    	System.out.println("In Const. CharmGUI()- data is null");
    }
	this.analyzedExprIndices = exprInidces;
	this.displayState = charm;
	//System.out.println("Charm Selected Expr size(): "+ charm.getSelectedExperiments().size());
	//System.out.println("Charm Selected Resultset size(): "+ charm.getSelectedResultSets().getExperiments().size());
	//System.out.println("Charm Expr List:" + charm.getExperimentList().size());
	
	// Hack to set Charm's CharmGUI variable to this to avoid null gui during load up
	charm.setCharmGUI(this);
	//this.displayState.setPValueCutoff(new PValue(.01,.01));
    //this.displayState.setPValueTestType(PValue.MEAN_AND_SIGN_TEST);
	
	try {
	      jbInit();
	    }
	    catch (Exception e) {
	    	System.out.println("Exception in CharmGUI constructor, jbInit()");
	    	e.printStackTrace();
	    }
	    
	      this.graphPanel.initializePredictionNodes(charm.getSelectedExperiments());
	      this.togglePredictionsButton.setEnabled(true);
	      this.togglePredictionsButton.setSelected(true);
	      
	      this.displayState.setStateVariable("Prediction Plot Toggle","on");
	      
	      this.jTabbedPane1.setSelectedIndex(1);
	      this.graphPanel.updateGraph();
	      this.validate();
	      
	      //updategraphPanel();
	      //charm.updateCharmViewer();
  }
  //Component initialization
  /**
   * Component initialization.
   * @throws Exception
   */
  private void jbInit() throws Exception {
	  //System.out.println("In CharmGUI jbInit()");
    //contentPane = (JPanel)this.getContentPane();
	contentPane = (JPanel)this;
    //titledBorder1 = new TitledBorder("");
    contentPane.setLayout(borderLayout1);
    //this.setSize(new Dimension(837, 300));
    //this.setTitle("ChARM View");
    //ImageIcon im3 = new ImageIcon(MainFrame.class.getResource("bin/org/tigr/images/charm_final1_icon.png"));
    //ImageIcon im3 = new ImageIcon("org/tigr/images/charm_final1_icon.png");
    //this.setIconImage(im3.getImage());

    initButtons();
    //initMenu();
    initLeftPanel();
    initRightPanel();

//  set up split panel
    /*
    jSplitPane1.add(scrollPaneRight, JSplitPane.RIGHT);
    jSplitPane1.add(jTabbedPane1, JSplitPane.LEFT);
    jSplitPane1.setDividerLocation(254);
    contentPane.add(jSplitPane1, BorderLayout.CENTER);
    */
    
    jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollPaneRight,jTabbedPane1);
    //jSplitPane1.setDividerLocation(JSplitPane.VERTICAL_SPLIT);
    //jSplitPane1.add(scrollPaneRight, JSplitPane.TOP);
    //jSplitPane1.add(jTabbedPane1, JSplitPane.BOTTOM);
    //jSplitPane1.setResizeWeight(0.5);
    jSplitPane1.setOneTouchExpandable(true);
    jSplitPane1.setContinuousLayout(true);
    jSplitPane1.setDividerLocation(600);
    contentPane.add(jSplitPane1, BorderLayout.CENTER);

    /*
    ImageIcon im = new ImageIcon(MainFrame.class.getResource("charm_ZoomIn24_3.gif"));
    ImageIcon im2 = new ImageIcon(MainFrame.class.getResource("charm_ZoomOut24_3.gif"));
    */
    // TODO, Select Good Cursors
    ImageIcon im = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_zoom-in.png"));
    ImageIcon im2 = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_zoom-out.png"));
    
    zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(im.getImage(), new Point(0, 0), "Zoom In Cursor");
    zoomOutCursor = Toolkit.getDefaultToolkit().createCustomCursor(im2.getImage(), new Point(0, 0), "Zoom Out Cursor");

  }



  /**
   * Overridden so we can exit when window is closed
   * Not needed in MeV
   */
  /*
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }
  */
  /**
   * Initialize all interface buttons.
   */
  public void initButtons() {
	  //System.out.println("In CharmGUI initButtons()");
    jPanelToolBar.setLayout(xYLayout1);
    jPanelToolBar.setToolTipText("");
    jPanelToolBar.add(jToolBarZoom, new XYConstraints(5, 2, 0, 0));
    //jPanelToolBar.add(jToolBar,  new XYConstraints(7, 6, 245, -1));
    jPanelToolBar.add(jToolBarRatioPred, new XYConstraints(245, 3, 0, 0));
    contentPane.add(jPanelToolBar, BorderLayout.NORTH);

    /*
    image1 = new ImageIcon(MainFrame.class.getResource("charm_openFile.png"));
    image2 = new ImageIcon(MainFrame.class.getResource("charm_closeFile.png"));
    zoomInImage = new ImageIcon(MainFrame.class.getResource("charm_ZoomIn24.png"));
    zoomOutImage = new ImageIcon(MainFrame.class.getResource("charm_ZoomOut24.png"));
    image5 = new ImageIcon(MainFrame.class.getResource("charm_help.png"));
    mousePointerImage  = new ImageIcon(MainFrame.class.getResource("charm_Select24.png"));
    resetZoom = new ImageIcon(MainFrame.class.getResource("charm_ZoomReset24.png"));
    saveImage = new ImageIcon(MainFrame.class.getResource("charm_Save16_2.gif"));
    image9 = new ImageIcon(MainFrame.class.getResource("charm_AnalyzeData16.gif"));
    image10 = new ImageIcon(MainFrame.class.getResource("charm_help.png"));
    */

    //image1 = new ImageIcon(this.getClass().getResource("org/tigr/images/charm_openFile.png"));
    //image2 = new ImageIcon("org/tigr/images/charm_closeFile.png");
    //image5 = new ImageIcon("org/tigr/images/charm_help.png");
    zoomInImage = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_zoom-in.png"));
    zoomOutImage = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_zoom-out.png"));
    mousePointerImage  = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_mouse.png"));
    resetZoom = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_fit-1.png"));
    saveImage = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_save.png"));
    predImage = new ImageIcon(this.getClass().getResource("/org/tigr/images/Charm_data-1.png"));
    //image9 = new ImageIcon(this.getClass().getResource("/org/tigr/images/charm_AnalyzeData16.gif"));
    //image10 = new ImageIcon(this.getClass().getResource("/org/tigr/images/charm_help.png"));

    //fileOpenButton.setIcon(image1);
    //fileOpenButton.setToolTipText("Load dataset");
    //fileOpenButton.setActionCommand("Load dataset");
    //fileOpenButton.addActionListener(menuListener);

    //fileCloseButton.setIcon(image2);
    //fileCloseButton.setToolTipText("Close dataset");
    //fileCloseButton.setActionCommand("Close dataset");
    //fileCloseButton.addActionListener(menuListener);
    //fileCloseButton.setEnabled(false);

       exportButton.addActionListener(graphButtonListener);
       exportButton.setActionCommand("Export results");
       exportButton.setToolTipText("Export data");
       exportButton.setIcon(saveImage);
       exportButton.setEnabled(true);

       //analyzeDataButton.addActionListener(menuListener);
       //analyzeDataButton.setActionCommand("Analyze data");
       //analyzeDataButton.setToolTipText("Run ChARM algorithm");
       //analyzeDataButton.setIcon(image9);
       //analyzeDataButton.setEnabled(false);

       //helpButton.setIcon(image10);
       //helpButton.setToolTipText("View help file");
       //helpButton.setActionCommand("View help");
       //helpButton.addActionListener(menuListener);
       //helpButton.setEnabled(true);



    zoomInButton.setIcon(zoomInImage);
    zoomInButton.setToolTipText("Zoom In");
    zoomInButton.setActionCommand("Zoom In");
    zoomInButton.addActionListener(graphButtonListener);

    zoomOutButton.setIcon(zoomOutImage);
    zoomOutButton.setToolTipText("Zoom Out");
    zoomOutButton.setActionCommand("Zoom Out");
    zoomOutButton.addActionListener(graphButtonListener);

    selectButton.setIcon(mousePointerImage);
    selectButton.setToolTipText("Select mode");
    selectButton.setActionCommand("Select");
    selectButton.addActionListener(graphButtonListener);
    selectButton.setSelected(true);
    displayState.setGraphMode("Select");

    resetZoomButton.setIcon(resetZoom);
    resetZoomButton.setToolTipText("Reset Zoom");
    resetZoomButton.setActionCommand("Reset Zoom");
    resetZoomButton.addActionListener(graphButtonListener);

    //toggleRatiosButton.setText("Data");
    //toggleRatiosButton.setToolTipText("Toggle data plots");
    //toggleRatiosButton.setActionCommand("Toggle Ratios");
    //toggleRatiosButton.addActionListener(graphButtonListener);
    //toggleRatiosButton.setEnabled(false);
    //toggleRatiosButton.setEnabled(true);

    togglePredictionsButton.setIcon(predImage);
    //togglePredictionsButton.setText("Predictions");
    togglePredictionsButton.setToolTipText("Toggle prediction plots");
    togglePredictionsButton.setActionCommand("Toggle Predictions");
    togglePredictionsButton.addActionListener(graphButtonListener);
    togglePredictionsButton.setEnabled(true);

    //jToolBar.add(fileOpenButton, null);
    //jToolBar.add(fileCloseButton, null);
    //jToolBar.add(exportButton, null);
    //jToolBar.add(analyzeDataButton, null);
    //jToolBar.add(helpButton, null);
    //jToolBar.setEnabled(true);
    //jToolBar.setBorderPainted(true);
    //jToolBar.setFloatable(false);
    
    jToolBarZoom.add(exportButton, null);
    jToolBarZoom.add(selectButton, null);
    jToolBarZoom.add(zoomInButton, null);
    jToolBarZoom.add(zoomOutButton, null);
    jToolBarZoom.add(resetZoomButton, null);
    jToolBarZoom.setEnabled(false);

    //jToolBarRatioPred.add(toggleRatiosButton, null);
    jToolBarRatioPred.add(togglePredictionsButton, null);
    jToolBarRatioPred.setBorder(null);
    jToolBarRatioPred.setFloatable(false);

  }

  /**
   * Initialize the GUI menu.
   * Not needed in MeV Raktim
   */
  /*
  public void initMenu() {
    jMenuFile.setText("File");
    jMenuFileExit.setBackground(SystemColor.control);
    jMenuFileExit.setText("Exit");
    jMenuFileExit.setActionCommand("Exit");
    jMenuFileExit.addActionListener(menuListener);

    jMenuLoadDataset.setBackground(SystemColor.control);
    jMenuLoadDataset.setText("Load Dataset");
    jMenuLoadDataset.setActionCommand("Load dataset");
    jMenuLoadDataset.addActionListener(menuListener);

    jMenuCloseDataset.setBackground(SystemColor.control);
    jMenuCloseDataset.setText("Close Dataset");
    jMenuCloseDataset.setActionCommand("Close dataset");
    jMenuCloseDataset.addActionListener(menuListener);
    jMenuCloseDataset.setEnabled(false);

    jMenuExportResults.setText("Export Results");
    jMenuExportResults.setBackground(SystemColor.control);
    jMenuExportResults.setActionCommand("Export results");
    jMenuExportResults.addActionListener(menuListener);
    jMenuExportResults.setEnabled(false);

    jMenuHelp.setText("Help");
    jMenuHelpAbout.setBackground(SystemColor.control);

    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.setActionCommand("About");
    jMenuHelpAbout.addActionListener(menuListener);

    jMenuHelpView.setText("View Help File");
    jMenuHelpView.setBackground(SystemColor.control);
    jMenuHelpView.setActionCommand("View help");
    jMenuHelpView.addActionListener(menuListener);


    jMenuTools.setText("Tools");
    jMenuAnalyzeData.setBackground(SystemColor.control);
    jMenuAnalyzeData.setText("Analyze data");
    jMenuAnalyzeData.setActionCommand("Analyze data");
    jMenuAnalyzeData.addActionListener(menuListener);
    jMenuAnalyzeData.setEnabled(false);


    jMenuSettings.setText("Settings");
    jMenuDisplaySettings.setBackground(SystemColor.control);
    jMenuDisplaySettings.setText("Display settings");
    jMenuDisplaySettings.setActionCommand("Display Settings");
    jMenuDisplaySettings.setEnabled(false);
    jMenuDisplaySettings.addActionListener(menuListener);


    statusText = new JLabel("No datasets loaded  ");
    statusPanel.setLayout(new BorderLayout());

    statusPanel.add(statusText,BorderLayout.EAST);
    contentPane.add(statusPanel,BorderLayout.SOUTH);


    jMenuHelp.add(jMenuHelpView);
    jMenuHelp.add(jMenuHelpAbout);

    jMenuFile.add(jMenuLoadDataset);
    jMenuFile.add(jMenuCloseDataset);
    jMenuFile.add(jMenuExportResults);
    jMenuFile.add(jMenuFileExit);

    jMenuSettings.add(jMenuDisplaySettings);


    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuTools);
    jMenuBar1.add(jMenuSettings);
    jMenuBar1.add(jMenuHelp);

    jMenuBar1.add(jMenuHelp);
    jMenuTools.add(jMenuAnalyzeData);

    this.setJMenuBar(jMenuBar1);
  }
  */
  /**
   * Initialize GUI left panel.
   */
  public void initLeftPanel() {
	  //System.out.println("In CharmGUI initLeftPanel()");
    //jTabbedPane1.setMaximumSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    //jTabbedPane1.setMinimumSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    //jTabbedPane1.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    jTabbedPane1.add(viewResultsBox, "Results");
    jTabbedPane1.add(expOptionsBox, "Experiment Options");
    jTabbedPane1.add(predictionOptionsBox, "Prediction Options");

    initViewResultsPanel();
    initExpOptionsPanel();
    initPredOptionsPanel();

  }

  /**
   * Initialize the GUI prediction  options panel.
   */
  public void initPredOptionsPanel() {
	  //System.out.println("In CharmGUI initPredOptionsPanel()");
    //predictionOptionsBox.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    pvaluePanel = new JPanel();
    //pvaluePanel.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT / 4));
    pvaluePanel.setLayout(new BoxLayout(pvaluePanel, BoxLayout.Y_AXIS));
    pvaluePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.
        createEtchedBorder(),
        "Prediction p-value cutoffs"));

    //corresponds to defined constant-types within chromosome class
    String[] cutoff_types = new String[] {
        "Sign AND Mean Tests",
        "Sign OR Mean Tests",
        "Sign Test",
        "Mean Test"};

    JPanel cutoffPanel = new JPanel();
    //cutoffPanel.setPreferredSize(new Dimension(200, 25));
    cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));

    cutoff_box = new JComboBox(cutoff_types);
    cutoff_box.setMaximumSize(new Dimension(LEFT_WIDTH, 25));
    cutoff_box.setSelectedIndex(0);
    cutoff_box.setToolTipText("Select the p-value cutoffs that displayed predictions must meet");

    cutoff_box.addActionListener(new CutoffSelectionListener());

    cutoffPanel.add(new JLabel("Filter predictions by: "));
    cutoffPanel.add(cutoff_box);
    pvaluePanel.add(cutoffPanel);

    pvalue_fields = new JTextField[2];

    String[] pvalue_labels = new String[] { "Sign p-value cutoff:  ", 
    										"Mean p-value cutoff:  "};
    for(int i=0; i < pvalue_fields.length; i++) {
      JPanel pvalCutoffpanel = new JPanel();
      pvalCutoffpanel.add(new JLabel(pvalue_labels[i]));
      //pvalCutoffpanel.setPreferredSize(new Dimension(LEFT_WIDTH, 50));
      //pvalCutoffpanel.setMaximumSize(new Dimension(LEFT_WIDTH, 50));
      pvalCutoffpanel.setLayout(new BoxLayout(pvalCutoffpanel, BoxLayout.X_AXIS));
      pvalCutoffpanel.setAlignmentX(pvaluePanel.CENTER_ALIGNMENT);

      pvalue_fields[i] = new JTextField();
      pvalue_fields[i].setMaximumSize(new Dimension(100, 25));

      pvalCutoffpanel.add(pvalue_fields[i]);
      pvaluePanel.add(pvalCutoffpanel);
    }
    pvalue_fields[0].setText("0.01");
    pvalue_fields[1].setText("0.01");

    displayState.setPValueCutoff(new PValue(.01,.01));
    displayState.setPValueTestType(PValue.MEAN_AND_SIGN_TEST);

    predcheckboxes = new JPanel();
    predcheckboxes.setLayout(new BoxLayout(predcheckboxes, BoxLayout.Y_AXIS));
    //ArrayList selectedResultSets = displayState.getSelectedResultSets();
    ResultContainer selectedResultSets = displayState.getSelectedResultSets();

    /* No need for check boxex. Only one result in Mev */
    /*
    for (int i = 0; i < selectedResultSets.size(); i++) {
      JCheckBox checkbox = new JCheckBox((String)selectedResultSets.get(i));
      checkbox.addActionListener(predCheckboxListener);
      checkbox.setAlignmentX(predcheckboxes.LEFT_ALIGNMENT);
      predcheckboxes.add(checkbox);
    }
	*/
    update_cutoffs_button = new JButton("Update Predictions Display");
    update_cutoffs_button.setToolTipText("Filter displayed predictions based on p-value cutoffs");
    update_cutoffs_button.setAlignmentX(pvaluePanel.CENTER_ALIGNMENT);
    update_cutoffs_button.addActionListener(new CutoffListener());

    pvaluePanel.add(update_cutoffs_button);
    predictionOptionsBox.add(pvaluePanel);

    /* Not Needed in MEV, Raktim
    JPanel checkboxPanel = new JPanel();
    checkboxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.
    createEtchedBorder(),
    "Select results to be displayed"));
    checkboxPanel.add(predcheckboxes);
    predictionOptionsBox.add(checkboxPanel);
    */
  }

  /**
   * Initializes Experiment Options panel.
   */
  public void initExpOptionsPanel() {
	  //System.out.println("In CharmGUI initExpOptionsPanel()");
    expOptionsBox.setBackground(Color.lightGray);
    //expOptionsBox.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    expOptionsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    expOptionsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.
        createEtchedBorder(), "Select the experiments to be displayed"));
    expcheckboxes = new JPanel();
    expcheckboxes.setLayout(new BoxLayout(expcheckboxes, BoxLayout.Y_AXIS));
    
    //Raktim Mar 11
    String[] experiment_list = (String[])displayState.getExperimentList().toArray(new String[0]);
    //String[] experiment_list = (String[])displayState.getSelectedExperiments().toArray(new String[0]);
    
    for (int i = 0; i < experiment_list.length; i++) {
      JCheckBox checkbox = new JCheckBox(experiment_list[i]);
      checkbox.addActionListener(expCheckboxListener);
      checkbox.setAlignmentX(expcheckboxes.CENTER_ALIGNMENT);
      expcheckboxes.add(checkbox);
    }

    if (experiment_list.length > 0) {
      ( (JCheckBox) expcheckboxes.getComponent(0)).setSelected(true);
    }

    expscrollpane = new JScrollPane(expcheckboxes);
    expscrollpane.getViewport().setBackground(STAT_BACKGROUND);
    //expscrollpane.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    expOptionsBox.add(expscrollpane);
  }

  /**
   * Initializes View Results panel.
   */
  public void initViewResultsPanel() {
	  //System.out.println("In CharmGUI initViewResultsPanel()");
    //viewResultsBox.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));

    genelistPanel = new JPanel();
    //genelistPanel.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
    genelistPanel.setLayout(new BoxLayout(genelistPanel, BoxLayout.Y_AXIS));

    /* Raktim Mar 13
    genelist = new GeneList((MultipleArrayData)data);
    genelist.addListSelectionListener(new GeneListListener());
    genelist.setBackground(STAT_BACKGROUND);
	*/
    geneTable  = new GeneTable((MultipleArrayData)data);
    geneTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    ListSelectionModel rowSM = geneTable.getSelectionModel();
    rowSM.addListSelectionListener(new GeneTableListener());
    //geneTable.addListSelectionListener(new GeneTableListener());
    geneTable.setBackground(STAT_BACKGROUND);
    
    //Raktim Mar 13
    //JScrollPane genescroll = new JScrollPane(genelist);
    JScrollPane genescroll = new JScrollPane(geneTable);
    //genescroll.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_COMP_HEIGHT));

    //exportGenesButton = new JButton("Save Gene List...");
    //exportGenesButton.setToolTipText("Write the currently listed gene and segment information into a tab-delimited text file");
    //exportGenesButton.setAlignmentX(genelistPanel.CENTER_ALIGNMENT);
    //exportGenesButton.addActionListener(new ExportGenesListener());
    //exportGenesButton.setEnabled(false);
    //runSigTestButton = new JButton("Check Significance...");
    //runSigTestButton.setToolTipText("Select a sequence of genes to run significance tests on");
    //runSigTestButton.addActionListener(new PermuteButtonListener());
    //runSigTestButton.setEnabled(false);

    //JPanel genelistButtonsPanel = new JPanel();
    //genelistButtonsPanel.setLayout(new BoxLayout(genelistButtonsPanel,BoxLayout.X_AXIS));
    //genelistButtonsPanel.add(exportGenesButton);
    //genelistButtonsPanel.add(runSigTestButton);

    genelistPanel.add(genescroll);
    //genelistPanel.add(genelistButtonsPanel);
    viewResultsBox.add(genelistPanel);
    //viewResultsBox.add(jToolBarDummy, null);
    /*
    statdisp = new StatComponent(data);
    JScrollPane statpane = new JScrollPane(statdisp);
    statpane.getViewport().setBackground(STAT_BACKGROUND);
    statpane.setPreferredSize(new Dimension(LEFT_WIDTH, LEFT_COMP_HEIGHT));
    viewResultsBox.add(statpane, null);
    */
  }

  /**
   * Initialize the right panel (experiment display panel).
   */
  public void initRightPanel() {
	  //System.out.println("In CharmGUI initRightPanel()");
	  scrollPaneRight = new PScrollPane();
	  if(scrollPaneRight == null){System.out.println("In CharmGUI initRightPanel() scrollPaneRight is null");}
	  if(displayState == null){System.out.println("In CharmGUI initRightPanel() displayState is null");}
    graphPanel = new GraphViewPanel(displayState,scrollPaneRight);
    //System.out.println("In CharmGUI initRightPanel() - GraphViewPanel created");
    graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
    //graphPanel.setPreferredSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
    graphPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    graphPanel.setBackground(GRAPH_BACKGROUND);
    graphPanel.setDoubleBuffered(true);
    graphPanel.setOpaque(true);

    scrollPaneRight = new PScrollPane(graphPanel);
    graphPanel.setParent(scrollPaneRight);

    scrollPaneRight.setKeyActionsDisabled(true);
    //System.out.println("In CharmGUI initRightPanel() Just Before End");
    graphPanel.initializeGraph();
  }

  /**
   * Close the currently open dataset.
   * Not needed in MeV
   */
  /*
  public void closeCurrentDataset() {
    int returnVal = JOptionPane.showConfirmDialog(null,"Are you sure you want to close the current dataset?","Confirm Dataset Close", JOptionPane.OK_CANCEL_OPTION);
    if(returnVal == JOptionPane.OK_OPTION) {
  displayState.setCurrentDataset(null);
  displayState.clearSelectedExperiments();
  displayState.clearResultSets();
  displayState.clearSelectedResultSets();

  graphPanel.updateGraph();
  CharmGUI.this.updateExpOptions();
  CharmGUI.this.updatePredOptions();
  CharmGUI.this.updateViewResults();

  fileCloseButton.setEnabled(false);
  //jMenuCloseDataset.setEnabled(false);

  //jMenuAnalyzeData.setEnabled(false);
  analyzeDataButton.setEnabled(false);

  //jMenuExportResults.setEnabled(false);
  exportButton.setEnabled(false);

  //jMenuDisplaySettings.setEnabled(false);

  toggleRatiosButton.setEnabled(false);
  toggleRatiosButton.setSelected(false);
  togglePredictionsButton.setSelected(false);
  togglePredictionsButton.setEnabled(false);
  displayState.setStateVariable("Data Plot Toggle", "off");
  displayState.setStateVariable("Prediction Plot Toggle", "off");
  //DatasetContainer currDataset = displayState.getCurrentDataset();
  //currDataset = null;
  System.gc();
 }
}
*/


  /**
   * Updates the Experiment Options panel with currently selected experiments.
   */
  public void updateExpOptions() {
    expcheckboxes.removeAll();
    
    // Raktim Mar 11
    String[] experiment_list = (String[])displayState.getExperimentList().toArray(new String[0]);
    //String[] experiment_list = (String[])displayState.getSelectedExperiments().toArray(new String[0]);
    
    for (int i = 0; i < experiment_list.length; i++) {
      JCheckBox checkbox = new JCheckBox(experiment_list[i]);
      checkbox.addActionListener(expCheckboxListener);
      expcheckboxes.add(checkbox);
    }

    if (experiment_list.length > 0) {
      //expcheckboxes.setPreferredSize(new Dimension(LEFT_WIDTH, 25 * experiment_list.length));
      ( (JCheckBox) expcheckboxes.getComponent(0)).setSelected(true);
    }
    expscrollpane.setViewportView(expcheckboxes);
    graphPanel.resetZoom();
  }

  /**
   * Updates right display panel.
   */
  public void updategraphPanel() {
	  
    graphPanel.updateGraph();
  }

  /**
   * Updates View Results panel with current display state
   */
  public void updateViewResults() {
	 //System.out.println("CharmGUI.updateViewResults()");
    String graphSelectType = displayState.getStateVariable("Graph Selection Type");
    boolean zeroLength = false;

    if (graphSelectType.equals("gene")) {
    	//System.out.println("updateViewResults():GENE View");
      ArrayList selectedGenes = displayState.getGraphSelectionGenes();

      if (selectedGenes != null ) {
        if(selectedGenes.size() > 0) {
          String exp = displayState.getGraphSelectionExperiment();
          //System.out.println("updateViewResults():GENE View, Expr: " + exp);
          //genelist.setGenes(selectedGenes, exp);
          geneTable.setGenes(selectedGenes, exp);
          //Raktim Mar 13
          //this.exportGenesButton.setEnabled(true);
          jTabbedPane1.setSelectedIndex(0);
          //genelist.setSelectedIndex(0);
          //geneTable.set setSelectedIndex(0);
          //Raktim Mar 13
        }
      }

      //this.runSigTestButton.setEnabled(true);
    }
    else if (graphSelectType.equals("window")) {
    	//System.out.println("updateViewResults():WINDOW View");
      ArrayList segments = displayState.getGraphSelectionWindows();

      if (segments != null /*&& displayState.getCurrentDataset() != null*/) {
        String currExp = displayState.getGraphSelectionExperiment();
        //System.out.println("updateViewResults():WINDOW View, Expr: " + currExp);
        //Chromosome currChrom = displayState.getCurrentDataset().getChromosome(displayState.getGraphSelectionChromosome());
        int currChrom = displayState.getGraphSelectionChromosome();
        
        //Raktim Mar 12
        //statdisp.setWindows((SegmentInfo[])segments.toArray(new SegmentInfo[0]),currChrom,displayState.getGraphSelectionExperiment());
        
        /* Map NA Removed Segment Start/End Indices to actual Indices */
        int segOrigSt, segOrigEnd = -1;
        ArrayList indOrig = getOrigIndArray(currExp, currChrom-1);
      
        /* To find out size of CloneArray needed */
        int size = 0;
        for (int i = 0; i < segments.size(); i++) {
        	segOrigSt = lookupGeneIndex(indOrig,((SegmentInfo)segments.get(i)).getStart());
            segOrigEnd = lookupGeneIndex(indOrig,((SegmentInfo)segments.get(i)).getEnd());
        	size += segOrigEnd - segOrigSt + 1;
        }
        CGHClone[] windowgenes = new CGHClone[size];
        //ArrayList windowgenes = new ArrayList();
        
        //System.out.println("updateViewResults()");
        //System.out.println("	#Segs:#CLones:Exp:Chr:--" + segments.size()+":"+ size +":"+currExp+":"+currChrom);
        int j = 0;
        for (int i = 0; i < segments.size(); i++) {
          //getGenesBetweenIndices(currExp, int ind1, int ind2, boolean includeNaNs);
          //windowgenes.addAll(currChrom.getGenesBetweenIndices(currExp,((SegmentInfo)segments.get(i)).getStart(),((SegmentInfo)segments.get(i)).getEnd(),false));
        	CGHClone[] clones = null;
        	segOrigSt = lookupGeneIndex(indOrig,((SegmentInfo)segments.get(i)).getStart());
            segOrigEnd = lookupGeneIndex(indOrig,((SegmentInfo)segments.get(i)).getEnd());
        	clones = ((MultipleArrayData)data).getClonesWithinIndices(segOrigSt,segOrigEnd, currExp, currChrom);
        	/* Collect clones from all segments and copy over to big Clones array */
        	for(int k = 0; k < clones.length; j++, k++) {
        		windowgenes[j] = clones[k];
        	}
        }
        
        //genelist.setGenes(windowgenes, currExp);
        //genelist.setSelectedIndex(0);
        geneTable.setGenes(windowgenes, currExp);
        //this.exportGenesButton.setEnabled(true);
        jTabbedPane1.setSelectedIndex(0);
      }
    }
    else {
      //genelist.clearGenes();
    	geneTable.clearGenes();
//    Raktim Mar 12
      //statdisp.clearDisplay();
      //this.exportGenesButton.setEnabled(false);
      //this.runSigTestButton.setEnabled(false);
    }
  }

  /*
   * New function to be used in ChARM class - Raktim
   */
  public GraphViewPanel getGraphViewPanel() {
	  return graphPanel;
  }
  
  public JToggleButton getTogglePredictionsButton() {
	  return togglePredictionsButton;
  }
  
  public JTabbedPane getJTabbedPane1() {
	  return jTabbedPane1;
  }
  /**
   * Updates Prediction Options panel with current display state.
   * Not needed in MeV
   */
  /*
  public void updatePredOptions() {

    predcheckboxes.removeAll();

    ArrayList resultsList = displayState.getResultSets();
    //ResultContainer resContainer = displayState.getResultSets();

    for (int i = 0; i < resultsList.size(); i++) {
      JCheckBox checkbox = new JCheckBox(((ResultContainer)resultsList.get(i)).getResultID());
      checkbox.addActionListener(predCheckboxListener);
      checkbox.setAlignmentX(predcheckboxes.CENTER_ALIGNMENT);
      checkbox.setSelected(true);
      predcheckboxes.add(checkbox);
      displayState.addSelectedResultSet(((ResultContainer)resultsList.get(i)).getResultID());
    }

    if(resultsList.size() > 0) ((JCheckBox) predcheckboxes.getComponent(0)).setSelected(true);
    predcheckboxes.updateUI();
  }
  */
  
  /**
   *
   * <p>Title: GeneListListener</p>
   * <p>Description: This inner class listens for user selection in the
   * list of genes on the left panel and updates the display panel accordingly.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class GeneListListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
      //e.getLastIndex();
    	//TODO
    	//Not sure which func to use getGenes() or getSelectedGenes()
    	CGHClone[] selected = genelist.getSelectedGenes();
      if (selected != null ) {
    	  //Raktim Mar 12
    	  //if(displayState.getStateVariable("Graph Selection Type").equals("gene")) statdisp.setGenes(selected, genelist.getExperiment());
        graphPanel.addGeneCircles(displayState.getGraphSelectionChromosome(),displayState.getGraphSelectionExperiment(),selected);
      }
    }
  }
  
  private class GeneTableListener implements ListSelectionListener {

	    public void valueChanged(ListSelectionEvent e) {
	      //e.getLastIndex();
	    	//TODO
	    	//Not sure which func to use getGenes() or getSelectedGenes()
	    	CGHClone[] selected = geneTable.getSelectedGenes();
	      if (selected != null ) {
	    	  //Raktim Mar 12
	    	  //if(displayState.getStateVariable("Graph Selection Type").equals("gene")) statdisp.setGenes(selected, genelist.getExperiment());
	        graphPanel.addGeneCircles(displayState.getGraphSelectionChromosome(),displayState.getGraphSelectionExperiment(),selected);
	      }
	    }
	  }


  /**
   *
   * <p>Title: ExportGenesListener</p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class ExportGenesListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      JFileChooser fileDialog = new JFileChooser(TMEV.getDataPath());
      int returnVal = fileDialog.showSaveDialog(CharmGUI.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String file = fileDialog.getSelectedFile().getAbsolutePath();
        //genelist.writeGenesToFile(file + EXTENSION);
        geneTable.writeGenesToFile(file + EXTENSION);

//Raktim Mar 13
        /*
        if (statdisp.containsWindows()) {
          statdisp.writeWindowToFile(file + EXTENSION);
        }
        */
      }

    }
  }

  /**
   *
   * <p>Title: ChARMRunListener</p>
   * <p>Description: This class monitors progress of current ChARM runs.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  /*
  private class ChARMRunListener implements ActionListener {
	 
    private boolean startCounting = false;
    public void actionPerformed(ActionEvent e) {
    System.out.println("In ChARMRunListener");
      //ChARMRunner charmRunner = displayState.getChARMRunner();
      float percent = 0.0f;//getProgress(); //charmRunner.getProgress();

      if (charmProgressMonitor.isCanceled()) {
        //System.out.println("canceled percent progress = "+percent);
        //TODO
        //displayState.clearRunningResults();
        //charmProgressTimer.stop();
        //charmRunner.stopRun();
        //charmProgressMonitor.hide();
      }

      //set display to predictions
      else if (percent >= 1.0) {
        //System.out.println("done percent progress = "+percent);
        //charmProgressTimer.stop();   
        //displayState.removeRunningResult(charmRunner.getResultSet());
        //displayState.removeRunningResult(displayState.getSelectedResultSets());
        //ResultContainer currSet = displayState.getSelectedResultSets();
        //if(currSet == null) {
          //displayState.addResultSet(displayState.getSelectedResultSets());
          //currSet = charmRunner.getResultSet();
        //}
        ///else {
          //currSet.addResults(charmRunner.getResultSet());
        //}
        //displayState.addSelectedResultSet(currSet.getResultID());

        //displayState.setRunningExpsToDone();
        //graphPanel.initializePredictionNodes(displayState.getRunningExperiments());
        //graphPanel.initializePredictionNodes(displayState.getSelectedExperiments());

        //togglePredictionsButton.setEnabled(true);
        //togglePredictionsButton.setSelected(true);
        //displayState.setStateVariable("Prediction Plot Toggle","on");

        //charmProgressMonitor.hide();
        //CharmGUI.this.updatePredOptions();
        //jTabbedPane1.setSelectedIndex(2);
        //graphPanel.updateGraph();
      }
      else {
        //charmProgressMonitor.setProgress( (int) (100 * percent));
        //int timeToCompletion = (int)(elapsedTime/percent-elapsedTime);
        //if(percent > 0 ) {
          //charmProgressMonitor.setTimeToCompletion( (int) timeToCompletion / 1000);
          //startCounting = true;
        //}
      }

      //if(startCounting) elapsedTime += CharmGUI.this.TIMER_DELAY;
    }
}
*/
  /**
   * Progress tracking helper
   * TODO
   */
  //private float getProgress() {
	//  return numOfExprsDone/numOfExprsSelected;
	  //return 0.0f;
  //}
  /**
   *
   * <p>Title: GraphButtonListener</p>
   * <p>Description: This class responds to all display panel button events. </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class GraphButtonListener implements ActionListener {

   public void actionPerformed(ActionEvent e) {
     String command = e.getActionCommand();

     if(command.equals("Select")) {
       displayState.setGraphMode("Select");
       graphPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
       zoomInButton.setSelected(false);
       zoomOutButton.setSelected(false);
       selectButton.setSelected(true);
       //displayState.setGraphMode("Select");
     }

     if(command.equals("Zoom In")) {
       displayState.setGraphMode("Zoom In");
       graphPanel.setCursor(zoomInCursor);
       zoomOutButton.setSelected(false);
       selectButton.setSelected(false);
       zoomInButton.setSelected(true);
     }

     if(command.equals("Zoom Out")) {
       displayState.setGraphMode("Zoom Out");
       graphPanel.setCursor(zoomOutCursor);
       zoomInButton.setSelected(false);
       selectButton.setSelected(false);
       zoomOutButton.setSelected(true);
     }

     if(command.equals("Reset Zoom")) {
       //graphPanel.setPreferredSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
       graphPanel.resetZoom();
       graphPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
       selectButton.setSelected(true);
       zoomInButton.setSelected(false);
       zoomOutButton.setSelected(false);
       displayState.setGraphMode("Select");
     }

     //if(command.equals("Toggle Ratios")) {
     //  if(toggleRatiosButton.isSelected()) displayState.setStateVariable("Data Plot Toggle","on");
     //  else displayState.setStateVariable("Data Plot Toggle","off");
     //  graphPanel.updateGraph();
     //}

     if(command.equals("Toggle Predictions")) {
       if(togglePredictionsButton.isSelected()) displayState.setStateVariable("Prediction Plot Toggle","on");
       else displayState.setStateVariable("Prediction Plot Toggle","off");
       graphPanel.updateGraph();
     }
     
     if(command.equals("Export results")) {
         ExportResultsDialog exportResults = new ExportResultsDialog(CharmGUI.this,displayState);
         exportResults.setLocationRelativeTo(dummyPanel);
         exportResults.setModal(false);
         exportResults.setVisible(true);

         if(exportResults.getExitStatus() == ExportResultsDialog.OK_VERIFIED) {
           String filename = exportResults.getFilename();
           System.out.println(filename+","+exportResults.getExportType());
           if(exportResults.getExportType() == ExportResultsDialog.EXPORT_IMAGE) {

           }
           else if(exportResults.getExportType() == ExportResultsDialog.EXPORT_FLAT_FILE) {
             int printStatus = displayState.printVisiblePredictionWindows(filename+CharmGUI.EXTENSION);
             if(printStatus < 0) JOptionPane.showMessageDialog(null,"Error writing to file!  Please try again.","File Output Error", JOptionPane.ERROR_MESSAGE);
           }
         }
     }

   }
 }

 /**
  *
  * <p>Title: PermuteButtonListener</p>
  * <p>Description: This class responds to user requests to check the significance
  * of manually-selected predictions.</p>
  * <p>Copyright: Copyright (c) 2004</p>
  * <p>Company: Princeton University</p>
  * @author Chad Myers, Xing Chen
  * @version 1.4
  */
  private class PermuteButtonListener implements ActionListener {

    JCheckBox mean_check;
    JCheckBox rank_check;
    JCheckBox sign_check;
    JCheckBox coeff_check;

    public void actionPerformed(ActionEvent e) {

     if(displayState.getGraphSelectionChromosome() > 0) {
      //Chromosome currChrom = displayState.getCurrentDataset().getChromosome(displayState.getGraphSelectionChromosome());
      int currChrom = displayState.getGraphSelectionChromosome();
      String currExp = displayState.getGraphSelectionExperiment();
      ArrayList geneList = displayState.getGraphSelectionGenes();

      SegmentInfo currSeg = new SegmentInfo();

      if(geneList.size() > 0) {
        //currSeg.setStart(currChrom.mapRealIndexToExcludeNaNIndex(currExp,currChrom.getGeneIndex((Gene)geneList.get(0))));
    	currSeg.setStart(((CGHClone)geneList.get(0)).getSortedIndex());
        //currSeg.setEnd(currChrom.mapRealIndexToExcludeNaNIndex(currExp,currChrom.getGeneIndex((Gene)geneList.get(geneList.size()-1))));
    	currSeg.setEnd(((CGHClone)geneList.get(geneList.size()-1)).getSortedIndex());
        currSeg.setType(SegmentInfo.TYPE_MANUAL);

        TestSignificanceDialog testSigDialog = new TestSignificanceDialog(displayState,currSeg,currChrom,currExp);
        testSigDialog.setLocationRelativeTo(dummyPanel);
        testSigDialog.setModal(true);
        testSigDialog.show();

        int returnStatus = testSigDialog.getExitStatus();
        if(returnStatus != TestSignificanceDialog.EXIT_CANCELLED) {
          graphPanel.updateGraph();
          togglePredictionsButton.setEnabled(true);
          togglePredictionsButton.setSelected(true);
          displayState.setStateVariable("Prediction Plot Toggle","on");

          ArrayList exps = new ArrayList();
          exps.add(currExp);
          //displayState.setRunningExperiments(exps);
          //displayState.setRunningExpsToDone();
          graphPanel.initializePredictionNodes(displayState.getSelectedExperiments());
          //CharmGUI.this.updatePredOptions();
          graphPanel.updateGraph();
        }

      }
    }

    }
  }


  /**
   *
   * <p>Title: CutoffSelectionListener</p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class CutoffSelectionListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      int index = cutoff_box.getSelectedIndex();
      if(index == 2) { pvalue_fields[0].setEnabled(true); pvalue_fields[1].setEnabled(false);}
      else if(index == 3)  { pvalue_fields[1].setEnabled(true); pvalue_fields[0].setEnabled(false);}
      else {
        pvalue_fields[0].setEnabled(true);
        pvalue_fields[1].setEnabled(true);
      }
    }
  }


  /**
   *
   * <p>Title: CutoffListener</p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class CutoffListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {

      try{
        Double signCutoff = Double.valueOf(pvalue_fields[0].getText());
        Double meanCutoff = Double.valueOf(pvalue_fields[1].getText());

        PValue filterPval = new PValue(meanCutoff.doubleValue(),
                                       signCutoff.doubleValue());
        cutoff_box.validate();
        int index = cutoff_box.getSelectedIndex();
        int testType = 0;
        switch (index) {
          case 0:
            testType = PValue.MEAN_AND_SIGN_TEST;
            break;
          case 1:
            testType = PValue.MEAN_OR_SIGN_TEST;
            break;
          case 2:
            testType = PValue.SIGN_TEST;
            break;
          case 3:
            testType = PValue.MEAN_TEST;
            break;
        }

        displayState.setPValueCutoff(filterPval);
        displayState.setPValueTestType(testType);
        graphPanel.initializePredictionNodes(displayState.getSelectedExperiments());
        graphPanel.updateGraph();
      }
      catch(java.lang.NumberFormatException exc) {
        JOptionPane.showMessageDialog(CharmGUI.this,"Please enter P-values between 0 and 1.","Error",JOptionPane.ERROR_MESSAGE);

      }
    }
}


  /**
   *
   * <p>Title: ExpOptionsCheckboxListener</p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  
  
   //Not Needed in MeV
  private class ExpOptionsCheckboxListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      
//    	 Raktim Mar 11
    	String[] experiment_list = (String[])displayState.getExperimentList().toArray(new String[1]);
      //String[] experiment_list = (String[])displayState.getSelectedExperiments().toArray(new String[1]);
      for (int i = 0; i < experiment_list.length; i++) {
        JCheckBox curr = (JCheckBox) expcheckboxes.getComponent(i);
        if (e.getSource() == curr) {
          if (curr.isSelected()) {
            displayState.addSelectedExperiment(experiment_list[i]);
            }
          else {
            displayState.removeSelectedExperiment(experiment_list[i]);
          }
        }
      }

      updategraphPanel();
    }

  }
  

  /**
   *
   * <p>Title: PredictionOptionsCheckboxListener</p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  // NOt needed in MeV
  /*
  private class PredictionOptionsCheckboxListener
      implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      ArrayList resultList  = displayState.getResultSets();
      for (int i = 0; i < resultList.size(); i++) {
        JCheckBox curr = (JCheckBox) predcheckboxes.getComponent(i);
        if (e.getSource() == curr) {
          if (curr.isSelected()) {
            displayState.addSelectedResultSet(((ResultContainer)resultList.get(i)).getResultID());
            }
          else {
            displayState.removeSelectedResultSet(((ResultContainer)resultList.get(i)).getResultID());
          }
        }
      }

      updategraphPanel();
    }

  }
  */
  /**
   *
   * <p>Title: MenuListener</p>
   * <p>Description: This class responds to all user menu choices.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
  private class MenuListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      if(command.equals("Exit")) {
        int returnVal = JOptionPane.showConfirmDialog(null,"Are you sure you want to quit?","Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
        if(returnVal == JOptionPane.OK_OPTION) System.exit(0);
      }
      /*
       * Will not be needed in Mev
       */
      /*
      else if(command.equals("About")) {
        //MainFrame_AboutBox dlg = new MainFrame_AboutBox(MainFrame.this);
    	MainFrame_AboutBox dlg = new MainFrame_AboutBox(new JFrame());
    	Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                         (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.show();
      }

      else if(command.equals("Display Settings")) {
        DisplaySettingsDialog displaySettings = new DisplaySettingsDialog(displayState);
        displaySettings.setLocationRelativeTo(dummyPanel);
        displaySettings.setModal(true);
        displaySettings.pack();
        displaySettings.setVisible(true);

      }

      else if(command.equals("Load dataset")) {
          if(displayState.getCurrentDataset() != null) {
        	  CharmGUI.this.closeCurrentDataset();
          }

          LoadDatasetDialog loadDataset = new LoadDatasetDialog(CharmGUI.this);
          loadDataset.setLocationRelativeTo(dummyPanel);
          loadDataset.setModal(true);

          loadDataset.setVisible(true);
          if(loadDataset.getExitStatus() == loadDataset.OK_VERIFIED) {
            String organism = loadDataset.getOrganism();
            String setName = loadDataset.getDatasetName();
            String filename = loadDataset.getFilename();

            boolean loadStatus = true;
            try{displayState.loadDataset(filename,organism,setName);}
            catch(Exception exc) { exc.printStackTrace();
                     JOptionPane.showMessageDialog(null,"Error reading: "+filename+". Please check format and try again.","Error", JOptionPane.ERROR_MESSAGE);
                     loadStatus = false;
            }

            if(loadStatus) {  statusText.setText(setName + " loaded  ");}
            else { statusText.setText("No datasets loaded.  ");}

             ArrayList allExps = displayState.getExperimentList();
             displayState.addSelectedExperiments(new ArrayList(allExps.subList(0,1)));

             updateExpOptions();
             jTabbedPane1.setSelectedIndex(1);

             toggleRatiosButton.setEnabled(true);
             toggleRatiosButton.setSelected(true);
             displayState.setStateVariable("Data Plot Toggle","on");

             fileCloseButton.setEnabled(true);
             //jMenuCloseDataset.setEnabled(true);

             //jMenuAnalyzeData.setEnabled(true);
             analyzeDataButton.setEnabled(true);

             //jMenuExportResults.setEnabled(true);
             exportButton.setEnabled(true);

             //jMenuDisplaySettings.setEnabled(true);

             graphPanel.initializeGraph();
             graphPanel.updateGraph();
             graphPanel.resetZoom();
          }
        }


      else if(command.equals("Close dataset")) {
    	  CharmGUI.this.closeCurrentDataset();
      }
	  */
      else if(command.equals("Analyze data")) {
    	 /* Transfer the Progress Monitor to ChARM 
    	 
         AnalyzeDataDialog analyzeData = new AnalyzeDataDialog(displayState,CharmGUI.this);
         analyzeData.setLocationRelativeTo(dummyPanel);

         analyzeData.setVisible(true);
         if(analyzeData.getExitStatus() == analyzeData.OK_VERIFIED) {
           ArrayList selectedExps = analyzeData.getSelectedExperiments();
           String analysisID = analyzeData.getAnalysisID();

           ArrayList chromList = displayState.getCurrentDataset().getChromosomes();
           ArrayList chromExpList = new ArrayList();

           for(int i=0; i < selectedExps.size(); i++) {
             for (int j=0; j < chromList.size(); j++) {
               ArrayList currRun = new ArrayList();
               currRun.add(0,(Chromosome)chromList.get(j));
               currRun.add(1,(String)selectedExps.get(i));
               chromExpList.add(currRun);
             }
           }
			
		  
           ChARMRunner charmRunner = new ChARMRunner(displayState.getCurrentDataset());
           displayState.setChARMRunner(charmRunner);
		   

           charmProgressMonitor = new ChARMProgressDialog();
           charmProgressMonitor.setProgress(0);
           charmProgressMonitor.setModal(false);
           charmProgressMonitor.setLocationRelativeTo(dummyPanel);
           charmProgressMonitor.setVisible(true);
           
           
           //Temp Comment 3 -lines. Need to be moved completely.
           // * The algo is executed from ChARM.java
           //
           //ResultContainer results = charmRunner.runCharm(chromExpList,true);
           //results.setResultID(analysisID);
           //displayState.addRunningResult(results);
           //displayState.setRunningExperiments(selectedExps);

           charmProgressTimer = new javax.swing.Timer(CharmGUI.this.TIMER_DELAY, new ChARMRunListener());
           charmProgressTimer.setDelay(CharmGUI.this.TIMER_DELAY);

           elapsedTime=0;
           charmProgressTimer.start();
         }*/

      }

      else if(command.equals("Export results")) {
          ExportResultsDialog exportResults = new ExportResultsDialog(CharmGUI.this,displayState);
          exportResults.setLocationRelativeTo(dummyPanel);
          exportResults.setModal(false);
          exportResults.setVisible(true);

          if(exportResults.getExitStatus() == ExportResultsDialog.OK_VERIFIED) {
            String filename = exportResults.getFilename();
            System.out.println(filename+","+exportResults.getExportType());
            if(exportResults.getExportType() == ExportResultsDialog.EXPORT_IMAGE) {

            }
            else if(exportResults.getExportType() == ExportResultsDialog.EXPORT_FLAT_FILE) {
              int printStatus = displayState.printVisiblePredictionWindows(filename+CharmGUI.EXTENSION);
              if(printStatus < 0) JOptionPane.showMessageDialog(null,"Error writing to file!  Please try again.","File Output Error", JOptionPane.ERROR_MESSAGE);
            }
          }
      }
      /*
       * This will not be needed in MeV
       */
      /*
      else if(command.equals("View help") ) {
        //HelpDialog helpDialog = new HelpDialog(MainFrame.this);
    	HelpDialog helpDialog = new HelpDialog(new JFrame());
        helpDialog.setLocationRelativeTo(dummyPanel);
        helpDialog.setModal(false);
        helpDialog.show();

      }
      */
      else { }
    }
  }
 
  /**
   * IViewer Methods
   */
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		return this;
		//return contentPane;
	}
	
	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return this.exptID;
	}
	
	public Expression getExpression() {
		//return null;
		
		return new Expression(this, this.getClass(), "new", 
				new Object[]{this.data, this.displayState, this.analyzedExprIndices });
		
		/*		new Object[]{this.experiment, this.createDefaultFeatures(this.experiment), 
			this.genes_result, this.samples_result, this.sampleClusters, 
			new Boolean(this.isExperimentCluster), this.genesTree, this.sampleTree, 
			new Integer(this.offset), (ExperimentViewer)this.expViewer});
			*/
	}
	
	public JComponent getHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getViewerType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void onClosed() {
		// TODO Auto-generated method stub
		
	}
	
	public void onDataChanged(IData data) {
		// TODO Auto-generated method stub
		
	}
	
	public void onDeselected() {
		// TODO Auto-generated method stub
		
	}
	
	public void onMenuChanged(IDisplayMenu menu) {
		// TODO Auto-generated method stub
		
	}
	
	public void onSelected(IFramework framework) {
		// TODO Auto-generated method stub
		updategraphPanel();
		//graphPanel.updateGraph();
	}
	
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub
		
	}
	
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub
		this.exptID = id;
		
	}

	/* CGH Helper Methods, Raktim */
	/**
	  * CGH ChARM function, Raktim
	  * Array to Map NA Remomed Indices of Genes to Real Indices
	  */
 	public ArrayList getOrigIndArray(String currExp, int chrInd) {
	  
 		ArrayList featuresList = data.getFeaturesList();
		int exprInd = -1;
		
		for (int column = 0; column < featuresList.size(); column++){
			String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
			if(name.trim().equals(currExp.trim())) {
				exprInd = column;
				break;
			}
			//System.out.println("exprNames " + name);
		}
		
	  	int[][] chrIndices = data.getChromosomeIndices();
		int st = chrIndices[chrInd][0];
	 	int end = chrIndices[chrInd][1];
	 	
	 	ArrayList ratioIndices = new ArrayList();
	   	//int ind = 0;
	   	for(int ii = st; ii < end; ii++) {
	   		float tmp = data.getLogAverageInvertedValue(exprInd, ii);
	   		if (!Float.isNaN(tmp)) {
	   			ratioIndices.add(new Integer(ii-st));
	   			//ind++;
	   		}
	   	}
	   	return ratioIndices;
 	}
 	
 	/**
 	 * CGH ChARM Function Raktim
 	 * TO match NA removed incdices to Original Indices of an Experiment & Chr
 	 */
 	private int lookupGeneIndex(ArrayList indices, int NAIndex) {
 		return ((Integer)indices.get(NAIndex)).intValue();
 	}
 }


