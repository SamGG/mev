package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.beans.Expression;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.util.FloatMatrix;

public class SAMClusterTableViewer extends ClusterTableViewer {
    public SAMClusterTableViewer(Experiment experiment, int[][] clusters,
			IData data, String[] auxTitles, float[][] auxData) {
    	super(experiment, clusters, data, auxTitles, getObjectArray(auxData));
    }
    public SAMClusterTableViewer(Experiment experiment, int[][] clusters,
			IData data, String[] auxTitles, FloatMatrix auxData) {
    	this(experiment, clusters, data, auxTitles, auxData.A);
    }
    
    public static Object[][] getObjectArray(float[][] auxData) {//class of auxData matches constructor param
    	Object[][] temparray = new Object[auxData.length][];
    	for(int i=0; i<auxData.length; i++) {
    		temparray[i] = new Float[auxData[i].length];
    		for(int j=0; j<auxData[i].length; j++)
    			temparray[i][j] = new Float(auxData[i][j]);
    	}
    	return temparray;
    }

    public Expression getExpression(){
		try {
			String[] auxTitles = getAuxTitles();
			Object[][] auxData = getAuxData();
			float[][] A = new float[auxData.length][];
			for(int i=0; i<auxData.length; i++) { 
				A[i] = new float[auxData[i].length];
				for(int j=0; j<auxData[i].length; j++)
					A[i][j] = ((Float)auxData[i][j]).floatValue();
			}
			FloatMatrix fm = new FloatMatrix(A);
	    	return new Expression(this, this.getClass(), "new",
					new Object[]{getExperiment(), getClusters(), getData(), auxTitles, fm});
		} catch (Exception e) {
			e.printStackTrace();
	    	return new Expression(this, this.getClass(), "new",
					new Object[]{getExperiment(), getClusters(), getData(), null, null});
		}

    }
}
