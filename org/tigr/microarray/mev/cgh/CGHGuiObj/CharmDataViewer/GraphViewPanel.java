package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm.ChARM;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ChromDataGraphNode;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ChromPredictionsGraphNode;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.IChromGraphNode;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
* This class implements the ChARM experiment display panel on the
 * right side of the interface.
*
 * <p>Title: GraphViewPanel</p>
 * <p>Description: This class implements the ChARM experiment display panel on the
 * right side of the interface.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

public class GraphViewPanel extends PCanvas implements ICGHViewer {
  public static final int HEIGHT = 3200;
  public static final int WIDTH = 1200;
  public static int END_SPACE = 100;
  public static float TEXT_HEIGHT=0;
  // TODO Check for static variable defs & usage.
  public static float MAX_GENE_VALUE = 1.25f;
  public static float AXIS_SCALE=1.0f;

  public static final int EXP_HEIGHT = 150;
  public static final int EXP_WIDTH = 1000;
  public static final int CHROM_SPACING = 20;
  public static final int EXP_PRED_SPACING = 10;
  public static final int PREDICTION_COMP_HEIGHT = 30;
  public static final int PREDICTION_HEIGHT = 10;
  public static final int TEXT_SIZE = 20;
  public static final int VISIBLE_HEIGHT = 1500;

  public static  Color TEXT_COLOR = Color.WHITE;
  public static  Color BACKGROUND_COLOR = Color.BLACK;
  public static  Color SELECTIONRECT_COLOR = Color.YELLOW;
  public static  Color ZOOMRECT_COLOR = Color.WHITE;

  /* DisplayStateManager functions transfered to ChARM.java */
  //private DisplayStateManager displayStateManager;
  private ChARM displayStateManager;
  
  private HashMap chromNodeHash;
  private ArrayList textNodes;
  private ArrayList textNodeEnds;

  private Color currBackgroundColor = Color.BLACK;

  private PScrollPane parentScrollPane;

  private MouseSelectionEventHandler mouseSelectionEventHandler;
  private MouseZoomEventHandler mouseZoomEventHandler;

  private PBounds initialBounds=null;
  private float initialScale=0;

  private IChromGraphNode currChromGraphSelection=null;
  private ArrayList currSelectedIndices=null;
  private final PText tooltipNode = new PText();

  private ArrayList currCircledGenes = new ArrayList();
  private ChromDataGraphNode currCircledChromNode;
  
  IFramework framework;
  IData data;
    
  public static final int GRAPH_WIDTH = 800;
  public static final int GRAPH_HEIGHT = 3200;
  public static final int LEFT_COMP_HEIGHT = 350;
  public static final int BUTTON_HEIGHT = 50;
  public static final Color GRAPH_BACKGROUND = Color.BLACK; //.LIGHT_GRAY;
  public static final Color STAT_BACKGROUND = Color.WHITE;
  /**
   * New Constructor
   * Raktim 9/14/06
   */
  public GraphViewPanel(IFramework framework) {
	  this.framework = framework;
	  this.data = framework.getData();
	  
	  /* The 3 variables may be avoidable - Raktim */
	  chromNodeHash = new HashMap();
	  textNodes = new ArrayList();
	  textNodeEnds = new ArrayList();
	    
	  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	  setPreferredSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
	  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	  setBackground(GRAPH_BACKGROUND);
	  setDoubleBuffered(true);
	  setOpaque(true);
	  
	  parentScrollPane = new PScrollPane(this);
	  setParent(parentScrollPane);
	  parentScrollPane.setKeyActionsDisabled(true);
	  
	  tooltipNode.setPickable(false);
	  this.getCamera().addChild(tooltipNode);
		
	  this.removeInputEventListener(this.getZoomEventHandler());
	  this.removeInputEventListener(this.getPanEventHandler());
		
	  this.mouseSelectionEventHandler = new MouseSelectionEventHandler();
	  this.addInputEventListener(mouseSelectionEventHandler);
	  this.mouseZoomEventHandler = new MouseZoomEventHandler();
	  this.addInputEventListener(mouseZoomEventHandler);
	  
	  //TODO May not be needed here.
	  initializeGraph();
  }
  /**
   * Class constructor.
   * @param disp DisplayStateManager- reference to state information container
   * DisplayStateManager replaced with ChARM
   * @param parentPane PScrollPane- parent scroll pane
   */
  //public GraphViewPanel(DisplayStateManager disp, PScrollPane parentPane) {
  public GraphViewPanel(ChARM disp, PScrollPane parentPane) {  
    super();
    System.out.println("In GraphViewPanel Constructor");
    displayStateManager = disp;
    //System.out.println("In GraphViewPanel() Just Before disp.getFramework()");
    //this.framework = disp.getFramework();
    //this.data = this.framework.getData();
    this.data = disp.getData();
    if(this.data == null){
    	System.out.println("In Const. GraphViewPanel()- data is null");
    }
    parentScrollPane = parentPane;

    chromNodeHash = new HashMap();
    textNodes = new ArrayList();
    textNodeEnds = new ArrayList();

    tooltipNode.setPickable(false);
    this.getCamera().addChild(tooltipNode);

    this.removeInputEventListener(this.getZoomEventHandler());
    this.removeInputEventListener(this.getPanEventHandler());

    this.mouseSelectionEventHandler = new MouseSelectionEventHandler();
    this.addInputEventListener(mouseSelectionEventHandler);
    this.mouseZoomEventHandler = new MouseZoomEventHandler();
    this.addInputEventListener(mouseZoomEventHandler);
  }

  /**
   * Sets the parent scrollpane reference.
   * @param scrollpane PScrollPane
   */
  public void setParent(PScrollPane scrollpane) {
  this.parentScrollPane = scrollpane;
  }

  /**
   * Initializes the graph display and creates data graph nodes for all chromosomes
   * of the first 2 experiments.
   * Raktim Changed to selected experiments for analysis
   */
  public void initializeGraph() {
	  //System.out.println("In GraphViewPanel initializeGraph()");
    chromNodeHash.clear();
    //DatasetContainer currDataset = displayStateManager.getCurrentDataset();
    //if (currDataset == null) return;
    if (this.data == null) {
    	System.out.println("In GraphViewPanel initializeGraph() this.data is null");
    	return;
    }
    else {
    	//System.out.println("In GraphViewPanel initializeGraph() Else");
       //ArrayList expList = currDataset.getExperiments();
       //ArrayList chromList = currDataset.getChromosomes();
       ArrayList expList = displayStateManager.getSelectedExperiments();
    	/*
       ArrayList featuresList = data.getFeaturesList();
	   for (int column = 0; column < expList.size(); column++){
			String name = (String)((ISlideData)expList.get(column)).getSlideDataName();
			//System.out.println("exprNames " + name);
			expList.add(name);
	   }
	   */
       int numOfChr = data.getNumChromosomes();
       System.out.println("numOfChr " + numOfChr);
       //for(int i=0; i<chromList.size(); i++) {
       for(int i = 0; i < numOfChr; i++) {
    	   int chrNumber = i + 1;
         //for(int j = 0; j < Math.min(expList.size(),1); j++) {
    	   for(int j = 0; j < expList.size(); j++) {
           //this.createChromDataGraphNode(((Chromosome)chromList.get(i)).getNumber(),(String)expList.get(j));
    		 //System.out.println("createChromDataGraphNode(chrNumber,(String)expList.get(j)" + chrNumber + " " + (String)expList.get(j));
        	 this.createChromDataGraphNode(chrNumber,(String)expList.get(j));
         }
       }
    }
  }

