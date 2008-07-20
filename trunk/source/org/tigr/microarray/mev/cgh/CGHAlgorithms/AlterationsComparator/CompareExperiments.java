/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CompareExperiments.java
 *
 * Created on November 22, 2003, 4:05 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.AlterationsComparator;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHDataGenerator.ComparisonFlankingRegionCalculator;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers.AlterationParametersViewer;
import org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers.DataRegionsDataModel;
import org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers.NumberOfAlterationsViewer;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.GroupExperimentsPanel;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
public class CompareExperiments extends NumberOfAlterationsCalculator {
    //MultipleArrayData data;
	IData data;
    int[] deletionsAOnly, deletionsBOnly, deletionsAandB, amplificationsAOnly, amplificationsBOnly, amplificationsAandB;
    String exprA;
    String exprB;

    /** Creates a new instance of CompareExperiments */
    public CompareExperiments() {
    }

    /** This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        ComparisonFlankingRegionCalculator frCalc = new ComparisonFlankingRegionCalculator();
        this.framework = framework;
        this.data = framework.getData();

        ExperimentsComparatorInitDlg initDlg = new ExperimentsComparatorInitDlg(framework.getFrame(), data.getFeaturesList());
        if(initDlg.showModal() != JOptionPane.OK_OPTION){
            return null;
        }

        GroupExperimentsPanel gPanel = initDlg.getGroupExperimentsPanel();
        int[] groupAssignments = gPanel.getGroupAssignments();

        //this.nodeName = data.getSampleName(groupAssignments[0]) + " vs. " + data.getSampleName(groupAssignments[1]);
        this.nodeName = "CompareExperiments";
        this.exprA = data.getSampleName(groupAssignments[0]);
        this.exprB = data.getSampleName(groupAssignments[1]);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeName);

        TwoExperimentAlterationComparator comparator = new TwoExperimentAlterationComparator();
        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.DELETION);

        DefaultMutableTreeNode deletionsNode = new DefaultMutableTreeNode("Deletions");

        DefaultMutableTreeNode cloneDeletionsNode = new DefaultMutableTreeNode("Clone Deletions");
        deletionsAOnly = comparator.getAOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(deletionsAOnly))));
        deletionsBOnly = comparator.getBOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(deletionsBOnly))));
        deletionsAandB = comparator.getAAndB();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(deletionsAandB))));

        DefaultMutableTreeNode regionDeletionsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        deletionsNode.add(cloneDeletionsNode);
        deletionsNode.add(regionDeletionsNode);

        root.add(deletionsNode);

        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.AMPLIFICATION);

        DefaultMutableTreeNode amplificationsNode = new DefaultMutableTreeNode("Amplifications");

        DefaultMutableTreeNode cloneAmplificationsNode = new DefaultMutableTreeNode("Clone Amplifications");
        amplificationsAOnly = comparator.getAOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(amplificationsAOnly))));
        amplificationsBOnly = comparator.getBOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(amplificationsBOnly))));
        amplificationsAandB = comparator.getAAndB();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(amplificationsAandB))));

        DefaultMutableTreeNode regionAmplificationsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        amplificationsNode.add(cloneAmplificationsNode);
        amplificationsNode.add(regionAmplificationsNode);

        root.add(amplificationsNode);

        addGeneralInfo(root);
        return root;
    }
    
    /**
     * Raktim 4/27
     * Added for State Saving
     * @param framework
     * @param groupAssignments
     * @return
     * @throws AlgorithmException
     */
    public DefaultMutableTreeNode execute(IFramework framework, int[] groupAssignments) throws AlgorithmException {
        ComparisonFlankingRegionCalculator frCalc = new ComparisonFlankingRegionCalculator();
        this.framework = framework;
        this.data = framework.getData();

        //this.nodeName = data.getSampleName(groupAssignments[0]) + " vs. " + data.getSampleName(groupAssignments[1]);
        this.nodeName = "CompareExperiments";
        this.exprA = data.getSampleName(groupAssignments[0]);
        this.exprB = data.getSampleName(groupAssignments[1]);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeName);

