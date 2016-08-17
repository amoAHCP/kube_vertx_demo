package ch.trivadis.verticles;

import ch.trivadis.configuration.CustomEndpointConfig;
import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.*;

/**
 * Created by Andy Moncsek on 01.04.16.
 * java -jar target/frontend-verticle-1.0-SNAPSHOT-fat.jar -conf local.json -cluster -cp cluster/
 */
@ServiceEndpoint(port = 8181)
@EndpointConfig(CustomEndpointConfig.class)
public class VxmsGateway extends VxmsEndpoint {


    public static final String USER_GET = "/api/users";
    public static final String USER_GET_BY_ID = "/api/users/:id";
    public static final String USER_POST = "/api/users";
    public static final String USER_PUT = "/api/users/:id";
    public static final String USER_DELETE = "/api/users/:id";


    @Override
    public void postConstruct(final Future<Void> startFuture) {
        // for demo purposes
        InitMongoDB.initMongoData(vertx, config());
    }

    @Path(USER_GET)
    @GET
    public void userGet(RestHandler handler) {

        handler.
                eventBusRequest().
                send(USER_GET, "").
                onErrorResult(onError -> new JsonArray().add(DefaultResponses.defaultErrorResponse()).encode()).
                mapToStringResponse(eventResponse -> eventResponse.result().body().toString()).
                retry(2).
                execute();
    }

    @Path(USER_GET_BY_ID)
    @GET
    public void userGetById(RestHandler handler) {
        final String id = handler.request().param("id");
        if (id == null || id.isEmpty()){
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                eventBusRequest().
                send(USER_GET_BY_ID, id).
                onErrorResult(onError -> DefaultResponses.defaultErrorResponse()).
                mapToStringResponse(eventResponse -> eventResponse.result().body().toString()).
                retry(2).
                execute();
    }

    @Path(USER_POST)
    @POST
    public void userPOST(RestHandler handler) {
        final Buffer body = handler.request().body();
        if (body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }

        handler.
                eventBusRequest().
                send(USER_POST + "-post", body.toJsonObject()).
                onErrorResult(onError -> DefaultResponses.defaultErrorResponse()).
                mapToStringResponse(eventResponse -> eventResponse.result().body().toString()).
                retry(2).
                execute();
    }

    @Path(USER_PUT)
    @PUT
    public void userPutById(RestHandler handler) {
        final String id = handler.request().param("id");
        final Buffer body = handler.request().body();
        if (id == null || id.isEmpty()|| body == null || body.toJsonObject().isEmpty()) {
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }


        final JsonObject message = DefaultResponses.mapToUser(body.toJsonObject(), id);
        handler.
                eventBusRequest().
                send(USER_PUT + "-put", message).
                onErrorResult(onError -> DefaultResponses.defaultErrorResponse()).
                mapToStringResponse(eventResponse -> eventResponse.result().body().toString()).
                retry(2).
                execute();
    }

    @Path(USER_DELETE)
    @DELETE
    public void userDeleteById(RestHandler handler) {
        final String id = handler.request().param("id");
        if (id == null || id.isEmpty()){
            handler.response().end(HttpResponseStatus.BAD_REQUEST);
            return;
        }
        handler.
                eventBusRequest().
                send(USER_DELETE + "-delete", id).
                onErrorResult(onError -> DefaultResponses.defaultErrorResponse()).
                mapToStringResponse(eventResponse -> eventResponse.result().body().toString()).
                retry(2).
                execute(HttpResponseStatus.NO_CONTENT);


    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).
                setConfig(new JsonObject().put("local", true).put("host", "0.0.0.0").put("port", 8181));
        VertxOptions vOpts = new VertxOptions();
        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(VxmsGateway.class.getName(), options, handle -> {

                });
            }
        });

    }
}