  /**
   * Checks if the specified experiment has been analyzed and if so, creates
   * prediction graph nodes for the given chromosome number.
   * @param chromNumber int
   * @param exp String
   */
  public void updatePredictionNode(int chromNumber, String exp) {
	  //System.out.println("updatePredictionNode() Chr: Expr " + chromNumber +":"+exp);
    //if(displayStateManager.experimentAnalyzed(exp)) {
	  /*ArrayList*/ ResultContainer selectedResults = displayStateManager.getSelectedResultSets();
      //for(int i=0; i<selectedResults.size(); i++) {
        this.createPredictionGraphNode(selectedResults.toString()/*(String)selectedResults.get(i)*/,chromNumber, exp);
      //}
    //}
  }

  /**
   * Initializes prediction nodes for the given list of experiments.
   * @param exps ArrayList
   */
  public void initializePredictionNodes(ArrayList exps) {
    //DatasetContainer currDataset = displayStateManager.getCurrentDataset();
    //if (currDataset == null) return;
    //else {
      //ArrayList chromList = currDataset.getChromosomes();
      /*ArrayList*/ ResultContainer selectedResults = displayStateManager.getSelectedResultSets();

      for (int i = 0; i < exps.size(); i++) {
    	  //System.out.println("initializePredictionNodes(): Expr " + (String) exps.get(i));
        //for (int j = 0; j < chromList.size(); j++) {
    	for (int j = 0; j < data.getNumChromosomes(); j++) {
          //for(int k=0; k < selectedResults.size(); k++) {
            //this.createPredictionGraphNode( (String)selectedResults.get(k), ((Chromosome) chromList.get(j)).getNumber(), (String) exps.get(i));
        	  this.createPredictionGraphNode(selectedResults.toString(), j+1, (String) exps.get(i));
          //}

        }
      }
    //}
  }

  /**
   * Sets the current y-axis scale to the specified value.
   * @param newScale double
   */
  public void updateAxisScale(float newScale) {
    this.AXIS_SCALE = newScale;

    ArrayList chromGraphNodes = new ArrayList(chromNodeHash.values());

    IChromGraphNode currNode = null;
    ChromDataGraphNode dataNode =null;
    for(int i=0; i < chromGraphNodes.size(); i++) {
      currNode = (IChromGraphNode)chromGraphNodes.get(i);
      //if(((Class)currNode.getClass()).getName().equals("charm.ChromDataGraphNode")) {
      if(currNode instanceof ChromDataGraphNode) {
        dataNode = (ChromDataGraphNode)currNode;
        dataNode.setAxisScale(newScale);
      }
    }
    this.refreshDataGraphs();

  }

  /**
   * Returns the current y-axis scale.
   * @return double
   */
  public double getAxisScale() {
    return this.AXIS_SCALE;
  }

  /**
   * Sets the graph background color to the given color.
   * @param newColor Color
   */
  public void setBackgroundColor(Color newColor) {
    this.setBackground(newColor);
    this.currBackgroundColor = newColor;

    if(newColor.equals(Color.BLACK)) {
      TEXT_COLOR = Color.WHITE;
      BACKGROUND_COLOR = newColor;
      SELECTIONRECT_COLOR = Color.YELLOW;
      ZOOMRECT_COLOR = Color.WHITE;
    }

    else if(newColor.equals(Color.WHITE)) {
      TEXT_COLOR = Color.BLACK;
      BACKGROUND_COLOR = newColor;
      SELECTIONRECT_COLOR = Color.MAGENTA;
      ZOOMRECT_COLOR = Color.BLACK;
    }

    this.updateGraph();
  }

  /**
   * Returns the current background color.
   * @return Color
   */
  public Color getBackgroundColor() {
    return this.currBackgroundColor;
  }

  /**
   * Redraws all graph nodes that have been changed.
   */
  public void refreshDataGraphs() {
	/*
    DatasetContainer currDataset = displayStateManager.getCurrentDataset();
    if (currDataset == null) {
      this.getLayer().removeAllChildren();
      return;
    }
    else {
    */
    //ArrayList chromList = currDataset.getChromosomes();
    ArrayList selectedExps = displayStateManager.getSelectedExperiments();

    //for (int j = 0; j < chromList.size(); j++) {
    for (int j = 0; j < data.getNumChromosomes(); j++) {
      //Chromosome chromosome = (Chromosome) chromList.get(j);
      int chromosome = j+1;
      double maxWidth = getMaxGeneSize(chromosome); //chromosome.maxSize();
      double currWidth = ((MultipleArrayData)this.data).getChromWidth(chromosome); // chromosome.getSize();

      for (int i = 0; i < selectedExps.size(); i++) {
        String currExp = (String) selectedExps.get(i);

        //if (displayStateManager.getStateVariable("Data Plot Toggle").equals("on")) {
          if (!chromNodeHash.containsKey(chromosome/*chromosome.getNumber()*/ + "," + selectedExps.get(i))) {
            this.createChromDataGraphNode(chromosome/*chromosome.getNumber()*/, (String) selectedExps.get(i));
          }
          ChromDataGraphNode currNode = (ChromDataGraphNode) chromNodeHash.get(chromosome/*chromosome.getNumber()*/ + "," + selectedExps.get(i));
          currNode.render();
        //}
      }
    }
    //}
  }


