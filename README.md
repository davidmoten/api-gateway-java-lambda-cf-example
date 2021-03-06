# api-gateway-java-lambda-cf-example
<a href="https://travis-ci.org/davidmoten/api-gateway-java-lambda-cf-example"><img src="https://travis-ci.org/davidmoten/api-gateway-java-lambda-cf-example.svg"/></a><br/>

Example of integration of api gateway and java lambda using cloud-formation. The REST API deployed is defined using OpenAPI 3.0 and returns JSON from the single `do` method.

## Update July 2021**
This project has been updated to use 
* OpenAPI 3.0 with 200, 302, 400, 500 responses
* properly mapped errors
* an S3 redirect example
* a binary response example (see the '/image` path in [cloudformation.yaml](src/main/aws/cloudformation.yaml])
* full json api

## About this project
This project is all about the [cloudformation.yaml](https://github.com/davidmoten/api-gateway-java-lambda-cf-example/blob/master/src/main/aws/cloudformation.yaml) script and the lambda [handler](https://github.com/davidmoten/api-gateway-java-lambda-cf-example/blob/master/src/main/java/com/github/davidmoten/apig/example/Handler.java). Have a look at those files which have been generously commented to understand more of how the api gateway to java lambda integration happens.

## How to install
You can deploy this application to your own AWS account by following the steps below. It's not a bad idea just to check that stuff is working. Then you can copy the project, refactor package names, artifact and groupId names and customize to your liking.

### Prerequisites
* Maven installed
* an AWS account with key and secret key access pair that has privileges to create resources
* a `<server>` entry in `~/.m2/settings.xml` with your aws key and secret key in the username and password fields (you can encrypt the password field using `mvn -ep`).

### Instructions
```bash
./deploy.sh -Dserver.id=<SERVERID> -Dapplication=<YOUR_APP_NAME>
```

If you are behind a proxy you can set `-Dproxy.host=<PROXY> -Dproxy.port=<PORT>` in the above command also.

The call to `deploy.sh` will do the following:

* build the project jar artifact
* create a bucket for artifacts (for lambda) for the application if doesn't exist (a unique name built from `application` and your AWS account ID will be used)
* deploy the built maven artifact versioned and timestamped to the artifact bucket
* create a CloudFormation stack comprising
  * lambda 
  * lambda execution role
  * api gateway
  * api gateway stage
  * single api user authenticated by `x-api-key` header
  * api usage plan for the single api user
  * one GET path that maps a String response from the lambda handler to `text/plain` content
  * permission for the api gateway to call the lambda (including the Test interface in the AWS Console)
* deploy the stage (not performed automatically by a CloudFormation update)

### How to call the api

Suppose your application name was `myapp`.

In the AWS Console go to **API Gateway** - **myapp** - **Stages** - **api** - **Invoke URL** and copy the url displayed there.

Also in the AWS Console go to **API Gateway** - **API Keys** - **myapp-user** - **API Key** - **Show** and copy it also.

```bash
URL= ...
X_API_KEY= ...
curl -H "x-api-key: $X_API_KEY" "$URL/api/do?name=fred"
```
prints out (JSON)
```
"Hello fred"
```

You can also test the api in the AWS Console at **API Gateway** - **myapp** - **Resources** - **/do** - **GET** - **Test**. Enter a value for name and click the Test button and you will see the response in the right of the frame. 

## How to delete the api
To remove the whole stack go to the AWS Console at **CloudFormation** - **Stacks** - **myapp** - **Actions** - **Delete Stack**.

There is one more item you can remove that is created by `deploy.sh` and that is the artifact bucket in S3 which is called *app-<ACCOUNT_ID>-artifacts*.

## Hints for developing
* a big hint to speed your development is work on the cloudformation stuff first (paths, parameters, integrations) then do the work in the java handler. The reason for this is that as you add dependencies for the handler (java libraries) the jar deployed to lambda will grow and your test cycle will become longer because of the delays in uploading the jar to AWS.
