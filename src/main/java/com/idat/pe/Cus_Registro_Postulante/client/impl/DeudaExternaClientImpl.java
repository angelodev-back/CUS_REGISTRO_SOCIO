package com.idat.pe.Cus_Registro_Postulante.client.impl;

import com.idat.pe.Cus_Registro_Postulante.client.DeudaExternaClient;
import com.idat.pe.Cus_Registro_Postulante.dto.ExternalDebtResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del cliente para consumir deudas externas desde API remota
 */
@Component
public class DeudaExternaClientImpl implements DeudaExternaClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DeudaExternaClientImpl.class);
    private static final String API_URL = "https://jsonubicaciongeografica.onrender.com/json/deudas-externas";
    
    private final RestTemplate restTemplate;
    
    public DeudaExternaClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public List<ExternalDebtResponseDTO> obtenerDeudas() {
        try {
            logger.info("Consumiendo deudas externas hacia: {}", API_URL);
            ExternalDebtResponseDTO[] deudas = restTemplate.getForObject(API_URL, ExternalDebtResponseDTO[].class);
            if (deudas != null) {
                return Arrays.asList(deudas);
            }
            return List.of();
        } catch (Exception e) {
            logger.error("Error API externa: {}", e.getMessage());
            return List.of();
        }
    }
}
