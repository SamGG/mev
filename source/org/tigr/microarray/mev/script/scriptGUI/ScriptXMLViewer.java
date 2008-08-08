/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptXMLViewer.java
 *
 * Created on February 28, 2004, 4:37 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.text.BadLocationException;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.microarray.mev.script.ScriptDocument;
import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.event.ScriptDocumentEvent;
import org.tigr.microarray.mev.script.event.ScriptEventListener;
import org.tigr.microarray.mev.script.util.AlgorithmNode;

/** ScriptXMLViewer renders the <CODE>Script</CODE> as a text editor in
 * xml text form.  The viewer is mostly just a viewer but lines with key:value
 * pairs can be edited.
 * @author braisted
 */
public class ScriptXMLViewer extends ViewerAdapter {
    
    /** ScriptDoucment to be rendered
     */    
    private ScriptDocument doc;
    /** ScriptManager to act as a info exchange conduit.
     *
     */    
    private ScriptManager manager;
    
    /** Main viewer component.
     */    
    private JTextArea pane;
    private HighlightPanel highlightPanel;
    /** Panel to display line numbers.
     */    
    private LineNumberPanel numPanel;
    private JPopupMenu popup;
    private Font vFont;
    int numLines;
    int prevNumLines;
    
    private boolean editOnly;
    private File scriptFile;
    private IFramework framework;
    
    
    /** Creates a new instance of ScriptXMLViewer
     * @param manager
     */
    public ScriptXMLViewer(ScriptManager manager) {
        this.manager = manager;
        editOnly = false;
        PaneListener listener = new PaneListener();
        pane = new JTextArea();
        pane.setEditable(false);
        
        pane.setSelectedTextColor(Color.red);
        pane.setSelectionColor(Color.blue);
        
        pane.setOpaque(false);
        //pane.set
        pane.setMargin(new Insets(0,10,5,10));
        vFont = new Font("Monospaced", Font.PLAIN, 12);
        pane.setFont(vFont);
        numPanel = new LineNumberPanel();
        numPanel.addMouseListener(listener);
        
        highlightPanel = new HighlightPanel();
        highlightPanel.setLayout(new GridBagLayout());
        
        highlightPanel.add(pane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        pane.addMouseListener(listener);
        
        popup = createPopupMenu(new MenuListener());
        updateSize();
    }
    
    /** Creates a new ScriptXMLViewer
     * @param Doc ScriptDocument to render
     * @param manager
     */    
    public ScriptXMLViewer(ScriptDocument Doc, ScriptManager manager) {
        doc = Doc;
        this.manager = manager;
        doc.addDocumentListener(new ScriptListener());
        editOnly = false;
        
        pane = new JTextArea();
        pane.setText(doc.toString());
        pane.setEditable(false);
        pane.setLineWrap(false);        
        pane.setSelectionColor(new Color(120,160,210));        
        pane.setOpaque(false);
        pane.setMargin(new Insets(0,0,5,10));
        vFont = new Font("Monospaced", Font.PLAIN, 12);
        pane.setFont(vFont);
        
        numPanel = new LineNumberPanel();
        
        highlightPanel = new HighlightPanel();
        highlightPanel.setLayout(new GridBagLayout());        
        highlightPanel.add(pane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        pane.addMouseListener(new PaneListener());
        popup = createPopupMenu(new MenuListener());
        updateSize();
    }
    
    
    /** Constructs a ScriptXMLViewer from reading the File object
     * @param inputFile  */    
    public ScriptXMLViewer(File inputFile) {
        this.editOnly = true;
        scriptFile = inputFile;
        
        pane = new JTextArea();
        
        String text;        
        try {
            text = readScript(inputFile);
            pane.setText(text);
        } catch (IOException e) {
            pane.setText(" ");
        }
        
        pane.setCaretPosition(0);
        pane.setLineWrap(false);        
        pane.setSelectionColor(new Color(120,160,210));        
        pane.setOpaque(false);
        pane.setMargin(new Insets(0,0,5,10));
        vFont = new Font("Monospaced", Font.PLAIN, 12);
        pane.setFont(vFont);
        
        numPanel = new LineNumberPanel();
        numPanel.setHeight(pane.getLineCount());
        
        highlightPanel = new HighlightPanel();
        highlightPanel.setLayout(new GridBagLayout());        
        highlightPanel.add(pane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        pane.addKeyListener(new PaneEditorListener());
        pane.addMouseListener(new PaneListener());
        popup = createPopupMenu(new MenuListener());
        updateSize();
    }
    
    
    /** Sets the script document to render
     * @param Doc  */    
    public void setDoc(ScriptDocument Doc) {
        doc = Doc;
        doc.addDocumentListener(new ScriptListener());
        updateSize();
    }
    
    /** Handles update during selection for viewing.
     */    
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.doc.updateScript();
        this.numPanel.clearSelection();
        updateSize();
    }
    
    private void updateSize() {
        if(doc == null)
            return;
        pane.setText(doc.toString());
        String text = " ";
        try{
            text = pane.getDocument().getText(0, pane.getDocument().getLength());
        }catch (Exception e){ }
        prevNumLines = numLines;
        numLines = pane.getLineCount();
        numPanel.setHeight(numLines);
        prevNumLines = numLines;
        pane.setCaretPosition(0);
    }
    
    /** Extract the text of the viewer
     * @return  */    
    public String getText() {
        return pane.getText();
    }
    
    /** Calls an update of the viewer such as on document changed.
     */    
    public void update() {
        updateSize();
    }
    
    /** Returns the content component for viewing
     */    
    public JComponent getContentComponent() {
        return highlightPanel;
    }
    
    /** returns the row header component form viewing.
     */    
    public JComponent getRowHeaderComponent() {
        return numPanel;
    }
    
    /** Highlights a particular algorithm range.
     */    
    public void highlightAlgorithmNode(AlgorithmNode node) {        
        String algName = node.getAlgorithmName();
        String algID = String.valueOf(node.getID());
        String dataRef = String.valueOf(node.getDataNodeRef());
        
        int startLine = getAlgMatch(algName, algID, dataRef);
        //highlight line on down
        int lineCount = pane.getLineCount();
        String endString;
        int end, start, endLine = -1;
        for(int i = startLine; i < lineCount; i++) {            
            try {
                start = this.pane.getLineStartOffset(i);
                end = this.pane.getLineEndOffset(i);
                endString = this.pane.getText(start, end-start);
            } catch (BadLocationException ble) { 
                return; 
            }
            
            if(endString.indexOf("/algorithm") != -1) {
                endLine = i+1;
                break;
            }
        }
        if(endLine > startLine) {
            mark(startLine, endLine);
        }
    }
    
    
    private int getAlgMatch(String algName, String algID, String dataRef) {
        int lineCount = this.pane.getLineCount();
        javax.swing.text.Document doc = this.pane.getDocument();
        String line, newLine;
        int keyIndex;
        int start, end;
        StringTokenizer stok;
        Vector indices = new Vector();
        boolean match = false;
        for(int i = 0; i < lineCount; i++) {
            try {
                start = this.pane.getLineStartOffset(i);
                end = this.pane.getLineEndOffset(i);
                line = this.pane.getText(start, end-start);
            } catch (BadLocationException ble) { return -1; }
            match = false;
            if(line.indexOf(algName) != -1 && line.indexOf("algorithm")!=-1){
                match = true;
                keyIndex = line.indexOf("alg_id");
                newLine = line.substring(keyIndex, line.length()-1);
                stok = new StringTokenizer(newLine, "\"");
                if(stok.hasMoreTokens())
                    stok.nextToken();
                if(stok.hasMoreTokens())
                    newLine = stok.nextToken();
                if(newLine.equals(algID))
                    match = true;
                else
                    match = false;
                if(match) {
                    keyIndex = line.indexOf("data_ref");
                    newLine = line.substring(keyIndex, line.length()-1);
                    stok = new StringTokenizer(newLine, "\"");
                    if(stok.hasMoreTokens())
                        stok.nextToken();
                    if(stok.hasMoreTokens())
                        newLine = stok.nextToken();
                    if(newLine.equals(dataRef))
                        match = true;
                    else
                        match = false;
                }
            }
            if(match)
                return i;
        }
        return -1;
    }
    
    private void mark(int start, int end) {
        highlightPanel.setSelection(start, end);
        highlightPanel.repaint();
        if(end>start)
            setEnableMenuItem("Clear Overlay", true);
        else
            setEnableMenuItem("Clear Overlay", false);
    }
    
    private JPopupMenu createPopupMenu(MenuListener listener) {
        JPopupMenu menu = new JPopupMenu();
        if(!editOnly) {
            menu.add(createItem("Edit", "edit-cmd", "Enabled when parameter key/value or algorithm is selected", listener, false));
            menu.addSeparator();
            menu.add(createItem("Clear Overlay", "clear-overlay-cmd", "Clears the algorithm overlay area if present", listener, false));
          //  menu.add(createItem("Mark Algorithm", "mark-algorithm-cmd", "Overlay shading if present", listener, true));
            menu.addSeparator();
        }
        menu.add(createItem("Save Script", "save-cmd", "Save script to file", listener, true));
        return menu;
    }
    
    private JMenuItem createItem(String title, String cmd, String tip, MenuListener listener, boolean enabled) {
        JMenuItem item = new JMenuItem(title);
        item.setEnabled(enabled);
        item.setFocusPainted(false);
        //item.setToolTipText(tip);
        item.setActionCommand(cmd);
        item.addActionListener(listener);
        return item;
    }
    
    private void setEnableMenuItem(String title, boolean enable) {
        int cnt = popup.getComponentCount();
        Object item;
        for(int i = 0; i < cnt; i++) {
            item = popup.getComponent(i);
            if(item instanceof JMenuItem) {
                if( ((JMenuItem)item).getText().equals(title)) {
                    ((JMenuItem)item).setEnabled(enable);
                    break;
                }
            }
        }
    }
    
    
    private String readScript(File file) throws IOException {
        BufferedReader bfr = new BufferedReader( new FileReader(file) );
        String text = new String();
        String line = new String();
        while( (line = bfr.readLine()) != null)
            text += line+"\n";
        return text;
    }
    
    
    private void saveScript() {
        JFileChooser chooser;
        if(scriptFile != null) {
            chooser = new JFileChooser(scriptFile.getPath());
            chooser.setSelectedFile(new File(scriptFile.getName()));
        } else {
            chooser = new JFileChooser(new File(TMEV.getSettingForOption(ScriptManager.CURRENT_SCRIPT_PATH)));
            if(doc.getDocumentName() != null) {
            	chooser.setSelectedFile(new File(doc.getDocumentName()));
            }
        }
        if(chooser.showSaveDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
            try {
                writeScript(chooser.getSelectedFile());
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(pane, "An error occured while saving the script to file", "Save Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    private void writeScript(File file) throws IOException {
        BufferedWriter bfr = new BufferedWriter( new FileWriter(file));
        bfr.write(pane.getText());
        bfr.flush();
        bfr.close();
    }
    
    
    private Hashtable getAlgorithmAttributes(int cursorLoc) {
        String text = pane.getText(); int index;
        text = text.substring(0, cursorLoc);
        cursorLoc = text.lastIndexOf("algorithm");
        Hashtable hash = null;
        if(cursorLoc > -1) {
            text = text.substring(cursorLoc);
            index = text.indexOf(">");
            if(index != -1) {
                text  = text.substring(0, index);
                hash = getAlgorithmAttributes(text);
            }
        }
        return hash;
    }
    
    private Hashtable getAlgorithmAttributes(String text) {
        
        StringTokenizer stok = new StringTokenizer(text, "\"");
        String tok;
        Hashtable hash = new Hashtable();
        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();
            if(tok.indexOf("alg_id")!=-1) {
                hash.put("alg_id", stok.hasMoreTokens() ? stok.nextToken():" ");
            } else if (tok.indexOf("alg_name") != -1) {
                hash.put("alg_name", stok.hasMoreTokens() ? stok.nextToken():" ");
            } else if (tok.indexOf("input_data_ref") != -1) {
                hash.put("input_data_ref", stok.hasMoreTokens() ? stok.nextToken():" ");
            } else if (tok.indexOf("alg_type") != -1) {
                hash.put("alg_type", stok.hasMoreTokens() ? stok.nextToken():" ");
            }
        }
        return hash;
    }
    
    public class PaneEditorListener implements KeyListener {
        
        public void keyPressed(KeyEvent keyEvent) {
            if(keyEvent.getKeyChar() == '\n') {
                numPanel.setHeight(pane.getLineCount());
            }
            
        }
        
        public void keyReleased(KeyEvent keyEvent) {
            if(keyEvent.getKeyChar() == '\n') {
                numPanel.setHeight(pane.getLineCount());
            }
        }
        
        public void keyTyped(KeyEvent keyEvent) {
            if(keyEvent.getKeyChar() == '\n') {
                numPanel.setHeight(pane.getLineCount());
            }
        }
        
    }
    
    
    public class PaneListener extends MouseAdapter {
        int start, end;
        String text;
        
        public void mouseClicked(MouseEvent me) {
            if(me.getModifiers() == MouseEvent.BUTTON1_MASK) {
                if(!editOnly) {
                    numPanel.list.clearSelection();
                    pane.getHighlighter().removeAllHighlights();
                }
            } else if( me.isPopupTrigger() ) {
                start = pane.getSelectionStart();
                end = pane.getSelectionEnd();
                
                if(end > start) {
                    text = pane.getSelectedText();
                    setEnableMenuItem("Edit", (text.indexOf("value") != -1));
                } else {
                    setEnableMenuItem("Edit", false);
                }
                setEnableMenuItem("Clear Overlay", highlightPanel.haveOverlay());
                popup.show(pane, me.getX(), me.getY());
            }
        }
        
        public void mouseReleased(MouseEvent me) {
            if(me.getModifiers() == MouseEvent.BUTTON1_MASK) {
                if(!editOnly) {
                    numPanel.list.clearSelection();
                    pane.getHighlighter().removeAllHighlights();
                }
            } else if( me.isPopupTrigger() ) {
                start = pane.getSelectionStart();
                end = pane.getSelectionEnd();
                
                if(end > start) {
                    text = pane.getSelectedText();
                    setEnableMenuItem("Edit", (text.indexOf("value") != -1));
                } else {
                    setEnableMenuItem("Edit", false);
                }
                setEnableMenuItem("Clear Overlay", highlightPanel.haveOverlay());
                popup.show(pane, me.getX(), me.getY());
            }
        }
        

        public void mousePressed(MouseEvent me) {
            if(me.getModifiers() == MouseEvent.BUTTON1_MASK) {
                if(!editOnly) {
                    numPanel.list.clearSelection();
                    pane.getHighlighter().removeAllHighlights();
                }
            } else if( me.isPopupTrigger() ) {
                start = pane.getSelectionStart();
                end = pane.getSelectionEnd();
                
                if(end > start) {
                    text = pane.getSelectedText();
                    setEnableMenuItem("Edit", (text.indexOf("value") != -1));
                } else {
                    setEnableMenuItem("Edit", false);
                }
                setEnableMenuItem("Clear Overlay", highlightPanel.haveOverlay());
                popup.show(pane, me.getX(), me.getY());
            }
        }
        
    }
    
    private class MenuListener implements ActionListener {
        
        
        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            if(cmd.equals("edit-cmd")) {
                String text = pane.getSelectedText();
                if(text == null)
                    return;
                int start = pane.getSelectionStart();
                
                String newText = new String(text);
                Hashtable attributes  = getAlgorithmAttributes(start);
                String algName = (String)(attributes.get("alg_name"));
                ScriptValueChangeDialog dialog = new ScriptValueChangeDialog(newText, algName, manager);
                if(dialog.showModal() == JOptionPane.OK_OPTION) {
                    newText = dialog.getLine();

                    String value = dialog.getValue();
                    if(value != null) {
                        if(doc.modifyParameter(attributes, dialog.getLine(), dialog.getValue())) {
                            doc.updateScript();
                            pane.setText(doc.toString());
                            pane.setCaretPosition(start);
                        } else {

                        }
                    }
                    try {
                        pane.getHighlighter().addHighlight(start, start+newText.length(), new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(new Color(120,160,210)));
                    } catch (BadLocationException ble) { }
                    
                    pane.select(start, start+newText.length());
                    
                }
            } else if(cmd.equals("clear-overlay-cmd")) {
                mark(-1,-1);
            } else if(cmd.equals("mark-algorithm-cmd")) {
                
            } else if(cmd.equals("save-cmd")) {
                saveScript();
            }
        }
        
    }
    
    
    public class LineNumberPanel extends JPanel {
        
        boolean newHeight = false;
        JList list;
        DefaultListModel model;
        int lineHeight;
        
        public LineNumberPanel() {
            super(new GridBagLayout());
            list = new JList();
            list.setAlignmentX(JList.RIGHT_ALIGNMENT);
            
            model = new DefaultListModel();
            list.setModel(model);
            list.setCellRenderer(new NumberPanelCellRenderer());
            
            list.setForeground(Color.red);
            list.setFont(vFont);
            setFont(vFont);
            list.setBackground(Color.lightGray);
            list.setVisibleRowCount(0);
            list.setVisible(true);
            
            FontMetrics fm = pane.getFontMetrics(pane.getFont());
            list.setFixedCellHeight(fm.getHeight());
            lineHeight = list.getFixedCellHeight();
            setPreferredSize(new Dimension(10,10));
            add(list, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            if(!editOnly) {
                list.addMouseListener(new LineNumberListener());
                this.addMouseListener(new LineNumberListener());
            }
        }
        
        public void setHeight(int h) {
            boolean bigger = false;
            newHeight = true;
            
            if(h > model.size()+1) {
                bigger = true;
                JLabel label;
                newHeight = true;
                for(int i = model.size()+1; i < h; i++) {
                    label = new JLabel(String.valueOf(i));
                    label.setFont(vFont);
                    label.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));
                    label.setHorizontalAlignment(JLabel.RIGHT);
                    label.setHorizontalTextPosition(JLabel.RIGHT);
                    model.addElement(label);
                }
            }
            list.setVisibleRowCount(h);
        }
        
        public void clearSelection() {
            list.clearSelection();
        }
        
        public void paint(Graphics g) {
            
            super.paint(g);
            
            if(newHeight) {
                newHeight = false;
                
                
                // String s = (String)model.elementAt(model.size()-1);
                
                String s = ((JLabel)(model.elementAt(model.size()-1))).getText();
                int space = 8;
                
                
                if(g != null) {
                    FontMetrics metrics = g.getFontMetrics();
                    metrics.stringWidth(s);
                    list.setSize(metrics.stringWidth(s)+space, pane.getHeight());
                    list.setPreferredSize(new Dimension(metrics.stringWidth(s)+space, pane.getHeight()));
                    setSize(metrics.stringWidth(s)+space, pane.getHeight());
                    setPreferredSize(new Dimension(metrics.stringWidth(s)+space, pane.getHeight()));
                }
            }
        }
        
        public class NumberPanelCellRenderer implements ListCellRenderer {
            
            Icon selIcon;
            JLabel selectedLabel;
            
            public NumberPanelCellRenderer() {
                selIcon = GUIFactory.getIcon("TreeBallLeaf.gif");
                selectedLabel = new JLabel(selIcon);
                selectedLabel.setBackground(Color.gray);
                selectedLabel.setOpaque(true);
                selectedLabel.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));
                selectedLabel.setIconTextGap(0);
                selectedLabel.setHorizontalAlignment(JLabel.RIGHT);
                selectedLabel.setHorizontalTextPosition(JLabel.RIGHT);
            }
            
            public java.awt.Component getListCellRendererComponent(javax.swing.JList jList, Object obj, int param, boolean selected, boolean param4) {
                if(selected && !editOnly)
                    return selectedLabel;
                return  (JLabel)obj;
            }
            
        }
        
        public class LineNumberListener extends MouseAdapter {
            public void mouseClicked(MouseEvent me) {
                int [] indices = list.getSelectedIndices();
                int curStart, curEnd;
                if(indices != null){
                    try {
                        curStart = pane.getLineStartOffset(indices[0]);
                        curEnd = pane.getLineEndOffset(indices[indices.length-1]);
                        pane.getHighlighter().removeAllHighlights();
                        pane.getHighlighter().addHighlight(curStart, curEnd, new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(new Color(120,160,210)));
                        
                    } catch (BadLocationException ble) { return; }
                    pane.select(curStart, curEnd);
                    pane.repaint();
                }
            }
            
            public void mouseReleased(MouseEvent me) {
                int [] indices = list.getSelectedIndices();
                int curStart, curEnd;
                if(indices != null){
                    try {
                        curStart = pane.getLineStartOffset(indices[0]);
                        curEnd = pane.getLineEndOffset(indices[indices.length-1]);
                        pane.getHighlighter().removeAllHighlights();
                        pane.getHighlighter().addHighlight(curStart, curEnd, new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(new Color(120,160,210)));
                        
                    } catch (BadLocationException ble) { return; }
                    pane.select(curStart, curEnd);
                    pane.repaint();
                }
            }
            
            
            
        }
    }
    
    
    
    public class HighlightPanel extends JPanel {
        int start, end, top, bottom;
        boolean algOverlay;
        
        public HighlightPanel() {
            super();
            super.setBackground(Color.white);
            setBackground(Color.white);
            algOverlay = false;
            start = end = -1;
        }
        
        public void setSelection(int s, int e) {
            start = s;
            end = e;
            algOverlay = e>s;
        }
        
        public boolean haveOverlay() {
            return algOverlay;
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            if(start > 0) {
                Color color = g.getColor();
                Graphics2D g2 = (Graphics2D)g;
                Composite composite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g.setColor(Color.green);
                g.fillRect(0, start*numPanel.lineHeight, getWidth(), (end-start)*numPanel.lineHeight);
                g.setColor(color);
                g2.setComposite(composite);
            }
        }
    }
    
    public class ScriptListener implements ScriptEventListener {
        
        public void documentChanged(ScriptDocumentEvent event) {
            updateSize();
        }
        
    }
    
}
