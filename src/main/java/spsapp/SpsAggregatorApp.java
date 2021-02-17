package spsapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import spsaggregate.SpsAggregator;

public class SpsAggregatorApp {

    private static final int MIN_BUFFER_SIZE = 3;
    protected static int BUFFER_SIZE;
    private static final String SPS_URL = "https://tweet-service.herokuapp.com/sps";

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            BUFFER_SIZE = Math.max(Integer.parseInt(args[0]), MIN_BUFFER_SIZE);
        } else {
            BUFFER_SIZE = MIN_BUFFER_SIZE;
        }
        System.out.println("Buffer size (sec): " + BUFFER_SIZE);

        try {
            BufferedReader inputReader = bufferedReaderFromFlux();

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

    public static BufferedReader urlBufferedReader() throws IOException {
        URL spss = new URL(SPS_URL);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(spss.openStream()));
        return in;
    }

    public static BufferedReader bufferedReaderFromFlux() throws IOException {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                getInputStreamFromFluxDataBuffer(
                    getInputAsFlux(SPS_URL))));
        return in;
    }

    private static Flux<DataBuffer> getInputAsFlux(String url) {
        WebClient webClient = WebClient.create(url);
        return webClient.get()
            .retrieve()
            .bodyToFlux(DataBuffer.class)
            .doOnError(throwable -> System.err.println(throwable.fillInStackTrace()));
    }

    private static InputStream getInputStreamFromFluxDataBuffer(Flux<DataBuffer> data) throws IOException {
        PipedOutputStream osPipe = new PipedOutputStream();
        PipedInputStream isPipe = new PipedInputStream(osPipe);
        DataBufferUtils.write(data, osPipe)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnComplete(() -> {
                try {
                    osPipe.close();
                } catch (IOException ignored) {
                }
            })
            .subscribe(DataBufferUtils.releaseConsumer());
        return isPipe;
    }

}
