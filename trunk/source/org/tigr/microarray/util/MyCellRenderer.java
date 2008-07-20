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


public class MyCellRenderer extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color whiteColor = new Color(254, 254, 254);
	private Color alternateColor = new Color(237, 243, 254);
	private Color selectedColor = new Color(61, 128, 223);

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean selected, boolean focused,
			int row, int column)
	{
		super.getTableCellRendererComponent(table, value,
				selected, focused, row, column);

//		Set the background color
		Color bg;
		if (!selected)
			bg = (row % 2 == 0 ? alternateColor : whiteColor);
		else
			bg = selectedColor;
		setBackground(bg);

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




