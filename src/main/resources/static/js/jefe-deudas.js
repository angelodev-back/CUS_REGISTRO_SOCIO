/**
 * jefe-deudas.js
 * Panel del Jefe de Atención al Cliente - Club Náutico Neptuno
 *
 * Flujo:
 *  1. Carga postulantes PENDIENTE/SUBSANADO desde /api/jefe/postulantes (BD local)
 *  2. Al seleccionar uno, consulta la API externa de deudas y filtra por numero_documento
 *  3. Muestra datos personales, clasificación automática y lista de deudas
 *  4. Permite verificar deudas (BD local) y aprobar/rechazar postulante
 */

// ─── Constantes ────────────────────────────────────────────────────
const API_BASE       = '/api/jefe';
// Usamos el endpoint proxy del backend para evitar CORS y cold-starts de Render.com
const API_DEUDAS_EXT = '/api/jefe/deudas-externas';

// ─── Estado global ──────────────────────────────────────────────────
let postulanteActual   = null;   // Objeto completo del postulante seleccionado
let deudasExternas     = [];     // Datos crudos del API externo para el postulante actual
let todosPostulantes   = [];     // Cache de todos los postulantes cargados
let deudasExternasAll  = null;   // Cache del JSON externo (evita múltiples llamadas)

// ─── Inicialización ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    cargarUserInfo();
    cargarPostulantes();
    configurarEventos();
});

function configurarEventos() {
    document.getElementById('btnLogout').addEventListener('click', logout);
    document.getElementById('btnAprobar').addEventListener('click', aprobarPostulante);
    document.getElementById('btnRechazar').addEventListener('click', mostrarModalRechazo);
    document.getElementById('btnCancelarRechazo').addEventListener('click', cerrarModalRechazo);
    document.getElementById('btnConfirmarRechazo').addEventListener('click', confirmarRechazo);

    // Cerrar modal al hacer clic fuera
    document.getElementById('modalRechazo').addEventListener('click', (e) => {
        if (e.target === document.getElementById('modalRechazo')) cerrarModalRechazo();
    });
}

