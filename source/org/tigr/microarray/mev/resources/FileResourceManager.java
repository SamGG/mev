/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.awt.Frame;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationURLsFileDefinition;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEEntrezSupportDataFile;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEImpliesAndURLDataFile;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSet;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.BroadGeneSetList;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GeneSigDbGeneSets;
import org.tigr.microarray.mev.file.FileType;


/**
 * Handles the retrieving and storing of files defined by ISupportFileDefinition
 * implementations. Gets these files from the web and stores them in the file
 * system repository and then gets them out again later, when they are
 * requested. Must be able to #1 get the file from the web given the data in the
 * support file definition, #2 store that file in the file system and #3 get
 * that file from the file system if it is requested again - with a different
 * instance of ISupportFileDefinition.
 * 
 * @author Eleanor
 * 
 */
public class FileResourceManager implements IResourceManager {
	File localRepository;
	private boolean promptToGetOnline = true;
	private boolean allowedOnline = true;
	private Vector<ISupportFileDefinition> filesCheckedThisSession = new Vector<ISupportFileDefinition>();

	private synchronized boolean fileHasBeenCheckedRecently(ISupportFileDefinition def) {
		Enumeration<ISupportFileDefinition> _checkedDefs = filesCheckedThisSession.elements();
		while(_checkedDefs.hasMoreElements()) {
			if(_checkedDefs.nextElement().matches(def)) {
//				System.out.println("file has been checked recently: " + def.getUniqueName());
				return true;
			}
		}
//		System.out.println("file has not been checked recently: " + def.getUniqueName());
		return false;
	}
	
	/**
	 * Creates the ResourceManager in a specific location on the local drive. 
	 * @param repository The location on the local drive where the file repository should be kept
	 * @throws RepositoryInitializationError if there is a problem writing to the specified repository location
	 */
	public FileResourceManager(File repository) throws RepositoryInitializationError {
		//Initialize repository location 
		this.localRepository = repository;
		if (!localRepository.exists()) {
			if (!localRepository.mkdir())
				throw new RepositoryInitializationError("Could not create new Repository in location " + localRepository.getAbsolutePath());
		}
		if (!localRepository.isDirectory() || !localRepository.canRead())
			throw new RepositoryInitializationError("Cannot write to repository at " + localRepository.getAbsolutePath());
		try {
			initializeProperties();
		} catch (NullPointerException npe) {
			//if no properties found, or TMEV is not found, it's ok. Just use defaults.
		}

		//Load additional urls from the web, update them if they have changed from program defaults.
		Properties mevUrls = new Properties();
		try {
			//TODO always check for an update online. Need to implement that forceUpdate().
			File urlPropertiesFile = getSupportFile(new PipelinedUrlsPropertiesDefinition(), true);
			try {
				InputStream in = new FileInputStream(urlPropertiesFile);
				if (in != null) {
					mevUrls.load(in); 
				}
			} catch (IOException ioe) {
				System.out.println("Couldn't get props file from web.");
			}
		} catch (SupportFileAccessError sfae) {
			//No catch needed. ISupportFileDefinition has defaults
		} catch (NullPointerException npe) {
			
		}
		ISupportFileDefinition.addBaseUrls((Hashtable)mevUrls);
		
	}
	
