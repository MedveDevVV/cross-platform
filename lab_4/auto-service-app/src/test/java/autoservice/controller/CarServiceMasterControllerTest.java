package autoservice.controller;

import autoservice.dto.CarServiceMasterDTO;
import autoservice.exception.MasterNotFoundException;
import autoservice.model.CarServiceMaster;
import autoservice.service.CarServiceMasterService;
import autoservice.mapper.CarServiceMasterMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CarServiceMasterController.class)
public class CarServiceMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarServiceMasterService masterService;

    @MockBean
    private CarServiceMasterMapper masterMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private CarServiceMaster master;
    private CarServiceMasterDTO masterDTO;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        master = new CarServiceMaster(testId, "Иванов Иван Иванович",
                LocalDate.of(1990, 1, 1));

        masterDTO = new CarServiceMasterDTO(testId, "Иванов Иван Иванович",
                LocalDate.of(1990, 1, 1));
    }

    @Test
    @WithMockUser(roles = "MASTER") // Добавляем аутентификацию
    void getAllMasters_ShouldReturnListOfMasters() throws Exception {
        // Arrange
        List<CarServiceMaster> masters = Collections.singletonList(master);

        Mockito.when(masterService.getAllMasters()).thenReturn(masters);
        Mockito.when(masterMapper.toDTO(master)).thenReturn(masterDTO);

        // Act & Assert
        mockMvc.perform(get("/api/masters")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testId.toString())))
                .andExpect(jsonPath("$[0].fullName", is("Иванов Иван Иванович")));
    }

    @Test
    @WithMockUser(roles = "MASTER") // Добавляем аутентификацию
    void getMasterById_WithValidId_ShouldReturnMaster() throws Exception {
        // Arrange
        Mockito.when(masterService.findById(testId)).thenReturn(master);
        Mockito.when(masterMapper.toDTO(master)).thenReturn(masterDTO);

        // Act & Assert
        mockMvc.perform(get("/api/masters/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testId.toString())))
                .andExpect(jsonPath("$.fullName", is("Иванов Иван Иванович")));
    }

    @Test
    @WithMockUser(roles = "MASTER") // Добавляем аутентификацию
    void getMasterById_WithInvalidId_ShouldThrowException() throws Exception {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        Mockito.when(masterService.findById(invalidId))
                .thenThrow(new MasterNotFoundException(invalidId));

        // Act & Assert
        mockMvc.perform(get("/api/masters/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NF_MASTER_001")))
                .andExpect(jsonPath("$.message",
                        is("Мастер с ID: " + invalidId + " не найден в системе")));
    }

    @Test
    @WithMockUser(roles = "MASTER") // Добавляем аутентификацию
    void searchByName_ShouldReturnMatchingMasters() throws Exception {
        // Arrange
        List<CarServiceMaster> masters = Collections.singletonList(master);

        Mockito.when(masterService.findByFullName("Иванов")).thenReturn(masters);
        Mockito.when(masterMapper.toDTO(master)).thenReturn(masterDTO);

        // Act & Assert
        mockMvc.perform(get("/api/masters/search")
                        .param("name", "Иванов")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Иванов Иван Иванович")));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Для создания нужна роль ADMIN
    void createMaster_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        CarServiceMasterDTO requestDTO = new CarServiceMasterDTO(
                null, "Петров Петр Петрович", LocalDate.of(1985, 5, 15));
        CarServiceMaster savedMaster = new CarServiceMaster(
                UUID.randomUUID(), requestDTO.fullName(),requestDTO.dateOfBirth());
        CarServiceMasterDTO responseDTO = new CarServiceMasterDTO(
                savedMaster.getId(), savedMaster.getFullName(),savedMaster.getDateOfBirth());

        Mockito.when(masterService.addMaster(any(CarServiceMaster.class))).thenReturn(savedMaster);
        Mockito.when(masterMapper.toDTO(savedMaster)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/masters")
                        .with(csrf()) // Добавляем CSRF токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is("Петров Петр Петрович")));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Для обновления нужна роль ADMIN
    void updateMaster_WithValidData_ShouldReturnUpdatedMaster() throws Exception {
        // Arrange
        CarServiceMasterDTO updateDTO = new CarServiceMasterDTO(
                null, "Иванов Иван Иванович", LocalDate.of(1990, 1, 1));
        CarServiceMaster updatedMaster = new CarServiceMaster(
                testId, "Петров Иван Иванович", LocalDate.of(2000, 1, 1));
        CarServiceMasterDTO responseDTO = new CarServiceMasterDTO(
                testId, "Петров Иван Иванович", LocalDate.of(2000, 1, 1));

        Mockito.when(masterService.updateMaster(eq(testId), any(CarServiceMaster.class))).thenReturn(updatedMaster);
        Mockito.when(masterMapper.toDTO(updatedMaster)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/masters/{id}", testId)
                        .with(csrf()) // Добавляем CSRF токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Петров Иван Иванович")))
                .andExpect(jsonPath("$.dateOfBirth", is("2000-01-01")));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Для удаления нужна роль ADMIN
    void deleteMaster_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        Mockito.when(masterService.findById(testId)).thenReturn(master);
        Mockito.doNothing().when(masterService).removeMaster(master);

        // Act & Assert
        mockMvc.perform(delete("/api/masters/{id}", testId)
                        .with(csrf()) // Добавляем CSRF токен
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}