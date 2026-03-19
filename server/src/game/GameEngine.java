package game;

//
// Engine principal do jogo.
// Executa o ciclo de atualização (tick) e aplica:
//  - processamento de inputs
//  - movimentação de jogadores
//  - resolução de colisões
//  - geração de frutas
//
import model.Player;
import model.Position;
import service.CollisionService;
import util.Utils;

public class GameEngine {

    private final GameState state;

    private final CollisionService collisionService = new CollisionService();

    public GameEngine(GameState state) {
        this.state = state;
    }

    public void tick() {
        state.nextTick();

        processInputs();
        movePlayers();
        collisionService.resolve(state);

        if (state.getTick() % 10 == 0) {
            generateFood();
        }
    }

    private void processInputs() {
        state.getPlayers().values().stream()
                .filter(Player::isAlive)
                .forEach(Player::processInput);
    }

    private void movePlayers() {
        state.getPlayers().values().stream()
                .filter(Player::isAlive)
                .forEach(Player::move);
    }

    private void generateFood() {
        int gridSize = state.getGridSize();
        int maxFruits = (int) (gridSize * gridSize * GameConfig.FRUIT_RATIO);
        int missing = maxFruits - state.getFruitCount();
        if (missing <= 0) return;

        for (int i = 0; i < missing; i++) {
            for (int attempt = 0; attempt < 100; attempt++) {
                int x = Utils.randomInt(gridSize);
                int y = Utils.randomInt(gridSize);

                if (state.getMap()[x][y] == 1) continue;
                if (isOccupiedByPlayer(x, y)) continue;

                state.getMap()[x][y] = 1;
                state.incrementFruit();
                break;
            }
        }
    }

    private boolean isOccupiedByPlayer(int x, int y) {
        for (Player p : state.getPlayers().values()) {
            for (Position pos : p.getBody()) {
                if (pos.x == x && pos.y == y) return true;
            }
        }
        return false;
    }
}