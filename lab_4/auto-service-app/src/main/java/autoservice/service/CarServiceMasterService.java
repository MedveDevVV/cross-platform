package autoservice.service;

import autoservice.exception.AutoServiceException;
import autoservice.exception.ErrorCodes;
import autoservice.exception.InvalidDateException;
import autoservice.exception.MasterNotFoundException;
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

    /**
     * Сохранить мастера
     *
     * @param master данные для мастера
     * @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public CarServiceMaster addMaster(CarServiceMaster master) {
        try {
            return masterRepository.save(master);
        } catch (RuntimeException e) {
            throw new AutoServiceException("Ошибка при сохранении мастера", ErrorCodes.SYS_DATABASE);
        }
    }

    @Transactional
    public void removeMaster(CarServiceMaster master) {
        masterRepository.delete(master);
    }

    public List<CarServiceMaster> getAllMasters() {
        return masterRepository.findAllByOrderByFullNameAsc();
    }

    /**
     * Находит мастера по ID
     *
     * @param id ID мастера
     * @throws MasterNotFoundException если мастер с таким ID не существует
     */
    public CarServiceMaster findById(UUID id) {
        return masterRepository.findById(id)
                .orElseThrow(() -> new MasterNotFoundException(id));
    }

    public List<CarServiceMaster> findByFullName(String namePart) {
        return masterRepository.findByFullNameContainingIgnoreCase(namePart);
    }

    /**
     * Обновляет данные по мастеру
     *
     * @param id id мастера
     * @param masterDetails новые данные для мастера
     * @throws MasterNotFoundException если мастер с таким id не найден
     * @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public CarServiceMaster updateMaster(UUID id, CarServiceMaster masterDetails) {
        CarServiceMaster master = findById(id);
        master.setFullName(masterDetails.getFullName());
        master.setDateOfBirth(masterDetails.getDateOfBirth());
        try {
            return masterRepository.save(master);
        } catch (RuntimeException e) {
            throw new AutoServiceException("Ошибка при сохранении мастера", ErrorCodes.SYS_DATABASE);
        }
    }

}