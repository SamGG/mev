package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class PCASelectionAreaDialog extends AlgorithmDialog {
    private int result;
    private Content3D content;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    
    private float positionX;
    private float positionY;
    private float positionZ;
    private float sizeX;
    private float sizeY;
    private float sizeZ;
    
    private int initSizePos;
    private float initSize;
    
    private final float maxPos;
    
    private JSlider posXSlider, posYSlider, posZSlider, sizeXSlider, sizeYSlider, sizeZSlider;
    
    /**
     * Constructs a <code>PCASelectionAreaDialog</code> with specified initial parameters.
     */
    public PCASelectionAreaDialog(Frame parent, float positionX, float positionY, float positionZ,
    float sizeX, float sizeY, float sizeZ) {
        super(parent, "PCA selection area configuration", true);
        
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        
        maxPos = positionX;
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel parameters = new JPanel(new GridLayout(0, 2, 10, 0));
        parameters.setBorder(new EmptyBorder(20, 20, 20, 10));
        parameters.setBackground(Color.white);
        
        parameters.add(new JLabel("Position X  "));
        textField1 = new JTextField(Float.toString(positionX), 5);
        parameters.add(textField1, BorderLayout.EAST);
        
        parameters.add(new JLabel("Position Y  "));
        textField2 = new JTextField(Float.toString(positionY), 5);
        parameters.add(textField2, BorderLayout.EAST);
        
        parameters.add(new JLabel("Position Z  "));
        textField3 = new JTextField(Float.toString(positionZ), 5);
        parameters.add(textField3, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size X  "));
        textField4 = new JTextField(Float.toString(sizeX), 5);
        parameters.add(textField4, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size Y "));
        textField5 = new JTextField(Float.toString(sizeY), 5);
        parameters.add(textField5, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size Z "));
        textField6 = new JTextField(Float.toString(sizeZ), 5);
        parameters.add(textField6, BorderLayout.EAST);
        
        
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.setForeground(Color.white);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
        panel3.setBackground(Color.white);
        panel3.add(parameters, BorderLayout.WEST);
        panel3.add(new JLabel(GUIFactory.getIcon("dialog_button_bar.gif")), BorderLayout.EAST);
        
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(panel3, BorderLayout.CENTER);
        addContent(panel1);
        setActionListeners(listener);
        pack();
        setResizable(false);
    }
    
    public PCASelectionAreaDialog(Content3D content, Frame parent, float positionX, float positionY, float positionZ,
    float sizeX, float sizeY, float sizeZ, float maxPosition) {
        super(parent, "PCA selection area configuration", false);
        
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.content = content;
        this.maxPos = maxPosition;
        //this.maxPos = (float)(maxPos + 0.05f*maxPos); // make the bounds a bit larger than the actual extent of the graph
        
        posXSlider = new JSlider(-1000, 1000, 0);
        posYSlider = new JSlider(-1000, 1000, 0);
        posZSlider = new JSlider(-1000, 1000, 0);
        
        initSizePos = getSliderPos(sizeX);
        initSize = sizeX;
        sizeXSlider = new JSlider(0, 2000, initSizePos);
        sizeYSlider = new JSlider(0, 2000, initSizePos);
        sizeZSlider = new JSlider(0, 2000, initSizePos);
        
        posXSlider.setBackground(Color.white);
        posYSlider.setBackground(Color.white);
        posZSlider.setBackground(Color.white);
        sizeXSlider.setBackground(Color.white);
        sizeYSlider.setBackground(Color.white);
        sizeZSlider.setBackground(Color.white);
        
        textField1 = new JTextField(Float.toString(positionX), 5);
        textField2 = new JTextField(Float.toString(positionY), 5);
        textField3 = new JTextField(Float.toString(positionZ), 5);
        textField4 = new JTextField(Float.toString(sizeX), 5);
        textField5 = new JTextField(Float.toString(sizeY), 5);
        textField6 = new JTextField(Float.toString(sizeY), 5);
        
        textField1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField1.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posXSlider.setValue(1000);
                        textField1.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posXSlider.setValue(-1000);
                        textField1.setText("" + (float)(-1f*maxPos));
                    } else {
                        posXSlider.setValue(sliderValue);
                        textField1.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posXSlider.setValue(0);
                    textField1.setText("0.0");
                    setPositionX(0f);
                    updateContent();
                }
                
                String posString = textField1.getText();
                float posX = Float.parseFloat(posString);
                setPositionX(posX);
                updateContent();
            }
        });
        
        textField1.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                int sliderValue;
                try {
                    String s = textField1.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posXSlider.setValue(1000);
                        textField1.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posXSlider.setValue(-1000);
                        textField1.setText("" + (float)(-1f*maxPos));
                    } else {
                        posXSlider.setValue(sliderValue);
                        textField1.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posXSlider.setValue(0);
                    textField1.setText("0.0");
                    setPositionX(0f);
                    updateContent();
                }
                
                String posString = textField1.getText();
                float posX = Float.parseFloat(posString);
                setPositionX(posX);
                updateContent();                
            }
            
                public void focusGained(FocusEvent e) {
		}            
        });
        
        posXSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);
                if (value == -1000) {
                    displayValue = (float)((-1f)*maxPos);
                }
                if (value == 1000) {
                    displayValue = maxPos;
                }
                textField1.setText("" + displayValue);
                String dString = textField1.getText();
                float pos = Float.parseFloat(dString);
                setPositionX(pos);
                updateContent();
            }
        });
        
        
        textField2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField2.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posYSlider.setValue(1000);
                        textField2.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posYSlider.setValue(-1000);
                        textField2.setText("" + (float)(-1f*maxPos));
                    } else {
                        posYSlider.setValue(sliderValue);
                        textField2.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posYSlider.setValue(0);
                    textField2.setText("0.0");
                    setPositionY(0f);
                    updateContent();
                }
                
                String posString = textField2.getText();
                float pos = Float.parseFloat(posString);
                setPositionY(pos);
                updateContent();
            }
        });       
        
        textField2.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                int sliderValue;
                try {
                    String s = textField2.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posYSlider.setValue(1000);
                        textField2.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posYSlider.setValue(-1000);
                        textField2.setText("" + (float)(-1f*maxPos));
                    } else {
                        posYSlider.setValue(sliderValue);
                        textField2.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posYSlider.setValue(0);
                    textField2.setText("0.0");
                    setPositionY(0f);
                    updateContent();
                }
                
                String posString = textField2.getText();
                float pos = Float.parseFloat(posString);
                setPositionY(pos);
                updateContent();                
            }
            
            public void focusGained(FocusEvent e) {
            }          
        });
        
        posYSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);
                if (value == -1000) {
                    displayValue = (float)((-1f)*maxPos);
                }
                if (value == 1000) {
                    displayValue = maxPos;
                }
                textField2.setText("" + displayValue);
                String dString = textField2.getText();
                float pos = Float.parseFloat(dString);
                setPositionY(pos);
                updateContent();
            }
        });
        
        
        textField3.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField3.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posZSlider.setValue(1000);
                        textField3.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posZSlider.setValue(-1000);
                        textField3.setText("" + (float)(-1f*maxPos));
                    } else {
                        posZSlider.setValue(sliderValue);
                        textField3.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posZSlider.setValue(0);
                    textField3.setText("0.0");
                    setPositionZ(0f);
                    updateContent();
                }
                
                String posString = textField3.getText();
                float pos = Float.parseFloat(posString);
                setPositionZ(pos);
                updateContent();
            }
        });     
        
        textField3.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                int sliderValue;
                try {
                    String s = textField3.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 1000) {
                        posZSlider.setValue(1000);
                        textField3.setText("" + (float)(maxPos));
                    }
                    else if (sliderValue <= -1000) {
                        posZSlider.setValue(-1000);
                        textField3.setText("" + (float)(-1f*maxPos));
                    } else {
                        posZSlider.setValue(sliderValue);
                        textField3.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    posZSlider.setValue(0);
                    textField3.setText("0.0");
                    setPositionZ(0f);
                    updateContent();
                }
                
                String posString = textField3.getText();
                float pos = Float.parseFloat(posString);
                setPositionZ(pos);
                updateContent();                
            }
            
            public void focusGained(FocusEvent e) {
            }          
        });            
            
        posZSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);
                if (value == -1000) {
                    displayValue = (float)((-1f)*maxPos);
                }
                if (value == 1000) {
                    displayValue = maxPos;
                }
                textField3.setText("" + displayValue);
                String dString = textField3.getText();
                float pos = Float.parseFloat(dString);
                setPositionZ(pos);
                updateContent();
            }
        });     
        
        
        textField4.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField4.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeXSlider.setValue(2000);
                        textField4.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeXSlider.setValue(0);
                        textField4.setText("" + 0.0f);
                    } else {
                        sizeXSlider.setValue(sliderValue);
                        textField4.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeXSlider.setValue(initSizePos);
                    textField4.setText("" + initSize);
                    setSizeX(initSize);
                    updateContent();
                }
                
                String sizeString = textField4.getText();
                float size = Float.parseFloat(sizeString);
                setSizeX(size);
                updateContent();
            }
        });   
        
        textField4.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                int sliderValue;
                try {
                    String s = textField4.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeXSlider.setValue(2000);
                        textField4.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeXSlider.setValue(0);
                        textField4.setText("" + 0.0f);
                    } else {
                        sizeXSlider.setValue(sliderValue);
                        textField4.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeXSlider.setValue(initSizePos);
                    textField4.setText("" + initSize);
                    setSizeX(initSize);
                    updateContent();
                }
                
                String sizeString = textField4.getText();
                float size = Float.parseFloat(sizeString);
                setSizeX(size);
                updateContent();                
            }
            
            public void focusGained(FocusEvent e) {
            }            
        });
        
        sizeXSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);      
                if (value == 0) {
                    displayValue = 0f;
                } 
                if (value == 2000) {
                    displayValue = (float)(2*maxPos);
                }
                textField4.setText("" + displayValue);
                String dString = textField4.getText();
                float size = Float.parseFloat(dString);
                setSizeX(size);
                updateContent();
            }
        });
        
        
        textField5.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField5.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeYSlider.setValue(2000);
                        textField5.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeYSlider.setValue(0);
                        textField5.setText("" + 0.0f);
                    } else {
                        sizeYSlider.setValue(sliderValue);
                        textField5.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeYSlider.setValue(initSizePos);
                    textField5.setText("" + initSize);
                    setSizeY(initSize);
                    updateContent();
                }
                
                String sizeString = textField5.getText();
                float size = Float.parseFloat(sizeString);
                setSizeY(size);
                updateContent();
            }
        });       
        
        textField5.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {  
                int sliderValue;
                try {
                    String s = textField5.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeYSlider.setValue(2000);
                        textField5.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeYSlider.setValue(0);
                        textField5.setText("" + 0.0f);
                    } else {
                        sizeYSlider.setValue(sliderValue);
                        textField5.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeYSlider.setValue(initSizePos);
                    textField5.setText("" + initSize);
                    setSizeY(initSize);
                    updateContent();
                }
                
                String sizeString = textField5.getText();
                float size = Float.parseFloat(sizeString);
                setSizeY(size);
                updateContent();                
            }
            
            public void focusGained(FocusEvent e) {
            }            
        });
        
        sizeYSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);      
                if (value == 0) {
                    displayValue = 0f;
                } 
                if (value == 2000) {
                    displayValue = (float)(2*maxPos);
                }
                textField5.setText("" + displayValue);
                String dString = textField5.getText();
                float size = Float.parseFloat(dString);
                setSizeY(size);
                updateContent();
            }
        });       
        
        textField6.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                int sliderValue;
                try {
                    String s = textField6.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeZSlider.setValue(2000);
                        textField6.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeZSlider.setValue(0);
                        textField6.setText("" + 0.0f);
                    } else {
                        sizeZSlider.setValue(sliderValue);
                        textField6.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeZSlider.setValue(initSizePos);
                    textField6.setText("" + initSize);
                    setSizeZ(initSize);
                    updateContent();
                }
                
                String sizeString = textField6.getText();
                float size = Float.parseFloat(sizeString);
                setSizeZ(size);
                updateContent();
            }
        });   
        
        textField6.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) { 
                int sliderValue;
                try {
                    String s = textField6.getText();
                    float val = Float.parseFloat(s);
                    sliderValue = getSliderPos(val);
                    if (sliderValue >= 2000) {
                        sizeZSlider.setValue(2000);
                        textField6.setText("" + (float)(2*maxPos));
                    }
                    else if (sliderValue == 0) {
                        sizeZSlider.setValue(0);
                        textField6.setText("" + 0.0f);
                    } else {
                        sizeZSlider.setValue(sliderValue);
                        textField6.setText("" + (float)val);
                    }
                } catch (Exception exc){
                    sizeZSlider.setValue(initSizePos);
                    textField6.setText("" + initSize);
                    setSizeZ(initSize);
                    updateContent();
                }
                
                String sizeString = textField6.getText();
                float size = Float.parseFloat(sizeString);
                setSizeZ(size);
                updateContent();                
            }
            
            public void focusGained(FocusEvent e) {
            }            
        });
        
        sizeZSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                float displayValue = getCoord(value);      
                if (value == 0) {
                    displayValue = 0f;
                } 
                if (value == 2000) {
                    displayValue = (float)(2*maxPos);
                }
                textField6.setText("" + displayValue);
                String dString = textField6.getText();
                float size = Float.parseFloat(dString);
                setSizeZ(size);
                updateContent();
            }
        });     
        
        
        //Listener listener = new Listener();
        //addWindowListener(listener);    
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();           
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag); 
        
        JLabel posXLabel = new JLabel("Position X  ");
        buildConstraints(constraints, 0, 0, 1, 1, 25, 16);
        gridbag.setConstraints(posXLabel, constraints);
        pane.add(posXLabel); 
        
        
        buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
        gridbag.setConstraints(textField1, constraints);
        pane.add(textField1);
        
        buildConstraints(constraints, 2, 0, 1, 1, 50, 0);
        gridbag.setConstraints(posXSlider, constraints);
        pane.add(posXSlider);    
        
        
        JLabel posYLabel = new JLabel("Position Y  ");
        buildConstraints(constraints, 0, 1, 1, 1, 25, 16);
        gridbag.setConstraints(posYLabel, constraints);
        pane.add(posYLabel); 
        
        
        buildConstraints(constraints, 1, 1, 1, 1, 25, 0);
        gridbag.setConstraints(textField2, constraints);
        pane.add(textField2);
        
        buildConstraints(constraints, 2, 1, 1, 1, 50, 0);
        gridbag.setConstraints(posYSlider, constraints);
        pane.add(posYSlider); 
        

        JLabel posZLabel = new JLabel("Position Z  ");
        buildConstraints(constraints, 0, 2, 1, 1, 25, 17);
        gridbag.setConstraints(posZLabel, constraints);
        pane.add(posZLabel); 
        
        
        buildConstraints(constraints, 1, 2, 1, 1, 25, 0);
        gridbag.setConstraints(textField3, constraints);
        pane.add(textField3);
        
        buildConstraints(constraints, 2, 2, 1, 1, 50, 0);
        gridbag.setConstraints(posZSlider, constraints);
        pane.add(posZSlider);     
        
        JLabel sizeXLabel = new JLabel("Size X  ");
        buildConstraints(constraints, 0, 3, 1, 1, 25, 17);
        gridbag.setConstraints(sizeXLabel, constraints);
        pane.add(sizeXLabel); 
        
        
        buildConstraints(constraints, 1, 3, 1, 1, 25, 0);
        gridbag.setConstraints(textField4, constraints);
        pane.add(textField4);
        
        buildConstraints(constraints, 2, 3, 1, 1, 50, 0);
        gridbag.setConstraints(sizeXSlider, constraints);
        pane.add(sizeXSlider);       
        
        JLabel sizeYLabel = new JLabel("Size Y  ");
        buildConstraints(constraints, 0, 4, 1, 1, 25, 17);
        gridbag.setConstraints(sizeYLabel, constraints);
        pane.add(sizeYLabel); 
        
        
        buildConstraints(constraints, 1, 4, 1, 1, 25, 0);
        gridbag.setConstraints(textField5, constraints);
        pane.add(textField5);
        
        buildConstraints(constraints, 2, 4, 1, 1, 50, 0);
        gridbag.setConstraints(sizeYSlider, constraints);
        pane.add(sizeYSlider);     
        
        JLabel sizeZLabel = new JLabel("Size Z  ");
        buildConstraints(constraints, 0, 5, 1, 1, 25, 17);
        gridbag.setConstraints(sizeZLabel, constraints);
        pane.add(sizeZLabel); 
        
        
        buildConstraints(constraints, 1, 5, 1, 1, 25, 0);
        gridbag.setConstraints(textField6, constraints);
        pane.add(textField6);
        
        buildConstraints(constraints, 2, 5, 1, 1, 50, 0);
        gridbag.setConstraints(sizeZSlider, constraints);
        pane.add(sizeZSlider);              
        
        addContent(pane);
        Listener listener = new Listener();
        addWindowListener(listener);
        setActionListeners(listener);    
        pack();
    }
    
    private int getSliderPos(float pos) {
        return Math.round((float)((1000f/maxPos)*pos));        
    }
    
    private float getCoord(int sliderPos) {
        return (float)((float)(maxPos*sliderPos)/1000f);
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
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /**
     * Returns x position.
     */
    public float getPositionX() {
        return Float.parseFloat(textField1.getText());
    }
    
    private void setPositionX(float posX) {
        positionX = posX;
    }
    
    /**
     * Returns y position.
     */
    public float getPositionY() {
        return Float.parseFloat(textField2.getText());
    }
    
    private void setPositionY(float posY) {
        positionY = posY;
    }    
    
    /**
     * Returns z position.
     */
    public float getPositionZ() {
        return Float.parseFloat(textField3.getText());
    }
    
    private void setPositionZ(float posZ) {
        positionZ = posZ;
    }    
    
    /**
     * Returns x size.
     */
    public float getSizeX() {
        return Float.parseFloat(textField4.getText());
    }
    
    private void setSizeX(float val) {
        sizeX = val;
    }    
    
    /**
     * Returns y size.
     */
    public float getSizeY() {
        return Float.parseFloat(textField5.getText());
    }
    
    private void setSizeY(float val) {
        sizeY = val;
    }    
    
    /**
     * Returns z size.
     */
    public float getSizeZ() {
        return Float.parseFloat(textField6.getText());
    }
    
    private void setSizeZ(float val) {
        sizeZ = val;
    }    
    
    private void updateContent() {
        content.setBoxPosition(getPositionX(), getPositionY(), getPositionZ());
        content.setBoxSize(getSizeX(), getSizeY(), getSizeZ());
        content.updateScene();       
    }
    
    private void updateContentToInit() {
        content.setBoxPosition(0f, 0f, 0f);
        content.setBoxSize(initSize, initSize, initSize);
        content.updateScene();
    }
    
    private void resetValues() {
        posXSlider.setValue(0);
        posYSlider.setValue(0);
        posZSlider.setValue(0);
        sizeXSlider.setValue(initSizePos);
        sizeYSlider.setValue(initSizePos);
        sizeZSlider.setValue(initSizePos);
        
        textField1.setText("" + 0f);
        textField2.setText("" + 0f);
        textField3.setText("" + 0f);
        textField4.setText("" + initSize);
        textField5.setText("" + initSize);
        textField6.setText("" + initSize);
    }
    
    public static void main(String [] args){
        PCASelectionAreaDialog dialog = new PCASelectionAreaDialog(new Frame(), 0,0,0,5,5,5);
        dialog.showModal();
    }
    
    /**
     * The listener to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    Float.parseFloat(textField1.getText());
                    Float.parseFloat(textField2.getText());
                    Float.parseFloat(textField3.getText());
                    Float.parseFloat(textField4.getText());
                    Float.parseFloat(textField5.getText());
                    Float.parseFloat(textField6.getText());
                    result = JOptionPane.OK_OPTION;
                } catch (Exception exception) {
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){ 
                resetValues();
                updateContentToInit();
                /*
                textField1.setText(String.valueOf(positionX));
                textField2.setText(String.valueOf(positionY));
                textField3.setText(String.valueOf(positionZ));
                textField4.setText(String.valueOf(sizeX));
                textField5.setText(String.valueOf(sizeY));
                textField6.setText(String.valueOf(sizeZ));
                 */
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(PCASelectionAreaDialog.this, "PCA Selection Area Configuration");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 600);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
    }
    
}
