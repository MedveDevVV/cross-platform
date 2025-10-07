package autoservice.service;

import autoservice.dto.CarServiceMastersQuery;
import autoservice.dto.OrderQuery;
import autoservice.enums.SortCarServiceMasters;
import autoservice.model.CarServiceMaster;
import autoservice.model.Order;
import autoservice.model.RepairOrder;
import autoservice.model.WorkshopPlace;
import autoservice.repository.MasterRepository;
import autoservice.repository.OrderRepository;
import autoservice.repository.WorkshopPlaceRepository;
import autoservice.repository.impl.RepairOrderRepository;
import autoservice.utils.csv.*;
import autoservice.utils.date.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class AutoServiceAdmin {
    private final MasterRepository masterRepository;
    private final WorkshopPlaceRepository workshopPlaceRepository;
    private final OrderRepository ordersRepository;
    private final OrderRepository deletedOrdersRepository = new RepairOrderRepository();


    public AutoServiceAdmin(MasterRepository masterRepository, WorkshopPlaceRepository workshopPlaceRepository,
                            OrderRepository ordersRepository) {
        this.masterRepository = masterRepository;
        this.workshopPlaceRepository = workshopPlaceRepository;
        this.ordersRepository = ordersRepository;
    }

    private List<Order> ordersFilter(@NotNull OrderRepository orders, OrderQuery orderQuery) {
        return orders.getAllOrders().stream()
                .filter(o -> orderQuery.carServiceMaster() == null
                        || o.getAssignPerson().equals(orderQuery.carServiceMaster()))
                .filter(o -> orderQuery.status() == null
                        || o.getStatus().equals(orderQuery.status()))
                .filter(o -> orderQuery.startDate() == null
                        || o.getStartDate().isAfter(orderQuery.startDate()))
                .filter(o -> orderQuery.endDate() == null
                        || o.getEndDate().isBefore(orderQuery.endDate()))
                .toList();
    }

    public void addMaster(CarServiceMaster master) {
        masterRepository.addMaster(master);
    }

    public void removeMaster(CarServiceMaster master) {
        masterRepository.removeMaster(master);
    }

    public void addWorkshopPlace(WorkshopPlace place) {
        workshopPlaceRepository.addPlace(place);
    }

    public void removePlace(WorkshopPlace place) {
        workshopPlaceRepository.removePlace(place);
    }

    public UUID createRepairOrder(LocalDate creationDate, LocalDate start, LocalDate end,
                                  String description, CarServiceMaster master, WorkshopPlace place) {
        RepairOrder order = new RepairOrder(creationDate, start, end, description);
        order.assignPerson(master);
        order.setWorkshopPlace(place);
        ordersRepository.addOrder(order);
        return order.getId();
    }

    public void cancelOrder(UUID orderId) {
        RepairOrder order = ordersRepository.getOrderById(orderId).orElse(null);
        if (order == null) return;
        ordersRepository.cancelOrder(order);
    }

    public void closedOrder(UUID orderId) {
        RepairOrder order = ordersRepository.getOrderById(orderId).orElse(null);
        if (order == null) return;
        ordersRepository.closeOrder(order);
    }

    public void removeOrder(UUID orderId) {
        RepairOrder order = ordersRepository.getOrderById(orderId).orElse(null);
        if (order == null) return;
        deletedOrdersRepository.addOrder(order);
        ordersRepository.removeOrder(order);
    }

    public void delayOrder(UUID orderId, Period period) {
        RepairOrder order = ordersRepository.getOrderById(orderId).orElse(null);
        if (order == null) return;
        order.setEndDate(order.getEndDate().plus(period));
        ordersRepository.updateOrder(order);
    }

    public Optional<RepairOrder> getOrderById(UUID orderId) {
        return ordersRepository.getOrderById(orderId);
    }

    public List<Order> getOrders(@NotNull OrderQuery orderQuery) {
        List<Order> orders = new ArrayList<>();
        if (orderQuery.isRemoved() == null) {
            orders.addAll(ordersFilter(ordersRepository, orderQuery));
            orders.addAll(ordersFilter(deletedOrdersRepository, orderQuery));
        } else if (orderQuery.isRemoved()) {
            orders.addAll(ordersFilter(deletedOrdersRepository, orderQuery));
        } else {
            orders.addAll(ordersFilter(ordersRepository, orderQuery));
        }
        if (orderQuery.sortOrders() != null) {
            orders.sort(orderQuery.sortOrders().getComparator());
        }
        return orders;
    }

    public List<CarServiceMaster> getCarServiceMasters() {
        List<CarServiceMaster> masters = new ArrayList<>(masterRepository.getAllMasters());
        masters.sort(Comparator.comparing(CarServiceMaster::getFullName));
        return masters;
    }

    public List<CarServiceMaster> getCarServiceMasters(CarServiceMastersQuery carServiceMastersQuery) {
        Objects.requireNonNull(carServiceMastersQuery, "carServiceMastersQuery cannot be null");
        List<CarServiceMaster> masters = new ArrayList<>();
        List<Order> orders = ordersRepository.getAllOrders();
        if (carServiceMastersQuery.isOccupied()) {
            for (Order order : orders) {
                if (DateUtils.isDateInRange(carServiceMastersQuery.localDate(), order.getStartDate(), order.getEndDate())) {
                    masters.add((CarServiceMaster) order.getAssignPerson());
                }
            }
        } else {
            masters.addAll(masterRepository.getAllMasters());
            for (Order order : orders) {
                if (DateUtils.isDateInRange(carServiceMastersQuery.localDate(), order.getStartDate(), order.getEndDate())) {
                    masters.remove((CarServiceMaster) order.getAssignPerson());
                }
            }
        }
        masters.sort(carServiceMastersQuery.sort().getComparator());
        return masters;
    }

    public Optional<CarServiceMaster> getMasterByOrder(UUID orderId) {
        RepairOrder order = ordersRepository.getOrderById(orderId).orElse(null);
        if (order == null) return Optional.empty();
        return Optional.ofNullable((CarServiceMaster) order.getAssignPerson());
    }

    public List<WorkshopPlace> getAvailablePlaces(LocalDate localDate) {
        List<WorkshopPlace> availablePlaces = workshopPlaceRepository.getAllPlaces();
        List<Order> orders = ordersRepository.getAllOrders();
        for (Order order : orders) {
            if (DateUtils.isDateInRange(localDate, order.getStartDate(), order.getEndDate())) {
                availablePlaces.remove(((RepairOrder) order).getWorkshopPlace());
            }
        }
        return availablePlaces;
    }

    public int countAvailablePlaces(LocalDate date) {
        int countPlaces = getAvailablePlaces(date).size();
        int countMasters = (getCarServiceMasters(CarServiceMastersQuery.builder().
                localDate(date)
                .isOccupied(false)
                .build()))
                .size();
        return Math.min(countPlaces, countMasters);
    }

    public Optional<LocalDate> getFirstAvailableSlot(@NotNull LocalDate date) {
        int countAvailable = 0;
        LocalDate endDate = date.plusDays(7);
        while (date.isBefore(endDate)) {
            countAvailable = countAvailablePlaces(date);
            if (countAvailable > 0) return Optional.of(date);
            date = date.plusDays(1);
        }
        return Optional.empty();
    }

    public void importMastersFromCsv(Path filePath) throws IOException {
        List<CarServiceMaster> masters = CsvImporter.importFromCsv(
                filePath, MasterCsvHelper.fieldsToMaster, MasterCsvHelper.MASTER_HEADER);

        for (CarServiceMaster master : masters) {
            Optional<CarServiceMaster> existing = masterRepository.getAllMasters().stream()
                    .filter(m -> m.getId().equals(master.getId()))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setFullName(master.getFullName());
                existing.get().setDateOfBirth(master.getDateOfBirth());
            } else {
                masterRepository.addMaster(master);
            }
        }
    }

    public void exportMastersToCsv(Path filePath) throws IOException {
        CsvExporter.exportToCsv(
                masterRepository.getAllMasters(),
                filePath,
                MasterCsvHelper.masterToFields,
                MasterCsvHelper.MASTER_HEADER);
    }

    public void importPlacesFromCsv(Path filePath) throws IOException {
        List<WorkshopPlace> places = CsvImporter.importFromCsv(
                filePath, PlaceCsvHelper.fieldsToPlace, PlaceCsvHelper.PLACE_HEADER);

        for (WorkshopPlace place : places) {
            WorkshopPlace existing = workshopPlaceRepository.findByName(place.getName());
            if (existing != null && existing.getId().equals(place.getId())) {
                existing.setName(place.getName());
            } else {
                workshopPlaceRepository.addPlace(place);
            }
        }
    }

    public void exportPlacesToCsv(Path filePath) throws IOException {
        CsvExporter.exportToCsv(
                workshopPlaceRepository.getAllPlaces(),
                filePath,
                PlaceCsvHelper.placeToFields,
                PlaceCsvHelper.PLACE_HEADER);
    }

    public void importOrdersFromCsv(Path filePath) throws IOException {
        List<RepairOrder> orders = CsvImporter.importFromCsv(
                filePath, OrderCsvHelper.fieldsToOrder, OrderCsvHelper.ORDER_HEADER);

        for (RepairOrder order : orders) {
            Optional<RepairOrder> existing = ordersRepository.getOrderById(order.getId());

            if (existing.isPresent()) {
                RepairOrder existingOrder = existing.get();
                existingOrder.setStartDate(order.getStartDate());
                existingOrder.setEndDate(order.getEndDate());
                existingOrder.setDescription(order.getDescription());
                existingOrder.setTotalPrice(order.getTotalPrice());

                // Обновление мастера
                if (order.getAssignPerson() != null) {
                    CarServiceMaster master = masterRepository.getAllMasters().stream()
                            .filter(m -> m.getId().equals(order.getAssignPerson().getId()))
                            .findFirst()
                            .orElse(null);
                    existingOrder.assignPerson(master);
                }

                // Обновление рабочего места
                if (order.getWorkshopPlace() != null) {
                    WorkshopPlace place = workshopPlaceRepository.getAllPlaces().stream()
                            .filter(p -> p.getId().equals(order.getWorkshopPlace().getId()))
                            .findFirst()
                            .orElse(null);
                    existingOrder.setWorkshopPlace(place);
                }
            } else {
                // связи для новых заказов
                if (order.getAssignPerson() != null) {
                    CarServiceMaster master = masterRepository.getAllMasters().stream()
                            .filter(m -> m.getId().equals(order.getAssignPerson().getId()))
                            .findFirst()
                            .orElse(null);
                    order.assignPerson(master);
                }

                if (order.getWorkshopPlace() != null) {
                    WorkshopPlace place = workshopPlaceRepository.getAllPlaces().stream()
                            .filter(p -> p.getId().equals(order.getWorkshopPlace().getId()))
                            .findFirst()
                            .orElse(null);
                    order.setWorkshopPlace(place);
                }

                ordersRepository.addOrder(order);
            }
        }
    }

    public void exportOrdersToCsv(Path filePath) throws IOException {
        CsvExporter.exportToCsv(
                ordersRepository.getAllOrders().stream()
                        .map(o -> (RepairOrder) o)
                        .collect(Collectors.toList()),
                filePath,
                OrderCsvHelper.orderToFields,
                OrderCsvHelper.ORDER_HEADER);
    }
}