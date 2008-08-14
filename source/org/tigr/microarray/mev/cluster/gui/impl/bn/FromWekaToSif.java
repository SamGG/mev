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

import org.tigr.microarray.mev.cluster.gui.IData;
public class FromWekaToSif {
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
				if(map)
					fromWekaToSifOneLine(s, pw, map);
				else
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
    
    // Raktim - Temp function for writing gene name instead of Acc#
    public static void fromWekaToSifOneLine(String s, PrintWriter pw, boolean map){	
    	//Raktim - Temporarily Done for Gene Name mapping for RnaI data
        Hashtable<String, String> AccGeneMap = new Hashtable<String, String>();
        //Raktim - Temporary Hard Coded Value for RnaI Data only
        AccGeneMap.put("NM_002880", "RAF1");
        AccGeneMap.put("NM_002880", "RAF1");
        AccGeneMap.put("NM_002507", "NGFR");
        AccGeneMap.put("NM_138957", "ERK2");
        AccGeneMap.put("NM_002746", "ERK1");
        AccGeneMap.put("NM_002755", "MEK1");
        AccGeneMap.put("NM_030662", "MEK2");
        AccGeneMap.put("NM_001964", "EGR-1");
        AccGeneMap.put("NM_004935", "CDK5");
        AccGeneMap.put("NM_176795", "RAS");
        
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
					if(map)
						pw.println(AccGeneMap.get(from) + " pd "+ AccGeneMap.get(to));
					else 
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
			if(map)
				pw.println(AccGeneMap.get(from) + " pd "+ AccGeneMap.get(to));
			else
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
	public static void fromWekaToXgmml(String evalStr, String fileName, boolean b, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
		if(probeIndexAssocHash != null) {
			System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
			//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
		} else {
			throw new NullArgumentException("Given Probe-Index Hash was null!");
		}
		
		Hashtable<String, String> uniqueNodesWithId = new Hashtable<String, String>();
		Vector<String> edges = new Vector<String>();
		int nodeId = 1;
		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label);
		
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
				Vector<String> _tmpEdges = fromWekaToNodes(s);
				if(_tmpEdges != null) {
					if(_tmpEdges.size() != 0) {
						edges.addAll(_tmpEdges);
						Iterator _itr = _tmpEdges.iterator();
						while(_itr.hasNext()) {
							String fromTo[] = ((String)_itr.next()).split("-");
							if(!uniqueNodesWithId.containsKey(fromTo[0])) {
								uniqueNodesWithId.put(fromTo[0].trim(), String.valueOf(nodeId));
								nodeId++;
							}
							if(!uniqueNodesWithId.containsKey(fromTo[1].trim())) {
								uniqueNodesWithId.put(fromTo[1].trim(), String.valueOf(nodeId));
								nodeId++;
							}
						}
						String _tmp = getXgmmlNodesAndEdges(edges, "-", uniqueNodesWithId, probeIndexAssocHash, data);
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
	 * @param edges
	 * @param fileName
	 * @param b
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromWekaToXgmml(Hashtable edgesTable, int numItr, float confThreshold, String fileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
		if(probeIndexAssocHash != null) {
			System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
			//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
		} else {
			throw new NullArgumentException("Given Probe-Index Hash was null!");
		}
		Hashtable<String, String> uniqueNodesWithId = new Hashtable<String, String>();
		Vector<String> edges = new Vector<String>();
		int nodeId = 1;
		String xgmmlContent = "";
		String label = fileName.substring(fileName.lastIndexOf(BNConstants.SEP)+1, fileName.lastIndexOf("."));
		xgmmlContent = XGMMLGenerator.createHeader(label);
		
		Enumeration enumerate = edgesTable.keys();
		while(enumerate.hasMoreElements()){
			String edge = (String)enumerate.nextElement();
			Integer count = (Integer)edgesTable.get(edge);
			float presence = count.floatValue()/numItr;
			if(presence >= confThreshold){
				edges.add(edge);
				String fromTo[] = edge.split("pd");
				if(!uniqueNodesWithId.containsKey(fromTo[0].trim())) {
					uniqueNodesWithId.put(fromTo[0].trim(), String.valueOf(nodeId));
					nodeId++;
				}
				if(!uniqueNodesWithId.containsKey(fromTo[1].trim())) {
					uniqueNodesWithId.put(fromTo[1].trim(), String.valueOf(nodeId));
					nodeId++;
				}
			}
		}
		String _tmp = getXgmmlNodesAndEdges(edges, "pd", uniqueNodesWithId, probeIndexAssocHash, data);
		if(_tmp != null) {
			if(!_tmp.isEmpty()) {
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
	 * @param edges
	 * @param nodeId
	 * @param probeIndexAssocHash
	 * @param data
	 * @return
	 */
	private static String getXgmmlNodesAndEdges(Vector edges, String nodeConnector, Hashtable nodesWithId, HashMap probeIndexAssocHash, IData data) {
		String xgmmlContent = "";
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
				xgmmlContent += XGMMLGenerator.createNode(labelFrom, srcId, data, fromTo[0]);
				nodeCreated.add(labelFrom);
			}
			
			String tgtId = (String)nodesWithId.get(labelTo);
			if(!nodeCreated.contains(labelTo)) {
				xgmmlContent += XGMMLGenerator.createNode(labelTo, tgtId, data, fromTo[1]);
				nodeCreated.add(labelTo);
			}
			
			xgmmlContent += XGMMLGenerator.createEdge(labelFrom, labelTo, srcId, tgtId);	
		}
		return xgmmlContent;
	}
	
	/**
	 * 
	 * @param inter
	 * @param fileName
	 * @param probeIndexAssocHash
	 * @param data
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public static void fromSimpleGeneEdgeToXgmml(ArrayList<SimpleGeneEdge> inter, String fileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
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
			SimpleGeneEdge sGE = null;
			int nodeId = 1;
			String xgmmlContent = "";
			System.out.println("Network File Name " + fileName);
			String label = fileName.substring(0, fileName.lastIndexOf("."));
			//String _tmp[] = fileName.split(".");
			//String label = _tmp[0];
			System.out.println("Network File Prefix " + label);
			xgmmlContent = XGMMLGenerator.createHeader(label);
			
			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				String labelFrom = sGE.getFrom().trim();
				String labelTo = sGE.getTo().trim();
				edges.add(sGE.getEdgeAsString("pd"));
				if(!uniqueNodesWithId.containsKey(labelFrom)) {
					uniqueNodesWithId.put(labelFrom, String.valueOf(nodeId));
					nodeId++;
				}
				if(!uniqueNodesWithId.containsKey(labelTo)) {
					uniqueNodesWithId.put(labelTo, String.valueOf(nodeId));
					nodeId++;
				}
			}
			String _tmp = getXgmmlNodesAndEdges(edges, "pd", uniqueNodesWithId, probeIndexAssocHash, data);
			if(_tmp != null) {
				if(!_tmp.isEmpty()) {
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
			edges.add(labelFrom + "-" + labelTo);
		}
		return edges;
		
    }
}
	    
	  
