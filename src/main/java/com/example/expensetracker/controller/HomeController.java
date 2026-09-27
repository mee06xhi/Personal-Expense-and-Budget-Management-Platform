package com.example.expensetracker.controller;

import java.time.YearMonth;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.expensetracker.service.ExpenseService;

@Controller
public class HomeController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/")
    public String home(Model model) {
        YearMonth current = YearMonth.now();
        double total      = expenseService.getTotalForMonth(current);
        double budget     = expenseService.getBudgetForMonth(current);

        model.addAttribute("total",              total);
        model.addAttribute("budget",             budget);
        model.addAttribute("remaining",          budget - total);
        model.addAttribute("recentExpenses",     expenseService.getRecentExpenses(5));
        model.addAttribute("spendingByCategory", expenseService.getSpendingByCategory(current));
        model.addAttribute("dailySpending",      expenseService.getDailySpending(current));
        model.addAttribute("currentMonth",       current.toString());
        model.addAttribute("minMonth",           "2020-01");
        return "index";
    }

    // Dashboard chart + stat data (month-aware)
    @GetMapping("/api/chart-data")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam String month) {
        YearMonth ym     = YearMonth.parse(month);
        double total     = expenseService.getTotalForMonth(ym);
        double budget    = expenseService.getBudgetForMonth(ym);
        double remaining = budget - total;

        return Map.of(
            "spendingByCategory", expenseService.getSpendingByCategory(ym),
            "dailySpending",      expenseService.getDailySpending(ym),
            "total",              total,
            "budget",             budget,
            "remaining",          remaining
        );
    }

    // Budget Status page
    @GetMapping("/budget-status")
    public String budgetStatus(Model model) {
        YearMonth current = YearMonth.now();
        double total      = expenseService.getTotalForMonth(current);
        double budget     = expenseService.getBudgetForMonth(current);

        model.addAttribute("total",              total);
        model.addAttribute("budget",             budget);
        model.addAttribute("remaining",          budget - total);
        model.addAttribute("spendingByCategory", expenseService.getSpendingByCategory(current));
        model.addAttribute("currentMonth",       current.toString());
        return "budget-status";
    }

    // Budget Status chart + stat data (month-aware)
    @GetMapping("/api/budget-data")
    @ResponseBody
    public Map<String, Object> getBudgetData(@RequestParam String month) {
        YearMonth ym     = YearMonth.parse(month);
        double total     = expenseService.getTotalForMonth(ym);
        double budget    = expenseService.getBudgetForMonth(ym);
        double remaining = budget - total;

        return Map.of(
            "spendingByCategory", expenseService.getSpendingByCategory(ym),
            "total",              total,
            "budget",             budget,
            "remaining",          remaining
        );
    }
}