/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;


/**
* The header renderer. All this does is make the text left aligned.
*/
public class MyHeaderRenderer extends DefaultTableCellRenderer
{
public Component getTableCellRendererComponent(JTable table,
Object value, boolean selected, boolean focused,
int row, int column)
{
super.getTableCellRendererComponent(table, value,
selected, focused, row, column);
setBorder(UIManager.getBorder("TableHeader.cellBorder"));
return this;
}
}