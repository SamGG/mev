/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Content3D.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:49 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PointLight;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.util.FloatMatrix;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.behaviors.PickRotateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickZoomBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Content3D extends JPanel {
    
    private IData data;
    private int mode;
    private FloatMatrix U;
    private Experiment experiment;
    private SimpleUniverse universe;
    private Canvas3D onScreenCanvas;
    private Canvas3D offScreenCanvas;
    private BranchGroup scene;
    private TransformGroup spinGroup;
    private boolean whiteBackground = false;
    private boolean selection = false;
    private boolean selectionBox = false;
    private boolean showSpheres = false;
    private boolean showText = false;
    private boolean geneViewer = true;
    private float scaleAxisX = 3f;
    private float scaleAxisY = 3f;
    private float scaleAxisZ = 3f;
    private float boxSizeX = 5f;
    private float boxSizeY = 5f;
    private float boxSizeZ = 5f;
    private float boxPositionX = 0f;
    private float boxPositionY = 0f;
    private float boxPositionZ = 0f;
    private float pointSize = 1.0f;
    private float selectedPointSize = 1.0f;
    private Color3f blackColor = new Color3f(0f, 0f, 0f);
    // private Color3f whiteColor = new Color3f(0.8f, 0.8f, 0.8f);
    //private Color3f whiteColor = new Color3f(0.98f, 0.98f, 0.98f);
    private Color3f whiteColor = new Color3f(1f, 1f, 1f);    
    
    /**
     * Constructs a <code>Content3D</code> with specified mode,
     * U-matrix and an experiment data.
     *
     * @param mode the annotations drawing mode.
     * @param U the dam U-matrix.
     * @param experiment the experiment data.
     */
    public Content3D(int mode, FloatMatrix U, Experiment experiment) {
        this.mode = mode;
        this.U = U;
        this.experiment = experiment;
        initScales(U);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 10));
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        this.onScreenCanvas = new Canvas3D(config);
        this.universe = new SimpleUniverse(onScreenCanvas);
        universe.getViewingPlatform().setNominalViewingTransform();
        
        offScreenCanvas = new Canvas3D(config, true);
        Screen3D sOn = onScreenCanvas.getScreen3D();
        Screen3D sOff = offScreenCanvas.getScreen3D();
        sOff.setSize(sOn.getSize());
        sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
        sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());
        // attach the offscreen canvas to the view
        universe.getViewer().getView().addCanvas3D(offScreenCanvas);
        
        add(onScreenCanvas, BorderLayout.CENTER);
    }
    
    /**
     * Constructs a <code>Content3D</code> with specified mode,
     * U-matrix and an experiment data.
     *
     * @param mode the annotations drawing mode.
     * @param U the dam U-matrix.
     * @param experiment the experiment data.
     */
    public Content3D(int mode, FloatMatrix U, Experiment experiment, boolean geneViewer) {
        this.mode = mode;
        this.U = U;
        this.experiment = experiment;
        this.geneViewer = geneViewer;
        initScales(U);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 10));
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        this.onScreenCanvas = new Canvas3D(config);
        this.universe = new SimpleUniverse(onScreenCanvas);
        universe.getViewingPlatform().setNominalViewingTransform();
        
        offScreenCanvas = new Canvas3D(config, true);
        Screen3D sOn = onScreenCanvas.getScreen3D();
        Screen3D sOff = offScreenCanvas.getScreen3D();
        sOff.setSize(sOn.getSize());
        sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
        sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());
        // attach the offscreen canvas to the view
        universe.getViewer().getView().addCanvas3D(offScreenCanvas);
        
        add(onScreenCanvas, BorderLayout.CENTER);
    }
    
    /**
     * Sets the content data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    
    /**
     * Returns x coordinate of the selection box.
     */
    public float getPositionX() {
        return boxPositionX;
    }
    
    /**
     * Returns y coordinate of the selection box.
     */
    public float getPositionY() {
        return boxPositionY;
    }
    
    /**
     * Returns z coordinate of the selection box.
     */
    public float getPositionZ() {
        return boxPositionZ;
    }
    
    /**
     * Returns x size of the selection box.
     */
    public float getSizeX() {
        return boxSizeX;
    }
    
    /**
     * Returns y size of the selection box.
     */
    public float getSizeY() {
        return boxSizeY;
    }
    
    /**
     * Returns z size of the selection box.
     */
    public float getSizeZ() {
        return boxSizeZ;
    }
    
    /**
     * Sets the selection box position.
     */
    public void setBoxPosition(float dimX, float dimY, float dimZ) {
        boxPositionX = dimX;
        boxPositionY = dimY;
        boxPositionZ = dimZ;
    }
    
    /**
     * Sets the selection box size.
     */
    public void setBoxSize(float dimX, float dimY, float dimZ) {
        boxSizeX = dimX;
        boxSizeY = dimY;
        boxSizeZ = dimZ;
    }
    
    /**
     * Returns size of a selected point.
     */
    public float getSelectedPointSize() {
        return selectedPointSize;
    }
    
    /**
     * Sets size of a selected point.
     */
    public void setSelectedPointSize(float size) {
        selectedPointSize = size;
    }
    
    /**
     * Returns a point size.
     */
    public float getPointSize() {
        return pointSize;
    }
    
    /**
     * Returns a specified point size.
     */
    private float getPointSize(boolean selected) {
        return selected ? getSelectedPointSize() : getPointSize();
    }
    
    /**
     * Sets a point size.
     */
    public void setPointSize(float size) {
        pointSize = size;
    }
    
    /**
     * Returns a scale of the x axis.
     */
    public float getScaleAxisX() {
        return scaleAxisX;
    }
    
    /**
     * Returns a scale of the y axis.
     */
    public float getScaleAxisY() {
        return scaleAxisY;
    }
    
    /**
     * Returns a scale of the z axis.
     */
    public float getScaleAxisZ() {
        return scaleAxisZ;
    }
    
    /**
     * Sets scales.
     */
    public void setScale(float dimX, float dimY, float dimZ) {
        scaleAxisX = dimX;
        scaleAxisY = dimY;
        scaleAxisZ = dimZ;
    }
    
    /**
     * Sets white background attribute.
     */
    public void setWhiteBackround(boolean value) {
        whiteBackground = value;
    }
    
    /**
     * @return true, if background is white.
     */
    public boolean isWhiteBackground() {
        return whiteBackground;
    }
    
    /**
     * Sets the selection attribute.
     */
    public void setSelection(boolean value) {
        selection = value;
    }
    
    /**
     * Returns value of the selection attribute.
     */
    public boolean isSelection() {
        return selection;
    }
    
    /**
     * Sets the selection box attribute.
     */
    public void setSelectionBox(boolean value) {
        selectionBox = value;
    }
    
    /**
     * Returns value of the selection box attribute.
     */
    public boolean isSelectionBox() {
        return selectionBox;
    }
    
    /**
     * Sets the show spheres attribute.
     */
    public void setShowSpheres(boolean value) {
        showSpheres = value;
    }
    
    /**
     * Returns value of the show spheres attribute.
     */
    public boolean isShowSpheres() {
        return showSpheres;
    }
    
    /**
     * Sets the show text attribute.
     */
    public void setShowText(boolean value) {
        showText = value;
    }
    
    /**
     * Returns value of the show text attribute.
     */
    public boolean isShowText() {
        return showText;
    }
    
    /**
     * Returns the content image.
     */
    public BufferedImage createImage() {
//        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, (BufferedImage)this.createImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight()));
        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight()));
        offScreenCanvas.setOffScreenLocation(onScreenCanvas.getLocationOnScreen());
        offScreenCanvas.setOffScreenBuffer(buffer);
        offScreenCanvas.renderOffScreenBuffer();
        offScreenCanvas.waitForOffScreenRendering();
        BufferedImage offImage = offScreenCanvas.getOffScreenBuffer().getImage();
        BufferedImage image = new BufferedImage(offImage.getWidth(), offImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        //BufferedImage image = (BufferedImage)this.createImage(offImage.getWidth(), offImage.getHeight());
        //BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(offImage.getWidth(), offImage.getHeight());        
        image.setData(offImage.getData());
        return image;
    }
    
    /**
     * Resets spin coordinaties.
     */
    public void reset() {
        spinGroup.setTransform(new Transform3D());
    }
    
    /**
     * Updates the universe scene.
     */
    protected void updateScene() {
        Transform3D spinTransform = new Transform3D();
        if (scene != null) {
            spinGroup.getTransform(spinTransform);
            scene.detach();
        }
        this.scene = createSceneGraph(onScreenCanvas, spinTransform);
        universe.addBranchGraph(scene);
    }
    
    /**
     * Sets scales according to U-matrix values.
     */
    private void initScales(FloatMatrix U) {
        float max = 0f;
        final int rows = U.getRowDimension();
        for (int i = rows; --i >= 0;) {
            max = Math.max(max, Math.max(Math.max(Math.abs(U.get(i, 0)), Math.abs(U.get(i, 1))), Math.abs(U.get(i, 2))));
        }
        setScale(max, max, max);
    }
    
    /**
     * Creates a branch group with specified canvas.
     */
    private BranchGroup createSceneGraph(Canvas3D canvas) {
        return createSceneGraph(canvas, null);
    }
    
    /**
     * Creates a branch group with specified canvas and transformation object.
     */
    private BranchGroup createSceneGraph(Canvas3D canvas, Transform3D spinTransform) {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        
        this.spinGroup = createCoordinateSystem(bounds);
        if (spinTransform != null) {
            spinGroup.setTransform(spinTransform);
        }
        TransformGroup objScale = createScaleTransformGroup(bounds);
        objScale.addChild(spinGroup);
        objRoot.addChild(objScale);
        
        PickRotateBehavior rotateBehavior = new PickRotateBehavior(objRoot, canvas, bounds);
        objRoot.addChild(rotateBehavior);
        PickZoomBehavior zoomBehavior = new PickZoomBehavior(objRoot, canvas, bounds);
        objRoot.addChild(zoomBehavior);
        PickTranslateBehavior translateBehavior = new PickTranslateBehavior(objRoot, canvas, bounds);
        objRoot.addChild(translateBehavior);
        
        objRoot.compile();
        return objRoot;
    }
    
    /**
     * Creates a scale transform group.
     */
    private TransformGroup createScaleTransformGroup(BoundingSphere bounds) {
        Transform3D t = new Transform3D();
        t.setScale(0.22);
        TransformGroup scale = new TransformGroup(t);
        
        Color3f bgColor = isWhiteBackground() ? whiteColor : blackColor;
        Background bg = new Background(bgColor);
        bg.setApplicationBounds(bounds);
        scale.addChild(bg);
        
        scale.addChild(createAmbientLight(bounds));
        scale.addChild(createLight(bounds, new Vector3d(0.0, 0.0, 3.0)));
        scale.addChild(createLight(bounds, new Vector3d(0.0, 0.0, 10.0)));
        return scale;
    }
    
    /**
     * Creates a coordinate system transform group.
     */
    private TransformGroup createCoordinateSystem(BoundingSphere bounds) {
        TransformGroup group = new TransformGroup();
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        group.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        // add axises
        group.addChild(createXAxis());
        group.addChild(createYAxis());
        group.addChild(createZAxis());
        
        if (isShowSpheres()) {
            // add spheres
            group.addChild(createSpheres());
        } else {
            // add selected and not selected points
            if (getPointSize() > 0) {
                Shape3D[] pointShapes = createUsualPoints();
                if (pointShapes != null) {
                    for (int i=0; i<pointShapes.length; i++) {
                        group.addChild(pointShapes[i]);
                    }
                }
                Shape3D points = createSelectedPoints();
                if (points != null) {
                    group.addChild(points);
                }
            }
        }
        if (isSelectionBox()) {
            group.addChild(createSelectionBox());
        }
        if (isShowText()) {
            group.addChild(createText());
        }
        OpenBehavior openObject = new OpenBehavior(group);
        openObject.setSchedulingBounds(bounds);
        group.addChild(openObject);
        return group;
    }

    
    /**
     * Creates a light transform group.
     */
    private TransformGroup createLight(BoundingSphere bounds, Vector3d vector) {
        Transform3D t = new Transform3D();
        t.set(vector);
        TransformGroup lightGroup = new TransformGroup(t);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        lightGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        
        ColoringAttributes attr = new ColoringAttributes();
        Color3f color = new Color3f(1.0f, 1.0f, 1.0f);
        attr.setColor(color);
        Appearance appearance = new Appearance();
        appearance.setColoringAttributes(attr);
        lightGroup.addChild(new Sphere(0.01f, Sphere.GENERATE_NORMALS, 15, appearance));
        Light light = new PointLight(color, new Point3f(0.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f));
        light.setInfluencingBounds(bounds);
        lightGroup.addChild(light);
        return lightGroup;
    }
    
    
    /**
     * Creates an ambient light.
     */
    private AmbientLight createAmbientLight(BoundingSphere bounds) {
        AmbientLight light = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
        light.setInfluencingBounds(bounds);
        return light;
    }
    
    /**
     * Creates a cone shape.
     */
    private Cone createCone() {
        return new Cone(0.05f, 0.2f);
    }
    
    /**
     * Creates a cylinder shape with specified color.
     */
    private Cylinder createCylinder(Color3f color) {
        Material material = new Material(color, blackColor, color, whiteColor, 100f);
        if (isWhiteBackground()) {
            material.setEmissiveColor(new Color3f(0.0f, 0.0f, 1.0f));
        }
        Appearance appearance = new Appearance();
        appearance.setLineAttributes(new LineAttributes(10, LineAttributes.PATTERN_SOLID, true));
        appearance.setMaterial(material);
        return new Cylinder(0.025f, 6f, appearance);
    }
    
    /**
     * Creates a selection box transform group.
     */
    private TransformGroup createSelectionBox() {
        Material material = new Material(new Color3f(0.5f, 0.5f, 0.5f), blackColor, new Color3f(0.5f, 0.5f, 0.5f), blackColor, 100.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparency(0.5f);
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);
        appearance.setTransparencyAttributes(ta);
        appearance.setMaterial(material);
        Transform3D transform = new Transform3D();
        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;
        Vector3f vector = new Vector3f(boxPositionX*factorX, boxPositionY*factorY, boxPositionZ*factorZ);
        transform.set(vector);
        TransformGroup selectionBox = new TransformGroup(transform);
        selectionBox.addChild(new Box(boxSizeX*factorX/2f, boxSizeY*factorY/2f, boxSizeZ*factorZ/2f, appearance));
        return selectionBox;
    }
    
    /**
     * Checkes if specified point is selected.
     */
    private boolean isPointSelected(float x, float y, float z) {
        float X1 = boxPositionX-boxSizeX/2f;
        float X2 = boxPositionX+boxSizeX/2f;
        float Y1 = boxPositionY-boxSizeY/2f;
        float Y2 = boxPositionY+boxSizeY/2f;
        float Z1 = boxPositionZ-boxSizeZ/2f;
        float Z2 = boxPositionZ+boxSizeZ/2f;
        return x>=X1 && x<=X2 && y>=Y1 && y<=Y2 && z>=Z1 && z<=Z2;
    }
    
    /**
     * Returns an array of selected genes.
     */
    protected int[] getSelectedGenes() {
        int[] genes = new int[getPointsCount(true)];
        int pos = 0;
        for (int i=0; i<U.getRowDimension(); i++) {
            if (isPointSelected(U.get(i,0), U.get(i,1), U.get(i,2))) {
                genes[pos] = experiment.getGeneIndexMappedToData(i);
                pos++;
            }
        }
        return genes;
    }
    
    /**
     * @param selected true if we need number of the selected points.
     */
    private int getPointsCount(boolean selected) {
        int count = 0;
        int selCount = 0;
        for (int i=0; i<U.getRowDimension(); i++) {
            if (isPointSelected(U.get(i,0), U.get(i,1), U.get(i,2))) {
                selCount++;
            } else {
                count++;
            }
        }
        return selected ? selCount : count;
    }
    
    /**
     * Returns number of unselected points.
     */
    private int getUsualPointsCount() {
        if (isSelection()) {
            return getPointsCount(false);
        }
        return U.getRowDimension();
    }
    
    /**
     * Creates a 3D shape which is set of selected points.
     */
    private Shape3D createSelectedPoints() {
        if (!isSelection()) {
            return null;
        }
        int count = getPointsCount(true);
        if (count < 1) {
            return null;
        }
        Color3f color = new Color3f(1.0f, 0.3f, 1.0f);
        Material material = new Material(color, color, color, color, 100.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setPointAttributes(new PointAttributes(getSelectedPointSize(), false));
        appearance.setMaterial(material);
        
        PointArray points = new PointArray(count, PointArray.COORDINATES | PointArray.COLOR_3 | PointArray.NORMALS);
        
        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;
        
        float x, y, z;
        int index = 0;
        for (int i=0; i<U.getRowDimension(); i++) {
            x = U.get(i,0);
            y = U.get(i,1);
            z = U.get(i,2);
            if (isPointSelected(x, y, z)) {
                points.setCoordinate(index, new Point3f(x*factorX, y*factorY, z*factorZ));
                index++;
            }
        }
        Shape3D pointsShape = new Shape3D();
        pointsShape.setAppearance(appearance);
        pointsShape.setGeometry(points);
        return pointsShape;
    }
    
    /**
     * Creates a point appearance with specified color.
     */
    private Appearance createPointAppearance(Color3f color) {
        Material material = new Material(color, color, color, color, 100.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setPointAttributes(new PointAttributes(getPointSize(), false));
        appearance.setMaterial(material);
        return appearance;
    }
    
    /**
     * Creates an array of 3D shapes which are sets of not selected points.
     */
    private Shape3D[] createUsualPoints() {
        int count = getUsualPointsCount();
        if (count < 1) {
            return null;
        }
        int uncoloredCount = data.getColoredProbesCount(-1);

        int delta = uncoloredCount == 0 ? 0 : 1;
        
        Color[] colors;
        if(geneViewer)
            colors = data.getColors(); // get colored gene clusters
        else
            colors = data.getExperimentColors();

        PointArray[] pointArrays = new PointArray[colors.length+delta];
        Appearance[] appearances = new Appearance[colors.length+delta];
        int[] counters = new int[colors.length+delta];
        Color3f color;
        
        // not published probes
        if (uncoloredCount > 0) {
            appearances[0] = createPointAppearance(isWhiteBackground() ? blackColor : whiteColor);
            pointArrays[0] = new PointArray(uncoloredCount, PointArray.COORDINATES | PointArray.COLOR_3 | PointArray.NORMALS);
            counters[0]    = 0;
        }
        if(geneViewer){
            // published ones
            for (int i=0; i<colors.length; i++) {
                color = new Color3f(colors[i]);
                appearances[i+delta] = createPointAppearance(color);
                pointArrays[i+delta] = new PointArray(data.getColoredProbesCount(i), PointArray.COORDINATES | PointArray.COLOR_3 | PointArray.NORMALS);
                counters[i+delta]    = 0;
            }
        } else{ //Experiment Viewer
            for (int i=0; i<colors.length; i++) {
                color = new Color3f(colors[i]);
                appearances[i+delta] = createPointAppearance(color);
                pointArrays[i+delta] = new PointArray(data.getColoredExperimentsCount(i), PointArray.COORDINATES | PointArray.COLOR_3 | PointArray.NORMALS);
                counters[i+delta]    = 0;
            }
        }
        
        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;
        float x, y, z;
        int index = 0;
        for (int i=0; i<U.getRowDimension(); i++) {
            x = U.get(i,0);
            y = U.get(i,1);
            z = U.get(i,2);
            if (!isSelection() || !isPointSelected(x, y, z)) {
                if(geneViewer)
                    index = data.getProbeColorIndex(experiment.getGeneIndexMappedToData(i))+delta;
                else
                    index = data.getExperimentColorIndex(i)+delta;

                pointArrays[index].setCoordinate(counters[index], new Point3f(x*factorX, y*factorY, z*factorZ));
                counters[index]++;
            }
        }
   
        Shape3D[] pointShapes = new Shape3D[pointArrays.length];
        for (int i=0; i<pointShapes.length; i++) {
            pointShapes[i] = new Shape3D();
            pointShapes[i].setAppearance(appearances[i]);
            pointShapes[i].setGeometry(pointArrays[i]);
        }
        return pointShapes;
    }
    
    /**
     * Creates a point appearance with specified color.
     */
    private Appearance createSphereAppearance(Color3f color) {
        Material material = new Material(color, this.blackColor, color, this.whiteColor, 100.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        return appearance;
    }
    
    /**
     * Creates a spheres transform group.
     */
    private TransformGroup createSpheres() {
        TransformGroup spheres = new TransformGroup();
        
        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;
        
        // usual material
        Color3f uColor = isWhiteBackground() ? blackColor : whiteColor;
        Appearance uAppearance = createSphereAppearance(uColor);
        
        // selected material
        Color3f sColor = isSelection() ? new Color3f(1.0f, 0.3f, 1.0f) : uColor;
        Appearance sAppearance = createSphereAppearance(sColor);
        
        Transform3D transform;
        Vector3d vector3d;
        TransformGroup sphere;
        boolean selected;
        float x, y, z;
        
        for (int i=0; i<U.getRowDimension(); i++) {
            x = U.get(i,0);
            y = U.get(i,1);
            z = U.get(i,2);
            transform  = new Transform3D();
            vector3d = new Vector3d(x*factorX, y*factorY, z*factorZ);
            transform.set(vector3d);
            sphere = new TransformGroup(transform);
            selected = (isSelection() && isPointSelected(x, y, z));
            if(geneViewer){
                if (data.getProbeColor(experiment.getGeneIndexMappedToData(i)) == null) {
                    sphere.addChild(new Sphere(getPointSize(selected)/20f, (selected ? sAppearance : uAppearance)));
                } else {
                    sphere.addChild(new Sphere(getPointSize(selected)/20f, (selected ? sAppearance : createSphereAppearance(new Color3f(data.getProbeColor(experiment.getGeneIndexMappedToData(i)))))));
                }
            }
            else{
                if (data.getExperimentColor(i) == null) {
                    sphere.addChild(new Sphere(getPointSize(selected)/20f, (selected ? sAppearance : uAppearance)));
                } else {
                    sphere.addChild(new Sphere(getPointSize(selected)/20f, (selected ? sAppearance : createSphereAppearance(new Color3f(data.getExperimentColor(i))))));
                }
            }
            spheres.addChild(sphere);
        }
        return spheres;
    }
       
    

    /**
     * Creates a text transform group.
     */
    private TransformGroup createText() {
        TransformGroup textGroup = new TransformGroup();
        
        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;
        
        //Font3D font = new Font3D(new Font("TestFont", Font.BOLD, 1), new FontExtrusion());
        Font3D font = new Font3D(new Font("TestFont", Font.BOLD, (int)(Math.round(getPointSize(false)))), new FontExtrusion());
        Color3f color3f;
        if(!this.whiteBackground)
            color3f = new Color3f(1.0f, 1.0f, 1.0f);
        else
            color3f = new Color3f(0.0f, 0.0f, 0.0f);
        
        Material material;
        if(!this.whiteBackground)
            material = new Material(color3f, whiteColor, color3f, whiteColor, 100f);
        else
            material = new Material(color3f, blackColor, color3f, blackColor, 100f);
        
        material.setLightingEnable(true);
        //material.setLightingEnable(false);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        
        Transform3D fontTransform = new Transform3D();
        fontTransform.setScale(0.1);
        
        TransformGroup tempGroup;
        Text3D text3d;
        Shape3D shape3d;
        String text;
        float x, y, z;
        for (int i=0; i<U.getRowDimension(); i++) {
            x = U.get(i,0);
            y = U.get(i,1);
            z = U.get(i,2);
            tempGroup = new TransformGroup(fontTransform);
            text = (mode == 1) ? data.getUniqueId(i) : data.getSampleName(experiment.getSampleIndex(i));
            text3d = new Text3D(font, text, new Point3f(x*factorX*10f+getPointSize(isPointSelected(x, y, z)), (y-0.035f)*factorY*10f, z*factorZ*10f));
            shape3d = new Shape3D();
            shape3d.setGeometry(text3d);
            shape3d.setAppearance(appearance);
            tempGroup.addChild(shape3d);
            textGroup.addChild(tempGroup);
        }
        return textGroup;
    }
    
    /**
     * Creates 3D shape for specified string.
     */
    private Shape3D createTextShape3D(String text) {
        Font3D axisFont = new Font3D(new Font("TestFont", Font.BOLD, 1), new FontExtrusion());
        Text3D text3D = new Text3D(axisFont, text);
        Shape3D shape = new Shape3D();
        shape.setGeometry(text3D);
        Color3f color3f;
        Material axisFontMaterial;
        if(!whiteBackground){
            color3f = new Color3f(0.5f, 0.5f, 0.5f);
            axisFontMaterial = new Material(color3f, blackColor, color3f, whiteColor, 100f);
        }
        else{
            color3f = new Color3f(0.0f, 0.0f, 0.0f);
            axisFontMaterial = new Material(color3f, blackColor, color3f, blackColor, 100f);
        }
        axisFontMaterial.setLightingEnable(true);
        Appearance axisFontAppearance = new Appearance();
        axisFontAppearance.setMaterial(axisFontMaterial);
        shape.setAppearance(axisFontAppearance);
        return shape;
    }
    
    /**
     * Creates x-axis transform group.
     */
    private TransformGroup createXAxis() {
        Transform3D axisTrans = new Transform3D();
        axisTrans.rotZ(-Math.PI/2.0d);
        Transform3D fontTrans = new Transform3D();
        fontTrans.rotZ(Math.PI/2.0d);
        
        if(!whiteBackground)
            return createAxis("X", new Color3f(0.5f, 0.5f, 0.5f), axisTrans, fontTrans);
        else
            return createAxis("X", new Color3f(0.0f, 0.0f, 0.0f), axisTrans, fontTrans);
    }
    
    /**
     * Creates y-axis transform group.
     */
    private TransformGroup createYAxis() {
        
        if(!whiteBackground)
            return createAxis("Y", new Color3f(0.3f, 0.3f, 1f), null, null);
        else
            return createAxis("Y", new Color3f(0.0f, 0.0f, 0.0f), null, null);
    }
    
    /**
     * Creates z-axis transform group.
     */
    private TransformGroup createZAxis() {
        Transform3D axisTrans = new Transform3D();
        axisTrans.rotX(-Math.PI/2.0d);
        Transform3D zTrans = new Transform3D();
        zTrans.rotY(Math.PI/2.0d);
        axisTrans.mul(zTrans);
        Transform3D fontTrans = new Transform3D();
        fontTrans.rotZ(Math.PI/2.0d);
        if(!whiteBackground)
            return createAxis("Z", new Color3f(1f, 0.3f, 1f), axisTrans, fontTrans);
        else
            return createAxis("Z", new Color3f(0.0f, 0.0f, 0.0f), axisTrans, fontTrans);
    }
    
    /**
     * Creates an axis transform group.
     */
    private TransformGroup createAxis(String name, Color3f color, Transform3D axisTrans, Transform3D fontTrans) {
        TransformGroup axis = new TransformGroup();
        if (axisTrans != null) {
            axis.setTransform(axisTrans);
        }
        axis.addChild(createCylinder(color));
        // Axis Positive End
        Transform3D posTransform = new Transform3D();
        posTransform.set(new Vector3d(0.0, 3.1, 0.0));
        TransformGroup posEnd = new TransformGroup(posTransform);
        posEnd.addChild(createCone());
        // Axis Negative End
        Transform3D negTransform = new Transform3D();
        negTransform.set(new Vector3d(0.0, -3.1, 0.0));
        Transform3D rotate180X = new Transform3D();
        rotate180X.rotX(Math.PI);
        negTransform.mul(rotate180X);
        TransformGroup negEnd = new TransformGroup(negTransform);
        negEnd.addChild(createCone());
        // Font
        Transform3D fontTransform = new Transform3D();
        fontTransform.set(0.22, new Vector3d(0.25, 2.75, -0.0125));
        TransformGroup fontGroup = new TransformGroup(fontTransform);
        
        TransformGroup rotFontGroup = new TransformGroup();
        if (fontTrans != null) {
            rotFontGroup.setTransform(fontTrans);
        }
        
        Shape3D shape3D = createTextShape3D(name);
        rotFontGroup.addChild(shape3D);
        
        fontGroup.addChild(rotFontGroup);
        
        axis.addChild(fontGroup);
        axis.addChild(posEnd);
        axis.addChild(negEnd);
        return axis;
    }
}
