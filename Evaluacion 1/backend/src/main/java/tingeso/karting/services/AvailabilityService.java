package tingeso.karting.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tingeso.karting.DTO.AvailabilityRequestDto;
import tingeso.karting.DTO.AvailabilityResponseDto;
import tingeso.karting.DTO.ConflictDto;
import tingeso.karting.entities.ReservaEntity;
import tingeso.karting.entities.ReservaStatus;
import tingeso.karting.repositories.ReservaRepository;

import java.beans.Transient;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final ReservaRepository reservaRepository;
    private final KartService kartService;


    private final Map<String, NavigableSet<Interval>> calendar = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAfterStartup() {
        System.out.println("=== Inicializando servicio de disponibilidad ===");

        calendar.clear();

        kartService.getAllKartIds().forEach(kartId -> {
            System.out.println("Registrando kart ID: " + kartId);
            calendar.put(kartId, new ConcurrentSkipListSet<>(
                Comparator.comparing(Interval::getStart)
                    .thenComparing(Interval::getEnd)
            ));
        });

        initCalendar();
    }

    private void initCalendar() {
        try {
            long totalReservas = reservaRepository.count();
            System.out.println("Total de reservas en la base de datos: " + totalReservas);

            List<ReservaEntity> activeReservations = new ArrayList<>();
            activeReservations.addAll(reservaRepository.findByStatus(ReservaStatus.CONFIRMED));
            activeReservations.addAll(reservaRepository.findByStatus(ReservaStatus.PENDING));

            System.out.println("Cargando " + activeReservations.size() + " reservas activas");

            for (ReservaEntity reservation : activeReservations) {
                if (reservation.getKartIds() != null && !reservation.getKartIds().isEmpty()) {
                    for (String kartId : reservation.getKartIds()) {
                        NavigableSet<Interval> intervals = calendar.get(kartId);
                        if (intervals != null) {
                            Interval interval = new Interval(reservation.getStartTime(), reservation.getEndTime());
                            boolean added = intervals.add(interval);
                            System.out.println("Kart " + kartId + " - Intervalo agregado: " + added +
                                               " (" + reservation.getStartTime() + " - " + reservation.getEndTime() + ")");
                        } else {
                            System.out.println("ERROR: Kart no encontrado: " + kartId);
                        }
                    }
                }
            }

            // Verificación final
            verifyCalendarState();

        } catch (Exception e) {
            System.err.println("ERROR al cargar reservas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void verifyCalendarState() {
        System.out.println("\n=== Verificando estado del calendario ===");
        int totalIntervals = 0;
        for (String kartId : calendar.keySet()) {
            NavigableSet<Interval> intervals = calendar.get(kartId);
            totalIntervals += intervals.size();
            System.out.println("Kart " + kartId + ": " + intervals.size() + " reservas");
            if (!intervals.isEmpty()) {
                System.out.println("  Intervalos: " + intervals);
            }
        }
        System.out.println("Total de intervalos registrados: " + totalIntervals);
        System.out.println("=======================================\n");
    }

    public List<String> getFreeKarts(OffsetDateTime start, OffsetDateTime end) {
        System.out.println("Getting free karts for period:");
        System.out.println("Start: " + start);
        System.out.println("End: " + end);

        List<String> freeKarts = calendar.entrySet().stream()
            .filter(e -> {
                boolean isFree = isKartFree(e.getValue(), start, end);
                System.out.println("Kart " + e.getKey() + " is free: " + isFree);
                return isFree;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        System.out.println("Free karts: " + freeKarts);
        return freeKarts;
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
        System.out.println("Checking " + intervals.size() + " intervals for availability");
        for (Interval interval : intervals) {
            if (interval.overlaps(start, end)) {
                System.out.println("  - Conflicto encontrado con intervalo: " +
                                   interval.getStart() + " hasta " + interval.getEnd());
                return false;
            }
        }

        Interval query = new Interval(start, end);

        Interval floor = intervals.floor(query);
        if (floor != null && floor.overlaps(start, end)) {
            return false;
        }

        Interval ceiling = intervals.ceiling(query);
        if (ceiling != null && ceiling.overlaps(start, end)) {
            return false;
        }

        return true;
    }

    public void registerKartReservation(String kartId, OffsetDateTime start, OffsetDateTime end) {
        calendar.get(kartId).add(new Interval(start, end));
    }

    public void registerReservation(ReservaEntity res) {
        if (res.getKartIds() != null && !res.getKartIds().isEmpty()) {
            System.out.println("Registrando reserva ID: " + res.getId() + " con " + res.getKartIds().size() + " karts");
            for (String kartId : res.getKartIds()) {
                NavigableSet<Interval> intervals = calendar.get(kartId);
                if (intervals != null) {
                    System.out.println("  - Agregando intervalo para kart " + kartId);
                    intervals.add(new Interval(res.getStartTime(), res.getEndTime()));
                } else {
                    System.out.println("  - ERROR: Kart ID no encontrado: " + kartId);
                }
            }
        } else {
            System.out.println("No se pueden registrar karts para reserva ID: " + res.getId() + " - no hay karts asignados");
        }
    }

    @AllArgsConstructor
    private static class Interval implements Comparable<Interval> {
        private OffsetDateTime start;
        private OffsetDateTime end;

        public OffsetDateTime getStart() {
            return start;
        }

        public OffsetDateTime getEnd() {
            return end;
        }

        public boolean overlaps(OffsetDateTime s, OffsetDateTime e) {
            return !start.isAfter(e) && !end.isBefore(s);
        }

        @Override
        public int compareTo(Interval other) {
            int startCompare = start.compareTo(other.start);
            return startCompare != 0 ? startCompare : end.compareTo(other.end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Interval interval = (Interval) o;
            return Objects.equals(start, interval.start) && Objects.equals(end, interval.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "Interval[" + start + " -> " + end + "]";
        }
    }
}