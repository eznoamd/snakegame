## SNAKE GAME

Este é um jogo criado com objetivos de aprendizado de UDP na prática e aplicação de gateway para conversão de server para WebSocket (navegadores).

Por ser a primeira versão, esta ainda possuí bugs e necessita revisão lógica.

> [!CAUTION] 
> novo servidor ainda não integrado com frontend

## Docker Setup

### Pré-requisitos
- Docker instalado
- Docker Compose instalado

### Como rodar com Docker

1. **Build e iniciar o container:**
   ```bash
   docker-compose up --build
   ```

2. **Para rodar em background:**
   ```bash
   docker-compose up -d --build
   ```

3. **Parar os containers:**
   ```bash
   docker-compose down
   ```

4. **Verificar logs:**
   ```bash
   docker-compose logs -f
   ```

### Portas expostas
- **3000**: Servidor UDP (clientes diretos)
- **8080**: Gateway WebSocket (clientes web)
- **3001**: Gateway return port

### Arquitetura Docker
O container inclui:
- OpenJDK 8
- Dependências Java (Gson, Java-WebSocket, SLF4J)
- Servidor UDP + Gateway WebSocket
- Configuração de rede otimizada