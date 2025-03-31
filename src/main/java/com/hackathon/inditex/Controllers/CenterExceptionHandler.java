package com.hackathon.inditex.Controllers;

import com.hackathon.inditex.DTO.CenterResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CenterExceptionHandler extends ResponseEntityExceptionHandler {

    // Este manejador captura especificamente violaciones de integridad de datos
    // como intentar crear un centro en coordenadas donde ya existe uno
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CenterResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CenterResponseDTO("There is already a logistics center in that position."));
    }
}