  /**
   * Removes all nodes from the current graph and redraws nodes from selected experiments.
   */
  public void updateGraph() {
    /*
    DatasetContainer currDataset = displayStateManager.getCurrentDataset();
    if (currDataset == null) {
      this.getLayer().removeAllChildren();
      return;
    }
    else {
    */
	  //System.out.println("In GraphViewPanel updateGraph()");
      ArrayList selectedExps = displayStateManager.getSelectedExperiments();
      //System.out.println("In GraphViewPanel updateGraph(), selectedExps.size(): " + selectedExps.size());
      this.getLayer().removeAllChildren();
      textNodes.clear();

      if (selectedExps.size() == 0) {
       // this.getLayer().addChild(getTextObjectAt("No Experiments Selected.", 0, this.EXP_HEIGHT / 2, false));
      }
      else {
    	  //System.out.println("In GraphViewPanel updateGraph(), else");
        //ArrayList chromList = currDataset.getChromosomes();
    	  //System.out.println("In GraphViewPanel updateGraph() Else ");
        double yOffset = 0;
        //if (displayStateManager.getStateVariable("Data Plot Toggle").equals("on") ||
            //displayStateManager.getStateVariable("Prediction Plot Toggle").equals("on")) {

          //for (int j = 0; j < chromList.size(); j++) {
        if (this.data == null) { 
        	System.out.println("data is null in GraphViewPanel updateGraph()");
        	//this.data = this.framework.getData();
        	//return; 
        	}
        //System.out.println("data.getNumChromosomes(): " + data.getNumChromosomes());
        for (int j = 0; j < data.getNumChromosomes(); j++) {
            //Chromosome chromosome = (Chromosome) chromList.get(j);
        	int chromosome = j+1;
            double maxWidth = getMaxGeneSize(chromosome); //chromosome.maxSize();
            double currWidth = ((MultipleArrayData)this.data).getChromWidth(chromosome); // chromosome.getSize();
            double xOffset = this.EXP_WIDTH * (1 - (currWidth / maxWidth)) / 2 + this.END_SPACE;

            //this.getLayer().addChild(getTextObjectAt("Chromosome " + chromosome.getNumber(), xOffset -20, yOffset, false));

            this.getLayer().addChild(getTextObjectAt(chromosome/*chromosome.getNumber()*/+"", xOffset -50, (double)(yOffset+this.EXP_HEIGHT / 2), false,this.TEXT_SIZE));

            for (int i = 0; i < selectedExps.size(); i++) {
              String currExp = (String) selectedExps.get(i);
              //this.getLayer().addChild(getTextObjectAt(currExp,0,this.EXP_HEIGHT/2+yOffset,this.END_SPACE-10));

              //if (displayStateManager.getStateVariable("Data Plot Toggle").equals("on")) {
              if(chromNodeHash == null){
            	  System.out.println("In GraphViewPanel updateGraph(). chromNodeHash is NULL");
              }
                if (!chromNodeHash.containsKey(chromosome/*chromosome.getNumber()*/ + "," + selectedExps.get(i))) {
                  this.createChromDataGraphNode(chromosome/*chromosome.getNumber()*/, (String) selectedExps.get(i));
                }
                ChromDataGraphNode currChrNode = (ChromDataGraphNode)chromNodeHash.get(chromosome/*chromosome.getNumber()*/ + "," + selectedExps.get(i));
                if(currChrNode == null){
              	  System.out.println("In GraphViewPanel updateGraph(). currChrNode is NULL");
                }
                currChrNode.render();
                currChrNode.reverseLastTranslation();
                currChrNode.translate(xOffset, yOffset + this.EXP_HEIGHT / 2+this.TEXT_HEIGHT);
                this.getLayer().addChild(currChrNode);
                yOffset = yOffset + this.EXP_HEIGHT;
              //}

              if (displayStateManager.getStateVariable("Prediction Plot Toggle").equals("on")) {
            	  //System.out.println("Prediction Plot Toggle ON");
                yOffset = yOffset + this.EXP_PRED_SPACING;

                //if (!displayStateManager.experimentAnalyzed(currExp)) {
                //  this.getLayer().addChild(this.getTextObjectAt("Not Analyzed.",xOffset,this.PREDICTION_COMP_HEIGHT/2+yOffset,false,this.TEXT_SIZE));
                //}
                //else {
	               //ArrayList selectedResults = displayStateManager.getSelectedResultSets();
                   ResultContainer selectedResults = displayStateManager.getSelectedResultSets();
                   if(selectedResults == null){
                	   System.out.println("In GraphViewPanel updateGraph(). selectedResults is NULL");
                   }
	               //for (int m=0; m < selectedResults.size(); m++) {
	
	                  if (!chromNodeHash.containsKey(selectedResults.toString()/*(String)selectedResults.get(m)*/+","+chromosome/*chromosome.getNumber()*/ + "," + selectedExps.get(i) + "pred")) {
	                    this.createPredictionGraphNode(selectedResults.toString()/*(String)selectedResults.get(m)*/,chromosome/*chromosome.getNumber()*/, (String) selectedExps.get(i));
	                  }
	
	                  ChromPredictionsGraphNode currPredNode = (ChromPredictionsGraphNode) chromNodeHash.get((String)selectedResults.toString()/*.get(m)*/+","+chromosome/*.getNumber()*/ + "," + selectedExps.get(i) +  "pred");
	                  if(currPredNode == null){
	                  	  System.out.println("In GraphViewPanel updateGraph(). currPredNode is NULL");
	                  }	
	                  currPredNode.reverseLastTranslation();
	                  currPredNode.translate(xOffset, yOffset);
	                  this.getLayer().addChild(currPredNode);
	                //}
                //}
                yOffset = yOffset + this.PREDICTION_COMP_HEIGHT;
              }
            }
              yOffset = yOffset + this.CHROM_SPACING;
            }
          //}

        //}
      //resetZoom();
      }

      this.mouseSelectionEventHandler.clearCurrentSelection();
      //System.out.println("In GraphViewPanel updateGraph(). THE END");
      System.gc();
    }

    /**
     * Prints the current display to the specified file with the given image renderer.
     * @param filename String
     * @param imageType String
     * @return int
     */
    public int printToFile(String filename,String imageType) {
    int returnStatus=0;
    BufferedImage graphimage = (BufferedImage)this.createImage(this.getWidth(),this.getHeight());
    Graphics g = graphimage.getGraphics();

    this.paintAll(g);

    try {
      javax.imageio.ImageIO.write(graphimage, imageType,new File(filename));
    } catch (Exception exc) { exc.printStackTrace(); returnStatus=-1;}

    return returnStatus;
  }

