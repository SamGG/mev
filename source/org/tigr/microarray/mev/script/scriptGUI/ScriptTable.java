/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptTable.java
 *
 * Created on February 28, 2004, 12:18 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.microarray.mev.script.Script;
import org.tigr.microarray.mev.script.ScriptDocument;
import org.tigr.microarray.mev.script.ScriptManager;

/**
 *
 * @author  braisted
 */
public class ScriptTable extends ViewerAdapter {
    public static final long serialVersionUID = 1000102010301030001L;
    
    private JTable table;
    private Vector data;
    private ScriptDataModel dataModel;
    private ScriptManager manager;
    
    private JPopupMenu menu;
    
    /** Creates a new instance of ScriptTable
     * @param manager ScriptManager object to be used to control events in the table.
     * @param data Vector
     */
    public ScriptTable(ScriptManager manager, Vector data) {       
        this.manager = manager;
        dataModel = new ScriptDataModel(data, constructHeaderNames());
        table = new JTable(dataModel);  
        table.setBackground(Color.white);
        TableListener listener = new TableListener();
        menu = createPopupMenu(listener);
        table.addMouseListener(listener);
        table.setVisible(true);
    }
    
    private Vector constructHeaderNames() {
        Vector names = new Vector();
        names.add("Row");
        names.add("Script ID");
        names.add("File Name");
        names.add("Script Name");
        names.add("Script Description");
        return names;
    }
    
    private JPopupMenu createPopupMenu (TableListener listener) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        item = new JMenuItem("Exectute Script");
        item.setActionCommand("run-script-cmd");
        item.addActionListener(listener);
        menu.add(item);
        
        return menu;
    }
    

    
    /**
     * @param doc  */    
    public void addScriptDoc(ScriptDocument doc) {
        data.add(doc);        
    }
    
    public void update() {
        
    }
    
    /** Returns the content component to MeV for viewing
     */    
    public JComponent getContentComponent() {
        return table;
    }
    
    /** Returns the header component to mev for viewing
     */    
    public JComponent getHeaderComponent() {
        return table.getTableHeader();
    }
    
    public class ScriptDataModel extends AbstractTableModel  implements java.io.Serializable {

        Vector data;
        ScriptDocument currentDoc;
        Vector headerNames;
        
        public ScriptDataModel(Vector data, Vector header){ 
            this.data = data; 
            this.headerNames = header;
        }
        
        public Object getValueAt(int row, int column) {
           currentDoc = ((Script)(data.elementAt(row))).getScriptDocument();
           Object obj = new String(" "); 
           String temp;
           
           switch( column ){
               case 0 : {
                    obj = String.valueOf(row+1);
                    break;
               }
               case 1 : {
                    obj = new String("Script("+(currentDoc.getDocumentID())+")");
                    break;
               }
               case 2 :{
                   temp = currentDoc.getDocumentFileName();
                   if(temp == null)
                       obj = new String("New Script");
                   else
                        obj = temp; 
                   break;
               }
               case 3 : {
                    obj = currentDoc.getDocumentName();
                    break;
               }
               case 4 : {
                    obj = currentDoc.getDescription();
                    break;
               }                   
           }
           return obj;          
        } 
    public String getColumnName(int column) {
        return (String)headerNames.elementAt(column);
    }
    
        public int getColumnCount() {
            return this.headerNames.size();
    }
    
    public int getRowCount() {
        return this.data.size();
    }
    
    }
    
    public class TableListener extends MouseAdapter implements ActionListener  {
        public TableListener() {
        }
        
        public void actionPerformed(java.awt.event.ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("run-script-cmd")) {
                int row = table.getSelectedRow();
                if(row > -1)
                    manager.runScript(row);
            }
        }
        
        public void mousePressed(MouseEvent me) {
            if(me.isPopupTrigger()) {
                menu.show(table, me.getX(), me.getY());
            }
        }
        
        public void mouseClicked(MouseEvent me) {
            
        }
        
        public void mouseReleased(MouseEvent me) {
            if(me.isPopupTrigger()) {
                menu.show(table, me.getX(), me.getY());                
            }
        }
        
    }
    
    
    
}
