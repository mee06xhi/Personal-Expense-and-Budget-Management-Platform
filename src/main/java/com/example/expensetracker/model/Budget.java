package com.example.expensetracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "budget")
public class Budget {

    @Id
    private String month; // e.g. "2026-04" — one row per month

    private double amount;

    public Budget() {}

    public Budget(String month, double amount) {
        this.month  = month;
        this.amount = amount;
    }

    public String getMonth()           { return month; }
    public double getAmount()          { return amount; }
    public void setAmount(double amt)  { this.amount = amt; }
}