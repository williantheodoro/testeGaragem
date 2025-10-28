-- V5: Inserir dados de teste para desenvolvimento

-- Ocupar algumas vagas
UPDATE spots SET occupied = TRUE, occupied_by = 'ABC1234', occupied_at = NOW() WHERE spot_code = 'A1';
UPDATE spots SET occupied = TRUE, occupied_by = 'XYZ5678', occupied_at = NOW() WHERE spot_code = 'B3';
UPDATE spots SET occupied = TRUE, occupied_by = 'DEF9012', occupied_at = NOW() WHERE spot_code = 'C5';

-- Inserir estacionamentos ativos
INSERT INTO parkings (license_plate, spot_id, entry_time, status) VALUES
('ABC1234', (SELECT id FROM spots WHERE spot_code = 'A1'), NOW() - INTERVAL 2 HOUR, 'ACTIVE'),
('XYZ5678', (SELECT id FROM spots WHERE spot_code = 'B3'), NOW() - INTERVAL 1 HOUR, 'ACTIVE'),
('DEF9012', (SELECT id FROM spots WHERE spot_code = 'C5'), NOW() - INTERVAL 30 MINUTE, 'ACTIVE');

-- Inserir estacionamentos finalizados
INSERT INTO parkings (license_plate, spot_id, entry_time, exit_time, total_price, status) VALUES
('GHI3456', (SELECT id FROM spots WHERE spot_code = 'A2'), NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 3 HOUR, 10.00, 'COMPLETED'),
('JKL7890', (SELECT id FROM spots WHERE spot_code = 'B1'), NOW() - INTERVAL 8 HOUR, NOW() - INTERVAL 6 HOUR, 15.00, 'COMPLETED');
