/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;
import java.util.ArrayList;

import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevChipAnnotation;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.util.FloatMatrix;


public class MultipleArrayDataPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		MultipleArrayData mad = (MultipleArrayData) oldInstance;

		Expression e = new Expression((MultipleArrayData)oldInstance, oldInstance.getClass(), "new",
				new Object[]{ 
						mad.getExperiment(), 
						new Boolean(mad.getUseMainData()), 
						mad.getAlternateExperiment(), 
						new Float(mad.getPercentageCutoff()), 
						new Boolean(mad.isPercentageCutoff()), 
						new Boolean(mad.isVarianceFilter()), 
						new Boolean(mad.isDetectionFilter()), 
						new Boolean(mad.isFoldFilter()),
						new Boolean(mad.getdfSet()), 
						new Boolean(mad.getffSet()), 
						mad.getDetectionFilter(), 
						mad.getFoldFilter(), 
						new Boolean(mad.areMedianIntensities()), 
						new Boolean(mad.isLowerCutoffs()), 
						new Float(mad.getLowerCY3Cutoff()), 
						new Float(mad.getLowerCY5Cutoff()), 
						mad.getExperimentColorsSaved(), 
						mad.getSpotColors(), 
						mad.getCurrentSampleLabelKey(),
						mad.getFeaturesList(),
						new Integer(mad.getDataType()),
						//Raktim CGH variables. 04/11
						mad.getSamplesOrder(),/*AsList()*/
						new Boolean(mad.isHasDyeSwap()),
						new Boolean(mad.isCGHData()),
						new Boolean(mad.isLog2Data()),
						mad.getClones(),
						new Integer(mad.getCGHSpecies()),
						//Raktim. 10/01.
						mad.getChromosomeIndices(),/* n x 3 Dim Array of ints */
						new Integer(mad.getCloneValueType()),
						//new Integer(mad.getLogState()),
						mad.getMultipleArrayDataState(),
						mad.getChipAnnotation()
						});
		return e;
	}

	public void initialize(Class type, Object oldInstance, Object newInstance, XMLEncoder encoder){
		;
	}
}