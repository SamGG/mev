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
/* GeneEdgeWithOtherInfo.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.util.Collection;import java.util.Iterator;
/**
 * The class <code>GeneEdgeWithOtherInfo</code> contains a gene edge object containing from, to, 
 * weight fields and other information as an ArrayList 
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 * @see GeneEdge
 */
public class GeneEdgeWithOtherInfo extends GeneEdge {
    /**
     * The variable <code>weight</code> denotes the weight of an edge
     */
    double weight = 0;
    /**
     * The variable <code>otherInfo</code> contains other information for this edge as an Collection
     */
    Collection otherInfo = null;
    /**
     * Creates a new <code>GeneEdgeWithOtherInfo</code> instance
     *
     * @param from a <code>String</code> corresponding to the start node
     * @param to a <code>String</code> corresponding to the end node
     * @param weight a <code>double</code> corresponding to the weight of this edge
     * @param inOtherInfo an <code>Collection</code> corresponding to other information
     */
    public GeneEdgeWithOtherInfo (String from, String to, double weight, Collection inOtherInfo){
	super.createGeneEdge(from,to);
	weight = weight;
	if(inOtherInfo!=null){
	    this.setOtherInfo(inOtherInfo);
	}
    }
    /**
     * The <code>toString</code> method returns the String representation of this GeneEdgeWithOtherInfo object
     *
     * @return a <code>String</code> representation of this GeneEdgeWithOtherInfo object in the format: 
     * (from,to)=weight and then toString() representation of each object in the other information space-separated
     */
    public String toString(){
	String result = "("+from+","+to+")="+weight;	
	if(otherInfo!=null){
	    Iterator it = otherInfo.iterator();
	    if(!otherInfo.isEmpty()){
		result += " ";
		while(it.hasNext()){
		    result += it.next().toString()+" ";
		}
	    }
	}
	return result;
    }
    /**
     * The <code>clone</code> method provides a deep copy of this object
     *
     * @return an <code>Object</code> corresponding to a deep copy of this object
     */
    public Object clone(){
	return (new GeneEdgeWithOtherInfo(from,to,weight,otherInfo));
    }
    /**
     * The <code>setOtherInfo</code> method sets other information for this GeneEdgeWithOtherInfo object
     *
     * @param inOtherInfo an <code>Collection</code> corresponding to other information for this GeneEdgeWithOtherInfo object
     */
    public void setOtherInfo(Collection inOtherInfo){
	Iterator it = inOtherInfo.iterator();
	this.otherInfo.add(it.next());
    }
    /**
     * The <code>getOtherInfo</code> method returns the other information associated with this GeneEdgeOtherInfo object
     *
     * @return an <code>Collection</code> corresponding to the other information associated with this GeneEdgeOtherInfo object
     */
    public Collection getOtherInfo(){
	return this.otherInfo;
    }
}

