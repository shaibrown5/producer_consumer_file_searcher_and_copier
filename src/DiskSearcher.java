/**

 *
 * Main application class.
 * This application searches for all files under some given path that contain a given textual pattern.
 * All files found are copied to some specific directory.
 */
import java.io.File;
import java.io.FileNotFoundException;

public class DiskSearcher {

    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;

    /**
     * Constructor
     */
    public DiskSearcher(){}

    /**
     * Main method.
     * Reads arguments from command line and starts the search.
     * Example for input:
     * java DiskSearcher <filename-pattern> <file-extension> <root directory> <destination directory> <# of searchers> <# of copiers>
     * @param args - Command line arguments
     */
    public static void main(String[] args){

        // <filename-pattern> <file-extension> <root directory> <destination directory> <# of searchers> <# of copiers>")
        //check number of arguments entered
        if(args.length != 6){
            throw  new IllegalArgumentException("Illegal amount of arguments entered");
        }

        // checks that the numbers are numbers and that they are positive
        try {
            int numOfSearcher = Integer.parseInt(args[4]);
            int numOfCopiers = Integer.parseInt(args[5]);

            if( numOfCopiers <= 0 || numOfSearcher <= 0 ){
                throw new NumberFormatException();
            }

            SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
            SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
            File rootDir = new File(args[2]);
            File destFile = new File(args[3]);
            if(!rootDir.exists()){
                throw new FileNotFoundException();
            }

            //creates single Scouter thread
            Scouter scout = new Scouter(directoryQueue, rootDir);
            Thread scoutThread = new Thread(scout);
            scout.run();

            //creates the given amount of searcher threads
            for(int i = 0 ;i < numOfSearcher ; i++){
                Searcher searcher = new Searcher(args[0], args[1], directoryQueue, resultsQueue);
                Thread searchThread = new Thread(searcher);
                searcher.run();
            }

            //creates the given amount of copierThreads
            for(int i = 0 ;i < numOfCopiers ; i++){
                Copier copier = new Copier(destFile, resultsQueue);
                Thread copierThread = new Thread(copier);
                copier.run();
            }
        }catch (NumberFormatException e){
            System.out.println("Amount of searchers and amount of copiers must be a positive number");
            e.printStackTrace();
        }catch (FileNotFoundException e){
            System.out.println("Root Directory not found");
            e.printStackTrace();
        }



    }

}
