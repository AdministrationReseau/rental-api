package inc.yowyob.rental_api;

import inc.yowyob.rental_api.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
// FIX: Added EnableConfigurationProperties to activate your AppProperties class
@EnableConfigurationProperties(AppProperties.class)
// This annotation is good practice to explicitly enable repositories
@EnableCassandraRepositories(basePackages = "inc.yowyob.rental_api")
public class RentalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalApiApplication.class, args);
	}
}