package io.vertx.example.web.vertxbus.java;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.Runner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * A {@link io.vertx.core.Verticle} which bridges Java to the @{link EventBus}.
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Server.class);
  }

  @Override
  public void start() throws Exception {

    Router router = Router.router(vertx);

    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddress("feed"));

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route("/eventbus/*").handler(ebHandler);

    // Start the web server and tell it to use the router to handle requests.
    vertx.createHttpServer().requestHandler(router::accept)
      .listen(8080, "localhost", ar -> {
        if (ar.succeeded()) {
          System.out.printf("Server started on localhost:%s", ar.result().actualPort());
        } else if (ar.failed()) {
          System.out.printf("Failed: %s", ar.cause());
        }
      });

    EventBus eb = vertx.eventBus();

    vertx.setPeriodic(1000L, t -> {
      // Create a timestamp string
      String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));

      eb.send("feed", new JsonObject().put("now", timestamp));
    });
  }
}