	public synchronized File getSupportFile(ISupportFileDefinition def, boolean getOnline) throws SupportFileAccessError {
		Vector<ISupportFileDefinition> v = new Vector<ISupportFileDefinition>();
		v.add(def);
		File f = getSupportFiles(v, getOnline).get(def);
		if(f == null)
			try {

				throw new SupportFileAccessError("Unable to download support file." + def.getURL());
			} catch (MalformedURLException mue) {
				throw new SupportFileAccessError("Unable to download support file: \n" + def.getUniqueName() + ": " + mue.getStackTrace().toString());
			}
		return f;
	}
	public synchronized Hashtable<ISupportFileDefinition, File> getSupportFiles(Collection<ISupportFileDefinition> defs, boolean getOnline) throws SupportFileAccessError {
		boolean getOnlineFlag = getOnline;
		Hashtable<ISupportFileDefinition, File> returnMap = new Hashtable<ISupportFileDefinition, File>();
		
		//Check each ISupportFileDefinition and remove it from request list if 
		//the file isn't allowed.
		Iterator<ISupportFileDefinition> it = defs.iterator();
		ArrayList<ISupportFileDefinition> toremove = new ArrayList<ISupportFileDefinition>();
		while(it.hasNext()) {
			ISupportFileDefinition isdf = it.next();
			if(!isdf.isAllowed()) {
				toremove.add(isdf);
			}
		}
		it = toremove.iterator();
		while(it.hasNext()) {
			defs.remove(it.next());
		}

		File[] cachedFiles = new File[defs.size()];

		Hashtable<ISupportFileDefinition, File> defsToCachedFiles = new Hashtable<ISupportFileDefinition, File>();

		
		ISupportFileDefinition[] defsarray = defs.toArray(new ISupportFileDefinition[defs.size()]);
		for(int i=0; i<defsarray.length; i++) {
			ISupportFileDefinition def = defsarray[i];
			File cachedFile = null;
			if (fileIsInRepository(def)) {
				try {
					cachedFile = getLatestVersionFromRepository(def);
					defsToCachedFiles.put(def, cachedFile);
					// If the stored file doesn't validate, delete it and allow
					// method to proceed to the downloading stage again.
					if(!def.isValid(cachedFile)) {
						cachedFile.delete();
					} else {
						// If the local file is valid and the definition states
						// that the file is not versioned,
						// there is no need to check online for an update to the
						// file. Return the file as is.
						if(!def.isVersioned() || fileHasBeenCheckedRecently(def)) {
							cachedFiles[i] = cachedFile;
						} else {
							getOnlineFlag = true;
						}
					} 
				} catch (IOException ioe) {
					// Deliberately suppressing an IOException. If there is a
					// problem getting the file from the repository, try
					// getting it from the internet. Do this by ignoring the
					// exception and allowing the code to continue
					// into the segment where it downloads the file from the
					// web.
				}
			}
		}
		
		Hashtable<ISupportFileDefinition, File> downloadedFileMap = new Hashtable<ISupportFileDefinition, File>();
		if(getOnlineFlag) {
			if(shouldAskToGetOnline()) {
			        AllowConnectionsDialog dialog = new AllowConnectionsDialog(new JFrame());
			        setAllowedOnline((dialog.showModal() == JOptionPane.YES_OPTION));
			        setAskToGetOnline(dialog.askAgain());
			}
			if(isAllowedOnline()) {
				try {
					downloadedFileMap = getUpdatesIfAvailable(new Vector<ISupportFileDefinition>(defs), defsToCachedFiles);
				} catch (SupportFileAccessError sfae) {
					//TODO suppress and return existing file?
				}
			}
		} 
		
		Enumeration<ISupportFileDefinition> allDefs = new Vector<ISupportFileDefinition>(defs).elements();
		while(allDefs.hasMoreElements()) {
			ISupportFileDefinition thisDef = allDefs.nextElement();
			File thisDefFile;
			if(downloadedFileMap.containsKey(thisDef)) {
				thisDefFile = downloadedFileMap.get(thisDef);
				returnMap.put(thisDef, thisDefFile);
			} else if(defsToCachedFiles.containsKey(thisDef)) {
				thisDefFile = defsToCachedFiles.get(thisDef);
				returnMap.put(thisDef, thisDefFile);
			}
		}
		return returnMap;
		
	}

	private boolean shouldCheckForUpdate(ISupportFileDefinition def) {
		
		return false;
	}
	
