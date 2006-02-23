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
import java.util.Properties;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class ConnectionFactory {
    private static ConnectionFactory ref = new ConnectionFactory();
    /** Creates new ConnectionFactory */
    public ConnectionFactory() {
        try{
            //Class.forName("com.merant.datadirect.jdbc.sqlserver.SQLServerDriver");
            //Class.forName("com.inet.tds.TdsDriver");
            //Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
        	//CSV Flat File JDBC Driver
        	//Class.forName("jstels.jdbc.csv.CsvDriver");
        	Class.forName("com.hxtt.sql.text.TextDriver");//.newInstance();
        }catch(ClassNotFoundException e){
            System.out.println("ERROR:  Exception loading driver class");
        }
    }

    /**
     * Raktim
     * UNused
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(String username, String password) throws SQLException{
        String url = "jdbc:microsoft:sqlserver://128.91.210.174:6430";
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Raktim
     * Unused
     * @return
     * @throws SQLException
     */
    public static Connection getConnectionTestServer() throws SQLException{
        String url = "jdbc:microsoft:sqlserver://128.91.210.134:2433";

        String username = "adam";
        String password = "Ambystoma";

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Raktim
     * Added to enable FLAT File based JDBC operations
     * Trial Version of Driver used.
     * Max queries = 25
     * @return
     * @throws SQLException
     */
    public static Connection getConnectionCSV () throws SQLException {
    	//String url = "jdbc:jstels:csv:" + "data";
    	String url = "jdbc:Text:///data";
    	Properties myProp = new Properties();
		myProp.put("_CSV_Header","true");
    	return DriverManager.getConnection(url, myProp);
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
