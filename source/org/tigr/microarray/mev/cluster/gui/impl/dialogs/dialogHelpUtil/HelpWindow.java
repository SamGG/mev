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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    
    public boolean getWindowContent(){
        String fileName = getFileName(dialogName);
        if(fileName == null){
            JOptionPane.showMessageDialog(this, dialogName+" help page cannot be located.");
            return false;
        }
        try{
            URL url = getClass().getResource("/org/tigr/microarray/mev/cluster/gui/impl/dialogs/dialogHelpUtil/dialogHelpPages/"+fileName);
            pane.setPage(url);
            return true;
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, dialogName+" help page cannot be found.");
            //e.printStackTrace();
            //  dispose();
            return false;
        }
    }
    
    public void setLocation(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
    }
    
    
    private String getFileName(String key){
    	if(key.equals("TDMS"))
            return "TDMS.html";
    	if(key.equals("Mev"))
            return "Mev.html";
    	if(key.equals("Tav"))
            return "Tav.html";
    	if(key.equals("GW"))
            return "GW.html";
    	if(key.equals("GCOS"))
            return "GCOS.html";
    	if(key.equals("MAGE"))
            return "magetab.html";
       	if(key.equals("bioconductor"))
            return "bioconductor.html";
    	if(key.equals("softAffy"))
            return "softAffy.html";
    	if(key.equals("softTwoChannel"))
            return "softTwoChannel.html";
    	if(key.equals("Agilent"))
            return "Agilent.html";
    	if(key.equals("CGH"))
            return "CGH.html";
    	if(key.equals("dChip"))
            return "dChip.html";
    	if(key.equals("GenePix"))
            return "GenePix.html";
        if(key.equals("KMC Initialization Dialog"))
            return "kmc_parameters.html";
        if(key.equals("HCL Initialization Dialog"))
            return "hcl_parameters.html";
        if(key.equals("SOTA Initialization Dialog"))
            return "sota_parameters.html";
        if(key.equals("CAST Initialization Dialog"))
            return "cast_parameters.html";
        if(key.equals("PCA Initialization Dialog"))
            return "pca_parameters.html";
        if(key.equals("RN Initialization Dialog"))
            return "rn_parameters.html";
        if(key.equals("SOM Initialization Dialog"))
            return "som_parameters.html";
        if(key.equals("GSH Initialization Dialog"))
            return "gsh_parameters.html";
        if(key.equals("QTC Initialization Dialog"))
            return "qtc_parameters.html";
        if(key.equals("FOM Initialization Dialog"))
            return "fom_parameters.html";
        if(key.equals("KMS Initialization Dialog"))
            return "kms_parameters.html";
        if(key.equals("PTM Initialization Dialog"))
            return "ptm_parameters.html";
        if(key.equals("TTEST Initialization Dialog"))
            return "ttest_parameters.html";
        if(key.equals("SVM Process Initialization Dialog"))
            return "svm1_parameters.html";
        if(key.equals("SVM Training Initialization Dialog"))
            return "svm2_parameters.html";
        if(key.equals("ST Initialization Dialog"))
            return "st_parameters.html";
        if(key.equals("SAM Initialization Dialog"))
            return "sam_parameters.html";
        if(key.equals("Iterative Log Mean Centering Initialization Dialog"))
            return "iterativelog_parameters.html";
        if(key.equals("Linear Regression Initialization Dialog"))
            return "linreg_parameters.html";
        if(key.equals("Ratio Statistics Initialization Dialog"))
            return "ratiostats_parameters.html";     
        if(key.equals("HCL Tree Properties"))
            return "hcl_tree_config_parameters.html";
        if(key.equals("PCA Result Configuration"))
            return "pca_result_config.html";  
        if(key.equals("PCA Selection Area Configuration"))
            return "pca_selection_area.html";  
        if(key.equals("One Way ANOVA Initialization Dialog"))
            return "one_way_anova_parameters.html";
        if(key.equals("Bayesian Estimation of Temporal Regulation- Initialization Dialog"))
            return "betr_parameters.html";
        if(key.equals("Non-negative Matrix Factorization- Initialization Dialog"))
            return "nmf_parameters.html";
        if(key.equals("Rank Products- Initialization Dialog"))
            return "rp_parameters.html";
        if(key.equals("NMF Initialization Dialog"))
            return "nmf_parameters.html";
        if(key.equals("Linear Models for Microarray Data- Initialization Dialog"))
            return "limma_parameters.html";
        if(key.equals("Cluster Save Dialog"))
            return "cluster_save_parameters.html";
        if(key.equals("Terrain Map Initialization Dialog"))
            return "terrain_parameters.html";
        if(key.equals("File Loader"))
            return "file_loader.html";
        if(key.equals("SOTA Tree Properties"))
            return "sota_tree_config_parameters.html";
        if(key.equals("DAM Initialization Dialog"))
            return "dam_parameters.html";        
        if(key.equals("GDM Initialization Dialog"))
            return "gdm_parameters.html";  
        if(key.equals("GDM Color Range Dialog"))
            return "gdm_color_range.html";
        if(key.equals("EASE Initialization Dialog"))
            return "ease_parameters.html";
        if(key.equals("GDM Result Selection Dialog"))
            return "result_selection_parameters.html";
        if(key.equals("Set Lower Cutoffs"))
            return "lower_cutoff_parameters.html";
        if(key.equals("Set Percentage Cutoff"))
            return "percent_cutoff_parameters.html";
        if(key.equals("Data Download"))
            return "jws_download_message.html";
        if(key.equals("Welcome to MeV!"))
            return "jws_welcome_message.html";
        if(key.equals("Experiment Label Editor"))
            return "experiment_label_editor.html";       
        if(key.equals("KNNC Mode Selection"))
            return "knnc_parameters1.html";
        if(key.equals("KNNC Parameter Selections"))
            return "knnc_parameters2.html";
        if(key.equals("KNNC Validataion Parameter Selections"))
            return "knnc_parameters3.html";
        if(key.equals("SAM Script Delta Dialog"))
            return "sam_script_delta_dialog.html";
        if(key.equals("List Import Dialog"))
            return "cluster_list_import_parameters.html";
        if(key.equals("Binned Import Dialog"))
            return "binned_cluster_list_import_parameters.html";
        if(key.equals("Auto Import Dialog"))
            return "auto_cluster_list_import_parameters.html";
        if(key.equals("Script Attribute Dialog"))
            return "script_attribute_parameters.html";        
        if(key.equals("Script Algorithm Initialization Dialog"))
            return "script_algorithm_selection.html";
        if(key.equals("Script Value Editor Dialog"))
            return "script_value_editor_dialog.html"; 
        if(key.equals("Script Error Log"))
            return "script_error_log.html";          
        if(key.equals("Diversity Ranking Cluster Selection"))
            return "diversity_ranking_cluster_sel_dialog.html";
        if(key.equals("Centroid Variance/Entropy Ranking Cluster Selection"))
            return "centroid_entropy_variance_sel_dialog.html";
        if(key.equals("Search Dialog"))
            return "search_init_parameters.html";
        if(key.equals("Search Result Dialog"))
            return "search_result_dialog.html";
        if(key.equals("Import Result Dialog"))
            return "import_result_dialog.html";
        if(key.equals("EASE Threshold Dialog"))
            return "ease_threshold_parameters.html";
        if(key.equals("Newick File Output Dialog"))
            return "hcl_newick_output_parameters.html";
        if(key.equals("Nexus File Output Dialog"))
            return "hcl_nexus_output_parameters.html";
        if(key.equals("EASE File Update Dialog"))
            return "ease_file_update_parameters.html";
        if(key.equals("Variance Filter Dialog"))
            return "variance_filter_parameters.html";            
        if(key.equals("Gene Annotation Import"))
            return "gene_annotation_import_parameters.html";
        if(key.equals("Color Scale Dialog"))
            return "set_ratio_scale_parameters.html";
		if (key.equals("USC Initialization Dialog")) {
			return "usc_parameters1.html";
		}
		if (key.equals("USC Assign Label Dialog")) {
			return "usc_parameters2.html";
		}
		if (key.equals("USC Delta Dialog")) {
			return "usc_parameters3.html";
		} 
		if (key.equals("USC Load Result Dialog")) {
			return "usc_parameters4.html";
		} 
		if(key.equals("BN Initialization Dialog")) {
			return "bn_parameters.html";
		}
		if(key.equals("LM Initialization Dialog")) {
			return "lm_parameters.html";
		}
        	if(key.equals("LEM Initialization Dialog"))
        		return "lem_parameters.html";
        	if(key.equals("LEM Bin Color and Limits Selection Dialog"))
        		return "lem_color_limits_parameters.html";
        	if(key.equals("LEM Customization Dialog"))
        		return "lem_customization_parameters.html";
        	if(key.equals("LEM Selection Range Dialog"))
        		return "lem_selection_range_paramters.html";
        	if(key.equals("NonpaR Mode Selection"))
        		return "nonpar_mode_parameters.html";
        	if(key.equals("NonpaR Significance Parameters"))
        		return "nonpar_significance_parameters.html";
        	if(key.equals("NonpaR Fisher Exact Parameters"))
        		return "nonpar_fisher_exact_parameters.html";
        	
        	if(key.equals("Java Out of Memory Error"))
        		return "hcl_out_of_memory_help.html";
        	if(key.equals("GSEA Help Dialog")){
        		return "GSEA_Help.html";
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
