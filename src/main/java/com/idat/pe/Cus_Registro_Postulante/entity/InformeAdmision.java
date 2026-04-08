package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "informe_admision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeAdmision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulante", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Postulante postulante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "recomendacion_manual", columnDefinition = "TEXT")
    private String recomendacionManual;

    @Column(name = "estado", length = 20)
    private String estado; // BORRADOR, FINALIZADO

    @Column(name = "fecha_ultima_edicion")
    private LocalDateTime fechaUltimaEdicion;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimaEdicion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "ACTUALIZADO";
        }
    }
}
