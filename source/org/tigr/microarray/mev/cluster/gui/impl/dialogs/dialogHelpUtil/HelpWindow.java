/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * HelpWindow.java
 *
 * Created on March 5, 2003, 10:36 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import edu.stanford.ejalbert.BrowserLauncher;

public class HelpWindow extends JDialog {
    
    String dialogName;
    JEditorPane pane;
    GradientPaint gp;
    Color backgroundColor = new Color(25,25,169);
    Color fadeColor = new Color(140,220,240);
    
    /** Creates new HelpWindow */
    public HelpWindow(JDialog parent, String dialogName) {
        super(parent);
        this.dialogName = dialogName;
        int lastIndex = dialogName.indexOf(" ");
        if(lastIndex > 0 && lastIndex <= 4){
            String abbr = dialogName.substring(0, lastIndex );
            if(abbr != null && abbr.length() > 0)
                this.setTitle(abbr+": Parameter Information");
        }
        else
            this.setTitle(this.dialogName+":Parameter Information");
        
        JPanel iconPanel = new JPanel(new GridBagLayout());
        JLabel iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
        FillPanel fill = new FillPanel();
        iconPanel.add(iconLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        iconPanel.add(fill, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
                
        pane = new JEditorPane();
        pane.setEditable(false);
        pane.setForeground(Color.black);
        pane.setMinimumSize(new Dimension(100,100));
        getContentPane().setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane();
        scroll.getViewport().setView(pane);
        scroll.setPreferredSize(new Dimension(200,200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
                
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        JButton closeButton = new JButton(" Close Help Window ");
        closeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        closeButton.requestFocus();
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(180, 30));
        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dispose();
            }
        });
        
        buttonPanel.add(new JPanel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        buttonPanel.add(closeButton, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(5,5,5,15),0,0));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        
        mainPanel.add(iconPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(scroll, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        mainPanel.setPreferredSize(new Dimension(600,600));
        this.getContentPane().add(mainPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        validate();
        pane.setContentType("text/html");
        setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setMargin(new Insets(10,15,10,15));
        
    }
    

    /** Creates new HelpWindow */
    public HelpWindow(JFrame parent, String dialogName) {
        super(parent);
        this.dialogName = dialogName;
        int lastIndex = dialogName.indexOf(" ");
        if(lastIndex > 0 && lastIndex <= 4){
            String abbr = dialogName.substring(0, lastIndex );
            if(abbr != null && abbr.length() > 0)
                this.setTitle(abbr+": Parameter Information");
        }
        else
            this.setTitle(this.dialogName+": Parameter Information");
        if (dialogName=="Java Out of Memory Error")
        	this.setTitle(this.dialogName+": Information");
        JPanel iconPanel = new JPanel(new GridBagLayout());
        JLabel iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
        FillPanel fill = new FillPanel();
        iconPanel.add(iconLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        iconPanel.add(fill, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        
        pane = new JEditorPane();
        pane.setEditable(false);
        pane.setForeground(Color.black);
        pane.setMinimumSize(new Dimension(100,100));
        getContentPane().setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane();
        scroll.getViewport().setView(pane);
        scroll.setPreferredSize(new Dimension(200,200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        JButton closeButton = new JButton(" Close Help Window ");
        closeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        closeButton.requestFocus();
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(180, 30));
        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dispose();
            }
        });
        
        buttonPanel.add(new JPanel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        buttonPanel.add(closeButton, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(5,5,5,15),0,0));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        
        mainPanel.add(iconPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(scroll, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        mainPanel.setPreferredSize(new Dimension(600,600));
        this.getContentPane().add(mainPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        validate();
        pane.setContentType("text/html");
        setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setMargin(new Insets(10,15,10,15));
        
    }

    public HelpWindow(JFrame parent, String dialogName, boolean isParmDialog) { 
        super(parent);
        this.dialogName = dialogName;
        int lastIndex = dialogName.indexOf(" ");
        
        this.setTitle(dialogName);
        
        JPanel iconPanel = new JPanel(new GridBagLayout());
        JLabel iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
        FillPanel fill = new FillPanel();
        iconPanel.add(iconLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        iconPanel.add(fill, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
                
        pane = new JEditorPane();
        pane.setEditable(false);
        pane.setForeground(Color.black);
        pane.setMinimumSize(new Dimension(100,100));

        getContentPane().setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane();
        scroll.getViewport().setView(pane);
        scroll.setPreferredSize(new Dimension(200,200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        JButton closeButton = new JButton(" Close Help Window ");
        closeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        closeButton.requestFocus();
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(180, 30));
        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dispose();
            }
        });
        
        buttonPanel.add(new JPanel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        buttonPanel.add(closeButton, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(5,5,5,15),0,0));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        
        mainPanel.add(iconPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(scroll, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        
        this.getContentPane().add(mainPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        validate();
        pane.setContentType("text/html");
        setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        pane.setMargin(new Insets(10,15,10,15));
    }
    

    
    public void setLocation(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
    }
    
    public static boolean launchBrowser(Window w, String dialogName) {
        String fileName = getFileName(dialogName);
        if(fileName == null){
             JOptionPane.showMessageDialog(w, dialogName+" help page cannot be located.");
            return false;
        }
        try{
         	File doclocation = new File("./documentation/manual/" + fileName);
            BrowserLauncher.openURL(doclocation.toString());
         } catch (Throwable t) {
         	t.printStackTrace();
         }
            return true;
    
    }
    private static String getFileName(String key){

        if(key.equals("File Loader"))						//
            return "Loading the Data.html";
    	if(key.equals("TDMS"))								//
            return "Loading the Data.html";
    	if(key.equals("GenericFileDialog"))					//
            return "Loading-mev-data.html";
    	if(key.equals("Mev"))
            return "Loading-mev-data.html";		//
    	if(key.equals("Tav"))
            return "Loading-mev-data.html";			//
    	if(key.equals("GW"))
            return "Loading-affy-data.html";			//
    	if(key.equals("GCOS"))
            return "Loading-affy-data.html";			//
    	if(key.equals("MAGE"))
            return "Loading-magetab-data.html";			//
       	if(key.equals("bioconductor"))						//
            return "Loading-affy-data.html";
    	if(key.equals("softAffy"))							//
            return "Loading-affy-data.html";
    	if(key.equals("softTwoChannel"))					//
            return "Loading-affy-data.html";
    	if(key.equals("Agilent"))							//
            return "Loading-agilent-data.html";	
    	if(key.equals("dChip"))								//
            return "Loading-affy-data.html";
    	if(key.equals("GenePix"))							//
            return "Loading-genepix-data.html";
    	if(key.equals("CGH"))								//
            return "compare.html";
    	//No documentation written
    	if(key.equals("LOLA Login Dialog"))	
            return "Table of Contents.html";
    	
        if(key.equals("KMC Initialization Dialog")) {   //
        	return "KMC.html";
        }
        if(key.equals("COA Result Configuration")) {   //
        	return "coa.html";
        }
        if(key.equals("COA Selection Area Configuration")) {   //
        	return "coa.html";
        }
        if(key.equals("HCL Initialization Dialog"))		//
            return "hcl.html";
        if(key.equals("HCL Tree Properties"))			//
            return "hcl.html";
        if(key.equals("SOTA Initialization Dialog"))	//
            return "SOTA.html";
        if(key.equals("SOTA Tree Properties"))			//
            return "SOTA.html";
        if(key.equals("CAST Initialization Dialog"))	//
            return "cast.html";
        if(key.equals("RN Initialization Dialog"))		//
            return "rn.html";
        if(key.equals("SOM Initialization Dialog"))		//
            return "som.html";
        if(key.equals("GSH Initialization Dialog"))		//
            return "gsh.html";
        if(key.equals("QTC Initialization Dialog"))		//
            return "QTC.html";
        if(key.equals("FOM Initialization Dialog"))		//
            return "FOM.html";
        if(key.equals("KMS Initialization Dialog"))		//
            return "kms.html";
        if(key.equals("PTM Initialization Dialog"))		//
            return "ptm.html";
        if(key.equals("TTEST Initialization Dialog"))	//
            return "test.html";
        if(key.equals("SVM Process Initialization Dialog"))//
            return "svm.html";
        if(key.equals("SVM Training Initialization Dialog"))//
            return "svm.html";
        if(key.equals("ST Initialization Dialog"))		//
            return "st.html";
        if(key.equals("SAM Initialization Dialog"))		//
            return "sam.html";
        if(key.equals("SAM Script Delta Dialog"))		//
            return "sam.html";
        if(key.equals("PCA Initialization Dialog"))										//
            return "pca.html";
        if(key.equals("PCA Result Configuration"))										//
            return "pca.html";  
        if(key.equals("PCA Selection Area Configuration"))								//
            return "pca.html";  
        if(key.equals("One Way ANOVA Initialization Dialog"))							//
            return "anova.html";
        if(key.equals("Bayesian Estimation of Temporal Regulation- Initialization Dialog"))  //
            return "betr.html";
        if(key.equals("Cluster Validation - Initialization Dialog"))		//
            return "clvalid.html";
        if(key.equals("CLVALID - Initialization Dialog"))		//
            return "clvalid.html";
        if(key.equals("Non-negative Matrix Factorization- Initialization Dialog"))		//
            return "nmf.html";
        if(key.equals("Rank Products- Initialization Dialog"))							//
            return "rp.html";
        if(key.equals("NMF Initialization Dialog"))										//
            return "nmf.html";
        if(key.equals("Linear Models for Microarray Data- Initialization Dialog"))		//
            return "limma.html";
        if(key.equals("Global Ancova- Initialization Dialog"))		//
            return "gcova.html";
        if(key.equals("Survival Analysis Initialization Dialog"))	//
            return "surv.html";
        if(key.equals("Terrain Map Initialization Dialog"))	//
            return "trn.html";
        if(key.equals("DAM Initialization Dialog"))			//
            return "dam.html";        
        if(key.equals("DAM Result Configuration"))			//
            return "dam.html";        
        if(key.equals("DAM Selection Area Configuration"))	//
            return "dam.html";        
        if(key.equals("DAM Initialization Dialog"))			//
            return "dam.html";        
        if(key.equals("DAM Process Initialization Dialog"))	//
            return "dam.html";        
        if(key.equals("GDM Initialization Dialog"))			//
            return "gdm.html";  
        if(key.equals("GDM Cluster Browser Dialog"))		//
            return "gdm.html";  
        if(key.equals("GDM Color Range Dialog"))			//
            return "gdm.html";
        if(key.equals("GDM Result Selection Dialog"))		//
            return "gdm.html";
        if(key.equals("EASE Initialization Dialog"))		//
            return "ease.html";
        if(key.equals("EASE StatisticalParams Dialog"))		//
        	return "ease.html";
        if(key.equals("EASE AdvancedParams Dialog"))		//
        	return "ease.html";
        if(key.equals("EASE Threshold Dialog"))				//
            return "ease.html";
//        if(key.equals("EASE File Update Dialog"))
//            return "ease_file_update_parameters.html";
        if(key.equals("Set Lower Cutoffs"))						//
            return "AdjustingtheData.html";
        if(key.equals("Set Percentage Cutoff"))					//
            return "AdjustingtheData.html";
        if(key.equals("Variance Filter Dialog"))				//
            return "AdjustingtheData.html";      
        if(key.equals("Experiment Label Editor"))				//
            return "displayoptions.html";       
        if(key.equals("KNNC Mode Selection"))					//
            return "knn.html";
        if(key.equals("KNNC Parameter Selections"))				//
            return "knn.html";
        if(key.equals("KNNC Validation Parameter Selections"))	//
            return "knn.html";
        if(key.equals("Cluster Save Dialog"))					//
            return "WorkingwithClusters.html";
        if(key.equals("List Import Dialog"))					//
            return "workingwithclusters.html";
        if(key.equals("Binned Import Dialog"))					//
            return "workingwithclusters.html";
        if(key.equals("Auto Import Dialog"))					//
            return "workingwithclusters.html";
        if(key.equals("Script Attribute Dialog"))				//
            return "script.html";        
        if(key.equals("Script Algorithm Initialization Dialog"))//
            return "script.html";
        if(key.equals("Script Value Input Dialog"))				//
            return "script.html"; 
        if(key.equals("Script Error Log"))						//
            return "script.html";          
        if(key.equals("Diversity Ranking Cluster Selection"))	//
            return "script-diversity-filter.html";
        if(key.equals("Centroid Variance/Entropy Ranking Cluster Selection"))	//
            return "script-centroid-variance-filter.html";
        //TODO find a file to link this to
        if(key.equals("Cluster Archive Submission"))			//
            return "centroid_entropy_variance_sel_dialog.html";
        if(key.equals("Search Dialog"))						//
            return "UM.html#search";
        if(key.equals("Search Result Dialog"))				//
            return "UM.html#results";
        if(key.equals("Import Result Dialog"))				//
            return "UM.html#importresults";
        if(key.equals("Gene Annotation Import"))			//
            return "UM.html#AGA";			
        if(key.equals("Newick File Output Dialog"))			//
            return "hcl_newick_output_parameters.html";
        if(key.equals("Nexus File Output Dialog"))			//
            return "hcl_nexus_output_parameters.html";      
        //There are no help documents for this
        if(key.equals("Genome Annotation Dialog"))			//
            return "Table of Contents.html";						
        if(key.equals("Color Scale Dialog"))				//
            return "displayoptions.html";
		if (key.equals("USC Initialization Dialog")) {		//
			return "usc.html";
		}
		if (key.equals("USC Assign Label Dialog")) {		//
			return "usc.html";
		}
		if (key.equals("USC Delta Dialog")) {				//
			return "usc.html";
		} 
		if (key.equals("USC Load Result Dialog")) {			//
			return "usc.html";
		}
		if(key.equals("BN Initialization Dialog")) {		//
			return "bn.html";
		}
		if(key.equals("BN File Update Dialog")) {			//
			return "bn.html";
		}
		if(key.equals("LM Initialization Dialog")) {		//
			return "lm.html";
		}
		if(key.equals("MINET Initialization Dialog")) {		//
			return "minet.html";
		}
		if(key.equals("edgeR Initialization Dialog")) {
			return "edger.html";
		}
		if(key.equals("DESeq Initialization Dialog")) {
			return "deseq.html";
		}
		if(key.equals("DEGseq Initialization Dialog")) {
			return "degseq.html";
		}
		if(key.equals("RNASeq Loader")) {
			return "Loading-rnaseq-data.html";
		}
		if(key.equals("EASE Threshold Dialog")) {
			return "ease_threshold.html";
		}

    	if(key.equals("LEM Initialization Dialog"))			//
    		return "lem.html";
    	if(key.equals("LEM Bin Color and Limits Selection Dialog")) //
    		return "lem.html";
    	if(key.equals("LEM Customization Dialog"))			//
    		return "lem.html";
    	if(key.equals("LEM Selection Range Dialog"))		//
    		return "lem.html";
    	if(key.equals("NonpaR Mode Selection"))				//
    		return "nonpar.html";
    	if(key.equals("NonpaR Significance Parameters"))	//
    		return "nonpar.html";
    	if(key.equals("NonpaR Fisher Exact Parameters"))	//
    		return "nonpar.html";
    	if(key.equals("Java Out of Memory Error"))			//
    		return "oome.html";
    	if(key.equals("GSEA Help Dialog")){					//
    		return "gsea.html";
    	}
		else {
			return null;
		}

    }
    
    public class FillPanel extends JPanel{
        
        public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            Dimension dim = this.getSize();
            gp = new GradientPaint(0,dim.height/2,backgroundColor,dim.width,dim.height/2,fadeColor);
            g2.setPaint(gp);
            g2.fillRect(0,0,dim.width, dim.height);
            g2.setColor(Color.black);
        }
    }
    
  /*
    public static void main(String [] args){
        JFrame frame = new JFrame();
        frame.setSize(400,800);
        HelpWindow w = new HelpWindow(0);
        //   HelpWindow w2 = new HelpWindow(1);
        JPanel mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(100,100));
        mainPanel.setLayout(new GridBagLayout());
   
        frame.getContentPane().add(w, BorderLayout.CENTER);
        frame.validate();
        frame.setVisible(true);
    }
   */
}
