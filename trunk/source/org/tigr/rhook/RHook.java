/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

/*
 * RHook.java
 *
 * Created on Jul 11, 2009, 1:53:47 PM
 * 
 * @author raktim
 */

package org.tigr.rhook;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.tigr.util.FloatMatrix;

class TextConsole implements RMainLoopCallbacks 
{
	public void rWriteConsole(Rengine re, String text, int oType)  {
		System.out.print(text);
		if(RHook.logger == null) {
			RHook.logger.start();
		} 
		if (oType == 1) { //Error/Warning
			RHook.log("Error/Warning -> ");
		}
		RHook.log(text);
	}

	public void rBusy(Rengine re, int which) {
		System.out.println("rBusy("+which+")");
		RHook.log("rBusy("+which+")");
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		System.out.print(prompt);
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			String s=br.readLine();
			return (s==null||s.length()==0)?s:s+"\n";
		} catch (Exception e) {
			System.out.println("jriReadConsole exception: "+e.getMessage());
		}
		return null;
	}

	public void rShowMessage(Rengine re, String message) {
		System.out.println("rShowMessage \""+message+"\"");
		JOptionPane.showMessageDialog(null, message);
	}

	public String rChooseFile(Rengine re, int newFile) {
		FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
		fd.show();
		String res=null;
		if (fd.getDirectory()!=null) res=fd.getDirectory();
		if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
		return res;
	}

	public void   rFlushConsole (Rengine re) {
	}

	public void   rLoadHistory  (Rengine re, String filename) {
	}

	public void   rSaveHistory  (Rengine re, String filename) {

	}
}


