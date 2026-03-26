package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad de Deuda Externa
 * Mapea la tabla: deuda_externa
 * Representa deudas externas de postulantes con clubes u organizaciones
 */
@Entity
@Table(name = "deuda_externa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeudaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deuda")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulante", nullable = false)
    private Postulante postulante;

    @Column(name = "nombre_club_origen", nullable = false, length = 100)
    private String nombreClubOrigen;

    @Column(name = "monto_deuda", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDeuda;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false, length = 20)
    private String estado; // pendiente, pagado, vencido

    @Column(nullable = false)
    private Boolean verificada;

    @Column(name = "fecha_verificacion")
    private LocalDate fechaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_verificador")
    private Usuario verificador;

    @Column(name = "observaciones_verificacion", columnDefinition = "TEXT")
    private String observacionesVerificacion;
}
