package com.nordstrom.xrpc.demos.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nordstrom.xrpc.server.Server;
import com.nordstrom.xrpc.testing.UnsafeHttp;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeopleTests {
  private Server server;
  private final OkHttpClient client = UnsafeHttp.unsafeHttp2Client();

  @BeforeEach
  void beforeEach() throws IOException {
    server = new Server(0);
    Application.configure(server);
    server.listenAndServe();
  }

  @AfterEach
  void afterEach() {
    server.shutdown();
  }

  @Test
  void testPostPeople() throws IOException {
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .post(
                        RequestBody.create(
                            MediaType.parse("application/json"), "{\"name\":\"bob\"}"))
                    .url(server.localEndpoint() + "/people")
                    .build())
            .execute();

    assertEquals(200, response.code());
  }

  @Test
  void testGetPeople() throws IOException {
    client
        .newCall(
            new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), "{\"name\":\"bob\"}"))
                .url(server.localEndpoint() + "/people")
                .build())
        .execute();

    Response response =
        client
            .newCall(
                new Request.Builder()
                    .get()
                    .url(server.localEndpoint() + "/people")
                    .header("Accept", "application/json")
                    .build())
            .execute();

    assertEquals("[{\"name\":\"bob\"}]", response.body().string());
  }

  @Test
  void testGetPerson() throws IOException {
    client
        .newCall(
            new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), "{\"name\":\"bob\"}"))
                .url(server.localEndpoint() + "/people")
                .build())
        .execute();

    Response response =
        client
            .newCall(
                new Request.Builder().get().url(server.localEndpoint() + "/people/bob").build())
            .execute();

    assertEquals("{\"name\":\"bob\"}", response.body().string());
  }

  @Test
  void testGetPersonNotFound() throws IOException {
    Response response =
        client
            .newCall(
                new Request.Builder().get().url(server.localEndpoint() + "/people/bob").build())
            .execute();

    assertEquals(404, response.code());
  }
}
