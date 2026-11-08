const API_BASE = localStorage.getItem('petcare_api_url') || 'http://localhost:8080';
const state = { owner: JSON.parse(localStorage.getItem('petcare_owner') || 'null'), pets: JSON.parse(localStorage.getItem('petcare_demo_pets') || '[]') };
const app = document.querySelector('#app');

function escapeHtml(value='') { return String(value).replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c])); }
function notice(message, type='error') { return `<div class="notice ${type}" role="alert">${escapeHtml(message)}</div>`; }
function logo() { return `<div class="brand-badge"><img class="logo-small" src="/img/petCareLogo.png" alt="Logo PetCare"> PetCare</div>`; }

function renderAuth(tab='login', message='') {
  app.innerHTML = `<main class="auth"><section class="brand"> <div>${logo()}<h1>El cuidado de tu mascota, en un solo lugar.</h1><p>Encuentra y gestiona servicios de cuidado para tus mascotas con información clara y confirmaciones organizadas.</p></div><p class="brand-badge">Universidad Iberoamericana</p></section><section class="auth-panel"><div class="panel"><div class="tabs"><button class="${tab==='login'?'active':''}" data-tab="login">Iniciar sesión</button><button class="${tab==='register'?'active':''}" data-tab="register">Crear cuenta</button></div>${message}${tab==='login'?loginForm():registerForm()}</div></section></main>`;
  document.querySelectorAll('[data-tab]').forEach(b => b.onclick = () => renderAuth(b.dataset.tab));
  document.querySelector('#login-form')?.addEventListener('submit', handleLogin);
  document.querySelector('#register-form')?.addEventListener('submit', handleRegister);
}
function loginForm() { return `<h2>Bienvenido nuevamente</h2><p class="muted">Usa un correo y una contraseña ficticios para recorrer el prototipo.</p><form id="login-form" class="form"><div class="field"><label for="login-email">Correo electrónico</label><input id="login-email" type="email" required placeholder="demo@petcare.test"></div><div class="field"><label for="login-password">Contraseña</label><input id="login-password" type="password" required minlength="4" placeholder="Cualquier contraseña"></div><button class="primary">Ingresar al modo demo</button></form><p class="muted">Este acceso es demostrativo: el backend aún no cuenta con autenticación ni JWT.</p>`; }
function registerForm() { return `<h2>Crear cuenta</h2><p class="muted">Registra tus datos para comenzar a cuidar mejor a tus mascotas.</p><form id="register-form" class="form"><div class="field"><label for="reg-name">Nombre completo</label><input id="reg-name" required maxlength="100" placeholder="Ej. Ana Mosquera"></div><div class="form-grid"><div class="field"><label for="reg-email">Correo electrónico</label><input id="reg-email" type="email" required maxlength="150" placeholder="correo@ejemplo.com"></div><div class="field"><label for="reg-phone">Teléfono</label><input id="reg-phone" required placeholder="3001234567"></div></div><div class="field"><label for="reg-password">Contraseña</label><input id="reg-password" type="password" required minlength="8" maxlength="72" placeholder="Mínimo 8 caracteres"></div><button class="primary">Crear cuenta</button></form>`; }

async function api(path, options={}) { let response; try { response = await fetch(`${API_BASE}${path}`, { headers:{'Content-Type':'application/json', ...(options.headers||{})}, ...options }); } catch { throw new Error(`No se pudo conectar con el backend en ${API_BASE}. Verifica que Spring Boot esté iniciado y que PostgreSQL esté disponible.`); } const text = await response.text(); let data; try { data = text ? JSON.parse(text) : null; } catch { data = { detail:text }; } if (!response.ok) throw new Error(data?.detail || data?.message || data?.title || `La operación falló (${response.status}).`); return data; }
async function handleRegister(event) { event.preventDefault(); const button = event.submitter; button.disabled = true; button.textContent = 'Creando cuenta...'; try { const data = await api('/registry/owner', {method:'POST', body:JSON.stringify({name:document.querySelector('#reg-name').value.trim(), email:document.querySelector('#reg-email').value.trim(), phone:document.querySelector('#reg-phone').value.trim(), password:document.querySelector('#reg-password').value})}); state.owner = { id:data.id, name:document.querySelector('#reg-name').value.trim(), email:document.querySelector('#reg-email').value.trim() }; localStorage.setItem('petcare_owner', JSON.stringify(state.owner)); renderDashboard('pets', notice('Cuenta creada correctamente. Ahora registra tu primera mascota.', 'success')); } catch (error) { renderAuth('register', notice(error.message)); } }
async function handleLogin(event) { event.preventDefault(); const email=document.querySelector('#login-email').value.trim(); const name=email.split('@')[0].replace(/[._-]+/g,' ') || 'Usuario demo'; state.owner={id:null,name:name.replace(/\b\w/g,c=>c.toUpperCase()),email,demo:true}; localStorage.setItem('petcare_owner',JSON.stringify(state.owner)); renderDashboard('home',notice('Estás usando el modo demo. El correo es ficticio y las funciones disponibles se muestran para validar el prototipo.','success')); }

