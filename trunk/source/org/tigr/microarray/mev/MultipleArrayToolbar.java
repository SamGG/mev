/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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
import java.net.URL;
import java.util.ArrayList;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GrayFilter.*;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.BorderFactory; 

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;

public class MultipleArrayToolbar extends JToolBar {
    
    /**
     * Construct a <code>MultipleArrayToolbar</code> using
     * specified action manager.
     * @see ActionManager
     */
    public MultipleArrayToolbar(ActionManager manager) { 
    	addAlgorithmActions(manager);
    }
    
    Action actiontester;
    ActionManager manager;
    SteppedComboBox[] steppedComboArray;
    SteppedComboBox cghSteppedComboBox;
    ImageIcon comboIcon;
    String [] category = {"Clustering","Statistics","Classification","Data Reduction","Meta Analysis","Visualization","Miscellaneous"};

	ImageIcon[] categoryIcon = new ImageIcon[category.length];
	ImageIcon[] disabledCategoryIcon = new ImageIcon[category.length];
    /**
     * Adds actions into the toolbar.
     */
  
    private int algorithmCount(ActionManager manager){
    	int count=0;
    	Action action;
    	while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(count)))!=null){
    		count++;
    	}
    	return count;
    }
    
    private void addAlgorithmActions(ActionManager manager) {
    	this.manager = manager;
    	Action action;
    	steppedComboArray = new SteppedComboBox[category.length];
    	
    	/*
    	for (int i=0; i<category.length; i++){
    		steppedComboArray[i] = new SteppedComboBox();
    	    ComboListener comboListener = new ComboListener();
			steppedComboArray[i].addItem(category[i]);
    		steppedComboArray[i].addActionListener(comboListener);
    		steppedComboArray[i].setMaximumRowCount(100);
    	}
    	*/
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
    		steppedComboArray[i].setMaximumRowCount(13);	
    		//int gray = 255;
    		//steppedComboArray[i].setBackground(new Color(gray, gray, gray));
    		steppedComboArray[i].addItem(disabledCategoryIcon[i]);
    	}
    	int index = 0;
    	for(int i=0;i<category.length;i++) {
   			 while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)))!=null) {
   				if((action.getValue(ActionManager.CATEGORY)).equals(category[i])) {
   					//add(action);   // commented to clear way for comboBoxes
   					steppedComboArray[i].addItem((ImageIcon)action.getValue(ActionManager.LARGE_ICON));
   					steppedComboArray[i].setActionCommand(ActionManager.ANALYSIS_COMMAND);
   				}
   				index++;
   			 }
   			ComboBoxRenderer renderer = new ComboBoxRenderer();
   		    steppedComboArray[i].setSelectedIndex(0);
   		    steppedComboArray[i].setRenderer(renderer);
   		    int preferredWidth = steppedComboArray[i].getPreferredSize().width;
   			steppedComboArray[i].setPreferredSize(new Dimension(50, steppedComboArray[i].getPreferredSize().height));
   			steppedComboArray[i].setMinimumSize(new Dimension(0,0));
   			//steppedComboArray[i].setMaximumSize(new Dimension(106,38));
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
        Action action;
        while ((action = manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index)))!=null) {
            cghSteppedComboBox.addItem((String)manager.getActions().get(ActionManager.CGH_NAME_OF_ANALYSIS+String.valueOf(index)));
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
				steppedComboArray[i].setEnabled(false);  //uncomment before committing
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
    private class ComboListener implements ActionListener {
    	public void actionPerformed(ActionEvent e){          
    		JComboBox cb = (JComboBox)e.getSource();
    		if (cb == cghSteppedComboBox){
    			
        		if (cb.getSelectedIndex()==0)
        			return;
        		int index = 0;
        		while (((String)manager.getActions().get(ActionManager.CGH_NAME_OF_ANALYSIS+String.valueOf(index))) != (String)cb.getItemAt(cb.getSelectedIndex()))
        		{index++;}
        		actiontester = manager.getAction(ActionManager.CGH_ANALYSIS_ACTION+String.valueOf(index));
        		manager.forwardAction(new ActionEvent(actiontester, e.getID(), (String)actiontester.getValue(Action.ACTION_COMMAND_KEY)));
        		cb.setSelectedIndex(0);
    			
    			return;
    		}
    		if (cb.getSelectedIndex()==0)
    			return;
    		int index = 0;
    		while (manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)).getValue(ActionManager.LARGE_ICON)!= cb.getItemAt(cb.getSelectedIndex()))
    		{index++;}
    		actiontester = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index));
    		manager.forwardAction(new ActionEvent(actiontester, e.getID(), (String)actiontester.getValue(Action.ACTION_COMMAND_KEY)));
    		cb.setSelectedIndex(0);
    	}
    }
    

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {
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
            	title = (String)manager.getActions().get(ActionManager.NAME_OF_ANALYSIS+String.valueOf(i));
	            this.setBorder(BorderFactory.createLineBorder(Color.gray));
            }
    		if (cghAnalysis){
    			image = (ImageIcon)manager.getActions().get(ActionManager.CGH_SMALL_ICON+String.valueOf(i));
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
    	        Dimension popupSize = ((SteppedComboBox)comboBox).getPopupSize();
    	        popupSize.setSize( popupSize.width,
    	          getPopupHeightForRowCount( comboBox.getMaximumRowCount() ) );
    	        Rectangle popupBounds = computePopupBounds( 0,
    	          comboBox.getBounds().height, popupSize.width, popupSize.height);
    	        scroller.setMaximumSize( popupBounds.getSize() );
    	        scroller.setPreferredSize( popupBounds.getSize() );
    	        scroller.setMinimumSize( popupBounds.getSize() );
    	        list.invalidate();
    	        int selectedIndex = comboBox.getSelectedIndex();
    	        if ( selectedIndex == -1 ) {
    	          list.clearSelection();
    	        } else {
    	          list.setSelectedIndex( selectedIndex );
    	        }
    	        list.ensureIndexIsVisible( list.getSelectedIndex() );
    	        setLightWeightPopupEnabled( comboBox.isLightWeightPopupEnabled() );
    	 
    	        show( comboBox, popupBounds.x, popupBounds.y );
    	      }
    	    };
    	    popup.getAccessibleContext().setAccessibleParent(comboBox);
    	    return popup;
    	  }
    	}
    	 
    	 
    	public class SteppedComboBox extends JComboBox {
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
