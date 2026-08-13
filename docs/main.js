const toast = document.querySelector('.toast');

document.querySelectorAll('[data-copy]').forEach(button => {
  button.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(button.dataset.copy);
      toast.textContent = `Copied ${button.dataset.copy}`;
    } catch {
      toast.textContent = button.dataset.copy;
    }
    toast.classList.add('show');
    window.setTimeout(() => toast.classList.remove('show'), 1800);
  });
});

// Screenshots are optional: swap in a placeholder until the PNGs are added to docs/.
document.querySelectorAll('.gallery img').forEach(img => {
  const placeholder = () => {
    const box = document.createElement('div');
    box.className = 'shot-missing';
    box.textContent = `Add ${img.getAttribute('src')} to docs/`;
    img.replaceWith(box);
  };
  img.addEventListener('error', placeholder);
  // The image may have already failed before this script ran.
  if (img.complete && img.naturalWidth === 0) placeholder();
});

document.querySelector('#year').textContent = new Date().getFullYear();
