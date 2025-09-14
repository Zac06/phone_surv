package it.zac06;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReceiverThread extends Thread {
	private Socket sock;
	private DataInputStream in;
	private DataOutputStream out;

	private boolean running;

	private Database db;
	private DBParams dbpar;

	private MD5Computer md5;

	private Queue<IntervalCloserThread> closerThreads;
	private List<ReceiverThread> cameraThreads;
	
	private LiveFrame sharedFrame;
	private FrameNotifier fn;

	public ReceiverThread(Socket sock, String name, DBParams dbpar, List<ReceiverThread> cameraThreads) throws ClassNotFoundException, SQLException {
		this.sock = sock;
		setName(name);
		
		this.running = true;

		this.md5 = new MD5Computer();

		this.dbpar = dbpar;
		
		this.sharedFrame=new LiveFrame();
		this.fn=new FrameNotifier();
		this.cameraThreads=cameraThreads;

		this.closerThreads = new LinkedList<IntervalCloserThread>();
		
	}

	public void run() {
		try {
			this.db = new Database(dbpar);

			in = new DataInputStream(sock.getInputStream()); // open data streams
			out = new DataOutputStream(sock.getOutputStream());
		} catch (ClassNotFoundException | SQLException | IOException e) {
			e.printStackTrace();

			closeThings();
			return;
		}

		// ================================

		// check if there already is a camera; if not, add it

		int idCam = 0;


		try {
			int camnameSize=in.readInt();
			setName(new String(in.readNBytes(camnameSize)));
			
			//System.out.println("HAH");
			System.out.println("["+getName()+"] Just connected");
			
			db.query("select id_cam from telecamera where nome='" + getName() + "'");

			if (db.getLastQueryRowCount() != 1) {
				db.update("insert into telecamera values (0, '" + getName() + "')");
				db.query("select last_insert_id() as id_cam");
			}

			// db.query("select * from telecamera where nome='"+getName()+"'");
			idCam = Integer.parseInt(db.processLastQuery("id_cam")[0]);

		} catch (SQLException | NumberFormatException e) {
			System.out.println("[" + getName() + "] Failed to add (or retrieve) camera name from database: " + getName());
			e.printStackTrace();
			closeThings();

			return;
		} catch (IOException e) {
			System.out.println("[" + getName() + "] Failed to add (or retrieve) camera name from client: " + getName());
			e.printStackTrace();
			closeThings();

			return;
		}

		// ======================================

		closeAlreadyExistingIntervals(idCam);

		// ========================================

		// receiving information
		// insert interval and start up everything
		try {
			int framesPerInterval = 0;
			int currentFramecount = 0;

			framesPerInterval = in.readInt();
			System.out.println("[" + getName() + "] Set the framesPerInterval to " + framesPerInterval + ".");

			while (running) {
				db.update("insert into intervallo values (0, now(), NULL, " + idCam + ");");
				db.query("select last_insert_id() as id_int");
				int idInt = Integer.parseInt(db.processLastQuery("id_int")[0]); // we are pretty much certain that it's
																					// going to parse into a number,
																					// since id_int is a MySQL integer.
																					// Also, last_insert_id() always
																					// gives back something.
				currentFramecount = 0;

				while (currentFramecount < framesPerInterval && running) {
					int size = in.readInt();
					if (size == -1) {
						stopThread();
						break;
					}
					System.out.println("[" + getName() + "] Estimated image " + currentFramecount + " of interval "
							+ idInt + " size: " + size);

					byte[] image = in.readNBytes(size);
					System.out.println("[" + getName() + "] Received image " + currentFramecount + " of interval " + idInt
							+ " effective size: " + image.length);

					String filenameDigest = md5.compute(image);
					String filenameDatetime = LocalDateTime.now().toString().replace(':', '-'); // replace illegal
																									// characters
					String picName = filenameDatetime + "_" + filenameDigest + ".jpg";

					try (FileOutputStream fout = new FileOutputStream(
							new File(ParameterLoader.getDataPath()+"/"+picName)); /* create an unique identifier */) {
						fout.write(image);
						System.out.println("[" + getName() + "] Saved the image " + currentFramecount + " of interval "
								+ idInt + " as " + picName);
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println(
								"[" + getName() + "] Could not add the picture '" + picName + "' to the storage.");
						continue;
					}

					try {
						db.update("insert into foto values (0, '" + picName + "', " + idInt + ")");
					} catch (SQLException e) {
						e.printStackTrace();
						System.out.println(
								"[" + getName() + "] Could not add the picture '" + picName + "' to the database.");
						continue;
					}
					
					sharedFrame.setImage(image);
					fn.signalNewFrame();

					currentFramecount++;

				}

				IntervalCloserThread tmpIct = new IntervalCloserThread(dbpar, "" + idInt);
				closerThreads.add(tmpIct);
				tmpIct.start();

			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		closeAlreadyExistingIntervals(idCam);
		closeThings();
		
		System.out.println("[" + getName() + "] Terminated\n");
		
		
	}

	public void stopThread() {
		running = false;
	}

	public void closeAlreadyExistingIntervals(int idCam) {
		// close the already existing intervals
		int rc = -1;
		try {
			db.query("select * from intervallo where fine is null and id_cam=" + idCam);
			rc = db.getLastQueryRowCount();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[" + getName() + "] Error while retrieving the intervals.");

			closeThings();
			return;
		}

		if (rc >= 1) {
			// close the already existing intervals.

			try {
				String[] existingIntervals = db.processLastQuery("id_int");
				IntervalCloserThread[] icts = new IntervalCloserThread[existingIntervals.length];

				for (int i = 0; i < existingIntervals.length; i++) {
					// AAAAAAAAAAAAAAAAAAa
					icts[i] = new IntervalCloserThread(dbpar, existingIntervals[i]);
					icts[i].start();

				}

				for (int i = 0; i < icts.length; i++) {
					try {
						icts[i].join();

					} catch (InterruptedException e) {
						e.printStackTrace();
						System.out.println("[" + getName() + "] Error while joining on IntervalCloserThread no. "
								+ icts[i].getName());
					}

				}

			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("[" + getName()
						+ "] Error while retrieving information necessary to store the unclosed intervals.");

				closeThings();
				return;
			}

		}
	}

	public void closeThings() {
		if(!running) {
			return;
		}
		stopThread();
		
		try {
			if(in!=null) {
				in.close();
				in=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if(out!=null) {
				out.close();
				out=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if(sock!=null) {
				sock.close();
				sock=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if(db!=null) {
				db.closeConnection();
				db=null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		fn.signalNoMoreFrames();
		
		synchronized (cameraThreads) {
			cameraThreads.remove(this);
		}
		
		while (!closerThreads.isEmpty()) {
			try {
				closerThreads.remove().join(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

	public LiveFrame getSharedFrame() {
		return sharedFrame;
	}
	
	public FrameNotifier getFn() {
		return fn;
	}
	
	
}
