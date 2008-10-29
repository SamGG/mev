package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.GeneAnnotationImportDialog;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.file.GBA;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.microarray.mev.Manager;
import org.tigr.microarray.util.FileLoaderUtility;



	/**
	 * 
	 * 
	 * @param args
	 */
	
	public class GSEADataPanel extends JPanel implements IWizardParameterPanel {
		
		//Group Assignment panel
		private javax.swing.JPanel groupAssignmentPanel;
		private javax.swing.JLabel factorLabel;
		private javax.swing.JButton groupAssignment;
		private javax.swing.JLabel emptyLabel;
		private javax.swing.JLabel assignmentSuccessfulLabel;
			
		//Annotation panel
		private javax.swing.JPanel annotationPanel;
		private javax.swing.JTextField annotationTextField;
		private javax.swing.JLabel annotationLabel;
		private javax.swing.JLabel uploadAnnotationLabel;
		private javax.swing.JButton connectButton;
	  	private javax.swing.JButton annotationButton;
	  	private javax.swing.JLabel infoLabel;
	  
	  	//Gene set panel
	    private javax.swing.JPanel genesetPanel;
	  	private javax.swing.JButton browseButton2;
	    private javax.swing.JLabel uploadGeneSetLabel;
	    private javax.swing.JTextField geneSetTextField;
	    
	    private AlgorithmData algData;
	    private IData idata;
	    private File selectedFile;
	   // private MultipleArrayViewer viewer;
	    private int currIndex;
	    
	    protected Vector exptNamesVector;
	    protected String[] factorNames;
	    protected int[] numFactorLevels;
	    protected int[] factorAAssignments, factorBAssignments, factorCAssignments;
	    protected JFrame parentFrame;
	    protected ClusterRepository clusterRepository;
	    
	    
	    
	    /** Creates new form DataPanel 
	     * IData would be required here, in order to initialize the
	     * GUI. To check if annotations are loaded or not we need a reference to IData.
	     * @param clusterRepository TODO
	     * 
	     * 
	     * */
	//    public GSEADataPanel(IData idata,AlgorithmData algData, JDialog parent) {//commented for testing
	    public GSEADataPanel(IData idata,AlgorithmData algData, JFrame parent, ClusterRepository clusterRepository) {
	    	
		    this.parentFrame = parent;
	    	this.idata=idata;
			this.algData = algData;
			this.clusterRepository=clusterRepository;
			
	        initComponents();
	        initialize("./data/Annotation/"+this.idata.getChipAnnotation().getChipType()+".txt",idata.isAnnotationLoaded(),
	        		"<html> You have already loaded annotations, so please continue with selecting the rest of the parameters</html>");
	    }
	    
	    /** This method is called from within the constructor to
	     * initialize the form.
	     * 
	     */
	    
	    private void initComponents() {
	    	GBA gba = new GBA();
	    	setLayout(new GridBagLayout());
	    	
	    	JPanel fileLoaderPanel;
	    	
	    	// Group Assignment panel
	    	groupAssignmentPanel = new javax.swing.JPanel();
	    	factorLabel = new javax.swing.JLabel();
	    	emptyLabel=new javax.swing.JLabel();
	    	assignmentSuccessfulLabel=new javax.swing.JLabel();
	    	
	    	
	    	groupAssignment = new javax.swing.JButton();
	       	groupAssignment.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			groupAssignment.setPreferredSize(new Dimension(90, 30));
	    	groupAssignment.addActionListener(new Listener());
	   
	    	//Annotation panel
	       	annotationPanel = new javax.swing.JPanel();
	       	annotationLabel = new javax.swing.JLabel();
	    	infoLabel=new javax.swing.JLabel();
	       	
	       	
	    	connectButton = new javax.swing.JButton();
	       	connectButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			connectButton.setPreferredSize(new Dimension(90, 30));
			connectButton.addActionListener(new Listener());
			
	    	uploadAnnotationLabel = new javax.swing.JLabel();
	    	annotationTextField = new javax.swing.JTextField();
	    	annotationTextField.setEditable(false);
	    	annotationButton = new javax.swing.JButton();
	       	annotationButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			annotationButton.setPreferredSize(new Dimension(90, 30));
			annotationButton.addActionListener(new Listener());
	    	
	    	
	    	

	    	//Gene set panel
	    	genesetPanel = new javax.swing.JPanel();
	       	geneSetTextField = new javax.swing.JTextField();
	    	geneSetTextField.setEditable(false);
	    	uploadGeneSetLabel = new javax.swing.JLabel();
	    	
	    	browseButton2 = new javax.swing.JButton();
	       	browseButton2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			browseButton2.setPreferredSize(new Dimension(90, 30));
			browseButton2.addActionListener(new Listener());

	    	//Group assignment panel
	    	groupAssignmentPanel.setLayout(new GridBagLayout());
	    	groupAssignmentPanel.setBorder(new TitledBorder(new EtchedBorder(), "Phenotype Assignment"));
	    	
   	    	factorLabel.setText("Assign phenotype labels to your samples");
   	    	groupAssignment.setText("Assign");
   	    
   	      	       	
   	        
    		gba.add(groupAssignmentPanel, factorLabel, 0, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5,5,5,5),0,0);
    		gba.add(groupAssignmentPanel, emptyLabel, 1, 0, 0, 0, 1, 0, GBA.H,	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(groupAssignmentPanel, groupAssignment, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
    		gba.add(groupAssignmentPanel, assignmentSuccessfulLabel, 0, 1, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    	
   	    		    	
	    	//Annotation panel layout
			annotationPanel.setLayout(new GridBagLayout());
	    	annotationPanel.setBorder(new TitledBorder(new EtchedBorder(), "Annotation"));
	    	annotationLabel.setText("Retrieve annotation from Resourcerer"); 
	    
	    	
	    	connectButton.setText("Connect"); 
	    	uploadAnnotationLabel.setText("OR Upload Annotation");
	    
	    	
	    	annotationButton.setText("Browse"); 
	    	
	    	gba.add(annotationPanel, infoLabel, 0, 0, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    	gba.add(annotationPanel, annotationLabel, 0, 1, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, connectButton, 1, 1, GBA.RELATIVE, 1, 0, 0,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    	    		
    		gba.add(annotationPanel, uploadAnnotationLabel, 0, 3, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5,5,5,5),0,0);
    		gba.add(annotationPanel, annotationTextField, 1, 3, 1, 0, 1, 0, GBA.H,	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, annotationButton, 2, 3, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
	    	
	    	
	     	
	    	

	       //Gene set panel layout
    		genesetPanel.setLayout(new GridBagLayout());
	    	genesetPanel.setBorder(new TitledBorder(new EtchedBorder(), "Geneset"));
	       	uploadGeneSetLabel.setText("Upload Geneset"); 
	       	browseButton2.setText("Browse"); 
	       	
	       	gba.add(genesetPanel, uploadGeneSetLabel, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(genesetPanel, geneSetTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(genesetPanel, browseButton2, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			
			
			fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
            fileLoaderPanel.setPreferredSize(new Dimension(650, 500));
            
			
			gba.add(fileLoaderPanel,groupAssignmentPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            gba.add(fileLoaderPanel, genesetPanel,0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            gba.add(fileLoaderPanel, annotationPanel, 0, 3, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
          
    	
            
	    	
	    }
	    
	/**
	 * Initializes data panel based on params. If annotation has already been loaded,
	 * disable the browse and connect button, also show the path of the data and 
	 * annotation files.
	 * 
	 * 
	 * @param annPath
	 * @param isAnnLoaded
	 */
	    
	public void initialize( String annPath, boolean isAnnLoaded, String info) {
		
	
		if(isAnnLoaded) {
			this.annotationTextField.setText(annPath);
			this.annotationButton.setEnabled(false);
			this.connectButton.setEnabled(false);
			this.infoLabel.setText("You have already loaded annotations, so please continue with the remaining parameter selection..");
			this.infoLabel.setForeground(Color.RED);
		}
		
	}
	    
	

	
	public void clearValuesFromAlgorithmData() {
		algData.getParams().getMap().remove("annotation-file");
		algData.getParams().getMap().remove("gene-set-file");
		
		
	}

	
	public void onDisplayed() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public void populateAlgorithmData() {
		
		if(annotationTextField.getText()!=null) {
		algData.addParam("annotation-file", annotationTextField.getText());
		}
		if(geneSetTextField.getText()!=null) {
		algData.addParam("gene-set-file",geneSetTextField.getText() );
		}
		
		
	}
	
	
	public void onButtonClick(Object source){

		FileLoaderUtility fileLoad = new FileLoaderUtility();
		File selectedFile;
		JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.DATA_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(this);
		selectedFile = fileChooser.getSelectedFile();

		if(retVal==JFileChooser.APPROVE_OPTION){
			if(source==annotationButton){
				
				if(retVal==JOptionPane.OK_OPTION){
					annotationTextField.setText(selectedFile.getAbsolutePath());
					setAnnotationFile(selectedFile);
					processAnnotationFile();
				}
			}else if(source==browseButton2){
				geneSetTextField.setText(selectedFile.getAbsolutePath());
			}
		}

	}
	
	
	
	public void getGroupAssignment(){

		Experiment experiment=this.idata.getExperiment();
		int number_of_samples=experiment.getNumberOfSamples();
		
	//	GSEAInitBox1 gBox1 = new GSEAInitBox1(parentFrame, null,true);
		GSEAInitBox1 gBox1 = new GSEAInitBox1(new JFrame(), null,true);
		GSEAInitBox2 gBox2=null;
		GSEAInitBox3 gBox3=null;
		gBox1.setVisible(true);
		int[][]assignments=null;

		//if(gBox2.isOkPressed()){
			
		//}
		
		
		if(gBox1.isOkPressed()){
			gBox2 = new GSEAInitBox2(new JFrame(), true,Integer.parseInt(gBox1.getNumberofFactors()));
			gBox2.setVisible(true);

			if(Integer.parseInt(gBox1.getNumberofFactors())== 3){
				factorNames=new String[3];

				factorNames[0]=gBox2.getFactorAName();
				factorNames[1]=gBox2.getFactorBName();
				factorNames[2]=gBox2.getFactorCName();


			}else if(Integer.parseInt(gBox1.getNumberofFactors())== 2){
				factorNames=new String[2];

				factorNames[0]=gBox2.getFactorAName();
				factorNames[1]=gBox2.getFactorBName();


			}else{
				factorNames=new String[1];
				factorNames[0]=gBox2.getFactorAName();

			}

			if(gBox2.isOkPressed()){
				if(Integer.parseInt(gBox1.getNumberofFactors())== 3){
					numFactorLevels=new int[3];
					numFactorLevels[0]=gBox2.getNumFactorALevels();
					numFactorLevels[1]=gBox2.getNumFactorBLevels();
					numFactorLevels[2]=gBox2.getNumFactorCLevels();
				}else if(Integer.parseInt(gBox1.getNumberofFactors())== 2){
					numFactorLevels=new int[2];
					numFactorLevels[0]=gBox2.getNumFactorALevels();
					numFactorLevels[1]=gBox2.getNumFactorBLevels();
				}else{
					numFactorLevels=new int[1];
					numFactorLevels[0]=gBox2.getNumFactorALevels();
				}
			}
		}

		exptNamesVector=new Vector();
		for (int i = 0; i < number_of_samples; i++) {
			//exptNamesVector.add(idata.getFullSampleName(experiment.getSampleIndex(i)));
			exptNamesVector.add(idata.getSampleName(i));
		}




		if(gBox2.isOkPressed()){

			gBox3=new GSEAInitBox3(parentFrame, true,Integer.parseInt(gBox1.getNumberofFactors()), exptNamesVector, factorNames, numFactorLevels,this.clusterRepository );
			gBox3.setVisible(true);
			assignments=new int[factorNames.length][];
			if(gBox3.isOkPressed()){
				if(Integer.parseInt(gBox1.getNumberofFactors())== 3){
					assignments[0]=gBox3.getFactorAAssignments();
					assignments[1]=gBox3.getFactorBAssignments();
					assignments[2]=gBox3.getFactorCAssignments();
				}else if (Integer.parseInt(gBox1.getNumberofFactors())== 2){
					assignments[0]=gBox3.getFactorAAssignments();
					assignments[1]=gBox3.getFactorBAssignments(); 
				}else
					assignments[0]=gBox3.getFactorAAssignments();
				this.assignmentSuccessfulLabel.setText("You have successfully assigned phenotypes.");
				this.assignmentSuccessfulLabel.setForeground(Color.RED);
				this.assignmentSuccessfulLabel.setHorizontalAlignment(SwingConstants.CENTER);
			}
		}
       	
		this.algData.addStringArray("factor-names", factorNames);
		this.algData.addIntArray("factor-levels", numFactorLevels);
		this.algData.addIntMatrix("factor-assignments", assignments);
		
	
	}


	
	
	
	
	 public void onConnect() {
     	if(this.idata.getDataType()==IData.DATA_TYPE_AFFY_ABS) {
     		AnnotationDialog annDialog = new AnnotationDialog(new JFrame());
    		if (annDialog.showModal() == JOptionPane.OK_OPTION) {
    			File selectedFile = new File(annDialog.getAnnotationFileName());
    			setAnnotationFile(selectedFile);
    			annotationTextField.setText(annDialog.getAnnotationFileName());
    			processAnnotationFile();

    		}

     	}
     	}
     
	 private void setAnnotationFile(File selectedFile) {
		this.selectedFile=selectedFile;
		
	}

	/**
		 * processAnnotationFile() function 
		 * 1. Reads the selected annotation file
		 * 2. Calls "GeneAnnotationImportDialog" to correctly map the
		 * unique identifier in the annotation file (probe id) to the unique identifier 
		 * in the expression data loaded.
		 * 3. Calls "addResourcererGeneAnnotation", which makes the necessary changes in SlideDataElement
		 * 
		 */
		public void processAnnotationFile() {
			try {
				
			//	System.out.println("processAnnotationFile");
				String[] dataFieldNames = this.idata.getFieldNames();
				//System.out.println("Annotation file path:"+getAnnotationFile().getAbsolutePath());
				//System.out.println("dataFieldName:"+dataFieldNames[0]);
				
				File annotationFile=new File(getAnnotationFile().getAbsolutePath());
				AnnotationFileReader reader=AnnotationFileReader.createAnnotationFileReader(annotationFile);
				Hashtable annoHash=reader.getAffyAnnotation();
			//	System.out.println("size of annohash:"+annoHash.size());
				GeneAnnotationImportDialog importDialog = new GeneAnnotationImportDialog(
					new JFrame(), dataFieldNames, MevAnnotation.getFieldNames());

				if (importDialog.showModal() == JOptionPane.OK_OPTION) {

					String[] newFields = MevAnnotation.getFieldNames();
					((MultipleArrayData)this.idata).addResourcererGeneAnnotation(importDialog
							.getDataAnnotationKey(), reader.getAffyAnnotation());

				}

			} catch (Exception e) {

			}
			this.idata.setAnnotationLoaded(true);
		}


	
	
	private File getAnnotationFile() {
		return this.selectedFile;
	}




	private class Listener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			Object source = event.getSource();
			if (source == annotationButton) { 
				onButtonClick(source);
			} else if (source == browseButton2) {
				onButtonClick(source);
			} else if(source == groupAssignment){
				getGroupAssignment();
				
			} else if (source==connectButton){
    			onConnect(); 
				
    		}
			
		}
	}

	
	
	
	
	
/*	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		GSEADataPanel p = new GSEADataPanel();	
		//p.setVisible(true);
		frame.getContentPane().add(p);
		frame.setSize(600,600);
		frame.setVisible(true);	

	}*/

}
