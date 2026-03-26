package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "estado_cuenta", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_socio_periodo_estado", columnNames = {"id_socio", "periodo"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cuenta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @Column(nullable = false)
    private LocalDate periodo;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "saldo_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;
}
