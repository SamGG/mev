/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AbstractAlgorithm.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

import javax.swing.event.EventListenerList;

/**
 * This abstract class provides default implementations for some
 * of the methods in the Algorithm interface. It takes care of the
 * management of listeners and provides some conveniences for
 * generating AlgorithmEvents and dispatching them to the listeners.
 * To create a concrete Algorithm as a subclass of AbstractAlgorithm
 * programmer need only provide implementations for the following two
 * methods:
 * <pre>
 * public AlgorithmData execute(AlgorithmData data) throws AlgorithmException;
 * public void abort();
 * </pre>
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public abstract class AbstractAlgorithm implements Algorithm {
    
    /** List of listeners */
    private EventListenerList listenerList = new EventListenerList();
    
	/*
	 *	Returns a string representation of the argument distance metric
	 */
    public static String getDistanceName(int function) {
	String functionName = "";
	
	switch (function) {
	    case Algorithm.DEFAULT:
		functionName = "Pearson correlation";
		break;
	    case Algorithm.PEARSON:
		functionName = "Pearson correlation";
		break;
	    case Algorithm.COSINE:
		functionName = "Cosine correlation";
		break;
	    case Algorithm.COVARIANCE:
		functionName = "Covariance";
		break;
	    case Algorithm.EUCLIDEAN:
		functionName = "Euclidean";
		break;
	    case Algorithm.DOTPRODUCT:
		functionName = "Dot product";
		break;
	    case Algorithm.PEARSONUNCENTERED:
		functionName = "Pearson uncentered";
		break;
	    case Algorithm.PEARSONSQARED:
		functionName = "Pearson squared";
		break;
	    case Algorithm.MANHATTAN:
		functionName = "Manhattan";
		break;
	    case Algorithm.SPEARMANRANK:
		functionName = "Spearman rank";
		break;
	    case Algorithm.KENDALLSTAU:
		functionName = "Kendall's Tau";
		break;
	    case Algorithm.MUTUALINFORMATION:
		functionName = "Mutual information";
		break;
	    default:
		functionName = "Undefined";
		break;
	}
	
	return functionName;
    }
    
    /**
     * Adds a listener to the list.
     * @param l the <code>AlgorithmListener</code>.
     */
    public void addAlgorithmListener(AlgorithmListener l) {
	listenerList.add(AlgorithmListener.class, l);
    }
    
    /**
     * Removes a listener from the list.
     * @param l the <code>AlgorithmListener</code>.
     */
    public void removeAlgorithmListener(AlgorithmListener l) {
	listenerList.remove(AlgorithmListener.class, l);
    }
    
    /**
     * Notifies all listeners that the algorithm's event has occured.
     * @param event the <code>AlgorithmEvent</code>.
     */
    public void fireValueChanged(AlgorithmEvent event) {
	Object[] listeners = listenerList.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==AlgorithmListener.class) {
		((AlgorithmListener)listeners[i+1]).valueChanged(event);
	    }
	}
    } 
}
