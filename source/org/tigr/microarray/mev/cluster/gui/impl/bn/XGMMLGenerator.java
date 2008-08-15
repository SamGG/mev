/**
 * 
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * @author Raktim
 *
 */
public class XGMMLGenerator {
	private static String lineSep = System.getProperty("line.separator");
	/**
	 * 
	 */
	public XGMMLGenerator() {

	}

	/**
	 * Creates XGMML File Header
	 * @param label
	 * @return
	 */
	public static String createHeader(String label) {
		String xgmml_header = "";
		xgmml_header =  "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" + lineSep;
		xgmml_header += "<graph label='" + label + "' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:cy='http://www.cytoscape.org' xmlns='http://www.cs.rpi.edu/XGMML' >" + lineSep;
		xgmml_header += "  <att name='documentVersion' value='1.1'/>" + lineSep;
		xgmml_header += "  <att name='networkMetadata'>" + lineSep;
		xgmml_header += "    <rdf:RDF>" + lineSep;
		xgmml_header += "      <rdf:Description rdf:about='http://www.cytoscape.org/'>" + lineSep;
		xgmml_header += "        <dc:type>Protein-Protein Interaction</dc:type>" + lineSep;
		xgmml_header += "        <dc:description>N/A</dc:description>" + lineSep;
		xgmml_header += "        <dc:identifier>N/A</dc:identifier>" + lineSep;
		xgmml_header += "        <dc:date>2008-07-18 21:55:08</dc:date>" + lineSep;
		xgmml_header += "        <dc:title>Sample XGMML Format</dc:title>" + lineSep;
		xgmml_header += "        <dc:source>http://www.cytoscape.org/</dc:source>" + lineSep;
		xgmml_header += "        <dc:format>Cytoscape-XGMML</dc:format>" + lineSep;
		xgmml_header += "      </rdf:Description>" + lineSep;
		xgmml_header += "    </rdf:RDF>" + lineSep;
		xgmml_header += "  </att>" + lineSep;
		xgmml_header += "  <att type='string' name='backgroundColor' value='#ccccff'/>" + lineSep;
		xgmml_header += "  <att type='string' name='Layout' value='Hierarchical'/>" + lineSep;
		return xgmml_header;
	}

	/**
	 * Creates a XGMML network Node
	 * @param label
	 * @param id
	 * @param data
	 * @param rowInd
	 * @return
	 */
	public static String createNode(String label, String id, IData data, int rowInd) {
		String node = "";
		//node = "  <node label='" + NM_001964 + " 'id='" + -1 + "'>";
		node = "  <node label='" + label + "' id='" + id + "' >" + lineSep;
		String[] fieldNames = MevAnnotation.getFieldNames();
		for(int i = 0; i < fieldNames.length; i++) {
			String _tmp[] = data.getElementAnnotation(rowInd, fieldNames[i]);
			if(_tmp[0].trim().equalsIgnoreCase("na")) {
				_tmp[0] = "";
			} else {
				_tmp[0] = replaceBadChar(_tmp[0]);
			}
			//AnnoAttributeObj annoObj =  data.getElementAnnotationObject(rowInd, fieldNames[i]);
			//System.out.println("Annotation from data.getElementAnnotation() & annoObj.getAttributeAt(): " + _tmp[0] + " : " /*+ (String)annoObj.getAttributeAt(0)*/);
			node += createNodeAttribute(fieldNames[i], _tmp[0]);
		}
		node += "    <graphics type='ELLIPSE' fill='#CCFF99'/>" + lineSep;
		node += "  </node>" + lineSep;
		return node;
	}

	/**
	 * Replaces chars like ' that XML cannot handle if not escaped properly
	 * @param string
	 * @return
	 */
	private static String replaceBadChar(String string) {
		// Replaces chars like ' that XML cannot handle if not escaped properly
		string  = string.replace("'", "");
		return string;
	}

	/**
	 * Creates a XGMML attribute node
	 * @param attribName
	 * @param attribValue
	 * @return
	 */
	public static String createNodeAttribute(String attribName, String attribValue) {
		String attrib = "";
		attrib = "    <att type='string' name='" + attribName + "' value='" + attribValue + "'/>" + lineSep;
		return attrib;
	}

	/**
	 * creates a XGMML Edge
	 * @param srcLbl
	 * @param tgtLabel
	 * @param srcId
	 * @param tgtId
	 * @return
	 */
	public static String createEdge(String srcLbl, String tgtLabel, String srcId, String tgtId) {
		String edge = "";
		//edge = "  <edge label='" + NM_138957 + " (pd) " + NM_176795 + "' source='" + -9 +"' target='" + -5 + "'>";
		edge = "  <edge label='" + srcLbl + " (pd) " + tgtLabel + "' source='" + srcId +"' target='" + tgtId + "' weight='0'>" + lineSep;
		//edge += "    <att type='string' name='canonicalName' value='" + srcLbl + " (pd) " + tgtLabel + "'/>" + lineSep;
		//edge += "    <att type='string' name='interaction' value='pd'/>" + lineSep;
		//edge += "    <graphics width='1' fill='#0000ff' cy:sourceArrow='3' cy:targetArrow='0' cy:sourceArrowColor='#000000' cy:targetArrowColor='#000000' cy:edgeLabelFont='Default-0-10' cy:edgeLineType='SOLID' cy:curved='STRAIGHT_LINES'/>" + lineSep;
		edge += "  </edge>" + lineSep;
		return edge;
	}

	/**
	 * Creates a XGMML Footer
	 * @return
	 */
	public static String getFooter() {
		String footer = "";
		footer = "</graph>" + lineSep;
		return footer;
	}

	/**
	 * Writes a XGMML file to disk
	 * @param file
	 * @param content
	 * @throws IOException
	 */
	public static void writeFileXGMML(String file, String content) throws IOException {
		try {
			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(fos, true);
			pw.print(content);
			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			throw e;
		}
	}
}
