/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageScreen.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:26:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class ImageScreen extends JWindow {
    
    public ImageScreen() { 
	super();
        addMouseListener(new Listener());
       
	ImageIcon image = new ImageIcon(ImageScreen.class.getResource("/org/tigr/images/mev_splash.gif"));
	ImageCanvas canvas = new ImageCanvas(image);
	canvas.setPreferredSize(new Dimension(image.getIconWidth()+4, image.getIconHeight()+5));     
	getContentPane().add(canvas, BorderLayout.CENTER);
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
    }
    
    class ImageCanvas extends JPanel {
	
	private ImageIcon image;
	
	public ImageCanvas(ImageIcon image) {
	    this.image = image;
            setBackground(Color.blue);
            setBorder(BorderFactory.createLineBorder(Color.black, 1));
	}
	
	public void paint(Graphics g) {
            super.paint(g);
	    g.drawImage(image.getImage(), 2, 2, this);
	}
    }
    
    public static void main(String [] args) {
        ImageScreen is = new ImageScreen();
        is.showImageScreen();
    }
    
    class Listener extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            ImageScreen.this.dispose();
        }
    }
}