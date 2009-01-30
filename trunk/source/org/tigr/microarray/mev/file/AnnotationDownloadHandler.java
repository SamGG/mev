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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASESupportDataFile;
import org.tigr.microarray.mev.resources.AvailableAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

public class AnnotationDownloadHandler {
	public static final String GOT_ANNOTATION_FILE = "got-annotation-file";
	JComboBox organismListBox;

	JComboBox arrayListBox;

	boolean annotationSelected = false;

	boolean isEnabled = true;

	/**
	 * Annotation Panel lets user choose additional annotations from
	 * Resourcerer. This feature is currently available only for Affymetrix
	 * files.
	 */
	JPanel annotationPanel;

	JLabel getAnnotation, /*customAnnotation,*/ statusLabel;

	JButton connectButton, browseButton;

	JTextField annFileListTextField;

	JLabel chooseOrg, chooseArray;

	JTextField annFileNameTextField;

	String defaultSpeciesName;
	String defaultArrayName;

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
		AvailableAnnotationsFileDefinition aafd = new AvailableAnnotationsFileDefinition();
		this.defaultSpeciesName = TMEV.getSettingForOption(TMEV.LAST_LOADED_SPECIES);
		this.defaultArrayName = TMEV.getSettingForOption(TMEV.LAST_LOADED_ARRAY);
		try {
			File f = irm.getSupportFile(aafd, true);
			this.annotationLists = aafd.parseAnnotationListFile(f);
		} catch (SupportFileAccessError sfae) {
			//TODO disable buttons
		} catch (IOException ioe) {
			//TODO same thing
		}
	}

	public String getAnnFilePath() {
		return annFileListTextField.getText();
	}
	public void setAnnFilePath(String filePath) {
		annFileListTextField.setText(filePath);
	}

	public JPanel getAnnotationLoaderPanel(GBA gba) {

		
		annotationPanel = new JPanel();
		annotationPanel.setLayout(new GridBagLayout());
		annotationPanel.setBorder(new TitledBorder(new EtchedBorder(), "Load Annotation Data"));

		statusLabel = new JLabel("Choose an array and Organism and select Download");
		
		getAnnotation = new JLabel("Retrieve  Annotation  from  Resourcerer");

		connectButton = new JButton("Connect");
		connectButton.setSize(new Dimension(100, 30));
		connectButton.setPreferredSize(new Dimension(100, 30));
		connectButton.addActionListener(new EventListener());

		statusLabel = new JLabel("Choose an array and Organism and select Download");
//		customAnnotation = new JLabel("Selected File:");

		annFileListTextField = new JTextField("No annotation selected.");
		annFileListTextField.setEditable(false);
		annFileListTextField.setForeground(Color.black);
		annFileListTextField.setFont(new Font("monospaced", Font.BOLD, 12));

		browseButton = new JButton("Browse");
		browseButton.setSize(new Dimension(100, 30));
		browseButton.setPreferredSize(new Dimension(100, 30));
		browseButton.addActionListener(new EventListener());

		chooseOrg = new JLabel("Choose Organism");
		chooseArray = new JLabel("Choose Array");
		

		if (annotationLists != null && annotationLists.size() > 0) {
			arrayListBox = new JComboBox();
			arrayListBox.setEnabled(true);
			arrayListBox.addActionListener(new EventListener());

			organismListBox = new JComboBox(new Vector<String>(annotationLists
					.keySet()));
			organismListBox.setSelectedIndex(0);
			organismListBox.addActionListener(new EventListener());

			if (defaultSpeciesName != null) {
				organismListBox.setSelectedItem(defaultSpeciesName);
			}

			updateLabel(organismListBox.getSelectedItem().toString());
			arrayListBox.setSelectedItem(defaultArrayName);

			connectButton.setEnabled(true);
		} else {
			Vector<String> temp = new Vector<String>();
			temp.add("No species available");
			organismListBox = new JComboBox(temp);
			organismListBox.setEnabled(false);
			Vector<String> temp2 = new Vector<String>();
			temp2.add("No arrays available");
			arrayListBox = new JComboBox(new Vector<String>(temp2));
			arrayListBox.setEnabled(false);
			getAnnotation = new JLabel("No annotation lists available.");
			connectButton.setEnabled(false);
			chooseOrg.setEnabled(false);
			chooseArray.setEnabled(false);
		}

//		gba.add(annotationPanel, statusLabel,			0, 0, 0, 0, 0, 0, GBA.H, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, chooseOrg, 			0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, chooseArray, 			0, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		gba.add(annotationPanel, organismListBox, 		1, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, arrayListBox, 			1, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		gba.add(annotationPanel, getAnnotation, 			3, 1, 2, 1, 0, 0, GBA.H, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, connectButton, 			3, 2, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 0), 0, 0);

//		gba.add(annotationPanel, customAnnotation, 		0, 3, 3, 1, 0, 0, GBA.H, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, statusLabel,	 		0, 3, 3, 1, 3, 0, GBA.H, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, annFileListTextField, 	0, 4, 2, 0, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, browseButton, 			2, 4, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 10, 0), 0, 0);
		
		checkForAnnotationFile();
		return annotationPanel;
	}
	


	public void setDownloadEnabled(boolean isEnabled) {
		connectButton.setEnabled(isEnabled);
		organismListBox.setEnabled(isEnabled);
		arrayListBox.setEnabled(isEnabled);
		this.isEnabled = isEnabled;
	}
	public void setBrowseEnabled(boolean isEnabled) {
		browseButton.setEnabled(isEnabled);
	}

	protected void updateLabel(String name) {
		arrayListBox.removeAllItems();
		Vector<String> annFileKeyBoxItems = annotationLists.get(name);
		for (int i = 0; i < annFileKeyBoxItems.size(); i++) {
			arrayListBox.addItem(annFileKeyBoxItems.elementAt(i));
		}
	}

	protected void checkForAnnotationFile() {
		connectButton.setEnabled(true);
		if (organismListBox.getSelectedItem() != null && arrayListBox.getSelectedItem() != null) {
			ISupportFileDefinition def = new ResourcererAnnotationFileDefinition(
					organismListBox.getSelectedItem().toString(), arrayListBox
							.getSelectedItem().toString());

			if (irm.fileIsInRepository(def)) {
				connectButton.setText("Select This");
				getAnnotation.setText("MeV has this file");
				statusLabel.setText("This file already stored locally. Click the Select button to use it.");
			} else {
				connectButton.setText("Download");
				getAnnotation.setText("Click to download.");
				statusLabel.setText("This file is not stored locally. Click the Download button to get it from the internet.");
			}
		} else {
			statusLabel.setText("It is not possible to select an annotation file at this time.");
		}

	}

	public boolean isAnnotationSelected() {
		return annotationSelected;
	}

	public boolean onClickAnnDownload() {
		try {
			ResourcererAnnotationFileDefinition rafd = new ResourcererAnnotationFileDefinition(organismListBox
					.getSelectedItem().toString(), arrayListBox
					.getSelectedItem().toString());
			File f = irm.getSupportFile(rafd, true);
			if (f==null)
				return false;
			this.annotationSelected = true;
			annFileListTextField.setText(f.getAbsolutePath());
			connectButton.setText("Selected");
			connectButton.setEnabled(false);
			getAnnotation.setText("Selected");
			getAdditionalSupportFiles(organismListBox
					.getSelectedItem().toString(), arrayListBox
					.getSelectedItem().toString());

			statusLabel.setText("This annotation file is selected and ready to use.");
			return true;
		} catch (SupportFileAccessError sfae) {
			annotationSelected = false;
			getAnnotation.setText("Failure");
			connectButton.setText("Select this");
			connectButton.setEnabled(true);
			statusLabel.setText("There was a problem downloading this file.");
			sfae.printStackTrace();
			return false;
		}
	}

	public int onAnnotationFileBrowse() {
		File selectedFile;
		JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.ANNOTATION_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(null);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			annFileListTextField.setText(selectedFile.getAbsolutePath());
			this.annotationSelected = true;
			statusLabel.setText("This file is selected and ready to use.");
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
		defs.add(new EASESupportDataFile(organismName, arrayName));
		irm.getSupportFiles(defs, true);
	}
	
	private class EventListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == browseButton) {
				int retVal = onAnnotationFileBrowse();
				if(additionalListener != null && retVal == JFileChooser.APPROVE_OPTION) {
					additionalListener.actionPerformed(new ActionEvent(browseButton, 0, GOT_ANNOTATION_FILE));
				}
			} else if (source == connectButton) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							boolean success = onClickAnnDownload();
							if(additionalListener != null && success ) {
								additionalListener.actionPerformed(new ActionEvent(connectButton, 0, GOT_ANNOTATION_FILE));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				});

				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
				
			} else if (source.equals(organismListBox)) {
				updateLabel((String) organismListBox.getSelectedItem());
				checkForAnnotationFile();
			} else if (source.equals(arrayListBox)) {
				checkForAnnotationFile();
			}
		}
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame("Testing Annotation Download Handler");
		IResourceManager irm;

	    	GBA gba = new GBA();
	    	frame.setLayout(new GridBagLayout());
	    	frame.setSize(600, 300);
	    	
		try {
			irm = new FileResourceManager(new File(new File(System.getProperty("user.home"), ".mev"), "repository"));
			irm.setAskToGetOnline(false);
		} catch (RepositoryInitializationError rie) {
			rie.printStackTrace();
			return;
		}
		try {
			File taxonfile = irm.getSupportFile(new AvailableAnnotationsFileDefinition(), true);
			AvailableAnnotationsFileDefinition aafd = new AvailableAnnotationsFileDefinition();
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
