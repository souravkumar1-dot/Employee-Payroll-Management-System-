package com.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Employee base class representing common attributes and payroll contract.
 */
public abstract class Employee {
    protected int id;
    protected String name;
    protected String email;
    protected String department;
    protected String designation;
    protected String type; // FULL_TIME, PART_TIME, CONTRACTOR, MANAGER
    protected String joiningDate;
    protected String bankAccount;
    protected String panOrTaxId;
    protected String status; // ACTIVE, ON_LEAVE, INACTIVE

    public Employee(int id, String name, String email, String department, String designation,
                    String type, String joiningDate, String bankAccount, String panOrTaxId, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.type = type;
        this.joiningDate = joiningDate;
        this.bankAccount = bankAccount;
        this.panOrTaxId = panOrTaxId;
        this.status = (status == null || status.isEmpty()) ? "ACTIVE" : status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getPanOrTaxId() { return panOrTaxId; }
    public void setPanOrTaxId(String panOrTaxId) { this.panOrTaxId = panOrTaxId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Abstract payroll calculations
    public abstract double getBasePay();
    public abstract double calculateAllowances(double bonus);
    public abstract double calculateOvertime(double overtimeHours);
    public abstract double calculateGross(int workingDays, int daysPresent, double overtimeHours, double bonus);
    public abstract double calculateProvidentFund(double gross);
    public abstract double calculateTax(double gross);
    public abstract double calculateOtherDeductions(int workingDays, int daysPresent);
    public abstract double calculateNet(int workingDays, int daysPresent, double overtimeHours, double bonus);

    /**
     * Serializes employee details to a Map for JSON representation.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        map.put("department", department);
        map.put("designation", designation);
        map.put("type", type);
        map.put("joiningDate", joiningDate);
        map.put("bankAccount", bankAccount);
        map.put("panOrTaxId", panOrTaxId);
        map.put("status", status);
        map.put("basePay", getBasePay());
        return map;
    }
}
