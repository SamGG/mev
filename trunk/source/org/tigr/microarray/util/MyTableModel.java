/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.util;

import javax.swing.table.DefaultTableModel;


public class MyTableModel extends DefaultTableModel
{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public int getRowCount()
{
return 10;
}

public int getColumnCount()
{
return 3;
}

public String getColumnName(int column)
{
switch (column)
{
case 0:
return "Song Name";
case 1:
return "Time";
default:
return "Artist";
}
}

public Object getValueAt(int row, int column)
{
switch (column)
{
case 0:
return "Fooing In The Wind";
case 1:
return "3:51";
default:
return "Foo Guy";
}
}

public boolean isCellEditable(int row, int column)
{
return false;
}
}
