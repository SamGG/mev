/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SlideDataLoader.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.util.swing;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.util.StringSplitter;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;

public class SlideDataLoader extends JDialog {
    
    private static final int BUFFER_SIZE = 1024*128;
    private int result = JOptionPane.CANCEL_OPTION;
    private ISlideData[] data;
    private ISlideMetaData meta;
    private File[] files;
    private boolean stop = false;
    private Exception exception;
    private LoadingPanel loadingPanel = new LoadingPanel();
    private boolean fillMissingSpots = false;
    
    /**
     * Creates a <code>SlideDataLoader</code> to load data from the
     * specified file.
     */
    public SlideDataLoader(JFrame frame, ISlideMetaData meta, File file, boolean fillMissingSpots) {
        this(frame, meta, new File[] {file}, fillMissingSpots);
    }
    
    /**
     * Creates a <code>SlideDataLoader</code> to load data from the
     * specified array of files.
     */
    public SlideDataLoader(JFrame frame, ISlideMetaData meta, File[] files, boolean fillMissingSpots) {
        super(frame, "Slide Data Loading", true);
        this.meta = meta;
        this.files = files;
        this.fillMissingSpots = fillMissingSpots;
        Listener listener = new Listener();
        
        loadingPanel.setFilesCount(files.length);
        JPanel btnsPanel = createBtnsPanel(listener);
        
        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        content.add(loadingPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        content.add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
        
        addWindowListener(listener);
        pack();
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() throws Exception {
        return showModal(false);
    }
    
    /**
     * Shows the dialog to load files with specified type of format.
     */
    public int showModal(boolean stanford) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        Thread thread = new Thread(new Loader(stanford));
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        show();
        // check the thread result
        if (isException()) {
            throw getException();
        }
        return result;
    }
    
    /**
     * Returns an array of loaded microarrays.
     */
    public ISlideData[] getData() {
        return data;
    }
    
    /**
     * Returns data loaded from specified microarray.
     */
    public ISlideData getData(int index) {
        if (data == null || index >= data.length || index < 0) {
            return null;
        }
        return data[index];
    }
    
    /**
     * @return true if it was an exception while data loaded.
     */
    private boolean isException() {
        return exception != null;
    }
    
    /**
     * Returns a wrapped exception.
     */
    private Exception getException() {
        return exception;
    }
    
    /**
     * Sets a wrapped exception.
     */
    private void setException(Exception e) {
        exception = e;
    }
    
    /**
     * Loads microarrays data.
     */
    private ISlideData[] loadData() throws IOException {
        if (files.length < 1) {
            return null;
        }
        ISlideData[] slideData = new ISlideData[files.length];
        int countOfLines = 0;
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            loadingPanel.setFilesProgress(i);
            loadingPanel.setRemain(files.length-i);
            loadingPanel.setFileName(files[i].getPath());
            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                loadingPanel.setLinesCount(countOfLines);
                if (meta == null) {
                    if(fillMissingSpots)
                        slideData[i] = loadSlideDataFillAllSpots(files[i]);
                    else
                        slideData[i] = loadSlideData(files[i]);
                    meta = slideData[i].getSlideMetaData();
                } else {
                    slideData[i] = loadFloatSlideData(files[i], countOfLines, meta);
                }
            } else {
                slideData[i] = loadFloatSlideData(files[i], countOfLines, meta);
            }
        }
        return slideData;
    }
    
    /**
     * Loads a microarray float values from the specified file.
     */
    private ISlideData loadFloatSlideData(final File file, final int countOfLines, ISlideMetaData slideMetaData) throws IOException {
        final int coordinatePairCount = TMEV.getCoordinatePairCount()*2;
        final int intensityCount = TMEV.getIntensityCount();
        final int preSpotRows = TMEV.getHeaderRowCount();
        
        //FL
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        FloatSlideData slideData = new FloatSlideData(slideMetaData);
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        
        String currentLine;
        StringSplitter ss = new StringSplitter((char)0x09);
        float[] intensities = new float[intensityCount];
        int header_row = 0;
        int index  = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            loadingPanel.setFileProgress(index);
            ss.init(currentLine);
            ss.passTokens(coordinatePairCount);
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            slideData.setIntensities(index, intensities[0], intensities[1]);
            index++;
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    /**
     * Loads full a microarray data from a specified file.
     * Fills all missing spots with default missing color.
     */
    private ISlideData loadSlideDataFillAllSpots(final File file) throws IOException {
        
        ISlideDataElement slideDataElement;
        String currentLine;
        
        //FL
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        int maxRows = 0, maxColumns = 0;
        String avoidNullString;
        int p, q;
        int coordinatePairCount = TMEV.getCoordinatePairCount();
        int intensityCount = TMEV.getIntensityCount();
        final int preSpotRows = TMEV.getHeaderRowCount();
        
        int[] rows = new int[coordinatePairCount];
        int[] columns = new int[coordinatePairCount];
        
        float[] intensities = new float[intensityCount];
        String[] moreFields = new String[TMEV.getFieldNames().length];
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        StringSplitter ss = new StringSplitter((char)0x09);
        int currentRow, currentColumn;
        int header_row = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            ss.init(currentLine);
            currentRow = ss.nextIntToken();
            currentColumn = ss.nextIntToken();
            if (currentRow > maxRows) maxRows = currentRow;
            if (currentColumn > maxColumns) maxColumns = currentColumn;
        }
        SlideData slideData = new SlideData(maxRows, maxColumns);
        reader.close();
        reader = new BufferedReader(new FileReader(file));
        header_row = 0;
        int curpos = 0;
        
        boolean [][] realData = new boolean[maxRows][maxColumns];
        
        while ((currentLine = reader.readLine()) != null) {
            
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            loadingPanel.setFileProgress(curpos++);
            ss.init(currentLine);
            for (int j = 0; j < coordinatePairCount; j++) {
                rows[j] = ss.nextIntToken();
                columns[j] = ss.nextIntToken();
            }
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            for (int j = 0; j < TMEV.getFieldNames().length; j++) {
                if (ss.hasMoreTokens()) {
                    avoidNullString = ss.nextToken();
                    if (avoidNullString.equals("null")) moreFields[j] = "";
                    else moreFields[j] = avoidNullString;
                } else {
                    moreFields[j] = "";
                }
            }
            realData[rows[0]-1][columns[0]-1] = true;
            slideDataElement = new SlideDataElement(rows, columns, intensities, moreFields);
            slideData.addSlideDataElement(slideDataElement);
        }
        reader.close();
        intensities[0] = 0.0f;
        intensities[1] = 0.0f;
        
        String [] dummyString = new String[TMEV.getFieldNames().length];
        for(int i = 0; i < dummyString.length; i++)
            dummyString[i] = "";
        
        for(int i = 0; i < maxRows ; i++){
            for(int j = 0; j < maxColumns; j++){
                if(!realData[i][j]){
                    slideDataElement = new SlideDataElement(new int[]{i+1, 1, 1}, new int[]{j+1, 1,1}, intensities, dummyString);
                    slideData.insertElementAt(slideDataElement, i*maxColumns+j);
                }
            }
        }
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    public static ISlideData fillBlankSpots(ISlideData slideData) {
        
        //calculate maxRows and maxColumns
        int maxRow = 0, maxCol = 0;
        int rowVal, colVal;
        ISlideDataElement element;
        
        for(int i = 0; i < slideData.getSize(); i++){
            element = slideData.getSlideDataElement(i);
            rowVal = element.getRow(0);
            colVal = element.getColumn(0);
            maxRow = Math.max(maxRow, rowVal);
            maxCol = Math.max(maxCol, colVal);
        }
                
        return fillBlankSpots(slideData, maxRow, maxCol);
    }
    
    public static ISlideData fillBlankSpots(ISlideData slideData, int maxRows, int maxColumns) {
        String [] dummyString = new String[TMEV.getFieldNames().length];
        float [] intensities = new float[TMEV.getIntensityCount()];
        boolean [][] realData = new boolean[maxRows][maxColumns];
        ISlideDataElement element;
        
        if(slideData.getSize() == maxRows*maxColumns)
            return slideData;
        
        for(int i = 0; i < slideData.getSize(); i++){
            element = slideData.getSlideDataElement(i);
            realData[element.getRow(0)-1][element.getColumn(0)-1] = true;            
        }
        
        for(int i = 0; i < TMEV.getIntensityCount(); i++)
            intensities[i] = 0.0f;
        
        for(int i = 0; i < dummyString.length; i++)
            dummyString[i] = "";
        
        if(slideData instanceof SlideData){
            for(int i = 0; i < maxRows ; i++){
                for(int j = 0; j < maxColumns; j++){
                    if(!realData[i][j]){
                        element = new SlideDataElement(new int[]{i+1, 1, 1}, new int[]{j+1, 1,1}, intensities, dummyString);
                        
                        ((SlideData)slideData).insertElementAt(element, i*maxColumns+j);
                    }
                }
            }
        }
   /*     else if(slideData instanceof FloatSlideData){
            for(int i = 0; i < maxRows ; i++){
                for(int j = 0; j < maxColumns; j++){
                    if(!realData[i][j]){
                        element = new SlideDataElement(new int[]{i+1, 1, 1}, new int[]{j+1, 1,1}, intensities, dummyString);
                        
                        ((FloatSlideData)slideData).insertElementAt(element, i*maxColumns+j);
                    }
                }
            }
        }
    */
        return slideData;
    }
    
    /**
     * Loads full a microarray data from a specified file.
     * Skips missing spots.
     */
    
    private ISlideData loadSlideData(final File file) throws IOException {
        
        ISlideDataElement slideDataElement;
        String currentLine;
        
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 9);
            TMEV.setNameIndex(TMEV.getNameIndex() - 9);
            TMEV.setIndicesAdjusted(true);
        }
        
        int maxRows = 0, maxColumns = 0;
        String avoidNullString;
        int p, q;
        int coordinatePairCount = TMEV.getCoordinatePairCount();
        int intensityCount = TMEV.getIntensityCount();
        final int preSpotRows = TMEV.getHeaderRowCount();
        
        int[] rows = new int[coordinatePairCount];
        int[] columns = new int[coordinatePairCount];
        float[] intensities = new float[intensityCount];
        String[] moreFields = new String[TMEV.getFieldNames().length];
        
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        StringSplitter ss = new StringSplitter((char)0x09);
        int currentRow, currentColumn;
        int header_row = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            ss.init(currentLine);
            currentRow = ss.nextIntToken();
            currentColumn = ss.nextIntToken();
            if (currentRow > maxRows) maxRows = currentRow;
            if (currentColumn > maxColumns) maxColumns = currentColumn;
        }
        SlideData slideData = new SlideData(maxRows, maxColumns);
        reader.close();
        reader = new BufferedReader(new FileReader(file));
        header_row = 0;
        int curpos = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            loadingPanel.setFileProgress(curpos++);
            ss.init(currentLine);
            for (int j = 0; j < coordinatePairCount; j++) {
                rows[j] = ss.nextIntToken();
                columns[j] = ss.nextIntToken();
            }
            for (int j = 0; j < intensityCount; j++) {
                intensities[j] = ss.nextFloatToken(0.0f);
            }
            for (int j = 0; j < TMEV.getFieldNames().length; j++) {
                if (ss.hasMoreTokens()) {
                    avoidNullString = ss.nextToken();
                    if (avoidNullString.equals("null")) moreFields[j] = "";
                    else moreFields[j] = avoidNullString;
                } else {
                    moreFields[j] = "";
                }
            }
            slideDataElement = new SlideDataElement(rows, columns, intensities, moreFields);
            slideData.addSlideDataElement(slideDataElement);
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }
    
    
    /**
     * Loads a microarray data from a stanford file.
     */
    public ISlideData[] loadStanford() throws Exception {
        if (this.files.length < 1) {
            return null;
        }
        
        //FL
        //Adjusts index values to make it consistent
        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(TMEV.getUniqueIDIndex() - 1);
            TMEV.setNameIndex(TMEV.getNameIndex() - 1);
            TMEV.setIndicesAdjusted(true);
        }
        
        final int preSpotRows = TMEV.getHeaderRowCount();
        final int preExperimentColumns = TMEV.getHeaderColumnCount();
        
        int spotCount = getCountOfLines(this.files[0]);
        if (spotCount <= 0) {
            throw new Exception("There are no spot data.");
        }
        loadingPanel.setFilesProgress(0);
        loadingPanel.setRemain(1);
        loadingPanel.setFileName(files[0].getPath());
        loadingPanel.setLinesCount(spotCount);
        
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        float cy3, cy5;
        String[] moreFields = new String[preExperimentColumns];
        
        //final int rColumns = (int)Math.ceil(Math.sqrt(spotCount));
        //final int rRows    = (int)Math.ceil((float)spotCount/(float)rColumns);
        final int rColumns = 1;
        final int rRows    = spotCount;
        
        ISlideData[] slideDataArray = null;
        SlideDataElement sde;
        
        BufferedReader reader = new BufferedReader(new FileReader(this.files[0]));
        StringSplitter ss = new StringSplitter((char)0x09);
        String currentLine;
        int counter, row, column;
        counter = 0;
        row = column = 1;
        while ((currentLine = reader.readLine()) != null) {
            if (stop) {
                return null;
            }
            ss.init(currentLine);
            if (counter == 0) { // parse header
                int experimentCount = ss.countTokens()+1 - preExperimentColumns;
                slideDataArray = new ISlideData[experimentCount];
                slideDataArray[0] = new SlideData(rRows, rColumns);
                for (int i=1; i<slideDataArray.length; i++) {
                    slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount);
                }
                ss.passTokens(preExperimentColumns);
                for (int i=0; i<experimentCount; i++) {
                    slideDataArray[i].setSlideDataName(ss.nextToken());
                }
            } else if (counter >= preSpotRows) { // data rows
                rows[0] = rows[2] = row;
                columns[0] = columns[2] = column;
                if (column == rColumns) {
                    column = 1;
                    row++;
                } else {
                    column++;
                }
                for (int i=0; i<preExperimentColumns; i++) {
                    moreFields[i] = ss.nextToken();
                }
                sde = new SlideDataElement(rows, columns, new float[2], moreFields);
                slideDataArray[0].addSlideDataElement(sde);
                for (int i=0; i<slideDataArray.length; i++) {
                    cy3 = 100000f;
                    try {
                        //LOG
                        cy5 = (float)(100000f*Math.pow(2.0f, Float.parseFloat(ss.nextToken())));
                        //cy5 = (float)(100000f*Math.pow(Math.E, Float.parseFloat(ss.nextToken())));
                    } catch (Exception e) {
                        cy3 = cy5 = 0f;
                    }
                    slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
                }
            }
            counter++;
            loadingPanel.setFileProgress(counter);
        }
        reader.close();
        return slideDataArray;
    }
    
    /**
     * Returns number of lines in the specified file.
     */
    private int getCountOfLines(File file) throws IOException {
        int count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            count++;
        }
        reader.close();
        return count-TMEV.getHeaderRowCount();
    }
    
    /**
     * Interrupts a loading process.
     */
    private void abort() {
        stop = true;
    }
    
    /**
     * Sets a result code of loading process.
     */
    private void setResult(int option) {
        result = option;
    }
    
    /**
     * Creates a panel with 'cancel' button.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel-command");
        cancelButton.addActionListener(listener);
        panel.add(cancelButton, BorderLayout.EAST);
        getRootPane().setDefaultButton(cancelButton);
        return panel;
    }
    
    /**
     * The class to listen to window and an action events.
     */
    private class Listener extends WindowAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            abort();
            setResult(JOptionPane.CANCEL_OPTION);
            dispose();
        }
        
        public void windowClosing(WindowEvent e) {
            abort();
            setResult(JOptionPane.CLOSED_OPTION);
            dispose();
        }
    }
    
    /**
     * The panel to display a loading progress.
     */
    private class LoadingPanel extends JPanel {
        
        private JProgressBar filesProgress = new JProgressBar();
        private JProgressBar fileProgress = new JProgressBar();
        private JLabel filesLabel = new JLabel("Remain: ");
        private JLabel fileLabel = new JLabel("File: ");
        
        /**
         * Constructs a <code>LoadingPanel</code>.
         */
        public LoadingPanel() {
            setPreferredSize(new Dimension(350, 120));
            setBorder(new BevelBorder(BevelBorder.RAISED));
            setLayout(new GridBagLayout());
            filesProgress.setStringPainted(true);
            fileProgress.setStringPainted(true);
            add(filesLabel,    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
            add(filesProgress, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
            add(fileLabel,     new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
            add(fileProgress,  new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
        }
        
        /**
         * Sets max value for the 'files' progress bar.
         */
        public void setFilesCount(int count) {
            filesProgress.setMaximum(count);
        }
        
        /**
         * Sets max value for the 'file' progress bar.
         */
        public void setLinesCount(int count) {
            fileProgress.setMaximum(count);
        }
        
        /**
         * Sets current value of the 'files' progress bar.
         */
        public void setFilesProgress(int value) {
            filesProgress.setValue(value);
        }
        
        /**
         * Sets current value of the 'file' progress bar.
         */
        public void setFileProgress(int value) {
            fileProgress.setValue(value);
        }
        
        /**
         * Sets name of a loaded file.
         */
        public void setFileName(String filename) {
            fileLabel.setText("File: "+filename);
        }
        
        /**
         * Sets common progress description.
         */
        public void setRemain(int count) {
            filesLabel.setText("Remains: "+String.valueOf(count)+" file(s)");
        }
    }
    
    /**
     * The class to allow run loading process in a separate thread.
     */
    private class Loader implements Runnable {
        
        private boolean stanford;
        
        public Loader(boolean stanford) {
            this.stanford = stanford;
        }
        
        public void run() {
            try {
                data = stanford ? loadStanford() : loadData();
                if (data == null) {
                    setResult(JOptionPane.CANCEL_OPTION);
                } else {
                    setResult(JOptionPane.OK_OPTION);
                }
            } catch (Exception e) {
                setException(e);
            } finally {
                dispose();
            }
        }
        
    }
}
