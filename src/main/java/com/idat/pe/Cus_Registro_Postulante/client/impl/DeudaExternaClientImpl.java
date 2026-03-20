package com.idat.pe.Cus_Registro_Postulante.client.impl;

import com.idat.pe.Cus_Registro_Postulante.client.DeudaExternaClient;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del cliente para consumir deudas externas desde API remota
 * Aplica SOLID: Single Responsibility - solo consume datos del API
 */
@Component
public class DeudaExternaClientImpl implements DeudaExternaClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DeudaExternaClientImpl.class);
    private static final String API_URL = "https://jsonubicaciongeografica.onrender.com/json/deudas-externas";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public DeudaExternaClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public List<DeudaExternaDTO> obtenerDeudas() {
        try {
            logger.info("Consumiendo deudas externas desde: {}", API_URL);
            
            DeudaExternaDTO[] deudas = restTemplate.getForObject(API_URL, DeudaExternaDTO[].class);
            
            if (deudas != null) {
                logger.info("Se obtuvieron {} deudas desde el API remoto", deudas.length);
                return Arrays.asList(deudas);
            }
            
            logger.warn("No se obtuvieron deudas del API remoto");
            return List.of();
            
        } catch (Exception e) {
            logger.error("Error al consumir API de deudas externas: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
