package crawlertask.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Test;

import crawlertask.main.Crawler;
import crawlertask.main.HTMLDocument;
import crawlertask.main.HTMLFetcher;
import crawlertask.main.ICallback;

public class CrawlerAndFetcherTest {

    private final static String NON_HTML_URL1 = 
            "https://www.google.de/logos/doodles/2014/world-cup-2014-11-6294918834159616.3-hp.gif";
    private final static String NON_HTML_URL2 = 
            "https://www.google.de/robots.txt";
    private final static String HTML_URL = 
            "https://www.google.de/";
    @Test
    public void testDiscoveredUrls() throws InterruptedException, 
            FileNotFoundException, UnsupportedEncodingException {
        int testAmt = 100;
        Crawler c = new Crawler("http://www.google.de", testAmt);
        assertTrue("Should at least have discovered " + testAmt
                +" urls.", c.getPolledUrls().size() + c.getUrlQueue().size() >= testAmt);
    }
    
    @Test
    public void testHTMLFetch() throws MalformedURLException {
        ICallback<HTMLDocument> failCallback = new ICallback<HTMLDocument>() {
            @Override
            public void callbackEvent(String eventName, HTMLDocument result) {
                fail();
            }
        };
        
        HTMLFetcher f = new HTMLFetcher(new URL(NON_HTML_URL1), failCallback);
        f.run();
        
        f = new HTMLFetcher(new URL(NON_HTML_URL2), failCallback);
        f.run();
        
        f = new HTMLFetcher(new URL(HTML_URL), new ICallback<HTMLDocument>() {
            @Override
            public void callbackEvent(String eventName, HTMLDocument result) {
                assertTrue("Should have found a document", result.getContent().startsWith("<!"));
            }
        });
        f.run();
    }
    
    @AfterClass
    public static void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get("urls.txt"));
    }

}
