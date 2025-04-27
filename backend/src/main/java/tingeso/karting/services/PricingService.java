package tingeso.karting.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tingeso.karting.DTO.PricingRequestDto;
import tingeso.karting.DTO.PricingResponseDto;

import java.math.BigDecimal;

@Service
public class PricingService {
    private static final BigDecimal VUELTAS_10_PRECIO = BigDecimal.valueOf(15000);
    private static final BigDecimal VUELTAS_15_PRECIO = BigDecimal.valueOf(20000);
    private static final BigDecimal VUELTAS_20_PRECIO = BigDecimal.valueOf(25000);
    private static final BigDecimal IVA = BigDecimal.valueOf(0.19);

    public PricingResponseDto calculatePrice(PricingRequestDto request){

        BigDecimal baseRate = determineBaseRate(request);

        BigDecimal groupDiscount = baseRate.multiply(BigDecimal.valueOf(groupDiscountRate(request.getNumPeople())));

        BigDecimal freqDiscount = baseRate.multiply(BigDecimal.valueOf(frequencyDiscountRate(request.getMonthlyVisits())));

        BigDecimal birthdayDiscount = BigDecimal.ZERO;
        if(request.isBirthday()){
            birthdayDiscount = baseRate.multiply(BigDecimal.valueOf(birthdayDiscountRate(request.getNumPeople())));
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
        // Si esta definido vueltas, se prioriza
        if (request.getLaps() != null) {
            switch (request.getLaps()) {
                case 10: return VUELTAS_10_PRECIO;
                case 15: return VUELTAS_15_PRECIO;
                case 20: return VUELTAS_20_PRECIO;
                default:   throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Número de vueltas inválido: " + request.getLaps()
                );
            }
        }
        int duration = request.getDuration();
        if (duration <= 30) return VUELTAS_10_PRECIO;
        if (duration <= 35) return VUELTAS_15_PRECIO;
        if (duration <= 40) return VUELTAS_20_PRECIO;
        throw new IllegalArgumentException("duracion no soportada: " + duration);
    }
    private double groupDiscountRate(int numPeople) {
        if (numPeople <= 2) return 0.0;
        if (numPeople <= 5) return 0.10;
        if (numPeople <= 10) return 0.20;
        return 0.30;
    }
    private double frequencyDiscountRate(int visitsPerMonth) {
        if (visitsPerMonth >= 7) return 0.30;
        if (visitsPerMonth >= 5) return 0.20;
        if (visitsPerMonth >= 2) return 0.10;
        return 0.0;
    }
    private double birthdayDiscountRate(int numPeople) {
        if (numPeople >= 11) return 0.50 * 2 / numPeople;
        if (numPeople >= 3)  return 0.50 * 1 / numPeople;
        return 0.0;
    }
}
