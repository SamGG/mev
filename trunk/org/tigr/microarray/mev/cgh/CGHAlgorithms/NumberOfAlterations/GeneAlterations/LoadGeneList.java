/*
 * LoadGeneList.java
 *
 * Created on June 15, 2003, 4:31 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHDataObj.GeneDataSet;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class LoadGeneList extends NumberOfAlterationsCalculator{

    /** Creates a new instance of LoadGeneList */
    public LoadGeneList() {

    }

    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;
        this.data = framework.getData();
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + "/data");
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(framework.getFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION){
            return loadGeneList(chooser.getSelectedFile(), data.getCGHSpecies());
        }else{
            return null;
        }
    }
    
    /**
     * Raktim 4/27
     * Added to make State Saving Work
     * @param framework
     * @param file
     * @return
     * @throws AlgorithmException
     */
    public DefaultMutableTreeNode execute(IFramework framework, File file) throws AlgorithmException {
        this.framework = framework;
        this.data = framework.getData();
        
        if(file != null){
            return loadGeneList(file, data.getCGHSpecies());
        }else{
            return null;
        }
    }

    public DefaultMutableTreeNode loadGeneList(File file, int species){
        Vector vecGeneData = null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            Vector geneNames = new Vector();
            while((line = reader.readLine()) != null){
                geneNames.add(line.trim());
            }

            GeneDataSet geneDataSet = new GeneDataSet();
            geneDataSet.loadGeneDataByGeneNames(geneNames, species);
            vecGeneData = geneDataSet.getGeneData();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("GeneAlterations");
            GeneAlterations alts = new GeneAmplifications();
            alts.setVecGeneData(vecGeneData);
            alts.setData(this.data);
            alts.setAddGenInfo(false);
            root.add(alts.execute(framework));

            alts = new GeneDeletions();
            alts.setVecGeneData(vecGeneData);
            alts.setData(this.data);
            alts.setAddGenInfo(false);
            root.add(alts.execute(framework));

            addGeneralInfo(root, file.getAbsolutePath());
            return root;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Raktim 4/27
     * Added for State Saving to capture the File used for LoadGeneList
     * @param root
     * @param file
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, String path) {
    	ICGHCloneValueMenu menu = framework.getCghCloneValueMenu();
    	DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Amplification Threshold: " + menu.getAmpThresh()));
        node.add(new DefaultMutableTreeNode("Deletion Threshold: " + menu.getDelThresh()));
        node.add(new DefaultMutableTreeNode("Amplification 2 Copy Threshold: " + menu.getAmpThresh2Copy()));
        node.add(new DefaultMutableTreeNode("Deletion 2 Copy Threshold: " + menu.getDelThresh2Copy()));
        node.add(new DefaultMutableTreeNode("File: " + path));
        root.add(node);
    }
}
