/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TranskriptionFactor.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:40:35 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class TranskriptionFactor {
    private static String fsep = System.getProperty("file.separator");
    public Frame ParentFrame;
    public String Name;
    public String Accession;
    public String Definition;
    public int Size=0;
    public int[][] Matrix;
    public SequenceLogo MySequenceLogo;
    public Vector Sequences;
    public boolean FastaFile=false;
    
    
    public TranskriptionFactor(Frame parentFrame) {
	ParentFrame=parentFrame;
    }
    
    public void ReadFile(String FilePath) {
	BufferedReader in;
	Vector DummyVector=new Vector();
	String Dummy=new String();
	String WorkString=new String();
	String Value=new String();
	int Position;
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(FilePath)));
	    Dummy=in.readLine();
	    while (Dummy!=null) {
		if (Dummy.substring(0,2).compareTo("NA")==0) {
		    Name=Dummy.substring(2,Dummy.length()).trim();
		}
		if (Dummy.substring(0,2).compareTo("AC")==0) {
		    Accession=Dummy.substring(2,Dummy.length()).trim();
		}
		if (Dummy.substring(0,2).compareTo("DE")==0) {
		    Definition=Dummy.substring(2,Dummy.length()).trim();
		}
		if (Dummy.substring(0,2).compareTo("P0")==0) {
		    Dummy=in.readLine();
		    while ((Dummy!=null) && (Dummy.substring(0,2).compareTo("XX")!=0)) {
			DummyVector.add(new String(Dummy));
			Dummy=in.readLine();
		    }
		    Size=DummyVector.size();
		    Matrix=new int[4][Size];
		    for (int i=0; i<DummyVector.size(); i++) {
			Dummy=(String)DummyVector.get(i);
			//                    System.out.println(Integer.valueOf(Dummy.substring(2,2+10).trim()).intValue());
			for (int j=0; j<4; j++) {
			    Matrix[j][i]=Integer.valueOf(Dummy.substring(2+j*7,Math.min(2+7*(j+1),Dummy.length())).trim()).intValue();
			}
			//                    System.out.println(Matrix[i][j]);
		    }
		}
		Dummy=in.readLine();
	    }
	    
	    if (Name==null) {
		Position=FilePath.lastIndexOf(fsep);
		Dummy=FilePath.substring(Position+1,FilePath.length());
		Position=Dummy.indexOf(".");
		Name=Dummy.substring(0,Position);
	    }
	    MySequenceLogo=new SequenceLogo(Matrix);
	    in.close();
	} catch (Exception e2) {
	    JOptionPane.showMessageDialog(null, "Can not read file "+FilePath+"!",e2.toString(), JOptionPane.ERROR_MESSAGE);
	}
    }
    
    public void ReadFastaFile(String FilePath) {
	BufferedReader in;
	Vector DummyVector=new Vector();
	String Dummy=new String();
	String WorkString=new String();
	String Value=new String();
	String Sequence=new String();
	Sequences=new Vector();
	int Position;
	Position=FilePath.lastIndexOf(fsep);
	Dummy=FilePath.substring(Position+1,FilePath.length());
	Position=Dummy.indexOf(".");
	Name=Dummy.substring(0,Position);
	
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(FilePath)));
	    Dummy=in.readLine();
	    while (Dummy!=null) {
		if (Dummy.substring(0,1).compareTo(">")==0) {
		    if (Sequence.length()!=0) {
			Sequences.add(Sequence);
			Sequence="";
		    }
		} else {
		    Sequence=Sequence.concat(Dummy);
		}
		Dummy=in.readLine();
	    }
	    Sequences.add(Sequence);
	    FastaFile=true;
	    Size=((String)Sequences.get(0)).length();
	    Matrix=new int[4][Size];
	    int CountA;
	    int CountC;
	    int CountG;
	    int CountT;
	    for (int i=0; i<Size; i++) {
		CountA=0;
		CountC=0;
		CountG=0;
		CountT=0;
		for (int j=0; j<Sequences.size(); j++) {
		    switch (((String)Sequences.get(j)).charAt(i)) {
			case 'A': CountA++; break;
			case 'C': CountC++; break;
			case 'G': CountG++; break;
			case 'T': CountT++; break;
		    }
		}
		Matrix[0][i]=CountA;
		Matrix[1][i]=CountC;
		Matrix[2][i]=CountG;
		Matrix[3][i]=CountT;
	    }
	    MySequenceLogo=new SequenceLogo(Matrix);
	    in.close();
	} catch (Exception e2) {
	    JOptionPane.showMessageDialog(null, "Can not read file "+FilePath+"!",e2.toString(), JOptionPane.ERROR_MESSAGE);
	}
    }
    
    public void Paint(Graphics2D g2, int Width, int Height) {
	g2.setColor(new Color(0,0,128));
	Font HeadlineFont = new Font("serif",Font.BOLD, 20);
	Font ReferenceFont = new Font("serif",Font.BOLD, 12);
	g2.setFont(HeadlineFont);
	g2.drawString(Name,20,40);
	g2.setColor(Color.black);
	Font InfoFont = new Font("monospaced",Font.PLAIN, 12);
	Font TableFont = new Font("monospaced",Font.BOLD, 13);
	g2.setFont(InfoFont);
	if (Accession!=null) g2.drawString("Accession number: " +Accession,20,75);
	if (Definition!=null) g2.drawString("Definition: " +Definition,20,90);
	g2.setFont(TableFont);
	g2.setColor(new Color(0,0,128));
	g2.fillRect(20,200,35+Size*25,25);
	g2.fillRect(20,225,25,110);
	g2.drawRect(20,200,35+Size*25,135);
	g2.setColor(Color.white);
	//      g2.drawLine(45,201,45,225);
	//      g2.drawLine(21,225,45,225);
	g2.drawString("A",28,250);
	g2.drawString("C",28,275);
	g2.drawString("G",28,300);
	g2.drawString("T",28,325);
	for (int i=0; i<Size; i++) {
	    g2.setFont(TableFont);
	    g2.setColor(Color.white);
	    g2.drawString(String.valueOf(i+1),60+i*25,218);
	    g2.setColor(Color.black);
	    g2.setFont(InfoFont);
	    for (int j=0; j<4; j++) {
		g2.drawString(String.valueOf(Matrix[j][i]),60+i*25,250+j*25);
		
	    }
	}
	MySequenceLogo.Paint(g2,45,400,10+Size*25,400,1,true);
    }
}
