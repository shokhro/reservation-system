package uz.shohruh.reservation.reservations;

import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toDomain(ReservationEntity e) {
        return new Reservation(
                e.getId(),
                e.getUserId(),
                e.getRoomId(),
                e.getStartDate(),
                e.getEndDate(),
                e.getStatus()
        );
    }

    public ReservationEntity toEntity(Reservation e) {
        return new ReservationEntity(
                e.id(),
                e.userId(),
                e.roomId(),
                e.startDate(),
                e.endDate(),
                e.status()
        );
    }
}