  /**
   * Creates a gene data graph node for the given experiment, chromosome number.
   * @param chrom int
   * @param currExp String
   */
  private void createChromDataGraphNode(int chrom, String currExp) {

    ChromDataGraphNode chromNode = new ChromDataGraphNode(chrom,currExp);
    //DatasetContainer currDataset = displayStateManager.getCurrentDataset();

    //Chromosome chromosome = currDataset.getChromosome(chrom);
    int chromosome = chrom;

    float maxWidth = getMaxGeneSize(chromosome); //chromosome.maxSize();
    float currWidth = ((MultipleArrayData)this.data).getChromWidth(chromosome); //chromosome.getSize();
    //System.out.println("createChromDataGraphNode():chrom:currExp:maxWidth:currWidth " + chrom+":"+currExp+":"+maxWidth+":"+currWidth);
    chromNode.setChromWidth(this.EXP_WIDTH*(currWidth/maxWidth));
    float genescale = ((this.EXP_HEIGHT/2)/this.MAX_GENE_VALUE);
    //TODO Replace chromosome.getRatioArray with a IData function.
    float[] ratios = getRatioArray(currExp, chrom, true); //chromosome.getRatioArray(currExp,true);
    ArrayList geneNames = getGeneNamesBetweenIndices(currExp, chrom, true); //chromosome.getGeneNamesBetweenIndices(currExp,0,ratios.length-1,true);
    chromNode.addGeneNodes(ratios,genescale,this.AXIS_SCALE,geneNames);
    chromNode.addClientProperty("tooltip", currExp+", Chrom. "+chrom);
    chromNodeHash.put(chromosome/*chromosome.getNumber()*/+","+currExp,chromNode);
  }

  /**
   * Deletes the gene data graph node corresponding to the given chromosome number
   * and experiment name.
   * @param chrom int
   * @param exp String
   */
  private void deleteDataGraphNode(int chrom, String exp) {
    //ArrayList chromList = displayStateManager.getCurrentDataset().getChromosomes();

    //Chromosome chromosome = (Chromosome)chromList.get(chrom);

    if(chromNodeHash.containsKey(chrom/*chromosome.getNumber()*/+","+exp)) {
      chromNodeHash.remove(chrom/*chromosome.getNumber()*/+","+exp);
    }
  }

  /**
   * Deletes the prediction graph node corresponding to the given chromosome number
   * and experiment.
   * @param chrom int
   * @param exp String
   */
  private void deletePredictionGraphNode(int chrom, String exp) {
     //ArrayList chromList = displayStateManager.getCurrentDataset().getChromosomes();

     //Chromosome chromosome = (Chromosome)chromList.get(chrom);

     if(chromNodeHash.containsKey(chrom/*chromosome.getNumber()*/+","+exp)) {
       chromNodeHash.remove(chrom/*chromosome.getNumber()*/+","+exp+"pred");
     }
   }


   /**
    * Create a prediction graph node from the given resultSet, chromosome number
    * and experiment name.
    * @param resultSet String
    * @param chrom int
    * @param currExp String
    */
   private void createPredictionGraphNode(String resultSet, int chrom, String currExp) {
	   //System.out.println("createPredictionGraphNode(), Expr, Chr : " + currExp+":"+ chrom);
     ChromPredictionsGraphNode chromNode = new ChromPredictionsGraphNode(chrom,currExp,resultSet);
     //DatasetContainer currDataset = displayStateManager.getCurrentDataset();

     //Chromosome chromosome = currDataset.getChromosome(chrom);
     int chromosome = chrom;
     double maxWidth = getMaxGeneSize(chromosome); //chromosome.maxSize(); /* The longest gene(in BP) in any Chr */
     double currWidth = ((MultipleArrayData)this.data).getChromWidth(chromosome); //chromosome.getSize(); /* End BP minus the Start BP */

     chromNode.setChromWidth(this.EXP_WIDTH*(currWidth/maxWidth));
     float[] data = getRatioArray(currExp, chrom, true); //chromosome.getRatioArray(currExp,true);
     // Create array of Indices with original data
     ArrayList indOrig = getOrigIndArray(currExp, chrom-1);
    // if(displayStateManager.experimentAnalyzed(currExp)) {
       ArrayList segments = displayStateManager.getPredictionSegments(/*resultSet,*/currExp,chrom/*chromosome.getNumber()*/);
       //System.out.println("createPredictionGraphNode(), Expr, Chr, segment.size : " + currExp+":"+ chrom+":"+segments.size());
       for (int i = 0; i < segments.size(); i++) {
    	   
    	   SegmentInfo currSeg = (SegmentInfo) segments.get(i);
    	   //System.out.println("createPredictionGraphNode(), Expr, Chr, segment : " + currExp+":"+ chrom+":"+currSeg.getSize());
    	 /*
         int startIndex = chromosome.mapExcludeNaNIndexToRealIndex(currExp, currSeg.getStart());
         int endIndex = chromosome.mapExcludeNaNIndexToRealIndex(currExp, currSeg.getEnd());
		 */
    	 int startIndex = lookupGeneIndex(indOrig,currSeg.getStart());
         int endIndex = lookupGeneIndex(indOrig,currSeg.getEnd());
           
         double startx = (startIndex / (double) data.length) * (this.EXP_WIDTH * (currWidth / maxWidth));
         double endx = (endIndex / (double) data.length) * (this.EXP_WIDTH * (currWidth / maxWidth));
         chromNode.addPrediction(currSeg, startx, endx);

      // }
     }
    /*else {
      chromNode.addChild(this.getTextObjectAt("Run Data Analyzer to create predictions.",0,this.PREDICTION_COMP_HEIGHT/2,false));
    }*/

    chromNode.setType(ChromDataGraphNode.TYPE_PREDICTIONS);
    chromNodeHash.put(resultSet+","+chrom/*chromosome.getNumber()*/ + "," + currExp + "pred", chromNode);
    //System.out.println("createPredictionGraphNode(): chromNodeHash.size(): "+ chromNodeHash.size());
}


  /**
   * Paints gene ratios for the specified chromosome and experiment at the given
   * x and y coordinates.
   * @param chrom int
   * @param exp int
   * @param x double
   * @param y double
   * @param parent PNode
   */
  private void paintGeneRatios(int chrom, int exp, double x, double y, PNode parent){

	  //System.out.println("In GraphViewPanel paintGeneRatios()");
    ArrayList selectedExps = displayStateManager.getSelectedExperiments();
    //ArrayList chromList = displayStateManager.getCurrentDataset().getChromosomes();

    String currExp = (String)selectedExps.get(exp);
    //Chromosome chromosome = (Chromosome)chromList.get(chrom);
    int chromosome = chrom;

    //paints the axis
    PLine axisLine = new PLine();
     axisLine.setStrokePaint(Color.BLACK);
    axisLine.addPoint(0,x,y);
    axisLine.addPoint(1,this.END_SPACE+this.EXP_WIDTH-x,y);
    parent.addChild(axisLine);

    //max ratio multiplied by this should be close to component height
    float genescale = ((this.EXP_HEIGHT/2)/Math.min(getMaxGeneValue(currExp, chrom)/*chromosome.getMaxGeneValue()*/,this.MAX_GENE_VALUE));
    float[] ratios = getRatioArray(currExp, chrom, true); //chromosome.getRatioArray(currExp,true);

    PLine geneLine = null;

    for (int i = 0; i < ratios.length; i++){

      double currx = ((double)i/(double)ratios.length)*(this.EXP_WIDTH-2*x) + x;

      if (!Double.isNaN(ratios[i])) {
        Color upcolor = Color.RED;
        Color downcolor = Color.GREEN.darker();

       /* if ((chromosome.geneAtRatioIndex(i,currExp,true).isSelected)&&
            (exp == selected_experiment)){
          upcolor = Color.RED.darker();
          downcolor = Color.GREEN.darker().darker();
        }*/

        Color currColor = null;
        if (ratios[i] > 0) {
          currColor = upcolor;
        }
        else {
          currColor=downcolor;
        }

        geneLine = new PLine();
        //geneLine.setPaint(currColor);
        geneLine.setStrokePaint(currColor);
        geneLine.addPoint(0,currx,y);
        geneLine.addPoint(1,currx,y+(ratios[i] * genescale));
        parent.addChild(geneLine);

        //g2.draw(new Line2D.Double(currx, (double)kgraph_height / 2 + yOffset, currx,
         //                         (( (double)kgraph_height / 2 + yOffset) - (ratios[i] * genescale))));
      }
    }
  }


