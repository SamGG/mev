package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.AnnotationURLLinkDialog.EventListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.StringSplitter;

public class GSEAURLLinkDialog extends AlgorithmDialog{
	
	File file;
    JComboBox annotationFieldsBox, urlTypesBox;
    String[] urlTemplates, urlKeys;
    IData data;
    Experiment experiment;
    int row;
    private int[] lastSelectedIndices;
    boolean okPressed = false;    
    String annotationKey;
    boolean hasAnnotationKey=false;
    String colName;
    
	
	 
	   
    public GSEAURLLinkDialog(JFrame parent, boolean modal, String annotationKey, int row, String colName, File file) {
    	 super(parent, "Link annotation to URL", modal);
   	
    	 this.file = file;
         this.row = row;
         this.colName=colName;
         this.lastSelectedIndices = ExperimentUtil.lastSelectedAnnotationIndices;
         this.hasAnnotationKey=true;
         this.annotationKey=annotationKey;
         setBounds(0, 0, 500, 300);
         setBackground(Color.white);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         
         populateFields();
         
         annotationFieldsBox.setSelectedIndex(0);
         urlTypesBox.setSelectedIndex(0);
         
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints constraints = new GridBagConstraints();
         //constraints.fill = GridBagConstraints.BOTH;
         
         JPanel pane = new JPanel();
         pane.setLayout(gridbag);     
         
         JPanel topPanel = new JPanel();
         topPanel.setBackground(Color.white);
         topPanel.setBorder(new TitledBorder("Select annotation to link out from"));
         GridBagLayout grid1 = new GridBagLayout();        
         topPanel.setLayout(grid1);
         
         //annotationFieldsBox = new JComboBox(annotationFields);
         buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
         grid1.setConstraints(annotationFieldsBox, constraints);
         topPanel.add(annotationFieldsBox);
         
         JPanel bottomPanel = new JPanel();
         bottomPanel.setBackground(Color.white);
         bottomPanel.setBorder(new TitledBorder("Select internet resource to link to"));
         GridBagLayout grid2 = new GridBagLayout();        
         bottomPanel.setLayout(grid2);
         
         //urlTypesBox = new JComboBox(urlTypes);
         buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
         grid2.setConstraints(urlTypesBox, constraints);
         bottomPanel.add(urlTypesBox);        
         
         constraints.fill = GridBagConstraints.BOTH;
         
         buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
         gridbag.setConstraints(topPanel, constraints);
         pane.add(topPanel);
         
         buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
         gridbag.setConstraints(bottomPanel, constraints);
         pane.add(bottomPanel);        
         
         addContent(pane);
         EventListener listener = new EventListener();
         setActionListeners(listener);
         this.addWindowListener(listener);        
    
     }    
   
	
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }    
    
    public boolean isOkPressed() {
        return okPressed;
    }    
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }  
    
    private void populateFields() {
        Vector annotFieldsVector = new Vector();
        Vector urlKeysVector = new Vector();
        Vector urlTemplateVector = new Vector();
        Vector urlDescriptionVector = new Vector();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader buff = new BufferedReader(fr);
            StringSplitter st = new StringSplitter('\t');
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) eof = true;
                else {
                    st.init(line);
                    urlKeysVector.add(st.nextToken());
                    urlTemplateVector.add(st.nextToken());
                    urlDescriptionVector.add(st.nextToken());
                }
            }
            buff.close();
            String[] urlDescriptions = new String[urlDescriptionVector.size()];
            for (int i = 0; i < urlDescriptions.length; i++) {
                urlDescriptions[i] = (String)(urlDescriptionVector.get(i));
            }
            urlTypesBox = new JComboBox(urlDescriptions);
            annotationFieldsBox = new JComboBox(new String[]{this.colName});
            //annotationFieldsBox = new JComboBox(data.getFieldNames());--commented for gsea testing
            urlTemplates = new String[urlTemplateVector.size()];
            urlKeys = new String[urlKeysVector.size()];
            
            for (int i = 0; i < urlTemplates.length; i++) {
                urlTemplates[i] = (String)(urlTemplateVector.get(i));
            }
            for (int i = 0; i < urlKeys.length; i++) {
                urlKeys[i] = (String)(urlKeysVector.get(i));
            }
        } catch (java.io.FileNotFoundException fne) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not find \"annotation_URLs.txt\" file.", "Error", JOptionPane.ERROR_MESSAGE);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Incompatible \"annotation_URLs.txt\" file. Possible issues: extra newline characters, too many or too few tabs per line", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
   
    
    
    //Added for GSEA  
    private void launchBrowser(String annotationKey) {
        
        try {
            String urlToUse = getCurrentURL(annotationKey);
            //System.out.println("url To use = " + urlToUse);
            BrowserLauncher.openURL(urlToUse);
        } catch (IOException ie) {
            JOptionPane.showMessageDialog(new JFrame(), ie.toString(),"Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Browser could not be launched! Possible problem: the annotation format may not be appropriate for this URL type!","Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    

    
    private String getCurrentURL(String annKey) {
        int fieldIndex = annotationFieldsBox.getSelectedIndex(); 
        int urlTemplateIndex = urlTypesBox.getSelectedIndex();   
        String currentURLTemplate = urlTemplates[urlTemplateIndex];
        String currentURLKey = urlKeys[urlTemplateIndex];
        String urlToUse = "";
        //NOTE: In the following statement, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs        
        String currentAnnotationString = annKey; 
      //  System.out.println("currentAnnotationString = " + currentAnnotationString);

        if (currentURLKey.equals("UniGene")) {
            String[] splitAnnotation = currentAnnotationString.split("\\.");
           
            String s1 = currentURLTemplate.replaceAll("FIELD1", splitAnnotation[1]);
            urlToUse = s1.replaceAll("FIELD2", splitAnnotation[0]);
        } else {            
            urlToUse = currentURLTemplate.replaceAll("FIELD1", currentAnnotationString);
        }
      // System.out.println("url To use = " + urlToUse);   
        return urlToUse;
    }


    public int[] getLastSelectedIndices() {
        return lastSelectedIndices;
    }
    
    public void setLastSelectedIndices() {
      
        lastSelectedIndices[0] = annotationFieldsBox.getSelectedIndex();
        lastSelectedIndices[1] = urlTypesBox.getSelectedIndex();
              
    }
    
    public class EventListener extends WindowAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){                
                okPressed = true;
                setLastSelectedIndices();
                ExperimentUtil.lastSelectedAnnotationIndices = getLastSelectedIndices();
                //int index = urlTypesBox.getSelectedIndex();
                launchBrowser(annotationKey);
                //BrowserLauncher.openURL(urlTemplates[index]);
                dispose();
            } else if (command.equals("reset-command")) {
                annotationFieldsBox.setSelectedIndex(0);
                urlTypesBox.setSelectedIndex(0);
                setLastSelectedIndices();
                 ExperimentUtil.lastSelectedAnnotationIndices = getLastSelectedIndices();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                
            }
        }
        
    }    

    
    
    
    
	
}
