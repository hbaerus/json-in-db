package movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import oracle.jdbc.OracleTypes;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Shows how to store multiple different record types and distinguish them
 * on read using Java 21 Record patterns. 
 */
public class JacksonDataBindRecordPatterns {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Circle.class, name = "circle"),
        @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle")
    })
    interface Shape {
        double area();
    }
    
    record Rectangle(double length, double width) implements Shape {

        @Override
        public double area() {
            return length * width;
        }
        
    }

    record Circle(double radius) implements Shape {

        @Override
        public double area() {
            return Math.PI * Math.pow(radius, 2);
        }
        
    }
    
    static record Movie(String name, String genre, OffsetDateTime created) {}
    
    public static void main(String[] args) throws Exception {
        PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(String.join("", args));
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        
        try (Connection con = pool.getConnection()) {
            Statement stmt = con.createStatement();
            stmt.execute("drop table if exists shapes");
            stmt.execute("create table shapes(data json)");
            PreparedStatement insert = con.prepareStatement("insert into shapes values (?)");
            insert.setObject(1, new Circle(3), OracleTypes.JSON);
            insert.execute();
            insert.setObject(1, new Rectangle(2,2), OracleTypes.JSON);
            insert.execute();
            insert.setObject(1, new Rectangle(2,5), OracleTypes.JSON);
            insert.execute();
            
            ResultSet rs = stmt.executeQuery("select data from shapes");
            while (rs.next()) {
                Shape shape = rs.getObject(1, Shape.class);
                if (shape instanceof Rectangle (var length, var width)) {
                    System.out.println("Rectangle: " + length + " x " + width);
                } else if (shape instanceof Circle(var radius)) {
                    System.out.println("Circle: " + radius);
                } else {
                    throw new IllegalStateException(shape.getClass().toString());
                }
            }
            rs.close();
            insert.close();
            stmt.close();
        }
    }
}
