/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;

/**
 * Webstart utility file for Cystoscape
 * @author Raktim
 *
 */
public class CytoscapeWebstart {

	public static Process runtimeProc;
	
	/**
     * Public static funtion invokes webstart and creates the jnlp file
     */
    public static void onWebstartCytoscape(Vector netFiles) {
    	//TODO
    	//Read code base and lib path from property file
    	String codeBase = ""; //"'http://www.cytoscape.org/tut/webstart/'";
    	String libDir = "";
    	
    	if(BNConstants.isSetCytoscapeParams()) {
    		codeBase = BNConstants.getCodeBaseLocation();
    		libDir = BNConstants.getLibDirLocation();
    	} else {
    		Hashtable repInfo = BNDownloadManager.getRepositoryInfoCytoscape();
        	codeBase = ((String)repInfo.get("cytoscape_webstart")).trim();
        	libDir = ((String)repInfo.get("cytoscape_lib_dir")).trim();
    	}
    		
    	if(codeBase == null || libDir == null) {
    		JOptionPane.showMessageDialog(new JFrame(), "Error reading properties file, will try with default values", "Cytoscape may not launch", JOptionPane.ERROR_MESSAGE);
    		codeBase = "gaggle.systemsbiology.net/2007-04/cy/blankSlate/cy2.6.0";
    		libDir = "/2007-04/jars_cy2.6.0/";
    	}
    	
    	String jnlpLoc = createGaggleCytoscapeJNLP(codeBase, libDir, netFiles);
    	// Figure out the location of the file location from the netFiles
    	String filePath = (String)(netFiles.get(0));
    	int index = filePath.indexOf(BNConstants.RESULT_DIR);
    	String fileLoc = filePath.substring(0, index-1);
    	//String jnlpURI = TMEV.getDataPath() + File.separator + BNConstants.RESULT_DIR + File.separator + BNConstants.CYTOSCAPE_URI;
    	String jnlpURI = fileLoc + File.separator + BNConstants.RESULT_DIR + File.separator + BNConstants.CYTOSCAPE_URI;
    	System.out.println("jnlpURI: " + jnlpURI);
    	
    	try {
            BufferedWriter out = new BufferedWriter(new FileWriter(jnlpURI));
            out.write(jnlpLoc);
            out.close();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(new JFrame(), "Error creating jnlp file", "Cytoscape will not launch", JOptionPane.ERROR_MESSAGE);
        }
        
    	//JOptionPane.showMessageDialog( new JFrame(), jnlpLoc, "Popup", JOptionPane.PLAIN_MESSAGE );
        startCytoscape(jnlpURI);
    }
    
    /**
     * private Static function to fork Java Webstart based on a JNLP file
     */
    private static void startCytoscape(String jnlpURI) {
        String command = System.getProperty("java.home");
        System.out.println("Java Home: " + command);
        command += File.separator +  "bin" + File.separator + "javaws " + jnlpURI;
        try {
        	runtimeProc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(new JFrame(), "Error launching Cytoscape", "Webstart Could not launch properly", JOptionPane.ERROR_MESSAGE);
            System.out.println("Failed to start Cytoscape!");
            e.printStackTrace();
        }
    }
    
