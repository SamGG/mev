/**
 * 
 */
package org.tigr.microarray.mev.sampleannotation;

/**
 * @author sarita
 *
 */
public interface ISampleAnnotation {
   public void setAnnotation(String key, String value);
   public String getAnnotation(String Key);
}
