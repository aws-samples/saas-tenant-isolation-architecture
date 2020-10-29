package tenant.export;

import java.util.*;

import tenant.vendinglayer.TokenVendor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import tenant.export.errors.GatewayError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import tenant.export.models.TenantProduct;


/**
 * Handler for requests to Lambda function.
 */
public class ApiGatewayHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayHandler.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        switch(input.getHttpMethod().toLowerCase()) {
            case "get":
                return handleGetRequest(input, context);
            case "post":
                return handlePostRequest(input, context);
            default:
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(405);
        }
    };

    public APIGatewayProxyResponseEvent handlePostRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        // we vending the token by extracting the tenant ID from the JWT token contained in
        // the request headers
        TokenVendor tokenVendor = new TokenVendor();
        final AwsCredentialsProvider awsCredentialsProvider =
            tokenVendor.vendTokenJwt(input.getHeaders());

        // we parse the body of the POST request, currently we only accept a 'data' parameter to
        // be written to DynamoDB, anything else will be ignored
        Map<String, String> body;
        try {
            TypeReference<Map<String,String>> typeRef = new TypeReference<Map<String,String>>() {};
            body = mapper.readValue(input.getBody(), typeRef);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON body.", e);
            throw new RuntimeException(createBadRequestResponse(context.getAwsRequestId(),
                "Error parsing JSON body."));
        }

        String tenant = tokenVendor.getTenant();
        logger.info("TENANT ID: " + tenant);

        // TenantProduct class encapsulates writing to DynamoDB using the enhanced DynamoDB
        // client, which allows us to use POJOs
        TenantProduct tentantProduct = new TenantProduct(awsCredentialsProvider, tenant, body.get("data"));
        tentantProduct.save();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return new APIGatewayProxyResponseEvent()
            .withHeaders(headers)
            .withStatusCode(201);
    }

    public APIGatewayProxyResponseEvent handleGetRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        // we vending the token by extracting the tenant ID from the JWT token contained in
        // the request headers
        TokenVendor tokenVendor = new TokenVendor();
        final AwsCredentialsProvider awsCredentialsProvider =
            tokenVendor.vendTokenJwt(input.getHeaders());

        String tenant = tokenVendor.getTenant();
        logger.info("TENANT ID: " + tenant);

        // TenantProduct class encapsulates writing to DynamoDB using the enhanced DynamoDB
        // client, which allows us to use POJOs
        TenantProduct tentantProduct = new TenantProduct(awsCredentialsProvider, tenant);
        tentantProduct = tentantProduct.load(tentantProduct);

        String body;
        try {
            body = mapper.writeValueAsString(tentantProduct);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON body.", e);
            throw new RuntimeException(createBadRequestResponse(context.getAwsRequestId(),
                "Error parsing JSON body."));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return new APIGatewayProxyResponseEvent()
            .withHeaders(headers)
            .withBody(body)
            .withStatusCode(200);
    }

    private String createBadRequestResponse(String requestId, String message) {
        try {
            GatewayError error = new GatewayError("Bad Request",
                "400", requestId, message);
            return mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error encoding JSON response");
        }
    }
}
