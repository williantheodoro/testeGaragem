package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueRequestDTO;
import com.estapar.parking.dto.RevenueResponseDTO;
import com.estapar.parking.service.RevenueService;
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
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários e de mutação para RevenueController
 * 
 * Cobertura de cenários:
 * - Cálculo de receita com sucesso (com e sem setor)
 * - Validação de datas
 * - Validação de setores
 * - Exceções genéricas
 * - Edge cases (valores zero, negativos, nulos)
 * - Testes de mutação para garantir robustez
 */
@WebMvcTest(RevenueController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("RevenueController - Testes Unitários e de Mutação")
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RevenueService revenueService;

    private RevenueRequestDTO validRequest;
    private RevenueRequestDTO requestWithSector;
    private RevenueRequestDTO requestWithoutSector;

    @BeforeEach
    void setUp() {
        // Request válido com data e setor
        validRequest = new RevenueRequestDTO();
        validRequest.setDate(LocalDate.of(2024, 1, 15));
        validRequest.setSector("A");

        // Request com setor específico
        requestWithSector = new RevenueRequestDTO();
        requestWithSector.setDate(LocalDate.of(2024, 1, 15));
        requestWithSector.setSector("B");

        // Request sem setor (todos os setores)
        requestWithoutSector = new RevenueRequestDTO();
        requestWithoutSector.setDate(LocalDate.of(2024, 1, 15));
        requestWithoutSector.setSector(null);
    }

    // ========== TESTES DE SUCESSO ==========

    @Test
    @DisplayName("Deve calcular receita com sucesso para data e setor específico")
    void testGetRevenueSuccess() throws Exception {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("1500.00");
        when(revenueService.calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq("A")
        )).thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(1500.00)));

        // Verify
        verify(revenueService, times(1)).calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq("A")
        );
    }

    @Test
    @DisplayName("Deve calcular receita total quando setor é null")
    void testGetRevenueTotalAllSectors() throws Exception {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("5000.00");
        when(revenueService.calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq(null)
        )).thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutSector)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(5000.00)));

        // Verify
        verify(revenueService, times(1)).calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq(null)
        );
    }

    @Test
    @DisplayName("Deve calcular receita zero quando não há transações")
    void testGetRevenueZero() throws Exception {
        // Arrange
        BigDecimal expectedRevenue = BigDecimal.ZERO;
        when(revenueService.calculateRevenue(
            any(LocalDate.class),
            any(String.class)
        )).thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(0)));

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve calcular receita para diferentes setores")
    void testGetRevenueMultipleSectors() throws Exception {
        // Arrange - Setor A
        when(revenueService.calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq("A")
        )).thenReturn(new BigDecimal("1000.00"));

        // Act & Assert - Setor A
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(1000.00)));

        // Arrange - Setor B
        when(revenueService.calculateRevenue(
            eq(LocalDate.of(2024, 1, 15)),
            eq("B")
        )).thenReturn(new BigDecimal("2000.00"));

        // Act & Assert - Setor B
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithSector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(2000.00)));

        // Verify
        verify(revenueService, times(1)).calculateRevenue(any(), eq("A"));
        verify(revenueService, times(1)).calculateRevenue(any(), eq("B"));
    }

    @Test
    @DisplayName("Deve calcular receita para data atual")
    void testGetRevenueForToday() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        RevenueRequestDTO todayRequest = new RevenueRequestDTO();
        todayRequest.setDate(today);
        todayRequest.setSector("A");

        BigDecimal expectedRevenue = new BigDecimal("500.00");
        when(revenueService.calculateRevenue(eq(today), eq("A")))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todayRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(500.00)));

        verify(revenueService, times(1)).calculateRevenue(eq(today), eq("A"));
    }

    @Test
    @DisplayName("Deve calcular receita para data no passado")
    void testGetRevenueForPastDate() throws Exception {
        // Arrange
        LocalDate pastDate = LocalDate.of(2023, 12, 1);
        RevenueRequestDTO pastRequest = new RevenueRequestDTO();
        pastRequest.setDate(pastDate);
        pastRequest.setSector("C");

        BigDecimal expectedRevenue = new BigDecimal("3000.00");
        when(revenueService.calculateRevenue(eq(pastDate), eq("C")))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pastRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(3000.00)));

        verify(revenueService, times(1)).calculateRevenue(eq(pastDate), eq("C"));
    }

    @Test
    @DisplayName("Deve calcular receita com valores decimais precisos")
    void testGetRevenueWithPreciseDecimals() throws Exception {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("1234.56");
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(1234.56)));

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve calcular receita com valores altos")
    void testGetRevenueWithLargeAmount() throws Exception {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("999999.99");
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(999999.99)));

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    // ========== TESTES DE VALIDAÇÃO ==========

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando data é null")
    void testGetRevenueWithNullDate() throws Exception {
        // Arrange
        RevenueRequestDTO invalidRequest = new RevenueRequestDTO();
        invalidRequest.setDate(null);
        invalidRequest.setSector("A");

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, never()).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para JSON inválido")
    void testGetRevenueWithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, never()).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST para payload vazio")
    void testGetRevenueWithEmptyPayload() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, never()).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando setor é string vazia")
    void testGetRevenueWithEmptySector() throws Exception {
        // Arrange
        RevenueRequestDTO emptyRequest = new RevenueRequestDTO();
        emptyRequest.setDate(LocalDate.of(2024, 1, 15));
        emptyRequest.setSector("");

        when(revenueService.calculateRevenue(any(), eq("")))
            .thenThrow(new IllegalArgumentException("Sector cannot be empty"));

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, times(1)).calculateRevenue(any(), eq(""));
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando formato de data é inválido")
    void testGetRevenueWithInvalidDateFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"invalid-date\",\"sector\":\"A\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, never()).calculateRevenue(any(), any());
    }

    // ========== TESTES DE EXCEÇÕES ==========

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando service lança IllegalArgumentException")
    void testGetRevenueIllegalArgumentException() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new IllegalArgumentException("Invalid sector"));

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando service lança NullPointerException")
    void testGetRevenueNullPointerException() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new NullPointerException("Null value encountered"));

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando service lança RuntimeException")
    void testGetRevenueRuntimeException() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST quando service lança ArithmeticException")
    void testGetRevenueArithmeticException() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new ArithmeticException("Division by zero"));

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    // ========== TESTES DE EDGE CASES ==========

    @Test
    @DisplayName("Deve processar setor com caracteres especiais")
    void testGetRevenueWithSpecialCharactersInSector() throws Exception {
        // Arrange
        RevenueRequestDTO specialRequest = new RevenueRequestDTO();
        specialRequest.setDate(LocalDate.of(2024, 1, 15));
        specialRequest.setSector("A-1");

        BigDecimal expectedRevenue = new BigDecimal("750.00");
        when(revenueService.calculateRevenue(any(), eq("A-1")))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(750.00)));

        verify(revenueService, times(1)).calculateRevenue(any(), eq("A-1"));
    }

    @Test
    @DisplayName("Deve processar setor com números")
    void testGetRevenueWithNumericSector() throws Exception {
        // Arrange
        RevenueRequestDTO numericRequest = new RevenueRequestDTO();
        numericRequest.setDate(LocalDate.of(2024, 1, 15));
        numericRequest.setSector("123");

        BigDecimal expectedRevenue = new BigDecimal("850.00");
        when(revenueService.calculateRevenue(any(), eq("123")))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(numericRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(850.00)));

        verify(revenueService, times(1)).calculateRevenue(any(), eq("123"));
    }

    @Test
    @DisplayName("Deve processar setor case-sensitive")
    void testGetRevenueCaseSensitiveSector() throws Exception {
        // Arrange - Setor "a" minúsculo
        RevenueRequestDTO lowerRequest = new RevenueRequestDTO();
        lowerRequest.setDate(LocalDate.of(2024, 1, 15));
        lowerRequest.setSector("a");

        when(revenueService.calculateRevenue(any(), eq("a")))
            .thenReturn(new BigDecimal("100.00"));

        // Act & Assert - "a" minúsculo
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lowerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(100.00)));

        // Arrange - Setor "A" maiúsculo
        when(revenueService.calculateRevenue(any(), eq("A")))
            .thenReturn(new BigDecimal("200.00"));

        // Act & Assert - "A" maiúsculo
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(200.00)));

        // Verify - Chamadas diferentes
        verify(revenueService, times(1)).calculateRevenue(any(), eq("a"));
        verify(revenueService, times(1)).calculateRevenue(any(), eq("A"));
    }

    @Test
    @DisplayName("Deve processar data no limite (01/01/2000)")
    void testGetRevenueWithMinDate() throws Exception {
        // Arrange
        LocalDate minDate = LocalDate.of(2000, 1, 1);
        RevenueRequestDTO minRequest = new RevenueRequestDTO();
        minRequest.setDate(minDate);
        minRequest.setSector("A");

        BigDecimal expectedRevenue = new BigDecimal("0.00");
        when(revenueService.calculateRevenue(eq(minDate), any()))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(0.00)));

        verify(revenueService, times(1)).calculateRevenue(eq(minDate), any());
    }

    @Test
    @DisplayName("Deve processar data no futuro")
    void testGetRevenueWithFutureDate() throws Exception {
        // Arrange
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        RevenueRequestDTO futureRequest = new RevenueRequestDTO();
        futureRequest.setDate(futureDate);
        futureRequest.setSector("A");

        BigDecimal expectedRevenue = BigDecimal.ZERO;
        when(revenueService.calculateRevenue(eq(futureDate), any()))
            .thenReturn(expectedRevenue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(futureRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(0)));

        verify(revenueService, times(1)).calculateRevenue(eq(futureDate), any());
    }

    // ========== TESTES DE MUTAÇÃO ==========

    @Test
    @DisplayName("Deve garantir que service é sempre chamado em caso de sucesso")
    void testServiceAlwaysCalledOnSuccess() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(new BigDecimal("100.00"));

        // Act
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Assert - Service DEVE ser chamado exatamente 1 vez
        verify(revenueService, times(1)).calculateRevenue(any(), any());
        verify(revenueService, atLeastOnce()).calculateRevenue(any(), any());
        verify(revenueService, atMostOnce()).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve garantir que service NÃO é chamado em caso de exceção de parsing")
    void testServiceNotCalledOnParsingException() throws Exception {
        // Act
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid}"))
                .andExpect(status().isBadRequest());

        // Assert - Service NÃO deve ser chamado
        verify(revenueService, never()).calculateRevenue(any(), any());
        verifyNoInteractions(revenueService);
    }

    @Test
    @DisplayName("Deve garantir que ResponseEntity.ok() é retornado apenas em sucesso")
    void testResponseEntityOkOnlyOnSuccess() throws Exception {
        // Arrange - Sucesso
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(new BigDecimal("500.00"));

        // Act & Assert - Deve retornar 200
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Arrange - Exceção
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new RuntimeException("Error"));

        // Act & Assert - Deve retornar 400 (não 200)
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve garantir que ResponseEntity.badRequest() é retornado em exceções")
    void testResponseEntityBadRequestOnException() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenThrow(new IllegalArgumentException("Error"));

        // Act & Assert - Deve retornar 400 (não 200 ou 500)
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
                verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve garantir que parâmetros corretos são passados ao service")
    void testCorrectParametersPassedToService() throws Exception {
        // Arrange
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);
        String expectedSector = "A";
        
        when(revenueService.calculateRevenue(eq(expectedDate), eq(expectedSector)))
            .thenReturn(new BigDecimal("1000.00"));

        // Act
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Assert - Parâmetros EXATOS devem ser passados
        verify(revenueService, times(1)).calculateRevenue(
            eq(expectedDate),
            eq(expectedSector)
        );
        
        // Verify - Não deve chamar com outros parâmetros
        verify(revenueService, never()).calculateRevenue(
            eq(expectedDate),
            eq("B")
        );
        verify(revenueService, never()).calculateRevenue(
            eq(LocalDate.of(2024, 1, 16)),
            eq(expectedSector)
        );
    }

    @Test
    @DisplayName("Deve garantir que response contém o valor retornado pelo service")
    void testResponseContainsServiceReturnValue() throws Exception {
        // Arrange
        BigDecimal serviceReturnValue = new BigDecimal("12345.67");
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(serviceReturnValue);

        // Act & Assert
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(12345.67)));

        verify(revenueService, times(1)).calculateRevenue(any(), any());
    }

    @Test
    @DisplayName("Deve processar múltiplas requisições consecutivas")
    void testMultipleConsecutiveRequests() throws Exception {
        // Arrange
        when(revenueService.calculateRevenue(any(), any()))
            .thenReturn(new BigDecimal("100.00"))
            .thenReturn(new BigDecimal("200.00"))
            .thenReturn(new BigDecimal("300.00"));

        // Act & Assert - Request 1
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(100.00)));

        // Act & Assert - Request 2
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(200.00)));

        // Act & Assert - Request 3
        mockMvc.perform(get("/revenue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(300.00)));

        // Verify - Service foi chamado 3 vezes
        verify(revenueService, times(3)).calculateRevenue(any(), any());
    }
}
