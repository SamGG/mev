/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMClassifyViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-03-10 20:21:56 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
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

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.util.FloatMatrix;

public class SVMClassifyViewer extends SVMResultViewer {
    public static final long serialVersionUID = 202018020001L;

    // calculation results
    private IData  experiment;
    private FloatMatrix discriminant;
    private SVMData data;
    private GeneralInfo info;
    private boolean classifyGenes;
    float [] classes;
    float [] discr;
    
    /**
     * Constructs a <code>SVMClassifyViewer</code> with specified data.
     */
    public SVMClassifyViewer(IFramework framework, IData experiment, SVMData data, FloatMatrix discriminant, GeneralInfo info, boolean classifyGenes) {
        super(framework);
        this.experiment   = experiment;
        this.discriminant = discriminant;
        this.data = data;
        this.info = info;
        this.classifyGenes = classifyGenes;
        
        FloatMatrix M = discriminant.transpose();
        classes = M.A[0];
        discr = M.A[1];      
        resultPanel = new ClassifyResultPanel();
      //  MyListener listener = new MyListener();
     //   resultPanel.addMouseListener(listener);
     //   resultPanel.addMouseMotionListener(listener);
        displayData();
        this.add(resultPanel,new GridBagConstraints(0,1,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    }
    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        
        this.discr = (float [])ois.readObject();
        this.classes = (float [])ois.readObject();
        this.discriminant = (FloatMatrix)ois.readObject();
        
        this.classifyGenes = ois.readBoolean();
        this.data = (SVMData)ois.readObject();
        this.info = (GeneralInfo)ois.readObject();
        
        MyListener listener = new MyListener();
        getContentComponent().addMouseListener(listener);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { 
        oos.writeObject(this.discr);
        oos.writeObject(this.classes);
        oos.writeObject(this.discriminant);
        
        oos.writeBoolean(this.classifyGenes);
        oos.writeObject(this.data);
        oos.writeObject(this.info);
    }
    
    public void onSelected(IFramework frm) {
        this.framework = frm;
        this.experiment = frm.getData();
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
        String Dummy;
        StringBuffer buffer = new StringBuffer();
        DecimalFormat format = new DecimalFormat();
        DecimalFormat format2 = new DecimalFormat();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(4);
        format.setGroupingUsed(false);
        buffer.append("Constant : "+Float.toString(data.constant)+"\n");
        buffer.append("Coefficient : "+Float.toString(data.coefficient)+"\n");
        buffer.append("Power : "+Float.toString(data.power)+"\n");
        buffer.append("Diagonal factor : "+Float.toString(data.diagonalFactor)+"\n");
        buffer.append("Convergence threshold : "+Float.toString(data.convergenceThreshold)+"\n");
        //buffer.append("Normalize : "+data.normalize+"\n");
        buffer.append("Radial : "+data.radial+"\n");
        buffer.append("Width factor : "+Float.toString(data.widthFactor)+"\n");
        buffer.append("Use Constraint : "+data.constrainWeights+"\n");
        buffer.append("Positive Constraint : "+Float.toString(data.positiveConstraint)+"\n");
        buffer.append("Negative Constraint : "+Float.toString(data.negativeConstraint)+"\n");
        buffer.append("Seed : "+Float.toString(data.seed)+"\n");
        buffer.append("Calculation time : "+Float.toString(info.time)+" ms\n");
        buffer.append("Objective : "+Float.toString(data.objective1)+"\n\n");
        if(genes){
            if(labelIndex >= 0 && labelIndex < fieldNames.length)
                buffer.append(" Index    Class.   Discr.    "+fieldNames[labelIndex]);
            else
                buffer.append(" Index    Class.   Discr.");
        }
        else
            buffer.append(" Index    Class.   Discr.     Experiment");
        Log.setText( buffer.toString());
        Log.setCaretPosition(0);
   //     Log.setSize(resultPanel.getWidth()*2, 15*Log.getFontMetrics(Log.getFont()).getHeight());
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
            int discrLength = discriminant.getRowDimension();
            String logText = Log.getText();
            logText = logText.substring(0, logText.lastIndexOf("Index"));
            out.write( logText  );
            if(classifyGenes){
                if(labelIndex >= 0 && labelIndex < fieldNames.length)
                    out.write("Index\tClass.\tDiscr.\t"+fieldNames[labelIndex]+"\t\n");
                else
                    out.write("Index\tClass.\tDiscr.\t\n");
            }
            else
                out.write("Index\tClass.\tDiscr.\tExperiment\t\n");
            for(int i = 0; i < discrLength; i++){
                if(classifyGenes)
                    out.write(String.valueOf(i+1)+"\t"+String.valueOf(discriminant.get(i,0))+"\t"+String.valueOf(discriminant.get(i,1))+
                    "\t"+experiment.getElementAttribute(getMultipleArrayDataRow(i), labelIndex)+ "\t\n");
                else
                    out.write(String.valueOf(i+1)+"\t"+String.valueOf(discriminant.get(i,0))+"\t"+String.valueOf(discriminant.get(i,1))+
                    "\t"+experiment.getSampleName(i) + "\t\n");
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
        return ((ClassifyResultPanel)resultPanel).updateSize();
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
    
    
    public class ClassifyResultPanel extends JPanel implements java.io.Serializable {
        int lineHeight = 20;
        
        int indexLength = 1;
        DecimalFormat floatFormat;
        DecimalFormat intFormat;
        DecimalFormat indexFormat;
        String [] spacerStrings;
        
        public ClassifyResultPanel(){
            
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
            
            setBackground(Color.white);
            Dimension d = updateSize();
            setSize(d.width, classes.length * lineHeight + 10);
            setPreferredSize(new Dimension(d.width, classes.length * lineHeight + 5));
        }
        
        public void paint(Graphics g){
            super.paint(g);
            
            g.setFont(new Font("monospaced", Font.PLAIN, 14));
            FontMetrics fm =g.getFontMetrics();
            //int lineHeight = fm.getHeight() + vertSpace;
            Rectangle rect = g.getClipBounds();
            String [] spacerStrings;
            int index;
            
            int top = getTopIndex(rect.y);
            int bottom = getBottomIndex(rect.y+rect.height, classes.length + 1);
            
            for(int i = top; i < bottom; i++){
                index = i;
                if(!isLegalIndex(index))
                    continue;
                spacerStrings = getSpacerStrings( indexLength, intFormat.format(classes[index]), floatFormat.format(discr[index]));
                if(classifyGenes){
                    g.drawString(" "+indexFormat.format(i+1)+spacerStrings[0]+intFormat.format(classes[index])
                    + spacerStrings[1]+ floatFormat.format(discr[index]) +spacerStrings[2] + experiment.getElementAttribute(getMultipleArrayDataRow(index), labelIndex), 10, (i+1)*lineHeight);  //map i to real data using exp
                }
                else{
                    g.drawString(" "+indexFormat.format(i+1)+spacerStrings[0]+intFormat.format(classes[index])
                    + spacerStrings[1]+ floatFormat.format(discr[index]) +spacerStrings[2] + experiment.getSampleName(index), 10, (i+1)*lineHeight);  //map i to real data using exp
                }
            }
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
            FontMetrics fm = this.getFontMetrics(new Font("monospaced", Font.PLAIN, 14));
            int len = 0;
            int numElem;
            indexLength = String.valueOf(classes.length).length();
            String s;
            // String [] spacerStrings;
            
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
            indexFormat.setMinimumIntegerDigits(indexLength);
            indexFormat.setGroupingUsed(false);
                        
            if(classifyGenes)
                numElem = experiment.getFeaturesSize();
            else
                numElem = experiment.getFeaturesCount();
            
            for(int i = 0; i < numElem; i++){
                spacerStrings = getSpacerStrings( indexLength, intFormat.format(classes[i]), floatFormat.format(discr[i]));
                if(classifyGenes){
                    s = " "+indexFormat.format(i+1)+spacerStrings[0]+intFormat.format(classes[i])
                    + spacerStrings[1]+ floatFormat.format(discr[i]) +spacerStrings[2] + experiment.getElementAttribute(getMultipleArrayDataRow(i), labelIndex);  //map i to real data using exp
                }
                else{
                    s = " "+indexFormat.format(i)+spacerStrings[0]+intFormat.format(classes[i])
                    + spacerStrings[1]+ floatFormat.format(discr[i]) +spacerStrings[2] + experiment.getSampleName(i);  //map i to real data using exp
                }
                len = Math.max(len, fm.stringWidth(s));
            }
            setSize(len + 10, getHeight());
            setPreferredSize(new Dimension(len+10, getHeight()));
            
            return new Dimension(len+10, getHeight());
        }
       
    }
    
}

