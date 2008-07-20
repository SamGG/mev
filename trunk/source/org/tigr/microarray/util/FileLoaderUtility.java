/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.util;

import java.io.File;
import java.util.Vector;



public class FileLoaderUtility {
		protected String fPath;
	    protected String selectedSingleFile;
	    protected Vector selectedFilesVec;
	    
	    
	    
	    public FileLoaderUtility() {
	    	
	    }
	
	public Vector getFileNameList(String directoryPath) {
        if (directoryPath == null) {
            return null;
        }
        FileBrowser fBrowser = new FileBrowser(directoryPath);
        Vector retrievedFileNames = fBrowser.getFileNamesVec();
        return retrievedFileNames;
    }
    
private class FileBrowser {
	
        
        private String workingFullDir;
        private String workingDir;
        private Vector filesVec;
        private File dir;
        private File subDir;
        public char DIRECTORY_DIV;
        public String DIRECTORY_DIV_S;
        
        public FileBrowser(String directory) {
            setDirectory(directory);
            String sep = System.getProperty("file.separator");
            DIRECTORY_DIV = sep.toCharArray()[0];
            DIRECTORY_DIV_S = sep;
        }
        
        public void setDirectory(String directory) {
            
            workingFullDir = directory;        
            dir = new File(directory);
            workingDir = dir.getAbsolutePath();
            
            if (! dir.isDirectory()) {
                workingDir = workingDir.substring(0, workingDir.lastIndexOf(DIRECTORY_DIV));
            } else {
                filterFiles();                
            }            
        }
        
        public void filterFiles() {
            File checkFile;
            String[] available;
            filesVec = new Vector();

            if (dir == null) 
                return;
             
            available = dir.list();
            
            if (workingDir == null) 
                workingDir = dir.getAbsolutePath();

            for (int i = 0; i < available.length; i++) {
                checkFile = new File(dir, available[i]);
            	
                if (checkFile.isFile()) {
                    //filesVec.addElement(workingDir + ((workingDir.endsWith(DIRECTORY_DIV_S) ? "" : DIRECTORY_DIV_S)) + available[i]);
                	filesVec.addElement(checkFile);
                }
            }
        }
        
        
        public Vector getFileNamesVec() {
            return filesVec;
        }
        
        
        public int getFileCounts() {
            return filesVec.size();
        }
        
                
        public String getAbsolutePath() {
            return workingDir;
        }
        
        
        public String creatSubDir(String sub) {            
            subDir = new File(workingFullDir + DIRECTORY_DIV + sub + DIRECTORY_DIV);
            return workingFullDir + DIRECTORY_DIV + sub + DIRECTORY_DIV;            
        }
        
        
        public String getExtension(File f) {            
            String ext = null;            
            String s = f.getName();            
            int i = s.lastIndexOf('.');
            
            if (i > 0 &&  i < s.length() - 1) {   
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }    

}
