package autoservice.controller;

import autoservice.dto.CarServiceMasterDTO;
import autoservice.mapper.CarServiceMasterMapper;
import autoservice.model.CarServiceMaster;
import autoservice.service.CarServiceMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class CarServiceMasterController {

    private final CarServiceMasterService masterService;
    private final CarServiceMasterMapper masterMapper;

    @GetMapping
    public List<CarServiceMasterDTO> getAllMasters() {
        return masterService.getAllMasters().stream()
                .map(masterMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarServiceMasterDTO> getMasterById(@PathVariable UUID id) {
            CarServiceMaster master = masterService.findById(id);
            return ResponseEntity.ok(masterMapper.toDTO(master));
    }

    @GetMapping("/search")
    public List<CarServiceMasterDTO> searchByName(@RequestParam String name) {
        return masterService.findByFullName(name).stream()
                .map(masterMapper::toDTO)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CarServiceMasterDTO> createMaster(@Valid @RequestBody CarServiceMasterDTO masterDTO) {

        CarServiceMaster master = new CarServiceMaster(
                masterDTO.fullName(),
                masterDTO.dateOfBirth()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masterMapper.toDTO(masterService.addMaster(master)));
    }

    @PutMapping("/{id}")
    public CarServiceMasterDTO updateMaster(@PathVariable UUID id, @Valid @RequestBody CarServiceMasterDTO masterDTO) {
        CarServiceMaster masterDetails = new CarServiceMaster(
                masterDTO.fullName(),
                masterDTO.dateOfBirth()
        );
        CarServiceMaster updated = masterService.updateMaster(id, masterDetails);
        return masterMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaster(@PathVariable UUID id) {
        CarServiceMaster master = masterService.findById(id);
        masterService.removeMaster(master);
        return ResponseEntity.noContent().build();
    }
}