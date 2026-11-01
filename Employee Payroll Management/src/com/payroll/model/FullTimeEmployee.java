package com.payroll.model;

import java.util.Map;

/**
 * Full-time salaried employee with structured allowances and statutory deductions.
 */
public class FullTimeEmployee extends Employee {
    private double monthlySalary;
    private double hraRate = 0.20; // 20% of basic
    private double daRate = 0.10;  // 10% of basic
    private double medicalAllowance = 1500.0;

    public FullTimeEmployee(int id, String name, String email, String department, String designation,
                            String joiningDate, String bankAccount, String panOrTaxId, String status,
                            double monthlySalary) {
        super(id, name, email, department, designation, "FULL_TIME", joiningDate, bankAccount, panOrTaxId, status);
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double getBasePay() {
        return monthlySalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double calculateAllowances(double bonus) {
        double hra = monthlySalary * hraRate;
        double da = monthlySalary * daRate;
        return hra + da + medicalAllowance + Math.max(0, bonus);
    }

    @Override
    public double calculateOvertime(double overtimeHours) {
        if (overtimeHours <= 0) return 0.0;
        // standard 160 hours per month; 1.5x overtime rate
        double hourlyRate = (monthlySalary / 160.0) * 1.5;
        return Math.round(overtimeHours * hourlyRate * 100.0) / 100.0;
    }

    @Override
    public double calculateGross(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        if (workingDays <= 0) workingDays = 22;
        if (daysPresent < 0) daysPresent = workingDays;

        // attendance prorating on basic
        double prorationFactor = Math.min(1.0, (double) daysPresent / (double) workingDays);
        double earnedBasic = monthlySalary * prorationFactor;
        double earnedAllowances = calculateAllowances(bonus);
        double overtime = calculateOvertime(overtimeHours);

        return Math.round((earnedBasic + earnedAllowances + overtime) * 100.0) / 100.0;
    }

    @Override
    public double calculateProvidentFund(double gross) {
        // 12% of basic salary capped at standard guidelines
        return Math.round((monthlySalary * 0.12) * 100.0) / 100.0;
    }

    @Override
    public double calculateTax(double gross) {
        // Monthly annualized tax slab approximation
        double annual = gross * 12.0;
        double annualTax = 0.0;
        if (annual > 1200000) {
            annualTax += (annual - 1200000) * 0.20 + (400000 * 0.15) + (300000 * 0.10);
        } else if (annual > 800000) {
            annualTax += (annual - 800000) * 0.15 + (300000 * 0.10);
        } else if (annual > 500000) {
            annualTax += (annual - 500000) * 0.10;
        }
        return Math.round((annualTax / 12.0) * 100.0) / 100.0;
    }

    @Override
    public double calculateOtherDeductions(int workingDays, int daysPresent) {
        // Professional tax + Insurance
        double profTax = 200.0;
        double healthInsurance = 450.0;
        return profTax + healthInsurance;
    }

    @Override
    public double calculateNet(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        double gross = calculateGross(workingDays, daysPresent, overtimeHours, bonus);
        double pf = calculateProvidentFund(gross);
        double tax = calculateTax(gross);
        double other = calculateOtherDeductions(workingDays, daysPresent);
        double net = gross - (pf + tax + other);
        return Math.max(0.0, Math.round(net * 100.0) / 100.0);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("hra", Math.round(monthlySalary * hraRate * 100.0) / 100.0);
        map.put("da", Math.round(monthlySalary * daRate * 100.0) / 100.0);
        map.put("medicalAllowance", medicalAllowance);
        return map;
    }
}
