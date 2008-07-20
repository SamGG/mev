/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TableDataWriter.java
 *
 * Created on May 21, 2003, 11:44 PM
 */

package org.tigr.microarray.mev.cgh.CGHUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.table.TableModel;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class TableDataWriter {

    /** Creates a new instance of TableDataWriter */
    public TableDataWriter() {
    }

    public void writeTable(TableModel model){
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            File outputFile = chooser.getSelectedFile();
            String dataToWrite = getTextContents(model);
            writeDataToFile(dataToWrite, outputFile);
        }
    }

    private String getTextContents(TableModel model){
        StringBuffer buffer = new StringBuffer();
        int i;
        for(i = 0; i < model.getColumnCount() - 1; i++){
            buffer.append(model.getColumnName(i) + "\t");
        }
        buffer.append("\n");
        for(i = 0; i < model.getRowCount(); i++){
            for(int j = 0; j < model.getColumnCount() - 1; j++){
                buffer.append(model.getValueAt(i, j) + "\t");
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    private void writeDataToFile(String dataToWrite, File outputFile){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(dataToWrite);
            writer.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

}
