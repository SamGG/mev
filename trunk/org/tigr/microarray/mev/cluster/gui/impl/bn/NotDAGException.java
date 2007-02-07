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
/* NotDAGException.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
/**
 * The class <code>NotDAGException</code> represents an exception to be thrown in a method when a given graph is not a DAG
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 * @see Exception
 */
public class NotDAGException extends Exception {
    /**
     * Creates a new <code>NotDAGException</code> instance without a detailed message
     *
     */
    public NotDAGException(){
    }
    /**
     * Creates a new <code>NotDAGException</code> instance with the given detailed message
     *
     * @param message a <code>String</code> corresponding to the given detailed message
     */
    public NotDAGException(String message){
	super(message);
    }
}
