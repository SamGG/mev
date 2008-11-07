/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.tigr.microarray.mev.resources.FileDownloader;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * This class is not finished. It hasn't been fully coded since MeV doesn't use any sftp connections yet. 
 * @author Eleanor
 *
 */
public class SFTPFileDownloader extends FileDownloader {
	JSch jsch;
	Session session;
	String host;
	String user;
	UserInfo ui;
	int port;
	Channel channel;
	ChannelSftp c;
	protected SFTPFileDownloader(URL host) {
		super(host);
		jsch = new JSch();
	}

	@Override
	public boolean connect() throws IOException {
		try {
			user = "test";
			//TODO write dialog to request username
			host = hostURL.getHost();
			port = 22;
			session = jsch.getSession(user, host, port);
			ui = new MyUserInfo();
			session.setUserInfo(ui);
	
			session.connect();
	
			channel = session.openChannel("sftp");
			channel.connect();
			c = (ChannelSftp) channel;
			
			return true;
		} catch (JSchException jse) {
			return false;
		} finally {
			//progress.dispose();
		}
	}
	@Override
	public void disconnect() {
		c.quit();
		session.disconnect();
	}
	@Override
	public String[] getFileList(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Date getLastModifiedDate(String path) {
		Vector<LsEntry> myfile;
		try {
			myfile = c.ls(hostURL.getFile());
		} catch (SftpException e) {
			e.printStackTrace();
			return null;
		}

		Date serverDateLastModified;
		if(myfile != null) {
			LsEntry obj = myfile.get(0);
			SftpATTRS attrs = ((com.jcraft.jsch.ChannelSftp.LsEntry) obj).getAttrs();
			serverDateLastModified = new Date(attrs.getMTime());
			return serverDateLastModified;
		} else {
			return null;
		}
	}
	@Override
	public File getTempFile(String path) throws SupportFileAccessError {
		// TODO Auto-generated method stub
		return null;
	}
	//TODO only download file if d is older than the date on server.lastmodified.
	private File getJschFTPFile(URL url, Date cachedDateLastModified) throws IOException, JSchException, SftpException, SupportFileAccessError {
		JSch jsch = new JSch();
		String user = "eleanora";
		//TODO write dialog to request username
		String host = url.getHost();
		int port = 22;
		Session session = jsch.getSession(user, host, port);
		UserInfo ui = new MyUserInfo();
		session.setUserInfo(ui);

		session.connect();

		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;
		

		c.cd(url.getPath().substring(0, url.getPath().lastIndexOf('/') + 1));

		SftpProgressMonitor monitor;
		monitor = this.progress;
		
		//Get the last modified date for the requested file from the server
		Vector<LsEntry> myfile = c.ls(url.getFile());

		Date serverDateLastModified;
		if(myfile != null) {
			LsEntry obj = myfile.get(0);
			SftpATTRS attrs = ((com.jcraft.jsch.ChannelSftp.LsEntry) obj).getAttrs();
			serverDateLastModified = new Date(attrs.getMTime());
		} else {
			//TODO should the default be today's date or date(0)?
			serverDateLastModified = new Date(0);
		}
		
		boolean downloadFile;
		
		if(cachedDateLastModified == null || cachedDateLastModified.before(serverDateLastModified)) 
			downloadFile = true;
		else
			downloadFile = false;
		
		File newFile;

		if(downloadFile) {
			newFile  = File.createTempFile("mev_resource", "");
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
			c.get(url.getFile(), bos, monitor);
			bos.close();
//			this.serverLastModifiedDate = serverDateLastModified;
		} else {
			newFile = null;
		}
		
		c.quit();
		session.disconnect();
		
		if(progress.wasCancelled) {
			SupportFileAccessError sfae = new SupportFileAccessError("Connection was cancelled by user.");
			sfae.setCancelledConnection(true);
			throw sfae;
		}
		return newFile;
	}

	public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		public String getPassword() {
			return passwd;
		}

		public boolean promptYesNo(String str) {
			Object[] options = { "yes", "no" };
			int foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[0]);
			return foo == 0;
		}

		String passwd;
		JTextField passwordField = (JTextField) new JPasswordField(20);

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			Object[] ob = { passwordField };
			int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				passwd = passwordField.getText();
				return true;
			} else {
				return false;
			}
		}

		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}

		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0,
				0, 0), 0, 0);
		private Container panel;

		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for (int i = 0; i < prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]), gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if (echo[i]) {
					texts[i] = new JTextField(20);
				} else {
					texts[i] = new JPasswordField(20);
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}

			if (JOptionPane.showConfirmDialog(null, panel, destination + ": " + name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
				String[] response = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					response[i] = texts[i].getText();
				}
				return response;
			} else {
				return null; // cancel
			}
		}
	}

}
