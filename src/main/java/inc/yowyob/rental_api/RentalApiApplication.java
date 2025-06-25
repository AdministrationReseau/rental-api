package inc.yowyob.rental_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
public class RentalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalApiApplication.class, args);
	}
}
