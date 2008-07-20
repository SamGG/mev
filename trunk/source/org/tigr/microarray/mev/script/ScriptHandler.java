/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptHandler.java
 *
 * Created on February 14, 2004, 10:44 PM
 */

package org.tigr.microarray.mev.script;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 * @author  braisted
 */
public class ScriptHandler {
    
    String tm4ScriptVersion = "1.0";
    String mevScriptVersion = "1.0";
    
    Document document;
    Element root;
    Element mevElement;
    Element primaryDataElement;
    
    Element analysisElement;
    int currAnalysisID = 1;
    int currDataID = 1;
    String lineSeparator = "\n";
    String indent = "   ";
    
    /** Creates a new instance of ScriptHandler */
    public ScriptHandler() {
        DOMImplementationImpl impl = new DOMImplementationImpl();
        document = impl.createDocument(null, "tm4", null);
        
        root = document.getDocumentElement();
        root.setAttribute("version", tm4ScriptVersion);
        
        mevElement = document.createElement("mev");
        mevElement.setAttribute("version", mevScriptVersion);
        root.appendChild(mevElement);
        
        analysisElement = document.createElement("analysis");
        
        primaryDataElement = document.createElement("primary_data");
        Element fileListElement = document.createElement("file_list");
        primaryDataElement.appendChild(fileListElement);
        
        mevElement.appendChild(primaryDataElement);
        mevElement.appendChild(analysisElement);
        
        // Element rootElement = new Element("tm4");
        // document = new Document(rootElement);
    }
    
    public Document getDocument() {
        return this.document;
    }
    
    public void createAlgorithmSet(int setID, int dataRef){
        Element algSetElement = document.createElement("alg_set");
        algSetElement.setAttribute("set_id", String.valueOf(setID));
        algSetElement.setAttribute("input_data_ref", String.valueOf(dataRef));
        analysisElement.appendChild(algSetElement);
    }
    
    public Element getAlgorithmSetByID(int setID){
        String id = String.valueOf(setID);
        NodeList elements = analysisElement.getElementsByTagName("alg_set");
        
        for(int i = 0; i < elements.getLength(); i++) {
            if(id.equals(((Element)elements.item(i)).getAttribute("set_id")))
                return (Element)elements.item(i);
        }
        return null;
    }
    
    
    public boolean appendAlgorithm(AlgorithmData data, int algID, int algSetRef, int inputDataRef) {
        
        Element algElement = document.createElement("algorithm");
        Element currElement;
//        Element nameElement;, paramsElement, paramElement, keyElement, valueElement;
        
        Element algSetElement = this.getAlgorithmSetByID(algSetRef);
        if(algSetElement == null)
            return false;
        
        Text nameText, keyText, valueText;
        
        AlgorithmParameters params = data.getParams();
        String name = params.getString("name");

        
        if(name != null){
            
            //set attributes, name and input ref
            algElement.setAttribute("name", name);
            algElement.setAttribute("input_data_ref", String.valueOf(inputDataRef));
            algElement.setAttribute("alg_id", String.valueOf(algID));
            
            //add parameter list with params
            addParameterList(algElement, params);
            
            
            //add matrices (or arrays, one dim matrices)
            Element matrices = document.createElement("mlist");
            
            //Int arrays
            Map map = data.getIntArrays();
            if(map.size() > 0) {
                addIntArrays(map, matrices);
            }
            
            //String arrays
            map = data.getStringArrays();
            if(map.size() > 0) {
                addStringArrays(map, matrices);
            }
            
            //matrices
            map = data.getMatrixes();
            if(map.size() > 0) {
                addMatrices(map, matrices);
            }
           
            //if there are matrices, add them
            if(matrices.getChildNodes().getLength() > 0)
                algElement.appendChild(matrices);
            
            //add output nodes
            String [] outputNodes = data.getStringArray("output-nodes");
            Element outputNodeElement, dataElement;
            if(outputNodes != null){
                outputNodeElement = document.createElement("output_data");
                for(int i = 0; i < outputNodes.length; i++) {
                    dataElement = document.createElement("data_node");
                    dataElement.setAttribute("data_node_id", String.valueOf(this.currDataID));
                    dataElement.setAttribute("name", outputNodes[i]);
                    outputNodeElement.appendChild(dataElement);
                    currDataID++;
                }
                algElement.appendChild(outputNodeElement);
            }
            
            //keySet.
        }
        algSetElement.appendChild(algElement);
        
        return true;
    }
    
    
    public void addParameterList(Element algElement, AlgorithmParameters params) {
        Map paramMap = params.getMap();
                String key = "", value = "";
       Element paramsElement, paramElement, keyElement, valueElement;        
        
        if(paramMap.size() > 1) {
            Set keySet = paramMap.keySet();
            Iterator iter = keySet.iterator();
            paramsElement = document.createElement("plist");
            
            while(iter.hasNext()){
                key = (String)iter.next();
                value = ((String)(paramMap.get(key)));
                
                //if key is name move on
                if(key.equals("name"))
                    continue;
                
                //make a param element
                paramElement = document.createElement("param");
                
                //set key and value attributes
                paramElement.setAttribute("key", key);
                paramElement.setAttribute("val", value);
                
                //append param to params
                paramsElement.appendChild(paramElement);
            }
            algElement.appendChild(paramsElement);
        }
    }
    
    
    public void addIntArrays(Map map, Element mlist) {
        Object [] arrayObjectNames = map.keySet().toArray();
        Element matrixElement, elementElement;
        int [] currentArray;
        
        String [] arrayNames = new String[arrayObjectNames.length];
        
        for (int i = 0; i < arrayNames.length; i++)
            arrayNames[i] = (String)arrayObjectNames[i];
        
        
        for(int i = 0; i < arrayNames.length; i++) {
            currentArray = (int [])map.get(arrayNames[i]);
            
            matrixElement = document.createElement("matrix");
            matrixElement.setAttribute("name", arrayNames[i]);
            matrixElement.setAttribute("type", "int");
            matrixElement.setAttribute("row_dim", String.valueOf(currentArray.length));
            
            for(int j = 0; j < currentArray.length; j++){
                elementElement = document.createElement("element");
                elementElement.setAttribute("row", String.valueOf(j));
                elementElement.setAttribute("col", "0");
                elementElement.setAttribute("val", String.valueOf(currentArray[i]));
                matrixElement.appendChild(elementElement);
            }
            
            mlist.appendChild(matrixElement);
        }
    }
    
