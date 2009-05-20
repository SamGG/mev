/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SuperExpressionFileLoader.java,v $
 * $Revision: 1.21 $
 * $Date: 2008-01-10 16:28:33 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindowDialog;
import org.tigr.microarray.mev.file.agilent.AgilentMevFileLoader;
import org.tigr.microarray.mev.resources.AvailableAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.ExpressionDataSupportDataFile;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

// Loads expression data in various file formats

public class SuperExpressionFileLoader {

	public static String DATA_PATH = TMEV.getDataPath();

	public static String ANNOTATION_PATH = TMEV.getSettingForOption("current-annotation-path");

	public final static ImageIcon ICON_COMPUTER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
			Thread.currentThread().getContextClassLoader().getResource("org/tigr/images/PCIcon.gif")));

	public final static ImageIcon ICON_DISK = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
			Thread.currentThread().getContextClassLoader().getResource("org/tigr/images/disk.gif")));

	public final static ImageIcon ICON_FOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
			Thread.currentThread().getContextClassLoader().getResource("org/tigr/images/Directory.gif")));

	public final static ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
			Thread.currentThread().getContextClassLoader().getResource("org/tigr/images/expandedfolder.gif")));

	protected ExpressionFileLoader[] fileLoaders;
	protected ExpressionFileLoader selectedFileLoader;
	protected FileFilter[] fileFilters;
	protected FileFilter selectedFileFilter;
	protected JFrame mainFrame;
	protected JPanel fileFilterPanel;
	protected JLabel fileFilterLabel;
	protected JLabel fileType;
	protected JComboBox fileTypeList;
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
	protected JMenu menu1, menu2, menu3, menu4;
	protected JMenu helpMenu;
	protected JMenuItem menuItem[];
	protected JMenuItem subMenuItem[];
	protected JTextField filetype=null;
	protected boolean hasAnnotationList = false;
	protected Hashtable<String, Vector<String>> annotationLists; 
	protected String defaultSpeciesName;
	protected String defaultArrayName;

	public SuperExpressionFileLoader(MultipleArrayViewer viewer, FileLoadInfo loadingInfo) {
		this(viewer);
		
		FileType ft = loadingInfo.getFileType();
		ExpressionFileLoader expressionLoader = getFileLoader(ft.getLoaderIndex());
		String arrayType = loadingInfo.getArrayType();
        String speciesName = getSpeciesName(loadingInfo.getArrayType());
        String fileURL = loadingInfo.getDataFileURL();
        boolean isMultiFile = loadingInfo.isMultiFile();
        File dataFile = new File(TMEV.getDataPath());
        
        if(loadingInfo.isDownloadDatafile()) {
	        try {
	        	dataFile = viewer.getSupportFile(new ExpressionDataSupportDataFile(fileURL, isMultiFile, ft));
	        } catch (SupportFileAccessError sfae) {
	        	ShowThrowableDialog.show(mainFrame, 
	        			"File download failed", 
	        			true, 
	        			ShowThrowableDialog.ERROR, 
	        			sfae, 
	        			"MeV was unable to download the file " + fileURL + 
	        			"<br/><br/>Please check that this is a valid URL and that " +
	        			"your computer is connected to the internet.");
	        	dataFile = null;
	        }
        }
        
        if(arrayType != null && !arrayType.equals("")) {
	        try {
	        	File annotationFile = getAnnotationFile(speciesName, arrayType);
	    		expressionLoader.setAnnotationFilePath(annotationFile.getAbsolutePath());
	        } catch (SupportFileAccessError sfae) {
	        	ShowThrowableDialog.show(mainFrame, 
	        			"File download failed", 
	        			true, 
	        			ShowThrowableDialog.ERROR, 
	        			sfae, 
	        			"MeV was unable to download the annotation file for the specified array \"" + arrayType + 
	        			"\".<br/><br/>The array name may be invalid or your computer may not be connected to the internet.");
	        }
        }
        
        if(dataFile != null) {
	        expressionLoader.setCoordinates(loadingInfo.getFirstRow(), loadingInfo.getFirstColumn());
	        
	        if (expressionLoader.canAutoLoad(dataFile)) {
	            selectedFileLoader = expressionLoader;
	            mainFrame.setVisible(false);
				setFilePath(dataFile.getAbsolutePath());
	            loader.run();
	        } else {
				changeSelectedFileFilterAndLoader(ft.getLoaderIndex());
				setFilePath(dataFile.getAbsolutePath());
	        }
        }
	}

	public SuperExpressionFileLoader(MultipleArrayViewer viewer) {
		this.viewer = viewer;
		this.defaultArrayName = TMEV.getSettingForOption(TMEV.LAST_LOADED_ARRAY);
		this.defaultSpeciesName = TMEV.getSettingForOption(TMEV.LAST_LOADED_SPECIES);
		hasAnnotationList = initializeAnnotationInfo();
		loader = new Loader();
		initializeFileLoaders();
		initializeGUI();
	}
	//Dan's TimeSaver
	public SuperExpressionFileLoader(MultipleArrayViewer viewer, boolean tf, String path) {
		this.viewer = viewer;
		this.defaultArrayName = TMEV.getSettingForOption(TMEV.LAST_LOADED_ARRAY);
		this.defaultSpeciesName = TMEV.getSettingForOption(TMEV.LAST_LOADED_SPECIES);
		hasAnnotationList = initializeAnnotationInfo();
		loader = new Loader();
		initializeFileLoaders();
		initializeGUI();
		((StanfordFileLoader)fileLoaders[0]).dansTimeSaver(path);
		onLoad();
		
	}
	public SuperExpressionFileLoader() {
		loader = new Loader();
		initializeFileLoaders();
		initializeGUI();
		hasAnnotationList = initializeAnnotationInfo();
	}

	private String getSpeciesName(String arrayType){
		if(arrayType == null)
			return null;
		Enumeration<String> allAnnTypes = annotationLists.keys();
		while(allAnnTypes.hasMoreElements()) {
			String thisSpecies = allAnnTypes.nextElement();
			Vector<String> theseArrays = annotationLists.get(thisSpecies);
			for(int i=0; i<theseArrays.size(); i++) {
				if(arrayType.equals(theseArrays.get(i))) {
						return thisSpecies;
				}
			}
		}
		return null;
	}
	private boolean initializeAnnotationInfo() {
		try {
			AvailableAnnotationsFileDefinition aafd = new AvailableAnnotationsFileDefinition();
			File f = viewer.getSupportFile(aafd);
			this.annotationLists = aafd.parseAnnotationListFile(f);
			return true;
		} catch (SupportFileAccessError sfae) {
			this.annotationLists = null;
			return false;
		} catch (IOException ioe) {
			this.annotationLists = null;
			return false;
		} catch (Exception e) {
			this.annotationLists = null;
			return false;
		}
	}
	public File getAnnotationFile(String organismName, String arrayName) throws SupportFileAccessError {
		return viewer.getSupportFile(new ResourcererAnnotationFileDefinition(organismName, arrayName));
	}


	protected void initializeFileLoaders() {
		int defaultSelection = 0;

		fileLoaders = new ExpressionFileLoader[16];
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
		fileLoaders[11] = null;
		fileLoaders[12] = null;
		fileLoaders[13] = null;
		fileLoaders[14] = null;
		fileLoaders[15] = null;
		
		selectedFileLoader = fileLoaders[defaultSelection];

		fileFilters = new FileFilter[fileLoaders.length];
		fileFilters[0] = fileLoaders[0].getFileFilter();
		selectedFileFilter = fileFilters[defaultSelection];
	}

	public void helpWindow(String st) {
		HelpWindow hw = new HelpWindow(mainFrame, st);
		if (hw.getWindowContent()) {
			hw.setSize(750, 650);
			hw.setLocation();
			hw.setVisible(true);
		} else {
			hw.setVisible(false);
			hw.dispose();
		}
	}

	public void menuItem(JMenu jItem, final String st) {
		menuItem = new JMenuItem[7];
		menuItem[0] = new JMenuItem("Tab Delimited, Multiple Sample Files");

		menuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("TDMS");
				} else {
					changeSelectedFileFilterAndLoader(0);

				}
			}
		});
		jItem.add(menuItem[0]);

		menuItem[1] = new JMenu("TIGR Files");
		subMenuItem = new JMenuItem[2];
		subMenuItem[0] = new JMenuItem("MeV Files");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("Mev"));
				} else {
					changeSelectedFileFilterAndLoader(1);
				}
			}
		});
		menuItem[1].add(subMenuItem[0]);

		subMenuItem[1] = new JMenuItem("TIGR ArrayViewer (*.tav) Files");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("Tav"));
				} else {
					changeSelectedFileFilterAndLoader(2);
				}
			}
		});
		menuItem[1].add(subMenuItem[1]);

		jItem.add(menuItem[1]);

		menuItem[2] = new JMenu("Affymetrix Files");

		subMenuItem = new JMenuItem[5];
		subMenuItem[0] = new JMenuItem("Affymetrix GCOS(using MAS5)Files");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("GCOS");
				} else {
					changeSelectedFileFilterAndLoader(3);
				}
			}
		});
		menuItem[2].add(subMenuItem[0]);

		subMenuItem[1] = new JMenuItem("dChip/DFCI_Core Format Files");
		menuItem[2].add(subMenuItem[1]);
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("dChip");
				} else {
					changeSelectedFileFilterAndLoader(4);
				}
			}
		});

		subMenuItem[2] = new JMenuItem("GW Affymetrix Files");
		menuItem[2].add(subMenuItem[2]);
		subMenuItem[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("GW");
				} else {
					changeSelectedFileFilterAndLoader(5);
				}
			}
		});

		subMenuItem[3] = new JMenuItem("Bioconductor(using MAS5) Files");
		menuItem[2].add(subMenuItem[3]);
		subMenuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("bioconductor");
				} else {
					changeSelectedFileFilterAndLoader(6);
				}
			}
		});
		subMenuItem[4] = new JMenuItem("RMA Files");
		menuItem[2].add(subMenuItem[4]);
		subMenuItem[4].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("RMA");
				} else {
					changeSelectedFileFilterAndLoader(7);
				}
			}
		});

		jItem.add(menuItem[2]);

		menuItem[3] = new JMenuItem("CGH Tab Delimited, Multiple Sample");
		menuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("CGH");
				} else {
					changeSelectedFileFilterAndLoader(8);
				}
			}
		});
		jItem.add(menuItem[3]);

		menuItem[4] = new JMenu("GEO Files");
		subMenuItem = new JMenuItem[4];
		subMenuItem[0] = new JMenuItem("GPL Family Format Files (Affymetrix)");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("GEOaffy"));
				} else {
					changeSelectedFileFilterAndLoader(9);
				}
			}
		});
		menuItem[4].add(subMenuItem[0]);

		subMenuItem[1] = new JMenuItem("GPL Family Format Files (Two Channel)");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("GEOtwo"));
				} else {
					changeSelectedFileFilterAndLoader(10);
				}
			}
		});
		menuItem[4].add(subMenuItem[1]);

		subMenuItem[2] = new JMenuItem("GEO Series Matrix Files");
		subMenuItem[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("GEOSeriesMatrix"));
				} else {
					changeSelectedFileFilterAndLoader(13);
				}
			}
		});
		menuItem[4].add(subMenuItem[2]);

		subMenuItem[3] = new JMenuItem("GEO GDS Format Files");
		subMenuItem[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("GEO GDS Format Files"));
				} else {
					changeSelectedFileFilterAndLoader(14);
				}
			}
		});
		menuItem[4].add(subMenuItem[3]);

		jItem.add(menuItem[4]);

