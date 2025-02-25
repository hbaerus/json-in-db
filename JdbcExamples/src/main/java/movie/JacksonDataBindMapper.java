package movie;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import oracle.jdbc.provider.oson.JacksonOsonConverter;

/**
 * This example shows how plain Java objects can be mapped to Oracle's binary
 * JSON encoding format (OSON). Unlike example {@code JacksonDataBind}, this
 * examples show how the object can be mapped to and from the OSON format
 * independent of a database. It uses Jackson's ObjectMapper and Oracle's
 * {@code OsonFactory}
 */
public class JacksonDataBindMapper {

    static record Movie(String name, String genre, OffsetDateTime created) {}
    
    public static void main(String[] args) throws Exception {

        Movie movie = new Movie("Iron Man", "Action", OffsetDateTime.now());
        
        ObjectMapper mapper = JacksonOsonConverter.getObjectMapper();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, movie);
        baos.close();

        byte[] oson = baos.toByteArray();

        Movie movie2 = mapper.readValue(oson, Movie.class);
        System.out.println("Name: " + movie2.name());
        System.out.println("Genre: " + movie2.genre());
        
    }
}
