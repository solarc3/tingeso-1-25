package tingeso.tariffsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.tariffsservice.services.PriceConfigService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final PriceConfigService priceConfigService;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, BigDecimal>> getPrices() {
        return ResponseEntity.ok(priceConfigService.getAllPrices());
    }

    @PostMapping("/price/{key}")
    public ResponseEntity<Map<String, Object>> updatePrice(@PathVariable String key,
                                                           @RequestBody Map<String, BigDecimal> body) {
        BigDecimal price = body.get("price");
        priceConfigService.setPrice(key, price);
        return ResponseEntity.ok(Map.of(
                "key", key,
                "price", priceConfigService.getPrice(key),
                "updated", true
        ));
    }
}
