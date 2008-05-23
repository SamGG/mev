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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class LMBNViewer extends ViewerAdapter {

	protected static final String SHOW_CYTO_WEBSTART = "webstart-cytoscape-cmd";
    protected static final String SHOW_CYTO_GAGGLE = "broadcast-cytoscape-cmd";
    
    protected EvtListener listener;
    protected JPopupMenu popup;
    
	private JComponent header;
    private JTextPane  content;
    
    private JLabel label;
    
    /**
     * Constructs a <code>LEMInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public LMBNViewer(String lmFile, String bnFile) {    
    	listener = new EvtListener();
        header  = createHeader();
        content = createContent(lmFile, bnFile);
        setMaxWidth(content, header);
        this.popup = createJPopupMenu(listener);
    }
        
    public LMBNViewer(JComponent content, JComponent header){
    	listener = new EvtListener();
    	this.content = (JTextPane)content;
    	this.header = header;
    	setMaxWidth(content, header);
    	this.popup = createJPopupMenu(listener);
    }
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
    	//return label;
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' color='#000080'><h1>LM/BN Network File<h1></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextPane createContent(String lmFile, String bnFile) {

    	JTextPane area = new JTextPane();
        area.setContentType("text/html");
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        Font font = new Font("Serif", Font.PLAIN, 10);
        area.setFont(font);
 
        String text = "<html><body><font face=\"sanserif\">";// color='#000080'>";

        text += "<h2>Network Files Created</h2><br><br>";
        text += "<b>LM Network File:     </b>"+lmFile+"<br><br>"; 
        text += "<b>BN Network File:     </b>"+bnFile+"<br>";
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

    public void onWebstartCystoscape() {
    	JOptionPane.showMessageDialog( new JFrame(), "Popup", "Popup", JOptionPane.PLAIN_MESSAGE );
    }
    
    public void onBroadcastToCystoscape() {
    	JOptionPane.showMessageDialog( new JFrame(), "Popup", "Popup", JOptionPane.PLAIN_MESSAGE );
    }
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(EvtListener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    /**
     * Adds menu items to the specified popup menu.
     */
    protected void addMenuItems(JPopupMenu menu, EvtListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Webstart Cytoscape", GUIFactory.getIcon("new16.gif"));
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_CYTO_WEBSTART);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast to Cytoscape", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_CYTO_GAGGLE);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
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
    private class EvtListener extends MouseAdapter implements ActionListener, java.io.Serializable {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals(SHOW_CYTO_WEBSTART)){
                onWebstartCystoscape();
            } else if(command.equals(SHOW_CYTO_GAGGLE)) {
                onBroadcastToCystoscape();
            } 
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            //maybeShowPopup(event);
            if (SwingUtilities.isRightMouseButton(event)) {
            	 maybeShowPopup(event);
            }
            //deselect(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            setEnableMenuItem(SHOW_CYTO_WEBSTART, 0 >= 0);
            setEnableMenuItem(SHOW_CYTO_GAGGLE, 0 >= 0);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
