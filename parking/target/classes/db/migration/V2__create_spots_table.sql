-- V2: Criar tabela de vagas
CREATE TABLE spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spot_code VARCHAR(10) NOT NULL UNIQUE,
    sector_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    occupied_by VARCHAR(20) NULL,
    occupied_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_spot_sector FOREIGN KEY (sector_id) 
        REFERENCES sectors(id) ON DELETE CASCADE,
    
    INDEX idx_sector_id (sector_id),
    INDEX idx_spot_code (spot_code),
    INDEX idx_occupied (occupied)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
