package com.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Historical payroll record representing a processed monthly payslip.
 */
public class PayrollRecord {
    private String id; // e.g. "PAY-202610-101"
    private String month; // e.g. "2026-10"
    private int employeeId;
    private String employeeName;
    private String department;
    private String designation;
    private String employeeType;
    private String bankAccount;
    private String panOrTaxId;

    private int workingDays;
    private int presentDays;
    private double overtimeHours;

    private double basicSalary;
    private double allowances;
    private double overtimePay;
    private double bonus;
    private double grossSalary;

    private double taxDeduction;
    private double pfDeduction;
    private double otherDeductions;
    private double totalDeductions;

    private double netSalary;
    private String status; // "PAID", "PROCESSING", "PENDING"
    private String processedDate;
    private String transactionRef;

    public PayrollRecord() {}

    public PayrollRecord(String id, String month, int employeeId, String employeeName,
                         String department, String designation, String employeeType,
                         String bankAccount, String panOrTaxId,
                         int workingDays, int presentDays, double overtimeHours,
                         double basicSalary, double allowances, double overtimePay, double bonus,
                         double grossSalary, double taxDeduction, double pfDeduction,
                         double otherDeductions, double totalDeductions, double netSalary,
                         String status, String processedDate, String transactionRef) {
        this.id = id;
        this.month = month;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
        this.employeeType = employeeType;
        this.bankAccount = bankAccount;
        this.panOrTaxId = panOrTaxId;
        this.workingDays = workingDays;
        this.presentDays = presentDays;
        this.overtimeHours = overtimeHours;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.overtimePay = overtimePay;
        this.bonus = bonus;
        this.grossSalary = grossSalary;
        this.taxDeduction = taxDeduction;
        this.pfDeduction = pfDeduction;
        this.otherDeductions = otherDeductions;
        this.totalDeductions = totalDeductions;
        this.netSalary = netSalary;
        this.status = status;
        this.processedDate = processedDate;
        this.transactionRef = transactionRef;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getPanOrTaxId() { return panOrTaxId; }
    public void setPanOrTaxId(String panOrTaxId) { this.panOrTaxId = panOrTaxId; }

    public int getWorkingDays() { return workingDays; }
    public void setWorkingDays(int workingDays) { this.workingDays = workingDays; }

    public int getPresentDays() { return presentDays; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public double getAllowances() { return allowances; }
    public void setAllowances(double allowances) { this.allowances = allowances; }

    public double getOvertimePay() { return overtimePay; }
    public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }

    public double getBonus() { return bonus; }
    public void setBonus(double bonus) { this.bonus = bonus; }

    public double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(double grossSalary) { this.grossSalary = grossSalary; }

    public double getTaxDeduction() { return taxDeduction; }
    public void setTaxDeduction(double taxDeduction) { this.taxDeduction = taxDeduction; }

    public double getPfDeduction() { return pfDeduction; }
    public void setPfDeduction(double pfDeduction) { this.pfDeduction = pfDeduction; }

    public double getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(double otherDeductions) { this.otherDeductions = otherDeductions; }

    public double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProcessedDate() { return processedDate; }
    public void setProcessedDate(String processedDate) { this.processedDate = processedDate; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("month", month);
        map.put("employeeId", employeeId);
        map.put("employeeName", employeeName);
        map.put("department", department);
        map.put("designation", designation);
        map.put("employeeType", employeeType);
        map.put("bankAccount", bankAccount);
        map.put("panOrTaxId", panOrTaxId);
        map.put("workingDays", workingDays);
        map.put("presentDays", presentDays);
        map.put("overtimeHours", overtimeHours);
        map.put("basicSalary", basicSalary);
        map.put("allowances", allowances);
        map.put("overtimePay", overtimePay);
        map.put("bonus", bonus);
        map.put("grossSalary", grossSalary);
        map.put("taxDeduction", taxDeduction);
        map.put("pfDeduction", pfDeduction);
        map.put("otherDeductions", otherDeductions);
        map.put("totalDeductions", totalDeductions);
        map.put("netSalary", netSalary);
        map.put("status", status);
        map.put("processedDate", processedDate);
        map.put("transactionRef", transactionRef);
        return map;
    }
}
