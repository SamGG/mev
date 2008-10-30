package org.tigr.microarray.mev.resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

public class SelectMultiFilesDialog extends AlgorithmDialog {
	String[] availableFileNames;
	int[] selectedFiles;
	JTable filelisttable;
	JLabel tableLabel, sourceLabel;

	public SelectMultiFilesDialog(JFrame frame, String title, String source, String[] filenames) {
		super(frame, title, true);
		this.availableFileNames = filenames;
		String[][] data = new String[availableFileNames.length][];
		for(int i=0; i<availableFileNames.length; i++) {
			data[i] = new String[1];
			data[i][0] = availableFileNames[i];
		}
		tableLabel = new JLabel("Choose the support files to use:");
		sourceLabel = new JLabel("Source Server: " + source);
		filelisttable = new JTable(new MyTableModel(data));
		
		setActionListeners(new Listener());
		
		JPanel tablePanel = new JPanel(new GridBagLayout());
		tablePanel.setBackground(Color.white);
		tablePanel.add(sourceLabel, 	new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
		tablePanel.add(tableLabel, 		new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
		tablePanel.add(filelisttable, 		new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
		
		Border tableborder = BorderFactory.createLineBorder(Color.black);
		Font font = new Font("Dialog", Font.BOLD, 12);
		tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Select Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
	            
		filelisttable.setBorder(tableborder);
		
		filelisttable.setColumnSelectionAllowed(false);
		filelisttable.setCellSelectionEnabled(false);
		filelisttable.setRowSelectionAllowed(true);
		filelisttable.setDragEnabled(false);
		
		addContent(tablePanel);
		
		this.setSize(570,750);
	}

	public int[] getSelectedFilesIndices() {
		return filelisttable.getSelectedRows();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] test = new String[3];
		test[0] = "filename1";
		test[1] = "filename 2 is really really really really really long.......";
		test[2] = "shortname";
		SelectMultiFilesDialog smfd = new SelectMultiFilesDialog(new JFrame(), "test title", "ftp://occams.dfci.harvard.edu/", test);
		smfd.setVisible(true);
		System.out.println("selected file indices: ");
		int[] selectedRows = smfd.getSelectedFilesIndices();
		for(int i=0; i<selectedRows.length; i++) {
			System.out.println("row " + selectedRows[i] + " file: " + test[selectedRows[i]]);
		}
		System.exit(0);
	}
	protected class MyTableModel extends AbstractTableModel {
		private String[][] data;
		public MyTableModel(String[][] data) {
			this.data = data;
		}
		public int getColumnCount() {
			return data[0].length;
		}

		public int getRowCount() {
			return data.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
	}

	protected class Listener extends DialogListener implements ItemListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals(AlgorithmDialog.OK_COMMAND)) {
				selectedFiles = filelisttable.getSelectedRows();
				SelectMultiFilesDialog.this.dispose();
				SelectMultiFilesDialog.this.disposeDialog();
			} else if(command.equals(AlgorithmDialog.CANCEL_COMMAND)) {
				selectedFiles = new int[0];
				SelectMultiFilesDialog.this.dispose();
				SelectMultiFilesDialog.this.disposeDialog();
			} else if (command.equals(AlgorithmDialog.RESET_COMMAND)) {
				selectedFiles = new int[0];
				filelisttable.clearSelection();
			}
			
		}

		public void itemStateChanged(ItemEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
