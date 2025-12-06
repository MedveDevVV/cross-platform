package autoservice.repository;

import autoservice.enums.OrderStatus;
import autoservice.model.CarServiceMaster;
import autoservice.model.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, UUID> {

    List<RepairOrder> findByStatus(OrderStatus status);

    // Поиск по мастеру
    List<RepairOrder> findByCarServiceMasterId(UUID masterId);

    // Поиск заказов в диапазоне дат
    List<RepairOrder> findByStartDateBetween(LocalDate start, LocalDate end);

    // Поиск заказов по дате создания
    List<RepairOrder> findByCreationDate(LocalDate creationDate);

    // Заказы по статусу и мастеру
    List<RepairOrder> findByStatusAndCarServiceMaster(OrderStatus status, CarServiceMaster master);

    // Запрос для поиска заказов по периоду
    @Query("SELECT o FROM RepairOrder o WHERE o.startDate >= :start AND o.endDate <= :end")
    List<RepairOrder> findOrdersInDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT o FROM RepairOrder o WHERE o.status = 'CREATED' " +
            "AND o.workshopPlace.id = :placeId AND o.carServiceMaster.id = :masterId " +
            "AND o.startDate > :start " +
            "ORDER BY o.startDate")
    List<RepairOrder> findCreatedOrdersByMasterAndPlaceAfterDate(
            @Param("start") LocalDate start,
            @Param("masterId") UUID masterId,
            @Param("placeId") UUID placeId);

    // Поиск заказов по рабочему месту
    List<RepairOrder> findByWorkshopPlaceId(UUID workshopPlaceId);

    // Сортировка по дате создания
    List<RepairOrder> findAllByOrderByCreationDateAsc();

    @Query("SELECT DISTINCT o.carServiceMaster FROM RepairOrder o " +
            "WHERE o.status = 'CREATED' " +
            "AND :date BETWEEN o.startDate AND o.endDate")
    List<CarServiceMaster> findMastersWithCreatedOrdersOnDate(@Param("date") LocalDate date);

    @Query("SELECT o FROM RepairOrder o WHERE o.status = 'CREATED' "
            + "AND :date BETWEEN o.startDate "
            + "AND o.endDate ORDER BY o.creationDate ASC")
    List<RepairOrder> findCreatedOrdersByDate(@Param("date") LocalDate date);

}

