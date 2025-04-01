package tingeso.karting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KartingApplication {
	@RequestMapping("/")
	public String home() {
		return "";
	}

	public static void main(String[] args) {
		SpringApplication.run(KartingApplication.class, args);
	}

}
