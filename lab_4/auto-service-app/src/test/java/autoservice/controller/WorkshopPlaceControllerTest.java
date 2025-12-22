package autoservice.controller;

import autoservice.dto.WorkshopPlaceDTO;
import autoservice.model.WorkshopPlace;
import autoservice.service.WorkshopPlaceService;
import autoservice.mapper.WorkshopPlaceMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(WorkshopPlaceController.class)
public class WorkshopPlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkshopPlaceService placeService;

    @MockBean
    private WorkshopPlaceMapper placeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID placeId;
    private WorkshopPlace place;
    private WorkshopPlaceDTO placeDTO;

    @BeforeEach
    void setUp() {
        placeId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        place = new WorkshopPlace(placeId, "Гараж 1");
        placeDTO = new WorkshopPlaceDTO(placeId, "Гараж 1");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void getAllPlaces_ShouldReturnListOfPlaces() throws Exception {
        // Arrange
        List<WorkshopPlace> places = Collections.singletonList(place);
        Mockito.when(placeService.getAllPlaces()).thenReturn(places);
        Mockito.when(placeMapper.toDTO(place)).thenReturn(placeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/workshop-places")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(placeId.toString())))
                .andExpect(jsonPath("$[0].name", is("Гараж 1")));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void getPlaceById_WithValidId_ShouldReturnPlace() throws Exception {
        // Arrange
        Mockito.when(placeService.findById(placeId)).thenReturn(place);
        Mockito.when(placeMapper.toDTO(place)).thenReturn(placeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/workshop-places/{id}", placeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(placeId.toString())))
                .andExpect(jsonPath("$.name", is("Гараж 1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlace_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        WorkshopPlaceDTO requestDTO = new WorkshopPlaceDTO(null, "Новый гараж");
        WorkshopPlace savedPlace = new WorkshopPlace(UUID.randomUUID(), "Новый гараж");
        WorkshopPlaceDTO responseDTO = new WorkshopPlaceDTO(savedPlace.getId(), "Новый гараж");

        Mockito.when(placeService.addPlace(any(WorkshopPlace.class))).thenReturn(savedPlace);
        Mockito.when(placeMapper.toDTO(savedPlace)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/workshop-places")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Новый гараж")));
    }
}