package org.tigr.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GeneMatrixFileFilter extends FileFilter{
	public boolean accept(File f) {
		  // Accept all directories and all gmt, gmx, or txt files.
	 
		if (f.isDirectory()) {
		    return true;
		}
		
		String extension = Utils.getExtension(f);
		if (extension != null) {
			if (extension.equals(Utils.gmx)) {
				return true;
			}else{
				return false;
			}

		}

		return false;
		
	}

	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Gene Matrix Files (*.gmx)";
	}

	
	
	
	
	
	
	
	
	


}
