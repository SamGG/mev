/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Vector;

public class Query {

  private String queryString;
  private static final int SYBASE = 0;
  private static final int MYSQL = 1;

  public Query(String queryString) {
    this.queryString = queryString;
  }

  public Vector execute(Connection c) throws SQLException {
  //public Vector execute(Connection c) {
    return execute(c, true);
  }

  public Vector execute(Connection c, boolean returnHeadings) throws SQLException {
  //public Vector execute(Connection c, boolean returnHeadings) {
    Vector resultVector = null;
    Statement s = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;
    String[] rowResults, columnLabels;

    try {
      resultVector = new Vector();
      s = c.createStatement();
      boolean isQuery = s.execute(queryString);

      if (isQuery) {

        rs = s.getResultSet();
        rsmd = rs.getMetaData();
        columnCount = rsmd.getColumnCount();

        if (returnHeadings) {

          columnLabels = new String[columnCount];
          for (int i = 0; i < columnCount; i++) columnLabels[i] = rsmd.getColumnLabel(i + 1);
          if (columnLabels.length > 0) resultVector.addElement(columnLabels);
        }

        while(rs.next()) {

          rowResults = new String[columnCount];
          for (int i = 0; i < columnCount; i++) {
            try {
              if (rs.getObject(i + 1).toString().trim() == null) {
                rowResults[i] = "null";
              } else {
                rowResults[i] = rs.getObject(i + 1).toString().trim();
              }
            } catch (NullPointerException npe) {
              rowResults[i] = "null";
            }
          }

        resultVector.addElement(rowResults);
        }

      } else {

        if (returnHeadings) resultVector.addElement(new String[]{"Rows affected"});
        resultVector.addElement(new String[]{"" + s.getUpdateCount()});
      }

      for (SQLWarning w = c.getWarnings(); w != null; w = w.getNextWarning()) {
        System.out.println("SQLWarning: " + w.getMessage() + ": " + w.getSQLState());
      }

      if (rs != null) rs.close();
      s.close();

    } catch (SQLException sqle) {

      throw sqle;
    }
      // we might not be able to use 'finally' here.  If we use, the method will
      // ALWAYS return the updateCount and never throw the sqle. /JL
    /*} finally {

      return resultVector;
    }*/

    return resultVector;
  }

  public Vector executeQuery(Connection c) throws SQLException {
  //public Vector executeQuery(Connection c) {
    return executeQuery(c, true);
  }

  public Vector executeQuery(Connection c, boolean returnHeadings) throws SQLException {
  //public Vector executeQuery(Connection c, boolean returnHeadings) {
    Vector resultVector = null;
    Statement s = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;
    String[] rowResults, columnLabels;

    try {
      resultVector = new Vector();
      s = c.createStatement();
      rs = s.executeQuery(queryString);
      rsmd = rs.getMetaData();
      columnCount = rsmd.getColumnCount();

      if (returnHeadings) {
        columnLabels = new String[columnCount];
        for (int i = 0; i < columnCount; i++) columnLabels[i] = rsmd.getColumnLabel(i + 1);
        if (columnLabels.length > 0) resultVector.addElement(columnLabels);
      }

    } catch (SQLException sqle) {

      //System.out.println("SQLException: " + sqle);
      throw sqle;
    }

    try {
      while(rs.next()) {
        rowResults = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
          try {
            if (rs.getObject(i + 1).toString().trim() == null) {
              rowResults[i] = "null";
            } else {
              rowResults[i] = rs.getObject(i + 1).toString().trim();
            }
          } catch (NullPointerException npe) {
            rowResults[i] = "null";
          }
        }

      resultVector.addElement(rowResults);
      }

      for (SQLWarning w = c.getWarnings(); w != null; w = w.getNextWarning()) {
        System.out.println("SQLWarning: " + w.getMessage() + ": " + w.getSQLState());
      }

      if (rs != null) rs.close();
      s.close();

    } catch (SQLException sqle) {

      //System.out.println("SQLException: " + sqle);
      throw sqle;
    }
      // we might not be able to use 'finally' here.  If we use, the method will
      // ALWAYS return the updateCount and never throw the sqle. -- JL

    /*} finally {

      return resultVector;
    }*/

    return resultVector;
  }

  public int executeUpdate(Connection c) throws SQLException {
  //public int executeUpdate(Connection c) {
    int updateCount = 0;

    try {

      Statement s = c.createStatement();
      updateCount = s.executeUpdate(queryString);
      for (SQLWarning w = c.getWarnings(); w != null; w = w.getNextWarning()) {
        System.out.println("SQLWarning: " + w.getMessage() + ": " + w.getSQLState());
      }

      s.close();

    } catch (SQLException sqle) {
      //System.out.println("SQLException: " + sqle);
      throw sqle;
    }
    // we might not be able to use 'finally' here.  If we use, the method will
    // ALWAYS return the updateCount and never throw the sqle. -- JL
    /*} finally {

      return updateCount;
    }*/
      return updateCount;
  }

  public static Vector resultsToList(Vector results) {
    String[] rowResults;
    Vector resultVector = new Vector();

    for (int i = 0; i < results.size(); i++) {
      rowResults = (String[]) results.elementAt(i);
      resultVector.addElement(rowResults[0]);
    }

    return resultVector;
  }

  public void setQueryStatement(String code){
    queryString = code;
  }

  public static Vector resultsToVectors(Vector results) {
    String[] rowResults;
    Vector newResults;
    Vector resultVector = new Vector();

    for (int i = 0; i < results.size(); i++) {
      newResults = new Vector();
      rowResults = (String[]) results.elementAt(i);

      for (int j = 0; j < rowResults.length; j++) {
        newResults.addElement(rowResults[j]);
      }

      resultVector.addElement(newResults);
    }

    return resultVector;
  }

  public String toString() {
    return queryString;
  }

  /****************************************************************************
   * <b>Description: </b>
   *    starts a transaction for a DBMS.
   * <p><b>Parameters: </b>
   * <br> c -- the connection created with a DBMS.
   * <br> dbSys -- the DBMS to be handled; 0: Sybase; 1: MySql.
   ***************************************************************************/
  public static void beginTransaction(Connection c, int dbSys) throws SQLException {
    try {
      // Set the autocommit flag to false to enable transactions
      c.setAutoCommit(false);

      // Explicitly set the chained to off and begin transaction
      Statement stmt = c.createStatement();
      if(dbSys == MYSQL){
        stmt.execute("BEGIN");
      } else if (dbSys == SYBASE){
        stmt.execute("SET CHAINED OFF");
        stmt.execute("BEGIN TRANSACTION");
      } else {
        System.out.println("Error: No DBMS found.");
      }
    } catch (SQLException sqle) {

      throw sqle;
    }
  }

  /*****************************************************************************
  * endTransaction:  transaction handling for DB
  ***************************************************************************/

  /* Complete transaction by executing a commit command and set the autocommit flag
  * back to true
  */
  public static void endTransaction(Connection c) throws SQLException {
    try {

      c.commit();	// Make the database changes permanent
      c.setAutoCommit(true);

    } catch (SQLException sqle) {

      throw sqle;
    }
  }

  /*****************************************************************************
  * abortTransaction:  transaction handling for DB
  ***************************************************************************/

  /* Abort the transaction by rolling back changes made to the database and
  * set the autocommit flag back to true
  */
  public static void abortTransaction(Connection c) throws SQLException {

    try {

      c.rollback();	// Rollback database changes
      c.setAutoCommit(true);

    } catch (SQLException sqle) {

      throw sqle;
    }
  }
}
