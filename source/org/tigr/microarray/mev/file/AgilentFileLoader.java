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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

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
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.cluster.gui.IData;

import org.tigr.microarray.util.FileLoaderUtility;


public class AgilentFileLoader extends ExpressionFileLoader {
	private GBA gba;
	private AgilentFileLoaderPanel aflp;
	private MultipleArrayViewer mav;
	private boolean loadEnabled = true;
	private String annotationFilePath;
	private ArrayList<String> columnHeaders;
	private boolean loadMedianIntensities=false;
	private String[] uidArray;

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
		
		Object[] dataFiles = aflp.getSelectedListModel().toArray();
		
		Vector<ISlideData> data = new Vector<ISlideData>();
		ISlideMetaData metaData = null;
		ISlideData slideData=null;
		
		setFilesCount(dataFiles.length);
		setRemain(dataFiles.length);

		
		for(int index=0; index<dataFiles.length; index++) {
			if (index == 0) {
				
				File file=new File(this.aflp.pathTextField.getText(),((File) dataFiles[index]).getName());
				slideData = loadSlideData(file);
				
				if (slideData == null)
					return null;
				data.add(slideData);
				metaData = slideData.getSlideMetaData();
			} else {
				File file=new File(this.aflp.pathTextField.getText(),((File) dataFiles[index]).getName());
				data.add(loadFloatSlideData(file, metaData));
			}
			
			setRemain(dataFiles.length - index - 1);
			setFilesProgress(index);
			
	
		}
		
		
		if(!getAnnotationFilePath().equalsIgnoreCase("NA")) {
			data.set(0, loadAnnotationFile((SlideData) data.elementAt(0),new File(getAnnotationFilePath())));
		}
		
