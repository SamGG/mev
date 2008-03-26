/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SubmissionManager.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:47 $
 * $Author: caliente $
 * $State: Exp $
 */
/*
 * SubmissionManager.java
 *
 * Created on June 25, 2004, 10:33 AM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  braisted
 */

public class SubmissionManager {
    
    private RepositoryConfigParser parser;
    private ClusterRepository repository;
    private IFramework framework;
    
    /** Creates a new instance of SubmissionManager */
    public SubmissionManager(IFramework framework, ClusterRepository repository) {
        this.framework = framework;
        this.repository = repository;
        parser = new RepositoryConfigParser();        
    }
    
    /** Initiates and completes a cluster submission.  Returns false IF submission
     * is KNOWN to have failed, else returns true
     */
    public boolean submit(Cluster cluster) {
        boolean submit = false;
        
        // check that parser has a root element and rep. info., else return false
        if(!parser.parseSubmissionConfigFile()) {
            // report error
            JOptionPane.showMessageDialog(this.framework.getFrame(), "Error parsing gene cluster submission configuration file, \n"+
            "\"archive_submission_config.xml\"", "Submission Configuration File", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // present archive selection dialog
        ClusterArchiveSelectionDialog dialog = new ClusterArchiveSelectionDialog(parser);
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
        // get the repository Element for the selected name            
            String repName = dialog.getSelectedRepositoryName(); 
            
                    // get the submitter implemenation class name        
            String implClassName = parser.getRepositorySubmissionClass(repName);
            
            try {
                // instantiate the submitter                       
                IClusterSubmitter submitter = (IClusterSubmitter)(this.getClass().forName(implClassName).newInstance());
                submit = submitter.submit(cluster, framework, parser);
            } catch ( Exception e) {
                
            }
        }
        
        return submit;        
    }

}
