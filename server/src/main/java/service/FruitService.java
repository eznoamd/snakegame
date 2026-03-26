package service;

import config.GameConfig;
import game.GameState;
import model.Player;
import model.Position;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

//
// Service com objetivo de centralizar a lógica de geração e gerenciamento de frutas
public class FruitService {

    // Para evitar gerar muitas frutas de uma vez e causar lag, limitamos a geração por tick
    private static final int MAX_GEN_PER_TICK = 5;

    //
    // Gera o maximo de frutas permitidas no tick 
    public void generateFruits(GameState state) {
        int gridSize = state.getGridSize();
        // Calcula o número máximo de frutas com base no tamanho total do mapa e na configuração de densidade de frutas
        int maxFruits = (int) (gridSize * gridSize * GameConfig.FRUIT_RATIO);
        // Calcula quantas frutas faltam para atingir o número máximo
        int missing = maxFruits - state.getFruitCount();
        if (missing <= 0) return;

        // Só tentamos gerar um pouco por vez para manter o tick estável (100ms)
        int attemptsThisTick = Math.min(missing, MAX_GEN_PER_TICK);

        // Tenta gerar as frutas faltantes
        for (int i = 0; i < attemptsThisTick; i++) {
            // limite de 30 tentativas aleatórias para achar um spot vazio
            for (int attempt = 0; attempt < 30; attempt++) {
                int x = Utils.randomInt(gridSize);
                int y = Utils.randomInt(gridSize);

                // Se a posição já tem fruta ou tem um player (VIVO), pula
                if (state.getMap()[x][y] == 1 || isOccupiedByActivePlayer(state, x, y)) {
                    continue;
                }

                state.getMap()[x][y] = 1;
                state.incrementFruit();
                break;
            }
        }
    }

    //
    // Verifica se uma posição está ocupada por algum jogador
    private boolean isOccupiedByActivePlayer(GameState state, int x, int y) {
        for (Player p : state.getPlayers().values()) {
            if (!p.isAlive()) continue;
            List<Position> snapshot = new ArrayList<>(p.getBody()); // ← adicione isso

            for (Position pos : snapshot) { // ← itere o snapshot
                if (pos.x == x && pos.y == y) return true;
            }
        }
        return false;
    }
}