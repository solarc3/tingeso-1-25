package tingeso.trackscheduleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.trackscheduleservice.DTO.ReservaResponseDto;
import tingeso.trackscheduleservice.services.TrackScheduleService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class TrackScheduleController {

    private final TrackScheduleService trackScheduleService;


     //TRACK-SCHEDULE-SERVICE/?startDate=X&endDate=Y
    @GetMapping
    public ResponseEntity<List<ReservaResponseDto>> getWeeklySchedule(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate) {
        OffsetDateTime from = OffsetDateTime.parse(startDate);
        OffsetDateTime to = OffsetDateTime.parse(endDate);

        List<ReservaResponseDto> schedule = trackScheduleService.getReservationsBetweenDates(from, to);
        return ResponseEntity.ok(schedule);
    }

//    @GetMapping("/availability")
//    public ResponseEntity<List<WeeklyScheduleResponse>> getAvailabilityByTimeSlots(
//        @RequestParam("startDate") String startDate,
//        @RequestParam("endDate") String endDate,
//        @RequestParam(value = "timeSlotHours", defaultValue = "1") int timeSlotHours) {
//
//        OffsetDateTime from = OffsetDateTime.parse(startDate);
//        OffsetDateTime to = OffsetDateTime.parse(endDate);
//
//        List<WeeklyScheduleResponse> availability = trackScheduleService.getAvailabilityByTimeSlots(from, to, timeSlotHours);
//        return ResponseEntity.ok(availability);
//    }
}