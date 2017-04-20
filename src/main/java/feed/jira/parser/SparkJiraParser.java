package feed.jira.parser;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import feed.jira.email.SSLEmailClient;
import feed.jira.feed.jira.html.JiraHTMLFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by venkat on 4/8/17.
 */
public class SparkJiraParser extends FeedReader implements JiraParser {
    private String feedUrl;
    private List<String> contentMatchList;
    private Set<String> jiraStatuses;

    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern squareBrackets = Pattern.compile("\\[([^)]+)\\]");

    private static final Pattern parantheses = Pattern.compile("\\(([^)]+)\\)");

    private static final String UNKNOWN = "UNKNOWN";

    private static final DateTimeFormatter format =
            DateTimeFormat.forPattern("d MMM, yyyy");

    public static DateTimeFormatter getFormat() {
        return format;
    }

    public SparkJiraParser(String url, List<String> contentMatchList,
                           Set<String> jiraStatuses)
            throws IOException, FeedException {
        super();
        this.feedUrl = url;
        this.contentMatchList = contentMatchList;
        this.jiraStatuses = jiraStatuses;
    }


    private String parseStatus(String title) {
        Matcher matcher = squareBrackets.matcher(title);
        matcher.find();
        return matcher.group(1).replace("jira] [", "");
    }

    private SparkJira parseJiraTitle(String title, SparkJira jira) {
        String jiraNum = jira.getKey().split("-")[1];
        int index = title.lastIndexOf(jiraNum);
        jira.setTitle(title.substring(index + jiraNum.length() + 2));
        return jira;
    }

    private SparkJira parseJiraContent(List<SyndContent> contents, SparkJira jira) {
        for (SyndContent content : contents) {
            String contentStr = content.getValue();
            jira.setRawContent(contentStr);
            String[] lines = contentStr.split("\n");
            for (String line : lines) {
                for (String match : contentMatchList) {
                    if (line.contains(match)) {
                        int index = line.indexOf(match);
                        String rest = line.substring(index, line.length());
                        rest = rest.replace("\n", "").replace("\r", "");

                        String[] keyValue = rest.split(":");
                        switch (match) {
                            case "Key:":
                                jira.setKey(keyValue[1]);
                                break;
                            case "URL:":
                                Matcher matcher = urlPattern.matcher(rest);
                                matcher.find();
                                jira.setUrl(rest.substring(matcher.start(0), matcher.end(0)));
                                break;
                            case "Issue Type:":
                                jira.setIssueType(keyValue[1]);
                                break;
                            case "Components:":
                                jira.setComponents
                                        (Arrays.asList(keyValue[1].split(",")));
                                break;
                            case "Affects Versions:":
                                jira.setAffectedVersions
                                        (Arrays.asList(keyValue[1].split(",")));
                                break;
                            case "Reporter:":
                                jira.setReporter(keyValue[1]);
                                break;
                            case "Assignee:":
                                jira.setAssignee(keyValue[1]);
                                break;
                        }
                    }
                }
            }
        }
        return jira;
    }

