package game;

import model.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//
// Representa o estado global do jogo.
// Mantém o mapa de frutas, jogadores conectados e o contador de ticks.
public class GameState {

    private final int gridSize; // Tamanho do mapa
    private final int[][] map;  // Mapa do jogo
    private int fruitCount;     // Contador de frutas
    private int tick;           // Contador de ticks

    private final Map<String, Player> players = new ConcurrentHashMap<>(); // Mapa de jogadores conectados, indexado por ID

    public GameState(int gridSize) {
        this.gridSize = gridSize;
        this.map = new int[gridSize][gridSize];
    }

    public int[][] getMap() { return map; }         // Retorna o mapa do jogo, onde 0 = vazio, 1 = fruta
    public int getGridSize() { return gridSize; }   // Retorna o tamanho do mapa

    public Map<String, Player> getPlayers() { return players; } // Retorna o mapa de jogadores conectados

    public int getFruitCount() { return fruitCount; } // Retorna o contador de frutas
    public void incrementFruit() { fruitCount++; }    // Incrementa o contador de frutas
    public void decrementFruit() { fruitCount--; }    // Decrementa o contador de frutas

    public int getTick() { return tick; } // Retorna o contador de ticks
    public void nextTick() { tick++; }    // Incrementa o contador de ticks
}