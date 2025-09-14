package it.zac06;
public class DBParams {
	private String host;
	private int port;
	private String dbname;
	private String username;
	private String password;
	
	public DBParams() {
		this.host = ParameterLoader.getDbHost();
		this.port = ParameterLoader.getDbPort();
		this.dbname = ParameterLoader.getDbName();
		this.username = ParameterLoader.getDbUsername();
		this.password = ParameterLoader.getDbPassword();
	}

	public DBParams(String host, int port, String dbname, String username, String password) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDbname() {
		return dbname;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	

}
