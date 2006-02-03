/*
 * CGHAlgorithmFactory.java
 *
 * Created on May 19, 2003, 2:53 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;
import org.tigr.microarray.mev.cluster.gui.IGUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHAlgorithmFactory implements IGUIFactory {

    String[] names = {"CloneAmplifications", "CloneDeletions", "CloneDeletions2Copy", "CloneAmplifications2Copy",  "RegionAmplifications", "RegionDeletions",
        "GeneAmplifications", "GeneDeletions", "LoadGeneList", "CompareExperiments"};

    /** Creates a new instance of CGHAlgorithmFactory */
    public CGHAlgorithmFactory() {
    }

    /** Returns the array of analysis descriptions.
     * @see AnalysisDescription
     */
    public AnalysisDescription[] getAnalysisDescriptions() {

        String key;
        String name, clazz, tooltip;
        Icon smallIcon, largeIcon;

        ArrayList list = new ArrayList();

        for(int i = 0; i < names.length; i++){
            //String name = names[i];
            name  = names[i];
            if("CloneDeletions".equals(name) ||"CloneAmplifications".equals(name) || "CloneDeletions2Copy".equals(name) ||"CloneAmplifications2Copy".equals(name)){
                clazz = "org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.CloneAlterations." + name;
            }else if("RegionDeletions".equals(name) ||"RegionAmplifications".equals(name)){
                clazz = "org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.RegionAlterations." + name;
            }else if("GeneDeletions".equals(name) || "GeneAmplifications".equals(name)) {
                clazz = "org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations." + name;
            }else if("LoadGeneList".equals(name)){
                clazz = "org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations." + name;
            }else if("CompareExperiments".equals(name)){
                clazz = "org.tigr.microarray.mev.cgh.CGHAlgorithms.AlterationsComparator." + name;
            }else{
                clazz = null;
            }

            //tooltip = bundle.getString(key+".tooltip").trim();
            tooltip = "tooltip";
            smallIcon = getIcon("analysis16.gif");
            largeIcon = getIcon("met.gif");
            //smallIcon = getIcon(bundle.getString(key+".smallIcon").trim());
            //largeIcon = getIcon(bundle.getString(key+".largeIcon").trim());
            list.add(new AnalysisDescription(name, clazz, smallIcon, largeIcon, tooltip));
        }
        return(AnalysisDescription[])list.toArray(new AnalysisDescription[list.size()]);
    }

    public static ImageIcon getIcon(String name) {
        URL url = GUIFactory.class.getResource("/org/tigr/microarray/mev/cluster/gui/impl/images/"+name);
        if (url == null)
            return null;
        return new ImageIcon(url);
    }

}
