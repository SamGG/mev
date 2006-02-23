/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * EaseUpdateManager.java
 *
 * Created on January 19, 2005, 2:53 PM
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HTMLMessageFileChooser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;

/**
 *
 * @author  braisted
 */
public class EASEUpdateManager {
    
    private JFrame frame;
    private Hashtable animalHash;
    private Hashtable plantHash;
    private Vector animalKeys;
    private Vector plantKeys;
    private String currentBaseFileLocation, destLoc;
    private int BUFFERSIZE = 1024;
    private byte [] buffer;
    private Progress progress;
    boolean okStatus = true;
    
    /** Creates a new instance of EaseUpdateManager */
    public EASEUpdateManager(JFrame parent) {
        
        frame = parent;
        currentBaseFileLocation = "ftp://ftp.tigr.org/pub/data/tgi/Resourcerer";
        
        //Initialize the progress bar
        progress = new Progress(parent, "Ease File Update Progress", new Listener());
        
        
        //build the species and arrays lists using the supplied file.
        //these hashes will be used to build the selection dialog.
        try {
            URL url = new java.net.URL(currentBaseFileLocation+"/kingdom_species_cloneset_list.txt");
            URLConnection conn = url.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            int length = 0;
            byte [] buffer = new byte [BUFFERSIZE];
            String text = "";
            
            while( (length = bis.read(buffer, 0, BUFFERSIZE)) > 0 ) {
                if(length == BUFFERSIZE)
                    text += new String(buffer);
                else {
                    byte [] lastArray = new byte[length];
                    System.arraycopy(buffer, 0, lastArray, 0, lastArray.length);
                    text += new String(lastArray);
                }
            }
            
            StringTokenizer stok = new StringTokenizer(text, "\n");
            StringTokenizer stok2;
            animalHash = new Hashtable();
            animalKeys = new Vector();
            plantHash = new Hashtable();
            plantKeys = new Vector();
            String kingdom, org, array;
            while(stok.hasMoreElements()) {
                stok2 = new StringTokenizer((String)(stok.nextElement()), "\t");
                
                if(stok2.countTokens() == 3) {
                    kingdom = (String)stok2.nextToken();
                    org = (String)stok2.nextToken();
                    array = (String)stok2.nextToken();
                    
                    //solve dos2unix problem if it exists
                    array = array.trim();
                    
                    if(kingdom.equals("animal")) {
                        if(animalHash.containsKey(org)) {
                            ((Vector)(animalHash.get(org))).addElement(array);
                        } else {
                            animalKeys.addElement(org);
                            animalHash.put(org, new Vector());
                            ((Vector)(animalHash.get(org))).addElement(array);
                        }
                    } else if(kingdom.equals("plant")) {
                        if(plantHash.containsKey(org)) {
                            ((Vector)(plantHash.get(org))).addElement(array);
                        } else {
                            plantKeys.addElement(org);
                            plantHash.put(org, new Vector());
                            ((Vector)(plantHash.get(org))).addElement(array);
                        }
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "<html>An error occurred when retrieving information on" +
            "available<br>species and clone set files.  Update request cannot be fulfilled.", "EASE Update Error", JOptionPane.ERROR_MESSAGE);
            okStatus = false;
            progress.dispose();
        }
    }
    
    
    /** Launches species/array type selection dialog, starts update process with progress status and completion status
     */
    public void updateFiles() {
        if(okStatus) {
            EASEFileUpdateDialog dialog = new EASEFileUpdateDialog(frame, plantKeys, plantHash, animalKeys, animalHash);
            if(dialog.showModal() == JOptionPane.OK_OPTION) { 
                updateEaseFiles(dialog.getSpeciesName(), dialog.getArrayName());
            }
        }
    }
    
    
    /** Kicks off the thread to update the file system given species and array
     */ 
    private void updateEaseFiles(String species, String array) {
        Thread thread = new Thread(new Runner(species, array));
        thread.start();
    }
    
    
    /** retrievs the File object to receive the file system download
     */
    private File getOutputFileLocation() {
        File file = null;
        
        String msg = "<html><center><h1>EASE Destination Selection</h1></center>";
        msg += "Use this interface to select the location for zip file download and extraction. ";
        msg += "Note that the ease directory will be labeled as \"ease_\" followed by the array name.<br><br>";
        msg += "When running EASE please use the button on the first dialog page to specify this folder as the base file ";
        msg += "system for EASE analysis. ";
        msg += "This will become the default location for EASE annoation information.";
        msg += "</html>";
        
        HTMLMessageFileChooser chooser = new HTMLMessageFileChooser(frame, "EASE File Update Location", msg, TMEV.getFile("data"), true);
        JFileChooser baseChooser = chooser.getFileChooser();
        baseChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSize(550, 550);
        if(chooser.showModal() == JOptionPane.OK_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }
    
    /** Controls the update process by calling for downloads and extractions
     */
    private void getBaseFiles(String species, String array) {
        
        String loc = currentBaseFileLocation + "/" + species +"/"+array+"_EASE.zip";
        boolean pass = true;
        
        File outputFile = getOutputFileLocation();
        if(outputFile == null)
            return;
        
        File baseDir = new File(outputFile.getAbsolutePath()+"/ease_"+array);
        baseDir.mkdir();
        
        outputFile = new File(baseDir.getAbsolutePath()+"/"+array+"_EASE.zip");
        
        progress.setTitle("Download Base Zip File");
        progress.show();
        pass = downloadFile(loc, outputFile);
        if(pass)
            pass = extractZipFile(outputFile);
        
        String classDir = baseDir+"/Data/Class/Implies_and_URL_data.zip";
        String auxFilesURL = currentBaseFileLocation+"/Implies_and_URL_data.zip";
        outputFile = new File(classDir);
        
        progress.setTitle("Download Implies and URL Data File");
        progress.setValue(0);
        if(pass)
            pass = downloadFile(auxFilesURL, outputFile);
        if(pass)
            pass = extractZipFile(outputFile);
        
        progress.dispose();
        
        if(pass)
            JOptionPane.showMessageDialog(frame, "The EASE file system update is complete.", "EASE File System Update", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, "The EASE file system update was terminated due to the reported error.", "EASE File System Update", JOptionPane.ERROR_MESSAGE);
    }

    
    /** Downloads the file at sourceURL to output file (dest), returns true if successful
     */
    private boolean downloadFile(String sourceURL, File dest) {
        BufferedInputStream bis;
        BufferedOutputStream bos;
        
        buffer = new byte [BUFFERSIZE];
        int length = 0;
        
        try {
            
            URL url = new URL(sourceURL);
            
            URLConnection conn = url.openConnection();
            
            conn.connect();
            int overallLength = conn.getContentLength();
            int currentLength = 0;
            progress.setUnits(100);
            progress.setValue(0);
            
            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(dest));
            
            while( (length = bis.read(buffer, 0, BUFFERSIZE)) > 0 ) {
                bos.write(buffer, 0, length);
                currentLength += length;
                progress.setValue((int)((float)100*(float)currentLength/(float)overallLength));
            }
            
            bos.flush();
            bos.close();
            bis.close();
        } catch (Exception ioe) {
            progress.dispose();
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(frame, "<html>An Error occured when downloading "+sourceURL+".<br>The update request cannot be fulfilled.</html>", "EASE Update Download Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /** Extracts the specified zip file, returns true is successful
     */
    private boolean extractZipFile(File outputFile) {
        BufferedInputStream bis;
        BufferedOutputStream bos;
        
        progress.setTitle("Extracting zip file: "+outputFile.getAbsolutePath());
        
        try {
            ZipFile zipFile = new ZipFile(outputFile);
            progress.setUnits(zipFile.size());
            
            Enumeration entries = zipFile.entries();
            File baseDir = outputFile.getParentFile();
            byte [] buffer = new byte [BUFFERSIZE];
            int length = 0;
            int cnt = 0;
            
            while(entries.hasMoreElements()) {
                
                progress.setValue(cnt);
                
                ZipEntry entry = (ZipEntry)entries.nextElement();
                
                if(entry.isDirectory()) {
                    cnt++;
                    continue;
                }
                
                String entryName = entry.getName();
                String entryFolder = (new File(entryName)).getParent();
                File entryDirectory = new File(baseDir.getAbsolutePath()+"/"+entryFolder);

                if(!entryDirectory.exists()) {
                    entryDirectory.mkdirs();
                }

                bos = new BufferedOutputStream(new FileOutputStream(baseDir.getAbsolutePath()+"/"+entry.getName()));
                bis = new BufferedInputStream(zipFile.getInputStream(entry));
                
                while( (length = bis.read(buffer, 0, BUFFERSIZE)) > 0 ) {
                    bos.write(buffer, 0, length);
                }
                
                cnt++;
                bos.flush();
                bos.close();
                bis.close();
            }
        } catch (Exception e) {
            progress.dispose();
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "<html>An Error occured when extracting "+outputFile.getAbsolutePath()+".<br>The update request cannot be fulfilled.</html>", "EASE Update Download Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public static void main(String [] args) {
        EASEUpdateManager manager = new EASEUpdateManager(new JFrame());
        manager.updateFiles();
    }
    
    private class Runner implements Runnable {
        private String species;
        private String array;
        
        public Runner(String species, String array) {
            this.species = species;
            this.array = array;
        }
        
        public void run() {
            getBaseFiles(species, array);
        }        
    }
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class Listener extends DialogListener implements WindowListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {                
                progress.dispose();                
            }
        }
        
        public void windowClosing(WindowEvent e) {
            progress.dispose();
        }        
    }
}
