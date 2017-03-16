package ch.trivadis.verticles;

import ch.trivadis.util.DefaultResponses;
import ch.trivadis.util.InitMongoDB;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Optional;

/**
 * Created by Andy Moncsek .
 */
public class GatewayVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // for demo purposes
        InitMongoDB.initMongoData(vertx, config());

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());


        // define some REST API

        router.get("/api/users").handler(this::getUsers);

        router.get("/api/users/:id").handler(this::getUserById);

        router.post("/api/users").handler(this::postUser);

        router.put("/api/users/:id").handler(this::updateUser);

        router.delete("/api/users/:id").handler(this::deleteUser);

        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(getHttpPort(),getHost());

        startFuture.complete();
    }




    private void updateUser(RoutingContext ctx) {
        // update the user properties
        JsonObject update = ctx.getBodyAsJson();
        JsonObject message = updateEntity(ctx, update);
        vertx.eventBus().send("/api.users.id.PUT", message, (Handler<AsyncResult<Message<String>>>) responseHandler -> defaultResponse(ctx, responseHandler));
    }


    private void postUser(RoutingContext ctx) {
        JsonObject newUser = ctx.getBodyAsJson();
        vertx.eventBus().send("/api.users.POST", newUser, (Handler<AsyncResult<Message<String>>>) responseHandler -> defaultResponse(ctx, responseHandler));
    }

    private void getUserById(RoutingContext ctx) {
        vertx.eventBus().send("/api.users.id.GET", ctx.request().getParam("id"), (Handler<AsyncResult<Message<String>>>) responseHandler -> defaultResponse(ctx, responseHandler));
    }

    private void getUsers(RoutingContext ctx) {
        vertx.eventBus().send("/api.users.GET", "", (Handler<AsyncResult<Message<String>>>) responseHandler -> defaultCollectionResponse(ctx, responseHandler));
    }

    private void deleteUser(RoutingContext ctx) {
        vertx.eventBus().send("/api.users.id.DELETE", ctx.request().getParam("id"), (Handler<AsyncResult<Message<String>>>) responseHandler -> {
            if (responseHandler.failed()) {
                ctx.fail(500);
            } else {
                ctx.response().setStatusCode(204);
                ctx.response().end();
            }

        });
    }

    private void defaultResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler) {
        if (responseHandler.failed()) {
            ctx.response().end(DefaultResponses.defaultErrorResponse().encodePrettily());
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(result.body());
        }
    }

    private void defaultCollectionResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler) {
        if (responseHandler.failed()) {
            ctx.response().end(new JsonArray().add(DefaultResponses.defaultErrorResponse()).encode());
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(result.body());
        }
    }

    private JsonObject updateEntity(RoutingContext ctx, JsonObject update) {
        JsonObject message = new JsonObject();
        message.put("username", update.getString("username"));
        message.put("firstName", update.getString("firstName"));
        message.put("lastName", update.getString("lastName"));
        message.put("address", update.getString("address"));
        message.put("id", ctx.request().getParam("id"));
        return message;
    }

    private String getHost() {
        return Optional.ofNullable(System.getProperty("http.address")).orElse("0.0.0.0");
    }

    private Integer getHttpPort() {
        return Integer.valueOf(Optional.ofNullable(System.getenv("httpPort")).orElse("8181"));
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true));
        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(GatewayVerticle.class.getName(), options, handle -> {

                });
            }
        });
    }


}
