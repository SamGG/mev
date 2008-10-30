package org.tigr.microarray.mev.resources;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.JOptionPane;

public abstract class FileDownloader {
	protected URL hostURL;
	RMProgress progress;
	int result = 0;
	
	protected FileDownloader(URL host) {
		try {
			this.hostURL = new URL(host.getProtocol(), host.getHost(), host.getPort(), "");
		} catch (MalformedURLException mue) {
		}
	}
	
	public static FileDownloader getInstance(URL host) {
		if (host.getProtocol().startsWith("ftp")) {
			return new FTPFileDownloader(host); 
		} else if (host.getProtocol().startsWith("sftp")) {
			return new SFTPFileDownloader(host);
		} else if (host.getProtocol().startsWith("http")) {
			return new HTTPDownloader(host);
		}
		return null;
	}

	public abstract boolean connect();
	public abstract void disconnect();
	public abstract Date getLastModifiedDate(String path);
	public abstract File getTempFile(String path) throws SupportFileAccessError;
	public abstract String[] getFileList(String path);
	public void destroy() {
		disconnect();
	}
	/**
	 * The class to listen to the dialog.
	 */
	public class DownloadProgressListener extends org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener implements WindowListener  {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
//			System.out.println("command: " + command);
//			new Exception().printStackTrace();

			if (command.equals("cancel-command")) {
				progress.wasCancelled = true;
				System.out.println("Cancelling...");
				result = JOptionPane.CANCEL_OPTION;
				progress.dispose();
//				thisFrame.dispose();
			}
			if (command.equals("window-close-command")) {
				progress.wasCancelled = true;
				System.out.println("Cancelling...");
				result = JOptionPane.CANCEL_OPTION;
				progress.dispose();
//				thisFrame.dispose();
			}
		}

		public void windowClosing(WindowEvent e) {
			progress.wasCancelled = true;
			result = JOptionPane.CANCEL_OPTION;
			System.out.println("closing window");
			progress.dispose();
//			thisFrame.dispose();
		}
	}
}