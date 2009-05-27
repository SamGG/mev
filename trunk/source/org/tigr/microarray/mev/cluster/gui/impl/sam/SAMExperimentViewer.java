/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SAMExperimentViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

public class SAMExperimentViewer extends ExperimentViewer {

    
    private float[] dValues, rValues, foldChangeArray, qLowestFDR;
    private int studyDesign;
    private boolean calculateQLowestFDR;
    //private Vector geneNamesVector;    
    /**
     * State-saving constructor used to load saved analysis files from MeV v4.0-4.3.
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SAMExperimentViewer(Experiment experiment, int[][] clusters, int studyDesign, float[] dValues, float[] rValues, float[] foldChangeArray, float[] qLowestFDR, boolean calculateQLowestFDR) {
    	super(experiment, clusters);
	 	initialize(studyDesign, dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR);
	}
    /**
     * State-saving constructor for MeV v4.4.
     * @param experiment
     * @param clusters
     * @param studyDesign
     * @param dValues
     * @param rValues
     * @param foldChangeArray
     * @param qLowestFDR
     * @param calculateQLowestFDR
     */
    public SAMExperimentViewer(Experiment experiment, ClusterWrapper clusters, int studyDesign, float[] dValues, float[] rValues, float[] foldChangeArray, float[] qLowestFDR, boolean calculateQLowestFDR) {
    	super(experiment, clusters.getClusters());
	 	initialize(studyDesign, dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR);
	}
    /**
     * @inheritDoc
     */ 
    public SAMExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, Boolean drawAnnotations, ExperimentHeader header, Insets insets, Integer exptID,
    		Integer studyDesign, float[] dValues, float[] rValues, float[] foldChangeArray, float[] qLowestFDR, Boolean calculateQLowestFDR) {
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue(), header, insets);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
    	initialize(studyDesign.intValue(), dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR.booleanValue());
    } 
    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{getExperiment(), getClusters(), new Integer(studyDesign), dValues, 
    		rValues, foldChangeArray, qLowestFDR, 
    		new Boolean(calculateQLowestFDR)});
    }

    private void initialize(int studyDesign, float[] dValues, float[] rValues, float[] foldChangeArray, float[] qLowestFDR, boolean calculateQLowestFDR){
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
        this.studyDesign = studyDesign;
        this.dValues = dValues;
        this.rValues = rValues;
        //this.geneNamesVector = geneNamesVector;         
        this.foldChangeArray = foldChangeArray;
        this.qLowestFDR = qLowestFDR;
        this.calculateQLowestFDR = calculateQLowestFDR;
	getContentComponent().addMouseListener(listener);
	getHeaderComponent().addMouseListener(listener);
    }
    
    /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        saveExperiment(frame, getExperiment(), getData(), getClusters());
    }

    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        saveExperiment(frame, getExperiment(), getData(), getCluster());
    }    
    
    
    /**
     * Saves values from specified experiment and its rows.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveCluster(file, experiment, data, rows);
        }
    }
    
    /**
     * Saves values from specified experiment and cluster.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[][] clusters) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveCluster(aFile, experiment, data, clusters[i]);
            }
        }
    }    
    
    private void saveCluster(File file, Experiment experiment, IData data, int[] rows) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        out.print("Original row");
        out.print("\t");
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        //out.print("\t");
        //out.print("GeneNamesVector");        
        out.print("\t");
        out.print("Score (d)");
        out.print("\t");
        out.print("Numerator (r)\t");
        out.print("Denominator (s+s0)\t");
        if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {        
            out.print("Fold Change");
        }
        if (calculateQLowestFDR) {
            out.print("\t");
            out.print("q-value (%)");
        }
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //handles cutoffs
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                if (k < fieldNames.length - 1) {
                    out.print("\t"); 
                }
            }
            //out.print("\t");
            //out.print((String)(geneNamesVector.get(rows[i])));            
            out.print("\t");
            out.print("" + dValues[rows[i]]);
            out.print("\t");
            out.print("" + rValues[rows[i]] + "\t");    
            out.print("" + (float)(rValues[rows[i]]/dValues[rows[i]]) + "\t");
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {            
                out.print("" + foldChangeArray[rows[i]]);
            }
            if (calculateQLowestFDR) {
                out.print("\t");
                out.print("" + qLowestFDR[rows[i]]);
            }
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }  
    
    /**
     * Returns a file choosed by the user.
     */
    private static File getFile(Frame frame) {
        File file = null;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int ret = fc.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
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
	    } else if (command.equals(STORE_CLUSTER_CMD)) {
		storeCluster();
	    } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
        //EH Gaggle test
        } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {
            broadcastClusterGaggle();
        } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {
            broadcastNamelistGaggle();
            }
	}
	
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
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
