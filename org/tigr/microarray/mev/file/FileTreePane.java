/*

Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).

All rights reserved.

*/

/*

 * $RCSfile: FileTreePane.java,v $

 * $Revision: 1.2 $

 * $Date: 2004-02-17 14:40:32 $

 * $Author: braisted $

 * $State: Exp $

 */



package org.tigr.microarray.mev.file;



import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Graphics;



import java.awt.Toolkit;

import java.io.File;

import java.util.Hashtable;

import java.util.Vector;



import javax.swing.DefaultListModel;

import javax.swing.Icon;

import javax.swing.ImageIcon;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JList;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTree;

import javax.swing.SwingUtilities;

import javax.swing.UIManager;



import javax.swing.event.ListSelectionEvent;

import javax.swing.event.ListSelectionListener;

import javax.swing.event.TreeExpansionEvent;

import javax.swing.event.TreeExpansionListener;

import javax.swing.event.TreeSelectionEvent;

import javax.swing.event.TreeSelectionListener;



import javax.swing.tree.DefaultMutableTreeNode;

import javax.swing.tree.DefaultTreeModel;

import javax.swing.tree.TreeCellRenderer;

import javax.swing.tree.TreePath;

import javax.swing.tree.TreeSelectionModel;



public class FileTreePane extends JPanel {


