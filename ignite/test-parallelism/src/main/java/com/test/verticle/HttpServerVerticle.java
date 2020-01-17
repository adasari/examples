package com.test.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;

public class HttpServerVerticle extends AbstractVerticle {
	
	private static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
	
	/** 
	 * @see io.vertx.core.AbstractVerticle#start()
	 */
	public void start() throws Exception {
		
		JsonObject config = config();
		APIConfigurations restAPIConfig = new APIConfigurations(vertx, config);
		
		Router router = restAPIConfig.configRouter();
		
		HttpServerOptions options = new HttpServerOptions();
		options.setCompressionSupported(true);
		
		HttpServer server = vertx.createHttpServer(options);
		server.requestHandler(router::accept)
			.listen(config().getInteger("port", 8099), status -> {
				if (status.succeeded()){
					logger.warn("HttpServerVerticle server listening to ..."+config().getInteger("port", 8099));
				}else{
					logger.error(" Error : already "+ config().getInteger("port", 8099)+" is in use");
				}
			});
	}
}
