

package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants;
import org.tigr.microarray.mev.cluster.gui.impl.util.MatrixFunctions;
import org.tigr.util.FloatMatrix;

public class GSEA extends AbstractAlgorithm {

	/**
	 * @param args
	 * 
	 */
	private Random aRandom=new Random();
	private LinkedHashMap overEnrichedPVals=new LinkedHashMap();
	private LinkedHashMap underEnrichedPVals=new LinkedHashMap();
	private boolean stop=false;


	/**
	 * This method should interrupt the calculation.
	 *
	 */
	public void abort() {
		stop=true;
	}



	public AlgorithmData execute(AlgorithmData data)throws AlgorithmException{

		try{
			
			//Get the number of permutations and call gsealmPerm
			int num_perms=Integer.parseInt(data.getParams().getString("permutations"));
			if(stop)
				return null;
			gsealmPerm(num_perms, data, true);


		}catch(Exception e){
			e.printStackTrace();
		}

		return data;



	}
	/**
	 * 
	 * @param aData
	 * @param removeNA
	 * @param factor_matrix
	 * @return Hashtable
	 * 
	 * @throws IOException 
	 * 
	 * 
	 * lmPerGene is modeled after the R function in GSEAlm package. The only
	 * difference being that this implementation only allows number of factors
	 * to be greater than equal to one and less than equal to three
	 * 
	 * Function returns a hashtable with coefficients and coefvars
	 * 
	 */
	public Hashtable<String, FloatMatrix> lmPerGene(AlgorithmData aData, FloatMatrix factor_matrix, boolean removeNA) {
		
		FloatMatrix gene_expression=aData.getGeneMatrix("gene-data-matrix"); 
		FloatMatrix x;
		FloatMatrix eSet;
		FloatMatrix xTranspose;
		FloatMatrix xx;
		FloatMatrix identity;
		FloatMatrix xy;
		FloatMatrix res;
		FloatMatrix beta;
		FloatMatrix xxInv;
		int nSamp;
		Hashtable<String, FloatMatrix>returnHash=new Hashtable<String, FloatMatrix>();

		
		if(removeNA)
			eSet=removeUnassignedSamples(aData.getVector("unassigned-samples"),gene_expression);
		else
			eSet=gene_expression;
		
		x=factor_matrix;
		nSamp=eSet.getColumnDimension();

		xTranspose=x.transpose();

		xx=xTranspose.times(x);

		//In the R function, they use solve(a) and since the second argument is not provided, solve returns teh inverse of a.
		//so, in R, if you say solve(a); the second argument b is considered to be an identity matrix. Trying to replicate that
		//FloatMatrix xxInv=xx.inverse();

		identity=new FloatMatrix(xx.getRowDimension(),xx.getColumnDimension());

		identity=FloatMatrix.identity(identity.getRowDimension(), identity.getColumnDimension());
		try{
		//Change this to avois errors if matrix is not singular	
		xxInv=xx.solve(identity);
		
		FloatMatrix hMat=x.times(xxInv).times(xTranspose);
		FloatMatrix diagonalMatrix=createDiagonalMatrix(nSamp, nSamp, 1);
		FloatMatrix dMat=diagonalMatrix.minus(hMat);


		int col=x.getColumnDimension();

		//Will possibly have lesser columns than the original expression set 

		xy=eSet.times(x);
		res=eSet.times(dMat);
		beta=xx.solve(xy.transpose());


		//result matrix is calculated using arrayTimes function of Jama. The dimensions of eSet and res are the same.
		//this is the equivalent of "eSet*res"  in R
		FloatMatrix result=eSet.arrayTimes(res);
		float[]varr=getRowSumandSome(result, (nSamp-col));
		Vector diagonal=returnDiagonal(xxInv);

		FloatMatrix varbeta=createMatrix(diagonal,  eSet.getRowDimension(), col);

		int[]margin={2};
		FloatMatrix temp=apply(varbeta, margin, varr, org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants.APPLY_DEFAULT );

		FloatMatrix coefvar=temp.transpose();
		
		//Add to AlgorithmData
		returnHash.put("lmPerGene-coefficients", beta);
		returnHash.put("lmPerGene-coefvar", coefvar);
		}catch(Exception e){
			
		}
		return returnHash;
	}





