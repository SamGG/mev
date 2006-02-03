/*
 * TableDataWriter.java
 *
 * Created on May 21, 2003, 11:44 PM
 */

package org.tigr.microarray.mev.cgh.CGHUtil;

import javax.swing.table.TableModel;
import java.io.*;
import javax.swing.JFileChooser;

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
