package spsapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;

public class SpsAggregateConsumer implements Runnable{

    private final BufferedReader resultReader;

    public SpsAggregateConsumer(InputStream resultStream){
        this.resultReader = new BufferedReader(
            new InputStreamReader(resultStream));
    }

    @Override
    public void run() {


        try {
            String outputLine;
            while ((outputLine = resultReader.readLine()) != null) {
                System.out.println(outputLine);
            }
            resultReader.close();
        } catch(Exception e){
            System.err.println("Something went wrong during read out: " + e);
        }

    }
}
