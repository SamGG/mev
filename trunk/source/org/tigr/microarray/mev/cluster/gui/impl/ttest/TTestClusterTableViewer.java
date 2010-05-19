package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.beans.Expression;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;

/**
 * ClusterTableViewer for displaying tabular summary data for a TTEST run.
 * @author Eleanor
 *
 */
public class TTestClusterTableViewer extends ClusterTableViewer {
	private TTestResults results;
	
	public TTestClusterTableViewer(Experiment experiment, ClusterWrapper clusters, IData data, TTestResults results) {
		super(experiment, clusters, data, getAuxTitles(results), getAuxData(results, experiment));
		this.results = results;
	}
	
	private static String[] getAuxTitles(TTestResults results) {
        Vector<String> titlesVector = new Vector<String>();
        if ((results.getTTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) || (results.getTTestDesign() == TtestInitDialog.PAIRED)) {
            titlesVector.add("GroupA mean");
            titlesVector.add("GroupA std.dev.");
            titlesVector.add("GroupB mean");
            titlesVector.add("GroupB std.dev.");
            titlesVector.add("Absolute t value");
        } else if (results.getTTestDesign() == TtestInitDialog.ONE_CLASS) {
            titlesVector.add("Gene mean");
            titlesVector.add("Gene std.dev.");
            titlesVector.add("t value");
        }
        titlesVector.add("Degrees of freedom");
        titlesVector.add("Raw p value");
        //titlesVector.add("Adj p value");
        if ((results.getSignificanceMethod() == TtestInitDialog.FALSE_NUM)||(results.getSignificanceMethod() == TtestInitDialog.FALSE_PROP)) {
            if (results.isCalculateAdjFDRPVals())
                titlesVector.add("Adj p value");
        } else {
            titlesVector.add("Adj p value");
        }            
        titlesVector.add("False Discovery Rate");
        
        String[] auxTitles = new String[titlesVector.size()];
        for (int i = 0; i < auxTitles.length; i++) {
            auxTitles[i] = (String)(titlesVector.get(i));
        }
        return auxTitles;
	}
	private static Object[][] getAuxData(TTestResults results, Experiment experiment) {
        Object[][] auxData = new Object[experiment.getNumberOfGenes()][getAuxTitles(results).length];
        for (int i = 0; i < auxData.length; i++) {
            int counter = 0;
            if ((results.getTTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) || (results.getTTestDesign() == TtestInitDialog.PAIRED)) {
                auxData[i][counter++] = new Float(results.getMeansAMatrix().A[i][0]);
                auxData[i][counter++] = new Float(results.getSdAMatrix().A[i][0]);
                auxData[i][counter++] = new Float(results.getMeansBMatrix().A[i][0]);
                auxData[i][counter++] = new Float(results.getSdBMatrix().A[i][0]);
            } else if (results.getTTestDesign() == TtestInitDialog.ONE_CLASS) {
            	Float temp = new Float(results.getOneClassMeansMatrix().A[i][0]);
                auxData[i][counter++] = temp;
                Float temp2 = new Float(results.getOneClassSDsMatrix().A[i][0]);
                auxData[i][counter++] = temp2;
            }
            auxData[i][counter++] = new Float(results.getTValuesMatrix().A[i][0]);
            auxData[i][counter++] = new Float(results.getDfMatrix().A[i][0]);
            auxData[i][counter++] = new Float(results.getRawPValuesMatrix().A[i][0]);
            if ((results.getSignificanceMethod() == TtestInitDialog.FALSE_NUM)||(results.getSignificanceMethod() == TtestInitDialog.FALSE_PROP)) {
                if (results.isCalculateAdjFDRPVals())
                    auxData[i][counter++] = new Float(results.getAdjPValuesMatrix().A[i][0]);
            } else {
                auxData[i][counter++] = new Float(results.getAdjPValuesMatrix().A[i][0]);
            }
            auxData[i][counter++] = new Float(results.getQValuesMatrix().A[i][0]);
        }
        return auxData;
	}
	
	public Expression getExpression() {
    	return new Expression(this, this.getClass(), "new",
				new Object[]{getExperiment(), ClusterWrapper.wrapClusters(getClusters()), getData(), results});

	}
}
