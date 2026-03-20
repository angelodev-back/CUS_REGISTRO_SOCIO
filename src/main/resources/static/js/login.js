// Login Manager - Autenticación de usuarios del sistema
class LoginManager {
    constructor() {
        this.form = document.getElementById('loginForm');
        this.btnLogin = document.getElementById('btnLogin');
        this.alertError = document.getElementById('alertError');
        this.errorMsg = document.getElementById('errorMsg');
        this.usernameInput = document.getElementById('username');
        this.passwordInput = document.getElementById('password');
        
        this.init();
    }

    init() {
        if (this.form) {
            this.form.addEventListener('submit', (e) => this.handleLogin(e));
        }
    }

    // Manejar envío del formulario
    async handleLogin(e) {
        e.preventDefault();

        const username = this.usernameInput.value.trim();
        const password = this.passwordInput.value.trim();

        // Validaciones básicas
        if (!username || !password) {
            this.showError('Por favor completa todos los campos');
            return;
        }

        // Deshabilitar botón
        this.btnLogin.disabled = true;
        this.btnLogin.innerHTML = `<svg viewBox="0 0 24 24" style="width:20px; height:20px; animation: spin 1s linear infinite; stroke:currentColor; stroke-width:2; fill:none;"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 1 10 10"/></svg> Validando...`;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok && data.body) {
                // Login exitoso
                const token = data.body.token;
                const rol = data.body.rol;

                // Guardar token en localStorage
                localStorage.setItem('jwt_token', token);
                localStorage.setItem('user_role', rol);
                localStorage.setItem('username', username);

                // Redirigir según rol
                this.redirectByRole(rol);
            } else {
                // Error en login
                this.showError(data.message || 'Usuario o contraseña incorrectos');
                this.btnLogin.disabled = false;
                this.btnLogin.innerHTML = `<svg viewBox="0 0 24 24" style="width:20px; height:20px; stroke:currentColor; stroke-width:2; fill:none;"><path d="M13 12h8M20 9l-3 3 3 3"/><path d="M4 12a8 8 0 1 0 16 0"/></svg> Acceder`;
            }
        } catch (error) {
            console.error('Error en autenticación:', error);
            this.showError('Error al conectar con el servidor. Intenta más tarde.');
            this.btnLogin.disabled = false;
            this.btnLogin.innerHTML = `<svg viewBox="0 0 24 24" style="width:20px; height:20px; stroke:currentColor; stroke-width:2; fill:none;"><path d="M13 12h8M20 9l-3 3 3 3"/><path d="M4 12a8 8 0 1 0 16 0"/></svg> Acceder`;
        }
    }

    // Mostrar error
    showError(message) {
        this.errorMsg.textContent = message;
        this.alertError.style.display = 'flex';
        
        // Auto-ocultar después de 5 segundos
        setTimeout(() => {
            this.alertError.style.display = 'none';
        }, 5000);
    }

    // Redirigir según rol
    redirectByRole(rol) {
        console.log('Rol del usuario:', rol);
        
        if (rol && rol.toUpperCase() === 'JEFE') {
            // Redirigir al dashboard del JEFE
            window.location.href = '/jefe/dashboard';
        } else if (rol && rol.toUpperCase() === 'SOCIO') {
            // Redirigir al dashboard del SOCIO
            window.location.href = '/socio/dashboard';
        } else {
            // Por defecto, ir a inicio
            window.location.href = '/';
        }
    }
}

// CSS para animación de carga
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style);

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    new LoginManager();
});
