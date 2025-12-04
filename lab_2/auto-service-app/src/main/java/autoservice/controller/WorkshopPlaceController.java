package autoservice.controller;

import autoservice.dto.WorkshopPlaceDTO;
import autoservice.mapper.WorkshopPlaceMapper;
import autoservice.model.WorkshopPlace;
import autoservice.service.WorkshopPlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workshop-places")
@RequiredArgsConstructor
public class WorkshopPlaceController {
    private final WorkshopPlaceService placeService;
    private final WorkshopPlaceMapper placeMapper;

    @GetMapping
    public List<WorkshopPlaceDTO> getAllPlaces(){
        return placeService.getAllPlaces().stream()
                .map(placeMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopPlaceDTO> getPlaceById(@PathVariable UUID id) {
        WorkshopPlace place = placeService.findById(id);
        return ResponseEntity.ok(placeMapper.toDTO(place));
    }

    @GetMapping("/search")
    public List<WorkshopPlaceDTO> searchByName(@RequestParam String name) {
        return placeService.findByName(name).stream()
                .map(placeMapper::toDTO)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deletePlace(@PathVariable UUID id) {
        placeService.removePlaceById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<WorkshopPlaceDTO> createPlace(@Valid @RequestBody WorkshopPlaceDTO placeDTO) {
        WorkshopPlace place = new WorkshopPlace(placeDTO.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placeMapper.toDTO(placeService.addPlace(place)));
    }

    @PutMapping("/{id}")
    public WorkshopPlaceDTO updatePlace(@PathVariable UUID id, @Valid @RequestBody WorkshopPlaceDTO placeDTO) {
        WorkshopPlace placeDetails = new WorkshopPlace(placeDTO.name());
        WorkshopPlace updated = placeService.updatePlace(id, placeDetails);
        return placeMapper.toDTO(updated);
    }
}