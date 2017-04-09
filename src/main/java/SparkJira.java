import java.util.List;

/**
 * Created by venkat on 4/8/17.
 */
public class SparkJira extends Jira {
    private String key;
    private String issueType;
    private List<String> components;
    private String reporter;
    private String assignee;
    private List<String> affectedVersions;
    private List<String> fixedVersions;
    private String rawContent;

    public SparkJira() {
        super();
    }

    public SparkJira(String url, String project, String status, String title) {
        super(url, project, status, title);
    }

    public String getKey() {
        return key;
    }

    public SparkJira setKey(String key) {
        this.key = key;
        return this;
    }

    public String getIssueType() {
        return issueType;
    }

    public SparkJira setIssueType(String issueType) {
        this.issueType = issueType;
        return this;
    }

    public List<String> getComponents() {
        return components;
    }

    public SparkJira setComponents(List<String> components) {
        this.components = components;
        return this;
    }

    public String getReporter() {
        return reporter;
    }

    public SparkJira setReporter(String reporter) {
        this.reporter = reporter;
        return this;
    }

    public String getAssignee() {
        return assignee;
    }

    public SparkJira setAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public List<String> getAffectedVersions() {
        return affectedVersions;
    }

    public SparkJira setAffectedVersions(List<String> affectedVersions) {
        this.affectedVersions = affectedVersions;
        return this;
    }

    public List<String> getFixedVersions() {
        return fixedVersions;
    }

    public SparkJira setFixedVersions(List<String> fixedVersions) {
        this.fixedVersions = fixedVersions;
        return this;
    }

    public String getRawContent() {
        return rawContent;
    }

    public SparkJira setRawContent(String rawContent) {
        this.rawContent = rawContent;
        return this;
    }

    public String toString() {
        return getKey() + " - "
                + getStatus() + " - "
                + getTitle() + " - "
                + getUrl() + " - "
                + getIssueType() + " - "
                + getAssignee() + " - "
                + getReporter() + " - "
                + getComponents() + " - "
                + getAffectedVersions();

    }
}