	public FloatMatrix createMatrix(Vector elements, int row, int col){
		FloatMatrix customMatrix=new FloatMatrix(row,col);
		int index=0;

		for(int j=0;j<col;j++){
			for(int i=0;i<row;i++){
				customMatrix.set(i, j, ((Float)elements.get(index)).floatValue());

			}
			if(index<elements.size()){
				index=index+1;
			}else
				index=0;
		}

		return customMatrix;

	}

	/**
	 * 
	 * This function returns the diagonal elements of a given matrix
	 * 
	 * TO DO: returnDiagonal and createDiagonalMatrix can be combined into one.
	 * Just add one more argument FloatMatrix to the createDiagonalMatrix function
	 * if this matrix is not null and has the required dimensions, return the diagonal of this matrix 
	 * as a FloatMatrix. If this matrix is null, create a FloatMatrix of the given dimensions and populate the diagonal elements
	 * with val
	 * 
	 */	
	public Vector returnDiagonal(FloatMatrix fm){

		Vector diagonalElements=new Vector();

		for(int row=0;row<fm.getRowDimension();row++){
			int col=0;
			while(col<fm.getColumnDimension()){
				if(row==col){
					diagonalElements.add(row, fm.get(row, col));
					break;
				}else
					col=col+1;

			}
		}

		return diagonalElements;
	}




	/**
	 * 
	 * Creates a diagonal matrix with the given value
	 * 
	 * 
	 * @param row
	 * @param col
	 * @param val
	 * @return
	 */

	public FloatMatrix createDiagonalMatrix(int row, int col, int val){
		FloatMatrix diagonalMatrix;
		int value;


		//If no value is specified, fill the matrix diagonal with 1
		if((value=val)==0){
			value=1;
		}else
			value=val;


		if(row==0){
			diagonalMatrix=new FloatMatrix(col, col, value);
		}else if(col==0){
			diagonalMatrix=new FloatMatrix(row, row, value);
		}else
			diagonalMatrix=new FloatMatrix(row, col, val);

		for(int rows=0; rows<row; rows++){
			for(int cols=0; cols<col; cols++){
				if(rows==cols){
					diagonalMatrix.set(rows, cols, value);
				}else
					diagonalMatrix.set(rows, cols, 0);
			}
		}

		return diagonalMatrix;



	}

	/**
	 * 
	 * removeUnassignedSamples removes the samples which do not have any factor assignments.
	 * 
	 * @param geneExpression
	 * @param unassigned
	 * @return FloatMatrix with the samples assigned NA removed 
	 */
	public FloatMatrix removeUnassignedSamples(Vector unassigned, FloatMatrix geneExpression){
		int colindex=0;
		int matrixIndex=0;
		int column=0;

		FloatMatrix withoutNA=new FloatMatrix(geneExpression.getRowDimension(), (geneExpression.getColumnDimension()-unassigned.size()));

		int size=unassigned.size();
		for(int i=0; i<geneExpression.getColumnDimension(); i++){
			if(!unassigned.contains(i)){
				withoutNA.setMatrix(0, withoutNA.getRowDimension()-1, matrixIndex, matrixIndex, geneExpression.getMatrix(0, geneExpression.getRowDimension()-1, i, i));
				if(matrixIndex<withoutNA.getColumnDimension())
					matrixIndex=matrixIndex+1;
			}

		}



		return withoutNA;

	}

	/**
	 * getRowSums returns a vector containing the row sums.
	 * The size of the vector would be equal to the number of rows of
	 * the matrix.
	 * 
	 * @param matrix
	 * @return
	 */
	public Vector getRowSums(FloatMatrix matrix){
		Vector rowSums=new Vector();
		for(int index=0; index<matrix.getRowDimension(); index++){
			float _tempVal=0;
			for(int col=0; col<matrix.getColumnDimension(); col++){
				if(!Float.isNaN(matrix.get(index, col)))
					_tempVal+=matrix.get(index, col);
			}
			rowSums.add(index, _tempVal);
		}
		return rowSums;
	}








	/**
	 * Returns a float array containing rowsums/(nSamp-col)
	 * @param float[]
	 * @return
	 */

