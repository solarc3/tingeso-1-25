package tingeso.karting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.PricingRequestDto;
import tingeso.karting.DTO.PricingResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {
    private static final BigDecimal IVA = BigDecimal.valueOf(0.19);

    private final PriceConfigService priceConfigService;

    @Autowired
    public PricingService(PriceConfigService priceConfigService) {
        this.priceConfigService = priceConfigService;
    }

    public PricingResponseDto calculatePrice(PricingRequestDto request) {
        BigDecimal baseRate = determineBaseRate(request);

        BigDecimal groupDiscount = baseRate.multiply(
            BigDecimal.valueOf(groupDiscountRate(request.getNumPeople())));

        BigDecimal freqDiscount = baseRate.multiply(
            BigDecimal.valueOf(frequencyDiscountRate(request.getMonthlyVisits())));

        BigDecimal birthdayDiscount = BigDecimal.ZERO;
        if(request.isBirthday()) {
            birthdayDiscount = baseRate.multiply(
                BigDecimal.valueOf(birthdayDiscountRate(request.getNumPeople())));
        }

        BigDecimal totalDiscount = groupDiscount.add(freqDiscount).add(birthdayDiscount);
        BigDecimal netPrice = baseRate.subtract(totalDiscount);

        //iva
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

    private BigDecimal determineBaseRate(PricingRequestDto request) {
        if (request.getLaps() != null) {
            switch (request.getLaps()) {
                case 10: return priceConfigService.getPrice("VUELTAS_10_PRECIO");
                case 15: return priceConfigService.getPrice("VUELTAS_15_PRECIO");
                case 20: return priceConfigService.getPrice("VUELTAS_20_PRECIO");
                default:   throw new ResponseStatusException(
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

    private double groupDiscountRate(int numPeople) {
        if (numPeople <= 2) return 0.0;
        if (numPeople <= 5) return priceConfigService.getPrice("DESCUENTO_GRUPO_PEQUENO")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
        if (numPeople <= 10) return priceConfigService.getPrice("DESCUENTO_GRUPO_MEDIANO")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
        return priceConfigService.getPrice("DESCUENTO_GRUPO_GRANDE")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private double frequencyDiscountRate(int visitsPerMonth) {
        if (visitsPerMonth >= 7) return priceConfigService.getPrice("DESCUENTO_FRECUENCIA_ALTA")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
        if (visitsPerMonth >= 5) return priceConfigService.getPrice("DESCUENTO_FRECUENCIA_MEDIA")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
        if (visitsPerMonth >= 2) return priceConfigService.getPrice("DESCUENTO_FRECUENCIA_BAJA")
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .doubleValue();
        return 0.0;
    }

    private double birthdayDiscountRate(int numPeople) {
        // Solo aplicamos descuento si hay al menos 3 personas
        if (numPeople < 3) return 0.0;

        BigDecimal descuentoBase = priceConfigService.getPrice("DESCUENTO_CUMPLEANOS")
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal descuento = descuentoBase;
        if (numPeople >= 11) {
            descuento = descuento.multiply(BigDecimal.valueOf(2));
        }

        // El descuento se reparte entre todos los asistentes
        return descuento.divide(BigDecimal.valueOf(numPeople), 4, RoundingMode.HALF_UP).doubleValue();
    }
}