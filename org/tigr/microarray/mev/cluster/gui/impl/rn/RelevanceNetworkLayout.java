/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelevanceNetworkLayout.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:44 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.tigr.microarray.mev.cluster.gui.impl.util.IntSorter;

/**
 * <strong>Note that this implementation is not synchronized.</strong>
 */
public class RelevanceNetworkLayout {

    public static final int RANDOM_LAYOUT = 0;
    public static final int CIRCULAR_LAYOUT = 1;

    /**
     * Lays out nodes with specified layout type.
     * @return an array of nodes coordinaties.
     */
    public float[][] doLayout(int[][] clusters, float[][] weights, int layout) {
        switch (layout) {
        case RANDOM_LAYOUT:
            return doRandomLayout(clusters, weights);
        case CIRCULAR_LAYOUT:
            return doCircularLayout(clusters, weights);
        }
        return null;
    }

    /**
     * Returns array of random coordinaties.
     */
    private float[][] doRandomLayout(int[][] clusters, float[][] weights) {
        float[][] coords = new float[clusters.length][2];
        Random random = new Random(0);
        for (int i=0; i<coords.length; i++) {
            coords[i][0] = Math.abs(random.nextFloat());
            coords[i][1] = Math.abs(random.nextFloat());
        }
        return coords;
    }

    /**
     * Returns nodes coordinaties as a set of circles.
     */
    private float[][] doCircularLayout(int[][] clusters, float[][] weights) {
        float[][] coords = new float[clusters.length][2];
        int[][] subnets = formRelevanceNetworks(clusters);
        // sort subnets
        int[] indices = new int[subnets.length];
        for (int i = indices.length; --i >= 0;) {
            indices[i] = i;
        }
        IntSorter.sort(indices, new RelNetComparator(subnets));

        int x_size = (int)Math.ceil(Math.sqrt(subnets.length));
        int y_size = (int)Math.ceil((float)subnets.length/(float)x_size);
        int subnet;
        for (int y=0; y<y_size; y++) {
            for (int x=0; x<x_size; x++) {
                subnet = y*x_size+x;
                if (subnet >= subnets.length) {
                    break;
                }
                arrangeGraph(subnets[indices[subnet]], coords, x, y); // do layout algorithm
            }
        }
        normalize(coords, clusters);
        return coords;
    }

    private void arrangeGraph(int[] subnet, float[][] coords, int x, int y) {
        for (int i=0; i<subnet.length; i++) {
            coords[subnet[i]][0]=(float)(Math.cos(2*Math.PI*i/subnet.length)+2.5*(x+1)-1.5f);
            coords[subnet[i]][1]=(float)(Math.sin(2*Math.PI*i/subnet.length)+2.5*(y+1)-1.5f);
        }
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
            if (visited[node]) {
                continue;
            }
            subnets.add(fillSubnet(new ArrayList(), node, visited, clusters));
        }
        // copy result into a new structure
        int subnets_size = subnets.size();
        int number_of_subnets = 0;
        for (int i=0; i<subnets_size; i++) {
            if (((ArrayList)subnets.get(i)).size() > 1) {
                number_of_subnets++;
            }
        }
        int[][] result = new int[number_of_subnets][];
        int subnet_pos = 0;
        ArrayList subnet;
        for (int i=0; i<subnets_size; i++) {
            subnet = (ArrayList)subnets.get(i);
            if (subnet.size() > 1) {
                result[subnet_pos] = new int[subnet.size()];
                for (int j=0; j<result[subnet_pos].length; j++) {
                    result[subnet_pos][j] = ((Integer)subnet.get(j)).intValue();
                }
                subnet_pos++;
            }
        }
        return result;
    }

    private ArrayList fillSubnet(ArrayList subnet, int root, boolean[] visited, int[][] clusters) {
        subnet.add(new Integer(root));
        visited[root] = true;
        int[] cluster = clusters[root];
        int node;
        for (int i=1; i<cluster.length; i++) {
            node = cluster[i];
            if (visited[node]) {
                continue;
            }
            fillSubnet(subnet, node, visited, clusters);
        }
        return subnet;
    }

}


