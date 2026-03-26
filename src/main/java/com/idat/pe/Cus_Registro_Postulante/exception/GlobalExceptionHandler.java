package com.idat.pe.Cus_Registro_Postulante.exception;

import com.idat.pe.Cus_Registro_Postulante.genericResponse.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones adaptado para flujos SSR y API.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores generales redirigiendo a una página de error en flujos web,
     * o devolviendo JSON si se detecta una petición de API.
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            GenericResponse<String, String> response = GenericResponse.<String, String>builder()
                    .message("Error inesperado en API")
                    .body(ex.getMessage())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("mensaje", "Ocurrió un inconveniente procesando tu solicitud.");
        mav.addObject("detalle", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<String, Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        GenericResponse<String, Map<String, String>> response = GenericResponse.<String, Map<String, String>>builder()
                .message("Error de validación")
                .body(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/");
    }
}
