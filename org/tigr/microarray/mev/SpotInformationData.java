/*
 * SpotInfomationData.java
 *
 * Created on July 1, 2003, 10:13 AM
 */

package org.tigr.microarray.mev;

public class SpotInformationData implements java.io.Serializable {

    private String [] columnHeaders;
    private String [][] spotData;
    
    /** Creates new SpotInfomationData */
    public SpotInformationData(String [] columnHeaders, String [][] spotData) {
        this.columnHeaders = columnHeaders;
        this.spotData = spotData;
    }
    
    public String [] getSpotInformationArray(int index){
        if(index < 0 || index > spotData.length)
            return null;
        else 
            return spotData[index];
    }
  
    public String getSpotInformation(String key, int index){
        int columnIndex = getColumnIndex(key);
        if(isLegalColumn(columnIndex))
            return spotData[index][columnIndex];
        else
            return null;
    }
    
    private int getColumnIndex(String key){
        for(int i = 0; i < columnHeaders.length; i++){
            if(columnHeaders[i].equalsIgnoreCase(key))
                return i;
        }
        return -1;
    }
    
    private boolean isLegalColumn(int col){
        return (col >= 0 && col < spotData[0].length); 
    }
    
    public String [] getSpotInformationHeader(){
        return columnHeaders;
    }
}
