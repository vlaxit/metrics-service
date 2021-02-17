package spsaggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class SpsAggregator implements Runnable{

    private String inputLine;
    private final BufferedReader in;
    private final PipedOutputStream aggregatedStream;
    private final ByTimeAggregator<Sps> aggregator;
    private final ObjectMapper mapper;

    public SpsAggregator(BufferedReader in, PipedOutputStream aggregatedStream, int bufferSize) {
        this.in = in;
        this.aggregatedStream = aggregatedStream;
        aggregator = new ByTimeAggregator<Sps>(bufferSize);
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
            try {
                outputResidualBufferedData();
                in.close();
                aggregatedStream.close();
            } catch (IOException e){
                System.err.println("Error while closing piped stream: " + e);
            }
        }
    }

    private void outputResidualBufferedData() throws IOException {
        for (Map<Sps, Long> i : aggregator.releaseAll()) {
            putResultToStream(Optional.of(i), aggregatedStream);
        }
    }

    private void processInputLine(String line) throws IOException{
        //System.out.println(line);
        Sps sps;
        Optional<Map<Sps, Long>> output;
        if (line != null && !line.isEmpty()) {
            sps = SpsJaksonMapper.fromString(line);
            if (null == sps) {
                System.out.println("Aggregated result might be affected by the malformed input: " + line);
            } else if (!sps.isSuccess()) {
                //System.out.println("Skipping unsuccessfull input: " + line);
            } else {
                output = aggregator.put(sps);
                putResultToStream(output, aggregatedStream);
            }
        }
    }

    private void putResultToStream(Optional<Map<Sps, Long>> output, PipedOutputStream stream)
        throws IOException {
        if(output.isPresent()) {
            for (Entry<Sps, Long> i : output.get().entrySet()) {
                SpsAggregate j = toAggregate(i.getKey(), i.getValue());
                writeToStream(j, stream);
            }
        }
    }

    private SpsAggregate toAggregate(Sps sps, Long count){
        return new SpsAggregate(sps, count);
    }

    private void writeToStream(SpsAggregate spsAgregate, PipedOutputStream stream) throws IOException{
        try {
            String value = mapper.writeValueAsString(spsAgregate);
            stream.write((value + "\n").getBytes());
        } catch(IOException e){
            System.err.println("Error while writing into output stream: " + e);
            stream.close();
            throw e;
        }
    }
}
