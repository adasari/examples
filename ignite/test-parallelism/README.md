# test-ignite-parallelism

sample application built on vertx+ apache ignite

To run the application -
-----------------------
java -jar test-parallelism-0.0.1-SNAPSHOT-fat.jar test.xml

test.xml is the ignite.xml which is availablie in the resources folder. use the test node ips
application runs on 8099 port by default

Building the applicaiton : 
--------------------------

maven clean install

To initiate the cache load :
--------------------------

http://localhost:8099/api/cache/load

CacheLoader.java is the iginite callable class which loads the data into cache using data streamer.
HttpWorkerVerticle#load which starts the compute job to load the data into cache when above mentioned rest api is invoked.


