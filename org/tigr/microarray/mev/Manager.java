/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Manager.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-02-24 20:23:44 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.tigr.util.Query;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.MessageDisplay;
import org.tigr.util.awt.ImageScreen;
import org.tigr.microarray.mev.TMEV;

public class Manager {//A class to keep track of viewers
    private static Vector activeComponents;
    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newMultipleArrayViewerItem;
    private JMenuItem newSingleArrayViewerItem;
    private JMenuItem newPreferencesItem;
    private JMenuItem loginItem;
    private JMenuItem quitItem;
    private JMenu displayMenu;
    private JRadioButtonMenuItem javaLFItem;
    private JRadioButtonMenuItem windowsLFItem;
    private JRadioButtonMenuItem motifLFItem;
    private JCheckBoxMenuItem toolTipsItem;
    private static JMenu windowMenu;
    private JMenu referencesMenu;
    private JMenuItem systemInfoItem;
    private JMenuItem acknolMenuItem;
    private JMenuItem papersMenuItem;
    private JMenuItem citationMenuItem;
    private JMenuItem aboutMenuItem;
    private ButtonGroup buttonGroup;
    
    private static EventListener eventListener;
    
    public Manager() {
        try {
            activeComponents = new Vector();
            eventListener = new EventListener();
            
            initializeFrame();
            
            initializeInput();
            
        } catch (Exception e) {
            System.out.println("Exception (Manager.const()): " + e);
            e.printStackTrace();
        }
    }
    
/*
    public void initializeFrame() {
        frame = new JFrame("TIGR MultiExperiment Viewer");
        frame.addWindowListener(eventListener);
        frame.setVisible(true);
 
        initializeMenuBar(frame);
        frame.setSize(frame.getPreferredSize());
 
        frame.validate();
        frame.setResizable(false);
    }
 */
  