function renderDashboard(section='home', message='') { app.innerHTML = `<header class="appbar"><h1><img class="logo-small" src="/img/petCareLogo.png" alt="Logo PetCare"> PetCare</h1><nav class="nav"><button data-section="home">Inicio</button><button data-section="pets">Mis mascotas</button><button data-section="services">Servicios</button><button data-section="history">Solicitudes</button><button id="logout">Salir</button></nav></header><main class="main">${message}<div id="content"></div></main>`; document.querySelectorAll('[data-section]').forEach(b => b.onclick=()=>renderDashboard(b.dataset.section)); document.querySelector('#logout').onclick=()=>{localStorage.removeItem('petcare_owner'); state.owner=null; renderAuth()}; loadSection(section); }
async function loadSection(section) { const content=document.querySelector('#content'); if(section==='home') { content.innerHTML=`<section class="hero"><div><h2>Hola, ${escapeHtml(state.owner?.name || 'familia PetCare')}.</h2><p>Organiza el cuidado de tus mascotas desde un solo lugar.</p></div><div class="top-actions"><button class="secondary" data-section="pets">Registrar mascota</button><button class="secondary" data-section="services">Buscar servicios</button></div></section><div class="card-grid"><article class="card"><h3>🐾 Mis mascotas</h3><p class="muted">Registra y consulta la información de tus compañeros.</p></article><article class="card"><h3>🩺 Servicios</h3><p class="muted">Consulta servicios, precios y franjas disponibles.</p></article><article class="card"><h3>📋 Solicitudes</h3><p class="muted">Aquí verás tus reservas y estados.</p></article></div>`; content.querySelectorAll('[data-section]').forEach(b=>b.onclick=()=>renderDashboard(b.dataset.section)); return; } if(section==='pets'){ content.innerHTML=`<div class="section-title"><div><h2>Mis mascotas</h2><p class="muted">Los datos se guardan en el backend de PetCare.</p></div><button class="primary" id="show-pet-form">Agregar mascota</button></div><div id="pet-form-wrap"></div><div id="pet-list" class="card-grid"></div>`; document.querySelector('#show-pet-form').onclick=()=>document.querySelector('#pet-form-wrap').innerHTML=petForm(); document.querySelector('#pet-form-wrap').addEventListener('submit',handlePet); renderPets(); return; } if(section==='services') return renderServices(content); if(section==='history') return renderHistory(content); content.innerHTML=`<div class="empty"><h2>Solicitudes</h2><p class="muted">Registra una solicitud desde el detalle de un servicio para verla aquí.</p></div>`; }

function requestStatusLabel(status) { return ({PENDIENTE:'Pendiente', ACEPTADO:'Aceptada', RECHAZADO:'Rechazada', CANCELADA:'Cancelada'})[status] || status; }

async function renderHistory(content, message='') {
  if (!state.owner?.id) { content.innerHTML=`${notice('Crea una cuenta para ver tu historial.','error')}<button class="secondary" id="history-account">Crear cuenta</button>`; document.querySelector('#history-account').onclick=()=>renderAuth('register'); return; }
  content.innerHTML=`${message}<div class="section-title"><div><h2>Mis solicitudes</h2><p class="muted">Consulta tus servicios, horarios y estados.</p></div><button class="secondary" id="history-services">Ver servicios</button></div><div id="history-list" class="card-grid"><div class="empty">Cargando solicitudes...</div></div>`;
  document.querySelector('#history-services').onclick=()=>renderDashboard('services');
  try {
    const requests=await api(`/service-requests/mine?ownerId=${encodeURIComponent(state.owner.id)}`);
    const list=document.querySelector('#history-list');
    list.innerHTML=requests.length ? requests.map(r=>`<article class="card request-card"><div class="request-card-head"><div><h3>${escapeHtml(r.offeringName)}</h3><p class="muted">${escapeHtml(r.petName)}</p></div><span class="status status-${String(r.status).toLowerCase()}">${escapeHtml(requestStatusLabel(r.status))}</span></div><p><strong>Fecha:</strong> ${escapeHtml(r.date)} · <strong>Hora:</strong> ${escapeHtml((r.startTime || '').slice(0,5))}</p>${r.status === 'PENDIENTE' ? `<button class="secondary cancel-request" data-request-id="${escapeHtml(r.id)}">Cancelar</button>` : ''}</article>`).join('') : '<div class="empty"><h3>Aún no tienes solicitudes</h3><p class="muted">Elige un servicio y una franja para comenzar.</p><button class="secondary" id="empty-services">Ver servicios</button></div>';
    document.querySelector('#empty-services')?.addEventListener('click',()=>renderDashboard('services'));
    list.querySelectorAll('[data-request-id]').forEach(button=>button.addEventListener('click',()=>cancelRequest(button.dataset.requestId, button)));
  } catch(error) { document.querySelector('#history-list').innerHTML=notice(error.message); }
}

