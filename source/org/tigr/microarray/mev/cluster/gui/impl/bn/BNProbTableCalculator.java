package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.util.ArrayList;
public class BNProbTableCalculator {
	private float[][] probTable;
	private boolean[] ptFound;
	private BifDOMBuilder bdb;
	ArrayList<BifNode> bif= new ArrayList<BifNode>();
	private int numNodes;
	public BNProbTableCalculator(BifDOMBuilder bdb){
		this.bdb =bdb;
		try {
			bif = this.bdb.build("C:/workspace/MeV_trunk_11_11/data/BN_files/FixedNetWithCPT.xml");
		} catch (Exception e){
			e.printStackTrace();
		}
		numNodes=bif.size();
		probTable = new float[3][numNodes];
		ptFound = new boolean[numNodes];
		for (int i=0; i<numNodes; i++){
			ptFound[i]= false;
		}
//		printbdb();
		calculateTables();
		printPTs();
	}
	
	/**
	 * Runs through all nodes, constructing probability tables of expression for each node.
	 */
	private void calculateTables(){
		for (int i=0; i<numNodes; i++){
			if(!havePT(bif.get(i)))
				getPT(bif.get(i));
		}
	}
	
	/**
	 * Recursive algorithm.  Gets a Probability table for the given node.  If any of the parent's of bifNode are not solved
	 * for their respective PTs, then getPT will call itself with the parent as the given node.
	 * @param bifNode
	 */
	private void getPT(BifNode bifNode){
		for (int i=0; i<bdb.getParents(bifNode).size(); i++){
			BifNode parent = bdb.getParents(bifNode).get(i);
			if (!havePT(parent))
				getPT(parent);
		}
		calcPTWithParents(bifNode);
	}
	
	/**
	 * Calculates the probability table for a given node.  
	 * *NOTE* All parents of this node must have been solved for their probability
	 * tables before bifNode's PT can be calculated.
	 * @param bifNode
	 */
	private void calcPTWithParents(BifNode bifNode){
		ArrayList<BifNode> parentNodes = bdb.getParents(bifNode);
		if (parentNodes.size()==0){
			for (int bin=0; bin<3; bin++){
				try{
					probTable[bin][bif.indexOf(bifNode)]=bifNode.getCPT()[bin];
				}catch(NullPointerException npe){
					System.out.println(bifNode.getChild());
					npe.printStackTrace();
				}
			}
			ptFound[bif.indexOf(bifNode)]=true;
			return;
		}
		for (int bin=0; bin<3; bin++){
			float prob = 0;
			int[] indices = new int[parentNodes.size()];
			for (int i=0; i<parentNodes.size(); i++){
				indices[i]=0;
			}
			for (int i=0; i<Math.pow(3, parentNodes.size()); i++){
				
				float p = 1;
				for (int parentCounter=0; parentCounter<parentNodes.size(); parentCounter++){
					p=p*probTable[indices[parentCounter]][bif.indexOf(parentNodes.get(parentCounter))];
				}
				prob = prob + p*bifNode.getCPT()[3*i+bin];
				
				int lastIndex = indices.length-1;
				for(int numP=0; numP<parentNodes.size(); numP++){
					indices[lastIndex]++;
					if(indices[lastIndex]>2){
						indices[lastIndex]=0;
						lastIndex--;
						continue;
					}
					break;
				}
			}
			probTable[bin][bif.indexOf(bifNode)]=prob;
			
		}
		ptFound[bif.indexOf(bifNode)]=true;
	}
	
	/**
	 * 
	 * @param bifNode
	 * @return Returns a boolean for whether or not the PT for the given node has yet been solved.
	 */
	private boolean havePT(BifNode bifNode){
		return ptFound[bif.indexOf(bifNode)];
	}
	
	/**
	 * Prints a table listing Probabilities of each state for each node.
	 */
	private void printPTs(){
		System.out.println();
		System.out.println("  ********Results*******");
		for (int i=0; i<this.numNodes; i++){
			System.out.print("Node: "+ bif.get(i).getChild()+":     \t");
			for (int j=0; j<3; j++){
				System.out.print(probTable[j][i]+"\t");
			}
			System.out.println();
		}
	}
	
	/**
	 * Prints info for each node
	 */
	private void printbdb(){
		int count = 0;
		for (int i=0; i<bif.size(); i++ ){
			System.out.println("\n"+i);
			float[] a =bif.get(i).getCPT();
			String b =bif.get(i).getChild();
			ArrayList<String> c = 	bif.get(i).getParents();
			if (b!=null){
				System.out.println("Child = " +b);
			}
			if (c!=null){
				System.out.print("Parents:\t");
				for (int j=0; j<c.size();j++){
					System.out.print(c.get(j)+"\t");
				}
				System.out.println();
			}
			if (a!=null){
				System.out.print("Node "+count+"\t");
				count++;
				for (int j=0; j<a.length;j++){
					System.out.print(a[j]+"\t");
				}
				System.out.println();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BifDOMBuilder bdb = new BifDOMBuilder();
		BNProbTableCalculator tableCalc = new BNProbTableCalculator(bdb);
//		
//		try {
//			int count = 0;
//			ArrayList<BifNode> bif= new ArrayList<BifNode>();
//			bif = bdb.build("C:/workspace/data/BN_files/resultBif1.xml");
////			float[][][] aa =bif.get(0).getCPT3D();
//			for (int i=0; i<bif.size(); i++ ){
//				System.out.println("\n"+i);
//				float[] a =bif.get(i).getCPT();
//				String b =bif.get(i).getChild();
//				ArrayList<String> c = 	bif.get(i).getParents();
//				if (b!=null){
//					System.out.println("Child = " +b);
//				}
//				if (c!=null){
//					System.out.print("Parents:\t");
//					for (int j=0; j<c.size();j++){
//						System.out.print(c.get(j)+"\t");
//					}
//					System.out.println();
//				}
//				if (a!=null){
//					System.out.print("Node "+count+"\t");
//					count++;
//					for (int j=0; j<a.length;j++){
//						System.out.print(a[j]+"\t");
//					}
//					System.out.println();
//				}
//			}
//			tableCalc.calculateTables();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}

}
