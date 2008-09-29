/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExpressionFileLoader.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:52:17 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.annotation.IChipAnnotation;

public abstract class ExpressionFileLoader extends SlideLoaderProgressBar { 

	protected SuperExpressionFileLoader superLoader;
	protected SlideLoaderProgressBar progress;
	protected boolean stop = false;

	protected IChipAnnotation chipAnno = null;

	public ExpressionFileLoader(MultipleArrayViewer mav) {
		super(mav.getFrame());

	}

	public ExpressionFileLoader(SuperExpressionFileLoader superLoader) {
		super(superLoader.getFrame());
		this.superLoader = superLoader;
	}

	public IChipAnnotation getChipAnnotation() {
		return chipAnno;
	}

	public abstract ISlideData loadExpressionFile(File f)
			throws IOException;

	public abstract Vector<ISlideData> loadExpressionFiles() throws IOException;

	public FileFilter getFileFilter() {

		FileFilter defaultFileFilter = new FileFilter() {

			public boolean accept(File f) {
				return true;
			}

			public String getDescription() {
				return "Generic Expression Files (*.*)";
			}
		};

		return defaultFileFilter;
	}

	public void setLoadEnabled(boolean state) {
		superLoader.setLoadEnabled(state);
	}

	public abstract boolean checkLoadEnable();

	public abstract JPanel getFileLoaderPanel();

	public abstract String getFilePath();

	public abstract void openDataPath();

	public abstract String getAnnotationFilePath();
	
	public abstract int getDataType();

	/**
	 * Returns number of lines in the specified file.
	 */
	protected int getCountOfLines(File file) throws IOException {
		int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			count++;
		}
		reader.close();
		return count;
	}

	public abstract void setFilePath(String path);

	/**
	 * Make a guess as to which of the data values represents the
	 * upper-leftmost expression value. Select that cell as the default.
	 */
	public Point guessFirstExpressionCell(Vector<Vector<String>> dataVector) {
		int guessCol = 0, guessRow = 0;
		Vector<String> lastRow = dataVector.get(dataVector.size() - 1);
		for (int j = lastRow.size() - 1; j >= 0; j--) {
			String thisEntry = lastRow.get(j);
			try {
				Float temp = new Float(thisEntry);
			} catch (Exception e) {
				guessCol = j + 1;
				break;
			}
		}
	
		for (int i = dataVector.size() - 1; i >= 0; i--) {
			Vector<String> thisRow = dataVector.get(i);
			String thisEntry = thisRow.get(guessCol);
			try {
				Float temp = new Float(thisEntry);
			} catch (Exception e) {
				guessRow = i + 1;
				break;
			}
		}
		return new Point(guessRow, guessCol);
	}

}
