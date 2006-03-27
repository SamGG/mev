   
package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.HistoryViewer;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.*;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.microarray.mev.cluster.gui.impl.fom.CastFOMViewerA;
import org.tigr.microarray.mev.cluster.gui.impl.fom.CastFOMViewerB;
import org.tigr.microarray.mev.cluster.gui.impl.fom.KFOMViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gdm.GDMExpViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gdm.GDMGeneViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsh.*;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.*;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.*;
import org.tigr.microarray.mev.cluster.gui.impl.kmcs.*;
import org.tigr.microarray.mev.cluster.gui.impl.knnc.*;
import org.tigr.microarray.mev.cluster.gui.impl.owa.*;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCA2DViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCA3DViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCADummyViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCInfoViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PlotVectorViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PlotViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.ValuesViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ptm.*;
import org.tigr.microarray.mev.cluster.gui.impl.qtc.*;
import org.tigr.microarray.mev.cluster.gui.impl.rn.*;
import org.tigr.microarray.mev.cluster.gui.impl.sam.*;
import org.tigr.microarray.mev.cluster.gui.impl.som.*;
import org.tigr.microarray.mev.cluster.gui.impl.sota.*;
import org.tigr.microarray.mev.cluster.gui.impl.st.*;
import org.tigr.microarray.mev.cluster.gui.impl.svm.*;
import org.tigr.microarray.mev.cluster.gui.impl.terrain.TerrainViewer;
import org.tigr.microarray.mev.cluster.gui.impl.tfa.*;
import org.tigr.microarray.mev.cluster.gui.impl.ttest.*;
import org.tigr.microarray.mev.cluster.gui.impl.cast.*;
import org.tigr.microarray.mev.cluster.gui.impl.coa.COA2DViewer;
import org.tigr.microarray.mev.cluster.gui.impl.coa.COA3DViewer;
import org.tigr.microarray.mev.cluster.gui.impl.coa.COADummyViewer;
import org.tigr.microarray.mev.cluster.gui.impl.coa.COAInertiaValsViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dam.*;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASECentroidViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASECentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASETableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ease.gotree.GOTreeViewer;

/**
 * A PersistenceDelegate class used for saving the state of 
 * implementations of the {@link IViewer} interface.
 * 
 * @author eleanora
 * @see org.tigr.microarray.mev.cluster.gui.IViewer
 * @see XMLEncoderFactory
 */
public class IViewerPersistenceDelegate extends PersistenceDelegate {
	
