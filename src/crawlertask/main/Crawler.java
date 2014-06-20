package crawlertask.main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A web crawler that... 
 * - fetches the content for a given start URL 
 * - extracts the links from the content. 
 * - goes on to crawl the extracted links (back to step 1) 
 * - the crawler should stop after 1000 found URLs
 * 
 * @author J.Wikner
 * 
 */
public class Crawler implements ICallback<HTMLDocument> {
    public final static int URLS_TO_DISCOVER = 1000;
    public final static int TIMEOUT_VAL = 10;
    public final static int THREAD_POOL_SIZE = 100;
    public final static Logger LOGGER = Logger
            .getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ExecutorService fetcherExecutor = Executors
            .newFixedThreadPool(THREAD_POOL_SIZE);

    /**
     * Queue of URLs to be crawled
     */
    private final LinkedBlockingQueue<String> urlQueue = new LinkedBlockingQueue<String>();

    /**
     * URLs polled from the queue
     */
    private final Set<String> polledUrls = new TreeSet<String>();

    /**
     * Total number of discovered URLs, ie. polledUrls + urlQueue. Store in a
     * variable since the queue size is O(n) to check
     */
    private int discoveredUrls;

    /**
     * A lock for operations that need to be mutual exclusive. Ie. modifing a
     * list while doing look-ups on it.
     */
    private final Semaphore mutex = new Semaphore(1);

    /**
     * Crawler that goes through URLs by breadth-first and runs until a given
     * amount of URLs are discovered.
     */
    public Crawler(String seedUrlStr, int toDiscover)
            throws InterruptedException, FileNotFoundException,
            UnsupportedEncodingException {
        HTMLFetcher fetcher = null;
        urlQueue.add(seedUrlStr);
        String urlStr = "";
        while (discoveredUrls < toDiscover) {
            urlStr = urlQueue.poll(TIMEOUT_VAL, TimeUnit.SECONDS);

            if (urlStr == null) {
                // Happens if the crawler has waited for a new URL in the queue
                // for more than the timeout value. This means that all leafs
                // have been found and the crawler cannot continue.
                break;
            }

            // Acquire mutex to avoid collision when adding and
            // comparing at the same time
            mutex.acquireUninterruptibly();
            polledUrls.add(urlStr);
            mutex.release();

            System.out.println("Crawling: " + urlStr);
            try {
                fetcher = new HTMLFetcher(new URL(urlStr), this);
            } catch (MalformedURLException e) {
                continue;
            }

            fetcherExecutor.execute(fetcher);
        }
        System.out.println("DONE: Discovered " + discoveredUrls + " URLs");

        System.out.println("Dumping them into: urls.txt");
        Collection<String> toDump = new LinkedList<String>(polledUrls);
        toDump.addAll(urlQueue);
        dumpCollection(toDump, "urls.txt");

        System.out.println("Waiting for " + TIMEOUT_VAL + " seconds for "
                + Thread.activeCount() + " threads to terminate...");
        fetcherExecutor.awaitTermination(TIMEOUT_VAL, TimeUnit.SECONDS);
    }

    /**
     * Dumps a collection of Strings into a given file in the working directory.
     * 
     * @param fileName
     *            name of the file.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private void dumpCollection(Collection<String> strs, String fileName)
            throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");

        for (String str : strs) {
            writer.println(str);
        }

        writer.close();
    }

    @Override
    public void callbackEvent(String eventName, HTMLDocument htmlDoc) {
        if (HTMLFetcher.EVENT_NAME.equals(eventName)) {

            Collection<String> urls = HTMLParser.getHrefs(htmlDoc);

            // Acquire mutex to avoid updating polled and queue while
            // comparing it to found URLs
            mutex.acquireUninterruptibly();

            // Remove any previously visited
            urls.removeAll(polledUrls);
            urls.removeAll(urlQueue);

            urlQueue.addAll(urls);
            discoveredUrls += urls.size();
            mutex.release();
        }
    }

    public Set<String> getPolledUrls() {
        return polledUrls;
    }

    public Queue<String> getUrlQueue() {
        return urlQueue;
    }

    public static void main(String[] args) throws MalformedURLException,
            InterruptedException, FileNotFoundException,
            UnsupportedEncodingException {
        LOGGER.setLevel(Level.OFF);
        new Crawler(args[0], URLS_TO_DISCOVER);
        System.out.println("Exiting");
        System.exit(0);
    }
}
