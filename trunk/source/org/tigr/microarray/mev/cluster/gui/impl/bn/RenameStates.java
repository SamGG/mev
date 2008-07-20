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
import weka.core.Attribute;
import weka.core.Instances;
import java.util.ArrayList;
public class RenameStates {
    // Assumes first attribute is CLASS
    /**
     * The <code>renameStates</code> method takes in a WEKA Instances object 
     * corresponding to the data (in this application, gene expression data) discretized into a number of bins 
     * and returns a new WEKA Instances object with the names of the bins 
     * in the given data replaced by the given bin labels
     *
     * @param data an <code>Instances</code> which is a WEKA Instances object corresponding to the gene expression data
     * @param binLabels an <code>ArrayList</code> of <code>String</code> corresponding to the label of each bin.
     * @return an <code>Instances</code> a new WEKA Instances object with the names of the bins 
     * in the given data replaced by the given bin labels
     */
    public static Instances renameStates(Instances data, ArrayList binLabels){
	ArrayList al = new ArrayList();
	Attribute attr = null;
	for(int i = 1; i < data.numAttributes(); i++){
	    if(data.attribute(i).isNominal()){
		attr = data.attribute(i);
		for(int j = 0; j < attr.numValues(); j++){
		    data.renameAttributeValue(attr, attr.value(j), (String) binLabels.get(j));
		}
	    }
	}
	return data;
    }
}







