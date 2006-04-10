/*
 * Created on Nov 9, 2005
 *
 * Creates an XMLEncoder and sets persistence delegates such that the encoder can
 * save the viewers and other attendant classes required for MeV state-saving.
 */
package org.tigr.microarray.mev.persistence;
   
import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLEncoder;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.HistoryViewer;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.ResultTree;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.*;
import org.tigr.microarray.mev.cluster.gui.impl.fom.*;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;
import org.tigr.microarray.mev.cluster.gui.impl.ptm.PTMExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.impl.sota.*;
import org.tigr.microarray.mev.cluster.gui.impl.st.HCLSupportTree;
import org.tigr.util.FloatMatrix;

import com.sun.j3d.utils.universe.Viewer;

/**
 * Factory class for (@link java.beans.XMLEncoder}.  
 * 
 * @author eleanora
 *
 */
public abstract class XMLEncoderFactory {
	
	/** 
	 * Creates an XMLEncoder to be used for saving the state of a MultipleArrayViewer. 
	 * Sets persistence delegates such that the encoder can
	 * save the viewers and other attendant classes required for MeV state-saving.  
	 * PersistenceDelegates for implementations of IViewer are found in 
	 * {@link IViewerPersistenceDelegate}.
	 */
	public static XMLEncoder getMAVEncoder(XMLEncoder oos, ResultTree tree){
		
		DefaultMutableTreeNode analysisNode = tree.getAnalysisNode();
        Enumeration allNodes = analysisNode.breadthFirstEnumeration();
        DefaultMutableTreeNode dmtn;
    	IViewerPersistenceDelegate ivpd = new IViewerPersistenceDelegate();
    	
        //Checks the ResultTree for IViewers and assignes a PersistenceDelegate
        //for each different type found.  All PersistenceDelegate Expression objects
        //for classes extending IViewer are found in the IViewerPersistenceDelegate class. 
        while(allNodes.hasMoreElements()){
        	dmtn = (DefaultMutableTreeNode)allNodes.nextElement();
        	Object o = dmtn.getUserObject();
        	if(o instanceof LeafInfo) {
        		LeafInfo l = (LeafInfo)o;
        		if(l.getViewer() != null) {
        			Object aViewer = l.getViewer();
//        			System.out.println(aViewer.getClass().toString());
        			oos.setPersistenceDelegate(
    						aViewer.getClass(),
							ivpd
    					);
        		}
        	}
        }
        
        //HistoryViewer is an IViewer but is not found in the analysisNode
        oos.setPersistenceDelegate(HistoryViewer.class, ivpd);

        //The classes below are not normally included in the resultTree, but still
        //need to have PersistenceDelegates assigned to them if one of their descendant
        //classes are in the resultTree.  Therefore they are always assigned here.  
        //Any new IViewer parent classes would also need to be included below.  
		oos.setPersistenceDelegate(ExperimentClusterViewer.class, ivpd);
		oos.setPersistenceDelegate(ExperimentViewer.class, ivpd);
		oos.setPersistenceDelegate(CentroidViewer.class, ivpd);
		oos.setPersistenceDelegate(CentroidsViewer.class, ivpd);
		oos.setPersistenceDelegate(ClusterTableViewer.class, ivpd);
		oos.setPersistenceDelegate(ClusterTableViewer.class, ivpd);
		oos.setPersistenceDelegate(ExperimentClusterTableViewer.class, ivpd);
		oos.setPersistenceDelegate(ExperimentClusterCentroidViewer.class, ivpd);
		oos.setPersistenceDelegate(ClusterRepository.class, new ClusterRepositoryPersistenceDelegate());
        oos.setPersistenceDelegate(GraphViewer.class, new ViewerPersistenceDelegate());
        oos.setPersistenceDelegate(Viewer.class, new ViewerPersistenceDelegate());
        
        
        //Below are other miscellaneous classes that need custom PersistenceDelegates to be
        //properly saved.
        oos.setPersistenceDelegate(
    			LeafInfo.class, 
    			new DefaultPersistenceDelegate(LeafInfo.getPersistenceDelegateArgs())
    		);

		oos.setPersistenceDelegate(
				ClusterList.class, 
				new DefaultPersistenceDelegate(ClusterList.getPersistenceDelegateArgs())
		);
		oos.setPersistenceDelegate(
				Cluster.class, 
				Cluster.getPersistenceDelegate()
		);
		oos.setPersistenceDelegate(
				MultipleArrayData.class, 
				new MultipleArrayDataPersistenceDelegate()
		);
		oos.setPersistenceDelegate(
				SlideData.class, 
				new ISlideDataPersistenceDelegate()
		);
		oos.setPersistenceDelegate(
				FloatSlideData.class, 
				new ISlideDataPersistenceDelegate()
		);
		oos.setPersistenceDelegate(
				ExperimentHeader.class, 
				new ExperimentHeaderPersistenceDelegate()
    	);
		oos.setPersistenceDelegate(
				Experiment.class, 
				new DefaultPersistenceDelegate(Experiment.getPersistenceDelegateArgs())
	    );
		oos.setPersistenceDelegate(
				HCLTree.class,
				HCLTree.getPersistenceDelegate()
				//new DefaultPersistenceDelegate(HCLTree.getPersistenceDelegateArgs())
			);
		oos.setPersistenceDelegate(
				org.tigr.microarray.mev.cluster.gui.impl.tease.HCLTree.class,
				org.tigr.microarray.mev.cluster.gui.impl.tease.HCLTree.getPersistenceDelegate()
			);
		oos.setPersistenceDelegate(
				HCLSupportTree.class,
				HCLSupportTree.getPersistenceDelegate()
				//new DefaultPersistenceDelegate(HCLSupportTree.getPersistenceDelegateArgs())
			);
		oos.setPersistenceDelegate(
				ExperimentClusterHeader.class,
				new DefaultPersistenceDelegate(ExperimentClusterHeader.getPersistenceDelegateArgs())
		);
		oos.setPersistenceDelegate(
				FloatMatrix.class,
				new DefaultPersistenceDelegate(FloatMatrix.getPersistenceDelegateArgs())
		);

		oos.setPersistenceDelegate(
				FOMContentComponent.class,  
				new DefaultPersistenceDelegate(FOMContentComponent.getPersistenceDelegateArgs())
			);
		oos.setPersistenceDelegate(
				CastFOMContentComponentB.class,  
				new DefaultPersistenceDelegate(CastFOMContentComponentB.getPersistenceDelegateArgs())
			);
		oos.setPersistenceDelegate(
				CastFOMContentComponentA.class,  
				new DefaultPersistenceDelegate(CastFOMContentComponentA.getPersistenceDelegateArgs())
			);

		oos.setPersistenceDelegate(
				PTMExperimentHeader.class,  
				PTMExperimentHeader.getPersistenceDelegate()
			);

		oos.setPersistenceDelegate(
				SOTATreeData.class,
				new SOTATreeDataPersistenceDelegate()
			);
		
		oos.setPersistenceDelegate(
				CentroidExperimentHeader.class,
				CentroidExperimentHeader.getPersistenceDelegate()
			);
		
		return oos;
	}
}
