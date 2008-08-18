package org.tigr.microarray.mev.persistence;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.InputStream;

import org.tigr.microarray.mev.MultipleArrayViewer;

public class MavXMLDecoder extends XMLDecoder {

	public MavXMLDecoder(InputStream in) {
		super(in);
	}

	public MavXMLDecoder(InputStream in, Object owner) {
		super(in, owner);
	}

	public MavXMLDecoder(InputStream in, Object owner,
			ExceptionListener exceptionListener) {
		super(in, owner, exceptionListener);
	}

	public MavXMLDecoder(InputStream in, Object owner,
			ExceptionListener exceptionListener, ClassLoader cl) {
		super(in, owner, exceptionListener, cl);
	}
    public Object readObject() { 
    	if(((MultipleArrayViewer)getOwner()).keepSaving()) {
    		return super.readObject();
    	} else {
    		close();
    		return null;
    	}
    }
}