	public float[] getRowSumandSome(FloatMatrix matrix, int val){
		float[] _tempList=new float[matrix.getRowDimension()];
		Vector sum=getRowSums(matrix);

		for(int i=0; i<sum.size(); i++){
			float value=((Float)sum.get(i)).floatValue();
			_tempList[i]=value/val;
		}





		return _tempList;

	}



	/**
	 * The apply function has been modeled after the R function "apply".
	 * This implementation provides the default function  "function(x)x*varr"
	 * used by lmPerGene function. It also provides the implementation for mean
	 * 
	 * MARGIN=1(row) or 2(column).  
	 * 
	 * As of now this function applies the array "varr" to the columns(MARGIN==2) of "matrix".
	 * The assumption is that length of varr==matrix.getRowDimension().   
	 * 
	 * @return
	 */
	public FloatMatrix apply(FloatMatrix matrix, int[] margin, float[]varr, String function ){//commented to test if double makes any differnce

		/**
		 * This loop applies function across the rows. 
		 * 
		 */

		FloatMatrix tempMatrix=new FloatMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
		if(margin.length==1&&margin[0] ==2){
			if(function.equalsIgnoreCase(org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants.APPLY_DEFAULT)){
				for(int col=0;col<matrix.getColumnDimension();col++){
					for(int row=0;row<matrix.getRowDimension();row++){
						float temp=(matrix.get(row, col))*varr[row];
						tempMatrix.set(row,col,temp);

					}
				}
			}
		}
		return	tempMatrix;


	}



	/**
	 * Sweep function is modeled afTer the R function "sweep"
	 * Unlike R, this implementation currently allows sweep only across rows. This can be changed in the future if 
	 * necessary.
	 * 
	 * Unlike R, the default function is not minus; but divide. Divide is the default when sweep is called
	 * from GSNormalize.
	 * 
	 * 
	 * 
	 * @param matrix
	 * @param margin
	 * @param stats
	 * @param function
	 * @return
	 */


	public FloatMatrix Sweep(FloatMatrix matrix, int[]margin, Vector stats,  String function){

		FloatMatrix swept=new FloatMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
		int statLength=stats.size();
		int prodMargin;

		if(margin.length==2){
			prodMargin=margin[0]*margin[1];
		}else
			prodMargin=margin[0];


		/*	
		 * These two error messages have been temporarily commented. I suspect the checks are wrong
		 * if(statLength > prodMargin || statLength < prodMargin){
			String eMsg="<html>The length of stats vector does not match the <br>" +
			"<html>dimensions of matrix. Cannot execute Sweep function </html>";
			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		if(statLength <=0){
			String eMsg="<html>The length of stats vector is zero<br>" +
			"<html>There was an error in calculating rowsums of AssociationMatrix <br>" +
			"<html>Cannot execute Sweep function </html>";
			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}*/

		//This loop applies "sweep" across the rows. Seperate loops would be needed to extend the sweep
		//function across columns and rows&columns. 
		for(int col=0; col<matrix.getColumnDimension();col++){
			for(int row=0; row<matrix.getRowDimension(); row++){
				/**
				 * DIVIDE is the default function as of now. If we need to accomodate any other function (eg minus,sqrt etc)
				 * add a constant to GSEAConstants and implement the function here.
				 */
				if(function.equalsIgnoreCase(org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants.DIVIDE_FUNCTION)){
					float val1=matrix.get(row, col);
					float val2=((Float)stats.get(row)).floatValue();
					val1=val1/val2;
					swept.set(row, col, val1);
				}
			}
		}


		return swept;

	}


