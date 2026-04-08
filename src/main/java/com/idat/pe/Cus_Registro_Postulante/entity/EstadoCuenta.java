package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad EstadoCuenta
 * Mapea la tabla: estado_cuenta
 */
@Entity
@Table(name = "estado_cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cuenta")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @Column(name = "periodo", nullable = false, length = 50)
    private String periodo; // Ej: "Abril 2024"

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(name = "monto_pagado", nullable = false)
    private Double montoPagado;

    @Column(name = "saldo_pendiente", nullable = false)
    private Double saldoPendiente;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
