package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "movimiento_nave")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoNave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_embarcacion", nullable = false)
    private Embarcacion embarcacion;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "fecha_retorno", nullable = false)
    private LocalDate fechaRetorno;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String itinerario;

    @Column(name = "estado_movimiento", nullable = false, length = 20)
    private String estado;
}
