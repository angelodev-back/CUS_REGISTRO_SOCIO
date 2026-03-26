package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "queja_reclamo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuejaReclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_queja_reclamo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_registro", nullable = false, length = 20)
    private String tipo; // queja, reclamo

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "estado_seguimiento", nullable = false, length = 20)
    private String estado;
}
