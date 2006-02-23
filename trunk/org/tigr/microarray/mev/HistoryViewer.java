/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HistoryViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
/*
 * HistoryViewer.java
 *
 * Created on February 3, 2004, 10:47 PM
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/** The HistoryViewer records analysis events in text
 * format.  For each event a time stamp and an event
 * text entry is made for the event.
 *
 * @author braisted
 */
public class HistoryViewer extends ViewerAdapter implements java.io.Serializable {        
    public static final long serialVersionUID = 100010201080001L;
    
    /** Contains the text contents of the viewer.
     */    
    private JTextArea content;
    /** Context menu.
     */    
    private JPopupMenu menu;
    
    /** Creates a new instance of HistoryViewer */
    public HistoryViewer() {
        initContent();        
        addHistory("Open Multiple Array Viewer");
        initMenu();
        content.addMouseListener(new HistoryListener());
    }
    
    /** Initialized the viewer's content.
     */    
    private void initContent() {
        content = new JTextArea();
        content.setEditable(false);
        content.setMargin(new Insets(10,10,10,10));
        content.setBackground(new Color(252,255,168));        
    }
    
    /** Initializes the <CODE> JPopupMenu</CODE>.
     */    
    private void initMenu() {
        menu = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Save History to File", GUIFactory.getIcon("save16.gif"));
        saveItem.setActionCommand("save");
        saveItem.addActionListener(new HistoryListener());
        menu.add(saveItem);
    }
    
    /** Adds a history entry
     * @param msg Messge to add to history.
     */    
    public void addHistory(String msg) {
        content.append(getDateStamp()+msg+"\n\n");
    }

    /** Returns the current date/time stamp in <CODE>String</CODE>
     * format.
     */    
    private String getDateStamp() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date) + " -- ";
    }
    
    /** Returns the viewers content.
     * @return
     */    
    public JComponent getContentComponent() {
        return content;
    }
        
    /** Writes the HistoryViewer to an <CODE>ObjectOutputStream</CODE>
     * @param oos ObjectOutputStream
     * @throws IOException
     */    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(content.getText());
    }
    
    /** Reads the HistoryViewer to an <CODE>ObjectOuputStream</CODE>
     *
     * @param ois ObjectInputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String text = (String)ois.readObject();
        initContent();
        content.append(text);
        addHistory("Load Analysis From File");
        initMenu();
        content.addMouseListener(new HistoryListener());
    }
    
    /** Saves the history to file.
     */    
    private void saveHistory() {
        String sep = System.getProperty("file.separator");
        JFileChooser chooser = new JFileChooser(TMEV.getFile("data/"));
        if(chooser.showSaveDialog(content) == JOptionPane.OK_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                PrintWriter bfr = new PrintWriter(new FileWriter(file));

                StringTokenizer stok = new StringTokenizer(content.getText(), "\n\n");
                
                while(stok.hasMoreTokens()){
                    bfr.println(stok.nextToken());
                }
                
                bfr.flush();
                bfr.close();
     
            } catch (IOException ioe) {
                String msg = ioe.getMessage();
                JOptionPane.showMessageDialog(content, "An error occurred while saving history. \nMessage: "+msg, "Error Saving History", JOptionPane.WARNING_MESSAGE);
            }
        }            
    }
    
    
    /** HistoryListener is responsible for listening to mouse
     * and menu events
     */    
    public class HistoryListener extends MouseAdapter implements ActionListener {
        /** Responds to press events.
         */        
        public void mousePressed(MouseEvent me) {
            if(me.isPopupTrigger()) {
                menu.show(content, me.getX(), me.getY());
            }
        }
        
        /** Responds to mouse released events.
         */        
        public void mouseReleased(MouseEvent me) {
            if(me.isPopupTrigger()) {
                menu.show(content, me.getX(), me.getY());
            }
        }
        /** Responds to menu events.
         */        
        public void actionPerformed(ActionEvent ae) {
            if(ae.getActionCommand().equals("save")){
                saveHistory();
            }
        }
    }

}
