package it.zac06;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ParameterLoader {
	private static String dbHost;
	private static int dbPort;
	private static String dbName;
	private static String dbUsername;
	private static String dbPassword;
	
	private static String dataPath;
	
	private static int cameraServerPort;
	private static int liveServerPort;
	
	public static void load(String filename) throws FileNotFoundException, IOException, IllegalPropertyDataException {
		try (BufferedReader fin = new BufferedReader(new FileReader(filename))) {

			String line = "";
			int i = 1;

			while ((line = fin.readLine()) != null) {
				String[] parts = line.split("=", 2);
				if (parts.length != 2) {
					throw new IllegalPropertyDataException("Line " + i
							+ " of the "+filename+" file is not defined correctly. Please define the properties with the following structure: <property name>=<data>. NO EXTRA SPACES");
				}

				switch (parts[0]) {
					case "dbHost": {
						dbHost = parts[1];
						break;
					}
					case "dbPort": {
						try {
							dbPort = Integer.parseInt(parts[1]);
						} catch (NumberFormatException e) {
							throw new IllegalPropertyDataException("Line " + i
									+ " of the "+filename+" file is not defined correctly. Please insert a valid dbPort number.");
						}
						break;
					}
					case "dbName": {
						dbName = parts[1];
						break;
					}
					case "dbUsername": {
						dbUsername = parts[1];
						break;
					}
					case "dbPassword": {
						dbPassword = parts[1];
						break;
					}
					case "dataPath": {
						try {
							Paths.get(parts[1]);
							dataPath = parts[1];
						} catch (InvalidPathException e) {
							throw new IllegalPropertyDataException("Line " + i
									+ " of the "+filename+" file is not defined correctly. Please insert a valid dataPath.");
						}
						
						break;
					}
					case "cameraServerPort": {
						try {
							cameraServerPort = Integer.parseInt(parts[1]);
						} catch (NumberFormatException e) {
							throw new IllegalPropertyDataException("Line " + i
									+ " of the "+filename+" file is not defined correctly. Please insert a valid cameraServerPort number.");
						}
						break;
					}
					case "liveServerPort": {
						try {
							liveServerPort = Integer.parseInt(parts[1]);
						} catch (NumberFormatException e) {
							throw new IllegalPropertyDataException("Line " + i
									+ " of the "+filename+" file is not defined correctly. Please insert a valid liveServerPort number.");
						}
						break;
					}
					default: {
						/*throw new IllegalPropertyDataException("Line " + i
								+ " of the "+filename+" file is not defined correctly. Please insert a valid property name.");*/
						System.out.println("Unknown property at line "+i+" of the "+filename+" for the DBParams constructor.");
					}
				}

				

				i++;
			}
			if (dbHost == null || dbName == null || dbUsername == null || dbPassword == null || dbPort <= 0) {
				throw new IllegalPropertyDataException("Missing one or more DB parameters in "+filename+" file, or invalid parameters");
			}
		}
	}

	public static String getDbHost() {
		return dbHost;
	}

	public static int getDbPort() {
		return dbPort;
	}

	public static String getDbName() {
		return dbName;
	}

	public static String getDbUsername() {
		return dbUsername;
	}

	public static String getDbPassword() {
		return dbPassword;
	}

	public static String getDataPath() {
		return dataPath;
	}

	public static int getCameraServerPort() {
		return cameraServerPort;
	}

	public static int getLiveServerPort() {
		return liveServerPort;
	}
	
	
}
