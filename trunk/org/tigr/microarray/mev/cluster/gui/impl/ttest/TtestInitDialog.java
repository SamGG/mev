/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TtestInitDialog.java,v $
 * $Revision: 1.12 $
 * $Date: 2006-10-24 16:28:03 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ttest;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.StringSplitter;
/**
 *
 * @author  nbhagaba
 * @version
 */
public class TtestInitDialog extends AlgorithmDialog {
    
    GroupExperimentsPanel gPanel;
    OneClassPanel oPanel;
    TwoClassPairedMainPanel tcpmPanel;    
    PValuePanel pPanel;
    SignificancePanel sPanel;
    //HCLSelectionPanel hclOpsPanel;
    HCLSigOnlyPanel hclOpsPanel;    
    Vector exptNames;
    JTabbedPane chooseDesignPane;
    DfCalcPanel dPanel;
    
    boolean lotsOfSamples = false;
    String lotsOfSamplesWarningText = "                                                Note: You can assign large numbers of samples quickly by using a saved text file.";
    
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;
    public static final int BETWEEN_SUBJECTS = 7;
    public static final int ONE_CLASS = 8;
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    public static final int PAIRED = 11;   
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;
    
    boolean okPressed = false;
    boolean permParamOkPressed = false;
    private int userNumCombs = 0;
    private boolean allCombsUsed = false;
    private int allPossCombs;
    private Color LABEL_COLOR = UIManager.getColor("Label.foreground");
    
    
    boolean tooMany = false;
    int count;
   