	public final static ImageIcon ICON_COMPUTER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(FileTreePane.class.getClassLoader().getResource("org/tigr/images/PCIcon.gif")));

	public final static ImageIcon ICON_DISK = new ImageIcon(Toolkit.getDefaultToolkit().getImage(FileTreePane.class.getClassLoader().getResource("org/tigr/images/disk.gif")));

        public final static ImageIcon ICON_FOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(FileTreePane.class.getClassLoader().getResource("org/tigr/images/Directory.gif")));

	public final static ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(FileTreePane.class.getClassLoader().getResource("org/tigr/images/expandedfolder.gif")));



        // hacked to work around directory problem

        public final static String WINDOWS = "windows";

        public final static String LINUX = "linux";

        public final static String MAC = "mac";

        private String os = System.getProperty("os.name");

        public char DIRECTORY_DIV;

        public String DIRECTORY_DIV_S;

        // end pc





	protected JTree m_tree;

	protected DefaultTreeModel m_model;

	protected JList fileList;

	private Vector rightListContent;

	private Vector leftListContent;



	protected String fPath;

	protected String selectedSingleFile;

	protected Vector selectedFilesVec;

	protected int selectedCount;



	protected Vector listeners = new Vector();

	public final static int NODE_COLLAPSED = 0;

	public final static int NODE_EXPANDED = 1;

	public final static int NODE_SELECTED = 2;



        // pc

	public FileTreePane() {
                
            String sep = System.getProperty("file.separator");
          //    if ( os.matches( "(?i).*" + WINDOWS + ".*" ) ){  //matches is 1.4 dependent

          //        DIRECTORY_DIV = '\\';

         //         DIRECTORY_DIV_S = "\\";

         //     }

      //        else {

        //          DIRECTORY_DIV = '/';

          //        DIRECTORY_DIV_S = "/";

            //  }

         // end pc

            DIRECTORY_DIV = sep.toCharArray()[0];
            DIRECTORY_DIV_S = sep;
	    JPanel fillerPane = new JPanel();

	    DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));

	    DefaultMutableTreeNode node;



	    File[] roots = File.listRoots();



	    for (int k = 0; k < roots.length; k++) {

		node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(roots[k])));

		top.add(node);

		node.add( new DefaultMutableTreeNode(new Boolean(true)));

	    }



	    m_model = new DefaultTreeModel(top);

	    m_tree = new JTree(m_model);

	    m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	    m_tree.putClientProperty("JTree.lineStyle", "Angled");

	    TreeCellRenderer renderer = new IconCellRenderer();

	    m_tree.setCellRenderer(renderer);

	    m_tree.addTreeExpansionListener(new EventHandler());

	    m_tree.addTreeSelectionListener(new EventHandler());

	    m_tree.setShowsRootHandles(true);

	    m_tree.setEditable(false);



	    JScrollPane s = new JScrollPane();

	    s.getViewport().add(m_tree);



	    setLayout(new BorderLayout());

	    add(s, BorderLayout.CENTER);

	}



	public String getSelection() {

	    return fPath;

	}



	private DefaultMutableTreeNode getTreeNode(TreePath path) {

	    return (DefaultMutableTreeNode) (path.getLastPathComponent());

	}



	private FileNode getFileNode(DefaultMutableTreeNode node) {



	    if (node == null) return null;



	    Object obj = node.getUserObject();



	    if (obj instanceof IconData)

		obj = ((IconData) obj).getObject();



	    if (obj instanceof FileNode) return (FileNode) obj;

	    else return null;

	}



	public Vector getFileNameList(String directoryPath) {



		//if (directoryPath == null) return null;

                if (directoryPath == null) {

                    System.out.println("FileTreePane <- getFilreNames(path) returning NULL");

                    return null;

                }



		FileBrowser fBrowser = new FileBrowser(fPath);



                //FileBrowser fBrowser = new FileBrowser("/home/pcahan1/pc_tmev/Data/affy/");



		Vector retrievedFileNames = fBrowser.getFileNamesVec();



		return retrievedFileNames;

	}



	public void addFileTreePaneListener(FileTreePaneListener listener) {

		listeners.addElement(listener);

	}



	public void removeFileTreePaneListener(FileTreePaneListener listener) {

		listeners.removeElement(listener);

	}



	public void fireEvent(FileTreePaneEvent event, int eventType) {

		Vector targets = (Vector) listeners.clone();

		for (int i = 0; i < targets.size(); i++) {

			FileTreePaneListener listener = (FileTreePaneListener) targets.elementAt(i);



			switch (eventType) {

				case (FileTreePane.NODE_COLLAPSED):

					listener.nodeCollapsed(event);

					break;

				case (FileTreePane.NODE_EXPANDED):

					listener.nodeExpanded(event);

					break;

				case (FileTreePane.NODE_SELECTED):

					listener.nodeSelected(event);

					break;

			}

		}

	}



	public static void main(String[] args) {

		FileTreePane ftp = new FileTreePane();



		JFrame frame = new JFrame("FileTreePane");

		frame.getContentPane().add(ftp);

		frame.setSize(600, 600);

		frame.setLocation(150, 150);

		frame.setVisible(true);

	}



	/*

	//

	//	FileTreePane - Internal Classes

	//

	*/



	private class FileNode {



	    protected File m_file;



	    public FileNode(File file) {

		m_file = file;

	    }



	    public File getFile() {

		return m_file;

	    }



	    public String toString() {

		return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();

	    }



	    public boolean expand(DefaultMutableTreeNode parent) {



		DefaultMutableTreeNode flag = (DefaultMutableTreeNode)parent.getFirstChild();



		if (flag==null) return false; //No flag



		Object obj = flag.getUserObject();

		if (!(obj instanceof Boolean)) return false; // Already expanded



		parent.removeAllChildren(); // Remove Flag



		File[] files = listFiles();

		if (files == null) return true;



		Vector v = new Vector();



		for (int k=0; k<files.length; k++) {

		    File f = files[k];

		    if (!(f.isDirectory())) continue;



		    FileNode newNode = new FileNode(f);

		    boolean isAdded = false;



		    for (int i=0; i<v.size(); i++) {

			FileNode nd = (FileNode)v.elementAt(i);

			if (newNode.compareTo(nd) < 0) {

			    v.insertElementAt(newNode, i);

			    isAdded = true;

			    break;

			}

		    }



		    if (!isAdded) v.addElement(newNode);

		}



		for (int i=0; i<v.size(); i++) {



		    FileNode nd = (FileNode)v.elementAt(i);

		    IconData idata = new IconData(FileTreePane.ICON_FOLDER, FileTreePane.ICON_EXPANDEDFOLDER, nd);

		    DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);

		    parent.add(node);



		    if (nd.hasSubDirs()) node.add(new DefaultMutableTreeNode(new Boolean(true)));

		}

		return true;

	    }



	    public boolean hasSubDirs() {



		File[] files = listFiles();



		if (files == null) return false;



		for (int k=0; k<files.length; k++) {

		    if (files[k].isDirectory()) return true;

		}



		return false;

	    }



	    public int compareTo(FileNode toCompare) {

		return m_file.getName().compareToIgnoreCase(toCompare.m_file.getName());

	    }



	    protected File[] listFiles() {



		if (!m_file.isDirectory()) return null;



		try {



		    return m_file.listFiles();



		} catch (Exception ex) {



		    JOptionPane.showMessageDialog(null, "Error reading directory " + m_file.getAbsolutePath(), "Warning", JOptionPane.WARNING_MESSAGE);

		    return null;

		}

	    }

	}



	private class EventHandler implements ListSelectionListener, TreeExpansionListener, TreeSelectionListener {



		// From ListSelectionListener



		public void valueChanged(ListSelectionEvent listSelectionEvent) {



			selectedFilesVec = new Vector();

			selectedCount = fileList.getSelectedValues().length;



			for (int i = 0; i <= selectedCount-1; i++) {

				selectedFilesVec.add(i, (String) (fileList.getSelectedValues())[i]);

			}



			selectedSingleFile = (String) (fileList.getSelectedValue());

			fPath = (String) (fileList.getSelectedValue());

		}



		// From TreeExpansionListener



		public void treeExpanded(TreeExpansionEvent event) {



			final DefaultMutableTreeNode node = getTreeNode(event.getPath());

			final FileNode fnode = getFileNode(node);



			Thread runner = new Thread() {

				public void run() {

					if (fnode != null && fnode.expand(node)) {

						Runnable runnable = new Runnable() {

							public void run() {

								m_model.reload(node);

							}

						};

						SwingUtilities.invokeLater(runnable);

					}

				}

			};



			runner.start();



			fireEvent(new FileTreePaneEvent(event, null), FileTreePane.NODE_EXPANDED);

		}



		public void treeCollapsed(TreeExpansionEvent event) {

			fireEvent(new FileTreePaneEvent(event, null), FileTreePane.NODE_COLLAPSED);

		}



		// From TreeSelectionListener



		public void valueChanged(TreeSelectionEvent event) {



			DefaultMutableTreeNode node = getTreeNode(event.getPath());

			FileNode fnode = getFileNode(node);



			if (fnode != null) {



				fPath = fnode.getFile().getAbsolutePath();

				if (fPath.charAt(fPath.length()-1) != DIRECTORY_DIV) {

					fPath += DIRECTORY_DIV;

				}



				Vector fileNameVector = getFileNameList(fPath);



				Hashtable hash = new Hashtable();

				hash.put("Path", fPath);

				if (! (fileNameVector == null)) {

					hash.put("Filenames", fileNameVector);

				}

				fireEvent(new FileTreePaneEvent(event, hash), FileTreePane.NODE_SELECTED);



			} else {



				Hashtable hash = new Hashtable();

				hash.put("Path", "");

				fireEvent(new FileTreePaneEvent(event, null), FileTreePane.NODE_SELECTED);

			}

		}

	}



	private class IconData {



	    protected Icon m_icon;

	    protected Icon m_expandedIcon;

	    protected Object m_data;



	    public IconData(Icon icon, Object data) {

		m_icon = icon;

		m_expandedIcon = null;

		m_data = data;

	    }



	    public IconData(Icon icon, Icon expandedIcon, Object data) {

		m_icon = icon;

		m_expandedIcon = expandedIcon;

		m_data = data;

	    }



	    public Icon getIcon() {

		return m_icon;

	    }



	    public Icon getExpandedIcon() {

		return m_expandedIcon!=null ? m_expandedIcon : m_icon;

	    }



	    public Object getObject() {

		return m_data;

	    }



	    public String toString() {

		return m_data.toString();

	    }

	}



	private class IconCellRenderer extends JLabel implements TreeCellRenderer {



	    protected Color m_textSelectionColor;

	    protected Color m_textNonSelectionColor;

	    protected Color m_bkSelectionColor;

	    protected Color m_bkNonSelectionColor;

	    protected Color m_borderSelectionColor;

	    protected boolean m_selected;



	    public IconCellRenderer() {



		super();



		m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");

		m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");

		m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");

		m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");

		m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");



		setOpaque(false);

	    }



	    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {



		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

		Object obj = node.getUserObject();

		setText(obj.toString());



		if (obj instanceof Boolean)

		    setText("Retrieving data...");

		if (obj instanceof IconData) {

		    IconData idata = (IconData)obj;

		    if (expanded)

			setIcon(idata.getExpandedIcon());

		    else

			setIcon(idata.getIcon());

		}else

		    setIcon(null);



		setFont(tree.getFont());

		setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);

		setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);



		m_selected = sel;



		return this;

	    }



	    public void paintComponent(Graphics g) {



		Color bColor = getBackground();

		Icon icon = getIcon();

		g.setColor(bColor);



		int offset = 0;

		if(icon != null && getText() != null) {

		    offset = (icon.getIconWidth() + getIconTextGap());

		}



		g.fillRect(offset, 0, getWidth() - 1 - offset,

		getHeight() - 1);



		if (m_selected) {

		    g.setColor(m_borderSelectionColor);

		    g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1);

		}



		super.paintComponent(g);

	    }

	}



	private class FileBrowser {



	    private String workingFullDir;

	    private String workingDir;

	    private Vector filesVec;

	    private File dir;

	    private File subDir;



	    public FileBrowser(String directory) {

		setDirectory(directory);

	    }



	    public void setDirectory(String directory) {



		workingFullDir = directory;

               // System.out.println("directory name passed to FileBrowser:"+directory);

		dir = new File(directory);

		workingDir = dir.getAbsolutePath();



		if (! dir.isDirectory()) {

                   // System.out.println("not a correct dir --IN FileTreePane --");



		    workingDir = workingDir.substring(0, workingDir.lastIndexOf(DIRECTORY_DIV));



		} else {

                  //  System.out.println("Is a correct dir --IN FileTreePane--");

		    filterFiles();

		}

	    }



	    public void filterFiles() {



		File checkFile;

		String[] available;

		filesVec = new Vector();



		if (dir == null) return;

		else available = dir.list();



		if (workingDir == null) workingDir = dir.getAbsolutePath();



		for (int i = 0; i < available.length; i++) {

		    checkFile = new File(dir, available[i]);

			if (checkFile.isFile()) {

				filesVec.addElement(workingDir + ((workingDir.endsWith(DIRECTORY_DIV_S) ? "" : DIRECTORY_DIV_S)) + available[i]);

			}

		}



	    }



	    public Vector getFileNamesVec() {

		return filesVec;

	    }



	    public int getFileCounts() {

		return filesVec.size();

	    }



	    public String getAbsolutePath() {

		return workingDir;

	    }



	    public String creatSubDir(String sub) {

		subDir = new File(workingFullDir + DIRECTORY_DIV + sub + DIRECTORY_DIV);



		//if (subDir.mkdir()){

		//    System.out.println(workingFullDir + "DIRECTORY_DIV" + sub + "DIRECTORY_DIV created");

		//} else {

		//    System.out.println(workingFullDir + "DIRECTORY_DIV" + sub + "DIRECTORY_DIV NOT created, it might already exists");

		//}



		return workingFullDir + DIRECTORY_DIV + sub + DIRECTORY_DIV;

	    }



	    public String getExtension(File f) {

		String ext = null;

		String s = f.getName();

		int i = s.lastIndexOf('.');



		if (i > 0 &&  i < s.length() - 1) {

		    ext = s.substring(i+1).toLowerCase();

		}



		return ext;

	    }

	}

    }