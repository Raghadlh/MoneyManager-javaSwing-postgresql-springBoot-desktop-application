package com.raghad.mapviewerbackend.Model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class SavingGoal {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private double targetAmount;
    private LocalDate deadline;


    @Column(nullable = false)
    private double savedAmount = 0.0;



    @ManyToOne
    private MyAppUser user;

    public MyAppUser getUser() {
        return user;
    }

    public void setUser(MyAppUser user) {
        this.user = user;
    }
    public Long getId() {
        return id;
    }


    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getSavedAmount() {
        return savedAmount;
    }
    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

}
