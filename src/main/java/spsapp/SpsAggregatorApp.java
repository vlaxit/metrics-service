package spsapp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

public class SpsAggregatorApp {

    public static void main(String[] args) throws Exception {

        try {
            BufferedReader inputReader = urlStremReader();
            //BufferedReader inputReader = fileReader();

            PipedInputStream resultStream = new PipedInputStream();
            PipedOutputStream aggregatedStream = new PipedOutputStream();
            resultStream.connect(aggregatedStream);

            SpsAggregator spsAggregator = new SpsAggregator(inputReader, aggregatedStream);
            SpsAggregateConsumer consumer = new SpsAggregateConsumer(resultStream);
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
        BufferedReader in = new BufferedReader(
            new InputStreamReader(spss.openStream()));
        return in;
    }

    private static BufferedReader  fileReader() throws IOException {
        final File initialFile = new File("src/main/resources/input.txt");
        final InputStream targetStream =
            new DataInputStream(new FileInputStream(initialFile));
        return new BufferedReader(new InputStreamReader(targetStream));
    }

}
