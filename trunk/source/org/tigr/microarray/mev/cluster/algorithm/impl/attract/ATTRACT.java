package org.tigr.microarray.mev.cluster.algorithm.impl.attract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;


import javax.swing.JOptionPane;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.Rengine;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GSEAUtils;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.ReadGeneSet;

import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;

public class ATTRACT extends AbstractAlgorithm{
	private FloatMatrix geneDataMatrix;
	private FloatMatrix incidenceMatrix;
	private String[] geneNames;
	private String[] geneSetNames;
	private String[] sampleNames;
	private String[]removedGenes;
	private String factorName;
	private int[][]grpAssignments;
	private LinkedHashMap<String, Float>geneSetSize;
	private LinkedHashMap<String, Integer>overEnriched;
	private boolean stop=false;
	private Geneset[]geneSets;
	private int topPathways;
	private Vector<String>excludedGeneSets;
	private String[]excludedGenes;
	
	
	
	
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	
		geneDataMatrix=data.getGeneMatrix("gene-data-matrix");
		geneNames=new String[data.getVector("Unique-Genes-in-Expressionset").size()];
		(data.getVector("Unique-Genes-in-Expressionset")).toArray(geneNames);

		
		geneSetNames=new String[data.getVector("gene-set-names").size()];
		(data.getVector("gene-set-names")).toArray(geneSetNames);
		
		
		sampleNames = data.getStringArray("sampleLabels");
		factorName=data.getStringArray("factor-names")[0];
		grpAssignments=data.getIntMatrix("factor-assignments");

		incidenceMatrix=data.getGeneMatrix("association-matrix");
		overEnriched=data.getMappings("over-enriched");
		geneSetSize=data.getMappings("gene-set-size");
		geneSets=((AttractAlgorithmParameters)data.getAlgorithmParameters("attract")).getGenesets();
	    topPathways=Integer.valueOf(data.getParams().getString("pathway-cutoff")).intValue();
	    excludedGeneSets=data.getVector("excluded-gene-sets");
	    
		runRAlg();
		if (stop) {
			throw new AbortException();
		}
		((AttractAlgorithmParameters)data.getAlgorithmParameters("attract")).setGenesets(geneSets);
		AlgorithmData result=new AlgorithmData();
	
