package org.tigr.microarray.mev;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.*;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.MevAnnotation;

public class TestMultipleArrayData {
	static MultipleArrayData data;
	static final String FIELD_NAME_1 = "field name 1";
	static final String FIELD_NAME_2 = "field name 2";
	
	@BeforeClass
	/**
	 * Creates a MultipleArrayData and fills it with fake data for testing.
	 */
	public static void beforeClass() {
		data = new MultipleArrayData();

		int geneNumber = 5;
		int expNumber = 4;
		
		assert(data.isAnnotationLoaded() == false);
		ArrayList<ISlideData> features = new ArrayList<ISlideData>();
		ISlideData sd = new SlideData();
		sd.getSlideMetaData().setFieldNames(new String[] {FIELD_NAME_1, FIELD_NAME_2});
		for(int i = 0; i < geneNumber; i++) {
			MevAnnotation ann = new MevAnnotation();
			ann.setCloneID("clone " + i);
			AffySlideDataElement asde = new AffySlideDataElement("test " + i, new int[] {1,2}, new int[] {2,1}, new float[] {1.5f, 2.5f}, new String[] {"testann " + i, "moretestann" + i}, ann);
			sd.addSlideDataElement(asde);
		}
		features.add(sd);
		
		for(int i=0; i<expNumber; i++) { //add four items to the Features set of slidedata, bringing the size to 5
			FloatSlideData fsd = new FloatSlideData(sd.getSlideMetaData());
			for(int j=0; j<geneNumber; j++) {
				fsd.setIntensities(j, .2f, -.2f);
			}
			features.add(fsd);
		}	
		data.setFeaturesList(features);
		
	}
	
	@Test
	/**
	 * Tests MultipleArrayData.getFieldNames(). This method should currently only return the field names for the annotation
	 * loaded into the old annotation model. 
	 */

	public void testGetFieldNames() {
		String[] allFilledAnnotationFields = data.getFieldNames();
		if(!allFilledAnnotationFields[0].equals(FIELD_NAME_1))
				fail("getAllFilledAnnotationFields()[0] is " + allFilledAnnotationFields[0]);
		if(!allFilledAnnotationFields[1].equals(FIELD_NAME_2))
			fail("getAllFilledAnnotationFields()[1] is " + allFilledAnnotationFields[1]);
		if(!allFilledAnnotationFields[2].equals(AnnotationFieldConstants.PROBE_ID))
			fail("getAllFilledAnnotationFields()[2] is " + allFilledAnnotationFields[2]);
		
	}
	
	@Test
	public void testFeaturesSize() {
		assert(data.getFeaturesCount() == 5);
	}
	
	@Test
	public void testExperimentSize() {
		assert(data.getFeaturesCount() == 4);
	}
}
