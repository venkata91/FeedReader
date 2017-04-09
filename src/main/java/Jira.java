/**
 * Created by venkat on 4/8/17.
 */
public class Jira {
    private String url;
    private String project;
    private String status;
    private String title;

    public Jira() {

    }

    public Jira(String url, String project, String status, String title) {
        this.url = url;
        this.project = project;
        this.status = status;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
