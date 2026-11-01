package com.payroll.service;

import com.payroll.model.*;
import com.payroll.util.SimpleJson;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing employee records, payroll execution, data persistence, and analytics.
 */
public class PayrollService {
    private static final String DATA_FILE = "data/payroll_db.json";
    private final Map<Integer, Employee> employees = new ConcurrentHashMap<>();
    private final Map<String, PayrollRecord> payrollRecords = new ConcurrentHashMap<>();
    private final AtomicInteger nextEmployeeId = new AtomicInteger(101);

    public PayrollService() {
        loadData();
        if (employees.isEmpty()) {
            seedSampleData();
            saveData();
        }
    }

    private void seedSampleData() {
        // Seed diverse real-world workforce
        FullTimeEmployee e1 = new FullTimeEmployee(
                101, "Aarav Sharma", "aarav.s@payrollpulse.com", "Engineering",
                "Senior Software Engineer", "2023-01-15", "HDFC0001234", "AAAPS1234K", "ACTIVE", 75000.0
        );

        Manager e2 = new Manager(
                102, "Priya Nair", "priya.n@payrollpulse.com", "Human Resources",
                "HR Director", "2021-06-10", "ICIC0005678", "BBYPN5678L", "ACTIVE", 95000.0, 15000.0
        );

        PartTimeEmployee e3 = new PartTimeEmployee(
                103, "Rohan Verma", "rohan.v@payrollpulse.com", "Design",
                "UI/UX Consultant", "2024-03-01", "SBIN0009988", "CCRPV9988M", "ACTIVE", 600.0
        );

        ContractEmployee e4 = new ContractEmployee(
                104, "Elena Vance", "elena.v@payrollpulse.com", "Marketing",
                "Content Strategist", "2024-05-12", "CITI0004321", "DDEEV4321N", "ACTIVE", 45000.0
        );

        FullTimeEmployee e5 = new FullTimeEmployee(
                105, "Vikram Malhotra", "vikram.m@payrollpulse.com", "Finance",
                "Financial Analyst", "2022-11-20", "AXIS0008765", "FFVVM8765P", "ACTIVE", 62000.0
        );

        employees.put(e1.getId(), e1);
        employees.put(e2.getId(), e2);
        employees.put(e3.getId(), e3);
        employees.put(e4.getId(), e4);
        employees.put(e5.getId(), e5);
        nextEmployeeId.set(106);

        // Pre-generate payroll for previous and current month
        processPayroll("2026-09", 22, Collections.emptyMap());
        processPayroll("2026-10", 22, Collections.emptyMap());

        // Mark previous month as paid
        for (PayrollRecord rec : payrollRecords.values()) {
            if ("2026-09".equals(rec.getMonth())) {
                rec.setStatus("PAID");
            }
        }
    }

