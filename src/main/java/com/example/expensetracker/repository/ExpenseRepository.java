package com.example.expensetracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.expensetracker.model.Expense;

@Repository   
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByCategory(String category);
}

// standard database operations — save(), findAll(), findById(), deleteById()