package it.zac06;
import org.java_websocket.*;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class LiveSenderThread extends Thread {
	//private List<ReceiverThread> camera_threads;
	//private Socket sock;
	//private DataInputStream in;
	//private DataOutputStream out;
	
	private WebSocket ws;
	
	private FrameNotifier fn;
	private LiveFrame lf;
	
	private boolean running;
	private long lastVersion;
	
	public LiveSenderThread(WebSocket ws, LiveFrame lf, FrameNotifier fn, String name) {
		//this.camera_threads=camera_threads;
		//this.sock=sock;
		
		this.running=true;
		
		this.lf=lf;
		this.fn=fn;
		setName(name);
		this.ws=ws;
		
		
	}
	
	public void run() {
		try {

			lastVersion=fn.getVersion();
			
			while(running) {
				fn.waitForNextFrame(lastVersion);
				lastVersion=fn.getVersion();
				if(lastVersion==-1) {
					System.out.println("["+getName()+"] Has been notified that the camera has stopped. Terminating.");
					
					closeThings();
					
					break;
				}
				
				byte[] image=lf.getImage();
				
				try {
					ws.send(image);
				} catch (WebsocketNotConnectedException e) {
					e.printStackTrace();
					
					System.out.println("["+getName()+"] Ended because the connection closed.");
					
					closeThings();
					break;
				}
				
			}
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			
			System.out.println("["+getName()+"] Ending the connection with viewer because of a thread timing error");
		} finally {
			closeThings();
		}
	}
	
	public void closeThings() {
		stopThread();
		
		if (ws != null && ws.isOpen()) {
	        ws.close();
	    }
	}
	
	public void stopThread() {
		running=false;
	}
}