  /**
   * Resets zoom level.
   */
  public void resetZoom() {
      Rectangle2D bounds = (Rectangle2D)this.getLayer().getGlobalFullBounds().getBounds2D();
      Rectangle2D newRect = new Rectangle2D.Double(bounds.getX(),bounds.getY(),bounds.getWidth(),Math.min(bounds.getHeight()/2,this.VISIBLE_HEIGHT));
      if(newRect.getWidth() > 0) this.getCamera().animateViewToCenterBounds(newRect, true,1);
      this.resizeTextNodes(this.getLayer().getCamera(0).getViewScale());
     }


     /**
      * Generates text node node with the given string at the given x and y coordinates.
      * @param textStr String
      * @param dx double
      * @param dy double
      * @param resize boolean- indicates whether or not this node should be resized with the zoom
      * @param size int
      * @return PText
      */
     private PText getTextObjectAt(String textStr, double dx, double dy, boolean resize,int size) {

     PText text = new PText(textStr);
     text.setTextPaint(TEXT_COLOR);
     text.setFont(new Font("Arial",Font.BOLD,size));
     text.translate(dx,dy);
     this.TEXT_HEIGHT = (float)text.getHeight();
     if(resize) {
       textNodes.add(text);
       textNodeEnds.add(new Double(dx));
     }
    return text;
  }

  /**
   * Removes all text nodes from the current graph.
   */
  public void removeTextNodes() {
    this.getLayer().removeChildren(textNodes);
  }

  /**
   * Ressizes all current text nodes by the given scale factor.
   * @param scale double
   */
  public void resizeTextNodes(double scale) {
    for(int i=0; i<textNodes.size(); i++) {
      PText currNode = (PText)textNodes.get(i);
      currNode.setFont(new Font("Arial", Font.BOLD, (int)(this.TEXT_SIZE/scale)));
      Rectangle2D bounds = currNode.getGlobalFullBounds().getBounds2D();
      currNode.translate(this.END_SPACE-(bounds.getX()+bounds.getWidth()),0);
      this.TEXT_HEIGHT = (int)(this.TEXT_SIZE/scale);
    }
    this.validateTree();
  }


  /**
   * Sets all text nodes to visible.
   */
  public void addTextNodes() {
    //this.getLayer().addChildren(textNodes);
    for (int i=0; i<textNodes.size(); i++) {
      ((PText)textNodes.get(i)).setVisible(true);
    }
  }

  /**
   * Adds gene circles (for selected genes) around each gene in the supplied array.
   * @param chrom int
   * @param exp String
   * @param genes Gene[]
   */
  public void addGeneCircles(int chrom, String exp, CGHClone[] genes) {
   removeGeneCircles();
   //System.out.println("addGeneCircles(), Chr, Exp, Genes.len: " + chrom +", "+exp +","+genes.length);
   ChromDataGraphNode chromNode = (ChromDataGraphNode)chromNodeHash.get(chrom+","+exp);
   for (int i=0; i<genes.length; i++) {
     //chromNode.setTooltip(displayStateManager.getCurrentDataset().getChromosome(chrom).getGeneIndex(genes[i]),true);
	   int relCloneIndex = data.getRelativeIndex(genes[i].getSortedIndex(), chrom-1);
	   chromNode.setTooltip(relCloneIndex,true);
     currCircledGenes.add(genes[i]);
   }
   currCircledChromNode = chromNode;
 }

 /**
  * Removes all gene circles.
  */
 public void removeGeneCircles() {
   ChromDataGraphNode chromNode = currCircledChromNode;

   if(chromNode != null /*&& displayStateManager.getCurrentDataset() != null*/) {
     for (int i = 0; i < currCircledGenes.size(); i++) {
       /*chromNode.setTooltip(displayStateManager.getCurrentDataset().
                            getChromosome(chromNode.getChromosomeNumber()).
                            getGeneIndex( (Gene) currCircledGenes.get(i)), false);*/
    	 int relCloneIndex = data.getRelativeIndex(((CGHClone)currCircledGenes.get(i)).getSortedIndex(), chromNode.getChromosomeNumber()-1);
    	 chromNode.setTooltip(relCloneIndex, false);
     }
     currCircledGenes.clear();
     currCircledChromNode = null;
   }
 }

 /**
  * Returns the y offset for the specified chromosome and experiment indices.
  * @param chromIndex int
  * @param expIndex int
  * @return double
  */
 private double getOffset(int chromIndex, int expIndex) {
    double offset = 0;
    ArrayList selectedExps = displayStateManager.getSelectedExperiments();
    offset = chromIndex*(this.CHROM_SPACING +selectedExps.size()*this.EXP_HEIGHT);
    offset = offset + expIndex*this.EXP_HEIGHT;
    return offset;
  }

 /**
  * Raktim: Helper to get the log ratios of a Chr of an Expr
  * @param currExp
  * @param chr
  * @param NAs
  * @return
  */
 private float[] getRatioArray(String currExp, int chr, boolean NAs) {
	 String curExp = currExp;
	 boolean NA = true; // Not used currently TODO
	 ArrayList featuresList = data.getFeaturesList();
	 int exprInd = -1;
	 int chrIndex = chr-1;
	 //System.out.println("featuresList Size " + featuresList.size());
	 // ArayList for All Experiment Names loaded
	 for (int column = 0; column < featuresList.size(); column++){
			//String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
		 	String name = data.getFullSampleName(column);
			//System.out.println("getRatioArray() exprNames " + name);
			if (name.equals(curExp)){
				exprInd = column;
				break;
			}
	  }
	 //System.out.println("Chr value in getRatioArray(): " + chr);
	 float [] ratios = new float[data.getNumDataPointsInChrom(chrIndex)];
	 for(int clone = 0; clone < ratios.length; clone++) {
		 ratios[clone] = data.getValue(exprInd, clone, chrIndex);
	 }
	 
	 return ratios;
 }

