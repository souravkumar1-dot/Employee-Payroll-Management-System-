package com.payroll.model;

import java.util.Map;

/**
 * Manager / Executive subclass with executive allowances and performance incentives.
 */
public class Manager extends FullTimeEmployee {
    private double leadershipAllowance;

    public Manager(int id, String name, String email, String department, String designation,
                   String joiningDate, String bankAccount, String panOrTaxId, String status,
                   double monthlySalary, double leadershipAllowance) {
        super(id, name, email, department, designation, joiningDate, bankAccount, panOrTaxId, status, monthlySalary);
        this.type = "MANAGER";
        this.leadershipAllowance = leadershipAllowance;
    }

    public double getLeadershipAllowance() {
        return leadershipAllowance;
    }

    public void setLeadershipAllowance(double leadershipAllowance) {
        this.leadershipAllowance = leadershipAllowance;
    }

    @Override
    public double calculateAllowances(double bonus) {
        // Base allowances (HRA, DA, Medical) + Leadership Allowance + Bonus
        double baseAllowances = super.calculateAllowances(bonus);
        return baseAllowances + leadershipAllowance;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("leadershipAllowance", leadershipAllowance);
        return map;
    }
}
