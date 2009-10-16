package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import java.awt.Dimension;
import java.awt.Frame;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;

public class NMFProgress extends Progress{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NMFProgress(Frame parent, String title, DialogListener listener) {
		super(parent, title, listener);
		this.setSize(600, 150);
		this.setPreferredSize(new Dimension(600, 150));
		this.setResizable(true);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

        NMFProgress p = new NMFProgress(new Frame(), "Test Progress", null);
        p.show();

	}

}
