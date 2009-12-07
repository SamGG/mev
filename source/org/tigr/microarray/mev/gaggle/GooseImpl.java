package org.tigr.microarray.mev.gaggle;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.*;
import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevChipAnnotation;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.GenomeBrowserWebstart;


public class GooseImpl implements Goose, GaggleConnectionListener {
	
	String myGaggleName = GaggleConstants.ORIGINAL_GAGGLE_NAME;
	String genomeBrowserGoose;
	String[] gooseNames;
	private boolean isConnected = false;
	String targetGoose = "Boss";
	RmiGaggleConnector gaggleConnector;
	Boss gaggleBoss;
	GaggleListener listener;
	
	public GooseImpl() {
	}
	
	public void setListener(GaggleListener listener) {
		this.listener = listener;
	}
		
	public String getTargetGoose() {
		return targetGoose;
	}
	public void setTargetGoose(String targetGoose) {
		this.targetGoose = targetGoose;
	}
	public String getMyGaggleName() {
		return myGaggleName;
	}
	public void setMyGaggleName(String myGaggleName) {
		this.myGaggleName = myGaggleName;
	}
	
	public String[] getGooseNames() {
		return gooseNames;
	}
	public void setGooseNames(String[] gooseNames) {
		this.gooseNames = gooseNames;
		updateGenomeBrowserGoose();
	}
	
	private void updateGenomeBrowserGoose() {
		if(genomeBrowserGoose != null) {
			for(String gooseName : gooseNames)
				if(genomeBrowserGoose.equals(gooseName))
					return;
			genomeBrowserGoose = null;
		}
    	for(int i=0; i<gooseNames.length; i++) {
    		if(gooseNames[i].startsWith("Genome Browser"))
    			genomeBrowserGoose = gooseNames[i];
    	}
	}
	
