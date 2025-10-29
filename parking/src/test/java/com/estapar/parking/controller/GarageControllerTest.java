package com.estapar.parking.controller;

import com.estapar.parking.dto.GarageConfigDTO;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GarageController Tests")
class GarageControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private GarageController garageController;

    private GarageConfigDTO mockGarageConfig;

    @BeforeEach
    void setUp() {
        // Configurar dados mock usando as classes internas estáticas
        List<GarageConfigDTO.SectorConfigDTO> mockSectors = Arrays.asList(
            createSectorConfigDTO("A", new BigDecimal("10.00"), 100),
            createSectorConfigDTO("B", new BigDecimal("12.00"), 80),
            createSectorConfigDTO("C", new BigDecimal("15.00"), 50)
        );

        List<GarageConfigDTO.SpotConfigDTO> mockSpots = Arrays.asList(
            createSpotConfigDTO(1L, "A", -23.561684, -46.655981),
            createSpotConfigDTO(2L, "A", -23.561690, -46.655990),
            createSpotConfigDTO(3L, "B", -23.561700, -46.656000)
        );

        mockGarageConfig = new GarageConfigDTO();
        mockGarageConfig.setGarage(mockSectors);
        mockGarageConfig.setSpots(mockSpots);
    }

    @Test
    @DisplayName("Deve retornar configuração da garagem com sucesso")
    void testGetGarageConfiguration_Success() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response, "Response não deve ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status deve ser 200 OK");
        assertNotNull(response.getBody(), "Body não deve ser null");
        assertEquals(mockGarageConfig, response.getBody(), "Deve retornar a configuração mockada");
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar configuração com setores corretos")
    void testGetGarageConfiguration_WithCorrectSectors() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getGarage(), "Lista de setores não deve ser null");
        assertEquals(3, response.getBody().getGarage().size(), "Deve ter 3 setores");
        
        // Verificar primeiro setor
        GarageConfigDTO.SectorConfigDTO sectorA = response.getBody().getGarage().get(0);
        assertEquals("A", sectorA.getSector());
        assertEquals(new BigDecimal("10.00"), sectorA.getBasePrice());
        assertEquals(100, sectorA.getMax_capacity());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar configuração com vagas corretas")
    void testGetGarageConfiguration_WithCorrectSpots() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getSpots(), "Lista de vagas não deve ser null");
        assertEquals(3, response.getBody().getSpots().size(), "Deve ter 3 vagas");
        
        // Verificar primeira vaga
        GarageConfigDTO.SpotConfigDTO spot1 = response.getBody().getSpots().get(0);
        assertEquals(1L, spot1.getId());
        assertEquals("A", spot1.getSector());
        assertEquals(-23.561684, spot1.getLat());
        assertEquals(-46.655981, spot1.getLng());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar configuração vazia quando não houver dados")
    void testGetGarageConfiguration_EmptyConfiguration() {
        // Arrange
        GarageConfigDTO emptyConfig = new GarageConfigDTO();
        emptyConfig.setGarage(Collections.emptyList());
        emptyConfig.setSpots(Collections.emptyList());
        
        when(parkingService.getGarageConfiguration()).thenReturn(emptyConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getGarage().isEmpty(), "Lista de setores deve estar vazia");
        assertTrue(response.getBody().getSpots().isEmpty(), "Lista de vagas deve estar vazia");
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar status 200 mesmo com configuração nula")
    void testGetGarageConfiguration_NullConfiguration() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(null);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response, "Response não deve ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status deve ser 200 OK");
        assertNull(response.getBody(), "Body deve ser null quando service retorna null");
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve chamar o service apenas uma vez")
    void testGetGarageConfiguration_ServiceCalledOnce() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        garageController.getGarageConfiguration();

        // Assert
        verify(parkingService, times(1)).getGarageConfiguration();
        verifyNoMoreInteractions(parkingService);
    }

    @Test
    @DisplayName("Deve retornar ResponseEntity não nulo")
    void testGetGarageConfiguration_ResponseEntityNotNull() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response, "ResponseEntity não deve ser null");
        assertNotNull(response.getStatusCode(), "Status code não deve ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar o mesmo objeto retornado pelo service")
    void testGetGarageConfiguration_ReturnsSameObjectFromService() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertSame(mockGarageConfig, response.getBody(), 
            "Deve retornar exatamente o mesmo objeto retornado pelo service");
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar configuração com um único setor")
    void testGetGarageConfiguration_WithSingleSector() {
        // Arrange
        List<GarageConfigDTO.SectorConfigDTO> singleSector = Collections.singletonList(
            createSectorConfigDTO("A", new BigDecimal("10.00"), 100)
        );
        
        GarageConfigDTO configWithOneSector = new GarageConfigDTO();
        configWithOneSector.setGarage(singleSector);
        configWithOneSector.setSpots(Collections.emptyList());
        
        when(parkingService.getGarageConfiguration()).thenReturn(configWithOneSector);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getGarage().size(), "Deve ter apenas 1 setor");
        assertEquals("A", response.getBody().getGarage().get(0).getSector());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar configuração com uma única vaga")
    void testGetGarageConfiguration_WithSingleSpot() {
        // Arrange
        List<GarageConfigDTO.SpotConfigDTO> singleSpot = Collections.singletonList(
            createSpotConfigDTO(1L, "A", -23.561684, -46.655981)
        );
        
        GarageConfigDTO configWithOneSpot = new GarageConfigDTO();
        configWithOneSpot.setGarage(Collections.emptyList());
        configWithOneSpot.setSpots(singleSpot);
        
        when(parkingService.getGarageConfiguration()).thenReturn(configWithOneSpot);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getSpots().size(), "Deve ter apenas 1 vaga");
        assertEquals(1L, response.getBody().getSpots().get(0).getId());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar todos os setores com preços diferentes")
    void testGetGarageConfiguration_WithDifferentPrices() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        
        assertEquals(new BigDecimal("10.00"), response.getBody().getGarage().get(0).getBasePrice());
        assertEquals(new BigDecimal("12.00"), response.getBody().getGarage().get(1).getBasePrice());
        assertEquals(new BigDecimal("15.00"), response.getBody().getGarage().get(2).getBasePrice());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar todas as vagas com coordenadas corretas")
    void testGetGarageConfiguration_WithCorrectCoordinates() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        
        // Verificar coordenadas da primeira vaga
        assertEquals(-23.561684, response.getBody().getSpots().get(0).getLat());
        assertEquals(-46.655981, response.getBody().getSpots().get(0).getLng());
        
        // Verificar coordenadas da segunda vaga
        assertEquals(-23.561690, response.getBody().getSpots().get(1).getLat());
        assertEquals(-46.655990, response.getBody().getSpots().get(1).getLng());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar setores com capacidades diferentes")
    void testGetGarageConfiguration_WithDifferentCapacities() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        
        assertEquals(100, response.getBody().getGarage().get(0).getMax_capacity());
        assertEquals(80, response.getBody().getGarage().get(1).getMax_capacity());
        assertEquals(50, response.getBody().getGarage().get(2).getMax_capacity());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    @Test
    @DisplayName("Deve retornar vagas com IDs sequenciais")
    void testGetGarageConfiguration_WithSequentialIds() {
        // Arrange
        when(parkingService.getGarageConfiguration()).thenReturn(mockGarageConfig);

        // Act
        ResponseEntity<GarageConfigDTO> response = garageController.getGarageConfiguration();

        // Assert
        assertNotNull(response.getBody());
        
        assertEquals(1L, response.getBody().getSpots().get(0).getId());
        assertEquals(2L, response.getBody().getSpots().get(1).getId());
        assertEquals(3L, response.getBody().getSpots().get(2).getId());
        
        verify(parkingService, times(1)).getGarageConfiguration();
    }

    // ==================== Métodos Auxiliares ====================

    private GarageConfigDTO.SectorConfigDTO createSectorConfigDTO(String sectorCode, BigDecimal basePrice, int maxCapacity) {
        GarageConfigDTO.SectorConfigDTO sector = new GarageConfigDTO.SectorConfigDTO();
        sector.setSector(sectorCode);
        sector.setBasePrice(basePrice);
        sector.setMax_capacity(maxCapacity);
        return sector;
    }

    private GarageConfigDTO.SpotConfigDTO createSpotConfigDTO(Long id, String sector, double lat, double lng) {
        GarageConfigDTO.SpotConfigDTO spot = new GarageConfigDTO.SpotConfigDTO();
        spot.setId(id);
        spot.setSector(sector);
        spot.setLat(lat);
        spot.setLng(lng);
        return spot;
    }
}
