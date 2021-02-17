package spsapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpsJaksonMapper {

    private static ObjectMapper spsMapper = new ObjectMapper();

    public static Sps fromString(String json) {
        try {
            return spsMapper.readValue(
                json.substring(json.indexOf('{'), json.length()),
                Sps.class);
        } catch (JsonProcessingException e){
            System.err.println("Error parsing input string " + json + ": " + e);
            return null;
        }
    }

}
