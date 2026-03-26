package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "embarcacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Embarcacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_embarcacion")
    private Integer id;

    @Column(name = "nombre_embarcacion", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_embarcacion", nullable = false, length = 100)
    private String tipo;

    @Column(nullable = true, length = 50)
    private String matricula;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado_embarcacion", nullable = false, length = 20)
    private String estado; // Activa, Inactiva, En mantenimiento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;
}