    public List<SparkJira> digest() {
        List<SparkJira> digest = new ArrayList<SparkJira>();
        List<SyndEntry> feedEntries;
        try {
            feedEntries = feedFetcher(feedUrl);
            SparkJira jira = null;
            for (SyndEntry entry : feedEntries) {
                String status = parseStatus(entry.getTitle());
                jira = new SparkJira();
                jira = parseJiraContent(entry.getContents(), jira);
                jira.setStatus(status);
                jira = parseJiraTitle(entry.getTitle(), jira);
                jira.setUpdatedAt(new DateTime(entry.getUpdatedDate()));
                digest.add(jira);
            }
        } catch (FeedException fe) {
            System.err.println(fe.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return digest;
    }

    public List<SparkJira> digestWithStatusFilter() {
        List<SparkJira> filteredDigest = new ArrayList<>();

        System.out.println(jiraStatuses);

        for (SparkJira jira : this.digest()) {
            if (jiraStatuses.contains(jira.getStatus()) || jiraStatuses.isEmpty()) {
                filteredDigest.add(jira);
            }
        }
        return filteredDigest;
    }

    public List<SparkJira> digestWithTimeFilter(DateTime from) {
        List<SparkJira> filteredDigest = this.digestWithStatusFilter();
        List<SparkJira> jiraDigestFromTime = new ArrayList<>();

        for (SparkJira jira : filteredDigest) {
            if (jira.getUpdatedAt().compareTo(from) > 0) {
                jiraDigestFromTime.add(jira);
            }
        }
        return jiraDigestFromTime;
    }

    public static String sendJiraDigest(String user, String password,
                                        String from, String to)
            throws IOException, FeedException {
        String result = "";
        String url = "https://mail-archives.apache.org/mod_mbox/spark-issues/?format=atom";
        List<String> matchList = Arrays.asList("Key:", "URL:", "Project:", "Issue Type:",
                "Components:", "Affects Versions:", "Reporter:",
                "Assignee:", "Priority:", "Fix For:");
        Set<String> jiraStatus = new HashSet<>(Arrays.asList("Created", "Resolved"));
        JiraParser sparkJiraParser = new SparkJiraParser(url, matchList, jiraStatus);

        StringBuffer jiraDigest = new StringBuffer();
        SSLEmailClient emailClient = new SSLEmailClient(user, password);

        List<? extends Jira> filteredDigest;
        Date today = new Date();
        DateTime previous = new DateTime(today).minusDays(1);
        String todayStr = new DateTime(today).toString(format);

        try {
            filteredDigest = sparkJiraParser.digestWithTimeFilter(previous);
            if (filteredDigest.size() <= 0) {
                return "No new Spark JIRA digest for today";
            }
            // Sort by updated time
            Collections.sort(filteredDigest);
            // Sort by jira status
            Collections.sort(filteredDigest, new Comparator<Jira>() {
                public int compare(Jira o1, Jira o2) {
                    return o1.getStatus().compareTo(o2.getStatus());
                }
            });

            String htmlFormattedDigest = JiraHTMLFormatter.jiraDigestHTML(filteredDigest);
            emailClient.sendEmail(from, to, "Spark OS JIRA daily digest - " + todayStr, htmlFormattedDigest);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result = from + " sent Spark JIRA digest to " + to;
        return result;
    }

    public static void main(String[] args) throws FeedException, IOException {
        String url = "https://mail-archives.apache.org/mod_mbox/spark-issues/?format=atom";
        List<String> matchList = Arrays.asList("Key:", "URL:", "Project:", "Issue Type:",
                "Components:", "Affects Versions:", "Reporter:",
                "Assignee:", "Priority:", "Fix For:");
        Set<String> jiraStatus = new HashSet<>();
        JiraParser sparkJiraParser = new SparkJiraParser(url, matchList, jiraStatus);

        StringBuffer jiraDigest = new StringBuffer();

        if (args.length < 4) {
            System.err.println("Not enough arguments passed");
            System.err.println("<Email> <password> <from> <to>");
        }
        String user = args[0];
        String password = args[1];
        String from = args[2];
        String to = args[3];

        SSLEmailClient emailClient = new SSLEmailClient(user, password);

        List<? extends Jira> filteredDigest;
        Date today = new Date();
        DateTime previous = new DateTime(today).minusDays(1);
        String todayStr = new DateTime(today).toString(format);
        System.out.println("Today " + todayStr);
        try {
            filteredDigest = sparkJiraParser.digestWithTimeFilter(previous);
            if (filteredDigest.size() <= 0) {
                System.out.println("No new Spark JIRA digest for today");
                return;
            }
            // Sort by updated time
            Collections.sort(filteredDigest);
            // Sort by jira status
            Collections.sort(filteredDigest, new Comparator<Jira>() {
                public int compare(Jira o1, Jira o2) {
                    return o1.getStatus().compareTo(o2.getStatus());
                }
            });

            String htmlFormattedDigest = JiraHTMLFormatter.jiraDigestHTML(filteredDigest);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/venkat/jiradigest.html")));
            bw.write(htmlFormattedDigest);
            bw.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}