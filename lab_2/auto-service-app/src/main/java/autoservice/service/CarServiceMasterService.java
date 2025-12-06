package autoservice.service;

import autoservice.model.CarServiceMaster;
import autoservice.repository.CarServiceMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarServiceMasterService {

    private final CarServiceMasterRepository masterRepository;

    @Transactional
    public CarServiceMaster addMaster(CarServiceMaster master) {
        return masterRepository.save(master);
    }

    @Transactional
    public void removeMaster(CarServiceMaster master) {
        masterRepository.delete(master);
    }

    public List<CarServiceMaster> getAllMasters() {
        return masterRepository.findAllByOrderByFullNameAsc();
    }

    public CarServiceMaster findById(UUID id) {
        return masterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Master not found with id: " + id));
    }

    public List<CarServiceMaster> findByFullName(String namePart) {
        return masterRepository.findByFullNameContainingIgnoreCase(namePart);
    }

    @Transactional
    public CarServiceMaster updateMaster(UUID id, CarServiceMaster masterDetails) {
        CarServiceMaster master = findById(id);
        master.setFullName(masterDetails.getFullName());
        master.setDateOfBirth(masterDetails.getDateOfBirth());
        return masterRepository.save(master);
    }

}