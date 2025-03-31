package com.hackathon.inditex.Controllers;

import com.hackathon.inditex.DTO.CenterDTO;
import com.hackathon.inditex.DTO.CenterResponseDTO;
import com.hackathon.inditex.Entities.Center;
import com.hackathon.inditex.Services.CenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing logistics centers.
 * Provides endpoints for creating, reading, updating, and deleting centers.
 */
@RestController
@RequestMapping("/api/centers")
public class CenterController {

    @Autowired
    private CenterService centerService;

    /**
     * Creates a new logistics center
     * 
     * @param centerDTO The center data to create
     * @return Response with appropriate message and status code
     */
    @PostMapping
    public ResponseEntity<CenterResponseDTO> createCenter(@RequestBody CenterDTO centerDTO) {
        CenterResponseDTO response = centerService.createCenter(centerDTO);

        if (response.getMessage().equals("Logistics center created successfully.")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            // According to the table in README, for error cases return 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves all logistics centers
     * 
     * @return List of all centers
     */
    @GetMapping
    public ResponseEntity<List<Center>> getAllCenters() {
        List<Center> centers = centerService.getAllCenters();
        return ResponseEntity.ok(centers);
    }

    /**
     * Updates an existing logistics center
     * 
     * @param id        The ID of the center to update
     * @param centerDTO The data to update
     * @return Response with appropriate message and status code
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> updateCenter(@PathVariable Long id, @RequestBody CenterDTO centerDTO) {
        CenterResponseDTO response = centerService.updateCenter(id, centerDTO);

        if (response.getMessage().equals("Center not found.")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (isErrorResponse(response)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Deletes a logistics center
     * 
     * @param id The ID of the center to delete
     * @return Response with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> deleteCenter(@PathVariable Long id) {
        CenterResponseDTO response = centerService.deleteCenter(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to check if a response is an error response
     */
    private boolean isErrorResponse(CenterResponseDTO response) {
        return response.getMessage().equals("Current load cannot exceed max capacity.") ||
                response.getMessage().equals("There is already a logistics center in that position.");
    }
}