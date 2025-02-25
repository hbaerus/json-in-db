package movie.springjdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import movie.springjdbc.model.MovieDetails;
import oracle.jdbc.provider.oson.JacksonOsonConverter;
import oracle.jdbc.provider.oson.OsonFactory;
import oracle.jdbc.provider.oson.OsonGenerator;
import oracle.sql.json.OracleJsonDatum;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

@Configuration
public class Config extends AbstractJdbcConfiguration {
    
    @Bean
    DataSource dataSource() throws SQLException {
        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionFactoryClassName("oracle.jdbc.replay.OracleDataSourceImpl");
        dataSource.setURL(System.getProperty("url"));
        dataSource.setInitialPoolSize(5);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(10);
        dataSource.setConnectionProperty("oracle.jdbc.jsonDefaultGetObjectType","oracle.sql.json.OracleJsonDatum");
        return dataSource;
    }

    @Override
    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(
            List.of(
                new MovieDetailsReader(),
                new MovieDetailsWriter()
            )
        );
    }
    
    @ReadingConverter
    private static class MovieDetailsReader implements Converter<OracleJsonDatum, MovieDetails> {
        @Override
        public MovieDetails convert(OracleJsonDatum source) {
            ObjectMapper mapper = JacksonOsonConverter.getObjectMapper();
            try {
                return mapper.readValue(source.shareBytes(), MovieDetails.class);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    
    @WritingConverter
    private static class MovieDetailsWriter implements Converter<MovieDetails, OracleJsonDatum> {
        @Override
        public OracleJsonDatum convert(MovieDetails source) {
            ObjectMapper mapper = JacksonOsonConverter.getObjectMapper();
            OsonFactory factory = (OsonFactory) mapper.getFactory();
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
              try (OsonGenerator osonGen = (OsonGenerator) factory.createGenerator(out)) {
                mapper.writeValue(osonGen, source);
              }
              return new OracleJsonDatum(out.toByteArray());
            } catch (IOException e) {
              throw new IllegalArgumentException(e);
            } 
        }
    }


}
