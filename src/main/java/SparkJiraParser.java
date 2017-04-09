import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by venkat on 4/8/17.
 */
public class SparkJiraParser extends FeedReader implements JiraParser {
    private String feedUrl;
    private List<String> contentMatchList;
    private List<String> titleFilterList;

    public SparkJiraParser(String url, List<String> contentMatchList,
                           List<String> titleFilterList)
            throws IOException, FeedException {
        super();
        this.feedUrl = url;
        this.contentMatchList = contentMatchList;
        this.titleFilterList = titleFilterList;

    }


    private boolean filterTitle(String title, List<String> filterWords) {
        for (String word : filterWords) {
            if (title.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private SparkJira parseJiraTitle(String title, SparkJira jira) {
        int index = title.indexOf(jira.getKey());
        jira.setTitle(title.substring(index + 1));
        return jira;
    }

    private SparkJira parseJiraContent(List<SyndContent> contents, SparkJira jira) {
        for (SyndContent content : contents) {
            String contentStr = content.getValue();
            String[] lines = contentStr.split("\n");
            for (String line : lines) {
                for (String match : contentMatchList) {
                    if (line.contains(match)) {
                        int index = line.indexOf(match);
                        String rest = line.substring(index, line.length());
                        rest = rest.replace("\n", "").replace("\r", "");

                        String[] keyValue = rest.split(":");
                        switch(match) {
                            case "Key":
                                jira.setKey(keyValue[1]);
                                break;
                            case "URL":
                                jira.setUrl(keyValue[1]);
                                break;
                            case "Issue Type":
                                jira.setIssueType(keyValue[1]);
                                break;
                            case "Components":
                                jira.setComponents
                                        (Arrays.asList(keyValue[1].split(",")));
                                break;
                            case "Affects Versions":
                                jira.setAffectedVersions
                                        (Arrays.asList(keyValue[1].split(",")));
                                break;
                            case "Reporter":
                                jira.setReporter(keyValue[1]);
                                break;
                            case "Assignee":
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
            int count = 1;
            for (SyndEntry entry : feedEntries) {
                if (filterTitle(entry.getTitle(), titleFilterList)) {
                    jira = new SparkJira();
                    System.out.println(count + " : " + entry.getTitle());
                    jira = parseJiraContent(entry.getContents(), jira);
                    jira = parseJiraTitle(entry.getTitle(), jira);
                    digest.add(jira);
                    count++;
                }

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
        List<String> matchList = Arrays.asList("Key", "URL", "Project", "Issue Type",
                "Components", "Affects Versions", "Reporter",
                "Assignee", "Priority", "Fix For");
        List<String> titleList = Arrays.asList("Created", "Resolved");
        JiraParser sparkJiraParser = new SparkJiraParser(url, matchList, titleList);

        for (Jira jira : sparkJiraParser.digest()) {
            System.out.println(jira);
        }
    }
}