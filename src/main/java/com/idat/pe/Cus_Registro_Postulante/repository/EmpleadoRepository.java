package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.Empleado;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Empleado
 * Contiene métodos de acceso a datos específicos para empleados
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    /**
     * Busca un empleado por su número de DNI
     * @param dni Número de documento del empleado
     * @return Optional con el empleado si existe
     */
    Optional<Empleado> findByDni(String dni);

    /**
     * Busca un empleado por su usuario asociado
     * @param usuario Usuario vinculado al empleado
     * @return Optional con el empleado si existe
     */
    Optional<Empleado> findByUsuario(Usuario usuario);

    /**
     * Verifica si existe un empleado con un DNI específico
     * @param dni Número de documento
     * @return true si existe, false en caso contrario
     */
    boolean existsByDni(String dni);
}
