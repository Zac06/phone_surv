package it.zac06;
import java.sql.*;
import java.util.Arrays;

public class Database {
	private Connection cn;
	private Statement st;
	private ResultSet lastQuery;
	private int lastUpdate;
	
	private static boolean loaded=false;
	
	public static void loadDriver() throws ClassNotFoundException {
		if(loaded) {
			return;
		}

		//Class.forName("com.mysql.jdbc.Driver");
		Class.forName("com.mysql.cj.jdbc.Driver");
		loaded=true;
	}
	
	public void openConnection(DBParams dbpar) throws SQLException {
		cn = (Connection) DriverManager.getConnection("jdbc:mysql://"+dbpar.getHost()+":"+dbpar.getPort()+"/"+dbpar.getDbname(), dbpar.getUsername(), dbpar.getPassword());
		//st=cn.createStatement();
		st = cn.createStatement(
		    ResultSet.TYPE_SCROLL_INSENSITIVE, 
		    ResultSet.CONCUR_READ_ONLY
		);
		
		lastQuery=null;
		lastUpdate=-1;
	}
	
	public void openConnection(String host, int port, String dbName, String user, String password) throws SQLException {
		cn = (Connection) DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+dbName, user, password);
		//st=cn.createStatement();
		st = cn.createStatement(
		    ResultSet.TYPE_SCROLL_INSENSITIVE, 
		    ResultSet.CONCUR_READ_ONLY
		);
		
		lastQuery=null;
		lastUpdate=-1;
	}
	
	public Database (String host, int port, String dbName, String user, String password) throws SQLException, ClassNotFoundException {
		loadDriver();
		openConnection(host, port, dbName, user, password);
	}
	
	public Database (DBParams dbpar) throws SQLException, ClassNotFoundException {
		loadDriver();
		openConnection(dbpar);
	}
	
	public void closeConnection() throws SQLException {
		st.close();
		cn.close();
	}
	
	public ResultSet query(String sql) throws SQLException {
		lastQuery=st.executeQuery(sql);
		return lastQuery;
	}
	
	public int update(String sql) throws SQLException{
		lastUpdate=st.executeUpdate(sql);
		return lastUpdate;
	}
	
	public static String[] processQuery(ResultSet rs, String col) throws SQLException {
		String[] retval= {};
		
		rs.beforeFirst();
		while(rs.next()) {
			retval=Arrays.copyOf(retval, retval.length+1);
			retval[retval.length-1]=rs.getString(col)==null ? "NULL" : rs.getString(col);
		}
		
		return retval;
	}
	
	public String[] processLastQuery(String col) throws SQLException {
		return processQuery(lastQuery, col);
	}
	
	public static String[][] processWholeQuery(ResultSet rs) throws SQLException {
		String[][] queryTable= {};
		
		ResultSetMetaData rsmd=rs.getMetaData();
		queryTable=new String[rsmd.getColumnCount()][];
		
		for(int i=0; i<queryTable.length; i++) {
			queryTable[i]=processQuery(rs, rsmd.getColumnName(i+1));
		}
		
		return queryTable;
		
	}
	
	public String[][] processWholeLastQuery() throws SQLException {
		return processWholeQuery(lastQuery);
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
	
	//=========================================

	public ResultSet getLastQuery() {
		return lastQuery;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}
	
	
}

