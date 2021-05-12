package com.github.davidmoten.apig.example;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;

public class Handler implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        try {
            // expects full request body passthrough from api gateway integration request
            StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);
            String name = request.queryStringParameter("name")
                    .orElseThrow(() -> new IllegalArgumentException("parameter not found: 'name'"));
            return "{\"response\": \"Hello " + name + "\"}";
        } catch (IllegalArgumentException e) {
            // the error message prefix is used in cloudformation.yaml to map to a 400 status code
            throw new RuntimeException("BadRequest: " + e.getMessage(), e);
        } catch (Throwable e) {
            // the error message prefix is used in cloudformation.yaml to map to a 500 status code
            throw new RuntimeException("UnexpectedException: " + e.getMessage(), e);
        }
    }
}
