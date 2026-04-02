package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad de Usuario
 * Mapea la tabla: usuario
 * Contiene solo credenciales de autenticación
 * Datos personales están en: Empleado (para empleados) o Postulante (para socios)
 */
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "correo_electronico", unique = true, nullable = false, length = 100)
    private String correoElectronico;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "estado_usuario", nullable = false)
    private Boolean estadoUsuario; // true = activo, false = inactivo
}
