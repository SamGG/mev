/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SAMCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:28 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

public class SAMCentroidsViewer extends CentroidsViewer {
    
    private int studyDesign;
    private float[] dValues, rValues, foldChangeArray, qLowestFDR;
    private boolean calculateQLowestFDR;
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SAMCentroidsViewer(Experiment experiment, int[][] clusters, int studyDesign,/*Vector geneNamesVector,*/ float[] dValues, float[] rValues, float[] foldChangeArray, float[] qLowestFDR, boolean calculateQLowestFDR) {
        super(experiment, clusters);
        this.studyDesign = studyDesign;
        this.dValues = dValues;
        this.rValues = rValues;
        //this.geneNamesVector = geneNamesVector;        
        this.foldChangeArray = foldChangeArray;
        this.qLowestFDR = qLowestFDR;
        this.calculateQLowestFDR = calculateQLowestFDR;
    }
    /**
	 * @inheritDoc
	 */
	public SAMCentroidsViewer(CentroidViewer cv) {
		super(cv);
	}
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { 
        oos.defaultWriteObject();
    }
    
    
    
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getClusters());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
                out.print(""+ foldChangeArray[rows[i]]);
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
}
