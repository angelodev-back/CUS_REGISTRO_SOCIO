package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "postulante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postulante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulante")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 11)
    private String numeroDocumento;

    // Persona Natural
    @Column(name = "nombres", length = 100)
    private String nombres;
    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;
    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    // Persona Jurídica
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "tipo_interes", length = 20)
    private String tipoInteres;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Convert(converter = EstadoPostulanteConverter.class)
    @Column(name = "estado_postulacion", nullable = false, length = 20)
    private EstadoPostulante estado;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDate.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPostulante.PENDIENTE;
        }
    }
}