    public void addStringArrays(Map map, Element mlist) {
        Object [] arrayObjectNames = map.keySet().toArray();
        Element matrixElement, elementElement;
        String [] currentArray;
        
        String [] arrayNames = new String[arrayObjectNames.length];
        
        for (int i = 0; i < arrayNames.length; i++)
            arrayNames[i] = (String)arrayObjectNames[i];
        
        
        for(int i = 0; i < arrayNames.length; i++) {
            currentArray = (String [])map.get(arrayNames[i]);
            
            matrixElement = document.createElement("matrix");
            matrixElement.setAttribute("name", arrayNames[i]);
            matrixElement.setAttribute("type", "String");
            matrixElement.setAttribute("row_dim", String.valueOf(currentArray.length));
            
            for(int j = 0; j < currentArray.length; j++){
                elementElement = document.createElement("element");
                elementElement.setAttribute("row", String.valueOf(j));
                elementElement.setAttribute("col", "0");
                elementElement.setAttribute("val", currentArray[i]);
                matrixElement.appendChild(elementElement);
            }
            
            mlist.appendChild(matrixElement);
        }
    }
    
    public void addMatrices(Map map, Element mlist) {
        Object [] matrixObjectNames = map.keySet().toArray();
        Element matrixElement;
        
        FloatMatrix currentFM;
        
        String [] matrixNames = new String[matrixObjectNames.length];
        
        for (int i = 0; i < matrixNames.length; i++)
            matrixNames[i] = (String)matrixObjectNames[i];
        
        
        for(int i = 0; i < matrixNames.length; i++) {
            currentFM = (FloatMatrix)map.get(matrixNames[i]);
            
            matrixElement = document.createElement("matrix");
            matrixElement.setAttribute("name", matrixNames[i]);
            matrixElement.setAttribute("type", "FloatMatrix");
            matrixElement.setAttribute("row_dim", String.valueOf(currentFM.getRowDimension()));
            
            addMatrixElements(currentFM, matrixElement);
            
            mlist.appendChild(matrixElement);
        }
    }
    
    public void addMatrixElements(FloatMatrix matrix, Element matrixElement) {
        Element elementElement;
        
        float [][] data = matrix.A;
        
        for(int i = 0; i < data.length; i++) {
            for(int j = 0; j < data[i].length; j++) {
                elementElement = document.createElement("element");
                elementElement.setAttribute("row", String.valueOf(i));
                elementElement.setAttribute("col", String.valueOf(j));
                elementElement.setAttribute("val", String.valueOf(data[i][j]));
                matrixElement.appendChild(elementElement);                
            }            
        }                      
    }
                
    public void writeDocument(String fileName) throws IOException {
        java.io.FileWriter writer = new java.io.FileWriter(fileName);
        serialize(document, writer);
        
        writer.flush();
        writer.close();
    }
    
    public void writeDocument(Document doc, Writer writer) throws IOException {
        serialize(doc, writer);
    }
    
    private void serialize(Document doc, Writer writer) throws IOException {
        serializeNode(doc, writer, "");
    }
    
    private void serializeNode(Node node, Writer writer, String indentLevel) throws IOException {
        String name;
        
        switch(node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                writer.write("<?xml version=\"1.0\"?>");
                writer.write(lineSeparator);
                
                Document doc = (Document)node;
                serializeNode(doc.getDocumentElement(), writer, " ");
                break;
            case Node.ELEMENT_NODE:
                name = node.getNodeName();
                writer.write(indentLevel + "<" + name);
                
                //posible attributes
                NamedNodeMap attrs = node.getAttributes();
                for(int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    writer.write(" "+ attr.getNodeName()+"=\""+attr.getNodeValue()+"\"");
                }
                
                NodeList children = node.getChildNodes();
                if(children.getLength() > 0) {
                    
                    writer.write(">");
                    
                    if((children.item(0) != null) &&
                    (children.item(0).getNodeType() == Node.ELEMENT_NODE )){
                        writer.write(lineSeparator);
                    }
                    for(int i = 0; i < children.getLength(); i++){
                        serializeNode(children.item(i), writer, indentLevel + indent);
                    }
                    if((children.item(0) != null) &&
                    (children.item(children.getLength()-1).getNodeType() == Node.ELEMENT_NODE)) {
                        writer.write(indentLevel);
                    }
                    
                    writer.write("</" + name + ">");
                    
                } else {
                    writer.write("/>");
                }
                
                writer.write(lineSeparator);
                break;
            case Node.TEXT_NODE:
                writer.write(node.getNodeValue());
                break;
            case Node.COMMENT_NODE:
                break;
        }
    }
    
}