	/**
	 * 
	 * @param dataSet
	 * @param incidence
	 * @param GSEAfun
	 * @param func1
	 * @param func2
	 * @param removeShift
	 * @param removeStat
	 * @return
	 */
	public FloatMatrix GSNormalize(FloatMatrix dataSet, FloatMatrix incidence, String GSEAfun, String func1, String func2
			, boolean removeShift, String removeStat){

		Vector normBy=new Vector();
		FloatMatrix outM=null;
		Vector rowSums, sqrtVector;



		if(removeShift){
			//While using apply here, the return is expected to be Vector. The apply function i have
			//returns FloatMatrix
			//	Vector colStat=apply(dataSet, 2, null,removeStat);
		}
		int nCol=incidence.getColumnDimension();
		int nRows=dataSet.getRowDimension();


		if(nCol!=nRows){
			String eMsg="<html>GSNormalize: Nonconforming matrices</html>";
			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
			//System.exit(1);
		}

		//Default GSEA implementation
		if(GSEAfun.equalsIgnoreCase(org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants.CROSS_PROD)&&func1.equalsIgnoreCase(GSEAConstants.DIVIDE_FUNCTION)
				&& func2.equalsIgnoreCase(GSEAConstants.SQRT)){
			rowSums=new Vector();
			sqrtVector=new Vector();

			//R uses the crossprod function here.



			outM=incidence.times(dataSet);//Check with Asaf if this is right?--It is right


			MatrixFunctions MatrixFunc=new MatrixFunctions();
			rowSums=MatrixFunc.getRowSums(incidence);


			for(int index=0;index<rowSums.size();index++){
				float temp=((Float)rowSums.get(index)).floatValue();
				float sqrt=(float) java.lang.Math.sqrt(temp);
				//rowSums.add(index, sqrt);//Commented for testing if out of memeory error is resolved; and it is
				sqrtVector.add(index, sqrt);
			}
			//normBy=rowSums;
			normBy=sqrtVector;
			int[]margin={1};
			/*Print normBy vector
			System.out.println("Printing the normBy vector");
			for(int i=0; i<normBy.size(); i++){
				System.out.println(normBy.get(i));
			}*/




			outM=Sweep(outM,margin,normBy, func1);
			//	System.out.println("GSNormalize:outM diemsions again:"+outM.getRowDimension()+":"+outM.getColumnDimension());

		}

		//Used in GSEAlmPerm, func2==Identity, when number of factors is zero
		if(GSEAfun.equalsIgnoreCase(GSEAConstants.CROSS_PROD)&& func1.equalsIgnoreCase(GSEAConstants.DIVIDE_FUNCTION)
				&& func2.equalsIgnoreCase(GSEAConstants.IDENTITY_FUNCTION)){
			outM=dataSet.transpose().times(incidence);//Check this
			rowSums=new Vector();
			rowSums=getRowSums(outM);
			normBy=rowSums;
			int[]margin={1};

			outM=Sweep(outM,margin,normBy, func1);


		}




		return outM;

	}

	
	public void gsealmPerm(int num_perms, AlgorithmData adata, boolean removeNA )throws Exception{

		FloatMatrix observedStats;
		FloatMatrix perms = null;

		/**
		 * First step is to extract the factor names, factor levels and assignments.
		 * 
		 * 
		 */
		String[]factorNames=adata.getStringArray("factor-names");
		int[]factorlevels=adata.getIntArray("factor-levels");
		int[][]factorAssignments=adata.getIntMatrix("factor-assignments");
		//Number of samples
		int nSamp=adata.getGeneMatrix("gene-data-matrix").getColumnDimension();
		
		
		/**
		 *Second step is to 
		 *1. Process the group assignments supplied by user and generate a factor matrix.
		 *2. Run lmPerGene
		 *  
		 * 
		 */
		ProcessGroupAssignments pg= new  ProcessGroupAssignments(factorNames, factorlevels, factorAssignments, true, nSamp);
		pg.findUnassignedSamples(factorNames, factorlevels, factorAssignments);
		adata.addVector("unassigned-samples", pg.getUnassignedColumns());
		FloatMatrix factor_matrix=pg.generateFactorMatrix(factorNames, factorlevels, factorAssignments);
		try{
		Hashtable<String, FloatMatrix>tempHash=lmPerGene(adata, factor_matrix, true);


		//Extract the result of lmPerGene, returned as Hashtable 
		FloatMatrix coefficients=tempHash.get("lmPerGene-coefficients");	
		FloatMatrix coefVar=tempHash.get("lmPerGene-coefvar");
		FloatMatrix amat=adata.getGeneMatrix("association-matrix");
		//Used to populate test statistic in gene set. Used for plotting in GUI
		adata.addGeneMatrix("lmPerGene-coefficients", tempHash.get("lmPerGene-coefficients"));
		adata.addGeneMatrix("lmPerGene-coefvar", tempHash.get("lmPerGene-coefvar"));

		/**
		 * Third step is to calculate per gene set statistic before permuting.
		 * Zero factors are no longer allowed, so no need to check for it or provide the 
		 * alternate execution steps
		 * 
		 * 
		 */



		//Extract the portion that contains the main factor coefficients. The zeroth coefficient is the intercept.
		//The second coefficient will be that of the main factor.
		FloatMatrix coef_intermediate=coefficients.getMatrix(1,1,0,coefficients.getColumnDimension()-1);


		//Extract the portion that contains the main factor coefVars
		FloatMatrix coefVar_intermediate=coefVar.getMatrix(1, 1, 0, coefVar.getColumnDimension()-1);


		//Calculate the sqrt of each element of the coefVar_intermediate matrix
		FloatMatrix sqrtCoefVar=new FloatMatrix(coefVar_intermediate.getRowDimension(), coefVar_intermediate.getColumnDimension());

		for(int col=0;col<coefVar_intermediate.getColumnDimension();col++){
			sqrtCoefVar.set(0,col, (float)Math.sqrt(coefVar_intermediate.get(0, col)));
		}
		//Divide the coef_intermediate matrix with sqrtCoefVar matrix
		FloatMatrix result1=coef_intermediate.arrayRightDivide(sqrtCoefVar);


		//I have passed the transpose of the result matrix here. The reason being that in the result matrix, the genes are in the
		//columns, and each row corresponds to (intercept, mainfactor, otherfactors if present)
		observedStats=GSNormalize(result1.transpose(), amat, GSEAConstants.CROSS_PROD, GSEAConstants.DIVIDE_FUNCTION, 
				GSEAConstants.SQRT, false, null);

		/**
		 * Fourth Step is to generate a permutation matrix and loop through the number of permutations 
		 * suggested by user.
		 * 
		 * 
		 */




		//permutation matrix with rows equal to the number of genes and colmuns equal to number of permutations
		FloatMatrix permMat=new FloatMatrix(adata.getGeneMatrix("gene-data-matrix").getRowDimension(), num_perms); 

		//In the R code, it says index=1L; check of this is the same as zeroth index in JAVA
		
		for(int index=0; index<num_perms; index++){


			Hashtable resultHash=findOriginalClassOrder(factorNames,factorlevels, factorAssignments, nSamp );
			Hashtable permutedFactorHash=generatePermutedFactorHash((Hashtable)resultHash.get("original-order"), (int[])resultHash.get("permuted-order"), factorNames, factorlevels, factorAssignments, nSamp);

			//pga is created just so that we can access the function generateFactorMatrix from the ProcessGroupAssignments class
			int[][]permutedFactorAssignments=(int[][])permutedFactorHash.get("permuted-factor-assignments");


			ProcessGroupAssignments pga=new ProcessGroupAssignments(factorNames,factorlevels,permutedFactorAssignments,true, nSamp);
			//Added these two lines in for testing
		//	pga.findUnassignedSamples(factorNames, factorlevels, permutedFactorAssignments);
			//adata.addVector("unassigned-samples", pga.getUnassignedColumns());


			
			FloatMatrix factor_matrix_new=pga.generateFactorMatrix(factorNames,factorlevels, permutedFactorAssignments);

			Hashtable<String, FloatMatrix>lmPerGeneresultHash=lmPerGene(adata,factor_matrix_new , true);
			FloatMatrix pCoefficients=lmPerGeneresultHash.get("lmPerGene-coefficients");	
			FloatMatrix pCoefVar=lmPerGeneresultHash.get("lmPerGene-coefvar");

			FloatMatrix pcoef_intermediate=pCoefficients.getMatrix(1,1,0,pCoefficients.getColumnDimension()-1);
			//Extract the portion that contains the main factor coefVars
			FloatMatrix pcoefVar_intermediate=pCoefVar.getMatrix(1, 1, 0, pCoefVar.getColumnDimension()-1);

			//Calculate the sqrt of each element of the coefVar_intermediate matrix
			FloatMatrix psqrtCoefVar=new FloatMatrix(pcoefVar_intermediate.getRowDimension(), pcoefVar_intermediate.getColumnDimension());

			for(int col=0;col<pcoefVar_intermediate.getColumnDimension();col++){
				psqrtCoefVar.set(0,col, (float)Math.sqrt(pcoefVar_intermediate.get(0, col)));
			}
			//Divide the coef_intermediate matrix with sqrtCoefVar matrix
			//TO DO: Check the matrxi dimensions (permMat and result)if they are the same
			FloatMatrix result2=pcoef_intermediate.arrayRightDivide(psqrtCoefVar);

			permMat.setMatrix(0, permMat.getRowDimension()-1, index, index, result2.transpose());


		}
		
		//Assigning to a function global variable to enable passing on as parametres
		//Dimesnions of this matrix would be equal to (row=number of gene sets, column=number of samples)

		perms=GSNormalize(permMat, amat, GSEAConstants.CROSS_PROD, GSEAConstants.DIVIDE_FUNCTION, 
				GSEAConstants.SQRT, false, null);
	
		/**
		 * Fifth step is to calculate p values based on the observed and the permuted values
		 * 
		 */

		Vector<String> geneSetNames=(Vector<String>)adata.getVector("gene-set-names");

		pValFromPermMat(observedStats, perms, geneSetNames, adata);
		}catch(Exception e){
			
		}

		
	}
	
	

	

