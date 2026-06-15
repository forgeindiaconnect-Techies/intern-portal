/* feedback.js */
document.addEventListener('DOMContentLoaded', () => {
  // Live char counter for textareas
  document.querySelectorAll('textarea[maxlength]').forEach(ta => {
    const counter = document.createElement('div');
    counter.style.cssText = 'font-size:.72rem;color:var(--text-muted);text-align:right;margin-top:4px;';
    const update = () => { counter.textContent = (ta.maxLength - ta.value.length) + ' characters left'; };
    ta.parentNode.insertBefore(counter, ta.nextSibling);
    ta.addEventListener('input', update);
    update();
  });
});
