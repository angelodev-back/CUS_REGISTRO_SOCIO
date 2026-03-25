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
        return attribute.name().toLowerCase();
    }

    @Override
    public EstadoPostulante convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return EstadoPostulante.valueOf(dbData.toUpperCase());
    }
}
