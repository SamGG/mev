/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/************************
 *
 * Class connLOLA
 *
 * communicates with LOLA
 * www.lola.gwu.edu/
 *
 * Patrick Cahan
 * pcahan1@gwu.edu
 *
 *************************/

package org.tigr.microarray.mev.cluster.clusterUtil.submit.lola;

//package classes;

//import java.io.File;
//import java.io.PrintWriter;
//import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

//import java.net.URL;
//import java.net.URLConnection;
//import java.net.HttpURLConnection;


public class connLOLA {

    //public static final String URL_LOLA = "http://www.lola.gwu.edu/test_lola/";
    public static final String URL_LOLA = "http://www.lola.gwu.edu/";
    public static final String FILE_LOGIN = "login.php";
    public static final String FILE_CONFIRM_LIST = "confirm_list.php";
    public static final String FILE_CREATE_LIST = "create_list.php";
    public static final String FILE_LOGOUT = "logout.php";

    private String email, pw, filename, list_name, list_desc, session;

    public connLOLA(String email, String pw){
	this(email, pw, "", "", "" );
    }

    public connLOLA(String email, String pw, String filename, String list_name, String list_desc){
        set_email(email);
	set_pw(pw);
	set_filename(filename);
	set_list_name(list_name);
	set_list_desc(list_desc);
    }

    // sets
    public void set_email( String email ){
	this.email = email;
    }
    public void set_pw( String pw ){
	this.pw = pw;
    }
    public void set_filename( String filename ){
	this.filename = filename;
    }
    public void set_list_name( String list_name ){
	this.list_name = list_name;
    }
    public void set_list_desc( String list_desc ){
	this.list_desc = list_desc;
    }
    public void set_session( String session ){
	this.session = session;
    }

    //gets
    public String get_email(){
	return this.email;
    }
    public String get_pw( ){
	return this.pw;
    }
    public String get_filename( ){
	return this.filename;
    }
    public String get_list_name(){
	return this.list_name;
    }
    public String get_list_desc(){
	return this.list_desc;
    }
    public String get_session(){
	return this.session;
    }

    public String login(){
        System.out.println("Logging in to LOLA"); //Display the string.
	try {
	    // Construct data
	    String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(get_email(), "UTF-8");
	    data += "&" + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(get_pw(), "UTF-8");

	    // Send data
	    URL url = new URL(URL_LOLA + FILE_LOGIN);
       
            URLConnection conn = url.openConnection();
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(data);
	    wr.flush();
               
            // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line, sessid;

	    // first line contains PHPSESSIONID
	    line = rd.readLine();
	    sessid = get_sessid(line);
	    this.set_session( sessid );
	    //	    System.out.println("Session id: " + sessid);
	    /*
	    while ((line = rd.readLine()) != null) {
		System.out.println(line);
		// Process line...
	    }
	    */
	    wr.close();
	    rd.close();
	    return sessid;
	} catch (Exception e) {
            System.out.println("Login Error");
            //e.printStackTrace();
	    return "";
	}
    }

    public void logout(){
	try {

	    String data = URLEncoder.encode("PHPSESSID", "UTF-8") + "=" + URLEncoder.encode(this.get_session(), "UTF-8");
	    //System.out.println("URL: " + URL_LOLA + FILE_LOGOUT);
	    // Send data
	    URL url = new URL(URL_LOLA + FILE_LOGOUT);
	    URLConnection conn = url.openConnection();
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(data);
	    wr.flush();
	    System.out.println("Logged out");
	}
	catch (Exception e) {
	}
    }

    public boolean submit_list( String gl_name, String gl_desc, /*String species, */String gl_list ){

	// get species code from string

	// submit list

	try{
	    String data = URLEncoder.encode("PHPSESSID", "UTF-8") + "=" + URLEncoder.encode(this.get_session(), "UTF-8");
	    data += "&" + URLEncoder.encode("gl_name", "UTF-8") + "=" + URLEncoder.encode(gl_name, "UTF-8");
	    data += "&" + URLEncoder.encode("gl_desc", "UTF-8") + "=" + URLEncoder.encode(gl_desc, "UTF-8");

	    // hard coded -- species id
            // from NCBI taxonomy database
            // rat = 10116
            // human = 9606
            // mouse = 10090
	    data += "&" + URLEncoder.encode("genome_id", "UTF-8") + "=" + URLEncoder.encode("9606", "UTF-8");

	    // hard coded gene id type -- AFFY
	    data += "&" + URLEncoder.encode("gene_id_type", "UTF-8") + "=" + URLEncoder.encode("2", "UTF-8");
	    data += "&" + URLEncoder.encode("gl_list", "UTF-8") + "=" + URLEncoder.encode(gl_list, "UTF-8");


	    System.out.println("URL: " + URL_LOLA + FILE_CONFIRM_LIST);
	    // Send data
	    URL url = new URL(URL_LOLA + FILE_CONFIRM_LIST);
	    URLConnection conn = url.openConnection();
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    //System.out.println(data);
	    wr.write(data);
	    wr.flush();
	    //System.out.println("list confirmed?");


	    // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;

	    //line = rd.readLine();
	    //sessid = get_sessid(line);
	    //this.set_session( sessid );
	    //System.out.println("Session id: " + this.get_session());
/*
	    while ((line = rd.readLine()) != null) {
		System.out.println(line);
		// Process line...
	    }
*/
	    wr.close();
	    rd.close();

	}
	catch (Exception e) {
            System.out.println("error in list submission");
            e.printStackTrace();
        }
	return true;
    }

public void create_list(){
	try {

	    String data = URLEncoder.encode("PHPSESSID", "UTF-8") + "=" + URLEncoder.encode(this.get_session(), "UTF-8");
	    //System.out.println("URL: " + URL_LOLA + FILE_CREATE_LIST);
	    // Send data
	    URL url = new URL(URL_LOLA + FILE_CREATE_LIST);
	    //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    URLConnection conn = url.openConnection();
	    //conn.setFollowRedirects(true);
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(data);
	    wr.flush();

	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;

	    line = rd.readLine();
	    //sessid = get_sessid(line);
	    //this.set_session( sessid );
	    //System.out.println(line);
/*
	    while ((line = rd.readLine()) != null) {
		System.out.println(line);
		// Process line...
	    }
*/
	    wr.close();
	    rd.close();
	    System.out.println("Gene list saved in LOLA");
	}
	catch (Exception e) {
            System.out.println("error in createList()");
            e.printStackTrace();
	}
    }

    private String get_sessid(String line){
	int start_name, end_name, end_sess;
	String sess_name = "PHPSESSID=";
	String sess_end = "-->";

	start_name = line.indexOf(sess_name);
	end_name = start_name + sess_name.length();
	end_sess = line.indexOf(sess_end);

	return line.substring(end_name, end_sess);
    }
}


