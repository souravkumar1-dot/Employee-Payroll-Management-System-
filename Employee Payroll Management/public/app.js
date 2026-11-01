/**
 * AURA PAYROLL — FINTECH COMMAND DECK CONTROLLER
 * Full-stack interaction engine connecting to Java SE 25 Backend (:8080)
 */

// --- Global Application State ---
const API_BASE = window.location.origin.includes('http') ? '' : 'http://localhost:8080';
let currentCycle = '2026-10';
let activeCurrency = 'INR';
let employees = [];
let payrollRecords = [];
let analytics = {};
let backendOnline = false;

// Currency Exchange Multipliers relative to INR
const CURRENCY_CONFIG = {
  INR: { symbol: '₹', rate: 1.0, decimals: 0 },
  USD: { symbol: '$', rate: 0.0118, decimals: 2 },
  EUR: { symbol: '€', rate: 0.0109, decimals: 2 },
  GBP: { symbol: '£', rate: 0.0093, decimals: 2 }
};

// --- Application Bootstrap ---
document.addEventListener('DOMContentLoaded', async () => {
  initThemeEngine();
  initDeckNavigation();
  initCycleSelector();
  initCurrencySelector();
  initCommandPalette();
  initTalentForm();
  initPayrollRunForm();
  initTaxLab();

  await verifyBackendEngine();
  await refreshDeckData();
});

// =====================================================================
// THEME SWITCHING ENGINE (LIGHT & DARK MODE)
// =====================================================================
function initThemeEngine() {
  const savedTheme = localStorage.getItem('aura_theme') || 
    (window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
  applyTheme(savedTheme);

  const themeBtn = document.getElementById('themeToggleBtn');
  themeBtn.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    applyTheme(next);
  });
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('aura_theme', theme);
}

// =====================================================================
// DECK NAVIGATION
// =====================================================================
function initDeckNavigation() {
  const tabs = document.querySelectorAll('.deck-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.dataset.tab;
      switchDeckTab(target);
    });
  });
}

function switchDeckTab(viewId) {
  document.querySelectorAll('.deck-tab').forEach(b => {
    b.classList.toggle('active', b.dataset.tab === viewId);
  });
  document.querySelectorAll('.deck-view').forEach(v => {
    v.classList.toggle('active', v.id === `view-${viewId}`);
  });
}

// =====================================================================
// CYCLE SELECTOR
// =====================================================================
function initCycleSelector() {
  const pills = document.querySelectorAll('.cycle-pill');
  pills.forEach(pill => {
    pill.addEventListener('click', async () => {
      pills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      currentCycle = pill.dataset.cycle;
      await refreshDeckData();
    });
  });
}

// =====================================================================
// CURRENCY SELECTOR
// =====================================================================
function initCurrencySelector() {
  const select = document.getElementById('currencySelect');
  select.addEventListener('change', (e) => {
    activeCurrency = e.target.value;
    updateDashboardUI();
    renderTalentTable();
    renderDisbursementTable();
    updatePayslipUI();
    runTaxLabSimulation();
    showToast(`Currency flipped to ${activeCurrency}`);
  });
}

function formatMoney(amountInInr) {
  const n = Number(amountInInr) || 0;
  const cfg = CURRENCY_CONFIG[activeCurrency] || CURRENCY_CONFIG.INR;
  const converted = n * cfg.rate;
  return cfg.symbol + ' ' + converted.toLocaleString('en-US', {
    minimumFractionDigits: cfg.decimals,
    maximumFractionDigits: cfg.decimals
  });
}

// =====================================================================
// BACKEND HEALTH & DATA PIPELINE
// =====================================================================
async function verifyBackendEngine() {
  const badge = document.getElementById('engineBadge');
  const txt = document.getElementById('engineStatusTxt');
  try {
    const res = await fetch(`${API_BASE}/api/health`, { cache: 'no-store' });
    if (res.ok) {
      backendOnline = true;
      txt.innerText = 'Java 25 Native :8080 Active';
      badge.style.borderColor = 'rgba(16, 185, 129, 0.4)';
      return;
    }
  } catch (err) {}
  backendOnline = false;
  txt.innerText = 'Autonomous Local Vault';
  badge.style.borderColor = 'rgba(245, 158, 11, 0.4)';
}

async function refreshDeckData() {
  try {
    if (backendOnline) {
      const [empRes, payRes, anaRes] = await Promise.all([
        fetch(`${API_BASE}/api/employees`),
        fetch(`${API_BASE}/api/payroll?month=${currentCycle}`),
        fetch(`${API_BASE}/api/analytics?month=${currentCycle}`)
      ]);
      employees = await empRes.json();
      payrollRecords = await payRes.json();
      analytics = await anaRes.json();
    } else {
      loadFallbackData();
    }
  } catch (err) {
    loadFallbackData();
  }

  updateDashboardUI();
  populateTalentDeptFilter();
  renderTalentTable();
  renderDisbursementTable();
  populateSlipSelector();
}

