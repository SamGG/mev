package org.tigr.microarray.mev.file;

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SuperExpressionFileLoader.java,v $
 * $Revision: 1.5 $
 * $Date: 2004-03-03 15:38:48 $
 * $Author: braisted $
 * $State: Exp $
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import javax.swing.tree.*;

import org.tigr.microarray.file.MevFileParser;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

import org.tigr.microarray.mev.TMEV;


// Loads expression data in various file formats

public class SuperExpressionFileLoader {
    
    public static String DATA_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"Data";
    public final static ImageIcon ICON_COMPUTER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SuperExpressionFileLoader.class.getClassLoader().getResource("org/tigr/images/PCIcon.gif")));
    public final static ImageIcon ICON_DISK = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SuperExpressionFileLoader.class.getClassLoader().getResource("org/tigr/images/disk.gif")));
    public final static ImageIcon ICON_FOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SuperExpressionFileLoader.class.getClassLoader().getResource("org/tigr/images/Directory.gif")));
    public final static ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SuperExpressionFileLoader.class.getClassLoader().getResource("org/tigr/images/expandedfolder.gif")));
    
    protected ExpressionFileLoader[] fileLoaders;
    protected ExpressionFileLoader selectedFileLoader;
    protected FileFilter[] fileFilters;
    protected FileFilter selectedFileFilter;
    
    protected JFrame mainFrame;
    
    protected JPanel fileFilterPanel;
    protected JLabel fileFilterLabel;
    protected JComboBox fileFilterComboBox;
    
    protected JPanel fileLoaderPanel;
    
    protected JButton infoButton;
    protected JButton cancelButton;
    protected JButton loadButton;
    protected JPanel buttonPanel;
    protected JPanel selectionPanel;
    protected JSplitPane mainSplitPane;
    protected GBA gba;
    protected EventListener eventListener;
    protected MultipleArrayViewer viewer;
    
    protected Loader loader;
    protected int loaderIndex = 0;
    
    public SuperExpressionFileLoader(MultipleArrayViewer viewer) {
        this.viewer = viewer;
        loader = new Loader();
        initializeDataPath();
        initializeFileLoaders();
        initializeGUI();
    }
    
    public SuperExpressionFileLoader() {
        //   this.viewer = viewer;
        loader = new Loader();
        initializeFileLoaders();
        initializeGUI();
    }
    
    protected void initializeFileLoaders() {
        
        int defaultSelection = 0;
        
        fileLoaders = new ExpressionFileLoader[5];
        fileLoaders[0] = new MevFileLoader(this);
        fileLoaders[1] = new StanfordFileLoader(this);
        fileLoaders[2] = new TavFileLoader(this);
        fileLoaders[3] = new AffymetrixFileLoader(this); 
        fileLoaders[4] = new GenePixFileLoader(this);
        //fileLoaders[4] = new ArraySuiteFileLoader();
        
        selectedFileLoader = fileLoaders[defaultSelection];
        
        fileFilters = new FileFilter[fileLoaders.length];
        for (int i = 0; i < fileLoaders.length; i++) {
            fileFilters[i] = fileLoaders[i].getFileFilter();
        }
        selectedFileFilter = fileFilters[defaultSelection];
    }
    
    public void initializeGUI() {
        gba = new GBA();
        eventListener = new EventListener();
        
        mainFrame = new JFrame("Expression File Loader");
        mainFrame.getContentPane().setLayout(new GridBagLayout());
        
        fileFilterLabel = new JLabel("Load expression files of type:");
        fileFilterComboBox = new JComboBox();
        addFileFilters(fileFilters);
        fileFilterComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                changeSelectedFileFilterAndLoader(fileFilterComboBox.getSelectedIndex());
            }
        });
        
        HeaderImagePanel header = new HeaderImagePanel();
        
        
        fileFilterPanel = new JPanel();
        fileFilterPanel.setLayout(new GridBagLayout());
        gba.add(fileFilterPanel, fileFilterLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(fileFilterPanel, fileFilterComboBox, 1, 0, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        fileLoaderPanel = selectedFileLoader.getFileLoaderPanel();
        fileLoaderPanel.setSize(new Dimension(600, 600));
        fileLoaderPanel.setPreferredSize(new Dimension(600, 600));
        
        infoButton = new JButton(null, new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("org/tigr/images/Information24.gif"))));
        infoButton.setActionCommand("info-command");
        infoButton.addActionListener(eventListener);
        infoButton.setFocusPainted(false);
        infoButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(eventListener);
        cancelButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cancelButton.setSize(60,30);
        cancelButton.setPreferredSize(new Dimension(60,30));
        cancelButton.setFocusPainted(false);
        loadButton = new JButton("Load");
        loadButton.addActionListener(eventListener);
        loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(240,240,240), new Color(180,180,180), new Color(10,0,0), new Color(10,10,10) ));
        loadButton.setSize(60,30);
        loadButton.setPreferredSize(new Dimension(60,30));
        loadButton.setFocusPainted(false);
        loadButton.setEnabled(false);
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        gba.add(buttonPanel, infoButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(buttonPanel, new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("org/tigr/images/dialog_button_bar.gif")))),
        1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(buttonPanel, cancelButton, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(buttonPanel, loadButton, 3, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        gba.add(mainFrame.getContentPane(), header, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C);
        gba.add(mainFrame.getContentPane(), fileFilterPanel, 0, 1, 1, 1, 1, 0, GBA.B, GBA.C);
        gba.add(mainFrame.getContentPane(), fileLoaderPanel, 0, 2, 1, 2, 1, 1, GBA.B, GBA.C);
        gba.add(mainFrame.getContentPane(), buttonPanel, 0, 4, 1, 1, 1, 0, GBA.H, GBA.C);
        
        mainFrame.setSize(1000, 750);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((screenSize.width - mainFrame.getSize().width) / 2 , (screenSize.height - mainFrame.getSize().height) / 2);
        mainFrame.setVisible(true);
        selectedFileLoader.openDataPath();
    }
    
    public void initializeDataPath(){
        
        String newPath = TMEV.getDataPath();

        if(newPath == null) {
            return;
        }
        
        File file = new File(newPath);        
        if(file.exists()){
            DATA_PATH = newPath;
        }        
    }
    
    public void setLoadEnabled(boolean state) {
        loadButton.setEnabled(state);
    }
    
    public void changeSelectedFileFilterAndLoader(int target) {
        
        if (target < 0 || target >= fileLoaders.length || target >= fileFilters.length) return;
        
        selectedFileLoader = fileLoaders[target];
        selectedFileFilter = fileFilters[target];
        loaderIndex = target;
        changeFileLoaderPanel(selectedFileLoader);
    }
    

    
    public void changeFileLoaderPanel(ExpressionFileLoader targetFileLoader) {
        
        Container cp = mainFrame.getContentPane();
        
        cp.remove(fileLoaderPanel); // Remove the old fileLoaderPanel
        fileLoaderPanel = targetFileLoader.getFileLoaderPanel();
        gba.add(cp, fileLoaderPanel, 0, 2, 1, 2, 1, 1, GBA.B, GBA.C);
        checkLoadEnable();
        cp.validate();
        selectedFileLoader.openDataPath();        
        cp.repaint();
    }
    
        /*
                Add the argument FileFilter to the FileFilter JComboBox
         */
    public void addFileFilter(FileFilter fileFilter) {
        if (fileFilter == null) return;
        fileFilterComboBox.addItem(fileFilter.getDescription());
    }
    
    public void addFileFilters(FileFilter[] fileFilters) {
        for (int i = 0; i < fileFilters.length; i++) {
            addFileFilter(fileFilters[i]);
        }
    }
    
    public void checkLoadEnable() {
        selectedFileLoader.checkLoadEnable();
    }
    
    public void onInfo() {
        HelpWindow hw = new HelpWindow(SuperExpressionFileLoader.this.getFrame(), "File Loader");
        if(hw.getWindowContent()){
            hw.setSize(450,650);
            hw.setLocation();
            hw.show();
        }
        else {
            hw.setVisible(false);
            hw.dispose();
        }
    }
    
    public void onCancel() {
        clean();
    }
    
    public void onLoad() {
        this.clean();
        
        Thread thread = new Thread(new Loader());
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        selectedFileLoader.showModal();
    }
    
    public void clean() {
        mainFrame.dispose();
        //More to clean up?
    }
    
    public JFrame getFrame(){
        return mainFrame;
    }
    
    public MultipleArrayViewer getArrayViewer(){
        return this.viewer;
    }
    
    
    public static void main(String[] args) {
        SuperExpressionFileLoader loader = new SuperExpressionFileLoader();
    }
    
    private ISlideData [] toISlideDataArray(Vector dataVector){
        if(dataVector == null || dataVector.size() < 1)
            return null;
        ISlideData [] data = new ISlideData[dataVector.size()];
        for(int i = 0; i < data.length; i++){
            data[i] = (ISlideData)(dataVector.elementAt(i));
        }
        return data;
    }

    
    private void updateDataPath(String  dataPath){
        if(dataPath == null)
            return;
        String renderedSep = "/";
        String renderedPath = new String();

        String sep = System.getProperty("file.separator");        
        String lineSep = System.getProperty("line.separator");

        StringTokenizer stok = new StringTokenizer(dataPath, sep);
        
        DATA_PATH = new String();

        String str;
        while(stok.hasMoreTokens() && stok.countTokens() > 1){
            str = stok.nextToken();
            renderedPath += str + renderedSep;            
            DATA_PATH += str + sep;
        }
        
        //Read tmev.cfg
        try{
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")+sep+"tmev.cfg"));

            String content = new String();
            String line;
            while( (line = br.readLine()) != null && !((line).equals("#DATA PATH"))){
                content += line+lineSep; 
            }
            
            if(line == null) {   //if at end of file
                content += lineSep;
                content += "#DATA PATH"+lineSep;
                content += "current-data-path "+renderedPath+lineSep;
            } else {            
            br.readLine(); //pass old path
            content += "#DATA PATH"+lineSep;
            content += "current-data-path "+renderedPath+lineSep;
            while( (line = br.readLine()) != null ){
                content += line+lineSep;
            }
            }
            
            
            BufferedWriter bfr = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+sep+"tmev.cfg"));
            bfr.write(content);
            bfr.flush();
            bfr.close();
            br.close();
            //if reset in file then update TMEV.dataPath;
            TMEV.setDataPath(DATA_PATH);
        } catch (IOException e){
            
        }
    }
    
    /*
     
        Member Classes
     
     */
    
    private class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            if (source == infoButton) {
                onInfo();
            } else if (source == cancelButton) {
                onCancel();
            } else if (source == loadButton) {
                onLoad();
            }
        }
    }
    
    /**
     * The class to allow run loading process in a separate thread.
     */
    private class Loader implements Runnable {
        
        public Loader() {
        }
        
        public void run() {
            Vector data = null;
            int dataType = 0;
            try {
                selectedFileLoader.showModal();
                data = selectedFileLoader.loadExpressionFiles();
                if(loaderIndex == 1)
                    dataType = IData.DATA_TYPE_RATIO_ONLY;
                else if(loaderIndex == 3) {
                    dataType = ((AffymetrixFileLoader)selectedFileLoader).getAffyDataType();
                } else
                    dataType = IData.DATA_TYPE_TWO_INTENSITY;
                selectedFileLoader.dispose();
                updateDataPath(selectedFileLoader.getFilePath());                
                if(data != null){
                    viewer.fireDataLoaded(toISlideDataArray(data), dataType);
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public class HeaderImagePanel extends JPanel{
        
        public HeaderImagePanel(){
            setLayout(new GridBagLayout());
            JLabel iconLabel = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("org/tigr/images/dialog_banner2.gif"))));
            iconLabel.setOpaque(false);
            iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            FillPanel fillPanel = new FillPanel();
            fillPanel.setBackground(Color.blue);
            add(iconLabel, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,5,0,0),0,0));
            add(fillPanel, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        }
        
    }
    
    public class FillPanel extends JPanel{
        GradientPaint gp;
        Color backgroundColor = new Color(25,25,169);
        Color fadeColor = new Color(140,220,240);
        public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            Dimension dim = this.getSize();
            //                gp = new GradientPaint(dim.width/2,0,backgroundColor,dim.width/2,dim.height/2,fadeColor);
            gp = new GradientPaint(0,dim.height/2,backgroundColor,dim.width,dim.height/2,fadeColor);
            g2.setPaint(gp);
            g2.fillRect(0,0,dim.width, dim.height);
            g2.setColor(Color.black);
        }
    }
}