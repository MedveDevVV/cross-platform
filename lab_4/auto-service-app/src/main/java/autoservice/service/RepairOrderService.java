package autoservice.service;

import autoservice.dto.RepairOrderQuery;
import autoservice.exception.AutoServiceException;
import autoservice.exception.ErrorCodes;
import autoservice.exception.OrderNotFoundException;
import autoservice.model.CarServiceMaster;
import autoservice.model.RepairOrder;
import autoservice.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;

    /**
     *  @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public RepairOrder addOrder(RepairOrder order) {
        try {
            return repairOrderRepository.save(order);
        } catch (RuntimeException e) {
            throw new AutoServiceException("Ошибка при сохранении заказа", ErrorCodes.SYS_DATABASE);
        }
    }

    /**
     *  @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public void cancelOrder(RepairOrder order) {
        order.cancel();
        addOrder(order);
    }

    /**
     *  @throws AutoServiceException если возникла проблема с сохранением
     */
    @Transactional
    public void closeOrder(RepairOrder order) {
        order.close();
        addOrder(order);
    }

    @Transactional
    public void removeOrder(RepairOrder order) {
        repairOrderRepository.delete(order);
    }

    @Transactional
    public void delayOrder(UUID orderId, Period period) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<RepairOrder> orders = new ArrayList<>();
        orders.add(order);
        orders.addAll(repairOrderRepository.findCreatedOrdersByMasterAndPlaceAfterDate(
                order.getStartDate(), order.getCarServiceMaster().getId(), order.getWorkshopPlace().getId()));
        order.setEndDate(order.getEndDate().plus(period));

        RepairOrder curOrder = order;
        for (int i = 0; i < orders.size() - 1; ++i) {
            RepairOrder nextOrder = orders.get(i + 1);
            if (curOrder.getEndDate().isBefore(nextOrder.getStartDate())) {
                resolveScheduleConflict(curOrder, nextOrder);
                curOrder = nextOrder;
            } else break;
        }
        repairOrderRepository.saveAll(orders);
    }

    public RepairOrder findOrderByIdOrThrow(UUID id) {
        return repairOrderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<RepairOrder> findOrdersByFilter(RepairOrderQuery query) {
        List<RepairOrder> orders = new ArrayList<>();
        if (query.carServiceMaster() != null) {
            if (query.status() != null) orders = repairOrderRepository
                    .findByStatusAndCarServiceMaster(query.status(), query.carServiceMaster());
            else orders = repairOrderRepository.findByCarServiceMasterId(query.carServiceMaster().getId());
        } else {
            if (query.status() != null) orders = repairOrderRepository.findByStatus(query.status());
            else orders = repairOrderRepository.findAll();
        }
        orders = orders.stream()
                .filter(o -> query.startDate() == null
                        || o.getStartDate().isAfter(query.startDate()))
                .filter(o -> query.endDate() == null
                        || o.getEndDate().isBefore(query.endDate()))
                .sorted(query.sortRepairOrders() != null
                        ? query.sortRepairOrders().getComparator()
                        : Comparator.comparing(RepairOrder::getCreationDate))
                .toList();

        return orders;
    }

    private void resolveScheduleConflict(RepairOrder curOrder, RepairOrder nextOrder) {
        Period period = (Period.between(nextOrder.getStartDate(), curOrder.getEndDate())).plus(Period.ofDays(1));
        nextOrder.setStartDate(nextOrder.getStartDate().plus(period));
        nextOrder.setEndDate(nextOrder.getEndDate().plus(period));
    }

    public List<RepairOrder> getAllOrders() {
        return repairOrderRepository.findAllByOrderByCreationDateAsc();
    }

    public List<RepairOrder> findCreatedOrdersByDate(LocalDate date) {
        return repairOrderRepository.findCreatedOrdersByDate(date);
    }

    public List<CarServiceMaster> findOccupiedMastersOnDate(LocalDate date) {
        return repairOrderRepository.findMastersWithCreatedOrdersOnDate(date);
    }

}