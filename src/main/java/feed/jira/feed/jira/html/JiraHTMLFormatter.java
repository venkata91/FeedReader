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
        String html = "<html>";
        String endHtml = "</html>";
        String head = "<head>";
        String endHead = "</head>";

        String body = "<body>";
        String endBody = "</body>";
        String table = "<table cellpadding=\"0\" border=\"0\" " +
                "cellspacing=\"10\" style=\"border-collapse:collapse\">";
        String tableE = "</table>";

        String tbody = "<tbody>";
        String tbodyE = "</tbody>";

        String td1 = "<td style=\"text-decoration:none;display:block;font-size:14px;" +
                "letter-spacing:-0.5px;font-weight:bold;color:#333;line-height:1.4\">";
        String tdE = "</td>";

        String td2 = "<td style=\"text-decoration:none;display:block;font-size:13px;" +
                "letter-spacing:-0.5px;color:#333;line-height:1.4\">";

        String tr = "<tr>";
        String trE = "</tr>";

        String ol = "<ol>";
        String olE = "</ol>";
        String li = "<li>";
        String endLi = "</li>";

        String div = "<div>";
        String divE = "</div>";

        String space5 = "     ";

        // htmlDigest.append(html);
        // htmlDigest.append(head);
        // htmlDigest.append(endHead);

        // htmlDigest.append(body);

        //htmlDigest.append(ol);

        for(Jira jira : jiraDigest) {
            SparkJira sparkJira = (SparkJira) jira;
            // htmlDigest.append(li);
            htmlDigest.append(table);
            htmlDigest.append(tbody);
            htmlDigest.append(div);
            htmlDigest.append(tr);

            // td + sparkJira.getUpdatedAt().toString(SparkJiraParser.getFormat()) + tdE
            htmlDigest.append(td1 + "<a href= " + sparkJira.getUrl() + ">"
                    + sparkJira.getKey() + "</a>" + " \t"
                    + sparkJira.getTitle() + tdE);

            htmlDigest.append(trE);
            htmlDigest.append(divE);
            htmlDigest.append(tbodyE);
            htmlDigest.append(tableE);

            htmlDigest.append(table);
            htmlDigest.append(tbody);
            htmlDigest.append(div);
            htmlDigest.append(tr);

            htmlDigest.append(td2 + sparkJira.getStatus() + space5
                    + sparkJira.getReporter() + space5
                    + sparkJira.getAssignee() + space5
                    + sparkJira.getAffectedVersions() + space5
                    + sparkJira.getFixedVersions() + space5
                    + sparkJira.getComponents() + tdE);

            htmlDigest.append(trE);
            htmlDigest.append(divE);
            htmlDigest.append(tbodyE);
            htmlDigest.append(tableE);
            // htmlDigest.append(endLi);
            htmlDigest.append("\n");
        }


        // htmlDigest.append(olE);
        // htmlDigest.append(endBody);

        // htmlDigest.append(endHtml);
        return htmlDigest.toString();
    }
}
