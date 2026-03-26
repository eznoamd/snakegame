package service;

import game.GameState;
import model.Player;
import model.Position;

import java.util.ArrayList;
import java.util.List;

//
// Service com objetivo de centralizar a lógica de verificar colisões no mapa
//
// Tipos de colisão:
//  - fora do mapa
//  - bater em algum player
//  - pegar frutas
public class CollisionService {

    // Verifica se possuí alguma colisão
    public void resolve(GameState state) {

        // para cada player
        for (Player p : state.getPlayers().values()) {
            if (!p.isAlive()) continue;  // evita fazer verificação com player morto

            verifyOutOfBounds(p, state);
            verifyFruitCollision(p, state);

            for (Player other : state.getPlayers().values()) {
                verifyHitPlayer(p, other, state);
            }
        }
    }

    //
    // Verifica se o player esta com a head fora do mapa e seta como (alive = false)
    private void verifyOutOfBounds(Player player, GameState state){
        int size = state.getGridSize();
        Position p = player.getHead();
        if (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size){
            player.setAlive(false);
        }
    }

    //
    // Verifica se o player esta com a head em cima de uma fruta, caso sim,
    // remove aquela fruta do mapa, diminui a quantidade de frutas e adiciona um body no player
    private void verifyFruitCollision(Player player, GameState state){
        Position head = player.getHead();
        if (state.getMap()[head.x][head.y] == 1) {
            state.getMap()[head.x][head.y] = 0;
            state.decrementFruit();
            player.grow();
            player.addFruit();
        }
    }

    //
    // Verifica se o player esta com a head em cima de um body alheio ou proprio,
    // seta como morto caso verdadeiro
    private void verifyHitPlayer(Player player, Player other, GameState state){
        int index = 0;

        Position head = player.getHead();
        List<Position> snapshot = new ArrayList<>(other.getBody());

        for (Position seg : snapshot) {
            // Evita verificar a colisão com a propria cabeça
            if ((player == other) && index == 0) {
                index++;
                continue;
            }

            // Verifica qualquer colisão e seta como morto caso verdadeiro
            if (seg.x == head.x && seg.y == head.y) {
                player.setAlive(false);
                return;
            }

            index++;
        }
    }
}