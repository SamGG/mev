/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMClassifyViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.Expression;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.microarray.mev.cluster.gui.impl.svm.SVMResultViewer.MyListener;
import org.tigr.util.FloatMatrix;

public class SVMClassifyViewer extends SVMResultViewer {

    // calculation results
    private FloatMatrix discriminant;
    private SVMData data;
    private GeneralInfo info;
    private boolean classifyGenes;
    float [] classes;
    float [] discr;
    
    DecimalFormat floatFormat, intFormat, indexFormat;
    int indexLength = 1;        
    int lineHeight = 20;
    String [] spacerStrings;
    
    ClassifyViewerTableModel cvtm; 
        

    public SVMClassifyViewer(Experiment expt, FloatMatrix discriminant, boolean classifyGenes){
    	super(expt);
        FloatMatrix M = discriminant.transpose();
        init(M.A[1], M.A[0], classifyGenes);
    }

    public SVMClassifyViewer(Experiment e, float[] discr, float[] classes, Boolean classifyGenes){
    	super(e);
    	init(discr, classes, classifyGenes.booleanValue());
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	Object[] superExpressionArgs = super.getExpression().getArguments();
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{superExpressionArgs[0], discr, classes, new Boolean(classifyGenes)});
    }
    
    private void init(float[] discr, float[] classes, boolean classifyGenes){
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
        indexLength = String.valueOf(classes.length).length();
        indexFormat.setMinimumIntegerDigits(indexLength);
        indexFormat.setGroupingUsed(false);
    
        this.classes = classes;
        this.discr = discr;
        this.classifyGenes = classifyGenes;
        cvtm = new ClassifyViewerTableModel();
     	this.resultTable = new JTable(cvtm);
        
        setBackground(Color.white);
        this.add(new JScrollPane(resultTable), new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        MyListener listener = new MyListener();
        resultTable.addMouseListener(listener);
        resultTable.addMouseMotionListener(listener);
    }
    
    class ClassifyViewerTableModel extends AbstractTableModel{
    	String[] header = new String[]{"Index", "Class", "Discriminant", annotationLabel};
    	FloatMatrix data;
    	public ClassifyViewerTableModel(){}
    	public String getColumnName(int col){
    		return header[col];
    	}
    	public Object getValueAt(int row, int col){
    		if(col == 0){
    			return new Integer(row+1);
    		} else if (col == 1) {
        		if(classes[row] == -1.0f)
    				return "none";
        		return intFormat.format(classes[row]).toString();
            } else if (col == 2) {
    			return floatFormat.format(discr[row]).toString();
    		} else if (col == 3) {
    			try {
		    		if(classifyGenes)
						return iData.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
					else 
						return iData.getSampleName(row);
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
			return classes.length;
		}
    }

    public void setExperiment(Experiment e){
    	super.setExperiment(e);
    }
    
    public void onSelected(IFramework frm) {
    	super.onSelected(frm);
        onMenuChanged(frm.getDisplayMenu());
    }
    
    /**
     * Displays data.
     */
    protected void displayData() {
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
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        fc.setCurrentDirectory(new File("Data"));
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
        } else return;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVMFile)));
            
            
            for(int row=0; row<cvtm.getRowCount(); row++){
            	for(int col=0; col<cvtm.getColumnCount(); col++){
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
    
	//TODO adding ResultPanel methods
    public void setSize(){
            Dimension d = updateSize();
            setSize(d.width, classes.length * lineHeight + 10);
            setPreferredSize(new Dimension(d.width, classes.length * lineHeight + 5));
        }
        
        
        private boolean isLegalIndex(int i){
            return (i >=0 && i < classes.length);
        }
        
        private int getTopIndex(int top) {
            if (top < 0) {
                return 0;
            }
            return top/lineHeight;
        }
        
        private int getBottomIndex(int bottom, int limit) {
            if (bottom < 0) {
                return 0;
            }
            int result = bottom/lineHeight+1;
            return result > limit ? limit : result;
        }
        
        protected String [] getSpacerStrings(int indexLength, String clas, String discr){
            
            String [] s = new String[3];
            int s2_length;
            s[0] = s[1] = s[2] = "";
            for(int i = indexLength; i < 10 ; i++){
                s[0] += " ";
            }
            if(clas.length() == 1)
                s[0] += " ";
            s[1] = "     ";
            s2_length = 13 - discr.length();
            for(int i = 0; i < s2_length;i++){
                s[2] += " ";
            }
            return s;
        }
        
        protected Dimension updateSize(){
    //	resultTable.
    //	FontMetrics fm = this.getFontMetrics(new Font("monospaced", Font.PLAIN, 14));
            int len = 0;
       
        //len = Math.max(len, fm.stringWidth(s));
        return new Dimension(120, getHeight());
    }
    
}

