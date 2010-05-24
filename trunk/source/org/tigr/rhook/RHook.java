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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.tigr.microarray.mev.TMEV;
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
		}
		if(re != null) {
			System.out.println("Rengine already exists");
			return;
		}
		System.out.println("Creating Rengine (with arguments)");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		re = new Rengine(args, false, new TextConsole());
		System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's ready
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
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
		
		// if RHOME variable is set
		isRhomeSet(r_home);

		// if R_HOME location exist
		doesExistRhome(r_home);
		
		// if OS specific dynamic lib exist for R
		doesExistRlib();
		
		if (!Rengine.versionCheck()) {
			System.err.println("** Version mismatch - Java files don't match library version.");
			logger.writeln("** Version mismatch - Java files don't match library version.");
			logger.stop();
			throw new Exception("Java class version mismatch");
		}

		if((re = Rengine.getMainEngine()) != null) {
			System.out.println("Rengine already exists");
			logger.writeln("Rengine already exists");
			cleanUp();
			return re;
		}

		System.out.println("Creating Rengine (with arguments)");
		String[] args = {"--no-save"};

		try {
			// in Mac check for if user has changed R version. If so try get 
			// compatible dynamic R lib for new version.
			checkMacR();
			// 1) we pass the arguments from the command line
			// 2) we won't use the main loop at first, we'll start it later
			// (that's the "false" as second argument)
			// 3) the callbacks are implemented by the TextConsole class above
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

	/**
	 * Test if R packages for a module are available and installed
	 * @param pkgName
	 * @throws Exception
	 */
	public static void testPackage(String pkgName) throws Exception {
		logger.writeln("Checking Package - " + pkgName);

		// install a package from a local zip or tar based on OS
		String pkgPath = System.getProperty("user.dir")+
		System.getProperty("file.separator")+
		RConstants.R_PACKAGE_DIR+
		System.getProperty("file.separator");

		// Get list of packages for the module
		String r_ver = TMEV.getSettingForOption("cur_r_ver");
		String pkg = TMEV.getSettingForOption(
				pkgName+"_"+
				getOSbyName()+"_"+
				r_ver+"_"+
				getARCHbyName()
		);
		// array of pkg names
		String pkgs[] = pkg.split(":");

		// check if packages are down-loaded in MEV.home/RPackages
		String pkgFolder = System.getProperty("user.dir") + "/" + RConstants.R_PACKAGE_DIR + "/";
		ArrayList<String> pkgsToDownload = markPkgsToDownload(pkgFolder, pkgs);

		if(pkgsToDownload.size() > 0) {
			// create complete url for each pkg associated with the module
			// based on R version, OS and architecture
			String ver = getMacRversionFromSymLink(RConstants.MAC_R_PATH).trim();
			String os = getOSbyName();
			String arch = getARCHbyName();
			ArrayList<String> pkg_url_list = createPkgUrls(
					RConstants.RHOOK_BASE_URL + "R" + ver + "/" + os,
					ver,
					pkgsToDownload,
					arch,
					os,
					repHash);

			// try downloading the packages to MEV.home/RPackages
			String pkg_dest = System.getProperty("user.dir")+"/"+RConstants.R_PACKAGE_DIR;
			updatePackages(pkg_url_list, pkg_dest);
		}

		// install the pkgs in R if not already installed
		for (int i=0; i < pkgs.length; i++) {

			REXP x = evalR("which(as.character(installed.packages()[,1])=='"+pkgs[i]+"')");
			if(x.asInt() != 0 ) {
				System.out.println(pkgs[i] + " Package Installed");
				logger.writeln(pkgs[i] + " Package Installed");
			}
			else {
				System.out.println(pkgs[i] + " Package NOT Installed");
				System.out.println("**** Attempting to install" + pkgs[i]+ " from local rep *****");
				logger.writeln(pkgName + " Package NOT Installed");
				logger.writeln("**** Attempting to install" + pkgs[i]+ " from local rep *****");

				//re.eval("install.packages('" + pkg.replace("\\", "/") + "', repos=NULL)");
				evalR("install.packages('" + pkgPath.replace("\\", "/")+pkgs[i] + "', repos=NULL)");
			}
		}
	}

	/**
	 * 
	 * @param pkgs
	 * @return
	 */
	private static ArrayList<String> markPkgsToDownload(String lookupfolder, String pkgs[]) {
		ArrayList<String> pkgsToDownload = new ArrayList<String>();

		for (int i=0; i < pkgs.length; i++) {
			File f = new File(lookupfolder+pkgs[i].trim());
			if(!f.exists())
				pkgsToDownload.add(pkgs[i].trim());
		}
		return pkgsToDownload;
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

		}

	}

	public static void endR(){
		re.end();
		System.out.println("ended R session");

	}

	static Rengine re;
	protected static RLogger logger;
	private static Hashtable<String, String> repHash;
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

	/**
	 * 
	 * @param name
	 * @param fm
	 * @param rowNames
	 * @param colNames
	 * @return
	 */
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

	/**
	 * 
	 * @throws Exception
	 */
	private static void cleanUp() throws Exception {
		//re.eval("rm(list = ls())");		
		evalR("rm(list = ls())");
		//logger.stop();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public static void endRSession() throws Exception {
		cleanUp();
		// TODO unload packages
		logger.stop();
	}

	/**
	 * 
	 * @param rObj
	 * @param filePath
	 * @param rowNames
	 * @param colNames
	 * @throws Exception
	 */
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

	/**
	 * 
	 * @param e
	 */
	public static void log(Exception e) {
		logger.writeln(e);
	}

	/**
	 * 
	 * @param str
	 */
	public static void log(String str) {
		logger.writeln(str);
	}

	/**
	 * 
	 * @return
	 */
	public static int getOS() {
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

	/**
	 * 
	 * @return
	 */
	public static String getOSbyName() {
		String os = System.getProperty("os.name");
		//String arch = System.getProperty("os.arch");
		//String ver = System.getProperty("os.version");
		if (os.toLowerCase().contains("mac")) {
			return "mac";
		}
		if (os.toLowerCase().contains("linux")) {
			return "linux";
		}
		if (os.toLowerCase().contains("win")) {
			return "win";
		}
		return "unknown";
	}

	/**
	 * 
	 * @return
	 */
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

	/**
	 * 
	 * @return
	 */
	private static String getARCHbyName() {
		//String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		//String ver = System.getProperty("os.version");
		if (arch.toLowerCase().contains("386") || 
				arch.toLowerCase().contains("x86") ||
				arch.toLowerCase().contains("32")) {
			return "32";
		}
		if (arch.toLowerCase().contains("686") || arch.toLowerCase().contains("64")) {
			return "64";
		}
		return "UNKNOWN_ARCH";
	}

	/**
	 * Function to check if R in Mac has been upgraded to newer version
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static boolean Mac_R_ver_Changed() throws Exception {
		// Check if R is installed
		String r_home = System.getenv("R_HOME");
		if(r_home == null || r_home == "") {
			System.err.println("** R_HOME not avaialble or not set properly.");
			throw new Exception("R_HOME not set or available.\n** Possible Causes:\n** MeV launched via WebStart");
		}

		if(!(new File(r_home).exists())) {
			System.err.println("R_HOME dir: " + r_home + " does not exist.");
			throw new Exception("R_HOME dir: " + r_home + "does not exist.\n** Possible Causes:\n** MeV launched via WebStart\n** " + r_home + " location removed");
		}

		// Get current version of R
		String ver = getMacRversionFromSymLink(RConstants.MAC_R_PATH).trim();
		System.out.println("Mac OS X current R version: " + ver);
		// Check if R version has changed since last use by 
		// comparing the version in MeV props.
		String last_used_ver = TMEV.getSettingForOption("cur_r_ver").trim();
		System.out.println("Mac OS X last R version: " + last_used_ver);

		// if version changed, check if lib and package for module is 
		if (ver.equals(last_used_ver)) 
			return false;
		return true;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean updateRDynLib() throws Exception {
		// Get current version of R
		String ver = getMacRversionFromSymLink(RConstants.MAC_R_PATH).trim();
		//System.out.println("Mac OS X current R version: " + ver);
		// Check if R version has changed since last use by 
		// comparing the version in MeV props.
		String last_used_ver = TMEV.getSettingForOption("cur_r_ver").trim();
		//System.out.println("Mac OS X last R version: " + last_used_ver);

		// Get system os and arch
		String os = getOSbyName();
		String arch = getARCHbyName();

		// read R hook repository info from Web
		getPropInfoRhook();

		// parse keys
		ArrayList<String> r_versionList = getValuesFrmHash(repHash, os+"_r_versions");
		ArrayList<String> archList = getValuesFrmHash(repHash, os+"_arch");

		// check if current R version is supported
		if (!r_versionList.contains(ver)) {
			System.out.println("R version: " + ver + " not yet supported");
			throw new Exception("R version: " + ver + " not yet supported");
		}

		// check if architecture is supported
		if (!archList.contains(arch)) {
			System.out.println(os + " " + arch + " bit not yet supported");
			throw new Exception(os + " " + arch + " bit not yet supported");
		}

		// check if lib and pkg available for new version
		String lib_url = RConstants.RHOOK_BASE_URL + "R" + ver + "/" + os + "/libjri.jnilib";

		// update lib and all packages associated with R ver
		// updateLibAndPackages(lib_url, pkg_url_list);
		updateRLib(lib_url);

		// update TMEV properties
		TMEV.storeProperty("cur_r_ver", ver);
		TMEV.storeProperty("prev_r_ver", last_used_ver);
		// storeModuleProperty(ver, r_moduleList, arch, os, repHash);
		return true;

	}

	/**
	 * 
	 * @param libUrl
	 * @throws IOException
	 */
	private static void updateRLib(String libUrl) throws IOException {
		String lib_dest = System.getProperty("user.dir")+"/"+RConstants.MAC_MEV_RES_LOC;

		// get dyn lib
		String fileName = getFileNameFromURL(libUrl);
		System.out.println("updateLibAndPackages remote LIB " + fileName);
		getRemoteFile(libUrl, lib_dest+"/"+fileName);
	}

	/**
	 * 
	 * @param libUrl
	 * @param pkgUrlList
	 * @throws IOException 
	 */
	private static void updatePackages(ArrayList<String> pkgUrlList, String pkg_dest) 
	throws IOException {
		String fileName;

		// get R pkgs
		for(int i=0; i < pkgUrlList.size(); i++) {
			fileName = getFileNameFromURL(pkgUrlList.get(i));
			System.out.println("updateLibAndPackages remote PKG " + fileName);
			getRemoteFile(pkgUrlList.get(i), pkg_dest+"/"+fileName);
		}
	}

	/**
	 * 
	 * @param libUrl
	 * @return
	 */
	private static String getFileNameFromURL(String libUrl) {
		System.out.println("getFileNameFromURL " + libUrl.substring(libUrl.lastIndexOf("/")+1));
		return libUrl.substring(libUrl.lastIndexOf("/")+1);
	}

	/**
	 * 
	 * @param libUrl
	 * @param libDest
	 * @throws IOException
	 */
	private static void getRemoteFile(String libUrl, String libDest) throws IOException {

		String newFName = libDest;//+getFileNameFromURL(libUrl);
		System.out.println("To Download: " + newFName);
		File old = new File(newFName);
		if (old.exists()) {
			// if file already exists re-name it
			String reNameTo = newFName + "_" + TMEV.getSettingForOption("cur_r_ver") + "_" + getDateTime();
			if (!old.renameTo(new File(reNameTo)))
				throw new IOException(newFName + " could not be renamed");
			System.out.println("getRemoteFile: Renamed to " + reNameTo);
		}

		URL url = new URL(libUrl);
		InputStream uis = url.openConnection().getInputStream();
		OutputStream fos = new FileOutputStream(newFName);
		int bytesRead;
		byte[] buf = new byte[1024];
		while ((bytesRead = uis.read(buf, 0, buf.length)) > 0)
			fos.write(buf, 0, bytesRead);
		fos.close();
		uis.close();
		System.out.println("Downloaded: " + newFName);
		System.out.println("	 >>>>>>>>>>>>>>>>>>>");
	}

	/**
	 * 
	 * @param baseUrl
	 * @param ver
	 * @param rModuleList
	 * @param arch
	 * @param os 
	 * @param repHash
	 * @return
	 */
	private static ArrayList<String> createPkgUrls(String baseUrl,
			String ver, ArrayList<String> rModuleList,
			String arch, String os, Hashtable<String, String> repHash) {

		System.out.println("createPkgUrls: baseUrl " + baseUrl);
		ArrayList<String> result = new ArrayList<String>();
		// module name loop
		for(int i = 0; i < rModuleList.size(); i++) {
			String tmp = rModuleList.get(i) + "_" + os + "_" + ver + "_" + arch;
			result.add(baseUrl + "/" + rModuleList.get(i) + "/" + repHash.get(tmp));
			System.out.println("createPkgUrls " + result.get(result.size()-1));
		}
		return result;
	}

	/**
	 * 
	 * @param ver
	 * @param rModuleList
	 * @param arch
	 * @param os
	 * @param repHash
	 */
	private static void storeModuleProperty(String ver,
			ArrayList<String> rModuleList, String arch, String os,
			Hashtable<String, String> repHash) {
		// module name loop
		for(int i = 0; i < rModuleList.size(); i++) {
			String tmp = rModuleList.get(i) + "_" + os + "_" + ver + "_" + arch;
			TMEV.storeProperty(tmp, repHash.get(tmp));
			System.out.println("TMEV stored prop " + tmp + ":" + repHash.get(tmp));
		}
	}

	/**
	 * 
	 * @param repHash
	 * @param key
	 * @return
	 */
	private static ArrayList<String> getValuesFrmHash(
			Hashtable<String, String> repHash, String key) {
		System.out.println("repHash size " + repHash.size());
		return new ArrayList<String>(Arrays.asList((repHash.get(key).split(RConstants.PROP_DELIM))));
	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static Hashtable<String, String> getPropInfoRhook() throws Exception {
		if (repHash != null)
			return repHash; 

		repHash = new Hashtable<String, String>();

		try {
			URLConnection conn = new URL(RConstants.RHOOK_PROP_URL).openConnection();    		    		

			//add repository property hashes to the vector
			repHash = parseProp(conn.getInputStream());			

			//Cache property results in TMEV props
			/*
			Enumeration keySet = repHash.keys();
			while(keySet.hasMoreElements()) {
				String key = (String)keySet.nextElement();
				TMEV.storeProperty(key, (String)repHash.get(key));
			}
			 */
		} catch (Exception e) {
			System.out.println("Could not retreive Web Repository Info.");
			e.printStackTrace();
			//JOptionPane.showMessageDialog(new Frame(), "An error occurred when retrieving Web Repository Info.\n  Update request cannot be fulfilled.", "Cytoscape Launch Error", JOptionPane.ERROR_MESSAGE);
			//return null;
			throw e;
		}

		//return the vector of repository hashes
		return repHash; 
	}

	/**
	 * For Mac OS X only --
	 * Check for R ver and dyn lib compatibility
	 * If mismatched try upgrading to correct version
	 */
	static void checkMacR() throws Exception {
		if (RHook.getOS() != RConstants.MAC_OS)
			return;
		try {
			if (Mac_R_ver_Changed()) {
				//if (!RHook.checkRDynLib("limma")) {
				if (!RHook.updateRDynLib()) {
					//JOptionPane.showMessageDialog(null, "Error updating R library", "REngine", JOptionPane.ERROR_MESSAGE);
					throw new Exception("Error updating R library"); 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			//JOptionPane.showMessageDialog(null, "Error updating R library\n **" + e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			throw new Exception("Error updating R library\n **" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static Hashtable<String, String> parseProp(InputStream is) throws IOException{
		String os  = getOSbyName();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String [] keyValue;

		Hashtable<String, String> currHash = new Hashtable<String, String>();
		String line;
		//loop through the file to parse into 
		while((line = br.readLine())!= null) {
			//comment line, if any
			if(line.startsWith("#"))
				continue;
			//if(!line.contains(os))
			//continue;
			keyValue = line.split("=");
			//add the current property specific to the os
			System.out.println("URL Config: " + keyValue[0] + "-" + keyValue[1]);
			currHash.put(keyValue[0], keyValue[1]);
		}
		return currHash;
	}

	/**
	 * Determines if a file/dir is symbolic link
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null)
			throw new NullPointerException("File must not be null");
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}

	/**
	 * 
	 * @param curRpath
	 * @return
	 */
	public static String getMacRversionFromSymLink (String curRpath) {
		// Detecting a link
		File file = new File(curRpath);
		try {
			System.out.println("Absolute path : " + file.getAbsoluteFile());
			System.out.println("Canonical path: " + file.getCanonicalFile());
			if (isSymlink(file))
				System.out.println("Path is a link");
			else
				System.out.println("Path is NOT a link");
			String can = file.getCanonicalPath();
			String ver = can.substring(can.lastIndexOf("/")+1);
			//Get R version
			System.out.println("R version: " + ver);
			return ver;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}

		/*
		boolean isSymbolicLink = Attributes.readBasicFileAttributes(file, LinkOption.NOFOLLOW_LINKS).isSymbolicLink();
		// Read symbolic link
		//Path link = ...;
		try {
		    System.out.format("Target of link '%s' is '%s'%n", file, file.readSymbolicLink());
		} catch (IOException x) {
		    System.err.println(x);
		}
		 */
	}

	/**
	 * Sets R library path to R_HOME/library
	 * Vista permission issues forces R to use tmp filder as library path 
	 * resulting to multiple install pf packages.
	 * @throws Exception
	 */
	private void setLibPath() throws Exception {
		String libPath = System.getenv("R_HOME");
		libPath = libPath.replace("\\", "/");
		libPath += "/library";
		String rCmd = ".libPaths('" + libPath + "')";
		System.out.println("libPath cmd " + rCmd);
		RHook.evalR(rCmd);

		rCmd = ".libPaths()";
		REXP rx = RHook.evalR(rCmd);
		System.out.println("Curr libPath " + rx.asStringArray()[0]);
	}

	/**
	 * 
	 * @throws Exception
	 */
	private static void doesExistRlib() throws Exception {
		String lib;
		// Mac OS
		if (getOS() == RConstants.MAC_OS){
			lib = System.getProperty("user.dir") + "/" + 
					"MeV.app/Contents/Resources/Java/" + 
					getLibraryName();
		} 
		// all other os
		else {
			lib = System.getenv("R_HOME") + "/" + getLibraryName();
		}
		
		if(!(new File(lib).exists())) {
			System.err.println("R_HOME dir: " + lib + " does not exist.");
			logger.writeln("R JRI lib: " + lib + "does not exist.");		
			logger.stop();
			throw new Exception("R JRI lib: " + lib + "does not exist.");
		}
	}
	
	/**
	 * 
	 * @param r_home
	 * @throws Exception
	 */
	private static void doesExistRhome(String r_home) throws Exception {
		if(!(new File(r_home).exists())) {
			System.err.println("R_HOME dir: " + r_home + " does not exist.");
			logger.writeln("R_HOME dir: " + r_home + " does not exist.");
			logger.writeln("** Possible Causes:");
			logger.writeln("** MeV launched via WebStart");
			logger.writeln("** " + r_home + " location removed");
			logger.stop();
			throw new Exception("R_HOME dir: " + r_home + "does not exist.\n** Possible Causes:\n** MeV launched via WebStart\n** " + r_home + " location removed");
		}
	}
	/**
	 * 
	 * @param r_home
	 * @throws Exception
	 */
	private static void isRhomeSet(String r_home) throws Exception{
		if(r_home == null || r_home == "") {
			System.err.println("** R_HOME not avaialble or not set properly.");
			logger.writeln("** R_HOME not avaialble or not set properly.");
			logger.writeln("** Possible Causes:");
			logger.writeln("** MeV launched via WebStart");
			//logger.writeln("** MeV launched via WebStart");
			logger.stop();
			throw new Exception("R_HOME not set or available.\n** Possible Causes:\n** MeV launched via WebStart");
		}
	}
	/**
	 * 
	 * @return
	 */
	private static String getLibraryName() {
		int os = getOS();
		switch(os) {
		case RConstants.MAC_OS:
			return "libjri.jnilib";
		case RConstants.WINDOWS_OS:
			return "jri.dll";
		case RConstants.LINUX_OS:
			return "libjri.so";
		default:
			return null;
		}
	}

	/**
	 * Return a unique time stamp
	 * @return
	 */
	private static String getDateTime() {
		Date now = new Date();
		String dateString = now.toString();

		SimpleDateFormat formatDt = new SimpleDateFormat("MMM_dd_yy_HH:mm:ss");
		dateString = formatDt.format(now);
		//System.out.println(" 2. " + dateString);
		return dateString;
	}
}
