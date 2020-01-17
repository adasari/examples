package com.test;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.cache.loader.CacheManager;
import com.test.verticle.HttpServerVerticle;
import com.test.verticle.HttpWorkerVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;

public class ApplicationLauncher {

    private static Logger logger =  LoggerFactory.getLogger(ApplicationLauncher.class);
    public static void main(String[] args) throws MalformedURLException {

        ClusterManager mgr = null;

        System.getProperties().put("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        
        mgr = new IgniteClusterManager(new File(args[0]).toURI().toURL());

        String address = getDefaultAddress();

        VertxOptions opts = new VertxOptions();
        opts.setClusterManager(mgr).setClusterHost(address);

        Vertx.clusteredVertx(opts, status -> {
            if (status.succeeded()) {
            	logger.debug("Vertx started as a cluster");
            	
                Vertx vertx = status.result();
                CacheManager.getInstance(vertx.getDelegate());
                
                DeploymentOptions deploymentOptions = new DeploymentOptions();
                vertx.deployVerticle(HttpServerVerticle.class.getName(), deploymentOptions, res -> {
                    if (res.succeeded()) {
                        logger.debug("HttpServerVerticle  deployed successfully");
                        
                        DeploymentOptions options = new DeploymentOptions();
                        options.setWorker(true).setMultiThreaded(true);

                        vertx.deployVerticle(HttpWorkerVerticle.class.getName(), options, res2 -> {
                            if (res2.succeeded()) {
                                logger.debug("HttpWorkerVerticle deployed successfully");
                            }
                            else {
                                logger.error("HttpWorkerVerticle deployment failed ", res2.cause());
                            }
                        });
                    }
                    else {
                        logger.error("HttpServerVerticle deployment failed ", res.cause());
                    }
                });
            }
        });
    }

    /**
     * @return
     */
    public static String getAddress() {

        try {
            String dockerIp = InetAddress.getByName("vertx").getHostAddress();
            return dockerIp;
        }
        catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static String getDefaultAddress() {
        Enumeration<NetworkInterface> nets;
        NetworkInterface netinf;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e) {
            return null;
        }
        while (nets.hasMoreElements()) {
            netinf = nets.nextElement();
            Enumeration<InetAddress> addresses = netinf.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress() && !(address instanceof Inet6Address)) {
                    return address.getHostAddress();
                }
            }
        }
        return null;
    }
}
