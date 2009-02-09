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
		printbdb();
		calculateTables();
		printPTs();
	}
	
	private void calculateTables(){
		for (int i=0; i<numNodes; i++){
			if(!havePT(bif.get(i)))
				getPT(bif.get(i));
		}
	}
	
	private void getPT(BifNode bifNode){
		for (int i=0; i<bdb.getParents(bifNode).size(); i++){
			BifNode parent = bdb.getParents(bifNode).get(i);
			if (!havePT(parent))
				getPT(parent);
		}
		calcPTWithParents(bifNode);
	}
	
	private void calcPTWithParents(BifNode bifNode){
		System.out.println("calcPT "+ bifNode.getChild());
		ArrayList<BifNode> parentNodes = bdb.getParents(bifNode);
		if (parentNodes.size()==0){
			for (int bin=0; bin<3; bin++){
				try{
					probTable[bin][bif.indexOf(bifNode)]=bifNode.getCPT()[bin];
				}catch(NullPointerException npe){
					System.out.println(bifNode.getChild());
					npe.printStackTrace();
				}
				System.out.println("asdasd   "+bifNode.getCPT()[bin]);
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
//				if (indices.length==2)
//				System.out.print(indices[0]+"\t"+indices[1]+"\n");
			}
			probTable[bin][bif.indexOf(bifNode)]=prob;
			
		}
		ptFound[bif.indexOf(bifNode)]=true;
	}
	
	private boolean havePT(BifNode bifNode){
		return ptFound[bif.indexOf(bifNode)];
	}
	
	private void printPTs(){
		System.out.println();
		System.out.println("********Results*******");
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
