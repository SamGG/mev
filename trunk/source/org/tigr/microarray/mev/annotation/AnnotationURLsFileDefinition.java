package org.tigr.microarray.mev.annotation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

public class AnnotationURLsFileDefinition extends ISupportFileDefinition {

	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(getBaseResourceURL("linkout_annotation_urls"));
	}

	@Override
	public String getUniqueName() {
		return "annotation_URLs.txt";
	}

	@Override
	public boolean isValid(File f) {
		return true;
	}

}
