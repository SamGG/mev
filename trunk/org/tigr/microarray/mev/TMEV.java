/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: TMEV.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev;

import java.io.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.*;

import org.tigr.util.StringSplitter;
import org.tigr.util.ConfMap;
import org.tigr.util.awt.ImageScreen;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.IGUIFactory;

public class TMEV {
    public final static int SYSTEM = 1000;
    public final static int DB_AVAILABLE = 1001;
    public final static int DB_LOGIN = 1002;
    public final static int DATA_AVAILABLE = 1003;
    public final static int SPOTFIRE_AVAILABLE = 1004;
    public final static int DATA_TYPE_TWO_DYE = 1;
    public final static int DATA_TYPE_AFFY = 2;
    
    private static Connection connection;
    private static Hashtable properties;
    private static AlgorithmFactory algorithmFactory;
    private static IGUIFactory guiFactory;
    private static int coordinatePairCount;
    private static int intensityCount;
    private static int headerRowCount;
    private static int headerColumnCount;
    private static int uniqueIDIndex;
    private static int nameIndex;
    //FL
    private static boolean indicesAdjusted = false;
    private static String[] fieldNames;
    private static String[] databases;
    
    // pcahan                       jcb:constant
    private static int dataType = DATA_TYPE_TWO_DYE;
    
