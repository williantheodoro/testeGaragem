package com.estapar.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.exception.ParkingFullException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.exception.VehicleNotFoundException;
import com.estapar.parking.model.ParkingSession;
import com.estapar.parking.model.Sector;
import com.estapar.parking.model.Spot;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import com.estapar.parking.repository.SpotRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingService Tests")
class ParkingServiceTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private ParkingService parkingService;

    private Sector sectorA;
    private Sector sectorB;
    private Spot spot1;
    private Spot spot2;
    private Spot spot3;
    private WebhookEventDTO entryEvent;
    private WebhookEventDTO parkedEvent;
    private WebhookEventDTO exitEvent;
    private ParkingSession activeSession;

    @BeforeEach
    void setUp() {
        // Setup Sectors
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

        // Setup Spots
        spot1 = new Spot();
        spot1.setId(1L);
        spot1.setSector(sectorA);
        spot1.setLatitude(-23.550520);
        spot1.setLongitude(-46.633308);
        spot1.setOccupied(false);

        spot2 = new Spot();
        spot2.setId(2L);
        spot2.setSector(sectorA);
        spot2.setLatitude(-23.551000);
        spot2.setLongitude(-46.634000);
        spot2.setOccupied(false);

        spot3 = new Spot();
        spot3.setId(3L);
        spot3.setSector(sectorB);
        spot3.setLatitude(-23.552000);
        spot3.setLongitude(-46.635000);
        spot3.setOccupied(false);

        // Setup Events
        entryEvent = new WebhookEventDTO();
        entryEvent.setEventType("ENTRY");
        entryEvent.setLicensePlate("ABC1234");
        entryEvent.setEntryTime(LocalDateTime.now());

        parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        parkedEvent.setLat(new BigDecimal("-23.550520"));
        parkedEvent.setLng(new BigDecimal("-46.633308"));

        exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(LocalDateTime.now().plusHours(2));

        // Setup Active Session
        activeSession = new ParkingSession();
        activeSession.setId(1L);
        activeSession.setLicensePlate("ABC1234");
        activeSession.setSector(sectorA);
        activeSession.setEntryTime(LocalDateTime.now());
        activeSession.setAppliedPrice(new BigDecimal("10.00"));
    }

    // ==================== TESTES DE handleEntry ====================

    @Test
    @DisplayName("Deve processar entrada com sucesso - setor com baixa ocupação")
    void testHandleEntry_Success_LowOccupancy() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA, sectorB));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(2L); // 20% ocupação
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);

        // Assert
        assertNotNull(result);
        assertEquals("ABC1234", result.getLicensePlate());
        assertEquals(sectorA, result.getSector());
        assertEquals(new BigDecimal("9.00"), result.getAppliedPrice()); // 10% desconto
        assertNull(result.getSpot()); // Spot ainda não atribuído

        verify(sessionRepository).findByLicensePlateAndExitTimeIsNull("ABC1234");
        verify(sectorRepository).findAll();
        // ✅ CORRIGIDO: Aceita múltiplas chamadas
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
        verify(sessionRepository).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("Deve processar entrada com preço normal - ocupação média")
    void testHandleEntry_Success_MediumOccupancy() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(4L); // 40% ocupação
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getAppliedPrice()); // Preço base
        // ✅ CORRIGIDO
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
    }

    @Test
    @DisplayName("Deve processar entrada com preço aumentado - alta ocupação")
    void testHandleEntry_Success_HighOccupancy() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(6L); // 60% ocupação
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("11.00"), result.getAppliedPrice()); // +10%
        // ✅ CORRIGIDO
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
    }

    @Test
    @DisplayName("Deve processar entrada com preço máximo - ocupação crítica")
    void testHandleEntry_Success_CriticalOccupancy() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(8L); // 80% ocupação
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("12.50"), result.getAppliedPrice()); // +25%
        // ✅ CORRIGIDO
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
    }

    @Test
    @DisplayName("Deve selecionar segundo setor quando primeiro está cheio")
    void testHandleEntry_Success_SelectSecondSector() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA, sectorB));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(10L); // Cheio
        when(spotRepository.countBySectorAndOccupied(sectorB, true)).thenReturn(2L); // Disponível
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(sectorB, result.getSector());
        // ✅ CORRIGIDO
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorB, true);
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo já está estacionado")
    void testHandleEntry_VehicleAlreadyParked() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));

        // Act & Assert
        VehicleAlreadyParkedException exception = assertThrows(
            VehicleAlreadyParkedException.class,
            () -> parkingService.handleEntry(entryEvent)
        );

        assertTrue(exception.getMessage().contains("ABC1234"));
        assertTrue(exception.getMessage().contains("already parked"));
        verify(sessionRepository).findByLicensePlateAndExitTimeIsNull("ABC1234");
        verify(sectorRepository, never()).findAll();
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando estacionamento está cheio")
    void testHandleEntry_ParkingFull() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA, sectorB));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(10L);
        when(spotRepository.countBySectorAndOccupied(sectorB, true)).thenReturn(5L);

        // Act & Assert
        ParkingFullException exception = assertThrows(
            ParkingFullException.class,
            () -> parkingService.handleEntry(entryEvent)
        );

        assertEquals("Parking is full", exception.getMessage());
        // ✅ CORRIGIDO
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorA, true);
        verify(spotRepository, atLeast(1)).countBySectorAndOccupied(sectorB, true);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar entrada com lista de setores vazia")
    void testHandleEntry_EmptySectorList() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        ParkingFullException exception = assertThrows(
            ParkingFullException.class,
            () -> parkingService.handleEntry(entryEvent)
        );

        assertEquals("Parking is full", exception.getMessage());
    }

    @Test
    @DisplayName("Deve processar entrada com múltiplos veículos")
    void testHandleEntry_MultipleVehicles() {
        // Arrange
        WebhookEventDTO event1 = createEntryEvent("ABC1234");
        WebhookEventDTO event2 = createEntryEvent("XYZ9876");

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(2L);
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> {
            ParkingSession session = i.getArgument(0);
            session.setId(System.currentTimeMillis());
            return session;
        });

        // Act
        ParkingSession result1 = parkingService.handleEntry(event1);
        ParkingSession result2 = parkingService.handleEntry(event2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("ABC1234", result1.getLicensePlate());
        assertEquals("XYZ9876", result2.getLicensePlate());
        verify(sessionRepository, times(2)).save(any(ParkingSession.class));
    }

    // ==================== TESTES DE handleParked ====================

    @Test
    @DisplayName("Deve processar estacionamento com sucesso - vaga mais próxima")
    void testHandleParked_Success_ClosestSpot() {
        // Arrange
        activeSession.setSpot(null);
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.findBySectorAndOccupied(sectorA, false))
            .thenReturn(Arrays.asList(spot1, spot2));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> i.getArgument(0));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ParkingSession result = parkingService.handleParked(parkedEvent);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSpot());
        assertEquals(spot1.getId(), result.getSpot().getId());
        assertNotNull(result.getParkedTime());

        verify(sessionRepository).findByLicensePlateAndExitTimeIsNull("ABC1234");
        verify(spotRepository).findBySectorAndOccupied(sectorA, false);
        verify(spotRepository).save(argThat(spot -> 
            spot.getOccupied() && 
            "ABC1234".equals(spot.getOccupiedBy()) &&
            spot.getOccupiedAt() != null
        ));
        verify(sessionRepository).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("Deve processar estacionamento sem coordenadas")
    void testHandleParked_Success_NoCoordinates() {
        // Arrange
        parkedEvent.setLat(null);
        parkedEvent.setLng(null);
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.findBySectorAndOccupied(sectorA, false))
            .thenReturn(Arrays.asList(spot1, spot2));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> i.getArgument(0));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ParkingSession result = parkingService.handleParked(parkedEvent);

        // Assert
        assertNotNull(result);
        assertEquals(spot1.getId(), result.getSpot().getId());
        verify(spotRepository).save(any(Spot.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não é encontrada")
    void testHandleParked_SessionNotFound() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());

        // Act & Assert
        VehicleNotFoundException exception = assertThrows(
            VehicleNotFoundException.class,
            () -> parkingService.handleParked(parkedEvent)
        );

        assertTrue(exception.getMessage().contains("ABC1234"));
        assertTrue(exception.getMessage().contains("No active session"));
        verify(sessionRepository).findByLicensePlateAndExitTimeIsNull("ABC1234");
        verify(spotRepository, never()).findBySectorAndOccupied(any(), anyBoolean());
        verify(spotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há vagas disponíveis")
    void testHandleParked_NoAvailableSpots() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.findBySectorAndOccupied(sectorA, false))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        ParkingFullException exception = assertThrows(
            ParkingFullException.class,
            () -> parkingService.handleParked(parkedEvent)
        );

        assertTrue(exception.getMessage().contains("No available spots"));
        assertTrue(exception.getMessage().contains(sectorA.getSectorCode()));
        verify(spotRepository).findBySectorAndOccupied(sectorA, false);
        verify(spotRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    // ==================== TESTES DE handleExit ====================

    @Test
    @DisplayName("Deve processar saída com sucesso - período gratuito")
    void testHandleExit_Success_FreePeriod() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusMinutes(25);
        
        activeSession.setEntryTime(entryTime);
        activeSession.setSpot(spot1);
        spot1.setOccupied(true);
        spot1.setOccupiedBy("ABC1234");
        
        exitEvent.setExitTime(exitTime);

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> i.getArgument(0));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getFinalAmount());
        assertEquals(exitTime, result.getExitTime());

        verify(spotRepository).save(argThat(spot -> 
            !spot.getOccupied() && 
            spot.getOccupiedBy() == null &&
            spot.getOccupiedAt() == null
        ));
        verify(sessionRepository).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("Deve processar saída com sucesso - 1 hora")
    void testHandleExit_Success_OneHour() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusHours(1);
        
        activeSession.setEntryTime(entryTime);
        activeSession.setSpot(spot1);
        activeSession.setAppliedPrice(new BigDecimal("10.00"));
        spot1.setOccupied(true);
        
        exitEvent.setExitTime(exitTime);

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> i.getArgument(0));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getFinalAmount());
    }

    @Test
    @DisplayName("Deve processar saída com sucesso - arredondar horas")
    void testHandleExit_Success_RoundUpHours() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusHours(2).plusMinutes(15);
        
        activeSession.setEntryTime(entryTime);
        activeSession.setSpot(spot1);
        activeSession.setAppliedPrice(new BigDecimal("10.00"));
        spot1.setOccupied(true);
        
        exitEvent.setExitTime(exitTime);

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.of(activeSession));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> i.getArgument(0));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("30.00"), result.getFinalAmount());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não é encontrada na saída")
    void testHandleExit_SessionNotFound() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull("ABC1234"))
            .thenReturn(Optional.empty());

        // Act & Assert
        VehicleNotFoundException exception = assertThrows(
            VehicleNotFoundException.class,
            () -> parkingService.handleExit(exitEvent)
        );

        assertTrue(exception.getMessage().contains("ABC1234"));
        assertTrue(exception.getMessage().contains("No active session"));
        verify(sessionRepository).findByLicensePlateAndExitTimeIsNull("ABC1234");
        verify(spotRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    // ==================== TESTES DE getParkingStatistics ====================

    @Test
    @DisplayName("Deve retornar estatísticas do estacionamento")
    void testGetParkingStatistics_Success() {
        // Arrange
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA, sectorB));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(3L);
        when(spotRepository.countBySectorAndOccupied(sectorA, false)).thenReturn(7L);
        when(spotRepository.countBySectorAndOccupied(sectorB, true)).thenReturn(4L);
        when(spotRepository.countBySectorAndOccupied(sectorB, false)).thenReturn(1L);

        // Act
        String stats = parkingService.getParkingStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.contains("Parking Statistics") || stats.contains("Sector"));
        assertTrue(stats.contains("A"));
        assertTrue(stats.contains("B"));
        verify(sectorRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar estatísticas com estacionamento vazio")
    void testGetParkingStatistics_EmptyParking() {
        // Arrange
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(0L);
        when(spotRepository.countBySectorAndOccupied(sectorA, false)).thenReturn(10L);

        // Act
        String stats = parkingService.getParkingStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.contains("0") || stats.contains("A"));
    }

    @Test
    @DisplayName("Deve retornar estatísticas com estacionamento cheio")
    void testGetParkingStatistics_FullParking() {
        // Arrange
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA));
        when(spotRepository.countBySectorAndOccupied(sectorA, true)).thenReturn(10L);
        when(spotRepository.countBySectorAndOccupied(sectorA, false)).thenReturn(0L);

        // Act
        String stats = parkingService.getParkingStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.contains("10"));
    }

    // ==================== TESTES DE getGarageConfiguration ====================

    @Test
    @DisplayName("Deve retornar configuração da garagem com sucesso")
    void testGetGarageConfiguration_Success() {
        // Arrange
        when(sectorRepository.findAll()).thenReturn(Arrays.asList(sectorA, sectorB));
        when(spotRepository.findAll()).thenReturn(Arrays.asList(spot1, spot2, spot3));

        // Act
        GarageConfigDTO config = parkingService.getGarageConfiguration();

        // Assert
        assertNotNull(config);
        assertNotNull(config.getGarage());
        assertNotNull(config.getSpots());
        assertEquals(2, config.getGarage().size());
        assertEquals(3, config.getSpots().size());

        GarageConfigDTO.SectorConfigDTO sectorConfig = config.getGarage().get(0);
        assertEquals("A", sectorConfig.getSector());
        assertEquals(new BigDecimal("10.00"), sectorConfig.getBasePrice());
        assertEquals(10, sectorConfig.getMax_capacity());

        GarageConfigDTO.SpotConfigDTO spotConfig = config.getSpots().get(0);
        assertEquals(1L, spotConfig.getId());
        assertEquals("A", spotConfig.getSector());
        assertNotNull(spotConfig.getLat());
        assertNotNull(spotConfig.getLng());

        verify(sectorRepository).findAll();
        verify(spotRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar configuração vazia quando não há setores")
    void testGetGarageConfiguration_NoSectors() {
        // Arrange
        when(sectorRepository.findAll()).thenReturn(new ArrayList<>());
        when(spotRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        GarageConfigDTO config = parkingService.getGarageConfiguration();

        // Assert
        assertNotNull(config);
        assertTrue(config.getGarage().isEmpty());
        assertTrue(config.getSpots().isEmpty());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private WebhookEventDTO createEntryEvent(String licensePlate) {
        WebhookEventDTO event = new WebhookEventDTO();
        event.setEventType("ENTRY");
        event.setLicensePlate(licensePlate);
        event.setEntryTime(LocalDateTime.now());
        return event;
    }
}
