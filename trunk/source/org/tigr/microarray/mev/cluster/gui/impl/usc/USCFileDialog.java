/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Sep 27, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.file.FileTreePane;
import org.tigr.microarray.mev.file.FileTreePaneEvent;
import org.tigr.microarray.mev.file.FileTreePaneListener;
import org.tigr.microarray.mev.file.GBA;

/**
 * Just a FileBrowser for user to load their Results File
 * 
 * @author vu
 */
public class USCFileDialog extends AlgorithmDialog {
	//member variables
	private GBA gba;
	private int result;
	
	private FileTreePane fileTreePane;
	private JPanel fileSelectionPanel;
	private JPanel fileListPanel;
	private JLabel fileAvailableLabel;
	private JLabel fileSelectedLabel;
	private JList fileAvailableList;
	private JList fileSelectedList;
	private JScrollPane fileAvailableScrollPane;
	private JScrollPane fileSelectedScrollPane;
	private JButton fileAddButton;
	//private JButton fileAddAllButton;
	private JButton fileRemoveButton;
	//private JButton fileRemoveAllButton;
	private JPanel fileButtonPanel;
	private JPanel selectionPanel;
	private JSplitPane splitPane;
	
	
	public USCFileDialog(Frame parent) {
		super( new JFrame(), "USC:Load Training Data Set", true );
		this.setResizable( true );
		this.setSize( 1000, 750 );
		
		this.gba = new GBA();
		
		this.getContentPane().setLayout( new GridLayout() );
		
		//create the FileTree
		String dataPath = "/" + TMEV.getDataPath();
		//System.out.println("USCFileDialog.dataPath:" + dataPath);
		this.fileTreePane = new FileTreePane( dataPath );
		//this.fileTreePane.openDataPath();
		//this.fileTreePane = new FileTreePane( "C:" + File.separator + "Dev" + File.separator + "MeV" + File.separator + "files" + File.separator + "human" );
		this.fileTreePane.addFileTreePaneListener(new FileTreePaneEventHandler());
		this.fileTreePane.setPreferredSize(new java.awt.Dimension(250, 50));
		
		//create the available and selected List
		this.fileSelectionPanel = new JPanel();
		this.fileSelectionPanel.setLayout( new GridBagLayout() );
		this.fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Training Data Sets"));
		this.fileAvailableLabel = new JLabel("Available files");
		this.fileSelectedLabel = new JLabel("Selected file");
		this.fileAvailableList = new JList(new DefaultListModel());
		this.fileAvailableList.setCellRenderer(new ListRenderer());
		this.fileSelectedList = new JList(new DefaultListModel());
		this.fileSelectedList.setCellRenderer(new ListRenderer());
		this.fileAvailableScrollPane = new JScrollPane(this.fileAvailableList);
		this.fileSelectedScrollPane = new JScrollPane(this.fileSelectedList);
		this.fileAddButton = new JButton("Add");
		this.fileAddButton.addActionListener(new EventHandler());
		//this.fileAddAllButton = new JButton("Add All");
		//this.fileAddAllButton.addActionListener(new EventHandler());
		this.fileRemoveButton = new JButton("Remove");
		this.fileRemoveButton.addActionListener(new EventHandler());
		//this.fileRemoveAllButton = new JButton("Remove All");
		//this.fileRemoveAllButton.addActionListener(new EventHandler());
        
		Dimension largestfileButtonSize = this.fileRemoveButton.getPreferredSize();
		this.fileAddButton.setPreferredSize( largestfileButtonSize );
		//this.fileAddAllButton.setPreferredSize( largestfileButtonSize );
		this.fileRemoveButton.setPreferredSize( largestfileButtonSize );
		//this.fileRemoveAllButton.setPreferredSize( largestfileButtonSize );
        
		this.fileButtonPanel = new JPanel();
		this.fileButtonPanel.setLayout(new GridBagLayout());
        
		gba.add(this.fileButtonPanel, this.fileAddButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		//gba.add(this.fileButtonPanel, this.fileAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(this.fileButtonPanel, this.fileRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		//gba.add(this.fileButtonPanel, this.fileRemoveAllButton, 0, 3, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
		this.fileListPanel = new JPanel();
		this.fileListPanel.setLayout(new GridBagLayout());
        
		gba.add(this.fileListPanel, this.fileAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(this.fileListPanel, this.fileSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(this.fileListPanel, this.fileAvailableScrollPane, 0, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(this.fileListPanel, this.fileButtonPanel, 1, 1, 1, 4, 0, 1, GBA.V, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(this.fileListPanel, this.fileSelectedScrollPane, 2, 1, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
		gba.add(this.fileSelectionPanel, this.fileListPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
     
		//this.fileAddAllButton.setFocusPainted(false);
		this.fileAddButton.setFocusPainted(false);
		//this.fileRemoveAllButton.setFocusPainted(false);
		this.fileRemoveButton.setFocusPainted(false);

		selectionPanel = new JPanel();
		selectionPanel.setLayout(new GridBagLayout());
		gba.add(selectionPanel, this.fileSelectionPanel, 0, 1, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.fileTreePane, selectionPanel);
		
		//listen for actions
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		this.addContent( splitPane );
		this.fileTreePane.openDataPath();
	}//end constructor
	
	
	public File getSelectedFile() {
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		Object[] selFiles = selModel.toArray();
		File f = ( File ) selFiles[ 0 ];
		return f;
	}
	
	
	public boolean fileSelected() {
		boolean toReturn;
		
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		Object[] selFiles = selModel.toArray();
		if( selFiles.length == 1 ) {
			toReturn = true;
		} else {
			toReturn = false;
		}
		
		return toReturn;
	}
	
	
	/**
	 * Returns a Vector of the files that were in the fileSelectedList
	 * @return	Vector of File objects
	 */
	public Vector getSelectedFiles() {
		Vector toReturn = new Vector();
		
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		Object[] selFiles = selModel.toArray();
		for( int i = 0; i < selFiles.length; i ++ ) {
			File f = ( File ) selFiles[ i ];
			toReturn.add( f );
		}
		
		return toReturn;
	}//end getSelectedFiles()
	
    
	/**
	 * Shows the dialog.
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}//end showModal()
	
	
	/**
	 * Handles displaying the correct files in the fileAvailableList
	 * @param filePath			Path of the file selected in the FileTreePane
	 * @param vFileName	Vector of file names contained within filePath
	 */
	public void processFileList( String filePath, Vector vFileName ) {
		DefaultListModel model = (DefaultListModel) this.fileAvailableList.getModel();
		model.clear();
		
		if( vFileName.size() > 0 ) {
			for( int i = 0; i < vFileName.size(); i ++ ) {
				String sFileName = ( String ) vFileName.elementAt( i );
				if( acceptFile( sFileName ) ) {
					File f = new File( sFileName );
					model.addElement( f );
				}
			}
		}
	}//end processFileList()
	
	
	/**
	 * Add the selected file(s) to the fileSelectedList, but don't duplicate
	 * @param fAdd
	 */
	public void addFile( File fAdd ) {
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		Object[] selFiles = selModel.toArray();
		if( selFiles.length == 0 ) {
			selModel.addElement( fAdd );
		}
		/*
		//make sure it's not already there
		boolean found = false;
		Object[] selFiles = selModel.toArray();
		for( int j = 0; j < selFiles.length; j ++ ) {
			File jFile = ( File ) selFiles[ j ];
			if( fAdd.getName().equals( jFile.getName() ) ) {
				found = true;
				break;
			}
		}
			
		if( ! found ) {
			selModel.addElement( fAdd );
		}
		*/
	}//end addFile()
	
	
	public void onAdd() {
		int[] chosenIndices = this.fileAvailableList.getSelectedIndices();
		DefaultListModel availModel = ( DefaultListModel ) this.fileAvailableList.getModel();
		
		for( int i = 0; i < chosenIndices.length; i ++ ) {
			File addFile = ( File ) availModel.getElementAt( chosenIndices[i] );
			
			this.addFile( addFile );
		}
	}//end onAdd()
	
	
	public void onAddAll() {
		DefaultListModel availModel = ( DefaultListModel ) this.fileAvailableList.getModel();
		int kount = availModel.size();
		
		for( int i = 0; i < kount; i ++ ) {
			File addFile = ( File ) availModel.getElementAt( i );
			
			this.addFile( addFile );
		}
	}//end onAddAll()
	
	
	public void onRemove() {
		int[] chosenIndices = this.fileSelectedList.getSelectedIndices();
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		
		//loop backwards to avoid looking off the end while we remove Objects from array
		for( int i = chosenIndices.length - 1; i >= 0; i -- ) {
			selModel.remove( chosenIndices[ i ] );
		}
	}//end onRemove()
	
	
	public void onRemoveAll() {
		DefaultListModel selModel = ( DefaultListModel ) this.fileSelectedList.getModel();
		selModel.removeAllElements();
	}//end onRemoveAll()
	
	
	public boolean acceptFile( String fileName ) {
		if( fileName.toLowerCase().endsWith( ".txt" ) ) {
			return true;
		} else {
			return false;
		}
	}//end acceptFile()
	
        
	private class FileTreePaneEventHandler implements FileTreePaneListener {
		public void nodeSelected(FileTreePaneEvent event) {   
			String filePath = (String) event.getValue("Path");
			Vector fileNames = (Vector) event.getValue("Filenames");
			
			processFileList(filePath, fileNames);
		}
        
		public void nodeCollapsed(FileTreePaneEvent event) {}
		public void nodeExpanded(FileTreePaneEvent event) {}
	}//end FileTreePaneEventHandler class
	
        
	private class ListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			File file = (File) value;
			setText(file.getName());
			return this;
		}
	}//end ListRenderer class

        
	private class EventHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
                
			Object source = event.getSource();
		      
			if (source == fileAddButton) {
				onAdd();
			//} else if (source == fileAddAllButton) {
				//onAddAll();
			} else if (source == fileRemoveButton) {
				onRemove();
			//} else if (source == fileRemoveAllButton) {
				//onRemoveAll();
			}
		}//end actionPerformed()
	}//end Eventhandler class
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				if( fileSelected() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				} else { 
					System.out.println( "No File Selected" );
				}
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				
				HelpWindow hw = new HelpWindow( USCFileDialog.this, "USC Load Result Dialog" );
				result = JOptionPane.CANCEL_OPTION;
				if(hw.getWindowContent()){
					hw.setSize(450,600);
					hw.setLocation();
					hw.show();
					return;
				} else {
					hw.setVisible(false);
					hw.dispose();
					return;
				}
			}
		}
        
		public void itemStateChanged(ItemEvent e) {
			//okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
		}
        
		public void windowClosing(WindowEvent e) {
			System.out.println( "windowClosing()" );
			result = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}//end internal Listener class
	
	
	public static void main( String[] args ) {
		USCFileDialog fd = new USCFileDialog( new Frame() );
		fd.showModal();
	}
}//end class
