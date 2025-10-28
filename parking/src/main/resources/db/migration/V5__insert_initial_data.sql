-- V4: Inserir dados iniciais

-- Inserir setores
INSERT INTO sectors (sector_code, base_price, max_capacity) VALUES
('A', 5.00, 10),
('B', 7.50, 8),
('C', 10.00, 12);

-- Inserir vagas do Setor A (10 vagas)
INSERT INTO spots (spot_code, sector_id, latitude, longitude, occupied) VALUES
('A1', 1, -23.5506, -46.6334, FALSE),
('A2', 1, -23.5507, -46.6335, FALSE),
('A3', 1, -23.5508, -46.6336, FALSE),
('A4', 1, -23.5509, -46.6337, FALSE),
('A5', 1, -23.5510, -46.6338, FALSE),
('A6', 1, -23.5511, -46.6339, FALSE),
('A7', 1, -23.5512, -46.6340, FALSE),
('A8', 1, -23.5513, -46.6341, FALSE),
('A9', 1, -23.5514, -46.6342, FALSE),
('A10', 1, -23.5515, -46.6343, FALSE);

-- Inserir vagas do Setor B (8 vagas)
INSERT INTO spots (spot_code, sector_id, latitude, longitude, occupied) VALUES
('B1', 2, -23.5516, -46.6344, FALSE),
('B2', 2, -23.5517, -46.6345, FALSE),
('B3', 2, -23.5518, -46.6346, FALSE),
('B4', 2, -23.5519, -46.6347, FALSE),
('B5', 2, -23.5520, -46.6348, FALSE),
('B6', 2, -23.5521, -46.6349, FALSE),
('B7', 2, -23.5522, -46.6350, FALSE),
('B8', 2, -23.5523, -46.6351, FALSE);

-- Inserir vagas do Setor C (12 vagas)
INSERT INTO spots (spot_code, sector_id, latitude, longitude, occupied) VALUES
('C1', 3, -23.5526, -46.6354, FALSE),
('C2', 3, -23.5527, -46.6355, FALSE),
('C3', 3, -23.5528, -46.6356, FALSE),
('C4', 3, -23.5529, -46.6357, FALSE),
('C5', 3, -23.5530, -46.6358, FALSE),
('C6', 3, -23.5531, -46.6359, FALSE),
('C7', 3, -23.5532, -46.6360, FALSE),
('C8', 3, -23.5533, -46.6361, FALSE),
('C9', 3, -23.5534, -46.6362, FALSE),
('C10', 3, -23.5535, -46.6363, FALSE),
('C11', 3, -23.5536, -46.6364, FALSE),
('C12', 3, -23.5537, -46.6365, FALSE);
