/* games.js – Developer Fun Zone */

// ── shared state ────────────────────────────────────────────────
let bugScore = 0, syntaxScore = 0;

function updateTotalScore() {
  const el = document.getElementById('totalScore');
  if (el) el.textContent = bugScore + syntaxScore;
  localStorage.setItem('devScore', bugScore + syntaxScore);
}

function switchGame(id, btn) {
  document.querySelectorAll('.game-panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.game-tab-btn').forEach(b => b.classList.remove('active'));
  document.getElementById(id).classList.add('active');
  btn.classList.add('active');
}

// ── Bug Hunter ───────────────────────────────────────────────────
const BUGS = [
  { code: `for (int i = 0; i <= arr.length; i++) {\n  System.out.println(arr[i]);\n}`, answer: 1, options: ['Missing semicolon', 'i <= arr.length (off-by-one)', 'Wrong variable name', 'Missing return'] },
  { code: `String s = null;\nif (s.equals("hello")) {\n  System.out.println("hi");\n}`, answer: 0, options: ['NullPointerException on s.equals()', 'Wrong import', 'Missing semicolon', 'Syntax error'] },
  { code: `int x = 5;\nif (x = 10) {\n  System.out.println("ten");\n}`, answer: 2, options: ['Missing braces', 'Wrong println', 'Assignment instead of == comparison', 'Missing return type'] },
  { code: `public int add(int a, int b) {\n  int sum = a + b;\n}`, answer: 3, options: ['Wrong parameter types', 'Missing import', 'Missing semicolon', 'Missing return statement'] },
  { code: `List<String> names = new ArrayList<>();\nnames.add("Alice");\nnames.get(5);`, answer: 1, options: ['Wrong List type', 'IndexOutOfBoundsException', 'Missing import', 'Wrong method name'] }
];
let bugIdx = 0;

function renderBug() {
  const bug = BUGS[bugIdx % BUGS.length];
  document.getElementById('bugCode').textContent = bug.code;
  const opts = document.getElementById('bugOptions');
  opts.innerHTML = '';
  bug.options.forEach((opt, i) => {
    const btn = document.createElement('button');
    btn.className = 'game-option';
    btn.textContent = opt;
    btn.onclick = () => checkBug(i, bug.answer, opts.children);
    opts.appendChild(btn);
  });
}

function checkBug(chosen, correct, btns) {
  Array.from(btns).forEach(b => b.onclick = null);
  btns[correct].classList.add('correct');
  if (chosen === correct) { bugScore++; document.getElementById('bugScore').textContent = bugScore; updateTotalScore(); }
  else btns[chosen].classList.add('wrong');
}

function nextBug() { bugIdx++; renderBug(); }

// ── Syntax Challenge ─────────────────────────────────────────────
const SYNTAX = [
  { q: 'Correct way to declare a Java list?', answer: 0, opts: ['List<String> list = new ArrayList<>();', 'list = new List<String>();', 'ArrayList list<String> = new();', 'new List("String")'] },
  { q: 'Which is valid Python list comprehension?', answer: 2, opts: ['[for x in range(5)]', '(x*2 x in range(5))', '[x*2 for x in range(5)]', 'list x*2 of range(5)'] },
  { q: 'Which SQL statement retrieves data?', answer: 1, opts: ['FETCH', 'SELECT', 'GET', 'PULL'] },
  { q: 'JavaScript async/await – correct syntax?', answer: 3, opts: ['async result = fetch(url)', 'result = await async fetch(url)', 'await async { fetch(url) }', 'const result = await fetch(url)'] },
  { q: 'Java interface default method keyword?', answer: 0, opts: ['default', 'virtual', 'base', 'common'] }
];
let syntaxIdx = 0;

function renderSyntax() {
  const s = SYNTAX[syntaxIdx % SYNTAX.length];
  document.getElementById('syntaxQuestion').textContent = s.q;
  const opts = document.getElementById('syntaxOptions');
  opts.innerHTML = '';
  s.opts.forEach((opt, i) => {
    const btn = document.createElement('button');
    btn.className = 'game-option';
    btn.textContent = opt;
    btn.onclick = () => checkSyntax(i, s.answer, opts.children);
    opts.appendChild(btn);
  });
}

function checkSyntax(chosen, correct, btns) {
  Array.from(btns).forEach(b => b.onclick = null);
  btns[correct].classList.add('correct');
  if (chosen === correct) { syntaxScore++; document.getElementById('syntaxScore').textContent = syntaxScore; updateTotalScore(); }
  else btns[chosen].classList.add('wrong');
}

function nextSyntax() { syntaxIdx++; renderSyntax(); }

// ── Memory Match ──────────────────────────────────────────────────
const PAIRS = ['☕', '🐍', '☕', '🐍', '⚡', '⚡', '🦀', '🦀', '🔷', '🔷', '🌊', '🌊'];
let flipped = [], matched = 0, moves = 0, memCards = [];

function initMemory() {
  flipped = []; matched = 0; moves = 0;
  document.getElementById('moveCount').textContent = 0;
  document.getElementById('matchCount').textContent = 0;
  const grid = document.getElementById('memoryGrid');
  if (!grid) return;
  grid.innerHTML = '';
  const shuffled = [...PAIRS].sort(() => Math.random() - 0.5);
  memCards = [];
  shuffled.forEach((sym, idx) => {
    const card = document.createElement('div');
    card.className = 'memory-card';
    card.dataset.sym = sym;
    card.dataset.idx = idx;
    card.innerHTML = `<span class="card-face">${sym}</span>`;
    card.addEventListener('click', flipCard);
    grid.appendChild(card);
    memCards.push(card);
  });
}

function flipCard() {
  if (this.classList.contains('flipped') || this.classList.contains('matched') || flipped.length === 2) return;
  this.classList.add('flipped');
  flipped.push(this);
  if (flipped.length === 2) {
    moves++;
    document.getElementById('moveCount').textContent = moves;
    if (flipped[0].dataset.sym === flipped[1].dataset.sym) {
      flipped.forEach(c => c.classList.add('matched'));
      matched++;
      document.getElementById('matchCount').textContent = matched;
      flipped = [];
    } else {
      const pair = [...flipped];
      setTimeout(() => { pair.forEach(c => c.classList.remove('flipped')); flipped = []; }, 900);
    }
  }
}

// ── Init ─────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  renderBug();
  renderSyntax();
  initMemory();
  const stored = parseInt(localStorage.getItem('devScore') || '0');
  const el = document.getElementById('totalScore');
  if (el && stored) el.textContent = stored;
});
