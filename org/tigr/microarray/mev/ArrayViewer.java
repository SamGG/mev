/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ArrayViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Cursor;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.tigr.util.Query;
import org.tigr.util.StringSplitter;
import org.tigr.util.awt.Viewer;
import org.tigr.util.awt.TransferDialog;
import org.tigr.util.StringSplitter;
import org.tigr.util.swing.FileTransferDialog;
import org.tigr.microarray.util.swing.TAVFileFilter;
import org.tigr.util.swing.TXTFileFilter;
import org.tigr.microarray.util.swing.SlideDataLoader;

import org.tigr.microarray.mev.file.SuperExpressionFileLoader;

abstract public class ArrayViewer extends JPanel {
    
    protected JFrame mainframe;
    protected boolean firstLoad = true;
    protected String currentDataPath;     

    public ArrayViewer(JFrame mainframe) {
        this.mainframe = mainframe;
    }
    
    public JFrame getFrame() {
        return mainframe;
    }
    
    public void setCursor(int cursor) {
        setCursor(Cursor.getPredefinedCursor(cursor));
    }
    
    private File selectFile() {
        return selectFile("Select a datafile to open", true);
    }
    

    
    private File selectFile(String title, boolean multiSelectionEnabled) {
        JFileChooser chooser;
        if(firstLoad)
		chooser = new JFileChooser(System.getProperty("user.dir"));
	  else
		chooser = new JFileChooser(currentDataPath);

        chooser.setDialogTitle(title);
        
        chooser.addChoosableFileFilter(new TXTFileFilter());
        chooser.addChoosableFileFilter(new TAVFileFilter());
        
        if (! multiSelectionEnabled) { // Using Stanford file
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        }
        chooser.setMultiSelectionEnabled(multiSelectionEnabled);
	  if(firstLoad)
	        chooser.setCurrentDirectory(new File("Data"));
        if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		firstLoad = false;
		currentDataPath = file.getParent();
		if(currentDataPath == null)
			currentDataPath = file.getPath();
            return file;
        }
        return null;
    }
    
    public ISlideData loadSlideData(ISlideMetaData slideMetaData) throws Exception {
        File file = selectFile();
        if (file == null) {
            return null;
        }
        SlideDataLoader loader;
        if(this instanceof MultipleArrayViewer)
            loader =  new SlideDataLoader(getFrame(), slideMetaData, file, false);
        else
            loader =  new SlideDataLoader(getFrame(), slideMetaData, file, true);
        if (loader.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        return loader.getData(0);
    }
    
    public ISlideData[] loadDirectory(ISlideMetaData slideMetaData) throws Exception {
        String parent, child, directory; 
	  if(firstLoad){
		parent = System.getProperty("user.dir");
         	child  = "Data";
	      directory = new File(parent, child).getPath();
	  } else {
		directory = currentDataPath;		
	  }   
        return loadSlideDataDirectory(directory, new FileFilter[] {new TAVFileFilter()}, slideMetaData);
    }
    
    private ISlideData[] loadSlideDataDirectory(String directory, FileFilter[] fileFilters, ISlideMetaData slideMetaData) throws Exception {
        FileTransferDialog dialog = new FileTransferDialog(getFrame(), directory, fileFilters);
        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        File[] files = dialog.getFiles();
        if (files.length < 1) {
            return null;
        }
	  if(firstLoad)
		firstLoad = false;
	  currentDataPath = files[0].getParent();
        if(currentDataPath == null)
	     currentDataPath = files[0].getPath();

        SlideDataLoader loader;
        
        if(this instanceof MultipleArrayViewer)
            loader =  new SlideDataLoader(getFrame(), slideMetaData, files, false);
        else
            loader =  new SlideDataLoader(getFrame(), slideMetaData, files, true);
        
        if (loader.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        return loader.getData();
    }
    
    public ISlideData[] loadStanfordFile(String title) throws Exception {
        File file = selectFile(title, false);
       if (file == null) {
            return null;
       }

       SlideDataLoader loader = new SlideDataLoader(getFrame(), null, file, false);
        if (loader.showModal(true) != JOptionPane.OK_OPTION) {
           return null;
        }
        return loader.getData();
    }
    
    abstract public void systemDisable(int state);
    
    abstract public void systemEnable(int state);
    
}
