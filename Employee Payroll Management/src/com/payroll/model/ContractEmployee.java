package com.payroll.model;

import java.util.Map;

/**
 * Contractor / Consultant with fixed retainer and statutory professional TDS withholding.
 */
public class ContractEmployee extends Employee {
    private double contractFee;
    private double tdsRate = 0.10; // 10% standard withholding under section 194J

    public ContractEmployee(int id, String name, String email, String department, String designation,
                            String joiningDate, String bankAccount, String panOrTaxId, String status,
                            double contractFee) {
        super(id, name, email, department, designation, "CONTRACTOR", joiningDate, bankAccount, panOrTaxId, status);
        this.contractFee = contractFee;
    }

    @Override
    public double getBasePay() {
        return contractFee;
    }

    public void setContractFee(double fee) {
        this.contractFee = fee;
    }

    @Override
    public double calculateAllowances(double bonus) {
        return Math.max(0, bonus);
    }

    @Override
    public double calculateOvertime(double overtimeHours) {
        // Contractors generally have flat deliverables rate; additional milestone stipend
        return Math.round(overtimeHours * (contractFee / 160.0) * 100.0) / 100.0;
    }

    @Override
    public double calculateGross(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        if (workingDays <= 0) workingDays = 22;
        int safeDays = daysPresent < 0 ? workingDays : daysPresent;
        double factor = (double) safeDays / (double) workingDays;
        double base = contractFee * factor;
        double extra = calculateOvertime(overtimeHours) + Math.max(0, bonus);
        return Math.round((base + extra) * 100.0) / 100.0;
    }

    @Override
    public double calculateProvidentFund(double gross) {
        return 0.0; // No PF for external contractors
    }

    @Override
    public double calculateTax(double gross) {
        return Math.round(gross * tdsRate * 100.0) / 100.0;
    }

    @Override
    public double calculateOtherDeductions(int workingDays, int daysPresent) {
        return 0.0;
    }

    @Override
    public double calculateNet(int workingDays, int daysPresent, double overtimeHours, double bonus) {
        double gross = calculateGross(workingDays, daysPresent, overtimeHours, bonus);
        double tax = calculateTax(gross);
        return Math.max(0.0, Math.round((gross - tax) * 100.0) / 100.0);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("contractFee", contractFee);
        map.put("tdsRate", tdsRate);
        return map;
    }
}
