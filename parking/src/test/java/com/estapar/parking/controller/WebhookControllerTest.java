package com.estapar.parking.controller;

import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.exception.ParkingFullException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.exception.VehicleNotFoundException;
import com.estapar.parking.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes unitários e de mutação para WebhookController
 * 
 * Cobertura de cenários:
 * - Eventos válidos (ENTRY, PARKED, EXIT)
 * - Eventos inválidos
 * - Exceções de negócio (ParkingFull, VehicleAlreadyParked, VehicleNotFound)
 * - Exceções genéricas
 * - Validação de payloads
 */
@WebMvcTest(WebhookController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookController - Testes Unitários e de Mutação")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    private WebhookEventDTO entryEvent;
    private WebhookEventDTO parkedEvent;
    private WebhookEventDTO exitEvent;
    private WebhookEventDTO invalidEvent;

    @BeforeEach
    void setUp() {
        // Setup ENTRY event
        entryEvent = new WebhookEventDTO();
        entryEvent.setEventType("ENTRY");
        entryEvent.setLicensePlate("ABC1234");
        entryEvent.setEntryTime(LocalDateTime.now());

        // Setup PARKED event
        parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        parkedEvent.setLat(new BigDecimal("-23.561684"));
        parkedEvent.setLng(new BigDecimal("-46.655981"));

        // Setup EXIT event
        exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(LocalDateTime.now());

        // Setup INVALID event
        invalidEvent = new WebhookEventDTO();
        invalidEvent.setEventType("INVALID_TYPE");
        invalidEvent.setLicensePlate("ABC1234");
    }

    // ========== TESTES DE SUCESSO ==========

    @Test
    @DisplayName("Deve processar evento ENTRY com sucesso e retornar 200 OK")
    void testHandleEntrySuccess() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve processar evento PARKED com sucesso e retornar 200 OK")
    void testHandleParkedSuccess() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve processar evento EXIT com sucesso e retornar 200 OK")
    void testHandleExitSuccess() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
    }

    // ========== TESTES DE VALIDAÇÃO ==========

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para tipo de evento inválido")
    void testHandleInvalidEventType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verify - nenhum método do service deve ser chamado
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para evento null")
    void testHandleNullEventType() throws Exception {
        // Arrange
        WebhookEventDTO nullEvent = new WebhookEventDTO();
        nullEvent.setEventType(null);
        nullEvent.setLicensePlate("ABC1234");

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para JSON inválido")
    void testHandleInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para payload vazio")
    void testHandleEmptyPayload() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== TESTES DE EXCEÇÕES DE NEGÓCIO ==========

    @Test
    @DisplayName("Deve retornar 409 CONFLICT quando estacionamento está cheio (ENTRY)")
    void testHandleEntryParkingFullException() throws Exception {
        // Arrange
        doThrow(new ParkingFullException("Parking is full"))
                .when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 409 CONFLICT quando veículo já está estacionado (ENTRY)")
    void testHandleEntryVehicleAlreadyParkedException() throws Exception {
        // Arrange
        doThrow(new VehicleAlreadyParkedException("Vehicle already parked"))
                .when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 409 CONFLICT quando estacionamento está cheio (PARKED)")
    void testHandleParkedParkingFullException() throws Exception {
        // Arrange
        doThrow(new ParkingFullException("No available spots"))
                .when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 409 CONFLICT quando veículo já está estacionado (PARKED)")
    void testHandleParkedVehicleAlreadyParkedException() throws Exception {
        // Arrange
        doThrow(new VehicleAlreadyParkedException("Vehicle already has a spot"))
                .when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 404 NOT FOUND quando veículo não é encontrado (EXIT)")
    void testHandleExitVehicleNotFoundException() throws Exception {
        // Arrange
        doThrow(new VehicleNotFoundException("Vehicle not found"))
                .when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 404 NOT FOUND quando veículo não é encontrado (PARKED)")
    void testHandleParkedVehicleNotFoundException() throws Exception {
        // Arrange
        doThrow(new VehicleNotFoundException("No active session found"))
                .when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
    }

    // ========== TESTES DE EXCEÇÕES GENÉRICAS ==========

    @Test
    @DisplayName("Deve retornar 500 INTERNAL SERVER ERROR para exceção genérica (ENTRY)")
    void testHandleEntryGenericException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 500 INTERNAL SERVER ERROR para exceção genérica (PARKED)")
    void testHandleParkedGenericException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database connection failed"))
                .when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 500 INTERNAL SERVER ERROR para exceção genérica (EXIT)")
    void testHandleExitGenericException() throws Exception {
        // Arrange
        doThrow(new NullPointerException("Null pointer exception"))
                .when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }

    // ========== TESTES DE MUTAÇÃO (Edge Cases) ==========

    @Test
    @DisplayName("Deve processar evento ENTRY com eventType em minúsculas")
    void testHandleEntryLowerCase() throws Exception {
        // Arrange
        entryEvent.setEventType("entry");
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Deve falhar pois é case-sensitive

        verify(parkingService, never()).handleEntry(any());
    }

    @Test
    @DisplayName("Deve processar evento PARKED com eventType em minúsculas")
    void testHandleParkedLowerCase() throws Exception {
        // Arrange
        parkedEvent.setEventType("parked");
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleParked(any());
    }

    @Test
    @DisplayName("Deve processar evento EXIT com eventType em minúsculas")
    void testHandleExitLowerCase() throws Exception {
        // Arrange
        exitEvent.setEventType("exit");
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar 400 para eventType com espaços")
    void testHandleEventTypeWithSpaces() throws Exception {
        // Arrange
        entryEvent.setEventType(" ENTRY ");

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleEntry(any());
    }

    @Test
    @DisplayName("Deve retornar 400 para eventType vazio")
    void testHandleEmptyEventType() throws Exception {
        // Arrange
        entryEvent.setEventType("");

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(parkingService, never()).handleEntry(any());
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos ENTRY consecutivos")
    void testHandleMultipleEntryEvents() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));

        // Act & Assert - Primeiro evento
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andExpect(status().isOk());

        // Act & Assert - Segundo evento
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andExpect(status().isOk());

        // Verify
        verify(parkingService, times(2)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar sequência completa: ENTRY -> PARKED -> EXIT")
    void testHandleCompleteSequence() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act & Assert - ENTRY
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andExpect(status().isOk());

        // Act & Assert - PARKED
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andExpect(status().isOk());

        // Act & Assert - EXIT
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andExpect(status().isOk());

        // Verify
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }

    // ========== TESTES DE COBERTURA DE MUTAÇÃO ==========

    @Test
    @DisplayName("Deve garantir que apenas ENTRY chama handleEntry")
    void testOnlyEntryCallsHandleEntry() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act - ENTRY
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entryEvent)))
                .andExpect(status().isOk());

        // Assert - Apenas handleEntry foi chamado
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve garantir que apenas PARKED chama handleParked")
    void testOnlyParkedCallsHandleParked() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act - PARKED
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parkedEvent)))
                .andExpect(status().isOk());

        // Assert - Apenas handleParked foi chamado
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve garantir que apenas EXIT chama handleExit")
    void testOnlyExitCallsHandleExit() throws Exception {
        // Arrange
        doNothing().when(parkingService).handleEntry(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleParked(any(WebhookEventDTO.class));
        doNothing().when(parkingService).handleExit(any(WebhookEventDTO.class));

        // Act - EXIT
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitEvent)))
                .andExpect(status().isOk());

        // Assert - Apenas handleExit foi chamado
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }
}
