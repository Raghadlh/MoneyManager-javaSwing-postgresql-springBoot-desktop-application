package com.raghad.mapviewerbackend.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MyAppUser user;

    private boolean isSaving;

    private String type;// INCOME or EXPENSE
    private String category;

    private double amount;
    private LocalDate date;
    private String note;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MyAppUser getUser() { return user; }
    public void setUser(MyAppUser user) { this.user = user; }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("saving")
    public boolean isSaving() {
        return isSaving;
    }

    @JsonProperty("saving")
    public void setSaving(boolean saving) {
        isSaving = saving;
    }

}
