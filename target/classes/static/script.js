window.onload = function() {
  const t = localStorage.getItem('theme');
  const c = document.getElementById('theme-toggle');
  if (t === 'dark' && c) c.checked = true;
  animatePie(passRate);
};

function toggleTheme() {
  const c = document.getElementById('theme-toggle');
  const b = document.body;
  if (c.checked) {
    b.setAttribute('data-theme', 'dark');
    localStorage.setItem('theme', 'dark');
  } else {
    b.removeAttribute('data-theme');
    localStorage.setItem('theme', 'light');
  }
}

function animatePie(p) {
  const c = document.querySelector('.pie-chart');
  if (!c) return;
  let start = 0;
  const duration = 1500;
  function step(ts) {
    if (!start) start = ts;
    const elapsed = ts - start;
    const progress = Math.min(elapsed / duration, 1);
    const ease = 1 - Math.pow(1 - progress, 3);
    const currentTotal = ease * 100;
    const currentPass = Math.min(p, currentTotal);
    c.style.setProperty('--pos-pass', currentPass + '%');
    c.style.setProperty('--pos-total', currentTotal + '%');
    if (progress < 1) requestAnimationFrame(step);
  }
  requestAnimationFrame(step);
}

function openTab(e, n) {
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.getElementById(n).classList.add('active');
  e.currentTarget.classList.add('active');
}

function toggleAccordion(i) {
  const p = document.getElementById(i);
  if (!p) return;
  if (p.style.maxHeight) {
    p.style.maxHeight = null;
  } else {
    p.style.maxHeight = p.scrollHeight + 'px';
  }
}

function openModal(id) {
  const s = findSample(samples, id);
  if (!s) return;
  document.getElementById('modal-title').innerText = s.label;
  document.getElementById('req-panel').innerHTML = buildReqHtml(s);
  document.getElementById('res-panel').innerHTML = buildResHtml(s);
  document.getElementById('assertions-panel').innerHTML = buildAssertHtml(s);
  if (s.assertions && s.assertions.length > 0) {
    document.getElementById('tab-assert').style.display = 'block';
  } else {
    document.getElementById('tab-assert').style.display = 'none';
  }
  openModalTab('req-panel', document.getElementById('tab-req'));
  const mb = document.querySelector('.modal-body');
  if (mb) mb.scrollTop = 0;
  const m = document.getElementById('detailsModal');
  if (m) {
    m.style.display = 'block';
    setTimeout(() => m.classList.add('show'), 10);
  }
}

function findSample(l, id) {
  for (let s of l) {
    if (s.id === id) return s;
    if (s.children) {
      let f = findSample(s.children, id);
      if (f) return f;
    }
  }
  return null;
}

function cleanReqBody(b) {
  let bc = b || '';
  let cIdx = bc.indexOf('Cookie Data:');
  if (cIdx !== -1) {
    bc = bc.substring(0, cIdx).trim();
  }
  let m = bc.match(/data:\r?\n([\s\S]*?)(\r?\n\[no cookies\]|$)/);
  if (m) {
    bc = m[1].trim();
  } else if (bc.includes('[no cookies]')) {
    bc = '';
  }
  return bc;
}

function applyFiltersWithLoader() {
  let loader = document.getElementById('filter-loader');
  if (loader) loader.style.display = 'flex';
  setTimeout(() => {
    applyFiltersLogic();
    if (loader) loader.style.display = 'none';
  }, 50);
}

