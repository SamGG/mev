package org.tigr.microarray.mev.persistence;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.OutputStream;

import org.tigr.microarray.mev.MultipleArrayViewer;

public class MavXMLEncoder extends XMLEncoder {
	private MultipleArrayViewer mav;

	public MavXMLEncoder(OutputStream out) {
		super(out);
		
	}
	public MavXMLEncoder(OutputStream out, MultipleArrayViewer mav) {
		super(out);
		this.mav = mav;
	}

    /**
     * Write an XML representation of the specified object to the output.
     *
     * @param o The object to be written to the stream.
     *
     * @see XMLDecoder#readObject
     */
    public void writeObject(Object o) {
	    if(mav.keepSaving())
	    	try {
	    		super.writeObject(o);
	    	} catch (StackOverflowError sofe) {
	    		;//Just printing this error will kill the saving process.
	    	} catch (Exception e) {
	    		ExceptionListener el = getExceptionListener();
	    		el.exceptionThrown(e);

	    	}
	    else 
	    	close();
    }
}
