/*
 * AnnotationURLLinkDialog.java
 *
 * Created on June 30, 2004, 1:33 PM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.util.*;

/**
 *
 * @author  nbhagaba
 */
public class AnnotationURLLinkDialog extends AlgorithmDialog {
        //NOTE: In the main constructor for this class, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs     
    File file;
    JComboBox annotationFieldsBox, urlTypesBox;
    String[] urlTemplates, urlKeys;
    IData data;
    Experiment experiment;
    int row;
    private int[] lastSelectedIndices;
    boolean okPressed = false;    
    String annotationKey;
    
    /** Creates a new instance of AnnotationURLLinkDialog */
    public AnnotationURLLinkDialog(JFrame parent, boolean modal, String[] annotationFields, String[] urlTypes) {
        //this constructor just used for testing, see real constructor below
        super(parent, "Link annotation to URL", modal);
        
        setBounds(0, 0, 500, 300);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        
        annotationFieldsBox = new JComboBox(annotationFields);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        grid1.setConstraints(annotationFieldsBox, constraints);
        topPanel.add(annotationFieldsBox);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.white);
        bottomPanel.setBorder(new TitledBorder("Select internet resource to link to"));
        GridBagLayout grid2 = new GridBagLayout();        
        bottomPanel.setLayout(grid2);
        
        urlTypesBox = new JComboBox(urlTypes);
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
    
    public AnnotationURLLinkDialog(JFrame parent, boolean modal, Experiment experiment, IData data, int row, File file) {
        super(parent, "Link annotation to URL", modal);
        this.data = data;
        this.experiment = experiment;
        this.file = file;
        this.row = row;
        setBounds(0, 0, 500, 300);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        populateFields();
        
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
    
    public AnnotationURLLinkDialog(JFrame parent, boolean modal, Experiment experiment, IData data, int row, File file, int[] lastSelectedIndices) {
        super(parent, "Link annotation to URL", modal);
        this.data = data;
        this.experiment = experiment;
        this.file = file;
        this.row = row;
        this.lastSelectedIndices = lastSelectedIndices;
        setBounds(0, 0, 500, 300);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        populateFields();
        
        annotationFieldsBox.setSelectedIndex(lastSelectedIndices[0]);
        urlTypesBox.setSelectedIndex(lastSelectedIndices[1]);
        
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
            annotationFieldsBox = new JComboBox(data.getFieldNames());
            urlTemplates = new String[urlTemplateVector.size()];
            urlKeys = new String[urlKeysVector.size()];
            
            for (int i = 0; i < urlTemplates.length; i++) {
                urlTemplates[i] = (String)(urlTemplateVector.get(i));
            }
            for (int i = 0; i < urlKeys.length; i++) {
                urlKeys[i] = (String)(urlKeysVector.get(i));
            }
        } catch (java.io.FileNotFoundException fne) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not find \"annotation_URLs.txt\" file in \"config\" directory", "Error", JOptionPane.ERROR_MESSAGE);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Incompatible \"annotation_URLs.txt\" file in \"config\" directory! Possible issues: extra newline characters, too many or too few tabs per line", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void launchBrowser() {
        //int fieldIndex = annotationFieldsBox.getSelectedIndex(); 
        //int urlTemplateIndex = urlTypesBox.getSelectedIndex();
        try {
             //String currentURLTemplate = urlTemplates[urlTemplateIndex];
            //NOTE: In the following statement, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs 
            //String currentAnnotationString = data.getElementAttribute(row, fieldIndex);
            //String urlToUse = currentURLTemplate.replaceAll("FIELD1", currentAnnotationString);
            String urlToUse = getCurrentURL();
            //System.out.println("url To use = " + urlToUse);
            //BrowserLauncher.openURL(urlTemplates[fieldIndex]);
            BrowserLauncher.openURL(urlToUse);
        } catch (IOException ie) {
            JOptionPane.showMessageDialog(new JFrame(), ie.toString(),"Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Browser could not be launched! Possible problem: the annotation format may not be appropriate for this URL type!","Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getCurrentURL() {
        int fieldIndex = annotationFieldsBox.getSelectedIndex(); 
        int urlTemplateIndex = urlTypesBox.getSelectedIndex();   
        String currentURLTemplate = urlTemplates[urlTemplateIndex];
        String currentURLKey = urlKeys[urlTemplateIndex];
        String urlToUse = "";
        //NOTE: In the following statement, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs        
        String currentAnnotationString = data.getElementAttribute(row, fieldIndex); 
        //System.out.println("currentAnnotationString = " + currentAnnotationString);

        if (currentURLKey.equals("UniGene")) {
            String[] splitAnnotation = currentAnnotationString.split("\\.");
            /*
            for (int i = 0; i < splitAnnotation.length; i++) {
                System.out.print("splitAnnotation[" + i + "] = " + splitAnnotation[i]);
            }
             */
            String s1 = currentURLTemplate.replaceAll("FIELD1", splitAnnotation[1]);
            urlToUse = s1.replaceAll("FIELD2", splitAnnotation[0]);
        } else {            
            urlToUse = currentURLTemplate.replaceAll("FIELD1", currentAnnotationString);
        }
        //System.out.println("url To use = " + urlToUse);   
        return urlToUse;
    }
    
    public int[] getLastSelectedIndices() {
        return lastSelectedIndices;
    }
    
    public void setLastSelectedIndices() {
        //int[] lastSelectedIndices = new int[2];
        lastSelectedIndices[0] = annotationFieldsBox.getSelectedIndex();
        lastSelectedIndices[1] = urlTypesBox.getSelectedIndex();
        //return lastSelectedIndices;        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){                
                okPressed = true;
                setLastSelectedIndices();
                ExperimentUtil.lastSelectedAnnotationIndices = getLastSelectedIndices();
                //int index = urlTypesBox.getSelectedIndex();
                launchBrowser();
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
    
    public static void main (String[] args) {
        String[] arg1 = {"User-defined field 1", "User-defined field 2", "User-defined field 3", "User-defined field 4"};
        String[] arg2 = {"GenBank", "TC#", "LocusLink", "Unigene"};
        
        AnnotationURLLinkDialog aDialog = new AnnotationURLLinkDialog(new JFrame(), false, arg1, arg2);
        aDialog.setVisible(true);
    }
    
}