    /**
     * JNLP file created from Gagggle code Base
     * @param codeBase
     * @param libDir
     * @param files
     * @return
     */
    private static String createGaggleCytoscapeJNLP(String codeBase, String libDir, Vector<String> files) {
    	String xml = "";
    	xml = "<?xml version='1.0' encoding='utf-8'?>";
    	xml += "<jnlp";
    	xml += "  codebase='http://" + codeBase + "'>";
    	xml += "  <information>";
    	xml += "    <title>Cytoscape 2.6.0 - Blank Slate</title>";
    	xml += "    <vendor> ISB (2007-04)</vendor>";
    	xml += "    <homepage href='docs/help.html'/>";
    	xml += "    <offline-allowed/>";
    	xml += "	<icon href='http://gaggle.systemsbiology.net/images/icons/gaggle_icon.gif'/><icon kind='splash' href='http://gaggle.systemsbiology.net/images/icons/gaggle_splash.gif'/>";
    	xml += "  </information>";
    	xml += "  <security>";
    	xml += "      <all-permissions/>";
    	xml += "  </security>";
    	xml += "  <resources>";
    	xml += "	<j2se version='1.5+' max-heap-size='1024M' />";
    	xml += "	<jar href='" + libDir + "cytoscape.jar'/>";
    	xml += "	<jar href='" + libDir + "FastInfoset.jar'/>";
    	xml += "	<jar href='" + libDir + "activation.jar'/>";
    	xml += "	<jar href='" + libDir + "biojava-1.4.jar'/>";
    	xml += "	<jar href='" + libDir + "colt.jar'/>";
    	xml += "	<jar href='" + libDir + "coltginy.jar'/>";
    	xml += "	<jar href='" + libDir + "com-nerius-math-xform.jar'/>";
    	xml += "	<jar href='" + libDir + "commons-cli-1.x-cytoscape-custom.jar'/>";
    	xml += "	<jar href='" + libDir + "concurrent.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-cruft-obo.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-geom-rtree.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-geom-spacial.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-graph-dynamic.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-graph-fixed.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-render-export.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-render-immed.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-render-stateful.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-task.jar'/>";
    	xml += "	<jar href='" + libDir + "cytoscape-util-intr.jar'/>";
    	xml += "	<jar href='" + libDir + "ding.jar'/>";
    	xml += "	<jar href='" + libDir + "fing.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-export-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-graphics2d-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-graphicsio-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-graphicsio-java-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-graphicsio-ps-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-graphicsio-svg-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-io-2.0.2.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-jas-plotter-2.2.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-swing-2.0.3.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-util-2.0.2.jar'/>";
    	xml += "	<jar href='" + libDir + "freehep-xml-2.1.1.jar'/>";
    	xml += "	<jar href='" + libDir + "giny.jar'/>";
    	xml += "	<jar href='" + libDir + "glf.jar'/>";
    	xml += "	<jar href='" + libDir + "http.jar'/>";
    	xml += "	<jar href='" + libDir + "i4jruntime.jar'/>";
    	xml += "	<jar href='" + libDir + "itext-2.0.4.jar'/>";
    	xml += "	<jar href='" + libDir + "jaxb-api.jar'/>";
    	xml += "	<jar href='" + libDir + "jaxb-impl.jar'/>";
    	xml += "	<jar href='" + libDir + "jaxws-api.jar'/>";
    	xml += "	<jar href='" + libDir + "jaxws-rt.jar'/>";
    	xml += "	<jar href='" + libDir + "jaxws-tools.jar'/>";
    	xml += "	<jar href='" + libDir + "jdom-1.0.jar'/>";
    	xml += "	<jar href='" + libDir + "jhall.jar'/>";
    	xml += "	<jar href='" + libDir + "jnlp.jar'/>";
    	xml += "	<jar href='" + libDir + "jsr173_1.0_api.jar'/>";
    	xml += "	<jar href='" + libDir + "jsr181-api.jar'/>";
    	xml += "	<jar href='" + libDir + "jsr250-api.jar'/>";
    	xml += "	<jar href='" + libDir + "junit.jar'/>";
    	xml += "	<jar href='" + libDir + "l2fprod-common-all.jar'/>";
    	xml += "	<jar href='" + libDir + "looks-2.1.4.jar'/>";
    	xml += "	<jar href='" + libDir + "phoebe.jar'/>";
    	xml += "	<jar href='" + libDir + "piccolo.jar'/>";
    	xml += "	<jar href='" + libDir + "resolver.jar'/>";
    	xml += "	<jar href='" + libDir + "saaj-api.jar'/>";
    	xml += "	<jar href='" + libDir + "saaj-impl.jar'/>";
    	xml += "	<jar href='" + libDir + "sjsxp.jar'/>";
    	xml += "	<jar href='" + libDir + "stax-ex.jar'/>";
    	xml += "	<jar href='" + libDir + "streambuffer.jar'/>";
    	xml += "	<jar href='" + libDir + "swing-layout-1.0.1.jar'/>";
    	xml += "	<jar href='" + libDir + "swingx-2006_10_27.jar'/>";
    	xml += "	<jar href='" + libDir + "tclib.jar'/>";
    	xml += "	<jar href='" + libDir + "undo.support.jar'/>";
    	xml += "	<jar href='" + libDir + "violinstrings-1.0.2.jar'/>";
    	xml += "	<jar href='" + libDir + "wizard.jar'/>";

    	xml += "	<jar href='" + libDir + "plugins/AutomaticLayout.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/CyGoose.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/CytoscapeEditor.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/GraphMerge.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/ManualLayout.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/SBMLReader.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/TableImport.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/biopax.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/browser.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/cPath.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/cpath2.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/filter.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/filters.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/linkout.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/psi_mi.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/quick_find.jar'/>";
    	xml += "	<jar href='" + libDir + "plugins/yLayouts.jar'/>";	
    		
    	xml += " </resources>";
    	xml += "  <application-desc main-class='cytoscape.CyMain'>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.layout.LayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.mskcc.biopax_plugin.plugin.BioPaxPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>browser.AttributeBrowserPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.cpath.plugin.CPathPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.editor.CytoscapeEditorPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>filter.cytoscape.CsFilter</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.filters.FilterPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>GraphMerge.GraphMerge</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>linkout.LinkOutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>ManualLayout.ManualLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.psi_mi.plugin.PsiMiPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.quickfind.plugin.QuickFindPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>sbmlreader.SBMLReaderPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>edu.ucsd.bioeng.coreplugin.tableImport.TableImportPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>yfiles.YFilesLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.systemsbiology.cytoscape.GagglePlugin</argument>";
    	//xml += "    <argument>-V</argument>";
    	//xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/plugins/vizmap.props</argument>";
    	//xml += "    <argument>-N</argument>";
    	//xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/data/BN_RnaI/results/May_27_08_22_55_27_343TabuSearch_BAYES_boot_result_4_0.7.sif</argument>";
    	for(int i=0; i < files.size(); i++) {
    		xml += "<argument>-N</argument>";
        	xml += "<argument>file:///" + files.get(i).replace("\\", "/") + "</argument>";
    	}
    	xml += "  </application-desc>";
    	xml += "</jnlp>";
    	return xml;
    }
    
