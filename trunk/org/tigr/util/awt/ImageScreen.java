/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageScreen.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

public class ImageScreen extends JFrame {
    
    public ImageScreen() {
	super("TIGR MultiExperimentViewer");
	setImageIcon(new ImageIcon(ImageScreen.class.getResource("/org/tigr/images/expression.gif")));
	ImageIcon image = new ImageIcon(ImageScreen.class.getResource("/org/tigr/images/MeV_splash.jpg"));
	ImageCanvas canvas = new ImageCanvas(image);
	canvas.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
	getContentPane().add(canvas, BorderLayout.CENTER);
	setResizable(false);
	pack();
    }
    
    public void showImageScreen() {
	Dimension screenSize = getToolkit().getScreenSize();
	setLocation(screenSize.width/2 - getSize().width/2, screenSize.height/2 - getSize().height/2);
	show();
    }
    
    public void showImageScreen(long millis) {
	try {
	    showImageScreen();
	    Thread.sleep(millis);
	    dispose();
	} catch (Exception e) {
	}
    }
    
    private void setImageIcon(ImageIcon icon) {
	setIconImage(icon.getImage());
    }
    
    class ImageCanvas extends JPanel {
	
	private ImageIcon image;
	
	public ImageCanvas(ImageIcon image) {
	    this.image = image;
	}
	
	public void paint(Graphics g) {
	    g.drawImage(image.getImage(), 0, 0, this);
	}
    }
}