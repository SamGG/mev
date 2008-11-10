package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

public class BNSupportDataFile extends ISupportFileDefinition {
	
		private static String fileRoot;
		private String species;
		private String array;
		private boolean singleFile = false;
		private boolean unzipFile = true;
		
		public BNSupportDataFile(String species, String array) {
			this.species = species.trim();
			this.array = array.trim();
			fileRoot =  getBaseResourceURL("bn_support_file_location");
		} 
		@Override
		public URL getURL() throws MalformedURLException {
			return new URL(fileRoot + species + BNConstants.SEP + array + "_BN.zip");
		}

		@Override
		public String getUniqueName() {
			return array + "_BN.zip";
		}
		
		public void setIsSingleFile(boolean flg) {
			singleFile = flg;
		}
		
		public boolean isSingleFile() {
			return singleFile;
		}
		
		public void setFileNeedsUnzipping(boolean flg) {
			unzipFile = flg;
		}
		
		public boolean fileNeedsUnzipping() {
			return unzipFile;
		}
		
		@Override
		public boolean isValid(File f) {
			return BNSupportDataFile.isValidFile(f);
		}
		
		public static boolean isValidFile(File f) {
			// TODO Auto-generated method stub
			return true;
		}
		
		public boolean isVersioned() {
			return true;
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