function applyFiltersLogic() {
  let tRad = document.querySelector('input[name="failType"]:checked');
  let tVal = tRad ? tRad.value : 'All';
  let cSel = document.getElementById('failCodeSelect');
  let cVal = cSel ? cSel.value : 'All';
  let allRows = document.querySelectorAll('#failed-list-container .request-row');
  let typeForCode = new Set();
  let codesForType = new Set();
  allRows.forEach(r => {
    let rType = r.getAttribute('data-fail-type');
    let rCode = r.getAttribute('data-code');
    if (tVal === 'All' || rType === tVal) {
      if (rCode) codesForType.add(rCode);
    }
    if (cVal === 'All' || rCode === cVal) {
      if (rType) typeForCode.add(rType);
    }
  });
  let selectContainer = document.getElementById('failCodeSelectContainer');
  if (selectContainer && cSel) {
    if (codesForType.size <= 1) {
      selectContainer.style.display = 'none';
      cSel.value = 'All';
      cVal = 'All';
    } else {
      selectContainer.style.display = 'flex';
    }
  }
  if (cSel) {
    let currentOpt = cSel.value;
    Array.from(cSel.options).forEach(opt => {
      if (opt.value === 'All') return;
      if (!codesForType.has(opt.value)) {
        opt.hidden = true;
        opt.disabled = true;
      } else {
        opt.hidden = false;
        opt.disabled = false;
      }
    });
    if (currentOpt !== 'All' && !codesForType.has(currentOpt)) {
      cSel.value = 'All';
      cVal = 'All';
      typeForCode.clear();
      allRows.forEach(r => {
        let rType = r.getAttribute('data-fail-type');
        if (rType) typeForCode.add(rType);
      });
    }
  }
  let radios = document.querySelectorAll('input[name="failType"]');
  radios.forEach(rad => {
    if (rad.value === 'All') {
      rad.disabled = false;
    } else {
      if (cVal !== 'All' && typeForCode.size === 1 && !typeForCode.has(rad.value)) {
        rad.disabled = true;
      } else {
        rad.disabled = false;
      }
    }
  });
  let totalVisible = 0;
  document.querySelectorAll('#failed-list-container .accordion').forEach(acc => {
    let p = acc.nextElementSibling;
    let vChild = false;
    let vCnt = 0;
    p.querySelectorAll('.request-row').forEach(r => {
      let m = true;
      if (tVal !== 'All' && r.getAttribute('data-fail-type') !== tVal) m = false;
      if (cVal !== 'All' && r.getAttribute('data-code') !== cVal) m = false;
      if (m) {
        r.style.display = 'flex';
        vChild = true;
        vCnt++;
      } else {
        r.style.display = 'none';
      }
    });
    if (vChild) {
      acc.style.display = 'flex';
      let cSp = acc.querySelector('.child-count');
      if (cSp) cSp.innerText = vCnt;
      totalVisible += vCnt;
    } else {
      acc.style.display = 'none';
    }
  });
  document.querySelectorAll('#failed-list-container > .request-row').forEach(r => {
    let m = true;
    if (tVal !== 'All' && r.getAttribute('data-fail-type') !== tVal) m = false;
    if (cVal !== 'All' && r.getAttribute('data-code') !== cVal) m = false;
    if (m) {
      r.style.display = 'flex';
      totalVisible++;
    } else {
      r.style.display = 'none';
    }
  });
  let ph = document.getElementById('no-results-placeholder');
  if (ph) {
    ph.style.display = totalVisible === 0 ? 'block' : 'none';
  }
}

function buildReqHtml(s) {
  let svg = `<svg width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='9' y='9' width='13' height='13' rx='2' ry='2'></rect><path d='M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1'></path></svg> Copy`;
  let curlBtn = `<button class='copy-btn curl-btn' onclick='copyCurl("${s.id}")'>${svg} Copy as cURL</button>`;
  return `${curlBtn}<h3>API Name</h3><pre>${s.label}</pre><h3>Endpoint</h3><pre>${s.url}</pre><h3>Method</h3><pre>${s.method}</pre><h3>Headers</h3><div class='pre-wrapper'><button class='copy-btn' onclick='copyText(this)'>${svg}</button><pre>${escape(s.reqHeaders)}</pre></div><h3>Body</h3><div class='pre-wrapper'><button class='copy-btn' onclick='copyText(this)'>${svg}</button><pre>${escape(cleanReqBody(s.reqBody))}</pre></div>`;
}

function buildAssertHtml(s) {
  if (!s.assertions || s.assertions.length === 0) return '<div style="padding:15px;color:var(--text-muted);">No assertions data available.</div>';
  let astHtml = '<h3>Assertions</h3><div style="margin-top:10px;">';
  s.assertions.forEach(a => {
    let st = a.failure ? 'FAIL' : 'PASS';
    let bg = a.failure ? 'var(--bg-badge-fail)' : 'var(--bg-badge-pass)';
    let tc = a.failure ? 'var(--text-fail)' : 'var(--text-pass)';
    let bc = a.failure ? 'var(--border-fail)' : 'var(--border-pass)';
    astHtml += `<div class='tx-item' style='flex-direction:column; align-items:flex-start; margin-bottom:10px;'><div style='display:flex; justify-content:space-between; width:100%; align-items:center;'><span class='tx-name'>${escape(a.name)}</span><span class='tx-status' style='background:${bg};color:${tc};border:1px solid ${bc}'>${st}</span></div>`;
    if (a.failureMessage) {
      astHtml += `<div style='font-size:0.85em; color:var(--text-body); margin-top:5px; background:var(--bg-page); padding:8px; border-radius:4px; width:100%; box-sizing:border-box; overflow-wrap:anywhere; word-break:break-all;'>${escape(a.failureMessage).replace(/\n/g, '<br/>')}</div>`;
    }
    astHtml += `</div>`;
  });
  astHtml += '</div>';
  return astHtml;
}

