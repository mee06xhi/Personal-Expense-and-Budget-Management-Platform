// ── SHARED CHART DEFAULTS ──────────────────────────────
Chart.defaults.color = '#888';
Chart.defaults.borderColor = '#1e1e2a';
Chart.defaults.font.family = 'Georgia, serif';

const categoryColors = {
    'Bills':                    '#4E79A7',
    'Food & Dining':            '#F28E2B',
    'Shopping':                 '#B07AA1',
    'Healthcare':               '#59A14F',
    'Housing':                  '#E15759',
    'Groceries':                '#76B7B2',
    'Education':                '#EDC948',
    'Transportation & Travel':  '#9C755F',
    'Miscellaneous':            '#BAB0AC',
    'Other':                    '#79706E'
};

// keep references so we can destroy and redraw on month change.
let donutInstance = null;
let lineInstance  = null;

// SET MONTH PICKER DEFAULT
const [initYear, initMonth] = currentMonth.split('-');

// automatically includes new years without any code change.
const currentYear = parseInt(initYear);
for (let y = 2020; y <= currentYear; y++) {
    const opt = document.createElement('option');
    opt.value = y;
    opt.textContent = y;
    yearPicker.appendChild(opt);
}

yearPicker.value  = initYear;
monthPicker.value = initMonth;

// DRAW FUNCTIONS

function drawDonut(spendingByCategory) {
    // Destroy previous chart if it exists (required before redrawing)
    if (donutInstance) donutInstance.destroy();

    const labels = Object.keys(spendingByCategory);
    const values = Object.values(spendingByCategory);
    const colors = labels.map(l => categoryColors[l] || '#e07b54');

    const canvas  = document.getElementById('donutChart');
    const noData  = document.getElementById('donutNoData');

    if (labels.length === 0) {
        canvas.style.display = 'none';
        noData.style.display = 'block';
        return;
    }

    canvas.style.display = 'block';
    noData.style.display = 'none';

    donutInstance = new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderColor: '#15151d',
                borderWidth: 3,
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { padding: 16, boxWidth: 12, color: '#c8b99a' }
                },
                tooltip: {
                    callbacks: {
                        label: ctx => ' ₹' + ctx.parsed.toLocaleString('en-IN')
                    }
                }
            }
        }
    });
}

function drawLine(dailySpending) {
    if (lineInstance) lineInstance.destroy();

    const labels = Object.keys(dailySpending);
    const values = Object.values(dailySpending);

    const canvas = document.getElementById('lineChart');
    const noData = document.getElementById('lineNoData');

    if (labels.length === 0) {
        canvas.style.display = 'none';
        noData.style.display = 'block';
        return;
    }

    canvas.style.display = 'block';
    noData.style.display = 'none';

    lineInstance = new Chart(canvas, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: 'Daily Spending',
                data: values,
                borderColor: '#e07b54',
                backgroundColor: 'rgba(224,123,84,0.12)',
                borderWidth: 2,
                pointBackgroundColor: '#e07b54',
                pointRadius: 4,
                pointHoverRadius: 6,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ' ₹' + ctx.parsed.y.toLocaleString('en-IN')
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: '#1e1e2a' },
                    ticks: {
                        color: '#555',
                        maxTicksLimit: 8,
                        // Show day number only
                        callback: (val, idx) => labels[idx].slice(8)
                    }
                },
                y: {
                    grid: { color: '#1e1e2a' },
                    ticks: {
                        color: '#555',
                        callback: val => '₹' + val.toLocaleString('en-IN')
                    },
                    beginAtZero: true
                }
            }
        }
    });
}

function onPickerChange() {
    updateViewDetailsLink(); 
    const year     = yearPicker.value;
    const month    = monthPicker.value;
    const selected = `${year}-${month}`;

    fetch(`/api/chart-data?month=${selected}`)
        .then(res => res.json())
        .then(data => {
            //  Update charts
            drawDonut(data.spendingByCategory);
            drawLine(data.dailySpending);

            //  Update stat cards 
            document.getElementById('statTotal').textContent =
                data.total.toLocaleString('en-IN');
            document.getElementById('statBudget').textContent =
                data.budget.toLocaleString('en-IN');
            document.getElementById('statRemaining').textContent =
                data.remaining.toLocaleString('en-IN');

            // Toggle over/under budget colour on the remaining card
            const remainingCard = document.getElementById('remainingCard');
            remainingCard.classList.remove('over-budget', 'under-budget');
            remainingCard.classList.add(data.remaining < 0 ? 'over-budget' : 'under-budget');
            updateProgressBar(data.total, data.budget);
        })
        .catch(err => console.error('Failed to fetch chart data:', err));
}

function updateProgressBar(total, budget) {
    const progressFill  = document.getElementById('progressFill');
    const progressLabel = document.getElementById('progressLabel');
    const alertOver     = document.getElementById('alertOver');
    const alertWarn     = document.getElementById('alertWarn');

    if (budget <= 0) {
        progressFill.style.width = '0%';
        progressFill.style.background = '#e07b54';
        progressLabel.textContent = 'No budget set';
        alertOver.style.display = 'none';
        alertWarn.style.display = 'none';
        return;
    }

    const pct = Math.min(total / budget * 100, 100);
    progressFill.style.width = pct + '%';
    progressFill.style.background = total > budget ? '#e06b8b' : '#e07b54';
    progressLabel.textContent = Math.round(total / budget * 100) + '%';

    alertOver.style.display = (total >= budget) ? 'block' : 'none';
    alertWarn.style.display = (total < budget && total >= budget * 0.8) ? 'block' : 'none';
}

function updateViewDetailsLink() {
    const year  = yearPicker.value;
    const month = monthPicker.value;
    // fromDate and toDate bracket the entire selected month
    const from  = `${year}-${month}-01`;
    const daysInMonth = new Date(year, parseInt(month), 0).getDate();
    const to    = `${year}-${month}-${String(daysInMonth).padStart(2, '0')}`;
    document.getElementById('viewDetailsBtn').href =
        `/expenses?fromDate=${from}&toDate=${to}`;
}

drawDonut(spendingByCategory);
drawLine(dailySpending);
updateProgressBar(initTotal, initBudget);
updateViewDetailsLink(); 

yearPicker.addEventListener('change', onPickerChange);
monthPicker.addEventListener('change', onPickerChange);