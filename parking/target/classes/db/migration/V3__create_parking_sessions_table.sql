-- V3: Criar tabela de sessões de estacionamento
CREATE TABLE parking_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    spot_id BIGINT,
    sector_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP NULL,
    applied_price DECIMAL(10,2) NULL,
    total_price DECIMAL(10,2) NULL,
    final_amount DECIMAL(10,2) NULL,
    parked_time TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_parking_session_spot FOREIGN KEY (spot_id) 
        REFERENCES spots(id) ON DELETE RESTRICT,
        
    CONSTRAINT fk_parking_session_sector FOREIGN KEY (sector_id) 
        REFERENCES sectors(id) ON DELETE RESTRICT,
        
    INDEX idx_license_plate (license_plate),
    INDEX idx_spot_id (spot_id),
    INDEX idx_status (status),
    INDEX idx_sector_id (sector_id),
    INDEX idx_entry_time (entry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
