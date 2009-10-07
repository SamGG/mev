package org.tigr.microarray.mev.gaggle;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.annotation.IChipAnnotation;

public interface GaggleListener {

	
	public void onUpdate(String[] gooseNames);
	public void onShow();
	public void onHide();
	public void onExit();
	public void onNameChange(String newGooseName);
	public void onUpdateConnected(boolean isConnected);
	public void nameListReceived(String[] names, String identifier, boolean interactive, String label);
	public void expressionDataReceived(ISlideData[] slideDataArray, IChipAnnotation chipAnno, int dataType);
}
