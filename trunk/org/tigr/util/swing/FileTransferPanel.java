/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FileTransferPanel.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileFilter;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class FileTransferPanel extends JPanel {
    
    private static final String FILE_ADD_CMD     = "file-add";
    private static final String FILE_DEL_CMD     = "file-del";
    private static final String FILE_ALL_ADD_CMD = "file-all-add";
    private static final String FILE_ALL_DEL_CMD = "file-all-del";
    private static final String FILE_CHOOSE      = "file-choose";
    
    private JList availList;
    private JList choosedList;
    private DefaultListModel availModel;
    private DefaultListModel choosedModel;
    
    private JButton addButton;
    private JButton delButton;
    private JButton addAllButton;
    private JButton delAllButton;
    
    private FileFilter[] fileFilters;
    private File directory;
    
    /**
     * Constructs a <code>FileTransferPanel</code> with specified initial
     * directory and set of file filters.
     */
    public FileTransferPanel(String currentDirectory, FileFilter[] fileFilters) {
	setLayout(new GridBagLayout());
	setBorder(new BevelBorder(BevelBorder.RAISED));
	
	this.fileFilters = fileFilters;
	
	Listener listener = new Listener();
	
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = new Insets(5, 5, 5, 5);
	
	// directory panel
	gbc.gridx  = 0;
	gbc.gridy  = 0;
	gbc.weightx = 1.0;
	gbc.gridwidth = 3;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	
	JPanel dirPanel = createDirPanel(currentDirectory);
	add(dirPanel, gbc);
	
	// labels...
	gbc.gridx  = 0;
	gbc.gridy  = 1;
	gbc.weightx = 0.0;
	gbc.gridwidth = 1;
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.WEST;
	
	add(new JLabel("Available:"), gbc);
	
	gbc.gridx  = 2;
	
	add(new JLabel("Chosen:"), gbc);
	
	// lists
	JScrollPane scroller;
	
	gbc.gridx  = 0;
	gbc.gridy  = 2;
	gbc.weightx = 0.5;
	gbc.weighty = 1.0;
	gbc.fill   = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	
	availList = createList(listener);
	availModel = (DefaultListModel)availList.getModel();
	scroller = new JScrollPane(availList);
	scroller.setPreferredSize(new Dimension(100, 100));
	add(scroller, gbc);
	
	gbc.gridx = 2;
	
	choosedList = createList(listener);
	choosedModel = (DefaultListModel)choosedList.getModel();
	scroller = new JScrollPane(choosedList);
	scroller.setPreferredSize(new Dimension(100, 100));
	add(scroller, gbc);
	
	// buttons
	gbc.gridx  = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.fill = GridBagConstraints.NONE;
	
	JPanel btnsPanel = createBtnsPanel(listener);
	add(btnsPanel, gbc);
	
	updateDirectory(new File(currentDirectory));
    }
    
    /**
     * Returns an array of choosed files.
     */
    public File[] getFiles() {
	File[] list = new File[choosedModel.size()];
	choosedModel.copyInto(list);
	return list;
    }
    
    /**
     * Returns number of choosed files.
     */
    public int getFilesNumber() {
	return choosedModel.size();
    }
    
    /**
     * Updates set of avaiable files from the specified directory.
     */
    private void updateDirectory(File directory) {
	// if not a folder
	if (!directory.isDirectory()) {
	    return;
	}
	// if the same folder
	if (this.directory != null && this.directory.equals(directory)) {
	    return;
	}
	this.directory = directory;
	
	availModel.clear();
	choosedModel.clear();
	
	FileSystemView fileSystem = FileSystemView.getFileSystemView();
	File[] list = fileSystem.getFiles(directory, true);
	for (int i=0; i<list.length; i++) {
	    if (accept(list[i])) {
		availModel.addElement(list[i]);
	    }
	}
	updateButtons();
    }
    
    /**
     *  Check if passed file is suitable.
     */
    private boolean accept(File file) {
	if (file.isDirectory()) {
	    return false;
	}
	if (fileFilters == null) {
	    return true;
	}
	for (int i=0; i<fileFilters.length; i++) {
	    if (fileFilters[i].accept(file)) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Adds selected files.
     */
    private void addAction() {
	int[] indices = availList.getSelectedIndices();
	for (int i=0; i<indices.length; i++) {
	    choosedModel.addElement(availModel.elementAt(indices[i]));
	}
	for (int i=0; i<indices.length; i++) {
	    availModel.remove(indices[i]-i);
	}
    }
    
    /**
     * Removes selected files from the choosed files list.
     */
    private void delAction() {
	int[] indices = choosedList.getSelectedIndices();
	for (int i=0; i<indices.length; i++) {
	    availModel.addElement(choosedModel.elementAt(indices[i]));
	}
	for (int i=0; i<indices.length; i++) {
	    choosedModel.remove(indices[i]-i);
	}
    }
    
    /**
     * Adds all avaiable file to choosed files list.
     */
    private void addAllAction() {
	final int SIZE = availModel.size();
	for (int i=0; i<SIZE; i++) {
	    choosedModel.addElement(availModel.elementAt(i));
	}
	availModel.clear();
    }
    
    /**
     * Adds all choosed file from the choosed files list.
     */
    private void delAllAction() {
	final int SIZE = choosedModel.size();
	for (int i=0; i<SIZE; i++) {
	    availModel.addElement(choosedModel.elementAt(i));
	}
	choosedModel.clear();
    }
    
    /**
     * Creates a JList.
     */
    private JList createList(Listener listener) {
	JList list = new JList(new DefaultListModel());
	list.setCellRenderer(new ListRenderer());
	list.addListSelectionListener(listener);
	list.addMouseListener(listener);
	return list;
    }
    
    /**
     * Creates a panel to display current directory.
     */
    private JPanel createDirPanel(String currentDirectory) {
	return new DirectoryPanel(currentDirectory);
    }
    
    /**
     * Creates a panel with 'add', 'del', 'add all' and 'del all' buttons.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
	
	addButton    = createButton("Add"    , FILE_ADD_CMD    , listener);
	delButton    = createButton("Del"    , FILE_DEL_CMD    , listener);
	addAllButton = createButton("Add All", FILE_ALL_ADD_CMD, listener);
	delAllButton = createButton("Del All", FILE_ALL_DEL_CMD, listener);
	
	JPanel panel = new JPanel(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill  = GridBagConstraints.HORIZONTAL;
	gbc.gridx = 0;
	gbc.gridy = 0;
	
	panel.add(addButton, gbc);
	gbc.gridy++;
	panel.add(delButton, gbc);
	gbc.gridy++;
	panel.add(addAllButton, gbc);
	gbc.gridy++;
	panel.add(delAllButton, gbc);
	return panel;
    }
    
    /**
     * Creates a button with specified text and action command.
     */
    private JButton createButton(String text, String command, ActionListener listener) {
	JButton button = new JButton(text);
	button.setEnabled(false);
	button.setActionCommand(command);
	button.addActionListener(listener);
	return button;
    }
    
    /**
     * Updates the dialog buttons states.
     */
    private void updateButtons() {
	if (availModel.isEmpty()) {
	    addButton.setEnabled(false);
	    addAllButton.setEnabled(false);
	} else {
	    addAllButton.setEnabled(true);
	    if (availList.isSelectionEmpty()) {
		addButton.setEnabled(false);
	    } else {
		addButton.setEnabled(true);
	    }
	}
	if (choosedModel.isEmpty()) {
	    delButton.setEnabled(false);
	    delAllButton.setEnabled(false);
	} else {
	    delAllButton.setEnabled(true);
	    if (choosedList.isSelectionEmpty()) {
		delButton.setEnabled(false);
	    } else {
		delButton.setEnabled(true);
	    }
	}
    }
    
    /**
     * The class to listen to mouse, action and list selection events.
     */
    private class Listener extends MouseAdapter implements ActionListener, ListSelectionListener {
	
	public void mouseClicked(MouseEvent e) {
	    if (e.getClickCount() < 2)
		return;
	    Object source = e.getSource();
	    if (source == availList) {
		addAction();
	    } else if (source == choosedList) {
		delAction();
	    }
	    updateButtons();
	}
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals(FILE_ADD_CMD)) {
		addAction();
	    } else if (command.equals(FILE_DEL_CMD)) {
		delAction();
	    } else if (command.equals(FILE_ALL_ADD_CMD)) {
		addAllAction();
	    } else if (command.equals(FILE_ALL_DEL_CMD)) {
		delAllAction();
	    }
	    updateButtons();
	}
	
	public void valueChanged(ListSelectionEvent e) {
	    updateButtons();
	}
    }
    
    /**
     * Class to render a list of files.
     */
    private class ListRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    File file = (File)value;
	    setText(file.getName());
	    return this;
	}
    }
    
    /**
     * The class to display and choose current directory.
     */
    private class DirectoryPanel extends JPanel implements ActionListener {
	
	private JTextField textField;
	
	/**
	 * Constructs a <code>DirectoryPanel</code> with specified current
	 * directory.
	 */
	public DirectoryPanel(String currentDirectory) {
	    setLayout(new GridBagLayout());
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(5, 5, 5, 5);
	    
	    gbc.gridx  = 0;
	    gbc.gridy  = 0;
	    gbc.anchor = GridBagConstraints.WEST;
	    
	    add(new JLabel("Directory:"), gbc);
	    
	    gbc.gridx  = 1;
	    gbc.weightx = 1.0;
	    gbc.fill   = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.CENTER;
	    
	    textField = new JTextField(currentDirectory, 30);
	    textField.setEditable(false);
	    add(textField, gbc);
	    
	    gbc.gridx  = 2;
	    gbc.weightx = 0.0;
	    gbc.anchor = GridBagConstraints.EAST;
	    gbc.fill   = GridBagConstraints.NONE;
	    
	    JButton chooseBtn = new JButton("Choose...");
	    chooseBtn.setActionCommand(FILE_CHOOSE);
	    chooseBtn.addActionListener(this);
	    add(chooseBtn, gbc);
	}
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals(FILE_CHOOSE)) {
		chooseDirectory();
	    }
	}
	
	/**
	 * Lets the user choose a directory.
	 */
	private void chooseDirectory() {
	    JFileChooser filechooser = new JFileChooser();
	    filechooser.setCurrentDirectory(new File(textField.getText()));
	    filechooser.setDialogTitle("Choose a directory");
	    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    
	    int ret = filechooser.showOpenDialog(this);
	    if (ret == JFileChooser.APPROVE_OPTION) {
		File dir = filechooser.getSelectedFile();
		textField.setText(dir.getPath());
		updateDirectory(dir);
	    }
	}
    }
}
