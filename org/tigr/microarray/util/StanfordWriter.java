
package org.tigr.microarray.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.tigr.microarray.web.ExpressionRecord;
import org.tigr.microarray.web.SearchResults;

public class StanfordWriter {
	SearchResults sr;
	public StanfordWriter(SearchResults sr) {
		this.sr = sr;
	}
	//filter by useThis
	//put row index as UID and make UID into AID
	public String getFile () {
		String returnString = "";
		Vector resultsVector = sr.getResults();
		returnString += "UID\tAID\tGenBank\tTC\tGO-term\tStudy_Name";
		int ratioStartIndex = 5;
		int columnNumber = ratioStartIndex;
		Hashtable exptHash = new Hashtable();
		for(int i = 0; i < resultsVector.size(); i++) {
			String exptName = ((ExpressionRecord)resultsVector.get(i)).getExptName();
			if (!exptHash.containsKey(exptName)) {
				exptHash.put(exptName, new Integer(columnNumber));
				returnString += "\t" + exptName;
				columnNumber++;
			}
		}
		returnString += "\n";
		Hashtable stanfordHash = new Hashtable();
		for(int i = 0; i < resultsVector.size(); i++) {
			String UID = ((ExpressionRecord)resultsVector.get(i)).getUID();
			String exptName = ((ExpressionRecord)resultsVector.get(i)).getExptName();
			float logRatio = ((ExpressionRecord)resultsVector.get(i)).getLogRatio();
			int exptColumn = ((Integer)exptHash.get(exptName)).intValue();
			if(!stanfordHash.containsKey(UID)) {
				stanfordHash.put(UID, new String[columnNumber]); 
				((String[])stanfordHash.get(UID))[0] = ((ExpressionRecord)resultsVector.get(i)).getUID();
				((String[])stanfordHash.get(UID))[1] = ((ExpressionRecord)resultsVector.get(i)).getGbNum();
				((String[])stanfordHash.get(UID))[2] = ((ExpressionRecord)resultsVector.get(i)).getTcNum();
				((String[])stanfordHash.get(UID))[3] = ((ExpressionRecord)resultsVector.get(i)).getGoTerm();
				((String[])stanfordHash.get(UID))[4] = ((ExpressionRecord)resultsVector.get(i)).getStudyName();
			}
			if (sr.getRecordStatus(i)) 
				((String[])stanfordHash.get(UID))[exptColumn] = new Float(logRatio).toString();
			else 
				((String[])stanfordHash.get(UID))[exptColumn] = "N/A";
		}
		Enumeration uidList = stanfordHash.keys();
		int j=1;
		while(uidList.hasMoreElements()) {
			String[] temp = (String[])stanfordHash.get(uidList.nextElement());
			returnString += j + "\t";
			for (int i=0; i<temp.length; i++) {
				returnString += temp[i] + "\t";
			}
			returnString += "\n";
			j++;
		}
		return returnString;
	}
}