    public synchronized List<Map<String, Object>> getAllEmployees() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Employee e : employees.values()) {
            list.add(e.toMap());
        }
        list.sort(Comparator.comparingInt(a -> (int) a.get("id")));
        return list;
    }

    public synchronized Employee getEmployee(int id) {
        return employees.get(id);
    }

    public synchronized Employee addEmployee(Map<String, Object> data) {
        int id = nextEmployeeId.getAndIncrement();
        String name = String.valueOf(data.getOrDefault("name", "New Employee"));
        String email = String.valueOf(data.getOrDefault("email", ""));
        String department = String.valueOf(data.getOrDefault("department", "General"));
        String designation = String.valueOf(data.getOrDefault("designation", "Staff"));
        String type = String.valueOf(data.getOrDefault("type", "FULL_TIME")).toUpperCase();
        String joiningDate = String.valueOf(data.getOrDefault("joiningDate", LocalDate.now().toString()));
        String bankAccount = String.valueOf(data.getOrDefault("bankAccount", "BANK000" + id));
        String panOrTaxId = String.valueOf(data.getOrDefault("panOrTaxId", "TAX" + id));
        String status = String.valueOf(data.getOrDefault("status", "ACTIVE"));

        double basePay = toDouble(data.get("basePay"), 40000.0);

        Employee emp;
        if ("PART_TIME".equals(type)) {
            emp = new PartTimeEmployee(id, name, email, department, designation, joiningDate, bankAccount, panOrTaxId, status, basePay);
        } else if ("CONTRACTOR".equals(type)) {
            emp = new ContractEmployee(id, name, email, department, designation, joiningDate, bankAccount, panOrTaxId, status, basePay);
        } else if ("MANAGER".equals(type)) {
            double leadershipAllowance = toDouble(data.get("leadershipAllowance"), 10000.0);
            emp = new Manager(id, name, email, department, designation, joiningDate, bankAccount, panOrTaxId, status, basePay, leadershipAllowance);
        } else {
            emp = new FullTimeEmployee(id, name, email, department, designation, joiningDate, bankAccount, panOrTaxId, status, basePay);
        }

        employees.put(id, emp);
        saveData();
        return emp;
    }

    public synchronized Employee updateEmployee(int id, Map<String, Object> data) {
        Employee existing = employees.get(id);
        if (existing == null) return null;

        if (data.containsKey("name")) existing.setName(String.valueOf(data.get("name")));
        if (data.containsKey("email")) existing.setEmail(String.valueOf(data.get("email")));
        if (data.containsKey("department")) existing.setDepartment(String.valueOf(data.get("department")));
        if (data.containsKey("designation")) existing.setDesignation(String.valueOf(data.get("designation")));
        if (data.containsKey("status")) existing.setStatus(String.valueOf(data.get("status")));
        if (data.containsKey("bankAccount")) existing.setBankAccount(String.valueOf(data.get("bankAccount")));
        if (data.containsKey("panOrTaxId")) existing.setPanOrTaxId(String.valueOf(data.get("panOrTaxId")));

        if (data.containsKey("basePay")) {
            double bp = toDouble(data.get("basePay"), 0.0);
            if (existing instanceof FullTimeEmployee) {
                ((FullTimeEmployee) existing).setMonthlySalary(bp);
            } else if (existing instanceof PartTimeEmployee) {
                ((PartTimeEmployee) existing).setHourlyRate(bp);
            } else if (existing instanceof ContractEmployee) {
                ((ContractEmployee) existing).setContractFee(bp);
            }
        }
        if (existing instanceof Manager && data.containsKey("leadershipAllowance")) {
            ((Manager) existing).setLeadershipAllowance(toDouble(data.get("leadershipAllowance"), 10000.0));
        }

        saveData();
        return existing;
    }

    public synchronized boolean deleteEmployee(int id) {
        Employee removed = employees.remove(id);
        if (removed != null) {
            saveData();
            return true;
        }
        return false;
    }

    public synchronized List<Map<String, Object>> getPayrollRecords(String month) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PayrollRecord rec : payrollRecords.values()) {
            if (month == null || month.isEmpty() || month.equals("ALL") || rec.getMonth().equalsIgnoreCase(month)) {
                list.add(rec.toMap());
            }
        }
        list.sort((a, b) -> String.valueOf(b.get("month")).compareTo(String.valueOf(a.get("month"))));
        return list;
    }

    public synchronized List<PayrollRecord> processPayroll(String month, int workingDays, Map<String, Object> overrides) {
        if (workingDays <= 0) workingDays = 22;
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<PayrollRecord> newlyProcessed = new ArrayList<>();
        String todayStr = LocalDate.now().toString();

        for (Employee emp : employees.values()) {
            if ("INACTIVE".equalsIgnoreCase(emp.getStatus()) || "TERMINATED".equalsIgnoreCase(emp.getStatus())) {
                continue;
            }

            int presentDays = workingDays;
            double overtimeHours = 0.0;
            double bonus = 0.0;

            String empKey = String.valueOf(emp.getId());
            if (overrides != null && overrides.containsKey(empKey)) {
                Object o = overrides.get(empKey);
                if (o instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) o;
                    if (map.containsKey("presentDays")) presentDays = toInt(map.get("presentDays"), workingDays);
                    if (map.containsKey("overtimeHours")) overtimeHours = toDouble(map.get("overtimeHours"), 0.0);
                    if (map.containsKey("bonus")) bonus = toDouble(map.get("bonus"), 0.0);
                }
            }

            double basic = emp.getBasePay();
            double allowances = emp.calculateAllowances(bonus);
            double overtimePay = emp.calculateOvertime(overtimeHours);
            double gross = emp.calculateGross(workingDays, presentDays, overtimeHours, bonus);

            double tax = emp.calculateTax(gross);
            double pf = emp.calculateProvidentFund(gross);
            double other = emp.calculateOtherDeductions(workingDays, presentDays);
            double totalDeductions = Math.round((tax + pf + other) * 100.0) / 100.0;
            double netSalary = Math.max(0.0, Math.round((gross - totalDeductions) * 100.0) / 100.0);

            String recId = "PAY-" + month.replace("-", "") + "-" + emp.getId();
            String txRef = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Retain status if existed
            String status = "PENDING";
            if (payrollRecords.containsKey(recId)) {
                status = payrollRecords.get(recId).getStatus();
            }

            PayrollRecord rec = new PayrollRecord(
                    recId, month, emp.getId(), emp.getName(), emp.getDepartment(),
                    emp.getDesignation(), emp.getType(), emp.getBankAccount(), emp.getPanOrTaxId(),
                    workingDays, presentDays, overtimeHours, basic, allowances, overtimePay, bonus,
                    gross, tax, pf, other, totalDeductions, netSalary, status, todayStr, txRef
            );

            payrollRecords.put(recId, rec);
            newlyProcessed.add(rec);
        }

        saveData();
        return newlyProcessed;
    }

    public synchronized boolean updatePayrollStatus(String id, String newStatus) {
        PayrollRecord rec = payrollRecords.get(id);
        if (rec != null) {
            rec.setStatus(newStatus.toUpperCase());
            saveData();
            return true;
        }
        return false;
    }

    public synchronized Map<String, Object> getAnalytics(String month) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<PayrollRecord> filtered = new ArrayList<>();

        for (PayrollRecord r : payrollRecords.values()) {
            if (month == null || month.isEmpty() || month.equals("ALL") || r.getMonth().equalsIgnoreCase(month)) {
                filtered.add(r);
            }
        }

        double totalNetPay = 0.0;
        double totalGrossPay = 0.0;
        double totalTax = 0.0;
        double totalPf = 0.0;
        int paidCount = 0;
        int pendingCount = 0;

        Map<String, Double> deptCost = new HashMap<>();
        Map<String, Integer> deptCount = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();

        for (PayrollRecord r : filtered) {
            totalNetPay += r.getNetSalary();
            totalGrossPay += r.getGrossSalary();
            totalTax += r.getTaxDeduction();
            totalPf += r.getPfDeduction();

            if ("PAID".equalsIgnoreCase(r.getStatus())) paidCount++;
            else pendingCount++;

            String dept = r.getDepartment() != null ? r.getDepartment() : "General";
            deptCost.put(dept, deptCost.getOrDefault(dept, 0.0) + r.getGrossSalary());
            deptCount.put(dept, deptCount.getOrDefault(dept, 0) + 1);

            String t = r.getEmployeeType() != null ? r.getEmployeeType() : "OTHER";
            typeCount.put(t, typeCount.getOrDefault(t, 0) + 1);
        }

        double avgNetSalary = filtered.isEmpty() ? 0.0 : totalNetPay / filtered.size();

        result.put("month", month != null ? month : "ALL");
        result.put("totalEmployees", employees.size());
        result.put("processedRecords", filtered.size());
        result.put("totalGrossPay", Math.round(totalGrossPay * 100.0) / 100.0);
        result.put("totalNetPay", Math.round(totalNetPay * 100.0) / 100.0);
        result.put("totalTaxWithheld", Math.round(totalTax * 100.0) / 100.0);
        result.put("totalPfContribution", Math.round(totalPf * 100.0) / 100.0);
        result.put("avgNetSalary", Math.round(avgNetSalary * 100.0) / 100.0);
        result.put("paidCount", paidCount);
        result.put("pendingCount", pendingCount);
        result.put("deptCost", deptCost);
        result.put("deptCount", deptCount);
        result.put("typeDistribution", typeCount);

        return result;
    }

    private synchronized void saveData() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            List<Map<String, Object>> empList = new ArrayList<>();
            for (Employee e : employees.values()) {
                empList.add(e.toMap());
            }
            root.put("employees", empList);

            List<Map<String, Object>> payList = new ArrayList<>();
            for (PayrollRecord p : payrollRecords.values()) {
                payList.add(p.toMap());
            }
            root.put("payroll", payList);
            root.put("nextId", nextEmployeeId.get());

            String json = SimpleJson.toJson(root);
            File f = new File(DATA_FILE);
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            Files.writeString(f.toPath(), json);
        } catch (Exception e) {
            System.err.println("Failed to persist data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void loadData() {
        try {
            File f = new File(DATA_FILE);
            if (!f.exists()) return;
            String content = Files.readString(f.toPath());
            Object parsed = SimpleJson.parse(content);
            if (!(parsed instanceof Map<?, ?>)) return;
            Map<String, Object> root = (Map<String, Object>) parsed;

            if (root.containsKey("nextId")) {
                nextEmployeeId.set(toInt(root.get("nextId"), 101));
            }

            if (root.containsKey("employees")) {
                List<Object> list = (List<Object>) root.get("employees");
                for (Object item : list) {
                    if (item instanceof Map<?, ?>) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        int id = toInt(m.get("id"), 0);
                        String name = String.valueOf(m.get("name"));
                        String email = String.valueOf(m.get("email"));
                        String dept = String.valueOf(m.get("department"));
                        String desig = String.valueOf(m.get("designation"));
                        String type = String.valueOf(m.get("type"));
                        String join = String.valueOf(m.get("joiningDate"));
                        String bank = String.valueOf(m.get("bankAccount"));
                        String tax = String.valueOf(m.get("panOrTaxId"));
                        String status = String.valueOf(m.get("status"));
                        double basePay = toDouble(m.get("basePay"), 30000.0);

                        Employee emp;
                        if ("PART_TIME".equals(type)) {
                            double rate = toDouble(m.get("hourlyRate"), basePay / 80.0);
                            emp = new PartTimeEmployee(id, name, email, dept, desig, join, bank, tax, status, rate);
                        } else if ("CONTRACTOR".equals(type)) {
                            emp = new ContractEmployee(id, name, email, dept, desig, join, bank, tax, status, basePay);
                        } else if ("MANAGER".equals(type)) {
                            double lead = toDouble(m.get("leadershipAllowance"), 10000.0);
                            emp = new Manager(id, name, email, dept, desig, join, bank, tax, status, basePay, lead);
                        } else {
                            emp = new FullTimeEmployee(id, name, email, dept, desig, join, bank, tax, status, basePay);
                        }
                        employees.put(id, emp);
                    }
                }
            }

            if (root.containsKey("payroll")) {
                List<Object> list = (List<Object>) root.get("payroll");
                for (Object item : list) {
                    if (item instanceof Map<?, ?>) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        PayrollRecord r = new PayrollRecord(
                                String.valueOf(m.get("id")),
                                String.valueOf(m.get("month")),
                                toInt(m.get("employeeId"), 0),
                                String.valueOf(m.get("employeeName")),
                                String.valueOf(m.get("department")),
                                String.valueOf(m.get("designation")),
                                String.valueOf(m.get("employeeType")),
                                String.valueOf(m.get("bankAccount")),
                                String.valueOf(m.get("panOrTaxId")),
                                toInt(m.get("workingDays"), 22),
                                toInt(m.get("presentDays"), 22),
                                toDouble(m.get("overtimeHours"), 0.0),
                                toDouble(m.get("basicSalary"), 0.0),
                                toDouble(m.get("allowances"), 0.0),
                                toDouble(m.get("overtimePay"), 0.0),
                                toDouble(m.get("bonus"), 0.0),
                                toDouble(m.get("grossSalary"), 0.0),
                                toDouble(m.get("taxDeduction"), 0.0),
                                toDouble(m.get("pfDeduction"), 0.0),
                                toDouble(m.get("otherDeductions"), 0.0),
                                toDouble(m.get("totalDeductions"), 0.0),
                                toDouble(m.get("netSalary"), 0.0),
                                String.valueOf(m.get("status")),
                                String.valueOf(m.get("processedDate")),
                                String.valueOf(m.get("transactionRef"))
                        );
                        payrollRecords.put(r.getId(), r);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading saved data: " + e.getMessage());
        }
    }

    private static double toDouble(Object o, double defaultVal) {
        if (o == null) return defaultVal;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static int toInt(Object o, int defaultVal) {
        if (o == null) return defaultVal;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
