package service;

import game.GameConfig;
import game.GameState;
import model.Direction;
import model.Player;
import model.Position;
import util.Utils;

import java.net.SocketAddress;

//
// Service com objetivo de centralizar como o server deve gerenciar o player;
public class PlayerService {

    //
    // Adiciona um player ao jogo, criando um novo Player e colocando ele no GameState
    public void addPlayer(GameState state, String id, String name, SocketAddress addr) {
        if (state.getPlayers().containsKey(id)) return;

        name = Utils.sanitizeName(name, GameConfig.NAME_LIMIT);

        Position spawn = findSpawnPosition(state);
        Direction dir = Utils.chooseDirection(spawn.x, spawn.y, state.getGridSize());

        Player p = new Player(id, name, addr, spawn, dir);
        state.getPlayers().put(id, p);
    }

    //
    // Obtem uma posição para nascer o player, tentando evitar colisões com outros players
    private Position findSpawnPosition(GameState state) {
        int gridSize = state.getGridSize();
        int margin = 5;
        int min = margin;
        int max = Math.max(gridSize - margin, margin + 1);

        for (int attempt = 0; attempt < 500; attempt++) {
            int x = Utils.randomInt(max - min) + min;
            int y = Utils.randomInt(max - min) + min;

            if (isOccupied(state, x, y)) continue;
            return new Position(x, y);
        }

        return new Position(gridSize / 2, gridSize / 2);
    }

    //
    // Hellper para verificar se uma posição já está ocupada por algum player
    private boolean isOccupied(GameState state, int x, int y) {
        for (Player p : state.getPlayers().values()) {
            for (Position pos : p.getBody()) {
                if (pos.x == x && pos.y == y) return true;
            }
        }
        return false;
    }

    //
    // Processa os inputs de cada jogador vivo
    public void processInputs(GameState state) {
        state.getPlayers().values().stream()
                .filter(Player::isAlive)
                .forEach(Player::processInput);
    }

    //
    // Move os jogadores vivos de acordo com suas direções atuais
    public void movePlayers(GameState state) {
        state.getPlayers().values().stream()
                .filter(Player::isAlive)
                .forEach(Player::move);
    }

    // Remove um player do jogo, caso ele tenha morrido ou desconectado
    public void removePlayer(GameState state, String id) {
        state.getPlayers().remove(id);
    }
}