package tingeso.tariffsservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tingeso.tariffsservice.DTO.PricingRequestDto;
import tingeso.tariffsservice.DTO.PricingResponseDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PricingService {
    private static final BigDecimal IVA = BigDecimal.valueOf(0.19);

    private final PriceConfigService priceConfigService;
    private final RestTemplate restTemplate;

    @Autowired
    public PricingService(PriceConfigService priceConfigService, RestTemplate restTemplate) {
        this.priceConfigService = priceConfigService;
        this.restTemplate = restTemplate;
    }

    public PricingResponseDto calculatePrice(PricingRequestDto request) {
        BigDecimal baseRate = determineBaseRate(request);

        BigDecimal groupDiscount = getGroupDiscount(baseRate, request.getNumPeople());
        BigDecimal freqDiscount = getFreqDiscount(baseRate, request.getMonthlyVisits());
        BigDecimal birthdayDiscount = getBirthdayDiscount(baseRate, request.getNumPeople(), request.isBirthday());
        BigDecimal totalDiscount = groupDiscount.add(freqDiscount)
            .add(birthdayDiscount);
        BigDecimal netPrice = baseRate.subtract(totalDiscount);
        BigDecimal ivaAmount = netPrice.multiply(IVA);
        BigDecimal totalPrice = netPrice.add(ivaAmount);

        return PricingResponseDto.builder()
            .baseRate(baseRate)
            .groupDiscount(groupDiscount)
            .frequencyDiscount(freqDiscount)
            .birthdayDiscount(birthdayDiscount)
            .tax(ivaAmount)
            .totalAmount(totalPrice)
            .build();
    }

    private BigDecimal getBirthdayDiscount(BigDecimal basePrice, int numPeople, boolean isBirthday) {
        if (numPeople < 3 || !isBirthday) {
            return BigDecimal.ZERO;
        }
        //no es necesario el 3r argumento, ya que sabiendo que si es cumple, para que tener que volver
        //a revisar, asi que se queda con solo 2 argumentos denuevo
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("basePrice", basePrice);
            request.put("numPeople", numPeople);
            return restTemplate.postForObject(
                "http://SPECIAL-RATES-SERVICE/api/birthday-discount/",
                request,
                BigDecimal.class);
        } catch (Exception e) {
            System.err.println("Error al llamar al servicio de descuentos de grupo: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private BigDecimal getGroupDiscount(BigDecimal basePrice, Integer numberOfPeople) {
        if (numberOfPeople == null || numberOfPeople <= 2) {
            return BigDecimal.ZERO;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("basePrice", basePrice);
            request.put("numberOfPeople", numberOfPeople);

            return restTemplate.postForObject(
                "http://GROUP-DISCOUNTS-SERVICE/api/group-discounts/group",
                request,
                BigDecimal.class);
        } catch (Exception e) {
            System.err.println("Error al llamar al servicio de descuentos de grupo: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getFreqDiscount(BigDecimal basePrice, Integer MonthlyVisits) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("basePrice", basePrice);
            request.put("monthlyVisits", MonthlyVisits);
            return restTemplate.postForObject(
                "http://CUSTOMER-DISCOUNTS-SERVICE/api/customer-discounts/monthly",
                request,
                BigDecimal.class);
        } catch (Exception e) {
            System.err.println("Error al llamar al servicio de descuentos de grupo: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private BigDecimal determineBaseRate(PricingRequestDto request) {
        if (request.getLaps() != null) {
            switch (request.getLaps()) {
                case 10:
                    return priceConfigService.getPrice("VUELTAS_10_PRECIO");
                case 15:
                    return priceConfigService.getPrice("VUELTAS_15_PRECIO");
                case 20:
                    return priceConfigService.getPrice("VUELTAS_20_PRECIO");
                default:
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Número de vueltas inválido: " + request.getLaps()
                    );
            }
        }

        int duration = request.getDuration();
        if (duration <= 30) return priceConfigService.getPrice("VUELTAS_10_PRECIO");
        if (duration <= 35) return priceConfigService.getPrice("VUELTAS_15_PRECIO");
        if (duration <= 40) return priceConfigService.getPrice("VUELTAS_20_PRECIO");
        throw new IllegalArgumentException("duración no soportada: " + duration);
    }

}