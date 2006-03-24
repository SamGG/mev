/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SVMOneOutViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:53 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.Expression;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.microarray.mev.cluster.gui.impl.svm.SVMResultViewer.MyListener;
import org.tigr.util.FloatMatrix;

public class SVMOneOutViewer extends SVMResultViewer {
    
    private FloatMatrix discriminant;

    private boolean classifyGenes;
    float [] classes;
    float [] discr;
    int [] initClasses;
    float [] classMatch;
    
    int [] elementScores;
    int [] iterationScores;
    int nonNeuts;
    
    DecimalFormat floatFormat, intFormat, indexFormat;
    
    OneOutViewerTableModel ovtm;
    
    /**
     * Create a new SVMOneOutViewer to display provided data in discriminant
     */
    public SVMOneOutViewer(Experiment experiment, FloatMatrix discriminant, boolean classifyGenes, int [] initClasses, int [] elementScores, int [] iterationScores, int nonNeuts){
    	 super(experiment);
        this.discriminant = discriminant;
         FloatMatrix M = discriminant.transpose();
         init(M.A[0], M.A[1], classifyGenes, initClasses, elementScores, iterationScores, nonNeuts);
    }
    
    public SVMOneOutViewer(Integer exptID, float[] classes, float[] discr, Boolean classifyGenes, int [] initClasses, int [] elementScores, int [] iterationScores, Integer nonNeuts){
   	 super(exptID.intValue());
        init(classes, discr, classifyGenes.booleanValue(), initClasses, elementScores, iterationScores, nonNeuts.intValue());
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{super.getExpression().getArguments()[0], classes, discr, new Boolean(classifyGenes), initClasses, elementScores, iterationScores, new Integer(nonNeuts)});
    }
    private void init(float[] classes, float[] discr, boolean classifyGenes, int[] initClasses, int[] elementScores, int[] iterationScores, int nonNeuts){
    	this.classes = classes;
    	this.discr = discr;
    	
        this.classifyGenes = classifyGenes;
        this.initClasses = initClasses;
        this.elementScores = elementScores;
        this.iterationScores = iterationScores;
        this.nonNeuts = nonNeuts;
        
        floatFormat = new DecimalFormat();
        floatFormat.setMaximumFractionDigits(4);
        floatFormat.setMinimumFractionDigits(4);
        floatFormat.setGroupingUsed(false);
    
        intFormat = new DecimalFormat();
        intFormat.setMinimumFractionDigits(0);
        intFormat.setMaximumFractionDigits(0);
        intFormat.setGroupingUsed(false);
        
        indexFormat = new DecimalFormat();
        indexFormat = new DecimalFormat();
        indexFormat.setMinimumFractionDigits(0);
        indexFormat.setMaximumFractionDigits(0);
        
        ovtm = new OneOutViewerTableModel();
        
     	this.resultTable = new JTable(ovtm);
     	this.add(new JScrollPane(resultTable), new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        MyListener listener = new MyListener();
        resultTable.addMouseListener(listener);
        resultTable.addMouseMotionListener(listener);
    }
    
    class OneOutViewerTableModel extends AbstractTableModel{
    	String[] header = new String[]{"Index", "Init", "Class", "Discriminant", "Element Score", "%", "Iteration Score", "%", annotationLabel};
        public OneOutViewerTableModel(){
    		
    	}
    	public String getColumnName(int col){
    		return header[col];
    	}
    	public Object getValueAt(int row, int col){
    		if(col == 0){
    			return indexFormat.format(row+1);
    		} else if (col == 1) {
        		if(initClasses[row] == -1.0f)
    				return "none";
        		return intFormat.format(initClasses[row]);
            } else if (col == 2) {
        		if(classes[row] == -1.0f)
    				return "none";
    			return intFormat.format(classes[row]).toString();
    		} else if (col == 3) {
    			return floatFormat.format(discr[row]);
    		} else if (col == 4) {
    			return (intFormat.format(elementScores[row]) + "/" + intFormat.format(elementScores.length)).toString();
    		} else if (col == 5) {
    			return intFormat.format((elementScores[row] / elementScores.length) * 100);
    		} else if (col == 6) {
    			return (intFormat.format(iterationScores[row]) + "/" + intFormat.format(iterationScores.length)).toString();
    		} else if (col == 7) {
    			return intFormat.format((iterationScores[row] / iterationScores.length) * 100);
        	} else if (col == 8) {
        		try {
	    		if(classifyGenes)
					return iData.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
				else 
					return iData.getSampleName(row);
        		} catch (NullPointerException npe){
        			npe.printStackTrace();
        			return  "";
    }    
	    	}
    		return new String("");
    	}  
    	public boolean isCellEditable(int row, int col) { return false; }

		public int getColumnCount() {
			return header.length;
		}
    
		public int getRowCount() {
			return classes.length;
		}
    }
    public void onSelected(IFramework frm) {
    	super.onSelected(frm);
        this.framework = frm;
        onMenuChanged(frm.getDisplayMenu());
    }    
    
    /**
     * Displays data.
     */
    protected void displayData() {
        displayResult(this.classifyGenes);
        //  this.resultPanel.repaint();
    }
    
    private void displayResult(boolean genes){

    }
    
    /**
     * Saves train result to a file.
     */
    protected void onSaveResult() {
        File SVMFile;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
        } else return;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVMFile)));
            for(int row=0; row<ovtm.getRowCount(); row++){
            	for(int col=0; col<ovtm.getColumnCount(); col++){
            		out.write(ovtm.getValueAt(row, col).toString() + '\t');
            }
            	out.write('\n');
            }
            out.flush();
            out.close();
            out = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog( this, "Error writing to file "+SVMFile.getPath()+"!","Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    protected Dimension updateSize(){
        return new Dimension(120, getHeight());
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }    
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }    
    
    	/**
	 * @return Returns the classifyGenes.
	 */
	public boolean isClassifyGenes() {
		return classifyGenes;
            }
	/**
	 * @return Returns the discr.
	 */
	public float[] getDiscr() {
		return discr;
            }
	/**
	 * @return Returns the nonNeuts.
	 */
	public int getNonNeuts() {
		return nonNeuts;
        }
	/**
	 * @return Returns the elementScores.
	 */
	public int[] getElementScores() {
		return elementScores;
            }
	/**
	 * @return Returns the iterationScores.
	 */
	public int[] getIterationScores() {
		return iterationScores;
        }
	/**
	 * @return Returns the initClasses.
	 */
	public int[] getInitClasses() {
		return initClasses;
    }
    
    public float[] getClasses(){return classes;}
}

