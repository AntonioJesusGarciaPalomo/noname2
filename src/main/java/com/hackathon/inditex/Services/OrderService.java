package com.hackathon.inditex.Services;

import com.hackathon.inditex.DTO.*;
import com.hackathon.inditex.Entities.Center;
import com.hackathon.inditex.Entities.Order;
import com.hackathon.inditex.Repositories.CenterRepository;
import com.hackathon.inditex.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class that handles order operations and center assignments.
 * Manages the creation of orders and assignment of logistics centers.
 */
@Service
public class OrderService {

    // Earth radius in kilometers, used for distance calculation
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Status constants for better code readability
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_AVAILABLE = "AVAILABLE";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CenterRepository centerRepository;

    /**
     * Creates a new order with PENDING status
     * 
     * @param orderRequestDTO The order request data
     * @return A response with order details and success message
     */
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        // Create a new order entity
        Order order = new Order();
        order.setCustomerId(orderRequestDTO.getCustomerId());
        order.setSize(orderRequestDTO.getSize());
        order.setCoordinates(orderRequestDTO.getCoordinates());
        order.setStatus(STATUS_PENDING);
        order.setAssignedCenter(null);

        // Save order to database
        Order savedOrder = orderRepository.save(order);

        // Create and populate response object
        OrderResponseDTO response = createOrderResponse(savedOrder);

        return response;
    }

    /**
     * Retrieves all orders from the database
     * 
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Assigns logistics centers to pending orders based on proximity and
     * availability
     * 
     * @return Response with processed orders information
     */
    @Transactional
    public AssignationResponseDTO assignOrdersToCenters() {
        // Get all orders with PENDING status
        List<Order> pendingOrders = orderRepository.findByStatus(STATUS_PENDING);

        // Sort orders by ID to prioritize older orders
        pendingOrders.sort(Comparator.comparing(Order::getId));

        // Get all available centers
        List<Center> availableCenters = getAvailableCenters();

        List<ProcessedOrderDTO> processedOrders = new ArrayList<>();

        // Process each pending order
        for (Order order : pendingOrders) {
            ProcessedOrderDTO processedOrder = processOrder(order, availableCenters);
            processedOrders.add(processedOrder);

            // If order was assigned, update the availableCenters list to reflect the new
            // currentLoad
            updateAvailableCentersIfOrderAssigned(processedOrder, availableCenters);
        }

        AssignationResponseDTO response = new AssignationResponseDTO();
        response.setProcessedOrders(processedOrders);

        return response;
    }

    /**
     * Processes a single order for center assignment
     * Finds the nearest compatible center with available capacity
     * 
     * @param order            The order to process
     * @param availableCenters List of available centers
     * @return Processed order information
     */
    private ProcessedOrderDTO processOrder(Order order, List<Center> availableCenters) {
        ProcessedOrderDTO processedOrder = new ProcessedOrderDTO();
        processedOrder.setOrderId(order.getId());

        // Filter centers that support the order type
        List<Center> compatibleCenters = findCompatibleCenters(order, availableCenters);

        if (compatibleCenters.isEmpty()) {
            // No centers support this order type
            return createPendingOrderResponse(
                    order.getId(),
                    "No available centers support the order type.");
        }

        // Find the nearest center with available capacity
        CenterDistancePair nearestCenter = findNearestCenterWithCapacity(order, compatibleCenters);

        if (nearestCenter == null) {
            // All centers are at maximum capacity
            return createPendingOrderResponse(
                    order.getId(),
                    "All centers are at maximum capacity.");
        }

        // Assign the order to the nearest center
        assignOrderToCenter(order, nearestCenter.center);

        // Create success response
        processedOrder.setDistance(nearestCenter.distance);
        processedOrder.setAssignedLogisticsCenter(nearestCenter.center.getName());
        processedOrder.setStatus(STATUS_ASSIGNED);

        return processedOrder;
    }

    /**
     * Calculates the haversine distance between two points on Earth
     * 
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Helper method to create an OrderResponseDTO from an Order entity
     */
    private OrderResponseDTO createOrderResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setSize(order.getSize());
        response.setAssignedLogisticsCenter(order.getAssignedCenter());
        response.setCoordinates(order.getCoordinates());
        response.setStatus(order.getStatus());
        response.setMessage("Order created successfully in PENDING status.");
        return response;
    }

    /**
     * Helper method to get all available centers
     */
    private List<Center> getAvailableCenters() {
        return centerRepository.findAll().stream()
                .filter(center -> STATUS_AVAILABLE.equals(center.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to update available centers list when an order is assigned
     */
    private void updateAvailableCentersIfOrderAssigned(ProcessedOrderDTO processedOrder,
            List<Center> availableCenters) {
        if (STATUS_ASSIGNED.equals(processedOrder.getStatus())) {
            for (Center center : availableCenters) {
                if (center.getName().equals(processedOrder.getAssignedLogisticsCenter())) {
                    center.setCurrentLoad(center.getCurrentLoad() + 1);
                    break;
                }
            }
        }
    }

    /**
     * Helper method to find centers compatible with an order
     */
    private List<Center> findCompatibleCenters(Order order, List<Center> availableCenters) {
        return availableCenters.stream()
                .filter(center -> center.getCapacity().contains(order.getSize()))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create a pending order response
     */
    private ProcessedOrderDTO createPendingOrderResponse(Long orderId, String message) {
        ProcessedOrderDTO response = new ProcessedOrderDTO();
        response.setOrderId(orderId);
        response.setDistance(null);
        response.setAssignedLogisticsCenter(null);
        response.setMessage(message);
        response.setStatus(STATUS_PENDING);
        return response;
    }

    /**
     * Helper method to find the nearest center with available capacity
     */
    private CenterDistancePair findNearestCenterWithCapacity(Order order, List<Center> compatibleCenters) {
        Center nearestCenter = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Center center : compatibleCenters) {
            if (center.getCurrentLoad() < center.getMaxCapacity()) {
                double distance = calculateHaversineDistance(
                        order.getCoordinates().getLatitude(),
                        order.getCoordinates().getLongitude(),
                        center.getCoordinates().getLatitude(),
                        center.getCoordinates().getLongitude());

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestCenter = center;
                }
            }
        }

        return nearestCenter != null ? new CenterDistancePair(nearestCenter, shortestDistance) : null;
    }

    /**
     * Helper method to assign an order to a center
     */
    private void assignOrderToCenter(Order order, Center center) {
        center.setCurrentLoad(center.getCurrentLoad() + 1);
        centerRepository.save(center);

        order.setStatus(STATUS_ASSIGNED);
        order.setAssignedCenter(center.getName());
        orderRepository.save(order);
    }

    /**
     * Helper class to store a center and its distance to an order
     */
    private static class CenterDistancePair {
        final Center center;
        final double distance;

        CenterDistancePair(Center center, double distance) {
            this.center = center;
            this.distance = distance;
        }
    }
}