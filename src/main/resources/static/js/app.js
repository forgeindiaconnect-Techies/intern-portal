/* app.js - global helpers */

const THEME_KEY = 'internPortalTheme';

function applyTheme(theme) {
  const nextTheme = theme === 'light' ? 'light' : 'dark';
  document.body.dataset.theme = nextTheme;
  document.querySelectorAll('[data-theme-toggle]').forEach(btn => {
    const showSun = nextTheme !== 'light';
    btn.setAttribute('aria-label', nextTheme === 'light' ? 'Switch to dark mode' : 'Switch to light mode');
    btn.setAttribute('title', nextTheme === 'light' ? 'Dark mode' : 'Light mode');
    btn.classList.toggle('sun-mode', showSun);
    btn.classList.toggle('moon-mode', !showSun);
    btn.innerHTML = nextTheme === 'light' ? '<i class="fas fa-moon"></i>' : '<i class="fas fa-sun"></i>';
  });
}

function toggleTheme() {
  const current = document.body.dataset.theme === 'light' ? 'light' : 'dark';
  const nextTheme = current === 'light' ? 'dark' : 'light';
  localStorage.setItem(THEME_KEY, nextTheme);
  applyTheme(nextTheme);
}

function installThemeToggle() {
  applyTheme(localStorage.getItem(THEME_KEY) || 'dark');
  document.querySelectorAll('.header-actions').forEach(actions => {
    if (actions.querySelector('[data-theme-toggle]')) return;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'theme-toggle';
    btn.dataset.themeToggle = 'true';
    btn.addEventListener('click', toggleTheme);
    actions.prepend(btn);
  });
  if (!document.querySelector('[data-theme-toggle]')) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'theme-toggle theme-toggle-floating';
    btn.dataset.themeToggle = 'true';
    btn.addEventListener('click', toggleTheme);
    document.body.appendChild(btn);
  }
  applyTheme(document.body.dataset.theme);
}

installThemeToggle();

function toggleSidebar() {
  document.querySelector('.sidebar')?.classList.toggle('open');
  document.getElementById('overlay')?.classList.toggle('show');
}

function closeSidebar() {
  document.querySelector('.sidebar')?.classList.remove('open');
  document.getElementById('overlay')?.classList.remove('show');
}

(function () {
  const btn = document.getElementById('menuBtn');
  if (btn) btn.style.display = window.innerWidth <= 992 ? 'block' : 'none';
  window.addEventListener('resize', () => {
    if (btn) btn.style.display = window.innerWidth <= 992 ? 'block' : 'none';
    if (window.innerWidth > 992) closeSidebar();
  });
})();

document.querySelectorAll('.alert-glass').forEach(el => {
  setTimeout(() => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(-8px)';
    el.style.transition = 'all .4s';
    setTimeout(() => el.remove(), 400);
  }, 5000);
});

const QUOTES = [
  'Coffee first, coding later.',
  'Intern today, team lead tomorrow.',
  'Every bug teaches a lesson.',
  'Small daily progress becomes strong skill.',
  'Clean code is quiet confidence.',
  'First solve the problem. Then write the code.'
];

const quoteEl = document.getElementById('randomQuote');
if (quoteEl) quoteEl.textContent = QUOTES[Math.floor(Math.random() * QUOTES.length)];

document.querySelectorAll('input[type=file][accept*=image]').forEach(inp => {
  inp.addEventListener('change', function () {
    const preview = document.getElementById(this.dataset.preview || 'imgPreview');
    if (preview && this.files[0]) {
      const reader = new FileReader();
      reader.onload = e => {
        preview.src = e.target.result;
        preview.style.display = 'block';
      };
      reader.readAsDataURL(this.files[0]);
    }
  });
});
