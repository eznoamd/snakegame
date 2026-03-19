package model;

//
// Classe com objetivo de centralizar um Input dado pelo user
// contém direção e tick (futuramente poderia guardar o timestamp)
public class Input {

    private Direction dir;
    private int tick;

    // Construtor completo, com direção e tick
    public Input(Direction dir, int tick) {
        this.dir = dir;
        this.tick = tick;
    }

    // Construtor simplificado, com apenas direção (tick pode ser setado depois ou ignorado)
    public Input(Direction dir) {
        this(dir, 0);
    }

    public Direction getDir() { return dir; }
    public int getTick() { return tick; }
}