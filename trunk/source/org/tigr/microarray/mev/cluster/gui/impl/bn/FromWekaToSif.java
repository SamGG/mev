/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.transform.stream.StreamSource;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class FromWekaToSif {
	
	/**
	 * Converts a Bif XML network with CPT info into a simple sif file 
	 * @param xmlBifStr
	 * @param pw
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void fromWekaBifToSif(String xmlBifStr, PrintWriter pw) throws SAXException, IOException {
		BifDOMBuilder bifDom = new BifDOMBuilder();
		ArrayList<BifNode> bifNodes = bifDom.build(new InputSource(new StringReader(xmlBifStr)));
		
		Hashtable<String, String> uniqueNodesIdMap = new Hashtable<String, String>();
		int nodesId = 1;
		
		for(int c = 0; c < bifNodes.size(); c++) {
			BifNode _tmpBifNode = (BifNode)bifNodes.get(c);
			String labelTo = _tmpBifNode.getChild();
			if(!uniqueNodesIdMap.containsKey(labelTo)) {
				uniqueNodesIdMap.put(labelTo, String.valueOf(nodesId));
				nodesId++;
			}
			//Run thru parent(s) if any and create edges in sif format
			for(int p = 0; p < _tmpBifNode.numParents(); p++) {
				String labelFrom = _tmpBifNode.getParentAt(p);
				if(!uniqueNodesIdMap.containsKey(labelFrom)) {
					uniqueNodesIdMap.put(labelFrom, String.valueOf(nodesId));
					nodesId++;
				}
				pw.println(labelFrom + " pd "+ labelTo);
			}
		}
	}
	
	// Pre: Name of input file from WEKA containing network structure
	// in the format: variable: parent1,...,parentn
	//      PrintWriter where to write the network in directed SIF format: node1 pd node2
	// Post: Read a network structure from WEKA output from the given input file name and writes the network in SIF format to the give output PrintWriter
	public static void fromWekaToSif(String evalStr, PrintWriter pw, boolean map){
		String[] evalSubstrings = evalStr.split("\n");
		String s = null;
		// Process lines after reading Network Structure and before reading LogScore
		boolean toProcess = false;
		for(int i = 0; i < evalSubstrings.length; i++){
			s = evalSubstrings[i];
			s = s.trim();
			if(s.startsWith("LogScore")){
				toProcess = false;
			}
			if(s.startsWith("CLASS"))
				continue;
			if(toProcess){
				//if(map)
				//fromWekaToSifOneLine(s, pw, map);
				//else
				fromWekaToSifOneLine(s, pw);
			}
			if(s.startsWith("Network structure")){
				toProcess = true;
			}
		}
	}
	// Pre: String containing variable: parent1,...,parentn
	//      PrintWriter where to write the network in directed SIF format: node1 pd node2
	// Post: Reads a variable parent1,...,parentn String and writes in directed SIF format node1 pd node2 to the PrintWriter
	public static void fromWekaToSifOneLine(String s, PrintWriter pw){	
		if(s.endsWith(":")){
			return;
		}
		int colon = s.indexOf("(");
		String to = s.substring(0,colon).trim();
		int startIndex = s.indexOf("): ")+3;
		int index = 0;
		String from;
		while(index != s.lastIndexOf(" ")){
			index = s.indexOf(" ", startIndex+1);
			if(index != -1){
				from = s.substring(startIndex,index).trim();
				if(!from.equals("CLASS") || !to.equals("CLASS")){
					pw.println(from + " pd "+ to);
				}
				startIndex = index;	    
			}	
			else {
				break;
			}
		}
		from = s.substring(startIndex, s.length()).trim();
		if(!from.equals("CLASS") || !to.equals("CLASS")){
			pw.println(from + " pd "+ to);
		}
	}

	/**
	 * 
	 * @param evalStr
	 * @param fileName
	 * @param b
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromWekaToXgmml2(boolean dir, String evalStr, String fileName, String cptFile, boolean b, HashMap probeIndexAssocHash, IData data, String interactionType) throws NullArgumentException, IOException {
		if(probeIndexAssocHash != null) {
			System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
			//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
		} else {
			throw new NullArgumentException("Given Probe-Index Hash was null!");
		}

		Hashtable<String, String> uniqueNodesWithId = new Hashtable<String, String>();
		Vector<String> edges = new Vector<String>();
		//int nodeId = 1;
		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label, cptFile);

		String[] evalSubstrings = evalStr.split("\n");
		String s = null;
		// Process lines after reading Network Structure and before reading LogScore
		boolean toProcess = false;
		Vector<String> nodes = new Vector<String>();
		for(int i = 0; i < evalSubstrings.length; i++){
			s = evalSubstrings[i];
			s = s.trim();
			if(s.startsWith("LogScore")){
				toProcess = false;
			}
			if(s.startsWith("CLASS"))
				continue;
			if(toProcess){
				Vector<String> _tmpEdges = fromWekaToNodes(s);
				if(_tmpEdges != null) {
					if(_tmpEdges.size() != 0) {
						edges.addAll(_tmpEdges);
						Iterator _itr = _tmpEdges.iterator();
						while(_itr.hasNext()) {
							String fromTo[] = ((String)_itr.next()).split("-");
							if(!nodes.contains(fromTo[0].trim())) {
								nodes.add(fromTo[0].trim());
							}

							if(!nodes.contains(fromTo[1].trim())) {
								nodes.add(fromTo[1].trim());
							}
							/*
							if(!uniqueNodesWithId.containsKey(fromTo[0])) {
								uniqueNodesWithId.put(fromTo[0].trim(), String.valueOf(nodeId));
								nodeId++;
							}
							if(!uniqueNodesWithId.containsKey(fromTo[1].trim())) {
								uniqueNodesWithId.put(fromTo[1].trim(), String.valueOf(nodeId));
								nodeId++;
							}
							 */
						}

						//To give a negative ID to nodes. Not sure if required
						int nodeId = nodes.size();
						Iterator _itr1 = nodes.iterator();
						while(_itr1.hasNext()) {
							uniqueNodesWithId.put((String)_itr1.next(), String.valueOf(nodeId*-1));
							nodeId--;
						}

						String _tmp = getXgmmlNodesAndEdges(dir, edges, "-", uniqueNodesWithId, probeIndexAssocHash, data);
						if(_tmp != null)
							xgmmlContent += _tmp;
					}
				}
			}
			if(s.startsWith("Network structure")){
				toProcess = true;
			}
		}
		xgmmlContent += XGMMLGenerator.getFooter();
		try {
			XGMMLGenerator.writeFileXGMML(fileName, xgmmlContent);
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	/**
	 * 
	 * @param bifStr
	 * @param fileName
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void fromWekaBifToXgmml(boolean dir, String bifStr, String fileName, String cptFileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException, SAXException {
		BifDOMBuilder bifDom = new BifDOMBuilder();
		ArrayList<BifNode> bifNodes = bifDom.build(new InputSource(new StringReader(bifStr)));
		
		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label, cptFileName);
		String xgmmlNodes = "";
		String xgmmlEdges = "";
		Hashtable<String, String> uniqueNodesIdMap = new Hashtable<String, String>();
		int nodesId = 1;
		for(int c = 0; c < bifNodes.size(); c++) {
			BifNode _tmpBifNode = (BifNode)bifNodes.get(c);
			String labelTo = _tmpBifNode.getChild();
			if(!uniqueNodesIdMap.containsKey(labelTo)) {
				uniqueNodesIdMap.put(labelTo, String.valueOf(nodesId));
				nodesId++;
				
				xgmmlNodes += getXgmmlNode(labelTo, uniqueNodesIdMap, probeIndexAssocHash, data);
			}
			//Run thru parent(s) if any and create edges
			for(int p = 0; p < _tmpBifNode.numParents(); p++) {
				String labelFrom = _tmpBifNode.getParentAt(p);
				if(!uniqueNodesIdMap.containsKey(labelFrom)) {
					uniqueNodesIdMap.put(labelFrom, String.valueOf(nodesId));
					nodesId++;
					
					xgmmlNodes += getXgmmlNode(labelFrom, uniqueNodesIdMap, probeIndexAssocHash, data);
				}
				xgmmlEdges += getXgmmlEdge(dir, labelFrom, labelTo, uniqueNodesIdMap);
			}
		}
		xgmmlContent += xgmmlNodes;
		xgmmlContent += xgmmlEdges;
		xgmmlContent += XGMMLGenerator.getFooter();
		try {
			XGMMLGenerator.writeFileXGMML(fileName, xgmmlContent);
		} catch (IOException ioe) {
			throw ioe;
		}
	}
	
	public static void fromWekaBifToXgmml(Hashtable edgesTable, int numItr, float confThreshold, String fileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
		
	}
	
	/**
	 * 
	 * @param evalStr
	 * @param fileName
	 * @param b
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromWekaToXgmml(boolean dir, String evalStr, String fileName, String cptFileName, boolean b, HashMap probeIndexAssocHash, IData data, String interactionType) throws NullArgumentException, IOException {
		if(probeIndexAssocHash != null) {
			System.out.println("fromWekaToXgmml - probeIndexAssocHash Size: " + probeIndexAssocHash.size());
			//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
		} else {
			throw new NullArgumentException("Given Probe-Index Hash was null!");
		}

		Vector<String> edges = new Vector<String>();
		//int nodeId = 1;
		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label, cptFileName);

		String[] evalSubstrings = evalStr.split("\n");
		String s = null;
		// Process lines after reading Network Structure and before reading LogScore
		boolean toProcess = false;
		Vector<String> nodes = new Vector<String>();
		for(int i = 0; i < evalSubstrings.length; i++){
			s = evalSubstrings[i];
			s = s.trim();
			if(s.startsWith("LogScore")){
				toProcess = false;
			}
			if(s.startsWith("CLASS"))
				continue;
			if(toProcess){
				Vector<String> _tmpEdges = fromWekaToNodes(s);
				if(_tmpEdges != null) {
					edges.addAll(_tmpEdges);
				}
			}
			if(s.startsWith("Network structure")){
				toProcess = true;
			}
		}
		
		Hashtable<String, String> uniqueNodesIdMap = new Hashtable<String, String>();
		Vector<String> nodeCreated = new Vector<String>();
		String xgmmlEdges = "";
		Iterator _itr1 = edges.iterator();
		int nodesId = 1;
		while(_itr1.hasNext()) {
			String _nodes[] = ((String)_itr1.next()).split("-"); //"-"
			String labelFrom = _nodes[0].trim();
			String labelTo = _nodes[1].trim();
			
			if(!uniqueNodesIdMap.containsKey(labelFrom)) {
				uniqueNodesIdMap.put(labelFrom, String.valueOf(nodesId));
				nodesId++;
			}
			
			if(!uniqueNodesIdMap.containsKey(labelTo)) {
				uniqueNodesIdMap.put(labelTo, String.valueOf(nodesId));
				nodesId++;
			}
			
			if(!nodeCreated.contains(labelFrom)) {
				xgmmlContent += getXgmmlNode(labelFrom, uniqueNodesIdMap, probeIndexAssocHash, data);
				nodeCreated.add(labelFrom);
			}
			
			if(!nodeCreated.contains(labelTo)) {
				xgmmlContent += getXgmmlNode(labelTo, uniqueNodesIdMap, probeIndexAssocHash, data);
				nodeCreated.add(labelTo);
			}
			
			xgmmlEdges+= getXgmmlEdge(dir, labelFrom, labelTo, uniqueNodesIdMap);
		}
		xgmmlContent += xgmmlEdges;
		xgmmlContent += XGMMLGenerator.getFooter();
		try {
			XGMMLGenerator.writeFileXGMML(fileName, xgmmlContent);
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	
	/**
	 * 
	 * @param edges
	 * @param fileName
	 * @param b
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromWekaToXgmml(boolean dir, Hashtable edgesTable, String cptFileName, int numItr, float confThreshold, String fileName, HashMap probeIndexAssocHash, IData data, String interactionType) throws NullArgumentException, IOException {
		if(probeIndexAssocHash != null) {
			System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
			//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
		} else {
			throw new NullArgumentException("Given Probe-Index Hash was null!");
		}
		Hashtable<String, String> uniqueNodesWithId = new Hashtable<String, String>();
		Vector<String> edges = new Vector<String>();
		Vector<String> nodes = new Vector<String>();

		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label, cptFileName);

		Enumeration enumerate = edgesTable.keys();
		while(enumerate.hasMoreElements()){
			String edge = (String)enumerate.nextElement();
			Integer count = (Integer)edgesTable.get(edge);
			float presence = count.floatValue()/numItr;
			if(presence >= confThreshold){
				edges.add(edge);
				String fromTo[] = edge.split("pd");
				if(!nodes.contains(fromTo[0].trim())) {
					nodes.add(fromTo[0].trim());
				}

				if(!nodes.contains(fromTo[1].trim())) {
					nodes.add(fromTo[1].trim());
				}

				/*
				if(!uniqueNodesWithId.containsKey(fromTo[0].trim())) {
					uniqueNodesWithId.put(fromTo[0].trim(), String.valueOf(nodeId));
					nodeId++;
				}
				if(!uniqueNodesWithId.containsKey(fromTo[1].trim())) {
					uniqueNodesWithId.put(fromTo[1].trim(), String.valueOf(nodeId));
					nodeId++;
				}
				 */
			}
		}
		//To give a negative ID to nodes. Not sure if required
		int nodeId = nodes.size();
		Iterator _itr = nodes.iterator();
		while(_itr.hasNext()) {
			uniqueNodesWithId.put((String)_itr.next(), String.valueOf(nodeId*-1));
			nodeId--;
		}

		String _tmp = getXgmmlNodesAndEdges(dir, edges, "pd", uniqueNodesWithId, probeIndexAssocHash, data);
		if(_tmp != null) {
			if(!_tmp.equals("")) {
				xgmmlContent += _tmp;
			}
		}
		xgmmlContent += XGMMLGenerator.getFooter();
		try {
			XGMMLGenerator.writeFileXGMML(fileName, xgmmlContent);
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	/**
	 * 
	 * @param labelFrom
	 * @param labelTo
	 * @param prodeIdMap
	 * @param probeIndexAssocHash
	 * @param data
	 * @return
	 */
	private static String getXgmmlNode(String nodelabel, Hashtable probeIdMap, HashMap probeIndexAssocHash, IData data) {
		String xgmmlNodeContent = "";
		String xgmmlEdgeContent = "";
		int probeInd;
		
		//Convert to XGMML node & Edge
		//Get index from hash map encoded into the form NM_23456 to 1-Afy_X1234 where 1 is the probe index
		String tmp[] = ((String)probeIndexAssocHash.get(nodelabel)).split("-");
		probeInd = Integer.parseInt(tmp[0]);
		
		xgmmlNodeContent = XGMMLGenerator.createNode(nodelabel, (String) probeIdMap.get(nodelabel), data, probeInd);
		return xgmmlNodeContent;
	}
	
	/**
	 * 
	 * @param labelFrom
	 * @param labelTo
	 * @param probeIdMap
	 * @return
	 */
	private static String getXgmmlEdge(boolean dir, String labelFrom, String labelTo, Hashtable probeIdMap) {
		String xgmmlEdgeContent = "";

		xgmmlEdgeContent += XGMMLGenerator.createEdge(dir, labelFrom, labelTo, (String) probeIdMap.get(labelFrom), (String) probeIdMap.get(labelTo));
		return xgmmlEdgeContent;
	}
	
	/**
	 * 
	 * @param edges
	 * @param interactionType 
	 * @param nodeId
	 * @param probeIndexAssocHash
	 * @param data
	 * @return
	 */
	private static String getXgmmlNodesAndEdges(boolean dir, Vector edges, String nodeConnector, Hashtable nodesWithId, HashMap probeIndexAssocHash, IData data) {
		String xgmmlNodeContent = "";
		String xgmmlEdgeContent = "";
		Vector<String> nodeCreated = new Vector<String>();
		String labelTo;
		String labelFrom;
		int[] fromTo = new int[2];

		Enumeration myEnum = edges.elements();
		while(myEnum.hasMoreElements()) {
			String nodes[] = ((String)myEnum.nextElement()).split(nodeConnector); //"-"
			labelFrom = nodes[0].trim();
			labelTo = nodes[1].trim();

			//Conver to XGMML node & Edge
			//Get indx from hash map encoded int the form NM_23456 to 1-Afy_X1234 where 1 is the probe index
			String tmp[] = ((String)probeIndexAssocHash.get(labelFrom)).split("-");
			fromTo[0] = Integer.parseInt(tmp[0]);
			tmp = ((String)probeIndexAssocHash.get(labelTo)).split("-");
			fromTo[1] = Integer.parseInt(tmp[0]);

			//Get annotation and create nodes and edges in XGMML Format
			//System.out.println("writeXGMML Edge Indices From: " + fromTo[0] + " To: " + fromTo[1]);
			String srcId = (String)nodesWithId.get(labelFrom);
			if(!nodeCreated.contains(labelFrom)) {
				xgmmlNodeContent += XGMMLGenerator.createNode(labelFrom, srcId, data, fromTo[0]);
				nodeCreated.add(labelFrom);
			}

			String tgtId = (String)nodesWithId.get(labelTo);
			if(!nodeCreated.contains(labelTo)) {
				xgmmlNodeContent += XGMMLGenerator.createNode(labelTo, tgtId, data, fromTo[1]);
				nodeCreated.add(labelTo);
			}

			xgmmlEdgeContent += XGMMLGenerator.createEdge(dir, labelFrom, labelTo, srcId, tgtId);	
		}

		return xgmmlNodeContent += xgmmlEdgeContent;
	}

	/**
	 * 
	 * @param inter
	 * @param fileName
	 * @param probeIndexAssocHash
	 * @param data
	 * @param interactionType 
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromSimpleGeneEdgeToXgmml(boolean dir, ArrayList<SimpleGeneEdge> inter, String fileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
		//FileOutputStream fos = null;
		String path=System.getProperty("user.dir");
		path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP;
		try {	    
			if(inter == null){
				System.out.println("UsefulInteractions-writeSif");  
				throw new NullArgumentException("Given inter was null!");
			}

			if(probeIndexAssocHash != null) {
				System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
				System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
			} else {
				throw new NullArgumentException("Given Probe-Index Hash was null!");
			}

			Hashtable<String, String> uniqueNodesWithId = new Hashtable<String, String>();
			Vector<String> edges = new Vector<String>();
			Vector<String> nodes = new Vector<String>();
			SimpleGeneEdge sGE = null;
			//int nodeId = 1;
			String xgmmlContent = "";
			System.out.println("Network File Name " + fileName);
			String label = fileName.substring(0, fileName.lastIndexOf("."));
			//String _tmp[] = fileName.split(".");
			//String label = _tmp[0];
			System.out.println("Network File Prefix " + label);
			xgmmlContent = XGMMLGenerator.createHeader(label, "");

			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				String labelFrom = sGE.getFrom().trim();
				String labelTo = sGE.getTo().trim();
				edges.add(sGE.getEdgeAsString("pd"));
				if(!nodes.contains(labelFrom.trim())) {
					nodes.add(labelFrom.trim());
				}

				if(!nodes.contains(labelTo.trim())) {
					nodes.add(labelTo.trim());
				}
				/*
				if(!uniqueNodesWithId.containsKey(labelFrom)) {
					uniqueNodesWithId.put(labelFrom, String.valueOf(nodeId));
					nodeId++;
				}
				if(!uniqueNodesWithId.containsKey(labelTo)) {
					uniqueNodesWithId.put(labelTo, String.valueOf(nodeId));
					nodeId++;
				}
				 */
			}
			//To give a negative ID to nodes. Not sure if required
			int nodeId = nodes.size();
			Iterator _itr = nodes.iterator();
			while(_itr.hasNext()) {
				uniqueNodesWithId.put((String)_itr.next(), String.valueOf(nodeId*-1));
				nodeId--;
			}
			String _tmp = getXgmmlNodesAndEdges(dir, edges, "pd", uniqueNodesWithId, probeIndexAssocHash, data);
			if(_tmp != null) {
				if(!_tmp.equals("")) {
					xgmmlContent += _tmp;
				}
			}
			xgmmlContent += XGMMLGenerator.getFooter();
			try {
				XGMMLGenerator.writeFileXGMML(path + fileName, xgmmlContent);
			} catch (IOException ioe) {
				throw ioe;
			}
		}
		catch(IOException ioe){
			//System.out.println(ioe);
			throw ioe;
		}
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	private static Vector<String> fromWekaToNodes(String s) {
		if(s.endsWith(":")){
			return null;
		}

		Vector<String> edges = new Vector<String>();
		int colon = s.indexOf("(");
		String labelTo = s.substring(0,colon).trim();
		int startIndex = s.indexOf("): ") + 3;
		int index = 0;
		String labelFrom;

		while(index != s.lastIndexOf(" ")){
			index = s.indexOf(" ", startIndex+1);
			if(index != -1){
				labelFrom = s.substring(startIndex,index).trim();
				if(!labelFrom.equals("CLASS") || !labelTo.equals("CLASS")){
					//pw.println(labelFrom + " pd " + labelTo);
					System.out.println(labelFrom + "-" + labelTo);
					edges.add(labelFrom + "-" + labelTo);
				}
				startIndex = index;	    
			}	
			else {
				break;
			}
		}
		labelFrom = s.substring(startIndex, s.length()).trim();
		if(!labelFrom.equals("CLASS") || !labelTo.equals("CLASS")){
			//pw.println(labelFrom + " pd " + labelTo);
			System.out.println(labelFrom + "-" + labelTo);
			edges.add(labelFrom + "-" + labelTo);
		}
		return edges;

	}
}


