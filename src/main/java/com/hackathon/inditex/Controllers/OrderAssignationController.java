package com.hackathon.inditex.Controllers;

import com.hackathon.inditex.DTO.AssignationResponseDTO;
import com.hackathon.inditex.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for order assignation.
 * Provides endpoint for assigning logistics centers to pending orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderAssignationController {

    @Autowired
    private OrderService orderService;

    /**
     * Assigns logistics centers to pending orders based on proximity and
     * availability.
     * Orders are processed in order of creation (by ID).
     * 
     * @return Information about processed orders
     */
    @PostMapping("/order-assignations")
    public ResponseEntity<AssignationResponseDTO> assignOrdersToCenters() {
        AssignationResponseDTO response = orderService.assignOrdersToCenters();
        return ResponseEntity.ok(response);
    }
}