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
				
				
				
			}
			
			
		}
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
					
			sorted.put(gset[setIndex].getGeneSetName(), sortHashMapByValues(temp));
					
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
		
		Iterator it=sortedMap.keySet().iterator();
		
		
	    return sortedMap;
	}

	
	
	
	
	

}
