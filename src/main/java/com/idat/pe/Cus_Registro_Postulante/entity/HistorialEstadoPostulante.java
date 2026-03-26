package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad de Historial de Estado del Postulante
 * Mapea la tabla: historial_estado_postulante
 * Registra los cambios de estado del postulante con motivo
 */
@Entity
@Table(name = "historial_estado_postulante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoPostulante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulante", nullable = false)
    private Postulante postulante;

    @Column(name = "id_jefe", nullable = false)
    private Integer idJefe;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDate fechaCambio;

    @Column(name = "estado_anterior", nullable = false, length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private String estadoNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Transient
    private Usuario jefe;
}
