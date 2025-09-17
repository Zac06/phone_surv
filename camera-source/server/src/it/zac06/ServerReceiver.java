package it.zac06;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServerReceiver {
	private static ServerSocket server;
	private static int index;
	private static DBParams dbpar;
	private static List<ReceiverThread> cameraThreads;

    private static boolean running=true;
	
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, IllegalPropertyDataException {
    	ParameterLoader.load(".properties");

        File directory = new File(ParameterLoader.getDataPath());
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // creates parent dirs if needed
            if (created) {
                System.out.println("[ServerReceiver] Data directory created: " + ParameterLoader.getDataPath());
            } else {
                System.out.println("[ServerReceiver] Failed to create data directory.");
                return;
            }
        } else {
            System.out.println("[ServerReceiver] Data directory already exists.");
        }
    	
        server=new ServerSocket(ParameterLoader.getCameraServerPort());
        dbpar=new DBParams();
        index=0;
        
        cameraThreads=new ArrayList<ReceiverThread>();
        
        System.out.println("[ServerReceiver] Avviato su "+server.getLocalPort());
        
        ServerLiveSender sls=new ServerLiveSender(cameraThreads);
        sls.start();
        
        while(running){
            ReceiverThread tmp=new ReceiverThread(server.accept(), "cam-"+Integer.toString(index), dbpar, cameraThreads);
            tmp.start();
            cameraThreads.add(tmp);
        }
        
        server.close();
    }
}
