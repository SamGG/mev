package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEEntrezSupportDataFile;
import org.tigr.microarray.mev.resources.PipelinedAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

public class AnnotationDownloadHandler {
	public static final String GOT_ANNOTATION_FILE = "got-annotation-file";
	public static final String CHOOSE_ORGANISM = "Choose an organism";
	public static final String CHOOSE_ARRAY = "Choose an array";
	
	boolean annotationSelected = false;

	boolean isEnabled = true;

	/**
	 * Annotation Panel lets user choose additional annotations from
	 * Resourcerer. This feature is currently available only for Affymetrix
	 * files.
	 */
	JPanel annotationPanel;

	String defaultSpeciesName;
	String defaultArrayName;
	boolean inProgress = false;
	
	JComboBox organismListBox;
	JComboBox arrayListBox;
	JCheckBox proceedLoadingAnnotation;
	JRadioButton autoDownload, loadFromFile;
	JTextField annFileLocation = new JTextField("No file selected");
	JButton browseAnnFileButton;
	JLabel statusLabel;
	ButtonGroup bg = new ButtonGroup();
	JLabel optionalMessage;
	
	String datapath;

	Hashtable<String, Vector<String>> annotationLists;
	IResourceManager irm;

	public AnnotationDownloadHandler(IResourceManager irm, Hashtable<String, Vector<String>> annotationLists, String defaultSpeciesName, String defaultArrayName) {
		this.irm = irm;
		this.annotationLists = annotationLists;
		this.defaultSpeciesName = defaultSpeciesName;
		this.defaultArrayName = defaultArrayName;
	}
	
	public AnnotationDownloadHandler(IFramework framework) {
		this.irm = framework.getResourceManager();
		PipelinedAnnotationsFileDefinition aafd = new PipelinedAnnotationsFileDefinition();
		this.defaultSpeciesName = TMEV.getSettingForOption(TMEV.LAST_LOADED_SPECIES, CHOOSE_ORGANISM);
		this.defaultArrayName = TMEV.getSettingForOption(TMEV.LAST_LOADED_ARRAY, CHOOSE_ARRAY);
		try {
			File f = irm.getSupportFile(aafd, false);
			this.annotationLists = aafd.parseAnnotationListFile(f);
		} catch (SupportFileAccessError sfae) {
			//TODO disable buttons
		} catch (IOException ioe) {
			//TODO same thing
		}
	}



	public JPanel getAnnotationLoaderPanel(GBA gba) {


		annotationPanel = new JPanel();
		annotationPanel.setLayout(new GridBagLayout());
		annotationPanel.setBorder(new TitledBorder(new EtchedBorder(), "Load Annotation Data"));

		proceedLoadingAnnotation = new JCheckBox("Load Annotation");
		proceedLoadingAnnotation.setEnabled(true);
		proceedLoadingAnnotation.setSelected(false);
		
		autoDownload = new JRadioButton("Automatically download");
		loadFromFile = new JRadioButton("Load from local file");

			
		annFileLocation.setEditable(false);
		browseAnnFileButton = new JButton("Choose File");
		statusLabel = new JLabel("Please select a species name and array name.");
		bg.add(autoDownload);
		bg.add(loadFromFile);

		autoDownload.addActionListener(new EventListener());
		loadFromFile.addActionListener(new EventListener());
		browseAnnFileButton.addActionListener(new EventListener());
		proceedLoadingAnnotation.addActionListener(new EventListener());
		
		if (annotationLists != null && annotationLists.size() > 0) {
			arrayListBox = new JComboBox();
			arrayListBox.setEnabled(true);
			arrayListBox.addActionListener(new EventListener());
			Vector<String> organisms = new Vector<String>(annotationLists.keySet());
			organisms.add(0, CHOOSE_ORGANISM);
			organismListBox = new JComboBox(organisms);
			organismListBox.setSelectedIndex(0);
			organismListBox.addActionListener(new EventListener());

			if (defaultSpeciesName != null) {
				organismListBox.setSelectedItem(defaultSpeciesName);
			}

			updateArrayList(organismListBox.getSelectedItem().toString());
			arrayListBox.setSelectedItem(defaultArrayName);

			proceedLoadingAnnotation.setSelected(true);
		} else {
			Vector<String> temp = new Vector<String>();
			temp.add("No species available");
			organismListBox = new JComboBox(temp);
			organismListBox.setEnabled(false);
			Vector<String> temp2 = new Vector<String>();
			temp2.add("No arrays available");
			arrayListBox = new JComboBox(new Vector<String>(temp2));
			arrayListBox.setEnabled(false);
			annotationLists = new Hashtable<String,Vector<String>>();
//			annotationLists.put("No species available", temp);
			proceedLoadingAnnotation.setEnabled(false);
			proceedLoadingAnnotation.setSelected(false);
		}

		int i=0; //add 1 to y-coordinate in GBA if the optional message label is in place. 
		if(optionalMessage != null) {
			i=1;
			gba.add(annotationPanel, optionalMessage, 		0, 0, 3, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);
		}

		gba.add(annotationPanel, autoDownload,				0, i, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);
		gba.add(annotationPanel, organismListBox, 			0, i+1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);
		gba.add(annotationPanel, arrayListBox, 				0, i+2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);

		gba.add(annotationPanel, loadFromFile,				1, i, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);
		gba.add(annotationPanel, annFileLocation,			1, i+1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);
		gba.add(annotationPanel, browseAnnFileButton,		1, i+2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 10), 0, 0);

