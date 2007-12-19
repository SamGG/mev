/**
 * 
 */
package org.tigr.microarray.mev.annotation;

/**
 * @author Raktim
 *
 */
public class AnnoAttributeObj {

	int attribCount;
	String attribName;
	String[] attribValue;
	
	AnnoAttributeObj(){
		attribCount = -1;
		attribName = "";
	}
	
	AnnoAttributeObj(String attribName, String[] attribVals){
		attribCount = attribVals.length;
		this.attribName = attribName;
		attribValue = attribVals;
	}
	
	public void setAttribute(String[] attribs) {
		attribValue = attribs;
	}
	
	public int getAttribCount(){
		return attribCount;
	}
	
		
	
	public Object getAttributeAt(int index){
		if (index <= attribCount-1)
			return attribValue[index];
		else 
			return null;
	}
	
	
	
	
	public String toString(){
		String _temp = new String();
		String delim = " | ";
		for(int i=0; i < attribCount; i++){
			_temp += attribValue[i];
			if ((i < attribCount-1)){
				_temp = _temp + delim;
			}
		}
		return _temp;
	}
}
