/**

 *
 * A copier thread.
 * Reads files to copy from a queue and copies them to the given destination.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Copier implements Runnable{

    public static final int COPY_BUFFER_SIZE = 4096;
    private final File f_DestFile;
    private SynchronizedQueue<File> m_ResultQueue;

    /**
     * Constructor.
     * Initializes the worker with a destination directory and a queue of files to copy.
     * @param destination - Destination directory
     * @param resultsQueue - Queue of files found, to be copied
     */
    public Copier(File destination, SynchronizedQueue<File> resultsQueue){
        this.f_DestFile = destination;
        this.m_ResultQueue = resultsQueue;
    }

    /**
     * Runs the copier thread.
     * Thread will fetch files from queue and copy them, one after each other, to the destination directory.
     * When the queue has no more files, the thread finishes.
     */
    public void run(){
        File currFile = m_ResultQueue.dequeue();


        while(currFile != null){
            try {
                //if the destination directory doesn't exits, create it
                if(!f_DestFile.exists()) {
                    f_DestFile.mkdirs();
                }
                //creates the file in the dest directory
                File resultFile = new File(f_DestFile, currFile.getName());
                //copies the files content / replaces files with the same name
                Files.copy(currFile.toPath(), resultFile.toPath(), REPLACE_EXISTING);
                currFile = m_ResultQueue.dequeue();
            }catch(IOException e){
                e.printStackTrace();
                return;
            }
        }
    }
}
