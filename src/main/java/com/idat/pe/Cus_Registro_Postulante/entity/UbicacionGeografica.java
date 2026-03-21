package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubicacion_geografica", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "tipo_ubicacion", "id_padre"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionGeografica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_ubicacion", nullable = false, length = 20)
    private String tipoUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre")
    private UbicacionGeografica padre;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(nullable = false, length = 20)
    private String estado;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "activo";
        }
    }
}
