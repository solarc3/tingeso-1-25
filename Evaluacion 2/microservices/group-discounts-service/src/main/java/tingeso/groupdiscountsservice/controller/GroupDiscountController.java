package tingeso.groupdiscountsservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tingeso.groupdiscountsservice.services.GroupDiscountService;
import tingeso.groupdiscountsservice.DTO.GroupDiscountRequest;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/group-discounts")
public class GroupDiscountController {

    @Autowired
    private GroupDiscountService groupDiscountService;

    @PostMapping("/group")
    public BigDecimal calculateGroupDiscount(@RequestBody GroupDiscountRequest request) {
        return groupDiscountService.calculateGroupDiscount(
            request.getBasePrice(),
            request.getNumberOfPeople()
                                                          );
    }
}