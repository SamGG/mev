package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


public abstract class GSEAWizard extends AlgorithmDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Current step in the process
	 */
	protected int currentStepIndex = 0;
	/**
	 * Total number of steps
	 */
	private int totSteps;
	/**
	 * Step titles
	 */
	private String [] stepTitles;	
	/**
	 * process display panel, left side with steps
	 */
	private ProcessDisplayPanel processPanel;
	/**
	 * Button bar with next, previous
	 */
	private WizardButtonBar wButtonPanel;
	/**
	 * Main panel the should adapt to the displayed panel
	 */
	private JPanel mainPanel;
	/**
	 * AlgorithmData to hold collected parameters
	 */
	private AlgorithmData algData;
	/**
	 * Result after dismissing dialog
	 */
	protected int result = JOptionPane.CANCEL_OPTION;
	
	
	
	
	public GSEAWizard(JFrame parent, String title, boolean modal, AlgorithmData params, String[] stepTitles,
			 int stepCount, JPanel initialPanel) {
		super(parent, title, modal);
		 setBounds(0,0, 800, 850);
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setPreferredSize(new Dimension(800, 850));
		//stepTitles = steps;		
		algData = params;		
		//create the ProcessDisplayPanel
		processPanel = new ProcessDisplayPanel(true, stepTitles);		
		
		totSteps = stepCount;
		//build a custom button bar to supplant into parent
		wButtonPanel = new WizardButtonBar();
		mainPanel.add(processPanel, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5),0,0));			
		super.addContent(mainPanel);		
		supplantButtonPanel(wButtonPanel);				
		validate();
		//pack();
		
		
		
	}
	
	
	public void setParamSufficient(boolean suff){
		wButtonPanel.setEnableNextButton(suff);
	}
	
	
	/**
	 * Sets an initial panel to display
	 * @param initialPanel
	 */
	public void setInitialPanel(JPanel initialPanel) {
		mainPanel.add(initialPanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
	}	
	
	/**
	 * Packs the wizard after making panel changes
	 */
	public void updateWizard() {
	//	pack();
	}

	
	

	/**
	 * Prepares the next component to display.  A combination of current algorithm parameters
	 * in AlgorithmData and step index is used to prepare the returned component for display.
	 * 
	 * @param currAlgData <code>AlgorithmData</code> containing the current parameters
	 * @param currentStepIndex the current step index which is incremented and decremented with next and prev
	 * @return returns the IWizardParameterPanel (JPanel extension) to display
	 */
	protected abstract IWizardParameterPanel prepareAndDeliverNextParameterPanel(AlgorithmData currAlgData, int currentStepIndex);
	
	/**
	 * Prepares the next component to display.  A combination of current algorithm parameters
	 * in AlgorithmData and step index is used to prepare the returned component for display.
	 * 
	 * @param currAlgData <code>AlgorithmData</code> containing the current parameters
	 * @param currentStepIndex the current step index which is incremented and decremented with next and prev
	 * @return returns the IWizardParameterPanel (JPanel extension) to display
	 */
	protected abstract IWizardParameterPanel prepareAndDeliverPreviousParameterPanel(AlgorithmData currAlgData, int currentStepIndex);
	
	/**
	 * Stets the step titles to display
	 * @param titles
	 */
	public void setStepTitles(String [] titles) {
		stepTitles = titles;
	}
	
	/**
	 * Returns the current step in the process
	 * @return current step
	 */
	public int getCurrentStepIndex() {
		return currentStepIndex;
	}
	
	/**
	 * Returns the current step title
	 * @return
	 */
	public String getCurrentStepTitle() {
		return stepTitles[currentStepIndex];
	}

	
    /** Shows the dialog.
     * @return  the int indicating OK or CANCEL
     * 
     */
    public int showModal() {
    	  return result;
    }
	
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    
    
    
    
    
	
	  /**
     * Advances to the next step in the process
     * @return
     */
	protected boolean nextStep() {
		
		
		//this method calls the abstract method to prepare the next panel
		JPanel nextPanel = (JPanel)prepareAndDeliverNextParameterPanel(algData, currentStepIndex);
		                   
		if(nextPanel == null)
			return false;
		
		//advance the index
		
		currentStepIndex++;
		
		//advance the highlighted step in the process display panel
		processPanel.setHighlight(currentStepIndex);
				
		//post the next panel...
	
		//Zeroth component will be the process display panel.
		Component comp = mainPanel.getComponent(1);
		mainPanel.remove(comp);
		validate();
		
		//The "Process Panel" is no longer on the side of the screen. So coordinates for the panel after process panel will be as below
		mainPanel.add(nextPanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		validate();
		
		if(currentStepIndex >= totSteps) {
			wButtonPanel.setNextText("Execute");
		}
		
		if(currentStepIndex >=1) {
			wButtonPanel.setEnableBackButton(true);		
		}

		//update for viewing, if needed
		((IWizardParameterPanel)nextPanel).onDisplayed();

		pack();	
	    //recenter
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	    
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		
	
	    setParamSufficient(false);
		return true;
	}
	
	
	
	private void prevStep() {

		//pull parameters into AlgorithmData
		//by calling AlgorithmPanel method to populate AlgorithmData
		IWizardParameterPanel prevPanel = prepareAndDeliverPreviousParameterPanel(algData, currentStepIndex);
		
		//advance the index
		currentStepIndex=currentStepIndex-1;
		
		//advance the highlighted step in the process display panel
		processPanel.setHighlight(currentStepIndex);
				
		//post the next panel...
		//Replace the second component and so on of main panel, first component is always process panel
		
		Component comp = mainPanel.getComponent(1);
		mainPanel.remove(comp);
			
	
		
		mainPanel.add((JPanel)prevPanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
	
		mainPanel.revalidate();
		//mainPanel.updateUI();
	    
		if(currentStepIndex < totSteps) {
			wButtonPanel.setNextText("Next >");
		}
		
		if(currentStepIndex < 1) {
			wButtonPanel.setEnableBackButton(false);			
		}
		
//		//update for viewing, if needed
//		((IWizardParameterPanel)prevPanel).onDisplayed();

		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	    
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		
	}
	
	
		
	
	private class ProcessDisplayPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String [] stepTitles;
		private JTextPane processPane;
		
		
		public ProcessDisplayPanel(boolean isTextDisplay, String[] stepTitles) {
			super();
			setBackground(Color.white);
			this.stepTitles = stepTitles;
			constructPanel();
		}

		/**
		 * Builds the process panel
		 */
		private void constructPanel() {
			setLayout(new GridBagLayout());
			processPane = new JTextPane();
			processPane.setEditable(false);			
			processPane.setContentType("text/html");
			processPane.setOpaque(false);
			processPane.setMargin(new Insets(10,10,10,10));
			Document doc = processPane.getDocument();
			String stepDescription = "<html><font face=\"MS Sans Serif\"><b><u>Process Outline</u></b><br><br>";						
			try {
				doc.insertString(doc.getLength(), stepDescription, null);			
				for(int i = 0; i < stepTitles.length; i++) {
					stepDescription += String.valueOf(i+1);
					stepDescription += ".) ";
				//	stepDescription += stepTitles[i]+"<br><br>";
					stepDescription += stepTitles[i]+"    ";
				}
				stepDescription += "</font></html>";			
				processPane.setText(stepDescription);
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}
			setHighlight(0);	
			add(processPane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
			
			
		}
		
		
		private void setHighlight(int step) {
		//	System.out.println("step to be highlighted in GSEAWizard:"+step);
			try {				
				Document doc = processPane.getDocument();			
				String text = doc.getText(0, doc.getLength());			
				int pos = text.indexOf((step+1)+".)");
				int endPos = text.indexOf(".",pos+2)+1;
				
				if(endPos < 0)
					endPos = text.length();
				
				if(pos > -1) {									
					Highlighter highlighter = processPane.getHighlighter();
					if(highlighter != null) {
						highlighter.removeAllHighlights();
						highlighter.addHighlight(pos, endPos, new MyHighlightPainter(new Color(200, 200, 255)));
					}
				}			
			} catch (BadLocationException ble) {
				//worst that can happen is no highlight..... let it go
			}		
		}

		/**
		 * Paint the process dialog
		 */
		public void paint(Graphics g) {
			super.paint(g);			
			
			GradientPaint gp = new GradientPaint(0, getHeight()/2, new Color(210,210,255), getWidth()/4,
					getHeight()/2, Color.white, false);			
			((Graphics2D)g).setPaint(gp);
			g.fillRect(0,0,getWidth()/2, getHeight());
			
			gp = new GradientPaint(getWidth(), getHeight()/2, new Color(210,210,255), getWidth()*3/4,
					getHeight()/2, Color.white, false);			
			((Graphics2D)g).setPaint(gp);
			g.fillRect(getWidth()/2,0,getWidth(), getHeight());		
			processPane.paint(g);
		}				


	// A private subclass of the default highlight painter
	class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
	    public MyHighlightPainter(Color color) {
	        super(color);
	    }
	}
	}

	
	 /**
     * Simple button panel class to coordinate next and back events
     * Including button disabling at end points
     *
     * @author braisted
     */
	private class WizardButtonBar extends JPanel {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton nextButton, backButton;
		
		public WizardButtonBar() {
			super(new GridBagLayout());	
			Listener listener = new Listener();			
			setPreferredSize(new Dimension(300, 30));
			setSize(new Dimension(300, 30));
			JButton cancelButton = new JButton("Cancel");
		
			JButton infoButton = new JButton(null, GUIFactory.getIcon("Information24.gif"));
			infoButton.setActionCommand("info-command");
			infoButton.addActionListener(listener);
			infoButton.setFocusPainted(false);
			infoButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			infoButton.setPreferredSize(new Dimension(30, 30));
		
			JLabel label = new JLabel(GUIFactory.getIcon("dialog_button_bar.gif"));
		         
			
			cancelButton.setActionCommand("cancel-command");
			cancelButton.addActionListener(listener);
			cancelButton.setFocusPainted(false);
			cancelButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			//cancelButton.setBackground(new Color(211,211,211));
			cancelButton.setPreferredSize(new Dimension(100, 30));
			
			backButton = new JButton("< Back");
			backButton.setActionCommand("back-command");
			backButton.addActionListener(listener);
			backButton.setEnabled(false);
			backButton.setFocusPainted(false);
			backButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		//	backButton.setBackground(new Color(211,211,211));
			backButton.setPreferredSize(new Dimension(100, 30));

			nextButton = new JButton("Next >");
			nextButton.setActionCommand("next-command");
			nextButton.addActionListener(listener);
			nextButton.setEnabled(false);
			nextButton.setFocusPainted(false);
			nextButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			//nextButton.setBackground(new Color(211,211,211));
			nextButton.setPreferredSize(new Dimension(100, 30));		
			
			add(infoButton, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,10,0,30),0,0));
			add(label, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			add(cancelButton, new GridBagConstraints(2,0,1,1,1,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,10,0,30),0,0));
			add(backButton, new GridBagConstraints(3,0,1,1,0,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,5),0,0));
			add(nextButton, new GridBagConstraints(4,0,1,1,0,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,10),0,0));
		}
		
		public void setNextText(String text) {
			nextButton.setText(text);			
		}
		
		public void setEnableBackButton(boolean enable) {
			backButton.setEnabled(enable);
		}
		
		public void setEnableNextButton(boolean enable) {
			nextButton.setEnabled(enable);
		}
		public void paint(Graphics g) {
			super.paint(g);
		}				
	}
	
	
	
	/**
	 * Listener class to fire next and previous events
	 * @author braisted
	 */
	public class Listener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			
			if(command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if(command.equals("next-command")) {
				JButton button = (JButton)(e.getSource());
				
				if(button.getText().equals("Execute")) {
					
					nextStep();
					String eMsg=new String();
					if(algData.getParams().getString("gene-identifier").equals("")||algData.getParams().getString("gene-set-directory").equals("")||algData.getParams().getString("annotation-file").equals("")){
				
						eMsg="<html>MeV is missing some required parameters, so you cannot execute...just yet<br>" +
						"<html>Please make sure that you <br>" +
						"1. Selected gene identifer present in your gene set file from the drop down list<br>"+
						"2. Loaded the gene set file AND the annotation file.<br></html>";
					
					JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						
					result = JOptionPane.OK_OPTION;
					dispose();
					}
				
				} else {
						nextStep();
				}	
			} else if(command.equals("back-command")) {
				prevStep();
			} else if(command.endsWith("info-command")){
				HelpWindow.launchBrowser(GSEAWizard.this, "GSEA Help Dialog");
			}
		}
		
	}
	
	
	
}
