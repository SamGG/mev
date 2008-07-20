/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 * Viewer for BN LM Module
 * @author Raktim
 *
 */
public class LMBNViewer extends ViewerAdapter {

	protected static final String SHOW_CYTO_WEBSTART = "webstart-cytoscape-cmd";
	protected JPopupMenu popup;
	private JComponent header;
	private JTextPane  content;
	private JLabel label;

	private Vector<String> networkFiles;

	/**
	 * Main constructor called from IClusterGui Implementation of the module
	 * @param files
	 */
	public LMBNViewer(Vector files) {    
		header  = createHeader();
		content = createContent(files);
		setNetworkFiles(files);
		//System.out.println("Get Network Files Size: " + getNetworkFiles().size());
		//System.out.println("Files 1: " + getNetworkFiles().get(0));
		//System.out.println("Files 2: " + getNetworkFiles().get(1));
		//System.out.println("Files 3: " + getNetworkFiles().get(2));
		//listener = new EvtListener();
		setMaxWidth(content, header);
		createJPopupMenu();
		content.addMouseListener(new EvtListener());
	}

	/**
	 * Unused constructor
	 * @param content
	 * @param header
	 */
	public LMBNViewer(JComponent content, JComponent header){
		//listener = new EvtListener();
		this.content = (JTextPane)content;
		this.header = header;
		setMaxWidth(content, header);
		createJPopupMenu();
	}

	/**
	 * Returns component to be inserted into the framework scroll pane.
	 */
	public JComponent getContentComponent() {
		return content;
	}

	/**
	 * Returns the viewer header.
	 */
	public JComponent getHeaderComponent() {
		return header;
	}