 /**
  * Raktim: Returns all Gene Names for a Chr of an Expr.
  * @param currExp
  * @param Chr
  * @param NAs
  * @return
  */
 private ArrayList getGeneNamesBetweenIndices(String currExp, int Chr, boolean NAs){
	 ArrayList geneNames = new ArrayList();
	 boolean NA = true; //Currently ignored TODO
	 String curExpr = currExp; //Currently ignored TODO
	 int chromosomeIndex = Chr - 1;
	 int ind_St = ((MultipleArrayData)data).getChromosomeStartIndex(chromosomeIndex);
	 int ind_End = ((MultipleArrayData)data).getChromosomeEndIndex(chromosomeIndex);
	 for(int st = ind_St; st <= ind_End; st++){
		 geneNames.add(((CGHClone)data.getCloneAt(st)).getName());
	 }
	 return geneNames;
 }
 
 /**
  * 
  * @param currExp
  * @param Chr
  * @return
  */
 private float getMaxGeneValue(String currExp, int Chr) {
	 float maxRatio = 0.0f;
	 String curExp = currExp;
	 ArrayList featuresList = data.getFeaturesList();
	 int exprInd = -1;
	 //System.out.println("featuresList Size " + featuresList.size());
	 // ArayList for All Experiment Names loaded
	 for (int column = 0; column < featuresList.size(); column++){
			String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
			//System.out.println("exprNames " + name);
			if (name.equals(currExp)){
				exprInd = column;
				break;
			}
	  }
 
	 int ChrGeneCnt = data.getNumDataPointsInChrom(Chr);
	 for(int clone = 0; clone < ChrGeneCnt; clone++) {
		 if(data.getValue(exprInd, clone, Chr) > maxRatio) {
			 maxRatio = data.getValue(exprInd, clone, Chr);
		 }
	 }
	 return maxRatio;
 }
 
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
 	
