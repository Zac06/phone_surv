package it.zac06;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class ServerLiveSender extends WebSocketServer {
	private List<ReceiverThread> cameraThreads;
	private List<WebSocket> cameraConnections;

	public ServerLiveSender(List<ReceiverThread> camera_threads) {
		super(new InetSocketAddress(ParameterLoader.getLiveServerPort()));

		this.cameraThreads = camera_threads;
		this.cameraConnections = new ArrayList<WebSocket>();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("[ServerLiveSender] Browser connesso: " + conn.getRemoteSocketAddress());
		// Puoi chiedere quale camera vuole subito qui
	}

	@Override
	public void onMessage(WebSocket conn, String message) {

		System.out.println("[ServerLiveSender] Richiesta: " + message);

		if (message.startsWith("camera:")) {
			String cameraName = message.substring("camera:".length());

			synchronized (cameraThreads) {
				int found = 0;

				if (cameraConnections.indexOf(conn) != -1) {
					found = 2;
				} else {
					for (ReceiverThread rt : cameraThreads) {
						if (cameraName.equals(rt.getName())) {
							LiveSenderThread tmp = new LiveSenderThread(conn, rt.getSharedFrame(), rt.getFn(),
									rt.getName() + "-LiveSenderThread-" + conn.hashCode());
							tmp.start();
							found = 1;
							cameraConnections.add(conn);
							System.out.println(
									"[ServerLiveSender] Aggiunto un nuovo visualizzatore live per la telecamera "
											+ rt.getName());

							break;
						}
					}
				}

				if (found == 0) {
					System.out.println("[ServerLiveSender] La telecamera richiesta (" + cameraName
							+ ") non è attualmente in diretta.");
					conn.close(1000, "La telecamera richiesta non è al momento disponibile");
					
					cameraConnections.remove(conn);
				} else if (found == 2) {
					System.out.println(
							"[ServerLiveSender] Questo client (" + cameraName + ") ha già richiesto una telecamera.");
				}
			}

		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		cameraConnections.remove(conn);

		System.out.println("[ServerLiveSender] Browser disconnesso");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		// ex.printStackTrace();
	}

	@Override
	public void onStart() {
		System.out.println("[ServerLiveSender] WebSocket server avviato su " + getPort());
	}
}
