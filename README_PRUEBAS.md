# Plan de Pruebas y Casos de Prueba - Club Náutico Neptuno (Grupo 4)

Este documento detalla el plan de pruebas y los casos específicos diseñados para validar las funcionalidades del Sistema de Registro y Auditoría de Socios.

---

## 1. Objetivo
Asegurar que el proceso de registro, validación externa y cambio de estado de los postulantes cumpla con los requisitos funcionales y de seguridad establecidos para el Club Náutico Neptuno.

---

## 2. Descripción de Columnas (Ficha Técnica)

| Columna | Instrucciones |
|:---|:---|
| **Id** | Identificador único del caso de prueba. |
| **Caso de Prueba** | Título descriptivo. |
| **Descripción** | Elementos, funcionalidades y acciones ejercidas. |
| **Fecha** | Fecha de creación del caso. |
| **Área Funcional** | Módulo asociado (Registro, Auditoría, Reportes). |
| **Funcionalidad** | Característica específica probada. |
| **Datos de Entrada** | Valores, archivos o acciones necesarias para ejecutar. |
| **Resultado Esperado** | Salida o comportamiento previsto. |
| **Información de Seguimiento** | Resultados reales, estado y observaciones. |

---

## 3. Matriz de Casos de Prueba

| Id | Caso de Prueba | Descripción | Fecha | Área Funcional | Funcionalidad | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado | Última Fecha |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| **CP-01** | Registro con Validación DNI | Registro validando identidad con fuente externa (RENIEC). | 07/04/2026 | Registro | Validación Identidad | DNI válido, Botón "Validar". | Autocompleta nombres y apellidos. | Éxito | **Pasado** | 07/04/2026 |
| **CP-02** | Consulta de Deuda Externa | Verificación de deudas en otros clubes vía JSON Render. | 07/04/2026 | Auditoría | Verificación Externa | Número de Documento. | Muestra tabla de deudas y clasifica. | Éxito | **Pasado** | 07/04/2026 |
| **CP-03** | Generación Informe PDF | Exportación de informe formal con firmas institucionales. | 07/04/2026 | Reportes | Reporte Premium | Click "Descargar PDF". | PDF con firmas dinámicas y logos. | Éxito | **Pasado** | 07/04/2026 |
| **CP-04** | Aprobación Masiva/Individual | Cambio de estado y creación de credenciales de socio. | 07/04/2026 | Admisión | Gestión Estados | Botón "Aprobar". | Crea Socio y Usuario (Clave: DNI*!). | Éxito | **Pasado** | 07/04/2026 |
| **CP-05** | Rechazo con Motivo | Denegación de ingreso con notificación por correo. | 07/04/2026 | Admisión | Gestión Estados | Motivo de rechazo + "Confirmar". | Estado RECHAZADO + Envío de Email. | Éxito | **Pasado** | 07/04/2026 |
| **CP-06** | Reporte Excel de Historial | Exportación de todos los movimientos a hoja de cálculo. | 07/04/2026 | Reportes | Análisis Datos | Botón "Descargar Historial". | Archivo .xlsx con transiciones de estado. | Éxito | **Pasado** | 07/04/2026 |

---

## 4. Requerimientos de Ambiente de Pruebas

*   **Software:** Java 17+, Spring Boot 3.4.x, Maven.
*   **Servicios Externos:** Acceso a APIs de Render (Identidad y Deudas).
*   **Dependencias:** Apache POI (Excel), iText/OpenPDF (PDF).
*   **Seguridad:** BCryptPasswordEncoder activo para validación de cuentas.

---

## 5. Observaciones Generales de Seguimiento

1.  **Validación de Teléfonos:** Se implementó una normalización estricta (+51) para asegurar la integridad de los datos de contacto.
2.  **Caché de Consultas:** Para mitigar la latencia de servicios externos, se configuró un caché de 5 minutos en el servicio de deudas.
3.  **Firmas Dinámicas:** El motor de reportes asocia automáticamente al Jefe que realiza la acción en el documento físico, garantizando trazabilidad.

---
**Proyecto:** Club Náutico Neptuno - Grupo 4
**Responsable:** Equipo de Desarrollo Premium
