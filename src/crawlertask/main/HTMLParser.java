package crawlertask.main;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Helper class holding static methods for parsing HTML. 
 */
public abstract class HTMLParser {

    public final static String FIND_HREF_PATTERN = 
            "<a\\s+(?:[^>]*?\\s+)?href=\"((?!(#|\\/\"))[^\"]*)\"";
    
    /**
     * @param doc HTMLDocument to parse
     * @return A Set of links found in a HTMLDocument and reformatted as
     * absolute if necessary.
     */
    public static Set<String> getHrefs(HTMLDocument doc) {
        final Set<String> results = new TreeSet<String>();
        final String html = doc.getContent();
        final URL sourceUrl = doc.getSourceUrl();
        final Pattern findHrefs = 
                Pattern.compile(FIND_HREF_PATTERN);
        final Matcher matchHrefs = findHrefs.matcher(html);
        
        String urlStr = "";
        while (matchHrefs.find()) {
            urlStr = matchHrefs.group(1);
            urlStr = formatUrlStr(urlStr, sourceUrl);
            if (urlStr != null) {
                results.add(urlStr);
            }
        }
        
        return results;
    }

    /**
     * @param hrefStr String to format
     * @param sourceUrl The URL where it was found
     * @return An absolute URL to the href.
     */
    private static String formatUrlStr(String hrefStr, URL sourceUrl) {
        String baseUrl = sourceUrl.getProtocol() + "://" + sourceUrl.getHost();
        String result = hrefStr;
        if (isExternalLink(hrefStr)) {
            result = (hrefStr.startsWith("//") 
                    ? sourceUrl.getProtocol() + ":" : "") + hrefStr;
        } else {
            if(hrefStr.contains(":")) {
                // If the href has :, it has a protocol that we 
                // don't care about. (ie. mailto:, tel:, etc)
                return null;
            }
            String path = sourceUrl.getPath();
            if (hrefStr.startsWith("/")) {
                result = baseUrl + hrefStr;
            } else {
                result = baseUrl + (path.isEmpty() ? "" : 
                    path.substring(0, path.lastIndexOf("/"))) + "/" + hrefStr;
            }
        }
        
        return result;
    }
    
    private static boolean isExternalLink(String urlStr) {
        return urlStr.startsWith("//") || urlStr.startsWith("http://") ||
                urlStr.startsWith("https://");
    }
}
