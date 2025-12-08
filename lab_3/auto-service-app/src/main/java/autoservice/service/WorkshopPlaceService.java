package autoservice.service;

import autoservice.exception.*;
import autoservice.model.WorkshopPlace;
import autoservice.repository.WorkshopPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkshopPlaceService {
    private final WorkshopPlaceRepository workshopPlaceRepository;

    /**
     * Добавить рабочее место
     *
     * @param place объект рабочее место
     * @return объект рабочее место с назначенным ID
     * @throws DuplicateEntityException если имя рабочего места не уникально
     * @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public WorkshopPlace addPlace(WorkshopPlace place) {
        try {
            return workshopPlaceRepository.save(place);
        } catch (DataIntegrityViolationException e) {
            if(e.getCause().getMessage().matches("(?i).*duplicate.*")) {
                throw new DuplicateEntityException("Рабочее место", "названием", place.getName());
            }
            throw new AutoServiceException("Ошибка при сохранении рабочего места", ErrorCodes.SYS_DATABASE);
        }
    }

    /**
     * Удаление рабочего места
     *
     * @param id id рабочего места
     * @throws WorkshopPlaceNotFoundException если рабочее место с таким id не найдено
     */
    @Transactional
    public void removePlaceById(UUID id) {
        WorkshopPlace place = findById(id);
        workshopPlaceRepository.delete(place);
    }

    public List<WorkshopPlace> getAllPlaces() {
        return workshopPlaceRepository.findAll();
    }

    /**
     * Найти рабочее место по ID
     *
     * @param id id рабочего места
     * @throws WorkshopPlaceNotFoundException если рабочее место с таким ID не найдено
     */
    public WorkshopPlace findById(UUID id) {
        return workshopPlaceRepository.findById(id)
                .orElseThrow(() -> new WorkshopPlaceNotFoundException(id));
    }

    public List<WorkshopPlace> findByName(String name) {
        return workshopPlaceRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Обновляет данные по рабочему месту
     *
     * @param id id рабочего места
     * @param placeDetails  рабочего места
     * @throws WorkshopPlaceNotFoundException если рабочее место с таким id не найдено
     * @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public WorkshopPlace updatePlace(UUID id, WorkshopPlace placeDetails) {
        WorkshopPlace place = findById(id);
        place.setName(placeDetails.getName());
        return addPlace(place);
    }
}