package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(unique = true, nullable = false, length = 11)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "correo_electronico", length = 100)
    private String correoElectronico;

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String password;

    @Column(name = "estado_usuario", nullable = false, length = 20)
    private String estado; // ACTIVO, INACTIVO
}
