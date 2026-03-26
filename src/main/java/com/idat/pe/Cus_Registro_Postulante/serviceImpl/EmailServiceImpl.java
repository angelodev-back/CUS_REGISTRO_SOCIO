package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void enviarCredenciales(String destinatario, String username, String tempPassword) {
        logger.info("Enviando correo de credenciales a: {}", destinatario);
        
        String subject = "Bienvenido al Club Neptuno - Tus credenciales de acceso";
        String message = String.format(
            "Estimado socio,\n\n" +
            "Su solicitud de incorporación ha sido APROBADA. Bienvenido al Club Náutico Neptuno.\n\n" +
            "Sus credenciales de acceso al sistema son:\n" +
            "Usuario: %s\n" +
            "Contraseña Temporal: %s\n\n" +
            "Por favor, cambie su contraseña al iniciar sesión por primera vez.\n\n" +
            "Atentamente,\n" +
            "Administración Club Neptuno",
            username, tempPassword
        );

        enviarCorreo(destinatario, subject, message);
    }

    @Override
    public void enviarNotificacionSubsanacion(String destinatario, String motivo) {
        logger.info("Enviando notificación de subsanación a: {}", destinatario);
        
        String subject = "Notificación de Subsanación - Club Neptuno";
        String message = String.format(
            "Estimado postulante,\n\n" +
            "Su solicitud requiere subsanación por el siguiente motivo:\n" +
            "%s\n\n" +
            "Puede ingresar al portal con su número de documento para actualizar sus datos.\n\n" +
            "Atentamente,\n" +
            "Administración Club Neptuno",
            motivo
        );

        enviarCorreo(destinatario, subject, message);
    }

    private void enviarCorreo(String to, String subject, String text) {
        if (mailSender == null) {
            logger.warn("JavaMailSender no está configurado. Simulación de envío de correo:");
            logger.info("TO: {}", to);
            logger.info("SUBJECT: {}", subject);
            logger.info("BODY: \n{}", text);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Correo enviado exitosamente a {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar correo a {}: {}", to, e.getMessage());
        }
    }
}
