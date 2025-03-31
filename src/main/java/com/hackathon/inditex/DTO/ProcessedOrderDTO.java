package com.hackathon.inditex.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderDTO {
    private Double distance;
    private Long orderId;
    private String assignedLogisticsCenter;
    private String message;
    private String status;
}