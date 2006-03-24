/*
 * Created on Jan 14, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 * Should display delta, rho, class assignment, discriminant score, genelist
 * 
 * @author vu
 */
public class USCSummaryViewer extends JPanel implements IViewer, ActionListener {
    private JButton saveGeneList;
    private JButton anotherDeltaRho;
    private USCGene[] genes;
    private USCResult result;
    transient IFramework framework;


    /**
     * Constructor
     * 
     * @param hybNames
     * @param resultP
     * @param uniqueClasses
     * @param params
     * @param genesP
     * @param frameworkP
     */
    public USCSummaryViewer(String[] hybNames, USCResult resultP,
            String[] uniqueClasses, String[] params, USCGene[] genesP,
            IFramework frameworkP) {
        this.framework = frameworkP;

        this.result = resultP;
        this.genes = genesP;

        //this.setLayout( new SpringLayout() );
        this.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 0;
        leftPanel.add(this.createParamPanel(this.result.getDelta(), this.result
                .getRho(), params, this.result.getNumGenesUsed()), c2);
        c2.gridx = 0;
        c2.gridy = 1;
        leftPanel.add(
                this.createAssPanel(hybNames, this.result, uniqueClasses), c2);
        c1.gridx = 0;
        c1.gridy = 0;

        this.add(leftPanel, c1);

        JPanel rightPanel = new JPanel();
        rightPanel.add(this.createGenePanel(this.genes));
        c1.gridx = 1;
        c1.gridy = 0;

        this.add(rightPanel, c1);
    }//end constructor


    /**
     * Displays the parameters used for classification
     * 
     * @param delta
     * @param rho
     * @return
     */
    private JPanel createParamPanel(double delta, double rho, String[] params,
            int numGenesUsed) {
        Border loweredBorder = BorderFactory.createLoweredBevelBorder();

        //create a JPanel
        JPanel toReturn = new JPanel();
        Dimension d = new Dimension(410, 130);
        toReturn.setPreferredSize(d);
        toReturn.setMaximumSize(d);
        toReturn.setMinimumSize(d);
        Font font = new Font("Arial", Font.PLAIN, 12);
        toReturn.setBorder(BorderFactory.createTitledBorder(loweredBorder,
                "Parameters Used", TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION, font, Color.GRAY));

        //list the params vertically
        BoxLayout bl = new BoxLayout(toReturn, BoxLayout.Y_AXIS);
        toReturn.setLayout(bl);

        //create the JLabels and add them to the JPanel
        String sDelta = new Float(delta).toString();
        String sRho = new Float(rho).toString();
        JLabel dLabel = new JLabel(new String("  Delta = " + sDelta + "  "));
        JLabel rLabel = new JLabel(new String("  Rho = " + sRho + "  "));
        JLabel binLabel = new JLabel("  # of Bins = " + params[0] + "  ");
        JLabel loLabel = new JLabel("  Correlation Low = " + params[1] + "  ");
        JLabel hiLabel = new JLabel("  Correlation High = " + params[2] + "  ");
        JLabel stepLabel = new JLabel("  Correlation Step = " + params[3]
                + "  ");
        toReturn.add(dLabel);
        toReturn.add(rLabel);
        toReturn.add(binLabel);
        toReturn.add(loLabel);
        toReturn.add(hiLabel);
        toReturn.add(stepLabel);

        return toReturn;
    }//createDRPanel()


    /**
     * Display the assignments and the discriminant scores
     * 
     * @param testHybNames
     * @param result
     * @param uniqueClasses
     * @return
     */
    private JPanel createAssPanel(String[] testHybNames, USCResult resultP,
            String[] uniqueClasses) {
        Border loweredBorder = BorderFactory.createLoweredBevelBorder();
        StringBuffer sbAss = new StringBuffer();
        StringBuffer sbDScore = new StringBuffer();

        //should list the test hybs and the class to which they were assigned
        for( int h = 0; h < testHybNames.length; h++ ) {
            int iClass = resultP.getAssignedClassIndex(h);

            String sAss = new String("  " + testHybNames[h] + " >>> "
                    + uniqueClasses[iClass] + "  ");

            String dScore = Double.toString(resultP.getDiscScores()[h][iClass]);

            sbAss.append(this.formatLine(sAss, dScore));
            sbAss.append(USCGUI.END_LINE);
        }//end h

        JPanel toReturn = new JPanel();

        JTextArea assArea = new JTextArea(testHybNames.length, 15);
        assArea.setBackground(toReturn.getBackground());
        assArea.setText(sbAss.toString());
        JScrollPane jspAss = new JScrollPane(assArea);
        Dimension d = new Dimension(410, 385);
        jspAss.setPreferredSize(d);
        jspAss.setMaximumSize(d);
        jspAss.setMinimumSize(d);

        toReturn.setSize(410, 365);
        toReturn.add(jspAss);
        return toReturn;
    }//USCSummaryViewer()


