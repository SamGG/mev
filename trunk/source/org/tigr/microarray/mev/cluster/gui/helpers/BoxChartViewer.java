package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class BoxChartViewer extends JPanel implements IViewer {
	private static final long serialVersionUID = 1L;
	private JDialog scaleDialog;
	private boolean scaleChanged;
	private int[][] clusters;
	private String[] clusterNames;
	private Color[] clusterColors;
	private Experiment experiment;
    private IFramework framework;
	private int exptID = 0;
	private int clusterCount;
    Color unselected = new Color(200,200,255);
    Color selectedRegion = new Color(170,170,255);
    
	private int chartHeight = 250;
	private int chartTopHeight = 75;
	private int chartIncrement = 100;
	private int chartleft = 100;
	private int chartGap = 120;
	private boolean showGridlines = false;
	private boolean autoScale = true;
	private boolean aggregateGeneCluster = false;
	private float fixedTop = 12;
	private float fixedBottom = 0;
	
	protected JPopupMenu popup;
	private JComboBox geneCB;
	private JComboBox geneAnnotationCB;
	private JComboBox geneClusterCB;
	private JComboBox chartTypeCB;
	private JComboBox geneOrClusterCB;
	private float[][] expressionAverages;
	private float[][] expressionSDs;
	private float[][] expressionMedian;
	private float[][] expressionQ1;
	private float[][] expressionQ3;
	private float[][] expressionIQR;
	private float[][] expressionLowerWhisker;
	private float[][] expressionUpperWhisker;
	private float[][][] upperOutliers;
	private float[][][] lowerOutliers;
	private boolean isSingleGene;
	private boolean isBoxPlot;
	private JMenuItem menuItemAuto;
	private JMenuItem menuItemSetScale;
	private JMenuItem menuItemFixScale;
	private JLabel annotLabel;
	private JLabel geneIDlabel;
	private JMenuItem aggGenClusterMenuItem;
	private JCheckBox aggregateCheckBox;
	private JScrollBar jScrollBar = new JScrollBar();
	private int multigeneCount;
	private String[] chartTitles;
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String ZOOM_IN = "zoom-in";
    protected static final String ZOOM_OUT = "zoom-out";
    protected static final String SAVE_DATA_CMD = "save-cluster-cmd";
    protected static final String TOGGLE_GRIDLINES_CMD = "toggle-gridlines-cmd";
    protected static final String AUTOSCALE_CMD = "auto-scale-cmd";
    protected static final String SET_SCALE_CMD = "set-scale-cmd";
    protected static final String FIX_SCALE_CMD = "fix-scale-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
	private static final String AGG_GENE_CLUSTER_CMD = "aggregate-gene-cluster";
	
	/**
	 * Constructs a <code>Box/Bar Chart Viewer</code> for insertion into ClusterTable
	 *
	 * @param experiment the data of an experiment.
	 */
	public BoxChartViewer(IFramework fm, Cluster[] clusterArray) {
	    framework= fm;
        this.experiment = framework.getData().getExperiment();
		setClusters(clusterArray);
        populateGeneListComboBoxes();
        generateSettingsComponents();
        createComponentLayout();         
    	init();
    }
	
	private void init(){
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
		this.setBackground(Color.white);
        addMouseListener(listener);
        addMouseMotionListener(listener);
	}
	
	private void createComponentLayout() {
		GridBagConstraints gbc = new GridBagConstraints();	       
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;        
        gbc.gridx=0;
        this.add(new JLabel("  Chart Type: "),gbc);
        gbc.gridx++;
        this.add(chartTypeCB,gbc);
        gbc.gridx++;
        this.add(new JLabel("  Gene Range: "),gbc);
        gbc.gridx++;
        this.add(geneOrClusterCB,gbc);
        gbc.gridx++;
        this.add(annotLabel,gbc);
        gbc.gridx++;
    	this.add(geneAnnotationCB,gbc);
        gbc.gridx++;
    	this.add(geneClusterCB,gbc);
        gbc.gridx++;
        this.add(geneIDlabel,gbc);
        gbc.gridx++;
    	this.add(geneCB,gbc);
        gbc.gridx++;
    	this.add(aggregateCheckBox,gbc);
        gbc.gridx = 0;		
	}
	
	private void generateSettingsComponents() {
		String[] chartTypes = {"Box Plot","Bar Graph"};
		chartTypeCB = new JComboBox(chartTypes);
		chartTypeCB.addActionListener(new ActionListener(){		

			public void actionPerformed(ActionEvent e) {
				isBoxPlot = chartTypeCB.getSelectedIndex()==0;
				repaint();	
			}
		});

		String[] geneOrCluster = {"Single Gene","Gene Cluster"};
		geneOrClusterCB = new JComboBox(geneOrCluster);
		geneOrClusterCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (geneOrClusterCB.getSelectedIndex()==0){
					annotLabel.setText("  Annotation Field: ");
					aggGenClusterMenuItem.setEnabled(false);
					geneClusterCB.setVisible(false);
			        aggregateCheckBox.setVisible(false);
					geneAnnotationCB.setVisible(true);
					geneIDlabel.setVisible(true);
					geneCB.setVisible(true);
					isSingleGene = true;
				} else {
					if (framework.getClusterRepository(0)==null || framework.getClusterRepository(0).isEmpty()){
			            JOptionPane.showMessageDialog(null, "The Gene Cluster Repository is empty\nPlease create a Gene Cluster to continue...", "Error", JOptionPane.WARNING_MESSAGE);
			            geneOrClusterCB.setSelectedIndex(0);
						isSingleGene = true;
			            return;
					}
					annotLabel.setText("  Select Cluster: ");
					aggGenClusterMenuItem.setEnabled(true);
					geneClusterCB.setVisible(true);
			        aggregateCheckBox.setVisible(true);
					geneAnnotationCB.setVisible(false);
					geneIDlabel.setVisible(false);
					geneCB.setVisible(false);	
					isSingleGene = false;				
				}
				repaint();				
			}        	
        });
        aggregateCheckBox = new JCheckBox("Aggregate Gene Cluster");
        aggregateCheckBox.setSelected(false);
        aggregateCheckBox.setBackground(Color.white);
        aggregateCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
    			aggregateGeneCluster = !aggregateGeneCluster;
	        	if (aggregateGeneCluster){
	        		aggGenClusterMenuItem.setText("Un-aggregate Gene Cluster");
	        	} else {
	        		aggGenClusterMenuItem.setText("Aggregate Gene Cluster");
	        	}        		
	        	repaint();				
			}        	
        });
        aggregateCheckBox.setVisible(false);

        geneIDlabel = new JLabel("  Gene ID: ");
        annotLabel = new JLabel("  Annotation Field: ");
        isBoxPlot = chartTypeCB.getSelectedIndex()==0;
        isSingleGene = geneOrClusterCB.getSelectedIndex()==0;
	}
	
	private void populateGeneListComboBoxes() {
		geneClusterCB = new JComboBox();
		geneClusterCB.removeAllItems();
		for (int i=0; i<framework.getClusterRepository(0).size(); i++){
			if (framework.getClusterRepository(0).getCluster(i+1)==null)
				break;
			Cluster cluster = framework.getClusterRepository(0).getCluster(i+1);
			geneClusterCB.addItem("Cluster #: "+cluster.getSerialNumber()+", "+cluster.getClusterLabel());
		}
		geneClusterCB.setVisible(false);
		geneClusterCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				repaint();				
			}        	
        });
		geneAnnotationCB = new JComboBox(framework.getData().getAllFilledAnnotationFields());
		geneAnnotationCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (chartTypeCB.getSelectedIndex()>1) //this method only applies for single gene
					return;
				geneCB.removeAllItems();
				int selected = Math.max(geneAnnotationCB.getSelectedIndex(), 0);
				DefaultComboBoxModel cbm = new DefaultComboBoxModel(framework.getData().getAnnotationList(framework.getData().getAllFilledAnnotationFields()[selected]));
				geneCB.setModel(cbm); 
			}        	
        });
		geneCB = new JComboBox(framework.getData().getAnnotationList(framework.getData().getAllFilledAnnotationFields()[0]));
		geneCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				repaint();				
			}        	
        });
		
	}
	public void setClusters(Cluster[] clusterArray){
		if (clusterArray==null||clusterArray.length==0)
			clusterArray = new Cluster[1];
		clusterCount = clusterArray.length;
		clusterNames = new String[clusterCount];
		clusterColors = new Color[clusterCount];
		clusters = new int[clusterCount][];
		for (int i=0; i<clusterCount; i++){
			if (clusterArray[i]==null){
				int numSamps = experiment.getNumberOfSamples();
				clusters[i] = new int[numSamps];
				for (int j=0; j<numSamps; j++){
					clusters[i][j] = j;
				}
				clusterColors[i] = Color.white;
				clusterNames[i] = "All Samples";
			} else {
				clusters[i] = clusterArray[i].getIndices();
				clusterColors[i] = clusterArray[i].getClusterColor();
				clusterNames[i] = clusterArray[i].getClusterLabel();
				if (clusterNames[i].length()<1)
					clusterNames[i] = "(Cluster "+(i+1)+")";
			}
		}
	}
    

	
	public void paint(Graphics g) {
		super.paint(g);
		if (clusterCount==0)
			return;
		boolean ismultigene = !isSingleGene;
		Graphics2D g2 = (Graphics2D)g;
		multigeneCount = 1;
		Cluster cluster = null;
		if (ismultigene){
			try{
				int begin = this.geneClusterCB.getSelectedItem().toString().indexOf(":");
				int end = this.geneClusterCB.getSelectedItem().toString().indexOf(",");
				String sn = this.geneClusterCB.getSelectedItem().toString().substring(begin+2,end);
				cluster = framework.getClusterRepository(0).getCluster(Integer.parseInt(sn));
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("Gene Cluster Problem");
				return;
			}
			if (cluster==null||cluster.getSize()<1)
				return;

			if (aggregateGeneCluster)
				multigeneCount = 1;
			else
				multigeneCount = cluster.getSize();
		}
		int chartTop = chartTopHeight;

		lowerOutliers = new float[multigeneCount][][];
		upperOutliers = new float[multigeneCount][][];
		expressionSDs = new float[multigeneCount][];
		expressionAverages = new float[multigeneCount][];
		expressionMedian = new float[multigeneCount][];
		expressionQ1 = new float[multigeneCount][];
		expressionQ3 = new float[multigeneCount][];
		expressionIQR = new float[multigeneCount][];
		expressionLowerWhisker = new float[multigeneCount][];
		expressionUpperWhisker = new float[multigeneCount][];
		chartTitles = new String[multigeneCount];
		int maxString = 0;
		for (int i=0; i<clusterNames.length; i++){
			maxString = Math.max(maxString, g.getFontMetrics().stringWidth(clusterNames[i]));
		}
		chartGap = maxString+50;
		for (int multigeneIndex=0; multigeneIndex < multigeneCount; multigeneIndex++){
			if (ismultigene){
				if (aggregateGeneCluster){					
					getChartData(cluster.getIndices(), multigeneIndex);
					chartTitles[multigeneIndex] = cluster.getClusterLabel();
				} else {
					getChartData(cluster.getIndices()[multigeneIndex], multigeneIndex);
					chartTitles[multigeneIndex] = framework.getData().getElementAnnotation(cluster.getIndices()[multigeneIndex], framework.getData().getFieldNames()[0])[0];
				}
			} else {
				getChartData(geneCB.getSelectedIndex(), multigeneIndex);
				chartTitles[multigeneIndex] = (String)geneCB.getSelectedItem();
			}
			float maxHeight = 0;
			float minHeight = Float.POSITIVE_INFINITY;
			for (int i=0; i<clusterCount; i++){
				if (this.isBoxPlot){
					for (int j=0; j<
					this.upperOutliers
					[multigeneIndex]
					 [i]
					  .length; j++){
						maxHeight = Math.max(upperOutliers[multigeneIndex][i][j], maxHeight);					
					}
					maxHeight = Math.max(expressionUpperWhisker[multigeneIndex][i], maxHeight);
				}
				else{
					maxHeight = Math.max(expressionAverages[multigeneIndex][i]+this.expressionSDs[multigeneIndex][i], maxHeight);
				}
			}
	
			for (int i=0; i<clusterCount; i++){
				if (this.isBoxPlot){
					for (int j=0; j<this.lowerOutliers[multigeneIndex][i].length; j++){
						minHeight = Math.min(lowerOutliers[multigeneIndex][i][j], minHeight);					
					}
					minHeight = Math.min(expressionLowerWhisker[multigeneIndex][i], minHeight);
				}
				else
					minHeight = 0f;//Math.min(expressionAverages[i], minHeight);
			}
			maxHeight++;
			float topgridline;
			float bottomgridline;
			if (this.autoScale){
				topgridline = (int)maxHeight+(int)maxHeight%2;
				bottomgridline =(int)minHeight-(int)minHeight%2-2;
				if (!isBoxPlot)
					bottomgridline = 0f;
				fixedTop = topgridline;
				fixedBottom = bottomgridline;
			} else {
				topgridline = fixedTop;
				bottomgridline = fixedBottom;
				if (!isBoxPlot)
					bottomgridline = 0f;
			}
			
			float gridrange = topgridline - bottomgridline;
			//draw everything
			//grid	
			g2.setColor(Color.black);
			g2.drawLine(chartleft-5, chartHeight+chartTop, chartleft+clusterCount*chartIncrement, chartHeight+chartTop);//horizontal line bottom
			g2.drawLine(chartleft, chartTop, chartleft, chartHeight+chartTop+5);//vertical line left
			g2.setColor(Color.gray);
			g2.drawLine(chartleft-7, chartTop, chartleft, chartTop);//horizontal line ticktop
			g2.drawLine(chartleft-5, chartTop+chartHeight/4, chartleft, chartTop+chartHeight/4);//horizontal line tick75%
			g2.drawLine(chartleft-7, chartTop+chartHeight/2, chartleft, chartTop+chartHeight/2);//horizontal line tickMid
			g2.drawLine(chartleft-5, chartTop+3*chartHeight/4, chartleft, chartTop+3*chartHeight/4);//horizontal line tick25%
			if (showGridlines){
				g2.setColor(Color.gray);
				g2.drawLine(chartleft, chartTop, chartleft+chartIncrement*clusterCount, chartTop);//horizontal line grid
				g2.drawLine(chartleft, chartTop+chartHeight/4, chartleft+chartIncrement*clusterCount, chartTop+chartHeight/4);//horizontal line grid
				g2.drawLine(chartleft, chartTop+chartHeight/2, chartleft+chartIncrement*clusterCount, chartTop+chartHeight/2);//horizontal line grid
				g2.drawLine(chartleft, chartTop+3*chartHeight/4, chartleft+chartIncrement*clusterCount, chartTop+3*chartHeight/4);//horizontal line grid
			}
	
			g2.setColor(Color.black);
			g2.drawString(Float.toString(bottomgridline + gridrange), chartleft-40, chartTop+3);//vertical axis label
			g2.drawString(Float.toString(bottomgridline + gridrange*3f/4f), chartleft-40, (chartTop+3)+chartHeight/4);//vertical axis label
			g2.drawString(Float.toString(bottomgridline + gridrange/2f), chartleft-40, (chartTop+3) + chartHeight/2);//vertical axis label
			g2.drawString(Float.toString(bottomgridline + gridrange/4f), chartleft-40, (chartTop+3) + (3*chartHeight/4));//vertical axis label		
			g2.drawString(Float.toString(bottomgridline), chartleft-40, (chartTop+3) + (chartHeight));//vertical axis label		
			
			Font font = g2.getFont();
			g2.setFont(new Font("Helvetica", Font.BOLD,  16));
			g2.drawString(chartTitles[multigeneIndex], chartleft + chartIncrement/2, chartTop-15);//Gene/group Name label
			g2.setFont(font);
			
			
			g2.rotate(-Math.PI/2);
			g2.drawString("Expression", -((chartTop+3)+2*chartHeight/3), (chartleft-60)); //y-axis Label
			g2.rotate(Math.PI/2);
	
			for (int i=0; i<this.clusterCount; i++){	
				int xorigin = (int)((.25f+(float)i)*chartIncrement)+chartleft;
				int yorigin = chartHeight+chartTop;
				int barWidth = chartIncrement/2;
				
				int barHeight = (int)((expressionAverages[multigeneIndex][i])*((float)chartHeight)/gridrange);
				int errorBarHeight = (int)(expressionSDs[multigeneIndex][i]*((float)chartHeight)/gridrange);
				
				int boxBottom = (int)((-bottomgridline+expressionQ1[multigeneIndex][i])*((float)chartHeight)/gridrange);
				int boxTop = (int)((-bottomgridline+expressionQ3[multigeneIndex][i])*((float)chartHeight)/gridrange);
				int boxMedian = (int)((-bottomgridline+expressionMedian[multigeneIndex][i])*((float)chartHeight)/gridrange);
				int boxLowerWhisker = (int)((-bottomgridline+expressionLowerWhisker[multigeneIndex][i])*((float)chartHeight)/gridrange);
				int boxUpperWhisker = (int)((-bottomgridline+expressionUpperWhisker[multigeneIndex][i])*((float)chartHeight)/gridrange);
	
				g2.setColor(Color.gray);
				g2.drawLine(xorigin + 3*barWidth/2, yorigin, xorigin + 3*barWidth/2, yorigin + 5);	//vertical tickmark
				g2.setColor(Color.black);
				g2.rotate(Math.PI/2);
				g2.drawString(clusterNames[i], (yorigin+5), -(xorigin + barWidth/2)); //cluster names
				g2.rotate(-Math.PI/2);		
				
				if (!isBoxPlot){	//bar graph
					g2.setColor(clusterColors[i]);
					g2.fillRect(xorigin, yorigin-barHeight, barWidth, barHeight);//box
					g2.setColor(Color.black);
					g2.drawRect(xorigin, yorigin-barHeight, barWidth, barHeight);//box outline
					g2.drawLine(xorigin + barWidth/2, yorigin - barHeight - errorBarHeight, xorigin + barWidth/2, yorigin - barHeight);	//vertical errorbar
					g2.drawLine(xorigin + barWidth/4, yorigin - barHeight - errorBarHeight, xorigin + 3*barWidth/4, yorigin - barHeight - errorBarHeight); //horizontal error bar
					
				} else if (isBoxPlot){ //box plot
					g2.setColor(clusterColors[i]);
					g2.fillRect(xorigin, yorigin-boxTop, barWidth, boxTop-boxBottom); //box	
					g2.setColor(Color.black);
					g2.drawRect(xorigin, yorigin-boxTop, barWidth, boxTop-boxBottom); //box	outline
					g2.drawLine(xorigin, yorigin-boxMedian, xorigin+barWidth, yorigin-boxMedian); //median line
					g2.drawLine(xorigin + barWidth/2, yorigin - boxTop, xorigin + barWidth/2, yorigin - boxUpperWhisker);	//vertical whisker upper
					g2.drawLine(xorigin + barWidth/2, yorigin - boxBottom, xorigin + barWidth/2, yorigin - boxLowerWhisker);	//vertical whisker lower
					g2.drawLine(xorigin + barWidth/4, yorigin - boxUpperWhisker, xorigin + 3*barWidth/4, yorigin - boxUpperWhisker);	//horizontal whisker upper
					g2.drawLine(xorigin + barWidth/4, yorigin - boxLowerWhisker, xorigin + 3*barWidth/4, yorigin - boxLowerWhisker);	//horizontal whisker lower
					
					//draw outliers
					int circleWidth = 6;
					for (int j=0; j<upperOutliers[multigeneIndex][i].length; j++){
						g2.drawOval(xorigin + barWidth/2 - circleWidth/2, yorigin-(int)((float)((-bottomgridline+upperOutliers[multigeneIndex][i][j]))*((float)chartHeight)/gridrange)-circleWidth/2, circleWidth, circleWidth);
					}
					for (int j=0; j<lowerOutliers[multigeneIndex][i].length; j++){
						g2.drawOval(xorigin + barWidth/2 - circleWidth/2, yorigin-(int)((float)((-bottomgridline+lowerOutliers[multigeneIndex][i][j]))*((float)chartHeight)/gridrange)-circleWidth/2, circleWidth, circleWidth);
					}
					
				} else {
					System.out.println("unknown chart type");
				}	
			}	
			
			chartTop = chartTop + (chartHeight+chartGap); //moves top down for next graph
		}
		jScrollBar.setUnitIncrement(chartHeight+chartGap);
		int viewerHeight = chartTop;//chartTop is now the bottom of graph
		this.setSize(new Dimension(this.getWidth(), viewerHeight));
		this.setPreferredSize(new Dimension(this.getWidth(),viewerHeight));
	}
	
	/**
	 * Gathers data for a single gene, uses getChartData(int[] geneIndices)
	 * @param geneIndex
	 */	
	private void getChartData(int geneIndex, int multigeneIndex) {
		int[] indices = new int[1];
		indices[0] = geneIndex;
		getChartData(indices, multigeneIndex);
	}
	
	/**
	 * Gathers data for a set of genes
	 * @param geneIndices
	 */
	private void getChartData(int[] geneIndices, int multigeneIndex) {
    	try{
    		lowerOutliers[multigeneIndex] = new float[clusterCount][];
    		upperOutliers[multigeneIndex] = new float[clusterCount][];
			expressionSDs[multigeneIndex] = new float[clusterCount];
			expressionAverages[multigeneIndex] = new float[clusterCount];
			expressionMedian[multigeneIndex] = new float[clusterCount];
			expressionQ1[multigeneIndex] = new float[clusterCount];
			expressionQ3[multigeneIndex] = new float[clusterCount];
			expressionIQR[multigeneIndex] = new float[clusterCount];
			expressionLowerWhisker[multigeneIndex] = new float[clusterCount];
			expressionUpperWhisker[multigeneIndex] = new float[clusterCount];
		  for (int i=0; i<clusterCount; i++){
			  float average=0;
			  float sum = 0;
			  int membership=0;
			  for (int j=0; j<clusters[i].length; j++){
				  for (int k=0; k<geneIndices.length; k++){
					  average = average + experiment.get(geneIndices[k], clusters[i][j]);
					  membership++;
				  }
			  }
			  average = average/(float)membership;
			  expressionAverages[multigeneIndex][i] = average;
			  for (int j=0; j<clusters[i].length; j++){
				  for (int k=0; k<geneIndices.length; k++){
					  sum = sum + (float)Math.pow(experiment.get(geneIndices[k], clusters[i][j]) - average, 2);  	
				  }
			  }
			  sum = sum/(float)membership;
			  expressionSDs[multigeneIndex][i] = (float)Math.sqrt(sum);
			  
			  //get box plot stuff
			  float[] expression = new float[clusters[i].length*geneIndices.length];
			  int expindex = 0;
			  for (int j=0; j<clusters[i].length; j++){
				  for (int k=0; k<geneIndices.length; k++){
					  expression[expindex++] = (experiment.get(geneIndices[k], clusters[i][j]));
				  }
			  }
			  Arrays.sort(expression);
			  expressionMedian[multigeneIndex][i] = getMedian(expression);
			  
			  float[] lower = new float[expression.length/2];
			  float[] upper = new float[expression.length/2];
			  for (int j=0; j<lower.length; j++){
				  lower[j] = expression[j];
				  upper[upper.length-1-j] = expression[expression.length-1-j];
			  }
			  expressionQ1[multigeneIndex][i] = getMedian(lower);
			  expressionQ3[multigeneIndex][i] = getMedian(upper);
			  expressionIQR[multigeneIndex][i] = expressionQ3[multigeneIndex][i] - expressionQ1[multigeneIndex][i];
			  expressionLowerWhisker[multigeneIndex][i] = expressionQ1[multigeneIndex][i] - 1.5f*expressionIQR[multigeneIndex][i];
			  expressionUpperWhisker[multigeneIndex][i] = expressionQ3[multigeneIndex][i] + 1.5f*expressionIQR[multigeneIndex][i];

			  int upperCount = 0;
			  int lowerCount = 0;
			  for (int index=0; index < expression.length; index++){
				  if(expression[index]<expressionLowerWhisker[multigeneIndex][i])
					  lowerCount++;
				  if(expression[index]>expressionUpperWhisker[multigeneIndex][i])
					  upperCount++;
			  }
			  lowerOutliers[multigeneIndex][i] = new float[lowerCount];
			  upperOutliers[multigeneIndex][i] = new float[upperCount];
			  for (int j=0; j<lowerOutliers[multigeneIndex][i].length; j++){
				  lowerOutliers[multigeneIndex][i][j] = expression[j];
			  }
			  for (int j=0; j<upperOutliers[multigeneIndex][i].length; j++){
				  upperOutliers[multigeneIndex][i][j] = expression[expression.length-1-j];
			  }
		  }
    	} catch (Exception e){
    			System.out.println("data problem");
    			e.printStackTrace();
    		  for (int i=0; i<clusterCount; i++){
    			  expressionAverages[multigeneIndex][i] = (float)Math.random();
    			  expressionSDs[multigeneIndex][i] = (float)Math.random()/10f;
    		  }
    	}
	}
    
	private float getMedian(float[] expression) {
		return (expression[expression.length/2] + expression[(expression.length-1)/2])/2f;
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
		geneClusterCB.removeAllItems();
		for (int i=0; i<framework.getClusterRepository(0).size(); i++){
			if (framework.getClusterRepository(0).getCluster(i+1)==null)
				break;
			Cluster cluster = framework.getClusterRepository(0).getCluster(i+1);
			geneClusterCB.addItem("Cluster #: "+cluster.getSerialNumber()+", "+cluster.getClusterLabel());
		}
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

    private void saveData() {
		File file;		
		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());	
		
		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();			
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(file));
				
				//comment row
				Date currDate = new Date(System.currentTimeMillis());			
				String dateString = currDate.toString();;
				String userName = System.getProperty("user.name");
				
				pw.println("# MeV");
				pw.println("# User: "+userName+" Save Date: "+dateString);
				pw.println("#");

				pw.println("# "+clusterCount+" Clusters");
				pw.println("# Annotation Field: "+geneAnnotationCB.getSelectedItem());

				for (int j=0; j<this.multigeneCount; j++){
					if (isSingleGene)
						pw.println("Gene: "+geneCB.getSelectedItem());
					else {
						if (aggregateGeneCluster){					
							pw.println("Gene Cluster: "+chartTitles[j]);
						} else {
							pw.println("Gene: "+chartTitles[j]);
						}
					}
						
					pw.print("\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.clusterNames[i]+"\t");
					}
					pw.println();
					
					pw.print("Genes Present\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.clusters[i].length+"\t");
					}
					pw.println();
	
					pw.print("Expression Mean\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionAverages[j][i]+"\t");
					}
					pw.println();
	
					pw.print("Expression Median\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionMedian[j][i]+"\t");
					}
					pw.println();
	
					pw.print("Expression Standard Deviation\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionSDs[j][i]+"\t");
					}
					pw.println();
	
					pw.print("Expression Q1\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionQ1[j][i]+"\t");
					}
					pw.println();
					
					pw.print("Expression Q3\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionQ3[j][i]+"\t");
					}
					pw.println();
	
					pw.print("Expression IQR\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionIQR[j][i]+"\t");
					}
					pw.println();
					
					pw.print("Upper Whisker\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionUpperWhisker[j][i]+"\t");
					}
					pw.println();
	
					pw.print("Lower Whisker\t");
					for (int i=0; i<clusterCount; i++){
						pw.print(this.expressionLowerWhisker[j][i]+"\t");
					}
					pw.println();
					pw.println();
				}
				
				pw.flush();
				pw.close();	
			}catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
	}
    

	private boolean setScale() {
		scaleDialog = new JDialog();
		scaleDialog.setTitle("Enter Y-Axis Limits");
		scaleDialog.setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		scaleChanged = false;
		GridBagLayout gbl = new GridBagLayout();
		scaleDialog.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		JTextField upper = new JTextField(""+fixedTop);
		JTextField lower = new JTextField(""+fixedBottom);
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				scaleChanged = true;
				scaleDialog.dispose();
			}			
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				scaleChanged = false;
				scaleDialog.dispose();
			}			
		});
		upper.setPreferredSize(new Dimension(40,20));
		lower.setPreferredSize(new Dimension(40,20));
		lower.setEnabled(this.isBoxPlot);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
