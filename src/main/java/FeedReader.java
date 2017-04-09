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
import java.util.List;

public class FeedReader {
    private CloseableHttpClient client;

    public FeedReader() throws IOException, FeedException {
        this.client = HttpClients.createMinimal();
    }

    public List<SyndEntry> feedFetcher(String feedUrl) throws FeedException, IOException {
        HttpUriRequest request = new HttpGet(feedUrl);
        try (CloseableHttpResponse response = client.execute(request);
             InputStream stream = response.getEntity().getContent()) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(stream));
            return feed.getEntries();
        }
    }
}
