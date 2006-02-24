/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PaperReferencesDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-02-24 15:23:35 $
 * $Author: wwang67 $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.awt.GBA;

public class PaperReferencesDialog extends JDialog implements java.awt.print.Printable,HyperlinkListener{
    
    private String text;
    private JEditorPane ed;
    
    public PaperReferencesDialog(Frame parent) {
        this(parent, "");
    }
    
    public PaperReferencesDialog(Frame parent, String labelText) {
        super(parent, "Papers / Publications Reference", false);
        EventListener listener = new EventListener();
        this.text = labelText;
        GBA gba = new GBA();
        Font font = new Font("serif", Font.PLAIN, 12);
        
        ed = new JEditorPane("text/html", labelText);
        ed.setEditable(false);
        ed.setMargin(new Insets(10,10,10,10));
        ed.setBackground(new Color(234,233,191));
        ed.setCaretPosition(0);
        ed.addHyperlinkListener(this);
        JScrollPane scrollPane = new JScrollPane(ed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel referencesPanel = new JPanel(new GridBagLayout());
        referencesPanel.setBackground(new Color(234,233,191));
        gba.add(referencesPanel, scrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
        
        JButton printButton = new JButton("Print");
        printButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        printButton.setFocusPainted(false);
        printButton.setActionCommand("print-command");
        printButton.addActionListener(listener);
        
        JButton closeButton = new JButton("  Close  ");
        closeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        closeButton.setFocusPainted(false);
        closeButton.setActionCommand("close-command");
        closeButton.addActionListener(listener);
        closeButton.setSize(120,30);
        closeButton.setPreferredSize(new Dimension(120, 30));
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
     //   gba.add(buttonPanel, printButton, 0, 0, 1, 1, 0,0, GBA.NONE, GBA.C);
        gba.add(buttonPanel, closeButton, 0, 0, 1, 1, 1, 1, GBA.NONE, GBA.C);
        
        getContentPane().setLayout(new GridBagLayout());
        gba.add(getContentPane(), referencesPanel, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C);
        gba.add(getContentPane(), buttonPanel, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C);
        
        setSize(550, 650);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
    }
    public static String createReferencesText() {
        String html = "";
        
        html += "<html><body>";
        html += "<p><font size = +1><b>HCL</b></font> - ";
        html += "<b>Hierarchical Clustering</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=9843981&query_hl=4&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Eisen, M.B., P.T. Spellman, P.O. Brown, and D. Botstein (1998) ";
        html +="</a>";
        html += "Cluster analysis and display of genome-wide expression patterns. ";
        html += "<i>Proceedings of the National Academy of Sciences USA </i> ";
        html += "95:14863-14868.";
        
        html += "<br><p><font size = +1><b>ST</b></font> - ";
        html += "<b>Support trees (Bootstrapping)</b>";
        html += "<br>Graur, D., and W.-H. Li. 2000. ";
        html += "<i>Fundamentals of Molecular Evolution. Second Edition.</i>";
        html += "Sinauer Associates, Sunderland, MA. pp 209-210";
        
        html += "<br><p><font size = +1><b>SOTA</b></font> - ";
        
        html += "<b>Self-organizing trees</b>";
        
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11238068&query_hl=7&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Herrero J., A. Valencia, J. Dopazo (2001).";
        html +="</a>";
        html += "A hierarchical unsupervised growing neural network for clustering gene";
        html += "expression patterns.  <i>Bioinformatics</i> 17(2):126-136.";
        
        html +="<br><p><a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=9069183&query_hl=9&itool=pubmed_docsum\"";
        html +=">";
        html += "Dopazo, J., and J. M. Carazo (1997).";
        html +="</a>";
        html += "Phylogenetic reconstruction using an unsupervised growing neural network that adopts the topology ";
        html += "of a phylogenetic tree.  <i>Journal of Molecular Evolution</i> 44:226-233.";
        
        html += "<br><p><font size = +1><b>KMC</b></font> - ";
        html += "<b>K-Means</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10783168&query_hl=11&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Soukas, A., P. Cohen, N.D. Socci, and J.M. Friedman (2000) ";
        html +="</a>";
        html += "Leptin-specific patterns of gene expression in white adipose tissue. ";
        html += "<i>Genes and Development</i> 14:963-980.";
        
        html += "<br><p><font size = +1><b>SOM</b></font> - ";
        html += "<b>Self-Organizing Maps</b>";
        //html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10783168&query_hl=11&itool=pubmed_docsum\"";
        //html +=">";
        html += "<br>Kohonen, T. (1982)  ";
        //html +="</a>";
        html += "Self-organized formation of topologically correct feature maps. <i>Biological Cybernetics</i> 43:59-69";
        html +="<br><p><a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10077610&query_hl=20&itool=pubmed_docsum\"";
        html +=">";
        html += "Tamayo, P., D. Slonim, J. Masirov, Q. Zhu, S. Kitareewan, ";
        html +="</a>";
        html += "E. Dmitrovsky, E.S. Lander, and T.R. Golub (1999)  ";
        html += "Interpreting patterns of gene expression with self-organizing maps: Methods and application ";
        html += "to hematopoietic differentiation. <i>Proceedings of the National Academy of Sciences USA </i> ";
        html += "96:2907-2912.";
        
        html += "<br><p><font size = +1><b>CAST</b></font> - ";
        html += "<b>Clustering Affinity Search Technique</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10582567&query_hl=23&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Ben-Dor, A., R. Shamir, and Z. Yakhini (1999) ";
        html +="</a>";
        html += "Clustering gene expression patterns. ";
        html += "<i>Journal of Computational Biology</i> 6:281-297.";
        
        html += "<br><p><font size = +1><b>QTC</b></font> - ";
        html += "<b>Jackknife Clustering</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10568750&query_hl=26&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Heyer, L.J., S. Kruglyak, and S. Yooseph (1999) ";
        html +="</a>";
        html += "Exploring expression data: identification and analysis of coexpressed genes. ";
        html += "<i>Genome Research</i> 9:1106-1115.";
        
        html += "<br><p><font size = +1><b>GSH</b></font> - ";
        html += "<b>Gene shaving</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11178228&query_hl=28&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Hastie, T. et al. (2000).";
        html +="</a>";
        html += "'Gene shaving' as a method for identifying distinct sets of genes with similar expression patterns";
        html += "Genome Biology 1(2):research0003.1-0003.21.";
        
        html += "<br><p><font size = +1><b>FOM</b></font> - ";
        html += "<b>Figures of Merit</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11301299&query_hl=31&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Yeung, K.Y., D.R. Haynor, and W.L. Ruzzo (2001) ";
        html +="</a>";
        html += "Validating clustering for gene expression data. ";
        html += "<i>Bioinformatics</i> 17:309-318.";
        
        html += "<br><p><font size = +1><b>PCA</b></font> - ";
        html += "<b>Principal Components Analysis</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10902193&query_hl=33&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Raychaudhuri, S.,  J. M. Stuart, & R. B. Altman (2000) ";
        html +="</a>";
        html += "Principal components analysis to summarize microarray experiments: ";
        html += "application to sporulation time series. ";
        html += "<i>Pacific Symposium on Biocomputing 2000, Honolulu, Hawaii,</i> 452-463. ";
        html += "Available at http://smi-web.stanford.edu/pubs/SMI_Abstracts/SMI-1999-0804.html";
        
        html += "<br><p><font size = +1><b>COA</b></font> - ";
        html += "<b>Correspondence Analysis</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11535808&query_hl=35&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Fellenberg K. et al. (2001) ";
        html +="</a>";
        html += "Correspondence analysis applied to microarray data. ";
        html += "<i>Proceedings of the National Academy of Sciences USA</i> 98: 10781-10786";
        html +="<br><p><a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=12490444&query_hl=37&itool=pubmed_docsum\"";
        html +=">";
        html += "Culhane A.C. et al. (2002) ";
        html +="</a>";
        html += "Between-group analysis of microarray data. ";
        html += "<i>Bioinformatics</i> 18:1600-1608.";
        
        html += "<br><p><font size = +1><b>RN</b></font> - ";
        html += "<b>Relevance networks</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11027309&query_hl=39&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Butte, A.J., P. Tamayo, D. Slonim, T.R. Golub and I.S. Kohane (2000) ";
        html +="</a>";
        html += "Discovering functional relationships between RNA expression and chemotherapautic ";
        html += "susceptibility using relevance networks.";
        html += "<i>Proceedings of the National Academy of Sciences USA</i> 97: 12182-12186";
        
        html += "<br><p><font size = +1><b>PTM</b></font> - ";
        html += "<b>Template Matching</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11597334&query_hl=42&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Pavlidis, P., and W.S. Noble (2001) ";
        html +="</a>";
        html += "Analysis of strain and regional variation in gene expression in mouse brain. ";
        html += "<i>Genome Biology</i> 2:research0042.1-0042.15";
        
        html += "<br><p><font size = +1><b>SAM</b></font> - ";
        html += "<b>Significance Analysis of Microarrays</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11309499&query_hl=44&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Tusher, V.G., R. Tibshirani and G. Chu. (2001) ";
        html +="</a>";
        html += "Significance analysis of microarrays applied to the ionizing radiation response. ";
        html += "<i>Proceedings of the National Academy of Sciences USA</i> 98: 5116-5121.";
        
        html +="<br><p><a href=\"http://www-stat.stanford.edu/~tibs/SAM/\"";
        html +=">";
        html += "Chu, G., B. Narasimhan, R. Tibshirani and V. Tusher (2002).";
        html +="</a>";
        html += "SAM \"Significance Analysis of Microarrays\" Users Guide and Technical Document.";
        html += "http://www-stat.stanford.edu/~tibs/SAM/";
  
        html += "<br><p><font size = +1><b>ANOVA</b></font> - ";
        html += "<b>One-way Analysis of Variance</b>";  
        
        html += "<br>Zar, J.H. 1999. Biostatistical Analysis. 4th ed. Prentice Hall, NJ.";
        
        html += "<br><p><font size = +1><b>TTEST</b></font> - ";
        html += "<b>T-Tests</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=12016052&query_hl=48&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Pan, W. (2002).  ";
        html +="</a>";
        html += "A comparative review of statistical methods for discovering differentially expressed genes in replicated microarray experiments ";
        html += "<i>Bioinformatics</i> 18: 546-554.";
        
        html += "<br><p>Dudoit, S., Y.H. Yang, M.J. Callow, and T. Speed (2000).";
        html += "Statistical methods for identifying differentially expressed genes ";
        html += "in replicated cDNA microarray experiments. <i>Technical report 2000</i> ";
        html += "Statistics Department, University of California, Berkeley.";

        html += "<br><p>Welch, B.L. (1947).";
        html += "The generalization of ‘students’ problem when several different population variances are involved. ";
        html += "<i>Biometrika</i> 34: 28-35.";
        html +="<br><p> <a href=\"http://linus.nci.nih.gov/~brb/TechReport.htm\"";
        html +=">";
        html += "Korn, E.L., J.F. Troendle, L.M. McShane, R. Simon (2001).";
        html +="</a>";
        html += "Controlling the number of false discoveries: application to high-dimensional genomic data ";
        html += "<i> Technical report 003</i>, Biometric Research Branch, National Cancer Institute. http://linus.nci.nih.gov/~brb/TechReport.htm";           
        
        html += "<br><p>Korn, E.L., J.F. Troendle, L.M. McShane, R. Simon (2004).";
        html += "Controlling the number of false discoveries: application to high-dimensional genomic data ";
        html += "<i>Journal of Statistical Planning and Inference</i> 124: 379-398.";      

        html += "<br><p><font size = +1><b>TFA</b></font> - ";
        html += "<b>Two Factor ANOVA</b>";
        
        html += "<br>Keppel, G., and S. Zedeck. (1989). Data Analysis for Research Designs. W. H. Freeman and Co., NY.";               
        html += "<br><p>Manly, B.F.J. (1997). Randomization, Bootstrap and Monte Carlo Methods in Biology. 2nd ed. Chapman and Hall / CRC , FL.";       
        html += "<br><p>Zar, J.H. 1999. Biostatistical Analysis. 4th ed. Prentice Hall, NJ.";  
                
        html += "<br><p><font size = +1><b>SVM</b></font> - ";
        html += "<b>Support Vector Machines</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=10618406&query_hl=86&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Brown, M.P., W.N. Grundy, D. Lin, N. Cristianini, C.W. Sugnet, ";
        html += "T.S. Furey, M. Ares, Jr., and D. Haussler (2000) ";
        html +="</a>";
        html += "Knowledge-based analysis of microarray gene expression data by using support vector machines. ";
        html += "<i>Proceedings of the National Academy of Sciences USA</i> 97: 262-267.";
        
        html += "<br><p><font size = +1><b>KNNC</b></font> - ";
        html += "<b>K-Nearest Neighbor Classification</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11779842&query_hl=75&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Theilhaber, J., Connolly, S. Roman-Roman, S. Bushnell, A. Jackson, K. Call, T. Garcia, R. Baron (2002) ";  
        html +="</a>";
        html += "Finding Genes in the C2Cl2 Osteogenic Pathway by K-Nearest-Neighbor Classification of Expression Data.";
        html += "<i>Genome Research</i> 12:165-176";

        html += "<br><p><font size = +1><b>DAM</b></font> - ";
        html += "<b>Discriminant Analysis Module</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=12217913&query_hl=78&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Nguyen, D.V., D.M. Rocke."; 
        html +="</a>";
        html += "Multi-class Cancer Classification via Partial Least Squares with Gene Expression Profiles.";
        html += "<i>Bioinformatics</i> 18(9):1216-1226, 2002";
        
        html += "<br><p><font size = +1><b>TRN</b></font> - ";
        html += "<b>Expression Terrain Maps</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=11557892&query_hl=82&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Kim, S.K., J. Lund, M. Kiraly, K. Duke, M. Jiang, J.M. Stuart, A. Eizinger,";
        html += "B.N. Wylie, and G.S. Davidson (2001) ";
        html +="</a>";
        html += "A Gene Expression Map for Caenorhabditis elegans. ";
        html += "<i>Science</i> 293: 2087-2092.";

        html += "<br><p><font size = +1><b>EASE</b></font> - ";
        html += "<b>Expression Analysis Systematic Explorer</b>";
        html +="<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=14519205&query_hl=84&itool=pubmed_docsum\"";
        html +=">";
        html += "<br>Hosack, D.A., G. Dennis Jr., B.T. Sherman, H.C. Lane, R.A. Lempicki. (2001)";
        html +="</a>";
        html += "Identifying biological themes within lists of genes with EASE.";
        html += "<i>Genome Biol.</i> 4:R70-R70.8, 2003.";        
        
        
        //html += "<br><p>UNDER CONSTRUCTION";
        
        html += "</body></html>";
        
        return html;
    }
    
    public static void main(String [] args){
        PaperReferencesDialog d = new PaperReferencesDialog(new Frame(), PaperReferencesDialog.createReferencesText());
        //System.exit(0);
    }
    
    public int print(java.awt.Graphics g, java.awt.print.PageFormat format, int page) throws java.awt.print.PrinterException {
        if(page > 2)
            return Printable.NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D)g;
       	g2d.clip(new java.awt.geom.Rectangle2D.Double(0, 0, format.getImageableWidth(), format.getImageableHeight()));
	g2d.translate(format.getImageableX(), -(page) * format.getImageableHeight());   
        g2d.scale(1.0,1.0);
        g2d.drawString("Test String", 0,20);   
        ed.paint(g);
        return Printable.PAGE_EXISTS;      
    }
    
    private Book makeBook(PageFormat page, int numPages){
        Book book = new Book();
        book.append(this, page, numPages);
        return book;
    }
    
    private class EventListener implements ActionListener, KeyListener {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("close-command")) {
                dispose();
            }
            else if(command.equals("print-command")){
                PrinterJob pj = PrinterJob.getPrinterJob();
                pj.setPrintable(PaperReferencesDialog.this, pj.defaultPage());
                int numPages = ed.getHeight();
                numPages /= pj.defaultPage().getImageableY();
                
                pj.setPageable(makeBook(pj.defaultPage(), numPages));
                if (pj.printDialog()) {
                    try {
                        pj.print();
                    } catch (PrinterException pe) {
                        System.out.println(pe);                       
                    }
                }
                
            }
        }
        
        
        
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                dispose();
            }
        }
        
        public void keyReleased(KeyEvent event) {;}
        public void keyTyped(KeyEvent event) {;}
    }

	public void hyperlinkUpdate(HyperlinkEvent e) {
		// TODO Auto-generated method stub
		HyperlinkEvent.EventType type=e.getEventType();
		URL url=e.getURL();
		if(type==HyperlinkEvent.EventType.ACTIVATED){
			try {
				//BrowserLauncher.openURL("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=9843981&query_hl=4&itool=pubmed_docsum" );
				BrowserLauncher.openURL(url.toString());
			} catch( IOException ex ) {
				//e.printStackTrace();
				
			}
		}
	}
}