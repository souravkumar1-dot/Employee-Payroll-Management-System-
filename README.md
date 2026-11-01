# ⚡ AURA PAYROLL — Autonomous FinTech Command Deck

A distinct, production-grade **Employee Payroll Management & Financial Intelligence Platform** developed natively in **Java 25 (Standard Edition)**, **HTML5**, **CSS3**, and **Vanilla JavaScript**.

---

## 💎 What Makes AURA Distinct & Unique

Unlike conventional left-sidebar HR dashboards, **AURA** features a sleek, high-tech **FinTech Command Deck** architecture with ambient glassmorphism and real-time computation:

1. 🌓 **Integrated Light & Dark Mode Engine**:
   - Smooth animated theme switch toggle pill in the top command bar.
   - Preserves user preference in `localStorage` and detects OS system theme (`prefers-color-scheme`).
   - Custom-tuned high-contrast typography and glowing neon accents.

2. ⌨️ **Quick Command Palette (`Ctrl+K` / `⌘K`)**:
   - Universal keyboard-driven search: jump across views, search any employee, toggle theme, trigger autonomous payroll, or export reports without leaving the keyboard.

3. 💱 **Multi-Currency Live Conversion**:
   - Instant conversion between **INR (₹)**, **USD ($)**, **EUR (€)**, and **GBP (£)** across all KPI widgets, ledger tables, payslips, and simulation labs.

4. 🧪 **Tax & Progressive Slab Simulation Lab**:
   - Visual tax bracket position bar (0%, 10%, 15%, 20%).
   - Live take-home pay forecasting with variable overtime, allowances, attendance proration, and Section 194J / 192 TDS deductions.

5. 📜 **Cryptographically Authenticated Digital Payslip**:
   - Formal corporate payslip artifact with verification badge, node signature, amount in words, and one-click **PDF print** or **JSON receipt** download.

6. ☕ **Zero-Dependency Pure Java 25 Backend**:
   - No heavy Maven/Gradle or third-party frameworks.
   - Built on native `com.sun.net.httpserver.HttpServer` with a multi-threaded thread pool.
   - Native JSON parser and serializer with atomic file persistence (`data/payroll_db.json`).

---

## 📁 System Architecture

```
Employee Payroll Management/
├── run.bat                          # One-click Windows batch launcher
├── run.ps1                          # PowerShell launcher
├── README.md                        # Documentation & setup guide
├── bin/                             # Compiled Java bytecodes
├── data/                            # Persistent JSON database storage
│   └── payroll_db.json
├── public/                          # Web Frontend Assets
│   ├── index.html                   # Command Deck SPA Interface
│   ├── style.css                    # Light/Dark Theme & Glassmorphism Styles
│   └── app.js                       # Command Palette & Currency Engine
└── src/com/payroll/
    ├── PayrollServer.java           # Multi-threaded HTTP Server & REST API
    ├── model/                       # Employee & Payroll models
    ├── service/PayrollService.java  # Business logic & calculations
    └── util/SimpleJson.java         # Native JSON parser & serializer
```

---

## 🚀 Running the Project

### Option 1: One-Click Windows Launcher (Recommended)
Double-click:
```
run.bat
```
*Compiles the Java sources, launches the server on port `8080`, and opens the command deck in your browser.*

### Option 2: Windows PowerShell
```powershell
.\run.ps1
```

### Option 3: Manual Execution
```bash
javac -d bin -sourcepath src src/com/payroll/PayrollServer.java
java -cp bin com.payroll.PayrollServer
```
Click here to view project [ https://souravkumar1-dot.github.io/Employee-Payroll-Management-System-/]
