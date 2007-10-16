package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

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
 * @author  Raktim Sinha
 */

public class GeneTable extends JTable{

  public static final int WIDTH = 300;
  public static final int HEIGHT = 300;

  public static final Color BACKGROUND = Color.LIGHT_GRAY;

  //Gene[] genes;
  CGHClone[] genes;
  String currexp;
  MultipleArrayData data;
  
  TableDataModel tableData;
  //JTable table; 
  //TitledBorder listborder;

  /**
   * Class constructor.
   */
  public GeneTable(MultipleArrayData data/*, TableDataModel tableData*/) {
    super(/*tableData*/);
    //setPreferredSize(new Dimension(WIDTH, HEIGHT));
    currexp = "";
    genes = null;
    this.data =data;
    //listborder = BorderFactory.createTitledBorder("Genes List");
    //listborder.setTitleJustification(TitledBorder.CENTER);

    //table = new JTable(new TableDataModel());
    //setPreferredScrollableViewportSize(new Dimension(500, 70));
    
    //setBorder(listborder);
    //setListData(new String[]{"Select one or more genes to display gene information."});
    //setModel(tableData);
    setBackground(BACKGROUND);
  }

  /**
   * Clears the gene list display.
   */
  public void clearGenes(){
    genes = null;
    currexp = "";
    System.gc();
    //setListData(new String[]{"Select one or more genes to display gene information."});
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
	
    //setPreferredSize(new Dimension(WIDTH, gene_selections.length*20));
    currexp = experiment;
    genes = gene_selections;
    Object[][] geneinfo;

    if ((genes != null)&&(genes.length > 0)) {
      
      geneinfo = new Object[genes.length][4];

      for (int i = 0; i < genes.length; i++) { 
          geneinfo[i][0] = genes[i].getName();
          geneinfo[i][1] = genes[i].getChromosome();
          geneinfo[i][2] = genes[i].getRatio();
          geneinfo[i][3] =genes[i].getDesc(data);
        }
      
      tableData = new TableDataModel(geneinfo);
      setModel(tableData);
      initCols();
      //listborder.setTitle("Experiment "+experiment);
      //setListData(geneinfo);
    }
    else {
      //listborder.setTitle("Gene List");
      //setListData(new String[]{"Select one or more genes to display gene information."});
      Object[][] info;
      info = new String[1][4];
      info[0][0] = "Probe Name";
      info[0][1] = "Chromosome";
      info[0][2] = "Log2 Ratio";
      info[0][3] = "Desc";
      tableData = new TableDataModel(info);
      
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

  //setPreferredSize(new Dimension(WIDTH, gene_selections.size()*20));
  currexp = experiment;
  Object[][] geneinfo;
  /*
  for (int i = 0; i < gene_selections.size(); i++){
    genes[i] = (CGHClone)gene_selections.get(i);
  }
*/
  if (/*(genes != null)&&*/(gene_selections.size() > 0)) {
    //listborder.setTitle("Experiment "+experiment);
    geneinfo = new Object[gene_selections.size()][4];
    genes = new CGHClone[gene_selections.size()];
    
    //System.out.println("Gene Sel: " + gene_selections.size());
    for (int i = 0; i < gene_selections.size(); i++) {
    	genes[i] = (CGHClone)gene_selections.get(i);
    	//System.out.println(i + ": " + genes[i].getName());
    	//for (int j = 0; j < geneinfo[0].length; j++) { 
    		if(genes[i] != null) {
	            geneinfo[i][0] = genes[i].getName();
	            geneinfo[i][1] = genes[i].getChromosome();
	            geneinfo[i][2] = genes[i].getRatio();
	            geneinfo[i][3] = genes[i].getDesc(data);
    		}
    		else {
            	//geneinfo[i] = " ";
            	geneinfo[i][0] = "";
                geneinfo[i][1] = "";
                geneinfo[i][2] = "";
                geneinfo[i][3] = "";
            }
          //}
    	}
    //setListData(geneinfo);
    tableData = new TableDataModel(geneinfo);
    setModel(tableData);
    initCols();
  }
  else {
    //listborder.setTitle("Gene List");
    //setListData(new String[]{"Select one or more genes to display gene information."});
    Object[][] info;
    info = new String[1][4];
    info[0][0] = "Probe Name";
    info[0][1] = "Chromosome";
    info[0][2] = "Log2 Ratio";
    info[0][3] = "Desc";
    tableData = new TableDataModel(info);
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
      //int[] indices = getSelectedIndices();
      int[] indices = getSelectedRows();
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
  
  private void initCols(){
	  TableColumn column = null;
	  for (int i = 0; i < 4; i++) {
	      column = getColumnModel().getColumn(i);
	      if (i == 3) {
	          column.setPreferredWidth(400); //Desc column is bigger
	      } else if (i == 1) {
	    	  column.setPreferredWidth(20);
	      }  else {
	          column.setPreferredWidth(30);
	      }
	  }


  }
}
