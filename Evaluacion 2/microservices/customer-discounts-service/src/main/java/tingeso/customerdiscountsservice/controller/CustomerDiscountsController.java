package tingeso.customerdiscountsservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tingeso.customerdiscountsservice.DTO.CustomerDiscountRequest;
import tingeso.customerdiscountsservice.services.CustomerDiscountService;

import java.math.BigDecimal;

@RestController
//@RequestMapping("/api/customer-discounts")
public class CustomerDiscountsController {
    @Autowired
    private CustomerDiscountService customerDiscountService;

    @PostMapping("/monthly")
    public BigDecimal calculateCustomerDiscount(@RequestBody CustomerDiscountRequest request){
        return customerDiscountService.calculateFrecuencyDiscount(
            request.getBasePrice(),
            request.getMonthlyVisits());

    }
}
