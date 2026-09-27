package com.example.expensetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public void addExpense(double amount, String category, String description, LocalDate date) {
        expenseRepository.save(new Expense(amount, category, description, date));
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getFilteredExpenses(List<String> categories, String fromDate, String toDate) {
        return expenseRepository.findAll().stream()
            .filter(e -> categories == null || categories.isEmpty() || categories.contains(e.getCategory()))
            .filter(e -> fromDate == null || fromDate.isEmpty() || !e.getDate().isBefore(LocalDate.parse(fromDate)))
            .filter(e -> toDate == null || toDate.isEmpty() || !e.getDate().isAfter(LocalDate.parse(toDate)))
            .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .collect(Collectors.toList());
    }

    public List<Expense> getRecentExpenses(int n) {
        return expenseRepository.findAll().stream()
            .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .limit(n)
            .collect(Collectors.toList());
    }

    // Total for the current month (used by sidebar + dashboard default)
    public double getTotal() {
        return getTotalForMonth(YearMonth.now());
    }

    // Total for a specific month
    public double getTotalForMonth(YearMonth ym) {
        return expenseRepository.findAll().stream()
            .filter(e -> YearMonth.from(e.getDate()).equals(ym))
            .mapToDouble(Expense::getAmount)
            .sum();
    }

    // Save or update budget for a specific month
    public void setBudget(String month, double amount) {
        Budget budget = budgetRepository.findByMonth(month).orElse(new Budget(month, 0));
        budget.setAmount(amount);
        budgetRepository.save(budget);
    }

    // Get budget for a specific month (returns 0 if not set)
    public double getBudgetForMonth(YearMonth ym) {
        return budgetRepository.findByMonth(ym.toString())
                .map(Budget::getAmount)
                .orElse(0.0);
    }

    // Get budget for current month — used by sidebar
    public double getBudget() {
        return getBudgetForMonth(YearMonth.now());
    }

    // Remaining for current month — used by sidebar
    public double getRemaining() {
        return getBudget() - getTotal();
    }

    // Remaining for a specific month
    public double getRemainingForMonth(YearMonth ym) {
        return getBudgetForMonth(ym) - getTotalForMonth(ym);
    }

    // Spending by category for a specific month (donut + bar charts)
    public Map<String, Double> getSpendingByCategory(YearMonth ym) {
        return expenseRepository.findAll().stream()
            .filter(e -> YearMonth.from(e.getDate()).equals(ym))
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.summingDouble(Expense::getAmount)
            ));
    }

    // Daily spending for a specific month (line chart)
    public Map<String, Double> getDailySpending(YearMonth ym) {
        return expenseRepository.findAll().stream()
            .filter(e -> YearMonth.from(e.getDate()).equals(ym))
            .collect(Collectors.groupingBy(
                e -> e.getDate().toString(),
                java.util.TreeMap::new,
                Collectors.summingDouble(Expense::getAmount)
            ));
    }

    public Optional<Expense> getById(int id) {
        return expenseRepository.findById(id);
    }

    public void updateExpense(int id, double amount, String category,
                              String description, LocalDate date) {
        expenseRepository.findById(id).ifPresent(expense -> {
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDescription(description);
            expense.setDate(date);
            expenseRepository.save(expense);
        });
    }

    public void deleteExpense(int id) {
        expenseRepository.deleteById(id);
    }
}