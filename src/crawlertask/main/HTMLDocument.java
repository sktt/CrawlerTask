package crawlertask.main;

import java.net.URL;

public class HTMLDocument {
    private final URL sourceUrl;
    private final String content;
    
    public HTMLDocument(URL sourceUrl, String content) {
        this.sourceUrl = sourceUrl;
        this.content   = content;
    }
    
    public URL getSourceUrl() {
        return sourceUrl;
    }
    
    public String getContent() {
        return content;
    }
}
