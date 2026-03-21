# Snake Game Server (Java)

## Classes da API v2

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

## Como rodar (via terminal)

> Necessário ter JDK instalado (Java 8+).

```powershell
cd c:\Users\Dell\Desktop\snakegame\server
javac -d out src\**\*.java
java -cp out Main
```

O servidor escuta por padrão na porta **3000**.

---

## Protocolo UDP (JSON)

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

```
{
  "tick": 123,
  "fruits": [{"x": 14, "y": 9}],
  "players": [{"id":"...,"body":[{"x":...,"y":...}]}],
  "self": {"id":"...","body":[...]}
}
```
