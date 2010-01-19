package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.GBA;

public class GSEAInfoDisplay extends ActionInfoDialog{
	
	private String geneSetName;
	private String geneName;
	private String testStats;
	private EventListener listener = new EventListener();

	public GSEAInfoDisplay(JFrame parent, String title, boolean modal, String genesetname, String geneName, String testStats) {
		super(parent, title, modal);
		setGeneName(geneName);
		setGeneSetName(genesetname);
		setTestStats(testStats);
	
		init();
		// TODO Auto-generated constructor stub
	}
	
	private void init() {
			Font infoDisplayFont = new Font("Arial", Font.PLAIN, 10); //new Font("monospaced", Font.PLAIN, 10);
	        JLabel spotImage = new JLabel(new ImageIcon(org.tigr.microarray.mev.InfoDisplay.class.getResource("/org/tigr/images/spot.gif")));
	        
	       
	        
	        JEditorPane infoDisplayTextPane = new JEditorPane();
	        infoDisplayTextPane.setContentType("text/html");
	        
	        
	        
	        infoDisplayTextPane.setFont(infoDisplayFont);
	        infoDisplayTextPane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
	        infoDisplayTextPane.setEditable(false);
	        infoDisplayTextPane.setMargin(new Insets(10,15,10,10));
	        infoDisplayTextPane.setText(createMessage());
	        infoDisplayTextPane.setCaretPosition(0);
	        
	        JButton closeButton = new JButton("Close Spot Information");
	        closeButton.setActionCommand("close");
	        closeButton.addActionListener(listener);
	        
	        contentPane.setLayout(new GridBagLayout());
	        JScrollPane scrollPane = new JScrollPane(infoDisplayTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	        scrollPane.getViewport().setBackground(Color.white);
	        GBA gba = new GBA();
	        
	        gba.add(contentPane, scrollPane, 0, 0, 3, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	        gba.add(contentPane, closeButton, 0, 3, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	        pack();
	        setSize(600, 500);
	        setResizable(true);
	        setTitle("Spot Information");
	        setLocation(300, 100);
		    setVisible(true);
		   
		
	}
	
	
	private String createMessage() {
		
		 String message = "<html><body bgcolor = \"#FFFFCC\"><basefont face = \"Arial\"><table cellpadding=4 valign=top><th colspan=2 align=left valign=center><font size=6>Geneset and Genes/font></th>";
		 message += "<tr><td><i>Geneset</i></td><td>" + getGeneSetName() + "</td></tr>"+
         "<tr><td><i>Gene</i></td><td>" + getGeneName()  + "</td></tr>";
         message += "<tr><td><i>Test statistics</i></td><td>" + getTestStats() + "</td></tr>";
         message += "</table>";
	     message += "</basefont></body></html>";
	    
	     return message;
	}

	public String getGeneSetName() {
		return geneSetName;
	}

	public void setGeneSetName(String geneSetName) {
		this.geneSetName = geneSetName;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public String getTestStats() {
		return testStats;
	}

	public void setTestStats(String testStats) {
		this.testStats = testStats;
	}
	
	private class EventListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			 String command = e.getActionCommand();
	            if (command.equals("close")) {
	               	setVisible(false);
	             	dispose();
	            	            	
	            }
			
		}
		
	}
	


}
