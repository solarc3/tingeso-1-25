package tingeso.karting.services;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.stereotype.Service;
import tingeso.karting.DTO.AvailabilityRequestDto;
import tingeso.karting.DTO.AvailabilityResponseDto;
import tingeso.karting.DTO.ConflictDto;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final ReservaRepository reservaRepository;
    private final KartService kartService;

    private final Map<String, NavigableSet<Interval>> calendar = new ConcurrentHashMap<>();
    @PostConstruct
    public void init(){
        //iniciar calendario con todos los karts ids para ir viendo disponibilidad y generar servicios
        // lista vacias con comparador, log n time date
        kartService.getAllKartIds().forEach(kartId ->
                                                calendar.put(kartId, new ConcurrentSkipListSet<>(Comparator.comparing(Interval::getStart)))
                                           );
        reservaRepository.findByStatus(ReservaStatus.CONFIRMED)
            .forEach(this::registerReservation);

    }



    public List<String> getFreeKarts(OffsetDateTime start, OffsetDateTime end) {
        return calendar.entrySet().stream()
            .filter(e -> isKartFree(e.getValue(), start, end))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public AvailabilityResponseDto checkAvail(AvailabilityRequestDto req) {
        OffsetDateTime start = req.getStartTime();
        OffsetDateTime end = req.getEndTime();

        List<String> freeKarts = getFreeKarts(start, end);
        AvailabilityResponseDto resp = new AvailabilityResponseDto();
        boolean available = freeKarts.size() >= req.getNumKarts();
        resp.setOk(available);
        if (!available) {
            List<ConflictDto> conflicts = calendar.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                             .filter(i -> i.overlaps(start, end))
                             .map(i -> new ConflictDto(e.getKey(), i.getStart(), i.getEnd()))
                        )
                .collect(Collectors.toList());
            resp.setConflicts(conflicts);
        }
        return resp;
    }



    public void removeReservation(ReservaEntity res) {
        if (res.getKartIds() != null) {
            for (String kartId : res.getKartIds()) {
                calendar.get(kartId)
                    .remove(new Interval(res.getStartTime(), res.getEndTime()));
            }
        }
    }
    private boolean isKartFree(NavigableSet<Interval> intervals, OffsetDateTime start, OffsetDateTime end) {
        Interval query = new Interval(start, end);
        Interval floor = intervals.floor(query);
        if (floor != null && floor.overlaps(start, end)) return false;
        Interval ceiling = intervals.ceiling(query);
        if (ceiling != null && ceiling.overlaps(start, end)) return false;
        return true;
    }

    public void registerKartReservation(String kartId, OffsetDateTime start, OffsetDateTime end) {
        calendar.get(kartId).add(new Interval(start, end));
    }

    public void registerReservation(ReservaEntity res) {
        if (res.getKartIds() != null) {
            for (String kartId : res.getKartIds()) {
                calendar.get(kartId).add(new Interval(res.getStartTime(), res.getEndTime()));
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class Interval {
        private OffsetDateTime start;
        private OffsetDateTime end;

        public boolean overlaps(OffsetDateTime s, OffsetDateTime e) {
            return !start.isAfter(e) && !end.isBefore(s);
        }
    }
}
