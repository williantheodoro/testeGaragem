-- V1: Criar tabela de setores
CREATE TABLE sectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sector_code VARCHAR(10) NOT NULL UNIQUE,
    base_price DECIMAL(10,2) NOT NULL,
    max_capacity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_sector_code (sector_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
