/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.file;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AffymetrixAnnotationParser;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;

public class TCGADownloader extends ExpressionFileLoader {

	private static final long serialVersionUID = 1L;
	private GBA gba;
	private int dataType = IData.DATA_TYPE_AFFY_ABS;
	private boolean stop = false;
	private TCGADownloaderPanel tcgaDP;
	ExpressionFileTableCellRenderer myCellRenderer;
	protected String[] moreFields = new String[] {};

	private JRadioButton dataTypeSFRB;
	private JRadioButton dataTypeRNARB;	
	JCheckBox selectAllCB;
	private ISlideData[] slideDataArray = null;
	private ISlideDataElement sde;

	private Hashtable _tempAnno = new Hashtable();
	private MultipleArrayViewer mav;
	private String testTCGAURL = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/gbm/cgcc/broad.mit.edu/ht_hg-u133a/transcriptome/broad.mit.edu_GBM.HT_HG-U133A.Level_2.7.1004.0/5500024037497121008340.D07.level2.data.txt";
	private String tcgaURLBase = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/";
	File tcgaDownloadFile;
	Progress dlProgress;

    public void setFilePath(String path) {
    	tcgaDP.setFileName(path);
    	processTCGAFile(testTCGAURL);
    }

	public TCGADownloader(SuperExpressionFileLoader superLoader) {
		super(superLoader);
		this.mav = superLoader.getArrayViewer();
		gba = new GBA();
		tcgaDP = new TCGADownloaderPanel();
	}
	

    public Vector<ISlideData> loadExpressionFiles() throws IOException {
    	if (this.tcgaDP.getXColumn()==0){
    		JOptionPane.showMessageDialog(null, 
					"The selected file has no gene annotation and cannot be loaded. " +
					"\n" +
					"\nPlease make sure you have selected an EXPRESSION value in the file loader table." +
					"\nThe 1st column cannot contain expression values.", 
					"Missing Annotation Error", JOptionPane.ERROR_MESSAGE, null);
    		return null;
    	}
    		
        return loadTCGAasStanfordExpressionFile(tcgaDownloadFile, 
        		this.tcgaDP.getXRow()+1,
        		this.tcgaDP.getXColumn());
	}

	public ISlideData loadExpressionFile(File f) {
		return null;
	}
	public boolean canAutoLoad(File f) {return true;}
	
	
    public Vector<ISlideData> loadTCGAasStanfordExpressionFile(File f, int rowcoord, int colcoord) throws IOException {
    	int preSpotRows, preExperimentColumns;
    		preSpotRows = rowcoord;
    		preExperimentColumns = colcoord; 
		int numLines = this.getCountOfLines(f);
		int spotCount = numLines - preSpotRows;

		if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
		}

		int[] rows = new int[] { 0, 1, 0 };
		int[] columns = new int[] { 0, 1, 0 };
		String value;
		float cy3, cy5;
		moreFields = new String[preExperimentColumns];

		final int rColumns = 1;
		final int rRows = spotCount;

		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringSplitter ss = new StringSplitter((char) 0x09);
		String currentLine;
		int counter, row, column;
		counter = 0;
		row = column = 1;
		this.setFilesCount(1);
		this.setRemain(1);
		this.setFilesProgress(0);
		this.setLinesCount(numLines);
		this.setFileProgress(0);

        
		if(isAnnotationSelected()) {
			this.mav.getData().setAnnotationLoaded(true);
			File annoFile=new File(getAnnotationFilePath());
			String extension=(annoFile.getName()).substring((annoFile.getName()).lastIndexOf('.')+1, annoFile.getName().length());
			
			if(annoFile.getName().endsWith("annot.csv")){
				//System.out.println("Ends with annot.csv");
				AffymetrixAnnotationParser aafp = AffymetrixAnnotationParser.createAnnotationFileParser(new File(getAnnotationFilePath()));
				_tempAnno = aafp.getAffyAnnotation();
				//chipAnno = aafp.getAffyChipAnnotation();
			}
				
			if(extension.equalsIgnoreCase("txt")){
				AnnotationFileReader afr = AnnotationFileReader.createAnnotationFileReader(new File(getAnnotationFilePath()));
				_tempAnno = afr.getAffyAnnotation();
				chipAnno = afr.getAffyChipAnnotation();
			}
		}

     

