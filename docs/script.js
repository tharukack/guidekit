const header = document.querySelector('[data-header]');
const nav = document.querySelector('[data-nav]');
const navToggle = document.querySelector('[data-nav-toggle]');

const setHeaderState = () => {
  header?.classList.toggle('scrolled', window.scrollY > 16);
};

setHeaderState();
window.addEventListener('scroll', setHeaderState, { passive: true });

navToggle?.addEventListener('click', () => {
  const isOpen = nav?.classList.toggle('open') ?? false;
  navToggle.setAttribute('aria-expanded', String(isOpen));
  navToggle.setAttribute('aria-label', isOpen ? 'Close navigation' : 'Open navigation');
  document.body.classList.toggle('nav-open', isOpen);
});

nav?.querySelectorAll('a').forEach((link) => {
  link.addEventListener('click', () => {
    nav.classList.remove('open');
    navToggle?.setAttribute('aria-expanded', 'false');
    navToggle?.setAttribute('aria-label', 'Open navigation');
    document.body.classList.remove('nav-open');
  });
});

const copyText = async (button, text) => {
  const label = button.querySelector('[data-copy-label]');
  const original = label?.textContent ?? 'Copy';

  try {
    await navigator.clipboard.writeText(text.trim());
    if (label) label.textContent = 'Copied!';
    button.classList.add('copied');
  } catch {
    if (label) label.textContent = 'Select to copy';
  }

  window.setTimeout(() => {
    if (label) label.textContent = original;
    button.classList.remove('copied');
  }, 1800);
};

document.querySelectorAll('[data-copy], [data-copy-target]').forEach((button) => {
  button.addEventListener('click', () => {
    const target = button.dataset.copyTarget
      ? document.querySelector(button.dataset.copyTarget)?.textContent
      : button.dataset.copy;
    if (target) copyText(button, target);
  });
});

const kotlinKeywords = new Set([
  'as', 'break', 'by', 'class', 'continue', 'data', 'do', 'else', 'false',
  'for', 'fun', 'if', 'in', 'interface', 'is', 'null', 'object', 'package',
  'private', 'public', 'return', 'sealed', 'true', 'val', 'var', 'when', 'while',
]);

const escapeCode = (value) => value.replace(/[&<>]/g, (character) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
})[character]);

document.querySelectorAll('pre code').forEach((code) => {
  if (code.children.length > 0) return;

  const source = code.textContent ?? '';
  const tokenPattern = /\/\/[^\n]*|"(?:\\.|[^"\\])*"|@\w+|\b\d+(?:\.\d+)?(?:f|dp)?\b|\b[A-Za-z_]\w*\b|[\s\S]/g;
  code.innerHTML = Array.from(source.matchAll(tokenPattern), (match) => {
    const token = match[0];
    let tokenClass = '';

    if (token.startsWith('//')) tokenClass = 'c-comment';
    else if (token.startsWith('"')) tokenClass = 'c-string';
    else if (token.startsWith('@')) tokenClass = 'c-annotation';
    else if (/^\d/.test(token)) tokenClass = 'c-number';
    else if (['true', 'false', 'null'].includes(token)) tokenClass = 'c-constant';
    else if (kotlinKeywords.has(token)) tokenClass = 'c-keyword';
    else if (/^[A-Z]/.test(token)) tokenClass = 'c-type';
    else if (/^[A-Za-z_]\w*$/.test(token) && /^\s*=/.test(source.slice((match.index ?? 0) + token.length))) tokenClass = 'c-property';

    const escaped = escapeCode(token);
    return tokenClass ? `<span class="${tokenClass}">${escaped}</span>` : escaped;
  }).join('');
});

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const reveals = document.querySelectorAll('.reveal');

if (reduceMotion || !('IntersectionObserver' in window)) {
  reveals.forEach((element) => element.classList.add('visible'));
} else {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12, rootMargin: '0px 0px -35px' });

  reveals.forEach((element) => observer.observe(element));
}

document.querySelectorAll('[data-year]').forEach((element) => {
  element.textContent = String(new Date().getFullYear());
});
