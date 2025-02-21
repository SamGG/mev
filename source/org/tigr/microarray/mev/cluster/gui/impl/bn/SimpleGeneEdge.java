/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import org.tigr.microarray.mev.annotation.IAnnotation;

/**
 * The class <code>SimpleGeneEdge</code> contains a simple gene edge object containing from, to and weight fields
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 * @author raktim
 * @see GeneEdge
 */
public class SimpleGeneEdge extends GeneEdge {
	/**
	 * The variable <code>weight</code> denotes the weight of an edge
	 */
	double weight = 1.0;

	/**
	 * Provides access to annotation for the nodes
	 */
	IAnnotation annoFrom, annoTo;

	SimpleGeneEdge(String from, String to, IAnnotation frmA, IAnnotation toA) {
		this.from = from;
		this.to = to;
		this.annoFrom = frmA;
		this.annoTo = toA;
	}
	
	/**
	 * Creates a new <code>SimpleGeneEdge</code> instance given
	 *
	 * @param from a <code>String</code> corresponding to the start node
	 * @param to a <code>String</code> corresponding to the end node
	 * Note: weight is 0 by default
	 */
	public SimpleGeneEdge(String from, String to){
		createGeneEdge(from,to);
	}

	/**
	 * Creates a new <code>SimpleGeneEdge</code> instance
	 *
	 * @param from a <code>String</code> corresponding to the start node
	 * @param to a <code>String</code> corresponding to the end node
	 * @param weight a <code>double</code> corresponding to the weight of this edge
	 */
	public SimpleGeneEdge(String from, String to, double weight){
		createGeneEdge(from,to);
		this.weight = weight;
	}

	/**
	 * The <code>toString</code> method returns the String representation of this SimpleGeneEdge object
	 *
	 * @return a <code>String</code> representation of this SimpleGeneEdge object in the format: (from,to)=weight
	 */
	public String toString(){
		String result = "("+from+","+to+")="+weight;
		return result;
	}

	/**
	 * The <code>getWeight</code> method returns the weight of this edge
	 *
	 * @return a <code>double</code> corresponding to the weight of this edge
	 */
	public double getWeight(){
		return this.weight;
	}

	public String getEdgeAsString(String edgeConnector){
		return from + " " + edgeConnector + " " + to;
	}
	/**
	 * The <code>equals</code> method checks whether the given SimpleGeneEdge is equal to this SimpleGeneEdge
	 *
	 * @param anotherObject an <code>Object</code> to be compared with this SimpleGeneEdge
	 * @return a <code>boolean</code> returns true if the given SimpleGeneEdge is equal to this SimpleGeneEdge
	 * and false otherwise 
	 */
	public boolean equals(Object anotherObject){
		SimpleGeneEdge edge = (SimpleGeneEdge) anotherObject;
		if(edge.getFrom().equals(this.from) && edge.getTo().equals(this.to)){
			return true;
		}
		return false;
	}
	
	public boolean reverseEquals(Object anotherObject) {
		SimpleGeneEdge edge = (SimpleGeneEdge) anotherObject;
		if(edge.getFrom().equals(this.to) && edge.getTo().equals(this.from)){
			return true;
		}
		return false;
	}
	
	/**
	 * The <code>compareTo</code> method compares the given SimpleGeneEdge with this SimpleGeneEdge using weight
	 *
	 * @param obj an <code>Object</code> to be compared with this SimpleGeneEdge
	 * @return an <code>int</code> returns -1 if this SimpleGeneEdge's weight is less than the given SimpleGeneEdge weight
	 *                             returns 0 if this SimpleGeneEdge's weight and the given SimpleGeneEdge weight are equal
	 *                             returns 1 otherwise
	 */
	public int compareTo(Object obj){
		SimpleGeneEdge another = (SimpleGeneEdge) obj;
		if(weight < another.getWeight()){
			return -1;
		}
		else if(weight == another.getWeight()){
			return 0;
		}
		else {
			return 1;
		}
	}
	
	public IAnnotation getFromNodeAnntation() {
		return this.annoFrom;
	}
	
	public IAnnotation getToNodeAnntation() {
		return this.annoTo;
	}
	
	public void setFromNodeAnntation(IAnnotation fr) {
		this.annoFrom = fr;
	}
	
	public void setToNodeAnntation(IAnnotation to) {
		this.annoTo = to;
	}
}