function loadFallbackData() {
  if (employees.length === 0) {
    employees = [
      { id: 101, name: "Aarav Sharma", email: "aarav.s@aura.io", department: "Engineering", designation: "Staff Distributed Systems Engineer", type: "FULL_TIME", joiningDate: "2023-01-15", bankAccount: "HDFC0001234", panOrTaxId: "AAAPS1234K", status: "ACTIVE", basePay: 85000 },
      { id: 102, name: "Priya Nair", email: "priya.n@aura.io", department: "Strategic People", designation: "Chief People Officer", type: "MANAGER", joiningDate: "2021-06-10", bankAccount: "ICIC0005678", panOrTaxId: "BBYPN5678L", status: "ACTIVE", basePay: 110000, leadershipAllowance: 20000 },
      { id: 103, name: "Rohan Verma", email: "rohan.v@aura.io", department: "Product Design", designation: "Design Systems Lead", type: "PART_TIME", joiningDate: "2024-03-01", bankAccount: "SBIN0009988", panOrTaxId: "CCRPV9988M", status: "ACTIVE", basePay: 55000, hourlyRate: 700 },
      { id: 104, name: "Elena Vance", email: "elena.v@aura.io", department: "Growth & Mktg", designation: "Global Brand Strategist", type: "CONTRACTOR", joiningDate: "2024-05-12", bankAccount: "CITI0004321", panOrTaxId: "DDEEV4321N", status: "ACTIVE", basePay: 50000 },
      { id: 105, name: "Vikram Malhotra", email: "vikram.m@aura.io", department: "Quant Finance", designation: "Lead Treasury Analyst", type: "FULL_TIME", joiningDate: "2022-11-20", bankAccount: "AXIS0008765", panOrTaxId: "FFVVM8765P", status: "ACTIVE", basePay: 72000 }
    ];
  }
  if (payrollRecords.length === 0) {
    payrollRecords = employees.map(emp => {
      const basic = emp.basePay;
      const allowances = emp.type === 'MANAGER' ? (basic * 0.3) + 21500 : (emp.type === 'FULL_TIME' ? (basic * 0.3) + 1500 : 0);
      const gross = basic + allowances;
      const pf = (emp.type === 'FULL_TIME' || emp.type === 'MANAGER') ? Math.round(basic * 0.12) : 0;
      const tax = Math.round(gross * 0.09);
      const other = 650;
      const totalDeductions = pf + tax + other;
      const net = gross - totalDeductions;
      return {
        id: `VCH-${currentCycle.replace('-','')}-${emp.id}`,
        month: currentCycle,
        employeeId: emp.id,
        employeeName: emp.name,
        department: emp.department,
        designation: emp.designation,
        employeeType: emp.type,
        bankAccount: emp.bankAccount,
        panOrTaxId: emp.panOrTaxId,
        workingDays: 22,
        presentDays: 22,
        overtimeHours: 0,
        basicSalary: basic,
        allowances: allowances,
        overtimePay: 0,
        bonus: 0,
        grossSalary: gross,
        taxDeduction: tax,
        pfDeduction: pf,
        otherDeductions: other,
        totalDeductions: totalDeductions,
        netSalary: net,
        status: "PAID",
        processedDate: "2026-10-31",
        transactionRef: "TXN" + Math.random().toString(36).substring(2, 9).toUpperCase()
      };
    });
  }
}

// =====================================================================
// DASHBOARD & FINANCIAL PULSE UI
// =====================================================================
function updateDashboardUI() {
  const records = payrollRecords;
  let totalNet = 0;
  let totalGross = 0;
  let totalTax = 0;
  let totalPf = 0;
  let paidCount = 0;
  const deptCostMap = {};

  records.forEach(r => {
    totalNet += Number(r.netSalary) || 0;
    totalGross += Number(r.grossSalary) || 0;
    totalTax += Number(r.taxDeduction) || 0;
    totalPf += Number(r.pfDeduction) || 0;
    if (r.status === 'PAID') paidCount++;

    const dept = r.department || 'General';
    deptCostMap[dept] = (deptCostMap[dept] || 0) + (Number(r.grossSalary) || 0);
  });

  const avgNet = records.length > 0 ? totalNet / records.length : 0;
  const settledRatio = records.length > 0 ? Math.round((paidCount / records.length) * 100) : 0;

  document.getElementById('pulseNetPay').innerText = formatMoney(totalNet);
  document.getElementById('pulseGrossPay').innerText = formatMoney(totalGross);
  document.getElementById('pulseTaxPay').innerText = formatMoney(totalTax);
  document.getElementById('pulsePfPay').innerText = formatMoney(totalPf);
  document.getElementById('pulseHeadcount').innerText = employees.length;
  document.getElementById('pulsePaidRatio').innerText = `${paidCount} / ${records.length}`;
  document.getElementById('pulseStatusRate').innerText = `${settledRatio}% Settled`;
  document.getElementById('pulseAvgSalary').innerText = formatMoney(avgNet);
  document.getElementById('pulseDeptCount').innerText = Object.keys(deptCostMap).length;

  // Render Department Spectrum Chart
  renderSpectrumChart(deptCostMap, totalGross);

  // Render Pulse Preview Table
  renderPulseTable(records.slice(0, 5));
}