		while ((currentLine = reader.readLine()) != null) {
			try {
				if (stop) {
					return null;
				}
				while (currentLine.endsWith("\t")) {
					currentLine = currentLine.substring(0, currentLine.length() - 1);
				}

				ss.init(currentLine);
				if (counter == 0) { // parse header
					
					int experimentCount = ss.countTokens() + 1 - preExperimentColumns;
					SampleAnnotation sampAnn=new SampleAnnotation();
					slideDataArray = new ISlideData[experimentCount];
					slideDataArray[0] = new SlideData(rRows, rColumns, sampAnn);
					slideDataArray[0].setSlideFileName(f.getPath());
					for (int i = 1; i < slideDataArray.length; i++) {
						 sampAnn=new SampleAnnotation();
						slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount, sampAnn);
						slideDataArray[i].setSlideFileName(f.getPath());
					}
					//get Field Names
					String[] fieldNames = new String[preExperimentColumns];
					for (int i = 0; i < preExperimentColumns; i++) {
						fieldNames[i] = ss.nextToken();
					}
					slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);

					for (int i = 0; i < experimentCount; i++) {
						
						String val=ss.nextToken();
						slideDataArray[i].setSampleAnnotationLoaded(true);
						slideDataArray[i].getSampleAnnotation().setAnnotation("Default Slide Name", val);
						slideDataArray[i].setSlideDataName(val);
						
						this.mav.getData().setSampleAnnotationLoaded(true);
				
						
					}
				} else if (counter >= preSpotRows) { 
					rows[0] = rows[2] = row;
					columns[0] = columns[2] = column;
					if (column == rColumns) {
						column = 1;
						row = row + 1;
					} else {
						column++;
					}
					for (int i = 0; i < preExperimentColumns; i++) {
						moreFields[i] = ss.nextToken();
					}

					String cloneName = moreFields[0];
					
					MevAnnotation mevAnno = null;
					if(_tempAnno.size() != 0) {
						if (((MevAnnotation) _tempAnno.get(cloneName)) != null) {
							mevAnno = (MevAnnotation) _tempAnno.get(cloneName);
						} else {
							mevAnno = new MevAnnotation();
							mevAnno.setCloneID(cloneName);
						}
					}
					if(getDataType() == TMEV.DATA_TYPE_AFFY) {
						sde = new AffySlideDataElement(String.valueOf(row + 1), rows, columns, new float[2], moreFields, mevAnno);
					} else {
						sde = new SlideDataElement(String.valueOf(row + 1), rows, columns, new float[2], moreFields, mevAnno);
					}
                
					slideDataArray[0].addSlideDataElement(sde);

					for (int i = 0; i < slideDataArray.length; i++) {
						cy3 = 1f; 
						try {
							value = ss.nextToken();
							cy5 = Float.parseFloat(value);
						} catch (Exception e) {
							cy3 = 0;
							cy5 = Float.NaN;
						}
						slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
					}
				} else {

					//advance to sample key
					for (int i = 0; i < preExperimentColumns - 1; i++) {
						ss.nextToken();
					}
					String key = ss.nextToken();

					for (int j = 0; j < slideDataArray.length; j++) {
						
						if(slideDataArray[j].getSampleAnnotation()!=null){
						
							String val=ss.nextToken();
							slideDataArray[j].getSampleAnnotation().setAnnotation(key, val);
							
						}else{
								SampleAnnotation sampAnn=new SampleAnnotation();
								sampAnn.setAnnotation(key, ss.nextToken());
								slideDataArray[j].setSampleAnnotation(sampAnn);
								slideDataArray[j].setSampleAnnotationLoaded(true);
						}
					}
				}