    /**
     * JNLP file created from Gagggle code Base
     * @param codeBase
     * @param files
     * @return
     */
    private static String createGaggleCytoscapeJNLP(String codeBase, Vector<String> files) {
    	String xml = "";
    	xml = "<?xml version='1.0' encoding='utf-8'?>";
    	xml += "<jnlp";
    	xml += "  codebase='http://gaggle.systemsbiology.net/2007-04/cy/blankSlate/cy2.6.0'>";
    	xml += "  <information>";
    	xml += "    <title>Cytoscape 2.6.0 - Blank Slate</title>";
    	xml += "    <vendor> ISB (2007-04)</vendor>";
    	xml += "    <homepage href='docs/help.html'/>";
    	xml += "    <offline-allowed/>";
    	xml += "	<icon href='http://gaggle.systemsbiology.net/images/icons/gaggle_icon.gif'/><icon kind='splash' href='http://gaggle.systemsbiology.net/images/icons/gaggle_splash.gif'/>";
    	xml += "  </information>";
    	xml += "  <security>";
    	xml += "      <all-permissions/>";
    	xml += "  </security>";
    	xml += "  <resources>";
    	xml += "	<j2se version='1.5+' max-heap-size='1024M' />";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/FastInfoset.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/activation.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/biojava-1.4.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/colt.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/coltginy.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/com-nerius-math-xform.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/commons-cli-1.x-cytoscape-custom.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/concurrent.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-cruft-obo.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-geom-rtree.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-geom-spacial.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-graph-dynamic.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-graph-fixed.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-render-export.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-render-immed.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-render-stateful.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-task.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/cytoscape-util-intr.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/ding.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/fing.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-export-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-graphics2d-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-graphicsio-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-graphicsio-java-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-graphicsio-ps-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-graphicsio-svg-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-io-2.0.2.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-jas-plotter-2.2.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-swing-2.0.3.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-util-2.0.2.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/freehep-xml-2.1.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/giny.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/glf.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/http.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/i4jruntime.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/itext-2.0.4.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jaxb-api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jaxb-impl.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jaxws-api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jaxws-rt.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jaxws-tools.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jdom-1.0.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jhall.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jnlp.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jsr173_1.0_api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jsr181-api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/jsr250-api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/junit.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/l2fprod-common-all.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/looks-2.1.4.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/phoebe.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/piccolo.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/resolver.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/saaj-api.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/saaj-impl.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/sjsxp.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/stax-ex.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/streambuffer.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/swing-layout-1.0.1.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/swingx-2006_10_27.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/tclib.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/undo.support.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/violinstrings-1.0.2.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/wizard.jar'/>";

    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/AutomaticLayout.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/CyGoose.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/CytoscapeEditor.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/GraphMerge.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/ManualLayout.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/SBMLReader.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/TableImport.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/biopax.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/browser.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/cPath.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/cpath2.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/filter.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/filters.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/linkout.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/psi_mi.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/quick_find.jar'/>";
    	xml += "	<jar href='/2007-04/jars_cy2.6.0/plugins/yLayouts.jar'/>";	
    		
    	xml += " </resources>";
    	xml += "  <application-desc main-class='cytoscape.CyMain'>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.layout.LayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.mskcc.biopax_plugin.plugin.BioPaxPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>browser.AttributeBrowserPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.cpath.plugin.CPathPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.editor.CytoscapeEditorPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>filter.cytoscape.CsFilter</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.filters.FilterPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>GraphMerge.GraphMerge</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>linkout.LinkOutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>ManualLayout.ManualLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.psi_mi.plugin.PsiMiPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.quickfind.plugin.QuickFindPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>sbmlreader.SBMLReaderPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>edu.ucsd.bioeng.coreplugin.tableImport.TableImportPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>yfiles.YFilesLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.systemsbiology.cytoscape.GagglePlugin</argument>";
    	//xml += "    <argument>-V</argument>";
    	//xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/plugins/vizmap.props</argument>";
    	//xml += "    <argument>-N</argument>";
    	//xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/data/BN_RnaI/results/May_27_08_22_55_27_343TabuSearch_BAYES_boot_result_4_0.7.sif</argument>";
    	for(int i=0; i < files.size(); i++) {
    		xml += "<argument>-N</argument>";
        	xml += "<argument>file:///" + files.get(i).replace("\\", "/") + "</argument>";
    	}
    	xml += "  </application-desc>";
    	xml += "</jnlp>";
    	return xml;
    }
    
