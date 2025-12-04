package autoservice.service;

import autoservice.dto.CarServiceMastersQuery;
import autoservice.dto.RepairOrderQuery;
import autoservice.enums.OrderStatus;
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

    @Transactional
    public RepairOrder addOrder(RepairOrder order) {
        return repairOrderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(RepairOrder order) {
        order.cancel();
        repairOrderRepository.save(order); // spring data сам решает выполнить insert или update
    }

    @Transactional
    public void closeOrder(RepairOrder order) {
        order.close();
        repairOrderRepository.save(order);
    }

    @Transactional
    public void removeOrder(RepairOrder order) {
        repairOrderRepository.delete(order);
    }

    @Transactional
    public void delayOrder(UUID orderId, Period period) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("order not found"));
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

    public Optional<RepairOrder> getOrderById(UUID id) {
        return repairOrderRepository.findById(id);
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