				this.setFileProgress(counter);

				counter++;
			} catch (NoSuchElementException nsee) {
				//Blank or corrupted line. Ignore.
			}
		}
		reader.close();

        Vector<ISlideData> data = new Vector<ISlideData>(slideDataArray.length);

		for (int i = 0; i < slideDataArray.length; i++)
			data.add(slideDataArray[i]);

		this.setFilesProgress(1);
		return data;
	}

    
	public FileFilter getFileFilter() {

		FileFilter mevFileFilter = new FileFilter() {

			public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt")) return true;
                else return false;
			}

			public String getDescription() {
				return "Tab Delimited, Multiple Sample Files (TDMS) (*.txt)";
			}
		};

		return mevFileFilter;
	}

	public boolean checkLoadEnable() {


		int tableRow = tcgaDP.getXRow() + 1; // Adjusted by 1 to account for the table header
		int tableColumn = tcgaDP.getXColumn();

        if (tableColumn < 0) return false;

		TableModel model = tcgaDP.getTable().getModel();
		String fieldSummary = "";
		for (int i = 0; i < tableColumn; i++) {
			fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
		}

        
        
		if (tableRow >= 1 && tableColumn >= 0) {
			setLoadEnabled(true);
			return true;
		} else {
			setLoadEnabled(false);
			return false;
		}
	}

	public boolean validateFile(File targetFile) {
		return true; // For now, no validation on Stanford Files
	}

	public JPanel getFileLoaderPanel() {
		return tcgaDP;
	}
    
	public void processTCGAFile(String tcgaURL) {
		Vector<String> columnHeaders = new Vector<String>();
		Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
		Vector<String> rowVector = null;
		BufferedReader reader = null;
		BufferedWriter out = null;
		String currentLine = null;
		String fileName = "tcgaDownload_"+System.currentTimeMillis()+".txt";
		tcgaDownloadFile = new File(fileName);
		try {
			tcgaDownloadFile.createNewFile();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(fileName);
		} catch (IOException e2) {
			e2.printStackTrace();
		}


		tcgaDP.setFileName(fileName);
		System.out.println("this.getFilePath() = "+this.getFilePath());
		int numFiles = tcgaDP.filesList.getSelectedIndices().length;
		DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		if (dataTypeSFRB.isSelected()){
			ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
			for (int i=0; i<tcgaDP.filesList.getSelectedIndices().length; i++){
				dlProgress.setDescription("Download Progress: "+(i+1)+" of "+tcgaDP.filesList.getSelectedIndices().length);
				try{	
					allData.add(new ArrayList<String>());
					URL url = new URL(tcgaURL+tcgaDP.filesList.getSelectedValues()[i]); 	
					URLConnection url_conn = url.openConnection();
					url_conn.setDoInput(true);
					url_conn.setUseCaches(true);
			
					reader = new BufferedReader( new InputStreamReader(url_conn.getInputStream()));
					while ((currentLine = reader.readLine()) != null) {
						if (i==0)
							allData.get(i).add(currentLine);
						else
							allData.get(i).add(currentLine.split("\t")[1]);
					}
				}catch (Exception e){
					System.out.println("error parsing file: "+tcgaURL+tcgaDP.filesList.getSelectedValues()[i]);
				}
			}
	
			try {
				out = new BufferedWriter(fstream);
				for (int i=0; i<allData.get(0).size(); i++){
					dlProgress.setDescription("Processing Data: "+(i+1)+" of "+allData.get(0).size());			
					String line = "";
	
					try{
						for (int j=0; j<allData.size(); j++){
							line += ((j==0?"":"\t")+allData.get(j).get(i));
						}
					} catch (Exception e){
						
					}
					out.write(line+"\n");
				}
				out.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		if (dataTypeRNARB.isSelected()){
			try {
				out = new BufferedWriter(fstream);
				BufferedReader[] readerArr = new BufferedReader[numFiles];
				for (int i=0; i<numFiles; i++){
					dlProgress.setDescription("Download Progress: "+(i+1)+" of "+numFiles);
					try{	
						URL url = new URL(tcgaURL+tcgaDP.filesList.getSelectedValues()[i]); 	
						URLConnection url_conn = url.openConnection();
						url_conn.setDoInput(true);
						url_conn.setUseCaches(true);
		
						readerArr[i] = new BufferedReader( new InputStreamReader(url_conn.getInputStream()));
					} catch (Exception e){
						System.out.println("error parsing file: ");
						e.printStackTrace();
					}
				}
				out.write("Tracking ID\t");
				out.write("locus\t");
				out.write("nearest_ref_id\t");
				out.write("class_code\t");
				out.write("transcript_length");
				for (int i=0; i<numFiles; i++){
					String sampleName = (String) tcgaDP.filesList.getSelectedValues()[i];
					if (sampleName.length()>50)
						sampleName = sampleName.substring(0, 49)+"...";
					readerArr[i].readLine();
					out.write("\t"+sampleName);	
					out.write("\t"+sampleName);			
				}
				out.write("\n");
				boolean notPageEnd = true;
				int index = 0;
				while (notPageEnd ){
					dlProgress.setDescription("Processing row: "+index);
					for (int i=0; i<numFiles; i++){
						currentLine = readerArr[i].readLine();
						if (currentLine==null){
							notPageEnd = false;
							break;
						}
						String[] curLine = currentLine.split("\t");
						if (i==0){
							out.write("ID_"+index+"\t");
							out.write(curLine[0]+"\t");
							out.write("\t");
							out.write("\t");
							out.write("\t");
						}
						out.write(curLine[3]+"\t");
						out.write(curLine[1]);	
						if (i!=numFiles-1)
							out.write("\t");
					}		
					out.write("\n");			
					index++;
				}		
				out.close();
				
					
				} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			StringSplitter ss = new StringSplitter('\t');
			reader = new BufferedReader(new FileReader(tcgaDownloadFile));
			currentLine = reader.readLine();
			while (currentLine.endsWith("\t")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}
			ss.init(currentLine);

			for (int i = 0; i < ss.countTokens() + 1; i++) {
				columnHeaders.add(ss.nextToken());
			}

			model.setColumnIdentifiers(columnHeaders);
			
			int cnt = 0;
			while ((currentLine = reader.readLine()) != null && cnt < 100) {
				cnt++;
				ss.init(currentLine);
				rowVector = new Vector<String>();
				for (int i = 0; i < ss.countTokens() + 1; i++) {
					try {
						rowVector.add(ss.nextToken());
					} catch (java.util.NoSuchElementException nsee) {
						rowVector.add(" ");
					}
				}

				dataVector.add(rowVector);
				model.addRow(rowVector);

			}
			out.close();
			reader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		tcgaDP.setTableModel(model);
        Point p = getFirstExpressionCell(dataVector);
        tcgaDP.setSelectedCell(p.x, p.y);
        dlProgress.dispose();
	}

	public String getFilePath() {
		return this.tcgaDP.fileNameTextField.getText();
	}

	public String getAnnotationFilePath() {
		return this.tcgaDP.getAnnFilePath();
	}

	public void openDataPath() {
	}


	public void setDataType(int data_Type) {
		if (data_Type != -1)
			dataType = data_Type;
		else
			dataType = IData.DATA_TYPE_RATIO_ONLY;
	}

	public int getDataType() {
		return dataType;
	}
	public boolean isAnnotationSelected() {
		return tcgaDP.adh.annotationSelected;
	}
    

	private class TCGADownloaderPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		JTextField fileNameTextField;

		JTextField selectedFiles;
		JTable expressionTable;
		JLabel instructionsLabel;
		JScrollPane tableScrollPane;
		JPanel tablePanel;
		JPanel fileLoaderPanel;

		private int xRow = -1;
		private int xColumn = -1;

		JPanel fileSelectionPanel;
		JLabel fileSelectionLabel, dataSelection;
		JButton downloadButton;
		JRadioButton twoColorArray;
		JRadioButton singleColorArray;
		
		AnnotationDownloadHandler adh;

		protected EventListener eventListener;

		private JList filesList;

		private LevelCBListener levelCBListener;

		private JComboBox[] tcgaLevelBoxes;

		private DefaultListModel dlm;

		public TCGADownloaderPanel() {
			super();
			
			adh = new AnnotationDownloadHandler(superLoader.viewer.getResourceManager(), superLoader.annotationLists, superLoader.defaultSpeciesName, superLoader.defaultArrayName);
			
			eventListener = new EventListener();
			levelCBListener = new LevelCBListener();
			setLayout(new GridBagLayout());

			fileNameTextField = new JTextField();
			fileNameTextField.setEditable(false);
			fileNameTextField.setForeground(Color.black);
			fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			selectedFiles = new JTextField();
			selectedFiles.setEditable(false);
			selectedFiles.setForeground(Color.black);
			selectedFiles.setFont(new Font("monospaced", Font.BOLD, 12));

			fileSelectionLabel = new JLabel();
			fileSelectionLabel.setForeground(java.awt.Color.BLACK);
			String fileTypeChoices = "<html> Selected files </html>";
			fileSelectionLabel.setText(fileTypeChoices);

			dataSelection = new JLabel();
			dataSelection.setForeground(java.awt.Color.BLACK);
			String chooseFile = "<html>Select expression data file</html>";
			dataSelection.setText(chooseFile);

			fileSelectionPanel = new JPanel();
			fileSelectionPanel.setLayout(new GridBagLayout());
			fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "TCGA Data Downloader"));

			tcgaLevelBoxes = new JComboBox[7];
			JLabel[] tcgaLevelLabels = new JLabel[7];
			
			tcgaLevelLabels[0] = new JLabel("Access Root");
			tcgaLevelLabels[1] = new JLabel("Tissue Type");
			tcgaLevelLabels[2] = new JLabel("Center Type");
			tcgaLevelLabels[3] = new JLabel("Center");
			tcgaLevelLabels[4] = new JLabel("Platform");
			tcgaLevelLabels[5] = new JLabel("Data Type");
			tcgaLevelLabels[6] = new JLabel("Archive");
			
			JLabel filesFoundLabel = new JLabel("Files found");

			tcgaLevelBoxes[0] = new JComboBox(new String[]{"Select/","tumor/","other/"});
			tcgaLevelBoxes[1] = new JComboBox();
			tcgaLevelBoxes[2] = new JComboBox();
			tcgaLevelBoxes[3] = new JComboBox();
			tcgaLevelBoxes[4] = new JComboBox();
			tcgaLevelBoxes[5] = new JComboBox();
			tcgaLevelBoxes[6] = new JComboBox();
			
			for (int i=0; i<tcgaLevelBoxes.length; i++){
				tcgaLevelBoxes[i].setEnabled(false);
				tcgaLevelBoxes[i].addActionListener(levelCBListener);
			}
			tcgaLevelBoxes[0].setEnabled(true);                               
			
			dataTypeSFRB = new JRadioButton("TDMS File");
			dataTypeRNARB = new JRadioButton("RNASeq");
			ButtonGroup bg = new ButtonGroup();
			bg.add(dataTypeSFRB);
			bg.add(dataTypeRNARB);
			dataTypeSFRB.setSelected(true);
			
			downloadButton = new JButton("Download from TCGA");
			downloadButton.addActionListener(eventListener);
			downloadButton.setEnabled(false);
			dlm  = new DefaultListModel();
			dlm.addElement("No files found");
			filesList = new JList(dlm);
			selectAllCB = new JCheckBox("Select All");
			selectAllCB.setEnabled(false);
			selectAllCB.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (selectAllCB.isSelected()){
						int[] indices = new int[dlm.size()];
						for (int i=0; i<indices.length; i++){
							indices[i] = i;
						}
						filesList.setSelectedIndices(indices);
						downloadButton.setEnabled(true);
					} else {
						downloadButton.setEnabled(false);
						filesList.clearSelection();
					}
				}				
			});
			filesList.setEnabled(false);
			filesList.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					if (filesList.getSelectedIndices()==null||filesList.getSelectedIndices().length==0){
						downloadButton.setEnabled(false);
					} else {
						downloadButton.setEnabled(true);
					}
					
				}
				//do nothing
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				
			});
			filesList.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JScrollPane fileScroll = new JScrollPane(filesList);

			gba.add(fileSelectionPanel, dataTypeSFRB, 1, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, dataTypeRNARB, 2, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			for (int i=0; i<tcgaLevelBoxes.length; i++){
				gba.add(fileSelectionPanel, tcgaLevelLabels[i], 0, i+1, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);	
				gba.add(fileSelectionPanel, tcgaLevelBoxes[i], 1, i+1, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);				
			}
			gba.add(fileSelectionPanel, filesFoundLabel, 2, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, fileScroll, 2, 1, 1, 6, GridBagConstraints.HORIZONTAL, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);			
			gba.add(fileSelectionPanel, selectAllCB, 2, tcgaLevelBoxes.length, 1, 1, GridBagConstraints.HORIZONTAL, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, downloadButton, 2, tcgaLevelLabels.length+1, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			
			expressionTable = new JTable();
    		myCellRenderer = new ExpressionFileTableCellRenderer();
    		expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
			expressionTable.setIntercellSpacing(new Dimension(1, 1));
			expressionTable.setShowHorizontalLines(false);
			expressionTable.setShowVerticalLines(true);
			expressionTable.setGridColor(Color.LIGHT_GRAY);
			expressionTable.setCellSelectionEnabled(true);
			expressionTable.setColumnSelectionAllowed(false);
			expressionTable.setRowSelectionAllowed(false);
			expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			expressionTable.getTableHeader().setReorderingAllowed(false);

			expressionTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
                    setSelectedCell(expressionTable.rowAtPoint(event.getPoint()), expressionTable.columnAtPoint(event.getPoint()));
				}
			});

            tableScrollPane = new JScrollPane(expressionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

			instructionsLabel = new JLabel();
			instructionsLabel.setForeground(java.awt.Color.red);
			String instructions = "<html>Click the upper-leftmost expression value. Click the <b>Load</b> button to finish.</html>";
			instructionsLabel.setText(instructions);

			tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));

			gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			fileLoaderPanel = new JPanel();
			fileLoaderPanel.setLayout(new GridBagLayout());

			
			gba.add(fileLoaderPanel, fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileLoaderPanel, tablePanel, 0, 3, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

		}
		public String getAnnFilePath() {
			return adh.getAnnFilePath();
		}
        private void setSelectedCell(int xR, int xC) {
			xRow = xR;
			xColumn = xC;
			myCellRenderer.setSelected(xRow, xColumn);
			expressionTable.repaint();
			checkLoadEnable();
		}
        

		public JTable getTable() {
			return expressionTable;
		}

		public int getXColumn() {
			return xColumn;
		}

		public int getXRow() {
			return xRow;
		}

		public void setFileName(String fileName) {
			fileNameTextField.setText(fileName);
			selectedFiles.setText(fileName);
		}

		public void setTableModel(TableModel model) {
			expressionTable.setModel(model);
			int numCols = expressionTable.getColumnCount();
			for (int i = 0; i < numCols; i++) {
				expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
			}
		}

		private class EventListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source == downloadButton) {
			        dlProgress = new Progress(null, "Downloading from TCGA", null);
			        dlProgress.setTitle("Downloading data from TCGA");
			        dlProgress.setIndeterminate(true);
			        dlProgress.setIndeterminantString("Please wait...");
			        dlProgress.setModal(true);
					Runnable runnable = new Runnable() {
						public void run() {
							processTCGAFile(downloadFileURL);	

							if (dataTypeRNARB.isSelected()){
								System.out.println("RNASeq selected");
								superLoader.changeSelectedFileFilterAndLoader(16);
								((RNASeqFileLoader)superLoader.selectedFileLoader).setTCGADataFile(tcgaDownloadFile.getName());
								TMEV.storeProperty("tcga-path", "tcga path to archive");	
							}
						}
					};
					Thread thread = new Thread(runnable);
					thread.start();
			        dlProgress.show();	
				} else if (source == twoColorArray) {
					dataType = IData.DATA_TYPE_RATIO_ONLY;
					setDataType(dataType);
				} else if (source == singleColorArray) {
					dataType = IData.DATA_TYPE_AFFY_ABS;
					setDataType(dataType);
				}
			}
		}
		boolean antiRecursion = false;
		private String downloadFileURL;
		private class LevelCBListener implements ActionListener {

			public void actionPerformed(ActionEvent event) {
				if (antiRecursion)
					return;
				antiRecursion = true;
				String urlBuild = tcgaURLBase+"";
				for (int i=0; i<tcgaLevelBoxes.length; i++){
					if (tcgaLevelBoxes[i].isEnabled()&&tcgaLevelBoxes[i].getSelectedItem()!=null){	
						urlBuild = urlBuild.concat(tcgaLevelBoxes[i].getSelectedItem().toString());
						if (tcgaLevelBoxes[i]==event.getSource())
							break;
					} else {				
						break;
					}
				}
				boolean sourceLevel = true;
				for (int i=0; i<tcgaLevelBoxes.length; i++){
					tcgaLevelBoxes[i].setEnabled(sourceLevel);		
					if (tcgaLevelBoxes[i]==event.getSource()){
						if (i<tcgaLevelBoxes.length-1){
							tcgaLevelBoxes[i+1].setEnabled(true);
							filesList.setEnabled(false);
							selectAllCB.setEnabled(false);
							tcgaLevelBoxes[i+1].removeAllItems();
							setNextCBLevel(i, urlBuild);
								
							i++;
							sourceLevel = false;
						} else {
							fillFilesFound(urlBuild);
							downloadFileURL = urlBuild;						
						}
					}
				}
				antiRecursion = false;
			}
		}
		private void fillFilesFound(String urlBuild) {
			try{	
				filesList.setEnabled(true);
				selectAllCB.setEnabled(true);
				URL url = new URL(urlBuild); 	
				URLConnection url_conn = url.openConnection();
				url_conn.setDoInput(true);
				url_conn.setUseCaches(true);						
				BufferedReader reader = new BufferedReader( new InputStreamReader(url_conn.getInputStream()));
				String currentLine;
				while ((currentLine = reader.readLine()) != null){
					if (currentLine.contains("Parent Directory")){
						break;
					}
				}
				dlm.removeAllElements();
				while ((currentLine = reader.readLine()) != null){
					if (!currentLine.contains("href"))
						break;
					String item = currentLine.substring(currentLine.indexOf(">"), currentLine.length());
					item = item.substring(1, item.indexOf("<"));
					//remove non-expression files
					if (dataTypeRNARB.isSelected()&&!item.contains("exon"))
						continue;
					if (!dataTypeRNARB.isSelected()&&(item.contains("DESCRIPTION.txt")||item.contains("MANIFEST.txt")||item.contains("README_DCC.txt")))
						continue;
					dlm.addElement(item);
				}
				
			}catch (Exception e){
				e.printStackTrace();
			}
			
		}
		private void setNextCBLevel(int i, String urlBuild) {
			try{	
				URL url = new URL(urlBuild); 	
				URLConnection url_conn = url.openConnection();
				url_conn.setDoInput(true);
				url_conn.setUseCaches(true);		
				BufferedReader reader = new BufferedReader( new InputStreamReader(url_conn.getInputStream()));
				String currentLine;
				while ((currentLine = reader.readLine()) != null){
					if (currentLine.contains("Parent Directory")){
						break;
					}
				}
				tcgaLevelBoxes[i+1].addItem("Select");
				while ((currentLine = reader.readLine()) != null){
					if (!currentLine.contains("href"))
						break;
					String item = currentLine.substring(currentLine.indexOf(">"), currentLine.length());
					item = item.substring(1, item.indexOf("<"));
					if(i+1==6){//filter archive for level 2
						if ((item.contains("Level_2")||(item.contains("Level_3")&&dataTypeRNARB.isSelected())) && !item.contains("tar.gz"))
							tcgaLevelBoxes[i+1].addItem(item);
					} else {
						tcgaLevelBoxes[i+1].addItem(item);
					}
				}
				System.out.println("tcgaLevelBoxes[i+1].getItemCount() "+tcgaLevelBoxes[i+1].getItemCount());
				if (tcgaLevelBoxes[i+1].getItemCount()<2){
					tcgaLevelBoxes[i+1].removeAllItems();
					tcgaLevelBoxes[i+1].addItem("No samples found in directory.");
					tcgaLevelBoxes[i+1].setEnabled(false);
				}
				
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args){
		FileWriter fstream;
		try {
			fstream = new FileWriter("C://Users//Dan//Desktop//rnaseqfileGen.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			BufferedReader[] reader = new BufferedReader[22];
			for (int i=0; i<22; i++){
				System.out.println("file number "+(i+1));
				try{	
					URL url = new URL("https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/brca/cgcc/unc.edu/illuminahiseq_rnaseqv2/rnaseqv2/unc.edu_BRCA.IlluminaHiSeq_RNASeqV2.Level_3.1.0.0/unc.edu.0042b857-502f-4d8e-baac-845c1a9bb64b.1154126.exon_quantification.txt"); 	
					URLConnection url_conn = url.openConnection();
					url_conn.setDoInput(true);
					url_conn.setUseCaches(true);
	
					reader[i] = new BufferedReader( new InputStreamReader(url_conn.getInputStream()));
				} catch (Exception e){
					System.out.println("error parsing file: ");
					e.printStackTrace();
				}
			}
			out.write("Tracking ID\t");
			out.write("locus\t");
			out.write("nearest_ref_id\t");
			out.write("class_code\t");
			out.write("transcript_length\t");
			for (int i=0; i<22; i++){
				reader[i].readLine();
				out.write("TCGA RNASeq Sample "+i+"\t"+"TCGA RNASeq Sample"+i+"\t");				
			}
			out.write("\n");
			boolean notPageEnd = true;
			while (notPageEnd ){
				for (int i=0; i<22; i++){
					String currentLine;
					int index = 0;
					currentLine = reader[i].readLine();
					if (currentLine==null){
						notPageEnd = false;
						break;
					}
					String[] curLine = currentLine.split("\t");
					if (i==0){
						out.write("RowID_"+index+"\t");
						out.write(curLine[0]+"\t");
						out.write("\t");
						out.write("\t");
						out.write("\t");
					}
					out.write(curLine[3]+"\t");
					out.write(curLine[1]+"\t");				
					index++;
				}		
				out.write("\n");	
			}		
			out.close();
			
				
			} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void setAnnotationFilePath(String filePath) {
		tcgaDP.adh.setAnnFilePath(filePath);
		tcgaDP.adh.annotationSelected = true;
		
	}
}
