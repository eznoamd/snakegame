package util;

import model.Position;
import model.Direction;

import java.util.Random;

public class Utils {

    private static final Random random = new Random();

    //
    // Escolhe a direção inicial com base na prioridade de escolha
    // se |x| > |y| a prioridade de escolha será em x, assim
    // escolhendo se ira para esquerda (menor que grid_size/2)
    // ou se ira para direita (maior que grid_size/2)
    public static Direction chooseDirection(int x, int y, int gridSize) {
        int modX = Math.abs(x);
        int modY = Math.abs(y);

        Direction xDir = Direction.RIGHT;
        double normX = (double) x / gridSize;
        if (normX > 0.5) xDir = Direction.LEFT;

        Direction yDir = Direction.DOWN;
        double normY = (double) y / gridSize;
        if (normY > 0.5) yDir = Direction.UP;

        return (modX <= modY) ? yDir : xDir;
    }

    //
    // Retorna um int aleatório levando em conta o max possivel
    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    //
    // Retorna a string de nome limpa
    public static String sanitizeName(String name, int limit) {
        if (name == null) return "Player";

        return name.trim()
                .substring(0, Math.min(name.length(), limit))
                .replaceAll("[^\\w ]", "");
    }

    //
    // Retorna para qual posição o player deve ir
    // com base na direção que esta ativa
    public static Position getNextPos(Position pos, Direction dir) {
        switch (dir) {
            case UP:
                return new Position(pos.x, pos.y - 1);
            case DOWN:
                return new Position(pos.x, pos.y + 1);
            case LEFT:
                return new Position(pos.x - 1, pos.y);
            case RIGHT:
                return new Position(pos.x + 1, pos.y);
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }
}