package service;

import config.GameConfig;
import game.GameState;
import model.Player;
import model.Position;
import util.Utils;

//
// Service com objetivo de centralizar a lógica de geração e gerenciamento de frutas
public class FruitService {

    //
    // Gera o maximo de frutas permitidas no tick 
    public void generateFruits(GameState state) {
        int gridSize = state.getGridSize();
        // Calcula o número máximo de frutas com base no tamanho total do mapa e na configuração de densidade de frutas
        int maxFruits = (int) (gridSize * gridSize * GameConfig.FRUIT_RATIO);
        // Calcula quantas frutas faltam para atingir o número máximo
        int missing = maxFruits - state.getFruitCount();
        if (missing <= 0) return;

        // Tenta gerar as frutas faltantes
        for (int i = 0; i < missing; i++) {
            for (int attempt = 0; attempt < 100; attempt++) {
                int x = Utils.randomInt(gridSize);
                int y = Utils.randomInt(gridSize);

                if (state.getMap()[x][y] == 1) continue;
                if (isOccupiedByPlayer(state, x, y)) continue;

                state.getMap()[x][y] = 1;
                state.incrementFruit();
                break;
            }
        }
    }

    //
    // Verifica se uma posição está ocupada por algum jogador
    private boolean isOccupiedByPlayer(GameState state, int x, int y) {
        for (Player p : state.getPlayers().values()) {
            for (Position pos : p.getBody()) {
                if (pos.x == x && pos.y == y) return true;
            }
        }
        return false;
    }
}