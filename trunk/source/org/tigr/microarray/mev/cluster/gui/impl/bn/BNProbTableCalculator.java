package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

public class BNProbTableCalculator {
	private float[][] origProbTable;
	private float[][] difProbTable;
	private float[][] probTable;
	private int[] setNodes;
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
		origProbTable = new float[3][numNodes];
		difProbTable = new float[3][numNodes];
		probTable = new float[3][numNodes];
		ptFound = new boolean[numNodes];
		setNodes = new int[numNodes];
		calculateTables();
		for (int i=0; i<3; i++){
			for (int j=0; j<numNodes; j++){
				origProbTable[i][j] = probTable[i][j];
			}
		}
		getDifTable();
	}
	
	/**
	 * Runs through all nodes, constructing probability tables of expression for each node.
	 */
	private void calculateTables(){
		for (int i=0; i<numNodes; i++){
			ptFound[i]= false;
		}
		for (int i=0; i<numNodes; i++){
			if(!havePT(bif.get(i)))
				getPT(bif.get(i));
		}
		getDifTable();
	}
	
	private void resetNodes(){
		for (int i=0; i<setNodes.length; i++){
			setNodes[i]=0;
		}
	}

	private void setNode(BifNode bifNode, int set){
		setNodes[bif.indexOf(bifNode)]=set;
	}
	
	private void getDifTable(){
		for (int i=0; i<3; i++){
			for (int j=0; j<numNodes; j++){
				difProbTable[i][j] = probTable[i][j]-origProbTable[i][j];
			}
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
		if(setNodes[bif.indexOf(bifNode)]!=0){
			for (int bin=0; bin<3; bin++){
				probTable[bin][bif.indexOf(bifNode)]=0;
			}
			probTable[setNodes[bif.indexOf(bifNode)]-1][bif.indexOf(bifNode)]=1;
			ptFound[bif.indexOf(bifNode)]=true;
			return;
		}
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
	 * Prints a table listing the current probabilities of each state for each node.
	 */
	private void printCurrentPTs(){
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
	 * Prints a table listing the original probabilities of each state for each node.
	 */
	private void printOriginalPTs(){
		System.out.println();
		System.out.println("  ********Original*******");
		for (int i=0; i<this.numNodes; i++){
			System.out.print("Node: "+ bif.get(i).getChild()+":     \t");
			for (int j=0; j<3; j++){
				System.out.print(origProbTable[j][i]+"\t");
			}
			System.out.println();
		}
	}
	/**
	 * Prints the difference table listing the probability changes of each state for each node from the original.
	 */
	private void printDifPTs(){
		System.out.println();
		System.out.println("  ********Difference*******");
		for (int i=0; i<this.numNodes; i++){
			System.out.print("Node: "+ bif.get(i).getChild()+":     \t");
			for (int j=0; j<3; j++){
				System.out.print(difProbTable[j][i]+"\t");
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
	private void showDialog(){

		JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
		for (int i=0; i<this.numNodes; i++){
			jp.add(new JLabel("Node: "+ this.bif.get(i).getChild()+": "), new GridBagConstraints(0,i,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
	        JButton jb = new JButton("Down");
	        jb.setActionCommand(String.valueOf(i));
	        jb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
            		setNode(bif.get(Integer.parseInt(evt.getActionCommand())), 1);
            		calculateTables();
            		printCurrentPTs();
            		printDifPTs();
                }
	        });
			jp.add(jb, new GridBagConstraints(1,i,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
			jb = new JButton("Neutral");
	        jb.setActionCommand(String.valueOf(i));
	        jb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	setNode(bif.get(Integer.parseInt(evt.getActionCommand())), 2);
            		calculateTables();
            		printCurrentPTs();
            		printDifPTs();
                }
	        });
			jp.add(jb, new GridBagConstraints(2,i,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
			jb = new JButton("Up");
	        jb.setActionCommand(String.valueOf(i));
	        jb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	setNode(bif.get(Integer.parseInt(evt.getActionCommand())), 3);
            		calculateTables();
            		printCurrentPTs();
            		printDifPTs();
                }
	        });
			jp.add(jb, new GridBagConstraints(3,i,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
		}
		
		JButton jb = new JButton("Reset");
        jb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
				resetNodes();
        		calculateTables();
        		printCurrentPTs();
        		printDifPTs();
            }
        });
		jp.add(jb, new GridBagConstraints(1,numNodes,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
	
		JDialog jd = new JDialog();
		jd.add(jp);
		jd.pack();
		jd.setSize(400, 800);
		jd.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BifDOMBuilder bdb = new BifDOMBuilder();
		BNProbTableCalculator tableCalc = new BNProbTableCalculator(bdb);
		tableCalc.printbdb();
		tableCalc.calculateTables();
//		tableCalc.setNode(tableCalc.bif.get(0), 2);
//		tableCalc.calculateTables();
		
		tableCalc.showDialog();
		
		
		
		
		System.out.println("Set "+tableCalc.bif.get(3).getChild());
		tableCalc.printCurrentPTs();
		tableCalc.printOriginalPTs();
		tableCalc.printDifPTs();
	}

}