//		scaleDialog.add(new JLabel("Enter Y-Axis labels"), gbc);
		gbc.gridwidth = 1;
		gbc.gridy++;
		scaleDialog.add(new JLabel("Upper Limit: "), gbc);
		gbc.gridx++;
		scaleDialog.add(upper, gbc);
		gbc.gridx--;
		gbc.gridy++;
		scaleDialog.add(new JLabel("Lower Limit: "), gbc);
		gbc.gridx++;
		scaleDialog.add(lower, gbc);
		gbc.gridy++;
		scaleDialog.add(button, gbc);
		gbc.gridx--;
		scaleDialog.add(cancel, gbc);
		scaleDialog.pack();
		scaleDialog.setModal(true);
		scaleDialog.setVisible(true);
		if (scaleChanged){
			try{
				float ft = Float.parseFloat(upper.getText());
				float fb = Float.parseFloat(lower.getText());
				if ((ft-fb)<=0){
					JOptionPane.showMessageDialog(null, "<html>Error -- Upper limit must be greater than lower limit</html>", "Validation Error", JOptionPane.ERROR_MESSAGE);
					return setScale();						
				}
				fixedTop = ft;
				fixedBottom = fb;
				
			}catch (Exception e){
				JOptionPane.showMessageDialog(null, "<html>Error -- Invalid inputs</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
				return setScale();
			}
		}
		return false;//if cancel or close is clicked
		
	}
    
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener,ActionListener {
	    
	    public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand();
	        if (command.equals(ZOOM_IN)){
	        	chartHeight = (chartHeight*3)/2;
	        	chartIncrement = (chartIncrement*3)/2;
//	    		jScrollBar.setUnitIncrement(chartHeight+chartGap);
	        	repaint();
	        }
	        if (command.equals(ZOOM_OUT)){
	        	chartHeight = (chartHeight*2)/3;
	        	chartIncrement = (chartIncrement*2)/3;
//	    		jScrollBar.setUnitIncrement(chartHeight+chartGap);
	        	repaint();
        	}
	        if (command.equals(SAVE_DATA_CMD)){
	        	saveData();
	        }
	        if (command.equals(TOGGLE_GRIDLINES_CMD)){
	        	showGridlines = !showGridlines;
	        	repaint();
	        }

	        if (command.equals(SET_SCALE_CMD)){
	        	setScale();
        		if (scaleChanged){
        	        menuItemAuto.setEnabled(true);
        			autoScale = false;
        		}   	
	        	repaint();
	        }
	        if (command.equals(FIX_SCALE_CMD)){
	        	float min = Float.MAX_VALUE;
	        	float max = Float.MIN_VALUE;
	        	for (int i=0; i<experiment.getNumberOfGenes(); i++){
	        		for (int j=0; j<experiment.getNumberOfSamples(); j++){
	        			if (min>experiment.get(i, j))
	        				min = experiment.get(i, j);
	        			if (max<experiment.get(i, j))
	        				max = experiment.get(i, j);
	        		}
	        	}

				fixedTop = (int)max+1;
				fixedBottom = (int)min;
    	        menuItemAuto.setEnabled(true);
    			autoScale = false;
	        	repaint();
	        }
	        if (command.equals(AGG_GENE_CLUSTER_CMD)){
    			aggregateGeneCluster = !aggregateGeneCluster;
	        	if (aggregateGeneCluster){
	        		aggGenClusterMenuItem.setText("Un-aggregate Gene Cluster");
	        		aggregateCheckBox.setSelected(true);
	        	} else {
	        		aggGenClusterMenuItem.setText("Aggregate Gene Cluster");
	        		aggregateCheckBox.setSelected(false);
	        	}        		
	        	repaint();				
	        }

	        if (command.equals(AUTOSCALE_CMD)){
    	        menuItemAuto.setEnabled(false);
        		autoScale = true;	        	
	        	repaint();
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
        	repaint();
        }
	    private void maybeShowPopup(MouseEvent e) {
	    	if (e.isPopupTrigger()){
	    		popup.show(e.getComponent(), e.getX(), e.getY());
	    		repaint();
	    	}
	    }
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
        menuItem = new JMenuItem("Zoom In", GUIFactory.getIcon("zoom_in.gif"));
        menuItem.setActionCommand(ZOOM_IN);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Zoom Out", GUIFactory.getIcon("zoom_out.gif"));
        menuItem.setActionCommand(ZOOM_OUT);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Save Data", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_DATA_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Toggle Gridlines", GUIFactory.getIcon("empty16.gif"));
        menuItem.setActionCommand(TOGGLE_GRIDLINES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItemAuto = new JMenuItem("Autoscale Y-Axis", GUIFactory.getIcon("Y_range_expand.gif"));
        menuItemAuto.setActionCommand(AUTOSCALE_CMD);
        menuItemAuto.addActionListener(listener);
        menuItemAuto.setEnabled(false);
        menu.add(menuItemAuto);

        menuItemSetScale = new JMenuItem("Set Y-Axis", GUIFactory.getIcon("Y_range_expand.gif"));
        menuItemSetScale.setActionCommand(SET_SCALE_CMD);
        menuItemSetScale.addActionListener(listener);
        menu.add(menuItemSetScale);

        menuItemFixScale = new JMenuItem("Fix Y-Axis to Global Range", GUIFactory.getIcon("Y_range_expand.gif"));
        menuItemFixScale.setActionCommand(FIX_SCALE_CMD);
        menuItemFixScale.addActionListener(listener);
        menu.add(menuItemFixScale);

        aggGenClusterMenuItem = new JMenuItem("Aggregate Gene Cluster", GUIFactory.getIcon("empty16.gif"));
        aggGenClusterMenuItem.setActionCommand(AGG_GENE_CLUSTER_CMD);
        aggGenClusterMenuItem.setEnabled(false);
        aggGenClusterMenuItem.addActionListener(listener);
        menu.add(aggGenClusterMenuItem);                
    }

	public void setScrollBar(JScrollBar jsb) {
		this.jScrollBar = jsb;
		jScrollBar.setUnitIncrement(chartHeight+chartGap);
	}
	
    public static void main(String[] args){
//    	JDialog jp3 = new JDialog();
//    	JDialog jp2 = new JDialog();
//    	int[][] clusters3 = { {1,2,3,5,9,11,12,13,14,15,16,17,18,19},{1,2,3,5,7,8,12},{1,2,3,4,7,11}};
//    	int[][] clusters2 = { {1,2,3,5,9,11,12,13,14,15,16,17,18,19},{1,2,3,5,7,8,12}};
//    	String[] clnames = {"Joe", "Moe", "Snow"};
//    	BoxChartViewer vdv3 = new BoxChartViewer(clusters3, clnames);
//    	BoxChartViewer vdv2 = new BoxChartViewer(clusters2, clnames);
//    	System.out.println(vdv3.getMedian(new float[]{1f}));
//    	jp2.add(vdv2);
//    	jp2.setBackground(Color.white);
//    	jp2.setModal(true);
//    	jp2.setAlwaysOnTop(true);
//    	jp2.setSize(459, 450);
//    	jp3.add(vdv3);
//    	jp3.setBackground(Color.white);
//    	jp3.setModal(true);
//    	jp3.setAlwaysOnTop(true);
//    	jp3.setSize(459, 450);
//    	jp3.setVisible(true);
//    	jp2.setVisible(true);
//    	System.out.println(3.5f%2);
    	System.exit(0);
    	
    }
}
