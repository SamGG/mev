   
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

		return e;
		
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
 