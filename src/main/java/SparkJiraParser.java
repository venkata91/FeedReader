import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;

import java.io.IOException;
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
                digest.add(jira);
            }
        } catch (FeedException fe) {
            System.err.println(fe.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return digest;
    }

    public static void main(String[] args) throws FeedException, IOException {
        String url = "https://mail-archives.apache.org/mod_mbox/spark-issues/" +
                "?format=atom";
        List<String> matchList = Arrays.asList("Key:", "URL:", "Project:", "Issue Type:",
                "Components:", "Affects Versions:", "Reporter:",
                "Assignee:", "Priority:", "Fix For:");
        Set<String> jiraStatus = new HashSet<>(Arrays.asList("Created", "Resolved", "Commented"));
        JiraParser sparkJiraParser = new SparkJiraParser(url, matchList, jiraStatus);

        int count = 1;
        for (Jira jira : sparkJiraParser.digest()) {
            if (jiraStatus.contains(jira.getStatus())) {
                System.out.println(count + " : " + jira);
            }
            SparkJira sparkJira = (SparkJira) jira;
            // System.out.println(sparkJira.getRawContent());
            count++;
        }
    }
}