package it.zac06;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class IntervalCloserThread extends Thread {
	private Database db;
	private DBParams dbpar;

	public IntervalCloserThread(DBParams dbpar, String name) {
		super();
		this.dbpar = dbpar;
		this.setName(""+Integer.parseInt(name));
	}
	
	public void run() {
		try {
			db=new Database(dbpar);
			
			String videonameDatetime=LocalDateTime.now().toString().replace(':', '-');
        	String videoFilename=getName()+"_"+videonameDatetime+".mp4";
        	String textFilename=getName()+"_"+videonameDatetime+".txt";
        	
        	//create the file comtaining all the filenames
        	BufferedWriter fout=new BufferedWriter(new FileWriter(ParameterLoader.getDataPath()+"/"+textFilename));
        	
			db.preparedQuery("select nomefile_f from foto where id_int=? order by nomefile_f", getName());
        	String[] photos=db.processLastQuery("nomefile_f");
        	for(int i=0; i<photos.length; i++) {
        		fout.write("file '"+photos[i]+"'\n");
        	}
        	
        	fout.close();
        	
        	
        	//avoid spaces in filenames issues
        	Process p = Runtime.getRuntime().exec(new String[]{		
        		    "ffmpeg", "-f", "concat", "-i", ParameterLoader.getDataPath()+"/"+textFilename,
        		    "-c:v", "libx264", "-r", "24", "-pix_fmt", "yuv420p",
        		    ParameterLoader.getDataPath()+"/"+videoFilename, "-y"
        		});
			
        	new Thread(() -> {
        	    try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
        	        String l;
        	        while ((l = r.readLine()) != null) {
        	            System.err.println("[ffmpeg-" + getName() + "] " + l);
        	        }
        	    } catch (IOException ignored) {}
        	}).start();

        	new Thread(() -> {
        	    try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        	        String l;
        	        while ((l = r.readLine()) != null) {
        	            System.out.println("[ffmpeg-" + getName() + "] " + l);
        	        }
        	    } catch (IOException ignored) {}
        	}).start();
            
			int ffmpe_exit_code=p.waitFor();
			if(ffmpe_exit_code!=0) {
				throw new IOException("[ffmpeg-"+getName()+"] FFmpeg exited with code " + ffmpe_exit_code);
			}
			
			db.startTransaction();
			
			db.preparedUpdate("insert into video (nomefile_v, id_int) values (?, ?)", videoFilename, getName());
			db.preparedUpdate("update intervallo set fine=now() where id_int=?", getName());
			
			//cleaning up the files/remnants
			File fdel=new File(ParameterLoader.getDataPath()+"/"+textFilename);
			fdel.delete();
			
			db.preparedUpdate("delete from foto where id_int=?", getName());
			
			for(int i=0; i<photos.length; i++) {
        		File photo_del=new File(ParameterLoader.getDataPath()+"/"+photos[i]);
        		photo_del.delete();
        	}
			
			db.commit();
			
			db.close();
			
		} catch (SQLException ee) {
			ee.printStackTrace();
			System.out.println("Error while updating information necessary to close the unclosed intervals. Interval number: "+getName());
			
			try {
				db.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("What the hell?");
			}
			
		} catch (IOException | InterruptedException ee) {
			ee.printStackTrace();
			System.out.println("Error while creating the video necessary to close the unclosed intervals. Interval number: "+getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Something's wrong with JDBC or MySQL...");
		} 
		
	}
	
	
}
