/**
 *
 *
 * A searcher thread.
 * Searches for files containing a given pattern and that end with a specific extension in all directories listed in a directory queue.
 */
import java.io.File;
import java.io.FilenameFilter;

public class Searcher implements Runnable {

    private final FilenameFilter f_WantedFile;
    private SynchronizedQueue<File> m_DirectoryQueue;
    private SynchronizedQueue<File> m_ResultsQueue;

    /**
     * Constructor. Initializes the searcher thread.
     * @param pattern - Pattern to look for
     * @param extension - wanted extension
     * @param directoryQueue - A queue with directories to search in (as listed by the scouter)
     * @param resultsQueue - A queue for files found (to be copied by a copier)
     */
    public Searcher(String pattern,String extension, SynchronizedQueue<File> directoryQueue, SynchronizedQueue<File> resultsQueue){

        this.m_DirectoryQueue = directoryQueue;
        this.m_ResultsQueue = resultsQueue;
        this.f_WantedFile = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension) && name.contains(pattern);
            }
        };
    }

    /**
     * Runs the searcher thread.
     * Thread will fetch a directory to search in from the directory queue, then search all files inside it (but will not recursively search subdirectories!).
     * Files that a contain the pattern and have the wanted extension are enqueued to the results queue.
     * This method begins by registering to the results queue as a producer and when finishes, it unregisters from it.
     *
     */
    public void run(){
        //first register as producer
        m_ResultsQueue.registerProducer();

        File root = m_DirectoryQueue.dequeue();
        // will return null when the queue is empty and no producers
        while(root != null){
            File[] dirList = root.listFiles(f_WantedFile);

            if(dirList != null) {
                for (File subDir : dirList) {
                    m_ResultsQueue.enqueue(subDir);
                }
            }

            root = m_DirectoryQueue.dequeue();
        }

        // lastly  unregister
        m_ResultsQueue.unregisterProducer();
    }
}
