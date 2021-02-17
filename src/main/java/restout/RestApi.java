package restout;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class RestApi extends SpringBootServletInitializer {
    private static int MIN_BUFFER_SIZE = 3;
    protected static int BUFFER_SIZE;

    public static void main(String[] args) {
        if(args.length > 0) {
            try {
                String value = Arrays.stream(args)
                    .map(i-> i.split("="))
                    .filter(j -> "--bufferSize".equals(j[0]))
                    .map(k -> k[1]).findFirst()
                    .orElse("-1");
                BUFFER_SIZE = Math.max(Integer.parseInt(value), MIN_BUFFER_SIZE);
            } catch (NumberFormatException e){
                System.err.println("Invalid buffer size: " + e);
                BUFFER_SIZE = MIN_BUFFER_SIZE;
            }
        } else {
            BUFFER_SIZE = MIN_BUFFER_SIZE;
        }
        System.out.println("Buffer size (sec): " + BUFFER_SIZE);
        SpringApplication.run(RestApi.class, args);
    }
}
