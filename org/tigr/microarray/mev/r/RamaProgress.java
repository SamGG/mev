/*
 * Created on Aug 16, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * @author iVu
 */
public class RamaProgress extends Thread {
	private JDialog dialog;
	private JFrame jf;
	private JProgressBar bar;
	
	
	public RamaProgress( JFrame jfP ) {
		this.jf = jfP;
		this.dialog = new JDialog( this.jf );
		this.dialog.setSize( 200, 100 );
		this.bar = new JProgressBar();
		this.bar.setPreferredSize( new Dimension( 150, 35 ) );
		bar.setIndeterminate( true );
		JLabel progressLabel = new JLabel( "This will take a long time" );
		
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		panel.add( new JLabel( "  " ) );
		panel.add( progressLabel );
		panel.add( bar );
		panel.repaint();

		this.dialog.getContentPane().add( panel );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.dialog.setLocation((screenSize.width - dialog.getSize().width)/2, 
				(screenSize.height - dialog.getSize().height)/2);
		this.dialog.setVisible( true );
	}
	
	
	public void run() {
		this.dialog.repaint();
	}
	
	
	public void kill() {
		this.dialog.dispose();
	}
	
	
	public static void main( String[] args ) {
		RamaProgress rp = new RamaProgress( new JFrame() );
		rp.run();
	}
}//end class