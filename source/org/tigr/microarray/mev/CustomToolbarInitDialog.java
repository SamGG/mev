package org.tigr.microarray.mev;
/*
All rights reserved.
 */
/*
 * CustomToolbarInitDialog.java
 *
 * 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.script.util.ScriptConstants;


public class CustomToolbarInitDialog extends AlgorithmDialog {
    
    /** result
     */    
    private int result;    
    /** algorithm index
     */    
    private int algorithmIndex = -1;    
   
    private String currentAlgorithmName;    
    /** main panel for components
     */    
    private JPanel mainPanel;
    /** action manager to accumulate available mev algorithms for presentation
     */    
    private ActionManager actionManager;    
    /** analysis algorithm selection controls
     */    
    private AlgorithmSelectionPanel algSelPanel;
    
    private JScrollPane sPane;
    /** Creates a new instance of CustomToolbarInitDialog
     * @param manager ActionManager to provide available algorithm
     */
    public CustomToolbarInitDialog(ActionManager manager) {
        super(new JFrame(), "Customized MenuToolbar Initialization Dialog", true);
        this.actionManager = manager;
        result = JOptionPane.CANCEL_OPTION;
        
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        this.setResetButtonText("Select All");
        //previewPanel = new PreviewPanel();
        algSelPanel = new AlgorithmSelectionPanel(actionManager);
        sPane=new JScrollPane(algSelPanel);
        //mainPanel.add(previewPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        mainPanel.add(sPane, new GridBagConstraints(0,0,1,2,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        Listener listener = new Listener();
        addWindowListener(listener);
        addContent(mainPanel);
        setActionListeners(listener);
        pack();
        /*
        if(!isAffyData)
            setSize(new Dimension(510, 587));
        else
            setSize(new Dimension(510, 665));
            */
        setSize(new Dimension(520, 665));
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    /** Resets the controls
     */    
    public void onReset() {
       TMEV.initCustomerAnalysis(getAlgorithmCount(this.actionManager));
    	
    }
    
    /** returns algorithm name
     */    
    public String getAlgorithmName() {
        return currentAlgorithmName;
    }
    
    /** returns the algorthms index
     */    
    public int getAlgorithmIndex() {
        int index;
        if(currentAlgorithmName != null) {
            String number = (String)(algSelPanel.indexHash.get(currentAlgorithmName));
            if(number != null) {
                return Integer.parseInt(number);
            }
        }
        return -1;
    }
    public int getAlgorithmCount(ActionManager actionManager) {
        int total=0;
        Action action;
        while ((action = actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(total)))!=null){
        	total++;
        }
        return total;
    }
    public int getSelAlgorithmCount() {
        int count=0;
        for(int i=0;i<TMEV.getCustomerAnalysisList().length();i++){
        	if(TMEV.getCustomerAnalysis()[i]==1)
        		count++;
        }
        return count;
    }
    
    private class AlgorithmSelectionPanel extends ParameterPanel {
    	Hashtable descriptions;
        //Hashtable locations;
        Hashtable indexHash;
        JPanel checkboxPanel;
        int totalCount;
        JCheckBox cbox;
        Icon icon;
        AlgorithmSelectionPanel(ActionManager actionManager) {
            super("Algorithm Selection");
            setLayout(new GridBagLayout());
            setBackground(Color.white);
            checkboxPanel=new JPanel();
            GridBagLayout gridbag=new GridBagLayout();
            checkboxPanel.setLayout(gridbag);
        	Action action;
        	int algCnt = 0,i=0,j=0;
        	CheckboxListener cboxListener;
        	GridBagConstraints gbc = new GridBagConstraints();
        	while ((action = actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(totalCount)))!=null){
        		totalCount++;
        	}
        	while ((action = actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(algCnt)))!=null){
        		boolean select=false;
                if(action != null) {
                	  int m=0,count=TMEV.getCustomerAnalysis().length;
                	  if(count!=totalCount){
                		  TMEV.initCustomerAnalysis(totalCount);
                	  }
                	  int mytag[]=new int[count];
                	  for(m=0;m<count;m++)
                		  mytag[m]=TMEV.getCustomerAnalysis()[m];
                      if(mytag[algCnt]==1)
                			 select =true;
                	  
                  cbox=new JCheckBox();
                  cbox.setIcon((Icon)action.getValue(ActionManager.LARGE_ICON));
                  cbox.setBorderPainted(true);
                  cbox.setSelected(select);
                  cboxListener=new CheckboxListener(totalCount,algCnt); 
                  cbox.addItemListener(cboxListener);         		
                  buildConstraints(gbc,j,i,1,1,25,20);
                  gridbag.setConstraints(cbox,gbc);
                  checkboxPanel.add(cbox);
              	  algCnt++;
                  if(j<3)
                	  j++;
                  else{
                	  j=0;
                	  i++;
                	  }
                }
        }
        	add(checkboxPanel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            //descriptions = new Hashtable();
            //indexHash = new Hashtable();
            // locations = new Hashtable();
           
        }
        void buildConstraints(GridBagConstraints gbc, int gx, int gy,
        	    int gw, int gh, int wx, int wy) {
        	        
        	        gbc.gridx = gx;
        	        gbc.gridy = gy;
        	        gbc.gridwidth = gw;
        	        gbc.gridheight = gh;
        	        gbc.weightx = wx;
        	        gbc.weighty = wy;
        	    }
        	    
    }
    private class CheckboxListener implements ItemListener{
    	//JCheckBox cbox;
    	int tag;
    	int count;
    	CheckboxListener(int i,int j){
    		count=i;
    		tag=j;
    	}
    	public void itemStateChanged(ItemEvent e){
    		if(e.getStateChange()==ItemEvent.SELECTED){
    			TMEV.setCustomerAnalysis(count,tag,1);
    		}
    		if(e.getStateChange()==ItemEvent.DESELECTED){
    			TMEV.setCustomerAnalysis(count,tag,0);
    		}
    	}
    }
 
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == okButton) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (source == cancelButton) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (source == resetButton) {
                onReset();
            }
            else if (source == infoButton){
                HelpWindow hw = new HelpWindow(CustomToolbarInitDialog.this, "Script Algorithm Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
}
