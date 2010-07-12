package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * Contains advanced features(population, annotation converter,annotation file
 * selection) to customize EASE analysis
 * 
 * @author hgomez
 * */
public class EASEAdvancedFeatures extends AlgorithmDialog {

	private EventListener listener;
	private String defaultFileLocation;
	/*Saves the initial location of the EASE custom Directory for reset purposes*/
	private Frame parent;
	protected String sep;
	private int result = JOptionPane.CANCEL_OPTION;
	private ClusterRepository repository;
	protected Font font;
	private int index=0;
	/* Specifies the kind of analysis to carry out */
	private boolean isClusterAnalysis = false;

	private boolean useLoadedAnnotationFile = false;

	/* Panel where Annotation support Files are selected */
	protected ConfigPanelExtension configPanelExtension;

	/* Panel for population selection */
	protected PopSelectionPanel popPanel;

	/* Panel for Annotation type selection */
	protected EaseParameterPanel paramPanel;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent Frame
	 * @param defaultFileLocation
	 *            previously selected location for annotation files
	 * */
	public EASEAdvancedFeatures(Frame parent, String defaultFileLocation,
			boolean isClusterAnalysis,ClusterRepository repository, String[] annotationLabels) {

		super(parent, "EASEAdvancedParamters", true);
		this.parent = parent;
		this.defaultFileLocation = defaultFileLocation;
		this.isClusterAnalysis = isClusterAnalysis;
		this.repository = repository;
		font = new Font("Dialog", Font.BOLD, 12);
		addWindowListener(listener);
		listener = new EventListener();

		configPanelExtension = new ConfigPanelExtension();

		popPanel = new PopSelectionPanel();

		/* Disables population selection if annotation mode was selected */
		if (isClusterAnalysis) {
			popPanel.setEnableControls(true);
		} else {
			popPanel.setEnableControls(false);
		}

		paramPanel = new EaseParameterPanel(annotationLabels);

		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);

