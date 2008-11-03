/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.awt.Frame;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.resources.FileDownloader.DownloadProgressListener;

import com.jcraft.jsch.SftpProgressMonitor;

import ftp.FtpObserver;

public class RMProgress extends Progress implements SftpProgressMonitor, FtpObserver {
	DownloadProgressListener dpl;
	Frame parentFrame;
	long count = 0;
	boolean wasCancelled = false;
	String title;
	
	public RMProgress(Frame parent, String title, DownloadProgressListener listener) {
		super(parent, title, listener);
		this.title = title;
		this.dpl = listener;
		this.parentFrame = parent;
		setModalityType(ModalityType.MODELESS);
	}
	
	public boolean count(long increment) {
		this.count += increment;
		setValue(new Long(this.count).intValue());
		update(getGraphics());
		if (wasCancelled) {
			System.out.println("Cancelling...");
			return false;
		}
		return true;
	}

	/**
	 * End only closes this dialog. It does not indicate that the download has been successfully completed.
	 */
	public void end() {
		dispose();
		parentFrame.dispose();
	}

	public void init(int op, String src, String dest, long max) {
		try {
			if (max == -1) {
				setIndeterminate(true);
				show();
			}
			setTitle(title);
			setUnits(new Long(max).intValue());
		} catch (NullPointerException npe) {
			setIndeterminate(true);
		}
		setDescription(src);
		show();
	}
	public void show(){
		super.show();
		update(getGraphics());
	}
	
	public void byteRead(int arg0) {
		count(new Long(arg0));
	}
	
	public void byteWrite(int arg0) {
		count(new Long(arg0));
	}
}
