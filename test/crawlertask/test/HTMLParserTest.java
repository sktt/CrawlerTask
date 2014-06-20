package crawlertask.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import crawlertask.main.HTMLDocument;
import crawlertask.main.HTMLParser;

public class HTMLParserTest {

    private final static String TEST_BASE = "http://www.visual-meta.com";
    private final static String TEST_PATH = "foo";
    private final static String TEST_DOC = "bar.html";
    
    private static URL testUrl;
    
    private final static String TEST_HTML = ""
            + "<a href=\"local-link.html\">something</a> another document"
            + "<a stuff href=\"/something#else\"></a>"
            + "<a href=\"/local-link\"></a> hello and so on "
            + "<a href=\"//www.google.com\"></a> hello and so on "
            + "<a href=\"#ignore\"></a>"
            + "<a href=\"/\"></a>";
    
    private final static Set<String> expected = new TreeSet<String>();
    
    @BeforeClass
    public static void setUp() {
        try {
            testUrl = new URL(TEST_BASE + "/" + TEST_PATH + "/" + TEST_DOC);
        } catch (MalformedURLException e) {
            fail();
        }
        
        expected.add(TEST_BASE + "/" + TEST_PATH + "/local-link.html");
        expected.add(TEST_BASE + "/something#else");
        expected.add(TEST_BASE + "/local-link");
        expected.add("http://www.google.com");
    }
    
    @Test
    public void testGetHrefs() throws MalformedURLException {
        Set<String> actual = HTMLParser.getHrefs(new HTMLDocument(testUrl, TEST_HTML));
        assertEquals("Should be of same length", expected.size(), actual.size());
        for (String expString : expected) {
            assertTrue("Expected \"" + expString + "\" to be contained in the result", 
                    actual.contains(expString));
        }
    }
    
}
