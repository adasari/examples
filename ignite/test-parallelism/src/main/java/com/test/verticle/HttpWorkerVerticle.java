package com.test.verticle;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.cache.loader.CacheLoader;
import com.test.cache.loader.CacheManager;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;

public class HttpWorkerVerticle extends AbstractVerticle {

    private static Logger logger = LoggerFactory.getLogger(HttpWorkerVerticle.class);

      /**
     * @see io.vertx.core.AbstractVerticle#start()
     */
    @Override
    public void start() {
        vertx.eventBus().consumer("CacheManager.load", this::load);
    }

	 /**
	 * @param message
	 */
	private void load(Message<String> message) {
	    	JsonObject responseMessage = new JsonObject();
	    	try {	

	    		List<CacheLoader> list = new ArrayList<>();
	    		for (int i = 0; i < 8; i++){
	    			list.add(new CacheLoader(i));
	    		}
	    		
	    		 CacheManager.getInstance().compute().withAsync().call(list);
	    		 
				 responseMessage.put("status", 200);
	             responseMessage.put("body", "{\"id\":200, \"status\" : \"loading started\"}");
	    	}catch (Exception ex){
	    		 responseMessage.put("status", 500);
	             responseMessage.put("body", ex.getMessage());
	    	}
	    	
	    	message.reply(responseMessage);
	 }
}
