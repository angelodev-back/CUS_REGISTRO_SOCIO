package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision;

import java.io.ByteArrayInputStream;

public interface PdfReportService {
    ByteArrayInputStream generarInformeRegistro(PostulanteConDeudasDTO datos, HistorialEstadoPostulante validacion, InformeAdmision informe);
}
