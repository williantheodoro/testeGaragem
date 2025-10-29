package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueRequestDTO;
import com.estapar.parking.dto.RevenueResponseDTO;
import com.estapar.parking.service.RevenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevenueController Tests")
class RevenueControllerTest {

    @Mock
    private RevenueService revenueService;

    @InjectMocks
    private RevenueController revenueController;

    private RevenueRequestDTO validRequest;
    private LocalDate testDate;
    private String testSector;
    private BigDecimal expectedRevenue;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        testSector = "A";
        expectedRevenue = new BigDecimal("1500.00");

        validRequest = new RevenueRequestDTO();
        validRequest.setDate(testDate);
        validRequest.setSector(testSector);
    }

    @Test
    @DisplayName("Deve retornar receita com sucesso quando dados são válidos")
    void testGetRevenue_Success() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(expectedRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response, "Response não deve ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status deve ser 200 OK");
        assertNotNull(response.getBody(), "Body não deve ser null");
        assertEquals(expectedRevenue, response.getBody().getAmount(), "Valor da receita deve corresponder");

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita zero quando não há movimentação")
    void testGetRevenue_ZeroRevenue() {
        // Arrange
        BigDecimal zeroRevenue = BigDecimal.ZERO;
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(zeroRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.ZERO, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST quando service lança exceção")
    void testGetRevenue_ServiceThrowsException() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenThrow(new RuntimeException("Erro ao calcular receita"));

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status deve ser 400 BAD_REQUEST");
        assertNull(response.getBody(), "Body deve ser null em caso de erro");

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST quando ocorre IllegalArgumentException")
    void testGetRevenue_IllegalArgumentException() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenThrow(new IllegalArgumentException("Setor inválido"));

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST quando ocorre NullPointerException")
    void testGetRevenue_NullPointerException() {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new NullPointerException("Data ou setor nulo"));

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve calcular receita para setor B")
    void testGetRevenue_SectorB() {
        // Arrange
        validRequest.setSector("B");
        BigDecimal sectorBRevenue = new BigDecimal("2500.00");
        
        when(revenueService.calculateRevenue(testDate, "B"))
            .thenReturn(sectorBRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sectorBRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, "B");
    }

    @Test
    @DisplayName("Deve calcular receita para setor C")
    void testGetRevenue_SectorC() {
        // Arrange
        validRequest.setSector("C");
        BigDecimal sectorCRevenue = new BigDecimal("3500.00");
        
        when(revenueService.calculateRevenue(testDate, "C"))
            .thenReturn(sectorCRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sectorCRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, "C");
    }

    @Test
    @DisplayName("Deve calcular receita para data específica")
    void testGetRevenue_SpecificDate() {
        // Arrange
        LocalDate specificDate = LocalDate.of(2024, 12, 25);
        validRequest.setDate(specificDate);
        BigDecimal christmasRevenue = new BigDecimal("5000.00");
        
        when(revenueService.calculateRevenue(specificDate, testSector))
            .thenReturn(christmasRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(christmasRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(specificDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita com valor alto")
    void testGetRevenue_HighValue() {
        // Arrange
        BigDecimal highRevenue = new BigDecimal("99999.99");
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(highRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(highRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita com valor decimal preciso")
    void testGetRevenue_PreciseDecimal() {
        // Arrange
        BigDecimal preciseRevenue = new BigDecimal("1234.56");
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(preciseRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(preciseRevenue, response.getBody().getAmount());
        assertEquals(0, preciseRevenue.compareTo(response.getBody().getAmount()));

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve chamar service com parâmetros corretos")
    void testGetRevenue_CorrectParameters() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(expectedRevenue);

        // Act
        revenueController.getRevenue(validRequest);

        // Assert
        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
        verify(revenueService, times(1)).calculateRevenue(any(LocalDate.class), anyString());
        verifyNoMoreInteractions(revenueService);
    }

    @Test
    @DisplayName("Deve retornar ResponseEntity com corpo correto")
    void testGetRevenue_ResponseBodyStructure() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(expectedRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAmount());
        assertTrue(response.getBody().getAmount() instanceof BigDecimal);

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita para data no passado")
    void testGetRevenue_PastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2023, 1, 1);
        validRequest.setDate(pastDate);
        BigDecimal pastRevenue = new BigDecimal("800.00");
        
        when(revenueService.calculateRevenue(pastDate, testSector))
            .thenReturn(pastRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pastRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(pastDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita para data futura")
    void testGetRevenue_FutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        validRequest.setDate(futureDate);
        BigDecimal futureRevenue = BigDecimal.ZERO;
        
        when(revenueService.calculateRevenue(futureDate, testSector))
            .thenReturn(futureRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(futureRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(futureDate, testSector);
    }

    @Test
    @DisplayName("Deve lidar com múltiplas chamadas consecutivas")
    void testGetRevenue_MultipleCalls() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(expectedRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response1 = revenueController.getRevenue(validRequest);
        ResponseEntity<RevenueResponseDTO> response2 = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody().getAmount(), response2.getBody().getAmount());

        verify(revenueService, times(2)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST e não propagar exceção")
    void testGetRevenue_ExceptionHandling() {
        // Arrange
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenThrow(new RuntimeException("Erro crítico"));

        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> {
            ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        });

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita com setor em minúscula")
    void testGetRevenue_LowercaseSector() {
        // Arrange
        validRequest.setSector("a");
        BigDecimal revenue = new BigDecimal("1000.00");
        
        when(revenueService.calculateRevenue(testDate, "a"))
            .thenReturn(revenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(revenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, "a");
    }

    @Test
    @DisplayName("Deve retornar receita para data de hoje")
    void testGetRevenue_Today() {
        // Arrange
        LocalDate today = LocalDate.now();
        validRequest.setDate(today);
        BigDecimal todayRevenue = new BigDecimal("2000.00");
        
        when(revenueService.calculateRevenue(today, testSector))
            .thenReturn(todayRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(todayRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(today, testSector);
    }

    @Test
    @DisplayName("Deve retornar receita com valor mínimo positivo")
    void testGetRevenue_MinimumPositiveValue() {
        // Arrange
        BigDecimal minRevenue = new BigDecimal("0.01");
        when(revenueService.calculateRevenue(testDate, testSector))
            .thenReturn(minRevenue);

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(minRevenue, response.getBody().getAmount());

        verify(revenueService, times(1)).calculateRevenue(testDate, testSector);
    }

    @Test
    @DisplayName("Deve garantir que response não é null mesmo em caso de erro")
    void testGetRevenue_ResponseNeverNull() {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new RuntimeException());

        // Act
        ResponseEntity<RevenueResponseDTO> response = revenueController.getRevenue(validRequest);

        // Assert
        assertNotNull(response, "Response nunca deve ser null");
        assertNotNull(response.getStatusCode(), "Status code nunca deve ser null");
    }
}
