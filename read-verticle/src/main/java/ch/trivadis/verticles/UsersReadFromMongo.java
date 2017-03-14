package ch.trivadis.verticles;

import ch.trivadis.util.InitMongoDB;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.stream.Collectors;

/**
 * Created by Andy Moncsek .
 */
public class UsersReadFromMongo extends AbstractVerticle {
    private MongoClient mongo;


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        mongo = InitMongoDB.initMongoData(vertx,config());

        vertx.eventBus().consumer("/api/users", getAllUsers());

        vertx.eventBus().consumer("/api/users/:id", getAllUserById());

        startFuture.complete();

    }



    private Handler<Message<Object>> getAllUsers() {
        return handler -> mongo.find("users", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
                handler.fail(500, "lookup failed");
                return;
            }
            handler.reply(new JsonArray(lookup.result().stream().collect(Collectors.toList())).encode());
        });
    }

    private Handler<Message<Object>> getAllUserById() {
        return handler -> {
            final Object body = handler.body();
            final String id = body.toString();
            mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> getResultAndReply(handler, lookup));
        };
    }

    private void getResultAndReply(Message<Object> handler, AsyncResult<JsonObject> lookup) {
        if (lookup.failed()) {
            handler.fail(500, "lookup failed");
            return;
        }
        JsonObject user = lookup.result();
        if (user == null) {
            handler.fail(404, "no user found");
        } else {
            handler.reply(user.encode());
        }
    }


    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        VertxOptions vOpts = new VertxOptions();
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("local", true));
        vOpts.setClustered(true);
        Vertx.clusteredVertx(vOpts, cluster -> {
            if (cluster.succeeded()) {
                final Vertx result = cluster.result();
                result.deployVerticle(UsersReadFromMongo.class.getName(), options, handle -> {

                });
            }
        });
    }

}
