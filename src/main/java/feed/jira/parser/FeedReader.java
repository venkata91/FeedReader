package feed.jira.parser;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class FeedReader {
    private CloseableHttpClient client;

    public FeedReader() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        HttpClientBuilder clientBuilder = HttpClients.custom().setSSLSocketFactory(sslsf);
        this.client = clientBuilder.build();
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

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException, FeedException {
        FeedReader feedReader = new FeedReader();
        System.out.println(feedReader.feedFetcher
                ("https://mail-archives.apache.org/mod_mbox/spark-issues/?format=atom").size());

    }
}
