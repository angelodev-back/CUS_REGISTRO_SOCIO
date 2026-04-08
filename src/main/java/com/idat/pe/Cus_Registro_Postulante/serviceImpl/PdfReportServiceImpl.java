package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.service.PdfReportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfReportServiceImpl implements PdfReportService {

    @Override
    public ByteArrayInputStream generarInformeRegistro(PostulanteConDeudasDTO datos, HistorialEstadoPostulante validacion, com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision informe) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
            Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(100, 116, 139));
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(13, 148, 136));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fontTableHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // Título y Encabezado
            Paragraph title = new Paragraph("Informe de Registro y Validación", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Club Náutico Neptuno — Expediente de Membresía", fontSubtitle);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // Sección: Datos del Postulante
            agregarSeccionTitulo(document, "1. INFORMACIÓN DEL POSTULANTE", fontHeader);
            
            PdfPTable tablePostulante = new PdfPTable(2);
            tablePostulante.setWidthPercentage(100);
            tablePostulante.setSpacingBefore(10);
            tablePostulante.setSpacingAfter(20);

            String nombreCompleto = (datos.getNombres() != null && !datos.getNombres().isEmpty())
                ? (datos.getNombres() + " " + datos.getApellidoPaterno() + " " + datos.getApellidoMaterno())
                : datos.getRazonSocial();

            agregarCeldaInformativa(tablePostulante, "Nombre / Razón Social", nombreCompleto, fontLabel, fontValue);
            agregarCeldaInformativa(tablePostulante, "Documento", datos.getTipoDocumento() + ": " + datos.getNumeroDocumento(), fontLabel, fontValue);
            agregarCeldaInformativa(tablePostulante, "Correo", datos.getCorreoElectronico(), fontLabel, fontValue);
            agregarCeldaInformativa(tablePostulante, "Teléfono", datos.getTelefono(), fontLabel, fontValue);
            agregarCeldaInformativa(tablePostulante, "Dirección", datos.getDireccion(), fontLabel, fontValue);
            agregarCeldaInformativa(tablePostulante, "Tipo de Membresía", datos.getTipoInteres(), fontLabel, fontValue);

            document.add(tablePostulante);

            // Sección: Deudas Externas (Asociadas)
            agregarSeccionTitulo(document, "2. REPORTE DE DEUDAS EXTERNAS ASOCIADAS", fontHeader);
            
            if (datos.getDeudas() != null && !datos.getDeudas().isEmpty()) {
                PdfPTable tableDeudas = new PdfPTable(4);
                tableDeudas.setWidthPercentage(100);
                tableDeudas.setSpacingBefore(10);
                tableDeudas.setSpacingAfter(20);
                tableDeudas.setWidths(new float[]{40, 20, 20, 20});

                // Encabezados de tabla
                String[] headers = {"Club de Origen", "Monto", "Fecha Registro", "Estado"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHead));
                    cell.setBackgroundColor(new Color(30, 41, 59));
                    cell.setPadding(8);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableDeudas.addCell(cell);
                }

                // Filas de deudas
                for (DeudaExternaDTO d : datos.getDeudas()) {
                    tableDeudas.addCell(crearCeldaTabla(d.getNombreClubOrigen(), fontValue));
                    tableDeudas.addCell(crearCeldaTabla("S/ " + d.getMontoDeuda(), fontValue));
                    tableDeudas.addCell(crearCeldaTabla(d.getFechaRegistro(), fontValue));
                    tableDeudas.addCell(crearCeldaTabla(d.getEstado().toUpperCase(), fontValue));
                }
                document.add(tableDeudas);
            } else {
                Paragraph noDeudas = new Paragraph("No se registran deudas pendientes en otras instituciones náuticas o sociales.", fontValue);
                noDeudas.setSpacingBefore(10);
                noDeudas.setSpacingAfter(20);
                document.add(noDeudas);
            }

            // Sección: Validación Administrativa
            agregarSeccionTitulo(document, "3. DICTAMEN DE VALIDACIÓN", fontHeader);
            
            PdfPTable tableValidacion = new PdfPTable(1);
            tableValidacion.setWidthPercentage(100);
            tableValidacion.setSpacingBefore(10);
            tableValidacion.setSpacingAfter(30);

            PdfPCell cellVal = new PdfPCell();
            cellVal.setBackgroundColor(new Color(248, 250, 252));
            cellVal.setPadding(15);
            cellVal.setBorderColor(new Color(226, 232, 240));

            // Lógica de Informe Manual vs Automático
            String aprobadoPor = (informe != null && informe.getEstado().equals("FINALIZADO"))
                ? "Validado y Confirmado por Jefe de Servicios Navieros"
                : (validacion != null && validacion.getEmpleado() != null) 
                    ? "Evaluado por: " + validacion.getEmpleado().getNombres() + " " + validacion.getEmpleado().getApellidoPaterno()
                    : "Evaluación Preliminar del Sistema";
            
            String motivo = (informe != null && informe.getObservaciones() != null && !informe.getObservaciones().isBlank())
                ? informe.getObservaciones()
                : (validacion != null && validacion.getMotivo() != null)
                    ? validacion.getMotivo()
                    : "El postulante ha pasado la validación automática de deudas externas con clasificación: " + datos.getClasificacion();

            String recomendacionFinal = (informe != null && informe.getRecomendacionManual() != null && !informe.getRecomendacionManual().isBlank())
                ? "RECOMENDACIÓN FINAL: " + informe.getRecomendacionManual()
                : "RECOMENDACIÓN SUGERIDA: Según el historial financiero analizado, el postulante presenta un perfil compatible con la membresía del club.";

            Paragraph pVal = new Paragraph();
            pVal.add(new Chunk(aprobadoPor + "\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            pVal.add(new Chunk(motivo + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 9)));
            pVal.add(new Chunk(recomendacionFinal, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(13, 148, 136))));
            
            cellVal.addElement(pVal);
            tableValidacion.addCell(cellVal);
            document.add(tableValidacion);

            // Firmas
            PdfPTable tableFirmas = new PdfPTable(2);
            tableFirmas.setWidthPercentage(100);
            tableFirmas.setSpacingBefore(50);

            // Determinar nombre del responsable
            String nombreResponsable = (validacion != null && validacion.getEmpleado() != null)
                ? (validacion.getEmpleado().getNombres() + " " + validacion.getEmpleado().getApellidoPaterno())
                : (informe != null && informe.getEstado().equals("FINALIZADO"))
                    ? "Jefe de Servicios Navieros"
                    : "Responsable de Admisión";

            PdfPCell linea1 = new PdfPCell();
            linea1.setBorder(Rectangle.NO_BORDER);
            linea1.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph p1 = new Paragraph();
            p1.add(new Chunk("__________________________\n", fontLabel));
            p1.add(new Chunk("Firma del Postulante\n", fontLabel));
            p1.add(new Chunk(nombreCompleto.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK)));
            p1.setAlignment(Element.ALIGN_CENTER);
            linea1.addElement(p1);
            
            PdfPCell linea2 = new PdfPCell();
            linea2.setBorder(Rectangle.NO_BORDER);
            linea2.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph p2 = new Paragraph();
            p2.add(new Chunk("__________________________\n", fontLabel));
            p2.add(new Chunk("Responsable de Admisión\n", fontLabel));
            p2.add(new Chunk(nombreResponsable.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK)));
            p2.setAlignment(Element.ALIGN_CENTER);
            linea2.addElement(p2);

            tableFirmas.addCell(linea1);
            tableFirmas.addCell(linea2);

            document.add(tableFirmas);

            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void agregarSeccionTitulo(Document doc, String texto, Font font) throws DocumentException {
        Paragraph p = new Paragraph(texto, font);
        p.setSpacingBefore(15);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    private void agregarCeldaInformativa(PdfPTable table, String label, String value, Font fLabel, Font fValue) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", fLabel));
        p.add(new Chunk(value != null && !value.isEmpty() ? value : "N/A", fValue));
        
        cell.addElement(p);
        table.addCell(cell);
    }

    private PdfPCell crearCeldaTabla(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/A", font));
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}
