package restout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import spsaggregate.SpsAggregator;
import spsapp.SpsAggregatorApp;

@RestController
public class OutputController {

    public OutputController() {}

    // stream of JSON lines
    @GetMapping(value = "/sps-aggregate",
        produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<String> getAggregateJsonStream() {
        try {
            BufferedReader aggregatedReader = initiateProcessing();

            return Flux.generate(() -> processLine(aggregatedReader),
                (state, sink) -> {
                    sink.next(state);
                    return processLine(aggregatedReader);
                });
        } catch (Exception e) {
            System.err.println("Error while fetching aggregate: " + e);
            return Flux.empty();
        }
    }

    public String processLine(BufferedReader aggregatedReader){
        try {
            return aggregatedReader.readLine() + "\n";
        } catch (Exception e){
            System.err.println(e);
            return "";
        }
    }

    private BufferedReader initiateProcessing() throws Exception {
        try {
            PipedOutputStream aggregatedStream = new PipedOutputStream();
            BufferedReader inputReader = SpsAggregatorApp.bufferedReaderFromFlux();
            PipedInputStream resultStream = new PipedInputStream();
            aggregatedStream.connect(resultStream);
            BufferedReader aggregatedReader = new BufferedReader(
                new InputStreamReader(resultStream));
            SpsAggregator spsAggregator = new SpsAggregator(inputReader, aggregatedStream, RestApi.BUFFER_SIZE);
            Thread producerThread = new Thread(spsAggregator);
            producerThread.start();
            return aggregatedReader;
        } catch(Exception e){
            System.err.println("Error caught while starting a processing thread: " + e);
            throw e;
        }
    }
}
