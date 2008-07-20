/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.annotation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

import edu.stanford.ejalbert.BrowserLauncher;

public class GenomeAnnoDialog extends AlgorithmDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    private JCheckBox[] annoFieldBoxes;
    private JTextField startBP;
    private JTextField endBP;
    private JComboBox chromosome;
    private Frame parent;
    private JDialog genomeAnnoDlg;
    private JButton ncbiMap;
    private JButton ucscMap;
    /**
     * Constructs a <code>RelNetInitDialog</code> with default
     * initial parameters.
     */
    public GenomeAnnoDialog(Frame parent) {
        super(parent, "Genome Annotation Field Selection", true);
        this.parent = parent;
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        
        //sample selection panel
        //sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        //parameter panel
        ParameterPanel parameters = new ParameterPanel("Genome annotation");
        parameters.setLayout(new GridLayout(0,3));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets.bottom = 5;
        
        String[] cols = MevAnnotation.getFieldNames();
        annoFieldBoxes = new JCheckBox[cols.length];
        for(int i=0; i < cols.length; i++){
        	annoFieldBoxes[i] = new JCheckBox(cols[i]);
        	annoFieldBoxes[i].setFocusPainted(false);
        	annoFieldBoxes[i].setBackground(Color.white);
        	annoFieldBoxes[i].setForeground(UIManager.getColor("Label.foreground"));
        	annoFieldBoxes[i].setActionCommand(cols[i]);
        	annoFieldBoxes[i].addActionListener(listener);
	        parameters.add(annoFieldBoxes[i], gbc);
        }
        
        ParameterPanel coordinates = new ParameterPanel();
        coordinates.setLayout(new GridLayout(0,3));
        
        String[] chrs = {"Select Chr","1","2","3","4","5","6","7","8","9","10","11","12"};
        chromosome = new JComboBox(chrs);
        coordinates.add(chromosome, gbc);
        
        startBP = new JTextField("Enter Start BP (in KB)");
        coordinates.add(startBP, gbc);
        
        endBP = new JTextField("Enter End BP (in KB)");
        coordinates.add(endBP, gbc);
        
        ParameterPanel urlsPanel = new ParameterPanel("URL Demo");
        urlsPanel.setLayout(new GridLayout(0,2));
        
        ncbiMap = new JButton("NCBI MapViewer");
        ncbiMap.setActionCommand("ncbi-map");
        ncbiMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.out.println("ncbi-map");
				try{
		        	//System.out.println("1: " + PublicURL.getURL_NCBI_Gene("MYC"));
					String taxId = "9606";
					String chr = String.valueOf(chromosome.getSelectedIndex());
			    	String st = String.valueOf(Integer.parseInt(startBP.getText()));
			    	String end = String.valueOf(Integer.parseInt(endBP.getText()));
			    	String[] params = {taxId, chr, st, end};
			    	String url = PublicURL.getURL(AnnotationURLConstants.NCBI_MAPVIEWER, params);
		        	System.out.println("1: " + url);
		        	BrowserLauncher.openURL(url);
		        	dispose();
		           }catch(Exception e){
		        	e.printStackTrace();
		        }
			}
	    });
        urlsPanel.add(ncbiMap, gbc);
        
        ucscMap = new JButton("UCSC Browser");
        ucscMap.setActionCommand("ucsc-map");
        ucscMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.out.println("ucsc-map");
				try{
		        	//System.out.println("1: " + PublicURL.getURL_NCBI_Gene("MYC"));
					String hg = "hg18";
					String chr = "chr" + String.valueOf(chromosome.getSelectedIndex());
			    	String st = String.valueOf(Integer.parseInt(startBP.getText()));
			    	String end = String.valueOf(Integer.parseInt(endBP.getText()));
			    	String[] params = {hg, chr, st, end};
			    	String url = PublicURL.getURL(AnnotationURLConstants.UCSC_BROWSER, params);
		        	System.out.println("1: " + url);
		        	BrowserLauncher.openURL(url);
		        	dispose();
		           }catch(Exception e){
		        	e.printStackTrace();
		        }
			}
	    });
        urlsPanel.add(ucscMap, gbc);
        
        contentPanel.add(coordinates, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        contentPanel.add(urlsPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        contentPanel.add(parameters, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        setActionListeners(listener);
        addContent(contentPanel);
        pack();
        setResizable(false);
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return 1;
    }
    
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
            	System.out.println("OK");
            	showGenomeAnnotationQueryResult();
            	dispose();
            } else if (command.equals("cancel-command")) {
                dispose();
            }else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(GenomeAnnoDialog.this, "Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450,500);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
        
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    }
    
    /* Raktim - Annotation Demo Only Function */
    private void showGenomeAnnotationQueryResult(){
    	Vector<String> colsSelected = new Vector<String>();
    	Vector<String[]> resultSet = null;
    	String [] colArgs;
    	int chr, st, end = -1;
    	 for(int i=0; i < annoFieldBoxes.length; i++){
         	if (annoFieldBoxes[i].isSelected()){
         	//	System.out.println(annoFieldBoxes[i].getActionCommand());
         		colsSelected.add(annoFieldBoxes[i].getText().trim());
         	}
    	 }
    	 colArgs = colsSelected.toArray(new String[1]);
    	 chr = chromosome.getSelectedIndex();
    	 st = Integer.parseInt(startBP.getText());
    	 end = Integer.parseInt(endBP.getText());
    	// System.out.println(chr + ", " + st + ", " + end);
    	 
    	 try {
    		 resultSet = MevAnnotation.getGenomeAnnotation(colArgs, chr, st, end);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("ResultSet size " + resultSet.size());
		Object[][] annoInfo;
		TableDataModel tableData = null;
	    if ((resultSet != null)&&(resultSet.size() > 0)) {
	    	annoInfo = new Object[resultSet.size()][colArgs.length];
			  for (int i = 0; i < resultSet.size(); i++) { 
				  for (int j = 0; j < colArgs.length; j++) { 
					  annoInfo[i][j] = resultSet.get(i)[j];
				  }
			    }
			  tableData = new TableDataModel(annoInfo, colArgs);
	    } else {
	    	Object[][] info;
	        info = new String[1][colArgs.length];
	        for (int i = 0; i < resultSet.size(); i++) { 
		        info[0][i] = colArgs[i];
	        }
	        tableData = new TableDataModel(info, colArgs);
	    }
		//
		genomeAnnoDlg = new JDialog(parent, "Results", false); 
		JPanel contentPanel = new JPanel();
	    contentPanel.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    
	    JPanel genelistPanel = new JPanel();
	    genelistPanel.setLayout(new BoxLayout(genelistPanel, BoxLayout.Y_AXIS));
	    JTable geneTable  = new JTable();
	    geneTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    geneTable.setModel(tableData);
	    //initCols();
	    //geneTable.setBackground(STAT_BACKGROUND);
	    JScrollPane genescroll = new JScrollPane(geneTable);
	    genelistPanel.add(genescroll);
	    contentPanel.add(genelistPanel);
	    
	    JPanel buttPanel = new JPanel();
	    buttPanel.setLayout(new BoxLayout(buttPanel, BoxLayout.Y_AXIS));
	    JButton closeWin = new JButton("Close Window");
	    closeWin.setActionCommand("close-window");
	    
	    closeWin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.out.println("HoHo");
				genomeAnnoDlg.dispose();
			}
	    });
	    
	    buttPanel.add(closeWin);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.ipady = 0;
	    c.weightx = 0.5;
	    c.weighty = 0;
	    c.anchor = GridBagConstraints.SOUTH;
	    c.gridx = 0;
	    c.gridwidth = 3;
	    c.gridy = 1;
	    contentPanel.add(buttPanel, c);
	    
	    genomeAnnoDlg.add(contentPanel);
	    genomeAnnoDlg.validate();
	    genomeAnnoDlg.pack();
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    genomeAnnoDlg.setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	    genomeAnnoDlg.setVisible(true);
		//
    }
    /* Raktim - Annotation Demo Only Class */
    private class TableDataModel extends AbstractTableModel {
    	Object[][] data;
        String[] columnNames; 
        
      TableDataModel(Object[][] dataModel, String[] cols) {
    	  data = dataModel;
    	  columnNames = cols;
      }
    	    public int getColumnCount() {
    	        return columnNames.length;
    	    }

    	    public int getRowCount() {
    	        return data.length;
    	    }

    	    public String getColumnName(int col) {
    	        return columnNames[col];
    	    }

    	    public Object getValueAt(int row, int col) {
    	        return data[row][col];
    	    }

    	    public Class getColumnClass(int c) {
    	        //return getValueAt(0, c).getClass();
    	    	return String.class;
    	    }

    	    /*
    	     * Don't need to implement this method unless your table's
    	     * editable.
    	     */
    	    public boolean isCellEditable(int row, int col) {
    	    	return false;
    	    }

    	    /*
    	     * Don't need to implement this method unless your table's
    	     * data can change.
    	     */
    	    public void setValueAt(Object value, int row, int col) {
    	        data[row][col] = value;
    	        fireTableCellUpdated(row, col);
    	    }
    	}

    public static void main(String[] args) {
    	GenomeAnnoDialog dlg = new GenomeAnnoDialog(new javax.swing.JFrame());
        if (dlg.showModal() == JOptionPane.OK_OPTION) {
            System.out.println("ok");
        }
        System.exit(0);
    }
}
