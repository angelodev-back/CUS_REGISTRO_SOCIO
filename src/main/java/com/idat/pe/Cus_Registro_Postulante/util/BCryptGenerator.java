package com.idat.pe.Cus_Registro_Postulante.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt seguros para contraseñas
 * USA ESTO PARA GENERAR HASHES DE PRUEBA
 */
public class BCryptGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Contraseña a hashear
        String password = "123456";
        
        // Generar hash
        String hashedPassword = encoder.encode(password);
        
        System.out.println("===========================================");
        System.out.println("BCrypt Password Generator");
        System.out.println("===========================================");
        System.out.println("Contraseña original: " + password);
        System.out.println("Hash BCrypt generado: " + hashedPassword);
        System.out.println("===========================================");
        System.out.println("Copia este hash en tu script SQL:");
        System.out.println("'" + hashedPassword + "'");
        System.out.println("===========================================");
    }
}
