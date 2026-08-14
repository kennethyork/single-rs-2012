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

const highscoreFile = document.querySelector('#highscore-file');
const highscoreSkill = document.querySelector('#highscore-skill');
const highscoreSummary = document.querySelector('#highscore-summary');
const highscoreBody = document.querySelector('#highscore-body');
let highscoreData = null;

const numberFormat = new Intl.NumberFormat();

function renderHighscores() {
  if (!highscoreData) return;
  const selection = highscoreSkill.value;
  const skillIndex = selection === 'overall' ? -1 : Number(selection);
  const ranked = [...highscoreData.entries].sort((left, right) => {
    const leftLevel = skillIndex < 0 ? left.totalLevel : left.levels[skillIndex];
    const rightLevel = skillIndex < 0 ? right.totalLevel : right.levels[skillIndex];
    const leftXp = skillIndex < 0 ? left.totalXp : left.xp[skillIndex];
    const rightXp = skillIndex < 0 ? right.totalXp : right.xp[skillIndex];
    return rightLevel - leftLevel || rightXp - leftXp || left.displayName.localeCompare(right.displayName);
  });

  highscoreBody.replaceChildren(...ranked.slice(0, 100).map((entry, index) => {
    const row = document.createElement('tr');
    const values = [
      index + 1,
      entry.displayName,
      entry.bot ? 'Bot' : 'Character',
      skillIndex < 0 ? entry.totalLevel : entry.levels[skillIndex],
      numberFormat.format(skillIndex < 0 ? entry.totalXp : entry.xp[skillIndex])
    ];
    values.forEach(value => {
      const cell = document.createElement('td');
      cell.textContent = value;
      row.append(cell);
    });
    return row;
  }));
}

highscoreFile.addEventListener('change', async () => {
  const file = highscoreFile.files[0];
  if (!file) return;
  try {
    const parsed = JSON.parse(await file.text());
    if (parsed.formatVersion !== 1 || !Array.isArray(parsed.skills) || !Array.isArray(parsed.entries)) {
      throw new Error('Unsupported highscore export format');
    }
    parsed.entries.forEach(entry => {
      if (!entry || typeof entry.displayName !== 'string' || !Array.isArray(entry.levels) || !Array.isArray(entry.xp)) {
        throw new Error('Invalid highscore entry');
      }
    });
    highscoreData = parsed;
    highscoreSkill.replaceChildren(new Option('Overall', 'overall'), ...parsed.skills.map((name, index) => new Option(name, String(index))));
    highscoreSkill.disabled = false;
    const generated = new Date(parsed.generatedAt);
    const generatedLabel = Number.isNaN(generated.valueOf()) ? 'unknown time' : generated.toLocaleString();
    highscoreSummary.textContent = `Loaded ${parsed.entries.length} local entries generated ${generatedLabel}. Nothing was uploaded.`;
    renderHighscores();
  } catch (error) {
    highscoreData = null;
    highscoreSkill.disabled = true;
    highscoreSummary.textContent = `Could not load ${file.name}: ${error.message}`;
    highscoreBody.innerHTML = '<tr><td colspan="5" class="highscore-empty">Choose a valid Single RS 2012 highscore export.</td></tr>';
  }
});

highscoreSkill.addEventListener('change', renderHighscores);
