/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMTrainViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.svm.SVMResultViewer.MyListener;


public class SVMTrainViewer extends SVMResultViewer {

    private float[] weights;
    private Experiment experiment;
    private SVMData data;
    private GeneralInfo info;
    private boolean classifyGenes;
    TrainViewerTableModel cvtm;
    DecimalFormat floatFormat;
    
    public SVMTrainViewer(Experiment expt, float[] weights, boolean classifyGenes, SVMData data){
    	super(expt);
    	init(weights, classifyGenes, data);
    }   
    public SVMTrainViewer(Experiment experiment, float[] weights, Boolean classifyGenes, SVMData data){
    	this(experiment, weights, classifyGenes.booleanValue(), data);
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{super.getExpression().getArguments()[0], weights, new Boolean(classifyGenes), data});
    }
    private void init(float[] weights, boolean classifyGenes, SVMData data){
        this.weights = weights;
        this.classifyGenes = classifyGenes;
    	this.data = data;
    
        floatFormat = new DecimalFormat();
        floatFormat.setMaximumFractionDigits(4);
        floatFormat.setMinimumFractionDigits(4);
        floatFormat.setGroupingUsed(false);
        
        setBackground(Color.white);
        cvtm = new TrainViewerTableModel();
     	this.resultTable = new JTable(cvtm);
        
        this.add(new JScrollPane(resultTable), new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        MyListener listener = new MyListener();
        resultTable.addMouseListener(listener);
        resultTable.addMouseMotionListener(listener);
    }
    
    class TrainViewerTableModel extends AbstractTableModel{
        String[] header = new String[]{"Index", "Weight", annotationLabel};
    	public TrainViewerTableModel(){}
    	public String getColumnName(int col){
    		return header[col];
    	}
    	public Object getValueAt(int row, int col){
    		if(col == 0){
    			return new Integer(row+1);
    		} else if (col == 1) {
    			return floatFormat.format(weights[row]).toString();
    		} else if (col == 2) {
    			try{
		    		if(classifyGenes){
		    			new Integer(labelIndex);
		    			getMultipleArrayDataRow(row);
		    			iData.toString();
						return iData.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
		    		} else {
						return iData.getFullSampleName(row);
		    		}
    			} catch (NullPointerException npe){
        			npe.printStackTrace();
    				return "";
    			}
    		}
    		return new String("");
    	}  
    	public boolean isCellEditable(int row, int col) { return false; }

		public int getColumnCount() {
			return header.length;
		}
		
		public int getRowCount() {
			return weights.length;
		}
    }
    
    public void setExperiment(Experiment e){
    	super.setExperiment(e);
    	this.experiment = e;
    }
    
    public void onSelected(IFramework frm) {
    	super.onSelected(frm);
        onMenuChanged(frm.getDisplayMenu());
    }
    
    
    /**
     * Displays train result.
     */
    protected void displayData() {
        if (weights == null) {
            return;
        }
        displayResult(this.classifyGenes);
    }
    
    
    private void displayResult(boolean genes){
    }
    
    /**
     * Saves train result to a file.
     */
    protected void onSaveResult() {
        File SVMFile;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new SVMFileFilter());
        fc.setFileView(new SVMFileView());
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
        } else return;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVMFile)));
            if(data.classificationFile != null)
                out.write("Classification file: "+data.classificationFile.getPath()+"\n");
                else
            	out.write("Classification file: None (SVM classification editor was used)\n");
            out.write("Constant : "+Float.toString(data.constant)+"\n");
            out.write("Coefficient : "+Float.toString(data.coefficient)+"\n");
            out.write("Power : "+Float.toString(data.power)+"\n");
            out.write("Diagonal factor : "+Float.toString(data.diagonalFactor)+"\n");
            out.write("Convergence threshold : "+Float.toString(data.convergenceThreshold)+"\n");
            //buffer.append("Normalize : "+data.normalize+"\n");
            out.write("Radial : "+data.radial+"\n");
            out.write("Width factor : "+Float.toString(data.widthFactor)+"\n");
            out.write("Use Constraint : "+data.constrainWeights+"\n");
            out.write("Positive Constraint : "+Float.toString(data.positiveConstraint)+"\n");
            out.write("Negative Constraint : "+Float.toString(data.negativeConstraint)+"\n");
            out.write("Seed : "+Float.toString(data.seed)+"\n");
            //out.write("Calculation time : " + Float.toString( info.time )+" ms\n");
            out.write("Objective : "+Float.toString(data.objective1)+"\n\n");
            
            out.write("Weights\tGB#\n");

            for(int row=0; row<cvtm.getRowCount(); row++){
            	for(int col=1; col<cvtm.getColumnCount(); col++){
            		out.write(cvtm.getValueAt(row, col).toString() + '\t');
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
    
    /*
    protected Dimension updateSize(){
        return ((TrainResultPanel)resultPanel).updateSize();
    }*/
    
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
    
        
        
        protected Dimension updateSize(){
            FontMetrics fm = this.getFontMetrics(new Font("monospaced", Font.PLAIN, 14));
            int len = 0;
            
            
            return new Dimension(len+10, getHeight());
        }
     
}