	private synchronized Hashtable<ISupportFileDefinition, File> getUpdatesIfAvailable(Vector<ISupportFileDefinition> defs, Hashtable<ISupportFileDefinition, File> defToCachedFileMap) throws SupportFileAccessError {
		Hashtable<ISupportFileDefinition, File> tempMap = new Hashtable<ISupportFileDefinition, File>();
		
		//hash defs on url host+protocol 
		Hashtable<URL, Vector<ISupportFileDefinition>> hostsHash = new Hashtable<URL, Vector<ISupportFileDefinition>>();
		Enumeration<ISupportFileDefinition> allDefs = defs.elements();
		while(allDefs.hasMoreElements()) {
			ISupportFileDefinition thisDef = allDefs.nextElement();
			if(!fileHasBeenCheckedRecently(thisDef)) {
				try {
					URL tempURL = thisDef.getURL();
					URL host = new URL(tempURL.getProtocol(), tempURL.getHost(), tempURL.getPort(), "");
					if(hostsHash.containsKey(host)) {
						hostsHash.get(host).add(thisDef);
					} else {
						Vector<ISupportFileDefinition> temp = new Vector<ISupportFileDefinition>();
						temp.add(thisDef);
						hostsHash.put(host, temp);
					}
				} catch(MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
		}
		
		//For each group of URLs (one group per protocol:host , get the cached dates, 
		//create a FileDownloader and download all the files if they are newer than the cached ones.
		Enumeration<URL> allHosts = hostsHash.keys();
		
		while(allHosts.hasMoreElements()) {
			URL thishost = allHosts.nextElement();

			FileDownloader fd = FileDownloader.getInstance(thishost);

			boolean connected = false;
			try {
				connected = fd.connect();
			} catch (IOException ioe) {
				//connection failed, 
				connected = false;
			} catch (NullPointerException npe) {
				connected = false;
			}
			if(connected) {
				Vector<ISupportFileDefinition> filesForThisHost = hostsHash.get(thishost);
				for(int i=0; i<filesForThisHost.size(); i++) {
					Date cachedDateForThisDef;
					URL thisDefURL = null;
					ISupportFileDefinition thisDef = filesForThisHost.get(i);
					File cachedFile = defToCachedFileMap.get(thisDef);
					if(cachedFile == null || !cachedFile.exists()) {
						cachedDateForThisDef = null;
					} else {
						try {
							cachedDateForThisDef = getDateFromFilename(cachedFile, thisDef);
						} catch (ParseException pe) {
							cachedDateForThisDef = new Date(0);
						}
					}
					
					try {
						thisDefURL = thisDef.getURL();
						Date lastModifiedDate = fd.getLastModifiedDate(thisDefURL.getPath());
						//if the locally cached data file doesn't have a cache date or the cache date 
						//is earlier than the server's lastModifiedDate, download the file and store it locally
						if(cachedDateForThisDef == null || roundedCompare(cachedDateForThisDef, lastModifiedDate)) {
							String path = thisDefURL.getPath();
							String query = thisDefURL.getQuery();
							if (query != null && query.length() > 0)
 								path += "?" + query;
	 						File f = fd.getTempFile(path);
							if(fd.wasCancelled()) {
								f = null;
							}
							if(f != null && f.exists()) {
								File finalFile = validateAndCopyToRepository(f, thisDef, lastModifiedDate);
								if (finalFile != null) {
									tempMap.put(thisDef, finalFile);
									filesCheckedThisSession.add(thisDef);
								}
							} else {
								SupportFileAccessError sfae = new SupportFileAccessError("File was not downloaded.");
								ShowThrowableDialog.show(null, "File not found", sfae);
							}
							//If the local cached version of the file is up-to-date, just store a reference to the
							//file definition to note that it's been checked on lately.
						} else {
							filesCheckedThisSession.add(thisDef);
						}
							
					} catch (MalformedURLException mue) {
						thisDefURL = null;
					}
				}
			} else {
				//Could not connect.
				Vector<ISupportFileDefinition> filesForThisHost = hostsHash.get(thishost);
				for(int i=0; i<filesForThisHost.size(); i++) {
					Date cachedDateForThisDef;
					URL thisDefURL = null;
					ISupportFileDefinition thisDef = filesForThisHost.get(i);
					File cachedFile = defToCachedFileMap.get(thisDef);
					if(cachedFile == null || !cachedFile.exists()) {
						
						//TODO add error window, allow user to select local file from file chooser
						//if they choose to do so, 
							//show window with url.
						JFileChooser chooser = new JFileChooser("");
						try {
							if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
								File selectedFile = chooser.getSelectedFile();
								thisDefURL = new URL("file://" + selectedFile.getAbsolutePath());
								FileDownloader fed = FileDownloader.getInstance(thisDefURL);
								Date lastModifiedDate = fed.getLastModifiedDate(selectedFile.getAbsolutePath());
								File f = fed.getTempFile(selectedFile.getCanonicalPath());
	
								if(f != null && f.exists()) {
									File finalFile = validateAndCopyToRepository(f, thisDef, lastModifiedDate);
									if (finalFile != null) {
										tempMap.put(thisDef, finalFile);
										filesCheckedThisSession.add(thisDef);
									}
								} else {
									SupportFileAccessError sfae = new SupportFileAccessError("File was not downloaded.");
									ShowThrowableDialog.show(null, "File not found", sfae);
								}
	
							} else {
								SupportFileAccessError sfae = new SupportFileAccessError("File not selected ");			
								throw sfae;
							}
						} catch (IOException ioe) {
							SupportFileAccessError sfae = new SupportFileAccessError(ioe);			
							throw sfae;
						}
					} else {
						SupportFileAccessError sfae = new SupportFileAccessError("Couldn't connect to server " + fd.hostURL);			
						throw sfae;
					}
				}
			}
		}
		return tempMap;
	}

	/**
	 * Store File temporaryDownloadedFile to the file system. Use def.getUniqueName() and serverLastModifiedDate to create a datestamped name for the file.
	 * 
	 * @param temporaryDownloadedFile the file to be stored to the repository
	 * @param def the file definition of temporaryDownloadedFile
	 * @param serverLastModifiedDate the last modified date of temporaryDownloadedFile on the server it came from
	 * @return the file handle of the newly-copied file in the repository
	 * @throws SupportFileAccessError if an error in copying occurs.
	 */
	private synchronized File validateAndCopyToRepository(File temporaryDownloadedFile, ISupportFileDefinition def, Date serverLastModifiedDate) throws SupportFileAccessError {
		try {
			File temporaryUncompressedFile = null;
			if (def.fileNeedsUnzipping())
				temporaryUncompressedFile = unzipFile(temporaryDownloadedFile);
			else
				temporaryUncompressedFile = temporaryDownloadedFile;
	
			if (def.isValid(temporaryUncompressedFile)) {
				try {
					File temp = createFileHandleInRepository(def, new Date(0));
					if(temp.exists())
						temp.delete();
					File newlyCachedFile = copyFileToRepository(temporaryUncompressedFile, def, serverLastModifiedDate);
					return newlyCachedFile;
				} catch (Exception e) {
					//There was a problem copying this file to the repository, though the file is valid and stored in a temporary location. 
					//Pop up a warning window that the user can see, so they know that their data isn't being stored and they'll
					//have to download their data again each time. Then return the tempfile.
					String friendlyMessage =
						"There was a problem storing your downloaded file to the local hard drive." +
						"The file is stored in a temporary location and can be used normally, \n" +
						"but was unable to be copied to MeV's permanent repository for later use. ";
					SupportFileAccessError sfae = new SupportFileAccessError("File could not be stored.", e);
					ShowThrowableDialog.show(new Frame(), "", true, 2, sfae, friendlyMessage);
					return temporaryUncompressedFile;
				}
			} else {
				String friendlyMessage = 
					"The downloaded file was corrupted. This could be a one-time problem or " +
					"could represent a problem with the file on the webserver. If this problem continues, " +
					"please contact the MeV development team at http://www.tm4.org/forum.";
				 SupportFileAccessError sfae = new SupportFileAccessError(
							"Downloaded file was corrupt.");
				ShowThrowableDialog.show(new Frame(), "Corrupted File", true, 0, sfae, friendlyMessage);
				throw sfae;
			}
			
		} catch (IOException ioe) {
			String friendlyMessage = "The selected file was not found. " +
					"The internet connection may have been interrupted during download.";
			 SupportFileAccessError sfae = new SupportFileAccessError(
						"There was a problem with the downloaded file",
						ioe);
			ShowThrowableDialog.show(new Frame(), "Internet Connectivity Problem", true, 0, sfae, friendlyMessage);
			throw sfae;
		} finally {
			//TODO add any cleanup items here.
		}
	}

	
	/**
	 * Compares two dates and returns true if the first date is before the second date.
	 * Compares only at the day level, returns false for dates with the same day but
	 * different times. 
	 * @param firstDate the first date to compare
	 * @param secondDate the second date to compare
	 * @return true if the first date, when rounded to the earliest day, is earlier than the second date, also rounded to the earliest day
	 */
	protected boolean roundedCompare(Date firstDate, Date secondDate) {
		if(firstDate == null || secondDate == null) {
			return false;
		}
		Calendar dateOne = Calendar.getInstance();
		Calendar dateTwo = Calendar.getInstance();
		dateOne.setTime(firstDate);
		dateTwo.setTime(secondDate);
		if(dateOne.get(Calendar.YEAR) < dateTwo.get(Calendar.YEAR)) {
			return true;
		}
		if(dateOne.get(Calendar.MONTH) < dateTwo.get(Calendar.MONTH)) {
			return true;
		}
		if(dateOne.get(Calendar.DAY_OF_MONTH) < dateTwo.get(Calendar.DAY_OF_MONTH)) {
			return true;
		}
		return false;
	}

	/**
	 * Return a Hashtable containing the ISupportFileDefinition objects mapped to their corresponding files, as defined in def
	 * and selected by the user. This method first connects to the internet using the url provided by def.getURL(), gets a list of files
	 * that are indexed at that location, and presents that list of filenames in a window for the user to select from. Those files selected
	 * by the user are then downloaded and returned in a hashtable. 
	 * 
	 * @param def a definition of the type of file to be selected and downloaded
	 * @return a Hashtable mapping the results
	 */
	public synchronized Hashtable<ISupportFileDefinition, File> getMultipleSupportFiles(IMultiSupportFileDefinition def) throws SupportFileAccessError {
		//For testing
		try {
			URL url = def.getURL();
			FileDownloader fd = FileDownloader.getInstance(url);
			String[] filenames;
			if(fd.connect()) {
				filenames = fd.getFileList(url.getPath());
				fd.disconnect();
			} else {
				SupportFileAccessError sfae = new SupportFileAccessError("Could not connect to " + url.toString());
				ShowThrowableDialog.show(new Frame(), "Could not connect to "+ url.toString(), true, 0, sfae);
				throw sfae;
			}
			
			if(filenames != null && filenames.length >0) {
				SelectMultiFilesDialog dialog = new SelectMultiFilesDialog(new JFrame(), "Select files to download", def.getURL().getHost(), filenames);
				dialog.setVisible(true);
				
				int[] indices = dialog.getSelectedFilesIndices();
				String[] selectedFiles = new String[indices.length];
				for(int i=0; i<indices.length; i++) {
					selectedFiles[i] = filenames[indices[i]];
				}
				
				Collection<ISupportFileDefinition> defs = def.getFileDefinitions(selectedFiles);
				
				return getSupportFiles(defs, true);
			} else {
				SupportFileAccessError sfae = new SupportFileAccessError("Unable to get a list of files from the location " + url.toString());
				ShowThrowableDialog.show(new Frame(), "Unable to get a list of files from the location "+ url.toString(), true, 0, sfae);
				throw sfae;
			}
			
		} catch (MalformedURLException mue) {
			 SupportFileAccessError sfae = new SupportFileAccessError("Malformed url in requested file", mue);
			ShowThrowableDialog.show(new Frame(), "Malformed url in requested file", true, 0, sfae);
			throw sfae;
		} catch (IOException ioe) {
			 SupportFileAccessError sfae = new SupportFileAccessError(ioe);
			ShowThrowableDialog.show(new Frame(), ioe.getMessage(), true, 0, sfae);
			throw sfae;
		}
	}

	/**
	 * Returns true if the file described by def is already stored in the
	 * local repository.
	 */
	public synchronized boolean fileIsInRepository(ISupportFileDefinition def) {
		try {
			if(getLatestVersionFromRepository(def) == null)
				return false;
			return getLatestVersionFromRepository(def).exists() && getLatestVersionFromRepository(def).canRead();
		} catch (IOException ioe) {
			return false;
		}
	}


	/**
	 * Unzips zippedFile into a temporary directory and returns that
	 * directory.
	 * 
	 * @param zippedFile
	 *                The file to be unzipped.
	 * @return A filehandle containing the directory containing the unzipped
	 *         file
	 * @throws ZipException
	 *                 if the zipped file is unable to be zipped
	 * @throws IOException
	 *                 if there is a problem writing to the temp directory
	 */
	private synchronized File unzipFile(File zippedFile) throws ZipException, IOException {
		if (zippedFile == null || !zippedFile.exists()) {
			System.out.println("couldn't find file to unzip");
			throw new IOException("Could not find file to unzip.");
		} 
		File unzipDir = File.createTempFile("mev_resource_unzip", "");
		unzipDir.deleteOnExit();

		if (!unzipDir.delete())
			throw new IOException();
		if (!unzipDir.mkdir())
			throw new IOException();

		ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedFile));
		int len;
		byte[] buf = new byte[1024];
		ZipEntry entry;

