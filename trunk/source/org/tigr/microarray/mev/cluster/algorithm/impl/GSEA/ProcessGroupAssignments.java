package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFileChooser;

import org.tigr.microarray.mev.file.StringSplitter;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.util.FloatMatrix;


/**
 * 
 * @author Sarita Nair
 * ReadFactorFile class reads a factor file and generates a Matrix of values corresponding to the
 * factor assignements.
 * 
 * Factor file format is as follows:
 * 
 * #Factor <tab>  number of factors
 * #Levels <tab> level1 <tab> level2...and so on
 * <tab> seperated level assignments begins. Each row corresponds to a factor
 * 
 * Factor relevel is not addressed here
 *
 */



public class ProcessGroupAssignments {

	/**
	 * @param args
	 * 
	 */
	
	
	protected FloatMatrix factor_matrix;
	protected Vector excludedColumns=new Vector();
	
	protected int nSamples;
	protected boolean removeNA;

	//GUI based grouping
	private String[]factornames=null;
	private int[][]factorAssignments=null;
	private int[]factorLevels=null;
	
	
	
	
	public ProcessGroupAssignments(String[]factorNames, int[]factorLevels, int[][]factorAssignments, boolean removeNA, int num_samples){
		this.factornames=factorNames;
		this.factorAssignments=factorAssignments;
		this.removeNA=removeNA;
		this.nSamples=num_samples;
	}
	
	
	
	
	
	
	
	public void findUnassignedSamples(String[]factorNames, int[]factorLevels, int[][]factorAssignments){
		this.factorAssignments=factorAssignments;
		for(int num_factors=0; num_factors<factorNames.length; num_factors++){
			//Extract assignments
			int[]tempAssignments=this.factorAssignments[num_factors];
			
			for(int col=0; col<tempAssignments.length; col++){
				//Add to the exluded columns list any samples that are unassigned
				if(tempAssignments[col]==0 && !excludedColumns.contains(col)){
					this.excludedColumns.add(col);
				}
			}
		}
		
		//Print the excluded columns
		/*for(int i=0; i<excludedColumns.size(); i++){
			System.out.println("Excluded column:"+excludedColumns.get(i));
		}*/
		
	}
	
	

	
	/**
	 * 
	 * @returns a Vector containing columns which are unassigned to any of the
	 * factors.
	 */
	
	public Vector getUnassignedColumns(){
		return this.excludedColumns;
	}
	
	
	
	
	public int calculateFactorMatrixColumns(String[]names, int[]levels){
		int cols=0;
		//Columns is equal to the number of factor levels minus one. The reason is the intercept column that is added 
	
		for(int i=0; i<names.length; i++){
			int temp=levels[i]-1;
			cols=cols+temp;
		}
		//Add a column for intercept
		cols=cols+1;
		return cols;
		
	}

	
	
	
	
