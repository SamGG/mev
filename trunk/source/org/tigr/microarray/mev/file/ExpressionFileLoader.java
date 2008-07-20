/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExpressionFileLoader.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:52:17 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.IChipAnnotation;

public abstract class ExpressionFileLoader extends SlideLoaderProgressBar { // implements Runnable {
    
    protected SuperExpressionFileLoader superLoader;
    protected SlideLoaderProgressBar progress;
    protected boolean stop = false;

    //EH testing chip annotation change
    protected IChipAnnotation chipAnno = null;
    
	public IChipAnnotation getChipAnnotation() {
		return chipAnno;
	}
    
    public ExpressionFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader.getFrame());
        this.superLoader = superLoader;
   //     this.loadingPanel = new LoadingPanel();   
    }
    
    public abstract ISlideData loadExpressionFile(File f) throws IOException;
    
    public abstract Vector loadExpressionFiles() throws IOException;
    
    private Vector loadExpressionFile(Vector fileVector) throws IOException{
        Vector dataVector = new Vector();
        ISlideData slideData;
        
        for (int i = 0; i < fileVector.size(); i++) {
            slideData = loadExpressionFile((File) fileVector.elementAt(i));
            if(slideData == null)
                return null;
            
            dataVector.add(slideData);
        }
        
        return dataVector;
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter defaultFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                return true;
            }
            
            public String getDescription() {
                return "Generic Expression Files (*.*)";
            }
        };
        
        return defaultFileFilter;
    }
    
    public void setLoadEnabled(boolean state) {
        superLoader.setLoadEnabled(state);
    }
    
    public abstract boolean checkLoadEnable();
    public abstract JPanel getFileLoaderPanel();
    public abstract String getFilePath();
    public abstract void openDataPath();    
    
    /**
     * Returns number of lines in the specified file.
     */
    protected int getCountOfLines(File file) throws IOException {
        int count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            count++;
        }
        reader.close();
        return count;
    }
    
    

}