public class RHook  {
	private RHook(String[] args) {

		// just making sure we have the right version of everything
		if (!Rengine.versionCheck()) {
			System.err.println("** Version mismatch - Java files don't match library version.");
			return;
			//System.exit(1);
		}
		if(re != null) {
			System.out.println("Rengine already exists");
			return;
		}
		System.out.println("Creating Rengine (with arguments)");
		//guiFrame.rOutText.append("Creating Rengine (with arguments)\n");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		re = new Rengine(args, false, new TextConsole());
		System.out.println("Rengine created, waiting for R");
		//guiFrame.rOutText.append("Rengine created, waiting for R\n");
		// the engine creates R is a new thread, so we should wait until it's ready
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			//guiFrame.rOutText.append("Cannot load R\n");
			return;
		}
	}

	public static Rengine startRSession() throws Exception {
		// Start Logging
		if(logger != null) {
			logger.stop();
		}
		logger = new RLogger(RLogger.getLogFileName());
		logger.start();
		System.out.println("Checking for R_HOME in environment: " + System.getenv("R_HOME"));
		String r_home = System.getenv("R_HOME");
		if(r_home == null || r_home == "") {
			System.err.println("** R_HOME not avaialble or not set properly.");
			logger.writeln("** R_HOME not avaialble or not set properly.");
			logger.writeln("** Possible Causes:");
			logger.writeln("** MeV launched via WebStart");
			//logger.writeln("** MeV launched via WebStart");
			logger.stop();
			throw new Exception("R_HOME not set or available.\n** Possible Causes:\n** MeV launched via WebStart");
		}
		
		if(!(new File(r_home).exists())) {
			System.err.println("R_HOME dir: " + r_home + " does not exist.");
			logger.writeln("R_HOME dir: " + r_home + " does not exist.");
			logger.writeln("** Possible Causes:");
			logger.writeln("** MeV launched via WebStart");
			logger.writeln("** " + r_home + " location removed");
			logger.stop();
			throw new Exception("R_HOME dir: " + r_home + "does not exist.\n** Possible Causes:\n** MeV launched via WebStart\n** " + r_home + " location removed");
		}
		
		if (!Rengine.versionCheck()) {
			System.err.println("** Version mismatch - Java files don't match library version.");
			logger.writeln("** Version mismatch - Java files don't match library version.");
			logger.stop();
			throw new Exception("Java class version mismatch");
		}

		if(re != null) {
			System.out.println("Rengine already exists");
			logger.writeln("Rengine already exists");
			cleanUp();
			return re;
		}

		System.out.println("Creating Rengine (with arguments)");
		String[] args = {"--no-save"};
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		try {
			//re = new Rengine(null, false, new TextConsole());
			re = new Rengine(args, false, new TextConsole());
		} catch (Exception e) {
			System.out.println("Error loading R");
			logger.writeln("Error loading R");
			logger.writeln(e);
			throw e;
		}
		System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's ready
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			logger.writeln("Cannot load R");
			//return null;
			throw new Exception("waitForR() error: Cannot load R");
		}
		return re;
	}

	public static REXP evalR(String cmd) throws Exception {
		REXP x;
		try {
			logger.writeln(cmd);
			x = re.eval(cmd);
		} catch (Exception e) {
			logger.writeln("Rengine eval error");
			logger.writeln(e.getMessage());
			logger.writeln(e);
			throw e;
			//return false;
		}
		return x;
	}
	//Raktim's Test Code
	//List all libs
	public int listLibs() {
		System.out.println("Raktim: Test Code");
		System.out.println("Counting Libs Installed");
		//guiFrame.cmdText.append("Counting Libs Installed\n");

		System.out.println("Parsing");
		//guiFrame.cmdText.append("\t installed.packages()\n");
		long e=re.rniParse("installed.packages()", 1);
		System.out.println("Result = "+e+", running eval");
		long r=re.rniEval(e, 0);
		System.out.println("Result = "+r+", building REXP");
		REXP x=new REXP(re, r);
		System.out.println("REXP result = "+x);
		//Return 1st element of a string array
		System.out.println("REXP result = "+x.asString());
		//Returns a string array
		String d[] = x.asStringArray();
		if (d!=null) {
			//int i=0; while (i<d.length) { System.out.println(d[i]); i++; }
			System.out.println(d.length + " packages installed");
			//guiFrame.rOutText.append(d.length + " packages installed\n");
		}
		return d.length;
	}

	//Test if limma/any R package is installed
	public static void testPackage(String pkgName) throws Exception {
		//System.out.println("Raktim: Test Code");
		//System.out.println("Checking for LIMMA");
		//System.out.println("Parsing");
		logger.writeln("Checking Package - " + pkgName);

		long e=re.rniParse("which(as.character(installed.packages()[,1])=='"+pkgName+"')", 1);
		//System.out.println("Result = "+e+", running eval");
		long r=re.rniEval(e, 0);
		//System.out.println("Result = "+r+", building REXP");
		REXP x=new REXP(re, r);
		//System.out.println("REXP result = "+x);
		//Return the index of limma
		//System.out.println("REXP result = "+x.asInt());
		if(x.asInt() != 0 ) {
			System.out.println(pkgName + " Package Installed");
			logger.writeln(pkgName + " Package Installed");
		}
		else {
			System.out.println(pkgName + " Package NOT Installed");
			System.out.println("**** Attempting to install" + pkgName+ " from local rep *****");
			logger.writeln(pkgName + " Package NOT Installed");
			logger.writeln("**** Attempting to install" + pkgName+ " from local rep *****");

			//TODO Code to install a package from a local zip or tar based on OS
			String pkg = System.getProperty("user.dir")+
						 System.getProperty("file.separator")+
						 RConstants.R_PACKAGE_DIR+
						 System.getProperty("file.separator");
			if(getOS() == RConstants.WINDOWS_OS) {
				pkg = pkg + RConstants.LIMMA_WIN;
			} else if(getOS() == RConstants.LINUX_OS) {
				if(getARCH() == RConstants.OS_ARCH_32)
					pkg = pkg + RConstants.LIMMA_LINUX_32;
				if(getARCH() == RConstants.OS_ARCH_64)
					pkg = pkg + RConstants.LIMMA_LINUX_32;
			} else if(getOS() == RConstants.MAC_OS) {
				pkg = pkg + RConstants.LIMMA_MAC;
			}
			//re.eval("install.packages('" + pkg.replace("\\", "/") + "', repos=NULL)");
			evalR("install.packages('" + pkg.replace("\\", "/") + "', repos=NULL)");
		}
	}

	//Code to operate on a object returned by a function
	//We create vector sd first and then use sd to create matrix y
	public void createData(int samples, int genes, boolean print) {
		//Generating DATA
		//create a SD vector sd
		//guiFrame.cmdText.append("Simulating expression Data\n");
		System.out.println("sd <- 0.3*sqrt(4/rchisq("+ genes +",df=4))");
		//guiFrame.cmdText.append("\t sd <- 0.3*sqrt(4/rchisq("+ genes +",df=4))\n");
		long e=re.rniParse("sd <- 0.3*sqrt(4/rchisq("+ genes +",df=4))", 1);

		System.out.println("Result = "+e+", running eval");
		long r=re.rniEval(e, 0);
		System.out.println("Result = "+r+", building REXP");
		REXP x=new REXP(re, r);
		System.out.println("REXP result = "+x);
		//Return the value in sd at index 0
		System.out.println("REXP result of sd variable = "+x.asDouble());
		////guiFrame.rOutText.append("REXP result of sd variable = "+x.asDouble()+"\n");

		//Now use sd to create y
		//Creating Data Matrix
		System.out.println("y <- matrix(rnorm("+ genes +"*"+ samples +",sd=sd),"+ genes +","+ samples +")");
		//guiFrame.cmdText.append("\t y <- matrix(rnorm("+ genes +"*"+ samples +",sd=sd),"+ genes +","+ samples +")\n");
		long ee=re.rniParse("y <- matrix(rnorm("+ genes +"*"+ samples +",sd=sd),"+ genes +","+ samples +")", 1);
		System.out.println("Result = "+ee+", running eval");
		long rr=re.rniEval(ee, 0);
		System.out.println("Result = "+rr+", building REXP");
		REXP xx=new REXP(re, rr);
		System.out.println("REXP result = "+xx);

		//REXP x;
		System.out.println("rownames(y) <- paste('Gene',1:"+ genes +")");
		//guiFrame.cmdText.append("\t rownames(y) <- paste('Gene',1:"+ genes +")\n");
		re.eval("rownames(y) <- paste('Gene',1:"+ genes +")");
		int t = samples - 2;
		System.out.println("y[1:2,"+ t +":"+ samples +"] <- y[1:2,"+ t +":"+ samples +"] + 2");
		//guiFrame.cmdText.append("\t y[1:2,"+ t +":"+ samples +"] <- y[1:2,"+ t +":"+ samples +"] + 2\n");
		re.eval("y[1:2,"+ t +":"+ samples +"] <- y[1:2,"+ t +":"+ samples +"] + 2");
		re.eval("names <- rownames(y)");
		x=re.eval("names");
		String names[] = x.asStringArray();

		//Return y[1,]
		double d[][] = xx.asMatrix();
		String tmp = new String();
		if (d!=null) {
			if (print) {
				int t_genes = genes;
				int t_samples = samples;
				if(genes > 10) { t_genes = 10; }
				if(samples > 5 ) { t_samples = 5; }
				int i=0; int g=0;
				while (g<t_genes) { 
					tmp += names[g] + "\t";
					while (i<t_samples) { 
						System.out.println(d[0][i]); 
						tmp += d[g][i]+"\t"; 
						i++; 
					}
					if(samples > 5 ) { tmp += "....."; }
					tmp += "\n";
					g++;
					i=0;
				}
				System.out.println("");
				//guiFrame.rOutText.append(tmp);
				//guiFrame.rOutText.append("\n");
				//if(genes > 10) { guiFrame.rOutText.append(".....\n"); }
			}
		}
	}

	// Try Running LIMMA
	// This should go into Algorithm module
	public void runLIMMA() {
		System.out.println("Loading Lib LIMMA");
		//guiFrame.cmdText.append("Running LIMMA \nLoading Lib LIMMA\n");

		//guiFrame.cmdText.append("\t library(limma)");
		//guiFrame.cmdText.append("\n");
		re.eval("library(limma)");

		System.out.println("design <- cbind(Grp1=1,Grp2vs1=c(rep(0,dim(y)[2]/2),rep(1,dim(y)[2]/2)))");
		//guiFrame.cmdText.append("\t design <- cbind(Grp1=1,Grp2vs1=c(rep(0,dim(y)[2]/2),rep(1,dim(y)[2]/2)))");
		//guiFrame.cmdText.append("\n");
		re.eval("design <- cbind(Grp1=1,Grp2vs1=c(rep(0,dim(y)[2]/2),rep(1,dim(y)[2]/2)))");

		// Ordinary fit
		System.out.println("fit <- lmFit(y,design)");
		//guiFrame.cmdText.append("\t fit <- lmFit(y,design)");
		//guiFrame.cmdText.append("\n");
		re.eval("fit <- lmFit(y,design)");
		System.out.println("fit <- eBayes(fit)");
		//guiFrame.cmdText.append("\t fit <- eBayes(fit)");
		//guiFrame.cmdText.append("\n");
		re.eval("fit <- eBayes(fit)");

		// Various ways of summarizing or plotting the results
		System.out.println("Summarizing LIMMA result");
		//guiFrame.cmdText.append("Summarizing LIMMA result");
		//guiFrame.cmdText.append("\n");
		System.out.println("res <- topTable(fit,coef=2)");
		//guiFrame.cmdText.append("\t res <- topTable(fit,coef=2)");
		//guiFrame.cmdText.append("\n");
		re.eval("res <- topTable(fit,coef=2)");
		System.out.println("res$ID");
		//guiFrame.cmdText.append("\t res$ID");
		//guiFrame.cmdText.append("\n");
		REXP x = re.eval("res$ID");
		String gene[]=x.asStringArray();
		x = re.eval("res$logFC");
		double logFC[]=x.asDoubleArray();
		x = re.eval("res$t");
		double t[]=x.asDoubleArray();
		x = re.eval("res$P.Value");
		double pValue[]=x.asDoubleArray();
		x = re.eval("res$adj.P.Val");
		double adj_pValue[]=x.asDoubleArray();
		String tmp1 = "\t ID \t logFC \t\t t \t\t P.Value \t\t adj.P.Val\n";
		if (gene!=null) {
			int i=0;
			System.out.println(tmp1);
			while (i<gene.length) { 
				System.out.println("["+i+"] \""+gene[i]+"\""); 
				tmp1+= i+ "\t" + gene[i]+ "\t" + logFC[i] + "\t" + t[i] + "\t" + pValue[i] + "\t" + adj_pValue[i] + "\n"; 
				i++; 
			}
			//guiFrame.rOutText.append(tmp1);
			//guiFrame.rOutText.append("\n");
		}
		//guiFrame.cmdText.setFont(old_F);
		//guiFrame.cmdText.setForeground(old_C);
	}

	public static void endR(){
		re.end();
		System.out.println("ended R session");

	}

	static Rengine re;
	protected static RLogger logger;
	//JRIJFrame guiFrame;

	/* MeV API Functions */
	/**
	 * R MAtrix notation
	 * mdat <- matrix(c(1,2,3, 11,12,13), nrow = 2, ncol=3, byrow=TRUE)
	 * @param name
	 * @param fm
	 * @return
	 */
	public static REXP createRDataMatrix(String name, FloatMatrix fm) {
		REXP x = null;
		int row = fm.getRowDimension();
		int col = fm.getColumnDimension();
		String srtVector = "c(";
		int jCol = 0;
		for(int iRow = 0; iRow < row; iRow++) {
			for(jCol = 0; jCol < col-1; jCol++) {
				srtVector += fm.get(iRow, jCol) + ",";
			}
			srtVector += fm.get(iRow, jCol);
		}
		srtVector += ")";
		String cmdR = name + "<- matrix(" + srtVector + ",nrow=" + row + ",ncol=" + col + ",byrow=TRUE)";
		System.out.println("createRDataMatrix " + cmdR);
		re.eval(cmdR);
		x = re.eval(name);
		return x;
	}

	/**
	 * R MAtrix notation
	 * mdat <- matrix(c(1,2,3, 11,12,13), nrow = 2, ncol=3, byrow=TRUE,dimnames = list(c("row1", "row2"),c("C.1", "C.2", "C.3")))
	 * @param name
	 * @param fm
	 * @param rowNames
	 * @param colNames
	 * @return
	 */
	public static REXP createRDataMatrix(String name, FloatMatrix fm, ArrayList<String> rowNames, ArrayList<String> colNames) {
		REXP x = null;
		int row = fm.getRowDimension();
		int col = fm.getColumnDimension();
		String srtVector = "c(";
		int jCol = 0;
		for(int iRow = 0; iRow < row; iRow++) {
			for(jCol = 0; jCol < col-1; jCol++) {
				srtVector += fm.get(iRow, jCol) + ",";
			}
			srtVector += fm.get(iRow, jCol);
		}
		srtVector += ")";
		String dimnames = "dimnames = list(";
		String rNames = "c(";
		int iRow = 0;
		for(; iRow < row-1; iRow++) {
			rNames += "'" + (String) rowNames.get(iRow) + "',";
		}
		rNames += "'" + (String) rowNames.get(iRow) + "')";

		String cNames = "c(";
		for(iRow = 0; iRow < row-1; iRow++) {
			cNames += "'" + (String) colNames.get(iRow) + "',";
		}
		cNames += "'" + (String) colNames.get(iRow) + "')";

		dimnames += rNames + "," + cNames + ")";

		String cmdR = name + "<- matrix(" + srtVector + ",nrow=" + row + ",ncol=" + col + ",byrow=TRUE," + dimnames +")";
		System.out.println("createRDataMatrix " + cmdR);
		re.eval(cmdR);
		x = re.eval(name);
		return x;
	}

	public static REXP createRDataMatrix(String name, FloatMatrix fm, String[] rowNames, String[] colNames) {
		REXP x = null;
		int row = fm.getRowDimension();
		int col = fm.getColumnDimension();
		String srtVector = "c(";

		for(int iRow = 0; iRow < row; iRow++) {
			for(int jCol = 0; jCol < col; jCol++) {
				if(iRow == row-1 & jCol == col-1)
					srtVector += fm.get(iRow, jCol);
				else 
					srtVector += fm.get(iRow, jCol) + ",";
			}
		}
		//srtVector = srtVector.substring(0, srtVector.length()-2);
		srtVector += ")";
		String dimnames = "dimnames = list(";
		String rNames = "c(";
		int iRow = 0;
		for(; iRow < row-1; iRow++) {
			rNames += "'" + (String) rowNames[iRow] + "',";
		}
		rNames += "'" + (String) rowNames[iRow] + "')";

		String cNames = "c(";
		for(iRow = 0; iRow < col-1; iRow++) {
			cNames += "'" + (String) colNames[iRow] + "',";
		}
		cNames += "'" + (String) colNames[iRow] + "')";

		dimnames += rNames + "," + cNames + ")";

		String cmdR = name + "<- matrix(" + srtVector + ",nrow=" + row + ",ncol=" + col + ",byrow=TRUE," + dimnames +")";
		System.out.println("createRDataMatrix " + cmdR);
		re.eval(cmdR);
		x = re.eval(name);
		return x;
	}

	private static void cleanUp() throws Exception {
		//re.eval("rm(list = ls())");		
		evalR("rm(list = ls())");
		//logger.stop();
	}

	public static void endRSession() throws Exception {
		cleanUp();
		logger.stop();
	}

	public static void createRDataMatrixFromFile(String rObj,	String filePath, boolean rowNames, String[] colNames) throws Exception {
		//Create col names vector
		String cNames = "cols <- c(0,";
		int iRow = 0;
		for(; iRow < colNames.length-1; iRow++) {
			cNames += "'" + (String) colNames[iRow] + "',";
		}
		cNames += "'" + (String) colNames[iRow] + "')";

		//re.eval(cNames);
		evalR(cNames);
		//Create a data frame
		String cmdR = rObj + " <- read.delim('" +filePath+ "', header=FALSE, sep='\t', row.names=1, col.names=cols)";
		//re.eval(cmdR);
		evalR(cmdR);
		//Convert to matrix
		//re.eval(rObj + " <- as.matrix(" + rObj + ")");
		evalR(rObj + " <- as.matrix(" + rObj + ")");
	}

	public static void log(Exception e) {
		logger.writeln(e);
	}

	public static void log(String str) {
		logger.writeln(str);
	}

	private static int getOS() {
		String os = System.getProperty("os.name");
		//String arch = System.getProperty("os.arch");
		//String ver = System.getProperty("os.version");
		if (os.toLowerCase().contains("mac")) {
			return RConstants.MAC_OS;
		}
		if (os.toLowerCase().contains("linux")) {
			return RConstants.LINUX_OS;
		}
		if (os.toLowerCase().contains("win")) {
			return RConstants.WINDOWS_OS;
		}
		return RConstants.UNKNOWN_OS;
	}
	
	private static int getARCH() {
		//String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		//String ver = System.getProperty("os.version");
		if (arch.toLowerCase().contains("386") || arch.toLowerCase().contains("32")) {
			return RConstants.OS_ARCH_32;
		}
		if (arch.toLowerCase().contains("686") || arch.toLowerCase().contains("64")) {
			return RConstants.OS_ARCH_64;
		}
		
		return RConstants.UNKNOWN_ARCH;
	}
}