	public String getGenomeBrowserGoose(String species) {
    	while(genomeBrowserGoose == null) {
			GenomeBrowserWebstart.onWebstartGenomeBrowser(species);
			if(JOptionPane.showConfirmDialog(new JFrame(), 
					"Could not find a connected Genome Browser.\n" +
					"MeV will launch an instance of the Genome Browser.\n" +
	        		"Please load the genome of the organism of interest.\n" +
	        		"Click Ok when the genome is loaded to begin the broadcast.\n" +
	        		"See http://mev.tm4.org/documentation/gaggle for help", 
	        		"Waiting for Genome Browser",
	        		JOptionPane.OK_CANCEL_OPTION, 
	        		JOptionPane.QUESTION_MESSAGE)
	        	!= JOptionPane.OK_OPTION) {
				return null;
			}
			updateGenomeBrowserGoose();
    	}
		return genomeBrowserGoose;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	

    /**
     * @author eleanora
     * @param matrix
     */
    public void doBroadcastMatrix(DataMatrix matrix, String target) {
    	if(!isConnected()) {
    		gaggleConnectWarning();
    		return;
    	}
    	if(target == null)
    		target = this.targetGoose;
		try {	//here is where an exception is thrown if gaggle is not connected. 
			gaggleBoss.broadcastMatrix(myGaggleName, target, matrix);
		} catch (RemoteException rex) {
			JOptionPane.showMessageDialog(new JFrame(), "Gaggle unavailable. Please use Utilities -> Connect to Gaggle.");
			disconnectFromGaggle();
		}
	}
    /**
     * @author eleanora
     */
    public void disconnectFromGaggle() {
    	if(isConnected())
    		gaggleConnector.disconnectFromGaggle(true);

    }

    public void gaggleInit(){
    	if(gaggleConnector == null) {
	        gaggleConnector = new RmiGaggleConnector(this);
	    	gaggleConnector.setAutoStartBoss(true);
	        new GooseShutdownHook(gaggleConnector);
	        gaggleConnector.addListener(this);
    	}
	}
    /**
     * @author eleanora
     * @return
     */
    public boolean connectToGaggle() {
    	TMEV.GAGGLE_CONNECT_ON_STARTUP = true;
    	if(gaggleConnector == null) {
    		gaggleInit();
    	}
        try {
            gaggleConnector.connectToGaggle();
        } catch (Exception ex0) {
            //System.err.println("MAV.connectToGaggle(): Failed to connect to gaggle: " + ex0.getMessage());
        }
        gaggleBoss = gaggleConnector.getBoss();
        if(gaggleBoss != null) {
	        return true;
        } else {
        	//System.out.println("MAV.connectToGaggle(): Couldn't connect to Gaggle");
			//JOptionPane.showMessageDialog(mainframe, "Gaggle unavailable.");
        	return false;
        }
    }
    /**
     * @author eleanora
     * @param nt
     */
    public void doBroadcastNetwork(Network nt) {
    	if(!isConnected()) {
    		gaggleConnectWarning();
    		return;
    	}
    	try {
    		gaggleBoss.broadcastNetwork(myGaggleName, targetGoose, nt);
    	} catch (RemoteException rex) {
    		System.err.println("doBroadcastNetwork: rmi error calling boss.broadcast");
			disconnectFromGaggle();
    	}
    }
    /**
     * @author eleanora
     * @param nl
     */
    public void doBroadcastNamelist(Namelist nl){
    	if(!isConnected()) {
    		gaggleConnectWarning();
    		return;
    	}
    	try {
    		gaggleBoss.broadcastNamelist(myGaggleName, targetGoose, nl);
    	} catch (RemoteException rex) {
    		System.err.println("doBroadcastNamelist: rmi error calling boss.broadcast");
			disconnectFromGaggle();
    	}
    }
    public void show(String key) throws RemoteException {
    	gaggleBoss.show(key);
    }
    
    protected void gaggleConnectWarning() {
		String title = "Not connected to Gaggle";
		String msg = "Please connect to Gaggle using the Utilities -> Gaggle menu.";
		JOptionPane.showMessageDialog(new JFrame(), msg, title, JOptionPane.OK_OPTION);
		disconnectFromGaggle();
    }

	/**
	 * Taken from Paul Shannon's MeV 3.1 Goose implementation
	 * His notes are below: 
	 * this, the GaggledMev implementation of Goose.handleMatrix () is inspired by the
	 * org/tigr/microarray/mev/file/StanfordFileLoader class.
	 * it reads a file of (typically) log10 ratio values, and returns a vector version
	 * of an array it constructs out of SlideData objects, one for each column found
	 * in the incoming data.  
	 *
	 *  ---- ISlideData [] slideDataArray = new ISlideData [experimentCount]
	 *       slideDataArray [0] = new SlideData (rRows == spotCount == # of genes, rColumn=1);
	 *       for (int i=1; i < slideDataArray.length; i++) {
	 *          slideDataArray[i] = new FloatSlideData (slideDataArray[0].getSlideMetaData(), spotCount);
	 *   
	 *  the above suggests that the 0th slideDataArray element is metadata
	 *  and that 1-n+1 elements are the actual data
	 *
	 *    int experimentCount = ss.countTokens () + 1 - preExperimentColumns;  // numerical columns + 1
	 *    slideDataArray = new ISlideData [experimentCount];
	 *
	 *  upon reading first row of file -- the title line -- these things occur,
	 *  creating & initializing a structure to hold a column's worth (a condition) of data
	 *
	 *     slideDataArray = new ISlideData [experimentCount];
	 *     slideDataArray [0] = new SlideData (rRows == spotCount == # of genes, rColumn=1);
	 *     slideDataArray [0].setSlideFileName (f.getPath());
	 *     for (int i=1; i < slideDataArray.length; i++) {
	 *       slideDataArray[i] = new FloatSlideData (slideDataArray[0].getSlideMetaData(), spotCount);
	 *       slideDataArray[i].setSlideFileName (f.getPath());
	 *       }
	 *
	 *  then, looping through all rows in the input matrix (or file) these things occur:
	 *    a  SlideDataElement 'sde' is created, and added to SlideDataArray [0]
	 *    i am not sure what this accomplishes
	 *
	 *  then looping through the columns, 
	 *     slideDataArray [columnNumber].setIntensities (rowNumber, cy3=0, cy5=ration)
	 *
	 * SlideDataElement sde:  constructed with these arguments:
	 *           String UID 
	 *           int [] rows
	 *           int [] columns
	 *           float [] intensities
	 *           String [] values)
	 *
	 * Vector slideDataList: a vector form of the slideDataArray
	 */
	public void handleMatrix(String sourceGoose, DataMatrix matrix)
			throws RemoteException {

		IChipAnnotation chipAnno = new MevChipAnnotation();
		chipAnno.setSpeciesName(matrix.getSpecies());
		
		Tuple metaData = matrix.getMetadata();
		@SuppressWarnings("unused")
		String identifier;
		@SuppressWarnings("unused")
		String logStatus;
		int dataType = IData.DATA_TYPE_AFFY_ABS;
		if (metaData != null) {
			List<Single> singles = metaData.getSingleList();
			for (Single thisSingle : singles) {
				if (thisSingle.getName().equals(GaggleConstants.MEV_METADATA)) {
					try {
						Tuple mevMetaData = (Tuple) thisSingle.getValue();
						List<Single> items = mevMetaData.getSingleList();
						for (Single item : items) {
							String itemname = item.getName();
							if (itemname.equals(GaggleConstants.DATA_TYPE)) {
								String untranslatedDataType = item.getValue().toString();
								dataType = GaggleTranslater.translateDataType(untranslatedDataType);
								chipAnno.setDataType(new Integer(dataType)
										.toString());
							}
							if (itemname.equals(GaggleConstants.ARRAY_NAME)) {
								chipAnno
										.setChipName(item.getValue().toString());
								chipAnno
										.setChipType(item.getValue().toString());
							}
							if (itemname.equals(GaggleConstants.LOG_STATUS))
								logStatus = item.getValue().toString();
						}
					} catch (ClassCastException cce) {
						// Someone put the wrong thing into this location
					}
				}
				if (thisSingle.getName()
						.equals(GaggleConstants.IDENTIFIER_TYPE)) {
					identifier = thisSingle.getValue().toString();
				}
			}
		}
		float cy3, cy5;
		String[] moreFields = new String[1];
		final int rColumns = 1;

		int row, column;
		row = column = 1;

		// ----------------------------------
		// make header assignments
		// ----------------------------------

		int experimentCount = matrix.getColumnCount(); // no kidding!

		// each element slideDataArray seems to be storage for one column of
		// data

		ISlideData[] slideDataArray = new ISlideData[experimentCount];
		slideDataArray[0] = new SlideData(matrix.getRowCount(), 1);
		slideDataArray[0].setSlideFileName("Broadcast via Gaggle from "
				+ sourceGoose + " " + matrix.getShortName());
		for (int i = 1; i < experimentCount; i++) {
			slideDataArray[i] = new FloatSlideData(slideDataArray[0]
					.getSlideMetaData(), matrix.getRowCount());
			slideDataArray[i].setSlideFileName("Broadcast via Gaggle from "
					+ sourceGoose + " " + matrix.getShortName());
		} // for i

		// get Field Names
		String[] fieldNames = new String[1];
		fieldNames[0] = matrix.getRowTitlesTitle();

		if (fieldNames == null || fieldNames[0] == null)
			fieldNames = new String[] { "untitled annotation" };
		slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);
		for (int i = 0; i < experimentCount; i++) {
			slideDataArray[i].setSlideDataName(matrix.getColumnTitles()[i]);
		}
		// ----------------------------------
		// assign the data
		// ----------------------------------

		double matrixData[][] = matrix.get();
		String[] rowTitles = matrix.getRowTitles();
		double maxval = Double.NEGATIVE_INFINITY, minval = Double.POSITIVE_INFINITY;

		for (int r = 0; r < matrix.getRowCount(); r++) {
			int[] rows = new int[] { 0, 1, 0 };
			int[] columns = new int[] { 0, 1, 0 };
			rows[0] = rows[2] = row;
			columns[0] = columns[2] = column;
			if (column == rColumns) {
				column = 1;
				row++;
			} else {
				column++;
			}

			moreFields[0] = rowTitles[r];
			ISlideDataElement sde;

			sde = new SlideDataElement(
					String.valueOf(row + 1), rows, columns, new float[2],
					moreFields);
			if(dataType == IData.DATA_TYPE_AFFY_ABS) 
				sde = new AffySlideDataElement(sde);
			
			slideDataArray[0].addSlideDataElement(sde);

			for (int i = 0; i < slideDataArray.length; i++) {
				cy3 = 1f; // set cy3 to a default value of 1.
				cy5 = (new Double(matrixData[r][i])).floatValue();
				slideDataArray[i].setIntensities(r, cy3, cy5);
				if (cy5 < minval)
					minval = cy5;
				if (cy5 > maxval)
					maxval = cy5;
			} // for i
		} // for r

		listener.expressionDataReceived(slideDataArray, chipAnno, dataType);
	}
	
