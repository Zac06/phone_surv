package it.zac06;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LiveFrame {
    private byte[] image;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LiveFrame() {
        this.image = new byte[0];
    }

    /** Replace the stored frame with a new one (exclusive). */
    public void setImage(byte[] newImage) {
        lock.writeLock().lock();
        try {
            // defensive copy
            this.image = Arrays.copyOf(newImage, newImage.length);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Get a copy of the current frame (shared). */
    public byte[] getImage() {
        lock.readLock().lock();
        try {
            //return Arrays.copyOf(image, image.length);
        	return image;
        } finally {
            lock.readLock().unlock();
        }
    }
}
