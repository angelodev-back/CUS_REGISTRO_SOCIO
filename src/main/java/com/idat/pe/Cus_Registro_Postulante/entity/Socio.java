package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad de Socio
 * Mapea la tabla: socio
 * Se crea cuando un postulante es aprobado
 */
@Entity
@Table(name = "socio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_postulante", nullable = false)
    private Postulante postulante;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_socio", nullable = false, length = 20)
    private String tipoSocio; // Nautico, Social

    @Column(name = "estado_socio", nullable = false, length = 20)
    private String estadoSocio; // activo, inactivo

    @Column(name = "fecha_activacion", nullable = false)
    private LocalDate fechaActivacion;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

}
