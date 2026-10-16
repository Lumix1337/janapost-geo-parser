let currentResults = [];
let activeFilter = 'all';
let currentPage = 1;
const pageSize = 50;

document.addEventListener('DOMContentLoaded', () => {
  setupSandbox();
  setupDropZone();
  setupFilters();
  setupPagination();
  setupExport();
  setupSampleButton();
});

// 1. Live Express Parse Sandbox
function setupSandbox() {
  const input = document.getElementById('sandbox-input');
  const btn = document.getElementById('btn-parse-single');

  const executeParse = async () => {
    const raw = input.value.trim();
    if (!raw) return;

    btn.disabled = true;
    btn.textContent = 'Разбор...';

    try {
      const res = await fetch('/api/parse', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: 'live_test', raw_address: raw })
      });
      const data = await res.json();
      renderSandboxResult(data);
    } catch (e) {
      alert('Ошибка при вызове API: ' + e.message);
    } finally {
      btn.disabled = false;
      btn.textContent = 'Разобрать';
    }
  };

  btn.addEventListener('click', executeParse);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') executeParse();
  });
}

function renderSandboxResult(item) {
  const container = document.getElementById('sandbox-result');
  container.style.display = 'grid';

  const fields = [
    { label: 'Область', val: item.province },
    { label: 'Город', val: item.city },
    { label: 'Район', val: item.district },
    { label: 'ЖК / Массив', val: item.living_area },
    { label: 'Тип улицы', val: item.street_type },
    { label: 'Название улицы', val: item.street_name },
    { label: 'Дом', val: item.house },
    { label: 'Квартира', val: item.apartment },
    { label: 'Подъезд', val: item.entrance },
    { label: 'Этаж', val: item.floor },
    { label: 'Ориентир', val: item.landmark },
    { label: 'Уверенность', val: (item.confidence * 100).toFixed(0) + '%' }
  ];

  container.innerHTML = fields.map(f => `
    <div class="result-tag">
      <span class="result-tag-label">${f.label}</span>
      <span class="result-tag-value">${f.val !== null ? escapeHtml(String(f.val)) : '<span style="color:#a0aec0">null</span>'}</span>
    </div>
  `).join('');
}

// 2. File Drag and Drop & Upload
function setupDropZone() {
  const zone = document.getElementById('drop-zone');
  const fileInput = document.getElementById('file-input');

  zone.addEventListener('click', () => fileInput.click());

  zone.addEventListener('dragover', (e) => {
    e.preventDefault();
    zone.classList.add('dragover');
  });

  zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));

  zone.addEventListener('drop', (e) => {
    e.preventDefault();
    zone.classList.remove('dragover');
    if (e.dataTransfer.files.length > 0) {
      processFile(e.dataTransfer.files[0]);
    }
  });

  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      processFile(e.target.files[0]);
    }
  });
}

function processFile(file) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    try {
      const json = JSON.parse(e.target.result);
      if (!Array.isArray(json)) {
        alert('Файл должен содержать JSON массив объектов [{ id, raw_address }]');
        return;
      }
      await uploadBatch(json);
    } catch (err) {
      alert('Ошибка чтения JSON: ' + err.message);
    }
  };
  reader.readAsText(file, 'UTF-8');
}

async function uploadBatch(records) {
  const startTime = performance.now();
  const btnExport = document.getElementById('btn-export');
  btnExport.disabled = true;

  try {
    const res = await fetch('/api/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(records)
    });
    const parsed = await res.json();
    const elapsed = Math.round(performance.now() - startTime);

    currentResults = parsed;
    updateStats(parsed, elapsed);
    renderTable();
    btnExport.disabled = false;
  } catch (err) {
    alert('Ошибка при пакетной обработке: ' + err.message);
  }
}

// 3. Stats & Table
function updateStats(results, elapsedMs) {
  document.getElementById('stats-bar').style.display = 'flex';
  document.getElementById('table-controls').style.display = 'flex';
  document.getElementById('table-container').style.display = 'block';

  const total = results.length;
  const high = results.filter(r => r.confidence >= 0.85).length;
  const review = total - high;

  document.getElementById('stat-total').textContent = total;
  document.getElementById('stat-high').textContent = high;
  document.getElementById('stat-review').textContent = review;
  document.getElementById('stat-time').textContent = elapsedMs + ' мс (' + Math.round((total / (elapsedMs / 1000))) + ' зап/сек)';
}

function setupFilters() {
  const chips = document.querySelectorAll('.filter-chip');
  chips.forEach(chip => {
    chip.addEventListener('click', () => {
      chips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      activeFilter = chip.dataset.filter;
      currentPage = 1;
      renderTable();
    });
  });

  const search = document.getElementById('search-table');
  search.addEventListener('input', () => {
    currentPage = 1;
    renderTable();
  });
}

function setupPagination() {
  const btnPrev = document.getElementById('btn-prev-page');
  const btnNext = document.getElementById('btn-next-page');

  if (btnPrev && btnNext) {
    btnPrev.addEventListener('click', () => {
      if (currentPage > 1) {
        currentPage--;
        renderTable();
      }
    });

    btnNext.addEventListener('click', () => {
      const filteredCount = getFilteredResults().length;
      const totalPages = Math.max(1, Math.ceil(filteredCount / pageSize));
      if (currentPage < totalPages) {
        currentPage++;
        renderTable();
      }
    });
  }
}

