package com.github.davidmoten.apig.example;

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
            
            // get the name query parameter
            String name = request.queryStringParameter("name")
                    .orElseThrow(() -> new IllegalArgumentException("parameter 'name' not found"));

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

                // must throw an exception to from java lambda to get 302
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
        catch (IllegalArgumentException e) {
            // Any exception that represents a bad request should be wrapped in a
            // BadRequestException and rethrown.

            // cloudformation.yaml defines pattern to match the errorMessage field to a 400
            // status code. Wrapping with BadRequestException will prefix the errorMessage
            // with 'BadRequest' which is matched in cloudformation pattern.
            throw new BadRequestException(e);
        } catch (RedirectException e) {
            // rethrow
            throw e;
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
