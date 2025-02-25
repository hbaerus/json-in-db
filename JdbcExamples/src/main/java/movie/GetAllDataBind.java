package movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import movie.model.Movie;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * This example shows how plain Java objects can be mapped to JSON type columns
 * using JSON bind. Internally, JDBC converts the Java object (Movie) too and
 * from OSON - Oracle's efficient binary encoding for JSON.
 * <p>
 * The dependency {@code ojdbc-provider-jackson-oson} is necessary to enable
 * these conversions. See: <a href=
 * "https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-jackson-oson">https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-jackson-oson</a>
 * </p>
 */
public class GetAllDataBind {
  
  public static void main(String[] args) throws Exception {
    
    PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
    pool.setURL(String.join("", args));
    pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    try (Connection con = pool.getConnection()) {
        PreparedStatement read = con.prepareStatement("SELECT data FROM movie");
        ResultSet rs = read.executeQuery();
        while(rs.next()) {
            
            Movie movie = rs.getObject(1, Movie.class);
            System.out.println(movie.getName() + "\t (" + movie.getGenre() + ")");
            
        }
        rs.close();
        read.close();
    }
  }
}
