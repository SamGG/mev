package org.tigr.microarray.mev.annotation;

public class InvalidAnnMappingFileException extends Exception {
	public InvalidAnnMappingFileException(){
		super();
	}
	public InvalidAnnMappingFileException(String message){
		super(message);
	}
	public InvalidAnnMappingFileException(Throwable t){
		super(t);
	}
}
