package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import com.borland.jbcl.layout.VerticalFlowLayout;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/


public class HelpDialog extends JDialog implements HyperlinkListener {
  /**
   * Link to help file.
   */
  private String helpFilename = "http://function.princeton.edu/ChARM/help/ChARMHelp.htm";

  private String message = null;
  JScrollPane jScrollPane1 = new JScrollPane();
  JButton okButton1 = new JButton( "OK" );
  JEditorPane jEditorPane1 = new JEditorPane();
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  JPanel jPanel1 = new JPanel();

  /**
   * Class constructor.
   * @param f JFrame
   */
  public HelpDialog(JFrame f) {
      super(f,"ChARM Viewer Help",true);
      try{ jbInit();}
      catch(Exception e) {System.out.println("Help Dialog:"+e);}
  }

  /**
   * Dialog initialization.
   * @throws Exception
   */
  private void jbInit() throws Exception {
    JPanel contentPane = (JPanel) this.getContentPane();
    this.setResizable(false);
    this.setSize(new Dimension(818, 689));


    okButton1.addActionListener( new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    } );
    this.getContentPane().setLayout( new FlowLayout() );
    contentPane.setLayout(verticalFlowLayout1);
    //jEditorPane1.setText("jEditorPane1");

    jEditorPane1.setEditable(false);
    String s = null;
    URL helpURL = null;
    try {
    /*  s = "file:"
	+ System.getProperty("user.dir")
	+ System.getProperty("file.separator")
	+ helpFilename;*/
    //helpURL = new URL(s);
    helpURL = new URL(helpFilename);
    /* ...  use the URL to initialize the editor pane  ... */
    } catch (Exception e) {
    System.err.println("Couldn't create help URL: " + s);
  }


    try {
    jEditorPane1.setPage(helpURL);}
    catch (IOException e) {
      System.err.println("Attempted to read a bad URL: " + helpURL);
      jEditorPane1.setText("Cannot connect to URL: "+helpURL);
    }
    jEditorPane1.setContentType("text/html");
    jEditorPane1.addHyperlinkListener(this);
    jScrollPane1.setPreferredSize(new Dimension(790, 600));
    jScrollPane1.getViewport().add(jEditorPane1, null);
    contentPane.add(jScrollPane1, null);
    contentPane.add(jPanel1, null);
    jPanel1.add(okButton1, null);
  }


  public void hyperlinkUpdate( HyperlinkEvent event )
     {
        //System.out.println("got here");
                 if( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
                 {

                         // Load some cursors
                         Cursor cursor = jEditorPane1.getCursor();
                         Cursor waitCursor = Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR );
                         jEditorPane1.setCursor( waitCursor );

                         // Handle the hyperlink change
                         SwingUtilities.invokeLater( new PageLoader( jEditorPane1,
                                                                 event.getURL(), cursor ) );
                 }
     }



     class PageLoader implements Runnable
     {
         private JEditorPane html;
         private URL         url;
         private Cursor      cursor;

         PageLoader( JEditorPane html, URL url, Cursor cursor )
         {
             this.html = html;
             this.url = url;
             this.cursor = cursor;
         }

         public void run()
         {
                 if( url == null )
                 {
                     // restore the original cursor
                     html.setCursor( cursor );

                     // PENDING(prinz) remove this hack when
                     // automatic validation is activated.
                     Container parent = html.getParent();
                     parent.repaint();
             }
             else
             {
                     Document doc = html.getDocument();
                     try {
                             html.setPage( url );
                     }
                     catch( IOException ioe )
                     {
                         html.setDocument( doc );
                     }
                     finally
                     {
                         // schedule the cursor to revert after
                         // the paint has happended.
                             url = null;
                         SwingUtilities.invokeLater( this );
                     }
                 }
             }
     }



  }
