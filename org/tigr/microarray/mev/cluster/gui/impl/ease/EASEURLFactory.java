/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: EASEURLFactory.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-07-26 21:32:00 $
 * $Author: braisted $
 * $State: Exp $
 */
/*
 * EASEURLFactory.java
 *
 * Created on November 20, 2003, 11:57 AM
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.io.*;
import javax.swing.JOptionPane;
import java.awt.Frame;

import org.tigr.microarray.mev.TMEV;
/**
 *
 * @author  braisted
 */
public class EASEURLFactory {
    
    /** Create a url String based on file name and the tag
     */
    public static String constructURL(String file, String tag){

        try {
            File urlFile = TMEV.getFile("Data/Ease/Data/Class/URL data/"+file+".txt");
            if(!urlFile.exists() || !urlFile.isFile()){
                JOptionPane.showMessageDialog(new Frame(), "The file: "+file+".txt"+"\n"+"does not exist. Files in this directory are used to construct URLs. \n Other files in this directory can be used as a template to construct\nthe required file.", "URL Construction Not Currently Supported for: "+ file, JOptionPane.WARNING_MESSAGE);
                return null;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(urlFile));
            br.readLine();
            String url = br.readLine();
            if(url == null)
                return null;
            int tagIndex = url.lastIndexOf("[*TAG*]");
            if(tagIndex < 0 || tagIndex >= url.length())
                return null;
            url = url.substring(0,tagIndex)+tag;
            return url;
        } catch (IOException ioe){
            return null;
        }
    }
    
}
