/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ProgressDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:02 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;

public class ProgressDialog extends JDialog {
    
    protected JFrame parent;
    protected String title;
    protected Container contentPane;
    protected GBA gba;
    protected JPanel labelPanel;
    protected JPanel mainPanel;
    protected JLabel[] labels;
    
    protected Vector timers;
    
    public ProgressDialog(JFrame parent, String title, boolean modal, int labelCount) {	
	super(parent, title, modal);
	this.parent = parent;
	this.title = title;
	labels = new JLabel[labelCount];
        mainPanel = new JPanel(new GridBagLayout());
	contentPane = getContentPane();
	gba = new GBA();
	timers = new Vector();	
	initializeGUI();
    }
    
    public void initializeGUI() {
	mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,5,0,5));
	contentPane.setLayout(new GridBagLayout());
	contentPane.setBackground(Color.white); 
//	contentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,5,0,5));
	labelPanel = new JPanel();
	labelPanel.setLayout(new GridBagLayout());
        labelPanel.setBackground(Color.white);
        labelPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
	
        
        
	for (int i = 0; i < labels.length; i++) {
	    labels[i] = new JLabel(title);
	    labels[i].setBackground(Color.white);
	    gba.add(labelPanel, labels[i], 0, i, 1, 1, 1, 0, GBA.H, GBA.C);
	}
        gba.add(mainPanel, labelPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
        
	gba.add(contentPane, new HeaderImagePanel(), 0, 0, 1, 1, 1, 0, GBA.B, GBA.W);
	gba.add(contentPane, mainPanel, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C);
        
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension mySize = getSize();
	setLocation((screenSize.width - mySize.width) / 2, (screenSize.height - mySize.height) / 2);
	setResizable(false);
	pack();
	setSize(405, 350);
    }
    
    public JPanel getLabelPanel() {
	return this.labelPanel;
    }
    
    public void setMainPanel(JPanel panel) {
	//contentPane.removeAll();
	gba.add(mainPanel, panel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
	repaint();
    }
    
    public void setMessage(int labelIndex, String message) {
	setMessage(labelIndex, message, Color.black);
    }
    
    public void setMessage(int labelIndex, String message, Color color) {
	
	if (labelIndex >= labels.length) throw new ArrayIndexOutOfBoundsException();
	
	JLabel label = labels[labelIndex];
	
	if (label != null) {
	    label.setForeground(color);
	    label.setText(message);
	    label.repaint();
	}
    }
    
    public void dismiss() {
	setVisible(false);
	for (int i = 0; i < timers.size(); i++) {
	    ((TimerThread) timers.elementAt(i)).setRunning(false);
	}
	dispose();
    }
    
    public void setTimerLabel(final int labelIndex, final String preString, final String postString, final int ms) {
	
	TimerThread timerThread = new TimerThread(labelIndex, preString, postString, ms);
	timerThread.setPriority(Thread.MIN_PRIORITY);
	timers.addElement(timerThread);
	timerThread.start();
    }
    
    public static void main(String [] args){
        ProgressDialog d = new ProgressDialog(new JFrame(), "Test Progress Dialog", true, 5);
        d.show();
        System.exit(0);
    }
    
    private class TimerThread extends Thread {
	
	public boolean running = true;
	private int labelIndex;
	private String preString;
	private String postString;
	private int ms;
	
	public TimerThread(int labelIndex, String preString, String postString, int ms) {
	    this.labelIndex = labelIndex;
	    this.preString = preString;
	    this.postString = postString;
	    this.ms = ms;
	}
	
	public void run() {
	    try {
		long startTime = System.currentTimeMillis();
		
		while (running) {
		    long currentTime = System.currentTimeMillis();
		    long seconds = (long) ((currentTime - startTime) / 1000f);
		    setMessage(labelIndex, preString + seconds + postString);
		    sleep(ms);
		}
		
	    } catch (InterruptedException ie) {;}
	}
	
	public void setRunning(boolean value) {
	    running = value;
	}
    }
    
        public class HeaderImagePanel extends JPanel{
        
        public HeaderImagePanel(){
            setLayout(new GridBagLayout());  
            java.net.URL url = HeaderImagePanel.class.getResource("/org/tigr/microarray/mev/cluster/gui/impl/images/dialog_banner2.gif");
            JLabel iconLabel;
            ImageIcon icon;
            if (url != null){	    
        	 icon = new ImageIcon(url);
                iconLabel = new JLabel(icon);
            }
            else
                iconLabel = new JLabel();
         //   JLabel iconLabel = new JLabel(javax.swing.UIManager.getIcon("/org/tigr/microarray/mev/cluster/gui/impl/images/dialog_banner2.gif"));
            iconLabel.setOpaque(false);
            iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            FillPanel fillPanel = new FillPanel();
            fillPanel.setBackground(Color.blue);
            add(iconLabel, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,5,0,0),0,0));
            add(fillPanel, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        }
    }
    
    public class FillPanel extends JPanel{
        GradientPaint gp;
        Color backgroundColor = new Color(25,25,169);
        Color fadeColor = new Color(140,220,240);
        
        public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            Dimension dim = this.getSize();
            //                gp = new GradientPaint(dim.width/2,0,backgroundColor,dim.width/2,dim.height/2,fadeColor);
            gp = new GradientPaint(0,dim.height/2,backgroundColor,dim.width,dim.height/2,fadeColor);
            g2.setPaint(gp);
            g2.fillRect(0,0,dim.width, dim.height);
            g2.setColor(Color.black);
        }
    }
}