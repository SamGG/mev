/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* WekaBNGui.java
 * Copyright (C) 2006 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import weka.classifiers.bayes.BayesNet;import weka.classifiers.Evaluation;
import cytoscape.CyMain;
import org.tigr.microarray.mev.cluster.gui.impl.bn.RunWekaProgressPanel;
//import cytoscape.cytoscape;
import org.tigr.microarray.mev.cluster.gui.impl.bn.FromWekaToSif;
public class WekaBNGui extends JPanel implements ActionListener {
    JButton inArffExprButton;
    JButton runWekaButton;
    JCheckBox reverseCb;
    JTextField numParentsTf;
    JLabel algoLabel;
    JLabel scoreLabel;
    JPanel numParentsTfPanel;
    JLabel numParentsTfLabel;
    JFileChooser fc;
    JCheckBox initBifCheckBox;
    String initBifFileName = null;
    String useArcReversal = "true";
    String numParents = "3";
    public static String arffExpressionFileName = null;
    JButton showInCytoButton; 
    JScrollPane evalScrollPane;
    JTextArea textArea;
    String evalStr = null;
    RunWekaProgressPanel runProgressPanel;
   // public static boolean runWekaDone=false; 
    public JPanel createComponents() {
	fc = new JFileChooser(new File(System.getProperty("user.dir")));
	algoLabel = new JLabel("Search algorithm: HillClimbing");
	reverseCb = new JCheckBox ("UseArcReversal", true);
	numParentsTfLabel = new JLabel("maxNrOfParents");
	numParentsTf = new JTextField("3");
	numParentsTfPanel = new JPanel(new GridLayout(0,2));
	numParentsTfPanel.add(numParentsTfLabel);
	numParentsTfPanel.add(numParentsTf);
	scoreLabel = new JLabel("Score: BDeu");
	//initBifFileButton = new JButton("Open initial XML Bif File...",
         //                        createImageIcon("images/Open16.gif"));
        
	initBifCheckBox=new JCheckBox("Apply initial XML Bif File",true);
	//initBifCheckBox.addActionListener(this);
	runWekaButton = new JButton("Run Bayesian Network");
	runWekaButton.addActionListener(this);
	JPanel panel = new JPanel(new GridLayout(6, 1));       
	panel.add(algoLabel);
	panel.add(reverseCb);
	panel.add(numParentsTfPanel);
	panel.add(scoreLabel);
	panel.add(initBifCheckBox);
	panel.add(runWekaButton);
	panel.setBorder(BorderFactory.createEmptyBorder(
                                        30, //top
                                        30, //left
                                        10, //bottom
                                        30) //right
                                        );
	return panel;
    }
    public void actionPerformed(ActionEvent e) {
	 /*
	    if (e.getSource() == initBifCheckBox) {
            int returnVal = fc.showOpenDialog(WekaBNGui.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
		//initBifFileName = file.getAbsolutePath();
		 initBifFileName = file.getName();
		System.out.println("initBifFileName=" + initBifFileName);
            } 
	    else {
                System.out.println("Open command cancelled by user");
            }
	 }
	 else 
	 */
	 if(e.getSource() == reverseCb){
	     useArcReversal = Boolean.toString(reverseCb.isSelected());
	 }
	 else if(e.getSource() == numParentsTf){
	     numParents = numParentsTf.getText();	     
	     System.out.println("numParents="+numParents);
	 }
	 else if(e.getSource() == runWekaButton) {
	     System.out.println("Run weka");	     
	    /*
	     if(!(new File(arffExpressionFileName)).exists()){
		 System.out.println("ARFF expression file name is null!");
		 JOptionPane.showMessageDialog(null,
					       "ARFF expression file name is null!",
					       "Error",
					       JOptionPane.ERROR_MESSAGE);
	     }
	     */
	     // Command to Weka should be like:
	     // java weka.classifiers.bayes.BayesNet -t bla_arff.arff -c 1 -D -Q weka.classifiers.bayes.net.search.local.HillClimber -- -R -N -P 3 -S BAYES -X result_list_bif.xml -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5
	   //  try {
	     //arffExpressionFileName=arffExpressionFileName.replaceAll(" ","_");
	     String arguments = "-t "+ arffExpressionFileName+ " -c 1";
	     arguments += " -Q weka.classifiers.bayes.net.search.local.HillClimber -- ";	     
	     if(useArcReversal.equals("true")){
		 arguments += "-R";
	     }	     
	     arguments += " -P "+numParents+" -S BAYES";
	     if(initBifCheckBox.isSelected() ){
		     while(!BNGUI.done){
		     try{
		        Thread.sleep(5000);	
		     }catch(InterruptedException x){
		     //ignore;
		     }
		     }
		 arguments += " -X resultBif.xml";
	     }		 
	     arguments += " -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5";
	     System.out.println("calling weka with arguments: \n"+arguments);
	     final String[] argsWeka = arguments.split(" ");
	     Thread thread = new Thread( new Runnable(){
		       public void run(){
			       try{     
	                           runProgressPanel=new RunWekaProgressPanel();
	                           runProgressPanel.setIndeterminate(true);
	                           runProgressPanel.setVisible(true);
				   evalStr = Evaluation.evaluateModel(new BayesNet(),argsWeka);
			       }catch(Exception ex){
			       ex.printStackTrace();
			       }
			 runProgressPanel.dispose();
	                 displayScrollPane(getScrollPanePanel(evalStr));
			 
			   }
	           });
	      thread.start();
	     
        }  
	 else if(e.getSource() == showInCytoButton){
	     try {
		 /*
		 if(evalStr == null){
		     System.out.println("Evaluation String from Weka is null!");
		     JOptionPane.showMessageDialog(null,
						   "Evaluation String from Weka is null!",
						   "Error",
						   JOptionPane.ERROR_MESSAGE);
		 }
		 else {
                 */		 //FileOutputStream fos = new FileOutputStream("result.sif"); //Raktim - Old Way
		 //String fileName = System.getProperty("user.dir")+"/data/bn/results/"+Useful.getUniqueFileID()+ "_" + "result.sif";
	     //Added Algorithim Name & Score Type to File Name
	     String fileName = System.getProperty("user.dir")+"/data/bn/results/"+Useful.getUniqueFileID()+ "_" + "result.sif";
		 FileOutputStream fos = new FileOutputStream(fileName);
		 PrintWriter pw = new PrintWriter(fos, true);		 //FromWekaToSif.fromWekaToSif(evalStr, pw);		     
		 FromWekaToSif.fromWekaToSif(evalStr, pw, false);
		     // call cytoscape here
		 final String[] argv = new String[4];
		 argv[0] = "-i";		 //argv[1] = "result.sif"; //Raktim - Old Way
		 argv[1] = fileName;
		 argv[2] = "-p";
		 argv[3] = System.getProperty("user.dir")+"/plugins/core/yLayouts.jar";		 Thread thread = new Thread( new Runnable(){
			 public void run(){
			    try{			    	cytoscape.CyMain.main(argv);
			    }catch(Exception ex){
			    ex.printStackTrace();
		    }
			       }
	              });
	             thread.start();
	 
	     }catch(IOException ioE){
		ioE.printStackTrace();     
	     }catch(Exception ex){
		 //System.out.println(ex);
		 ex.printStackTrace();
		 JOptionPane.showMessageDialog(null,
					       ex.toString(),
					       "Error",
					       JOptionPane.ERROR_MESSAGE);
	     }
    
	 }
	 cleanUpFile();
    }
    
    
    private void cleanUpFile(){
	    String[] files=new String[8];
	    files[0]="getInterModLit.props";
	    files[1]="getInterModBoth.props";
	    files[2]="getInterModPPIDirectly.props";
	    files[3]="outInteractionsLit.txt";
	    files[4]="outInteractionsPPI.txt";
	    files[5]="outInteractionsBoth.txt";
            files[6]="weka_transposed.csv";
	    files[7]="prepareXMLBifMod.props";
	    
	    String sep=System.getProperty("file.separator");
	    String path=System.getProperty("user.dir");
	    path=path+sep+"data"+sep+"bn"+sep;
	    for(int i=0;i<8;i++){
	     File file=new File(path,files[i]);
              file.deleteOnExit();	    
	    }
	    
    }
    public void displayScrollPane(JPanel panel){
	JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        JFrame frame = new JFrame("Results from Weka");
       // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().add(panel);
	frame.pack();
	frame.setVisible(true);
    }

    public JPanel getScrollPanePanel(String evalString){
	JPanel evalPanel = new JPanel();
	evalPanel.setLayout(new BorderLayout());
	textArea = new JTextArea(evalString, 30, 50);
	evalScrollPane = new JScrollPane(textArea);
	evalScrollPane.setPreferredSize(textArea.getPreferredScrollableViewportSize());	
	evalScrollPane.revalidate(); // knows when to resize
	evalPanel.add(evalScrollPane, BorderLayout.NORTH);
	showInCytoButton = new JButton("Show network in Cytoscape");
	showInCytoButton.addActionListener(this);
	evalScrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, showInCytoButton);
	evalPanel.add(showInCytoButton, BorderLayout.SOUTH);
	return evalPanel;
    }
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = WekaBNGui.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void createAndShowGUI(String inArffExpressionFileName) {    	arffExpressionFileName = inArffExpressionFileName;
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Prepare Runing Bayesian Network");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        WekaBNGui app = new WekaBNGui();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
