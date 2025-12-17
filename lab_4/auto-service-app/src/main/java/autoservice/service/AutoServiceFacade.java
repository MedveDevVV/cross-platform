package autoservice.service;

import autoservice.dto.CarServiceMastersQuery;
import autoservice.enums.SortCarServiceMasters;
import autoservice.exception.AutoServiceException;
import autoservice.exception.ErrorCodes;
import autoservice.exception.MasterNotAssignedException;
import autoservice.model.CarServiceMaster;
import autoservice.model.RepairOrder;
import autoservice.model.WorkshopPlace;
import autoservice.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoServiceFacade {

    private final CarServiceMasterService masterService;
    private final WorkshopPlaceService workshopPlaceService;
    private final RepairOrderService repairOrderService;

    // Создание заказа с валидацией
    @Transactional
    public RepairOrder createRepairOrder(LocalDate creationDate, LocalDate start, LocalDate end,
                                  String description, CarServiceMaster master, WorkshopPlace place) {
        DateUtils.validateDateRange(start, end);

        RepairOrder order = new RepairOrder(creationDate, start, end, description);
        order.setCarServiceMaster(master);
        order.setWorkshopPlace(place);
        return repairOrderService.addOrder(order);
    }

    public List<CarServiceMaster> findAvailableMastersOnDate(LocalDate date) {
        List<CarServiceMaster> masters = masterService.getAllMasters();
        masters.removeAll(repairOrderService.findOccupiedMastersOnDate(date));
        return masters;
    }

    public List<CarServiceMaster> getCarServiceMasters(CarServiceMastersQuery query) {
        Objects.requireNonNull(query, "carServiceMastersQuery cannot be null");
        List<CarServiceMaster> masters;
        if (query.isOccupied() != null)
            masters = query.isOccupied() ? repairOrderService.findOccupiedMastersOnDate(query.localDate())
                    : findAvailableMastersOnDate(query.localDate());
        else masters = masterService.getAllMasters();

        if (query.sort() != null)
            masters.sort(query.sort().getComparator());
        else masters.sort(Comparator.comparing(CarServiceMaster::getFullName));

        return masters;
    }

    public List<WorkshopPlace> getAvailablePlacesOnDate(LocalDate date) {
        List<WorkshopPlace> availablePlaces = workshopPlaceService.getAllPlaces();
        List<RepairOrder> orders = repairOrderService.findCreatedOrdersByDate(date);

        for (RepairOrder order : orders) {
            availablePlaces.remove(order.getWorkshopPlace());
        }
        return availablePlaces;
    }

    public int countAvailablePlacesOnDate(LocalDate date) {
        AtomicInteger countPlaces = new AtomicInteger(0);
        AtomicInteger countMasters = new AtomicInteger(0);

        Thread thread1 = new Thread(() -> {
            countPlaces.set(getAvailablePlacesOnDate(date).size());
        });

        Thread thread2 = new Thread(() -> {
            countMasters.set(getCarServiceMasters(CarServiceMastersQuery.builder()
                    .localDate(date)
                    .isOccupied(false)
                    .sort(SortCarServiceMasters.NAME)
                    .build()).size());
        });
        thread1.start();
        thread2.start();
        try{
            thread1.join(5000);
            thread2.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AutoServiceException(
                    "Операция была прервана",
                    ErrorCodes.SYS_INTERNAL,
                    e
            );
        }

        return Math.min(countPlaces.get(), countMasters.get());
    }

    public Optional<LocalDate> getFirstAvailableSlot(LocalDate date) {
        LocalDate endDate = date.plusDays(7);
        while (date.isBefore(endDate)) {
            if (countAvailablePlacesOnDate(date) > 0) return Optional.of(date);
            date = date.plusDays(1);
        }
        return Optional.empty();
    }

    public CarServiceMaster getMasterByOrderId(UUID orderId) {
        RepairOrder order = repairOrderService.findOrderByIdOrThrow(orderId);
        if (order.getCarServiceMaster() == null) {
            throw new MasterNotAssignedException(orderId);
        }
        return order.getCarServiceMaster();
    }
}
