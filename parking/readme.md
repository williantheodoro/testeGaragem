🚗 Sistema de Gerenciamento de Estacionamento

🚀 Início Rápido

Pré-requisitos
Docker Desktop instalado
Porta 3003 disponível

Executar o Projeto
# 1. Clone o repositório
git clone <url-do-repositorio>
cd parking

# 2. Inicie os containers
docker-compose up --build

# 3. Aguarde a mensagem:
# "Started ParkingApplication in X.XXX seconds"

📡 Testar a API
1. Verificar configuração da garagem
curl http://localhost:3003/garage

2. Registrar entrada de veículo
curl -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "event": "entry",
    "license_plate": "ABC1234",
    "spot_id": 1,
    "timestamp": "2025-10-28T10:00:00Z"
  }'
3. Registrar saída de veículo
curl -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "event": "exit",
    "license_plate": "ABC1234",
    "timestamp": "2025-10-28T12:00:00Z"
  }'

🧪 Executar Testes
Testes Unitários
docker-compose exec app mvn test

PITest (Mutation Testing)
# Executar PITest
docker-compose exec app mvn test-compile org.pitest:pitest-maven:mutationCoverage

# Copiar relatório para visualizar
docker cp parking-app:/app/target/pit-reports ./pit-reports

# Abrir relatório no navegador
open pit-reports/*/index.html  # macOS
xdg-open pit-reports/*/index.html  # Linux
start pit-reports/*/index.html  # Windows

🛠️ Comandos Úteis
# Parar containers
docker-compose stop

# Parar e remover containers
docker-compose down

# Ver logs
docker-compose logs -f app

# Acessar banco de dados
docker-compose exec db psql -U postgres -d parking_db

# Reiniciar aplicação
docker-compose restart app

📊 Endpoints
Método	Endpoint	Descrição
GET	    /garage	  Retorna configuração da garagem
POST	  /webhook	Recebe eventos de entrada/saída

🐛 Problemas Comuns
Porta 3003 em uso
# Matar processo na porta
lsof -ti:3003 | xargs kill -9  # Mac/Linux

Containers não iniciam
docker-compose down -v
docker-compose up --build