function renderSpectrumChart(deptMap, total) {
  const container = document.getElementById('spectrumContainer');
  container.innerHTML = '';
  const entries = Object.entries(deptMap);
  if (entries.length === 0) {
    container.innerHTML = '<p class="sub-addr">No disbursement telemetry recorded for this cycle.</p>';
    return;
  }

  const palette = ['#6366f1', '#10b981', '#f59e0b', '#a855f7', '#06b6d4', '#ec4899'];
  let idx = 0;
  entries.sort((a, b) => b[1] - a[1]);

  entries.forEach(([dept, cost]) => {
    const pct = total > 0 ? Math.round((cost / total) * 100) : 0;
    const color = palette[idx % palette.length];
    idx++;

    const row = document.createElement('div');
    row.className = 'spectrum-row';
    row.innerHTML = `
      <div class="spectrum-label-line">
        <span>${dept}</span>
        <span><strong>${formatMoney(cost)}</strong> (${pct}%)</span>
      </div>
      <div class="spectrum-track">
        <div class="spectrum-fill" style="width: ${pct}%; background: ${color};"></div>
      </div>
    `;
    container.appendChild(row);
  });
}

function renderPulseTable(records) {
  const tbody = document.getElementById('pulseTableBody');
  tbody.innerHTML = '';
  if (records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="padding: 1.5rem; text-align: center; color: var(--text-tertiary);">No entries in current cycle. Click "Execute Auto-Payroll" to run engine.</td></tr>`;
    return;
  }

  records.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="font-mono" style="font-weight: 700;">${r.id}</span></td>
      <td>
        <div class="avatar-node">
          <div class="avatar-badge">${getInitials(r.employeeName)}</div>
          <div class="avatar-info">
            <span class="avatar-name">${r.employeeName}</span>
            <span class="avatar-meta">#${r.employeeId}</span>
          </div>
        </div>
      </td>
      <td>
        <div><strong>${r.designation}</strong></div>
        <div class="avatar-meta">${r.department}</div>
      </td>
      <td class="font-mono">${formatMoney(r.grossSalary)}</td>
      <td class="font-mono" style="color: var(--rose);">- ${formatMoney(r.totalDeductions)}</td>
      <td class="font-mono" style="font-weight: 700; color: var(--accent-primary);">${formatMoney(r.netSalary)}</td>
      <td>${getStatusTag(r.status)}</td>
      <td>
        <button class="action-ghost-btn sm" onclick="inspectSlip('${r.id}')">View Slip</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// =====================================================================
// TALENT LEDGER (EMPLOYEES)
// =====================================================================
function populateTalentDeptFilter() {
  const depts = new Set(employees.map(e => e.department).filter(Boolean));
  const filter = document.getElementById('talentDeptFilter');
  filter.innerHTML = '<option value="ALL">All Departments</option>';
  depts.forEach(d => {
    const opt = document.createElement('option');
    opt.value = d;
    opt.innerText = d;
    filter.appendChild(opt);
  });
}

function renderTalentTable() {
  const search = (document.getElementById('talentSearchInput')?.value || '').toLowerCase();
  const deptFilter = document.getElementById('talentDeptFilter')?.value || 'ALL';
  const tierFilter = document.getElementById('talentTierFilter')?.value || 'ALL';
  const tbody = document.getElementById('talentTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const filtered = employees.filter(emp => {
    const matchesSearch = !search ||
      emp.name.toLowerCase().includes(search) ||
      emp.email.toLowerCase().includes(search) ||
      emp.designation.toLowerCase().includes(search) ||
      String(emp.id).includes(search);
    const matchesDept = deptFilter === 'ALL' || emp.department === deptFilter;
    const matchesTier = tierFilter === 'ALL' || emp.type === tierFilter;
    return matchesSearch && matchesDept && matchesTier;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="padding: 2rem; text-align: center; color: var(--text-tertiary);">No talent records matching parameters.</td></tr>`;
    return;
  }

  filtered.forEach(emp => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="font-mono" style="font-weight: 700;">#${emp.id}</span></td>
      <td>
        <div class="avatar-node">
          <div class="avatar-badge">${getInitials(emp.name)}</div>
          <div class="avatar-info">
            <span class="avatar-name">${emp.name}</span>
            <span class="avatar-meta">${emp.email}</span>
          </div>
        </div>
      </td>
      <td>
        <div><strong>${emp.designation}</strong></div>
        <div class="avatar-meta">${emp.department}</div>
      </td>
      <td>${getTierTag(emp.type)}</td>
      <td class="font-mono" style="font-weight: 700;">
        ${emp.type === 'PART_TIME' ? `${formatMoney(emp.hourlyRate || (emp.basePay/80))}/hr` : formatMoney(emp.basePay)}
      </td>
      <td>
        <div class="font-mono" style="font-size: 0.8rem;">${emp.bankAccount || '--'}</div>
        <div class="font-mono" style="font-size: 0.725rem; color: var(--text-tertiary);">PAN: ${emp.panOrTaxId || '--'}</div>
      </td>
      <td>${getStatusTag(emp.status)}</td>
      <td>
        <div style="display: flex; gap: 0.35rem;">
          <button class="action-ghost-btn sm" onclick="openTalentModal(${emp.id})">Edit</button>
          <button class="action-ghost-btn sm" style="color: var(--rose);" onclick="removeTalent(${emp.id})">Decommission</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function initTalentForm() {
  const form = document.getElementById('talentForm');
  const typeSelect = document.getElementById('formType');

  typeSelect.addEventListener('change', (e) => {
    const val = e.target.value;
    const leadWrap = document.getElementById('formLeadWrap');
    const baseLbl = document.getElementById('formBaseLabel');
    if (val === 'MANAGER') {
      leadWrap.style.display = 'flex';
      baseLbl.innerText = 'Monthly Base Compensation (₹) *';
    } else if (val === 'PART_TIME') {
      leadWrap.style.display = 'none';
      baseLbl.innerText = 'Hourly Wage Rate (₹) *';
    } else if (val === 'CONTRACTOR') {
      leadWrap.style.display = 'none';
      baseLbl.innerText = 'Monthly Retainer Fee (₹) *';
    } else {
      leadWrap.style.display = 'none';
      baseLbl.innerText = 'Monthly Base Compensation (₹) *';
    }
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const idStr = document.getElementById('formEmpId').value;
    const payload = {
      name: document.getElementById('formName').value,
      email: document.getElementById('formEmail').value,
      department: document.getElementById('formDept').value,
      designation: document.getElementById('formDesignation').value,
      type: document.getElementById('formType').value,
      basePay: parseFloat(document.getElementById('formBasePay').value) || 0,
      bankAccount: document.getElementById('formBank').value,
      panOrTaxId: document.getElementById('formPan').value,
      status: document.getElementById('formStatus').value
    };

    if (payload.type === 'MANAGER') {
      payload.leadershipAllowance = parseFloat(document.getElementById('formLead').value) || 12000;
    }

    try {
      if (backendOnline) {
        if (idStr) {
          await fetch(`${API_BASE}/api/employees/${idStr}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          showToast(`Talent #${idStr} updated`);
        } else {
          await fetch(`${API_BASE}/api/employees`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          showToast('Resource profile registered');
        }
      } else {
        if (idStr) {
          const id = parseInt(idStr);
          const idx = employees.findIndex(e => e.id === id);
          if (idx !== -1) employees[idx] = { ...employees[idx], ...payload };
        } else {
          const newId = employees.length > 0 ? Math.max(...employees.map(e => e.id)) + 1 : 101;
          employees.push({ id: newId, ...payload });
        }
        showToast('Resource profile stored (Local Vault)');
      }
      document.getElementById('talentModal').classList.remove('active');
      await refreshDeckData();
    } catch (err) {
      showToast('Error saving talent: ' + err.message, 'error');
    }
  });

  // Search input binding
  document.getElementById('talentSearchInput')?.addEventListener('input', renderTalentTable);
  document.getElementById('talentDeptFilter')?.addEventListener('change', renderTalentTable);
  document.getElementById('talentTierFilter')?.addEventListener('change', renderTalentTable);
}

function openTalentModal(empId = null) {
  const modal = document.getElementById('talentModal');
  const title = document.getElementById('talentModalTitle');
  const form = document.getElementById('talentForm');
  form.reset();

  if (empId) {
    const emp = employees.find(e => e.id === empId);
    if (!emp) return;
    title.innerText = `Edit Resource Profile #${emp.id}`;
    document.getElementById('formEmpId').value = emp.id;
    document.getElementById('formName').value = emp.name;
    document.getElementById('formEmail').value = emp.email;
    document.getElementById('formDept').value = emp.department;
    document.getElementById('formDesignation').value = emp.designation;
    document.getElementById('formType').value = emp.type;
    document.getElementById('formBasePay').value = emp.basePay;
    document.getElementById('formBank').value = emp.bankAccount || '';
    document.getElementById('formPan').value = emp.panOrTaxId || '';
    document.getElementById('formStatus').value = emp.status || 'ACTIVE';

    if (emp.type === 'MANAGER') {
      document.getElementById('formLeadWrap').style.display = 'flex';
      document.getElementById('formLead').value = emp.leadershipAllowance || 12000;
    } else {
      document.getElementById('formLeadWrap').style.display = 'none';
    }
  } else {
    title.innerText = 'Enroll New Talent Resource';
    document.getElementById('formEmpId').value = '';
    document.getElementById('formLeadWrap').style.display = 'none';
  }
  modal.classList.add('active');
}

async function removeTalent(empId) {
  if (!confirm(`Confirm decommissioning resource #${empId}?`)) return;
  try {
    if (backendOnline) {
      await fetch(`${API_BASE}/api/employees/${empId}`, { method: 'DELETE' });
    } else {
      employees = employees.filter(e => e.id !== empId);
    }
    showToast(`Resource #${empId} decommissioned`);
    await refreshDeckData();
  } catch (err) {
    showToast('Failed to decommission: ' + err.message, 'error');
  }
}

// =====================================================================
// DISBURSEMENT MATRIX (PAYROLL RUNS)
// =====================================================================
function initPayrollRunForm() {
  document.getElementById('openPayrollRunModal').addEventListener('click', () => {
    document.getElementById('payrollRunModal').classList.add('active');
  });

  document.getElementById('payrollRunForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const month = document.getElementById('runCycleInput').value;
    const workingDays = parseInt(document.getElementById('runWorkingDaysInput').value) || 22;

    try {
      if (backendOnline) {
        await fetch(`${API_BASE}/api/payroll/process`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ month, workingDays, overrides: {} })
        });
        showToast(`Autonomous payroll calculation completed for ${month}`);
      } else {
        payrollRecords = employees.map(emp => {
          const basic = emp.basePay;
          const allowances = emp.type === 'MANAGER' ? (basic * 0.3) + 21500 : (emp.type === 'FULL_TIME' ? (basic * 0.3) + 1500 : 0);
          const gross = basic + allowances;
          const pf = (emp.type === 'FULL_TIME' || emp.type === 'MANAGER') ? Math.round(basic * 0.12) : 0;
          const tax = Math.round(gross * 0.09);
          const other = 650;
          const totalDeductions = pf + tax + other;
          const net = gross - totalDeductions;
          return {
            id: `VCH-${month.replace('-','')}-${emp.id}`,
            month: month,
            employeeId: emp.id,
            employeeName: emp.name,
            department: emp.department,
            designation: emp.designation,
            employeeType: emp.type,
            bankAccount: emp.bankAccount,
            panOrTaxId: emp.panOrTaxId,
            workingDays: workingDays,
            presentDays: workingDays,
            overtimeHours: 0,
            basicSalary: basic,
            allowances: allowances,
            overtimePay: 0,
            bonus: 0,
            grossSalary: gross,
            taxDeduction: tax,
            pfDeduction: pf,
            otherDeductions: other,
            totalDeductions: totalDeductions,
            netSalary: net,
            status: 'PENDING',
            processedDate: new Date().toISOString().split('T')[0],
            transactionRef: "TXN" + Math.random().toString(36).substring(2, 9).toUpperCase()
          };
        });
        showToast(`Cycle ${month} executed (Local Vault)`);
      }
      document.getElementById('payrollRunModal').classList.remove('active');
      currentCycle = month;
      await refreshDeckData();
    } catch (err) {
      showToast('Payroll engine run failed: ' + err.message, 'error');
    }
  });

  // Table filters & CSV trigger
  document.getElementById('disburseSearchInput')?.addEventListener('input', renderDisbursementTable);
  document.getElementById('disburseStatusFilter')?.addEventListener('change', renderDisbursementTable);
  document.getElementById('exportCsvTrigger')?.addEventListener('click', exportLedgerCSV);
}

