package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "permiso_navegacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoNavegacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimiento", nullable = false)
    private MovimientoNave movimiento;

    @Column(name = "fecha_tramite", nullable = false)
    private LocalDate fechaTramite;

    @Column(name = "estado_permiso", nullable = false, length = 20)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
