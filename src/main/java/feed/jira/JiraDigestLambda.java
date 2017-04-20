package feed.jira;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.rometools.rome.io.FeedException;
import feed.jira.parser.SparkJiraParser;

import java.io.IOException;
import java.util.Map;

/**
 * Created by venkat on 4/19/17.
 */
public class JiraDigestLambda  implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        String user = input.get("email");
        String password = input.get("password");
        String from = input.get("from");
        String to = input.get("to");

        if(user == null || user.length() <= 0) {
            return "email : " + user + " is empty or not valid";
        } else if (password == null || password.length() <= 0) {
            return "password : " + password + " is empty or not valid";
        } else if(from == null || from.length() <= 0) {
            return "from : " + from + " is empty or not valid";
        } else if(to == null || to.length() <= 0) {
            return "to : " + from + " is empty or not valid";
        }

        int numRetries = 3;

        numRetries = input.containsKey("retries") ?
                Integer.parseInt(input.get("retries")) : numRetries;

        int retry = 0;
        while(true) {
            try {
                return SparkJiraParser.sendJiraDigest(user, password, from, to);
            } catch (IOException ioe) {
                return ioe.getMessage();
            } catch (FeedException fe) {
                if (++retry == numRetries) {
                    return fe.getMessage();
                }
            }
        }
    }
}
