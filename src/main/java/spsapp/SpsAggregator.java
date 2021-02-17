package spsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Map;

public class SpsAggregator implements Runnable{

    private String inputLine;
    private final BufferedReader in;
    private final PipedOutputStream aggregatedStream;
    private final ByTimeAggregator<Sps> aggregator;
    private final ObjectMapper mapper;

    public SpsAggregator(BufferedReader in, PipedOutputStream aggregatedStream) {
        this.in = in;
        this.aggregatedStream = aggregatedStream;
        aggregator = new ByTimeAggregator<Sps>();
        mapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            while ((inputLine = in.readLine()) != null) {
                processInputLine(inputLine);
            }
        } catch(IOException e){
            System.err.println("Something went wrong: " + e);
        } finally {
            outputResidualBufferedData();
            try {
                in.close();
                aggregatedStream.close();
            } catch (IOException e){
                System.err.println("Error while closing piped stream: " + e);
            }
        }
    }

    private void outputResidualBufferedData() {
        aggregator.releaseAll().forEach(i -> putResultToStream(i, aggregatedStream));
    }

    private void processInputLine(String line) throws IOException{
        System.out.println(line);
        Sps sps;
        Map<Sps, Long> output;
        if (line != null && !line.isEmpty()) {
            //System.out.println("parsing sps from json...");
            sps = SpsJaksonMapper.fromString(line);
            if (null == sps) {
                System.out.println("Aggregated result might be affected by the malformed input: " + line);
            } else if (!sps.isSuccess()) {
                System.out.println("Skipping unsuccessfull input: " + line);
            } else {
                output = aggregator.put(sps);
                putResultToStream(output, aggregatedStream);
            }
        }
    }

    private void putResultToStream(Map<Sps, Long> output, PipedOutputStream stream) {
        //replace with optional
        if(output != null) {
            output.entrySet().stream()
                .map(i -> toAggregate(i.getKey(), i.getValue()))
                .forEach(j -> writeToStream(j, stream));
        }
    }

    private SpsAggregate toAggregate(Sps sps, Long count){
        return new SpsAggregate(sps, count);
    }

    private void writeToStream(SpsAggregate spsAgregate, PipedOutputStream stream) {
        try {
            String value = mapper.writeValueAsString(spsAgregate);
            stream.write((value + "\n").getBytes());
        } catch(IOException e){
            System.err.println("Error while writing into output stream: " + e);
        }
    }

    private void writeToStream(String value, PipedOutputStream stream) {
        try {
            stream.write(("[From buffer] " + value + "\n").getBytes());
        } catch(IOException e){
            System.err.println("Error while writing into output stream: " + e);
        }
    }

}