function getFilteredResults() {
  const searchVal = (document.getElementById('search-table')?.value || '').toLowerCase().trim();

  return currentResults.filter(item => {
    if (activeFilter === 'high' && item.confidence < 0.85) return false;
    if (activeFilter === 'review' && item.confidence >= 0.85) return false;
    if (searchVal) {
      const match = (item.raw_address && item.raw_address.toLowerCase().includes(searchVal)) ||
                    (item.city && item.city.toLowerCase().includes(searchVal)) ||
                    (item.street_name && item.street_name.toLowerCase().includes(searchVal)) ||
                    (item.id && item.id.toLowerCase().includes(searchVal));
      if (!match) return false;
    }
    return true;
  });
}

function renderTable() {
  const tbody = document.getElementById('table-body');
  const filtered = getFilteredResults();
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));

  if (currentPage > totalPages) {
    currentPage = totalPages;
  }
  if (currentPage < 1) {
    currentPage = 1;
  }

  // Update pagination indicators
  const pageIndicator = document.getElementById('page-indicator');
  const btnPrev = document.getElementById('btn-prev-page');
  const btnNext = document.getElementById('btn-next-page');

  if (pageIndicator) {
    pageIndicator.textContent = filtered.length === 0 ? '0 из 0' : `Стр. ${currentPage} из ${totalPages}`;
  }
  if (btnPrev) {
    btnPrev.disabled = currentPage <= 1;
  }
  if (btnNext) {
    btnNext.disabled = currentPage >= totalPages;
  }

  const startIdx = (currentPage - 1) * pageSize;
  const paginated = filtered.slice(startIdx, startIdx + pageSize);

  tbody.innerHTML = paginated.map(item => {
    const confClass = item.confidence >= 0.85 ? 'conf-high' : (item.confidence >= 0.65 ? 'conf-mid' : 'conf-low');
    return `
      <tr data-id="${escapeHtml(item.id)}">
        <td><strong>${escapeHtml(item.id)}</strong></td>
        <td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(item.raw_address)}</td>
        <td class="cell-editable" data-field="province">${valOrNull(item.province)}</td>
        <td class="cell-editable" data-field="city">${valOrNull(item.city)}</td>
        <td class="cell-editable" data-field="district">${valOrNull(item.district)}</td>
        <td class="cell-editable" data-field="living_area">${valOrNull(item.living_area)}</td>
        <td class="cell-editable" data-field="street_type">${valOrNull(item.street_type)}</td>
        <td class="cell-editable" data-field="street_name"><strong>${valOrNull(item.street_name)}</strong></td>
        <td class="cell-editable" data-field="house"><strong>${valOrNull(item.house)}</strong></td>
        <td class="cell-editable" data-field="apartment">${valOrNull(item.apartment)}</td>
        <td class="cell-editable" data-field="entrance">${valOrNull(item.entrance)}</td>
        <td class="cell-editable" data-field="floor">${valOrNull(item.floor)}</td>
        <td class="cell-editable" data-field="landmark">${valOrNull(item.landmark)}</td>
        <td><span class="conf-pill ${confClass}">${(item.confidence * 100).toFixed(0)}%</span></td>
      </tr>
    `;
  }).join('');

  // Setup inline editing
  tbody.querySelectorAll('.cell-editable').forEach(td => {
    td.addEventListener('dblclick', () => makeEditable(td));
  });
}

function makeEditable(td) {
  const currentVal = td.textContent.trim() === 'null' ? '' : td.textContent.trim();
  const input = document.createElement('input');
  input.type = 'text';
  input.value = currentVal;
  input.style.width = '100%';
  input.style.padding = '4px 6px';
  input.style.fontSize = '12px';

  td.textContent = '';
  td.appendChild(input);
  input.focus();

  const finish = () => {
    const newVal = input.value.trim();
    const rowId = td.parentElement.dataset.id;
    const field = td.dataset.field;
    const item = currentResults.find(r => r.id === rowId);
    if (item) {
      item[field] = newVal === '' ? null : newVal;
      td.innerHTML = valOrNull(item[field]);
    }
  };

  input.addEventListener('blur', finish);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') finish();
  });
}

function setupExport() {
  const btn = document.getElementById('btn-export');
  btn.addEventListener('click', () => {
    if (!currentResults || currentResults.length === 0) return;
    const blob = new Blob([JSON.stringify(currentResults, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'jana_parsed_output.json';
    a.click();
    URL.revokeObjectURL(url);
  });
}

function setupSampleButton() {
  const btn = document.getElementById('btn-sample');
  btn.addEventListener('click', () => {
    const sample = [
      { "id": "rec_00417", "raw_address": "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70" },
      { "id": "rec_00002", "raw_address": "66квартал 12дом 69квартира" },
      { "id": "rec_00003", "raw_address": "Ул .Курильская 9 кв 1" },
      { "id": "rec_00004", "raw_address": "Туран 55/6 магазин береке 4подьезд 9этаж" },
      { "id": "rec_00005", "raw_address": "Турксибский район, Рыбинский переулок, дом 4" },
      { "id": "rec_00006", "raw_address": "Нурсити 2 микрорайон 11дом 5 подьезд 239 кв" },
      { "id": "rec_00007", "raw_address": "Кулманова 133" },
      { "id": "rec_00008", "raw_address": "жуалы 19" }
    ];
    uploadBatch(sample);
  });
}

function valOrNull(v) {
  return v !== null && v !== undefined && v !== '' ? escapeHtml(String(v)) : '<span style="color:#cbd5e1">null</span>';
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
