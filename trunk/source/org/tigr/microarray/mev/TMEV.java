/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TMEV.java,v $
 * $Revision: 1.20 $
 * $Date: 2007-12-20 19:55:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.tigr.microarray.mev.annotation.AnnotationURLsFileDefinition;
import org.tigr.microarray.mev.annotation.InvalidAnnMappingFileException;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.gui.IGUIFactory;
import org.tigr.microarray.mev.file.FileLoadInfo;
import org.tigr.microarray.mev.file.InvalidFileArgumentsException;
import org.tigr.microarray.mev.resources.ExpressionDataSupportDataFile;
import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
import org.tigr.util.ConfMap;
import org.tigr.util.StringSplitter;
import org.tigr.util.awt.ImageScreen;

public class TMEV {

    public final static int SYSTEM = 1000;
    public final static int DB_AVAILABLE = 1001;
    public final static int DB_LOGIN = 1002;
    public final static int DATA_AVAILABLE = 1003;
    public final static int SPOTFIRE_AVAILABLE = 1004;
    public final static int DATA_TYPE_TWO_DYE = 1;
    public final static int DATA_TYPE_AFFY = 2;

    public static final String LAST_LOADED_ARRAY = "last-loaded-array";
    public static final String LAST_LOADED_SPECIES = "last-loaded-species";
    public static final String PROMPT_TO_SAVE_ANALYSIS = "prompt-for-save";
    public static final String PROMPT_TO_GET_ONLINE = "prompt-to-get-online";
    public static final String ALLOWED_ONLINE = "allow-internet-connections";
    public static final String CUSTOM_ANNOTATION_URLS_FILE = "annotation-urls-mapping-file";
    public static final String CURRENT_DATA_PATH = "current-data-path";
    public static final String CURRENT_ANNOTATION_PATH = "current-annotation-path";
    
    public final static int ANALYSIS_LOADED = 101;
    
    public static final String ELEMENT_SIZE_WIDTH	= "last-selected-element-width";
    public static final String ELEMENT_SIZE_HEIGHT	= "last-selected-element-height";
    public static final String COLOR_SCHEME_INDEX	= "last-selected-color-scheme-index";
    public static final String COLOR_SCHEME_POSITIVE= "last-selected-color-scheme-positive";
    public static final String COLOR_SCHEME_NEGATIVE= "last-selected-color-scheme-negative";
    public static final String COLOR_SCHEME_NEUTRAL	= "last-selected-color-scheme-neutral";
    public static final String COLOR_SCHEME_DOUBLE	= "last-selected-color-scheme-double-gradient";
    
    private static Connection connection;
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
    private static String[] databases;
    
    //Prompt user to save analysis on close
    public static boolean permitSavePrompt = true;
    
    // pcahan                       jcb:constant
    private static int dataType = DATA_TYPE_TWO_DYE;
    
    //OS string
    private static String os = "";
    
    //signals active save in progress
    public static boolean activeSave = false;
    
    //added for Rama (vu 2005.08.24)
    public static String rPath = "127.0.0.1:6311";
    
    private static IResourceManager resourceManager;
    
    /**
     * Raktim
     * Nov 07, 2005
     * CGH default Values
     */
    public static boolean cloneDistributionsLogState = true;
    public static int browserDefaultDyeSwapValue = 1;
    public static int browserDefaultNoDyeSwapValue = 5;
    public static int defaultCloneValue = 1;
    public static int defaultFlankingRegionValue = 0;
    public static final int CGH_SPECIES_HS = 0;
    public static final int CGH_SPECIES_MM = 1;
    public static final int CGH_SPECIES_Undef = -100;

    
    public static boolean GAGGLE_CONNECT_ON_STARTUP = false;
    public static String PROPERTY_CONFIG_FILES = "config-files";

    private static File mevUserDir;
    private static File mevPropertiesFile;
    
    private static ConfMap props;
    private static Properties buildProps = new Properties();
	private static SessionOptions so;
    