    final int fileLoadMin=20;
    /** Creates new TtestInitDialog */
    public TtestInitDialog(JFrame parentFrame, boolean modality, Vector exptNames) {
        super(parentFrame, "TTEST: T-test", modality);
        this.exptNames = exptNames;
        setBounds(0, 0, 800, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        if(exptNames.size()>fileLoadMin){
        	lotsOfSamples = true;
        }
        
        chooseDesignPane = new JTabbedPane();
        sPanel = new SignificancePanel();
        pPanel = new PValuePanel();
        gPanel = new GroupExperimentsPanel(exptNames);
        for (count = 0; count < gPanel.groupARadioButtons.length; count++) {
            gPanel.groupARadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.groupARadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
            });
       }
        for (count = 0; count < gPanel.groupBRadioButtons.length; count++) {
            gPanel.groupBRadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.groupBRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
                
            });
        }
        
        for (count = 0; count < gPanel.neitherGroupRadioButtons.length; count++) {
            gPanel.neitherGroupRadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.neitherGroupRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                     
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
                
            });
       }
        count = 0;
        oPanel = new OneClassPanel();
        for (int i = 0; i < oPanel.includeExpts.length; i++) {
            oPanel.includeExpts[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.neitherGroupRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                     
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }                
            });
       }
        chooseDesignPane.add("One-class", oPanel);
        chooseDesignPane.add("Between subjects", gPanel);
        tcpmPanel = new TwoClassPairedMainPanel();
        chooseDesignPane.add("Paired", tcpmPanel);        
        pPanel.tDistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sPanel.justAlphaButton.setSelected(true);
                sPanel.maxTButton.setSelected(false);
                sPanel.maxTButton.setEnabled(false);
                sPanel.minPButton.setSelected(false);
                sPanel.minPButton.setEnabled(false);  
                sPanel.falseNumButton.setEnabled(false);
                sPanel.falsePropButton.setEnabled(false);
                sPanel.calcFDRPVals.setEnabled(false);
                sPanel.fastFDRButton.setEnabled(false);
                sPanel.slowFDRButton.setEnabled(false);
                sPanel.falseNumField.setEnabled(false);
                sPanel.falsePropField.setEnabled(false);
            }
        });
        pPanel.permutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == pPanel.permutButton) {                    
                    sPanel.maxTButton.setEnabled(true);
                    //sPanel.minPButton.setEnabled(true);    // **** ENABLE THIS WHEN MINP MTHOD HAS BEEN DEBUGGED  - 1/12/2004  
                    sPanel.falseNumButton.setEnabled(true);
                    sPanel.falsePropButton.setEnabled(true);
                    //sPanel.calcFDRPVals.setEnabled(true);
                    sPanel.fastFDRButton.setEnabled(true);
                    //sPanel.slowFDRButton.setEnabled(true);
                    sPanel.falseNumField.setEnabled(true);
                    sPanel.falsePropField.setEnabled(true); 
                    
                    if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                        int[] grpAssignments = getGroupAssignments();
                        int grpACounter = 0;
                        int grpBCounter = 0;
                        for (int i = 0; i < grpAssignments.length; i++) {
                            if (grpAssignments[i] == GROUP_A) {
                                grpACounter++;
                            } else if (grpAssignments[i] == GROUP_B) {
                                grpBCounter++;
                            }
                        }
                        if ((grpACounter < 2) || (grpBCounter < 2)) {
                            pPanel.numCombsLabel.setForeground(Color.red);
                            pPanel.numCombsLabel.setText("Error! Group A and Group B must each contain more than one sample");
                            //JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                        } else {
                            int numCombs = 0;
                            pPanel.numCombsLabel.setForeground(Color.black);
                            pPanel.numCombsLabel.setText("There are too many unique permutations                                  ");
                            pPanel.allCombsButton.setEnabled(false);
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.randomGroupsButton.setSelected(true);
                            pPanel.timesField.setEnabled(true);
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setText("100");
                            //tooMany = true;
                            if ((grpACounter + grpBCounter) <= 20) {
                                numCombs = getNumCombs((grpACounter + grpBCounter), grpACounter);
                                pPanel.numCombsLabel.setForeground(Color.black);
                                pPanel.numCombsLabel.setText("There are " + numCombs + " unique permutations                                ");
                                allPossCombs = numCombs;
                                pPanel.allCombsButton.setEnabled(true);
                                pPanel.randomGroupsButton.setEnabled(true);
                            }
                        }
                    } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                        pPanel.numCombsLabel.setForeground(Color.black);
                        int validNum = getNumValidOneClassExpts();
                        if (validNum <= 1) {
                            pPanel.numCombsLabel.setForeground(Color.red);
                            pPanel.numCombsLabel.setText("Error! Choose at least two samples");
                            pPanel.randomGroupsButton.setEnabled(false); 
                            pPanel.timesField.setBackground(Color.gray);
                            pPanel.timesField.setEnabled(false);
                        } else if (validNum <= 29) {
                            pPanel.numCombsLabel.setText("There are " + (int)Math.pow(2, validNum) + " possible combinations                    ");
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.allCombsButton.setEnabled(true);
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setEnabled(true);                           
                        } else {
                            pPanel.numCombsLabel.setText("There are too many unique combinations                                       ");
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setEnabled(true);  
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.randomGroupsButton.setSelected(true);                            
                        }
                        //pPanel.allCombsButton.setEnabled(false);
                        /*
                        pPanel.randomGroupsButton.setEnabled(true);
                        pPanel.randomGroupsButton.setSelected(true);
                        pPanel.timesField.setEnabled(true);
                        pPanel.timesField.setBackground(Color.white);
                        pPanel.timesField.setText("100");  
                         */                    
                    }  else if (getTestDesign() == TtestInitDialog.PAIRED) {
                        if (tcpmPanel.tcpPanel.pairedListModel.size() < 2) {
                            JOptionPane.showMessageDialog(null, "Need at least two pairs of samples!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            if (tcpmPanel.tcpPanel.pairedListModel.size() <= 29) {
                                pPanel.numCombsLabel.setText("There are " + (int)(Math.pow(2, tcpmPanel.tcpPanel.pairedListModel.size())) + " possible combinations                    ");
                                pPanel.randomGroupsButton.setEnabled(true);
                                pPanel.allCombsButton.setEnabled(true);
                                pPanel.timesField.setBackground(Color.white);
                                pPanel.timesField.setEnabled(true);
                            } else {
                                pPanel.numCombsLabel.setText("There are too many unique combinations                                       ");
                                pPanel.timesField.setBackground(Color.white);
                                pPanel.timesField.setEnabled(true);
                                pPanel.randomGroupsButton.setEnabled(true);
                                pPanel.randomGroupsButton.setSelected(true);                             
                            }
                        }
                    }
                }
            }
        });
        chooseDesignPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pPanel.tDistButton.setSelected(true);
                sPanel.justAlphaButton.setSelected(true);
                sPanel.maxTButton.setSelected(false);
                sPanel.maxTButton.setEnabled(false);
                sPanel.minPButton.setSelected(false);
                sPanel.minPButton.setEnabled(false);                
                pPanel.allCombsButton.setEnabled(false);
                pPanel.randomGroupsButton.setEnabled(false);
                pPanel.numCombsLabel.setForeground(Color.black);
                pPanel.numCombsLabel.setText(""); 
                pPanel.timesField.setEnabled(false);
                if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    pPanel.numCombsLabel.setForeground(Color.black);
                    pPanel.numCombsLabel.setText("");                     
                }
            }
        });
        buildConstraints(constraints, 0, 0, 1, 1, 100, 45);
        gridbag.setConstraints(chooseDesignPane, constraints);
        pane.add(chooseDesignPane);     
        
        dPanel = new DfCalcPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        gridbag.setConstraints(dPanel, constraints);  
        pane.add(dPanel);
        
        buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        
        //sPanel = new SignificancePanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 25);
        gridbag.setConstraints(sPanel, constraints);
        pane.add(sPanel);
        
        hclOpsPanel = new HCLSigOnlyPanel();
        buildConstraints(constraints, 0, 4, 1, 1, 0, 5);
        gridbag.setConstraints(hclOpsPanel, constraints);
        
        pane.add(hclOpsPanel);
        addContent(pane);
        
        EventListener listener = new EventListener();
        setActionListeners(listener);
        

        if (parentFrame == null)//CCC AMP
        	cancelButton.setEnabled(false);
       
        
    }
    
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
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
    
    class TwoClassPairedMainPanel extends JPanel {
        TwoClassPairedPanel tcpPanel;
        JButton saveButton, resetButton, loadButton;
        GridBagConstraints constraints;
        GridBagLayout gridbag;  
        JLabel lotsOfSamplesWarningLabel;
        
        public TwoClassPairedMainPanel() {
            tcpPanel = new TwoClassPairedPanel();
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(Color.white);
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();   
            this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tcpPanel, constraints);
            this.add(tcpPanel);
            
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	lotsOfSamplesWarningLabel.setBackground(Color.gray);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,1,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            GridBagLayout grid1 = new GridBagLayout();
            bottomPanel.setLayout(grid1);
            
            saveButton = new JButton("Save pairings");
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(TwoClassPairedMainPanel.this); 
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile(); 
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currentA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currentB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                out.print(currentA);
                                out.print("\t");
                                out.print(currentB);
                                out.print("\t");
                                out.println();
                            }
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            });
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 1, 1, 1, 33, 100);
            grid1.setConstraints(saveButton, constraints);
            bottomPanel.add(saveButton);       
            
            loadButton = new JButton("Load pairings");
           
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //tcpPanel.reset();
                    int returnVal = fc.showOpenDialog(TwoClassPairedMainPanel.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        tcpPanel.reset();
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String currentLine;  
                            while((currentLine = buff.readLine()) != null) {
                                StringSplitter st = new StringSplitter('\t');
                                st.init(currentLine);
                                
                                for (int i = 0; i < 2; i++) {
                                    String s = st.nextToken();
                                    if (i == 0) {
                                        tcpPanel.pairedAExpts.add(new Integer(s));
                                    } else if (i == 1) {
                                        tcpPanel.pairedBExpts.add(new Integer(s));
                                    }
                                }
                                
                            }
                            buff.close();
                            
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                String currPair = "A: " + (String)(exptNames.get(currA)) + " - B: " + (String)(exptNames.get(currB));
                                tcpPanel.exptButtons[currA].setEnabled(false);
                                tcpPanel.exptButtons[currB].setEnabled(false); 
                                tcpPanel.pairedListModel.addElement(currPair);
                                
                            }
                            if (tcpPanel.pairedAExpts.size() > 0) {
                                tcpPanel.removeABPairButton.setEnabled(true);
                                tcpPanel.pairedExptsList.setSelectedIndex(tcpPanel.pairedListModel.size() - 1);
                            }                            
                            
                            pPanel.tDistButton.setSelected(true);
                            sPanel.justAlphaButton.setSelected(true);
                            sPanel.maxTButton.setSelected(false);
                            sPanel.maxTButton.setEnabled(false);
                            sPanel.minPButton.setSelected(false);
                            sPanel.minPButton.setEnabled(false);
                            pPanel.randomGroupsButton.setEnabled(false);
                            pPanel.allCombsButton.setEnabled(false);
                            pPanel.timesField.setEnabled(false);
                            pPanel.numCombsLabel.setText("                                                                            ");
                            
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(tcpPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                    }
                }
            });
           
            buildConstraints(constraints, 1, 1, 1, 1, 33, 100);
            grid1.setConstraints(loadButton, constraints);
            bottomPanel.add(loadButton);     
            
            resetButton = new JButton("Reset");
           
            resetButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    tcpPanel.reset();
                    
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");                    
                }
            });
            
            buildConstraints(constraints, 2, 1, 1, 1, 34, 100);
            grid1.setConstraints(resetButton, constraints);
            bottomPanel.add(resetButton);    
            
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
            gridbag.setConstraints(bottomPanel, constraints);
            this.add(bottomPanel);             
        }
    }
    
    class TwoClassPairedPanel extends JPanel {
        ExperimentButton[] exptButtons;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JTextField currentATextField, currentBTextField;
        JButton removeCurrentAButton, removeCurrentBButton, loadABPairButton, removeABPairButton;
        PairedExperimentsPanel pairPanel;
        JList pairedExptsList;
        DefaultListModel pairedListModel;
        boolean currentAFilled, currentBFilled;
        int currentAExpt, currentBExpt;
        int numPanels = 0;
        Vector pairedAExpts, pairedBExpts;
        
        public TwoClassPairedPanel() {
            currentAExpt = -1;
            currentBExpt = -1;
            currentAFilled = false;
            currentBFilled = false;
            pairedAExpts = new Vector();
            pairedBExpts = new Vector();
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setLayout(gridbag);  
            
            pairedListModel = new DefaultListModel();
            pairedExptsList = new JList(pairedListModel);
            numPanels = exptNames.size()/512 + 1;
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            exptButtons = new ExperimentButton[exptNames.size()];
            
            int maxWidth = 0;
            int maxNameLength = 0;
            
            for (int i = 0; i < exptNames.size(); i++) {
                exptButtons[i] = new ExperimentButton(i);
                //set current panel
                currPanel = i / 512;
                
                if (exptButtons[i].getPreferredSize().getWidth() > maxWidth) {
                    maxWidth = (int)Math.ceil(exptButtons[i].getPreferredSize().getWidth());
                }
                
                String s = (String)(exptNames.get(i));
                int currentNameLength = s.length();
                
                if (currentNameLength > maxNameLength) {
                    maxNameLength = currentNameLength;
                }
                buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(exptButtons[i], constraints);
                panels[currPanel].add(exptButtons[i]);
            }
            
            currentATextField = new JTextField("", maxNameLength + 2);
            currentBTextField = new JTextField("", maxNameLength + 2);
            
            currentATextField.setBackground(Color.white);
            currentBTextField.setBackground(Color.white);
            currentATextField.setEditable(false);
            currentBTextField.setEditable(false);   
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);            
            
            buildConstraints(constraints, 0, 0, 1, 1, 40, 100);
            constraints.fill =GridBagConstraints.BOTH;
            gridbag.setConstraints(scroll, constraints);
            this.add(scroll);
            
            constraints.fill = GridBagConstraints.NONE;
            
            JPanel currentSelectionPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            currentSelectionPanel.setLayout(grid2);
            removeCurrentAButton = new JButton("< Remove A");
            removeCurrentBButton = new JButton("< Remove B");
            loadABPairButton = new JButton("   Load Pair >>   ");
            removeABPairButton = new JButton("<< Remove Pair");
            removeCurrentAButton.setEnabled(false);
            removeCurrentBButton.setEnabled(false);
            loadABPairButton.setEnabled(false); 
            removeABPairButton.setEnabled(false);
            removeCurrentAButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentAExpt].setEnabled(true);
                    currentAExpt = -1;
                    currentATextField.setText("");
                    currentAFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);
                }
            });
            
            removeCurrentBButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentBExpt].setEnabled(true);
                    currentBExpt = -1;
                    currentBTextField.setText("");
                    currentBFilled = false;
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);                    
                }
            });     
            
            loadABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String currentPair = "A: " + (String)(exptNames.get(currentAExpt)) + " - B: " + (String)(exptNames.get(currentBExpt));
                    pairedListModel.addElement(currentPair);
                    pairedAExpts.add(new Integer(currentAExpt));
                    pairedBExpts.add(new Integer(currentBExpt));
                    currentAExpt = -1;
                    currentBExpt = -1;
                    currentATextField.setText("");
                    currentBTextField.setText("");
                    currentAFilled = false;
                    currentBFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false); 
                    removeABPairButton.setEnabled(true);
                    pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                    
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");                    
                }
            });
            
            removeABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    int index = pairedExptsList.getSelectedIndex();
                    pairedListModel.removeElementAt(index);
                    int removedAIndex = ((Integer)(pairedAExpts.remove(index))).intValue();
                    int removedBIndex = ((Integer)(pairedBExpts.remove(index))).intValue();
                    exptButtons[removedAIndex].setEnabled(true);
                    exptButtons[removedBIndex].setEnabled(true);
                    if (pairedListModel.isEmpty()) {
                        removeABPairButton.setEnabled(false);
                    } else {
                        pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                    }
                    
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");                    
                }
            });
            JScrollPane currentAScroll = new JScrollPane(currentATextField);
            currentAScroll.setMinimumSize(new Dimension(90, 50));
            JScrollPane currentBScroll = new JScrollPane(currentBTextField);
            currentBScroll.setMinimumSize(new Dimension(90, 50));
            
            currentAScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentAScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);   
            
            currentBScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentBScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);            
            
            buildConstraints(constraints, 0, 0, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentAButton, constraints);
            currentSelectionPanel.add(removeCurrentAButton);
            
            JLabel aLabel = new JLabel(" Current A: ");
            buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
            grid2.setConstraints(aLabel, constraints);
            currentSelectionPanel.add(aLabel);    
            
            buildConstraints(constraints, 2, 0, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentAScroll, constraints);
            currentSelectionPanel.add(currentAScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 0, 1, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentBButton, constraints);
            currentSelectionPanel.add(removeCurrentBButton);   
            
            JLabel bLabel = new JLabel("Current B: ");
            buildConstraints(constraints, 1, 1, 1, 1, 20, 0);
            grid2.setConstraints(bLabel, constraints);
            currentSelectionPanel.add(bLabel);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentBScroll, constraints);
            currentSelectionPanel.add(currentBScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 1, 0, 1, 1, 10, 0);
            //constraints.fill = GridBagConstraints.HORIZONTAL;
            //constraints.ipadx = 200;
            gridbag.setConstraints(currentSelectionPanel, constraints);
            this.add(currentSelectionPanel);   
            
            constraints.fill = GridBagConstraints.NONE;
            //constraints.ipadx = 0;
            
            JPanel pairButtonsPanel = new JPanel();
            GridBagLayout grid3 = new GridBagLayout();
            pairButtonsPanel.setLayout(grid3);
            buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
            grid3.setConstraints(loadABPairButton, constraints);
            pairButtonsPanel.add(loadABPairButton);
            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            grid3.setConstraints(removeABPairButton, constraints);
            pairButtonsPanel.add(removeABPairButton);            
            
            buildConstraints(constraints, 2, 0, 1, 1, 5, 0);
            gridbag.setConstraints(pairButtonsPanel, constraints);
            this.add(pairButtonsPanel);  
            
            buildConstraints(constraints, 3, 0, 1, 1, 45, 0);
            constraints.fill = GridBagConstraints.BOTH;
            JScrollPane pairScroll = new JScrollPane(pairedExptsList);
            pairScroll.setBorder(new TitledBorder("Paired Samples"));
            gridbag.setConstraints(pairScroll, constraints);
            this.add(pairScroll);              
        }
        
        public void reset() {
            for (int i = 0; i < exptButtons.length; i++) {
                exptButtons[i].setEnabled(true);
                currentATextField.setText("");
                currentBTextField.setText("");
                removeCurrentAButton.setEnabled(false);
                removeCurrentBButton.setEnabled(false);
                loadABPairButton.setEnabled(false);
                removeABPairButton.setEnabled(false);
                pairedListModel.clear();
                currentAFilled = false;
                currentBFilled = false;
                currentAExpt = -1;
                currentBExpt = -1;
                pairedAExpts.clear();
                pairedBExpts.clear();
            }
        }
        
        class ExperimentButton extends JButton {
            String s;
            int index;
            public ExperimentButton(int i) {
                this.index = i;
                s = (String)(exptNames.get(i));
                this.setText(s);
                this.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if ((currentAFilled)&&(currentBFilled)) {
                            JOptionPane.showMessageDialog(null, "Clear at least one current field first!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (!currentAFilled) {
                            currentAExpt = index;
                            currentATextField.setText(s);
                            currentAFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentAButton.setEnabled(true);
                        } else if (!currentBFilled) {
                            currentBExpt = index;
                            currentBTextField.setText(s);
                            currentBFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentBButton.setEnabled(true);
                        }
                        
                        if ((currentAFilled) && (currentBFilled)) {
                            loadABPairButton.setEnabled(true);
                        } else {
                            loadABPairButton.setEnabled(false);
                        }
                    }
                });
            }
        }
       
        class PairedExperimentsPanel extends JPanel {
            public PairedExperimentsPanel() {
                //this.setBorder(new TitledBorder("Paired Experiments"));
            }
        }
   }    
    class OneClassPanel extends JPanel {
        JTextField meanField;
        JCheckBox[] includeExpts;
        int numPanels = 0;
        JButton saveButton, loadButton, resetButton;
        JLabel lotsOfSamplesWarningLabel;
        
        OneClassPanel() {
            this.setBackground(Color.white);
            JLabel meanLabel = new JLabel("Enter the mean value to be tested against: ");
            meanField = new JTextField("0", 7);
            includeExpts = new JCheckBox[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            numPanels = exptNames.size()/512 + 1;
             JPanel [] panels = new JPanel[numPanels];
             
             int currPanel = 0;
             for(int i = 0; i < panels.length; i++) {
                 panels[i] = new JPanel(gridbag);
             }
    
            for (int i = 0; i < exptNames.size(); i++) {
            	 //set current panel
                currPanel = i / 512;
                //JLabel expLabel = new JLabel((String)(exptNames.get(i)));
                includeExpts[i] = new JCheckBox((String)(exptNames.get(i)), true);
                buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                gridbag.setConstraints(includeExpts[i], constraints);
                panels[currPanel].add(includeExpts[i]);
            }
            JPanel bigPanel = new JPanel(new GridBagLayout());
            
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            JPanel enterMeanPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            enterMeanPanel.setLayout(grid2);                 
            
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(meanLabel, constraints);
            enterMeanPanel.add(meanLabel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(meanField, constraints);
            enterMeanPanel.add(meanField);    
            
            JScrollPane scroll2 = new JScrollPane(enterMeanPanel);
            
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, scroll2);
            split.setOneTouchExpandable(true);
            split.setDividerLocation(150);
          
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
            gridbag.setConstraints(split, constraints);
            this.add(split);  
           
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JPanel lsrPanel = new JPanel();
            loadButton = new JButton("Load settings");
            saveButton = new JButton("Save settings");
            resetButton = new JButton("Reset");
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < includeExpts.length; i++) {
                        includeExpts[i].setSelected(true);
                    }
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");                    
                }
            });
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));  
           
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(OneClassPanel.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < includeExpts.length; i++) {
                                if (includeExpts[i].isSelected()) {
                                    out.print(1);
                                } else {
                                    out.print(0);
                                }
                                if (i < includeExpts.length - 1) {
                                    out.print("\t");
                                }
                            }
                            out.println();
                            out.flush();
                            out.close();                            
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            });
           
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(OneClassPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        pPanel.tDistButton.setSelected(true);
                        sPanel.justAlphaButton.setSelected(true);
                        sPanel.maxTButton.setSelected(false);
                        sPanel.maxTButton.setEnabled(false);
                        sPanel.minPButton.setSelected(false);
                        sPanel.minPButton.setEnabled(false);
                        pPanel.randomGroupsButton.setEnabled(false);
                        pPanel.allCombsButton.setEnabled(false);
                        pPanel.timesField.setEnabled(false);
                        pPanel.numCombsLabel.setText("                                                                            ");   
                        
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);     
                            String line = buff.readLine();
                            //System.out.println(line);
                            StringSplitter st = new StringSplitter('\t');
                            st.init(line);  
                            Vector includeExptsVector = new Vector();
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                includeExptsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                            buff.close();
                            if (includeExptsVector.size() != includeExpts.length) {
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                for (int i = 0; i < includeExpts.length; i++) {
                                    int currentState = ((Integer)(includeExptsVector.get(i))).intValue();
                                    if (currentState == 0) {
                                        includeExpts[i].setSelected(false);
                                    } else if (currentState == 1) {
                                        includeExpts[i].setSelected(true);
                                    }else {
                                        for (int j = 0; j < includeExpts.length; j++) {
                                            includeExpts[j].setSelected(true);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;                                        
                                    }
                                }
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE); 
                            
                        }
                    } else {
                    }
                }
            });
           
            
            GridBagLayout grid3 = new GridBagLayout();
            lsrPanel.setLayout(grid3);
            
            buildConstraints(constraints, 0, 1, 1, 1, 33, 100);
            grid3.setConstraints(saveButton, constraints);
            lsrPanel.add(saveButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            grid3.setConstraints(loadButton, constraints);
            lsrPanel.add(loadButton);            
            
            buildConstraints(constraints, 2, 1, 1, 1, 33, 0);
            grid3.setConstraints(resetButton, constraints);
            lsrPanel.add(resetButton);            
            
            //constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
            gridbag.setConstraints(lsrPanel, constraints);
            this.add(lsrPanel);            
        }
        
        
        public void reset() {
            for (int i = 0; i < includeExpts.length; i++) {
                includeExpts[i].setSelected(true);
            }
            meanField.setText("0");          
        }
    }
   
    class GroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JLabel lotsOfSamplesWarningLabel;
        int numPanels = 0;
        JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
        GroupExperimentsPanel(Vector exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            numPanels = exptNames.size()/512 + 1;
            expLabels = new JLabel[exptNames.size()];
            groupARadioButtons = new JRadioButton[exptNames.size()];
            groupBRadioButtons = new JRadioButton[exptNames.size()];
            neitherGroupRadioButtons = new JRadioButton[exptNames.size()];
            ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            JPanel [] panels = new JPanel[numPanels];
            
            int currPanel = 0;
            for(int i = 0; i < panels.length; i++) {
                panels[i] = new JPanel(gridbag);
            }
            
            for (int i = 0; i < exptNames.size(); i++) {
            	 //set current panel
                currPanel = i / 512;
                String s1 = (String)(exptNames.get(i));//permut
                expLabels[i] = new JLabel(s1);
                expLabels[i].setForeground(Color.black);
                chooseGroup[i] = new ButtonGroup();
                groupARadioButtons[i] = new JRadioButton("Group A", true);
                //      groupARadioButtons[i].setBackground(Color.white);
                //      groupARadioButtons[i].setForeground(LABEL_COLOR);
             
                chooseGroup[i].add(groupARadioButtons[i]);
                groupBRadioButtons[i] = new JRadioButton("Group B", false);
                //groupBRadioButtons[i].setBackground(Color.white);
                //groupBRadioButtons[i].setForeground(LABEL_COLOR);
             
                chooseGroup[i].add(groupBRadioButtons[i]);
                
                neitherGroupRadioButtons[i] = new JRadioButton("Neither group", false);
                // neitherGroupRadioButtons[i].setBackground(Color.white);
                //neitherGroupRadioButtons[i].setForeground(LABEL_COLOR);
              
                chooseGroup[i].add(neitherGroupRadioButtons[i]);
                buildConstraints(constraints, 0, i%512, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(expLabels[i], constraints);
               
                panels[currPanel].add(expLabels[i]);
                
                buildConstraints(constraints, 1, i%512, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(groupARadioButtons[i], constraints);
                
                panels[currPanel].add(groupARadioButtons[i]);
                
                buildConstraints(constraints, 2, i%512, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(groupBRadioButtons[i], constraints);
              
                panels[currPanel].add(groupBRadioButtons[i]);
                
                buildConstraints(constraints, 3, i%512, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(neitherGroupRadioButtons[i], constraints);
               
                panels[currPanel].add(neitherGroupRadioButtons[i]);
 
            }
            JPanel bigPanel = new JPanel(new GridBagLayout());           
            for(int i = 0; i < numPanels; i++) {
                bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JScrollPane scroll = new JScrollPane(bigPanel);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.setBorder(BorderFactory.createLineBorder(Color.black,2));
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
        	JLabel label1 = new JLabel("                                                Note: Group A and Group B  MUST each contain more than one sample.");
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag2.setConstraints(label1, constraints);
            this.add(label1);
            
            if(lotsOfSamples){
            	lotsOfSamplesWarningLabel = new JLabel(lotsOfSamplesWarningText);
            	this.add(lotsOfSamplesWarningLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            }
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            
            JButton saveButton = new JButton(" Save grouping ");
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            saveButton.setFocusPainted(false);
            saveButton.setPreferredSize(new Dimension(100,30));
            JButton loadButton = new JButton(" Load grouping ");
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            loadButton.setFocusPainted(false);
            loadButton.setPreferredSize(new Dimension(100,30));
            JButton resetButton = new JButton(" Reset ");
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            resetButton.setFocusPainted(false);
            resetButton.setPreferredSize(new Dimension(55,30));
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                        groupARadioButtons[i].setSelected(true);
                    }
                 
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");                    
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(GroupExperimentsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < groupAssgn.length; i++) {
                                out.print(groupAssgn[i]);
                                if (i < groupAssgn.length - 1) {
                                    out.print("\t");
                                }
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
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(GroupExperimentsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String line = buff.readLine();
                            //System.out.println(line);
                            StringSplitter st = new StringSplitter('\t');
                            st.init(line);
                            Vector groupsVector = new Vector();
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                groupsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                            buff.close();
                            int[] groupAssgn = getGroupAssignments();
                            if (groupsVector.size() != groupAssgn.length) {
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                for (int i = 0; i < groupsVector.size(); i++) {
                                    int currentExpt = ((Integer)groupsVector.get(i)).intValue();
                                    if (currentExpt == 1) {
                                        groupARadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 2) {
                                        groupBRadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 3) {
                                        neitherGroupRadioButtons[i].setSelected(true);
                                    } else {
                                        for (int j = 0; j < finNum; j++) {
                                            groupARadioButtons[j].setSelected(true);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                }
                            }
                            
                            pPanel.tDistButton.setSelected(true);
                            sPanel.justAlphaButton.setSelected(true);
                            sPanel.maxTButton.setSelected(false);
                            sPanel.maxTButton.setEnabled(false);
                            sPanel.minPButton.setSelected(false);
                            sPanel.minPButton.setEnabled(false);
                            pPanel.randomGroupsButton.setEnabled(false);
                            pPanel.allCombsButton.setEnabled(false);
                            pPanel.timesField.setEnabled(false);
                            pPanel.numCombsLabel.setText("                                                                            ");                        
                        
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            //e.printStackTrace();
                        }
                        
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
                }
            });
            
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0,5,0,5);
            
            buildConstraints(constraints, 0, 1, 1, 1, 33, 100);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 1, 1, 1, 34, 0);
            gridbag3.setConstraints(resetButton, constraints);
            panel2.add(resetButton);
            
            buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            gridbag2.setConstraints(panel2, constraints);
            this.add(panel2);
            
        }
        public void reset(){
            final int finNum = exptNames.size();
            for (int i = 0; i < finNum; i++) {
                groupARadioButtons[i].setSelected(true);
            }
        }
    }
   
    class PValuePanel extends JPanel {
        JRadioButton tDistButton, permutButton, randomGroupsButton, allCombsButton;
        JLabel numCombsLabel;
        JTextField timesField, alphaInputField;
        //JButton permParamButton;
        
        PValuePanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "P-Value Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            this.setLayout(gridbag);
            
            //permParamButton = new JButton("Permutation parameters");
            //permParamButton.setEnabled(false);
            
            ButtonGroup chooseP = new ButtonGroup();
            
            tDistButton = new JRadioButton("p-values based on t-distribution", true);
            tDistButton.setFocusPainted(false);
            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
            tDistButton.setBackground(Color.white);
            /*
            tDistButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == tDistButton) {
                        permParamButton.setEnabled(false);
                    }
                }
            });
             */
            chooseP.add(tDistButton);
            
            permutButton = new JRadioButton("p-values based on permutation:  ", false);
            permutButton.setFocusPainted(false);
            permutButton.setForeground(UIManager.getColor("Label.foreground"));
            permutButton.setBackground(Color.white);
            /*
            permutButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == permutButton) {
                int[] grpAssignments = getGroupAssignments();
                    int grpACounter = 0;
                    int grpBCounter = 0;
                    for (int i = 0; i < grpAssignments.length; i++) {
                        if (grpAssignments[i] == GROUP_A) {
                            grpACounter++;
                        } else if (grpAssignments[i] == GROUP_B) {
                            grpBCounter++;
                        }
                    }
                    if ((grpACounter < 2) || (grpBCounter < 2)) {
                        JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
             
                        //if (pPanel.permutButton.isSelected()) {
                            //System.out.println("grpACounter = " + grpACounter + ", grpBCounter = " + grpBCounter);
                            int numCombs = 0;
                            tooMany = true;
                            if ((grpACounter + grpBCounter) <= 20) {
                                numCombs = getNumCombs((grpACounter + grpBCounter), grpACounter);
                                tooMany = false;
                            }
                    }
                }
            });
             */
            
            //permutButton.setEnabled(false);
            chooseP.add(permutButton);
            /*
            buildConstraints(constraints, 0, 0, 1, 1, 0, 20);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
             */
            
            /*
            JLabel line1 = new JLabel("___________________________________________________________________________________________________");
            buildConstraints(constraints, 0, 1, 4, 1, 100, 5);
            gridbag.setConstraints(line1, constraints);
            this.add(line1);
             */
            
            
               /*
            buildConstraints(constraints, 0, 2, 1, 1, 0, 15);
            //constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(permutButton, constraints);
            this.add(permutButton);
                */
            
            /*
            buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(permParamButton, constraints);
            this.add(permParamButton);
             */
            
            //constraints.anchor = GridBagConstraints.CENTER;
            
            numCombsLabel = new JLabel("                                       ");
            numCombsLabel.setOpaque(false);
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
            gridbag.setConstraints(numCombsLabel, constraints);
            this.add(numCombsLabel);
             */
            
            JPanel panel1 = new JPanel();
            panel1.setBackground(Color.white);
            GridBagLayout grid1 = new GridBagLayout();
            panel1.setLayout(grid1);
            
            buildConstraints(constraints, 0, 0, 1, 1, 30, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(tDistButton, constraints);
            panel1.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
            constraints.anchor = GridBagConstraints.WEST;
            grid1.setConstraints(permutButton, constraints);
            panel1.add(permutButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 70, 0);
            constraints.ipadx = 50;
            constraints.anchor = GridBagConstraints.WEST;
            grid1.setConstraints(numCombsLabel, constraints);
            panel1.add(numCombsLabel);
            
            constraints.ipadx = 100;
            //constraints.ipady = 100;
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 20);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(panel1, constraints);
            this.add(panel1);
            
            //DONE UPTO HERE -- 9/18/02
            
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.ipadx = 0;
            constraints.ipady = 0;
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout grid2 = new GridBagLayout();
            panel2.setLayout(grid2);
            panel2.setBorder(new EtchedBorder());
            
            
            randomGroupsButton = new JRadioButton("Randomly group samples ", true);
            randomGroupsButton.setFocusPainted(false);
            randomGroupsButton.setForeground(UIManager.getColor("Label.foreground"));
            randomGroupsButton.setBackground(Color.white);
            randomGroupsButton.setEnabled(false);
            
            allCombsButton = new JRadioButton("Use all permutations                 ", false);
            allCombsButton.setFocusPainted(false);
            allCombsButton.setForeground(UIManager.getColor("Label.foreground"));
            allCombsButton.setBackground(Color.white);
            allCombsButton.setEnabled(false);
            
            
            ButtonGroup chooseAllOrRandom = new ButtonGroup();
            
            buildConstraints(constraints, 0, 0, 1, 1, 60, 50);
            grid2.setConstraints(randomGroupsButton, constraints);
            panel2.add(randomGroupsButton);
            
            timesField = new JTextField("100", 7);
            timesField.setEnabled(false);
            buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(timesField, constraints);
            panel2.add(timesField);
            
            JLabel timesLabel = new JLabel("times");
            timesLabel.setOpaque(false);
            buildConstraints(constraints, 2, 0, 1, 1, 20, 0);
            grid2.setConstraints(timesLabel, constraints);
            panel2.add(timesLabel);
            
            buildConstraints(constraints, 0, 1, 1, 1, 60, 0);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(allCombsButton, constraints);
            panel2.add(allCombsButton);
            
            chooseAllOrRandom.add(randomGroupsButton);
            chooseAllOrRandom.add(allCombsButton);
            
            allCombsButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == allCombsButton) {
                        timesField.setText("");
                        timesField.setBackground(Color.gray);
                        timesField.setEnabled(false);
                    }
                }
            });
            
            randomGroupsButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == randomGroupsButton) {
                        timesField.setText("100");
                        timesField.setBackground(Color.white);
                        timesField.setEnabled(true);
                    }
                }
            });
            
            tDistButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() ==tDistButton) {
                        randomGroupsButton.setEnabled(false);
                        allCombsButton.setEnabled(false);
                        timesField.setEnabled(false);
                        numCombsLabel.setText("                                                                            ");
                    }
                }
            });
            
            buildConstraints(constraints, 0, 1, 1, 1, 100, 60);
            constraints.anchor = GridBagConstraints.NORTH;
            constraints.fill = GridBagConstraints.VERTICAL;
            gridbag.setConstraints(panel2, constraints);
            this.add(panel2);
            /*
            JLabel line2 = new JLabel("___________________________________________________________________________________________________");
            buildConstraints(constraints, 0, 5, 4, 1, 100, 5);
            gridbag.setConstraints(line2, constraints);
            this.add(line2);
             */
            
            constraints.fill = GridBagConstraints.NONE;
            
            JPanel panel3 = new JPanel();
            panel3.setBackground(Color.white);
            GridBagLayout grid3 = new GridBagLayout();
            panel3.setLayout(grid3);
            
            JLabel alphaLabel = new JLabel("Overall alpha (critical p-value):                   ");
            alphaLabel.setOpaque(false);
            buildConstraints(constraints, 0, 0, 1, 1, 75, 100);
            constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            grid3.setConstraints(alphaLabel, constraints);
            panel3.add(alphaLabel);
            
            alphaInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(alphaInputField, constraints);
            panel3.add(alphaInputField);
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
            gridbag.setConstraints(panel3, constraints);
            this.add(panel3);
            
            /*
            JButton pButton = new JButton("pValuePanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(pButton, constraints);
            this.add(pButton);
             */        
        }
    }
    
    class SignificancePanel extends JPanel {
        JRadioButton minPButton, maxTButton, justAlphaButton, stdBonfButton, adjBonfButton, falseNumButton, falsePropButton, fastFDRButton, slowFDRButton;
        JTextField falseNumField, falsePropField;
        JCheckBox calcFDRPVals;
        SignificancePanel() {
            //      this.setBorder(new TitledBorder(new EtchedBorder(), "Significance based on: "));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "p-value / false discovery corrections", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            /*
            JLabel sigLabel = new JLabel("Significance based on: ");
            buildConstraints(constraints, 0, 0, 3, 1, 0, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sigLabel, constraints);
            this.add(sigLabel);
             */
            
            ButtonGroup sigGroup = new ButtonGroup();
            
            justAlphaButton = new JRadioButton("just alpha (no correction)", true);
            justAlphaButton.setFocusPainted(false);
            justAlphaButton.setForeground(UIManager.getColor("Label.foreground"));
            justAlphaButton.setBackground(Color.white);
            sigGroup.add(justAlphaButton);
            
            stdBonfButton = new JRadioButton("standard Bonferroni correction", false);
            stdBonfButton.setFocusPainted(false);
            stdBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            stdBonfButton.setBackground(Color.white);
            sigGroup.add(stdBonfButton);
            
            adjBonfButton = new JRadioButton("adjusted Bonferroni correction", false);
            adjBonfButton.setFocusPainted(false);
            adjBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            adjBonfButton.setBackground(Color.white);
            sigGroup.add(adjBonfButton);
            
            minPButton = new JRadioButton("minP", false);
            minPButton.setEnabled(false);
            minPButton.setFocusPainted(false);
            minPButton.setForeground(UIManager.getColor("Label.foreground"));
            minPButton.setBackground(Color.white);
            sigGroup.add(minPButton);            
            
            maxTButton = new JRadioButton("maxT", false);
            maxTButton.setEnabled(false);
            maxTButton.setFocusPainted(false);
            maxTButton.setForeground(UIManager.getColor("Label.foreground"));
            maxTButton.setBackground(Color.white);
            sigGroup.add(maxTButton);            
            
            falseNumButton = new JRadioButton("EITHER, The number of false significant genes should not exceed", false);
            falseNumButton.setEnabled(false);
            //falseNumButton.setEnabled(false);
            falseNumButton.setFocusPainted(false);
            falseNumButton.setForeground(UIManager.getColor("Label.foreground"));
            falseNumButton.setBackground(Color.white);
            sigGroup.add(falseNumButton);            
            
            falsePropButton = new JRadioButton("OR, The proportion of false significant genes should not exceed", false);
            falsePropButton.setEnabled(false);
            //falsePropButton.setEnabled(false);
            falsePropButton.setFocusPainted(false);
            falsePropButton.setForeground(UIManager.getColor("Label.foreground"));
            falsePropButton.setBackground(Color.white);
            sigGroup.add(falsePropButton);            
            
            falseNumField = new JTextField(10);
            falseNumField.setText("10");
            falseNumField.setEnabled(false);
            falsePropField = new JTextField(10);
            falsePropField.setText("0.05");
            falsePropField.setEnabled(false);
            calcFDRPVals = new JCheckBox("Calculate adjusted p values for false discovery control", false);
            calcFDRPVals.setEnabled(false);
            calcFDRPVals.setBackground(Color.white);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 33);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);
            
            JPanel westfallYoungPanel = new JPanel();
            westfallYoungPanel.setBackground(Color.white);
            westfallYoungPanel.setBorder(new EtchedBorder());
            GridBagLayout grid2 = new GridBagLayout(); 
            westfallYoungPanel.setLayout(grid2);
 
            JLabel stepDownLabel = new JLabel("Step-down Westfall and Young methods (for permutations only): ");
            buildConstraints(constraints, 0, 0, 1, 1, 34, 100);
            //buildConstraints(constraints, 0, 1, 1, 1, 34, 50);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.EAST;
            //gridbag.setConstraints(stepDownLabel, constraints);
            grid2.setConstraints(stepDownLabel, constraints);
            //this.add(stepDownLabel);  
            westfallYoungPanel.add(stepDownLabel);
            
            //buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(minPButton, constraints);
            grid2.setConstraints(minPButton, constraints);
            //this.add(minPButton); 
            westfallYoungPanel.add(minPButton);
            
            
            //buildConstraints(constraints, 2, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(maxTButton, constraints);
            grid2.setConstraints(maxTButton, constraints);
            //this.add(maxTButton);  
            westfallYoungPanel.add(maxTButton);
            
            buildConstraints(constraints, 0, 1, 3, 1, 100, 33);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(westfallYoungPanel, constraints);
            this.add(westfallYoungPanel);
            
            ButtonGroup fastOrSlow = new ButtonGroup();
            fastFDRButton = new JRadioButton("Fast approximation (but possibly conservative)", true);
            fastFDRButton.setSelected(true);
            fastFDRButton.setEnabled(false);
            fastFDRButton.setBackground(Color.white);
            fastOrSlow.add(fastFDRButton);
            slowFDRButton = new JRadioButton("Complete computation (possibly slow)");
            slowFDRButton.setEnabled(false);
            slowFDRButton.setBackground(Color.white);
            fastOrSlow.add(slowFDRButton);
            
            JPanel FDRPanel = new JPanel();
            FDRPanel.setBackground(Color.white);
            FDRPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control (permutations only)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout grid3 = new GridBagLayout(); 
            FDRPanel.setLayout(grid3); 
            
            JLabel FDRLabel = new JLabel("With confidence of [1 - alpha] : ");            
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 0, 2, 1, 100, 20);
            grid3.setConstraints(FDRLabel, constraints);
            FDRPanel.add(FDRLabel);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 20);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falseNumButton, constraints);
            FDRPanel.add(falseNumButton);       
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falseNumField, constraints);
            FDRPanel.add(falseNumField);    
            
            buildConstraints(constraints, 0, 2, 1, 1, 50, 20);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falsePropButton, constraints);
            FDRPanel.add(falsePropButton);    
            
            buildConstraints(constraints, 1, 2, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falsePropField, constraints);
            FDRPanel.add(falsePropField);    
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 3, 1, 1, 50, 20);
            grid3.setConstraints(fastFDRButton, constraints);
            FDRPanel.add(fastFDRButton);    
            
            buildConstraints(constraints, 1, 3, 1, 1, 50, 0);
            grid3.setConstraints(slowFDRButton, constraints);
            FDRPanel.add(slowFDRButton);              
            
            buildConstraints(constraints, 0, 4, 2, 1, 100, 20);
            grid3.setConstraints(calcFDRPVals, constraints);
            FDRPanel.add(calcFDRPVals);     
            
            buildConstraints(constraints, 0, 2, 3, 1, 100, 34);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(FDRPanel, constraints);
            this.add(FDRPanel); 
            
            constraints.fill = GridBagConstraints.NONE;
            /*
            JButton sButton = new JButton("significancePanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sButton, constraints);
            this.add(sButton);
             */
            
        }
    }
    
    class OkCancelPanel extends JPanel {
        JButton okButton, cancelButton;
        JCheckBox drawTreesBox;
        OkCancelPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "General"));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            drawTreesBox = new JCheckBox("Draw hierarchical trees", false);
            buildConstraints(constraints, 0, 0, 2, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(drawTreesBox, constraints);
            this.add(drawTreesBox);
            
            okButton = new JButton("OK");
            buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(okButton, constraints);
            this.add(okButton);
            
            cancelButton = new JButton("Cancel");
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(cancelButton, constraints);
            this.add(cancelButton);
            
            /*
            JButton oButton = new JButton("OkCancelPanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(oButton, constraints);
            this.add(oButton);
             */
        }
    }
    
    class DfCalcPanel extends JPanel {
        JRadioButton welchButton, eqVarButton;
        DfCalcPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Variance assumption (for between subjects t-test only)"));  
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            welchButton = new JRadioButton("Welch approximation (unequal group variances)", true);
            welchButton.setBackground(Color.white);
            eqVarButton = new JRadioButton("Assume equal group variances", false);
            eqVarButton.setBackground(Color.white);
            
            ButtonGroup group = new ButtonGroup();
            group.add(welchButton);
            group.add(eqVarButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(welchButton, constraints);
            this.add(welchButton);    
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(eqVarButton, constraints);
            this.add(eqVarButton);            
        }
        
        public void reset() {
            welchButton.setSelected(true);
        }
    }
    
    private long factorial(int n) {
        if ((n==1) || (n == 0)) {
            return 1;
        }
        else {
            return factorial(n-1) * n;
        }
    }
    
    private int getNumCombs(int n, int k) { // nCk
        
        /*
        System.out.println("n = " + n);
        System.out.println("k = " + k);
        System.out.println("Numerator: factorial(n) = " + factorial(n));
        System.out.println("factorial(k) = " + factorial(k));
        System.out.println("factorial(n-k) = " + factorial(n-k));
         */
        return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
    }
    
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public boolean isDrawTrees() {
        return hclOpsPanel.isHCLSelected();
    }
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }
    
    public int getTestDesign() {
        int design = -1;
        if (chooseDesignPane.getSelectedIndex() == 0) {
            design = TtestInitDialog.ONE_CLASS;
        } else if (chooseDesignPane.getSelectedIndex() == 1){
            design = TtestInitDialog.BETWEEN_SUBJECTS;
        } else if (chooseDesignPane.getSelectedIndex() == 2) {
            design = TtestInitDialog.PAIRED;
        }
        return design;
    }
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        for (int i = 0; i < exptNames.size(); i++) {
            if (gPanel.groupARadioButtons[i].isSelected()) {
                groupAssignments[i] = GROUP_A;
            } else if (gPanel.groupBRadioButtons[i].isSelected()) {
                groupAssignments[i] = GROUP_B;
            } else {
                groupAssignments[i] = NEITHER_GROUP;
            }
        }
        
        return groupAssignments;
    }
    
    public boolean isPermut() {
        return pPanel.permutButton.isSelected();
    }
    
    public int getUserNumCombs() {
        String s1 = pPanel.timesField.getText();
        int num = 0;
        if (!useAllCombs()) {
            num = Integer.parseInt(s1);
        } else {
            if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                num = allPossCombs;
            } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                num = (int)Math.pow(2, getNumValidOneClassExpts());
            } else if (getTestDesign() == TtestInitDialog.PAIRED) {
                num = (int)(Math.pow(2, tcpmPanel.tcpPanel.pairedListModel.size()));
            }
        }
        return num;
    }
    
    public double getAlphaValue() {
        String s1 = pPanel.alphaInputField.getText();
        
        return Double.parseDouble(s1);
    }
    
    public int[] getOneClassAssignments() {
        int[] oneClassAssignments = new int[oPanel.includeExpts.length];
       
        for (int i = 0; i < oneClassAssignments.length; i++) {
            if (oPanel.includeExpts[i].isSelected()) {
                oneClassAssignments[i] = 1;
            } else {
                oneClassAssignments[i] = 0;
            }
        }
        
        return oneClassAssignments;
    }
    
    public int getNumValidOneClassExpts() {
        int validNum = 0;
        int[] oca = getOneClassAssignments();
        
        for (int i =0; i < oca.length; i++) {
            if (oca[i] == 1) {
                validNum++;
            }
        }
        
        return validNum;
    }
    
    public double getOneClassMean() {
        return Double.parseDouble(oPanel.meanField.getText());
    }
    
    public int getFalseNum() {
        return Integer.parseInt(sPanel.falseNumField.getText());
    }
    
    public double getFalseProp() {
        return Double.parseDouble(sPanel.falsePropField.getText());
    }
    
    public boolean validateAlpha(String alpha){
        float a;
        try{
            a = Float.parseFloat(alpha);
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Alpha value is not a valid input value.", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.alphaInputField.requestFocus();
            pPanel.alphaInputField.selectAll();
            return false;
        }
        if(a <= 0 || a >= 1){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Alpha value must be > 0 and < 1.0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.alphaInputField.requestFocus();
            pPanel.alphaInputField.selectAll();
            return false;
        }
        return true;
    }
    
    public boolean validateFalseNum() {
        int a;
        try {
            String falseNum = sPanel.falseNumField.getText();
            a = Integer.parseInt(falseNum);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(TtestInitDialog.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            sPanel.falseNumField.requestFocus();
            sPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(TtestInitDialog.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            sPanel.falseNumField.requestFocus();
            sPanel.falseNumField.selectAll();
            return false;          
        }
        return true;
    }
    
    public boolean validateFalseProp() {
        float a;
        try {
            String falseProp = sPanel.falsePropField.getText();
            a = Float.parseFloat(falseProp);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(TtestInitDialog.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            sPanel.falsePropField.requestFocus();
            sPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(TtestInitDialog.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            sPanel.falsePropField.requestFocus();
            sPanel.falsePropField.selectAll();
            return false;          
        }
        return true;
    }    
    
    public boolean validatePermutations(String n){
        int i;
        try {
            i = Integer.parseInt(n);
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Number of Permutations is not a valid input value.", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.timesField.requestFocus();
            pPanel.timesField.selectAll();
            return false;
        }
        if(i <= 0){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Number of Permutations must be > 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.timesField.requestFocus();
            pPanel.timesField.selectAll();
            return false;
        }
        return true;
    }
    
    
    public class EventListener implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            
            String command = ae.getActionCommand();
            
            if(command.equals("ok-command")){
                if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                    int[] grpAssignments = getGroupAssignments();
                    int grpACounter = 0;
                    int grpBCounter = 0;
                    for (int i = 0; i < grpAssignments.length; i++) {
                        if (grpAssignments[i] == GROUP_A) {
                            grpACounter++;
                        } else if (grpAssignments[i] == GROUP_B) {
                            grpBCounter++;
                        }
                    }
                    if ((grpACounter < 2) || (grpBCounter < 2)) {
                        JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one sample", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String alpha = pPanel.alphaInputField.getText();
                        float a;
                        if(pPanel.permutButton.isSelected() && pPanel.randomGroupsButton.isSelected()){
                            String iter = pPanel.timesField.getText();
                            if(!validatePermutations(iter)){
                                okPressed = false;
                                return;                            
                            }
                        }                    
                        if(!validateAlpha(alpha)){
                        okPressed = false;
                            return;
                        } 
                        if (sPanel.falseNumButton.isSelected()) {                           
                            if (!validateFalseNum()) {
                                okPressed = false; 
                                return;
                            }
                        }
                        if (sPanel.falsePropButton.isSelected()) {
                            if (!validateFalseProp()) {
                                okPressed = false;
                                return;
                            }
                        }
                        okPressed = true;
                        hide();
                        dispose();
                    }
                } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    try {
                        float mean = Float.parseFloat(oPanel.meanField.getText());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(oPanel, "Invalid value entered for mean", "Error", JOptionPane.WARNING_MESSAGE);
                        okPressed = false;
                        return;
                    }
                    
                    if (getNumValidOneClassExpts() < 2) {
                        JOptionPane.showMessageDialog(oPanel, "Select at least two samples", "Error", JOptionPane.WARNING_MESSAGE);
                        okPressed = false;
                        return;      
                    }
                    
                    String alpha = pPanel.alphaInputField.getText();
                    
                    float a;
                    if(pPanel.permutButton.isSelected() && pPanel.randomGroupsButton.isSelected()){
                        String iter = pPanel.timesField.getText();
                        if(!validatePermutations(iter)){
                            okPressed = false;
                            return;
                        }
                    }
                    if(!validateAlpha(alpha)){
                        okPressed = false;
                        return;
                    } 
                    
                    if (sPanel.falseNumButton.isSelected()) {
                        if (!validateFalseNum()) {
                            okPressed = false;
                            return;
                        }
                    }
                    if (sPanel.falsePropButton.isSelected()) {
                        if (!validateFalseProp()) {
                            okPressed = false;
                            return;
                        }
                    }                   
                    okPressed = true;
                    hide();
                    dispose();                    
                } else if (getTestDesign() == TtestInitDialog.PAIRED) {
                    if (tcpmPanel.tcpPanel.pairedListModel.size() < 2) {
                        JOptionPane.showMessageDialog(null, "Need at least two pairs of samples!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (tcpmPanel.tcpPanel.pairedListModel.size() <= 29) {
                            int numCombs = getUserNumCombs();
                            int numUniquePerms = (int)(Math.pow(2, tcpmPanel.tcpPanel.pairedListModel.size()));
                            //SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                            //sapDialog.setVisible(true);
                           //allUniquePermsUsed = sapDialog.useAllPerms();                            
                        }
                        
                    
                    String alpha = pPanel.alphaInputField.getText();
                    
                    float a;
                    if(pPanel.permutButton.isSelected() && pPanel.randomGroupsButton.isSelected()){
                        String iter = pPanel.timesField.getText();
                        if(!validatePermutations(iter)){
                            okPressed = false;
                            return;
                        }
                    }
                    if(!validateAlpha(alpha)){
                        okPressed = false;
                        return;
                    }    
                    
                    if (sPanel.falseNumButton.isSelected()) {
                        if (!validateFalseNum()) {
                            okPressed = false;
                            return;
                        }
                    }
                    if (sPanel.falsePropButton.isSelected()) {
                        if (!validateFalseProp()) {
                            okPressed = false;
                            return;
                        }
                    }                    
                        
                    okPressed = true;
                    javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                    dispose();
                    }
                    
                }
            } else if(command.equals("reset-command")){
                if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                    gPanel.reset();
                } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    oPanel.reset();
                } else if (getTestDesign() == TtestInitDialog.PAIRED) {
                    tcpmPanel.tcpPanel.reset();
                }
                pPanel.tDistButton.setSelected(true);
                pPanel.randomGroupsButton.setEnabled(false);
                pPanel.allCombsButton.setEnabled(false);
                pPanel.timesField.setEnabled(false);
                pPanel.timesField.setBackground(Color.white);
                pPanel.timesField.setText("100");
                pPanel.numCombsLabel.setText("                                                                            ");
                pPanel.alphaInputField.setText("0.01");
                sPanel.justAlphaButton.setSelected(true);
                sPanel.falseNumField.setText("10");
                sPanel.falsePropField.setText("0.05");
                hclOpsPanel.setHCLSelected(false);
                dPanel.reset();
            } else if(command.equals("cancel-command")){
                okPressed = false;
                setVisible(false);
                dispose();
            } else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(TtestInitDialog.this, "TTEST Initialization Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 600);
                    helpWindow.setLocation();
                    helpWindow.show();
                } else{
                    helpWindow.dispose();
                }
            }
        }
    }
    
    
    public Vector getPairedAExpts() {
        return tcpmPanel.tcpPanel.pairedAExpts;
    }
    
    public Vector getPairedBExpts() {
        return tcpmPanel.tcpPanel.pairedBExpts;
    }    
    
    public int getSignificanceMethod() {
        if (sPanel.justAlphaButton.isSelected()) {
            return TtestInitDialog.JUST_ALPHA;
        } else if (sPanel.stdBonfButton.isSelected()) {
            return TtestInitDialog.STD_BONFERRONI;
        } else if (sPanel.adjBonfButton.isSelected()){
            return TtestInitDialog.ADJ_BONFERRONI;
        } else if (sPanel.maxTButton.isSelected()) {
            return TtestInitDialog.MAX_T;
        } else if (sPanel.minPButton.isSelected()){
            return TtestInitDialog.MIN_P;
        } else if (sPanel.falseNumButton.isSelected()) {
            return TtestInitDialog.FALSE_NUM;
        } else if (sPanel.falsePropButton.isSelected()) {
            return TtestInitDialog.FALSE_PROP;
        } else {
            return -1;
        }
    }
    
    public boolean calculateFDRPVals() {
        return sPanel.calcFDRPVals.isSelected();
    }
    
    public boolean doFastFDRApprox() {
        return sPanel.fastFDRButton.isSelected();
    }
    
    public boolean useWelchDf() {
        return dPanel.welchButton.isSelected();
    }
    
    public boolean useAllCombs() {
        return pPanel.allCombsButton.isSelected();
    }
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector nameVector = new Vector();
        
        for (int i = 0; i < 10; i++) {
            nameVector.add("Exp " + i);
        }
        TtestInitDialog  tDialog= new TtestInitDialog(dummyFrame, true, nameVector);
        for (int i = 0; i < 50; i++) {
            System.out.println("2^" + i + " = " + (int)Math.pow(2, i));
        }
        
        tDialog.setVisible(true);
        
        System.exit(0);
    }    
}
