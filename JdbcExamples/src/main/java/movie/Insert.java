package movie;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import movie.model.Image;
import movie.model.Movie;
import oracle.jdbc.OracleType;
import oracle.sql.json.OracleJsonFactory;
import oracle.sql.json.OracleJsonObject;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Inserts three JSON values into the {@code movie} table.
 * 
 * <p>
 * Run {@link CreateTable} before running this example. The purpose of this
 * example is to show some of different JSON can be input into a query. In all
 * cases, the value inserted is converted to Oracle's binary JSON format
 * internally before being sent to the database.
 * </p>
 */
public class Insert {

    public static void main(String[] args) throws Exception {
        PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(String.join("", args));
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        
        try (Connection con = pool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO movie VALUES (:1)");
    
            // JSON text (String)
            String str = "{\"name\":\"Iron Man\", \"genre\": \"Action\", \"gross\":585366247}";
            pstmt.setObject(1, str, OracleType.JSON);
            pstmt.execute();

            // JSON object
            OracleJsonFactory factory = new OracleJsonFactory();
            OracleJsonObject obj = factory.createObject();
            obj.put("name", "Interstellar");
            obj.put("genre", "Sci-fi");
            obj.put("gross", 677471339);
            obj.put("release", OffsetDateTime.of(2008, 5, 2, 0, 0, 0, 0, ZoneOffset.UTC));
            pstmt.setObject(1, obj, OracleType.JSON);
            pstmt.execute();
    
            // JSON text (byte stream/file)
            FileInputStream in = new FileInputStream("data/matrix.json");
            pstmt.setObject(1, in, OracleType.JSON);
            in.close();
            pstmt.execute();
            
            // Jackson Data Bind (works because of dependency ojdbc-provider-jackson-oson)
            Movie movie = new Movie();
            movie.setName("The Godfather");
            movie.setGross(BigDecimal.valueOf(246120974));
            movie.setGenre("Drama");
            
            List<Image> images = new ArrayList<Image>();
            images.add(new Image("img1.png", "Main movie poster"));
            images.add(new Image("img2.png", "Marlon Brando"));
            movie.setImages(images);
            pstmt.setObject(1, movie, OracleType.JSON);
            pstmt.execute();
            
            System.out.println("Inserted four movies into the movie table");
        }
    }
}
