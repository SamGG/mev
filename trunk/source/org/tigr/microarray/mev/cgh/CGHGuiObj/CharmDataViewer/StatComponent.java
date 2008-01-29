package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IData;
/**
* This class implements the statistic/annotation panel in the lower left corner of
 * the ChARM UI display.
*
 * <p>Title: StatComponent</p>
 * <p>Description: This class implements the statistic/annotation panel in the lower left corner of
 * the ChARM UI display.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

public class StatComponent extends JLabel {

  public static final Color BACKGROUND = Color.LIGHT_GRAY;

  public static final int WIDTH = 30;
  public static final int HEIGHT = 50;
  public static final int LINE_HEIGHT = 25;

  // charm.Window[] windows; Rakitm 8/31
  SegmentInfo[] segInfos;
  //Gene[] genes;
  CGHClone[] genes;
  //Chromosome chromosome;
  int chromosome;
  String instructions;
  String experiment;
  boolean windowset;
  boolean geneset;
  
  IData data;

  /**
   * Class constructor.
   */
  public StatComponent(IData data) {
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
    setBackground(BACKGROUND);
    windowset = geneset = false;
    this.data = data;
  }

  /**
   * Sets the list of segInfos in the display panel to the supplied window array.
   * @param win Window[]
   * @param chromo Chromosome
   * @param exp String
   */
  //public void setWindows(SegmentInfo[] segs, Chromosome chromo, String exp){
  public void setWindows(SegmentInfo[] segs, int chromo, String exp){
    segInfos = segs;
    experiment = exp;
    chromosome = chromo;
    geneset = false;
    windowset = true;
    setPreferredSize(new Dimension(getWidth(), Math.max(LINE_HEIGHT*segInfos.length*11, HEIGHT)) );
    revalidate();
    repaint();
  }

  /**
   * Clears statistics display panel.
   */
  public void clearDisplay(){
    segInfos = null;
    genes = null;
    System.gc();
    windowset = geneset = false;
    setPreferredSize(new Dimension(WIDTH, HEIGHT) );
    revalidate();
    repaint();
  }

  /**
   * Sets the display panel content to the list of genes in the supplied array.
   * @param input_genes Gene[]
   * @param exp String
   */
  public void setGenes(CGHClone[] input_genes, String exp){
    genes = input_genes;
    geneset = true;
    windowset = false;
    experiment = exp;
    setPreferredSize(new Dimension(getWidth(), Math.max(LINE_HEIGHT*genes.length*6, HEIGHT)) );
    revalidate();
    repaint();
  }

  /**
   * Indicates whether the panel currently contains segInfos (predictions) or gene annotations.
   * @return boolean
   */
  public boolean containsWindows(){
    return (segInfos != null);
  }

  /**
   * Write current list of predictions to the specified file.
   * @param file String
   */
  public void writeWindowToFile(String file){

    if (segInfos != null){
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));

        out.newLine();
        out.newLine();

        out.write("Experiment\tChromosome\t" +
                  "First gene start bp\tLast gene end bp\tStart Gene ID\tEnd Gene ID\t" +
                  "Sign positive p-value\tSign negative p-value\t" +
                  "Mean p-value");

        out.newLine();

        for (int i = 0; i < segInfos.length; i++) {
    	
          //Gene startGene = chromosome.geneAtRatioIndex(segInfos[i].getStart(),experiment,false);
          //Gene endGene = chromosome.geneAtRatioIndex(segInfos[i].getEnd(),experiment,false);

          //out.write(experiment + "\t" + chromosome.getNumber() + "\t" +
        	out.write(experiment + "\t" + chromosome + "\t" +
          /*
          startGene.getStart()+"\t"+
          endGene.getEnd()+"\t"+
          startGene.getID()+"\t"+
          endGene.getID()+"\t"+
          */
          data.getCloneAt(segInfos[i].getStart(), chromosome) + "\t" +
          data.getCloneAt(segInfos[i].getEnd(), chromosome) + "\t" +
          segInfos[i].getStatistic("Sign pos") +"\t"+ segInfos[i].getStatistic("Sign neg") +"\t"+
          segInfos[i].getStatistic("Mean"));

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
   * Paints this component- either window stats, gene annotations, or the default opening display.
   * @param g Graphics
   */
  public void paintComponent(Graphics g){

    int width = getWidth();
    int height = getHeight();

    if (windowset){
      //paintWindowStats(g, width, height);
    }
    else if (geneset){
      paintGeneInfo(g, width, height);
    }
    else {
      //paintOpeningDisp(g, width, height);
    }

  }

  /**
   * Paint annotations for current list of genes.
   * @param g Graphics
   * @param width int
   * @param height int
   */
  public void paintGeneInfo(Graphics g, int width, int height){
    g.setFont(new Font("myfont", Font.ROMAN_BASELINE, 12));

    //ArrayList fields = genes[0].getDataFields();

    for (int i = 0; i < genes.length; i++) {
      g.setColor(Color.BLACK);
      if (i % 2 == 0){
        g.setColor(Color.BLUE);
      }
      int curr_line = LINE_HEIGHT * i * 5 + LINE_HEIGHT + 10;

      g.drawString("Gene: " + genes[i].getName(),5,curr_line);
      g.drawString("Chromosome: "+genes[i].getChromosome(),5,curr_line +LINE_HEIGHT);
      g.drawString("Value: " +genes[i].getRatio(/*experiment*/), 5, curr_line+LINE_HEIGHT*2);

        //for(int j=0; j<fields.size(); j++) {
          //g.drawString(fields.get(j)+": "+genes[i].getData((String)fields.get(j)),5,curr_line+LINE_HEIGHT*(3+j));
        //}

      //g.drawString("Common Name: " + genes[i].getName(), 5, curr_line + LINE_HEIGHT*3);
      //g.drawString("Function: "+ genes[i].getFunction() , 5, curr_line + (4*LINE_HEIGHT));
      //g.drawString("Strand: " + genes[i].getStrand(), 5, curr_line + 3*LINE_HEIGHT);
      //g.drawString("Start/End base pairs: "+
      //             genes[i].getStart() + " / "+genes[i].getEnd(), 5,curr_line + 3*LINE_HEIGHT);
      //g.setColor(Color.white);
      //g.drawLine(0, (int)(5.5*LINE_HEIGHT), getWidth(), (int)(4.5*LINE_HEIGHT));
    }

  }

  /**
   * Probably Not required
   * Paint default display.
   * @param g Graphics
   * @param width int
   * @param height int
   */
  public void paintOpeningDisp(Graphics g, int width, int height){
     g.drawString("Select a prediction bar to view test results", 5, height / 10);
  }

  /**
   * Probably Not REquired
   * Paint statistics for the current set of segInfos. (window list must be set first)
   * @param g Graphics
   * @param width int
   * @param height int
   */
  public void paintWindowStats(Graphics g, int width, int height){

    g.setFont(new Font("myfont", Font.ROMAN_BASELINE, 12));

    for (int i = 0; i < segInfos.length; i++){
      int offset = i * 10 * LINE_HEIGHT;
      //charm.Window window = windows[i]; Rakitm 8/31
      SegmentInfo segInfo = segInfos[i];
      
      CGHClone startgene = data.getCloneAt(segInfos[i].getStart(), chromosome);
      CGHClone endgene = data.getCloneAt(segInfos[i].getEnd(), chromosome);
      //Gene startgene = chromosome.geneAtRatioIndex( (int) segInfo.getStart(),experiment,false);
      //Gene endgene = chromosome.geneAtRatioIndex( (int) segInfo.getEnd(),experiment,false);

      if ((startgene != null)&&(endgene != null)){
        g.setColor(Color.BLUE);

        g.drawString("Prediction Statistics", width / 4, LINE_HEIGHT + offset);


        /*g.drawString("Bounds (base pair coordinates): [" +
                     startgene.getStart() + " , " + endgene.getStart() + "]",
                     5, 2 * LINE_HEIGHT + offset);*/
        int indentSize = 15;

        g.drawString("Start/End gene IDs: " + "[" + startgene.getName()/* .getID()*/ + " , " +
                     endgene.getName()/* .getID()*/ + "]", 5, LINE_HEIGHT * 2 + offset);
        g.drawString("Significance test results:", 5, LINE_HEIGHT * 3 + offset);

        g.drawString("Mean p-value = " + segInfo.getStatistic("Mean"), 5+indentSize,
                     4 * LINE_HEIGHT + offset);
        g.drawString("Sign test p-values: ", 5+indentSize, 5 * LINE_HEIGHT + offset - 5);
        g.drawString("Negative = " + segInfo.getStatistic("Sign neg") + "     ",
                     15+indentSize, (int) (6 * LINE_HEIGHT + offset) - 2);
        g.drawString("Positive = " + segInfo.getStatistic("Sign pos") + "     ",
                     15+indentSize, 7 * LINE_HEIGHT + offset);
        //g.setColor(Color.WHITE);
        //g.drawLine(0, LINE_HEIGHT * 10 + offset + 2, getWidth(),
         //          LINE_HEIGHT * 10 + offset + 2);
      }
    }

  }

}