    public static void main(String[] args) {
		so = parseArgs(args);
		if(so.sendHelpText()) {
			System.out.println(so.getHelpText());
			System.exit(0);
		}

		if (so.isConnectToGaggle())
			GAGGLE_CONNECT_ON_STARTUP = true;
        
    	mevUserDir = new File(System.getProperty("user.home"), ".mev");
    	mevPropertiesFile = new File(mevUserDir, "mev.properties");
    	try {

            loadBuildProperties();
            
            System.out.println("MultiExperimentViewer - version "+getVersion()+" - " + System.getProperty("os.name"));
            String Java3DTitle, Java3DVendor, Java3DVersion;
            InformationPanel info = new InformationPanel();
            try {
               
                Java3DTitle = info.getJava3DRunTimeEnvironment();
                Java3DVendor = info.getJava3DVendor();
                Java3DVersion = info.getJava3DVersion();
            } catch (Exception e) {
                Java3DTitle="not installed";
                Java3DVendor="not available";
                Java3DVersion="not available";
            }
            
            os = System.getProperty("os.name");

            //System.out.println(System.currentTimeMillis());
            System.out.println("Java Runtime Environment version: "+System.getProperty("java.version"));
            System.out.println("Java Runtime Environment vendor: "+System.getProperty("java.vendor"));
            System.out.println("Java Virtual Machine name: "+System.getProperty("java.vm.name"));
            System.out.println("Java Virtual Machine version: "+System.getProperty("java.vm.version"));
            System.out.println("Java Virtual Machine vendor: "+System.getProperty("java.vm.vendor"));
            System.out.println("Java 3D Runtime Environment: "+Java3DTitle);
            System.out.println("Java 3D Runtime Environment vendor: "+Java3DVendor);
            System.out.println("Java 3D Runtime Environment version:"+Java3DVersion);
            System.out.println("Operating System name: "+os);
            System.out.println("Operating System version: "+System.getProperty("os.version"));
            System.out.println("Operating System architecture: "+System.getProperty("os.arch"));
            System.out.println("MeV Build: " + buildProps.getProperty("BUILD-REVISION", "unknown"));
            System.out.println("MeV Revision Date: " + buildProps.getProperty("REVISION-DATE", "unknown"));
            System.out.println("MeV Branch: " + buildProps.getProperty("BUILD-BRANCH-URL", "unknown"));
            loadProperties();

            setupFiles();
                     
            configure();
            ImageScreen is = new ImageScreen();
            is.showImageScreen(3000);
            
            Manager manager = new Manager();
            
            try {
        	    resourceManager = new FileResourceManager(new File(mevUserDir, "repository"));
        	    
            } catch (RepositoryInitializationError rie) {
        	    JOptionPane.showMessageDialog(null, "MeV was unable to create a repository for support data files at the location \n" + mevUserDir.getAbsolutePath() + "repository.\n" + 
        			    "MeV will be able to load expression data and do analyses, \nbut will not be able to automatically retrieve support data files from the internet for you. ", 
        			    "Error creating Support File Repository", 
        			    JOptionPane.WARNING_MESSAGE);
        	    rie.printStackTrace();
            }
            
        	File _urlsFile;
            if(resourceManager != null) {
       	        try {
           	    	_urlsFile = resourceManager.getSupportFile(new AnnotationURLsFileDefinition(), true);
           	    	loadAnnotationsURLs(_urlsFile);
           	    } catch (SupportFileAccessError sfae) {
           	    } catch (NullPointerException npe) {
           	    }
            } else {
    	    	_urlsFile = getConfigurationFile("annotation_URLs.txt");
    	    	loadAnnotationsURLs(_urlsFile);
            }
            if(TMEV.getSettingForOption(CUSTOM_ANNOTATION_URLS_FILE, null) != null) {
            	try {
            		String _urlfilename = TMEV.getSettingForOption(CUSTOM_ANNOTATION_URLS_FILE, null);
        	    	loadAnnotationsURLs(new File(_urlfilename));
            	} catch (Exception e) {}
            }
            
            if (os.indexOf("Apple") != -1 || os.indexOf("Mac") != -1 ) {
                manager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());              
            }

		if(so.getDataFile() != null && so.getFileType() != null) {
            try {
            	FileLoadInfo fileInfo = new FileLoadInfo(so);

				Manager.createNewMultipleArrayViewer(fileInfo);
            } catch(InvalidFileArgumentsException ifae) {
            	ShowThrowableDialog.show(new JFrame(), "", true, ShowThrowableDialog.ERROR, ifae, "MeV was unable to understand the arguments supplied to it: \n" + so.toString());
    			Manager.createNewMultipleArrayViewer();
            }

		} else {
			Manager.createNewMultipleArrayViewer();
		}

		} catch (Exception e) {
			ShowThrowableDialog.show(new JFrame(), "Error downloading file", false, e);
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(),
					"An error was encountered during file download.  " + e.getMessage(),
					"Data Download Error", JOptionPane.WARNING_MESSAGE);
			Manager.createNewMultipleArrayViewer();
		}
	}
    public static String getVersion() {
    	String temp =buildProps.getProperty("PACKAGE-MAJOR-VERSION", "unknown") + "." +buildProps.getProperty("PACKAGE-MINOR-VERSION", "unknown") + "." + buildProps.getProperty("PACKAGE-MICRO-VERSION", "unknown");
    	if(temp.contains("unknown"))
    		return "unknown";
    	return temp;
    }
    public static void loadAnnotationsURLs(File urlsFile) throws FileNotFoundException, InvalidAnnMappingFileException {
    	if (PublicURL.loadURLs(urlsFile) != 0) {
    		throw new InvalidAnnMappingFileException();
    	}
    }


	private static SessionOptions parseArgs(String[] args) {
		SessionOptions so = new SessionOptions();
		CmdLineParser parser = new CmdLineParser(so);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -cp CLASSPATH org.tigr.microarray.mev.TMEV [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();
		}

		return so;
	}
    
    public static IResourceManager getResourceManager() {
	    return resourceManager;
    }
    private static void writeProperties() {
    	try {
	        if(mevUserDir.exists()) {
	        	if(mevUserDir.isDirectory() && mevUserDir.canRead()) {
	        		if(mevPropertiesFile.exists() && mevPropertiesFile.canRead()) {
	        	        FileOutputStream propsout = new FileOutputStream(mevPropertiesFile);
	        			props.store(propsout, "MeV Properties");
	        			propsout.close();
	       		 	}
	       	 	} 
	        } else {
		       	mevUserDir.mkdir();
				mevPropertiesFile.createNewFile();
		        FileOutputStream propsout = new FileOutputStream(mevPropertiesFile);
		        props.store(propsout, "MeV Properties");
		        propsout.close();
	        }
    	} catch (IOException ioe) {
    		System.out.println("Cannot store properties to file " + mevPropertiesFile.toString());
    	}
    }
    
    /**
     * Checks for the existence of configuration files in the user.home/.mev directory. 
     * If files exist, leave them alone. Otherwise, read them from classpath and write them 
     * to user directory.
     *
     */
    private static void setupFiles() {
		if(mevUserDir.exists()) {
			if(mevUserDir.isDirectory() && mevUserDir.canRead()) {
				//Get the list of config files from MeV's properties object
	        	String configfilelist = (String)props.getProperty(PROPERTY_CONFIG_FILES);
	        	StringSplitter ss = new StringSplitter(',');
	        	Vector<String> filenames = new Vector<String>();
	        	ss.init(configfilelist);
	        	while(ss.hasMoreTokens()) {
	        		String temp = ss.nextToken();
	        		filenames.add(temp);
	        	}
	        	//for each configuration file, read it from the classpath and copy to the user's .mev directory
	        	for(String filename: filenames) {
	 	        	try {
	 	        		InputStream in = TMEV.class.getClassLoader().getResourceAsStream(filename);
		 	        	if(in != null) {
			        		DataInputStream dis = new DataInputStream(in);
			        		FileOutputStream fos = new FileOutputStream(new File(mevUserDir, filename.substring(filename.lastIndexOf('/')+1)));
			 	        	while(dis.available() != 0){
			 	        		fos.write(dis.readByte());
			 	        	}
			 	        	in.close();
			 	        	fos.close();
		 	        	} 
			        } catch (IOException ioe) {
			        	System.out.println("Couldn't copy file " + filename + " from classpath to " + mevUserDir);
			        	ioe.printStackTrace();
			        }
	        	}
		    }
		}
    }
    
    /**
     * Loads a set of properties including information about the MeV build. Not saved in the user's properties file.
     */
    private static void loadBuildProperties() {
		try {
			InputStream in = TMEV.class.getClassLoader().getResourceAsStream("org/tigr/microarray/mev/build.properties");
			if (in != null) {
				buildProps.load(in); // Can throw IOException
			}
		} catch (IOException ioe) {	}
    }
    
    /**
     * Loads a user properties file from userdir/.mev/mev.properties, if available or loads a default 
     * properties file from the classpath and writes it to userdir/.mev/mev.properties if not. 
     * 
     * @return
     * @throws IOException
     */
    private static void loadProperties() throws IOException {

		props = new ConfMap();
		try {
			InputStream in = TMEV.class.getClassLoader().getResourceAsStream("org/tigr/microarray/mev/default.properties");
			if (in != null) {
				props.load(in); // Can throw IOException
			}
		} catch (IOException ioe) {
			System.out.println("Could not load default properties from org/tigr/microarray/mev/default.properties");
		}

		
		/*		 
		 * Try to get online and download default properties from tm4.org. If not available, skip. 
		 * If available, *override* stored user url locations with new ones from website. 
		 */
//		try {
//			URLConnection conn = new URL(MEV_URL_PROPERTIES_LOCATION).openConnection();
//			InputStream is = conn.getInputStream();
//			props.load(is);
//		} catch (IOException ioe) {
//			System.out.println("Could not download default properties from tm4.org.");
//		}

		/*
		 * load user's properties from mev.properties file, if any. 
		 */
		if (mevUserDir.exists()) {
			if (mevUserDir.isDirectory() && mevUserDir.canRead()) {
				if (mevPropertiesFile.exists() && mevPropertiesFile.canRead()) {
					InputStream in2 = new FileInputStream(mevPropertiesFile);
					props.load(in2);
					in2.close();
				} else {
					mevPropertiesFile.createNewFile();
				}
			}
		} else {
			mevUserDir.mkdir();
			mevPropertiesFile.createNewFile();
		}
		writeProperties();

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
            //FL
            indicesAdjusted = false;
            
            while ((currentLine = reader.readLine()) != null) {
                currentLine.trim();
                if (!(currentLine.startsWith("//") || (currentLine.length() == 0))) {
                    ss = new StringTokenizer(currentLine, "\t");
                    key = ss.nextToken();
					if (ss.hasMoreTokens())
						props.put(key, ss.nextToken());
					else
						props.put(key, new String(""));
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
        } catch (Exception e) {
            e.printStackTrace();
            returnValue = false;
        }
        return returnValue;
    }
    public static String getSettingForOption(String option, String defaultValue) {
	    try {
		    if(!props.containsKey(option))
			    return defaultValue;
		    return (String) props.get(option);
	    } catch (NullPointerException npe) {
		    return defaultValue;
	    }
    }
    public static String getSettingForOption(String option) {
        String setting = "";
        try {
            setting = (String) props.get(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setting;
    }
    public static void storeProperty(String key, String value) {
    	props.setProperty(key, value);
   		writeProperties();

    }
    
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
    
    //TODO maybe this should be moved to IData?
    public static void setNameIndex(int index) {
        TMEV.nameIndex = (index < 0) ? 0 : index;
    }

    public static void setPermitPrompt(boolean permitPrompt) {
        if(TMEV.permitSavePrompt != permitPrompt) {
        	permitSavePrompt = permitPrompt;
        	storeProperty("prompt-for-save", new Boolean(permitPrompt).toString());
        }
    }
    

    /*
     * This code was modified by Jim Johnson with other changes to enable
     * Java Web Start
     */
    public static void configure() {
        ConfMap cfg = props;
        
            try {
                
            String guiFactoryClassName = cfg.getString("gui.factory.class");
           
            if (guiFactoryClassName == null || guiFactoryClassName.equals("null")) {

            	guiFactoryClassName = "org.tigr.microarray.mev.cluster.gui.impl.GUIFactory";
            	System.out.println("GUI Factory class name not found in properties. Using default value.");
            }
            
            Class clazz = Class.forName(guiFactoryClassName);
            guiFactory = (IGUIFactory)clazz.newInstance();
            
            algorithmFactory = new TMEVAlgorithmFactory(cfg);
            
            TMEV.permitSavePrompt = cfg.getBoolean("prompt-for-save", true);
            TMEV.setDataPath(cfg.getProperty(CURRENT_DATA_PATH));

            
            //read the Rserve connection path
            String sPath = cfg.getString( "rserve-path" );
            if( sPath != null && ! sPath.equals("") ) {
                TMEV.rPath = sPath;
            } else {
            	TMEV.rPath = "localhost:6311";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getRPath() {
    	return rPath;
    }
    
    public static String getDataPath() {
		File f = new File(getSettingForOption(CURRENT_DATA_PATH));
		if(!f.isDirectory()) 
			return f.getParent();
    		return getSettingForOption(CURRENT_DATA_PATH);
    }
    
    public static void updateRPath( String rPath ) {
        if(rPath == null)
            return;
        storeProperty("rserve-path", rPath);
    }
    
	/**
	 * Updates the data path in config given a formatted data path string
	 * 
     * @deprecated use TMEV.setDataPath(String newPath)
     */
    public static void updateDataPath(String  newDataPath){
        if(newDataPath == null)
            return;
        setDataPath(newDataPath);
    }
    
    
    public static void setDataPath(String newPath) {
		if(!new File(newPath).isDirectory())
			storeProperty(CURRENT_DATA_PATH, new File(newPath).getParent());
		storeProperty(CURRENT_DATA_PATH, new File(newPath).getAbsolutePath());
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
    
	/**
	 * Returns the configuration file indicated by the fileName argument
     */
    public static File getConfigurationFile(String fileName) {
    	return new File(mevUserDir, fileName);
    }
    
	/**
	 * Returns a file relative to the base directory
     */
    public static File getFile(String fileName) {
        return new File(fileName);
    }
    
	/**
	 * Returns a string representing the OS name
     */
    public static String getOSName() {
        return os;
    }

}