		parameters.add(configPanelExtension, new GridBagConstraints(0, 0, 1, 1,
				1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		parameters.add(popPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		parameters.add(paramPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		addContent(parameters);

		setActionListeners(listener);

		if (repository == null || repository.isEmpty()) {
			JPanel panel = popPanel;
			panel.removeAll();
			panel.validate();
			panel.setOpaque(false);
			panel.add(new JLabel("Empty Cluster Repository"),
					new GridBagConstraints(0, 0, 1, 1, 0, 0,
							GridBagConstraints.NORTH, GridBagConstraints.NONE,
							new Insets(15, 0, 10, 0), 0, 0));
		}
		pack();
	}

	/**
	 * Contains annotation parameter controls.
	 */
	protected class EaseParameterPanel extends JPanel {

		private static final long serialVersionUID = 3446234672105256730L;
		protected JTextField converterFileField;
		protected JList fileList;
		protected JButton browserButton;
		protected JComboBox fieldNamesBox;
		protected JList annFileList;
		protected JButton removeButton;
		protected JCheckBox useAnnBox;
		protected JLabel fileLabel;

		protected JRadioButton useLoadedAnn;

		/**
		 * Constructs a new EaseParameterPanel which contains controls
		 * for selecting the annotation information to be used in running EASE.
		 * 
		 * @param fieldNames
		 *            annotation types
		 */
		public EaseParameterPanel(String[] fieldNames) {
			JPanel convPanel = new JPanel(new GridBagLayout());
			convPanel.setBackground(Color.white);
			convPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED),
					"Annotation Conversion File",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, font, Color.black));
			useAnnBox = new JCheckBox("Use Annotation Converter", false);
			useAnnBox.setActionCommand("use-converter-command");
			useAnnBox.addActionListener(listener);
			useAnnBox.setBackground(Color.white);
			useAnnBox.setFocusPainted(false);
			useAnnBox.setEnabled(true);
			

			converterFileField = new JTextField(30);
			converterFileField.setBorder(BorderFactory.createBevelBorder(
					BevelBorder.LOWERED, Color.lightGray, Color.gray));
			converterFileField.setEnabled(false);
			converterFileField.setBackground(Color.lightGray);

			browserButton = new JButton("File Browser");
			browserButton.setActionCommand("converter-file-browser-command");
			browserButton.setFocusPainted(false);
			browserButton.setPreferredSize(new Dimension(150, 25));
			browserButton.setSize(150, 25);
			browserButton.addActionListener(listener);
			browserButton.setEnabled(false);

			fileLabel = new JLabel("File :");
			fileLabel.setEnabled(false);
			convPanel.add(useAnnBox, new GridBagConstraints(0, 0, 3, 1, 0.0,
					0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(0, 15, 15, 0), 0, 0));
			convPanel.add(fileLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
					0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 15, 15, 0), 0, 0));
			convPanel
					.add(this.browserButton, new GridBagConstraints(0, 2, 3, 1,
							0.0, 0.0, GridBagConstraints.WEST,
							GridBagConstraints.VERTICAL,
							new Insets(0, 15, 0, 0), 0, 0));
			convPanel.add(this.converterFileField, new GridBagConstraints(1, 1,
					2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 15, 15, 0), 0, 0));

			// Annotation file panel
			JPanel annPanel = new JPanel(new GridBagLayout());
			annPanel.setBackground(Color.white);
			annPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED),
					"Gene Annotation / Gene Ontology Linking Files",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, font, Color.black));

			// annVector = new Vector();
			annFileList = new JList(new DefaultListModel());
			annFileList.setCellRenderer(new ListRenderer());
			annFileList.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			annFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane annPane = new JScrollPane(annFileList);

			JButton annButton = new JButton("Add Files");
			annButton.setActionCommand("ann-file-browser-command");
			annButton.addActionListener(listener);
			annButton.setFocusPainted(false);
			annButton.setPreferredSize(new Dimension(150, 25));
			annButton.setSize(150, 25);

			removeButton = new JButton("Remove Selected");
			removeButton.setActionCommand("remove-ann-file-command");
			removeButton.addActionListener(listener);
			removeButton.setFocusPainted(false);
			removeButton.setPreferredSize(new Dimension(150, 25));
			removeButton.setSize(150, 25);
			removeButton.setEnabled(false);

			JPanel fillPanel = new JPanel();
			fillPanel.setBackground(Color.white);
			annPanel.add(fillPanel, new GridBagConstraints(0, 0, 1, 1, 0.0,
					0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			annPanel.add(annButton, new GridBagConstraints(1, 0, 1, 1, 0.0,
					0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 10, 5), 0, 0));
			annPanel
					.add(removeButton, new GridBagConstraints(2, 0, 1, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.VERTICAL,
							new Insets(0, 5, 10, 0), 0, 0));
			annPanel.add(new JLabel("Files: "), new GridBagConstraints(0, 1, 1,
					1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			annPanel.add(annPane, new GridBagConstraints(1, 1, 2, 1, 0.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			sep = System.getProperty("file.separator");
			File file = new File(defaultFileLocation);
			String tempPath = file.getPath();
			Vector<String> fileVector = new Vector<String>();
			fileList = new JList(fileVector);
			if (file.exists()) {
				String[] listFileNames = file.list();
				for (int i = 0; i < listFileNames.length; i++) {
					File tempFile = new File(tempPath + sep + listFileNames[i]);
					if (tempFile.isFile()) {
						fileVector.add(listFileNames[i]);
					}
				}
				if (fileVector.size() > 0) {
					converterFileField.setText(tempPath + sep
							+ ((String) fileVector.elementAt(0)));
				}
			}

			converterFileField.setText("");

			this.fieldNamesBox = new JComboBox(fieldNames);
			this.fieldNamesBox.setEditable(false);
			this.fieldNamesBox.setEnabled(true);
			fieldNamesBox.setSelectedItem(AnnotationFieldConstants.PROBE_ID);
			index=fieldNamesBox.getSelectedIndex();

			this.setLayout(new GridBagLayout());

			JPanel annotKeyPanel = new JPanel(new GridBagLayout());
			annotKeyPanel.setBackground(Color.white);
			annotKeyPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					"MeV Annotation Key  (\"Unique ID\")",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, font, Color.black));

			annotKeyPanel.add(new JLabel("Annotation Key:  "),
					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
			annotKeyPanel.add(this.fieldNamesBox, new GridBagConstraints(1, 0,
					1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.add(annotKeyPanel, new GridBagConstraints(0, 0, 1, 1, 1.0,
					0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			this.add(convPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			this.add(annPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}

		protected void updateFileDirectoryField() {

			File file = new File((String) this.fileList.getSelectedValue());
			if (file == null)
				return;

			String tempPath = file.getParent();
			int fileIndex = this.fileList.getSelectedIndex();
			String fileName = (String) (this.fileList.getModel()
					.getElementAt(this.fileList.getSelectedIndex()));
			this.converterFileField.setText(tempPath + sep + fileName);
		}

		protected void updateAnnFileList(File[] files) {
			File file;
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				if (!((DefaultListModel) annFileList.getModel()).contains(file)) {
					((DefaultListModel) annFileList.getModel())
							.addElement(file);
				}
			}
			annFileList.validate();
		}

		protected class EaseListListener implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				updateFileDirectoryField();
			}
		}

		public void removeSelectedFiles() {
			int[] indices = annFileList.getSelectedIndices();

			for (int i = 0; i < indices.length; i++) {
				// annFileList.remove(indices[i]);
				((DefaultListModel) annFileList.getModel())
						.removeElementAt(indices[i]);
			}
			if (annFileList.getModel().getSize() < 1) {
				this.removeButton.setEnabled(false);
			}
			annFileList.validate();
		}

		protected class ListRenderer extends DefaultListCellRenderer {
			private static final long serialVersionUID = 663620540385920562L;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
				File file = (File) value;
				setText(file.getName());
				return this;
			}
		}
	}

	/**
	 * @return directory chosen as source for new annotation files
	 */
	public String getNewSupportFileLocation() {

		return configPanelExtension.supportFileLocationField.getText();
	}

	/**
	 * @return a flag signaling whether a background population was selected
	 * */
	public boolean isPopFileModeSelected() {
		return popPanel.fileButton.isSelected();
	}

	public String getPopFieldText() {
		return popPanel.popField.getText();
	}

	/**
	 * Returns the converter file name
	 */
	public String getConverterFileName() {
		return paramPanel.converterFileField.getText();

	}

	/**
	 * Returns whether the Annotation selection check box is selected
	 * */
	public boolean isAnnBoxSelected() {
		return paramPanel.useAnnBox.isSelected();
	}

	/**
	 * Returns Annotation Selection check box text
	 * */
	public String getAnnBoxText() {
		return paramPanel.useAnnBox.getText();
	}

	/**
	 * Returns the annotation type string.
	 */
	public String getAnnotationKeyType() {
		return (String) paramPanel.fieldNamesBox.getSelectedItem();
	}

	/**
	 * Returns the list of annotation-theme mapping files.
	 */
	public String[] getAnnToGOFileList() {

		String[] fileNames = new String[((DefaultListModel) paramPanel.annFileList
				.getModel()).size()];
		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = ((File) (((DefaultListModel) paramPanel.annFileList
					.getModel()).elementAt(i))).getPath();
		}
		return fileNames;
	}

	/**
	 * Places the window in the middle of the screen
	 * 
	 * @return an Integer depending on a window event
	 * */
	public int showModal() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2,
				(screenSize.height - getSize().height) / 2);
		setVisible(true);
		return result;
	}

	/**
	 * This class is for selection of new directory for Support Files
	 * */
	protected class ConfigPanelExtension extends ParameterPanel {

		private static final long serialVersionUID = 1L;
		JLabel statusLabel;
		JTextField supportFileLocationField;
		JButton browseSupportFileButton;

		public ConfigPanelExtension() {
			super("Directory Selection for Support Files");
			setLayout(new GridBagLayout());

			statusLabel = new JLabel("Choose a custom EASE filesystem");
			supportFileLocationField = new JTextField(25);
			supportFileLocationField.setEditable(true);
			supportFileLocationField.setText("Please select a directory");
			browseSupportFileButton = new JButton("Browse");
			browseSupportFileButton
					.setActionCommand("select-file-base-command");
			browseSupportFileButton.addActionListener(listener);

			add(statusLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(5, 25, 0, 20), 0, 0));
			add(supportFileLocationField, new GridBagConstraints(0, 3, 2, 1, 1,
					0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(10, 30, 5, 0), 0, 0));
			add(browseSupportFileButton, new GridBagConstraints(4, 3, 1, 1, 0,
					0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
					new Insets(5, 25, 5, 20), 0, 0));

		}

		public void browseForSupportFiles() {
			String startDir = TMEV
					.getSettingForOption(EASEGUI.LAST_EASE_FILE_LOCATION);
			if (startDir == null)
				startDir = supportFileLocationField.getText();
			File file = new File(startDir);
			if (!file.exists()) {
				file = TMEV.getFile("data/ease");
				if (file == null) {
					file = new File(System.getProperty("user.dir"));
				}
				TMEV.storeProperty(EASEGUI.LAST_EASE_FILE_LOCATION, file
						.toString());
			}
			JFileChooser chooser = new JFileChooser(file);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(EASEAdvancedFeatures.this) == JOptionPane.OK_OPTION) {
				supportFileLocationField.setText(chooser.getSelectedFile()
						.getAbsolutePath());
				TMEV.storeProperty(EASEGUI.LAST_EASE_FILE_LOCATION,
						supportFileLocationField.getText());
			}
		}
	}

	/**
	 * Contains population selection controls.
	 * */
	protected class PopSelectionPanel extends ParameterPanel {

		private static final long serialVersionUID = 4355369410348482259L;
		JRadioButton fileButton;
		JRadioButton dataButton;
		JTextField popField;
		JButton browseButton;
		JLabel fileLabel;

		public PopSelectionPanel() {
			super("Population Selection");
			setLayout(new GridBagLayout());

			ButtonGroup bg = new ButtonGroup();

			fileButton = new JRadioButton(
					"Select Background Population from File");
			fileButton.setBackground(Color.white);
			fileButton.setFocusPainted(false);
			fileButton.setSelected(true);
			
			bg.add(fileButton);

			fileButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					paramPanel.fieldNamesBox.setEnabled(true);
					paramPanel.fieldNamesBox
							.setSelectedItem(AnnotationFieldConstants.PROBE_ID);
					paramPanel.useAnnBox.setEnabled(true);
					browseButton.setEnabled(fileButton.isSelected());
					popField.setEnabled(fileButton.isSelected());
					popField.setBackground(Color.white);
					popField.setText("Please select a population file");
					fileLabel.setEnabled(fileButton.isSelected());

				}
			});

			dataButton = new JRadioButton(
					"Select Background Population from Current Viewer");
			dataButton.setBackground(Color.white);
			dataButton.setFocusPainted(false);
			bg.add(dataButton);
			dataButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					browseButton.setEnabled(fileButton.isSelected());
					popField.setEnabled(fileButton.isSelected());
					popField.setBackground(Color.lightGray);
					fileLabel.setEnabled(fileButton.isSelected());

				}
			});

			browseButton = new JButton("File Browser");
			browseButton.setFocusPainted(false);
			browseButton.setEnabled(false);
			browseButton.setPreferredSize(new Dimension(150, 25));
			browseButton.setSize(150, 25);
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JFileChooser chooser = new JFileChooser(new File(
							getNewSupportFileLocation(), "Lists"));
					chooser.setDialogTitle("Population File Selection");
					chooser.setMultiSelectionEnabled(false);
					if (chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION) {
						popField.setText(chooser.getSelectedFile().getPath());
					}
				}
			});

			fileLabel = new JLabel("File: ");
			popField = new JTextField(25);
			fileButton.setSelected(!useLoadedAnnotationFile);
			popField.setEnabled(fileButton.isSelected());
			fileLabel.setEnabled(fileButton.isSelected());
			popField.setText("Please select a population file");

			if (fileButton.isSelected())
				popField.setBackground(Color.white);
			else
				popField.setBackground(Color.lightGray);

			add(fileButton, new GridBagConstraints(0, 1, 3, 1, 1, 0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(10, 30, 0, 0), 0, 0));
			add(fileLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 30, 0, 0), 0, 0));
			add(popField, new GridBagConstraints(1, 2, 1, 1, 1, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 10, 0, 0), 0, 0));
			add(browseButton, new GridBagConstraints(2, 2, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 25, 0, 20), 0, 0));

			add(dataButton, new GridBagConstraints(0, 3, 3, 1, 1, 0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(15, 30, 20, 0), 0, 0));
		}

		/**
		 * Modifies population control based on the selected mode
		 */
		protected void setEnableControls(boolean enable) {
			fileButton.setEnabled(enable);
			dataButton.setEnabled(enable);
			popField.setEnabled(enable);
			browseButton.setEnabled(enable);
			fileLabel.setEnabled(enable);
			setOpaque(enable);
		}

	}

	/**
	 * Class that takes care of event handling actions
	 * */

	protected class EventListener extends DialogListener implements

	ItemListener {

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();
			if (command.equals("select-file-base-command")) {
				configPanelExtension.browseForSupportFiles();
			} else if (command.equals("use-converter-command")) {
				if (paramPanel.useAnnBox.isSelected()) {
					paramPanel.browserButton.setEnabled(true);
					paramPanel.converterFileField.setEnabled(true);
					paramPanel.converterFileField.setBackground(Color.white);
					paramPanel.fileLabel.setEnabled(true);
					paramPanel.converterFileField.setText("Please select a conversion file");
				} else {
					paramPanel.browserButton.setEnabled(false);
					paramPanel.converterFileField.setEnabled(false);
					paramPanel.converterFileField
							.setBackground(Color.lightGray);
					paramPanel.fileLabel.setEnabled(false);
				}
			} else if (command.equals("converter-file-browser-command")) {
				File convertFile = new File(getNewSupportFileLocation() + sep
						+ "Data" + sep + "Convert");
				JFileChooser chooser = new JFileChooser(convertFile);
				chooser.setDialogTitle("Annotation Converter Selection");
				chooser.setMultiSelectionEnabled(false);
				if (chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION) {
					paramPanel.converterFileField.setText(chooser
							.getSelectedFile().getPath());

				}
				return;
			} else if (command.equals("ann-file-browser-command")) {
				File classFile = new File(getNewSupportFileLocation() + sep
						+ "Data" + sep + "Class" + sep);
				if (!classFile.canRead())
					classFile = new File("." + sep + "data" + sep + "ease");
				JFileChooser chooser = new JFileChooser(classFile);
				chooser
						.setDialogTitle("Annotation --> GO Term, File(s) Selection");
				chooser.setMultiSelectionEnabled(true);
				if (chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION) {
					paramPanel.updateAnnFileList(chooser.getSelectedFiles());
					paramPanel.removeButton.setEnabled(true);
				}
			} else if (command.equals("remove-ann-file-command")) {
				paramPanel.removeSelectedFiles();
			} else if (command.equals("ok-command")) {
				if (getNewSupportFileLocation() == null
						|| getNewSupportFileLocation().equals("")
						|| getNewSupportFileLocation().equals(" ")
						|| getNewSupportFileLocation().equals("Please select a directory")) {
					JOptionPane
							.showMessageDialog(
									parent,
									"Please enter a directory or use the file browser to select a directory for support files.",
									"EASE Initialization: Missing Parameter",
									JOptionPane.WARNING_MESSAGE);
					return;

				}

				if (isClusterAnalysis && isPopFileModeSelected()) {

					if (getPopFieldText() == null
							|| getPopFieldText().equals("")
							|| getPopFieldText().equals(" ")) {
						JOptionPane
								.showMessageDialog(
										parent,
										"Please enter a background population or use the file browser to select a file of one.",
										"EASE Initialization: Missing Parameter",
										JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
				if (getAnnToGOFileList().length == 0) {
					JOptionPane
							.showMessageDialog(
									parent,
									"You have not selected any gene annotation/gene ontology linking files. \n"
											+ "Please enter files or use the browser to select files.",
									"EASE Initialization: Missing Parameter",
									JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (isAnnBoxSelected()) {
					String fileName = getConverterFileName();
					if (fileName == null || fileName.equals("")
							|| fileName.equals(" ")) {
						JOptionPane
								.showMessageDialog(
										parent,
										"You have selected to use an annotation conversion file but have not made a file selection.\n"
												+ "Please enter a file name or browse to select a file.",
										"EASE Initialization: Missing Parameter",
										JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(EASEAdvancedFeatures.this,
						"EASE AdvancedParams Dialog");
				result = JOptionPane.CANCEL_OPTION;
				if (hw.getWindowContent()) {
					hw.setSize(600, 600);
					hw.setLocation();
					hw.show();
				} else {
					hw.setVisible(false);
					hw.dispose();
				}
			}
		}

		public void itemStateChanged(ItemEvent arg0) {

		}
	}
	
	/**
	 * Resets dialog controls. It does not perform a master reset of all dialogs
	 */
	public void resetControls() {
		/*Resets options in directory selection panel*/
		configPanelExtension.supportFileLocationField.setText("Please select a directory");
		
		/*Resets options in the population selection panel*/
		popPanel.fileButton.setSelected(true);
		popPanel.popField.setText(" ");
		popPanel.popField.setBackground(Color.white);
		popPanel.popField.setEnabled(true);
		popPanel.browseButton.setEnabled(true);
		popPanel.fileLabel.setEnabled(true);
		
		/*Resets options for the annotation key panel*/
		paramPanel.fieldNamesBox.setSelectedIndex(index);
		
		/*Resets options for the annotation conversion file*/
		paramPanel.useAnnBox.setSelected(false);
		paramPanel.browserButton.setEnabled(false);
		paramPanel.converterFileField.setText(" ");
		paramPanel.converterFileField.setEnabled(false);
		paramPanel.converterFileField
				.setBackground(Color.lightGray);
		paramPanel.fileLabel.setEnabled(false);
		
		/*Reset options for the gene ontology linking files*/
		((DefaultListModel) paramPanel.annFileList.getModel())
					.removeAllElements();
		paramPanel.removeButton.setEnabled(false);
		paramPanel.annFileList.validate();
		
	}

	public static void main(String[] args) {
		String[] labels = new String[3];
		labels[0] = "TC#";
		labels[1] = "GB#";
		labels[2] = "Role";

		EASEAdvancedFeatures advancedFeat = new EASEAdvancedFeatures(
				new JFrame(), "/ease", false, new ClusterRepository(1),
				labels);
		advancedFeat.showModal();
	}
}