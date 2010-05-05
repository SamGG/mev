/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TerrainViewer.java,v $
 * $Revision: 1.11 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.Billboard;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.Node;
import javax.media.j3d.PickCone;
import javax.media.j3d.PointLight;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.vecmath.Color3f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ListDialog;
import org.tigr.microarray.mev.cluster.gui.impl.util.IntArray;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class TerrainViewer extends JPanel implements IViewer {


    private IData data;
    private IFramework framework;
    private int labelIndex = -1;

    private boolean isGenes = true;
    private int[][] clusters;
    private float[][] weights;
    private float[][] locations;
    private float sigma;
    private int grid_size = 65;
    private boolean isBillboard = false;

    private final Color3f BLACK_COLOR = new Color3f(0f, 0f, 0f);
    private final Color3f WHITE_COLOR = new Color3f(1f, 1f, 1f);
    private static final BoundingSphere BILLBOARD_BOUNDS = new BoundingSphere(new Point3d(), 100d);
    private static final Point3f BILLBOARD_P3F = new Point3f(-0.008f, 0.0008f, 0);

    // menu commands
    private static final String FILL_POLYGON_CMD  = "fill-polygone-cmd";
    private static final String ZOOM_IN_CMD       = "zoom-in-cmd";
    private static final String UNDO_CMD          = "undo-cmd";
    private static final String SHOW_ALL_CMD      = "show-all-cmd";
    private static final String POINTS_SHAPE_CMD  = "points-shape-cmd";
    private static final String CUBES_SHAPE_CMD   = "cubes-shape-cmd";
    private static final String SPHERES_SHAPE_CMD = "spheres-shape-cmd";
    private static final String GRID_SIZE_CMD     = "grid-size-cmd";
    private static final String SHOW_ELEMENTS_CMD = "show-elements-cmd";
    private static final String DESELECT_CMD      = "deselect-cmd";
    private static final String SHOW_CONTROLS_CMD = "show-controls-cmd";
    private static final String DRIFT_DIALOG_CMD  = "drift-dialog-cmd";
    private static final String LAUNCH_SESSION_CMD = "launch-session-cmd";
    private static final String SET_CLUSTER_CMD   = "set-cluster-cmd";
    private static final String SHOW_LINKS_CMD    = "show-links-cmd";
    private static final String LINKS_THRESHOLD_CMD  = "links-threshold-cmd";
    private static final String LINKS_WIDTH_CMD   = "links-width-cmd";
    private static final String HIDE_LABELS_CMD   = "hide-labels-cmd";
    private static final String USE_BILLBOARD_CMD = "use-billboard-cmd";
    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    private JPopupMenu popup;
    private JWindow tipWindow;
    private ControlPanel controlPanel;

    // 3D scene and its elements
    private SimpleUniverse universe;
    private Canvas3D onScreenCanvas;
    private Canvas3D offScreenCanvas;
    private TransformGroup view_tg;
    private GenesShape genesShape;
    private BranchGroup sceneGroup;
    private Landscape landscape;
    private LinksShape linksShape;
    private SelectionShape selectionShape;
    private BranchGroup labelsGroup;
    private KeyMotionBehavior keyMotionBehavior;
    private DriftInterpolator driftInterpolator; 
    private PickBehavior pickBehavior;
    // zooming variables
    private Point2f up_left_point  = new Point2f(0f, 0f);
    private Point2f bottom_right_point = new Point2f(1f, 1f);
    // undo zoom operation support
    private UndoManager undoManager = new UndoManager();
    private Experiment experiment;
    private int exptID = 0;
    private static boolean enabled3D = true;

    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{new Boolean(isGenes), this.experiment, this.clusters, this.weights, this.locations, new Float(this.sigma), new Integer(this.labelIndex)});
    }
    public TerrainViewer(Boolean isGenes, Experiment experiment, int[][] clusters, float[][] weights, float[][] locations, Float sigma, Integer labelIndex) {
    	this(isGenes.booleanValue(), experiment, clusters, weights, locations, sigma.floatValue(), labelIndex.intValue());
    }
    public TerrainViewer(boolean isGenes, Experiment experiment, int[][] clusters, float[][] weights, float[][] locations, float sigma, int labelIndex) {
        this.isGenes = isGenes;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters;
        this.weights = weights;
        this.locations = locations;
        this.sigma = sigma;
        setPreferredSize(new Dimension(10, 10));
        Listener listener = new Listener();
        // create the universe
        if(enabled3D) {
	        try {
		        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		        this.onScreenCanvas = new Canvas3D(config);
		        this.universe = new SimpleUniverse(this.onScreenCanvas);
		
		        this.offScreenCanvas = new Canvas3D(config, true);
		        Screen3D sOn = onScreenCanvas.getScreen3D();
		        Screen3D sOff = offScreenCanvas.getScreen3D();
		        sOff.setSize(sOn.getSize());
		        sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
		        sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());
		        // attach the offscreen canvas to the view
		        this.universe.getViewer().getView().addCanvas3D(this.offScreenCanvas);
		        // set its bounds
		        BoundingLeaf boundingLeaf = new BoundingLeaf(new BoundingSphere(new Point3d(), 100d));
		        boundingLeaf.setCapability(BoundingLeaf.ALLOW_REGION_READ);
		        PlatformGeometry platformGeometry = new PlatformGeometry();
		        platformGeometry.addChild(boundingLeaf);
		        platformGeometry.compile();
		        this.universe.getViewingPlatform().setPlatformGeometry(platformGeometry);
		        // set distances
		        this.universe.getViewer().getView().setFrontClipDistance(0.001);
		        this.universe.getViewer().getView().setBackClipDistance(0.5);
		        // basis point
		        Point3d basis = new Point3d(0.5, 0, 0.5);
		        this.view_tg = universe.getViewingPlatform().getViewPlatformTransform();
		        // set initilal view point
		        setInitialViewPoint(view_tg, basis);
		        // drifting
		        this.driftInterpolator = new DriftInterpolator(this.view_tg, boundingLeaf);
		        // create heights
		        float[][] heights = DomainUtil.getHeights(this.locations, this.grid_size, this.sigma);
		        // selection shape
		        this.selectionShape = new SelectionShape();
		        // the landscape
		        this.landscape = new Landscape(heights);
		        this.landscape.setPoligonMode(PolygonAttributes.POLYGON_FILL);
		        TransformGroup landTransform = new TransformGroup();
		        landTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		        landTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		        landTransform.addChild(this.landscape);
		        // links shape
		        this.linksShape = new LinksShape(clusters, weights, locations);
		        // keyboard support
		        this.keyMotionBehavior = createKeyMotionBehavior(this.view_tg, basis, boundingLeaf);
		        // control panel
		        this.controlPanel = new ControlPanel(landTransform, this.keyMotionBehavior, boundingLeaf);
		        //this.controlPanel.setVisible(false);
		        Behavior sliderBehavior = this.controlPanel.getSliderBehavior();
		        // add gene shapes
		        this.genesShape = new GenesShape(GenesShape.POINTS, this.locations, this.up_left_point, this.bottom_right_point);
		        this.genesShape.setBounds(boundingLeaf.getRegion());
		        // add labels
		        this.labelIndex = labelIndex;
		        // create scene
		        Node[] nodes = new Node[] {this.selectionShape, landTransform, sliderBehavior, this.keyMotionBehavior, this.driftInterpolator, this.genesShape, this.linksShape};
		        this.sceneGroup = createSceneGraph(nodes, boundingLeaf);
		
		        this.pickBehavior = new PickBehavior(this.sceneGroup, this.onScreenCanvas, boundingLeaf.getRegion());
		        this.pickBehavior.setPickListener(listener);
		        this.sceneGroup.addChild(this.pickBehavior);
		
		        this.sceneGroup.compile();
		        // add the canvas to this panel
		        setLayout(new BorderLayout());
		        add(this.onScreenCanvas, BorderLayout.CENTER);
		        // control panel
		        add(this.controlPanel, BorderLayout.SOUTH);
		
		        this.popup = createJPopupMenu(listener);
		        this.onScreenCanvas.addMouseListener(listener);
		        this.onScreenCanvas.addMouseMotionListener(listener);
		        this.onScreenCanvas.addKeyListener(listener);
		        enabled3D = true;
	        } catch (UnsatisfiedLinkError ule) {
	        	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
		        enabled3D = false;
		        add(getJ3DErrorPlaceholderContent());
	        } catch (java.lang.NoClassDefFoundError ncdfe) {
	        	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
		        enabled3D = false;
		        add(getJ3DErrorPlaceholderContent());
	        }
        } else {
	        add(getJ3DErrorPlaceholderContent());
        }
    }
    private JTextArea getJ3DErrorPlaceholderContent() {
        JTextArea area = new JTextArea(20, 20);
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));

        area.setText("No 3D viewer is available. To view the results of this analysis, please install Java3D, available at java.sun.com. \n" +
        		"Use the File -> Save Analysis As option to save your results. \n" +
        		"After installing Java3D, restart MeV and load the saved analysis file to view these results in an interactive form. \n");
        area.setCaretPosition(0);
        return area;
    }

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;   
    }

    // IViewer implementation
    public JComponent getContentComponent() {
        return this;
    }

    public JComponent getHeaderComponent() {
        return null;
    }

    public void onSelected(IFramework framework) {
    	if(enabled3D) {
	        this.framework = framework;
	        if(this.tipWindow == null)
	            this.tipWindow = new JWindow(framework.getFrame());
	
	        this.universe.addBranchGraph(this.sceneGroup);
	        this.data = framework.getData();
	        onDataChanged(this.data);
	        onMenuChanged(framework.getDisplayMenu());
    	}
    }

    public void onDataChanged(IData data) {
        this.data = data;
        if (isGenes)
            this.genesShape.updateColors(this.data.getColorIndices(), this.data.getColors());
        else
            this.genesShape.updateColors(this.data.getExperimentColorIndices(), this.data.getExperimentColors());

    }

    public void onMenuChanged(IDisplayMenu menu) {
        if (this.labelIndex == menu.getLabelIndex())
            return;
        this.labelIndex = menu.getLabelIndex();
        updateLabelsGroup();
    }

    public void onDeselected() {
    	if(enabled3D){
    		this.sceneGroup.detach();
    	}
    }

    public void onClosed() {
    }

    public BufferedImage getImage() {
        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, new BufferedImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR));
        offScreenCanvas.setOffScreenLocation(onScreenCanvas.getLocationOnScreen());
        offScreenCanvas.setOffScreenBuffer(buffer);
        offScreenCanvas.renderOffScreenBuffer();
        offScreenCanvas.waitForOffScreenRendering();
        BufferedImage offImage = offScreenCanvas.getOffScreenBuffer().getImage();
        BufferedImage image = new BufferedImage(offImage.getWidth(), offImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        image.setData(offImage.getData());
        return image;
    }
    // end IViewer implementation

    private void setInitialViewPoint(TransformGroup tg, Point3d basis) {
        Transform3D t3d = new Transform3D();
        t3d.lookAt(new Point3d(1.42, 1.0, 1.42), basis, new Vector3d(0, 1, 0));
        t3d.invert();
        tg.setTransform(t3d);
    }

    private BranchGroup createSceneGraph(Node[] nodes, BoundingLeaf boundingLeaf) {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        objRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        objRoot.setCapability(Group.ALLOW_CHILDREN_WRITE);

        Background bg = new Background(BLACK_COLOR);
        bg.setApplicationBoundingLeaf(boundingLeaf);

        objRoot.addChild(bg);
        objRoot.addChild(createAmbientLight(boundingLeaf));
        objRoot.addChild(createPointLight(new Point3f(1.5f, 0.5f, 0.5f), boundingLeaf));
        objRoot.addChild(createPointLight(new Point3f(0.5f, 0.5f, 1.5f), boundingLeaf));

        for (int i=0; i<nodes.length; i++)
            objRoot.addChild(nodes[i]);

        return objRoot;
    }

    private TransformGroup createLight(Bounds bounds, Vector3d vector) {
        Transform3D t = new Transform3D();
        t.set(vector);
        TransformGroup lightGroup = new TransformGroup(t);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        lightGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        ColoringAttributes attr = new ColoringAttributes();
        Color3f color = new Color3f(1f, 1f, 1f);
        attr.setColor(color);
        Appearance appearance = new Appearance();
        appearance.setColoringAttributes(attr);
        lightGroup.addChild(new Sphere(0.01f, Sphere.GENERATE_NORMALS, 15, appearance));
        Light light = new PointLight(color, new Point3f(0.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f));
        light.setInfluencingBounds(bounds);
        lightGroup.addChild(light);
        return lightGroup;
    }

    private Billboard createLabelBillboard(TransformGroup label_tg) {
        label_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Billboard billboard = new TerrainBillboard(label_tg, Billboard.ROTATE_ABOUT_POINT, BILLBOARD_P3F, this.view_tg);
        billboard.setSchedulingBounds(BILLBOARD_BOUNDS);
        return billboard;
    }

    private BranchGroup createLabelsGroup(int labelIndex, float[][] locations, Point2f start, Point2f end) {
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);

        BranchGroup labels = new BranchGroup(); 
        labels.setCapability(BranchGroup.ALLOW_DETACH);
        if (this.isGenes && labelIndex < 0)
            return labels;

        Transform3D transform = new Transform3D();
        Vector3f vector3f = new Vector3f();
        TransformGroup position_tg;
        TransformGroup label_tg;
        float scale = Math.max(Math.abs(start.x-end.x), Math.abs(start.y-end.y));
        for (int i=0; i<locations.length; i++) {
            float x = locations[i][0];
            float y = locations[i][1];
            if (x >= start.x && x <= start.x+scale && y >= start.y && y <= start.y+scale) {
                vector3f.set((x-start.x)/scale+0.008f, -0.008f, (y-start.y)/scale);
                transform.set(vector3f);
                position_tg = new TransformGroup(transform);
                label_tg = new TransformGroup();
                String label = this.isGenes ? this.data.getElementAttribute(i, labelIndex) : this.data.getSampleName(i); 
                label_tg.addChild(createText2D(label, WHITE_COLOR, pa));
                position_tg.addChild(label_tg);
                labels.addChild(position_tg);
                if (this.isBillboard) {
                    Billboard billboard = createLabelBillboard(label_tg);
                    labels.addChild(billboard);
                }
            }
        }
        labels.compile();
        return labels;
    }

    private BranchGroup createViewBehavior(TransformGroup tg, Point3d basis, BoundingLeaf boundingLeaf) {
        BranchGroup behRoot = new BranchGroup();
        behRoot.setCapability(BranchGroup.ALLOW_DETACH);
        behRoot.addChild(createKeyMotionBehavior(tg, basis, boundingLeaf));
        return behRoot;
    }

    private KeyMotionBehavior createKeyMotionBehavior(TransformGroup tg, Point3d basis, BoundingLeaf boundingLeaf) {
        KeyMotionBehavior key = new KeyMotionBehavior(tg);
        key.setSchedulingBoundingLeaf(boundingLeaf);
        key.setBasis(basis);
        return key;
    }

    private AmbientLight createAmbientLight(BoundingLeaf boundingLeaf) {
        AmbientLight light = new AmbientLight();
        light.setInfluencingBoundingLeaf(boundingLeaf);
        return light;
    }

    private Light createDirectionLight(BoundingLeaf boundingLeaf) {
        DirectionalLight light = new DirectionalLight(WHITE_COLOR, new Vector3f(-1, 0, -1));
        light.setInfluencingBoundingLeaf(boundingLeaf);
        return light;
    }

    private PointLight createPointLight(Point3f position, BoundingLeaf boundingLeaf) {
        PointLight light = new PointLight(WHITE_COLOR, position, new Point3f(1f, 0f, 0f));
        light.setInfluencingBoundingLeaf(boundingLeaf);
        return light;
    }

    private Node createTransformGroup(Transform3D transform, Node node) {
        TransformGroup tg = new TransformGroup(transform);
        tg.addChild(node);
        return tg;
    }

    private Text2D createText2D(String text, Color3f color, PolygonAttributes pa) {
        Text2D text2D = new Text2D("", color, "Arial", 12, Font.BOLD);
        text2D.setRectangleScaleFactor(0.001f);
        text2D.setString(text);
        text2D.getAppearance().setPolygonAttributes(pa);
        text2D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        text2D.getGeometry().setCapability(Geometry.ALLOW_INTERSECT);
        text2D.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
        text2D.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
        text2D.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        return text2D;
    }

    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JCheckBoxMenuItem("Control Panel");
        menuItem.setSelected(true);
        menuItem.setActionCommand(SHOW_CONTROLS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menu.addSeparator();
        
        menuItem = new JMenuItem("Grid...");
        menuItem.setActionCommand(GRID_SIZE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("Fill Polygon");
        menuItem.setSelected(true);
        menuItem.setActionCommand(FILL_POLYGON_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
     
        menu.addSeparator();
        JMenu shape_menu = new JMenu("Element Shape");
        ButtonGroup buttonGroup = new ButtonGroup();

        menuItem = new JRadioButtonMenuItem("Point");
        menuItem.setSelected(true);
        menuItem.setActionCommand(POINTS_SHAPE_CMD);
        menuItem.addActionListener(listener);
        buttonGroup.add(menuItem);
        shape_menu.add(menuItem);

        menuItem = new JRadioButtonMenuItem("Cube");
        menuItem.setActionCommand(CUBES_SHAPE_CMD);
        menuItem.addActionListener(listener);
        buttonGroup.add(menuItem);
        shape_menu.add(menuItem);

        menuItem = new JRadioButtonMenuItem("Sphere");
        menuItem.setActionCommand(SPHERES_SHAPE_CMD);
        menuItem.addActionListener(listener);
        buttonGroup.add(menuItem);
        shape_menu.add(menuItem);

        menu.add(shape_menu);
        
        JMenu labels_menu = new JMenu("Labels");
        menuItem = new JCheckBoxMenuItem("Hide");
        menuItem.setSelected(true);
        menuItem.setActionCommand(HIDE_LABELS_CMD);
        menuItem.addActionListener(listener);
        labels_menu.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Billboard");
        menuItem.setSelected(false);
        menuItem.setActionCommand(USE_BILLBOARD_CMD);
        menuItem.addActionListener(listener);
        labels_menu.add(menuItem);

        menu.add(labels_menu);
 
        menu.addSeparator();
        
        JMenu zoom_menu = new JMenu("Zoom");

        menuItem = new JMenuItem("In");
        menuItem.setActionCommand(ZOOM_IN_CMD);
        menuItem.addActionListener(listener);
        zoom_menu.add(menuItem);

        menuItem = new JMenuItem("Undo");
        menuItem.setActionCommand(UNDO_CMD);
        menuItem.addActionListener(listener);
        zoom_menu.add(menuItem);

        menuItem = new JMenuItem("Show All");
        menuItem.setActionCommand(SHOW_ALL_CMD);
        menuItem.addActionListener(listener);
        zoom_menu.add(menuItem);

        menu.add(zoom_menu);
        
        menu.addSeparator();
        
        JMenu links_menu = new JMenu("Links");

        menuItem = new JCheckBoxMenuItem("Show");
        menuItem.setSelected(false);
        menuItem.setActionCommand(SHOW_LINKS_CMD);
        menuItem.addActionListener(listener);
        links_menu.add(menuItem);

        menuItem = new JMenuItem("Threshold...");
        menuItem.setActionCommand(LINKS_THRESHOLD_CMD);
        menuItem.addActionListener(listener);
        links_menu.add(menuItem);

        menuItem = new JMenuItem("Thickness...");
        menuItem.setActionCommand(LINKS_WIDTH_CMD);
        menuItem.addActionListener(listener);
        links_menu.add(menuItem);

        menu.add(links_menu);
                
        menu.addSeparator();

        menuItem = new JMenuItem("Store cluster");
        menuItem.setActionCommand(SET_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Launch New Session");
        menuItem.setActionCommand(LAUNCH_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);       
        
        menuItem = new JMenuItem("Deselect");
        menuItem.setActionCommand(DESELECT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Show Elements");
        menuItem.setActionCommand(SHOW_ELEMENTS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Drift...");
        menuItem.setActionCommand(DRIFT_DIALOG_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        

        return menu;
    }

    /**
     * Returns a menu item by specified action command.
     */
    private JMenuItem getJMenuItem(JPopupMenu menu, String command) {
        Component[] components = menu.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenu) {
                JMenuItem item = getJMenuItem(((JMenu)components[i]).getPopupMenu(), command);
                if (item != null)
                    return item;
            }
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }

    /**
     * Sets a menu item state.
     */
    private void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(this.popup, command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }

    private void onPicked(MouseEvent event, PickCanvas canvas) {
        canvas.setShapeLocation(event);
        PickResult pickResult = canvas.pickClosest();
        if (pickResult != null && pickResult.getObject() == this.genesShape) {
            PickIntersection pi = pickResult.getClosestIntersection(canvas.getStartPosition());
            int[] indices = pi.getPrimitiveVertexIndices();
            final int index = this.genesShape.getShapeIndex(indices[0]);
            if (index < 0)
                return;
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run() {
                                               onPicked(index);
                                           }
                                       });
        }
    }

    private void onPicked(int shapeIndex) {
        if (isGenes)
            this.framework.displaySlideElementInfo(0, shapeIndex);
    }

    private void onCtrlPicked(MouseEvent event, PickCanvas canvas) {
        canvas.setShapeLocation(event);
        PickResult pickResult = canvas.pickClosest();
        if (pickResult != null) {
            if (pickResult.getObject() == this.genesShape) {
                PickIntersection pi = pickResult.getClosestIntersection(canvas.getStartPosition());
                int[] indices = pi.getPrimitiveVertexIndices();
                int index = this.genesShape.getShapeIndex(indices[0]);
                if (index < 0) {
                    return;
                }
                float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
                float x = (locations[index][0]-this.up_left_point.x)/scale;
                float y = (locations[index][1]-this.up_left_point.y)/scale;
                this.driftInterpolator.drift(new Point3d(x, 0, y), new Vector3f(0, 0, 1));
            } else if (pickResult.getObject() == this.landscape) {
                PickIntersection pi = pickResult.getClosestIntersection(canvas.getStartPosition());
                Point3d point = pi.getPointCoordinates();
                point.y += this.controlPanel.getSliderValue();
                Vector3f normal = pi.getPointNormal();
                this.driftInterpolator.drift(point, normal);
            }
        }
    }

    private void onSetCluster() {

        float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
        Point2f start = this.selectionShape.getStartCoords();
        Point2f end   = this.selectionShape.getEndCoords();
        start.scale(scale);
        end.scale(scale);
        start.add(this.up_left_point);
        end.add(this.up_left_point);
        IntArray ids = new IntArray();
        for (int i=0; i<this.locations.length; i++) {
            float x = this.locations[i][0];
            float y = this.locations[i][1];
            if (x>start.x && x<end.x && y>start.y && y<end.y)
                ids.add(i);
        }

        if (isGenes) {
              framework.storeSubCluster(ids.toArray(), this.experiment, Cluster.GENE_CLUSTER);
              this.genesShape.updateColors(this.data.getColorIndices(), this.data.getColors());
        } else {
            framework.storeSubCluster(ids.toArray(), this.experiment, Cluster.EXPERIMENT_CLUSTER);
            this.genesShape.updateColors(this.data.getExperimentColorIndices(), this.data.getExperimentColors());
        }
        this.selectionShape.clearSelection();
    }
    
    
    private void onLaunchNewSession() {
        
        float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
        Point2f start = this.selectionShape.getStartCoords();
        Point2f end   = this.selectionShape.getEndCoords();
        start.scale(scale);
        end.scale(scale);
        start.add(this.up_left_point);
        end.add(this.up_left_point);
        IntArray ids = new IntArray();
        for (int i=0; i<this.locations.length; i++) {
            float x = this.locations[i][0];
            float y = this.locations[i][1];
            if (x>start.x && x<end.x && y>start.y && y<end.y)
                ids.add(i);
        }

//        int [] indices = ids.toArray();
  //      if(indices.length > 0) {
            if (isGenes) {
            framework.launchNewMAV(ids.toArray(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);

            } else {
            framework.launchNewMAV(ids.toArray(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);

            }
    //    }
        this.selectionShape.clearSelection();
        
    }
    
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */
    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(i);
        }
        return dataIndices;
    }
    

    private void onFillPolygon() {
        JMenuItem item = getJMenuItem(this.popup, FILL_POLYGON_CMD);
        if (item.isSelected()) {
            this.landscape.setPoligonMode(PolygonAttributes.POLYGON_FILL);
        } else {
            this.landscape.setPoligonMode(PolygonAttributes.POLYGON_LINE);
        }
    }

    private void onShowControls() {
        JMenuItem item = getJMenuItem(this.popup, SHOW_CONTROLS_CMD);
        if (item.isSelected()) {
            this.controlPanel.setVisible(true);
        } else {
            this.controlPanel.setVisible(false);
        }
    }

    /**
     * Returns intersection point the view ray and x-z flat.
     */
    private Point3d getIntersectionPoint(MouseEvent event, PickCanvas canvas) {
        float tolerance = canvas.getTolerance();
        canvas.setTolerance(2f);
        Point3d o3d = new Point3d();
        Vector3d d3d = new Vector3d();
        canvas.setShapeLocation(event);
        PickCone ps = (PickCone)canvas.getPickShape();
        canvas.setTolerance(tolerance); // restore tolerance
        ps.getOrigin(o3d);
        ps.getDirection(d3d);
        double x0 = o3d.x - o3d.y*d3d.x/d3d.y;
        double z0 = o3d.z - o3d.y*d3d.z/d3d.y;
        if (o3d.y < 0 || x0 < 0 || z0 < 0 || x0 > 1 || z0 > 1)
            return null;
        return new Point3d(x0, 0, z0);
    }

    private void onStartSelection(MouseEvent event, PickCanvas canvas) {
        this.selectionShape.startSelection(getIntersectionPoint(event, canvas));
    }

    private void onDragSelection(MouseEvent event, PickCanvas canvas) {
        this.selectionShape.dragSelection(getIntersectionPoint(event, canvas));
    }

    private void onReleaseSelection() {
    }

    private void onDeselect() {
        this.selectionShape.clearSelection();
    }

    private void onShowAll() {
        doZoom(new Point2f(0f, 0f), new Point2f(1f, 1f));
    }

    private void onZoom() {
        this.undoManager.addEdit(new ZoomUndoable(this.up_left_point, this.bottom_right_point));

        Point2f start = this.selectionShape.getStartCoords();
        Point2f end   = this.selectionShape.getEndCoords();
        float minSide = Math.min(end.x-start.x, end.y-start.y);
        end.set(start.x+minSide, start.y+minSide);
        float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
        start.scale(scale);
        end.scale(scale);
        start.add(this.up_left_point);
        end.add(this.up_left_point);
        doZoom(start, end);
    }

    private void doZoom(Point2f start, Point2f end) {
        this.up_left_point.set(start);
        this.bottom_right_point.set(end);
        float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
        float[][] heights = DomainUtil.getHeights(this.locations, this.grid_size, this.sigma*scale, this.up_left_point, this.bottom_right_point);
        this.landscape.setHeights(heights);
        this.genesShape.setZoom(this.up_left_point, this.bottom_right_point);
        this.linksShape.setZoom(this.up_left_point, this.bottom_right_point);
        this.selectionShape.clearSelection();
        updateLabelsGroup();
    }

    private void onUndo() {
        this.undoManager.undo();
    }

    private void onPointsShape() {
        this.pickBehavior.setTolerance(1f);
        this.genesShape.setType(GenesShape.POINTS);
    }

    private void onSpheresShape() {
        this.pickBehavior.setTolerance(0f);
        this.genesShape.setType(GenesShape.SPHERES);
    }

    private void onCubesShape() {
        this.pickBehavior.setTolerance(0f);
        this.genesShape.setType(GenesShape.CUBES);
    }

    private void onGridSize() {
        String value = (String)JOptionPane.showInputDialog(this, "Enter in a number of the grid cells per side.", "Input", JOptionPane.OK_CANCEL_OPTION, null, null, String.valueOf(this.grid_size-1));
        if (value == null || value.equals(""))
            return;
        try {
            int size = Integer.parseInt(value);
            if (size <= 2 || size >= 500) {
                throw new NumberFormatException("value must be more than 2 and less than 500.");
            }
            this.grid_size = size+1;
            float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
            float[][] heights = DomainUtil.getHeights(this.locations, this.grid_size, this.sigma*scale, this.up_left_point, this.bottom_right_point);
            this.landscape.setHeights(heights);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Illegal number: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDriftDialog() {
        String value = (String)JOptionPane.showInputDialog(this, "Enter in a minimal distance.", "Input", JOptionPane.OK_CANCEL_OPTION, null, null, String.valueOf(this.driftInterpolator.getMinDistance()));
        if (value == null || value.equals(""))
            return;
        try {
            float distance = Float.parseFloat(value);
            if (distance <= 0f || distance >= 1f)
                throw new NumberFormatException("value must be more than 0 and less than 1.");
            this.driftInterpolator.setMinDistance(distance);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Illegal number: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowLinks() {
        JMenuItem item = getJMenuItem(this.popup, SHOW_LINKS_CMD);
        if (item.isSelected())
            this.linksShape.setVisible(true);
        else
            this.linksShape.setVisible(false);
    }

    private void onLinksThreshold() {
        String value = (String)JOptionPane.showInputDialog(this, "Enter in links threshold (between 0 and 1).", "Input", JOptionPane.OK_CANCEL_OPTION, null, null, String.valueOf(this.linksShape.getThreshold()));
        if (value == null || value.equals(""))
            return;
        try {
            float threshold = Float.parseFloat(value);
            if (threshold < 0f || threshold > 1f)
                throw new NumberFormatException("value must be between 0 and 1.");
            this.linksShape.setThreshold(threshold);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Illegal number: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLinksWidth() {
        String value = (String)JOptionPane.showInputDialog(this, "Enter in links thickness (between 1 and 10).", "Input", JOptionPane.OK_CANCEL_OPTION, null, null, String.valueOf(this.linksShape.getLinksWidth()));
        if (value == null || value.equals(""))
            return;
        try {
            float thickness = Float.parseFloat(value);
            if (thickness < 1f || thickness > 10f)
                throw new NumberFormatException("value must be between 1 and 10.");
            this.linksShape.setLinksWidth(thickness);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Illegal number: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onHideLabels() {
        JMenuItem item = getJMenuItem(this.popup, HIDE_LABELS_CMD);
        if (!item.isSelected()) {
            this.labelsGroup = createLabelsGroup(this.labelIndex, this.locations, this.up_left_point, this.bottom_right_point);
            this.sceneGroup.addChild(this.labelsGroup);
        } else {
            this.labelsGroup.detach();
            this.labelsGroup = null;
        }
    }

    private void onUseBillboard() {
        JMenuItem bill_item = getJMenuItem(this.popup, USE_BILLBOARD_CMD);
        this.isBillboard = bill_item.isSelected();
        if (this.labelsGroup != null) {
            this.labelsGroup.detach();
        }
        JMenuItem hide_item = getJMenuItem(this.popup, HIDE_LABELS_CMD);
        if (!hide_item.isSelected()) {
            this.labelsGroup = createLabelsGroup(this.labelIndex, this.locations, this.up_left_point, this.bottom_right_point);
            this.sceneGroup.addChild(this.labelsGroup);
        }
    }

    private void onShowElements() {
        float scale = Math.max(Math.abs(this.up_left_point.x-this.bottom_right_point.x), Math.abs(this.up_left_point.y-this.bottom_right_point.y));
        Point2f start = this.selectionShape.getStartCoords();
        Point2f end   = this.selectionShape.getEndCoords();
        start.scale(scale);
        end.scale(scale);
        start.add(this.up_left_point);
        end.add(this.up_left_point);
        
        

        ArrayList info = new ArrayList();
        for (int i=0; i<locations.length; i++) {
            float x = locations[i][0];
            float y = locations[i][1];
            if (x >= start.x && x <= end.x && y >= start.y && y <= end.y) {
                info.add(new String("Element["+String.valueOf(i)+1+"]: "+(isGenes ? this.data.getElementAttribute(this.experiment.getGeneIndexMappedToData(i), labelIndex) : this.data.getSampleName(i) )));
            }
        }
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        String title = "Selected Elements";
        ListDialog list = new ListDialog(frame, title, info.toArray(new String[info.size()]));
        list.showModal();
    }

    private void updateLabelsGroup() {
        if (this.labelsGroup != null) {
            this.labelsGroup.detach();
            this.labelsGroup = null;
        }
        JMenuItem item = getJMenuItem(this.popup, HIDE_LABELS_CMD);
        if (!item.isSelected()) {
            this.labelsGroup = createLabelsGroup(this.labelIndex, this.locations, this.up_left_point, this.bottom_right_point);
            this.sceneGroup.addChild(this.labelsGroup);
        }
    }

    private void showElementTip(int x, int y) {
        if (isGenes && this.labelIndex < 0)
            return;
        PickCanvas canvas = new PickCanvas(this.onScreenCanvas, this.sceneGroup);
        canvas.setTolerance(this.pickBehavior.getTolerance());
        canvas.setShapeLocation(x, y);
        PickResult[] pickResult = canvas.pickAllSorted();
        if (pickResult == null)
            return;
        for (int i=0; i<pickResult.length; i++)
            if (pickResult[i].getObject() == this.genesShape) {
                PickIntersection pi = pickResult[i].getClosestIntersection(canvas.getStartPosition());
                if (pi == null)
                    return;
                int[] indices = pi.getPrimitiveVertexIndices();
                final int index = this.genesShape.getShapeIndex(indices[0]);
                if (index < 0)
                    return;
                String text = this.isGenes ? this.data.getElementAttribute(index, this.labelIndex) : this.data.getSampleName(index);

                JToolTip tooltip = new JToolTip();
                tooltip.setTipText(text);
                Dimension size = tooltip.getPreferredSize();

                Point screenLocation = this.onScreenCanvas.getLocationOnScreen();

                this.tipWindow.getContentPane().add(tooltip, BorderLayout.CENTER);
                this.tipWindow.setLocation((int)(screenLocation.x+x), (int)(screenLocation.y+y+20));
                this.tipWindow.pack();
                this.tipWindow.setVisible(true);
                return;
            }
    }

    private void hideElementTip() {
        this.tipWindow.getContentPane().removeAll();
        this.tipWindow.setVisible(false);
    }

    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return this.clusters;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return this.experiment;
    }    

    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
    /**
     * The listener to listen to menu, mouse and keyboard events.
     */
    private class Listener extends MouseAdapter implements ActionListener, MouseMotionListener, KeyListener, PickListener {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == this.timer) {
                onTimerEvent();
                return;
            }
            String command = event.getActionCommand();
            if (command.equals(SET_CLUSTER_CMD)) {
                TerrainViewer.this.onSetCluster();
            } else if (command.equals(SHOW_ELEMENTS_CMD)) {
                TerrainViewer.this.onShowElements();
            } else if (command.equals(FILL_POLYGON_CMD)) {
                TerrainViewer.this.onFillPolygon();
            } else if (command.equals(SHOW_CONTROLS_CMD)) {
                TerrainViewer.this.onShowControls();
            } else if (command.equals(ZOOM_IN_CMD)) {
                TerrainViewer.this.onZoom();
            } else if (command.equals(UNDO_CMD)) {
                TerrainViewer.this.onUndo();
            } else if (command.equals(DESELECT_CMD)) {
                TerrainViewer.this.onDeselect();
            } else if (command.equals(SHOW_ALL_CMD)) {
                TerrainViewer.this.onShowAll();
            } else if (command.equals(POINTS_SHAPE_CMD)) {
                TerrainViewer.this.onPointsShape();
            } else if (command.equals(SPHERES_SHAPE_CMD)) {
                TerrainViewer.this.onSpheresShape();
            } else if (command.equals(CUBES_SHAPE_CMD)) {
                TerrainViewer.this.onCubesShape();
            } else if (command.equals(GRID_SIZE_CMD)) {
                TerrainViewer.this.onGridSize();
            } else if (command.equals(DRIFT_DIALOG_CMD)) {
                TerrainViewer.this.onDriftDialog();
            } else if (command.equals(SHOW_LINKS_CMD)) {
                TerrainViewer.this.onShowLinks();
            } else if (command.equals(HIDE_LABELS_CMD)) {
                TerrainViewer.this.onHideLabels();
            } else if (command.equals(USE_BILLBOARD_CMD)) {
                TerrainViewer.this.onUseBillboard();
            } else if (command.equals(LINKS_THRESHOLD_CMD)) {
                TerrainViewer.this.onLinksThreshold();
            } else if (command.equals(LINKS_WIDTH_CMD)) {
                TerrainViewer.this.onLinksWidth();
            } else if (command.equals(LAUNCH_SESSION_CMD)) {
                TerrainViewer.this.onLaunchNewSession();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                setEnableMenuItem(ZOOM_IN_CMD, TerrainViewer.this.selectionShape.hasSelection());
                setEnableMenuItem(DESELECT_CMD, TerrainViewer.this.selectionShape.hasSelection());
                setEnableMenuItem(SET_CLUSTER_CMD, TerrainViewer.this.selectionShape.hasSelection());
                setEnableMenuItem(SHOW_ELEMENTS_CMD, TerrainViewer.this.selectionShape.hasSelection());
                setEnableMenuItem(UNDO_CMD, TerrainViewer.this.undoManager.canUndo());
                TerrainViewer.this.popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void onMousePressed(MouseEvent event, PickCanvas canvas) {
            TerrainViewer.this.hideElementTip();
            if (event.isMetaDown() || event.isAltDown())
                return;
            if (event.isShiftDown()) {
                // selection
                TerrainViewer.this.onStartSelection(event, canvas);
            } else {
                if (event.isControlDown())
                    TerrainViewer.this.onCtrlPicked(event, canvas);
                else
                    TerrainViewer.this.onPicked(event, canvas);
            }
        }

        public void onMouseDragged(MouseEvent event, PickCanvas canvas) {
            if (event.isShiftDown())
                TerrainViewer.this.onDragSelection(event, canvas);
        }

        public void onMouseReleased(MouseEvent event, PickCanvas canvas) {
            TerrainViewer.this.onReleaseSelection();
        }

        private PickResult[] getPickSortedResult(MouseEvent event, PickCanvas canvas) {
            canvas.setShapeLocation(event);
            return canvas.pickAllSorted();
        }

        private PickResult[] getPickAllResult(MouseEvent event, PickCanvas canvas) {
            canvas.setShapeLocation(event);
            return canvas.pickAll();
        }

        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            // awt canvas events have to be used because of J3D bug
            TerrainViewer.this.keyMotionBehavior.onKeyEvent(e);
        }
        public void keyReleased(KeyEvent e) {}

        // tips monitoring
        private Timer timer;
        private int x = 0, y = 0;

        public Listener() {
            this.timer = new Timer(1000, this);
            this.timer.setRepeats(false);
        }

        public void mouseEntered(MouseEvent e) {
            this.timer.start();
        }

        public void mouseExited(MouseEvent e) {
            this.timer.stop();
        }

        public void mouseMoved(MouseEvent e) {
            this.x = e.getX();
            this.y = e.getY();
            TerrainViewer.this.hideElementTip();
            this.timer.restart();
        }

        private void onTimerEvent() {
            TerrainViewer.this.showElementTip(this.x, this.y);
        }

        public void mouseDragged(MouseEvent e) {
        }
    }

    // Undo commands
    private class ZoomUndoable extends AbstractUndoableEdit {

        private Point2f start;
        private Point2f end;

        public ZoomUndoable(Point2f start, Point2f end) {
            this.start = new Point2f(start);
            this.end = new Point2f(end);
        }

        public void undo() throws CannotUndoException {
            TerrainViewer.this.doZoom(this.start, this.end);
        }

        public void redo() throws CannotRedoException {
        }

        public String getPresentationName() {
            return "Zoom";
        }
    }



	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.exptID;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}

}
