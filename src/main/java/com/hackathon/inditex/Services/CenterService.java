package com.hackathon.inditex.Services;

import com.hackathon.inditex.DTO.CenterDTO;
import com.hackathon.inditex.DTO.CenterResponseDTO;
import com.hackathon.inditex.Entities.Center;
import com.hackathon.inditex.Repositories.CenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class that handles logistics center operations.
 * Provides methods for creating, reading, updating, and deleting logistics
 * centers.
 */
@Service
public class CenterService {

    @Autowired
    private CenterRepository centerRepository;

    /**
     * Creates a new logistics center.
     * Validates if the currentLoad doesn't exceed maxCapacity and if there's no
     * existing center at the same coordinates.
     * 
     * @param centerDTO Data Transfer Object containing center information
     * @return Response with appropriate message
     */
    @Transactional
    public CenterResponseDTO createCenter(CenterDTO centerDTO) {
        // Check if currentLoad exceeds maxCapacity
        if (centerDTO.getCurrentLoad() > centerDTO.getMaxCapacity()) {
            return new CenterResponseDTO("Current load cannot exceed max capacity.");
        }

        // Check if center already exists at these coordinates
        Optional<Center> existingCenter = findCenterAtCoordinates(
                centerDTO.getCoordinates().getLatitude(),
                centerDTO.getCoordinates().getLongitude());

        if (existingCenter.isPresent()) {
            return new CenterResponseDTO("There is already a logistics center in that position.");
        }

        // Create and save the new center
        Center center = mapDtoToEntity(centerDTO);
        centerRepository.save(center);

        return new CenterResponseDTO("Logistics center created successfully.");
    }

    /**
     * Retrieves all logistics centers.
     * 
     * @return List of all centers
     */
    public List<Center> getAllCenters() {
        return centerRepository.findAll();
    }

    /**
     * Updates an existing logistics center.
     * Only updates the fields that are provided in the DTO.
     * 
     * @param id        The ID of the center to update
     * @param centerDTO Data Transfer Object containing fields to update
     * @return Response with appropriate message
     */
    @Transactional
    public CenterResponseDTO updateCenter(Long id, CenterDTO centerDTO) {
        Optional<Center> optionalCenter = centerRepository.findById(id);

        if (optionalCenter.isEmpty()) {
            return new CenterResponseDTO("Center not found.");
        }

        Center center = optionalCenter.get();

        // Update center fields with provided values
        updateCenterFields(center, centerDTO);

        // Check if new coordinates are already used by another center
        if (centerDTO.getCoordinates() != null) {
            CenterResponseDTO coordinateCheckResult = validateCoordinatesForUpdate(id, centerDTO, center);
            if (coordinateCheckResult != null) {
                return coordinateCheckResult;
            }
        }

        // Check if currentLoad exceeds maxCapacity after update
        if (center.getCurrentLoad() > center.getMaxCapacity()) {
            return new CenterResponseDTO("Current load cannot exceed max capacity.");
        }

        // Save updated center
        centerRepository.save(center);

        return new CenterResponseDTO("Logistics center updated successfully.");
    }

    /**
     * Deletes a logistics center by ID.
     * 
     * @param id The ID of the center to delete
     * @return Response with success message
     */
    @Transactional
    public CenterResponseDTO deleteCenter(Long id) {
        // We're not checking if the center exists as the documentation doesn't specify
        // any special handling for this case
        centerRepository.deleteById(id);
        return new CenterResponseDTO("Logistics center deleted successfully.");
    }

    /**
     * Helper method to find a center at specific coordinates.
     */
    private Optional<Center> findCenterAtCoordinates(Double latitude, Double longitude) {
        return centerRepository.findByCoordinatesLatitudeAndCoordinatesLongitude(latitude, longitude);
    }

    /**
     * Helper method to map a DTO to an entity.
     */
    private Center mapDtoToEntity(CenterDTO dto) {
        Center center = new Center();
        center.setName(dto.getName());
        center.setCapacity(dto.getCapacity());
        center.setStatus(dto.getStatus());
        center.setMaxCapacity(dto.getMaxCapacity());
        center.setCurrentLoad(dto.getCurrentLoad());
        center.setCoordinates(dto.getCoordinates());
        return center;
    }

    /**
     * Helper method to update entity fields from DTO.
     */
    private void updateCenterFields(Center center, CenterDTO dto) {
        if (dto.getName() != null) {
            center.setName(dto.getName());
        }
        if (dto.getCapacity() != null) {
            center.setCapacity(dto.getCapacity());
        }
        if (dto.getStatus() != null) {
            center.setStatus(dto.getStatus());
        }
        if (dto.getMaxCapacity() != null) {
            center.setMaxCapacity(dto.getMaxCapacity());
        }
        if (dto.getCurrentLoad() != null) {
            center.setCurrentLoad(dto.getCurrentLoad());
        }
    }

    /**
     * Helper method to validate coordinates during update.
     * 
     * @return Response DTO if validation fails, null if validation passes
     */
    private CenterResponseDTO validateCoordinatesForUpdate(Long centerId, CenterDTO dto, Center center) {
        Optional<Center> existingCenter = findCenterAtCoordinates(
                dto.getCoordinates().getLatitude(),
                dto.getCoordinates().getLongitude());

        if (existingCenter.isPresent() && !existingCenter.get().getId().equals(centerId)) {
            return new CenterResponseDTO("There is already a logistics center in that position.");
        }

        center.setCoordinates(dto.getCoordinates());
        return null;
    }
}