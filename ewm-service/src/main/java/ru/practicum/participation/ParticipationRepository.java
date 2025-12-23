package ru.practicum.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.event.Event;
import ru.practicum.state.ParticipationStatus;
import ru.practicum.user.User;

import java.util.List;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.event.id = :eventId AND p.status = :status")
    long countParticipationByEventAndStatus(@Param("eventId") Long eventId,
                                            @Param("status") ParticipationStatus status);

    List<Participation> findAllByRequester(User requester);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Participation> findAllByEvent(Event event);

}
