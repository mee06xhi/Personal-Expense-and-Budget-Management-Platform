package com.example.expensetracker.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity   //class should be mapped to a database table
@Table(name = "expenses")
public class Expense {

    @Id  //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //auto-increment id
    private int id;
    private double amount;
    private String category;
    private String description;
    private LocalDate date;

    public Expense() {}     // default constructor

    public Expense(double amount, String category, String description, LocalDate date) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    // Getters
    public int getId()             { return id; }
    public double getAmount()      { return amount; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate()     { return date; }

    // Setters
    public void setAmount(double amount)       { this.amount = amount; }
    public void setCategory(String category)   { this.category = category; }
    public void setDescription(String desc)    { this.description = desc; }
    public void setDate(LocalDate date)        { this.date = date; }
}
