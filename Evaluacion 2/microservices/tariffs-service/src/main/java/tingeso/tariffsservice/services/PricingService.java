package tingeso.tariffsservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.tariffsservice.DTO.PricingRequestDto;
import tingeso.tariffsservice.DTO.PricingResponseDto;

import java.math.BigDecimal;

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
        // TODO: ACA FALTAN PARAMETROS DE DESCUENTO
        // TODO: AGREGAR BDAY, AMOUNT OF PEOPLE, DEBE LLAMAR OTRO MICROSERVICIO PARA OBTENER LOS DESCUENTOS
        BigDecimal ivaAmount = baseRate.multiply(IVA);
        BigDecimal totalPrice = baseRate.add(ivaAmount);

        return PricingResponseDto.builder()
            .baseRate(baseRate)
            .groupDiscount(baseRate)
            .frequencyDiscount(baseRate)
            .birthdayDiscount(baseRate)
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

}