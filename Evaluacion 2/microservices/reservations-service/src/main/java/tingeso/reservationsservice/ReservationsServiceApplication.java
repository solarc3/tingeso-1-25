package tingeso.reservationsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReservationsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationsServiceApplication.class, args);
	}

}