		gba.add(annotationPanel, proceedLoadingAnnotation,	2, i, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, statusLabel,				2, i+1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		
		checkForAnnotationFile();

		autoDownload.setSelected(true);
		onSelectAutoDownload();
		checkForAnnotationFile();
		return annotationPanel;
	}

	public void setOptionalMessage(String message) {
		optionalMessage = new JLabel(message);
	}
	
	public String getAnnFilePath() {

		return datapath;//annFileLocation.getText();
	}
	public void setAnnFilePath(String filePath) {
		datapath = filePath;
		annFileLocation.setText(filePath);
	}


	

	protected void updateArrayList(String organismName) {
		arrayListBox.removeAllItems();
		Vector<String> annFileKeyBoxItems = annotationLists.get(organismName);
		if(annFileKeyBoxItems == null) {
			annFileKeyBoxItems = new Vector<String>();
			annFileKeyBoxItems.add(0, CHOOSE_ARRAY);
		} 
		if(annFileKeyBoxItems.get(0) == null || !annFileKeyBoxItems.get(0).equals(CHOOSE_ARRAY)) {
			annFileKeyBoxItems.add(0, CHOOSE_ARRAY);
		}
		for (int i = 0; i < annFileKeyBoxItems.size(); i++) {
			arrayListBox.addItem(annFileKeyBoxItems.elementAt(i));
		}
	}

	protected void checkForAnnotationFile() {		
		try {
			final String organismName = organismListBox.getSelectedItem().toString();
			final String arrayName = arrayListBox.getSelectedItem().toString();
			if(autoDownload.isSelected()) {
				if(!organismName.equals(CHOOSE_ORGANISM) &&	!arrayName.equals(CHOOSE_ARRAY)) {
					final ISupportFileDefinition def = new ResourcererAnnotationFileDefinition(organismName, arrayName);

					Thread thread = new Thread(new Runnable() {
						public void run() {
							try {
								inProgress = true;
								updateLabel();
								try {
									datapath = irm.getSupportFile(def, true).getAbsolutePath();
								} catch (SupportFileAccessError sfae) {
									annotationSelected = false;
									inProgress = false;
									updateLabel();
									if(sfae.isFileNotFound() || !sfae.isInternetAccessWasAllowed()) {
										ShowThrowableDialog.show(new JFrame(), "Annotation not available", true, ShowThrowableDialog.ERROR, sfae, 
												def.getURL() + "was not found. Please check that your internet connection is enabled. ");
									} else {
										ShowThrowableDialog.show(new JFrame(), "Annotation not available", true, ShowThrowableDialog.ERROR, sfae, sfae.getMessage());
									}
									return;
								}
								getAdditionalSupportFiles(organismName, arrayName);
								annotationSelected = true;
								inProgress = false;
								updateLabel();
							} catch (Exception e) {
								annotationSelected = false;
								updateLabel();
								e.printStackTrace();
							}
						}
						
					});
	
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.start();
				}
			} 
		} catch (NullPointerException npe){
			annotationSelected = false;
		}
		updateLabel();
	}

	public boolean isAnnotationSelected() {
		return annotationSelected;
	}

