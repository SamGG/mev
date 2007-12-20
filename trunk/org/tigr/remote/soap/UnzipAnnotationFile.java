package org.tigr.remote.soap;


import java.io.*;
import java.util.*;
import java.util.zip.*;


public class UnzipAnnotationFile {

	private String directoryName;
	private String fileName;
	private String targetFileName;
	private String unzippedFile;
	
 public UnzipAnnotationFile(String localDirectory, String fileName ) {
	 this.fileName=fileName;
	 this.directoryName=localDirectory;
 }

  public String unZipFiles() {
   Enumeration entries;
   ZipFile zipFile;

    try {
    
//      System.out.println("directory name:"+directoryName);
//      System.out.println("File name:"+fileName);
      String targetFileName=fileName.substring(0,fileName.indexOf('.'));
      zipFile = new ZipFile(directoryName+"/"+fileName);
      entries = zipFile.entries();

      while(entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();
        //System.out.println("zip fileentry name:"+entry.getName());
        if(entry.isDirectory()) {
//          System.err.println("Extracting directory: " + entry.getName());
          File temp= (new File(directoryName+"/"+entry.getName()+".txt"));
          continue;
        }
       
        if(entry.getName().equals(targetFileName)) {
        	       
//        System.err.println("Extracting file: " + entry.getName());
        copyInputStream(zipFile.getInputStream(entry),
           new BufferedOutputStream(new FileOutputStream(directoryName+"/"+targetFileName+".txt")));
        this.unzippedFile=(targetFileName+".txt");
      }
      }

      zipFile.close();
    } catch (IOException ioe) {
      System.err.println("Unhandled exception:");
      ioe.printStackTrace();
      
    }
    return (this.unzippedFile);
  }

  
  public static final void copyInputStream(InputStream in, OutputStream out)
  throws IOException
  {
    byte[] buffer = new byte[1024];
    int len;

    while((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }
  
  
  
  
  
  
}