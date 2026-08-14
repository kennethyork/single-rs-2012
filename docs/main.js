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
const highscoreConnect = document.querySelector('#highscore-connect');
const highscoreFallback = document.querySelector('#highscore-fallback');
const highscoreSkill = document.querySelector('#highscore-skill');
const highscoreSearch = document.querySelector('#highscore-search');
const highscoreSummary = document.querySelector('#highscore-summary');
const highscoreResults = document.querySelector('#highscore-results');
const highscoreBody = document.querySelector('#highscore-body');
const highscoreProfile = document.querySelector('#highscore-profile');
const highscoreProfileName = document.querySelector('#highscore-profile-name');
const highscoreProfileSummary = document.querySelector('#highscore-profile-summary');
const highscoreProfileSkills = document.querySelector('#highscore-profile-skills');
const highscoreProfileClose = document.querySelector('#highscore-profile-close');
let highscoreData = null;
let selectedHighscoreUsername = null;
let liveHighscoreHandle = null;
let highscoreModifiedAt = 0;
let highscoreRefreshTimer = null;

const numberFormat = new Intl.NumberFormat();
const supportsLiveHighscores = 'showOpenFilePicker' in window && 'indexedDB' in window;
const highscoreCacheKey = 'single-rs-2012-highscores-cache-v1';
const highscoreViewKey = 'single-rs-2012-highscores-view-v1';

function readHighscoreView() {
  try {
    const saved = JSON.parse(localStorage.getItem(highscoreViewKey));
    return saved && typeof saved === 'object' ? saved : {};
  } catch {
    return {};
  }
}

function saveHighscoreView() {
  try {
    localStorage.setItem(highscoreViewKey, JSON.stringify({
      search: highscoreSearch.value,
      skill: highscoreSkill.value,
      selectedUsername: selectedHighscoreUsername
    }));
  } catch {
    // The rankings still work when private browsing disables local storage.
  }
}

function validateHighscoreData(parsed) {
  if (!parsed || parsed.formatVersion !== 1 || !Array.isArray(parsed.skills) || !Array.isArray(parsed.entries)) {
    throw new Error('Unsupported highscore export format');
  }
  parsed.entries.forEach(entry => {
    if (!entry || typeof entry.displayName !== 'string' || typeof entry.username !== 'string' ||
        !Array.isArray(entry.levels) || !Array.isArray(entry.xp) ||
        entry.levels.length < parsed.skills.length || entry.xp.length < parsed.skills.length) {
      throw new Error('Invalid highscore entry');
    }
  });
  return parsed;
}

function applyHighscoreData(parsed, summary) {
  const savedView = highscoreData ? {
    search: highscoreSearch.value,
    skill: highscoreSkill.value,
    selectedUsername: selectedHighscoreUsername
  } : readHighscoreView();
  highscoreData = parsed;
  highscoreSkill.replaceChildren(new Option('Overall', 'overall'), ...parsed.skills.map((name, index) => new Option(name, String(index))));
  if ([...highscoreSkill.options].some(option => option.value === savedView.skill)) {
    highscoreSkill.value = savedView.skill;
  }
  highscoreSearch.value = typeof savedView.search === 'string' ? savedView.search : '';
  selectedHighscoreUsername = typeof savedView.selectedUsername === 'string' ? savedView.selectedUsername : null;
  highscoreSkill.disabled = false;
  highscoreSearch.disabled = false;
  highscoreSummary.textContent = summary;
  renderHighscores();
  if (selectedHighscoreUsername) {
    const selected = parsed.entries.find(entry => entry.username === selectedHighscoreUsername);
    if (selected) renderHighscoreProfile(selected, false);
    else {
      selectedHighscoreUsername = null;
      highscoreProfile.hidden = true;
      saveHighscoreView();
    }
  }
}

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
  const query = highscoreSearch.value.trim().toLocaleLowerCase();
  const matches = query
    ? ranked.filter(entry => entry.displayName.toLocaleLowerCase().includes(query) || entry.username.toLocaleLowerCase().includes(query))
    : ranked;
  highscoreResults.textContent = query
    ? `${matches.length} player${matches.length === 1 ? '' : 's'} match “${highscoreSearch.value.trim()}”.`
    : `Showing the top ${Math.min(matches.length, 100)} of ${matches.length} players. Click anyone to view every skill.`;

  if (matches.length === 0) {
    highscoreBody.innerHTML = '<tr><td colspan="5" class="highscore-empty">No players match that search.</td></tr>';
    return;
  }

  highscoreBody.replaceChildren(...matches.slice(0, 100).map(entry => {
    const row = document.createElement('tr');
    row.className = 'highscore-row';
    row.tabIndex = 0;
    row.setAttribute('role', 'button');
    row.setAttribute('aria-label', `View all stats for ${entry.displayName}`);
    const values = [
      ranked.indexOf(entry) + 1,
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
    const openProfile = () => renderHighscoreProfile(entry);
    row.addEventListener('click', openProfile);
    row.addEventListener('keydown', event => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        openProfile();
      }
    });
    return row;
  }));
}

