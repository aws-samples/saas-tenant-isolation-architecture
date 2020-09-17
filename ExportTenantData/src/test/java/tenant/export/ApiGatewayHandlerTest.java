package tenant.export;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import tenant.export.ApiGatewayHandler;

import java.util.HashMap;
import java.util.Map;

public class ApiGatewayHandlerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void successfulResponse() throws JsonProcessingException {
//        ApiGatewayHandler app = new ApiGatewayHandler();
//        APIGatewayProxyRequestEvent ev = new APIGatewayProxyRequestEvent();
//        ev.setBody("{\"name\":\"unitTest\"}");
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzdm96emEiLCJpYXQiOjE2MDAxNjI2NTMsImV4cCI6MTYzMTY5ODY1MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsImN1c3RvbTp0ZW5hbnRfaWQiOiIxMjM0NTY3ODkifQ.AH2QEdB0TgvRgAkbo8aOXSgyVT62NTPgql7r4Yf2Xek");
//        ev.setHeaders(headers);
//        APIGatewayProxyResponseEvent result = app.handleRequest(ev, null);
//        assertEquals(result.getStatusCode().intValue(), 201);
//        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    }

//  @Test
//  public void successfulResponse() {
//    ApiGatewayHandler app = new ApiGatewayHandler();
//    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
//    assertEquals(result.getStatusCode().intValue(), 200);
//    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
//    String content = result.getBody();
//    assertNotNull(content);
//    assertTrue(content.contains("\"message\""));
//    assertTrue(content.contains("\"hello world\""));
//    assertTrue(content.contains("\"location\""));
//  }
}
