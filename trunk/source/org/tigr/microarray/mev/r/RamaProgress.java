/*
 * Created on Aug 16, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * @author iVu
 */
public class RamaProgress {
	private JDialog dialog;
	private JFrame jf;
	private JProgressBar bar;
	
	
	public RamaProgress( JFrame jfP ) {
		this.jf = jfP;
		this.dialog = new JDialog( this.jf );
		this.dialog.setSize( 500, 100 );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.Y_AXIS ) );
		
		//label
		JLabel progressLabel = new JLabel( "This could take a long time" );
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout( new BoxLayout( labelPanel, BoxLayout.X_AXIS ) );
		labelPanel.add( Box.createHorizontalGlue() );
		labelPanel.add( progressLabel );
		labelPanel.add( Box.createHorizontalGlue() );
		JLabel progressLabel2 = new JLabel( "As a reference, 4 arrays (640 genes) takes about half an hour" );
		JPanel labelPanel2 = new JPanel();
		labelPanel2.setLayout( new BoxLayout( labelPanel2, BoxLayout.X_AXIS ) );
		labelPanel2.add( Box.createHorizontalGlue() );
		labelPanel2.add( progressLabel2 );
		labelPanel2.add( Box.createHorizontalGlue() );
		
		//progressbar
		this.bar = new JProgressBar();
		this.bar.setPreferredSize( new Dimension( 150, 35 ) );
		this.bar.setIndeterminate( true );
		JPanel barPanel = new JPanel();
		barPanel.setLayout( new BoxLayout( barPanel, BoxLayout.X_AXIS ) );
		barPanel.add( Box.createHorizontalGlue() );
		barPanel.add( bar );
		barPanel.add( Box.createHorizontalGlue() );
		barPanel.repaint();
		
		//add to mainPanel
		mainPanel.add( Box.createVerticalGlue() );
		mainPanel.add( labelPanel );
		mainPanel.add( labelPanel2 );
		mainPanel.add( Box.createVerticalGlue() );
		mainPanel.add( barPanel );

		this.dialog.getContentPane().add( mainPanel );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.dialog.setLocation((screenSize.width - dialog.getSize().width)/2, 
				(screenSize.height - dialog.getSize().height)/2);
		this.dialog.setVisible( true );
	}
	
	
	public void kill() {
		this.dialog.dispose();
	}
	
	
	public JProgressBar getProgressBar() {
		return this.bar;
	}
	
	
	public static void main( String[] args ) {
		RamaProgress rp = new RamaProgress( new JFrame() );
	}
}//end class