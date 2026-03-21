package com.idat.pe.Cus_Registro_Postulante.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPostulanteConverter implements AttributeConverter<EstadoPostulante, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPostulante attribute) {
        if (attribute == null) {
            return null;
        }
        // La base de datos espera minúsculas debido al CHECK constraint
        return attribute.name().toLowerCase();
    }

    @Override
    public EstadoPostulante convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Hibernate lo lee de la base de datos y lo convierte al Enum en mayúsculas
        return EstadoPostulante.valueOf(dbData.toUpperCase());
    }
}