async function cancelRequest(requestId, button) {
  if (!confirm('¿Seguro que quieres cancelar?')) return;
  button.disabled=true; button.textContent='Cancelando...';
  try {
    await api(`/service-requests/${encodeURIComponent(requestId)}/cancel?ownerId=${encodeURIComponent(state.owner.id)}`, {method:'PATCH'});
    await renderHistory(document.querySelector('#content'), notice('Solicitud cancelada correctamente. La franja volvió a quedar disponible.','success'));
  } catch(error) {
    button.disabled=false; button.textContent='Cancelar';
    document.querySelector('#history-list')?.insertAdjacentHTML('afterbegin', notice(error.message));
  }
}

async function renderServices(content) {
  content.innerHTML = `<div class="section-title"><div><h2>Servicios disponibles</h2><p class="muted">El catálogo se carga desde el backend.</p></div><input id="service-search" class="search" placeholder="Buscar servicio"></div><div id="service-list" class="card-grid"><div class="empty">Cargando servicios...</div></div><div id="service-detail"></div>`;
  const list = document.querySelector('#service-list');
  try {
    const services = await api('/services');
    const draw = (items) => { list.innerHTML = items.length ? items.map(s => `<article class="card service-card" data-service-id="${s.id}"><h3>${escapeHtml(s.name)}</h3><p class="muted">${escapeHtml(s.category)} · $${Number(s.price).toLocaleString('es-CO')}</p><p>${escapeHtml(s.description)}</p><button class="secondary" data-service-id="${s.id}">Ver detalle y franjas</button></article>`).join('') : '<div class="empty">No hay servicios que coincidan.</div>'; list.querySelectorAll('[data-service-id]').forEach(el => el.onclick = event => { event.stopPropagation(); showServiceDetail(el.dataset.serviceId); }); };
    draw(services);
    document.querySelector('#service-search').oninput = e => draw(services.filter(s => `${s.name} ${s.category}`.toLowerCase().includes(e.target.value.toLowerCase())));
  } catch (error) { list.innerHTML = notice(error.message); }
}

async function showServiceDetail(id) {
  const target = document.querySelector('#service-detail');
  target.innerHTML = '<div class="card">Cargando detalle...</div>';
  try { const service = await api(`/services/${id}`); target.innerHTML = `<article class="card detail-card"><h2>${escapeHtml(service.name)}</h2><p>${escapeHtml(service.description)}</p><p class="muted">${escapeHtml(service.conditions || 'Sin condiciones adicionales')} · $${Number(service.price).toLocaleString('es-CO')}</p><h3>Franjas disponibles</h3><div class="slot-list">${service.freeSlots.length ? service.freeSlots.map(slot => `<button class="secondary slot" data-slot-id="${slot.id}">${slot.date} · ${slot.startTime.slice(0,5)}–${slot.endTime.slice(0,5)}</button>`).join('') : '<p class="muted">No hay franjas libres.</p>'}</div><div id="request-panel"></div></article>`; target.querySelectorAll('[data-slot-id]').forEach(button => button.onclick=()=>selectSlot(service, button.dataset.slotId)); } catch (error) { target.innerHTML = notice(error.message); }
}