    /**
     * 
     * @param genes
     * @return
     */
    private JPanel createGenePanel(USCGene[] genesP) {
        Border loweredBorder = BorderFactory.createLoweredBevelBorder();

        String genesUsed = new String(genesP.length + " Genes Used");

        JPanel toReturn = new JPanel();
        Font font = new Font("Arial", Font.PLAIN, 12);
        toReturn.setBorder(BorderFactory.createTitledBorder(loweredBorder,
                genesUsed, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
                font, Color.GRAY));
        Dimension d = new Dimension(210, 525);
        toReturn.setPreferredSize(d);
        toReturn.setMaximumSize(d);
        toReturn.setMinimumSize(d);

        StringBuffer sbGenes = new StringBuffer();
        for( int i = 0; i < genesP.length; i++ ) {
            sbGenes.append(genesP[i].getGeneName());
            sbGenes.append(USCGUI.END_LINE);
        }
        JTextArea geneArea = new JTextArea(genesP.length, 15);
        geneArea.setBackground(toReturn.getBackground());
        geneArea.setText(sbGenes.toString());
        JScrollPane jsp = new JScrollPane(geneArea);
        //jsp.setPreferredSize( new Dimension( 190, 460 ) );
        jsp.setPreferredSize(new Dimension(190, 430));
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        toReturn.add(jsp);

        this.saveGeneList = new JButton("Save GeneList");
        Dimension dButton = new Dimension(200, 30);
        saveGeneList.setPreferredSize(dButton);
        saveGeneList.setMaximumSize(dButton);
        saveGeneList.setMinimumSize(dButton);
        this.saveGeneList.addActionListener(this);
        toReturn.add(this.saveGeneList);
        /*
         * this.anotherDeltaRho = new JButton( "Test With Different Params" );
         * saveGeneList.setPreferredSize( dButton );
         * saveGeneList.setMaximumSize( dButton ); saveGeneList.setMinimumSize(
         * dButton ); this.anotherDeltaRho.addActionListener( this );
         * toReturn.add( this.anotherDeltaRho );
         */
        return toReturn;
    }//createGenePanel()


    private void onAnotherDeltaRho() {
        System.out.println("onAnotherDeltaRho()");
    }//onAnotherDeltaRho()


    /**
     * 
     *  
     */
    private void onSaveGeneList() {
        String currentPath = TMEV.getDataPath();
        USCTextFileFilter textFilter = new USCTextFileFilter();
        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.addChoosableFileFilter(textFilter);
        if( chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ) {
            File saveFile;

            if( chooser.getFileFilter() == textFilter ) {
                //make sure to add .txt
                String path = chooser.getSelectedFile().getPath();
                if( path.toLowerCase().endsWith("txt") ) {
                    //great, already ok
                    saveFile = new File(path);
                } else {
                    //add it
                    String subPath;
                    int period = path.lastIndexOf(".");
                    if( period != -1 ) {
                        System.out.println("period  = -1");
                        subPath = path.substring(0, period);
                    } else {
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    saveFile = new File(newPath);
                }
            } else {
                saveFile = chooser.getSelectedFile();
            }
            StringBuffer sb = new StringBuffer();
            
            for( int i = 0; i < this.genes.length; i++ ) {
            	USCGene gene = this.genes[ i ];
            	
                sb.append(gene.getGeneName());
				
				for( int e = 0; e < gene.getExtraFieldSize(); e ++ ) {
					sb.append( USCGUI.TAB );
					sb.append( gene.getExtraField( e ) );
				}
				
				sb.append( USCGUI.END_LINE );
            }
            
            this.writeFile(saveFile, sb.toString());
        } else {
            System.out.println("User cancelled Gene List Save");
        }
    }//onSaveGeneList()


    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }//writeFile()


    public void actionPerformed(ActionEvent e) {
        if( e.getSource() == this.saveGeneList ) {
            this.onSaveGeneList();
        } else if( e.getSource() == this.anotherDeltaRho ) {
            this.onAnotherDeltaRho();
        }
    }


    /**
     * 
     * @param assLine
     * @param dScore
     * @return
     */
    private String formatLine(String assLine, String dScore) {
        int width = 70;

        int assKount = assLine.length();
        int dKount = dScore.length();

        int diff = width - assKount - dKount;

        StringBuffer sb = new StringBuffer(assLine);
        for( int i = 0; i < diff; i++ ) {
            sb.append("-");
        }
        sb.append(dScore);

        return sb.toString();
    }


    public JComponent getContentComponent() {
        return this;
    }//getContentComponent()


