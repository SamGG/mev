/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HypergeometricProbability.java,v $
 * $Revision: 1.1 $
 * $Date: 2004-02-06 22:55:36 $
 * $Author: braisted $
 * $State: Exp $
 */

/*
 * Catherine Loader of Bell Laboratories originally wrote
 * this fast and accurate estimation of Gaussian hyper-geometric
 * probability in C:
 * (http://cm.bell-labs.com/cm/ms/departments/sia/catherine/dbinom/).
 *
 * It has been incorporated into the DAVID(1) and EASE(2) software
 * packages and converted to open-source java code by the Laboratory
 * of Immunopathogenesis and Bioinformatics, Science Applications
 * International Corporation-Frederick, Inc.  For questions, comments,
 * and acknowledgements please contact:

 * Richard A. Lempicki, PhD
 * Senior Scientist
 * SAIC-Frederick, Inc., Clinical Services Program
 * Head, Laboratory of Immunopathogenesis and Bioinformatics
 * P.O. Box B
 * Frederick, MD 21702-1201
 * T: (301) 846-5093
 * F: (301) 846-6762
 * E: rlempicki@niaid.nih.gov

 * Acknowledgements:

 * 1.	Dennis G Jr., Sherman BT., Hosack DA., Lane CH., Lempicki RA.
 * DAVID: Database for Annotation, Visualization, and Integrated Discovery.
 * Genome Biology 4(9):R60.
 *
 * 2.	Hosack DA., Dennis G Jr., Sherman BT., Lane HC., and Lempicki RA.
 * Identifying biological themes within lists of genes with EASE.
 * Genome Biology 4(10):R70.
 */

/* 
 * Developed by NIAID LTB Lab.
 */

package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

public class HypergeometricProbability {

    /** Pi Squared pre-calculated */
    public final double P12 = 6.283185307179586476925286;
    /** First saddle point for correcting error of Stirling's approximation*/
    public final double S0 = 0.083333333333333333333;
    /** First saddle point for correcting error of Stirling's approximation*/
    public final double S1 = 0.00277777777777777777778;
    /** First saddle point for correcting error of Stirling's approximation*/
    public final double S2 = 0.00079365079365079365079365;
    /** First saddle point for correcting error of Stirling's approximation*/
    public final double S3 = 0.000595238095238095238095238;
    /** First saddle point for correcting error of Stirling's approximation*/
    public final double S4 = 0.0008417508417508417508417508;

    /** Array storing errors of Stirling's approximation of n! */
    public static double[] sfe = new double[16];

    public HypergeometricProbability() {

            sfe[0] = 0.0;
            sfe[1] = 0.081061466795327258219670264;
            sfe[2] = 0.041340695955409294093822081;
            sfe[3] = 0.0276779256849983391487892927;
            sfe[4] = 0.020790672103765093111522771;
            sfe[5] = 0.0166446911898211921631948653;
            sfe[6] = 0.013876128823070747998745727;
            sfe[7] = 0.0118967099458917700950557241;
            sfe[8] = 0.010411265261972096497478567;
            sfe[9] = 0.0092554621827127329177286366;
            sfe[10] = 0.008330563433362871256469318;
            sfe[11] = 0.0075736754879518407949720242;
            sfe[12] = 0.006942840107209529865664152;
            sfe[13] = 0.0064089941880042070684396310;
            sfe[14] = 0.005951370112758847735624416;
            sfe[15] = 0.0055547335519628013710386899;
    }


    /** returns error of Stirling's approximation of n! */
    public double stirlerr(int n){
        double nn;
        if (n<16) return(sfe[(int)n]);
        nn = (double)n;
        nn = nn*nn;
        if (n>500) return((S0-S1/nn)/n);
        if (n>80) return((S0-(S1-S2/nn)/nn)/n);
        if (n>35) return((S0-(S1-(S2-S3/nn)/nn)/nn)/n);
        return((S0-(S1-(S2-(S3-S4/nn)/nn)/nn)/nn)/n);
    }

    /** returns a factor of the binomial distribution */
    public double bd0(int x, double np){
        double ej, s, s1, v;
        int j;

        if(Math.abs(x-np) < 0.1*(x+np)){
            s = (x-np)*(x-np)/(x+np);
            v = (x-np)/(x+np);
            ej = 2*x*v;
            v = v*v;

            for (j=1; ;++j){
                ej *= v;
                s1 = s+ej/((j<<1)+1);
                if (s1==s) return(s1);
                s = s1;
            }
        }

        return(x*Math.log(x/np)+np-x);
    }

    /** returns a discrete value from the binomial distribution */
    public double dbinom(int x, int n, double p){

        double lc;

        if (p==0.0) return( (x==0) ? 1.0 : 0.0);
        if (p==1.0) return( (x==n) ? 1.0 : 0.0);
        if (x==0) return(Math.exp(n*Math.log(1-p)));
        if (x==n) return(Math.exp(n*Math.log(p)));
        if ((x<0) | (x>n)) return(0.0);

        lc = stirlerr(n) - stirlerr(x) - stirlerr(n-x) - bd0(x,n*p) - bd0(n-x,n*(1.0-p));


        return(Math.exp(lc)*Math.sqrt(n/(P12*x*(n-x))));
    }

    /** returns the binomial approximation of a discrete value from the hypergeometric distribution*/
    public double dhyperg(int x, int t, int m, int n){
        double p;
        p = ((double)m)/((double)n);

        return( dbinom(x,t,p) * dbinom(m-x,n-t,p) / dbinom(m,n,p) );
    }


    /** returns the one-tailed hypergeometric probability of over-representation */
    public double SumHGP(int  POPTOT, int POPHITS, int SAMPTOT, int SAMPHITS){
        double P;

        if ((SAMPHITS>SAMPTOT) || (SAMPHITS>POPHITS) || (SAMPTOT>POPTOT) || (POPHITS>POPTOT)){

            return 0;
        }
        else if (SAMPHITS<1){return 1; }
        else if (POPHITS<SAMPTOT){
            int R;
            P=0;
            for (R=SAMPHITS; R<(POPHITS+1); R++){
		P+=dhyperg(R, SAMPTOT, POPHITS, POPTOT);
	    }
            return P;
        }else{
            int R;
            P=0;
            for (R=SAMPHITS; R<(SAMPTOT+1); R++){
            	P+=dhyperg(R, SAMPTOT, POPHITS, POPTOT);
            	}
            return P;
	}
    }
    
    public static void main(String [] args){
        HypergeometricProbability hgp = new HypergeometricProbability();
        System.out.println("p = "+hgp.SumHGP(325, 19, 37, 5));
    }

}
