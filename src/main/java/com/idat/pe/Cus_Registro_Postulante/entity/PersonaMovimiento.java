package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persona_movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimiento", nullable = false)
    private MovimientoNave movimiento;

    @Column(nullable = false, length = 20)
    private String tipo; // tripulante, pasajero

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "documento_identidad", length = 20)
    private String documentoIdentidad;
}