function renderDisbursementTable() {
  const search = (document.getElementById('disburseSearchInput')?.value || '').toLowerCase();
  const statusFilter = document.getElementById('disburseStatusFilter')?.value || 'ALL';
  const tbody = document.getElementById('disburseTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const filtered = payrollRecords.filter(r => {
    const matchesSearch = !search ||
      r.employeeName.toLowerCase().includes(search) ||
      r.id.toLowerCase().includes(search) ||
      r.department.toLowerCase().includes(search);
    const matchesStatus = statusFilter === 'ALL' || r.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="13" style="padding: 2rem; text-align: center; color: var(--text-tertiary);">No vouchers in this view filter.</td></tr>`;
    return;
  }

  filtered.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="font-mono" style="font-weight: 700;">${r.id}</span></td>
      <td>
        <div><strong>${r.employeeName}</strong></div>
        <div class="avatar-meta">#${r.employeeId} &bull; ${r.department}</div>
      </td>
      <td>${getTierTag(r.employeeType)}</td>
      <td class="font-mono" style="font-size: 0.8rem;">${r.presentDays}/${r.workingDays}d</td>
      <td class="font-mono">${formatMoney(r.basicSalary)}</td>
      <td class="font-mono">${formatMoney(r.allowances)}</td>
      <td class="font-mono">${formatMoney(r.overtimePay)}</td>
      <td class="font-mono" style="font-weight: 700;">${formatMoney(r.grossSalary)}</td>
      <td class="font-mono" style="color: var(--rose);">- ${formatMoney(r.pfDeduction)}</td>
      <td class="font-mono" style="color: var(--rose);">- ${formatMoney(r.taxDeduction)}</td>
      <td class="font-mono" style="font-weight: 800; color: var(--accent-primary);">${formatMoney(r.netSalary)}</td>
      <td>
        <select class="pill-select" style="padding: 0.25rem 0.55rem; font-size: 0.75rem;" onchange="alterVoucherStatus('${r.id}', this.value)">
          <option value="PAID" ${r.status === 'PAID' ? 'selected' : ''}>SETTLED (PAID)</option>
          <option value="PENDING" ${r.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
          <option value="PROCESSING" ${r.status === 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
        </select>
      </td>
      <td>
        <button class="action-ghost-btn sm" onclick="inspectSlip('${r.id}')">Slip</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function alterVoucherStatus(id, newStatus) {
  try {
    if (backendOnline) {
      await fetch(`${API_BASE}/api/payroll/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id, status: newStatus })
      });
    } else {
      const rec = payrollRecords.find(r => r.id === id);
      if (rec) rec.status = newStatus;
    }
    showToast(`Voucher ${id} marked ${newStatus}`);
    await refreshDeckData();
  } catch (err) {
    showToast('Failed to update status', 'error');
  }
}

