/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptDataTransformer.java
 *
 * Created on March 26, 2004, 10:24 AM
 */

package org.tigr.microarray.mev.script.util;

import java.util.Arrays;
import java.util.Vector;

import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;
import org.tigr.microarray.util.Adjustment;

import org.tigr.microarray.mev.DetectionFilter;
import org.tigr.microarray.mev.FoldFilter;
import org.tigr.microarray.mev.ISlideData;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;


/** The ScriptDataTransformer class supports script execution by providing
 * methods to produce new Experiment objects with trimmed gene or experiment
 * lists representing results from scripting algorithms.  The class also handles
 * the execution of all Adjustment class algorithms and all of the Cluster Selection
 * class algorithms.
 * @author braisted
 */
public class ScriptDataTransformer {
    
    /** The base Experiment object to be modified by the transformer.
     */    
    private Experiment experiment;
    /** MeV's framework object.
     */    
    private IFramework framework;
    
    /** Creates a new instance of ScriptDataTransformer
     * @param experiment Experiment object, target of transformation
     * @param framework MeV IFramework
     */
    public ScriptDataTransformer(Experiment experiment, IFramework framework) {
        this.experiment = experiment.copy();
        this.framework = framework;
    }
    
    /** Transforms the Experiment object according to the AlgorithmData's parameter
     * set.  These transformations include processing of Adjustment class algorithms.
     * @param data AlgorithmData holding the parameters used of a transformation.
     * @return
     */    
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
            //create gene list where all must excede limits
            
        } else if(algName.equals("Affy Detection Filter")) {
            experiment = applyAffyDetectionFilter(data);
        } else if(algName.equals("Affy Fold Filter")) {
            experiment = applyAffyFoldFilter(data);
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
    
    
    /** Creates a new experiment based on cut-off criteria (%).  Genes are retained
     * if they have more than the criteria % of valid expression values over the
     * loaded samples.
     * @param percent Percentage criteria.
     * @return
     */    
    private Experiment createPercentCutoffExperiment(float percent) {
       
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
    
    
    /** Trims the experiment based on index list.  Boolean value indicates if
     * its a gene or experment trim.
     * @param indices Element indices to retain
     * @param geneCut if true genes will be trimmed, else experiments will be trimmed
     * @return
     */    
    public Experiment getTrimmedExperiment(int [] indices, boolean geneCut) {
        if(geneCut)
            return getReducedExperiment_GeneReduction(indices);
        return getReducedExperiment_ExperimentReduction(indices);
    }
    
    
    /** Specifically trims genes based on passed indices which are to be
     * retained.
     * @param indices Gene indices to retain
     * @return
     */    
    private Experiment getReducedExperiment_GeneReduction(int [] indices) {
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
    
    /** Trims experiments based on indices to retain
     * @param colIndices Experiment indicies to retain
     * @return
     */    
    private Experiment getReducedExperiment_ExperimentReduction(int [] colIndices) {
        FloatMatrix fm = experiment.getMatrix();
        int rowCount = fm.getRowDimension();
        float [][] matrix = new float[rowCount][colIndices.length];
        
        for(int i = 0; i < rowCount; i++) {
            for(int j = 0; j < colIndices.length; j++) {
                matrix[i][j] = fm.A[i][colIndices[j]];
            }
        }
        return new Experiment(new FloatMatrix(matrix), colIndices, experiment.getRowMappingArrayCopy());
    }
    
    /** This is the main support for cluster selection class algorithms.
     * The resulting indices represent the selected clusters and the
     * AlgorithmData passed in is augmented with values upon which the
     * selection was based, e.g. calculated cluter diversities or
     * centroid varibilities.  Note tht the input AlgorithmData indicates
     * the selection algorithm and parameters as well as the critical boolean
     * indicator to direct the selection of gene vs. experiment clusters.
     * @param algData AlgorithmData containing the parameters.
     * @param clusters Cluster indices
     * @return
     */
    public int [][] selectClusters(AlgorithmData algData, int [][] clusters) {
        AlgorithmParameters params = algData.getParams();
        int numOfDesiredClusters = params.getInt("desired-cluster-count");
        int minClusterSize = params.getInt("minimum-cluster-size");
        boolean areGeneClusters = params.getBoolean("process-gene-clusters");
        String algName = params.getString("name");
        int [][] selectedClusters;
        int [][] orderedClusters;
        
        if(algName.equals("Diversity Ranking Cluster Selection")) {
            int function = params.getInt("distance-function");
            boolean useAbsolute = params.getBoolean("use-absolute");
            boolean useCentroidBasedDiversity = params.getBoolean("use-centroid-based-variability");
            orderedClusters = getClustersBasedOnDiversityRank(algData, experiment.getMatrix(), clusters, areGeneClusters, useCentroidBasedDiversity, function, useAbsolute);
            
            int clusterCount = 0;
            Vector clusterVector = new Vector();
            for(int i = 0; i < orderedClusters.length && clusterCount < numOfDesiredClusters; i++) {
                if(orderedClusters[i].length >= minClusterSize) {
                    clusterVector.add(orderedClusters[i]);
                    clusterCount++;
                }
            }
            
            selectedClusters = new int[clusterVector.size()][];
            
            for(int i = 0; i < selectedClusters.length; i++)
                selectedClusters[i] = (int [])(clusterVector.elementAt(i));
            
        } else { // (if other algorithms are added --> if(algName.equals("Centroid Entropy/Variance Ranking Cluster Selection")) {
            FloatMatrix matrix = this.experiment.getMatrix();
            
            if(!areGeneClusters)
                matrix = matrix.transpose();
            
            FloatMatrix means = getMeans(matrix, clusters);
            
            //restore matrix
            if(!areGeneClusters)
                matrix = matrix.transpose();
            
            boolean useCentroidVariance = params.getBoolean("use-centroid-variance");
            if(useCentroidVariance)
                orderedClusters = getClustersBasedOnVarianceRank(algData, means, clusters, areGeneClusters);
            else
                orderedClusters = getClustersBasedOnEntropyRank(algData, means, clusters, areGeneClusters);
            
            int clusterCount = 0;
            Vector clusterVector = new Vector();
            for(int i = 0; i < orderedClusters.length && clusterCount < numOfDesiredClusters; i++) {
                if(orderedClusters[i].length >= minClusterSize) {
                    clusterVector.add(orderedClusters[i]);
                    clusterCount++;
                }
            }
            
            selectedClusters = new int[clusterVector.size()][];
            
            for(int i = 0; i < selectedClusters.length; i++)
                selectedClusters[i] = (int [])(clusterVector.elementAt(i));
        }
        return selectedClusters;
    }
    
    
    /** Applys the diveristy rank cluster selection.
     * @param algData parameters
     * @param data Input data
     * @param inputClusters clusters
     * @param geneClusters indicates nature of input clusters
     * @param useCentroids indicates if centroids should be used or if diversity should
     * be intra-gene distances.
     * @param function distance function
     * @param absolute is distance absolute
     * @return
     */    
    private int [][] getClustersBasedOnDiversityRank(AlgorithmData algData, FloatMatrix data, int [][] inputClusters, boolean geneClusters, boolean useCentroids, int function, boolean absolute) {
        FloatMatrix means;
        float [] diversities;
        int [][] newClusters = new int[inputClusters.length][];
        
        //insures that means are correct
        if(!geneClusters)
            data = data.transpose();
        
        if(useCentroids) {
            means = getMeans(data, inputClusters);
            diversities = getCentroidBasedDiversities(data, means, inputClusters, function, absolute);
        } else {
            diversities = getGeneBasedDiversities(data, inputClusters, function, absolute);
        }
        
        QSort sort = new QSort(diversities);
        diversities = sort.getSorted();
        
        int [] origOrder = sort.getOrigIndx();
        
        for(int i = 0; i < newClusters.length; i++)
            newClusters[i] = inputClusters[origOrder[i]];
        
        //store results
        String [] diversityArray = new String[diversities.length];
        String [] clusterPop = new String[diversities.length];
        for(int i = 0; i < diversities.length; i++) {
            diversityArray[i] = String.valueOf(diversities[i]);
            clusterPop[i] = String.valueOf(newClusters[i].length);
        }
        algData.addStringArray("diversity-value-array", diversityArray);
        algData.addStringArray("cluster-population-array", clusterPop);
        
        
        //transpose to restore original ordering
        if(!geneClusters)
            data = data.transpose();
        
        return newClusters;
    }
    
    
    /**  Calculates means for the clusters
     * @param data Expression matrix
     * @param clusters cluster indices
     * @return
     */
    private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    
    /**  Returns a set of means for an element
     * @return
     * @param data input data
     * @param indices indices to use */
    private float [] getMeans(FloatMatrix data, int [] indices){
        int nSamples = data.getColumnDimension();
        float [] means = new float[nSamples];
        float sum = 0;
        float n = 0;
        float value;
        for(int i = 0; i < nSamples; i++){
            n = 0;
            sum = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j],i);
                if(!Float.isNaN(value)){
                    sum += value;
                    n++;
                }
            }
            if(n > 0)
                means[i] = sum/n;
            else
                means[i] = Float.NaN;
        }
        return means;
    }
    
    /** Gest the centroid based diversities
     * @return
     * @param data Input data
     * @param means Centroids (means patterns)
     * @param clusters cluster indicies
     * @param function distance function
     * @param absolute use absolute distance? */    
    private float [] getCentroidBasedDiversities(FloatMatrix data, FloatMatrix means, int [][] clusters, int function, boolean absolute) {
        float [] div = new float[clusters.length];
        
        for(int i = 0; i < div.length; i++) {
            div[i] = 0;
            for(int j = 0; j < clusters[i].length; j++) {
                div[i] += ExperimentUtil.geneDistance(means, data, i, clusters[i][j], function, 1.0f, absolute);
            }
            div[i] /= clusters[i].length;
        }
        return div;
    }
    
    /** get diversity based on intra gene diatance.
     * @param data
     * @param clusters
     * @param function
     * @param absolute
     * @return
     */    
    private float [] getGeneBasedDiversities(FloatMatrix data, int [][] clusters, int function, boolean absolute) {
        float [] div = new float[clusters.length];
        
        //for each cluser
        for(int i = 0; i < div.length; i++) {
            div[i] = 0;
            for(int j = 0; j < clusters[i].length; j++) {
                for(int k = 0; k < clusters[i].length/2; k++) {
                    div[i] += ExperimentUtil.geneDistance(data, data, clusters[i][j], clusters[i][k], function, 1.0f, absolute);
                }
            }
            div[i] /= (Math.pow(clusters[i].length, 2.0))/2+(clusters[i].length)/2;
        }
        return div;
    }
    
    /** Select clusters based on variance ranking
     * @param algData
     * @param means
     * @param clusters
     * @param areGeneClusters
     * @return  */    
    private int [][] getClustersBasedOnVarianceRank(AlgorithmData algData, FloatMatrix means, int [][] clusters, boolean areGeneClusters) {
        float [] variances = getCentroidVariances(means);
        int [][] newClusters = new int[clusters.length][];
        
        QSort sort = new QSort(variances, QSort.DESCENDING);
        variances = sort.getSorted();
        
        int [] origOrder = sort.getOrigIndx();
        
        for(int i = 0; i < newClusters.length; i++)
            newClusters[i] = clusters[origOrder[i]];
        
        //store results
        String [] varianceArray = new String[variances.length];
        String [] clusterPop = new String[variances.length];
        for(int i = 0; i < variances.length; i++) {
            varianceArray[i] = String.valueOf(variances[i]);
            clusterPop[i] = String.valueOf(newClusters[i].length);
        }
        algData.addStringArray("diversity-value-array", varianceArray);
        algData.addStringArray("cluster-population-array", clusterPop);
        
        return newClusters;
    }
    
    /** returns centroid based variances
     */    
    private float [] getCentroidVariances(FloatMatrix means) {
        float [] vars = new float[means.getRowDimension()];
        int cols = means.getColumnDimension();
        float sos;
        float mean;
        
        for(int i = 0; i < vars.length; i++) {
            sos = 0;
            mean = getMean(means.A[i]);
            for(int j = 0; j < cols ; j++) {
                sos += Math.pow((means.A[i][j]-mean),2);
            }
            vars[i] = sos;
        }
        return vars;
    }
    
    /** returns a mean value for input
     */    
    private float getMean(float [] vals) {
        float mean = 0;
        int n = 0;
        for(int i = 0; i < vals.length; i++) {
            if(!Float.isNaN(vals[i])) {
                n++;
                mean += vals[i];
            }
        }
        return (n > 0 ? ((float)(mean/n)) : 0f);
    }
    
    /** returns clusters based on entropy ranking.
     */    
    private int [][] getClustersBasedOnEntropyRank(AlgorithmData algData, FloatMatrix means, int [][] clusters, boolean areGeneClusters) {
        float [] entropies = new float[means.getRowDimension()];
        
        for(int i = 0; i < entropies.length; i++) {
            entropies[i] = (float)(getEntropy(means.A[i]));
        }
        
        int [][] newClusters = new int[clusters.length][];
        
        QSort sort = new QSort(entropies, QSort.DESCENDING);
        entropies = sort.getSorted();
        
        int [] origOrder = sort.getOrigIndx();
        
        for(int i = 0; i < newClusters.length; i++)
            newClusters[i] = clusters[origOrder[i]];
        
        //store results
        String [] varianceArray = new String[entropies.length];
        String [] clusterPop = new String[entropies.length];
        for(int i = 0; i < entropies.length; i++) {
            varianceArray[i] = String.valueOf(entropies[i]);
            clusterPop[i] = String.valueOf(newClusters[i].length);
        }
        algData.addStringArray("diversity-value-array", varianceArray);
        algData.addStringArray("cluster-population-array", clusterPop);
        
        return newClusters;
    }
    
    
    /** returns the entropy of a set of values, entropy method extracted from
     * RN.java.
     */    
    private double getEntropy(float[] pVector) {
        int c_DecileCount = 10;
        double fltMin = Double.MAX_VALUE;
        double fltMax = -Double.MAX_VALUE;
        int i=0;
        int[] arrDeciles = new int[c_DecileCount];
        
        final int iSize = pVector.length;
        int iValCount = 0;
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            fltMin = Math.min(fltMin, pVector[i]);
            fltMax = Math.max(fltMax, pVector[i]);
            iValCount++;
        }
        
        double fltStep = (fltMax-fltMin)/(c_DecileCount);
        if (fltStep == 0d) {
            return -1.0*Math.log(1.0)/(Math.log(2.0));
        }
        
        if (fltMin == Double.MAX_VALUE)
            return 0d;
        
        Arrays.fill(arrDeciles, 0);
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            int iDecileInd = (int)Math.ceil((pVector[i]-fltMin)/fltStep)-1;
            if (iDecileInd < 0) {
                iDecileInd = 0;
            }
            arrDeciles[iDecileInd]++;
        }
        if (iValCount == 0)
            return 0d;
        
        // finally, calculate entropy
        double dblEntropy=0;
        
        for (i=0; i<c_DecileCount; i++) {
            if (arrDeciles[i] == 0) {
                continue;
            }
            double dblPx=((double)arrDeciles[i])/iValCount;
            dblEntropy += dblPx*Math.log(dblPx)/(Math.log(2.0)); // log2(x)==log(x)/log(2)
        }
        return -dblEntropy;
    }
    
    /** Applies the detection filter trim
     */    
    private Experiment applyAffyDetectionFilter(AlgorithmData algData) {

        //parameters
        AlgorithmParameters params = algData.getParams();
        
        //get IData
        IData data = framework.getData();
        
        //extract indices in Experiment to check
        int [] rows = experiment.getRowMappingArrayCopy();
        int [] cols = experiment.getColumnIndicesCopy();
        int numGenes = experiment.getNumberOfGenes();
        int expCount = experiment.getNumberOfSamples();

        
        //construct DetectionFilter and set properties
        String [] expNames = new String[expCount];
        for(int i = 0; i < expNames.length; i++) {
            expNames[i] = data.getFullSampleName(cols[i]);
        }
        DetectionFilter filter = new DetectionFilter(expNames);
        filter.set_both(params.getBoolean("is-required-in-both-groups"));
      
        int [] numReq = algData.getIntArray("number-required");
        //  filter.set_num_required(algData.getIntArray("number-required"));
        //this takes a group number, and a number required
        for(int i = 0; i < numReq.length; i++) {
            filter.set_num_required(i, numReq[i]);
        }
        
        int [] groupMembership = algData.getIntArray("group-memberships");
        //filter.set_group_membership();
        //takes a group index and the file (col) index?
        for(int i = 0; i < groupMembership.length; i++) {
            filter.set_group_membership(groupMembership[i], i);
        }
        
        String [] detectionCalls = new String[expCount];
        String detection;
        ISlideData slideData;  //supports getDetection(int row)
        boolean [] isPresent = new boolean[numGenes]; 
        int numPresent = 0; 
        boolean present;
        
        //tally present genes
        for(int probe = 0; probe < numGenes; probe++) {         
            for(int exp = 0; exp < expCount; exp++) {
                detectionCalls[exp] = data.getFeature(cols[exp]).getDetection(rows[probe]);                
            }                        
            present = filter.keep_gene(detectionCalls);
            if(present) {
                isPresent[probe] = true;
                numPresent++;
            }
        }

        //construct Experiment
        FloatMatrix matrix = experiment.getMatrix();
        float [][] values = new float[numPresent][expCount];
        int [] rowMap = new int[numPresent];
        int cnt = 0;
        for(int i = 0; i < numGenes; i++) {
            if(isPresent[i]) {                
                rowMap[cnt] = rows[i];
                values[cnt] = matrix.A[i];                
                cnt++;
            }
        }
        
        return (new Experiment(new FloatMatrix(values), cols, rowMap));
    }
    


    /** applies the affy fold filter trim
     * @param algData
     * @return  */    
    private Experiment applyAffyFoldFilter(AlgorithmData algData) {
        
        //parameters
        AlgorithmParameters params = algData.getParams();
        
        //get IData
        IData data = framework.getData();
        FloatMatrix matrix = experiment.getMatrix();
                
        //extract indices in Experiment to check
        int [] rows = experiment.getRowMappingArrayCopy();
        int [] cols = experiment.getColumnIndicesCopy();
        int numGenes = experiment.getNumberOfGenes();
        int expCount = experiment.getNumberOfSamples();

        
        //construct DetectionFilter and set properties
        String [] expNames = new String[expCount];
        for(int i = 0; i < expNames.length; i++) {
            expNames[i] = data.getFullSampleName(cols[i]);
        }
        FoldFilter filter = new FoldFilter(expNames);
        float foldChange = params.getFloat("fold-change");
        int [] numReq = algData.getIntArray("number-of-members");
        String divider = params.getString("divider-string");
        
        filter.set_fold_change(foldChange);
        filter.set_divider(divider);
        
        int [] groupMembership = algData.getIntArray("group-memberships");

        for(int i = 0; i < groupMembership.length; i++) {
            filter.set_group_membership(groupMembership[i], i);
        }
        
        float [] foldHits = new float[expCount];
        String detection;
        ISlideData slideData;  //supports getDetection(int row)
        boolean [] isPresent = new boolean[numGenes]; 
        int numPresent = 0; 
        boolean present;
        float [] vals = new float[numGenes];
        //tally present genes
        for(int probe = 0; probe < numGenes; probe++) {         
           //for(int exp = 0; exp < expCount; exp++) {
           //     foldHits[exp] = data.getFeature(cols[exp]).get_fold_change(rows[probe]);                
            //}                        
            present = filter.keep_gene(matrix.A[probe]);
            if(present) {
                isPresent[probe] = true;
                numPresent++;
            }
        }

        //construct Experiment
        float [][] values = new float[numPresent][expCount];
        int [] rowMap = new int[numPresent];
        int cnt = 0;
        for(int i = 0; i < numGenes; i++) {
            if(isPresent[i]) {                
                rowMap[cnt] = rows[i];
                values[cnt] = matrix.A[i];                
                cnt++;
            }
        }
        
        return (new Experiment(new FloatMatrix(values), cols, rowMap));
   
        }
        
        
    
}
