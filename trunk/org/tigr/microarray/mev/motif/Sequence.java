/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Sequence.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

/**************************** Sequence ADT ***************************
 * E == <I,S>	2-tuple
 * I == sequence identification key
 * S == biological sequence
 **********************************************************************/

public class Sequence {
    public int I;                       /* identifier for entity */
    public int n;                       /* length of entity sequence */
    public boolean xnu;                  /* sequences xnu'ed */
    public char[] S;                     /* sequence index in expression matrix*/
    public char[] X;                     /* if !xnu'ed == S; else X'ed seq */
    public String Info;                   /* description of entity */
    
    public Sequence(int j, Alphabet A, String SequenceString) {
	this.I=j;
	this.S=new char[SequenceString.length()];
	this.X=new char[SequenceString.length()];
	for (int i=0; i<SequenceString.length(); i++) {
	    S[i]=A.let2code[SequenceString.charAt(i)];
	    X[i]=S[i];
	}
	this.n=SequenceString.length();
	this.xnu=false;
    }
    
    public String PutSeqID() {
	long  k;
	String Text="";
	if (Info !=null) {
/*	         for (k=0; !isspace(E->info[k]); k++){
		  if(E->info[k]==0) break;
		  fprintf(fptr,"%c", E->info[k]);
	     }*/
	} else Text="random"+String.valueOf((int)I);
	return Text;
    }
}