function exportLedgerCSV() {
  if (payrollRecords.length === 0) {
    showToast('Ledger empty, nothing to export!', 'error');
    return;
  }
  const headers = ['VoucherID', 'AccountingMonth', 'EmpID', 'ResourceName', 'StrategicUnit', 'Role', 'ContractTier', 'BasicSalary', 'Allowances', 'GrossAccrual', 'PFWithheld', 'TDSTax', 'TotalDeductions', 'NetTakeHome', 'DisbursementState', 'TxnRef'];
  const rows = payrollRecords.map(r => [
    r.id, r.month, r.employeeId, `"${r.employeeName}"`, `"${r.department}"`, `"${r.designation}"`,
    r.employeeType, r.basicSalary, r.allowances, r.grossSalary, r.pfDeduction, r.taxDeduction,
    r.totalDeductions, r.netSalary, r.status, r.transactionRef
  ]);

  const csv = "data:text/csv;charset=utf-8," + [headers.join(','), ...rows.map(e => e.join(','))].join("\n");
  const link = document.createElement("a");
  link.setAttribute("href", encodeURI(csv));
  link.setAttribute("download", `AURA_Ledger_${currentCycle}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Disbursement ledger exported to CSV');
}

// =====================================================================
// ENCRYPTED DIGITAL PAYSLIP
// =====================================================================
function populateSlipSelector() {
  const sel = document.getElementById('slipSelectDropdown');
  if (!sel) return;
  sel.innerHTML = '';
  if (payrollRecords.length === 0) {
    sel.innerHTML = '<option value="">No vouchers registered</option>';
    return;
  }
  payrollRecords.forEach(r => {
    const opt = document.createElement('option');
    opt.value = r.id;
    opt.innerText = `${r.employeeName} (${r.id}) — ${formatMoney(r.netSalary)}`;
    sel.appendChild(opt);
  });
  renderSlipDocument(payrollRecords[0]);

  sel.addEventListener('change', (e) => {
    const r = payrollRecords.find(item => item.id === e.target.value);
    if (r) renderSlipDocument(r);
  });
}

function inspectSlip(recordId) {
  const r = payrollRecords.find(item => item.id === recordId);
  if (r) {
    document.getElementById('slipSelectDropdown').value = recordId;
    renderSlipDocument(r);
    switchDeckTab('slip');
  }
}

function renderSlipDocument(rec) {
  if (!rec) return;
  document.getElementById('slipPeriodLabel').innerText = `${formatCycleName(rec.month)} Accounting Cycle`;
  document.getElementById('slipTxnHash').innerText = `TXN-${rec.transactionRef || '98A2-E8F1'}`;
  document.getElementById('slipName').innerText = rec.employeeName;
  document.getElementById('slipStaffId').innerText = `#AUR-${rec.employeeId}`;
  document.getElementById('slipRole').innerText = rec.designation;
  document.getElementById('slipDept').innerText = rec.department;
  document.getElementById('slipBank').innerText = rec.bankAccount || 'HDFC0001234';
  document.getElementById('slipPan').innerText = rec.panOrTaxId || 'AAAPS1234K';
  document.getElementById('slipAttendance').innerText = `${rec.presentDays} / ${rec.workingDays} Operational Days`;
  document.getElementById('slipTier').innerText = rec.employeeType.replace('_', ' ');

  const basic = rec.basicSalary;
  const hra = rec.employeeType === 'FULL_TIME' || rec.employeeType === 'MANAGER' ? basic * 0.20 : 0;
  const daMed = rec.employeeType === 'FULL_TIME' || rec.employeeType === 'MANAGER' ? basic * 0.10 + 1500 : 0;
  const lead = rec.employeeType === 'MANAGER' ? 20000 : 0;

  document.getElementById('slipBasic').innerText = formatMoney(basic);
  document.getElementById('slipHra').innerText = formatMoney(hra);
  document.getElementById('slipDaMed').innerText = formatMoney(daMed);
  document.getElementById('slipLead').innerText = formatMoney(lead);
  document.getElementById('slipOvertime').innerText = formatMoney(rec.overtimePay);
  document.getElementById('slipBonus').innerText = formatMoney(rec.bonus);
  document.getElementById('slipGross').innerText = formatMoney(rec.grossSalary);

  document.getElementById('slipPf').innerText = formatMoney(rec.pfDeduction);
  document.getElementById('slipTax').innerText = formatMoney(rec.taxDeduction);
  document.getElementById('slipPt').innerText = formatMoney(200);
  document.getElementById('slipIns').innerText = formatMoney(450);
  document.getElementById('slipDeduct').innerText = formatMoney(rec.totalDeductions);

  document.getElementById('slipNetCurrency').innerText = activeCurrency;
  document.getElementById('slipNet').innerText = formatMoney(rec.netSalary);
  document.getElementById('slipWords').innerText = convertAmountToWords(Math.round(rec.netSalary));
  document.getElementById('slipTimestamp').innerText = rec.processedDate || new Date().toISOString().split('T')[0];
  document.getElementById('slipNodeSig').innerText = `0x${Math.random().toString(16).substring(2, 10).toUpperCase()}...44EA`;
}

function updatePayslipUI() {
  const sel = document.getElementById('slipSelectDropdown');
  if (sel && sel.value) {
    const r = payrollRecords.find(item => item.id === sel.value);
    if (r) renderSlipDocument(r);
  }
}

function downloadSlipJSON() {
  const sel = document.getElementById('slipSelectDropdown');
  const r = payrollRecords.find(item => item.id === sel.value);
  if (!r) return;
  const jsonStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(r, null, 2));
  const link = document.createElement("a");
  link.setAttribute("href", jsonStr);
  link.setAttribute("download", `Payslip_${r.id}.json`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Encrypted JSON receipt downloaded');
}

// =====================================================================
// TAX & SLAB SIMULATION LAB
// =====================================================================
function initTaxLab() {
  const inputs = ['labTier', 'labBasePay', 'labLeadAllowance', 'labWorkingDays', 'labPresentDays', 'labOvertime', 'labBonus'];
  inputs.forEach(id => {
    document.getElementById(id)?.addEventListener('input', runTaxLabSimulation);
  });

  document.getElementById('labTier')?.addEventListener('change', (e) => {
    const val = e.target.value;
    const leadFld = document.getElementById('labLeadField');
    const baseLbl = document.getElementById('labBaseLabel');
    if (val === 'MANAGER') {
      leadFld.style.display = 'flex';
      baseLbl.innerText = 'Monthly Base Compensation';
    } else if (val === 'PART_TIME') {
      leadFld.style.display = 'none';
      baseLbl.innerText = 'Hourly Consultant Rate';
      document.getElementById('labBasePay').value = '700';
    } else if (val === 'CONTRACTOR') {
      leadFld.style.display = 'none';
      baseLbl.innerText = 'Contract Retainer Fee';
    } else {
      leadFld.style.display = 'none';
      baseLbl.innerText = 'Monthly Base Compensation';
    }
    runTaxLabSimulation();
  });

  runTaxLabSimulation();
}

function runTaxLabSimulation() {
  const tier = document.getElementById('labTier').value;
  const base = parseFloat(document.getElementById('labBasePay').value) || 0;
  const lead = parseFloat(document.getElementById('labLeadAllowance').value) || 0;
  const workingDays = parseInt(document.getElementById('labWorkingDays').value) || 22;
  const presentDays = parseInt(document.getElementById('labPresentDays').value) || 22;
  const overtime = parseFloat(document.getElementById('labOvertime').value) || 0;
  const bonus = parseFloat(document.getElementById('labBonus').value) || 0;

  let earnedBase = 0;
  let allowances = 0;
  let overtimePay = 0;
  let pf = 0;
  let tax = 0;
  let other = 0;

  if (tier === 'PART_TIME') {
    earnedBase = presentDays * 4.0 * base;
    overtimePay = Math.round(overtime * base * 1.25);
    allowances = bonus;
    const gross = earnedBase + overtimePay + allowances;
    tax = gross > 25000 ? Math.round(gross * 0.05) : 0;
    other = 100;
  } else if (tier === 'CONTRACTOR') {
    const factor = Math.min(1.0, presentDays / workingDays);
    earnedBase = Math.round(base * factor);
    overtimePay = Math.round(overtime * (base / 160.0));
    allowances = bonus;
    const gross = earnedBase + overtimePay + allowances;
    tax = Math.round(gross * 0.10);
    other = 0;
  } else {
    // FULL_TIME & MANAGER
    const factor = Math.min(1.0, presentDays / workingDays);
    earnedBase = Math.round(base * factor);
    const hra = Math.round(base * 0.20);
    const da = Math.round(base * 0.10);
    const med = 1500;
    const leadAllow = tier === 'MANAGER' ? lead : 0;
    allowances = hra + da + med + leadAllow;
    overtimePay = Math.round(overtime * (base / 160.0) * 1.5);
    pf = Math.round(base * 0.12);

    const gross = earnedBase + allowances + overtimePay + bonus;
    const annual = gross * 12.0;

    // Progressive annualized tax slabs
    let annualTax = 0;
    if (annual > 1200000) annualTax += (annual - 1200000) * 0.20 + 95000;
    else if (annual > 800000) annualTax += (annual - 800000) * 0.15 + 30000;
    else if (annual > 500000) annualTax += (annual - 500000) * 0.10;
    tax = Math.round(annualTax / 12.0);
    other = 650;
  }

  const gross = earnedBase + allowances + overtimePay + bonus;
  const deductions = pf + tax + other;
  const net = Math.max(0, gross - deductions);

  document.getElementById('labResBasic').innerText = formatMoney(earnedBase);
  document.getElementById('labResAllow').innerText = formatMoney(allowances);
  document.getElementById('labResOvertime').innerText = formatMoney(overtimePay);
  document.getElementById('labResBonus').innerText = formatMoney(bonus);
  document.getElementById('labResGross').innerText = formatMoney(gross);
  document.getElementById('labResPf').innerText = `- ${formatMoney(pf)}`;
  document.getElementById('labResTax').innerText = `- ${formatMoney(tax)}`;
  document.getElementById('labResOther').innerText = `- ${formatMoney(other)}`;
  document.getElementById('labResultNet').innerText = formatMoney(net);
  document.getElementById('labResultNetWords').innerText = convertAmountToWords(Math.round(net));

  // Update slab visualizer
  const annualized = gross * 12.0;
  document.getElementById('slabAnnualVal').innerText = formatMoney(annualized);
}

// =====================================================================
// COMMAND PALETTE (CTRL+K / CMD+K)
// =====================================================================
function initCommandPalette() {
  const modal = document.getElementById('cmdPaletteModal');
  const input = document.getElementById('cmdPaletteInput');
  const trigger = document.getElementById('cmdPaletteBtn');

  function openPalette() {
    modal.classList.add('active');
    input.value = '';
    renderPaletteResults('');
    input.focus();
  }

  function closePalette() {
    modal.classList.remove('active');
  }

  trigger.addEventListener('click', openPalette);

  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      if (modal.classList.contains('active')) closePalette();
      else openPalette();
    } else if (e.key === 'Escape' && modal.classList.contains('active')) {
      closePalette();
    }
  });

  modal.addEventListener('click', (e) => {
    if (e.target === modal) closePalette();
  });

  input.addEventListener('input', (e) => {
    renderPaletteResults(e.target.value);
  });
}