    public JComponent getHeaderComponent() {
        return null;
    }


    public JComponent getRowHeaderComponent() {
        return null;
    }


    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }


    public void onSelected(IFramework framework) {

    }


    public void onDataChanged(IData data) {

    }


    public void onMenuChanged(IDisplayMenu menu) {

    }


    public void onDeselected() {

    }


    public void onClosed() {

    }


    public BufferedImage getImage() {
        return null;
    }


    public int[][] getClusters() {
        return null;
    }


    public Experiment getExperiment() {
        return null;
    }


    /* (non-Javadoc)
     * @see org.tigr.microarray.mev.cluster.gui.IViewer#getViewerType()
     */
    public int getViewerType() {
        return 0;
    }


	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}


	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}
}//end class

/*
 * public JPanel createAssPanelTest() { String[] testHybNames = new String[ 20 ];
 * String[] dScores = new String[ 20 ]; for( int i = 0; i < testHybNames.length;
 * i ++ ) { testHybNames[ i ] = ( i + ".txt" ); dScores[ i ] = ( "0." + i ); }
 * 
 * Border loweredBorder = BorderFactory.createLoweredBevelBorder();
 * 
 * JPanel assPanel = new JPanel(); Dimension d = new Dimension( 315, 385 );
 * assPanel.setPreferredSize( d ); assPanel.setMaximumSize( d );
 * assPanel.setMinimumSize( d ); Font font = new Font( "Arial", Font.PLAIN, 12 );
 * assPanel.setBorder( BorderFactory.createTitledBorder( loweredBorder, "Class
 * Assignments", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, font,
 * Color.GRAY ) );
 * 
 * BoxLayout bl = new BoxLayout( assPanel, BoxLayout.Y_AXIS );
 * assPanel.setLayout( bl );
 * 
 * JPanel dPanel = new JPanel(); Dimension dD = new Dimension( 95, 385 );
 * dPanel.setPreferredSize( dD ); dPanel.setMaximumSize( dD );
 * dPanel.setMinimumSize( dD ); dPanel.setBorder(
 * BorderFactory.createTitledBorder( loweredBorder, "D-Score",
 * TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, font, Color.GRAY ) );
 * 
 * BoxLayout bl2 = new BoxLayout( dPanel, BoxLayout.Y_AXIS ); dPanel.setLayout(
 * bl2 );
 * 
 * //should list the test hybs and the class to which they were assigned for(
 * int h = 0; h < testHybNames.length; h ++ ) { String sAss = new String( " " +
 * testHybNames[ h ] + " >> " + h + " " );
 * 
 * JLabel label = new JLabel( sAss ); assPanel.add( label );
 * 
 * JLabel dLabel = new JLabel( dScores[ h ] ); dPanel.add( dLabel ); }//end h
 * 
 * JPanel toReturn = new JPanel(); toReturn.setLayout( new SpringLayout() );
 * toReturn.setSize( 410, 385 ); toReturn.add( assPanel ); toReturn.add( dPanel );
 * SpringUtilities.makeCompactGrid( toReturn, 1, 2, 0, 0, 0, 0 ); return
 * toReturn; }//
 */

/*
 * //for testing purposes public static void main( String[] args ) {
 * System.out.println( "invoked by main" ); USCSummaryViewer usv = new
 * USCSummaryViewer(); JFrame jf = new JFrame(); jf.setSize( 800, 620 );
 * jf.getContentPane().add( usv ); jf.show(); } public USCSummaryViewer() {
 * this.setLayout( new SpringLayout() );
 * 
 * String[] params = new String[ 4 ]; params[ 0 ] = "50"; params[ 1 ] = "0.5";
 * params[ 2 ] = "1.0"; params[ 3 ] = "0.1";
 * 
 * String[] g = new String[ 600 ]; for( int i = 0; i < g.length; i ++ ) { g[ i ] =
 * new String( i + " 0123456789012345678" ); }
 * 
 * JPanel leftPanel = new JPanel(); leftPanel.setLayout( new SpringLayout() );
 * leftPanel.add( this.createParamPanel( 0.4f, 0.9f, params ) ); leftPanel.add(
 * this.createAssPanelTest() );
 * 
 * JPanel rightPanel = new JPanel(); rightPanel.setLayout( new SpringLayout() );
 * rightPanel.add( this.createGenePanel( g ) );
 * 
 * this.add( leftPanel ); this.add( rightPanel );
 * 
 * SpringUtilities.makeCompactGrid( this, 1, 2, 10, 10, 10, 10 );
 * SpringUtilities.makeCompactGrid( leftPanel, 2, 1, 10, 10, 10, 10 );
 * SpringUtilities.makeCompactGrid( rightPanel, 1, 1, 10, 10, 10, 10 ); }
 */
