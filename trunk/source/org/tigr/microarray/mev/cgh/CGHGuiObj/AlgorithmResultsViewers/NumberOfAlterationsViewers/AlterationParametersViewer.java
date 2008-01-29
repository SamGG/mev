/*
 * AlterationParametersViewer.java
 *
 * Created on May 19, 2003, 5:26 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class AlterationParametersViewer extends ViewerAdapter{

    JTextArea txtResults;

    public AlterationParametersViewer(String text){
        populateResultsText(text);
    }

    /** Creates a new instance of AlterationParametersViewer */
    public AlterationParametersViewer(IFramework framework) {
        populateResultsText(framework);
    }

    private void populateResultsText(IFramework framework){
        txtResults = new JTextArea(50, 20);
        txtResults.setEditable(false);
        StringBuffer sb = new StringBuffer();

        ICGHCloneValueMenu menu = framework.getCghCloneValueMenu();
        //int copyNumberDeterminationType = menu.getCopyNumberDeterminationType();

        //if(copyNumberDeterminationType == ICGHDisplayMenu.COPY_DETERMINATION_BY_THRESHOLD){
            sb.append("Determination Type:  Thresholds\n");
            sb.append("Amplification Threshold " + menu.getAmpThresh() + "\n");
            sb.append("Deletion Threshold " + menu.getDelThresh() + "\n");
            sb.append("Amplification 2 Copy Threshold " + menu.getAmpThresh2Copy() + "\n");
            sb.append("Deletion 2 Copy Threshold " + menu.getDelThresh2Copy() + "\n");

        //}else{
        //    sb.append("Determination Type:  MixtureModel");
        //}

        txtResults.setText(sb.toString());
        txtResults.setCaretPosition(0);

    }

    private void populateResultsText(String text){
        txtResults = new JTextArea(50, 20);
        txtResults.setEditable(false);
        txtResults.setText(text);
        txtResults.setCaretPosition(0);
    }

    public JComponent getContentComponent() {
        return txtResults;
    }

}
