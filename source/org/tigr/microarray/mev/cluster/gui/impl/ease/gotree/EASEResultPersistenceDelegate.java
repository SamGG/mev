package org.tigr.microarray.mev.cluster.gui.impl.ease.gotree;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.cluster.algorithm.impl.ease.EaseAlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASETableViewer;
import org.tigr.microarray.mev.persistence.EASETableViewerPersistenceDelegate;
import org.tigr.microarray.mev.persistence.PersistenceObjectFactory;

public class EASEResultPersistenceDelegate extends PersistenceDelegate {
    public static PersistenceDelegate getPersistenceDelegate(){
    	return new EASETableViewerPersistenceDelegate();
    }
	@Override
	protected Expression instantiate(Object oldInstance, Encoder encoder) {
		Expression e;
		try {
			EaseAlgorithmData ead = (EaseAlgorithmData) oldInstance;
			
			File outputFile = File.createTempFile("easetabledata", ".txt", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        PrintWriter pw = new PrintWriter(new FileOutputStream(outputFile));
	        Object[][] data = ead.getResultMatrix();
	        
        	if(data != null)
        	for(int i=0; i<data.length; i++) {
        		for(int j=0; j<data[i].length; j++) {
        			pw.print(data[i][j]);
        			if(j == data[i].length-1) {
        				pw.print("\n");
        			} else {
        				pw.print("\t");
        			}
        		}
        	}
        	//Copy all implies files from repository to temp directory for zipping with original. Set that temp directory as 
        	//implies location for new file
        	
        	File impliesDir = new File(ead.getImpliesFileLocation()); //File.createTempFile("implies", ".txt", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
        	String[] impliesFiles = impliesDir.list();
        	for(int i=0; i<impliesFiles.length; i++) {
        		copyFile(new File(impliesDir, impliesFiles[i]), new File(MultipleArrayViewer.CURRENT_TEMP_DIR, impliesFiles[i]));
        	}
        	
	        e = new Expression((EaseAlgorithmData) oldInstance, new PersistenceObjectFactory().getClass(), "makeEASEResult",
		    		new Object[] {outputFile.getName()}
		    	);
		    	pw.close();

		} catch (Exception ioe){
			ioe.printStackTrace();
			System.out.println("Can't write to file to save EASETableViewer");
			return null;
		}
		return e;
	}
    /**
    *
    * @param in
    * @param out
    * @throws IOException
    */
   private void copyFile(File in, File out) throws IOException {
       FileChannel inChannel = new FileInputStream(in).getChannel();
       FileChannel outChannel = new FileOutputStream(out).getChannel();

       try {
           inChannel.transferTo(0, inChannel.size(), outChannel);
       }
       catch (IOException e) {
           throw e;
       }
       finally {
           if (inChannel != null) inChannel.close();
           if (outChannel != null) outChannel.close();
       }
   } 
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
