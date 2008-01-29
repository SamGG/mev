
package org.tigr.microarray.mev.persistence;

import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tigr.microarray.mev.MultipleArrayViewer;


public class BufferedImagePersistenceDelegate extends PersistenceDelegate {
	
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		BufferedImageWrapper biw = (BufferedImageWrapper) oldInstance;
		BufferedImage bi = biw.getBufferedImage();
		try {
			File outputFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "bufferedimage", ".jpg");
	        outputFile.deleteOnExit();
	        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
	        PersistenceObjectFactory.writeBufferedImage(dos, bi);
	        dos.close();
			return new Expression((BufferedImageWrapper) oldInstance, new PersistenceObjectFactory().getClass(), "readBufferedImage",
					new Object[]{outputFile.getName()});
		} catch (IOException ioe){
			System.out.println("Can't write to file to save BufferedImage");
			return null;
		}
	}

	public void initialize(Class type, Object oldInstance, Object newInstance, XMLEncoder encoder){
		;
	}
}










































 