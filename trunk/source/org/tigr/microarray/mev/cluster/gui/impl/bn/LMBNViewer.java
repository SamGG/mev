package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class LMBNViewer extends ViewerAdapter {

	protected static final String SHOW_CYTO_WEBSTART = "webstart-cytoscape-cmd";
    protected static final String SHOW_CYTO_GAGGLE = "broadcast-cytoscape-cmd";
    
    protected EvtListener listener;
    protected JPopupMenu popup;
    
	private JComponent header;
    private JTextPane  content;
    
    private JLabel label;
    
    public static Process runtimeProc;
    
    private Vector<String> networkFiles = new Vector<String>();
    
    public LMBNViewer(Vector files) {    
    	listener = new EvtListener();
        header  = createHeader();
        content = createContent(files);
        networkFiles = files;
        setMaxWidth(content, header);
        this.popup = createJPopupMenu(listener);
    }
    
    /**
     * Constructs a <code>LEMInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public LMBNViewer(String lmFile, String bnFile) {    
    	listener = new EvtListener();
        header  = createHeader();
        content = createContent(lmFile, bnFile);
        networkFiles.add(lmFile);
        networkFiles.add(bnFile);
        setMaxWidth(content, header);
        this.popup = createJPopupMenu(listener);
    }
        
    public LMBNViewer(JComponent content, JComponent header){
    	listener = new EvtListener();
    	this.content = (JTextPane)content;
    	this.header = header;
    	setMaxWidth(content, header);
    	this.popup = createJPopupMenu(listener);
    }
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
    	//return label;
    	return content;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
        return header;
    }
    
    /**
     * Returns the viewer popup menu.
     */
    public JPopupMenu getJPopupMenu() {
        return popup;
    } 
    /**
     * Creates the viewer header.
     */
    private JComponent createHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' color='#000080'><h1>LM/BN Network File<h1></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextPane createContent(String lmFile, String bnFile) {

    	JTextPane area = new JTextPane();
        area.setContentType("text/html");
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        Font font = new Font("Serif", Font.PLAIN, 10);
        area.setFont(font);
 
        String text = "<html><body><font face=\"sanserif\" color='#000080'>";

        text += "<h2>Network Files Created</h2><br><br>";
        text += "<b>LM Network File:     </b>"+lmFile+"<br><br>"; 
        text += "<b>BN Network File:     </b>"+bnFile+"<br>";
        text += "</font></body></html>";
        
        area.setText(text);
        area.setCaretPosition(0);
        
        label = new JLabel(text);
        return area;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextPane createContent(Vector files) {

    	JTextPane area = new JTextPane();
        area.setContentType("text/html");
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        Font font = new Font("Serif", Font.PLAIN, 10);
        area.setFont(font);
 
        String text = "<html><body><font face=\"sanserif\" color='#000080'>";
        
        for(int i=0; i < files.size(); i++) {
	        text += "<h2>Network Files Created</h2><br><br>";
	        text += "<b>Network File:     </b>"+files.get(i)+"<br><br>"; 
	        //text += "<b>BN Network File:     </b>"+bnFile+"<br>";
        }
        text += "</font></body></html>";
        
        area.setText(text);
        area.setCaretPosition(0);
        
        label = new JLabel(text);
        return area;
    }
    
    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
        int c_width = content.getPreferredSize().width;
        int h_width = header.getPreferredSize().width;
        if (c_width > h_width) {
            header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
        } else {
            content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
        }
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }

    /**
     * 
     */
    public static synchronized void onWebstartCystoscape(Vector netFiles) {
    	//String codeBase = "'http://www.wikipathways.org//wpi/bin/cytoscape/'";
    	String codeBase = "'http://www.cytoscape.org/tut/webstart/'";
    	String jnlpLoc = createCytoscapeJNLP(codeBase, netFiles);
    	String jnlpURI = TMEV.getDataPath() + File.separator + BNConstants.RESULT_DIR + File.separator + BNConstants.CYTOSCAPE_URI;
    	
    	try {
            BufferedWriter out = new BufferedWriter(new FileWriter(jnlpURI));
            out.write(jnlpLoc);
            out.close();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(new JFrame(), "Error creating jnlp file");
        }
        
    	//JOptionPane.showMessageDialog( new JFrame(), jnlpLoc, "Popup", JOptionPane.PLAIN_MESSAGE );
        startCytoscape(jnlpURI);
    }
    
    /**
     * 
     */
    private static void startCytoscape(String jnlpURI) {
        String command = System.getProperty("java.home");
        System.out.println("Java Home: " + command);
        command += File.separator +  "bin" + File.separator + "javaws " + jnlpURI;
        try {
        	runtimeProc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            System.out.println("Failed to start Cytoscape!");
            e.printStackTrace();
        }
    }
    /**
     * 
     */
    private static String createCytoscapeJNLP2(String codeBase, Vector<String> files) {
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

    private static String createCytoscapeJNLP(String codeBase, Vector<String> files) {
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
	public void onBroadcastToCystoscape() {
    	JOptionPane.showMessageDialog( new JFrame(), "Cytoscape Popup", "Popup", JOptionPane.PLAIN_MESSAGE );
    }
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(EvtListener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    /**
     * Adds menu items to the specified popup menu.
     */
    protected void addMenuItems(JPopupMenu menu, EvtListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Webstart Cytoscape", GUIFactory.getIcon("new16.gif"));
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_CYTO_WEBSTART);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast to Cytoscape", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_CYTO_GAGGLE);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    /**
     * Returns a menu item by specified action command.
     * @return null, if menu item was not found.
     */
    protected JMenuItem getJMenuItem(String command) {
        Component[] components = popup.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }
    
    /**
     * Sets menu enabled flag.
     */
    protected void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }
    
    /**
     * The class to listen to mouse, action.
     */
    private class EvtListener extends MouseAdapter implements ActionListener, java.io.Serializable {
        
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals(SHOW_CYTO_WEBSTART)){
                onWebstartCystoscape(networkFiles);
            } else if(command.equals(SHOW_CYTO_GAGGLE)) {
                onBroadcastToCystoscape();
            } 
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            //maybeShowPopup(event);
            if (SwingUtilities.isRightMouseButton(event)) {
            	 maybeShowPopup(event);
            }
            //deselect(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            setEnableMenuItem(SHOW_CYTO_WEBSTART, 0 >= 0);
            setEnableMenuItem(SHOW_CYTO_GAGGLE, 0 >= 0);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