		while ((entry = zis.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				continue;
			}

			String entryName = entry.getName();
			String entryFolder = (new File(entryName)).getParent();
			if (entryFolder == null)
				entryFolder = "";
			File entryDirectory = new File(unzipDir, entryFolder);

			if (!entryDirectory.exists()) {
				entryDirectory.mkdirs();
			}

			File outFile = new File(unzipDir, entry.getName());
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(outFile));
			while ((len = zis.read(buf)) > 0) {
				dos.write(buf, 0, len);
			}
			dos.close();
			zis.closeEntry();
		}
		zis.close();

		return unzipDir;
	}


	/**
	 * Checks the repository for the most recent cached file defined by def. If def.isVersioned returns false, 
	 * this method simply returns the canonically-named file. If true, however, this method will find all
	 * versions of this file in the repository and return the one with the most recent datestamp in the filename.
	 * @param def the ISupportFileDefinition whose file the method is to check for. 
	 * @return the most recently-datestamped File in the repository that matches def.  
	 * @throws IOException if a problem occurrs when accessing the repository.
	 */
	private synchronized File getLatestVersionFromRepository(ISupportFileDefinition def) throws IOException {
		if(!def.isVersioned()) {
			return createFileHandleInRepository(def, new Date(0));
		} else {
			File supportFileDirectory = new File(localRepository, def.getClass().getName());
			if (!supportFileDirectory.exists()) {
				supportFileDirectory.mkdir();
			} else {
				if (!supportFileDirectory.isDirectory() || !supportFileDirectory.canRead())
					throw new IOException("Couldn't write to support file directory " + supportFileDirectory.getAbsolutePath());
			}

			File[] versions = supportFileDirectory.listFiles(def);
			File latestFile = null;
			Date newest = new Date(0);
			for (int i=0; i<versions.length; i++) {
				Date d;
				try {
					d = getDateFromFilename(versions[i], def);
				} catch (ParseException pe) {
					d = new Date(0);
				}
				if(d.after(newest))
					newest = d;
				latestFile = versions[i];
			}
			return latestFile;
		}
	}
	/**
	 * Returns a Date object embedded in new name of file f, of type def. Presumes that 
	 * the date string immediately follows the string returned by {@link def.getUniqueName()} and
	 * immediately precedes a dot and file type extension, if there is one. If no date can be parsed
	 * from the filename as stated, returns a Date(0). 
	 * @param f
	 * @param def
	 * @return
	 */
	protected static Date getDateFromFilename(File f, ISupportFileDefinition def) throws ParseException {
		String filename = f.getName();
		String uniqueName = def.getUniqueName();
		String prefix = "";
		String dateString = "";
		if(uniqueName.lastIndexOf('.') > 0) {
			prefix = uniqueName.substring(0, uniqueName.lastIndexOf('.'));
		} else {
			prefix = uniqueName;			
		}
		if(filename.lastIndexOf('.') > 0)
			dateString = filename.substring(prefix.length(), filename.lastIndexOf('.'));
		else 
			dateString = filename.substring(prefix.length());
			return stringToDate(dateString);
		}
	/**
	 * Creates a datestamped file name for a file of type def, with date d.
	 * @param def
	 * @param d
	 * @return
	 */
	private String createDatedFileName(ISupportFileDefinition def, Date d) {
		String uniqueName = def.getUniqueName();
		String prefix="";
		String suffix="";
		//If the file name has a .txt or other extension, split on the last dot in the filename.
		if(uniqueName.lastIndexOf('.') > 0 ) {
			prefix = uniqueName.substring(0, uniqueName.lastIndexOf('.'));
			suffix = uniqueName.substring(uniqueName.lastIndexOf('.'));
		} else {
			prefix = uniqueName;
			suffix = "";
		}
		return prefix + dateToString(d) + suffix;
	}
	
	private static Date stringToDate(String s) throws ParseException {
		DateFormat df = new SimpleDateFormat("_yyyy-MM-dd", new Locale("en"));
		return df.parse(s);
	}
	
	private String dateToString(Date d) {
		DateFormat df = new SimpleDateFormat("_yyyy-MM-dd", new Locale("en"));
		String temp = df.format(d);
		return temp;
	}


	/**
	 * Creates a file in the Repository where a new file can be copied.
	 * Creates any necessary directories.
	 * 
	 * @param def
	 * @return
	 * @throws IOException
	 */
	private synchronized File createFileHandleInRepository(ISupportFileDefinition def, Date d) throws IOException {
		if (d == null)
			d = new Date(0);
		File supportFileDirectory = new File(localRepository, def.getClass().getName());
		if (!supportFileDirectory.exists()) {
			supportFileDirectory.mkdir();
		} else {
			if (!supportFileDirectory.isDirectory() || !supportFileDirectory.canRead())
				throw new IOException("Couldn't write to support file directory " + supportFileDirectory.getAbsolutePath());
		}
		File supportFile;
		if(def.isVersioned())
		supportFile = new File(supportFileDirectory, createDatedFileName(def, d));
		else
			supportFile = new File(supportFileDirectory, def.getUniqueName());
		return supportFile;
	}

	/**
	 * Copy the file in unzippedDir to the appropriate place in the
	 * repository as described by def. If def describes a multi-file,
	 * recursively copy the contents of unzippedDir to the repository in a
	 * folder named as specified by def.
	 * 
	 * @param unzippedDir
	 *                The directory containing the file or files to be
	 *                copied.
	 * @param def
	 *                the support file definition that describes where the
	 *                files should be copied and what to name them.
	 */
	private synchronized File copyFileToRepository(File unzippedDir, ISupportFileDefinition def, Date d) throws IOException {
		File f = createFileHandleInRepository(def, d);
		
		//TODO should files with no date be created with no date in filename? 
		assert !f.exists(): "File " + f.getAbsolutePath() + " should not exist.";
		
		if(!def.fileNeedsUnzipping()) {
			copyFiles(unzippedDir, f);
			return f;
		}
		if (def.isSingleFile()) {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				File singleFile = unzippedDir.listFiles()[0];
				fis = new FileInputStream(singleFile);
				fos = new FileOutputStream(f);
				byte[] buf = new byte[1024];
				int len;
				while ((len = fis.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fis.close();
				fos.close();
			} finally {
				try {
					if (fos != null)
						fos.close();
					if (fis != null)
						fis.close();
				} catch (IOException ioe) {
					//Doesn't matter if these are null - this is just a cleanup block.
				}
			}

			//f is a directory and must be copied recursively
		} else {
			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				f.mkdir();
				copyFiles(unzippedDir, f);
			}  finally {
				try {
					if (fos != null)
						fos.close();
					if (fis != null)
						fis.close();
				} catch (IOException ioe) {
					//Doesn't matter if these are null - this is just a cleanup block.
				}
			}
		}
		return f;
	}


	/**
	 * This function will copy files or directories from one location to
	 * another. note that the source and the destination must be mutually
	 * exclusive. This function can not be used to copy a directory to a sub
	 * directory of itself. The function will also have problems if the
	 * destination files already exist.
	 * 
	 * @param src --
	 *                A File object that represents the source for the copy
	 * @param dest --
	 *                A File object that represnts the destination for the
	 *                copy.
	 * @throws IOException
	 *                 if unable to copy.
	 */
	private synchronized static void copyFiles(File src, File dest) throws IOException {
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
		}
		//is this a <b style="color:black;background-color:#99ff99">directory</b> <b style="color:black;background-color:#a0ffff">copy</b>?
		if (src.isDirectory()) {
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//<b style="color:black;background-color:#a0ffff">copy</b> all the files in the list.
			for (int i = 0; i < list.length; i++) {
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1, dest1);
			}
		} else {
			//This was not a <b style="color:black;background-color:#99ff99">directory</b>, so lets just <b style="color:black;background-color:#a0ffff">copy</b> the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				//open the files for input and output
				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) { //Error copying file... 
				IOException wrapper = new IOException("unable to copy file from " + src.getAbsolutePath() + " to " + dest.getAbsolutePath()
						+ ".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) {
					fin.close();
				}
				if (fout != null) {
					fin.close();
				}
			}
		}
	}




	/**
	 * In practice, this method is never used. It's left here in case we need it in the future.
	 */
	public boolean checkForUpdate(ISupportFileDefinition def) {
		return false;
	}
	
	private void initializeProperties() {
		setAllowedOnline(new Boolean(TMEV.getSettingForOption(TMEV.ALLOWED_ONLINE, "true")));
		setAskToGetOnline(new Boolean(TMEV.getSettingForOption(TMEV.PROMPT_TO_GET_ONLINE, "true")));
	}
	private boolean isAllowedOnline() {
		return allowedOnline;
	}
	private boolean shouldAskToGetOnline() {
		return promptToGetOnline;
	}
	private void setAllowedOnline(boolean b) {
		allowedOnline = b;
		try {
			TMEV.storeProperty(TMEV.ALLOWED_ONLINE, new Boolean(allowedOnline).toString());
		} catch (NullPointerException npe) {
			//
		}
	}
	public void setAskToGetOnline(boolean b) {
		promptToGetOnline = b;
		try {
			TMEV.storeProperty(TMEV.PROMPT_TO_GET_ONLINE, new Boolean(promptToGetOnline).toString());
		} catch (NullPointerException npe) {
			//
		}
	}
	protected void finalize() throws Throwable {
	}

	/**
	 * Example code for GSEA downloads
	 */
	private void testGSEADownloads() {
		try {
			//Get the file containing the list of available geneset files.
			File geneSetList = getSupportFile(new BroadGeneSetList(), true);
			try {
				//Parse the list of geneset files into filename strings
				ArrayList<String> genesetFilenames = BroadGeneSetList.getFileNames(geneSetList);

				//get email address from user
				String email = "whatever@wherever.com";
				String genesetFileName;				
				
				ArrayList<ISupportFileDefinition> defs = new ArrayList<ISupportFileDefinition>();
				Iterator<String> it = genesetFilenames.iterator();
				//Add each geneset file definition to the container
				while(it.hasNext()) {
					genesetFileName = it.next();
					//Create a definition for each geneset file
					defs.add(new BroadGeneSet(genesetFileName, email));
				}
				//Ask the resource manager to download a file for each definition
				Hashtable<ISupportFileDefinition, File> results = getSupportFiles(defs, true);
				
				//Check each file for validity, print a list of the valid downloaded files
				Enumeration<ISupportFileDefinition> e = results.keys();
				while(e.hasMoreElements()) {
					ISupportFileDefinition thisDef = e.nextElement();
					File temp = results.get(thisDef);
					if(thisDef.isValid(temp))
						System.out.println("support file downloaded correctly: " + temp.getAbsolutePath());
					else 
						System.out.println("support file not downloaded " + temp.getAbsolutePath());
				}
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
		} catch (SupportFileAccessError sfae) {
			sfae.printStackTrace();
		}
		try {
			
			GeneSigDbGeneSets temp = new GeneSigDbGeneSets();
			File geneSigs = getSupportFile(temp, true);
			if(temp.isValid(geneSigs)) {
				System.out.println("GeneSigDb download file is valid.");
	}
	
		} catch (SupportFileAccessError sfae) {
			System.out.println("Could not download GeneSigDbGeneSets file.");
		}
	}
	
	public static void main(String[] args) {
		FileResourceManager frm;

		//test creation of FileResourceManager
		try {
			frm = new FileResourceManager(new File(new File(System.getProperty("user.home"), ".mev"), "repository"));
		} catch (RepositoryInitializationError rie) {
			rie.printStackTrace();
			return;
		}
		
		//Test creation of support file definitions
		Vector<ISupportFileDefinition> defs = new Vector<ISupportFileDefinition>();
//		defs.add(new ResourcererAnnotationFileDefinition("C.elegans", "affy_Celegans"));
//		defs.add(new SupportFileUrlsPropertiesDefinition());
//		defs.add(new EASEImpliesAndURLDataFile());
//		defs.add(new ExpressionDataSupportDataFile("http://www.tm4.org/webstart/mev/TDMS_format_sample.txt", false, FileType.STANFORD));
		defs.add(new ResourcererAnnotationFileDefinition("Human", "APPLERA_ABI1700"));
//		defs.add(new ResourcererAnnotationFileDefinition("Human", "affy_HG-U133A"));
//		defs.add(new ResourcererAnnotationFileDefinition("Rat", "Agilent_RatOligo"));
//		defs.add(new ResourcererAnnotationFileDefinition("Sugar_Cane", "affy_Sugar_Cane"));
//		defs.add(new EASEEntrezSupportDataFile("Sugar_Cane", "affy_Sugar_Cane"));
//		defs.add(new AnnotationURLsFileDefinition());
//		defs.add(new GseaSupportDataFile("c1.all.v2.5.symbols.gmt"));
//		defs.add(new GseaSupportDataFile("c2.v2.symbols.gmt"));
//		defs.add(new AvailableAnnotationsFileDefinition());

		Hashtable<ISupportFileDefinition, File> results = null;
		try {
			results = frm.getSupportFiles(defs, true);
		} catch (SupportFileAccessError sfae) {
			sfae.printStackTrace();
		}

		if(results == null) {
			System.out.println("No results returned from getSupportFiles(defs, true)");
			System.exit(0);
		} 
		
		Enumeration<ISupportFileDefinition> e = defs.elements();
		while(e.hasMoreElements()) {
			ISupportFileDefinition thisDef = e.nextElement();
			if(thisDef == null)  {
				System.out.println("null support definition");
			} else {
				File f = results.get(thisDef);
				if (f == null)
					System.out.println("file for def " + thisDef.getUniqueName() + " was not found");
				else
					System.out.println("Found file " + thisDef.getUniqueName());
			}
		}
		frm.testGSEADownloads();
		
		//Test retrieving of definitions
		Enumeration<ISupportFileDefinition> temp = defs.elements();
		ISupportFileDefinition def;
		while (temp.hasMoreElements()) {
			def = temp.nextElement();
	
			try {
				try {
					File f = frm.getLatestVersionFromRepository(def);
					if(f != null)
						System.out.println(f.getAbsolutePath());
					else 
						System.out.println("File not found in repository.");
				} catch (IOException ioe) {
					ioe.printStackTrace();
					return;
				}
				File f = frm.getSupportFile(def, true);
				if(!f.exists())
					System.out.println(f.getAbsolutePath() + " does not exist.");
				if(!f.canRead())
					System.out.println(f.getAbsolutePath() + " cannot be read.");
			} catch (SupportFileAccessError sfae) {
				sfae.printStackTrace();
			}
		}
		
		//MultiSupportFile testing code. 
//		IMultiSupportFileDefinition mdef = new GseaMultiSuppFileDefinition();
//		try {
//			Hashtable<ISupportFileDefinition, File> supportfilesHash = frm.getMultipleSupportFiles(mdef);
//			Enumeration<ISupportFileDefinition> supportfiles = supportfilesHash.keys();
//			System.out.println("FileResourceManager.getMultipleSupportFiles returned " + supportfilesHash.size() + " entries.");
//			while(supportfiles.hasMoreElements()) {
//				ISupportFileDefinition thisDef = supportfiles.nextElement();
//				//Must check for null - ResourceManager fails quietly if one of the files can't be found, and doesn't populate the 
//				//hashtable. 
//				if(supportfilesHash.containsKey(thisDef) && supportfilesHash.get(thisDef)!= null) {
//					System.out.println("got file " + supportfilesHash.get(thisDef).getAbsolutePath() + " for definition " + thisDef.getUniqueName());
//				} else {
//					System.out.println("Didn't get file for definition " + thisDef.getUniqueName());
//				}
//			}
//		} catch(SupportFileAccessError sfae) {
//			sfae.printStackTrace();
//		}
		
		System.out.println("FileResourceManager finished.");
		System.exit(0);
	}

}
