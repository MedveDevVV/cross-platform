package autoservice.controller;

import autoservice.dto.CreateRepairOrderRequest;
import autoservice.dto.RepairOrderDTO;
import autoservice.dto.RepairOrderQuery;
import autoservice.dto.SearchRepairOrderRequest;
import autoservice.mapper.RepairOrderMapper;
import autoservice.model.CarServiceMaster;
import autoservice.model.RepairOrder;
import autoservice.model.WorkshopPlace;
import autoservice.service.AutoServiceFacade;
import autoservice.service.CarServiceMasterService;
import autoservice.service.RepairOrderService;
import autoservice.service.WorkshopPlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService orderService;
    private final RepairOrderMapper orderMapper;
    private final CarServiceMasterService masterService;
    private final WorkshopPlaceService placeService;
    private final AutoServiceFacade autoServiceFacade;

    @GetMapping
    public List<RepairOrderDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairOrderDTO> getOrderById(@PathVariable UUID id) {
        RepairOrder order = orderService.findOrderByIdOrThrow(id);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @PostMapping
    public ResponseEntity<RepairOrderDTO> createOrder(@Valid @RequestBody CreateRepairOrderRequest request) {
        CarServiceMaster master = masterService.findById(request.masterId());
        WorkshopPlace place = placeService.findById(request.workshopPlaceId());

        RepairOrder order = autoServiceFacade.createRepairOrder(
                LocalDate.now(),
                request.startDate(),
                request.endDate(),
                request.description(),
                master,
                place
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDTO(order));
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        RepairOrder order = orderService.findOrderByIdOrThrow(id);
        orderService.cancelOrder(order);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Void> closeOrder(@PathVariable UUID id) {
        RepairOrder order = orderService.findOrderByIdOrThrow(id);
        orderService.closeOrder(order);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/delay")
    public ResponseEntity<Void> delayOrder(@PathVariable UUID id, @RequestParam int days) {
        orderService.delayOrder(id, Period.ofDays(days));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        RepairOrder order = orderService.findOrderByIdOrThrow(id);
        orderService.removeOrder(order);
        return ResponseEntity.noContent().build();
    }

    // Поиск с фильтрами
    @GetMapping("/search")
    public ResponseEntity<List<RepairOrderDTO>> searchOrders(@Valid SearchRepairOrderRequest request) {

        CarServiceMaster master = null;
        if (request.masterId() != null) {
            master = masterService.findById(request.masterId());
        }

        RepairOrderQuery query = RepairOrderQuery.builder()
                .status(request.status())
                .carServiceMaster(master)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .sortOrders(request.sortBy())
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.findOrdersByFilter(query).stream().map(orderMapper::toDTO).toList());
    }

    @GetMapping("/{id}/master")
    public ResponseEntity<CarServiceMaster> getMasterByOrderId(@PathVariable UUID id) {
        CarServiceMaster master = autoServiceFacade.getMasterByOrderId(id);
        return ResponseEntity.ok(master);
    }

    @GetMapping("/available-slots/next")
    public ResponseEntity<LocalDate> getNextAvailableSlot(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate) {

        LocalDate searchDate = (fromDate != null) ? fromDate : LocalDate.now();
        Optional<LocalDate> availableSlot = autoServiceFacade.getFirstAvailableSlot(searchDate);

        return availableSlot
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

}
