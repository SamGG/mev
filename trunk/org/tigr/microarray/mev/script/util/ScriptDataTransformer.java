/*
 * ScriptDataTransformer.java
 *
 * Created on March 26, 2004, 10:24 AM
 */

package org.tigr.microarray.mev.script.util;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.util.Adjustment;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

import org.tigr.microarray.mev.cluster.gui.Experiment;

/**
 *
 * @author  braisted
 */
public class ScriptDataTransformer {
    
    private Experiment experiment;
    
    /** Creates a new instance of ScriptDataTransformer */
    public ScriptDataTransformer(Experiment experiment) {
        this.experiment = experiment.copy();
    }
    
    public Experiment transformData(AlgorithmData data) {
        AlgorithmParameters params = data.getParams();
        
        java.util.Map map = params.getMap();
        java.util.Set keys = map.keySet();
        java.util.Iterator iter = keys.iterator();
        Object obj;
        while(iter.hasNext()) {
            obj = iter.next();
            System.out.println("key ="+obj+" value ="+ map.get(obj));
        }    
        
        String algName = params.getString("name");
        
        System.out.println("script transformer algName="+algName);
        if(algName == null)
            return null;
        
        if(algName.equals("Percentage Cutoff")) {
            float percent = params.getFloat("percent-cutoff");
            experiment = createPercentCutoffExperiment(percent);
        } else if (algName.equals("Lower Cutoff")) {
            float cy3Cutoff = params.getFloat("cy3-lower-cutoff");
            float cy5Cutoff = params.getFloat("cy5-lower-cutoff");
        } else if(algName.equals("Normalize Spots")) {
            Adjustment.normalizeSpots(experiment.getMatrix());
        } else if(algName.equals("Divide Spots by RMS")) {
            Adjustment.divideSpotsRMS(experiment.getMatrix());
        } else if(algName.equals("Divide Spots by SD")) {
            Adjustment.divideSpotsSD(experiment.getMatrix());
        } else if(algName.equals("Mean Center Spots")) {
            Adjustment.meanCenterSpots(experiment.getMatrix());
        } else if(algName.equals("Median Center Spots")) {
            Adjustment.medianCenterSpots(experiment.getMatrix());
        } else if(algName.equals("Digital Spots")) {
            Adjustment.digitalSpots(experiment.getMatrix());
        } else if(algName.equals("Normalize Experiments")) {
            Adjustment.normalizeExperiments(experiment.getMatrix());
        } else if(algName.equals("Divide Experiments by RMS")) {
            Adjustment.divideExperimentsRMS(experiment.getMatrix());
        } else if(algName.equals("Divide Experiments by SD")) {
            Adjustment.divideExperimentsSD(experiment.getMatrix());
        } else if(algName.equals("Mean Center Experiments")) {
            Adjustment.meanCenterExperiments(experiment.getMatrix());
        } else if(algName.equals("Median Center Experiments")) {
            Adjustment.medianCenterExperiments(experiment.getMatrix());
        } else if(algName.equals("Digital Experiments")) {
            Adjustment.digitalExperiments(experiment.getMatrix());
        }
        return experiment;
    }
    
    
    private Experiment createPercentCutoffExperiment(float percent) {
        
        System.out.println("In percent cutoff");
        
        FloatMatrix fm = experiment.getMatrix();
        int [] origRowMap = experiment.getRowMappingArrayCopy();
        int colCount = fm.getColumnDimension();
        int validExperimentCount = (int) (colCount * (percent/100f));        
        boolean [] isValid = new boolean[fm.getRowDimension()];
        int cnt;
        int validCount = 0;
        
        //validate genes
        for(int i = 0; i < isValid.length; i++) {
            cnt = 0;
            for(int j = 0; j < colCount; j++) {
                if(!Float.isNaN(fm.A[i][j]))
                    cnt++;
                if(cnt > validExperimentCount) {
                    isValid[i] = true;
                    validCount++;
                    break;
                }
            }
        }
        
        float [][] matrix = new float[validCount][colCount];
        int [] newRowMap = new int[validCount];
        int currRow = 0;
        
        for(int i = 0; i < fm.A.length; i++) {
            if(isValid[i]) {
                newRowMap[currRow] = origRowMap[i];
                for(int j = 0; j < colCount; j++) {
                    matrix[currRow][j] = fm.A[i][j];
                }
                currRow++;
            }
        }
        
        return new Experiment(new FloatMatrix(matrix), experiment.getColumnIndicesCopy(), newRowMap);
    }
    
    
    public Experiment getTrimmedExperiment(int [] indices) {
        FloatMatrix fm = experiment.getMatrix();
        int [] origRowMap = experiment.getRowMappingArrayCopy();
        int colCount = fm.getColumnDimension();                
        float [][] matrix = new float[indices.length][colCount];        
        int [] newRowMap = new int[indices.length];
        int currRow = 0;

        int dataRow = 0;
        for(int i = 0; i < indices.length; i++) {
            dataRow = origRowMap[indices[i]];
            newRowMap[i] = dataRow;
            for(int j = 0; j < colCount; j++) {
                    matrix[i][j] = fm.A[indices[i]][j];
            }            
        }      
        return new Experiment(new FloatMatrix(matrix), experiment.getColumnIndicesCopy(), newRowMap);
    }
    
    
}
