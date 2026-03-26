package service;

import game.GameState;
import model.Player;
import model.Position;

import java.util.*;

//
// Service com objetivo de centralizar a contrução do visibility state,
// especificando apenas o que o player ira visualizar
public class VisibilityService {

    private final int viewRadius;

    public VisibilityService(int viewRadius) {
        this.viewRadius = viewRadius;
    }

    //
    // Controi a visibility state para retornar ao player
    public Map<String, Object> buildState(GameState state, String playerId) {

        Player self = state.getPlayers().get(playerId);
        if (self == null) return null;  

        if (self.getBody().isEmpty()) return null;
        Position head = self.getHead();
    
        Map<String, Object> result = new HashMap<>();
        result.put("tick", state.getTick());

        List<Map<String, Integer>> fruits = new ArrayList<>();

        for (int x = head.x - viewRadius; x <= head.x + viewRadius; x++) {
            for (int y = head.y - viewRadius; y <= head.y + viewRadius; y++) {

                if (inBounds(x, y, state) && state.getMap()[x][y] == 1) {
                    fruits.add(posToMap(x, y));
                }
            }
        }

        result.put("fruits", fruits);

        List<Map<String, Object>> players = new ArrayList<>();

        for (Player p : state.getPlayers().values()) {

            //if (!p.isAlive()) continue;

            if (!isPlayerVisible(p, head)) continue;

            Map<String, Object> pData = new HashMap<>();
            pData.put("id", p.getId());
            pData.put("body", bodyToList(p));

            players.add(pData);
        }

        result.put("players", players);

        Map<String, Object> selfData = new HashMap<>();
        selfData.put("id", self.getId());
        selfData.put("body", bodyToList(self));
        selfData.put("alive", self.isAlive());

        result.put("self", selfData);

        return result;
    }

    //
    // Helppers
    //

    //
    // Verifica se o (x, y) estão dentro do mapa
    private boolean inBounds(int x, int y, GameState state) {
        int size = state.getGridSize();
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    //
    // Verifica se o player em questão é visivel
    private boolean isPlayerVisible(Player p, Position center) {
        for (Position pos : p.getBody()) {
            if (Math.abs(pos.x - center.x) <= viewRadius &&
                    Math.abs(pos.y - center.y) <= viewRadius) {
                return true;
            }
        }
        return false;
    }

    //
    // Transforma a lista de positions (body) em uma lista de maps -> List('x': x, 'y': y)
    private List<Map<String, Integer>> bodyToList(Player p) {
        List<Map<String, Integer>> body = new ArrayList<>();

        for (Position pos : p.getBody()) {
            body.add(posToMap(pos.x, pos.y));
        }

        return body;
    }

    //
    // Mapeia a position em um map (x: position.x, y: position.y )
    private Map<String, Integer> posToMap(int x, int y) {
        Map<String, Integer> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        return map;
    }
}