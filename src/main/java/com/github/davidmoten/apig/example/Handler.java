package com.github.davidmoten.apig.example;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.davidmoten.aws.helper.BadRequestException;
import com.github.davidmoten.aws.helper.RedirectException;
import com.github.davidmoten.aws.helper.ServerException;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;

public class Handler implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        // be sure to wrap all server side code in a try-catch and catch ALL throwables
        try {
            // expects full request body passthrough from api gateway integration request
            StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);

            // binary response
            if ("/image".equals(request.resourcePath().orElse(""))) {
                byte[] buffer = new byte[8192];
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                try (InputStream in = Handler.class.getResourceAsStream("/tiny.png")) {
                    int n;
                    while ((n = in.read(buffer)) != -1) {
                        b.write(buffer, 0, n);
                    }
                }
                return Base64.getEncoder().encodeToString(b.toByteArray());
            }

            // get the name query parameter
            String name = request.queryStringParameter("name")
                    .orElseThrow(() -> new IllegalArgumentException("parameter 'name' not found"));

            // for a POST, request body is in input.get("body-json")

            // demonstrate two paths
            // 1: s3 redirect
            // 2: return json

            if ("redirect".equals(name)) {
                // return the s3 url of a file, return bucket should have expiry set to say 4
                // hours so we have enough time to debug stuff if required
//                String replyBucketName = "replyBucket";
//                String replyObjectName = "replayObject";
//                
//                // use AWS SDK S3 object to generate presigned url
//                AmazonS3Client s3 = new AmazonS3Client();                
//                String url = s3.generatePresignedUrl( //
//                        replyBucketName, //
//                        replyObjectName, //
//                        new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4))) //
//                        .toString();
                String url = "https://blah";

                // must throw an exception from java lambda to get 302
                // redirection to work! The error message (the url) is mapped by
                // the integration response part of the API Gateway to a 302
                // status code with Location header equal to the url value
                throw new RedirectException(url);
            } else {
                // as we are returning JSON we specify a don't-touch-it response template in the
                // cloudformation script so that it doesn't get serialized again into json
                // (quoted).
                return "{\"response\": \"Hello " + name + "\"}";
            }
        }
        // lambda infrastructure will ensure that any thrown exception gets returned as
        // a json payload {errorMessage, errorType, stackTrace, cause}
        catch (BadRequestException | ServerException | RedirectException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            // Any exception that represents a bad request should be wrapped in a
            // BadRequestException and rethrown.

            // cloudformation.yaml defines pattern to match the errorMessage field to a 400
            // status code. Wrapping with BadRequestException will prefix the errorMessage
            // with 'BadRequest' which is matched in cloudformation pattern.
            throw new BadRequestException(e);
        } catch (Throwable e) {
            // Note that it is advised to catch Throwable because any uncaught errors will
            // get mapped to a 200 response (assuming that their message doesn't by chance
            // pattern match any of the other response codes).

            /// cloudformation.yaml defines pattern to match the errorMessage field to a 500
            // status code. Wrapping with ServerException will prefix the errorMessage with
            // 'ServerException' which is matched in cloudformation pattern.
            throw new ServerException(e);
        }
    }
}
