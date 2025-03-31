package com.hackathon.inditex.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignationResponseDTO {
    @JsonProperty("processed-orders")
    private List<ProcessedOrderDTO> processedOrders;
}