	public void doExit() throws RemoteException {
		disconnectFromGaggle();
		listener.onExit();
	}
	public void doHide() throws RemoteException {
		listener.onHide();
	}
	public void doShow() throws RemoteException {
		listener.onShow();
	}
	
	public String getName() throws RemoteException {
		return myGaggleName;
	}
	
	public void handleNameList(String arg0, Namelist nl) throws RemoteException {
		String broadcastName = nl.getName();
		nl.getMetadata();
		Tuple metaData = nl.getMetadata();
		String identifier = null;
		boolean interactive = true;
		if(metaData != null) {
			List<Single> singles = metaData.getSingleList();
			for(Single thisSingle: singles) {
				if(thisSingle.getName().equals(GaggleConstants.MEV_METADATA)) {
					//Parse MEV metadata that's useful
				}
				if(thisSingle.getName().equals(GaggleConstants.IDENTIFIER_TYPE)) {
					identifier = thisSingle.getValue().toString();
				}
				try {
					if(thisSingle.getName().equals(GaggleConstants.INTERACTIVE)) {
						interactive = new Boolean(thisSingle.getValue().toString()).booleanValue();
					}
				} catch (Exception e) {
					//Any kind of parsing error should result in going interactive. 
					interactive = true;
				}
			}
		}
		
		listener.nameListReceived(nl.getNames(), identifier, interactive, broadcastName);
	}

