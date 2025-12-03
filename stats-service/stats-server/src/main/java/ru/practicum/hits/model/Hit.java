package ru.practicum.hits.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "hits")
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String app; // Название приложения
    private String uri; // Uri
    private String ip; // Ip адрес
    private LocalDateTime timestamp; // Дата и время, когда был совершен запрос

}
