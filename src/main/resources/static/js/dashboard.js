/* dashboard.js - Chart.js charts */

document.addEventListener('DOMContentLoaded', () => {
  const trendCtx = document.getElementById('attendanceChart');
  if (trendCtx) {
    const labels = typeof analyticsLabels !== 'undefined' ? analyticsLabels : [];
    const values = typeof analyticsValues !== 'undefined' ? analyticsValues : [];
    new Chart(trendCtx, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Present',
          data: values,
          borderColor: '#f5c542',
          backgroundColor: 'rgba(245,197,66,0.14)',
          borderWidth: 2.5,
          pointBackgroundColor: '#ffe08a',
          pointRadius: 4,
          tension: 0.4,
          fill: true
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { labels: { color: '#94a3b8', font: { size: 12 } } } },
        scales: {
          x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.04)' } },
          y: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.04)' }, beginAtZero: true }
        }
      }
    });
  }

  const donutCtx = document.getElementById('donutChart');
  if (donutCtx) {
    const present = typeof presentToday !== 'undefined' ? presentToday : 0;
    const absent = typeof absentToday !== 'undefined' ? absentToday : 0;
    new Chart(donutCtx, {
      type: 'doughnut',
      data: {
        labels: ['Present', 'Absent'],
        datasets: [{
          data: [present, absent],
          backgroundColor: ['rgba(245,197,66,0.82)', 'rgba(239,68,68,0.62)'],
          borderColor: ['#f5c542', '#ef4444'],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        cutout: '70%',
        plugins: {
          legend: { position: 'bottom', labels: { color: '#94a3b8', font: { size: 11 }, padding: 16 } }
        }
      }
    });
  }

  const barCtx = document.getElementById('attendanceBar');
  if (barCtx) {
    const pct = typeof attendancePct !== 'undefined' ? attendancePct : 0;
    new Chart(barCtx, {
      type: 'bar',
      data: {
        labels: ['Your Attendance'],
        datasets: [{
          data: [pct],
          backgroundColor: ['rgba(245,197,66,0.7)'],
          borderColor: ['#f5c542'],
          borderWidth: 2,
          borderRadius: 8
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: { max: 100, ticks: { color: '#64748b', callback: v => v + '%' }, grid: { color: 'rgba(255,255,255,0.04)' } },
          y: { ticks: { color: '#94a3b8' }, grid: { display: false } }
        }
      }
    });
  }
});
