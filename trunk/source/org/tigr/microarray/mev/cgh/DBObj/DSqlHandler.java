/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cgh.DBObj;

/*
 * DSqlHandler.java
 *
 * Created on February 25, 2002, 1:28 PM
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class DSqlHandler {

    Connection connection;

    /** Creates new DExperimentData */
    public DSqlHandler() {
    }




    public boolean saveItem(String sql){
        Statement statement = null;

        int result = 0;
        try{
            connection = ConnectionFactory.getConnection("","");
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(statement != null){
                    statement.close();
                }
                if(connection != null){
                    connection.close();
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }
        return(result == 1);
    }

    public boolean saveItemTestServer(String sql){
        Statement statement = null;

        int result = 0;
        try{
            connection = ConnectionFactory.getConnectionTestServer();
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(statement != null){
                    statement.close();
                }
                if(connection != null){
                    connection.close();
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }
        return(result == 1);
    }

    public ResultSet fetchItemsOracle(String sql){
        try{
            connection = OracleConnectionFactory.getConnectionOracle();

            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            return rs;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Raktim
     * Trial Version of Driver used.
     * Max queries = 25
     * @param sql
     * @return
     */
    public ResultSet fetchItemsCSV(String sql){
        try{
            connection = ConnectionFactory.getConnectionCSV();

            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            return rs;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}