// ─── Perfil del JEFE ────────────────────────────────────────────────
async function cargarUserInfo() {
    const token = localStorage.getItem('jwt_token');
    if (!token) { window.location.href = '/login'; return; }

    try {
        const res = await fetch('/api/auth/profile', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error('No autorizado');

        const data = await res.json();
        if (data.body) {
            const nombre = data.body.nombres || 'Jefe';
            document.getElementById('nombreJefe').textContent = nombre;
            document.getElementById('avatarJefe').textContent  = nombre.charAt(0).toUpperCase();
        }
    } catch {
        document.getElementById('nombreJefe').textContent = 'Jefe';
    }
}

// ─── Cargar postulantes (BD local) ───────────────────────────────────
async function cargarPostulantes() {
    const token = localStorage.getItem('jwt_token');

    try {
        const res = await fetch(`${API_BASE}/postulantes`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) throw new Error('Error al cargar postulantes');

        const data = await res.json();
        todosPostulantes = data.body || [];
        renderizarTablaPostulantes(todosPostulantes);

    } catch (err) {
        console.error(err);
            const container = document.getElementById('tablaPostulantesBody');
            if (!container) {
                console.warn('No se encontró #tablaPostulantesBody en el DOM. ¿Estás usando la vista correcta?');
                return;
            }
            container.innerHTML = `
                <tr><td colspan="5" style="text-align:center;color:var(--error);padding:20px;font-size:13px;">
                    ⚠️ Error al cargar postulantes: ${err.message}
                </td></tr>`;
    }
}

function renderizarTablaPostulantes(lista) {
    const container = document.getElementById('tablaPostulantesBody');
    
    if (!container) {
        console.error('ERROR CRÍTICO: No se encontró #tablaPostulantesBody en el DOM.');
        console.warn('Verifica que estés usando la URL correcta: /jefe/dashboard-deudas');
        console.warn('O que el HTML incluya <tbody id="tablaPostulantesBody">');
        return;
    }

    if (!lista || lista.length === 0) {
        container.innerHTML = `
            <tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:24px;font-size:13px;line-height:1.6;">
                No hay postulantes pendientes de revisión en este momento.
            </td></tr>`;
        return;
    }

    container.innerHTML = lista.map(p => {
        const nombre = p.nombres
            ? `${p.nombres} ${p.apellidoPaterno || ''} ${p.apellidoMaterno || ''}`.trim()
            : (p.razonSocial || 'Sin nombre');
        const estado = (p.estadoPostulacion || 'PENDIENTE').toLowerCase();
        const fecha = p.fechaRegistro ? formatearFecha(p.fechaRegistro) : '—';
        return `
            <tr>
                <td><strong>${nombre}</strong></td>
                <td><span style="color:var(--text-muted);font-size:12px">${p.tipoDocumento}:</span> ${p.numeroDocumento}</td>
                <td>${fecha}</td>
                <td><span class="pc-badge badge-${estado}">${capitalizarPrimera(p.estadoPostulacion || 'PENDIENTE')}</span></td>
                <td style="text-align:center">
                    <button class="btn-verificar-row" onclick="seleccionarPostulante(${p.idPostulante})">Ver Detalles</button>
                </td>
            </tr>`;
    }).join('');
}

function filtrarPostulantes() {
    const q = document.getElementById('searchInput').value.toLowerCase().trim();
    if (!q) { renderizarTablaPostulantes(todosPostulantes); return; }

    const filtrados = todosPostulantes.filter(p => {
        const nombre = `${p.nombres || ''} ${p.apellidoPaterno || ''} ${p.razonSocial || ''}`.toLowerCase();
        return nombre.includes(q) || (p.numeroDocumento || '').includes(q);
    });
    renderizarTablaPostulantes(filtrados);
}

// ─── Seleccionar postulante ───────────────────────────────────────────
async function seleccionarPostulante(idPostulante) {

    mostrarLoadingDetalle();

    const token = localStorage.getItem('jwt_token');

    try {
        // 1. Obtener datos del postulante desde la BD local
        const res = await fetch(`${API_BASE}/postulantes/${idPostulante}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error('Postulante no encontrado');

        const data = await res.json();
        postulanteActual = data.body;

        // 2. Obtener deudas del API externo (con cache)
        await cargarDeudasExternas();

        // 3. Filtrar deudas del postulante actual por numero_documento
        const registro = deudasExternasAll
            ? deudasExternasAll.find(r =>
                  r.numero_documento === postulanteActual.numeroDocumento &&
                  r.tipo_documento   === postulanteActual.tipoDocumento)
            : null;

        deudasExternas = registro ? registro.deudas : [];
        const clasExtAPI = registro ? mapearClasificacion(registro.clasificacion_sugerida) : null;

        // 4. Calcular clasificación (usamos la del API externo si existe, sino la del backend)
        const clasificacion = clasExtAPI || postulanteActual.clasificacion || 'Sin datos';

        // 5. Renderizar
        renderizarDetalle(clasificacion);

    } catch (err) {
        console.error(err);
        mostrarToast('Error al cargar el postulante: ' + err.message, 'error');
        ocultarDetalle();
    }
}

/** Carga y cachea el JSON de deudas externas via proxy del backend */
async function cargarDeudasExternas() {
    if (deudasExternasAll !== null) return; // Ya tenemos cache

    const token = localStorage.getItem('jwt_token');
    try {
        const res = await fetch(API_DEUDAS_EXT, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (res.status === 503) {
            // El servidor externo (Render.com) puede tener cold start
            const data = await res.json().catch(() => ({}));
            const msg = data.error || 'El servicio externo no está disponible.';
            mostrarToast('⚠️ ' + msg, 'error');
            deudasExternasAll = [];
            return;
        }

        if (!res.ok) throw new Error('Error al consultar el proxy de deudas (' + res.status + ')');
        deudasExternasAll = await res.json();

    } catch (err) {
        console.warn('No se pudo cargar el API de deudas:', err.message);
        mostrarToast('No se pudo cargar el registro de deudas externas. Intente nuevamente.', 'error');
        deudasExternasAll = [];
    }
}

/** Convierte la clasificación de la API externa al texto del sistema */
function mapearClasificacion(claveAPI) {
    const mapa = {
        'SOCIO_PAGADOR':           'Socio Pagador',
        'SOCIO_PAGADOR_ESPORADICO': 'Socio Pagador Esporádico',
        'SOCIO_RENUENTE_PAGO':     'Socio Renuente a Pago',
    };
    return mapa[claveAPI] || claveAPI;
}

// ─── Renderizar detalle ───────────────────────────────────────────────
function mostrarLoadingDetalle() {
    document.getElementById('vistaLista').style.display   = 'none';
    document.getElementById('contenidoPostulante').style.display = 'block';
    document.getElementById('datosPersonales').innerHTML = '<p style="color:#9ca3af;font-size:13px;">Cargando...</p>';
    document.getElementById('deudasContenedor').innerHTML  = '<p style="color:#9ca3af;font-size:13px;text-align:center;padding:20px">Cargando deudas desde API externo...</p>';
}

function ocultarDetalle() {
    document.getElementById('vistaLista').style.display = 'block';
    document.getElementById('contenidoPostulante').style.display = 'none';
}

function renderizarDetalle(clasificacion) {
    if (!postulanteActual) return;

    document.getElementById('vistaLista').style.display = 'none';
    document.getElementById('contenidoPostulante').style.display = 'block';

    renderizarDatosPersonales();
    renderizarClasificacion(clasificacion);
    renderizarDeudas();
}

function renderizarDatosPersonales() {
    const p  = postulanteActual;
    const nombre = p.nombres
        ? `${p.nombres} ${p.apellidoPaterno || ''} ${p.apellidoMaterno || ''}`.trim()
        : (p.razonSocial || '—');

    const campos = [
        { label: 'Nombre / Razón Social', valor: nombre },
        { label: 'Tipo Documento',         valor: p.tipoDocumento || '—' },
        { label: 'Nº Documento',           valor: p.numeroDocumento || '—' },
        { label: 'Correo Electrónico',     valor: p.correoElectronico || '—' },
        { label: 'Teléfono',               valor: p.telefono || '—' },
        { label: 'Tipo de Interés',        valor: p.tipoInteres || '—' },
        { label: 'Fecha Nacimiento',       valor: p.fechaNacimiento ? formatearFecha(p.fechaNacimiento) : '—' },
        { label: 'Fecha Registro',         valor: p.fechaRegistro  ? formatearFecha(p.fechaRegistro)  : '—' },
        { label: 'Estado',                 valor: p.estadoPostulacion || '—' },
    ];

    document.getElementById('datosPersonales').innerHTML = campos.map(c => `
        <div class="data-field">
            <label>${c.label}</label>
            <span>${c.valor}</span>
        </div>`).join('');
}

function renderizarClasificacion(clasificacion) {
    let clase = 'clas-default';
    let icono = '•';
    let desc  = '';

    if (clasificacion.toLowerCase().includes('renuente')) {
        clase = 'clas-renuente';
        icono = '⚠';
        desc  = 'El postulante tiene deudas pendientes o vencidas sin regularizar. Se recomienda rechazar o solicitar subsanación.';
    } else if (clasificacion.toLowerCase().includes('esporádico') || clasificacion.toLowerCase().includes('esporadico')) {
        clase = 'clas-esporadico';
        icono = '~';
        desc  = 'El postulante tiene historial de pagos pero con retrasos. Evalúa aprobar con seguimiento.';
    } else if (clasificacion.toLowerCase().includes('pagador')) {
        clase = 'clas-pagador';
        icono = '✓';
        desc  = 'El postulante no presenta deudas pendientes. Apto para aprobación.';
    }

    document.getElementById('clasificacionContainer').innerHTML = `
        <span class="clasificacion-badge ${clase}">${icono} ${clasificacion}</span>`;
    document.getElementById('clasificacionDesc').textContent = desc;
}

function renderizarDeudas() {
    const container = document.getElementById('deudasContenedor');

    if (!deudasExternas || deudasExternas.length === 0) {
        container.innerHTML = `
            <div style="text-align:center;padding:32px;color:#9ca3af;">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" style="opacity:.4;display:block;margin:0 auto 12px"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
                <p style="font-size:14px;font-weight:600;">Sin deudas externas registradas</p>
                <p style="font-size:13px;margin-top:4px;">Este postulante no figura en el registro externo de deudas.</p>
            </div>`;
        return;
    }

    container.innerHTML = `
        <table class="deudas-table">
            <thead>
                <tr>
                    <th>Club / Entidad</th>
                    <th>Monto</th>
                    <th>Fecha Registro</th>
                    <th>Estado</th>
                    <th>Verificada</th>
                    <th>Observaciones</th>
                    <th style="width:110px;">Acción</th>
                </tr>
            </thead>
            <tbody>
                ${deudasExternas.map((d, idx) => filadeDeuda(d, idx)).join('')}
            </tbody>
        </table>`;
}
function filadeDeuda(d, idx) {
    const monto      = parseFloat(d.monto_deuda || 0);
    const montoClass = monto > 0 ? 'monto-deuda' : 'monto-cero';
    const montoTexto = `S/. ${monto.toFixed(2)}`;
    const verificada = d.id_verificador != null;

    function getBadgeEstado(estado) {
        const est = (estado || 'pendiente').toLowerCase();
        if (est === 'pendiente') return '<span class="badge badge-pendiente">Pendiente</span>';
        if (est === 'subsanado') return '<span class="badge badge-subsanado">Subsanado</span>';
        if (est === 'rechazado') return '<span class="badge badge-rechazado">Rechazado</span>';
        return `<span class="badge">${estado}</span>`;
    }

    return `
        <tr id="fila-deuda-${idx}">
            <td><strong>${d.nombre_club_origen || '—'}</strong></td>
            <td><span class="${montoClass}">${montoTexto}</span></td>
            <td>${d.fecha_registro ? formatearFecha(d.fecha_registro) : '—'}</td>
            <td>${getBadgeEstado(d.estado)}</td>
            <td>
                <span class="verificado-badge ${verificada ? 'verificado-si' : 'verificado-no'}">
                    ${verificada
                        ? '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> Verificada'
                        : '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="8" y1="12" x2="16" y2="12"/></svg> Sin verificar'}
                </span>
            </td>
            <td style="font-size:12px;color:#6b7280;max-width:180px;word-break:break-word;">
                ${d.observaciones_verificacion || '—'}
            </td>
            <td>
                <button
                    class="btn-verificar-row"
                    id="btn-verif-${idx}"
                    onclick="verificarDeudaExterna(${idx})"
                    ${verificada ? 'disabled' : ''}>
                    ${verificada ? '✓ Listo' : 'Verificar'}
                </button>
            </td>
        </tr>`;
}

// ─── Verificar deuda (desde BD local si existe, o efecto visual si es solo API) ──
async function verificarDeudaExterna(idx) {
    const deuda = deudasExternas[idx];
    if (!deuda) return;

    // Mostrar mini-modal para observaciones
    const obs = prompt('Observaciones de verificación (opcional):');
    if (obs === null) return; // Canceló

    // Si la deuda existe en la BD local (tiene id), la verificamos por API
    if (deuda.id) {
        const token = localStorage.getItem('jwt_token');
        try {
            const res = await fetch(`${API_BASE}/deudas/${deuda.id}/verificar`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ observaciones: obs })
            });
            if (!res.ok) throw new Error('Error al verificar en BD');
        } catch (err) {
            mostrarToast('No se pudo registrar en BD: ' + err.message, 'error');
        }
    }

    // Actualizar vista localmente (la deuda de la API externa no tiene id real de BD)
    deuda.id_verificador = 1; // Marcamos como verificada
    deuda.observaciones_verificacion = obs || 'Verified';

    const btn = document.getElementById(`btn-verif-${idx}`);
    if (btn) {
        btn.disabled = true;
        btn.textContent = '✓ Listo';
    }

    const fila = document.getElementById(`fila-deuda-${idx}`);
    if (fila) {
        const tdVerif = fila.querySelectorAll('td')[4];
        if (tdVerif) {
            tdVerif.innerHTML = `
                <span class="verificado-badge verificado-si">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    Verificada
                </span>`;
        }
        const tdObs = fila.querySelectorAll('td')[5];
        if (tdObs) tdObs.textContent = obs || '—';
    }

    mostrarToast('Deuda marcada como verificada', 'success');
}

// ─── Aprobar postulante ───────────────────────────────────────────────
async function aprobarPostulante() {
    if (!postulanteActual) { mostrarToast('Selecciona un postulante primero', 'error'); return; }
    if (!confirm(`¿Aprobar la solicitud de "${nombreCompleto()}"? Se creará automáticamente el Socio y el Usuario.`)) return;

    const token = localStorage.getItem('jwt_token');
    const btn   = document.getElementById('btnAprobar');

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Procesando...';

    try {
        const res = await fetch(`${API_BASE}/postulantes/${postulanteActual.idPostulante}/aprobar`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Error al aprobar');

        mostrarToast(`✓ Postulante aprobado. Socio y usuario creados exitosamente.`, 'success');

        // Limpiar y recargar
        setTimeout(() => {
            postulanteActual = null;
            deudasExternas   = [];
            ocultarDetalle();
            deudasExternasAll = null; // Invalidar cache para refresco
            cargarPostulantes();
        }, 1800);

    } catch (err) {
        console.error(err);
        mostrarToast('Error al aprobar: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Aprobar Postulante`;
    }
}

// ─── Rechazar postulante ──────────────────────────────────────────────
function mostrarModalRechazo() {
    if (!postulanteActual) { mostrarToast('Selecciona un postulante primero', 'error'); return; }

    document.getElementById('modalRechazoSubtitle').textContent =
        `Postulante: ${nombreCompleto()} — ${postulanteActual.tipoDocumento}: ${postulanteActual.numeroDocumento}`;
    document.getElementById('motivoRechazo').value = '';
    document.getElementById('modalRechazo').classList.add('open');
}

function cerrarModalRechazo() {
    document.getElementById('modalRechazo').classList.remove('open');
}

async function confirmarRechazo() {
    const motivo = document.getElementById('motivoRechazo').value.trim();
    if (!motivo) {
        document.getElementById('motivoRechazo').style.borderColor = '#ef4444';
        document.getElementById('motivoRechazo').focus();
        return;
    }
    document.getElementById('motivoRechazo').style.borderColor = '';

    const token = localStorage.getItem('jwt_token');
    const btn   = document.getElementById('btnConfirmarRechazo');
    btn.disabled = true;
    btn.textContent = 'Rechazando...';

    try {
        const res = await fetch(`${API_BASE}/postulantes/${postulanteActual.idPostulante}/rechazar`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ motivo })
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Error al rechazar');

        cerrarModalRechazo();
        mostrarToast('Postulante rechazado. Motivo registrado en historial.', 'success');

        setTimeout(() => {
            postulanteActual = null;
            deudasExternas   = [];
            ocultarDetalle();
            cargarPostulantes();
        }, 1800);

    } catch (err) {
        console.error(err);
        mostrarToast('Error al rechazar: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirmar Rechazo';
    }
}

// ─── Logout ───────────────────────────────────────────────────────────
function logout() {
    if (confirm('¿Deseas cerrar sesión?')) {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_role');
        localStorage.removeItem('username');
        window.location.href = '/login';
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────

function nombreCompleto() {
    if (!postulanteActual) return '';
    return postulanteActual.nombres
        ? `${postulanteActual.nombres} ${postulanteActual.apellidoPaterno || ''}`.trim()
        : (postulanteActual.razonSocial || 'Postulante');
}

function capitalizarPrimera(str) {
    if (!str) return '—';
    const s = str.toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function formatearFecha(fecha) {
    try {
        const d = new Date(fecha + 'T00:00:00');
        return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch {
        return fecha;
    }
}

// ─── Toast notifications ──────────────────────────────────────────────
function mostrarToast(mensaje, tipo = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${tipo}`;

    const icono = tipo === 'success'
        ? '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>'
        : '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>';

    toast.innerHTML = `${icono} <span>${mensaje}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'opacity 0.4s, transform 0.4s';
        toast.style.opacity    = '0';
        toast.style.transform  = 'translateX(60px)';
        setTimeout(() => toast.remove(), 400);
    }, 4500);
}
