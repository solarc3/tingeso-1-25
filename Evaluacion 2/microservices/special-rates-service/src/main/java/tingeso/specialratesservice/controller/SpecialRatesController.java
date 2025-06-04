package tingeso.specialratesservice.controller;


import tingeso.specialratesservice.DTO.SpecialRatesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tingeso.specialratesservice.services.SpecialRatesService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/birthday-discount")
public class SpecialRatesController {

    @Autowired
    private SpecialRatesService specialRatesService;

    @PostMapping("/")
    public BigDecimal calculateBirthdayDiscount(@RequestBody SpecialRatesRequest request){
        return specialRatesService.calculateBirthdayDiscount(request.getBasePrice(), request.getNumPeople());
    }
}
