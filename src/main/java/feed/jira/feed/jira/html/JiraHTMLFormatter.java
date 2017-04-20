package feed.jira.feed.jira.html;

import feed.jira.parser.Jira;
import feed.jira.parser.SparkJira;
import feed.jira.parser.SparkJiraParser;
import java.text.ParseException;
import java.util.List;

/**
 * Created by venkat on 4/15/17.
 */
public class JiraHTMLFormatter {
    public static String jiraDigestHTML(List<? extends Jira> jiraDigest) throws ParseException {
        StringBuffer htmlDigest = new StringBuffer();

        String body = "<body>";
        String endBody = "</body>";

        String span = "<span>";
        String spanE = "</span>";

        String table = "<table  style=\"font-family: &quot;Calibri&quot;, &quot;Verdana&quot;, &quot;Helvetica&quot;;font-size: 12px;background: #fff;width:750px;border-collapse: collapse;text-align: left;margin: 20px\">";
        String tableE = "</table>";

        String tr = "<tr>";
        String trE = "</tr>";

        String th = "<th  style=\"font-size: 14px;font-weight: normal;color: #039;border-bottom: 2px solid #6678b1;padding: 10px 8px\">";
        String thE = "</th>";

        String td = "<td style=\"border-bottom: 1px solid #ccc;color: #669;padding: 6px 8px\">";
        String tdE = "</td>";
        htmlDigest.append(body);
        htmlDigest.append("<h2>Spark OS JIRA daily digest </h2>");
        htmlDigest.append(span);
        htmlDigest.append(table);

        htmlDigest.append(tr);
        htmlDigest.append(th + " JIRA " + thE
                + th + " Title " + thE
                + th + " Status " + thE
                + th + " Reporter " + thE
                + th + " Affects versions " + thE
                + th + " Components " + thE
                + th + " Updated At " + thE);
        htmlDigest.append(trE);

        for (Jira jira : jiraDigest) {
            htmlDigest.append(tr);
            SparkJira sparkJira = (SparkJira) jira;
            htmlDigest.append(td + "<a href= " + sparkJira.getUrl() + ">" + sparkJira.getKey() + "</a>" + " \t" + tdE
                    + td + sparkJira.getTitle() + tdE
                    + td + sparkJira.getStatus() + tdE
                    + td + sparkJira.getReporter() + tdE
                    + td + sparkJira.getAffectedVersions() + tdE
                    + td + sparkJira.getComponents() + tdE
                    + td + sparkJira.getUpdatedAt().toString(SparkJiraParser.getFormat()) + tdE);

            htmlDigest.append(trE);
        }


        htmlDigest.append(tableE);
        htmlDigest.append(spanE);
        htmlDigest.append(endBody);

        return htmlDigest.toString();
    }
}