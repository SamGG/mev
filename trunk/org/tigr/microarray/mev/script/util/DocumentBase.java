/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptHandler.java
 *
 * Created on February 14, 2004, 10:44 PM
 */

package org.tigr.microarray.mev.script.util;

import java.awt.Frame;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.XMLSerializer;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.event.ScriptDocumentEvent;
import org.tigr.microarray.mev.script.event.ScriptEventListener;



/**
 *
 * @author  braisted
 */
public class DocumentBase extends DefaultHandler {
    
    String tm4ScriptVersion = "1.0";
    String mevScriptVersion = "1.0";
    ScriptManager manager;
    
    Document document;
    String scriptText;
    Element root;
    Element mevElement;
    Element primaryDataElement;
    Element commentElement;
    
    Element analysisElement;
    
    int currDataID = 1;
    int currAlgSetID = 1;
    String lineSeparator = System.getProperty("line.separator");
    String indent = "   ";
    
    int parseErrors = 0;
    ErrorLog errorLog;
    
    private Vector listeners;
    boolean parsedScript = false;
    
    /** Creates a new instance of ScriptHandler */
    public DocumentBase(ScriptManager manager) {
        this.manager = manager;
        errorLog = new ErrorLog(manager);
        listeners = new Vector();
        
        DOMImplementationImpl impl = new DOMImplementationImpl();
        DocumentType docType = impl.createDocumentType("TM4ML",null,"mev_script_dtd.dtd");
        
        document = impl.createDocument(null, "TM4ML", docType);
        
        root = document.getDocumentElement();
        root.setAttribute("version", tm4ScriptVersion);
        
        mevElement = document.createElement("mev");
        mevElement.setAttribute("version", mevScriptVersion);
        root.appendChild(mevElement);
        
        analysisElement = document.createElement("analysis");
        primaryDataElement = document.createElement("primary_data");
        primaryDataElement.setAttribute("id", "1");
        
        createAlgorithmSet(1); //default algoritm set
        
        //Element fileListElement = document.createElement("file_list");
        //primaryDataElement.appendChild(fileListElement);
        
        mevElement.appendChild(primaryDataElement);
        mevElement.appendChild(analysisElement);
        
        scriptText = new String("");
        updateScript();
        
        // Element rootElement = new Element("tm4");
        // document = new Document(rootElement);
    }
    
    
    /** Creates a new instance of ScriptHandler */
    public DocumentBase(String date, String name, String description, ScriptManager  manager) {
        this.manager = manager;
        errorLog = new ErrorLog(manager);
        listeners = new Vector();
        
        DOMImplementationImpl impl = new DOMImplementationImpl();
        DocumentType docType = impl.createDocumentType("TM4ML",null,"mev_script_dtd.dtd");
        
        document = impl.createDocument(null, "TM4ML", docType);
        
        root = document.getDocumentElement();
        root.setAttribute("version", tm4ScriptVersion);
        
        
        mevElement = document.createElement("mev");
        mevElement.setAttribute("version", mevScriptVersion);
        root.appendChild(mevElement);
        
        if(date != null) {
            setDateComment(date);
        }
        
        if(name != null) {
            setNameComment(name);
        }
        
        if(description != null) {
            setDescriptionComment(description);
        }
        
        analysisElement = document.createElement("analysis");
        primaryDataElement = document.createElement("primary_data");
        primaryDataElement.setAttribute("id", "1");
        
        createAlgorithmSet(1); //default algoritm set
        
        mevElement.appendChild(primaryDataElement);
        mevElement.appendChild(analysisElement);
        
        scriptText = new String("");
        updateScript();
        
        // Element rootElement = new Element("tm4");
        // document = new Document(rootElement);
    }
    
    
    public void setDocumnet(Document doc) {
        document = doc;
        updateScript();
    }
    
    public Document getDocument() {
        return this.document;
    }
    
    public void setDateComment(String comment) {
        Comment nameElement = document.createComment("Original Script Creation Date: " + comment);
        mevElement.appendChild(nameElement);
        updateScript();
    }
    
