/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: InformationPanel.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class InformationPanel extends JPanel {
    
    public Timer MyTimer;
    private ImageIcon DNAIcon;
    int InfoPosition=350;
    int MemoryPosition;
    private String Java3DTitle;
    private String Java3DVendor;
    private String Java3DVersion;
    
    public InformationPanel() {
	super();
	this.setBackground(new Color(0,71,153));
	MyTimer= new Timer(1000, new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		repaint(40,MemoryPosition,500,100);
	    }
	});
	try {
	    ClassLoader classLoader = getClass().getClassLoader();
	    classLoader.loadClass("com.sun.j3d.utils.universe.SimpleUniverse");
	    Package p = Package.getPackage("javax.media.j3d");
	    if (p == null) {
		Java3DTitle="not installed";
		Java3DVendor="not available";
		Java3DVersion="not available";
	    } else {
		Java3DTitle=p.getImplementationTitle();
		Java3DVendor=p.getImplementationVendor();
		Java3DVersion=p.getImplementationVersion();
	    }
	} catch (Exception e) {
	    Java3DTitle="not installed";
	    Java3DVendor="not available";
	    Java3DVersion="not available";
	}
	//      System.getProperties().list(System.out);
    }
    
    public void Start() {
	MyTimer.start();
    }
    
    public void Stop() {
	MyTimer.stop();
    }
    
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	g2.setColor(Color.white);
	//BufferedImage DNA = new BufferedImage(DNAIcon.getIconWidth(),DNAIcon.getIconHeight(),BufferedImage.TYPE_INT_RGB);
	//Graphics2D DNAContext = DNA.createGraphics();
	//DNAContext.drawImage(DNAIcon.getImage(), 0, 0, null);
	//g2.drawImage(DNA,this.getWidth()-200,100,this);
	Font HeadlineFont = new Font("serif",Font.BOLD,54);
	g2.setFont(HeadlineFont);
	g2.drawString("TIGR MeV",40,70);
	Font InfoFont = new Font("monospaced",Font.BOLD,14);
	g2.setFont(InfoFont);
	g2.drawString("Version "+TMEV.VERSION,40,90);
	int CurrentPosition=150;
	g2.setColor(new Color(155,180,215));
	//g2.drawString("Version 1.0 Beta",40,CurrentPosition);
	//CurrentPosition+=20;
	//g2.drawString("Program subversion (Build):",40,CurrentPosition);
	//g2.drawString("12.15.2000",InfoPosition,CurrentPosition);
	//CurrentPosition+=20;
	g2.drawString("Java Runtime Environment version:",40,CurrentPosition);
	g2.drawString(System.getProperty("java.version"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java Runtime Environment vendor:",40,CurrentPosition);
	g2.drawString(System.getProperty("java.vendor"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java Virtual Machine name:",40,CurrentPosition);
	g2.drawString(System.getProperty("java.vm.name"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java Virtual Machine version:",40,CurrentPosition);
	g2.drawString(System.getProperty("java.vm.version"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java Virtual Machine vendor:",40,CurrentPosition);
	g2.drawString(System.getProperty("java.vm.vendor"),InfoPosition,CurrentPosition);
	
	CurrentPosition+=20;
	g2.drawString("Java 3D Runtime Environment:",40,CurrentPosition);
	g2.drawString(Java3DTitle,InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java 3D Runtime Environment vendor:",40,CurrentPosition);
	g2.drawString(Java3DVendor,InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Java 3D Runtime Environment version:",40,CurrentPosition);
	g2.drawString(Java3DVersion,InfoPosition,CurrentPosition);
	
	CurrentPosition+=20;
	g2.drawString("Operating System name:",40,CurrentPosition);
	g2.drawString(System.getProperty("os.name"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Operating System version:",40,CurrentPosition);
	g2.drawString(System.getProperty("os.version"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Operating System architecture:",40,CurrentPosition);
	g2.drawString(System.getProperty("os.arch"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("User's account name:",40,CurrentPosition);
	g2.drawString(System.getProperty("user.name"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("User's home directory:",40,CurrentPosition);
	g2.drawString(System.getProperty("user.home"),InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("User's current working directory:",40,CurrentPosition);
	g2.drawString(System.getProperty("user.dir"),InfoPosition,CurrentPosition);
	DecimalFormat format = new DecimalFormat();
	format.setGroupingSize(3);
	MemoryPosition=CurrentPosition;
	CurrentPosition+=20;
	g2.drawString("Free System Memory:",40,CurrentPosition);
	long FreeMemory=Runtime.getRuntime().freeMemory();
	g2.drawString(format.format(FreeMemory)+" Bytes",InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.drawString("Total System Memory:",40,CurrentPosition);
	long TotalMemory=Runtime.getRuntime().totalMemory();
	g2.drawString(format.format(TotalMemory)+" Bytes",InfoPosition,CurrentPosition);
	CurrentPosition+=20;
	g2.setColor(new Color(55,110,175));
	g2.setColor(new Color(110,150,200));
	double OnePercent=TotalMemory/175.0;
	g2.setColor(new Color(110,150,200));
	for (int i=0; i<175; i++) {
	    if ((long)Math.round(OnePercent*i)>=FreeMemory) {
		g2.setColor(new Color(55,110,175));
	    }
	    g2.fillRect(40+i*3,CurrentPosition,2,20);
	}
    }
    
    public String getJava3DRunTimeEnvironment(){
        return this.Java3DTitle;
    }
    
    public String getJava3DVendor(){
        return this.Java3DVendor;
    }
    
    public String getJava3DVersion(){
        return this.Java3DVersion;
    }    
}