/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class ExpressionFileTableCellRenderer extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color whiteColor = new Color(254, 254, 254);
	private Color alternateColor = new Color(237, 243, 254);
	private Color sampleAnnotationColor = new Color(205, 225, 225);
	private Color colAnnotationColor = new Color(225, 225, 255);
	private Color sampleAnnLabelColor = new Color(255,255,200);
	private int selectedRow=0;
	private int selectedCol=0;

	public void setSelected(int row, int col){
		this.selectedRow = row;
		this.selectedCol = col;
	}
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean selected, boolean focused,
			int thisRow, int thisColumn)
	{
		super.getTableCellRendererComponent(table, value,
				selected, focused, thisRow, thisColumn);

//		Set the background color
		Color bg;
		if (!selected)
			bg = (thisRow % 2 == 0 ? alternateColor : whiteColor);
		else
			bg = sampleAnnotationColor;
		setBackground(bg);

//		Color cells based on whether they will be loaded as annotation or expression data
		if (selectedRow != 0 || selectedCol != 0){
			if(thisColumn < selectedCol && thisRow < selectedRow) {
				setBackground(Color.WHITE);
				if(thisColumn == selectedCol-1)
					setBackground(sampleAnnLabelColor);
			} else if(thisColumn < selectedCol)
				setBackground(colAnnotationColor);
			else if (thisRow < selectedRow)
				setBackground(sampleAnnotationColor);
		}
		
//		Set the foreground to white when selected
		Color fg;
		if (selected)
			fg = Color.white;
		else
			fg = Color.black;
		setForeground(fg);

		
		return this;
	}
}




