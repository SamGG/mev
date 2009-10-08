package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

public class BroadGeneSetList extends ISupportFileDefinition {
	String broadGeneSetRoot;
	public BroadGeneSetList() {
		this.broadGeneSetRoot =  getBaseResourceURL("broad_gene_set_support_file_location");
	}
	
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(broadGeneSetRoot + "?action=list");
	}

	@Override
	public String getUniqueName() {
		return "broad_genesets.txt";
	}

	@Override
	/**
	 * Could really use a validator to tell us if this file is complete or not.
	 */
	public boolean isValid(File f) {
		return true;
	}
	public static ArrayList<String> getFileNames(File f) throws IOException {
		ArrayList<String> names= new ArrayList<String>();
		
		FileReader fr = null;
		BufferedReader buff = null;
		fr = new FileReader(f);
		buff = new BufferedReader(fr);
        boolean eof = false;
        while (!eof) {
            String line = buff.readLine();
            if (line == null) {
            	eof = true;
            } else {
            	//remove whitespace (\W)
            	Pattern p = Pattern.compile("\\s");
            	String[] temp = p.split(line.subSequence(0, line.length()));
            	names.add(temp[0]);
            }
        }
        return names;
	}
}
