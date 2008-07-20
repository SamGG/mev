/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jan 26, 2006
 */
package org.tigr.microarray.mev.r;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.tigr.microarray.mev.TMEV;

/**
 * @author iVu
 */
public class RTest {
	public static void main( String[] args ) {
		onSave( true );
	}
	
	
    /**
     * 
     * @param gamma1
     * @param gamma2
     * @param genes
     */
    private static void onSave( boolean allOut ) {
        String currentPath = TMEV.getDataPath();
        RamaTextFileFilter textFilter = new RamaTextFileFilter();
        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.addChoosableFileFilter(textFilter);
        if( chooser.showSaveDialog( new JFrame() ) == JFileChooser.APPROVE_OPTION ) {
            File saveFile;

            if( chooser.getFileFilter() == textFilter ) {
                //make sure to add .txt
                String path = chooser.getSelectedFile().getPath();
                System.out.println( "path" + path );
                if( path.toLowerCase().endsWith("txt") ) {
                    //great, already ok
                    saveFile = new File(path);
                } else {
                    //add it
                    String subPath;
                    int period = path.lastIndexOf(".");
                    System.out.println( "period:" + period );
                    if( period != -1 ) {
                        System.out.println("period found");
                        subPath = path.substring(0, period);
                    } else {
                        System.out.println("period not found");
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    System.out.println( "newPath:" + newPath );
                    saveFile = new File(newPath);
                }
            } else {
                saveFile = chooser.getSelectedFile();
            }
            StringBuffer sb = new StringBuffer();
            sb.append( "GeneName" );
            sb.append( Rama.TAB );
            sb.append( "IntensityA" );
            sb.append( Rama.TAB );
            sb.append( "IntensityB" );
            if( allOut ) {
            	sb.append( Rama.TAB );
            	sb.append( "qLow" );
            	sb.append( Rama.TAB );
            	sb.append( "qUp" );
            }
            sb.append( Rama.END_LINE );
            /*
            for( int i = 0; i < genes.length; i++ ) {
            	sb.append( genes[ i ] );
                sb.append( Rama.TAB );
            	sb.append( gamma1[ i ] );
                sb.append( Rama.TAB );
            	sb.append( gamma2[ i ] );
            	if( allOut ) {
            		sb.append( Rama.TAB );
            		sb.append( qLo[ i ] );
            		sb.append( Rama.TAB );
            		sb.append( qUp[ i ] );
            	}
				sb.append( Rama.END_LINE );
            }//i
            */
            writeFile(saveFile, sb.toString());
        } else {
            //System.out.println("User cancelled Gene List Save");
        }
        
        System.exit( 0 );
    }//onSaveGeneList()


    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private static void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }//writeFile()
}
