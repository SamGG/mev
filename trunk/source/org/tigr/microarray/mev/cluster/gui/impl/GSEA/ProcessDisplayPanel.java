package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.GSEAWizard.Listener;

import org.tigr.microarray.mev.file.GBA;

public class ProcessDisplayPanel extends JPanel {

	/**
	 * @param args
	 * 
	 * 
	 */
	
	//private JRadioButton runGSEA;
	//private JRadioButton antialiasing;
	private GBA gba=new GBA();
	private boolean isTextDisplay;
	private String [] stepTitles;
	private JTextPane processPane;
	
	
	public ProcessDisplayPanel(boolean isTextDisplay, String[] stepTitles) {
		super();
		setBackground(Color.white);
		this.isTextDisplay = isTextDisplay;
		this.stepTitles = stepTitles;
		constructPanel();
	}

	/**
	 * Builds the process panel
	 */
	private void constructPanel() {
		setLayout(new GridBagLayout());
		processPane = new JTextPane();
		processPane.setEditable(false);			
		processPane.setContentType("text/html");
		processPane.setOpaque(false);
		processPane.setMargin(new Insets(10,10,10,10));
		Document doc = processPane.getDocument();
		String stepDescription = "<html><font face=\"MS Sans Serif\"><b><u>Process Outline</u></b><br><br>";						
		try {
			doc.insertString(doc.getLength(), stepDescription, null);			
			for(int i = 0; i < stepTitles.length; i++) {
				stepDescription += String.valueOf(i+1);
				stepDescription += ".) ";
				stepDescription += stepTitles[i]+"<br><br>";
			}
			stepDescription += "</font></html>";			
			processPane.setText(stepDescription);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}
		setHighlight(0);			
		add(processPane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));

		
		
		/*gba=new GBA();
		setLayout(new GridBagLayout());
		runGSEA = new JRadioButton("Run GSEA");
		runGSEA.setFocusPainted(false);
		runGSEA.setActionCommand("run-gsea");
		runGSEA.addActionListener(new Listener());
		
		antialiasing = new JRadioButton("Antialiasing");
		antialiasing.setFocusPainted(false);
		antialiasing.setActionCommand("antialiasing");
	//	antialiasing.addActionListener(new Listener());
		
		gba.add(this, runGSEA, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C,
				new Insets(0, 20, 0, 5), 0, 0);
		gba.add(this, antialiasing, 0, 3, 1, 1, 1, 0, GBA.H,
				GBA.C, new Insets(0, 20, 0, 5), 0, 0);
	   */
		
	}
	
/*public class Listener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			
			
			if (command.equalsIgnoreCase("run-gsea")) {
				System.out.println("run-gsea");
				
			}else if (command.equalsIgnoreCase("antialiasing")) {
				
			}
		}
		
	}*/
	
	private void setHighlight(int step) {
		try {				
			Document doc = processPane.getDocument();			
			String text = doc.getText(0, doc.getLength());			
			int pos = text.indexOf((step+1)+".)");
			int endPos = text.indexOf(".",pos+2)+1;
			
			if(endPos < 0)
				endPos = text.length();
			
			if(pos > -1) {									
				Highlighter highlighter = processPane.getHighlighter();
				if(highlighter != null) {
					highlighter.removeAllHighlights();
					highlighter.addHighlight(pos, endPos, new MyHighlightPainter(new Color(200, 200, 255)));
				}
			}			
		} catch (BadLocationException ble) {
			//worst that can happen is no highlight..... let it go
		}		
	}

	/**
	 * Paint the process dialog
	 */
	public void paint(Graphics g) {
		super.paint(g);			
		
		GradientPaint gp = new GradientPaint(0, getHeight()/2, new Color(210,210,255), getWidth()/4,
				getHeight()/2, Color.white, false);			
		((Graphics2D)g).setPaint(gp);
		g.fillRect(0,0,getWidth()/2, getHeight());
		
		gp = new GradientPaint(getWidth(), getHeight()/2, new Color(210,210,255), getWidth()*3/4,
				getHeight()/2, Color.white, false);			
		((Graphics2D)g).setPaint(gp);
		g.fillRect(getWidth()/2,0,getWidth(), getHeight());		
		processPane.paint(g);
	}				


// A private subclass of the default highlight painter
class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
    public MyHighlightPainter(Color color) {
        super(color);
    }
}

	

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
