/*
 * Permutations.java
 *
 * Created on November 14, 2003, 3:42 PM
 */

package org.tigr.util;

/**
 *
 * @author  nbhagaba
 */
public class Permutations {
    
    
/*-------------------------------------------------------------------
 Description:
   Enumerates all possible permutations of choosing k objects from n
   distint objects.  Initialize the enumeration by setting j[0] to a
   negative value.  Then, each call to enumeratePermutations will
   generate the next permutation and place it in j[0..k-1].  A return
   value of false indicates there are no more permutations to
   generate.  j needs to be allocated with a size for at least n
   elements.
 
 Author:
   Brent Worden
 
 Language:
   C++
 
 Usage:
   int perm[10] = {-1};
   while(enumeratePermutations(10, 5, perm)){
       // do something with perm[0..4]
   }
 
 Reference:
   Tucker, Allen.  Applied Combinatorics.  3rd Ed.  1994.
-------------------------------------------------------------------*/
    public static boolean enumeratePermutations(int n, int k, int j[]){
        int i;
        if(j[0] < 0){
            for(i = 0; i < n; ++i){
                j[i] = i;
            }
            int start = k;
            int end = n - 1;
            int t;
            while(start < end){
                t = j[start];
                j[start++] = j[end];
                j[end--] = t;
            }
            return true;
        } else {
            for(i = n - 2; i >= 0 && j[i] >= j[i+1]; --i){}
            if(i < 0){
                return false;
            } else {
                int least = i + 1;
                for(int m = i + 2; m < n; ++m){
                    if(j[m] < j[least] && j[m] > j[i]){
                        least = m;
                    }
                }
                int t = j[i];
                j[i] = j[least];
                j[least] = t;
                if(k - 1 > i){
                    int start = i + 1;
                    int end = n - 1;
                    while(start < end){
                        t = j[start];
                        j[start++] = j[end];
                        j[end--] = t;
                    }
                    start = k;
                    end = n - 1;
                    while(start < end){
                        t = j[start];
                        j[start++] = j[end];
                        j[end--] = t;
                    }
                }
                return true;
            }
        }
    }
    
    public static void main(String[] args) {
	int[] comb = new int[5];
	for (int i = 0; i < comb.length; i++) {
	    comb[i] = -1;
	}
	int counter = 0;
        /*
	while (enumeratePermutations(5, 5, comb)) {
	    for (int i = 0; i < comb.length; i++) {
		System.out.print("" + comb[i] + " ");
	    }
            counter++;
	    System.out.println();
	}
        System.out.println("Number of permutations = " + counter);
         **/
         
        
        for (int i = 1000; i <= 1500; i++) {
            String s = String.valueOf(i);
            
            float exp = Float.parseFloat(s);
            //System.out.println("s = " +s + ", exp = " + exp + ", 2^" + exp + " = " + (float)(Math.pow(2, exp)));
            System.out.println("2^" + i + " = "+ Math.pow(2, i));
        }
         
    }    
}
