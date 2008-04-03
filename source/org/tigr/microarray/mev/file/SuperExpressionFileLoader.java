/*
 Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
 All rights reserved.
 */
/*
 * $RCSfile: SuperExpressionFileLoader.java,v $
 * $Revision: 1.21 $
 * $Date: 2008-01-10 16:28:33 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindowDialog;
import org.tigr.microarray.mev.file.agilent.AgilentMevFileLoader;
import org.tigr.microarray.mev.file.agilent.AgilentMevFileLoader;

// Loads expression data in various file formats

public class SuperExpressionFileLoader {

	public static String DATA_PATH = TMEV.getDataPath();
	
	public final static ImageIcon ICON_COMPUTER = new ImageIcon(Toolkit
			.getDefaultToolkit().getImage(
					Thread.currentThread().getContextClassLoader()
							.getResource("org/tigr/images/PCIcon.gif")));
	
	public final static ImageIcon ICON_DISK = new ImageIcon(Toolkit
			.getDefaultToolkit().getImage(
					Thread.currentThread().getContextClassLoader()
							.getResource("org/tigr/images/disk.gif")));

	public final static ImageIcon ICON_FOLDER = new ImageIcon(Toolkit
			.getDefaultToolkit().getImage(
					Thread.currentThread().getContextClassLoader()
							.getResource("org/tigr/images/Directory.gif")));

	public final static ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(Toolkit
			.getDefaultToolkit().getImage(
					Thread.currentThread().getContextClassLoader()
							.getResource("org/tigr/images/expandedfolder.gif")));

	protected ExpressionFileLoader[] fileLoaders;

	protected ExpressionFileLoader selectedFileLoader;

	protected FileFilter[] fileFilters;

	protected FileFilter selectedFileFilter;

	protected JFrame mainFrame;

	protected JPanel fileFilterPanel;

	protected JLabel fileFilterLabel;
	//Added by Sarita
	protected JLabel fileType;
	protected JComboBox fileTypeList;
	//
	
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
	protected JMenuBar menuBar;
	protected JMenu menu1,menu2, menu3, menu4;
	//Added by Sarita
	protected JMenu helpMenu;
	//
	protected JMenuItem menuItem[];
	protected JMenuItem subMenuItem[];
	protected JTextField filetype=null;
	
	public SuperExpressionFileLoader(MultipleArrayViewer viewer) {
	

		this.viewer = viewer;
		loader = new Loader();
		initializeDataPath();
		initializeFileLoaders();
		initializeGUI();
	}

	public SuperExpressionFileLoader() {
		
		loader = new Loader();
		initializeFileLoaders();
		initializeGUI();
	}

	protected void initializeFileLoaders() {

		int defaultSelection = 0;

		//Added by Sarita: Changed file loaders from 13 to 14
		fileLoaders = new ExpressionFileLoader[15];
		
		fileLoaders[0] = new StanfordFileLoader(this);
		
		fileLoaders[1] = null;
		fileLoaders[2] = null;
		fileLoaders[3] = null;
		fileLoaders[4] = null;
		fileLoaders[5] = null;
		fileLoaders[6] = null;
		fileLoaders[7] = null;
		fileLoaders[8] = null;
		fileLoaders[9] = null;
		fileLoaders[10] = null;
		fileLoaders[11] = null; /* Raktim, CGH Loader */
		fileLoaders[12] = null;
		//Added by Sarita
		fileLoaders[13] = null;
		fileLoaders[14] = null;
		
		selectedFileLoader = fileLoaders[defaultSelection];

		fileFilters = new FileFilter[fileLoaders.length];
		fileFilters[0] = fileLoaders[0].getFileFilter();
		selectedFileFilter = fileFilters[defaultSelection];
	}
	public void helpWindow(String st){
		HelpWindow hw = new HelpWindow(mainFrame, st);
		if (hw.getWindowContent()) {
			hw.setSize(750, 650);
			hw.setLocation();
			hw.show();
		} else {
			hw.setVisible(false);
			hw.dispose();
		}	
	}
	public void menuItem(JMenu jItem,final String st){
		menuItem=new JMenuItem[6];
		menuItem[0]= new JMenuItem("Tab Delimited, Multiple Sample Files");
		
		menuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					helpWindow("TDMS");
				}else{
				changeSelectedFileFilterAndLoader(0);
				
				}
			}
		});
		jItem.add(menuItem[0]);
		
		menuItem[1]= new JMenu("TIGR Files");
		subMenuItem= new JMenuItem[2];
		subMenuItem[0]=new JMenuItem("MeV Files");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
				  HelpWindowDialog hwd= new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("Mev"));
				}else{
				changeSelectedFileFilterAndLoader(1);
				//filetype.setText("MeV Files(*.mev)");
				}
			}
		});
		menuItem[1].add(subMenuItem[0]);
		
		subMenuItem[1]=new JMenuItem("TIGR ArrayViewer (*.tav) Files");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("Tav"));
					}else{
				changeSelectedFileFilterAndLoader(2);
				//filetype.setText("TIGR ArrayViewer Files(*.tav)");
			}
			}
		});
		menuItem[1].add(subMenuItem[1]);
		
		jItem.add(menuItem[1]);
		
		menuItem[2] = new JMenu("Affymetrix Files");
		
        subMenuItem= new JMenuItem[5];
        subMenuItem[0]=new JMenuItem("Affymetrix GCOS(using MAS5)Files");
        subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("GCOS");
					}else{
				changeSelectedFileFilterAndLoader(3);
				//filetype.setText("Affymetrix GCOS(using MAS5)Files");
					}
			}
		});
		menuItem[2].add(subMenuItem[0]);

		subMenuItem[1] = new JMenuItem("dChip/DFCI_Core Format Files");
		menuItem[2].add(subMenuItem[1]);
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("dChip");
					}else{
				changeSelectedFileFilterAndLoader(4);
				//filetype.setText("dChip/DFCI_Core Format Files");
			}
			}
		});
		
		subMenuItem[2] = new JMenuItem("GW Affymetrix Files");
		menuItem[2].add(subMenuItem[2]);
		subMenuItem[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("GW");
					}else{
				changeSelectedFileFilterAndLoader(5);
				//filetype.setText("GW Affymetrix Files");
			}
			}
		});
		
		subMenuItem[3] = new JMenuItem("Bioconductor(using MAS5) Files");
		menuItem[2].add(subMenuItem[3]);
		subMenuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("bioconductor");
				}else{
				changeSelectedFileFilterAndLoader(6);
				//filetype.setText("Bioconductor(using MAS5) Files");
			}
		}
		});
		subMenuItem[4] = new JMenuItem("RMA Files");
		menuItem[2].add(subMenuItem[4]);
		subMenuItem[4].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("RMA");
				}else{
				changeSelectedFileFilterAndLoader(7);
				//filetype.setText("RMA Files");
			}
		}
		});
		
		jItem.add(menuItem[2]);
	
		menuItem[3] = new JMenuItem("CGH Tab Delimited, Multiple Sample");
		menuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					  helpWindow("CGH");
				}else{
				changeSelectedFileFilterAndLoader(8);
				//filetype.setText("CGH Files");
			}
		}
		});
		jItem.add(menuItem[3]);
		
		menuItem[4]= new JMenu("GEO Files");
		//Added by Sarita:Submenu items increased to 4
		subMenuItem= new JMenuItem[4];
		subMenuItem[0]=new JMenuItem("GPL Family Format Files (Affymetrix)");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("GEOaffy"));
				}else{
				changeSelectedFileFilterAndLoader(9);
				//filetype.setText("GEO SOFT Affymetrix Format Files");
			}
			}
		});
		menuItem[4].add(subMenuItem[0]);
		
		subMenuItem[1]=new JMenuItem("GPL Family Format Files (Two Channel)");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("GEOtwo"));
				}else{
				changeSelectedFileFilterAndLoader(10);
				//filetype.setText("GEO SOFT Two Channel Format Files");
			}
		}
		});
		menuItem[4].add(subMenuItem[1]);
		
	//Added by Sarita	
		
		subMenuItem[2]=new JMenuItem("GEO Series Matrix Files");
		subMenuItem[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("GEOSeriesMatrix"));
				}else{
				changeSelectedFileFilterAndLoader(13);
				//filetype.setText("GEO SOFT Two Channel Format Files");
			}
		}
		});
		menuItem[4].add(subMenuItem[2]);
	
		subMenuItem[3]=new JMenuItem("GEO GDS Format Files");
		subMenuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("GEO GDS Format Files"));
				}else{
				changeSelectedFileFilterAndLoader(14);
				//filetype.setText("GEO SOFT Two Channel Format Files");
			}
		}
		});
		menuItem[4].add(subMenuItem[3]);
		
		jItem.add(menuItem[4]);
		
		menuItem[5]= new JMenu("Other Format Files");
		
		subMenuItem= new JMenuItem[2];
		subMenuItem[0]=new JMenuItem("GenePix Format Files");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("GenePix"));
				}else{
				changeSelectedFileFilterAndLoader(11);
			//	filetype.setText("GenePix Format Files");
			}
		}
		});
		menuItem[5].add(subMenuItem[0]);
		
		subMenuItem[1]=new JMenuItem("Agilent Files");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(st=="File Format Hint"){
					HelpWindowDialog hwd =new HelpWindowDialog(mainFrame,HelpWindowDialog.createText("Agilent"));
				}else{
				changeSelectedFileFilterAndLoader(12);
				//filetype.setText("Agilent Format Files");
			}
		}
		});
		menuItem[5].add(subMenuItem[1]);
		
		jItem.add(menuItem[5]);
		
	}
	public void initializeGUI() {

		gba = new GBA();
		eventListener = new EventListener();

		mainFrame = new JFrame("Expression File Loader");
		//mainFrame.getContentPane().setBackground(new Color(220, 220, 220));
		mainFrame.getContentPane().setLayout(new GridBagLayout());

		menuBar = new JMenuBar();
		
	//Commented by Sarita	
		menu1 = new JMenu("Select File Loader");
		menu1.setMnemonic(KeyEvent.VK_S);
		menuItem(menu1,"Select Expression File Type");
		menuBar.add(menu1);
		
	/*	menu3 = new JMenu("  ");
		menu3.setMnemonic(KeyEvent.VK_S);
		menuItem(menu3,"Select Expression File Type");
		menuBar.add(menu1);
		
		
		menu4 = new JMenu("  ");
		menu4.setMnemonic(KeyEvent.VK_S);
		menuItem(menu4,"Select Expression File Type");
		menuBar.add(menu1);*/
		
		
		menu2 = new JMenu("Help");
		menu2.setMnemonic(KeyEvent.VK_H);
		menuItem(menu2,"File Format Hint");
		menuBar.add(menu2);
		menuBar.setBorderPainted(true);
		mainFrame.setJMenuBar(menuBar);
		
		
		fileFilterLabel=new JLabel();
	
			
		fileLoaderPanel = selectedFileLoader.getFileLoaderPanel();
	//	fileLoaderPanel.setSize(new Dimension(600, 600));// commented by sarita, temporarily
		//fileLoaderPanel.setPreferredSize(new Dimension(600, 600));// commented temporarily by sarita
		//fileLoaderPanel.setSize(new Dimension(650, 650)); //Works except when you add more stuff to AnnotationPanel
		//fileLoaderPanel.setPreferredSize(new Dimension(650, 650)); 
		
		fileLoaderPanel.setSize(new Dimension(750, 750)); 
		fileLoaderPanel.setPreferredSize(new Dimension(750, 750)); 
		
        // begin additions by Dan
	
        URL url = this.getClass().getClassLoader().getResource("org/tigr/images/Information24.gif");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        Icon imageIcon = new ImageIcon(image);
        infoButton = new JButton(null, imageIcon);
		// end additions by Dan
		infoButton.setActionCommand("info-command");
		infoButton.addActionListener(eventListener);
		infoButton.setFocusPainted(false);
		infoButton.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.RAISED));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(eventListener);
		cancelButton.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.RAISED));
		cancelButton.setSize(60, 30);
		cancelButton.setPreferredSize(new Dimension(60, 30));
		cancelButton.setFocusPainted(false);
		loadButton = new JButton("Load");
		loadButton.addActionListener(eventListener);
		loadButton.setBorder(BorderFactory.createBevelBorder(
				BevelBorder.RAISED, new Color(240, 240, 240), new Color(180,
						180, 180), new Color(10, 0, 0), new Color(10, 10, 10)));
		loadButton.setSize(60, 30);
		loadButton.setPreferredSize(new Dimension(60, 30));
		loadButton.setFocusPainted(false);
		loadButton.setEnabled(false);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		gba.add(buttonPanel, infoButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);


        // begin additions by Dan:
        url = this.getClass().getClassLoader().getResource("org/tigr/images/dialog_button_bar.gif");
        image = Toolkit.getDefaultToolkit().getImage(url);
        imageIcon = new ImageIcon(image);
        JLabel myLbl = new JLabel(imageIcon);
		// end additions by Dan 

        gba
				.add(
						buttonPanel,
                        myLbl,
						1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5),
						0, 0);
		gba.add(buttonPanel, cancelButton, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);

		gba.add(buttonPanel, loadButton, 3, 0, 1, 1, 0, 0, GBA.N, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);

		//gba.add(mainFrame.getContentPane(), header, 0, 0, 1, 1, 1, 0, GBA.H,
			//	GBA.C);
		//Check why this configuration works fine for TDMS but NOT for RMA
	//	gba.add(mainFrame.getContentPane(), fileFilterPanel, 0, 0, 1, 1, 1, 1,
		//		GBA.B, GBA.C);
		
	//	gba.add(mainFrame.getContentPane(), fileFilterLabel, 0, 0, 1, 1, 1, 0,
		//		GBA.H, GBA.C);///-----------commented temporarily by sarita
		gba.add(mainFrame.getContentPane(), fileLoaderPanel, 0, 1, 1, 3, 1, 1,
				GBA.B, GBA.C);
		gba.add(mainFrame.getContentPane(), buttonPanel, 0, 4, 1, 1, 1, 0,
				GBA.H, GBA.C);

		//mainFrame.setSize(1000, 780); //Original size, Commented by sarita
		
		//Commented temporarily--Dec 08,07.
		//mainFrame.setSize(1000, 840);//Stops GUI shaking when trying to add stuff to Annotation panel
		mainFrame.setSize(800, 680);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		//Added by Sarita
		Dimension frameSize = mainFrame.getSize();
		
		if (frameSize.height > screenSize.height) {
			   frameSize.height = screenSize.height;
			}
		if (frameSize.width > screenSize.width) {
			   frameSize.width = screenSize.width;
		}
		//		
		
		mainFrame.setLocation(
				(screenSize.width - mainFrame.getSize().width) / 2,
				(screenSize.height - mainFrame.getSize().height) / 2);
		
		
	
		mainFrame.setVisible(true);
		selectedFileLoader.openDataPath();
	}

	public void initializeDataPath() {
		String newPath = TMEV.getDataPath();
		newPath = (new File(newPath)).getPath();

		if (newPath == null) {
			return;
		}

		String sep = System.getProperty("file.separator");

		// if Linux or Mac / goes in front of the path
		if (sep.equals("/"))
			newPath = sep + newPath;

		File file = new File(newPath);
		if (file.exists()) {
			DATA_PATH = newPath;
		} else {
			file = TMEV.getFile("/data");
			if (file != null)
				DATA_PATH = file.getPath();
		}
	}

	public void setLoadEnabled(boolean state) {
		loadButton.setEnabled(state);
	}

	private ExpressionFileLoader getFileLoader(int target) {

		ExpressionFileLoader loader;
		
		if (target >= 0 && target < fileLoaders.length
				&& fileLoaders[target] != null) {
			
			return fileLoaders[target];
		}

		setLoadEnabled(false);
		
		switch (target) {
		case 0:
			loader = fileLoaders[0];
			break;
		case 1:
			loader = new MevFileLoader(this);
			break;
		case 2:
			loader = new TavFileLoader(this);
			break;
		case 3:
			loader = new AffyGCOSFileLoader(this);
			break;
		case 4:
			loader = new DFCI_CoreFileLoader(this);
			break;
		case 5:
			loader = new AffymetrixFileLoader(this);
			break;
		
		case 6: 	
			loader = new Mas5FileLoader(this);
			break;
		
		case 7:	//wwang for RMA
			loader =new RMAFileLoader(this);
			break;	
		case 8:
			 //loader = new CGHStanfordFileLoader(this); /* Raktim, for CGH Loader */
			 loader = new CGHStanfordFileLoader(this);
			 break;		
		case 9:
			
			//loader = new SOFT_AffyFileLoader(this);
			loader = new SOFT_AffymetrixFileLoader(this);
			break;	
		case 10:
			loader = new SOFT_TwoChannelFileLoader(this);
			break;	
	
		case 11:
			loader = new GenePixFileLoader(this);
			break;
		case 12:
			loader = new AgilentMevFileLoader(this);
			break;
		case 13:
			loader = new GEOSeriesMatrixLoader(this);
			break;	
		case 14:
			loader = new GEO_GDSFileLoader(this);
			break;
		default:
			loader = new StanfordFileLoader(this);
			break;
		}
		fileLoaders[target] = loader;
		return loader;
	}

	public void changeSelectedFileFilterAndLoader(int target) {

		if (target < 0 || target >= fileLoaders.length
				|| target >= fileFilters.length)
			return;

		selectedFileLoader = getFileLoader(target);
		
		this.mainFrame.toFront();
		fileFilters[target] = selectedFileLoader.getFileFilter();
		selectedFileFilter = fileFilters[target];
		loaderIndex = target;
		changeFileLoaderPanel(selectedFileLoader);
	}

	public void changeFileLoaderPanel(ExpressionFileLoader targetFileLoader) {

		Container cp = mainFrame.getContentPane();
		
		cp.remove(fileLoaderPanel); // Remove the old fileLoaderPanel
		fileLoaderPanel = targetFileLoader.getFileLoaderPanel();
		
		gba.add(cp, fileLoaderPanel, 0, 1, 1, 3, 1, 1, GBA.B, GBA.C);
		checkLoadEnable();
		cp.validate();
		selectedFileLoader.openDataPath();
		cp.repaint();
	}

	/*
	 * Add the argument FileFilter to the FileFilter JComboBox
	 */
	public void addFileFilter(FileFilter fileFilter) {
		if (fileFilter == null) {
			return;
		}
		fileFilterComboBox.addItem(fileFilter.getDescription());
	}

	public void addFileFilters(FileFilter[] fileFilters) {
		for (int i = 0; i < fileFilters.length; i++) {
			fileFilterComboBox.addItem(getFileDescription(i));
		}
	}

	
	
	
	public String getFileDescription(int target) {
		String desc;
		switch (target) {
		case 0:
			desc = "Tab Delimited, Multiple Sample Files (TDMS) (*.*)";
			break;
		case 1:
			desc = "MeV Files (*.mev and *.ann)";
			break;
		case 2:
			desc = "TIGR ArrayViewer Files (*.tav)";
			break;
		case 3:
			desc = "Affymetrix GCOS(using MAS5) Files";
			break;
		case 4:
			desc = "dChip/DFCI_Core Format Files";
			break;
		case 5:
			desc = "GW Affymetrix Files";
			break;
			
		case 6:
			desc ="Bioconductor(using MAS5) Files";
			break;
				
		case 7:
			desc = "RMA Files";
			break;	
		case 8:
			desc = "CGH Tab Delimited, Multiple Sample";
			break;
		case 9:
			desc = "GEO SOFT Affymetrix Format Files";
			break;		
		case 10:
			desc =  "GEO SOFT Two Channel Format Files";
			break;			
        case 11:
            desc = "GenePix Format Files";
            break;
        case 12:
            desc =  "Agilent Files";
        case 13:
            desc =  "GEO Series Matrix Files";
              
            break;
        case 14:
            desc =  "GEO GDS Format Files";
            break;
		default:
			desc = "Tab Delimited, Multiple Sample Files (TDMS) (*.*)";
			
			break;
		}
		return desc;
	}

	public void checkLoadEnable() {
		selectedFileLoader.checkLoadEnable();
	}
	
	
	//Added by Sarita
	 public void onSelectingFileType(Object selectedItem) {
     	//String sft=(String)fileTypeList.getSelectedItem();
     	String sft=(String)selectedItem;
     	if(sft.equalsIgnoreCase("Tab Delimited, Multiple Sample")) {
     		changeSelectedFileFilterAndLoader(0);
     	}
     	
     	if(sft.equalsIgnoreCase("MeV")) {
     		changeSelectedFileFilterAndLoader(1);
     	}
     	
     	if(sft.equalsIgnoreCase("TIGR Array Viewer (*.tav)")) {
     		changeSelectedFileFilterAndLoader(2);
     	}
     	
    	if(sft.equalsIgnoreCase("Affymetrix GCOS(using MAS5) Files")) {
     		changeSelectedFileFilterAndLoader(3);
     	}
     	
    	if(sft.equalsIgnoreCase("dChip/DFCI_Core Format Files")) {
     		changeSelectedFileFilterAndLoader(4);
     	}
    	
    	if(sft.equalsIgnoreCase("GW Affymetrix Files")) {
     		changeSelectedFileFilterAndLoader(5);
     	} 	
    	          	
     	if(sft.equalsIgnoreCase("Bioconductor(using MAS5) Files")) {
     		changeSelectedFileFilterAndLoader(6);
     	}
     	
     	if(sft.equalsIgnoreCase("RMA Files")) {
     		changeSelectedFileFilterAndLoader(7);
     	}
     	
     	if(sft.equalsIgnoreCase("CGH Tab Delimited, Multiple Sample")) {
     		changeSelectedFileFilterAndLoader(8);
     	}
    	
       	if(sft.equalsIgnoreCase("GEO SOFT Affymetrix Format Files")) {
     		changeSelectedFileFilterAndLoader(9);
     	}
     	
    	if(sft.equalsIgnoreCase("GEO SOFT Two Channel Format Files")) {
     		changeSelectedFileFilterAndLoader(10);
     	}
    	
    	
    	if(sft.equalsIgnoreCase("GenePix Format Files")) {
     		changeSelectedFileFilterAndLoader(11);
     	}
     	
    
     	if(sft.equalsIgnoreCase("Agilent Files")) {
     		changeSelectedFileFilterAndLoader(12);
     	}
     	
    	if(sft.equalsIgnoreCase("GEO Series Matrix Files")) {
     		changeSelectedFileFilterAndLoader(13);
     	}
    	
    	if(sft.equalsIgnoreCase("GEO GDS Format Files")) {
     		changeSelectedFileFilterAndLoader(14);
     	}
 	}
     
  

	public void onInfo() {
		HelpWindow hw = new HelpWindow(SuperExpressionFileLoader.this
				.getFrame(), "File Loader");
		if (hw.getWindowContent()) {
			hw.setSize(450, 650);
			hw.setLocation();
			hw.show();
		} else {
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
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
		selectedFileLoader.showModal();
	}

	public void clean() {
		mainFrame.dispose();
		// More to clean up?
	}

	public JFrame getFrame() {
		return this.viewer.getFrame();
	}

	public MultipleArrayViewer getArrayViewer() {
		return this.viewer;
	}

	//main mETHOD COMMENTED BY sARITA
	/*public static void main(String[] args) {
		SuperExpressionFileLoader loader = new SuperExpressionFileLoader();
	}*/

	private ISlideData[] toISlideDataArray(Vector dataVector) {
		if (dataVector == null || dataVector.size() < 1)
			return null;
		ISlideData[] data = new ISlideData[dataVector.size()];
	
		for (int i = 0; i < data.length; i++) {
			data[i] = (ISlideData) (dataVector.elementAt(i));
		}
		return data;
	}

	private void updateDataPath(String dataPath) {
		
		
		if (dataPath == null)
			return;
		String renderedSep = "/";
		String renderedPath = new String();

		String sep = System.getProperty("file.separator");
		String lineSep = System.getProperty("line.separator");

		StringTokenizer stok = new StringTokenizer(dataPath, sep);

		DATA_PATH = new String();

		String str;
		while (stok.hasMoreTokens() && stok.countTokens() > 1) {
			str = stok.nextToken();
			renderedPath += str + renderedSep;
			DATA_PATH += str + sep;
		}
		// sets the data path in config to render well
		TMEV.updateDataPath(renderedPath);

		// sets variable to conform to OS spec.
		TMEV.setDataPath(DATA_PATH);
	}

	/*
	 * 
	 * Member Classes
	 * 
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
		//	System.out.println("Run");
			Vector data = null;
			int dataType = 0;
			try {
				selectedFileLoader.showModal();
				data = selectedFileLoader.loadExpressionFiles();
				if (loaderIndex == 10 || loaderIndex == 8||loaderIndex==7) /* Raktim, added check for 8, CGH Data */
					dataType = IData.DATA_TYPE_RATIO_ONLY;
				else if(loaderIndex == 6){
					//dataType = ((Mas5FileLoader)selectedFileLoader)
					//.getAffyDataType();
					dataType = ((Mas5FileLoader)selectedFileLoader)
						.getAffyDataType();
				}else if(loaderIndex==0){
					dataType = ((StanfordFileLoader)selectedFileLoader)
					.getDataType();
					
					
				}	else if (loaderIndex == 5) {
				//	dataType = ((AffymetrixFileLoader) selectedFileLoader)
					//		.getAffyDataType();
					dataType = ((AffymetrixFileLoader) selectedFileLoader)
					.getAffyDataType();
				}
				else if (loaderIndex == 3) {
					dataType = ((AffyGCOSFileLoader) selectedFileLoader)
					.getAffyDataType();
									
					//dataType = ((AffyGCOSFileLoader) selectedFileLoader)
						//	.getAffyDataType();
				}else if (loaderIndex == 9) {
					dataType = ((SOFT_AffymetrixFileLoader) selectedFileLoader)
							.getDataType();
									
					
				}else if (loaderIndex == 4) {
					//dataType = ((DFCI_CoreFileLoader) selectedFileLoader)
					//.getAffyDataType();	
					dataType = ((DFCI_CoreFileLoader) selectedFileLoader)
					.getAffyDataType();	
								
	
				}else if (loaderIndex == 13) {
				 dataType = ((GEOSeriesMatrixLoader) selectedFileLoader)
					.getDataType();	
					
	
				}else if (loaderIndex == 14) {
					dataType = ((GEO_GDSFileLoader) selectedFileLoader)
					.getDataType();
								
	
				}else 
					dataType = IData.DATA_TYPE_TWO_INTENSITY;
				selectedFileLoader.dispose();
				
				updateDataPath(selectedFileLoader.getFilePath());
				if (data != null) {
					viewer.fireDataLoaded(toISlideDataArray(data), dataType);					
				}
			} catch (Exception ioe) {
			
				ioe.printStackTrace();
				ioe.getCause();
			}
		}
	}

	public class HeaderImagePanel extends JPanel {

		public HeaderImagePanel() {
			setLayout(new GridBagLayout());
			JLabel iconLabel = new JLabel(
					new ImageIcon(
							Toolkit
									.getDefaultToolkit()
									.getImage(// changed by dan:
											this.getClass().getClassLoader()
													.getResource("org/tigr/images/dialog_banner2.gif"))));
			iconLabel.setOpaque(false);
			iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			FillPanel fillPanel = new FillPanel();
			fillPanel.setBackground(Color.blue);
			add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(0, 5, 0, 0), 0, 0));
			add(fillPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}

	}

	public class FillPanel extends JPanel {
		GradientPaint gp;

		Color backgroundColor = new Color(25, 25, 169);

		Color fadeColor = new Color(140, 220, 240);

		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Dimension dim = this.getSize();
			// gp = new
			// GradientPaint(dim.width/2,0,backgroundColor,dim.width/2,dim.height/2,fadeColor);
			gp = new GradientPaint(0, dim.height / 2, backgroundColor,
					dim.width, dim.height / 2, fadeColor);
			g2.setPaint(gp);
			g2.fillRect(0, 0, dim.width, dim.height);
			g2.setColor(Color.black);
		}
	}
}