	 /**
	  * ChARM CGH function
	  * Return Max Gene Ratio of a Chr
	  * @return
	  */
	 public float getMaxGeneSize(int Chr){
		 //TODO
	 	float maxsize = 2.44472064E8f;
	 	/*
	 	int chromosomeIndex = Chr - 1;
		int ind_St = ((MultipleArrayData)data).getChromosomeStartIndex(chromosomeIndex);
		int ind_End = ((MultipleArrayData)data).getChromosomeEndIndex(chromosomeIndex);
		 
	 	for(int i = ind_St; i < ind_End; i++) {
	 		CGHClone temp = (CGHClone)data.getCloneAt(i);
	 		float size = temp.getStop() - temp.getStart();
	 		if(size > maxsize) maxsize = size;
	 	}
	 	*/
	 	return maxsize;
	 }
  /**
   *
   * <p>Title: MouseSelectionEventHandler</p>
   * <p>Description: This class handles all mouse selection events on the graph display.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
private class MouseSelectionEventHandler extends PBasicInputEventHandler {
    // The rectangle that is currently getting created.
    protected PPath rectangle;

    // The mouse press location for the current pressed, drag, release sequence.
    protected Point2D pressPoint;
    protected Point2D endPoint;

    // The current drag location.
    protected Point2D dragPoint;

    private Point2D lastMovementPoint = new Point2D.Double(0,0);
    private double moveSpeed;
    private PPath tooltipPt = null;
    private Rectangle2D tooltipRect;

    private GeneGraphNode currMouseOverGene;
    private boolean mousePressed;


    public void mousePressed(PInputEvent e) {
      super.mousePressed(e);
      mousePressed=true;
      clearToolTip();

      if (displayStateManager.getGraphMode().equals("Select") && e.isRightMouseButton()) this.displayExpToolTip(e);

      if (displayStateManager.getGraphMode().equals("Select") && e.isLeftMouseButton()) {
        PLayer layer = GraphViewPanel.this.getLayer();
        GraphViewPanel.this.removeGeneCircles();
        // Initialize the locations.
        pressPoint = e.getPosition();
        dragPoint = pressPoint;

        clearCurrentSelection();
        // create a new rectangle and add it to the canvas layer so that
        // we can see it.
        rectangle = new PPath();
        rectangle.setStroke(
            new BasicStroke( (float) (1 / e.getCamera().getViewScale())));
        rectangle.setStrokePaint(GraphViewPanel.this.SELECTIONRECT_COLOR);
        layer.addChild(rectangle);

        // update the rectangle shape.
        updateRectangle();
      }
    }

    public void mouseMoved(PInputEvent e) {
      super.mouseDragged(e);
      if (displayStateManager.getGraphMode().equals("Select")) {
    	  //System.out.println("GraphViewPanel Graph Mode - Select");
        Point2D p = e.getPosition();
        this.moveSpeed = p.distance(this.lastMovementPoint);
        lastMovementPoint = p;

        if(this.moveSpeed <= 100) {
        	//System.out.println("GraphViewPanel - Calling updateToolTip()");
        	updateToolTip(e);
        }
        else {
        	//System.out.println("GraphViewPanel - Calling clearToolTip()");
        	clearToolTip();
        }
      }
    }

    public void mouseClicked(PInputEvent e) {
      super.mouseClicked(e);

      if(e.isRightMouseButton()) this.displayExpToolTip(e);
    }

    public void mouseDragged(PInputEvent e) {
      if (displayStateManager.getGraphMode().equals("Select") && rectangle != null) {
        super.mouseDragged(e);
        // update the drag point location.
        dragPoint = e.getPosition();
        // update the rectangle shape.
        updateRectangle();
      }
    }


    public void mouseReleased(PInputEvent e) {
      mousePressed=false;
      if (displayStateManager.getGraphMode().equals("Select") && rectangle != null) {
        super.mouseReleased(e);
        // update the rectangle shape.
        updateRectangle();
        endPoint = dragPoint;

        PLayer layer = GraphViewPanel.this.getLayer();
        Iterator vals = chromNodeHash.values().iterator();
        ArrayList intersections = new ArrayList();
        while(vals.hasNext()) {
          PNode curr = (PNode)vals.next();
          Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.getGlobalFullBounds().getX(),rectangle.getGlobalFullBounds().getY(),rectangle.getGlobalFullBounds().getWidth(),rectangle.getGlobalFullBounds().getHeight());
          if(curr.getGlobalFullBounds().intersects(rect)) intersections.add(curr);
        }
        //System.out.println("GraphViewPanel.mouseReleased.intersections.size(): " + intersections.size());
        clearCurrentSelection();
        if(intersections.size() > 0 ) handleIntersection(intersections);

        layer.removeChild(rectangle);

        rectangle = null;
      }
    }

    public void updateRectangle() {
      // create a new bounds that contains both the press and current
      // drag point.
      PBounds b = new PBounds();
      b.add(pressPoint);
      b.add(dragPoint);

      // Set the rectangles bounds.
      rectangle.setPathTo(b);
    }

    public void updateToolTip(PInputEvent event) {

      double cameraScale = event.getCamera().getViewScale();
      PNode n = event.getInputManager().getMouseOver().getPickedNode();
      //if (n == null) System.out.println("updateToolTip(), PNode NULL");
      if(cameraScale > 1.0) {
        clearToolTip();
        
        //while(n != null && !((Class)n.getClass()).getName().equals("charm.GeneGraphNode")) {
        while(n != null && ! (n instanceof GeneGraphNode)) {
        	//System.out.println("updateToolTip(), IF While Loop, Class Name- " + ((Class)n.getClass()).getName());
        	n = n.getParent();
        }
        //if (n == null) System.out.println("updateToolTip(), PNode NULL");
        if(n != null) {
        	//System.out.println("updateToolTip() - cameraScale > 1.0, n ! NULL");
          currMouseOverGene = (GeneGraphNode)n;
          currMouseOverGene.setMouseOver(true);
          currMouseOverGene.render();
        }
      }
      else {
    	  //System.out.println("updateToolTip() - cameraScale < 1.0");
    	  //while(n != null && !((Class)n.getClass()).getName().equals("charm.ChromDataGraphNode")) {
    	  while(n != null && !(n instanceof ChromDataGraphNode)) {
    		  //System.out.println("updateToolTip() - cameraScale < 1.0, n ! NULL");
    		  n = n.getParent();
    	  }
      }

      if(n != null) {
        String tooltipString = (String) n.getClientProperty("tooltip");
        Point2D p = event.getCanvasPosition();

        event.getPath().canvasToLocal(p, GraphViewPanel.this.getCamera());

        tooltipNode.setText(tooltipString);
        tooltipNode.setOffset(p.getX() + 8,p.getY() - 8);

        tooltipNode.setPaint(new Color(255,255,204));
        tooltipNode.setTextPaint(Color.BLACK);
      }
      else {
    	  //System.out.println("updateToolTip() - clearToolTip(), cameraScale- " + cameraScale);
    	  clearToolTip();
      }
/*
      Point2D p = event.getPosition();
      //event.getPath().canvasToLocal(p, event.getCamera());

      double cameraScale = event.getCamera().getViewScale();

      PNode n;
      if(cameraScale > 2) n = getIntersectingGeneNode((ChromGraphNode)currChromNode,tooltipRect);
      else n = currChromNode;

      if(n != null) {

        tooltipNode.setText(tooltipString);
        tooltipNode.setPaint(GraphViewPanel.this.TEXT_COLOR);
        Rectangle2D tooltipBounds = tooltipNode.getGlobalFullBounds().getBounds2D();
        tooltipNode.globalToLocal(tooltipBounds);
        tooltipNode.globalToLocal(tooltipRect);
        tooltipNode.translate(tooltipRect.getCenterX()-tooltipBounds.getX(),tooltipRect.getCenterY()-tooltipBounds.getY());
       // tooltipNode.setOffset(tooltipRect.getCenterX() + .1, tooltipRect.getCenterY() - .1);
      }
      else tooltipNode.setText("");
*/
    }

    public void displayExpToolTip(PInputEvent e) {
      PNode n = e.getInputManager().getMouseOver().getPickedNode();
      //while(n != null && !((Class)n.getClass()).getName().equals("charm.ChromDataGraphNode")) n = n.getParent();
      while(n != null && !(n instanceof ChromDataGraphNode)) 
    	  n = n.getParent();

      if(n != null) {
        clearToolTip();
        String tooltipString = (String) n.getClientProperty("tooltip");
        Point2D p = e.getCanvasPosition();

        e.getPath().canvasToLocal(p, GraphViewPanel.this.getCamera());

        tooltipNode.setText(tooltipString);
        tooltipNode.setOffset(p.getX() + 8,p.getY() - 8);

        tooltipNode.setPaint(new Color(255,255,204));
        tooltipNode.setTextPaint(Color.BLACK);
}

    }
    public void clearToolTip() {
      tooltipNode.setText("");

      if(currMouseOverGene != null) {
        currMouseOverGene.setMouseOver(false);
        currMouseOverGene.setChanged(true);
        currMouseOverGene.render();
        currMouseOverGene = null;
      }
    }

    public PNode getIntersectingChromNode(Rectangle2D rect) {
      Iterator vals = chromNodeHash.values().iterator();
      PNode intersection= null;
      boolean found = false;
      while(vals.hasNext() && !found) {
        PNode curr = (PNode)vals.next();
        if(curr.getGlobalFullBounds().intersects(rect)) {
          intersection= curr;
          found = true;
        }
      }
      return intersection;
    }


    public PNode getIntersectingGeneNode(IChromGraphNode chromIntersection,Rectangle2D rect) {
      if(chromIntersection != null && chromIntersection.getType() == IChromGraphNode.TYPE_DATA) {
        ChromDataGraphNode chromNode = (ChromDataGraphNode) chromIntersection;
        return chromNode.getIntersectingChild(rect);
      }
      else return null;
    }


    public void handleIntersection(ArrayList intersectingNodes) {
    	//System.out.println("GraphViewPanel.handleIntersection().intersectingNodes.size() " + intersectingNodes.size());
      double minDist=1e6;
      double currDist;
      int minIndex=0;
          for(int i=0; i<intersectingNodes.size(); i++) {
            Point2D nodePt = ((PNode)intersectingNodes.get(i)).getBounds().getCenter2D();
            ((PNode)intersectingNodes.get(i)).localToGlobal(nodePt);
            //System.out.println("Got PNode");
            Point2D rectPt = rectangle.getBounds().getCenter2D();
            rectangle.localToGlobal(rectPt);
            currDist = Point2D.distance(0,nodePt.getY(),0,rectPt.getY());

            if(currDist < minDist) {
              minDist=currDist; minIndex = i;
            }
          }
          //System.out.println("minIndex: " + minIndex);
          currChromGraphSelection = (IChromGraphNode)intersectingNodes.get(minIndex);
          currSelectedIndices = currChromGraphSelection.selectChildren(rectangle.getGlobalFullBounds());

          //DatasetContainer currDataset = displayStateManager.getCurrentDataset();

          int selectType = currChromGraphSelection.getType();
          if(selectType == IChromGraphNode.TYPE_DATA) {
        	  //System.out.println("handleIntersection(); ChromGraphNode.TYPE_DATA");
            ChromDataGraphNode node = (ChromDataGraphNode)currChromGraphSelection;
            //Chromosome currChrom = currDataset.getChromosome(node.getChromosomeNumber());
            int currChrom = node.getChromosomeNumber();
            String currExp = node.getExperiment();
            //System.out.println("currExp, currChrom " + currExp + " " + currChrom);
            ArrayList selectedGenes = new ArrayList();
            for(int i=0; i<currSelectedIndices.size(); i++) {
              /* Need to verify Gene index mapping */
              //Gene currGene = currChrom.geneAtRatioIndex(((Integer)currSelectedIndices.get(i)).intValue(),currExp,true);
              int rowInd = ((Integer)currSelectedIndices.get(i)).intValue();
              //System.out.println("currChrom, currExp, currSelInd " + currChrom + ", " + currExp + ", " + rowInd);
              int exprCol = ((MultipleArrayData)data).getExperimentIndex(currExp);
              int cloneInd = data.getCloneIndex(rowInd, currChrom-1);
              CGHClone currGene = (data).getCloneAt(cloneInd);
        	  float ratio = data.getRatio(exprCol, cloneInd, IData.LOG);
        	  currGene.setRatio(ratio);
              selectedGenes.add(currGene);
            }
            displayStateManager.setGraphSelectionGenes(currExp,currChrom,selectedGenes);
            //System.out.println("GraphViewPanel.handleIntersection.selectedGenes.size(): " + selectedGenes.size());
          }
          if(selectType == IChromGraphNode.TYPE_PREDICTIONS) {
        	  //System.out.println("handleIntersection(); ChromGraphNode.TYPE_PREDICTIONS, currSelectedIndices.size(): " + currSelectedIndices.size());
            ChromPredictionsGraphNode node = (ChromPredictionsGraphNode)currChromGraphSelection;
            String currExp = node.getExperiment();
            //Chromosome currChrom = currDataset.getChromosome(node.getChromosomeNumber());
            int currChrom = node.getChromosomeNumber();
            //System.out.println("currExp, currChrom " + currExp + " " + currChrom);
            ArrayList segmentList = displayStateManager.getPredictionSegments(/*node.getResultSet(),*/currExp,currChrom);

            ArrayList selectedSegments = new ArrayList();
            for(int i=0; i<currSelectedIndices.size(); i++) {
            	selectedSegments.add((SegmentInfo)segmentList.get(((Integer)currSelectedIndices.get(i)).intValue()));
            }
            displayStateManager.setGraphSelectionSegments(currExp,node.getChromosomeNumber(),selectedSegments);
            //System.out.println("GraphViewPanel.handleIntersection.selectedSegments.size(): " + selectedSegments.size());
          }
    }

    public void clearCurrentSelection() {
      if (currChromGraphSelection != null) {
        currChromGraphSelection.unselectChildren(currSelectedIndices);
        currChromGraphSelection = null;
        currSelectedIndices = null;
        displayStateManager.clearGraphSelection();
      }
    }
  }


  /**
   *
   * <p>Title: MouseZoomEventHandler</p>
   * <p>Description: This class handles all zoom events for the graph display.</p>
   * <p>Copyright: Copyright (c) 2004</p>
   * <p>Company: Princeton University</p>
   * @author Chad Myers, Xing Chen
   * @version 1.4
   */
