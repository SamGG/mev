package org.tigr.microarray.mev.cluster.gui.impl.attract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;


public class DataPanel extends org.tigr.microarray.mev.cluster.gui.impl.gsea.DataPanel{
	private AlgorithmData data;
	public DataPanel(IData idata, AlgorithmData algData, JFrame parent,
			ClusterRepository clusterRepository, IFramework framework) {
		super(idata, algData, parent, clusterRepository, framework);
		data=algData;
	}
	
	

}
