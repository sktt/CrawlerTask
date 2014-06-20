package crawlertask.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class HTMLFetcher implements Runnable {
    private final URL url;
    private final ICallback<HTMLDocument> callback;
    
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final static String EVENT_NAME = "fetched";
    
    /**
     * Creates an observable HTML fetcher that connects and fetches the 
     * content of a URL, given that it contains HTML. Follows 301 and 302
     * redirects. Once the content has been fetched, notify observers.
     * @param url The URL to be fetched
     * @param callback The callback object to call when the task is complete.
     */
    public HTMLFetcher(URL url, ICallback<HTMLDocument> callback) {
        this.url = url;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        String result = "";
        try {
            HttpURLConnection connection = 
                    (HttpURLConnection)(url.openConnection());
            String contentType = connection.getHeaderField("Content-Type");

            if(contentType == null || !contentType.contains("html")) {
                // if we did not received the right kind of content, 
                // then we're not interested
                return;
            }

            result = fetchPageHTML(connection);
        } catch (IOException e) {
            LOGGER.warning("Something went wrong "
                    + "when fetching from: " + url 
                    + ": " + e.getMessage());
            return;
        }
        
        callback.callbackEvent(EVENT_NAME, new HTMLDocument(url, result));
    }
    
    /**
     * 
     * @param connection HTTP connection to a certain URL.
     * @return A String representation of the requested URL'S DOM or null if 
     * there was nothing to fetch
     * @throws IOException If there was a problem handling the data stream
     */
    private String fetchPageHTML(HttpURLConnection connection) 
            throws IOException {
        final int responseCode = connection.getResponseCode();
        
        // Follow redirects
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            return fetchPageHTML((HttpURLConnection)
                    new URL(connection.getHeaderField("Location"))
                    .openConnection());
        }
        
        // If OK wasn't received, let us know and return
        if (responseCode != HttpURLConnection.HTTP_OK) {
            LOGGER.warning("recieved status code " + 
                    responseCode + "! Skipping URL: "
                    + connection.getURL());
            return "";
        }
        
        // Read the content from the URL
        final BufferedReader rd = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        
        String line = "";
        final StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        
        rd.close();
        
        return sb.toString();
    }
    
}
