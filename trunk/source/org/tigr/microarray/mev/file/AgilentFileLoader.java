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
import java.util.Arrays;
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
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;

import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.PipelinedAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

import org.tigr.microarray.util.FileLoaderUtility;


public class AgilentFileLoader extends ExpressionFileLoader {
	private GBA gba;
	private AgilentFileLoaderPanel aflp;
	private MultipleArrayViewer mav;
	

	private boolean loadEnabled = true;
	private String annotationFilePath="NA";
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
				
				File file=new File(getFilePath(),((File) dataFiles[index]).getName());
			
				slideData = loadSlideData(file);
				
				if (slideData == null)
					return null;
				data.add(slideData);
				metaData = slideData.getSlideMetaData();
			} else {
				File file=new File(getFilePath(),((File) dataFiles[index]).getName());
				data.add(loadFloatSlideData(file, metaData));
			}
			
			setRemain(dataFiles.length - index - 1);
			setFilesProgress(index);
			
	
		}
		
		if(aflp.isAnnotationLoaded()) {
			data.set(0, loadResourcererAnnotationFile((SlideData) data.elementAt(0),new File(getAnnotationFilePath())));
		}else if(!getAnnotationFilePath().equalsIgnoreCase("NA")) {
			data.set(0, loadAnnotationFile((SlideData) data.elementAt(0),new File(getAnnotationFilePath())));
			
		}
		
		
		return data;
	}
	
	
	
	public SlideData loadResourcererAnnotationFile(SlideData targetData, File sourceFile) throws IOException {
		
		this.mav.getData().setAnnotationLoaded(true);
		File annoFile=new File(getAnnotationFilePath());
		
		AnnotationFileReader afr = AnnotationFileReader.createAnnotationFileReader(new File(getAnnotationFilePath()));
		Hashtable _tempAnno = afr.getAffyAnnotation();
		chipAnno = afr.getAffyChipAnnotation();
		int dataLength = targetData.getSize();
		int probeColumn=Arrays.asList(targetData.getSlideMetaData().getFieldNames()).indexOf(AgilentFileParser.PROBENAME);
	
		
		for(int index=0; index<dataLength; index++) {
		
			
			MevAnnotation mevAnno = null;
			if(_tempAnno.size() != 0 && probeColumn!=-1) {
				String cloneName = targetData.getSlideDataElement(index).getExtraFields()[probeColumn];
				
				if (((MevAnnotation) _tempAnno.get(cloneName)) != null) {
					mevAnno = (MevAnnotation) _tempAnno.get(cloneName);
				} else {
				/*
				  * Sarita: clone ID explicitly set here because if the data file
				  * has a probe (for eg. Affy house keeping probes) for which Resourcerer
				  * does not have annotation, MeV would still work fine. NA will be
				  * appended for the rest of the fields. 
				 */
					
				mevAnno = new MevAnnotation();
				mevAnno.setCloneID(cloneName);
			}
				
		}
			targetData.getSlideDataElement(index).setElementAnnotation(mevAnno);
			
		}
		targetData.getSlideMetaData().updateFilledAnnFields();
		return targetData;
	}
	
	
	
	public SlideData loadAnnotationFile(SlideData targetData, File sourceFile) throws IOException {
		
	
		AgilentAnnotationFileParser parser=new AgilentAnnotationFileParser();
		parser.loadAnnotationFile(sourceFile);
			
		if(parser.isAnnotationLoaded()) {
		
			ArrayList<String> headers = parser.getColumnHeaders();
							
			String[][]annMatrix=parser.getAnnotationMatrix();
					
			targetData.getSlideMetaData().setFieldNames(headers.toArray(new String[headers.size()]));

			Hashtable hash = new Hashtable();
			String[] value;
			int dataLength = targetData.getSize();
			for (int i = 0; i < annMatrix.length; i++) {
				value = new String[headers.size()];
				System.arraycopy(annMatrix[i], 1, value, 0,
						headers.size());
			
				hash.put(annMatrix[i][0], value);
				
			}
			
			
			String[] extraFields;
			for (int i = 0; i < dataLength; i++) {
				extraFields = new String[headers.size()];
				extraFields=(String[])(hash.get(uidArray[i]));
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
		int numAnnotationColumns=-1;
		String[][]data=new String[1][1];
		SlideDataElement sde;
		int[] rows;
		int[] cols;
		float[] intensities;
		
		String[]annotationHeaders=null;
		
		if(afp.isAgilentFileValid()) {
			
			data=new String[afp.getDataMatrix().length][afp.getDataMatrix()[0].length];
			data=afp.getDataMatrix();
			uidArray = new String[data.length];
					
			int probeName=afp.getRequiredHeaders().indexOf(AgilentFileParser.PROBENAME);
			int genename=afp.getRequiredHeaders().indexOf(AgilentFileParser.GENENAME);
			int rProcessedSignal=afp.getRequiredHeaders().indexOf(AgilentFileParser.RPROCESSEDSIGNAL);
			int gProcessedSignal = afp.getRequiredHeaders().indexOf(AgilentFileParser.GPROCESSEDSIGNAL);
		
			int rMedianSignal=afp.getRequiredHeaders().indexOf(AgilentFileParser.RMEDIANSIGNAL);
			int gMedianSignal=afp.getRequiredHeaders().indexOf(AgilentFileParser.GMEDIANSIGNAL);
			
			
			
			if(probeName!=-1 && genename!=-1)
				numAnnotationColumns=5;
			
			
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

			
		

			int maxRow = 0;
			int maxCol = 0;
			for (int i = 0; i < data.length; i++) {
				maxRow = Math.max(maxRow, Integer.parseInt(data[i][1]));
				maxCol = Math.max(maxCol, Integer.parseInt(data[i][2]));
			}
			
			slideData = new SlideData(maxRow, maxCol);
			setLinesCount(data.length);
			
			annotationHeaders=new String[numAnnotationColumns];
			for (int fieldCnt = 0; fieldCnt < numAnnotationColumns; fieldCnt++) {
				annotationHeaders[fieldCnt] =afp.getRequiredHeaders().get(fieldCnt);
				
			}
			
			//slideData.getSlideMetaData().appendFieldNames(annotationHeaders);
			slideData.getSlideMetaData().setFieldNames(annotationHeaders);
			
			for (int i = 0; i < data.length; i++) {
				rows = new int[3];
				cols = new int[3];
				intensities = new float[2];

				uidArray[i] = data[i][0];
				
				
				String[] fieldNames = new String[numAnnotationColumns];
				for (int fieldCnt = 0; fieldCnt < numAnnotationColumns; fieldCnt++) {
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
				
					Thread thread = new Thread(new Runnable() {
						public void run() {
							JOptionPane
									.showConfirmDialog(
											aflp,
											"The input file \""
													+ fileName
													+ "\" seems to be missing critical columns like "
													+ "\n"
													+ "FeatureNum, rMedianSignal, gMedianSignal," +
															"rProcessedSignal and gProcessedSignal.",
											"Loading Aborted/Loading Error",
											JOptionPane.ERROR_MESSAGE);
						}
					});
					thread.start();
					return null;
				}
				sde = new SlideDataElement(data[i][0], rows, cols, intensities,
						fieldNames, null);
				slideData.addSlideDataElement(sde);
				
				setFileProgress(i);
			}
			
			slideData.setSlideDataName(targetFile.getName());
			slideData.setSlideFileName(targetFile.getPath());
			return slideData;	
		}
	
		return null;
	  
	}
	
	
	
	
	
	public ISlideData loadFloatSlideData(File currentFile, ISlideMetaData metaData) throws IOException {

		AgilentFileParser afp=new AgilentFileParser();
		afp.loadFile(currentFile);
		FloatSlideData floatSlideData = null;
		int intensity1=0;
		int intensity2=0;
		
		
		
		
		if (afp.isAgilentFileValid()) {

			if (loadMedianIntensities) {
				intensity1 = afp.getRequiredHeaders().indexOf(
						AgilentFileParser.RMEDIANSIGNAL);
				intensity2 = afp.getRequiredHeaders().indexOf(
						AgilentFileParser.GMEDIANSIGNAL);
			} else {
				intensity1 = afp.getRequiredHeaders().indexOf(
						AgilentFileParser.RPROCESSEDSIGNAL);
				intensity2 = afp.getRequiredHeaders().indexOf(
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
		
		if(aflp.isAnnotationLoaded()) {
			this.annotationFilePath=aflp.adh.getAnnFilePath();
			return annotationFilePath;
		}else if(annotationFilePath.length()==0) {
			return "NA";
		}else
			return annotationFilePath;
	}

	public int getDataType() {
		// TODO Auto-generated method stub
		return IData.DATA_TYPE_TWO_INTENSITY;
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
	
	public MultipleArrayViewer getMav() {
		return mav;
	}

	private class AgilentFileLoaderPanel extends JPanel {

		private JPanel choicePanel, annotationPanel, dataPanel;
		
		private JRadioButton loadIButton;
		private JRadioButton loadMedButton;
		private JPanel selectFilePanel, annotationSelectionMethodPanel;
		private JLabel annotationSelectionLabel;
		private JComboBox annotationSelectionBox;
		private JLabel availableLabel, selectedLabel, selectFile, selectAnnotationFile;
		private JList availableList, selectedList;
		private JScrollPane availableScrollPane, selectedScrollPane;
		private JButton addButton, addAllButton, removeButton, removeAllButton,
				browseButton1, annBrowseButton;
		private JTextField pathTextField, annPathTextField;
		private AnnotationDownloadHandler adh=null;
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

			

			gba.add(choicePanel, loadIButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 2, 0, 2), 0, 0);
			gba.add(choicePanel, loadMedButton, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(0, 2, 0, 2), 0, 0);
		
			// Annotation panel
			annotationPanel = new javax.swing.JPanel();
			annotationPanel.setLayout(new GridBagLayout());
			annotationPanel.setBorder(new TitledBorder(new EtchedBorder(),
					"Annotation Panel"));
			
			annotationSelectionMethodPanel=new javax.swing.JPanel();
			annotationSelectionLabel = new JLabel("Annotation selection method");

			String[] selectionMethods = new String[2];
			selectionMethods[0] = "Load Agilent provided annotation file";
			selectionMethods[1] = "Load gene annotations from Resourcerer";

			annotationSelectionBox = new JComboBox(selectionMethods);
			annotationSelectionBox.addActionListener(new Listener());
			createAnnotationPanel(AgilentFileLoaderPanel.LOAD_AGILENT_ANNOTATION);

			gba.add(annotationSelectionMethodPanel, annotationSelectionLabel, 0, 0, 1, 1, 1,
					0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(annotationSelectionMethodPanel, new JLabel(), 0, 1, 1, 1, 1,
					0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(annotationSelectionMethodPanel, annotationSelectionBox, 2, 0, 1, 1, 1, 0,
					GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(annotationPanel, annotationSelectionMethodPanel, 0, 0, 1, 1, 1, 0,
					GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			
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
					onAddAll();
				}
			});

			removeButton = new JButton("Remove");
			removeButton.setPreferredSize(new Dimension(100, 20));
			removeButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {
					onRemove();
				}
			});

			removeAllButton = new JButton("Remove All");
			removeAllButton.setPreferredSize(new Dimension(100, 20));
			removeAllButton.addActionListener(new Listener() {
				public void actionPerformed(ActionEvent e) {
					onRemoveAll();
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

				selectAnnotationFile = new JLabel("Select Agilent annotation file");

				annPathTextField = new JTextField();
				annPathTextField.setEditable(false);
				annPathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
			
				annBrowseButton = new JButton("Browse");
				annBrowseButton.setName("Browse");
				annBrowseButton.setActionCommand("browse");
				annBrowseButton.setSize(new Dimension(100, 30));
				annBrowseButton.setPreferredSize(new Dimension(100, 30));
				annBrowseButton.addActionListener(new Listener() {
					public void actionPerformed(ActionEvent e) {
						int result=onSelectAgilentAnnotationFile();
						if(result==JOptionPane.OK_OPTION)
							annPathTextField.setText(getAnnotationFilePath());
						
					}
				});

				gba.add(selectFilePanel, selectAnnotationFile, 0, 0, 1, 1, 0, 0, GBA.B,
						GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				gba.add(selectFilePanel, annPathTextField, 1, 0, 1, 1, 1, 0,
						GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				gba.add(selectFilePanel, annBrowseButton, 2, 0, GBA.RELATIVE, 1, 0, 0,
						GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				
							
				gba.add(annotationPanel, selectFilePanel, 0, 1, 1, 1, 1, 1,
						GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				revalidate();

			}else if(actionCommand.equalsIgnoreCase(LOAD_RESOURCERER_ANNOTATION)) {
				IResourceManager irm;
				Hashtable<String, Vector<String>> speciestoarrays = new Hashtable<String, Vector<String>>();
				try {
					irm = new FileResourceManager(new File(new File(System
							.getProperty("user.home"), ".mev"), "repository"));
					irm.setAskToGetOnline(false);
				} catch (RepositoryInitializationError rie) {
					rie.printStackTrace();
					return;
				}

				try {
					File taxonfile = irm.getSupportFile(
							new PipelinedAnnotationsFileDefinition(), true);
					PipelinedAnnotationsFileDefinition aafd = new PipelinedAnnotationsFileDefinition();
					speciestoarrays = aafd.parseAnnotationListFile(taxonfile);
				} catch (SupportFileAccessError sfae) {
					// fail("Couldn't get species/array mappings from repository.");
				} catch (IOException ioe) {
					ioe.printStackTrace();
					// fail("Couldn't get annotation file.");
				}

				adh = new AnnotationDownloadHandler(irm, speciestoarrays,
						"Human", "affy_HG-U133A");
				selectFilePanel = new JPanel();
				selectFilePanel.setLayout(new GridBagLayout());

				if (getMav().getData().isAnnotationLoaded()) {

					annotationPanel.setVisible(false);
					adh.setOptionalMessage("Annotation is already loaded for array "
							+ getMav().getData().getChipAnnotation()
							.getChipType());
					adh.setAnnFilePath(getMav().getData().getChipAnnotation()
							.getAnnFileName());
					
				}
				adh.addListener(new Listener());
				gba.add(selectFilePanel, adh.getAnnotationLoaderPanel(gba), 0,
						0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0,
						0);
				gba.add(annotationPanel, selectFilePanel, 0, 1, 1, 1, 1, 1,
						GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
				revalidate();

				adh.setDownloadEnabled(!getMav().getData()
						.isAnnotationLoaded());
				
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

		public int onSelectAgilentAnnotationFile() {
			JFileChooser fileChooser = new JFileChooser(
					SuperExpressionFileLoader.DATA_PATH);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			int retVal = fileChooser
					.showOpenDialog(AgilentFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String path = selectedFile.getAbsolutePath();
				setAnnotationFilePath(path);
				
			}
			return retVal;
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
		
		private void updateLabel(String name) {
			annotationSelectionBox.setSelectedItem(name);
			
		}
		
		private boolean isAnnotationLoaded() {
			if(adh!=null) {
				return adh.isAnnotationSelected();
			}
			else
				return false;
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
				}else if(source.equals(annotationSelectionBox)) {
					updateLabel((String)annotationSelectionBox.getSelectedItem());
					
					if(((String)annotationSelectionBox.getSelectedItem()).equalsIgnoreCase("Load gene annotations from Resourcerer")){
						
						annotationPanel.removeAll();
						revalidate();
						createAnnotationPanel(AgilentFileLoaderPanel.LOAD_RESOURCERER_ANNOTATION);

						gba.add(annotationSelectionMethodPanel, annotationSelectionLabel, 0, 0, 1, 1, 1,
								0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
						gba.add(annotationSelectionMethodPanel, annotationSelectionBox, 2, 0, 1, 1, 1, 0,
								GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
						gba.add(annotationPanel, annotationSelectionMethodPanel, 0, 0, 1, 1, 1, 0,
								GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
					
						revalidate();

					}else if(((String)annotationSelectionBox.getSelectedItem()).equalsIgnoreCase("Load Agilent provided annotation file")){
						annotationPanel.removeAll();
						revalidate();
						createAnnotationPanel(AgilentFileLoaderPanel.LOAD_AGILENT_ANNOTATION);

						gba.add(annotationSelectionMethodPanel, annotationSelectionLabel, 0, 0, 1, 1, 1,
								0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
						gba.add(annotationSelectionMethodPanel, annotationSelectionBox, 2, 0, 1, 1, 1, 0,
								GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
						gba.add(annotationPanel, annotationSelectionMethodPanel, 0, 0, 1, 1, 1, 0,
								GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
					
						revalidate();
					}
					
					
				}
				
			}

		}
		
		
		
		
		
		
	}

}
