package com.estapar.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estapar.parking.model.Sector;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevenueService Tests - Mutation Testing")
class RevenueServiceTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private RevenueService revenueService;

    private Sector sectorA;
    private Sector sectorB;
    private Sector sectorC;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);

        sectorA = new Sector();
        sectorA.setId(1L);
        sectorA.setSectorCode("A");
        sectorA.setBasePrice(new BigDecimal("10.00"));
        sectorA.setMaxCapacity(10);

        sectorB = new Sector();
        sectorB.setId(2L);
        sectorB.setSectorCode("B");
        sectorB.setBasePrice(new BigDecimal("15.00"));
        sectorB.setMaxCapacity(5);

        sectorC = new Sector();
        sectorC.setId(3L);
        sectorC.setSectorCode("C");
        sectorC.setBasePrice(new BigDecimal("20.00"));
        sectorC.setMaxCapacity(20);
    }

    // ==================== TESTES DE MUTAÇÃO - VALORES DE RETORNO ====================

    @DisplayName("Mutation Tests - Return Values")
    class ReturnValueMutationTests {

        @Test
        @DisplayName("Deve retornar exatamente o valor calculado - não pode ser alterado")
        void testCalculateRevenue_ExactValue_NotMutated() {
            // Arrange
            BigDecimal expectedRevenue = new BigDecimal("150.00");
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(expectedRevenue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert - Testa mutação de valor de retorno
            assertNotNull(result);
            assertEquals(expectedRevenue, result);
            assertEquals(0, result.compareTo(new BigDecimal("150.00")));
            assertNotEquals(new BigDecimal("149.99"), result);
            assertNotEquals(new BigDecimal("150.01"), result);
        }

        @Test
        @DisplayName("Deve retornar ZERO quando null - não pode retornar null")
        void testCalculateRevenue_NullToZero_NotNull() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(null);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert - Testa mutação null -> valor
            assertNotNull(result, "Result must not be null");
            assertEquals(BigDecimal.ZERO, result);
            assertEquals(0, result.compareTo(BigDecimal.ZERO));
            assertTrue(result.signum() == 0, "Result must be zero");
        }

        @Test
        @DisplayName("Deve retornar valor não-nulo mesmo quando receita é zero")
        void testCalculateRevenue_ZeroIsNotNull() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(BigDecimal.ZERO);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert - Testa mutação de retorno
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result);
            assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
        }
    }

    // ==================== TESTES DE MUTAÇÃO - CONDICIONAIS ====================

    @DisplayName("Mutation Tests - Conditionals")
    class ConditionalMutationTests {

        @Test
        @DisplayName("Deve verificar condição null corretamente - não pode inverter")
        void testCalculateRevenue_NullCheck_CannotInvert() {
            // Arrange - Teste quando é NULL
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(null);

            // Act
            BigDecimal resultWhenNull = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(BigDecimal.ZERO, resultWhenNull);

            // Arrange - Teste quando NÃO é NULL
            BigDecimal nonNullRevenue = new BigDecimal("100.00");
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(nonNullRevenue);

            // Act
            BigDecimal resultWhenNotNull = revenueService.calculateRevenue(testDate, "A");

            // Assert - Garante que a condição não foi invertida
            assertNotEquals(resultWhenNull, resultWhenNotNull);
            assertEquals(nonNullRevenue, resultWhenNotNull);
        }

        @Test
        @DisplayName("Deve aplicar operador ternário corretamente")
        void testCalculateRevenue_TernaryOperator_Correct() {
            // Arrange - Caso TRUE (revenue != null)
            BigDecimal revenue = new BigDecimal("200.00");
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(revenue);

            // Act
            BigDecimal resultTrue = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(revenue, resultTrue);

            // Arrange - Caso FALSE (revenue == null)
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(null);

            // Act
            BigDecimal resultFalse = revenueService.calculateRevenue(testDate, "A");

            // Assert - Garante que ambos os ramos funcionam
            assertEquals(BigDecimal.ZERO, resultFalse);
            assertNotEquals(resultTrue, resultFalse);
        }
    }

    // ==================== TESTES DE MUTAÇÃO - EXCEÇÕES ====================

    @DisplayName("Mutation Tests - Exceptions")
    class ExceptionMutationTests {

        @Test
        @DisplayName("Deve lançar RuntimeException - não pode remover throw")
        void testCalculateRevenue_MustThrowException_CannotRemove() {
            // Arrange
            String invalidSector = "INVALID";
            when(sectorRepository.findBysectorCode(invalidSector))
                .thenReturn(Optional.empty());

            // Act & Assert - Garante que exceção é lançada
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, invalidSector),
                "Exception must be thrown"
            );

            assertNotNull(exception);
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("Sector not found"));
        }

        @Test
        @DisplayName("Deve conter mensagem correta na exceção - não pode alterar")
        void testCalculateRevenue_ExceptionMessage_MustContainSectorName() {
            // Arrange
            String sectorName = "Z";
            when(sectorRepository.findBysectorCode(sectorName))
                .thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, sectorName)
            );

            // Assert - Testa mutação de String
            String message = exception.getMessage();
            assertEquals("Sector not found: Z", message);
            assertTrue(message.contains("Sector not found"));
            assertTrue(message.contains(sectorName));
            assertTrue(message.contains(":"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", "XYZ", "999"})
        @DisplayName("Deve lançar exceção para múltiplos setores inválidos")
        void testCalculateRevenue_ThrowsForInvalidSectors(String invalidSector) {
            // Arrange
            when(sectorRepository.findBysectorCode(invalidSector))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, invalidSector)
            );

            verify(sessionRepository, never()).sumRevenueByDateAndSector(any(), any());
        }
    }

    // ==================== TESTES DE MUTAÇÃO - CHAMADAS DE MÉTODO ====================

    @DisplayName("Mutation Tests - Method Calls")
    class MethodCallMutationTests {

        @Test
        @DisplayName("Deve chamar findBysectorCode exatamente uma vez - não pode remover")
        void testCalculateRevenue_MustCallFindBySectorCode() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(new BigDecimal("100.00"));

            // Act
            revenueService.calculateRevenue(testDate, "A");

            // Assert - Garante que método foi chamado
            verify(sectorRepository, times(1)).findBysectorCode("A");
        }

        @Test
        @DisplayName("Deve chamar sumRevenueByDateAndSector - não pode remover")
        void testCalculateRevenue_MustCallSumRevenue() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(new BigDecimal("100.00"));

            // Act
            revenueService.calculateRevenue(testDate, "A");

            // Assert - Garante que método foi chamado
            verify(sessionRepository, times(1))
                .sumRevenueByDateAndSector(eq(testDate), eq(sectorA));
        }

        @Test
        @DisplayName("Deve chamar métodos na ordem correta")
        void testCalculateRevenue_MethodCallOrder() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(new BigDecimal("100.00"));

            // Act
            revenueService.calculateRevenue(testDate, "A");

            // Assert - Verifica ordem de chamadas
            var inOrder = org.mockito.Mockito.inOrder(sectorRepository, sessionRepository);
            inOrder.verify(sectorRepository).findBysectorCode("A");
            inOrder.verify(sessionRepository).sumRevenueByDateAndSector(testDate, sectorA);
        }

        @Test
        @DisplayName("Não deve chamar sumRevenue quando setor não existe")
        void testCalculateRevenue_NoSumRevenueWhenSectorNotFound() {
            // Arrange
            when(sectorRepository.findBysectorCode("INVALID"))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, "INVALID")
            );

            // Assert - Garante que sumRevenue NÃO foi chamado
            verify(sessionRepository, never()).sumRevenueByDateAndSector(any(), any());
        }
    }

    // ==================== TESTES DE MUTAÇÃO - VALORES NUMÉRICOS ====================

    @DisplayName("Mutation Tests - Numeric Values")
    class NumericValueMutationTests {

        @ParameterizedTest
        @CsvSource({
            "0.01, 0.01",
            "1.00, 1.00",
            "10.00, 10.00",
            "100.00, 100.00",
            "999.99, 999.99",
            "1234.56, 1234.56",
            "9999.99, 9999.99"
        })
        @DisplayName("Deve retornar valores exatos - não pode incrementar/decrementar")
        void testCalculateRevenue_ExactNumericValues(String input, String expected) {
            // Arrange
            BigDecimal revenue = new BigDecimal(input);
            BigDecimal expectedValue = new BigDecimal(expected);
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(revenue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert - Testa mutações numéricas (++, --, +1, -1)
            assertEquals(expectedValue, result);
            assertEquals(0, result.compareTo(expectedValue));
            assertNotEquals(expectedValue.add(BigDecimal.ONE), result);
            assertNotEquals(expectedValue.subtract(BigDecimal.ONE), result);
        }

        @Test
        @DisplayName("Deve preservar precisão decimal - não pode arredondar")
        void testCalculateRevenue_PreserveDecimalPrecision() {
            // Arrange
            BigDecimal preciseValue = new BigDecimal("123.456789");
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(preciseValue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(preciseValue, result);
            assertEquals(preciseValue.scale(), result.scale());
            assertEquals(0, result.compareTo(preciseValue));
        }

        @Test
        @DisplayName("Deve diferenciar zero de valores pequenos")
        void testCalculateRevenue_ZeroVsSmallValues() {
            // Arrange - Zero
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(BigDecimal.ZERO);

            // Act
            BigDecimal zeroResult = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(BigDecimal.ZERO, zeroResult);
            assertTrue(zeroResult.compareTo(BigDecimal.ZERO) == 0);

            // Arrange - Valor pequeno
            BigDecimal smallValue = new BigDecimal("0.01");
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(smallValue);

            // Act
            BigDecimal smallResult = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertNotEquals(zeroResult, smallResult);
            assertTrue(smallResult.compareTo(BigDecimal.ZERO) > 0);
        }
    }

    // ==================== TESTES DE MUTAÇÃO - OPERADORES LÓGICOS ====================

    @DisplayName("Mutation Tests - Logical Operators")
    class LogicalOperatorMutationTests {

        @Test
        @DisplayName("Deve validar Optional.empty() corretamente - não pode inverter")
        void testCalculateRevenue_OptionalEmpty_Validation() {
            // Arrange - Optional.empty()
            when(sectorRepository.findBysectorCode("INVALID"))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, "INVALID")
            );

            // Arrange - Optional.of()
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(new BigDecimal("100.00"));

            // Act - Não deve lançar exceção
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertNotNull(result);
            assertEquals(new BigDecimal("100.00"), result);
        }

        @Test
        @DisplayName("Deve usar orElseThrow corretamente - não pode usar orElse")
        void testCalculateRevenue_OrElseThrow_NotOrElse() {
            // Arrange
            when(sectorRepository.findBysectorCode("INVALID"))
                .thenReturn(Optional.empty());

            // Act & Assert - Deve lançar exceção, não retornar valor padrão
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> revenueService.calculateRevenue(testDate, "INVALID")
            );

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("Sector not found"));
        }
    }

    // ==================== TESTES DE MUTAÇÃO - BOUNDARY VALUES ====================

    @DisplayName("Mutation Tests - Boundary Values")
    class BoundaryValueMutationTests {

    	@Test
        @DisplayName("Deve tratar limite inferior (zero)")
        void testCalculateRevenue_LowerBoundary_Zero() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(BigDecimal.ZERO);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(BigDecimal.ZERO, result);
            assertEquals(0, result.signum(), "Signum of zero must be 0");
            assertFalse(result.signum() < 0, "Zero is not negative");
            assertFalse(result.signum() > 0, "Zero is not positive");
            assertEquals(0, result.compareTo(BigDecimal.ZERO));
            assertTrue(result.equals(BigDecimal.ZERO));
        }
        @Test
        @DisplayName("Deve tratar valores muito grandes")
        void testCalculateRevenue_UpperBoundary_LargeValues() {
            // Arrange
            BigDecimal largeValue = new BigDecimal("999999999.99");
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(largeValue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(largeValue, result);
            assertTrue(result.compareTo(new BigDecimal("1000000")) > 0);
            assertTrue(result.compareTo(new BigDecimal("999999999.98")) > 0);
            assertTrue(result.compareTo(new BigDecimal("999999999.99")) == 0);
        }


        @Test
        @DisplayName("Deve tratar valores mínimos positivos")
        void testCalculateRevenue_MinimumPositive() {
            // Arrange
            BigDecimal minValue = new BigDecimal("0.01");
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(minValue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, "A");

            // Assert
            assertEquals(minValue, result);
            assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
            assertTrue(result.compareTo(new BigDecimal("0.02")) < 0);
        }
    }

    // ==================== TESTES DE MUTAÇÃO - PARÂMETROS ====================

    @DisplayName("Mutation Tests - Parameters")
    class ParameterMutationTests {

        @Test
        @DisplayName("Deve usar parâmetro date corretamente - não pode ignorar")
        void testCalculateRevenue_DateParameter_MustBeUsed() {
            // Arrange
            LocalDate date1 = LocalDate.of(2024, 1, 15);
            LocalDate date2 = LocalDate.of(2024, 1, 16);
            
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sessionRepository.sumRevenueByDateAndSector(date1, sectorA))
                .thenReturn(new BigDecimal("100.00"));
            when(sessionRepository.sumRevenueByDateAndSector(date2, sectorA))
                .thenReturn(new BigDecimal("200.00"));

            // Act
            BigDecimal result1 = revenueService.calculateRevenue(date1, "A");
            BigDecimal result2 = revenueService.calculateRevenue(date2, "A");

            // Assert - Garante que datas diferentes produzem resultados diferentes
            assertNotEquals(result1, result2);
            verify(sessionRepository).sumRevenueByDateAndSector(eq(date1), eq(sectorA));
            verify(sessionRepository).sumRevenueByDateAndSector(eq(date2), eq(sectorA));
        }

        @Test
        @DisplayName("Deve usar parâmetro sectorName corretamente - não pode ignorar")
        void testCalculateRevenue_SectorNameParameter_MustBeUsed() {
            // Arrange
            when(sectorRepository.findBysectorCode("A"))
                .thenReturn(Optional.of(sectorA));
            when(sectorRepository.findBysectorCode("B"))
                .thenReturn(Optional.of(sectorB));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
                .thenReturn(new BigDecimal("100.00"));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorB))
                .thenReturn(new BigDecimal("200.00"));

            // Act
            BigDecimal resultA = revenueService.calculateRevenue(testDate, "A");
            BigDecimal resultB = revenueService.calculateRevenue(testDate, "B");

            // Assert - Garante que setores diferentes produzem resultados diferentes
            assertNotEquals(resultA, resultB);
            verify(sectorRepository).findBysectorCode("A");
            verify(sectorRepository).findBysectorCode("B");
        }

        @ParameterizedTest
        @CsvSource({
            "A, 100.00",
            "B, 200.00",
            "C, 300.00"
        })
        @DisplayName("Deve processar diferentes combinações de parâmetros")
        void testCalculateRevenue_DifferentParameterCombinations(String sectorName, String expectedRevenue) {
            // Arrange
            Sector sector = sectorName.equals("A") ? sectorA : 
                           sectorName.equals("B") ? sectorB : sectorC;
            BigDecimal revenue = new BigDecimal(expectedRevenue);
            
            when(sectorRepository.findBysectorCode(sectorName))
                .thenReturn(Optional.of(sector));
            when(sessionRepository.sumRevenueByDateAndSector(testDate, sector))
                .thenReturn(revenue);

            // Act
            BigDecimal result = revenueService.calculateRevenue(testDate, sectorName);

            // Assert
            assertEquals(revenue, result);
        }
    }

    // ==================== TESTES ADICIONAIS DE COBERTURA ====================

    @Test
    @DisplayName("Deve calcular receita com sucesso - caso básico")
    void testCalculateRevenue_Success_BasicCase() {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("150.00");
        
        when(sectorRepository.findBysectorCode("A"))
            .thenReturn(Optional.of(sectorA));
        when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
            .thenReturn(expectedRevenue);

        // Act
        BigDecimal result = revenueService.calculateRevenue(testDate, "A");

        // Assert
        assertNotNull(result);
        assertEquals(expectedRevenue, result);
        verify(sectorRepository).findBysectorCode("A");
        verify(sessionRepository).sumRevenueByDateAndSector(testDate, sectorA);
    }

    @Test
    @DisplayName("Deve retornar ZERO quando receita é null")
    void testCalculateRevenue_ReturnsZero_WhenRevenueIsNull() {
        // Arrange
        when(sectorRepository.findBysectorCode("A"))
            .thenReturn(Optional.of(sectorA));
        when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
            .thenReturn(null);

        // Act
        BigDecimal result = revenueService.calculateRevenue(testDate, "A");

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("Deve lançar exceção quando setor não existe")
    void testCalculateRevenue_ThrowsException_WhenSectorNotFound() {
        // Arrange
        when(sectorRepository.findBysectorCode("INVALID"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> revenueService.calculateRevenue(testDate, "INVALID")
        );

        assertEquals("Sector not found: INVALID", exception.getMessage());
        verify(sectorRepository).findBysectorCode("INVALID");
        verify(sessionRepository, never()).sumRevenueByDateAndSector(any(), any());
    }

    @Test
    @DisplayName("Deve calcular receita para múltiplos setores")
    void testCalculateRevenue_MultipleSectors() {
        // Arrange
        when(sectorRepository.findBysectorCode("A")).thenReturn(Optional.of(sectorA));
        when(sectorRepository.findBysectorCode("B")).thenReturn(Optional.of(sectorB));
        when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorA))
            .thenReturn(new BigDecimal("100.00"));
        when(sessionRepository.sumRevenueByDateAndSector(testDate, sectorB))
            .thenReturn(new BigDecimal("200.00"));

        // Act
        BigDecimal resultA = revenueService.calculateRevenue(testDate, "A");
        BigDecimal resultB = revenueService.calculateRevenue(testDate, "B");

        // Assert
        assertEquals(new BigDecimal("100.00"), resultA);
        assertEquals(new BigDecimal("200.00"), resultB);
        assertTrue(resultB.compareTo(resultA) > 0);
    }

    @Test
    @DisplayName("Deve calcular receita para diferentes datas")
    void testCalculateRevenue_DifferentDates() {
        // Arrange
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 1, 16);
        
        when(sectorRepository.findBysectorCode("A"))
            .thenReturn(Optional.of(sectorA));
        when(sessionRepository.sumRevenueByDateAndSector(date1, sectorA))
            .thenReturn(new BigDecimal("100.00"));
        when(sessionRepository.sumRevenueByDateAndSector(date2, sectorA))
            .thenReturn(new BigDecimal("150.00"));

        // Act
        BigDecimal result1 = revenueService.calculateRevenue(date1, "A");
        BigDecimal result2 = revenueService.calculateRevenue(date2, "A");

        // Assert
        assertEquals(new BigDecimal("100.00"), result1);
        assertEquals(new BigDecimal("150.00"), result2);
        assertNotEquals(result1, result2);
    }
}
