package ch.trivadis.verticles;

import ch.trivadis.configuration.CustomEndpointConfig;
import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 01.04.16.
 * java -jar target/frontend-verticle-1.0-SNAPSHOT-fat.jar -conf local.json -cluster -cp cluster/
 */
@ServiceEndpoint(port = 8181, name = "gateway")
@EndpointConfig(CustomEndpointConfig.class)
public class GatewayVxms extends VxmsEndpoint {

    Logger log = Logger.getLogger(GatewayVxms.class.getName());



    @Override
    public void postConstruct(final Future<Void> startFuture) {
        // for demo purposes
        InitMongoDB.initMongoData(vertx, config());
        startFuture.complete();

    }

    @Path("/api/users")
    @GET
    public void userGet(RestHandler handler) {
        handler.
                eventBusRequest().
                send("/read/api.users.GET", "").
                mapToStringResponse(this::defaultResponse).
                retry(2).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((onError, future) ->
                        future.complete(new JsonArray().add(DefaultResponses.
                                defaultErrorResponse(onError.getMessage())).
                                encodePrettily())
                ).
                execute();
    }

    @Path("/api/users/:id")
    @GET
    public void userGetById(RestHandler handler) {
        final String id = handler.request().param("id");
        if (id == null || id.isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                eventBusRequest().
                send("/read/api.users.id.GET", id).
                mapToStringResponse(this::defaultResponse).
                retry(2).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((onError, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(onError.getMessage()).
                                encodePrettily())
                ).
                execute();
    }

    @Path("/api/users")
    @POST
    public void userPOST(RestHandler handler) {
        final Buffer body = handler.request().body();
        if (body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                eventBusRequest().
                send("/write/api.users.POST", body.toJsonObject()).
                mapToStringResponse(this::defaultResponse).
                retry(2).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((onError, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(onError.getMessage()).
                                encodePrettily())
                ).
                httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
                execute();
    }

    @Path("/api/users/:id")
    @PUT
    public void userPutById(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        if (id == null || id.isEmpty() || body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        final JsonObject user = DefaultResponses.mapToUser(body.toJsonObject(), id);
        handler.
                eventBusRequest().
                send("/write/api.users.id.PUT", user).
                mapToStringResponse(this::defaultResponse).
                retry(2).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((onError, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(onError.getMessage()).
                                encodePrettily())
                ).
                httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
                execute();
    }



    @Path("/api/users/:id")
    @DELETE
    public void userDeleteById(RestHandler handler) {
        final String id = handler.request().param("id");
        if (id == null || id.isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                eventBusRequest().
                send("/write/api.users.id.DELETE", id).
                mapToStringResponse(this::defaultResponse).
                retry(2).
                timeout(2000).
                onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage())).
                onFailureRespond((onError, future) ->
                        future.complete(DefaultResponses.
                                defaultErrorResponse(onError.getMessage()).
                                encodePrettily())
                ).
                httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE).
                execute(HttpResponseStatus.NO_CONTENT);

    }

    private void defaultResponse(AsyncResult<Message<Object>> message, Future<String> future) {
        if (message.failed()) {
            future.fail(message.cause());
        } else {
            future.complete(message.result().body().toString());
        }
    }


    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).
                setConfig(new JsonObject().put("local", true).put("exportedPort", 8181));

        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(GatewayVxms.class.getName(), options, handle -> {

                });
            }
        });

    }
}
