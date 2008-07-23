/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GenePixFileLoader.java,v $
 * $Revision: 1.11 $
 * $Date: 2008-01-04 17:51:56 $
 * $Author: saritanair $
 * $State: Exp $
 */
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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.util.FileLoaderUtility;

public class GenePixFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private GenePixFileLoaderPanel gpflp;    
    private boolean loadEnabled = false;
    
    public GenePixFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        gba = new GBA();
        gpflp = new GenePixFileLoaderPanel();
    }
    
    
    public Vector loadExpressionFiles() throws IOException {
        Object[] genePixFiles = gpflp.getGenePixSelectedListModel().toArray();
        if(genePixFiles == null || genePixFiles.length < 1)
            return null;
        Vector data = new Vector();
        ISlideMetaData meta = null;
        setFilesCount(genePixFiles.length);
        setRemain(genePixFiles.length);
        for (int i = 0; i < genePixFiles.length; i++) {
           // setFileName(((File)genePixFiles[i]).getName());
        	
            if(i == 0){
            	
            	File file=new File(this.gpflp.pathTextField.getText(),((File) genePixFiles[i]).getName());
                data.add( loadSlideData(file) );
                meta = (ISlideMetaData)(data.elementAt(0));
            } else {
            	File file=new File(this.gpflp.pathTextField.getText(),((File) genePixFiles[i]).getName());
                data.add( loadFloatSlideData(file, meta));
            }
            setFilesProgress(i+1);    
            if(i > 0 && i%10 == 0)
                java.lang.Runtime.getRuntime().gc();
        }
        if(data != null && data.size() > 0){
            String [] fieldNames = new String[2];
            fieldNames[0] = "Name";
            fieldNames[1] = "ID";
            //EH fieldnames are not loaded into TMEV anymore
            //TMEV.setFieldNames(fieldNames);
            meta.setFieldNames(fieldNames);
        }
        return data;
    }
    
    
    public ISlideData loadSlideData(File currentFile){
        SlideData slideData = null;
        GenepixFileParser parser = new GenepixFileParser(currentFile, false);
      //  parser.run();
        if(parser.isCompleted()){
            Vector data = parser.getTavFile();
            Vector spotData;
            ISlideDataElement sde;
            int [] rows = new int[3];
            int [] cols = new int[3];
            float [] intensity = new float[2];
            String [] moreFields = new String[2];
            int numElements = data.size();
            
            int maxRows = 0;
            int maxCols = 0;
            int currRow, currCol;
            
            for(int i = 0; i < numElements; i++){
                spotData = (Vector)(data.elementAt(i));
                maxRows = Math.max(maxRows, ((Integer)spotData.elementAt(2)).intValue());
                maxCols = Math.max(maxCols, ((Integer)spotData.elementAt(3)).intValue());
            }
                 
            slideData = new SlideData(maxRows, maxCols);
            this.setLinesCount(numElements);
            for(int i = 0; i < numElements; i++){  //start at 1 to pass header
                rows = new int[3];
                cols = new int[3];
                intensity = new float[2];
                moreFields = new String[2];
                spotData = (Vector)(data.elementAt(i));
                intensity[0] = (float)((Integer)spotData.elementAt(0)).intValue();
                intensity[1] = (float)((Integer)spotData.elementAt(1)).intValue();
                rows[0] = ((Integer)spotData.elementAt(2)).intValue();
                cols[0] = ((Integer)spotData.elementAt(3)).intValue();
                rows[1] = 0;  //no slide row or slide column provided
                cols[1] = 0;
                rows[2] = ((Integer)spotData.elementAt(4)).intValue();
                cols[2] = ((Integer)spotData.elementAt(5)).intValue();
                moreFields[0] = (String)spotData.elementAt(6);
                moreFields[1] = (String)spotData.elementAt(7);
                int flag  =((Integer)spotData.elementAt(8)).intValue();
                sde = new SlideDataElement(String.valueOf(i+1), rows, cols, intensity, moreFields);
                sde.setGenePixFlags(((Integer)spotData.elementAt(8)).intValue());
                slideData.add(sde);
                setFileProgress(i);
            }
            slideData.setSlideDataName(currentFile.getName());
            slideData.setSlideFileName(currentFile.getPath());
        }
        return slideData;
    }
    
    
    public ISlideData loadFloatSlideData(File currentFile, ISlideMetaData meta){
        FloatSlideData slideData = null;
        float cy3, cy5;
        Vector spotData;
        int m=0;
        GenepixFileParser parser = new GenepixFileParser(currentFile, false);
     //   parser.run();
        if(parser.isCompleted()){
            slideData = new FloatSlideData(meta);
            Vector data = parser.getTavFile();
            int numElements = data.size();
            setLinesCount(numElements);
            for(int i = 0; i < numElements; i++){
                spotData = (Vector)(data.elementAt(i));  
                cy3 = (float)((Integer)spotData.elementAt(0)).intValue();
                cy5 = (float)((Integer)spotData.elementAt(1)).intValue();
                spotData.elementAt(2);
                spotData.elementAt(6);
                slideData.setIntensities( i, cy3, cy5);
                spotData.elementAt(7);
                m=((Integer)spotData.elementAt(8)).intValue();
                //System.out.print(m);
                slideData.setGenePixFlags(i,m);
                setFileProgress(i);
            }
            slideData.setSlideDataName(currentFile.getName());
            slideData.setSlideFileName(currentFile.getPath());
        }
        return slideData;
    }
    
    
    
    public ISlideData loadExpressionFile(File f) throws IOException {
        return null;
    }
    
    
    public Vector loadAnnotationFile(File f) throws IOException {
        return new Vector();
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter genePixFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".gpr")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "GenePix Files (*.gpr)";
            }
        };
        
        return genePixFileFilter;
    }
    
    public boolean checkLoadEnable() {
        setLoadEnabled(loadEnabled);
        return this.loadEnabled;
    }
    
    public void markLoadEnabled(boolean state) {
        loadEnabled = state;
        checkLoadEnable();
    }
    
    public JPanel getFileLoaderPanel() {
        return gpflp;
    }
    
    public void processFileList(String filePath, Vector fileNames) {
        
        gpflp.setPath(filePath);
        
        if (fileNames == null) return; // Don't process files if there aren't any
        
        FileFilter genePixFileFilter = getFileFilter();
        
        gpflp.getGenePixAvailableListModel().clear();
        
        for (int i = 0; i < fileNames.size(); i++) {
            
            File targetFile = new File((String) fileNames.elementAt(i));
            
            if (genePixFileFilter.accept(targetFile)) {
                gpflp.getGenePixAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }
    
    public String getFilePath() {
        if(this.gpflp.getGenePixSelectedListModel().getSize() < 1)
            return null;
        return ((File)(gpflp.getGenePixSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
        this.gpflp.openDataPath();
    }

    
/**
 * 
 * Gene Pix File Loader Panel
 * @author Sarita Nair
 *
 */


    private class GenePixFileLoaderPanel extends JPanel {
        
    	JPanel selectionPanel;
    	JPanel fileLoaderPanel;

    	
    	/**
    	 * genePixSelectionPanel contains three panels which are
    	 * 1. selectFilePanel
    	 * 2. genePixListPanel
    	 */
    	JPanel genePixSelectionPanel;
    	
    	/**
    	 * SelectFilePanel is a panel within the genePixSelectionPanel.
    	 * It allows the user to select an expression data file.  
    	 */
    	  JPanel selectFilePanel;
    	  JLabel selectFile;
    	  JButton browseButton1;
    	  JTextField pathTextField;
    	    	
    	
    	/**
    	 * genePixListPanel displays the available genePix files and the user selected 
    	 * Gene pix files for analysis.  User can select/de select  multiple files
    	 * using the Add, AddAll, Remove and RemoveAll buttons. It contains the following components
    	 * 1. genePixAvailbleLabel
    	 * 2. genePixSelectedLabel
    	 * 3. genePixAvailableList
    	 * 4. genePixSelectedList
    	 * 5. genePixAvailableScollPane
    	 * 6. genePixSelectedScrollPane
    	 * 7. genePixButtonPanel
    	 */
    	  JPanel genePixListPanel;
    	  
    	    	
    	 /**
    	  * Label for the list of available gene pix files
    	  */
    	JLabel genePixAvailableLabel;
    	
    	/**
    	 * Label for the list of selected gene pix files
    	 */
    	JLabel genePixSelectedLabel;
    	
    	/**
    	 * List showing the available gene pix format files 
    	 */
    	JList genePixAvailableList;
    	
    	/**
    	 * List showing the selected gene pix files
    	 */
    	JList genePixSelectedList;
    	
    	/**
    	 * Scrollpane containing the gene pixAvailableList
    	 */
    	JScrollPane genePixAvailableScrollPane;
    	
    	/**
    	 * Scrollpane containing the genePixSelectedList
    	 */
    	JScrollPane genePixSelectedScrollPane;
    	
    	
    	/**
    	 * Panel containing the Add, AddAll, Remove and RemoveAll buttons
    	 */
    	JPanel genePixButtonPanel;
    	
    	JButton genePixAddButton;
    	JButton genePixAddAllButton;
    	JButton genePixRemoveButton;
    	JButton genePixRemoveAllButton;
    	


    	


    	public GenePixFileLoaderPanel() {

    		setLayout(new GridBagLayout());
    		
    		
    		selectionPanel = new JPanel();
    		selectionPanel.setLayout(new GridBagLayout());

    		genePixSelectionPanel = new JPanel();
    		genePixSelectionPanel.setLayout(new GridBagLayout());

    		genePixSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(),
    		"File    (GenePix Format Files)"));


    		// selectFilePanel

    		selectFilePanel = new JPanel();
    		selectFilePanel.setLayout(new GridBagLayout());

    		selectFile = new JLabel("Select expression data directory");

    		browseButton1 = new JButton("Browse");
    		browseButton1.setSize(new Dimension(100, 30));
    		browseButton1.setPreferredSize(new Dimension(100, 30));
    		browseButton1.addActionListener(new EventHandler());

    		pathTextField = new JTextField();
    		pathTextField.setEditable(false);
    		pathTextField.setForeground(Color.black);
    		pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
    		
    		gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    	
    		
    		//gene PixListPanel
    		
    		genePixListPanel = new JPanel();
    		genePixListPanel.setLayout(new GridBagLayout());

    		
    		genePixAvailableLabel = new JLabel("Available");
    		genePixSelectedLabel = new JLabel("Selected");
    		genePixAvailableList = new JList(new DefaultListModel());
    		genePixSelectedList = new JList(new DefaultListModel());

    		genePixAvailableScrollPane = new JScrollPane(genePixAvailableList);
    		genePixSelectedScrollPane = new JScrollPane(genePixSelectedList);
    		
    		
    		
    		genePixButtonPanel = new JPanel();
    		genePixButtonPanel.setLayout(new GridBagLayout());
    		    		
    		
    		genePixAddButton = new JButton("Add");
    		genePixAddButton.setPreferredSize(new Dimension(100, 20));

    		genePixAddButton.addActionListener(new EventHandler());
    		genePixAddAllButton = new JButton("Add All");

    		genePixAddAllButton.setPreferredSize(new Dimension(100, 20));

    		genePixAddAllButton.addActionListener(new EventHandler());
    		genePixRemoveButton = new JButton("Remove");

    		genePixRemoveButton.setPreferredSize(new Dimension(100, 20));

    		genePixRemoveButton.addActionListener(new EventHandler());
    		genePixRemoveAllButton = new JButton("Remove All");

    		genePixRemoveAllButton.setPreferredSize(new Dimension(100, 20));

    		genePixRemoveAllButton.addActionListener(new EventHandler());

    		
    		this.genePixAddAllButton.setFocusPainted(false);
    		this.genePixAddButton.setFocusPainted(false);
    		this.genePixRemoveAllButton.setFocusPainted(false);
    		this.genePixRemoveButton.setFocusPainted(false);
    		
    		
    		
    		//Setting layout for affymetrixButtonPanel
    		gba.add(genePixButtonPanel, genePixAddButton, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixButtonPanel, genePixAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixButtonPanel, genePixRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixButtonPanel, genePixRemoveAllButton, 0, 3, 1, 1, 0, 0,
    				GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    	
    		
    
    		//Setting the layout for affymetrixListPanel
    		
    		gba.add(genePixListPanel, genePixAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixListPanel, genePixAvailableScrollPane, 0, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
    				new Insets(0, 0, 0, 0), 0, 0);
    		gba.add(genePixListPanel, genePixButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixListPanel, genePixSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(genePixListPanel, genePixSelectedScrollPane, 2, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(genePixSelectionPanel, selectFilePanel, 0, 0, 1, 1, 1, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		
    		gba.add(genePixSelectionPanel, genePixListPanel, 0, 2, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);

   
    		
    		gba.add(selectionPanel, genePixSelectionPanel, 0, 1, 2, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		
    		
    		
    		fileLoaderPanel = new JPanel();
    		fileLoaderPanel.setLayout(new GridBagLayout());

    		gba.add(fileLoaderPanel, selectionPanel, 0, 0, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    	
    		    		
    		gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.H, GBA.N,
    				new Insets(5, 5, 5, 5), 0, 0);




        }
    	
    	 
    	
    	
        public void setPath(String path) {
            pathTextField.setText(path);
        }
        
        public void openDataPath(){
        //   this.fileTreePane.openDataPath();
        }
        
        
      
        
        public void onDataFileBrowse() {
        	
        	FileLoaderUtility fileLoad = new FileLoaderUtility();
        	Vector retrievedFileNames = new Vector();
        	JFileChooser fileChooser = new JFileChooser(
        			SuperExpressionFileLoader.DATA_PATH);
        	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	int retVal = fileChooser.showOpenDialog(GenePixFileLoaderPanel.this);

        	if (retVal == JFileChooser.APPROVE_OPTION) {
        		((DefaultListModel) genePixAvailableList.getModel()).clear();
        		((DefaultListModel) genePixSelectedList.getModel()).clear();

        		File selectedFile = fileChooser.getSelectedFile();
        		String path = selectedFile.getAbsolutePath();
        		pathTextField.setText(path);
        		retrievedFileNames = fileLoad.getFileNameList(selectedFile
        				.getAbsolutePath());

        		for (int i = 0; i < retrievedFileNames.size(); i++) {
        			Object fileName = retrievedFileNames.get(i);
        			boolean acceptFile = getFileFilter()
        			.accept((File) fileName);

        			if (acceptFile) {
        				
        				String Name=fileChooser.getName((File) fileName);
        				((DefaultListModel) genePixAvailableList.getModel())
						.addElement(new File(Name));
        				
        				
        			/*	Object addItem = fileName;
        				((DefaultListModel) genePixAvailableList.getModel())
        				.addElement(addItem);*/
        			}
        		}

        	}


        }
        
        
       
        
        public void validateLists() {

        	// Currently, a minimum of one gene Pix file must be selected to enable absoluteRadiong.
        	// If the reference option is selected, a minimum of one gene pix file must also
        	// be chosen as a reference.

        	if (((DefaultListModel) genePixSelectedList.getModel()).size() > 0) {
        			markLoadEnabled(true);
        		
        	} else {
        		markLoadEnabled(false);
        	}
        }
        
        public void onGenePixAdd() {
        	int[] chosenIndices = genePixAvailableList.getSelectedIndices();
        	Object[] chosenObjects = new Object[chosenIndices.length];

        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		// For remove-then-add functionality
        		//Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).remove(chosenIndices[i]);
        		// For copy-then-add functionality
        		Object addItem = ((DefaultListModel) genePixAvailableList.getModel()).getElementAt(chosenIndices[i]);
        		chosenObjects[i] = addItem;
        	}

        	for (int i = 0; i < chosenIndices.length; i++) {
        		((DefaultListModel) genePixSelectedList.getModel()).addElement(chosenObjects[i]);
        	}

        	validateLists();
        }

        public void onGenePixAddAll() {
        	int elementCount = ((DefaultListModel) genePixAvailableList.getModel()).size();
        	for (int i = 0; i < elementCount; i++) {
        		Object addItem = ((DefaultListModel) genePixAvailableList.getModel()).getElementAt(i);
        		((DefaultListModel) genePixSelectedList.getModel()).addElement(addItem);
        	}

        	validateLists();
        }

        public void onGenePixRemove() {
        	int[] chosenIndices = genePixSelectedList.getSelectedIndices();

        	// Designed with copy-then-add functionality in mind
        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		((DefaultListModel) genePixSelectedList.getModel()).remove(chosenIndices[i]);
        	}

        	validateLists();
        }

        public void onGenePixRemoveAll() {
        	// Designed with copy-then-add functionality in mind
        	((DefaultListModel) genePixSelectedList.getModel()).removeAllElements();

        	validateLists();
        }

       
       
       
        public DefaultListModel getGenePixAvailableListModel() {
        	return (DefaultListModel) genePixAvailableList.getModel();
        }

      

        public DefaultListModel getGenePixSelectedListModel() {
        	return (DefaultListModel) genePixSelectedList.getModel();
        }

      
        private class EventHandler implements ActionListener {
        	public void actionPerformed(ActionEvent event) {

        		Object source = event.getSource();

        		if (source == genePixAddButton) {
        			onGenePixAdd();
        		} else if (source == genePixAddAllButton) {
        			onGenePixAddAll();
        		} else if (source == genePixRemoveButton) {
        			onGenePixRemove();
        		} else if (source == genePixRemoveAllButton) {
        			onGenePixRemoveAll();
        		} else if (source==browseButton1){
        			onDataFileBrowse();  
        		}
  


        	}
        }

    
    }


@Override
public String getAnnotationFilePath() {
	
	
	return null;
}  
  
}
