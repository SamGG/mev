/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptParser.java
 *
 * Created on December 15, 2003, 10:59 PM
 */

package org.tigr.microarray.mev.script.util;

import java.io.*;

//Dom imports
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

//Parser Import
import org.apache.xerces.dom.DOMImplementationImpl;

import org.apache.xerces.parsers.DOMParser;
/**
 *
 * @author  braisted
 */
public class ScriptParser {
    
    Document doc;
    String lineSeparator = "\n\n";
    String indent = "   ";
    
    /** Creates a new instance of ScriptParser */
    public ScriptParser() {
        
    }
    
    public void printValues(String fileName) throws Exception {
        //File specification
        File file = new File(fileName);
        //Intantiate parser
        DOMParser parser = new DOMParser();
        //Parse file
        parser.parse(file.toURL().toString());
        //get document
        doc = parser.getDocument();
        //get document root
        Element analysis = doc.getDocumentElement();
        
        NodeList algSets = analysis.getElementsByTagNameNS("","alg_set");
        NodeIterator algorithmIterator;
        Element algSet;
        
        Element algorithm;
      
        
        for(int i = 0; i < algSets.getLength(); i++){
            
            algSet = (Element)algSets.item(i);
            
            algorithmIterator = ((DocumentTraversal)doc).createNodeIterator(algSet, NodeFilter.SHOW_ALL, null, true);
        
            Node node;
            while((node = algorithmIterator.nextNode()) != null) {
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    System.out.println("Node name = "+node.getNodeName());
                } else if(node.getNodeType() == Node.TEXT_NODE){
                    System.out.println("Value = "+node.getNodeValue());
                }
                System.out.println("=========================");
                
            }
            
        
        }   
        
    }
     
    public void writeDocument(String fileName) throws IOException {
        java.io.FileWriter writer = new java.io.FileWriter(fileName);
        serialize(doc, writer);        
        writer.flush();
        writer.close();
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
                
                writer.write(">");
                
                NodeList children = node.getChildNodes();
                if(children != null) {
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
                    
                    
                }
                writer.write("</" + name + ">");
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
                break;
        }
    }
    
    public static void main(String [] args){
        ScriptParser sp = new ScriptParser();
        try{
        sp.printValues("c:/Temp/script.xml");

        sp.writeDocument("c:/Temp/result_script.xml");
        
        } catch (Exception e) {e.printStackTrace();}
    }
}
