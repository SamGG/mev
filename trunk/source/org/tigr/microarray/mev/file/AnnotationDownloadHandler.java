package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

public class AnnotationDownloadHandler {
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

	JLabel getAnnotation, customAnnotation;

	JButton connectButton, browseButton;

	JTextField annFileListTextField;

	JLabel chooseOrg, chooseArray;

	JTextField annFileNameTextField;

	String defaultSpeciesName;

	Hashtable<String, Vector<String>> annotationLists;

	SuperExpressionFileLoader superLoader;

	public AnnotationDownloadHandler(SuperExpressionFileLoader superLoader) {
		this.superLoader = superLoader;
		this.annotationLists = superLoader.annotationLists;
		this.defaultSpeciesName = superLoader.defaultSpeciesName;
	}

	public String getAnnFilePath() {
		return annFileListTextField.getText();
	}

	public JPanel getAnnotationLoaderPanel(GBA gba) {

		annotationPanel = new JPanel();
		annotationPanel.setLayout(new GridBagLayout());
		annotationPanel.setBorder(new TitledBorder(new EtchedBorder(), "Annotation"));

		getAnnotation = new JLabel("Retrieve  Annotation  from  Resourcerer");

		connectButton = new JButton("Connect");
		connectButton.setSize(new Dimension(100, 30));
		connectButton.setPreferredSize(new Dimension(100, 30));
		connectButton.addActionListener(new EventListener());

		customAnnotation = new JLabel("Selected File:");

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

		if (superLoader.hasAnnotationList) {
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

			if (superLoader.getDefaultArrayName() != null) {
				arrayListBox.setSelectedItem(superLoader.getDefaultArrayName());
			}

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

		gba.add(annotationPanel, chooseOrg, 0, 0, 1, 1, 0, 0, GBA.H, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, chooseArray, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);

		gba.add(annotationPanel, organismListBox, 1, 0, 1, 1, 0, 0, GBA.H,
				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, arrayListBox, 1, 1, 1, 1, 0, 0, GBA.H, GBA.C,
				new Insets(5, 5, 5, 5), 0, 0);

		gba.add(annotationPanel, getAnnotation, 3, 0, 2, 1, 0, 0, GBA.H, GBA.E,
				new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, connectButton, 3, 1, GBA.RELATIVE, 1, 0, 0,
				GBA.NONE, GBA.E, new Insets(5, 5, 5, 0), 0, 0);

		gba.add(annotationPanel, customAnnotation, 0, 2, 3, 1, 0, 0, GBA.H,
				GBA.E, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, annFileListTextField, 0, 3, 2, 0, 1, 0, GBA.H,
				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(annotationPanel, browseButton, 2, 3, GBA.RELATIVE, 1, 0, 0,
				GBA.NONE, GBA.E, new Insets(5, 5, 10, 0), 0, 0);
		
		checkForAnnotationFile();
		return annotationPanel;
	}
	


	public void setEnabled(boolean isEnabled) {
		connectButton.setEnabled(isEnabled);
//		browseButton.setEnabled(isEnabled);
		organismListBox.setEnabled(isEnabled);
		arrayListBox.setEnabled(isEnabled);
		this.isEnabled = isEnabled;
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

			if (superLoader.viewer.hasSupportFile(def)) {
				connectButton.setText("Select This");
				getAnnotation.setText("MeV has this file");
			} else {
				connectButton.setText("Download");
				getAnnotation.setText("Click to download.");
			}
		} else {
		}

	}

	public boolean isAnnotationSelected() {
		return annotationSelected;
	}

	public void onClickAnnDownload() {
		try {
			File f = superLoader.getAnnotationFile(organismListBox
					.getSelectedItem().toString(), arrayListBox
					.getSelectedItem().toString());
			// mav.getData().setAnnotationLoaded(true);
			this.annotationSelected = true;
//			annotationFilename = f.getAbsolutePath();
			// setAnnotationFileName(f.getAbsolutePath());
			annFileListTextField.setText(f.getAbsolutePath());
			connectButton.setText("Selected");
			connectButton.setEnabled(false);
			getAnnotation.setText("Selected");
			superLoader.getAdditionalSupportFiles(organismListBox
					.getSelectedItem().toString(), arrayListBox
					.getSelectedItem().toString());
		} catch (SupportFileAccessError sfae) {
			annotationSelected = false;
			getAnnotation.setText("Failure");
			connectButton.setText("Select this");
			connectButton.setEnabled(true);
			sfae.printStackTrace();
		}
	}

	public void onAnnotationFileBrowse() {
		File selectedFile;
		JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.ANNOTATION_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(null);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			annFileListTextField.setText(selectedFile.getAbsolutePath());
			this.annotationSelected = true;
		}
	}

	private class EventListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == browseButton) {
				onAnnotationFileBrowse();
			} else if (source == connectButton) {
				onClickAnnDownload();
			} else if (source.equals(organismListBox)) {
				updateLabel((String) organismListBox.getSelectedItem());
				checkForAnnotationFile();
			} else if (source.equals(arrayListBox)) {
				checkForAnnotationFile();
			}
		}
	}
}