   public void initializeFrame() {
        frame = new JFrame("TIGR MultiExperiment Viewer");
        frame.addWindowListener(eventListener);
        
        initializeMenuBar(frame);
        frame.setSize(frame.getPreferredSize());

        frame.setResizable(false);
        //frame.pack() was required for WindowsXP 
        frame.pack();
        frame.setVisible(true);
    }
   
    
    public void initializeMenuBar(JFrame frame) {
        menuBar = new JMenuBar();
        
        fileMenu = new JMenu("File");
        newMultipleArrayViewerItem = new JMenuItem("New Multiple Array Viewer");
        newMultipleArrayViewerItem.addActionListener(eventListener);
        newMultipleArrayViewerItem.setMnemonic(KeyEvent.VK_M);
        fileMenu.add(newMultipleArrayViewerItem);
        
        newSingleArrayViewerItem = new JMenuItem("New Single Array Viewer");
        // newSingleArrayViewerItem.setToolTipText("Temporarily Disabled -- visit www.tigr.org/software/TM4 for update.");
        //  newSingleArrayViewerItem.setEnabled(false);
        newSingleArrayViewerItem.addActionListener(eventListener);
        newSingleArrayViewerItem.setMnemonic(KeyEvent.VK_S);
        fileMenu.add(newSingleArrayViewerItem);
        
        fileMenu.addSeparator();
        
        //newPreferencesItem = new JMenuItem("New Preferences File");
        //newPreferencesItem.addActionListener(eventListener);
        //newPreferencesItem.setMnemonic(KeyEvent.VK_P);
        //fileMenu.add(newPreferencesItem);
        
        //fileMenu.addSeparator();
        
        //loginItem = new JMenuItem("Login to Database");
        //loginItem.addActionListener(eventListener);
        //loginItem.setMnemonic(KeyEvent.VK_L);
        //fileMenu.add(loginItem);
        
        //fileMenu.addSeparator();
        
        quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(eventListener);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
        fileMenu.add(quitItem);
        
        menuBar.add(fileMenu);
        
        displayMenu = new JMenu("Display");
        
        buttonGroup = new ButtonGroup();
        
        javaLFItem = new JRadioButtonMenuItem("Metal L&F");
        javaLFItem.addActionListener(eventListener);
        displayMenu.add(javaLFItem);
        buttonGroup.add(javaLFItem);
        javaLFItem.setSelected(true);
        
        windowsLFItem = new JRadioButtonMenuItem("Windows L&F");
        windowsLFItem.addActionListener(eventListener);
        displayMenu.add(windowsLFItem);
        buttonGroup.add(windowsLFItem);
        
        motifLFItem = new JRadioButtonMenuItem("Motif L&F");
        motifLFItem.addActionListener(eventListener);
        displayMenu.add(motifLFItem);
        buttonGroup.add(motifLFItem);
        
        toolTipsItem = new JCheckBoxMenuItem("Show ToolTips");
        toolTipsItem.addActionListener(eventListener);
        displayMenu.add(toolTipsItem);
        toolTipsItem.setSelected(true);
        
        menuBar.add(displayMenu);
        
        windowMenu = new JMenu("Window");
        windowMenu.setEnabled(false);  //until we meet again...
        windowMenu.addActionListener(eventListener);
        menuBar.add(windowMenu);
        
        referencesMenu = new JMenu("References");
        
        acknolMenuItem = new JMenuItem("Contributions...");
        acknolMenuItem.addActionListener(eventListener);
        referencesMenu.add(acknolMenuItem);
        
        papersMenuItem = new JMenuItem("Papers / Publications");
        papersMenuItem.addActionListener(eventListener);
        referencesMenu.add(papersMenuItem);
        
        citationMenuItem = new JMenuItem("Referencing TMeV...");
        citationMenuItem.addActionListener(eventListener);
        referencesMenu.add(citationMenuItem);
        
        referencesMenu.addSeparator();
        
        systemInfoItem = new JMenuItem("System Info");
        systemInfoItem.addActionListener(eventListener);
        referencesMenu.add(systemInfoItem);
        
        referencesMenu.addSeparator();
        
        aboutMenuItem = new JMenuItem("About MeV");
        aboutMenuItem.addActionListener(eventListener);
        referencesMenu.add(aboutMenuItem);
        
        menuBar.add(referencesMenu);
        
        frame.setJMenuBar(menuBar);
        
        menuBar.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-2, menuBar.getFontMetrics(menuBar.getFont()).getHeight()+5));
    }
    
    public boolean selectPreferencesFile() {
        File inputFile = null;
        String filename = "";
        boolean success = false;
        Dimension screenSize = frame.getToolkit().getScreenSize();
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String extension = "";
                if (f.isDirectory()) return true;
                if (f.getName().endsWith("Preferences")) return true;
                else if (f.getName().endsWith("preferences")) return true;
                else if (f.getName().endsWith(".pref")) return true;
                else return false;
            }
            public String getDescription() {
                return "Preference Files";
            }
        });
        chooser.setDialogTitle("Select Preferences File");
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(TMEV.getFile("preferences/"));
        
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            inputFile = chooser.getSelectedFile();
        } else {
            System.out.println("No preference file selected");
            return false;
            //    inputFile = new File(chooser.getCurrentDirectory(), "TMEV Preferences");
        }
        
        if (TMEV.readPreferencesFile(inputFile) == false) {
            message(frame, "Error in preferences file: " + inputFile.getName());
            TMEV.quit();
        } else {
            System.out.println("Successfully read preferences file - " + inputFile.getName());
            return true;
        }
        
        return false;
    }
    
    public static void addComponent(Component component) {
        activeComponents.addElement(component);
        updateWindowMenu();
    }
    
    public static void updateWindowMenu() {
        String menuTitle;
        JMenuItem item;
        windowMenu.removeAll();
        
        windowMenu.setEnabled(activeComponents.size() > 0);
        
        Component component;
        
        for(int i = 0; i < activeComponents.size(); i++) {
            component = (Component)(activeComponents.elementAt(i));
            if(component instanceof SingleArrayViewer) {
                item = new JMenuItem(((SingleArrayViewer)component).getFrame().getTitle());
                
            } else {
                item = new JMenuItem(((MultipleArrayViewer)component).getFrame().getTitle());
            }
            item.setActionCommand("window-cmd");
            item.addActionListener(eventListener);
            windowMenu.add(item);
        }
    }
    
    public static Component getComponent(int position) {
        return(Component) activeComponents.elementAt(position);
    }
    
    public static void removeComponent(Component component) {
        activeComponents.removeElement(component);
        updateWindowMenu();
    }
    
    public void initializeInput() {
        try {
            //  String inputPreference = TMEV.getSettingForOption("Input Preference");
            String inputPreference = "Only File";
            if (inputPreference.equals("Database")) {
                databaseLogin();
                systemEnable(TMEV.DB_AVAILABLE);
                systemEnable(TMEV.DB_LOGIN);
            } else if (inputPreference.equals("File")) {
                databaseLogin();
                systemEnable(TMEV.DB_AVAILABLE);
                systemEnable(TMEV.DB_LOGIN);
            } else if (inputPreference.equals("Only File")) {
            } else {
                Manager.message(frame, "Error: Invalid Preferences File");
            }
        } catch (Exception e) {
            System.out.println("Exception (TMEV.initializeInput()): " + e);
        }
    }
    
    public static void createNewMultipleArrayViewer() {
        MultipleArrayViewer mav = new MultipleArrayViewer();
        Manager.addComponent(mav);
        
        TMEV.clearFieldNames();
        //Remove the next two lines (about DB system enabling)
        //mav.systemEnable(TMEV.DB_AVAILABLE);
        //mav.systemEnable(TMEV.DB_LOGIN);
        mav.getFrame().setSize(1050, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
    }
    
    public static void createNewMultipleArrayViewer(MultipleArrayData data, String clusterLabel){
        MultipleArrayViewer mav = new MultipleArrayViewer(data);
        //Remove the next two lines (about DB system enabling)
        //mav.systemEnable(TMEV.DB_AVAILABLE);
        //mav.systemEnable(TMEV.DB_LOGIN);
        mav.getFrame().setSize(1050, 700);
        if(clusterLabel != null)
            mav.getFrame().setTitle("TIGR Multiple Array Viewer, "+clusterLabel);
        Manager.addComponent(mav);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
    }
    
    
    public static void createNewMultipleArrayViewer(MultipleArrayMenubar origMenubar, MultipleArrayData data, String clusterLabel){
        
        MultipleArrayViewer mav = new MultipleArrayViewer(data, origMenubar);
        //Remove the next two lines (about DB system enabling)
        //mav.systemEnable(TMEV.DB_AVAILABLE);
        //mav.systemEnable(TMEV.DB_LOGIN);
        mav.getFrame().setSize(1050, 700);
        if(clusterLabel != null)
            mav.getFrame().setTitle("TIGR Multiple Array Viewer, "+clusterLabel);
        Manager.addComponent(mav);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
    }
    
    public static void createNewSingleArrayViewer() {
        SingleArrayViewer sav = new SingleArrayViewer(new JFrame("Single Array Viewer"));
        Manager.addComponent(sav);
        //Remove the next two lines (about DB system enabling)
        sav.systemEnable(TMEV.DB_AVAILABLE);
        sav.systemEnable(TMEV.DB_LOGIN);
        sav.getFrame().setSize(650, 650);
        sav.getFrame().setLocation(100, 100);
        sav.getFrame().setVisible(true);
    }
    
    public static void createNewSingleArrayViewer(ISlideData preparedArray) {
        SingleArrayViewer sav = new SingleArrayViewer(new JFrame("Single Array Viewer"), preparedArray);
        Manager.addComponent(sav);
        //Remove the next two lines (about DB system enabling)
        //sav.systemEnable(TMEV.DB_AVAILABLE);
        //sav.systemEnable(TMEV.DB_LOGIN);
        sav.getFrame().setSize(650, 650);
        sav.getFrame().setLocation(100, 100);
        sav.getFrame().setVisible(true);
        sav.refreshSlide();
    }
    
    public static void createNewSingleArrayViewer(ISlideData preparedArray, float upperCy3Cutoff, float upperCy5Cutoff) {
        SingleArrayViewer sav = new SingleArrayViewer(new JFrame("Single Array Viewer"), preparedArray);
        Manager.addComponent(sav);
        //Remove the next two lines (about DB system enabling)
        sav.systemEnable(TMEV.DB_AVAILABLE);
        sav.systemEnable(TMEV.DB_LOGIN);
        sav.setUpperLimits(upperCy3Cutoff, upperCy5Cutoff);
        sav.panel.setXYScrollbars((long)upperCy3Cutoff, (long)upperCy5Cutoff);
        sav.getFrame().setSize(650, 650);
        sav.getFrame().setLocation(100, 100);
        sav.getFrame().setVisible(true);
        sav.refreshSlide();
    }
    
    public static void displaySlideElementInfo(JFrame frame, MultipleArrayData data, int feature, int probe) {
        new InfoDisplay(frame, data, feature, probe);
    }
    
    public static void displaySlideElementInfo(JFrame frame, ISlideData slideData, ISlideDataElement element, int probe) {
        new InfoDisplay(frame, slideData, element, probe);
    }
    
    public void systemDisable(int state) {
        switch (state) {
            case TMEV.SYSTEM:
                break;
            case TMEV.DATA_AVAILABLE:
                break;
            case TMEV.DB_AVAILABLE:
                break;
            case TMEV.DB_LOGIN:
                break;
        }
        for (int i = 0; i < activeComponents.size(); i++) {
            ((ArrayViewer) activeComponents.elementAt(i)).systemDisable(state);
        }
    }
    
    public void systemEnable(int state) {
        switch (state) {
            case TMEV.SYSTEM:
                break;
            case TMEV.DATA_AVAILABLE:
                break;
            case TMEV.DB_AVAILABLE:
                break;
            case TMEV.DB_LOGIN:
                break;
        }
        for (int i = 0; i < activeComponents.size(); i++) {
            ((ArrayViewer)activeComponents.elementAt(i)).systemEnable(state);
        }
    }
    
    public void databaseLogin() {
        DatabaseLoginDialog dld = new DatabaseLoginDialog(frame);
        dld.addActionInfoListener(new ActionInfoListener() {
            public void actionInfoPerformed(ActionInfoEvent event) {
                Hashtable hash = event.getHashtable();
                databaseLoad((String) hash.get("username"), (String) hash.get("password"));
            }
        });
        dld.show();
    }
    
    public void databaseLoad(String username, String password) {
        if ((TMEV.getConnection() != null) || TMEV.connect(username, password)) {
            SetDatabaseDialog sdd = new SetDatabaseDialog(frame);
            if (sdd.showModal() == JOptionPane.OK_OPTION) {
                useDatabase(sdd.getDatabase());
            }
        }
    }
    
    public void useDatabase(String database) {
        try {
            Query query = new Query("use " + database);
            query.executeUpdate(   TMEV.getConnection(   ));
            for (int i = 0; i < Manager.activeComponents.size(); i++) {
                ((ArrayViewer) activeComponents.elementAt(i)).systemEnable(TMEV.DB_LOGIN);
            }
        } catch (Exception e) {
            System.out.println("Exception (ColumnApplet.useDatabase()): " + e);
        }
    }
    
    public static void message(JFrame parent, String message) {
        System.out.println(message);
        MessageDisplay md = new MessageDisplay(parent, message);
        md.show();
    }
    
    public static void message(JFrame parent, Exception e) {
        message(parent, e.toString());
    }
    
    public static void exception(JFrame parent, String exception) {
        System.out.println("EXCEPTION: " + exception);
        MessageDisplay md = new MessageDisplay(parent, exception);
        md.show();
    }
    
    public static void exception(JFrame parent, Exception e) {
        exception(parent, e.toString());
    }
    
    private void handleItems(Object target) {
    }
    
    public void setLookAndFeel(String lookAndFeelStr) {
        try {
            UIManager.setLookAndFeel(lookAndFeelStr);
            SwingUtilities.updateComponentTreeUI(frame);
            for (int i = 0; i < activeComponents.size(); i++) {
                SwingUtilities.updateComponentTreeUI(((Component) activeComponents.elementAt(i)).getParent());
            }
        } catch (Exception e) {
        }
    }
    
    private class EventListener implements ActionListener, WindowListener {
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            handleItems(source);
            if (source == newMultipleArrayViewerItem) {
                createNewMultipleArrayViewer();
            } else if (source == newSingleArrayViewerItem) {
                createNewSingleArrayViewer();
            } else if (source == loginItem) {
                databaseLogin();
            } else if (source == newPreferencesItem) {
                selectPreferencesFile();
            } else if (source == quitItem) {
                TMEV.quit();
            } else if (source == javaLFItem) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    SwingUtilities.updateComponentTreeUI(frame);
                    for (int i = 0; i < activeComponents.size(); i++) {
                        SwingUtilities.updateComponentTreeUI(((Component) activeComponents.elementAt(i)).getParent());
                    }
                } catch (Exception e) {
                }
            } else if (source == windowsLFItem) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    SwingUtilities.updateComponentTreeUI(frame);
                    for (int i = 0; i < activeComponents.size(); i++) {
                        SwingUtilities.updateComponentTreeUI(((Component) activeComponents.elementAt(i)).getParent());
                    }
                } catch (Exception e) {
                }
            } else if (source == motifLFItem) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    SwingUtilities.updateComponentTreeUI(frame);
                    for (int i = 0; i < activeComponents.size(); i++) {
                        SwingUtilities.updateComponentTreeUI(((Component) activeComponents.elementAt(i)).getParent());
                    }
                } catch (Exception e) {
                }
            } else if (source == toolTipsItem) {
                if (toolTipsItem.isSelected()) {
                    ToolTipManager.sharedInstance().setEnabled(true);
                } else {
                    ToolTipManager.sharedInstance().setEnabled(false);
                }
            } else if(source == acknolMenuItem){
                new AcknowlegementDialog(frame, AcknowlegementDialog.createAcknowlegementText());
            } else if (source == papersMenuItem) {
                new PaperReferencesDialog(frame, PaperReferencesDialog.createReferencesText());
            } else if(source == citationMenuItem){
                new MevCitationDialog(frame, MevCitationDialog.createCitationText());
            } else if (source == aboutMenuItem) {
                ImageScreen is = new ImageScreen();
                is.showImageScreen();
            } else if (source == systemInfoItem) {
                int width = 640, height = 550;
                InformationPanel infoPanel = new InformationPanel();
                JFrame frame = new JFrame("System Information");
                frame.getContentPane().add(infoPanel);
                frame.setSize(width, height);
                Dimension screenSize = frame.getToolkit().getScreenSize();
                frame.setLocation(screenSize.width/2 - width/2, screenSize.height/2 - height/2);
                frame.setResizable(false);
                frame.setVisible(true);
                infoPanel.Start();
            }
            
            if (event.getActionCommand().equals("window-cmd")) {
                int compCount = windowMenu.getItemCount();
                JMenuItem item = (JMenuItem)(event.getSource());
                
                for(int i = 0; i < compCount; i++) {
                    if(item == windowMenu.getItem(i)) {
                        JComponent component = (JComponent)activeComponents.elementAt(i);
                        component.requestFocus();
                        component.setLocation(component.getLocation());
                        
                        if(component.getClass() == SingleArrayViewer.class)
                            ((SingleArrayViewer)component).getFrame().requestFocus();
                        else
                            ((MultipleArrayViewer)component).getFrame().requestFocus();
                        
                        break;
                    }
                }
            }
        }
        
        public void windowClosing(WindowEvent event) {
            
            //in the event of an active save, inform user of state save
            if(TMEV.activeSave) {
                JOptionPane.showMessageDialog(frame, "Analayis save is in progress. "+
                "MeV will close when complete.", "Analysis Save in Progress", JOptionPane.INFORMATION_MESSAGE);
            }
            
            frame.dispose();
            
            while(TMEV.activeSave) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) { }
            }
            System.exit(0);
        }
        
        
        public void windowOpened(WindowEvent event) {}
        public void windowClosed(WindowEvent event) {}
        public void windowIconified(WindowEvent event) {}
        public void windowDeiconified(WindowEvent event) {}
        public void windowActivated(WindowEvent event) {}
        public void windowDeactivated(WindowEvent event) {}
    }
}