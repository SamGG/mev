package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;

public class BifDOMBuilder {

	private DocumentBuilder builder;
	private ArrayList<BifNode> bifAL = new ArrayList<BifNode>();
	
	public BifDOMBuilder()   {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			builder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			System.out.println("BifDOMBuilder() exception...");
			pce.printStackTrace();
		}
		System.out.println("BifDOMBuilder()");
	}
	
	/**
	 * 
	 * @param fileName_bif
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public ArrayList<BifNode> build(String fileName_bif) throws SAXException, IOException {
		bifAL.clear();
//		ArrayList<BifNode> bifNodes = new ArrayList<BifNode>();
		//Number of states attribute
		int BIN = 0;
		
		Document document = builder.parse(fileName_bif);
		
		//Find number of states/bins
		NodeList nodes_variables = document.getElementsByTagName("VARIABLE");
		
		//If no VARIABLE tags found, then doc is invalid
		if (nodes_variables.getLength() == 0) {
			throw new SAXException("Invalid XML file content");
		}
		
		//Check first VARIABLE node. If CLASS check next. If still cannot be determined throw Exception
		Node var = nodes_variables.item(0);
		Node var1 = nodes_variables.item(1);
		
		if (var.getNodeType() == Node.ELEMENT_NODE && 
				!(((Element) var).getElementsByTagName("NAME").item(0).getChildNodes().item(0).getNodeValue().equals("CLASS"))) {
			
			BIN = ((Element) var).getElementsByTagName("OUTCOME").getLength();
		} else if (var1.getNodeType() == Node.ELEMENT_NODE &&  
				!(((Element) var1).getElementsByTagName("NAME").item(0).getChildNodes().item(0).getNodeValue().equals("CLASS"))) {
			
			BIN = ((Element) var1).getElementsByTagName("OUTCOME").getLength();
		} else {
			throw new SAXException("Number of discrete states could not be determined");
		}
		System.out.println("Number of BINs " + BIN);
		
		//Grab all DEF nodes
		NodeList nodes_def = document.getElementsByTagName("DEFINITION");
		for (int i = 0; i < nodes_def.getLength(); i++) {
			Node def = nodes_def.item(i);
			
			//Create BifNode and set bin size
			BifNode bnode = new BifNode();
			bnode.setBins(BIN);
			
			//Retrieve all FOR, GIVEN & TABLE nodes
			NodeList nodes_j = def.getChildNodes();
			for (int j = 0; j < nodes_j.getLength(); j++) {
				Node node_j = nodes_j.item(j);
				
				//Ignore Node labeled CLASS
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getChildNodes().item(0).getNodeValue().equals("CLASS")) {
					break;
				}
				
				//Node is FOR (child - 1)
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("FOR")) {
					//Set child for BifNode
					Element child = (Element) node_j;
					
					//Set child
					bnode.setChild(child.getChildNodes().item(0).getNodeValue().trim());
				}
				
				//Node is GIVEN (parent - 0, 1 or many)
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("GIVEN")) {
					//Set parent for, count 1
					Element given = (Element) node_j;
					
					//Add Parent
					bnode.addParent(given.getChildNodes().item(0).getNodeValue().trim());
				}
				
				//Node is TABLE ( 1 )
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("TABLE")) {
					//Read CPT as a string
					Element table = (Element) node_j;
					String strTable = table.getChildNodes().item(0).getNodeValue();
										
					String _tmp[] = strTable.trim().split(" ");
					//System.out.println("Length " + _tmp.length);
					
					//Store string CPT as a nD array of floats
					//System.out.println("Start stringCPTto3dArray()");
					bnode.initCPTnD(nDArrayFromStringCPT(_tmp, BIN));
					
					//Also store CPT as a 1D array of floats whichever is helpful
					//System.out.println("Start stringCPTtoArray()");
					bnode.initCPT(ArrayFromStringCPT(_tmp));
				}
			}
			if(bnode.getChild() != null)
				bifAL.add(bnode);
		}
		return bifAL;
	}
	
	/**
	 * 
	 * @param fileName_bif
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public HashMap buildHashMap(String fileName_bif) throws SAXException, IOException {
		HashMap<String, BifNode> bifNodes = new HashMap<String, BifNode>();
		//Number of states attribute
		int BIN = 0;
		
		Document document = builder.parse(fileName_bif);
		
		//Find number of states/bins
		NodeList nodes_variables = document.getElementsByTagName("VARIABLE");
		
		//If no VARIABLE tags found, then doc is invalid
		if (nodes_variables.getLength() == 0) {
			throw new SAXException("Invalid XML file content");
		}
		
		//Check first VARIABLE node. If CLASS check next. If still cannot be determined throw Exception
		Node var = nodes_variables.item(0);
		Node var1 = nodes_variables.item(1);
		
		if (var.getNodeType() == Node.ELEMENT_NODE && 
				!(((Element) var).getElementsByTagName("NAME").item(0).getChildNodes().item(0).getNodeValue().equals("CLASS"))) {
			
			BIN = ((Element) var).getElementsByTagName("OUTCOME").getLength();
		} else if (var1.getNodeType() == Node.ELEMENT_NODE &&  
				!(((Element) var1).getElementsByTagName("NAME").item(0).getChildNodes().item(0).getNodeValue().equals("CLASS"))) {
			
			BIN = ((Element) var1).getElementsByTagName("OUTCOME").getLength();
		} else {
			throw new SAXException("Number of discrete states could not be determined");
		}
		System.out.println("Number of BINs " + BIN);
		
		//Grab all DEF nodes
		NodeList nodes_def = document.getElementsByTagName("DEFINITION");
		for (int i = 0; i < nodes_def.getLength(); i++) {
			Node def = nodes_def.item(i);
			
			//Create BifNode and set bin size
			BifNode bnode = new BifNode();
			bnode.setBins(BIN);
			
			//Retrieve all FOR, GIVEN & TABLE nodes
			NodeList nodes_j = def.getChildNodes();
			for (int j = 0; j < nodes_j.getLength(); j++) {
				Node node_j = nodes_j.item(j);
				
				//Ignore Node labeled CLASS
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getChildNodes().item(0).getNodeValue().equals("CLASS")) {
					break;
				}
				
				//Node is FOR (child - 1)
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("FOR")) {
					//Set child for BifNode
					Element child = (Element) node_j;
					
					//Set child
					System.out.println("Adding Child :" + child.getChildNodes().item(0).getNodeValue().trim());
					bnode.setChild(child.getChildNodes().item(0).getNodeValue().trim());
				}
				
				//Node is GIVEN (parent - 0, 1 or many)
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("GIVEN")) {
					//Set parent for, count 1
					Element given = (Element) node_j;
					
					//Add Parent
					System.out.println("Adding Parent :" + given.getChildNodes().item(0).getNodeValue().trim());
					bnode.addParent(given.getChildNodes().item(0).getNodeValue().trim());
				}
				
				//Node is TABLE ( 1 )
				if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("TABLE")) {
					//Read CPT as a string
					Element table = (Element) node_j;
					String strTable = table.getChildNodes().item(0).getNodeValue();
										
					String _tmp[] = strTable.trim().split(" ");
					//System.out.println("Length " + _tmp.length);
					
					//Store string CPT as a nD array of floats
					//System.out.println("Start stringCPTto3dArray()");
					bnode.initCPTnD(nDArrayFromStringCPT(_tmp, BIN));
					
					//Also store CPT as a 1D array of floats whichever is helpful
					//System.out.println("Start stringCPTtoArray()");
					bnode.initCPT(ArrayFromStringCPT(_tmp));
				}
				//bifNodes.add(bnode);
			}
			//System.out.println("Key Child " + bnode.getChild());
			if(bnode.getChild() != null)
				bifNodes.put(bnode.getChild(), bnode);
		}
		return bifNodes;
	}
	
	/**
	 * 
	 * @param name The desired BifNode's child.
	 * @return The BifNode with child corresponding to the String 'name'.
	 */
	public BifNode getBifNode(String name){
		for (int i=0; i<bifAL.size(); i++){
			if (bifAL.get(i)
					.getChild()
					.equals(name))
				return bifAL.get(i);
		}
		return null;
	}
	
	/**
	 * 
	 * @param name The child of all the parents.
	 * @return An ArrayList of BifNodes representing the parents of the node with child 'name'.
	 */	
	public ArrayList<BifNode> getParents(BifNode bifNode){
		ArrayList<BifNode> bifNodeParents = new ArrayList<BifNode>();
		ArrayList<String> parents = bifNode.getParents();
		if (parents!=null)
			for (int i=0; i<parents.size(); i++){
				if (parents.get(i)!=null)
					bifNodeParents.add(getBifNode(parents.get(i)));
			}
		return bifNodeParents;
	}
	/**
	 * 
	 * @param _cpt
	 * @return
	 */
	private float[] ArrayFromStringCPT(String[] _cpt) {
		float arr_1[] = new float[_cpt.length];
		for(int k = 0; k < _cpt.length; k++){
			//System.out.println(_cpt[k]);
			arr_1[k] = Float.parseFloat(_cpt[k]);
		}
		return arr_1;
	}

	/**
	 * 
	 * @param _cpt
	 * @param binSize
	 * @return
	 */
	private float [][][] nDArrayFromStringCPT(String _cpt[], int binSize) {
//		System.out.println("_cpt.length " + _cpt.length);
		float arr_1[][][] = new float[_cpt.length][binSize][binSize];
		for(int k = 0; k < _cpt.length;){
			for(int l = 0; l < binSize; l++){
				//System.out.println(_cpt[k+l]);
				arr_1[k][l][l] = Float.parseFloat(_cpt[k+l]);
			}
			k += binSize;
		}
		return arr_1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BifDOMBuilder bdb = new BifDOMBuilder();
		try {
			bdb.build("C:/Projects/MeV/MeV_SVN/data/BN_files/affy_HG-U133_Plus_2_BN/results/FixedNetWithCPT.xml");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