    public void setNameComment(String comment) {
        Comment nameElement = document.createComment("Script Name: " + comment);
        mevElement.appendChild(nameElement);
        updateScript();
    }
    
    public void setDescriptionComment(String comment) {
        Comment dElement = document.createComment("Script Description: "+ comment);
        mevElement.appendChild(dElement);
        updateScript();
    }
    
    
    public Element createAlgorithmSet(int dataRef){
        Element algSetElement = document.createElement("alg_set");
        algSetElement.setAttribute("set_id", String.valueOf(currAlgSetID));
        algSetElement.setAttribute("input_data_ref", String.valueOf(dataRef));
        analysisElement.appendChild(algSetElement);
        updateScript();
        currAlgSetID++;
        return algSetElement;
    }
    
    public Element getAlgorithmSetByID(int setID){
        String id = String.valueOf(setID);
        NodeList elements = analysisElement.getElementsByTagName("alg_set");
        
        for(int i = 0; i < elements.getLength(); i++) {
            System.out.println("in getAlgSetByID("+id);
            System.out.println("attribute = "+((Element)elements.item(i)).getAttribute("set_id"));
            if(id.equals(((Element)elements.item(i)).getAttribute("set_id")))
                return (Element)elements.item(i);
        }
        return null;
    }
    
    
    public Element getAlgorithmSetByDataRef(int dataRef) {
        int setID = -1;
        String dataID = String.valueOf(dataRef);
        //if data ref. is to primary data return default alg set
        if(dataRef == 1)
            return getAlgorithmSetByID(1);
        
        NodeList list = document.getElementsByTagName("algorithm");
        Node algNode = null, node;
        for(int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            if(dataID.equals(((Element)node).getAttribute("input_data_ref"))){
                algNode = node;
                break;
            }
        }
        
        //the data node has no algs on it, create an alg set, pass the current id back
        if(algNode == null) {
            return this.createAlgorithmSet(dataRef);
        } else {
            return (Element)algNode.getParentNode();
        }
    }
    
    
    
    
    //need to get an algorithm ID, and get or make an alg set ref.
    
