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
import org.tigr.util.StringSplitter;

public class BroadGeneSet extends ISupportFileDefinition {
	private String fileName, emailAddress, broadGeneSetRoot;
	private static String errorString = "ERROR:";
	
	public BroadGeneSet(String fileName, String emailAddress) {
		this.fileName = fileName;
		this.emailAddress = emailAddress;
		this.broadGeneSetRoot =  getBaseResourceURL("broad_gene_set_support_file_location");
	}
	
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(broadGeneSetRoot + "?action=fetch&fileName=" + fileName + "&email=" + emailAddress);
	}

	@Override
	public String getUniqueName() {
		return fileName;
	}

	@Override
	public boolean isValid(File f) {
		try {
			FileReader fr = null;
			BufferedReader buff = null;
			fr = new FileReader(f);
			buff = new BufferedReader(fr);
//            StringSplitter st = new StringSplitter((char)0x09);
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) {
                	eof = true;
                } else {
                    if(Pattern.matches(errorString, line.subSequence(0, line.length())))
                    	return false;
                	else
                		return true;
                }
            }
			return true;
		} catch (IOException ioe) {
			return false;
		}
	}
	public boolean fileNeedsUnzipping() {
		return false;
	}
	public boolean isSingleFile() {
		return true;
	}

}
