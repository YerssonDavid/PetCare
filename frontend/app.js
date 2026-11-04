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
async function loadSection(section) { const content=document.querySelector('#content'); if(section==='home') { content.innerHTML=`<section class="hero"><div><h2>Hola, ${escapeHtml(state.owner?.name || 'familia PetCare')}.</h2><p>Organiza el cuidado de tus mascotas desde un solo lugar.</p></div><div class="top-actions"><button class="secondary" data-section="pets">Registrar mascota</button><button class="secondary" data-section="services">Buscar servicios</button></div></section><div class="card-grid"><article class="card"><h3>🐶 Mis mascotas</h3><p class="muted">Registra y consulta la información de tus compañeros.</p></article><article class="card"><h3>🩺 Servicios</h3><p class="muted">Próximamente podrás consultar servicios disponibles.</p></article><article class="card"><h3>📋 Solicitudes</h3><p class="muted">Aquí verás tus reservas y estados.</p></article></div>`; content.querySelectorAll('[data-section]').forEach(b=>b.onclick=()=>renderDashboard(b.dataset.section)); return; } if(section==='pets'){ content.innerHTML=`<div class="section-title"><div><h2>Mis mascotas</h2><p class="muted">Los datos se guardan en el backend de PetCare.</p></div><button class="primary" id="show-pet-form">Agregar mascota</button></div><div id="pet-form-wrap"></div><div id="pet-list" class="card-grid"></div>`; document.querySelector('#show-pet-form').onclick=()=>document.querySelector('#pet-form-wrap').innerHTML=petForm(); document.querySelector('#pet-form-wrap').addEventListener('submit',handlePet); renderPets(); return; } content.innerHTML=`<div class="empty"><h2>${section==='services'?'Servicios':'Solicitudes'}</h2><p class="muted">Este módulo está preparado en el prototipo visual y será conectado en la siguiente fase del backend.</p></div>`; }
function petForm(){return `<form id="pet-form" class="card form"><h3>Registrar mascota</h3><div class="form-grid"><div class="field"><label>Nombre</label><input name="name" required maxlength="100"></div><div class="field"><label>Tipo</label><select name="typePet" required><option value="DOG">Perro</option><option value="CAT">Gato</option><option value="OTHER">Otro</option></select></div></div><div class="form-grid"><div class="field"><label>Edad</label><input name="age" type="number" min="0" required></div><div class="field"><label>Descripción</label><input name="description" maxlength="500" placeholder="Características u observaciones"></div></div><button class="primary">Guardar mascota</button></form>`;}

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

function renderPets() {
  const list=document.querySelector('#pet-list'); if(!list)return;
  list.innerHTML=state.pets.length ? state.pets.map(p=>`<article class="card pet-card"><div class="pet-avatar">🐾</div><div><h3>${escapeHtml(p.name)}</h3><p class="muted">${escapeHtml(p.typePet)} · ${p.age} años</p></div><button class="delete-button" data-delete-pet="${escapeHtml(p.id||p.name)}">Eliminar</button></article>`).join('') : `<div class="empty"><h3>Aún no tienes mascotas registradas</h3><p class="muted">Agrega la primera para empezar.</p></div>`;
  list.querySelectorAll('[data-delete-pet]').forEach(button => button.onclick=()=>deletePet(button.dataset.deletePet));
}

if (state.owner) renderDashboard(); else renderAuth();