	/**
	 * pValFromPermMat should return a String array, containing three columns namely;
	 * 1) Gene set names
	 * 2) Lower pvalues
	 * 3) Upper pvalues
	 * 
	 * 
	 * @param obsStats
	 * @param permMat
	 * @return String[][] will be used to generate a JTable
	 */	
	public void pValFromPermMat(FloatMatrix obsStats, FloatMatrix permMat, Vector<String> geneSetNames, AlgorithmData data){



		float[]lower_rowSums=new float[permMat.getRowDimension()];
		float[]upper_rowSums=new float[permMat.getRowDimension()];


		
		HashMap tempHash=new HashMap();

		int nCols=permMat.getColumnDimension();
		//pVals is a FloatMatrix with rows equal to that of permMat and cols =2. This matrix will be 
		//copied into the String array resultMatrix.
	//	FloatMatrix pVals=new FloatMatrix(permMat.getRowDimension(),2, Float.valueOf("NaN"));


		//tempObs has dimensions equal to permMat
		FloatMatrix tempObs=new FloatMatrix(permMat.getRowDimension(), permMat.getColumnDimension());

		//Replicate/Copy the obsStats matrix into all the columns of tempObs.
		//This loop tries to replicate the functionality of the R function "rep"

		for(int i=0; i<tempObs.getColumnDimension(); i++){
			tempObs.setMatrix(0, tempObs.getRowDimension()-1, i, i, obsStats);
		}

		//Dimensions of tempObs are equal to that of perms
		/**
		 * This loop does the following:
		 * 
		 * 1. Compare the elements in perm Mat with elements in tempObs, to check for two conditions.
		 * First is whether the elements in permMat are greater than the corresponding elements in tempObs 
		 * matrix. If so, we add 1 to the variable num_upper_pVals, else we add 0. Store each of the num_upper_pVals
		 * in the rowSum vector. These values will later be divided by N (ncol(permMat)). These would be the upper p Vals.
		 * 
		 * 
		 * 2. The second condition we check for is if elements in permMat are lesser than corresponding elements in
		 * tempObs. Repeat the procedure as decribed above.
		 * 
		 * 
		 * 
		 * 
		 * 
		 */

		for(int row=0; row<permMat.getRowDimension(); row++){
			int num_lower_pVals=0;
			int num_upper_pVals=0;

			for(int column=0; column<permMat.getColumnDimension(); column++){
				if(permMat.get(row, column)>=tempObs.get(row, column)){
					num_upper_pVals=num_upper_pVals+1;
				}else if(permMat.get(row, column)<=tempObs.get(row, column)){
					num_lower_pVals=num_lower_pVals+1;
				}

			}
			lower_rowSums[row]=(num_lower_pVals);
			upper_rowSums[row]=(num_upper_pVals);
		}

	
		//Add the over enriched p values to the HashMap. This was done to be able to
		//sort the pvalues, to be displayed in the pValue Graph
	
		for(int row=0; row<permMat.getRowDimension(); row++){
			tempHash.put(geneSetNames.get(row), (upper_rowSums[row]/nCols));
		}
		
		//Sort the p Values in ascending order
		this.overEnrichedPVals=this.sortHashMapByValues(tempHash);
		tempHash.clear();
		tempHash=new HashMap();
		
		//Add the under enriched p values to the HashMap 
		
		for(int row=0; row<permMat.getRowDimension(); row++){
			tempHash.put(geneSetNames.get(row), (lower_rowSums[row]/nCols));
		}
		
		//Sort the p Values in ascending order
		this.underEnrichedPVals=this.sortHashMapByValues(tempHash);
		tempHash.clear();
	
		data.addMappings("over-enriched", overEnrichedPVals);
		data.addMappings("under-enriched", underEnrichedPVals);
		
		
		
	}

