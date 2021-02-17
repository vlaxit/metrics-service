# metrics-service
Metrix service for aggregating streaming data

###About
The app aggregates data from https://tweet-service.herokuapp.com/sps endpoint, calculating entries by time (sec), and grouping by all other properties.
The result is a stream of json data which is result of calculation.

###Implementation details
Project is implemented using java 8 standard libs. The idea is to buffer input data for couple of seconds, in case additional data appears with delay, or the input stream of data is not ordered correctly.
From the data structure which buffers and groups/counts entries the resulting stream is produced, once the initial buffer gets populated. Currently one thread fills the buffer and another one reads from it, deleting read aggregated data from the structure immediately. 

The structure is implemented using TreeMap, which guarantees that the output stream of data is sorted by time ascending. 

Concerning scalability, current implementation would require some modifications to face this challenge. The idea is that the buffer of stored data is relatively large and consistent in time, which is not the case at the moment - the data get overwritten as stream of calculated data picks the results. However, if the buffer structure would be large enough, and available for longer period of time, one thread could fill it and multiple threads could read from it, thus enabling better scalability.

###Install and run
Java 8 and maven are needed to install/run the app. There are 2 versions of outputting the data
####Command line output
Outputs the calculated json stream in command line.

Install the app: 
<code>
mvn clean install
</code>

Start the app:
<code>
mvn exec:java -Dexec.mainClass=spsapp.SpsAggregatorApp -Dexec.args="7"
</code>

Where the first arg says how many seconds of data we want to buffer.

####Rest api output
Outputs the result in a streaming fashion as a rest endpoint, located at:
http://localhost:8080/sps-aggregate

#####Install the app: 
<code>
mvn clean install
</code>

#####Start the app:
<code>
mvn spring-boot:run -Dspring-boot.run.arguments="--bufferSize=8"
</code>

Where the --bufferSize parameter defines how many seconds of data we want to buffer.
