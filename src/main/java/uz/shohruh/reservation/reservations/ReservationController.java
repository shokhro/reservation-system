package uz.shohruh.reservation.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id) {
        log.info("Called getReservationById: id=" + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber
    ) {
        ReservationSearchFilter filter =
                new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        log.info("Called getAllReservations");
        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservation
    ) {
        log.info("Called createReservation");
        Reservation savedReservation = reservationService.create(reservation);
        return ResponseEntity.status(201)
                .header("test-header", "123")
                .body(savedReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate) {
        log.info("Called updateReservation id={}, reservationToUpdate={}",
                id, reservationToUpdate);
        Reservation update =
                reservationService.update(id, reservationToUpdate);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("id") Long id) {
        log.info("Called cancelReservation: id={}", id);
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        return ResponseEntity.ok(reservationService.approveReservation(id));
    }
}



















