package tingeso.karting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.karting.DTO.PriceConfigDto;
import tingeso.karting.services.PriceConfigService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final PriceConfigService priceConfigService;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, BigDecimal>> getPrices() {
        return ResponseEntity.ok(priceConfigService.getAllPrices());
    }

    @PostMapping("/prices")
    public ResponseEntity<Map<String, BigDecimal>> updatePrices(@RequestBody PriceConfigDto priceConfigDto) {
        Map<String, BigDecimal> prices = priceConfigDto.getPrices();
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            priceConfigService.setPrice(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(priceConfigService.getAllPrices());
    }

    @PostMapping("/price/{key}")
    public ResponseEntity<Map<String, Object>> updateSinglePrice(
        @PathVariable String key,
        @RequestBody Map<String, BigDecimal> requestBody
                                                                ) {
        BigDecimal newPrice = requestBody.get("price");
        if (newPrice == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Price value is required"));
        }

        priceConfigService.setPrice(key, newPrice);

        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("price", priceConfigService.getPrice(key));
        response.put("updated", true);

        return ResponseEntity.ok(response);
    }
}