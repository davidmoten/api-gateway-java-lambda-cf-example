# api-gateway-java-lambda-cf-example
Example of integration of api gateway and java lambda using cloud-formation

## How to install

### Prerequisites
* an AWS account with key and secret key access pair that has privileges to create resources
* a `<server>` entry in `~/.m2/settings.xml` with your aws key and secret key in the username and password fields (you can encrypt these using `mvn -ep`).

### Instructions
```bash
./deploy.sh <MODE> -Dserver.id=<SERVERID>
```
The call to `deploy.sh` will do the following:

* build the project jar artifact
* create a bucket for artifacts (for lambda) for the application if doesn't exist
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

In the AWS Console go to **API Gateway - api-gateway-java-lambda-cf-example-&lt;MODE&gt; - Stages - <MODE>** and copy the url displayed there.

Also in the AWS Console go to **API Gateway - API Keys - api-gateway-java-lambda-cf-user-<MODE> - API Key - Show** and copy it also.

```bash
URL= ...
X_API_KEY= ...
curl -H "x-api-key: $X_API_KEY" "$URL/&lt;MODE&gt;/do?name=fred"
```
prints out 
```
Hello fred
```



