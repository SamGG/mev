package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
/**
* This class implements the gene list panel in the upper left corner of
 * the ChARM UI display.
*
 * <p>Title: GeneList</p>
 * <p>Description: This class implements the gene list panel in the upper left corner of
 * the ChARM UI display.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Xing Chen
 * @version 1.0
 */

public class GeneList extends JList {

  public static final int WIDTH = 300;
  public static final int HEIGHT = 300;

  public static final Color BACKGROUND = Color.LIGHT_GRAY;

  //Gene[] genes;
  CGHClone[] genes;
  String currexp;
  MultipleArrayData data;
  
  TitledBorder listborder;

  /**
   * Class constructor.
   */
  public GeneList(MultipleArrayData data) {
    super();
    //setPreferredSize(new Dimension(WIDTH, HEIGHT));
    currexp = "";
    genes = null;
    this.data =data;
    listborder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Genes List");
    setBorder(listborder);
    setListData(new String[]{"Select one or more genes to display gene information."});
    setBackground(BACKGROUND);
  }

  /**
   * Clears the gene list display.
   */
  public void clearGenes(){
    genes = null;
    currexp = "";
    System.gc();
    setListData(new String[]{"Select one or more genes to display gene information."});
  }

  /**
   * Indicates whether the display is empty or contains genes.
   * @return boolean
   */
  public boolean containsGenes(){
    return (genes != null);
  }

  /**
   * Sets the current gene list to the given array.
   * @param gene_selections Gene[]
   * @param experiment String
   */
  //public void setGenes(Gene[] gene_selections, String experiment) {
  public void setGenes(CGHClone[] gene_selections, String experiment) {
	
    setPreferredSize(new Dimension(WIDTH, gene_selections.length*20));
    currexp = experiment;
    genes = gene_selections;
    String[] geneinfo;

    if ((genes != null)&&(genes.length > 0)) {
      listborder.setTitle("Experiment "+experiment);
      geneinfo = new String[genes.length];

      for (int i = 0; i < geneinfo.length; i++) { 
          geneinfo[i] = "" + genes[i].getName() + ":    Chr "+genes[i].getChromosome()+    ",     Log2 Ratio = " + genes[i].getRatio() + ",	Desc - " + genes[i].getDesc(data);
        }

      setListData(geneinfo);
    }
    else {
      listborder.setTitle("Gene List");
      setListData(new String[]{"Select one or more genes to display gene information."});
    }

    //repaint();
    //setListData(new String[] {"---------Genes were selected----", "a", "b"});
  }

  /**
   * Sets the current gene list to the given ArrayList.
   * @param gene_selections ArrayList
   * @param experiment String
   */
  public void setGenes(ArrayList gene_selections, String experiment) {

  setPreferredSize(new Dimension(WIDTH, gene_selections.size()*20));
  currexp = experiment;
  String[] geneinfo;
  /*
  for (int i = 0; i < gene_selections.size(); i++){
    genes[i] = (CGHClone)gene_selections.get(i);
  }
*/
  if (/*(genes != null)&&*/(gene_selections.size() > 0)) {
    listborder.setTitle("Experiment "+experiment);
    geneinfo = new String[gene_selections.size()];
    genes = new CGHClone[ gene_selections.size() ];
    
    for (int i = 0; i < geneinfo.length; i++) {
    	genes[i] = (CGHClone)gene_selections.get(i);
        if(genes[i] != null) 
        	geneinfo[i] = "" + genes[i].getName() + ":    Chr "+genes[i].getChromosome()+ ",  Value: " + genes[i].getRatio()+ ",	Desc - " + genes[i].getDesc(data);
        else geneinfo[i] = " ";
      }

    setListData(geneinfo);
  }
  else {
    listborder.setTitle("Gene List");
    setListData(new String[]{"Select one or more genes to display gene information."});
  }
}

/**
 * Returns the current gene array.
 * @return Gene[]
 */
public CGHClone[] getGenes(){
    if (genes != null) {
      return genes;
    }
    else {
      return null;
    }
  }

  /**
   * TODO
   * Returns the selected gene list entries.
   * @return Gene[]
   */
  public CGHClone[] getSelectedGenes(){
    if (genes != null) {
      int[] indices = getSelectedIndices();
      if (indices.length > 0){
    	  CGHClone[] selected = new CGHClone[indices.length];
        for (int i = 0; i < indices.length; i++) {
           if(genes[indices[i]] != null) selected[i] = genes[indices[i]];
        }
        return selected;
      }
      else {
        return null;
      }
    }
    else {
      return null;
    }
  }

  /**
   * Writes all the genes in the current list to the specified file.
   * @param file String
   */
  public void writeGenesToFile(String file){
    if (genes != null){
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));

        out.write("Experiment\tChromosome\tGene ID\t");
        //ArrayList fields = genes[0].getDataFields();
        //for(int i=0; i<fields.size(); i++) {
          //out.write(fields.get(i)+"\t");
        //}
        out.write("Value");
        out.newLine();
        for (int i = 0; i < genes.length; i++) {

          out.write(currexp + "\t" + genes[i].getChromosome()+"\t"+genes[i].getName() + "\t");
          //for(int j=0; j<fields.size(); j++) {
            //out.write(genes[i].getData((String)fields.get(j))+"\t");
          //}
          out.write(genes[i].getRatio(/*currexp*/)+"");
          out.newLine();
        }
        out.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * Returns the experiment corresponding to the current list of genes.
   * @return String
   */
  public String getExperiment(){
    return currexp;
  }
}
