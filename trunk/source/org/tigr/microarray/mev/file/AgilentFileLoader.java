package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.cluster.gui.IData;


public class AgilentFileLoader extends ExpressionFileLoader {
	private GBA gba;
	private AgilentFileLoaderPanel aflp;
	private MultipleArrayViewer mav;
	private boolean loadEnabled = false;
	private String annotationFilePath;
	private ArrayList<String> columnHeaders;
	private boolean loadMedianIntensities=false;

	public AgilentFileLoader(SuperExpressionFileLoader superLoader) {
		super(superLoader);
		gba = new GBA();
		aflp = new AgilentFileLoaderPanel();
		mav = superLoader.getArrayViewer();

	}

	public ISlideData loadExpressionFile(File f) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ISlideData> loadExpressionFiles() throws IOException {
		// TODO Auto-generated method stub
		this.loadMedianIntensities = aflp.loadMedButton.isSelected();
		return null;
	}
	/**
	 * populates slidedata from the given file
	 * @param currentFile
	 * @return
	 * @throws IOException
	 */
	public ISlideData loadSlideData(File targetFile) throws IOException {
		
		SlideData slideData = null;
		AgilentFileParser afp=new AgilentFileParser();
		afp.loadFile(targetFile);
		
		if(afp.isAgilentFileValid()) {
			
			int probeColumn=afp.getColumn(AgilentFileParser.PROBENAME);
			int rProcessedSignal=afp.getColumn(AgilentFileParser.RPROCESSEDSIGNAL);
			int gProcessedSignal = afp.getColumn(AgilentFileParser.GPROCESSEDSIGNAL);
			int row=afp.getColumn(AgilentFileParser.ROW);
			int column=afp.getColumn(AgilentFileParser.COLUMN);
			int rMedianSignal=afp.getColumn(AgilentFileParser.RMEDIANSIGNAL);
			int gMedianSignal=afp.getColumn(AgilentFileParser.GMEDIANSIGNAL);
			
			
			// Intensities exist??
			if (loadMedianIntensities && (rMedianSignal==-1 || gMedianSignal==-1)) {
				
				JOptionPane.showMessageDialog(aflp,
									"Error loading "
											+ targetFile.getName()
											+ "\n"
											+ "The file was missing median intensity columns indicated by\n"
											+ "the header names rMedianSignal and gMedianSignal",
									"Load Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}else {
				
				JOptionPane.showMessageDialog(aflp,
									"Error loading "
											+ targetFile.getName()
											+ "\n"
											+ "The file was missing intensity columns indicated by\n"
											+ "the header names rProcessedSignal and gProcessedSignal",
									"Load Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}

	
			
			
		}
	
		
	  return slideData;	
	}
	
	
	public boolean checkLoadEnable() {
		// TODO Auto-generated method stub
		setLoadEnabled(loadEnabled);
		return this.loadEnabled;

	}

	public String getAnnotationFilePath() {
		// TODO Auto-generated method stub
		return annotationFilePath;
	}

	public int getDataType() {
		// TODO Auto-generated method stub
		return IData.DATA_TYPE_RATIO_ONLY;
	}

	public JPanel getFileLoaderPanel() {
		// TODO Auto-generated method stub
		return aflp;
	}

	public String getFilePath() {
		// TODO Auto-generated method stub
		if (aflp.pathTextField == null)
			return null;
		return aflp.pathTextField.getText();

	}

	public void openDataPath() {
		// TODO Auto-generated method stub

	}

	public void setAnnotationFilePath(String filePath) {
		// TODO Auto-generated method stub
		this.annotationFilePath = filePath;
	}

	public void setFilePath(String path) {
		// TODO Auto-generated method stub
		aflp.pathTextField.setText(path);
		processFileList(path, new Vector());
	}

	public void processFileList(String filePath, Vector fileNames) {

		if (fileNames == null)
			return; // Don't process files if there aren't any

		FileFilter mevFileFilter = getFileFilter();

		aflp.getAvailableListModel().clear();

		for (int i = 0; i < fileNames.size(); i++) {

			File targetFile = new File((String) fileNames.elementAt(i));

			if (mevFileFilter.accept(targetFile)) {
				aflp.getAvailableListModel().addElement(
						new File((String) fileNames.elementAt(i)));
			}

		}
	}

	private class AgilentFileLoaderPanel extends JPanel {

		private JPanel choicePanel, annotationPanel, dataPanel;
		private JCheckBox noAnnFileBox;
		private JCheckBox saveSpotInfoBox;
		private JCheckBox cutQuotesBox;
		private JRadioButton loadIButton;
		private JRadioButton loadMedButton;
		private JPanel selectFilePanel;
		private JLabel annotationSelectionLabel;
		private JComboBox annotationSelectionBox;
		private JLabel availableLabel, selectedLabel, selectFile;
		private JList availableList, selectedList;
		private JScrollPane availableScrollPane, selectedScrollPane;
		private JButton addButton, addAllButton, removeButton, removeAllButton,
				browseButton1;
		private JTextField pathTextField;
		private static final String LOAD_AGILENT_ANNOTATION = "load_agilent_annotation_file";
		private static final String LOAD_RESOURCERER_ANNOTATION = "load_resourcerer_annotation";

		private AgilentFileLoaderPanel() {
			setLayout(new GridBagLayout());

			// choice Panel
			choicePanel = new javax.swing.JPanel();
			choicePanel.setBorder(new TitledBorder(new EtchedBorder(),
					"Choice Panel"));
			choicePanel.setLayout(new GridBagLayout());

			loadIButton = new JRadioButton("Load Integrated Spot Intensities",
					true);
			loadIButton.setFocusPainted(false);
			loadMedButton = new JRadioButton("Load Median Spot Intensities");
			loadMedButton.setFocusPainted(false);
			ButtonGroup bg = new ButtonGroup();
			bg.add(loadIButton);
			bg.add(loadMedButton);

			noAnnFileBox = new JCheckBox(
					"Use Annotation Contained in MeV File (no annotation file)",
					false);
			noAnnFileBox.setFocusPainted(false);
			noAnnFileBox.setActionCommand("use-annotation-in-mev-file");
			noAnnFileBox.addActionListener(new Listener());

			saveSpotInfoBox = new JCheckBox("Load Auxiliary Spot Information",
					false);
			saveSpotInfoBox.setFocusPainted(false);

			cutQuotesBox = new JCheckBox("Remove Annotation Quotes(\"...\")",
					false);
			cutQuotesBox.setHorizontalAlignment(JCheckBox.CENTER);
			cutQuotesBox.setFocusPainted(false);

			gba.add(choicePanel, loadIButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 2, 0, 2), 0, 0);
			gba.add(choicePanel, saveSpotInfoBox, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(0, 2, 0, 2), 0, 0);
			gba.add(choicePanel, loadMedButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 2, 0, 2), 0, 0);
			gba.add(choicePanel, noAnnFileBox, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 2, 0, 2), 0, 0);
			gba.add(choicePanel, cutQuotesBox, 0, 2, 2, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 2, 0, 2), 0, 0);

			// Annotation panel
			annotationPanel = new javax.swing.JPanel();
			annotationPanel.setLayout(new GridBagLayout());
			annotationPanel.setBorder(new TitledBorder(new EtchedBorder(),
					"Annotation Panel"));

			annotationSelectionLabel = new JLabel("Annotation selection method");

			String[] selectionMethods = new String[2];
			selectionMethods[0] = "Load Agilent provided annotation file ";
			selectionMethods[1] = "Load gene annotations from Resourcerer";

			annotationSelectionBox = new JComboBox(selectionMethods);
			annotationSelectionBox.addActionListener(new Listener());
			createAnnotationPanel(AgilentFileLoaderPanel.LOAD_AGILENT_ANNOTATION);

			gba.add(annotationPanel, annotationSelectionLabel, 0, 0, 1, 1, 1,
					0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(annotationPanel, annotationSelectionBox, 2, 0, 1, 1, 1, 0,
					GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			// gba.add(annotationPanel,selectFilePanel, 0, 1, 1, 1, 1, 1, GBA.B,
			// GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			// Data panel
			dataPanel = new javax.swing.JPanel();
			dataPanel.setLayout(new GridBagLayout());
			dataPanel.setBorder(new TitledBorder(new EtchedBorder(),
					"Agilent Feature Extraction Files (*.txt)"));

			// selectFilePanel for choosing directory
			JPanel selectFilePanel = new JPanel();
			selectFilePanel.setLayout(new GridBagLayout());

			selectFile = new JLabel(
					"Select directory containing Agilent Feature Extraction Files");

			browseButton1 = new JButton("Browse");
			browseButton1.setSize(new Dimension(100, 30));
			browseButton1.setPreferredSize(new Dimension(100, 30));
			browseButton1.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {

				}
			});

			pathTextField = new JTextField();
			pathTextField.setEditable(false);
			pathTextField.setForeground(Color.black);
			pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,
					GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,
					0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			// listPanel for available and selected file lists
			JPanel listPanel = new JPanel();
			listPanel.setLayout(new GridBagLayout());
			// listPanel.setBackground(Color.white);
			availableLabel = new JLabel("Available Files");
			selectedLabel = new JLabel("Selected Files");
			availableList = new JList(new DefaultListModel());
			selectedList = new JList(new DefaultListModel());

			availableScrollPane = new JScrollPane(availableList);
			selectedScrollPane = new JScrollPane(selectedList);

			// buttonPanel for add, remove, addall and removeall buttons
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());

