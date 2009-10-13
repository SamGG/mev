package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.GSEAURLLinkDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.TableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class LeadingEdgeTableViewer extends TableViewer {

	private static final String SAVE_TABLE_COMMAND = "save_table_command";
	private static final String STORE_CLUSTER_COMMAND = "store_cluster_command";
	private static final String LINK_TO_URL_COMMAND = "link-to-url-command";
	private static final String CLEAR_ALL_COMMAND = "clear-all-cmd";
	private static final String SELECT_ALL_COMMAND = "select-all-cmd";

	private DefaultMutableTreeNode gseaRoot;
	private JPopupMenu menu;

	private JMenuItem urlMenuItem;
	int xColumn;

	public LeadingEdgeTableViewer(String[] headerNames, Object[][] data) {
		super(headerNames, data);
		menu = createPopupMenu();

		table.setRowSelectionAllowed(true);

		table.addMouseListener(new Listener());
		if (table.getRowCount() > 0)
			table.getSelectionModel().setSelectionInterval(0, 0);
	}

	public Expression getExpression() {
		return new Expression(this, this.getClass(), "new", new Object[] {
				headerNames, data });
	}

	/**
	 * Creates the context menu
	 * 
	 * @return
	 */
	protected JPopupMenu createPopupMenu() {
		Listener listener = new Listener();
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item;

		item = new JMenuItem("Store Selection as Cluster");
		item.setActionCommand(STORE_CLUSTER_COMMAND);
		item.addActionListener(listener);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Save pValues Table");
		item.setActionCommand(SAVE_TABLE_COMMAND);
		item.addActionListener(listener);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Select all rows...", GUIFactory
				.getIcon("TableViewerResult.gif"));
		item.setActionCommand(SELECT_ALL_COMMAND);
		item.addActionListener(listener);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Clear all selections...", GUIFactory
				.getIcon("TableViewerResult.gif"));
		item.setActionCommand(CLEAR_ALL_COMMAND);
		item.addActionListener(listener);
		menu.add(item);

		menu.addSeparator();

		urlMenuItem = new JMenuItem("Link to URL");
		urlMenuItem.setActionCommand(LINK_TO_URL_COMMAND);
		urlMenuItem.addActionListener(listener);
		menu.add(urlMenuItem);

		return menu;
	}

	public void onSelected(IFramework framework) {
		super.onSelected(framework);
		if (this.gseaRoot == null) {
			try {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) framework
						.getCurrentNode().getParent();
				Object userObject = node.getUserObject();
				if (userObject instanceof String) {
					if (((String) userObject).indexOf("GSEA") != -1) {
						this.gseaRoot = node;
					}
				}
			} catch (Exception e) {
				System.out.println("selection exception");
				e.printStackTrace();
			}
		}
	}

	private void linkToURL2() {
		JFrame frame = (JFrame) (JOptionPane.getFrameForComponent(table));
		// System.out.println("Column selection
		// allowed:"+table.getColumnSelectionAllowed());
		xColumn = table.getSelectedColumn();
		// System.out.println("Selected column is:"+xColumn);
		String colName = table.getColumnName(xColumn);
		String Annotation = (String) table.getValueAt(table.getSelectedRow(),
				xColumn);
		// System.out.println("Annotation:"+Annotation);
		File file = TMEV.getConfigurationFile("annotation_URLs.txt");

		GSEAURLLinkDialog adialog = new GSEAURLLinkDialog(frame, false,
				Annotation, table.getSelectedRow(), colName, file);
		adialog.setVisible(true);

	}

	/**
	 * Handles the storage of selected rows from the table.
	 * 
	 */
	protected void onStoreSelectedRows() {
		int[] tableIndices = table.getSelectedRows();
		if (tableIndices == null || tableIndices.length == 0)
			return;

		// convert to possibly sorted table indices
		for (int i = 0; i < tableIndices.length; i++)
			tableIndices[i] = ((DefaultViewerTableModel) model)
					.getRow(tableIndices[i]);

		JFileChooser chooser = new JFileChooser(TMEV.getFile("/Data"));
		String fileName = "";
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			fileName = file.getName();
			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream(file));
				int rows = tableIndices.length;
				int cols = table.getColumnCount();

				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						pw.print(((String) (table.getValueAt(tableIndices[row],
								col)))
								+ "\t");
					}
					pw.print("\n");
				}
				pw.flush();
				pw.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				javax.swing.JOptionPane.showMessageDialog(this,
						("Error Saving Table to file: " + fileName),
						"Output Error", JOptionPane.WARNING_MESSAGE);
			}

		}

	}

	/**
	 * Saves the ease table to file
	 */
	protected void onSaveTable() {
		JFileChooser chooser = new JFileChooser(TMEV.getFile("/Data"));
		String fileName = "";
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			fileName = file.getName();
			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream(file));
				int rows = table.getRowCount();
				int cols = table.getColumnCount();

				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						pw
								.print(((String) (table.getValueAt(row, col)))
										+ "\t");
					}
					pw.print("\n");
				}
				pw.flush();
				pw.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				javax.swing.JOptionPane.showMessageDialog(this,
						("Error Saving Table to file: " + fileName),
						"Output Error", JOptionPane.WARNING_MESSAGE);
			}

		}
	}

	/**
	 * Handles events
	 */
	protected class Listener extends MouseAdapter implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if (command.equals(LINK_TO_URL_COMMAND)) {
				linkToURL2();
			} else if (command.equals(STORE_CLUSTER_COMMAND)) {
				onStoreSelectedRows();
			} else if (command.equals(SAVE_TABLE_COMMAND)) {
				onSaveTable();
			} else if (command.equals(CLEAR_ALL_COMMAND)) {
				table.clearSelection();
			} else if (command.equals(SELECT_ALL_COMMAND)) {
				table.selectAll();
			}
		}

		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {

				if (table.getSelectedRowCount() != 1) {
					urlMenuItem.setEnabled(false);
				} else {

					urlMenuItem.setEnabled(true);
				}
				menu.show(me.getComponent(), me.getX(), me.getY());
			}
		}

		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				menu.show(me.getComponent(), me.getX(), me.getY());
			}
		}

	}

}
