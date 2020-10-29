package tenant.watchdog;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import tenant.watchdog.models.Event;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.lang.IllegalStateException;
import java.nio.charset.StandardCharsets;

/**
 * Handler for requests to Lambda function.
 */
public class WatchdogHandler implements RequestStreamHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String topicArn = System.getenv("SNS_TOPIC");
    private static final String searchString = System.getenv("SEARCH_STRING");
    private static final SnsClient snsClient = SnsClient.create();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII))) {
            Event event = gson.fromJson(reader, Event.class);
            String eventName = event.getDetail().getEventName();
            if(eventName.equals("AssumeRole")) {
                String policy = event.getDetail().getRequestParameters().getPolicy();
                String roleArn = event.getDetail().getRequestParameters().getRoleArn();

                logger.log("RoleArn: " + roleArn);
                logger.log("Policy: " + policy);
                if(policy == null || !policy.contains(searchString)) {
                    // Publish a message to an Amazon SNS topic.
                    final String msg = "A call to AssumeRoll was made without an inline policy.";
                    PublishRequest publishRequest = PublishRequest.builder()
                        .message(msg)
                        .topicArn(topicArn)
                        .build();
                    snsClient.publish(publishRequest);
                }
            }
        } catch (IllegalStateException | JsonSyntaxException exception) {
            logger.log(exception.toString());
        }
    }
}