		//Add excluded genesets to Algorithm Data, to access from viewers
		result.addVector("excluded-gene-sets", excludedGeneSets);
		System.out.println("length of excluded genesets:"+excludedGeneSets.size());
		//Add excluded genes to Algorithm Data  
		result.addStringArray("excluded-genes", excludedGenes);
		return result;
	}
	
	
	
	public void abort() {
		// TODO Auto-generated method stub
		
	}
	
	
	public void runRAlg()throws AbortException {
		
		
		Rengine re;
		REXP x;
		try {
			re = RHook.startRSession();
			if(re == null) {
				JOptionPane.showMessageDialog(null, "Error creating R Engine",  "REngine", JOptionPane.ERROR_MESSAGE);
				//logger.writeln("Could not get REngine");
				throw new AbortException();
				//return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			//logger.writeln("Could not get REngine");
			throw new AbortException();
			//return;
		}
		
		
		
		
		try {
			RHook.testPackage("attract");
		
			RHook.log("Starting R Algorithim");
			
			String rCmd = "library(attract)";
			RHook.evalR(rCmd);
			
			rCmd="library(Biobase)";
			RHook.evalR(rCmd);
			
			rCmd="library(limma)";
			RHook.evalR(rCmd);
			
			rCmd="library(DBI)";
			RHook.evalR(rCmd);
			
			rCmd="library(RSQLite)";
			RHook.evalR(rCmd);
			
			rCmd="library(AnnotationDbi)";
			RHook.evalR(rCmd);
			
			rCmd="library(KEGG.db)";
			RHook.evalR(rCmd);
					
			rCmd="library(XML)";
			RHook.evalR(rCmd);
			
			rCmd="library(GSEABase)";
			RHook.evalR(rCmd);
			
			rCmd="library(genefilter)";
			RHook.evalR(rCmd);
			
			rCmd="library(xtable)";
			RHook.evalR(rCmd);
			
			rCmd="library(Category)";
			RHook.evalR(rCmd);
			
			rCmd="library(GO.db)";
			RHook.evalR(rCmd);
			
			rCmd="library(RBGL)";
			RHook.evalR(rCmd);
			
			rCmd="library(annotate)";
			RHook.evalR(rCmd);
						
			rCmd="library(GOstats)";
			RHook.evalR(rCmd);
						
			rCmd="library(graph)";
			RHook.evalR(rCmd);
			
			rCmd="library(cluster)";
			RHook.evalR(rCmd);
			
			rCmd="library(GSEAlm)";
			RHook.evalR(rCmd);
			
			
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"dataMatrixfile.txt";
			//if(fileLoc.contains("\\"));
			fileLoc = fileLoc.replace("\\", "/");
			String filePath = writeGeneDataMatrixToFile(fileLoc, geneDataMatrix, geneNames, sampleNames);
		
			//Create data matrix in R from a file
			System.out.println("Reading in data matrix");
			RHook.log("Reading in data matrix");
			rCmd="data<-read.delim('"+filePath+"', sep='\\t', header=TRUE)";
		//	System.out.println("r Command is:"+rCmd);
			RHook.evalR(rCmd);
			
			fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"sampleGroupingsR.txt";
			fileLoc = fileLoc.replace("\\", "/");
			filePath=writeAssignmentsAsFile(fileLoc);
			filePath=fileLoc;
			System.out.println("Reading in sample grouping");
			RHook.log("Reading in sample grouping");
			rCmd="sample.info<-read.delim('"+filePath+"', sep='\\t', header=TRUE)";
		//	System.out.println("r Command is:"+rCmd);
			RHook.evalR(rCmd);
			
			rCmd="exprs.dat<-data[,-1]";
			RHook.evalR(rCmd);
			
			rCmd="rownames(exprs.dat)<-as.character(data[,1])";
			RHook.evalR(rCmd);
			
			rCmd="exprs.dat<-as.matrix(exprs.dat)";
			RHook.evalR(rCmd);
			
			System.out.println("Creating expression set");
			RHook.log("Creating Expression Set");
			rCmd="eset<-new('ExpressionSet')";
			RHook.evalR(rCmd);
	
			rCmd="eset@assayData<-new.env()";
			RHook.evalR(rCmd);
			
			
			rCmd="assign('exprs', exprs.dat, eset@assayData)";
			RHook.evalR(rCmd);
			
			
			rCmd="p.eset<-new('AnnotatedDataFrame', data=sample.info)";
			RHook.evalR(rCmd);
			
			rCmd="eset@phenoData<-p.eset";
			RHook.evalR(rCmd);
			
			//Faster to create an RDataMatrix directly from "incidence-matrix" than writing out a file and reading it	
			//System.out.println("Creating incidence matrix in R");
			RHook.log("Creating incidence matrix in R");
			
			fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"incidenceMatrixFile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			//System.out.println("Starting to write file:"+System.currentTimeMillis());
			filePath=writeGeneDataMatrixToFile(fileLoc, incidenceMatrix,geneSetNames,  geneNames);
			//filePath=writeMatrix(fileLoc, incidenceMatrix,geneSetNames,  geneNames);
			//System.out.println("Ending write file:"+System.currentTimeMillis());
			//System.out.println("filepath of incidence matrix:"+fileLoc);
			
			rCmd="incidencematrix<-read.delim('"+fileLoc+"', sep='\\t', header=TRUE)";
			RHook.evalR(rCmd);
		
			rCmd="incidence<-incidencematrix[,-1]";
			RHook.evalR(rCmd);
			
			rCmd="rownames(incidence)<-as.character(incidencematrix[,1])";
			RHook.evalR(rCmd);
			
			rCmd="incidence<-as.matrix(incidence)";
			RHook.evalR(rCmd);
			
					
			fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"significantGeneSetFile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			writeSignificantGenesetsToFile(fileLoc,geneSetSize, overEnriched);
			
			//System.out.println("Reading in significant gene sets");
			RHook.log("Reading in Significant Genesets");
			
			
			rCmd="rankedPathways<-read.delim('"+fileLoc+"', sep='\\t', header=TRUE)";
			RHook.evalR(rCmd);
			
		//	System.out.println("Creating attractor moduleset");
			RHook.log("Creating AttractorModuleSet");
			
						
			rCmd="eModuleSet<-new('AttractorModuleSet')";
			RHook.evalR(rCmd);
			
			rCmd="eModuleSet@eSet<-eset";
			RHook.evalR(rCmd);
			
			rCmd="eModuleSet@incidenceMatrix<-incidence";
			RHook.evalR(rCmd);
			
			rCmd="eModuleSet@rankedPathways<-rankedPathways";
			RHook.evalR(rCmd);
			
			rCmd="cellType<-as.character('"+factorName+"')";
			RHook.evalR(rCmd);
			
			rCmd="eModuleSet@cellTypeTag<-cellType";
			RHook.evalR(rCmd);
			
			//Removing these flat genes 
			//System.out.println("Removing flat genes");
			rCmd="removeTheseGenes<-remove.flat.genes(eset, cellType, contrasts=NULL, limma.cutoff=0.05)";
			x=RHook.evalR(rCmd);
				
				
			excludedGenes=x.asStringArray();
		
			//Keep the remaining genes
			rCmd="keepTheseGenes<-setdiff(featureNames(eset), removeTheseGenes)";
			x=RHook.evalR(rCmd);
			
		
			
			Vector<String>genesToKeep=new Vector(Arrays.asList(x.asStringArray()));
			
		//	System.out.println("genes to keep length:"+genesToKeep.size());
			
			//Remove the flat genes from Geneset object as well
			ReadGeneSet rgset=new ReadGeneSet(new String(""),new String(""));
			rgset.setMinNumOfGenes(5);
			geneSets=rgset.removeGenesNotinExpressionData(geneSets,genesToKeep );
			
			
			//Remove gene sets which now no longer have atleast 5 genes in them. The minimum cluster size 
			//specified for findSynExprs function in R is 5. 
			geneSets=rgset.removeGenesetsWithoutMinimumGenes(rgset.getExcludedGeneSets(), geneSets);
			
			//Append the newly excluded gene sets to the existing list
			excludedGeneSets.addAll(rgset.getExcludedGeneSets());
			
			System.out.println("geneSets length " + geneSets.length);
			System.out.println("geneSetNames.length && excludedGeneSets.size() " + geneSetNames.length + ", " + excludedGeneSets.size());
			if (geneSets == null || geneSets.length == 0){
				//stop = true;
				RHook.endRSession();
				return;
			}
			
			//Order the gene sets by size
			geneSets=new GSEAUtils().getGeneSetSortedBySize(geneSets);
			
			//Loop through gene sets (number specified by user in GUI, default is 5) of size X and with significant p value
			//Each gene set may have different number of syn expression groups.
			
			RHook.log("Finding synexpression groups");
			
			
			for(int geneSetIndex=0; geneSetIndex<topPathways; geneSetIndex++) {
			
			String geneSetName=geneSets[geneSetIndex].getGeneSetName();	
		
			rCmd="mapkSyn<-find.synexprs('"+geneSetName+"', eModuleSet, removeTheseGenes)";
			RHook.evalR(rCmd);
			
			//Find genes in the expression data that are correlated to the synexpression groups
			rCmd="mapkCor<-find.corr.partners(mapkSyn, eset, removeTheseGenes)";
			RHook.evalR(rCmd);
			
			
			//Number of synexpression groups found
			rCmd="length(mapkSyn@groups)";
			x=RHook.evalR(rCmd);
			int numGroups=x.asInt();
			//Synexpression groups found for a geneset and the corresponding correlated genes
			for(int groups=0; groups<numGroups; groups++) {
				rCmd="asMatrix<-t(as.matrix(unlist(mapkSyn@groups[["+(groups+1)+"]]), nrow=length((mapkSyn@groups[["+(groups+1)+"]])), ncol=length(unlist(mapkSyn@groups[["+(groups+1)+"]])), byrow=true))";
				RHook.evalR(rCmd);
				rCmd="asVector<-as.vector(asMatrix)";
				x=RHook.evalR(rCmd);
				geneSets[geneSetIndex].setSynExpressionGroup("Group"+(groups+1),x.asStringArray());
				
				//rCmd="asMatrix<-t(as.matrix(unlist(mapkCor@groups[["+(groups+1)+"]]), nrow=length((mapkCor@groups[["+(groups+1)+"]])), ncol=length(unlist(mapkCor@groups[["+(groups+1)+"]])), byrow=true))";
				rCmd="asMatrix<-t(as.matrix(unlist(mapkCor[["+(groups+1)+"]]), nrow=length((mapkCor[["+(groups+1)+"]])), ncol=length(unlist(mapkCor[["+(groups+1)+"]])), byrow=true))";
				//x=RHook.evalR(rCmd);
				RHook.evalR(rCmd);
				rCmd="asVector<-as.vector(asMatrix)";
				x=RHook.evalR(rCmd);
				geneSets[geneSetIndex].setSimilarGenes("Group"+(groups+1),x.asStringArray());
				
				
			}
			
			rCmd="mapkSyn@profiles";
			x=RHook.evalR(rCmd);
		
			double[][]doubleArray=x.asMatrix();
			float[][]floatArray=convertDoubleToFloat(doubleArray);
			
			//Set Average gene expression profile of genes found in the synexpression group
			geneSets[geneSetIndex].setSynExpressionProfiles(new FloatMatrix(floatArray));
			
			//rCmd="mapkCor@profiles";
			rCmd="mapkCor";
			x=RHook.evalR(rCmd);
		
			doubleArray=x.asMatrix();
			floatArray=convertDoubleToFloat(doubleArray);
			
			//Set gene expression profile of genes similar to ones in the synexpression group
			geneSets[geneSetIndex].setSimilarGeneExpressionProfile(new FloatMatrix(floatArray));
			
		}
						
		//	System.out.println("end r session");
			RHook.endRSession();
			//Remove all temporary files
			removeAllTmps();
			
			
		}catch(Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}		
	}
	
	/**
	 * Returns a float array
	 * @param doubleArray
	 * @return
	 */
	private float[][]convertDoubleToFloat(double[][]doubleArray){
		System.out.println("double Array Length: " + doubleArray.length);
		float[][]floatArray=new float[doubleArray.length][];
		//Convert double array to float array			
		for(int index=0; index<doubleArray.length; index++) {
			floatArray[index]=new float[doubleArray[index].length];
			for(int index2=0; index2<doubleArray[index].length; index2++) {
				floatArray[index][index2]=(float)doubleArray[index][index2];
			}
				
		}
		return floatArray;
	}
	
	
	
	
	 /**
	 * Writes the factor assignments to file. Follows the following format
	 * SampleName \t FactorName
	 * 
	 */
	private String writeAssignmentsAsFile(String fileLoc) {
		
		File file = new File(fileLoc);			
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(file));
						
				pw.println("Sample Name\t"+factorName);
				
				for(int sample = 0; sample  < sampleNames.length; sample++) {
					pw.print(sampleNames[sample]+"\t");
					
					for(int grpIndex=0; grpIndex<grpAssignments.length; grpIndex++) {
						
						if(grpAssignments[grpIndex][sample]!=0)
							pw.write("Group"+grpAssignments[grpIndex][sample]);
						else
							pw.print("Exclude");
						
						if(grpIndex==grpAssignments.length-1)
							pw.println();
						else
							pw.print('\t');
					}
														
					
				}
				pw.flush();
				pw.close();			
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
	return fileLoc;
	}

	
	
	/**
	 * writes significant gene sets to a file
	 * @param fileLoc
	 * @param geneSets
	 * @returns location of the file
	 */
	
	
	public String writeSignificantGenesetsToFile(String fileLoc, LinkedHashMap<String, Float>geneSetSize, LinkedHashMap overenriched) {
		System.out.println("write significant genesets to file");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));
		//	String[][]pVals =new String[overenriched.size()][4];
			String[][]pVals =new String[overenriched.size()][3];
			//Generate a 2 d string array from the over enriched linked hashmaps
			Object[]gene_sets=overenriched.keySet().toArray();
		   
			for(int i=0; i<overenriched.size(); i++){
				pVals[i][0]=(String)gene_sets[i];
				pVals[i][1]=(String)gene_sets[i];
				pVals[i][2]=((Float)overenriched.get(gene_sets[i])).toString();
				//pVals[i][3]=Float.toString();
		   	}
		   	int row=pVals.length;
		   	int col=3;
		   	String srtVector = "";
		 //  	srtVector="GeneSetName\tDescription\tpValues\tSize\n";
			srtVector="GeneSetName\tDescription\tpValues\n";
			out.write(srtVector);
			srtVector="";
			for(int iRow = 0; iRow < row; iRow++) {
				
			for(int jCol = 0; jCol < col; jCol++) {
					if(jCol == col-1)
						srtVector += pVals[iRow][jCol] + "\n";
					else 
						srtVector += pVals[iRow][jCol] + "\t";
				}
				out.write(srtVector);
				srtVector = "";
			}
			out.close();
			
		
		} catch(IOException e) {
			return null;
		}
		
		return fileLoc;
	}
	
	
	public String writeMatrix(String fileLoc, FloatMatrix fm, String[] rowNames, String[]sampleNames) {
		try {
		PrintWriter writer=new PrintWriter(new FileWriter(fileLoc));
		
		
		fm.print(writer, 2, 0);
		
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return fileLoc;
	}
	
	
	
	/**
	 * writes the content of gene data matrix in to a file
	 * @param fileLoc
	 * @param fm FloatMatrix containing expression values
	 * @param rowNames Names of genes
	 * @returns location of the file
	 */
	
	
	public String writeGeneDataMatrixToFile(String fileLoc, FloatMatrix fm, String[] rowNames, String[]sampleNames) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileLoc)));
			
			int row = fm.getRowDimension();
			int col = fm.getColumnDimension();
			
			
			String srtVector = "";
			srtVector="GeneNames\t";
			out.write(srtVector);
			
			for(int cols=0; cols<col; cols++) {
				if(cols == col-1) {
					srtVector = sampleNames[cols] + "\n";
					out.write(srtVector);
				}else {	
					srtVector = sampleNames[cols] + "\t";
					out.write(srtVector);
				}
				
			}
			
			srtVector="";

			for(int iRow = 0; iRow < row; iRow++) {
				srtVector = rowNames[iRow] + "\t";
				out.write(srtVector);
				for(int jCol = 0; jCol < col; jCol++) {
					
					
					if(jCol == col-1) {
						srtVector = fm.get(iRow, jCol) + "\n";
						out.write(srtVector);
					}
					else {
						srtVector = fm.get(iRow, jCol) + "\t";
						out.write(srtVector);
					}
				}
				
				
			}
			out.flush();
			out.close();
		} catch(IOException e) {
			return null;
		}
		return fileLoc;
	}
	
		
	private void removeAllTmps() {
		String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator");
		String fPath=fileLoc+"dataMatrixfile.txt";
		fPath = fPath.replace("\\", "/");
		new File(fPath).delete();
			
		
		fPath=fileLoc+"sampleGroupingsR.txt";
		fPath = fPath.replace("\\", "/");
		new File(fPath).delete();
		
		fPath=fileLoc+"incidenceMatrixFile.txt";
		fPath = fPath.replace("\\", "/");
		new File(fPath).delete();
		
		fPath=fileLoc+"significantGeneSetFile.txt";
		fPath = fPath.replace("\\", "/");
		new File(fPath).delete();
		
	}
	
	public Geneset[]getGeneSets(){
		return geneSets;
	}
	
	public void setGenesets(Geneset[]gset) {
		geneSets=gset;
	}
	

}