function renderPaletteResults(query) {
  const container = document.getElementById('cmdResultsList');
  container.innerHTML = '';
  const q = query.toLowerCase().trim();

  const commands = [
    { title: 'Switch to Financial Pulse', meta: 'Navigation', action: () => switchDeckTab('pulse') },
    { title: 'Open Talent Ledger', meta: 'Navigation', action: () => switchDeckTab('directory') },
    { title: 'Open Disbursement Matrix', meta: 'Navigation', action: () => switchDeckTab('execution') },
    { title: 'Open Encrypted Payslip', meta: 'Navigation', action: () => switchDeckTab('slip') },
    { title: 'Launch Tax & Slab Lab', meta: 'Navigation', action: () => switchDeckTab('taxlab') },
    { title: 'Toggle Light / Dark Mode', meta: 'Preferences', action: () => document.getElementById('themeToggleBtn').click() },
    { title: 'Trigger Autonomous Payroll Run', meta: 'Execution', action: () => document.getElementById('payrollRunModal').classList.add('active') },
    { title: 'Enroll New Talent Resource', meta: 'HRMS', action: () => openTalentModal() },
    { title: 'Export Payroll Ledger (.CSV)', meta: 'Ledger', action: () => exportLedgerCSV() }
  ];

  // Also include employee matches
  employees.forEach(emp => {
    commands.push({
      title: `Inspect Resource: ${emp.name}`,
      meta: `${emp.department} • #${emp.id}`,
      action: () => {
        const r = payrollRecords.find(item => item.employeeId === emp.id);
        if (r) inspectSlip(r.id);
        else {
          switchDeckTab('directory');
          document.getElementById('talentSearchInput').value = emp.name;
          renderTalentTable();
        }
      }
    });
  });

  const matches = commands.filter(c => !q || c.title.toLowerCase().includes(q) || c.meta.toLowerCase().includes(q));

  matches.slice(0, 8).forEach(m => {
    const item = document.createElement('div');
    item.className = 'cmd-item';
    item.innerHTML = `
      <span>${m.title}</span>
      <span class="cmd-item-meta">${m.meta}</span>
    `;
    item.addEventListener('click', () => {
      document.getElementById('cmdPaletteModal').classList.remove('active');
      m.action();
    });
    container.appendChild(item);
  });
}

