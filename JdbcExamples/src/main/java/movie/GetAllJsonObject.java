package movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import movie.model.Movie;
import oracle.sql.json.OracleJsonObject;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Gets each movie as an instance of OracleJsonObject and 
 * prints the name and genre fields.
 */
public class GetAllJsonObject {
  
  public static void main(String[] args) throws Exception {
    
    PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
    pool.setURL(String.join("", args));
    pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    try (Connection con = pool.getConnection()) {
        PreparedStatement read = con.prepareStatement("SELECT data FROM movie");
        ResultSet rs = read.executeQuery();
        while(rs.next()) {
            
            OracleJsonObject movie = rs.getObject(1, OracleJsonObject.class);
            System.out.println(movie.getString("name") + "\t (" + movie.getString("genre") + ")");
            
        }
        rs.close();
        read.close();
    }
  }
}
