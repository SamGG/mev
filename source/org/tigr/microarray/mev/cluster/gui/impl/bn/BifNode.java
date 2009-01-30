/**
 * @author Raktim Sinha
 * Class to Model a Bif XML node with Parent Child and corresponding CPTs
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.util.ArrayList;

public class BifNode {

	ArrayList<String> parents = new ArrayList<String>();
	String child;
	float CPT[];
	int bins;
	
	public BifNode() {
		// TODO Auto-generated constructor stub
	}

	public BifNode(String child) {
		this.child = child;
	}
	
	public BifNode(String child, ArrayList<String> parents){
		this.child = child;
		if(parents != null && parents.size() > 0)
			this.parents = parents;
	}
	
	/**
	 * First function to be called before CPT values can be assigned.
	 * @param s
	 */
	public void initCPT(int s) {
		CPT = new float[s];
	}
	
	public float[] getCPT(){
		return CPT;
	}
	
	public int numParents() {
		return this.parents.size();
	}
	
	public boolean isOrphan(){
		if (this.parents.size() > 0)
			return false;
		return true;
	}
	
	public void setChild(String child) {
		this.child = child;
	}
	
	public ArrayList<String> getParents(){
		return this.parents;
	}
	
	public String getParentAt(int i){
		if (i < this.parents.size() || i > this.parents.size()-1) return null;
		return this.parents.get(i);
	}
	
	public String getChild(){
		return this.child;
	}
	
	public void addParent(String parent) {
		this.parents.add(parent);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
