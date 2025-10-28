-- V3: Criar tabela de estacionamentos
CREATE TABLE parkings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    spot_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP NULL,
    total_price DECIMAL(10,2) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_parking_spot FOREIGN KEY (spot_id) 
        REFERENCES spots(id) ON DELETE RESTRICT,
    
    INDEX idx_license_plate (license_plate),
    INDEX idx_spot_id (spot_id),
    INDEX idx_status (status),
    INDEX idx_entry_time (entry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