	public int onAnnotationFileBrowse() {
		File selectedFile;
		JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.ANNOTATION_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(null);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			setAnnFilePath(selectedFile.getAbsolutePath());
			this.annotationSelected = true;
			updateLabel();
		}
		return retVal;
	}
	private ActionListener additionalListener;
	public void addListener(ActionListener listener) {
		this.additionalListener = listener;
	}

	/**
	 * Downloads additional support files (besides the annotation files) that may be needed by the user for their data.
	 * @param organismName
	 * @param arrayName
	 */
	public void getAdditionalSupportFiles(String organismName, String arrayName) throws SupportFileAccessError {
		Vector<ISupportFileDefinition> defs = new Vector<ISupportFileDefinition>();
		defs.add(new EASEEntrezSupportDataFile(organismName, arrayName));
		irm.getSupportFiles(defs, true);
	}
	
	private class EventListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == browseAnnFileButton) {
				int retVal = onAnnotationFileBrowse();
				if(additionalListener != null && retVal == JFileChooser.APPROVE_OPTION) {
					additionalListener.actionPerformed(new ActionEvent(browseAnnFileButton, 0, GOT_ANNOTATION_FILE));
				}
			} else if (source.equals(autoDownload)) {
				if(autoDownload.isSelected()) {
					onSelectAutoDownload();
				}
			} else if (source.equals(loadFromFile)) {
				if(loadFromFile.isSelected()) {
					onSelectBrowseFile();
				}
			} else if (source.equals(proceedLoadingAnnotation)) {
				onToggleProceed();
			} else if (source.equals(organismListBox)) {
				updateArrayList((String) organismListBox.getSelectedItem());
				checkForAnnotationFile();
			} else if (source.equals(arrayListBox)) {
				checkForAnnotationFile();
			}
		}
	}
	private void onSelectAutoDownload() {
		annFileLocation.setEnabled(false);
		browseAnnFileButton.setEnabled(false);
		organismListBox.setEnabled(true);
		arrayListBox.setEnabled(true);
		checkForAnnotationFile();
		if(annotationSelected)
			proceedLoadingAnnotation.setSelected(true);

	}
	private void onSelectBrowseFile() {
		annFileLocation.setEnabled(true);
		browseAnnFileButton.setEnabled(true);
		organismListBox.setEnabled(false);
		arrayListBox.setEnabled(false);
		if(annotationSelected)
			proceedLoadingAnnotation.setSelected(true);
		updateLabel();
	}
	private void onToggleProceed() {
		updateLabel();
	}
	private void updateLabel() {
		if(inProgress) {
			statusLabel.setText("Downloading...");
			statusLabel.setForeground(Color.black);
		} else if(!proceedLoadingAnnotation.isSelected()) {
			statusLabel.setText("No annotation will be loaded.");
			statusLabel.setForeground(Color.red);
			annotationSelected = false;
		} else {
			annotationSelected = true;
			if(autoDownload.isSelected()) {
				if(organismListBox.getSelectedItem() == null ||
						organismListBox.getSelectedItem().equals(CHOOSE_ORGANISM) || 
						arrayListBox.getSelectedItem() == null ||
						arrayListBox.getSelectedItem().equals(CHOOSE_ARRAY)){
					statusLabel.setText("Please choose an array and species name.");
					statusLabel.setForeground(Color.red);
					annotationSelected = false;
				} else {
					if(annotationSelected) {
						statusLabel.setText("Annotation will be loaded.");
						statusLabel.setForeground(Color.black);
					} else {
						statusLabel.setText("Annotation could not be downloaded.");
						statusLabel.setForeground(Color.red);
					}
				}
			} else {
				if(!new File(annFileLocation.getText()).exists()) {
					statusLabel.setText("Please choose a valid annotation file.");
					statusLabel.setForeground(Color.red);
					annotationSelected = false;
				} else {
					statusLabel.setText("Annotation will be loaded.");
					statusLabel.setForeground(Color.black);
					annotationSelected = true;
				}
			}
		}
		if(!isEnabled) {
			statusLabel.setForeground(Color.gray);
		}
		SwingUtilities.updateComponentTreeUI(statusLabel);
		annotationPanel.repaint();
		statusLabel.repaint();
	
	}
	
	public void setDownloadEnabled(boolean isEnabled) {
		autoDownload.setEnabled(isEnabled);
		loadFromFile.setSelected(!isEnabled);
		loadFromFile.setEnabled(isEnabled);
		organismListBox.setEnabled(isEnabled);
		arrayListBox.setEnabled(isEnabled);
		proceedLoadingAnnotation.setSelected(false);
		proceedLoadingAnnotation.setEnabled(false);
		annotationSelected = false;
		statusLabel.setForeground(Color.gray);
		this.isEnabled = isEnabled;
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame("Testing Annotation Download Handler");
		IResourceManager irm;

	    	GBA gba = new GBA();
	    	frame.setLayout(new GridBagLayout());
	    	frame.setSize(800, 200);
	    	
		try {
			irm = new FileResourceManager(new File(new File(System.getProperty("user.home"), ".mev"), "repository"));
			irm.setAskToGetOnline(false);
		} catch (RepositoryInitializationError rie) {
			rie.printStackTrace();
			return;
		}
		try {
			File taxonfile = irm.getSupportFile(new PipelinedAnnotationsFileDefinition(), true);
			PipelinedAnnotationsFileDefinition aafd = new PipelinedAnnotationsFileDefinition();
			Hashtable<String, Vector<String>> speciestoarrays = aafd.parseAnnotationListFile(taxonfile);
			AnnotationDownloadHandler adh = new AnnotationDownloadHandler(irm, speciestoarrays, "Human", "affy_HG-U133A");
			JPanel annotationPanel = adh.getAnnotationLoaderPanel(gba);
			frame.add(annotationPanel);

		} catch (SupportFileAccessError sfae) {
//			fail("Couldn't get species/array mappings from repository.");
		} catch (IOException ioe) {
			ioe.printStackTrace();
//			fail("Couldn't get annotation file.");
		}
		frame.setVisible(true);
		
	}
}
