package it.zac06;

import java.math.BigDecimal;
import java.sql.*;
import java.io.*;
import java.net.*;

public class Database implements AutoCloseable {
	private Connection cn;
	private Statement st;
	private PreparedStatement ps;
	private ResultSet lastQuery;
	private int lastUpdate;

	private static boolean loaded = false;

	/**
	 * This method loads the MySQL driver (com.mysql.jdbc.Driver) into memory, making it ready for use.
	 * This method is called automatically by the constructor, so you don't need to call it manually.
	 * @throws ClassNotFoundException
	 */
	public static void loadDriver() throws ClassNotFoundException {
		if (loaded) {
			return;
		}

		// Class.forName("com.mysql.jdbc.Driver");
		Class.forName("com.mysql.cj.jdbc.Driver");
		loaded = true;
	}

	/**
	 * This method opens a connection to the database. It is called automatically by the constructor, so you don't need to call it manually.
	 * You can call it in case you want to re-open a connection using the same object.
	 * @param dbpar DBParams object containing the database connection parameters.
	 * @throws SQLException
	 */
	public void open(DBParams dbpar) throws SQLException {
		open(dbpar.getHost(), dbpar.getPort(), dbpar.getDbname(), dbpar.getUsername(), dbpar.getPassword());
	}

	/**
	 * This method opens a connection to the database. It is called automatically by the constructor, so you don't need to call it manually.
	 * You can call it in case you want to re-open a connection using the same object.
	 * @param host Hostname or IP address of the database server
	 * @param port Port number of the database server
	 * @param dbName Database name
	 * @param user Username to access the database
	 * @param password Password of the user accessing the database
	 * @throws SQLException
	 */
	public void open(String host, int port, String dbName, String user, String password) throws SQLException {
		cn = (Connection) DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user,
				password);

