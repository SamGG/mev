package org.tigr.microarray.util;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import org.tigr.microarray.web.ExpressionRecord;
import org.tigr.microarray.web.SearchParameters;
import org.tigr.microarray.web.SearchResults;

public class MeVWriter {
	private SearchResults sr;
	private SearchParameters sp;
	private Vector resultsVector;
	public MeVWriter(SearchResults sr, SearchParameters sp) {
		this.sr = sr;
		this.sp = sp;
		resultsVector = sr.getResults();
	}
	//allow filtering from filterarray
	public String getComments() {
		String returnString = "";
		returnString += "#created_by:\tTREX PGA website (pga.tigr.org)\n";
	//	returnString += "#version:\t" + info.getVersion() + "\n";
	//	String += "#Name:\t" + outFile.toString());
		java.util.Date today = new java.util.Date();
		returnString += "#create_date:\t" + today.toString() + "\n";
		returnString += "#row_count:\t" + resultsVector.size() + "\n";
		returnString += "#description:\tSearch Page results\n";		
		Enumeration parameters = sp.getParamsHash().keys();
		while(parameters.hasMoreElements()) {
			String thisKey = (String)parameters.nextElement();
			String[] theseparams = (String[])sp.getParamsHash().get(thisKey);
			String thisValue = "";
			for (int i=0; i<theseparams.length; i++ ){
				thisValue += theseparams[i];
				if (i < theseparams.length -1) thisValue += ", ";
			}
			returnString += "#" + thisKey + ":\t" + thisValue + "\n";
		}
		return returnString;
	}
	public String getHeaders() {
	//	return ("UID\tAID\tIA\tIB\tR\tC\tMR\tMC\tSR\tSC\tRatio\tExpt\tGB#\tTC#\tGO\tstudy\tZS\n");
		return ExpressionRecord.getMeVHeader() + "\n";
	}

	public void printData(PrintWriter out) {
		Enumeration e = sr.getOnlyValidRecords();
		int i=0;
		while(e.hasMoreElements()) {
			ExpressionRecord thisRecord = (ExpressionRecord)e.nextElement();
			if(thisRecord.useThis())
				out.println((++i) + "\t" + thisRecord.getMEVRow());			
		}
	}
}