/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * LolaSubmission.java
 *
 * Created on July 2, 2004, 12:03 PM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit.lola;

import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.submit.IClusterSubmitter;
import org.tigr.microarray.mev.cluster.clusterUtil.submit.RepositoryConfigParser;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;

/**
 *
 * @author  braisted
 */
public class LOLASubmitter implements IClusterSubmitter {
    
    private Cluster cluster;
    private IFramework framework;
    private RepositoryConfigParser parser;
    private Submitter submitter;
    private Thread thread;
    private boolean stop = false;
    
    /** Creates a new instance of LolaSubmission */
    public LOLASubmitter() {
        
    }
    
    public boolean submit(Cluster cluster, IFramework framework, RepositoryConfigParser parser) {
        this.cluster = cluster;
        this.framework = framework;
        this.parser = parser;
        
        try {
            submitter = new Submitter();
            thread = new Thread(submitter);
            thread.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Error starting the submission thread, submission aborted", "Submission Thread Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
        /*
        //get the sample names in order that are contained in the cluster
        IData data = framework.getData();
         
        if( ! ( data.getDataType() == IData.DATA_TYPE_AFFY_ABS  ||
        data.getDataType() == IData.DATA_TYPE_AFFY_REF  ||
        data.getDataType() == IData.DATA_TYPE_AFFY_MEAN  ||
        data.getDataType() == IData.DATA_TYPE_AFFY_MEDIAN )) {
            JOptionPane.showMessageDialog(framework.getFrame(), "The Data Type must be Human Affymetrix data to comply with the LOLA repository.", "Data Type Mismatch Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
         
        Experiment experiment = cluster.getExperiment();
        int count = experiment.getNumberOfSamples();
        int [] sampleOrder = experiment.getColumnIndicesCopy();  //sample indices
        String [] expNames = new String[count];
        for(int i = 0; i < count; i++) {
            expNames[i] = data.getSampleName(sampleOrder[i]);
        }
         
        //get email and password information
        Hashtable userInfo = parser.getUserInfo("LOLA");
        String email = (String)(userInfo.get("email"));
        String pw = (String)(userInfo.get("password"));
        LOLAPasswordDialog passDialog = new LOLAPasswordDialog(email, pw);
         
        if(passDialog.showModal() != JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Submission aborted prior to login.", "Abort LOLA Submission", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
         
        email = passDialog.getUserName();
        pw = passDialog.getPassword();
         
        //have user info open lola dialog
        LOLADialog dialog = new LOLADialog("Export Gene List to LOLA", expNames, cluster);
        int [] sampleGroupings;
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            sampleGroupings = dialog.getGroupAssignments();
         
            float [] folds = getFolds(data, cluster, expNames, sampleGroupings);
         
            String geneList = constructGeneList(data, cluster.getIndices(), folds);
         
            if( email != null && pw != null) {
                // connect to LOLA
                connLOLA connection = new connLOLA(email, pw);
                String sessionID = connection.login();
                if(sessionID.equals("")) {
                    JOptionPane.showMessageDialog(framework.getFrame(), "Error during login to LOLA.  Please go to http://lola.gwu.edu to establish an account.\n"+
                    "If you have an account edit the user information for LOLA in the archive_submission_config.xml (in the config folder) to reflect\nyour email and LOLA password.", "LOLA Login Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!connection.submit_list(cluster.getClusterLabel(), cluster.getClusterDescription(), geneList)) {
                    JOptionPane.showMessageDialog(framework.getFrame(), "After login, an error occurred during list submission to LOLA.\nPlease check that the primary identifiers are Affy ID's or LocusLink.\n"+
                    "The submission page for LOLA describes the submission requirements.", "LOLA Submission Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                connection.create_list();
                connection.logout();
                JOptionPane.showMessageDialog(framework.getFrame(), "Submission to LOLA completed successfully.  Thank you.", "LOLA Submission Confirmation", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(framework.getFrame(), "Error during login to LOLA.  No login information available.  Please go to http://lola.gwu.edu to establish an account.\n"+
                "If you have an account edit the user information for LOLA in the archive_submission_config.xml (in the config folder) to reflect\nyour email and LOLA password.", "LOLA Login Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
         **/
    }
    
    
    
    
    private float [] getFolds(IData data, Cluster cluster, String [] sampleNames, int [] groupings) {
        
        int sns = sampleNames.length;
        int[] genes = cluster.getIndices();
        float[] signals = new float[sns];
        float[] folds = new float[genes.length];
        
        for(int i=0; i<genes.length; i++){
            for(int j = 0; j<sns; j++){
                signals[j] = data.getRatio(j,genes[i],data.LINEAR);
            }
            folds[i] = get_fold(signals, groupings);
        }
        return folds;
    }
    
    //pcahan
    private float get_fold(float[] signals, int [] groupings){
        float mean[] = new float[3];
        mean[0] = mean[1] = mean[2] = 0.0f;
        int num_a, num_b;
        num_a = num_b = 0;
        for (int i = 0; i < signals.length; i++){
            //System.out.println("sig i: " + signals[i]);
            if (groupings[i] == 0){
                num_a++;
            }
            else if(groupings[i] == 1){
                num_b++;
            }
            mean[groupings[i]] += signals[i];
        }
        mean[0] = mean[0]/num_a;
        mean[1] = mean[1]/num_b;
        //System.out.println("Mean a: " + mean[0] + "Mean b: " + mean[1]);
        return mean[0]/mean[1];
    }
    
    
    private String constructGeneList(IData data, int [] rows, float [] folds) {
        String gene_list = new String();
        // loops over genes
        for (int i=0; i<rows.length; i++) {
            gene_list += data.getElementAttribute(rows[i], 0) + "\t"+folds[i]+"\n";
        }
        return gene_list;
    }
    
    private class Submitter implements Runnable {
        public void run() {
            
            Logger logger = new Logger(framework.getFrame(), "LOLA Submission Log", new SubmissionListener());
            logger.show();
            logger.append("Begin LOLA Submission Process\n");
            //get the sample names in order that are contained in the cluster
            IData data = framework.getData();
            
            logger.append("Data Type Verification\n");
            
            if( ! ( data.getDataType() == IData.DATA_TYPE_AFFY_ABS  ||
            data.getDataType() == IData.DATA_TYPE_AFFY_REF  ||
            data.getDataType() == IData.DATA_TYPE_AFFY_MEAN  ||
            data.getDataType() == IData.DATA_TYPE_AFFY_MEDIAN )) {
                JOptionPane.showMessageDialog(framework.getFrame(), "The Data Type must be Human Affymetrix data to comply with the LOLA repository.", "Data Type Mismatch Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if(stop)
                return;

            logger.append("Extract Sample Names\n");
            
            Experiment experiment = cluster.getExperiment();
            int count = experiment.getNumberOfSamples();
            int [] sampleOrder = experiment.getColumnIndicesCopy();  //sample indices
            String [] expNames = new String[count];
            for(int i = 0; i < count; i++) {
                expNames[i] = data.getSampleName(sampleOrder[i]);
            }
            
            logger.append("Get User Login Info\n");
            //get email and password information
            Hashtable userInfo = parser.getUserInfo("LOLA");
            String email = (String)(userInfo.get("email"));
            String pw = (String)(userInfo.get("password"));
            LOLAPasswordDialog passDialog = new LOLAPasswordDialog(email, pw);
            
            if(stop)
                return;
            
            if(passDialog.showModal() != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(framework.getFrame(), "Submission aborted prior to login.", "Abort LOLA Submission", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            email = passDialog.getUserName();
            pw = passDialog.getPassword();
            
            logger.append("Collect Grouping Information and Fold Changes\n");
            
            //have user info open lola dialog
            LOLADialog dialog = new LOLADialog("Export Gene List to LOLA", expNames, cluster);
            int [] sampleGroupings;
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                sampleGroupings = dialog.getGroupAssignments();
                
                float [] folds = getFolds(data, cluster, expNames, sampleGroupings);
                
                logger.append("Construct Gene List\n");
                
                String geneList = constructGeneList(data, cluster.getIndices(), folds);
                                              
                if(stop)
                    return;
                
                if( email != null && pw != null) {
                    // connect to LOLA
                    connLOLA connection = new connLOLA(email, pw);
                    
                    logger.append("Login to LOLA\n");
                    
                    String sessionID = connection.login();
                    
                    if(stop)
                        return;
                    
                    if(sessionID.equals("")) {
                        JOptionPane.showMessageDialog(framework.getFrame(), "Error during login to LOLA.  Please go to http://lola.gwu.edu to establish an account.\n"+
                        "If you have an account please try again. You may Edit the user information for LOLA in the archive_submission_config.xml (in the config folder) to reflect\nyour email and LOLA password so that MeV will retain this information for you.", "LOLA Login Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.append("Login Complete\n");
                    logger.append("Submit List\n");
                    
                    if(!connection.submit_list(cluster.getClusterLabel(), cluster.getClusterDescription(), geneList)) {
                        JOptionPane.showMessageDialog(framework.getFrame(), "After login, an error occurred during list submission to LOLA.\nPlease check that the primary identifiers are Affy ID's or LocusLink.\n"+
                        "The submission page for LOLA describes the submission requirements.", "LOLA Submission Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.append("Commit List\n");
                    connection.create_list();
                    
                    logger.append("Logout from LOLA\n");
                    connection.logout();
                    
                    JOptionPane.showMessageDialog(framework.getFrame(), "Submission to LOLA completed successfully.  Thank you.", "LOLA Submission Confirmation", JOptionPane.INFORMATION_MESSAGE);
                    logger.append("Record Submission to History Node\n");
                    
                    framework.addHistory("Gene submission to LOLA, Cluster #"+cluster.getSerialNumber()+", containing "+cluster.getSize()+" genes.");
                    logger.dispose();
                } else {
                    JOptionPane.showMessageDialog(framework.getFrame(), "Error during login to LOLA.  No login information available.  Please go to http://lola.gwu.edu to establish an account.\n"+
                    "If you have an account edit the user information for LOLA in the archive_submission_config.xml (in the config folder) to reflect\nyour email and LOLA password.", "LOLA Login Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            return;
            
        }
    }
    
    private class SubmissionListener extends DialogListener {
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            if(thread != null)
                stop = true;
        }
        
    }
    
}