private class MouseZoomEventHandler extends PBasicInputEventHandler {
    private static final int MIN_ZOOMRECT_SIZE = 50;

    // The rectangle that is currently getting created.
    protected PPath rectangle;

    // The mouse press location for the current pressed, drag, release sequence.
    protected Point2D pressPoint;
    protected Point2D endPoint;

    // The current drag location.
    protected Point2D dragPoint;

    public void mousePressed(PInputEvent e) {
      super.mousePressed(e);

           if(displayStateManager.getGraphMode().equals("Zoom In")) {
             PLayer layer = GraphViewPanel.this.getLayer();

             // Initialize the locations.
             pressPoint = e.getPosition();
             dragPoint = pressPoint;

             // create a new rectangle and add it to the canvas layer so that
             // we can see it.
             rectangle = new PPath();
             rectangle.setStroke(
                 new BasicStroke( (float) (1 / e.getCamera().getViewScale())));
             rectangle.setStrokePaint(GraphViewPanel.this.ZOOMRECT_COLOR);
             layer.addChild(rectangle);

             // update the rectangle shape.
             updateRectangle();
           }
    }

    public void mouseDragged(PInputEvent e) {
     super.mouseDragged(e);

      if(displayStateManager.getGraphMode().equals("Zoom In") && rectangle != null) {

        // update the drag point location.
        dragPoint = e.getPosition();

        // update the rectangle shape.
        updateRectangle();
      }
    }

    public void mouseReleased(PInputEvent e) {
      super.mouseReleased(e);

      if(displayStateManager.getGraphMode().equals("Zoom In") && rectangle != null) {
       // update the rectangle shape.
        updateRectangle();
        endPoint = dragPoint;
        PLayer layer = GraphViewPanel.this.getLayer();
        layer.removeChild(rectangle);

        if(rectangle.getWidth() < this.MIN_ZOOMRECT_SIZE || rectangle.getHeight() < this.MIN_ZOOMRECT_SIZE) {
          e.getCamera().scaleViewAboutPoint(2.0,rectangle.getX(),rectangle.getY());
        }
        else {
          double rectWidth = rectangle.getWidth();
          double rectHeight = rectangle.getHeight();
          double currWidth = e.getCamera().getWidth();
          double currHeight = e.getCamera().getHeight();
          e.getCamera().animateViewToCenterBounds(rectangle.getGlobalFullBounds(),true,70);
        }

        rectangle = null;
      }

       if(displayStateManager.getGraphMode().equals("Zoom Out")) {
         endPoint = e.getPosition();
         e.getCamera().scaleViewAboutPoint(.5,endPoint.getX(),endPoint.getY());
       }
       GraphViewPanel.this.resizeTextNodes(e.getCamera().getViewScale());
    }

    public void updateRectangle() {
            // create a new bounds that contains both the press and current
            // drag point.
            PBounds b = new PBounds();
            b.add(pressPoint);
            b.add(dragPoint);

            // Set the rectangles bounds.
            rectangle.setPathTo(b);
    }
 }

	/**
	 * CGH Modifications for MeV
	 * @param menu
	 */
	public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
		// TODO Auto-generated method stub
		
	}
	
	public void onMenuChanged(ICGHDisplayMenu menu) {
		// TODO Auto-generated method stub
		
	}
	
	public void onThresholdsChanged(ICGHDisplayMenu menu) {
		// TODO Auto-generated method stub
		
	}
	
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		return this;
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
		return 0;
	}
	
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
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
		this.framework = framework;
		
	}
	
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub
		
	}
	
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub
		
	}
}
