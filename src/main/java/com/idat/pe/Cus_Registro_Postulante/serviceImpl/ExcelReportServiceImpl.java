package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.repository.HistorialEstadoPostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.InformeAdmisionRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.service.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {

    @Autowired
    private HistorialEstadoPostulanteRepository historialRepository;

    @Autowired
    private InformeAdmisionRepository informeRepository;

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Override
    public ByteArrayInputStream generarReporteHistorialGeneral(List<HistorialEstadoPostulante> historial) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Historial General");

            // Cabecera Estilo
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Columnas
            String[] columns = {"ID Historial", "Fecha Cambio", "Postulante", "Doc. Identidad", "Estado Ant.", "Estado Nuevo", "Responsable", "Motivo / Obs."};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (HistorialEstadoPostulante h : historial) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(h.getId());
                row.createCell(1).setCellValue(h.getFechaCambio().toString());
                
                String nombrePostulante = h.getPostulante().getNombres() != null 
                    ? h.getPostulante().getNombres() + " " + h.getPostulante().getApellidoPaterno()
                    : h.getPostulante().getRazonSocial();
                row.createCell(2).setCellValue(nombrePostulante);
                row.createCell(3).setCellValue(h.getPostulante().getNumeroDocumento());
                row.createCell(4).setCellValue(h.getEstadoAnterior());
                row.createCell(5).setCellValue(h.getEstadoNuevo());
                
                String responsable = (h.getEmpleado() != null) 
                    ? h.getEmpleado().getNombres() + " " + h.getEmpleado().getApellidoPaterno()
                    : "Sistema / Postulante";
                row.createCell(6).setCellValue(responsable);
                row.createCell(7).setCellValue(h.getMotivo());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generarReporteDetallado(Integer idPostulante) {
        Postulante p = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        InformeAdmision info = informeRepository.findByPostulante_Id(idPostulante).orElse(null);
        List<HistorialEstadoPostulante> historial = historialRepository.findByPostulante_IdOrderByFechaCambioDesc(idPostulante);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Informe Detallado");

            // Estilos
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Sección 1: Datos del Postulante
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("INFORME DE ADMISIÓN - CLUB NEPTUNO");
            r0.getCell(0).setCellStyle(titleStyle);

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Postulante:");
            r2.createCell(1).setCellValue(p.getNombres() != null ? p.getNombres() + " " + p.getApellidoPaterno() : p.getRazonSocial());
            
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Documento:");
            r3.createCell(1).setCellValue(p.getNumeroDocumento());

            // Sección 2: Informe (Si existe)
            int currentLine = 5;
            if (info != null) {
                Row riTitle = sheet.createRow(currentLine++);
                riTitle.createCell(0).setCellValue("EVALUACIÓN DEL JEFE");
                riTitle.getCell(0).setCellStyle(titleStyle);

                Row ri1 = sheet.createRow(currentLine++);
                ri1.createCell(0).setCellValue("Observaciones:");
                ri1.createCell(1).setCellValue(info.getObservaciones());

                Row ri2 = sheet.createRow(currentLine++);
                ri2.createCell(0).setCellValue("Recomendación:");
                ri2.createCell(1).setCellValue(info.getRecomendacionManual());
                
                currentLine++;
            }

            // Sección 3: Historial
            Row rhTitle = sheet.createRow(currentLine++);
            rhTitle.createCell(0).setCellValue("HISTORIAL DE AUDITORÍA");
            rhTitle.getCell(0).setCellStyle(titleStyle);

            Row rhHeader = sheet.createRow(currentLine++);
            String[] cols = {"Fecha", "Estado Anterior", "Estado Nuevo", "Responsable", "Motivo"};
            for(int i=0; i<cols.length; i++) rhHeader.createCell(i).setCellValue(cols[i]);

            for (HistorialEstadoPostulante h : historial) {
                Row row = sheet.createRow(currentLine++);
                row.createCell(0).setCellValue(h.getFechaChangeStr()); // Usar helper
                row.createCell(1).setCellValue(h.getEstadoAnterior());
                row.createCell(2).setCellValue(h.getEstadoNuevo());
                row.createCell(3).setCellValue(h.getEmpleado() != null ? h.getEmpleado().getNombres() : "Sistema");
                row.createCell(4).setCellValue(h.getMotivo());
            }

            // Sección 4: Firmas
            currentLine += 3;
            Row rfLine = sheet.createRow(currentLine++);
            rfLine.createCell(0).setCellValue("__________________________");
            rfLine.createCell(2).setCellValue("__________________________");

            Row rfLabel = sheet.createRow(currentLine++);
            rfLabel.createCell(0).setCellValue("Firma del Postulante");
            rfLabel.createCell(2).setCellValue("Responsable de Admisión");

            Row rfNames = sheet.createRow(currentLine++);
            String nameP = (p.getNombres() != null ? (p.getNombres() + " " + p.getApellidoPaterno()) : p.getRazonSocial()).toUpperCase();
            rfNames.createCell(0).setCellValue(nameP);

            String nameR = "RESPONSABLE DE ADMISIÓN";
            if (info != null && info.getEstado().equals("FINALIZADO")) {
                nameR = "JEFE DE SERVICIOS NAVIEROS";
            } else if (!historial.isEmpty() && historial.get(0).getEmpleado() != null) {
                nameR = (historial.get(0).getEmpleado().getNombres() + " " + historial.get(0).getEmpleado().getApellidoPaterno()).toUpperCase();
            }
            rfNames.createCell(2).setCellValue(nameR);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel detallado: " + e.getMessage());
        }
    }
}