async function selectSlot(service, slotId) {
  const panel = document.querySelector('#request-panel');
  if (!state.owner?.id) { panel.innerHTML = `${notice('Crea una cuenta para solicitar.','error')}<button class="secondary" id="create-account">Crear cuenta</button>`; document.querySelector('#create-account').onclick=()=>renderAuth('register'); return; }
  panel.innerHTML = '<div class="card">Cargando mascotas...</div>';
  try {
    const pets = await api(`/pets?ownerId=${encodeURIComponent(state.owner.id)}`);
    state.pets = pets;
    if (!pets.length) { panel.innerHTML = `${notice('Registra una mascota antes de solicitar un servicio.','error')}<button class="secondary" id="go-pets">Mis mascotas</button>`; document.querySelector('#go-pets').onclick=()=>renderDashboard('pets'); return; }
    panel.innerHTML = `<form id="request-form" class="form"><h3>Solicitar servicio</h3><div class="field"><label for="request-pet">Mascota</label><select id="request-pet" required>${pets.map(p=>`<option value="${p.id}">${escapeHtml(p.name)}</option>`).join('')}</select></div><button class="primary">Solicitar</button><div id="request-message"></div></form>`;
    document.querySelector('#request-form').onsubmit = async event => { event.preventDefault(); const button=event.submitter; button.disabled=true; button.textContent='Enviando...'; try { const response=await api('/service-requests',{method:'POST',body:JSON.stringify({ownerId:state.owner.id,petId:document.querySelector('#request-pet').value,slotId})}); const chosen=pets.find(p=>p.id===document.querySelector('#request-pet').value); panel.innerHTML=`<div class="notice success"><h3>Solicitud creada</h3><p>${escapeHtml(response.offeringName)} · ${escapeHtml(chosen?.name || response.petName)}</p><p>${response.date} · ${response.startTime.slice(0,5)} · Estado: Pendiente</p><button class="secondary" id="view-history">Ver mis solicitudes</button></div>`; const slotButton=document.querySelector(`[data-slot-id="${slotId}"]`); if(slotButton){slotButton.disabled=true;slotButton.textContent+=' · Ocupada';} document.querySelector('#view-history').onclick=()=>renderDashboard('history'); } catch(error) { document.querySelector('#request-message').innerHTML=notice(error.message); button.disabled=false; button.textContent='Solicitar'; } };
  } catch(error) { panel.innerHTML=notice(error.message); }
}
function petForm(){return `<form id="pet-form" class="card form"><h3>Registrar mascota</h3><div class="form-grid"><div class="field"><label>Nombre</label><input name="name" required maxlength="100"></div><div class="field"><label>Tipo</label><select name="typePet" required><option value="PERRO">Perro</option><option value="GATO">Gato</option><option value="AVE">Ave</option><option value="CONEJO">Conejo</option><option value="OTRO">Otro</option></select></div></div><div class="form-grid"><div class="field"><label>Edad</label><input name="age" type="number" min="0" required></div><div class="field"><label>Descripción</label><input name="description" maxlength="500" placeholder="Características u observaciones"></div></div><button class="primary">Guardar mascota</button></form>`;}

document.addEventListener('click', event => {
  const card = event.target.closest('#content .card');
  if (!card || card.querySelector('button')) return;
  const title = card.querySelector('h3')?.textContent || '';
  if (title.includes('Mis mascotas')) renderDashboard('pets');
  if (title.includes('Servicios')) renderDashboard('services');
  if (title.includes('Solicitudes')) renderDashboard('history');
});

async function handlePet(event){
  event.preventDefault(); const form=event.target; const button=form.querySelector('button'); button.disabled=true;
  const petData={name:form.name.value.trim(),typePet:form.typePet.value,age:Number(form.age.value),description:form.description.value.trim()};
  if(!state.owner?.id){ state.pets.push(petData); localStorage.setItem('petcare_demo_pets',JSON.stringify(state.pets)); form.outerHTML=notice('Mascota guardada en el modo demo.','success'); renderPets(); return; }
  try { const data=await api('/registry/pet',{method:'POST',body:JSON.stringify({ownerId:state.owner.id,...petData})}); state.pets.push({...petData,id:data.id}); localStorage.setItem('petcare_demo_pets',JSON.stringify(state.pets)); form.outerHTML=notice('Mascota registrada correctamente en el backend.','success'); renderPets(); }
  catch(error){ form.insertAdjacentHTML('afterbegin',notice(error.message)); button.disabled=false; }
}

async function deletePet(id) {
  if (!confirm('¿Quieres eliminar esta mascota?')) return;
  try {
    if (state.owner?.id && id) await api(`/registry/pet?petId=${encodeURIComponent(id)}&ownerId=${encodeURIComponent(state.owner.id)}`, { method:'DELETE' });
    state.pets = state.pets.filter(p => String(p.id || p.name) !== String(id));
    localStorage.setItem('petcare_demo_pets', JSON.stringify(state.pets));
    renderPets();
  } catch (error) { alert(error.message); }
}

async function renderPets() {
  const list=document.querySelector('#pet-list'); if(!list)return;
  if (state.owner?.id) { list.innerHTML='<div class="empty">Cargando mascotas...</div>'; try { state.pets=await api(`/pets?ownerId=${encodeURIComponent(state.owner.id)}`); } catch(error) { list.innerHTML=notice(error.message); return; } }
  list.innerHTML=state.pets.length ? state.pets.map(p=>`<article class="card pet-card"><div class="pet-avatar">🐾</div><div><h3>${escapeHtml(p.name)}</h3><p class="muted">${escapeHtml(p.typePet)} · ${p.age} años</p></div><button class="delete-button" data-delete-pet="${escapeHtml(p.id||p.name)}">Eliminar</button></article>`).join('') : `<div class="empty"><h3>Aún no tienes mascotas registradas</h3><p class="muted">Agrega la primera para empezar.</p></div>`;
  list.querySelectorAll('[data-delete-pet]').forEach(button => button.onclick=()=>deletePet(button.dataset.deletePet));
}

if (state.owner) renderDashboard(); else renderAuth();
