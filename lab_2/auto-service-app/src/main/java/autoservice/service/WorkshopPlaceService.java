package autoservice.service;

import autoservice.model.WorkshopPlace;
import autoservice.repository.WorkshopPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkshopPlaceService {
    private final WorkshopPlaceRepository workshopPlaceRepository;

    @Transactional
    public WorkshopPlace addPlace(WorkshopPlace place) {
        // Проверяем уникальность имени
        if (workshopPlaceRepository.findByName(place.getName()).isPresent()) {
            throw new RuntimeException("Workshop place with name '" + place.getName() + "' already exists");
        }
        return workshopPlaceRepository.save(place);
    }

    @Transactional
    public void removePlaceById(UUID id) {
        WorkshopPlace place = findById(id);
        workshopPlaceRepository.delete(place);
    }

    public List<WorkshopPlace> getAllPlaces() {
        return workshopPlaceRepository.findAll();
    }

    public WorkshopPlace findById(UUID id) {
        return workshopPlaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workshop place not found with id: " + id));
    }

    public List<WorkshopPlace> findByName(String name) {
        return workshopPlaceRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public WorkshopPlace updatePlace(UUID id, WorkshopPlace placeDetails) {
        WorkshopPlace place = findById(id);
        place.setName(placeDetails.getName());
        return workshopPlaceRepository.save(place);
    }
}