	/**
	 * 
	 * 
	 * 
	 */
	public FloatMatrix generateFactorMatrix(String[]factorNames,int[]factorLevels,int[][]factorAssignments){

		int cols=calculateFactorMatrixColumns(factorNames, factorLevels);

		int matColStart=1;
		int matColEnd=1;
		int unassigned=((Vector)getUnassignedColumns()).size();
		/*Printingthe factor assignments*
		System.out.println("The factor assignments are:");
		for(int i=0; i<factorAssignments.length; i++){
			for(int j=0; j<factorAssignments[i].length; j++){
				System.out.print(factorAssignments[i][j]);
				System.out.print('\t');
			}
			System.out.println();
		}*/
		
		
		
		

		//In the R function model.matrix, after which this is modeled, if there are samples which are unassigned to any factors OR
		//unassigned to one of the factors; The function removes those samples before computing the FloatMatrix.
		//So, the rows of the resulting matrix will always be (number of samples-unassigned).
		factor_matrix=new FloatMatrix(nSamples-unassigned,cols);
		
		//Setting values of the "Intercept Column". This is the first column of the factor matrix and all rows will always be equal to 1
		for(int i=0; i<factor_matrix.getRowDimension(); i++){
			factor_matrix.set(i, 0, 1);
		}

		for(int i=0; i<factorNames.length; i++){
			int tempcolStart=0;
			int tempRowStart=0;
			//MeV assigns numbers to factor levels. Level==0 is for unassigned samples
			//The first level of any factor is always considered to be the intercept, hence level starts with 2
			int factorlevel=2;
			int [] rowVector=factorAssignments[i];
		//	System.out.println("rowVector size:"+rowVector.length);
			//System.out.println("number of samples:"+factor_matrix.getRowDimension());
			int current_factor_level=factorLevels[i];
			FloatMatrix tempMatrix=new FloatMatrix(factor_matrix.getRowDimension(),current_factor_level-1);

			while(factorlevel<=current_factor_level){
				int samples=0;
				//	System.out.println("factorlevel"+factorlevel);
				while( samples<nSamples-1){
					//	System.out.println("factor-Assignment"+rowVector[samples]);
					//System.out.println("level"+factorlevel);
					if(this.excludedColumns.contains(samples)){
						//System.out.println("excluded column:"+samples);
						if(samples<nSamples-1)
							samples=samples+1;
						
					}
					//System.out.println("rowvector"+samples+":"+rowVector[samples]);
					if(rowVector[samples]==factorlevel){
						tempMatrix.set(tempRowStart, tempcolStart, 1);
						tempRowStart=tempRowStart+1;
					}else{
						tempRowStart=tempRowStart+1;
					}
					if(samples<nSamples-1)
						samples=samples+1;
				}
				//Move to the next column
				if(tempcolStart < current_factor_level){
					tempcolStart=tempcolStart+1;
				}
				//Reset the row index to 0
				tempRowStart=0;
				//Go to the next factor level
				factorlevel=factorlevel+1;
			}//End of factor level for loop
			matColEnd=matColStart+(current_factor_level-1)-1;
			//	System.out.println("FloatMatrix col start:"+matColStart);
			//System.out.println("FloatMatrix col end:"+matColEnd);

			factor_matrix.setMatrix(0, factor_matrix.getRowDimension()-1, matColStart, matColEnd, tempMatrix);
			matColStart=matColEnd+1;
			matColEnd=matColStart;


		}//End of factornames for loop

		/*****************Testing factor matrix******************************
		File factorMatrix;
		try {
			factorMatrix = new File("C:/Users/sarita/Desktop/GSEA-TestData/factorMatrix_JAVA.txt");
			 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(factorMatrix)));
			 int tcount=0;
			 while(tcount<factor_matrix.getRowDimension()){
				 for(int j=0; j<factor_matrix.getColumnDimension(); j++){
				
				 pw.write(Float.toString(factor_matrix.get(tcount, j)));
				 pw.write('\t');
				}
				 pw.write('\n');
				 tcount=tcount+1;
			 }
			 
			 
			 pw.close();
			 
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	
	

		return factor_matrix;
	}


	
	
	
		
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Hashtable fHash=new Hashtable();
		String[]factorNames={"FactorA","FactorB"};
		int[]factorLevels={3,2};
		int[][]factorAssignments={{3,1,2,1,2,3},{1,2,1,2,2,0}};
		//factorAssignments[0]={3,1,2,1,2,3};
		//factorAssignments[1]={1,2,1,2,2,3};
		
				
		ProcessGroupAssignments pg= new  ProcessGroupAssignments(new String[]{"FactorA", "FactorB"}, new int[]{3,3}, factorAssignments, true, 6);
		pg.findUnassignedSamples(factorNames, factorLevels, factorAssignments);
		FloatMatrix factor_matrix=pg.generateFactorMatrix(factorNames, factorLevels, factorAssignments);
		
		for(int i=0;i<factor_matrix.getRowDimension();i++){
			for(int j=0;j<factor_matrix.getColumnDimension();j++){
				System.out.print(factor_matrix.get(i, j));
				System.out.print('\t');
			}
			System.out.println();
		}
		
		
		
		
		
		
		
	/*	JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.DATA_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(null);
		File selectedFile;

		if (retVal == JFileChooser.APPROVE_OPTION) {
			try{
			selectedFile = fileChooser.getSelectedFile();
			ProcessGroupAssignments rf=new ProcessGroupAssignments(selectedFile, true,10 );
			//Vector p=rf.parseFactorFile();
			//FloatMatrix fm=rf.generateFactorMatrix(p, true);
			
			
			//fm.print(4, 0);
			
			
			
			
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
		}*/



	}

}
