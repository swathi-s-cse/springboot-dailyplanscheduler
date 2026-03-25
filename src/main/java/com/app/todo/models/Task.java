package com.app.todo.models;

//IT IS AN ENTITY CLASS
//used in the application act as a table in DB

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private boolean completed;


    @Column(name = "last_completed_date")
    private LocalDate lastCompletedDate;


    public boolean isCompletedToday() {
        return completed
                && lastCompletedDate != null
                && lastCompletedDate.equals(LocalDate.now());
    }

}
