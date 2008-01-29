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