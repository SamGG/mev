
package org.tigr.microarray.mev.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.remote.soap.ResourcererFTPClient;




/**
 *
 * @author  Sarita Nair
 */
public class AnnotationDialog extends AlgorithmDialog{
	
	private JComboBox  annFileKeyBox, organismListBox;
	private JButton connect;
	private static JLabel statusLabel=new JLabel();
	private String annotationFileName;
	
	private String [] annFileKeys= new String[] {};
	private String[]organismListKey= new String[] {};
	
	private ColumnNamesPanel fieldSelectionPanel;
	public FileDownloadProgressPanel progressPanel;
	private int result = JOptionPane.CANCEL_OPTION;
	private Hashtable <String, Vector> Org2chipType;
	private Hashtable AllHash;
	
	private Vector organismNames=new Vector();
	private Vector arrayNames=new Vector();
	
	public ReadTaxonFile readFile;
	
	
	
	
	/** Creates a new instance of AnnotationDialog */
	public AnnotationDialog(JFrame frame) {
		
		
		super(frame, "Annotation", true);
		ReadTaxonFile readFile=new ReadTaxonFile("anonymous","");
		readFile.connectToResourcerer();
		
		try {
		AllHash=readFile.Org2ChipType(new File(readFile.getTaxonFilePath()));
		
		organismNames=(Vector)AllHash.get("OrganismList");
		//System.out.println("organismNames size:"+organismNames.size());
		this.organismListKey =Vector2StringArray(organismNames);
		
		
		Org2chipType=(Hashtable)AllHash.get("Org2ChipType");
		arrayNames=(Vector)Org2chipType.get(organismNames.firstElement());
	//	System.out.println("arrayNames size:"+arrayNames.size());
		this.annFileKeys=Vector2StringArray(arrayNames);
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	
		
		JLabel annKeyLabel = new JLabel("Select organism and chip type below then click Connect");
		annFileKeyBox = new JComboBox(annFileKeys);
		annFileKeyBox.addActionListener(new Listener());
		
		JLabel chipTypeName=new JLabel("Select chip type");
		organismListBox=new JComboBox(organismListKey);
		organismListBox.addActionListener(new Listener());
		
		JLabel fileKeyLabel = new JLabel("Connect to Resourcerer"); 
		
		connect=new JButton("Connect");
		connect.setSize(new Dimension(100, 30));
		connect.setPreferredSize(new Dimension(100, 30));
		connect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				//System.out.println("Trying to connect...");
				onConnect(getFileAnnotationKey(), getOrganismNameKey());
			}
		});    
		
		
		fieldSelectionPanel = new ColumnNamesPanel(annFileKeys);
		
		ParameterPanel panel = new ParameterPanel("Annotation Import Dialog");
		panel.setLayout(new GridBagLayout());
		
		panel.add(annKeyLabel, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,10,0,10),0,0));
		panel.add(organismListBox, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25,10,0,10),0,0));
		panel.add(chipTypeName, new GridBagConstraints(0,2,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,10,0,10),0,0));
		panel.add(annFileKeyBox, new GridBagConstraints(0,3,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25,10,0,10),0,0));
		
		panel.add(fileKeyLabel, new GridBagConstraints(0,4,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,20,10),0,0));        
		panel.add(connect, new GridBagConstraints(0,5,1,1,50,10, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,10,5,0),0,0));
		//Changed grid height from 5 to 1
		panel.add(fieldSelectionPanel, new GridBagConstraints(0,6,1,1,50,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,20,0),0,0));        
		
		setActionListeners(new Listener());
		addContent(panel);
		pack();
	}
	
	
	
	public String[]Vector2StringArray(Vector vec){
		String[]array=new String[vec.size()] ;
		
		for(int i=0; i<vec.size();i++) {
			array[i]=(String)vec.get(i);
		}
	return array;	
		
	}
	
	
	
	
	
	public void onConnect(final String chipType, final String organismType) {
		
		progressPanel = new FileDownloadProgressPanel("Downloading Annotation File..", AnnotationDialog.this);
		progressPanel.setLocationRelativeTo(this);
		progressPanel.setVisible(true);
		progressPanel.setAlwaysOnTop(true);
		progressPanel.setIndeterminate(true);
		
		Thread thread = new Thread( new Runnable(){
			public void run() {
				try {
					
					ResourcererFTPClient ftpclient=new ResourcererFTPClient(chipType, organismType,
							"anonymous","", AnnotationDialog.this);
					ftpclient.connectToResourcerer();
					
					progressPanel.dispose();
					
					AnnotationDialog.this.cancelButton.setEnabled(true);
					AnnotationDialog.this.resetButton.setEnabled(true);
					setAnnotationFileName(ftpclient.getAnnotationFileName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				progressPanel.dispose();
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	//Changes the label to reflect current status
	public static void statusChange(String upDate) {
		statusLabel.setText(upDate);
	}
	
	
	public String getAnnotationFileName() {
		return annotationFileName;
	}
	
	public void setAnnotationFileName(String fileName) {
		this.annotationFileName=fileName;
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
	
	/** Returns the annotation key to identify genes in the annotation file
	 */
	public String getFileAnnotationKey() {
		return (String)this.annFileKeyBox.getSelectedItem();
	}
	
	public String getOrganismNameKey() {
		return (String)this.organismListBox.getSelectedItem();
	}
	
	/** Resets the controls to the initial state
	 */
	private void resetControls() {
		this.annFileKeyBox.setSelectedIndex(0);
		
	}
	
	
	 protected void updateLabel(String name) {
		annFileKeyBox.removeAllItems();
		Vector annFileKeyBoxItems = (Vector)(this.Org2chipType.get(name));
		for(int i = 0; i < annFileKeyBoxItems.size(); i++)
			annFileKeyBox.addItem(annFileKeyBoxItems.elementAt(i));
		
	}
		
	   
	
	
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener {
		
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
						
			if(e.getSource().equals(organismListBox)) {
				updateLabel((String)organismListBox.getSelectedItem()); 
			}			
			if (command.equals("ok-command")) {
				result = JOptionPane.OK_OPTION;
				statusChange("");
				dispose();
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			}
			else if (command.equals("reset-command")){
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			}
			else if (command.equals("info-command")){
				HelpWindow hw = new HelpWindow(AnnotationDialog.this, "Gene Annotation Import");
				result = JOptionPane.CANCEL_OPTION;
				if(hw.getWindowContent()){
					hw.setSize(600,650);
					hw.setLocation();
					hw.show();
					return;
				} else {
					hw.setVisible(false);
					hw.dispose();
					return;
				}
			}
			//dispose();
		}
		
		public void windowClosing(WindowEvent e) {
			result = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}    
	
	
	private class ColumnNamesPanel extends JPanel {
		
		JCheckBox[] columnNameBoxes;
		
		JButton selectAllButton, clearAllButton;
		
		ColumnNamesPanel(String [] columnHeaders) {
			
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createTitledBorder("File Download Status"));
			this.setBackground(Color.white);
			
			statusLabel.setAutoscrolls(true);
			
			//add(statusLabel, new GridBagConstraints(0,0,1,1,50,10, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,10,5,0),0,0));       
			add(statusLabel, new GridBagConstraints(0,1,2,1,100,90, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));            
		}
		
		
	}    
	
	//Trial  class for status bar
	
	public class FileDownloadProgressPanel extends JFrame implements ActionListener {
		
		public JProgressBar progressBar;
		JPanel progressPanel;
		
		public FileDownloadProgressPanel(String initialMessage, AlgorithmDialog dialog) {
			setTitle(initialMessage);
			
			
			progressPanel = new JPanel(new BorderLayout());
			progressPanel.setPreferredSize(new Dimension(350, 80));
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setPreferredSize(new Dimension(310, 30));
			progressBar.setVisible(true);
			
//			cancelButton = new JButton("Cancel");
//			cancelButton.setActionCommand("cancel");	      
//			cancelButton.addActionListener(this);
//			cancelButton.setPreferredSize(new Dimension(70, 30));
			
			progressPanel.add(progressBar, BorderLayout.PAGE_START);
//			progressPanel.add(cancelButton, BorderLayout.CENTER);
			progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
			setContentPane(progressPanel);
			
			progressPanel.setOpaque(true);
			//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			pack(); 
			setVisible(true);
		}
		
		public void update(String message){
			//System.out.println("update msg:"+message);
			progressBar.setString(message);
			progressBar.setValue(progressBar.getMinimum());
		}
		
		public void increment(){
			progressBar.setValue(progressBar.getValue() + 1);
		}
		
		public void setMaximum(int i){
			progressBar.setMaximum(i);
		}
		
		public int getMaximum(){
			return progressBar.getMaximum();
		}
		
		public void setValue(int i){
			progressBar.setValue(i);
		}
		
		public void setIndeterminate(boolean b){
			progressBar.setIndeterminate(b);
		}
		
		public void actionPerformed(ActionEvent evt) {
			String command = evt.getActionCommand();
			if (command.equals("cancel")) {
				dispose();
				//mav.cancelLoadState();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				progressBar.setIndeterminate(true);
				progressBar.setString("Cleaning Up...");
			}
		}
		public void onClose(){
			dispose();
			//mav.cancelLoadState();
		}
	}
	
	
	public static void main(String [] args) {
		
		AnnotationDialog dialog = new AnnotationDialog(new JFrame());
		dialog.showModal();
		//System.exit(0);
	}

	
	
	
}
