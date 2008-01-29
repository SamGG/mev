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