// =====================================================================
// HELPERS, TAGS & CONVERTERS
// =====================================================================
function getInitials(name) {
  if (!name) return 'AP';
  return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
}

function getStatusTag(status) {
  const s = String(status).toUpperCase();
  if (s === 'PAID' || s === 'ACTIVE') return `<span class="tag-badge paid">${s}</span>`;
  if (s === 'PENDING' || s === 'ON_LEAVE') return `<span class="tag-badge pending">${s}</span>`;
  if (s === 'PROCESSING') return `<span class="tag-badge processing">${s}</span>`;
  return `<span class="tag-badge">${s}</span>`;
}

function getTierTag(type) {
  const t = String(type).toUpperCase();
  if (t === 'FULL_TIME') return `<span class="tag-badge full-time">Salaried</span>`;
  if (t === 'MANAGER') return `<span class="tag-badge manager">Executive</span>`;
  if (t === 'PART_TIME') return `<span class="tag-badge part-time">Hourly</span>`;
  if (t === 'CONTRACTOR') return `<span class="tag-badge contractor">Contractor</span>`;
  return `<span class="tag-badge">${t}</span>`;
}

function formatCycleName(cycle) {
  if (!cycle || cycle === 'ALL') return 'Global Fiscal';
  const parts = cycle.split('-');
  const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  const m = parseInt(parts[1], 10) - 1;
  return `${months[m] || ''} ${parts[0]}`;
}

