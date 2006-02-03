/*
 * CGHCloneComparator.java
 *
 * Created on October 21, 2005
 */
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
package org.tigr.microarray.mev.cgh.CGHDataGenerator;

import java.util.Comparator;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;

public class CGHCloneComparator implements Comparator{

    /** Creates a new instance of CGHCloneComparator */
    public CGHCloneComparator() {
    }

    public int compare(Object obj, Object obj1) {
    	//System.out.println("Compare");
        CGHClone clone1 = (CGHClone)obj;
        CGHClone clone2 = (CGHClone)obj1;

        Integer chr1 = new Integer(clone1.getChromosome());
        Integer chr2 = new Integer(clone2.getChromosome());

        Integer st1 = new Integer(clone1.getStart());
        Integer st2 = new Integer(clone2.getStart());

        if (clone1.equals(clone2)) {
        	return 0;
        }
        else if (chr1.equals(chr2) && st1.equals(st2)){
        	//System.out.println("Clone Equal: " + clone1.getName() + ", " + clone2.getName());
        	return -1;
        }
        else if (!chr1.equals(chr2)){
        	return chr1.compareTo(chr2);
        } else {
        	//return st1.compareTo(st2);

        	int val = st1.compareTo(st2);
        	/**
        	 * Raktim Oct 21, 05
        	 * This is a rare situation scenario.
        	 * If 2 or more probes map to the same genomic position
        	 * they would appear in the sorted list in the order they appear in the file.
        	 * Forcing it to be reported as a bigger value by the Comparator interface.
        	 * Otherwise it will appear as a subkey of the first value in the TreeMap.
        	 */

        	if (val == 0){ return -1; }
        	else
        	return val;


        }

        /*
         * Adam Implementation
        if(clone1.getChromosome() != clone2.getChromosome()){
            return clone1.getChromosome() - clone2.getChromosome();
        }else{
            return clone1.getStart() - clone2.getStart();
        }
        */
    }
}