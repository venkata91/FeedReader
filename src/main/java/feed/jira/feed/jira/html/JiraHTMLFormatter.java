package feed.jira.feed.jira.html;

import com.sun.istack.internal.NotNull;
import feed.jira.parser.Jira;
import feed.jira.parser.SparkJira;
import feed.jira.parser.SparkJiraParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.List;
import java.util.StringTokenizer;

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

        String table = "<table  style=\"font-family: &quot;Calibri&quot;, &quot;Verdana&quot;, &quot;Helvetica&quot;;font-size: 12px;background: #fff;width:750px;border-collapse: collapse;text-align: left;margin: 20px\">";
        String tableE = "</table>";

        String tbody = "<tbody>";
        String tbodyE = "</tbody>";

        String tr = "<tr>";
        String trE = "</tr>";

        String th = "<th  style=\"font-size: 14px;font-weight: normal;color: #039;border-bottom: 2px solid #6678b1;padding: 10px 8px\">";
        String thE = "</th>";

        String td = "<td style=\"border-bottom: 1px solid #ccc;color: #669;padding: 6px 8px\">";
        String tdE = "</td>";

        String div = "<div>";
        String divE = "</div>";

        String style = "<style>";
        String styleE = "</style>";

        String span = "<span>";
        String spanE = "</span>";

        String tableStyle = "table {\n" +
                "        font-family: \"Calibri\", \"Verdana\", \"Helvetica\";\n" +
                "        font-size: 12px;\n" +
                "        background: #fff;\n" +
                "        width: 750px;\n" +
                "        border-collapse: collapse;\n" +
                "        text-align: left;\n" +
                "        margin: 20px;\n" +
                "    }\n" +
                "    th {\n" +
                "        font-size: 14px;\n" +
                "        font-weight: normal;\n" +
                "        color: #039;\n" +
                "        border-bottom: 2px solid #6678b1;\n" +
                "        padding: 10px 8px;\n" +
                "    }\n" +
                "    td {\n" +
                "        border-bottom: 1px solid #ccc;\n" +
                "        color: #669;\n" +
                "        padding: 6px 8px;\n" +
                "    }\n" +
                "    tbody tr:hover td {\n" +
                "        color: #009;\n" +
                "    }\n";

        // htmlDigest.append(html);
        // htmlDigest.append(head);
        // htmlDigest.append(style);
        // htmlDigest.append(tableStyle);
        // htmlDigest.append(styleE);
        // htmlDigest.append(endHead);

        htmlDigest.append(body);
        htmlDigest.append("<h2>Spark OS JIRA daily digest </h2>");
        htmlDigest.append(span);
        htmlDigest.append(table);

        htmlDigest.append(tr);
        htmlDigest.append(th + " JIRA " + thE
                    +th + " Title " + thE
                    +th + " Status " +thE
                    +th + " Reporter " + thE
                    +th + " Affects versions " + thE
                    +th + " Components " + thE
                    +th + " Updated At " + thE);
        htmlDigest.append(trE);

        for(Jira jira : jiraDigest) {
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


        // htmlDigest.append(tableE);
        // htmlDigest.append(span);
        // htmlDigest.append(endBody);
        // htmlDigest.append(endHtml);
        return htmlDigest.toString();
    }

    public static String inlineCss(String html) {
        final String style = "style";
        Document doc = Jsoup.parse(html);
        Elements els = doc.select(style);// to get all the style elements
        for (Element e : els) {
            String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim();
            String delims = "{}";
            StringTokenizer st = new StringTokenizer(styleRules, delims);
            while (st.countTokens() > 1) {
                String selector = st.nextToken(), properties = st.nextToken();
                if (!selector.contains(":")) { // skip a:hover rules, etc.
                    Elements selectedElements = doc.select(selector);
                    for (Element selElem : selectedElements) {
                        String oldProperties = selElem.attr(style);
                        selElem.attr(style,
                                oldProperties.length() > 0 ? concatenateProperties(
                                        oldProperties, properties) : properties);
                    }
                }
            }
            e.remove();
        }
        return doc.toString();
    }

    private static String concatenateProperties(String oldProp, @NotNull String newProp) {
        oldProp = oldProp.trim();
        if (!oldProp.endsWith(";"))
            oldProp += ";";
        return oldProp + newProp.replaceAll("\\s{2,}", " ");
    }
}