    /**
     * Jnlp file created from Wiki jar - Unused
     * @param codeBase
     * @param files
     * @return
     */
    private static String createWikiCytoscapeJNLP(String codeBase, Vector<String> files) {
    	String xml = "";
    	xml = "<?xml version='1.0' encoding='UTF-8'?>";
    	xml += "<jnlp codebase='http://www.wikipathways.org//wpi/bin/cytoscape/'>";
    	xml += "  <security>";
    	xml += "    <all-permissions />";
    	xml += "  </security>";
    	xml += "  <information>";
    	xml += "    <title>Cytoscape Webstart</title>";
    	xml += "    <vendor>Cytoscape Collaboration</vendor>";
    	xml += "    <homepage href='http://cytoscape.org' />";
    	xml += "    <offline-allowed />";
    	xml += "  </information>";
    	xml += "  <resources>";
    	xml += "    <j2se version='1.5+' max-heap-size='1024M' />";
    	xml += "    <!--All lib jars that cytoscape requires to run should be in this list-->";
    	xml += "    <jar href='cytoscape.jar' />";
    	xml += "    <jar href='lib/jnlp.jar' />";
    	xml += "    <jar href='lib/commons-cli-1.x-cytoscape-custom.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-ps-2.0.jar' />";
    	xml += "    <jar href='lib/wizard.jar' />";
    	xml += "    <jar href='lib/piccolo.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-pdf-2.0.jar' />";
    	xml += "    <jar href='lib/cytoscape-util-intr.jar' />";
    	xml += "    <jar href='lib/coltginy.jar' />";
    	xml += "    <jar href='lib/looks-2.1.4.jar' />";
    	xml += "    <jar href='lib/violinstrings-1.0.2.jar' />";
    	xml += "    <jar href='lib/biojava-1.4.jar' />";
    	xml += "    <jar href='lib/cytoscape-graph-dynamic.jar' />";
    	xml += "    <jar href='lib/jaxb-impl.jar' />";
    	xml += "    <jar href='lib/tclib.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-svg-2.0.jar' />";
    	xml += "    <jar href='lib/colt.jar' />";
    	xml += "    <jar href='lib/freehep-swing-2.0.2.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-export.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-immed.jar' />";
    	xml += "    <jar href='lib/fing.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-stateful.jar' />";
    	xml += "    <jar href='lib/cytoscape-geom-rtree.jar' />";
    	xml += "    <jar href='lib/junit.jar' />";
    	xml += "    <jar href='lib/freehep-xml-2.0.1.jar' />";
    	xml += "    <jar href='lib/ding.jar' />";
    	xml += "    <jar href='lib/freehep-export-2.0.3.jar' />";
    	xml += "    <jar href='lib/swingx-2006_10_27.jar' />";
    	xml += "    <jar href='lib/cytoscape-cruft-obo.jar' />";
    	xml += "    <jar href='lib/itext-2.0.4.jar' />";
    	xml += "    <jar href='lib/com-nerius-math-xform.jar' />";
    	xml += "    <jar href='lib/swing-layout-1.0.1.jar' />";
    	xml += "    <jar href='lib/undo.support.jar' />";
    	xml += "    <jar href='lib/giny.jar' />";
    	xml += "    <jar href='lib/jsr173_1.0_api.jar' />";
    	xml += "    <jar href='lib/activation.jar' />";
    	xml += "    <jar href='lib/cytoscape-task.jar' />";
    	xml += "    <jar href='lib/jhall.jar' />";
    	xml += "    <jar href='lib/jdom-1.0.jar' />";
    	xml += "    <jar href='lib/jaxb-api.jar' />";
    	xml += "    <jar href='lib/freehep-graphics2d-2.0.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-swf-2.0.jar' />";
    	xml += "    <jar href='lib/cytoscape-geom-spacial.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-emf-2.0.jar' />";
    	xml += "    <jar href='lib/freehep-util-2.0.1.jar' />";
    	xml += "    <jar href='lib/freehep-io-2.0.1.jar' />";
    	xml += "    <jar href='lib/phoebe.jar' />";
    	xml += "    <jar href='lib/cytoscape-graph-fixed.jar' />";
    	xml += "    <jar href='lib/concurrent.jar' />";
    	xml += "    <jar href='lib/glf.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-2.0.jar' />";
    	xml += "    <jar href='lib/l2fprod-common-all.jar' />";
    	xml += "    <!--These are the plugins you wish to load, edit as necessary.-->";
    	xml += "    <jar href='plugins/ManualLayout.jar' />";
    	xml += "    <jar href='plugins/SBMLReader.jar' />";
    	xml += "    <jar href='plugins/psi_mi.jar' />";
    	xml += "    <jar href='plugins/TableImport.jar' />";
    	xml += "    <jar href='plugins/exesto.jar' />";
    	xml += "    <jar href='plugins/linkout.jar' />";
    	xml += "    <jar href='plugins/CytoscapeEditor.jar' />";
    	xml += "    <jar href='plugins/GraphMerge.jar' />";
    	xml += "    <jar href='plugins/yLayouts.jar' />";
    	xml += "    <jar href='plugins/quick_find.jar' />";
    	xml += "    <jar href='plugins/cPath.jar' />";
    	xml += "    <jar href='plugins/browser.jar' />";
    	xml += "    <jar href='plugins/filter.jar' />";
    	xml += "    <jar href='plugins/AutomaticLayout.jar' />";
    	xml += "    <jar href='plugins/biopax.jar' />";
    	xml += "    <jar href='plugins/filters.jar' />";
    	xml += "    <jar href='plugins/gpml.jar' />";
    	xml += "    <jar href='plugins/NamedSelection.jar' />";
    	xml += "    <jar href='plugins/BubbleRouter.jar' />";
    	xml += "  </resources>";
    	xml += "  <!--This starts-up Cytoscape, specify your plugins to load, and other command line arguments.  Plugins not specified here will not be loaded.-->";
    	xml += "  <application-desc main-class='cytoscape.CyMain'>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>ManualLayout.ManualLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>sbmlreader.SBMLReaderPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.psi_mi.plugin.PsiMiPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>edu.ucsd.bioeng.coreplugin.tableImport.TableImportPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>linkout.LinkOutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.editor.CytoscapeEditorPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>GraphMerge.GraphMerge</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>yfiles.YFilesLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.quickfind.plugin.QuickFindPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.cpath.plugin.CPathPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>browser.AttributeBrowserPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>filter.cytoscape.CsFilter</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.layout.LayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.mskcc.biopax_plugin.plugin.BioPaxPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.filters.FilterPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>gpml.GpmlPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>namedSelection.NamedSelection</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.bubbleRouter.BubbleRouterPlugin</argument>";
    	//xml += "   <argument>-N</argument>";
    	//xml += "<argument>http://www.wikipathways.org/images/9/9d/Hs_Cell_cycle_KEGG.gpml</argument>";
    	for(int i=0; i < files.size(); i++) {
    	    		xml += "<argument>-N</argument>";
    	        	xml += "<argument>file:///" + files.get(i).replace("\\", "/") + "</argument>";
    	    	}
    	xml += "    <argument>-V</argument>";
    	xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/plugins/vizmap.props</argument>";
    	xml += "  </application-desc>";
    	xml += "</jnlp>";
    	return xml;
	}
    
