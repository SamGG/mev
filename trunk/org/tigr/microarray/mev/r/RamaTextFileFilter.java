/*
 * Created on Feb 7, 2005
 */
package org.tigr.microarray.mev.r;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author vu
 */
public class RamaTextFileFilter extends FileFilter {

	/**
	 *
	 */

	public boolean accept(File f) {
		if( f.isDirectory() ) {
			return true;
		} else if( f.getPath().toLowerCase().endsWith( ".txt" ) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 */

	public String getDescription() {
		return "Text (.txt) Files";
	}

}