function formatBody(body, headers) {
  if (!body) return '';
  try {
    let isJson = headers && headers.toLowerCase().includes('application/json');
    let bodyTrim = body.trim();
    if (isJson || bodyTrim.startsWith('{') || bodyTrim.startsWith('[')) {
      return JSON.stringify(JSON.parse(body), null, 2);
    }
    return body;
  } catch (e) {
    return body;
  }
}

function buildCurl(s) {
  if (!s.url) return '';
  let m = s.method || 'GET';
  let nl = '\n';
  let b = cleanReqBody(s.reqBody);
  let c = 'curl --location \'' + s.url + '\'';
  if (m !== 'GET' && !(m === 'POST' && b)) {
    c = 'curl --location --request ' + m + ' \'' + s.url + '\'';
  }
  if (s.reqHeaders) {
    let hs = s.reqHeaders.split('\n');
    let ignore = ['connection', 'host', 'user-agent', 'content-length', 'accept-encoding'];
    for (let h of hs) {
      let ht = h.trim();
      if (ht.match(/^[a-zA-Z0-9-]+:/)) {
        let hName = ht.substring(0, ht.indexOf(':')).trim().toLowerCase();
        if (!ignore.includes(hName)) {
          c += ' ' + String.fromCharCode(92) + nl + '--header \'' + ht.replace(/'/g, "'\\''") + '\'';
        }
      }
    }
  }
  if (b) {
    c += ' ' + String.fromCharCode(92) + nl + '--data \'' + b.replace(/'/g, "'\\''") + '\'';
  }
  return c;
}

function buildResHtml(s) {
  if (s.children && s.children.length > 0) {
    let h = '<h3>Transaction Summary</h3><div style="padding:10px">';
    s.children.forEach(c => {
      let st = (c.resCode >= 200 && c.resCode < 400) ? 'PASS' : 'FAIL';
      let bg = st === 'PASS' ? 'var(--bg-badge-pass)' : 'var(--bg-badge-fail)';
      let tc = st === 'PASS' ? 'var(--text-pass)' : 'var(--text-fail)';
      let bc = st === 'PASS' ? 'var(--border-pass)' : 'var(--border-fail)';
      h += `<div class='tx-item'><span class='tx-name'>${c.label}</span><span class='tx-status' style='background:${bg};color:${tc};border:1px solid ${bc}'>${st}</span></div>`;
    });
    h += '</div>';
    return h;
  }
  let formattedBody = formatBody(s.resBody, s.resHeaders);
  let svg = `<svg width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='9' y='9' width='13' height='13' rx='2' ry='2'></rect><path d='M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1'></path></svg> Copy`;
  return `<h3>Latency</h3><pre>${s.latency} ms</pre><h3>Status</h3><pre>${s.resCode} ${s.resMsg}</pre><h3>Headers</h3><div class='pre-wrapper'><button class='copy-btn' onclick='copyText(this)'>${svg}</button><pre>${escape(s.resHeaders)}</pre></div><h3>Body</h3><div class='pre-wrapper'><button class='copy-btn' onclick='copyText(this)'>${svg}</button><pre>${escape(formattedBody)}</pre></div>`;
}

function openModalTab(i, elem) {
  document.querySelectorAll('.modal-panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.modal-tab').forEach(t => t.classList.remove('active'));
  document.getElementById(i).classList.add('active');
  if (elem) {
    elem.classList.add('active');
  } else {
    event.currentTarget.classList.add('active');
  }
  const mb = document.querySelector('.modal-body');
  if (mb) mb.scrollTop = 0;
}

function closeModal() {
  const m = document.getElementById('detailsModal');
  if (m) {
    m.classList.remove('show');
    setTimeout(() => m.style.display = 'none', 300);
  }
}

function copyText(b) {
  let pre = b.nextElementSibling;
  if (pre) {
    navigator.clipboard.writeText(pre.innerText);
    showToast();
  }
}

function copyCurl(id) {
  const s = findSample(samples, id);
  if (s) {
    navigator.clipboard.writeText(buildCurl(s));
    showToast();
  }
}

function showToast() {
  const x = document.getElementById('toast');
  if (x) {
    x.className = 'toast show';
    setTimeout(function() {
      x.className = x.className.replace('show', '');
    }, 3000);
  }
}

function escape(s) {
  if (!s) return '';
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

window.onclick = function(e) {
  const m = document.getElementById('detailsModal');
  if (e.target == m) closeModal();
};
