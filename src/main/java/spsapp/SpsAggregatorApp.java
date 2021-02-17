package spsapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import spsaggregate.SpsAggregator;

public class SpsAggregatorApp {

    private static int MIN_BUFFER_SIZE = 3;
    protected static int BUFFER_SIZE;

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            BUFFER_SIZE = Math.max(Integer.parseInt(args[0]), MIN_BUFFER_SIZE);
        } else {
            BUFFER_SIZE = MIN_BUFFER_SIZE;
        }
        System.out.println("Buffer size (sec): " + BUFFER_SIZE);

        try {
            BufferedReader inputReader = urlStremReader();

            PipedInputStream resultStream = new PipedInputStream();
            PipedOutputStream aggregatedStream = new PipedOutputStream();
            resultStream.connect(aggregatedStream);

            SpsAggregator spsAggregator = new SpsAggregator(inputReader, aggregatedStream, BUFFER_SIZE);
            ConsoleOutputter consumer = new ConsoleOutputter(resultStream);
            Thread producerThread = new Thread(spsAggregator);
            Thread consumerThread = new Thread(consumer);

            producerThread.start();
            consumerThread.start();
            producerThread.join();
            consumerThread.join();

        } catch(Exception e){
            System.err.println("Error caught during processing: " + e);
            throw e;
        }
    }

    private static BufferedReader urlStremReader() throws IOException {
        URL spss = new URL("https://tweet-service.herokuapp.com/sps");
        //URL spss = new URL("http://localhost:8080/sps-aggregate");
        BufferedReader in = new BufferedReader(
            new InputStreamReader(spss.openStream()));
        return in;
    }

}
