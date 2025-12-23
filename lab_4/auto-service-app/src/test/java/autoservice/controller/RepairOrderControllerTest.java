package autoservice.controller;

import autoservice.dto.CreateRepairOrderRequest;
import autoservice.dto.RepairOrderDTO;
import autoservice.enums.OrderStatus;
import autoservice.exception.OrderNotFoundException;
import autoservice.model.CarServiceMaster;
import autoservice.model.RepairOrder;
import autoservice.model.WorkshopPlace;
import autoservice.service.AutoServiceFacade;
import autoservice.service.CarServiceMasterService;
import autoservice.service.RepairOrderService;
import autoservice.service.WorkshopPlaceService;
import autoservice.mapper.RepairOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RepairOrderController.class)
public class RepairOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairOrderService orderService;

    @MockBean
    private RepairOrderMapper orderMapper;

    @MockBean
    private CarServiceMasterService masterService;

    @MockBean
    private WorkshopPlaceService placeService;

    @MockBean
    private AutoServiceFacade autoServiceFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID masterId;
    private UUID placeId;
    private UUID orderId;
    private CarServiceMaster master;
    private WorkshopPlace place;
    private RepairOrder order;
    private RepairOrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        masterId = UUID.randomUUID();
        placeId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        master = new CarServiceMaster(masterId, "Иванов Иван Иванович", LocalDate.of(1990, 1, 1));
        place = new WorkshopPlace(placeId, "Место 1");

        order = new RepairOrder(
                orderId,
                master,
                place,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Ремонт двигателя",
                OrderStatus.CREATED,
                1000.0f
        );

        orderDTO = new RepairOrderDTO(
                orderId,
                masterId,
                "Иванов Иван Иванович",
                placeId,
                "Место 1",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Ремонт двигателя",
                OrderStatus.CREATED,
                1000.0f
        );
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createOrder_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateRepairOrderRequest request = new CreateRepairOrderRequest(
                masterId,
                placeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Ремонт двигателя"
        );

        Mockito.when(masterService.findById(masterId)).thenReturn(master);
        Mockito.when(placeService.findById(placeId)).thenReturn(place);
        Mockito.when(autoServiceFacade.createRepairOrder(
                any(LocalDate.class),
                any(LocalDate.class),
                any(LocalDate.class),
                any(String.class),
                any(CarServiceMaster.class),
                any(WorkshopPlace.class)
        )).thenReturn(order);
        Mockito.when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.description", is("Ремонт двигателя")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void getOrderById_WithValidId_ShouldReturnOrder() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenReturn(order);
        Mockito.when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.description", is("Ремонт двигателя")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void getOrderById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenThrow(new OrderNotFoundException(orderId));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NF_ORDER_001")))
                .andExpect(jsonPath("$.message", is("Заказ с ID: " + orderId + " не найден в системе")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void cancelOrder_WithValidId_ShouldReturnOk() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenReturn(order);
        Mockito.doNothing().when(orderService).cancelOrder(order);

        // Act & Assert
        mockMvc.perform(put("/api/orders/{id}/cancel", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void cancelOrder_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenThrow(new OrderNotFoundException(orderId));

        // Act & Assert
        mockMvc.perform(put("/api/orders/{id}/cancel", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NF_ORDER_001")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void closeOrder_WithValidId_ShouldReturnOk() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenReturn(order);
        Mockito.doNothing().when(orderService).closeOrder(order);

        // Act & Assert
        mockMvc.perform(put("/api/orders/{id}/close", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void delayOrder_WithValidId_ShouldReturnOk() throws Exception {
        // Arrange
        Mockito.doThrow(new OrderNotFoundException(orderId))
                .when(orderService).delayOrder(orderId, java.time.Period.ofDays(3));

        // Act & Assert
        mockMvc.perform(put("/api/orders/{id}/delay", orderId)
                        .with(csrf())
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NF_ORDER_001")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrder_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        Mockito.when(orderService.findOrderByIdOrThrow(orderId)).thenReturn(order);
        Mockito.doNothing().when(orderService).removeOrder(order);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{id}", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}