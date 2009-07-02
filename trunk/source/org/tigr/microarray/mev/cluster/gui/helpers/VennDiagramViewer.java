package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.util.ArrayList;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class VennDiagramViewer extends JPanel implements IViewer {
	private int[][] clusters = new int[3][];
	private int[][] regionIndices = new int[7][];
	private String[] clusterNames = new String[3];
	private Color[] clusterColors = new Color[3];
	private boolean[] circleSelected ={false, false, false};
	private int[] posInfluence ={0, 0, 0};
	private int[] negInfluence ={0, 0, 0};
	private boolean[] regionSelected ={false, false, false, false, false, false, false, false};
	private Experiment experiment;
    private IFramework framework;
	private int totalElements;
	private int exptID = 0;
	private int zoom = 3;
	private float pval = 2.0f;
	private int clusterCount=3;
	private int[] intersects;
    Color unselected = new Color(200,200,255);
    Color selectedRegion = new Color(170,170,255);
    private boolean showHighlights = true;
    
	private Font values_font ;
	private Font class_font ;
	private Font pval_font ;
	
	private Circle[] circles = new Circle[3];
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
	protected JPopupMenu popup;
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String ZOOM_IN = "zoom-in";
    protected static final String ZOOM_OUT = "zoom-out";
    protected static final String HIGHLIGHT_CIRCLES = "highlight-circles";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
	
	/**
	 * Constructs a <code>Venn Diagram Viewer</code> for insertion into ClusterTable
	 *
	 * @param experiment the data of an experiment.
	 */
	public VennDiagramViewer(IFramework fm, boolean sampleVD, Cluster[] clusterArray) {
		setClusters(clusterArray);
	    framework= fm;
        this.experiment = framework.getData().getExperiment();
    	if (sampleVD)
    		totalElements = experiment.getNumberOfSamples();
    	else
    		totalElements = experiment.getNumberOfGenes();
    	init();
    }
	/**
	 * Constructs a Venn Diagram Viewer
	 *
	 *
	 */
	public VennDiagramViewer(int[][] clusterArray){
		this(clusterArray, null, null);
	}
	/**
	 * Constructs a Venn Diagram Viewer
	 *
	 *
	 */
	public VennDiagramViewer(int[][] clusterArray, String[] clusterNames){
		this(clusterArray, clusterNames, null);
	}
	/**
	 * Constructs a <code>Venn Diagram Viewer</code> for testing
	 *
	 *
	 */
	public VennDiagramViewer(int[][] clusterArray, String[] clusterNames, Color[] clusterColors) {
		this(clusterArray, clusterNames, clusterColors, 25);
    }

	/**
	 * The Master Constructor <code>Venn Diagram Viewer</code>
	 *
	 *
	 */
	public VennDiagramViewer(int[][] clusterArray, String[] clusterNames, Color[] clusterColors, int totalEls) {
		setClusters(clusterArray, clusterNames, clusterColors);
        totalElements = totalEls;
        init();
    }
	
	private void init(){
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        addMouseListener(listener);
        addMouseMotionListener(listener);
        refreshData();
	}
	public void setClusters(Cluster[] clusterArray){
		if (clusterArray==null){
    		clusterCount=1;
    		return;
    	}
		clusterCount = Math.min(Math.max(1, clusterArray.length),4);//sets clusterCount to 1,2,3,or 4
		if (clusterCount==1||clusterCount==4)
			return;
		circles[2] = new Circle();//added to prevent null-pointers from 2-cluster diagrams
		for (int i=0; i<clusterCount; i++){
			clusters[i] = clusterArray[i].getIndices();
			clusterColors[i] = clusterArray[i].getClusterColor();
			clusterNames[i] = clusterArray[i].getClusterLabel();
			if (clusterNames[i].length()<1)
				clusterNames[i] = "(Cluster "+(i+1)+")";
			circles[i] = new Circle();
			circles[i].setTag(clusterNames[i]);
		}
		refreshData();
	}
    public void setClusters(int[][] clusterArray, String[] names, Color[] colors){
    	if (clusterArray==null){
    		clusterCount=1;
    		return;
    	}
		clusterCount = Math.min(Math.max(1, clusterArray.length),4);//sets clusterCount to 1,2,3,or 4
		if (clusterCount==1||clusterCount==4)
			return;
		circles[2] = new Circle();//added to prevent null-pointers from 2-cluster diagrams
		for (int i=0; i<clusterCount; i++){
			clusters[i] = clusterArray[i];
			clusterColors[i] = colors==null? Color.black:colors[i];
			clusterNames[i] = names==null? "Cluster "+(i+1):names[i];
			circles[i] = new Circle();
			circles[i].setTag(clusterNames[i]);
		}
		refreshData();
    }
    
	private void setTwoClusterPValue(){
		pval = (float)cumulativeBinomialDistributionFunction(
				this.totalElements, 
				intersects[0], 
				this.clusters[0].length, 
				this.clusters[1].length );		
	}
	private void find2ClusterInfluences(){
		posInfluence[0]=0;
		posInfluence[1]=0;
		negInfluence[0]=0;
		negInfluence[1]=0;
		if (regionSelected[0]&&regionSelected[1]&&regionSelected[2]){
			posInfluence[0]=4;
			posInfluence[1]=4;
			negInfluence[0]=0;
			negInfluence[1]=0;
		}
		if (regionSelected[0]&&regionSelected[1]&&!regionSelected[2]){
			posInfluence[0]=4;
			posInfluence[1]=0;
			negInfluence[0]=0;
			negInfluence[1]=0;
		}
		if (regionSelected[0]&&!regionSelected[1]&&regionSelected[2]){
			posInfluence[0]=0;
			posInfluence[1]=4;
			negInfluence[0]=0;
			negInfluence[1]=0;
		}
		if (!regionSelected[0]&&regionSelected[1]&&regionSelected[2]){
			posInfluence[0]=4;
			posInfluence[1]=4;
			negInfluence[0]=-4;
			negInfluence[1]=-4;
		}
		if (regionSelected[0]&&!regionSelected[1]&&!regionSelected[2]){
			posInfluence[0]=2;
			posInfluence[1]=2;
			negInfluence[0]=0;
			negInfluence[1]=0;
		}
		if (!regionSelected[0]&&regionSelected[1]&&!regionSelected[2]){
			posInfluence[0]=2;
			posInfluence[1]=0;
			negInfluence[0]=0;
			negInfluence[1]=-2;
		}
		if (!regionSelected[0]&&!regionSelected[1]&&regionSelected[2]){
			posInfluence[0]=0;
			posInfluence[1]=2;
			negInfluence[0]=-2;
			negInfluence[1]=0;
		}
		if (!regionSelected[0]&&!regionSelected[1]&&!regionSelected[2]){
			posInfluence[0]=0;
			posInfluence[1]=0;
			negInfluence[0]=0;
			negInfluence[1]=0;
		}
	}
	private void find3ClusterInfluences(){
		posInfluence[0]=0;
		posInfluence[1]=0;
		posInfluence[2]=0;
		negInfluence[0]=0;
		negInfluence[1]=0;
		negInfluence[2]=0;
		if (regionSelected[0]&&!regionSelected[1]) posInfluence[2]++;//center borders
		if (regionSelected[0]&&!regionSelected[2]) posInfluence[1]++;
		if (regionSelected[0]&&!regionSelected[3]) posInfluence[0]++;
		if (regionSelected[1]&&!regionSelected[4]) posInfluence[1]++;//inner borders
		if (regionSelected[1]&&!regionSelected[5]) posInfluence[0]++;
		if (regionSelected[2]&&!regionSelected[4]) posInfluence[2]++;
		if (regionSelected[2]&&!regionSelected[6]) posInfluence[0]++;
		if (regionSelected[3]&&!regionSelected[5]) posInfluence[2]++;
		if (regionSelected[3]&&!regionSelected[6]) posInfluence[1]++;
		if (regionSelected[4]) posInfluence[0]++;//outer borders
		if (regionSelected[5]) posInfluence[1]++;
		if (regionSelected[6]) posInfluence[2]++;
		if (regionSelected[1]&&!regionSelected[0]) negInfluence[2]--;//center borders
		if (regionSelected[2]&&!regionSelected[0]) negInfluence[1]--;
		if (regionSelected[3]&&!regionSelected[0]) negInfluence[0]--;
		if (regionSelected[4]&&!regionSelected[1]) negInfluence[1]--;//inner borders
		if (regionSelected[5]&&!regionSelected[1]) negInfluence[0]--;
		if (regionSelected[4]&&!regionSelected[2]) negInfluence[2]--;
		if (regionSelected[6]&&!regionSelected[2]) negInfluence[0]--;
		if (regionSelected[5]&&!regionSelected[3]) negInfluence[2]--;
		if (regionSelected[6]&&!regionSelected[3]) negInfluence[1]--;
	}
    private int[] getThreeClusterOverlap(int[][] clusters){
    	ArrayList<Integer> clusterA = new ArrayList<Integer>();
    	for (int i=0; i<clusters[0].length; i++){
    		clusterA.add(clusters[0][i]);
    	}
    	ArrayList<Integer> clusterB = new ArrayList<Integer>();
    	for (int i=0; i<clusters[1].length; i++){
    		clusterB.add(clusters[1][i]);
    	}
    	ArrayList<Integer> clusterC = new ArrayList<Integer>();
    	for (int i=0; i<clusters[2].length; i++){
    		clusterC.add(clusters[2][i]);
    	}
    	ArrayList<Integer>[] indices = new ArrayList[7];
    	for (int i=0; i<7; i++)
    		indices[i] = new ArrayList<Integer>();
    	int[] overlaps = new int[8];
    	
        for(int i=0; i<totalElements; i++){
    		boolean a=false;
    		boolean b=false;
    		boolean c=false;
    		if (clusterA.contains(i))
    			a=true;
    		if (clusterB.contains(i))
    			b=true;
    		if (clusterC.contains(i))
    			c=true;
    		if (a&b&c){
    			overlaps[0]++;
    			indices[0].add(i);
    		}
    		if (a&b&(!c)){
    			overlaps[1]++;
    			indices[1].add(i);
    		}
    		if (a&(!b)&c){
    			overlaps[2]++;
    			indices[2].add(i);
    		}
    		if ((!a)&b&c){
    			overlaps[3]++;
    			indices[3].add(i);
    		}
    		if (a&(!b)&(!c)){
    			overlaps[4]++;
    			indices[4].add(i);
    		}
    		if ((!a)&b&(!c)){
    			overlaps[5]++;
    			indices[5].add(i);
    		}
    		if ((!a)&(!b)&c){
    			overlaps[6]++;
    			indices[6].add(i);
    		}
    		if ((!a)&(!b)&(!c)){
    			overlaps[7]++;
    		}
    	}
    	for (int i=0; i<7; i++){
    		regionIndices[i] = new int[indices[i].size()];
    		for (int j=0; j<indices[i].size(); j++)
    			regionIndices[i][j] = indices[i].get(j);
    	}
    	return overlaps;
    }
    private int[] getTwoClusterOverlap(int[][] clusters){
    	ArrayList<Integer> clusterA = new ArrayList<Integer>();
    	for (int i=0; i<clusters[0].length; i++){
    		clusterA.add(clusters[0][i]);
    	}
    	ArrayList<Integer> clusterB = new ArrayList<Integer>();
    	for (int i=0; i<clusters[1].length; i++){
    		clusterB.add(clusters[1][i]);
    	}
    	ArrayList<Integer>[] indices = new ArrayList[7];
    	for (int i=0; i<7; i++)
    		indices[i] = new ArrayList<Integer>();
    	int[] overlaps = new int[4];
    	
        for(int i=0; i<totalElements; i++){
    		boolean a=false;
    		boolean b=false;
    		if (clusterA.contains(i))
    			a=true;
    		if (clusterB.contains(i))
    			b=true;
    		if (a&b){
    			overlaps[0]++;
    			indices[0].add(i);
    		}
    		if (a&(!b)){
    			overlaps[1]++;
    			indices[1].add(i);
    		}
    		if ((!a)&b){
    			overlaps[2]++;
    			indices[2].add(i);
    		}
    		if ((!a)&(!b)){
    			overlaps[3]++;
    			indices[3].add(i);
    		}
    	}
    	for (int i=0; i<7; i++){
    		regionIndices[i] = new int[indices[i].size()];
    		for (int j=0; j<indices[i].size(); j++)
    			regionIndices[i][j] = indices[i].get(j);
    	}
    	return overlaps;
    }

	  
	private void refreshData(){
		for (int i=0; i<7; i++)
			regionSelected[i] = false;
		if (clusterCount==3){
			intersects = getThreeClusterOverlap(clusters);
    		find3ClusterInfluences();
		}else if(clusterCount==2){
			intersects = getTwoClusterOverlap(clusters);
			setTwoClusterPValue();
    		find2ClusterInfluences();
		}else{
			return;
		}
		this.setBackground(Color.white);
		
		values_font = new java.awt.Font("Courier", Font.BOLD, 18);
		class_font = new java.awt.Font("Courier", Font.BOLD, 24);
		pval_font = new java.awt.Font("Courier", Font.BOLD, 12);
		setCircles();
      }

	private void setCircles(){
		int circleWidth = 60;
		int xloc = 25;
		int yloc = 12;
		
		circles[0].x = zoom*xloc;
		circles[0].y = zoom*yloc;       
		circles[1].x = zoom*(xloc+(circleWidth/2));      
		circles[1].y = zoom*yloc;
		circles[2].x = zoom*(xloc+(circleWidth/4));
		circles[2].y = (int)(zoom*(yloc+(float)circleWidth*0.433013f));   //sqrt(3)/2 *width/2
        circles[0].diameter  = zoom*circleWidth;
        circles[1].diameter  = zoom*circleWidth;
        circles[2].diameter  = zoom*circleWidth;

        circles[0].label_x = zoom*xloc;
        circles[0].label_y = zoom*(yloc-3);
        circles[1].label_x = zoom*(xloc+(5*circleWidth/4));  
        circles[1].label_y = zoom*(yloc-3);
        circles[2].label_x = zoom*(xloc+(circleWidth/2));   
        circles[2].label_y = zoom*(yloc+(5*circleWidth/3));

        circles[0].center_x = (circles[0].x + (circles[0].x + circles[0].diameter))/2   ;
        circles[0].center_y = (circles[0].y + (circles[0].y + circles[0].diameter))/2   ;
        circles[1].center_x = (circles[1].x + (circles[1].x + circles[1].diameter))/2   ;
        circles[1].center_y = (circles[1].y + (circles[1].y + circles[1].diameter))/2   ;
        circles[2].center_x = (circles[2].x + (circles[2].x + circles[2].diameter))/2   ;
        circles[2].center_y = (circles[2].y + (circles[2].y + circles[2].diameter))/2   ;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if (clusterCount==4){
		        g.setColor(new Color(0, 0, 128));
		        g.setFont(ERROR_FONT);
		        g.drawString("Too Many Clusters", 10, 30);
		        return;
		}
		if (clusterCount==1){
		        g.setColor(new Color(0, 0, 128));
		        g.setFont(ERROR_FONT);
		        g.drawString("Select 2 or 3 Clusters", 10, 30);
		        return;
		}
		if (clusterCount==3){
			if (showHighlights)
				draw3ClusterSelectedArcs(g);
			draw3ClusterSelectedRegions(g);
		} else{
			if (showHighlights)
				draw2ClusterSelectedArcs(g);
		    draw2ClusterSelectedRegions(g);
			
		}
		drawOutlines(g);
		drawValues(g);
	}

      private void draw2ClusterSelectedArcs(Graphics g){
	        g.setColor(Color.white);
		    int arcwidth = 6;
		    g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,240);
		    g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,240,240);
		    if(posInfluence[0]>0){
		    	g.setColor(getPosArcColor(posInfluence[0]));
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,240);
		    }
		    if(posInfluence[1]>0){
		    	g.setColor(getPosArcColor(posInfluence[1]));
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,240,240);
		    }
		    if(negInfluence[0]<0){
		    	g.setColor(getNegArcColor(negInfluence[0]));
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,240);
		    }
		    if(negInfluence[1]<0){
		    	g.setColor(getNegArcColor(negInfluence[0]));
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,240,240);
		    }
		    
		    if(negInfluence[0]<0&&posInfluence[0]>0){
		    	g.setColor(Color.gray);
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,240);
		    }
		    if(negInfluence[1]<0&&posInfluence[1]>0){
		    	g.setColor(Color.gray);
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,240,240);
		    }
      }
      private void draw3ClusterSelectedArcs(Graphics g){
	        g.setColor(Color.white);
		    int arcwidth = 6;
		    g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,180);
		    g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,300,180);
		    g.fillArc(circles[2].x-(arcwidth/2)*zoom,circles[2].y-(arcwidth/2)*zoom,circles[2].diameter+arcwidth*zoom,circles[2].diameter+arcwidth*zoom,180,180);
		    if(posInfluence[0]>0){
		    	g.setColor(getPosArcColor(posInfluence[0]));//new Color(255,255-64*posInfluence[0]+1,255-64*posInfluence[0]+1));
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,180);
		    }
		    if(posInfluence[1]>0){
		    	g.setColor(getPosArcColor(posInfluence[1]));//new Color(255,255-64*posInfluence[1]+1,255-64*posInfluence[1]+1));
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,300,180);
		    }
		    if(posInfluence[2]>0){
		    	g.setColor(getPosArcColor(posInfluence[2]));//new Color(255,255-64*posInfluence[2]+1,255-64*posInfluence[2]+1));
		    	g.fillArc(circles[2].x-(arcwidth/2)*zoom,circles[2].y-(arcwidth/2)*zoom,circles[2].diameter+arcwidth*zoom,circles[2].diameter+arcwidth*zoom,180,180);
		    }
		    if(negInfluence[0]<0){
		    	g.setColor(getNegArcColor(negInfluence[0]));//new Color(255+64*negInfluence[0]+1,255+64*negInfluence[0]+1,255));
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,180);
		    }
		    if(negInfluence[1]<0){
		    	g.setColor(getNegArcColor(negInfluence[1]));//new Color(255+64*negInfluence[1]+1,255+64*negInfluence[1]+1,255));
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,300,180);
		    }
		    if(negInfluence[2]<0){
		    	g.setColor(getNegArcColor(negInfluence[2]));//new Color(255+64*negInfluence[2]+1,255+64*negInfluence[2]+1,255));
		    	g.fillArc(circles[2].x-(arcwidth/2)*zoom,circles[2].y-(arcwidth/2)*zoom,circles[2].diameter+arcwidth*zoom,circles[2].diameter+arcwidth*zoom,180,180);
		    }
		    
		    if(negInfluence[0]<0&&posInfluence[0]>0){
		    	g.setColor(Color.gray);
		    	g.fillArc(circles[0].x-(arcwidth/2)*zoom,circles[0].y-(arcwidth/2)*zoom,circles[0].diameter+arcwidth*zoom,circles[0].diameter+arcwidth*zoom,60,180);
		    }
		    if(negInfluence[1]<0&&posInfluence[1]>0){
		    	g.setColor(Color.gray);
		    	g.fillArc(circles[1].x-(arcwidth/2)*zoom,circles[1].y-(arcwidth/2)*zoom,circles[1].diameter+arcwidth*zoom,circles[1].diameter+arcwidth*zoom,300,180);
		    }
		    if(negInfluence[2]<0&&posInfluence[2]>0){
		    	g.setColor(Color.gray);
		    	g.fillArc(circles[2].x-(arcwidth/2)*zoom,circles[2].y-(arcwidth/2)*zoom,circles[2].diameter+arcwidth*zoom,circles[2].diameter+arcwidth*zoom,180,180);
		    }
      }   
      private Color getPosArcColor(int influence){
    	  return new Color(255,255-63*influence,255-63*influence);
      }
      private Color getNegArcColor(int influence){
    	  return new Color(255+63*influence,255+63*influence,255);
      }   
      private void draw2ClusterSelectedRegions(Graphics g){
	        g.setColor(regionSelected[1]? selectedRegion:unselected);
	        g.fillOval(circles[0].x, circles[0].y, circles[0].diameter,circles[0].diameter) ;

	        g.setColor(regionSelected[2]? selectedRegion:unselected);
	        g.fillOval(circles[1].x, circles[1].y, circles[1].diameter,circles[1].diameter) ;
	        
	        //center
	        g.setColor((regionSelected[0])? selectedRegion:unselected);
	        g.fillArc(circles[0].x, circles[0].y,circles[0].diameter,circles[0].diameter,300,120);
	        g.fillArc(circles[1].x, circles[1].y,circles[1].diameter,circles[1].diameter,120,120);
      }
      private void draw3ClusterSelectedRegions(Graphics g){
	        g.setColor(regionSelected[4]? selectedRegion:unselected);
	        g.fillOval(circles[0].x, circles[0].y, circles[0].diameter,circles[0].diameter) ;

	        g.setColor(regionSelected[5]? selectedRegion:unselected);
	        g.fillOval(circles[1].x, circles[1].y, circles[1].diameter,circles[1].diameter) ;
	        
	        if (clusterCount==3){
		        g.setColor(regionSelected[6]? selectedRegion:unselected);
		        g.fillOval(circles[2].x, circles[2].y,circles[2].diameter,circles[2].diameter);
	        }
	        //middles
	        g.setColor((regionSelected[1])? selectedRegion:unselected);
	        g.fillArc(circles[0].x, circles[0].y,circles[0].diameter,circles[0].diameter,300,120);
	        g.fillArc(circles[1].x, circles[1].y,circles[1].diameter,circles[1].diameter,120,120);
	        g.setColor((regionSelected[3])? selectedRegion:unselected);
	        g.fillArc(circles[1].x, circles[1].y,circles[1].diameter,circles[1].diameter,180,120);
	        g.fillArc(circles[2].x, circles[2].y,circles[2].diameter,circles[2].diameter,0,120);
	        g.setColor((regionSelected[2])? selectedRegion:unselected);
	        g.fillArc(circles[0].x, circles[0].y,circles[0].diameter,circles[0].diameter,240,120);
	        g.fillArc(circles[2].x, circles[2].y,circles[2].diameter,circles[2].diameter,60,120);
	        
	        //center
	        g.setColor((regionSelected[0])? selectedRegion:unselected);
	        g.fillArc(circles[0].x, circles[0].y,circles[0].diameter,circles[0].diameter,300,60);
	        g.fillArc(circles[1].x, circles[1].y,circles[1].diameter,circles[1].diameter,180,60);
	        g.fillArc(circles[2].x, circles[2].y,circles[2].diameter,circles[2].diameter,60,60);
      }
      private void drawOutlines(Graphics g){
		  Graphics2D g2 = (Graphics2D) g;
		  g.setColor(Color.black);
		  g2.setStroke(new BasicStroke(4.0f));
		  g2.drawOval(circles[0].x, circles[0].y, circles[0].diameter,circles[0].diameter) ;
		  g2.drawOval(circles[1].x, circles[1].y, circles[1].diameter,circles[1].diameter) ;
		  if (clusterCount==3)
		        g2.drawOval(circles[2].x, circles[2].y,circles[2].diameter,circles[2].diameter);
		  
		  g2.setFont(class_font);
		
		  // Draw cluster labels
		  g2.setColor(clusterColors[0]);
		  g2.drawString(circles[0].label, circles[0].label_x -getGraphics().getFontMetrics().stringWidth(circles[0].label)/2, circles[0].label_y );//30, 30 );
		  g2.setColor(clusterColors[1]);
		  g2.drawString(circles[1].label, circles[1].label_x -getGraphics().getFontMetrics().stringWidth(circles[1].label)/2, circles[1].label_y );//300, 30 );
		  if (clusterCount==3){
		        g2.setColor(clusterColors[2]);
		        g2.drawString(circles[2].label, circles[2].label_x -getGraphics().getFontMetrics().stringWidth(circles[2].label)/2, circles[2].label_y );//150, 300 );
		  }
      }
      private void drawValues(Graphics g){
	        g.setColor(Color.black);
	        g.setFont(values_font);
	        if (clusterCount==3){
	        	int[][] coordinates = get3ClusterValueCoordinates();
	        	for (int i=0; i<8; i++)
			        g.drawString(Integer.toString(intersects[i]), coordinates[0][i], coordinates[1][i]);
	        }
	        if (clusterCount==2){
	        	//draw the intersection values
	        	int[][] coordinates = get2ClusterValueCoordinates();
	        	for (int i=0; i<4; i++)
			        g.drawString(Integer.toString(intersects[i]), coordinates[0][i]-getGraphics().getFontMetrics().stringWidth(Integer.toString(intersects[i]))/2, coordinates[1][i]+6);

	        	//draw the p-values
		        String pvalstring;
		        pval = Math.round(10000*pval)/10000f;
		        int maxlength = Float.toString(pval).length();
		        pvalstring = pval<.0001? "p-value < .0001":"p-value = "+Float.toString(pval).substring(0, Math.min(6, maxlength));
		        g.setColor(Color.blue);
		        g.setFont(pval_font);
		        g.drawString(pvalstring, coordinates[0][3]-25, coordinates[1][3]+25 );//pvalue
	        }    	  
      }
      
	private int[][] get3ClusterValueCoordinates(){
		int[][]vals = new int[2][8];
		vals[0][0] =zoom*66;	//middle x
		vals[1][0] =zoom*51;	//middle y
		vals[0][1] =zoom*66;	//inner top x
		vals[1][1] =zoom*31;	//inner top y
		vals[0][2] =zoom*48;	//inner left x
		vals[1][2] =zoom*60;	//inner left y
		vals[0][3] =zoom*87;	//inner right x
		vals[1][3] =zoom*60;	//inner right y
		vals[0][4] =zoom*40;	//outer left x
		vals[1][4] =zoom*32;	//outer left y
		vals[0][5] =zoom*95;	//outer right x
		vals[1][5] =zoom*32;	//outer right y
		vals[0][6] =zoom*66;	//outer bottom x
		vals[1][6] =zoom*83;	//outer bottom y
		vals[0][7] =zoom*125;	//excluded x
		vals[1][7] =zoom*61;	//excluded y
		return vals;
	}
	private int[][] get2ClusterValueCoordinates(){
		

        
		int[][]vals = new int[2][4];
		vals[0][0] =circles[0].center_x+circles[0].diameter/4;	//middle x
		vals[1][0] =circles[0].center_y;	//middle y
		vals[0][1] =circles[0].center_x-circles[0].diameter/4;	//left x
		vals[1][1] =circles[0].center_y;	//left y
		vals[0][2] =circles[1].center_x+circles[0].diameter/4;	//right x
		vals[1][2] =circles[0].center_y;	//right y
		vals[0][3] =circles[0].center_x+circles[0].diameter/4;	//excluded x
		vals[1][3] =circles[0].center_y+3*circles[0].diameter/4;	//excluded y
		return vals;
	}
      
      private static double cumulativeBinomialDistributionFunction(int total, int intersect, int cl1, int cl2){
    	  double value = 0;
    	  for (int i=0; i<intersect; i++){
    		  value = value + binomialDistributionFunction(i, cl2, (double)cl1/(double)total);
    	  }
    	  return 1-value;
      }
      private static double binomialDistributionFunction(int k, int n, double p){
    	  double value =1;
    	  int max = Math.max(k, n-k);
    	  double whichp;
    	  if (max==k){
    		  whichp = p;
    	  }else{
    		  whichp=1-p;
    	  }
    	  for (int i=n; i>max; i--){
    		  value = value*i;
    		  value = value*(1-whichp);
    		  value = value/(i-max);
    	  }
    	  value = value*Math.pow(whichp, max);
    	  return value;
      }

	private class Circle {
	      int x,y, diameter, label_x, label_y, center_x, center_y;
	      String label;
	
	      private Circle() {
	        label = "no label";
	      }
	
	      private Circle(String tag) {
	        label = tag;
	      }
	      void setTag(String tag) {
	    	label = tag;
	      }
	      boolean pointInCircle(int x, int y) {
	    	  if (((((x - center_x)*(x - center_x)) + ((y - center_y)*(y - center_y))) - (diameter/2 * diameter/2)) > 0 )
    		  return false;
    	  else
    		  return true;
	      }
	}
    
    public JComponent getContentComponent(){
    	return this;
    }
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent(){
    	return null;
    }
    
    /**
     * Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent(){
    	return null;
    }  

    /**
     * Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex){
    	return null;
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework){
    }
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data){
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu){
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected(){
    }
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed(){
    }
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage(){
    	return null;
    }
    
    /**
     * Returns the viewer's clusters or null
     */
    public int[][] getClusters(){
    	return clusters;
    }
    
    /**
     *  Returns the viewer's experiment or null
     */
    public Experiment getExperiment(){
        return experiment;
    }
    
    /**
     * Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType(){
        return -1;
    }
    
    /**
     * state-saving
     * Sets the Experiment field for this Viewer.  Used when restoring state.  
     *
     */
    public void setExperiment(Experiment e){
    }
    
    /**
    * state-saving
    * Returns the ID value for the Experiment associated with this viewer.
    */
    public int getExperimentID(){
		return this.exptID;
    }
    
    /**
    * state-saving
    * Sets the ID value for the Experiment associated with this IViewer
    */
    public void setExperimentID(int id){
    }
    
    /**
     * This method returns an expression that represents the constructor call that 
     * will result in the re-creation of this object's state. See the state-saving 
     * developer documentation for more details. 
     * @return An Expression that can be used to restore the object's state. 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
				new Object[]{experiment, ClusterWrapper.wrapClusters(clusters)});
    }

	public int getRegion(int x, int y) {
		if (clusterCount==2){
			boolean circleA = circles[0].pointInCircle(x, y);
			boolean circleB = circles[1].pointInCircle(x, y);
			if( circleA  & circleB)
				return 0 ;
			else if( circleA  & !circleB)
				return 1;
			else if( !circleA & circleB)
				return 2;
			else if( !circleA & !circleB)
				return 7;
		}
		boolean circleA = circles[0].pointInCircle(x, y);
		boolean circleB = circles[1].pointInCircle(x, y);
		boolean circleC = circles[2].pointInCircle(x, y);
		if( circleA  & circleB  & circleC)
			return 0 ;
		else if( circleA  & circleB & !circleC)
			return 1;
		else if( circleA & !circleB  & circleC)
			return 2;
		else if( !circleA & circleB & circleC)
			return 3;
		else if( circleA  & !circleB  & !circleC)
			return 4;
		else if( !circleA & circleB  & !circleC)
			return 5;
		else if( !circleA  & !circleB & circleC)
			return 6;
		else
			return 7;
	}
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener,ActionListener {
	    
	    public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand();
	        if (command.equals(ZOOM_IN)){
	        	zoom++;
	        	refreshData();
	        	repaint();
	        }
	        if (command.equals(ZOOM_OUT)){
	        	zoom--;
	        	zoom = Math.max(1, zoom);
	        	refreshData();
	        	repaint();
	        }
	        if (command.equals(SAVE_CLUSTER_CMD)){
	        	storeCluster();
	        }
	        if (command.equals(HIGHLIGHT_CIRCLES)){
	        	setHighlightCircles();
	        }
	        
	    }
        public void mouseClicked(MouseEvent event) {
        }
        
        public void mouseMoved(MouseEvent event) {
        }
        
        public void mouseEntered(MouseEvent event) {
        }
        
        public void mouseExited(MouseEvent event) {
        }
        
        public void mouseDragged(MouseEvent event) {
        }
        /** Called when the mouse has been pressed. */
        public void mousePressed(MouseEvent event) {
        }

        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
	        maybeShowPopup(event);
        	if (event.isPopupTrigger())
        		return;
        	if (getRegion(event.getX(),event.getY())==7){ //outside circles
        		clearSelections();
        		refreshData();
        		repaint();
        		return;
        	}
        	if(circles[0].pointInCircle(event.getX(), event.getY()))
        		circleSelected[0] = !circleSelected[0];
        	if(circles[1].pointInCircle(event.getX(), event.getY()))
        		circleSelected[1] = !circleSelected[1];
        	if(circles[2].pointInCircle(event.getX(), event.getY()))
        		circleSelected[2] = !circleSelected[2];
        	if (clusterCount==3){
	        	regionSelected[getRegion(event.getX(),event.getY())]=!regionSelected[getRegion(event.getX(),event.getY())];
	        	circleSelected[0] = regionSelected[0]||regionSelected[1]||regionSelected[2]||regionSelected[4];
	        	circleSelected[1] = regionSelected[0]||regionSelected[1]||regionSelected[3]||regionSelected[5];
	        	circleSelected[2] = regionSelected[0]||regionSelected[2]||regionSelected[3]||regionSelected[6];
	        	find3ClusterInfluences();
        	}
        	if (clusterCount==2){
	        	regionSelected[getRegion(event.getX(),event.getY())]=!regionSelected[getRegion(event.getX(),event.getY())];
	        	circleSelected[0] = regionSelected[0]||regionSelected[1]||regionSelected[2]||regionSelected[4];
	        	circleSelected[1] = regionSelected[0]||regionSelected[1]||regionSelected[3]||regionSelected[5];
	        	circleSelected[2] = false;
	        	find2ClusterInfluences();
        	}
        	repaint();
        }
	    private void maybeShowPopup(MouseEvent e) {
	    	if (e.isPopupTrigger()){
	    		popup.show(e.getComponent(), e.getX(), e.getY());
	    		repaint();
	    	}
	    }
    }
    private void clearSelections(){
    	for (int i=0; i<regionSelected.length; i++)
    		regionSelected[i]=false;
    	for (int i=0; i<circleSelected.length; i++)
    		circleSelected[i]=false;
    }
	/**
	 * Creates a popup menu.
	 */
	protected JPopupMenu createJPopupMenu(ActionListener listener) {
	    JPopupMenu popup = new JPopupMenu();
	    addMenuItems(popup, listener);
	    return popup;
	}

    /**
     * Adds viewer specific menu items.
     */
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Zoom In", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(ZOOM_IN);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Zoom Out", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(ZOOM_OUT);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Store Selected Cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem(showHighlights ? "Hide Selected Circles Highlights":"Show Selected Circles Highlights", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(HIGHLIGHT_CIRCLES);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    private int[] getSelectedIndices(){
    	ArrayList<Integer> indicesAL = new ArrayList<Integer>();
    	for(int i=0; i<7; i++){
    		if (regionSelected[i]){
    			for(int j=0; j<regionIndices[i].length; j++){
    				indicesAL.add(regionIndices[i][j]);
    			}
    		}
    	}
    	int[] indices = new int[indicesAL.size()];
    	for (int i=0; i<indices.length; i++)
    		indices[i] = indicesAL.get(i);
    	return indices;
    }
    /**
     *  Sets cluster color
     */
    private void storeCluster(){
    	try{
    		framework.storeCluster(getSelectedIndices(), experiment, ClusterRepository.GENE_CLUSTER);
    	}catch (Exception e){
    		int[] a = this.getSelectedIndices();
    		for (int i=0; i<a.length; i++)
    			System.out.print(a[i]+"\t");
    	}
    }

    /**
     *  Sets cluster color
     */
    private void setHighlightCircles(){
    	showHighlights = !showHighlights;
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        repaint();
    }
	
    public static void main(String[] args){
    	JDialog jp3 = new JDialog();
    	JDialog jp2 = new JDialog();
    	int[][] clusters3 = { {1,2,3,5,9,11,12,13,14,15,16,17,18,19},{1,2,3,5,7,8,12},{1,2,3,4,7,11}};
    	int[][] clusters2 = { {1,2,3,5,9,11,12,13,14,15,16,17,18,19},{1,2,3,5,7,8,12}};
    	String[] clnames = {"Joe", "Moe", "Snow"};
    	VennDiagramViewer vdv3 = new VennDiagramViewer(clusters3, clnames);
    	VennDiagramViewer vdv2 = new VennDiagramViewer(clusters2, clnames);
    	jp2.add(vdv2);
    	jp2.setBackground(Color.white);
    	jp2.setModal(true);
    	jp2.setAlwaysOnTop(true);
    	jp2.setSize(459, 450);
    	jp3.add(vdv3);
    	jp3.setBackground(Color.white);
    	jp3.setModal(true);
    	jp3.setAlwaysOnTop(true);
    	jp3.setSize(459, 450);
    	jp3.setVisible(true);
    	jp2.setVisible(true);
    	System.exit(0);
    	
    }

}