		if(!AgilentAnnotationFileParser.isAnnotationLoaded()) {
			String msg = "The selected annotation file";
			msg += " is in a different format than what MeV expects (Agilent feature extraction software version 10.7)\n";
			JOptionPane.showMessageDialog(aflp, msg,
					"Annotation Mismatch Warning", JOptionPane.WARNING_MESSAGE);
		}
		
		
		return data;
	}
	
	
		
	
	public SlideData loadAnnotationFile(SlideData targetData, File sourceFile) throws IOException {
		
	
		AgilentAnnotationFileParser parser=new AgilentAnnotationFileParser();
		parser.loadAnnotationFile(sourceFile);
		
		if(parser.isAnnotationLoaded()) {
			ArrayList<String> headers = parser.getColumnHeaders();
			int firstAnnField=1;
			
			if(headers.indexOf(AgilentAnnotationFileParser.COLUMN)!=-1 &&
					headers.indexOf(AgilentAnnotationFileParser.ROW)!=-1) {
				firstAnnField=3;
			}
			
			
			String[][]annMatrix=parser.getAnnotationMatrix();
			
			
			String[] annotHeaders = new String[headers.size() - firstAnnField];
			int headerIndex=0;
			for (int i = 1; i < headers.size(); i++) {
				
				if(!((String)headers.get(i)).equalsIgnoreCase(AgilentAnnotationFileParser.COLUMN)
						&& !((String)headers.get(i)).equalsIgnoreCase(AgilentAnnotationFileParser.ROW)) {
					annotHeaders[headerIndex] = (String) headers.get(i);
					headerIndex=headerIndex+1;
				}
			}
			targetData.getSlideMetaData().appendFieldNames(annotHeaders);

			Hashtable hash = new Hashtable();
			String[] value;
			int dataLength = targetData.size();
			for (int i = 0; i < annMatrix.length; i++) {
				value = new String[annMatrix[i].length - firstAnnField];
				System.arraycopy(annMatrix[i], firstAnnField, value, 0,
						annMatrix[i].length - firstAnnField);
				hash.put(annMatrix[i][0], value);
			}
			
			SlideDataElement sde;
			String[] extraFields;
			for (int i = 0; i < dataLength; i++) {
				extraFields = (String[]) (hash.get(targetData.getFieldNames()[0]));
				((SlideDataElement) targetData.getSlideDataElement(i))
						.setExtraFields(extraFields);
			}
			
			
		}
		
		return targetData;

		
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
			
			int probeColumn=afp.getRequiredHeaders().get(AgilentFileParser.PROBENAME);
			int rProcessedSignal=afp.getRequiredHeaders().get(AgilentFileParser.RPROCESSEDSIGNAL);
			int gProcessedSignal = afp.getRequiredHeaders().get(AgilentFileParser.GPROCESSEDSIGNAL);
			int row=afp.getRequiredHeaders().get(AgilentFileParser.ROW);
			int column=afp.getRequiredHeaders().get(AgilentFileParser.COLUMN);
			int rMedianSignal=afp.getRequiredHeaders().get(AgilentFileParser.RMEDIANSIGNAL);
			int gMedianSignal=afp.getRequiredHeaders().get(AgilentFileParser.GMEDIANSIGNAL);
			int featureNum=afp.getRequiredHeaders().get(AgilentFileParser.FEATURENUMBER);
			
			
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
			}else if((rProcessedSignal==-1 || gProcessedSignal==-1)){
				
				JOptionPane.showMessageDialog(aflp,
									"Error loading "
											+ targetFile.getName()
											+ "\n"
											+ "The file was missing intensity columns indicated by\n"
											+ "the header names rProcessedSignal and gProcessedSignal",
									"Load Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}

			
			String[][]data=afp.getDataMatrix();
			SlideDataElement sde;
			int[] rows;
			int[] cols;
			float[] intensities;
			String uid;
			String[][] spotData;
			uidArray = new String[data.length];

			int maxRow = 0;
			int maxCol = 0;
			for (int i = 0; i < data.length; i++) {
				maxRow = Math.max(maxRow, Integer.parseInt(data[i][1]));
				maxCol = Math.max(maxCol, Integer.parseInt(data[i][2]));
			}
			
			slideData = new SlideData(maxRow, maxCol);
			setLinesCount(data.length);
			
			for (int i = 0; i < data.length; i++) {
				rows = new int[3];
				cols = new int[3];
				intensities = new float[2];

				uidArray[i] = data[i][0];
				
				
				String[] fieldNames = new String[rProcessedSignal];
				for (int fieldCnt = 0; fieldCnt < rProcessedSignal; fieldCnt++) {
					fieldNames[fieldCnt] = data[i][fieldCnt];
				}

				try {
					if(loadMedianIntensities) {
						intensities[0] = Float.parseFloat(data[i][rMedianSignal]);
						intensities[1] = Float.parseFloat(data[i][gMedianSignal]);
					}else {
						intensities[0] = Float.parseFloat(data[i][rProcessedSignal]);
						intensities[1] = Float.parseFloat(data[i][gProcessedSignal]);
					}
					rows[0] = Integer.parseInt(data[i][1]);
					cols[0] = Integer.parseInt(data[i][2]);
					rows[1] = 0;
					cols[1] = 0;
					
						rows[2] = 0;
						cols[2] = 0;
					
				} catch (NumberFormatException e) {
					final String fileName = targetFile.getName();
					final int loc = i;
					Thread thread = new Thread(new Runnable() {
						public void run() {
							JOptionPane
									.showConfirmDialog(
											aflp,
											"The input file \""
													+ fileName
													+ "\" was missing critical information on line # "
													+ String.valueOf(loc + 1)
													+ "\n"
													+ "Agilent two color file loading require entries for FeatureNum, rMedianSignal, gMedianSignal," +
															"rProcessedSignal and gProcessedSignal.",
											"Loading Aborted/Loading Error",
											JOptionPane.ERROR_MESSAGE);
						}
					});
					thread.start();
					return null;
				}
				sde = new SlideDataElement(data[i][0], rows, cols, intensities,
						null);
				slideData.add(sde);
				slideData.getSlideMetaData().appendFieldNames(fieldNames);
				setFileProgress(i);
			}
			
			
		}
		slideData.setSlideDataName(targetFile.getName());
		slideData.setSlideFileName(targetFile.getPath());
		
	  return slideData;	
	}
	
	
	
	
	
	public ISlideData loadFloatSlideData(File currentFile, ISlideMetaData metaData) throws IOException {
		
		AgilentFileParser afp=new AgilentFileParser();
		afp.loadFile(currentFile);
		FloatSlideData floatSlideData = null;
		int intensity1=0;
		int intensity2=0;
		
		
		
		
		if (afp.isAgilentFileValid()) {

			if (loadMedianIntensities) {
				intensity1 = afp.getRequiredHeaders().get(
						AgilentFileParser.RMEDIANSIGNAL);
				intensity2 = afp.getRequiredHeaders().get(
						AgilentFileParser.GMEDIANSIGNAL);
			} else {
				intensity1 = afp.getRequiredHeaders().get(
						AgilentFileParser.RPROCESSEDSIGNAL);
				intensity2 = afp.getRequiredHeaders().get(
						AgilentFileParser.GPROCESSEDSIGNAL);
			}

			if ((intensity1 == -1 || intensity2 == -1)) {
				if(loadMedianIntensities) {
				JOptionPane
						.showMessageDialog(
								aflp,
								"Error loading "
										+ currentFile.getName()
										+ "\n"
										+ "The file was missing median intensity columns indicated by\n"
										+ "the header names rMedianSignal and gMedianSignal",
								"Load Error", JOptionPane.ERROR_MESSAGE);
				}else {
					JOptionPane
					.showMessageDialog(
							aflp,
							"Error loading "
									+ currentFile.getName()
									+ "\n"
									+ "The file was missing intensity columns indicated by\n"
									+ "the header names rProcessedSignal and gProcessedSignal",
							"Load Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			

			String[][] data = afp.getDataMatrix();
			setLinesCount(data.length);

			for (int i = 0; i < data.length; i++) {
				floatSlideData.setIntensities(i, Float
						.parseFloat(data[i][intensity1]), Float
						.parseFloat(data[i][intensity2]));
				setFileProgress(i);
			}
		}
		floatSlideData.setSlideDataName(currentFile.getName());
		floatSlideData.setSlideFileName(currentFile.getPath());
		
		return floatSlideData;
		
	}
	
	
	
	
	
	public boolean checkLoadEnable() {
		// TODO Auto-generated method stub
		setLoadEnabled(loadEnabled);
		return this.loadEnabled;

	}

	public String getAnnotationFilePath() {

		if(annotationFilePath.isEmpty()) {
			return "NA";
		}else
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
					onFeatureFileBrowse();
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
		
		
		
		public void onFeatureFileBrowse() {
			FileLoaderUtility fileLoad=new FileLoaderUtility();
			Vector<String> retrievedFileNames=new Vector<String>();
			JFileChooser fileChooser = new JFileChooser(
					SuperExpressionFileLoader.DATA_PATH);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
			int retVal = fileChooser.showOpenDialog(AgilentFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				((DefaultListModel) getAvailableListModel()).clear();
				((DefaultListModel) getSelectedListModel()).clear();

				File selectedFile = fileChooser.getSelectedFile();
				String path=selectedFile.getAbsolutePath();
				retrievedFileNames=fileLoad.getFileNameList(selectedFile.getAbsolutePath());
				//retrievedFileNames=fileLoad.getFileNameList(path);
				
				
				for (int i = 0; i < retrievedFileNames.size(); i++) {
					
					Object fileName=retrievedFileNames.get(i);
					boolean acceptFile=getFileFilter().accept((File)fileName);
					
					
					
					if(acceptFile) {
						pathTextField.setText(path);
						String Name=fileChooser.getName((File) fileName);
						
						
					((DefaultListModel) getAvailableListModel())
							.addElement(new File(Name));
					}
				}

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
				Object source = e.getSource();

				if (source == addButton) {
					onAdd();
				} else if (source == addAllButton) {
					onAddAll();
				} else if (source == removeButton) {
					onRemove();
				} else if (source == removeAllButton) {
					onRemoveAll();
				} 
				
			}

		}

	}

}