    /**
     * JNLP file created from Sun Example Jar - UNused
     * @param codeBase
     * @param files
     * @return
     */
    private static String createSunCytoscapeJNLP(String codeBase, Vector<String> files) {
    	String xml = "";
    	xml = "<?xml version='1.0' encoding='UTF-8'?>";
    	xml += "<!-- Cytoscape 2.4 -->";
    	xml += "<jnlp codebase='http://www.cytoscape.org/tut/webstart'>";
    	xml += "<security>";
    	xml += "    <all-permissions />";
    	xml += "  </security>";
    	xml += "  <information>";
    	xml += "    <title>Cytoscape v2.4 Webstart</title>";
    	xml += "    <vendor>Cytoscape Collaboration</vendor>";
    	xml += "    <homepage href='http://cytoscape.org' />";
    	xml += "    <offline-allowed />";
    	xml += "  </information>";
    	xml += "  <resources>";
    	xml += "    <j2se version='1.5+' max-heap-size='1024M' />";
    	xml += "    <!--All lib jars that cytoscape requires to run should be in this list-->";
    	xml += "    <jar href='cytoscape.jar' />";
    	xml += "    <jar href='lib/activation.jar' />";
    	xml += "    <jar href='lib/biojava-1.4.jar' />";
    	xml += "    <jar href='lib/colt.jar' />";
    	xml += "    <jar href='lib/coltginy.jar' />";
    	xml += "    <jar href='lib/com-nerius-math-xform.jar' />";
    	xml += "    <jar href='lib/commons-cli-1.x-cytoscape-custom.jar' />";
    	xml += "    <jar href='lib/concurrent.jar' />";
    	xml += "    <jar href='lib/cytoscape-cruft-obo.jar' />";
    	xml += "    <jar href='lib/cytoscape-geom-rtree.jar' />";
    	xml += "    <jar href='lib/cytoscape-geom-spacial.jar' />";
    	xml += "    <jar href='lib/cytoscape-graph-dynamic.jar' />";
    	xml += "    <jar href='lib/cytoscape-graph-fixed.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-export.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-immed.jar' />";
    	xml += "    <jar href='lib/cytoscape-render-stateful.jar' />";
    	xml += "    <jar href='lib/cytoscape-task.jar' />";
    	xml += "    <jar href='lib/cytoscape-util-intr.jar' />";
    	xml += "    <jar href='lib/ding.jar' />";
    	xml += "    <jar href='lib/fing.jar' />";
    	xml += "    <jar href='lib/freehep-base.jar' />";
    	xml += "    <jar href='lib/freehep-graphics2d.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-gif.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-pdf.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-ps.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-svg.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio-swf.jar' />";
    	xml += "    <jar href='lib/freehep-graphicsio.jar' />";
    	xml += "    <jar href='lib/giny.jar' />";
    	xml += "    <jar href='lib/glf.jar' />";
    	xml += "    <jar href='lib/jaxb-api.jar' />";
    	xml += "    <jar href='lib/jaxb-impl.jar' />";
    	xml += "    <jar href='lib/jdom.jar' />";
    	xml += "    <jar href='lib/jhall.jar' />";
    	xml += "    <jar href='lib/jnlp.jar' />";
    	xml += "    <jar href='lib/jsr173_1.0_api.jar' />";
    	xml += "    <jar href='lib/junit.jar' />";
    	xml += "    <jar href='lib/looks-1.1.3.jar' />";
    	xml += "    <jar href='lib/phoebe.jar' />";
    	xml += "    <jar href='lib/piccolo.jar' />";
    	xml += "    <jar href='lib/piccolox.jar' />";
    	xml += "    <jar href='lib/swing-layout-1.0.1.jar' />";
    	xml += "    <jar href='lib/tclib.jar' />";
    	xml += "    <jar href='lib/violinstrings-1.0.2.jar' />";
    	xml += "    <jar href='lib/wizard.jar' />";
    	xml += "    <jar href='lib/xercesImpl.jar' />";
    	xml += "    <!--These are the plugins you wish to load, edit as necessary.-->";
    	xml += "    <jar href='plugins/AutomaticLayout.jar' />";
    	xml += "    <jar href='plugins/biopax.jar' />";
    	xml += "    <jar href='plugins/browser.jar' />";
    	xml += "    <jar href='plugins/cPath.jar' />";
    	xml += "    <jar href='plugins/CytoscapeEditor.jar' />";
    	xml += "    <jar href='plugins/exesto.jar' />";
    	xml += "    <jar href='plugins/filter.jar' />";
    	xml += "    <jar href='plugins/GraphMerge.jar' />";
    	xml += "    <jar href='plugins/linkout.jar' />";
    	xml += "    <jar href='plugins/ManualLayout.jar' />";
    	xml += "    <jar href='plugins/psi_mi.jar' />";
    	xml += "    <jar href='plugins/quick_find.jar' />";
    	xml += "    <jar href='plugins/SBMLReader.jar' />";
    	xml += "    <jar href='plugins/TableImport.jar' />";
    	xml += "    <jar href='plugins/yeast-context.jar' />";
    	xml += "    <jar href='plugins/yLayouts.jar' />";
    	xml += "  </resources>";
    	xml += "  <!--This starts-up Cytoscape, specify your plugins to load, and other command line arguments.  Plugins not specified here will not be loaded.-->";
    	xml += "  <application-desc main-class='cytoscape.CyMain'>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.layout.LayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.mskcc.biopax_plugin.plugin.BioPaxPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>browser.AttributeBrowserPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.cpath.plugin.CPathPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>cytoscape.editor.CytoscapeEditorPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>filter.cytoscape.CsFilter</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>GraphMerge.GraphMerge</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>linkout.LinkOutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>ManualLayout.ManualLayoutPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>org.cytoscape.coreplugin.psi_mi.plugin.PsiMiPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>csplugins.quickfind.plugin.QuickFindPlugIn</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>sbmlreader.SBMLReaderPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>edu.ucsd.bioeng.coreplugin.tableImport.TableImportPlugin</argument>";
    	xml += "    <argument>-p</argument>";
    	xml += "    <argument>yfiles.YFilesLayoutPlugin</argument>";
    	xml += "    <argument>-V</argument>";
    	xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/plugins/vizmap.props</argument>";
    	//xml += "    <argument>-N</argument>";
    	//xml += "    <argument>file:///C:/cscie75/Projects/MeV/MeV_SVN/data/BN_RnaI/results/May_27_08_22_55_27_343TabuSearch_BAYES_boot_result_4_0.7.sif</argument>";
    	for(int i=0; i < files.size(); i++) {
    		xml += "<argument>-N</argument>";
        	xml += "<argument>file:///" + files.get(i).replace("\\", "/") + "</argument>";
    	}
    	xml += "  </application-desc>";
    	xml += "</jnlp>";
    	return xml;
    }
}
