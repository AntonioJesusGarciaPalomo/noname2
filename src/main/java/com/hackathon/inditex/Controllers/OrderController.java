package com.hackathon.inditex.Controllers;

import com.hackathon.inditex.DTO.OrderRequestDTO;
import com.hackathon.inditex.DTO.OrderResponseDTO;
import com.hackathon.inditex.Entities.Order;
import com.hackathon.inditex.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing orders.
 * Provides endpoints for creating and retrieving orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Creates a new order with PENDING status
     * 
     * @param orderRequestDTO The order data
     * @return The created order with status and message
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO response = orderService.createOrder(orderRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all orders
     * 
     * @return List of all orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}