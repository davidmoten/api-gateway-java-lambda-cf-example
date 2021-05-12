package com.github.davidmoten.apig.example;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;

public class Handler implements RequestHandler<Map<String,Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        // expects full request body passthrough from api gateway integration
        // request
        StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);

        String name = request.queryStringParameter("name")
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: 'name'"));

        return "Hello " + name;
    }
}