	/**
	 * Creates an {@link Expression} 
	 * @inheritDoc
	 */
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		//default expression
		Expression e = new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{});

		e = ((IViewer)oldInstance).getExpression();

			
		/*
		if(oldInstance instanceof ViewerAdapter) {
			Object va = oldInstance;
			if(va instanceof KMCInfoViewer) {
				KMCInfoViewer ev = (KMCInfoViewer)va;
				e = new Expression((KMCInfoViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getContentComponent(), new Boolean(ev.getClusterGenes())});	
			} else if (va instanceof KMCSuppInfoViewer) {
				KMCSuppInfoViewer ev = (KMCSuppInfoViewer)va;
				e = new Expression((KMCSuppInfoViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getContentComponent(), ev.getHeaderComponent(), new Boolean(ev.getClusterGenes())});
			} else if (va instanceof PCInfoViewer) {
				PCInfoViewer ev = (PCInfoViewer)va;
				e = new Expression((PCInfoViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getT()});
			} else if (va instanceof PlotViewer) {
				PlotViewer ev = (PlotViewer)va;
				e = new Expression((PlotViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getS()});
			} else if (va instanceof PlotVectorViewer) {
				PlotVectorViewer ev = (PlotVectorViewer)va;
				e = new Expression((PlotVectorViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getT()});
			} else if (va instanceof PCADummyViewer) {
				PCADummyViewer ev = (PCADummyViewer)va;
				e = new Expression((PCADummyViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getU(), ev.getS(), new Integer(ev.getMode())});
			} else if (va instanceof COADummyViewer) {
				COADummyViewer ev = (COADummyViewer)va;
				e = new Expression((COADummyViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getGeneUMatrix(), ev.getExptUMatrix()});
			} else if (va instanceof SOTAInfoViewer) {
				SOTAInfoViewer ev = (SOTAInfoViewer)va;
				e = new Expression((SOTAInfoViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getHeader(), ev.getContent(), new Boolean(ev.getClusterGenes())});

			} else {	//Default ViewerAdapter persistence delegate.  
				e = new Expression(oldInstance, oldInstance.getClass(), "new",
						new Object[]{((ViewerAdapter)oldInstance).getContentComponent(), ((ViewerAdapter)oldInstance).getHeaderComponent()});
			}
		} else if (oldInstance instanceof IViewer) {		//oldInstance is not a ViewerAdapter
			Object iv = oldInstance;
			if (iv instanceof ExperimentViewer) {
				if (iv instanceof SOTAExperimentViewer){
					SOTAExperimentViewer ev = (SOTAExperimentViewer)iv;
					e = new Expression((SOTAExperimentViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getExptViewer(), new Float(ev.getFactor()), new Integer(ev.getFunction()), new Integer(ev.getNumberOfCells()), new Boolean(ev.getGeneClusterViewer()), new Boolean(ev.getUseDoubleGradient()), ev.getClusterDivFM(), ev.getCentroidDataFM(), ev.getClusters(), new Boolean(ev.getIsDrawAnnotations()), ev.getHeaderComponent(), ev.getInsets(), new Integer(ev.getExperimentID()), ev.getCodes(), ev.getViewPanel()});  
				} else if(iv instanceof PTMExperimentViewer){
					PTMExperimentViewer ev = (PTMExperimentViewer)iv;
					e = new Expression((PTMExperimentViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getExpViewer(), ev.getPTMHeader(), ev.getAuxTitles(), ev.getAuxData()});  
				} else if(iv instanceof TtestExperimentViewer) {
					TtestExperimentViewer eccv = (TtestExperimentViewer)iv;
						e = new Expression((TtestExperimentViewer) oldInstance, oldInstance.getClass(), "new",
								new Object[]{eccv.getClusters(), eccv.getSamplesOrder(), new Boolean(eccv.getIsDrawAnnotations()), eccv.getHeader(), eccv.getInsets(), new Integer(eccv.getExperimentID()),			
											new Integer(eccv.getTTestDesign()), eccv.getOneClassMeans(), eccv.getOneClassSDs(), eccv.getMeansA(), eccv.getMeansB(), eccv.getSdA(), eccv.getSdB(), eccv.getRawPValues(), eccv.getAdjPValues(), eccv.getTValues(), eccv.getDfValues()});  
				} else if(iv instanceof SAMExperimentViewer){
					SAMExperimentViewer ev = (SAMExperimentViewer)iv;
					e = new Expression((SAMExperimentViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getClusters(), ev.getSamplesOrder(), new Boolean(ev.getIsDrawAnnotations()), ev.getHeader(), ev.getInsets(), new Integer(ev.getExperimentID()),  
						new Integer(ev.getStudyDesign()), ev.getDValues(), ev.getRValues(), ev.getFoldChangeArray(), ev.getQLowestFDR(), new Boolean(ev.isCalculateQLowestFDR())});
				} else if(iv instanceof OWAExperimentViewer){
					OWAExperimentViewer ev = (OWAExperimentViewer)iv;
					e = new Expression((OWAExperimentViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getClusters(), ev.getSamplesOrder(), new Boolean(ev.getIsDrawAnnotations()), ev.getHeader(), ev.getInsets(), new Integer(ev.getExperimentID()), ev.getGeneGroupMeans(), ev.getGeneGroupSDs(), ev.getRawPValues(), ev.getAdjPValues(), ev.getFValues(), ev.getSsGroups(), ev.getSsError(), ev.getDfNumValues(), ev.getDfDenomValues()});
				} else if(iv instanceof TFAExperimentViewer){
					TFAExperimentViewer ev = (TFAExperimentViewer)iv;
					e = new Expression((TFAExperimentViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getClusters(), ev.getSamplesOrder(), new Boolean(ev.getIsDrawAnnotations()), ev.getHeader(), ev.getInsets(), new Integer(ev.getExperimentID()), ev.getAuxTitles(), ev.getAuxData()});  
				
				} else {	//Default constructor to use for ExperimentViewers
					ExperimentViewer ev = (ExperimentViewer)iv;
					e = new Expression(oldInstance, oldInstance.getClass(), "new",
							new Object[]{ev.getClusters(), ev.getSamplesOrder(), new Boolean(ev.getIsDrawAnnotations()), ev.getHeader(), ev.getInsets(), new Integer(ev.getExperimentID())});  
				}
				
			} else if (iv instanceof ExperimentClusterTableViewer) {
				ExperimentClusterTableViewer ev = (ExperimentClusterTableViewer)iv;
				e = new Expression((ExperimentClusterTableViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getClusters(), ev.getAuxTitles(), ev.getAuxData(), ev.getSortedClusters(), new Integer(ev.getExptID())});  
			
			} else if (iv instanceof ExperimentClusterCentroidsViewer) {
					ExperimentClusterCentroidsViewer eccv = (ExperimentClusterCentroidsViewer)iv;
					e = new Expression(oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getExperimentClusterCentroidViewer(), new Integer(eccv.getExperimentID())});  
				
			} else if (iv instanceof ExperimentClusterCentroidViewer) {
				if (iv instanceof PTMExperimentCentroidViewer) {
					PTMExperimentCentroidViewer eccv = (PTMExperimentCentroidViewer)iv;
					e = new Expression((PTMExperimentCentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), new Integer(eccv.getExperimentID()), new Integer(eccv.getClusterIndex()), eccv.getMeans(), eccv.getVariances(), eccv.getCodes(), eccv.getTemplateVector(), eccv.getAuxTitles(), eccv.getAuxData()});  
			
				} else {
					ExperimentClusterCentroidViewer eccv = (ExperimentClusterCentroidViewer) iv;
					e = new Expression(oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), new Integer(eccv.getExperimentID()), new Integer(eccv.getClusterIndex()), eccv.getMeans(), eccv.getVariances(), eccv.getCodes()});  
				}
				
			} else if(iv instanceof ExperimentClusterViewer) {
				ExperimentClusterViewer eccv = (ExperimentClusterViewer)iv;
				e = new Expression( oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getGenesOrder(), new Boolean(eccv.getDrawAnnotations()), new Integer(eccv.getOffset()), eccv.getHeader(), new Boolean(eccv.getHasCentroid()), eccv.getCentroids(), eccv.getElementSize(), new Integer(eccv.getLabelIndex()), new Integer(eccv.getExperimentID())});  
				
			} else if (iv instanceof CentroidViewer) {
				if (iv instanceof PTMCentroidViewer) {
					PTMCentroidViewer eccv = (PTMCentroidViewer)iv;
					e = new Expression((PTMCentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID()), eccv.getTemplateVector(), eccv.getAuxTitles(), eccv.getAuxData()});
				} else if (iv instanceof TtestCentroidViewer) {
					TtestCentroidViewer eccv = (TtestCentroidViewer)iv;
					e = new Expression((TtestCentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID()), 
							new Integer(eccv.getTTestDesign()), eccv.getOneClassMeans(), eccv.getOneClassSDs(), eccv.getMeansA(), eccv.getMeansB(), eccv.getSdA(), eccv.getSdB(), eccv.getRawPValues(), eccv.getAdjPValues(), eccv.getTValues(), eccv.getDfValues()});
				} else if (iv instanceof SAMCentroidViewer) {
					SAMCentroidViewer eccv = (SAMCentroidViewer)iv;
					e = new Expression((SAMCentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID()), 
							new Integer(eccv.getStudyDesign()), eccv.getDValues(), eccv.getRValues(), eccv.getFoldChangeArray(), eccv.getQLowestFDR(), new Boolean(eccv.isCalculateQLowestFDR())});
				} else if (iv instanceof OWACentroidViewer) {
					OWACentroidViewer eccv = (OWACentroidViewer)iv;
					e = new Expression((OWACentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID()), eccv.getGeneGroupMeans(), eccv.getGeneGroupSDs(), eccv.getRawPValues(), eccv.getAdjPValues(), eccv.getFValues(), eccv.getSsGroups(), eccv.getSsError(), eccv.getDfNumValues(), eccv.getDfDenomValues()});
				} else if (iv instanceof TFACentroidViewer) {
					TFACentroidViewer eccv = (TFACentroidViewer)iv;
					e = new Expression((TFACentroidViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID()), eccv.getAuxTitles(), eccv.getAuxData()});
				   
				} else {
					CentroidViewer eccv = (CentroidViewer)iv;
					e = new Expression(oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getClusters(), eccv.getVariances(), eccv.getMeans(), eccv.getCodes(), new Integer(eccv.getExperimentID())});
				}
				
				
			} else  if (iv instanceof CentroidsViewer) {
				if (iv instanceof TtestCentroidsViewer) {
					TtestCentroidsViewer eccv = (TtestCentroidsViewer)iv;
					e = new Expression((TtestCentroidsViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getCentroidViewer(), new Integer(eccv.getTTestDesign()), eccv.getOneClassMeans(), eccv.getOneClassSDs(), eccv.getMeansA(), eccv.getMeansB(), eccv.getSdA(), eccv.getSdB(), eccv.getRawPValues(), eccv.getAdjPValues(), eccv.getTValues(), eccv.getDfValues()});
				} else if (iv instanceof OWACentroidsViewer) {
					OWACentroidsViewer eccv = (OWACentroidsViewer)iv;
					e = new Expression((OWACentroidsViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getCentroidViewer(), eccv.getGeneGroupMeans(), eccv.getGeneGroupSDs(), eccv.getRawPValues(), eccv.getAdjPValues(), eccv.getFValues(), eccv.getSsGroups(), eccv.getSsError(), eccv.getDfNumValues(), eccv.getDfDenomValues()});
				} else if (iv instanceof TFACentroidsViewer) {
					TFACentroidsViewer eccv = (TFACentroidsViewer)iv;
					e = new Expression((TFACentroidsViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getCentroidViewer(), eccv.getAuxTitles(), eccv.getAuxData()});
				
				} else {
					CentroidsViewer eccv = (CentroidsViewer)iv;
					e = new Expression(oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getCentroidViewer()});
				}
				
			} else if (iv instanceof ClusterTableViewer) {
				ClusterTableViewer eccv = (ClusterTableViewer)iv;
				e = new Expression((ClusterTableViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getClusters(), eccv.getFieldNames(), eccv.getAuxTitles(), eccv.getAuxData(), new Integer(eccv.getExperimentID())});
	
			} else if (iv instanceof HCLViewer) {
				if(iv instanceof HCLSupportViewer){
					HCLSupportViewer eccv = (HCLSupportViewer)iv;
					e = new Expression((HCLSupportViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{new Integer(eccv.getExperimentID()), eccv.getFeatures(), eccv.getGenes_result(), eccv.getSamples_result(), eccv.getSampleClusters(), new Boolean(eccv.getIsExperimentCluster()), eccv.getGenesTree(), eccv.getSampleTree(), new Integer(eccv.getOffset()), eccv.getExperimentViewer(), eccv.getGeneTreeSupportVector(), eccv.getExptTreeSupportVector()});
				} else {
					HCLViewer eccv = (HCLViewer)iv;
					e = new Expression((HCLViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{new Integer(eccv.getExperimentID()), eccv.getFeatures(), eccv.getGenes_result(), eccv.getSamples_result(), eccv.getSampleClusters(), new Boolean(eccv.getIsExperimentCluster()), eccv.getGenesTree(), eccv.getSampleTree(), new Integer(eccv.getOffset()), eccv.getExperimentViewer()});
				}
				
			} else if (iv instanceof TableViewer) {
				if(iv instanceof EASETableViewer){
					EASETableViewer eccv = (EASETableViewer)iv;
						e = new Expression((EASETableViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getHeaderNames(), eccv.getData()});
				} else {
					TableViewer eccv = (TableViewer)iv;
					e = new Expression((TableViewer) oldInstance, oldInstance.getClass(), "new",
							new Object[]{eccv.getHeaderNames(), eccv.getData()});
				}	
			} else if (iv instanceof GOTreeViewer) {
				GOTreeViewer eccv = (GOTreeViewer)iv;
				e = new Expression((GOTreeViewer) oldInstance, oldInstance.getClass(), "new",
					new Object[]{new Integer(eccv.getExperimentID()), eccv.getStoredNodes(), 
						eccv.getBaseFileSystem(), eccv.getCategory(), eccv.getHeaderFields(), 
						new Integer(eccv.getSelectionPolarity()), new Boolean(eccv.isVerbose()), 
						new Double(eccv.getUpper()), new Double(eccv.getLower())});		 
				
			} else if (iv instanceof HCLClusterInfoViewer) {
				HCLClusterInfoViewer eccv = (HCLClusterInfoViewer)iv;
				e = new Expression((HCLClusterInfoViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getContent(), eccv.getClusterGenes(), eccv.getZThr()});
				
			} else if (iv instanceof HCLNodeHeightGraph) {
				HCLNodeHeightGraph eccv = (HCLNodeHeightGraph)iv;
				e = new Expression((HCLNodeHeightGraph) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getTreeData()});
				
			} else if (iv instanceof SOTAExperimentTreeViewer) {
				SOTAExperimentTreeViewer eccv = (SOTAExperimentTreeViewer)iv;
				e = new Expression((SOTAExperimentTreeViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getSOTATreeData(), eccv.getClusterIndices()});
			} else if (iv instanceof SOTAGeneTreeViewer) {
				SOTAGeneTreeViewer eccv = (SOTAGeneTreeViewer)iv;
				e = new Expression((SOTAGeneTreeViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getSotaTreeData(), eccv.getSampleTree(), eccv.getClusters()}); //, eccv.getExpViewer()});
			} else if (iv instanceof SOTADiversityViewer) {
				SOTADiversityViewer eccv = (SOTADiversityViewer)iv;
				e = new Expression((SOTADiversityViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getValues()});
			} else if (iv instanceof RelevanceNetworkViewer) {
				RelevanceNetworkViewer eccv = (RelevanceNetworkViewer)iv;
				e = new Expression((RelevanceNetworkViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Boolean(eccv.getIsGenes()), eccv.getClusters(), eccv.getWeights(), eccv.getIndices(), new Integer(eccv.getExperimentID())});
			} else if(iv instanceof SOMExperimentViewer){
				SOMExperimentViewer ev = (SOMExperimentViewer)iv;
				e = new Expression((SOMExperimentViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{ev.getExpViewer(), ev.getClusters(), ev.getHeader()});  
			} else if (iv instanceof UMatrixDistanceViewer) {
				UMatrixDistanceViewer eccv = (UMatrixDistanceViewer)iv;
				e = new Expression((UMatrixDistanceViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getClusters(), eccv.getUMatrix(), eccv.getDimX(), eccv.getDimY(), eccv.getTopology()});
			} else if (iv instanceof UMatrixColorViewer) {
				UMatrixColorViewer eccv = (UMatrixColorViewer)iv;
				e = new Expression((UMatrixColorViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getClusters(), eccv.getUMatrix(), eccv.getDimX(), eccv.getDimY(), eccv.getTopology()});
			} else if (iv instanceof PTMExperimentCentroidsViewer) {
				PTMExperimentCentroidsViewer eccv = (PTMExperimentCentroidsViewer)iv;
				e = new Expression((PTMExperimentCentroidsViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getCentroidViewer(), eccv.getAuxTitles(), eccv.getAuxData()});
			} else if (iv instanceof PTMExperimentSubCentroidsViewer) {
				PTMExperimentSubCentroidsViewer eccv = (PTMExperimentSubCentroidsViewer)iv;
				e = new Expression((PTMExperimentSubCentroidsViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getCentroidViewer()});
			} else if (iv instanceof TTestVolcanoPlotViewer) {
				TTestVolcanoPlotViewer ev = (TTestVolcanoPlotViewer)iv;
				e = new Expression((TTestVolcanoPlotViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(ev.getExperimentID()), ev.getXArray(), ev.getYArray(), 
						ev.getIsSig(), new Integer(ev.getTTestDesign()), new Double(ev.getOneClassMean()), ev.getOneClassMeans(),
						ev.getOneClassSDs(), ev.getMeansA(), ev.getMeansB(), ev.getSdA(), ev.getSdB(),
						ev.getRawPValues(), ev.getAdjPValues(), ev.getTValues(), ev.getDfValues()});    
			} else if (iv instanceof SAMGraphViewer) {
				SAMGraphViewer eccv = (SAMGraphViewer)iv;
				e = new Expression((SAMGraphViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{eccv.getXArray(), eccv.getYArray(), new Integer(eccv.getStudyDesign()), new Double(eccv.getDelta())});
			} else if (iv instanceof SVMOneOutViewer) {
				SVMOneOutViewer eccv = (SVMOneOutViewer)iv;
				e = new Expression((SVMOneOutViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getClasses(), eccv.getDiscr(), new Boolean(eccv.isClassifyGenes()), eccv.getInitClasses(), eccv.getElementScores(), eccv.getIterationScores(), new Integer(eccv.getNonNeuts())});
			} else if (iv instanceof SVMTrainViewer) {
				SVMTrainViewer eccv = (SVMTrainViewer)iv;
				e = new Expression((SVMTrainViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getWeights(), new Boolean(eccv.isClassifyGenes()), eccv.getData()});
			} else if (iv instanceof SVMClassifyViewer) {
				SVMClassifyViewer eccv = (SVMClassifyViewer)iv;
				e = new Expression((SVMClassifyViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getDiscr(), eccv.getClasses(), new Boolean(eccv.isClassifyGenes())});
			} else if (iv instanceof SVMDiscriminantExperimentViewer) {
				SVMDiscriminantExperimentViewer eccv = (SVMDiscriminantExperimentViewer)iv;
				e = new Expression((SVMDiscriminantExperimentViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getClusters(), new Integer(eccv.getNumRetainedPos()), new Integer(eccv.getNumRecruitedNeg()), eccv.getDiscriminants(), eccv.getSamplesOrder(), new Boolean(eccv.isClassifyGenes()), eccv.getHeader()});
			} else if (iv instanceof DAM3DViewer) {
				DAM3DViewer eccv = (DAM3DViewer)iv;
				e = new Expression((DAM3DViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getMode()), eccv.getMatrix3D(), new Boolean(eccv.isGeneViewer()), new Integer(eccv.getExperimentID())});
			} else if (iv instanceof GDMGeneViewer) {
				GDMGeneViewer eccv = (GDMGeneViewer)iv;
				e = new Expression((GDMGeneViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getGeneDistMatrix(), eccv.getRawMatrix(), new Integer(eccv.getProbes()), new Integer(eccv.getFeaturesCount()), new Float(eccv.getMinValue()),
						eccv.getDistanceMetric(), new Float(eccv.getOrigMaxValue()), new Float(eccv.getOrigMinValue()), new Float(eccv.getMaxValue()), new Integer(eccv.getMaxGeneNameLength()), 
						eccv.getFieldNames(), new Integer(eccv.getDisplayEvery()), eccv.getClusters(), new Integer(eccv.getNumOfClusters())});
			} else if (iv instanceof GDMExpViewer) {
				GDMExpViewer eccv = (GDMExpViewer)iv;
				e = new Expression((GDMExpViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getExpDistMatrix(), eccv.getRawMatrix(), new Integer(eccv.getProbes()), new Integer(eccv.getFeaturesCount()), new Float(eccv.getMinValue()),
						eccv.getDistanceMetric(), new Float(eccv.getOrigMaxValue()), new Float(eccv.getOrigMinValue()), new Float(eccv.getMaxValue()), new Integer(eccv.getMaxExpNameLength()), 
						new Integer(eccv.getDisplayEvery()), eccv.getClusters(), new Integer(eccv.getNumOfClusters())});
			} else if (iv instanceof PCA2DViewer) {
				PCA2DViewer eccv = (PCA2DViewer)iv;
				e = new Expression((PCA2DViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getUMatrix(), new Boolean(eccv.isGeneViewer()), new Integer(eccv.getAxis1()), new Integer(eccv.getAxis2())});
			} else if (iv instanceof PCA3DViewer) {
				PCA3DViewer eccv = (PCA3DViewer)iv;
				e = new Expression((PCA3DViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), new Boolean(eccv.isGeneViewer()), eccv.getU(), new Integer(eccv.getMode()), new Integer(eccv.getXAxis()), new Integer(eccv.getYAxis()), new Integer(eccv.getZAxis())});
			} else if (iv instanceof COA2DViewer) {
				COA2DViewer eccv = (COA2DViewer)iv;
				e = new Expression((COA2DViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), eccv.getGeneUMatrix(), eccv.getExptUMatrix(), new Integer(eccv.getGeneOrExpt()), new Integer(eccv.getAxis1()), new Integer(eccv.getAxis2())});
			} else if (iv instanceof COA3DViewer) {
				COA3DViewer eccv = (COA3DViewer)iv;
				e = new Expression((COA3DViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), new Integer(eccv.getGeneOrExpt()), eccv.getGeneUMatrix(), eccv.getExptUMatrix(), eccv.getU(), new Integer(eccv.getXAxis()), new Integer(eccv.getYAxis()), new Integer(eccv.getZAxis())});
			} else if (iv instanceof TerrainViewer) {
				TerrainViewer eccv = (TerrainViewer)iv;
				e = new Expression((TerrainViewer) oldInstance, oldInstance.getClass(), "new",
						new Object[]{new Integer(eccv.getExperimentID()), new Boolean(eccv.isGenes()), eccv.getClusters(), eccv.getWeights(), eccv.getLocations(), new Float(eccv.getSigma()), new Integer(eccv.getLabelIndex())});
			}
		}
		*/ 
		return e;
		
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
 