			addButton = new JButton("Add");
			addButton.setPreferredSize(new Dimension(100, 20));
			addButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {
					onAdd();
				}
			});

			addAllButton = new JButton("Add All");
			addAllButton.setPreferredSize(new Dimension(100, 20));
			addAllButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {

				}
			});

			removeButton = new JButton("Remove");
			removeButton.setPreferredSize(new Dimension(100, 20));
			removeButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {

				}
			});

			removeAllButton = new JButton("Remove All");
			removeAllButton.setPreferredSize(new Dimension(100, 20));
			removeAllButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {

				}

			});

			gba.add(buttonPanel, addButton, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(buttonPanel, addAllButton, 0, 1, 1, 1, 0, 0, GBA.N, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(buttonPanel, removeButton, 0, 2, 1, 1, 0, 0, GBA.N, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(buttonPanel, removeAllButton, 0, 3, 1, 1, 0, 0, GBA.N,
					GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(listPanel, availableLabel, 0, 0, 1, 1, 0, 0, GBA.N, GBA.C,
					new Insets(5, 5, 5, 5), 0, 0);
			gba.add(listPanel, availableScrollPane, 0, 1, 1, 4, 5, 1, GBA.B,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(listPanel, buttonPanel, 1, 1, 1, 4, 1, 1, GBA.B, GBA.C,
					new Insets(5, 5, 5, 5), 0, 0);
			gba.add(listPanel, selectedLabel, 2, 0, 1, 1, 0, 0, GBA.N, GBA.C,
					new Insets(5, 5, 5, 5), 0, 0);
			gba.add(listPanel, selectedScrollPane, 2, 1, 1, 4, 5, 1, GBA.B,
					GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			gba.add(dataPanel, selectFilePanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(dataPanel, listPanel, 0, 2, 1, 1, 1, 1, GBA.B, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);

			gba.add(this, choicePanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(this, annotationPanel, 0, 2, 1, 1, 100, 100, GBA.B, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);
			gba.add(this, dataPanel, 0, 4, 1, 1, 1, 1, GBA.B, GBA.C,
					new Insets(2, 2, 2, 2), 0, 0);

			revalidate();

		}

		public void createAnnotationPanel(String actionCommand) {

			if (actionCommand.equalsIgnoreCase(LOAD_AGILENT_ANNOTATION)) {

				selectFilePanel = new JPanel();
				selectFilePanel.setLayout(new GridBagLayout());

				JLabel selectFile = new JLabel("Select Agilent annotation file");

				JTextField pathTextField = new JTextField();
				pathTextField.setEditable(false);
				pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
				pathTextField.setSize(new Dimension(500, 20));
				pathTextField.setPreferredSize(new Dimension(500, 20));

				JButton browse = new JButton("Browse");
				browse.setName("Browse");
				browse.setActionCommand("browse");
				browse.setSize(new Dimension(100, 30));
				browse.setPreferredSize(new Dimension(100, 30));
				browse.addActionListener(new Listener() {
					public void actionPerformed(ActionEvent e) {
						onSelectAgilentAnnotationFile();
					}
				});

				gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,
						GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0,
						GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				gba.add(selectFilePanel, browse, 2, 0, GBA.RELATIVE, 1, 0, 0,
						GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

				gba.add(annotationPanel, selectFilePanel, 0, 1, 1, 1, 1, 1,
						GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				revalidate();

			}

		}

		/**
		 * select Agilent provided annotation file
		 * 
		 * 
		 * 
		 */

		public void onSelectAgilentAnnotationFile() {
			JFileChooser fileChooser = new JFileChooser(
					SuperExpressionFileLoader.DATA_PATH);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int retVal = fileChooser
					.showOpenDialog(AgilentFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String path = selectedFile.getAbsolutePath();
				setAnnotationFilePath(path);
			}

		}

		/**
		 * adds a selected file from the available list to the selected list
		 * 
		 * 
		 */
		public void onAdd() {

			int[] chosenIndices = availableList.getSelectedIndices();
			Object[] chosenObjects = new Object[chosenIndices.length];

			for (int i = chosenIndices.length - 1; i >= 0; i--) {
				Object addItem = ((DefaultListModel) availableList.getModel())
						.getElementAt(chosenIndices[i]);
				chosenObjects[i] = addItem;
			}

			for (int i = 0; i < chosenIndices.length; i++) {
				((DefaultListModel) selectedList.getModel())
						.addElement(chosenObjects[i]);
			}

		}

		/**
		 * same as onAdd, but for multiple selected files
		 * 
		 * 
		 * 
		 */

		public void onAddAll() {

			int elementCount = ((DefaultListModel) availableList.getModel())
					.size();
			for (int i = 0; i < elementCount; i++) {
				Object addItem = ((DefaultListModel) availableList.getModel())
						.getElementAt(i);
				((DefaultListModel) selectedList.getModel())
						.addElement(addItem);
			}

		}
		/**
		 * removes file from the 'selected' list
		 * 
		 * 
		 * 
		 */
		public void onRemove() {

			int[] chosenIndices = selectedList.getSelectedIndices();

			// Designed with copy-then-add functionality in mind
			for (int i = chosenIndices.length - 1; i >= 0; i--) {
				((DefaultListModel) selectedList.getModel())
						.remove(chosenIndices[i]);
			}

		}
		
		/**
		 * same as onRemove, just for multiple files
		 * 
		 * 
		 */
		public void onRemoveAll() {
			((DefaultListModel) selectedList.getModel()).removeAllElements();

		}

		public DefaultListModel getAvailableListModel() {
			return (DefaultListModel) availableList.getModel();
		}

		public DefaultListModel getSelectedListModel() {
			return (DefaultListModel) selectedList.getModel();
		}

		private class Listener implements ActionListener {

			public void actionPerformed(ActionEvent e) {

			}

		}

	}

}
