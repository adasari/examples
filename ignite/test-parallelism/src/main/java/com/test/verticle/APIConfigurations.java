package com.test.verticle;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.http.credentials.TokenCredentials;
import org.pac4j.http.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.http.profile.creator.ProfileCreator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.vertx.auth.Pac4jAuthProvider;
import org.pac4j.vertx.handler.impl.Pac4jAuthHandlerOptions;
import org.pac4j.vertx.handler.impl.RequiresAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;

public class APIConfigurations {

    private static Logger logger = LoggerFactory.getLogger(APIConfigurations.class);
    private final Vertx vertx;
    private final JsonObject config;

    /**
     * @param vertx
     * @param config
     */
    public APIConfigurations(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    /**
     * @return
     */
    public Router configRouter() {

        Router router = Router.router(vertx);

        io.vertx.ext.web.Router routerNonRx = (io.vertx.ext.web.Router) router.getDelegate();

        // CORS support
        router.route()
                .handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.PUT).allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.OPTIONS)
                        .allowedHeader("Content-Type").allowedHeader("Cache-Control")
                        .allowedHeader("Pragma").allowedHeader("Expires"));

        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.GET, "/api/cache/load").handler(this::reqHandler);
        
        router.route().handler(StaticHandler.create());
        return router;
    }

    public void reqHandler(RoutingContext ctx) {
        // this delivery options used for only initial full load which is taking more time.

        DeliveryOptions options = new DeliveryOptions();
        options.setSendTimeout(10 * 60 * 1000);

        try {
            switch (ctx.currentRoute().getPath()) {
            case "/api/cache/load":

                vertx.eventBus().rxSend("CacheManager.load", null).subscribe(reply -> sendResponse(reply, ctx), ex -> sendErrorResponse(ex, ctx));
                break;

            default:
                setNoCacheHeaders(ctx);
                ctx.request().response().headers().add("Content-Type", "application/json");
                ctx.request().response().setStatusCode(500);
                ctx.request().response().end("{\"message\": \"URL not found\"}");
            }
        }
        catch (Exception ex) {
            setNoCacheHeaders(ctx);
            ctx.request().response().headers().add("Content-Type", "application/json");
            ctx.request().response().setStatusCode(500);
            ctx.request().response().end("{\"message\": \"Unable to process the request\"}");
        }
    }

    /**
     * @param replyMessage
     * @param ctx
     */
    private void sendResponse(Message<Object> replyMessage, RoutingContext ctx) {

        JsonObject message = (JsonObject) replyMessage.body();

        ctx.request().response().headers().add("Content-Type", "application/json");
        setNoCacheHeaders(ctx);

        Integer status = message.getInteger("status");
        String body = message.getString("body");

        if (null != status) {
            ctx.request().response().setStatusCode(status);
        }

        if (null != body) {
        	ctx.request().response().end(body);
        }

    }

    /**
     * This method sets no cache headers to response.
     * @param ctx
     */
    private void setNoCacheHeaders(RoutingContext ctx) {
        ctx.request().response().headers().add("Cache-Control", "no-cache, no-store, must-revalidate");
        ctx.request().response().headers().add("Pragma", "no-cache");
        ctx.request().response().headers().add("Expires", "0");
    }

    private void sendErrorResponse(Throwable th, RoutingContext ctx) {
        logger.error("Error while invoking " + ctx.currentRoute().getPath(), th);
        setNoCacheHeaders(ctx);
        
        ctx.request().response().setStatusCode(500);
        JsonObject obj = new JsonObject();
        obj.put("id", 500);
        obj.put("message", th.getMessage());
        ctx.request().response().end(Json.encode(obj));
        
    }
}
