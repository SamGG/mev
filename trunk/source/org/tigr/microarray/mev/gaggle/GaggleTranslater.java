package org.tigr.microarray.mev.gaggle;

import java.util.Hashtable;
import java.util.Vector;


import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.Tuple;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.util.FloatMatrix;

public class GaggleTranslater {
	private MultipleArrayViewer mav;
	
	public GaggleTranslater(MultipleArrayViewer mav) {
		this.mav = mav;
	}

	public DataMatrix createMatrix(int[] rows, String fieldname, String species, String algorithmName) {
		
    	IData data = mav.getData();
    	DataMatrix m = new DataMatrix();
    	
    	FloatMatrix f = data.getExperiment().getMatrix();
    	String[] rowTitles = new String[rows.length];
    	m.setSize(rows.length, f.getColumnDimension());
    	for (int i=0; i<rows.length; i++) {
    		for (int j=0; j<f.getColumnDimension(); j++) {
                m.set(i, j, data.getRatio(j, rows[i], IData.LOG));
    		}
    	}
    	rowTitles = data.getAnnotationList(fieldname, rows);
    	m.setRowTitles(rowTitles);
    	m.setRowTitlesTitle(fieldname);
    	String[] temp = new String[f.getColumnDimension()];
    	for(int i=0; i<temp.length; i++) {
    		temp[i] = data.getSampleAnnotation(i, data.getCurrentSampleLabelKey());
    		if(temp[i] == null || temp[i].equalsIgnoreCase("na"))
    			temp[i] = "Sample " + i+1;
    	}
    	m.setColumnTitles(temp);
    	m.setSpecies(species);
    	m.setName ("MeV matrix (" + m.getRowCount() +  " x " + m.getColumnCount() +") from algorithm " + algorithmName);
    	m.setShortName ("MeV matrix (" + m.getRowCount() +  " x " + m.getColumnCount() +") from algorithm " + algorithmName);
    	m.setMetadata(getMatrixMetadata(fieldname, null, data.getDataType(), data.getChipAnnotation().getChipName(), algorithmName));
    	return m;
	    
	}


