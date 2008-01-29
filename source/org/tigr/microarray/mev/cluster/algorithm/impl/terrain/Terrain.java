/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Terrain.java,v $
 * $Revision: 1.3 $
 * $Date: 2006-02-23 20:59:46 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Vector2f;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;
import org.tigr.microarray.mev.cluster.algorithm.impl.util.FloatArray;
import org.tigr.microarray.mev.cluster.algorithm.impl.util.IntArray;
import org.tigr.util.FloatMatrix;

public class Terrain extends AbstractAlgorithm implements InterfaceToObjects {

    private FloatMatrix experiment;
    private IntArray[] links;
    private FloatArray[] distances;
    private float[][] coords;
    private boolean stop = false;

    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        AlgorithmParameters map = data.getParams();
        int function = map.getInt("distance-function", PEARSON);
        float factor = map.getFloat("distance-factor", 1.0f);
        boolean absolute = map.getBoolean("distance-absolute", false);
        int k_neighbours = map.getInt("neighbors");
        this.experiment = data.getMatrix("experiment");

        // TODO: change algo to work without copy of the experiment
        if (!map.getBoolean("use-genes")) {
            FloatMatrix matrix = new FloatMatrix(experiment.getColumnDimension(), experiment.getRowDimension());
            for (int i=0; i<experiment.getRowDimension(); i++)
                for (int j=0; j<experiment.getColumnDimension(); j++)
                    matrix.set(j, i, experiment.get(i, j));
            this.experiment = matrix;
        }

        int nGenes = this.experiment.getRowDimension();
        int sum = nGenes*(nGenes+1)/2;
        int step = sum/100+1;

        //cache distances
        MergeJoinBag bag = new MergeJoinBag(nGenes, Math.min(k_neighbours, nGenes));

        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Analyzing Links...");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);

        int progress = 0;
        for (int nCurIndOuter=0; nCurIndOuter<nGenes; nCurIndOuter++) {
            if (this.stop) {
                throw new AbortException();
            }
            progress++;
            // calculation
            for (int nCurIndInner=nCurIndOuter+1; nCurIndInner<nGenes; nCurIndInner++) {
                float value = ExperimentUtil.geneDistance(this.experiment, null, nCurIndOuter, nCurIndInner, function, factor, absolute);
                //value = value*value;
                bag.Assert(nCurIndOuter, nCurIndInner, value);
                // progress events handling
                progress++;
                if (progress%step == 0) {
                    event.setIntValue(progress/step);
                    event.setDescription("Analysing Links...");
                    fireValueChanged(event);
                }
            }
        }
        bag.Normalize(0.01f);

        this.links = new IntArray[nGenes];
        this.distances = new FloatArray[nGenes];
        for (int i=0; i<this.links.length; i++) {
            this.links[i] = new IntArray();
            this.links[i].add(i);
            this.distances[i] = new FloatArray();
            this.distances[i].add(0f);
        }
        int bagRowCount = bag.getRowCount();
        int bagColCount = bag.getColumnCount();
        for (int i=0; i<bagRowCount; i++) {
            int[] pIntVect = bag.getIndVector(i);
            float[] pFloatVect = bag.getValVector(i);
            for (int j=0; j<bagColCount; j++) {
                if (pIntVect[j]>=0 && pFloatVect[j]>0) {
                    this.links[i].add(pIntVect[j]);
                    this.distances[i].add(pFloatVect[j]);
                    this.links[pIntVect[j]].add(i);
                    this.distances[pIntVect[j]].add(pFloatVect[j]);
                }
            }
        }
        // copy nodes to the cluster structure
        Cluster cluster = new Cluster();
        NodeList clusterNodeList = cluster.getNodeList();
        clusterNodeList.ensureCapacity(nGenes);
        for (int i=0; i<nGenes; i++) {
            NodeValueList values = new NodeValueList(1);
            values.addNodeValue(new NodeValue("weights", this.distances[i].toArray()));
            Node node = new Node(this.links[i].toArray());
            node.setValues(values);
            clusterNodeList.addNode(node);
        }

        int[][] clusters = new int[this.links.length][];
        for (int i=0; i<clusters.length; i++)
            clusters[i] = this.links[i].toArray();

        this.coords = doCircularLayout(clusters);

        FDGLAlgoT fdgl = new FDGLAlgoT(this);
        fdgl.InitFromInterface();

