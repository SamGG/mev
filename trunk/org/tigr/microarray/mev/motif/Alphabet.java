/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Alphabet.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.tigr.microarray.mev.cluster.*;

public class Alphabet {
    public int n;                /* number of LETTERS */
    private String alphabet;    /* ALPHABET */
    public  String code2let;    /* CODE2LETTER */
    public  String code2lower;     /* CODE2LETTER lower case */
    public char[] let2code;        /* LETTER2CODE */
    private String C;             /* complementary bases */
    private String prs;           /* pairs string */
    private char[][] R;           /* relatedness scoring matrix */
    private char[][] pairs;        /* residue pairs */
    private long loR;             /* lowest value in R */
    private long hiR;             /* highest value in R */
    private long npairs;          /* number of pairs */
    private boolean[] paired;  /* is residue r paired? */
    private int ALPHA_NUM_SYMBOLS=127;
    
    public Alphabet(String Letters, char[] R) {
	int i;
	char c;
	this.n = Letters.length();
	this.alphabet=new String(Letters);       /* ALPHABET */
	this.code2let=new String(Letters);       /* CODE2LETTER */
	this.code2lower=new String(Letters);     /* LETTER2CODE */
	this.let2code=new char[ALPHA_NUM_SYMBOLS];
	for (i=0; i<ALPHA_NUM_SYMBOLS; i++) {
	    let2code[i]=0;   /* =error */
	}
	for (i=0; i<this.n; i++) {
	    c = Letters.charAt(i);
	    this.let2code[c] = (char)i;
	}
	if (R==null) {                      /* RELATION */
	    this.R = null;
	}
	this.paired=new boolean[this.n+1];
	for (i=0; i<=this.n; i++) {
	    this.paired[i]=false;
	}
	this.pairs = null;
	this.prs = null;
	this.C = null;
    }
}