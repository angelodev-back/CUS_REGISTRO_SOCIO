package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "consolidado_gastos", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_socio_periodo_consolidado", columnNames = {"id_socio", "periodo"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsolidadoGastos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consolidado")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @Column(nullable = false)
    private LocalDate periodo;

    @Column(name = "total_gastos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalGastos;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;
}
