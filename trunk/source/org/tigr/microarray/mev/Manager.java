/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Manager.java,v $
 * $Revision: 1.18 $
 * $Date: 2007-12-19 21:39:34 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.tigr.microarray.mev.annotation.InvalidAnnMappingFileException;
import org.tigr.microarray.mev.file.FileLoadInfo;
import org.tigr.microarray.mev.file.FileType;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.Query;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.util.awt.ImageScreen;
import org.tigr.util.awt.MessageDisplay;

public class Manager {//A class to keep track of viewers
    private static Vector<Component> activeComponents;
    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newMultipleArrayViewerItem;
    private JMenuItem newPreferencesItem;
    private JMenuItem loginItem;
    private JMenuItem quitItem;
    private JMenu preferencesMenu;
    private JRadioButtonMenuItem javaLFItem;
    private JRadioButtonMenuItem windowsLFItem;
    private JRadioButtonMenuItem motifLFItem;
    private JCheckBoxMenuItem toolTipsItem;
    private JCheckBoxMenuItem promptToSaveItem;
    private JCheckBoxMenuItem promptToGetOnlineItem;
    private JMenuItem selectURLLinkoutFile;
    private static JMenu windowMenu;
    private JMenu referencesMenu;
    private JMenuItem systemInfoItem;
    private JMenuItem acknolMenuItem;
    private JMenuItem papersMenuItem;
    private JMenuItem citationMenuItem;
    private JMenuItem aboutMenuItem;
    private ButtonGroup buttonGroup;
    //added 9.27.05 vu
    private JMenu helpMenu;
    private JMenuItem bugReportMenuItem;
    private JMenuItem featureReqMenuItem;
    //added by wwang
    private JMenuItem documentMenuItem;
    
    private static EventListener eventListener;
    
    public Manager() {
        try {
            activeComponents = new Vector<Component>();
            eventListener = new EventListener();
            
            initializeFrame();
            
            initializeInput();
            
        } catch (Exception e) {
            System.out.println("Exception (Manager.const()): " + e);
            e.printStackTrace();
        }
    }
    
