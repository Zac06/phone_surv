package it.zac06;

import java.math.BigDecimal;
import java.sql.*;
import java.io.*;
import java.net.*;

public class Database {
	private Connection cn;
	private Statement st;
	private PreparedStatement ps;
	private ResultSet lastQuery;
	private int lastUpdate;

	private static boolean loaded = false;

	public static void loadDriver() throws ClassNotFoundException {
		if (loaded) {
			return;
		}

		// Class.forName("com.mysql.jdbc.Driver");
		Class.forName("com.mysql.cj.jdbc.Driver");
		loaded = true;
	}

	public void open(DBParams dbpar) throws SQLException {
		open(dbpar.getHost(), dbpar.getPort(), dbpar.getDbname(), dbpar.getUsername(), dbpar.getPassword());
	}

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

	public Database(String host, int port, String dbName, String user, String password)
			throws SQLException, ClassNotFoundException {
		loadDriver();
		open(host, port, dbName, user, password);
	}

	public Database(DBParams dbpar) throws SQLException, ClassNotFoundException {
		loadDriver();
		open(dbpar);
	}

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

	public void closeLastQuery() throws SQLException{
		if (lastQuery != null && !lastQuery.isClosed()) {
			lastQuery.close();
			lastQuery = null;
		}
	}

	public ResultSet query(String sql) throws SQLException {
		lastQuery = st.executeQuery(sql);
		return lastQuery;
	}

	public int update(String sql) throws SQLException {
		lastUpdate = st.executeUpdate(sql);
		return lastUpdate;
	}

	public ResultSet preparedQuery(String sql, Object... params) throws SQLException {
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

		lastQuery=ps.executeQuery();
		return lastQuery;
	}

	public static int getRowCount(ResultSet rs) throws SQLException {
		int numRows;
		rs.last();
		numRows = rs.getRow();
		rs.beforeFirst();
		return numRows;
	}

	public int getLastQueryRowCount() throws SQLException {
		return getRowCount(lastQuery);
	}

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

	public String[] processLastQuery(String col) throws SQLException {
		return processQuery(lastQuery, col);
	}

	public static String[][] processWholeQuery(ResultSet rs) throws SQLException {
		String[][] queryTable = {};

		ResultSetMetaData rsmd = rs.getMetaData();
		queryTable = new String[rsmd.getColumnCount()][];

		for (int i = 0; i < queryTable.length; i++) {
			queryTable[i] = processQuery(rs, rsmd.getColumnName(i + 1));
		}

		return queryTable;

	}

	public String[][] processWholeLastQuery() throws SQLException {
		return processWholeQuery(lastQuery);
	}

	public void startTransaction() throws SQLException {
		cn.setAutoCommit(false);
	}

	public void commit() throws SQLException {
		cn.commit();
		cn.setAutoCommit(true);
	}

	public void rollback() throws SQLException {
		cn.rollback();
		cn.setAutoCommit(true);
	}

	// =========================================

	public ResultSet getLastQuery() {
		return lastQuery;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

}
