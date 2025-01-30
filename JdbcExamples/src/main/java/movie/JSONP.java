package movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Wrapper;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import oracle.jdbc.OracleType;
import oracle.sql.json.OracleJsonObject;
import oracle.sql.json.OracleJsonValue;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Inserts and retrieves a value using JSON-P (jakarta.json) interfaces.
 * 
 * <p>
 * Run first: {@link CreateTable}, {@link Insert}
 * </p>
 * 
 * @see https://javaee.github.io/jsonp/ 
 */
public class JSONP {

    public static void main(String[] args) throws SQLException {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        
        PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(String.join("", args));
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        
        try (Connection con = pool.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO movie VALUES (:1)");
            JsonObject obj = factory.createObjectBuilder()
                                    .add("name", "Forrest Gump")
                                    .add("genre", "Comedy")
                                    .add("gross", 678151134)
                                    .build();
            stmt.setObject(1, obj, OracleType.JSON);
            stmt.execute();
            stmt.close();
            System.out.println("Inserted Forrest Gump ");
            
            stmt = con.prepareStatement(
                    "SELECT m.data FROM movie m WHERE m.data.name.string() = :1");
            stmt.setString(1, "Interstellar");
            ResultSet rs = stmt.executeQuery(); 
            rs.next();
            obj = rs.getObject(1, JsonObject.class);
            System.out.println("Retrieved Interstellar from the database");
            System.out.println(obj.toString());
            
            // Values such as JsonObject, JsonArray, JsonParser, and JsonGenerator
            // produced from JDBC can be mapped back and forth between the jakarta.json
            // counterparts using the facade pattern. Mapping back and forth does not
            // make a copy of the data but rather it provides an alternate view of the same
            // data.

            // Smith timestamp attribute is reported as a string when using the jakarta.json apis
            JsonValue value = obj.get("release");
            System.out.println(value + " is of type " + value.getValueType());
            
            // However, we can unwrap the object to get the true type
            OracleJsonObject oraObj = ((Wrapper)obj).unwrap(OracleJsonObject.class);
            OracleJsonValue oraValue = oraObj.get("release");
            System.out.println(oraValue + " is of type " + oraValue.getOracleJsonType());
            
            // Values can be rewraped at any time
            JsonObject obj2 = oraObj.wrap(JsonObject.class); 
            System.out.println(obj.equals(obj2));
        }
    }
}
