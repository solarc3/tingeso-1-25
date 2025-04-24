package tingeso.karting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KartingApplication {
	@RequestMapping("/api/me")
	public String home() {
	    return "Hola soy " + System.getenv("HOSTNAME");
	}

	public static void main(String[] args) {
		SpringApplication.run(KartingApplication.class, args);
	}

}
