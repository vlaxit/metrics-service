package spsapp;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.junit.jupiter.api.Test;

class SpsAggregatorTest {

    private static final String EXPECTED = "{\"device\":\"android\",\"sps\":1,\"title\":\"stranger things\",\"country\":\"JP\",\"time\":1613239895518}\n"
        + "{\"device\":\"ps3\",\"sps\":1,\"title\":\"cuervos\",\"country\":\"USA\",\"time\":1613239895518}\n"
        + "{\"device\":\"xbox_360\",\"sps\":2,\"title\":\"matrix\",\"country\":\"IND\",\"time\":1613239895518}\n"
        + "{\"device\":\"xbox_360\",\"sps\":2,\"title\":\"cuervos\",\"country\":\"IND\",\"time\":1613239895518}\n";
    private SpsAggregator spsAggregator;

    @Test
    void runTest() throws Exception{
        // given
        BufferedReader inputReader = fileReader();
        PipedInputStream resultStream = new PipedInputStream();
        PipedOutputStream aggregatedStream = new PipedOutputStream();
        resultStream.connect(aggregatedStream);
        spsAggregator = new SpsAggregator(inputReader, aggregatedStream);
        // when
        spsAggregator.run();
        // then
        String result = outputAsString(new BufferedReader(
            new InputStreamReader(resultStream)));
        assertThat(result).isEqualTo(EXPECTED);

    }

    private BufferedReader fileReader() throws IOException {
        final File initialFile = new File("src/test/resources/input.txt");
        final InputStream targetStream =
            new DataInputStream(new FileInputStream(initialFile));
        return new BufferedReader(new InputStreamReader(targetStream));
    }

    private String outputAsString(BufferedReader resultReader) {
        StringBuilder output = new StringBuilder();
        try {
            String outputLine;
            while ((outputLine = resultReader.readLine()) != null) {
                System.out.println(outputLine);
                output.append(outputLine + "\n");
            }
            resultReader.close();
        } catch(Exception e){
            System.err.println("Something went wrong during read out: " + e);
        }
        return output.toString();
    }
}