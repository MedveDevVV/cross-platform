package autoservice.repository;

import autoservice.model.WorkshopPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopPlaceRepository extends JpaRepository<WorkshopPlace, UUID> {

    // Автоматический поиск по имени
    Optional<WorkshopPlace> findByName(String name);

    // Находит место по точному имени
    List<WorkshopPlace> findByNameContainingIgnoreCase(String name);

    // Проверка существования по имени
    boolean existsByName(String name);
}