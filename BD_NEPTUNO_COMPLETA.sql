-- =====================================================
-- NEPTUNO CLUB NAUTICO - SCRIPT COMPLETO BD
-- =====================================================
-- Crea la base de datos completa con datos iniciales
-- Sin errores, lista para ejecutar
-- =====================================================

DROP DATABASE IF EXISTS Neptuno;
CREATE DATABASE Neptuno
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE Neptuno;

-- =========================================================
-- 1. TABLA DE ROLES
-- =========================================================
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 2. TABLA DE UBICACION GEOGRAFICA
-- =========================================================
CREATE TABLE ubicacion_geografica (
    id_ubicacion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_ubicacion VARCHAR(20) NOT NULL,
    id_padre INT NULL,
    codigo_postal VARCHAR(20) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',

    CONSTRAINT FK_ubicacion_padre FOREIGN KEY (id_padre)
        REFERENCES ubicacion_geografica(id_ubicacion),

    CONSTRAINT CK_ubicacion_tipo
        CHECK (tipo_ubicacion IN ('PAIS', 'DEPARTAMENTO', 'PROVINCIA', 'DISTRITO')),

    CONSTRAINT CK_ubicacion_estado
        CHECK (estado IN ('activo', 'inactivo')),

    CONSTRAINT UQ_ubicacion_nombre_tipo_padre
        UNIQUE (nombre, tipo_ubicacion, id_padre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_ubicacion_padre ON ubicacion_geografica(id_padre);
CREATE INDEX IX_ubicacion_tipo ON ubicacion_geografica(tipo_ubicacion);

-- =========================================================
-- 3. TABLA DE USUARIOS
-- =========================================================
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NULL,
    correo_electronico VARCHAR(100) NULL,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    estado_usuario VARCHAR(20) NOT NULL,
    id_ubicacion INT NULL,

    CONSTRAINT FK_usuario_rol FOREIGN KEY (id_rol)
        REFERENCES rol(id_rol),

    CONSTRAINT FK_usuario_ubicacion FOREIGN KEY (id_ubicacion)
        REFERENCES ubicacion_geografica(id_ubicacion),

    CONSTRAINT CK_usuario_estado
        CHECK (estado_usuario IN ('activo', 'inactivo', 'suspendido'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_usuario_ubicacion ON usuario(id_ubicacion);
CREATE INDEX IX_usuario_nombre ON usuario(nombre_usuario);

-- =========================================================
-- 4. TABLA DE POSTULANTES
-- =========================================================
CREATE TABLE postulante (
    id_postulante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(11) NOT NULL UNIQUE,

    nombres VARCHAR(100) NULL,
    apellido_paterno VARCHAR(100) NULL,
    apellido_materno VARCHAR(100) NULL,
    razon_social VARCHAR(200) NULL,

    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(15) NULL,
    direccion VARCHAR(200) NULL,
    id_ubicacion INT NULL,
    fecha_nacimiento DATE NULL,
    tipo_interes VARCHAR(20) NULL,
    codigo_postal VARCHAR(20) NULL,
    fecha_registro DATE NOT NULL,
    estado_postulacion VARCHAR(20) NOT NULL,

    CONSTRAINT FK_postulante_ubicacion FOREIGN KEY (id_ubicacion)
        REFERENCES ubicacion_geografica(id_ubicacion),

    CONSTRAINT CK_postulante_tipo_documento
        CHECK (tipo_documento IN ('DNI', 'RUC')),

    CONSTRAINT CK_postulante_estado
        CHECK (estado_postulacion IN ('pendiente', 'aprobado', 'rechazado', 'subsanado'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_postulante_numero ON postulante(numero_documento);
CREATE INDEX IX_postulante_email ON postulante(correo_electronico);

-- =========================================================
-- 5. TABLA DE HISTORIAL DE ESTADOS DEL POSTULANTE
-- =========================================================
CREATE TABLE historial_estado_postulante (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_postulante INT NOT NULL,
    id_jefe INT NOT NULL,
    fecha_cambio DATE NOT NULL,
    estado_anterior VARCHAR(20) NOT NULL,
    estado_nuevo VARCHAR(20) NOT NULL,
    motivo TEXT NULL,

    CONSTRAINT FK_historial_postulante FOREIGN KEY (id_postulante)
        REFERENCES postulante(id_postulante),

    CONSTRAINT FK_historial_jefe FOREIGN KEY (id_jefe)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 6. TABLA DE DEUDAS EXTERNAS
-- =========================================================
CREATE TABLE deuda_externa (
    id_deuda INT AUTO_INCREMENT PRIMARY KEY,
    id_postulante INT NOT NULL,
    nombre_club_origen VARCHAR(100) NOT NULL,
    monto_deuda DECIMAL(10,2) NOT NULL,
    fecha_registro DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    verificada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_verificacion DATE NULL,
    id_verificador INT NULL,
    observaciones_verificacion TEXT NULL,

    CONSTRAINT FK_deuda_postulante FOREIGN KEY (id_postulante)
        REFERENCES postulante(id_postulante),

    CONSTRAINT FK_deuda_verificador FOREIGN KEY (id_verificador)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_deuda_externa_postulante ON deuda_externa(id_postulante);

-- =========================================================
-- 7. TABLA DE SOCIOS
-- =========================================================
CREATE TABLE socio (
    id_socio INT AUTO_INCREMENT PRIMARY KEY,
    id_postulante INT NOT NULL UNIQUE,
    id_usuario INT NOT NULL UNIQUE,
    tipo_socio VARCHAR(20) NOT NULL,
    estado_socio VARCHAR(20) NOT NULL,
    fecha_activacion DATE NOT NULL,
    fecha_baja DATE NULL,

    CONSTRAINT FK_socio_postulante FOREIGN KEY (id_postulante)
        REFERENCES postulante(id_postulante),

    CONSTRAINT FK_socio_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario),

    CONSTRAINT CK_tipo_socio
        CHECK (tipo_socio IN ('Nautico', 'Social'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 8. TABLA DE EMBARCACIONES
-- =========================================================
CREATE TABLE embarcacion (
    id_embarcacion INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    nombre_embarcacion VARCHAR(100) NOT NULL,
    tipo_embarcacion VARCHAR(100) NOT NULL,
    matricula VARCHAR(50) NULL,
    descripcion TEXT NULL,
    estado_embarcacion VARCHAR(20) NOT NULL,

    CONSTRAINT FK_embarcacion_socio FOREIGN KEY (id_socio)
        REFERENCES socio(id_socio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 9. TABLA DE MOVIMIENTO DE NAVE
-- =========================================================
CREATE TABLE movimiento_nave (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    id_embarcacion INT NOT NULL,
    fecha_salida DATE NOT NULL,
    fecha_retorno DATE NOT NULL,
    itinerario TEXT NOT NULL,
    estado_movimiento VARCHAR(20) NOT NULL,

    CONSTRAINT FK_movimiento_socio FOREIGN KEY (id_socio)
        REFERENCES socio(id_socio),

    CONSTRAINT FK_movimiento_embarcacion FOREIGN KEY (id_embarcacion)
        REFERENCES embarcacion(id_embarcacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_movimiento_fechas ON movimiento_nave(fecha_salida, fecha_retorno);

-- =========================================================
-- 10. TABLA DE PERSONAS EN MOVIMIENTO
-- =========================================================
CREATE TABLE persona_movimiento (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,
    id_movimiento INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) NULL,

    CONSTRAINT FK_persona_movimiento FOREIGN KEY (id_movimiento)
        REFERENCES movimiento_nave(id_movimiento),

    CONSTRAINT CK_persona_movimiento_tipo
        CHECK (tipo IN ('tripulante', 'pasajero'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 11. TABLA DE PERMISOS DE NAVEGACION
-- =========================================================
CREATE TABLE permiso_navegacion (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    id_movimiento INT NOT NULL,
    fecha_tramite DATE NOT NULL,
    estado_permiso VARCHAR(20) NOT NULL,
    observaciones TEXT NULL,

    CONSTRAINT FK_permiso_movimiento FOREIGN KEY (id_movimiento)
        REFERENCES movimiento_nave(id_movimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 12. TABLA DE CONSOLIDADO MENSUAL DE GASTOS
-- =========================================================
CREATE TABLE consolidado_gastos (
    id_consolidado INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    periodo DATE NOT NULL,
    total_gastos DECIMAL(10,2) NOT NULL,
    fecha_generacion DATE NOT NULL,

    CONSTRAINT FK_consolidado_socio FOREIGN KEY (id_socio)
        REFERENCES socio(id_socio),

    CONSTRAINT UQ_socio_periodo_consolidado UNIQUE (id_socio, periodo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 13. TABLA DE TIPO DE PAGO
-- =========================================================
CREATE TABLE tipo_pago (
    id_tipo_pago INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 14. TABLA DE DOCUMENTO DE PAGO
-- =========================================================
CREATE TABLE documento_pago (
    id_documento_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_consolidado INT NOT NULL,
    tipo_comprobante VARCHAR(20) NOT NULL,
    serie VARCHAR(5) NOT NULL,
    numero INT NOT NULL,
    fecha_emision DATE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    igv DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    fecha_pago DATE NULL,
    monto_pagado DECIMAL(10,2) NULL,
    codigo_transaccion VARCHAR(50) NULL,
    estado_transaccion VARCHAR(20) NULL,
    id_tipo_pago INT NULL,

    CONSTRAINT FK_documento_consolidado FOREIGN KEY (id_consolidado)
        REFERENCES consolidado_gastos(id_consolidado),

    CONSTRAINT FK_documento_tipo_pago FOREIGN KEY (id_tipo_pago)
        REFERENCES tipo_pago(id_tipo_pago),

    CONSTRAINT CK_documento_tipo_comprobante
        CHECK (tipo_comprobante IN ('boleta', 'factura'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 15. TABLA DE DETALLE DEL DOCUMENTO DE PAGO
-- =========================================================
CREATE TABLE detalle_documento_pago (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_documento_pago INT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT FK_detalle_documento_pago FOREIGN KEY (id_documento_pago)
        REFERENCES documento_pago(id_documento_pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IX_detalle_documento ON detalle_documento_pago(id_documento_pago);

-- =========================================================
-- 16. TABLA DE ESTADO DE CUENTA DEL SOCIO
-- =========================================================
CREATE TABLE estado_cuenta (
    id_estado_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    periodo DATE NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    saldo_pendiente DECIMAL(10,2) NOT NULL,
    fecha_actualizacion DATE NOT NULL,

    CONSTRAINT FK_estado_cuenta_socio FOREIGN KEY (id_socio)
        REFERENCES socio(id_socio),

    CONSTRAINT UQ_socio_periodo_estado UNIQUE (id_socio, periodo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 17. TABLA DE QUEJAS Y RECLAMOS
-- =========================================================
CREATE TABLE queja_reclamo (
    id_queja_reclamo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo_registro VARCHAR(20) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro DATE NOT NULL,
    estado_seguimiento VARCHAR(20) NOT NULL,

    CONSTRAINT FK_queja_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 18. TABLA DE SEGUIMIENTO DE QUEJAS Y RECLAMOS
-- =========================================================
CREATE TABLE seguimiento_queja_reclamo (
    id_seguimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_queja_reclamo INT NOT NULL,
    fecha_seguimiento DATE NOT NULL,
    detalle_seguimiento TEXT NOT NULL,
    estado_actual VARCHAR(20) NOT NULL,

    CONSTRAINT FK_seguimiento_queja FOREIGN KEY (id_queja_reclamo)
        REFERENCES queja_reclamo(id_queja_reclamo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- 1. Insertar Roles
INSERT INTO rol (nombre_rol, descripcion) VALUES 
('JEFE', 'Jefe de Atención al Cliente'),
('SOCIO', 'Socio del Club Náutico Neptuno'),
('ADMIN', 'Administrador del Sistema');

-- 2. Obtener IDs de roles para usarlos
SET @jefe_id := (SELECT id_rol FROM rol WHERE nombre_rol = 'JEFE');
SET @socio_id := (SELECT id_rol FROM rol WHERE nombre_rol = 'SOCIO');

-- 3. Insertar Usuario JEFE de prueba
-- Contraseña: 123456
-- Hash generado con BCrypt (puedes cambiar por uno tuyo usando /debug/bcrypt)
INSERT INTO usuario (
    id_rol,
    dni,
    nombres,
    apellido_paterno,
    apellido_materno,
    direccion,
    correo_electronico,
    nombre_usuario,
    contrasena_hash,
    estado_usuario
) VALUES (
    @jefe_id,
    '12345678',
    'Juan',
    'Pérez',
    'García',
    'Av. Náutica 1234, Lima',
    'jefe@neptuno.club',
    'jefe_admin',
    '$2a$12$AsGDqFuMqbKvbt/ccAU5Fea35hLPI7qFkcR/dgaQBP10Ichmk9J1K',
    'activo'
);

-- 4. Insertar Usuario SOCIO de prueba
INSERT INTO usuario (
    id_rol,
    dni,
    nombres,
    apellido_paterno,
    apellido_materno,
    direccion,
    correo_electronico,
    nombre_usuario,
    contrasena_hash,
    estado_usuario
) VALUES (
    @socio_id,
    '87654321',
    'María',
    'López',
    'Rodríguez',
    'Av. Libertad 5678, Lima',
    'maria@email.com',
    'maria_socio',
    '$2a$12$AsGDqFuMqbKvbt/ccAU5Fea35hLPI7qFkcR/dgaQBP10Ichmk9J1K',
    'activo'
);

-- =====================================================
-- VERIFICACIÓN - EJECUTAR QUERIES
-- =====================================================

-- Ver roles creados
SELECT 'ROLES CREADOS' as 'INFO';
SELECT * FROM rol;

-- Ver usuarios creados
SELECT 'USUARIOS CREADOS' as 'INFO';
SELECT id_usuario, nombre_usuario, nombres, apellido_paterno, estado_usuario, r.nombre_rol 
FROM usuario u 
LEFT JOIN rol r ON u.id_rol = r.id_rol 
ORDER BY u.id_usuario;

-- =====================================================
-- CREDENCIALES DE PRUEBA
-- =====================================================
-- Usuario JEFE:
--   - Usuario: jefe_admin
--   - Contraseña: 123456
--   - Rol: JEFE
--   - Estado: activo
--
-- Usuario SOCIO:
--   - Usuario: maria_socio
--   - Contraseña: 123456
--   - Rol: SOCIO
--   - Estado: activo
-- =====================================================