        event.setId(AlgorithmEvent.SET_UNITS);
        event.setIntValue(100);
        event.setDescription("Layouting...");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);

        while (!fdgl.shouldStop()) {
            if (this.stop) {
                throw new AbortException();
            }
            fdgl.CalculateForceField();
            fdgl.MoveSystem();
            fdgl.UpdateSource();    //TODO: may be change interface for QuadTree to fdgl??

            event.setIntValue((int)fdgl.getPercentage());
            event.setDescription("Layouting...");
            fireValueChanged(event);
        }
        float max = normalize(this.coords);
        float sigma = 0.02f; //FDGLAlgoT.c_RepulsiveRadius/max;

        AlgorithmData result = new AlgorithmData();
        result.addCluster("links", cluster);
        result.addMatrix("locations", new FloatMatrix(this.coords));
        result.addParam("sigma", String.valueOf(sigma));
        return result;
    }
    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
        this.stop = true;
    }

    private float[][] doCircularLayout(int[][] clusters) {
        float[][] coords = new float[clusters.length][2];
        int[][] subnets = formRelevanceNetworks(clusters);

        int x_size = (int)Math.ceil(Math.sqrt(subnets.length));
        int y_size = (int)Math.ceil((float)subnets.length/(float)x_size);
        int subnet;
        for (int y=0; y<y_size; y++) {
            for (int x=0; x<x_size; x++) {
                subnet = y*x_size+x;
                if (subnet >= subnets.length) {
                    break;
                }
                arrangeGraph(subnets[subnet], coords, x, y); // do layout algorithm
            }
        }
        return coords;
    }

    private void arrangeGraph(int[] subnet, float[][] coords, int x, int y) {
        for (int i=0; i<subnet.length; i++) {
            coords[subnet[i]][0]=(float)(1000*Math.cos(2*Math.PI*i/subnet.length)+1500*(x+1)/*-1.5f*/);
            coords[subnet[i]][1]=(float)(1000*Math.sin(2*Math.PI*i/subnet.length)+1500*(y+1)/*-1.5f*/);
        }
    }

    private float normalize(float[][] coords) {
        float max = 0;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        for (int i=0; i<coords.length; i++) {
            minX = Math.min(minX, coords[i][0]);
            minY = Math.min(minY, coords[i][1]);
        }
        for (int i=0; i<coords.length; i++) {
            coords[i][0] = coords[i][0] - minX;
            coords[i][1] = coords[i][1] - minY;
        }
        for (int i=0; i<coords.length; i++)
            max = Math.max(max, Math.max(coords[i][0], coords[i][1]));

        for (int i=0; i<coords.length; i++) {
            coords[i][0] = coords[i][0]/max;
            coords[i][1] = coords[i][1]/max;
        }
        return max;
    }

    /**
     * Normalize nodes coordinaties to be from 0 to 1.
     */
    private void normalize(float[][] coords, int[][] clusters) {
        float max = 0;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        for (int i=0; i<clusters.length; i++) {
            if (clusters[i].length > 1) {
                for (int j=0; j<clusters[i].length; j++) {
                    minX = Math.min(minX, coords[clusters[i][j]][0]);
                    minY = Math.min(minY, coords[clusters[i][j]][1]);
                }
            }
        }
        for (int i=0; i<coords.length; i++) {
            coords[i][0] = coords[i][0] - minX;
            coords[i][1] = coords[i][1] - minY;
        }
        for (int i=0; i<clusters.length; i++) {
            if (clusters[i].length > 1) {
                for (int j=0; j<clusters[i].length; j++) {
                    max = Math.max(max, Math.max(coords[clusters[i][j]][0], coords[clusters[i][j]][1]));
                }
            }
        }
        for (int i=0; i<coords.length; i++) {
            coords[i][0] = coords[i][0]/max;
            coords[i][1] = coords[i][1]/max;
        }
    }

    /**
     * Returns nodes for every subnet in relnet.
     */
    public int[][] formRelevanceNetworks(int[][] clusters) {
        int size = clusters.length;
        boolean[] visited = new boolean[size];
        Arrays.fill(visited, false);
        ArrayList subnets = new ArrayList();
        for (int node=0; node<size; node++) {
            if (visited[node])
                continue;
            subnets.add(fillSubnet(new IntArray(), node, visited, clusters));
        }
        // copy result into a new structure
        int subnets_size = subnets.size();
        int number_of_subnets = 0;
        for (int i=0; i<subnets_size; i++)
            if (((IntArray)subnets.get(i)).getSize() > 1)
                number_of_subnets++;

        int[][] result = new int[number_of_subnets][];
        int subnet_pos = 0;
        IntArray subnet;
        for (int i=0; i<subnets_size; i++) {
            subnet = (IntArray)subnets.get(i);
            if (subnet.getSize() > 1) {
                result[subnet_pos] = subnet.toArray();
                subnet_pos++;
            }
        }
        return result;
    }

    private IntArray fillSubnet(IntArray subnet, int root, boolean[] visited, int[][] clusters) {
        subnet.add(root);
        visited[root] = true;
        int[] cluster = clusters[root];
        int node;
        for (int i=1; i<cluster.length; i++) {
            node = cluster[i];
            if (visited[node])
                continue;
            fillSubnet(subnet, node, visited, clusters);
        }
        return subnet;
    }

    public int[] GetAllObjectsIds() {       // returns the array of Object Identificators(ID)
        int number_of_genes = this.experiment.getRowDimension();
        int[] ids = new int[number_of_genes];
        for (int i=0; i<ids.length; i++)
            ids[i] = i;
        return ids;
    }

    public void GetObjectGeom(int iObjID, Vector2f pt) {  // returns the metrix(x and y coords) for every object with iObjID identity
        pt.set(this.coords[iObjID][0], this.coords[iObjID][1]);
    }

    public int GetAdjCountFor(int iObjId) {
        return links[iObjId].getSize()-1;
    }

    public void GetAdjInfoFor(int iObjId/*in*/, IntArray rArrAdjIds/*out*/, FloatArray rArrAdjVals/*out*/) {
        int iSize=links[iObjId].getSize();

        rArrAdjIds.clear();
        rArrAdjVals.clear();
        for (int i=1; i<iSize; i++) {   // 'int i=1;' to ignore first element
            rArrAdjIds.add(links[iObjId].get(i));
            rArrAdjVals.add(distances[iObjId].get(i));
        }
    }

    // Uses to return data back to the 'storage'
    public void SetObjectGeom(Vector2f[] rRet) {
        for (int i=0; i<rRet.length; i++) {
            this.coords[i][0] = rRet[i].x;
            this.coords[i][1] = rRet[i].y;
        }
    }
}
