package tingeso.tariffsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class TariffsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TariffsServiceApplication.class, args);
    }

}
