/*
 * COAInertiaValsViewer.java
 *
 * Created on December 14, 2004, 4:03 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class COAInertiaValsViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202022020001L;
    
    private JComponent header;
    private JTextArea  content; 
    private double[] inertiaVals, cumulInertiaVals;
    
    private JPopupMenu popup;
    
    //private SAMState localSAMState;

    /** Creates new SAMDeltaInfoViewer */
    public COAInertiaValsViewer(double[] inertiaVals, double[] cumulInertiaVals) {
        this.inertiaVals = inertiaVals;
        this.cumulInertiaVals = cumulInertiaVals;    
           
	header  = createHeader();
	content = createContent();
	setMaxWidth(content, header);   

    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        //oos.writeObject(this.deltaGrid);
        //oos.writeObject(this.medNumFalse);
        //System.out.println("In writeObject()");
        //System.out.println("SAMState.firstRun = " + SAMState.firstRun);
        //System.out.println("SAMState.groupAssignments.length = " + SAMState.groupAssignments.length);  

        //oos.writeObject(new SAMState());
        oos.defaultWriteObject();
        //oos.writeObject();
        
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        //ois.readObject();
        ois.defaultReadObject();
        //System.out.println("In readObject()");
        //System.out.println("SAMState.firstRun = " + SAMState.firstRun);
        //System.out.println("SAMState.groupAssignments.length = " + SAMState.groupAssignments.length);        
        header = createHeader();
        content = createContent();
        setMaxWidth(content, header);
    }
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
	return content;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
	return header;
    }
    
    /**
     * Creates the viewer header.
     */
    /*
    private JComponent createHeader() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBackground(Color.white);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = new Insets(10, 0, 10, 0);
	panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Delta Table</b></font></body></html>"), gbc);
        //panel.add(new JLabel("Delta\tMedian false\t90th %ile false\t  # sig. genes\tFDR(%) Median\t   FDR(%) 90th %ile\n\n"), gbc);
	return panel;
    } 
     */
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
	
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }    
    
    private JComponent createHeader() {
	JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
	panel.setBackground(Color.white);
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.insets = new Insets(10, 200, 10, 200);
	JLabel label1 = new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Inertia values</b></font></body></html>");
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        //constraints.fill = GridBagConstraints.BOTH;
        //constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(label1, constraints);
        panel.add(label1);
        
        JTextArea area = new JTextArea();
        area.setEditable(false);
        StringBuffer sb = new StringBuffer();
        sb.append("  Axis\tInertia (%)\tCumulative Inertia (%)");
        area.setForeground(Color.blue);
        area.setText(sb.toString());
	area.setCaretPosition(0);
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.insets = new Insets(10, 0, 0, 0);
        //constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(area, constraints);
        panel.add(area);        
        
	return panel;
    }    
    
    private JTextArea createContent() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        StringBuffer sb = new StringBuffer();
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);        
        //sb.append("Delta\tMedian false\t90th %ile false\t  # sig. genes\tFDR(%) Median\t   FDR(%) 90th %ile\n\n");
        //counter = 0;
        for (int counter = 0; counter < inertiaVals.length; counter++) {            
            sb.append("   " + (counter + 1)+ "\t" + printFormat(inertiaVals[counter]) + "\t" + printFormat(cumulInertiaVals[counter]) + "\n");
            //counter += 100;
        }
        area.setText(sb.toString());
	area.setCaretPosition(0);

        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("Data"));   
        fc.setDialogTitle("Save Inertia values");
        
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Save inertia values", GUIFactory.getIcon("save16.gif"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(COAInertiaValsViewer.this.getHeaderComponent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            out.print("Axis\tInertia (%)\tCumulative Inertia (%)\n\n");
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < inertiaVals.length; i++) {
                                //out.print(groupAssgn[i]);
                                out.print((i + 1) + "\t" + inertiaVals[i] + "\t" + cumulInertiaVals[i] + "\n");
                            }
                            out.println();
                            out.flush();
                            out.close();
                       } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }                
            }
        });
        
        popup.add(menuItem);
        
        area.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(),
                    e.getX(), e.getY());
                }
            }            
            /*
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    //System.out.println("Right clicked");
                    fc.setDialogTitle("Save delta table");
                    int returnVal = fc.showSaveDialog(SAMDeltaInfoViewer.this.getHeaderComponent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            out.print("Delta\tMedian false\t90th %ile false\t# sig. genes\tFDR(%) Median\tFDR(%) 90th %ile");
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < deltaGrid.length; i++) {
                                //out.print(groupAssgn[i]);
                                out.print(deltaGrid[i] + "\t" + medNumFalse[i] + "\t" + false90th[i] + "\t" + numSig[i] + "\t" + FDRMedian[i] + "\t" + FDR90th[i] + "\n");
                            }
                            out.println();
                            out.flush();
                            out.close();
                       } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }                     
                    
                }
            }
             */
        });
        
	return area;        
    }

    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
	int c_width = content.getPreferredSize().width;
	int h_width = header.getPreferredSize().width;
	if (c_width > h_width) {
	    header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
	} else {
	    content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
	}
    } 
    
    private String printFormat(double d) {
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2); 
        if (Double.isNaN(d)) {
            return "N/A";
        } else {
            return nf.format(d);
        }
    }
    
}

    

