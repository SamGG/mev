/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/* x
 * $RCSfile: MultipleArrayToolbar.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-04-14 21:08:51 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.BorderFactory; 

import org.tigr.microarray.mev.action.ActionManager;

public class MultipleArrayToolbar extends JToolBar {
    
	private static final long serialVersionUID = 1L;
	boolean tempDeletion = true;
    /**
     * Construct a <code>MultipleArrayToolbar</code> using
     * specified action manager.
     * @see ActionManager
     */
    public MultipleArrayToolbar(ActionManager manager) { 
    	addAlgorithmActions(manager);
    }
    
    Action actiontester;
    int totalModules;
    ActionManager manager;
    SteppedComboBox[] steppedComboArray;
    SteppedComboBox cghSteppedComboBox;
    ImageIcon comboIcon;
    String[] category = {"Clustering","Statistics","Classification","Data Reduction","Meta Analysis","Visualization","Miscellaneous"};

	ImageIcon[] categoryIcon = new ImageIcon[category.length];
	ImageIcon[] disabledCategoryIcon = new ImageIcon[category.length];

    
    private void addAlgorithmActions(ActionManager manager) {
    	this.manager = manager;
    	totalModules = 0;
    	Action action;
    	steppedComboArray = new SteppedComboBox[category.length];
    	int index = 0;
//    	categoryIcon[index]=manager.getIcon("RNAseq.gif");
//        disabledCategoryIcon[index++]=manager.getIcon("RNAseq_gry.gif");
    	categoryIcon[0]=manager.getIcon("Clustering_1.gif");
        disabledCategoryIcon[0]=manager.getIcon("Clustering_1gry.gif");
        categoryIcon[1]=manager.getIcon("Statistics_1.gif");
        disabledCategoryIcon[1]=manager.getIcon("Statistics_1gry.gif");
        categoryIcon[2]=manager.getIcon("Classification_1.gif");
        disabledCategoryIcon[2]=manager.getIcon("Classification_1gry.gif");
        categoryIcon[3]=manager.getIcon("Data_Reduction_1.gif");
        disabledCategoryIcon[3]=manager.getIcon("Data_Reduction_1gry.gif");
        categoryIcon[4]=manager.getIcon("Meta_Analysis_1.gif");
        disabledCategoryIcon[4]=manager.getIcon("Meta_Analysis_1gry.gif");
        categoryIcon[5]=manager.getIcon("Visualization_1.gif");
        disabledCategoryIcon[5]=manager.getIcon("Visualization_1gry.gif");
        categoryIcon[6]=manager.getIcon("Miscellaneous_1.gif");
        disabledCategoryIcon[6]=manager.getIcon("Miscellaneous_1gry.gif");
    	for (int i=0; i<category.length; i++){
    		steppedComboArray[i] = new SteppedComboBox();
    		ComboListener comboListener = new ComboListener();
    		steppedComboArray[i].addActionListener(comboListener);
    		steppedComboArray[i].setMaximumRowCount(20);	
    		steppedComboArray[i].addItem(disabledCategoryIcon[i]);
    	}
    	index = 0;
    	for(int i=0;i<category.length;i++) {
   			 while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)))!=null) {
   				if((action.getValue(ActionManager.CATEGORY)).equals(category[i])) {
   					//add(action);   // commented to clear way for comboBoxes
   					steppedComboArray[i].addItem((ImageIcon)action.getValue(ActionManager.LARGE_ICON));
   					steppedComboArray[i].setActionCommand(ActionManager.ANALYSIS_COMMAND);
   		    		steppedComboArray[i].putClientProperty("categoryIconExists", true);
   		    		steppedComboArray[i].putClientProperty("categoryIcon",categoryIcon[i]);
   		    		totalModules++;
   				}
   				index++;
   			 }
   			ComboBoxRenderer renderer = new ComboBoxRenderer();
   		    steppedComboArray[i].setSelectedIndex(0);
   		    steppedComboArray[i].setRenderer(renderer);
   		    int preferredWidth = steppedComboArray[i].getPreferredSize().width;
   			steppedComboArray[i].setPreferredSize(new Dimension(50, steppedComboArray[i].getPreferredSize().height));
   			steppedComboArray[i].setMinimumSize(new Dimension(0,0));
   			steppedComboArray[i].setPopupWidth(preferredWidth);
   			
   			add(steppedComboArray[i]);
   			this.addSeparator();
   			index=0;
    		}
    	}
 
    public void addCGHComboBox(){
    	cghSteppedComboBox = new SteppedComboBox();
    	ComboListener comboListener = new ComboListener();
    	cghSteppedComboBox.addActionListener(comboListener);
    	
    	cghSteppedComboBox.setMaximumRowCount(100);
        
    	cghSteppedComboBox.addItem("CGH Analysis");int index = 0;
        while ((manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index)))!=null) {
            cghSteppedComboBox.addItem((String)manager.getActionNames().get(ActionManager.CGH_NAME_OF_ANALYSIS+String.valueOf(index)));
            index++;
        }
    	
    	ComboBoxRenderer renderer = new ComboBoxRenderer();
    	cghSteppedComboBox.setSelectedIndex(0);
    	cghSteppedComboBox.setRenderer(renderer);
		int preferredWidth = cghSteppedComboBox.getPreferredSize().width;
		cghSteppedComboBox.setPreferredSize(new Dimension(50, cghSteppedComboBox.getPreferredSize().height));
		cghSteppedComboBox.setMinimumSize(new Dimension(0,0));
		cghSteppedComboBox.setPopupWidth(preferredWidth);

    	//add(cghSteppedComboBox); //commented to remove "CGH Analysis" from toolbar
    }
    
    /**
     * Overriden from JToolBar.
     */
    public JButton add(Action a) {
	JButton button = super.add(a);
	button.setActionCommand((String)a.getValue(Action.ACTION_COMMAND_KEY));
	button.setIcon((Icon)a.getValue(ActionManager.LARGE_ICON));
    button.setFocusPainted(false);
	return button;
    }
    
    /**
     * Returns an array of buttons with the same action command.
     */
    private AbstractButton[] getButtons(String command) {
	ArrayList list = new ArrayList();
	Component[] components = getComponents();
	for (int i = 0; i < components.length; i++) {
	    if (components[i] instanceof AbstractButton) {
		    	if (((AbstractButton)components[i]).getActionCommand().equals(command)){
				    list.add(components[i]);
			    }
			}
		}
	return (AbstractButton[])list.toArray(new AbstractButton[list.size()]);
    }
    
    /**
     * Sets state of buttons with specified action command.
     */
    private void setEnable(String command, boolean enable) {
	AbstractButton[] buttons = getButtons(command);
	if (buttons == null || buttons.length < 1) {
	    return;
	}
	for (int i=0; i<buttons.length; i++) {
	    buttons[i].setEnabled(enable);            
	}
    }
    
    /**
     * Disables some buttons according to specified state.
     */
    public void systemDisable(int state) {
	switch (state) {
	    case TMEV.SYSTEM:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		setEnable(ActionManager.LOAD_FILE_COMMAND, false);
		setEnable(ActionManager.LOAD_EXPRESSION_COMMAND, false);
		setEnable(ActionManager.LOAD_DIRECTORY_COMMAND, false);
		break;
	    case TMEV.DATA_AVAILABLE:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		setEnable(ActionManager.SAVE_IMAGE_COMMAND, false);
		setEnable(ActionManager.PRINT_IMAGE_COMMAND, false);
		setEnable(ActionManager.ANALYSIS_COMMAND, false);
		for (int i=0; i<steppedComboArray.length; i++){
			steppedComboArray[i].setEnabled(false);
			steppedComboArray[i].insertItemAt(this.disabledCategoryIcon[i], 0);
			steppedComboArray[i].removeItemAt(1);
		}
		break;
	    case TMEV.DB_AVAILABLE:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		break;
	    case TMEV.DB_LOGIN:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		break;
	}
    }
    
    /**
     * Enables some buttons according to specified state.
     */
    public void systemEnable(int state) {
	switch (state) {
	    case TMEV.SYSTEM:
		setEnable(ActionManager.LOAD_FILE_COMMAND, true);
		setEnable(ActionManager.LOAD_DIRECTORY_COMMAND, true);
		setEnable(ActionManager.LOAD_EXPRESSION_COMMAND, true);
		break;
	    case TMEV.DATA_AVAILABLE:
		setEnable(ActionManager.SAVE_IMAGE_COMMAND, true);
		setEnable(ActionManager.PRINT_IMAGE_COMMAND, true);
		setEnable(ActionManager.ANALYSIS_COMMAND, true);
		for (int i=0; i<steppedComboArray.length; i++){
			steppedComboArray[i].setEnabled(true);
			steppedComboArray[i].insertItemAt(categoryIcon[i], 0);
			steppedComboArray[i].removeItemAt(1);
		}
		repaint();
		break;
	    case TMEV.DB_AVAILABLE:
		break;
	    case TMEV.DB_LOGIN:
		setEnable(ActionManager.LOAD_DB_COMMAND, true);
		break;
	}
    }

	/**
	 * Disables modules not applicable to microarray data.
	 * 
	 */
	public void enableRNASeq(boolean isRNASeq) {
		int index = 0;
		ArrayList<String> removedModules = new ArrayList<String>();
		if (isRNASeq){
			removedModules.add("BN"); //Bayesiean Networks
			removedModules.add("LM"); //Literature Mining
			removedModules.add("EASE"); //Literature Mining
		} else {
			//RNASeq modules...
			removedModules.add("EDGER"); 
			removedModules.add("DESEQ"); 
			removedModules.add("DEGSEQ"); 
		}
		while (index<totalModules){
			Action action  = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index));
			if (removedModules.contains(action.getValue(Action.NAME))){
				for (int i=0; i<category.length; i++){
					if (action.getValue(ActionManager.CATEGORY).equals(category[i])){
						steppedComboArray[i].removeItem((ImageIcon)action.getValue(ActionManager.LARGE_ICON));
					}
				}
			}				
			index++;
		}
	}
	
    private class ComboListener implements ActionListener{
    	public void actionPerformed(ActionEvent e){      		
    		JComboBox cb = (JComboBox)e.getSource();
    		if (cb == cghSteppedComboBox){
        		if (cb.getSelectedIndex()==0)
        			return;
        		int index = 0;
        		while (((String)manager.getActionNames().get(ActionManager.CGH_NAME_OF_ANALYSIS+String.valueOf(index))) != (String)cb.getItemAt(cb.getSelectedIndex()))
        		{index++;}
        		actiontester = manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index));
        		manager.forwardAction(new ActionEvent(actiontester, e.getID(), (String)actiontester.getValue(Action.ACTION_COMMAND_KEY)));
        		cb.setSelectedIndex(0);
    			return;
    		}
    		if (cb.getSelectedIndex()<0 ||tempDeletion){
    			cb.setSelectedIndex(0);
    			return;
    		}
    		int index = 0;
    		while (manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)).getValue(ActionManager.LARGE_ICON)!= cb.getItemAt(cb.getSelectedIndex())){
    			index++;
    			if (index>=totalModules){
    				return;
    			}
    		}
    		actiontester = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index));
    		manager.forwardAction(new ActionEvent(actiontester, e.getID(), (String)actiontester.getValue(Action.ACTION_COMMAND_KEY)));
    		cb.putClientProperty("categoryIconExists", true);
    		cb.insertItemAt((ImageIcon)cb.getClientProperty("categoryIcon"), 0);
    		cb.setSelectedIndex(0);
    	}
    }
    

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                                           JList list,
                                           Object value,
                                           int index,
                                           boolean isSelected,
                                           boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            int i = 0;
            ImageIcon image;
            boolean header = false;
            boolean cghAnalysis = false;
            String title;
            
    		while (value != manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(i)).getValue(ActionManager.LARGE_ICON)){
    			i++;
    			if (manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(i))==null){
    				if (manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index))!=null)
    					cghAnalysis = true;
    				header = true;
    				break;
    			}
    		}
    		
    		if (header){ 
            	title = " ";
            	this.setBorder(null);
            	image = (ImageIcon)value;
            }else{
            	image = (ImageIcon)value;
            	title = (String)manager.getActionNames().get(ActionManager.NAME_OF_ANALYSIS+String.valueOf(i));
	            this.setBorder(BorderFactory.createLineBorder(Color.gray));
            }
    		if (cghAnalysis){
    			image = (ImageIcon)manager.getActionNames().get(ActionManager.CGH_SMALL_ICON+String.valueOf(i));
    		}
    		setIcon(image);
    		setText(title);
            setFont(list.getFont());

            return this;
        }
    }
    
    
    
    
    class SteppedComboBoxUI extends MetalComboBoxUI {
    	  protected ComboPopup createPopup() {
    	    BasicComboPopup popup = new BasicComboPopup( comboBox ) {
    	    	
    	      public void show() {    	       
	    	        tempDeletion = true;
	    	        if (comboBox.getClientProperty("categoryIconExists")==(Object)true){
	    	        	comboBox.removeItemAt(0);
	    	        	comboBox.putClientProperty("categoryIconExists", false);
	    	        }
	    	        tempDeletion = false;
	    	        Dimension popupSize = ((SteppedComboBox)comboBox).getPopupSize();
	    	        popupSize.setSize( popupSize.width,
	    	          getPopupHeightForRowCount( comboBox.getMaximumRowCount() ) );
	    	        Rectangle popupBounds = computePopupBounds( 0,
	    	          comboBox.getBounds().height, popupSize.width, popupSize.height);
	    	        scroller.setMaximumSize( popupBounds.getSize() );
	    	        scroller.setPreferredSize( popupBounds.getSize() );
	    	        scroller.setMinimumSize( popupBounds.getSize() );
	    	        list.invalidate();
	    	        list.clearSelection();
	    	        list.ensureIndexIsVisible( list.getSelectedIndex() );
	    	        setLightWeightPopupEnabled( comboBox.isLightWeightPopupEnabled() );
	    	        comboBox.setFocusable(false);
	    	        show( comboBox, popupBounds.x, popupBounds.y );
    	      }
    	    };
    	    popup.getAccessibleContext().setAccessibleParent(comboBox);
    	    return popup;
    	  }
    	}
    	 
    	 
    	public class SteppedComboBox extends JComboBox {
    	  /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		protected int popupWidth;
    	 
    	  public SteppedComboBox(ComboBoxModel aModel) {
    	    super(aModel);
    	    setUI(new SteppedComboBoxUI());
    	    popupWidth = 0;
    	  }
    	 
    	  public SteppedComboBox(final Object[] items) {
    	    super(items);
    	    setUI(new SteppedComboBoxUI());
    	    popupWidth = 0;
    	  }
    	  public SteppedComboBox() {
      	    super();
      	    setUI(new SteppedComboBoxUI());
      	    popupWidth = 0;
      	  }
      	 
    	  public SteppedComboBox(Vector items) {
    	    super(items);
    	    setUI(new SteppedComboBoxUI());
    	    popupWidth = 0;
    	  }
    	 
    	 
    	  public void setPopupWidth(int width) {
    	    popupWidth = width;
    	  }
    	 
    	  public Dimension getPopupSize() {
    	    Dimension size = getSize();
    	    if (popupWidth < 1) popupWidth = size.width;
    	    return new Dimension(popupWidth, size.height);
    	  }
    	}
    	
    
}
