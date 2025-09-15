import com.sun.amms.control.camera.CameraCtrl;
import com.sun.midp.main.Main;
import com.sun.mmedia.VideoControlProxy;

import javax.microedition.amms.control.camera.CameraControl;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.*;
import java.io.*;



public class PictureTaker extends Thread{
    private boolean running;
    private String dest;
    private int port;

    private Player pl;
    private VideoControl vc;
    private CameraControl cc;
    private DummyCanvas canvas;

    private Semaphore me;
    private Semaphore him;

    private SocketConnection sock;
    private DataInputStream in;
    private DataOutputStream out;

    private int frames_per_interval;
    private String cam_name;

    public PictureTaker(String name, String dest, int port, int frames_per_interval, String cam_name) {
        super(name);
        this.port=port;
        this.dest=dest;
        this.running=true;

        this.me=new Semaphore(0);
        this.him=new Semaphore(0);

        this.frames_per_interval=frames_per_interval;
        this.cam_name=cam_name;
    }

    public void run(){
        try{
            canvas=new DummyCanvas();

            pl=Manager.createPlayer("capture://video");
            pl.realize();
            pl.prefetch();
            pl.start();

            VideoControl vc = (VideoControl) pl.getControl("VideoControl");
            if (vc != null){
                cc = (CameraControl) pl.getControl("javax.microedition.amms.control.camera.CameraControl");
                if (cc != null) {
                    try {
                        cc.enableShutterFeedback(false);

                        int[] resolutions=cc.getSupportedStillResolutions();
                        MainMIDlet.log("AVAILABLE VIDEO RESes: "+Arrays.toString(resolutions));

                    } catch (Exception e) {
                        // Handle the case where muting is not supported
                    }
                }

                vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, canvas);

                sock=(SocketConnection) Connector.open("socket://"+dest+":"+port);
                MainMIDlet.log(sock.getLocalAddress());

                in=sock.openDataInputStream();
                out=sock.openDataOutputStream();

                out.writeInt(cam_name.getBytes().length);
                out.write(cam_name.getBytes());

                out.writeInt(frames_per_interval); //frames per interval

                int count=0;
                while(running){


                    byte[] image = vc.getSnapshot("encoding=jpeg"); // or "encoding=jpeg"
                    MainMIDlet.log("Captured image size: " + image.length + " bytes");

                    MainMIDlet.log("Image number "+count+" size: " + image.length + " bytes");

                    out.writeInt(image.length);
                    out.write(image);

                    MainMIDlet.log("Image number "+count+" was sent");

                    count++;

                }
            }

            in.close();
            out.close();
            sock.close();

            pl.close();

        } catch (Exception e) {
            MainMIDlet.log("PICTURETAKER: "+e.toString());
        }
    }

    public void stop(){
        running=false;

    }

}
