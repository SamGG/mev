package org.tigr.microarray.mev.file;

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ExpressionFileLoader.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.TMEV;

public abstract class ExpressionFileLoader extends SlideLoaderProgressBar { // implements Runnable {
    
    protected SuperExpressionFileLoader superLoader;
    protected SlideLoaderProgressBar progress;
    protected boolean stop = false;
    
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
    
    /**
     * Sets the annotation field names in TMEV, appends if names exist
     */
    protected void setTMEVFieldNames(Vector annotNames){
        if(annotNames.size() > 0){
            String [] fieldNames = new String[annotNames.size()];
            for(int i = 0; i < fieldNames.length; i++){
                fieldNames[i] = (String)(annotNames.elementAt(i));
            }
            TMEV.appendFieldNames(fieldNames);
        }
    }
}