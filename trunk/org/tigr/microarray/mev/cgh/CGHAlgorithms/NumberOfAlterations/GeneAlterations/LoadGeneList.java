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

    public DefaultMutableTreeNode loadGeneList(File file, int species){
        Vector vecGeneData = null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            Vector geneNames = new Vector();
            while((line = reader.readLine()) != null){
                geneNames.add(line);
            }

            GeneDataSet geneDataSet = new GeneDataSet();
            geneDataSet.loadGeneDataByGeneNames(geneNames, species);
            vecGeneData = geneDataSet.getGeneData();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Gene Alterations");
            GeneAlterations alts = new GeneAmplifications();
            alts.setVecGeneData(vecGeneData);
            alts.setData(this.data);
            root.add(alts.execute(framework));

            alts = new GeneDeletions();
            alts.setVecGeneData(vecGeneData);
            alts.setData(this.data);
            root.add(alts.execute(framework));

            return root;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
