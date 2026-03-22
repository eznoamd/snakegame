# Snake Game Server (Java)

## Arquitetura do Sistema

O sistema utiliza uma arquitetura híbrida que suporta dois tipos de clientes:
- **Clientes UDP diretos** (desktop/aplicações nativas)
- **Clientes WebSocket** (navegadores web)

O servidor Java roda exclusivamente com UDP, enquanto um **Gateway WebSocket** faz a ponte entre clientes web e o servidor UDP.

## Classes da API v2

### Pacote `config`
- **`GameConfig`**: constantes globais do jogo (tamanho do mapa, taxa de tick, visão, etc.).
- **`ServerConfig`**: configurações de rede (portas UDP, gateway, host).

### Pacote `game`
- **`GameEngine`**: lógica principal do jogo (processamento de inputs, movimentação, colisões).
- **`GameState`**: estado global do jogo (mapa, jogadores, tick, frutas).
- **`TickLoop`**: agendador que dispara o tick a cada 1/TICK_RATE segundos.

### Pacote `server`
- **`UdpServer`**: servidor UDP principal; recebe mensagens e envia estado visível para jogadores.
- **`UdpWebSocketGateway`**: gateway WebSocket para permitir conexões de navegadores web.
- **`ClientConnection`**: representação unificada de conexões (UDP direto ou WebSocket).

### Pacote `service`
- **`PlayerService`**: gerenciamento de jogadores (add/remove, spawn, inputs, movimentação).
- **`CollisionService`**: validação de colisões (paredes, frutas, outros jogadores).
- **`VisibilityService`**: filtro de visibilidade baseado em VIEW_RADIUS.
- **`FruitService`**: geração de frutas conforme FRUIT_RATIO.

### Pacote `model`
- **`Player`**: representação de jogador (corpo, direção, inputs, alive, etc.).
- **`Position`**: coordenadas `(x, y)` no grid.
- **`Direction`**: enum de direções (`UP/DOWN/LEFT/RIGHT`).
- **`Input`**: direção enviada pelo jogador.

### Pacote `util`
- **`MessageUtils`**: utilitários para criação e processamento de mensagens JSON.
- **`Utils`**: utilitários gerais (direção inicial, posição futura, saneamento, random).

---

## Como rodar (via terminal)

> Pré-requisitos:
> - JDK 8+ instalado
> - Biblioteca Gson para processamento JSON
> - Java-WebSocket para o gateway

### 1. Compilar o servidor Java

```powershell
cd c:\Users\Dell\Desktop\snakegame\server
javac -d out src\**\*.java
```

### 2. Iniciar o sistema completo

```powershell
java -cp out Main
```

O sistema iniciará dois serviços:
- **Servidor UDP** na porta **3000** (comunicação com clientes)
- **Gateway WebSocket** na porta **8080** (clientes web)

### 3. Portas utilizadas

- **3000**: Servidor UDP principal
- **3001**: Porta de retorno do Gateway (respostas do servidor)
- **8080**: Gateway WebSocket (clientes web)

### Dependências Maven (se necessário)

```xml
<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.9</version>
    </dependency>
    <dependency>
        <groupId>org.java-websocket</groupId>
        <artifactId>Java-WebSocket</artifactId>
        <version>1.5.3</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.36</version>
    </dependency>
</dependencies>
```

### Download manual das dependências

**Download manual:**
Se não estiver usando Maven, baixe os JARs necessários:
- [Gson](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar)
- [Java-WebSocket](https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.5.3/Java-WebSocket-1.5.3.jar)
- [SLF4J API](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar)
- [SLF4J Simple](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar)

Coloque os JARs na pasta `lib/` e compile com:
```powershell
javac -cp "lib/*;." -d out src\**\*.java
java -cp "lib/*;out" Main
```

---

## Protocolo

### Clientes UDP Direto

Enviam mensagens JSON diretamente para o servidor UDP na porta 3000:

#### Mensagens enviadas pelo cliente

- **Join**
  ```json
  { "type": "join", "name": "SeuNome" }
  ```

- **Input (direção)**
  ```json
  { "type": "input", "dir": "UP" }
  ```

- **Leave**
  ```json
  { "type": "leave" }
  ```

### Clientes WebSocket

Conectam-se ao gateway na porta 8080. O gateway adiciona automaticamente o ID do jogador:

#### Mensagens enviadas pelo cliente (sem ID)
  ```json
  { "type": "join", "name": "SeuNome" }
  ```

#### Mensagens reencaminhadas pelo servidor (com ID)
  ```json
  { "type": "join", "name": "SeuNome", "id": "uuid-gerado" }
  ```

### Estado retornado pelo servidor

O servidor envia um JSON com:
- `tick`: número do tick atual
- `fruits`: lista de frutas visíveis
- `players`: lista de jogadores visíveis (corpos de todos)
- `self`: informações do próprio jogador (apenas para clientes WebSocket)
- `selfId`: ID do jogador (apenas para clientes WebSocket)

Exemplo:
```json
{
  "tick": 123,
  "fruits": [{"x": 14, "y": 9}],
  "players": [
    {
      "id": "uuid-1",
      "name": "Jogador1",
      "body": [{"x": 5, "y": 5}, {"x": 4, "y": 5}],
      "alive": true
    }
  ],
  "self": {
    "id": "uuid-2",
    "name": "MeuNome",
    "body": [{"x": 10, "y": 10}],
    "alive": true
  },
  "selfId": "uuid-2"
}
```

---

## Configurações do Jogo

### GameConfig
- **GRID_SIZE**: 50 (tamanho do mapa 50x50)
- **TICK_RATE**: 10 (10 atualizações por segundo)
- **VIEW_RADIUS**: 12 (raio de visibilidade do jogador)
- **FRUIT_RATIO**: 0.6 (60% do mapa preenchido com frutas)
- **NAME_LIMIT**: 20 (limite de caracteres no nome)

### ServerConfig
- **SERVER_PORT**: 3000 (porta do servidor UDP)
- **GATEWAY_PORT**: 8080 (porta do gateway WebSocket)
- **GATEWAY_RETURN_PORT**: 3001 (porta de retorno do gateway)
- **UDP_HOST**: "127.0.0.1" (endereço do servidor)

---

## Fluxo de Comunicação

### Cliente WebSocket → Servidor
1. Cliente envia mensagem para Gateway WebSocket (porta 8080)
2. Gateway adiciona ID do jogador
3. Gateway reencaminha para Servidor UDP (porta 3000)
4. Servidor processa e envia resposta para porta 3001
5. Gateway recebe resposta e envia apenas para o cliente correto

### Cliente UDP Direto → Servidor
1. Cliente envia mensagem diretamente para Servidor UDP (porta 3000)
2. Servidor processa e envia resposta diretamente para o cliente

---

## Arquitetura de Processamento

O servidor utiliza um **event queue** para evitar condições de corrida:
1. Mensagens UDP são recebidas e convertidas em tarefas (Runnables)
2. Tarefas são enfileiradas na `eventQueue`
3. A cada tick, o `TickLoop` processa todas as tarefas da fila
4. Estado do jogo é atualizado de forma thread-safe

---

## Visibilidade

O sistema implementa **visibilidade limitada**:
- Cada jogador só vê objetos dentro do `VIEW_RADIUS`
- `VisibilityService` filtra o estado antes de enviar
- Reduz tráfego de rede e aumenta justiça do jogo
