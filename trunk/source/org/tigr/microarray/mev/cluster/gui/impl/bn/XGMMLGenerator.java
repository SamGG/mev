/**
 * 
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
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
	public static String createHeader(String label, String cptFileLoc) {
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
		xgmml_header += "  <att name='backgroundColor' value='#ccccff'/>" + lineSep;
		xgmml_header += "  <att name='layout' value='grid'/>" + lineSep;
		xgmml_header += "  <att name='cpt' value='" + cptFileLoc.trim() + "'/>" + lineSep;
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
		String nodeType = "string";
		//node = "  <node label='" + NM_001964 + " 'id='" + -1 + "'>";
		node = "  <node label='" + label + "' id='" + id + "' >" + lineSep;
		//EH
		String[] fieldNames = data.getFieldNames();//MevAnnotation.getFieldNames();
		for(int i = 0; i < fieldNames.length; i++) {
			String _tmp[] = data.getElementAnnotation(rowInd, fieldNames[i]);
			if(_tmp[0].trim().equalsIgnoreCase("na")) {
				_tmp[0] = "";
			} else {
				_tmp[0] = replaceBadChar(_tmp[0]);
			}
			//AnnoAttributeObj annoObj =  data.getElementAnnotationObject(rowInd, fieldNames[i]);
			//System.out.println("Annotation from data.getElementAnnotation() & annoObj.getAttributeAt(): " + _tmp[0] + " : " /*+ (String)annoObj.getAttributeAt(0)*/);
			if(fieldNames[i].equals("GENE_SYMBOL") 
					|| fieldNames[i].equals("GENE_TITLE")
					|| fieldNames[i].equals("ENTREZ_ID")
					|| fieldNames[i].equals("GENBANK_ACC")
					)
				nodeType = "list";
			node += createNodeAttribute(fieldNames[i], _tmp[0], nodeType);
			nodeType = "string";
		}
		node += "    <graphics type='ELLIPSE' width='2' fill='#FFCC99' outline='#CC9900' >" + lineSep;
		node += "    	<att name='cytoscapeNodeGraphicsAttributes'>" + lineSep;
		node += "    		<att name='nodeLabelFont' value='Default-0-12'/>" + lineSep;
		node += "    		<att name='borderLineType' value='solid'/>" + lineSep;
		node += "    	</att>" + lineSep;
		node += "    </graphics>" + lineSep;
		node += "  </node>" + lineSep;
		return node;
	}

	/**
	 * Replaces char ' that XML cannot handle if not escaped properly
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
	 * @param nodeType 
	 * @return
	 */
	public static String createNodeAttribute(String attribName, String attribValue, String nodeType) {
		String attrib = "";
		if(nodeType.equals("list")) {
			attrib = "    <att type='list' name='" + attribName + "' label='" + attribName + "'>" + lineSep;
			
			StringTokenizer tokens = new StringTokenizer(attribValue, "///");
	    	while(tokens.hasMoreTokens()){
	    		attrib += "    	<att type='string' value='" + tokens.nextToken().trim() + "'/>" + lineSep;
	    	}
			attrib += "    </att>" + lineSep;
		} else {
			attrib = "    <att type='string' name='" + attribName + "' value='" + attribValue + "'/>" + lineSep;	
		}

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
		edge += "    <att type='string' name='interaction' value='pd'/>" + lineSep;
		edge += "    <graphics width='2' fill='#999999' cy:sourceArrow='0' cy:targetArrow='3' cy:sourceArrowColor='#cdcdc1' cy:targetArrowColor='#333333' cy:edgeLabelFont='Default-0-10' cy:edgeLineType='SOLID' cy:curved='STRAIGHT_LINES'/>" + lineSep;
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
