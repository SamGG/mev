/*
 * Created on Jan 14, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
public class RamaSummaryViewer extends JPanel implements IViewer, ActionListener {
	private Rconnection rc;
	private JButton shutdownButton;
	private JFrame jf;
	
	
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
    public RamaSummaryViewer( double shift, int B, int minIter ) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout( new GridBagLayout() );
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 0;
        leftPanel.add( this.createParamPanel( shift, B, minIter ), c2 );
        
        //add buttonPanel
        c2.gridx = 0;
        c2.gridy = 1;
        leftPanel.add( this.createButtonPanel(), c2 );
        
        //add leftPanel to mainPanel
        c1.gridx = 0;
        c1.gridy = 0;
        this.add(leftPanel, c1);
    }//end constructor
	
	
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
    public RamaSummaryViewer( double shift, int B, int minIter, Rconnection rcP,
    		JFrame jfP ) {
    	this.rc = rcP;
    	this.jf = jfP;
    	
        this.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout( new GridBagLayout() );
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 0;
        leftPanel.add( this.createParamPanel( shift, B, minIter ), c2 );
        
        //add buttonPanel
        c2.gridx = 0;
        c2.gridy = 1;
        leftPanel.add( this.createButtonPanel(), c2 );
        
        //add leftPanel to mainPanel
        c1.gridx = 0;
        c1.gridy = 0;
        this.add(leftPanel, c1);
    }//end constructor


    /**
     * Displays the parameters used for classification
     * 
     * @param delta
     * @param rho
     * @return
     */
    private JPanel createParamPanel( double shift, int B, int minIter ) {
        //create a JPanel
        JPanel toReturn = new JPanel();
        Border loweredBorder = BorderFactory.createEtchedBorder();
         
        Dimension d = new Dimension( 150, 80 );
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
        String sShift = new Float( shift ).toString();
        JLabel sLabel = new JLabel(new String("  Shift = " + sShift + "  "));
        JLabel bLabel = new JLabel(new String("  B = " + B + "  "));
        JLabel minIterLabel = new JLabel("  minIter = " + minIter + "  ");
        toReturn.add(sLabel);
        toReturn.add(bLabel);
        toReturn.add(minIterLabel);
        
        return toReturn;
    }//createDRPanel()
    
    
    private JPanel createButtonPanel() {
    	JPanel toReturn = new JPanel();
    	
    	Dimension d = new Dimension( 175, 25 );
    	Dimension dButton = new Dimension( 150, 25 );
    	this.shutdownButton = new JButton( "Shutdown Rserve" );
    	this.shutdownButton.setPreferredSize( dButton );
    	this.shutdownButton.addActionListener( this );
    	
    	toReturn.add( this.shutdownButton );
    	
    	return toReturn;
    }//createButtonPanel()


    
    private void onSaveGeneList() {
    	/*
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
        */
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
        if( e.getSource() == this.shutdownButton ) {
        	try {
        		int i = JOptionPane.showConfirmDialog( this.jf, "Really Shutdown Rserve?",
        				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
        		if( i == JOptionPane.OK_OPTION ) {
        			if( this.rc != null ) {
        				this.rc.shutdown();
        			}
        			this.shutdownButton.setEnabled( false );
        		}
			} catch( RSrvException e1 ) {
				e1.printStackTrace();
			}
        }
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
    public int getViewerType() {
        return 0;
    }
    
    
    public static void main( String[] args ) {
    	
    	try {
    		System.out.println( "Trying to start stuff" );
			//Runtime.getRuntime().exec( "/Applications/Dev/R.app" );
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("R CMD Rserve --no-save");
            /*
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            System.out.println("<ERROR>");
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            System.out.println("</ERROR>");
            */
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	RamaSummaryViewer v = new RamaSummaryViewer( 11.134, 10000, 21000 );
    	JFrame jf = new JFrame(  );
    	jf.setSize( 400, 300 );
    	jf.getContentPane().add( v );
    	jf.setVisible( true );
    	jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }
}//end class