function showToast(msg, type = 'info') {
  const hub = document.getElementById('neoToastHub');
  const toast = document.createElement('div');
  toast.className = 'neo-toast';
  toast.innerHTML = `
    <span style="color: var(--accent-primary); font-weight:800;">●</span>
    <span>${msg}</span>
  `;
  hub.appendChild(toast);
  setTimeout(() => toast.remove(), 3200);
}

function convertAmountToWords(num) {
  if (num === 0) return 'Zero Only';
  const a = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine', 'Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];
  const b = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

  function inWords(n) {
    if (n < 20) return a[n];
    const digit = n % 10;
    if (n < 100) return b[Math.floor(n / 10)] + (digit !== 0 ? ' ' + a[digit] : '');
    if (n < 1000) return inWords(Math.floor(n / 100)) + ' Hundred' + (n % 100 !== 0 ? ' ' + inWords(n % 100) : '');
    if (n < 100000) return inWords(Math.floor(n / 1000)) + ' Thousand' + (n % 1000 !== 0 ? ' ' + inWords(n % 1000) : '');
    if (n < 10000000) return inWords(Math.floor(n / 100000)) + ' Lakh' + (n % 100000 !== 0 ? ' ' + inWords(n % 100000) : '');
    return inWords(Math.floor(n / 10000000)) + ' Crore' + (n % 10000000 !== 0 ? ' ' + inWords(n % 10000000) : '');
  }

  return inWords(num).trim() + ' Only';
}
