# Snake Game Server (Java)

Este repositório contém um servidor de jogo _Snake_ em Java, baseado em UDP.

---

## ✅ O que está implementado

- **Servidor UDP** que recebe comandos JSON (`join`, `input`, `leave`) dos clientes.
- **Loop de ticks** em `GameConfig.TICK_RATE` (padrão 10 Hz).
- **Manutenção do estado do jogo** (`GameState`) com jogadores e frutas.
- **Visibilidade local**: cada jogador só recebe a parte do mapa que está dentro do seu `VIEW_RADIUS`.
- **Detecção de colisões** com paredes, frutas e corpos de jogadores.
- **Geração de frutas** (na mesma proporção definida em `GameConfig.FRUIT_RATIO`).

---

## 🧩 Modelo de Classes (High level)

### Pacotes principais

- `game`
  - **`GameConfig`**: constantes globais (tamanho do mapa, taxa de tick, porta, etc.).
  - **`GameState`**: estado global do jogo (mapa, jogadores, tick, frutas).
  - **`GameEngine`**: lógica de execução dos ticks (entradas, movimento, colisões e geração de frutas).
  - **`TickLoop`**: agendador que dispara o tick a cada 1/TICK_RATE segundos.

- `server`
  - **`UdpServer`**: servidor UDP; recebe mensagens dos clientes e envia o estado visível para cada jogador.

- `service`
  - **`PlayerService`**: adiciona/remove jogadores + lógica de spawn + processamento de inputs e movimentação dos jogadores.
  - **`CollisionService`**: valida colisões (paredes, frutas, outros jogadores).
  - **`VisibilityService`**: filtra o estado para retornar apenas o que o jogador pode enxergar.
  - **`FruitService`**: gera frutas periodicamente conforme `GameConfig.FRUIT_RATIO`.

- `model`
  - **`Player`**: representação de cada jogador (corpo, direção, inputs pendentes, alive, etc.).
  - **`Position`**: `(x, y)` no grid.
  - **`Direction`**: enum de direção (`UP/DOWN/LEFT/RIGHT`).
  - **`Input`**: direção enviada pelo jogador.

- `util`
  - **`Utils`**: utilitários (direção inicial, posição futura, sanitação de nome, random).

---

## 🛠️ Como rodar (via terminal)

> Necessário ter JDK instalado (Java 8+).

```powershell
cd c:\Users\Dell\Desktop\snakegame\server
javac -d out src\**\*.java
java -cp out Main
```

O servidor escuta por padrão na porta **3000**.

---

## 📡 Protocolo UDP (JSON)

### Mensagens enviadas pelo cliente

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

### Estado retornado pelo servidor (para cada jogador)

O servidor envia um JSON com:

- `tick`: número do tick atual
- `fruits`: lista de frutas visíveis
- `players`: lista de jogadores visíveis (corpos de todos)
- `self`: informações do próprio jogador

Exemplo:

```json
{
  "tick": 123,
  "fruits": [{"x": 14, "y": 9}],
  "players": [{"id":"...,"body":[{"x":...,"y":...}]}],
  "self": {"id":"...","body":[...]}
}
```

---

## 🔍 O que estava faltando / apontamentos (corrigido)

### Correções aplicadas (servidor agora compila / roda):

- `Main.java` estava vazio (agora inicializa `UdpServer`).
- `Input` exigia um `tick` no construtor, mas o servidor chamava com apenas `dir`.
- `Player` não inicializava `body` e `direction`, levando a `NullPointerException` ao mover.
- `Player.grow()` duplicava a cabeça em vez de duplicar a cauda.
- `GameEngine.generateFood()` foi refatorado para `FruitService.generateFruits()`.
- Input e movimento agora são gerenciados por `PlayerService` e filtram `Player::isAlive`.

### Observações / possíveis melhorias

- `GameMap` atualmente não é utilizado (pode ser removido ou integrado).
- Ainda não há persistência ou remoção de jogadores mortos (apenas marcar `alive = false`).
- A lógica de spawn do jogador é básica (pode ser aprimorada para evitar colisões próximas ou spawn em áreas cheias).
- `UdpServer` utiliza `addr.toString()` como ID do jogador; em redes reais, clientes atrás de NAT podem compartilhar `SocketAddress`.

---

## 💡 Próximos passos sugeridos

- Implementar autenticação ou um `playerId` gerado no servidor (evitar usar IP/porta como ID).
- Suporte a múltiplas frutas por tick com controle de quantidade máxima.
- Remoção de jogadores inativos/mortos após certo número de ticks.
- Adicionar relatório de métricas ou logs estruturados.

---

Se quiseres, posso também criar um pequeno cliente JavaScript/HTML para se conectar ao servidor via UDP (WebRTC ou websockets) ou sugerir como testar com `netcat`/scripts Python.
