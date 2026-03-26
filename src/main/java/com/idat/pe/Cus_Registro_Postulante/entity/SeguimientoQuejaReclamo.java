package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "seguimiento_queja_reclamo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoQuejaReclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_queja_reclamo", nullable = false)
    private QuejaReclamo quejaReclamo;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDate fechaSeguimiento;

    @Column(name = "detalle_seguimiento", columnDefinition = "TEXT", nullable = false)
    private String detalle;

    @Column(name = "estado_actual", nullable = false, length = 20)
    private String estadoActual;
}
