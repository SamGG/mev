package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class GSEAUtils {
	
	

	
	
	private Vector<String>geneSetNames;
	private ArrayList<String>sorted_gene_names=new ArrayList<String>();
	private HashMap<String, Float> sortedGeneStatistics=new HashMap<String, Float>();
	private LinkedHashMap<String, Float>geneSetWithSize;
	
	public  Vector<String>getGeneSetNames(Geneset[]gset){
		geneSetNames=new Vector<String>();
		
		for(int index=0; index<gset.length; index++){
			if(geneSetNames!=null){
			if(!geneSetNames.contains(gset[index].getGeneSetName()))
				geneSetNames.add(gset[index].getGeneSetName());
				
			}
		}
		
		
		return geneSetNames;
	}
	
	/**
	 * Returns genesets sorted by size
	 * @param gset
	 * @returns a LinkedHashMap containing s sorted by sizegene set
	 */
	
	
	public Geneset[] getGeneSetSortedBySize(Geneset[] gset) {
		Geneset[] tempSet = new Geneset[gset.length];
		int newGenesetIndex=0;
		geneSetWithSize = new LinkedHashMap<String, Float>();
		
		for (int index = 0; index < gset.length; index++) {
			geneSetWithSize.put(gset[index].getGeneSetName(), new Float(
					(gset[index].getGenesetElements().size())));

		}
		geneSetWithSize = (sortHashMapByValuesDescending(geneSetWithSize));

		Iterator<String> it = geneSetWithSize.keySet().iterator();

		while (it.hasNext()) {
			String name = it.next();
			int genesetIndex = -1;
			// Iterate over gene sets till you find the desired one
			for (int index = 0; index < gset.length; index++) {
				if (gset[index].getGeneSetName().equalsIgnoreCase(name)) {
					genesetIndex = index;
					break;
				}
			}
			
			tempSet[newGenesetIndex]=gset[genesetIndex];
			newGenesetIndex=newGenesetIndex+1;
		}
		return tempSet;
	}
	

	
	
	public Geneset[]populateTestStatistic(IGeneData[]gData, Geneset[]geneset, FloatMatrix coef){
		Geneset[]tempSet=geneset;
		
		//Extract the portion that contains the main factor coefficients. The zeroth coefficient is the intercept.
		//The second coefficient will be that of the main factor.
		FloatMatrix coef_intermediate=coef.getMatrix(1,1,0,coef.getColumnDimension()-1);
		
		//Loop through each gene set
		for(int setIndex=0; setIndex<tempSet.length; setIndex++){
			
			//Loop through elements of a  gene set
			for(int elementIndex=0; elementIndex<tempSet[setIndex].getGenesetElements().size(); elementIndex++){
				//Pull out the gene at position elementIndex
				String Gene=tempSet[setIndex].getGeneSetElement(elementIndex).getGene();
				//System.out.println("Gene:"+Gene);
				//Get the index of geneDataElement corresponding to the Gene extracted above.
				
				int index=((GeneData)gData[0]).getPosition(Gene);
				
				//Pull out the test statistic corresponding to this index from coef_intermediate and assign to geneSetElement
				float tstat=coef_intermediate.get(0, index);
			//	System.out.println("test stat:"+tstat);
				tempSet[setIndex].getGeneSetElement(elementIndex).setTestStat(tstat);
				sortedGeneStatistics.put(Gene, tstat);
				
				
			}
			
			
		}
		sortedGeneStatistics=sortHashMapByValuesDescending(sortedGeneStatistics);
		setSortedGeneStatistics(sortedGeneStatistics);
		return tempSet;
		
		
	}
	

	
	
	public HashMap<String, LinkedHashMap<String, Float>>getDescendingSortedTestStats(Geneset[]gset){
	HashMap<String, LinkedHashMap<String, Float>> sorted=new HashMap<String, LinkedHashMap<String, Float>>();
		
		//Loop through each gene set
		for(int setIndex=0; setIndex<gset.length; setIndex++){
			HashMap<String, Float> temp=new HashMap<String, Float>();
			//Loop through elements of a  gene set
			for(int elementIndex=0; elementIndex<gset[setIndex].getGenesetElements().size(); elementIndex++){
				//Pull out the gene at position elementIndex
				String Gene=gset[setIndex].getGeneSetElement(elementIndex).getGene();
				float tStat=gset[setIndex].getGeneSetElement(elementIndex).getTestStat();
				temp.put(Gene, new Float(tStat));
				
			}
			
			sorted.put(gset[setIndex].getGeneSetName(), sortHashMapByValuesDescending(temp));
		
		}
		
		
		return sorted;
		
		
		
	}
	
	
	
	
	
	public HashMap<String, LinkedHashMap<String, Float>> getSortedTestStats(Geneset[]gset){
		HashMap<String, LinkedHashMap<String, Float>> sorted=new HashMap<String, LinkedHashMap<String, Float>>();
		int index=0;
	
		//Loop through each gene set
		for(int setIndex=0; setIndex<gset.length; setIndex++){
		
			HashMap<String, Float> temp=new HashMap<String, Float>();
			//Loop through elements of a  gene set
			for(int elementIndex=0; elementIndex<gset[setIndex].getGenesetElements().size(); elementIndex++){
				//Pull out the gene at position elementIndex
				String Gene=gset[setIndex].getGeneSetElement(elementIndex).getGene();
				float tStat=gset[setIndex].getGeneSetElement(elementIndex).getTestStat();
				temp.put(Gene, new Float(tStat));
				
			}
			LinkedHashMap<String, Float>tempMap=sortHashMapByValues(temp);	
			sorted.put(gset[setIndex].getGeneSetName(), tempMap);
			
			
					
		}
	
		return sorted;
	}
	
	
	public LinkedHashMap sortHashMapByValues(HashMap passedMap){
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap<String, Float> sortedMap = 
	        new LinkedHashMap<String, Float>();
	    
	    Iterator valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Float)val);
	               
	                break;
	            }

	        }

	    }
	   
	    return sortedMap;
	}

	
	
	
	public LinkedHashMap sortHashMapByValuesDescending(HashMap passedMap){
		
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues, Collections.reverseOrder());
	    Collections.sort(mapKeys, Collections.reverseOrder());
	        
	    LinkedHashMap<String, Float> sortedMap = 
	        new LinkedHashMap<String, Float>();
	    
	    Iterator valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Float)val);
	                break;
	            }

	        }

	    }
		
		
	    return sortedMap;
	}


	public ArrayList<String> getSorted_gene_names() {
		return sorted_gene_names;
	}


	public void setSorted_gene_names(ArrayList<String> sorted_gene_names) {
		this.sorted_gene_names = sorted_gene_names;
	}


	public HashMap<String, Float> getSortedGeneStatistics() {
		return sortedGeneStatistics;
	}


	public void setSortedGeneStatistics(HashMap<String, Float> sortedGeneStatistics) {
		this.sortedGeneStatistics = sortedGeneStatistics;
		ArrayList<String>tempList=new ArrayList<String>();
		Iterator it=sortedGeneStatistics.keySet().iterator();
		int index=0;
		while(it.hasNext()) {
			String name=(String)it.next();
			tempList.add(index,name );
			
			index=index+1;
		}
		
		setSorted_gene_names(tempList);

	}

	
	  public FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
	       FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
	       for(int i = 0; i < clusters.length; i++){
	           means.A[i] = getMeans(data, clusters[i]);
	       }
	       return means;
	   }
	  
	   
	  /*
	    *  Returns a set of means for an element
	    */
	   public float [] getMeans(FloatMatrix data, int [] indices){
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
	   
	  
	   /** Returns a matrix of standard deviations grouped by cluster and element
	    * @param data Expression data
	    * @param means calculated means
	    * @param clusters cluster indices
	    * @return
	    */
	   public FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
	       int nSamples = data.getColumnDimension();
	       FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
	       for(int i = 0; i < clusters.length; i++){
	           variances.A[i] = getVariances(data, means, clusters[i], i);
	       }
	       return variances;
	   }
	   
	   /** Calculates the standard deviation for a set of genes.  One SD for each experiment point
	    * in the expression vectors.
	    * @param data Expression data
	    * @param means previously calculated means
	    * @param indices gene indices for cluster members
	    * @param clusterIndex the index for the cluster to work upon
	    * @return
	    */
	   public float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
	       int nSamples = data.getColumnDimension();
	       float [] variances = new float[nSamples];
	       float sse = 0;
	       float mean;
	       float value;
	       int n = 0;
	       for(int i = 0; i < nSamples; i++){
	           mean = means.get(clusterIndex, i);
	           n = 0;
	           sse = 0;
	           for(int j = 0; j < indices.length; j++){
	               value = data.get(indices[j], i);
	               if(!Float.isNaN(value)){
	                   sse += (float)Math.pow((value - mean),2);
	                   n++;
	               }
	           }
	           if(n > 1)
	               variances[i] = (float)Math.sqrt(sse/(n-1));
	           else
	               variances[i] = 0.0f;
	       }
	       return variances;
	   }
	   
	
	
	

}
