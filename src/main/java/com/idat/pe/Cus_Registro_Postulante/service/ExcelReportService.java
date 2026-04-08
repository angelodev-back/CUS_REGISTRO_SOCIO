package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante;
import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelReportService {
    /**
     * Genera un reporte Excel del historial general de todos los postulantes.
     */
    ByteArrayInputStream generarReporteHistorialGeneral(List<HistorialEstadoPostulante> historial);

    /**
     * Genera un reporte Excel detallado de un solo postulante (Informe de Admisión + Historial).
     */
    ByteArrayInputStream generarReporteDetallado(Integer idPostulante);
}
