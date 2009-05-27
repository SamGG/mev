package org.tigr.microarray.mev.cluster;

import java.util.Hashtable;

/**
 * Wraps an int[][]. Each ClusterWrapper should contain a unique int[][]. Call
 * wrapClusters(int[[] clusters) to make a new one. 
 * 
 * @author Eleanor
 *
 */
public class ClusterWrapper {
	/*
	 * Stores a mapping of each wrapped int[][] to the wrapper that contains it.
	 * Used to make sure that only one ClusterWrapper is used to wrap a 
	 * given int[][].
	 */
	private static Hashtable<int[][], ClusterWrapper> hash = new Hashtable<int[][], ClusterWrapper>();

	private int[][] intMatrix;

	public static ClusterWrapper wrapClusters(int[][] clusters) {
		if(!hash.contains(clusters))
			hash.put(clusters, new ClusterWrapper(clusters));
		return hash.get(clusters);
	}
	
	private ClusterWrapper(int[][] clusters) {
		this.intMatrix = clusters;
	}
	
	public int[][] getClusters() {
		return intMatrix;
	}
}
