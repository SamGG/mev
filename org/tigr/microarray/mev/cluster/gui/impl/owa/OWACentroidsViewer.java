/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OWACentroidsViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-02-05 22:09:18 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.owa;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class OWACentroidsViewer extends CentroidsViewer {

    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    private static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    private static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    
    private JPopupMenu popup;
    private JMenuItem setOverallMaxMenuItem;
    private JMenuItem setClusterMaxMenuItem;
    
    private Vector fValues, pValues, dfNumValues, dfDenomValues, ssGroups, ssError;
    private float[][] geneGroupMeans, geneGroupSDs;
    
    /** Creates new OWACentroidsViewer */
    public OWACentroidsViewer(Experiment experiment, int[][] clusters, float[][] geneGroupMeans, float[][] geneGroupSDs, Vector pValues, Vector fValues, Vector ssGroups, Vector ssError, Vector dfNumValues, Vector dfDenomValues) {
        super(experiment, clusters);        
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        this.pValues = pValues;
        this.fValues = fValues;
        this.geneGroupMeans = geneGroupMeans;
        this.geneGroupSDs = geneGroupSDs;
        this.ssGroups = ssGroups;
        this.ssError = ssError;
        this.dfNumValues = dfNumValues;
        this.dfDenomValues = dfDenomValues;
        getContentComponent().addMouseListener(listener);        
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
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
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Save all clusters", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        setOverallMaxMenuItem = new JMenuItem("Set Y to overall max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setOverallMaxMenuItem.setActionCommand(SET_Y_TO_EXPERIMENT_MAX_CMD);
        setOverallMaxMenuItem.addActionListener(listener);
        setOverallMaxMenuItem.setEnabled(false);
        menu.add(setOverallMaxMenuItem);
        
        setClusterMaxMenuItem = new JMenuItem("Set Y to cluster max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setClusterMaxMenuItem.setActionCommand(SET_Y_TO_CLUSTER_MAX_CMD);
        setClusterMaxMenuItem.addActionListener(listener);
        menu.add(setClusterMaxMenuItem);
    }
    
    /**
     * Saves all clusters.
     */
    private void onSaveClusters() {
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
            //if (i < fieldNames.length - 1) {
                out.print("\t");
            //}
        }
        for (int i = 0; i < geneGroupMeans[0].length; i++) {
            out.print("Group" + (i+1) + " mean\t");
            out.print("Group" + (i + 1) + " std.dev.\t");
        }        
        //out.print("\t");
        out.print("F ratio");
        out.print("\t");
        out.print("SS(Groups)\t");
        out.print("SS(Error)\t");
        out.print("df (Groups)\t");
        out.print("df (Error)\t");
        out.print("p value");
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                //if (k < fieldNames.length - 1) {
                    out.print("\t");
                //}
            }
            for (int j = 0; j < geneGroupMeans[i].length; j++) {
                out.print(geneGroupMeans[i][j] + "\t");
                out.print(geneGroupSDs[i][j] + "\t");
            }            
            //out.print("\t");
            out.print("" + ((Float)fValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)ssGroups.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)ssError.get(rows[i])).floatValue());
            out.print("\t");            
            out.print("" + ((Float)dfNumValues.get(rows[i])).floatValue());
            out.print("\t"); 
            out.print("" + ((Float)dfDenomValues.get(rows[i])).floatValue());
            out.print("\t");            
            out.print("" + ((Float)pValues.get(rows[i])).floatValue());
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
        final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
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
            if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
                onSaveClusters();
            } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                setAllYRanges(CentroidViewer.USE_EXPERIMENT_MAX);
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                setAllYRanges(CentroidViewer.USE_CLUSTER_MAX);
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            }
        }
        
        private void setAllYRanges(int yRangeOption){
            int numClusters = getClusters().length;
            for(int i = 0; i < numClusters; i++){
                centroidViewer.setClusterIndex(i);
                centroidViewer.setYRangeOption(yRangeOption);
            }
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        
        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }    

}
