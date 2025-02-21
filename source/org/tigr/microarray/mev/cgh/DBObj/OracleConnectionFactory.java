/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cgh.DBObj;

/*
 * ConnectionFactory.java
 *
 * Created on February 25, 2002, 1:31 AM
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class OracleConnectionFactory {
    private static OracleConnectionFactory ref = new OracleConnectionFactory();
    /** Creates new ConnectionFactory */
    public OracleConnectionFactory() {
        try{
        	ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class.forName("oracle.jdbc.driver.OracleDriver", true, cl);
        }catch(ClassNotFoundException e){
            System.out.println("ERROR:  Exception loading driver class");
        }
    }

    public static Connection getConnectionOracle() throws SQLException{
        String url = "jdbc:oracle:thin:@158.130.47.16:1521:genomics";

        String username = "margolia";
        String password = "richmond1";

        return DriverManager.getConnection(url, username, password);
    }

    public static void close(ResultSet rs){
        try{
            rs.close();
        }catch(Exception ignored){}
    }

    public static void close(Statement stmt){
        try{
            stmt.close();
    }catch (Exception ignored){}
    }

    public static void close(Connection conn){
        try{
            conn.close();
    }catch (Exception ignored){}
    }
}
