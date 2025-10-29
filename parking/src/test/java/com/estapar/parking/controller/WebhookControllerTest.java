package com.estapar.parking.controller;

import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.exception.ParkingFullException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.exception.VehicleNotFoundException;
import com.estapar.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookController Tests")
class WebhookControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private WebhookController webhookController;

    private WebhookEventDTO entryEvent;
    private WebhookEventDTO parkedEvent;
    private WebhookEventDTO exitEvent;
    private WebhookEventDTO invalidEvent;

    @BeforeEach
    void setUp() {
        entryEvent = createWebhookEvent("ENTRY", "ABC1234");
        parkedEvent = createWebhookEvent("PARKED", "ABC1234");
        exitEvent = createWebhookEvent("EXIT", "ABC1234");
        invalidEvent = createWebhookEvent("INVALID", "ABC1234");
    }

    // ==================== Testes de Sucesso - ENTRY ====================

    @Test
    @DisplayName("Deve processar evento ENTRY com sucesso")
    void testHandleWebhook_EntrySuccess() {
        // Arrange
        when(parkingService.handleEntry(entryEvent)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleEntry(entryEvent);
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos ENTRY")
    void testHandleWebhook_MultipleEntries() {
        // Arrange
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response1 = webhookController.handleWebhook(entryEvent);
        ResponseEntity<Void> response2 = webhookController.handleWebhook(entryEvent);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        verify(parkingService, times(2)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar evento ENTRY com placa diferente")
    void testHandleWebhook_EntryDifferentPlate() {
        // Arrange
        WebhookEventDTO event = createWebhookEvent("ENTRY", "XYZ9876");
        when(parkingService.handleEntry(event)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(event);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(parkingService, times(1)).handleEntry(event);
    }

    // ==================== Testes de Sucesso - PARKED ====================

    @Test
    @DisplayName("Deve processar evento PARKED com sucesso")
    void testHandleWebhook_ParkedSuccess() {
        // Arrange
        when(parkingService.handleParked(parkedEvent)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleParked(parkedEvent);
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos PARKED")
    void testHandleWebhook_MultipleParked() {
        // Arrange
        when(parkingService.handleParked(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response1 = webhookController.handleWebhook(parkedEvent);
        ResponseEntity<Void> response2 = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        verify(parkingService, times(2)).handleParked(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar evento PARKED com placa diferente")
    void testHandleWebhook_ParkedDifferentPlate() {
        // Arrange
        WebhookEventDTO event = createWebhookEvent("PARKED", "DEF5678");
        when(parkingService.handleParked(event)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(event);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(parkingService, times(1)).handleParked(event);
    }

    // ==================== Testes de Sucesso - EXIT ====================

    @Test
    @DisplayName("Deve processar evento EXIT com sucesso")
    void testHandleWebhook_ExitSuccess() {
        // Arrange
        when(parkingService.handleExit(exitEvent)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(exitEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleExit(exitEvent);
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos EXIT")
    void testHandleWebhook_MultipleExits() {
        // Arrange
        when(parkingService.handleExit(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response1 = webhookController.handleWebhook(exitEvent);
        ResponseEntity<Void> response2 = webhookController.handleWebhook(exitEvent);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        verify(parkingService, times(2)).handleExit(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar evento EXIT com placa diferente")
    void testHandleWebhook_ExitDifferentPlate() {
        // Arrange
        WebhookEventDTO event = createWebhookEvent("EXIT", "GHI1357");
        when(parkingService.handleExit(event)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(event);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(parkingService, times(1)).handleExit(event);
    }

    // ==================== Testes de Evento Inválido ====================

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para evento inválido")
    void testHandleWebhook_InvalidEventType() {
        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(invalidEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para evento null")
    void testHandleWebhook_NullEventType() {
        // Arrange
        WebhookEventDTO nullTypeEvent = createWebhookEvent(null, "ABC1234");

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(nullTypeEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para evento vazio")
    void testHandleWebhook_EmptyEventType() {
        // Arrange
        WebhookEventDTO emptyTypeEvent = createWebhookEvent("", "ABC1234");

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(emptyTypeEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para evento com case diferente")
    void testHandleWebhook_LowercaseEventType() {
        // Arrange
        WebhookEventDTO lowercaseEvent = createWebhookEvent("entry", "ABC1234");

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(lowercaseEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(parkingService, never()).handleEntry(any());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para evento desconhecido")
    void testHandleWebhook_UnknownEventType() {
        // Arrange
        WebhookEventDTO unknownEvent = createWebhookEvent("UNKNOWN", "ABC1234");

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(unknownEvent);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(parkingService, never()).handleEntry(any());
        verify(parkingService, never()).handleParked(any());
        verify(parkingService, never()).handleExit(any());
    }

    // ==================== Testes de Exceções - CONFLICT ====================

    @Test
    @DisplayName("Deve retornar CONFLICT quando estacionamento está cheio - ENTRY")
    void testHandleWebhook_ParkingFullException_Entry() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new ParkingFullException("Estacionamento cheio"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    @Test
    @DisplayName("Deve retornar CONFLICT quando estacionamento está cheio - PARKED")
    void testHandleWebhook_ParkingFullException_Parked() {
        // Arrange
        when(parkingService.handleParked(parkedEvent))
            .thenThrow(new ParkingFullException("Estacionamento cheio"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(parkingService, times(1)).handleParked(parkedEvent);
    }

    @Test
    @DisplayName("Deve retornar CONFLICT quando veículo já está estacionado - ENTRY")
    void testHandleWebhook_VehicleAlreadyParkedException_Entry() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new VehicleAlreadyParkedException("Veículo já estacionado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    @Test
    @DisplayName("Deve retornar CONFLICT quando veículo já está estacionado - PARKED")
    void testHandleWebhook_VehicleAlreadyParkedException_Parked() {
        // Arrange
        when(parkingService.handleParked(parkedEvent))
            .thenThrow(new VehicleAlreadyParkedException("Veículo já estacionado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(parkingService, times(1)).handleParked(parkedEvent);
    }

    @Test
    @DisplayName("Deve retornar CONFLICT quando estacionamento está cheio - EXIT")
    void testHandleWebhook_ParkingFullException_Exit() {
        // Arrange
        when(parkingService.handleExit(exitEvent))
            .thenThrow(new ParkingFullException("Setor cheio"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(exitEvent);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(parkingService, times(1)).handleExit(exitEvent);
    }

    // ==================== Testes de Exceções - NOT_FOUND ====================

    @Test
    @DisplayName("Deve retornar NOT_FOUND quando veículo não é encontrado - EXIT")
    void testHandleWebhook_VehicleNotFoundException_Exit() {
        // Arrange
        when(parkingService.handleExit(exitEvent))
            .thenThrow(new VehicleNotFoundException("Veículo não encontrado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(exitEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleExit(exitEvent);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND quando veículo não é encontrado - PARKED")
    void testHandleWebhook_VehicleNotFoundException_Parked() {
        // Arrange
        when(parkingService.handleParked(parkedEvent))
            .thenThrow(new VehicleNotFoundException("Veículo não encontrado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(parkingService, times(1)).handleParked(parkedEvent);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND quando veículo não é encontrado - ENTRY")
    void testHandleWebhook_VehicleNotFoundException_Entry() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new VehicleNotFoundException("Veículo não encontrado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    // ==================== Testes de Exceções - INTERNAL_SERVER_ERROR ====================

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para exceção genérica - ENTRY")
    void testHandleWebhook_GenericException_Entry() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para exceção genérica - PARKED")
    void testHandleWebhook_GenericException_Parked() {
        // Arrange
        when(parkingService.handleParked(parkedEvent))
            .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(parkedEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(parkingService, times(1)).handleParked(parkedEvent);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para exceção genérica - EXIT")
    void testHandleWebhook_GenericException_Exit() {
        // Arrange
        when(parkingService.handleExit(exitEvent))
            .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(exitEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(parkingService, times(1)).handleExit(exitEvent);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para NullPointerException")
    void testHandleWebhook_NullPointerException() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new NullPointerException("Null pointer"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para IllegalArgumentException")
    void testHandleWebhook_IllegalArgumentException() {
        // Arrange
        when(parkingService.handleEntry(entryEvent))
            .thenThrow(new IllegalArgumentException("Argumento inválido"));

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(parkingService, times(1)).handleEntry(entryEvent);
    }

    // ==================== Testes de Fluxo Completo ====================

    @Test
    @DisplayName("Deve processar fluxo completo: ENTRY -> PARKED -> EXIT")
    void testHandleWebhook_CompleteFlow() {
        // Arrange
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);
        when(parkingService.handleParked(any(WebhookEventDTO.class))).thenReturn(null);
        when(parkingService.handleExit(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> entryResponse = webhookController.handleWebhook(entryEvent);
        ResponseEntity<Void> parkedResponse = webhookController.handleWebhook(parkedEvent);
        ResponseEntity<Void> exitResponse = webhookController.handleWebhook(exitEvent);

        // Assert
        assertEquals(HttpStatus.OK, entryResponse.getStatusCode());
        assertEquals(HttpStatus.OK, parkedResponse.getStatusCode());
        assertEquals(HttpStatus.OK, exitResponse.getStatusCode());

        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar eventos de diferentes veículos")
    void testHandleWebhook_DifferentVehicles() {
        // Arrange
        WebhookEventDTO vehicle1 = createWebhookEvent("ENTRY", "ABC1234");
        WebhookEventDTO vehicle2 = createWebhookEvent("ENTRY", "XYZ9876");
        WebhookEventDTO vehicle3 = createWebhookEvent("ENTRY", "DEF5678");
        
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response1 = webhookController.handleWebhook(vehicle1);
        ResponseEntity<Void> response2 = webhookController.handleWebhook(vehicle2);
        ResponseEntity<Void> response3 = webhookController.handleWebhook(vehicle3);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());

        verify(parkingService, times(3)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar sequência de eventos para mesmo veículo")
    void testHandleWebhook_SameVehicleSequence() {
        // Arrange
        String plate = "ABC1234";
        WebhookEventDTO entry = createWebhookEvent("ENTRY", plate);
        WebhookEventDTO parked = createWebhookEvent("PARKED", plate);
        WebhookEventDTO exit = createWebhookEvent("EXIT", plate);
        
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);
        when(parkingService.handleParked(any(WebhookEventDTO.class))).thenReturn(null);
        when(parkingService.handleExit(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        webhookController.handleWebhook(entry);
        webhookController.handleWebhook(parked);
        webhookController.handleWebhook(exit);

        // Assert
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleParked(any(WebhookEventDTO.class));
        verify(parkingService, times(1)).handleExit(any(WebhookEventDTO.class));
    }

    // ==================== Testes de Validação ====================

    @Test
    @DisplayName("Deve garantir que response nunca é null")
    void testHandleWebhook_ResponseNeverNull() {
        // Arrange
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNotNull(response, "Response nunca deve ser null");
        assertNotNull(response.getStatusCode(), "Status code nunca deve ser null");
    }

    @Test
    @DisplayName("Deve garantir que body é sempre null")
    void testHandleWebhook_BodyAlwaysNull() {
        // Arrange
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);

        // Assert
        assertNull(response.getBody(), "Body deve sempre ser null para ResponseEntity<Void>");
    }

    @Test
    @DisplayName("Deve não propagar exceções")
    void testHandleWebhook_DoesNotThrowExceptions() {
        // Arrange
        when(parkingService.handleEntry(any(WebhookEventDTO.class)))
            .thenThrow(new RuntimeException("Erro crítico"));

        // Act & Assert
        assertDoesNotThrow(() -> {
            ResponseEntity<Void> response = webhookController.handleWebhook(entryEvent);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("Deve processar evento com placa vazia")
    void testHandleWebhook_EmptyPlate() {
        // Arrange
        WebhookEventDTO emptyPlateEvent = createWebhookEvent("ENTRY", "");
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(emptyPlateEvent);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Deve processar evento com placa null")
    void testHandleWebhook_NullPlate() {
        // Arrange
        WebhookEventDTO nullPlateEvent = createWebhookEvent("ENTRY", null);
        when(parkingService.handleEntry(any(WebhookEventDTO.class))).thenReturn(null);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(nullPlateEvent);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(parkingService, times(1)).handleEntry(any(WebhookEventDTO.class));
    }

    // ==================== Método Auxiliar ====================

    private WebhookEventDTO createWebhookEvent(String eventType, String plate) {
        WebhookEventDTO event = new WebhookEventDTO();
        event.setEventType(eventType);
        event.setLicensePlate(plate);
        return event;
    }
}
