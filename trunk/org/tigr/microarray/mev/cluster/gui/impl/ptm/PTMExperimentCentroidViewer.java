/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PTMExperimentCentroidViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-02-05 22:10:56 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;


public class PTMExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
   
    private JPopupMenu popup;
    private Vector templateVector;
    private int numberOfGenes;
    private String[] auxTitles;
    private Object[][] auxData;    
    
    /**
     * Construct a <code>PTMExperimentCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public PTMExperimentCentroidViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
        this.numberOfGenes = experiment.getNumberOfGenes();
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	this.templateVector = templateVector;
        this.auxTitles = auxTitles;
        this.auxData = auxData;        
	getContentComponent().addMouseListener(listener);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(this.templateVector);
        oos.writeInt(this.numberOfGenes);
        oos.writeObject(this.auxData);
        oos.writeObject(this.auxTitles);
    }    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {        
        this.templateVector = (Vector)ois.readObject();
        this.numberOfGenes = ois.readInt();
        this.auxData = (Object [][])ois.readObject();
        this.auxTitles = (String [])ois.readObject();
            
        Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	getContentComponent().addMouseListener(listener);
    }    
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
	JPopupMenu popup = new JPopupMenu();
	addMenuItems(popup, listener);
	return popup;
    }
    
    
    /**
     * Saves all clusters.
     */
    private void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    ExperimentUtil.saveAllExperimentClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
    /**
     * Save the viewer cluster.
     */
    private void onSaveCluster() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    ExperimentUtil.saveExperimentClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
    /**
     * Sets a public color.
     */
    private void onSetColor() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	Color newColor = JColorChooser.showDialog(frame, "Choose color", DEF_CLUSTER_COLOR);
	if (newColor != null) {
	    setClusterColor(newColor);
	}
    }
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
	setClusterColor(null);
    }
    
    /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
	FontMetrics metrics = g.getFontMetrics();
	Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
	paint((Graphics2D)g, rect, true);
    }
    
    
        /**
     * Paints chart into specified graphics and with specified bounds.
     */
    public void paint(Graphics2D g, Rectangle rect, boolean drawMarks) {
        super.subPaint(g,rect,drawMarks);
        
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        final int left = rect.x;
        final int top = rect.y;
        final int width  = rect.width;
        final int height = rect.height;
        
        if (width < 5 || height < 5) {
            return;
        }
        
        final int zeroValue = top + (int)Math.round(height/2f);
        final int numberOfSamples  = this.getCluster().length;
        
        //do this outside paint once menu is set up
        if(this.yRangeOption == this.USE_EXPERIMENT_MAX)
            maxYValue = this.maxExperimentValue;
        else if(this.yRangeOption == this.USE_CLUSTER_MAX)
            maxYValue = this.maxClusterValue;
        
        if (maxYValue == 0.0f) {
            maxYValue = 1.0f;
        }
        
        final float factor = height/(2f*maxYValue);
        final float stepX  = width/(float)(numberOfGenes-1);
        final int   stepsY = (int)maxYValue+1;
        
        if (this.drawVariances) {
            // draw variances
            g.setColor(bColor);
            for (int i=0; i<numberOfGenes; i++) {
                if(Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.variances[this.clusterIndex][i]) || (this.variances[this.clusterIndex][i] < 0.0f)) {
                    continue;
                }
                g.drawLine(left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
                g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor));
                g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
            }
        }
        if (this.drawValues) {
            // draw values
            float fValue, sValue;
            Color color;
            for (int sample=0; sample<getCluster().length; sample++) {
                for (int probe=0; probe<numberOfGenes-1; probe++) {
                    fValue = this.experiment.get(probe, getCluster()[sample]);
                    sValue = this.experiment.get(probe+1, getCluster()[sample]);
                    if (Float.isNaN(fValue) || Float.isNaN(sValue)) {
                        continue;
                    }
                    color = this.data.getExperimentColor(getCluster()[sample]);
                    color = color == null ? DEF_CLUSTER_COLOR : color;
                    g.setColor(color);
                    g.drawLine(left+(int)Math.round(probe*stepX)    , zeroValue - (int)Math.round(fValue*factor),
                    left+(int)Math.round((probe+1)*stepX), zeroValue - (int)Math.round(sValue*factor));
                }
            }
        }
        
        if (this.drawCodes && this.codes != null && clusters[clusterIndex].length > 0) {
            g.setColor(Color.gray);
            for (int i=0; i<numberOfGenes-1; i++) {
                g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i+1]*factor));
            }
        }
        
        // draw zero line
        g.setColor(Color.black);
        g.drawLine(left, zeroValue, left+width, zeroValue);
        // draw magenta line
        if (getCluster() != null && getCluster().length > 0) {
            g.setColor(Color.magenta);
            for (int i=0; i<numberOfGenes-1; i++) {
                if (Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.means[this.clusterIndex][i+1])) {
                    continue;
                }
                g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i+1]*factor));
            }
        }
        
        
        
        
        	//draw template
	float[] templateArray = new float[templateVector.size()];

	for (int i = 0; i < templateArray.length; i++){
	    templateArray[i] = ((Float)(templateVector.get(i))).floatValue();
	}	
	for (int i = 0; i < templateArray.length; i++){
	    templateArray[i] = templateArray[i] - 0.5f;
	}

	for (int i = 0; i < numberOfGenes - 1; i++) {
	    g.setColor(Color.red);
	    if (!Float.isNaN(templateArray[i])) {
		g.fillOval(left+(int)Math.round(i*stepX) - 2, zeroValue-(int)Math.round(templateArray[i]*factor) - 2, 5, 5);
	    }
	    if (!Float.isNaN(templateArray[i+1])) {
		g.fillOval(left+(int)Math.round((i+1)*stepX) - 2, zeroValue-(int)Math.round(templateArray[i+1]*factor) - 2, 5, 5);
	    }
	    if (Float.isNaN(templateArray[i]) || Float.isNaN(templateArray[i+1])) {
		continue;
	    }
	    g.setColor(Color.blue);
	    g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(templateArray[i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(templateArray[i+1]*factor));
	}
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        // draw rectangle
        g.setColor(Color.black);
        g.drawRect(left, top, width, height);
        // draw X items
        for (int i=1; i<numberOfGenes-1; i++) {
            g.drawLine(left+(int)Math.round(i*stepX), top+height-5, left+(int)Math.round(i*stepX), top+height);
        }
        //draw Y items
        for (int i=1; i<stepsY; i++) {
            g.drawLine(left, zeroValue-(int)Math.round(i*factor), left+5, zeroValue-(int)Math.round(i*factor));
            g.drawLine(left, zeroValue+(int)Math.round(i*factor), left+5, zeroValue+(int)Math.round(i*factor));
        }
        // draw genes info
        g.setColor(bColor);
 /*      if (drawMarks) {
            FontMetrics metrics = g.getFontMetrics();
            String str;
            int strWidth;
            //draw Y digits
            for (int i=1; i<stepsY; i++) {
                str = String.valueOf(i);
                strWidth = metrics.stringWidth(str);
                g.drawString(str, left-10-strWidth, zeroValue+5-(int)Math.round(i*factor));
                str = String.valueOf(-i);
                strWidth = metrics.stringWidth(str);
                g.drawString(str, left-10-strWidth, zeroValue+5+(int)Math.round(i*factor));
            }
            // draw X samples names
            g.rotate(-Math.PI/2.0);
            final int max_name_width = getNamesWidth(metrics);
            for (int i=0; i<numberOfSamples; i++) {
                g.drawString(data.getSampleName(experiment.getSampleIndex(getCluster()[i])), -height-top-10-max_name_width, left+(int)Math.round(i*stepX)+3);
            }
            g.rotate(Math.PI/2.0);
        }
  */
        if (getCluster() != null && getCluster().length > 0 && this.drawVariances) {
            // draw points
            g.setColor(bColor);
            for (int i=0; i<numberOfGenes; i++) {
                if (Float.isNaN(this.means[this.clusterIndex][i])) {
                    continue;
                }
                g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor)-3, 6, 6);
            }
        }
        
        
        
        g.setColor(bColor);
        if (getCluster() == null || getCluster().length == 0) {
            g.drawString("No Experiments In Cluster", left+10, top+20);
        } else {
            if(getCluster().length == 1)
                g.drawString(1 + " Experiment", left+10, top+20);
            else
                g.drawString(getCluster().length+" Experiments", left+10, top+20);
        }
    }

    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals(SAVE_CLUSTER_CMD)) {
		onSaveCluster();
	    } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
		onSaveClusters();
	    } else if (command.equals(SET_DEF_COLOR_CMD)) {
		onSetDefaultColor();
	    } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_EXPERIMENT_MAX;
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_CLUSTER_MAX;
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            } else if (command.equals(STORE_CLUSTER_CMD)) {
		storeCluster();
	    } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            }
	}
	
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
	    if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
		return;
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}
    }
    
}