//jaw start---------------------------------------------
		menuItem[5] = new JMenuItem("MAGE-TAB Files");

		menuItem[5].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					helpWindow("MAGE");
				} else {
					changeSelectedFileFilterAndLoader(15);

				}
			}
		});
		jItem.add(menuItem[5]);
		
//jaw end-----------------------------------------------
		menuItem[6] = new JMenu("Other Format Files");

		subMenuItem = new JMenuItem[2];
		subMenuItem[0] = new JMenuItem("GenePix Format Files");
		subMenuItem[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("GenePix"));
				} else {
					changeSelectedFileFilterAndLoader(11);
				}
			}
		});
		menuItem[6].add(subMenuItem[0]);

		subMenuItem[1] = new JMenuItem("Agilent Files");
		subMenuItem[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (st == "File Format Hint") {
					HelpWindowDialog hwd = new HelpWindowDialog(mainFrame, HelpWindowDialog.createText("Agilent"));
				} else {
					changeSelectedFileFilterAndLoader(12);
				}
			}
		});
		menuItem[6].add(subMenuItem[1]);

		jItem.add(menuItem[6]);

	}

	public void initializeGUI() {

		gba = new GBA();
		eventListener = new EventListener();

		mainFrame = new JFrame("Expression File Loader");
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				viewer.setDataLoadersEnabled(true);
			}
			public void windowClosed(WindowEvent e) {
				viewer.setDataLoadersEnabled(true);
			}
			public void windowOpened(WindowEvent e) {
				viewer.setDataLoadersEnabled(false);
			}
		});
		
		mainFrame.getContentPane().setLayout(new GridBagLayout());

		menuBar = new JMenuBar();

		menu1 = new JMenu("Select File Loader");
		menu1.setMnemonic(KeyEvent.VK_S);
		menuItem(menu1, "Select Expression File Type");
		menuBar.add(menu1);

		menu2 = new JMenu("Help");
		menu2.setMnemonic(KeyEvent.VK_H);
		menuItem(menu2, "File Format Hint");
		menuBar.add(menu2);
		menuBar.setBorderPainted(true);
		mainFrame.setJMenuBar(menuBar);

		fileFilterLabel = new JLabel();

		fileLoaderPanel = selectedFileLoader.getFileLoaderPanel();

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
		infoButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(eventListener);
		cancelButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		cancelButton.setSize(60, 30);
		cancelButton.setPreferredSize(new Dimension(60, 30));
		cancelButton.setFocusPainted(false);
		loadButton = new JButton("Load");
		loadButton.addActionListener(eventListener);
		loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(240, 240, 240), new Color(180, 180, 180),
				new Color(10, 0, 0), new Color(10, 10, 10)));
		loadButton.setSize(60, 30);
		loadButton.setPreferredSize(new Dimension(60, 30));
		loadButton.setFocusPainted(false);
		loadButton.setEnabled(false);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		gba.add(buttonPanel, infoButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		// begin additions by Dan:
		url = this.getClass().getClassLoader().getResource("org/tigr/images/dialog_button_bar.gif");
		image = Toolkit.getDefaultToolkit().getImage(url);
		imageIcon = new ImageIcon(image);
		JLabel myLbl = new JLabel(imageIcon);
		// end additions by Dan

		gba.add(buttonPanel, myLbl, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(buttonPanel, cancelButton, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		gba.add(buttonPanel, loadButton, 3, 0, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		gba.add(mainFrame.getContentPane(), fileLoaderPanel, 0, 1, 1, 3, 1, 1, GBA.B, GBA.C);
		gba.add(mainFrame.getContentPane(), buttonPanel, 0, 4, 1, 1, 1, 0, GBA.H, GBA.C);
		
		mainFrame.setSize(800, 680);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = mainFrame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}

		mainFrame.setLocation((screenSize.width - mainFrame.getSize().width) / 2, (screenSize.height - mainFrame.getSize().height) / 2);

		mainFrame.setVisible(true);
		selectedFileLoader.openDataPath();
	}


	public void setLoadEnabled(boolean state) {
		loadButton.setEnabled(state);
	}

	private ExpressionFileLoader getFileLoader(int target) {

		ExpressionFileLoader loader;
		FileType selectedType = FileType.getTypeFromLoaderIndex(target);

		if (target >= 0 && target < fileLoaders.length && fileLoaders[target] != null) {

			return fileLoaders[target];
		}

		setLoadEnabled(false);

		switch (selectedType) {
		case STANFORD:
			loader = fileLoaders[0];
			break;
		case MEV_TARBALL:
			loader = new MevFileLoader(this);
			break;
		case TAV:
			loader = new TavFileLoader(this);
			break;
		case AFFY_GCOS:
			loader = new AffyGCOSFileLoader(this);
			break;
		case DCHIP:
			loader = new DFCI_CoreFileLoader(this);
			break;
		case GW_AFFY:
			loader = new AffymetrixFileLoader(this);
			break;
		case BIOCONDUCTOR_MAS5:
			loader = new Mas5FileLoader(this);
			break;
		case RMA:
			loader = new RMAFileLoader(this);
			break;
		case CGH:
			loader = new CGHStanfordFileLoader(this);
			break;
		case AFFY_GPL:
			loader = new SOFT_AffymetrixFileLoader(this);
			break;
		case TWO_CHANNEL_GPL:
			loader = new SOFT_TwoChannelFileLoader(this);
			break;
		case GENEPIX:
			loader = new GenePixFileLoader(this);
			break;
		case AGILENT:
			loader = new AgilentMevFileLoader(this);
			break;
		case GEO_SERIES_MATRIX:
			loader = new GEOSeriesMatrixLoader(this);
			break;
		case GEO_GDS:
			loader = new GEO_GDSFileLoader(this);
			break;
		case MAGETAB:
			loader = new MAGETABFileLoader(this);
			break;
		default:
			loader = new StanfordFileLoader(this);
			break;
		}
		fileLoaders[target] = loader;
		return loader;
	}

	public void changeSelectedFileFilterAndLoader(int target) {

		if (target < 0 || target >= fileLoaders.length || target >= fileFilters.length)
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

		cp.remove(fileLoaderPanel); // Remove the old
		// fileLoaderPanel
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
		return FileType.getTypeFromLoaderIndex(target).getDescription();
	}

	public void checkLoadEnable() {
		selectedFileLoader.checkLoadEnable();
	}

	public void onInfo() {
		HelpWindow hw = new HelpWindow(SuperExpressionFileLoader.this.getFrame(), "File Loader");
		if (hw.getWindowContent()) {
			hw.setSize(450, 650);
			hw.setLocation();
			hw.setVisible(true);
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

	// main mETHOD COMMENTED BY sARITA
	/*
	 * public static void main(String[] args) { SuperExpressionFileLoader
	 * loader = new SuperExpressionFileLoader(); }
	 */

	private ISlideData[] toISlideDataArray(Vector<ISlideData> dataVector) {
//		return dataVector.toArray(new ISlideData[dataVector.size()]);
		if (dataVector == null || dataVector.size() < 1) {
			return null;
		}
		ISlideData[] data = new ISlideData[dataVector.size()];

		for (int i = 0; i < data.length; i++) {
			data[i] = (ISlideData) (dataVector.elementAt(i));
		}
		return data;
	}

	private void updateDataPath(String dataPath) {//, String annotationPath) {
		if(dataPath != null) {
			
			DATA_PATH = dataPath;
			TMEV.setDataPath(DATA_PATH);
		}
//		if (annotationPath != null) {
//			ANNOTATION_PATH = annotationPath;
//			TMEV.storeProperty("current-annotation-path", ANNOTATION_PATH);
//		}
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
			Vector<ISlideData> data = null;
			int dataType = 0;
			IChipAnnotation chipAnnotation = null;
			try {
				selectedFileLoader.showModal();
				data = selectedFileLoader.loadExpressionFiles();
				chipAnnotation = selectedFileLoader.getChipAnnotation();
				dataType = selectedFileLoader.getDataType();
				selectedFileLoader.dispose();
//				updateDataPath(selectedFileLoader.getFilePath(), selectedFileLoader.getAnnotationFilePath());
				updateDataPath(selectedFileLoader.getFilePath());//, selectedFileLoader.getAnnotationFilePath());
				if (data != null) {
					viewer.fireDataLoaded(toISlideDataArray(data), chipAnnotation, dataType);
				}
			} catch (Exception ioe) {
				ioe.printStackTrace();
				ioe.getCause();
			}
		}
	}

	public void setFilePath(String path) {
		selectedFileLoader.setFilePath(path);
	}

	public class HeaderImagePanel extends JPanel {

		public HeaderImagePanel() {
			setLayout(new GridBagLayout());
			JLabel iconLabel = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(// changed
					// by
					// dan:
					this.getClass().getClassLoader().getResource("org/tigr/images/dialog_banner2.gif"))));
			iconLabel.setOpaque(false);
			iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			FillPanel fillPanel = new FillPanel();
			fillPanel.setBackground(Color.blue);
			add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 5, 0, 0),
					0, 0));
			add(fillPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
					0, 0));
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
			gp = new GradientPaint(0, dim.height / 2, backgroundColor, dim.width, dim.height / 2, fadeColor);
			g2.setPaint(gp);
			g2.fillRect(0, 0, dim.width, dim.height);
			g2.setColor(Color.black);
		}
	}

	public String getDefaultSpeciesName() {
		return defaultSpeciesName;
	}

	public String getDefaultArrayName() {
		return defaultArrayName;
	}

}
