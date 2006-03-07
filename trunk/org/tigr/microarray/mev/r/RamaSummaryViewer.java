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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
	private JButton saveResult;
	
	private JFrame jf;
	
	private RamaResult result;
	
	
    /**
     * 
     * @param resultP
     */
    public RamaSummaryViewer( RamaResult resultP ) {
		this.result = resultP;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.anchor = GridBagConstraints.FIRST_LINE_START;
		
		//create Panel for params and button
		JPanel leftPanel = this.createLeftPanel();
		c1.gridx = 0;
		c1.gridy = 0;
		this.add(leftPanel, c1);
		
		//create tablePanel
		JPanel tablePanel = this.createTablePanel( result );
		c1.gridx = 1;
		c1.gridy = 0;
		this.add( tablePanel, c1 );
    }//end constructor
    
    
    /**
     * 
     * @param result
     * @return
     */
    private JPanel createTablePanel( RamaResult result ) {
    	JPanel returnPanel = new JPanel();
    	returnPanel.setSize( 600,400 );
    	
    	//display gammas in a jtable
		DefaultTableModel dm = new DefaultTableModel();
    	dm.setDataVector( result.getResultTable(), result.getResultHeader() );
		TableSorter sorter = new TableSorter( dm );
		JTable jt = new JTable( sorter ) {
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);
				repaint();
			}
		};
		sorter.setTableHeader( jt.getTableHeader() );
		jt.setPreferredScrollableViewportSize(new Dimension(600, 400));
		
		//set column widths
		jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		TableColumn column = null;
		for ( int i = 0; i < result.getResultHeader().length; i ++ ) {
			column = jt.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(200);
			} else  {
				column.setPreferredWidth(100);
			} 
		}
		//add components and return
		JScrollPane jsp = new JScrollPane( jt );
		returnPanel.add( jsp );
    	
    	return returnPanel;
    }//createResultPanel()
    
    
    /**
     * 
     * @return
     */
    private JPanel createLeftPanel() {
		JPanel leftPanel = new JPanel();
		leftPanel.setSize( 200, 400 );
		leftPanel.setBackground( Color.RED );
		leftPanel.setLayout( new GridBagLayout() );
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		
		//add paramPanel
		c2.gridx = 0;
		c2.gridy = 0;
		leftPanel.add( this.createParamPanel( result.getShift(), 
				result.getB(), result.getMinIter() ), c2 );
		
		//add buttonPanel
		c2.gridx = 0;
		c2.gridy = 1;
		leftPanel.add( this.createButtonPanel(), c2 );
    	
		return leftPanel;
    }//createLeftPanel()


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
    
    
    /**
     * 
     * @return
     */
    private JPanel createButtonPanel() {
    	JPanel toReturn = new JPanel();
    	toReturn.setLayout( new BoxLayout( toReturn, BoxLayout.Y_AXIS ) );
    	toReturn.setSize( 300, 400 );
    	
    	//Dimension d = new Dimension( 175, 25 );
    	Dimension dButton = new Dimension( 150, 25 );
    	
    	//create shutdown button
    	this.shutdownButton = new JButton( "Shutdown Rserve" );
    	this.shutdownButton.setPreferredSize( dButton );
    	this.shutdownButton.setMinimumSize( dButton );
    	this.shutdownButton.setMaximumSize( dButton );
    	this.shutdownButton.addActionListener( this );
		
    	//create save button
		this.saveResult = new JButton( "Save Results" );
		this.saveResult.setPreferredSize( dButton );
		this.saveResult.setMinimumSize( dButton );
		this.saveResult.setMaximumSize( dButton );
		this.saveResult.addActionListener( this );
		
		//add to panel
		//toReturn.add( Box.createHorizontalGlue() );
		toReturn.add( this.saveResult );
		//toReturn.add( Box.createHorizontalGlue() );
    	toReturn.add( this.shutdownButton );
		//toReturn.add( Box.createHorizontalGlue() );
    	
    	return toReturn;
    }//createButtonPanel()

    
    /**
     * 
     */
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
		} else if( e.getSource() == this.saveResult ) {
			this.result.saveRamaResult( this.jf );
		}
	}//actionPerformed()


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
    	String[] genes = new String[ 1 ];
    	double[] g1 = new double[ 1 ];
    	double[] g2 = new double[ 1 ];
    	double shift = .2d;
    	String start = "12:30";
    	String end = "1:30";
    	
    	genes[ 0 ] = "test1";
    	g1[ 0 ] = .5d;
    	g2[ 0 ] = 1.1d;
    	
    	RamaResult result = new RamaResult( g1, g2, shift, start, end );
    	result.setGenes( genes );
		result.setB( 2 );
		result.setMinIter( 3 );
    	RamaSummaryViewer v = new RamaSummaryViewer( result );
		
    	JFrame jf = new JFrame(  );
    	jf.setSize( 800, 600 );
    	jf.getContentPane().add( v );
    	jf.setVisible( true );
    	jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    	
    	/*
    	try {
    		System.out.println( "Trying to start stuff" );
			//Runtime.getRuntime().exec( "/Applications/Dev/R.app" );
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("R CMD Rserve --no-save");
            
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            System.out.println("<ERROR>");
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            System.out.println("</ERROR>");
            
		} catch( IOException e ) {
			e.printStackTrace();
		}
    	*/
		/*
    	RamaSummaryViewer v = new RamaSummaryViewer( 11.134, 10000, 21000 );
    	JFrame jf = new JFrame(  );
    	jf.setSize( 400, 300 );
    	jf.getContentPane().add( v );
    	jf.setVisible( true );
    	jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    	*/
    }
}//end class