function renderHighscoreProfile(entry, scroll = true) {
  selectedHighscoreUsername = entry.username;
  saveHighscoreView();
  highscoreProfileName.textContent = entry.displayName;
  highscoreProfileSummary.textContent = `${entry.bot ? 'Bot' : 'Character'} · Combat ${entry.combatLevel} · Total level ${numberFormat.format(entry.totalLevel)} · Total XP ${numberFormat.format(entry.totalXp)}`;
  highscoreProfileSkills.replaceChildren(...highscoreData.skills.map((skill, index) => {
    const row = document.createElement('tr');
    [skill, entry.levels[index], numberFormat.format(entry.xp[index])].forEach(value => {
      const cell = document.createElement('td');
      cell.textContent = value;
      row.append(cell);
    });
    return row;
  }));
  highscoreProfile.hidden = false;
  if (scroll) highscoreProfile.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

async function loadHighscoreFile(file, live = false) {
  try {
    const parsed = validateHighscoreData(JSON.parse(await file.text()));
    try {
      localStorage.setItem(highscoreCacheKey, JSON.stringify(parsed));
    } catch {
      // A denied cache must not stop the selected file from loading.
    }
    const generated = new Date(parsed.generatedAt);
    const generatedLabel = Number.isNaN(generated.valueOf()) ? 'unknown time' : generated.toLocaleString();
    const summary = live
      ? `Live: ${parsed.entries.length} local entries, last game update ${generatedLabel}. Refreshing automatically; nothing is uploaded.`
      : `Loaded ${parsed.entries.length} local entries generated ${generatedLabel}. Nothing was uploaded.`;
    highscoreModifiedAt = file.lastModified;
    applyHighscoreData(parsed, summary);
  } catch (error) {
    highscoreSummary.textContent = `Could not load ${file.name}: ${error.message}`;
    if (!highscoreData) {
      highscoreSkill.disabled = true;
      highscoreSearch.disabled = true;
      highscoreResults.textContent = '';
      highscoreProfile.hidden = true;
      highscoreBody.innerHTML = '<tr><td colspan="5" class="highscore-empty">Choose a valid Single RS 2012 highscore export.</td></tr>';
    }
  }
}

function restoreCachedHighscores() {
  try {
    const cached = localStorage.getItem(highscoreCacheKey);
    if (!cached) return false;
    const parsed = validateHighscoreData(JSON.parse(cached));
    const generated = new Date(parsed.generatedAt);
    const generatedLabel = Number.isNaN(generated.valueOf()) ? 'unknown time' : generated.toLocaleString();
    applyHighscoreData(parsed, `Showing ${parsed.entries.length} saved local entries from ${generatedLabel}. Reconnecting live updates in the background.`);
    return true;
  } catch {
    try {
      localStorage.removeItem(highscoreCacheKey);
    } catch {
      // Ignore browsers that deny local storage entirely.
    }
    return false;
  }
}

function openHighscoreDatabase() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('single-rs-2012-highscores', 1);
    request.onupgradeneeded = () => request.result.createObjectStore('files');
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function saveHighscoreHandle(handle) {
  const database = await openHighscoreDatabase();
  await new Promise((resolve, reject) => {
    const request = database.transaction('files', 'readwrite').objectStore('files').put(handle, 'live-export');
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
  database.close();
}

async function getSavedHighscoreHandle() {
  const database = await openHighscoreDatabase();
  const handle = await new Promise((resolve, reject) => {
    const request = database.transaction('files').objectStore('files').get('live-export');
    request.onsuccess = () => resolve(request.result || null);
    request.onerror = () => reject(request.error);
  });
  database.close();
  return handle;
}

async function refreshLiveHighscores(requestAccess = false) {
  if (!liveHighscoreHandle) return;
  let permission = await liveHighscoreHandle.queryPermission({ mode: 'read' });
  if (permission !== 'granted' && requestAccess) {
    permission = await liveHighscoreHandle.requestPermission({ mode: 'read' });
  }
  if (permission !== 'granted') {
    highscoreSummary.textContent = highscoreData
      ? 'Showing saved stats. Click Connect live highscores to resume automatic updates.'
      : 'Click Connect live highscores to restore access to the remembered file.';
    return;
  }
  const file = await liveHighscoreHandle.getFile();
  if (!highscoreData || file.lastModified !== highscoreModifiedAt) await loadHighscoreFile(file, true);
}

function startLiveHighscoreRefresh() {
  window.clearInterval(highscoreRefreshTimer);
  highscoreRefreshTimer = window.setInterval(() => {
    refreshLiveHighscores().catch(error => {
      highscoreSummary.textContent = `Live highscore refresh failed: ${error.message}`;
    });
  }, 10_000);
}

highscoreConnect.addEventListener('click', async () => {
  try {
    if (!supportsLiveHighscores) {
      highscoreFile.click();
      return;
    }
    if (!liveHighscoreHandle) {
      [liveHighscoreHandle] = await window.showOpenFilePicker({
        multiple: false,
        types: [{ description: 'Single RS 2012 highscores', accept: { 'application/json': ['.json'] } }]
      });
      await saveHighscoreHandle(liveHighscoreHandle);
    }
    await refreshLiveHighscores(true);
    startLiveHighscoreRefresh();
  } catch (error) {
    if (error.name !== 'AbortError') highscoreSummary.textContent = `Could not connect live highscores: ${error.message}`;
  }
});

highscoreFile.addEventListener('change', async () => {
  const file = highscoreFile.files[0];
  if (file) await loadHighscoreFile(file);
});

highscoreSkill.addEventListener('change', () => {
  saveHighscoreView();
  renderHighscores();
});
highscoreSearch.addEventListener('input', () => {
  saveHighscoreView();
  renderHighscores();
});
highscoreProfileClose.addEventListener('click', () => {
  selectedHighscoreUsername = null;
  highscoreProfile.hidden = true;
  saveHighscoreView();
});

restoreCachedHighscores();

if (supportsLiveHighscores) {
  highscoreFallback.hidden = true;
  getSavedHighscoreHandle().then(async handle => {
    if (!handle) return;
    liveHighscoreHandle = handle;
    await refreshLiveHighscores();
    startLiveHighscoreRefresh();
  }).catch(() => {
    if (!highscoreData) highscoreSummary.textContent = 'Connect the local highscore file to enable automatic updates.';
  });
} else {
  highscoreConnect.textContent = 'Choose highscore export';
  highscoreFallback.hidden = true;
}

document.addEventListener('visibilitychange', () => {
  if (!document.hidden && liveHighscoreHandle) refreshLiveHighscores().catch(() => {});
});
