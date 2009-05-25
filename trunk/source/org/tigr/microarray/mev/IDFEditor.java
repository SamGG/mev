package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.tigr.microarray.mev.ExperimentLabelEditor.CellEditor;
import org.tigr.microarray.mev.ExperimentLabelEditor.CellRenderer;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ListOrderDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.sampleannotation.MageIDF;
import org.tigr.microarray.mev.sampleannotation.IDFConstants;

public class IDFEditor extends AlgorithmDialog {

	JTable table;
	DefaultViewerTableModel model;
	CellEditor cellEditor;
	int result = JOptionPane.OK_OPTION;
	JPopupMenu popup;

	JMenuItem mergeRowsPopItem;
	JMenuItem mergeRowsItem;
	JMenuItem delRowsPopItem;
	JMenuItem delRowsItem;
	JCheckBoxMenuItem enableReorderItem;
	JCheckBoxMenuItem enableReorderPopItem;

	boolean allowReordering = false;
	JFrame parent;
	
	/**
	 * IDFEditor is used to display/edit the fields in an IDF file.
	 * It is similar to ExperimentLabelEditor with the exception that 
	 * 1. The "Label Keys" for e.g "Sample Names" in a data file are all in the 
	 * first column (spread over different rows) 
	 * 
	 * @param parent
	 * @param title
	 * @param modal
	 * @param permitReorder
	 * @param idfObject
	 */
	public IDFEditor(JFrame parent, String title, boolean modal,
			boolean permitReorder, IData data) {
		super(parent, "IDF Editor", true);

		this.parent = parent;
		Listener listener = new Listener();
		allowReordering = permitReorder;
	
		model = new DefaultViewerTableModel(data);
		
		table = new JTable(model);
		table.setDefaultRenderer(String.class, new CellRenderer());
		table.setDefaultRenderer(JTextField.class, new CellRenderer());
		table.setDefaultRenderer(Object.class, new CellRenderer());
		table.setRowSelectionAllowed(true);
	   
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	
		/*table.getColumn("Label Key").setMinWidth(130);--commented by Sarita
		for (int i = 1; i < table.getColumnCount(); i++) {
			table.setMinWidth(200);
		}*/
		table.setDefaultEditor(JTextField.class, new DefaultCellEditor(
				new JTextField()));
		table.setRowHeight(25);
	
		JScrollPane pane = new JScrollPane(table);
		pane.setColumnHeaderView(table.getTableHeader());
		table.setPreferredScrollableViewportSize(new Dimension(700, 350));
		ParameterPanel panel = new ParameterPanel("IDF Fields");
		panel.setLayout(new GridBagLayout());
		panel.add(pane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 0, 0));
		JMenuBar menuBar = createMenuBar(listener);
		this.setJMenuBar(menuBar);
		popup = createPopupMenu(listener);
		addContent(panel);
		int w = (this.getToolkit().getScreenSize().width);
		int dataWidth = (130 + 100 * (table.getColumnCount() - 1)) - 50;
		if (w > 100) {
			w = (int) (w * 0.8);
			setSize(Math.min(w, dataWidth), 300);
		} else
			setSize(700, 300);
		table.addMouseListener(listener); // table actions
		setActionListeners(listener); // listens for button hits, method in
		// AlgorithmDialog
	}

	
	 public int showModal() {
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	        show();
	        return result;
	    }
	
	 public String [][] getLableData() {
	        String [][] data = new String [table.getRowCount()][table.getColumnCount()];
	            for(int i = 0; i < data.length; i++) {
	                for(int j = 0; j < data[i].length; j++) {
	                    data[i][j] = (String)(model.getValueAt(i,j));  
	                }
	            }   
	               
	        return data;
	    }
	    
	    public String [][] getLabelDataWithoutKeys() {
	        String [][] data = new String [table.getRowCount()][table.getColumnCount()-1];
	     for(int i = 0; i < data.length; i++) {
	                for(int j = 0; j < data[i].length; j++) {
	                    data[i][j] = (String)(model.getValueAt(i,j+1));  
	                }
	            }   
	        return data;        
	    }
	
	    public String[] getLabelKeys() {
	        String [] keys = new String[table.getRowCount()];
	        for(int i = 0; i < keys.length; i++) {
	            keys[i] = (String)(model.getValueAt(i,0));  //just get the keys
	        }
	        
	        
	        return keys;
	    }
	
	
	public class DefaultViewerTableModel extends AbstractTableModel implements
			java.io.Serializable {
		boolean[] numerical;
		Row[] rows;
		int colToSort = 0;
		boolean ascending = false;
		int rowCount;
		String currKey;
		String[] rowNames;
		String[][] dataObject;
		ArrayList<ArrayList> list=new ArrayList<ArrayList>();
		String[] idfFields;
		// This would indicate the maximum number of columns in the editor. This
		// is required to decide on the number of columns that must be added when user says "insert
		// row"
		int maxColumns = 0;

		JTextField valueObject;
		JLabel label;
		IData data;
		

		/**
		 * This inner class is used to support basic manipulation of the table.
		 * The table helps to support ascending and descending row sorting based
		 * on numerical or alphabetical column contents.
		 * 
		 * @param headerNames
		 *            header names
		 * @param data
		 *            data matrix
		 */
		public DefaultViewerTableModel(IData data) {
			this.data=data;
			Hashtable<String, List<String>> idfHash = this.data.getIDFObject().getIDFHash();
			
			//Get the hash table keys
			Enumeration temp=idfHash.keys();
			// Number of rows will be equal to the number of keys in the hashtable
			rowCount = idfHash.keySet().size();
			rowNames = new String[rowCount];

			/**
			 * Get the data in the idfObject in to a String array. The rows
			 * would be equal to all the fields available in IDFConstants class.
			 * 
			 * 
			 */
			

		while(temp.hasMoreElements()){
			
			// The zeroth column would contain the IDF field names
						
				// Get the value/s associated with that field
			    String key=(String)temp.nextElement();
				ArrayList _temp =new ArrayList();
				
				_temp=(ArrayList)idfHash.get(key);
				_temp.add(0, key);
				
			
				//dataObject 2d String array needs rowCount and ColumnCount, so maxColumn keeps 
				//track of which field has the maximum columns and assigns it to maxColumns
				if (_temp.size() > maxColumns)
					maxColumns = _temp.size();
					list.add(_temp);

			}
			//maxColumns+1 is to accomodate the column containing the Key (for e.g Person Name)
			dataObject = new String[rowCount][maxColumns+1];
			
			for(int i=0; i<rowCount; i++){
				ArrayList l=list.get(i);
				for(int j=0; j<l.size(); j++){
					dataObject[i][j]=(String)l.get(j);
				//  System.out.println(dataObject[i][j]);
				}
		
				l.clear();
			}
			
		
			rows = new Row[rowCount];

			for (int i = 0; i < rows.length; i++) {
				rows[i] = new Row();
				rows[i].index = i;
			}
			valueObject = new JTextField();
			valueObject.setOpaque(true);
			label = new JLabel();
			label.setOpaque(true);
			
		}

		public void initializeRows(int rowCount) {
			rows = new Row[rowCount];
			for (int i = 0; i < rows.length; i++) {
				rows[i] = new Row();
				rows[i].index = i;
			}
		}

		/**
		 * Sets column as numerical for sorting.
		 * 
		 * @param col
		 *            column index
		 * @param numericalBool
		 *            sets as numerical or not numerical
		 */
		public void setNumerical(int col, boolean numericalBool) {
			if (col > -1 && col < numerical.length)
				numerical[col] = numericalBool;
		}

		public int getRowCount() {
			return rows.length;
		}

		public Object getValueAt(int param, int param1) {
			return dataObject[param][param1];
		}

		public void setValueAt(Object value, int row, int col) {
			dataObject[row][col] = (String) value;
			this.fireTableChanged(new TableModelEvent(this, row, row, col));
		}

		// In case of IDF files, columns would be filled with values for the row
		// labels
		// so there are no column names. All the IDF fields are populated in the
		// rows.

		public String getColumnName(int index) {
			return null;
			// return columnNames[index];
		}

		private boolean isNumerical(int col) {
			return numerical[col];
		}

		public void sort(int col) {
			ascending = !ascending;
			colToSort = col;
			Arrays.sort(rows);
			fireTableDataChanged();
		}

		public int getRow(int tableRow) {
			return rows[tableRow].index;
		}

		public boolean isCellEditable(int row, int col) {
			//Sarita: changed from row to col. Should not be able to edit the labels.
			return (col > 0);
		}

		private class Row implements Comparable, java.io.Serializable {
			public int index;
			private String myString, otherString;

			public int compareTo(Object other) {
				if (ascending)
					return compareToOther(other);
				return compareToOther(other) * (-1);
			}

			public int compareToOther(Object other) {
				Row otherRow = (Row) other;
				Object myObject = getValueAt(index, colToSort);
				Object otherObject = getValueAt(otherRow.index, colToSort);
				if (myObject instanceof Comparable) {
					if (isNumerical(colToSort)) { // catch string designation
						// of a number
						if (myObject instanceof String) {
							Float myFloat = new Float((String) myObject);
							Float otherFloat = new Float((String) otherObject);
							return myFloat.compareTo(otherFloat);
						}
					}
					return ((Comparable) myObject).compareTo(otherObject);
				}
				if (myObject instanceof JLabel) {
					myString = ((JLabel) (myObject)).getText();
					otherString = ((JLabel) (otherObject)).getText();
					return myString.compareTo(otherString);
				} else
					return index - otherRow.index;
			}
		}
		
		
		
		
		//Allows user to add missing fields of IDF file.
		public void addNewRow() {
			int fieldCount=0;
			
			boolean flag=false;
			ArrayList rows_to_add=new ArrayList();
			//Get the available keys in the table
			String[]keys=getLabelKeys();
			//Get all fields from IDFConstants
			Field[] fields=IDFConstants.class.getFields();
			
		//Loop through all the fields
			for(int index1=0; index1<fields.length; index1++){
				try{
				String temp=(String)fields[index1].get(new IDFConstants());
				flag=false;
				//System.out.println("Field:"+temp);
				//Loop through all the keys
				for(int index2=0; index2<keys.length; index2++){
					//If current field is also present in the keys, set flag to true 
				//	System.out.println("Key:"+keys[index2]);
					if(temp.equalsIgnoreCase(keys[index2])){
						flag=true;
						
					}
					
				}
				
				if(flag==false){
					//System.out.println("Field to add:"+temp);
						rows_to_add.add(temp);
					
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			if(rows_to_add.isEmpty()){
				JOptionPane.showMessageDialog(null, "Your file has all required IDF Fields", "Information", JOptionPane.INFORMATION_MESSAGE);
			}else{
				String text=
	        		  "<html><body><b><center>The following missing fields will be added</center><b><br>";//<hr size=3>";
						for(int num=0; num<rows_to_add.size(); num++){
							text+=rows_to_add.get(num)+"<br>";
						}
					  text+="<br><br></body></html>";
					  JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE);
				
			}
				
			
			
			//Compare with all available fields in IDFConstants 
			
			
			//set the size of newData accordingly. Insert the field names
			String[][] newData = new String[dataObject.length + rows_to_add.size()][maxColumns];
			for (int i = 0; i < newData.length ; i++) {
				if(i<dataObject.length)
					newData[i] = dataObject[i];
				else{
					newData[i][0]=(String)rows_to_add.get(fieldCount);
					fieldCount++;
									
				}		
			}
			
			
			
			// might have to fill row data with " " ?
			dataObject = newData;
			initializeRows(dataObject.length);
			for (int i = 1; i < maxColumns; i++) {
				dataObject[dataObject.length - 1][i] = "";
			}
			// fireTableDataChanged();
			fireTableRowsInserted(dataObject.length - 1, dataObject.length - 1);
			
		}

		public void addNewRow(String[] newRow) {
			String[][] newData = new String[dataObject.length + 1][maxColumns];
			for (int i = 0; i < newData.length - 1; i++) {
				newData[i] = dataObject[i];
			}
			// might have to fill row data with " " ?
			dataObject = newData;
			initializeRows(dataObject.length);

			// set new data
			dataObject[dataObject.length - 1] = newRow;

			// fireTableDataChanged();
			fireTableRowsInserted(dataObject.length - 1, dataObject.length - 1);
		}

		public void mergeRows(int[] rowsToMerge) {
			// get keys
			Vector keysToMerge = new Vector();
			for (int i = 0; i < rowsToMerge.length; i++) {
				keysToMerge.addElement(dataObject[rowsToMerge[i]][0]);
			}

			// present keys in list to order
			JList keyOrderList = new JList(keysToMerge);
			keyOrderList.setSelectedIndex(0);

			ListOrderDialog dialog = new ListOrderDialog(parent, keyOrderList,
					keysToMerge, "Sample Label Keys", " ");

			if (dialog.showModal() != JOptionPane.OK_OPTION)
				return;

			keysToMerge = dialog.getSortedVector();

			// merge data in order within the new row data
			String[] newRow = new String[table.getColumnCount()];

			for (int i = 0; i < newRow.length; i++) {
				newRow[i] = (String) (getMergedData(keysToMerge, i));
			}
			// append row
			addNewRow(newRow);
		}

		public Object getMergedData(Vector keys, int col) {
			String mergedData = "";
			for (int i = 0; i < keys.size(); i++) {
				if (i == 0)
					mergedData += getDataForKey((String) (keys.elementAt(i)),
							col);
				else
					mergedData += "-"
							+ getDataForKey((String) (keys.elementAt(i)), col);
			}
			return mergedData;
		}

		public Object getDataForKey(String key, int col) {
			for (int i = 0; i < dataObject.length; i++) {
				if (dataObject[i][0].equals(key))
					return dataObject[i][col];
			}
			return " ";
		}

		public void deleteRows(int[] selectedRows) {
			boolean[] delRows = new boolean[dataObject.length];
			int rowsToDelete = 0;

			for (int i = 0; i < selectedRows.length; i++) {
				if (selectedRows[i] != 0) { // protect the primary label
					delRows[selectedRows[i]] = true;
					rowsToDelete++;
				}
			}

			String[][] newData = new String[dataObject.length - rowsToDelete][];
			int newRowCnt = 0;
			for (int i = 0; i < dataObject.length; i++) {
				if (delRows[i] == false) {
					newData[newRowCnt] = dataObject[i];
					newRowCnt++;
				}
			}
			initializeRows(newData.length);
			dataObject = newData;
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return maxColumns;
		}

	}

	public class CellRenderer extends DefaultTableCellRenderer {

		JPanel colorPanel;
		JLabel label;
		JTextField textField;

		public CellRenderer() {
			textField = new JTextField();
			textField.setOpaque(true);
			colorPanel = new JPanel();
		}

		/**
		 * Renders basic data input types JLabel, Color,
		 */
		public Component getTableCellRendererComponent(JTable jTable,
				Object obj, boolean param, boolean param3, int row, int col) {
			if (obj instanceof Color) {
				colorPanel.setBackground((Color) obj);
				return colorPanel;
			} else if (obj instanceof JLabel) {
				label = (JLabel) obj;
				label.setOpaque(true);
				label.setFont(new Font("Arial", Font.PLAIN, 12));
				if (row == 0 || col == 0)
					label.setBackground(Color.lightGray);
				else
					label.setBackground(new Color(225, 225, 225));

				label.setForeground(Color.black);
				label.setHorizontalAlignment(JLabel.CENTER);
				if (table.isRowSelected(row) && row != 0)
					label.setBackground(table.getSelectionBackground());
				return label;
			} else if (obj instanceof JTextField) {
				textField = (JTextField) obj;

				if (row == 0) {
					textField.setEditable(false);
					textField.setBackground(Color.lightGray);
				} else if (col == 0) {
					textField.setEditable(true);
					textField.setBackground(new Color(255, 255, 155));
				} else {
					textField.setEditable(true);
					textField.setBackground(Color.white);
				}
				if (table.isRowSelected(row) && row != 0)
					textField.setBackground(table.getSelectionBackground());

				return textField;

			} else if (obj instanceof String) {
				textField.setText((String) obj);
				if (row == 0) {
					textField.setEditable(false);
					textField.setBackground(Color.lightGray);
				} else if (col == 0) {
					textField.setEditable(true);
					textField.setBackground(new Color(255, 255, 155));
				} else {
					textField.setEditable(true);
					textField.setBackground(Color.white);
				}

				if (table.isRowSelected(row) && row != 0)
					textField.setBackground(table.getSelectionBackground());

				return textField;
			}
			colorPanel.setBackground(Color.white);
			return colorPanel;
		}
	}

	public class CellEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {

		JTextField field;
		String value;

		public CellEditor() {
			field = new JTextField();
			field.setBackground(new Color(222, 222, 222));
		}

		public Object getCellEditorValue() {
			return field;
		}

		public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
			value = field.getText();
			this.fireEditingStopped();
		}

		public java.awt.Component getTableCellEditorComponent(
				javax.swing.JTable jTable, Object obj, boolean selected,
				int row, int col) {
			field = (JTextField) obj;
			return (JTextField) obj;
		}

	}

	private JMenuBar createMenuBar(Listener listener) {
		JMenuBar bar = new JMenuBar();
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');

		JMenuItem addRowItem = new JMenuItem("Add Missing IDF Fields");
		addRowItem.setActionCommand("add-row-command");
		addRowItem.addActionListener(listener);

		
		mergeRowsItem = new JMenuItem("Merge Selected Rows");
		mergeRowsItem.setActionCommand("merge-rows-command");
		mergeRowsItem.addActionListener(listener);

		delRowsItem = new JMenuItem("Delete Selected Rows");
		delRowsItem.setActionCommand("del-rows-command");
		delRowsItem.addActionListener(listener);

		enableReorderItem = new JCheckBoxMenuItem("Enable Sample Reordering",
				false);
		enableReorderItem.setActionCommand("reorder-command");
		enableReorderItem.setEnabled(allowReordering);
		enableReorderItem
				.setToolTipText("forces samples to reorder to match table, hit info button");
		enableReorderItem.addActionListener(listener);
		
	
		editMenu.add(addRowItem);
		editMenu.add(mergeRowsItem);
		editMenu.add(delRowsItem);
		editMenu.add(new javax.swing.JSeparator());
		editMenu.add(enableReorderItem);

		bar.add(editMenu);

		return bar;
	}

	private JPopupMenu createPopupMenu(Listener listener) {
		JPopupMenu popup = new JPopupMenu();
		
		JMenuItem addIDFFields = new JMenuItem("Add All IDF Fields");
		addIDFFields.setActionCommand("add-idf-fields-command");
		addIDFFields.addActionListener(listener);
	
		
		JMenuItem addRowPopItem = new JMenuItem("Add Missing IDF fields");
		addRowPopItem.setActionCommand("add-row-command");
		addRowPopItem.addActionListener(listener);

		/*mergeRowsPopItem = new JMenuItem("Merge Selected Rows");
		mergeRowsPopItem.setActionCommand("merge-rows-command");
		mergeRowsPopItem.addActionListener(listener);*/

		delRowsPopItem = new JMenuItem("Delete Selected Rows");
		delRowsPopItem.setActionCommand("del-rows-command");
		delRowsPopItem.addActionListener(listener);

		enableReorderPopItem = new JCheckBoxMenuItem(
				"Enable Sample Reordering", false);
		enableReorderPopItem.setActionCommand("reorder-command");
		enableReorderPopItem.setEnabled(allowReordering);
		enableReorderPopItem
				.setToolTipText("forces samples to reorder to match table, hit info button");
		enableReorderPopItem.addActionListener(listener);
		
		popup.add(addRowPopItem);
		popup.add(addIDFFields);
		popup.add(mergeRowsPopItem);
		popup.add(delRowsPopItem);
		popup.addSeparator();
		popup.add(enableReorderPopItem);

		return popup;
	}

	private class Listener extends MouseAdapter implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			// menu actions
			if (command.equals("add-row-command")) {
				model.addNewRow();
			} /*else if (command.equals("merge-rows-command")) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length < 2)
					return;
				model.mergeRows(selectedRows);
			}*/ else if (command.equals("del-rows-command")) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length < 1)
					return;
				model.deleteRows(selectedRows);
			} else if (allowReordering && command.equals("reorder-command")) {
				if (ae.getSource() == enableReorderItem)
					enableReorderPopItem.setSelected(enableReorderItem
							.isSelected());
				else
					enableReorderItem.setSelected(enableReorderPopItem
							.isSelected());
			}

			// Dialog level commands
			else if (command.equals("ok-command")) {
				if (table.isEditing()) {
					
					String value = ((JTextField) table.getEditorComponent())
							.getText();
					table.setValueAt(value, table.getEditingRow(), table
							.getEditingColumn());
				}
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if (command.equals("cancel-command")) {
				System.out.println("idfeditor cancel button clicked");
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(IDFEditor.this,
						"Experiment Label Editor");
				result = JOptionPane.CANCEL_OPTION;
				if (hw.getWindowContent()) {
					hw.setSize(550, 600);
					hw.setLocation();
					hw.show();
				} else {
					hw.setVisible(false);
					hw.dispose();
				}
			}

		}

		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				popup.show(table, me.getX(), me.getY());
				return;
			}

			// table.getSelectedRows();
			// table.getSelected

		}

		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				popup.show(table, me.getX(), me.getY());
				return;
			}
			if (table.getSelectedRowCount() > 1) {
				mergeRowsItem.setEnabled(true);
				mergeRowsPopItem.setEnabled(true);
			} else {
				mergeRowsItem.setEnabled(false);
				mergeRowsPopItem.setEnabled(false);
			}
			if (table.getSelectedRowCount() > 0) {
				delRowsItem.setEnabled(true);
				delRowsPopItem.setEnabled(true);
			} else {
				delRowsItem.setEnabled(false);
				delRowsPopItem.setEnabled(false);
			}
		}

	}

}
