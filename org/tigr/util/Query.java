/*
Copyright @ 2001-2002, The Institute for Genomic Research (TIGR).
All rights reserved.

This software is provided "AS IS".  TIGR makes no warranties, express
or implied, including no representation or warranty with respect to
the performance of the software and derivatives or their safety,
effectiveness, or commercial viability.  TIGR does not warrant the
merchantability or fitness of the software and derivatives for any
particular purpose, or that they may be exploited without infringing
the copyrights, patent rights or property rights of others. TIGR shall
not be liable for any claim, demand or action for any loss, harm,
illness or other damage or injury arising from access to or use of the
software or associated information, including without limitation any
direct, indirect, incidental, exemplary, special or consequential
damages.

This software program may not be sold, leased, transferred, exported
or otherwise disclaimed to anyone, in whole or in part, without the
prior written consent of TIGR.
*/

package org.tigr.util;

import java.sql.*;
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

    } finally {

      return resultVector;
    }
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

    } finally {

      return resultVector;
    }
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

    } finally {

      return updateCount;
    }
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
