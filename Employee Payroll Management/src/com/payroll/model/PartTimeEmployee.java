package com.payroll.model;

import java.util.Map;

/**
 * Part-time / Hourly employee calculated on hourly wage rate.
 */
public class PartTimeEmployee extends Employee {
    private double hourlyRate;
    private int standardHoursPerMonth = 80;

    public PartTimeEmployee(int id, String name, String email, String department, String designation,
                            String joiningDate, String bankAccount, String panOrTaxId, String status,
                            double hourlyRate) {
        super(id, name, email, department, designation, "PART_TIME", joiningDate, bankAccount, panOrTaxId, status);
        this.hourlyRate = hourlyRate;
    }

    @Override
    public double getBasePay() {
        return hourlyRate * standardHoursPerMonth;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public double calculateAllowances(double bonus) {
        return Math.max(0, bonus);
    }

    @Override
    public double calculateOvertime(double overtimeHours) {
        if (overtimeHours <= 0) return 0.0;
        return Math.round(overtimeHours * hourlyRate * 1.25 * 100.0) / 100.0;
    }

    @Override
    public double calculateGross(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        if (workingDays <= 0) workingDays = 22;
        int safeDays = daysPresent < 0 ? workingDays : daysPresent;
        // Assume 4 hours/day standard part-time shift
        double billableHours = (double) safeDays * 4.0;
        double baseEarnings = billableHours * hourlyRate;
        double overtime = calculateOvertime(overtimeHours);
        return Math.round((baseEarnings + overtime + Math.max(0, bonus)) * 100.0) / 100.0;
    }

    @Override
    public double calculateProvidentFund(double gross) {
        // Part time is exempt or low rate
        return 0.0;
    }

    @Override
    public double calculateTax(double gross) {
        // Flat 5% TDS for hourly part-time if threshold exceeded
        if (gross > 25000) {
            return Math.round(gross * 0.05 * 100.0) / 100.0;
        }
        return 0.0;
    }

    @Override
    public double calculateOtherDeductions(int workingDays, int daysPresent) {
        return 100.0; // Minimal admin fee/welfare
    }

    @Override
    public double calculateNet(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        double gross = calculateGross(workingDays, daysPresent, overtimeHours, bonus);
        double tax = calculateTax(gross);
        double other = calculateOtherDeductions(workingDays, daysPresent);
        return Math.max(0.0, Math.round((gross - (tax + other)) * 100.0) / 100.0);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("hourlyRate", hourlyRate);
        map.put("standardHours", standardHoursPerMonth);
        return map;
    }
}
