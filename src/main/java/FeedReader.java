import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * It Reads and prints any RSS/Atom feed type.
 * <p>
 *
 * @author Alejandro Abdelnur
 */
public class FeedReader {
    List<String> matchList = Arrays.asList("Key", "URL", "Project", "Issue Type",
            "Components", "Affects Versions", "Reporter",
            "Assignee", "Priority", "Fix For");

    public void feedFetcher(String url) {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            HttpUriRequest request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request);
                 InputStream stream = response.getEntity().getContent()) {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(stream));
                int count = 1;
                for (SyndEntry entry : feed.getEntries()) {
                    //System.out.println(count + " : " + entry.getTitle());
                    StringBuffer parsedContent = parseContents(entry.getContents());
                    System.out.println(parsedContent);
                    count++;
                }
            }
        } catch (FeedException fe) {
            System.err.println(fe.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void readFeed(String url) {
        boolean ok = false;
        try {
            URL feedUrl = new URL(url);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            int count = 1;
            for (SyndEntry entry : feed.getEntries()) {
                System.out.println(count + " : " + entry.getDescription());
                for (SyndContent content : entry.getContents()) {
                    // System.out.println(content.getValue());
                }
                count++;
            }

            ok = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR: " + ex.getMessage());
        }

        if (!ok) {
            System.out.println();
            System.out.println("FeedReader reads and prints any RSS/Atom feed type.");
            System.out.println("The first parameter must be the URL of the feed to read.");
            System.out.println();
        }
    }

    public StringBuffer parseContents(List<SyndContent> contents) {
        StringBuffer buffer = new StringBuffer();
        for (SyndContent content : contents) {
            String contentStr = content.getValue();
            String rest = matchOne(contentStr);
            String out = getValueAfterColon(rest);
            if (out.length() > 0) {
                buffer.append("[");
                buffer.append(out);
                buffer.append("]");
                buffer.append(", ");
            }
        }
        return buffer;
    }

    public String matchOne(String input) {
        for (String match : matchList) {
            if (input.contains(match)) {
                int index = input.indexOf(match);
                String rest = input.substring(index, input.length());
                return rest;
            }
        }
        return "";
    }

    public String getValueAfterColon(String line) {
        String[] splits = line.split(":");
        return splits[splits.length - 1];
    }

    public static void main(String[] args) {
        String url = "https://mail-archives.apache.org/mod_mbox/spark-issues/" +
                "?format=atom";
        FeedReader reader = new FeedReader();
        reader.feedFetcher(url);
    }
}