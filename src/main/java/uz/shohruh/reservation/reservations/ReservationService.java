package uz.shohruh.reservation.reservations;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;;

@Service
public class ReservationService {

    private final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final ReservationMapper mapper;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Reservation getById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(
                        "Not found reservations by id " + id
                ));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(ReservationSearchFilter filter) {

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        Pageable pageable = Pageable.ofSize(pageSize)
                                    .withPage(pageNumber);

        List<ReservationEntity> allEntities =
                repository.searchAllByFilter(filter.roomId(), filter.userId(), pageable);

        return allEntities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Reservation create(Reservation reservation) {
        if (reservation.status() != null) {
            throw new IllegalArgumentException(("Status should be empty"));
        }

        ReservationEntity savedReservation = mapper.toEntity(reservation);
        savedReservation.setStatus(ReservationStatus.PENDING);

        ReservationEntity save = repository.save(savedReservation);
        log.info("hammasi yaxshi bo'ladi={}", save.toString());
        return mapper.toDomain(save);
    }

    public Reservation update(Long id, Reservation reservationToUpdate) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservations by id = " + reservationToUpdate.id()
                ));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservations: status="
                    + reservationToUpdate.status());
        }

        ReservationEntity reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(reservationEntity.getStatus());

        ReservationEntity updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        ReservationEntity reservation = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservations by id = " + id));

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
           throw new IllegalStateException("Cannot cancel the reservations. Reservation was already cancelled");
        }
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservations. Contact with manage please");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservations: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservations by id = " + id
                ));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservations: status="
                    + reservationEntity.getStatus());
        }

        boolean isConflict = isReservationConflict(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );
        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservations because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

    private boolean isReservationConflict(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Long> conflictingIds = repository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );
        if (conflictingIds.isEmpty()) {
            return false;
        }
        log.info("Conflicting with ids={}", conflictingIds);
        return true;
    }

}