    public Namelist createNamelist(Experiment e, int[] rows) {
    	if(e == null || rows == null)
    		return new Namelist();
    	IData data = mav.getData();
    	int[] indices = new int[rows.length];
    	for(int i=0; i<rows.length; i++) {
    		indices[i] = e.getGeneIndexMappedToData(rows[i]);
    	}
        Namelist nl = new Namelist();
        String fieldName = data.getFieldNames()[mav.getMenubar().getDisplayMenu().getLabelIndex()];
    	String[] names = new String[indices.length];
    	for(int i=0; i<names.length; i++) {
    		names[i] = data.getAnnotationList(fieldName, new int[]{indices[i]})[0];
    	}
    	nl.setName("MeV Namelist (" + names.length + ")");
		nl.setNames(names);
    	nl.setSpecies(mav.getCurrentSpecies());
    	nl.setMetadata(getNamelistMetadata(fieldName, null));

    	return nl;
    }
    /**
     * Builds a Gaggle Network object from input params and broadcasts it to the Gaggle network.
     * Should be called by Network-broadcasting components.
     * @author eleanora
     */
    public Network createNetwork(Vector<int[]> interactions, Vector<String> types, Vector<Boolean> directionals) {
    	IData data = mav.getData();
		Network nt = new Network();		
    	nt.setSpecies(mav.getCurrentSpecies());
    	Hashtable<String, String[]> nodeAnnotations = new Hashtable<String, String[]>();
    	String[] allFields = data.getFieldNames();
    	for(int i=0; i<interactions.size(); i++) {
    		String source = data.getAnnotationList(data.getFieldNames()[mav.getMenubar().getDisplayMenu().getLabelIndex()], new int[]{interactions.get(i)[0]})[0];
    		String target = data.getAnnotationList(data.getFieldNames()[mav.getMenubar().getDisplayMenu().getLabelIndex()], new int[]{interactions.get(i)[1]})[0];
    		
    		Interaction tempInt = new Interaction(source, target, types.get(i), directionals.get(i));
    		
    		nt.add(tempInt);
    		
    		if(!nodeAnnotations.containsKey(source)) {
    			nodeAnnotations.put(source, new String[0]);
    			for(String field: allFields) {
        			nt.addNodeAttribute(source, field, data.getElementAnnotation(interactions.get(i)[0], field)[0]);
    			}
    		}
    		if(!nodeAnnotations.containsKey(target)) {
    			nodeAnnotations.put(target, new String[0]);
    			for(String field: allFields) {
    				nt.addNodeAttribute(target, field, data.getElementAnnotation(interactions.get(i)[1], field)[0]);
    			}
    		}
    	}

    	nt.setName("MeV Network (" + nt.getNodes().length + ")");
    	nt.setMetadata(getNetworkMetadata());
    	return nt;
    }
    private static Tuple getNamelistMetadata(String identifierType, String source) {
    	Tuple metaData = new Tuple();

    	Single geneIdentifier = new Single(GaggleConstants.IDENTIFIER_TYPE, identifierType);
    	metaData.addSingle(geneIdentifier);
    	
    	//TODO decide whether to include an meVDataValues Tuple if there are no items inside it.
    	Tuple meVDataValues = new Tuple();
    	if(source != null && !source.equals(""))
    		meVDataValues.addSingle(new Single(GaggleConstants.ALGORITHM_SOURCE, source));
    	
    	Single meVMetaData = new Single(GaggleConstants.MEV_METADATA, meVDataValues);

    	metaData.addSingle(meVMetaData);
    	
    	return metaData;
    }
	/**
	 * Creates a metadata Tuple for broadcasting with a Gaggle object. 
	 * @return
	 */
	private static Tuple getMatrixMetadata(String identifier, String logStatus, int dataType, String arrayName, String algorithmSource) {
		Tuple metaData = new Tuple();

		if(identifier != null && !identifier.equals(""))
			metaData.addSingle(new Single(GaggleConstants.IDENTIFIER_TYPE, identifier));
		
		Tuple meVDataValues = new Tuple();
		
		if(logStatus != null && !logStatus.equals("")) 
			meVDataValues.addSingle(new Single(GaggleConstants.LOG_STATUS, logStatus));
		
//		if(dataType != null && !dataType.equals("")) 
			meVDataValues.addSingle(new Single(GaggleConstants.DATA_TYPE, createGaggleDatatype(dataType)));
		
		if(arrayName != null && !arrayName.equals("")) 
			meVDataValues.addSingle(new Single(GaggleConstants.ARRAY_NAME, arrayName));
		
		if(algorithmSource != null && !algorithmSource.equals("")) 
			meVDataValues.addSingle(new Single(GaggleConstants.ALGORITHM_SOURCE, algorithmSource));
		
		Single meVMetaData = new Single(GaggleConstants.MEV_METADATA, meVDataValues);
	
		metaData.addSingle(meVMetaData);
		return metaData;
	}

    //TODO What needs to go in here? 
    private Tuple getNetworkMetadata() {
    	Tuple metaData = new Tuple();

//    	Single geneIdentifier = new Single(GaggleConstants.IDENTIFIER_TYPE, identifierType);
//    	metaData.addSingle(geneIdentifier);
    	
    	//TODO decide whether to include an meVDataValues Tuple if there are no items inside it.
    	Tuple meVDataValues = new Tuple();
//    	if(source != null && !source.equals(""))
//    		meVDataValues.addSingle(new Single(GaggleConstants.ALGORITHM_SOURCE, source));
    	
    	Single meVMetaData = new Single(GaggleConstants.MEV_METADATA, meVDataValues);

    	metaData.addSingle(meVMetaData);
    	
    	return metaData;
    }

	/**
	 * Converts the data type as stored in MeV's data model into a simpler
	 * data type string suitable for transmitting as Gaggle metadata.
	 * @param mevDataType
	 * @return a human-readable Gaggle-compatible data type string
	 */
	public static String createGaggleDatatype(int mevDataType) {
		if(mevDataType == IData.DATA_TYPE_TWO_INTENSITY ||
				mevDataType == IData.DATA_TYPE_RATIO_ONLY)
			return GaggleConstants.RATIO;
		return GaggleConstants.INTENSITY;
	}

	/**
	 * Translates a descriptive string indicating a data type into a 
	 * real data type suitable for putting into MeV's data model.
	 * @param untranslatedDataType
	 * @return an MeV datatype compatible with IData
	 */
	public static int translateDataType(String untranslatedDataType) {
		if(untranslatedDataType.equals(GaggleConstants.INTENSITY))
			return IData.DATA_TYPE_AFFY_ABS;
		if(untranslatedDataType.equals(GaggleConstants.RATIO))
			return IData.DATA_TYPE_RATIO_ONLY;
	
		return IData.DATA_TYPE_AFFY_ABS;
	}
}