		st = cn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);

		ps=null;

		lastQuery = null;
		lastUpdate = -1;
	}

	/**
	 * This constructor creates a new Database object and opens a connection to the database.
	 * It automatically loads the MySQL driver, if not already done previously, and then opens the connection.
	 * @param host Hostname or IP address of the database server
	 * @param port Port number of the database server
	 * @param dbName Database name
	 * @param user Username to access the database
	 * @param password Password of the user accessing the database
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Database(String host, int port, String dbName, String user, String password)
			throws SQLException, ClassNotFoundException {
		loadDriver();
		open(host, port, dbName, user, password);
	}

	/**
	 * This constructor creates a new Database object and opens a connection to the database.
	 * It automatically loads the MySQL driver, if not already done previously, and then opens the connection.
	 * @param dbpar DBParams object containing the database connection parameters.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Database(DBParams dbpar) throws SQLException, ClassNotFoundException {
		loadDriver();
		open(dbpar);
	}

	/**
	 * This method closes the connection to the database. It is NOT automatically called, so you need to invoke it manually.
	 * @throws SQLException
	 */
	@Override
	public void close() throws SQLException {
		if (lastQuery != null && !lastQuery.isClosed()) {
			lastQuery.close();
			lastQuery=null;
		}
		if (ps != null && !ps.isClosed()) {
			ps.close();
			ps=null;
		}
		if (st != null && !st.isClosed()) {
			st.close();
			st=null;
		}
		if (cn != null && !cn.isClosed()) {
			cn.close();
			cn=null;
		}
	}

	/**
	 * This method closes the last ResultSet representing the last executed query.
	 * It is automatically called by the query() and preparedQuery() methods, so you don't need to call it manually.
	 * @throws SQLException
	 */
	public void closeLastQuery() throws SQLException{
		if (lastQuery != null && !lastQuery.isClosed()) {
			lastQuery.close();
			lastQuery = null;
		}
	}

	/**
	 * This method allows to execute a query on the database. 
	 * It does not allow to perform a prepared query; see preparedQuery() for that.
	 * @param sql String containing the SQL query to execute
	 * @return ResultSet object representing the result of the query
	 * @throws SQLException
	 */
	public ResultSet query(String sql) throws SQLException {
		closeLastQuery();
		lastQuery = st.executeQuery(sql);
		return lastQuery;
	}

	/**
	 * This method allows to execute an update on the database. 
	 * It does not allow to perform a prepared update; see preparedUpdate() for that.
	 * @param sql String containing the SQL update to execute
	 * @return Integer representing the number of rows affected by the update
	 * @throws SQLException
	 */
	public int update(String sql) throws SQLException {
		lastUpdate = st.executeUpdate(sql);
		return lastUpdate;
	}

	private void preparePrepared(String sql, Object... params) throws SQLException {
		closeLastQuery();

		if(ps!=null && !ps.isClosed()){
			ps.close();
			ps=null;
		}
		ps = cn.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);

		for(int i=0; i<params.length; i++) {
			Object curObj=params[i];

			if(curObj==null){
				ps.setNull(i+1, java.sql.Types.NULL);

			}else if(curObj instanceof String){
				ps.setString(i+1, (String)curObj);

			}else if(curObj instanceof BigDecimal){
				ps.setBigDecimal(i+1, (BigDecimal)curObj);

			}else if(curObj instanceof Short){
				ps.setShort(i+1, (Short)curObj);

			}else if(curObj instanceof Integer){
				ps.setInt(i+1, (Integer)curObj);

			}else if(curObj instanceof Long){
				ps.setLong(i+1, (Long)curObj);

			}else if(curObj instanceof Byte){
				ps.setByte(i+1, (Byte)curObj);

			}else if(curObj instanceof Float){
				ps.setFloat(i+1, (Float)curObj);

			}else if(curObj instanceof Double){
				ps.setDouble(i+1, (Double)curObj);

			}else if(curObj instanceof byte[]){
				ps.setBytes(i+1, (byte[])curObj);

			}else if(curObj instanceof java.sql.Date){
				ps.setDate(i+1, (java.sql.Date)curObj);

			}else if(curObj instanceof Time){
				ps.setTime(i+1, (Time)curObj);

			}else if(curObj instanceof java.sql.Timestamp){
				ps.setTimestamp(i+1, (java.sql.Timestamp)curObj);

			}else if(curObj instanceof Boolean){
				ps.setBoolean(i+1, (Boolean)curObj);

			}else if(curObj instanceof InputStream){
				ps.setBinaryStream(i+1, (InputStream)curObj);

			}else if(curObj instanceof java.sql.Blob){
				ps.setBlob(i+1, (java.sql.Blob)curObj);

			}else if(curObj instanceof java.sql.Clob){
				ps.setClob(i+1, (java.sql.Clob)curObj);

			}else if(curObj instanceof java.sql.Array){
				ps.setArray(i+1, (java.sql.Array)curObj);

			}else if(curObj instanceof Reader){
				ps.setCharacterStream(i+1, (Reader)curObj);

			}else if(curObj instanceof java.sql.SQLXML){
				ps.setSQLXML(i+1, (java.sql.SQLXML)curObj);

			}else if(curObj instanceof URL){
				ps.setURL(i+1, (URL)curObj);

			}else {
				ps.setObject(i+1, curObj);

			}
		}
	}

	/**
	 * This method allows to execute a prepared query on the database.
	 * It does not allow to perform a simple query. See query() for that.
	 * It behaves just like printf(): the parameters are passed to the query in the order they are given.
	 * @param sql String containing the SQL query to execute
	 * @param params Primitives/objects containing the parameters to pass to the query
	 * @return ResultSet object representing the result of the query
	 * @throws SQLException
	 */
	public ResultSet preparedQuery(String sql, Object... params) throws SQLException {
		preparePrepared(sql, params);

		lastQuery=ps.executeQuery();
		return lastQuery;
	}

	/**
	 * This method allows to execute a prepared update on the database.
	 * It does not allow to perform a simple update. See update() for that.
	 * It behaves just like printf(): the parameters are passed to the query in the order they are given.
	 * @param sql String containing the SQL update to execute
	 * @param params Primitives/objects containing the parameters to pass to the update
	 * @return Integer representing the number of rows affected by the update
	 * @throws SQLException
	 */
	public int preparedUpdate(String sql, Object... params) throws SQLException {
		preparePrepared(sql, params);

		lastUpdate=ps.executeUpdate();
		return lastUpdate;
	}

	/**
	 * This method returns the number of rows in the last query executed.
	 * @param rs ResultSet object representing the query to process
	 * @return The number of rows in the query
	 * @throws SQLException
	 */
	public static int getRowCount(ResultSet rs) throws SQLException {
		int numRows;
		rs.last();
		numRows = rs.getRow();
		rs.beforeFirst();
		return numRows;
	}

	/**
	 * This method returns the number of rows in the last query executed.
	 * @return The number of rows in the last query executed
	 * @throws SQLException
	 */
	public int getLastQueryRowCount() throws SQLException {
		return getRowCount(lastQuery);
	}

	/**
	 * This method processes a query and returns the result as a String array.
	 * @param rs ResultSet object representing the query to process
	 * @param col String representing the column (field) to process
	 * @return String array containing the values of the specified column in the query
	 * @throws SQLException
	 */
	public static String[] processQuery(ResultSet rs, String col) throws SQLException {
		String[] retval = new String[getRowCount(rs)];
		int i = 0;

		rs.beforeFirst();
		while (rs.next()) {
			retval[i] = rs.getString(col) == null ? "NULL" : rs.getString(col);
			i++;
		}

		return retval;
	}

	/**
	 * This method processes the last query executed and returns the result as a String array.
	 * @param col String representing the column (field) to process
	 * @return String array containing the values of the specified column in the last query executed
	 * @throws SQLException
	 */
	public String[] processLastQuery(String col) throws SQLException {
		return processQuery(lastQuery, col);
	}

	/**
	 * This method processes the last query executed and returns the result in a table.
	 * @param rs ResultSet object representing the query to process
	 * @return String table containing the values of the query in a table format
	 * @throws SQLException
	 */
	public static String[][] processWholeQuery(ResultSet rs) throws SQLException {
		String[][] queryTable = {};

		ResultSetMetaData rsmd = rs.getMetaData();
		queryTable = new String[rsmd.getColumnCount()][];

		for (int i = 0; i < queryTable.length; i++) {
			queryTable[i] = processQuery(rs, rsmd.getColumnName(i + 1));
		}

		return queryTable;

	}

	/**
	 * This method processes the last query executed and returns the result in a table.
	 * @return String table containing the values of the last query executed in a table format
	 * @throws SQLException
	 */
	public String[][] processWholeLastQuery() throws SQLException {
		return processWholeQuery(lastQuery);
	}

	/**
	 * This method sets autocommit to false. This equals to suspending all operations until a commit (effectively, a transaction)
	 * @throws SQLException
	 */
	public void startTransaction() throws SQLException {
		cn.setAutoCommit(false);
	}

	/**
	 * This method commits the current transaction.
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		cn.commit();
		cn.setAutoCommit(true);
	}

	/**
	 * This method rollsback the current transaction; it is usually invoked when the commit fails.
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		cn.rollback();
		cn.setAutoCommit(true);
	}

	/**
	 * This method returns the last query executed.
	 * @return ResultSet object representing the last query executed
	 */
	public ResultSet getLastQuery() {
		return lastQuery;
	}

	/**
	 * This method returns the last update executed.
	 * @return Integer representing the last update executed
	 */
	public int getLastUpdate() {
		return lastUpdate;
	}

}
