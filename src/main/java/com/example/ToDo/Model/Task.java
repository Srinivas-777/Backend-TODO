package com.example.ToDo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String operation;

    String task;

    String urgency;

    String dateTime;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    boolean completed = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    User user;
}
