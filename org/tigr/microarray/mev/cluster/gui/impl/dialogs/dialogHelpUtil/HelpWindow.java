/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * HelpWindow.java
 *
 * Created on March 5, 2003, 10:36 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil;

import java.io.*;

import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;

import java.awt.GradientPaint;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

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
            this.setTitle(this.dialogName+": Parameter Information");
        
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
            JOptionPane.showMessageDialog(this, dialogName+" help page can not be located.");
            return false;
        }
        try{
            URL url = getClass().getResource("/org/tigr/microarray/mev/cluster/gui/impl/dialogs/dialogHelpUtil/dialogHelpPages/"+fileName);
            pane.setPage(url);
            return true;
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, dialogName+" help page can not be found.");
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
        if(key.equals("Cluster Save Dialog"))
            return "cluster_save_parameters.html";
        if(key.equals("Terrain Map Initialization Dialog"))
            return "terrain_parameters.html";
        if(key.equals("File Loader"))
            return "file_loader.html";
        if(key.equals("SOTA Tree Properties"))
            return "sota_tree_config_parameters.html";
        if(key.equals("GDM Initialization Dialog"))
            return "gdm_parameters.html";  
        if(key.equals("GDM Color Range Dialog"))
            return "gdm_color_range.html";
        if(key.equals("EASE Initialization Dialog"))
            return "ease_parameters.html";
        else
            return null;
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
