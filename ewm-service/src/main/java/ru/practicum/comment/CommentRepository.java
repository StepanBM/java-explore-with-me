package ru.practicum.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.Event;
import ru.practicum.user.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByEventAndAuthor(Event event, User user);

    Page<Comment> findByEventIdOrderByCreatedDesc(Long eventId, Pageable pageable);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}
