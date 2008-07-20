/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif;
import java.util.ArrayList;
import java.util.HashSet;

import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;

/**
 * The class <code>SifToXMLBifUtil</code> contains useful methods for converting a graph in modified Cytoscape SIF format
 * with weights into an XML BIF format (standard format to encode Bayesian networks that can be used as input to WEKA).
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class SifToXMLBifUtil {
	/**
	 * The variable <code>debug</code> is a debug flag.
	 */
	public static boolean debug = false;
	/**
	 * The <code>maxEltsInOneSetOfValues</code> method takes in values for the attributes
	 * and returns the max number of elements in one set of values
	 *
	 * @param values an <code>ArrayList</code> of <code>ArrayList</code>s of values
	 * @return an <code>int</code> corresponding to the max number of elements in one set of values
	 */
	public static int maxEltsInOneSetOfValues(ArrayList values){
		int max = 0;
		ArrayList oneSetOfValues = null;
		for(int i = 0; i < values.size(); i++){
			oneSetOfValues = (ArrayList) values.get(i);
			if(oneSetOfValues.size() > max){
				max = oneSetOfValues.size();
			}
		}
		return max;
	}



	/**
	 * The <code>getOneUniformDistribution</code> method takes in number of values for an RV
	 * and returns a uniform distribution of that size
	 *
	 * @param valueSize an <code>int</code> corresponding to the number of values for an RV
	 * @return an <code>ArrayList</code> of <code>Double</code>s corresponding the uniform distribution 
	 * such that each element in the ArrayList of size valueSize is equal and the sum of all the elements is 1.0
	 */
	public static ArrayList getOneUniformDistribution(int valueSize){
		double d = (double)1.0 / (double) valueSize;
		ArrayList oneDistribution = new ArrayList(valueSize);
		for(int k = 0; k < valueSize; k++){
			oneDistribution.add(new Double(d));
		}
		return oneDistribution;
	}

	/**
	 * The <code>getNames</code> method takes in a given graph and returns the set of unique RVs
	 * corresponding to the end-points of the edges in the given interactions
	 *
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects
	 * corresponding to the given graph
	 * @return an <code>ArrayList</code> of <code>String</code>s corresponding to the set of unique RVs 
	 * corresponding to the end-points of the edges in the given interactions
	 */
	public static ArrayList getNames(ArrayList interactions){
		HashSet names = new HashSet();
		SimpleGeneEdge sGE = null;
		for(int i = 0; i < interactions.size(); i++){
			sGE = (SimpleGeneEdge) interactions.get(i);
			names.add(sGE.getFrom());
			names.add(sGE.getTo());
		}
		ArrayList result = new ArrayList();
		result.addAll(names);
		return result;
	}


	/**
	 * The <code>getMaxParents</code> method takes in an ArrayList of interactions containing several 
	 * SimpleGeneEdge objects and an ArrayList of names of RVs (unique end-points to the edges in interactions)
	 *
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @param names an <code>ArrayList</code> of names of RVs (unique end-points to the edges in interactions
	 * @return an <code>int</code> corresponding to the max number of parents found for any RV
	 */
	public static int getMaxParents(ArrayList interactions, ArrayList names){
		SimpleGeneEdge tmp;
		int numParents =0;
		int maxParents = 0;
		for(int j = 0; j < names.size(); j++){
			numParents = 0;
			for(int i = 0; i < interactions.size(); i++){
				tmp = (SimpleGeneEdge) interactions.get(i);
				if(tmp.getTo().equals((String)names.get(j))){
					numParents++;
				}
			}
			if(numParents > maxParents){
				maxParents = numParents;
			}
		}
		return maxParents;
	}


	/**
	 * The <code>getMaxWeight</code> method is given an ArrayList of interactions containing several SimpleGeneEdge objects
	 * and returns a double corresponding to the max weight among all the edges contained in interactions
	 *
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects 
	 * @return a <code>double</code> corresponding to the max weight among all the edges contained in interactions
	 */
	public static double getMaxWeight(ArrayList interactions){
		SimpleGeneEdge sGE = null;
		double max = 0.0;
		for(int i = 0; i < interactions.size(); i++){
			sGE = (SimpleGeneEdge) interactions.get(i);
			if(sGE.getWeight() > max){
				max = sGE.getWeight();
			}
		}
		return max;
	}

	/**
	 * The <code>getCardinalityOfParents</code> method gets the cardinality of the set of states
	 * the parents of an RV can take
	 *
	 * @param parents an <code>ArrayList</code> corresponding to the parents of an RV
	 * @param iAttribute an <code>int</code> correspoinding to the attribute
	 * @param values an <code>ArrayList</code> corresponding the the values the RV can take
	 * @return an <code>int</code> corresponding to the number of states the parents of an RV can take
	 */
	public static int getCardinalityOfParents(ArrayList parents, int iAttribute, ArrayList values){
		int card = 1;
		for(int p = 0; p < parents.size(); p++){
			card *= ((ArrayList)values.get(p)).size();
		}
		return card;
	}


	/**
	 * The <code>makeUniformDistributions</code> method is given an ArrayList containing the names of the RVs
	 * and the max number of parents and returns are 2D array of ArrayList representing the distributions
	 * for each RV given its parents considering 3 states to be equally likely so 0.33 for each probability.
	 *
	 * @param names an <code>ArrayList</code> the names of the RVs
	 * @param values an <code>ArrayList</code> the values the RVs can take
	 * @param maxParents an <code>int</code> the max number of parents among all RVs
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @return an <code>ArrayList[][]</code> which is 2D array of <code>ArrayList</code>s 
	 * representing the CPT for each RV, namely, distributions for each RV given its parents. 
	 * The distributions are computed using uniform distribution based on the number of states.
	 * For example, if variable A has no parents and 3 states, 
	 * its CPT will be p(A=state1) = 0.33 p(A=state2) = 0.33 p(A=state3) = 0.33.
	 */
	public static ArrayList[][] makeUniformDistributions(ArrayList names, ArrayList values, int maxParents, ArrayList interactions){
		int maxElts = maxEltsInOneSetOfValues(values);
		System.gc();
		if(debug){
			System.out.println("maxElts="+maxElts);      
		}	
		ArrayList[][] distributions = new ArrayList[names.size()][(int)java.lang.Math.pow(maxElts,maxParents)];
		ArrayList oneSetOfValues = null;
		for(int i = 0; i < names.size(); i++){
			if(debug){
				System.out.println("for name "+i+"="+names.get(i));
			}
			oneSetOfValues = (ArrayList) values.get(i);
			ArrayList parents = getParents(names,i,interactions);
			for(int j = 0; j < getCardinalityOfParents(parents,i,values); j++){
				distributions[i][j] = getOneUniformDistribution(oneSetOfValues.size());
				if(debug){
					System.out.println("distr["+i+"]["+j+"]="+distributions[i][j]);
				}
			}
		}
		return distributions;
	}

	/**
	 * The <code>computeDistributions</code> method is given an ArrayList of interactions containing
	 * several SimpleGeneEdge objects, an ArrayList of names of the RVs (unique set of end-points of edges
	 * contained in interactions) an ArrayList of values for the RV, and an int corresponding to max number of parents 
	 * and returns a 2D array of ArrayList containing in this application, assuming each RV is a gene that can take
	 * one of 3 possible states, for the 3rd value (corresponding to +1 or over-expressed) the probability is
	 * weight/maxWeight out of all interactions and for the other values (corresponding to -1 or under-expressed
	 * and 0 or normal to be so that the sum of all probabilities for all 3 values adds up to 1.
	 *
	 * @param names an <code>ArrayList</code> the names of the RVs
	 * @param values an <code>ArrayList</code> the values the RVs can take
	 * @param maxParents an <code>int</code> the max number of parents among all RVs
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @return an <code>ArrayList[][]</code> which is 2D array of <code>ArrayList</code>s 
	 * representing the CPT for each RV, namely, distributions for each RV given its parents. 
	 * The distributions are computed from the weights of the edges in the given graph G(V,E)
	 * such that given 2 variables with a directed edge from a to b with weight w(A,B),
	 * the probability of A given B will be computed as: p(A|B) = w(A,B) / max forall e in E of w(e)
	 */
	public static ArrayList[][] computeDistributions(ArrayList names, ArrayList values, int maxParents, ArrayList interactions){
		System.gc();
		int maxElts = maxEltsInOneSetOfValues(values);
		if(debug){
			System.out.println("maxElts="+maxElts);      
		}
		int numNames = names.size();
		int numValues = values.size();
		ArrayList valuesForOneGene = null;
		String name = null;
		ArrayList oneSetOfValues = null;
		// get max weight out of all interactions
		double maxWeight = getMaxWeight(interactions);
		ArrayList[][] distributions = new ArrayList[names.size()][(int)java.lang.Math.pow(maxElts,maxParents)];
		ArrayList parents = null;
		int card = 1;
		int index = 0;
		for(int i = 0; i < numNames; i++){
			index = 0;
			card = 1;
			// for each RV denoted by name in the given ArrayList of names
			name = (String) names.get(i);
			oneSetOfValues = (ArrayList) values.get(i);
			if(debug){
				System.out.println("name="+name+" @i="+i);
			}
			// get its parents
			parents = getParents(names, i, interactions);
			card = getCardinalityOfParents(parents, i, values);
			if(parents.size()==0){
				while(index < card){
					distributions[i][index] = getOneUniformDistribution(oneSetOfValues.size());
					if(debug){
						System.out.println("no par: distr["+i+"]["+index+"]="+distributions[i][index]);
					}
					index++;
				}
			}
			else {
				int j = 0;
				valuesForOneGene = (ArrayList) values.get(j);
				// for each parent of this RV
				// compute the distribution of this RV given its parent
				while(index < card){
					if(index == valuesForOneGene.size()){
						j++;
						valuesForOneGene = (ArrayList) values.get(j);
					}
					if(debug){
						System.out.print("#j="+j+ " for parent=");
						System.out.print(parents.get(j));
					}
					distributions[i][index] = computeOneDistribution(index, card, name, j, names, parents, valuesForOneGene, interactions, maxWeight);
					if(debug){
						System.out.println("distr["+i+"]["+index+"]="+distributions[i][index]);
					}
					index++;
				}
			}	    
		}
		return distributions;
	}

	/**
	 * The <code>getWeightForOneName</code> method returns the weights of an edge from the jth parent to a given RV
	 *
	 * @param name a <code>String</code> of the RV 
	 * @param j an <code>int</code> corresponding to the index of the parent in parents to consider a start node
	 * to get edge weight
	 * @param parents an <code>ArrayList</code> corresponding to the parents of the RV
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @return a <code>double</code> corresponding to the weight of the edge in the given graph from the jth parent 
	 * to a given RV
	 */
	public static double getWeightForOneName(String name, int j, ArrayList parents, ArrayList interactions){
		SimpleGeneEdge sGE = null;
		double weight = 0.0;
		String parent = (String) parents.get(j);
		for(int i = 0; i < interactions.size(); i++){
			sGE = (SimpleGeneEdge) interactions.get(i);
			if((sGE.getTo().equals(name)) && (sGE.getFrom().equals(parent))){
				weight = sGE.getWeight();
			}
		}    
		return weight;
	}


	/**
	 * The <code>computeOneDistribution</code> method computes distribution for one RV from weights
	 * In this application, assuming each RV is a gene that can take one of 3 possible states, for the 3rd value
	 * (corresponding to +1 or over-expressed) the probability is weight/maxWeight out of all interactions
	 * and for the other values (corresponding to -1 or under-expressed and 0 or normal to be 
	 * so that the sum of all probabilities for all 3 values adds up to 1.
	 *
	 * @param index an <code>int</code> corresponding to the index of the RV
	 * @param card an <code>int</code> corresponding to the cardinality of the RV's parents
	 * @param name a <code>String</code> corresponding to the name of the RV
	 * @param j an <code>int</code> corresponding to the jth parent of the RV
	 * @param names an <code>ArrayList</code> corresponding the set of all RVs
	 * @param parents an <code>ArrayList</code> corresponding to the parents of the RV
	 * @param valuesForOneGene an <code>ArrayList</code> corresponding to the values taken by one RV
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects of the given graph
	 * @param maxWeight a <code>double</code> corresponding the maximum weight among all edge weights in the given graph
	 * @return an <code>ArrayList</code> corresponding to the computed distribution for one RV from weights
	 */
	public static ArrayList computeOneDistribution(int index, int card, String name, int j, ArrayList names, ArrayList parents, ArrayList valuesForOneGene, ArrayList interactions, double maxWeight){
		if(debug){
			System.out.println("in computeOneDistr: index="+index+" card="+card+" name="+name+" j="+j);
		}
		if((index+1) % card == 0){
			double weight = 0.0;
			ArrayList oneDistr = new ArrayList();
			double lastValue = 0.0;
			double otherValues = 0.0;
			weight = getWeightForOneName(name, j, parents, interactions);
			lastValue = weight/maxWeight;
			otherValues = (double) (1 - lastValue) / (double) (valuesForOneGene.size()-1);
			for(int i = 0; i < valuesForOneGene.size() -1; i++){
				oneDistr.add(new Double(otherValues));	
			}
			oneDistr.add(new Double(lastValue));
			return oneDistr;
		}
		else {
			return getOneUniformDistribution(valuesForOneGene.size());
		}
	}


	/**
	 * The <code>getParents</code> method takes in the given set of RVs, the given graph, and the index of a given RV
	 *
	 * @param names an <code>ArrayList</code> corresponding to the given set of RVs
	 * @param index an <code>int</code> corresponding to the given index of an RV
	 * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the subgraph 
	 * composed of all incoming edges of the given RV in the given graph, in other words, 
	 * the set of edges of parents of the RV to the RV from the given graph.
	 */
	public static ArrayList getParents(ArrayList names, int index, ArrayList interactions){
		ArrayList parents = new ArrayList();
		for(int i = 0; i < interactions.size(); i++){
			SimpleGeneEdge tmp = (SimpleGeneEdge) interactions.get(i);
			if(tmp.getTo().equals((String)names.get(index))){
				parents.add(tmp.getFrom());
			}
		}
		return parents;
	}
}
