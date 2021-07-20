package com.github.davidmoten.apig.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.davidmoten.aws.helper.BadRequestException;
import com.github.davidmoten.aws.helper.RedirectException;

public class Handler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        // be sure to wrap all server side code in a try-catch and catch ALL throwables
        try {

            // binary response with customized content-type
            // note that using a LAMBDA_PROXY integration the request variables are
            // different so we look at
            // "path"
            if ("/wms".equals(input.get("path"))) {
                return createWmsJsonResponse();
            } else {
                throw new BadRequestException("unknown path");
            }
        }
        // lambda infrastructure will ensure that any thrown exception gets returned as
        // a json payload {errorMessage, errorType, stackTrace, cause}
        catch (BadRequestException | IllegalArgumentException e) {
            return new APIGatewayProxyResponseEvent() //
                    .withStatusCode(400) //
                    .withBody(e.getMessage()) //
                    .withHeaders(header("Content-Type", "application/json").build());
        } catch (RedirectException e) {
            return new APIGatewayProxyResponseEvent() //
                    .withStatusCode(302) //
                    .withHeaders(header("Location", e.getMessage()).build());
        } catch (Throwable e) {
            return new APIGatewayProxyResponseEvent() //
                    .withStatusCode(500) //
                    .withBody(e.getMessage()) //
                    .withHeaders(header("Content-Type", "application/json").build());
        }
    }

    private static Builder header(String name, String value) {
        return new Builder().header(name, value);
    }

    static final class Builder {
        private Map<String, String> headers = new HashMap<>();

        Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        Map<String, String> build() {
            return headers;
        }
    }

    private static APIGatewayProxyResponseEvent createWmsJsonResponse() throws IOException {
        try (InputStream in = Handler.class.getResourceAsStream("/tiny.png")) {
            byte[] b = readBytes(in);
            String b64 = Base64.getEncoder().encodeToString(b);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "image/png");
            return new APIGatewayProxyResponseEvent().withStatusCode(200)//
                    .withHeaders(headers) //
                    .withIsBase64Encoded(true) //
                    .withBody(b64);
        }
    }

    private static byte[] readBytes(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        int n;
        while ((n = in.read(buffer)) != -1) {
            b.write(buffer, 0, n);
        }
        return b.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(createWmsJsonResponse());
    }
}
