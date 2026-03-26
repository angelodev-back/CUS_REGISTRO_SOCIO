package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "documento_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento_pago")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consolidado", nullable = false)
    private ConsolidadoGastos consolidado;

    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private String tipoComprobante; // boleta, factura

    @Column(nullable = false, length = 5)
    private String serie;

    @Column(nullable = false)
    private Integer numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "codigo_transaccion", length = 50)
    private String codigoTransaccion;

    @Column(name = "estado_transaccion", length = 20)
    private String estadoTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_pago")
    private TipoPago tipoPago;
}
