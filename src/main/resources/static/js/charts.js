/* charts.js - analytics page */
document.addEventListener('DOMContentLoaded', () => {
  const ctx = document.getElementById('analyticsMainChart');
  if (!ctx) return;

  const labels = typeof analyticsLabels !== 'undefined' ? analyticsLabels : [];
  const values = typeof analyticsValues !== 'undefined' ? analyticsValues : [];

  new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Present Students',
        data: values,
        backgroundColor: 'rgba(245,197,66,0.58)',
        borderColor: '#f5c542',
        borderWidth: 2,
        borderRadius: 10,
        maxBarThickness: 90
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { labels: { color: '#94a3b8' } }
      },
      scales: {
        x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.04)' } },
        y: { beginAtZero: true, ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.04)' } }
      }
    }
  });
});
