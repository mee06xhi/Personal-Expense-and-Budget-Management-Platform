package com.example.expensetracker.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;

@Controller
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/add-expense")
    public String showAddExpense() {
        return "add-expense";
    }

    @PostMapping("/add-expense")
    public String addExpense(@RequestParam double amount,
                             @RequestParam String category,
                             @RequestParam String description,
                             @RequestParam String date) {
        expenseService.addExpense(amount, category, description, LocalDate.parse(date));
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String viewExpenses(@RequestParam(required = false) List<String> category,
                               @RequestParam(required = false) String fromDate,
                               @RequestParam(required = false) String toDate,
                               Model model) {
        List<Expense> filtered = expenseService.getFilteredExpenses(category, fromDate, toDate);
        model.addAttribute("expenses", filtered);
        model.addAttribute("selectedCategories", category);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "expenses";
    }

    // Show Set Budget form — receives the month from the Budget Status page
    @GetMapping("/budget")
    public String showBudget(@RequestParam(required = false) String month, Model model) {
        YearMonth ym = (month != null && !month.isEmpty())
                ? YearMonth.parse(month)
                : YearMonth.now();

        model.addAttribute("selectedMonth", ym.toString());
        model.addAttribute("currentBudget", expenseService.getBudgetForMonth(ym));

        // Human-readable label e.g. "April 2026"
        java.time.format.DateTimeFormatter fmt =
            java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy");
        model.addAttribute("monthLabel", ym.atDay(1).format(fmt));

        return "budget";
    }

    // Save budget for the specific month passed in the form
    @PostMapping("/budget")
    public String setBudget(@RequestParam double budgetAmount,
                            @RequestParam String month) {
        expenseService.setBudget(month, budgetAmount);
        return "redirect:/budget-status";
    }

    @GetMapping("/edit-expense/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Optional<Expense> expense = expenseService.getById(id);
        if (expense.isEmpty()) return "redirect:/expenses";
        model.addAttribute("expense", expense.get());
        return "edit-expense";
    }

    @PostMapping("/edit-expense/{id}")
    public String updateExpense(@PathVariable int id,
                                @RequestParam double amount,
                                @RequestParam String category,
                                @RequestParam String description,
                                @RequestParam String date) {
        expenseService.updateExpense(id, amount, category, description, LocalDate.parse(date));
        return "redirect:/expenses";
    }

    @PostMapping("/delete-expense/{id}")
    public String deleteExpense(@PathVariable int id) {
        expenseService.deleteExpense(id);
        return "redirect:/expenses";
    }
}