package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

public class BifDOMBuilder {

	private DocumentBuilder builder;

	public BifDOMBuilder()   {
		// TODO Auto-generated constructor stub
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			builder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			System.out.println("BifDOMBuilder() exception...");
			pce.printStackTrace();
		}
		System.out.println("BifDOMBuilder()");
	}

	public void build(String fileName_bif) throws SAXException, IOException {
		System.out.println("build()");
		Document document = builder.parse(fileName_bif);
		//NodeList nodes_i = document.getDocumentElement().getChildNodes();
		NodeList nodes_def = document.getElementsByTagName("DEFINITION");
		System.out.println("build()-- Total Def Nodes " + nodes_def.getLength());
		//System.out.println("build()-- Total Nodes " + nodes_i.getLength());
		for (int i = 0; i < nodes_def.getLength(); i++) {
			Node def = nodes_def.item(i);
			System.out.println("Node Name " + def.getNodeName());
			//Retreive all DEFINITION nodes
			//if (node_i.getNodeType() == Node.ELEMENT_NODE && ((Element) node_i).getTagName().equals("DEFINITION")) {
				System.out.println("DEF Node");
				//Element def = (Element) node_i;
				//Retreive all FOR and GIVEN nodes
				NodeList nodes_j = def.getChildNodes();
				for (int j = 0; j < nodes_j.getLength(); j++) {
					Node node_j = nodes_j.item(j);
					//Node is FOR (child, count 1)
					if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("FOR")) {
						//Set child for BifNode
						System.out.println("FOR Node");
						Element child = (Element) node_j;
						System.out.println("Child-- " + child.getNodeValue());
					}
					//Node is GIVEN
					if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("GIVEN")) {
						//Set parent for, count 1
						System.out.println("GIVEN Node");
						Element given = (Element) node_j;
						System.out.println("Parent----- " + given.getNodeValue());
					}
					//Node is TABLE, count 1
					if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("TABLE")) {
						System.out.println("TABLE Node");
						Element table = (Element) node_j;
						System.out.println("CPT----- " + table.getNodeValue());
					}
				}
			//}
		}
		return;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BifDOMBuilder bdb = new BifDOMBuilder();
		try {
			bdb.build("C:/Projects/Mev/MeV_SVN/data/BN_files/affy_HG-U133_Plus_2_BN/results/FixedNetWithCPT.xml");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