        TwoExperimentAlterationComparator comparator = new TwoExperimentAlterationComparator();
        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.DELETION);

        DefaultMutableTreeNode deletionsNode = new DefaultMutableTreeNode("Deletions");

        DefaultMutableTreeNode cloneDeletionsNode = new DefaultMutableTreeNode("Clone Deletions");
        deletionsAOnly = comparator.getAOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(deletionsAOnly))));
        deletionsBOnly = comparator.getBOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(deletionsBOnly))));
        deletionsAandB = comparator.getAAndB();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(deletionsAandB))));

        DefaultMutableTreeNode regionDeletionsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        deletionsNode.add(cloneDeletionsNode);
        deletionsNode.add(regionDeletionsNode);

        root.add(deletionsNode);

        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.AMPLIFICATION);

        DefaultMutableTreeNode amplificationsNode = new DefaultMutableTreeNode("Amplifications");

        DefaultMutableTreeNode cloneAmplificationsNode = new DefaultMutableTreeNode("Clone Amplifications");
        amplificationsAOnly = comparator.getAOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(amplificationsAOnly))));
        amplificationsBOnly = comparator.getBOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(amplificationsBOnly))));
        amplificationsAandB = comparator.getAAndB();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(amplificationsAandB))));

        DefaultMutableTreeNode regionAmplificationsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        amplificationsNode.add(cloneAmplificationsNode);
        amplificationsNode.add(regionAmplificationsNode);

        root.add(amplificationsNode);

        addGeneralInfo(root);
        return root;
    }


    /**
     * Old UNUSED
     */
    public DefaultMutableTreeNode execute2(IFramework framework) throws AlgorithmException {
        ComparisonFlankingRegionCalculator frCalc = new ComparisonFlankingRegionCalculator();
        this.framework = framework;
        this.data = framework.getData();

        ExperimentsComparatorInitDlg initDlg = new ExperimentsComparatorInitDlg(framework.getFrame(), data.getFeaturesList());
        if(initDlg.showModal() != JOptionPane.OK_OPTION){
            return null;
        }

        GroupExperimentsPanel gPanel = initDlg.getGroupExperimentsPanel();
        int[] groupAssignments = gPanel.getGroupAssignments();

        this.nodeName = data.getSampleName(groupAssignments[0]) + " vs. " + data.getSampleName(groupAssignments[1]);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeName);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Parameters", createParametersViewer())));

        TwoExperimentAlterationComparator comparator = new TwoExperimentAlterationComparator();
        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.DELETION);

        DefaultMutableTreeNode deletionsNode = new DefaultMutableTreeNode("Deletions");

        DefaultMutableTreeNode cloneDeletionsNode = new DefaultMutableTreeNode("Clone Deletions");
        deletionsAOnly = comparator.getAOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(deletionsAOnly))));
        deletionsBOnly = comparator.getBOnly();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(deletionsBOnly))));
        deletionsAandB = comparator.getAAndB();
        cloneDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(deletionsAandB))));

        DefaultMutableTreeNode regionDeletionsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionDeletionsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        deletionsNode.add(cloneDeletionsNode);
        deletionsNode.add(regionDeletionsNode);

        root.add(deletionsNode);

        comparator.compareExperiments(this.data, groupAssignments[0], groupAssignments[1], TwoExperimentAlterationComparator.AMPLIFICATION);

        DefaultMutableTreeNode amplificationsNode = new DefaultMutableTreeNode("Amplifications");

        DefaultMutableTreeNode cloneAmplificationsNode = new DefaultMutableTreeNode("Clone Amplifications");
        amplificationsAOnly = comparator.getAOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(amplificationsAOnly))));
        amplificationsBOnly = comparator.getBOnly();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(amplificationsBOnly))));
        amplificationsAandB = comparator.getAAndB();
        cloneAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(amplificationsAandB))));

        DefaultMutableTreeNode regionAmplificationsNode = new DefaultMutableTreeNode("Region Amplifications");
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAOnly(), comparator.getBOnly(), groupAssignments[0])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("B Only", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getBOnly(), comparator.getAOnly(), groupAssignments[1])))));
        regionAmplificationsNode.add(new DefaultMutableTreeNode(new LeafInfo("A and B", createComparisonViewer(frCalc.calculateFlankingRegions(this.data, comparator.getAAndB(), null, groupAssignments)))));

        amplificationsNode.add(cloneAmplificationsNode);
        amplificationsNode.add(regionAmplificationsNode);

        root.add(amplificationsNode);

        root.add(new DefaultMutableTreeNode(new LeafInfo("Summary", createSummaryViewer())));
        return root;
    }

    private IViewer createComparisonViewer(Vector dataRegions){
        ICGHDataRegion[] alterationRegions = (ICGHDataRegion[])dataRegions.toArray(new ICGHDataRegion[0]);
        DataRegionsDataModel dataModel = new DataRegionsDataModel(alterationRegions);

        NumberOfAlterationsViewer viewer = new NumberOfAlterationsViewer();
        viewer.setData(data);
        viewer.setDataModel(dataModel);

        return viewer;

    }

    private IViewer createComparisonViewer(int[] cloneIndices){

        ICGHDataRegion[] alterationRegions = new ICGHDataRegion[cloneIndices.length];

        for(int i = 0; i < cloneIndices.length; i++){
            alterationRegions[i] = data.getCloneAt(cloneIndices[i]);
        }

        DataRegionsDataModel dataModel = new DataRegionsDataModel(alterationRegions);

        NumberOfAlterationsViewer viewer = new NumberOfAlterationsViewer();
        viewer.setData(data);
        viewer.setDataModel(dataModel);

        return viewer;
    }

    private IViewer createSummaryViewer(){
        StringBuffer sb = new StringBuffer();
        sb.append("Deletions A Only " + deletionsAOnly.length + "\n");
        sb.append("Deletions B Only " + deletionsBOnly.length + "\n");
        sb.append("Deletions A and B " + deletionsAandB.length + "\n\n");

        sb.append("Amplifications A Only " + amplificationsAOnly.length + "\n");
        sb.append("Amplifications B Only " + amplificationsBOnly.length + "\n");
        sb.append("Amplifications A and B " + amplificationsAandB.length + "\n\n");

        return new AlterationParametersViewer(sb.toString());
    }

    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root) {
    	ICGHCloneValueMenu menu = framework.getCghCloneValueMenu();
    	DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Amplification Threshold: " + menu.getAmpThresh()));
        node.add(new DefaultMutableTreeNode("Deletion Threshold: " + menu.getDelThresh()));
        node.add(new DefaultMutableTreeNode("Amplification 2 Copy Threshold: " + menu.getAmpThresh2Copy()));
        node.add(new DefaultMutableTreeNode("Deletion 2 Copy Threshold: " + menu.getDelThresh2Copy()));

        node.add(new DefaultMutableTreeNode("Experiment A: " + this.exprA));
        node.add(new DefaultMutableTreeNode("Experiment B: " + this.exprB));
        node.add(new DefaultMutableTreeNode("Deletions A Only: " + deletionsAOnly.length));
        node.add(new DefaultMutableTreeNode("Deletions B Only: " + deletionsBOnly.length));
        node.add(new DefaultMutableTreeNode("Deletions A and B: " + deletionsAandB.length));

        node.add(new DefaultMutableTreeNode("Amplifications A Only: " + amplificationsAOnly.length));
        node.add(new DefaultMutableTreeNode("Amplifications B Only: " + amplificationsBOnly.length));
        node.add(new DefaultMutableTreeNode("Amplifications A and B: " + amplificationsAandB.length));
        root.add(node);
    }
    
}
