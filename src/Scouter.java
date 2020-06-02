/**

 *
 * A scouter thread This thread lists all sub-directories from a given root path.
 * Each sub-directory is enqueued to be searched for files by Searcher threads.
 */
import java.io.File;

public class Scouter implements Runnable {

    //A queue for directories to be searched
    private SynchronizedQueue<File> m_DirectoryQueue;
    //Root directory to start from
    private final File f_Root;

    /**
     * Constructor.
     * Initializes the scouter with a queue for the directories to be searched and a root directory to start from.
     * @param directoryQueue - A queue for directories to be searched
     * @param root - Root directory to start from
     */
    public Scouter(SynchronizedQueue<File> directoryQueue, File root ){
        this.m_DirectoryQueue = directoryQueue;
        this.f_Root = root;
    }

    /**
     * Starts the scouter thread.
     * Lists directories under root directory and adds them to queue, then lists directories in the next level and enqueues them and so on.
     * This method begins by registering to the directory queue as a producer and when finishes, it unregisters from it.
     */
    public void run(){
        // first register the producer
        m_DirectoryQueue.registerProducer();

        enqueueFiles(f_Root);

        //lastly unregister the producer
        m_DirectoryQueue.unregisterProducer();
    }

    /**
     * recursivly places all directories and subdirectories in the queue
     * @param currentRoot - current root directory to check
     */
    private void enqueueFiles(File currentRoot){
        // stop condition
        if (currentRoot.listFiles().length == 0) return;
        // recursivly places all files in the queue
        for(File subDir : currentRoot.listFiles(File::isDirectory)){
            m_DirectoryQueue.enqueue(subDir);
            enqueueFiles(subDir);
        }
    }
}