	public void setName(String newGaggleName) throws RemoteException {
		this.myGaggleName = newGaggleName;
		listener.onNameChange(newGaggleName);
	}

	public void update(String[] gooseNames) throws RemoteException {
		setGooseNames(gooseNames);
	
    	Vector<String> menuNames = new Vector<String>();
    	menuNames.add("Boss");
    	for(int i=0; i<gooseNames.length; i++) {
    		if(!gooseNames[i].equals(myGaggleName))
    			menuNames.add(gooseNames[i]);
    	}
    	String[] temp = new String[menuNames.size()];
    	for(int i=0; i<temp.length; i++) {
        	temp[i] = (String)menuNames.get(i);
    	}
    	
		listener.onUpdate(temp);
	}
    
	/**
	 * This method is for the GaggleConnectionListener implementation.
	 * @author eleanora
	 * @param connected
	 * @param boss
	 */
    public void setConnected(boolean connected, Boss boss) {
    	this.isConnected = connected;
    	gaggleBoss = boss;
        if(isConnected) {
	        try {
	        } catch (NullPointerException npe) {
	        	//Suppress exception if menubar doesn't exist.
	        }
        } else {
    	    myGaggleName = GaggleConstants.ORIGINAL_GAGGLE_NAME;
        }
        listener.onUpdateConnected(connected);
    }

	/**
	 * Implemented to satisfy the Goose interface
	 */
	public void handleCluster(String arg0,
			org.systemsbiology.gaggle.core.datatypes.Cluster arg1)
			throws RemoteException {
		//MeV ignores incoming Cluster broadcasts
	}
	/**
	 * Implemented to satisfy the Goose interface
	 */
	public void handleNetwork(String arg0, Network arg1) throws RemoteException {
		//MeV does not handle Networks. Incoming network broadcasts are ignored.
	}
	/**
	 * Implemented to satisfy the Goose interface
	 */
	public void handleTuple(String arg0, GaggleTuple arg1)
			throws RemoteException {
		//MeV ignores incoming Tuple broadcasts
	}
	/**
	 * Implemented to satisfy the Goose interface
	 */
	public void handleCluster(String arg0, Cluster arg1) throws RemoteException {
		//MeV ignores incoming Cluster broadcasts. 
	}
	/**
	 * Implemented to satisfy the Goose interface
	 */
	public void doBroadcastList() throws RemoteException {}
}
