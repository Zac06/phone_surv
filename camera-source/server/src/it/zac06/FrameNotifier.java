package it.zac06;
public class FrameNotifier {
    private long version = 0;        // increments each frame
    private final Object lock = new Object();

    /** Called by the producer (camera) when a new frame is ready. */
    public void signalNewFrame() {
        synchronized (lock) {
            version++;
            lock.notifyAll(); // wake all waiting threads
        }
    }
    
    public void signalNoMoreFrames() {
    	synchronized (lock) {
    		version=-1;
    		lock.notifyAll();
    	}
    }

    /** Called by consumers to block until a newer frame is available. */
    public void waitForNextFrame(long lastVersion) throws InterruptedException {
        synchronized (lock) {
            while (version == lastVersion) {
                lock.wait();
            }
        }
    }

    /** Returns the current version number. */
    public long getVersion() {
        synchronized (lock) {
            return version;
        }
    }
}
