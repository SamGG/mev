package org.tigr.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SOFTFileFilter extends FileFilter{
	 public boolean accept(File file) {
			
			String extension = Utils.getExtension(file);
			if (extension != null) {
				if (extension.equals(Utils.soft)) {
					return true;
				}else{
					return false;
				}

			}

			return false;
		   
}
	 public String getDescription() {
			return "GEO Soft files (*.soft)";
		    }
	 
	 
}