    public static void main(String[] args) {
        try {
            System.out.println("TIGR MultiExperimentViewer (1057937513220) - version 2.2 - " + System.getProperty("os.name"));
            String Java3DTitle, Java3DVendor, Java3DVersion;
            try {
                InformationPanel info = new InformationPanel();
                Java3DTitle = info.getJava3DRunTimeEnvironment();
                Java3DVendor = info.getJava3DVendor();
                Java3DVersion = info.getJava3DVersion();
            } catch (Exception e) {
                Java3DTitle="not installed";
                Java3DVendor="not available";
                Java3DVersion="not available";
            }
            // System.out.println(System.currentTimeMillis());
            System.out.println("Java Runtime Environment version: "+System.getProperty("java.version"));
            System.out.println("Java Runtime Environment vendor: "+System.getProperty("java.vendor"));
            System.out.println("Java Virtual Machine name: "+System.getProperty("java.vm.name"));
            System.out.println("Java Virtual Machine version: "+System.getProperty("java.vm.version"));
            System.out.println("Java Virtual Machine vendor: "+System.getProperty("java.vm.vendor"));
            System.out.println("Java 3D Runtime Environment: "+Java3DTitle);
            System.out.println("Java 3D Runtime Environment vendor: "+Java3DVendor);
            System.out.println("Java 3D Runtime Environment version:"+Java3DVersion);
            System.out.println("Operating System name: "+System.getProperty("os.name"));
            System.out.println("Operating System version: "+System.getProperty("os.version"));
            System.out.println("Operating System architecture: "+System.getProperty("os.arch"));
            
            configure();
            ImageScreen is = new ImageScreen();
            is.showImageScreen(1500);
            
            Manager manager = new Manager();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean readPreferencesFile(File inputFile) {
        BufferedReader reader = null;
        boolean returnValue = true;
        
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            returnValue = false;
        }
        StringTokenizer ss;
        try {
            String currentLine, key;
            TMEV.properties = new Hashtable();
            
            //FL
            indicesAdjusted = false;
            
            while ((currentLine = reader.readLine()) != null) {
                currentLine.trim();
                if (!(currentLine.startsWith("//") || (currentLine.length() == 0))) {
                    ss = new StringTokenizer(currentLine, "\t");
                    key = ss.nextToken();
                    if (ss.hasMoreTokens()) properties.put(key, ss.nextToken());
                    else properties.put(key, new String(""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String elementInfo = TMEV.getSettingForOption("Element Info");
            ss = new StringTokenizer(elementInfo, ":");
            coordinatePairCount = Integer.parseInt(ss.nextToken());
            intensityCount = Integer.parseInt(ss.nextToken());
            
            String headerInfo = TMEV.getSettingForOption("Headers");
            ss = new StringTokenizer(headerInfo, ":");
            headerRowCount = Integer.parseInt(ss.nextToken());
            headerColumnCount = Integer.parseInt(ss.nextToken());
            
            String uniqueIDString = TMEV.getSettingForOption("Unique ID");
            uniqueIDIndex = Integer.parseInt(uniqueIDString);
            
            String nameString = TMEV.getSettingForOption("Spot Name");
            nameIndex = Integer.parseInt(nameString);
            
            String dbs = TMEV.getSettingForOption("Database Names");
            ss = new StringTokenizer(dbs, ":");
            TMEV.databases = new String[ss.countTokens()];
            for (int i = 0; ss.hasMoreTokens(); i++) {
                TMEV.databases[i] = ss.nextToken();
            }
            
            String additionalFields = TMEV.getSettingForOption("Additional Fields");
            ss = new StringTokenizer(additionalFields, ":");
            if (ss.countTokens() > 0) {
                TMEV.fieldNames = new String[ss.countTokens()];
                for (int i = 0; ss.hasMoreTokens(); i++) {
                    TMEV.fieldNames[i] = ss.nextToken();
                }
            } else TMEV.fieldNames = null;
        } catch (Exception e) {
            e.printStackTrace();
            returnValue = false;
        }
        return returnValue;
    }
    
    public static String getSettingForOption(String option) {
        String setting = "";
        try {
            setting = (String) properties.get(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setting;
    }
    
    /*
    public static boolean connect(String username, String password) {
        try {
            Class.forName("com.sybase.jdbc2.jdbc.SybDriver");
            String server = new String(getSettingForOption("Database Server Name"));
            Properties connectionProperties = new Properties();
     
            connectionProperties.put("user", username);
            connectionProperties.put("password", password);
            connectionProperties.put("APPLICATIONNAME", "TIGR MultiExperimentViewer");
     
            DriverManager.setLoginTimeout(2200);
            TMEV.connection = DriverManager.getConnection(server, connectionProperties);
            return true;
        } catch (Exception e) {
            System.out.println("Exception (TMEV.connect()): " + e);
            return false;
        }
    }
     */
    
        /*
         * This code was modified by Jim Johnson with other changes to enable
         * Java Web Start
         */
    public static boolean connect(String username, String password) {
        try {
            System.err.println("jdbc.drivers " + System.getProperty("jdbc.drivers"));
            for (Enumeration en = DriverManager.getDrivers();en.hasMoreElements();) {
                System.err.println("jdbc.driver " + en.nextElement());
            }
            String drivers = System.getProperty("jdbc.drivers");
            if (drivers != null) {
                for (StringTokenizer st = new StringTokenizer(drivers,":"); st.hasMoreTokens();) {
                    try {
                        Class.forName(st.nextToken());
                    } catch (Exception ex) {
                    }
                }
            }
            //Class.forName("com.sybase.jdbc2.jdbc.SybDriver");
            String server = new String(getSettingForOption("Database Server Name"));
            Properties connectionProperties = new Properties();
            
            connectionProperties.put("user", username);
            connectionProperties.put("password", password);
            connectionProperties.put("APPLICATIONNAME", "TIGR MultiExperimentViewer");
            
            DriverManager.setLoginTimeout(2200);
            TMEV.connection = DriverManager.getConnection(server, connectionProperties);
            return true;
        } catch (Exception e) {
            System.out.println("Exception (TMEV.connect()): " + e);
            return false;
        }
    }
    
    
    public static int getCoordinatePairCount() {return TMEV.coordinatePairCount;}
    public static int getIntensityCount() {return TMEV.intensityCount;}
    public static int getHeaderRowCount() {return TMEV.headerRowCount;}
    public static int getHeaderColumnCount() {return TMEV.headerColumnCount;}
    public static String[] getFieldNames() {
        if(TMEV.fieldNames == null)
            return new String[0];
        return TMEV.fieldNames;
    }
    public static String[] getDatabases() {return TMEV.databases;}
    public static int getUniqueIDIndex() {return TMEV.uniqueIDIndex;}
    public static int getNameIndex() {return TMEV.nameIndex;}
    public static Connection getConnection() {return TMEV.connection;}
    //pcahan
    public static int getDataType() {return TMEV.dataType; }
    //set method for use with SuperLoader
    public static void setDataType(int dataType){ TMEV.dataType = dataType; }
    public static void setIndicesAdjusted(boolean state) {TMEV.indicesAdjusted = state;}
    public static boolean indicesAdjusted() {return TMEV.indicesAdjusted;}
    public static void setUniqueIDIndex(int index) {
        TMEV.uniqueIDIndex = (index < 0) ? 0 : index;
    }
    
    
    public static void setNameIndex(int index) {
        TMEV.nameIndex = (index < 0) ? 0 : index;
    }
    
    public static void setFieldNames(String [] fieldNames){
        TMEV.fieldNames = fieldNames;
    }
    
    public static void appendFieldNames(String [] fieldNames){
        if(TMEV.fieldNames == null || fieldNames == null)  //trying to set to null or initial set
            TMEV.fieldNames = fieldNames;
        else {                  //names exist and new names exist, APPEND (ie. mev format, extra ann load)
            String [] newNames = new String[TMEV.fieldNames.length+fieldNames.length];
            System.arraycopy(TMEV.fieldNames, 0, newNames, 0, TMEV.fieldNames.length);
            System.arraycopy(fieldNames, 0, newNames, TMEV.fieldNames.length, fieldNames.length);
            TMEV.fieldNames = newNames;
        }
    }
    
    public static void clearFieldNames(){
        TMEV.fieldNames = null;
    }
    
 /*   public static void configure() {
        String filename = "tmev.cfg";
        ConfMap cfg = new ConfMap();
        try {
            cfg.load( new FileInputStream(filename));
  
            String guiFactoryClassName = cfg.getString("gui.factory.class");
            if (guiFactoryClassName != null && !guiFactoryClassName.equals("null")) {
                Class clazz = Class.forName(guiFactoryClassName);
                guiFactory = (IGUIFactory)clazz.newInstance();
            } else {
                throw new Exception("GUI factory class name not found, check the 'gui.factory.class' key in "+filename+" file.");
            }
            algorithmFactory = new TMEVAlgorithmFactory(cfg);
        } catch (IOException ioe) {
            System.out.println("Error to load configuration file.");
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  */
    
    /*
     * This code was modified by Jim Johnson with other changes to enable
     * Java Web Start
     */
    public static void configure() {
        String filename = "tmev.cfg";
        ConfMap cfg = new ConfMap();
        try {
            // Try reading configuration from resource
            try {
                InputStream is = TMEV.class.getClassLoader().getResourceAsStream(filename);
                if (is != null) {
                    cfg.load(is);
                }
            } catch (SecurityException se) {
                System.out.println("resource configuration file " + se);
            } catch (IOException ioe) {
                System.out.println("Error to load configuration file.");
                ioe.printStackTrace();
            }
            // Try reading configuration from local file
            try {
                cfg.load( new FileInputStream(filename) );
            } catch (FileNotFoundException fnfe) {
                System.out.println("local configuration file " + fnfe);
            } catch (IOException ioe) {
                System.out.println("Error to load configuration file.");
                ioe.printStackTrace();
            }
            
            
            String guiFactoryClassName = cfg.getString("gui.factory.class");
            if (guiFactoryClassName != null && !guiFactoryClassName.equals("null")) {
                Class clazz = Class.forName(guiFactoryClassName);
                guiFactory = (IGUIFactory)clazz.newInstance();
            } else {
                throw new Exception("GUI factory class name not found, check the 'gui.factory.class' key in "+filename+" file.");
            }
            algorithmFactory = new TMEVAlgorithmFactory(cfg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static AlgorithmFactory getAlgorithmFactory() {
        return algorithmFactory;
    }
    
    public static IGUIFactory getGUIFactory() {
        return guiFactory;
    }
    
    public static void quit() {
        try {
            connection.close();
        } catch (Exception e) {
            ;
        }
        System.exit(0);
    }
    
    
}