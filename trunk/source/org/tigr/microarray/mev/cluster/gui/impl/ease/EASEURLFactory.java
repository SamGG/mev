/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASEURLFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:50 $
 * $Author: caliente $
 * $State: Exp $
 */
/*
 * EASEURLFactory.java
 *
 * Created on November 20, 2003, 11:57 AM
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
/**
 *
 * @author  braisted
 */
public class EASEURLFactory {
    
    /**
	 * Create a url String based on file name and the tag
	 */
    public static String constructURL(String file, String tag){

        try {
        	EASEImpliesAndURLDataFile def = new EASEImpliesAndURLDataFile();
        	File temp = TMEV.getResourceManager().getSupportFile(def, false);
        	
            File urlFile = new File(def.getURLDataLocation(temp), file + ".txt"); //TMEV.getFile("data/ease/Data/Class/URL data/"+file+".txt");
            if(!urlFile.exists() || !urlFile.isFile()){
                JOptionPane.showMessageDialog(new Frame(), "The file: "+file+".txt"+"\n"+"does not exist. Files in this directory are used to construct URLs. \n Other files in this directory can be used as a template to construct\nthe required file.", "URL Construction Not Currently Supported for: "+ file, JOptionPane.WARNING_MESSAGE);
                return null;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(urlFile));
			br.readLine();
			String url = br.readLine();
			if (url == null) return null;
			int tagIndex = url.lastIndexOf("[*TAG*]");
			if (tagIndex < 0 || tagIndex >= url.length()) return null;
			url = url.substring(0, tagIndex) + tag;
			return url;
        } catch (IOException ioe){
            return null;
        } catch (SupportFileAccessError sfae) {
        	return null;
        }
    }
    
}
