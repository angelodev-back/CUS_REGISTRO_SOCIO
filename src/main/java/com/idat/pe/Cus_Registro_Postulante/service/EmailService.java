package com.idat.pe.Cus_Registro_Postulante.service;

public interface EmailService {
    void enviarCredenciales(String destinatario, String username, String tempPassword);
    void enviarNotificacionSubsanacion(String destinatario, String motivo);
}