    public boolean appendAlgorithm(AlgorithmData data, int inputDataRef) {
        
        Element algElement = document.createElement("algorithm");
        Element currElement;
        boolean added = false;
        
        Element algSetElement = this.getAlgorithmSetByDataRef(inputDataRef);
        
        if(algSetElement == null) {
            System.out.println("null alg set element, bad id??");
            return false;
        }
        
        Text nameText, keyText, valueText;
        
        AlgorithmParameters params = data.getParams();
        String name = params.getString("name");
        String alg_type = params.getString("alg-type");
        int algID = algSetElement.getElementsByTagName("algorithm").getLength()+1;
        
        if(name != null){
            
            System.out.println("Name != null");
            
            //set attributes, name and input ref
            algElement.setAttribute("alg_name", name);
            algElement.setAttribute("input_data_ref", String.valueOf(inputDataRef));
            algElement.setAttribute("alg_id", String.valueOf(algID));
            algElement.setAttribute("alg_type", String.valueOf(alg_type));
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
            String outputClass;
            if(outputNodes != null){
                
                System.out.println("Output nodes are not null, being created");
                
                
                outputNodeElement = document.createElement("output_data");
                outputClass = data.getParams().getString("output-class");
                if(outputClass != null)
                    outputNodeElement.setAttribute("output_class", outputClass);
                for(int i = 0; i < outputNodes.length; i++) {
                    currDataID++;
                    dataElement = document.createElement("data_node");
                    dataElement.setAttribute("data_node_id", String.valueOf(this.currDataID));
                    dataElement.setAttribute("name", outputNodes[i]);
                    outputNodeElement.appendChild(dataElement);
                    System.out.println("output node ="+outputNodes[i]);
                }
                algElement.appendChild(outputNodeElement);
            }
            
            //keySet.
        }
        algSetElement.appendChild(algElement);
        added = true;
        System.out.println("child appended");
        updateScript();
        System.out.println("updateScript() done");
        fireScriptEvent();
        System.out.println("Fired script event");
        return added;
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
                
                //if key points to control data move on
                if(key.equals("name") || key.equals("output-class") || key.equals("alg-type"))
                    continue;
                
                //make a param element
                paramElement = document.createElement("param");
                
                //set key and value attributes
                paramElement.setAttribute("key", key);
                paramElement.setAttribute("value", value);
                
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
            matrixElement.setAttribute("type", "int-array");
            matrixElement.setAttribute("row_dim", String.valueOf(currentArray.length));
            matrixElement.setAttribute("col_dim", "1");
            
            for(int j = 0; j < currentArray.length; j++){
                elementElement = document.createElement("element");
                elementElement.setAttribute("row", String.valueOf(j));
                elementElement.setAttribute("col", "0");
                elementElement.setAttribute("value", String.valueOf(currentArray[j]));
                matrixElement.appendChild(elementElement);
            }
            
            mlist.appendChild(matrixElement);
        }
    }
    
    public void addStringArrays(Map map, Element mlist) {
        Object [] arrayObjectNames = map.keySet().toArray();
        Element matrixElement, elementElement;
        String [] currentArray;
        
        String key;
        
        Vector arrayNames = new Vector();
        
        for (int i = 0; i < arrayObjectNames.length; i++) {
            key = (String)arrayObjectNames[i];
            if(!(key.equals("output-nodes")))
                arrayNames.add(key);
        }
        
        if(arrayNames.size() < 1)
            return;
        
        for(int i = 0; i < arrayNames.size(); i++) {
            currentArray = (String [])map.get((String)(arrayNames.elementAt(i)));
            
            matrixElement = document.createElement("matrix");
            matrixElement.setAttribute("name", ((String)arrayNames.elementAt(i)));
            matrixElement.setAttribute("type", "string-array");
            matrixElement.setAttribute("row_dim", String.valueOf(currentArray.length));
            matrixElement.setAttribute("col_dim", "1");
            
            for(int j = 0; j < currentArray.length; j++){
                elementElement = document.createElement("element");
                elementElement.setAttribute("row", String.valueOf(j));
                elementElement.setAttribute("col", "0");
                elementElement.setAttribute("value", currentArray[j]);
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
            matrixElement.setAttribute("col_dim", String.valueOf(currentFM.getColumnDimension()));
            
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
                elementElement.setAttribute("value", String.valueOf(data[i][j]));
                matrixElement.appendChild(elementElement);
            }
        }
    }
    
    public void writeDocument(String fileName) throws IOException {
        FileWriter writer = new java.io.FileWriter(fileName);
        serialize(writer);
        
        writer.flush();
        writer.close();
    }
    
    
    public void writeDocument(Writer writer) throws IOException {
        serialize(writer);
    }
    
    
    
    /*****************************
     *
     * Serialization Code
     *
     */
    private void serialize(Writer writer) throws IOException {
        serializeNode(document,  writer, "");
    }
    
    private void serializeNode(Node node, Writer writer, String indentLevel) throws IOException {
        String name;
        String text;
        StringTokenizer stok;
        
        switch(node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                writer.write("<?xml version=\"1.0\"?>");
                writer.write(lineSeparator);
                writer.write(ScriptConstants.DOCTYPE_STRING);
                writer.write(lineSeparator);
                Document doc = (Document)node;
                serializeNode(doc.getDocumentElement(), writer, " ");
                break;
            case Node.ELEMENT_NODE:
                boolean haveContent = false;
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
                // name = node.getNodeName();
                //// String text = node.getNodeValue();
                // writer.write(indentLevel + "<" + name + ">" + text + "<\\" + name +">");
                // writer.write(lineSeparator);
                break;
            case Node.COMMENT_NODE:
                text = node.getNodeValue();
                writer.write("<!--");
                
                stok = new StringTokenizer(text, " ");
                int charCnt;
                String word;
                
                while(stok.hasMoreElements()) {
                    charCnt = 0;
                    while(charCnt < 50){
                        word = stok.nextToken();
                        writer.write(word);
                        charCnt = word.length();
                    }
                    writer.write(lineSeparator);
                }
                writer.write(" -->");
                break;
        }
    }
    
    public void updateScript() {
        //        CharArrayWriter writer = new CharArrayWriter();
        try {
            scriptText = "";
            writeScriptText(document, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //      this.scriptText = writer.toString();
    }
    
    
    private void writeScriptText(Node node, String indentLevel) throws IOException {
        String name;
        String text;
        StringTokenizer stok;
        
        switch(node.getNodeType()) {
            
            case Node.DOCUMENT_NODE:
                scriptText += ("<?xml version=\"1.0\"?>");
                scriptText += (lineSeparator);
                
                //scriptText += (ScriptConstants.DOCTYPE_STRING);
                //scriptText += (lineSeparator);
                
                NodeList nodes = node.getChildNodes();
                if(nodes != null) {
                    for(int i = 0; i < nodes.getLength(); i++) {
                        writeScriptText(nodes.item(i), "");
                    }
                }
                /*
                Document doc = (Document)node;
                writeScriptText(doc.getDocumentElement()," ");
                 **/
                break;
            case Node.ELEMENT_NODE:
                name = node.getNodeName();
                //    if(parsedScript)
                //       scriptText += "<"+name;
                //    else
                scriptText += (indentLevel + "<" + name);
                
                //posible attributes
                NamedNodeMap attrs = node.getAttributes();
                for(int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    scriptText += (" "+ attr.getNodeName()+"=\""+attr.getNodeValue()+"\"");
                }
                
                NodeList children = node.getChildNodes();
                if(children.getLength() > 0) {
                    
                    scriptText += (">");
                    
                    if((children.item(0) != null) &&
                    (children.item(0).getNodeType() == Node.ELEMENT_NODE || children.item(0).getNodeType() == Node.COMMENT_NODE)){
                        scriptText += (lineSeparator);
                    }
                    for(int i = 0; i < children.getLength(); i++){
                        writeScriptText(children.item(i), indentLevel + indent);
                    }
                    if((children.item(0) != null) &&
                    (children.item(children.getLength()-1).getNodeType() == Node.ELEMENT_NODE)) {
                        scriptText += (indentLevel);
                    }
                    
                    scriptText += ("</" + name + ">");
                    
                } else {
                    scriptText += ("/>");
                }
                
                scriptText += (lineSeparator);
                break;
            case Node.TEXT_NODE:
                // text = node.getNodeValue();
                // scriptText += indentLevel+text+lineSeparator;
                
                
                
                String newText = node.getNodeValue();
                String preText = "";
                int pt;
                if(newText.indexOf("\n") != -1 && scriptText.lastIndexOf('\n') == scriptText.length()-1){
                    pt = newText.lastIndexOf("\n")+1;
                    if(pt < newText.length()) {
                        preText = newText.substring(pt);
                        preText +=newText.substring(0, pt-1);
                        newText = preText;
                    }
                    
                }
                
                
                scriptText += newText;
                
                // name = node.getNodeName();
                //// String text = node.getNodeValue();
                // scriptText += (indentLevel + "<" + name + ">" + text + "<\\" + name +">");
                // scriptText += (lineSeparator);
                break;
                
            case Node.COMMENT_NODE:
                text = node.getNodeValue();
                scriptText += indentLevel+"<!-- ";
                /*
                stok = new StringTokenizer(text, " ");
                int charCnt = 0;
                String word;
                 
                while(stok.hasMoreElements()) {
                 
                    word = stok.nextToken();
                    if (charCnt < 50){
                        scriptText += word+" ";
                        charCnt += word.length();
                    } else {
                        scriptText += lineSeparator+indentLevel+"     ";
                        scriptText += word+" ";
                        charCnt = 0;
                    }
                }
                 */
                scriptText += text;
                
                scriptText += " -->"+lineSeparator+lineSeparator;
                break;
            case Node.DOCUMENT_TYPE_NODE:
                DocumentType docType = (DocumentType)node;
                scriptText += ("<!DOCTYPE " + docType.getName());
                scriptText += " SYSTEM ";
                scriptText += "\""+docType.getSystemId()+"\">";
                scriptText += lineSeparator;
                break;
        }
    }
    
    public String toString() {
        return scriptText;
    }
    
    public ErrorLog getErrorLog() {
        return errorLog;
    }
    
    public void showErrorLog() {
        
    }
    
    public int getErrorCount() {
        return parseErrors;
    }
    
    
    
    public boolean loadXMLFile(File inputFile, Progress progress) throws Exception{
        if(!inputFile.exists())
            return false;
        
        progress.setUnits(4);
        progress.setValue(1);
        progress.setDescription("Parsing File");
        
        errorLog.setFile(inputFile);
        
        
        DOMParser parser = new DOMParser();
        
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            // System.out.println("dtd url: *"+new File(System.getProperty("user.dir")+"\\Data\\mev_script_dtd.dtd").toURL().toString()+"**");
            
            // parser.setFeature(new File(System.getProperty("user.dir")+"\\Data\\mev_script_dtd.dtd").toURL().toString(), true);
            parser.setErrorHandler(this);
            parser.parse(inputFile.toURL().toString());
            
        } catch ( Exception e ) {
            return false;
        }
        
        document = parser.getDocument();
        
        //now set key element references.
        root = document.getDocumentElement();
        
        
        NodeList list = root.getElementsByTagName("mev");
        if(list != null && list.getLength() > 0)
            mevElement = (Element)list.item(0);
        
        
        list = root.getElementsByTagName("primary_data");
        if(list != null && list.getLength() > 0)
            primaryDataElement = (Element)list.item(0);
        
        list = root.getElementsByTagName("analysis");
        if(list != null && list.getLength() > 0)
            analysisElement = (Element)list.item(0);
        
        progress.setValue(2);
        progress.setDescription("Checking Algorithm Sets");
        
        //set alg_set counter value
        list = root.getElementsByTagName("alg_set");
        if(list != null && list.getLength() > 0) {
            int maxID = -1;
            for(int i = 0; i < list.getLength(); i++) {
                currAlgSetID = Integer.parseInt(((Element)list.item(i)).getAttribute("set_id"));
                if(currAlgSetID > maxID)
                    maxID = currAlgSetID;
            }
            if(currAlgSetID < 0)
                currAlgSetID = 0;
            else
                currAlgSetID = maxID;
            currAlgSetID++; //Increment for next
        }
        
        progress.setValue(2);
        progress.setDescription("Setting Output ID");
        
        //set data_node counter value
        list = root.getElementsByTagName("data_node");
        if(list != null && list.getLength() > 0) {
            int maxID = -1;
            for(int i = 0; i < list.getLength(); i++) {
                currDataID = Integer.parseInt(((Element)list.item(i)).getAttribute("data_node_id"));
                if(currDataID > maxID)
                    maxID = currDataID;
            }
            if(currDataID < 0)
                currDataID = 0;
            else
                currDataID = maxID;  //preincremented before use
        }
        
        
        progress.setValue(3);
        progress.setDescription("Internal Serialization");
        
        System.out.println("load xml file:");
        parsedScript = true;
        updateScript();
        
        //System.out.println(this.toString());
        //Extract name, description and date tags if availible
        progress.setValue(4);
        progress.setDescription("Done");
        progress.dispose();
        
        return validateDocument();
    }
    
    public boolean validateDocument() {
        return true;
    }
    
    
    public boolean modifyParameter(Hashtable attributes, String modLine, String value) {
        NodeList list = document.getElementsByTagName("algorithm");
        Element algElement;
        for(int i = 0; i < list.getLength(); i++) {
            algElement = (Element)list.item(i);
            System.out.println("********************");
            if(algorithmMatches(attributes, algElement)) {
                return modifyAlgorithmParameter(algElement, modLine, value);
            }
        }
        return false;
    }
    
    
    //find and mod the parameter
    public boolean modifyAlgorithmParameter(Element elem, String modLine, String value) {
        NodeList list = elem.getElementsByTagName("param");
        Element param;
        String key;
        System.out.println("Mod alg param");
        for(int i =0; i < list.getLength(); i++) {
            param = (Element)list.item(i);
            key = param.getAttribute("key");
            
            if(modLine.indexOf(key) > -1) {
                System.out.println("Set new att value for param key val"+key+" "+value);
                param.setAttribute("value", value);
                fireScriptEvent();
                return true;
            }
        }
        return false;
        
    }
    
    //verify algorithm Element is correct
    private boolean algorithmMatches(Hashtable attributes, Element algorithm) {
        Enumeration enum = attributes.keys();
        String key, value, algValue;
        boolean match = true;
        while(enum.hasMoreElements()) {
            key = (String)enum.nextElement();
            value = (String)attributes.get(key);
            algValue = algorithm.getAttribute(key);
            System.out.println("key = "+key+" value = *"+value+"* docValue = *"+algValue+"*");
            if(!algValue.equals(value))
                return false;
        }
        return true;
    }
    /*
    public static void main(String [] args) {
        DocumentBase hand = new DocumentBase();
     
     
     
     
        AlgorithmData data = new AlgorithmData();
        data.addParam("name", "KMC");
        data.addParam("k", "10");
        data.addParam("iter", "50");
        data.addParam("eval-genes", "true");
        data.addParam("alg_type", "cluster");
     
        data.addParam("output-class", "multi-cluster-output");
     
     
        String [] outArray = new String[1];
     
        outArray[0] = "Multi-cluster Result";
        data.addStringArray("output-nodes", outArray);
     
        FloatMatrix matrix = new FloatMatrix(3,3);
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                matrix.set(i, j, (float)(i/(j+1)));
            }
        }
        data.addMatrix("fuzzy-factor", matrix);
     
     
        //   hand.createAlgorithmSet(1,1);
        hand.appendAlgorithm(data, 1);
     
        data = new AlgorithmData();
        data.addParam("name", "TTEST");
        data.addParam("type", "TTEST.TWO_CLASS");
        data.addParam("alpha", "0.01");
        data.addParam("p-correction", "TTEST.NO_CORRECTION");
        data.addParam("alg_type", "cluster");
     
        int [] groupAssignments = new int[10];
     
        for(int i = 0; i < 10; i++) {
            groupAssignments[i] = i%2;
        }
     
        data.addIntArray("group-assignments", groupAssignments);
     
        groupAssignments = new int[12];
     
        for(int i = 0; i < 12; i++) {
            groupAssignments[i] = i%2;
        }
     
        data.addIntArray("another-array-test", groupAssignments);
     
        data.addParam("output-class", "partition-output");
     
        outArray = new String[2];
        outArray[0] = "significant";
        outArray[1] = "non significant";
        data.addStringArray("output-nodes", outArray);
     
        hand.appendAlgorithm(data, 1);
     
     
     
        try {
     
            //XMLSerializer serial = new XMLSerializer();
     
            //serial.set(new FileWriter("dfaf"));
     
            //serial.serialize(hand.getDocument());
     
            // hand.serialize(hand.getDocument(), new FileWriter(new File("C:\\Temp\\kmc_script.xml")));
            hand.writeDocument("C:\\MyProjects\\source_3_5\\Data\\kmc_script2.xml");
     
     
     
            java.io.CharArrayWriter caw = new java.io.CharArrayWriter();
     
            // hand.writeDocument(hand.getDocument(), caw);
            hand.writeDocument(caw);
            char [] array = caw.toCharArray();
            String s = new String(array);
            System.out.println(s);
     
            System.out.println("Try a write");
            hand.updateScript();
            System.out.println(hand.toString());
     
            javax.swing.JEditorPane pane = new javax.swing.JEditorPane("text/plain",s);
            pane.setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN, 12));
            System.out.println(pane.getContentType());
            pane.setBackground(java.awt.Color.lightGray);
            pane.setMargin(new java.awt.Insets(10,10,10,10));
            javax.swing.JFrame frame = new javax.swing.JFrame();
            frame.getContentPane().add(pane);
            frame.setSize(300,500);
            frame.setVisible(true);
     
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println("Done");
    }
     */
    
    //  WARNING Event Handler
    public void warning(SAXParseException e)
    throws SAXException {
        System.err.println("Warning:  "+e);
        parseErrors++;
        errorLog.recordWarning(e);
    }
    
    //  ERROR Event Handler
    public void error(SAXParseException e)
    throws SAXException {
        System.err.println("Error:  "+e);
        errorLog.recordError(e);
        parseErrors++;
    }
    
    //  FATAL ERROR Event Handler
    public void fatalError(SAXParseException e)
    throws SAXException {
        System.err.println("Fatal Error:  "+e);
        errorLog.recordFatalError(e);
        parseErrors++;
    }
    
    
    public void addDocumentListener(ScriptEventListener listener) {
        if(!listeners.contains(listener))
            listeners.add(listener);
    }
    
    private void fireScriptEvent() {
        ScriptDocumentEvent event = new ScriptDocumentEvent(this);
        for(int i = 0; i < listeners.size(); i++) {
            ((ScriptEventListener)listeners.elementAt(i)).documentChanged(event);
        }
    }
    
    public void removeScriptListener(ScriptEventListener listener) {
        listeners.remove(listener);
    }
    
    public void removeAlgorithm(AlgorithmNode node) {
        int algID = node.getID();
        int dataRef = node.getDataNodeRef();
        String name = node.getAlgorithmName();
        
        Element algSetElement = getAlgorithmSetByDataRef(dataRef);
        Element algorithmElement = null;
        NodeList list;
        boolean found = false;
        if(algSetElement != null) {
            list = algSetElement.getElementsByTagName("algorithm");
            for(int i = 0; i < list.getLength(); i++) {
                algorithmElement = (Element)list.item(i);
                if((String.valueOf(algID)).equals(algorithmElement.getAttribute("alg_id"))
                && (name.equals(algorithmElement.getAttribute("alg_name")))) {
                    found = true;
                    break;
                }
            }
            if(found) {
                //Need to remove lower alg sets if present and possibly
                //roll back algset number.
                
                if(algorithmElement == null)
                    return;
  
                //Decided not to rollback alg id's for deleted
                Node parentNode = algorithmElement.getParentNode();
                if(parentNode != null) {
                    
                    //Get output node and then the data node id's
                    list = algorithmElement.getElementsByTagName("data_node");
                    
                    //((Element)(algorithmElement.getElementsByTagName("output_data").item(0))).getElementsByTagName("data_node");
                    String [] data_refs = new String[list.getLength()];
                    Node dataNode;
                    for(int i = 0; i < list.getLength(); i++) {
                        dataNode = list.item(i);
                        data_refs[i] = ((Element)dataNode).getAttribute("data_node_id");
                    }
                    
                    //Check if this analysis node has algorithm sets that reference
                    //the output of the algorithm to delete, if so remove them.
                    list = analysisElement.getElementsByTagName("alg_set");
                    Node algSetNode;
                    String ref;
                    for(int i = 0; i < list.getLength(); i++) {
                        algSetNode = list.item(i);
                        ref = ((Element)algSetNode).getAttribute("input_data_ref");
                        for(int j = 0; j < data_refs.length; j++) {
                            if(data_refs[j].equals(ref))
                                analysisElement.removeChild(algSetNode);
                        }
                    }
                    
                    parentNode.removeChild(algorithmElement);
                    
                    //Check for more algorithms
                    if(parentNode.getChildNodes().getLength() == 0) {
                        Node grandparent = parentNode.getParentNode();
                        if(grandparent != null && parentNode.getNodeName().equals("alg_set")
                        && !(((Element)parentNode).getAttribute("set_id")).equals("1")){
                            //parent was an algset without children
                            //and it was not the base algset "1"
                            grandparent.removeChild(parentNode);
                        }
                    }
                }
                updateScript();
                fireScriptEvent();
            }
        }
    }
    
    
    
}