    public void initializeFrame() {
        frame = new JFrame("MultiExperiment Viewer");
        frame.addWindowListener(eventListener);
        
        initializeMenuBar(frame);
        frame.setSize(frame.getPreferredSize());
        
        // Added by JD to show icon in window and task bar
        String iconFile = "org/tigr/images/icon.png";
        URL iconURL = this.getClass().getClassLoader().getResource(iconFile);
        ImageIcon imgIcon = new ImageIcon(iconURL);
        if (imgIcon!=null) {
        	frame.setIconImage(imgIcon.getImage());
        }

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
        
        fileMenu.addSeparator();
        
        quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(eventListener);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
        fileMenu.add(quitItem);
        
        menuBar.add(fileMenu);
        
        preferencesMenu = new JMenu("Preferences");
        
        buttonGroup = new ButtonGroup();
        
        javaLFItem = new JRadioButtonMenuItem("Metal L&F");
        javaLFItem.addActionListener(eventListener);
        preferencesMenu.add(javaLFItem);
        buttonGroup.add(javaLFItem);
        javaLFItem.setSelected(true);
        
        windowsLFItem = new JRadioButtonMenuItem("Windows L&F");
        windowsLFItem.addActionListener(eventListener);
        preferencesMenu.add(windowsLFItem);
        buttonGroup.add(windowsLFItem);
        
        motifLFItem = new JRadioButtonMenuItem("Motif L&F");
        motifLFItem.addActionListener(eventListener);
        preferencesMenu.add(motifLFItem);
        buttonGroup.add(motifLFItem);
        
        toolTipsItem = new JCheckBoxMenuItem("Show ToolTips");
        toolTipsItem.addActionListener(eventListener);
        preferencesMenu.add(toolTipsItem);
        toolTipsItem.setSelected(true);
        

        promptToSaveItem = new JCheckBoxMenuItem("Prompt to save analysis before closing");
        promptToSaveItem.addActionListener(eventListener);
        preferencesMenu.add(promptToSaveItem);
        promptToSaveItem.setSelected(new Boolean(TMEV.getSettingForOption(TMEV.PROMPT_TO_SAVE_ANALYSIS)));

        promptToGetOnlineItem = new JCheckBoxMenuItem("Ask to get online");
        promptToGetOnlineItem.addActionListener(eventListener);
        preferencesMenu.add(promptToGetOnlineItem);
        promptToGetOnlineItem.setSelected(new Boolean(TMEV.getSettingForOption(TMEV.PROMPT_TO_GET_ONLINE)));
        
        selectURLLinkoutFile = new JMenuItem("Select annotation linkout file...");
        selectURLLinkoutFile.addActionListener(eventListener);
        preferencesMenu.add(selectURLLinkoutFile);
        
        menuBar.add(preferencesMenu);
        
        windowMenu = new JMenu("Window");
        windowMenu.setEnabled(false);  //until we meet again...
        windowMenu.addActionListener(eventListener);
        menuBar.add(windowMenu);
        
        referencesMenu = new JMenu("About");
        
        acknolMenuItem = new JMenuItem("Credits");
        acknolMenuItem.addActionListener(eventListener);
        referencesMenu.add(acknolMenuItem);
        
        papersMenuItem = new JMenuItem("Papers / Publications");
        papersMenuItem.addActionListener(eventListener);
        referencesMenu.add(papersMenuItem);
        
        citationMenuItem = new JMenuItem("Referencing MeV...");
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
        
        helpMenu = new JMenu( "Help" );
        documentMenuItem = new JMenuItem( "Mev Manual" );
        documentMenuItem.addActionListener(eventListener);
        bugReportMenuItem = new JMenuItem( "Report Bug" );
        bugReportMenuItem.addActionListener( eventListener );
        helpMenu.add( documentMenuItem );
        helpMenu.add( bugReportMenuItem );
        
        featureReqMenuItem = new JMenuItem( "Request a Feature" );
        featureReqMenuItem.addActionListener( eventListener );
        
        helpMenu.add( featureReqMenuItem );
        
        menuBar.add( helpMenu );
        
        frame.setJMenuBar(menuBar);
        
        menuBar.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-2, menuBar.getFontMetrics(menuBar.getFont()).getHeight()+5));
    }
    
    public boolean selectPreferencesFile() {
        File inputFile = null;
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
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
        JMenuItem item;
        windowMenu.removeAll();
        
        windowMenu.setEnabled(activeComponents.size() > 0);
        
        Component component;
        
        for(int i = 0; i < activeComponents.size(); i++) {
            component = (Component)(activeComponents.elementAt(i));
            item = new JMenuItem(((MultipleArrayViewer)component).getFrame().getTitle());
            item.setActionCommand("window-cmd");
            item.addActionListener(eventListener);
            windowMenu.add(item);
        }
    }
    
    public static Component getLastComponent() {
    	return ( Component ) activeComponents.lastElement();
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
    
    public static void createNewMultipleArrayViewer( int xOffset, int yOffset ) {
        MultipleArrayViewer mav = new MultipleArrayViewer();
        Manager.addComponent(mav);
        mav.getFrame().setSize(1150, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - mav.getFrame().getSize().width)/2 + xOffset;
        int y = (screenSize.height - mav.getFrame().getSize().height)/2 + yOffset;
        mav.getFrame().setLocation(x, y);
        mav.getFrame().setVisible(true);
    }
    
    public static void createNewMultipleArrayViewer() {
        MultipleArrayViewer mav = new MultipleArrayViewer();
        Manager.addComponent(mav);
        mav.getFrame().setSize(1150, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
    }

    public static void createNewMultipleArrayViewer(MultipleArrayData data, String clusterLabel){
        MultipleArrayViewer mav = new MultipleArrayViewer(data);
        mav.getFrame().setSize(1150, 700);
        if(clusterLabel != null)
            mav.getFrame().setTitle("Multiple Array Viewer, "+clusterLabel);
        Manager.addComponent(mav);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
    }
    
    
    public static void createNewMultipleArrayViewer(MultipleArrayMenubar origMenubar, MultipleArrayData data, String clusterLabel){
        MultipleArrayViewer mav = new MultipleArrayViewer(data, origMenubar);
        mav.getFrame().setSize(1150, 700);
        if(clusterLabel != null)
            mav.getFrame().setTitle("Multiple Array Viewer, "+clusterLabel);
        Manager.addComponent(mav);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
        
    }
  
	public static void createNewMultipleArrayViewer(FileLoadInfo fileInfo) {
		MultipleArrayViewer mav = new MultipleArrayViewer();
        Manager.addComponent(mav);
        mav.getFrame().setSize(1150, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mav.getFrame().setLocation((screenSize.width - mav.getFrame().getSize().width)/2, (screenSize.height - mav.getFrame().getSize().height)/2);
        mav.getFrame().setVisible(true);
        mav.loadData(fileInfo);
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
        dld.setVisible(true);
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
        md.setVisible(true);
    }
    
    public static void message(JFrame parent, Exception e) {
        message(parent, e.toString());
    }
    
    public static void exception(JFrame parent, String exception) {
        System.out.println("EXCEPTION: " + exception);
        MessageDisplay md = new MessageDisplay(parent, exception);
        md.setVisible(true);
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

            } else if (source == promptToGetOnlineItem) {
        	    TMEV.storeProperty(TMEV.PROMPT_TO_GET_ONLINE, new Boolean(promptToGetOnlineItem.isSelected()).toString());
        	    TMEV.getResourceManager().setAskToGetOnline(new Boolean(promptToGetOnlineItem.isSelected()));
            } else if (source == selectURLLinkoutFile) {
            	JFileChooser chooser = new JFileChooser(System.getProperty(TMEV.getDataPath()));
        		chooser.setDialogTitle("Select an annotation mapping file");
        		chooser.setCurrentDirectory(new File(TMEV.getDataPath()));
        		chooser.setMultiSelectionEnabled(false);

        		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        			File urlsFile = chooser.getSelectedFile();
	            	try {
	            		TMEV.loadAnnotationsURLs(urlsFile);
	            		TMEV.storeProperty(TMEV.CUSTOM_ANNOTATION_URLS_FILE, urlsFile.getAbsolutePath());
	            	} catch (InvalidAnnMappingFileException iamfe) {
	            		JOptionPane.showMessageDialog(frame, "MeV was unable to read the URL mappings from the file " + urlsFile.toString() + ". The MeV manual contains details about this type of file. ", "Error", JOptionPane.ERROR_MESSAGE);            		
	            	} catch (FileNotFoundException fnfe) {
	            		JOptionPane.showMessageDialog(frame, "Could not find " + urlsFile + " file", "Error", JOptionPane.ERROR_MESSAGE);            		
	            	}
        		}
            } else if (source == promptToSaveItem) {
        	    TMEV.storeProperty(TMEV.PROMPT_TO_SAVE_ANALYSIS, new Boolean(promptToSaveItem.isSelected()).toString());
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
            } else if( source == bugReportMenuItem ) {	//added 9.27.05 vu
            	try {
					BrowserLauncher.openURL( "http://sourceforge.net/tracker/?atid=656691&group_id=110558&func=browse" );
				} catch( IOException e ) {
					e.printStackTrace();
					//BrowserLauncher doesn't work on this system, display dialog
					JOptionPane.showMessageDialog( frame, 
							"Go to http://sourceforge.net/tracker/?atid=656691&group_id=110558&func=browse",
							"Input Error", JOptionPane.ERROR_MESSAGE );
				}
            } else if( source == featureReqMenuItem ) {	//added 9.27.05 vu
            	try {
					BrowserLauncher.openURL( "http://sourceforge.net/tracker/?atid=656694&group_id=110558&func=browse" );
				} catch( IOException e ) {
					e.printStackTrace();
					//BrowserLauncher doesn't work on this system, display dialog
					JOptionPane.showMessageDialog( frame, 
							"Go to http://sourceforge.net/tracker/?atid=656694&group_id=110558&func=browse",
							"Input Error", JOptionPane.ERROR_MESSAGE );
				}
            }else if( source == documentMenuItem ) {	//added wwang
            	try {
					BrowserLauncher.openURL( "http://www.tm4.org/documentation/MeV_Manual_4_4.pdf" );
				} catch( IOException e ) {
					e.printStackTrace();
					//BrowserLauncher doesn't work on this system, display dialog
					JOptionPane.showMessageDialog( frame, 
							"Go to http://www.tm4.org/documentation/MeV_Manual_4_4.pdf",
							"Input Error", JOptionPane.ERROR_MESSAGE );
				}
            
            }
            if (event.getActionCommand().equals("window-cmd")) {
                int compCount = windowMenu.getItemCount();
                JMenuItem item = (JMenuItem)(event.getSource());
                
                for(int i = 0; i < compCount; i++) {
                    if(item == windowMenu.getItem(i)) {
                        JComponent component = (JComponent)activeComponents.elementAt(i);
                        component.requestFocus();
                        component.setLocation(component.getLocation());
                        
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
