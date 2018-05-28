# couchbase-session-replication

This project is actively under development. Use at your own risk.

# Usage
```bash
mvn package
docker-compose up
```
Should create a couchbase cluster runnning tomcat and the sample web application.

Then you can run the integration tests (*Spec)

## Using session spitter to write integration tests

```java
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{\n\t\"field\":\"message\",\n\t\"value\":\"wow lolix\"\n}");
Request request = new Request.Builder()
  .url("http://localhost:8080/sessionspitterservlet/SessionSpitter")
  .post(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
```