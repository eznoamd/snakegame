package game;

//
// Representa o estado global do jogo.
// Mantém o mapa de frutas, jogadores conectados e o contador de ticks.
import model.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private final int gridSize;
    private final int[][] map;
    private int fruitCount;
    private int tick;

    private final Map<String, Player> players = new ConcurrentHashMap<>();

    public GameState(int gridSize) {
        this.gridSize = gridSize;
        this.map = new int[gridSize][gridSize];
    }

    public int[][] getMap() { return map; }
    public int getGridSize() { return gridSize; }

    public Map<String, Player> getPlayers() { return players; }

    public int getFruitCount() { return fruitCount; }
    public void incrementFruit() { fruitCount++; }
    public void decrementFruit() { fruitCount--; }

    public int getTick() { return tick; }
    public void nextTick() { tick++; }
}