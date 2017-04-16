package feed.jira.parser;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by venkat on 4/8/17.
 */
public interface JiraParser {
    public List<? extends Jira> digest();

    public List<? extends Jira> digestWithStatusFilter();

    public List<SparkJira> digestWithTimeFilter(DateTime from);
}