	public LinkedHashMap sortHashMapByValues(HashMap passedMap){
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap sortedMap = 
	        new LinkedHashMap();
	    
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







	/**
	 * findOriginalClassOrder/permuteLabels  is modeled after the R functions split and unsplit.
	 * findOriginalClassOrder will find the original sample classification.
	 * This needs to be done just once and hnec has been seperated in to a different function.
	 * Number of permutations can be 100/100 whatever. No need to repeat this step.
	 *  
	 * 
	 * @param factorHash
	 * factorHash has the factor name as the key and a vector containing the individual sample assignments as the value. 
	 * 
	 * @param factorLevels
	 * factorLevels is a vector containing the level of each factor in the model
	 * 
	 * @param num_factors
	 * num_factors is the number of factors in the model
	 * 
	 * @return
	 */

	public Hashtable findOriginalClassOrder(String[]factorNames, int[]factorLevels, int[][]factorAssignments, int num_Samples){
		Hashtable origOrder=new Hashtable();
		Hashtable permOrder=new Hashtable();
		Hashtable resultHash=new Hashtable();

		//Number of factors equal to one
		//Hashtable origOrder had key="samplenumber" and value="classification". In case of one factor,
		//origOrder has key=samplenumber and value="one-factor-class" for all samples
		if(factorNames.length==1){

			int[]factorAssignment=factorAssignments[0];
			Vector assign=new Vector();


			for(int i=0; i<num_Samples; i++){
				assign.add(i);
				origOrder.put(i,"one-factor-class");
			}



			permOrder.put("one-factor-class", assign);

		}


		//Loops over the number of samples in the data
		for(int index=0; index<num_Samples; index++){


			//Number of factors is 3. so we will permute the factor labels of factors 2 and 3. The assumption is that
			//the first factor is the main factor.
			if(factorNames.length>=2){
				//StringBuffer classAssignment=new StringBuffer("CLASS-");
				String classAssignment=new String("CLASS-");
				//The vector containing the individual factor assignments for each sample (one sample at a time)
				for(int i=1; i<factorNames.length; i++){
					//Checking factor assignment combinations (e.g: Male/Diploid or Female/Hyperdiploid)for every sample
					//If the combination happens to be (Male/NA or NA/Hyperdiploid), that sample is assigned to class-unknown
					if(factorAssignments[i][index]!=0){
						classAssignment=classAssignment+Integer.toString(factorAssignments[i][index]);
						//classAssignment=classAssignment.append(new StringBuffer(Integer.toString(factorAssignments[i][index])));
					}else
						classAssignment=new String("CLASS-unknown");
				}
				classAssignment=classAssignment.trim();
				if(!permOrder.containsKey(classAssignment)){
					Vector temp=new Vector();
					temp.add(index);
					permOrder.put(classAssignment, temp);
				}else{
					((Vector)(permOrder.get(classAssignment))).add(index);

				}


				if(!origOrder.containsKey(index)){
					origOrder.put(index, classAssignment);
				}


			}//End of two or more factor if loop


		}


		//System.out.println("number of keys in permOrder:"+permOrder.size());
		Enumeration keys=permOrder.keys();

		int[]permutedSampleAssignment=new int[num_Samples];
		while(keys.hasMoreElements()){
			String key=(String)keys.nextElement();
			Vector value=(Vector)permOrder.get(key);

			ArrayList permutedArray=new ArrayList(value.size());
			//	System.out.println("size of permuted array before permutation is:"+permutedArray.size());
			permutedArray=(ArrayList)getPermutedValues(value);
			//System.out.println("size of permuted array after permutation is:"+permutedArray.size());


			/*****Added for testing******/



			for (int i = 0; i < value.size(); i++) {
				int temp=((Integer)value.get(i)).intValue();
				permutedSampleAssignment[temp]=((Integer)permutedArray.get(i)).intValue();


			}
			/*****Added for testing ends******/
			permOrder.remove(key);
			//permOrder.put(key, permutedArray);
		}

		permOrder.clear();


		resultHash.put("original-order", origOrder);
		resultHash.put("permuted-order", permutedSampleAssignment);


		return resultHash;
	}



	public ArrayList getPermutedValues(Vector values){

		ArrayList permutedValidArray = new ArrayList(values.size());


		for (int i = 0; i < values.size(); i++) {
			//System.out.println("original Values:"+((Integer)values.get(i)).intValue());
			permutedValidArray.add(i, ((Integer)values.get(i)).intValue());
		}

		int aStart=0;
		int aEnd=values.size()-1;


		for (int i = permutedValidArray.size(); i > 1; i--) {

			int randomNumber =  aRandom.nextInt(i - 1);
			//    System.out.println("random number is:"+randomNumber);
			int temp = ((Integer)permutedValidArray.get(randomNumber)).intValue();
			permutedValidArray.set(randomNumber,  permutedValidArray.get(i-1));
			permutedValidArray.set(i-1, temp);


		}  

		return permutedValidArray;

	}




	//only for testing, will replace old one if works.

	public Hashtable generatePermutedFactorHash(Hashtable origOrder, int[]permOrder, String[]factorNames, int[]factorLevels, int[][]factorAssignments, int num_samples){

		Hashtable permutedFactorHash=new Hashtable();
		int[] rowVector=new int[1];
		int[][]permutedFactorAssignments=new int[factorNames.length][num_samples];
		int sample;



		// Number of factors greater than or equal to 1
		if(factorNames.length>=1){
			rowVector=factorAssignments[0];
			for(int index=1; index<factorNames.length; index++){
				permutedFactorAssignments[index]=factorAssignments[index];
			}

			int[]permutedSampleOrder=permOrder;

			for(int index=0; index<num_samples; index++){
				permutedFactorAssignments[0][index]= rowVector[permutedSampleOrder[index]];

			}



		}

		permutedFactorHash.put("permuted-factor-assignments", permutedFactorAssignments);



		return permutedFactorHash;

	}




	public static void main(String[] args) {
		FloatMatrix m=new FloatMatrix(4,3);
		FloatMatrix geneset=new FloatMatrix(2,4);
		float[][] _temp={{1,1,1,0},{1,0,1,0}};

		for(int i=0; i<2; i++){
			for(int j=0; j<4; j++){
				geneset.set(i, j, _temp[i][j]);
			}
		}





		int index=1;
		for(int col=0; col<3; col++){
			for(int row=0; row<4; row++){
				m.set(row, col, index);
				index=index+1;
			}
		}

		System.out.println("matrix :");
		for(int row=0; row<4; row++){
			for(int col=0; col<3; col++){
				System.out.print(m.get(row, col));
				System.out.print('\t');
			}
			System.out.println();
		}
		System.out.println("matrix Ends:");

		System.out.println("printing geneset");
		for(int i=0; i<2; i++){
			for(int j=0; j<4; j++){
				System.out.print(geneset.get(i, j));
				System.out.print('\t');
			}
			System.out.println();
		}
		System.out.println("printing gene set ends");

		GSEA gsea=new GSEA();
		FloatMatrix _gSNormRes=gsea.GSNormalize(m, geneset, GSEAConstants.CROSS_PROD, GSEAConstants.DIVIDE_FUNCTION, GSEAConstants.SQRT, false, null);

		System.out.println("printing GSNormalize results");
		for(int i=0; i<_gSNormRes.getRowDimension(); i++){
			for(int j=0; j<_gSNormRes.getColumnDimension(); j++){
				System.out.print(_gSNormRes.get(i, j));
				System.out.print('\t');
			}
			System.out.println();
		}





	}






}
