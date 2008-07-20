/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NonparInfoViewer.java,v $
 * $Revision: 1.1 $
 * $Date: 2007-09-13 18:43:18 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class NonparInfoViewer extends ViewerAdapter implements java.io.Serializable {
	
	private JComponent header;
	private JTextArea  content;
	
	public NonparInfoViewer(int[][] clusters, String mode, boolean useAlpha, float sigLevel, float compFDR, String [] groupNames, int [] numPerGroup) {
		header  = createHeader();
		int numGenes = 0;
		
		for(int i = 0; i < clusters.length; i++)
			numGenes += clusters[i].length;
		
		content = createContent(clusters, mode, numGenes, useAlpha, sigLevel, compFDR, groupNames, numPerGroup);
		setMaxWidth(content, header);
	}
	
	
	public NonparInfoViewer(int [][] clusters, String modeStr, boolean useAlpha, float sigLevel, 
			String factorAName, String factorBName, String [] factorANames, String []factorBNames) {
		header = createHeader();

		int numGenes = 0;		
		for(int i = 0; i < clusters.length; i++)
			numGenes += clusters[i].length;
		
		content = createMackSkillingsContent(clusters, numGenes, modeStr, sigLevel, factorAName, factorBName,
				factorANames, factorBNames);
		
		setMaxWidth(content, header);
	}
	
	public NonparInfoViewer(int[][] clusters, String mode, boolean useAlpha, float sigLevel, float compFDR, String [] groupNames, int [] numPerGroup, String [] binNames, float binCutoff) {
		header  = createHeader();
		int numGenes = 0;
		
		for(int i = 0; i < clusters.length; i++)
			numGenes += clusters[i].length;
		
		content = createFisherExactContent(clusters, mode, numGenes, useAlpha, sigLevel, compFDR, groupNames, numPerGroup, binNames, binCutoff);
		setMaxWidth(content, header);
	}
	
	
	public NonparInfoViewer(JTextArea content, JComponent header){
		this.header = header;
		this.content = content;
		setMaxWidth(content, header);
	}
	
	/**
	 * Returns component to be inserted into the framework scroll pane.
	 */
	public JComponent getContentComponent() {
		return content;
	}
	
	/**
	 * Returns the viewer header.
	 */
	public JComponent getHeaderComponent() {
		return header;
	}
	
	/**
	 * Creates the viewer header.
	 */
	private JComponent createHeader() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.white);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 10, 0);
		panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Cluster Information</b></font></body></html>"), gbc);
		return panel;
	}
	
	/**
	 * Creates the viewer content component.
	 */
	private JTextArea createContent(int[][] clusters, String mode, int numGenes, boolean useAlpha,  float sigLevel, float compFDR, String [] groupNames, int [] numPerGroup) {
		
		JTextArea area = new JTextArea(clusters.length*3, 20);
		area.setEditable(false);
		area.setMargin(new Insets(0, 10, 0, 0));            
		
		StringBuffer sb = new StringBuffer(clusters.length*3*10+500);
		
		sb.append("Test: "+mode+"\n");
		if(useAlpha) {
			sb.append("Significance Based on Input Alpha \n");				
			sb.append("alpha: p < "+sigLevel+"\n");	
		} else {
			sb.append("Significance Based on Est. FDR (Benjamini-Hochberg)\n");				
			sb.append("Selected FDR Limit: "+sigLevel+"\n");
			sb.append("Computed FDR for Sig. Genes: "+compFDR+"\n");				
		}
		
		sb.append("\nGroup Information:\n");

		int n;
		for(int i = 0; i < groupNames.length-1; i++) {
			n = numPerGroup[i];
			sb.append(groupNames[i]+" ("+String.valueOf(n)+" sample"+((n>1)?"s":"")+" in analysis)\n");
		}
		
		//check for excluded samples	
		n = numPerGroup[numPerGroup.length-1];
		if(n>0)		
			sb.append(numPerGroup[numPerGroup.length-1]+" sample"+((n>1)?"s":"")+" excluded from analysis\n");		
		
		sb.append("\n");
		
		for (int counter = 0; counter < clusters.length; counter++) {
			if (counter == 0) {
				sb.append("Significant genes ");
				sb.append("\t");
				sb.append("# of Significant Genes: " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are Signficant: "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			} else {
				sb.append("Non-significant genes ");
				sb.append("\t");
				sb.append("# of non-significant Genes: " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are not signficant: "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			}
		}
		area.setText(sb.toString());
		area.setCaretPosition(0);
		return area;
	}
	
	
	public JTextArea createMackSkillingsContent(int [][] clusters, int numGenes, String modeStr, float sigLevel,
			String factorAName, String factorBName, String [] factorANames, String [] factorBNames) {
		
		JTextArea area = new JTextArea(clusters.length*3, 20);
		area.setEditable(false);
		area.setMargin(new Insets(0, 10, 0, 0));            
		
		StringBuffer sb = new StringBuffer(clusters.length*3*10+500);
		
		sb.append("Test: "+modeStr+"\n");
		sb.append("Significance Based on Input Alpha \n");				
		sb.append("alpha: p < "+sigLevel+"\n");	
		
		sb.append("\nDesign Information:\n");

		int n;
		
		sb.append(factorAName+" (Factor A) Levels:\n");
		for(int i = 0; i < factorANames.length; i++)
			sb.append("  "+factorANames[i]+"\n");
		sb.append("\n");
		sb.append(factorBName+" (Factor B) Levels:\n");
		for(int i = 0; i < factorBNames.length; i++)
			sb.append("  "+factorBNames[i]+"\n");
		sb.append("\n\n");		
		sb.append("\n");
		
		for (int counter = 0; counter < clusters.length; counter++) {
			if (counter == 0) {
				sb.append(factorAName+" Significant genes ");
			} else if(counter == 1) {
				sb.append(factorAName+" Non-significant genes ");				
			} else if(counter == 2) {
				sb.append(factorBName+" Significant genes ");				
			} else if(counter == 3) {
				sb.append(factorBName+" Non-significant genes ");								
			} else if(counter == 4) {
				sb.append("Incomplete Design (not tested)");								
			}
			sb.append("\t");
			sb.append("# of Genes: " +clusters[counter].length);
			sb.append("\n\t\t");
			sb.append("% of Genes: "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
			sb.append("\n\n");
		}
		area.setText(sb.toString());
		area.setCaretPosition(0);
		return area;
	}
	
	
	/**
	 * Creates the viewer content component.
	 */
	private JTextArea createFisherExactContent(int[][] clusters, String mode, int numGenes, boolean useAlpha,  float sigLevel, float compFDR, String [] groupNames, int [] numPerGroup, String [] binNames, float binCutoff) {
		
		JTextArea area = new JTextArea(clusters.length*3, 20);
		area.setEditable(false);
		area.setMargin(new Insets(0, 10, 0, 0));            
		
		StringBuffer sb = new StringBuffer(clusters.length*3*10+500);
		
		sb.append("Test: "+mode+"\n");
		
		if(useAlpha) {
			sb.append("Significance Based on Input Alpha \n");				
			sb.append("alpha: p < "+sigLevel+"\n");	
		} else {
			sb.append("Significance Based on Est. FDR (Benjamini-Hochberg)\n");				
			sb.append("Selected FDR Limit: "+sigLevel+"\n");
			sb.append("Computed FDR for Sig. Genes: "+compFDR+"\n");				
		}
		
		sb.append("\nGroup Information:\n");

		int n;
		for(int i = 0; i < groupNames.length-1; i++) {
			n = numPerGroup[i];
			sb.append(groupNames[i]+" ("+String.valueOf(n)+" sample"+((n>1)?"s":"")+" in analysis)\n");
		}
		
		//check for excluded samples	
		n = numPerGroup[numPerGroup.length-1];
		if(n>0)		
			sb.append(numPerGroup[numPerGroup.length-1]+" sample"+((n>1)?"s":"")+" excluded from analysis\n");		
		
		sb.append("\n");
		
		sb.append("\nBin Information:\n");
		sb.append("Data was binned or partitioned using a ");
		sb.append("cutoff value of "+String.valueOf(binCutoff)+"\n\n");
				
		for (int counter = 0; counter < clusters.length; counter++) {
			if (counter == 0) {
				sb.append("Significant genes ");
				sb.append("\t");
				sb.append("# of Significant Genes: " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are Signficant: "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			} else if(counter == 1 ){
				sb.append("Significant genes (left tail lower)");
				sb.append("\t");
				sb.append("# of Significant Genes (left tail lower): " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are Signficant (left tail lower): "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			} else if(counter == 2 ){
				sb.append("Significant genes (right tail lower)");
				sb.append("\t");
				sb.append("# of Significant Genes (right tail lower): " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are Signficant (right tail lower): "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			} else if(counter == 3 ){
				sb.append("Non-significant genes ");
				sb.append("\t");
				sb.append("# of non-significant Genes: " +clusters[counter].length);
				sb.append("\n\t\t");
				sb.append("% of Genes that are not signficant: "+Math.round((float)clusters[counter].length/(float)numGenes*100f)+"%");
				sb.append("\n\n");
			}


		}
		area.setText(sb.toString());
		area.setCaretPosition(0);
		return area;
	}
	
	/**
	 * Synchronize content and header sizes.
	 */
	private void setMaxWidth(JComponent content, JComponent header) {
		int c_width = content.getPreferredSize().width;
		int h_width = header.getPreferredSize().width;
		if (c_width > h_width) {
			header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
		} else {
			content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
		}
	}
	
	/** Returns a component to be inserted into the scroll pane row header
	 */
	public JComponent getRowHeaderComponent() {
		return null;
	}
	
}
