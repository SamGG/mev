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
/* GeneEdge.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
/**
 * The abstract class <code>GeneEdge</code> contains from and to fields and accessors and mutators.
 * The design decision to make this class abstract rather than an interface is based upon the fact 
 * that abstract classes can be modified and provide a default behavior without having to change 
 * all the extending classes which have a choice of overriding the default behavior or not
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public abstract class GeneEdge {
    /**
     * The variable <code>from</code> corresponds to the from node of the edge if it's a directed edge 
     * from <code>from</code> from to <code>to</code>
     */
    String from;
    /**
     * The variable <code>to</code> corresponds to the to node of the edge if it's a directed edge 
     * from <code>from</code> to <code>to</code>
     */
    String to;
    /**
     * Default constructor, does nothing
     *
     */
    public GeneEdge() {
    }
    /**
     * The <code>createGeneEdge</code> method creates a gene edge directed from the given <code>from</code> 
     * to the given <code>to</code>
     *
     * @param from a <code>String</code> corresponds to the from node of the edge if it's a directed edge
     * from <code>from</code> from to <code>to</code>
     * @param to a <code>String</code> corresponds to the to node of the edge if it's a directed edge 
     * from <code>from</code> to <code>to</code>
     */
    public void createGeneEdge(String from, String to){
	this.from = from;
	this.to = to;
    }
    /**
     * The <code>getFrom</code> method returns the start node of this edge
     *
     * @return a <code>String</code> corresponding to the start node of this edge
     */
    public String getFrom(){
	return this.from;
    }
    /**
     * The <code>getTo</code> method returns the end node of this edge
     *
     * @return a <code>String</code> corresponding to the end node of this edge
     */
    public String getTo(){
	return this.to;
    }
    /**
     * The <code>toString</code> method returns the String representation of this GeneEdgeWithOtherInfo object
     *
     * @return a <code>String</code> representation of this GeneEdge object to be implemented 
     * in a concrete class extending this abstract class
     */
    public abstract String toString();
}




