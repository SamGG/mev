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

public class GeneSigDbGeneSets extends ISupportFileDefinition {
	private String fileName = "genesigdb_genesets.txt", genesigDbGeneSetRoot;
	private static String errorString = "ERROR:";
	
	public GeneSigDbGeneSets() {
		this.genesigDbGeneSetRoot =  getBaseResourceURL("genesigdb_support_file_location");
	}
	
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(genesigDbGeneSetRoot);
	}

	@Override
	public String getUniqueName() {
		return fileName;
	}

	@Override
	public boolean isValid(File f) {
		FileReader fr = null;
		BufferedReader buff = null;	
		try {
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
		} finally {
			try {
			if(fr != null) {
				fr.close();
			}
			if(buff != null) {
				buff.close();
			}
			} catch (IOException ioe) {}
		}
	}
	public boolean fileNeedsUnzipping() {
		return false;
	}
	public boolean isSingleFile() {
		return true;
	}

}