	/**
	 * Returns the viewer popup menu.
	 */
	public JPopupMenu getJPopupMenu() {
		return popup;
	} 
	/**
	 * Creates the viewer header.
	 */
	private JComponent createHeader() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.white);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 10, 0);
		//panel.add(new JLabel("<html><body bgcolor='#FBB917'><font face='serif' color='#000080'><h1>LM/BN Network File<h1></font></body></html>"), gbc);
		return panel;
	}


	/**
	 * Creates the viewer content component.
	 */
	private JTextPane createContent(Vector files) {

		JTextPane area = new JTextPane();
		area.setContentType("text/html");
		area.setEditable(false);
		area.setMargin(new Insets(0, 0, 0, 0));
		Font font = new Font("San-Serif", Font.PLAIN, 10);
		area.setFont(font);

		String text = "<html><body bgcolor='#ECE5B6' ><font face='sanserif' color='#000000'>";
		
		//For the standard 3 files
		text += "<h2>LM/BN Network Files</h2>";
		text += "<ul>";
		text += "<li type='1'><table border='0' bgcolor='#F88017' width='90%'>";
		text += "<tr bgcolor='#C9BE62'>";
			text += "<th>File Description</th>";
			text += "<th>File Location</th>";
		text += "</tr>";
		
		//1. LM File
		if(files.size() >= 1) {
			text += "<tr bgcolor='#FFE87C'>";
				text += "<td>Literature Mining Network File</td>";
				text += "<td>"+files.get(0)+"</td>";
			text += "</tr>";
		}
		
		//2. BN File for Observed Data
		if(files.size() >= 2) {
			text += "<tr bgcolor='#ECD872'>";
				text += "<td>BN File for Observed Data</td>";
				text += "<td>"+files.get(1)+"</td>";
			text += "</tr>";
		}
		
		//3. BN File from bootstrap Data
		if(files.size() >= 3) {
			text += "<tr bgcolor='#FFE87C'>";
				text += "<td>BN File from bootstrap Data with user chosen Conf. Threshold</td>";
				text += "<td>"+files.get(2)+"</td>";
			text += "</tr>";
		}
		
		text += "</table></li>";
		text += "</ul>";
		
		if(files.size() > 3) {
			text += "<h2>BN File from Bootstrap data</h2>";
			text += "<ul>";
			text += "<li type='1'><table border='0' bgcolor='#F88017' width='90%'>";
			text += "<tr bgcolor='#C9BE62'>";
				text += "<th>File Description</th>";
				text += "<th>File Location</th>";
			text += "</tr>";
			for(int i=3; i < files.size(); i++) {
				if(i % 2 == 0) {
				text += "<tr bgcolor='#ECD872'>";
					text += "<td>BN File from bootstrap Data with user chosen Conf. Threshold</td>";
					text += "<td>"+files.get(i)+"</td>";
				text += "</tr>";
				} else {
					text += "<tr bgcolor='#FFE87C'>";
					text += "<td>BN File from bootstrap Data with user chosen Conf. Threshold</td>";
					text += "<td>"+files.get(i)+"</td>";
				text += "</tr>";
				}
			}
			text += "</table></li>";
		}
		
		/*
		//For the standard 3 files
		//1. LM File
		text += "<h2>Literature Mining Network File:</h2><br><br>";
		text += "<b>File Path and Location:     </b>"+files.get(0)+"<br><br>"; 

		//2. BN File for Observed Data
		text += "<h2>BN File for Observed Data</h2><br><br>";
		text += "<b>File Path and Location:     </b>"+files.get(1)+"<br><br>"; 

		//3. BN File from bootstrap Data
		text += "<h2>BN File from bootstrap Data with user chosen Conf. Threshold</h2><br><br>";
		text += "<b>File Path and Location:     </b>"+files.get(2)+"<br><br>"; 
		
		//For networks generated for different confidence thresholds from bootstrap data
		if(files.size() > 3) {
			for(int i=3; i < files.size(); i++) {
				text += "<h2>BN File from Bootstrap data</h2><br><br>";
				text += "<b>File Path and Location:     </b>"+files.get(i)+"<br><br>";
			}
		}
		*/
		text += "</font></body></html>";

		area.setText(text);
		area.setCaretPosition(0);

		label = new JLabel(text);
		return area;
	}

	/**
	 * Synchronize content and header sizes.
	 */
	private void setMaxWidth(JComponent content, JComponent header) {
		int c_width = content.getPreferredSize().width;
		int h_width = header.getPreferredSize().width;
		if (c_width > h_width) {
			header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
		} else {
			content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
		}
	}

	/** Returns a component to be inserted into the scroll pane row header
	 */
	public JComponent getRowHeaderComponent() {
		return null;
	}

	/**
	 * Calls the static funtion to launch webstart
	 */
	private void onWebstartCytoscape() {
		CytoscapeWebstart.onWebstartCytoscape(this.getNetworkFiles());
	}

	/**
	 * Creates a popup menu.
	 */
	private void createJPopupMenu() {
		this.popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Webstart Cytoscape", GUIFactory.getIcon("new16.gif"));
		menuItem.setEnabled(true);
		menuItem.setActionCommand(SHOW_CYTO_WEBSTART);
		menuItem.addActionListener(new EvtListener());
		this.popup.add(menuItem);

	}

	/**
	 * Returns a menu item by specified action command.
	 * @return null, if menu item was not found.
	 */
	protected JMenuItem getJMenuItem(String command) {
		Component[] components = popup.getComponents();
		for (int i=0; i<components.length; i++) {
			if (components[i] instanceof JMenuItem) {
				if (((JMenuItem)components[i]).getActionCommand().equals(command))
					return(JMenuItem)components[i];
			}
		}
		return null;
	}

	/**
	 * Sets menu enabled flag.
	 */
	protected void setEnableMenuItem(String command, boolean enable) {
		JMenuItem item = getJMenuItem(command);
		if (item == null) {
			return;
		}
		item.setEnabled(enable);
	}

	/**
	 * The class to listen to mouse, action.
	 */
	private class EvtListener extends MouseAdapter implements ActionListener {

		/** Responds to press events.
		 */        
		public void mousePressed(MouseEvent me) {
			if(me.isPopupTrigger()) {
				popup.show(content, me.getX(), me.getY());
			}
		}

		/** Responds to mouse released events.
		 */        
		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger()) {
				popup.show(content, me.getX(), me.getY());
			}
		}
		/** Responds to menu events.
		 */        
		public void actionPerformed(ActionEvent ae) {
			if(ae.getActionCommand().equals(SHOW_CYTO_WEBSTART)){
				onWebstartCytoscape();
			}
		}
	}

	public void setNetworkFiles(Vector files) {
		this.networkFiles = files;
	}

	public Vector getNetworkFiles() {
		return this.networkFiles;
	}

	/**
	 * Provides the Expression required to express the state of this object
	 * in a saved file.
	 */
	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new", new Object[]{this.networkFiles});

	}
}
